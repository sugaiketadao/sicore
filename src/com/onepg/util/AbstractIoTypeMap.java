package com.onepg.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * I/O variable type map base class.<br>
 * <ul>
 * <li>A map class with typed value retrieval and storage methods.</li>
 * <li>Internally stores values as strings.</li>
 * <li>Preserves insertion order of values.</li>
 * <li>Extends Map&lt;String, String&gt; for versatility.</li>
 * <li>Can be made read-only via constructor parameter.</li>
 * <li>Basic rules and restrictions:
 * <ul>
 * <li>Keys are limited to lowercase letters, digits, underscores, hyphens, and dots only.<br>
 * (Only characters valid in JSON, and lowercase letters to eliminate DBMS differences)</li>
 * <li>In principle, value retrieval methods do not return <code>null</code>.</li>
 * <li>To retrieve <code>null</code>, use an explicit method [e.g.] <code>#getStringNullable(String)</code>.</li>
 * <li>In principle, retrieving a value with a non-existent key causes a runtime error.</li>
 * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>,
 * or use an explicit method that specifies a default value [e.g.] <code>#getStringOrDefault(String, String)</code>.</li>
 * <li>Storing with an already existing key causes a runtime error.</li>
 * <li>To store with a potentially existing key, use an explicit method [e.g.] <code>#putForce(String, String)</code>.</li>
 * <li>To explicitly store <code>null</code>, also use an explicit method [e.g.] <code>#putNull(String)</code>.</li>
 * <li>Unlike regular maps, results of <code>#keySet()</code>, <code>#entrySet()</code>, and <code>#values()</code>
 * are read-only.<br>
 * (Keys are managed separately internally, and removing from results would break consistency)</li>
 * <li>Timestamps are stored with up to 6 fractional digits.</li>
 * </ul>
 * </ul>
 */
public abstract class AbstractIoTypeMap implements Map<String, String> {

  /** Value storage map. */
  private final Map<String, String> valMap;
  /** All key set (used for key existence check including keys outside value storage map). */
  private final Set<String> allKey;

  /** Date-time formatter: date for I/O (SQL: YYYYMMDD). */
  static final DateTimeFormatter DTF_IO_DATE = DateTimeFormatter.ofPattern("uuuuMMdd")
      .withResolverStyle(ResolverStyle.STRICT);
  /** Date-time formatter: timestamp for I/O, also URL-friendly (SQL: YYYYMMDD"T"HH24MISSFF6). */
  static final DateTimeFormatter DTF_IO_TIMESTAMP =
      DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSSSSS").withResolverStyle(ResolverStyle.STRICT);

  /**
   * Constructor.
   */
  AbstractIoTypeMap() {
    super();
    this.valMap = new LinkedHashMap<>();
    this.allKey = new LinkedHashSet<>();
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since contents held by this class are immutable objects (<code>String</code>), this is effectively a deep copy.</li>
   * </ul>
   *
   * @param srcMap source map
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap) {
    this(srcMap, false);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since contents held by this class are immutable objects (<code>String</code>), this is effectively a deep copy.</li>
   * </ul>
   *
   * @param srcMap source map
   * @param readOnly <code>true</code> to create a read-only map
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super();

    if (ValUtil.isNull(srcMap)) {
      throw new RuntimeException("Source map is required. ");
    }

    if (readOnly) {
      // 自クラスのインスタンスにコピーしてから読み取り専用にコピーする。
      final AbstractIoTypeMap tmp = new AbstractIoTypeMap() {};
      tmp.putAll(srcMap);
      this.valMap = Map.copyOf(tmp.valMap);
      this.allKey = Set.copyOf(tmp.allKey);
      return;
    }

    this.valMap = new LinkedHashMap<>();
    this.allKey = new LinkedHashSet<>();
    putAll(srcMap);
  }

  /**
   * Gets value storage map.
   *
   * @return the value storage map
   */
  protected final Map<String, String> getValMap() {
    return this.valMap;
  }

  /**
   * Gets all key set.
   *
   * @return the all key set
   */
  protected final Set<String> allKeySet() {
    return this.allKey;
  }

