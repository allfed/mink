#!/bin/bash

# sum a series of rasters and save the result
# arg1: all the rasters separated by commas
# arg2:location to save and name of output raster
# arg3: folder to move to for saving results

r.series --overwrite input=$1 output=$2 method=sum

cd $3

#  save the result as ascii
r.out.ascii input=$2 output=- > $2.asc
