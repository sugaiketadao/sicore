package com.onepg.web;

import com.onepg.db.DbUtil;
import com.onepg.util.Io;
import com.onepg.util.ValUtil;

import java.sql.Connection;

/**
 * Database access web service base class.<br>
 * <ul>
 * <li>This is the base class for web services that include database connections.</li>
 * <li>Automatically handles obtaining and closing DB connections.</li>
 * </ul>
 */
public abstract class AbstractDbAccessWebService extends AbstractWebService {

  /**
   * DB connection.
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
   * Calls main processing.<br>
   * <ul>
   * <li>Obtains a pooled DB connection and executes the superclass processing.</li>
   * <li>Commits if processing completes normally.</li>
   * <li>Rolls back if an exception error occurs. (Rollback is performed in <code>DbConnPooled#close()</code>)</li>
   * <li>Always closes the DB connection after processing ends.</li>
   * </ul>
   *
   * @param io Argument and return value (request and response)
   * @throws Exception Exception error
   */
  @Override
  void execute(final Io io) throws Exception {
    // Gets pooled DB connection
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
   * Gets DB connection.<br>
   * <ul>
   * <li>Returns the current DB connection.</li>
   * <li>This connection is valid only within the <code>execute</code> method.</li>
   * </ul>
   *
   * @return DB connection
   */
  protected Connection getDbConn() {
    if (ValUtil.isNull(this.dbConn)) {
      throw new RuntimeException("Database connection is valid only during main processing (execute method).");
    }
    return this.dbConn;
  }
}