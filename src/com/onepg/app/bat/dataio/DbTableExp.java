package com.onepg.app.bat.dataio;

import java.io.File;
import java.sql.Connection;

import com.onepg.bat.AbstractBatch;
import com.onepg.db.DbUtil;
import com.onepg.db.SqlBuilder;
import com.onepg.db.SqlResultSet;
import com.onepg.db.SqlUtil;
import com.onepg.util.FileUtil;
import com.onepg.util.IoItems;
import com.onepg.util.IoTsvWriter;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

/**
 * DB table data export batch class.<br>
 * <ul>
 * <li>Exports data from the specified DB table to a TSV file for data I/O.<br>
 * The TSV file for data I/O has the following characteristics:
 *   <ul>
 *   <li>Outputs column names as the first row of the file.</li>
 *   <li>The file encoding is fixed to UTF-8 and the line separator is fixed to LF.</li>
 *   <li>Escapes <code>null</code> values.</li>
 *   <li>Escapes newline characters (CRLF, CR, LF) and tab characters within values.</li>
 *   </ul></li>
 * </li>
 * <li>A JDBC library (jar file) matching the DBMS is required.</li>
 * <li>Arguments to the <code>main</code> method are in URL parameter format. (See <code>AbstractBatch</code>)</li>
 * <li>Arguments are as follows:
 *   <ul>
 *   <li>url: JDBC connection URL [Example] jdbc:postgresql://localhost:5432/db01</li>
 *   <li>user: database user (optional)</li>
 *   <li>pass: database password (optional)</li>
 *   <li>table: target table physical name</li>
 *   <li>output: output path (directory can be specified)</li>
 *   <li>where: extraction condition (optional)</li>
 *   <li>zip: zip compression flag; true when compressing (optional)</li>
 *   </ul></li>
 * <li>If a directory is specified for the output path, the file name will be the table name.</li>
 * </ul>
 */
public class DbTableExp extends AbstractBatch {

  /**
   * Main processing.
   * @param args arguments
   */
  public static void main(String[] args) {
    System.exit((new DbTableExp()).callMain(args));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doExecute(final IoItems io) throws Exception {
    // Argument - JDBC connection URL (required)
    final String jdbcUrl = io.getString("url");
    // Argument - database user (optional)
    final String dbUser = io.getStringOrDefault("user", null);
    // Argument - database password (optional)
    final String dbPass = io.getStringOrDefault("pass", null);
    // Argument - target table name (required); treated as lowercase
    final String tableName =  io.getString("table").toLowerCase();
    // Argument - output path (required)
    String outputPath = io.getString("output");
    // Argument - extraction condition (optional)
    final String where = io.getStringOrDefault("where", ValUtil.BLANK);
    // Zip compression flag (optional)
    final boolean isZip = io.getBooleanOrDefault("zip", false);
    
    // Output file path
    if (FileUtil.isDirectory(outputPath)) {
      // If the output path is a directory, the file name is the table name
      outputPath = FileUtil.joinPath(outputPath, tableName + ".tsv");
    } else {
      if (FileUtil.existsParent(outputPath)) {
        // Error if the parent directory does not exist
        throw new RuntimeException("Output parent directory does not exist. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }

    if (FileUtil.exists(outputPath)) {
      // Check that the output file does not already exist
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    int count = 0;
    // Database connection
    try (final Connection conn = DbUtil.getConnByUrl(jdbcUrl, dbUser, dbPass, super.traceCode)) {
      // Check table existence
      if (!DbUtil.isExistsTable(conn, tableName)) {
        throw new RuntimeException("Specified table does not exist. " + LogUtil.joinKeyVal("table", tableName));
      }

      super.logger.info("Starting DB data export. " + LogUtil.joinKeyVal("table", tableName));
      // Retrieve primary keys
      final String[] pkeys = DbUtil.getPrimaryKeys(conn, tableName);
      // SQL
      final SqlBuilder sb = new SqlBuilder();
      sb.addQuery("SELECT * FROM ").addQuery(tableName);
      if (!ValUtil.isBlank(where)) {
        if (where.toUpperCase().startsWith("WHERE ")) {
          sb.addQuery(where);
        } else {
          sb.addQuery("WHERE ").addQuery(where);
        }
      }
      if (!ValUtil.isEmpty(pkeys)) {
        sb.addQuery(" ORDER BY ");
        for (final String key : pkeys) {
          sb.addQuery(key).addQuery(",");
        }
        sb.delLastChar();
      }

      // Retrieve from DB and output to file
      try (final SqlResultSet rSet = SqlUtil.select(conn, sb);
          final IoTsvWriter tw = new IoTsvWriter(outputPath)) {
        // Output column names
        tw.println(rSet.getItemNames());
        for (final IoItems row : rSet) {
          tw.println(row);
        }
        count = rSet.getReadedCount();
      }
    }

    final String outPath;
    if (isZip) {
      final String zipPath = FileUtil.replaceTypeMark(outputPath, "zip");
      FileUtil.zip(outputPath,  ValUtil.UTF8, zipPath);
      // Delete the text file
      (new File(outputPath)).delete();
      outPath = zipPath;
    } else {
      outPath = outputPath;
    }
    if (ValUtil.isBlank(where)) {
      super.logger.info("DB data exported successfully. " + LogUtil.joinKeyVal("table", tableName, "count", count, "file", outPath));
    } else {
      super.logger.info("DB data exported successfully. " + LogUtil.joinKeyVal("table", tableName, "count", count, "file", outPath, "where", where));
    }
  }
}
