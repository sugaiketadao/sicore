# Web Page Structure Standards

## Overview
- Defines standard rules for developing browser-based systems.
- Adhering to these standards and this framework enables code standardization and improves development and maintenance efficiency.
- In this document, "list" refers collectively to both list (repeating rows) and detail (repeating rows). Sections discussing lists apply to both.
- This document uses the following samples for explanation:
    - HTML/JavaScript: `pages/app/exmodule/`
    - Java: `src/com/example/app/service/exmodule/`

## Prerequisites
- Use HTML5, ES6 (ECMAScript 2015), and CSS3+.

<!-- AI_SKIP_START -->
## Framework Features and Design Philosophy

This framework is designed with simplicity and development efficiency as priorities. The following features enable consistent development from prototype to production.

### HTML Files Work As-Is (Prototype-Driven Development)

**Prototypes run directly**:
```html
<!-- During prototyping -->
<input type="text" name="user_nm" value="Mike Davis">

<!-- Same HTML in production -->
<input type="text" name="user_nm">
```

**Advantages**:
- HTML created by designers can be used as-is.
- Usability can be verified at the prototype stage.
- No template engine is required.
- Development is possible with only HTML knowledge.

### Stateless Architecture

**No session on the server side**:
- State is managed using the browser's `sessionStorage`.
- Scaling out is easy.
- Not affected by server restarts.

**URL parameters can also be used**:
```javascript
// Pass data during page navigation
HttpUtil.movePage('editpage.html', {
  user_id: 'U001',
  upd_ts: '20250123T235959123456'
});
```

### Data Flow Between Browser and Server

Using list page search processing as an example, this shows the data flow from browser to server.

```
[Browser] List page listpage.html
     │
     │ 1. Search button clicked
     │
     ▼
[JavaScript] listpage.js
     │
     │ 2. Generate request JSON from browser input values using PageUtil.getValues()
     │    → { user_id: "U001", user_nm: "Mike Davis" }
     │
     │ 3. Send JSON using HttpUtil.callJsonService()
     │
     ▼
[Framework]
     │ 4. Convert request JSON → Io object
     │
     ▼
[Java] ExampleListSearch.java
     │
     │ 5. Build SQL using SqlBuilder from request values in Io object
     │
     │ 6. Retrieve from database using SqlUtil.selectBulk()
     │
     │ 7. Store database retrieval results → in Io object
     │
     ▼
[Framework]
     │ 8. Convert Io object → response JSON
     │
     ▼
[JavaScript] listpage.js
     │
     │ 9. Display response JSON in browser using PageUtil.setValues()
     │
     ▼
[Browser] Database retrieval results are displayed on the list page
```

### HTML ⇔ JSON Bidirectional Conversion Mechanism

One of the core features of this framework is automatic bidirectional conversion between HTML and JSON. This mechanism makes data exchange between frontend and backend extremely simple.

#### Conversion from Form Input Elements to JSON

**Corresponding HTML**:
```html
<!-- Simple inputs -->
<input type="text" name="user_id" value="U001">
<input type="text" name="user_nm" value="Mike Davis">
<input type="text" name="email" value="mike.davis@example.com">

<!-- Table row data -->
<table>
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
//     {"pet_nm": "Buddy", "weight_kg": "5.0"},
//     {"pet_nm": "Whiskers", "weight_kg": "2.5"}
//   ]
// }
```

**No `<form>` tag required**:
- `PageUtil.getValues()` automatically collects values.
- Retrieval is possible as long as the `name` attribute exists.
- Form submission events are not used.

#### Conversion from JSON to HTML

**JavaScript side**:
```javascript
// Receive response JSON from server
const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);

// Display response JSON in browser
PageUtil.setValues(res);
```

**Values are set automatically**:
```html
<!-- Before response -->
<input type="text" name="user_id">
<span data-name="user_nm"></span>

<!-- After response (set automatically) -->
<input type="text" name="user_id" value="U001">
<span data-name="user_nm">Mike Davis</span>
```
<!-- AI_SKIP_END -->

## File Structure

### Directory Structure
- [*] indicates items that can be changed in configuration files.
- Store multiple HTML files (hereinafter referred to as pages) that are functionally related in a module directory.
- Store JavaScript and CSS files for each page in the same module directory as the HTML file.
- The following is an example; directory names and depth levels are arbitrary.

