#!/bin/bash


# This script is designed to sum multiple high-resolution raster datasets into a single high-resolution raster.
# It is particularly useful in geographic information systems (GIS) where handling of large spatial data is required.
#
# The script takes three arguments:
# 1. A comma-separated list of high-resolution raster names.
# 2. The name of a low-resolution region which will be used for setting the boundary extents.
# 3. The name of the output raster where the summed high-resolution raster will be saved.
#
# The script operates by first setting the region to the low-resolution extents, then adjusting it to match
# the high-resolution of the first raster in the input list. It ensures the boundary extents remain consistent
# with the low-resolution region. The high-resolution rasters are then summed, and the result is saved in the
# specified output raster file.
#
# Error handling is incorporated to ensure that the region is reset to the original low-resolution settings in
# case of any errors during execution. This is achieved through a cleanup function that is called on script exit
# or error.
#
# Usage:
# ./script_name.sh <comma_separated_highres_rasters> <lowres_region_name> <summed_highres_raster>
# Example:
# ./script_name.sh WHEA_cropland_highres,MAIZ_cropland_highres temp_lowres_region


set -e # exit on error

if [ $# -ne 3 ]; then
  echo "Function is to sum several rasters at high resolution raster"
  echo "Usage: $0 <comma_separated_highres_rasters> <lowres_region_name> <summed_highres_raster>"
  echo " Example usages:"
  echo " $0 WHEA_cropland_highres,MAIZ_cropland_highres temp_lowres_region"
  exit 1
fi

comma_separated_highres_rasters=$1
lowres_region_name=$2
summed_highres_raster=$3
first_highres_raster_name=$(echo "$comma_separated_highres_rasters" | cut -d',' -f1)


# just in case we error out, don't want to have a wierd resolution after
cleanup() {
    g.region region=$lowres_region_name
    echo "reset to original region"
}

# on exit or error, cleanup back to original low res region
trap cleanup EXIT ERR




g.region region=$lowres_region_name

# Save the north, south, east, west boundaries and resolutions for lowres
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# set region (including resolution and boundaries) to the high resolution raster
g.region rast=$first_highres_raster_name

# set the north, south, east, west boundaries back to the original low res boundaries
g.region n=$n s=$s w=$w e=$e

# get the high resolution raster pixel sizes
read nsres_highres ewres_highres <<< $(r.info -gs $first_highres_raster_name | awk -F'=' '$1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# the actual summation
r.series --overwrite --quiet input=$comma_separated_highres_rasters output=$summed_highres_raster method=sum 

# move back to low res
g.region region=$lowres_region_name

echo "successfully summed highres raster saved to $summed_highres_raster raster"
