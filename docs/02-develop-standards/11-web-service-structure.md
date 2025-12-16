# Web Service Structure Standards

## Overview
- Defines standard rules for web services (web server processing).
- Adhering to these standards and this framework enables code standardization and improves development and maintenance efficiency.
- This document uses the following samples for explanation:
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## Prerequisites
- Use Java 11 or higher.
- Requests to web services are in JSON format or URL parameters.
- Responses to web pages are in JSON format only.
- HTML is obtained only through static file downloads; dynamic HTML generation by web services is not performed.

<!-- AI_SKIP_START -->
## Framework Features and Design Philosophy

This framework eliminates backend development complexity through a simple, JSON-centric architecture.

### Communication with Browser is JSON Only

**Advantages**:
- Protocol is simple.
- Frontend and backend are completely separated.
- API development and screen development use the same mechanism.
- Testing is possible with JavaScript alone.
- Integration with mobile apps is easy.

**Implementation**:
```
Browser             Server
  │                   │
  │------ JSON ------>│
  │　　　　　　 Web Service Processing
  │<------ JSON ------│
```

### URL Routing

**URL directly becomes the web service to execute**:
```
URL: http://localhost:8080/services/exmodule/ExampleListSearch

↓ Auto-mapping

Executed web service class: com.example.app.service.exmodule.ExampleListSearch
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

**Advantages**:
- Web service class name is clearly identifiable from URL.
- No routing configuration required.
- No annotations required.
- Clear and intuitive.
<!-- AI_SKIP_END -->

## File Structure

### Directory Structure
- [*] indicates items specified in configuration files.
- Store multiple Java classes (one or more) that are functionally related in a module Java package.
- Use the same name for the module Java package as the module directory under the web page directory, and store web service Java classes used by the web pages of that module (create one web service Java package for each web page directory in a 1:1 relationship).

```
[project root]/                       # Project root: In most cases, this is the project name.
├── config/                        # Configuration file directory [* Set directory path]
├── resources/                     # Resource file directory
├── src/                           # Java source file directory
│   ├── [project domain]/         # Project domain Java package: Can be multiple levels. [Example] "com/example/"
│   │   ├── app/                 # Application Java package: Branch point from common component Java package
│   │   │   └── service/        # Web service Java package [* Set package name and context name]
│   │   │        └── [module]/  # Module Java package: Stores web service Java classes.
│   │   └── util/                # Common component Java package: Stores common components used by web service Java classes. [Package name "util" is an example]
│   └── com/onpg/                 # Framework domain Java package
├── classes/                       # Java class compilation directory
└── lib/                           # Java library file directory
```

### File Creation Unit
- Create one web service Java class for each event processing.

### Configuration Files
- The following configuration files are used by this framework; for individual features, prepare configuration files with different file names.
- Multiple configuration files for individual features are allowed, but configuration keys must be unique across all files.
- Configuration files other than `config.properties` can be placed in directories changed from the default, but `config.properties` must always exist at `[application deployment directory]/config/config.properties`.

| Configuration File Name | Purpose | Main Settings |
|-|-|-|
| `config.properties` | Configuration directory specification | Specify when changing the configuration directory from the default. |
| `web.properties` | Web server | Set port number, concurrent processing count, and web service Java package. |
| `bat.properties` | Batch processing | Configure batch processing settings. |
| `db.properties` | Database connection | Set database connection information. |
| `log.properties` | Logging | Set log file output destination. |

<!-- AI_SKIP_START -->
**Configuration file example for individual features**:

```properties
my.custom.key=value
```

**Retrieval method**:

```java
// Get feature configuration
String myValue = PropertiesUtil.MODULE_PROP_MAP.getString("my.custom.key");
```
<!-- AI_SKIP_END -->


### Resource Files
The following resource files are used by this framework; for individual features, add resource files with different file names.

| Resource File Name | Purpose | Main Settings |
|-|-|-|
| `msg.json` | Message text | Define message text such as error messages. |

## Web Service Configuration

Explains the implementation rules for web services and the main features provided by the framework.

### Web Service Class Structure
- Web service classes extend the `AbstractWebService` class or `AbstractDbAccessWebService` class, using a structure based on the GoF Template Method pattern.
- The `io` instance argument of the template method (method that requires implementation) `#doExecute` becomes the return value as-is.
- `#doExecute` is executed with the request from the web page already set in `io`.
- For responses to the web page, set values in `io` and end `#doExecute` as the return value. Request values become part of the response as-is unless changed or deleted.

### Type-Safe Data Processing with Io Class

<!-- AI_SKIP_START -->
#### Mechanism for JSON and Map Bidirectional Conversion
**Io class features**:
- Extends `Map<String, String>`
- JSON ⇔ `Io` object bidirectional conversion
- Supports up to 3 levels of JSON hierarchy structure
- Type-safe `get()` and `put()` methods
- Request and response are unified

