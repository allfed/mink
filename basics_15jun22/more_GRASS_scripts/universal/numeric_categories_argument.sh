#!/bin/bash

# this script takes a list of rasters and pulls out their numeric categories and
# assigns them the same numbers as names so that when you do a post-script map,
# they will show up in the legend.

if [ $# != 1 ]; then
  echo "Usage: numeric_categories_argument.sh raster"
  echo ""
  echo "This assigns the category numbers to the category descriptions."
  exit 0
fi

echo "name of raster = [$1]"

map_name=$1

ttt1=/tmp/t1.txt
ttt2=/tmp/t2.txt

#####################

gisdbase=`g.gisenv get=GISDBASE`
location=`g.gisenv get=LOCATION_NAME`
mapset=`  g.gisenv get=MAPSET`

#category_list=`r.cats $map_name`
category_list=`r.category $map_name`

echo "$category_list" | cut --fields=1 > $ttt1
echo "$category_list" | cut --fields=1 > $ttt2

category_string=`paste --delimiters=":" $ttt1 $ttt2`

assign_categories_from_string.sh $map_name "$category_string"


#echo "# 1 categories
#
#0.00 0.00 0.00 0.00
#$category_string" >> ${gisdbase}${location}/${mapset}/cats/$map_name

