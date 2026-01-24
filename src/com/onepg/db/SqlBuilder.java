package com.onepg.db;

import com.onepg.util.ValUtil;

import java.util.List;

/**
 * SQL builder.<br>
 * <ul>
 * <li>A class that encapsulates SQL and parameter list required for database access.</li>
 * <li>Has methods that can build SQL and set parameters simultaneously.</li>
 * <li><code>add*</code> methods return the instance itself, so they can be used in method chains.</li>
 * </ul>
 * <pre>
 * [SQL Addition Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * [SQL Addition Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * [SQL Addition Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * [SQL Addition Example 4] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * [SQL Addition Example 5] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * [SQL Execution Example] <code>SqlResultSet rSet = SqlUtil.select(getDbConn(), sqlBuilder);</code>
 * </pre>
 * 
 */
public final class SqlBuilder extends SqlBean {

  /**
   * Constructor.
   */
  public SqlBuilder() {
    super();
  }

  /**
   * Appends SQL string.<br>
   * <ul>
   * <li>Appends SQL string.</li>
   * <li>Replaces 2 or more consecutive blanks with a single blank when appending.</li>
   * </ul>
   * 
   * @param sql the SQL string
   */
  private void appendQuery(final String sql) {
    SqlUtil.appendQuery(super.queryBuilder, sql);
  }

  /**
   * Adds parameters.<br>
   * <ul>
   * <li>Can pass multiple parameters.</li>
   * </ul>
   *
   * @param params the parameters (multiple allowed)
   */
  private void addAllParameters(final Object... params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    for (final Object param : params) {
      super.bindValues.add(param);
    }
  }

  /**
   * Adds parameter list.
   *
   * @param params the parameter list
   */
  private void addAllParameters(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    super.bindValues.addAll(params);
  }

  /**
   * Adds SQL builder.<br>
   * <ul>
   * <li>Inherits SQL and parameters.</li>
   * </ul>
   *
   * @param sb the SQL builder
   */
  public void addSqlBuilder(final SqlBuilder sb) {
    // Add SQL
    appendQuery(sb.getQuery());
    // Add parameters
    addAllParameters(sb.getBindValues());
  }

  /**
   * Adds SQL and parameters.<br>
   * <ul>
   * <li>Parameter arguments are optional and can be passed as single or multiple.</li>
   * </ul>
   * <pre>
   * [Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
   * [Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
   * [Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
   * </pre>
   * @param sql    the SQL
   * @param params the parameters (multiple allowed) (optional)
   * @return the instance itself
   */
  public SqlBuilder addQuery(final String sql, final Object... params) {
    // Add SQL
    appendQuery(sql);
    // Add parameters
    addAllParameters(params);
    return this;
  }

  /**
   * Adds parameters.<br>
   * <ul>
   * <li>Can pass multiple parameters.</li>
   * </ul>
   * <pre>
   * [Example 1] <code>sqlBuilder.addParams(userId);</code>
   * [Example 2] <code>sqlBuilder.addParams(birthDtFrom, birthDtTo);</code>
   * </pre>
   *
   * @param params the parameters (multiple allowed)
   * @return the instance itself
   */
  public SqlBuilder addParams(final Object... params) {
    addAllParameters(params);
    return this;
  }

  /**
   * Adds comma-separated SQL bind characters.<br>
   * <ul>
   * <li>Adds SQL bind characters "?" separated by commas to SQL for the number of elements in the list.</li>
   * <li>If the list contains 3 elements, "?,?,?" is added to SQL.</li>
   * <li>Values in the list are added as SQL bind character parameters.</li>
   * <li>Intended for use in IN clauses with variable number of SQL bind characters.</li>
   * </ul>
   * <pre>
   * [Example]<code>sqlBuilder.addQuery("AND type_cs IN (").addListInBind(list).addQuery(")");</code>
   * </pre>
   *
   * @param params the parameter list
   * @return the instance itself
   */
  public SqlBuilder addListInBind(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return this;
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      sb.append("?,");
    }
    ValUtil.deleteLastChar(sb);

    appendQuery(sb.toString());
    addAllParameters(params);
    return this;
  }

  /**
   * Adds SQL and parameter only if the parameter is not <code>null</code> and not blank.<br>
   * <ul>
   * <li>Adds SQL and parameter only if the parameter is not <code>null</code> and not blank.</li>
   * <li>Other specifications are the same as <code>#addQuery(String, Object...)</code>.</li>
   * <li>Use the shortcut method <code>#addQnotB(String, Object)</code> for this method.</li>
   * </ul>
   * <pre>In the example below, SQL is added only if <code>userId</code> is not <code>null</code> and not blank.
   * [Example] <code>sqlBuilder.addQueryIfNotBlankParameter("AND user_id = ? ", userId);</code>
   * </pre>
   * @see #addQuery(String, Object...)
   * @see #addQnotB(String, Object)
   * @param sql the SQL
   * @param param the parameter (single only)
   * @return this instance
   */
  public SqlBuilder addQueryIfNotBlankParameter(final String sql, final Object param) {

    if (ValUtil.isNull(param)) {
      return this;
    }

    if (param instanceof String && ValUtil.isBlank((String) param)) {
      return this;
    }

    addQuery(sql, param);
    return this;
  }

  /**
   * Adds SQL and parameter only if the parameter is not <code>null</code> and not blank.<br>
   * <ul>
   * <li>Shortcut for <code>#addQueryIfNotBlankParameter(String, Object)</code>.</li>
   * <li>Adds SQL and parameter only if the parameter is not <code>null</code> and not blank.</li>
   * <li>Other specifications are the same as <code>#addQuery(String, Object...)</code>.</li>
   * </ul>
   * <pre>In the example below, SQL is added only if <code>userId</code> is not <code>null</code> and not blank.
   * [Example] <code>sqlBuilder.addQnotB("AND user_id = ? ", userId);</code>
   * </pre>
   *
   * @see #addQueryIfNotBlankParameter(String, Object)
   * @param sql the SQL
   * @param param the parameter (single only)
   * @return this instance
   */
  @SuppressWarnings("all")
  public SqlBuilder addQnotB(final String sql, final Object param) {
    return addQueryIfNotBlankParameter(sql, param);
  }

  /**
   * Deletes the last SQL character.
   * <ul>
   * <li>Deletes the last character (one character) of the SQL string.</li>
   * </ul>
   * <pre>[Example]
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(",");
   * }
   * // Delete the last comma
   * sb.deleteLastChar();
   * </code></pre>
   * 
   * @return the instance itself
   */
  public SqlBuilder delLastChar() {
    ValUtil.deleteLastChar(super.queryBuilder);
    return this;
  }
  
  /**
   * Deletes the last SQL characters.
   * <ul>
   * <li>Deletes the specified number of characters from the end of the SQL string.</li>
   * </ul>
   * <pre>[Example]
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(" AND ");
   * }
   * // Delete the last AND
   * sb.delLastChar(4);
   * </code></pre>
   * 
   * @param deleteCharCount the number of characters to delete
   * @return the instance itself
   */
  public SqlBuilder delLastChar(final int deleteCharCount) {
    ValUtil.deleteLastChar(super.queryBuilder, deleteCharCount);
    return this;
  }
  
}
