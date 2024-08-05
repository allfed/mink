#!/bin/bash

# mask any raster that will be run by available weather files.

initial_mask=$1
final_mask=$2

#don't print any progress bar
export GRASS_VERBOSE=0

# mask using available weather files:
r.mapcalc "$final_mask = if(isnull(WTH_file_mask), null(),${initial_mask})"
echo "final_mask:"
echo $final_mask
/mnt/data/basics_15jun22/sge_Mink3daily/export_scripts/quick_display.sh $final_mask .
echo "initial_mask:"
echo $initial_mask
/mnt/data/basics_15jun22/sge_Mink3daily/export_scripts/quick_display.sh $initial_mask .