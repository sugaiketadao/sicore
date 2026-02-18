#!/bin/bash
#
# Application environment variables.
#

# Application name.
# TODO: Change to an appropriate value according to the project
readonly APP_NAME="example-app"

# Application home directory.
# Two directories above this sub directory (assuming APP_HOME/script/sub)
readonly APP_HOME=$(cd $(dirname ${BASH_SOURCE[0]})/../.. && pwd)

# Log output directory.
# TODO: Change to an appropriate value according to the project
# TODO: Add user name (${USER}) to the log output directory path if it cannot be written due to permission hierarchy
readonly LOG_DIR="/tmp/logs"
if [[ ! -d ${LOG_DIR} ]]; then
  # Create if it does not exist
  mkdir -p ${LOG_DIR}
fi

# Alert file path.
# Log file path subject to failure monitoring
# TODO: Change to an appropriate value according to the project
readonly ALERT_FILE="/tmp/logs/alert.log"
if [[ ! -e ${ALERT_FILE} ]]; then
  # Create if it does not exist
  touch ${ALERT_FILE}
fi
