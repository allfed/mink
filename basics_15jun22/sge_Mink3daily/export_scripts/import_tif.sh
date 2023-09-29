#!/bin/bash
# for example: AGMIP_princeton_RF_yield_whe_halfdegree_2005
# export days to maturity, planting dates, and yield data to GRASS

if [ $# -lt 2 ]; then
  echo "Usage: $0 folder_to_save raster_to_import"
  echo " folder_to_save is relative to the git_root folder (the mink/ directory)"
  echo " For example:"
  echo " $0.sh . nice_tiff.tif"
  echo " saves the ascii folder in mink/ (the git root) as nice_raster.asc"
  echo " $0.sh wth_control nice_tiff.tif"
  echo " saves the ascii folder in the mink/wth_control/ folder as nice_raster.asc"
  echo " You can also specify an absolute folder with a folder_to_save starting with \"/\"."
  exit 1
fi


. ../default_paths_etc.sh # import git_root

folder=$1
raster=`basename $2 .tif`


if [[ $folder == /* ]]; then
  location="${folder}"
elif [[ -z $folder ]]; then
  location="$git_root/"
else
  location="$git_root/${folder}"
fi

#  save the result as ascii
r.in.gdal -o input="$location/$raster.tif" output=$raster --overwrite --quiet
