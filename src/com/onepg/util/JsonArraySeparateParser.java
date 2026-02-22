package com.onepg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JSON array separate parser.
 * @hidden
 */
final class JsonArraySeparateParser extends AbstractStringSeparateParser {

  /** JSON array pattern. */
  static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("^\\s*\\[\\s*.*\\s*\\]\\s*$");
  /** Blank JSON array pattern. */
  static final Pattern BLANK_JSON_ARRAY_PATTERN = Pattern.compile("^\\s*\\[\\s*\\]\\s*$");

  /**
   * Constructor.
   *
   * @param json JSON string
   */
  JsonArraySeparateParser(final String json) {
    super(json);
  }

  /**
   * Searches for separation start and end positions.<br>
   * <ul>
   * <li>Separates at comma locations not enclosed in double quotations (without escape), square brackets, or curly braces.</li>
   * </ul>
   *
   * @param value target string
   * @return list of start and end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value) || BLANK_JSON_ARRAY_PATTERN.matcher(value).find()) {
      // If blank
      return idxs;
    }

    if (!JSON_ARRAY_PATTERN.matcher(value).find()) {
      throw new RuntimeException("Must be enclosed in square brackets. " + LogUtil.joinKeyVal("json", value));
    }

    // Gets the position of the outermost square brackets
    final int outerBegin = value.indexOf("[") + 1;
    final int outerEnd = value.lastIndexOf("]") - 1;

    // First start position
    int beginPos = outerBegin;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;
    int nestAryLvl = 0;
    int nestMapLvl = 0;

    int i = outerBegin;

    // Performance optimization (using 1000 characters as threshold)
    final char[] valChars;
    final boolean useCharAry = (value.length() > 1_000);
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
        // Determines start and end positions excluding zero-byte blanks
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
        // Only if not escaped
        // Toggles inside double quotations
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignores inside double quotations
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
        // Ignores inside square brackets or curly braces (until the brackets are closed)
        continue;
      }

      if (c == ',') {
        // If comma, adds the start and end positions
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next start position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    // Adds the last start and end positions
    if (notBlank) {
      trimDqPosAdd(idxs, beginPos, endPos, value);
    }
    return idxs;
  }
}
