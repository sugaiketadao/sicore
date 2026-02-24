package com.onepg.util;

import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Log text handler class.<br>
 * <ul>
 * <li>Pools its own instances internally and returns instances from the pool for the same file path.</li>
 * <li>Holds text writer instances internally and returns them to the log writer <code>LogWriter</code>.</li>
 * <li>Handles opening and closing of log text.</li>
 * <li>Rolls the file when the date changes.</li>
 * <li>The text writer class serializes output even when called from parallel threads.</li>
 * </ul>
 * @hidden
 */
public final class LogTxtHandler implements AutoCloseable {

  /** Information log file configuration key suffix. */
  private static final String INF_FILE_PROP_KEY_SUFFIX = ".inf.file";
  /** Error log file configuration key suffix. */
  private static final String ERR_FILE_PROP_KEY_SUFFIX = ".err.file";

  /** Log text handler pool map &lt;file path, log text handler&gt; (singleton). */
  private static final Map<String, LogTxtHandler> logTxtPoolMaps_ = new ConcurrentHashMap<>();

  /** Base file path (without extension). */
  private final String baseFilePath;
  /** Extension (with dot). */
  private final String fileTypeMark;
  /** File path (base file path + extension). */
  private final String filePath;
  /** Previous output date (YYYYMMDD). */
  private String beforePrintDate = null;

  /** Text writer (serialized output). */
  private TxtSerializeWriter tw = null;

  /** Date-time formatter: date. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Gets the log text handler.<br>
   * <ul>
   * <li>If the same file path exists in the pool, returns the instance from the pool.</li>
   * <li>If it does not exist in the pool, creates and returns an instance.</li>
   * </ul>
   *
   * @param keyPrefix configuration key prefix
   * @param isErr <code>true</code> for error log
   * @return log text handler
   */
  static LogTxtHandler getInstance(final String keyPrefix, final boolean isErr) {
    final String key;
    if (isErr) {
      key = keyPrefix + ERR_FILE_PROP_KEY_SUFFIX;
    } else {
      key = keyPrefix + INF_FILE_PROP_KEY_SUFFIX;
    }
    // Gets configuration
    if (!LogUtil.PROP_MAP.containsKey(key)) {
      throw new RuntimeException("Property does not exist. "
          + LogUtil.joinKeyVal("file", FwPropertiesName.LOG.toString(), "key", key));
    }
    // Log file path (already converted to absolute path)
    final String logPath = LogUtil.PROP_MAP.getString(key);

    // If already generated, returns the pooled handler
    if (logTxtPoolMaps_.containsKey(logPath)) {
      return logTxtPoolMaps_.get(logPath);
    }

    // Generates log text handler
    synchronized (logTxtPoolMaps_) {
      // Checks again, and if already generated, returns it (checks above as well to avoid performance degradation)
      if (logTxtPoolMaps_.containsKey(logPath)) {
        return logTxtPoolMaps_.get(logPath);
      }

      if (LogUtil.isDevelopMode()) {
        LogUtil.stdout("Creates a log text handler. " + LogUtil.joinKeyVal("path", logPath));
      }
      // Log text handler
      final LogTxtHandler lfh = new LogTxtHandler(logPath);
      // Stores the log text handler in the map
      logTxtPoolMaps_.put(logPath, lfh);
      return lfh;
    }
  }

  /**
   * Closes the log text handler.<br>
   * <ul>
   * <li>Closes all pooled log text handlers.</li>
   * </ul>
   */
  public static synchronized void closeAll() {
    // Creates a copy of keys and iterates because the close processing of LogTxtHandler deletes from the pool
    for (final String key : new ArrayList<>(logTxtPoolMaps_.keySet())) {
      final LogTxtHandler handler = logTxtPoolMaps_.get(key);
      if (ValUtil.isNull(handler)) {
        continue;
      } 
      try {
        if (LogUtil.isDevelopMode()) {
          LogUtil.stdout("Closes the log text handler. " + LogUtil.joinKeyVal("path", key));
        }
        handler.close();
      } catch (final Exception e) {
        // Suppresses errors during log close (because errors may occur during log output)
        LogUtil.stdout(e, "An exception occurred while closing the log text handler. " + LogUtil.joinKeyVal("path", key));
      }
    }
    logTxtPoolMaps_.clear();
  }


