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
   * Database column class type.<br>
   * <ul>
   * <li>Indicates the Java variable class corresponding to the database column type.</li>
   * <li>Numeric types are unified to BigDecimal.</li>
   * <li>StringToDateCls and StringToTsCls are for SQLite, converting strings to date/timestamp.</li>
   * </ul>
   */
  enum ItemClsType {
    StringCls, BigDecCls, DateCls, TsCls, StringToDateCls, StringToTsCls
  }

  /**
   * Constructor.
   */
  private SqlUtil() {
    // No processing
  }

  /**
   * Gets one record (error if zero records).<br>
   * <ul>
   * <li>Use when exactly one record is expected to exist.</li>
   * <li>Throws an exception error if zero records are returned.</li>
   * <li>Throws an exception error if multiple records are returned.</li>
   * </ul>
   *
   * @param conn Database connection
   * @param sqlWithParams SQL and parameters
   * @return the row data map
   */
  public static IoItems selectOneExists(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    final IoItems retMap = selectFirstRec(conn, sqlWithParams, false);
    if (ValUtil.isNull(retMap)) {
      throw new RuntimeException("No matching data exists. " + sqlWithParams.toString());
    }
    return retMap;
  }

  /**
   * Gets one record.<br>
   * <ul>
   * <li>Use when exactly one record is expected to exist.</li>
   * <li>Returns <code>null</code> if zero records are returned.</li>
   * <li>Throws an exception error if multiple records are returned.</li>
   * <li>Column physical names are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return the row data map
   */
  public static IoItems selectOne(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    return selectFirstRec(conn, sqlWithParams, false);
  }

  /**
   * Gets one record (multiple records OK).<br>
   * <ul>
   * <li>Returns <code>null</code> if zero records are returned.</li>
   * <li>Returns the first record without error even if multiple records are retrieved.</li>
   * <li>Column physical names are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return the row data map
   */
  public static IoItems selectOneMultiIgnore(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    return selectFirstRec(conn, sqlWithParams, true);
  }

  /**
   * Gets the first record.<br>
   * <ul>
   * <li>Returns <code>null</code> if zero records are returned.</li>
   * <li>Pass <code>true</code> to multiDataIgnore argument to not treat multiple records as an error.<br>
   * Returns the first record if multiple records are retrieved.</li>
   * <li>Throws an exception error if multiDataIgnore argument is <code>false</code> and multiple records are retrieved.</li>
   * <li>Column physical names are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn            Database connection
   * @param sqlWithParams   SQL and parameters
   * @param multiDataIgnore <code>true</code> to not treat multiple records as an error
   * @return the row data map (may be <code>null</code>)
   */
  private static IoItems selectFirstRec(final Connection conn, final AbstractSqlWithParameters sqlWithParams,
      final boolean multiDataIgnore) {

    // Bulk retrieval
    final IoRows rows = selectBulkByLimitCount(conn, sqlWithParams, 1);
    if (rows.size() <= 0) {
      // No data
      return null;
    }

    if (rows.isLimitOver()) {
      // More than one row retrieved
      if (!multiDataIgnore) {
        throw new RuntimeException("Multiple records were retrieved. " + sqlWithParams.toString());
      }
    }
    return rows.get(0);
  }

  /**
   * Gets multiple records.<br>
   * <ul>
   * <li>Returns as <code>SqlResultSet</code>.</li>
   * <li>Column physical names in the row map obtained from <code>SqlResultSet</code>
   * iterator are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use in try clause (try-with-resources statement).</li>
   * <li>This class sets the default fetch size to 500. To fetch all records, use <code>SqlUtil#selectFetchAll(Connection, AbstractSqlWithParameters)</code>.</li>
   * <li>About fetch size per DBMS:
   * <ul>
   * <li>Oracle default is 10 records which is small, so fetch size is specified.</li>
   * <li>PostgreSQL default fetches all records which may cause OutOfMemory, so fetch size is specified.</li>
   * <li>In PostgreSQL, if fetch size is specified (not fetching all), updated retrieved data,
   * and intermediate commit, cursor invalid error (SQLSTATE 34000) occurs. In that case,
   * stop intermediate commits or fetch all records.<br>
   * Alternatively, though more complex, data can be retrieved in chunks using SQL LIMIT clause.</li>
   * <li>In MS-SqlServer, fetch size may not work as specified, so if OutOfMemory is possible,
   * data must be retrieved in chunks using SQL LIMIT clause.</li>
   * </ul>
   * </li>
   * </ul>
   * <pre>[Example]
   * <code>try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), sqlBuilder);) {
   *   for (final IoItems row : rSet) {
   *     : omitted
   *   </code>
   *   if (rSet.getReadedCount() <= 0) {
   *     // If zero records
   *   }
   * }}
   * </pre>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return the SQL result set
   */
  public static SqlResultSet select(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    return selectByFetchSize(conn, sqlWithParams, DEFAULT_FETCH_SIZE);
  }

  /**
   * Gets multiple records (fetch all).<br>
   * <ul>
   * <li>Basically, do not use this method.</li>
   * <li>Use only when issues occur without fetching all.</li>
   * <li>Retrieving a large number of records with this method may cause memory errors.</li>
   * </ul>
   * 
   * @see #select(Connection, AbstractSqlWithParameters)
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return the SQL result set
   */
  public static SqlResultSet selectFetchAll(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    return selectByFetchSize(conn, sqlWithParams, 0);
  }

  /**
   * Gets multiple records in bulk.<br>
   * <ul>
   * <li>Returns a multiple row list.</li>
   * <li>Returns a list with size zero if zero records are returned.</li>
   * <li>Column physical names of each row map are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>This method consumes memory, so use <code>#select(Connection, AbstractSqlWithParameters)</code>
   * for loop processing.</li>
   * <li>Retrieving a large number of records with this method may cause memory errors.</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @param limitCount Maximum number of records to retrieve
   * @return the multiple row list
   */
  public static IoRows selectBulk(final Connection conn, final AbstractSqlWithParameters sqlWithParams,
      final int limitCount) {
    return selectBulkByLimitCount(conn, sqlWithParams, limitCount);
  }

  /**
   * Gets multiple records in bulk (all records).<br>
   * <ul>
   * <li>Returns a multiple row list.</li>
   * <li>Returns a list with size zero if zero records are returned.</li>
   * <li>Column physical names of each row map are lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>This method consumes memory, so use <code>#select(Connection, AbstractSqlWithParameters)</code>
   * for loop processing.</li>
   * <li>Retrieving a large number of records with this method may cause memory errors.</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return the multiple row list
   */
  public static IoRows selectBulkAll(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    return selectBulkByLimitCount(conn, sqlWithParams, 0);
  }

  /**
   * Gets multiple records in bulk.
   * 
   * @param conn Database connection
   * @param sqlWithParams SQL and parameters
   * @param limitCount Maximum number of records to retrieve (retrieves all if zero or less)
   * @return the multiple row list
   */
  private static IoRows selectBulkByLimitCount(final Connection conn, final AbstractSqlWithParameters sqlWithParams,
      final int limitCount) {
    // Fetch size
    final int fetchSize;
    if (limitCount <= 0 || DEFAULT_FETCH_SIZE < limitCount) {
      // To reduce memory usage as much as possible
      // Use default fetch size for all records or when exceeding default fetch size
      fetchSize = DEFAULT_FETCH_SIZE;
    } else {
      // Add +1 for limit exceeded check
      fetchSize = limitCount + 1;
    }

    final IoRows rows = new IoRows();
    try (final SqlResultSet rSet = SqlUtil.selectByFetchSize(conn, sqlWithParams, fetchSize);) {
      final Iterator<IoItems> ite = rSet.iterator();
      while (ite.hasNext()) {
        final IoItems row = ite.next();
        rows.add(row);
        if (limitCount > 0 && rows.size() >= limitCount) {
          // End because limit count reached
          if (ite.hasNext()) {
            // Data still exists = limit exceeded
            rows.setLimitOver(true);
          }
          break;
        }
      }
      if (rSet.getReadedCount() > 0) {
        // Set begin and end row numbers if not zero records
        rows.setBeginRowNo(1);
        rows.setEndRowNo(rSet.getReadedCount());
      }
    }
    return rows;
  }

  /**
   * Gets multiple records with specified fetch size.
   *
   * @param conn Database connection
   * @param sqlWithParams SQL and parameters
   * @param fetchSize Fetch size
   * @return the SQL result set
   */
  private static SqlResultSet selectByFetchSize(final Connection conn, final AbstractSqlWithParameters sqlWithParams,
      final int fetchSize) {
        
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String sql = sqlWithParams.getSql();
    final List<Object> params = sqlWithParams.getParameters();
    if (logger.isDevelopMode()) {
      // Output SQL log
      logger.develop("SQL#SELECT execution. " + LogUtil.joinKeyVal("sql", sqlWithParams, "fetchSize", fetchSize));
    }

    try {
      // Create statement
      final PreparedStatement stmt = conn.prepareStatement(sql);
      // Set parameters to statement
      setStmtParameters(stmt, params, dbmsName);
      // Set fetch-related properties to statement
      setStmtFetchProperty(stmt, fetchSize);

      // Execute SQL
      final ResultSet rset = stmt.executeQuery();
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMap(rset);

      // Connection serial code
      final String serialCode = DbUtil.getSerialCode(conn);

      // SQL result set
      final SqlResultSet retSet = new SqlResultSet(stmt, rset, itemClsMap, serialCode);
      return retSet;

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data retrieval. " + LogUtil.joinKeyVal("sql",
          sqlWithParams, "fetchSize", fetchSize), e);
    }
  }

  /**
   * Inserts one record by specifying table.<br>
   * <ul>
   * <li>Inserts one record by specifying the table name.</li>
   * <li>Parameters not existing in the table are ignored.</li>
   * <li>If a column is added to the table after implementation and that column name already exists in the parameters,<br>
   * values will be inserted into the added column without implementation changes, so caution is needed.</li>
   * <li>Throws an exception error if the affected count is zero for reasons other than unique constraint violation.</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values
   * @return <code>false</code> if unique constraint violation, <code>true</code> if one record was inserted successfully
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skip parameters not existing in the table
          continue;
        }

        // Column class type
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Get value by column class type
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Append SQL
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }

      // Build SQL
      sbInto.deleteLastChar(1);
      sbVals.deleteLastChar(1);
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try{
      // Execute SQL
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
   * Inserts one record by specifying table (timestamp auto-set).<br>
   * <ul>
   * <li>Inserts one record by specifying the table name.</li>
   * <li>Sets the timestamp for optimistic locking.</li>
   * <li>Parameters not existing in the table are ignored.</li>
   * <li>If a column is added to the table after implementation and that column name already exists in the parameters,<br>
   * values will be inserted into the added column without implementation changes, so caution is needed.</li>
   * <li>Throws an exception error if the affected count is zero for reasons other than unique constraint violation.</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values
   * @param tsItem    Timestamp column name (for optimistic locking)
   * @return <code>false</code> if unique constraint violation, <code>true</code> if one record was inserted successfully
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
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skip parameters not existing in the table
          continue;
        }
        if (tsItem.equals(itemName)) {
          // Skip timestamp
          continue;
        }

        // Column class type
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Get value by column class type
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Append SQL
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
      }
      // Append timestamp SQL
      sbInto.addQuery(tsItem);
      sbVals.addQuery(curTs);

      // Build SQL
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try {
      // Execute SQL
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
   * Updates one record by specifying table.<br>
   * <ul>
   * <li>Updates one record by specifying the table name.</li>
   * <li>Throws an exception error if multiple records are updated.</li>
   * <li>Parameters not existing in the table are ignored.</li>
   * <li>If a column is added to the table after implementation and that column name already exists in the parameters,<br>
   * values will be updated in the added column without implementation changes, so caution is needed.</li>
   * <li>WHERE clause is created from key columns.</li>
   * <li>Key columns must be included in the parameter values.</li>
   * <li>Key column names must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values (including key column names)
   * @param keyItems  Key column names
   *
   * @return <code>true</code> if one record was updated, <code>false</code> if zero records
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
   * Updates one record by specifying table (timestamp exclusive control update).<br>
   * <ul>
   * <li>Updates one record by specifying the table name.</li>
   * <li>Throws an exception error if multiple records are updated.</li>
   * <li>Performs optimistic locking using timestamp.</li>
   * <li>Parameters not existing in the table are ignored.</li>
   * <li>If a column is added to the table after implementation and that column name already exists in the parameters,<br>
   * values will be updated in the added column without implementation changes, so caution is needed.</li>
   * <li>WHERE clause is created from key columns and timestamp column (for exclusive control).</li>
   * <li>Key columns and timestamp column must be included in the parameter values.</li>
   * <li>Key column names and timestamp column name must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Timestamp column is updated with the current datetime.</li>
   * <li>Use <code>#updateOne(Connection, String, AbstractIoTypeMap, String[])</code> if timestamp exclusive control is not needed.</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values (including key column names)
   * @param keyItems  Key column names
   * @param tsItem    Timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was updated, <code>false</code> if zero records
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
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
      whereItems[keyItems.length] = tsItem;

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Append SET clause
      addSetQuery(sb, params, whereItems, itemClsMap);
      // Update timestamp column with current datetime
      sb.addQuery(",").addQuery(tsItem).addQuery("=").addQuery(curTs);
      // Append WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params), e);
    }

    try {
      // Execute SQL
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
   * Updates records by specifying table.<br>
   * <ul>
   * <li>Updates multiple records by specifying the table name.</li>
   * <li>Parameters not existing in the table are ignored.</li>
   * <li>If a column is added to the table after implementation and that column name already exists in the parameters,<br>
   * values will be updated in the added column without implementation changes, so caution is needed.</li>
   * <li>WHERE clause is created from extraction condition columns.</li>
   * <li>Extraction condition columns must be included in the parameter values.</li>
   * <li>Extraction condition column names must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param tableName  Table name
   * @param params     Parameter values (including extraction condition column names)
   * @param whereItems Extraction condition column names (optional) <code>null</code> if omitted
   *
   * @return the number of updated records
   */
  public static int update(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Append SET clause
      addSetQuery(sb, params, whereItems, itemClsMap);
      // Append WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Execute SQL
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Deletes one record by specifying table.<br>
   * <ul>
   * <li>Deletes one record by specifying the table name.</li>
   * <li>Throws an exception error if multiple records are deleted.</li>
   * <li>WHERE clause is created from key columns.</li>
   * <li>Key columns must be included in the parameter values.</li>
   * <li>Key column names must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values (including key column names)
   * @param keyItems  Key column names
   *
   * @return <code>true</code> if one record was deleted, <code>false</code> if zero records
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
   * Deletes one record by specifying table (timestamp exclusive control delete).<br>
   * <ul>
   * <li>Deletes one record by specifying the table name.</li>
   * <li>Throws an exception error if multiple records are deleted.</li>
   * <li>Performs optimistic locking using timestamp.</li>
   * <li>WHERE clause is created from key columns and timestamp column (for exclusive control).</li>
   * <li>Key columns and timestamp column must be included in the parameter values.</li>
   * <li>Key column names and timestamp column name must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use <code>#deleteOne(Connection, String, AbstractIoTypeMap, String[])</code>
   * if timestamp exclusive control is not needed.</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @param params    Parameter values (including key column names)
   * @param keyItems  Key column names
   * @param tsItem    Timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was deleted, <code>false</code> if zero records
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
   * Deletes records by specifying table.<br>
   * <ul>
   * <li>Deletes multiple records by specifying the table name.</li>
   * <li>WHERE clause is created from extraction condition columns.</li>
   * <li>Extraction condition columns must be included in the parameter values.</li>
   * <li>Extraction condition column names must be specified in lowercase letters. (Key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn Database connection
   * @param tableName Table name
   * @param params Parameter values (including extraction condition column names)
   * @param whereItems Extraction condition column names
   *
   * @return the number of deleted records
   */
  public static int delete(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String[] whereItems) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // Database column name to class type map
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("DELETE FROM ").addQuery(tableName);
      // Append WHERE clause
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);
      
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Execute SQL
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Appends SET clause.
   *
   * @param sb         SQL builder
   * @param params     Parameter values (including extraction condition column names)
   * @param whereItems Extraction condition column names (optional) <code>null</code> if omitted
   * @param itemClsMap Database column name to class type map
   */
  private static void addSetQuery(final SqlBuilder sb, final AbstractIoTypeMap params,
      final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    // Create SET clause
    sb.addQuery(" SET ");

    // Extraction condition column list for existence check
    final List<String> whereItemList;
    if (ValUtil.isEmpty(whereItems)) {
      whereItemList = Arrays.asList(new String[] {});
    } else {
      whereItemList = Arrays.asList(whereItems);
    }

    for (final String itemName : params.keySet()) {
      if (!itemClsMap.containsKey(itemName)) {
        // Skip parameters not existing in the table
        continue;
      }
      if (whereItemList.contains(itemName)) {
        // Skip if exists in extraction condition columns
        continue;
      }

      // Column class type
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // Get value by column class type
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // Append SQL
      sb.addQuery(itemName).addQuery("=?", param).addQuery(",");
    }
    sb.deleteLastChar(1);
  }

  /**
   * Appends WHERE clause.
   *
   * @param sb         SQL builder
   * @param tableName  Table name
   * @param params     Parameter values (including extraction condition column names)
   * @param whereItems Extraction condition column names (optional) <code>null</code> if omitted
   * @param itemClsMap Database column name to class type map
   */
  private static void addWhereQuery(final SqlBuilder sb, final String tableName,
      final AbstractIoTypeMap params, final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    if (ValUtil.isEmpty(whereItems)) {
      return;
    }

    // Create WHERE clause
    sb.addQuery(" WHERE ");
    for (final String itemName : whereItems) {
      if (!itemClsMap.containsKey(itemName)) {
        // Skip parameters not existing in the table
        continue;
      }
      if (!params.containsKey(itemName)) {
        // Error if extraction condition column does not exist in parameters
        throw new RuntimeException("Extraction condition field does not exist in parameters. " + LogUtil.joinKeyVal("tableName",
            tableName, "whereItemName", itemName, "params", params));
      }

      // Column class type
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // Get value by column class type
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // Append SQL
      sb.addQuery(itemName).addQuery("=?", param).addQuery(" AND ");
    }
    sb.deleteLastChar(4);
  }

  /**
   * SQL insert/update/delete one record.<br>
   * <ul>
   * <li>Throws an exception error if the affected count is multiple records.</li>
   * </ul>
   *
   * @param conn       Database connection
   * @param sqlWithParams SQL and parameters
   * @return <code>true</code> if affected count is one record, <code>false</code> if zero records
   */
  public static boolean executeOne(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    final int ret = execute(conn, sqlWithParams);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were affected. " + LogUtil.joinKeyVal("sql", sqlWithParams));
    }
    return (ret == 1);
  }

  /**
   * SQL insert/update/delete.
   *
   * @param conn Database connection
   * @param sqlWithParams SQL and parameters
   * @return the affected count
   */
  public static int execute(final Connection conn, final AbstractSqlWithParameters sqlWithParams) {
    try {
      return executeSql(conn, sqlWithParams);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during SQL execution. " + LogUtil.joinKeyVal("sql", sqlWithParams), e);
    }
  }

  /**
   * Executes SQL builder.
   *
   * @param conn Database connection
   * @param sqlWithParams SQL and parameters
   * @return the affected count
   * @throws SQLException SQL exception error
   */
  private static int executeSql(final Connection conn, final AbstractSqlWithParameters sqlWithParams)
      throws SQLException {
        
    if (logger.isDevelopMode()) {
      // Output SQL log
      logger.develop("SQL#EXECUTE execution. " + LogUtil.joinKeyVal("sql", sqlWithParams));
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // Create statement
    try (final PreparedStatement stmt = conn.prepareStatement(sqlWithParams.getSql());) {
      // Set parameters to statement
      setStmtParameters(stmt, sqlWithParams.getParameters(), dbmsName);
      // Execute SQL
      final int ret = stmt.executeUpdate();
      return ret;
    }
  }

  /**
   * Sets parameters to statement.
   *
   * @param stmt     Statement
   * @param params   Parameters
   * @param dbmsName DBMS name
   * @throws SQLException SQL exception error
   */
  private static void setStmtParameters(final PreparedStatement stmt, final List<Object> params,
      final DbmsName dbmsName) throws SQLException {
    int bindNo = 0;
    for (final Object param : params) {
      ++bindNo;
      if (dbmsName == DbmsName.SQLITE) {
        if (ValUtil.isNull(param)) {
          stmt.setObject(bindNo, param);
        } else if (param instanceof java.sql.Timestamp) {
          // Convert java.sql.Timestamp to String and set
          final java.sql.Timestamp ts = (java.sql.Timestamp) param;
          final LocalDateTime ldt = ts.toLocalDateTime();
          final String s = DTF_SQL_TIMESTAMP.format(ldt);
          stmt.setString(bindNo, s);
        } else if (param instanceof java.sql.Date) {
          // Convert java.sql.Date to String and set
          final java.sql.Date dt = (java.sql.Date) param;
          final LocalDate ld = dt.toLocalDate();
          final String s = DTF_SQL_DATE.format(ld);
          stmt.setString(bindNo, s);
        } else {
          stmt.setObject(bindNo, param);
        }
      } else {
        stmt.setObject(bindNo, param);
      }
    }
  }
  /**
   * Sets fetch-related properties to statement.
   *
   * @param stmt Statement
   * @param fetchSize Fetch size
   * @throws SQLException SQL exception error
   */
  private static void setStmtFetchProperty(final PreparedStatement stmt, final int fetchSize)
      throws SQLException {
    // Set fetch direction and fetch size
    stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    stmt.setFetchSize(fetchSize);
  }

  /**
   * Creates a map of DB item name-to-class type from a result set.<br>
   * <ul>
   * <li>Creates a map of item names to class types from a result set.</li>
   * <li>The map preserves item order.</li>
   * <li>Item physical names are converted to lowercase letters. (Matches key rules of
   *     <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param rset Result set
   * @return DB item name-to-class type map
   * @throws SQLException SQL exception error
   */
  private static Map<String, ItemClsType> createItemNameClsMap(final ResultSet rset)
      throws SQLException {

    // DB item name-to-class type map
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS name
    final DbmsName dbmsName = DbUtil.getDbmsName(rset);
    // Result set meta information
    final ResultSetMetaData rmeta = rset.getMetaData();
    // Column count
    final int itemCount = rmeta.getColumnCount();
    // Loop through result set columns
    for (int c = 1; c <= itemCount; c++) {
      // Column name
      final String itemName;
      if (DbmsName.DB2 == dbmsName) {
        // DB2's #getColumnName returns the original item name, not alias
        itemName = rmeta.getColumnLabel(c).toLowerCase();
      } else {
        itemName = rmeta.getColumnName(c).toLowerCase();
      }
      // Type number
      final int typeNo = rmeta.getColumnType(c);
      // Type name
      // Oracle's DATE type also holds time and is judged as TIMESTAMP, so judgment by type name is necessary.
      final String typeName = rmeta.getColumnTypeName(c).toUpperCase();
      // Item class type
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * Creates a map of DB item name-to-class type by specifying a table.<br>
   * <ul>
   * <li>Creates a map of item names to class types from DB meta information.</li>
   * <li>The map preserves item order.</li>
   * <li>Item physical names are converted to lowercase letters. (Matches key rules of
   *     <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      Database connection
   * @param tableName Table name
   * @throws SQLException SQL exception error
   */
  private static Map<String, ItemClsType> createItemNameClsMapByMeta(final Connection conn,
      final String tableName) throws SQLException {

    // DB item name-to-class type map
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS name
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // DB meta information
    final DatabaseMetaData cmeta = conn.getMetaData();
    // Column information result set
    final ResultSet rset = cmeta.getColumns(null, null, tableName, null);

    while (rset.next()) {
      // Column name
      final String itemName = rset.getString("COLUMN_NAME").toLowerCase();
      // Type number
      final int typeNo = rset.getInt("DATA_TYPE");
      // Type name
      // Oracle's DATE type also holds time and is judged as TIMESTAMP, so judgment by type name is necessary.
      final String typeName = rset.getString("TYPE_NAME");
      // Item class type
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * Converts DB item class type.
   *
   * @param typeNo   Type number
   * @param typeName Type name in uppercase
   * @param dbmsName DBMS name
   * @return Class type
   */
  private static ItemClsType convItemClsType(final int typeNo, final String typeName, final DbmsName dbmsName) {
    final ItemClsType itemCls;
    if (/* JDBC types mapped to BigDecimal */ Types.DECIMAL == typeNo || Types.NUMERIC == typeNo
        || /* JDBC types mapped to Integer */ Types.TINYINT == typeNo || Types.SMALLINT == typeNo
        || Types.INTEGER == typeNo || /* JDBC types mapped to Long */ Types.BIGINT == typeNo
        || /* JDBC types mapped to Float */ Types.FLOAT == typeNo || Types.REAL == typeNo
        || /* JDBC types mapped to Double */ Types.DOUBLE == typeNo) {
      // Unify numeric types to BigDecimal
      itemCls = ItemClsType.BigDecCls;
    } else if (Types.DATE == typeNo) {
      if ("DATETIME".equals(typeName) && DbmsName.MSSQL == dbmsName) {
        itemCls = ItemClsType.TsCls;
      } else if (DbmsName.SQLITE == dbmsName) {
        // When Types.DATE is returned from result set in SQLite, it is actually a string, so conversion is needed (see #createIoItemsFromResultSet)
        itemCls = ItemClsType.StringToDateCls;
      } else {
        itemCls = ItemClsType.DateCls;
      }
    } else if (Types.TIMESTAMP == typeNo || Types.TIMESTAMP_WITH_TIMEZONE == typeNo) {
      if ("DATE".equals(typeName) && DbmsName.ORACLE == dbmsName) {
        itemCls = ItemClsType.DateCls;
      } else if (DbmsName.SQLITE == dbmsName) {
        // When Types.TIMESTAMP is returned from result set in SQLite, it is actually a string, so conversion is needed (see #createIoItemsFromResultSet)
        itemCls = ItemClsType.StringToTsCls;
      } else {
        itemCls = ItemClsType.TsCls;
      }
    } else {
      if (DbmsName.SQLITE == dbmsName) {
        if ("DATE".equals(typeName)) {
          // SQLite table meta information has Types.VARCHAR with type name "DATE"
          itemCls = ItemClsType.StringToDateCls;
        } else if ("TIMESTAMP".equals(typeName)) {
          // SQLite table meta information has Types.VARCHAR with type name "TIMESTAMP"
          itemCls = ItemClsType.StringToTsCls;
        } else {
          itemCls = ItemClsType.StringCls;
        }
      } else {
        // Unify string types to String
        itemCls = ItemClsType.StringCls;
      }
    }
    return itemCls;
  }

  /**
   * Creates a row map from a result set.<br>
   * <ul>
   * <li>Returns the values of the current row of the result set as a map.</li>
   * </ul>
   *
   * @param rset Result set
   * @param itemClsMap DB item name-to-class type map
   * @return Row map
   */
  static IoItems createIoItemsFromResultSet(final ResultSet rset,
      final Map<String, ItemClsType> itemClsMap) {

    // Row map
    final IoItems rowMap = new IoItems();

    // Loop through DB items
    for (final Map.Entry<String, ItemClsType> ent : itemClsMap.entrySet()) {
      // Item name
      final String itemName = ent.getKey();
      try {
        // Item class type
        final ItemClsType itemCls = ent.getValue();
        // Set value by item class type
        if (ItemClsType.StringCls == itemCls) {
          final String value = rset.getString(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.BigDecCls == itemCls) {
          final BigDecimal value = rset.getBigDecimal(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.DateCls == itemCls) {
          final java.sql.Date value = rset.getDate(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.TsCls == itemCls) {
          final java.sql.Timestamp value = rset.getTimestamp(itemName);
          rowMap.put(itemName, value);
        } else if (ItemClsType.StringToDateCls == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value) || value.length() != 10) {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDate ld = LocalDate.parse(value, DTF_SQL_DATE);
          // Originally should set as Date, but since IoItems converts to LocalDate internally, set as is
          rowMap.put(itemName, ld);
        } else if (ItemClsType.StringToTsCls == itemCls) {
          final String value = rset.getString(itemName);
          if (ValUtil.isBlank(value)) {
            rowMap.putNull(itemName);
            continue;
          }
          final String ajustVal;
          final int len = value.length();
          if (len == 19) {
            // Add .000000 when fractional seconds are missing ("uuuu-MM-dd HH:mm:ss" = 19 characters)
            ajustVal = value + ".000000";
          } else if (19 < len && len < 26) {
            // Add 000000 when fractional seconds digits are insufficient ("uuuu-MM-dd HH:mm:ss.SSSSSS" = 26 characters)
            ajustVal = ValUtil.substring(value + "000000", 0, 26);
          } else if (len == 26) {
            ajustVal = value;
          } else if (len > 26) {
            // Truncate fractional seconds to 6 digits ("uuuu-MM-dd HH:mm:ss.SSSSSS" = 26 characters)
            ajustVal = ValUtil.substring(value, 0, 26);
          } else {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDateTime ldt = LocalDateTime.parse(ajustVal, DTF_SQL_TIMESTAMP);
          // Originally should set as Timestamp, but since IoItems converts to LocalDateTime internally, set as is
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
   * Gets parameter value.<br>
   * <ul>
   * <li>Uses different parameter getters based on DB item class type to retrieve and return the value.</li>
   * </ul>
   *
   * @param params Parameters
   * @param itemName Item name
   * @param itemCls DB item class type
   * @return Parameter value
   */
  private static Object getValueFromIoItemsByItemCls(final AbstractIoTypeMap params, final String itemName,
      final ItemClsType itemCls) {
    final Object param;
    if (ItemClsType.StringCls == itemCls) {
      param = params.getStringNullable(itemName);
    } else if (ItemClsType.BigDecCls == itemCls) {
      param = params.getBigDecimalNullable(itemName);
    } else if (ItemClsType.DateCls == itemCls) {
      param = params.getSqlDateNullable(itemName);
    } else if (ItemClsType.TsCls == itemCls) {
      param = params.getSqlTimestampNullable(itemName);
    } else if (ItemClsType.StringToDateCls == itemCls) {
      final LocalDate ld = params.getDateNullable(itemName);
      if (ValUtil.isNull(ld)) {
        param = null;
      } else {
        param = ld.format(DTF_SQL_DATE);
      }
    } else if (ItemClsType.StringToTsCls == itemCls) {
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
   * Determines unique constraint violation error.<br>
   * <ul>
   * <li>Determines whether it is a unique constraint violation error for each DBMS.</li>
   * <li>Returns <code>true</code> if it is a unique constraint violation.</li>
   * </ul>
   *
   * @param e SQL exception
   * @param dbmsName DBMS name
   * @return <code>true</code> if it is a unique constraint violation error
   */
  private static boolean isUniqueKeyErr(final SQLException e, final DbmsName dbmsName) {
    // Oracle unique constraint violation error determination
    if (dbmsName == DbmsName.ORACLE && e.getErrorCode() == 1) {
      return true;
    }
    // PostgreSQL unique constraint violation error determination
    if (dbmsName == DbmsName.POSTGRESQL && "23505".equals(e.getSQLState())) {
      return true;
    } 
    // MS-SqlServer unique constraint violation error determination
    if (dbmsName == DbmsName.MSSQL && "23000".equals(e.getSQLState())
        && e.getMessage().contains("Violation of UNIQUE KEY constraint")) {
      return true;
    }
    // SQLite unique constraint violation error determination
    if (dbmsName == DbmsName.SQLITE && e.getErrorCode() == 19
        && e.getMessage().contains("UNIQUE constraint failed")) {
      return true;
    }
    // DB2 unique constraint violation error determination
    if (dbmsName == DbmsName.DB2 && "23505".equals(e.getSQLState())) {
      return true;
    }

    return false;
  }

  /** Timestamp retrieval SQL (6 fractional second digits) map. */
  private static final Map<DbmsName, String> SQL_CUR_TS = new HashMap<>();
  /** Timestamp retrieval SQL (6 fractional second digits) other. */
  private static final String SQL_CUR_TS_OTHER;
  static {
    // SQLite can only retrieve 3 fractional second digits, so append 000 to make 6 digits
    SQL_CUR_TS.put(DbmsName.SQLITE, "strftime('%Y-%m-%d %H:%M:%f000', 'now', 'localtime')");
    SQL_CUR_TS.put(DbmsName.MSSQL, "SYSDATETIME()");
    SQL_CUR_TS_OTHER = "CURRENT_TIMESTAMP(6)";
  }

  /**
   * Gets current timestamp value retrieval SQL by DBMS.
   *
   * @param dbmsName DBMS name
   * @return Current timestamp value retrieval SQL
   */
  private static String getCurrentTimestampSql(final DbmsName dbmsName) {
    return SQL_CUR_TS.getOrDefault(dbmsName, SQL_CUR_TS_OTHER);
  }

  /** Date retrieval SELECT statement map. */
  private static final Map<DbmsName, String> SQL_SELECT_TODAY = new HashMap<>();
  static {
    SQL_SELECT_TODAY.put(DbmsName.POSTGRESQL, "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today");
    SQL_SELECT_TODAY.put(DbmsName.ORACLE,    "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM DUAL");
    SQL_SELECT_TODAY.put(DbmsName.MSSQL,     "SELECT CONVERT(VARCHAR, FORMAT(GETDATE(), 'yyyyMMdd')) today");
    SQL_SELECT_TODAY.put(DbmsName.SQLITE,    "SELECT strftime('%Y%m%d', 'now', 'localtime') today");
    SQL_SELECT_TODAY.put(DbmsName.DB2, "SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM SYSIBM.DUAL");
  }
  
  /**
   * Gets current date by DBMS.
   *
   * @param conn Database connection
   * @return Current date (YYYYMMDD format)
   */
  public static String getToday(final Connection conn) {
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final String sql = SQL_SELECT_TODAY.get(dbmsName);
    if (ValUtil.isBlank(sql)) {
      throw new RuntimeException("Current date retrieval SQL is undefined for this DBMS. " + LogUtil.joinKeyVal("dbmsName", dbmsName));
    }
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery(sql);
    final IoItems ret = selectOne(conn, sb);
    return ret.getString("today");
  }

}
