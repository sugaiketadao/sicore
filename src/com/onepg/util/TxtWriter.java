package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Text writer class.<br>
 * <ul>
 * <li>A print writer wrapper class dedicated to text files.</li>
 * <li>Allows character set and line separator specification.</li>
 * <li>Writing <code>null</code> outputs only a line break.</li>
 * </ul>
 */
public class TxtWriter implements AutoCloseable {

  /** Print writer. */
  private final CustomPrintWriter pw;
  /** File path. */
  private final String filePath;
  /** Number of output rows. */
  private long lineCount = 0;

  /**
   * Constructor.
   *
   * @param filePath the file path
   * @param lineSep the line separator
   * @param charSet the character set
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet) {
    this(filePath, lineSep, charSet, false, false, false);
  }
  
  /**
   * Constructor.
   *
   * @param filePath the file path
   * @param lineSep the line separator
   * @param charSet the character set
   * @param withBom <code>true</code> if with BOM
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final boolean withBom) {
    this(filePath, lineSep, charSet, withBom, false, false);
  }

  /**
   * Constructor.
   *
   * @param filePath the file path
   * @param lineSep the line separator
   * @param charSet the character set
   * @param withBom <code>true</code> if with BOM
   * @param canAppend <code>true</code> if appending is allowed
   * @param lineFlush <code>true</code> if flushing on line break
   */
  public TxtWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final boolean withBom, 
    final boolean canAppend, final boolean lineFlush) {
    this.filePath = FileUtil.convAbsolutePath(filePath);

    // Error if file already exists and not appending
    if (!canAppend && FileUtil.exists(this.filePath)) {
      throw new RuntimeException("File already exists. " + LogUtil.joinKeyVal("path", this.filePath));
    }
    // Error if parent directory does not exist
    if (!FileUtil.existsParent(this.filePath)) {
      throw new RuntimeException("File creation target directory does not exist. " + LogUtil.joinKeyVal("path", this.filePath));
    }

    try {
      final FileOutputStream fos = new FileOutputStream(this.filePath, canAppend);
      if (withBom && CharSet.UTF8 == charSet) {
        // BOM
        fos.write(0xef);
        fos.write(0xbb);
        fos.write(0xbf);
      }
      final OutputStreamWriter os = new OutputStreamWriter(fos, charSet.toString());
      final BufferedWriter bw = new BufferedWriter(os);
      final CustomPrintWriter pw = new CustomPrintWriter(bw, lineFlush, lineSep);
      this.pw = pw;
    } catch (IOException e) {
      throw new RuntimeException("An exception error occurred while creating output stream. " 
          + LogUtil.joinKeyVal("path", this.filePath), e);
    }
  }

  /**
   * Closes the file.
   */
  public void close() {
    this.pw.flush();
    this.pw.close();
  }

  /**
   * Outputs a row.
   *
   * @param line the row data
   */
  public void println(final String line) {
    // Replaces null with blank (outputs only a line break)
    this.pw.println(ValUtil.nvl(line));
    this.lineCount++;
  }

  /**
   * Flushes the buffer.
   */
  public void flush() {
    this.pw.flush();
  }

  /**
   * Gets the file path.
   *
   * @return the file path
   */
  public String getFilePath() {
    return this.filePath;
  }

  /**
   * Gets the number of output rows.
   *
   * @return the number of output rows
   */
  public long getLineCount() {
    return this.lineCount;
  }

  /**
   * Converts to string.
   *
   * @return the file path
   */
  public String toString() {
    return LogUtil.joinKeyVal("path", this.filePath, "lineCount", String.valueOf(this.lineCount));
  }
}
