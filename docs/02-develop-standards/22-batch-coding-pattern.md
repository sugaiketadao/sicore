# Batch Processing Coding Patterns

<!-- AI_SKIP_START -->
Implementation patterns for each batch processing based on sample programs.
Both humans and AI can quickly create similar functionality by following these patterns.
<!-- AI_SKIP_END -->

---

## Table of Contents

- [Sample Data Structure](#sample-data-structure)
- [1. Data Export Processing](#1-data-export-processing)
- [2. Data Import Processing](#2-data-import-processing)
- [Validation Pattern List](#validation-pattern-list)
- [File Naming Rules](#file-naming-rules)
- [Reference](#reference)

---

## Sample Data Structure

### Table Definition

**Table: t_user**
| Item | Physical Name | Type | Notes |
|------|--------|-----|------|
| User ID | user_id | VARCHAR(4) | PK |
| User Name | user_nm | VARCHAR(20) | |
| Email | email | VARCHAR(50) | |
| Country of Origin | country_cs | VARCHAR(2) | JP/US/BR/AU |
| Gender | gender_cs | VARCHAR(1) | M/F |
| Spouse | spouse_cs | VARCHAR(1) | Y/N |
| Annual Income | income_am | NUMERIC(10) | |
| Birth Date | birth_dt | DATE | |
| Update Timestamp | upd_ts | TIMESTAMP(6) | For logging and optimistic locking. |

---

## 1. Data Export Processing

**Usage**: Retrieves data from database and outputs to CSV file.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. Get the output file path from arguments.
2. Execute argument validation (required check, file existence check).
3. Execute database retrieval SQL.
4. Output header row to CSV file.
5. Output database retrieval results line by line in CSV format.
6. Output information log if retrieved count is 0.
<!-- AI_SKIP_END -->

### Java（ExampleExport.java）

```java
public class ExampleExport extends AbstractDbAccessBatch {

  /** SQL definition: User retrieval. */
  private static final SqlConst SQL_SEL_USER = SqlConst.begin()
    .addQuery("SELECT ")
    .addQuery("  u.user_id ")
    .addQuery(", u.user_nm ")
    .addQuery(", u.email ")
    .addQuery(", u.country_cs ")
    .addQuery(", u.gender_cs ")
    .addQuery(", u.spouse_cs ")
    .addQuery(", u.income_am ")
    .addQuery(", u.birth_dt ")
    .addQuery(", u.upd_ts ")
    .addQuery(" FROM t_user u ")
    .addQuery(" ORDER BY u.user_id ")
    .end();

  public static void main(String[] args) {
    final ExampleExport batch = new ExampleExport();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Get output file path
    final String outputPath = io.getString("output");

    // Argument validation
    if (ValUtil.isBlank(outputPath)) {
      throw new RuntimeException("'output' is required.");
    }
    if (FileUtil.exists(outputPath)) {
      throw new RuntimeException("Output path already exists. " + LogUtil.joinKeyVal("output", outputPath));
    }
    if (!FileUtil.existsParent(outputPath)) {
      throw new RuntimeException("Output parent directory not exists. " + LogUtil.joinKeyVal("output", outputPath));
    }

    // Retrieve from database and output to file
    try (final SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);
        final CsvWriter cw = new CsvWriter(outputPath, LineSep.CRLF, CharSet.UTF8, CsvType.DQ_ALL_LF)) {
      // Outputs the column names
      cw.println(rSet.getItemNames());
      for (final IoItems row : rSet) {
        cw.println(row);
      }
      if (rSet.getReadedCount() == 0) {
        super.logger.info("No data found to export. " + LogUtil.joinKeyVal("output", outputPath));
      }
    }
    return 0;
  }
}
```

**Execution Example**:
```
java com.example.app.bat.exmodule.ExampleExport "output=/tmp/user_export.csv"
```

<!-- AI_SKIP_START -->
### Application Points

- `SqlResultSet#getItemNames()`: Retrieves an array of retrieved field names.
- `ValUtil.joinCsvAllDq(String[])`: Joins all fields in CSV format with double quotation marks.
- `IoItems#createCsvAllDq()`: Outputs row data in CSV format (all fields with double quotation marks).
- `SqlResultSet#getReadedCount()`: Retrieves the number of processed rows.
<!-- AI_SKIP_END -->

---

## 2. Data Import Processing

**Usage**: Reads CSV file and registers/updates data in database.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. Get the input file path from arguments.
2. Execute argument validation (required check, file existence check).
3. Open CSV file and get header row.
4. Read data rows line by line and map with field name array.
5. Execute UPDATE SQL, and if the update count is 0, execute INSERT SQL (UPSERT processing).
6. Output information log if data rows count is 0.
<!-- AI_SKIP_END -->

### Java（ExampleImport.java）

```java
public class ExampleImport extends AbstractDbAccessBatch {

  /** SQL definition: User registration. */
  private static final SqlConst SQL_INS_USER = SqlConst.begin()
    .addQuery("INSERT INTO t_user ( ")
    .addQuery("  user_id ")
    .addQuery(", user_nm ")
    .addQuery(", email ")
    .addQuery(", country_cs ")
    .addQuery(", gender_cs ")
    .addQuery(", spouse_cs ")
    .addQuery(", income_am ")
    .addQuery(", birth_dt ")
    .addQuery(", upd_ts ")
    .addQuery(" ) VALUES ( ")
    .addQuery("  ? ", "user_id", BindType.STRING)
    .addQuery(", ? ", "user_nm", BindType.STRING)
    .addQuery(", ? ", "email", BindType.STRING)
    .addQuery(", ? ", "country_cs", BindType.STRING)
    .addQuery(", ? ", "gender_cs", BindType.STRING)
    .addQuery(", ? ", "spouse_cs", BindType.STRING)
    .addQuery(", ? ", "income_am", BindType.BIGDECIMAL)
    .addQuery(", ? ", "birth_dt", BindType.DATE)
    .addQuery(", ? ", "upd_ts", BindType.TIMESTAMP)
    .addQuery(" ) ")
    .end();

  /** SQL definition: User update. */
  private static final SqlConst SQL_UPD_USER = SqlConst.begin()
    .addQuery("UPDATE t_user SET ")
    .addQuery("  user_nm = ? ", "user_nm", BindType.STRING)
    .addQuery(", email = ? ", "email", BindType.STRING)
    .addQuery(", country_cs = ? ", "country_cs", BindType.STRING)
    .addQuery(", gender_cs = ? ", "gender_cs", BindType.STRING)
    .addQuery(", spouse_cs = ? ", "spouse_cs", BindType.STRING)
    .addQuery(", income_am = ? ", "income_am", BindType.BIGDECIMAL)
    .addQuery(", birth_dt = ? ", "birth_dt", BindType.DATE)
    .addQuery(", upd_ts = ? ", "upd_ts", BindType.TIMESTAMP)
    .addQuery(" WHERE user_id = ? ", "user_id", BindType.STRING)
    .end();

  public static void main(String[] args) {
    final ExampleImport batch = new ExampleImport();
    batch.callMain(args);
  }

  @Override
  public int doExecute(final IoItems io) throws Exception {
    // Get input file path
    final String inputPath = io.getString("input");

    // Argument validation
    if (ValUtil.isBlank(inputPath)) {
      throw new RuntimeException("'input' is required.");
    }
    if (!FileUtil.exists(inputPath)) {
      throw new RuntimeException("Input path not exists. " + LogUtil.joinKeyVal("input", inputPath));
    }

    // Reads the file and updates the database
    try (final CsvReader cr = new CsvReader(inputPath, CharSet.UTF8, CsvType.DQ_ALL_LF)) {
      for (final IoItems row : cr) {
        if (!SqlUtil.executeOne(getDbConn(), SQL_UPD_USER.bind(row))) {
          // Executes insert if the update count is 0
          SqlUtil.executeOne(getDbConn(), SQL_INS_USER.bind(row));
        }
      }
      if (cr.getReadedCount() == 0) {
        // When there are no data rows (header only or empty file)
        super.logger.info("No data found to import. " + LogUtil.joinKeyVal("input", inputPath));
      }
    }
    return 0;
  }
}
```

**Execution Example**:
```
java com.example.app.bat.exmodule.ExampleImport "input=/tmp/user_import.csv"
```

<!-- AI_SKIP_START -->
### Application Points

- `TxtReader#getFirstLine()`: Retrieves the first line (header row), and that line is excluded from the loop.
- `ValUtil.splitCsvDq(String)`: Splits a CSV format string with double quotation marks.
- `IoItems#putAllByCsvDq(String[], String)`: Creates a map from field name array and CSV row.
- `SqlConst#bind(IoItems)`: Binds values to predefined SQL and generates a SqlBuilder.
- `SqlUtil.executeOne()`: Returns true if the update count is 1, false if 0.
<!-- AI_SKIP_END -->

---

## Validation Pattern List

### Argument Validation

| Check | Code Example |
|---------|----------|
| Required check | `if (ValUtil.isBlank(path)) { throw new RuntimeException("'param' is required."); }` |
| File existence check (input) | `if (!FileUtil.exists(path)) { throw new RuntimeException("Input path not exists."); }` |
| File non-existence check (output) | `if (FileUtil.exists(path)) { throw new RuntimeException("Output path already exists."); }` |
| Directory existence check | `if (!FileUtil.existsParent(path)) { throw new RuntimeException("Parent directory not exists."); }` |

### Error Log Output Pattern

```java
// Output parameters in key-value format
throw new RuntimeException("Error message. " + LogUtil.joinKeyVal("key1", val1, "key2", val2));
```

---

## Sample Code

- Java: `src/com/example/app/bat/exmodule/`
