# Java Coding Rules

## Overview
- Defines the Java coding rules.
- Adherence to these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Follow the [Web Service Structure Standards](../02-develop-standards/11-web-service-structure.md).
- Follow the [Batch Processing Structure Standards](../02-develop-standards/12-batch-processing-structure.md).

## Variable/Class Names
- Variable names MUST use lowerCamelCase.
    - [Example] `final String lowerCamelCase = map.getString("lower_snake_case")`
- Variable names (constant names) declared with `static final` MUST use UPPER_SNAKE_CASE.
    - [Example] `static final String UPPER_SNAKE_CASE = "SNAKE"`
- Class names MUST use UpperCamelCase.
    - [Example] `public class UpperCamelCase {`
- Method names, variable names, and constant names starting with an underscore `_` are reserved for use by this framework and MUST NOT be used in applications.

## Declarations/Scope
- As a general rule, add the `final` modifier to variables and do not reuse them. The following cases are exceptions:
    - Incrementing array indexes and similar.
    - Incrementally appending or replacing strings.
    - Member variables of Bean classes.
    - Cases where performance degrades significantly.
- Local variables (variables inside methods) MUST be declared as close as possible to their point of use, with the smallest possible scope.
- As a general rule, member variables (variables outside methods, excluding constants) MUST NOT be used. Member variables of Bean classes are exceptions.
- Variables with the `static` modifier MUST also have the `final` modifier.
- Methods and constants of module classes MUST be created with `private` scope or `package` scope (no scope specification), and MUST NOT be used from other modules. Abstract methods of abstract classes in this framework are exceptions.
- Module classes MUST NOT use class inheritance. Abstract classes in this framework are exceptions.
- Create common components for modules as utility classes (hereinafter referred to as cross-module utility classes).
- All methods and constants of cross-module utility classes MUST have the `static` modifier added, and MUST be used without instantiation. The constructor MUST have `private` scope.
- DO NOT pass floating-point number literals to the `BigDecimal` constructor.
    - [NG Example] `new BigDecimal(0.1)`
    - [OK Example] `new BigDecimal("0.1")`

## Access Methods
- When accessing methods or member variables of a superclass, add `super.`.
- When accessing member variables (variables outside methods, excluding constants), add `this.`. As a general rule, member variables MUST NOT be used, and their usage locations (exception locations) MUST be made explicit.

## Comparison/Evaluation
- DO NOT compare `boolean` variables with `true`/`false`.
    - [NG Example] `if (hasError == true)`
    - [OK Example] `if (hasError)`
- When determining whether a variable value is blank (including `null`), use the components of this framework. The same applies when determining whether an array, list, or map contains zero elements.
    - [NG Example] `if (val == null || val.trim().length == 0)`
    - [OK Example 1] `if (ValUtil.isBlank(val))`
    - [OK Example 2] `if (ValUtil.isEmpty(ary))`
- When determining whether a string constant and a string variable have the same value, use the constant as the subject of comparison.
    - [NG Example] `if (value.equals(CONST))`
    - [OK Example] `if (CONST.equals(value))`
- When determining whether two string variables (not constants) have the same value, use the components of this framework.
    - [Example] `if (ValUtil.equals(val1, val2))`

## Processing Methods
- Use `java.util.ArrayList` for list-type classes.
- Use `com.onepg.util.IoItem` from this framework for map-type classes. Use `java.util.HashMap` when handling values (classes) that cannot be stored in `com.onepg.util.IoItem`.
- Normally, use the enhanced `for` loop for iteration.
    - [Example 1] `for (final String val : list)`
    - [Example 2] `for (final Map.Entry<String, String> ent : map.entrySet()))`
- When an index value is needed, use the `length` attribute in the loop.
    - [Example] `for (int i = 0; i < list.length; i++)`
- When concatenating strings multiple times, use the `java.lang.StringBuilder` class.
- Classes implementing `java.lang.AutoCloseable` or `java.io.Closeable` MUST be declared in a `try` clause (try-with-resources statement). Calling the `close` method is not required.
    ```java
    try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
      for (final String line : tr) {
        ：
      }
    }
    ```
- The use of `System.out` is prohibited.
- Prioritizing readability of diffs during modifications above all else, the use of ternary operators, lambda expressions `->`, method references `::`, and method chains of the Stream API is prohibited.
- When the same literal value with the same meaning is used in multiple locations, declare it as a constant. If only used in one location, a literal value is acceptable.

