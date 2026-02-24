package com.onepg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.onepg.util.BreakException;
import com.onepg.util.LogUtil;

/**
 * Pooled database connection class.
 * @hidden
 */
public final class DbConnPooled extends DbConn {

  /**
   * Busy connection list &lt;connection serial code&gt; (thread-safe).<br>
   * <ul>
   * <li>Copy of the list managed by <code>DbUtil</code></li>
   * </ul>
   */
  private final ConcurrentLinkedQueue<String> connBusyList;

  /**
   * Constructor.
   *
   * @param conn database connection
   * @param serialCode connection serial code
   * @param connBusyList busy connection list
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList) {
    this(conn, serialCode, connBusyList, null);
  }

  /**
   * Constructor.
   *
   * @param conn database connection
   * @param serialCode connection serial code
   * @param connBusyList busy connection list
   * @param traceCode trace code
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList, final String traceCode) {
    super(conn, serialCode, traceCode);
    this.connBusyList = connBusyList;
    if (super.logger.isDevelopMode()) {
      super.logger.develop("Database connection is now busy. " + LogUtil.joinKeyVal("busyConnSize", this.connBusyList.size()));
    }
  }

  /**
   * Closes the database connection.<br>
   * <ul>
   * <li>Actually does not disconnect the database, only removes from the busy connection list. (Image of returning to the pool)</li>
   * <li>To actually disconnect the database, use <code>#rollbackCloseForce()</code>.</li>
   * <li>Rolls back to reset the transaction.</li>
   * <li>If the connection is closed for any reason, only removes from the busy connection list.</li>
   * <li>Disconnected connections are checked and discarded in <code>DbUtil#getConnPooled()</code>.</li>
   * <li>If an error occurs within this method, actually disconnects the database, but if the disconnection process also errors, throws an exception.<br>
   * (To detect the occurrence of abnormal connections early)</li>
   * </ul>
   */
  @Override
  public void close() throws SQLException {
    try {
      if (!super.isClosed()) {
        super.rollback();
      }
    } catch (SQLException re) {
      super.logger.error(re, "Exception error occurred during database rollback. ");
      // Force database disconnection if rollback error occurs
      try {
        super.close();
      } catch (SQLException ce) {
        // This point should not be reached because super.close() throws BreakException, but throws an exception in case it is reached
        super.logger.error(ce, "Exception error occurred during database close. ");
        throw new BreakException();
      }
    } finally {
      // Remove from busy connection list
      this.connBusyList.remove(super.serialCode);
      if (super.logger.isDevelopMode()) {
        super.logger.develop("Released busy database connection. " + LogUtil.joinKeyVal("busyConnSize", this.connBusyList.size()));
      }
    }
  }
}
