package com.onepg.web;

import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.ValUtil.CharSet;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web server utility class.<br>
 * <ul>
 * <li>Provides common processing for HTTP responses.</li>
 * <li>Supports various response formats such as text, file, and redirect.</li>
 * </ul>
 * @hidden
 */
final class ServerUtil {

  /** Log writer. */
  private static final LogWriter logger = LogUtil.newLogWriter(ServerUtil.class);

  /** Optimal buffer size (bytes). */
  private static final int OPTIMAL_BUFFER_SIZE = calcBufferSize();
  /** Minimum file size for text compression (1KB). */
  private static final long TXT_TO_COMPRESS_MIN_SIZE = 1024;
  /** Maximum file size for text compression (1MB). */
  private static final long TXT_TO_COMPRESS_MAX_SIZE = 1024 * 1024;
  
  /** HTTP date format (RFC 1123). */
  private static final DateTimeFormatter DTF_HTTP_DATE = 
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH)
                       .withZone(ZoneId.of("GMT"));

  /**
   * Constructor.<br>
   * <ul>
   * <li>Prohibits instantiation because this is a utility class.</li>
   * </ul>
   */
  private ServerUtil() {
    // No processing
  }

  /**
   * Calculates optimal buffer size.
   * @return Buffer size (bytes)
   */
  private static int calcBufferSize() {
    // Available memory
    final long maxMemory = Runtime.getRuntime().maxMemory();
    // 4GB or more
    if (maxMemory > 4L * 1024 * 1024 * 1024) {
      // 64KB
      return 65536;
    }
    // 1GB or more
    if (maxMemory > 1024 * 1024 * 1024) {
      // 32KB
      return 32768;
    }
    // Basic size 8KB
    return 8192;
  }

  /**
   * Displays text response.<br>
   * <ul>
   * <li>Returns with OK(200) status.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param txts Display text (multiple allowed)
   * @throws IOException I/O exception error
   */
  static void responseText(final HttpExchange exchange, final String... txts) throws IOException {
    responseText(exchange, HttpURLConnection.HTTP_OK, txts);
  }

  /**
   * Displays error text response.<br>
   * <ul>
   * <li>Returns with Internal Server Error(500) status.</li>
   * <li>Responds including error details (stack trace).</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param e        Error object
   * @param txts     Display text (multiple allowed)
   * @throws IOException I/O exception error
   */
  static void responseText(final HttpExchange exchange, final Throwable e, final String... txts)
      throws IOException {
    // Includes stack trace in response
    final String[] errTxts = new String[txts.length + 1];
    System.arraycopy(txts, 0, errTxts, 0, txts.length);
    errTxts[txts.length] = LogUtil.getStackTrace(ValUtil.LF, e);
    responseText(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, errTxts);
  }

  /**
   * Displays text response.<br>
   * <ul>
   * <li>Returns a text response with the specified HTTP status code.</li>
   * </ul>
   *
   * @param exchange   HTTP send/receive data
   * @param httpStatus HTTP status code (HttpURLConnection.HTTP_*)
   * @param txts       Display text (multiple allowed)
   * @throws IOException I/O exception error
   */
  static void responseText(final HttpExchange exchange, final int httpStatus, final String... txts)
      throws IOException {
    final String txt = ValUtil.join(ValUtil.LF, txts);
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", "text/plain; charset=UTF-8");
    headers.set("Cache-Control", "no-cache");
    responseCompressed(exchange, httpStatus, txt);
  }


  /**
   * Displays JSON response.<br>
   * <ul>
   * <li>Returns with OK(200) status.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param json     JSON string
   * @throws IOException I/O exception error
   */
  static void responseJson(final HttpExchange exchange, final String json)
      throws IOException {
    if (ValUtil.isBlank(json)) {
      throw new RuntimeException("Response JSON string is empty. ");
    }
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", "application/json; charset=UTF-8");
    headers.set("Cache-Control", "no-cache");
    responseCompressed(exchange, HttpURLConnection.HTTP_OK, json);
  }

  /**
   * Displays file.<br>
   * <ul>
   * <li>Returns the content of the specified file as a response.</li>
   * <li>Sets the appropriate Content-Type according to the file type.</li>
   * <li>Checks the file modification date and time and discards the cache appropriately.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param resFile Display file
   * @param charSet Character set
   * @return <code>true</code> if cache is used, <code>false</code> otherwise including any errors
   * @throws IOException I/O exception error
   */
  static boolean responseFile(final HttpExchange exchange, final File resFile, final CharSet charSet)
      throws IOException {
    // Checks file existence
    if (!resFile.exists() || !resFile.isFile()) {
      responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND, "File not found. " +  LogUtil.joinKeyVal("filename", resFile.getName()));
      return false;
    }

    // Prevents path traversal attacks
    final String canonicalPath = resFile.getCanonicalPath();
    if (!canonicalPath.startsWith(PropertiesUtil.APPLICATION_DIR_PATH)) {
      responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN, "Access denied. " +  LogUtil.joinKeyVal("filename", resFile.getName()));
      return false;
    }

    // Server-side file modification date serial value (milliseconds)
    final long serverModMsec = resFile.lastModified();
    // Server-side file modification date string
    final String serverModVal = DTF_HTTP_DATE.format(Instant.ofEpochMilli(serverModMsec));
    // Client-side file modification date string
    final String clientModVal = exchange.getRequestHeaders().getFirst("If-Modified-Since");

    if (isUseCache(serverModMsec, serverModVal, clientModVal)) {
      // Returns 304 if not modified and encourages cache usage
      final Headers headers = exchange.getResponseHeaders();
      setSecurityHeaders(headers);
      headers.set("Last-Modified", serverModVal);
      headers.set("Cache-Control", "max-age=0, must-revalidate");
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
      if (logger.isDevelopMode()) {
        logger.develop("Using client-side cache. " + LogUtil.joinKeyVal("filename", resFile.getName(),
            "lastModified", serverModVal));
      }
      return true;
    }

    if (logger.isDevelopMode()) {
      logger.develop("Returning latest file. " + LogUtil.joinKeyVal("filename", resFile.getName(), "lastModified", serverModVal));
    }

    // Checks content type
    final String checkCtype;
    
    // Determines by extension first because Files#probeContentType() may be inaccurate on Windows OS
    final String fileName = resFile.getName().toLowerCase();
    if (fileName.endsWith(".css")) {
      checkCtype = "text/css";
    } else if (fileName.endsWith(".js")) {
      checkCtype = "application/javascript";
    } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
      checkCtype = "text/html";
    } else if (fileName.endsWith(".svg")) {
      checkCtype = "image/svg+xml";
    } else if (fileName.endsWith(".woff") || fileName.endsWith(".woff2")) {
      checkCtype = "font/woff";
    } else if (fileName.endsWith(".ttf")) {
      checkCtype = "font/ttf";
    } else if (fileName.endsWith(".eot")) {
      checkCtype = "application/vnd.ms-fontobject";
    } else {
      // Uses Java standard MIME type for others
      checkCtype = Files.probeContentType(resFile.toPath());
    }
    
    // Determines if text
    final boolean isText = (checkCtype.startsWith("text/")
        || checkCtype.endsWith("/javascript")
        || checkCtype.endsWith("/json")
        || checkCtype.endsWith("/xml")
        || checkCtype.endsWith("+xml"));
    
    // Content type for header setting
    final String headCtype;
    if (ValUtil.isBlank(checkCtype)) {
      // Treats as binary when unknown
      headCtype = "application/octet-stream";
    } else {
      if (isText && !checkCtype.contains("charset=")) {
        // Adds character set specification
        headCtype = checkCtype + "; charset=" + charSet.toString();
      } else {
        headCtype = checkCtype;
      }
    }
    
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Content-Type", headCtype);
    // Cache control
    headers.set("Last-Modified", serverModVal);
    headers.set("Cache-Control", "max-age=0, must-revalidate");
    headers.set("ETag", "\"" + serverModMsec + "-" + resFile.length() + "\"");

    final long fileSize = resFile.length();
    if (isText && TXT_TO_COMPRESS_MIN_SIZE < fileSize && fileSize <= TXT_TO_COMPRESS_MAX_SIZE) {
      // Compresses and responds if text file is within target size for compression
      final String fileContent = Files.readString(resFile.toPath(),
          java.nio.charset.Charset.forName(charSet.toString()));
      responseCompressed(exchange, HttpURLConnection.HTTP_OK, fileContent);
      return false;
    }

    // Responds without compression if not target for compression
    responseCopyOrStream(exchange, resFile);

    return false;
  }

  /**
   * Determines cache usage.<br>
   * <ul>
   * <li>Compares the server-side file modification date and time with the client-side If-Modified-Since header.</li>
   * <li>Determines that cache is available if the client-side date and time is equal to or later than the server-side date and time.</li>
   * <li>Compares dates and times considering a 1-second error margin.</li>
   * <li>If date and time parsing fails, determines by exact string match.</li>
   * </ul>
   * 
   * @param serverModMsec Server-side file modification date serial value (milliseconds)
   * @param serverModVal  Server-side file modification date (RFC 1123 format string)
   * @param clientModVal  Client-side If-Modified-Since header (RFC 1123 format string, <code>null</code> allowed)
   * @return <code>true</code> if cache is available
   */
  private static boolean isUseCache(final long serverModMsec, final String serverModVal, final String clientModVal) {
    if (ValUtil.isNull(clientModVal)) {
      return false;
    }
    try {
      // Client-side file modification date
      final ZonedDateTime clientModTime = ZonedDateTime.parse(clientModVal, DTF_HTTP_DATE);
      // Serial value (milliseconds)
      final long clientModMsec = clientModTime.toInstant().toEpochMilli();
      // If the file modification date is not equal to or later than the client-side date (compares considering a 1-second error margin)
      if (serverModMsec <= clientModMsec + 1000) {
        return true;
      }
    } catch (Exception e) {
      // Compares strings on parse error
      if (serverModVal.equals(clientModVal)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Displays redirect.<br>
   * <ul>
   * <li>Redirects to the specified URL.</li>
   * <li>Uses 301 Moved Permanently status.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param url Redirect URL
   * @throws IOException I/O exception error
   */
  static void responseRedirect(final HttpExchange exchange, final String url) throws IOException {
    final Headers headers = exchange.getResponseHeaders();
    setSecurityHeaders(headers);
    headers.set("Location", url);
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, -1);
    // Response body is empty
    exchange.getResponseBody().close();
  }

  /**
   * Gets request full URL.<br>
   * <ul>
   * <li>Builds a complete URL from the current request.</li>
   * <li>Optionally allows path addition or query removal.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @param addPath Additional path
   * @param trimQuery <code>true</code> to remove query
   * @return Full URL
   */
  static String getRequestFullUrl(final HttpExchange exchange, final String addPath,
      final boolean trimQuery) {
    final String protocol = ValUtil.splitReg(exchange.getProtocol().toLowerCase(), "/")[0];
    final String host = exchange.getRequestHeaders().getFirst("Host");
    final URI uri = exchange.getRequestURI();
    final String path = uri.getPath();
    final String query = uri.getQuery();
    
    final StringBuilder urlSb = new StringBuilder();
    urlSb.append(protocol).append("://").append(host).append(path);
    if (!ValUtil.isBlank(addPath)) {
      urlSb.append(addPath);
    }
    if (!ValUtil.isBlank(query) && !trimQuery) {
      urlSb.append("?").append(query);
    }
    return urlSb.toString();
  }

  /**
   * Gets request body (gets POST data).<br>
   * <ul>
   * <li>Gets the body part of the HTTP request as a string.</li>
   * <li>Ensures proper resource closure.</li>
   * </ul>
   *
   * @param exchange HTTP send/receive data
   * @return Request string
   * @throws IOException I/O exception error
   */
  static String getRequestBody(final HttpExchange exchange) throws IOException {
    final StringBuilder sb = new StringBuilder();
    try (final InputStream is = exchange.getRequestBody();
         final BufferedReader br = new BufferedReader(
             new InputStreamReader(is, ValUtil.UTF8))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    }
    return sb.toString();
  }

  /**
   * Sets security headers.
   * 
   * @param headers HTTP send/receive headers
   */
  private static void setSecurityHeaders(final Headers headers) {
    // XSS protection
    headers.set("X-Content-Type-Options", "nosniff");
    headers.set("X-Frame-Options", "DENY");
    headers.set("X-XSS-Protection", "1; mode=block");
        
    // CSP (Content Security Policy)
    headers.set("Content-Security-Policy", 
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
    
    // Referrer control
    headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  /**
   * File response copy or stream response (normal processing).
   * 
   * @param exchange HTTP send/receive data
   * @param resFile  Display file
   */
  private static void responseCopyOrStream(final HttpExchange exchange, final File resFile)
      throws IOException, FileNotFoundException {
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, resFile.length());
    try (final OutputStream os = exchange.getResponseBody()) {
      if (resFile.length() <= OPTIMAL_BUFFER_SIZE) {
        Files.copy(resFile.toPath(), os);
      } else {
        try (final FileInputStream fis = new FileInputStream(resFile)) {
          final byte[] buffer = new byte[OPTIMAL_BUFFER_SIZE];
          int bytesRead;
          while ((bytesRead = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
          }
        }
      }
    }
  }

  /**
   * Compression-supported response.
   * @param exchange HTTP send/receive data
   * @param httpStatus HTTP status code (HttpURLConnection.HTTP_*)
   * @param resTxt Response text
   */
  private static void responseCompressed(final HttpExchange exchange, final int httpStatus, 
        final String resTxt) throws IOException {

    final Headers headers = exchange.getResponseHeaders();
    final String acceptEncoding = ValUtil.nvl(exchange.getRequestHeaders().getFirst("Accept-Encoding"));
    
    byte[] resBytes;
    if (acceptEncoding.contains("gzip") && resTxt.length() > 1024) {
        // GZIP compression
        resBytes = compresseGzip(resTxt.getBytes(ValUtil.UTF8));
        headers.set("Content-Encoding", "gzip");
    } else {
        resBytes = resTxt.getBytes(ValUtil.UTF8);
    }
    
    exchange.sendResponseHeaders(httpStatus, resBytes.length);
    try (final OutputStream os = exchange.getResponseBody()) {
        os.write(resBytes);
    }
  }

  /**
   * GZIP compression processing.
   * @param data Compression target data
   * @return Compressed data
   */
  private static byte[] compresseGzip(final byte[] data) throws IOException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         final GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
        gzos.write(data);
        gzos.finish();
        return baos.toByteArray();
    }
  }
}