**Sample code**:
```java
// The framework automatically converts request JSON to an Io object
// (The following code is not required)
// String reqJson = "{\"user_id\":\"U001\",\"income_am\":\"1200000\",\"birth_dt\":\"19870321\"}";
// Io io = new Io();
// io.putAllByJson(reqJson);

// Retrieve values type-safely
String userId = io.getString("user_id"); // "U001"
long incomeAm = io.getLong("income_am"); // 1200000
LocalDate birthDt = io.getDateNullable("birth_dt"); // Explicitly indicates null retrieval is possible

// Add values without specifying type (stored internally as String)
io.put("user_nm", "Mike Davis");
io.put("income_am", 1230000); // Numbers are also converted internally to "1230000"

// The framework automatically converts to response JSON
// (The following code is not required)
// String resJson = io.createJson();
```
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### Request and Response Unified
**Handle with Io class only**:
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
    // The io object becomes the response as-is
  }
}
```

**Advantages**:
- Request values become response as-is unless changed.
- Input value echo-back is automatic.
- Code volume is reduced.
<!-- AI_SKIP_END -->

### Database Access Processing
- When accessing the database within processing, extend the `AbstractDbAccessWebService` class to retrieve a database connection from the `#getDbConn` method anywhere in the class during `#doExecute` method processing.
- Always use `SqlUtil` methods for SQL execution.
- The database connection used within the `#doExecute` method is normally committed when `#doExecute` completes successfully.
- If an error message is set in `io`, the database is rolled back. Rollback also occurs when an exception error is thrown.

<!-- AI_SKIP_START -->
#### Transaction Management Mechanism
**Role of AbstractDbAccessWebService**:
```java
public abstract class AbstractDbAccessWebService extends AbstractWebService {
  
  @Override
  void execute(final Io io) throws Exception {
    // 1. Get database connection from pool
    try (final Connection conn = DbUtil.getConnPooled(super.traceCode)) {
      this.dbConn = conn;
      
      // 2. Execute subclass's doExecute()
      super.execute(io);
      
      // 3. Auto commit if no errors
      if (!io.hasErrorMsg()) {
        this.dbConn.commit();
      }
      // 4. DB connection released when try-with-resources ends, auto rollback on DB connection side
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
      return; // Error exists → auto rollback
    }
    
    // Header registration
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    // Detail registration (multiple rows)
    IoRows details = io.getRows("detail");
    for (IoItems detail : details) {
      detail.put("user_id", io.getString("user_id"));
      SqlUtil.insert(getDbConn(), "t_user_detail", detail);
    }
    
    // Success message
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    
    // Normal completion (no errors) → auto commit
  }
}
```

**Advantages**:
- No need to explicitly write commit/rollback.
- No forgotten rollback on errors.
- Transaction boundaries are clear (1 request = 1 transaction).
<!-- AI_SKIP_END -->

#### Dynamic SQL Building (SqlBuilder)
**Basic usage**:
```java
// Generate SQL builder
SqlBuilder sb = new SqlBuilder();

// Build SQL (method chaining supported)
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ").addQuery(" WHERE 1=1 ");

// Add conditions only when value exists (skip when blank)
sb.addQnotB("   AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB("   AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
sb.addQnotB("   AND u.email LIKE ? || '%' ", io.getString("email"));

sb.addQuery(" ORDER BY u.user_id ");

// Execute database retrieval
IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

<!-- AI_SKIP_START -->
**Executed SQL** (when request user_id is "U001", user_nm is empty, email is "test"):
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
// Add SQL only
sb.addQuery(" FROM t_user u ");
// Add SQL & required parameter
sb.addQuery(" AND user_id = ? ", userId);

// Add SQL & parameter only when value is not blank
sb.addQnotB(" AND user_id = ? ", userId);

// Add parameter only
sb.addParam(userId);

// Merge another SqlBuilder
sb.addSqlBuilder(otherSb);
```

<!-- AI_SKIP_START -->
**Advantages**:
- SQL and parameters are built together (logic lines are not separated).
- No need to concatenate SQL strings with conditional branching.
- SQL injection protection is built-in.
- Debugging is easy (can check SQL with `SqlBuilder#toString()`).
<!-- AI_SKIP_END -->

