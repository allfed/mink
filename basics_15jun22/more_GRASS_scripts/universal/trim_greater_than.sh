#!/bin/bash

usage_string="
Usage: $0 input_raster_name output_raster_name threshold t|n

This takes the input raster and removes values that are greater than or equal to
the threshold, writing the result to the output raster. The final argument
determines how the outliers are handled: t means they are replaced by the threshold
value with the proper sign (censored at threshold); n means they are replaced by
nulls (truncated, as it were).
"

if [ $# -ne 4 ]; then
  echo "$usage_string"
  exit 1
fi

 input_raster_name=$1
output_raster_name=$2
threshold=$3
            t_or_n=$4

# get rid of case sensitivity
t_or_n=`echo "$t_or_n" | tr "[:upper:]" "[:lower:]"`

if [ "$t_or_n" = t ]; then
  extreme_value=$threshold
elif [ "$t_or_n" = n ]; then
  extreme_value="null()"
else
  echo "$0: Final arguement must be t or n"
  exit 2
fi

r.mapcalc $output_raster_name = "if($input_raster_name >= $threshold, $extreme_value , $input_raster_name)"





