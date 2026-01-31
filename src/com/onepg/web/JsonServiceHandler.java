package com.onepg.web;

import com.onepg.util.Io;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ResourcesUtil;
import com.onepg.util.ResourcesUtil.FwResourceName;
import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;

/**
 * JSON service handler class.<br>
 * <ul>
 * <li>Receives HTTP requests and delegates processing to the corresponding web service class.</li>
 * <li>Dynamically resolves the service class name from the URL path and executes it using reflection.</li>
 * </ul>
 * @hidden
 */
final class JsonServiceHandler extends AbstractHttpHandler {

  /** Own context path. */
  private final String contextPath;

  /** Service class package. */
  private final String svcClsPackage;

  /** Message map &lt;message ID, message text&gt;. */
  private final IoItems msgMap;

  /**
   * Constructor.<br>
   * <ul>
   * <li>Sets the context path and service class package.</li>
   * </ul>
   *
   * @param contextPath Context path
   * @param svcClsPackage Service class package
   */
  JsonServiceHandler(final String contextPath, final String svcClsPackage) {
    super();
    this.contextPath = contextPath;
    this.svcClsPackage = svcClsPackage;
    this.msgMap = ResourcesUtil.getJson(FwResourceName.MSG);
  }

  /**
   * {@inheritDoc}
   * <ul>
   * <li>Dynamically resolves and executes the service class from the request URL.</li>
   * <li>Processes parameters according to the <code>GET/POST</code> method.</li>
   * </ul>
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    // Request path
    final String reqPath = exchange.getRequestURI().getPath();
    // Builds class name
    final String clsName = buildClsNameByReq(reqPath);
    
    try {
      // Creates and validates service class
      final AbstractWebService serviceObj = createWebServiceClsInstance(clsName);
      
      // Processes request parameters
      final Io io = reqToIoParams(exchange, clsName);
      
      // Executes service processing
      serviceObj.execute(io);
      
      // Response
      final String resJson = io.createJsonWithMsg(this.msgMap);
      ServerUtil.responseJson(exchange, resJson);
      
    } catch (final ClassNotFoundException e) {
      super.logger.error(e, "Web service class not found. " + LogUtil.joinKeyVal("class", clsName));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND, "Json service class not found. ");
    } catch (final Exception | Error e) {
      super.logger.error(e, "An exception error occurred in web service execution. " + LogUtil.joinKeyVal("class", clsName));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, "Unexpected json service error. ");
    }
  }

  /**
   * Builds class name.<br>
   * <ul>
   * <li>Generates the service class name from the request path.</li>
   * </ul>
   *
   * @param reqPath Request path
   * @return Class name
   */
  private String buildClsNameByReq(final String reqPath) {
    return this.svcClsPackage + "."
        + reqPath.replace("/" + this.contextPath + "/", "").replace("/", ".");
  }

  /**
   * Creates service instance.<br>
   * <ul>
   * <li>Generates a service instance from the class name and performs type checking.</li>
   * </ul>
   *
   * @param clsName Class name
   * @return Service instance
   * @throws Exception Instance creation error
   */
  private AbstractWebService createWebServiceClsInstance(final String clsName) throws Exception {
    final Class<?> cls = getCls(clsName);
    final Object clsObj = cls.getDeclaredConstructor().newInstance();

    if (!(clsObj instanceof AbstractWebService)) {
      throw new RuntimeException("Classes not inheriting from web service base class (AbstractWebService) cannot be executed. ");
    }

    return (AbstractWebService) clsObj;
  }

  /**
   * Gets class.<br>
   * <ul>
   * <li>Obtains a Class object from the class name.</li>
   * </ul>
   *
   * @param clsName Class name
   * @return Class object
   * @throws ClassNotFoundException If the class is not found
   */
  private Class<?> getCls(final String clsName) {
    try {
      return Class.forName(clsName);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Web service class not found. " + LogUtil.joinKeyVal("class", clsName), e);
    }
  }
  
  /**
   * Converts request parameters to I/O parameters.<br>
   * <ul>
   * <li>Parses parameters according to the HTTP method and sets them in the <code>Io</code> object.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param clsName Class name (for logging)
   * @return Io object containing parameters
   * @throws Exception Parameter processing error
   */
  private Io reqToIoParams(final HttpExchange exchange, final String clsName) throws Exception {
    final String reqMethod = exchange.getRequestMethod();
    final Io io = new Io();

    if ("GET".equals(reqMethod)) {
      final String query = exchange.getRequestURI().getQuery();
      io.putAllByUrlParam(query);
    } else if ("POST".equals(reqMethod)) {
      final String body = ServerUtil.getRequestBody(exchange);
      io.putAllByJson(body);
    } else {
      throw new RuntimeException("Only GET or POST method is valid. " + LogUtil.joinKeyVal("method", reqMethod));
    }
    return io;
  }
}