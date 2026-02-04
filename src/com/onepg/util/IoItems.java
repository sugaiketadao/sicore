package com.onepg.util;

import java.util.Map;

import com.onepg.util.ValUtil.CsvType;

/**
 * Input/output items map class.<br>
 * <ul>
 * <li>Supports CSV input and output.</li>
 * <li>Supports JSON input and output.</li>
 * <li>Supports URL parameter input and output.</li>
 * <li>Follows the basic rules and restrictions of <code>AbstractIoTypeMap</code>.</li>
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
   * <li>Performs deep copy since the content is an immutable object (<code>String</code>).</li>
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
   * <li>Performs deep copy since the content is an immutable object (<code>String</code>).</li>
   * </ul>
   *
   * @param srcMap source map
   * @param readOnly <code>true</code> to create a read-only map
   */
  public IoItems(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super(srcMap, readOnly);
  }

  /**
   * Creates CSV string.<br>
   * <ul>
   * <li>Creates CSV string in the order values were added to the map.</li>
   * <li>When CSV type includes double quotes, converts double quotes (") within values to two double quotes ("").</li>
   * <li>When CSV type allows line breaks and values contain line break codes, unifies line break codes (CRLF/CR) to LF.</li>
   * <li>When CSV type does not allow line breaks (other than line break types) and values contain line break codes, converts line break codes (CRLF/CR/LF) to half-width spaces.</li>
   * <li>String lists, nested maps, multi-row lists, and array lists are not output.</li>
   * </ul>
   *
   * @param csvType CSV type
   * @return the CSV string
   */
  public String createCsv(final CsvType csvType) {
    if (csvType == CsvType.NO_DQ) {
      return createCsvNoDq();
    } else if (csvType == CsvType.DQ_ALL) {
      return createCsvAllDq(false);
    } else if (csvType == CsvType.DQ_ALL_LF) {
      return createCsvAllDq(true);
    } else if (csvType == CsvType.DQ_STD) {
      return createCsvStdDq(false);
    } else {
      return createCsvStdDq(true);
    }
  }

  /**
   * Creates CSV string.
   *
   * @return the CSV string
   */
  private String createCsvNoDq() {
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
   * Creates CSV string with double quotes.
   *
   * @param hasLf flag indicating line breaks are allowed
   * @return the CSV string
   */
  private String createCsvAllDq(final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
      sb.append('"').append(ValUtil.convCsvDqWrap(val, hasLf)).append('"');
      sb.append(',');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Creates CSV string with double quotes compliant with CSV specification.
   *
   * @param hasLf flag indicating line breaks are allowed
   * @return the CSV string
   */
  private String createCsvStdDq(final boolean hasLf) {
    final StringBuilder sb = new StringBuilder();
    for (final Entry<String, String> ent : super.getValMap().entrySet()) {
      final String val = ValUtil.nvl(ent.getValue());
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
   * Creates URL parameter (the part after ? in URL).
   *
   * @return the URL-encoded GET parameter
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
   * Creates JSON string.
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
   * Stores CSV values.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>Items with blank keys in the key array are not stored. (Specify blank keys for columns to skip.)</li>
   * <li>When the number of CSV items exceeds the number of keys, excess items are not stored.</li>
   * <li>When the number of keys exceeds the number of CSV items, the values for those keys are always blank.</li>
   * <li>For CSV with double quotes, two consecutive double quotes ("") within values are converted to a single double quote (") and stored.</li>
   * </ul>
   *
   * @param keys key array
   * @param csv CSV string
   * @param csvType CSV type
   * @return the number of stored items
   */
  public int putAllByCsv(final String[] keys, final String csv, final CsvType csvType) {
    if (csvType == CsvType.NO_DQ) {
      return putAllByCsvNoDq(keys, csv);
    } else {
      return putAllByCsvDq(keys, csv);
    }
  }

  /**
   * Stores CSV values.
   *
   * @param keys key array
   * @param csv CSV string
   * @return the number of stored items
   */
  int putAllByCsvNoDq(final String[] keys, final String csv) {
    // Maximum index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : new SimpleSeparateParser(csv, ",")) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Terminate when CSV columns exceed key columns
        break;
      }

      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // Skip if key name is blank (treat as unnecessary item)
        continue;
      }

      // Store value
      count++;
      put(key, value);
    }
    return count;
  }

  /**
   * Stores CSV values with double quotes.
   *
   * @see #putAllByCsv(String[], String)
   * @param keys key array
   * @param csv CSV string
   * @return the number of stored items
   */
  int putAllByCsvDq(final String[] keys, final String csv) {
    return putAllByCsvDq(keys, csv, new CsvDqParser(csv));
  }

  /**
   * Stores CSV values with double quotes (performance optimization).
   *
   * @see #putAllByCsv(String[], String)
   * @param keys key array
   * @param csv CSV string
   * @param dqParser CSV parser
   * @return the number of stored items
   */
  int putAllByCsvDq(final String[] keys, final String csv, final CsvDqParser dqParser) {
    // Maximum key index
    final int keyMaxIdx = keys.length - 1;

    int keyIdx = -1;
    int count = 0;

    for (final String value : dqParser) {
      keyIdx++;
      if (keyMaxIdx < keyIdx) {
        // Terminate when CSV columns exceed key columns
        break;
      }
      // Key name
      final String key = keys[keyIdx];
      if (ValUtil.isBlank(key)) {
        // Skip if key name is blank (treat as unnecessary item)
        continue;
      }

      // Store value
      count++;
      put(key, value.replace("\"\"", "\""));
    }
    return count;
  }

  /**
   * Stores URL parameter values (the part after ? in URL).<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * </ul>
   *
   * @param url the entire URL or URL parameter
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
        // Array keys result in error
        throw new RuntimeException("Keys representing arrays cannot be used. " + LogUtil.joinKeyVal("key", key));
      }

      // Store value
      count++;
      put(key, val);
    }
    return count;

  }

  /**
   * Stores JSON values.
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
   * Returns string for logging.
   */
  public final String toString() {
    return createLogString();
  }

}
