# SQL Coding Rules

## Overview
- Defines the SQL coding rules.
- Adherence to these rules aims to standardize program code and improve development and maintenance efficiency.

## Prerequisites
- Conform to "ISO/IEC 9075 Database Language SQL" (hereinafter referred to as standard SQL).

## Aliases
- Add an alias to DB tables, with aliases being 2–5 characters.
- Add an alias to `SELECT` statements even when only a single DB table is used.
- For `INSERT`, `UPDATE`, and `DELETE` statements, add aliases only when multiple DB tables are used. This does not apply when the DBMS restricts alias usage.
- As a general rule, do not add aliases to DB columns. If duplicate DB column names exist, add appropriate aliases.
- When adding an alias to a DB column, do not change the suffix of the DB column name.
    - [NG Example] `u.user_nm AS creatorname`
    - [OK Example] `u.user_nm AS creator_nm`
- DO NOT omit `AS` when adding aliases.
- Aliases MUST be unique throughout the entire query, including subqueries.

## Table Joins
- Write inner joins using `JOIN`. Omit `INNER`.
- Write outer joins using `LEFT JOIN`.
- In the `ON` clause for join conditions, write the column from the `JOIN`ed table on the left side.
    - [OK Example] `FROM tbla AS ta JOIN tblz AS tz ON tz.id = ta.id`
    - [NG Example] `FROM tbla AS ta JOIN tblz AS tz ON ta.id = tz.id`
- Write table join statements in standard SQL. The Oracle-specific syntax using `(+)` is not permitted.

## SELECT Clause
- As a general rule, DO NOT use `SELECT *`. An exception is made when columns are specified in a subquery. (Example: `SELECT * FROM (SELECT id, name FROM tbla)`)
- Use `SELECT 1` for subqueries inside `EXISTS`.
- Use `COUNT(*)` when counting all retrieved records. (Use `*` as the argument.)

## Coding Rules
- Use `<>` instead of `!=` for the inequality operator.
- Do not use implicit type conversions.
- For fixed values (such as `'1'` in `WHERE status = '1'`), write them as literals in the SQL rather than using SQL bind variable `?`.
- Do not use `DISTINCT` when `GROUP BY` is available.
- Do not use `COALESCE` on columns that cannot contain `null`.
- Do not use subqueries where they are unnecessary.
- Use functions conforming to standard SQL. Oracle-specific functions such as `NVL` are not permitted.

## Style
- Use 2 half-width spaces for indentation; do not use tab characters.
- Use uppercase for reserved words and function names.
- Use lowercase for DB table names, DB column names, and their aliases (other than reserved words and function names).
- Add 1 half-width space before and after arithmetic operators (`+`, `-`, etc.), comparison operators (`=`, `<=`, etc.), and string concatenation (`||`).
- Add a line break immediately before the following:
    - `,` inside `SELECT`, `GROUP BY`, `ORDER BY`, and `SET` clauses.
    - `FROM`, `WHERE`, `AND`, `OR`, `JOIN`, `LEFT JOIN`, `ON`.
    - `WHEN`, `ELSE`, `END` in a `CASE` expression.
    - Subqueries.
- Add a line break immediately after the following:
    - `SELECT`, `GROUP BY`, `ORDER BY`.
    - Subqueries.
- Apply indentation to the following:
    - Between `CASE` and `END` in a `CASE` statement.
    - The entire subquery.

## Comments
- Write comments in Java code; do not use SQL comments.
