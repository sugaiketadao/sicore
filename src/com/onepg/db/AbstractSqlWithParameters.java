package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL parameters base class.<br>
 * <ul>
 * <li>A class that encapsulates SQL and parameter list required for database access.</li>
 * <li>Replaces 2 or more consecutive blanks with a single blank when adding SQL.</li>
 * </ul>
 */
public abstract class AbstractSqlWithParameters {

  /** Single-byte blank. */
  private static final String ONEBLANK = " ";

  /** SQL. */
  private final StringBuilder query = new StringBuilder();
  /** Parameters. */
  private final List<Object> parameters = new ArrayList<>();
  
  /**
   * SQL to avoid ORACLE protocol violation.<br>
   * <a href="https://support.oracle.com/knowledge/Middleware/2707017_1.html">support.oracle.com (reference)</a>
   */
  public static final String ORACLE_PROTOCOL_ERR_AVOID_SQL =
      " /* protocol error avoidance */ FETCH FIRST 99999999 ROWS ONLY ";

  /**
   * Constructor.
   */
  AbstractSqlWithParameters() {
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
   * Appends SQL.<br>
   * <ul>
   * <li>Appends SQL.</li>
   * <li>Replaces 2 or more consecutive blanks with a single blank when appending.</li>
   * </ul>
   * 
   * @param sql    SQL
   */
  protected void addSql(final String sql) {
    if (ValUtil.isBlank(sql)) {
      return;
    }

    // If the argument SQL starts with a blank, add a single blank at the beginning
    // However, do not add if existing SQL is empty or ends with a blank
    if (sql.startsWith(ONEBLANK) && this.query.length() > 0
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
  }

  /**
   * Appends parameters.<br>
   * <ul>
   * <li>Multiple values can be passed.</li>
   * </ul>
   *
   * @param params Parameters (multiple allowed)
   */
  protected void addParameters(final Object... params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    for (final Object param : params) {
      this.parameters.add(param);
    }
  }

  /**
   * Appends parameters list.
   *
   * @param params Parameter list
   */
  protected void addParameters(final List<Object> params) {
    if (ValUtil.isEmpty(params)) {
      return;
    }
    this.parameters.addAll(params);
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
