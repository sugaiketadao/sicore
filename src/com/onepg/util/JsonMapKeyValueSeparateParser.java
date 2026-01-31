package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JSON map key-value separate parser.
 * @hidden
 */
final class JsonMapKeyValueSeparateParser extends AbstractStringSeparateParser {

  /**
   * Gets the key-value array.
   *
   * @param jsonItem JSON item string
   * @return character array {key, value}. Returns <code>null</code> if the value is invalid (may contain <code>null</code>)
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
   * @param jsonItem JSON item string
   */
  private JsonMapKeyValueSeparateParser(final String jsonItem) {
    super(jsonItem);
  }

  /**
   * Searches for separation start and end positions.<br>
   * <ul>
   * <li>Separates at colon locations not enclosed in double quotations (without escape).</li>
   * <li>Returns only the first colon location.</li>
   * </ul>
   *
   * @param value target string
   * @return list of start and end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // If blank
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
        // Only if not escaped
        // Toggles inside double quotations
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignores inside double quotations
        continue;
      }

      if (c == ':') {
        // If colon, stores it as the key end position
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next start position
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
