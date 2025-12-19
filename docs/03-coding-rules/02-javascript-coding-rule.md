# JavaScript Coding Rules

## Overview
- Define the coding rules for JavaScript.
- Following these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Follow the [Web Page Structure Standards](../02-develop-standards/01-web-page-structure.md).

## Variable Names, Method Names, and Class Names
- Use lowerCamelCase for variable names.
    - [Example] `const lowerCamelCase = req['lower_snake_case']`
- Use UPPER_SNAKE_CASE for variable names declared with const and initialized with literals (= constant names).
    - [Example] `const UPPER_SNAKE_CASE = 'SNAKE'`
- Use lower_snake_case for object keys.
    - [Example] `const obj = {lower_snake_case: 'snake'}`
- Use lowerCamelCase for method names.
    - [Example 1] `const init = async function`
    - [Example 2] `const editMove = async function`
- Use UpperCamelCase for class names.
    - [Example 1] `const UpperCamelCase = /** @lends UpperCamelCase */ {`
    - [Example 2] `class UpperCamelCase {`
- Do not use `remove` alone as a method name or variable name.
    - [NG] `const remove = function`
    - [OK] `const removeLine = function`
- Method names, variable names, constant names, and object keys (including request and response) starting with underscore `_` are reserved for this framework and MUST NOT be used by applications.

## Declarations
- As a rule, declare variables with `const` and do not reuse them. The following uses are exceptions and should be declared with `let`. Do not use `var` declarations.
    - Array index increments, etc.
    - Progressive string additions or replacements.
    - Progressive array additions.
    - When performance is significantly degraded.
- Declare local variables (variables within methods) immediately before use, in locations where the scope is as small as possible.
- Do not use member variables (variables outside methods, excluding constants).
- Do not use class inheritance in module classes.
- Create common components for modules as utility classes.
- Use utility classes without instantiation.
- Write arrays and objects using literal notation.
    - [NG 1] `const ary = new Array()`
    - [NG 2] `const obj = new Object()`
    - [OK 1] `const ary = []`
    - [OK 2] `const obj = {}`
- Do not enclose object keys in single quotes.
    - [NG] `const obj = {'key1': 'V1', 'key2': 2}`
    - [OK] `const obj = {key1: 'V1', key2: 2}`

## Access Method
- Use bracket notation for object key access.
    - [NG] `const val = obj.user_id` (dot notation)
    - [OK] `const val = obj['user_id']`

## Comparison and Evaluation
- Use `===` and `!==` instead of `==` and `!=` for equality operators.
- Do not compare `boolean` variables with `true`/`false`.
    - [NG] `if (hasError === true)`
    - [OK] `if (hasError)`
- When checking if a variable's value is blank (including `null`) or if the variable itself is `undefined`, do not check with the variable alone; use this framework's utilities. The same applies when checking if arrays or objects have zero elements.
    - [NG] `if (!val)`
    - [OK 1] `if (ValUtil.isBlank(val))`
    - [OK 2] `if (ValUtil.isEmpty(ary))`

## Processing Method
- Do not create classes for module processing; use one method per process.
- Use the `of` method for array loop processing.
    - [Example] `for (const val of ary)`
- Use the `in` method for object loop processing.
    - [Example] `for (const key in obj)`
- Prioritize readability of diffs during modifications; prohibit the use of ternary operators and arrow functions `=>`.

## Error Handling
- Do not catch errors in module processing.

## Logging
- Do not output console logs in module processing. Even if temporarily output during development, remove them before test completion.

## Style
- Use 2 space characters for indentation within methods and control structures (`if`, `for`, etc.). Do not use tab characters.
- Do not omit braces `{ }` in control structures (`if`, `for`, etc.), and add a line break immediately after `{` and immediately before `}`.
- Enclose strings in single quotes.
- Actively use template literals when concatenating variables with strings.
- When breaking long string literals across lines, do not use the backslash `\` at the end of lines; use template literals instead.
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
- Write JSDoc for all declarations except variables within methods. This does not mean variables within methods do not need explanations; write explanations as regular comments.
- Explanations for private scope methods, variables, and constants may be simplified.
- Enclose JSDoc comments with `/** */`.
- Write regular comments (other than JSDoc) on a single line with `//` at the beginning of the line. Comments at the end of lines are prohibited.

## JSDoc
- The format for method declarations is as follows:
    - Write the class name or method name on the first line. Add `.<br>` after the name.
    - The class name or method name should concisely describe its role or responsibility in one phrase; do not write the English class/method name itself.
    - From the second line onward, describe the role, responsibility, and processing as bullet points using `<ul>` or `<ol>`.
    - Enclose sample code with `<pre><code>`. Use escape characters for HTML tags inside `<code>`.
    - Describe method arguments with `@param` tags.
        - Write `@param` tags in the format "@param {type} variableName description".
        - Enclose optional argument "variableName" in `[]` and add "(optional)" at the end of the "description".
        - Write "{type}" as, for example, `{string}`, `{Object}`, `{Array<string>}`, etc.
    - Describe method return values with `@returns` tags.
        - Write `@returns` tags in the format "@returns {type} description".
        - Write "{type}" as, for example, `{string}`, `{Object}`, `{Array<string>}`, etc.
        - If `null` may be returned, write "{type}" as, for example, `{Object|null}`.
- Add `@private` tag to private scope methods, variables, and constants.
