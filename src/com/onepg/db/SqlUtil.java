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
import java.util.TreeMap;

/**
 * SQL execution utility class.
 */
public final class SqlUtil {

  /** Log writer. */
  private static final LogWriter logger = LogUtil.newLogWriter(SqlUtil.class);

  /** Default fetch size. */
  private static final int DEFAULT_FETCH_SIZE = 500;

  /** Date/time formatter: date in SQL standard format. */
  private static final DateTimeFormatter DTF_SQL_DATE =
      DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
  /** Date/time formatter: timestamp in SQL standard format. */
  private static final DateTimeFormatter DTF_SQL_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * SQL to avoid Oracle protocol violation errors.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com (reference)</a>
   */
  public static final String ORACLE_PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";
      
  /**
   * DB column class type.<br>
   * <ul>
   * <li>Indicates the Java variable class corresponding to the DB column type.</li>
   * <li>Unifies all numeric types to BigDecimal.</li>
   * <li>STRING_TO_DATE_CLS and STRING_TO_TS_CLS are for SQLite and perform conversion from strings to dates/timestamps.</li>
   * </ul>
   */
  enum ItemClsType {
    STRING_CLS, BIGDECIMAL_CLS, DATE_CLS, TIMESTAMP_CLS, STRING_TO_DATE_CLS, STRING_TO_TS_CLS
  }

  /**
   * Constructor.
   */
  private SqlUtil() {
    // No processing.
  }

  /**
   * Retrieves a single record (throws an error if no record exists).<br>
   * <ul>
   * <li>Use this method when exactly one matching record is expected to exist.</li>
   * <li>Throws an exception if the result is zero records.</li>
   * <li>Throws an exception if multiple records are returned.</li>
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
   * Retrieves a single record.<br>
   * <ul>
   * <li>Use this method when exactly one matching record is expected to exist.</li>
   * <li>Returns <code>null</code> if the result is zero records.</li>
   * <li>Throws an exception if multiple records are returned.</li>
   * <li>Physical field names are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
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
   * Retrieves a single record (multiple records allowed).<br>
   * <ul>
   * <li>Returns <code>null</code> if the result is zero records.</li>
   * <li>Returns the first record without error even if multiple records are retrieved.</li>
   * <li>Physical field names are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
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
   * <li>Pass <code>true</code> to the multiDataIgnore argument to suppress errors when multiple records are retrieved.</li>
   * <li>Returns the first record when multiple records are retrieved.</li>
   * <li>Throws an exception if multiDataIgnore is <code>false</code> and multiple records are retrieved.</li>
   * <li>Physical field names are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param multiDataIgnore pass <code>true</code> to suppress errors when multiple records are retrieved
   * @return the row data map (may be <code>null</code>)
   */
  private static IoItems selectFirstRec(final Connection conn, final SqlBean sb, final boolean multiDataIgnore) {

    // Retrieve in bulk.
    final IoRows rows = selectBulkByLimitCount(conn, sb, 1);
    if (rows.size() <= 0) {
      // No data.
      return null;
    }

    if (rows.isLimitOver()) {
      // More than one row retrieved.
      if (!multiDataIgnore) {
        throw new RuntimeException("Multiple records were retrieved. " + sb.toString());
      }
    }
    return rows.get(0);
  }

  /**
   * Retrieves multiple records.<br>
   * <ul>
   * <li>Returns a <code>SqlResultSet</code>.</li>
   * <li>Physical field names of row maps obtained from the <code>SqlResultSet</code> iterator are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use in a try statement (try-with-resources).</li>
   * <li>The default fetch size in this class is 500. Use <code>SqlUtil#selectFetchAll(Connection, SqlBean)</code> to fetch all records.</li>
   * <li>Notes on fetch size per DBMS:
   * <ul>
   * <li>Oracle defaults to 10, which is small, so specify the fetch size.</li>
   * <li>PostgreSQL defaults to fetching all records, which may cause OutOfMemory, so specify the fetch size.</li>
   * <li>With PostgreSQL, specifying a fetch size (without fetching all), updating retrieved data, and performing intermediate commits causes a cursor invalidation error (SQLSTATE 34000). In that case, stop intermediate commits or fetch all records.<br>
   * Alternatively, though more complex, splitting data retrieval using a SQL LIMIT clause also resolves the issue.</li>
   * <li>With MS SQL Server, specifying a fetch size may not work as expected, so use a SQL LIMIT clause to split data retrieval if OutOfMemory is a concern.</li>
   * </ul>
   * </li>
   * </ul>
   * <pre>[Example]
   * <code>try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), sqlBuilder);) {
   *   for (final IoItems row : rSet) {
   *     : (omitted)
   *   }
   *   if (rSet.getReadedCount() <= 0) {
   *     // Zero records.
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
   * Retrieves multiple records (fetches all records).<br>
   * <ul>
   * <li>Do not use this method in general.</li>
   * <li>Use this method only when issues occur without fetching all records.</li>
   * <li>Retrieving a large number of records with this method may cause an OutOfMemory error.</li>
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
   * <li>Returns a list of multiple rows.</li>
   * <li>Returns an empty list if the result is zero records.</li>
   * <li>Physical field names of each row map are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use <code>#select(Connection, SqlBean)</code> for loop processing since this method consumes memory.</li>
   * <li>Retrieving a large number of records with this method may cause an OutOfMemory error.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param limitCount the maximum number of records to retrieve
   * @return the list of multiple rows
   */
  public static IoRows selectBulk(final Connection conn, final SqlBean sb, final int limitCount) {
    return selectBulkByLimitCount(conn, sb, limitCount);
  }

