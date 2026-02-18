#!/bin/bash
#
# Execute data import batch
#
bash $(dirname $0)/sub/java-exec.sh $(basename $0 .sh) com.example.app.bat.exmodule.ExampleImport input=/tmp/example_export.txt