  /**
   * Validates key.
   *
   * @param key key
   */
  protected final void validateKey(final String key) {
    ValUtil.validateIoKey(key);
  }

  /**
   * Validates key for retrieval.
   *
   * @param key key
   */
  private final void validateKeyForGet(final String key) {
    // Check key existence in value storage map
    if (!this.valMap.containsKey(key)) {
      throw new RuntimeException("Key does not exist. " + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * Validates key for storage.
   *
   * @param key key
   * @param canOverwrite overwrite allowed
   */
  private final void validateKeyForPut(final String key, final boolean canOverwrite) {
    // Key validity check
    validateKey(key);

    final boolean isExists = this.valMap.containsKey(key);
    if (!canOverwrite) {
      // Check key does not exist in value storage map
      if (isExists) {
        throw new RuntimeException("Key already exists. " + LogUtil.joinKeyVal("key", key));
      }
    }

    // Check key existence in other formats
    if (!isExists && this.allKey.contains(key)) {
      throw new RuntimeException("Key already exists as a value in another format. "
                                + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * Validates key and retrieves value.
   *
   * @param key key
   * @return the value
   */
  protected final String getVal(final String key) {
    // Validate key
    validateKeyForGet(key);
    return this.valMap.get(key);
  }

  /**
   * Validates key and stores value.
   *
   * @param key key
   * @param value value
   * @param canOverwrite overwrite allowed
   * @return the previously stored value
   */
  protected final String putVal(final String key, final String value, final boolean canOverwrite) {
    // Validate key
    validateKeyForPut(key, canOverwrite);
    // Store in all key set
    this.allKey.add(key);
    return this.valMap.put(key, value);
  }

  /**
   * Retrieves string.<br>
   * <ul>
   * <li>Use typed value retrieval methods [e.g.] <code>#getString(String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the string
   */
  @Override
  @Deprecated
  public final String get(final Object key) {
    return getString((String) key);
  }

  /**
   * Retrieves string.<br>
   * <ul>
   * <li>Use typed value retrieval methods [e.g.] <code>#getStringNullableOrDefault(String, String)</code>.</li>
   * <li>Returns the default value parameter if key does not exist.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param notExistsValue value to return if key does not exist
   * @return the string
   */
  @Override
  @Deprecated
  public final String getOrDefault(final Object key, final String notExistsValue) {
    return getStringNullableOrDefault((String) key, notExistsValue);
  }

  /**
   * Retrieves string.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Returns empty string if stored value is <code>null</code>. (<code>null</code> is not returned)</li>
   * <li>To retrieve <code>null</code>, use <code>#getStringNullable(String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the string
   */
  public final String getString(final String key) {
    return ValUtil.nvl(getVal(key));
  }

  /**
   * Retrieves string.<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Returns empty string if stored value is <code>null</code>. (<code>null</code> is not returned)</li>
   * <li>To retrieve <code>null</code>, use <code>#getStringNullableOrDefault(String, String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param notExistsValue value to return if key does not exist
   * @return the string
   */
  public final String getStringOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getString(key);
  }

  /**
   * Retrieves string (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the string (nullable)
   */
  public final String getStringNullable(final String key) {
    return getVal(key);
  }

  /**
   * Retrieves string (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the string (nullable)
   */
  public final String getStringNullableOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getStringNullable(key);
  }

  /**
   * Retrieves numeric value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Returns zero if stored value is <code>null</code>. (<code>null</code> is not returned)</li>
   * <li>To retrieve <code>null</code>, use <code>#getBigDecimalNullable(String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the numeric value
   */
  public final BigDecimal getBigDecimal(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(val);
    } catch (final NumberFormatException e) {
      throw new RuntimeException("Invalid numeric value. " + LogUtil.joinKeyVal("key", key, "value", val), e);
    }
  }

  /**
   * Retrieves numeric value.<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Returns zero if stored value is <code>null</code>. (<code>null</code> is not returned)</li>
   * <li>To retrieve <code>null</code>, use <code>#getBigDecimalNullableOrDefault(String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param notExistsValue value to return if key does not exist
   * @return the numeric value
   */
  public final BigDecimal getBigDecimalOrDefault(final String key,
      final BigDecimal notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBigDecimal(key);
  }

  /**
   * Retrieves numeric value (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the numeric value (nullable)
   */
  public final BigDecimal getBigDecimalNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    return new BigDecimal(val);
  }

  /**
   * Retrieves numeric value (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the numeric value (nullable)
   */
  public final BigDecimal getBigDecimalNullableOrDefault(final String key,
      final BigDecimal notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBigDecimalNullable(key);
  }

  /**
   * Retrieves int value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Returns zero if stored value is <code>null</code>.</li>
   * <li>Throws exception if value is outside int range.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the int value
   */
  public final int getInt(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.intValueExact();
  }

  /**
   * Retrieves int value.<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Returns zero if stored value is <code>null</code>.</li>
   * <li>Throws exception if value is outside int range.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param notExistsValue value to return if key does not exist
   * @return the int value
   */
  public final int getIntOrDefault(final String key, final int notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getInt(key);
  }

  /**
   * Retrieves long value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Returns zero if stored value is <code>null</code>.</li>
   * <li>Throws exception if value is outside long range.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the long value
   */
  public final long getLong(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.longValueExact();
  }

  /**
   * Retrieves long value.<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Returns zero if stored value is <code>null</code>.</li>
   * <li>Throws exception if value is outside long range.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param notExistsValue value to return if key does not exist
   * @return the long value
   */
  public final long getLongOrDefault(final String key, final long notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getLong(key);
  }

  /**
   * Retrieves date (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Throws exception if date conversion fails.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the date (nullable)
   */
  public final LocalDate getDateNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    final LocalDate ld = LocalDate.parse(val, DTF_IO_DATE);
    return ld;
  }

  /**
   * Retrieves date (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Throws exception if date conversion fails.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the date (nullable)
   */
  public final LocalDate getDateNullableOrDefault(final String key,
      final LocalDate notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getDateNullable(key);
  }

  /**
   * Retrieves date-time (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Throws exception if date-time conversion fails.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the date-time (nullable)
   */
  public final LocalDateTime getDateTimeNullable(final String key) {
    final String val = getVal(key);
    if (ValUtil.isBlank(val)) {
      return null;
    }
    final LocalDateTime ldt = LocalDateTime.parse(val, DTF_IO_TIMESTAMP);
    return ldt;
  }

  /**
   * Retrieves date-time (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Throws exception if date-time conversion fails.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the date-time (nullable)
   */
  public final LocalDateTime getDateTimeNullableOrDefault(final String key,
      final LocalDateTime notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getDateTimeNullable(key);
  }

  /**
   * Retrieves SQL date (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Throws exception if date conversion fails.</li>
   * <li>Intended for use as SQL bind value.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the SQL date (nullable)
   */
  public final java.sql.Date getSqlDateNullable(final String key) {
    final LocalDate ld = getDateNullable(key);
    if (ValUtil.isNull(ld)) {
      return null;
    }
    return java.sql.Date.valueOf(ld);
  }

  /**
   * Retrieves SQL date (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Throws exception if date conversion fails.</li>
   * <li>Intended for use as SQL bind value.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the SQL date (nullable)
   */
  public final java.sql.Date getSqlDateNullableOrDefault(final String key,
      final java.sql.Date notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getSqlDateNullable(key);
  }

  /**
   * Retrieves SQL timestamp (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Throws exception if timestamp conversion fails.</li>
   * <li>Intended for use as SQL bind value.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the SQL timestamp (nullable)
   */
  public final java.sql.Timestamp getSqlTimestampNullable(final String key) {
    final LocalDateTime ldt = getDateTimeNullable(key);
    if (ValUtil.isNull(ldt)) {
      return null;
    }
    return java.sql.Timestamp.valueOf(ldt);
  }

  /**
   * Retrieves SQL timestamp (nullable).<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Throws exception if timestamp conversion fails.</li>
   * <li>Intended for use as SQL bind value.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the SQL timestamp (nullable)
   */
  public final java.sql.Timestamp getSqlTimestampNullableOrDefault(final String key,
      final java.sql.Timestamp notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getSqlTimestampNullable(key);
  }

  /**
   * Retrieves boolean value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>To retrieve a value with a potentially non-existent key, check existence beforehand with <code>#containsKey(Object)</code>.</li>
   * <li>Boolean evaluation follows <code>ValUtil.isTrue(String)</code>.</li>
   * </ul>
   *
   * @see ValUtil#isTrue(String)
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the boolean value
   */
  public final boolean getBoolean(final String key) {
    final String val = getStringNullable(key);
    final boolean ret = ValUtil.isTrue(val);
    return ret;
  }

  /**
   * Retrieves boolean value.<br>
   * <ul>
   * <li>Returns the default value parameter if key does not exist.</li>
   * <li>Value evaluation follows <code>ValUtil.isTrue(String)</code>.</li>
   * </ul>
   *
   * @param key            key
   * @param notExistsValue value to return if key does not exist
   * @return the boolean value
   */
  public final boolean getBooleanOrDefault(final String key, final boolean notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getBoolean(key);
  }

  /**
   * Stores <code>null</code>.
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the previously stored string
   */
  public final String putNull(final String key) {
    return putVal(key, (String) null, false);
  }

  /**
   * Stores <code>null</code> (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the previously stored string
   */
  public final String putNullForce(final String key) {
    return putVal(key, (String) null, true);
  }

  /**
   * Stores string.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, String)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value value
   * @return the previously stored string
   */
  @Override
  public final String put(final String key, final String value) {
    return putVal(key, value, false);
  }

  /**
   * Stores numeric value.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, BigDecimal)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value numeric value
   * @return the previously stored string
   */
  public final String put(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    return putVal(key, value.toPlainString(), false);
  }

  /**
   * Stores int value.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, int)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value int value
   * @return the previously stored string
   */
  public final String put(final String key, final int value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * Stores long value.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, long)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value long value
   * @return the previously stored string
   */
  public final String put(final String key, final long value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * Stores date.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, LocalDate)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date
   * @return the previously stored string
   */
  public final String put(final String key, final LocalDate value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final String s = value.format(DTF_IO_DATE);
    return putVal(key, s, false);
  }

  /**
   * Stores date-time.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, LocalDateTime)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date-time
   * @return the previously stored string
   */
  public final String put(final String key, final LocalDateTime value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final String s = value.format(DTF_IO_TIMESTAMP);
    return putVal(key, s, false);
  }

  /**
   * Stores UTIL date.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, java.util.Date)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date (including <code>java.sql.Date</code>)
   * @return the previously stored string
   */
  public final String put(final String key, final java.util.Date value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final LocalDate ld = ValUtil.dateToLocalDate(value);
    return put(key, ld);
  }

  /**
   * Stores SQL timestamp.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, java.sql.Timestamp)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value timestamp
   * @return the previously stored string
   */
  public final String put(final String key, final java.sql.Timestamp value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    final LocalDateTime ldt = value.toLocalDateTime();
    return put(key, ldt);
  }

  /**
   * Stores boolean value.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>To store with a potentially existing key, use <code>#putForce(String, boolean)</code>.</li>
   * </ul>
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value boolean value
   * @return the previously stored string
   */
  public final String put(final String key, final boolean value) {
    return put(key, Boolean.toString(value));
  }

  /**
   * Stores string (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value value
   * @return the previously stored string
   */
  public final String putForce(final String key, final String value) {
    return putVal(key, value, true);
  }

  /**
   * Stores numeric value (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value numeric value
   * @return the previously stored string
   */
  public final String putForce(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    return putVal(key, value.toPlainString(), true);
  }

  /**
   * Stores int value (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value int value
   * @return the previously stored string
   */
  public final String putForce(final String key, final int value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * Stores long value (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value long value
   * @return the previously stored string
   */
  public final String putForce(final String key, final long value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * Stores date (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date
   * @return the previously stored string
   */
  public final String putForce(final String key, final LocalDate value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final String s = value.format(DTF_IO_DATE);
    return putVal(key, s, true);
  }


  /**
   * Stores date-time (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date-time
   * @return the previously stored string
   */
  public final String putForce(final String key, final LocalDateTime value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final String s = value.format(DTF_IO_TIMESTAMP);
    return putVal(key, s, true);
  }

  /**
   * Stores UTIL date (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value date (including <code>java.sql.Date</code>)
   * @return the previously stored string
   */
  public final String putForce(final String key, final java.util.Date value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final LocalDate ld = ValUtil.dateToLocalDate(value);
    return putForce(key, ld);
  }

  /**
   * Stores SQL timestamp (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value timestamp
   * @return the previously stored string
   */
  public final String putForce(final String key, final java.sql.Timestamp value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    final LocalDateTime ldt = value.toLocalDateTime();
    return putForce(key, ldt);
  }

  /**
   * Stores boolean value (overwrite allowed).
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @param value boolean value
   * @return the previously stored string
   */
  public final String putForce(final String key, final boolean value) {
    return putForce(key, Boolean.toString(value));
  }

  /**
   * Stores all values.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If potentially existing keys are included, use <code>#putAllForce(Map)</code>.</li>
   * </ul>
   *
   * @param map map
   */
  @Override
  public final void putAll(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, false);
  }

  /**
   * Stores all values (overwrite allowed).
   *
   * @param map map
   */
  public final void putAllForce(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, true);
  }

  /**
   * Stores map.<br>
   * <ul>
   * <li>Since contents are immutable objects (<code>String</code>), this is effectively a deep copy.</li>
   * </ul>
   *
   * @param srcMap source map
   * @param canOverwrite overwrite allowed
   */
  private final void putAllByMap(final Map<? extends String, ? extends String> srcMap,
      final boolean canOverwrite) {
    if (ValUtil.isNull(srcMap)) {
      return;
    }

    for (final Entry<? extends String, ? extends String> ent : srcMap.entrySet()) {
      putVal(ent.getKey(), ent.getValue(), canOverwrite);
    }
  }

  /**
   * Gets map size.
   *
   * @return the map size
   */
  @Override
  public final int size() {
    return this.valMap.size();
  }

  /**
   * Checks if map is empty.
   *
   * @return <code>true</code> if empty
   */
  @Override
  public final boolean isEmpty() {
    return this.valMap.isEmpty();
  }

  /**
   * Checks if key exists in map.
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return <code>true</code> if exists
   */
  @Override
  public final boolean containsKey(final Object key) {
    return this.valMap.containsKey((String) key);
  }

  /**
   * Checks if value exists in map.
   *
   * @param value value
   * @return <code>true</code> if exists
   */
  @Override
  public final boolean containsValue(final Object value) {
    return this.valMap.containsValue((String) value);
  }

  /**
   * Removes value from map.
   *
   * @param key key (lowercase letters, digits, underscores, hyphens, dots only)
   * @return the removed value
   */
  @Override
  public final String remove(final Object key) {
    this.allKey.remove((String) key);
    return this.valMap.remove((String) key);
  }

  /**
   * Clears map.
   */
  @Override
  public final void clear() {
    throw new UnsupportedOperationException("Clear method is not available for this class. Create a new instance instead.");
  }

  /**
   * Gets key set.<br>
   * <ul>
   * <li>Read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the key set
   */
  @Override
  public final Set<String> keySet() {
    return Collections.unmodifiableSet(this.valMap.keySet());
  }

  /**
   * Gets value collection.<br>
   * <ul>
   * <li>Read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the value collection
   */
  @Override
  public final Collection<String> values() {
    return Collections.unmodifiableCollection(this.valMap.values());
  }

  /**
   * Gets entry set.<br>
   * <ul>
   * <li>Read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the entry set
   */
  @Override
  public final Set<Entry<String, String>> entrySet() {
    return Collections.unmodifiableSet(this.valMap.entrySet());
  }
}
