@echo off
rem #
rem # DB table data import batch execution
rem #
rem # url: JDBC connection URL [e.g.] jdbc:postgresql://localhost:5432/db01
rem # user: DB user (optional)
rem # pass: DB password (optional)
rem # table: target table physical name
rem # output: output path (directory specification allowed)
rem # where: search condition (optional)
rem # zip: zip compression flag, true when compressing (optional)
rem #

rem # SQLite DB file path (absolute path conversion)
for %%I in ("%~dp0..") do set PARENT_DIR=%%~fI
set JDBC_URL=jdbc:sqlite:%PARENT_DIR:\=/%/example_db/data/example.dbf

call %~dp0sub\java-exec.bat %~n0 com.onepg.app.bat.dataio.DbTableImp "url=%JDBC_URL%&input=C:\tmp\t_user.tsv"
