package com.onepg.web;

import com.onepg.util.FileUtil;
import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.ValUtil.CharSet;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.net.HttpURLConnection;

/**
 * Static file handler class.
 * @hidden
 */
final class StaticFileHandler extends AbstractHttpHandler {

  /** Static file encoding. */
  private static final CharSet STATIC_FILE_CHARSET = CharSet.UTF8;

  /** Server deployment directory path. */
  private final String serverDeployPath;
  
  /**
   * Constructor.
   */
  StaticFileHandler() {
    super();
    this.serverDeployPath = PropertiesUtil.APPLICATION_DIR_PATH;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final HttpExchange exchange) throws Exception {
    // Request file
    final String reqPath = exchange.getRequestURI().getPath();
    final String reqFilePath;

    if (reqPath.endsWith("/")) {
      // Displays index.html if ending with slash
      reqFilePath = FileUtil.joinPath(this.serverDeployPath, reqPath, "index.html");
    } else {
      reqFilePath = FileUtil.joinPath(this.serverDeployPath, reqPath);
    }
    final File reqFile = new File(reqFilePath);

    // Adds path traversal check before file access in doExecute
    final String canonicalPath = reqFile.getCanonicalPath();
    if (!canonicalPath.startsWith(this.serverDeployPath)) {
      super.logger.error("Path traversal attack detected. " + LogUtil.joinKeyVal("request", reqPath));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN,
          "Access is invalid. " + LogUtil.joinKeyVal("filename", reqFile.getName()));
      return;
    }

    // Checks access file validity
    if (!checkAccessFile(reqFilePath)) {
      super.logger.error("File is not accessible. " + LogUtil.joinKeyVal("request", reqPath));
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_FORBIDDEN,
          "Access is invalid. " + LogUtil.joinKeyVal("filename", reqFile.getName()));
      return;
    }

    if (!reqFile.exists()) {
      // Returns error if file is not found
      ServerUtil.responseText(exchange, HttpURLConnection.HTTP_NOT_FOUND,
          "File does not exist. " + LogUtil.joinKeyVal("requestPath", reqPath));
      return;
    }

    if (reqFile.isDirectory()) {
      // Redirects with slash added to the original URL if directory
      final String reqlUrl = ServerUtil.getRequestFullUrl(exchange, "/", false);
      if (super.logger.isDevelopMode()) {
        super.logger.develop("Directory specified, redirecting. "
                + LogUtil.joinKeyVal("request", reqPath)
                + LogUtil.joinKeyVal("redirect", reqlUrl));
      }
      ServerUtil.responseRedirect(exchange, reqlUrl);
      return;
    }
    if (super.logger.isDevelopMode()) {
      super.logger.develop("Static file accessed. " + LogUtil.joinKeyVal("path", reqPath));
    }
    // Returns file as is (statically)
    ServerUtil.responseFile(exchange, reqFile, STATIC_FILE_CHARSET);
  }

  /**
   * Checks access file validity.
   * 
   * @param filePath File path
   */
  private boolean checkAccessFile(final String filePath) {
    final String fileName = new File(filePath).getName().toLowerCase();
    // Prohibits access to hidden files and configuration files
    // Prohibits access to executable files
    // Prohibits access to backup files
    if (fileName.startsWith(".")
        || fileName.endsWith(".xml")
        || fileName.endsWith(".conf")
        || fileName.endsWith(".properties")
        || fileName.endsWith(".json")
        || fileName.endsWith(".log")
        || fileName.endsWith(".exe")
        || fileName.endsWith(".bat")
        || fileName.endsWith(".sh")
        || fileName.endsWith(".com")
        || fileName.endsWith(".class")
        || fileName.endsWith(".bak")
        || fileName.endsWith(".old")
        || fileName.endsWith(".org")
        || fileName.endsWith(".backup")
        || fileName.endsWith("~")
        || fileName.contains(".backup.")) {
      return false;
    }
    
    return true;
  }
}
