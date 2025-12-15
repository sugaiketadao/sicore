package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.Io;
import com.onepg.util.ValUtil;

import java.sql.Connection;

/**
 * Database access web service base class.<br>
 * <ul>
 * <li>A base class for web services that include database connection.</li>
 * <li>Automatically handles database connection acquisition and closing.</li>
 * </ul>
 */
public abstract class AbstractDbAccessWebService extends AbstractWebService {

  /**
   * Database connection.
   */
  private Connection dbConn = null;

  /**
   * Constructor.<br>
   * <ul>
   * <li>Calls the superclass constructor.</li>
   * </ul>
   */
  public AbstractDbAccessWebService() {
    super();
  }

  /**
   * Invokes main processing.<br>
   * <ul>
   * <li>Acquires a pooled database connection and executes the superclass processing.</li>
   * <li>Commits if processing completes normally.</li>
   * <li>Rolls back if an exception error occurs. (Rollback is performed in <code>DbConnPooled#close()</code>)</li>
   * <li>Always closes the database connection after processing completes.</li>
   * </ul>
   *
   * @param io the argument and return value (request and response)
   * @throws Exception exception error
   */
  @Override
  void execute(final Io io) throws Exception {
    // Acquires pooled database connection
    try (final Connection conn = DbUtil.getConnPooled(super.traceCode)) {
      this.dbConn = conn;
      super.execute(io);
      if (!io.hasErrorMsg()) {
        this.dbConn.commit();
      }
    } finally {
      this.dbConn = null;
    }
  }

  /**
   * Gets database connection.<br>
   * <ul>
   * <li>Returns the current database connection.</li>
   * <li>The connection is valid only within the execute method.</li>
   * </ul>
   *
   * @return the database connection
   */
  protected Connection getDbConn() {
    if (ValUtil.isNull(this.dbConn)) {
      throw new RuntimeException("Database connection is valid only during main processing (execute method).");
    }
    return this.dbConn;
  }
}