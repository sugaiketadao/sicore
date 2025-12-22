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
 * <li>Always call <code>#close()</code> because output content may remain in cache if the last write is concurrent. (<code>#close()</code> internally calls <code>#flush()</code>)</li>
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
   * Prints cache (serialized).
   *
   * @param fullFlush <code>true</code> to output all cached data (intended for use at program termination)
   */
  private synchronized void cachePrint(final boolean fullFlush) {
    if (fullFlush) {
      String cache = null;
      while ((cache = this.lineCache.poll()) != null) {
        super.println(cache);
      }
      return;
    }

    // Outputs only the cached data at this point in time.
    // Finishes even if added during processing to avoid long processing.
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
    // Replaces null here as well because putting null in cache is not desirable, even though TxtWriter#println also replaces null
    this.lineCache.offer(ValUtil.nvl(line));
    if (this.isPrinting.compareAndSet(false, true)) {
      try {
        // If not currently printing, switches to printing and outputs cache
        cachePrint(false);
      }finally {
        // Resets flag even on exception
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
