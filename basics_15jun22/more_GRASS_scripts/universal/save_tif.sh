#!/bin/bash
# for the purpose of exporting existing masked data for use in python analysis
# usage: ./export_masked_tifs.sh raster_to_export save_folder

raster=$1
folder=$2

r.out.gdal input=$raster output=$raster.tif format=GTiff --quiet

location="/mnt/data/"

if [[ -z $folder ]]; then
  location="/mnt/data/"
else
  location="/mnt/data/${folder}"
fi

echo "save loc:"
echo "$location"

mv $raster.tif $location

