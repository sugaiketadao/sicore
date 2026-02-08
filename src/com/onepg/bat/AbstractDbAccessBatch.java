package com.onepg.bat;

import com.onepg.db.DbUtil;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.sql.Connection;

/**
 * Database access batch processing base class.<br>
 * <ul>
 * <li>Base class for batch processing that includes database connections.</li>
 * <li>Automatically handles database connection acquisition and closing.</li>
 * </ul>
 */
public abstract class AbstractDbAccessBatch extends AbstractBatch {

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
  public AbstractDbAccessBatch() {
    super();
  }

  /**
   * Invokes main processing.<br>
   * <ul>
   * <li>Converts arguments from URL parameter format to map format, executes log start processing, acquires database connection, and then calls the <code>doExecute</code> method.</li>
   * <li>Accepts multiple arguments as an array to accommodate the length limit per command-line argument.</li>
   * <li>The converted arguments are passed to the <code>doExecute</code> method as the <code>IoItems</code> class.</li>
   * <li>If the return value of the <code>doExecute</code> method is 0, the processing is considered to have terminated normally and commits.</li>
   * <li>If the return value of the <code>doExecute</code> method is other than 0 or an exception error occurs, the processing is considered to have terminated abnormally and rolls back. (Rollback is performed by <code>Connection#close()</code>)</li>
   * <li>After processing ends, the database connection is always closed.</li>
   * </ul>
   *
   * @param args the arguments
   */
  @Override
  protected void callMain(final String[] args) {    
    final IoItems argsMap = new IoItems();
    argsMap.putAllByBatParam(args);
    if (this.logger.isDevelopMode()) {
      this.logger.develop(LogUtil.joinKeyVal("arguments", argsMap));
    }

    int status = 0;    
    try {
      this.logger.begin();
      // Get pooled database connection
      try (final Connection conn = DbUtil.getConn(super.traceCode)) {
        this.dbConn = conn;
        status = doExecute(argsMap);
        if (status == 0) {
          this.dbConn.commit();
        }
      } finally {
        this.dbConn = null;
      }
    } catch (final Exception | Error e) {
      status = 1;
      this.logger.error(e, "An exception error occurred in batch processing. ");
    }
    this.logger.end(status);
    System.exit(status);
  }

  /**
   * Gets database connection.<br>
   * <ul>
   * <li>Returns the current database connection.</li>
   * <li>The connection is valid only within the <code>callMain</code> method.</li>
   * </ul>
   *
   * @return the database connection
   */
  protected Connection getDbConn() {
    if (ValUtil.isNull(this.dbConn)) {
      throw new RuntimeException("Database connection is valid only during main processing (callMain method).");
    }
    return this.dbConn;
  }
}