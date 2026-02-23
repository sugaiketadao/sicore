# Web Page Structure Standard

## Overview
- Defines standard rules for system development using browsers.
- By adhering to this standard and this framework, aims to unify program code and improve development and maintenance efficiency.
- In this document, terms are used with the following meanings:
    - Web page (screen): a single screen unit displayed in a browser (including HTML, CSS, and JavaScript)
    - HTML: the `.html` file itself, or the HTML language
    - Web service: server-side processing (business logic and DB operations implemented in Java)
- In this document, list pages (repeating rows) and detail data (repeating rows) are collectively called "lists", and sections explaining lists apply to both.
- This document explains using the following samples:
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## Prerequisites
- Use HTML5, ES6 (ECMAScript 2015), and CSS3+α.

<!-- AI_SKIP_START -->
## Framework Features and Design Philosophy

This framework is designed with emphasis on simplicity and development efficiency. The following features enable consistent development from prototype to production.

### HTML Files Can Be Used As Is (Prototype-driven Development)

**Prototype works as is**:
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
- Development possible with only HTML learning cost.

### Stateless Architecture

**No server-side session**:
- State managed by browser's `sessionStorage`.
- Easy to scale out.
- Not affected by server restart.

**URL parameters also available**:
```javascript
// Pass data during page transition
HttpUtil.movePage('editpage.html', {
  user_id: 'U001',
  upd_ts: '20250123T235959123456'
});
```

### Data Flow Between Browser⇔Server

The data flow from browser to server is shown using list page search processing as an example.

```
[Browser] List page listpage.html
     │
     │ 1. Search button pressed
     │
     ▼
[JavaScript] listpage.js
     │
     │ 2. Generate request JSON from browser input values via PageUtil.getValues()
     │    → { user_id: "U001", user_nm: "Mike Davis" }
     │
     │ 3. Send JSON via HttpUtil.callJsonService()
     │
     ▼
[Framework]
     │ 4. Request JSON → Convert to Io object
     │
     ▼
[Java] ExampleListSearch.java
     │
     │ 5. Build SQL with SqlBuilder from Io object request values
     │
     │ 6. Retrieve from database via SqlUtil.selectBulk()
     │
     │ 7. Database retrieval result → Store in Io object
     │
     ▼
[Framework]
     │ 8. Io object → Convert to response JSON
     │
     ▼
[JavaScript] listpage.js
     │
     │ 9. Display response JSON in browser via PageUtil.setValues()
     │
     ▼
[Browser] Database retrieval result displayed on list page
```

### HTML⇔JSON Conversion Mechanism

One of the core features of this framework is automatic conversion between HTML and JSON. This mechanism makes data exchange between frontend and backend extremely simple.

#### Form Input Elements to JSON Conversion

**Supported HTML**:
```html
<!-- Simple input -->
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="Mike Davis">
<input type="text" name="email" value="mike.davis@example.com">

<!-- Table row data -->
<table>
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
</table>
```

**JavaScript side**:
```javascript
// Generate request JSON from browser input values
const req = PageUtil.getValues();

// Execution result
// {
//   "user_id": "U001",
//   "user_nm": "Mike Davis",
//   "email": "mike.davis@example.com",
//   "list": [
//     {"pet_nm": "Pochi", "weight_kg": "5.0"},
//     {"pet_nm": "Tama", "weight_kg": "2.5"}
//   ]
// }
```

**No <form> tag required**:
- `PageUtil.getValues()` automatically collects values.
- Values can be retrieved with just the `name` attribute.
- Does not use form submission events.

#### JSON to HTML Conversion

**JavaScript side**:
```javascript
// Receive response JSON from server
const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);

// Display response JSON in browser
PageUtil.setValues(res);
```

**Values automatically set**:
```html
<!-- Before response -->
<input type="text" name="user_id">
<span data-name="user_nm"></span>

<!-- After response (automatically set) -->
<input type="text" name="user_id" value="U001">
<span data-name="user_nm">Mike Davis</span>
```
<!-- AI_SKIP_END -->

