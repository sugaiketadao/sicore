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
 * <li>A wrapper class for file reader <code>BufferedReader</code>.</li>
 * <li>Declares in a try clause (try-with-resources statement).</li>
 * </ul>
 * <pre>[Example]
 * <code>try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
 *   // Skips header row
 *   tr.skip();
 *   for (final String line : tr) {
 *     : omitted
 *   }
 * }</code>
 * </pre>
 */
public final class TxtReader implements Iterable<String>, AutoCloseable {

  /** Buffered reader. */
  private final BufferedReader br;
  /** File path. */
  private final String filePath;
  /** The line that was read. */
  private String nextLine = null;

  /** Number of rows already read. */
  private int readedCount = 0;
  /** Flag indicating whether the last row has been read. */
  private boolean readedEndRowFlag = false;
  /** <code>true</code> if closed. */
  private boolean isClosed = false;

  /**
   * Constructor.
   *
   * @param filePath the file path
   * @param charSet  the character set
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
   * Creates an iterator.
   *
   * @return the read line iterator
   */
  @Override
  public Iterator<String> iterator() {
    return new TxtReadIterator();
  }

  /**
   * Closes the file.
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
   * Gets the number of rows already read.<br>
   * <ul>
   * <li>Returns the count read by the iterator.</li>
   * </ul>
   *
   * @return the number of rows already read
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * Determines whether the last row has been read.
   *
   * @return <code>true</code> if the last row has been read
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * Skips one row.
   * 
   * @see #skip(int)
   * @return <code>false</code> if there were insufficient rows
   */
  public boolean skip() {
    return skip(1);
  }

  /**
   * Skips rows.<br>
   * <ul>
   * <li>Skips header rows, etc.</li>
   * <li>Even if the skip count exceeds the file rows, no error occurs and returns <code>false</code>.</li>
   * <li>The read row count is not incremented.</li>
   * </ul>
   *
   * @param count the number of rows to skip
   * @return <code>false</code> if there were insufficient rows
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
        // Skips one row
        final String line = this.br.readLine();
        if (ValUtil.isNull(line)) {
          // Sets last row read flag ON
          readedEndRowFlag = true;
          // Closes the file
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
   * Gets the first line.<br>
   * <ul>
   * <li>Gets the first line of the file.</li>
   * <li>The read row count is incremented after retrieval.</li>
   * <li>Returns <code>null</code> in the following cases:
   *   <ul><li>The file is already closed.</li>
   *       <li>The file is empty (zero rows), or the last row has already been reached.</li></ul>
   * </ul>
   *
   * @return the first line string
   */
  public String getFirstLine() {
    if (this.isClosed) {
      return null;
    }
    try {
      final String line = this.br.readLine();
      if (ValUtil.isNull(line)) {
        // Sets last row read flag ON
        readedEndRowFlag = true;
        // Closes the file
        close();
        return null;
      }
      // Increments the read row count
      readedCount++;
      return line;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while reading first line. " + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * Read line iterator class.
   */
  public final class TxtReadIterator implements Iterator<String> {

    /** Flag indicating whether next row exists. */
    private boolean hasNextRow = false;
    /** Flag indicating whether next row has been checked. */
    private boolean hasNextChecked = false;

    /**
     * Constructor.
     */
    private TxtReadIterator() {
        super();
    }

    /**
     * Checks for next row.<br>
     * <ul>
     * <li>Closes the file reader if next row does not exist, in case try clause was not used.</li>
     * <li>Does not re-check on consecutive hasNext() calls.</li>
     * </ul>
     *
     * @return <code>true</code> if next row exists
     */
    @Override
    public boolean hasNext() {
      // Does not re-check if already checked
      if (hasNextChecked) {
        return this.hasNextRow;
      }

      // Checks for next row existence
      try {
        nextLine = br.readLine();
        this.hasNextRow = !ValUtil.isNull(nextLine);
        this.hasNextChecked = true; // Check completed flag
      } catch (IOException e) {
        throw new RuntimeException("An exception error occurred while checking for next row. " + LogUtil.joinKeyVal("readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
        // Sets last row read flag ON
        readedEndRowFlag = true;
        // Closes the file
        close();
      }

      return this.hasNextRow;
    }

    /**
     * Gets the next row.
     *
     * @return the row
     */
    @Override
    public String next() {
      if (!hasNext()) {
        throw new RuntimeException("Next row does not exist. " + LogUtil.joinKeyVal( "readedCount", String.valueOf(readedCount)));
      }

      final String line = nextLine;
      nextLine = null;
      // Increments the read row count
      readedCount++;

      // Needs to check again
      this.hasNextChecked = false;
      return line;
    }
  }
}
