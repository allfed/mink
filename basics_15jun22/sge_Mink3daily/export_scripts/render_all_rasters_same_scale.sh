#!/bin/bash
if [ $# -lt 2 ]; then
  echo "Usage: $0 results_folder raster1 raster2 ..."
  exit 1
fi

# declare associative arrays to store max and min float and int values for each raster
declare -A maxfloat minfloat maxint minint

# Initialize overall_max_int to a very small number
overall_max_int=-99999999999999

# Initialize overall_min_int to a very large number
overall_min_int=99999999999999

# Store the first argument in a variable
results_folder=$1 

# Shift the argument list by one, so that $1 is now the second argument from the original list, $2 is the third, etc.
shift

# loop over all input rasters
for raster in "$@"; do
  # get the max and min float values for the current raster
  maxfloat[$raster]=`r.univar -g map=$raster | grep max | awk -F "=" '{print $2}'`
  minfloat[$raster]=`r.univar -g map=$raster | grep min | awk -F "=" '{print $2}'`
  
  # convert max and min float values to integers
  maxint[$raster]=${maxfloat[$raster]%.*}
  minint[$raster]=${minfloat[$raster]%.*}
  
  # update overall_max_int and overall_min_int
  overall_max_int=$(( ${maxint[$raster]} > $overall_max_int ? ${maxint[$raster]} : $overall_max_int ))
  overall_min_int=$(( ${minint[$raster]} < $overall_min_int ? ${minint[$raster]} : $overall_min_int ))
done

# loop over all input rasters again
for raster in "$@"; do
  # limit the raster values between overall_max_int and overall_min_int
  r.mapcalc "${raster} = if($raster > $overall_max_int, $overall_max_int, $raster)"
  r.mapcalc "${raster} = if($raster < $overall_min_int, $overall_min_int, $raster)"
  
  # get the min and max values of the updated raster
  stats=`r.univar $raster -g`
  min=`echo "$stats" | grep min= | cut -d= -f2`
  max=`echo "$stats" | grep max= | cut -d= -f2`
  
  # calculate the different values for the colors
  overall_min_intplusone=`echo "scale=0; $overall_min_int + 1" | bc`
  quartermax=`echo "scale=0; $overall_max_int / 4" | bc`
  halfmax=`echo "scale=0; $overall_max_int / 2" | bc`
  threequartersmax=`echo "scale=0; ($overall_max_int * 3 + 2) / 4" | bc`

  # if overall_min is less than zero, set it to zero
  if [ "$overall_min_int" -lt 0 ]; then
    overall_min_int=0
  fi
  
  # construct the color rule string
  classic_color_string="$overall_min_int black\n$overall_min_intplusone blue\n$quartermax cyan\n$halfmax green\n$threequartersmax yellow\n$overall_max_int red"

  echo "raster"
  echo $raster
  echo "overall_max_int: $overall_max_int"
  echo "overall_min_int: $overall_min_int"
  echo "quartermax: $quartermax"
  echo "halfmax: $halfmax"
  echo "threequartersmax: $threequartersmax"
  echo "classic_color_string:"
  echo -e "$classic_color_string"
  # apply the color rule to the raster
  printf "$classic_color_string" | r.colors $raster rules=-

  # call the quick_display script to display the raster
  ./quick_display.sh $raster $results_folder $overall_max_int $overall_min_int
done