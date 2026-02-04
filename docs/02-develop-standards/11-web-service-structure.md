# Web Service Structure Standard

## Overview
- Defines standard rules for web services (web server processing).
- By adhering to this standard and this framework, aims to unify program code and improve development and maintenance efficiency.
- This document explains using the following samples:
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## Prerequisites
- Use Java 11 or higher.
- Requests to web services SHALL be in JSON format or URL parameters.
- Responses to web pages SHALL be in JSON format only.
- HTML SHALL be retrieved only by downloading static files, and do not generate HTML dynamically in web services.

<!-- AI_SKIP_START -->
## Framework Features and Design Philosophy

This framework eliminates backend development complexity through a simple JSON-centric architecture.

### Communication with Browser is JSON Only

**Benefits**:
- Protocol is simple.
- Frontend and backend are completely separated.
- API development and screen development use the same mechanism.
- Testing is possible with JavaScript only.
- Integration with mobile apps is easy.

**Implementation**:
```
Browser              Server
  │                   │
  │------ JSON ------>│
  │           Web service processing
  │<------ JSON ------│
```

### URL Routing

**URL directly becomes the web service to execute**:
```
URL: http://localhost:8080/services/exmodule/ExampleListSearch

↓ Automatic mapping

Executing web service class: com.example.app.service.exmodule.ExampleListSearch
```

**Implementation example**:
```java
package com.example.app.service.exmodule;

public class ExampleListSearch extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // Processing implementation
  }
}
```

**Benefits**:
- Web service class name can be clearly identified from URL.
- Routing configuration is not required.
- Annotations are not required.
- Provides clarity without confusion.
<!-- AI_SKIP_END -->

## File Structure

### Directory Structure
- [※] indicates parts that can be specified in configuration files.
- Module Java packages store multiple (or single) functionally cohesive Java classes.
- Module Java package names MUST be the same as module directory names under web page directory, and store web service Java classes used by web pages of that module (create web service Java packages in one-to-one correspondence with web page directories).

```
[project root]/                       # Project root: In most cases, becomes the project name.
├── config/                        # Configuration file storage directory [※Configure directory path]
├── resources/                     # Resource file storage directory
├── src/                           # Java source file storage directory
│   ├── [project domain]/         # Project domain Java package: May have multiple hierarchies. [Example] "com/example/"
│   │   ├── app/                 # Module Java package: Branch point from common component Java package
│   │   │   └── service/        # Web service Java package [※Configure package name and context name]
│   │   │        └── [module]/  # Module-level Java package: Stores web service Java classes.
│   │   └── util/                # Common component Java package: Stores common components used from web service Java classes. [Package name util is an example]
│   └── com/onpg/                 # This framework domain Java package
├── classes/                       # Java class compilation destination directory
└── lib/                           # Java library file storage directory
```

### File Creation Unit
- Create one web service Java class for each event processing.

### Configuration Files
- The following configuration files are used by this framework, so for individual functions, prepare configuration files with names other than the following.
- Individual function configuration files MAY be multiple, but configuration keys MUST be unique across all files.
- Configuration files other than `config.properties` can be placed in a directory changed from default, but `config.properties` MUST exist at `[application deployment directory]/config/config.properties`.

| Configuration File Name | Usage | Main Configuration Content |
|-|-|-|
| `config.properties` | Configuration directory specification | Specify when changing configuration directory from default. |
| `web.properties` | Web server | Configure port number, concurrent processing count, and web service Java package. | 
| `bat.properties` | Batch processing | Configure batch processing settings. |
| `db.properties` | Database connection | Configure database connection information. |
| `log.properties` | Logging | Configure log file output destination. |

<!-- AI_SKIP_START -->
**Individual function configuration file example**:

```properties
my.custom.key=value
```

**Retrieval method**:

```java
// Retrieve function configuration
String myValue = PropertiesUtil.MODULE_PROP_MAP.getString("my.custom.key");
```
<!-- AI_SKIP_END -->


