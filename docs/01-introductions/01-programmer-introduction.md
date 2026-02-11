# SICore Framework - Introduction for Programmers

This document introduces the features and basic usage of the SICore framework.

---

## 1. Framework Features

### 1.1 Prototype-driven Development

```html
<!-- Prototype -->
<input type="text" name="user_nm" value="Mike Davis">

<!-- Production uses same HTML -->
<input type="text" name="user_nm">
```

**Benefits**:
- HTML created by designers can be used as is.
- Usability can be verified at the prototype stage.
- No template engine required.

### 1.2 Communication Using Only JSON

```
Browser              Server
  │                   │
  │------ JSON ------>│
  │           Web service processing
  │<------ JSON ------│
```

**Benefits**:
- Simple protocol.
- Frontend and backend are completely separated.
- Both API development and screen development use the same mechanism.
- Easy integration with mobile apps.

### 1.3 URL Maps Directly to Class Name

```
URL: http://localhost:8080/services/exmodule/ExampleListSearch
↓ Automatic mapping
Executed class: com.example.app.service.exmodule.ExampleListSearch
```

**Benefits**:
- No routing configuration required.
- No annotations required.
- Class name can be clearly identified from URL.

### 1.4 Stateless Architecture

- No server-side session.
- State managed by browser's `sessionStorage`.
- Easy to scale out.

### 1.5 Three-tier Scope of Session Storage

| Scope | Method | Usage Example |
|-|-|-|
| Page unit | `get/setPageObj()` | Hold search conditions on list page. |
| Module unit | `get/setModuleObj()` | Hold data being entered between pages. |
| System unit | `get/setSystemObj()` | Hold login information. |

### 1.6 Original CSS (No External Framework)

```html
<!-- 12-column grid system -->
<div class="grid-row">
  <div class="grid-col-6">1/2 of full width</div>
  <div class="grid-col-6">1/2 of full width</div>
</div>

<!-- Responsive ready -->
<!-- Smartphone: Columns automatically switch to vertical layout -->
<!-- Tablet: Label elements and form input elements within columns automatically switch to vertical layout -->
```

**Benefits**:
- No external framework like Bootstrap required.
- Consists of only minimal necessary CSS (onepg-base.css single file).
- Responsive support ready (PC, tablet, smartphone).
- Low learning cost (fewer class names to remember).

---

## 2. Data Flow

```
[Browser] listpage.html
     │
     │ 1. Search button pressed
     ▼
[JavaScript] listpage.js
     │ 2. PageUtil.getValues() → { user_id: "U001", user_nm: "Mike Davis" }
     │ 3. Send JSON with HttpUtil.callJsonService()
     ▼
[Framework]
     │ 4. Request JSON → Convert to Io object
     ▼
[Java] ExampleListSearch.java
     │ 5. Build SQL with SqlBuilder
     │ 6. Retrieve from database with SqlUtil.selectBulk()
     │ 7. Result → Store in Io object
     ▼
[Framework]
     │ 8. Io object → Convert to response JSON
     ▼
[JavaScript]
     │ 9. Display in browser with PageUtil.setValues()
     ▼
[Browser] Database retrieval result displayed on list
```

---

## 3. Automatic HTML⇔JSON Conversion

### 3.1 HTML to JSON

```html
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="Mike Davis">

<tbody id="list">
  <tr>
    <td><input name="list.pet_nm" value="Pochi"></td>
    <td><input name="list.weight_kg" value="5.0"></td>
  </tr>
  <tr>
    <td><input name="list.pet_nm" value="Tama"></td>
    <td><input name="list.weight_kg" value="2.5"></td>
  </tr>
</tbody>
```

```javascript
const req = PageUtil.getValues();
// {
//   "user_id": "U001",
//   "user_nm": "Mike Davis",
//   "list": [
//     {"pet_nm": "Pochi", "weight_kg": "5.0"},
//     {"pet_nm": "Tama", "weight_kg": "2.5"}
//   ]
// }
```

- No `<form>` tag required.
- Values can be retrieved with just the `name` attribute.

### 3.2 JSON to HTML

```javascript
const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
PageUtil.setValues(res);
```

```html
<!-- Automatically set -->
<input type="text" name="user_id" value="U001">
<span data-name="user_nm">Mike Davis</span>
```

---

## 4. Custom HTML Attributes

### 4.1 data-name Attribute

Display only, does not retrieve values:

```html
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

### 4.2 data-check-off-value Attribute

Specifies the value to send when checkbox is OFF:

```html
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">
```

### 4.3 data-value-format-type Attribute

Automatically formats display values:

| Setting Value | Item Value | Formatted Value |
|-|-|-|
| `num` | `1000000` | `1,000,000` |
| `ymd` | `20251231` | `2025/12/31` |
| `hms` | `123456` | `12:34:56` |

```html
<input type="text" name="income_am" data-value-format-type="num">
```

---

## 5. Web Service Implementation

### 5.1 Basic Structure

```java
package com.example.app.service.exmodule;

