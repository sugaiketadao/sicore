package com.onepg.db;

import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixed SQL.<br>
 * <ul>
 * <li>Stores the SQL string and bind field definitions (field names and types).</li>
 * <li>Primarily intended for use in batch processing; declare as a class variable (constant).</li>
 * <li>Using this class enables caching of prepared statements, so its use for performance improvement in web service processing is also assumed.</li>
 * <li>When executing SQL, pass parameters with bind values together with this SQL.</li>
 * <li>The same field name can be bound multiple times with the same type.</li>
 * <li>If there are no bind fields, use as-is.</li>
 * <li>To execute using the prepared statement cache, use <code>SqlUtil.executeOneCache</code> or <code>SqlUtil.executeCache</code>.</li>
 * </ul>
 * <pre>
 * [SQL declaration example - with bind fields]
 * <code>SqlConst SQL_INS_PET = SqlConst.begin()
 *     .addQuery("INSERT INTO t_pet ( ")
 *     .addQuery("  pet_no ")
 *     .addQuery(", pet_nm ")
 *     .addQuery(", birth_dt ")
 *     .addQuery(", ins_ts ")
 *     .addQuery(", upd_ts ")
 *     .addQuery(" ) VALUES ( ")
 *     .addQuery("  ? ", "pet_no", BindType.BigDecimal)
 *     .addQuery(", ? ", "pet_nm", BindType.String)
 *     .addQuery(", ? ", "birth_dt", BindType.Date)
 *     .addQuery(", ? ", "now_ts", BindType.Timestamp)
 *     .addQuery(", ? ", "now_ts", BindType.Timestamp)
 *     .addQuery(" ) ")
 *     .end();</code>
 * [SQL execution example - with bind fields]
 * <code>SqlUtil.executeOne(conn, SQL_INS_PET.bind(io));</code>
 * [SQL declaration example - without bind fields]
 * <code>SqlConst SQL_SEL_USER = SqlConst.begin()
 *     .addQuery("SELECT ")
 *     .addQuery("  u.user_id ")
 *     .addQuery(", u.user_nm ")
 *     .addQuery(", u.email ")
 *     .addQuery(", u.birth_dt ")
 *     .addQuery(" FROM t_user AS u ")
 *     .addQuery(" ORDER BY u.user_id ")
 *     .end();</code>
 * [SQL execution example - without bind fields]
 * <code>SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);</code>
 * [SQL execution example - prepared statement cache execution]
 * <code>SqlUtil.executeOneCache(conn, SQL_INS_PET.bind(io));</code>
 * </pre>
 */
public final class SqlConst extends SqlBean {

  /**
   * Bind type.<br>
   * <ul>
   * <li>Indicates the type used when binding to SQL.</li>
   * <li>Numeric types are unified as <code>BigDecimal</code>.</li>
   * </ul>
   */
  public enum BindType {
    STRING, BIGDECIMAL, DATE, TIMESTAMP
  }

  /** 
   * Bind field name list.<br>
   * <ul>
   * <li>A list for maintaining the order of bind field names.</li>
   * <li>If the same field name is bound multiple times, it is stored multiple times in order.</li>
   * </ul>
   */
  private final List<String> bindItemNames;
  /** Bind field definition map &lt;field name, type&gt;. */
  private final Map<String, BindType> bindItems;
  
  /**
   * Constructor.
   * 
   * @param query SQL string
   * @param bindItemNames bind field name list
   * @param bindItems bind field definition map &lt;field name, type&gt;
   */
  SqlConst(final String query, final List<String> bindItemNames, final Map<String, BindType> bindItems) {
    super(query);
    this.bindItemNames = bindItemNames;
    this.bindItems = bindItems;
  }
  
  /**
   * Creates a fixed SQL builder instance.
   * 
   * @return the fixed SQL builder instance
   */
  public static SqlConstBuilder begin() {
    return new SqlConstBuilder();
  }
  
  /**
   * Sets bind values.<br>
   * <ul>
   * <li>Sets the stored SQL string and the parameter value map received as an argument into a SQL Bean and returns it.</li>
   * <li>Creates a bind value list from the bind field name list and bind field definition map using values from the parameter value map, and sets it in the SQL Bean.</li>
   * <li>The elements of the bind value list are of type <code>Object</code>, and each field value is stored with the type specified in the bind field definition.</li>
   * <li>If a bind field name does not exist in the parameter value map, a runtime error occurs.</li>
   * </ul>
   * 
   * @param params parameter value map
   * @return the SQL Bean
   */
  public SqlBean bind(final AbstractIoTypeMap params) {
    if (ValUtil.isNull(params)) {
      throw new RuntimeException("Parameter map must not be null.");
    }
    if (ValUtil.isEmpty(this.bindItemNames)) {
      throw new RuntimeException("Bind item list is empty. Use the fixed SQL directly.");
    }
    
    final List<Object> bindValues = new ArrayList<>();
    for (final String itemName : this.bindItemNames) {
      if (!params.containsKey(itemName)) {
        throw new RuntimeException("Parameter value not found for bind item. "
                                + LogUtil.joinKeyVal("itemName", itemName));
      }
      final BindType bindType = this.bindItems.get(itemName);
      if (BindType.STRING == bindType) {
        final String paramValue = params.getString(itemName);
        bindValues.add(paramValue);
      } else if (BindType.BIGDECIMAL == bindType) {
        final BigDecimal paramValue = params.getBigDecimal(itemName);
        bindValues.add(paramValue);
      } else if (BindType.DATE == bindType) {
        final java.sql.Date paramValue = params.getSqlDateNullable(itemName);
        bindValues.add(paramValue);
      } else if (BindType.TIMESTAMP == bindType) {
        final java.sql.Timestamp paramValue = params.getSqlTimestampNullable(itemName);
        bindValues.add(paramValue);
      }
    }
    return new SqlBean(super.id, super.query, bindValues);
  }

