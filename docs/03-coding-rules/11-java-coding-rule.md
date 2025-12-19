# Java Coding Rules

## Overview
- Define the coding rules for Java.
- Following these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Follow the [Web Service Structure Standards](../02-develop-standards/11-web-service-structure.md).
<!-- Follow the [Batch Processing Structure Standards](13-batch-processing-structures.md). -->

## Variable Names and Class Names
- Use lowerCamelCase for variable names.
    - [Example] `final String lowerCamelCase = map.getString("lower_snake_case")`
- Use UPPER_SNAKE_CASE for variable names declared with static final (constant names).
    - [Example] `static final String UPPER_SNAKE_CASE = "SNAKE"`
- Use UpperCamelCase for class names.
    - [Example] `public class UpperCamelCase {`
- Method names, variable names, and constant names starting with underscore `_` are reserved for this framework and MUST NOT be used by applications.

## Declarations and Scope
- As a rule, add the `final` modifier to variables and do not reuse them. The following uses are exceptions:
    - Array index increments, etc.
    - Progressive string additions or replacements.
    - Member variables in Bean classes.
    - When performance is significantly degraded.
- Declare local variables (variables within methods) immediately before use, in locations where the scope is as small as possible.
- As a rule, do not use member variables (variables outside methods, excluding constants). Member variables in Bean classes are exceptions.
- Always add the `final` modifier to variables with the `static` modifier.
- Create methods and constants in module classes with `private` scope or `package` scope (no scope specified), and do not use them from other modules. Abstract methods from this framework's abstract classes are exceptions.
- Do not use class inheritance in module classes. This framework's abstract classes are exceptions.
- Create common components for modules as utility classes.
- Add the `static` modifier to all methods and constants in utility classes, and use them without instantiation. Make constructors `private` scope.
- Do not pass floating-point literals to `BigDecimal` constructors.
    - [NG] `new BigDecimal(0.1)`
    - [OK] `new BigDecimal("0.1")`

## Access Method
- Add `super.` when accessing methods or member variables in the superclass.
- Add `this.` when accessing member variables (variables outside methods, excluding constants). As a rule, member variables should not be used; use `this.` to explicitly indicate exception locations.

## Comparison and Evaluation
- Do not compare `boolean` variables with `true`/`false`.
    - [NG] `if (hasError == true)`
    - [OK] `if (hasError)`
- Use this framework's utilities when checking if a variable's value is blank (including `null`). The same applies when checking if arrays, lists, or maps have zero elements.
    - [NG] `if (val == null || val.trim().length == 0)`
    - [OK 1] `if (ValUtil.isBlank(val))`
    - [OK 2] `if (ValUtil.isEmpty(ary))`
- When checking if a string constant and a string variable are equal, use the constant as the comparison source.
    - [NG] `if (value.equals(CONST))`
    - [OK] `if (CONST.equals(value))`
- Use this framework's utilities when checking if two string variables (not constants) are equal.
    - [Example] `if (ValUtil.equals(val1, val2))`

## Processing Method
- Use `java.util.ArrayList` for list type classes.
- Use this framework's `com.onepg.util.IoItem` for map type classes. Use `java.util.HashMap` when handling values (classes) that cannot be stored in `com.onepg.util.IoItem`.
- Normally, use the enhanced `for` statement for loop processing.
    - [Example 1] `for (final String val : list)`
    - [Example 2] `for (final Map.Entry<String, String> ent : map.entrySet()))`
- When index values are needed, use the `length` attribute method for loop processing.
    - [Example] `for (int i = 0; i < list.length; i++)`
- Use `java.lang.StringBuilder` class when concatenating strings multiple times.
- Declare classes that extend `java.lang.AutoCloseable` or `java.io.Closeable` in `try` clauses (try-with-resources statements). Calling the `close` method is not required.
```try clause declaration [Example]
try (final TxtReader tr = new TxtReader(filePath, ValUtil.UTF8);) {
  for (final String line : tr) {
    :
  }
}
```
- Prohibit the use of `System.out`.
- Prohibit catching subclasses of `java.lang.Exception` (hereinafter referred to as error classes) in module processing.
- Prioritize readability of diffs during modifications; prohibit the use of ternary operators, lambda expressions `->`, method references `::`, and Stream API method chaining.
- Declare constants when using the same literal value with the same meaning in multiple locations. Literal values are acceptable if used in only one location.

