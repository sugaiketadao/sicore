package com.onepg.util;

import com.onepg.util.ValUtil.LineSep;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;


/**
 * Log writer class.
 * <ul>
 * <li>Accepts log output from individual processes.</li>
 * <li>Handles formatting of log text.</li>
 * <li>Gets text writer instance from log text handler <code>LogTxtHandler</code> and outputs logs.</li>
 * <li>Holds two log text handlers: one for info and one for errors.</li>
 * <li>Outputs logs to console as well in develop mode.</li>
 * <li>Log text opening and closing is handled by log text handler; this class does not open or close.</li>
 * </ul>
 */
public final class LogWriter {

  /** Created class name. */
  private final String clsName;
  /** Thread name. */
  private final String threadName;
  /** Info log text handler. */
  private final LogTxtHandler infHdr;
  /** Error log text handler. */
  private final LogTxtHandler errHdr;
  /** Console writer (for development). */
  private final PrintWriter console;

  /** Trace code. */
  private final String traceCode;
  /** Info log prefix. */
  private final String infPrefix;
  /** Error log prefix. */
  private final String errPrefix;
  /** Develop log prefix. */
  private final String devPrefix;
  /** Begin-end log suffix. */
  private final String beginEndSuffix;
  /** Develop mode flag. */
  private final boolean isDevelopMode;
  /** Elapsed time measurement start time. */
  private long watchStartTime = 0;

  /** Date-time formatter: timestamp ISO 8601 compliant. */
  private static final DateTimeFormatter DTF_LOG_TIMESTAMP = DateTimeFormatter
      .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Constructor.
   *
   * @param cls               the target class for logging
   * @param traceCode         the trace code (optional)
   * @param isDevelopMode     the develop mode
   * @param infLogTxtHandler  the info log text handler
   * @param errLogTxtHandler  the error log text handler
   * @param consoleWriter     the console writer
   */
  LogWriter(final Class<?> cls, final String traceCode, final boolean isDevelopMode,
      final LogTxtHandler infLogTxtHandler, final LogTxtHandler errLogTxtHandler,
      final PrintWriter consoleWriter) {

    this.clsName = cls.getName();
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
    this.beginEndSuffix = LogUtil.joinKeyVal("class", this.clsName, "thread", this.threadName);
  }

  /**
   * Flushes the log writers.
   */
  public void flush() {
    try {
        this.infHdr.getWriter().flush();
    } catch (Exception e) {
        // Swallows errors during log processing
        LogUtil.stdout(e, "An exception occurred while flushing the info log. ");
    }
    
    try {
        this.errHdr.getWriter().flush();
    } catch (Exception e) {
        // Swallows errors during log processing
        LogUtil.stdout(e, "An exception occurred while flushing the error log. ");
    }
  }

  /**
   * Creates a log message.
   *
   * @param prefix the prefix
   * @param msg the log message
   * @return the log message
   */
  private String createMsg(final String prefix, final String msg) {
    final String tm = LocalDateTime.now().format(DTF_LOG_TIMESTAMP);
    final String log = tm + prefix + ValUtil.nvl(msg);
    return log;
  }

  /**
   * Common log output processing.
   *
   * @param prefix the prefix
   * @param msg the message
   * @param toErrorLog <code>true</code> to output to error log as well
   * @param stackTrace the stack trace
   */
  private void writeLog(final String prefix, final String msg, final boolean toErrorLog, 
                     final String stackTrace) {
    final String log = createMsg(prefix, msg);
    
    // Outputs to info log
    this.infHdr.getWriter().println(log);
    
    // Also outputs to error log if specified
    if (toErrorLog) {
        this.errHdr.getWriter().println(log);
        if (stackTrace != null) {
            this.errHdr.getWriter().println(stackTrace);
        }
    }
    
    // Console output in develop mode
    if (this.isDevelopMode) {
        this.console.println(log);
        if (stackTrace != null) {
            this.console.println(stackTrace);
        }
    }
  }

  /**
   * Outputs an error.
   *
   * @param e   the error instance
   * @param msg the log message
   */
  public void error(final Throwable e, final String msg) {
    final String etrace;
    if (ValUtil.isNull(e)) {
      etrace = null;
    } else {
      etrace = LogUtil.getStackTrace(LineSep.LF.toString(), e);
    }
    writeLog(this.errPrefix, msg, true, etrace);
  }

  /**
   * Outputs an error.
   *
   * @param e   the error instance
   */
  public void error(final Throwable e) {
    error(e, ValUtil.BLANK);
  }

  /**
   * Outputs an error.
   *
   * @param msg the log message
   */
  public void error(final String msg) {
    error(null, msg);
  }

  /**
   * Outputs info.
   *
   * @param msg the log message
   */
  public void info(final String msg) {
    writeLog(this.infPrefix, msg, false, null);
  }

  /**
   * Outputs begin info.
   */
  public void begin() {
    final String log = createMsg(this.infPrefix, "<begin> " + this.beginEndSuffix);

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs end info.
   */
  public void end() {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix);

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs end info.
   * 
   * @param exitStatus the exit status
   */
  public void end(final int exitStatus) {
    final String log = createMsg(this.infPrefix, "< end > " + this.beginEndSuffix + " " + LogUtil.joinKeyVal("status", exitStatus));

    this.infHdr.getWriter().println(log);
    if (this.isDevelopMode) {
      this.console.println(log);
    }
  }

  /**
   * Outputs for development.
   *
   * @param msg the log message
   */
  public void develop(final String msg) {
    if (!this.isDevelopMode) {
      return;
    }
    final String log = createMsg(this.devPrefix, msg);

    this.infHdr.getWriter().println(log);
    this.console.println(log);
  }

  /**
   * Returns whether develop mode is enabled.
   *
   * @return <code>true</code> if develop log is enabled
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
    
    // Resets stopwatch
    this.watchStartTime = 0;
  }

  // Constants
  private static final long MILLIS_PER_SECOND = 1000L;
  private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
  private static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;

  /**
   * Formats elapsed time.
   *
   * @param millis the milliseconds
   * @return the formatted time
   */
  private String milliSecToHmsSss(final long millis) {
    final long hours = millis / MILLIS_PER_HOUR;
    final long minutes = (millis / MILLIS_PER_MINUTE) % 60;
    final long seconds = (millis / MILLIS_PER_SECOND) % 60;
    final long milliseconds = millis % MILLIS_PER_SECOND;
    
    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
  }
}
