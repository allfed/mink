#!/bin/bash
crop_caps=$1
overall_yield_raster=$2
results_folder=$3

historical="${crop_caps}_yield"

./save_ascii.sh $historical
# ./save_ascii.sh $overall_yield_raster

# cp "${historical}.asc" ../../../$results_folder
echo "PWD"
echo $PWD
echo "overall asc going to"
echo ../../../$results_folder


mv "${overall_yield_raster}.asc" ../../../$results_folder
