#!/bin/bash

usage_string="
Usage: $0 input_raster_name output_raster_name absolute_threshold t|n

This takes the input raster and removes outliers that are farther from zero than
the absolute threshold, writing the result to the output raster. The final argument
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
absolute_threshold=$3
            t_or_n=$4

# get rid of case sensitivity
t_or_n=`echo "$t_or_n" | tr "[:upper:]" "[:lower:]"`

if [ "$t_or_n" = t ]; then
  extreme_value=$absolute_threshold
elif [ "$t_or_n" = n ]; then
  extreme_value="null()"
else
  echo "$0: Final arguement must be t or n"
  exit 2
fi

r.mapcalc $output_raster_name = "if($input_raster_name < -$absolute_threshold, -$extreme_value , \
                                    if($input_raster_name > $absolute_threshold, $extreme_value, $input_raster_name) )"





