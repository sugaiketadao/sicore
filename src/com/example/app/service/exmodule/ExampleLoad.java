package com.example.app.service.exmodule;

import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.Io.MsgType;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.ValUtil;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * Data retrieval web service class.
 */
public class ExampleLoad extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Retrieve header
    getHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Retrieve detail
    getDetail(io);
  }

  /**
   * Retrieves header.
   * 
   * @param io argument and return value (request and response)
   */
  private void getHead(final Io io) {
    // Database retrieval SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", u.country_cs ");
    sb.addQuery(", u.gender_cs ");
    sb.addQuery(", u.spouse_cs ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

    // Retrieve one record from database
    final IoItems head = SqlUtil.selectOne(getDbConn(), sb);
    if (ValUtil.isNull(head)) {
      // Set optimistic locking error message when data not found
      io.putMsg(MsgType.ERROR, "e0002", new String[]{io.getString("user_id")});
      return;
    }
    // Set database retrieval result
    io.putAll(head);
  }

  /**
   * Retrieves detail.
   * 
   * @param io argument and return value (request and response)
   */
  private void getDetail(final Io io) {
    // Database retrieval SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  d.pet_no ");
    sb.addQuery(", d.pet_nm ");
    sb.addQuery(", d.type_cs ");
    sb.addQuery(", d.gender_cs ");
    sb.addQuery(", d.vaccine_cs ");
    sb.addQuery(", d.weight_kg ");
    sb.addQuery(", d.birth_dt ");
    sb.addQuery(" FROM t_user_pet d ");
    sb.addQuery(" WHERE d.user_id = ? ", io.getString("user_id"));
    sb.addQuery(" ORDER BY d.pet_no");
    // Bulk retrieval from database
    final IoRows detail = SqlUtil.selectBulkAll(getDbConn(), sb);
    // Set database retrieval result
    io.putRows("detail", detail);
  }

}
