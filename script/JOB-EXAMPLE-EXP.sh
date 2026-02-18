#!/bin/bash
#
# Execute data export batch
#
bash $(dirname $0)/sub/java-exec.sh $(basename $0 .sh) com.example.app.bat.exmodule.ExampleExport output=/tmp/example_export.txt
