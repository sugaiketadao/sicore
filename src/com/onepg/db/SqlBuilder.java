package com.onepg.db;

import com.onepg.util.ValUtil;

import java.util.List;

/**
 * SQL builder.<br>
 * <ul>
 * <li>Has methods that can assemble SQL and set parameters simultaneously.</li>
 * <li>Methods starting with add* return the instance itself, enabling method chaining.</li>
 * </ul>
 * <pre>
 * [Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * [Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * [Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * [Example 4] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * [Example 5] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * </pre>
 * 
 * @see AbstractSqlWithParameters
 */
public final class SqlBuilder extends AbstractSqlWithParameters {

  /**
   * Constructor.
   */
  public SqlBuilder() {
    super();
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
    super.addSql(sb.getSql());
    // Append parameters
    super.addParameters(sb.getParameters());
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
    super.addSql(sql);
    // Append parameters
    super.addParameters(params);
    return this;
  }

  /**
   * Appends parameters.<br>
   * <ul>
   * <li>Multiple values can be passed.</li>
   * </ul>
   * <pre>
   * [Example 1] <code>sqlBuilder.addParam(userId);</code>
   * [Example 2] <code>sqlBuilder.addParam(birthDtFrom, birthDtTo);</code>
   * </pre>
   *
   * @param params Parameters (multiple allowed)
   * @return this instance
   */
  public SqlBuilder addParam(final Object... params) {
    super.addParameters(params);
    return this;
  }

  /**
   * Appends comma-separated bind character SQL.<br>
   * <ul>
   * <li>Appends bind character "?" to SQL separated by commas for each element in the list.</li>
   * <li>If the list has 3 elements, "?,?,?" is appended to SQL.</li>
   * <li>Values in the list are added as bind character parameters.</li>
   * <li>Intended for use with IN clauses where the number of bind characters varies.</li>
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

    super.addSql(sb.toString());
    super.addParameters(params);
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
}
