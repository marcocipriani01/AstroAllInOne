#!/bin/bash
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
        sudo apt-get install -y socat
        echo "Done."

    else
        exit 4
    fi
fi
if [ -z "$(which indiserver)" ]; then
    echo "INDI not found!"
    exit 5
fi

installDir="$(dirname "$0")/../out/artifacts/AstroAllInOne_jar/AstroAllInOne.jar"
if [ ! -f "$installDir" ]; then
    echo "AstroAllInOne.jar not found! Please build the module first!"
    exit 6
fi

dataDir="$HOME/.config/AstroAllInOne"
mkdir -p "$dataDir"
java -jar "$installDir" "--settings=$dataDir" "$1" "-v"