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

 /** Array item names. */
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
   * <li>The reference to the source list is disconnected because the content is deep copied.</li>
   * <li></li>
   * </ul>
   *
   * @param srcList source list
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
      // Although the <code>ArrayList</code> constructor is a shallow copy, since the content is an immutable object (<code>String</code>), it effectively becomes a deep copy.
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
