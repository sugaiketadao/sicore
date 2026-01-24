package com.onepg.util;

import java.util.Map;

/**
 * I/O field group map class.<br>
 * <ul>
 * <li>Can input/output CSV.</li>
 * <li>Can input/output JSON.</li>
 * <li>Can input/output URL parameters.</li>
 * <li>Basic rules and limitations conform to <code>AbstractIoTypeMap</code>.</li>
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
   * <li>Since the contents are immutable objects (<code>String</code>), it is effectively a deep copy.</li>
   * </ul>
   *
   * @param srcMap the source map
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap) {
    super(srcMap);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since the contents are immutable objects (<code>String</code>), it is effectively a deep copy.</li>
   * </ul>
   *
   * @param srcMap the source map
   * @param readOnly <code>true</code> to create a read-only map
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super(srcMap, readOnly);
  }

  /**
   * Creates CSV.<br>
   * <ul>
   * <li>Creates a CSV string in the order values were added.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return the CSV string
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
   * Creates CSV with double quotes.<br>
   * <ul>
   * <li>Creates a CSV string in the order values were added.</li>
   * <li>Outputs all fields with double quotes.</li>
   * <li>Double quotes in values are converted to two double quote characters.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return the CSV string
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
   * Creates CSV with CSV-specification-compliant double quotes.<br>
   * <ul>
   * <li>Creates a CSV string in the order values were added.</li>
   * <li>Outputs with double quotes added to fields that require them per CSV specification.</li>
   * <li>Double quotes in values are converted to two double quote characters.</li>
   * <li>String lists, nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return the CSV string
   */
  public String createCsvDq() {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      // Quote only if comma, newline, or double quote is contained
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
   * Creates URL parameters (the part after ? in a URL).
   *
   * @return the URL-encoded GET parameters
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
   * @return the JSON string
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
   * Creates log output string.
   *
   * @return the log output string
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
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>Fields with blank keys in the key name array are not stored. (Apply to fields that do not require storage.)</li>
   * <li>If CSV field count exceeds key name array count, excess fields are not stored.</li>
   * </ul>
   *
   * @param keys the key name array
   * @param csv the CSV string
   * @return the stored field count
   */
  public int putAllByCsv(final String[] keys, final String csv) {
    // Maximum index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new SimpleSeparateParser(csv, ",")) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Exit if CSV columns exceed key columns
        break;
      }

      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // Skip as unnecessary field if key name is blank
        continue;
      }

      // Store value
      count++;
      put(key, value);
    }
    return count;
  }

  /**
   * Stores double-quoted CSV.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>Fields with blank keys in the key name array are not stored. (Apply to fields that do not require storage.)</li>
   * <li>If CSV field count exceeds key name array count, excess fields are not stored.</li>
   * <li>Two consecutive double quotes in values are converted to a single double quote and stored.</li>
   * </ul>
   *
   * @see #putAllByCsv(String[], String)
   * @param keys the key name array
   * @param csv the CSV string
   * @return the stored field count
   */
  public int putAllByCsvDq(final String[] keys, final String csv) {
    // Maximum key index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new CsvDqParser(csv)) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Exit if CSV columns exceed key columns
        break;
      }
      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // Skip as unnecessary field if key name is blank
        continue;
      }

      // Store value
      count++;
      put(key, value.replace("\"\"", "\""));
    }
    return count;
  }

  /**
   * Stores URL parameter (the part after ? in a URL) values.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * </ul>
   *
   * @param url the full URL or URL parameters
   * @return the stored parameter count
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
        // Array key is an error
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
   * @param json the JSON string
   * @return the stored field count
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
   * Returns string for logging.
   */
  public final String toString() {
    return createLogString();
  }

}
