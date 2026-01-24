# AI Instruction Guide (Debugging and Fixing)

This document explains how to instruct AI when requesting debugging and fixing of AI-generated code.

---

## Table of Contents

- [1. Basic Debugging Flow](#1-basic-debugging-flow)
- [2. Requesting Compilation Error Fixes](#2-requesting-compilation-error-fixes)
- [3. Operation Verification](#3-operation-verification)
- [4. Requesting Bug Fixes](#4-requesting-bug-fixes)
- [5. Common Errors and Solutions](#5-common-errors-and-solutions)

---

## 1. Basic Debugging Flow

```
1. Request compilation error fixes
   ↓
2. Verify operation (server startup and screen operation)
   ↓
3. Request bug fixes (provide error logs and expected behavior)
   ↓
4. Verify operation (reconfirm after fixes)
   ↓
5. Repeat steps 3-4 as needed
```

---

## 2. Requesting Compilation Error Fixes

### 2.1 Basic Request Methods

When compilation errors occur after code generation, request fixes as follows:

**Prompt Example 1 (Simple)**:
```
[With the error file open]
Fix the compilation errors
```

**Prompt Example 2 (Specify Package)**:
```
Fix compilation errors under the com.example.app.service.ordermng package
```

**Prompt Example 3 (Select Error Location)**:
```
[With the error location selected in the editor]
Fix this compilation error
```

**Prompt Example 4 (Paste Error Location)**:
```
[With the error file open]
Fix the following compilation error

PropertiesUtil.getString
```

### 2.2 Common Causes of Compilation Errors

| Error Type | Cause | Solution |
|-----------|------|--------|
| Method not found | Incorrect API usage | Verify correct API specification and request fix |
| Class not found | Missing import | Request import statement addition |
| Type mismatch | Incorrect return value or argument type | Request conversion to correct type |
| Value overwrite | Overwriting existing key in Io class | Remove overwrite code or use force overwrite |

---

## 3. Operation Verification

### 3.1 Server Startup

After resolving compilation errors, start the server to verify operation.

**Startup Procedure**:
1. Execute `src/com/onepg/web/StandaloneServerStarter.java`
2. Wait until the startup completion message appears in the console
3. Access the target screen in the browser

**Shutdown Procedure**:
- Execute `src/com/onepg/web/StandaloneServerStopper.java`
- Server restart is required when Java classes are modified.

### 3.2 Screen Access

**URL Format**:
```
http://localhost:{port}/pages/app/{module-name}/listpage.html
http://localhost:{port}/pages/app/{module-name}/editpage.html
Refer to 'config\web.properties' for port
```

**Verification Items**:
- [ ] No errors are output to the VS Code console
- [ ] The screen displays correctly
- [ ] Initial display processing operates correctly
- [ ] Search, registration, update, and deletion operate correctly
- [ ] Validation operates correctly
- [ ] Error messages display correctly

### 3.3 Information Gathering When Errors Occur

When errors occur during operation verification, gather the following information:

**Information to Gather**:
1. **Console error log**: Copy the entire stack trace
2. **Browser developer tools**: Errors in Network tab and Console tab
3. **Operation steps**: Steps leading to the error occurrence
4. **Expected behavior**: How it should operate normally

---

## 4. Requesting Bug Fixes

### 4.1 When Providing Error Logs

When runtime errors occur, provide the error log and request fixes.

**Prompt Example**:
```
Fix the error
java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```

**Key Points**:
- Include the first few lines of the stack trace (error type and occurrence location)
- Extract only relevant parts when the entire error message is too long

### 4.2 When Providing Expected vs Actual Behavior

When there is no error log or when behavior differs from expectations, provide the expected behavior and actual behavior.

**Prompt Example**:
```
When clicking the save button, "Saved" should be displayed, but nothing is shown.
Fix this.
```

```
When searching on the list page, all records are displayed regardless of search conditions.
Fix it to enable partial match search by title.
```

### 4.3 Bug Fix Request Template

When fixing bugs with complex reproduction steps or specifications, use the following template:

**Template**:
```markdown
## Bug Report

### Location
{Screen name / Function name}

### Operation Steps
1. {Step 1}
2. {Step 2}
3. {Step 3}

### Expected Behavior
{How it should operate normally}

### Actual Behavior
{How it actually operated}

### Error Log (if available)
```
{Error log}
```

### Fix Request
Please fix the above bug.
```

---

## 5. Common Errors and Solutions

### 5.1 Key already exists (Duplicate Key Error)

**Symptom**:
```
java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```

**Cause**:
- Attempting to set a value with the same key twice in the `Io` class.
- `Io.put()` prohibits overwriting the same key, so calling `put()` again on an existing key causes an error.
- The same error occurs with `Io.putAll()`.

**Common Pattern**:
- Setting SELECT results that include keys sent from the screen (e.g., `user_id`) using `io.putAll(row)`

**Solution**:

1. **Exclude duplicate keys from the SELECT statement**:
   Values sent from the screen are already stored in `Io`, so there is no need to retrieve them again with SELECT.
   ```java
   // NG: SELECT results include keys already sent from the screen (user_id)
   sb.addQuery("SELECT ");
   sb.addQuery("  u.user_id "); // ← Retrieving user_id
   sb.addQuery(", u.user_nm ");
   sb.addQuery(", u.email ");
   sb.addQuery(" FROM t_user u ");
   sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id")); // ← user_id exists in io
   final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
   io.putAll(row); // ← Error due to duplicate user_id
   
   // OK: Exclude user_id from the SELECT statement
   sb.addQuery("SELECT ");
   // ... (Do not SELECT user_id)
   sb.addQuery("  u.user_nm ");
   sb.addQuery(", u.email ");
   sb.addQuery(" FROM t_user u ");
   sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id")); 
   final IoItems row = SqlUtil.selectOne(getDbConn(), sb);
   io.putAll(row); // ← No duplication
   ```

2. **Use `putAllForce()` when forced overwriting is necessary**:
   ```java
   // When forcing overwrite
   io.putAllForce(row);
   ```

**Prompt Example**:
```
A "Key already exists" error is occurring.
Remove the code that resets keys already sent from the screen.

java.lang.RuntimeException: Key already exists. key="user_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.putAllByMap(AbstractIoTypeMap.java:989)
 at com.onepg.util.AbstractIoTypeMap.putAll(AbstractIoTypeMap.java:961)
 at com.example.app.service.exmodule.ExampleLoad.getHead(ExampleLoad.java:60)
 at com.example.app.service.exmodule.ExampleLoad.doExecute(ExampleLoad.java:23)
```


### 5.2 JavaScript Execution Errors

**Symptom**:
JavaScript errors are displayed in the browser console.

**Solution**:
Copy and provide the error message from the browser developer tools.

**Prompt Example**:
```
The following error is displayed in the browser console. Please fix it.

Uncaught TypeError: Cannot read properties of undefined (reading 'value')
    at editpage.js:45
```

---

## 6. Efficient Debugging Tips

### 6.1 Verify Step by Step

1. **Compilation errors**: First verify that compilation passes
2. **Screen display**: Verify that the screen is displayed
3. **Initialization**: Verify that initial display processing operates
4. **Each function**: Verify search, registration, update, and deletion in sequence

### 6.2 Be Specific with Error Information

- Provide the stack trace without omitting anything
- Describe operation steps specifically
- Clearly distinguish expected behavior from actual behavior

### 6.3 Always Reconfirm After Fixes

You MUST verify operation again after AI fixes the code.
Verify related functions as well, since fixes may introduce other issues.
