package com.onepg.util;

import com.onepg.util.ValUtil.LineSep;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Print writer wrapper class.<br>
 * <ul>
 * <li>Line separator can be specified instead of being OS-dependent.</li>
 * <li>Auto flush functionality allows control of immediate output on line breaks.</li>
 * <li>Provides <code>null</code> safety and validates arguments in advance.</li>
 * </ul>
 * @hidden
 */
public final class CustomPrintWriter extends PrintWriter {

  /** Line separator. */
  private final String lineSep;
  /** Auto flush flag (mainly on line breaks). */
  private final boolean autoFlush;

  /**
   * Constructor.
   *
   * @param out writer
   * @param autoFlush <code>true</code> to auto flush (mainly on line breaks)
   * @param lineSep line separator
   */
  public CustomPrintWriter(final Writer out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * Constructor.
   *
   * @param out output stream
   * @param autoFlush flush on line breaks
   * @param lineSep line separator
   */
  public CustomPrintWriter(final OutputStream out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * Writes a value with line separator.
   *
   * @param value value to output
   */
  private void writeValueAndLineSep(final String value) {
    super.write(value);
    super.write(this.lineSep);
    flushIfNeeded();
  }

  /**
   * Writes data.
   *
   * @param line output data
   */
  private void writeAndFlush(final String line) {
    super.write(line);
    flushIfNeeded();
  }

  /**
   * Executes flush.
   */
  private void flushIfNeeded() {
    if (this.autoFlush) {
      super.flush();
    }
  }

  /**
   * Writes a line separator.
   */
  @Override
  public void println() {
    writeAndFlush(this.lineSep);
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(boolean x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(char x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(int x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(long x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(float x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(double x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(char[] x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(String x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a value with line separator.
   * @param x output data
   */
  @Override
  public void println(Object x) {
    writeValueAndLineSep(String.valueOf(x));
  }

}
