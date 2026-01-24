# SQL Coding Rules

## Overview
- Defines coding rules for SQL.
- By following these rules, the code is standardized and development/maintenance efficiency is improved.

## Prerequisites
- Conform to "ISO/IEC 9075 Database Language SQL" (hereinafter referred to as standard SQL).

## Aliases
- Add aliases to database tables with 2-5 characters.
- Add aliases to `SELECT` statements even when using only one database table.
- Add aliases to `INSERT`/`UPDATE`/`DELETE` statements only when using multiple database tables. This does not apply if DBMS restrictions prevent adding aliases.
- In principle, do not add aliases to database fields. Add appropriate aliases when multiple fields with the same name exist.
- When adding aliases to database fields, do not change the suffix of the field name.
    - [NG Example] `u.user_nm creatorname`
    - [OK Example] `u.user_nm creator_nm`
- Omit `AS` when adding aliases.
- Name aliases to be unique throughout, including subqueries.

## Table Joins
- Write inner joins using `JOIN`. Omit `INNER`.
- Write outer joins using `LEFT JOIN`.
- In the join condition `ON` clause, write the table being `JOIN`ed on the left side.
    - [OK Example] `FROM tbla ta JOIN tblz tz ON tz.id = ta.id`
    - [NG Example] `FROM tbla ta JOIN tblz tz ON ta.id = tz.id`
- Write table join statements in standard SQL. Do not use the Oracle-specific `(+)` notation.

## SELECT Clause
- In principle, do not use `SELECT *`. Exception is when fields are specified in subqueries.
- Use `SELECT 1` in subqueries within `EXISTS`.
- Use `COUNT(*)` when counting all retrieved records. (Use `*` as the argument.)

## Description Rules
- Use `<>` instead of `!=` for inequality operators.
- Do not use implicit type conversion.
- Use SQL bind variable `?` instead of writing fixed values as literals in SQL.
- Do not use `DISTINCT` when `GROUP BY` is available.
- Do not use `COALESCE` for fields that do not contain null.
- Do not use subqueries where they are not needed.
- Use functions that conform to standard SQL. Oracle-specific functions such as `NVL` are not allowed.

## Style
- Use 2 spaces for indentation. Do not use tab characters.
- Use uppercase for reserved words and function names.
- Use lowercase for database table names, database field names, and their aliases, excluding reserved words and function names.
- Insert one space before and after operators (`+`, `-`, etc.), comparison operators (`=`, `<=`, etc.), and string operations (`||`).
- Insert a line break immediately before the following locations:
    - `,` within `SELECT` clause, `GROUP BY` clause, `ORDER BY` clause, and `SET` clause.
    - `FROM`, `WHERE`, `AND`, `OR`, `JOIN`, `LEFT JOIN`, `ON`.
    - `WHEN`, `ELSE`, `END` in `CASE` statements.
    - Subqueries.
- Insert a line break immediately after the following locations:
    - `SELECT`, `GROUP BY`, `ORDER BY`.
    - Subqueries.
- Decrease indentation in the following locations:
    - Between `CASE` and `END` in `CASE` statements.
    - Entire subqueries.

## Comments
- Write comments in Java code, not in SQL comments.
