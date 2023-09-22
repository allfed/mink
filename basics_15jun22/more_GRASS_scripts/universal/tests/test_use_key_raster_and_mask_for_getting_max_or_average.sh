#!/bin/bash

set -e

cd .. # change directory to where the scripts are defined

# Define paths and names for sample rasters
key_rasters="test_key_raster1,test_key_raster2"
value_rasters="test_value_raster1,test_value_raster2"
mask="test_mask"
output_raster="test_output_raster"
script_to_test="use_key_raster_and_mask_for_getting_max_or_average.sh"

# 1. Setup

nrows=$(g.region -g | grep "^rows=" | cut -d'=' -f2)
echo "nrows"
echo $nrows

# Creating dummy key rasters (planting months) for testing using r.mapcalc
r.mapcalc "test_key_raster1 = if(col() % 2 == 0, 2, 3)"
r.mapcalc "test_key_raster2 = if(col() % 2 == 0, 4, 5)"

# expect average of key raster to be 3 for even, 4 for odd.

# Creating dummy value rasters (yields) for testing
r.mapcalc "test_value_raster1 = if(row() % 3 == 0, 20, 15)"
r.mapcalc "test_value_raster2 = if(row() % 3 == 0, 15, 20)"

# Creating a mask with null values in the middle of the region
r.mapcalc "test_mask = if(row() > ($nrows/3) && row() < 2*($nrows/3), null(), 1)"

# 2. Test Execution

bash $script_to_test $key_rasters $mask $value_rasters $output_raster

# 3. Validation

# For areas where the mask is null, we expect the average of key rasters. 
# For areas where the mask is not null, we expect the key raster where the value raster is max.

r.mapcalc "expected_output = if(isnull($mask), round((test_key_raster1 + test_key_raster2)/2.0), if(test_value_raster1 > test_value_raster2, test_key_raster1, test_key_raster2))"

# Generate a difference raster
r.mapcalc "difference = if($output_raster != expected_output, 1, 0)"

# Compute statistics on the difference raster
sum_value=$(r.univar -g map=difference | grep '^sum=' | cut -d'=' -f2)

if [[ $sum_value -eq 0 ]]; then
    echo "Test passed: The output raster matches expected values."
else
    echo "Test failed: The output raster doesn't match expected values."
    exit 1
fi

# 4. Cleanup
# Remove the test rasters and the expected output raster
g.remove -f rast=$(echo $key_rasters,$value_rasters,$mask,$output_raster,expected_output,difference | tr "," ",") --quiet

echo "Test completed."