  /**
   * Retrieves all records in bulk.<br>
   * <ul>
   * <li>Returns a list of multiple rows.</li>
   * <li>Returns an empty list if the result is zero records.</li>
   * <li>Physical field names of each row map are in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use <code>#select(Connection, SqlBean)</code> for loop processing since this method consumes memory.</li>
   * <li>Retrieving a large number of records with this method may cause an OutOfMemory error.</li>
   * </ul>
   *
   * @param conn the database connection
   * @param sb the SQL Bean
   * @return the list of multiple rows
   */
  public static IoRows selectBulkAll(final Connection conn, final SqlBean sb) {
    return selectBulkByLimitCount(conn, sb, 0);
  }

  /**
   * Retrieves multiple records in bulk.
   * 
   * @param conn the database connection
   * @param sb the SQL Bean
   * @param limitCount the maximum number of records to retrieve (retrieves all records if zero or less)
   * @return the list of multiple rows
   */
  private static IoRows selectBulkByLimitCount(final Connection conn, final SqlBean sb, final int limitCount) {
    // Fetch size.
    final int fetchSize;
    if (limitCount <= 0 || DEFAULT_FETCH_SIZE < limitCount) {
      // To minimize memory usage,
      // use the default fetch size when retrieving all records or when the limit exceeds the default fetch size.
      fetchSize = DEFAULT_FETCH_SIZE;
    } else {
      // Add 1 for limit exceeded detection.
      fetchSize = limitCount + 1;
    }

    final IoRows rows = new IoRows();
    try (final SqlResultSet rSet = selectByFetchSize(conn, sb, fetchSize);) {
      final Iterator<IoItems> ite = rSet.iterator();
      while (ite.hasNext()) {
        final IoItems row = ite.next();
        rows.add(row);
        if (limitCount > 0 && rows.size() >= limitCount) {
          // Reached the record limit; stopping.
          if (ite.hasNext()) {
            // More data exists = limit exceeded.
            rows.setLimitOver(true);
          }
          break;
        }
      }
      if (rSet.getReadedCount() > 0) {
        // Set the start and end row numbers when there is at least one record.
        rows.setBeginRowNo(1);
        rows.setEndRowNo(rSet.getReadedCount());
      }
    }
    return rows;
  }

  /**
   * Retrieves multiple records with a specified fetch size.
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
      // Log SQL output.
      logger.develop("SQL#SELECT execution. " + LogUtil.joinKeyVal("sql", sb, "fetchSize", fetchSize));
    }
    PreparedStatement stmt = null;
    ResultSet rset = null;
    try {
      // Generate statement.
      stmt = conn.prepareStatement(sql);
      // Set parameters on the statement.
      setStmtParameters(stmt, bindValues, dbmsName);
      // Set fetch-related properties on the statement.
      setStmtFetchProperty(stmt, fetchSize);

      // Execute SQL.
      rset = stmt.executeQuery();
      // DB column name / class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMap(rset);

      // Connection serial code.
      final String serialCode = DbUtil.getSerialCode(conn);

      // SQL result set.
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
   * Inserts a single record into the specified table.<br>
   * <ul>
   * <li>Inserts one record into the specified table.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be inserted into the new column without modifying the implementation.</li>
   * <li>Throws an exception if the number of affected rows is zero for reasons other than a unique constraint violation.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values
   * @return <code>false</code> if a unique constraint violation occurs; <code>true</code> if the record was successfully inserted
   */
  public static boolean insertOne(final Connection conn, final String tableName, final AbstractIoTypeMap params) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    final SqlBuilder sbInto = new SqlBuilder();
    final SqlBuilder sbVals = new SqlBuilder();
    try {
      // DB column name / class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      int count = 0;
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skip parameters not present in the table.
          continue;
        }

