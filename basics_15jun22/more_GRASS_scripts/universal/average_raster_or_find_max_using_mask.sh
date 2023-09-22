#!/bin/bash

set -e

# average a series of rasters (or find max if mask is not null)

# $1: raster(s) to average/max
# $2: mask, null if average, defined where want to max
# $3: output raster name


# *NOTES on null values*
# No-data (NULL) handling
# https://grass.osgeo.org/grass82/manuals/r.series.html
# Without -n flag, the complete list of inputs for each cell (including NULLs) is passed to the aggregate function. Individual aggregates can handle data as they choose. Mostly, they just compute the aggregate over the non-NULL values, producing a NULL result only if all inputs are NULL. 

r.series --overwrite input=$1 output="deleteme_avg" method=average --quiet

r.series --overwrite input=$1 output="deleteme_max" method=maximum --quiet

r.mapcalc $3 = "if(isnull($2),deleteme_avg,deleteme_max)"
