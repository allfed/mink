#!/bin/bash

# this is to do some sensible colors for difference or error maps centered on zero

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster_to_color [classic|classic_wide|cream] [min,max,std]"
  echo ""
  echo "default is classic. classic_wide will make the central black"
  echo "area wider (more values fall into \"we don't care\")."
  echo "cream gives an alternate \"we don't care\" color and different"
  echo "order for the negative colors."
  exit 2
fi

raster=$1
second_arg=$2
 third_arg=$3

if [ -z "`echo "$second_arg" | grep ,`" ]; then
  # second argument has no commas, assume it is the color scheme
  color_scheme=$second_arg
   min_max_std=$third_arg
else
  # second argument has commas, assume it is the min/max/std
   min_max_std=$second_arg
  color_scheme=$third_arg
fi

if [ -z "$min_max_std" ]; then
  # figure out the stats for ourselves
  stats=`r.univar $raster -g`
  std=`echo "$stats" | grep stddev= | cut -d= -f2`
  min=`echo "$stats" | grep min= | cut -d= -f2`
  max=`echo "$stats" | grep max= | cut -d= -f2`
else
  # use the provided values
  min=`echo "$min_max_std" | cut -d, -f1`
  max=`echo "$min_max_std" | cut -d, -f2`
  std=`echo "$min_max_std" | cut -d, -f3`
fi

#extreme=`echo "scale=15 ; amax = sqrt($max * $max) ; amin = sqrt($min * $min) ; if(amax > amin) {amax} else {amin}" | bc`
extreme=`echo "scale=15 ; stretch=1.001 ; amax = sqrt($max * $max) ; amin = sqrt($min * $min) ; if(amax > amin) {amax * stretch} else {amin * stretch}" | bc`

#echo "std = [$std]"
two_std=`echo "if ($std * 2 < $extreme) {$std * 2} else {$std}" | bc`

if [ $extreme = 0 ]; then
  extreme=0.0003
  two_std=0.0002
      std=0.0001
fi

classic_color_string=\
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

#0 211:167:123
#0 255:218:113
cream_color_string=\
"-$extreme red
-$two_std 255:96:255
-$std yellow
0 255:236:184
$std green
$two_std cyan
$extreme blue"

tiny_number=0.00001
creamzero_color_string=\
"-$extreme red
-$two_std 255:96:255
-$std yellow
-$tiny_number 255:236:184
0 40:40:40
$tiny_number 255:236:184
$std green
$two_std cyan
$extreme blue"

if [ -z "$color_scheme" -o "$color_scheme" = "classic" ]; then
  echo "$classic_color_string" | r.colors $raster color=rules
elif [ "$color_scheme" = "classic_wide" ]; then
  echo "$wide_color_string"    | r.colors $raster color=rules
elif [ "$color_scheme" = "cream" ]; then
#  echo "$cream_color_string"
  echo "$cream_color_string"   | r.colors $raster color=rules
elif [ "$color_scheme" = "creamzero" ]; then
  echo "$creamzero_color_string"   | r.colors $raster color=rules
else
  echo "unrecognized color scheme: [$color_scheme]"
fi



