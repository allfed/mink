#!/bin/bash

# divide two rasters
# arg1: input raster 1
# arg2: input raster 2
# arg3: output production
# echo "dividing rasters"
# echo "dividing $1"
# echo "by $2"
# echo "to get $3"
r.mapcalc "$3 = $1 / $2" #2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"
