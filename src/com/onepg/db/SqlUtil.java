package com.onepg.db;

import com.onepg.db.DbUtil.DbmsName;
import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL execution utility class.
 */
public final class SqlUtil {

  /** Log writer. */
  private static final LogWriter logger = LogUtil.newLogWriter(SqlUtil.class);

  /** Default fetch size. */
  private static final int DEFAULT_FETCH_SIZE = 500;

  /** Date/time formatter: Date SQL standard. */
  private static final DateTimeFormatter DTF_SQL_DATE =
      DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
  /** Date/time formatter: Timestamp SQL standard. */
  private static final DateTimeFormatter DTF_SQL_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * SQL to avoid ORACLE protocol violation.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com (reference)</a>
   */
  public static final String ORACLE_PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";
      
  /**
   * Database column class type.<br>
   * <ul>
   * <li>Indicates the Java variable class corresponding to the database column type.</li>
   * <li>Numeric types are unified to BigDecimal.</li>
   * <li>StringToDateCls and StringToTsCls are for SQLite, converting strings to date/timestamp.</li>
   * </ul>
   */
  enum ItemClsType {
    STRING_CLS, BIGDECIMAL_CLS, DATE_CLS, TIMESTAMP_CLS, STRING_TO_DATE_CLS, STRING_TO_TS_CLS
  }

  /**
   * Constructor.
   */
  private SqlUtil() {
    // No processing
  }

  /**
   * Retrieves one record (error on zero records).<br>
   * <ul>
   * <li>Uses this method with the premise that only one target record exists.</li>
   * <li>Throws a runtime exception if the result is zero records.</li>
   * <li>Throws a runtime exception if the result is multiple records.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the row data map
   */
  public static IoItems selectOneExists(final Connection conn, final SqlBean sb) {
    final IoItems retMap = selectFirstRec(conn, sb, false);
    if (ValUtil.isNull(retMap)) {
      throw new RuntimeException("No matching data exists. " + sb.toString());
    }
    return retMap;
  }

  /**
   * Retrieves one record.<br>
   * <ul>
   * <li>Uses this method with the premise that only one target record exists.</li>
   * <li>Returns <code>null</code> if the result is zero records.</li>
   * <li>Throws a runtime exception if the result is multiple records.</li>
   * <li>Column physical names become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the row data map
   */
  public static IoItems selectOne(final Connection conn, final SqlBean sb) {
    return selectFirstRec(conn, sb, false);
  }

  /**
   * Retrieves one record (multiple records acceptable).<br>
   * <ul>
   * <li>Returns <code>null</code> if the result is zero records.</li>
   * <li>Returns the first record without error even if multiple records can be retrieved.</li>
   * <li>Column physical names become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the row data map
   */
  public static IoItems selectOneMultiIgnore(final Connection conn, final SqlBean sb) {
    return selectFirstRec(conn, sb, true);
  }

  /**
   * Retrieves the first record.<br>
   * <ul>
   * <li>Returns <code>null</code> if the result is zero records.</li>
   * <li>Passes <code>true</code> to the multiDataIgnore parameter to avoid error even if multiple records can be retrieved.<br>
   * Returns the first record if multiple records can be retrieved.</li>
   * <li>Throws a runtime exception if the multiDataIgnore parameter is passed <code>false</code> and multiple records can be retrieved.</li>
   * <li>Column physical names become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param multiDataIgnore <code>true</code> to avoid error even if multiple records can be retrieved
   * @return the row data map (may be <code>null</code>)
   */
  private static IoItems selectFirstRec(final Connection conn, final SqlBean sb, final boolean multiDataIgnore) {

    // Bulk retrieval
    final IoRows rows = selectBulkByLimitCount(conn, sb, 1);
    if (rows.size() <= 0) {
      // No data
      return null;
    }

    if (rows.isLimitOver()) {
      // Retrieved one or more rows
      if (!multiDataIgnore) {
        throw new RuntimeException("Multiple records were retrieved. " + sb.toString());
      }
    }
    return rows.get(0);
  }

  /**
   * Retrieves multiple records.<br>
   * <ul>
   * <li>Returns as <code>SqlResultSet</code>.</li>
   * <li>Column physical names of row maps retrieved from the <code>SqlResultSet</code>
   * iterator become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Uses in try clause (try-with-resources statement).</li>
   * <li>This class sets the default fetch size to 500. Uses <code>SqlUtil#selectFetchAll(Connection, SqlBean)</code> to fetch all records.</li>
   * <li>About fetch size per DBMS
   * <ul>
   * <li>Oracle defaults to 10 records which is small, so specifies fetch size.</li>
   * <li>PostgreSQL defaults to fetch all records which may cause OutOfMemory, so specifies fetch size.</li>
   * <li>PostgreSQL causes cursor invalid error (SQLSTATE 34000) when specifying fetch size (not fetching all records), updating retrieved data, and performing intermediate commit. In such case, stops intermediate commit or fetches all records.<br>
   * Also resolves by dividing and retrieving data with SQL LIMIT clause though processing becomes complex.</li>
   * <li>MS-SqlServer may not follow the specified fetch size, so divides and retrieves data with SQL LIMIT clause when there is a possibility of OutOfMemory.</li>
   * </ul>
   * </li>
   * </ul>
   * <pre>[Example]
   * <code>try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), sqlBuilder);) {
   *   for (final IoItems row : rSet) {
   *     : Omitted
   *   }
   *   if (rSet.getReadedCount() <= 0) {
   *     // For zero records case
   *   }
   * }</code>
   * </pre>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the SQL result set
   */
  public static SqlResultSet select(final Connection conn, final SqlBean sb) {
    return selectByFetchSize(conn, sb, DEFAULT_FETCH_SIZE);
  }

