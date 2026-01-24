package com.onepg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * I/O map class.<br>
 * <ul>
 * <li>Can hold string lists.</li>
 * <li>Can hold nested variable type maps.</li>
 * <li>Can hold multiple rows of variable type maps as lists.</li>
 * <li>Can hold string array (list) as lists.</li>
 * <li>The above data structures are deep copied during both storage and retrieval.</li>
 * <li>Can input/output JSON.</li>
 * <li>Can input/output URL parameters.</li>
 * <li>Basic rules and limitations conform to <code>AbstractIoTypeMap</code>.</li>
 * 
 * <li>About string lists
 * <ul>
 * <li>Handle with <code>**List</code> methods such as <code>#getList(String)</code>, <code>#putList(String, List)</code>, <code>#containsKeyList(String)</code>,
 * <code>#removeList(String)</code>.</li>
 * <li>String lists are not included in the results of map class methods like <code>#size()</code>, <code>#containsKey(Object)</code>,
 * <code>#keySet()</code>.<br>
 * However, <code>#clear()</code> also clears the stored string lists.</li>
 * <li>Adding elements to a list retrieved by <code>#getList(String)</code> does not affect the list held by this class.</li>
 * <li>Adding elements to the original list stored by <code>#putList(String, List)</code>
 * does not affect the list held by this class.</li>
 * <li>String lists are not included in CSV output.</li>
 * <li>Be careful about string length when using URL parameters containing string lists.</li>
 * <li>Performance may degrade when repeatedly storing and retrieving lists or handling large lists, due to deep copying.</li>
 * </ul>
 * </li>
 * 
 * <li>About nested maps
 * <ul>
 * <li>Handle with <code>**Nest</code> methods such as <code>#getNest(String)</code>, <code>#putNest(String, Map)</code>, <code>#containsKeyNest(String)</code>,
 * <code>#removeNest(String)</code>.</li>
 * <li>Nested maps are not included in the results of map class methods like <code>#size()</code>, <code>#containsKey(Object)</code>,
 * <code>#keySet()</code>.<br>
 * However, <code>#clear()</code> also clears the stored nested maps.</li>
 * <li>Adding elements to a map retrieved by <code>#getNest(String)</code> does not affect the map held by this class.</li>
 * <li>Adding elements to the original map stored by <code>#putNest(String, List)</code>
 * does not affect the map held by this class.</li>
 * <li>Nested maps are not included in CSV output or URL parameter output.</li>
 * <li>Performance may degrade when repeatedly storing and retrieving lists or handling large lists, due to deep copying.</li>
 * </ul>
 * </li>
 * 
 * <li>About multiple rows lists
 * <ul>
 * <li>Handle with <code>**Rows</code> methods such as <code>#getRows(String)</code>, <code>#putRows(String, Collection)</code>,
 * <code>#containsKeyRows(String)</code>, <code>#removeRows(List)</code>.</li>
 * <li>Multiple rows lists are not included in the results of map class methods like <code>#size()</code>, <code>#containsKey(Object)</code>,
 * <code>#keySet()</code>.<br>
 * However, <code>#clear()</code> also clears the stored multiple rows lists.</li>
 * <li>Adding elements to a list retrieved by <code>#getRows(String)</code> does not affect the list held by this class.</li>
 * <li>Adding elements to the original list stored by <code>#putRows(String, Collection)</code>
 * does not affect the list held by this class.</li>
 * <li>Multiple rows lists are not included in CSV output or URL parameter output.</li>
 * <li>Performance may degrade when repeatedly storing and retrieving lists or handling large lists, due to deep copying.</li>
 * </ul>
 * </li>
 * 
 * <li>About array lists
 * <ul>
 * <li>Handle with <code>**Arys</code> methods such as <code>#getArys(String)</code>, <code>#putArys(String, Collection)</code>,
 * <code>#containsKeyArys(String)</code>, <code>#removeArys(List)</code>.</li>
 * <li>Array lists are not included in the results of map class methods like <code>#size()</code>, <code>#containsKey(Object)</code>,
 * <code>#keySet()</code>.<br>
 * However, <code>#clear()</code> also clears the stored array lists.</li>
 * <li>Adding elements to a list retrieved by <code>#getArys(String)</code> does not affect the list held by this class.</li>
 * <li>Adding elements to the original list stored by <code>#putArys(String, Collection)</code>
 * does not affect the list held by this class.</li>
 * <li>Array lists are not included in CSV output or URL parameter output.</li>
 * <li>Performance may degrade when repeatedly storing and retrieving lists or handling large lists, due to deep copying.</li>
 * </ul>
 * </li>
 * 
 * <li>JSON does not support arrays of 4 or more levels (dimensions).
 * <ul>
 * <li>{ [ [ ] ] } is supported. → Array list</li>
 * <li>{ [ { } ] } is supported. → Multiple rows list</li>
 * <li>{ [ [ [ ] ] ] } is not supported.</li>
 * <li>{ [ [ { } ] ] } is not supported.</li>
 * </ul>
 * </li>
 * 
 * <li>About messages
 * <ul>
 * <li>Can hold message ID and target field name, and messages are output together with JSON output.</li>
 * <li>By passing message text as an argument during JSON output, held message texts are also output together.</li>
 * <li><code>{0}, {1}, ...</code> in message text are replaced with strings passed as arguments when adding messages.</li>
 * </ul>
 * </li>
 * 
 * </ul>
 *
 * @see AbstractIoTypeMap
 */
public final class Io extends AbstractIoTypeMap {

  /** String list storage map. */
  private final Map<String, List<String>> listMap = new LinkedHashMap<>();
  /** Nested map storage map. */
  private final Map<String, Io> nestMap = new LinkedHashMap<>();
  /** Multiple rows list storage map. */
  private final Map<String, IoRows> rowsMap = new LinkedHashMap<>();
  /** Array list storage map. */
  private final Map<String, IoArrays> arysMap = new LinkedHashMap<>();

