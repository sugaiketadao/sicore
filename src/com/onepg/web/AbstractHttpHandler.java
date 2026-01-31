package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 * HTTP handler base class.<br>
 * <ul>
 * <li>Provides common error handling.</li>
 * <li>Defines specific HTTP request processing by implementing the <code>doExecute</code> method in subclasses.</li>
 * <li>If class variables are used in subclasses, those variables are shared across multiple requests.</li>
 * </ul>
 * @hidden
 */
abstract class AbstractHttpHandler implements HttpHandler {

  /** Log writer. */
  protected final LogWriter logger;

  /**
   * Main processing.<br>
   * <ul>
   * <li>Implements specific HTTP request processing in subclasses.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @throws Exception Exception error
   */
  protected abstract void doExecute(final HttpExchange exchange) throws Exception;

  /**
   * Constructor.
   */
  AbstractHttpHandler() {
    super();
    this.logger = LogUtil.newLogWriter(getClass());
  }

  /**
   * Calls main processing.<br>
   * <ul>
   * <li>Outputs logs at the appropriate error level and returns an error response when an exception occurs.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @throws IOException I/O exception error
   */
  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    try {
      doExecute(exchange);
    } catch (final Exception | Error e) {
      this.logger.error(e, "An exception error occurred in HTTP handler processing. ");
      try {
        ServerUtil.responseText(exchange, e, "Unexpected http handler error. ");
      } catch (IOException re) {
        this.logger.error(re, "An exception error occurred while outputting HTTP handler error response. ");
        try {
          exchange.close();
        } catch (Exception ce) {
          this.logger.error(ce, "An exception error occurred in HttpExchange close. ");
        }
      }
    }
    // Flushes logs in development mode
    if (this.logger.isDevelopMode()) {
      this.logger.flush();
    }
  }
}
