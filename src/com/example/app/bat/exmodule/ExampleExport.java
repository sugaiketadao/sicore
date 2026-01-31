package com.example.app.bat.exmodule;

import com.onepg.bat.AbstractDbAccessBatch;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlResultSet;
import com.onepg.db.SqlUtil;
import com.onepg.util.FileUtil;
import com.onepg.util.ValUtil.CharSet;
import com.onepg.util.ValUtil.LineSep;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.TxtWriter;
import com.onepg.util.ValUtil;

/**
 * Data export batch class.
 */
public class ExampleExport extends AbstractDbAccessBatch {

    /** SQL definition: user retrieval. */
    private static final SqlConst SQL_SEL_USER = SqlConst.begin()
      .addQuery("SELECT ")
      .addQuery("  u.user_id ")
      .addQuery(", u.user_nm ")
      .addQuery(", u.email ")
      .addQuery(", u.country_cs ")
      .addQuery(", u.gender_cs ")
      .addQuery(", u.spouse_cs ")
      .addQuery(", u.income_am ")
      .addQuery(", u.birth_dt ")
      .addQuery(", u.upd_ts ")
      .addQuery(" FROM t_user u ")
      .addQuery(" ORDER BY u.user_id ")
      .end();

  /**
   * Main processing.
   * @param args arguments
   */
  public static void main(String[] args) {
    final ExampleExport batch = new ExampleExport();
    batch.callMain(args);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Retrieves the output file path
    final String outputPath = io.getString("output");

    if (ValUtil.isBlank(outputPath)) {
      // Checks if the 'output' parameter is required
      throw new RuntimeException("'output' is required.");
    }
    if (FileUtil.exists(outputPath)) {
      // Checks if the output file does not exist
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }
    if (!FileUtil.existsParent(outputPath)) {
      // Checks if the output parent directory exists
      throw new RuntimeException("Output parent directory not exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    // Retrieves data from the database and outputs to a file
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);
        final TxtWriter tw = new TxtWriter(outputPath, LineSep.LF, CharSet.UTF8)) {
      final String[] itemNames = rSet.getItemNames();
      tw.println(ValUtil.joinCsvAllDq(itemNames));
      for (final IoItems row : rSet) {
        tw.println(row.createCsvDq());
      }
      if (rSet.getReadedCount() == 0) {
        super.logger.info("No data found to export. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }
    return 0;
  }
}
