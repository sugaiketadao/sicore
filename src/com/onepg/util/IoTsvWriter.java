package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;

/**
 * TSV writer class for data I/O.<br>
 * <ul>
 * <li>Wraps <code>TxtWriter</code> to provide functionality specialized for data I/O TSV output.</li>
 * <li>Simplifies output in TSV format.</li>
 * <li>Outputs column names as the first row of the file.</li>
 * <li>The file encoding is fixed to UTF-8 and the line separator is fixed to LF.</li>
 * <li>Escapes <code>null</code> values.</li>
 * <li>Escapes newline characters (CRLF, CR, LF) and tab characters within values.</li>
 * </ul>
 */
public final class IoTsvWriter implements AutoCloseable {

  /** Text writer. */
  private final TxtWriter txtWriter;

  /**
   * Constructor.
   *
   * @param filePath file path
   */
  public IoTsvWriter(final String filePath) {
    this.txtWriter = new TxtWriter(filePath, LineSep.LF, CharSet.UTF8);
  }

  /**
   * Closes the file.
   */
  public void close() {
    this.txtWriter.close();
  }

  /**
   * Outputs a TSV row (string array).
   *
   * @param values value array
   */
  public void println(final String[] values) {
    this.txtWriter.println(ValUtil.joinIoTsv(values));
  }

  /**
   * Outputs a TSV row (IoItems).
   *
   * @param row row data
   */
  public void println(final IoItems row) {
    this.txtWriter.println(row.createIoTsv());
  }

  /**
   * Flushes the output.
   */
  public void flush() {
    this.txtWriter.flush();
  }

  /**
   * Retrieves the file path.
   *
   * @return the file path
   */
  public String getFilePath() {
    return this.txtWriter.getFilePath();
  }

  /**
   * Retrieves the number of output rows.
   *
   * @return the number of output rows
   */
  public long getLineCount() {
    return this.txtWriter.getLineCount();
  }

  /**
   * Converts to a string.
   *
   * @return the file path and the number of output rows
   */
  public String toString() {
    return this.txtWriter.toString();
  }
}
