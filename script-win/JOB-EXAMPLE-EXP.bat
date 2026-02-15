@echo off
rem #
rem # Execute data export batch
rem #
call %~dp0sub\java-exec.bat %~n0 com.example.app.bat.exmodule.ExampleExport "output=C:\tmp\example_export.txt"
