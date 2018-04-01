#!/bin/bash
cd "$(dirname "$0")"
rm ./AstroAllInOne.deb 2> /dev/null
cp ../out/artifacts/AstroAllInOne_jar/AstroAllInOne.jar AstroAllInOne/usr/share/AstroAllInOne/AstroAllInOne.jar
cp ../logo.png AstroAllInOne/usr/share/AstroAllInOne/logo.png
chmod +x AstroAllInOne/usr/share/applications/AstroAllInOne.desktop
chmod +x AstroAllInOne/usr/bin/astroallinone
sudo chown -R root:root AstroAllInOne/
dpkg-deb --build AstroAllInOne
sudo chown -R ${USER}:${USER} AstroAllInOne/
read -p "Do you want to install the deb file now (y/n)? " re
if [ "$re" == "y" ]; then
    sudo dpkg -i ./AstroAllInOne.deb
fi