package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.PropertiesUtil;
import com.onepg.util.PropertiesUtil.FwPropertiesName;
import com.onepg.util.IoItems;
import com.onepg.util.ValUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Database utility class.
 */
public final class DbUtil {

  /** DBMS name. */
  enum DbmsName {
    /** PostgreSQL. */
    POSTGRESQL("PostgreSQL"),
    /** Oracle. */
    ORACLE("Oracle"),
    /** MS-SqlServer. */
    MSSQL("Microsoft SQL Server"),
    /** SQLite. */
    SQLITE("SQLite"),
    /** DB2. */
    DB2("DB2"),
    /** Other. */
    ETC("-");

    /** Product name. */
    private final String productName;

    /**
     * Constructor.
     *
     * @param value Product name
     */
    private DbmsName(final String value) {
      this.productName = value;
    }

    @Override
    public String toString() {
      return this.productName;
    }
  }


  /** Default database connection name (part before .dbcon.url). */
  private static final String DEFAULT_CONN_NAME = "default";

  /** Database connection - URL suffix. */
  private static final String PKEY_SUFFIX_URL = ".conn.url";
  /** Database connection - User suffix. */
  private static final String PKEY_SUFFIX_USER = ".conn.user";
  /** Database connection - Password suffix. */
  private static final String PKEY_SUFFIX_PASS = ".conn.pass";
  /** Database connection - Max connections (pool size) suffix. */
  private static final String PKEY_SUFFIX_MAX = ".conn.max";

  /** Database configuration. */
  private static final IoItems PROP_MAP;
  /** SQL execution elapsed time for warning output. */
  static final long SQL_EXEC_WARN_TIME_MSEC;

  static {
    // Get database configuration
    PROP_MAP = PropertiesUtil.getFrameworkProps(FwPropertiesName.DB);
    SQL_EXEC_WARN_TIME_MSEC = PROP_MAP.getLongOrDefault("sqlexec.warn.time", -1);
  }

  /**
   * Connection pool management map <database connection name, connection pool map <connection serial code, database connection>> (singleton).<br>
   * <ul>
   * <li>Holds a map per database connection name to handle multiple connections simultaneously.</li>
   * <li>Internal connection pool map uses thread-safe class.</li>
   * <li>Connection serial code is generated in <code>DbConn</code> class when connection is established.</li>
   * </ul>
   */
  private static final Map<String, ConcurrentMap<String, Connection>> connPoolMaps_ =
      new HashMap<>();

  /**
   * Busy connection management map <database connection name, busy connection list <connection serial code>> (singleton).<br>
   * <ul>
   * <li>Holds a list per database connection name to handle multiple connections simultaneously.</li>
   * <li>Internal busy connection list uses thread-safe class.</li>
   * <li>Connection serial code is generated in <code>DbConn</code> class when connection is established.</li>
   * </ul>
   */
  private static final Map<String, ConcurrentLinkedQueue<String>> connBusyLists_ = new HashMap<>();

  /**
   * Constructor.
   */
  private DbUtil() {
    // No processing
  }

