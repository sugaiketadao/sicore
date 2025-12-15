package com.onepg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JSON map separation parser.
 * @hidden
 */
final class JsonMapSeparateParser extends AbstractStringSeparateParser {

  /** JSON map pattern. */
  static final Pattern JSON_MAP_PATTERN = Pattern.compile("^\\s*\\{\\s*.*\\s*\\}\\s*$");
  /** Blank JSON map pattern. */
  static final Pattern BLANK_JSON_MAP_PATTERN = Pattern.compile("^\\s*\\{\\s*\\}\\s*$");

  /**
   * Constructor.
   *
   * @param json the JSON string
   */
  JsonMapSeparateParser(final String json) {
    super(json);
  }

  /**
   * Finds begin-end positions for separation.
   * <ul>
   * <li>Separates at comma positions that are not enclosed in double quotes (unescaped), square brackets, or curly braces.</li>
   * </ul>
   *
   * @param value the target string
   * @return the list of begin-end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value) || BLANK_JSON_MAP_PATTERN.matcher(value).find()) {
      // Empty case
      return idxs;
    }

    if (!JSON_MAP_PATTERN.matcher(value).find()) {
      throw new RuntimeException("Must be enclosed in curly braces. " + LogUtil.joinKeyVal("json", value));
    }

    // Gets positions of outer curly braces
    final int outerBegin = value.indexOf("{") + 1;
    final int outerEnd = value.lastIndexOf("}") - 1;

    // First begin position
    int beginPos = outerBegin;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;
    int nestAryLvl = 0;
    int nestMapLvl = 0;

    int i = outerBegin;

    // Performance optimization (optimized with 1000 characters as boundary)
    final char[] valChars;
    final boolean useCharAry = (value.length() > 1000);
    if (useCharAry) {
      // Uses char[] array
      valChars = value.toCharArray();
    } else {
      // Uses charAt()
      valChars = null;
    }

    for (; i <= outerEnd; i++) {
      final char c;
      if (useCharAry) {
        c = valChars[i];
      } else {
        c = value.charAt(i);
      }
      
      if (c != ' ' && c != ',') {
        // Determines begin-end positions excluding zero-byte blanks
        if (!notBlank) {
          notBlank = true;
          beginPos = i;
        }
        endPos = i + 1;
      }

      if (c == '"') {
        if (useCharAry) {
          if (isPreEsc(valChars, i)) {
            continue;
          }
        } else {
          if (isPreEsc(value, i)) {
            continue;
          }
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

      if (c == '[') {
        // Opening square bracket
        nestAryLvl++;
        continue;
      }
      if (c == ']' && nestAryLvl > 0) {
        // Closing square bracket
        nestAryLvl--;
        continue;
      }
      if (c == '{') {
        // Opening curly brace
        nestMapLvl++;
        continue;
      }
      if (c == '}' && nestMapLvl > 0) {
        // Closing curly brace
        nestMapLvl--;
        continue;
      }
      if (nestAryLvl > 0 || nestMapLvl > 0) {
        // Ignores within square brackets or curly braces (until brackets are closed)
        continue;
      }

      if (c == ',') {
        // Adds begin-end positions at comma
        idxs.add(new int[] {beginPos, endPos});
        // Next begin position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    if (notBlank) {
      // Adds the last begin-end positions
      idxs.add(new int[] {beginPos, endPos});
    }
    return idxs;
  }
}