  /**
   * Retrieves multiple records (fetch all records).<br>
   * <ul>
   * <li>Basically does not use this method.</li>
   * <li>Uses this method only when defects occur without fetching all records.</li>
   * <li>This method may cause memory error when retrieving a large number of records.</li>
   * </ul>
   * 
   * @see #select(Connection, SqlBean)
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the SQL result set
   */
  public static SqlResultSet selectFetchAll(final Connection conn, final SqlBean sb) {
    return selectByFetchSize(conn, sb, 0);
  }

  /**
   * Retrieves multiple records in bulk.<br>
   * <ul>
   * <li>Returns multiple row list.</li>
   * <li>Returns a list with zero size if the result is zero records.</li>
   * <li>Column physical names of the map for one row become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>This method consumes memory, so uses <code>#select(Connection, SqlBean)</code>
   * for loop processing.</li>
   * <li>This method may cause memory error when retrieving a large number of records.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param limitCount the retrieval count limit
   * @return the multiple row list
   */
  public static IoRows selectBulk(final Connection conn, final SqlBean sb, final int limitCount) {
    return selectBulkByLimitCount(conn, sb, limitCount);
  }

  /**
   * Retrieves multiple records in bulk (retrieves all records).<br>
   * <ul>
   * <li>Returns multiple row list.</li>
   * <li>Returns a list with zero size if the result is zero records.</li>
   * <li>Column physical names of the map for one row become lowercase alphabetic characters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>This method consumes memory, so uses <code>#select(Connection, SqlBean)</code>
   * for loop processing.</li>
   * <li>This method may cause memory error when retrieving a large number of records.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the multiple row list
   */
  public static IoRows selectBulkAll(final Connection conn, final SqlBean sb) {
    return selectBulkByLimitCount(conn, sb, 0);
  }

  /**
   * Retrieves multiple records in bulk.
   * 
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param limitCount the retrieval count limit (retrieves all records if zero or less)
   * @return the multiple row list
   */
  private static IoRows selectBulkByLimitCount(final Connection conn, final SqlBean sb, final int limitCount) {
    // Fetch size
    final int fetchSize;
    if (limitCount <= 0 || DEFAULT_FETCH_SIZE < limitCount) {
      // To reduce memory usage as much as possible
      // Uses default fetch size for all records or when exceeding default fetch size
      fetchSize = DEFAULT_FETCH_SIZE;
    } else {
      // Adds +1 for limit exceeded determination
      fetchSize = limitCount + 1;
    }

    final IoRows rows = new IoRows();
    try (final SqlResultSet rSet = selectByFetchSize(conn, sb, fetchSize);) {
      final Iterator<IoItems> ite = rSet.iterator();
      while (ite.hasNext()) {
        final IoItems row = ite.next();
        rows.add(row);
        if (limitCount > 0 && rows.size() >= limitCount) {
          // Terminates because reached limit count
          if (ite.hasNext()) {
            // Still has data = limit exceeded
            rows.setLimitOver(true);
          }
          break;
        }
      }
      if (rSet.getReadedCount() > 0) {
        // Sets begin row number and end row number for cases other than zero records
        rows.setBeginRowNo(1);
        rows.setEndRowNo(rSet.getReadedCount());
      }
    }
    return rows;
  }

  /**
   * Retrieves multiple records with specified fetch size.
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param fetchSize the fetch size
   * @return the SQL result set
   */
  private static SqlResultSet selectByFetchSize(final Connection conn, final SqlBean sb,
      final int fetchSize) {
        
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String sql = sb.getQuery();
    final List<Object> bindValues = sb.getBindValues();
    if (logger.isDevelopMode()) {
      // Outputs SQL log
      logger.develop("SQL#SELECT execution. " + LogUtil.joinKeyVal("sql", sb, "fetchSize", fetchSize));
    }
    PreparedStatement stmt = null;
    ResultSet rset = null;
    try {
      // Generates statement
      stmt = conn.prepareStatement(sql);
      // Sets parameters to statement
      setStmtParameters(stmt, bindValues, dbmsName);
      // Sets fetch-related properties to statement
      setStmtFetchProperty(stmt, fetchSize);

      // Executes SQL
      rset = stmt.executeQuery();
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMap(rset);

      // Connection serial code
      final String serialCode = DbUtil.getSerialCode(conn);

      // SQL result set
      final SqlResultSet retSet = new SqlResultSet(stmt, rset, itemClsMap, serialCode);
      return retSet;

    } catch (SQLException e) {
      DbUtil.closeQuietly(rset);
      DbUtil.closeQuietly(stmt);
      throw new RuntimeException("Exception error occurred during data retrieval. " + LogUtil.joinKeyVal("sql",
          sb, "fetchSize", fetchSize), e);
    }
  }

