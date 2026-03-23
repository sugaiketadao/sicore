# HTML/CSS Coding Rules

## Overview
- Defines coding rules for HTML and CSS.
- By following these rules, the code is standardized and development/maintenance efficiency is improved.

## Prerequisites
- Follow the [Web Page Structure Standard](../02-develop-standards/01-web-page-structure.md).

## name / data-name / id Attribute Names and CSS Class Names
- Use lower_snake_case for `name` attribute names. Use of `.` is permitted only for list parts. (See [Web Page Structure Standard - name Attribute](../02-develop-standards/01-web-page-structure.md#name-attribute).)
    - [Example 1] `name="lower_snake_case"`
    - [Example 2] `name="list.lower_snake_case"`
- Apply the same naming convention as `name` attributes to `data-name` attribute names.
- Use lowerCamelCase for `id` attribute names.
    - [Example] `id="lowerCamelCase"`
- Use lower-kebab-case for CSS class names.
    - [Example] `class="lower-kebab-case"`
- DO NOT use `name` attribute names, `id` attribute names, or CSS class names that begin with an underscore `_`, as they are reserved for use by this framework.

## HTML Style
- Use 2 spaces for indentation when writing nested elements. Do not use tab characters.
- Use lowercase for tag names.
    - [OK] `<input>`
    - [NG] `<INPUT>`
- Use lowercase for attribute names, and enclose attribute values in double quotes.
    - [OK] `type="text"`
    - [NG] `TYPE='TEXT'`
- Do not self-close `input` elements.
    - [OK] `<input type="text">`
    - [NG] `<input type="text"/>`
- Write boolean attributes such as `checked`, `disabled`, and `readonly` using only the attribute name.
    - [OK] `<input readonly>`
    - [NG] `<input readonly="readonly">`
- Do not insert spaces before or after the equals sign `=` of attributes.
    - [OK] `type="text"`
    - [NG] `type = "text"`
- Do not insert unnecessary line breaks before or after the text content of an element (immediately after the opening tag or immediately before the closing tag). When inserting `<br>`, do not add indentation before or after `<br>`.
    ```[OK]
    <label>[OK]<br>
    Do not insert unnecessary line breaks before or after text content of an element.<br>
    When inserting a line break, do not add indentation before or after it.<label>
    ```
    ```[NG]
    <label>
        [NG]<br>
        Do not insert unnecessary line breaks before or after text content of an element.<br>
        When inserting a line break, do not add indentation before or after it.
    </label>
    ```

## CSS Style
- Use 2 spaces for indentation within braces. Do not use tab characters.
- Insert a line break immediately after `{` and immediately before `}`.
- Enclose strings in double quotes.
- Insert one space in the following locations. However, do not insert spaces at the end of lines or immediately before semicolons at the end of lines.
    - Before and after child selectors `>`, adjacent selectors `+`, and similar selectors.
    - Before and after operators (`+`, `-`, etc.).
    - Between a colon and the string that follows it.
    - Between `!important` and the string that precedes it.
- Omit units for numeric zero values.

## Comments
- Delete HTML tags and CSS classes instead of commenting them out. If there are important notes, leave only the notes as comments.
- Do not write change history comments. If there are important notes, leave only the notes as comments.