public class ExampleListSearch extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    // Implement processing
  }
}
```

- Inherit from `AbstractDbAccessWebService`.
- `io` is both the request and the response.

### 5.2 Io Class

```java
// Retrieve values type-safely
String userId = io.getString("user_id");
long incomeAm = io.getLong("income_am");
LocalDate birthDt = io.getDateNullable("birth_dt"); // Explicitly indicates possibility of returning null

// Set values (no type specification required)
io.put("user_nm", "Mike Davis");
io.put("income_am", 1230000);
```

### 5.3 Request and Response Integration

```java
public void doExecute(Io io) throws Exception {
  SqlBuilder sb = new SqlBuilder();
  sb.addQuery("SELECT u.user_nm, u.email FROM t_user u ");
  sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));

  final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
  if (!ValUtil.isNull(row)) {
    io.putAll(row);
  } else {
    io.putMsg(MsgType.ERROR, "x0001");
  }
  // io becomes the response as is
}
```

---

## 6. Transaction Management

```java
public class ExampleUpsert extends AbstractDbAccessWebService {
  
  @Override
  public void doExecute(Io io) throws Exception {
    if (ValUtil.isBlank(io.getString("user_id"))) {
      io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
      return; // → Has error → Automatic rollback
    }
    
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    // Normal termination (no error) → Automatic commit
  }
}
```

**Benefits**:
- No need to explicitly write commit/rollback.
- No forgetting rollback on error.
- Guarantees 1 request = 1 transaction.

---

## 7. SqlBuilder

```java
SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT u.user_id, u.user_nm, u.email ");
sb.addQuery(" FROM t_user u WHERE 1=1 ");

// Add condition only when value exists
sb.addQnotB(" AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB(" AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));

sb.addQuery(" ORDER BY u.user_id ");

IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

**Benefits**:
- SQL and parameters can be built simultaneously.
- SQL injection protected.
- SQL can be verified with `toString()`.

---

## 8. Message Display

**msg.json**:
```json
{
  "ev001": "{0} is required.",
  "i0002": "Registered {0}."
}
```

**Java side**:
```java
io.putMsg(MsgType.ERROR, "ev001", new String[]{"User ID"}, "user_id");
io.putMsg(MsgType.INFO, "i0002", new String[]{"U001"});
```

**JSON output**:
```json
{
  "_msgs": [{"type": "error", "id": "ev001", "text": "User ID is required.", "item": "user_id"}],
  "_has_err": true
}
```

---

## 9. Io Class Bug Prevention

### 9.1 NULL Safety

```java
String value = io.getString("key");         // Returns "".
String value = io.getStringNullable("key"); // Returns null (explicit).
```

### 9.2 Type Safety

```java
int age = io.getInt("age");
LocalDate birthDt = io.getDateNullable("birth_dt");
BigDecimal income = io.getBigDecimal("income_am");
```

### 9.3 Key Duplication Check

```java
io.put("user_id", "U001");
io.put("user_id", "U002");      // Causes error.
io.putForce("user_id", "U002"); // Explicitly overwrite.
```

### 9.4 Non-existent Key

```java
io.getString("userid"); // Non-existent key causes error.
io.getStringOrDefault("userid", ""); // Returns default value.
```

### 9.5 Safety Through Deep Copy

```java
// Deep copy on store
List<String> srcList = new ArrayList<>(Arrays.asList("A", "B"));
io.putList("items", srcList);
srcList.add("C");  // Modify original list
// io.getList("items") remains ["A", "B"] (no impact)

// Deep copy on retrieval
List<String> gotList = io.getList("items");
gotList.add("D");  // Modify retrieved list
// io.getList("items") remains ["A", "B"] (no impact)
```

> **Note**: Deep copying is performed, so repeated storage and retrieval of large data may impact performance.

---

## 10. Benefits of Unified Item Naming

```sql
CREATE TABLE t_user (user_id VARCHAR(10), user_nm VARCHAR(50));
```

```html
<input name="user_id">
<input name="user_nm">
```

```java
String userId = io.getString("user_id");
SqlUtil.insertOne(conn, "t_user", io);
```

**Benefits**:
- No conversion code required.
- No mapping processing required.
- Reduces bugs.
- Database design document functions as specification document as is.

---

## Related Documents

- [Introduction for Managers](../01-introductions/02-manager-introduction.md)
- [Web Page Structure Standard (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [Web Service Structure Standard (Java)](../02-develop-standards/11-web-service-structure.md)
- [Batch Processing Structure Standard (Java)](../02-develop-standards/12-batch-processing-structure.md)
