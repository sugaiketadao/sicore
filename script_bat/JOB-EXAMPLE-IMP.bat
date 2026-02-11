@echo off
rem #
rem # Execute data import batch
rem #
call %~dp0sub\java-exec.bat %~n0 com.example.app.bat.exmodule.ExampleImport "input=C:\tmp\example_export.txt"
