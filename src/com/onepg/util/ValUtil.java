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

  /** Code: ON value. */
  public static final String ON = "1";
  /** Code: OFF value. */
  public static final String OFF = "0";

  /** Line break code - LF. */
  public static final String LF = "\n";
  /** Line break code - CR. */
  public static final String CR = "\r";
  /** Line break code - CRLF. */
  public static final String CRLF = "\r\n";

  /** Character set specification - UTF-8. */
  public static final String UTF8 = StandardCharsets.UTF_8.name();
  /** Character set specification - Shift_JIS. */
  public static final String SJIS = "Shift_JIS";
  /** Character set specification - MS932. */
  public static final String MS932 = "MS932";

  /** JSON <code>null</code> character. */
  public static final String JSON_NULL = "null";

  /** Date/time formatter: date. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Input/output map key check pattern.<br>
   * <ul>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * </ul>
   */
  private static final Pattern PATTERN_IO_KEY_CHECK = Pattern.compile("^[a-z0-9_.-]+$");

  /** Character set specification. */
  public enum CharSet {
    /** Character set specification - UTF-8. */
    UTF8(ValUtil.UTF8),
    /** Character set specification - Shift_JIS. */
    SJIS(ValUtil.SJIS),
    /** Character set specification - MS932. */
    MS932(ValUtil.MS932);
    /** Character set. */
    private final String setName;

    /**
     * Constructor.
     *
     * @param value character set
     */
    private CharSet(final String value) {
      this.setName = value;
    }

    @Override
    public String toString() {
      return this.setName;
    }
  }

  /** Line break code. */
  public enum LineSep {
    /** Line break code - LF. */
    LF(ValUtil.LF),
    /** Line break code - CR. */
    CR(ValUtil.CR),
    /** Line break code - CRLF. */
    CRLF(ValUtil.CRLF);
    /** Line break code. */
    private final String sep;

    /**
     * Constructor.
     *
     * @param value line break code
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
   * CSV type enumeration.<br>
   * 
   * <ul>
   * <li>Defines how to add double quotes to each CSV item and how to handle line break codes.</li>
   * <li>When CSV type allows line breaks and values contain line break codes, unifies line break codes (CRLF/CR) to LF.</li>
   * <li>When CSV type does not allow line breaks (other than line break types) and values contain line break codes, converts line break codes (CRLF/CR/LF) to half-width spaces.</li>
   * </ul>
   */
  public enum CsvType {
    /** No double quotes */
    NO_DQ,
    /** All items with double quotes */
    DQ_ALL,
    /** With double quotes compliant with CSV specification */
    DQ_STD,
    /** All items with double quotes, line breaks allowed */
    DQ_ALL_LF,
    /** With double quotes compliant with CSV specification, line breaks allowed */
    DQ_STD_LF
  }

  /**
   * Checks if <code>null</code>.<br>
   * <ul>
   * <li>Checks if <code>Object</code> is <code>null</code>.</li>
   * </ul>
   *
   * @param obj target to check
   * @return <code>true</code> if <code>null</code>
   */
  public static boolean isNull(final Object obj) {
    return null == obj;
  }

  /**
   * Checks if blank.<br>
   * <ul>
   * <li>Treats as blank if composed only of half-width spaces, zero-byte string, or <code>null</code>.</li>
   * </ul>
   *
   * @param value target to check
   * @return <code>true</code> if blank
   */
  public static boolean isBlank(final String value) {
    if (isNull(value)) {
      return true;
    }
    return (value.trim().length() == 0);
  }

  /**
   * Checks if empty.<br>
   * <ul>
   * <li>Checks if array has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param values target to check
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final Object[] values) {
    return (values == null || values.length == 0);
  }

  /**
   * Checks if empty.<br>
   * <ul>
   * <li>Checks if list has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param list target to check
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final List<?> list) {
    if (isNull(list)) {
      return true;
    }
    return list.isEmpty();
  }

  /**
   * Checks if empty.<br>
   * <ul>
   * <li>Checks if map has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param map target to check
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
   * <li>Checks if characters can be used as keys for input/output map.</li>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * </ul>
   *
   * @param key target key to check
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
   * <li>Checks if characters can be used as keys for input/output map.</li>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscore</li><li>Hyphen</li><li>Dot</li></ul>
   * </li>
   * <li>Throws <code>RuntimeException</code> if contains invalid characters.</li>
   * </ul>
   *
   * @param key target key to check
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
   * @param value target to check
   * @return blank if <code>null</code>
   */
  public static String nvl(final String value) {
    return nvl(value, BLANK);
  }

  /**
   * Replaces <code>null</code>.<br>
   * <ul>
   * <li>Returns replacement string if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value target to check
   * @param nullDefault replacement string
   * @return replacement string if <code>null</code>
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
   * <li>Returns zero if number is <code>null</code>.</li>
   * </ul>
   *
   * @param value target to check
   * @return zero if <code>null</code>
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
   * <li>Returns replacement string if string is blank.</li>
   * <li>Uses <code>#isBlank(String)</code> for blank check.</li>
   * </ul>
   *
   * @param value target to check
   * @param blankDefault replacement string
   * @return replacement string if blank
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
   * @param joint joining character
   * @param values targets to join
   * @return the joined string
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
   * @param joint joining character
   * @param list targets to join
   * @return the joined string
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
   * Joins set.<br>
   * <ul>
   * <li>Returns blank if set is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param joint joining character
   * @param list targets to join
   * @return the joined string
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
   * Joins array as CSV.<br>
   * <ul>
   * <li>Returns blank if array is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * <li>When CSV type includes double quotes, converts double quotes (") within values to two double quotes ("").</li>
   * <li>When CSV type allows line breaks and values contain line break codes, unifies line break codes (CRLF/CR) to LF.</li>
   * <li>When CSV type does not allow line breaks (other than line break types) and values contain line break codes, converts line break codes (CRLF/CR/LF) to half-width spaces.</li>
   * </ul>
   *
   * @param values targets to join
   * @param csvType CSV type
   * @return the joined CSV string
   */
  static String joinCsv(final String[] values, final CsvType csvType) {
    if (isEmpty(values)) {
      return BLANK;
    }
    if (csvType == CsvType.NO_DQ) {
      return joinCsvNoDq(values);
    } else if (csvType == CsvType.DQ_ALL) {
      return joinCsvAllDq(values, false);
    } else if (csvType == CsvType.DQ_ALL_LF) {
      return joinCsvAllDq(values, true);
    } else if (csvType == CsvType.DQ_STD) {
      return joinCsvStdDq(values, false);
    } else {
      return joinCsvStdDq(values, true);
    }
  }

  /**
   * Joins array as CSV.
   *
   * @param values targets to join
   * @return the joined CSV string
   */
  private static String joinCsvNoDq(final String[] values) {
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
   * Joins array as CSV with double quotes.
   *
   * @param values targets to join
   * @param hasLf flag indicating line breaks are allowed
   * @return the joined CSV string
   */
  private static String joinCsvAllDq(final String[] values, final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final String value : values) {
      final String val = ValUtil.nvl(value);
      sb.append('"').append(ValUtil.convCsvDqWrap(val, hasLf)).append('"');
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Joins array as CSV with double quotes compliant with CSV specification.
   *
   * @param values targets to join
   * @param hasLf flag indicating line breaks are allowed
   * @return the joined CSV string
   */
  private static String joinCsvStdDq(final String[] values, final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final String value : values) {
      final String val = ValUtil.nvl(value);
      // Quote only when comma, line break, or double quote is included
      if (val.contains(",") || val.contains("\"") || val.contains(ValUtil.LF) || val.contains(ValUtil.CR)) {
        sb.append('"').append(ValUtil.convCsvDqWrap(val, hasLf)).append('"');
      } else {
        sb.append(val);
      }
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Converts for CSV double quote wrapping.<br>
   * <ul>
   * <li>Converts double quotes (") within values to two double quotes ("").</li>
   * <li>When line break flag is <code>true</code> and values contain line break codes, unifies line break codes (CRLF/CR) to LF.</li>
   * <li>When line break flag is <code>false</code> and values contain line break codes, converts line break codes (CRLF/CR/LF) to half-width spaces.</li>
   * </ul>
   * 
   * @param value target string
   * @param hasLf flag indicating line breaks are allowed
   * @return the converted string
   */
  static String convCsvDqWrap(final String value, final boolean hasLf) {
    if (hasLf) {
      return value.replace("\"", "\"\"").replace(ValUtil.CRLF, ValUtil.LF).replace(ValUtil.CR, ValUtil.LF);
    } else {
      return value.replace("\"", "\"\"").replace(ValUtil.CRLF, " ").replace(ValUtil.CR, " ").replace(ValUtil.LF, " ");
    }
  }

  /**
   * Splits into array.<br>
   * <ul>
   * <li>Returns a zero-length array if the string is <code>null</code>.</li>
   * </ul>
   *
   * @param value target string to split
   * @param sep separator character
   * @return the split string array
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
   * Splits into array using regular expression.<br>
   * <ul>
   * <li>Returns a zero-length array if the string is <code>null</code>.</li>
   * </ul>
   *
   * @param value target string to split
   * @param sep separator character (regular expression)
   * @return the split string array
   */
  public static String[] splitReg(final String value, final String sep) {
    return splitReg(value, sep, -1);
  }

  /**
   * Splits into array using regular expression.<br>
   * <ul>
   * <li>Returns a zero-length array if the string is <code>null</code>.</li>
   * </ul>
   *
   * @param value target string to split
   * @param sep separator character (regular expression)
   * @param limitLength maximum length
   * @return the split string array
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
   * <li>Splits a CSV string into a string array.</li>
   * <li>Returns a zero-length array if the CSV string is <code>null</code>.</li>
   * <li>When the CSV has double quotes, converts two double quotes ("") within values to a single double quote (").</li>
   * </ul>
   *
   * @param csv CSV string
   * @param csvType CSV type
   * @return the split string array
   */
  static String[] splitCsv(final String csv, final CsvType csvType) {
    if (isNull(csv)) {
      return new String[] {};
    }
    final List<String> list = new ArrayList<>();

    if (csvType == CsvType.NO_DQ) {
      for (final String value : new SimpleSeparateParser(csv, ",")) {
        list.add(value);
      }
    } else {
      for (final String value : new CsvDqParser(csv)) {
        list.add(value.replace("\"\"", "\""));
      }
    }
    return list.toArray(new String[0]);
  }

  /**
   * Compares strings.<br>
   * <ul>
   * <li>Treats <code>null</code> as a zero-byte blank string for comparison.</li>
   * </ul>
   *
   * @param str1 first target to compare
   * @param str2 second target to compare
   * @return <code>true</code> if equal
   */
  public static boolean equals(final String str1, final String str2) {
    return nvl(str1).equals(nvl(str2));
  }

  /**
   * Compares numbers.<br>
   * <ul>
   * <li>Treats <code>null</code> as zero for comparison.</li>
   * </ul>
   *
   * @param dec1 first target to compare
   * @param dec2 second target to compare
   * @return <code>true</code> if equal
   */
  public static boolean equals(final BigDecimal dec1, final BigDecimal dec2) {
    // equals with different precision does not return true, so use compareTo
    // e.g., new BigDecimal("1").equals(new BigDecimal("1.0")) does not return true
    return (nvl(dec1).compareTo(nvl(dec2)) == 0);
  }

  /**
   * Safely extracts a substring (start index only).<br>
   * <ul>
   * <li>Extracts from the start index to the end of the string.</li>
   * </ul>
   *
   * @param value      target string
   * @param beginIndex start index
   * @return the extracted string
   */
  public static String substring(final String value, final Integer beginIndex) {
    return substring(value, beginIndex, null);
  }

  /**
   * Safely extracts a substring.<br>
   * <ul>
   * <li>Safely extracts a specified range of the string.</li>
   * <li>Handles out-of-range specifications and invalid values appropriately.</li>
   * </ul>
   *
   * @param value      target string
   * @param beginIndex start index (optional); defaults to 0 if <code>null</code> is passed (omitted)
   * @param endIndex   end index (optional); defaults to the string length if <code>null</code> is passed (omitted)
   * @return the extracted string
   */
  public static String substring(final String value, final Integer beginIndex, final Integer endIndex) {
    if (isNull(value)) {
      return BLANK;
    }
    
    // Fill in default values
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
    // Clamp to valid range
      end = Math.min(endIndex, value.length());
    }
    
    // Returns blank if the start position is at or after the end position, or at or after the string length
    if (begin < 0 || begin >= end || begin >= value.length()) {
      return BLANK;
    }

    return value.substring(begin, end);
  }

  /**
   * Checks alphanumeric.<br>
   * <ul>
   * <li>Checks if the string is valid as alphanumeric characters.</li>
   * </ul>
   *
   * @param value target to check
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
   * <li>Checks if the string is valid as a numeric value.</li>
   * </ul>
   *
   * @param value target to check
   * @return <code>true</code> if valid
   */
  public static boolean isNumber(final String value) {
    return isNumber(value, false, false);
  }

  /** Pattern for numeric check. */
  private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^([1-9]\\d*|0)$");
  /** Pattern for numeric check - allows decimals. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC =
      Pattern.compile("^([1-9]\\d*|0)(\\.\\d+)?$");
  /** Pattern for numeric check - allows negative values. */
  private static final Pattern VALID_NUMBER_PATTERN_MINUS = Pattern.compile("^[-]?([1-9]\\d*|0)$");
  /** Pattern for numeric check - allows decimals and negative values. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC_MINUS =
      Pattern.compile("^[-]?([1-9]\\d*|0)(\\.\\d+)?$");

  /**
   * Checks numeric.<br>
   * <ul>
   * <li>Checks if the string is valid as a numeric value.</li>
   * </ul>
   *
   * @param value target to check
   * @param minusNg <code>true</code> to treat negative values as invalid
   * @param decNg <code>true</code> to treat decimals as invalid
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
   * Checks length.<br>
   * <ul>
   * <li>Assumes <code>#isBlank(String)</code> is <code>true</code>; returns <code>false</code> if the target is blank.</li>
   * </ul>
   * 
   * @param value      target to check
   * @param len valid length
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
   * Checks numeric length (precision).<br>
   * <ul>
   * <li>Verifies that the integer part and decimal part lengths are within the specified range.</li>
   * <li>Arguments follow the same DB field definition format: specify the total length of integer and decimal parts, and the decimal part length separately.</li>
   * <li>Assumes <code>#isNumber(String)</code> is <code>true</code>; returns <code>false</code> if the target is blank.</li>
   * </ul>
   * 
   * @param value      target to check
   * @param intPartLen valid length of integer part plus decimal part combined
   * @param decPartLen valid length of decimal part
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
   * Removes leading zeros for numeric check.<br>
   * <ul>
   * <li>Also supports numbers with decimal points.</li>
   * <li>Normalizes "-0" or "-000" to "0".</li>
   * </ul>
   *
   * @param value string
   * @return the processed string
   */
  private static String trimLeftZeroByIsNumber(final String value) {
    final boolean hasMinus = value.startsWith("-");
    final String tmp;
    if (hasMinus) {
      tmp = value.substring(1);
    } else {
      tmp = value;
    }
      
    // Check decimal point position
    final int dotIndex = tmp.indexOf('.');
    final String ret;
    
    if (dotIndex == -1) {
      // No decimal point: standard leading zero removal
      ret = tmp.replaceAll("^0+", "");
    } else if (dotIndex == 0) {
      // Format like ".5": return as is (no integer part)
      ret = tmp;
    } else {
      // Has decimal point: remove leading zeros from integer part only
      final String intPart = tmp.substring(0, dotIndex).replaceAll("^0+", "");
      final String decPart = tmp.substring(dotIndex); // includes "."
      ret = intPart + decPart;
    }
    
    // If all zeros
    if (isBlank(ret) || ret.equals(".")) {
      return "0";
    }
    
    // Integer part is empty → fill with "0"
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
   * Checks valid date.<br>
   * <ul>
   * <li>Checks if the string is valid as a date.</li>
   * </ul>
   *
   * @param value target to check (YYYYMMDD)
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
      // If a parse error occurs
      return false;
    }
    return true;
  }

  /** Set of strings considered as boolean true. */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "on");

  /**
   * Checks boolean true.<br>
   * <ul>
   * <li>Checks if the string is considered as boolean true.</li>
   * <li>Evaluates as follows:
   * <ol>
   * <li>"1", "true", "yes", "on" (all half-width) are <code>true</code>.</li>
   * <li><code>null</code> or blank, as well as any other values, are <code>false</code>.</li>
   * <li>Case-insensitive.</li>
   * <li>Leading and trailing half-width spaces are ignored.</li>
   * </ol>
   * </li>
   * </ul>
   *
   * @param val target to check
   * @return <code>true</code> if considered as boolean true
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
   * Converts from Date type to LocalDate type.
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
   * Removes double quotes.<br>
   * <ul>
   * <li>Removes double quotes from the start and end of the string.</li>
   * </ul>
   *
   * @see #trimBothEnds(String, char, char)
   * @param value target string
   * @return the string after removal
   */
  public static String trimDq(final String value) {
    return trimBothEnds(value, '"', '"');
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims only when both end characters are found.</li>
   * </ul>
   *
   * @param value target string
   * @param prefix character at the start
   * @param suffix character at the end
   * @return the string with both ends trimmed
   */
  public static String trimBothEnds(final String value, final char prefix, final char suffix) {
    return trimBothEnds(value, String.valueOf(prefix), String.valueOf(suffix));
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims only when both end characters are found.</li>
   * </ul>
   *
   * @param value target string
   * @param prefix character at the start
   * @param suffix character at the end
   * @return the string with both ends trimmed
   */
  public static String trimBothEnds(final String value, final String prefix, final String suffix) {
    if (ValUtil.isNull(value) || value.length() < 2) {
      // Must have at least 2 characters
      return value;
    }
    if (value.startsWith(prefix) && value.endsWith(suffix)) {
      return value.substring(prefix.length(), value.length() - suffix.length());
    }
    return value;
  }

  /** Regular expression - leading full-width space. */
  private static final String REGEX_ZENKAKU_SPACE_START = "^[" + '\u3000' + "]+";
  /** Regular expression - trailing full-width space. */
  private static final String REGEX_ZENKAKU_SPACE_END = "[" + '\u3000' + "]+$";

  /**
   * Trims full-width spaces from both ends of string.<br>
   * <ul>
   * <li>Removes full-width spaces from the start and end of the string.</li>
   * </ul>
   *
   * @param value target string
   * @return the string after removal
   */
  public static String trimZenkakuSpace(final String value) {
    if (isNull(value)) {
      return value;
    }
    return value.replaceFirst(REGEX_ZENKAKU_SPACE_START, BLANK)
        .replaceFirst(REGEX_ZENKAKU_SPACE_END, BLANK);
  }

  /**
   * Removes leading zeros.<br>
   * <ul>
   * <li>Removes <code>"0"</code> from the left of the string.</li>
   * </ul>
   *
   * @param value string
   * @return the processed string
   */
  public static String trimLeftZero(final String value) {
    if (isNull(value)) {
      return value;
    }
    final String ret = value.replaceAll("^0+", "");
    return ret;
  }

  /**
   * Pads with leading zeros.<br>
   * <ul>
   * <li>Pads <code>"0"</code> to the left of the string.</li>
   * <li>Pads with <code>0</code> if the argument is <code>null</code>.</li>
   * </ul>
   *
   * @param value string
   * @param digit number of characters after padding
   * @return the processed string
   */
  public static String paddingLeftZero(final String value, final int digit) {
    if (ValUtil.isNull(value)) {
      return ("%0" + digit + "d").formatted(0);
    }
    if (value.length() >= digit) {
      // Return as is if length exceeds the specified digit count
      return value;
    }
    final StringBuilder sb = new StringBuilder(value);
    while (sb.length() < digit) {
      sb.insert(0, "0");
    }
    return sb.toString();
  }

  /**
   * Pads with leading zeros.<br>
   * <ul>
   * <li>Converts a number to a string and pads <code>"0"</code> to the left.</li>
   * </ul>
   *
   * @param value number
   * @param digit number of characters after padding
   * @return the processed string
   */
  public static String paddingLeftZero(final int value, final int digit) {
    return ("%0" + digit + "d").formatted(value);
  }


  /**
   * Deletes the last character.
   *
   * @param sb StringBuilder
   */
  public static void deleteLastChar(final StringBuilder sb) {
    if (sb.length() >= 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * Deletes the last characters.
   *
   * @param sb StringBuilder
   * @param length length of characters to delete from the end
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
   * <li>To match JavaScript's encodeURIComponent, converts single-byte blank from "+" to "%20".</li>
   * <li>Converts wildcard "*" to "%2A".</li>
   * </ul>
   *
   * @param url target string to convert
   * @return the converted string
   */
  public static String urlEncode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLEncoder.encode(value, ValUtil.UTF8).replace("+", "%20").replace("*",
          "%2A");
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * Converts URL decoding.
   *
   * @param url target string to convert
   * @return the converted string
   */
  public static String urlDecode(final String url) {
    final String value = ValUtil.nvl(url);
    try {
      return URLDecoder.decode(value, ValUtil.UTF8);
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  /**
   * Converts JSON escape.<br>
   * <ul>
   * <li>Escapes characters that require escaping in JSON.</li>
   * <li>Also escapes <code>null</code> as a control character.</li>
   * </ul>
   *
   * @param value string
   * @return the converted string
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
   * <li>Removes escaping of characters that require escaping in JSON.</li>
   * </ul>
   *
   * @param value string
   * @return the string after removal
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

  /** Formatter for sequence code: timestamp (SQL:YYYYMMDDHH24MISSFF9). */
  private static final DateTimeFormatter SEQCODE_DTF = DateTimeFormatter
      .ofPattern("uuuuMMddHHmmssSSSSSSSSS").withResolverStyle(ResolverStyle.STRICT);
  /** Timestamp split digit specification for sequence code. */
  private static final int[][] SEQCODE_TM_SPLIT_KETA = {{2, 5}, // year:month1 max "991"
      {5, 8}, // month2:day max "930"
      {8, 11}, // hour:minute1 max "235"
      {11, 14}, // minute2:second max "959"
      {14, 17}, // millisecond max "999"
      {17, 20}, // microsecond max "999"
      {20, 23} // nanosecond max "999"
  };

  /**
   * Retrieves the sequence code value.<br>
   * <ul>
   * <li>Returns a timestamp including nanoseconds converted to base-36.</li>
   * <li>Sleeps for 1 nanosecond before returning to ensure uniqueness within the system.</li>
   * <li>When used across multiple systems, additional consideration such as appending a hostname or system name is required to ensure unique values.</li>
   * </ul>
   *
   * @return the sequence code value
   */
  public static synchronized String getSequenceCode() {
    // Sleep for 1ns
    try {
      TimeUnit.NANOSECONDS.sleep(1);
    } catch (InterruptedException ignore) {
      // No processing
    }
    // Split timestamp
    final String tm = LocalDateTime.now().format(SEQCODE_DTF);

    final StringBuilder sb = new StringBuilder();
    for (final int[] keta : SEQCODE_TM_SPLIT_KETA) {
      final String tmPart = tm.substring(keta[0], keta[1]);
      final int tmPartInt = Integer.parseInt(tmPart);
      final String tmPart36 = Integer.toString(tmPartInt, Character.MAX_RADIX);
      sb.append("%2s".formatted(tmPart36).replace(' ', '0').toUpperCase());
    }
    return sb.toString();
  }



}