  /**
   * Gets the default database connection.<br>
   * <ul>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @return the database connection
   */
  public static Connection getConn() {
    return getConnByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * Gets the default database connection.<br>
   * <ul>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   * 
   * @param traceCode Trace code
   * @return the database connection
   */
  public static Connection getConn(final String traceCode) {
    return getConnByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * Gets the database connection by configuration name.<br>
   * <ul>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName Database connection name in configuration file (part before .dbcon.url)
   * @return the database connection
   */
  public static Connection getConnByConfigName(final String connName) {
    return getConnByConfigName(connName, null);
  }

  /**
   * Gets the database connection by configuration name.<br>
   * <ul>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  Database connection name in configuration file (part before .dbcon.url)
   * @param traceCode Trace code
   * @return the database connection
   */
  public static Connection getConnByConfigName(final String connName, final String traceCode) {
    // Generate connection serial code
    final String serialCode = createSerialCode(connName);
    // New connection
    final Connection conn = createConn(connName);
    // Wrap and return
    final DbConn dbConn = new DbConn(conn, serialCode, traceCode);
    return dbConn;
  }

  /**
   * Gets the default pooled database connection.<br>
   * <ul>
   * <li>Gets a pooled database connection.</li>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @return the database connection
   */
  public static Connection getConnPooled() {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * Gets the default pooled database connection.<br>
   * <ul>
   * <li>Gets a pooled database connection.</li>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param traceCode Trace code
   * @return the database connection
   */
  public static Connection getConnPooled(final String traceCode) {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * Gets the pooled database connection by configuration name.<br>
   * <ul>
   * <li>Gets a pooled database connection.</li>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  Database connection name in configuration file (part before .dbcon.url)
   * @return the database connection
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName) {
    return getConnPooledByConfigName(connName,  null);
  }

  /**
   * Gets the pooled database connection by configuration name.<br>
   * <ul>
   * <li>Gets a pooled database connection.</li>
   * <li>Declare in try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  Database connection name in configuration file (part before .dbcon.url)
   * @param traceCode Trace code
   * @return the database connection
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName, final String traceCode) {

    // Create management data for database connection name if not exists
    if (!connPoolMaps_.containsKey(connName)) {
      connPoolMaps_.put(connName, new ConcurrentHashMap<String, Connection>());
      connBusyLists_.put(connName, new ConcurrentLinkedQueue<String>());
    }

    // Connection pool (thread-safe)
    final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
    // Busy connections (thread-safe)
    final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

    // Find and return a connection that is not busy
    // Use iterator as removal may occur
    final Iterator<Map.Entry<String, Connection>> connIte = connPoolMap.entrySet().iterator();
    // Connection pool loop
    while (connIte.hasNext()) {
      final Map.Entry<String, Connection> connEnt = connIte.next();
      // Connection serial code
      final String serialCode = connEnt.getKey();
      if (!connBusyList.contains(serialCode)) {
        // Not in busy connection list
        final Connection conn = connEnt.getValue();
        if (isClosedQuietly(conn)) {
          // If disconnected for some reason, remove from map and search next
          connPoolMap.remove(serialCode);
          continue;
        }
        // Add connection serial code to busy connection list (removal is done on disconnect)
        // Add before instance creation to prevent same connection from being used by multiple threads
        connBusyList.add(serialCode);
        // Wrap and return
        final DbConnPooled dbConn = new DbConnPooled(conn, serialCode, connBusyList, traceCode);
        return dbConn;
      }
    }
    // If no unused connection and max connections not reached, create new connection and return.

    // Max connections (pool size)
    final int maxSize = PROP_MAP.getInt(connName + PKEY_SUFFIX_MAX);

    if (connPoolMap.size() >= maxSize) {
      throw new RuntimeException("Database connection limit reached. " + LogUtil.joinKeyVal("maxSize", maxSize));
    }

    // Generate connection serial code
    final String newSerialCode = createSerialCode(connName);
    // Establish new connection
    final Connection conn = createConn(connName);
    // Add to connection pool
    connPoolMap.put(newSerialCode, conn);
    // Add connection serial code to busy connection list (removal is done on disconnect)
    connBusyList.add(newSerialCode);
    // Wrap and return
    final DbConnPooled dbConn = new DbConnPooled(conn, newSerialCode, connBusyList, traceCode);
    return dbConn;
  }

  /**
   * Closes pooled database connections.<br>
   * <ul>
   * <li>Closes all pooled database connections.</li>
   * <li>Also closes connections in use.</li>
   * </ul>
   * 
   * @return <code>true</code> if any connections were closed
   */
  public static synchronized boolean closePooledConn() {
    boolean ret = false;
    // Use iterator as removal may occur
    final Iterator<String> connNameIte = connPoolMaps_.keySet().iterator();
    // Connection pool management map loop
    while (connNameIte.hasNext()) {
      final String connName = connNameIte.next();
      // Connection pool (thread-safe)
      final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
      // Busy connections (thread-safe)
      final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

      // Connection pool loop
      for (final Map.Entry<String, Connection> connEnt : connPoolMap.entrySet()) {
        // Connection serial code
        final String serialCode = connEnt.getKey();
        if (connBusyList.contains(serialCode)) {
          // Connection is in use
          LogUtil.stdout("Warninng! Database connection is currently busy during close pooled connections. "
              + LogUtil.joinKeyVal("serialCode", serialCode));
        }
        // Database connection
        final Connection conn = connEnt.getValue();
        // Wrap before closing database connection (to unify log output)
        @SuppressWarnings("resource")
        final DbConn dbConn = new DbConn(conn, serialCode);
        dbConn.rollbackCloseForce();
        ret = true;
      }
      // Remove from connection pool management map and busy connection management map after all database connections are closed
      connPoolMaps_.remove(connName);
      connBusyLists_.remove(connName);
    }
    return ret;
  }

  /**
   * Establishes a database connection.
   *
   * @param connName Database connection name in configuration file (part before .dbcon.url)
   * @return the database connection
   */
  private static Connection createConn(final String connName) {
    if (!PROP_MAP.containsKey(connName + PKEY_SUFFIX_URL)) {
      throw new RuntimeException("Configuration does not exist. "  + LogUtil.joinKeyVal("ConnName", connName));
    }

    // Database URL
    final String url = PROP_MAP.getString(connName + PKEY_SUFFIX_URL);

    // Database user
    final String user;
    // Database password
    final String pass;
    if (PROP_MAP.containsKey(connName + PKEY_SUFFIX_USER)) {
      user = PROP_MAP.getString(connName + PKEY_SUFFIX_USER);
      pass = PROP_MAP.getString(connName + PKEY_SUFFIX_PASS);
    } else {
      user = null;
      pass = null;
    }

    try {
      final Connection conn = DriverManager.getConnection(url, user, pass);
      return conn;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during database connection. " 
                              + LogUtil.joinKeyVal("url", url, "user", user), e);
    }
  }

  /**
   * Checks if database connection is closed ignoring errors.<br>
   * <ul>
   * <li>Use this method instead of <code>#isClosed()</code> from components to avoid throwing errors.</li>
   * </ul>
   *
   * @param conn Database connection
   * @return <code>true</code> if the database connection is closed (also returns <code>true</code> on error)
   */
  private static boolean isClosedQuietly(final Connection conn) {
    try {
      if (conn.isClosed()) {
        return true;
      }
    } catch (SQLException ignore) {
      // Treat as closed on exception error
      return true;
    }
    return false;
  }

  /**
   * Generates a serial code.
   *
   * @param connName Database connection name in configuration file (part before .dbcon.url)
   * @return the serial code
   */
  private static String createSerialCode(final String connName) {
    final String serialCode = ValUtil.getSequenceCode();
    return serialCode + "-" + connName;
  }

  /**
   * Gets the serial code from a database connection.
   *
   * @param conn Database connection
   * @return the serial code
   */
  static String getSerialCode(final Connection conn) {
    if (conn instanceof DbConn) {
      return ((DbConn) conn).getSerialCode();
    }
    return "-";
  }

  /**
   * Gets the DBMS name from a database connection.
   *
   * @param conn Database connection
   * @return the DBMS name
   */
  static DbmsName getDbmsName(final Connection conn) {
    try {
      // Product name from database connection
      final String productName =
          ValUtil.nvl(conn.getMetaData().getDatabaseProductName()).toLowerCase();
      for (final DbmsName dbmsName : DbmsName.values()) {
        if (dbmsName.toString().toLowerCase().contains(productName)) {
          return dbmsName;
        }
      }
      return DbmsName.ETC;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * Gets the DBMS name from a database statement.
   *
   * @param stmt Database statement
   * @return the DBMS name
   */
  static DbmsName getDbmsName(final Statement stmt) {
    try {
      return getDbmsName(stmt.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * Gets the DBMS name from a database result set.
   *
   * @param rset Database result set
   * @return the DBMS name
   */
  static DbmsName getDbmsName(final ResultSet rset) {
    try {
      return getDbmsName(rset.getStatement());
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while getting DBMS name. ", e);
    }
  }

  /**
   * Gets the database connection configuration names.
   * @return the list of connection names where URL is configured (includes default connection name)
   */
  public static List<String> getConnNames() {
    final List<String> ret = new ArrayList<>();
    for (final String key : PROP_MAP.keySet()) {
      if (key.endsWith(PKEY_SUFFIX_URL)) {
        final String connName = key.substring(0, key.length() - PKEY_SUFFIX_URL.length());
        ret.add(connName);
      }
    }
    return ret;
  }

  /**
   * Closes prepared statement ignoring errors.
   *
   * @param stmt the prepared statement
   */
  static void closeQuietly(final PreparedStatement stmt) {
    if (ValUtil.isNull(stmt)) {
      return;
    }
    try {
      if (stmt.isClosed()) {
        return;
      }
      stmt.close();
    } catch (SQLException ignore) {
      // No processing
    }
  }
  
  /**
   * Closes result set ignoring errors.
   *
   * @param rset the result set
   */
  static void closeQuietly(final ResultSet rset) {
    if (ValUtil.isNull(rset)) {
      return;
    }
    try {
      if (rset.isClosed()) {
        return;
      }
      rset.close();
    } catch (SQLException ignore) {
      // No processing
    }
  }
}