```
[project root]/               # Project root: In most cases, this is the project name.
└── pages/                 # Web page directory [* Only directory name is configurable]
     ├── app/              # Application directory: Branch point from common component directory
     │   └── [module]/    # Module directory: Mainly stores HTML and JavaScript files.
     ├── util/             # Common component directory: Stores common components used by module pages.
     └── lib/              # Framework directory
```

### Directory Examples
- Module directory: [pages/app/exmodule/](../../pages/app/exmodule/)
- Framework directory: [pages/lib/](../../pages/lib/)

## HTML Rules

Defines the rules for writing HTML. The explanation proceeds from basic structure.

### Minimal Structure (Language: English)
```HTML
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>[Page Title]</title>
  <link href="../../lib/css/onepg-base.css" rel="stylesheet">
  <link href="[System-wide or module-wide common CSS file (optional)]" rel="stylesheet">
  <style>
    /* CSS for each page (optional) */
  </style>
</head>
<body>
<body>
  <main>
  [Page Content]
  </main>
  <script src="../../lib/js/onepg-utils.js" defer></script>
  <script src="[JavaScript file name for each page (optional)]" defer></script>
</body>
</html>
```

### HTML Tag Usage
- Do not use `type="number"` or `type="date"` added in HTML5 for `<input>`; use `type="text"` instead.
- Use `<button>` instead of `<input type="button">` for buttons.
- Use `<span>` for always non-editable text displays that show responses but do not need to be included in requests. For text display within lists, use `<td>` as-is.
- Use `<label>` for text displays associated with form input elements. Set the `id` attribute on form input elements and the `for` attribute on `<label>` as needed.
- For checkboxes and radio buttons, wrap the element and value name with `<label>`.
  [Example] `<label><input type="checkbox">Value Name</label>`

### name Attribute
- Add the `name` attribute to form input elements that are included in requests. The same applies to form input elements that display responses.
- When displaying responses using `<span>` or `<td>`, which do not originally have a `name` attribute, add the `data-name` attribute (custom attribute). In this framework, `data-name` and `name` attributes are treated the same for response display.
- Elements in list sections (repeating sections) follow these rules:
    - For elements within rows (hereinafter referred to as cell elements), set the `name` attribute using `.` as a separator in the format `tableId.fieldName`. The `.`-separated `name` attribute is used only for cell elements.
    - A parent or grandparent element of the cell element must have the `id` attribute set to the prefix before the `.` (hereinafter referred to as table element). In most cases, the table element is `<tbody>` or `<table>`.
    - Child elements directly under the table element (the element with the `id` attribute) must be the top-level elements of the repeating section (hereinafter referred to as row element). In most cases, the row element is `<tr>`.

### Naming for name Attribute and data-name Attribute (Custom Attribute)
- Typically, the values of `name` and `data-name` attributes match the physical field names of database columns used in web service input/output. If the same database column exists multiple times on the same page, add an appropriate suffix.
- By matching physical field names, the amount of web service processing code is reduced. If page input values are registered or updated directly to the database, the request from the web page can be passed directly to the SQL execution utility Java class to execute SQL without building the SQL manually.

| Use Case | Format | Example |
|-|-|-|
| Basic | Match the physical field name of the database column targeted for input/output or retrieval. | `user_id` |
| Cell element | Use "table element `id` attribute + '.' + physical field name". | `list.user_id` or `detail.user_id` |
| When duplicated (1) | Use "physical field name + '_' + appropriate suffix". | [Range for search conditions]<br>From: `birth_dt_from`<br>To: `birth_dt_to` |
| When duplicated (2) | Use "transaction item physical field name + '_' + appropriate suffix". | [Names from same master]<br>Walk duty name: `strollduty_id_name`<br>Meal duty name: `mealduty_id_name` |

<!-- AI_SKIP_START -->
#### Advantages of Unified Field Names
Unifying physical field names (DB column names = HTML `name` attribute = Java map key) provides the following advantages:

