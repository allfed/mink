#!/bin/bash
# for the purpose of exporting GRASS masked raster data (tif is useful for python import)
# usage: ./export_masked_tifs.sh raster_to_export

if [ $# -lt 2 ]; then
  echo "Usage: $0 raster_to_save folder_to_save ..."
  exit 1
fi

. ../default_paths_etc.sh # import git_root

raster=$1
folder=$2

#  save the result as ascii
r.out.ascii input=$raster output=- > $raster.asc


if [[ $folder == /* ]]; then
  location="${folder}"
elif [[ -z $folder ]]; then
  location="$git_root/"
else
  location="$git_root/${folder}"
fi

echo "save loc ascii:"
echo "$location$raster.asc"

mv $raster.asc $location
