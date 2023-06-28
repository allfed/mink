#!/bin/bash

# average a series of rasters and save the result

# $1: raster to average/max
# $2: mask, null if average, defined where want to max
# $3: output raster name
# $4:location to save


# echo ""
# echo "running the average..."


# *NOTES on null values*
# No-data (NULL) handling
# https://grass.osgeo.org/grass82/manuals/r.series.html
# Without -n flag, the complete list of inputs for each cell (including NULLs) is passed to the aggregate function. Individual aggregates can handle data as they choose. Mostly, they just compute the aggregate over the non-NULL values, producing a NULL result only if all inputs are NULL. 
r.series --overwrite input=$1 output="deleteme_avg" method=average --quiet #2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"

r.series --overwrite input=$1 output="deleteme_max" method=maximum

r.mapcalc $3 = "if(isnull($2),deleteme_avg,deleteme_max)"


cd $4

# echo ""
# echo "running the save..."
# if you want to save the result as ascii, you could uncomment this line below
r.out.ascii input=$3 output=- --quiet > $3.asc
