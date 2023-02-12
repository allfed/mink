#!/bin/bash
crop_caps=$1
overall_yield_raster=$2
results_folder=$3

historical="${crop_caps}_yield"

./save_ascii.sh $overall_yield_raster 
./save_ascii.sh $historical

cp "${overall_yield_raster}.asc" ../../../$3
cp "${historical}.asc" ../../../$3
