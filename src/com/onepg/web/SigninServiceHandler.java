package com.onepg.web;

import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;

/**
 * Sign-in service handler class.
 * @hidden
 */
final class SigninServiceHandler extends AbstractHttpHandler {

  /**
   * Constructor.
   */
  SigninServiceHandler() {
    // Sign-in service does not require JWT validation
    super(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    try {
      // Process request parameters
      final Io io = reqToIoParams(exchange);
      
      // Execute sign-in service processing
      (new SigninService()).execute(io);
      
      // Response
      final String resJson = io.createJsonWithMsg(ServerUtil.MSG_MAP);
      ServerUtil.responseJson(exchange, resJson);
      
    } catch (final Exception | Error e) {
      super.logger.error(e, "An exception error occurred in signin service execution. ");
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Unexpected signin service error. ");
    }
  }
  
  /**
   * Converts request parameters to IO parameters.<br>
   * <ul>
   * <li>Treats any method other than POST as an exception error.</li>
   * </ul>
   *
   * @param exchange HTTP exchange data
   * @return the Io object containing the parameters
   * @throws Exception parameter processing error
   */
  private Io reqToIoParams(final HttpExchange exchange) throws Exception {
    final String reqMethod = exchange.getRequestMethod();
    final Io io = new Io();

    if ("POST".equals(reqMethod)) {
      final String body = ServerUtil.getRequestBody(exchange);
      io.putAllByJson(body);
    } else {
      throw new RuntimeException("Only POST method is valid. " + LogUtil.joinKeyVal("method", reqMethod));
    }
    return io;
  }
}
