# AI Prompt Guide (for Debugging and Fixes)

This guide standardizes the instruction methods for debugging and requesting fixes for AI-generated code.

---

## Table of Contents

- [1. Basic Debugging Flow](#1-basic-debugging-flow)
- [2. Requesting Compile Error Fixes](#2-requesting-compile-error-fixes)
- [3. Verifying Functionality](#3-verifying-functionality)
- [4. Requesting Bug Fixes](#4-requesting-bug-fixes)
- [5. Common Errors and Solutions](#5-common-errors-and-solutions)

---

## 1. Basic Debugging Flow

```
1. Request compile error fixes
   ↓
2. Verify functionality (start server, operate screens)
   ↓
3. Request bug fixes (provide error logs, expected behavior)
   ↓
4. Verify functionality (re-verify after fixes)
   ↓
5. Repeat steps 3-4 as needed
```

---

## 2. Requesting Compile Error Fixes

### 2.1 Basic Request Method

If compile errors occur after code generation, request fixes as follows.

**Prompt Example 1 (Simple)**:
```
Fix the compile errors
```

**Prompt Example 2 (Specifying Package)**:
```
Fix the compile errors in the todo package
```

**Prompt Example 3 (Selecting and Presenting Error Location)**:
```
Fix this error
[Request with the error location selected in the editor]
```

### 2.2 When AI Cannot Detect Errors

If the AI cannot recognize errors detected by the IDE, select the error location and present it directly.

**Steps**:
1. Select the line where the error occurs in the editor
2. Request with the selection active
3. If there is an error message, present it as well

**Prompt Examples**:
```
PropertiesUtil.getString(
[With error location selected]
```

```
io.putError(
[With error location selected]
```

### 2.3 Common Causes of Compile Errors

| Error Type | Cause | Solution |
|-----------|------|----------|
| Method not found | API misuse | Verify correct API specification and request fix |
| Class not found | Missing import | Request adding import statement |
| Type mismatch | Return value or argument type difference | Request conversion to correct type |
| Value overwrite | Overwriting existing key in Io class | Remove overwrite code or use force overwrite |

---

## 3. Verifying Functionality

### 3.1 Starting the Server

After resolving compile errors, start the server to verify functionality.

**Startup Steps**:
1. Run `StandaloneServerStarter` from the debug configuration
2. Wait until the startup completion message appears in the console
3. Access the target screen in the browser

### 3.2 Accessing Screens

**URL Format**:
```
http://localhost:{port}/pages/app/{module_name}/listpage.html
http://localhost:{port}/pages/app/{module_name}/editpage.html
Refer to 'config\web.properties' for the port
```

**Verification Items**:
- [ ] Does the screen display correctly?
- [ ] Does initialization processing work correctly?
- [ ] Do search, insert, update, and delete work correctly?
- [ ] Does validation work correctly?
- [ ] Are error messages displayed correctly?

### 3.3 Collecting Information When Errors Occur

If errors occur during verification, collect the following information.

**Information to Collect**:
1. **Console error log**: Copy the entire stack trace
2. **Browser developer tools**: Errors in Network tab and Console tab
3. **Operation steps**: Steps taken until the error occurred
4. **Expected behavior**: How it should work

---

## 4. Requesting Bug Fixes

### 4.1 Providing Error Logs

When runtime errors occur, provide the error log and request fixes.

**Prompt Example**:
```
Fix this error
java.lang.RuntimeException: Key already exists. key="todo_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.put(AbstractIoTypeMap.java:689)
 at com.example.app.service.todo.TodoLoad.doExecute(TodoLoad.java:22)
```

**Key Points**:
- Include the first few lines of the stack trace (error type and location)
- If the entire error message is long, extract only the relevant parts

### 4.2 Providing Expected and Actual Behavior

When there is no error log or when behavior differs from expectations, provide expected and actual behavior.

**Prompt Examples**:
```
When I press the save button, "Saved successfully" should display, but nothing appears.
Fix this.
```

```
When searching on the list screen, all records are displayed regardless of search conditions.
Fix it so that it searches by partial match on the title.
```

### 4.3 Bug Fix Request Template

**Template**:
```markdown
## Bug Report

### Location
{Screen name / Feature name}

### Steps to Reproduce
1. {Step 1}
2. {Step 2}
3. {Step 3}

### Expected Behavior
{How it should work}

### Actual Behavior
{How it actually worked}

### Error Log (if any)
```
{Error log}
```

### Fix Request
Fix the above bug.
```

---

## 5. Common Errors and Solutions

### 5.1 Key already exists (Key Duplication Error)

**Symptoms**:
```
java.lang.RuntimeException: Key already exists. key="todo_id"
 at com.onepg.util.AbstractIoTypeMap.validateKeyForPut(AbstractIoTypeMap.java:173)
 at com.onepg.util.AbstractIoTypeMap.putVal(AbstractIoTypeMap.java:206)
 at com.onepg.util.AbstractIoTypeMap.put(AbstractIoTypeMap.java:689)
 at com.example.app.service.todo.TodoLoad.doExecute(TodoLoad.java:22)
```

**Cause**:
Attempting to set a value with the same key twice in the `Io` class.
Since `Io.put()` prohibits overwriting the same key, calling `put()` on an existing key causes an error.

**Common Occurrence Patterns**:
- A key sent from the screen (e.g., `todo_id`) is being set again on the server side with `io.put("todo_id", ...)`
- Setting values to the same key multiple times within a loop

**Solutions**:

1. **Remove unnecessary `put()` calls**:
   Values sent from the screen are already stored in `Io`, so there is no need to set them again.
   ```java
   // NG: Re-setting a key already sent from the screen
   io.put("todo_id", data.getString("todo_id"));
   
   // OK: Remove the unnecessary put()
   // (The todo_id sent from the screen is used as-is)
   ```

2. **Use `putForce()` when force overwrite is needed**:
   ```java
   // To force overwrite
   io.putForce("todo_id", newValue);
   ```

**Prompt Example**:
```
A Key already exists error is occurring.
Remove the code that re-sets keys already sent from the screen.

java.lang.RuntimeException: Key already exists. key="todo_id"
 at com.example.app.service.todo.TodoLoad.doExecute(TodoLoad.java:22)
```


### 5.2 JavaScript Runtime Errors

**Symptoms**:
An error is displayed in the browser console.

**Solution**:
Copy the error message from the browser developer tools and present it.

**Prompt Example**:
```
The following error is displayed in the browser console. Fix it.

Uncaught TypeError: Cannot read properties of undefined (reading 'value')
    at editpage.js:45
```

---

## 6. Tips for Efficient Debugging

### 6.1 Verify Step by Step

1. **Compile errors**: First verify that compilation succeeds
2. **Screen display**: Verify that the screen displays
3. **Initialization**: Verify that initialization processing works
4. **Each feature**: Verify search, insert, update, and delete in order

### 6.2 Provide Specific Error Information

- Present stack traces without abbreviation
- Describe operation steps specifically
- Clearly distinguish between expected and actual behavior

### 6.3 Always Re-verify After Fixes

Always re-verify code fixed by AI.
Since fixes may cause other issues, also verify related features.
