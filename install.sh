#!/bin/bash
# Build process for the mink docker container

echo "Building GRASS GIS Docker container..."
cd GRASS_program/grass
docker build -t grassgis .
echo "" && echo "GRASS GIS Docker container built"

echo "" && echo "Builing mink Docker container"
cd ../../
docker build -t mink .

echo "" && echo "Mink Docker container built. To build the singularity container, run 'build_singularity.sh'"
