package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV (with double quotes) parser.
 * @hidden
 */
final class CsvDqParser extends AbstractStringSeparateParser {
  
  /** 
   * Flag indicating unclosed double quotes.
   * (To prevent the value calculated in findBeginEnds() from being overwritten by initial value set during field declaration, do not set initial value here and set value only within findBeginEnds())
   */
  private boolean unclosedDq;

  /**
   * Constructor.
   *
   * @param csv CSV string
   */
  CsvDqParser(final String csv) {
    super(csv);
  }

  /**
   * Searches for separation start and end positions.<br>
   * <ul>
   * <li>Splits at comma positions not enclosed by unescaped double quotes.</li>
   * <li>Commas within double quotes are not treated as separators.</li>
   * <li>Escaped double quotes (\" ) are treated as normal characters.</li>
   * <li>Two consecutive double quotes ("") are treated as a single double quote character (").</li>
   * <li>Leading and trailing whitespace characters of each item are removed.</li>
   * <li>Also checks if double quotes are closed.</li>
   * </ul>
   *
   * @param value target string
   * @return list of start and end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    this.unclosedDq = false;
    final List<int[]> idxs = new ArrayList<>();
    if (ValUtil.isBlank(value)) {
      // When empty
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
        // Treat as escape sequence (""→") when next character after double quote is also double quote
        if (i + 1 < value.length() && value.charAt(i + 1) == '"') {
          if (inDq) {
            // Valid only within double quotes
            // Also skip next double quote
            i++; 
            continue;
          }
        }
        // Only when not escaped
        // Toggle within double quotes
        inDq = !inDq;
        continue;
      }
      if (inDq) {
        // Ignore within double quotes
        continue;
      }
      if (c == ',') {
        // Add start and end positions when comma
        trimDqPosAdd(idxs, beginPos, endPos, value);
        // Next start position
        beginPos = i + 1;
        endPos = beginPos;
        notBlank = false;
      }
    }

    // Add last start and end positions
    trimDqPosAdd(idxs, beginPos, endPos, value);

    // Turn on flag when double quotes are not closed
    if (inDq) {
      this.unclosedDq = true;
    }
    return idxs;
  }

  /**
   * Gets flag indicating unclosed double quotes.
   *
   * @return <code>true</code> if double quotes are not closed
   */  
  boolean isUnclosedDq() {
    return this.unclosedDq; 
  }
}
