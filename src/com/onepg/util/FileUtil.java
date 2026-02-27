package com.onepg.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * File operations utility class.
 */
public final class FileUtil {

  /** Date-time formatter: timestamp for files. */
  private static final DateTimeFormatter DTF_FILE_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Constructor.
   */
  private FileUtil() {
    // No processing
  }

  /**
   * Retrieves the OS temporary directory path.<br>
   * <ul>
   * <li>Specifically, returns the path of the Java java.io.tmpdir system property.</li>
   * </ul>
   *
   * @return the OS temporary directory path
   */
  public static String getOsTemporaryPath() {
    final String tempDir = System.getProperty("java.io.tmpdir");
    return tempDir;
  }

  /**
   * Joins file paths.<br>
   * <ul>
   * <li>Passing blank at the end of arguments returns a path ending with / or \.</li>
   * <li>The separator is the OS-appropriate character.</li>
   * </ul>
   *
   * @param paths file paths (multiple specification)
   * @return the file path
   */
  public static String joinPath(final String... paths) {
    if (ValUtil.isEmpty(paths)) {
      return ValUtil.BLANK;
    }
    
    String ret = new String(paths[0]);
    if (PropertiesUtil.isWindowsOs() && ret.length() == 2 && ret.toString().endsWith(":")) {
      ret += File.separator;
    }
    for (int i = 1; i < paths.length; i++) {
      ret = (new File(ret, paths[i])).getPath();
    }
    if (paths.length > 1 && ValUtil.isBlank(paths[paths.length - 1])) {
      ret += File.separator;
    }
    return ret.toString();
  }

  /**
   * Converts to OS path.<br>
   * <ul>
   * <li>The separator is replaced with the OS-appropriate character.</li>
   * </ul>
   *
   * @param path file path
   * @return the file path
   */
  public static String convOsPath(final String path) {
    return (new File(path)).getPath();
  }

  /**
   * Converts to absolute path.<br>
   * <ul>
   * <li>Relative paths are converted to absolute paths.</li>
   * <li>The separator is replaced with the OS-appropriate character.</li>
   * </ul>
   *
   * @param path relative path
   * @return the absolute path
   */
  public static String convAbsolutePath(final String path) {
    final String[] paths = ValUtil.split(convOsPath(path), File.separator);
    String ret = new String(paths[0]);
    if (PropertiesUtil.isWindowsOs() && ret.length() == 2 && ret.endsWith(":")) {
      ret += File.separator;
    }
    for (int i = 1; i < paths.length; i++) {
      if (".".equals(paths[i])) {
        continue;
      }
      if ("..".equals(paths[i])) {
        if (i == 1) {
          // Skip because there is no parent when the current element is immediately after the root
          continue;
        }
        ret = (new File(ret)).getParentFile().getPath();
        continue;
      }
      ret = (new File(ret, paths[i])).getPath();
    }
    return ret;
  }

  /**
   * Checks if a file exists (directories are also acceptable).
   *
   * @param checkPath path to check
   * @return <code>true</code> if exists
   */
  public static boolean exists(final String checkPath) {
    final Path path = Paths.get(checkPath);
    return Files.exists(path);
  }

  /**
   * Checks if the parent directory exists (directories are also acceptable).
   *
   * @param checkPath path to check
   * @return <code>true</code> if exists
   */
  public static boolean existsParent(final String checkPath) {
    if (ValUtil.isBlank(checkPath)) {
      return false;
    }
    final String parentPath = getParentPath(checkPath);
    return (!ValUtil.isNull(parentPath)) && exists(parentPath);
  }

  /**
   * Retrieves the file name from the full path (directories are also acceptable).
   *
   * @param fullPath full path
   * @return the file name only
   */
  public static String getFileName(final String fullPath) {
    return Paths.get(fullPath).getFileName().toString();
  }

  /**
   * Retrieves the parent directory path from the full path (directories are also acceptable).
   *
   * @param fullPath full path
   * @return the parent directory path
   */
  public static String getParentPath(final String fullPath) {
    return Paths.get(fullPath).getParent().toString();
  }

