#!/bin/bash
installDir="$(dirname "$0")"
java -jar "$(cat ${installDir}/jar)" "--driver" "--install-dir=$installDir"