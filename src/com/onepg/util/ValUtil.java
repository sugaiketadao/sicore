package com.onepg.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Value utility class.
 */
public final class ValUtil {
  /**
   * Constructor.
   */
  private ValUtil() {
    // No processing
  }

  /** Blank value (zero-byte string). */
  public static final String BLANK = "";

  /** Category: ON value. */
  public static final String ON = "1";
  /** Category: OFF value. */
  public static final String OFF = "0";

  /** JSON <code>null</code> character. */
  public static final String JSON_NULL = "null";

  /** Date time formatter: Date. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Input/output map key check pattern.<br>
   * <ul>
   * <li>Allowed characters are as follows.
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * </ul>
   */
  private static final Pattern PATTERN_IO_KEY_CHECK = Pattern.compile("^[a-z0-9_.-]+$");

  /** Character set specification. */
  public enum CharSet {
    /** Character set specification - UTF-8. */
    UTF8(StandardCharsets.UTF_8.name()),
    /** Character set specification - Shift_JIS. */
    SJIS("Shift_JIS"),
    /** Character set specification - MS932. */
    MS932("MS932");

    /** Character set. */
    private final String setName;

    /**
     * Constructor.
     *
     * @param value Character set
     */
    private CharSet(final String value) {
      this.setName = value;
    }

    @Override
    public String toString() {
      return this.setName;
    }
  }

  /** Line separator. */
  public enum LineSep {
    /** Line separator LF. */
    LF("\n"),
    /** Line separator CRLF. */
    CRLF("\r\n");

    /** Line separator. */
    private final String sep;

    /**
     * Constructor.
     *
     * @param value Line separator
     */
    private LineSep(final String value) {
      this.sep = value;
    }

    @Override
    public String toString() {
      return this.sep;
    }
  }

  /**
   * Checks <code>null</code>.<br>
   * <ul>
   * <li>Checks whether <code>Object</code> is <code>null</code>.</li>
   * </ul>
   *
   * @param obj Check target
   * @return <code>true</code> if <code>null</code>
   */
  public static boolean isNull(final Object obj) {
    return null == obj;
  }

  /**
   * Checks blank.<br>
   * <ul>
   * <li>Determines as blank in any of the following cases: consisting only of half-width spaces / zero-byte string / <code>null</code>.</li>
   * </ul>
   *
   * @param value Check target
   * @return <code>true</code> if blank
   */
  public static boolean isBlank(final String value) {
    if (isNull(value)) {
      return true;
    }
    return (value.trim().length() == 0);
  }

  /**
   * Checks empty.<br>
   * <ul>
   * <li>Checks whether array has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param values Check target
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final Object[] values) {
    return (values == null || values.length == 0);
  }

  /**
   * Checks empty.<br>
   * <ul>
   * <li>Checks whether list has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param list Check target
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final List<?> list) {
    if (isNull(list)) {
      return true;
    }
    return list.isEmpty();
  }

  /**
   * Checks empty.<br>
   * <ul>
   * <li>Checks whether map has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param map Check target
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final Map<?, ?> map) {
    if (isNull(map)) {
      return true;
    }
    return map.isEmpty();
  }

  /**
   * Checks input/output map key format.<br>
   * <ul>
   * <li>Checks whether characters can be used as keys in input/output maps.</li>
   * <li>Allowed characters are as follows.
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * </ul>
   *
   * @param key Check target key
   * @return <code>false</code> if contains invalid characters
   */
  public static boolean isValidIoKey(final String key) {
    if (isBlank(key)) {
      return false;
    }
    return PATTERN_IO_KEY_CHECK.matcher(key).matches();
  }