  /**
   * Retrieves the file modified date-time.
   *
   * @param fullPath full path
   * @return the file modified date-time (yyyyMMddHHmmss)
   */
  public static String getFileModifiedDateTime(final String fullPath) {
    final File file = new File(fullPath);
    final long lastModified = file.lastModified();
    final Instant lastInst = Instant.ofEpochMilli(lastModified);
    final LocalDateTime ldt = LocalDateTime.ofInstant(lastInst, ZoneId.systemDefault());
    final String ret = DTF_FILE_TIMESTAMP.format(ldt);
    return ret;
  }

  /**
   * Splits file name or full path into extension and the rest.
   *
   * @param fileName file name or full path
   * @return the string array {part before extension, extension} (neither includes the dot)
   */
  public static String[] splitFileTypeMark(final String fileName) {
    final int markIdx = fileName.lastIndexOf(".");
    final String[] ret = new String[2];

    if (markIdx <= 0) {
      // Files without extension or starting with a dot are returned without extension.
      ret[0] = fileName;
      ret[1] = ValUtil.BLANK;
      return ret;
    }
    ret[0] = fileName.substring(0, markIdx);
    ret[1] = fileName.substring(markIdx + 1);
    return ret;
  }

  /**
   * Retrieves the file path (absolute path) list.<br>
   * <ul>
   * <li>File name and extension search strings are case-insensitive.</li>
   * </ul>
   *
   * @param dirPath     target directory path
   * @param typeMark    search extension (optional) <code>null</code> if omitted ※ dot character not required
   * @param prefixMatch search file name prefix match (optional) <code>null</code> if omitted
   * @param middleMatch search file name middle match (optional) <code>null</code> if omitted
   * @param suffixMatch search file name suffix match (optional) <code>null</code> if omitted
   * @return the file path (absolute path) list
   * @throws RuntimeException if the directory does not exist
   */
  public static List<String> getFileList(final String dirPath, final String typeMark,
      final String prefixMatch, final String middleMatch, final String suffixMatch) {
    final File parentDir = new File(dirPath);
    if (!parentDir.exists()) {
      throw new RuntimeException("Directory does not exist. " + LogUtil.joinKeyVal("path", dirPath));
    }

    final File[] files;
    if (ValUtil.isBlank(typeMark) && ValUtil.isBlank(prefixMatch) && ValUtil.isBlank(middleMatch)
        && ValUtil.isBlank(suffixMatch)) {
      // No conditions
      files = parentDir.listFiles();
    } else {
      // With conditions
      final String typeMarkL = ValUtil.nvl(typeMark).toLowerCase();
      final String prefixMatchL = ValUtil.nvl(prefixMatch).toLowerCase();
      final String middleMatchL = ValUtil.nvl(middleMatch).toLowerCase();
      final String suffixMatchL = ValUtil.nvl(suffixMatch).toLowerCase();
      final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
          final String[] names = FileUtil.splitFileTypeMark(name);
          names[0] = names[0].toLowerCase();
          names[1] = names[1].toLowerCase();
          if (!ValUtil.isBlank(typeMarkL) && !names[1].equals(typeMarkL)) {
            // Extension does not match
            return false;
          }
          if (!ValUtil.isBlank(prefixMatchL) && !names[0].startsWith(prefixMatchL)) {
            // File name does not match prefix
            return false;
          }
          if (!ValUtil.isBlank(middleMatchL) && !names[0].contains(middleMatchL)) {
            // File name does not match middle string
            return false;
          }
          if (!ValUtil.isBlank(suffixMatchL) && !names[0].endsWith(suffixMatchL)) {
            // File name does not match suffix
            return false;
          }
          // Matches the specified conditions
          return true;
        }
      };
      files = parentDir.listFiles(filter);
    }

    final List<String> retList = new ArrayList<>();
    if (ValUtil.isNull(files)) {
      return retList;
    }
    for (final File file : files) {
      retList.add(file.getAbsolutePath());
    }
    return retList;
  }

  /**
   * Moves a file.
   *
   * @see #move(File, File)
   * @param srcFilePath source file path
   * @param destFilePath destination file path (directory specification allowed)
   * @return the destination file object
   */
  public static File move(final String srcFilePath, final String destFilePath) {
    return move(new File(srcFilePath), new File(destFilePath));
  }

  /**
   * Moves a file.<br>
   * <ul>
   * <li>If the destination is a directory specification, the file name will be the same as the source.</li>
   * </ul>
   *
   * @param srcFile source file
   * @param destFile destination file (directory specification allowed)
   * @return the destination file object
   * @throws IllegalArgumentException if the destination file already exists
   * @throws IllegalStateException if file move fails
   */
  public static File move(final File srcFile, final File destFile) {
    final Path srcPath = Paths.get(srcFile.getAbsolutePath());
    final Path destPath = resolveDestinationPath(srcFile, destFile);
    
    if (destPath.toFile().exists()) {
      throw new RuntimeException("Destination file already exists. "
                              + LogUtil.joinKeyVal("path", destPath.toString()));
    }
    try {
      Files.move(srcPath, destPath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file move. " + LogUtil.joinKeyVal("src",
          srcFile.getAbsolutePath(), "dest", destFile.getAbsolutePath()), e);
    }
    return destPath.toFile();
  }

  /**
   * Copies a file.
   *
   * @see #copy(File, File)
   * @param srcFilePath source file path
   * @param destFilePath destination file path (directory specification allowed)
   * @return the destination file object
   */
  public static File copy(final String srcFilePath, final String destFilePath) {
    return copy(new File(srcFilePath), new File(destFilePath));
  }

  /**
   * Copies a file.<br>
   * <ul>
   * <li>If the destination is a directory specification, the file name will be the same as the source.</li>
   * </ul>
   *
   * @param srcFile source file
   * @param destFile destination file (directory specification allowed)
   * @return the destination file object
   * @throws IllegalArgumentException if the destination file already exists
   * @throws IllegalStateException if file copy fails
   */
  public static File copy(final File srcFile, final File destFile) {
    final Path srcPath = Paths.get(srcFile.getAbsolutePath());
    final Path destPath = resolveDestinationPath(srcFile, destFile);
    
    if (destPath.toFile().exists()) {
      throw new RuntimeException("Destination file already exists. "
                               + LogUtil.joinKeyVal("path", destPath.toString()));
    }
    try {
      Files.copy(srcPath, destPath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file copy. " + LogUtil.joinKeyVal("src",
          srcFile.getAbsolutePath(), "dest", destFile.getAbsolutePath()), e);
    }
    return destPath.toFile();
  }

  /**
   * Resolves the move/copy destination path.
   */
  private static Path resolveDestinationPath(final File srcFile, final File destFile) {
    if (destFile.isDirectory()) {
      return Paths.get(FileUtil.joinPath(destFile.getAbsolutePath(), srcFile.getName()));
    } else {
      return Paths.get(destFile.getAbsolutePath());
    }
  }

  /**
   * Deletes a file.<br>
   * <ul>
   * <li>Returns <code>false</code> if the file does not exist.</li>
   * </ul>
   *
   * @param deleteFilePath file path to delete
   * @return <code>false</code> if the file does not exist
   * @throws IllegalStateException if file deletion fails
   */
  public static boolean delete(final String deleteFilePath) {
    final Path deletePath = Paths.get(deleteFilePath);
    if (!deletePath.toFile().exists()) {
      return false;
    }
    try {
      Files.delete(deletePath);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in file deletion. "
                                + LogUtil.joinKeyVal("path", deleteFilePath), e);
    }
    return true;
  }

  /**
   * Deletes a file.
   *
   * @see #delete(String)
   * @param deleteFile file to delete
   * @return <code>false</code> if the file does not exist
   */
  public static boolean delete(final File deleteFile) {
    return delete(deleteFile.getAbsolutePath());
  }

  /**
   * Creates a directory.
   *
   * @param dirPath directory path
   * @return <code>false</code> if it already exists
   * @throws IllegalStateException if directory creation fails
   */
  public static boolean makeDir(final String dirPath) {
    final Path path = Paths.get(dirPath);
    if (Files.exists(path)) {
      return false;
    }
    try {
      // Also creates parent directories if they do not exist
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new RuntimeException("Exception error occurred in directory creation. " + LogUtil.joinKeyVal("path", dirPath),
          e);
    }
    return true;
  }

}