### Resource Files
The following resource files are used by this framework, so for individual functions, add resource files with names other than the following.

| Resource File Name | Usage | Main Configuration Content |
|-|-|-|
| `msg.json` | Message text | Define error message text and other messages. |

## Web Service Structure

Explains web service implementation rules and main functions provided by this framework.

### Web Service Class Structure
- Web service classes inherit from `AbstractWebService` class or `AbstractDbAccessWebService` class, and have a structure using GoF Template Method pattern.
- The `io` instance argument of template method (method that requires implementation) `#doExecute` becomes the return value as is.
- `#doExecute` is executed with request from web page set in `io`.
- Set response to web page in `io`, and terminate `#doExecute` as return value. Request values become response as is unless changed or deleted.

### Type-safe Data Processing with Io Class

<!-- AI_SKIP_START -->
#### JSON and Map Mutual Conversion Mechanism
**Io class features**:
- Inherits from `Map<String, String>`
- JSON ⇔ `Io` object mutual conversion is possible
- Supports JSON hierarchical structure up to 3 levels
- Type-safe `get()` and `put()` methods
- Request and response are unified

**Sample code**:
```java
// Request JSON is automatically converted to Io object by framework
// (Following code implementation is not required)
// String reqJson = "{\"user_id\":\"U001\",\"income_am\":\"1200000\",\"birth_dt\":\"19870321\"}";
// Io io = new Io();
// io.putAllByJson(reqJson);

// Retrieve values type-safely
String userId = io.getString("user_id"); // "U001"
long incomeAm = io.getLong("income_am"); // 1200000
LocalDate birthDt = io.getDateNullable("birth_dt"); // Explicitly indicates possibility of null retrieval

// Set values without type specification (stored internally as String type)
io.put("user_nm", "Mike Davis");
io.put("income_am", 1230000); // Numbers are also converted to "1230000" internally

// Conversion to response JSON is automatically performed by framework
// (Following code implementation is not required)
// String resJson = io.createJson();
```
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### Request and Response Unified
**Can be handled with Io class only**:
```java
public class ExampleLoadName extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // Create SQL from request
    SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));

    // Execute database retrieval
    final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
    if (!ValUtil.isNull(row)) {
      // Set database retrieval result as response
      io.putAll(row);
    } else {
      // Set error message as response
      io.putMsg(MsgType.ERROR, "x0001"); // User information not found.
    }
    // io object becomes response as is
  }
}
```

**Benefits**:
- Request values become response as is unless changed.
- Input value echo back is performed automatically.
- Can reduce code amount.
<!-- AI_SKIP_END -->

### Database Access Processing
- When accessing database within processing, by inheriting from `AbstractDbAccessWebService` class, database connection can be retrieved from `#getDbConn` method anywhere within class during `#doExecute` method processing.
- For SQL execution, MUST use `SqlUtil` methods.
- Database connection used within `#doExecute` method is normally committed when `#doExecute` terminates normally.
- If error message is set in `io`, database is rolled back. Database is also rolled back when exception error occurs.

<!-- AI_SKIP_START -->
#### Transaction Management Mechanism
**Role of AbstractDbAccessWebService**:
```java
public abstract class AbstractDbAccessWebService extends AbstractWebService {
  
  @Override
  void execute(final Io io) throws Exception {
    // 1. Get DB connection from pool
    try (final Connection conn = DbUtil.getConnPooled(super.traceCode)) {
      this.dbConn = conn;
      
      // 2. Execute subclass doExecute()
      super.execute(io);
      
      // 3. Automatically commit if no error
      if (!io.hasErrorMsg()) {
        this.dbConn.commit();
      }
      // 4. Release DB connection when try-with-resources ends, automatic rollback on DB connection side
    } finally {
      this.dbConn = null;
    }
  }
}
```

