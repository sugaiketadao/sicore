package com.onepg.web;

import com.onepg.util.BreakException;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Web service base class.<br>
 * <ul>
 * <li>Provides common processing (log output, exception handling, etc.) for each web service.</li>
 * <li>Defines specific web service processing by implementing the <code>doExecute</code> method in subclasses.</li>
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
   * <li>Implements specific web service processing in subclasses.</li>
   * </ul>
   *
   * @param io Argument and return value (request and response)
   * @throws Exception Exception error
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
   * Calls main processing.<br>
   * <ul>
   * <li>Executes log start processing, calls the <code>doExecute</code> method, and finally executes log end processing.</li>
   * <li>This is package-private to prevent direct calls from outside.</li>
   * </ul>
   *
   * @param io Argument and return value (request and response)
   * @throws Exception Exception error
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
