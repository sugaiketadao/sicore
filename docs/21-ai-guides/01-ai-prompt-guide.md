# AI Prompt Guide (for Business Screen Development)

This guide standardizes the instruction methods for having AI create business screens.

---

## Table of Contents

- [1. Basic Instruction Flow](#1-basic-instruction-flow)
- [2. Required Input Information](#2-required-input-information)
- [3. Prompt Templates](#3-prompt-templates)
- [4. DB Column to HTML Element Conversion Rules](#4-db-column-to-html-element-conversion-rules)
- [5. How to Write Code Value Definitions](#5-how-to-write-code-value-definitions)
- [6. List of Generated Files](#6-list-of-generated-files)
- [7. Example: User Management Screen](#7-example-user-management-screen)
- [8. Error Message Definitions](#8-error-message-definitions)
- [9. References](#9-references)

---

## 1. Basic Instruction Flow

```
1. Provide table definitions
2. Specify screen type (list + edit / list only / edit only)
3. Define code values
4. Specify validation requirements
5. Request generation
6. Request compile error fixes
7. Verify functionality
8. Request bug fixes
```

> **Note**: For details on steps 6-8, refer to [AI Prompt Guide (for Debugging and Fixes)](02-ai-debug-guide.md).

---

## 2. Required Input Information

### 2.1 Required Information

| Item | Description | Example |
|------|-------------|---------|
| Feature Name | Screen feature name | User Management |
| Module Name | Directory/package name (lowercase letters) | usermst |
| Table Definition | CREATE TABLE format | See below |
| Screen Type | list + edit / list only / edit only | list + edit |

### 2.2 Optional Information

| Item | Description | Example |
|------|-------------|---------|
| Code Value Definition | Option values and display names | Gender: M=Male, F=Female |
| Search Conditions | List screen search condition items | User ID (prefix match), Name (partial match) |
| Validation | Required, length, format | User ID: required, 4 digits, alphanumeric |
| Initial Values | Values to display initially | Today's date for birthday |

---

## 3. Prompt Templates

### 3.1 List + Edit Screen (Standard Pattern)

```markdown
## Business Screen Development Request

### Feature Name
{Feature Name}

### Module Name
{Module Name}

### Table Definition
```sql
create table t_{table_name} (
  {column definitions}
  ,primary key ({primary key})
);
```

### Code Value Definitions
- {column_name}_cs: {code}={display name}, {code}={display name}

### Screen Type
list + edit

### Search Conditions (List Screen)
- {item name}: {search method}

### Validation (Edit Screen)
- {item name}: {validation content}

### Generation Request
Generate the following files with the above requirements:
- pages/app/{module_name}/listpage.html
- pages/app/{module_name}/listpage.js
- pages/app/{module_name}/editpage.html
- pages/app/{module_name}/editpage.js
- src/com/example/app/service/{module_name}/{Module}ListInit.java
- src/com/example/app/service/{module_name}/{Module}ListSearch.java
- src/com/example/app/service/{module_name}/{Module}Load.java
- src/com/example/app/service/{module_name}/{Module}Upsert.java
- src/com/example/app/service/{module_name}/{Module}Delete.java

Follow the implementation patterns in docs/02-develop-standards/21-event-coding-pattern.md.
```

### 3.2 List Only (Reference Screen)

```markdown
## Business Screen Development Request

### Feature Name
{Feature Name}

### Module Name
{Module Name}

### Table Definition
{CREATE TABLE}

### Screen Type
list only (reference only)

### Generation Request
- pages/app/{module_name}/listpage.html
- pages/app/{module_name}/listpage.js
- src/com/example/app/service/{module_name}/{Module}ListInit.java
- src/com/example/app/service/{module_name}/{Module}ListSearch.java
```

### 3.3 Header + Detail Screen

```markdown
## Business Screen Development Request

### Feature Name
{Feature Name}

### Module Name
{Module Name}

### Header Table
```sql
create table t_{header} (
  {column definitions}
  ,primary key ({primary key})
);
```

### Detail Table
```sql
create table t_{detail} (
  {column definitions}
  ,primary key ({header primary key}, {detail sequence number})
);
```

### Screen Type
list + edit (header-detail structure)

### Generation Request
Make the detail table editable together with the header on the edit screen.
```

---

## 4. DB Column to HTML Element Conversion Rules

### 4.1 Automatic Determination by Suffix

| Suffix | Meaning | HTML Element | Example |
|--------|---------|--------------|---------|
| `_id` | ID/Code | `<input type="text">` | user_id |
| `_nm` | Name | `<input type="text">` | user_nm |
| `_cs` | Code Classification | `<select>` or `<radio>` | gender_cs |
| `_dt` | Date | `<input>` + `data-value-format-type="ymd"` | birth_dt |
| `_ts` | Timestamp | `<input type="hidden">` | upd_ts |
| `_am` | Amount | `<input>` + `data-value-format-type="num"` | income_am |
| `_kg`, `_cm` | Quantity | `<input>` + `data-value-format-type="num"` | weight_kg |
| `_no` | Number | `<input type="text">` or display only | pet_no |

### 4.2 Determination by Type

| DB Type | HTML Element |
|---------|--------------|
| VARCHAR(1) + `_cs` | `<radio>` or `<checkbox>` |
| VARCHAR(2+) + `_cs` | `<select>` |
| NUMERIC | `<input type="text" class="align-right">` |
| DATE | `<input>` + `data-value-format-type="ymd"` |
| TIMESTAMP | `<input type="hidden">` (for optimistic locking) |

### 4.3 HTML Attribute Usage

| Purpose | Attribute | Example |
|---------|-----------|---------|
| Input & Retrieval | `name` | `<input name="user_id">` |
| Display Only | `data-name` | `<td data-name="user_nm">` |
| Numeric Comma Formatting | `data-value-format-type="num"` | `1000000` → `1,000,000` |
| Date Slash Formatting | `data-value-format-type="ymd"` | `20251231` → `2025/12/31` |
| Checkbox OFF Value | `data-check-off-value` | `<input type="checkbox" data-check-off-value="N">` |

---

## 5. How to Write Code Value Definitions

### 5.1 Standard Format

```markdown
### Code Value Definitions
- gender_cs (Gender): M=Male, F=Female
- country_cs (Country): JP=Japan, US=United States, BR=Brazil, AU=Australia
- type_cs (Type): DG=Dog, CT=Cat, BD=Bird
- spouse_cs (Spouse): Y=Yes, N=No *Checkbox
- vaccine_cs (Vaccinated): Y=Vaccinated, N=Not vaccinated *Checkbox
```

### 5.2 HTML Generation Examples

**For select**:
```html
<select name="country_cs">
  <option value="">Not selected</option>
  <option value="JP">Japan</option>
  <option value="US">United States</option>
</select>
```

**For radio**:
```html
<label><input type="radio" name="gender_cs" value="">Not selected</label>
<label><input type="radio" name="gender_cs" value="M">Male</label>
<label><input type="radio" name="gender_cs" value="F">Female</label>
```

**For checkbox**:
```html
<label><input type="checkbox" name="spouse_cs" value="Y" data-check-off-value="N">Yes</label>
```

---

## 6. List of Generated Files

### 6.1 For List + Edit Screen

```
pages/app/{module}/
├── listpage.html      # List screen HTML
├── listpage.js        # List screen JavaScript
├── editpage.html      # Edit screen HTML
└── editpage.js        # Edit screen JavaScript

src/com/example/app/service/{module}/
├── {Module}ListInit.java    # List initialization
├── {Module}ListSearch.java  # List search
├── {Module}Load.java        # Data retrieval
├── {Module}Upsert.java      # Insert/Update
└── {Module}Delete.java      # Delete
```

### 6.2 File Naming Conventions

| File Type | Naming Convention | Example |
|-----------|-------------------|---------|
| List HTML | listpage.html | listpage.html |
| Edit HTML | editpage.html | editpage.html |
| List JS | listpage.js | listpage.js |
| Edit JS | editpage.js | editpage.js |
| Java List Init | {Module}ListInit.java | UserListInit.java |
| Java List Search | {Module}ListSearch.java | UserListSearch.java |
| Java Data Retrieval | {Module}Load.java | UserLoad.java |
| Java Insert/Update | {Module}Upsert.java | UserUpsert.java |
| Java Delete | {Module}Delete.java | UserDelete.java |

---

## 7. Example: User Management Screen

### 7.1 Complete Prompt Example

```markdown
## Business Screen Development Request

### Feature Name
User Management

### Module Name
exmodule

### Header Table
```sql
create table t_user (
  user_id varchar(4)
 ,user_nm varchar(20)
 ,email varchar(50)
 ,country_cs varchar(2)
 ,gender_cs varchar(1)
 ,spouse_cs varchar(1)
 ,income_am numeric(10)
 ,birth_dt date
 ,upd_ts timestamp(6)
 ,primary key (user_id)
);
```

### Detail Table
```sql
create table t_user_pet (
  user_id varchar(4)
 ,pet_no numeric(2)
 ,pet_nm varchar(10)
 ,type_cs varchar(2)
 ,gender_cs varchar(1)
 ,vaccine_cs varchar(1)
 ,weight_kg numeric(3,1)
 ,birth_dt date
 ,upd_ts timestamp(6)
 ,primary key (user_id, pet_no)
);
```

### Code Value Definitions
- country_cs (Country): JP=Japan, US=United States, BR=Brazil, AU=Australia
- gender_cs (Gender): M=Male, F=Female
- spouse_cs (Spouse): Y=Yes, N=No *Checkbox
- type_cs (Pet Type): DG=Dog, CT=Cat, BD=Bird
- vaccine_cs (Vaccination): Y=Vaccinated, N=Not vaccinated *Checkbox

### Screen Type
list + edit (header-detail structure)

### Search Conditions (List Screen)
- user_id: prefix match
- user_nm: partial match
- email: partial match
- country_cs: exact match (dropdown)
- gender_cs: exact match (radio)
- spouse_cs: condition only when checked
- income_am: greater than or equal
- birth_dt: on or after

### Validation (Edit Screen)
Header:
- user_id: required, fixed 4 digits, alphanumeric
- user_nm: required, 20 characters or less
- email: 50 characters or less
- income_am: numeric, 10 digits or less
- birth_dt: date format

Detail:
- pet_nm: required, 10 characters or less
- weight_kg: numeric (1 decimal place)

### Initial Values
- Display 5 empty detail rows for new registration

### Generation Request
Generate the following files with the above requirements:
- pages/app/exmodule/listpage.html
- pages/app/exmodule/listpage.js
- pages/app/exmodule/editpage.html
- pages/app/exmodule/editpage.js
- src/com/example/app/service/exmodule/ExampleListInit.java
- src/com/example/app/service/exmodule/ExampleListSearch.java
- src/com/example/app/service/exmodule/ExampleLoad.java
- src/com/example/app/service/exmodule/ExampleUpsert.java
- src/com/example/app/service/exmodule/ExampleDelete.java

Follow the implementation patterns in docs/02-develop-standards/21-event-coding-pattern.md.
```

---

## 8. Error Message Definitions

### 8.1 Standard Message IDs

**Validation Errors (ev***)**:
| ID | Message | Usage |
|----|---------|-------|
| ev001 | {0} is required. | Required check |
| ev011 | {0} must be alphanumeric. | Format check |
| ev012 | {0} must be a number. | Numeric check |
| ev013 | {0} must be in date format. | Date check |
| ev021 | {0} must be {1} characters or less. | Length check |
| ev022 | {0} must be exactly {1} characters. | Fixed length check |

**Business Errors (e****)**:
| ID | Message | Usage |
|----|---------|-------|
| e0001 | {0} is already registered. | Duplicate error |
| e0002 | {0} has been updated by another user. | Optimistic locking error |

**Success Messages (i****)**:
| ID | Message | Usage |
|----|---------|-------|
| i0001 | {0} has been registered. | New registration success |
| i0002 | {0} has been updated. | Update success |
| i0003 | {0} has been deleted. | Delete success |

---

## 9. References

- [Prompt Generation Template](11-ai-prompt-generator-template.md) - Template for generating prompts from table definitions
- [Event-based Coding Patterns](../02-develop-standards/21-event-coding-pattern.md) - Implementation pattern details
- [Web Page Structure Standard](../02-develop-standards/01-web-page-structure.md) - HTML/JS structure details
- [Web Service Structure Standard](../02-develop-standards/11-web-service-structure.md) - Java structure details
- Samples: `pages/app/exmodule/`, `src/com/example/app/service/exmodule/`
