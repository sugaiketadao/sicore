package com.onepg.util;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;


/**
 * Log writer class.<br>
 * <ul>
 * <li>Accepts log output from individual processes.</li>
 * <li>Handles log text formatting.</li>
 * <li>Gets text writer instances from log text handler <code>LogTxtHandler</code> to output logs.</li>
 * <li>Holds two log text handlers for information and error.</li>
 * <li>Outputs logs to console in development mode.</li>
 * <li>Opening and closing of log text is handled by the log text handler, not by this class.</li>
 * </ul>
 */
public final class LogWriter {

  /** Generation class name. */
  private final String clsName;
  /** Parent process ID. */
  private final long ppid;
  /** Thread name. */
  private final String threadName;
  /** Information log text handler. */
  private final LogTxtHandler infHdr;
  /** Error log text handler. */
  private final LogTxtHandler errHdr;
  /** Console writer (for development). */
  private final PrintWriter console;

  /** Trace code. */
  private final String traceCode;
  /** Information log prefix. */
  private final String infPrefix;
  /** Error log prefix. */
  private final String errPrefix;
  /** Development log prefix. */
  private final String devPrefix;
  /** Begin/end log suffix. */
  private final String beginEndSuffix;
  /** Development mode flag. */
  private final boolean isDevelopMode;
  /** Elapsed time measurement start time. */
  private long watchStartTime = 0;

  /** Date-time formatter: timestamp fully compliant with ISO 8601. */
  private static final DateTimeFormatter DTF_LOG_TIMESTAMP = DateTimeFormatter
      .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Constructor.
   *
   * @param cls               log target class
   * @param ppid              parent process ID
   * @param traceCode         trace code (optional)
   * @param isDevelopMode     development mode
   * @param infLogFileHandler information log text handler
   * @param errLogFileHandler error log text handler
   * @param consoleWriter     console writer
   */
  LogWriter(final Class<?> cls, final long ppid, final String traceCode, final boolean isDevelopMode,
      final LogTxtHandler infLogTxtHandler, final LogTxtHandler errLogTxtHandler,
      final PrintWriter consoleWriter) {

    this.clsName = cls.getName();
    this.ppid = ppid;
    this.threadName = Thread.currentThread().getName();
    this.traceCode = ValUtil.nvl(traceCode);
    this.isDevelopMode = isDevelopMode;
    this.infHdr = infLogTxtHandler;
    this.errHdr = errLogTxtHandler;
    this.console = consoleWriter;
    final String prefixTraceCode;
    if (ValUtil.isBlank(this.traceCode)) {
      prefixTraceCode = ValUtil.BLANK;
    } else {
      prefixTraceCode = " #" + this.traceCode;
    }
    this.infPrefix = prefixTraceCode + " [INF] ";
    this.errPrefix = prefixTraceCode + " [ERR] ";
    this.devPrefix = prefixTraceCode + " [DEV] ";
    this.beginEndSuffix = LogUtil.joinKeyVal("class", this.clsName, "ppid", this.ppid, "thread", this.threadName);
  }

  /**
   * Flushes.
   */
  public void flush() {
    try {
      this.infHdr.getWriter().flush();
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while flushing the info log. ");
    }
    
    try {
      this.errHdr.getWriter().flush();
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while flushing the error log. ");
    }
  }

  /**
   * Creates log message.
   *
   * @param prefix prefix
   * @param msg log message
   * @return log message
   */
  private String createMsg(final String prefix, final String msg) {
    final String tm = LocalDateTime.now().format(DTF_LOG_TIMESTAMP);
    final String log = tm + prefix + ValUtil.nvl(msg);
    return log;
  }

