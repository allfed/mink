#!/bin/bash

# the idea here is to make a png monitor for when i don't have a functional gui...

if [ $# -lt 1 ]; then
  echo "Usage: $0 output_name [width_height]"
  echo ""
  echo "output_name is the filename (no suffix) where you want the"
  echo "  contents to be written out"
  echo ""
  echo "width_height is a comma-separated pair of integers of how"
  echo "  many pixels wide and high the image should be. If nothing"
  echo "  is supplied, the defaults will be used."
  echo ""
  echo "To actually create the image, you have to do: d.mon stop=PNG"
  exit 1
fi

# close it out in case it is already in use
d.mon stop=PNG 2>&1 | grep -v "not running"

output_name=$1
width_height=$2

dir_name=`dirname $output_name`
mkdir -pv $dir_name

# interpret the size or set defaults
if [ -z "$width_height" ]; then
   width_to_use=1000  # default width
  height_to_use=500   # default height
else
   width_to_use=`echo "$width_height" | cut -d"," -f1`
  height_to_use=`echo "$width_height" | cut -d"," -f2`
fi


export  GRASS_WIDTH=$width_to_use
export GRASS_HEIGHT=$height_to_use
export GRASS_PNGFILE=${output_name}.png
export GRASS_BACKGROUNDCOLOR=FFFFFF
export GRASS_TRUECOLOR=TRUE
export GRASS_PNG_COMPRESSION=9

d.mon start=PNG