  /**
   * Inserts one record with specified table.<br>
   * <ul>
   * <li>Inserts one record by specifying table name.</li>
   * <li>Ignores parameters that do not exist in the table.</li>
   * <li>Attention is required because if a column is added to the table after implementation completion and the column name already exists in the parameters,<br>
   * the value will be inserted into the added column without implementation modification.</li>
   * <li>Throws a runtime exception if the affected count is zero for reasons other than unique constraint violation.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values
   * @return <code>false</code> for unique constraint violation, <code>true</code> if one record is normally inserted
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skips parameters that do not exist in the table
          continue;
        }

        // Column class type
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Value retrieval by column class type
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Adds SQL
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }

      // Assembles SQL
      sbInto.delLastChar();
      sbVals.delLastChar();
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try{
      // Executes SQL
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // Unique constraint violation error
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * Inserts one record with specified table (automatic timestamp setting).<br>
   * <ul>
   * <li>Inserts one record by specifying table name.</li>
   * <li>Sets timestamp for optimistic concurrency control.</li>
   * <li>Ignores parameters that do not exist in the table.</li>
   * <li>Attention is required because if a column is added to the table after implementation completion and the column name already exists in the parameters,<br>
   * the value will be inserted into the added column without implementation modification.</li>
   * <li>Throws a runtime exception if the affected count is zero for reasons other than unique constraint violation.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values
   * @param tsItem    the timestamp column name (for optimistic concurrency control)
   * @return <code>false</code> for unique constraint violation, <code>true</code> if one record is normally inserted
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String curTs = getCurrentTimestampSql(dbmsName);

    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skips parameters that do not exist in the table
          continue;
        }
        if (tsItem.equals(itemName)) {
          // Skips timestamp
          continue;
        }

        // Column class type
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Value retrieval by column class type
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Adds SQL
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }
      // Adds timestamp SQL
      sbInto.addQuery(tsItem);
      sbVals.addQuery(curTs);

      // Assembles SQL
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try {
      // Executes SQL
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // Unique constraint violation error
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * Updates one record with specified table.<br>
   * <ul>
   * <li>Updates one record by specifying table name.</li>
   * <li>Throws a runtime exception if multiple records are updated.</li>
   * <li>Ignores parameters that do not exist in the table.</li>
   * <li>Attention is required because if a column is added to the table after implementation completion and the column name already exists in the parameters,<br>
   * the value will be updated in the added column without implementation modification.</li>
   * <li>WHERE clause is created by key columns.</li>
   * <li>Key columns must be included in parameter values.</li>
   * <li>Alphabetic characters of key column names must be specified in lowercase. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key column names)
   * @param keyItems  the key column names
   *
   * @return <code>true</code> if one record is updated, <code>false</code> if zero records
   */
  public static boolean updateOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems) {
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    final int ret = update(conn, tableName, params, keyItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were updated. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "params", params));
    }
    return (ret == 1);
  }

  /**
   * Updates one record with specified table (timestamp exclusive control update).<br>
   * <ul>
   * <li>Updates one record by specifying table name.</li>
   * <li>Throws a runtime exception if multiple records are updated.</li>
   * <li>Performs optimistic concurrency control with timestamp.</li>
   * <li>Ignores parameters that do not exist in the table.</li>
   * <li>Attention is required because if a column is added to the table after implementation completion and the column name already exists in the parameters,<br>
   * the value will be updated in the added column without implementation modification.</li>
   * <li>WHERE clause is created by key columns and timestamp column (exclusive control).</li>
   * <li>Key columns and timestamp column must be included in parameter values.</li>
   * <li>Alphabetic characters of key column names and timestamp column name must be specified in lowercase. (<code>AbstractIoTypeMap</code>
   * key rule)</li>
   * <li>Timestamp column is updated with current date/time.</li>
   * <li>Uses <code>#updateOne(Connection, String, AbstractIoTypeMap, String[])</code> when timestamp exclusive control is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key column names)
   * @param keyItems  the key column names
   * @param tsItem    the timestamp column name (for optimistic concurrency control)
   *
   * @return <code>true</code> if one record is updated, <code>false</code> if zero records
   */
  public static boolean updateOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems, final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    if (ValUtil.isBlank(tsItem)) {
      throw new RuntimeException("Timestamp column name is required. ");
    }
    
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String curTs = getCurrentTimestampSql(dbmsName);
    
    final SqlBuilder sb = new SqlBuilder();
    try {
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
      whereItems[keyItems.length] = tsItem;

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Adds SET clause
      addSetQuery(sb, params, whereItems, itemClsMap);
      // Updates timestamp column with current date/time
      sb.addQuery(",").addQuery(tsItem).addQuery("=").addQuery(curTs);
      // Adds WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params), e);
    }

    try {
      // Executes SQL
      final int ret = executeSql(conn, sb);
      if (ret > 1) {
        throw new RuntimeException("Multiple records were updated. " + LogUtil.joinKeyVal("sql", sb));
      }
      return (ret == 1);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Updates with specified table.<br>
   * <ul>
   * <li>Updates multiple records by specifying table name.</li>
   * <li>Ignores parameters that do not exist in the table.</li>
   * <li>Attention is required because if a column is added to the table after implementation completion and the column name already exists in the parameters,<br>
   * the value will be updated in the added column without implementation modification.</li>
   * <li>WHERE clause is created by filter condition columns.</li>
   * <li>Filter condition columns must be included in parameter values.</li>
   * <li>Alphabetic characters of filter condition column names must be specified in lowercase. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       the database connection
   * @param tableName  the table name
   * @param params     the parameter values (including filter condition column names)
   * @param whereItems the filter condition column names (optional) <code>null</code> if omitted
   *
   * @return the update count
   */
  public static int update(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Adds SET clause
      addSetQuery(sb, params, whereItems, itemClsMap);
      // Adds WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Executes SQL
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Deletes one record with specified table.<br>
   * <ul>
   * <li>Deletes one record by specifying table name.</li>
   * <li>Throws a runtime exception if multiple records are deleted.</li>
   * <li>WHERE clause is created by key columns.</li>
   * <li>Key columns must be included in parameter values.</li>
   * <li>Alphabetic characters of key column names must be specified in lowercase. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key column names)
   * @param keyItems  the key column names
   *
   * @return <code>true</code> if one record is deleted, <code>false</code> if zero records
   */
  public static boolean deleteOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems) {
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    final int ret = delete(conn, tableName, params, keyItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were deleted. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "params", params));
    }
    return (ret == 1);
  }

  /**
   * Deletes one record with specified table (timestamp exclusive control delete).<br>
   * <ul>
   * <li>Deletes one record by specifying table name.</li>
   * <li>Throws a runtime exception if multiple records are deleted.</li>
   * <li>Performs optimistic concurrency control with timestamp.</li>
   * <li>WHERE clause is created by key columns and timestamp column (exclusive control).</li>
   * <li>Key columns and timestamp column must be included in parameter values.</li>
   * <li>Alphabetic characters of key column names and timestamp column name must be specified in lowercase. (<code>AbstractIoTypeMap</code>
   * key rule)</li>
   * <li>Uses
   * <code>#deleteOne(Connection, String, AbstractIoTypeMap, String[])</code>
   * when timestamp exclusive control is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key column names)
   * @param keyItems  the key column names
   * @param tsItem    the timestamp column name (for optimistic concurrency control)
   *
   * @return <code>true</code> if one record is deleted, <code>false</code> if zero records
   */
  public static boolean deleteOne(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] keyItems, final String tsItem) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    if (ValUtil.isEmpty(keyItems)) {
      throw new RuntimeException("Key column names are required. ");
    }
    if (ValUtil.isBlank(tsItem)) {
      throw new RuntimeException("Timestamp column name is required. ");
    }
    
    final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
    whereItems[keyItems.length] = tsItem;
    final int ret = delete(conn, tableName, params, whereItems);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were deleted. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params));
    }
    return (ret == 1);
  }

  /**
   * Deletes with specified table.<br>
   * <ul>
   * <li>Deletes multiple records by specifying table name.</li>
   * <li>WHERE clause is created by filter condition columns.</li>
   * <li>Filter condition columns must be included in parameter values.</li>
   * <li>Alphabetic characters of filter condition column names must be specified in lowercase. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param tableName the table name
   * @param params the parameter values (including filter condition column names)
   * @param whereItems the filter condition column names
   *
   * @return the delete count
   */
  public static int delete(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // Database column name and class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("DELETE FROM ").addQuery(tableName);
      // Adds WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);
      
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Executes SQL
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Adds SET clause.
   *
   * @param sb         the SQL builder
   * @param params     the parameter values (including filter condition column names)
   * @param whereItems the filter condition column names (optional) <code>null</code> if omitted
   * @param itemClsMap the database column name and class type map
   */
  private static void addSetQuery(final SqlBuilder sb, final AbstractIoTypeMap params,
      final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    // Creates SET clause
    sb.addQuery(" SET ");

    // Filter condition column list for existence check
    final List<String> whereItemList;
    if (ValUtil.isEmpty(whereItems)) {
      whereItemList = Arrays.asList(new String[] {});
    } else {
      whereItemList = Arrays.asList(whereItems);
    }

    for (final String itemName : params.keySet()) {
      if (!itemClsMap.containsKey(itemName)) {
        // Skips parameters that do not exist in the table
        continue;
      }
      if (whereItemList.contains(itemName)) {
        // Skips if exists in filter condition columns
        continue;
      }

      // Column class type
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // Value retrieval by column class type
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // Adds SQL
      sb.addQuery(itemName).addQuery("=?", param).addQuery(",");
    }
    sb.delLastChar();
  }

  /**
   * Adds WHERE clause.
   *
   * @param sb         the SQL builder
   * @param tableName  the table name
   * @param params     the parameter values (including filter condition column names)
   * @param whereItems the filter condition column names (optional) <code>null</code> if omitted
   * @param itemClsMap the database column name and class type map
   */
  private static void addWhereQuery(final SqlBuilder sb, final String tableName,
      final AbstractIoTypeMap params, final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    if (ValUtil.isEmpty(whereItems)) {
      return;
    }

    // Creates WHERE clause
    sb.addQuery(" WHERE ");
    for (final String itemName : whereItems) {
      if (!itemClsMap.containsKey(itemName)) {
        // Skips parameters that do not exist in the table
        continue;
      }
      if (!params.containsKey(itemName)) {
        // Throws error if filter condition column does not exist in parameters
        throw new RuntimeException("Extraction condition field does not exist in parameters. " + LogUtil.joinKeyVal("tableName",
            tableName, "whereItemName", itemName, "params", params));
      }

      // Column class type
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // Value retrieval by column class type
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // Adds SQL
      sb.addQuery(itemName).addQuery("=?", param).addQuery(" AND ");
    }
    sb.delLastChar(4);
  }

  /**
   * SQL one record insert, update, or delete.<br>
   * <ul>
   * <li>Throws a runtime exception if the affected count is multiple records.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return <code>true</code> if the affected count is one record, <code>false</code> if zero records
   */
  public static boolean executeOne(final Connection conn, final SqlBean sb) {
    final int ret = execute(conn, sb);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were affected. " + LogUtil.joinKeyVal("sql", sb));
    }
    return (ret == 1);
  }

  /**
   * SQL insert, update, or delete.
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the affected count
   */
  public static int execute(final Connection conn, final SqlBean sb) {
    try {
      return executeSql(conn, sb);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during SQL execution. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Executes SQL.
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the affected count
   * @throws SQLException the SQL exception error
   */
  private static int executeSql(final Connection conn, final SqlBean sb)
      throws SQLException {
        
    if (logger.isDevelopMode()) {
      // Outputs SQL log
      logger.develop("SQL#EXECUTE execution. " + LogUtil.joinKeyVal("sql", sb));
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // Generates statement
    try (final PreparedStatement stmt = conn.prepareStatement(sb.getQuery());) {
      // Sets parameters to statement
      setStmtParameters(stmt, sb.getBindValues(), dbmsName);
      // Executes SQL
      final int ret = stmt.executeUpdate();
      return ret;
    }
  }

  /**
   * Sets parameters to statement.
   *
   * @param stmt     the statement
   * @param bindValues   the bind value list
   * @param dbmsName the DBMS name
   * @throws SQLException the SQL exception error
   */
  private static void setStmtParameters(final PreparedStatement stmt, final List<Object> bindValues,
      final DbmsName dbmsName) throws SQLException {
    int bindNo = 0;
    for (final Object bindValue : bindValues) {
      ++bindNo;
      if (dbmsName == DbmsName.SQLITE) {
        if (ValUtil.isNull(bindValue)) {
          stmt.setObject(bindNo, bindValue);
        } else if (bindValue instanceof java.sql.Timestamp) {
          // Converts and sets to String for java.sql.Timestamp
          final java.sql.Timestamp ts = (java.sql.Timestamp) bindValue;
          final LocalDateTime ldt = ts.toLocalDateTime();
          final String s = DTF_SQL_TIMESTAMP.format(ldt);
          stmt.setString(bindNo, s);
        } else if (bindValue instanceof java.sql.Date) {
          // Converts and sets to String for java.sql.Date
          final java.sql.Date dt = (java.sql.Date) bindValue;
          final LocalDate ld = dt.toLocalDate();
          final String s = DTF_SQL_DATE.format(ld);
          stmt.setString(bindNo, s);
        } else {
          stmt.setObject(bindNo, bindValue);
        }
      } else {
        stmt.setObject(bindNo, bindValue);
      }
    }
  }

  /**
   * Sets fetch-related properties to statement.
   *
   * @param stmt the statement
   * @param fetchSize the fetch size
   * @throws SQLException the SQL exception error
   */
  private static void setStmtFetchProperty(final PreparedStatement stmt, final int fetchSize)
      throws SQLException {
    // Sets fetch direction and fetch size
    stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    stmt.setFetchSize(fetchSize);
  }

  /**
   * Creates database column name and class type map from result set.<br>
   * <ul>
   * <li>Creates a map of column names and class types from the result set.</li>
   * <li>The map preserves column order.</li>
   * <li>Converts column physical names to lowercase alphabetic characters. (Aligns with key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param rset the result set
   * @return the database column name and class type map
   * @throws SQLException the SQL exception error
   */
  private static Map<String, ItemClsType> createItemNameClsMap(final ResultSet rset)
      throws SQLException {

    // Database column name and class type map
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS name
    final DbmsName dbmsName = DbUtil.getDbmsName(rset);
    // Result set metadata
    final ResultSetMetaData rmeta = rset.getMetaData();
    // Column count
    final int itemCount = rmeta.getColumnCount();
    // Result set column loop
    for (int c = 1; c <= itemCount; c++) {
      // Column name
      final String itemName;
      if (DbmsName.DB2 == dbmsName) {
        // Because DB2's #getColumnName returns original column name instead of alias
        itemName = rmeta.getColumnLabel(c).toLowerCase();
      } else {
        itemName = rmeta.getColumnName(c).toLowerCase();
      }
      // Type number
      final int typeNo = rmeta.getColumnType(c);
      // Type name
      // Oracle's DATE type also has time and is judged as TIMESTAMP, so needs to judge by type name.
      final String typeName = rmeta.getColumnTypeName(c).toUpperCase();
      // Column class type
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * Creates database column name and class type map with specified table.<br>
   * <ul>
   * <li>Creates a map of column names and class types from database metadata.</li>
   * <li>The map preserves column order.</li>
   * <li>Converts column physical names to lowercase alphabetic characters. (Aligns with key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param tableName the table name
   * @throws SQLException the SQL exception error
   */
  private static Map<String, ItemClsType> createItemNameClsMapByMeta(final Connection conn,
      final String tableName) throws SQLException {

    // Database column name and class type map
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS name
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // Database metadata
    final DatabaseMetaData cmeta = conn.getMetaData();
    // Column information result set
    final ResultSet rset = cmeta.getColumns(null, null, tableName, null);

    while (rset.next()) {
      // Column name
      final String itemName = rset.getString("COLUMN_NAME").toLowerCase();
      // Type number
      final int typeNo = rset.getInt("DATA_TYPE");
      // Type name
      // Oracle's DATE type also has time and is judged as TIMESTAMP, so needs to judge by type name.
      final String typeName = rset.getString("TYPE_NAME");
      // Column class type
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * Converts database column class type.
   *
   * @param typeNo   the type number
   * @param typeName the type name uppercase
   * @param dbmsName the DBMS name
   * @return the class type
   */
  private static ItemClsType convItemClsType(final int typeNo, final String typeName, final DbmsName dbmsName) {
    final ItemClsType itemCls;
    if (/* JDBC types mapped to BigDecimal */ Types.DECIMAL == typeNo || Types.NUMERIC == typeNo
        || /* JDBC types mapped to Integer */ Types.TINYINT == typeNo || Types.SMALLINT == typeNo
        || Types.INTEGER == typeNo || /* JDBC types mapped to Long */ Types.BIGINT == typeNo
        || /* JDBC types mapped to Float */ Types.FLOAT == typeNo || Types.REAL == typeNo
        || /* JDBC types mapped to Double */ Types.DOUBLE == typeNo) {
      // Unifies numeric types to BigDecimal
      itemCls = ItemClsType.BIGDECIMAL_CLS;
    } else if (Types.DATE == typeNo) {
      if ("DATETIME".equals(typeName) && DbmsName.MSSQL == dbmsName) {
        itemCls = ItemClsType.TIMESTAMP_CLS;
      } else if (DbmsName.SQLITE == dbmsName) {
        // When Types.DATE is returned from result set in SQLite, it is actually a string so needs conversion (see #createIoItemsFromResultSet)
        itemCls = ItemClsType.STRING_TO_DATE_CLS;
      } else {
        itemCls = ItemClsType.DATE_CLS;
      }
    } else if (Types.TIMESTAMP == typeNo || Types.TIMESTAMP_WITH_TIMEZONE == typeNo) {
      if ("DATE".equals(typeName) && DbmsName.ORACLE == dbmsName) {
        itemCls = ItemClsType.DATE_CLS;
      } else if (DbmsName.SQLITE == dbmsName) {
        // When Types.TIMESTAMP is returned from result set in SQLite, it is actually a string so needs conversion (see #createIoItemsFromResultSet)
        itemCls = ItemClsType.STRING_TO_TS_CLS;
      } else {
        itemCls = ItemClsType.TIMESTAMP_CLS;
      }
    } else {
      if (DbmsName.SQLITE == dbmsName) {
        if ("DATE".equals(typeName)) {
          // SQLite table metadata has Types.VARCHAR with type name "DATE"
          itemCls = ItemClsType.STRING_TO_DATE_CLS;
        } else if ("TIMESTAMP".equals(typeName)) {
          // SQLite table metadata has Types.VARCHAR with type name "TIMESTAMP"
          itemCls = ItemClsType.STRING_TO_TS_CLS;
        } else {
          itemCls = ItemClsType.STRING_CLS;
        }
      } else {
        // Unifies string types to String
        itemCls = ItemClsType.STRING_CLS;
      }
    }
    return itemCls;
  }

  /**
   * Creates result set row map.<br>
   * <ul>
   * <li>Returns values of the current row of result set as a map.</li>
   * </ul>
   *
   * @param rset the result set
   * @param itemClsMap the database column name and class type map
   * @return the row map
   */
  static IoItems createIoItemsFromResultSet(final ResultSet rset,
      final Map<String, ItemClsType> itemClsMap) {

    // Row map
    final IoItems rowMap = new IoItems();

    // Database column loop
    for (final Map.Entry<String, ItemClsType> ent : itemClsMap.entrySet()) {
      // Column name
      final String itemName = ent.getKey();
      try {
        // Column class type
        final ItemClsType itemCls = ent.getValue();
        // Sets value by column class type
        if (ItemClsType.STRING_CLS == itemCls) {
          final String value = rset.getString(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.BIGDECIMAL_CLS == itemCls) {
          final BigDecimal value = rset.getBigDecimal(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.DATE_CLS == itemCls) {
          final java.sql.Date value = rset.getDate(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.TIMESTAMP_CLS == itemCls) {
          final java.sql.Timestamp value = rset.getTimestamp(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.STRING_TO_DATE_CLS == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value) || value.length() != 10) {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDate ld = LocalDate.parse(value, DTF_SQL_DATE);
          // Should set as Date originally, but sets as is because it is converted to LocalDate and set within IoItems
          rowMap.put(itemName, ld);
        } else if (ItemClsType.STRING_TO_TS_CLS == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value)) {
            rowMap.putNull(itemName);
            continue;
          }
          final String ajustVal;
          final int len = value.length();
          if (len == 19) {
            // Adds .000000 when there is no fractional seconds ("uuuu-MM-dd HH:mm:ss" = 19 characters)
            ajustVal = value + ".000000";
          } else if (19 < len && len < 26) {
            // Adds 000000 for insufficient fractional seconds digits ("uuuu-MM-dd HH:mm:ss.SSSSSS" = 26 characters)
            ajustVal = ValUtil.substring(value + "000000", 0, 26);
          } else if (len == 26) {
            ajustVal = value;
          } else if (len > 26) {
            // Cuts fractional seconds digits at 6 digits ("uuuu-MM-dd HH:mm:ss.SSSSSS" = 26 characters)
            ajustVal = ValUtil.substring(value, 0, 26);
          } else {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDateTime ldt = LocalDateTime.parse(ajustVal, DTF_SQL_TIMESTAMP);
          // Should set as Timestamp originally, but sets as is because it is converted to LocalDateTime and set within IoItems
          rowMap.put(itemName, ldt);
        } else {
          throw new RuntimeException("Item class type is invalid. "
              + LogUtil.joinKeyVal("itemName", itemName, "itemCls", itemCls.toString()));
        }
      } catch (final Exception e) {
        throw new RuntimeException("Exception error occurred while getting value from result set. "
                                + LogUtil.joinKeyVal("itemName", itemName), e);
      }
    }
    return rowMap;
  }

  /**
   * Retrieves parameter value.<br>
   * <ul>
   * <li>Retrieves and returns value by using parameter getter appropriately based on database column class type.</li>
   * </ul>
   *
   * @param params the parameters
   * @param itemName the column name
   * @param itemCls the database column class type
   * @return the parameter value
   */
  private static Object getValueFromIoItemsByItemCls(final AbstractIoTypeMap params, final String itemName,
      final ItemClsType itemCls) {
    final Object param;
    if (ItemClsType.STRING_CLS == itemCls) {
      param = params.getStringNullable(itemName);
    } else if (ItemClsType.BIGDECIMAL_CLS == itemCls) {
      param = params.getBigDecimalNullable(itemName);
    } else if (ItemClsType.DATE_CLS == itemCls) {
      param = params.getSqlDateNullable(itemName);
    } else if (ItemClsType.TIMESTAMP_CLS == itemCls) {
      param = params.getSqlTimestampNullable(itemName);
    } else if (ItemClsType.STRING_TO_DATE_CLS == itemCls) {
      final LocalDate ld = params.getDateNullable(itemName);
      if (ValUtil.isNull(ld)) {
        param = null;
      } else {
        param = ld.format(DTF_SQL_DATE);
      }
    } else if (ItemClsType.STRING_TO_TS_CLS == itemCls) {
      final LocalDateTime ldt = params.getDateTimeNullable(itemName);
      if (ValUtil.isNull(ldt)) {
        param = null;
      } else {
        param = ldt.format(DTF_SQL_TIMESTAMP);
      }
    } else {
      throw new RuntimeException("Item class type is invalid. "
          + LogUtil.joinKeyVal("itemName", itemName, "itemCls", itemCls.toString()));
    }
    return param;
  }

  /**
   * Judges unique constraint violation error.<br>
   * <ul>
   * <li>Judges whether it is a unique constraint violation error for each DBMS.</li>
   * <li>Returns <code>true</code> for unique constraint violation.</li>
   * </ul>
   *
   * @param e the SQL exception
   * @param dbmsName the DBMS name
   * @return <code>true</code> for unique constraint violation error
   */
  private static boolean isUniqueKeyErr(final SQLException e, final DbmsName dbmsName) {
    // Oracle unique constraint violation error check
    if (dbmsName == DbmsName.ORACLE && e.getErrorCode() == 1) {
      return true;
    }
    // PostgreSQL unique constraint violation error check
    if (dbmsName == DbmsName.POSTGRESQL && "23505".equals(e.getSQLState())) {
      return true;
    } 
    // MS-SqlServer unique constraint violation error check
    if (dbmsName == DbmsName.MSSQL && "23000".equals(e.getSQLState())
        && e.getMessage().contains("Violation of UNIQUE KEY constraint")) {
      return true;
    }
    // SQLite unique constraint violation error check
    if (dbmsName == DbmsName.SQLITE && e.getErrorCode() == 19
        && e.getMessage().contains("UNIQUE constraint failed")) {
      return true;
    }
    // DB2 unique constraint violation error check
    if (dbmsName == DbmsName.DB2 && "23505".equals(e.getSQLState())) {
      return true;
    }

    return false;
  }

  /** Timestamp retrieval SQL (6 fractional seconds digits) map. */
  private static final Map<DbmsName, String> SQL_CUR_TS = new HashMap<>();
  /** Timestamp retrieval SQL (6 fractional seconds digits) other. */
  private static final String SQL_CUR_TS_OTHER;
  static {
    // SQLite can only retrieve 3 fractional seconds digits, so adds 000 to make it 6 digits
    SQL_CUR_TS.put(DbmsName.SQLITE, "strftime('%Y-%m-%d %H:%M:%f000', 'now', 'localtime')");
    SQL_CUR_TS.put(DbmsName.MSSQL, "SYSDATETIME()");
    SQL_CUR_TS_OTHER = "CURRENT_TIMESTAMP(6)";
  }

  /**
   * Retrieves current timestamp value retrieval SQL by DBMS.
   *
   * @param dbmsName the DBMS name
   * @return the current timestamp value retrieval SQL
   */
  private static String getCurrentTimestampSql(final DbmsName dbmsName) {
    return SQL_CUR_TS.getOrDefault(dbmsName, SQL_CUR_TS_OTHER);
  }

  /** Date retrieval SELECT statement map. */
  private static final Map<DbmsName, SqlConst> SQL_SELECT_TODAY = new HashMap<>();
  static {
    SQL_SELECT_TODAY.put(DbmsName.POSTGRESQL, SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today").end());
    SQL_SELECT_TODAY.put(DbmsName.ORACLE,    SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM DUAL").end());
    SQL_SELECT_TODAY.put(DbmsName.MSSQL,     SqlConst.begin().addQuery("SELECT CONVERT(VARCHAR, FORMAT(GETDATE(), 'yyyyMMdd')) today").end());
    SQL_SELECT_TODAY.put(DbmsName.SQLITE,    SqlConst.begin().addQuery("SELECT strftime('%Y%m%d', 'now', 'localtime') today").end());
    SQL_SELECT_TODAY.put(DbmsName.DB2, SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM SYSIBM.DUAL").end());
  }
  
  /**
   * Retrieves current date by DBMS.
   *
   * @param conn the database connection
   * @return the current date (YYYYMMDD format)
   */
  public static String getToday(final Connection conn) {
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final SqlConst sc = SQL_SELECT_TODAY.get(dbmsName);
    if (ValUtil.isNull(sc)) {
      throw new RuntimeException("Current date retrieval SQL is undefined for this DBMS. " + LogUtil.joinKeyVal("dbmsName", dbmsName));
    }
    final IoItems ret = selectOne(conn, sc);
    return ret.getString("today");
  }

  /** One byte blank. */
  private static final String ONEBLANK = " ";

  /**
   * Adds SQL.<br>
   * <ul>
   * <li>Adds SQL string to StringBuilder.</li>
   * <li>Adds one character blank at the beginning if the beginning of parameter SQL is blank. (However does not add if existing SQL is empty or the last character is blank)</li>
   * <li>Trims blanks before and after parameter SQL, and replaces two or more blanks with one character blank.</li>
   * <li>Adds one character blank at the end if the end of parameter SQL is blank.</li>
   * </ul>
   * 
   * @param toSb  the destination StringBuilder
   * @param sql the SQL to add
   */
  static void appendQuery(final StringBuilder toSb, final String sql) {
    if (ValUtil.isBlank(sql)) {
      return;
    }

    // Adds one character blank at the beginning if the beginning of parameter SQL is blank
    // However does not add if existing SQL is empty or the last character is blank
    if (sql.startsWith(ONEBLANK) && toSb.length() > 0
        && toSb.charAt(toSb.length() - 1) != ' ') {
      toSb.append(ONEBLANK);
    }

    // Trims blanks before and after
    // Replaces two or more blanks with one character blank
    // However does not replace blanks enclosed in single quotation marks
    toSb.append(trimQuerySpaces(sql));

    // Adds one character blank at the end if the end of parameter SQL is blank
    if (sql.endsWith(ONEBLANK)) {
      toSb.append(ONEBLANK);
    }
  }

  /**
   * Replaces two or more blanks with one character blank in SQL string.<br>
   * <ul>
   * <li>Trims blanks before and after.</li>
   * <li>Replaces two or more blanks with one character blank.</li>
   * <li>Does not replace blanks enclosed in single quotation marks.</li>
   * </ul>
   * 
   * @param sql the SQL
   * @return the result SQL
   */
  private static String trimQuerySpaces(final String sql) {
    if (ValUtil.isBlank(sql)) {
        return ValUtil.BLANK;
    }
    
    final int length = sql.length();
    final char[] chars = sql.toCharArray(); // Speeds up with array access
    final StringBuilder ret = new StringBuilder(length);
    
    boolean inSq = false;
    boolean prevSpace = false;
    int beginPos = 0;
    int endPos = length;
    
    // Pre-calculates trim before and after
    while (beginPos < endPos && Character.isWhitespace(chars[beginPos])) {
        beginPos++;
    }
    while (endPos > beginPos && Character.isWhitespace(chars[endPos - 1])) {
        endPos--;
    }
    
    for (int i = beginPos; i < endPos; i++) {
        final char c = chars[i];

        if (c == '\'' && (i == 0 || chars[i-1] != '\\')) {
            inSq = !inSq;
            ret.append(c);
            prevSpace = false;
        } else if (inSq) {
            ret.append(c);
            prevSpace = false;
        } else if (Character.isWhitespace(c)) {
            if (!prevSpace) {
                ret.append(' ');
                prevSpace = true;
            }
        } else {
            ret.append(c);
            prevSpace = false;
        }
    }
    
    return ret.toString();
  }

}
