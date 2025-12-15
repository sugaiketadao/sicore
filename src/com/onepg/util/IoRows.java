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

  /** Begin row number. */
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
   * <li>Deep copies the contents, so the reference to the source list is disconnected.</li>
   * </ul>
   *
   * @param srcList the source list
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
      // Copy class variable values if source map is this class
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
   * Retrieves the begin row number.
   *
   * @return the begin row number
   */
  public int getBeginRowNo() {
    return beginRowNo;
  }

  /**
   * Stores the begin row number.
   *
   * @param beginRowNo the begin row number
   */
  public void setBeginRowNo(final int beginRowNo) {
    this.beginRowNo = beginRowNo;
  }

  /**
   * Retrieves the end row number.
   *
   * @return the end row number
   */
  public int getEndRowNo() {
    return endRowNo;
  }

  /**
   * Stores the end row number.
   *
   * @param endRowNo the end row number
   */
  public void setEndRowNo(final int endRowNo) {
    this.endRowNo = endRowNo;
  }

  /**
   * Determines if limit exceeded.
   *
   * @return <code>true</code> if limit exceeded
   */
  public boolean isLimitOver() {
    return limitOverFlag;
  }

  /**
   * Stores the limit exceeded flag.
   *
   * @param limitOver <code>true</code> if limit exceeded
   */
  public void setLimitOver(final boolean limitOver) {
    this.limitOverFlag = limitOver;
  }
}
