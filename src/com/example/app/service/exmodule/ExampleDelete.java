package com.example.app.service.exmodule;

import java.sql.Connection;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.Io.MsgType;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * Delete web service class.
 */
public class ExampleDelete extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Delete header
    deleteHead(io);
    if (io.hasErrorMsg()) {
      // Exit processing on optimistic locking error
      return;
    }
    // Delete detail
    deleteDetail(io);
    // Set success message
    io.putMsg(MsgType.INFO, "i0003", new String[] { io.getString("user_id") });
  }

  /**
   * Deletes header.
   * 
   * @param io argument and return value (request and response)
   */
  private void deleteHead(final Io io) {
    // Delete one record from database
    if (!SqlUtil.deleteOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * Deletes detail.
   * 
   * @param io argument and return value (request and response)
   */
  private void deleteDetail(final Io io) {
    final Connection conn = getDbConn();
    // Delete multiple records from database
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (super.logger.isDevelopMode()) {
      super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt));
    }
  }
}