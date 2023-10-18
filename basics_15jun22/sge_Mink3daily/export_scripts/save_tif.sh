#!/bin/bash
# for the purpose of exporting GRASS masked raster data for use in python analysis
# usage: ./export_masked_tifs.sh raster_to_export

. ../default_paths_etc.sh # import git_root

if [ $# -lt 2 ]; then
  echo "Usage: $0 folder_to_save raster_to_save "
  echo " folder_to_save is relative to the git_root folder (the mink/ directory)"
  echo " For example:"
  echo " $0 . nice_raster"
  echo " saves the tif folder in mink/ (the git root) as nice_raster.tif"
  echo " $0 wth_control nice_raster"
  echo " saves the tif folder in the mink/wth_control/ folder as nice_raster.tif"
  echo " You can also specify an absolute folder with a folder_to_save starting with \"/\"."
  exit 1
fi

folder=$1
raster=$2

# redirecting error out. It tends to complain about color tables, but that's unimportant. So I silenced it
# If the output failed to be created, then the next check would catch it and throw a meaning ful error
r.out.gdal input=$raster output=$raster.tif format=GTiff type=Float32 #--quiet --overwrite 2> /dev/null

if [ ! -f $raster.tif ]; then
    echo "ERROR: r.out.gdal was unable to create the tif."
    echo "get rid of the 2> /dev/null in code above to see what it says about the error."
    exit 1
fi

if [[ $folder == /* ]]; then
  location="${folder}"
elif [[ -z $folder ]]; then
  location="$git_root/"
else
  location="$git_root/${folder}"
fi

print_location="${location%/}"

echo ""
echo "save loc tif:"
echo "$print_location/$raster.tif"
echo ""

mv $raster.tif $location

