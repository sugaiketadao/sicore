package com.onepg.util;

import java.util.Map;

/**
 * Input/output items map class.<br>
 * <ul>
 * <li>Can input and output CSV.</li>
 * <li>Can input and output JSON.</li>
 * <li>Can input and output URL parameters.</li>
 * <li>The basic rules and restrictions conform to <code>AbstractIoTypeMap</code>.</li>
 * </ul>
 *
 * @see AbstractIoTypeMap
 */
public final class IoItems extends AbstractIoTypeMap {

  /**
   * Constructor.
   */
  public IoItems() {
    super();
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since the content is an immutable object (<code>String</code>), it effectively becomes a deep copy.</li>
   * </ul>
   *
   * @param srcMap source map
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap) {
    super(srcMap);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since the content is an immutable object (<code>String</code>), it effectively becomes a deep copy.</li>
   * </ul>
   *
   * @param srcMap source map
   * @param readOnly <code>true</code> to create a read-only map
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super(srcMap, readOnly);
  }

  /**
   * Creates CSV.<br>
   * <ul>
   * <li>Creates the CSV string in the value addition order.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return CSV string
   */
  public String createCsv() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      sb.append(val);
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Creates CSV with double quotations.<br>
   * <ul>
   * <li>Creates the CSV string in the value addition order.</li>
   * <li>Outputs all items with double quotations added.</li>
   * <li>If the value contains double quotations, they are converted to two double quotation characters.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return CSV string
   */
  public String createCsvAllDq() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      sb.append('"').append(val.replace("\"", "\"\"")).append('"');
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Creates CSV with CSV specification-compliant double quotations.<br>
   * <ul>
   * <li>Creates the CSV string in the value addition order.</li>
   * <li>Outputs necessary items with double quotations added in compliance with CSV specification.</li>
   * <li>If the value contains double quotations, they are converted to two double quotation characters.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return CSV string
   */
  public String createCsvDq() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      // Quote only if contains comma, line break, or double quote
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
   * Creates URL parameters (the part after ? in the URL).
   *
   * @return URL-encoded GET parameters
   */
  public String createUrlParam() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      final String val = valMap.get(key);
      final String encVal = ValUtil.urlEncode(val);
      sb.append(key).append('=').append(encVal).append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Creates JSON.
   *
   * @return JSON string
   */
  public String createJson() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      sb.append('"').append(key).append('"').append(':');
      final String val = valMap.get(key);
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '{');
    sb.append('}');
    return sb.toString();
  }

  /**
   * Creates the log output string.
   *
   * @return log output string
   */
  final String createLogString() {
    final StringBuilder sb = new StringBuilder();
    try {
      for (final Entry<String, String> ent : super.getValMap().entrySet()) {
        final String key = ent.getKey();
        final String val = ent.getValue();
        final String sval = LogUtil.convOutput(val);
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
   * Stores CSV.<br>
   * <ul>
   * <li>Storage with already existing keys results in a runtime error.</li>
   * <li>Items with blank keys in the argument key name array are not stored. (Apply to items that do not need storage)</li>
   * <li>If the number of CSV items is greater than the number of key name array elements, the surplus items are not stored.</li>
   * </ul>
   *
   * @param keys key name array
   * @param csv CSV string
   * @return the number of stored items
   */
  public int putAllByCsv(final String[] keys, final String csv) {
    // Maximum index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new SimpleSeparateParser(csv, ",")) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Terminate if CSV columns are more than key columns
        break;
      }

      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // If the key name is blank, skip it as an unnecessary item
        continue;
      }

      // Store value
      count++;
      put(key, value);
    }
    return count;
  }

  /**
   * Stores CSV with double quotations.<br>
   * <ul>
   * <li>Storage with already existing keys results in a runtime error.</li>
   * <li>Items with blank keys in the argument key name array are not stored. (Apply to items that do not need storage)</li>
   * <li>If the number of CSV items is greater than the number of key name array elements, the surplus items are not stored.</li>
   * <li>Two consecutive double quotations within the value are converted to one double quotation and stored.</li>
   * </ul>
   *
   * @see #putAllByCsv(String[], String)
   * @param keys key name array
   * @param csv CSV string
   * @return the number of stored items
   */
  public int putAllByCsvDq(final String[] keys, final String csv) {
    // Key maximum index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new CsvDqParser(csv)) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Terminate if CSV columns are more than key columns
        break;
      }
      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // If the key name is blank, skip it as an unnecessary item
        continue;
      }

      // Store value
      count++;
      put(key, value.replace("\"\"", "\""));
    }
    return count;
  }

  /**
   * Stores URL parameter (the part after ? in the URL) values.<br>
   * <ul>
   * <li>Storage with already existing keys results in a runtime error.</li>
   * </ul>
   *
   * @param url entire URL or URL parameters
   * @return the number of stored parameters
   */
  public int putAllByUrlParam(final String url) {
    if (ValUtil.isBlank(url)) {
      return 0;
    }

    final String params;
    if (url.indexOf('?') > 0) {
      params = url.substring(url.indexOf('?') + 1);
    } else {
      params = url;
    }

    int count = 0;
    for (final String param : new SimpleSeparateParser(params, "&")) {
      final String[] keyVal = ValUtil.splitReg(param, "=", 2);
      final String key = keyVal[0];
      final String val;
      if (keyVal.length == 1) {
        val = ValUtil.BLANK;
      } else {
        val = ValUtil.urlDecode(keyVal[1]);
      }

      if (key.endsWith("[]")) {
        // Array keys are errors
        throw new RuntimeException("Keys representing arrays cannot be used. " + LogUtil.joinKeyVal("key", key));
      }

      // Store value
      count++;
      put(key, val);
    }
    return count;

  }

  /**
   * Stores JSON.
   *
   * @param json JSON string
   * @return the number of stored items
   */
  public int putAllByJson(final String json) {
    if (ValUtil.isBlank(json)) {
      return 0;
    }

    int count = 0;

    // Loop through JSON items
    for (final String item : new JsonMapSeparateParser(json)) {
      final String[] keyVal = JsonMapKeyValueSeparateParser.getKeyValue(item);
      if (ValUtil.isNull(keyVal)) {
        continue;
      }
      final String key = keyVal[0];
      final String val = keyVal[1];

      if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(val).find()
          || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(val).find()) {
        throw new RuntimeException("Associative arrays and arrays are not supported as values. " + LogUtil.joinKeyVal("json", val));
      }

      count++;
      if (ValUtil.JSON_NULL.equals(val)) {
        putNull(key);
        continue;
      }
      final String unEscVal = ValUtil.jsonUnEscape(ValUtil.trimDq(val));
      put(key, unEscVal);
    }
    return count;
  }

  /**
   * Returns the string for logging.
   */
  public final String toString() {
    return createLogString();
  }

}
