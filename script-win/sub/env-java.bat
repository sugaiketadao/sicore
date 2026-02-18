rem #
rem # Java environment variables.
rem #

rem # Java home directory.
rem # TODO: If set in OS environment variables, the following is unnecessary and can be deleted
rem # TODO: Change to an appropriate value according to the project
set JAVA_HOME=C:\pleiades\java\21

rem # Java binary path.
set JAVA_BIN=%JAVA_HOME%\bin

rem # JVM heap area initial size default value.
rem # Do not overwrite if individually set
if not defined JVM_XMS (
  rem # TODO: Change to an appropriate value according to the project
  set JVM_XMS=-Xms128m
)

rem # JVM heap area maximum size default value.
rem # Do not overwrite if individually set
if not defined JVM_XMX (
  rem # TODO: Change to an appropriate value according to the project
  set JVM_XMX=-Xmx1024m
)
