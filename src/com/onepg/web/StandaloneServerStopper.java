package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.IoItems;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * StandaloneHttpServer stop class.
 * @hidden
 */
public final class StandaloneServerStopper {

  /** HTTP connection timeout (milliseconds). */
  private static final int HTTP_TIMEOUT_MS = 5000;

  /**
   * Main processing.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {
    try {
      // Web server settings
      final IoItems propMap = PropertiesUtil.getFrameworkProps(FwPropertiesName.WEB);
      final int portNo = propMap.getInt("port.no");
      final String serverStopContext = propMap.getString("server.stop.context");
      // Sends stop request
      stopHttpServer(portNo, serverStopContext);
      // Confirms stop
      confirmServerStop(portNo);

      LogUtil.stdout("Web server stop processing completed.");
      System.exit(0);

    } catch (final Exception e) {
      LogUtil.stdout(e, "An exception error occurred in web server stop. ");
      System.exit(1);
    }
  }

  /**
   * Sends web server stop request.<br>
   *
   * @param port Target port
   * @param stopContext Stop context
   * @throws IOException HTTP communication error
   */
  private static void stopHttpServer(final int port, final String stopContext) throws IOException {
    final String stopUrl = "http://localhost:" + String.valueOf(port) + "/" + stopContext;
    LogUtil.stdout("Sending stop request. " + LogUtil.joinKeyVal("url", stopUrl));

    HttpURLConnection connection = null;
    try {
      final URI uri = new URI(stopUrl);
      final URL url = uri.toURL();
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(HTTP_TIMEOUT_MS);
      connection.setReadTimeout(HTTP_TIMEOUT_MS);

      final int responseCode = connection.getResponseCode();
      LogUtil.stdout("Stop request response received. " + LogUtil.joinKeyVal("responseCode", responseCode));

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Reads response content
        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
          final String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
          LogUtil.stdout("Stop request response content: " + response);
        }
      } else {
        LogUtil.stdout("Stop request received error response. " + LogUtil.joinKeyVal("responseCode", responseCode));
      }
    } catch (final java.net.URISyntaxException e) {
      throw new RuntimeException("Invalid URL format. " + LogUtil.joinKeyVal("url", stopUrl), e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * Confirms server stop.<br>
   *
   * @param port Target port
   */
  private static void confirmServerStop(final int port) {
    final String checkUrl = "http://localhost:" + String.valueOf(port) + "/";
    LogUtil.stdout("Starting server stop confirmation.");

    // Checks up to 10 times at 1-second intervals
    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(1000); // Waits 1 second

        final URI uri = new URI(checkUrl);
        final URL url = uri.toURL();
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);

        final int responseCode = connection.getResponseCode();
        connection.disconnect();

        LogUtil.stdout("Server is still running. " + LogUtil.joinKeyVal("attempt", i + 1, "responseCode", responseCode));
      } catch (final java.net.URISyntaxException e) {
        throw new RuntimeException("Invalid URL format. " + LogUtil.joinKeyVal("url", checkUrl), e);
      } catch (final Exception e) {
        // Connection error = server has stopped
        LogUtil.stdout("Server has stopped. " + LogUtil.joinKeyVal("attempt", i + 1));
        return;
      }
    }

    LogUtil.stdout("Warning: Server stop confirmation timed out. Please check server status manually.");
  }

}