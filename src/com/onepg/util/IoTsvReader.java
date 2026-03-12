package com.onepg.util;

import java.util.Iterator;

import com.onepg.util.ValUtil.CharSet;


/**
 * TSV reader class for data I/O.<br>
 * <ul>
 * <li>Wrapper class for the text reader <code>TxtReader</code>.</li>
 * <li>Declare in a try clause (try-with-resources statement).</li>
 * <li>Provides an iterator that returns each TSV row as <code>IoItems</code>.</li>
 * <li>Uses the first row of the file as column names and as keys for <code>IoItems</code>.</li>
 * <li>Key names must be valid as <code>IoItems</code> object keys. (key rule of <code>AbstractIoTypeMap</code>)</li>
 * <li>Assumes the file encoding is UTF-8 and the line separator is LF.</li>
 * <li>Assumes that newline characters (CRLF, CR, LF) and tab characters within values are escaped.</li>
 * </ul>
 * <pre>[Example]
 * <code>try (final IoTsvReader tr = new IoTsvReader(filePath);) {
 *   for (final IoItems items : tr) {
 *     : (omitted)
 *   }
 * }</code>
 * </pre>
 */
public final class IoTsvReader implements Iterable<IoItems>, AutoCloseable {

  /** Text reader */
  private final TxtReader txtReader;
  /** Key array */
  private final String[] keys;
  /** Number of rows read (excluding the header row). */
  private int readedCount = 0;

  /**
   * Constructor.<br>
   * <ul>
   * <li>Uses the first row of the file as the header row and as keys for <code>IoItems</code>.</li>
   * </ul>
   *
   * @param filePath file path
   */
  public IoTsvReader(final String filePath) {
    this.txtReader = new TxtReader(filePath, CharSet.UTF8);
    // Retrieves the first row of the file as keys
    final String firstLine = this.txtReader.getFirstLine();
    if (ValUtil.isNull(firstLine)) {
      this.keys = new String[0];
      return;
    }
    this.keys = ValUtil.split(firstLine, ValUtil.TAB);
  }

  /**
   * Creates an iterator.
   *
   * @return the TSV row iterator
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new TsvReadIterator();
  }

  /**
   * Closes the file.
   */
  @Override
  public void close() {
    this.txtReader.close();
  }

  /**
   * Retrieves the key array.<br>
   * <ul>
   * <li>Returns the key array retrieved from the first row of the file.</li>
   * <li>Returns an empty array if the file has zero rows.</li>
   * </ul>
   */
  public String[] getKeys() {
    return this.keys;
  }

  /**
   * Retrieves the number of rows read (excluding the header row).<br>
   * <ul>
   * <li>Returns the number of rows read by the iterator.</li>
   * <li>The first row is not counted when it is used as the key (header row).</li>
   * </ul>
   *
   * @return the number of rows read
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * Checks if the last row has been read.
   *
   * @return <code>true</code> if the last row has been read
   */
  public boolean isReadedEndRow() {
    return this.txtReader.isReadedEndRow();
  }

  /**
   * TSV row read iterator class.
   */
  public final class TsvReadIterator implements Iterator<IoItems> {

    /** Iterator for TxtReader */
    private final Iterator<String> txtIterator;

    /**
     * Constructor.
     */
    private TsvReadIterator() {
      super();
      this.txtIterator = txtReader.iterator();
    }

    /**
     * Checks if the next row exists.
     *
     * @return <code>true</code> if the next row exists
     */
    @Override
    public boolean hasNext() {
      if (ValUtil.isEmpty(keys)) {
        // Treats as no next row if the key array is empty (txtIterator.hasNext() should also return false, but this is a precaution)
        return false;
      }
      return this.txtIterator.hasNext();
    }

    /**
     * Retrieves the next row.<br>
     * <ul>
     * <li>Stores the TSV row in <code>IoItems</code> and returns it.</li>
     * </ul>
     *
     * @return the <code>IoItems</code> containing the TSV row
     */
    @Override
    public IoItems next() {
      final String line = this.txtIterator.next();
      // Increments the row read count
      readedCount++;

      final IoItems items = new IoItems();
      items.putAllByIoTsv(keys, line);
      return items;
    }
  }
}
