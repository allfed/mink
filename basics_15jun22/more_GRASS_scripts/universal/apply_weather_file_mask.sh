#!/bin/bash

# mask any raster that will be run by available weather files.

initial_mask=$1
final_mask=$2

echo "initial_mask"
echo "final_mask"
echo $initial_mask
echo $final_mask

#don't print any progress bar
# export GRASS_VERBOSE=0

# mask using available weather files:
r.mapcalc "$final_mask = if(isnull(WTH_file_mask), null(),${initial_mask})"