        // Column class type.
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Get value by column class type.
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Add to SQL.
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
        count++;
      }
      if (count <= 0) {
        // No registration target items exist.
        throw new RuntimeException("No registration target items exist. " + LogUtil.joinKeyVal("tableName", tableName,
            "params", params));
      }

      // Assemble SQL.
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
      // Execute SQL.
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // Unique constraint violation error.
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * Inserts a single record into the specified table (with automatic timestamp setting).<br>
   * <ul>
   * <li>Inserts one record into the specified table.</li>
   * <li>Sets the timestamp for optimistic locking.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be inserted into the new column without modifying the implementation.</li>
   * <li>Throws an exception if the number of affected rows is zero for reasons other than a unique constraint violation.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values
   * @param tsItem    the timestamp column name (for optimistic locking)
   * @return <code>false</code> if a unique constraint violation occurs; <code>true</code> if the record was successfully inserted
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
      // DB column name / class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sbInto.addQuery("INSERT INTO ").addQuery(tableName);

      sbInto.addQuery(" ( ");
      sbVals.addQuery(" ( ");
      int count = 0;
      for (final String itemName : params.keySet()) {
        if (!itemClsMap.containsKey(itemName)) {
          // Skip parameters not present in the table.
          continue;
        }
        if (tsItem.equals(itemName)) {
          // Skip the timestamp column.
          continue;
        }

        // Column class type.
        final ItemClsType itemCls = itemClsMap.get(itemName);
        // Get value by column class type.
        final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);

        // Add to SQL.
        sbInto.addQuery(itemName).addQuery(",");
        sbVals.addQuery("?,", param);
        count++;
      }
      if (count <= 0) {
        // No registration target items exist.
        throw new RuntimeException("No registration target items exist. " + LogUtil.joinKeyVal("tableName", tableName,
            "params", params));
      }

      // Add timestamp SQL.
      sbInto.addQuery(tsItem);
      sbVals.addQuery(curTs);

      // Assemble SQL.
      sbInto.addQuery(" ) VALUES ");
      sbVals.addQuery(" ) ");
      sbInto.addSqlBuilder(sbVals);

    } catch (final SQLException e) {
      throw new RuntimeException("Exception error occurred during data insert SQL generation. "
          + LogUtil.joinKeyVal("tableName", tableName, "params", params), e);
    }

    try {
      // Execute SQL.
      final int ret = executeSql(conn, sbInto);
      if (ret != 1) {
        throw new RuntimeException("Failed to insert data. " + LogUtil.joinKeyVal("sql",
            sbInto));
      }
      return true;

    } catch (final SQLException e) {
      if (isUniqueKeyErr(e, dbmsName)) {
        // Unique constraint violation error.
        return false;
      }
      throw new RuntimeException("Exception error occurred during data insert. " + LogUtil.joinKeyVal("sql",
          sbInto), e);
    }
  }

  /**
   * Updates a single record in the specified table.<br>
   * <ul>
   * <li>Updates one record in the specified table.</li>
   * <li>Throws an exception if multiple records are updated.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * <li>A WHERE clause is built using the key columns.</li>
   * <li>Key columns MUST be included in the parameter values.</li>
   * <li>Key column names MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key columns)
   * @param keyItems  the key column names
   *
   * @return <code>true</code> if one record was updated; <code>false</code> if zero records were updated
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
   * Updates a single record in the specified table (with timestamp optimistic locking).<br>
   * <ul>
   * <li>Updates one record in the specified table.</li>
   * <li>Throws an exception if multiple records are updated.</li>
   * <li>Performs optimistic locking using a timestamp.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * <li>A WHERE clause is built using the key columns and the timestamp column (for locking).</li>
   * <li>Key columns and the timestamp column MUST be included in the parameter values.</li>
   * <li>Key column names and the timestamp column name MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>The timestamp column is updated to the current date and time.</li>
   * <li>Use <code>#updateOne(Connection, String, AbstractIoTypeMap, String[])</code> if timestamp optimistic locking is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key columns and the timestamp column)
   * @param keyItems  the key column names
   * @param tsItem    the timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was updated; <code>false</code> if zero records were updated
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
      // DB項目名・クラスタイプマップ
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      final String[] whereItems = Arrays.copyOf(keyItems, keyItems.length + 1);
      whereItems[keyItems.length] = tsItem;

      sb.addQuery("UPDATE ").addQuery(tableName);
      // SET句追加
      addSetQuery(sb, params, whereItems, itemClsMap);
      // タイムスタンプ項目は現在日時で更新
      sb.addQuery(",").addQuery(tsItem).addQuery("=").addQuery(curTs);
      // WHERE句追加
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "keyItems", keyItems, "tsItem", tsItem, "params", params), e);
    }

    try {
      // SQL実行
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
   * Updates a single record in the specified table by primary key.<br>
   * <ul>
   * <li>Updates one record in the specified table.</li>
   * <li>Throws an exception if multiple records are updated.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * <li>A WHERE clause is built using the primary key columns of the table.</li>
   * <li>Throws an exception if the table has no primary key.</li>
   * <li>Primary key columns MUST be included in the parameter values.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including primary key columns)
   *
   * @return <code>true</code> if one record was updated; <code>false</code> if zero records were updated
   */
  public static boolean updateOneByPkey(final Connection conn, final String tableName, final AbstractIoTypeMap params) {
    final String[] pkItems = getPkeys(conn, tableName);
    return updateOne(conn, tableName, params, pkItems);
  }

  /**
   * Updates a single record in the specified table by primary key (with timestamp optimistic locking).<br>
   * <ul>
   * <li>Updates one record in the specified table.</li>
   * <li>Throws an exception if multiple records are updated.</li>
   * <li>Performs optimistic locking using a timestamp.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * <li>A WHERE clause is built using the primary key columns and the timestamp column (for locking).</li>
   * <li>Throws an exception if the table has no primary key.</li>
   * <li>Primary key columns and the timestamp column MUST be included in the parameter values.</li>
   * <li>The timestamp column name MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>The timestamp column is updated to the current date and time.</li>
   * <li>Use <code>#updateOneByPkey(Connection, String, AbstractIoTypeMap)</code> if timestamp optimistic locking is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including primary key columns and the timestamp column)
   * @param tsItem    the timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was updated; <code>false</code> if zero records were updated
   */
  public static boolean updateOneByPkey(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String tsItem) {
    final String[] pkItems = getPkeys(conn, tableName);
    return updateOne(conn, tableName, params, pkItems, tsItem);
  }

  /**
   * Updates multiple records in the specified table.<br>
   * <ul>
   * <li>Updates multiple records in the specified table.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * <li>A WHERE clause is built using the search condition columns.</li>
   * <li>Search condition columns MUST be included in the parameter values.</li>
   * <li>Search condition column names MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       the database connection
   * @param tableName  the table name
   * @param params     the parameter values (including search condition columns)
   * @param whereItems the search condition column names (optional; pass <code>null</code> if omitted)
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
      // Create column name/class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Add SET clause.
      addSetQuery(sb, params, whereItems, itemClsMap);
      // Add WHERE clause.
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Execute SQL.
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Updates all records in the specified table.<br>
   * <ul>
   * <li>Updates all records in the specified table.</li>
   * <li>Parameters not present in the table are ignored.</li>
   * <li>Note: if a column is added to the table after implementation and that column name already exists in the parameters, values will be updated in the new column without modifying the implementation.</li>
   * </ul>
   *
   * @param conn       the database connection
   * @param tableName  the table name
   * @param params     the parameter values
   *
   * @return the number of updated records
   */
  public static int updateAll(final Connection conn, final String tableName, final AbstractIoTypeMap params) {

    if (ValUtil.isEmpty(params)) {
      throw new RuntimeException("Parameters are required. ");
    }

    final SqlBuilder sb = new SqlBuilder();
    try {
      // Create column name/class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("UPDATE ").addQuery(tableName);
      // Add SET clause.
      addSetQuery(sb, params, null, itemClsMap);

    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "params", params), e);
    }

    try {
      // Execute SQL.
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data update. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Deletes a single record from the specified table.<br>
   * <ul>
   * <li>Deletes one record from the specified table.</li>
   * <li>Throws an exception if multiple records are deleted.</li>
   * <li>A WHERE clause is built using the key columns.</li>
   * <li>Key columns MUST be included in the parameter values.</li>
   * <li>Parameters that are not key columns are ignored.</li>
   * <li>Key column names MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key columns)
   * @param keyItems  the key column names
   *
   * @return <code>true</code> if one record was deleted; <code>false</code> if zero records were deleted
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
   * Deletes a single record from the specified table (with timestamp optimistic locking).<br>
   * <ul>
   * <li>Deletes one record from the specified table.</li>
   * <li>Throws an exception if multiple records are deleted.</li>
   * <li>Performs optimistic locking using a timestamp.</li>
   * <li>A WHERE clause is built using the key columns and the timestamp column (for locking).</li>
   * <li>Key columns and the timestamp column MUST be included in the parameter values.</li>
   * <li>Parameters that are neither key columns nor the timestamp column are ignored.</li>
   * <li>Key column names and the timestamp column name MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use <code>#deleteOne(Connection, String, AbstractIoTypeMap, String[])</code> if timestamp optimistic locking is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including key columns and the timestamp column)
   * @param keyItems  the key column names
   * @param tsItem    the timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was deleted; <code>false</code> if zero records were deleted
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
   * Deletes a single record from the specified table by primary key.<br>
   * <ul>
   * <li>Deletes one record from the specified table.</li>
   * <li>Throws an exception if multiple records are deleted.</li>
   * <li>A WHERE clause is built using the primary key columns of the table.</li>
   * <li>Throws an exception if the table has no primary key.</li>
   * <li>Primary key columns MUST be included in the parameter values.</li>
   * <li>Parameters that are not primary key columns are ignored.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including primary key columns)
   *
   * @return <code>true</code> if one record was deleted; <code>false</code> if zero records were deleted
   */
  public static boolean deleteOneByPkey(final Connection conn, final String tableName, final AbstractIoTypeMap params) {
    final String[] pkItems = getPkeys(conn, tableName);
    return deleteOne(conn, tableName, params, pkItems);
  }

  /**
   * Deletes a single record from the specified table by primary key (with timestamp optimistic locking).<br>
   * <ul>
   * <li>Deletes one record from the specified table.</li>
   * <li>Throws an exception if multiple records are deleted.</li>
   * <li>Performs optimistic locking using a timestamp.</li>
   * <li>A WHERE clause is built using the primary key columns and the timestamp column (for locking).</li>
   * <li>Throws an exception if the table has no primary key.</li>
   * <li>Primary key columns and the timestamp column MUST be included in the parameter values.</li>
   * <li>Parameters that are neither primary key columns nor the timestamp column are ignored.</li>
   * <li>The timestamp column name MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * <li>Use <code>#deleteOneByPkey(Connection, String, AbstractIoTypeMap)</code> if timestamp optimistic locking is not required.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   * @param params    the parameter values (including primary key columns and the timestamp column)
   * @param tsItem    the timestamp column name (for optimistic locking)
   *
   * @return <code>true</code> if one record was deleted; <code>false</code> if zero records were deleted
   */
  public static boolean deleteOneByPkey(final Connection conn, final String tableName, final AbstractIoTypeMap params,
      final String tsItem) {
    final String[] pkItems = getPkeys(conn, tableName);
    return deleteOne(conn, tableName, params, pkItems, tsItem);
  }

  /**
   * Deletes multiple records from the specified table.<br>
   * <ul>
   * <li>Deletes multiple records from the specified table.</li>
   * <li>A WHERE clause is built using the search condition columns.</li>
   * <li>Search condition columns MUST be included in the parameter values.</li>
   * <li>Search condition column names MUST be specified in lowercase. (key rule of <code>AbstractIoTypeMap</code>)</li>
   * </ul>
   *
   * @param conn       the database connection
   * @param tableName  the table name
   * @param params     the parameter values (including search condition columns)
   * @param whereItems the search condition column names
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
      // Create column name/class type map.
      final Map<String, ItemClsType> itemClsMap = createItemNameClsMapByMeta(conn, tableName);

      sb.addQuery("DELETE FROM ").addQuery(tableName);
      // Add WHERE clause.
      addWhereQuery(sb, tableName, params, whereItems, itemClsMap);
      
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete SQL generation. " + LogUtil.joinKeyVal("tableName", tableName,
          "whereItems", whereItems, "params", params), e);
    }

    try {
      // Execute SQL.
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Deletes all records from the specified table.<br>
   * <ul>
   * <li>Deletes all records from the specified table.</li>
   * </ul>
   *
   * @param conn      the database connection
   * @param tableName the table name
   *
   * @return the number of deleted records
   */
  public static int deleteAll(final Connection conn, final String tableName) {

    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("DELETE FROM ").addQuery(tableName);

    try {
      // Execute SQL.
      final int ret = executeSql(conn, sb);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during data delete. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * Adds a SET clause.
   *
   * @param sb         the SQL builder
   * @param params     the parameter values (including search condition columns)
   * @param whereItems the search condition column names (optional; pass <code>null</code> if omitted)
   * @param itemClsMap the column name/class type map
   */
  private static void addSetQuery(final SqlBuilder sb, final AbstractIoTypeMap params,
      final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    // Build the SET clause.
    sb.addQuery(" SET ");

    // List of search condition columns for existence check.
    final List<String> whereItemList;
    if (ValUtil.isEmpty(whereItems)) {
      whereItemList = Arrays.asList(new String[] {});
    } else {
      whereItemList = Arrays.asList(whereItems);
    }

    int count = 0;
    for (final String itemName : params.keySet()) {
      if (!itemClsMap.containsKey(itemName)) {
        // Skip parameters not present in the table.
        continue;
      }
      if (whereItemList.contains(itemName)) {
        // Skip if present in the search condition columns.
        continue;
      }

      // Column class type.
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // Get value by column class type.
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // Add SQL.
      sb.addQuery(itemName).addQuery("=?", param).addQuery(",");
      count++;
    }
    if (count == 0) {
      throw new RuntimeException("No columns to update. " + LogUtil.joinKeyVal("params", params, "whereItems", whereItems));
    }
    // Remove the trailing comma.
    sb.delLastChar();
  }

  /**
   * Adds a WHERE clause.
   *
   * @param sb         the SQL builder
   * @param tableName  the table name
   * @param params     the parameter values (including search condition columns)
   * @param whereItems the search condition column names (optional; pass <code>null</code> if omitted)
   * @param itemClsMap the column name/class type map
   */
  private static void addWhereQuery(final SqlBuilder sb, final String tableName,
      final AbstractIoTypeMap params, final String[] whereItems, final Map<String, ItemClsType> itemClsMap) {

    if (ValUtil.isEmpty(whereItems)) {
      // 抽出条件項目名が指定されていない場合はエラー
      throw new RuntimeException("Extraction condition column names are required. " + LogUtil.joinKeyVal("tableName", tableName, "params", params));
    }

    // WHERE句の作成
    sb.addQuery(" WHERE ");
    for (final String itemName : whereItems) {
      if (!itemClsMap.containsKey(itemName)) {
        // テーブルに存在しない場合はエラー
        throw new RuntimeException("Extraction condition field does not exist in table. " +
          LogUtil.joinKeyVal("tableName", tableName, "whereItemName", itemName, "params", params));
      }
      if (!params.containsKey(itemName)) {
        // 抽出条件項目がパラメーターに存在しない場合はエラー
        throw new RuntimeException("Extraction condition field does not exist in parameters. " +
          LogUtil.joinKeyVal("tableName", tableName, "whereItemName", itemName, "params", params));
      }

      // 項目クラスタイプ
      final ItemClsType itemCls = itemClsMap.get(itemName);
      // 項目クラスタイプごとの値取得
      final Object param = getValueFromIoItemsByItemCls(params, itemName, itemCls);
      // SQL追加
      sb.addQuery(itemName).addQuery("=?", param).addQuery(" AND ");
    }
    sb.delLastChar(4);
  }

  /**
   * SQL １件登録・更新・削除.<br>
   * <ul>
   * <li>反映件数が複数件の場合は例外エラーとする。</li>
   * </ul>
   *
   * @param conn DB接続
   * @param sb SQL Bean
   * @return 反映件数が１件の場合は <code>true</code>、０件の場合は <code>false</code>
   */
  public static boolean executeOne(final Connection conn, final SqlBean sb) {
    final int ret = execute(conn, sb);
    if (ret > 1) {
      throw new RuntimeException("Multiple records were affected. " + LogUtil.joinKeyVal("sql", sb));
    }
    return (ret == 1);
  }

  /**
   * SQL 登録・更新・削除.
   *
   * @param conn DB接続
   * @param sb SQL Bean
   * @return 反映件数
   */
  public static int execute(final Connection conn, final SqlBean sb) {
    try {
      return executeSql(conn, sb);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during SQL execution. " + LogUtil.joinKeyVal("sql", sb), e);
    }
  }

  /**
   * SQL実行.
   *
   * @param conn DB接続
   * @param sb SQL Bean
   * @return 反映件数
   * @throws SQLException SQL例外エラー
   */
  private static int executeSql(final Connection conn, final SqlBean sb)
      throws SQLException {
        
    if (logger.isDevelopMode()) {
      // SQLログ出力
      logger.develop("SQL#EXECUTE execution. " + LogUtil.joinKeyVal("sql", sb));
    }
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // ステートメント生成
    try (final PreparedStatement stmt = conn.prepareStatement(sb.getQuery());) {
      // ステートメントにパラメーターセット
      setStmtParameters(stmt, sb.getBindValues(), dbmsName);
      // SQL実行
      final int ret = stmt.executeUpdate();
      return ret;
    }
  }

  /**
   * ステートメントにパラメーターセット.
   *
   * @param stmt     ステートメント
   * @param bindValues   バインド値リスト
   * @param dbmsName DBMS名
   * @throws SQLException SQL例外エラー
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
          // java.sql.Timestamp の場合は String に変換してセット
          final java.sql.Timestamp ts = (java.sql.Timestamp) bindValue;
          final LocalDateTime ldt = ts.toLocalDateTime();
          final String s = DTF_SQL_TIMESTAMP.format(ldt);
          stmt.setString(bindNo, s);
        } else if (bindValue instanceof java.sql.Date) {
          // java.sql.Date の場合は String に変換してセット
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
   * ステートメントにフェッチ関連プロパティをセット.
   *
   * @param stmt ステートメント
   * @param fetchSize フェッチサイズ
   * @throws SQLException SQL例外エラー
   */
  private static void setStmtFetchProperty(final PreparedStatement stmt, final int fetchSize)
      throws SQLException {
    // フェッチ方向とフェッチサイズをセット
    stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    stmt.setFetchSize(fetchSize);
  }

  /**
   * 結果セットからDB項目名・クラスタイプマップ作成.<br>
   * <ul>
   * <li>結果セットから項目名とクラスタイプのマップを作成する。</li>
   * <li>マップは項目順を保持する。</li>
   * <li>項目物理名は英字小文字に変換する。（<code>AbstractIoTypeMap</code> のキールールとあわせる）</li>
   * </ul>
   *
   * @param rset 結果セット
   * @return DB項目名・クラスタイプマップ
   * @throws SQLException SQL例外エラー
   */
  private static Map<String, ItemClsType> createItemNameClsMap(final ResultSet rset)
      throws SQLException {

    // DB項目名・クラスタイプマップ
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS名
    final DbmsName dbmsName = DbUtil.getDbmsName(rset);
    // 結果セットメタ情報
    final ResultSetMetaData rmeta = rset.getMetaData();
    // 列数
    final int itemCount = rmeta.getColumnCount();
    // 結果セット列のループ
    for (int c = 1; c <= itemCount; c++) {
      // 列名
      final String itemName;
      if (DbmsName.DB2 == dbmsName) {
        // DB2 の #getColumnName は別名でなく元の項目名を返すため
        itemName = rmeta.getColumnLabel(c).toLowerCase();
      } else {
        itemName = rmeta.getColumnName(c).toLowerCase();
      }
      // 型No
      final int typeNo = rmeta.getColumnType(c);
      // 型名
      // Oracle は DATE型も時刻を持っており TIMESTAMP と判断されるので型名で判断する必要がある。
      final String typeName = rmeta.getColumnTypeName(c).toUpperCase();
      // 項目クラスタイプ
      final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

      itemClsMap.put(itemName, itemCls);
    }
    return itemClsMap;
  }

  /**
   * テーブル指定でDB項目名・クラスタイプマップ作成.<br>
   * <ul>
   * <li>DBメタ情報から項目名とクラスタイプのマップを作成する。</li>
   * <li>マップは項目順を保持する。</li>
   * <li>項目物理名は英字小文字に変換する。（<code>AbstractIoTypeMap</code> のキールールとあわせる）</li>
   * </ul>
   *
   * @param conn DB接続
   * @param tableName テーブル名
   * @throws SQLException SQL例外エラー
   */
  private static Map<String, ItemClsType> createItemNameClsMapByMeta(final Connection conn,
      final String tableName) throws SQLException {

    // DB項目名・クラスタイプマップ
    final Map<String, ItemClsType> itemClsMap = new LinkedHashMap<>();

    // DBMS名
    final DbmsName dbmsName = DbUtil.getDbmsName(conn);
    // DBメタ情報
    final DatabaseMetaData cmeta = conn.getMetaData();
    // 列情報結果セット
    try (final ResultSet rset = cmeta.getColumns(null, null, tableName, null)) {
      while (rset.next()) {
        // 列名
        final String itemName = rset.getString("COLUMN_NAME").toLowerCase();
        // 型No
        final int typeNo = rset.getInt("DATA_TYPE");
        // 型名
        // Oracle は DATE型も時刻を持っており TIMESTAMP と判断されるので型名で判断する必要がある。
        final String typeName = rset.getString("TYPE_NAME");
        // 項目クラスタイプ
        final ItemClsType itemCls = convItemClsType(typeNo, typeName, dbmsName);

        itemClsMap.put(itemName, itemCls);
      }
    }
    return itemClsMap;
  }

  /**
   * DB項目クラスタイプ変換.
   *
   * @param typeNo   型No
   * @param typeName 型名大文字
   * @param dbmsName DBMS名
   * @return クラスタイプ
   */
  private static ItemClsType convItemClsType(final int typeNo, final String typeName, final DbmsName dbmsName) {
    final ItemClsType itemCls;
    if (/* BigDecimal にマッピングされる JDBC型 */ Types.DECIMAL == typeNo || Types.NUMERIC == typeNo
        || /* Integer にマッピングされる JDBC型 */ Types.TINYINT == typeNo || Types.SMALLINT == typeNo
        || Types.INTEGER == typeNo || /* Long にマッピングされる JDBC型 */ Types.BIGINT == typeNo
        || /* Float にマッピングされる JDBC型 */ Types.FLOAT == typeNo || Types.REAL == typeNo
        || /* Double にマッピングされる JDBC型 */ Types.DOUBLE == typeNo) {
      // 数値の型は BigDecimal に統一する
      itemCls = ItemClsType.BIGDECIMAL_CLS;
    } else if (Types.DATE == typeNo) {
      if ("DATETIME".equals(typeName) && DbmsName.MSSQL == dbmsName) {
        itemCls = ItemClsType.TIMESTAMP_CLS;
      } else if (DbmsName.SQLITE == dbmsName) {
        // SQLLite で Types.DATE が結果セットから返された場合は実際は文字列なので変換する必要がある（#createIoItemsFromResultSet 参照）
        itemCls = ItemClsType.STRING_TO_DATE_CLS;
      } else {
        itemCls = ItemClsType.DATE_CLS;
      }
    } else if (Types.TIMESTAMP == typeNo || Types.TIMESTAMP_WITH_TIMEZONE == typeNo) {
      if ("DATE".equals(typeName) && DbmsName.ORACLE == dbmsName) {
        itemCls = ItemClsType.DATE_CLS;
      } else if (DbmsName.SQLITE == dbmsName) {
        // SQLLite で Types.TIMESTAMP が結果セットから返された場合は実際は文字列なので変換する必要がある（#createIoItemsFromResultSet 参照）
        itemCls = ItemClsType.STRING_TO_TS_CLS;
      } else {
        itemCls = ItemClsType.TIMESTAMP_CLS;
      }
    } else {
      if (DbmsName.SQLITE == dbmsName) {
        if ("DATE".equals(typeName)) {
          // SQLLite のテーブルメタ情報は Types.VARCHAR でタイプ名が "DATE" となる
          itemCls = ItemClsType.STRING_TO_DATE_CLS;
        } else if ("TIMESTAMP".equals(typeName)) {
          // SQLLite のテーブルメタ情報は Types.VARCHAR でタイプ名が "TIMESTAMP" となる
          itemCls = ItemClsType.STRING_TO_TS_CLS;
        } else {
          itemCls = ItemClsType.STRING_CLS;
        }
      } else {
        // 文字列の型は String に統一する
        itemCls = ItemClsType.STRING_CLS;
      }
    }
    return itemCls;
  }

  /**
   * 結果セット行マップ作成.<br>
   * <ul>
   * <li>結果セットの現在行の値をマップで返す。</li>
   * </ul>
   *
   * @param rset 結果セット
   * @param itemClsMap DB項目名・クラスタイプマップ
   * @return 行マップ
   */
  static IoItems createIoItemsFromResultSet(final ResultSet rset,
      final Map<String, ItemClsType> itemClsMap) {

    // 行マップ
    final IoItems rowMap = new IoItems();

    // DB項目のループ
    for (final Map.Entry<String, ItemClsType> ent : itemClsMap.entrySet()) {
      // 項目名
      final String itemName = ent.getKey();
      try {
        // 項目クラスタイプ
        final ItemClsType itemCls = ent.getValue();
        // 項目クラスタイプごとの値セット
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
          // 本来は Date でセットする必要があるが、IoItems 内で LocalDate に変換されてセットされるためそのままセット
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
            // 小数秒が無い場合は .000000 を付与する（"uuuu-MM-dd HH:mm:ss"＝19文字）
            ajustVal = value + ".000000";
          } else if (19 < len && len < 26) {
            // 小数秒の桁数不足は 000000 を付与する（"uuuu-MM-dd HH:mm:ss.SSSSSS"＝26文字）
            ajustVal = ValUtil.substring(value + "000000", 0, 26);
          } else if (len == 26) {
            ajustVal = value;
          } else if (len > 26) {
            // 小数秒の桁数は 6桁で切る（"uuuu-MM-dd HH:mm:ss.SSSSSS"＝26文字）
            ajustVal = ValUtil.substring(value, 0, 26);
          } else {
            rowMap.putNull(itemName);
            continue;
          }
          final LocalDateTime ldt = LocalDateTime.parse(ajustVal, DTF_SQL_TIMESTAMP);
          // 本来は Timestamp でセットする必要があるが、IoItems 内で LocalDateTime に変換されてセットされるためそのままセット
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
   * パラメーター値取得.<br>
   * <ul>
   * <li>DB項目クラスタイプによってパラメーターの getter を使い分けて値を取得して返す。</li>
   * </ul>
   *
   * @param params パラメーター
   * @param itemName 項目名
   * @param itemCls DB項目クラスタイプ
   * @return パラメーター値
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
   * 一意制約違反エラー判定.<br>
   * <ul>
   * <li>DBMS別に一意制約違反エラーかどうかを判定する。</li>
   * <li>一意制約違反の場合は <code>true</code> を返す。</li>
   * </ul>
   *
   * @param e SQL例外
   * @param dbmsName DBMS名
   * @return 一意制約違反エラーの場合は <code>true</code>
   */
  private static boolean isUniqueKeyErr(final SQLException e, final DbmsName dbmsName) {
    // Oracle 一意制約違反エラー判定
    if (dbmsName == DbmsName.ORACLE && e.getErrorCode() == 1) {
      return true;
    }
    // PostgreSQL 一意制約違反エラー判定
    if (dbmsName == DbmsName.POSTGRESQL && "23505".equals(e.getSQLState())) {
      return true;
    } 
    // MS-SqlServer 一意制約違反エラー判定
    if (dbmsName == DbmsName.MSSQL && (e.getErrorCode() == 2627 || e.getErrorCode() == 2601)) {
      return true;
    }
    // SQLite 一意制約違反エラー判定
    if (dbmsName == DbmsName.SQLITE && e.getErrorCode() == 19
        && e.getMessage().contains("UNIQUE constraint failed")) {
      return true;
    }
    // DB2 一意制約違反エラー判定
    if (dbmsName == DbmsName.DB2 && "23505".equals(e.getSQLState())) {
      return true;
    }

    return false;
  }

  /**
   * 主キー項目名取得.<br>
   * <ul>
   * <li>JDBCメタ情報からテーブルの主キー項目名を取得します。</li>
   * <li>主キーが存在しないテーブルは実行時エラーとなります。</li>
   * <li>項目物理名は英字小文字に変換します。（<code>AbstractIoTypeMap</code> のキールール）</li>
   * </ul>
   *
   * @param conn      DB接続
   * @param tableName テーブル名
   * @return 主キー項目名配列（KEY_SEQ 順）
   */
  private static String[] getPkeys(final Connection conn, final String tableName) {
    try {
      // KEY_SEQ 順で並べる
      final Map<Short, String> pkMap = new TreeMap<>();
      try (final ResultSet rset = conn.getMetaData().getPrimaryKeys(null, null, tableName)) {
        while (rset.next()) {
          pkMap.put(rset.getShort("KEY_SEQ"), rset.getString("COLUMN_NAME").toLowerCase());
        }
      }
      return pkMap.values().toArray(new String[0]);
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred during primary key retrieval. " + LogUtil.joinKeyVal("tableName", tableName), e);
    }
  }

  /** タイムスタンプ取得SQL（小数秒6桁） マップ. */
  private static final Map<DbmsName, String> SQL_CUR_TS = new HashMap<>();
  /** タイムスタンプ取得SQL（小数秒6桁） その他. */
  private static final String SQL_CUR_TS_OTHER;
  static {
    // SQLLite は小数秒3桁しか取得できないため、000を付与して6桁にする
    SQL_CUR_TS.put(DbmsName.SQLITE, "strftime('%Y-%m-%d %H:%M:%f000', 'now', 'localtime')");
    SQL_CUR_TS.put(DbmsName.MSSQL, "SYSDATETIME()");
    SQL_CUR_TS_OTHER = "CURRENT_TIMESTAMP(6)";
  }

  /**
   * DBMS別 現在タイムスタンプ値取得SQL取得.
   *
   * @param dbmsName DBMS名
   * @return 現在タイムスタンプ値取得SQL
   */
  private static String getCurrentTimestampSql(final DbmsName dbmsName) {
    return SQL_CUR_TS.getOrDefault(dbmsName, SQL_CUR_TS_OTHER);
  }

  /** 日付取得SELECT文 マップ. */
  private static final Map<DbmsName, SqlConst> SQL_SELECT_TODAY = new HashMap<>();
  static {
    SQL_SELECT_TODAY.put(DbmsName.POSTGRESQL, SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today").end());
    SQL_SELECT_TODAY.put(DbmsName.ORACLE,     SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM DUAL").end());
    SQL_SELECT_TODAY.put(DbmsName.MSSQL,      SqlConst.begin().addQuery("SELECT CONVERT(VARCHAR, FORMAT(GETDATE(), 'yyyyMMdd')) today").end());
    SQL_SELECT_TODAY.put(DbmsName.SQLITE,     SqlConst.begin().addQuery("SELECT strftime('%Y%m%d', 'now', 'localtime') today").end());
    SQL_SELECT_TODAY.put(DbmsName.DB2,        SqlConst.begin().addQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP,'YYYYMMDD') today FROM SYSIBM.DUAL").end());
  }
  
  /**
   * DBMS別 現在日付取得.
   *
   * @param conn DB接続
   * @return 現在日付（YYYYMMDD形式）
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

  /** １バイトブランク. */
  private static final String ONEBLANK = " ";

  /**
   * SQL追加.<br>
   * <ul>
   * <li>SQL文字列を StringBuilder に追加します。</li>
   * <li>引数SQLの先頭がブランクの場合、先頭に１文字ブランク追加します。（ただし既存SQLが空または最後がブランクの場合は追加しない）</li>
   * <li>引数SQLの前後のブランクをトリムし、２文字以上のブランクを１文字ブランクに置き換えます。</li>
   * <li>引数SQLの最後がブランクの場合、最後に１文字ブランク追加します。</li>
   * </ul>
   * 
   * @param toSb  追加先 StringBuilder
   * @param sql 追加するSQL
   */
  static void appendQuery(final StringBuilder toSb, final String sql) {
    if (ValUtil.isBlank(sql)) {
      return;
    }

    // 引数SQLの先頭がブランクの場合、先頭に１文字ブランク追加
    // ただし既存SQLが空または最後がブランクの場合は追加しない
    if (sql.startsWith(ONEBLANK) && toSb.length() > 0
        && toSb.charAt(toSb.length() - 1) != ' ') {
      toSb.append(ONEBLANK);
    }

    // 前後のブランクをトリム
    // ２文字以上のブランクを１文字ブランクに置き換え
    // ただしシングルクォーテーションに挟まれたブランクは置き換えない
    toSb.append(trimQuerySpaces(sql));

    // 引数SQLの最後がブランクの場合、最後に１文字ブランク追加
    if (sql.endsWith(ONEBLANK)) {
      toSb.append(ONEBLANK);
    }
  }

  /**
   * SQL文字列内の２文字以上のブランクを１文字ブランクに置き換え.<br>
   * <ul>
   * <li>前後のブランクをトリム。</li>
   * <li>２文字以上のブランクを１文字ブランクに置き換え。</li>
   * <li>シングルクォーテーションに挟まれたブランクは置き換えない。</li>
   * </ul>
   * 
   * @param sql SQL
   * @return 結果SQL
   */
  private static String trimQuerySpaces(final String sql) {
    if (ValUtil.isBlank(sql)) {
      return ValUtil.BLANK;
    }
    
    final int length = sql.length();
    final char[] chars = sql.toCharArray(); // 配列アクセスで高速化
    final StringBuilder ret = new StringBuilder(length);
    
    boolean inSq = false;
    boolean prevSpace = false;
    int beginPos = 0;
    int endPos = length;
    
    // 前後のトリムを事前計算
    while (beginPos < endPos && Character.isWhitespace(chars[beginPos])) {
      beginPos++;
    }
    while (endPos > beginPos && Character.isWhitespace(chars[endPos - 1])) {
      endPos--;
    }
    
    for (int i = beginPos; i < endPos; i++) {
      final char c = chars[i];
      if (c == '\'') {
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