  /**
   * Common log output processing.
   */
  private void writeLog(final String prefix, final String msg, final boolean toErrorLog, 
                     final String stackTrace) {
    final String log = createMsg(prefix, msg);
    
    try {
      // Outputs to information log
      this.infHdr.getWriter().println(log);
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while writing to the info log. " + LogUtil.joinKeyVal("log", log));
    }

    // When also outputting to error log
    if (toErrorLog) {
      try {
        this.errHdr.getWriter().println(log);
        if (stackTrace != null) {
          this.errHdr.getWriter().println(stackTrace);
        }
      } catch (Exception e) {
        // Suppresses errors during log processing
        LogUtil.stdout(e, "An exception occurred while writing to the error log. " + LogUtil.joinKeyVal("log", log));
      }
    }
    
    // Console output in development mode
    if (this.isDevelopMode) {
      this.console.println(log);
      if (stackTrace != null) {
        this.console.println(stackTrace);
      }
    }
  }

  /**
   * Outputs error.
   *
   * @param e   error instance
   * @param msg log output message
   */
  public void error(final Throwable e, final String msg) {
    final String etrace;
    if (ValUtil.isNull(e)) {
      etrace = null;
    } else {
      etrace = LogUtil.getStackTrace(ValUtil.LF, e);
    }
    writeLog(this.errPrefix, msg, true, etrace);
  }

  /**
   * Outputs error.
   *
   * @param e   error instance
   */
  public void error(final Throwable e) {
    error(e, ValUtil.BLANK);
  }

  /**
   * Outputs error.
   *
   * @param msg log output message
   */
  public void error(final String msg) {
    error(null, msg);
  }

  /**
   * Outputs information.
   *
   * @param msg log output message
   */
  public void info(final String msg) {
    writeLog(this.infPrefix, msg, false, null);
  }

  /**
   * Outputs start information.
   *
   */
  public void begin() {
    final String log = createMsg(this.infPrefix, "<begin> " + this.beginEndSuffix);

    try {
      this.infHdr.getWriter().println(log);
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while writing to the begin log. " + LogUtil.joinKeyVal("log", log));
    }
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs end information.
   *
   */
  public void end() {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix);

    try {
      this.infHdr.getWriter().println(log);
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while writing to the end log. " + LogUtil.joinKeyVal("log", log));
    }
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs end information.
   * @param exitStatus exit status
   */
  public void end(final int exitStatus) {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix + " " + LogUtil.joinKeyVal("status", exitStatus));

    try {
      this.infHdr.getWriter().println(log);
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while writing to the end log. " + LogUtil.joinKeyVal("log", log));
    }
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs for development.
   *
   * @param msg log output message
   */
  public void develop(final String msg) {
    if (!this.isDevelopMode) {
      return;
    }
    final String log = createMsg(this.devPrefix, msg);

    try {
      this.infHdr.getWriter().println(log);
    } catch (Exception e) {
      // Suppresses errors during log processing
      LogUtil.stdout(e, "An exception occurred while writing to the develop log. " + LogUtil.joinKeyVal("log", log));
    }
    this.console.println(log);
  }

  /**
   * Checks development mode.
   *
   * @return <code>true</code> if development log is enabled
   */
  public boolean isDevelopMode() {
    return this.isDevelopMode;
  }

  /**
   * Starts elapsed time measurement.
   */
  public void startWatch() {
    this.watchStartTime = System.currentTimeMillis();
    this.info("<stopwatch> start");
  }

  /**
   * Stops elapsed time measurement.
   */
  public void stopWatch() {
    if (this.watchStartTime == 0) {
      this.error("<stopwatch> Not started.");
      return;
    }
    
    final long elapsedMillis = System.currentTimeMillis() - this.watchStartTime;
    final String formattedTime = milliSecToHmsSss(elapsedMillis);
    this.info("<stopwatch> stop time=" + formattedTime);
    
    // Resets the stopwatch
    this.watchStartTime = 0;
  }

  // Adds constants
  private static final long MILLIS_PER_SECOND = 1_000L;
  private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

  /**
   * Formats elapsed time.
   */
  private String milliSecToHmsSss(final long millis) {
    final long hours = millis / MILLIS_PER_HOUR;
    final long minutes = (millis / MILLIS_PER_MINUTE) % 60;
    final long seconds = (millis / MILLIS_PER_SECOND) % 60;
    final long milliseconds = millis % MILLIS_PER_SECOND;
    
    return "%02d:%02d:%02d.%03d".formatted(hours, minutes, seconds, milliseconds);
  }
}
