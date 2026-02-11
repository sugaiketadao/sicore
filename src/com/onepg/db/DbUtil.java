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
     * @param value product name
     */
    private DbmsName(final String value) {
      this.productName = value;
    }

    @Override
    public String toString() {
      return this.productName;
    }
  }


  /** Default database connection name (the part before .dbcon.url). */
  private static final String DEFAULT_CONN_NAME = "default";

  /** Database connection - URL suffix. */
  private static final String PKEY_SUFFIX_URL = ".conn.url";
  /** Database connection - user suffix. */
  private static final String PKEY_SUFFIX_USER = ".conn.user";
  /** Database connection - password suffix. */
  private static final String PKEY_SUFFIX_PASS = ".conn.pass";
  /** Database connection - maximum connection count (pool size) suffix. */
  private static final String PKEY_SUFFIX_MAX = ".conn.max";

  /** Database configuration. */
  private static final IoItems PROP_MAP;
  /** Warning output SQL execution elapsed time. */
  static final long SQL_EXEC_WARN_TIME_MSEC;

  static {
    // Retrieve database configuration
    PROP_MAP = PropertiesUtil.getFrameworkProps(FwPropertiesName.DB);
    SQL_EXEC_WARN_TIME_MSEC = PROP_MAP.getLongOrDefault("sqlexec.warn.time", -1);
  }

  /**
   * Connection pool management map &lt;database connection name, connection pool map &lt;connection serial code, database connection&gt;&gt; (singleton).<br>
   * <ul>
   * <li>Maintains a map for each database connection name to handle multiple connections simultaneously.</li>
   * <li>Uses thread-safe classes for internal connection pool maps.</li>
   * <li>Connection serial codes are generated within the <code>DbConn</code> class when connections are established.</li>
   * </ul>
   */
  private static final Map<String, ConcurrentMap<String, Connection>> connPoolMaps_ =
      new HashMap<>();

  /**
   * Busy connection management map &lt;database connection name, busy connection list &lt;connection serial code&gt;&gt; (singleton).<br>
   * <ul>
   * <li>Maintains a list for each database connection name to handle multiple connections simultaneously.</li>
   * <li>Uses thread-safe classes for internal busy connection lists.</li>
   * <li>Connection serial codes are generated within the <code>DbConn</code> class when connections are established.</li>
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
   * Retrieves the default database connection.<br>
   * <ul>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @return the database connection
   */
  public static Connection getConn() {
    return getConnByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * Retrieves the default database connection.<br>
   * <ul>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   * 
   * @param traceCode trace code
   * @return the database connection
   */
  public static Connection getConn(final String traceCode) {
    return getConnByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * Retrieves a database connection with the specified connection name.<br>
   * <ul>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName database connection name in the configuration file (the part before .dbcon.url)
   * @return the database connection
   */
  public static Connection getConnByConfigName(final String connName) {
    return getConnByConfigName(connName, null);
  }

  /**
   * Retrieves a database connection with the specified connection name.<br>
   * <ul>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  database connection name in the configuration file (the part before .dbcon.url)
   * @param traceCode trace code
   * @return the database connection
   */
  public static Connection getConnByConfigName(final String connName, final String traceCode) {
    // Generate connection serial code
    final String serialCode = createSerialCode(connName);
    // Create new connection
    final Connection conn = createConn(connName);
    // Wrap and return
    final DbConn dbConn = new DbConn(conn, serialCode, traceCode);
    return dbConn;
  }

  /**
   * Retrieves the default pooled database connection.<br>
   * <ul>
   * <li>Retrieves a pooled database connection.</li>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @return the database connection
   */
  public static Connection getConnPooled() {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME);
  }

  /**
   * Retrieves the default pooled database connection.<br>
   * <ul>
   * <li>Retrieves a pooled database connection.</li>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param traceCode trace code
   * @return the database connection
   */
  public static Connection getConnPooled(final String traceCode) {
    return getConnPooledByConfigName(DEFAULT_CONN_NAME, traceCode);
  }

  /**
   * Retrieves a pooled database connection with the specified connection name.<br>
   * <ul>
   * <li>Retrieves a pooled database connection.</li>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  database connection name in the configuration file (the part before .dbcon.url)
   * @return the database connection
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName) {
    return getConnPooledByConfigName(connName,  null);
  }

  /**
   * Retrieves a pooled database connection with the specified connection name.<br>
   * <ul>
   * <li>Retrieves a pooled database connection.</li>
   * <li>Declare in a try clause (try-with-resources statement).</li>
   * </ul>
   *
   * @param connName  database connection name in the configuration file (the part before .dbcon.url)
   * @param traceCode trace code
   * @return the database connection
   */
  public static synchronized Connection getConnPooledByConfigName(final String connName, final String traceCode) {

    // Create management data for database connection name if it does not exist
    if (!connPoolMaps_.containsKey(connName)) {
      connPoolMaps_.put(connName, new ConcurrentHashMap<String, Connection>());
      connBusyLists_.put(connName, new ConcurrentLinkedQueue<String>());
    }

    // Connection pool (thread-safe)
    final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
    // Busy connections (thread-safe)
    final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

    // Find and return a connection that is not in use
    // Use iterator as it may be removed
    final Iterator<Map.Entry<String, Connection>> connIte = connPoolMap.entrySet().iterator();
    // Loop through connection pool
    while (connIte.hasNext()) {
      final Map.Entry<String, Connection> connEnt = connIte.next();
      // Connection serial code
      final String serialCode = connEnt.getKey();
      if (!connBusyList.contains(serialCode)) {
        // Not in busy connection list
        final Connection conn = connEnt.getValue();
        if (isClosedQuietly(conn)) {
          // If the database is disconnected for any reason, remove from map and search for next
          connPoolMap.remove(serialCode);
          continue;
        }
        // Add connection serial code to busy connection list (removed when database is disconnected)
        // Add before instance creation to prevent the same connection from being used by multiple threads
        connBusyList.add(serialCode);
        // Wrap and return
        final DbConnPooled dbConn = new DbConnPooled(conn, serialCode, connBusyList, traceCode);
        return dbConn;
      }
    }
    // If there are no unused connections and the maximum connection count has not been reached, create a new connection and return it.

    // Maximum connection count (pool size)
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
    // Add connection serial code to busy connection list (removed when database is disconnected)
    connBusyList.add(newSerialCode);
    // Wrap and return
    final DbConnPooled dbConn = new DbConnPooled(conn, newSerialCode, connBusyList, traceCode);
    return dbConn;
  }

  /**
   * Closes pooled database connections.<br>
   * <ul>
   * <li>Disconnects all pooled database connections.</li>
   * <li>Also disconnects busy connections.</li>
   * </ul>
   * 
   */
  public static synchronized void closePooledConn() {
    // Use iterator as it will be removed
    final Iterator<String> connNameIte = connPoolMaps_.keySet().iterator();
    // Loop through connection pool management map
    while (connNameIte.hasNext()) {
      final String connName = connNameIte.next();
      // Connection pool (thread-safe)
      final ConcurrentMap<String, Connection> connPoolMap = connPoolMaps_.get(connName);
      // Busy connections (thread-safe)
      final ConcurrentLinkedQueue<String> connBusyList = connBusyLists_.get(connName);

      // Loop through connection pool
      for (final Map.Entry<String, Connection> connEnt : connPoolMap.entrySet()) {
        // Connection serial code
        final String serialCode = connEnt.getKey();
        if (connBusyList.contains(serialCode)) {
          // If connection is busy
          LogUtil.stdout("Warninng! Database connection is currently busy during close pooled connections. "
              + LogUtil.joinKeyVal("serialCode", serialCode));
        }
        // Database connection
        final Connection conn = connEnt.getValue();
        // Wrap before disconnecting database (to unify log output)
        @SuppressWarnings("resource")
        final DbConn dbConn = new DbConn(conn, serialCode);
        dbConn.rollbackCloseForce();
      }
      // Remove from connection pool management map and busy connection management map after disconnecting all databases
      connPoolMaps_.remove(connName);
      connBusyLists_.remove(connName);
    }
  }

  /**
   * Establishes a database connection.
   *
   * @param connName database name in the configuration file (the part before .dbcon.url)
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
   * Checks if the database is disconnected, ignoring errors.<br>
   * <ul>
   * <li>Components should use this method instead of <code>#isClosed()</code> as it does not throw errors.</li>
   * </ul>
   *
   * @param conn database connection
   * @return <code>true</code> if the database is disconnected (also <code>true</code> in case of error)
   */
  private static boolean isClosedQuietly(final Connection conn) {
    try {
      if (conn.isClosed()) {
        return true;
      }
    } catch (SQLException ignore) {
      // Determine as closed in case of exception error
      return true;
    }
    return false;
  }

  /**
   * Generates a serial code.
   *
   * @param connName database connection name in the configuration file (the part before .dbcon.url)
   * @return the serial code
   */
  private static String createSerialCode(final String connName) {
    final String serialCode = ValUtil.getSequenceCode();
    return serialCode + "-" + connName;
  }

  /**
   * Retrieves the serial code from the database connection.
   *
   * @param conn database connection
   * @return the serial code
   */
  static String getSerialCode(final Connection conn) {
    if (conn instanceof DbConn) {
      return ((DbConn) conn).getSerialCode();
    }
    return "-";
  }

  /**
   * Retrieves the DBMS name from the database connection.
   *
   * @param conn database connection
   * @return the DBMS name
   */
  static DbmsName getDbmsName(final Connection conn) {
    try {
      // Database connection product name
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
   * Retrieves the DBMS name from the database statement.
   *
   * @param stmt database statement
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
   * Retrieves the DBMS name from the database result set.
   *
   * @param rset database result set
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
   * Retrieves database connection configuration names.
   * @return the list of connection names with URLs configured (includes the default connection name)
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
   * Closes the prepared statement, ignoring errors.
   *
   * @param stmt prepared statement
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
   * Closes the result set, ignoring errors.
   *
   * @param rset result set
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

