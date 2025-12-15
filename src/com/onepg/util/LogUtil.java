package com.onepg.util;

import com.onepg.db.SqlBuilder;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.ValUtil.LineSep;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Log utility class.
 */
public final class LogUtil {

  /** <code>null</code> replacement character for log output. */
  private static final String NULL_REP = "<nul>";

  /** Default log property key prefix (the part before property keys <code>.inf.file</code> and <code>.err.file</code>). */
  private static final String DEFAULT_LOG_KEYPREFIX = "default";

  /** Log properties (file paths converted to absolute paths). */
  static final IoItems PROP_MAP;
  /** Develop mode. */
  private static final boolean DEVELOP_MODE;
  /** Console writer. */
  private static final PrintWriter CONSOLE_WRITER;

  static {
    PROP_MAP = PropertiesUtil.getFrameworkProps(FwPropertiesName.LOG);
    DEVELOP_MODE = Boolean.parseBoolean(PROP_MAP.getString("develop.mode"));
    CONSOLE_WRITER = new CustomPrintWriter(System.out, true, LineSep.LF);

    // Disables System.out
    final PrintStream disablePs = new PrintStream(System.err) {
      @Override
      public void println(final String value) {
        throw new UnsupportedOperationException("System.out is disabled.");
      }

      @Override
      public void print(final String value) {
        throw new UnsupportedOperationException("System.out is disabled.");
      }
    };
    System.setOut(disablePs);
  }

  /**
   * Constructor.
   */
  private LogUtil() {
    // No processing
  }

  /**
   * Creates a log writer instance.
   *
   * @param cls the target class for logging
   * @return the log writer instance
   */
  public static LogWriter newLogWriter(final Class<?> cls) {
    return newLogWriter(cls, null);
  }

  /**
   * Creates a log writer instance.
   *
   * @param cls the target class for logging
   * @param traceCode the trace code
   * @return the log writer instance
   */
  public static LogWriter newLogWriter(final Class<?> cls, final String traceCode) {
    final LogTxtHandler infHdr = LogTxtHandler.getInstance(DEFAULT_LOG_KEYPREFIX, false);
    final LogTxtHandler errHdr = LogTxtHandler.getInstance(DEFAULT_LOG_KEYPREFIX, true);
    try {
      return new LogWriter(cls, traceCode, DEVELOP_MODE, infHdr, errHdr, CONSOLE_WRITER);
    } catch (Exception | Error e) {
      throw new RuntimeException("An exception occurred while creating the log writer instance. ", e);
    }
  }

  /**
   * Outputs to standard output.
   *
   * @param msgs the messages
   */
  public static void stdout(final String... msgs) {
    stdout(null, msgs);
  }

  /**
   * Outputs to standard output.
   *
   * @param e the error object
   * @param msgs the messages
   */
  public static void stdout(final Throwable e, final String... msgs) {
    final String msg = ValUtil.join(System.lineSeparator(), msgs);
    CONSOLE_WRITER.println(msg);
    if (!ValUtil.isNull(e)) {
      e.printStackTrace(CONSOLE_WRITER);
    }
    CONSOLE_WRITER.flush();
  }

  /**
   * Outputs Java information to standard output.
   */
  public static void javaInfoStdout() {
    CONSOLE_WRITER.println("Java Information.");
    CONSOLE_WRITER.println(" version = " + System.getProperty("java.version"));
    CONSOLE_WRITER.println(" home    = " + System.getProperty("java.home"));
    CONSOLE_WRITER.println(" class   = " + System.getProperty("java.class.path"));
    CONSOLE_WRITER.println(" tmpdir  = " + System.getProperty("java.io.tmpdir"));
    CONSOLE_WRITER.println("");
    CONSOLE_WRITER.println("OS User Information.");
    CONSOLE_WRITER.println(" name = " + System.getProperty("user.name"));
    CONSOLE_WRITER.println(" home = " + System.getProperty("user.home"));
    CONSOLE_WRITER.println(" dir  = " + System.getProperty("user.dir"));
    CONSOLE_WRITER.println("");
  }

  /**
   * Returns whether develop mode is enabled.
   *
   * @return <code>true</code> if develop mode is enabled
   */
  public static boolean isDevelopMode() {
    return DEVELOP_MODE;
  } 

