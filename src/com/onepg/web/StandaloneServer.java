package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.LogTxtHandler;
import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.IoItems;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Web server class.
 * @hidden
 */
public final class StandaloneServer {

  /** Singleton instance. */
  private static StandaloneServer instance = null;
  /** HTTP server (web server). */
  private HttpServer server = null;
  /** Stop processing executed flag. */
  private boolean terminated = false;

  /**
   * Constructor.
   */
  private StandaloneServer() {
    // No processing
  }
  
  /**
   * Gets instance.
   * @return JsonHttpServer instance
   */
  static synchronized StandaloneServer getInstance() {
    if (instance == null) {
      instance = new StandaloneServer();
    }
    return instance;
  }

  /**
   * Main processing.<br>
   * <ul>
   * <li>Starts the web server.</li>
   * </ul>
   *
   * @param args Arguments
   * @throws IOException I/O exception error
   */
  public static void main(final String[] args) {
    LogUtil.javaInfoStdout();
    LogUtil.stdout("Starting web server main processing. arguments=" + LogUtil.join(args));

    // Gets singleton instance
    final StandaloneServer myObj = getInstance();

    // Adds shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        myObj.terminate();
      }
    });

    // Executes self instance
    try {
      myObj.start(args);
    } catch (Exception e) {
      // Outputs logs first here
      LogUtil.stdout(e, "An exception error occurred in web server startup. ");
      System.exit(1);
      return;
    }
    LogUtil.stdout("Ending web server main processing. ");
  }

  /**
   * Starts web server.
   *
   * @param args Arguments
   * @throws IOException I/O exception error
   */
  private void start(final String[] args) throws IOException {
    LogUtil.stdout("Starting web server startup processing. ");

    // Web server settings
    final IoItems propMap = PropertiesUtil.getFrameworkProps(FwPropertiesName.WEB);

    // Generates web server
    final int portNo = propMap.getInt("port.no");
    final int waitingProcessesCount = propMap.getInt("waiting.processes.count");
    final int parallelProcessesCount = propMap.getInt("parallel.processes.count");
    this.server = HttpServer.create(new InetSocketAddress(portNo), waitingProcessesCount);
    this.server.setExecutor(Executors.newFixedThreadPool(parallelProcessesCount));

    // Root URL handler
    LogUtil.stdout("Creating context. '/'");
    this.server.createContext("/", new RootHandler());

    // Server stop URL handler
    final String serverStopContext = propMap.getString("server.stop.context");
    LogUtil.stdout("Creating context. '/" + serverStopContext + "'");
    this.server.createContext("/" + serverStopContext, new StopHandler());

    // Static file handler
    final String staticFileContext = propMap.getString("static.file.context");
    LogUtil.stdout("Creating context. '/" + staticFileContext + "'");
    this.server.createContext("/" + staticFileContext, new StaticFileHandler());
    
    // JSON service handler
    final String jsonServiceContext = propMap.getString("json.service.context");
    final String jsonServicePackage = propMap.getString("json.service.package");
    LogUtil.stdout("Creating context. '/" + jsonServiceContext + "'" + " (Java package '"
        + jsonServicePackage + "')");
    this.server.createContext("/" + jsonServiceContext,
        new JsonServiceHandler(jsonServiceContext, jsonServicePackage));

    // Starts
    this.server.start();
    LogUtil.stdout("Web server started. " + LogUtil.joinKeyVal("port", String.valueOf(portNo), "parallel",
            String.valueOf(parallelProcessesCount), "stopUrl", String.valueOf(serverStopContext)));
  }

  /**
   * Root URL handler.
   */
  private class RootHandler implements HttpHandler {
    @Override
    public void handle(final HttpExchange exchange) throws IOException {
      ServerUtil.responseText(exchange, "HTTP server is running.");
    }
  }

  /**
   * Server stop handler.
   */
  private class StopHandler implements HttpHandler {
    @Override
    public void handle(final HttpExchange exchange) throws IOException {
      try {
        // Sends response first
        ServerUtil.responseText(exchange, "HTTP server shutdown...", "Reload to confirm shutdown.");
        // Calls terminate method to perform complete cleanup
        StandaloneServer.this.terminate();
        
        // Waits 100ms for response transmission completion
        TimeUnit.MILLISECONDS.sleep(100);
        // Ensures JVM process termination
        System.exit(0);
        
      } catch (final Exception | Error e) {
        LogUtil.stdout(e, "An exception error occurred in web server stop handler. ");
        // Terminates process even if error occurs
        System.exit(1);
      }
    }
  }

  /**
   * Stop processing.
   */
  synchronized void terminate() {
    // Does nothing if stop processing has already been executed (prevents duplicate execution)
    if (this.terminated) {
      return;
    }
    this.terminated = true;
    
    LogUtil.stdout("Starting web server stop processing. ");
    try {
      // Stops web server
      if (!ValUtil.isNull(this.server)) {
        this.server.stop(0);
        this.server = null;
        LogUtil.stdout("Web server stopped.");
      }
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in web server stop.");
    }
    try {
      // Disconnects pooled DB
      if (DbUtil.closePooledConn()) {
        LogUtil.stdout("Disconnected pooled DB connections.");
      }
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in disconnecting pooled DB connections. ");
    }
    try {
      // Closes log text file
      if (LogTxtHandler.closeAll()) {
        LogUtil.stdout("Closed log text file.");
      }
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in log text file close.");
    }
    // Note: Does not call System.exit()
    // - When via shutdown hook: JVM is already in termination process
    // - When via HTTP: StopHandler side executes System.exit()
  }
}
