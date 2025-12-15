package com.onepg.util;

import com.onepg.util.ValUtil.LineSep;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Print writer wrapper class.<br>
 * <ul>
 * <li>Line separator can be specified instead of using OS-dependent default.</li>
 * <li>Auto-flush feature enables immediate output control on line breaks.</li>
 * <li>Provides <code>null</code> safety and validates invalid arguments in advance.</li>
 * </ul>
 * @hidden
 */
public final class CustomPrintWriter extends PrintWriter {

  /** Line separator. */
  private final String lineSep;
  /** Auto-flush flag (mainly on line breaks). */
  private final boolean autoFlush;

  /**
   * Constructor.
   *
   * @param out the writer
   * @param autoFlush <code>true</code> to auto-flush (mainly on line breaks)
   * @param lineSep the line separator
   */
  public CustomPrintWriter(final Writer out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * Constructor.
   *
   * @param out the output stream
   * @param autoFlush flush on line break
   * @param lineSep the line separator
   */
  public CustomPrintWriter(final OutputStream out, final boolean autoFlush, final LineSep lineSep) {
    super(out, false);
    this.autoFlush = autoFlush;
    this.lineSep = lineSep.toString();
  }

  /**
   * Writes the value and line separator.
   *
   * @param value the value to output
   */
  private void writeValueAndLineSep(final String value) {
    super.write(value);
    super.write(this.lineSep);
    flushIfNeeded();
  }

  /**
   * Writes.
   *
   * @param line the output data
   */
  private void writeAndFlush(final String line) {
    super.write(line);
    flushIfNeeded();
  }

  /**
   * Flushes if needed.
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
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(boolean x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(char x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(int x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(long x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(float x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(double x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(char[] x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(String x) {
    writeValueAndLineSep(String.valueOf(x));
  }

  /**
   * Writes a line separator.
   * @param x the output data
   */
  @Override
  public void println(Object x) {
    writeValueAndLineSep(String.valueOf(x));
  }

}