## File Structure

### Directory Structure
- [※] indicates parts that can be changed in configuration files.
- Store multiple (or single) HTML files (hereinafter called pages) that are functionally cohesive in the module directory.
- JavaScript files and CSS files for each page are stored in the same module directory as the HTML files.
- The following is an example; directory names and hierarchy depth are arbitrary.

```
[project root]/               # Project root: In most cases, becomes the project name.
└── pages/                 # Web page directory [※Directory name only configurable]
     ├── app/              # Module directory: Branch point from common component directory
     │   └── [module]/    # Module directory: Mainly stores HTML files and JavaScript files.
     ├── util/             # Common component directory: Stores common components used from module pages.
     └── lib/              # This framework directory
```

### Directory [Example]
- Module directory: [pages/app/exmodule/](../../pages/app/exmodule/)
- Framework directory: [pages/lib/](../../pages/lib/)

## HTML Rules

Defines HTML writing rules. Explains starting from basic structure.

### Minimum Structure
```HTML
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>【Page Title】</title>
  <link href="../../lib/css/onepg-base.css" rel="stylesheet">
  <link href="【System-wide or module-wide common CSS file (optional)】" rel="stylesheet">
  <style>
    /* Page-specific CSS (optional) */
  </style>
</head>
<body>
<body>
  <main>
  【Page Content】
  </main>
  <script src="../../lib/js/onepg-utils.js" defer></script>
  <script src="【Page-specific JavaScript file name (optional)】" defer></script>
</body>
</html>
```

### HTML Tag Usage
- Do not use `<input>` `type="number"` or `type="date"` added in HTML5; use `type="text"`.
- For buttons, do not use `<input type="button"`; use `<button>`.
- For text display that shows response but does not need to be requested and is always non-editable, use `<span>`. For text display in lists, use `<td>` as is.
- For text display associated with form input elements, use `<label>`, and set `id` attribute to form input elements and `for` attribute to `<label>` as needed.
- For checkboxes and radio buttons, enclose element and value name with `<label>`.
  [Example] `<label><input type="checkbox">Value Name</label>`

### name Attribute
- Add `name` attribute to form input elements for requests. Same for form input elements that display responses.
- When displaying response with `<span>` or `<td>` that originally do not have `name` attribute, add `data-name` attribute (custom attribute). This framework treats `data-name` attribute and `name` attribute the same for response display.
- Elements in list parts (repeating parts) follow the rules below:
    - Elements within rows (hereinafter called row elements) set `name` attribute with `.` delimiter in the format `table id.item name`. Use `.`-delimited `name` attribute only for row elements.
    - An element (hereinafter called table element) with the prefix part before `.` delimiter `table id` as `id` attribute must exist as parent or grandparent element of row elements. In most cases, table element is `<tbody>` or `<table>`.
    - Direct child elements of table element (element assigned `id` attribute) must be the top-level element of the repeating part (hereinafter called row element). In most cases, row element is `<tr>`.
　
### name Attribute / data-name Attribute (Custom Attribute) Naming
- Normally, values of `name` attribute and `data-name` attribute match the DB item physical name for input/output in web service processing. When the same DB item exists multiple times on the same page, add appropriate suffix.
- By matching DB item physical name, code amount in web service processing is reduced. If page input values are to be registered or updated in DB as is, SQL can be executed without building SQL by passing requests from web page directly to SQL execution utility Java class.

| Usage | Format | Example |
|-|-|-|
| Basic | Match DB item physical name that is input/output target or retrieval target. | `user_id` |
| Row element | "table element `id` attribute + "." + DB item physical name". | `list.user_id` or `detail.user_id` |
| When duplicate 1 | "DB item physical name + "_" + appropriate suffix". | 【DB retrieval condition from/to】<br>From: `birth_dt_from`<br>To: `birth_dt_to`|
| When duplicate 2 | "Transaction item DB item physical name + "_" + appropriate suffix". | 【Names from same master】<br>Walk duty name: `strollduty_id_name`<br>Meal duty name: `mealduty_id_name` |

