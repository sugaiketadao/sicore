# Java API Reference

## AbstractBatch (Abstract Class)

**Package**: com.onepg.bat

### Methods

#### doExecute (Abstract Method)

```
protected void doExecute(IoItems args)
```

**Parameters**:
- `args`: `IoItems`

#### callMain

```
protected int callMain(String[] args)
```

**Parameters**:
- `args`: `String[]`

**Returns**: `int`

## AbstractDbAccessBatch (Abstract Class)

**Package**: com.onepg.bat

### Methods

#### doExecute (Abstract Method)

```
protected void doExecute(IoItems args)
```

**Parameters**:
- `args`: `IoItems`

#### callMain

```
protected int callMain(String[] args)
```

**Parameters**:
- `args`: `String[]`

**Returns**: `int`

#### getDbConn

```
protected Connection getDbConn()
```

**Returns**: `Connection`

## AbstractDbAccessWebService (Abstract Class)

**Package**: com.onepg.web

### Methods

#### getDbConn

```
protected Connection getDbConn()
```

**Returns**: `Connection`

## AbstractIoTypeMap (Abstract Class)

**Package**: com.onepg.util

### Methods

#### getValMap

```
protected Map<String, String> getValMap()
```

**Returns**: `Map<String, String>`

#### allKeySet

```
protected Set<String> allKeySet()
```

**Returns**: `Set<String>`

#### validateKey

```
protected void validateKey(String key)
```

**Parameters**:
- `key`: `String`

#### getVal

