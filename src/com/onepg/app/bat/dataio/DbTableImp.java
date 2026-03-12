package com.onepg.app.bat.dataio;

import java.sql.Connection;
import java.util.Map;

import com.onepg.bat.AbstractBatch;
import com.onepg.db.DbUtil;
import com.onepg.db.SqlConst;
import com.onepg.db.SqlUtil;
import com.onepg.db.SqlConst.BindType;
import com.onepg.db.SqlConst.SqlConstBuilder;
import com.onepg.util.FileUtil;
import com.onepg.util.IoItems;
import com.onepg.util.IoTsvReader;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;


/**
 * DB table data import batch class.<br>
 * <ul>
 * <li>Imports data from a TSV file for data I/O into the specified DB table.<br>
 * The TSV file for data I/O assumes the following:
 *   <ul>
 *   <li>Column names in the first row of the file.</li>
 *   <li>The file encoding is UTF-8 and the line separator is LF.</li>
 *   <li><code>null</code> values are escaped.</li>
 *   <li>Newline characters (CRLF, CR, LF) and tab characters within values are escaped.</li>
 *   </ul></li>
 * </li>
 * <li>A JDBC library (jar file) matching the DBMS is required.</li>
 * <li>Arguments to the <code>main</code> method are in URL parameter format. (See <code>AbstractBatch</code>)</li>
 * <li>Arguments are as follows:
 *   <ul>
 *   <li>url: JDBC connection URL [Example] jdbc:postgresql://localhost:5432/db01</li>
 *   <li>user: database user (optional)</li>
 *   <li>pass: database password (optional)</li>
 *   <li>table: target table physical name (optional)</li>
 *   <li>input: input file path</li>
 *   </ul></li>
 * <li>If the target table physical name is omitted, the input file name is used as the table name.</li>
 * <li>A zip-compressed file can also be specified for the input file path.</li>
 * </ul>
 */
public class DbTableImp extends AbstractBatch {

  /**
   * Main processing.
   * @param args arguments
   */
  public static void main(String[] args) {
    System.exit((new DbTableImp()).callMain(args));
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
    // Argument - input file path (required)
    final String inputPath = io.getString("input");
    final String inputFileName = FileUtil.trimTypeMark(FileUtil.getFileName(inputPath));
    // Argument - target table name (optional); treated as lowercase
    final String tableName =  io.getStringOrDefault("table", inputFileName).toLowerCase();

    if (!FileUtil.exists(inputPath)) {
      // Error if the input file does not exist
      throw new RuntimeException("Input file not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // Unzip
    final boolean isZip = (FileUtil.getTypeMark(inputPath).equalsIgnoreCase("zip"));
    
    // Input file path
    final String inPath;
    if (isZip) {
      inPath = FileUtil.unzip(inputPath, FileUtil.getParentPath(inputPath))[0];
    } else {
      inPath = inputPath;
    } 
    
    super.logger.info("Starting DB data import. " + LogUtil.joinKeyVal("table", tableName, "file", inPath));

    // Database connection
    int count = 0;
    // Database connection
    try (final Connection conn = DbUtil.getConnByUrl(jdbcUrl, dbUser, dbPass, super.traceCode);
      final IoTsvReader tr = new IoTsvReader(inPath)) {

      final String[] keys = tr.getKeys();
      if (ValUtil.isEmpty(keys)) {
        // If there is no header row, treat the file as having zero rows and terminate
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("file", inPath));
        return;
      }
      // Check table existence
      if (!DbUtil.isExistsTable(conn, tableName)) {
        throw new RuntimeException("Specified table does not exist. " + LogUtil.joinKeyVal("table", tableName));
      }
      
      // DB column name and bind type map
      final Map<String, BindType> bindType = SqlUtil.createItemBindTypeMapByMeta(conn, tableName);
    
      // Create insert SQL (uses SqlConstBuilder, which is not normally used directly)
      final SqlConstBuilder scb = SqlConst.begin();
      scb.addQuery("INSERT INTO ").addQuery(tableName).addQuery(" ( ");
      for (final String key : keys) {
        scb.addQuery(key).addQuery(",");
      }
      scb.delLastChar();
      scb.addQuery(" ) VALUES ( ");
      for (final String key : keys) {
        if (!bindType.containsKey(key)) {
          throw new RuntimeException("Column name does not exist in the table. " + LogUtil.joinKeyVal("table", tableName, "column", key));
        }
        scb.addQuery("?", key, bindType.get(key)).addQuery(",");
      }
      scb.delLastChar();
      scb.addQuery(" ) ");
      final SqlConst sc = scb.end();
      
      // Read file and insert into DB
      for (final IoItems row : tr) {
        SqlUtil.executeOneCache(conn, sc.bind(row));
        // Commit and log every 5000 records
        if (tr.getReadedCount() % 5000 == 0) {
          super.logger.info("Intermediate commit every 5000 records. " + LogUtil.joinKeyVal("count", tr.getReadedCount()));
          conn.commit();
        }
      }
      if (tr.getReadedCount() == 0) {
        // If only the header row exists
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      } else {
        conn.commit();
      }
      count = tr.getReadedCount();
    }

    if (isZip) {
      FileUtil.delete(inPath);
    }

    super.logger.info("DB data imported successfully. " + LogUtil.joinKeyVal("count", count, "file", inputPath));
  }
}