```sql
-- DB column
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

**Advantages**:
- **No conversion code required**: No need for camelCase conversion, etc.
- **Reduced code volume**: No need to write mapping logic.
- **Fewer bugs**: No conversion mistakes.
- **Improved maintainability**: Database design documents function as specifications.
<!-- AI_SKIP_END -->

### Applying Hidden / Disabled / Read-Only Styles
- Use the disabled attribute `disabled` when form input elements such as `<input>` become non-editable.
- Unlike standard form submission, note that disabled elements are included in requests in this framework.
- To hide elements, change the `display` style or `visibility` style.
    - When hiding with `display` style (`display:none`), the element's space is not preserved.
    - When hiding with `visibility` style (`visibility:hidden`), the element's space is preserved.
- Unlike standard form submission, note that hidden elements are excluded from requests in this framework.
- Use the read-only attribute `readonly` only when reusing an update page as a view page or delete page where all fields are non-editable.
- For `<select>` read-only behavior, apply `disabled` to `<option>` elements other than the selected value as an alternative.

### Page Section Division
- When processing the entire page with this framework's JavaScript components becomes verbose, divide the page into sections to narrow down the processing scope for efficiency.
- Divide into sections using `<section id="sectionName">`, setting the `id` attribute to a section name representing that part. HTML tags other than `<section>` can also be used.

### Section Naming Examples
| Section Use | Section Name | Example |
|-|-|-|
| Search conditions section | `searchConditionsArea` | `<section id="searchConditionsArea">` |
| Search results list section | `searchResultsArea` | `<section id="searchResultsArea">` |

### Framework Custom Attributes
- The following are element attributes uniquely defined by this framework; do not use them for other purposes.
- When using them, use the methods provided by this framework.

| Target Element | Custom Attribute | Example | Purpose |
|-|-|-|-|
| Non-form input elements | `data-name` | `data-name="user_nm"` | Alternative to `name` attribute |
| Form input elements | `data-obj-row-idx` | `data-obj-row-idx="0"` | Index value for array data in request |
| `<input type="checkbox">` | `data-check-off-value` | `value="1" data-check-off-value="0"` | Request data value when checkbox is unchecked |
| `<input type="text">` | `data-value-format-type` | `data-value-format-type="num"` | Format values on response and unformat on request based on this type |
| All elements | `data-style-display-backup` | `data-style-display-backup="inline-block"` | `display` style value before hiding, used when re-showing |
| All elements | `data-style-visibility-backup` | `data-style-visibility-backup="visible"` | `visibility` style value before hiding, used when re-showing |
| Error fields | `data-title-backup` | `data-title-backup="Title"` | `title` attribute value before error message was set, used when clearing |
| `<input type="radio">` | `data-radio-obj-name` | `data-radio-obj-name="gender_cs"` | `name` attribute value before row index was appended for radio buttons within row elements, used for requests |

<!-- AI_SKIP_START -->
#### Usage Examples and Advantages of data-name Attribute
**Purpose**: Display data on non-form elements (not retrieved for requests)

```html
<!-- Input/display element (name attribute) -->
<input type="text" name="user_id">

<!-- Display-only element (data-name attribute) -->
<span data-name="user_nm"></span>
<td data-name="list.pet_nm"></td>
```

**JavaScript processing**:
```javascript
// Retrieval gets only name attributes (data-name is not retrieved)
const values = PageUtil.getValues(); // name attributes only

// Setting applies to both name and data-name
PageUtil.setValues(values);          // Sets to both name and data-name
```

**Advantages**:
- Values can be set to `<span>`, `<td>`, etc.
- Displays the same way as the `name` attribute.
- Clearly separates display-only fields from input fields.
<!-- AI_SKIP_END -->

<!-- AI_SKIP_START -->
#### Usage Examples and Advantages of data-check-off-value Attribute
**Purpose**: Define the value when a checkbox is OFF

```html
<!-- Check ON: "1", OFF: "0" -->
<input type="checkbox" name="is_dog" value="1" data-check-off-value="0">

<!-- Check ON: "true", OFF: "false" -->
<input type="checkbox" name="is_cat" value="true" data-check-off-value="false">
```

**Request JSON**:
```javascript
// When checked ON
// { "is_dog": "1", "is_cat": "true" }

// When checked OFF
// { "is_dog": "0", "is_cat": "false" }
```

**Advantages**:
- By explicitly defining the checkbox OFF value, no supplementary logic for OFF state is required.
- `boolean` values can also be handled.

**Notes**:
- Do not use for optional search conditions.
<!-- AI_SKIP_END -->


### data-value-format-type Attribute Values
The configuration values for `data-value-format-type` are as follows:

| Value | Format Type Name | Field Value [Example] | Formatted Value |
|-|-|-|-|
| `num` | Number - comma-separated | `1000000` | `1,000,000` |
| `ymd` | Date - YYYY/MM/DD format | `20251231` | `2025/12/31` |
| `hms` | Time - HH:MI:SS format | `123456` | `12:34:56` |
| `upper` | Uppercase conversion (code/ID) | `abc123` | `ABC123` *Remains uppercase after unformat |

<!-- AI_SKIP_START -->
#### Usage Examples and Advantages of data-value-format-type Attribute
**Purpose**: Automatic value formatting and unformatting

```html
<!-- Number (comma-separated) -->
<input type="text" name="income_am" data-value-format-type="num">

