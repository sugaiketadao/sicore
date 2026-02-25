package com.example.app.service.exmodule;

import java.sql.Connection;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.LogUtil;
import com.onepg.util.Io.MsgType;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * Deletion web service class.
 */
public class ExampleDelete extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Deletes the header
    deleteHead(io);
    if (io.hasErrorMsg()) {
      // Exits the process if an optimistic locking error occurs
      return;
    }
    // Deletes the detail
    deleteDetail(io);
    // Sets the success message
    io.putMsg(MsgType.INFO, "i0003", new String[] { io.getString("user_id") });
  }

  /**
   * Deletes the header.
   * 
   * @param io argument and return value (request and response)
   */
  private void deleteHead(final Io io) {
    // Deletes one row from the database
    if (!SqlUtil.deleteOneByPkey(getDbConn(), "t_user", io, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * Deletes the detail.
   * 
   * @param io argument and return value (request and response)
   */
  private void deleteDetail(final Io io) {
    final Connection conn = getDbConn();
    // Deletes multiple rows from the database
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (super.logger.isDevelopMode()) {
      super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt));
    }
  }
}