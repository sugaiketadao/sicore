#!/bin/bash
#
# Java class execution subscript.
# Job ID is used for log output and is assumed to be a unique value for each calling batch.
#
# @param $1 Job ID
# @param $2 Java class
# @param $3 and later Arguments (all from the third onward)
# @return Java command exit status
#

# Variable declaration required
set -u
# Abort on non-zero return value
set -e
set -E
# Permission setting for newly created directories and files: 755
umask 022

# Display command and line number when error occurs (for debugging)
# trap 'echo "ERROR at line ${LINENO}: ${BASH_COMMAND}" >&2' ERR

#
# Gets the current date.
# Returns a string in YYYYMMDD format.
#
getNowDate() {
  date +"%Y%m%d"
}

#
# Gets the current timestamp.
# Returns a string in YYYYMMDD"T"HHMMSS format.
#
getNowTimestamp() {
  date +"%Y%m%dT%H%M%S"
}

#
# Standard error output.
# Assumes output of prerequisite errors (missing arguments, unset environment variables, etc.) when executing this script.
#
# @param $1 Message
#
printStdErr() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${MSG} >&2
}

#
# Log output.
# Outputs to log file.
#
# @param $1 Message
#
printLog() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${APP_NAME}/${JOB_ID} pid=${PS_ID} ${MSG} >> ${LOG_FILE} 2>&1
}

#
# Alert output.
# Outputs to both log file and alert file.
# Output to alert file assumes notification by failure monitoring application.
# If ALERT_FILE environment variable is not set, output to alert file is not performed.
#
# @param $1 Message
#
printAlert() {
  local MSG="$1"
  local YMDHMS=$(getNowTimestamp)
  echo ${YMDHMS} ${APP_NAME}/${JOB_ID} pid=${PS_ID} ${MSG} >> ${LOG_FILE} 2>&1
  if [[ "${ALERT_FILE}" != "" ]] ; then
    echo ${YMDHMS} ${APP_NAME}/${JOB_ID} pid=${PS_ID} ${MSG} >> ${ALERT_FILE} 2>&1
  fi
}

# Execution user check
# TODO: Delete the following if check is not required
# TODO: Change to an appropriate value according to the project
readonly ALLOW_USER=batchuser
readonly CURRENT_USER=$(whoami)
if [[ "${ALLOW_USER}" != "${CURRENT_USER}" ]] ; then
  printStdErr "ERROR: ${CURRENT_USER} is not allowed. Please execute as ${ALLOW_USER} user."
  exit 1
fi

# Required argument check
if [[ $# -lt 1 ]] ; then
  printStdErr "ERROR: First argument (Job ID) is required"
  exit 1
fi
if [[ $# -lt 2 ]] ; then
  printStdErr "ERROR: Second argument (Java class) is required"
  exit 1
fi

# Argument storage
# [$1] Job ID
readonly JOB_ID=$1
# [$2] Java class
readonly EXEC_CLS=$2
# [$3 and later] Arguments (all)
shift 2
readonly EXEC_ARGS=("$@")

# Process ID (gets the current process ID)
readonly PS_ID=$$

# Script directory (absolute path conversion).
readonly SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)

# Load application environment variables
source ${SCRIPT_DIR}/env-app.sh

# Application environment variable check
if [[ "${APP_NAME:-}" = "" ]]; then
  printStdErr "ERROR: APP_NAME is blank in env-app.sh"
  exit 1
fi
if [[ "${APP_HOME:-}" = "" ]]; then
  printStdErr "ERROR: APP_HOME is blank in env-app.sh"
  exit 1
fi
if [[ "${LOG_DIR:-}" = "" ]]; then
  printStdErr "ERROR: LOG_DIR is blank in env-app.sh"
  exit 1
fi
if [[ ! -d ${APP_HOME} ]]; then
  printStdErr "ERROR: APP_HOME not exist: ${APP_HOME}"
  exit 1
fi
if [[ ! -d ${LOG_DIR} ]]; then
  printStdErr "ERROR: LOG_DIR not exist: ${LOG_DIR}"
  exit 1
fi

# Load Java environment variables
source ${SCRIPT_DIR}/env-java.sh

# Java environment variable check
if [[ "${JAVA_BIN:-}" = "" ]]; then
  printStdErr "ERROR: JAVA_BIN is blank in env-java.sh"
  exit 1
fi
if [[ ! -d ${JAVA_BIN} ]]; then
  printStdErr "ERROR: JAVA_BIN not exist: ${JAVA_BIN}"
  exit 1
fi

# Log file path (date_JobID.log)
# The file redirecting standard output is locked and multiple processes (Java commands) cannot be executed simultaneously, so include Job ID in the file name.
readonly LOG_YMD=$(getNowDate)
readonly LOG_FILE=${LOG_DIR}/${LOG_YMD}_${JOB_ID}.log

# Start log
printLog "[START] ${EXEC_CLS}"

# Java classpath
readonly JAVA_CP=${APP_HOME}/lib/*:${APP_HOME}/classes

# Java execution
set +e
${JAVA_BIN}/java ${JVM_XMS} ${JVM_XMX} -cp "${JAVA_CP}" ${EXEC_CLS} "${EXEC_ARGS[@]}" >> ${LOG_FILE} 2>&1
readonly EXIT_STATUS=$?
set -e

# End log
printLog "[END] ${EXIT_STATUS}"

# Alert output on error
if [[ "${EXIT_STATUS}" != "0" ]] ; then
  printAlert "[ERROR] ${EXEC_CLS}(${EXIT_STATUS})"
fi

# Exit
exit ${EXIT_STATUS}