<!-- Date (YYYY/MM/DD format) -->
<input type="text" name="birth_dt" data-value-format-type="ymd">

<!-- Time (HH:MI:SS format) -->
<input type="text" name="stroll_tm" data-value-format-type="hms">

<!-- Uppercase conversion -->
<input type="text" name="user_id" data-value-format-type="upper">
```

**JavaScript behavior**:
```javascript
// On set: Auto-format
PageUtil.setValues({
  income_am: "1200000",  // → value="1,200,000"
  birth_dt:  "19870321", // → value="1987/03/21"
  stroll_tm: "123456",   // → value="12:34:56"
  user_id:   "u001"      // → value="U001"
});

// On get: Auto-unformat
const req = PageUtil.getValues();
// {
//   income_am: "1200000",  // Commas removed
//   birth_dt:  "19870321", // Slashes removed
//   stroll_tm: "123456",   // Colons removed
//   user_id:   "U001"      // As-is
// }
```

**Processing on Java side**:
```java
// No formatting/unformatting needed on Java side
BigDecimal price = io.getBigDecimal("income_am");     // Gets (BigDecimal)1200000 as-is
LocalDate orderDate = io.getDateNullable("birth_dt"); // Gets (LocalDate)1987-03-21 as-is

// Can register directly to database
SqlUtil.insert(getDbConn(), "t_user", io);
```

**Advantages**:
- Automatically converts between display format and database storage values.
- No need to write formatting logic in Java or JavaScript.
- Separates appearance from data.
<!-- AI_SKIP_END -->


### Framework-Used HTML
- The following HTML is used by this framework; do not manipulate directly from individual feature processing.
- When using, operate through the methods provided by this framework.

| Purpose | HTML | Operation Example |
|-|-|-|
| Message display area | `<section id="_msg"></section>` | PageUtil.setMsg(res); |


## JavaScript Rules

Defines the rules for writing JavaScript.

### Writing Method
- Do not write JavaScript in HTML; write it in an external file and reference it.
- Dedicate one JavaScript file per page, with the file name matching the HTML file.
  [Example] For `listpage.html`, use `listpage.js`
- Event handlers for button elements, etc., should only execute a single function defined in an external file.
- Load JavaScript files at the end of `<body>`. Add the `defer` attribute when doing so.
- Write initialization JavaScript for page display at the end of the JavaScript file.
- When executing web services within JavaScript, use synchronous processing with `await`, so define functions in module JavaScript files with `async`.

```HTML
    <button type="button" onclick="insert()">Register</button>
    <script src="****.js" defer></script>
  </body>
