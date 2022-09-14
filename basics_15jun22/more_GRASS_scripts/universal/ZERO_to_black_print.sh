#!/bin/bash

# this is to do some sensible colors for difference or error maps centered on zero

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster_to_color [max_value_to_use] [min_value_to_use]"
  echo ""
  echo "with no bounds or only a max bound, zero is set to black and negative values get shades of gray"
  echo "with both bounds, this just forces a rainbow between the bounds"
  echo "THIS PRINTS OUT THE COLOR RULES BUT DOES NOT REASSIGN THE COLORS"
  exit 2
fi

raster=$1
max_value_to_use=$2
min_value_to_use=$3

# check to see if the desired raster even exists
raster_test=`g.mlist rast pat=$raster`

if [ -z "$raster_test" ]; then
  echo "$0: raster <$raster> not found"
  exit
fi

magic_overshoot_fraction=0.001
if [ -z "$max_value_to_use" ]; then
  stats=`r.univar $raster -g`
  temp_max=`echo "$stats" | grep max= | cut -d= -f2`
  max=`echo "$temp_max * (1 + $magic_overshoot_fraction)" | bc`
  temp_min=`echo "$stats" | grep min= | cut -d= -f2`
  min=`echo "$temp_min * (1 + $magic_overshoot_fraction)" | bc`
else
  max=$max_value_to_use
  if [ -z "$min_value_to_use" ]; then
    stats=`r.univar $raster -g`
    temp_min=`echo "$stats" | grep min= | cut -d= -f2`
    min=`echo "$temp_min * (1 + $magic_overshoot_fraction)" | bc`
  else
    temp_min=$min_value_to_use
    min=$min_value_to_use
  fi
fi



color_list=\
"yellow
green
cyan
blue
magenta
red"

junk=\
"red
magenta
blue
cyan
green
yellow"

small_num=0.0001


# we need to test whether we want to use black for
# zero or not.
# we want to do so when there is no minimum specified
# we want to do so when the minimum is specified, but is zero

if [ -z "$min_value_to_use" ]; then
  use_zero_as_black=1
else
  test_for_min_is_zero=`echo "if($min_value_to_use == 0) {1} else {0}" | bc`
  if [ $test_for_min_is_zero = 1 ]; then
    # the specified minimum is zero, so we want to use black
    use_zero_as_black=1
  else
    # the specified minimum is non-zero, so we want to use the bottome of our list of colors
    use_zero_as_black=0
  fi
fi


if [ $use_zero_as_black = 1 ]; then

if [ $max = 0 ]; then
  min_is_below_zero=`echo "if ($temp_min < 0) {1} else {0}" | bc`
  if [ $min_is_below_zero -eq 1 ]; then

color_string=\
"$min 128:128:128
0 black
$small_num yellow"

else
  color_string="0 black"
fi # min is below zero
else

min_is_below_zero=`echo "if ($temp_min < 0) {1} else {0}" | bc`

#echo "min below zero = [$min_is_below_zero]; temp_min = [$temp_min]"
#echo "ZZZZ"

if [ $min_is_below_zero -eq 1 ]; then

color_string=\
"$min 128:128:128
0 black
$small_num yellow"

else

color_string=\
"-$small_num black
0 black
$small_num yellow"

fi

for (( line=2 ; line<=6 ; line++ ))
do
  color=`echo "$color_list" | sed -n "${line}p"`

  value=`echo "scale = 5 ; $max * ($line - 1) / 5" | bc`


color_string="${color_string}
$value $color"

done
fi # if max = 0

else # if min_value_to_use is empty

color_string=""
for (( line=1 ; line<=6 ; line++ ))
do
  color=`echo "$color_list" | sed -n "${line}p"`

  value=`echo "scale = 5 ; ($max - $min) * ($line - 1) / 5 + $min" | bc`


color_string="${color_string}
$value $color"

done

fi # else of if min_value_to_use is empty





#echo "[$color_string]"
#echo "temp_max = $temp_max ; functional max = $max"
echo "$color_string"

