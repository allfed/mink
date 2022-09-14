#!/bin/bash

# this is to do some sensible colors for difference or error maps centered on zero

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster_to_color wide_colors_if_present (meaning, black for +/- 1 std"
  exit 2
fi

raster=$1
use_wide_colors=$2

stats=`r.univar $raster -g`
std=`echo "$stats" | grep stddev= | cut -d= -f2`
min=`echo "$stats" | grep min= | cut -d= -f2`
max=`echo "$stats" | grep max= | cut -d= -f2`

magic_stretcher=1.0000001

extreme=`echo "scale=15 ; amax = sqrt($max * $max) ; amin = sqrt($min * $min) ; if(amax > amin) {amax * $magic_stretcher} else {amin * $magic_stretcher}" | bc`

#echo "std = [$std]"
two_std=`echo "$std * 2" | bc`

if [ $extreme = 0 ]; then
  extreme=0.0003
  two_std=0.0002
      std=0.0001
fi

color_string=\
"-$extreme yellow
-$two_std magenta
-$std red
0 black
$std green
$two_std cyan
$extreme blue"

wide_color_string=\
"-$extreme yellow
-$two_std red
-$std black
0 black
$std black
$two_std green
$extreme blue"

if [ -n "$use_wide_colors" ]; then
  echo "$wide_color_string" | r.colors $raster color=rules
else
  echo "$color_string" | r.colors $raster color=rules
fi