```
protected String getVal(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### putVal

```
protected String putVal(String key, String value, boolean canOverwrite)
```

**Parameters**:
- `key`: `String`
- `value`: `String`
- `canOverwrite`: `boolean`

**Returns**: `String`

## AbstractWebService (Abstract Class)

**Package**: com.onepg.web

### Methods

#### doExecute (Abstract Method)

```
public void doExecute(Io io)
```

**Parameters**:
- `io`: `Io`

## BreakException

**Package**: com.onepg.util

## CsvReader

**Package**: com.onepg.util

### Methods

#### iterator

```
public Iterator<IoItems> iterator()
```

**Returns**: `Iterator<IoItems>`

#### close

```
public void close()
```

#### getReadedCount

```
public int getReadedCount()
```

**Returns**: `int`

#### isReadedEndRow

```
public boolean isReadedEndRow()
```

**Returns**: `boolean`

#### hasNext

```
public boolean hasNext()
```

**Returns**: `boolean`

#### next

```
public IoItems next()
```

**Returns**: `IoItems`

## CsvWriter

**Package**: com.onepg.util

### Methods

#### close

```
public void close()
```

#### println

```
public void println(String[] values)
```

**Parameters**:
- `values`: `String[]`

#### println

```
public void println(IoItems row)
```

**Parameters**:
- `row`: `IoItems`

#### flush

```
public void flush()
```

#### getFilePath

```
public String getFilePath()
```

**Returns**: `String`

#### getLineCount

```
public long getLineCount()
```

**Returns**: `long`

## DbUtil

**Package**: com.onepg.db

### Methods

#### getConn

```
public Connection getConn()
```

**Returns**: `Connection`

#### getConn

```
public Connection getConn(String traceCode)
```

**Parameters**:
- `traceCode`: `String`

**Returns**: `Connection`

#### getConnByConfigName

```
public Connection getConnByConfigName(String connName)
```

**Parameters**:
- `connName`: `String`

**Returns**: `Connection`

#### getConnByConfigName

```
public Connection getConnByConfigName(String connName, String traceCode)
```

**Parameters**:
- `connName`: `String`
- `traceCode`: `String`

**Returns**: `Connection`

#### getConnPooled

```
public Connection getConnPooled()
```

**Returns**: `Connection`

#### getConnPooled

```
public Connection getConnPooled(String traceCode)
```

**Parameters**:
- `traceCode`: `String`

**Returns**: `Connection`

#### getConnPooledByConfigName

```
public Connection getConnPooledByConfigName(String connName)
```

**Parameters**:
- `connName`: `String`

**Returns**: `Connection`

#### getConnPooledByConfigName

```
public Connection getConnPooledByConfigName(String connName, String traceCode)
```

**Parameters**:
- `connName`: `String`
- `traceCode`: `String`

**Returns**: `Connection`

#### closePooledConn

```
public void closePooledConn()
```

#### getConnNames

```
public List<String> getConnNames()
```

**Returns**: `List<String>`

## FileUtil

**Package**: com.onepg.util

### Methods

#### getOsTemporaryPath

```
public String getOsTemporaryPath()
```

**Returns**: `String`

#### joinPath

```
public String joinPath(String... paths)
```

**Parameters**:
- `paths`: `String...`

**Returns**: `String`

#### convOsPath

```
public String convOsPath(String path)
```

**Parameters**:
- `path`: `String`

**Returns**: `String`

#### convAbsolutePath

```
public String convAbsolutePath(String path)
```

**Parameters**:
- `path`: `String`

**Returns**: `String`

#### exists

```
public boolean exists(String checkPath)
```

**Parameters**:
- `checkPath`: `String`

**Returns**: `boolean`

#### existsParent

```
public boolean existsParent(String checkPath)
```

**Parameters**:
- `checkPath`: `String`

**Returns**: `boolean`

#### getFileName

```
public String getFileName(String fullPath)
```

**Parameters**:
- `fullPath`: `String`

**Returns**: `String`

#### getParentPath

```
public String getParentPath(String fullPath)
```

**Parameters**:
- `fullPath`: `String`

**Returns**: `String`

#### getFileModifiedDateTime

```
public String getFileModifiedDateTime(String fullPath)
```

**Parameters**:
- `fullPath`: `String`

**Returns**: `String`

#### splitFileTypeMark

```
public String[] splitFileTypeMark(String fileName)
```

**Parameters**:
- `fileName`: `String`

**Returns**: `String[]`

#### getFileList

```
public List<String> getFileList(String dirPath, String typeMark, String prefixMatch, String middleMatch, String suffixMatch)
```

**Parameters**:
- `dirPath`: `String`
- `typeMark`: `String`
- `prefixMatch`: `String`
- `middleMatch`: `String`
- `suffixMatch`: `String`

**Returns**: `List<String>`

#### move

```
public File move(String srcFilePath, String destFilePath)
```

**Parameters**:
- `srcFilePath`: `String`
- `destFilePath`: `String`

**Returns**: `File`

#### move

```
public File move(File srcFile, File destFile)
```

**Parameters**:
- `srcFile`: `File`
- `destFile`: `File`

**Returns**: `File`

#### copy

```
public File copy(String srcFilePath, String destFilePath)
```

**Parameters**:
- `srcFilePath`: `String`
- `destFilePath`: `String`

**Returns**: `File`

#### copy

```
public File copy(File srcFile, File destFile)
```

**Parameters**:
- `srcFile`: `File`
- `destFile`: `File`

**Returns**: `File`

#### delete

```
public boolean delete(String deleteFilePath)
```

**Parameters**:
- `deleteFilePath`: `String`

**Returns**: `boolean`

#### delete

```
public boolean delete(File deleteFile)
```

**Parameters**:
- `deleteFile`: `File`

**Returns**: `boolean`

#### makeDir

```
public boolean makeDir(String dirPath)
```

**Parameters**:
- `dirPath`: `String`

**Returns**: `boolean`

## Io

**Package**: com.onepg.util

### Methods

#### getString

```
public String getString(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### getStringOrDefault

```
public String getStringOrDefault(String key, String notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `String`

**Returns**: `String`

#### getStringNullable

```
public String getStringNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### getStringNullableOrDefault

```
public String getStringNullableOrDefault(String key, String notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `String`

**Returns**: `String`

#### getBigDecimal

```
public BigDecimal getBigDecimal(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `BigDecimal`

#### getBigDecimalOrDefault

```
public BigDecimal getBigDecimalOrDefault(String key, BigDecimal notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `BigDecimal`

**Returns**: `BigDecimal`

#### getBigDecimalNullable

```
public BigDecimal getBigDecimalNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `BigDecimal`

#### getBigDecimalNullableOrDefault

```
public BigDecimal getBigDecimalNullableOrDefault(String key, BigDecimal notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `BigDecimal`

**Returns**: `BigDecimal`

#### getInt

```
public int getInt(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `int`

#### getIntOrDefault

```
public int getIntOrDefault(String key, int notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `int`

**Returns**: `int`

#### getLong

```
public long getLong(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `long`

#### getLongOrDefault

```
public long getLongOrDefault(String key, long notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `long`

**Returns**: `long`

#### getDateNullable

```
public LocalDate getDateNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `LocalDate`

#### getDateNullableOrDefault

```
public LocalDate getDateNullableOrDefault(String key, LocalDate notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `LocalDate`

**Returns**: `LocalDate`

#### getDateTimeNullable

```
public LocalDateTime getDateTimeNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `LocalDateTime`

#### getDateTimeNullableOrDefault

```
public LocalDateTime getDateTimeNullableOrDefault(String key, LocalDateTime notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `LocalDateTime`

**Returns**: `LocalDateTime`

#### getBoolean

```
public boolean getBoolean(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### getBooleanOrDefault

```
public boolean getBooleanOrDefault(String key, boolean notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `boolean`

**Returns**: `boolean`

#### putNull

```
public String putNull(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### putNullForce

```
public String putNullForce(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### put

```
public String put(String key, String value)
```

**Parameters**:
- `key`: `String`
- `value`: `String`

**Returns**: `String`

#### put

```
public String put(String key, BigDecimal value)
```

**Parameters**:
- `key`: `String`
- `value`: `BigDecimal`

**Returns**: `String`

#### put

```
public String put(String key, int value)
```

**Parameters**:
- `key`: `String`
- `value`: `int`

**Returns**: `String`

#### put

```
public String put(String key, long value)
```

**Parameters**:
- `key`: `String`
- `value`: `long`

**Returns**: `String`

#### put

```
public String put(String key, LocalDate value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDate`

**Returns**: `String`

#### put

```
public String put(String key, LocalDateTime value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDateTime`

**Returns**: `String`

#### put

```
public String put(String key, java.util.Date value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.util.Date`

**Returns**: `String`

#### put

```
public String put(String key, java.sql.Timestamp value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.sql.Timestamp`

**Returns**: `String`

#### put

```
public String put(String key, boolean value)
```

**Parameters**:
- `key`: `String`
- `value`: `boolean`

**Returns**: `String`

#### putForce

```
public String putForce(String key, String value)
```

**Parameters**:
- `key`: `String`
- `value`: `String`

**Returns**: `String`

#### putForce

```
public String putForce(String key, BigDecimal value)
```

**Parameters**:
- `key`: `String`
- `value`: `BigDecimal`

**Returns**: `String`

#### putForce

```
public String putForce(String key, int value)
```

**Parameters**:
- `key`: `String`
- `value`: `int`

**Returns**: `String`

#### putForce

```
public String putForce(String key, long value)
```

**Parameters**:
- `key`: `String`
- `value`: `long`

**Returns**: `String`

#### putForce

```
public String putForce(String key, LocalDate value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDate`

**Returns**: `String`

#### putForce

```
public String putForce(String key, LocalDateTime value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDateTime`

**Returns**: `String`

#### putForce

```
public String putForce(String key, java.util.Date value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.util.Date`

**Returns**: `String`

#### putForce

```
public String putForce(String key, java.sql.Timestamp value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.sql.Timestamp`

**Returns**: `String`

#### putForce

```
public String putForce(String key, boolean value)
```

**Parameters**:
- `key`: `String`
- `value`: `boolean`

**Returns**: `String`

#### putAll

```
public void putAll(Map<? extends String, ? extends String> map)
```

**Parameters**:
- `map`: `Map<? extends String, ? extends String>`

#### putAllForce

```
public void putAllForce(Map<? extends String, ? extends String> map)
```

**Parameters**:
- `map`: `Map<? extends String, ? extends String>`

#### size

```
public int size()
```

**Returns**: `int`

#### isEmpty

```
public boolean isEmpty()
```

**Returns**: `boolean`

#### containsKey

```
public boolean containsKey(Object key)
```

**Parameters**:
- `key`: `Object`

**Returns**: `boolean`

#### containsValue

```
public boolean containsValue(Object value)
```

**Parameters**:
- `value`: `Object`

**Returns**: `boolean`

#### remove

```
public String remove(Object key)
```

**Parameters**:
- `key`: `Object`

**Returns**: `String`

#### clear

```
public void clear()
```

#### keySet

```
public Set<String> keySet()
```

**Returns**: `Set<String>`

#### values

```
public Collection<String> values()
```

**Returns**: `Collection<String>`

#### entrySet

```
public Set<Entry<String, String>> entrySet()
```

**Returns**: `Set<Entry<String, String>>`

#### equals

```
public boolean equals(Object obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### hashCode

```
public int hashCode()
```

**Returns**: `int`

#### session

```
public AbstractIoTypeMap session()
```

**Returns**: `AbstractIoTypeMap`

#### getList

```
public List<String> getList(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `List<String>`

#### putList

```
public List<String> putList(String key, List<String> list)
```

**Parameters**:
- `key`: `String`
- `list`: `List<String>`

**Returns**: `List<String>`

#### putListForce

```
public List<String> putListForce(String key, List<String> list)
```

**Parameters**:
- `key`: `String`
- `list`: `List<String>`

**Returns**: `List<String>`

#### getNest

```
public Io getNest(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `Io`

#### putNest

```
public Io putNest(String key, Io nest)
```

**Parameters**:
- `key`: `String`
- `nest`: `Io`

**Returns**: `Io`

#### putNestForce

```
public Io putNestForce(String key, Io nest)
```

**Parameters**:
- `key`: `String`
- `nest`: `Io`

**Returns**: `Io`

#### getRows

```
public IoRows getRows(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `IoRows`

#### putRows

```
public IoRows putRows(String key, Collection<? extends Map<? extends String, ? extends String>> rows)
```

**Parameters**:
- `key`: `String`
- `rows`: `Collection<? extends Map<? extends String, ? extends String>>`

**Returns**: `IoRows`

#### putRowsForce

```
public IoRows putRowsForce(String key, Collection<? extends IoItems> rows)
```

**Parameters**:
- `key`: `String`
- `rows`: `Collection<? extends IoItems>`

**Returns**: `IoRows`

#### getArys

```
public IoArrays getArys(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `IoArrays`

#### putArys

```
public IoArrays putArys(String key, Collection<? extends List<String>> arys)
```

**Parameters**:
- `key`: `String`
- `arys`: `Collection<? extends List<String>>`

**Returns**: `IoArrays`

#### putArysForce

```
public IoArrays putArysForce(String key, Collection<? extends List<String>> arys)
```

**Parameters**:
- `key`: `String`
- `arys`: `Collection<? extends List<String>>`

**Returns**: `IoArrays`

#### createJson

```
public String createJson()
```

**Returns**: `String`

#### createJsonWithMsg

```
public String createJsonWithMsg(Map<String, String> msgTextMap)
```

**Parameters**:
- `msgTextMap`: `Map<String, String>`

**Returns**: `String`

#### putAll

```
public void putAll(Io map)
```

**Parameters**:
- `map`: `Io`

#### putAllForce

```
public void putAllForce(Io map)
```

**Parameters**:
- `map`: `Io`

#### putAllByUrlParam

```
public int putAllByUrlParam(String url)
```

**Parameters**:
- `url`: `String`

**Returns**: `int`

#### putAllByJson

```
public int putAllByJson(String json)
```

**Parameters**:
- `json`: `String`

**Returns**: `int`

#### containsKeyList

```
public boolean containsKeyList(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### removeList

```
public List<String> removeList(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `List<String>`

#### containsKeyNest

```
public boolean containsKeyNest(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### removeNest

```
public Io removeNest(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `Io`

#### containsKeyRows

```
public boolean containsKeyRows(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### removeRows

```
public IoRows removeRows(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `IoRows`

#### containsKeyArys

```
public boolean containsKeyArys(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### removeArys

```
public IoArrays removeArys(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `IoArrays`

#### putMsg

```
public void putMsg(MsgType type, String msgId)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`

#### putMsg

```
public void putMsg(MsgType type, String msgId, String[] replaceVals)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`
- `replaceVals`: `String[]`

#### putMsg

```
public void putMsg(MsgType type, String msgId, String itemId)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`
- `itemId`: `String`

#### putMsg

```
public void putMsg(MsgType type, String msgId, String[] replaceVals, String itemId)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`
- `replaceVals`: `String[]`
- `itemId`: `String`

#### putMsg

```
public void putMsg(MsgType type, String msgId, String itemId, String rowListId, int rowIndex)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`
- `itemId`: `String`
- `rowListId`: `String`
- `rowIndex`: `int`

#### putMsg

```
public void putMsg(MsgType type, String msgId, String[] replaceVals, String itemId, String rowListId, int rowIndex)
```

**Parameters**:
- `type`: `MsgType`
- `msgId`: `String`
- `replaceVals`: `String[]`
- `itemId`: `String`
- `rowListId`: `String`
- `rowIndex`: `int`

#### hasMsg

```
public boolean hasMsg()
```

**Returns**: `boolean`

#### hasErrorMsg

```
public boolean hasErrorMsg()
```

**Returns**: `boolean`

#### clearMsg

```
public void clearMsg()
```

## IoArrays

**Package**: com.onepg.util

## IoItems

**Package**: com.onepg.util

### Methods

#### getString

```
public String getString(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### getStringOrDefault

```
public String getStringOrDefault(String key, String notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `String`

**Returns**: `String`

#### getStringNullable

```
public String getStringNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### getStringNullableOrDefault

```
public String getStringNullableOrDefault(String key, String notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `String`

**Returns**: `String`

#### getBigDecimal

```
public BigDecimal getBigDecimal(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `BigDecimal`

#### getBigDecimalOrDefault

```
public BigDecimal getBigDecimalOrDefault(String key, BigDecimal notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `BigDecimal`

**Returns**: `BigDecimal`

#### getBigDecimalNullable

```
public BigDecimal getBigDecimalNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `BigDecimal`

#### getBigDecimalNullableOrDefault

```
public BigDecimal getBigDecimalNullableOrDefault(String key, BigDecimal notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `BigDecimal`

**Returns**: `BigDecimal`

#### getInt

```
public int getInt(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `int`

#### getIntOrDefault

```
public int getIntOrDefault(String key, int notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `int`

**Returns**: `int`

#### getLong

```
public long getLong(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `long`

#### getLongOrDefault

```
public long getLongOrDefault(String key, long notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `long`

**Returns**: `long`

#### getDateNullable

```
public LocalDate getDateNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `LocalDate`

#### getDateNullableOrDefault

```
public LocalDate getDateNullableOrDefault(String key, LocalDate notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `LocalDate`

**Returns**: `LocalDate`

#### getDateTimeNullable

```
public LocalDateTime getDateTimeNullable(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `LocalDateTime`

#### getDateTimeNullableOrDefault

```
public LocalDateTime getDateTimeNullableOrDefault(String key, LocalDateTime notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `LocalDateTime`

**Returns**: `LocalDateTime`

#### getBoolean

```
public boolean getBoolean(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### getBooleanOrDefault

```
public boolean getBooleanOrDefault(String key, boolean notExistsValue)
```

**Parameters**:
- `key`: `String`
- `notExistsValue`: `boolean`

**Returns**: `boolean`

#### putNull

```
public String putNull(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### putNullForce

```
public String putNullForce(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `String`

#### put

```
public String put(String key, String value)
```

**Parameters**:
- `key`: `String`
- `value`: `String`

**Returns**: `String`

#### put

```
public String put(String key, BigDecimal value)
```

**Parameters**:
- `key`: `String`
- `value`: `BigDecimal`

**Returns**: `String`

#### put

```
public String put(String key, int value)
```

**Parameters**:
- `key`: `String`
- `value`: `int`

**Returns**: `String`

#### put

```
public String put(String key, long value)
```

**Parameters**:
- `key`: `String`
- `value`: `long`

**Returns**: `String`

#### put

```
public String put(String key, LocalDate value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDate`

**Returns**: `String`

#### put

```
public String put(String key, LocalDateTime value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDateTime`

**Returns**: `String`

#### put

```
public String put(String key, java.util.Date value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.util.Date`

**Returns**: `String`

#### put

```
public String put(String key, java.sql.Timestamp value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.sql.Timestamp`

**Returns**: `String`

#### put

```
public String put(String key, boolean value)
```

**Parameters**:
- `key`: `String`
- `value`: `boolean`

**Returns**: `String`

#### putForce

```
public String putForce(String key, String value)
```

**Parameters**:
- `key`: `String`
- `value`: `String`

**Returns**: `String`

#### putForce

```
public String putForce(String key, BigDecimal value)
```

**Parameters**:
- `key`: `String`
- `value`: `BigDecimal`

**Returns**: `String`

#### putForce

```
public String putForce(String key, int value)
```

**Parameters**:
- `key`: `String`
- `value`: `int`

**Returns**: `String`

#### putForce

```
public String putForce(String key, long value)
```

**Parameters**:
- `key`: `String`
- `value`: `long`

**Returns**: `String`

#### putForce

```
public String putForce(String key, LocalDate value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDate`

**Returns**: `String`

#### putForce

```
public String putForce(String key, LocalDateTime value)
```

**Parameters**:
- `key`: `String`
- `value`: `LocalDateTime`

**Returns**: `String`

#### putForce

```
public String putForce(String key, java.util.Date value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.util.Date`

**Returns**: `String`

#### putForce

```
public String putForce(String key, java.sql.Timestamp value)
```

**Parameters**:
- `key`: `String`
- `value`: `java.sql.Timestamp`

**Returns**: `String`

#### putForce

```
public String putForce(String key, boolean value)
```

**Parameters**:
- `key`: `String`
- `value`: `boolean`

**Returns**: `String`

#### putAll

```
public void putAll(Map<? extends String, ? extends String> map)
```

**Parameters**:
- `map`: `Map<? extends String, ? extends String>`

#### putAllForce

```
public void putAllForce(Map<? extends String, ? extends String> map)
```

**Parameters**:
- `map`: `Map<? extends String, ? extends String>`

#### size

```
public int size()
```

**Returns**: `int`

#### isEmpty

```
public boolean isEmpty()
```

**Returns**: `boolean`

#### containsKey

```
public boolean containsKey(Object key)
```

**Parameters**:
- `key`: `Object`

**Returns**: `boolean`

#### containsValue

```
public boolean containsValue(Object value)
```

**Parameters**:
- `value`: `Object`

**Returns**: `boolean`

#### remove

```
public String remove(Object key)
```

**Parameters**:
- `key`: `Object`

**Returns**: `String`

#### clear

```
public void clear()
```

#### keySet

```
public Set<String> keySet()
```

**Returns**: `Set<String>`

#### values

```
public Collection<String> values()
```

**Returns**: `Collection<String>`

#### entrySet

```
public Set<Entry<String, String>> entrySet()
```

**Returns**: `Set<Entry<String, String>>`

#### equals

```
public boolean equals(Object obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### hashCode

```
public int hashCode()
```

**Returns**: `int`

#### putAllByBatParam

```
public int putAllByBatParam(String[] args)
```

**Parameters**:
- `args`: `String[]`

**Returns**: `int`

## IoRows

**Package**: com.onepg.util

### Methods

#### getBeginRowNo

```
public int getBeginRowNo()
```

**Returns**: `int`

#### setBeginRowNo

```
public void setBeginRowNo(int beginRowNo)
```

**Parameters**:
- `beginRowNo`: `int`

#### getEndRowNo

```
public int getEndRowNo()
```

**Returns**: `int`

#### setEndRowNo

```
public void setEndRowNo(int endRowNo)
```

**Parameters**:
- `endRowNo`: `int`

#### isLimitOver

```
public boolean isLimitOver()
```

**Returns**: `boolean`

#### setLimitOver

```
public void setLimitOver(boolean limitOver)
```

**Parameters**:
- `limitOver`: `boolean`

## LogUtil

**Package**: com.onepg.util

### Methods

#### newLogWriter

```
public LogWriter newLogWriter(Class<?> cls)
```

**Parameters**:
- `cls`: `Class<?>`

**Returns**: `LogWriter`

#### newLogWriter

```
public LogWriter newLogWriter(Class<?> cls, String traceCode)
```

**Parameters**:
- `cls`: `Class<?>`
- `traceCode`: `String`

**Returns**: `LogWriter`

#### stdout

```
public void stdout(String... msgs)
```

**Parameters**:
- `msgs`: `String...`

#### stdout

```
public void stdout(Throwable e, String... msgs)
```

**Parameters**:
- `e`: `Throwable`
- `msgs`: `String...`

#### javaInfoStdout

```
public void javaInfoStdout()
```

#### isDevelopMode

```
public boolean isDevelopMode()
```

**Returns**: `boolean`

#### getStackTrace

```
public String getStackTrace(String lineSep, Throwable e)
```

**Parameters**:
- `lineSep`: `String`
- `e`: `Throwable`

**Returns**: `String`

#### joinKeyVal

```
public String joinKeyVal(Object... keyVal)
```

**Parameters**:
- `keyVal`: `Object...`

**Returns**: `String`

#### joinValues

```
public String joinValues(String... values)
```

**Parameters**:
- `values`: `String...`

**Returns**: `String`

#### join

```
public String join(String[] values)
```

**Parameters**:
- `values`: `String[]`

**Returns**: `String`

#### join

```
public String join(List<?> values)
```

**Parameters**:
- `values`: `List<?>`

**Returns**: `String`

#### join

```
public String join(Map<String, T> map)
```

**Parameters**:
- `map`: `Map<String, T>`

**Returns**: `String`

#### replaceNullValue

```
public String replaceNullValue(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### formatDaysTime

```
public String formatDaysTime(long msec)
```

**Parameters**:
- `msec`: `long`

**Returns**: `String`

#### getClassNameAndLineNo

```
public String getClassNameAndLineNo(Class<?> callerClass)
```

**Parameters**:
- `callerClass`: `Class<?>`

**Returns**: `String`

## LogWriter

**Package**: com.onepg.util

### Methods

#### flush

```
public void flush()
```

#### error

```
public void error(Throwable e, String msg)
```

**Parameters**:
- `e`: `Throwable`
- `msg`: `String`

#### error

```
public void error(Throwable e)
```

**Parameters**:
- `e`: `Throwable`

#### error

```
public void error(String msg)
```

**Parameters**:
- `msg`: `String`

#### info

```
public void info(String msg)
```

**Parameters**:
- `msg`: `String`

#### begin

```
public void begin()
```

#### end

```
public void end()
```

#### end

```
public void end(int exitStatus)
```

**Parameters**:
- `exitStatus`: `int`

#### develop

```
public void develop(String msg)
```

**Parameters**:
- `msg`: `String`

#### isDevelopMode

```
public boolean isDevelopMode()
```

**Returns**: `boolean`

#### startWatch

```
public void startWatch()
```

#### stopWatch

```
public void stopWatch()
```

## PropertiesUtil

**Package**: com.onepg.util

### Methods

#### getFrameworkProps

```
public IoItems getFrameworkProps(FwPropertiesName propFileName)
```

**Parameters**:
- `propFileName`: `FwPropertiesName`

**Returns**: `IoItems`

#### isWindowsOs

```
public boolean isWindowsOs()
```

**Returns**: `boolean`

## ResourcesUtil

**Package**: com.onepg.util

### Methods

#### getJson

```
public IoItems getJson(FwResourceName resourceName)
```

**Parameters**:
- `resourceName`: `FwResourceName`

**Returns**: `IoItems`

#### getJson

```
public IoItems getJson(String fileName)
```

**Parameters**:
- `fileName`: `String`

**Returns**: `IoItems`

## SqlBean

**Package**: com.onepg.db

## SqlBuilder

**Package**: com.onepg.db

### Methods

#### addSqlBuilder

```
public void addSqlBuilder(SqlBuilder sb)
```

**Parameters**:
- `sb`: `SqlBuilder`

#### addQuery

```
public SqlBuilder addQuery(String sql, Object... params)
```

**Parameters**:
- `sql`: `String`
- `params`: `Object...`

**Returns**: `SqlBuilder`

#### addParams

```
public SqlBuilder addParams(Object... params)
```

**Parameters**:
- `params`: `Object...`

**Returns**: `SqlBuilder`

#### addListInBind

```
public SqlBuilder addListInBind(List<Object> params)
```

**Parameters**:
- `params`: `List<Object>`

**Returns**: `SqlBuilder`

#### addQueryIfNotBlankParameter

```
public SqlBuilder addQueryIfNotBlankParameter(String sql, Object param)
```

**Parameters**:
- `sql`: `String`
- `param`: `Object`

**Returns**: `SqlBuilder`

#### addQnotB

```
public SqlBuilder addQnotB(String sql, Object param)
```

**Parameters**:
- `sql`: `String`
- `param`: `Object`

**Returns**: `SqlBuilder`

#### delLastChar

```
public SqlBuilder delLastChar()
```

**Returns**: `SqlBuilder`

#### delLastChar

```
public SqlBuilder delLastChar(int deleteCharCount)
```

**Parameters**:
- `deleteCharCount`: `int`

**Returns**: `SqlBuilder`

## SqlConst

**Package**: com.onepg.db

### Methods

#### begin

```
public SqlConstBuilder begin()
```

**Returns**: `SqlConstBuilder`

#### bind

```
public SqlBean bind(AbstractIoTypeMap params)
```

**Parameters**:
- `params`: `AbstractIoTypeMap`

**Returns**: `SqlBean`

#### end

```
public SqlConst end()
```

**Returns**: `SqlConst`

#### addQuery

```
public SqlConstBuilder addQuery(String sql)
```

**Parameters**:
- `sql`: `String`

**Returns**: `SqlConstBuilder`

#### addQuery

```
public SqlConstBuilder addQuery(String sql, String itemName, BindType bindType)
```

**Parameters**:
- `sql`: `String`
- `itemName`: `String`
- `bindType`: `BindType`

**Returns**: `SqlConstBuilder`

## SqlResultSet

**Package**: com.onepg.db

### Methods

#### iterator

```
public Iterator<IoItems> iterator()
```

**Returns**: `Iterator<IoItems>`

#### close

```
public void close()
```

#### isExists

```
public boolean isExists()
```

**Returns**: `boolean`

#### getReadedCount

```
public int getReadedCount()
```

**Returns**: `int`

#### isReadedEndRow

```
public boolean isReadedEndRow()
```

**Returns**: `boolean`

#### getItemNames

```
public String[] getItemNames()
```

**Returns**: `String[]`

#### hasNext

```
public boolean hasNext()
```

**Returns**: `boolean`

#### next

```
public IoItems next()
```

**Returns**: `IoItems`

## SqlUtil

**Package**: com.onepg.db

### Methods

#### selectOneExists

```
public IoItems selectOneExists(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `IoItems`

#### selectOne

```
public IoItems selectOne(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `IoItems`

#### selectOneMultiIgnore

```
public IoItems selectOneMultiIgnore(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `IoItems`

#### select

```
public SqlResultSet select(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `SqlResultSet`

#### selectFetchAll

```
public SqlResultSet selectFetchAll(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `SqlResultSet`

#### selectBulk

```
public IoRows selectBulk(Connection conn, SqlBean sb, int limitCount)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`
- `limitCount`: `int`

**Returns**: `IoRows`

#### selectBulkAll

```
public IoRows selectBulkAll(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `IoRows`

#### insertOne

```
public boolean insertOne(Connection conn, String tableName, AbstractIoTypeMap params)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`

**Returns**: `boolean`

#### insertOne

```
public boolean insertOne(Connection conn, String tableName, AbstractIoTypeMap params, String tsItem)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `tsItem`: `String`

**Returns**: `boolean`

#### updateOne

```
public boolean updateOne(Connection conn, String tableName, AbstractIoTypeMap params, String[] keyItems)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `keyItems`: `String[]`

**Returns**: `boolean`

#### updateOne

```
public boolean updateOne(Connection conn, String tableName, AbstractIoTypeMap params, String[] keyItems, String tsItem)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `keyItems`: `String[]`
- `tsItem`: `String`

**Returns**: `boolean`

#### updateOneByPkey

```
public boolean updateOneByPkey(Connection conn, String tableName, AbstractIoTypeMap params)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`

**Returns**: `boolean`

#### updateOneByPkey

```
public boolean updateOneByPkey(Connection conn, String tableName, AbstractIoTypeMap params, String tsItem)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `tsItem`: `String`

**Returns**: `boolean`

#### update

```
public int update(Connection conn, String tableName, AbstractIoTypeMap params, String[] whereItems)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `whereItems`: `String[]`

**Returns**: `int`

#### updateAll

```
public int updateAll(Connection conn, String tableName, AbstractIoTypeMap params)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`

**Returns**: `int`

#### deleteOne

```
public boolean deleteOne(Connection conn, String tableName, AbstractIoTypeMap params, String[] keyItems)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `keyItems`: `String[]`

**Returns**: `boolean`

#### deleteOne

```
public boolean deleteOne(Connection conn, String tableName, AbstractIoTypeMap params, String[] keyItems, String tsItem)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `keyItems`: `String[]`
- `tsItem`: `String`

**Returns**: `boolean`

#### deleteOneByPkey

```
public boolean deleteOneByPkey(Connection conn, String tableName, AbstractIoTypeMap params)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`

**Returns**: `boolean`

#### deleteOneByPkey

```
public boolean deleteOneByPkey(Connection conn, String tableName, AbstractIoTypeMap params, String tsItem)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `tsItem`: `String`

**Returns**: `boolean`

#### delete

```
public int delete(Connection conn, String tableName, AbstractIoTypeMap params, String[] whereItems)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`
- `params`: `AbstractIoTypeMap`
- `whereItems`: `String[]`

**Returns**: `int`

#### deleteAll

```
public int deleteAll(Connection conn, String tableName)
```

**Parameters**:
- `conn`: `Connection`
- `tableName`: `String`

**Returns**: `int`

#### executeOne

```
public boolean executeOne(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `boolean`

#### execute

```
public int execute(Connection conn, SqlBean sb)
```

**Parameters**:
- `conn`: `Connection`
- `sb`: `SqlBean`

**Returns**: `int`

#### getToday

```
public String getToday(Connection conn)
```

**Parameters**:
- `conn`: `Connection`

**Returns**: `String`

## TxtReader

**Package**: com.onepg.util

### Methods

#### iterator

```
public Iterator<String> iterator()
```

**Returns**: `Iterator<String>`

#### close

```
public void close()
```

#### getReadedCount

```
public int getReadedCount()
```

**Returns**: `int`

#### isReadedEndRow

```
public boolean isReadedEndRow()
```

**Returns**: `boolean`

#### skip

```
public boolean skip()
```

**Returns**: `boolean`

#### skip

```
public boolean skip(int count)
```

**Parameters**:
- `count`: `int`

**Returns**: `boolean`

#### getFirstLine

```
public String getFirstLine()
```

**Returns**: `String`

#### hasNext

```
public boolean hasNext()
```

**Returns**: `boolean`

#### next

```
public String next()
```

**Returns**: `String`

## TxtWriter

**Package**: com.onepg.util

### Methods

#### close

```
public void close()
```

#### println

```
public void println(String line)
```

**Parameters**:
- `line`: `String`

#### flush

```
public void flush()
```

#### getFilePath

```
public String getFilePath()
```

**Returns**: `String`

#### getLineCount

```
public long getLineCount()
```

**Returns**: `long`

## ValUtil

**Package**: com.onepg.util

### Methods

#### isNull

```
public boolean isNull(Object obj)
```

**Parameters**:
- `obj`: `Object`

**Returns**: `boolean`

#### isBlank

```
public boolean isBlank(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `boolean`

#### isEmpty

```
public boolean isEmpty(Object[] values)
```

**Parameters**:
- `values`: `Object[]`

**Returns**: `boolean`

#### isEmpty

```
public boolean isEmpty(List<?> list)
```

**Parameters**:
- `list`: `List<?>`

**Returns**: `boolean`

#### isEmpty

```
public boolean isEmpty(Map<?, ?> map)
```

**Parameters**:
- `map`: `Map<?, ?>`

**Returns**: `boolean`

#### isValidIoKey

```
public boolean isValidIoKey(String key)
```

**Parameters**:
- `key`: `String`

**Returns**: `boolean`

#### validateIoKey

```
public void validateIoKey(String key)
```

**Parameters**:
- `key`: `String`

#### nvl

```
public String nvl(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### nvl

```
public String nvl(String value, String nullDefault)
```

**Parameters**:
- `value`: `String`
- `nullDefault`: `String`

**Returns**: `String`

#### nvl

```
public BigDecimal nvl(BigDecimal value)
```

**Parameters**:
- `value`: `BigDecimal`

**Returns**: `BigDecimal`

#### bvl

```
public String bvl(String value, String blankDefault)
```

**Parameters**:
- `value`: `String`
- `blankDefault`: `String`

**Returns**: `String`

#### join

```
public String join(String joint, String... values)
```

**Parameters**:
- `joint`: `String`
- `values`: `String...`

**Returns**: `String`

#### join

```
public String join(String joint, List<String> list)
```

**Parameters**:
- `joint`: `String`
- `list`: `List<String>`

**Returns**: `String`

#### join

```
public String join(String joint, Set<String> list)
```

**Parameters**:
- `joint`: `String`
- `list`: `Set<String>`

**Returns**: `String`

#### split

```
public String[] split(String value, String sep)
```

**Parameters**:
- `value`: `String`
- `sep`: `String`

**Returns**: `String[]`

#### splitReg

```
public String[] splitReg(String value, String sep)
```

**Parameters**:
- `value`: `String`
- `sep`: `String`

**Returns**: `String[]`

#### splitReg

```
public String[] splitReg(String value, String sep, int limitLength)
```

**Parameters**:
- `value`: `String`
- `sep`: `String`
- `limitLength`: `int`

**Returns**: `String[]`

#### equals

```
public boolean equals(String str1, String str2)
```

**Parameters**:
- `str1`: `String`
- `str2`: `String`

**Returns**: `boolean`

#### equals

```
public boolean equals(BigDecimal dec1, BigDecimal dec2)
```

**Parameters**:
- `dec1`: `BigDecimal`
- `dec2`: `BigDecimal`

**Returns**: `boolean`

#### substring

```
public String substring(String value, Integer beginIndex)
```

**Parameters**:
- `value`: `String`
- `beginIndex`: `Integer`

**Returns**: `String`

#### substring

```
public String substring(String value, Integer beginIndex, Integer endIndex)
```

**Parameters**:
- `value`: `String`
- `beginIndex`: `Integer`
- `endIndex`: `Integer`

**Returns**: `String`

#### isAlphabetNumber

```
public boolean isAlphabetNumber(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `boolean`

#### isNumber

```
public boolean isNumber(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `boolean`

#### isNumber

```
public boolean isNumber(String value, boolean minusNg, boolean decNg)
```

**Parameters**:
- `value`: `String`
- `minusNg`: `boolean`
- `decNg`: `boolean`

**Returns**: `boolean`

#### checkLength

```
public boolean checkLength(String value, int len)
```

**Parameters**:
- `value`: `String`
- `len`: `int`

**Returns**: `boolean`

#### checkLengthNumber

```
public boolean checkLengthNumber(String value, int intPartLen, int decPartLen)
```

**Parameters**:
- `value`: `String`
- `intPartLen`: `int`
- `decPartLen`: `int`

**Returns**: `boolean`

#### isDate

```
public boolean isDate(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `boolean`

#### isTrue

```
public boolean isTrue(String val)
```

**Parameters**:
- `val`: `String`

**Returns**: `boolean`

#### dateToLocalDate

```
public LocalDate dateToLocalDate(java.util.Date date)
```

**Parameters**:
- `date`: `java.util.Date`

**Returns**: `LocalDate`

#### trimDq

```
public String trimDq(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### trimBothEnds

```
public String trimBothEnds(String value, char prefix, char suffix)
```

**Parameters**:
- `value`: `String`
- `prefix`: `char`
- `suffix`: `char`

**Returns**: `String`

#### trimBothEnds

```
public String trimBothEnds(String value, String prefix, String suffix)
```

**Parameters**:
- `value`: `String`
- `prefix`: `String`
- `suffix`: `String`

**Returns**: `String`

#### trimZenkakuSpace

```
public String trimZenkakuSpace(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### trimLeftZero

```
public String trimLeftZero(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### paddingLeftZero

```
public String paddingLeftZero(String value, int digit)
```

**Parameters**:
- `value`: `String`
- `digit`: `int`

**Returns**: `String`

#### paddingLeftZero

```
public String paddingLeftZero(int value, int digit)
```

**Parameters**:
- `value`: `int`
- `digit`: `int`

**Returns**: `String`

#### deleteLastChar

```
public void deleteLastChar(StringBuilder sb)
```

**Parameters**:
- `sb`: `StringBuilder`

#### deleteLastChar

```
public void deleteLastChar(StringBuilder sb, int length)
```

**Parameters**:
- `sb`: `StringBuilder`
- `length`: `int`

#### urlEncode

```
public String urlEncode(String url)
```

**Parameters**:
- `url`: `String`

**Returns**: `String`

#### urlDecode

```
public String urlDecode(String url)
```

**Parameters**:
- `url`: `String`

**Returns**: `String`

#### jsonEscape

```
public String jsonEscape(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### jsonUnEscape

```
public String jsonUnEscape(String value)
```

**Parameters**:
- `value`: `String`

**Returns**: `String`

#### getSequenceCode

```
public String getSequenceCode()
```

**Returns**: `String`

