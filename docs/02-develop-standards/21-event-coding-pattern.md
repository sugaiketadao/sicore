# Event-Based Coding Patterns

<!-- AI_SKIP_START -->
Implementation patterns for each event based on sample programs.
Both humans and AI can quickly create similar features by following these patterns.
<!-- AI_SKIP_END -->

---

## Table of Contents

- [Sample Data Structure](#sample-data-structure)
- [1. List Initialization](#1-list-initialization)
- [2. List Search Processing](#2-list-search-processing)
- [3. Edit Page Navigation](#3-edit-page-navigation)
- [4. Edit Initialization (New/Update Determination)](#4-edit-initialization-newupdate-determination)
- [5. Data Retrieval Processing](#5-data-retrieval-processing)
- [6. Registration/Update Processing](#6-registrationupdate-processing)
- [7. Delete Processing](#7-delete-processing)
- [8. Row Add/Row Delete Processing](#8-row-addrow-delete-processing)
- [9. Cancel Processing](#9-cancel-processing)
- [Validation Patterns](#validation-patterns)
- [Message Patterns](#message-patterns)
- [File Naming Conventions](#file-naming-conventions)
- [References](#references)

---

## Sample Data Structure

### Table Definitions

**Header table: t_user**
| Field | Physical Name | Type | Notes |
|------|--------|-----|------|
| User ID | user_id | VARCHAR(4) | PK |
| User Name | user_nm | VARCHAR(20) | |
| Email | email | VARCHAR(50) | |
| Country of Origin | country_cs | VARCHAR(2) | JP/US/BR/AU |
| Gender | gender_cs | VARCHAR(1) | M/F |
| Spouse | spouse_cs | VARCHAR(1) | Y/N |
| Annual Income | income_am | NUMERIC(10) | |
| Birthday | birth_dt | DATE | |
| Update Timestamp | upd_ts | TIMESTAMP(6) | For logging and optimistic locking. |

**Detail table: t_user_pet**
| Field | Physical Name | Type | Notes |
|------|--------|-----|------|
| User ID | user_id | VARCHAR(4) | PK1 |
| Pet Number | pet_no | NUMERIC(2) | PK2 |
| Pet Name | pet_nm | VARCHAR(10) | |
| Type | type_cs | VARCHAR(2) | DG/CT/BD |
| Gender | gender_cs | VARCHAR(1) | M/F |
| Vaccinated | vaccine_cs | VARCHAR(1) | Y/N |
| Weight | weight_kg | NUMERIC(3,1) | |
| Birthday | birth_dt | DATE | |
| Update Timestamp | upd_ts | TIMESTAMP(6) | For logging. |

---

## 1. List Initialization

**Purpose**: Set initial values when opening the list page.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. Access URL `pages/app/exmodule/listpage.html` from the browser.
2. HTML, CSS, and JavaScript files are returned from the web server to the browser.
3. Initialization processing `listpage.js#init` is automatically executed in the browser.
    1. Clear messages.
    2. Call initialization web service `/services/exmodule/ExampleListInit`.
    3. Web service class `ExampleListInit` is executed.
    4. Web service response is returned to the browser.
    5. Retrieve previous search conditions from session.
    6. Merge web service response with previous search conditions.
    7. Set merged values to the search conditions area.
<!-- AI_SKIP_END -->

### JavaScript (listpage.js)

```javascript
const init = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Call web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListInit');
  // Get previous search conditions (optional)
  const old = StorageUtil.getPageObj('searchConditions');
  // Merge and set
  Object.assign(res, old);
  PageUtil.setValues(res, DomUtil.getById('searchConditionsArea'));
};

// Execute initialization
init();
```

### Java (ExampleListInit.java)

```java
public class ExampleListInit extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Set initial values (e.g., today's date)
    final String today = SqlUtil.getToday(getDbConn());
    io.put("birth_dt", today);
  }
}
```

<!-- AI_SKIP_START -->
### Application Points

- Retrieve options for initial display (dropdowns, etc.) from the database and set them.
- Set initial values based on logged-in user information.
<!-- AI_SKIP_END -->

---

## 2. List Search Processing

**Purpose**: Retrieve data with search conditions when the search button is clicked.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. User enters search conditions and clicks the search button.
2. JavaScript `listpage.js#search` is executed.
    1. Clear messages.
    2. Clear list area.
    3. Get search conditions from the search conditions area.
    4. Save search conditions to session (for next initial display).
    5. Call search web service `/services/exmodule/ExampleListSearch`.
    6. Web service class `ExampleListSearch` is executed.
        1. Execute validation (on error, set error message and exit).
        2. Build dynamic SQL with SqlBuilder.
        3. Execute database retrieval.
        4. Set retrieval results to response.
    7. Web service response is returned to the browser.
    8. Display messages (exit on error).
    9. Set retrieval results to list area (generate rows from template).
<!-- AI_SKIP_END -->

### JavaScript (listpage.js)

```javascript
const search = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Clear list
  PageUtil.clearRows('list');
  // Get search conditions
  const req = PageUtil.getValues(DomUtil.getById('searchConditionsArea'));
  // Save search conditions to session (optional)
  StorageUtil.setPageObj('searchConditions', req);
  // Call web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
  // Display messages
  PageUtil.setMsg(res);
  // Exit on error
  if (PageUtil.hasError(res)) {
    return;
  }
  // Set results
  PageUtil.setValues(res, DomUtil.getById('searchResultsArea'));
};
```

### Java (ExampleListSearch.java)

```java
public class ExampleListSearch extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Validation
    validate(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Database retrieval
    getList(io);
  }

  private void getList(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_id ");
    sb.addQuery(", u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(", CASE WHEN u.gender_cs = 'M' THEN 'Male' ");
    sb.addQuery("       WHEN u.gender_cs = 'F' THEN 'Female' ");
    sb.addQuery("       ELSE 'Other' END gender_dn ");
    sb.addQuery(", u.income_am ");
    sb.addQuery(", u.birth_dt ");
    sb.addQuery(", u.upd_ts ");
    sb.addQuery(" FROM t_user u WHERE 1=1 ");
    // Add condition only if value exists
    sb.addQnotB(" AND u.user_id = ? ", io.getString("user_id"));
    sb.addQnotB(" AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));
    sb.addQnotB(" AND u.email LIKE ? || '%' ", io.getString("email"));
    sb.addQnotB(" AND u.country_cs = ? ", io.getString("country_cs"));
    sb.addQnotB(" AND u.gender_cs = ? ", io.getString("gender_cs"));
    sb.addQnotB(" AND u.spouse_cs = ? ", io.getString("spouse_cs"));
    sb.addQnotB(" AND u.income_am >= ? ", io.getBigDecimalNullable("income_am"));
    sb.addQnotB(" AND u.birth_dt = ? ", io.getDateNullable("birth_dt"));
    sb.addQuery(" ORDER BY u.user_id ");

    // Bulk retrieval (maximum 5 records)
    final IoRows rows = SqlUtil.selectBulk(getDbConn(), sb, 5);
    io.putRows("list", rows);
    io.put("list_size", rows.size());

    if (rows.size() <= 0) {
      io.putMsg(MsgType.INFO, "i0004", new String[] { "0" });
    }
  }

  private void validate(final Io io) throws Exception {
    // Number check
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) && !ValUtil.isNumber(incomeAm)) {
      io.putMsg(MsgType.ERROR, "ev012", new String[] { "Annual Income" }, "income_am");
    }
    // Date check
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt) && !ValUtil.isDate(birthDt)) {
      io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birthday" }, "birth_dt");
    }
  }
}
```

### List HTML (Template Section)

```html
<tbody id="list">
  <script type="text/html">
    <tr>
      <td><input type="text" name="list.user_id" disabled>
        <input type="hidden" name="list.upd_ts"></td>
      <td data-name="list.user_nm"></td>
      <td data-name="list.email"></td>
      <td data-name="list.gender_dn"></td>
      <td data-name="list.income_am" data-value-format-type="num"></td>
      <td data-name="list.birth_dt" data-value-format-type="ymd"></td>
      <td><button type="button" onclick="editMove(this)">Edit</button></td>
    </tr>
  </script>
</tbody>
```

<!-- AI_SKIP_START -->
### Application Points

- `addQnotB`: Add condition only if value is not blank.
- `selectBulk(conn, sb, count)`: Retrieve with maximum record count specified.
- `selectBulkAll(conn, sb)`: Retrieve all records.
<!-- AI_SKIP_END -->

---

## 3. Edit Page Navigation

**Purpose**: Navigate from list to edit page (passing key values).

<!-- AI_SKIP_START -->
**Processing Flow**:
1. User clicks the "Edit" button in the list.
2. `editMove(btnElm)` is executed.
   1. Get data from the row containing the button (`getRowValuesByInnerElm`).
   2. Navigate to page with parameters (`HttpUtil.movePage`).
3. Browser navigates to `editpage.html?user_id=xxx&upd_ts=xxx`.
4. Continues to edit page initialization (see Section 4).
<!-- AI_SKIP_END -->

### JavaScript (listpage.js)

```javascript
// Edit button processing
const editMove = async function (btnElm) {
  // Get data from the row containing the button
  const req = PageUtil.getRowValuesByInnerElm(btnElm);
  // Navigate with parameters
  HttpUtil.movePage('editpage.html', req);
};

// New button processing
const create = async function () {
  // Navigate without parameters (new registration)
  HttpUtil.movePage('editpage.html');
};
```

<!-- AI_SKIP_START -->
### Application Points

- `getRowValuesByInnerElm(elm)`: Get data from the row containing the button or other element.
- Use `HttpUtil.getUrlParams()` to retrieve parameters at the destination page.
<!-- AI_SKIP_END -->

---

## 4. Edit Initialization (New/Update Determination)

**Purpose**: Determine whether it's a new registration or update when opening the edit page.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. Edit page is loaded in the browser.
2. `init()` is executed.
   1. Clear messages.
   2. Get URL parameters (`HttpUtil.getUrlParams()`).
   3. Branch based on presence of key value.
      - **No key (new)**: `initInsert()` → Add empty detail rows.
      - **Key exists (update)**: `initUpdate()` → Proceed to data retrieval in Section 5.
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const init = async function () {
  PageUtil.clearMsg();
  // Get URL parameters
  const params = HttpUtil.getUrlParams();

  if (ValUtil.isBlank(params['user_id'])) {
    // No key → new registration
    initInsert();
    return;
  }
  // Key exists → update
  await initUpdate(params);
};

// New registration initialization
const initInsert = function () {
  // Add 5 detail rows
  PageUtil.addRow('detail', new Array(5));
};

// Update initialization
const initUpdate = async function (params) {
  // Disable key field
  DomUtil.setEnable(DomUtil.getByName('user_id'), false);
  // Get data
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleLoad', params);
  PageUtil.setMsg(res);
  if (PageUtil.hasError(res)) {
    return;
  }
  PageUtil.setValues(res);
  // Add 5 rows if detail is empty
  if (ValUtil.isEmpty(res['detail'])) {
    PageUtil.addRow('detail', new Array(5));
  }
};

init();
```

---

## 5. Data Retrieval Processing

**Purpose**: Retrieve header and detail by specifying a key.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. Called from edit initialization (update mode only).
2. Disable key field (`DomUtil.setEnable`).
3. Call web service `ExampleLoad`.
   1. Retrieve header (`selectOne` with key and optimistic lock check).
   2. Exit if optimistic lock error (another user has already updated).
   3. Retrieve details (`selectBulkAll`).
   4. Return results.
4. Display response messages (`PageUtil.setMsg`).
5. If no errors, set values to screen (`PageUtil.setValues`).
6. Add empty rows if detail is empty.
<!-- AI_SKIP_END -->

### Java (ExampleLoad.java)

```java
public class ExampleLoad extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Get header
    getHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Get details
    getDetail(io);
  }

  private void getHead(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm, u.email, u.country_cs ");
    sb.addQuery(", u.gender_cs, u.spouse_cs ");
    sb.addQuery(", u.income_am, u.birth_dt ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    sb.addQuery("   AND u.upd_ts = ? ", io.getSqlTimestampNullable("upd_ts"));

    final IoItems head = SqlUtil.selectOne(getDbConn(), sb);
    if (ValUtil.isNull(head)) {
      // Optimistic lock error
      io.putMsg(MsgType.ERROR, "e0002", new String[]{io.getString("user_id")});
      return;
    }
    io.putAll(head);
  }

  private void getDetail(final Io io) {
    final SqlBuilder sb = new SqlBuilder();
    sb.addQuery("SELECT ");
    sb.addQuery("  d.pet_no, d.pet_nm, d.type_cs ");
    sb.addQuery(", d.gender_cs, d.vaccine_cs ");
    sb.addQuery(", d.weight_kg, d.birth_dt ");
    sb.addQuery(" FROM t_user_pet d ");
    sb.addQuery(" WHERE d.user_id = ? ", io.getString("user_id"));
    sb.addQuery(" ORDER BY d.pet_no ");

    final IoRows detail = SqlUtil.selectBulkAll(getDbConn(), sb);
    io.putRows("detail", detail);
  }
}
```

<!-- AI_SKIP_START -->
### Application Points

- Use `upd_ts` for optimistic locking (detect updates by other users).
- `selectOne`: Retrieve one record (returns null if not found).
- `putAll(row)`: Merge retrieval results directly into response.
<!-- AI_SKIP_END -->

---

## 6. Registration/Update Processing

**Purpose**: Register or update header and details.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. User clicks the "Register" button.
2. `upsert()` is executed.
   1. Clear messages.
   2. Get values from entire page (`PageUtil.getValues`).
   3. Call web service `ExampleUpsert`.
3. Execute server-side processing.
   1. Execute header validation (required, format, length).
   2. Exit if errors exist (return error messages).
   3. Execute detail validation (for each row).
   4. Exit if errors exist.
   5. Register/update header (determine new/update by `upd_ts`).
   6. Delete all details then register all.
   7. Set success message.
4. Display response messages (`PageUtil.setMsg`).
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const upsert = async function () {
  PageUtil.clearMsg();
  // Get values from entire page
  const req = PageUtil.getValues();
  // Call web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleUpsert', req);
  PageUtil.setMsg(res);
};
```

### Java (ExampleUpsert.java)

```java
public class ExampleUpsert extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Header validation
    validateHeader(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Detail validation
    validateDetail(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Header registration/update
    upsertHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Detail delete → register
    delInsDetail(io);
    // Success message
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    }
  }

  private void validateHeader(final Io io) throws Exception {
    // Required check
    final String userId = io.getString("user_id");
    if (ValUtil.isBlank(userId)) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
    } else if (!ValUtil.isAlphabetNumber(userId)) {
      io.putMsg(MsgType.ERROR, "ev011", new String[] { "User ID" }, "user_id");
    } else if (!ValUtil.checkLength(userId, 4)) {
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "User ID", "4" }, "user_id");
    }

    final String userNm = io.getString("user_nm");
    if (ValUtil.isBlank(userNm)) {
      io.putMsg(MsgType.ERROR, "ev001", new String[] { "User Name" }, "user_nm");
    }
    // Check other fields in the same way...
  }

  private void validateDetail(final Io io) throws Exception {
    if (!io.containsKeyRows("detail")) {
      return;
    }
    final IoRows detail = io.getRows("detail");
    for (int rowIdx = 0; rowIdx < detail.size(); rowIdx++) {
      final IoItems row = detail.get(rowIdx);
      // Check for each detail row
      final String petNm = row.getString("pet_nm");
      if (ValUtil.isBlank(petNm)) {
        // Specify row index for detail errors
        io.putMsg(MsgType.ERROR, "ev001", new String[] { "Pet Name" }, "pet_nm", "detail", rowIdx);
      }
    }
  }

  private void upsertHead(final Io io) {
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // New registration
      if (!SqlUtil.insertOne(getDbConn(), "t_user", io, "upd_ts")) {
        io.putMsg(MsgType.ERROR, "e0001", new String[] { io.getString("user_id") }, "user_id");
      }
      return;
    }
    // Update
    if (!SqlUtil.updateOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  private void delInsDetail(final Io io) {
    final Connection conn = getDbConn();
    // Delete all existing details
    SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (!io.containsKeyRows("detail")) {
      return;
    }
    // Register new details
    final IoRows detail = io.getRows("detail");
    final String userId = io.getString("user_id");
    int dno = 0;
    for (final IoItems row : detail) {
      dno++;
      row.put("user_id", userId);
      row.put("pet_no", dno);
      SqlUtil.insertOne(conn, "t_user_pet", row);
    }
  }
}
```

<!-- AI_SKIP_START -->
### Application Points

- If `upd_ts` is blank, determine it as new registration.
- "Delete all → Register all" pattern is simple for details.
- `insertOne(conn, table, io, "upd_ts")`: Automatically sets current timestamp to upd_ts.
- `updateOne(conn, table, io, keys, "upd_ts")`: Performs optimistic locking with upd_ts.
<!-- AI_SKIP_END -->

---

## 7. Delete Processing

**Purpose**: Delete header and details.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. User clicks the "Delete" button.
2. `del()` is executed.
   1. Clear messages.
   2. Get values from entire page.
   3. Call web service `ExampleDelete`.
3. Execute server-side processing.
   1. Delete header (`deleteOne` with optimistic lock check).
   2. Exit if optimistic lock error.
   3. Delete details (`delete` to delete all records).
   4. Set success message.
4. Display response messages.
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const del = async function () {
  PageUtil.clearMsg();
  const req = PageUtil.getValues();
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleDelete', req);
  PageUtil.setMsg(res);
};
```

### Java (ExampleDelete.java)

```java
public class ExampleDelete extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Delete header
    deleteHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Delete details
    deleteDetail(io);
    // Success message
    io.putMsg(MsgType.INFO, "i0003", new String[] { io.getString("user_id") });
  }

  private void deleteHead(final Io io) {
    if (!SqlUtil.deleteOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  private void deleteDetail(final Io io) {
    SqlUtil.delete(getDbConn(), "t_user_pet", io, new String[] { "user_id" });
  }
}
```

---

## 8. Row Add/Row Delete Processing

**Purpose**: Dynamically add or delete detail rows.

<!-- AI_SKIP_START -->
**Processing Flow (Add Row)**:
1. User clicks the "Add Row" button.
2. `addRow()` is executed.
   1. Generate a new row from template with `PageUtil.addRow('detail')`.
   2. Row is appended to the end of the detail table.

**Processing Flow (Delete Row)**:
1. User selects checkboxes of rows to delete.
2. Click the "Delete Row" button.
3. `removeRow()` is executed.
   1. Delete checked rows with `PageUtil.removeRow('detail.chk', '1')`.
   2. Corresponding rows are removed from the DOM.
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
// Add row
const addRow = function () {
  PageUtil.addRow('detail');
};

// Delete row (delete checked rows)
const removeRow = function () {
  PageUtil.removeRow('detail.chk', '1');
};
```

### HTML (Detail Template)

```html
<tbody id="detail">
  <script type="text/html">
    <tr>
      <td><input type="checkbox" name="detail.chk" value="1"></td>
      <td data-name="detail.pet_no"></td>
      <td><input type="text" name="detail.pet_nm"></td>
      <td><select name="detail.type_cs">
        <option value="">Not Selected</option>
        <option value="DG">Dog</option>
        <option value="CT">Cat</option>
      </select></td>
      <!-- Other fields... -->
    </tr>
  </script>
</tbody>
```

---

## 9. Cancel Processing

**Purpose**: Cancel editing and return to list page.

<!-- AI_SKIP_START -->
**Processing Flow**:
1. User clicks the "Cancel" button.
2. `cancel()` is executed.
   1. Navigate to list page with `HttpUtil.movePage('listpage.html')`.
3. List page initialization is executed (see Section 1).
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const cancel = async function () {
  HttpUtil.movePage('listpage.html');
};
```

---

## Validation Patterns

| Check Content | Method | Usage Example |
|--------------|--------|---------------|
| Required check. | `ValUtil.isBlank(str)` | `if (ValUtil.isBlank(userId))` |
| Alphanumeric check. | `ValUtil.isAlphabetNumber(str)` | `if (!ValUtil.isAlphabetNumber(userId))` |
| Number check. | `ValUtil.isNumber(str)` | `if (!ValUtil.isNumber(incomeAm))` |
| Date check. | `ValUtil.isDate(str)` | `if (!ValUtil.isDate(birthDt))` |
| Length check. | `ValUtil.checkLength(str, len)` | `if (!ValUtil.checkLength(userId, 4))` |
| Numeric length check. | `ValUtil.checkLengthNumber(str, int, dec)` | `if (!ValUtil.checkLengthNumber(weight, 3, 1))` |

---

## Message Patterns

### Header Field Error

```java
io.putMsg(MsgType.ERROR, "ev001", new String[] { "User ID" }, "user_id");
// → "User ID is required." + user_id field highlight
```

### Detail Field Error

```java
io.putMsg(MsgType.ERROR, "ev001", new String[] { "Pet Name" }, "pet_nm", "detail", rowIdx);
// → "Pet Name is required." + detail.pet_nm field highlight (corresponding row)
```

### Success Message

```java
io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
// → "U001 has been registered."
```

---

## File Naming Conventions

| Type | File Name | Class Name |
|------|-----------|------------|
| List initialization. | ExampleListInit.java | ExampleListInit |
| List search processing. | ExampleListSearch.java | ExampleListSearch |
| Data retrieval processing. | ExampleLoad.java | ExampleLoad |
| Registration/update processing. | ExampleUpsert.java | ExampleUpsert |
| Delete processing. | ExampleDelete.java | ExampleDelete |

---

### Sample Code
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB Definitions/Test Data: `example_db/example_data_create.sql`