#### Unified Interface for Text Files, Database Retrieval Results, and Screen Lists
**Field-level and row-level processing**:
```java
// List input from screen: Process screen list row by row (IoItems)
IoRows detail = io.getRows("detail");
for (IoItems row : detail) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
}

// SqlResultSet: Retrieve database results row by row (IoItems)
try (SqlResultSet rSet = SqlUtil.select(getDbConn(), sb)) {
  // Process row by row (Iterator pattern)
  for (IoItems row : rSet) {
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
  }
}

// TxtReader: Read text file line by line (String)
try (TxtReader reader = new TxtReader("/path/to/data.csv", ValUtil.UTF8)) {
  // Skip header row
  reader.skip();
  
  // Define key name array
  String[] keys = {"user_id", "user_nm", "email"};
  
  // Process line by line (Iterator pattern)
  for (String line : reader) {
    // Set CSV row to IoItems
    IoItems row = new IoItems();
    row.putAllByCsvDq(keys, line); // Supports double-quoted CSV
    
    String userId = row.getString("user_id");
    String userNm = row.getString("user_nm");
    // Execute processing
  }
}
```

<!-- AI_SKIP_START -->
**Advantages**:
- Screen list (`IoRows`), `SqlResultSet`, and `TxtReader` all use the same loop processing.
- Memory efficient (processes row by row).
- Large data volumes can be processed safely.
- Type-safe data access with `IoItems`.
- Code is independent of data source (screen, database, file).
<!-- AI_SKIP_END -->

### Message Display Mechanism
Add messages to the following file as needed. The numbering rule for message IDs below is an example.

**Resource file** (resources/msg.json):
```json
{
  "ev001": "{0} is required.",
  "ev011": "{0} must contain only alphanumeric characters.",
  "ev012": "{0} must contain only numbers.",
  "i0002": "{0} has been registered.",
  "i0004": "Search returned {0} results."
}
```

**Setting messages on Java side**:
```java
// Error message (without field specification)
io.putMsg(MsgType.ERROR, "ev011", new String[]{"User ID"});
// → "User ID must contain only alphanumeric characters."

// Error message (with field specification)
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
// → "User ID is required." + highlight user_id field

io.putMsg(MsgType.ERROR, "ev012", new String[]{"Annual Income"}, "income_am");
// → "Annual Income must contain only numbers." + highlight income_am field

// Info message
io.putMsg(MsgType.INFO, "i0002", new String[]{"U001"});
// → "U001 has been registered."

// Multiple messages
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User Name"}, "user_nm");

// List field message
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id", "detail", rowIdx);
```

**JSON output**:
```json
// Without field specification
{
  "user_id": "U001",
  "_msgs": [
    {
      "type": "error",
      "id": "ev011",
      "text": "User ID must contain only alphanumeric characters."
    }
  ],
  "_has_err": true
}

// With field specification
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
**Advantages**:
- Centralized management of message text.
- Easy localization.
- Field-level highlighting supported, including list fields.
- Highlight CSS class is automatically applied.

**Additional note**:
- `_msgs` and `_has_err` are processed by the framework; do not use them directly from the application.
<!-- AI_SKIP_END -->

### Logging Processing
For logging, use the `logger` instance held by the superclass `AbstractWebService` (including `AbstractDbAccessWebService`). The purposes of logging are as follows:

| Purpose | Method to Use |
|-|-|
| Development debugging | `logger#develop` |
| Production information monitoring | `logger#info` |
| Production error monitoring | `logger#error` |
| Production concurrency monitoring | `logger#begin`, `logger#end` |
| Production performance monitoring | `logger#startWatch`, `logger#stopWatch` |

- When development debugging output causes performance overhead, use the `logger#isDevelopMode` method to confirm that debug logs will be output beforehand.
```java
if (logger.isDevelopMode()) {
    logger.develop("Delete count. " + LogUtil.joinKeyVal("count", delCnt);
}
```

<!-- AI_SKIP_START -->
#### Log File Rotation
**Auto rotation**:
- Log file switching on a daily basis
- Log level control

**Log output settings** (log.properties):
```properties
develop.mode=true
default.inf.file=/tmp/logs/info.log
default.err.file=/tmp/logs/error.log
```

**Usage**:
```java
// Development debug log
// Output only when develop.mode=true
logger.develop("Search conditions: " + LogUtil.joinKeyVal("userId", userId));

// Info log
// Output to the file path in default.inf.file
logger.info("Processing started: " + traceCode);

// Error log
// Output to the file path in default.err.file
logger.error(exception, "Error occurred");

// Performance measurement
logger.startWatch();
// ... processing ...
logger.stopWatch(); // Automatically outputs elapsed time
```
<!-- AI_SKIP_END -->

### Bug Prevention Features

The `Io` class has features to prevent bugs that commonly occur with general Map classes.

#### NULL-Safe Handling
**Problem**: General Map returns `null` from `get()`
```java
// General Map
Map<String, String> map = new HashMap<>();
String value = map.get("key"); // Returns null → cause of NullPointerException
```

