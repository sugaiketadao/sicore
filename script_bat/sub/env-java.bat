rem #
rem # Java environment variables.
rem #

rem # Java home directory.
rem # TODO: If set in OS environment variables, delete the following line
rem # TODO: Change to appropriate value for your project
set JAVA_HOME=C:\pleiades\java\21

rem # Java binary path.
set JAVA_BIN=%JAVA_HOME%\bin

rem # JVM heap area initial size default value.
rem # Do not overwrite if individually configured
if not defined JVM_XMS (
  rem # TODO: Change to appropriate value for your project
  set JVM_XMS=-Xms128m
)

rem # JVM heap area maximum size default value.
rem # Do not overwrite if individually configured
if not defined JVM_XMX (
  rem # TODO: Change to appropriate value for your project
  set JVM_XMX=-Xmx1024m
)
