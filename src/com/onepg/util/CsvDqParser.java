package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV (with double quotes) parser.
 * @hidden
 */
public final class CsvDqParser extends AbstractStringSeparateParser {

  /**
   * Constructor.
   *
   * @param csv the CSV string
   */
  public CsvDqParser(final String csv) {
    super(csv);
  }

  /**
   * Searches for begin-end positions for separation.<br>
   * <ul>
   * <li>Separates at comma positions not enclosed in (unescaped) double quotes.</li>
   * <li>Commas inside double quotes are not treated as delimiters.</li>
   * <li>Escaped double quotes (backslash-double quote) are treated as characters.</li>
   * <li>Leading and trailing whitespace are removed.</li>
   * </ul>
   *
   * @param value the target string
   * @return the begin-end position list
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // 空の場合
      return idxs;
    }

    int beginPos = 0;
    int endPos = beginPos;

    boolean notBlank = false;
    boolean inDq = false;

    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);

      if (!notBlank && c != ' ' && c != ',') {
        notBlank = true;
        beginPos = i;
        endPos = i + 1;
      }
      if (notBlank && c != ' ' && c != ',') {
        endPos = i + 1;
      }

      if (c == '"') {
        if (isPreEsc(value, i)) {
          continue;
        }
        // Toggle inside double quotes only when not escaped
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignore inside double quotes
        continue;
      }
      if (c == ',') {
        // Add begin-end position at comma
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next begin position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    // Add the last begin-end position
    trimDqPosAdd(idxs, beginPos, endPos, value);
    return idxs;
  }

}
