package com.onepg.util;

import com.onepg.util.ValUtil.CharSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Properties file utility class.
 * <ul>
 * <li>Properties file extension shall be .properties. (Others are ignored.)</li>
 * <li>Properties file storage directory (hereinafter, configuration directory) is the config directory directly under the application deployment directory, fixed relative to the application deployment directory.
 * <br>[Example] For application/lib/program.jar, it is under application/config/.
 * <br>For details of the application deployment directory, see <code>#APPLICATION_DIR_PATH</code>.</li>
 * <li>Normally, the configuration directory path is as above, but to change the path, specify the path with the property key CONFIG_DIR in config/config.properties.</li>
 * <li>Properties file character set is assumed to be UTF-8.</li>
 * <li>Properties file content lists property keys and values as follows:
 * <br>KEY1=VAL1
 * <br>KEY2=VAL2
 * <br>KEY3=VAL3</li>
 * <li>Properties file names are free, but the following file names are reserved for framework components and cannot be used:
 * <br>web.properties
 * <br>bat.properties
 * <br>db.properties
 * <br>config.properties</li>
 * <li>The number of properties files is free, but property keys must be unique across properties files. (Duplication with framework component properties files is acceptable.)</li>
 * <li>If a value is enclosed in ${ and }, it is replaced with the value of the enclosed environment variable. (Partial replacement is also possible.)
 * <br>However, if it does not exist in environment variables, a system error occurs.</li>
 * <li>$ApplicationDirPath within a value is replaced with the application deployment directory path. (Partial replacement is also possible.)
 * <br>For details of the application deployment directory, see <code>#APPLICATION_DIR_PATH</code>.</li>
 * <li>$TemporaryDirPath within a value is replaced with the OS temporary directory path. (Partial replacement is also possible.)
 * <br>Specifically, it is replaced with the path of Java's java.io.tmpdir system property.</li>
 * <li>Values starting with &lt;ConvertAbsolutePath&gt; are converted to absolute paths.</li>
 * <li>Lines starting with # in properties files are ignored as comments. (Specification of <code>Properties#load(java.io.InputStream)</code>.)</li>
 * <li>This class does not handle writing to properties files.</li>
 * </ul>
 */
public final class PropertiesUtil {

  /** Properties file character set. */
  private static final String PROP_FILE_CHAR_SET = CharSet.UTF8.toString();
  /** Default configuration directory name. */
  private static final String DEFAULT_PROP_DIRNAME = "config";
  /** Property key - configuration directory specification. */
  private static final String PROPDIR_PKEY = "config.dir";
  /** Properties file extension. */
  private static final String PROPERTIES_TYPEMARK = "properties";

  /** Application deployment directory path replacement character. */
  private static final String REPLACE_APPLICATION_DIR_PATH = "$ApplicationDirPath";
  /** Temporary directory path replacement character. */
  private static final String REPLACE_TEMPORARY_DIR_PATH = "$TemporaryDirPath";
  /** Absolute path conversion directive. */
  private static final String CONVERT_ABSOLUTE_PATH = "<ConvertAbsolutePath>";
  /** Environment variable replacement pattern. */
  private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

  /** Framework-reserved properties file names. */
  public enum FwPropertiesName {
    /** Properties file name - framework-reserved web server configuration file name. */
    WEB("web.properties"),
    /** Properties file name - framework-reserved batch processing configuration file name. */
    BAT("bat.properties"),
    /** Properties file name - framework-reserved DB configuration file name. */
    DB("db.properties"),
    /** Properties file name - framework-reserved log configuration file name. */
    LOG("log.properties"),
    /** Properties file name - framework-reserved configuration directory specification file. */
    PROPDIR("config.properties");

    /** File name. */
    private final String name;

    /**
     * Constructor.
     *
     * @param value the file name
     */
    private FwPropertiesName(final String value) {
      this.name = value;
    }

    @Override
    public String toString() {
      return this.name;
    }

    /**
     * Checks existence.
     *
     * @param name the file name
     * @return <code>true</code> if exists
     */
    private static boolean exists(final String name) {
      for (final FwPropertiesName fwName : values()) {
        if (fwName.toString().equalsIgnoreCase(name)) {
          return true;
        }
      }
      return false;
    }
  }

  /** Local host name. */
  public static final String LOCALHOST_NAME = getLocalHostName();

  /**
   * Application deployment directory path.
   * <ul>
   * <li>Application deployment directory refers to one of the following directories:
   * <ul>
   * <li>Two levels up from the Java class file deployment root directory (com, jp, etc.)
   * <br>[Example] For appdeploy/classes/com/onepg/Program.class, it is appdeploy</li>
   * <li>Two levels up from the Jar file deployment directory
   * <br>[Example] For appdeploy/lib/program.jar, it is appdeploy</li>
   * </ul>
   * </ul>
   */
  public static final String APPLICATION_DIR_PATH;
  /** Temporary directory path. */
  private static final String TEMPORARY_DIR_PATH;

