package com.example.app.service.exmodule;

import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.Io.MsgType;
import com.onepg.util.IoRows;
import com.onepg.util.ValUtil;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * List search web service class.
 */
public class ExampleListSearch extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Validates the database retrieval conditions
    validate(io);
    if (io.hasErrorMsg()) {
      // Exits the process if a validation error occurs
      return;
    }
    
    // Retrieves data from the database
    getList(io);
  }

  /**
   * Retrieves data from the database.
   * 
   * @param io argument and return value (request and response)
   */
  private void getList(final Io io) {
    // Database retrieval SQL
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_id ");
    sb.addQuery(", u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", CASE WHEN u.gender_cs = 'M' THEN 'Male' WHEN u.gender_cs = 'F' THEN 'Female' ELSE 'Other' END gender_dn ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(", u.upd_ts ");
    sb.addQuery(" FROM t_user u ").addQuery(" WHERE 1=1 ");
    sb.addQnotB("   AND u.user_id = ? ", io.getString("user_id"));
    sb.addQnotB("   AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
    sb.addQnotB("   AND u.email LIKE ? || '%' ", io.getString("email"));
    sb.addQnotB("   AND u.country_cs = ? ", io.getString("country_cs"));
    sb.addQnotB("   AND u.gender_cs = ? ", io.getString("gender_cs"));
    sb.addQnotB("   AND u.spouse_cs = ? ", io.getString("spouse_cs"));
    sb.addQnotB("   AND u.income_am >= ? ", io.getBigDecimalNullable("income_am"));
    sb.addQnotB("   AND u.birth_dt = ? ", io.getDateNullable("birth_dt"));
    sb.addQuery(" ORDER BY u.user_id ");
    // Retrieves data in bulk from the database
    final IoRows rows = SqlUtil.selectBulk(getDbConn(), sb, 5);
    // Sets the retrieval result
    io.putRows("list", rows);
    // Sets the retrieval count
    io.put("list_size", rows.size());
    if (rows.size() <= 0) {
      // Sets the message when the retrieval count is zero
      io.putMsg(MsgType.INFO, "i0004", new String[] { String.valueOf(rows.size()) });
    }
  }

  
  /**
   * Validates the database retrieval conditions.
   * 
   * @param io argument and return value (request and response)
   * @throws Exception validation error
   */
  private void validate(final Io io) throws Exception {

    // Checks the income
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) ) {
      if (!ValUtil.isNumber(incomeAm)) {
        // Sets the numeric invalid message
        io.putMsg(MsgType.ERROR, "ev012", new String[] { "Income" }, "income_am");
      }
    }

    // Checks the birth date
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt)) {
      if (!ValUtil.isDate(birthDt)) {
        // Sets the date invalid message
        io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth date" }, "birth_dt");
      }
    }
  }

}
