rem #
rem # Application environment variables.
rem #

rem # Application name.
rem # TODO: Change to appropriate value for your project
set APP_NAME=example-app

rem # Application home directory.
rem # Two levels up from this sub directory (assumes APP_HOME/script/sub)
set APP_HOME=%~dp0..\..
rem # Convert to absolute path
pushd %APP_HOME%
set APP_HOME=%CD%
popd

rem # Log output directory.
rem # If write permission is restricted, add user name (%USERNAME%) to the log output directory path
rem # TODO: Change to appropriate value for your project
set LOG_DIR=c:\tmp\logs\%USERNAME%
if not exist "%LOG_DIR%" (
  rem # Create if it does not exist
  mkdir %LOG_DIR%
)

rem # Alert file path.
rem # Log file path for failure monitoring
rem # TODO: Change to appropriate value for your project
set ALERT_FILE=c:\tmp\logs\alert.log
