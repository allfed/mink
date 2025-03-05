#!/bin/bash
# for the purpose of exporting GRASS masked raster data (tif is useful for python import)
# usage: ./export_masked_tifs.sh raster_to_export

set -e

if [ $# -lt 1 ]; then
  echo "Usage: $0 folder_to_create "
  exit 1
fi

. ../default_paths_etc.sh # import git_root

folder=$1

location="$git_root/${folder}"

#  save the result as ascii
echo "mkdir -p $location"

mkdir -p "$location"
