# AI Prompt Guide (Business Screen Creation)

This document is a guide that standardizes the prompt methods for instructing AI to create business screens.

---

## Table of Contents

- [1. Basic Instruction Flow](#1-basic-instruction-flow)
- [2. Required Input Information](#2-required-input-information)
- [3. Prompt Templates](#3-prompt-templates)
- [4. DB Field → HTML Element Conversion Rules](#4-db-field--html-element-conversion-rules)
- [5. How to Define Code Values](#5-how-to-define-code-values)
- [6. List of Generated Files](#6-list-of-generated-files)
- [7. Example: User Management Screen](#7-example-user-management-screen)
- [8. Error Message Definitions](#8-error-message-definitions)
- [9. References](#9-references)

---

## 1. Basic Instruction Flow

```
1. Present table definition
2. Specify screen type (list + edit / list only / edit only)
3. Define code values
4. Specify validation requirements
5. Request generation
6. Request compilation error fixes
7. Verify functionality
8. Request bug fixes
```

> **Note**: For details on steps 6-8, refer to [AI Prompt Guide (Debugging and Fixing)](02-ai-debug-guide.md).

---

## 2. Required Input Information

### 2.1 Mandatory Information

| Item | Description | Example |
|------|------|-----|
| Function Name | Function name of the screen | User Management |
| Module Name | Directory/package name (lowercase English letters) | usermst |
| Table Definition | CREATE TABLE format | See below |
| Screen Type | List + edit / List only / Edit only | List + edit |

### 2.2 Optional Information

| Item | Description | Example |
|------|------|-----|
| Code Value Definition | Option values and display names | Gender: M=Male, F=Female |
| Search Conditions | Search condition fields for list page | User ID (prefix match), Name (partial match) |
| Validation | Required/length/format | User ID: Required, 4 digits, alphanumeric |
| Initial Value | Values to display initially | Today's date in birth date |

---

## 3. Prompt Templates

### 3.1 List + Edit Screen (Standard Pattern)

```markdown
## Business Screen Creation Request

### Function Name
{Function Name}

### Module Name
{Module Name}

### Table Definition
```sql
create table t_{Table Name} (\n  {Column Definition}
  ,primary key ({Primary Key})
);
```

### Code Value Definition
- {Column Name}_cs: {Code}={Display Name}, {Code}={Display Name}

### Screen Type
List + Edit

### Search Conditions (List Page)
- {Field Name}: {Search Method}

### Validation (Edit Page)
- {Field Name}: {Check Content}

### Generation Request
Generate the following files with the above requirements:
- pages/app/{module name}/listpage.html
- pages/app/{module name}/listpage.js
- pages/app/{module name}/editpage.html
- pages/app/{module name}/editpage.js
- src/com/example/app/service/{module name}/{Module}ListInit.java
- src/com/example/app/service/{module name}/{Module}ListSearch.java
- src/com/example/app/service/{module name}/{Module}Load.java
- src/com/example/app/service/{module name}/{Module}Upsert.java
- src/com/example/app/service/{module name}/{Module}Delete.java

Follow the implementation patterns in docs/02-develop-standards/21-event-coding-pattern.md.
```

### 3.2 List Only (View Page)

```markdown
## Business Screen Creation Request

### Function Name
{Function Name}

### Module Name
{Module Name}

### Table Definition
{CREATE TABLE}

### Screen Type
List only (read-only)

### Generation Request
- pages/app/{module name}/listpage.html
- pages/app/{module name}/listpage.js
- src/com/example/app/service/{module name}/{Module}ListInit.java
- src/com/example/app/service/{module name}/{Module}ListSearch.java
```

### 3.3 Header + Detail Screen

```markdown
## Business Screen Creation Request

### Function Name
{Function Name}

### Module Name
{Module Name}

### Header Table
```sql
create table t_{Header} (
  {Column Definition}
  ,primary key ({Primary Key})
);
```

### Detail Table
```sql
create table t_{Detail} (
  {Column Definition}
  ,primary key ({Header Primary Key}, {Detail Sequence Number})
);
```

### Screen Type
List + Edit (header-detail structure)

### Generation Request
Enable editing of the detail table together with the header on the edit page.
```

---

## 4. DB Field → HTML Element Conversion Rules

### 4.1 Automatic Determination by Suffix

| Suffix | Meaning | HTML Element | Example |
|-------------|------|----------|-----|
| `_id` | ID/Code | `<input type="text">` | user_id |
| `_nm` | Name | `<input type="text">` | user_nm |
| `_cs` | Code classification | `<select>` or `<radio>` | gender_cs |
| `_dt` | Date | `<input>` + `data-value-format-type="ymd"` | birth_dt |
| `_ts` | Timestamp | `<input type="hidden">` | upd_ts |
| `_am` | Amount | `<input>` + `data-value-format-type="num"` | income_am |
| `_kg`, `_cm` | Quantity | `<input>` + `data-value-format-type="num"` | weight_kg |
| `_no` | Number | `<input type="text">` or display only | pet_no |

### 4.2 Determination by Type

| DB Type | HTML Element |
|------|----------|
| VARCHAR(1) + `_cs` | `<radio>` or `<checkbox>` |
| VARCHAR(2~) + `_cs` | `<select>` |
| NUMERIC | `<input type="text" class="align-right">` |
| DATE | `<input>` + `data-value-format-type="ymd"` |
| TIMESTAMP | `<input type="hidden">` (for optimistic locking) |

### 4.3 HTML Attribute Usage

| Purpose | Attribute | Example |
|------|------|-----|
| Input & Retrieve | `name` | `<input name="user_id">` |
| Display Only | `data-name` | `<td data-name="user_nm">` |
| Numeric Comma | `data-value-format-type="num"` | `1000000` → `1,000,000` |
| Date Slash | `data-value-format-type="ymd"` | `20251231` → `2025/12/31` |
| Checkbox OFF Value | `data-check-off-value` | `<input type="checkbox" data-check-off-value="N">` |

---

## 5. How to Define Code Values

### 5.1 Standard Format

```markdown
### Code Value Definition
- gender_cs (Gender): M=Male, F=Female
- country_cs (Country): JP=Japan, US=United States, BR=Brazil, AU=Australia
- type_cs (Type): DG=Dog, CT=Cat, BD=Bird
- spouse_cs (Spouse): Y=Yes, N=No ※Checkbox
- vaccine_cs (Vaccinated): Y=Vaccinated, N=Not Vaccinated ※Checkbox
```

### 5.2 HTML Generation Examples

**For select**:
```html
<select name="country_cs">
  <option value="">Not Selected</option>
  <option value="JP">Japan</option>
  <option value="US">United States</option>
</select>
```

**For radio**:
```html
<label><input type="radio" name="gender_cs" value="">Not Selected</label>
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
├── listpage.html      # List page HTML
├── listpage.js        # List page JavaScript
├── editpage.html      # Edit page HTML
└── editpage.js        # Edit page JavaScript

src/com/example/app/service/{module}/
├── {Module}ListInit.java    # List initialization
├── {Module}ListSearch.java  # List search processing
├── {Module}Load.java        # Data retrieval
├── {Module}Upsert.java      # Registration/update processing
└── {Module}Delete.java      # Deletion processing
```

### 6.2 File Naming Rules

| File Type | Naming Rule | Example |
|-------------|---------|-----|
| List HTML | listpage.html | listpage.html |
| Edit HTML | editpage.html | editpage.html |
| List JS | listpage.js | listpage.js |
| Edit JS | editpage.js | editpage.js |
| Java List Init | {Module}ListInit.java | UserListInit.java |
| Java List Search | {Module}ListSearch.java | UserListSearch.java |
| Java Data Load | {Module}Load.java | UserLoad.java |
| Java Upsert | {Module}Upsert.java | UserUpsert.java |
| Java Delete | {Module}Delete.java | UserDelete.java |

---

## 7. Example: User Management Screen

### 7.1 Complete Prompt Example

```markdown
## Business Screen Creation Request

### Function Name
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

### Code Value Definition
- country_cs (Country of Origin): JP=Japan, US=United States, BR=Brazil, AU=Australia
- gender_cs (Gender): M=Male, F=Female
- spouse_cs (Spouse): Y=Yes, N=No ※Checkbox
- type_cs (Pet Type): DG=Dog, CT=Cat, BD=Bird
- vaccine_cs (Vaccination): Y=Vaccinated, N=Not Vaccinated ※Checkbox

### Screen Type
List + Edit (header-detail structure)

### Search Conditions (List Page)
- user_id: Prefix match
- user_nm: Partial match
- email: Partial match
- country_cs: Exact match (dropdown)
- gender_cs: Exact match (radio)
- spouse_cs: Condition only when checked
- income_am: Greater than or equal to
- birth_dt: On or after

### Validation (Edit Page)
Header:
- user_id: Required, 4 digits fixed, alphanumeric
- user_nm: Required, within 20 digits
- email: Within 50 digits
- income_am: Numeric, within 10 digits
- birth_dt: Date format

Detail:
- pet_nm: Required, within 10 digits
- weight_kg: Numeric (1 decimal place)

### Initial Value
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
|----|-----------|------|
| ev001 | {0} is required. | Required field check |
| ev011 | {0} must be alphanumeric. | Format check |
| ev012 | {0} must be numeric. | Numeric check |
| ev013 | {0} must be in date format. | Date check |
| ev021 | {0} must be within {1} digits. | Length check |
| ev022 | {0} must be exactly {1} digits. | Fixed length check |

**Business Errors (e****)**:
| ID | Message | Usage |
|----|-----------|------|
| e0001 | {0} is already registered. | Duplicate error |
| e0002 | {0} was updated by another user. | Optimistic locking error |

**Success Messages (i****)**:
| ID | Message | Usage |
|----|-----------|------|
| i0001 | {0} has been registered. | New registration success |
| i0002 | {0} has been updated. | Update success |
| i0003 | {0} has been deleted. | Deletion success |

---

## 9. References

- [Prompt Generation Template](11-ai-prompt-generator-template.md) - Template for generating prompts from table definitions
- [Event-Based Coding Patterns](../02-develop-standards/21-event-coding-pattern.md) - Implementation pattern details
- [Web Page Structure Standards](../02-develop-standards/01-web-page-structure.md) - HTML/JS structure details
- [Web Service Structure Standards](../02-develop-standards/11-web-service-structure.md) - Java structure details
- Samples: `pages/app/exmodule/`, `src/com/example/app/service/exmodule/`
