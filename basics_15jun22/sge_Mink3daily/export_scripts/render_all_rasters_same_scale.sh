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
  output_lines=$(r.univar -g $raster | wc -l)
  if [ "$output_lines " -eq 0 ]; then
    echo "Display Error: the raster $raster is composed entirely of NULL values in current region."
    echo "Or, there is only one raster cell to take statistics on."
    echo "The problem is, we can't get statistics so we can't generate the combined raster."
    echo "Region:"
    g.region -p
    exit 0
  fi

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
  output_lines=$(r.univar -g $raster | wc -l)
  if [ "$output_lines " -eq 0 ]; then
    echo "Display Error: the raster $raster is composed entirely of NULL values in current region."
    echo "Or, there is only one raster cell to take statistics on."
    echo "The problem is, we can't get statistics so we can't generate the combined raster."
    echo "Region:"
    g.region -p
    exit 0
  fi


  min=`echo "$stats" | grep min= | cut -d= -f2`
  max=`echo "$stats" | grep max= | cut -d= -f2`
  
  # call the quick_display script to display the raster
  ./quick_display.sh $raster $results_folder $overall_max_int $overall_min_int
done