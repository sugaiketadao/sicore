package com.onepg.util;

import java.util.Iterator;

import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.ValUtil.CharSet;


/**
 * CSV reader class.<br>
 * <ul>
 * <li>Wrapper class for text reader <code>TxtReader</code>.</li>
 * <li>Declare in try clause (try-with-resources statement).</li>
 * <li>Provides an iterator that returns each row of CSV as <code>IoItems</code>.</li>
 * <li>Uses the first line of the file as column names and uses them as keys for <code>IoItems</code>.</li>
 * <li>When the first line of the file does not contain column names, the keys for <code>IoItems</code> must be passed to the constructor.</li>
 * <li>Items with blank keys in the key array are not stored in <code>IoItems</code>. (Specify blank keys for columns to skip.)</li>
 * <li>When the number of CSV items exceeds the number of keys, excess items are not stored.</li>
 * <li>When the number of keys exceeds the number of CSV items, the values for those keys are always blank.</li>
 * <li>When CSV type includes double quotes, two consecutive double quotes ("") within values are converted to a single double quote (").</li>
 * <li>When CSV type allows line breaks and values (between double quotes) contain line break codes, unifies line break codes (CRLF/CR) to LF.</li>
 * <li>When CSV type does not allow line breaks (other than line break types) and values (between double quotes) contain line break codes, the line break locations are incorrectly recognized as column separators, resulting in insufficient columns.</li>
 * </ul>
 * <pre>[Example]
 * <code>try (final CsvReader cr = new CsvReader(filePath, ValUtil.UTF8, CsvType.DQ_ALL);) {
 *   for (final IoItems items : cr) {
 *     : omitted
 *   }
 * }</code>
 * </pre>
 */
public final class CsvReader implements Iterable<IoItems>, AutoCloseable {

  /** Text reader */
  private final TxtReader txtReader;
  /** Key array */
  private final String[] keys;
  /** CSV type */
  private final CsvType csvType;
  /** Number of rows read (excluding the header row). */
  private int readedCount = 0;

  /**
   * Constructor.<br>
   * <ul>
   * <li>Uses the first row of the file as the header row and as the key for <code>IoItems</code>.</li>
   * </ul>
   *
   * @param filePath file path
   * @param charSet  character set
   * @param csvType CSV type
   */
  public CsvReader(final String filePath, final CharSet charSet, final CsvType csvType) {
    this.txtReader = new TxtReader(filePath, charSet);
    this.csvType = csvType;
    // Get the first line of the file as keys
    final String firstLine = this.txtReader.getFirstLine();
    if (ValUtil.isNull(firstLine)) {
      this.keys = new String[0];
      return;
    }
    this.keys = ValUtil.splitCsv(firstLine, csvType);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Uses the key array argument and reads from the first line of the file as CSV data.</li>
   * </ul>
   *
   * @param filePath file path
   * @param charSet  character set
   * @param keys key array
   * @param csvType CSV type
   */
  public CsvReader(final String filePath, final CharSet charSet, final String[] keys, final CsvType csvType) {
    this.txtReader = new TxtReader(filePath, charSet);
    this.csvType = csvType;
    if (ValUtil.isEmpty(keys)) {
      throw new RuntimeException("Keys array is empty or null. " + LogUtil.joinKeyVal("path", filePath));
    }
    this.keys = keys;
  }

  /**
   * Creates iterator.
   *
   * @return the CSV row iterator
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new CsvReadIterator();
  }

  /**
   * Closes file.
   */
  @Override
  public void close() {
    this.txtReader.close();
  }

  /**
   * Returns the number of rows read (excluding the header row).<br>
   * <ul>
   * <li>Returns the number of rows read by the iterator.</li>
   * <li>If the first row of the file is used as the key (header row), it is not counted.</li>
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
   * CSV read row iterator class.
   */
  public final class CsvReadIterator implements Iterator<IoItems> {

    /** TxtReader iterator */
    private final Iterator<String> txtIterator;

    /**
     * Constructor.
     */
    private CsvReadIterator() {
      super();
      this.txtIterator = txtReader.iterator();
    }

    /**
     * Checks if next row exists.
     *
     * @return <code>true</code> if next row exists
     */
    @Override
    public boolean hasNext() {
      if (ValUtil.isEmpty(keys)) {
        // Treat as no next row when key array is empty (txtIterator.hasNext() should also be false, but just in case)
        return false;
      }
      return this.txtIterator.hasNext();
    }

    /**
     * Gets next row.<br>
     * <ul>
     * <li>Stores CSV row in <code>IoItems</code> and returns it.</li>
     * </ul>
     *
     * @return the <code>IoItems</code> of CSV row
     */
    @Override
    public IoItems next() {
      final String line = this.txtIterator.next();
      // Increment the number of rows read
      readedCount++;
      
      if (csvType == CsvType.NO_DQ) {
        // No double quotes
        final IoItems items = new IoItems();
        items.putAllByCsvNoDq(keys, line);
        return items;
      }
      
      if (csvType == CsvType.DQ_ALL || csvType == CsvType.DQ_STD) {
        // With double quotes, no line breaks
        final IoItems items = new IoItems();
        items.putAllByCsvDq(keys, line);
        return items;
      }

      // With double quotes and line breaks
      String mergedLine = line;
      CsvDqParser dqParser = new CsvDqParser(mergedLine);
      while (dqParser.isUnclosedDq() && this.txtIterator.hasNext()) {
        // Concatenate next line if double quotes are not closed
        final String nextLine = this.txtIterator.next();
        mergedLine = mergedLine + ValUtil.LF + nextLine;
        dqParser = new CsvDqParser(mergedLine);
      }
      final IoItems items = new IoItems();
      items.putAllByCsvDq(keys, mergedLine, dqParser);
      return items;
    }
  }
}
