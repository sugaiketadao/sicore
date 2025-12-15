# SICore Framework Introduction for Programmers

This document introduces the features and basic usage of the SICore Framework.

---

## 1. Framework Features

### 1.1 Prototype-Driven Development

```html
<!-- During prototyping -->
<input type="text" name="user_nm" value="Mike Davis">

<!-- Same HTML in production -->
<input type="text" name="user_nm">
```

**Advantages**:
- Use HTML created by designers directly.
- Verify usability during the prototype stage.
- No template engine required.

### 1.2 JSON-Only Communication

```
Browser             Server
  │                   │
  │------ JSON ------>│
  │　　　　　　 Web Service Processing
  │<------ JSON ------│
```

**Advantages**:
- Simple protocol.
- Complete separation of frontend and backend.
- API development and screen development use the same mechanism.
- Easy integration with mobile apps.

### 1.3 URL Directly Maps to Class Name

```
URL: http://localhost:8080/services/exmodule/ExampleListSearch
↓ Auto-mapping
Executed Class: com.example.app.service.exmodule.ExampleListSearch
```

**Advantages**:
- No routing configuration required.
- No annotations required.
- Class name is clearly identifiable from URL.

### 1.4 Stateless Architecture

- No server-side sessions.
- State is managed in browser's `sessionStorage`.
- Easy to scale out.

### 1.5 Custom CSS (No External Frameworks)

```html
<!-- 12-column grid system -->
<div class="grid-row">
  <div class="grid-col-6">1/2 of full width</div>
  <div class="grid-col-6">1/2 of full width</div>
</div>

<!-- Responsive design included -->
<!-- Smartphone: Columns automatically stack vertically -->
<!-- Tablet: Labels and form input elements within columns automatically stack vertically -->
```

**Advantages**:
- No external frameworks like Bootstrap required.
- Minimal CSS only (single file: onepg-base.css).
- Responsive design included (PC, tablet, smartphone).
- Low learning cost (few class names to remember).

### 1.6 Three-Tier Scope in Session Storage

| Scope | Method | Use Case |
|-|-|-|
| Page | `get/setPageObj()` | Retain search conditions on list page. |
| Module | `get/setModuleObj()` | Retain data being entered between pages. |
| System | `get/setSystemObj()` | Retain login information. |

---

## 2. Data Flow

```
[Browser] listpage.html
     │
     │ 1. Click search button
     ▼
[JavaScript] listpage.js
     │ 2. PageUtil.getValues() → { user_id: "U001", user_nm: "Mike Davis" }
     │ 3. Send JSON via HttpUtil.callJsonService()
     ▼
[Framework]
     │ 4. Convert request JSON → Io object
     ▼
[Java] ExampleListSearch.java
     │ 5. Build SQL with SqlBuilder
     │ 6. Retrieve data with SqlUtil.selectBulk()
     │ 7. Store result → Io object
     ▼
[Framework]
     │ 8. Convert Io object → response JSON
     ▼
[JavaScript]
     │ 9. Display in browser with PageUtil.setValues()
     ▼
[Browser] Retrieved data displayed in list
```

---

## 3. HTML⇔JSON Auto-Conversion

### 3.1 HTML to JSON

```html
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="Mike Davis">

<tbody id="list">
  <tr>
    <td><input name="list.pet_nm" value="Buddy"></td>
    <td><input name="list.weight_kg" value="5.0"></td>
  </tr>
  <tr>
    <td><input name="list.pet_nm" value="Whiskers"></td>
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
//     {"pet_nm": "Buddy", "weight_kg": "5.0"},
//     {"pet_nm": "Whiskers", "weight_kg": "2.5"}
//   ]
// }
```

- No `<form>` tag is required.
- Values can be retrieved from elements with a `name` attribute.

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

Display only; does not retrieve the value:

```html
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

### 4.2 data-check-off-value Attribute

Specifies the value to send when the checkbox is OFF:

```html
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">
```

### 4.3 data-value-format-type Attribute

Automatically formats display values:

| Value | Field Value | Formatted Value |
|-|-|-|
| `num` | `1000000` | `1,000,000` |
| `ymd` | `20251231` | `2025/12/31` |
| `hms` | `123456` | `12:34:56` |
| `upper` | `abc123` | `ABC123` |

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
    // Implementation
  }
}
```

- Extend `AbstractDbAccessWebService`.
- `io` is both the request and response.

### 5.2 Io Class

```java
// Retrieve values with type safety
String userId = io.getString("user_id");
long incomeAm = io.getLong("income_am");
LocalDate birthDt = io.getDateNullable("birth_dt"); // Explicitly indicates it may return null

// Set values (no type specification needed)
io.put("user_nm", "Mike Davis");
io.put("income_am", 1230000);
```

### 5.3 Unified Request and Response

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
  // io becomes the response as-is
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
      return; // → Error exists → Auto rollback
    }
    
    SqlUtil.upsert(getDbConn(), "t_user_header", "user_id", io);
    
    io.putMsg(MsgType.INFO, "i0002", new String[]{io.getString("user_id")});
    // Normal completion (no error) → Auto commit
  }
}
```

**Advantages**:
- No need to explicitly write commit or rollback.
- No risk of forgetting to rollback on error.
- Guaranteed that 1 request = 1 transaction.

---

## 7. SqlBuilder

```java
SqlBuilder sb = new SqlBuilder();
sb.addQuery("SELECT u.user_id, u.user_nm, u.email ");
sb.addQuery(" FROM t_user u WHERE 1=1 ");

// Add conditions only when value exists
sb.addQnotB(" AND u.user_id = ? ", io.getString("user_id"));
sb.addQnotB(" AND u.user_nm LIKE '%' || ? || '%' ", io.getString("user_nm"));

sb.addQuery(" ORDER BY u.user_id ");

IoRows rows = SqlUtil.selectBulk(getDbConn(), sb);
```

**Advantages**:
- Build SQL and parameters simultaneously.
- SQL injection protection included.
- Use `toString()` to verify SQL.

---

## 8. Message Display

**msg.json**:
```json
{
  "ev001": "{0} is required.",
  "i0002": "{0} has been registered."
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

## 9. Bug Prevention in Io Class

### 9.1 NULL-Safe

```java
String value = io.getString("key");         // Returns "".
String value = io.getStringNullable("key"); // Returns null (explicit).
```

### 9.2 Type-Safe

```java
int age = io.getInt("age");
LocalDate birthDt = io.getDateNullable("birth_dt");
BigDecimal income = io.getBigDecimal("income_am");
```

### 9.3 Duplicate Key Check

```java
io.put("user_id", "U001");
io.put("user_id", "U002");      // Throws error.
io.putForce("user_id", "U002"); // Explicitly overwrites.
```

### 9.4 Non-Existent Key

```java
io.getString("userid"); // Throws error for non-existent key.
io.getStringOrDefault("userid", ""); // Returns default value.
```

### 9.5 Safety through Deep Copy

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

---

## 10. Benefits of Unified Field Names

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
- DB design documents serve directly as specifications.

---

## References

- [Introduction for Managers](../01-introductions/02-manager-introduction.md)
- [Web Page Structure Standards (HTML/JavaScript/CSS)](../02-develop-standards/01-web-page-structure.md)
- [Web Service Structure Standards (Java)](../02-develop-standards/11-web-service-structure.md)
