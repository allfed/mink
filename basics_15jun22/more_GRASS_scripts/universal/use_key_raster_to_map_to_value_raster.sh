#!/bin/bash

set -e 

# use key value raster to assign the output raster the values from a list of value rasters

# arg0: key raster
# arg1: list of value rasters
# arg2: output combined raster
key_raster=$1
IFS=',' read -ra value_rasters_array <<< "$2"
output_combined_raster=$3

# Initialize r.mapcalc expression with an empty string
mapcalc_expression="$output_combined_raster = "

# Loop through the value rasters and construct the expression
for index in "${!value_rasters_array[@]}"; do
    if [[ $index -eq 0 ]]; then
        mapcalc_expression+="if($key_raster == $index, ${value_rasters_array[index]}"
    else
        mapcalc_expression+=", if($key_raster == $index, ${value_rasters_array[index]}"
    fi
done

# Close all the if-statements
for index in "${!value_rasters_array[@]}"; do
    mapcalc_expression+=")"
done

# Execute r.mapcalc with the constructed expression
# echo "r.mapcalc attempted key value expression!"
# echo "r.mapcalc \"$mapcalc_expression\""
# exit 1
eval "r.mapcalc \"$mapcalc_expression\""