<!-- AI_SKIP_START -->
#### Benefits of Unified Item Naming
By unifying DB item physical name = HTML `name` attribute = Java map key, the following benefits are achieved:

```sql
-- DB items
CREATE TABLE t_user (
  user_id VARCHAR(10),
  user_nm VARCHAR(50),
  ...
);
```

```html
<!-- HTML -->
<input name="user_id">
<input name="user_nm">
```

```java
// Java map key
String userId = io.getString("user_id");
String userNm = io.getString("user_nm");

// Auto-bind with SqlUtil
SqlUtil.insertOne(conn, "t_user", io);
```

**Benefits**:
- **No conversion code required**: No need for camel case conversion.
- **Reduced code amount**: No need to write mapping processing.
- **Reduced bugs**: No conversion errors.
- **Improved maintainability**: Database design document functions as specification document as is.
<!-- AI_SKIP_END -->

### Hidden/Disabled/Read-only Style Application
- When form input elements like `<input>` become non-editable, use the disabled attribute `disabled`.
- Unlike normal form submission, in this framework disabled elements are also request targets, so be careful.
- To hide elements, change `display` style or `visibility` style.
    - When hiding with `display` style (`display:none`), element space is not preserved.
    - When hiding with `visibility` style (`visibility:hidden`), element space is preserved.
- Unlike normal form submission, in this framework hidden elements are not request targets, so be careful.
- Use read-only attribute `readonly` only when diverting update page to inquiry page or delete page where all items are non-editable.
- For read-only `<select>`, substitute by applying `disabled` to `<option>` other than selected value.

### Page Section Division
- When processing the entire page with this framework's JavaScript components becomes redundant, efficiency can be improved by dividing the page into sections and narrowing down the processing targets.
- Divide sections by enclosing with `<section id="section name">`, and set section name representing that part to `id` attribute. HTML tags other than `<section>` can also be used.

### Section Naming Examples
| Section Usage | Section Name | Example |
|-|-|-|
| Database retrieval condition part | `searchConditionsArea` | `<section id="searchConditionsArea">` |
| Database retrieval result list part | `searchResultsArea` | `<section id="searchResultsArea">` |

### Framework Custom Attributes
- The following are element attributes uniquely defined by this framework, so do not use for other purposes.
- When using, use from methods prepared by this framework.

| Target Element | Custom Attribute | Example | Usage |
|-|-|-|-|
| Elements other than form input elements | `data-name` | `data-name="user_nm"` | Alternative to `name` attribute |
| Form input elements | `data-obj-row-idx` | `data-obj-row-idx="0"` | Index value of array data in request |
| `<input type="checkbox">` | `data-check-off-value` | `value="1" data-check-off-value="0"` | Request data value when check is OFF |
| `<input type="text">` | `data-value-format-type` | `data-value-format-type="num"` | Format value on response and unformat on request according to this type |
| All elements | `data-style-display-backup` | `data-style-display-backup="inline-block"` | Used to restore with `display` style value before hiding |
| All elements | `data-style-visibility-backup` | `data-style-visibility-backup="visible"` | Used to restore with `visibility` style value before hiding |
| Error items | `data-title-backup` | `data-title-backup="Title"` | Used to clear with `title` attribute value before setting error message |
| `<input type="radio">` | `data-radio-obj-name` | `data-radio-obj-name="gender_cs"` | Used on request with `name` attribute value before row index assignment for radio button in row element |

<!-- AI_SKIP_START -->
#### data-name Attribute Usage Examples and Benefits
**Usage**: Data display with elements other than form elements (does not retrieve)

```html
<!-- Input/display element (name attribute) -->
<input type="text" name="user_id">

<!-- Display-only element (data-name attribute) -->
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

**JavaScript processing**:
```javascript
// On retrieval, retrieves only name attribute (does not retrieve data-name)
const values = PageUtil.getValues(); // name attribute only

