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
 * I/O variable-type map base class.<br>
 * <ul>
 * <li>A map class that provides value retrieval methods and value storage methods for each type.</li>
 * <li>Internally stores values as strings.</li>
 * <li>Preserves the storage order of values.</li>
 * <li>Provides versatility by extending Map&lt;String, String&gt;.</li>
 * <li>Can be made read-only by specifying a constructor argument.</li>
 * <li>Basic rules and restrictions:
 * <ul>
 * <li>Characters that can be used as keys are limited to lowercase letters, numbers, underscores, hyphens, and dots only.<br>
 * (Only characters that can be used in JSON, and letters are unified to lowercase to eliminate DBMS differences)</li>
 * <li>In principle, value retrieval methods do not return <code>null</code>.</li>
 * <li>If you want to retrieve <code>null</code>, use an explicit method such as <code>#getStringNullable(String)</code>.</li>
 * <li>In principle, value retrieval with a non-existent key results in a runtime error.</li>
 * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>,
 * or retrieve using an explicit method such as <code>#getStringOrDefault(String, String)</code>
 * that specifies a return value for non-existent keys.</li>
 * <li>Storing with an already existing key results in a runtime error.</li>
 * <li>When storing a value with a key that may already exist, use an explicit method such as <code>#putForce(String, String)</code>.</li>
 * <li>When explicitly storing <code>null</code>, use an explicit method such as <code>#putNull(String)</code>.</li>
 * <li>Unlike normal maps, the results of <code>#keySet()</code>, <code>#entrySet()</code>, and <code>#values()</code>
 * are read-only.<br>
 * (Keys are managed separately internally, and removal from the results would break consistency)</li>
 * <li>Timestamps are stored with up to 6 decimal places.</li>
 * </ul>
 * </ul>
 */
public abstract class AbstractIoTypeMap implements Map<String, String> {

  /** Value storage map. */
  private final Map<String, String> valMap;
  /** All key set (used to check for keys that exist outside the value storage map). */
  private final Set<String> allKey;

