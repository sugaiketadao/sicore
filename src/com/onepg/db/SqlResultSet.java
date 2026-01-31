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
 * <li>A wrapper class for the result set <code>ResultSet</code> that provides an iterator and handles closing of the statement and result set.</li>
 * <li>By using this class, data can only be retrieved through the iterator.</li>
 * <li>Declare in a try clause (try-with-resources statement).</li>
 * <li>The physical field names in the row map retrieved from the iterator of this class are in lowercase English letters. (<code>IoItems</code> keys)</li>
 * </ul>
 * 
 * @see SqlUtil#select(java.sql.Connection, SqlBuilder)
 */
public final class SqlResultSet implements Iterable<IoItems>, AutoCloseable {

  /** Statement. */
  private final PreparedStatement stmt;
  /** Result set. */
  private final ResultSet rset;
  /** Database column name and class type map. */
  private final Map<String, ItemClsType> nameClsMap;
  /** Connection serial code. */
  private final String serialCode;

  /** Number of rows read. */
  private int readedCount = 0;
  /** Last row read flag. */
  private boolean readedEndRowFlag = false;

  /**
   * Constructor.
   *
   * @param stmt statement
   * @param rset result set
   * @param nameClsMap database column name and class type map
   * @param serialCode connection serial code
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
   * Closes the result set and statement.<br>
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
   * Checks if data exists (matches database retrieval conditions).<br>
   * <ul>
   * <li>In DB2, #isBeforeFirst (with TYPE_FORWARD_ONLY) causes an error.</li>
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
   * Retrieves the number of rows read (not the count matching database retrieval conditions).<br>
   * <ul>
   * <li>Returns the count read by the iterator.</li>
   * </ul>
   *
   * @return the number of rows read
   */
  public int getReadedCount() {
    return this.readedCount;
  }

  /**
   * Determines if the last row has been read.
   *
   * @return <code>true</code> if the last row has been read
   */
  public boolean isReadedEndRow() {
    return this.readedEndRowFlag;
  }

  /**
   * Retrieves database column names.
   *
   * @return the database column name string array
   */
  public String[] getItemNames() {
    final String[] itemNames = new String[this.nameClsMap.size()];
    int idx = 0;
    for (final String name : this.nameClsMap.keySet()) {
      itemNames[idx++] = name;
    }
    return itemNames;
  }

  /**
   * Result row iterator class.
   * <ul>
   * <li>Iterator class for the result set <code>ResultSet</code>.</li>
   * </ul>
   */
  public final class SqlResultRowIterator implements Iterator<IoItems> {

    /** Next row exists flag. */
    private boolean hasNextRow = false;
    /** Next row checked flag. */
    private boolean hasNextChecked = false;

    /**
     * Constructor.
     */
    private SqlResultRowIterator() {
        super();
    }

    /**
     * Checks if the next row exists.<br>
     * <ul>
     * <li>Closes the result set and statement if the next row does not exist, in case the try clause was not used.</li>
     * <li>Does not recheck on consecutive hasNext() calls.</li>
     * </ul>
     *
     * @return <code>true</code> if the next row exists
     */
    @Override
    public boolean hasNext() {
      // Do not recheck if already checked
      if (hasNextChecked) {
          return this.hasNextRow;
      }

      // Check if next row exists
      try {
          // <code>ResultSet#isLast()</code> cannot be used with TYPE_FORWARD_ONLY.
          this.hasNextRow = rset.next();
          this.hasNextChecked = true; // Confirmation completed flag
      } catch (SQLException e) {
          throw new RuntimeException("Exception error occurred while checking next record in result set. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)), e);
      }

      if (!this.hasNextRow) {
          // Turn on last row read flag
          readedEndRowFlag = true;
          // Close the result set and statement
          close();
      }

      return this.hasNextRow;
    }

    /**
     * Retrieves the next row.
     *
     * @return the row map
     */
    @Override
    public IoItems next() {
      if (!hasNext()) {
          throw new RuntimeException("Next record does not exist. " + LogUtil.joinKeyVal("serialCode",
              serialCode, "readedCount", String.valueOf(readedCount)));
      }

      // Retrieve result set row map
      final IoItems retMap = SqlUtil.createIoItemsFromResultSet(rset, nameClsMap);
      readedCount++;

      // Recheck is necessary
      this.hasNextChecked = false;

      return retMap;
    }
  }
}