## Unexpected Cases
- When the following unexpected cases occur, create and throw a `java.lang.RuntimeException` error to abort processing:
    - When unexpected arguments are passed.
    - When unexpected database values are retrieved.
    - When database data that should always exist does not exist.
    - When database data that should exist as only one record exists as multiple records.
- Do not use error classes other than `java.lang.RuntimeException` in module processing.
- The logic for throwing errors on unexpected arguments or database values is as follows:
    - The `else` clause of expected `if`, `else if` statements (same applies to `default` clause in `case` statements).
    - `if` statements that check for unexpected conditions.

```Unexpected case logic
[Example 1]
if (ValUtil.equals(kbn, "1")) {
    // Expected
} else if (ValUtil.equals(kbn, "2")) {
    // Expected
} else {
    throw new RuntimeException("Unexpected category. " + LogUtil.joinKeyVal("category value", kbn));
}

[Example 2]
if (!ValUtil.isDate(beforDate)) {
  // Unexpected
  throw new RuntimeException("Previous date is invalid. " + LogUtil.joinKeyVal("date", beforDate));
}
```

- Use the following database retrieval methods of this framework's `com.onepg.util.SqlUtil` to throw errors in unexpected cases:
    - `SqlUtil#selectOneExists`: Throws an error when zero or multiple records are retrieved.
    - `SqlUtil#selectOne`: Throws an error when multiple records are retrieved.
    - `SqlUtil#updateOne`: Throws an error when multiple records are updated.
    - `SqlUtil#deleteOne`: Throws an error when multiple records are deleted.
    - `SqlUtil#executeOne`: Throws an error when multiple records are affected.

## Style
- Use 2 space characters for indentation within methods and control structures (`if`, `for`, etc.). Do not use tab characters.
- Do not omit braces `{ }` in control structures (`if`, `for`, etc.), and add a line break immediately after `{` and immediately before `}`.
- Add a single space in the following locations. However, do not add spaces at the end of lines or immediately before semicolons at line ends.
    - Between keywords (`if`, `return`, etc.) and the string that follows.
    - Between keywords (`else`, `catch`, etc.) and the string that precedes.
    - Between the opening brace `{` and the string that precedes it.
    - Before and after all operators (`+`, `=`, `!==`, `&&`, etc.). However, increment and decrement operators are excluded.
    - Between commas, colons, semicolons and the string that follows.
- Target approximately 100 characters per line excluding indentation; wrap lines if they exceed this. Do not wrap unnecessarily if lines are shorter.

## Comments
- Do not comment out logic; delete it instead. If there are important notes, leave only those notes as comments.
- Do not include change history comments. If there are important notes, leave only those notes as comments.
- Write JavaDoc for all declarations except variables within methods. This does not mean variables within methods do not need explanations; write explanations as regular comments.
- Explanations for `private` scope methods, variables, and constants may be simplified.
- Enclose JavaDoc comments with `/** */`.
- Write regular comments (other than JavaDoc) on a single line with `//` at the beginning of the line. Comments at the end of lines are prohibited.

## JavaDoc
- Create `package-info.java` for each module package and include the module name.
- The format for method declarations is as follows:
    - Write the class name or method name on the first line. Add `.<br>` after the name.
    - The class name or method name should concisely describe its role or responsibility in one phrase; do not write the English class/method name itself.
    - From the second line onward, describe the role, responsibility, and processing as bullet points using `<ul>` or `<ol>`.
    - Describe method arguments with `@param` tags.
        - Write `@param` tags in the format "@param variableName description".
    - Describe method return values with `@return` tags.
        - Write `@return` tags in the format "@return description".
        - If `null` may be returned, add "(nullable)" at the end of the "description".
- Add `{@inheritDoc}` to implementations of abstract methods. JavaDoc with only `{@inheritDoc}` is acceptable.
``` Abstract method implementation JavaDoc example
  /**
   * {@inheritDoc}
   */
  @Override
```
- Use `@see` tags to reference JavaDoc of other Java classes or methods.
- Enclose sample code in text with `<pre><code>`. Use only `<code>` for short one-line code.
- Also enclose the following names with `<code>`:
    - Database table names
    - Field names
    - Java class names
    - Java method names
- Write Java method names of other Java classes in the format `<code>JavaClassName#methodName</code>`.
- When writing database table names and field names together, use the format `<code>tableName.fieldName</code>`.
