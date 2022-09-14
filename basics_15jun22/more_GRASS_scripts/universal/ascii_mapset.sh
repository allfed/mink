#!/bin/bash

# This is a script to dump every map in a mapset to a simple graphics file.

mapset=$1
output_directory=$2
search_pattern="$3"
region_type="$4"

if [ $# -lt 2 ]; then

  echo ""
  echo "Usage: $0 mapset output_directory [\"search pattern\"] [this|zoom]"
  echo ""
  echo "The search should be in quotation marks. If you want the current"
  echo "mapset, then the first argument should consist of open-closed"
  echo "quotation marks with no content. For all rasters, use an empty pattern. The final optional control regards"
  echo "which region to use. The default is to use the raster's extent/resolution. Zoom will"
  echo "use the raster's native extent/resolution except it will also employ g.region zoom=asdf"
  echo "while \"this\" will keep the current region as set prior to running the script. A pattern must be used to get to this|zoom."
  echo ""

  exit 1
fi

# Check for empty mapset

if [ -z "$mapset" ]; then

  mapset=`g.gisenv get=MAPSET`

fi

# Make sure the output directory exists.

mkdir -p $output_directory

# Get list of rasters
if [ -n "$search_pattern" ]; then
  raster_list=`g.mlist rast mapset=$mapset pattern="$search_pattern"`
else
  raster_list=`g.mlist rast mapset=$mapset`
fi

# need a for loop that loops over all the rasters in the 
# raster list


for raster in $raster_list
do

  if [ -z "$region_type" ]; then
    g.region rast=$raster@$mapset
  elif [ "$region_type" = zoom ]; then
    g.region rast=$raster@$mapset
    g.region zoom=$raster@$mapset
  elif [ "$region_type" != this ]; then
    echo "Bad region option; you supplied [$region_type], it must be blank, zoom, or this."
    exit 1
  fi

  echo " -- exporting $raster@$mapset start at `date` --"
  r.out.arc input=$raster@$mapset output=${output_directory}/${raster}_at_$mapset.asc

done


