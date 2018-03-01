#!/bin/bash
trap close INT
function close() {
    echo "Bye!"
    exit 0
}

echo "Welcome!"
echo "Checking required packages..."
if [ -z "$(which java)" ]; then
    echo "Java not found!"
    exit 3
fi
if [ -z "$(which socat)" ]; then
    echo "socat not found!"
    read -p "Do you want to install it (y/n)? " ri
    if [ "$ri" == "y" ]; then
        echo "Installing..."
        sudo apt-get install socat
        echo "Done."

    else
        exit 3
    fi
fi
if [ -z "$(which indiserver)" ]; then
    echo "INDI not found!"
    exit 3
fi

echo "Loading AstroAllInOne..."
installDir="$(dirname "$0")/AstroAllInOne.jar"
if [ -n "$1" ]; then
    installDir="$1"
fi
if [ -f "$installDir" ]; then
        echo "Using installed AstroAllInOne in $(dirname ${installDir})..."
        echo "$installDir" > "$(dirname "$0")/jar"

else
    echo "AstroAllInOne not found!"
    exit 4
fi

echo "Loading installed drivers list..."
find /usr/bin -name indi_* -perm /u+x -type f > "$(dirname "$0")/drivers"

echo "Starting Control Panel..."
java -jar "$installDir" "--control-panel" "--install-dir=$(dirname ${installDir})"

echo "Starting driver..."
indiserver "$(dirname "$0")/driver.sh"
close