rem #
rem # Application environment variables.
rem #

rem # Application name.
rem # TODO: Change to an appropriate value according to the project
set APP_NAME=example-app

rem # Application home directory (absolute path conversion).
rem # Two directories above this sub directory (assuming APP_HOME/script/sub)
for %%I in ("%~dp0..\..") do set APP_HOME=%%~fI

rem # Log output directory.
rem # TODO: Change to an appropriate value according to the project
rem # TODO: Add user name (%USERNAME%) to the log output directory path if it cannot be written due to permission hierarchy
set LOG_DIR=c:\tmp\logs
if not exist "%LOG_DIR%" (
  rem # Create if it does not exist
  mkdir %LOG_DIR%
)

rem # Alert file path.
rem # Log file path subject to failure monitoring
rem # TODO: Change to an appropriate value according to the project
set ALERT_FILE=c:\tmp\logs\alert.log
if not exist "%ALERT_FILE%" (
  rem # Create if it does not exist
  type nul > %ALERT_FILE%
)