// On set, sets both name and data-name
PageUtil.setValues(values);          // Sets both name and data-name
```

**Benefits**:
- Can set values to `<span>`, `<td>`, and others.
- Can display the same way as `name` attribute.
- Can clearly separate display-only fields and input fields.
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### data-check-off-value Attribute Usage Examples and Benefits
**Usage**: Define checkbox OFF value

```html
<!-- When checked ON: "1", when OFF: "0" -->
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">

<!-- When checked ON: "true", when OFF: "false" -->
<input type="checkbox" name="is_cat" value="true" data-check-off-value="false">
```

**Request JSON**:
```javascript
// When checked ON
// { "is_dog": "1", "is_cat": "true" }

// When checked OFF
// { "is_dog": "0", "is_cat": "false" }
```

**Benefits**:
- By explicitly defining checkbox OFF value, completion logic for OFF state becomes unnecessary.
- Can also handle `boolean` values.

**Note**:
- Do not use for non-mandatory database retrieval conditions.
<!-- AI_SKIP_END -->


### data-value-format-type Attribute Setting Values
The setting values for `data-value-format-type` are as follows.

| Setting Value | Format Type Name | Item Value [Example] | Formatted Value |
|-|-|-|-|
| `num` | Number - comma-separated | `1000000` | `1,000,000` |
| `ymd` | Date - YYYY/MM/DD format | `20251231` | `2025/12/31` |
| `hms` | Time - HH:MI:SS format | `123456` | `12:34:56` |

<!-- AI_SKIP_START -->
#### data-value-format-type Attribute Usage Examples and Benefits
**Usage**: Automatic value formatting/unformatting

```html
<!-- Number (comma-separated) -->
<input type="text" name="income_am" data-value-format-type="num">

<!-- Date (YYYY/MM/DD format) -->
<input type="text" name="birth_dt" data-value-format-type="ymd">

<!-- Time (HH:MI:SS format) -->
<input type="text" name="stroll_tm" data-value-format-type="hms">

```

**JavaScript operation**:
```javascript
// On set: automatic formatting
PageUtil.setValues({
  income_am: "1200000",  // → value="1,200,000"
  birth_dt:  "19870321", // → value="1987/03/21"
  stroll_tm: "123456",   // → value="12:34:56"
  user_id:   "u001"      // → value="U001"
});

// On get: automatic unformatting
const req = PageUtil.getValues();
// {
//   income_am: "1200000",  // Comma removed
//   birth_dt:  "19870321", // Slash removed
//   stroll_tm: "123456",   // Colon removed
//   user_id:   "U001"      // As is
// }
```

**Processing on Java side**:
```java
// No need for formatting/unformatting on Java side
BigDecimal price = io.getBigDecimal("income_am");     // (BigDecimal)1200000 can be retrieved as is
LocalDate orderDate = io.getDateNullable("birth_dt"); // (LocalDate)1987-03-21 can be retrieved as is

// Can register to DB as is
SqlUtil.insert(getDbConn(), "t_user", io);
```

**Benefits**:
- Can automatically convert between display format and DB storage value.
- No need to write formatting processing in Java/JavaScript.
- Can separate appearance and data.
<!-- AI_SKIP_END -->


### Framework HTML Usage
- The following is HTML used by this framework, so do not directly operate from individual function processing.
- When using, operate from methods prepared by this framework.

| Usage | HTML | Operation Example |
|-|-|-|
| Message display area | `<section id="_msg"></section>` | PageUtil.setMsg(res); |


## JavaScript Rules

Defines JavaScript description rules.

### Description Method
- Do not write JavaScript within HTML; write it in external files and reference them.
- JavaScript dedicated to that page MUST be one file, and file name MUST be the same as HTML file.
  [Example] For `listpage.html`, use `listpage.js`
- Within event processing such as button elements, only execute one function defined in external file.
- Describe JavaScript files at the end within `<body>` to load them. At that time, add `defer` attribute.
- Initial processing JavaScript on page display is described at the end of JavaScript file.
- When executing web service within JavaScript, it becomes synchronous processing using `await`, so functions within module JavaScript file are defined with `async`.

```HTML
    <button type="button" onclick="insert()">Register</button>
    <script src="****.js" defer></script>
  </body>