**Usage example**:
```java
public class ExampleUpsert extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // Validation
    if (ValUtil.isBlank(io.getString("user_id"))) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
      return; // Error exists → automatic rollback
    }
    
    // Register header
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    // Register details (multiple rows)
    IoRows details = io.getRows("detail");
    for (IoItems detail : details) {
      detail.put("user_id", io.getString("user_id"));
      SqlUtil.insert(getDbConn(), "t_user_detail", detail);
    }
    
    // Success message
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    
    // Normal termination (no error) → automatic commit
  }
}
```

**Benefits**:
- No need to explicitly write commit/rollback.
- Forgetting rollback on error does not occur.
- Transaction boundary is clear (1 request = 1 transaction).
<!-- AI_SKIP_END -->

#### Dynamic SQL Building (SqlBuilder)
**Basic usage**:
```java
// Generate SQL builder
SqlBuilder sb = new SqlBuilder();

// Build SQL (method chaining possible)
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ").addQuery(" WHERE 1=1 ");

// Add condition only when value exists (skip when blank)
sb.addQnotB("   AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB("   AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
sb.addQnotB("   AND u.email LIKE ? || '%' ", io.getString("email"));

sb.addQuery(" ORDER BY u.user_id ");

// Execute database retrieval
IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

<!-- AI_SKIP_START -->
**Executed SQL** (When request user_id is "U001", user_nm is blank, email is "test"):
```sql
SELECT 
  u.user_id 
, u.user_nm 
, u.email 
 FROM t_user u 
 WHERE 1=1 
   AND u.user_id = ?         -- Parameter: "U001"
   AND u.email LIKE ? || '%' -- Parameter: "test"
 ORDER BY u.user_id
```
<!-- AI_SKIP_END -->

**Main methods**:
```java
// Add only SQL
sb.addQuery(" FROM t_user u ");
// Add SQL & mandatory parameter
sb.addQuery(" AND user_id = ? ", userId);

// Add SQL & parameter only when value is not blank
sb.addQnotB(" AND user_id = ? ", userId);

// Add only parameter
sb.addParams(userId);

// Merge other SqlBuilder
sb.addSqlBuilder(otherSb);
```

<!-- AI_SKIP_START -->
**Benefits**:
- Can build SQL and parameters simultaneously (logic lines are not separated).
- No need to concatenate SQL strings with conditional branching.
- SQL injection countermeasures are implemented.
- Debugging is easy (can check SQL with `SqlBuilder#toString()`).
<!-- AI_SKIP_END -->

#### Unified Interface for Text Files, Database Retrieval Results, and Screen Lists
**Item-level and row-level processing**:
```java
// List input from screen: Process screen list one row (IoItems) at a time
IoRows detail = io.getRows("detail");
for (IoItems row : detail) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
}

// SqlResultSet: Retrieve database results one row (IoItems) at a time
try (SqlResultSet rSet = SqlUtil.select(getDbConn(), sb)) {
  // Process one row at a time (Iterator pattern)
  for (IoItems row : rSet) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
  }
}

// TxtReader: Read text file one line (String) at a time
try (TxtReader reader = new TxtReader("/path/to/data.csv", ValUtil.UTF8)) {
  // Skip header row
  reader.skip();
  
  // Define key array
  String[] keys = {"user_id", "user_nm", "email"};
  
  // Process one row at a time (Iterator pattern)
  for (String line : reader) {
    // Set CSV row to IoItems
    IoItems row = new IoItems();
    row.putAllByCsvDq(keys, line); // Support CSV with double quotes
    
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
  }
}
```

<!-- AI_SKIP_START -->
**Benefits**:
- Screen list (`IoRows`), `SqlResultSet`, `TxtReader` all use the same loop processing.
- Memory efficient (processes one row at a time).
- Can safely process large amounts of data.
- Can access data type-safely with `IoItems`.
- Code is independent of data source (screen, DB, file).
<!-- AI_SKIP_END -->

### Message Display Mechanism
Add messages to the following file as needed. The message ID numbering rule below is an example.

**Resource file** (resources/msg.json):
```json
{
  "ev001": "{0} is required.",
  "ev011": "{0} must be alphanumeric only.",
  "ev012": "{0} must be numeric only.",
  "i0002": "Registered {0}.",
  "i0004": "Search result is {0} records."
}
```

