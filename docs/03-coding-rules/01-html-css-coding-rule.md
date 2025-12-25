# HTML/CSS Coding Rules

## Overview
- Define the coding rules for HTML and CSS.
- Following these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Follow the [Web Page Structure Standards](../02-develop-standards/01-web-page-structure.md).

## name Attribute Names, data-name Attribute Names, id Attribute Names, and CSS Class Names
- Use lower_snake_case for `name` attribute names. Use of `.` is permitted only in list sections. (Refer to [Web Page Structure Standards - name Attribute](../02-develop-standards/01-web-page-structure.md#name-attribute).)
    - [Example 1] `name="lower_snake_case"`
    - [Example 2] `name="list.lower_snake_case"`
- The naming convention for `data-name` attribute names follows the same rules as `name` attributes.
- Use lowerCamelCase for `id` attribute names.
    - [Example] `id="lowerCamelCase"`
- Use lowercase kebab-case for CSS class names.
    - [Example] `class="lower-kebab-case"`
- `name` attribute names, `id` attribute names, and CSS class names starting with underscore `_` are reserved for this framework and MUST NOT be used by applications.

## HTML Style
- Use 2 space characters for indentation when writing nested elements. Do not use tab characters.
- Use lowercase for tag names.
    - [OK] `<input>`
    - [NG] `<INPIT>`
- Use lowercase for attribute names and enclose attribute values in double quotes.
    - [OK] `type="text"`
    - [NG] `TYPE='TEXT'`
- Do not self-close input elements.
    - [OK] `<input type="text">`
    - [NG] `<input type="text"/>`
- Write boolean attributes such as checked, disabled, readonly with the attribute name only.
    - [OK] `<input readonly>`
    - [NG] `<input readonly="readonly">`
- Do not put spaces before or after the equals sign in attributes.
    - [OK] `type="text"`
    - [NG] `type = "text"`
- Do not add unnecessary line breaks before or after string literals within elements. When adding line breaks, do not add unnecessary indentation.
    ```[OK]
    <label>[OK]<br>
    Do not add unnecessary line breaks before or after string literals within elements.<br>
    When adding line breaks, do not add unnecessary indentation.<label>
    ```
    ```[NG]
    <label>
        [NG]<br>
        Do not add unnecessary line breaks before or after string literals within elements.<br>
        When adding line breaks, do not add unnecessary indentation.
    <label>
    ```

## CSS Style
- Use 2 space characters for indentation inside braces. Do not use tab characters.
- Add a line break immediately after `{` and immediately before `}`.
- Enclose strings in double quotes.
- Add a single space in the following locations. However, do not add spaces at the end of lines or immediately before semicolons at line ends.
    - Before and after child selectors `>`, adjacent selectors `+`, etc.
    - Before and after operators (`+`, `-`, etc.).
    - Between a colon and the string that follows it.
    - Between `!important` and the string that precedes it.
- Omit units for zero values.

## Comments
- Do not comment out HTML tags or CSS classes; delete them instead. If there are important notes, leave only those notes as comments.
- Do not include change history comments. If there are important notes, leave only those notes as comments.
