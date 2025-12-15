package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple separation parser.<br>
 * <ul>
 * <li>Separates a string by the specified delimiter.</li>
 * <li>Supports variable-length delimiters.</li>
 * <li>Consecutive delimiters are treated as empty strings.</li>
 * </ul>
 * <pre>[Example]
 * <code>for (final String item : (new SimpleSeparateParser("a,b,c", ","))) {
 *   System.out.println(item);
 * }</code></pre>
 * @hidden
 */
public final class SimpleSeparateParser extends AbstractStringSeparateParser {

  /** Separator string. */
  private final String sep;

  /**
   * Constructor.
   *
   * @param line the string
   * @param sep the separator string
   */
  public SimpleSeparateParser(final String line, final String sep) {
    super(line);
    this.sep = sep;
  }

  /**
   * Finds begin-end positions for separation.
   *
   * @param value the target string
   * @return the list of begin-end positions
   */
  @Override
  protected List<int[]> findBeginEnds(final String value) {
    final List<int[]> idxs = new ArrayList<>();
    final int sepLen = this.sep.length();
    int beginPos = 0;
    int endPos = beginPos;

    while ((endPos = value.indexOf(this.sep, beginPos)) != -1) {
      // Up to before the delimiter
      idxs.add(new int[] {beginPos, endPos});
      // Right after the delimiter
      beginPos = endPos + sepLen;
    }

    // Adds the last begin-end position
    idxs.add(new int[] {beginPos, value.length()});
    return idxs;
  }
}