  /**
   * Constructor.
   *
   * @param baseFilePath base file path
   */
  private LogTxtHandler(final String baseFilePath) {

    final String[] tmp = FileUtil.splitFileTypeMark(baseFilePath);
    this.baseFilePath = tmp[0];
    this.fileTypeMark = "." + tmp[1];
    this.filePath = this.baseFilePath + this.fileTypeMark;

    // If the file from the previous startup remains, uses the file modified date as the previous output date
    if (FileUtil.exists(this.filePath)) {
      final String modDt = FileUtil.getFileModifiedDateTime(this.filePath);
      this.beforePrintDate = modDt.substring(0, 8);
      if (!ValUtil.isDate(this.beforePrintDate)) {
        throw new RuntimeException("File modified date is invalid. "
            + LogUtil.joinKeyVal("path", this.filePath, "modDate", modDt, "beforePrintDate", this.beforePrintDate));
      }
    } else {
      final String nowDate = LocalDateTime.now().format(DTF_DATE);
      this.beforePrintDate = nowDate;
    }
    // Opens file
    open();
  }

  /**
   * Opens the file.
   */
  private void open() {
    this.tw = new TxtSerializeWriter(this.filePath, LineSep.LF, CharSet.UTF8, false, true, false);
  }

  /**
   * Closes the file.
   */
  @Override
  public void close() {
    if (this.tw != null) {
      try {
        this.tw.close();
      } catch (final Exception e) {
        // Suppresses errors during log close, but outputs for debugging
        LogUtil.stdout(e, "An exception occurred while closing the text writer. " + LogUtil.joinKeyVal("path", this.tw.getFilePath()));
      } finally {
        this.tw = null;
        // Removes this instance from the pool
        logTxtPoolMaps_.remove(this.filePath);
      }
    }
  }

  /**
   * Gets the text writer.<br>
   * <ul>
   * <li>If the date has changed from the previous time, executes file rolling.</li>
   * </ul>
   */
  TxtSerializeWriter getWriter() {
    final String nowDate = LocalDateTime.now().format(DTF_DATE);
    if (!nowDate.equals(this.beforePrintDate)) {
      rolling(nowDate);
    }
    if (ValUtil.isNull(this.tw)) {
      throw new RuntimeException("Text writer is not available. " + LogUtil.joinKeyVal("path", this.filePath));
    }
    return this.tw;
  }

  /**
   * Executes file rolling.<br>
   * <ul>
   * <li>Closes the file, renames it, and opens a new file.</li>
   * </ul>
   *
   * @param newDate date after rolling
   */
  private synchronized void rolling(final String newDate) {
    // Rechecks if being processed from another thread
    if (newDate.equals(this.beforePrintDate)) {
      return;
    }

    final String destPath = this.baseFilePath + "_" + this.beforePrintDate + this.fileTypeMark;
    if (FileUtil.exists(destPath)) {
      // Basically impossible, but if a dated file already exists, does not rename
      // To continue even on rolling failure, outputs error log and continues processing
      LogUtil.stdout("Dated file already exists. " + LogUtil.joinKeyVal("path", this.filePath));
      this.beforePrintDate = newDate;
      return;
    }
    
    // Calling close() would remove this from the pool, so directly closes the text writer before moving the file
    try {
      this.tw.close();
    } catch (final Exception e) {
      // Suppresses errors during log close, but outputs for debugging
      LogUtil.stdout(e, "An exception occurred while closing the text writer. " + LogUtil.joinKeyVal("path", this.tw.getFilePath()));
      return;
    }
    this.tw = null;
    try {
      if (FileUtil.exists(this.filePath)) {
        FileUtil.move(this.filePath, destPath);
      }
      open();
      this.beforePrintDate = newDate;
    } catch (final Exception e) {
      // To continue even on rolling failure, outputs error log and continues processing
      LogUtil.stdout(e, "An exception occurred during file rolling. " + LogUtil.joinKeyVal("path", this.filePath));
      // Reopens the file and continues processing
      if (ValUtil.isNull(this.tw)) {
        open();
      }
      this.beforePrintDate = newDate;
    }
  }
}
