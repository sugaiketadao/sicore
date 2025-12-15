# SQL Coding Rules

## Overview
- Define the coding rules for SQL.
- Following these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Comply with "ISO/IEC 9075 Database Language SQL" (hereinafter referred to as standard SQL).

## Aliases
- Add aliases to database tables, with aliases being 2-5 characters.
- Add aliases in `SELECT` statements even when using only one database table.
- Add aliases in `INSERT`, `UPDATE`, and `DELETE` statements only when using multiple database tables. This does not apply when DBMS limitations prevent adding aliases.
- As a rule, do not add aliases to database fields. Add appropriate aliases when the same database field name exists multiple times.
- When adding aliases to database fields, do not change the suffix of the database field name.
    - [NG] `u.user_nm creatorname`
    - [OK] `u.user_nm creator_nm`
- Omit `AS` when adding aliases.
- Name aliases to be unique throughout the entire query, including subqueries.

## Table Joins
- Write inner joins with `JOIN`. Omit `INNER`.
- Write outer joins with `LEFT JOIN`.
- In `ON` clause join conditions, write the joined table on the left side.
    - [OK] `FROM tbla ta JOIN tblz tz ON tz.id = ta.id`
    - [NG] `FROM tbla ta JOIN tblz tz ON ta.id = tz.id`
- Write table join statements in standard SQL. Oracle-specific notation using `(+)` is not permitted.

## SELECT Clause
- As a rule, do not use `SELECT *`. Exception is when fields are specified in subqueries.
- Use `SELECT 1` for subqueries within `EXISTS`.
- Use `COUNT(*)` when counting all retrieved records. (Use `*` as the argument.)

## Writing Rules
- Use `<>` for the negation operator.
- Do not use implicit type conversion.
- Write fixed values as literals in SQL without using bind variables.
- Do not use `DISTINCT` when `GROUP BY` can be used.
- Do not use `COALESCE` for fields that do not contain `null`.
- Do not use subqueries where they are not necessary.
- Use functions that comply with standard SQL. Oracle-specific functions such as `NVL` are not permitted.

## Style
- Use 2 space characters for indentation. Do not use tab characters.
- Use uppercase for keywords and function names.
- Use lowercase for database table names, database field names, and their aliases (other than keywords and function names).
- Add a single space before and after operators (`+`, `-`, etc.), comparison operators (`=`, `<=`, etc.), and string operations (`||`).

- Add a line break before the following:
    - `,` within `SELECT`, `GROUP BY`, `ORDER BY`, and `SET` clauses.
    - `FROM`, `WHERE`, `AND`, `OR`, `JOIN`, `LEFT JOIN`, `ON`.
    - `WHEN`, `ELSE`, `END` of `CASE`.
    - Subqueries.
- Add a line break after the following:
    - `SELECT`, `GROUP BY`, `ORDER BY`.
    - Subqueries.
- Indent the following:
    - Between `CASE` and `END` in `CASE` statements.
    - Entire subqueries.

## Comments
- Write comments in Java code; do not use SQL comments.
