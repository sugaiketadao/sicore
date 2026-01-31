package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * String separation processing base class.
 * @hidden
 */
abstract class AbstractStringSeparateParser implements Iterable<String> {

  /** Target string. */
  private final String value;
  /** Begin-end position list. */
  private List<int[]> beginEnds = null;


  /**
   * Searches for separation begin and end positions.<br>
   * <ul>
   * <li>Returns the begin position of one separated item at index 0 of the array and the end position at index 1.</li>
   * <li>Returns as a list assuming multiple items.</li>
   * </ul>
   *
   * @param value target string
   * @return the begin-end position list
   */
  protected abstract List<int[]> findBeginEnds(final String value);

  /**
   * Constructor.
   *
   * @param value target string
   */
  AbstractStringSeparateParser(final String value) {
    this.value = value; // nullも許可する設計なのでそのまま
  }

  @Override
  public Iterator<String> iterator() {
    // Lazy initialization for performance improvement
    if (ValUtil.isNull(this.beginEnds)) {
      if (ValUtil.isNull(value)) {
        this.beginEnds = new ArrayList<>();
      } else {
        this.beginEnds = findBeginEnds(this.value);
      }
    }
    return new StringSeparateIterator();
  }

  /**
   * Determines if a character is escaped.<br>
   * <ul>
   * <li>Determines if the character at the specified position is escaped (escaped if preceded by an odd number of backslashes).</li>
   * </ul>
   *
   * @param target target to check
   * @param targetPos target position
   * @return <code>true</code> if escaped
   */
  protected boolean isPreEsc(final String target, final int targetPos) {
    if (targetPos <= 0 || target == null || targetPos > target.length()) {
        return false;
    }
    
    int bsCount = 0;
    for (int i = targetPos - 1; i >= 0; i--) {
      if (target.charAt(i) != '\\') {
        break;
      }
      bsCount++;
    }
    return (bsCount % 2 == 1);
  }

  /**
   * Determines if a character is escaped (char[] version).<br>
   * <ul>
   * <li>Determines if the character at the specified position is escaped (escaped if preceded by an odd number of backslashes).</li>
   * </ul>
   *
   * @param target target char array to check
   * @param targetPos target position
   * @return <code>true</code> if escaped
   */
  protected static boolean isPreEsc(final char[] target, final int targetPos) {
    if (targetPos <= 0 || target == null || targetPos >= target.length) {
      return false;
    }

    int bsCount = 0;
    for (int i = targetPos - 1; i >= 0 && target[i] == '\\'; i--) {
      bsCount++;
    }

    return (bsCount & 1) == 1;
  }

  /**
   * Stores the inner part in the list if both ends are double quotations.
   *
   * @param retList  result list
   * @param beginPos begin position
   * @param endPos   end position
   * @param value    original string
   */
  protected void trimDqPosAdd(final List<int[]> retList, final int beginPos, final int endPos,
      final String value) {
    if (beginPos < value.length() && endPos > 0 && beginPos + 1 < endPos
        && "\"".equals(value.substring(beginPos, beginPos + 1))
        && "\"".equals(value.substring(endPos - 1, endPos))) {
        retList.add(new int[] {beginPos + 1, endPos - 1});
    } else {
        retList.add(new int[] {beginPos, endPos});
    }
  }

  /**
   * String separation iterator class.
   */
  private final class StringSeparateIterator implements Iterator<String> {
    /** Current position. */
    private int beginEndsIndex = 0;
    /** Maximum position. */
    private final int maxIndex;

    /**
     * Constructor.
     */
    private StringSeparateIterator() {
      this.maxIndex = beginEnds.size();
    }

    /**
     * Checks if the next string exists.
     *
     * @return <code>true</code> if the next string exists
     */
    @Override
    public boolean hasNext() {
      return this.beginEndsIndex < this.maxIndex;
    }

    /**
     * Retrieves the next string.
     *
     * @return the next string
     */
    @Override
    public String next() {
      if (!hasNext()) {
        throw new RuntimeException("No next element exists. " +
            LogUtil.joinKeyVal("currentIndex", this.beginEndsIndex, "maxIndex", this.maxIndex));
      }

      final int[] pos = beginEnds.get(this.beginEndsIndex);
      this.beginEndsIndex++;

      // Validation
      if (pos[0] < 0 || pos[1] > value.length() || pos[0] > pos[1]) {
          throw new RuntimeException("Invalid string index. " +
              LogUtil.joinKeyVal("begin", pos[0], "end", pos[1], 
                                "valueLength", value.length()));
      }

      try {
          return value.substring(pos[0], pos[1]);
      } catch (StringIndexOutOfBoundsException e) {
          throw new RuntimeException("String index out of bounds. " +
              LogUtil.joinKeyVal("begin", pos[0], "end", pos[1]), e);
      }
    }
  }

}