  /** Storage type. */
  private enum StorageType {
    /** String list storage. */
    LIST,
    /** Nested map storage. */
    NEST,
    /** Multiple rows list storage. */
    ROWS,
    /** Array list storage. */
    ARYS;
  }

  /**
   * Constructor.
   */
  public Io() {
    super();
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Deep copies the contents, so the reference to the source map is disconnected.</li>
   * </ul>
   *
   * @param srcMap the source map
   */
  public Io(final Map<? extends String, ? extends String> srcMap) {
    super(srcMap);
  }

  /**
   * Constructor.<br>
   * <ul>
   * <li>Deep copies the contents, so the reference to the source map is disconnected.</li>
   * <li>Messages are also copied.</li>
   * </ul>
   *
   * @param srcMap the source map
   */
  public Io(final Io srcMap) {
    super();

    if (ValUtil.isNull(srcMap)) {
      throw new RuntimeException("Source map is required. ");
    }

    // Deep copies the contents, so the reference to the source map is disconnected.
    putAllByIoMap(srcMap, false);
  }

  /**
   * Validates key on retrieval (by storage type).
   *
   * @param type the storage type
   * @param key the key
   */
  private void validateKeyByTypeForGet(final StorageType type, final String key) {

    if (StorageType.LIST == type) {
      // String list storage map key existence check
      if (!this.listMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in string list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.NEST == type) {
      // Nested map storage map key existence check
      if (!this.nestMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in nested map. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.ROWS == type) {
      // Multiple rows list storage map key existence check
      if (!this.rowsMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in multiple rows list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }

    if (StorageType.ARYS == type) {
      // Array list storage map key existence check
      if (!this.arysMap.containsKey(key)) {
        throw new RuntimeException("Key does not exist in array list. "
                                + LogUtil.joinKeyVal("key", key));
      }
    }
  }

  /**
   * Validates key on storage (by storage type).
   *
   * @param type the storage type
   * @param key the key
   * @param canOverwrite overwrite allowed
   */
  private void validateKeyByTypeForPut(final StorageType type, final String key,
      final boolean canOverwrite) {

    // Key validity check
    validateKey(key);

    // Value storage map key non-existence check
    if (super.getValMap().containsKey(key)) {
      throw new RuntimeException("Key is for value storage map. " + LogUtil.joinKeyVal("key", key));
    }

    if ((StorageType.LIST == type && !canOverwrite) || StorageType.LIST != type) {
      // String list storage map key non-existence check
      if (this.listMap.containsKey(key)) {
        throw new RuntimeException("Already a string list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.NEST == type && !canOverwrite) || StorageType.NEST != type) {
      // Nested map storage map key non-existence check
      if (this.nestMap.containsKey(key)) {
        throw new RuntimeException("Already a nested map key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.ROWS == type && !canOverwrite) || StorageType.ROWS != type) {
      // Multiple rows list storage map key non-existence check
      if (this.rowsMap.containsKey(key)) {
        throw new RuntimeException("Already a multiple rows list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }

    if ((StorageType.ARYS == type && !canOverwrite) || StorageType.ARYS != type) {
      // Array list storage map key non-existence check
      if (this.arysMap.containsKey(key)) {
        throw new RuntimeException("Already an array list key (cannot overwrite). " + LogUtil.joinKeyVal("key", key));
      }
    }
  }

  /**
   * Validates key and retrieves string list copy.<br>
   * <ul>
   * <li>The <code>ArrayList</code> constructor performs a shallow copy, but since the contents are
   * immutable objects (<code>String</code>), it is effectively a deep copy and the reference to
   * the stored list is severed.</li>
   * </ul>
   *
   * @param key the key
   * @return the string list (nullable)
   */
  private List<String> getCopyList(final String key) {
    // Validate key
    validateKeyByTypeForGet(StorageType.LIST, key);

    final List<String> list = this.listMap.get(key);
    if (ValUtil.isNull(list)) {
      return null;
    }
    // Return copy
    final List<String> copyList = new ArrayList<>(list);
    return copyList;
  }

  /**
   * Validates key and retrieves nested map copy.<br>
   * <ul>
   * <li>The <code>Io</code> constructor deep copies the contents, so the reference to the stored
   * map is severed.</li>
   * </ul>
   *
   * @param key the key
   * @return the nested map (nullable)
   */
  private Io getCopyNest(final String key) {
    // Validate key
    validateKeyByTypeForGet(StorageType.NEST, key);

    final Io nest = this.nestMap.get(key);
    if (ValUtil.isNull(nest)) {
      return null;
    }
    // Return copy
    final Io copyNest = new Io(nest);
    return copyNest;
  }

  /**
   * Validates key and retrieves multiple rows list copy.<br>
   * <ul>
   * <li><code>IoRows</code> constructor deep copies the contents, so the reference to the stored list is disconnected.</li>
   * </ul>
   *
   * @param key the key
   * @return the multiple rows list (nullable)
   */
  private IoRows getCopyRows(final String key) {
    // Key validation
    validateKeyByTypeForGet(StorageType.ROWS, key);

    final IoRows rows = this.rowsMap.get(key);
    if (ValUtil.isNull(rows)) {
      return null;
    }
    // Return copy
    final IoRows copyRows = new IoRows(rows);
    return copyRows;
  }

  /**
   * Validates key and retrieves array list copy.<br>
   * <ul>
   * <li><code>IoArrays</code> constructor deep copies the contents, so the reference to the stored list is disconnected.</li>
   * </ul>
   *
   * @param key the key
   * @return the array list (nullable)
   */
  private IoArrays getCopyArys(final String key) {
    // Key validation
    validateKeyByTypeForGet(StorageType.ARYS, key);

    final IoArrays arys = this.arysMap.get(key);
    if (ValUtil.isNull(arys)) {
      return null;
    }
    // Return copy
    final IoArrays copyArys = new IoArrays(arys);
    return copyArys;
  }

  /**
   * Validates key and stores string list copy.<br>
   * <ul>
   * <li>The <code>ArrayList</code> constructor performs a shallow copy, but since the contents are
   * immutable objects (<code>String</code>), it is effectively a deep copy and the reference to
   * the source list is severed.</li>
   * </ul>
   *
   * @param key the key
   * @param srcList the source list
   * @param canOverwrite overwrite allowed
   * @return the previously stored string list
   */
  private List<String> putCopyList(final String key, final List<String> srcList,
      final boolean canOverwrite) {
    // Validate key
    validateKeyByTypeForPut(StorageType.LIST, key, canOverwrite);
    // Store in all keys
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.listMap.put(key, null);
    }
    // Store copy
    final List<String> copyList = new ArrayList<>(srcList);
    return this.listMap.put(key, copyList);
  }

  /**
   * Validates key and stores nested map copy.<br>
   * <ul>
   * <li>The <code>Io</code> constructor deep copies the contents, so the reference to the source
   * map is severed.</li>
   * </ul>
   *
   * @param key the key
   * @param srcMap the source map
   * @param canOverwrite overwrite allowed
   * @return the previously stored nested map
   */
  private Io putCopyNest(final String key, final Io srcMap, final boolean canOverwrite) {
    // Validate key
    validateKeyByTypeForPut(StorageType.NEST, key, canOverwrite);
    // Store in all keys
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcMap)) {
      return this.nestMap.put(key, null);
    }
    // Store copy
    final Io copyNest = new Io(srcMap);
    return this.nestMap.put(key, copyNest);
  }

  /**
   * Validates key and stores multiple rows list copy.<br>
   * <ul>
   * <li><code>IoRows</code> constructor deep copies the contents, so the reference to the source list is disconnected.</li>
   * </ul>
   *
   * @param key the key
   * @param srcList the source list
   * @param canOverwrite overwrite allowed
   * @return the previous stored multiple rows list
   */
  private IoRows putCopyRows(final String key,
      final Collection<? extends Map<? extends String, ? extends String>> srcList,
      final boolean canOverwrite) {
    // Key validation
    validateKeyByTypeForPut(StorageType.ROWS, key, canOverwrite);
    // Store in all keys
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.rowsMap.put(key, null);
    }
    // Store copy
    final IoRows copyRows = new IoRows(srcList);
    return this.rowsMap.put(key, copyRows);
  }

  /**
   * Validates key and stores array list copy.<br>
   * <ul>
   * <li><code>IoArrays</code> constructor deep copies the contents, so the reference to the source list is disconnected.</li>
   * </ul>
   *
   * @param key the key
   * @param srcList the source list
   * @param canOverwrite overwrite allowed
   * @return the previous stored array list
   */
  private IoArrays putCopyArys(final String key,
      final Collection<? extends Collection<? extends String>> srcList,
      final boolean canOverwrite) {
    // Key validation
    validateKeyByTypeForPut(StorageType.ARYS, key, canOverwrite);
    // Store in all keys
    super.allKeySet().add(key);

    if (ValUtil.isNull(srcList)) {
      return this.arysMap.put(key, null);
    }
    // Store copy
    final IoArrays copyArys = new IoArrays(srcList);
    return this.arysMap.put(key, copyArys);
  }

  /**
   * Retrieves string list.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>If retrieving a value with a key that may not exist, confirm existence beforehand with
   * <code>#containsKeyList(String)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns a zero-size list. (<code>null</code> is
   * not returned)</li>
   * <li>Adding elements to the retrieved list does not affect the list held by this class. (deep
   * copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the string list
   */
  public List<String> getList(final String key) {
    final List<String> list = getCopyList(key);
    if (ValUtil.isNull(list)) {
      return Collections.emptyList();
    }
    return list;
  }

  /**
   * Stores string list.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If storing a value with a key that may already exist, store with
   * <code>#putListForce(String, List)</code>.</li>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param list the string list
   * @return the previously stored string list
   */
  public List<String> putList(final String key, final List<String> list) {
    return putCopyList(key, list, false);
  }

  /**
   * Stores string list (overwrite allowed).<br>
   * <ul>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param list the string list
   * @return the previously stored string list
   */
  public List<String> putListForce(final String key, final List<String> list) {
    return putCopyList(key, list, true);
  }

  /**
   * Retrieves nested map.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>If retrieving a value with a key that may not exist, confirm existence beforehand with
   * <code>#containsKeyNest(String)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns a zero-size map. (<code>null</code> is
   * not returned)</li>
   * <li>Adding elements to the retrieved map does not affect the map held by this class. (deep
   * copied)</li>
   * <li>Performance degrades when handling large maps or repeatedly storing and retrieving
   * maps.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the nested map
   */
  public Io getNest(final String key) {
    final Io nest = getCopyNest(key);
    if (ValUtil.isNull(nest)) {
      return new Io();
    }
    return nest;
  }

  /**
   * Stores nested map.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If storing a value with a key that may already exist, store with
   * <code>#putNestForce(String, Nest)</code>.</li>
   * <li>Adding elements to the original stored map does not affect the map held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large maps or repeatedly storing and retrieving
   * maps.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param nest the nested map
   * @return the previously stored nested map
   */
  public Io putNest(final String key, final Io nest) {
    return putCopyNest(key, nest, false);
  }

  /**
   * Stores nested map (overwrite allowed).<br>
   * <ul>
   * <li>Adding elements to the original stored map does not affect the map held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large maps or repeatedly storing and retrieving
   * maps.</li>
   * </ul>
   *
   * @see #putNest(String, Nest)
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param nest the nested map
   * @return the previously stored nested map
   */
  public Io putNestForce(final String key, final Io nest) {
    return putCopyNest(key, nest, true);
  }

  /**
   * Retrieves multiple rows list.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>If retrieving a value with a key that may not exist, confirm existence beforehand with
   * <code>#containsKeyRows(String)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns a zero-size list. (<code>null</code> is
   * not returned)</li>
   * <li>Adding elements to the retrieved list does not affect the list held by this class. (deep
   * copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the multiple rows list
   */
  public IoRows getRows(final String key) {
    final IoRows rows = getCopyRows(key);
    if (ValUtil.isNull(rows)) {
      return new IoRows();
    }
    return rows;
  }

  /**
   * Stores multiple rows list.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If storing a value with a key that may already exist, store with
   * <code>#putRowsForce(String, List)</code>.</li>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param rows the multiple rows list
   * @return the previously stored multiple rows list
   */
  public IoRows putRows(final String key,
      final Collection<? extends Map<? extends String, ? extends String>> rows) {
    return putCopyRows(key, rows, false);
  }

  /**
   * Stores multiple rows list (overwrite allowed).<br>
   * <ul>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param rows the multiple rows list
   * @return the previously stored multiple rows list
   */
  public IoRows putRowsForce(final String key, final Collection<? extends IoItems> rows) {
    return putCopyRows(key, rows, true);
  }

  /**
   * Retrieves array list.<br>
   * <ul>
   * <li>Retrieving a value with a non-existent key causes a runtime error.</li>
   * <li>If retrieving a value with a key that may not exist, confirm existence beforehand with
   * <code>#containsKeyArys(String)</code>.</li>
   * <li>If the stored value is <code>null</code>, returns a zero-size list. (<code>null</code> is
   * not returned)</li>
   * <li>Adding elements to the retrieved list does not affect the list held by this class. (deep
   * copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the array list
   */
  public IoArrays getArys(final String key) {
    final IoArrays arys = getCopyArys(key);
    if (ValUtil.isNull(arys)) {
      return new IoArrays();
    }
    return arys;
  }

  /**
   * Stores array list.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If storing a value with a key that may already exist, store with
   * <code>#putArysForce(String, List)</code>.</li>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param arys the array list
   * @return the previously stored array list
   */
  public IoArrays putArys(final String key, final Collection<? extends List<String>> arys) {
    return putCopyArys(key, arys, false);
  }

  /**
   * Stores array list (overwrite allowed).<br>
   * <ul>
   * <li>Adding elements to the original stored list does not affect the list held by this class.
   * (deep copied)</li>
   * <li>Performance degrades when handling large lists or repeatedly storing and retrieving
   * lists.</li>
   * </ul>
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @param arys the array list
   * @return the previously stored array list
   */
  public IoArrays putArysForce(final String key, final Collection<? extends List<String>> arys) {
    return putCopyArys(key, arys, true);
  }

  /**
   * Creates URL parameters (the part after ? in URL).<br>
   * <ul>
   * <li>String lists become comma-separated parameters.</li>
   * <li>Nested maps, multiple rows lists, and array lists are not output.</li>
   * </ul>
   *
   * @return the URL-encoded GET parameters
   */
  public String createUrlParam() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      if (this.listMap.containsKey(key)) {
        final List<String> list = this.listMap.get(key);
        final String paramAry = listToUrlParamArray(key, list);
        sb.append(paramAry).append('&');
        continue;
      }
      final String val = valMap.get(key);
      final String encVal = ValUtil.urlEncode(val);
      sb.append(key).append('=').append(encVal).append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Converts string list to URL parameter array string.
   *
   * @param key the key
   * @param list the string list
   * @return the URL parameter array string
   */
  private String listToUrlParamArray(final String key, final List<String> list) {
    final String pKey = key + "[]";

    final StringBuilder sb = new StringBuilder();
    if (ValUtil.isEmpty(list)) {
      sb.append(pKey).append('=');
      return sb.toString();
    }
    for (final String val : list) {
      sb.append(pKey).append('=');
      final String encVal = ValUtil.urlEncode(val);
      sb.append(encVal);
      sb.append('&');
    }
    ValUtil.deleteLastChar(sb);
    return sb.toString();
  }

  /**
   * Creates JSON.
   *
   * @return the JSON string
   */
  public String createJson() {
    return createJsonWithMsg(null);
  }

  /**
   * Creates JSON with messages.
   * @param msgTextMap the message text map &lt;message ID, message text&gt;
   * @return the JSON string
   */
  public String createJsonWithMsg(final Map<String, String> msgTextMap) {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    for (final String key : super.allKeySet()) {
      sb.append('"').append(key).append('"').append(':');

      if (this.listMap.containsKey(key)) {
        final List<String> list = this.listMap.get(key);
        final String json = listToJsonArray(list);
        sb.append(json).append(',');
        continue;
      }
      if (this.nestMap.containsKey(key)) {
        final Io nest = this.nestMap.get(key);
        final String json = nest.createJson();
        sb.append(json).append(',');
        continue;
      }
      if (this.rowsMap.containsKey(key)) {
        final IoRows rows = this.rowsMap.get(key);
        final String json = rowsToJsonArray(rows);
        sb.append(json).append(',');
        continue;
      }
      if (this.arysMap.containsKey(key)) {
        final IoArrays arys = this.arysMap.get(key);
        final String json = arysToJsonArray(arys);
        sb.append(json).append(',');
        continue;
      }

      final String val = valMap.get(key);
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }

    // Add message (only if message exists)
    if (hasMsg()) {
      final String msg = createMsgJsoAry(msgTextMap);
      sb.append('"').append("_msg").append('"').append(':').append(msg).append(',');
      // Add error flag
      sb.append('"').append("_has_err").append('"').append(':').append(hasErrorMsg()).append(',');
    }

    ValUtil.deleteLastChar(sb);
    sb.insert(0, '{');
    sb.append('}');
    return sb.toString();
  }

  /**
   * Converts string list to JSON array string.
   *
   * @param list the string list
   * @return the JSON array string
   */
  private String listToJsonArray(final List<String> list) {
    if (ValUtil.isEmpty(list)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final String val : list) {
      if (ValUtil.isNull(val)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String escVal = ValUtil.jsonEscape(val);
      sb.append('"').append(escVal).append('"').append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * Converts multiple rows list to JSON array string.
   *
   * @param rows the multiple rows list
   * @return the JSON array string
   */
  private String rowsToJsonArray(final IoRows rows) {
    if (ValUtil.isEmpty(rows)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final IoItems row : rows) {
      if (ValUtil.isNull(row)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String json = row.createJson();
      sb.append(json).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * Converts array list to JSON array string.
   *
   * @param arys the array list
   * @return the JSON array string
   */
  private String arysToJsonArray(final IoArrays arys) {
    if (ValUtil.isEmpty(arys)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final List<String> ary : arys) {
      if (ValUtil.isNull(ary)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String json = listToJsonArray(ary);
      sb.append(json).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * Creates log output string.
   *
   * @return the log output string
   */
  private final String createLogString() {
    final Map<String, String> valMap = super.getValMap();
    final StringBuilder sb = new StringBuilder();
    try {
      for (final String key : super.allKeySet()) {
        sb.append(key).append('=');
        if (this.listMap.containsKey(key)) {
          final List<String> list = this.listMap.get(key);
          final String log = LogUtil.join(list);
          sb.append(log).append(',');
          continue;
        }
        if (this.nestMap.containsKey(key)) {
          final Io nest = this.nestMap.get(key);
          final String log = nest.createLogString();
          sb.append(log).append(',');
          continue;
        }
        if (this.rowsMap.containsKey(key)) {
          final IoRows rows = this.rowsMap.get(key);
          final String log = rowsToLog(rows);
          sb.append(log).append(',');
          continue;
        }
        if (this.arysMap.containsKey(key)) {
          final IoArrays arys = this.arysMap.get(key);
          final String log = arysToLog(arys);
          sb.append(log).append(',');
          continue;
        }

        final String val = valMap.get(key);
        final String sval = LogUtil.convOutput(val);
        sb.append(sval);
        sb.append(',');
      }
      ValUtil.deleteLastChar(sb);
      sb.insert(0, '{');
      sb.append('}');
    } catch (Exception ignore) {
      // No processing
    }
    return sb.toString();
  }

  /**
   * Converts multiple rows list to log output string.
   *
   * @param rows the multiple rows list
   * @return the log output string
   */
  private String rowsToLog(final IoRows rows) {
    if (ValUtil.isEmpty(rows)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final IoItems row : rows) {
      if (ValUtil.isNull(row)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String log = row.createLogString();
      sb.append(log).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }

  /**
   * Converts array list to log output string.
   *
   * @param arys the array list
   * @return the log output string
   */
  private String arysToLog(final IoArrays arys) {
    if (ValUtil.isEmpty(arys)) {
      return "[]";
    }
    final StringBuilder sb = new StringBuilder();
    for (final List<String> ary : arys) {
      if (ValUtil.isNull(ary)) {
        sb.append(ValUtil.JSON_NULL).append(',');
        continue;
      }
      final String log = LogUtil.join(ary);
      sb.append(log).append(',');
    }
    ValUtil.deleteLastChar(sb);
    sb.insert(0, '[');
    sb.append(']');
    return sb.toString();
  }


  /**
   * Stores all values.<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * <li>If keys that may already exist are included, store with <code>#putAllForce(Map)</code>.</li>
   * <li>Messages are also copied.</li>
   * </ul>
   *
   * @param map the I/O map
   */
  public void putAll(final Io map) {
    putAllByIoMap(map, false);
  }

  /**
   * Stores all values (overwrite allowed).
   * @see #putAll(Io)
   * @param map the I/O map
   */
  public void putAllForce(final Io map) {
    putAllByIoMap(map, true);
  }

  /**
   * Stores I/O map.<br>
   * <ul>
   * <li>Deep copies the contents, so the reference to the source map is severed.</li>
   * <li>Messages are also copied.</li>
   * </ul>
   *
   * @param srcMap       the source map
   * @param canOverwrite overwrite allowed
   */
  private void putAllByIoMap(final Io srcMap, final boolean canOverwrite) {

    if (ValUtil.isNull(srcMap)) {
      return;
    }

    final Map<String, String> valMap = srcMap.getValMap();

    // Loop through all keys
    for (final String key : srcMap.allKeySet()) {
      // Store value
      if (valMap.containsKey(key)) {
        putVal(key, valMap.get(key), canOverwrite);
      }
      // Store string list
      if (srcMap.listMap.containsKey(key)) {
        putCopyList(key, srcMap.listMap.get(key), canOverwrite);
      }
      // Store nested map
      if (srcMap.nestMap.containsKey(key)) {
        putCopyNest(key, srcMap.nestMap.get(key), canOverwrite);
      }
      // Store multiple rows list
      if (srcMap.rowsMap.containsKey(key)) {
        putCopyRows(key, srcMap.rowsMap.get(key), canOverwrite);
      }
      // Store array list
      if (srcMap.arysMap.containsKey(key)) {
        putCopyArys(key, srcMap.arysMap.get(key), canOverwrite);
      }
    }
    // Copy messages as well
    copyMsg(srcMap);
  }

  /**
   * Stores URL parameters (the part after ? in URL).<br>
   * <ul>
   * <li>Storing with an already existing key causes a runtime error.</li>
   * </ul>
   *
   * @param url the full URL or URL parameters
   * @return the number of stored parameters
   */
  public int putAllByUrlParam(final String url) {
    if (ValUtil.isBlank(url)) {
      return 0;
    }

    final String params;
    if (url.indexOf('?') > 0) {
      params = url.substring(url.indexOf('?') + 1);
    } else {
      params = url;
    }

    final Map<String, List<String>> aryList = new LinkedHashMap<>();
    int count = 0;
    for (final String param : new SimpleSeparateParser(params, "&")) {
      final String[] keyVal = ValUtil.splitReg(param, "=", 2);
      final String key = keyVal[0];
      final String val;
      if (keyVal.length == 1) {
        val = ValUtil.BLANK;
      } else {
        val = ValUtil.urlDecode(keyVal[1]);
      }

      if (key.endsWith("[]")) {
        final String lsKey = key.substring(key.length() - 2);
        final List<String> list;
        if (aryList.containsKey(lsKey)) {
          list = aryList.get(lsKey);
        } else {
          list = new ArrayList<>();
          aryList.put(lsKey, list);
        }
        list.add(val);
        continue;
      }

      count++;
      put(key, val);
    }

    for (final Entry<String, List<String>> ent : aryList.entrySet()) {
      count++;
      putList(ent.getKey(), ent.getValue());
    }

    return count;
  }

  /**
   * Stores JSON.
   *
   * @param json the JSON string
   * @return the number of stored items
   */
  public int putAllByJson(final String json) {
    if (ValUtil.isBlank(json)) {
      return 0;
    }

    int count = 0;

    // Loop through JSON items
    for (final String item : new JsonMapSeparateParser(json)) {
      final String[] keyVal = JsonMapKeyValueSeparateParser.getKeyValue(item);
      if (ValUtil.isNull(keyVal)) {
        continue;
      }
      final String key = keyVal[0];
      final String val = keyVal[1];

      count++;
      if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(val).find()) {
        // Add nested map
        final Io nest = new Io();
        nest.putAllByJson(val);
        putNest(key, nest);
        continue;
      }
      if (JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(val).find()) {
        // Add string list
        final List<String> list = jsonArrayToList(val);
        // However, if there is an associative array in the value, add as multiple rows list
        // If there is an array in the value, add as array list
        boolean isRows = false;
        boolean isArys = false;
        for (final String listVal : list) {
          if (ValUtil.isBlank(listVal)) {
            continue;
          }
          if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(listVal).find()) {
            isRows = true;
            break;
          }
          if (JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(listVal).find()) {
            isArys = true;
            break;
          }
        }
        if (isRows) {
          // Add multiple rows list
          final IoRows rows = jsonArrayToRows(val);
          putRows(key, rows);
          continue;
        }
        if (isArys) {
          // Add array list
          final IoArrays arys = jsonArrayToArys(val);
          putArys(key, arys);
          continue;
        }
        putList(key, list);
        continue;
      }
      if (ValUtil.JSON_NULL.equals(val)) {
        putNull(key);
        continue;
      }
      final String unEscVal = ValUtil.jsonUnEscape(ValUtil.trimDq(val));
      put(key, unEscVal);
    }
    return count;
  }

  /**
   * Converts JSON array string to string list.
   *
   * @param json the JSON array string
   * @return the string list
   */
  private List<String> jsonArrayToList(final String json) {
    final List<String> list = new ArrayList<>();
    for (final String value : new JsonArraySeparateParser(json)) {
      if (ValUtil.JSON_NULL.equals(value)) {
        list.add(null);
        continue;
      }
      final String val = ValUtil.jsonUnEscape(value);
      list.add(val);
    }
    return list;
  }

  /**
   * Converts JSON array string to multiple rows list.
   *
   * @param jsonAry the JSON array string
   * @return the multiple rows list
   */
  private IoRows jsonArrayToRows(final String jsonAry) {
    final IoRows rows = new IoRows();
    for (final String json : new JsonArraySeparateParser(jsonAry)) {
      if (ValUtil.JSON_NULL.equals(json)) {
        rows.add(null);
        continue;
      }

      final IoItems row = new IoItems();
      row.putAllByJson(json);

      // Check if there are 4 or more layers
      for (final String value : row.values()) {
        if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(value).find()
            || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(value).find()) {
          throw new RuntimeException("Arrays of 4 or more layers are not supported. " + LogUtil.joinKeyVal("json", jsonAry));
        }
      }

      rows.add(row);
    }
    return rows;
  }

  /**
   * Converts JSON array string to array list.
   *
   * @param jsonAry the JSON array string
   * @return the array list
   */
  private IoArrays jsonArrayToArys(final String jsonAry) {
    final IoArrays arys = new IoArrays();

    for (final String json : new JsonArraySeparateParser(jsonAry)) {
      if (ValUtil.JSON_NULL.equals(json)) {
        arys.add(null);
        continue;
      }

      final List<String> ary = new ArrayList<>();
      for (final String aryVal : new JsonArraySeparateParser(json)) {

        if (ValUtil.JSON_NULL.equals(aryVal)) {
          ary.add(null);
          continue;
        }

        // Check if there are 4 or more layers
        if (JsonMapSeparateParser.JSON_MAP_PATTERN.matcher(aryVal).find()
            || JsonArraySeparateParser.JSON_ARRAY_PATTERN.matcher(aryVal).find()) {
          throw new RuntimeException("Arrays of 4 or more layers are not supported. " + LogUtil.joinKeyVal("json", jsonAry));
        }

        final String val = ValUtil.jsonUnEscape(aryVal);
        ary.add(val);
      }

      arys.add(ary);
    }
    return arys;
  }

  /**
   * Returns string for logging.
   */
  public final String toString() {
    return createLogString();
  }

  /**
   * Checks if string list key exists.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return <code>true</code> if exists
   */
  public boolean containsKeyList(final String key) {
    return this.listMap.containsKey(key);
  }

  /**
   * Removes string list.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the removed list
   */
  public List<String> removeList(final String key) {
    super.allKeySet().remove(key);
    return this.listMap.remove(key);
  }

  /**
   * Checks if nested map key exists.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return <code>true</code> if exists
   */
  public boolean containsKeyNest(final String key) {
    return this.nestMap.containsKey(key);
  }

  /**
   * Removes nested map.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the removed nested map
   */
  public Io removeNest(final String key) {
    super.allKeySet().remove(key);
    return this.nestMap.remove(key);
  }

  /**
   * Checks if multiple rows list key exists.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return <code>true</code> if exists
   */
  public boolean containsKeyRows(final String key) {
    return this.rowsMap.containsKey(key);
  }

  /**
   * Removes multiple rows list.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the removed multiple rows list
   */
  public IoRows removeRows(final String key) {
    super.allKeySet().remove(key);
    return this.rowsMap.remove(key);
  }

  /**
   * Checks if array list key exists.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return <code>true</code> if exists
   */
  public boolean containsKeyArys(final String key) {
    return this.arysMap.containsKey(key);
  }

  /**
   * Removes array list.
   *
   * @param key the key (lowercase letters, numbers, underscores, hyphens, and dots only)
   * @return the removed array list
   */
  public IoArrays removeArys(final String key) {
    super.allKeySet().remove(key);
    return this.arysMap.remove(key);
  }

  /** Message holding map. */
  private final Map<String, MsgBean> msgMap = new LinkedHashMap<>();
  /** Error message exists flag. */
  private boolean errMsgExists = false;

  /** Message type. */
  public enum MsgType {
    /** Error message. */
    ERROR,
    /** Warning message. */
    WARN,
    /** Information message. */
    INFO;
  }

  /** Message holding class. */
  private class MsgBean {

    /** Message type. */
    private MsgType type;
    /** Message ID. */
    private String msgId;
    /** Replacement strings in message. */
    private String[] replaceVals;
    /** Target item ID. */
    private String itemId;
    /** Row list ID. */
    private String rowListId;
    /** Row index. */
    private int rowIndex;

    /**
     * Constructor.
     * 
     * @param type        the message type
     * @param msgId       the message ID
     * @param replaceVals the replacement strings in message to replace <code>{0}, {1}, ...</code>
     *                    in the message text
     * @param itemId      the target item ID
     * @param rowListId   the row list ID
     * @param rowIndex    the row index
     */
    private MsgBean(final MsgType type, final String msgId, final String[] replaceVals, final String itemId,
        final String rowListId, final int rowIndex) {
      this.type = type;
      this.msgId = msgId;
      this.replaceVals = replaceVals;
      this.itemId = itemId;
      this.rowListId = rowListId;
      this.rowIndex = rowIndex;
    }

    /**
     * Copy constructor.<br>
     * <ul>
     * <li>Performs deep copy and severs reference to the original object.</li>
     * </ul>
     * 
     * @param srcMsg the source message Bean
     */
    private MsgBean(final MsgBean srcMsg) {
      this.type = srcMsg.type;
      this.msgId = srcMsg.msgId;
      // Deep copy of array
      if (ValUtil.isNull(srcMsg.replaceVals)) {
        this.replaceVals = null;
      } else {
        this.replaceVals = new String[srcMsg.replaceVals.length];
        System.arraycopy(srcMsg.replaceVals, 0, this.replaceVals, 0, srcMsg.replaceVals.length);
      }
      this.itemId = srcMsg.itemId;
      this.rowListId = srcMsg.rowListId;
      this.rowIndex = srcMsg.rowIndex;
    }

    /**
     * Returns message type.
     * 
     * @return the message type
     */
    private MsgType getType() {
      return type;
    }

    /**
     * Creates key.<br>
     * <ul>
     * <li>Used to prevent duplicate registration of messages with the same content.</li>
     * <li>Determines the display order of messages.</li>
     * </ul>
     *
     * @return the key string
     */
    private String createKey() {
      final StringBuilder sb = new StringBuilder();
      sb.append(this.type.ordinal()).append("_").append(this.msgId);
      if (!ValUtil.isEmpty(this.replaceVals)) {
        sb.append("_").append(String.join("&", this.replaceVals));
      }
      if (!ValUtil.isBlank(this.itemId)) {
        if (ValUtil.isBlank(this.rowListId)) {
          sb.append(this.itemId);
        } else {
          sb.append(this.rowListId).append("[").append(ValUtil.paddingLeftZero(this.rowIndex, 4)).append("].")
              .append(this.itemId);
        }
      }
      return sb.toString();
    }

    /**
     * Creates JSON.
     * 
     * @param msgTextMap the message text map &lt;message ID, message text&gt; (optional)
     * @return the JSON string
     */
    protected String createJson(final Map<String, String> msgTextMap) {
      final StringBuilder sb = new StringBuilder();
      sb.append('{');
      sb.append('"').append("type").append('"').append(':').append('"').append(this.type.name()).append('"').append(',');
      sb.append('"').append("id").append('"').append(':').append('"').append(this.msgId).append('"').append(',');

      final String text;
      if (!ValUtil.isNull(msgTextMap) && msgTextMap.containsKey(this.msgId)) {
        final String msgText = msgTextMap.get(this.msgId);
        text = replaceText(msgText, this.replaceVals);
      } else {
        if (!ValUtil.isEmpty(this.replaceVals)) {
          text = "Error info: " + ValUtil.join(",", this.replaceVals);
        } else {
          text = ValUtil.BLANK;
        }
      }
      sb.append('"').append("text").append('"').append(':').append('"').append(ValUtil.jsonEscape(text)).append('"')
          .append(',');

      if (!ValUtil.isBlank(this.itemId)) {
        sb.append('"').append("item").append('"').append(':');
        if (!ValUtil.isBlank(this.rowListId)) {
          sb.append('"').append(this.rowListId).append('.').append(this.itemId).append('"').append(',');
          sb.append('"').append("row").append('"').append(':').append(this.rowIndex).append(',');
        } else {
          sb.append('"').append(this.itemId).append('"').append(',');
        }
      }
      ValUtil.deleteLastChar(sb);
      sb.append('}');
      return sb.toString();
    }

    /**
     * Replaces text.<br>
     * <ul>
     * <li>Replaces <code>{0}, {1}, ...</code> in the message text.</li>
     * </ul>
     * 
     * @param text the target text
     * @param replaceVals the replacement value array
     * @return the replaced text
     */
    private String replaceText(final String text, final String[] replaceVals) {
      String ret = text;
      if (!ValUtil.isEmpty(replaceVals)) {
        for (int i = 0; i < replaceVals.length; i++) {
          final String key = "{" + i + "}";
          final String val = ValUtil.nvl(replaceVals[i]);
          ret = ret.replace(key, val);
        }
      }
      // If {0}, {1}, ... remain, replace with empty string
      final String regex = "\\{[0-9]+\\}";
      ret = ret.replaceAll(regex, ValUtil.BLANK);
      return ret;
    }
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   */
  public void putMsg(final MsgType type, final String msgId) {
    putMsg(type, msgId, null, null, null, -1);
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   * @param replaceVals the replacement strings in message (optional)
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals) {
    putMsg(type, msgId, replaceVals, null, null, -1);
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   * @param itemId the target item ID (optional)
   */
  public void putMsg(final MsgType type, final String msgId, final String itemId) {
    putMsg(type, msgId, null, itemId, null, -1);
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   * @param replaceVals the replacement strings in message (optional)
   * @param itemId the target item ID (optional)
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals, final String itemId) {
    putMsg(type, msgId, replaceVals, itemId, null, -1);
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   * @param itemId the target item ID (optional)
   * @param rowListId the row list ID (optional)
   * @param rowIndex the row index (optional)
   */
  public void putMsg(final MsgType type, final String msgId, final String itemId, final String rowListId,
      final int rowIndex) {
    putMsg(type, msgId, null, itemId, rowListId, rowIndex);
  }

  /**
   * Adds message.
   * 
   * @param type the message type
   * @param msgId the message ID
   * @param replaceVals the replacement strings in message (optional)
   * @param itemId the target item ID (optional)
   * @param rowListId the row list ID (optional)
   * @param rowIndex the row index (optional)
   */
  public void putMsg(final MsgType type, final String msgId, final String[] replaceVals, final String itemId,
      final String rowListId, final int rowIndex) {
    if (ValUtil.isBlank(msgId)) {
      throw new RuntimeException("msgId is blank.");
    }
    if (rowIndex >= 0 && ValUtil.isBlank(rowListId)) {
      throw new RuntimeException("rowListId is blank.");
    }
    if (!ValUtil.isBlank(rowListId) && rowIndex < 0) {
      throw new RuntimeException("rowIndex is invalid.");
    }
    if (!ValUtil.isBlank(rowListId) && ValUtil.isBlank(itemId)) {
      throw new RuntimeException("itemId is blank.");
    }
    final MsgBean msg = new MsgBean(type, msgId, replaceVals, itemId, rowListId, rowIndex);
    final String key = msg.createKey();
    if (this.msgMap.containsKey(key)) {
      return;
    }
    this.msgMap.put(key, msg);
    
    // Update error message flag
    if (MsgType.ERROR == type) {
      this.errMsgExists = true;
    }
  }

  /**
   * Checks if message is held.
   * 
   * @return <code>true</code> if message is held
   */
  public boolean hasMsg() {
    return !ValUtil.isEmpty(this.msgMap);
  }

  /**
   * Checks if error message exists.
   * 
   * @return <code>true</code> if error message exists
   */
  public boolean hasErrorMsg() {
    return this.errMsgExists;
  }

  /**
   * Clears messages.
   */
  public void clearMsg() {
    this.msgMap.clear();
    this.errMsgExists = false;
  }

  /**
   * Copies messages.<br>
   * <ul>
   * <li>Deep copies messages, so the reference to the original object is severed.</li>
   * </ul>
   * 
   * @param srcMap the source map
   */
  private void copyMsg(final Io srcMap) {
    if (ValUtil.isNull(srcMap)) {
      return;
    }

    for (final MsgBean bean : srcMap.msgMap.values()) {
      // Store as deep copy
      final MsgBean msg = new MsgBean(bean);
      final String key = msg.createKey();
      if (this.msgMap.containsKey(key)) {
        return;
      }
      this.msgMap.put(key, msg);

      // Update error message flag
      if (MsgType.ERROR == msg.getType()) {
        this.errMsgExists = true;
      }
    }
  }

  /**
   * Creates message JSON array.<br>
   * <ul>
   * <li>If message text map is specified, sets the message text corresponding to the message
   * ID.</li>
   * <li>If message text map is not specified, the message text will be an empty string.</li>
   * </ul>
   * 
   * @param msgTextMap the message text map &lt;message ID, message text&gt;
   * @return the JSON array "[ {...}, {...}, ... ]"
   */
  private String createMsgJsoAry(final Map<String, String> msgTextMap) {
    final StringBuilder sb = new StringBuilder();
    sb.append('[');
    if (!ValUtil.isEmpty(this.msgMap)) {
      for (final MsgBean msgBean : this.msgMap.values()) {
        final String msgJson = msgBean.createJson(msgTextMap);
        sb.append(msgJson).append(',');
      }
      ValUtil.deleteLastChar(sb);
    }
    sb.append(']');
    return sb.toString();
  }
}
