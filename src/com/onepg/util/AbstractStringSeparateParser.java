package com.onepg.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * String separation processing base class.<br>
 * <ul>
 * <li>Provides functionality to split a string according to specified rules and iterate over each item.</li>
 * <li>Subclasses implement the separation rules.</li>
 * </ul>
 * @hidden
 */
abstract class AbstractStringSeparateParser implements Iterable<String> {

  /** Target string. */
  private final String value;
  /** List of start and end positions. */
  private List<int[]> beginEnds = null;


  /**
   * Searches for separation start and end positions.<br>
   * <ul>
   * <li>Returns the start position of one separated item at array index 0 and the end position at index 1.</li>
   * <li>Returns a list assuming multiple items.</li>
   * </ul>
   *
   * @param value target string
   * @return list of start and end positions
   */
  protected abstract List<int[]> findBeginEnds(final String value);

  /**
   * Constructor.
   *
   * @param value target string
   */
  AbstractStringSeparateParser(final String value) {
    this(value, false);
  }

  /**
   * Constructor.
   * <ul>
   * <li>When subclass constructor has processing such as member variable initialization after <code>super()</code> call, need to delay initialization and perform it when <code>iterator()</code> is executed.</li>
   * </ul>
   * <pre>[Delayed constructor example]<code>
   *   SubParser(final String line, final String sep) {
   *     super(line, true);
   *     this.sep = sep;
   *   }</code></pre>
   * 
   * @param value target string
   * @param delayInit delayed initialization flag; <code>true</code> delays initialization
   */
  AbstractStringSeparateParser(final String value, final boolean delayInit) {
    this.value = value; // Design allows null, so use as is
    if (!delayInit) {
      init();
    }
  }

  @Override
  public Iterator<String> iterator() {
    init();
    return new StringSeparateIterator();
  }

  /**
   * Initialization processing.<br>
   * <ul>
   * <li>Calls search for separation start and end positions.</li>
   * </ul>
   */
  private void init() {
    if (ValUtil.isNull(this.beginEnds)) {
      if (ValUtil.isNull(this.value)) {
        this.beginEnds = new ArrayList<>();
      } else {
        this.beginEnds = findBeginEnds(this.value);
      }
    }
  }

  /**
   * Checks if escaped.<br>
   * <ul>
   * <li>Checks if the character at the specified position is escaped (escaped if preceded by an odd number of backslashes)</li>
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
   * Checks if escaped (char[] version).<br>
   * <ul>
   * <li>Checks if the character at the specified position is escaped (escaped if preceded by an odd number of backslashes)</li>
   * </ul>
   *
   * @param target target char array to check
   * @param targetPos target position
   * @return <code>true</code> if escaped
   */
  protected static boolean isPreEsc(final char[] target, final int targetPos) {
    if (targetPos <= 0 || ValUtil.isNull(target) || targetPos >= target.length) {
      return false;
    }

    int bsCount = 0;
    for (int i = targetPos - 1; i >= 0 && target[i] == '\\'; i--) {
      bsCount++;
    }
    // Returns true for odd number
    return (bsCount % 2 == 1);
  }

  /**
   * Stores inside to list when both ends are double quotes.
   *
   * @param retList  result list
   * @param beginPos start position
   * @param endPos   end position
   * @param value    original string
   */
  protected void trimDqPosAdd(final List<int[]> retList, final int beginPos, final int endPos,
      final String value) {
    if (beginPos < value.length() && endPos > 0 && beginPos + 1 < endPos
        && value.charAt(beginPos) == '"'
        && value.charAt(endPos - 1) == '"') {
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
     * Checks if next string exists.
     *
     * @return <code>true</code> if next string exists
     */
    @Override
    public boolean hasNext() {
      return this.beginEndsIndex < this.maxIndex;
    }

    /**
     * Gets next string.
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
