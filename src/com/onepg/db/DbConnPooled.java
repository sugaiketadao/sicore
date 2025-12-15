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
   * Busy connection list <connection serial code> (thread-safe).<br>
   * <ul>
   * <li>Copy of list managed by <code>DbUtil</code></li>
   * </ul>
   */
  private final ConcurrentLinkedQueue<String> connBusyList;

  /**
   * Constructor.
   *
   * @param conn Database connection
   * @param serialCode Connection serial code
   * @param connBusyList Busy connection list
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList) {
    this(conn, serialCode, connBusyList, null);
  }

  /**
   * Constructor.
   *
   * @param conn Database connection
   * @param serialCode Connection serial code
   * @param connBusyList Busy connection list
   * @param traceCode Trace code
   */
  DbConnPooled(final Connection conn, final String serialCode, final ConcurrentLinkedQueue<String> connBusyList, final String traceCode) {
    super(conn, serialCode, traceCode);
    this.connBusyList = connBusyList;
    if (super.logger.isDevelopMode()) {
      super.logger.develop("Database connection is now busy. " + LogUtil.joinKeyVal("busyConnSize", this.connBusyList.size()));
    }
  }

  /**
   * Disconnect from database.<br>
   * <ul>
   * <li>Does not actually disconnect, only removes from busy connection list. (Returns to pool)</li>
   * <li>Use <code>#rollbackCloseForce()</code> to actually disconnect.</li>
   * <li>Rollback to reset transaction.</li>
   * <li>If connection is closed for some reason, only removes from busy connection list.</li>
   * <li>Closed connections are checked and discarded in <code>DbUtil#getConnPooled()</code>.</li>
   * <li>If error occurs in this method, actually disconnects, and if disconnect also errors, throws exception.<br>
   * (To detect abnormal connections early)</li>
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
      // Force disconnect if rollback error occurs
      try {
        super.close();
      } catch (SQLException ce) {
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
