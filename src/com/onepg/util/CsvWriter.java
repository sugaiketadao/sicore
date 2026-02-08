package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.ValUtil.LineSep;

/**
 * CSV writer class.<br>
 * <ul>
 * <li>Wraps TxtWriter and provides features specialized for CSV output.</li>
 * <li>Simplifies output in CSV format.</li>
 * <li>Allows specification of character set, line separator, and CSV format.</li>
 * </ul>
 */
public class CsvWriter implements AutoCloseable {

  /** Text writer. */
  private final TxtWriter txtWriter;
  /** CSV format. */
  private final CsvType csvType;

  /**
   * Constructor.
   *
   * @param filePath file path
   * @param lineSep line separator
   * @param charSet character set
   * @param csvType CSV format
   */
  public CsvWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final CsvType csvType) {
    this.txtWriter = new TxtWriter(filePath, lineSep, charSet);
    this.csvType = csvType;
  }

  /**
   * Closes the file.
   */
  public void close() {
    this.txtWriter.close();
  }

  /**
   * Outputs a CSV row (string array).
   *
   * @param values value array
   */
  public void println(final String[] values) {
    this.txtWriter.println(ValUtil.joinCsv(values, this.csvType));
  }

  /**
   * Outputs a CSV row (IoItems).
   *
   * @param row row data
   */
  public void println(final IoItems row) {
    this.txtWriter.println(row.createCsv(this.csvType));
  }

  /**
   * Flushes the output.
   */
  public void flush() {
    this.txtWriter.flush();
  }

  /**
   * Returns the file path.
   *
   * @return the file path
   */
  public String getFilePath() {
    return this.txtWriter.getFilePath();
  }

  /**
   * Returns the number of output lines.
   *
   * @return the number of output lines
   */
  public long getLineCount() {
    return this.txtWriter.getLineCount();
  }

  /**
   * Converts to a string.
   *
   * @return the file path and number of output lines
   */
  public String toString() {
    return this.txtWriter.toString();
  }
}
