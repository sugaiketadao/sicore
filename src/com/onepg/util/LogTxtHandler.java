package com.onepg.util;

import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Log text handler class.
 * <ul>
 * <li>Pools its own instances internally and returns the pooled instance for the same file path.</li>
 * <li>Holds a text writer instance internally and returns it to the log writer <code>LogWriter</code>.</li>
 * <li>Handles opening and closing of log text.</li>
 * <li>Rolls the file when the date changes.</li>
 * <li>The text writer class serializes output even when called from parallel threads.</li>
 * </ul>
 * @hidden
 */
public final class LogTxtHandler implements AutoCloseable {

  /** Info log file property key suffix. */
  private static final String INF_FILE_PROP_KEY_SUFFIX = ".inf.file";
  /** Error log file property key suffix. */
  private static final String ERR_FILE_PROP_KEY_SUFFIX = ".err.file";

  /** Log text handler pool map <file path, log text handler> (singleton). */
  private static final Map<String, LogTxtHandler> logTxtPoolMaps_ = new HashMap<>();

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
   * Returns the log text handler.
   * <ul>
   * <li>Returns the pooled instance if the same file path exists in the pool.</li>
   * <li>Creates and returns a new instance if not in the pool.</li>
   * </ul>
   *
   * @param keyPrefix the property key prefix
   * @param isErr <code>true</code> for error log
   * @return the log text handler
   */
  static LogTxtHandler getInstance(final String keyPrefix, final boolean isErr) {
    final String key;
    if (isErr) {
      key = keyPrefix + ERR_FILE_PROP_KEY_SUFFIX;
    } else {
      key = keyPrefix + INF_FILE_PROP_KEY_SUFFIX;
    }
    // Gets property
    if (!LogUtil.PROP_MAP.containsKey(key)) {
      throw new RuntimeException("Property does not exist. "
          + LogUtil.joinKeyVal("file", FwPropertiesName.LOG.toString(), "key", key));
    }
    // Log file path (converted to absolute path)
    final String logPath = LogUtil.PROP_MAP.getString(key);

    // Returns pooled handler if already created
    if (logTxtPoolMaps_.containsKey(logPath)) {
      return logTxtPoolMaps_.get(logPath);
    }

    // Creates log text handler
    synchronized (logTxtPoolMaps_) {
      // Checks again; returns if already created (checks above too to avoid performance degradation)
      if (logTxtPoolMaps_.containsKey(logPath)) {
        return logTxtPoolMaps_.get(logPath);
      }

      LogUtil.stdout("Creates a log text handler. " + LogUtil.joinKeyVal("path", logPath));
      // Log text handler
      final LogTxtHandler lfh = new LogTxtHandler(logPath);
      // Stores log text handler in map
      logTxtPoolMaps_.put(logPath, lfh);
      return lfh;
    }
  }

  /**
   * Closes the log text handler.
   * <ul>
   * <li>Closes all pooled log text handlers.</li>
   * </ul>
   *
   * @return <code>true</code> if any handler was closed
   */
  public static synchronized boolean closeAll() {
    boolean ret = false;
    // Creates a copy of keys to iterate since LogTxtHandler close removes from pool
    for (final String key : new ArrayList<>(logTxtPoolMaps_.keySet())) {
      final LogTxtHandler handler = logTxtPoolMaps_.get(key);
      if (ValUtil.isNull(handler)) {
        continue;
      } 
      try {
        LogUtil.stdout("Closes the log text handler. " + LogUtil.joinKeyVal("path", key));
        handler.close();
        ret = true;
      } catch (final Exception e) {
        // Swallows errors during log close (errors may occur during log output)
        LogUtil.stdout(e, "An exception occurred while closing the log text handler. " + LogUtil.joinKeyVal("path", key));
      }
    }
    logTxtPoolMaps_.clear();
    return ret;
  }


  /**
   * Constructor.
   *
   * @param baseFilePath the base file path
   */
  private LogTxtHandler(final String baseFilePath) {

    final String[] tmp = FileUtil.splitFileTypeMark(baseFilePath);
    this.baseFilePath = tmp[0];
    this.fileTypeMark = "." + tmp[1];
    this.filePath = this.baseFilePath + this.fileTypeMark;

    // If file remains from previous startup, uses file modified date as previous output date
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
  private final void open() {
    this.tw = new TxtSerializeWriter(this.filePath, true, false, LineSep.LF,
        CharSet.UTF8, false);
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
        // Swallows errors during log close but outputs for debugging
        LogUtil.stdout(e, "An exception occurred while closing the text writer. " + LogUtil.joinKeyVal("path", this.tw.getFilePath()));
      } finally {
        this.tw = null;
        // Removes this instance from pool
        logTxtPoolMaps_.remove(this.filePath);
      }
    }
  }

  /**
   * Returns the text writer.
   * <ul>
   * <li>Executes file rolling if the date has changed from the previous one.</li>
   * </ul>
   *
   * @return the text writer
   */
  TxtSerializeWriter getWriter() {
    final String nowDate = LocalDateTime.now().format(DTF_DATE);
    if (!nowDate.equals(this.beforePrintDate)) {
      rolling(nowDate);
    }
    return this.tw;
  }

  /**
   * Executes file rolling.
   * <ul>
   * <li>Closes the file, renames it, and opens a new file.</li>
   * </ul>
   *
   * @param newDate the date after rolling
   */
  private synchronized void rolling(final String newDate) {
    // Checks again if processed by another thread
    if (newDate.equals(this.beforePrintDate)) {
      return;
    }

    final String destPath = this.baseFilePath + "_" + this.beforePrintDate + this.fileTypeMark;
    if (FileUtil.exists(destPath)) {
      // Does not rename if dated file already exists (normally should not happen)
      // Outputs error log and continues since rolling failure should continue processing
      LogUtil.stdout("Dated file already exists. " + LogUtil.joinKeyVal("path", this.filePath));
      this.beforePrintDate = newDate;
      return;
    }
    
    try {
      close();
      if (FileUtil.exists(this.filePath)) {
        FileUtil.move(this.filePath, destPath);
      }
      open();
      this.beforePrintDate = newDate;
    } catch (final Exception e) {
      // Outputs error log and continues since rolling failure should continue processing
      LogUtil.stdout(e, "An exception occurred during file rolling. " + LogUtil.joinKeyVal("path", this.filePath));
      // Re-opens file and continues processing
      if (ValUtil.isNull(this.tw)) {
        open();
      }
      this.beforePrintDate = newDate;
    }
  }
}
