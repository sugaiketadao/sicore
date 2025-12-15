package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Array list.<br>
 * <ul>
 * <li>Holds table-image data.</li>
 * </ul>
 */
public final class IoArrays extends ArrayList<List<String>> {

 /** Array field names. */
  // private List<String> names = new ArrayList<String>();

  /**
   * Constructor.
   */
  public IoArrays() {
    super();
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Deep copies the contents, so the reference to the source list is disconnected.</li>
   * <li></li>
   * </ul>
   *
   * @param srcList the source list
   */
  public IoArrays(final Collection<? extends Collection<? extends String>> srcList) {
    if (ValUtil.isNull(srcList)) {
      throw new RuntimeException("Source list is required. ");
    }
    for (final Collection<? extends String> row : srcList) {
      if (ValUtil.isNull(row)) {
        add(null);
        continue;
      }
      // <code>ArrayList</code> constructor performs shallow copy, but since the contents are immutable objects (<code>String</code>), it is effectively a deep copy.
      add(new ArrayList<String>(row));
    }
  }

  /**
   * Constructor.
   */
  public IoArrays(final int initialCapacity) {
    super(initialCapacity);
  }
}