  /** Configuration directory path. */
  private static final String PROP_STORAGE_DIR_PATH;

  /**
   * Module properties.
   * <ul>
   * <li>Returns property values from properties files under the configuration directory as a map.</li>
   * <li>Excludes framework-reserved properties files.</li>
   * <li>The number of properties files is free, but property keys must be unique across properties files.</li>
   * </ul>
   * <pre>[Example] <code>final String value = PropertiesUtil.MODULE_PROP_MAP.getString("module.unique.key");</code></pre>
   */
  public static final IoItems MODULE_PROP_MAP;

  static {
    // Initializes in the static block to make the processing order explicit.
    APPLICATION_DIR_PATH = getJavaClassParentPath();
    TEMPORARY_DIR_PATH = FileUtil.getOsTemporaryPath();
    PROP_STORAGE_DIR_PATH = getPropDir();
    MODULE_PROP_MAP = getModulePorps();
  }

  /**
   * Gets the configuration directory.
   *
   * @return the configuration directory path
   */
  private static String getPropDir() {
    // Default configuration directory path
    final String defaultDirPath = FileUtil.joinPath(APPLICATION_DIR_PATH, DEFAULT_PROP_DIRNAME);
    // Configuration directory specification file path
    final String propDirFilePath = FileUtil.joinPath(defaultDirPath, FwPropertiesName.PROPDIR.toString());
    
    if (FileUtil.exists(propDirFilePath)) {
      // Default configuration
      final IoItems propMap = getPropertiesMap(propDirFilePath);
      // Configuration directory path
      final String path = propMap.getString(PROPDIR_PKEY);
      if (!FileUtil.exists(path)) {
        throw new RuntimeException("Configuration directory does not exist. " + LogUtil.joinKeyVal("path", path));
      }
      return path;
    } else {
      // If configuration directory specification file does not exist, uses default configuration directory as configuration directory path.
      return defaultDirPath;
    }
  }

  /**
   * Gets module properties.
   *
   * @return the module properties map
   */
  private static IoItems getModulePorps() {
    final IoItems allPropMap = new IoItems();
    final List<String> fileList =
        FileUtil.getFileList(PROP_STORAGE_DIR_PATH, PROPERTIES_TYPEMARK, null, null, null);
    for (final String filePath : fileList) {
      if (FwPropertiesName.exists(FileUtil.getFileName(filePath))) {
        // Excludes framework-reserved properties files
        continue;
      }
      final IoItems propMap = getPropertiesMap(filePath);
      allPropMap.putAll(propMap);
    }
    // Read-only map
    return new IoItems(allPropMap, true);
  }

  /**
   * Constructor.
   */
  private PropertiesUtil() {
    // No processing
  }

  /**
   * Returns the local host name.
   *
   * @return the host name
   */
  private static String getLocalHostName() {
    try {
      final String hostName = InetAddress.getLocalHost().getHostName();
      return hostName;
    } catch (UnknownHostException e) {
      throw new RuntimeException("An exception occurred while getting the local host name. ", e);
    }
  }

  /**
   * Returns framework properties.
   *
   * @param propFileName the properties file name
   * @return the property value map
   */
  public static IoItems getFrameworkProps(final FwPropertiesName propFileName) {
    if (!FwPropertiesName.exists(propFileName.toString())) {
      throw new RuntimeException("Not a framework-reserved properties file name. " + LogUtil.joinKeyVal("file", propFileName.toString()));
    }
    final String propFilePath = FileUtil.joinPath(PROP_STORAGE_DIR_PATH, propFileName.toString());
    if (!FileUtil.exists(propFilePath)) {
      throw new RuntimeException("Properties file does not exist. " + LogUtil.joinKeyVal("file", propFilePath));
    }
    final IoItems propMap = getPropertiesMap(propFilePath);
    return propMap;
  }

  /**
   * Returns the property value map.
   * <ul>
   * <li>The returned map is read-only.</li>
   * </ul>
   *
   * @param propFilePath the properties file path
   * @return the property value map (read-only)
   */
  private static IoItems getPropertiesMap(final String propFilePath) {
    // Properties instance
    final Properties props = getPropertiesObj(propFilePath);
    final IoItems retMap = new IoItems();
    for (Map.Entry<Object, Object> ent : props.entrySet()) {
      final String key = (String) ent.getKey();
      final String val = (String) ent.getValue();
      if (retMap.containsKey(key)) {
        final String val2 = retMap.getString(key);
        // Throws error if property key is duplicated
        throw new RuntimeException("Property key is duplicated. "
            + LogUtil.joinKeyVal("file", propFilePath, "key", key, "value1", val, "value2", val2));
      }
      final String convVal = convDirPath(convEnv(val, propFilePath, key));
      retMap.put(key, convVal);
    }
    return new IoItems(retMap, true);
  }