**Solution**: Io class consciously retrieves null
```java
// Io class
Io io = new Io();

// Basic methods do not return null (return blank)
String value = io.getString("key"); // ""

// Use explicit method when null check is needed
String value = io.getStringNullable("key"); // null
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Prevents NullPointerException.
- Makes null handling conscious.
<!-- AI_SKIP_END -->

#### Type-Safe Handling
**Problem**: Error when converting string to number
```java
// General Map
String str = map.get("age");
int age = Integer.parseInt(str); // Possible NumberFormatException
```

**Solution**: Type conversion methods
```java
// Io class
int age = io.getInt("age");  // Blank converts to zero, non-numeric logs key and value as error
LocalDate birthDt = io.getDateNullable("birth_dt");   // Date format check
BigDecimal income_am = io.getBigDecimal("income_am"); // Blank converts to zero, preserves precision
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Early detection of type conversion errors.
- Eliminates implicit type conversions.
- Internal string storage guarantees numeric precision.
<!-- AI_SKIP_END -->

#### Strict Key Duplication Check
**Problem**: Unintended overwriting
```java
// General Map
map.put("user_id", "U001");
map.put("user_id", "U002"); // Overwritten (no warning)
```

**Solution**: Duplication error
```java
// Io class
io.put("user_id", "U001");
io.put("user_id", "U002"); // Overwriting is basically an error, logs key

// Use explicit method for intentional overwriting
io.putForce("user_id", "U002"); // OK
```

#### Avoiding Key Duplication When Merging Query Results with io
When using `io.putAll()` to merge database query results into the request `io`, a key duplication error occurs if the SELECT clause contains columns that already exist in `io`.

**Solution**: Do not include columns that already exist in the request `io` (such as primary key or optimistic locking timestamp) in the SELECT clause. If you must include them, use `io.putAllForce()` instead.

**Correct example**:

```java
// Request: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_nm ");      // Retrieve fields other than user_id, upd_ts
sb.addQuery(", u.email ");
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // OK: SELECT clause does not contain user_id, upd_ts
```

**Error example**:

```java
// Request: {"user_id": "U001", "upd_ts": "20250123T235959123456"}

final SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT ");
sb.addQuery("  u.user_id ");      // NG: Field exists in request
sb.addQuery(", u.user_nm ");
sb.addQuery(", u.email ");
sb.addQuery(", u.upd_ts ");       // NG: Field exists in request
sb.addQuery(" FROM t_user u ");
sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
io.putAll(row); // Error: user_id, upd_ts already exist in io
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Detects key typos.
- Prevents unintended overwriting (Map treated as `final` declaration).
- Guarantees data integrity.
<!-- AI_SKIP_END -->

#### Error When Retrieving with Non-Existent Key
**Problem**: Mistake due to typo
```java
// General Map
Map<String, String> map = new HashMap<>();
map.put("user_id", "U001");
String value = map.get("userid"); // null (typo not noticed)
```

**Solution**: Strict key check
```java
// Io class
io.put("user_id", "U001");
String value = io.getString("userid"); // Non-existent key logs key as error

// When existence check is needed
if (io.containsKey("userid")) {
  String value = io.getString("userid");
}

// Or use OrDefault method
String value = io.getStringOrDefault("userid", ""); // Non-existent key returns ""
```

<!-- AI_SKIP_START -->
**Bug prevention effect**:
- Early detection of typos.
- Prevents key name mistakes (Map treated as undeclared).
- Reduces debugging time.
<!-- AI_SKIP_END -->


#### Safety through Deep Copy
The `Io` class performs deep copy when storing and retrieving lists, nested maps, multiple rows lists, and array lists. This guarantees the following safety:

<!-- AI_SKIP_START -->
- **Prevention of unintended reference sharing**: Modifying the original stored list or map does not affect the data inside `Io`.
- **Independence of retrieved data**: Modifying retrieved lists or maps does not affect the data inside `Io`.
- **Prevention of bugs**: Avoids unexpected side effects caused by referencing the same data from multiple locations.
<!-- AI_SKIP_END -->

```java
// Deep copy during storage
List<String> srcList = new ArrayList<>(Arrays.asList("A", "B"));
io.putList("items", srcList);
srcList.add("C");  // Modify the original list
// io.getList("items") remains ["A", "B"] (unaffected)

// Deep copy during retrieval
List<String> gotList = io.getList("items");
gotList.add("D");  // Modify the retrieved list
// io.getList("items") remains ["A", "B"] (unaffected)
```

> **Note**: Performance may be affected when repeatedly storing and retrieving large data, due to deep copying.

## References

- [Web Page Structure Standards (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [Event-Based Coding Patterns](../02-develop-standards/21-event-coding-pattern.md)