  /**
   * Returns the stack trace of the error object.
   * <ul>
   * <li>Stops retrieving stack trace if <code>BreakException</code> is encountered.</li>
   * </ul>
   *
   * @param lineSep the line separator
   * @param e the error object
   * @return the stack trace
   */
  public static String getStackTrace(final String lineSep, final Throwable e) {
    final StringBuilder sb = new StringBuilder();
    try {
      Throwable current = e;
      boolean isFirst = true;

      while (!ValUtil.isNull(current) && !(current instanceof BreakException)) {
        if (!isFirst) {
          sb.append(lineSep).append("Caused by: ");
        }
        sb.append(current.toString());
        
        for (final StackTraceElement element : current.getStackTrace()) {
          sb.append(lineSep);
          sb.append(" at ");
          sb.append(element.toString());
        }
        
        current = current.getCause();
        isFirst = false;
      }
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Concatenates key-value strings.
   * <ul>
   * <li>Creates a log string by concatenating in the format "key=value, key=value, key=value,...".</li>
   * <li>If the value is an array, list, or map, calls the respective concatenation method.</li>
   * </ul>
   *
   * @param keyVal the key-values (key, value, key, value, key, value,...)
   * @return the concatenated string in "key=value, key=value, key=value" format
   */
  public static String joinKeyVal(final Object... keyVal) {
    if (ValUtil.isNull(keyVal)) {
      return NULL_REP;
    }
    if (ValUtil.isEmpty(keyVal)) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (int i = 0; i < keyVal.length; i++) {
        if (i % 2 == 0) {
          if (ValUtil.isNull(keyVal[i])) {
            sb.append(NULL_REP);
          } else {
            // Assumes key is a string
            sb.append(keyVal[i].toString());
          }
          sb.append('=');
        } else {
          final String sval = convOutput(keyVal[i]);
          sb.append(sval);
          sb.append(',');
        }
      }
      ValUtil.deleteLastChar(sb);
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Concatenates values.
   * <ul>
   * <li>Creates a log string by concatenating in the format "value, value,...".</li>
   * </ul>
   *
   * @param values the values to concatenate
   * @return the string in "value, value, value, value" format
   */
  public static String joinValues(final String... values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Concatenates array values.
   * <ul>
   * <li>Creates a log string by concatenating in the format "[value, value,...]".</li>
   * </ul>
   *
   * @param values the values to concatenate
   * @return the string in "[value, value, value, value]" format
   */
  public static String join(final String[] values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '[');
      sb.append(']');
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Concatenates list values.
   * <ul>
   * <li>Creates a log string by concatenating values in the list in the format "[value, value,...]".</li>
   * </ul>
   *
   * @param values the list to concatenate
   * @return the string in "[value, value, value, value]" format
   */
  public static String join(final List<?> values) {
    if (ValUtil.isNull(values)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Object val : values) {
        final String sval = convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '[');
      sb.append(']');
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Concatenates map key-values.
   * <ul>
   * <li>Creates a log string by concatenating keys and values in the map in the format "{key=value, key=value, key=value,...}".</li>
   * </ul>
   *
   * @param <T> the type of values in the map
   * @param map the map to concatenate
   * @return the string in "{key=value, key=value, key=value}" format
   */
  public static <T> String join(final Map<String, T> map) {
    if (ValUtil.isNull(map)) {
      return NULL_REP;
    }
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Map.Entry<String, ?> ent : map.entrySet()) {
        final String key = ent.getKey();
        final Object val = ent.getValue();
        final String sval = convOutput(val);
        sb.append(key).append('=').append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '{');
      sb.append('}');
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Converts to a log string.
   * <ul>
   * <li>Returns a replacement character for log if the value is <code>null</code>.</li>
   * <li>Encloses in double quotes and escapes double quotes within the string.</li>
   * <li>This method is provided to avoid the method with Object argument.</li>
   * </ul>
   *
   * @param val the string
   * @return the log string
   */
  static String convOutput(final String val) {
    if (ValUtil.isNull(val)) {
      return NULL_REP;
    }
    return '"' + ((String) val).replace("\"", "\\\"") + '"';
  }

  /**
   * Converts to a log string.
   * <ul>
   * <li>Returns a replacement character for log if the value is <code>null</code>.</li>
   * <li>If the value is a string, encloses in double quotes and escapes double quotes within the string.</li>
   * <li>If the value is a date or datetime, converts to string using the standard format.</li>
   * <li>If the value is an array, list, or map, calls the respective concatenation method.</li>
   * <li>Returns the result of <code>toString()</code> method for other objects.</li>
   * </ul>
   *
   * @param obj the object
   * @return the log string
   */
  static String convOutput(final Object obj) {
    try {
      if (ValUtil.isNull(obj)) {
        return NULL_REP;
      } else if (obj instanceof String) {
        return '"' + ((String) obj).replace("\"", "\\\"") + '"';
      } else if (obj instanceof Io) {
        // Io is a subclass of Map, so checks before Map
        return ((Io) obj).toString();
      } else if (obj instanceof IoItems) {
        // IoItems is a subclass of Map, so checks before Map
        return ((IoItems) obj).toString();
      } else if (obj instanceof SqlBuilder) {
        return ((SqlBuilder) obj).toString();
      } else if (obj instanceof String[]) {
        return join((String[]) obj);
      } else if (obj instanceof List) {
        return join((List<?>) obj);
      } else if (obj instanceof Map) {
        try {
          @SuppressWarnings("unchecked")
          final Map<String, ?> smap = (Map<String, ?>) obj;
          return join(smap);
        } catch (Exception e) {
          return "<MAP_CAST_ERROR>";
        }
      } else if (obj instanceof BigDecimal) {
        return ((BigDecimal) obj).toPlainString();
      } else if (obj instanceof LocalDate) {
        return ((LocalDate) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      } else if (obj instanceof LocalDateTime) {
        return ((LocalDateTime) obj).format(AbstractIoTypeMap.DTF_IO_TIMESTAMP);
      } else if (obj instanceof java.sql.Timestamp) {
        // java.sql.Timestamp is a subclass of java.util.Date, so checks before java.util.Date
        return ((java.sql.Timestamp) obj).toLocalDateTime().format(AbstractIoTypeMap.DTF_IO_TIMESTAMP);
      } else if (obj instanceof java.sql.Date) {
        // java.sql.Date is a subclass of java.util.Date, so checks before java.util.Date
        return ValUtil.dateToLocalDate((java.sql.Date) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      } else if (obj instanceof java.util.Date) {
        return ValUtil.dateToLocalDate((java.util.Date) obj).format(AbstractIoTypeMap.DTF_IO_DATE);
      }
      return String.valueOf(obj);
    } catch (Exception e) {
      return "<CONVERT_ERROR>";
    }
  }

  /**
   * Replaces <code>null</code>.
   * <p>
   * Returns a replacement character for log if the value is <code>null</code>.
   * </p>
   *
   * @param value the value; replacement character if <code>null</code>
   * @return the value as is if not <code>null</code>; replacement character if <code>null</code>
   */
  public static String replaceNullValue(final String value) {
    return ValUtil.nvl(value, NULL_REP);
  }

  /**
   * Formats date and time.
   * <ul>
   * <li>Converts milliseconds to a human-readable format (days, hours, minutes, seconds, milliseconds).</li>
   * <li>Example: "11T03:15:30.123", "0T00:00:00.000"</li>
   * </ul>
   *
   * @param msec the milliseconds
   * @return the formatted uptime
   */
  public static String formatDaysTime(final long msec) {
    try {
      if (msec <= 0) {
        return "0T00:00:00.000";
      }
      // Overflow protection
      if (Long.MAX_VALUE < (msec / 1000)) {
        return ValUtil.BLANK;
      }

      final long sec = msec / 1000;
      final long min = sec / 60;
      final long hur = min / 60;
      long day = hur / 24;
      
      // Check for unrealistic values
      if (day > 999999) { // More than approximately 2700 years
        day = -1;
      }

      final long sepHur = hur % 24;     // Range 0-23
      final long sepMin = min % 60;     // Range 0-59
      final long sepSec = sec % 60;     // Range 0-59
      final long sepMsec = msec % 1000; // Range 0-999 (milliseconds)
      
      final StringBuilder sb = new StringBuilder();
      sb.append(day).append("T");
      sb.append(String.format("%02d", sepHur)).append(":");
      sb.append(String.format("%02d", sepMin)).append(":");
      sb.append(String.format("%02d", sepSec)).append(".");
      sb.append(String.format("%03d", sepMsec));

      return sb.toString();
    } catch (Exception ignore) {
      // No processing
      return ValUtil.BLANK;
    }
  }

}
