package com.onepg.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.onepg.util.BreakException;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Database connection wrapper class.
 * @hidden
 */
public class DbConn implements Connection {

  /** Trace code. */
  private final String traceCode;
  /** Log writer. */
  protected final LogWriter logger;
  /** Database connection. */
  private final Connection conn;
  /** Connection serial code. */
  protected final String serialCode;

  /**
   * Constructor.
   *
   * @param conn       database connection
   * @param serialCode connection serial code
   */
  DbConn(final Connection conn, final String serialCode) {
    this(conn, serialCode, null);
  }

  /**
   * Constructor.
   *
   * @param conn       database connection
   * @param serialCode connection serial code
   * @param traceCode  trace code
   */
  DbConn(final Connection conn, final String serialCode, final String traceCode) {
    super();
    this.conn = conn;
    this.serialCode = serialCode;
    this.traceCode = traceCode;
    this.logger = LogUtil.newLogWriter(getClass(), ValUtil.nvl(this.traceCode, this.serialCode));
    this.logger.develop("Database connection created. ");
    init();
  }

  /**
   * Initialization processing.<br>
   * <ul>
   * <li>To avoid PostgreSQL cursor invalidation errors, setting <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
   * ignores the fetch size and fetches all records, which may cause OutOfMemory errors.
   * Therefore, that configuration logic is discontinued. (See <code>SqlUtil#select(Connection, SqlBuilder)</code>)<br>
   * Do not implement this in the future.</li>
   * </ul>
   */
  private void init() {
    try {
      // Turn off auto commit
      this.conn.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during database initialization. ", e);
    }
  }

  /**
   * Retrieves the serial code.
   *
   * @return the serial code
   */
  String getSerialCode() {
    return this.serialCode;
  }

  /**
   * Forces rollback and disconnects the database.<br>
   * <ul>
   * <li>Components should use this method instead of <code>#close()</code> as it does not throw errors.</li>
   * </ul>
   */
  void rollbackCloseForce() {
    try {
      if (this.conn.isClosed()) {
        this.logger.develop("Database connection is already closed. ");
        return;
      }
    } catch (SQLException e) {
      // No processing
    }
    try {
      this.conn.rollback();
      this.logger.develop("Database connection rolled back. ");
    } catch (SQLException e) {
      this.logger.error(e, "Exception error occurred during database rollback. ");
    }
    try {
      this.conn.close();
      this.logger.develop("Database connection closed. ");
    } catch (SQLException e) {
      this.logger.error(e, "Exception error occurred during database close. ");
      throw new BreakException();
    }
  }

  @Override
  public String toString() {
    return "{" + LogUtil.joinKeyVal("serialCode", this.serialCode, "traceCode", this.traceCode) + "}";
  }

  /* Database connection delegation methods below */

  /**
   * Disconnects the database.<br>
   * <ul>
   * <li>Rolls back even in normal database disconnection to unify behavior.</li>
   * </ul>
   */
  @Override
  public void close() throws SQLException {
    this.rollbackCloseForce();
  }

  /**
   * Unwraps the wrapper.<br>
   * <ul>
   * <li>Returns an object that implements the specified interface.</li>
   * </ul>
   *
   * @param <T> the return value type
   * @param iface the interface to unwrap
   * @return the unwrapped object
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return conn.unwrap(iface);
  }

  /**
   * Determines if this is a wrapper.<br>
   * <ul>
   * <li>Determines whether this is a wrapper for the specified interface.</li>
   * </ul>
   *
   * @param iface the interface to check
   * @return <code>true</code> if this is a wrapper
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return conn.isWrapperFor(iface);
  }

  @Override
  public Statement createStatement() throws SQLException {
    return conn.createStatement();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return conn.prepareStatement(sql);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return conn.prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return conn.prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return conn.prepareStatement(sql, columnNames);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return conn.prepareCall(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  @SuppressWarnings("all")
  public String nativeSQL(String sql) throws SQLException {
    return conn.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    conn.setAutoCommit(autoCommit);
    this.logger.develop("Auto commit set. " + LogUtil.joinKeyVal("auto", autoCommit));
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return conn.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    conn.commit();
    this.logger.develop("Database connection committed. ");
  }

  @Override
  public void rollback() throws SQLException {
    conn.rollback();
    this.logger.develop("Database connection rolled back. ");
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    conn.rollback(savepoint);
    if (!this.logger.isDevelopMode()) {
      return;
    }
    if (ValUtil.isNull(savepoint)) {
      this.logger.develop("Database connection rolled back. " + LogUtil.joinKeyVal("savepoint", null));
    } else {
      this.logger.develop("Database connection rolled back. " + LogUtil.joinKeyVal("savepoint", savepoint.getSavepointName()));
    }
  }

  @Override
  public boolean isClosed() throws SQLException {
    return conn.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return conn.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    conn.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return conn.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    conn.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return conn.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    conn.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return conn.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return conn.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    conn.clearWarnings();
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return conn.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    conn.setTypeMap(map);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    conn.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    return conn.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return conn.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return conn.setSavepoint(name);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    conn.releaseSavepoint(savepoint);
  }

  @Override
  public Clob createClob() throws SQLException {
    return conn.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    return conn.createBlob();
  }

  @Override
  @SuppressWarnings("all")
  public NClob createNClob() throws SQLException {
    return conn.createNClob();
  }

  @Override
  @SuppressWarnings("all")
  public SQLXML createSQLXML() throws SQLException {
    return conn.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return conn.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    conn.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    conn.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    return conn.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return conn.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return conn.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return conn.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    conn.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return conn.getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    conn.abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    conn.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return conn.getNetworkTimeout();
  }

  @Override
  public void beginRequest() throws SQLException {
    conn.beginRequest();
  }

  @Override
  public void endRequest() throws SQLException {
    conn.endRequest();
  }

  @Override
  public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey,
      int timeout) throws SQLException {
    return conn.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
  }

  @Override
  public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
    return conn.setShardingKeyIfValid(shardingKey, timeout);
  }

  @Override
  public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey)
      throws SQLException {
    conn.setShardingKey(shardingKey, superShardingKey);
  }

  @Override
  public void setShardingKey(ShardingKey shardingKey) throws SQLException {
    conn.setShardingKey(shardingKey);
  }
}
