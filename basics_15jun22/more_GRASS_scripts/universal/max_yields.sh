#!/bin/bash

# the idea is to search over all the months in the year to see which month provides the best yield

# argument $1 is the line_list, with the following pattern:
# "raster_month_1,raster_month_2,raster_month_3,raster_month_4,raster_month_5,raster_month_6,raster_month_7,raster_month_8,raster_month_9,raster_month_10,raster_month_11,raster_month_12"

# argument $2 is the raster name to save the result with the maximum of all cells
# argument $3 is the raster name to save the result with the maximum index of all cells

r.series --overwrite input=$1 output=$2 method=maximum
r.series --overwrite input=$1 output=$3 method=max_raster