**Set message on Java side**:
```java
// Error message (no field specification)
io.putMsg(MsgType.ERROR, "ev011", new String[]{"User ID"});
// → "User ID must be alphanumeric only."

// Error message (with field specification)
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
// → "User ID is required." + highlight user_id field

io.putMsg(MsgType.ERROR, "ev012", new String[]{"Annual Income"}, "income_am");
// → "Annual Income must be numeric only." + highlight income_am field

// Info message
io.putMsg(MsgType.INFO, "i0002", new String[]{"U001"});
// → "Registered U001."

// Multiple messages
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User Name"}, "user_nm");

// List part field message
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id", "detail", rowIdx);
```

**JSON output**:
```json
// When no field specification
{
  "user_id": "U001",
  "_msgs": [
    {
      "type": "error",
      "id": "ev011",
      "text": "User ID must be alphanumeric only."
    }
  ],
  "_has_err": true
}

// When field specification provided
{
  "user_id": "U001",
  "_msgs": [
    {
      "type": "error",
      "id": "ev001",
      "text": "User ID is required.",
      "item": "user_id"
    },
    {
      "type": "error", 
      "id": "ev001",
      "text": "User Name is required.",
      "item": "user_nm"
    }
  ],
  "_has_err": true
}
```

<!-- AI_SKIP_START -->
**Benefits**:
- Can centrally manage message text.
- Internationalization is easy.
- Can highlight display per field, and also supports list part fields.
- Highlight display CSS class is automatically applied.

**Note**:
- `_msgs` and `_has_err` are processed by framework, so do not use directly from application.
<!-- AI_SKIP_END -->

### Logging Processing
For log output, use `logger` instance that superclass `AbstractWebService` (including `AbstractDbAccessWebService`) has. Usage of log output is as follows.

| Usage | Method Used |
|-|-|
| Development debugging | `logger#develop` |
| Production info monitoring | `logger#info` |
| Production error monitoring | `logger#error` |
| Production concurrency monitoring | `logger#begin`, `logger#end` |
| Production performance monitoring | `logger#startWatch`, `logger#stopWatch` |

- When development debug output causes performance load, confirm in advance that debug log will be output with `logger#isDevelopMode` method.
```java
if (logger.isDevelopMode()) {
    logger.develop("Delete count. " + LogUtil.joinKeyVal("count", delCnt);
}
```

<!-- AI_SKIP_START -->
#### Log File Rotation
**Automatic rotation**:
- Switch log file per day
- Log level control

**Log output configuration** (log.properties):
```properties
develop.mode=true
default.inf.file=/tmp/logs/info.log
default.err.file=/tmp/logs/error.log
```

**Usage method**:
```java
// Development debug log
// Output only when develop.mode=true
logger.develop("DB retrieval condition: " + LogUtil.joinKeyVal("userId", userId));

// Info log
// Output to file path of default.inf.file
logger.info("Processing start: " + traceCode);

// Error log
// Output to file path of default.err.file
logger.error(exception, "Error occurred");

// Performance measurement
logger.startWatch();
// ... processing ...
logger.stopWatch(); // Automatically output elapsed time
```
<!-- AI_SKIP_END -->

### Bug Prevention Features

The `Io` class has functions to prevent bugs that commonly occur with general Map classes.

#### NULL-safe Handling
**Problem**: General Map returns `null` from `get()`
```java
// General Map
Map<String, String> map = new HashMap<>();
String value = map.get("key"); // Returns null → Cause of NullPointerException
```

