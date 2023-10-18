#!/bin/bash
# for the purpose of exporting GRASS masked raster data (tif is useful for python import)
# usage: ./export_masked_tifs.sh raster_to_export

set -e

if [ $# -lt 2 ]; then
  echo "Usage: $0 folder_to_save raster_to_save "
  echo " folder_to_save is relative to the git_root folder (the mink/ directory)"
  echo " For example:"
  echo " $0 . nice_raster"
  echo " saves the ascii folder in mink/ (the git root) as nice_raster.asc"
  echo " $0 wth_control nice_raster"
  echo " saves the ascii folder in the mink/wth_control/ folder as nice_raster.asc"
  echo " You can also specify an absolute folder with a folder_to_save starting with \"/\"."
  exit 1
fi

. ../default_paths_etc.sh # import git_root

folder=$1
raster=$2

#  save the result as ascii
r.out.ascii input=$raster output=- > $raster.asc

if [[ $folder == /* ]]; then
  location="${folder}"
elif [[ -z $folder ]]; then
  location="$git_root/"
else
  location="$git_root/${folder}"
fi

print_location="${location%/}"
echo "save loc ascii:"
echo "$print_location/$raster.asc"
mv $raster.asc $location
