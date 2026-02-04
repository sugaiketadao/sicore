package com.onepg.util;

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
 * Properties file utility class.<br>
 * <ul>
 * <li>The properties file extension is .properties. (Others are ignored)</li>
 * <li>The properties file storage directory (hereinafter, configuration directory) is the config
 * directory directly under the application deployment directory, and is fixed relative to the application deployment directory. <br>
 * [Example] For application/lib/program.jar, it is under application/config/. <br>
 * For details on the application deployment directory, refer to <code>#APPLICATION_DIR_PATH</code>.</li>
 * <li>Normally, the configuration directory path is as described above, but if you want to change the path, specify the path with the configuration key
 * CONFIG_DIR in config/config.properties.</li>
 * <li>The character set of the properties file is assumed to be UTF-8.</li>
 * <li>The content of the properties file lists configuration keys and values as shown in the example below. <br>
 * KEY1=VAL1 <br>
 * KEY2=VAL2 <br>
 * KEY3=VAL3</li>
 * <li>The properties file name is flexible, but the following file names are reserved for framework components and cannot be used. <br>
 * web.properties <br>
 * bat.properties <br>
 * db.properties <br>
 * config.properties</li>
 * <li>The number of properties files is flexible, but configuration keys must be unique across properties files. (Duplication with framework component properties files is acceptable)</li>
 * <li>If the configuration value is enclosed in ${ and }, it is replaced with the value of the enclosed environment variable. (Partial replacement is also possible) <br>
 * However, if the environment variable does not exist, a system error occurs.</li>
 * <li>$ApplicationDirPath in the configuration value is replaced with the application deployment directory path. (Partial replacement is also possible)<br>
 * For details on the application deployment directory, refer to <code>#APPLICATION_DIR_PATH</code>.</li>
 * <li>$TemporaryDirPath in the configuration value is replaced with the OS temporary directory path. (Partial replacement is also possible)<br>
 * Specifically, it replaces the path of Java's java.io.tmpdir system property.</li>
 * <li>Configuration values starting with &lt;ConvertAbsolutePath&gt; are converted to absolute paths.</li>
 * <li>Lines starting with # in the properties file
 * are ignored as comments. (Specification of <code>Properties#load(java.io.InputStream)</code>)</li>
 * <li>This class does not handle writing to properties files.</li>
 * </ul>
 */
public final class PropertiesUtil {

  /** Properties file character set. */
  private static final String PROP_FILE_CHAR_SET = ValUtil.UTF8;
  /** Default configuration directory name. */
  private static final String DEFAULT_PROP_DIRNAME = "config";
  /** Configuration key - configuration directory specification. */
  private static final String PROPDIR_PKEY = "config.dir";
  /** Properties file extension. */
  private static final String PROPERTIES_TYPEMARK = "properties";

  /** Application deployment directory path replacement character. */
  private static final String REPLACE_APPLICATION_DIR_PATH = "$ApplicationDirPath";
  /** Temporary directory path replacement character. */
  private static final String REPLACE_TEMPORARY_DIR_PATH = "$TemporaryDirPath";
  /** Absolute path conversion instruction. */
  private static final String CONVERT_ABSOLUTE_PATH = "<ConvertAbsolutePath>";
  /** Environment variable replacement pattern. */
  private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

  /** Framework-reserved properties file name. */
  public enum FwPropertiesName {
    /** Properties file name - framework-reserved web server properties file name. */
    WEB("web.properties"),
    /** Properties file name - framework-reserved batch processing properties file name. */
    BAT("bat.properties"),
    /** Properties file name - framework-reserved database properties file name. */
    DB("db.properties"),
    /** Properties file name - framework-reserved log properties file name. */
    LOG("log.properties"),
    /** Properties file name - framework-reserved configuration directory specification file. */
    PROPDIR("config.properties");

    /** File name. */
    private final String name;

    /**
     * Constructor.
     *
     * @param value file name
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
     * @param name file name
     * @return <code>true</code> if it exists
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
   * Application deployment directory path.<br>
   * <ul>
   * <li>The application deployment directory refers to one of the following directories.
   * <ul>
   * <li>Two directories above the Java class file deployment root directory (such as com or jp) <br>
   * [Example] For appdeploy/classes/com/onepg/Program.class, it is appdeploy</li>
   * <li>Two directories above the Jar file deployment directory <br>
   * [Example] For appdeploy/lib/program.jar, it is appdeploy</li>
   * </ul>
   * </ul>
   */
  public static final String APPLICATION_DIR_PATH;
  /** Temporary directory path. */
  private static final String TEMPORARY_DIR_PATH;

  /** Configuration directory path. */
  private static final String PROP_STORAGE_DIR_PATH;

  /**
   * Module configuration.<br>
   * <ul>
   * <li>Returns configuration values from properties files under the configuration directory as a map.</li>
   * <li>Excludes framework-reserved properties files.</li>
   * <li>The number of properties files is flexible, but configuration keys must be unique across properties files.</li>
   * </ul>
   * <pre>[Example] <code>final String value = PropertiesUtil.MODULE_PROP_MAP.getString("module.unique.key");</code></pre>
   */
  public static final IoItems MODULE_PROP_MAP;

  static {
    // Initializes in the static block to make the processing order explicit
    APPLICATION_DIR_PATH = getJavaClassParentPath();
    TEMPORARY_DIR_PATH = FileUtil.getOsTemporaryPath();
    PROP_STORAGE_DIR_PATH = getPropDir();
    MODULE_PROP_MAP = getModulePorps();
  }

  /**
   * Gets the configuration directory.
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
      // If the configuration directory specification file does not exist, uses the default configuration directory as the configuration directory path.
      return defaultDirPath;
    }
  }

  /**
   * Gets module configuration.
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
   * Gets the local host name.
   *
   * @return host name
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
   * Gets framework configuration.
   *
   * @param propFileName properties file name
   * @return configuration value map
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
   * Gets the configuration value map.<br>
   * <ul>
   * <li>The returned map is read-only.</li>
   * </ul>
   *
   * @param propFilePath properties file path
   * @return configuration value map (read-only)
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
        // Error if property key is duplicated
        throw new RuntimeException("Property key is duplicated. "
            + LogUtil.joinKeyVal("file", propFilePath, "key", key, "value1", val, "value2", val2));
      }
      final String convVal = convDirPath(convEnv(val, propFilePath, key));
      retMap.put(key, convVal);
    }
    return new IoItems(retMap, true);
  }

  /**
   * Gets the Properties instance.
   *
   * @param propFilePath properties file path
   * @return Properties instance
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
   * Replaces configuration value environment variables.<br>
   * <ul>
   * <li>If the configuration value is enclosed in ${ and }, it is replaced with the value of the enclosed environment variable. (Partial replacement is also possible) <br>
   * However, if the environment variable does not exist, a system error occurs.</li>
   * </ul>
   *
   * @param value configuration value
   * @param filePath properties file path (for error log)
   * @param key configuration key (for error log)
   * @return configuration value
   */
  private static String convEnv(final String value, final String filePath, final String key) {
    if (!value.contains("${") || !value.contains("}")) {
      return value;
    }

    final Matcher mt = ENV_VAR_PATTERN.matcher(value);

    final StringBuilder sb = new StringBuilder();
    int end = 0;
    while (mt.find()) {
      // Gets only the environment variable name inside ${} brackets by specifying group 1
      final String envKey = mt.group(1);  
      String envVal = System.getenv(envKey);
      if (ValUtil.isNull(envVal)) {
        throw new RuntimeException("Environment variable does not exist. " + LogUtil.joinKeyVal("file", filePath, "key", key, "envKey", envKey));
      }
      // Removes trailing double quotations because Windows cannot declare environment variables with zero-byte blanks and declares them with ""
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
   * Replaces configuration value directory paths.<br>
   * <ul>
   * <li>$ApplicationDirPath in the configuration value is replaced with the application deployment directory path. (Partial replacement is also possible)<br>
   * For details on the application deployment directory, refer to <code>#APPLICATION_DIR_PATH</code>.</li>
   * <li>$TemporaryDirPath in the configuration value is replaced with the OS temporary directory path. (Partial replacement is also possible)<br>
   * Specifically, it replaces the path of Java's java.io.tmpdir system property.</li>
   * <li>Configuration values starting with &lt;ConvertAbsolutePath&gt; are converted to absolute paths.</li>
   * </ul>
   *
   * @param value configuration value
   * @return replaced configuration value
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
   * Gets the Java class file parent directory path.<br>
   * <ul>
   * <li>Returns the directory path one level above the Java class file deployment directory.<br>
   * Or returns the directory path one level above the Jar file deployment directory.</li>
   * </ul>
   *
   * @return binary file parent directory path
   * @throws IllegalStateException if getting the class file path fails
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
      // For class files
      rootPath = file.getParent();
    } else {
      // For jar files
      rootPath = file.getParentFile().getParent();
    }
    return rootPath;
  }

  /**
   * Checks MS-Windows OS.
   *
   * @return <code>true</code> if Windows OS
   */
  public static boolean isWindowsOs() {
    return ValUtil.nvl(System.getProperty("os.name")).toLowerCase().startsWith("windows");
  }

}
