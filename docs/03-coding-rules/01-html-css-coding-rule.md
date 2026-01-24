# HTML/CSS Coding Rules

## Overview
- Defines coding rules for HTML and CSS.
- By following these rules, the code is standardized and development/maintenance efficiency is improved.

## Prerequisites
- Follow the [Web Page Structure Standard](../02-develop-standards/01-web-page-structure.md).

## name/data-name/id Attribute Names and CSS Class Names
- Use lower_snake_case for `name` attribute names. For list sections only, `.` is allowed. (Refer to [Web Page Structure Standard - name attribute](../02-develop-standards/01-web-page-structure.md#name-attribute).)
    - [Example 1] `name="lower_snake_case"`
    - [Example 2] `name="list.lower_snake_case"`
- Use the same naming convention as `name` attributes for `data-name` attribute names.
- Use lowerCamelCase for `id` attribute names.
    - [Example] `id="lowerCamelCase"`
- Use lowercase kebab-case for CSS class names.
    - [Example] `class="lower-kebab-case"`
- DO NOT use `name` attribute names, `id` attribute names, or CSS class names that begin with an underscore `_`, as they are reserved for use by this framework.

## HTML Style
- Use 2 spaces for indentation when nesting elements. Do not use tab characters.
- Use lowercase for tag names.
    - [OK Example] `<input>`
    - [NG Example] `<INPIT>`
- Use lowercase for attribute names and enclose attribute values in double quotation marks.
    - [OK Example] `type="text"`
    - [NG Example] `TYPE='TEXT'`
- Do not close input elements.
    - [OK Example] `<input type="text">`
    - [NG Example] `<input type="text"/>`
- For boolean attributes such as checked, disabled, and readonly, write only the attribute name.
    - [OK Example] `<input readonly>`
    - [NG Example] `<input readonly="readonly">`
- Do not insert spaces before or after the equals sign in attributes.
    - [OK Example] `type="text"`
    - [NG Example] `type = "text"`
- Do not insert unnecessary line breaks before or after string literals within elements. When inserting line breaks, do not add unnecessary indentation.
    ```[OK Example]
    <label>[OK Example]<br>
    Do not insert unnecessary line breaks before or after string literals within elements.<br>
    When inserting line breaks, do not add unnecessary indentation.<label>
    ```
    ```[NG Example]
    <label>
        [NG Example]<br>
        Do not insert unnecessary line breaks before or after string literals within elements.<br>
        When inserting line breaks, do not add unnecessary indentation.
    <label>
    ```

## CSS Style
- Use 2 spaces for indentation within braces. Do not use tab characters.
- Insert a line break immediately after `{` and immediately before `}`.
- Enclose strings in double quotation marks.
- Insert one space in the following locations. However, do not insert spaces at the end of lines or immediately before semicolons at the end of lines.
    - Before and after child selectors `>` and adjacent selectors `+`.
    - Before and after operators (`+`, `-`, etc.).
    - Between colons and the following string.
    - Between `!important` and the preceding string.
- Omit units for zero values.

## Comments
- Delete HTML tags and CSS classes instead of commenting them out. If there are important notes, leave only the notes as comments.
- Do not write change history comments. If there are important notes, leave only the notes as comments.