</html>
```

```JavaScript
/**
 * Initial processing.
 */
const init = async function () {
  :
};

/**
 * Registration processing.
 */
const insert = async function () {
  :
};

// Execute initial processing
init();
```

### Element Retrieval Method
- Use `name` attribute or `data-name` attribute for element retrieval.
- Elements not handled in request and response (elements without `name` attribute or `data-name` attribute assigned) MUST add `id` attribute for retrieval.

### JavaScript Processing Scope
- In JavaScript (browser side), the following SHALL be processing scope, and other processing SHALL be web service processing.
    - Response data display
    - Request data creation
    - Web server processing invocation
    - Element disabling/hiding/read-only control
    - Browser storage management
- Initial values for form input elements to be set on page load are returned as response from web service, and do not set initial values with JavaScript only.

### Framework Basic Components
Methods of JavaScript component classes of this framework used in standard pages are as follows.

| Component Method | Usage |
|-|-|
| `PageUtil.getValues()` | Retrieve page data |
| `PageUtil.setValues()` | Set page data |
| `PageUtil.setMsg()` | Set message |
| `PageUtil.clearMsg()` | Clear message |
| `PageUtil.hasError()` | Check for error message |
| `HttpUtil.callJsonService()` | Call JSON web service |
| `HttpUtil.movePage()` | Navigate to specified URL |
| `HttpUtil.getUrlParams()` | Retrieve URL parameters |
| `StorageUtil.getPageObj()` | Retrieve page-level browser storage data |
| `StorageUtil.setPageObj()` | Store page-level browser storage data |
| `DomUtil.getByName()` | Retrieve `name` attribute selector element |
| `DomUtil.getById()` | Retrieve `id` attribute selector element |
| `DomUtil.setEnable()` | Toggle element enable state |
| `DomUtil.setVisible()` | Toggle element visibility |

<!-- AI_SKIP_START -->
### StorageUtil 3-Tier Scope
Browser storage can be managed with three scopes according to usage.

| Scope | `StorageUtil` Retrieval Method | Storage Method | Usage | Usage Example |
|-|-|-|-|-|
| Page-level | `getPageObj()` | `setPageObj()` | URL HTML file level, data retention within one page | Retaining search conditions in list page |
| Module-level | `getModuleObj()` | `setModuleObj()` | URL module directory level, data sharing between pages | Retaining data being input between header edit page ⇔ detail edit page |
| System-level | `getSystemObj()` | `setSystemObj()` | Data sharing across entire system | Retaining login information |
<!-- AI_SKIP_END -->


## CSS Rules

### Selector
- In page-level CSS, use class selector, and do not use the following selectors.
    - Element selector
    - Attribute selector
    - ID selector
    - Universal selector

### Description Method
- Do not write CSS within HTML; write it in external files and reference them.
- CSS dedicated to that page MUST be one file, and file name MUST be the same as HTML file.
  [Example] For `listpage.html`, use `listpage.css`
- Describe CSS files at the end within `<head>` to load them.
- Size specification for tables, columns, and items is permitted to be directly written within HTML.
- When directly writing within HTML, write it in `style` attribute of each element or write it in `<style>` after CSS file reference within `<head>`.

### Size Specification Unit
- When specifying size, specify it in `rem` unit.

### Grid Layout CSS Classes
- Use grid system for element placement.
- Use the following CSS classes of this framework for grid layout.
- The trailing number of column element CSS class name represents column width. (The `*` part in the table below)
- Specify column width from `1` to `12` so that the total becomes 12.

| CSS Class | Usage | Rule |
|-|-|-|
| `.grid-row` | Grid layout row element | Use in `<div>` |
| `.grid-col-*` | Grid layout column element | Use in `<div>`, and place directly under `<div.grid-row>` |

<!-- AI_SKIP_START -->
#### Grid Layout Usage Examples
**Grid system**:
```html
<!-- 1 row 3 columns grid (grid-col-4 × 3 = 12) -->
<div class="grid-row">
  <div class="grid-col-4">
    <label>User ID</label>
    <input type="text" name="user_id">
  </div>
  <div class="grid-col-4">
    <label>User Name</label>
    <input type="text" name="user_nm">
  </div>
  <div class="grid-col-4">
    <label>Email</label>
    <input type="text" name="email">
  </div>
