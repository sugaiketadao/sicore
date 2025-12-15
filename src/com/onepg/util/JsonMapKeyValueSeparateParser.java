package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JSON map key-value separation parser.
 * @hidden
 */
final class JsonMapKeyValueSeparateParser extends AbstractStringSeparateParser {

  /**
   * Returns the key-value array.
   *
   * @param jsonItem the JSON item string
   * @return the string array {key, value}; returns <code>null</code> for invalid values (nullable)
   */
  static String[] getKeyValue(final String jsonItem) {
    // Separates key and value by colon
    final Iterator<String> keyValIte = (new JsonMapKeyValueSeparateParser(jsonItem)).iterator();
    if (!keyValIte.hasNext()) {
      // Invalid value
      return null;
    }
    final String key = keyValIte.next();
    if (ValUtil.isBlank(key)) {
      // Invalid value
      return null;
    }
    if (!keyValIte.hasNext()) {
      // Invalid value
      return null;
    }
    final String val = keyValIte.next();
    return new String[] {key, val};
  }

  /**
   * Constructor.
   *
   * @param jsonItem the JSON item string
   */
  private JsonMapKeyValueSeparateParser(final String jsonItem) {
    super(jsonItem);
  }

  /**
   * Finds begin-end positions for separation.
   * <ul>
   * <li>Separates at colon positions that are not enclosed in double quotes (unescaped).</li>
   * <li>Returns only the first colon position.</li>
   * </ul>
   *
   * @param value the target string
   * @return the list of begin-end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // Empty case
      return idxs;
    }

    int beginPos = 0;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;
    boolean readingValue = false;

    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);

      if (!notBlank && c != ' ' && c != ':') {
        notBlank = true;
        beginPos = i;
        endPos = i + 1;
      }
      if (readingValue) {
        if (notBlank && c != ' ') {
          endPos = i + 1;
        }
        continue;
      }
      if (notBlank && c != ' ' && c != ':') {
        endPos = i + 1;
      }

      if (c == '"') {
        if (isPreEsc(value, i)) {
          continue;
        }
        // Only when not escaped
        // Toggle within double quotes
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignores within double quotes
        continue;
      }

      if (c == ':') {
        // Stores as key end position at colon
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next begin position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
        readingValue = true;
      }
    }
    trimDqPosAdd(idxs, beginPos, endPos, value);
    return idxs;
  }

}