</html>
```

```JavaScript
/**
 * Initialization.
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

// Execute initialization
init();
```

### Element Retrieval Methods
- Use the `name` attribute or `data-name` attribute to retrieve elements.
- For elements not handled in requests and responses (elements without `name` or `data-name` attributes), add an `id` attribute for retrieval.

### JavaScript Processing Scope
- JavaScript (browser-side) handles the following scope; other processing is handled by web service processing:
    - Displaying response data
    - Creating request data
    - Calling web server processing
    - Controlling element disable/hide/read-only states
    - Session management
- Initial values for form input elements displayed on page load must be returned as a response from a web service. Do not set initial values using JavaScript alone.

### Framework Basic Components
The methods of this framework's JavaScript component classes used in standard pages are as follows:

| Component Method | Purpose |
|-|-|
| `PageUtil.getValues()` | Get page data |
| `PageUtil.setValues()` | Set page data |
| `PageUtil.setMsg()` | Set message |
| `PageUtil.clearMsg()` | Clear message |
| `PageUtil.hasError()` | Check for error message presence |
| `HttpUtil.callJsonService()` | Call JSON web service |
| `HttpUtil.movePage()` | Navigate to specified URL |
| `HttpUtil.getUrlParams()` | Get URL parameters |
| `StorageUtil.getPageObj()` | Get page-level session data |
| `StorageUtil.setPageObj()` | Store page-level session data |
| `DomUtil.getByName()` | Get element by `name` attribute selector |
| `DomUtil.getById()` | Get element by `id` attribute selector |
| `DomUtil.setEnable()` | Toggle element enable/disable |
| `DomUtil.setVisible()` | Toggle element show/hide |

<!-- AI_SKIP_START -->
### StorageUtil Three-Tier Scope
Session storage can be managed in three scopes according to purpose:

| Scope | `StorageUtil` Get Method | Store Method | Purpose | Usage Example |
|-|-|-|-|-|
| Page-level | `getPageObj()` | `setPageObj()` | Per URL HTML file, data persistence within one page | Preserving search conditions on list page |
| Module-level | `getModuleObj()` | `setModuleObj()` | Per URL module directory, data sharing between pages | Preserving in-progress data between header edit page ⇔ detail edit page |
| System-level | `getSystemObj()` | `setSystemObj()` | Data sharing across entire system | Preserving login information |
<!-- AI_SKIP_END -->


## CSS Rules

### Selectors
- Use class selectors for page-level CSS; do not use the following selectors:
    - Element selectors
    - Attribute selectors
    - ID selectors
    - Universal selectors

### Writing Method
- Do not write CSS in HTML; write it in an external file and reference it.
- Dedicate one CSS file per page, with the file name matching the HTML file.
  [Example] For `listpage.html`, use `listpage.css`
- Load CSS files at the end of `<head>`.
- Direct specification of table, column, and field sizes in HTML is permitted.
- When writing directly in HTML, use the `style` attribute on each element or write in `<style>` after the CSS file reference in `<head>`.

### Size Specification Units
- When specifying sizes, use `rem` units.

### Grid Layout CSS Classes
- Use the grid system for element placement.
- Use the following CSS classes from this framework for grid layout.
- The trailing number in column element CSS class names represents the column width (the `*` portion in the table below).
- Specify column widths from `1` to `12` so they total 12.

| CSS Class | Purpose | Rule |
|-|-|-|
| `.grid-row` | Row element for grid layout | Use on `<div>` |
| `.grid-col-*` | Column element for grid layout | Use on `<div>` and place directly under `<div.grid-row>` |

<!-- AI_SKIP_START -->
#### Grid Layout Usage Examples
**Grid system**:
```html
<!-- 3-column grid in 1 row (grid-col-4 × 3 = 12) -->
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

<!-- 2-column grid in 1 row (grid-col-6 × 2 = 12) -->
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

**Key points**:
- Grid is a 12-division system
- Arrange so column totals per row equal 12
- Example: `grid-col-4` × `3` = 12, `grid-col-6` × `2` = 12, `grid-col-6` + `4` + `2` = 12

**Advantages**:
- Few class names to remember.
- Already responsive.
- Composed of custom CSS only (no external frameworks).
<!-- AI_SKIP_END -->

### Form Input Element CSS Classes
Use the following CSS classes from this framework for form input element placement.

| CSS Class | Purpose | Rule |
|-|-|-|
| `.item-head` | Label element placement per field | Use on `<div>` and place directly under `<div.grid-col-*>` for grid layout |
| `.item-body` | Form input element placement per field | Use on `<div>` and place directly under `<div.grid-col-*>` for grid layout |

```html
<div class="grid-col-1">
  <div class="item-head"><label>Field Name</label></div>
  <div class="item-body"><input type="text"></div>
</div>
```

### Table Element CSS Classes
Use the following CSS classes from this framework for table element placement.

| CSS Class | Purpose | Rule |
|-|-|-|
| `.table` | Table element placement | Use on the `<div>` that is the parent element of `<table>` |

```html
<div class="table"><table></table></div>
```


## Dynamic List Display

Explains data display for lists (repeating sections).

### Display Mechanism
- JSON for list sections is an array of objects.
- List data is displayed by this framework's JavaScript components as follows:
    1. Retrieve the row element that serves as a template (hereinafter referred to as template row element).
    2. Generate row elements from the template row element for the number of array elements (number of rows) in the list data.
    3. Set the object values for each row.
- Place the template row element as a child element (first position) of the table element, wrapped in `<script type="text/html">`.

### Dynamic List Display Example

```javascript
// Response JSON
{
  "list": [
    {"user_id": "U001", "user_nm": "Mike Davis"},
    {"user_id": "U002", "user_nm": "IKEDA Ken"}
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
    <tr><td><input type="text" name="list.user_id" value="U002"></td><td data-name="list.user_nm">IKEDA Ken</td></tr>
  </tbody>
</table>
```

## References

- [Web Service Structure Standards (Java)](../02-develop-standards/11-web-service-structure.md)
- [Event-Based Coding Patterns](../02-develop-standards/21-event-coding-pattern.md)
