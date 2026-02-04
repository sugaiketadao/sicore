package com.onepg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import com.onepg.util.ValUtil.CharSet;


/**
 * Text reader class.<br>
 * <ul>
 * <li>Wrapper class for file reader <code>BufferedReader</code>.</li>
 * <li>Declares in try clause (try-with-resources statement).</li>
 * </ul>
 * <pre>[Example]
 * <code>try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
 *   // Skips header row
 *   tr.skip();
 *   for (final String line : tr) {
 *     : Omitted
 *   }
 * }</code>
 * </pre>
 */
public final class TxtReader implements Iterable<String>, AutoCloseable {

  /** Buffered reader */
  private final BufferedReader br;
  /** File path. */
  private final String filePath;
  /** Read row */
  private String nextLine = null;

  /** Read row count. */
  private int readedCount = 0;
  /** Last row read flag. */
  private boolean readedEndRowFlag = false;
  /** <code>true</code> if closed */
  private boolean isClosed = false;

  /**
   * Constructor.
   *
   * @param filePath File path
   * @param charSet  Character set
   */
  public TxtReader(final String filePath, final CharSet charSet) {
    this.filePath = FileUtil.convAbsolutePath(filePath);

    final File targetFile = new File(this.filePath);
    if (!targetFile.exists()) {
      throw new RuntimeException("File does not exist. " + LogUtil.joinKeyVal("path", this.filePath));
    }

    try {
      this.br = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), charSet.toString()));
    } catch (UnsupportedEncodingException | FileNotFoundException e) {
      throw new RuntimeException("An exception error occurred while reading file. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * Creates iterator.
   *
   * @return Read row iterator
   */
  @Override
  public Iterator<String> iterator() {
    return new TxtReadIterator();
  }

  /**
   * Closes file.
   */
  @Override
  public void close() {
    if (this.isClosed) {
      return;
    }
    this.isClosed = true;
    try {
      this.br.close();
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while closing file. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * Gets read row count.<br>
   * <ul>
   * <li>Returns the count read by the iterator.</li>
   * </ul>
   *
   * @return Read row count
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * Checks if last row has been read.
   *
   * @return <code>true</code> if last row has been read
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * Skips one row.
   * 
   * @see #skip(int)
   * @return <code>false</code> if there are insufficient rows
   */
  public boolean skip() {
    return skip(1);
  }

  /**
   * Skips rows.<br>
   * <ul>
   * <li>Skips rows such as header rows.</li>
   * <li>Does not result in an error even if the skip row count is greater than the file rows, and the return value becomes <code>false</code>.</li>
   * <li>Read row count is not counted up.</li>
   * </ul>
   *
   * @param count Skip row count
   * @return <code>false</code> if there are insufficient rows
   */
  public boolean skip(final int count) {
    if (this.isClosed) {
      return false;
    }
    if (count <= 0) {
      return true;
    }
    
    try {
      for (int c = 1; c <= count; c++) {        
        // Skips reading one row
        final String line = this.br.readLine();
        if (ValUtil.isNull(line)) {
          // Last row read ON
          readedEndRowFlag = true;
          // Closes file
          close();
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while skipping rows. " + LogUtil.joinKeyVal("path", this.filePath) + LogUtil.joinKeyVal("skipCount", String.valueOf(count)), e);
    }
  }
  
  /**
   * Gets first row.<br>
   * <ul>
   * <li>Gets the first row of the file.</li>
   * <li>Read row count is counted up after retrieval.</li>
   * <li>Returns <code>null</code> in the following cases
   *   <ul><li>File is already closed.</li>
   *       <li>File is empty (zero rows), or has already reached the last row.</li></ul>
   * <li>An exception is thrown if any rows have already been read.</li>
   * </ul>
   *
   * @return First row string
   */
  public String getFirstLine() {
    if (this.isClosed) {
      return null;
    }
    if (readedCount > 0) {
      throw new RuntimeException("First line can only be read when no lines have been read yet. " + LogUtil.joinKeyVal("path", this.filePath) + LogUtil.joinKeyVal("readedCount", String.valueOf(readedCount)));
    }
    try {
      final String line = this.br.readLine();
      if (ValUtil.isNull(line)) {
        // Last row read ON
        readedEndRowFlag = true;
        // Closes file
        close();
        return null;
      }
      // Counts up read row count
      readedCount++;
      return line;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while reading first line. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * Read row iterator class.
   */
  public final class TxtReadIterator implements Iterator<String> {

    /** Has next row flag. */
    private boolean hasNextRow = false;
    /** Next row checked flag. */
    private boolean hasNextChecked = false;

    /**
     * Constructor.
     */
    private TxtReadIterator() {
        super();
    }

    /**
     * Checks next row.<br>
     * <ul>
     * <li>Closes file reader if next row does not exist in case try clause was not used.</li>
     * <li>Does not recheck on consecutive hasNext() calls.</li>
     * </ul>
     *
     * @return <code>true</code> if next row exists
     */
    @Override
    public boolean hasNext() {
      // Does not recheck if already checked
      if (hasNextChecked) {
        return this.hasNextRow;
      }

      // Checks next row existence
      try {
        nextLine = br.readLine();
        this.hasNextRow = !ValUtil.isNull(nextLine);
        this.hasNextChecked = true; // Confirmation complete flag
      } catch (IOException e) {
        throw new RuntimeException("An exception error occurred while checking for next row. " + LogUtil.joinKeyVal("readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
        // Last row read ON
        readedEndRowFlag = true;
        // Closes file
        close();
      }

      return this.hasNextRow;
    }

    /**
     * Gets next row.
     *
     * @return Row
     */
    @Override
    public String next() {
      if (!hasNext()) {
        throw new RuntimeException("Next row does not exist. " + LogUtil.joinKeyVal( "readedCount", String.valueOf(readedCount)));
      }

      final String line = nextLine;
      nextLine = null;
      // Counts up read row count
      readedCount++;

      // Recheck is necessary
      this.hasNextChecked = false;
      return line;
    }
  }
}
