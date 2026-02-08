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
   * リスト連結.<br>
   * <ul>
   * <li>リストが空の場合はブランクを返す。</li>
   * <li><code>null</code> はブランクとして連結される。</li>
   * </ul>
   *
   * @param joint つなぎ目文字
   * @param list 連結対象
   * @return 連結した文字列
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
   * リスト連結.<br>
   * <ul>
   * <li>リストが空の場合はブランクを返す。</li>
   * <li><code>null</code> はブランクとして連結される。</li>
   * </ul>
   *
   * @param joint つなぎ目文字
   * @param list 連結対象
   * @return 連結した文字列
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
   * 配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字
   * @return 分割した文字列配列
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
   * 正規表現配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字（正規表現）
   * @return 分割した文字列配列
   */
  public static String[] splitReg(final String value, final String sep) {
    return splitReg(value, sep, -1);
  }

  /**
   * 正規表現配列分割.<br>
   * <ul>
   * <li>文字が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * </ul>
   *
   * @param value 分割対象文字
   * @param sep 分割文字（正規表現）
   * @param limitLength 最大長さ
   * @return 分割した文字列配列
   */
  public static String[] splitReg(final String value, final String sep, final int limitLength) {
    if (isNull(value)) {
      return new String[] {};
    }
    return value.split(sep, limitLength);
  }

  /**
   * CSV分割.<br>
   * <ul>
   * <li>CSV文字列を文字列配列に分割する。</li>
   * <li>CSV文字列が <code>null</code> の場合は長さゼロの配列を返す。</li>
   * <li>ダブルクォーテーション付 CSVの場合、値内のダブルクォーテーション2つ("")をダブルクォーテーション(")に変換して格納する。</li>
   * </ul>
   *
   * @param csv CSV文字列
   * @param csvType CSVタイプ
   * @return 分割した文字列配列
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
   * 文字列比較.<br>
   * <ul>
   * <li><code>null</code> はゼロバイトブランク文字として比較する。</li>
   * </ul>
   *
   * @param str1 比較対象その1
   * @param str2 比較対象その2
   * @return 同値の場合は <code>true</code>
   */
  public static boolean equals(final String str1, final String str2) {
    return nvl(str1).equals(nvl(str2));
  }

  /**
   * 数値比較.<br>
   * <ul>
   * <li><code>null</code> はゼロとして比較する。</li>
   * </ul>
   *
   * @param dec1 比較対象その1
   * @param dec2 比較対象その2
   * @return 同値の場合は <code>true</code>
   */
  public static boolean equals(final BigDecimal dec1, final BigDecimal dec2) {
    // 精度違いの equals は <code>true</code> にならないので compareTo を使用する
    // 例：new BigDecimal("1").equals(new BigDecimal("1.0")) は <code>true</code> にならない
    return (nvl(dec1).compareTo(nvl(dec2)) == 0);
  }

  /**
   * 文字列安全切り取り（開始インデックスのみ指定）.<br>
   * <ul>
   * <li>開始インデックスから文字列の最後まで切り取る。</li>
   * </ul>
   *
   * @param value      対象文字列
   * @param beginIndex 開始インデックス
   * @return 切り取り文字列
   */
  public static String substring(final String value, final Integer beginIndex) {
    return substring(value, beginIndex, null);
  }

  /**
   * 文字列安全切り取り.<br>
   * <ul>
   * <li>文字列の指定範囲を安全に切り取る。</li>
   * <li>範囲外の指定や不正な値に対して適切に処理する。</li>
   * </ul>
   *
   * @param value      対象文字列
   * @param beginIndex 開始インデックス（省略可能）<code>null</code> を渡した（省略した）場合は0
   * @param endIndex   終了インデックス（省略可能）<code>null</code> を渡した（省略した）場合は文字列長
   * @return 切り取り文字列
   */
  public static String substring(final String value, final Integer beginIndex, final Integer endIndex) {
    if (isNull(value)) {
      return BLANK;
    }
    
    // 省略値の補完
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
    // 範囲外補正
      end = Math.min(endIndex, value.length());
    }
    
    // 開始位置が終了位置以降、または開始位置が文字列長以降の場合は空文字を返す
    if (begin < 0 || begin >= end || begin >= value.length()) {
      return BLANK;
    }

    return value.substring(begin, end);
  }

  /**
   * 英数字チェック.<br>
   * <ul>
   * <li>文字列が英数字として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isAlphabetNumber(final String value) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    return Pattern.matches("^[a-zA-Z0-9]+$", value);
  }

  /**
   * 数値チェック.<br>
   * <ul>
   * <li>文字列が数値として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isNumber(final String value) {
    return isNumber(value, false, false);
  }

  /** 数値チェック用パターン. */
  private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^([1-9]\\d*|0)$");
  /** 数値チェック用パターン - 小数許容. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC =
      Pattern.compile("^([1-9]\\d*|0)(\\.\\d+)?$");
  /** 数値チェック用パターン - マイナス値許容. */
  private static final Pattern VALID_NUMBER_PATTERN_MINUS = Pattern.compile("^[-]?([1-9]\\d*|0)$");
  /** 数値チェック用パターン - 小数許容、マイナス値許容. */
  private static final Pattern VALID_NUMBER_PATTERN_DEC_MINUS =
      Pattern.compile("^[-]?([1-9]\\d*|0)(\\.\\d+)?$");

  /**
   * 数値チェック.<br>
   * <ul>
   * <li>文字列が数値として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象
   * @param minusNg マイナス値を無効とする場合は <code>true</code>
   * @param decNg 小数を無効とする場合は <code>true</code>
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isNumber(final String value, final boolean minusNg, final boolean decNg) {
    if (isBlank(value)) {
      // ブランクの場合
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
   * 桁数チェック.<br>
   * <ul>
   * <li><code>#isBlank(String)</code> が <code>true</code> の前提とし、チェック対象がブランクの場合は <code>false</code> を返す。</li>
   * </ul>
   * 
   * @param value      チェック対象
   * @param len 有効桁数
   * @return 有効な場合は <code>true</code>
   */
  public static boolean checkLength(final String value, final int len) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }
    return value.length() <= len;
  }

  /**
   * 数値桁数（精度）チェック.<br>
   * <ul>
   * <li>整数部分と小数点以下の桁数が指定された範囲内であることを確認する。</li>
   * <li>引数はDB項目定義と同じで整数部と小数部を足した桁数と小数部だけの桁数で指定する。</li>
   * <li><code>#isNumber(String)</code> が <code>true</code> の前提とし、チェック対象がブランクの場合は
   * <code>false</code> を返す。</li>
   * </ul>
   * 
   * @param value      チェック対象
   * @param intPartLen 整数部と小数部を足した有効桁数
   * @param decPartLen 小数部の有効桁数
   * @return 有効な場合は <code>true</code>
   */
  public static boolean checkLengthNumber(final String value, final int intPartLen,
      final int decPartLen) {
    if (isBlank(value)) {
      // ブランクの場合
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
   * 数値チェック用左ゼロ除去.<br>
   * <ul>
   * <li>小数点を含む数値にも対応。</li>
   * <li>"-0" や "-000" は "0" に正規化される。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 処理後の文字
   */
  private static String trimLeftZeroByIsNumber(final String value) {
    final boolean hasMinus = value.startsWith("-");
    final String tmp;
    if (hasMinus) {
      tmp = value.substring(1);
    } else {
      tmp = value;
    }
      
    // 小数点の位置を確認
    final int dotIndex = tmp.indexOf('.');
    final String ret;
    
    if (dotIndex == -1) {
      // 小数点なし: 通常の左ゼロ除去
      ret = tmp.replaceAll("^0+", "");
    } else if (dotIndex == 0) {
      // ".5" のような形式: そのまま返す（整数部がないため）
      ret = tmp;
    } else {
      // 小数点あり: 整数部分のみ左ゼロ除去
      final String intPart = tmp.substring(0, dotIndex).replaceAll("^0+", "");
      final String decPart = tmp.substring(dotIndex); // "." を含む
      ret = intPart + decPart;
    }
    
    // すべてゼロだった場合
    if (isBlank(ret) || ret.equals(".")) {
      return "0";
    }
    
    // 整数部分が空 → "0" を補完
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
   * 実在日チェック.<br>
   * <ul>
   * <li>文字列が日付として有効かチェックする。</li>
   * </ul>
   *
   * @param value チェック対象（YYYYMMDD）
   * @return 有効な場合は <code>true</code>
   */
  public static boolean isDate(final String value) {
    if (isBlank(value)) {
      // ブランクの場合
      return false;
    }

    if (value.length() != 8 || !isNumber(value)) {
      // 8桁以外または数値以外の場合
      return false;
    }
    try {
      LocalDate.parse(value, DTF_DATE);
    } catch (Exception e) {
      // パースしてエラーが発生した場合
      return false;
    }
    return true;
  }

  /** 真偽値「真」とみなす文字列のセット */
  private static final Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "on");

  /**
   * 真偽値チェック.<br>
   * <ul>
   * <li>文字列が真偽値「真」とみなす値かチェックする。</li>
   * <li>下記の評価を行う。
   * <ol>
   * <li>"1", "true", "yes", "on"（すべて半角）は <code>true</code>。</li>
   * <li><code>null</code> またはブランクは <code>false</code> を含み、上記以外は <code>false</code>。</li>
   * <li>大文字小文字を区別しない。</li>
   * <li>左右の半角スペースは無視する。</li>
   * </ol>
   * </li>
   * </ul>
   *
   * @param val チェック対象
   * @return 真偽値「真」とみなす場合は <code>true</code>
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
   * Date型 to LocalDate型変換.
   *
   * @param date Date型（java.sql.Date 含む）
   * @return LocalDate型
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
   * ダブルクォーテーション除去.<br>
   * <ul>
   * <li>文字列の開始と終了のダブルクォーテーションを除去する。</li>
   * </ul>
   *
   * @see #trimBothEnds(String, char, char)
   * @param value 対象文字列
   * @return 除去後文字列
   */
  public static String trimDq(final String value) {
    return trimBothEnds(value, '"', '"');
  }

  /**
   * 文字列両端切り捨て.<br>
   * <ul>
   * <li>トリムして両端文字ともに見つかった場合のみ切り捨てる。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @param prefix 前の端の文字
   * @param suffix 後ろの端の文字
   * @return 両端を切り捨てた文字列
   */
  public static String trimBothEnds(final String value, final char prefix, final char suffix) {
    return trimBothEnds(value, String.valueOf(prefix), String.valueOf(suffix));
  }

  /**
   * 文字列両端切り捨て.<br>
   * <ul>
   * <li>トリムして両端文字ともに見つかった場合のみ切り捨てる。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @param prefix 前の端の文字
   * @param suffix 後ろの端の文字
   * @return 両端を切り捨てた文字列
   */
  public static String trimBothEnds(final String value, final String prefix, final String suffix) {
    if (ValUtil.isNull(value) || value.length() < 2) {
      // 最低２文字以上はあるはず
      return value;
    }
    if (value.startsWith(prefix) && value.endsWith(suffix)) {
      return value.substring(prefix.length(), value.length() - suffix.length());
    }
    return value;
  }

  /** 正規表現 - 始端全角文字列. */
  private static final String REGEX_ZENKAKU_SPACE_START = "^[" + '\u3000' + "]+";
  /** 正規表現 - 終端全角文字列. */
  private static final String REGEX_ZENKAKU_SPACE_END = "[" + '\u3000' + "]+$";

  /**
   * 文字列両端全角スペース切り捨て.<br>
   * <ul>
   * <li>文字列の開始と終了の全角スペースを除去する。</li>
   * </ul>
   *
   * @param value 対象文字列
   * @return 除去後文字列
   */
  public static String trimZenkakuSpace(final String value) {
    if (isNull(value)) {
      return value;
    }
    return value.replaceFirst(REGEX_ZENKAKU_SPACE_START, BLANK)
        .replaceFirst(REGEX_ZENKAKU_SPACE_END, BLANK);
  }

  /**
   * 左ゼロ除去.<br>
   * <ul>
   * <li>文字列の左の <code>"0"</code> を除去する。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 処理後の文字
   */
  public static String trimLeftZero(final String value) {
    if (isNull(value)) {
      return value;
    }
    final String ret = value.replaceAll("^0+", "");
    return ret;
  }

  /**
   * 左ゼロ詰め.<br>
   * <ul>
   * <li>文字列の左に <code>"0"</code> を詰める。</li>
   * </ul>
   *
   * @param value 文字列
   * @param digit 詰めた後の文字桁数
   * @return 処理後の文字
   */
  public static String paddingLeftZero(final String value, final int digit) {
    if (ValUtil.isNull(value)) {
      return String.format("%0" + digit + "d", 0);
    }
    if (value.length() >= digit) {
      // 長さが超えている場合はそのまま返す
      return value;
    }
    final StringBuilder sb = new StringBuilder(value);
    while (sb.length() < digit) {
      sb.insert(0, "0");
    }
    return sb.toString();
  }

  /**
   * 左ゼロ詰め.<br>
   * <ul>
   * <li>数値を文字列に変換して左に <code>"0"</code> を詰める。</li>
   * </ul>
   *
   * @param value 数値
   * @param digit 詰めた後の文字桁数
   * @return 処理後の文字
   */
  public static String paddingLeftZero(final int value, final int digit) {
    return String.format("%0" + String.valueOf(digit) + "d", value);
  }


  /**
   * 最終１文字削除.
   *
   * @param sb StringBuilder
   */
  public static void deleteLastChar(final StringBuilder sb) {
    if (sb.length() >= 1) {
      sb.deleteCharAt(sb.length() - 1);
    }
  }

  /**
   * 最終文字削除.
   *
   * @param sb StringBuilder
   * @param length 最終文字長さ
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
   * URLエンコード変換.<br>
   * <ul>
   * <li>JavaScript の encodeURIComponent と合わせるため１バイトブランクを "+" から "%20" に変換しなおす。</li>
   * <li>ワイルドカードの "*" を "%2A" に変換する。</li>
   * </ul>
   *
   * @param url 変換文字列
   * @return 変換後文字列
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
   * URLデコード変換.
   *
   * @param url 変換文字列
   * @return 変換後文字列
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
   * JSONエスケープ変換.<br>
   * <ul>
   * <li>JSONでエスケープが必要な文字をエスケープする。</li>
   * <li><code>null</code> も制御文字としてエスケープする。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 変換後文字列
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
   * JSONエスケープ変換除去.<br>
   * <ul>
   * <li>JSONでエスケープが必要な文字のエスケープを除去する。</li>
   * </ul>
   *
   * @param value 文字列
   * @return 除去後文字列
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

  /** シーケンスコード用 フォーマッター：タイムスタンプ（SQL:YYYYMMDDHH24MISSFF9）. */
  private static final DateTimeFormatter SEQCODE_DTF = DateTimeFormatter
      .ofPattern("uuuuMMddHHmmssSSSSSSSSS").withResolverStyle(ResolverStyle.STRICT);
  /** シーケンスコード用 タイムスタンプ分割桁指定. */
  private static final int[][] SEQCODE_TM_SPLIT_KETA = {{2, 5}, // 年:月1 最大 "991"
      {5, 8}, // 月2日 最大 "930"
      {8, 11}, // 時:分1 最大 "235"
      {11, 14}, // 分2:秒 最大 "959"
      {14, 17}, // ミリ秒 最大 "999"
      {17, 20}, // マイクロ秒 最大 "999"
      {20, 23} // ナノ秒 最大 "999"
  };

  /**
   * シーケンスコード値取得.<br>
   * <ul>
   * <li>ナノ秒まで含むタイムスタンプを36進数に変換した値を返す。</li>
   * <li>システム内で常にユニークな値を返すため１ナノ秒スリープしてから返す。</li>
   * <li>複数のシステムで使用する場合、ユニークな値を返すためにはホスト名やシステム名を付加するなどの考慮が必要となる。</li>
   * </ul>
   *
   * @return シーケンスコード値
   */
  public static synchronized String getSequenceCode() {
    // 1nsスリープ
    try {
      TimeUnit.NANOSECONDS.sleep(1);
    } catch (InterruptedException ignore) {
      // 処理なし
    }
    // タイムスタンプを分割
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
