#!/bin/bash

# average a series of rasters and save the result

# arg0: rasters
# arg1:location to save


# echo ""
# echo "running the average..."


# *NOTES on null values*
# No-data (NULL) handling
# https://grass.osgeo.org/grass82/manuals/r.series.html
# Without -n flag, the complete list of inputs for each cell (including NULLs) is passed to the aggregate function. Individual aggregates can handle data as they choose. Mostly, they just compute the aggregate over the non-NULL values, producing a NULL result only if all inputs are NULL. 
r.series --overwrite input=$1 output=$2 method=average --quiet #2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"


cd $3

# echo ""
# echo "running the save..."
# if you want to save the result as ascii, you could uncomment this line below
r.out.ascii input=$2 output=- --quiet > $2.asc
