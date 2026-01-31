package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;

/**
 * Resource file utility class.<br>
 * <ul>
 * <li>Reads resource files.</li>
 * <li>The resource file storage directory (hereinafter referred to as resource directory) is the resources
 * directory directly under the application deployment directory and is fixed relative to the application deployment directory. <br>
 * [Example] For application/lib/program.jar, it is under application/resources/. <br>
 * For details on the application deployment directory, refer to <code>PropertiesUtil#APPLICATION_DIR_PATH</code>.</li>
 * <li>Assumes that the character set of resource files is UTF-8.</li>
 * <li>Resource file names are flexible, but the following file names are reserved for framework components and cannot be used. <br>
 * msg.json</li>
 * </ul>
 */
public final class ResourcesUtil {

  /** Resource file character set. */
  private static final CharSet RESRC_FILE_CHAR_SET = CharSet.UTF8;
  /** Resource directory name. */
  private static final String RESRC_DIRNAME = "resources";

  /** Resource directory path. */
  private static final String RESRC_STORAGE_DIR_PATH = FileUtil.joinPath(PropertiesUtil.APPLICATION_DIR_PATH, RESRC_DIRNAME);

  /** Framework-reserved resource file names. */
  public enum FwResourceName {
    /** Resource file name - Framework-reserved message resource file name. */
    MSG("msg.json");

    /** File name. */
    private final String name;

    /**
     * Constructor.
     *
     * @param value File name
     */
    private FwResourceName(final String value) {
      this.name = value;
    }

    @Override
    public String toString() {
      return this.name;
    }

    /**
     * Checks existence.
     *
     * @param name File name
     * @return <code>true</code> if it exists
     */
    private static boolean exists(final String name) {
      for (final FwResourceName fwName : values()) {
        if (fwName.toString().equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Constructor.
   */
  private ResourcesUtil() {
    // No processing
  }

  /**
   * Gets framework-reserved resource JSON.<br>
   * <ul>
   * <li>Reads the JSON file under the resource directory and returns it as a map.</li>
   * <li>The returned map is read-only.</li>
   * </ul>
   *
   * @param resourceName Framework-reserved resource name
   * @return Map
   */
  public static IoItems getJson(final FwResourceName resourceName) {
    return getJsonToIoItems(resourceName.toString());
  }

  /**
   * Gets resource JSON.<br>
   * <ul>
   * <li>Reads the JSON file under the resource directory and returns it as a map.</li>
   * <li>The returned map is read-only.</li>
   * </ul>
   * 
   * @param fileName JSON file name
   * @return Map
   */
  public static IoItems getJson(final String fileName) {
    if (FwResourceName.exists(fileName)) {
      throw new RuntimeException("Framework-reserved resource files must be specified with FwResourceName class constants. " + LogUtil.joinKeyVal("file", fileName));
    }
    return getJsonToIoItems(fileName);
  }
  
  /**
   * Gets map by reading JSON file.
   * @param fileName File name
   * @return Map
   */
  private static IoItems getJsonToIoItems(final String fileName) {
    final String filePath = FileUtil.joinPath(RESRC_STORAGE_DIR_PATH, fileName);
    final StringBuilder sb = new StringBuilder();
    try (final TxtReader tr = new TxtReader(filePath, RESRC_FILE_CHAR_SET);) {
      for (final String line : tr) {
        sb.append(line);
      }
    }
    final IoItems ioMap = new IoItems();
    ioMap.putAllByJson(sb.toString());
    // Read-only map
    return new IoItems(ioMap, true);
  }
}
