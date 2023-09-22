#!/bin/bash

set -e

cd .. # change directory to where the scripts are defined

# Define paths and names for sample rasters
key_raster="test_key_raster"
value_raster1="test_value_raster1"
value_raster2="test_value_raster2"
output_combined_raster="test_output_combined_raster"
script_to_test="use_key_raster_to_map_to_value_raster.sh"

# 1. Setup

# Creating dummy rasters for testing using r.mapcalc
r.mapcalc "$key_raster = if(row() % 2 == 0, 0, 1)"
r.mapcalc "$value_raster1 = 10"
r.mapcalc "$value_raster2 = 20"

# 2. Test Execution

bash $script_to_test $key_raster "$value_raster1,$value_raster2" $output_combined_raster

# 3. Validation

# Now, let's validate. For each cell:
# If key_raster is 1, output_combined_raster should be 10.
# If key_raster is 2, output_combined_raster should be 20.
# We'll generate a raster of differences and check if there are any non-zero values.

r.mapcalc "difference = if($key_raster == 0 && $output_combined_raster != 10, 1, if($key_raster == 1 && $output_combined_raster != 20, 1, 0))"

# Compute statistics on the difference raster
# r.univar -g map=difference
# stats=$(r.stats -n -c input=difference)
# # r.univar 
# echo stats
# echo $stats
sum_value=$(r.univar -g map=difference | grep '^sum=' | cut -d'=' -f2)
# $stats
if [[ $sum_value -eq 0 ]]; then
    echo "Test passed: The output raster has expected values."
else
    echo "Test failed: The output raster doesn't match expected values."
    exit 1
fi

# 4. Cleanup
# Remove the test rasters
g.remove -f rast=$key_raster,$value_raster1,$value_raster2,$output_combined_raster,difference --quiet

echo "Test completed."
