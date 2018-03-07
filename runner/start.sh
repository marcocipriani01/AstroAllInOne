#!/bin/bash
close() {
    echo "Bye!"
    exit 0
}
trap close INT

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
    echo "Using AstroAllInOne in $(dirname ${installDir})..."
fi
if [ ! -f "$installDir" ]; then
    echo "AstroAllInOne not found!"
    exit 4
fi

dataDir="$HOME/.config/AstroAllInOne"
mkdir -p "$dataDir"

java -jar "$installDir" "--data-dir=$dataDir" "-p=7625"