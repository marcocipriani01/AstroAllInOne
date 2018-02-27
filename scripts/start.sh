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

echo "Starting AstroAllInOne..."
if [ -z "$1" ]; then
    echo "$(dirname "$0")/AstroAllInOne.jar" > "$(dirname "$0")/jar"

else
    echo "$1" > "$(dirname "$0")/jar"
fi
indiserver "$(dirname "$0")/driver.sh"