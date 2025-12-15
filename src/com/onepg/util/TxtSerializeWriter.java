package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Text writer serialization wrapper class.<br>
 * <ul>
 * <li>Serializes output even when called from parallel threads.</li>
 * <li>Also caches output data internally.</li>
 * </ul>
 * @hidden
 */
final class TxtSerializeWriter extends TxtWriter {

  /** Line cache. */
  private final ConcurrentLinkedQueue<String> lineCache = new ConcurrentLinkedQueue<>();
  /** Printing flag. */
  private AtomicBoolean printNow = new AtomicBoolean(false);

  /**
   * Constructor.
   *
   * @param filePath the file path
   * @param canAppend <code>true</code> if appending is allowed
   * @param lineFlush <code>true</code> if flushing on line break
   * @param lineSep the line separator
   * @param charSet the character set
   * @param withBom <code>true</code> if with BOM
   */
  TxtSerializeWriter(final String filePath, final boolean canAppend, final boolean lineFlush,
      final LineSep lineSep, final CharSet charSet, final boolean withBom) {
    super(filePath, canAppend, lineFlush, lineSep, charSet, withBom);
  }

  /**
   * Prints a line (serialized).
   */
  private synchronized void linePrint(final String line) {
    super.println(line);
  }

  /**
   * Prints cache (serialized).
   *
   * @param fullFlush <code>true</code> to output all cached data (intended for use at program termination)
   */
  private synchronized void cachePrint(final boolean fullFlush) {
    final int olsSize = lineCache.size();
    if (olsSize <= 0) {
      return;
    }
    final int escSize;
    if (fullFlush) {
      escSize = Integer.MAX_VALUE;
    } else {
      escSize = olsSize;
    }

    int count = 0;
    String cache = null;
    while ((cache = this.lineCache.poll()) != null) {
      // Retrieves from cache and outputs
      linePrint(cache);
      count++;
      if (escSize <= count) {
        // Finishes for now even if added during processing (to avoid long processing)
        return;
      }
    }
  }

  @Override
  public void println(String line) {
    if (ValUtil.isNull(line)) {
      return;
    }

    // Stores in cache first and then outputs
    this.lineCache.offer(line);
    if (this.printNow.compareAndSet(false, true)) {
      try {
        // If not currently printing, switches to printing and outputs cache
        cachePrint(false);
      } catch (Exception e) {
        throw e; 
      }finally {
        // Resets flag even on exception
        this.printNow.set(false);
      }
    }
  }

  @Override
  public void flush() {
    cachePrint(true);
    super.flush();
  }

  @Override
  public void close() {
    flush();
    super.close();
  }
}