## Unexpected Cases
- When the following unexpected cases occur, generate and throw a `java.lang.RuntimeException` to halt processing.
    - When an unexpected argument is passed.
    - When an unexpected DB value is retrieved.
    - When DB data that must always exist does not exist.
    - When DB data that should exist as a single record has multiple records.
- In module processing, subclasses of `java.lang.Exception` (hereinafter referred to as error classes) MUST NOT be caught, and MUST be thrown to the caller.
- In module processing, the only error class to generate and throw is `java.lang.RuntimeException`.
- DO NOT add a `throws` clause to `public` methods of cross-module utility classes. If an error that requires a `throws` clause occurs, catch it and instead generate (passing the caught error to the constructor) and throw a `java.lang.RuntimeException`.
- The logic for throwing errors on unexpected arguments or DB values is as follows:
    - The `else` clause of an expected `if`/`else if` (the same applies to the `default` clause of a `switch` statement).
    - An `if` statement that checks for an unexpected condition.
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

- By using the following DB data retrieval methods of `com.onepg.util.SqlUtil` in this framework, errors are thrown for unexpected cases.
    - `SqlUtil#selectOneExists`: An error occurs when zero records or multiple records are retrieved.
    - `SqlUtil#selectOne`: An error occurs when multiple records are retrieved.
    - `SqlUtil#updateOne`: An error occurs when the number of updated records is more than one.
    - `SqlUtil#deleteOne`: An error occurs when the number of deleted records is more than one.
    - `SqlUtil#executeOne`: An error occurs when the number of applied records is more than one.

## Style
- Use 2 half-width spaces for indentation inside methods and control structures (`if`, `for`, etc.); do not use tab characters.
- Do not omit braces `{ }` for control structures (`if`, `for`, etc.); add a line break immediately after `{` and immediately before `}`.
- Add 1 half-width space at the following locations. However, do not add spaces at the end of a line or immediately before a semicolon at the end of a line.
    - Between a reserved word (`if`, `return`, etc.) and the string that follows it.
    - Between a reserved word (`else`, `catch`, etc.) and the string that precedes it.
    - Between the opening brace `{` and the string that precedes it.
    - Before and after all operators (`+`, `=`, `!==`, `&&`, etc.). Increment and decrement operators are excluded.
    - Between a comma, colon, or semicolon and the string that follows it.
- The target for one line is 120 characters excluding indentation; wrap if it exceeds this. Do not add unnecessary line breaks if under this limit.
- Actively use text blocks (Text Block `"""..."""`) for strings spanning multiple lines. ***Only when using JDK 15 or later***
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
- Do not comment out logic; delete it. If there are notes or warnings, leave only the notes as comments.
- Do not include change history comments. If there are notes or warnings, leave only the notes as comments.
- Add JavaDoc to all declarations except variables inside methods. This does not mean that variables inside methods do not need descriptions; write descriptions as regular comments.
- Descriptions of methods, variables, and constants declared with `private` scope may be simplified.
- JavaDoc comments MUST be enclosed in `/** */`.
- Regular comments (not for JavaDoc) MUST be written on a single line starting with `//` at the beginning of the line. End-of-line comments are prohibited.

## JavaDoc
- Create a `package-info.java` for each module package and include the module name.
- The format for method declarations is as follows:
    - Write the class name or method name on the first line. Append `.<br>` after the name.
    - Express the class name or method name concisely in one phrase that captures its role or responsibility; do not write the actual Java class name or method name in English.
    - From the second line onward, describe the role, responsibility, and processing description as a bulleted list using `<ul>` or `<ol>`.
    - Describe method arguments using the `@param` tag.
        - Write the `@param` tag in the format: "@param variable_name description".
    - Describe return values using the `@return` tag.
        - Write the `@return` tag in the format: "@return description".
        - If `null` may be returned as a return value, append "(nullable)" at the end of the description.
- Add `{@inheritDoc}` to the implementation of abstract methods. A JavaDoc with only `{@inheritDoc}` is acceptable.
    ```java
    /**
     * {@inheritDoc}
     */
    @Override
    ```
- When referring to the JavaDoc of another Java class or Java method, use the `@see` tag.
- Enclose sample code within text using `<pre><code>`. For short single-line code, only `<code>` is sufficient.
- Also enclose the following names in `<code>`:
    - DB table names
    - Column names
    - Java class names
    - Java method names
- For Java method names in other Java classes, write in the format: `<code>JavaClassName#methodName</code>`.
- When writing a DB table name and column name together, write in the format: `<code>DBTableName.columnName</code>`.