  /**
   * Fixed SQL builder.
   * <ul>
   * <li>A builder class for constructing fixed SQL (<code>SqlConst</code>).</li>
   * <li>Provides methods to build SQL and define bind fields (field names and types) simultaneously.</li>
   * <li>The <code>addQuery</code> method returns the instance itself, so it can be used in method chains.</li>
   * </ul>
   */
  public static final class SqlConstBuilder {

    /** SQL string. */
    private final StringBuilder query = new StringBuilder();

    /** 
     * Bind field name list.<br>
     * <ul>
     * <li>A list for maintaining the order of bind field names.</li>
     * <li>If the same field name is bound multiple times, it is stored multiple times in order.</li>
     * </ul>
     */
    private final List<String> bindItemNames = new ArrayList<>();
    /** Bind field definition map &lt;field name, type&gt;. */
    private final Map<String, BindType> bindItems = new LinkedHashMap<>();
      
    /**
     * Constructor.
     */
    SqlConstBuilder() {
      // No processing
    }

    /**
     * Returns the fixed SQL.
     *
     * @return the fixed SQL
     */
    public SqlConst end() {
      return new SqlConst(this.query.toString(), this.bindItemNames, this.bindItems);
    }

    /**
     * Adds SQL.<br>
     * <ul>
     * <li>Replaces two or more consecutive blanks with a single blank before appending.</li>
     * </ul>
     * 
     * @param sql SQL
     * @return the instance itself
     */
    public SqlConstBuilder addQuery(final String sql) {
      // Add SQL
      SqlUtil.appendQuery(this.query, sql); 
      return this;
    }

    /**
     * Adds SQL and bind field definition (field name and type).<br>
     * <ul>
     * <li>The SQL string must contain exactly one bind placeholder <code>?</code>.</li>
     * <li>The bind field name must be a valid value as an <code>Io</code> object key (key rules of <code>AbstractIoTypeMap</code>).</li>
     * </ul>
     *
     * @param sql      SQL
     * @param itemName bind field name
     * @param bindType bind type
     * @return the instance itself
     */
    public SqlConstBuilder addQuery(final String sql, final String itemName, final BindType bindType) {
      // Validate bind field name
      ValUtil.validateIoKey(itemName);

      if (ValUtil.isBlank(sql)) {
        throw new RuntimeException("SQL must not be blank.");
      }
      // Check the number of ? placeholders in the SQL string
      final int placeholderCount = sql.length() - sql.replace("?", "").length();
      if (placeholderCount != 1) {
        throw new RuntimeException("SQL must contain exactly one bind placeholder '?'. "
            + LogUtil.joinKeyVal("sql", sql, "placeholderCount", placeholderCount));
      }
      // Check existing bind field type
      if (this.bindItems.containsKey(itemName) && this.bindItems.get(itemName) != bindType) {
        throw new RuntimeException("Bind item already exists with different type. "
                                + LogUtil.joinKeyVal("itemName", itemName,
                                                    "existingType", this.bindItems.get(itemName),
                                                    "newType", bindType));
      }
    
      // Add SQL
      SqlUtil.appendQuery(this.query, sql); 
      // Add to bind field name list and bind field definition map
      this.bindItemNames.add(itemName);
      this.bindItems.put(itemName, bindType);

      return this;
    }
    
    /**
     * Deletes the last SQL string character.
     * <ul>
     * <li>Deletes the last character (one character) of the SQL string.</li>
     * </ul>
     * 
     * @return the instance itself
     */
    public SqlConstBuilder delLastChar() {
      ValUtil.deleteLastChar(this.query);
      return this;
    }
    
    /**
     * Deletes the last SQL string characters.
     * <ul>
     * <li>Deletes the specified number of characters from the end of the SQL string.</li>
     * </ul>
     * 
     * @param deleteCharCount number of characters to delete
     * @return the instance itself
     */
    public SqlConstBuilder delLastChar(final int deleteCharCount) {
      ValUtil.deleteLastChar(this.query, deleteCharCount);
      return this;
    }
  }
}
