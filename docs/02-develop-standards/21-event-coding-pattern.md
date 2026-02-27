# Event Coding Pattern

<!-- AI_SKIP_START -->
Implementation patterns for each event based on sample programs.
Both humans and AI can quickly create similar functions by following these patterns.
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
- [7. Deletion Processing](#7-deletion-processing)
- [8. Add Row/Remove Row Processing](#8-add-rowremove-row-processing)
- [9. Cancel Processing](#9-cancel-processing)
- [Validation Pattern List](#validation-pattern-list)
- [Message Pattern](#message-pattern)
- [File Naming Rules](#file-naming-rules)
- [Reference](#reference)

---

## Sample Data Structure

### Table Definition

**Header table: t_user**
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

**Detail table: t_user_pet**
| Item | Physical Name | Type | Notes |
|------|--------|-----|------|
| User ID | user_id | VARCHAR(4) | PK1 |
| Pet Number | pet_no | NUMERIC(2) | PK2 |
| Pet Name | pet_nm | VARCHAR(10) | |
| Type | type_cs | VARCHAR(2) | DG/CT/BD |
| Gender | gender_cs | VARCHAR(1) | M/F |
| Vaccinated | vaccine_cs | VARCHAR(1) | Y/N |
| Weight | weight_kg | NUMERIC(3,1) | |
| Birth Date | birth_dt | DATE | |
| Update Timestamp | upd_ts | TIMESTAMP(6) | For logging. |

---

## 1. List Initialization

**Usage**: Set initial values when opening list page.

<!-- AI_SKIP_START -->
**Processing flow**:
1. Access URL `pages/app/exmodule/listpage.html` from browser.
2. HTML, CSS, JavaScript files are returned from web server to browser.
3. Initialization `listpage.js#init` is automatically executed in browser.
    1. Clear messages.
    2. Call initialization web service `/services/exmodule/ExampleListInit`.
    3. Web service class `ExampleListInit` is executed.
    4. Web service response is returned to browser.
    5. Retrieve previous database retrieval conditions from browser storage.
    6. Merge web service response with previous database retrieval conditions.
    7. Set merged values to database retrieval conditions area.
<!-- AI_SKIP_END -->

### JavaScript (listpage.js)

```javascript
const init = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Call web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListInit');
  // Retrieve previous search conditions (optional)
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
    // Set initial value (example: today's date)
    final String today = SqlUtil.getToday(getDbConn());
    io.put("birth_dt", today);
  }
}
```

<!-- AI_SKIP_START -->
### Application Points

- Retrieve options (dropdown, etc.) to display initially from DB and set them.
- Set initial values based on sign-in user information.
<!-- AI_SKIP_END -->

---

## 2. List Search Processing

**Usage**: Retrieve data from DB based on search conditions when search button is clicked.

<!-- AI_SKIP_START -->
**Processing flow**:
1. User enters search conditions and clicks the search button.
2. JavaScript `listpage.js#search` is executed.
    1. Clear messages.
    2. Clear list area.
    3. Retrieve search conditions from DB extraction conditions area.
    4. Save search conditions to browser storage (for next initialization).
    5. Call search web service `/services/exmodule/ExampleListSearch`.
    6. Web service class `ExampleListSearch` is executed.
        1. Execute validation (if error, set error message and terminate).
        2. Build dynamic SQL with SqlBuilder.
        3. Execute DB extraction.
        4. Set extraction results to response.
    7. Web service response is returned to browser.
    8. Display messages (if error, terminate).
    9. Set extraction results to list area (generate rows from template).
<!-- AI_SKIP_END -->

### JavaScript（listpage.js）

```javascript
const search = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Clear list
  PageUtil.clearRows('list');
  // Retrieve search conditions
  const req = PageUtil.getValues(DomUtil.getById('searchConditionsArea'));
  // Save search conditions to browser storage (optional)
  StorageUtil.setPageObj('searchConditions', req);
  // Call web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
  // Display messages
  PageUtil.setMsg(res);
  // Terminate if error
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
    // DB extraction
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
    // Add condition only when value exists
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
      io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth Date" }, "birth_dt");
    }
  }
}
```

### List HTML (Template Part)

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

- `addQnotB`: Adds condition only when value is not blank.
- `selectBulk(conn, sb, count)`: Retrieves records with specified maximum count.
- `selectBulkAll(conn, sb)`: Retrieves all records.
<!-- AI_SKIP_END -->

---

## 3. Edit Page Navigation

**Usage**: Navigate from list to edit page (pass key values).

<!-- AI_SKIP_START -->
**Processing flow**:
1. User clicks the "Edit" button on the list.
2. `editMove(btnElm)` is executed.
   1. Retrieve data from the row containing the button (`getRowValuesByInnerElm`).
   2. Navigate to page with parameters (`HttpUtil.movePage`).
3. Browser navigates to `editpage.html?user_id=xxx&upd_ts=xxx`.
4. Continues to edit page initialization (see Section 4).
<!-- AI_SKIP_END -->

### JavaScript (listpage.js)

```javascript
// Edit button processing
const editMove = async function (btnElm) {
  // Retrieve data from row containing button
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

- `getRowValuesByInnerElm(elm)`: Retrieves data from row containing button or element.
- At destination, retrieve parameters with `HttpUtil.getUrlParams()`.
<!-- AI_SKIP_END -->

---

## 4. Edit Initialization (New/Update Determination)

**Usage**: Determine whether to perform new registration or update when opening edit page.

<!-- AI_SKIP_START -->
**Processing flow**:
1. Edit page is loaded in browser.
2. `init()` is executed.
   1. Clear messages.
   2. Retrieve URL parameters (`HttpUtil.getUrlParams()`).
   3. Branch based on presence of key value.
      - **No key (new)**: `initInsert()` → Add empty detail rows.
      - **Has key (update)**: `initUpdate()` → Proceed to data retrieval in Section 5.
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const init = async function () {
  PageUtil.clearMsg();
  // Retrieve URL parameters
  const params = HttpUtil.getUrlParams();

  if (ValUtil.isBlank(params['user_id'])) {
    // No key → New registration
    initInsert();
    return;
  }
  // Has key → Update
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
  // Retrieve data
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleLoad', params);
  PageUtil.setMsg(res);
  if (PageUtil.hasError(res)) {
    return;
  }
  PageUtil.setValues(res);
  // If no detail records, add 5 rows
  if (ValUtil.isEmpty(res['detail'])) {
    PageUtil.addRow('detail', new Array(5));
  }
};

init();
```

---

## 5. Data Retrieval Processing

**Usage**: Retrieve header and details by specifying key.

<!-- AI_SKIP_START -->
**Processing flow**:
1. Called from edit initialization (update only).
2. Disable key field (`DomUtil.setEnable`).
3. Call web service `ExampleLoad`.
   1. Retrieve header (`selectOne` performs key and optimistic locking check).
   2. If optimistic locking error, terminate (another user has already updated).
   3. Retrieve details (`selectBulkAll`).
   4. Return results.
4. Display messages from response (`PageUtil.setMsg`).
5. If no error, set values to screen (`PageUtil.setValues`).
6. If no detail records, add empty rows.
<!-- AI_SKIP_END -->

### Java (ExampleLoad.java)

```java
public class ExampleLoad extends AbstractDbAccessWebService {

  @Override
  public void doExecute(final Io io) throws Exception {
    // Retrieve header
    getHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Retrieve details
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
      // Optimistic locking error
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

- `upd_ts` performs optimistic locking (detects updates by other users).
- `selectOne`: Retrieves one record (returns null if not found).
- `putAll(row)`: Merges retrieved results directly into response.
<!-- AI_SKIP_END -->

---

## 6. Registration/Update Processing

**Usage**: Register or update header and details.

<!-- AI_SKIP_START -->
**Processing flow**:
1. User clicks "Register" button.
2. `upsert()` is executed.
   1. Clear messages.
   2. Retrieve values from entire page (`PageUtil.getValues`).
   3. Call web service `ExampleUpsert`.
3. Execute server-side processing.
   1. Execute header validation (required, format, length).
   2. If error exists, terminate (return error message).
   3. Execute detail validation (for each row).
   4. If error exists, terminate.
   5. Register/update header (determine new/update by `upd_ts`).
   6. Delete all details then register all.
   7. Set success message.
4. Display messages from response (`PageUtil.setMsg`).
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
const upsert = async function () {
  PageUtil.clearMsg();
  // Retrieve values from entire page
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
    // Register/update header
    upsertHead(io);
    if (io.hasErrorMsg()) {
      return;
    }
    // Delete details → Register
    delInsDetail(io);
    // Success message
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    }
  }

  private void validateHeader(final Io io) throws Exception {
    // Required field check
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
    // Check other fields similarly...
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
        // Detail error specifies row index
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
    if (!SqlUtil.updateOneByPkey(getDbConn(), "t_user", io, "upd_ts")) {
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
    // Register details as new
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

- If `upd_ts` is blank, it is determined as new registration.
- For details, the "delete all → register all" pattern is concise.
- `insertOne(conn, table, io, "upd_ts")`: Automatically sets current timestamp to upd_ts.
- `updateOne(conn, table, io, keys, "upd_ts")`: Performs optimistic locking with upd_ts.
<!-- AI_SKIP_END -->

---

## 7. Deletion Processing

**Usage**: Delete header and details.

<!-- AI_SKIP_START -->
**Processing flow**:
1. User clicks "Delete" button.
2. `del()` is executed.
   1. Clear messages.
   2. Retrieve values from entire page.
   3. Call web service `ExampleDelete`.
3. Execute server-side processing.
   1. Delete header (`deleteOne` with optimistic locking check).
   2. If optimistic locking error, terminate.
   3. Delete details (`delete` deletes all records).
   4. Set success message.
4. Display messages from response.
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
    if (!SqlUtil.deleteOneByPkey(getDbConn(), "t_user", io, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  private void deleteDetail(final Io io) {
    SqlUtil.delete(getDbConn(), "t_user_pet", io, new String[] { "user_id" });
  }
}
```

---

## 8. Add Row/Remove Row Processing

**Usage**: Dynamically add or remove detail rows.

<!-- AI_SKIP_START -->
**Processing flow (Add row)**:
1. User clicks "Add Row" button.
2. `addRow()` is executed.
   1. Generate new row from template with `PageUtil.addRow('detail')`.
   2. Row is added at the end of detail table.

**Processing flow (Remove row)**:
1. User selects checkbox of row to delete.
2. Click "Remove Row" button.
3. `removeRow()` is executed.
   1. Delete checked rows with `PageUtil.removeRow('detail.chk', '1')`.
   2. Corresponding rows are removed from DOM.
<!-- AI_SKIP_END -->

### JavaScript (editpage.js)

```javascript
// Add row
const addRow = function () {
  PageUtil.addRow('detail');
};

// Remove row (delete checked rows)
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

**Usage**: Cancel editing and return to list.

<!-- AI_SKIP_START -->
**Processing flow**:
1. User clicks "Cancel" button.
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

## Validation Pattern List

| Check Description | Method | Usage Example |
|-------------|---------|--------|
| Required field check. | `ValUtil.isBlank(str)` | `if (ValUtil.isBlank(userId))` |
| Alphanumeric check. | `ValUtil.isAlphabetNumber(str)` | `if (!ValUtil.isAlphabetNumber(userId))` |
| Number check. | `ValUtil.isNumber(str)` | `if (!ValUtil.isNumber(incomeAm))` |
| Date check. | `ValUtil.isDate(str)` | `if (!ValUtil.isDate(birthDt))` |
| Length check. | `ValUtil.checkLength(str, len)` | `if (!ValUtil.checkLength(userId, 4))` |
| Numeric length check. | `ValUtil.checkLengthNumber(str, int, dec)` | `if (!ValUtil.checkLengthNumber(weight, 3, 1))` |

---

## Message Pattern

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
// → "U001 was registered."
```

---

## File Naming Rules

| Type | File Name | Class Name |
|------|-----------|----------|
| List initialization. | ExampleListInit.java | ExampleListInit |
| List search processing. | ExampleListSearch.java | ExampleListSearch |
| Data retrieval processing. | ExampleLoad.java | ExampleLoad |
| Registration/update processing. | ExampleUpsert.java | ExampleUpsert |
| Deletion processing. | ExampleDelete.java | ExampleDelete |

---

## Sample Code
- HTML/JavaScript: `pages/app/exmodule/`
- Java: `src/com/example/app/service/exmodule/`
- DB Definitions/Test Data: `example_db/example_data_create.sql`
