# Java Coding Rules

## Overview
- Defines coding rules for Java.
- By following these rules, the code is standardized and development/maintenance efficiency is improved.

## Prerequisites
- Follow the [Web Service Structure Standard](../02-develop-standards/11-web-service-structure.md).
- Follow the [Batch Processing Structure Standard](../02-develop-standards/12-batch-processing-structure.md).

## Variable/Class Names
- Use lowerCamelCase for variable names.
    - [Example] `final String lowerCamelCase = map.getString("lower_snake_case")`
- Use UPPER_SNAKE_CASE for static final variable names (i.e., constant names).
    - [Example] `static final String UPPER_SNAKE_CASE = "SNAKE"`
- Use UpperCamelCase for class names.
    - [Example] `public class UowerCamelCase {`
- DO NOT use method names, variable names, or constant names that begin with an underscore `_`, as they are reserved for use by this framework.

## Declarations/Scope
- In principle, add the `final` modifier to variables and do not reuse them. The following use cases are exceptions:
    - Incrementing array indices.
    - Incremental string additions or replacements.
    - Member variables of Bean classes.
    - Cases where performance degrades significantly.
- Declare local variables (variables within methods) immediately before use, in locations where the scope is as small as possible.
- In principle, do not use member variables (variables outside methods, excluding constants). Member variables of Bean classes are exceptions.
- Always add the `final` modifier to variables with the `static` modifier.
- Create methods and constants of module classes with `private` scope or `package` scope (no scope specified) and do not use them from other modules. Abstract methods of framework abstract classes are exceptions.
- Do not perform class inheritance for module classes. Framework abstract classes are exceptions.
- Create common components for modules as utility classes.
- Add the `static` modifier to all methods and constants in utility classes and use them without instantiation. Make constructors `private` scope.
- Do not pass floating-point number literals to the constructor of `BigDecimal`.
    - [NG Example] `new BigDecimal(0.1)`
    - [OK Example] `new BigDecimal("0.1")`

## Access Methods
- Add `super.` when accessing methods or member variables possessed by the superclass.
- Add `this.` when accessing member variables (variables outside methods, excluding constants). In principle, member variables should not be used, so clearly indicate the locations where they are used (exceptional cases).

## Comparison/Evaluation
- Do not compare `boolean` variables with `true`/`false`.
    - [NG Example] `if (hasError == true)`
    - [OK Example] `if (hasError)`
- When determining whether a variable's value is blank (including `null`), use framework components. The same applies when determining whether arrays, lists, or maps contain zero items.
    - [NG Example] `if (val == null || val.trim().length == 0)`
    - [OK Example 1] `if (ValUtil.isBlank(val))`
    - [OK Example 2] `if (ValUtil.isEmpty(ary))`
- When determining whether a string constant and a string variable are equal, use the constant as the comparison source.
    - [NG Example] `if (value.equals(CONST))`
    - [OK Example] `if (CONST.equals(value))`
- When determining whether string variables (not constants) are equal, use framework components.
    - [Example] `if (ValUtil.equals(val1, val2))`

## Processing Methods
- Use `java.util.ArrayList` for list type classes.
- Use `com.onepg.util.IoItem` from this framework for map type classes. Use `java.util.HashMap` when handling values (classes) that cannot be stored in `com.onepg.util.IoItem`.
- Normally, use the enhanced `for` style for loop processing.
    - [Example 1] `for (final String val : list)`
    - [Example 2] `for (final Map.Entry<String, String> ent : map.entrySet()))`
- Use the style with the `length` attribute for loop processing when index values are needed.
    - [Example] `for (int i = 0; i < list.length; i++)`
- Use the `java.lang.StringBuilder` class when concatenating strings multiple times.
- Declare classes that inherit `java.lang.AutoCloseable` or `java.io.Closeable` in a `try` clause (try-with-resources statement). Calling the `close` method is not necessary.
    ```java
    try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
      for (final String line : tr) {
        :
      }
    }
    ```
- DO NOT use `System.out`.
- DO NOT catch subclasses of `java.lang.Exception` (hereinafter referred to as error classes) in module processing.
- Prioritize diff readability during modifications; DO NOT use ternary operators, lambda expressions `->`, method references `::`, or StreamAPI method chaining.
- Declare constants when using the same literal value with the same meaning in multiple locations. Literal values may be used if only in one location.

## Unexpected Cases
- When the following unexpected cases occur, generate and throw a `java.lang.RuntimeException` error to halt processing.
    - When unexpected parameters are passed.
    - When unexpected database values are retrieved.
    - When database data that should exist does not exist.
    - When multiple database records exist where only one should exist.
