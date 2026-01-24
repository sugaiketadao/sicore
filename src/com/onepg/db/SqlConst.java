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
 * <li>Stores SQL string and bind item definitions (item name and type).</li>
 * <li>When executing SQL, parameters with bind values are passed together with this SQL.</li>
 * <li>The same item name can be bound multiple times with the same type.</li>
 * <li>If there are no bind items, it is used as is.</li>
 * </ul>
 * <pre>
 * [SQL Declaration Example 1]<code>SqlConst SQL_INS_PET = SqlConst.begin()
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
 * [SQL Execution Example 1] <code>SqlUtil.executeOne(conn, SQL_INS_PET.bind(io));</code>
 * [SQL Declaration Example 2]<code>SqlConst SQL_SEL_USER = SqlConst.begin()
 *     .addQuery("SELECT ")
 *     .addQuery("  u.user_id ")
 *     .addQuery(", u.user_nm ")
 *     .addQuery(", u.email ")
 *     .addQuery(", u.birth_dt ")
 *     .addQuery(" FROM t_user u ")
 *     .addQuery(" ORDER BY u.user_id ")
 *     .end();</code>
 * [SQL Execution Example 2] <code>SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);</code>
 * </pre>
 */
public final class SqlConst extends SqlBean {

  /**
   * Bind type.<br>
   * <ul>
   * <li>Indicates the type when binding to SQL.</li>
   * <li>Numeric types are unified to <code>BigDecimal</code>.</li>
   * </ul>
   */
  public enum BindType {
    STRING, BIGDECIMAL, DATE, TIMESTAMP
  }

  /** 
   * Bind item name list.<br>
   * <ul>
   * <li>List to preserve the order of bind item names.</li>
   * <li>When the same item name is bound multiple times, it is stored multiple times in that order.</li>
   * </ul>
   */
  private final List<String> bindItemNames;
  /** Bind item definition map <item name, type>. */
  private final Map<String, BindType> bindItems;
  
  /**
   * Constructor.
   * 
   * @param query the SQL string
   * @param bindItemNames the bind item name list
   * @param bindItems the bind item definition map <item name, type>
   */
  SqlConst(final String query, final List<String> bindItemNames, final Map<String, BindType> bindItems) {
    super(query);
    this.bindItemNames = bindItemNames;
    this.bindItems = bindItems;
  }
  
  /**
   * Creates fixed SQL builder instance.
   * 
   * @return the fixed SQL builder instance
   */
  public static SqlConstBuilder begin() {
    return new SqlConstBuilder();
  }
  
  /**
   * Sets bind values.<br>
   * <ul>
   * <li>Returns a SQL Bean with the stored SQL string and the parameter value map received as an argument.</li>
   * <li>Creates a bind value list from the parameter value map based on the bind item name list and bind item definition map, and sets it in the SQL Bean.</li>
   * <li>Each element of the bind value list is <code>Object</code>, and each item value is stored in the type according to the bind item definition.</li>
   * <li>If the bind item name does not exist in the parameter value map, a runtime error occurs.</li>
   * </ul>
   * 
   * @param params the parameter value map
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
   * <li>Builder class that builds fixed SQL (<code>SqlConst</code>).</li>
   * <li>Has methods that can build SQL and define bind items (item name and type) simultaneously.</li>
   * <li><code>addQuery</code> methods return the instance itself, so they can be used in method chains.</li>
   * </ul>
   */
  public static final class SqlConstBuilder {

    /** SQL string. */
    private final StringBuilder query = new StringBuilder();

    /** 
     * Bind item name list.<br>
     * <ul>
     * <li>List to preserve the order of bind item names.</li>
     * <li>When the same item name is bound multiple times, it is stored multiple times in that order.</li>
     * </ul>
     */
    private final List<String> bindItemNames = new ArrayList<>();
    /** Bind item definition map <item name, type>. */
    private final Map<String, BindType> bindItems = new LinkedHashMap<>();
      
    /**
     * Constructor.
     */
    SqlConstBuilder() {
      // No processing
    }

    /**
     * Returns fixed SQL.
     *
     * @return the fixed SQL
     */
    public SqlConst end() {
      return new SqlConst(this.query.toString(), this.bindItemNames, this.bindItems);
    }

    /**
     * Adds SQL.<br>
     * <ul>
     * <li>Replaces 2 or more consecutive blanks with a single blank when appending.</li>
     * </ul>
     * 
     * @param sql the SQL
     * @return the instance itself
     */
    public SqlConstBuilder addQuery(final String sql) {
      // Add SQL
      SqlUtil.appendQuery(this.query, sql); 
      return this;
    }

    /**
     * Adds SQL and bind item definition (item name and type).<br>
     * <ul>
     * <li>The SQL string must contain exactly one bind placeholder <code>?</code>.</li>
     * <li>The bind item name must be a valid value as an <code>Io</code> object key. (Key rule of <code>AbstractIoTypeMap</code>)</li>
     * </ul>
     *
     * @param sql      the SQL
     * @param itemName the bind item name
     * @param bindType the bind type
     * @return the instance itself
     */
    public SqlConstBuilder addQuery(final String sql, final String itemName, final BindType bindType) {
      // Check bind item name
      ValUtil.validateIoKey(itemName);

      if (ValUtil.isBlank(sql)) {
        throw new RuntimeException("SQL must not be blank.");
      }
      // Check number of ? in SQL string
      final int placeholderCount = sql.length() - sql.replace("?", "").length();
      if (placeholderCount != 1) {
        throw new RuntimeException("SQL must contain exactly one bind placeholder '?'. "
            + LogUtil.joinKeyVal("sql", sql, "placeholderCount", placeholderCount));
      }
      // Check existing bind item type
      if (this.bindItems.containsKey(itemName) && this.bindItems.get(itemName) != bindType) {
        throw new RuntimeException("Bind item already exists with different type. "
                                + LogUtil.joinKeyVal("itemName", itemName,
                                                    "existingType", this.bindItems.get(itemName),
                                                    "newType", bindType));
      }
    
      // Add SQL
      SqlUtil.appendQuery(this.query, sql); 
      // Add to bind item name list and bind item definition map
      this.bindItemNames.add(itemName);
      this.bindItems.put(itemName, bindType);

      return this;
    }
  }
  
}
