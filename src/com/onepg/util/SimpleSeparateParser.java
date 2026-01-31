package com.onepg.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple separate parser.<br>
 * <ul>
 * <li>Separates strings by the specified delimiter</li>
 * <li>Supports variable-length delimiters</li>
 * <li>Consecutive delimiters are treated as empty strings</li>
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
   * @param line String
   * @param sep Separator string
   */
  public SimpleSeparateParser(final String line, final String sep) {
    super(line);
    this.sep = sep;
  }

  /**
   * Searches for separation start and end points.
   *
   * @param value Target string
   * @return Start and end point list
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

    // Adds the last start and end point
    idxs.add(new int[] {beginPos, value.length()});
    return idxs;
  }
}
