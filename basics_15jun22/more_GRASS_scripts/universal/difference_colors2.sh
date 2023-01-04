#!/bin/bash

# this is to do some sensible colors for difference or error maps centered on zero

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster_to_color [classic|classic_wide|cream] [min,max,std] [steps]"
  echo ""
  echo "default is classic. classic_wide will make the central black"
  echo "area wider (more values fall into \"we don't care\")."
  echo "cream gives an alternate \"we don't care\" color and different"
  echo "order for the negative colors. steps means clean transitions between values"
  exit 2
fi

raster=$1
second_arg=$2
 third_arg=$3
fourth_arg=$4

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

  stats=`r.univar $raster -g`
  real_min=`echo "$stats" | grep min= | cut -d= -f2`
  real_max=`echo "$stats" | grep max= | cut -d= -f2`
  real_extreme=`echo "scale=15 ; amax = sqrt($real_max * $real_max) ; amin = sqrt($real_min * $real_min) ; if(amax > amin) {amax} else {amin}" | bc`
fi

extreme=`echo "scale=15 ; amax = sqrt($max * $max) ; amin = sqrt($min * $min) ; if(amax > amin) {amax} else {amin}" | bc`

#echo "std = [$std]"
two_std=`echo "if ($std * 2 < $extreme) {$std * 2} else {$std}" | bc`

if [ $extreme = 0 ]; then
  real_extreme=0.0004
  extreme=0.0003
  two_std=0.0002
      std=0.0001
fi


a_hair=0.0000001 # Beware the MAGIC NUMBER!!!

if [ -z "$fourth_arg" ]; then
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
"-$extreme yellow
-$two_std red
-$std 255:96:255
0 255:236:184
$std green
$two_std cyan
$extreme blue"

else

    std_minus_hair=`echo "$std     - $a_hair" | bc`
two_std_minus_hair=`echo "$two_std - $a_hair" | bc`
extreme_minus_hair=`echo "$extreme - $a_hair" | bc`

classic_color_string=\
"-$real_extreme yellow
-$extreme yellow
-$extreme_minus_hair magenta
-$two_std magenta
-$two_std_minus_hair red
-$std red
-$std_minus_hair black
0 black
$std_minus_hair black
$std green
$two_std_minus_hair green
$two_std cyan
$extreme_minus_hair cyan
$extreme blue
$real_extreme blue"

wide_color_string=\
"-$real_extreme yellow
-$extreme yellow
-$extreme_minus_hair magenta
-$two_std magenta
-$two_std_minus_hair red
-$std red
-$std_minus_hair black
0 black
$std_minus_hair black
$std green
$two_std_minus_hair green
$two_std cyan
$extreme_minus_hair cyan
$extreme blue
$real_extreme blue"

#0 211:167:123
#0 255:218:113
cream_color_string=\
"-$real_extreme yellow
-$extreme yellow
-$extreme_minus_hair red
-$two_std red
-$two_std_minus_hair 255:96:255
-$std 255:96:255
-$std_minus_hair 255:236:184
0 255:236:184
$std_minus_hair 255:236:184
$std green
$two_std_minus_hair green
$two_std cyan
$extreme_minus_hair cyan
$extreme blue
$real_extreme blue"


fi






if [ -z "$color_scheme" -o "$color_scheme" = "classic" ]; then
  echo "$classic_color_string" | r.colors $raster color=rules
elif [ "$color_scheme" = "classic_wide" ]; then
  echo "$wide_color_string"    | r.colors $raster color=rules
elif [ "$color_scheme" = "cream" ]; then
#  echo "$cream_color_string"
  echo "$cream_color_string"   | r.colors $raster color=rules
else
  echo "unrecognized color scheme: [$color_scheme]"
fi



