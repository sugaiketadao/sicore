# JavaScript Coding Rules

## Overview
- Defines coding rules for JavaScript.
- By following these rules, the code is standardized and development/maintenance efficiency is improved.

## Prerequisites
- Follow the [Web Page Structure Standard](../02-develop-standards/01-web-page-structure.md).

## Variable/Method/Class Names
- Use lowerCamelCase for variable names.
    - [Example] `const lowerCamelCase = req['lower_snake_case']`
- Use UPPER_SNAKE_CASE for variable names declared with `const` and initialized with a literal (i.e., constant names).
    - [Example] `const UPPER_SNAKE_CASE = 'SNAKE'`
- Use lower_snake_case for object (associative array) keys.
    - [Example] `const obj = {lower_snake_case: 'snake'}`
- Use lowerCamelCase for method names.
    - [Example 1] `const init = async function`
    - [Example 2] `const editMove = async function`
- Use UpperCamelCase for class names.
    - [Example 1] `const UpperCamelCase = /** @lends UpperCamelCase */ {`
    - [Example 2] `class UpperCamelCase {`
- DO NOT use `remove` alone as a method name or variable name.
    - [NG] `const remove = function`
    - [OK] `const removeLine = function`
- DO NOT use method names, variable names, constant names, or object keys (including those in requests and responses) that begin with an underscore `_`, as they are reserved for use by this framework.

## Declarations
- In principle, declare variables with `const` and do not reuse them. Declare with `let` as an exception for the following use cases. Do not use `var` declarations.
    - Incrementing array indices and similar.
    - Incremental string additions or replacements.
    - Incremental array additions.
    - Cases where performance degrades significantly.
- Declare local variables (variables within methods) immediately before use, in locations where the scope is as small as possible.
- Do not use member variables (variables outside methods, excluding constants).
- Do not perform class inheritance for module classes.
- Create common components for modules as utility classes.
- Use utility classes without instantiation.
- Write arrays and objects (associative arrays) using literals.
    - [NG 1] `const ary = new Array()`
    - [NG 2] `const obj = new Object()`
    - [OK 1] `const ary = []`
    - [OK 2] `const obj = {}`
- Do not enclose object keys in single quotes.
    - [NG] `const obj = {'key1': 'V1', 'key2': 2}`
    - [OK] `const obj = {key1: 'V1', key2: 2}`

## Access Methods
- Use bracket notation for accessing object (associative array) keys.
    - [NG] `const val = obj.user_id` (dot notation)
    - [OK] `const val = obj['user_id']`

## Comparison/Evaluation
- Use `===` and `!==` instead of `==` and `!=` for equality operators.
- Do not compare `boolean` variables with `true`/`false`.
    - [NG] `if (hasError === true)`
    - [OK] `if (hasError)`
- When determining whether a variable's value is blank (including `null`) or the variable itself is `undefined`, do not evaluate directly; use framework components. The same applies when determining whether arrays or objects contain zero items.
    - [NG (direct evaluation)] `if (!val)`
    - [OK 1] `if (ValUtil.isBlank(val))`
    - [OK 2] `if (ValUtil.isEmpty(ary))`

## Processing Methods
- In module processing, do not create classes; instead, create a separate method for each event handler (one method per event).
- Use `of` for array loop processing.
    - [Example] `for (const val of ary)`
- Use `in` for object (associative array) loop processing.
    - [Example] `for (const key in obj)`
- Prioritize diff readability during modifications; DO NOT use ternary operators or arrow functions `=>`.

## Error Handling
- DO NOT catch errors in module processing.

## Logging
- DO NOT output console logs in module processing. Even if temporarily added during development, remove them before testing is complete.

## Style
- Use 2 spaces for indentation within methods and control structures (`if`, `for`, etc.). Do not use tab characters.
- Do not omit braces `{ }` for control structures (`if`, `for`, etc.); insert a line break immediately after `{` and immediately before `}`.
- Enclose strings in single quotes.
- Actively use template literals when concatenating variables into strings.
- When wrapping long string literals across lines, use template literals instead of appending `\` at the end of lines.
- Insert one space in the following locations. However, do not insert spaces at the end of lines or immediately before semicolons at the end of lines.
    - Between reserved words (`if`, `return`, etc.) and the following string.
    - Between reserved words (`else`, `catch`, etc.) and the preceding string.
    - Between the opening brace `{` and the preceding string.
    - Before and after all operators (`+`, `=`, `!==`, `&&`, etc.). However, exclude increment and decrement operators.
    - Between commas, colons, semicolons and the following string.
- Aim for approximately 120 columns per line excluding indentation, and wrap lines if they exceed this. Do not wrap lines unnecessarily if they are shorter.

## Comments
- Delete logic instead of commenting it out. If there are important notes, leave only the notes as comments.
- Do not write change history comments. If there are important notes, leave only the notes as comments.
- Write JSDoc for all declarations except variables within methods. This does not mean that variables within methods do not need explanations; write explanations as regular comments.
- Descriptions of methods, variables, and constants declared with `private` scope may be simplified.
- Enclose JSDoc comments in `/** */`.
- Write regular comments (non-JSDoc) using the format of adding `//` at the beginning of a line, using one line. DO NOT write comments at the end of lines.

## JSDoc
- Format method declarations as follows:
    - Write the class name or method name on the first line. Add `.<br>` after the name.
    - The class name or method name should concisely express its role or responsibility in one phrase; do not write the actual English class/method name itself.
    - From the second line onward, describe the role, responsibility, or processing using bullet points with `<ul>` or `<ol>`.
    - Enclose sample code in `<pre><code>`. Write HTML tags inside `<code>` using escape characters.
    - Describe method parameters using the `@param` tag.
        - Write the `@param` tag in the format "@param {type} variableName description".
        - Enclose the "variableName" of optional parameters in `[]`, and write "(optional)" at the end of the "description".
        - Write "{type}" as [Example] `{string}`, `{Object}`, `{Array<string>}`, etc.
    - Describe method return values using the `@returns` tag.
        - Write the `@returns` tag in the format "@returns {type} description".
        - Write "{type}" as [Example] `{string}`, `{Object}`, `{Array<string>}`, etc.
        - If `null` may be returned as a return value, write "{type}" as [Example] `{Object|null}`.
- Add the `@private` tag to methods, variables, and constants with `private` scope.
