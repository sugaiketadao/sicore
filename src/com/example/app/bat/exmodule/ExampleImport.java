package com.example.app.bat.exmodule;

import com.onepg.bat.AbstractDbAccessBatch;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlUtil;
import com.onepg.db.SqlConst.BindType;
import com.onepg.util.CsvReader;
import com.onepg.util.FileUtil;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.CsvType;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

/**
 * Data import batch class.
 */
public class ExampleImport extends AbstractDbAccessBatch {

    /** SQL definition: user registration. */
    private static final SqlConst SQL_INS_USER = SqlConst.begin()
      .addQuery("INSERT INTO t_user ( ")
      .addQuery("  user_id ")
      .addQuery(", user_nm ")
      .addQuery(", email ")
      .addQuery(", country_cs ")
      .addQuery(", gender_cs ")
      .addQuery(", spouse_cs ")
      .addQuery(", income_am ")
      .addQuery(", birth_dt ")
      .addQuery(", upd_ts ")
      .addQuery(" ) VALUES ( ")
      .addQuery("  ? ", "user_id", BindType.STRING)
      .addQuery(", ? ", "user_nm", BindType.STRING)
      .addQuery(", ? ", "email", BindType.STRING)
      .addQuery(", ? ", "country_cs", BindType.STRING)
      .addQuery(", ? ", "gender_cs", BindType.STRING)
      .addQuery(", ? ", "spouse_cs", BindType.STRING)
      .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
      .addQuery(", ? ", "birth_dt", BindType.DATE)
      .addQuery(", ? ", "upd_ts", BindType.TIMESTAMP)
      .addQuery(" ) ")
      .end();

    /** SQL definition: user update. */
    private static final SqlConst SQL_UPD_USER = SqlConst.begin()
      .addQuery("UPDATE t_user SET ")
      .addQuery("  user_nm = ? ", "user_nm", BindType.STRING)
      .addQuery(", email = ? ", "email", BindType.STRING)
      .addQuery(", country_cs = ? ", "country_cs", BindType.STRING)
      .addQuery(", gender_cs = ? ", "gender_cs", BindType.STRING)
      .addQuery(", spouse_cs = ? ", "spouse_cs", BindType.STRING)
      .addQuery(", income_am = ? ", "income_am", BindType.BIGDECIMAL)
      .addQuery(", birth_dt = ? ", "birth_dt", BindType.DATE)
      .addQuery(", upd_ts = ? ", "upd_ts", BindType.TIMESTAMP)
      .addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)
      .end();

  /**
   * Main processing.
   * @param args arguments
   */
  public static void main(String[] args) {
    final ExampleImport batch = new ExampleImport();
    batch.callMain(args);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Retrieves the input file path
    final String inputPath = io.getString("input");

    if (ValUtil.isBlank(inputPath)) {
      // Checks if the 'input' parameter is required
      throw new RuntimeException("'input' is required.");
    }
    if (!FileUtil.exists(inputPath)) {
      // Checks if the input file exists
      throw new RuntimeException("Input path not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // Reads the file and updates the database
    try (final CsvReader cr = new CsvReader(inputPath, CharSet.UTF8, CsvType.DQ_ALL)) {
      for (final IoItems row : cr) {
        if (!SqlUtil.executeOne(getDbConn(), SQL_UPD_USER.bind(row))) {
          // Executes insert if the update count is 0
          SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
        }
      }
      if (cr.getReadedCount() <= 1) {
        // When there are no data rows (header only or empty file)
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      }
    }
    return 0;
  }
}
