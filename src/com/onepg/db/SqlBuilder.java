package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL builder.<br>
 * <ul>
 * <li>Encapsulates SQL and parameter list required for database access.</li>
 * <li>Has methods that can assemble SQL and set parameters simultaneously.</li>
 * <li>Methods starting with add* return this instance, enabling method chaining.</li>
 * </ul>
 * <pre>
 * [Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * [Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * [Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * [Example 4] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * [Example 5] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * </pre>
 * 
 */
public final class SqlBuilder {

  /** SQL string. */
  private final StringBuilder query = new StringBuilder();
  /** Parameters. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * Constructor.
   */
  public SqlBuilder() {
    super();
  }

  /**
   * Appends an SQL string.<br>
   * <ul>
   * <li>Appends an SQL string.</li>
   * <li>Replaces 2 or more consecutive blanks with a single blank when appending.</li>
   * </ul>
   * 
   * @param sql SQL string
   */
  private void appendQuery(final String sql) {
    SqlUtil.appendQuery(this.query, sql);
  }

  /**
   * Adds parameters.<br>
   * <ul>
   * <li>Multiple values can be passed.</li>
   * </ul>
   *
   * @param params Parameters (multiple allowed)
   */
  private void addAllParameters(final Object... params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    for (final Object param : params) {
      this.parameters.add(param);
    }
  }

  /**
   * Adds a parameter list.
   *
   * @param params Parameter list
   */
  private void addAllParameters(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    this.parameters.addAll(params);
  }

  /**
   * Gets the SQL string.
   *
   * @return the SQL string
   */
  String getQuery() {
    return this.query.toString();
  }

  /**
   * Gets the parameters.
   *
   * @return the parameters
   */
  List<Object> getParameters() {
    return this.parameters;
  }

  /**
   * Appends an SQL builder.<br>
   * <ul>
   * <li>Inherits SQL and parameters.</li>
   * </ul>
   *
   * @param sb SQL builder
   */
  public void addSqlBuilder(final SqlBuilder sb) {
    // Append SQL
    appendQuery(sb.getQuery());
    // Append parameters
    addAllParameters(sb.getParameters());
  }

  /**
   * Appends SQL and parameters.<br>
   * <ul>
   * <li>Parameter arguments are optional and can be passed as single or multiple values.</li>
   * </ul>
   * <pre>
   * [Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
   * [Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
   * [Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
   * </pre>
   * @param sql    SQL
   * @param params Parameters (multiple allowed) (optional)
   * @return this instance
   */
  public SqlBuilder addQuery(final String sql, final Object... params) {
    // Append SQL
    appendQuery(sql);
    // Append parameters
    addAllParameters(params);
    return this;
  }

  /**
   * Appends parameters.<br>
   * <ul>
   * <li>Multiple values can be passed.</li>
   * </ul>
   * <pre>
   * [Example 1] <code>sqlBuilder.addParams(userId);</code>
   * [Example 2] <code>sqlBuilder.addParams(birthDtFrom, birthDtTo);</code>
   * </pre>
   *
   * @param params Parameters (multiple allowed)
   * @return this instance
   */
  public SqlBuilder addParams(final Object... params) {
    addAllParameters(params);
    return this;
  }

  /**
   * Appends comma-separated SQL bind placeholders.<br>
   * <ul>
   * <li>Appends SQL bind placeholders "?" to SQL, comma-separated for the number of elements in the list.</li>
   * <li>If the list contains 3 elements, "?,?,?" is appended to SQL.</li>
   * <li>The values in the list are added as SQL bind variable parameters.</li>
   * <li>Intended for use in IN clauses with a variable number of bind placeholders.</li>
   * </ul>
   * <pre>
   * [Example] <code>sqlBuilder.addQuery("AND type_cs IN (").addListInBind(list).addQuery(")");</code>
   * </pre>
   *
   * @param params Parameter list
   * @return this instance
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
   * Appends SQL and parameter only if the parameter is not <code>null</code> and not blank.<br>
   * <ul>
   * <li>Appends SQL and parameter only if the parameter is not <code>null</code> and not blank.</li>
   * <li>Other specifications are the same as <code>#addQuery(String, Object...)</code>.</li>
   * <li>Basically, use the shortcut method <code>#addQnotB(String, Object)</code>.</li>
   * </ul>
   * <pre>In the example below, SQL is appended only if <code>userId</code> is not <code>null</code> or blank.
   * [Example] <code>sqlBuilder.addQueryIfNotBlankParameter("AND user_id = ? ", userId);</code>
   * </pre>
   * @see #addQuery(String, Object...)
   * @see #addQnotB(String, Object)
   * @param sql SQL
   * @param param Parameter (single only)
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
   * Appends SQL and parameter only if the parameter is not <code>null</code> and not blank.<br>
   * <ul>
   * <li>Shortcut for <code>#addQueryIfNotBlankParameter(String, Object)</code>.</li>
   * <li>Appends SQL and parameter only if the parameter is not <code>null</code> and not blank.</li>
   * <li>Other specifications are the same as <code>#addQuery(String, Object...)</code>.</li>
   * </ul>
   * <pre>In the example below, SQL is appended only if <code>userId</code> is not <code>null</code> or blank.
   * [Example] <code>sqlBuilder.addQnotB("AND user_id = ? ", userId);</code>
   * </pre>
   *
   * @see #addQueryIfNotBlankParameter(String, Object)
   * @param sql SQL
   * @param param Parameter (single only)
   * @return this instance
   */
  @SuppressWarnings("all")
  public SqlBuilder addQnotB(final String sql, final Object param) {
    return addQueryIfNotBlankParameter(sql, param);
  }

  /**
   * Deletes characters from the end of the SQL string.
   * <ul>
   * <li>Deletes the last character (1 character) from the SQL string.</li>
   * </ul>
   * <pre>[Example]
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(",");
   * }
   * // Delete last comma
   * sb.deleteLastChar();
   * </code></pre>
   * 
   * @return this instance
   */
  public SqlBuilder delLastChar() {
    ValUtil.deleteLastChar(this.query);
    return this;
  }
  
  /**
   * Deletes characters from the end of the SQL string.
   * <ul>
   * <li>Deletes the specified number of characters from the end of the SQL string.</li>
   * </ul>
   * <pre>[Example]
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(" AND ");
   * }
   * // Delete last AND
   * sb.delLastChar(4);
   * </code></pre>
   * 
   * @param deleteCharCount Number of characters to delete
   * @return this instance
   */
  public SqlBuilder delLastChar(final int deleteCharCount) {
    ValUtil.deleteLastChar(this.query, deleteCharCount);
    return this;
  }
  
  /**
   * Gets the SQL string length.
   *
   * @return the SQL string length
   */
  public int length() {
    return this.query.length();
  }

  /**
   * Returns a string for logging.
   */
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    try {
      sb.append("{\"").append(this.query.toString()).append("\"<-");
      sb.append(LogUtil.join(this.parameters)).append("}");
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }
}