  /** Date-time formatter: Date for I/O (SQL: YYYYMMDD). */
  static final DateTimeFormatter DTF_IO_DATE = DateTimeFormatter.ofPattern("uuuuMMdd")
      .withResolverStyle(ResolverStyle.STRICT);
  /** Date-time formatter: Timestamp for I/O, also optimal for URLs (SQL: YYYYMMDD"T"HH24MISSFF6). */
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
   * <li>Since this class holds immutable objects (<code>String</code>), this essentially performs a deep copy.</li>
   * </ul>
   *
   * @param srcMap the source map
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap) {
    this(srcMap, false);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Since this class holds immutable objects (<code>String</code>), this essentially performs a deep copy.</li>
   * </ul>
   *
   * @param srcMap the source map
   * @param readOnly <code>true</code> to create a read-only map
   */
  AbstractIoTypeMap(final Map<? extends String, ? extends String> srcMap, final boolean readOnly) {
    super();

    if (ValUtil.isNull(srcMap)) {
      throw new RuntimeException("Source map is required. ");
    }

    if (readOnly) {
      // Copy to an instance of this class, then copy to read-only.
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
   * Retrieves the value storage map.
   *
   * @return the value storage map
   */
  protected final Map<String, String> getValMap() {
    return this.valMap;
  }

  /**
   * Retrieves the all key set.
   *
   * @return the all key set
   */
  protected final Set<String> allKeySet() {
    return this.allKey;
  }

  /**
   * Validates the key.
   *
   * @param key the key
   */
  protected final void validateKey(final String key) {
    ValUtil.validateIoKey(key);
  }

  /**
   * Validates the key for retrieval.
   *
   * @param key the key
   */
  private final void validateKeyForGet(final String key) {
    // Check if key exists in value storage map
    if (!this.valMap.containsKey(key)) {
      throw new RuntimeException("Key does not exist. " + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * Validates the key for storage.
   *
   * @param key the key
   * @param canOverwrite whether to allow overwriting
   */
  private final void validateKeyForPut(final String key, final boolean canOverwrite) {
    // Check key validity
    validateKey(key);

    final boolean isExists = this.valMap.containsKey(key);
    if (!canOverwrite) {
      // Check that key does not exist in value storage map
      if (isExists) {
        throw new RuntimeException("Key already exists. " + LogUtil.joinKeyVal("key", key));
      }
    }

    // Check for key existence in other formats
    if (!isExists && this.allKey.contains(key)) {
      throw new RuntimeException("Key already exists as a value in another format. "
                                + LogUtil.joinKeyVal("key", key));
    }
  }

  /**
   * Validates the key and retrieves the value.
   *
   * @param key the key
   * @return the value
   */
  protected final String getVal(final String key) {
    // Validate key
    validateKeyForGet(key);
    return this.valMap.get(key);
  }

  /**
   * Validates the key and stores the value.
   *
   * @param key the key
   * @param value the value
   * @param canOverwrite whether to allow overwriting
   * @return the previous stored value
   */
  protected final String putVal(final String key, final String value, final boolean canOverwrite) {
    // Validate key
    validateKeyForPut(key, canOverwrite);
    // Store in all key set
    this.allKey.add(key);
    return this.valMap.put(key, value);
  }

  /**
   * Retrieves a string.<br>
   * <ul>
   * <li>Please use a type-specific value retrieval method such as <code>#getString(String)</code> explicitly.</li>
   * </ul>
   *
   * @deprecated Please use a type-specific value retrieval method such as <code>#getString(String)</code> explicitly.
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the string
   */
  @Override
  @Deprecated
  public final String get(final Object key) {
    return getString((String) key);
  }

  /**
   * Retrieves a string.<br>
   * <ul>
   * <li>Please use a type-specific value retrieval method such as <code>#getStringNullableOrDefault(String, String)</code> explicitly.</li>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * </ul>
   *
   * @deprecated Please use a type-specific value retrieval method such as <code>#getStringNullableOrDefault(String, String)</code> explicitly.
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param notExistsValue the return value when the key does not exist
   * @return the string
   */
  @Override
  @Deprecated
  public final String getOrDefault(final Object key, final String notExistsValue) {
    return getStringNullableOrDefault((String) key, notExistsValue);
  }

  /**
   * Retrieves a string.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns an empty string. (Does not return <code>null</code>)</li>
   * <li>If you want to retrieve <code>null</code>, retrieve using <code>#getStringNullable(String)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the string
   */
  public final String getString(final String key) {
    return ValUtil.nvl(getVal(key));
  }

  /**
   * Retrieves a string.<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If the stored value is <code>null</code>, returns an empty string. (Does not return <code>null</code>)</li>
   * <li>If you want to retrieve <code>null</code>, retrieve using <code>#getStringNullableOrDefault(String, String)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param notExistsValue the return value when the key does not exist
   * @return the string
   */
  public final String getStringOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getString(key);
  }

  /**
   * Retrieves a string (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the string (nullable)
   */
  public final String getStringNullable(final String key) {
    return getVal(key);
  }

  /**
   * Retrieves a string (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
   * @return the string (nullable)
   */
  public final String getStringNullableOrDefault(final String key, final String notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getStringNullable(key);
  }

  /**
   * Retrieves a numeric value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns zero. (Does not return <code>null</code>)</li>
   * <li>If you want to retrieve <code>null</code>, retrieve using <code>#getBigDecimalNullable(String)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves a numeric value.<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If the stored value is <code>null</code>, returns zero. (Does not return <code>null</code>)</li>
   * <li>If you want to retrieve <code>null</code>, retrieve using <code>#getBigDecimalNullableOrDefault(String)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves a numeric value (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves a numeric value (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves an int value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns zero.</li>
   * <li>If the value is outside the int range, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the int value
   */
  public final int getInt(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.intValueExact();
  }

  /**
   * Retrieves an int value.<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If the stored value is <code>null</code>, returns zero.</li>
   * <li>If the value is outside the int range, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param notExistsValue the return value when the key does not exist
   * @return the int value
   */
  public final int getIntOrDefault(final String key, final int notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getInt(key);
  }

  /**
   * Retrieves a long value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns zero.</li>
   * <li>If the value is outside the long range, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the long value
   */
  public final long getLong(final String key) {
    final BigDecimal val = getBigDecimal(key);
    return val.longValueExact();
  }

  /**
   * Retrieves a long value.<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If the stored value is <code>null</code>, returns zero.</li>
   * <li>If the value is outside the long range, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param notExistsValue the return value when the key does not exist
   * @return the long value
   */
  public final long getLongOrDefault(final String key, final long notExistsValue) {
    if (!this.valMap.containsKey(key)) {
      return notExistsValue;
    }
    return getLong(key);
  }

  /**
   * Retrieves a date (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If date conversion fails, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves a date (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If date conversion fails, throws an exception error.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves a date-time (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If date-time conversion fails, throws an exception error.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves a date-time (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If date-time conversion fails, throws an exception error.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves an SQL date (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If date conversion fails, throws an exception error.</li>
   * <li>Intended to be used as an SQL bind value.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves an SQL date (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If date conversion fails, throws an exception error.</li>
   * <li>Intended to be used as an SQL bind value.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves an SQL timestamp (nullable).<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>If timestamp conversion fails, throws an exception error.</li>
   * <li>Intended to be used as an SQL bind value.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
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
   * Retrieves an SQL timestamp (nullable).<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>If timestamp conversion fails, throws an exception error.</li>
   * <li>Intended to be used as an SQL bind value.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * Retrieves a boolean value.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key results in a runtime error.</li>
   * <li>When retrieving a value with a key that may not exist, check existence in advance using <code>#containsKey(Object)</code>.</li>
   * <li>Boolean value evaluation conforms to <code>ValUtil.isTrue(String)</code>.</li>
   * </ul>
   *
   * @see ValUtil#isTrue(String)
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the boolean value
   */
  public final boolean getBoolean(final String key) {
    final String val = getStringNullable(key);
    final boolean ret = ValUtil.isTrue(val);
    return ret;
  }

  /**
   * Retrieves a boolean value.<br>
   * <ul>
   * <li>If the key does not exist, the non-existent return value from the argument is returned.</li>
   * <li>Value evaluation conforms to <code>ValUtil.isTrue(String)</code>.</li>
   * </ul>
   *
   * @param key            the key
   * @param notExistsValue the return value when the key does not exist
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
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the previously stored string
   */
  public final String putNull(final String key) {
    return putVal(key, (String) null, false);
  }

  /**
   * Stores <code>null</code> (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the previously stored string
   */
  public final String putNullForce(final String key) {
    return putVal(key, (String) null, true);
  }

  /**
   * Stores a string.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, String)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the value
   * @return the previously stored string
   */
  @Override
  public final String put(final String key, final String value) {
    return putVal(key, value, false);
  }

  /**
   * Stores a numeric value.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, BigDecimal)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the numeric value
   * @return the previously stored string
   */
  public final String put(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNull(key);
    }
    return putVal(key, value.toPlainString(), false);
  }

  /**
   * Stores an int value.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, int)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the int value
   * @return the previously stored string
   */
  public final String put(final String key, final int value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * Stores a long value.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, long)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the long value
   * @return the previously stored string
   */
  public final String put(final String key, final long value) {
    return putVal(key, String.valueOf(value), false);
  }

  /**
   * Stores a date.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, LocalDate)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date
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
   * Stores a date-time.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, LocalDateTime)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date-time
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
   * Stores a UTIL date.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, java.util.Date)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date (including <code>java.sql.Date</code>)
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
   * Stores an SQL timestamp.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, java.sql.Timestamp)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the timestamp
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
   * Stores a boolean value.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>When storing a value with a key that may already exist, store using <code>#putForce(String, boolean)</code>.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the boolean value
   * @return the previously stored string
   */
  public final String put(final String key, final boolean value) {
    return put(key, Boolean.toString(value));
  }

  /**
   * Stores a string (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the value
   * @return the previously stored string
   */
  public final String putForce(final String key, final String value) {
    return putVal(key, value, true);
  }

  /**
   * Stores a numeric value (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the numeric value
   * @return the previously stored string
   */
  public final String putForce(final String key, final BigDecimal value) {
    if (ValUtil.isNull(value)) {
      return putNullForce(key);
    }
    return putVal(key, value.toPlainString(), true);
  }

  /**
   * Stores an int value (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the int value
   * @return the previously stored string
   */
  public final String putForce(final String key, final int value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * Stores a long value (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the long value
   * @return the previously stored string
   */
  public final String putForce(final String key, final long value) {
    return putVal(key, String.valueOf(value), true);
  }

  /**
   * Stores a date (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date
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
   * Stores a date-time (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date-time
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
   * Stores a UTIL date (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the date (including <code>java.sql.Date</code>)
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
   * Stores an SQL timestamp (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the timestamp
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
   * Stores a boolean value (allows overwriting).
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param value the boolean value
   * @return the previously stored string
   */
  public final String putForce(final String key, final boolean value) {
    return putForce(key, Boolean.toString(value));
  }

  /**
   * Stores all values.<br>
   * <ul>
   * <li>Storing with an already existing key results in a runtime error.</li>
   * <li>If keys that may already exist are included, use <code>#putAllForce(Map)</code>.</li>
   * </ul>
   *
   * @param map the map
   */
  @Override
  public final void putAll(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, false);
  }

  /**
   * Stores all values (allows overwriting).
   *
   * @param map the map
   */
  public final void putAllForce(final Map<? extends String, ? extends String> map) {
    putAllByMap(map, true);
  }

  /**
   * Stores a map.<br>
   * <ul>
   * <li>Since the content is immutable objects (<code>String</code>), this essentially performs a deep copy.</li>
   * </ul>
   *
   * @param srcMap the source map
   * @param canOverwrite whether to allow overwriting
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
   * Retrieves the map size.
   *
   * @return the map size
   */
  @Override
  public final int size() {
    return this.valMap.size();
  }

  /**
   * Determines if the map is empty.
   *
   * @return <code>true</code> if the map is empty
   */
  @Override
  public final boolean isEmpty() {
    return this.valMap.isEmpty();
  }

  /**
   * Checks if a map key exists.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return <code>true</code> if the key exists
   */
  @Override
  public final boolean containsKey(final Object key) {
    return this.valMap.containsKey((String) key);
  }

  /**
   * Checks if the map contains a value.
   *
   * @param value the value
   * @return <code>true</code> if the value exists
   */
  @Override
  public final boolean containsValue(final Object value) {
    return this.valMap.containsValue((String) value);
  }

  /**
   * Removes a map value.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the removed value
   */
  @Override
  public final String remove(final Object key) {
    this.allKey.remove((String) key);
    return this.valMap.remove((String) key);
  }

  /**
   * Clears the map.<br>
   * <ul>
   * <li>The clear method is prohibited to prevent easy reuse of instances.</li>
   * <li>Please create a new instance instead of clearing.</li>
   * </ul>
   */
  @Override
  public final void clear() {
    throw new UnsupportedOperationException("Clear method is not available for this class. Create a new instance instead.");
  }

  /**
   * Retrieves the key set.<br>
   * <ul>
   * <li>Made read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the key set
   */
  @Override
  public final Set<String> keySet() {
    return Collections.unmodifiableSet(this.valMap.keySet());
  }

  /**
   * Retrieves the value collection.<br>
   * <ul>
   * <li>Made read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the value collection
   */
  @Override
  public final Collection<String> values() {
    return Collections.unmodifiableCollection(this.valMap.values());
  }

  /**
   * Retrieves the entry set.<br>
   * <ul>
   * <li>Made read-only because removal would break internal consistency.</li>
   * </ul>
   *
   * @return the entry set
   */
  @Override
  public final Set<Entry<String, String>> entrySet() {
    return Collections.unmodifiableSet(this.valMap.entrySet());
  }
  
  /**
   * Compares for equality.<br>
   * <ul>
   * <li>Determines equality if the contents are identical.</li>
   * </ul>
   *
   * @param obj the object to compare
   * @return <code>true</code> if the contents are identical
   */
  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (ValUtil.isNull(obj) || getClass() != obj.getClass()) {
        return false;
    }
    final AbstractIoTypeMap other = (AbstractIoTypeMap) obj;
    return this.valMap.equals(other.valMap);
  }

  /**
   * Retrieves the hash code.
   *
   * @return the hash code
   */
  @Override
  public final int hashCode() {
    return this.valMap.hashCode();
  }
}
