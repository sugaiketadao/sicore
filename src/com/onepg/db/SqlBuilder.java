package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL builder.<br>
 * <ul>
 * <li>A class that encapsulates SQL and parameter list required for database access.</li>
 * <li>Has methods that can assemble SQL and set parameters simultaneously.</li>
 * <li>Methods starting with add* return the instance itself, enabling method chaining.</li>
 * <li>Replaces 2 or more consecutive blanks with a single blank when adding SQL.</li>
 * </ul>
 * <pre>
 * [Example 1] <code>sqlBuilder.addQuery("AND a.user_id IS NOT NULL ");</code>
 * [Example 2] <code>sqlBuilder.addQuery("AND a.user_id = ? ", userId);</code>
 * [Example 3] <code>sqlBuilder.addQuery("AND ? <= a.birth_dt AND a.birth_dt <= ?", birthDtFrom, birthDtTo);</code>
 * [Example 4] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId);</code>
 * [Example 5] <code>sqlBuilder.addQnotB("AND a.user_id = ? ", userId).addQnotB("AND a.user_nm LIKE ? ", '%' + name + '%');</code>
 * </pre>
 */
public final class SqlBuilder {

  /** Single-byte blank. */
  private static final String ONEBLANK = " ";

  /** SQL. */
  private final StringBuilder query = new StringBuilder();
  /** Parameters. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * SQL to avoid protocol violation.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com (reference)</a>
   */
  public static final String PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";

  /**
   * Constructor.
   */
  public SqlBuilder() {
    // No processing
  }

  /**
   * Gets the SQL string.
   *
   * @return the SQL string
   */
  String getSql() {
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
   * Gets the SQL string length.
   *
   * @return the SQL string length
   */
  public int length() {
    return this.query.length();
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

    addQuery(sb.getSql());

    final List<Object> params = sb.getParameters();
    for (final Object param : params) {
      addParam(param);
    }
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
    if (ValUtil.isBlank(sql)) {
      return this;
    }

    // If the argument SQL starts with a blank, add a single blank at the beginning
    // However, do not add if existing SQL is empty or ends with a blank
    if (sql.startsWith(ONEBLANK) && this.query.length() > 1
        && this.query.charAt(this.query.length() - 1) != ' ') {
      this.query.append(ONEBLANK);
    }

    // Trim leading and trailing blanks
    // Replace 2 or more consecutive blanks with a single blank
    // However, do not replace blanks enclosed in single quotes
    this.query.append(trimSpaces(sql));

    // If the argument SQL ends with a blank, add a single blank at the end
    if (sql.endsWith(ONEBLANK)) {
      this.query.append(ONEBLANK);
    }

    // Append parameters
    addParam(params);

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
    if (ValUtil.isEmpty(params)) {
      return this;
    }
    for (final Object param : params) {
      this.parameters.add(param);
    }
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
    for (final Object param : params) {
      this.parameters.add(param);
      sb.append("?,");
    }
    ValUtil.deleteLastChar(sb);
    addQuery(sb.toString());
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
   * [Example] <code>sqlBuilder.addQueryWithParamNotBlank("AND user_id = ? ", userId);</code>
   * </pre>
   * @see #addQuery(String, Object...)
   * @see #addQnotB(String, Object)
   * @param sql SQL
   * @param param Parameter (single only)
   * @return this instance
   */
  public SqlBuilder addQueryWithParamNotBlank(final String sql, final Object param) {

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
   * <li>Shortcut for <code>#addQueryWithParamNotBlank(String, Object)</code>.</li>
   * <li>Appends SQL and parameter only if the parameter is not <code>null</code> and not blank.</li>
   * <li>Other specifications are the same as <code>#addQuery(String, Object...)</code>.</li>
   * </ul>
   * <pre>In the example below, SQL is appended only if <code>userId</code> is not <code>null</code> or blank.
   * [Example] <code>sqlBuilder.addQnotB("AND user_id = ? ", userId);</code>
   * </pre>
   *
   * @see #addQueryWithParamNotBlank(String, Object)
   * @param sql SQL
   * @param param Parameter (single only)
   * @return this instance
   */
  @SuppressWarnings("all")
  public SqlBuilder addQnotB(final String sql, final Object param) {
    return addQueryWithParamNotBlank(sql, param);
  }

  /**
   * Deletes the last SQL characters.
   * <ul>
   * <li>Deletes the specified number of characters from the end of SQL.</li>
   * </ul>
   * <pre>[Example]
   * <code>for (final String key : params.keySet()) {
   *   sb.addQuery(key).addQuery("=?", params.get(key)).addQuery(",");
   * </code>
   * // Delete the last comma
   * sb.deleteLastChar(1);
   * }</pre>
   * 
   * @param deleteCharCount Number of characters to delete
   */
  public void deleteLastChar(final int deleteCharCount) {
    if (this.query.length() <= deleteCharCount) {
      return;
    }
    this.query.setLength(this.query.length() - deleteCharCount);
  }

  /**
   * Clears parameters.
   */
  public void clearParameters() {
    this.parameters.clear();
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

  /**
   * Replaces 2 or more consecutive blanks with a single blank.<br>
   * <ul>
   * <li>Trims leading and trailing blanks.</li>
   * <li>Replaces 2 or more consecutive blanks with a single blank.<br>
   * However, blanks enclosed in single quotes are not replaced.</li>
   * </ul>
   * 
   * @param sql SQL
   * @return the result SQL
   */
  private static String trimSpaces(final String sql) {
      if (ValUtil.isBlank(sql)) {
          return ValUtil.BLANK;
      }
      
      final int length = sql.length();
      final char[] chars = sql.toCharArray(); // Use array access for performance
      final StringBuilder ret = new StringBuilder(length);
      
      boolean inSq = false;
      boolean prevSpace = false;
      int beginPos = 0;
      int endPos = length;
      
      // Pre-calculate leading and trailing trim positions
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