  /**
   * Checks input/output map key format.<br>
   * <ul>
   * <li>Checks whether characters can be used as keys in input/output maps.</li>
   * <li>Allowed characters are as follows.
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * <li>Throws <code>RuntimeException</code> if contains invalid characters.</li>
   * </ul>
   *
   * @param key Check target key
   */
  public static void validateIoKey(final String key) {
    if (!ValUtil.isValidIoKey(key)) {
      throw new RuntimeException("Only lowercase letters, digits, underscores, hyphens, and dots are allowed. "
                                + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * Replaces <code>null</code> with blank.<br>
   * <ul>
   * <li>Returns blank if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value Check target
   * @return Blank if <code>null</code>
   */
  public static String nvl(final String value) {
    return nvl(value, BLANK);
  }

  /**
   * Replaces <code>null</code>.<br>
   * <ul>
   * <li>Returns replacement character if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value Check target
   * @param nullDefault Replacement character
   * @return Replacement character if <code>null</code>
   */
  public static String nvl(final String value, final String nullDefault) {
    if (isNull(value)) {
      return nullDefault;
    }
    return value;
  }

  /**
   * Replaces <code>null</code> with zero.<br>
   * <ul>
   * <li>Returns zero if numeric value is <code>null</code>.</li>
   * </ul>
   *
   * @param value Check target
   * @return Zero if <code>null</code>
   */
  public static BigDecimal nvl(final BigDecimal value) {
    if (isNull(value)) {
      return BigDecimal.ZERO;
    }
    return value;
  }

  /**
   * Replaces blank.<br>
   * <ul>
   * <li>Returns replacement character if string is blank.</li>
   * <li>Uses <code>#isBlank(String)</code> for blank determination.</li>
   * </ul>
   *
   * @param value Check target
   * @param blankDefault Replacement character
   * @return Replacement character if blank
   */
  public static String bvl(final String value, final String blankDefault) {
    if (isBlank(value)) {
      return blankDefault;
    }
    return value;
  }

  /**
   * Joins array.<br>
   * <ul>
   * <li>Returns blank if array is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param joint Joint character
   * @param values Join target
   * @return Joined string
   */
  public static String join(final String joint, final String... values) {
    if (isEmpty(values)) {
      return BLANK;
    }
    final String j = nvl(joint);
    final StringBuilder sb = new StringBuilder();
    for (final String val : values) {
      sb.append(nvl(val)).append(j);
    }
    ValUtil.deleteLastChar(sb, j.length());
    return sb.toString();
  }

  /**
   * Joins list.<br>
   * <ul>
   * <li>Returns blank if list is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param joint Joint character
   * @param list Join target
   * @return Joined string
   */
  public static String join(final String joint, final List<String> list) {
    if (list.isEmpty()) {
      return BLANK;
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    final String[] values = list.toArray(new String[] {});
    return join(joint, values);
  }

  /**
   * Joins list.<br>
   * <ul>
   * <li>Returns blank if list is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param joint Joint character
   * @param list Join target
   * @return Joined string
   */
  public static String join(final String joint, final Set<String> list) {
    if (list.isEmpty()) {
      return BLANK;
    }
    if (list.size() == 1) {
      return list.toArray(new String[] {})[0];
    }
    final String[] values = list.toArray(new String[] {});
    return join(joint, values);
  }

  /**
   * Joins array to CSV.<br>
   * <ul>
   * <li>Returns blank if array is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param values Join target
   * @return Joined CSV string
   */
  public static String joinCsv(final String[] values) {
    if (isEmpty(values)) {
      return BLANK;
    }
    final StringBuilder sb = new StringBuilder();
    for (final String value : values) {
      final String val = ValUtil.nvl(value);
      sb.append(val);
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Joins array to CSV with double quotation.<br>
   * <ul>
   * <li>Returns blank if array is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * <li>Outputs with double quotation added to all array elements.</li>
   * <li>If value contains double quotation, it is converted to two consecutive double quotations.</li>
   * </ul>
   *
   * @param values Join target
   * @return Joined CSV string
   */
  public static String joinCsvAllDq(final String[] values) {
    if (isEmpty(values)) {
      return BLANK;
    }
    final StringBuilder sb = new StringBuilder();
    for (final String value : values) {
      final String val = ValUtil.nvl(value);
      sb.append('"').append(val.replace("\"", "\"\"")).append('"');
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Joins array to CSV with CSV specification compliant double quotation.<br>
   * <ul>
   * <li>Returns blank if array is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * <li>Outputs with double quotation added to necessary elements according to CSV specification.</li>
   * <li>If value contains double quotation, it is converted to two consecutive double quotations.</li>
   * </ul>
   *
   * @param values Join target
   * @return Joined CSV string
   */
  public static String joinCsvDq(final String[] values) {
    if (isEmpty(values)) {
      return BLANK;
    }
    final StringBuilder sb = new StringBuilder();
    for (final String value : values) {
      final String val = ValUtil.nvl(value);
      // Quotes only if comma, line break, or double quote is contained
      if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
        sb.append('"').append(val.replace("\"", "\"\"")).append('"');
      } else {
        sb.append(val);
      }
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Splits array.<br>
   * <ul>
   * <li>Returns zero-length array if character is <code>null</code>.</li>
   * </ul>
   *
   * @param value Split target character
   * @param sep Split character
   * @return Split string array
   */
  public static String[] split(final String value, final String sep) {
    if (isNull(value)) {
      return new String[] {};
    }
    final List<String> list = new ArrayList<>();
    for (final String val : new SimpleSeparateParser(value, sep)) {
      list.add(val);
    }
    return list.toArray(new String[0]);
  }

  /**
   * Splits array by regular expression.<br>
   * <ul>
   * <li>Returns zero-length array if character is <code>null</code>.</li>
   * </ul>
   *
   * @param value Split target character
   * @param sep Split character (regular expression)
   * @return Split string array
   */
  public static String[] splitReg(final String value, final String sep) {
    return splitReg(value, sep, -1);
  }

  /**
   * Splits array by regular expression.<br>
   * <ul>
   * <li>Returns zero-length array if character is <code>null</code>.</li>
   * </ul>
   *
   * @param value Split target character
   * @param sep Split character (regular expression)
   * @param limitLength Maximum length
   * @return Split string array
   */
  public static String[] splitReg(final String value, final String sep, final int limitLength) {
    if (isNull(value)) {
      return new String[] {};
    }
    return value.split(sep, limitLength);
  }

  /**
   * Splits CSV.<br>
   * <ul>
   * <li>Splits CSV string into string array.</li>
   * <li>Returns zero-length array if CSV string is <code>null</code>.</li>
   * </ul>
   *
   * @param csv CSV string
   * @return Split string array
   */
  public static String[] splitCsv(final String csv) {
    if (isNull(csv)) {
      return new String[] {};
    }
    final List<String> list = new ArrayList<>();

    for (final String value : new SimpleSeparateParser(csv, ",")) {
      list.add(value);
    }
    return list.toArray(new String[0]);
  }

  /**
   * Splits CSV with double quotation.<br>
   * <ul>
   * <li>Splits CSV string into string array.</li>
   * <li>Returns zero-length array if CSV string is <code>null</code>.</li>
   * <li>Two consecutive double quotations within value are converted to one double quotation and stored.</li>
   * </ul>
   * 
   * @param csv CSV string
   * @return Split string array
   */
  public static String[] splitCsvDq(final String csv) {
    if (isNull(csv)) {
      return new String[] {};
    }
    final List<String> list = new ArrayList<>();

    for (final String value : new CsvDqParser(csv)) {
      list.add(value.replace("\"\"", "\""));
    }
    return list.toArray(new String[0]);
  }

  /**
   * Compares strings.<br>
   * <ul>
   * <li>Compares <code>null</code> as zero-byte blank character.</li>
   * </ul>
   *
   * @param str1 Comparison target 1
   * @param str2 Comparison target 2
   * @return <code>true</code> if equal
   */
  public static boolean equals(final String str1, final String str2) {
    return nvl(str1).equals(nvl(str2));
  }

  /**
   * Compares numeric values.<br>
   * <ul>
   * <li>Compares <code>null</code> as zero.</li>
   * </ul>
   *
   * @param dec1 Comparison target 1
   * @param dec2 Comparison target 2
   * @return <code>true</code> if equal
   */
  public static boolean equals(final BigDecimal dec1, final BigDecimal dec2) {
    // Uses compareTo because equals with different precision does not return <code>true</code>
    // Example: new BigDecimal("1").equals(new BigDecimal("1.0")) does not return <code>true</code>
    return (nvl(dec1).compareTo(nvl(dec2)) == 0);
  }

  /**
   * Safe substring (start index only specified).<br>
   * <ul>
   * <li>Cuts from start index to end of string.</li>
   * </ul>
   *
   * @param value      Target string
   * @param beginIndex Start index
   * @return Substring
   */
  public static String substring(final String value, final Integer beginIndex) {
    return substring(value, beginIndex, null);
  }

  /**
   * Safe substring.<br>
   * <ul>
   * <li>Safely cuts specified range of string.</li>
   * <li>Properly handles out-of-range specifications and invalid values.</li>
   * </ul>
   *
   * @param value      Target string
   * @param beginIndex Start index (optional) 0 if <code>null</code> is passed (omitted)
   * @param endIndex   End index (optional) string length if <code>null</code> is passed (omitted)
   * @return Substring
   */
  public static String substring(final String value, final Integer beginIndex, final Integer endIndex) {
    if (isNull(value)) {
      return BLANK;
    }
    
    // Supplements omitted values
    final int begin;
    if (isNull(beginIndex)) {
      begin = 0;
    } else {
      begin = beginIndex;
    }
    final int end;
    if (isNull(endIndex)) {
      end = value.length();
    } else {
    // Out-of-range correction
      end = Math.min(endIndex, value.length());
    }
    
    // Returns empty string if start position is after end position, or start position is after string length
    if (begin < 0 || begin >= end || begin >= value.length()) {
      return BLANK;
    }

    return value.substring(begin, end);
  }

  /**
   * Checks alphanumeric.<br>
   * <ul>
   * <li>Checks whether string is valid as alphanumeric.</li>
   * </ul>
   *
   * @param value Check target
   * @return <code>true</code> if valid
   */
  public static boolean isAlphabetNumber(final String value) {
    if (isBlank(value)) {
      // If blank
      return false;
    }
    return Pattern.matches("^[a-zA-Z0-9]+$", value);
  }

  /**
   * Checks numeric.<br>
   * <ul>
   * <li>Checks whether string is valid as numeric.</li>
   * </ul>
   *
   * @param value Check target
   * @return <code>true</code> if valid
   */
  public static boolean isNumber(final String value) {
    return isNumber(value, false, false);
  }

  /** Numeric check pattern. */
  private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^([1-9]\\d*|0)$");
  /** Numeric check pattern - decimal allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC =
      Pattern.compile("^([1-9]\\d*|0)(\\.\\d+)?$");
  /** Numeric check pattern - minus value allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_MINUS = Pattern.compile("^[-]?([1-9]\\d*|0)$");
  /** Numeric check pattern - decimal allowed, minus value allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC_MINUS =
      Pattern.compile("^[-]?([1-9]\\d*|0)(\\.\\d+)?$");

  /**
   * Checks numeric.<br>
   * <ul>
   * <li>Checks whether string is valid as numeric.</li>
   * </ul>
   *
   * @param value Check target
   * @param minusNg <code>true</code> if minus value is invalid
   * @param decNg <code>true</code> if decimal is invalid
   * @return <code>true</code> if valid
   */
  public static boolean isNumber(final String value, final boolean minusNg, final boolean decNg) {
    if (isBlank(value)) {
      // If blank
      return false;
    }
    final String checkVal = trimLeftZeroByIsNumber(value);
    if (minusNg && decNg) {
      return VALID_NUMBER_PATTERN.matcher(checkVal).find();
    } else if (minusNg) {
      return VALID_NUMBER_PATTERN_DEC.matcher(checkVal).find();
    } else if (decNg) {
      return VALID_NUMBER_PATTERN_MINUS.matcher(checkVal).find();
    }
    return VALID_NUMBER_PATTERN_DEC_MINUS.matcher(checkVal).find();
  }

  /**
   * Checks digit count.<br>
   * <ul>
   * <li>Assumes <code>#isBlank(String)</code> is <code>true</code>, and returns <code>false</code> if check target is blank.</li>
   * </ul>
   * 
   * @param value      Check target
   * @param len Valid digit count
   * @return <code>true</code> if valid
   */
  public static boolean checkLength(final String value, final int len) {
    if (isBlank(value)) {
      // If blank
      return false;
    }
    return value.length() <= len;
  }

  /**
   * Checks numeric digit count (precision).<br>
   * <ul>
   * <li>Confirms that digit count of integer part and after decimal point are within specified range.</li>
   * <li>Arguments are specified with digit count of integer part plus decimal part and digit count of decimal part only, same as DB item definition.</li>
   * <li>Assumes <code>#isNumber(String)</code> is <code>true</code>, and returns
   * <code>false</code> if check target is blank.</li>
   * </ul>
   * 
   * @param value      Check target
   * @param intPartLen Valid digit count of integer part plus decimal part
   * @param decPartLen Valid digit count of decimal part
   * @return <code>true</code> if valid
   */
  public static boolean checkLengthNumber(final String value, final int intPartLen,
      final int decPartLen) {
    if (isBlank(value)) {
      // If blank
      return false;
    }
    final String checkVal = trimLeftZeroByIsNumber(value);
    final String patternStr;
    if (decPartLen > 0) {
      patternStr = "^[-]?\\d{1," + intPartLen + "}(\\.\\d{1," + decPartLen + "})?$";
    } else {
      patternStr = "^[-]?\\d{1," + intPartLen + "}$";
    }
    final Pattern pattern = Pattern.compile(patternStr);
    return pattern.matcher(checkVal).find();
  }

  /**
   * Removes left zeros for numeric check.<br>
   * <ul>
   * <li>Supports numeric values including decimal point.</li>
   * <li>"-0" or "-000" is normalized to "0".</li>
   * </ul>
   *
   * @param value String
   * @return Processed character
   */
  private static String trimLeftZeroByIsNumber(final String value) {
    final boolean hasMinus = value.startsWith("-");
    final String tmp;
    if (hasMinus) {
      tmp = value.substring(1);
    } else {
      tmp = value;
    }
      
    // Checks decimal point position
    final int dotIndex = tmp.indexOf('.');
    final String ret;
    
    if (dotIndex == -1) {
      // No decimal point: normal left zero removal
      ret = tmp.replaceAll("^0+", "");
    } else if (dotIndex == 0) {
      // Format like ".5": returns as is (no integer part)
      ret = tmp;
    } else {
      // With decimal point: left zero removal only for integer part
      final String intPart = tmp.substring(0, dotIndex).replaceAll("^0+", "");
      final String decPart = tmp.substring(dotIndex); // Including "."
      ret = intPart + decPart;
    }
    
    // If all zeros
    if (isBlank(ret) || ret.equals(".")) {
      return "0";
    }
    
    // Integer part is empty → Supplements "0"
    if (ret.startsWith(".")) {
      if (hasMinus) {
        return "-0" + ret;
      }
      return "0" + ret;
    }

    if (hasMinus) {
      return "-" + ret;
    }
    return ret;
  }

  /**
   * Checks actual date.<br>
   * <ul>
   * <li>Checks whether string is valid as date.</li>
   * </ul>
   *
   * @param value Check target (YYYYMMDD)
   * @return <code>true</code> if valid
   */
  public static boolean isDate(final String value) {
    if (isBlank(value)) {
      // If blank
      return false;
    }

    if (value.length() != 8 || !isNumber(value)) {
      // If not 8 digits or not numeric
      return false;
    }
    try {
      LocalDate.parse(value, DTF_DATE);
    } catch (Exception e) {
      // If parse error occurs
      return false;
    }
    return true;
  }

  /** String set considered as boolean "true" */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "on");

  /**
   * Checks boolean value.<br>
   * <ul>
   * <li>Checks whether string is considered as boolean "true" value.</li>
   * <li>Performs the following evaluation.
   * <ol>
   * <li>"1", "true", "yes", "on" (all half-width) are <code>true</code>.</li>
   * <li><code>null</code> or blank is <code>false</code>, and other than above are <code>false</code>.</li>
   * <li>Case insensitive.</li>
   * <li>Ignores left and right half-width blanks.</li>
   * </ol>
   * </li>
   * </ul>
   *
   * @param val Check target
   * @return <code>true</code> if considered as boolean "true"
   */
  public static boolean isTrue(final String val) {
    if (isBlank(val)) {
      return false;
    }
    final String lowVal = val.trim().toLowerCase();
    if (TRUE_VALUES.contains(lowVal)) {
      return true;
    }
    return false;
  }

  /**
   * Converts Date type to LocalDate type.
   *
   * @param date Date type (including java.sql.Date)
   * @return LocalDate type
   */
  public static LocalDate dateToLocalDate(final java.util.Date date) {
    final java.sql.Date sd;
    if (date instanceof java.sql.Date) {
      sd = (java.sql.Date) date;
    } else {
      sd = new java.sql.Date(date.getTime());
    }
    final LocalDate ld = sd.toLocalDate();
    return ld;
  }

  /**
   * Removes double quotation.<br>
   * <ul>
   * <li>Removes double quotation at start and end of string.</li>
   * </ul>
   *
   * @see #trimBothEnds(String, char, char)
   * @param value Target string
   * @return Removed string
   */
  public static String trimDq(final String value) {
    return trimBothEnds(value, '"', '"');
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims only if both end characters are found after trim.</li>
   * </ul>
   *
   * @param value Target string
   * @param prefix Character at front end
   * @param suffix Character at back end
   * @return String with both ends trimmed
   */
  public static String trimBothEnds(final String value, final char prefix, final char suffix) {
    return trimBothEnds(value, String.valueOf(prefix), String.valueOf(suffix));
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims only if both end characters are found after trim.</li>
   * </ul>
   *
   * @param value Target string
   * @param prefix Character at front end
   * @param suffix Character at back end
   * @return String with both ends trimmed
   */
  public static String trimBothEnds(final String value, final String prefix, final String suffix) {
    if (ValUtil.isNull(value) || value.length() < 2) {
      // Should have at least 2 characters
      return value;
    }
    if (value.startsWith(prefix) && value.endsWith(suffix)) {
      return value.substring(prefix.length(), value.length() - suffix.length());
    }
    return value;
  }

  /** Regular expression - start full-width string. */
  private static final String REGEX_ZENKAKU_SPACE_START = "^[" + '\u3000' + "]+";
  /** Regular expression - end full-width string. */
  private static final String REGEX_ZENKAKU_SPACE_END = "[" + '\u3000' + "]+$";

  /**
   * Trims full-width spaces at both ends of string.<br>
   * <ul>
   * <li>Removes full-width spaces at start and end of string.</li>
   * </ul>
   *
   * @param value Target string
   * @return Removed string
   */
  public static String trimZenkakuSpace(final String value) {
    if (isNull(value)) {
      return value;
    }
    return value.replaceFirst(REGEX_ZENKAKU_SPACE_START, BLANK)
        .replaceFirst(REGEX_ZENKAKU_SPACE_END, BLANK);
  }

  /**
   * Removes left zeros.<br>
   * <ul>
   * <li>Removes <code>"0"</code> on the left of string.</li>
   * </ul>
   *
   * @param value String
   * @return Processed character
   */
  public static String trimLeftZero(final String value) {
    if (isNull(value)) {
      return value;
    }
    final String ret = value.replaceAll("^0+", "");
    return ret;
  }

  /**
   * Pads left zeros.<br>
   * <ul>
   * <li>Pads <code>"0"</code> on the left of string.</li>
   * </ul>
   *
   * @param value String
   * @param digit Character digit count after padding
   * @return Processed character
   */
  public static String paddingLeftZero(final String value, final int digit) {
    if (ValUtil.isNull(value)) {
      return String.format("%0" + digit + "d", 0);
    }
    if (value.length() >= digit) {
      // Returns as is if length exceeded
      return value;
    }
    final StringBuilder sb = new StringBuilder(value);
    while (sb.length() < digit) {
      sb.insert(0, "0");
    }
    return sb.toString();
  }

  /**
   * Pads left zeros.<br>
   * <ul>
   * <li>Converts numeric value to string and pads <code>"0"</code> on the left.</li>
   * </ul>
   *
   * @param value Numeric value
   * @param digit Character digit count after padding
   * @return Processed character
   */
  public static String paddingLeftZero(final int value, final int digit) {
    return String.format("%0" + String.valueOf(digit) + "d", value);
  }


  /**
   * Deletes last one character.
   *
   * @param sb StringBuilder
   */
  public static void deleteLastChar(final StringBuilder sb) {
    if (sb.length() >= 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * Deletes last characters.
   *
   * @param sb StringBuilder
   * @param length Last character length
   */
  public static void deleteLastChar(final StringBuilder sb, final int length) {
    if (length <= 0) {
      return;
    }
    if (sb.length() >= length) {
      sb.setLength(sb.length() - length);
    }
  }

  /**
   * Converts URL encoding.<br>
   * <ul>
   * <li>Converts 1-byte blank from "+" to "%20" to match JavaScript's encodeURIComponent.</li>
   * <li>Converts wildcard "*" to "%2A".</li>
   * </ul>
   *
   * @param url Conversion string
   * @return Converted string
   */
  public static String urlEncode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLEncoder.encode(value, CharSet.UTF8.toString()).replace("+", "%20").replace("*",
          "%2A");
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * Converts URL decoding.
   *
   * @param url Conversion string
   * @return Converted string
   */
  public static String urlDecode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLDecoder.decode(value, CharSet.UTF8.toString());
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * Converts JSON escape.<br>
   * <ul>
   * <li>Escapes characters that need to be escaped in JSON.</li>
   * <li>Also escapes <code>null</code> as control character.</li>
   * </ul>
   *
   * @param value String
   * @return Converted string
   */
  public static String jsonEscape(final String value) {
    if (isNull(value)) {
      return "\\u0000";
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '/':
          sb.append("\\/");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < ' ') {
            String t = "000" + Integer.toHexString(c);
            sb.append("\\u" + t.substring(t.length() - 4));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  /**
   * Removes JSON escape conversion.<br>
   * <ul>
   * <li>Removes escape for characters that need to be escaped in JSON.</li>
   * </ul>
   *
   * @param value String
   * @return Removed string
   */
  public static String jsonUnEscape(final String value) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      final char ch = value.charAt(i);
      if (ch == '\\' && i + 1 < value.length()) {
        char nextChar = value.charAt(i + 1);
        switch (nextChar) {
          case '"':
            sb.append('"');
            i++;
            break;
          case '\\':
            sb.append('\\');
            i++;
            break;
          case 'b':
            sb.append('\b');
            i++;
            break;
          case 'f':
            sb.append('\f');
            i++;
            break;
          case 'n':
            sb.append('\n');
            i++;
            break;
          case 'r':
            sb.append('\r');
            i++;
            break;
          case 't':
            sb.append('\t');
            i++;
            break;
          case 'u':
            if (i + 5 < value.length()) {
              try {
                final String ifHex = value.substring(i + 2, i + 6);
                final int hex = Integer.parseInt(ifHex, 16);
                sb.append((char) hex);
                i += 5;
              } catch (NumberFormatException e) {
                sb.append(ch);
              }
            } else {
              sb.append(ch);
            }
            break;
          default:
            sb.append(ch);
            break;
        }
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /** Sequence code formatter: timestamp (SQL:YYYYMMDDHH24MISSFF9). */
  private static final DateTimeFormatter SEQCODE_DTF = DateTimeFormatter
      .ofPattern("uuuuMMddHHmmssSSSSSSSSS").withResolverStyle(ResolverStyle.STRICT);
  /** Sequence code timestamp split digit specification. */
  private static final int[][] SEQCODE_TM_SPLIT_KETA = {{2, 5}, // Year:Month1 Max "991"
      {5, 8}, // Month2Day Max "930"
      {8, 11}, // Hour:Minute1 Max "235"
      {11, 14}, // Minute2:Second Max "959"
      {14, 17}, // Millisecond Max "999"
      {17, 20}, // Microsecond Max "999"
      {20, 23} // Nanosecond Max "999"
  };

  /**
   * Gets a sequence code value.<br>
   * <ul>
   * <li>Returns a base-36 encoded timestamp value including nanoseconds.</li>
   * <li>Sleeps for 1 nanosecond before returning to ensure the value is always unique within the system.</li>
   * <li>When using across multiple systems, additional measures such as appending a hostname or system name are needed to ensure unique values.</li>
   * </ul>
   *
   * @return Sequence code value
   */
  public static synchronized String getSequenceCode() {
    // 1ns sleep
    try {
      TimeUnit.NANOSECONDS.sleep(1);
    } catch (InterruptedException ignore) {
      // No processing
    }
    // Splits timestamp
    final String tm = LocalDateTime.now().format(SEQCODE_DTF);

    final StringBuilder sb = new StringBuilder();
    for (final int[] keta : SEQCODE_TM_SPLIT_KETA) {
      final String tmPart = tm.substring(keta[0], keta[1]);
      final int tmPartInt = Integer.parseInt(tmPart);
      final String tmPart36 = Integer.toString(tmPartInt, Character.MAX_RADIX);
      sb.append(String.format("%2s", tmPart36).replace(' ', '0').toUpperCase());
    }
    return sb.toString();
  }



}
