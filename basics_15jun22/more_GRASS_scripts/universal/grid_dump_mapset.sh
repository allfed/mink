#!/bin/bash

# This is a script to dump every map in a mapset to a simple graphics file.

mapset=$1
output_directory=$2
search_pattern="$3"
width_height_pair="$4"
region_type="$5"

if [ $# -lt 2 ]; then

  echo ""
  echo "Usage: $0 mapset output_directory [\"search pattern\"] [width,height] [this|zoom]"
  echo ""
  echo "The search should be in quotation marks. If you want the current"
  echo "mapset, then the first argument should consist of open-closed"
  echo "quotation marks with no content. To use the width/height pair, a pattern must be"
  echo "supplied. For all rasters, use an empty pattern. The final optional control regards"
  echo "which region to use. The default is to use the raster's extent/resolution. Zoom will"
  echo "use the raster's native extent/resolution except it will also employ g.region zoom=asdf"
  echo "while \"this\" will keep the current region as set prior to running the script."
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

# Specifications for map display, using the default settings

d.mon stop=PNG

# need a for loop that loops over all the rasters in the 
# raster list

if [ -n "$width_height_pair" ]; then
  export  GRASS_WIDTH=`echo "$width_height_pair" | cut -d, -f1`
  export GRASS_HEIGHT=`echo "$width_height_pair" | cut -d, -f2`
fi

    export GRASS_TRUECOLOR=TRUE
    export GRASS_PNG_COMPRESSION=9

for raster in $raster_list
do
  export GRASS_PNGFILE=$output_directory/${raster}_at_${mapset}.png

  if [ -z "$region_type" ]; then
    g.region rast=$raster@$mapset
  elif [ "$region_type" = zoom ]; then
    g.region rast=$raster@$mapset
    g.region zoom=$raster@$mapset
  elif [ "$region_type" != this ]; then
    echo "Bad region option; you supplied [$region_type], it must be blank, zoom, or this."
    exit 1
  fi

  d.mon start=PNG

  d.rast $raster@$mapset
  d.vect cntry05 type=boundary color=gray

  # first change since feb 4, 2011 (30sep15)
  raster_type=`r.info -t $raster@$mapset | cut -d= -f2`
  if [ $raster_type = "CELL" ]; then
    n_categories=`r.category $raster@$mapset | wc -l`

    if [ $n_categories -lt 12 ]; then
      eval d.legend map=$raster@$mapset $legend_options at=1,40,2,5
    else
      # make a bigger legend
      eval d.legend map=$raster@$mapset $legend_options at=1,80,2,10
    fi
  else
    # not a categorical map
      eval d.legend map=$raster@$mapset $legend_options at=1,40,2,5
  fi

#  d.legend $raster@$mapset at=1,40,1,5 # -s

echo \
".C black
.S 3.0
$raster@$mapset"\
| d.text at=98,95 align=lr

# and now, how about a lat/long grid to orient ourselves
# i want to default to something reasonable because i am too lazy
# to allow for it to be user inputed.
#
# i think i will just say that i want a certain number of them left/right
# and use that as the square resolution

west_side=`g.region -g | grep w= | cut -d= -f2`
east_side=`g.region -g | grep e= | cut -d= -f2`

n_boxes=15
box_width=`echo "scale=15; ($east_side - $west_side) / $n_boxes" | bc`

d.grid size=$box_width

  d.mon stop=PNG
done


