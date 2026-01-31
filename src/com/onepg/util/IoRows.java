package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Multiple rows list.<br>
 * <ul>
 * <li>Holds multiple map data.</li>
 * </ul>
 */
public final class IoRows extends ArrayList<IoItems> {

  /** Start row number. */
  private int beginRowNo = -1;
  /** End row number. */
  private int endRowNo = -1;
  /** Limit exceeded flag. */
  private boolean limitOverFlag = false;

  /**
   * Constructor.
   */
  public IoRows() {
    super();
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since the content is deep-copied, the reference with the source list is disconnected.</li>
   * </ul>
   *
   * @param srcList source list
   */
  public IoRows(final Collection<? extends Map<? extends String, ? extends String>> srcList) {
    if (ValUtil.isNull(srcList)) {
      throw new RuntimeException("Source list is required. ");
    }
    for (final Map<? extends String, ? extends String> row : srcList) {
      if (ValUtil.isNull(row)) {
        add(null);
        continue;
      }
      add(new IoItems(row));
    }

    if (srcList instanceof IoRows) {
      // If the source map is this class, copy class variable values
      final IoRows tlist = (IoRows) srcList;
      setBeginRowNo(tlist.getBeginRowNo());
      setEndRowNo(tlist.getEndRowNo());
      setLimitOver(tlist.isLimitOver());
    }
  }

  /**
   * Constructor.
   */
  public IoRows(final int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Gets the start row number.
   *
   * @return start row number
   */
  public int getBeginRowNo() {
    return beginRowNo;
  }

  /**
   * Stores the start row number.
   *
   * @param beginRowNo start row number
   */
  public void setBeginRowNo(final int beginRowNo) {
    this.beginRowNo = beginRowNo;
  }

  /**
   * Gets the end row number.
   *
   * @return end row number
   */
  public int getEndRowNo() {
    return endRowNo;
  }

  /**
   * Stores the end row number.
   *
   * @param endRowNo end row number
   */
  public void setEndRowNo(final int endRowNo) {
    this.endRowNo = endRowNo;
  }

  /**
   * Checks whether the limit is exceeded.
   *
   * @return <code>true</code> if the limit is exceeded
   */
  public boolean isLimitOver() {
    return limitOverFlag;
  }

  /**
   * Stores the limit exceeded flag.
   *
   * @param limitOver <code>true</code> if the limit is exceeded
   */
  public void setLimitOver(final boolean limitOver) {
    this.limitOverFlag = limitOver;
  }
}