- Do not use error classes other than `java.lang.RuntimeException` in module processing.
- Write logic to throw errors for unexpected parameters or database values as follows:
    - In the `else` clause (similarly in the `default` clause of `case` statements) of `if` and `else if` statements where cases are expected.
    - In `if` statements that check for unexpected cases
    ```java
    [Example 1]
    if (ValUtil.equals(kbn, "1")) {
      // Expected case
    } else if (ValUtil.equals(kbn, "2")) {
      // Expected case
    } else {
      throw new RuntimeException("Unexpected category. " + LogUtil.joinKeyVal("Category value", kbn));
    }

    [Example 2]
    if (!ValUtil.isDate(beforDate)) {
      // Unexpected case
      throw new RuntimeException("Invalid previous date. " + LogUtil.joinKeyVal("Date", beforDate));
    }
    ```

- Throw errors for unexpected cases by appropriately using the following database data retrieval methods of `com.onepg.util.SqlUtil` from this framework.
    - `SqlUtil#selectOneExists`: Throws an error if zero or multiple records are retrieved.
    - `SqlUtil#selectOne`: Throws an error if multiple records are retrieved.
    - `SqlUtil#updateOne`: Throws an error if the number of updated records is multiple.
    - `SqlUtil#deleteOne`: Throws an error if the number of deleted records is multiple.
    - `SqlUtil#executeOne`: Throws an error if the number of affected records is multiple.

## Style
- Use 2 spaces for indentation within methods and control structures (`if`, `for`, etc.). Do not use tab characters.
- Do not omit braces `{ }` for control structures (`if`, `for`, etc.); insert a line break immediately after `{` and immediately before `}`.
- Insert one space in the following locations. However, do not insert spaces at the end of lines or immediately before semicolons at the end of lines.
    - Between reserved words (`if`, `return`, etc.) and the following string.
    - Between reserved words (`else`, `catch`, etc.) and the preceding string.
    - Between the opening brace `{` and the preceding string.
    - Before and after all operators (`+`, `=`, `!==`, `&&`, etc.). However, exclude increment and decrement operators.
    - Between commas, colons, semicolons and the following string.
- Aim for approximately 100 columns per line excluding indentation, and wrap lines if they exceed this. Do not wrap lines unnecessarily if they are shorter.
- Actively use text blocks (Text Block """...""") for multi-line strings. ***JDK15 or later only***
    ```java
    sb.addQuery("SELECT ");
    sb.addQuery("  u.user_nm ");
    sb.addQuery(", u.email ");
    sb.addQuery(" FROM t_user u ");
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    ↓
    sb.addQuery("""
      SELECT
        u.user_nm
      , u.email
      FROM t_user u
    """);
    sb.addQuery(" WHERE u.user_id = ? ", io.getString("user_id"));
    ```

## Comments
- Delete logic instead of commenting it out. If there are important notes, leave only the notes as comments.
- Do not write change history comments. If there are important notes, leave only the notes as comments.
- Write JavaDoc for all declarations except variables within methods. This does not mean that variables within methods do not need explanations; write explanations as regular comments.
- Descriptions of methods, variables, and constants declared with `private` scope may be simplified.
- Enclose JavaDoc comments in `/** */`.
- Write regular comments (non-JavaDoc) using the format of adding `//` at the beginning of a line, using one line. DO NOT write comments at the end of lines.

## JavaDoc
- Create `package-info.java` for each module package and write the module name.
- Format method declarations as follows:
    - Write the class name or method name on the first line. Add `.<br>` after the name.
    - The class name or method name should concisely express its role or responsibility in one phrase; do not write the actual English class/method name itself.
    - From the second line onward, describe the role, responsibility, or processing using bullet points with `<ul>` or `<ol>`.
    - Describe method parameters using the `@param` tag.
        - Write the `@param` tag in the format "@param variableName description".
    - Describe method return values using the `@return` tag.
        - Write the `@return` tag in the format "@return description".
        - If `null` may be returned as a return value, write "(null possible)" at the end of the "description".
- Add `{@inheritDoc}` to implementations of abstract methods. JavaDoc with only `{@inheritDoc}` is acceptable.
    ```java
    /**
     * {@inheritDoc}
     */
    @Override
    ```
- When referencing JavaDoc of other Java classes or methods, write using the `@see` tag.
- Enclose sample code in text in `<pre><code>`. For short one-line code, enclose only in `<code>`.
- Also enclose the following names in `<code>`:
    - Database table names
    - Field names
    - Java class names
    - Java method names
- Write Java method names of other Java classes in the format `<code>JavaClassName#methodName</code>`.
- When writing database table names and field names together, write in the format `<code>databaseTableName.fieldName</code>`.
