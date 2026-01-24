# Batch Processing Structure Standard

## Overview
- Defines standard rules for batch processing.
- By adhering to this standard and this framework, aims to unify program code and improve development and maintenance efficiency.
- This document explains using the following samples:
    - Java: `src/com/example/app/bat/exmodule/`

## Prerequisites
- Use Java 11 or later.
- Batch processing arguments use only the first argument (`args[0]`) in URL parameter format.
- Batch processing return values are 0 for normal termination and 1 for abnormal termination.

## File Structure

### Directory Structure
- Module Java packages store one or more Java classes that are functionally cohesive.

```
[project root]/                       # Project root: In most cases, becomes the project name.
├── config/                        # Configuration file directory [※Configure directory path]
├── resources/                     # Resource file directory
├── src/                           # Java source file directory
│   ├── [project domain]/         # Project domain Java package [Example] "com/example/"
│   │   ├── app/                 # Module Java package: Branch point from common component Java package
│   │   │   └── bat/            # Batch processing Java package
│   │   │        └── [module]/  # Module Java package: Stores batch processing Java classes.
│   │   └── util/                # Common component Java package: Stores common components used from batch processing Java classes. [Package name util is an example]
│   └── com/onpg/                 # This framework domain Java package
├── classes/                       # Java class compilation destination directory
└── lib/                           # Java library file directory
```

### File Creation Unit
- Create one Java class for each batch processing.

### Configuration Files
Refer to [Web Service Processing Structure Standard - Configuration Files](../02-develop-standards/11-web-service-structure.md#configuration-files)

### Resource Files
Refer to [Web Service Processing Structure Standard - Resource Files](../02-develop-standards/11-web-service-structure.md#resource-files)


## Batch Processing Structure

Explains implementation rules for batch processing and main features provided by the framework.

### Batch Processing Class Structure
- Batch processing classes inherit from the `AbstractBatch` class or `AbstractDbAccessBatch` class and have a structure using the GoF Template Method pattern.
- By calling the `callMain` method from the `main` method, common processing (log output, exception handling, etc.) is executed.
- Arguments in URL parameter format are converted to map format and set in the `io` argument of the `doExecute` template method (method that requires implementation).
- The return value of the `doExecute` method becomes the return value of the `main` method (0: normal termination, other than 0: abnormal termination).

**Implementation Example**:
```java
public class ExampleBatch extends AbstractBatch {

  public static void main(String[] args) {
    final ExampleBatch batch = new ExampleBatch();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Implement batch processing content
    return 0;
  }
}
```

**Execution Example**:
```
java com.example.app.bat.exmodule.ExampleBatch "param1=value1&param2=value2"
```

### Database Access Processing
- When accessing database within processing, by inheriting the `AbstractDbAccessBatch` class, database connection can be obtained from the `getDbConn` method anywhere within the class during `doExecute` method processing.
- Always use `SqlUtil` methods for SQL execution.
- If the return value of the `doExecute` method is 0, the processing is considered to have terminated normally and database commit is performed.
- If the return value of the `doExecute` method is other than 0 or an exception error occurs, the processing is considered to have terminated abnormally and database rollback is performed.

**Implementation Example**:
```java
public class ExampleDbBatch extends AbstractDbAccessBatch {

  private static final SqlConst SQL_SEL_USER = SqlConst.begin()
    .addQuery("SELECT u.user_id, u.user_nm FROM t_user u ")
    .addQuery(" ORDER BY u.user_id ")
    .end();

  public static void main(String[] args) {
    final ExampleDbBatch batch = new ExampleDbBatch();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Execute database retrieval
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER)) {
      for (final IoItems row : rSet) {
        // Process row by row
      }
    }
    return 0; // Normal termination → Automatic commit
  }
}
```

#### Fixed SQL Definition (SqlConst)
- In batch processing, define fixed SQL with the `SqlConst` class and hold it as `static final` in class fields.
- `SqlConst` is an immutable SQL definition. When executing SQL, bind parameters with the `bind` method and generate `SqlBean` for use.

**Basic Usage (SELECT)**:
```java
// SQL definition (class field)
private static final SqlConst SQL_SEL_USER = SqlConst.begin()
  .addQuery("SELECT ")
  .addQuery("  u.user_id ")
  .addQuery(", u.user_nm ")
  .addQuery(", u.email ")
  .addQuery(" FROM t_user u ")
  .addQuery(" ORDER BY u.user_id ")
  .end();

// SQL execution (within method)
try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER)) {
  for (final IoItems row : rSet) {
    // Process row by row
  }
}
```

**Parameter Binding (INSERT/UPDATE)**:
```java
// SQL definition (class field)
private static final SqlConst SQL_INS_USER = SqlConst.begin()
  .addQuery("INSERT INTO t_user ( ")
  .addQuery("  user_id ")
  .addQuery(", user_nm ")
  .addQuery(", income_am ")
  .addQuery(", birth_dt ")
  .addQuery(" ) VALUES ( ")
  .addQuery("  ? ", "user_id", BindType.STRING)
  .addQuery(", ? ", "email", BindType.STRING)
  .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
  .addQuery(", ? ", "birth_dt", BindType.DATE)
  .addQuery(" ) ")
  .end();

// SQL execution (within method)
IoItems row = new IoItems();
row.put("user_id", "U001");
row.put("email", "test@example.com");
row.put("income_am", 1200000);
row.put("birth_dt", "19900101");
SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
```

**BindType List**:
| BindType | Usage | Notes |
|-|-|-|
| `STRING` | String | VARCHAR, etc. |
| `BIGDECIMAL` | Numeric | INTEGER, NUMERIC, DECIMAL, etc. (all numeric types use this type) |
| `DATE` | Date | DATE (yyyyMMdd format) |
| `TIMESTAMP` | Timestamp | TIMESTAMP (yyyyMMddHHmmssSSS format) |

**Main Methods**:
```java
// Start SQL definition
SqlConst.begin()

// Add SQL only
.addQuery(" FROM t_user u ")

// Add SQL & bind definition
.addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)

// End SQL definition (make immutable)
.end()

// Bind values and generate SqlBean
SQL_INS_USER.bind(row)
```

### Log Output Processing
For log output, use the `logger` instance that the superclass `AbstractBatch` (including `AbstractDbAccessBatch`) has.
Refer to [Web Service Processing Structure Standard - Log Output Processing](../02-develop-standards/11-web-service-structure.md#log-output-processing) for log output usage.

