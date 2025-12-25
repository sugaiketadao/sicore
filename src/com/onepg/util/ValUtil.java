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

  /** Single quote character. */
  public static final String SQ = "'";
  /** Double quote character. */
  public static final String DQ = "\"";

  /** JSON <code>null</code> string. */
  public static final String JSON_NULL = "null";

  /** Date-time formatter: date. */
  private static final DateTimeFormatter DTF_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Input-output map key check pattern.<br>
   * <ul>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscores</li><li>Hyphens</li><li>Dots</li></ul>
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
     * @param value the character set
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
     * @param value the line separator
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
   * Checks for <code>null</code>.<br>
   * <ul>
   * <li>Checks whether <code>Object</code> is <code>null</code>.</li>
   * </ul>
   *
   * @param obj the target to check
   * @return <code>true</code> if <code>null</code>
   */
  public static boolean isNull(final Object obj) {
    return null == obj;
  }

  /**
   * Checks for blank.<br>
   * <ul>
   * <li>Determines as blank if composed of only half-width spaces, zero-byte string, or <code>null</code>.</li>
   * </ul>
   *
   * @param value the target to check
   * @return <code>true</code> if blank
   */
  public static boolean isBlank(final String value) {
    if (isNull(value)) {
      return true;
    }
    return (value.trim().length() == 0);
  }

  /**
   * Checks for empty.<br>
   * <ul>
   * <li>Checks whether array has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param values the target to check
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final Object[] values) {
    return (values == null || values.length == 0);
  }

  /**
   * Checks for empty.<br>
   * <ul>
   * <li>Checks whether list has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param list the target to check
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final List<?> list) {
    if (isNull(list)) {
      return true;
    }
    return list.isEmpty();
  }

  /**
   * Checks for empty.<br>
   * <ul>
   * <li>Checks whether map has zero length or is <code>null</code>.</li>
   * </ul>
   *
   * @param map the target to check
   * @return <code>true</code> if empty
   */
  public static boolean isEmpty(final Map<?, ?> map) {
    if (isNull(map)) {
      return true;
    }
    return map.isEmpty();
  }


  /**
   * Checks input-output map key format.<br>
   * <ul>
   * <li>Checks whether the key contains only valid characters for input-output map keys.</li>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscores</li><li>Hyphens</li><li>Dots</li></ul>
   * </li>
   * </ul>
   *
   * @param key the key to check
   * @return <code>false</code> if contains invalid characters
   */
  public static boolean isValidIoKey(final String key) {
    if (isBlank(key)) {
      return false;
    }
    return PATTERN_IO_KEY_CHECK.matcher(key).matches();
  }

  /**
   * Checks input-output map key format.<br>
   * <ul>
   * <li>Checks whether the key contains only valid characters for input-output map keys.</li>
   * <li>Allowed characters are as follows:
   * <ul><li>Lowercase letters</li><li>Digits</li><li>Underscores</li><li>Hyphens</li><li>Dots</li></ul>
   * </li>
   * <li>Throws <code>RuntimeException</code> if contains invalid characters.</li>
   * </ul>
   *
   * @param key the key to check
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
   * @param value the target to check
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
   * @param value the target to check
   * @param nullDefault the replacement string
   * @return the replacement string if <code>null</code>
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
   * @param value the target to check
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
   * <li>Uses <code>#isBlank(String)</code> for blank determination.</li>
   * </ul>
   *
   * @param value the target to check
   * @param blankDefault the replacement string
   * @return the replacement string if blank
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
   * @param joint the joint character
   * @param values the target to join
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
   * @param joint the joint character
   * @param list the target to join
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
   * Joins list.<br>
   * <ul>
   * <li>Returns blank if list is empty.</li>
   * <li><code>null</code> is joined as blank.</li>
   * </ul>
   *
   * @param joint the joint character
   * @param list the target to join
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
   * Splits into array.<br>
   * <ul>
   * <li>Returns zero-length array if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value the string to split
   * @param sep the separator string
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
   * <li>Returns zero-length array if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value the string to split
   * @param sep the separator string (regular expression)
   * @return the split string array
   */
  public static String[] splitReg(final String value, final String sep) {
    return splitReg(value, sep, -1);
  }

  /**
   * Splits into array using regular expression.<br>
   * <ul>
   * <li>Returns zero-length array if string is <code>null</code>.</li>
   * </ul>
   *
   * @param value the string to split
   * @param sep the separator string (regular expression)
   * @param limitLength the maximum length
   * @return the split string array
   */
  public static String[] splitReg(final String value, final String sep, final int limitLength) {
    if (isNull(value)) {
      return new String[] {};
    }
    return value.split(sep, limitLength);
  }

  /**
   * Compares strings.<br>
   * <ul>
   * <li>Compares <code>null</code> as zero-byte blank string.</li>
   * </ul>
   *
   * @param str1 the first comparison target
   * @param str2 the second comparison target
   * @return <code>true</code> if equal
   */
  public static boolean equals(final String str1, final String str2) {
    return nvl(str1).equals(nvl(str2));
  }

  /**
   * Compares numbers.<br>
   * <ul>
   * <li>Compares <code>null</code> as zero.</li>
   * </ul>
   *
   * @param dec1 the first comparison target
   * @param dec2 the second comparison target
   * @return <code>true</code> if equal
   */
  public static boolean equals(final BigDecimal dec1, final BigDecimal dec2) {
    // Uses compareTo because equals does not return <code>true</code> for different precision
    // Example: new BigDecimal("1").equals(new BigDecimal("1.0")) does not return <code>true</code>
    return (nvl(dec1).compareTo(nvl(dec2)) == 0);
  }

  /**
   * Safely extracts substring (specifying only start index).<br>
   * <ul>
   * <li>Extracts from start index to the end of the string.</li>
   * </ul>
   *
   * @param value      the target string
   * @param beginIndex the start index
   * @return the extracted string
   */
  public static String substring(final String value, final Integer beginIndex) {
    return substring(value, beginIndex, null);
  }

  /**
   * Safely extracts substring.<br>
   * <ul>
   * <li>Safely extracts the specified range from the string.</li>
   * <li>Appropriately handles out-of-range or invalid values.</li>
   * </ul>
   *
   * @param value      the target string
   * @param beginIndex the start index (optional); 0 if <code>null</code> is passed (omitted)
   * @param endIndex   the end index (optional); string length if <code>null</code> is passed (omitted)
   * @return the extracted string
   */
  public static String substring(final String value, final Integer beginIndex, final Integer endIndex) {
    if (isNull(value)) {
      return BLANK;
    }
    
    // Default value completion
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
    
    // Returns empty string if start position is at or after end position, or start position is at or after string length
    if (begin < 0 || begin >= end || begin >= value.length()) {
      return BLANK;
    }

    return value.substring(begin, end);
  }

  /**
   * Checks for alphanumeric.<br>
   * <ul>
   * <li>Checks whether string is valid as alphanumeric.</li>
   * </ul>
   *
   * @param value the target to check
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
   * Checks for number.<br>
   * <ul>
   * <li>Checks whether string is valid as a number.</li>
   * </ul>
   *
   * @param value the target to check
   * @return <code>true</code> if valid
   */
  public static boolean isNumber(final String value) {
    return isNumber(value, false, false);
  }

  /** Pattern for number check. */
  private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^([1-9]\\d*|0)$");
  /** Pattern for number check - decimal allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC =
      Pattern.compile("^([1-9]\\d*|0)(\\.\\d+)?$");
  /** Pattern for number check - negative allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_MINUS = Pattern.compile("^[-]?([1-9]\\d*|0)$");
  /** Pattern for number check - decimal allowed, negative allowed. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC_MINUS =
      Pattern.compile("^[-]?([1-9]\\d*|0)(\\.\\d+)?$");

  /**
   * Checks for number.<br>
   * <ul>
   * <li>Checks whether string is valid as a number.</li>
   * </ul>
   *
   * @param value the target to check
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
   * <li>Assumes <code>#isBlank(String)</code> is <code>true</code>, and returns <code>false</code> if target is blank.</li>
   * </ul>
   * 
   * @param value      the target to check
   * @param len the valid length
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
   * <li>Confirms that integer part and decimal part lengths are within specified ranges.</li>
   * <li>Arguments follow DB field definition: total of integer and decimal parts, and decimal part only.</li>
   * <li>Assumes <code>#isNumber(String)</code> is <code>true</code>, and returns
   * <code>false</code> if target is blank.</li>
   * </ul>
   * 
   * @param value      the target to check
   * @param intPartLen the valid length of integer and decimal parts combined
   * @param decPartLen the valid length of decimal part
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
   * Removes leading zeros for number check.<br>
   * <ul>
   * <li>Supports numbers including decimal points.</li>
   * <li>"-0" and "-000" are normalized to "0".</li>
   * </ul>
   *
   * @param value the string
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
      
    // Checks the position of the decimal point
    final int dotIndex = tmp.indexOf('.');
    final String ret;
    
    if (dotIndex == -1) {
      // No decimal point: removes leading zeros normally
      ret = tmp.replaceAll("^0+", "");
    } else if (dotIndex == 0) {
      // Format like ".5": returns as-is (no integer part)
      ret = tmp;
    } else {
      // With decimal point: removes leading zeros from integer part only
      final String intPart = tmp.substring(0, dotIndex).replaceAll("^0+", "");
      final String decPart = tmp.substring(dotIndex); // Includes "."
      ret = intPart + decPart;
    }
    
    // If all zeros
    if (isBlank(ret) || ret.equals(".")) {
      return "0";
    }
    
    // If integer part is empty → complements with "0"
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
   * Checks for valid date.<br>
   * <ul>
   * <li>Checks whether string is valid as a date.</li>
   * </ul>
   *
   * @param value the target to check (YYYYMMDD)
   * @return <code>true</code> if valid
   */
  public static boolean isDate(final String value) {
    if (isBlank(value)) {
      // If blank
      return false;
    }

    if (value.length() != 8 || !isNumber(value)) {
      // If not 8 digits or not a number
      return false;
    }
    try {
      LocalDate.parse(value, DTF_DATE);
    } catch (Exception e) {
      // If error occurred during parsing
      return false;
    }
    return true;
  }

  /** Set of strings considered as boolean "true". */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "on");

  /**
   * Checks for boolean true.<br>
   * <ul>
   * <li>Checks whether string is considered as boolean "true".</li>
   * <li>Performs the following evaluation:
   * <ol>
   * <li>"1", "true", "yes", "on" (all half-width) are <code>true</code>.</li>
   * <li><code>null</code> or blank, and anything other than above are <code>false</code>.</li>
   * <li>Case-insensitive.</li>
   * <li>Leading and trailing half-width spaces are ignored.</li>
   * </ol>
   * </li>
   * </ul>
   *
   * @param val the target to check
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
   * @param date the Date type (including java.sql.Date)
   * @return the LocalDate type
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
   * @param value the target string
   * @return the string after removal
   */
  public static String trimDq(final String value) {
    return trimBothEnds(value, '"', '"');
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims and removes only if both end characters are found.</li>
   * </ul>
   *
   * @param value the target string
   * @param prefix the leading end character
   * @param suffix the trailing end character
   * @return the string with both ends trimmed
   */
  public static String trimBothEnds(final String value, final char prefix, final char suffix) {
    return trimBothEnds(value, String.valueOf(prefix), String.valueOf(suffix));
  }

  /**
   * Trims both ends of string.<br>
   * <ul>
   * <li>Trims and removes only if both end characters are found.</li>
   * </ul>
   *
   * @param value the target string
   * @param prefix the leading end string
   * @param suffix the trailing end string
   * @return the string with both ends trimmed
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

  /** Regular expression - leading full-width space string. */
  private static final String REGEX_ZENKAKU_SPACE_START = "^[" + '\u3000' + "]+";
  /** Regular expression - trailing full-width space string. */
  private static final String REGEX_ZENKAKU_SPACE_END = "[" + '\u3000' + "]+$";

  /**
   * Trims full-width spaces from both ends of string.<br>
   * <ul>
   * <li>Removes full-width spaces from the start and end of the string.</li>
   * </ul>
   *
   * @param value the target string
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
   * @param value the string
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
   * </ul>
   *
   * @param value the string
   * @param digit the number of digits after padding
   * @return the processed string
   */
  public static String paddingLeftZero(final String value, final int digit) {
    if (ValUtil.isNull(value)) {
      return String.format("%0" + digit + "d", 0);
    }
    if (value.length() >= digit) {
      // Returns as-is if length exceeds
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
   * <li>Converts number to string and pads <code>"0"</code> to the left.</li>
   * </ul>
   *
   * @param value the number
   * @param digit the number of digits after padding
   * @return the processed string
   */
  public static String paddingLeftZero(final int value, final int digit) {
    return String.format("%0" + String.valueOf(digit) + "d", value);
  }


  /**
   * Deletes the last character.
   *
   * @param sb the StringBuilder
   */
  public static void deleteLastChar(final StringBuilder sb) {
    if (sb.length() >= 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * Deletes the last characters.
   *
   * @param sb the StringBuilder
   * @param length the length of last characters
   */
  public static void deleteLastChar(final StringBuilder sb, final int length) {
    if (length <= 0) {
      return;
    }
    if (sb.length() >= length) {
      sb.delete(sb.length() - length, sb.length());
    }
  }

  /**
   * URL encodes.<br>
   * <ul>
   * <li>Replaces one-byte blank from "+" to "%20" to match JavaScript's encodeURIComponent.</li>
   * <li>Converts wildcard "*" to "%2A".</li>
   * </ul>
   *
   * @param url the string to convert
   * @return the converted string
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
   * URL decodes.
   *
   * @param url the string to convert
   * @return the converted string
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
   * JSON escapes.<br>
   * <ul>
   * <li>Escapes characters that require escaping in JSON.</li>
   * <li>Also escapes <code>null</code> as a control character.</li>
   * </ul>
   *
   * @param value the string
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
   * Removes JSON escape.<br>
   * <ul>
   * <li>Removes escaping of characters that require escaping in JSON.</li>
   * </ul>
   *
   * @param value the string
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
  /** Digit specification for timestamp split for sequence code. */
  private static final int[][] SEQCODE_TM_SPLIT_KETA = {{2, 5}, // Year:Month1 max "991"
      {5, 8}, // Month2Day max "930"
      {8, 11}, // Hour:Minute1 max "235"
      {11, 14}, // Minute2:Second max "959"
      {14, 17}, // Millisecond max "999"
      {17, 20}, // Microsecond max "999"
      {20, 23} // Nanosecond max "999"
  };

  /**
   * Gets sequence code value.<br>
   * <ul>
   * <li>Returns a timestamp including nanoseconds converted to base-36.</li>
   * <li>Sleeps for 1 nanosecond before returning to always return a unique value within the system.</li>
   * <li>When used across multiple systems, consideration such as appending host name or system name is required to ensure unique values.</li>
   * </ul>
   *
   * @return the sequence code value
   */
  public static synchronized String getSequenceCode() {
    // Sleeps for 1 ns
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
