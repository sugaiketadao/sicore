package com.onepg.db;

import com.onepg.db.SqlUtil.ItemClsType;
import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * SQL result set wrapper class.
 *
 * <ul>
 * <li>A wrapper class for <code>ResultSet</code> that provides an iterator and handles closing of statement and result set.</li>
 * <li>Through this class, data can only be retrieved via the iterator.</li>
 * <li>Declare in try clause (try-with-resources statement).</li>
 * <li>The column physical names in the row map obtained from this class's iterator are lowercase letters. (Keys of <code>IoItems</code>)</li>
 * </ul>
 * 
 * @see SqlUtil#select(java.sql.Connection, SqlBuilder)
 */
public final class SqlResultSet implements Iterable<IoItems>, AutoCloseable {

  /** Statement. */
  private final PreparedStatement stmt;
  /** Result set. */
  private final ResultSet rset;
  /** Database column name to class type map. */
  private final Map<String, ItemClsType> nameClsMap;
  /** Connection serial code. */
  private final String serialCode;

  /** Number of rows read. */
  private int readedCount = 0;
  /** Flag indicating whether the last row has been read. */
  private boolean readedEndRowFlag = false;

  /**
   * Constructor.
   *
   * @param stmt Statement
   * @param rset Result set
   * @param nameClsMap Database column name to class type map
   * @param serialCode Connection serial code
   */
  SqlResultSet(final PreparedStatement stmt, final ResultSet rset,
      final Map<String, ItemClsType> nameClsMap, final String serialCode) throws SQLException {
    super();
    this.rset = rset;
    this.stmt = stmt;
    this.nameClsMap = nameClsMap;
    this.serialCode = serialCode;
  }

  /**
   * Creates an iterator.
   *
   * @return the result row iterator
   */
  @Override
  public Iterator<IoItems> iterator() {
    return new SqlResultRowIterator();
  }

  /**
   * Closes resources.<br>
   * <ul>
   * <li>Closes the result set and statement.</li>
   * </ul>
   *
   */
  @Override
  public void close() {
    closeResultSet();
    closeStatement();
  }

  /**
   * Closes the result set.
   */
  private void closeResultSet() {
    try {
      if (this.rset.isClosed()) {
        return;
      }
    } catch (SQLException ignore) {
      // No processing
    }
    try {
      this.rset.close();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while closing the result set. "
                              + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * Closes the statement.
   */
  private void closeStatement() {
    try {
      if (this.stmt.isClosed()) {
        return;
      }
    } catch (SQLException ignore) {
      // No processing
    }
    try {
      this.stmt.close();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while closing the statement. "
                              + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * Checks if data exists (matches found for database extraction criteria).<br>
   * <ul>
   * <li>On DB2, <code>#isBeforeFirst</code> (with TYPE_FORWARD_ONLY) causes an error.</li>
   * </ul>
   *
   * @return <code>true</code> if data was retrieved
   */
  public boolean isExists() {
    try {
      return this.rset.isBeforeFirst();
    } catch (SQLException e) {
      throw new RuntimeException("Exception error occurred while checking data existence in result set. "
                                + LogUtil.joinKeyVal("serialCode", serialCode), e);
    }
  }

  /**
   * Gets the number of rows read (not the number of rows matching database extraction criteria).<br>
   * <ul>
   * <li>Returns the number of rows read via the iterator.</li>
   * </ul>
   *
   * @return the number of rows read
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * Checks if the last row has been read.
   *
   * @return <code>true</code> if the last row has been read
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * Result row iterator class.
   * <ul>
   * <li>An iterator class for <code>ResultSet</code>.</li>
   * </ul>
   */
  public final class SqlResultRowIterator implements Iterator<IoItems> {

    /** Flag indicating if next row exists. */
    private boolean hasNextRow = false;
    /** Flag indicating if next row check has been performed. */
    private boolean hasNextChecked = false;

    /**
     * Constructor.
     */
    private SqlResultRowIterator() {
        super();
    }

    /**
     * Checks for next row.<br>
     * <ul>
     * <li>Closes the result set and statement if no next row exists, in case try clause was not used.</li>
     * <li>Does not recheck on consecutive hasNext() calls.</li>
     * </ul>
     *
     * @return <code>true</code> if next row exists
     */
    @Override
    public boolean hasNext() {
      // Do not recheck if already checked
      if (hasNextChecked) {
          return this.hasNextRow;
      }

      // Check for next row existence
      try {
          // <code>ResultSet#isLast()</code> cannot be used with TYPE_FORWARD_ONLY.
          this.hasNextRow = rset.next();
          this.hasNextChecked = true; // Check completed flag
      } catch (SQLException e) {
          throw new RuntimeException("Exception error occurred while checking next record in result set. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
          // Set last row read flag to ON
          readedEndRowFlag = true;
          // Close the result set and statement
          close();
      }

      return this.hasNextRow;
    }

    /**
     * Gets the next row.
     *
     * @return the row map
     */
    @Override
    public IoItems next() {
      if (!hasNext()) {
          throw new RuntimeException("Next record does not exist. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)));
      }

      // Get the result set row map
      final IoItems retMap = SqlUtil.createIoItemsFromResultSet(rset, nameClsMap);
      readedCount++;

      // Recheck required
      this.hasNextChecked = false;

      return retMap;
    }
  }
}
