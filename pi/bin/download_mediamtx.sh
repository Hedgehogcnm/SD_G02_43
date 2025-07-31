#!/bin/bash
#This is a bash script file to install mediamtx and modify the mediamtx.yml file content 
#Version and raspi architecture
VERSION=1.13.1
ARCH="linux_arm64"

#Download and extract
wget -c https://github.com/bluenviron/mediamtx/releases/download/$VERSION/mediamtx_${VERSION}_${ARCH}.tar.gz
tar -xzf mediamtx_${VERSION}_${ARCH}.tar.gz
rm mediamtx_${VERSION}_${ARCH}.tar.gz

#Copy the content from our configured mediamtx.yml file to the one we installed
cp ../config/mediamtx.yml mediamtx.yml

echo "Mediamtx setup complete. Run it with: ./mediamtx" 