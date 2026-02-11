@echo off
rem #
rem # Java class execution subscript.
rem # Job ID is used for log output and is expected to be a unique value for each calling batch.
rem #
rem # @param $1 Job ID
rem # @param $2 Java class
rem # @param $3 onwards Arguments (all from 3rd onwards)
rem # @return Exit status of Java command
rem #

rem # Prevent affecting environment variables
setlocal

rem # Check required arguments
if "%1" == "" (
  call :printStdErr "ERROR: First argument (Job ID) is required" 
  exit /b 1
)
if "%2" == "" (
  call :printStdErr "ERROR: Second argument (Java class) is required"
  exit /b 1
)

rem # Store arguments
rem # [$1] Job ID
set JOB_ID=%1
rem # [$2] Java class
set EXEC_CLS=%2
rem # [$3 onwards] Arguments (supports up to 9)
set EXEC_ARGS=%3 %4 %5 %6 %7 %8 %9

rem # Process ID (retrieve parent process ID)
powershell -Command "try { $process = Get-CimInstance Win32_Process -Filter \"ProcessId=$pid\" -ErrorAction Stop; if ($process) { exit $process.ParentProcessId } else { exit 0 } } catch { exit 0 }"
set PS_ID=%ERRORLEVEL%

rem # Load application environment variables
call %~dp0env-app.bat
rem # Check application environment variables
if "%APP_NAME%"=="" (
  call :printStdErr "ERROR: APP_NAME is blank in env-app.bat"
  exit /b 1
)
if "%APP_HOME%"=="" (
  call :printStdErr "ERROR: APP_HOME is blank in env-app.bat"
  exit /b 1
)
if not exist "%APP_HOME%" (
  call :printStdErr "ERROR: APP_HOME not exist: %APP_HOME%"
  exit /b 1
)
if "%LOG_DIR%"=="" (
  call :printStdErr "ERROR: LOG_DIR is blank in env-app.bat"
  exit /b 1
)
if not exist "%LOG_DIR%" (
  call :printStdErr "ERROR: LOG_DIR not exist: %LOG_DIR%"
  exit /b 1
)

rem # Load Java environment variables
call %~dp0env-java.bat

rem # Check Java environment variables
if "%JAVA_BIN%"=="" (
  call :printStdErr "ERROR: JAVA_BIN is blank in env-java.bat"
  exit /b 1
)
if not exist "%JAVA_BIN%" (
  call :printStdErr "ERROR: JAVA_BIN not exist: %JAVA_BIN%"
  exit /b 1
)

rem # Log file path (date_JobID.log)
rem # Files redirecting standard output are locked and multiple processes (Java commands) cannot execute simultaneously, so include Job ID in the file name.
call :getNowDate LOG_YMD
set LOG_FILE=%LOG_DIR%\%LOG_YMD%_%JOB_ID%.log

rem # Start log
call :printLog "START: %EXEC_CLS%(%EXEC_ARGS%)"

rem # Java classpath
set JAVA_CP=%APP_HOME%\lib\*;%APP_HOME%\classes

rem # Execute Java
%JAVA_BIN%\java %JVM_XMS% %JVM_XMX% -cp %JAVA_CP% %EXEC_CLS% %EXEC_ARGS%>>%LOG_FILE% 2>&1
set EXIT_STATUS=%ERRORLEVEL%

rem # End log
call :printLog "END: EXIT(%EXIT_STATUS%)"

rem # Output alert on error
if not "%EXIT_STATUS%" == "0" (
  call :printAlert "ERROR: %EXEC_CLS%(%EXEC_ARGS%) -> EXIT(%EXIT_STATUS%)"
)

rem # Exit
exit /b %EXIT_STATUS%


rem # Sub-functions below

rem #
rem # Get current date.
rem # Sets a string in YYYYMMDD format to the argument variable.
rem #
rem # @param $1 Variable name to set date
rem #
:getNowDate
rem # Assumes %date% = YYYY/MM/DD
set "%~1=%date:~0,4%%date:~5,2%%date:~8,2%"
rem # Assumes %date% = MM/DD/YYYY or MM.DD.YYYY
rem # set "%~1=%date:~6,4%%date:~0,2%%date:~3,2%"
exit /b

rem #
rem # Get current timestamp.
rem # Sets a string in YYYYMMDD"T"HHMMSS format to the argument variable.
rem #
rem # @param $1 Variable name to set timestamp
rem #
:getNowTimestamp
call :getNowDate YMD
rem # Zero-pad time
set ZPTIME=%time: =0%
rem # Append time to date and set return value
set "%~1=%YMD%T%ZPTIME:~0,2%%ZPTIME:~3,2%%ZPTIME:~6,2%"
exit /b

rem #
rem # Standard error output.
rem # Intended for outputting prerequisite errors during script execution (insufficient arguments, environment variables not set, etc.).
rem #
rem # @param $1 Message (enclose in double quotation marks if it contains blanks)
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
rem # @param $1 Message (enclose in double quotation marks if it contains blanks)
rem #
:printLog
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% [%PS_ID%] %MSG:"=%>>%LOG_FILE% 2>&1
exit /b

rem #
rem # Alert output.
rem # Outputs to both log file and alert file.
rem # Output to alert file is intended for notification by failure monitoring application.
rem # If %ALERT_FILE% environment variable is not set, no output to alert file is performed.
rem #
rem # @param $1 Message (enclose in double quotation marks if it contains blanks)
rem #
:printAlert
set MSG=%1
call :getNowTimestamp YMDHMS
echo %YMDHMS% %APP_NAME%/%JOB_ID% [%PS_ID%] %MSG:"=%>>%LOG_FILE% 2>&1
if "%ALERT_FILE%"=="" (
  exit /b
)
echo %YMDHMS% %APP_NAME%/%JOB_ID% [%PS_ID%] %MSG:"=%>>%ALERT_FILE% 2>&1
exit /b
