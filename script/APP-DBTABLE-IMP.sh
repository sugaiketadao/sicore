#!/bin/bash
#
# DB table data import batch execution
#
# url: JDBC connection URL [e.g.] jdbc:postgresql://localhost:5432/db01
# user: DB user (optional)
# pass: DB password (optional)
# table: target table physical name
# output: output path (directory specification allowed)
# where: search condition (optional)
# zip: zip compression flag, true when compressing (optional)
#

# SQLite DB file path
readonly PARENT_DIR=$(cd $(dirname $0)/.. && pwd)
readonly JDBC_URL="jdbc:sqlite:${PARENT_DIR}/example_db/data/example.dbf"

bash $(dirname $0)/sub/java-exec.sh $(basename $0 .sh) com.onepg.app.bat.dataio.DbTableImp "url=${JDBC_URL}&input=/tmp/t_user.tsv"