</div>

<!-- 1 row 2 columns grid (grid-col-6 × 2 = 12) -->
<div class="grid-row">
  <div class="grid-col-6">
    <label>User ID</label>
    <input type="text" name="user_id">
  </div>
  <div class="grid-col-6">
    <label>User Name</label>
    <input type="text" name="user_nm">
  </div>
</div>
```

**Points**:
- Grid is a 12-division system
- Arrange so that column total in one row becomes 12
- Example: `grid-col-4` × `3` = 12, `grid-col-6` × `2` = 12, `grid-col-6` + `4` + `2` = 12

**Benefits**:
- Few class names to remember.
- Responsive-ready.
- Composed of original CSS only (no external framework used).
<!-- AI_SKIP_END -->

### Form Input Element CSS Classes
Use the following CSS classes of this framework for form input element placement.

| CSS Class | Usage | Rule |
|-|-|-|
| `.item-head` | Item-level label element placement | Use in `<div>`, and in case of grid layout, place directly under `<div.grid-col-*>` |
| `.item-body` | Item-level form input element placement | Use in `<div>`, and in case of grid layout, place directly under `<div.grid-col-*>` |

```html
<div class="grid-col-1">
  <div class="item-head"><label>Item Name</label></div>
  <div class="item-body"><input type="text"></div>
</div>
```

### Table Element CSS Classes
Use the following CSS classes of this framework for table element placement.

| CSS Class | Usage | Rule |
|-|-|-|
| `.table` | Table element placement | Use in `<div>` which is parent element of `<table>` |

```html
<div class="table"><table></table></div>
```


## Dynamic List Display

Explains data display for lists (repeating parts).

### Display Mechanism
- JSON for list part MUST be an array of associative arrays.
- List data is displayed by JavaScript components of this framework in the following manner.
    1. Retrieves row element that becomes template (hereinafter called template row element).
    2. Generates row elements for the number of list data arrays (number of rows) from template row element.
    3. Sets associative array values for each row.
- Template row element is placed enclosed in `<script type="text/html">` as child element (first) of table element.

### Dynamic List Display Example

```javascript
// Response JSON
{
  "list": [
    {"user_id": "U001", "user_nm": "Mike Davis"},
    {"user_id": "U002", "user_nm": "Ken Ikeda"}
  ]
}
```

```html
<!-- Template (initial state) -->
<table>
  <thead><tr><th>ID</th><th>Name</th></tr></thead>
  <tbody id="list">
    <script type="text/html">
      <tr><td><input type="text" name="list.user_id"></td><td data-name="list.user_nm"></td></tr>
    </script>
  </tbody>
</table>

<!-- After data set -->
<table>
  <thead><tr><th>ID</th><th>Name</th></tr></thead>
  <tbody id="list">
    <script type="text/html">
      <tr><td><input type="text" name="list.user_id"></td><td data-name="list.user_nm"></td></tr>
    </script>
    <tr><td><input type="text" name="list.user_id" value="U001"></td><td data-name="list.user_nm">Mike Davis</td></tr>
    <tr><td><input type="text" name="list.user_id" value="U002"></td><td data-name="list.user_nm">Ken Ikeda</td></tr>
  </tbody>
</table>
```

## Related Documents

- [Web Service Structure Standard (Java)](../02-develop-standards/11-web-service-structure.md)
- [Event-Specific Coding Patterns](../02-develop-standards/21-event-coding-pattern.md)
