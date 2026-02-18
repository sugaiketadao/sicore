#!/bin/bash
#
# Java environment variables.
#

# Java home directory.
# TODO: If set in OS environment variables, the following is unnecessary and can be deleted
# TODO: Change to an appropriate value according to the project
readonly JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Java binary path.
readonly JAVA_BIN=${JAVA_HOME}/bin

# JVM heap area initial size default value.
# Do not overwrite if individually set
if [[ ! -v JVM_XMS ]]; then
  # TODO: Change to an appropriate value according to the project
  readonly JVM_XMS="-Xms128m"
fi

# JVM heap area maximum size default value.
# Do not overwrite if individually set
if [[ ! -v JVM_XMX ]]; then
  # TODO: Change to an appropriate value according to the project
  readonly JVM_XMX="-Xmx1024m"
fi
