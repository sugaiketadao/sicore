package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV (with double quotations) parser.
 * @hidden
 */
public final class CsvDqParser extends AbstractStringSeparateParser {

  /**
   * Constructor.
   *
   * @param csv CSV string
   */
  public CsvDqParser(final String csv) {
    super(csv);
  }

  /**
   * Searches for separation begin and end positions.<br>
   * <ul>
   * <li>Separates at comma positions not enclosed by double quotations (without escape).</li>
   * <li>Commas inside double quotations are not treated as delimiters.</li>
   * <li>Escaped double quotations (\") are treated as characters.</li>
   * <li>Two consecutive double quotations ("") are treated as characters.</li>
   * <li>Leading and trailing whitespace characters are removed.</li>
   * </ul>
   *
   * @param value target string
   * @return the begin-end position list
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // If empty
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
        // If the next character after double quotation is also a double quotation, treat as escape sequence ("" → ")
        if (i + 1 < value.length() && value.charAt(i + 1) == '"') {
          if (inDq) {
            // Valid only inside double quotations
            // Skip the next double quotation as well
            i++; 
            continue;
          }
        }
        // Only if not escaped
        // Toggle inside double quotations
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignore inside double quotations
        continue;
      }
      if (c == ',') {
        // If comma, add begin and end positions
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next begin position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    // Add the last begin and end positions
    trimDqPosAdd(idxs, beginPos, endPos, value);
    return idxs;
  }

}
