package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * HTTP handler base class.<br>
 * <ul>
 * <li>Provides common error handling.</li>
 * <li>Defines concrete HTTP request processing by implementing the <code>doExecute</code> method in a subclass.</li>
 * <li>When class variables are used in a subclass, those variables are shared across multiple requests.</li>
 * </ul>
 * @hidden
 */
abstract class AbstractHttpHandler implements HttpHandler {

  /** Log writer. */
  protected final LogWriter logger;

  /** JWT validation flag. */
  private final boolean isJwtValidate;

  /**
   * Main processing.<br>
   * <ul>
   * <li>Implements concrete HTTP request processing in a subclass.</li>
   * </ul>
   *
   * @param exchange HTTP exchange data
   * @throws Exception exception error
   */
  protected abstract void doExecute(final HttpExchange exchange) throws Exception;

  /**
   * Constructor.
   */
  AbstractHttpHandler() {
    this(true);
  }

  /**
   * Constructor.
   * 
   * @param isJwtValidate JWT validation flag (specify <code>false</code> if JWT validation is not required)
   */
  AbstractHttpHandler(final boolean isJwtValidate) {
    super();
    this.logger = LogUtil.newLogWriter(getClass());
    this.isJwtValidate = (ServerUtil.LDAP_ENABLED && isJwtValidate);
    if (!this.isJwtValidate) {
      this.logger.info("Jwt validation is disabled.");
    }
  }

  /**
   * Invokes the main processing.<br>
   * <ul>
   * <li>On exception, logs at the appropriate error level and returns an error response.</li>
   * </ul>
   *
   * @param exchange HTTP exchange data
   * @throws IOException I/O exception error
   */
  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    try {
      // JWT validation
      if (this.isJwtValidate) {
        if (!validateJwt(exchange)) {
          // Return an error response and terminate processing on JWT validation failure
          ServerUtil.responseText(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized. ");
          return;
        }
      }
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
    // Flush log in developer mode
    if (this.logger.isDevelopMode()) {
      this.logger.flush();
    }
  }

  /**
   * JWT validation.<br>
   * <ul>
   * <li>Retrieves the JWT from the Authorization header and validates it.</li>
   * <li>Outputs an error log and returns <code>false</code> if the JWT is invalid.</li>
   * <li>Normally, at this point the JWT is expected to be valid, so an error log is output.</li>
   * </ul>
   *
   * @param exchange HTTP exchange data
   * @return <code>true</code> if validation succeeds
   * @throws IOException I/O exception error
   */
  private boolean validateJwt(final HttpExchange exchange) {
    final String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      this.logger.info("Authorization header is missing or invalid. " + LogUtil.joinKeyVal("request", exchange.getRequestURI().getPath()));
      return false;
    }
    try {
      JwtUtil.validateToken(authHeader.substring(7));
      return true;
    } catch (final Exception e) {
      this.logger.error(e, "JWT validation failed. " + LogUtil.joinKeyVal("request", exchange.getRequestURI().getPath()));
      return false;
    }
  }
}
