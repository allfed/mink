#!/bin/bash

# the idea is to search over all the months in the year to see which month provides the best yield

# argument $1 is the line_list, with the following pattern:
# "raster_month_1,raster_month_2,raster_month_3,raster_month_4,raster_month_5,raster_month_6,raster_month_7,raster_month_8,raster_month_9,raster_month_10,raster_month_11,raster_month_12"

# argument $2 is the raster name to save the result with the maximum of all cells
# argument $3 is the raster name to save the result with the maximum index of all cells
# echo ""
# echo ""
# echo ""
# echo "max yields was run"
# echo "line_list_to_find_max_for"
# echo "$line_list_to_find_max_for"
# echo "save_raster_max"
# echo "$save_raster_max"
# echo "save_raster_max_key"
# echo "$save_raster_max_key"
# echo ""
# echo ""
line_list_to_find_max_for=$1
save_raster_max=$2
save_raster_max_key=$3
r.series --overwrite input=$line_list_to_find_max_for output=$save_raster_max method=maximum
r.series --overwrite input=$line_list_to_find_max_for output=deleteme_${save_raster_max_key} method=max_raster

# Ensure the key of the max raster result is a CELL type (categorical integer raster) rather than double (DCELL)
# (because it is a raster representing the index of the raster which was chosen for the maximum)
r.mapcalc "${save_raster_max_key} = round(deleteme_${save_raster_max_key})"  

g.remove rast=deleteme_${save_raster_max_key}
