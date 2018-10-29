#!/bin/bash
function build() {
    cd $1
    chown -R root:root AstroAllInOne/
    echo "Invoking dpkg-deb..."
    dpkg-deb --build AstroAllInOne
    chown -R ${2}:${2} AstroAllInOne/
    chown -R ${2}:${2} AstroAllInOne.deb
    DISPLAY=$3 zenity --question --title="AstroAllInOne" --text="Package created. Install it now?" --width=250 2>/dev/null
    if [ "$?" == "0" ]; then
        dpkg -i ./AstroAllInOne.deb
    fi
}

cd "$(dirname "$0")"
echo "Cleaning..."
rm ./AstroAllInOne.deb 2> /dev/null
echo "Copying files..."
cp ../out/artifacts/AstroAllInOne_jar/AstroAllInOne.jar AstroAllInOne/usr/share/AstroAllInOne/AstroAllInOne.jar
cp ../logo.png AstroAllInOne/usr/share/AstroAllInOne/logo.png
echo "Setting permissions..."
chmod +x AstroAllInOne/usr/share/applications/AstroAllInOne.desktop
chmod +x AstroAllInOne/usr/bin/astroallinone
xhost local:root
if [ -n "$(which gksudo)" ]; then
    gksudo bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

elif [ -n "$(which kdesudo)" ]; then
    kdesudo bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

elif [ -n "$(which pkexec)" ]; then
    pkexec bash -c "$(declare -f build); build \"$(pwd)\" \"${USER}\" \"${DISPLAY}\""

else
    sudo chown -R root:root AstroAllInOne/
    echo "Invoking dpkg-deb..."
    dpkg-deb --build AstroAllInOne
    sudo chown -R ${USER}:${USER} AstroAllInOne/
    sudo chown -R ${USER}:${USER} AstroAllInOne.deb
    read -p "Do you want to install the deb file now (y/n)? " re
    if [ "$re" == "y" ]; then
        dpkg -i ./AstroAllInOne.deb
    fi
fi