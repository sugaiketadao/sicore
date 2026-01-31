package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Text writer serialization wrapper class.<br>
 * <ul>
 * <li>Serializes output even when output from parallel threads.</li>
 * <li>Also caches output data internally.</li>
 * <li>If the last write is simultaneous, output contents may remain in cache, so <code>#close()</code> must be called. (<code>#flush()</code> is called within <code>#close()</code>)</li>
 * </ul>
 * @hidden
 */
final class TxtSerializeWriter extends TxtWriter {

  /** Line cache. */
  private final ConcurrentLinkedQueue<String> lineCache = new ConcurrentLinkedQueue<>();
  /** Printing flag. */
  private AtomicBoolean isPrinting = new AtomicBoolean(false);

  /**
   * Constructor.
   *
   * @param filePath File path
   * @param lineSep Line separator
   * @param charSet Character set
   * @param withBom <code>true</code> if with BOM
   * @param canAppend <code>true</code> if append is allowed
   * @param lineFlush <code>true</code> if flushing on line break
   */
  TxtSerializeWriter(final String filePath, final LineSep lineSep, final CharSet charSet, final boolean withBom,
     final boolean canAppend, final boolean lineFlush) {
    super(filePath, lineSep, charSet, withBom, canAppend, lineFlush);
  }

  /**
   * Cache output (serialized).
   *
   * @param fullFlush <code>true</code> if outputting all cached data (assumed to be used at program termination)
   */
  private synchronized void cachePrint(final boolean fullFlush) {
    if (fullFlush) {
      String cache = null;
      while ((cache = this.lineCache.poll()) != null) {
        super.println(cache);
      }
      return;
    }

    // Outputs only what is cached at the current time.
    // Ends even if added during processing to avoid long processing time.
    final int cacheSize = lineCache.size();
    for (int i = 0; i < cacheSize; i++) {
      final String cache = this.lineCache.poll();
      if (cache == null) {
        break;
      }
      super.println(cache);
    }
  }

  @Override
  public void println(final String line) {
    // Stores in cache first, then outputs
    // TxtWriter#println also replaces null, but replacing here too as putting null in cache is not desirable
    this.lineCache.offer(ValUtil.nvl(line));
    if (this.isPrinting.compareAndSet(false, true)) {
      try {
        // If flag is not printing, switches to printing and outputs cache
        cachePrint(false);
      }finally {
        // Restores flag even if exception occurs
        this.isPrinting.set(false);
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
