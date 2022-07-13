#!/bin/bash
# Build process for the mink docker container
GRASS_DIR=GRASS_program

echo "Checking for local copy of GRASS"
if [ -e $GRASS_DIR/configure ]; then
    echo "Local copy of GRASS exists"
else
    echo "Download GRASS source code..."
    mkdir $GRASS_DIR && cd $GRASS_DIR
    echo "svn co https://svn.osgeo.org/grass/grass/branches/develbranch_6/ ."
    svn co https://svn.osgeo.org/grass/grass/branches/develbranch_6/ .
fi

echo "Building singularity"
mkdir mink_sandbox
sudo singularity build --sandbox mink_sandbox mink.def
