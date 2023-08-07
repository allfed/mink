#!/bin/bash
# for the purpose of exporting GRASS masked raster data for use in python analysis
# usage: ./export_masked_tifs.sh raster_to_export

. ../default_paths_etc.sh # import git_root

if [ $# -lt 2 ]; then
  echo "Usage: $0 raster_to_save folder_to_save ..."
  exit 1
fi

raster=$1
folder=$2

r.out.gdal input=$raster output=$raster.tif format=GTiff type=Float32 --quiet --overwrite

if [[ $folder == /* ]]; then
  location="${folder}"
elif [[ -z $folder ]]; then
  location="$git_root/"
else
  location="$git_root/${folder}"
fi

echo ""
echo "save loc tif:"
echo "$location$raster.tif"
echo ""

mv $raster.tif $location

