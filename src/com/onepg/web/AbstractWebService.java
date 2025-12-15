package com.onepg.web;

import com.onepg.util.BreakException;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Web service base class.<br>
 * <ul>
 * <li>Provides common processing (logging, exception handling, etc.) for each web service.</li>
 * <li>Defines concrete web service processing by implementing doExecute method in subclasses.</li>
 * </ul>
 */
public abstract class AbstractWebService {

  /** Trace code. */
  protected final String traceCode;
  /** Log writer. */
  protected final LogWriter logger;

  /**
   * Main processing.<br>
   * <ul>
   * <li>Implements concrete web service processing in subclasses.</li>
   * </ul>
   *
   * @param io the argument and return value (request and response)
   * @throws Exception exception error
   */
  public abstract void doExecute(final Io io) throws Exception;

  /**
   * Constructor.
   */
  public AbstractWebService() {
    this.traceCode = ValUtil.getSequenceCode();
    this.logger = LogUtil.newLogWriter(getClass(), this.traceCode);
  }

  /**
   * Invokes main processing.<br>
   * <ul>
   * <li>Package-private to prevent direct external calls.</li>
   * <li>Executes common processing including logging and exception handling, then invokes concrete business processing.</li>
   * </ul>
   *
   * @param io the argument and return value (request and response)
   * @throws Exception exception error
   */
  void execute(final Io io) throws Exception {
    try {
      this.logger.begin();
      doExecute(io);
    } catch (final Exception | Error e) {
      this.logger.error(e, "An exception error occurred in web service processing. ");
      throw new BreakException();
    } finally {
      this.logger.end();
    }
  }
}