**Solution**: Io class retrieves null consciously
```java
// Io class
Io io = new Io();

// Basic methods do not return null (return blank)
String value = io.getString("key"); // ""

// When judging null, explicitly specify
String value = io.getStringNullable("key"); // null
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Can prevent NullPointerException.
- Can make null handling conscious.
<!-- AI_SKIP_END -->

#### Type-safe Handling
**Problem**: Error in conversion from string to number
```java
// General Map
String str = map.get("age");
int age = Integer.parseInt(str); // Possibility of NumberFormatException
```

**Solution**: Type conversion methods
```java
// Io class
int age = io.getInt("age");  // Converts blank to zero, logs key and value on error for non-numeric
LocalDate birthDt = io.getDateNullable("birth_dt");   // Date format check
BigDecimal income_am = io.getBigDecimal("income_am"); // Converts blank to zero, preserves precision
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Can detect type conversion errors early.
- Can eliminate implicit type conversion.
- Can guarantee numeric precision with internal string storage.
<!-- AI_SKIP_END -->

#### Key Duplication Strict Check
**Problem**: Unintentional overwrite
```java
// General Map
map.put("user_id", "U001");
map.put("user_id", "U002"); // Overwritten (no warning)
```

**Solution**: Duplication error
```java
// Io class
io.put("user_id", "U001");
io.put("user_id", "U002"); // Basically overwrite is error and logs key

// When overwriting, do it intentionally
io.putForce("user_id", "U002"); // OK
```

#### Notes When Setting Database Retrieval Results to io
- When database retrieval results containing database items with the same name as items existing in request `io` are set as response with `io.putAll()`, a key duplication error occurs.
- Exclude items included in request `io` (such as primary keys and optimistic locking timestamps) from SELECT clause.
- When excluding from SELECT clause is difficult, set with `io.putAllForce()`.

**Correct example**:

```java
// Request: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_nm ");      // Retrieve items other than user_id, upd_ts
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // OK: SELECT clause does not include user_id, upd_ts
```

**Error example**:

```java
// Request: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");      // NG: Item exists in request
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(", u.upd_ts ");       // NG: Item exists in request
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // Error: user_id, upd_ts already exist in io
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Can detect key typos.
- Can prevent unintentional overwrite (treated as `final` declaration though it is a map).
- Can guarantee data integrity.
<!-- AI_SKIP_END -->

#### Non-existent Key Retrieval Error
**Problem**: Mistake due to typo
```java
// General Map
Map<String, String> map = new HashMap<>();
map.put("user_id", "U001");
String value = map.get("userid"); // null (does not notice typo)
```

**Solution**: Strict key check
```java
// Io class
io.put("user_id", "U001");
String value = io.getString("userid"); // Non-existent key is error and logs key

// When existence check is necessary
if (io.containsKey("userid")) {
  String value = io.getString("userid");
}

// Or use OrDefault method
String value = io.getStringOrDefault("userid", ""); // Non-existent key returns ""
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Can detect typos early.
- Can prevent key name mistakes (treated as undeclared though it is a map).
- Can reduce debugging time.
<!-- AI_SKIP_END -->

#### Deep Copy Safety
The `Io` class performs deep copy when storing and retrieving lists, nested maps, multi-row lists, and array lists. This guarantees the following safety:

<!-- AI_SKIP_START -->
- **Prevention of unintentional reference sharing**: Even if the original list or map stored is changed, `Io` internal data is not affected.
- **Independence of retrieved data**: Even if the retrieved list or map is changed, `Io` internal data is not affected.
- **Prevention of bugs in advance**: Can avoid unexpected side effects due to referencing the same data from multiple locations.
<!-- AI_SKIP_END -->

```java
// Deep copy on storage
List<String> srcList = new ArrayList<>(Arrays.asList("A", "B"));
io.putList("items", srcList);
srcList.add("C");  // Change original list
// io.getList("items") remains ["A", "B"] (no effect)

// Deep copy on retrieval
List<String> gotList = io.getList("items");
gotList.add("D");  // Change retrieved list
// io.getList("items") remains ["A", "B"] (no effect)
```

> **Note**: Due to deep copy, performance may be affected in processing that repeatedly stores and retrieves large data.

## Related Documents

- [Web Page Structure Standard (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [Event Coding Pattern](../02-develop-standards/21-event-coding-pattern.md)

