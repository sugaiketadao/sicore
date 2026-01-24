package com.onepg.db;

import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL Bean class.<br>
 * <ul>
 * <li>Stores SQL string and bind values.</li>
 * <li>Instances of this class are passed as arguments to SQL execution methods of <code>SqlUtil</code>.</li>
 * </ul>
 */
public class SqlBean {

  /** SQL identifier ID.  */
  protected final String id;
  
  /** SQL string. */
  protected final String query;
  
  /** SQL string builder. */
  protected final StringBuilder queryBuilder;

  /** Bind value list. */
  protected final List<Object> bindValues;
  
  /**
   * Constructor for fixed SQL.
   */
  protected SqlBean(final String query) {
    // Get class package + class name + line number as ID
    this.id = LogUtil.getClassNameAndLineNo(this.getClass());
    this.query = query;
    this.queryBuilder = null;
    this.bindValues = new ArrayList<>();
  }
  
  /**
   * Constructor for fixed SQL with bind values.
   */
  protected SqlBean(final String id, final String query, final List<Object> bindValues) {
    this.id = id;
    this.query = query;
    this.queryBuilder = null;
    this.bindValues = bindValues;
  }
  
  /**
   * Constructor for dynamic SQL.
   */
  protected SqlBean() {
    this.id = ValUtil.BLANK;
    this.query = null;
    this.queryBuilder = new StringBuilder();
    this.bindValues = new ArrayList<>();
  }

  /**
   * Gets SQL identifier ID.
   *
   * @return the SQL identifier ID (<code>null</code> allowed)
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
