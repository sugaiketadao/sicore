package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.LogTxtHandler;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
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

    // Generates web server
    final int portNo = ServerUtil.PROP_MAP.getInt("port.no");
    final int waitingProcessesCount = ServerUtil.PROP_MAP.getInt("waiting.processes.count");
    final int parallelProcessesCount = ServerUtil.PROP_MAP.getInt("parallel.processes.count");
    this.server = HttpServer.create(new InetSocketAddress(portNo), waitingProcessesCount);
    this.server.setExecutor(Executors.newFixedThreadPool(parallelProcessesCount));

    // Root URL handler
    LogUtil.stdout("Creating context. '/'");
    this.server.createContext("/", new RootHandler());

    // Server stop URL handler
    final String serverStopContext = ServerUtil.PROP_MAP.getString("server.stop.context");
    LogUtil.stdout("Creating context. '/" + serverStopContext + "'");
    this.server.createContext("/" + serverStopContext, new StopHandler());

    // Static file handler
    if (ServerUtil.PROP_MAP.containsKey("static.file.context")) {
      final String staticFileContext = ServerUtil.PROP_MAP.getString("static.file.context");
      LogUtil.stdout("Creating context. '/" + staticFileContext + "'");
      this.server.createContext("/" + staticFileContext, new StaticFileHandler());
    }

    // JSON service handler
    if (ServerUtil.PROP_MAP.containsKey("json.service.context")) {
      final String jsonServiceContext = ServerUtil.PROP_MAP.getString("json.service.context");
      final String jsonServicePackage = ServerUtil.PROP_MAP.getString("json.service.package");
      LogUtil.stdout("Creating context. '/" + jsonServiceContext + "'" + " (Java package '" + jsonServicePackage + "')");
      this.server.createContext("/" + jsonServiceContext, new JsonServiceHandler(jsonServiceContext, jsonServicePackage));
    }

    // Sign-in service handler
    if (ServerUtil.PROP_MAP.containsKey("signin.service.context")) {
      final String signinServiceContext = ServerUtil.PROP_MAP.getString("signin.service.context");
      LogUtil.stdout("Creating context. '/" + signinServiceContext + "'");
      this.server.createContext("/" + signinServiceContext, new SigninServiceHandler());
    }

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
      DbUtil.closePooledConn();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in disconnecting pooled DB connections. ");
    }
    try {
      // Closes log text file
      LogTxtHandler.closeAll();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in log text file close.");
    }
    // Does not call System.exit()
    // When called via shutdown hook, the JVM is already in the process of terminating
    // When called via HTTP, System.exit() is called from the StopHandler
  }
}
