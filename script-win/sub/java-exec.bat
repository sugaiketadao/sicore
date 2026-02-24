@echo off
rem #
rem # Java class execution subscript.
rem # Job ID is used for log output and is assumed to be a unique value for each calling batch.
rem #
rem # @param $1 Job ID
rem # @param $2 Java class
rem # @param $3 and later Arguments (all from the third onward)
rem # @return Java command exit status
rem #

rem # Prevent environment variable interference
rem # Enable delayed expansion
setlocal enabledelayedexpansion

rem # Execution user check
rem # TODO: Delete the following if check is not required
rem # TODO: Change to an appropriate value according to the project
set ALLOW_USER=batchuser
for /f "tokens=*" %%i in ('whoami') do set "CURRENT_USER=%%i"
if not "%ALLOW_USER%" == "%CURRENT_USER%" (
  call :printStdErr "ERROR: %CURRENT_USER% is not allowed. Please execute as %ALLOW_USER% user."
  exit /b 1
)

rem # Required argument check
if "%1" == "" (
  call :printStdErr "ERROR: First argument (Job ID) is required" 
  exit /b 1
)
if "%2" == "" (
  call :printStdErr "ERROR: Second argument (Java class) is required"
  exit /b 1
)

rem # Argument storage
rem # [$1] Job ID
set JOB_ID=%1
rem # [$2] Java class
set EXEC_CLS=%2
rem # [$3 and later] Arguments (up to 9 supported)
set EXEC_ARGS=%3 %4 %5 %6 %7 %8 %9

rem # Process ID (gets the parent process ID)
powershell -Command "try { $process = Get-CimInstance Win32_Process -Filter \"ProcessId=$pid\" -ErrorAction Stop; if ($process) { exit $process.ParentProcessId } else { exit 0 } } catch { exit 0 }"
set PS_ID=%ERRORLEVEL%

rem # Script directory (absolute path conversion).
for %%I in ("%~dp0") do set SCRIPT_DIR=%%~fI

rem # Load application environment variables
call %SCRIPT_DIR%\env-app.bat
rem # Application environment variable check
if "%APP_NAME%"=="" (
  call :printStdErr "ERROR: APP_NAME is blank in env-app.bat"
  exit /b 1
)
if "%APP_HOME%"=="" (
  call :printStdErr "ERROR: APP_HOME is blank in env-app.bat"
  exit /b 1
)
if "%LOG_DIR%"=="" (
  call :printStdErr "ERROR: LOG_DIR is blank in env-app.bat"
  exit /b 1
)
if not exist "%APP_HOME%" (
  call :printStdErr "ERROR: APP_HOME not exist: %APP_HOME%"
  exit /b 1
)
if not exist "%LOG_DIR%" (
  call :printStdErr "ERROR: LOG_DIR not exist: %LOG_DIR%"
  exit /b 1
)

rem # Load Java environment variables
call %SCRIPT_DIR%env-java.bat

rem # Java environment variable check
if "%JAVA_BIN%"=="" (
  call :printStdErr "ERROR: JAVA_BIN is blank in env-java.bat"
  exit /b 1
)
if not exist "%JAVA_BIN%" (
  call :printStdErr "ERROR: JAVA_BIN not exist: %JAVA_BIN%"
  exit /b 1
)

rem # Log file path (date_JobID.log)
rem # The file redirecting standard output is locked and multiple processes (Java commands) cannot be executed simultaneously, so include Job ID in the file name.
call :getNowDate LOG_YMD
set LOG_FILE=%LOG_DIR%\%LOG_YMD%_%JOB_ID%.log

rem # Start log
call :printLog "[START] %EXEC_CLS%"

rem # Java classpath
set JAVA_CP=%APP_HOME%\lib\*;%APP_HOME%\classes

rem # Java execution
%JAVA_BIN%\java %JVM_XMS% %JVM_XMX% -cp "%JAVA_CP%" %EXEC_CLS% %EXEC_ARGS%>>%LOG_FILE% 2>&1
set EXIT_STATUS=%ERRORLEVEL%

rem # End log
call :printLog "[END] %EXIT_STATUS%"

rem # Alert output on error
if not "%EXIT_STATUS%" == "0" (
  call :printAlert "[ERROR] %EXEC_CLS%(%EXIT_STATUS%)"
)

rem # Exit
exit /b %EXIT_STATUS%


rem # Below are sub functions

rem #
rem # Gets the current date.
rem # Sets a string in YYYYMMDD format to the argument variable.
rem #
rem # @param $1 Variable name to set the date
rem #
:getNowDate
if "%DATE_FORMAT%"=="US" (
  rem # Assumes MM/DD/YYYY format
  set "%~1=%date:~6,4%%date:~0,2%%date:~3,2%"
) else if "%DATE_FORMAT%"=="EU" (
  rem # Assumes DD/MM/YYYY format
  set "%~1=%date:~6,4%%date:~3,2%%date:~0,2%"
) else (
  rem # Assumes YYYY/MM/DD format
  set "%~1=%date:~0,4%%date:~5,2%%date:~8,2%"
)
exit /b

rem #
rem # Gets the current timestamp.
rem # Sets a string in YYYYMMDD"T"HHMMSS format to the argument variable.
rem #
rem # @param $1 Variable name to set the timestamp
rem #
:getNowTimestamp
call :getNowDate YMD
rem # Zero-pad the time
set ZPTIME=%time: =0%
rem # Add time to date and set to return value
set "%~1=%YMD%T%ZPTIME:~0,2%%ZPTIME:~3,2%%ZPTIME:~6,2%"
exit /b

rem #
rem # Standard error output.
rem # Assumes output of prerequisite errors (missing arguments, unset environment variables, etc.) when executing this script.
rem #
rem # @param $1 Message (enclose in double quotations if it contains blanks)
rem #
:printStdErr
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %MSG:"=%>&2
exit /b

rem #
rem # Log output.
rem # Outputs to log file.
rem #
rem # @param $1 Message (enclose in double quotations if it contains blanks)
rem #
:printLog
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%LOG_FILE% 2>&1
exit /b

rem #
rem # Alert output.
rem # Outputs to both log file and alert file.
rem # Output to alert file assumes notification by failure monitoring application.
rem # If %ALERT_FILE% environment variable is not set, output to alert file is not performed.
rem #
rem # @param $1 Message (enclose in double quotations if it contains blanks)
rem #
:printAlert
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%LOG_FILE% 2>&1
if "%ALERT_FILE%"=="" (
  exit /b
)
echo %YMDHMS% %APP_NAME%/%JOB_ID% pid=%PS_ID% %MSG:"=%>>%ALERT_FILE% 2>&1
exit /b