  /**
   * Returns the Properties instance.
   *
   * @param propFilePath the properties file path
   * @return the Properties instance
   */
  private static Properties getPropertiesObj(final String propFilePath) {
    final File propFile = new File(propFilePath);
    if (!propFile.exists()) {
      throw new RuntimeException("Properties file does not exist. " + LogUtil.joinKeyVal("path", propFilePath));
    }
    final Properties props = new Properties();
    try (final FileInputStream fis = new FileInputStream(propFile);
        final InputStreamReader isr = new InputStreamReader(fis, PROP_FILE_CHAR_SET);) {
      props.load(isr);
    } catch (IOException e) {
      throw new RuntimeException("An exception occurred while reading the properties file. " + LogUtil.joinKeyVal("file", propFilePath), e);
    }
    return props;
  }

  /**
   * Replaces environment variables in property values.
   * <ul>
   * <li>If a value is enclosed in ${ and }, it is replaced with the value of the enclosed environment variable. (Partial replacement is also possible.)
   * <br>However, if it does not exist in environment variables, a system error occurs.</li>
   * </ul>
   *
   * @param value the property value
   * @param filePath the properties file path (for error log)
   * @param key the property key (for error log)
   * @return the property value
   */
  private static String convEnv(final String value, final String filePath, final String key) {
    if (!value.contains("${") || !value.contains("}")) {
      return value;
    }

    final Matcher mt = ENV_VAR_PATTERN.matcher(value);

    final StringBuilder sb = new StringBuilder();
    int end = 0;
    while (mt.find()) {
      // Specifies group 1 to get only the environment variable name inside ${}
      final String envKey = mt.group(1);  
      String envVal = System.getenv(envKey);
      if (ValUtil.isNull(envVal)) {
        throw new RuntimeException("Environment variable does not exist. " + LogUtil.joinKeyVal("file", filePath, "key", key, "envKey", envKey));
      }
      // Windows cannot declare environment variables with zero-byte blank, so declares with ""; removes trailing double quotes
      envVal = ValUtil.trimDq(envVal);
      final int start = mt.start();
      sb.append(value.substring(end, start));
      sb.append(envVal);
      end = mt.end();
    }
    sb.append(value.substring(end));
    return sb.toString();
  }

  /**
   * Replaces directory paths in property values.
   * <ul>
   * <li>$ApplicationDirPath within a value is replaced with the application deployment directory path. (Partial replacement is also possible.)
   * <br>For details of the application deployment directory, see <code>#APPLICATION_DIR_PATH</code>.</li>
   * <li>$TemporaryDirPath within a value is replaced with the OS temporary directory path. (Partial replacement is also possible.)
   * <br>Specifically, it is replaced with the path of Java's java.io.tmpdir system property.</li>
   * <li>Values starting with &lt;ConvertAbsolutePath&gt; are converted to absolute paths.</li>
   * </ul>
   *
   * @param value the property value
   * @return the replaced property value
   */
  private static String convDirPath(final String value) {
    String retVal = value;
    retVal = retVal.replace(REPLACE_APPLICATION_DIR_PATH, APPLICATION_DIR_PATH);
    retVal = retVal.replace(REPLACE_TEMPORARY_DIR_PATH, TEMPORARY_DIR_PATH);
    if (value.startsWith(CONVERT_ABSOLUTE_PATH)) {
      retVal = retVal.substring(CONVERT_ABSOLUTE_PATH.length());
      retVal = FileUtil.convAbsolutePath(retVal);
    }
    return retVal;
  }
  
  /**
   * Returns the Java class file parent directory path.
   * <ul>
   * <li>Returns the path one level up from the Java class file deployment directory.
   * <br>Or returns the path one level up from the Jar file deployment directory.</li>
   * </ul>
   *
   * @return the binary file parent directory path
   * @throws IllegalStateException if failed to get the class file path
   */
  private static String getJavaClassParentPath() {
    final ProtectionDomain pd = FileUtil.class.getProtectionDomain();
    final CodeSource cs = pd.getCodeSource();
    final URL location = cs.getLocation();
    File file;
    try {
      file = new File(location.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("An exception occurred while getting the parent directory path of the running Java class file. "
          + LogUtil.joinKeyVal("location", location.toString()), e);
    }

    final String rootPath;
    if (file.isDirectory()) {
      // For class file
      rootPath = file.getParent();
    } else {
      // For jar file
      rootPath = file.getParentFile().getParent();
    }
    return rootPath;
  }

  /**
   * Determines if the OS is MS-Windows.
   *
   * @return <code>true</code> if Windows OS
   */
  public static boolean isWindowsOs() {
    return ValUtil.nvl(System.getProperty("os.name")).toLowerCase().startsWith("windows");
  }

}
