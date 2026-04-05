package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.util.List;

/**
 * SQL Bean class.<br>
 * <ul>
 * <li>Stores SQL string and bind values.</li>
 * <li>Instances of this class are passed as arguments to SQL execution methods of <code>SqlUtil</code>.</li>
 * </ul>
 */
public class SqlBean {

  /** SQL-ID.  */
  protected final String id;
  /** SQL string. */
  protected final String query;
  /** SQL string builder. */
  protected final StringBuilder queryBuilder;
  /** Bind value list. */
  protected final List<Object> bindValues;
  
  /**
   * Constructor.
   * @param id SQL-ID
   * @param query SQL string
   * @param queryBuilder SQL string builder
   * @param bindValues Bind value list
   */
  protected SqlBean(final String id, final String query, final StringBuilder queryBuilder, final List<Object> bindValues) {
    this.id = id;
    this.query = query;
    this.queryBuilder = queryBuilder;
    this.bindValues = bindValues;
  }
  
  /**
   * Gets SQL-ID.
   *
   * @return the SQL-ID (<code>null</code> allowed)
   */
  String getId() {
    return this.id;
  }

  /**
   * Gets SQL string.
   *
   * @return the SQL string
   */
  String getQuery() {
    if (!ValUtil.isNull(this.queryBuilder)) {
      return this.queryBuilder.toString();
    } else {
      // Fixed SQL
      return this.query;
    }
  }
  
  /**
   * Gets bind values.
   *
   * @return the bind value list
   */
  List<Object> getBindValues() {
    return this.bindValues;
  }
  
  /**
   * Returns string for logging.
   */
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    try {
      sb.append("{");
      sb.append("\"").append(getQuery()).append("\"");
      sb.append("<-").append(LogUtil.join(getBindValues()));
      sb.append("}");
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }
}
