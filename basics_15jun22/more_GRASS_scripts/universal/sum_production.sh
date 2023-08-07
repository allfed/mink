#!/bin/bash

# sum a series of rasters and save the result
# arg1: all the rasters separated by commas
# arg2:location to save and name of output raster
# arg3: folder to move to for saving results
# echo "rasters to add"
# echo $1

r.series --overwrite input=$1 output=$2 method=sum #2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"
