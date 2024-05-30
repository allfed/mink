#!/bin/bash

r.mapcalc "one_pixel1 = 41"
r.mapcalc "one_pixel2 = 42"
r.mapcalc "one_pixel3 = 42"

# Combine rasters to average
rasters="one_pixel1,one_pixel2,one_pixel3"

# Minimum value to consider (e.g., 0)
min_value=0

# Method of averaging (mode in this case)
method="mode"

# Output raster for the average
output_raster="average_raster"
cd ..
# Call the averaging script
./average_rasters.sh "$rasters" "$min_value" "$method" "$output_raster"

# Check the result
r.info $output_raster
r.univar $output_raster
cd -
