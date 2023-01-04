#!/bin/bash

# this script takes a list of rasters and pulls out their numeric categories and
# assigns them the same numbers as names so that when you do a post-script map,
# they will show up in the legend.

raster_to_categorize="$1"
category_string="$2"

#echo "CS = [$category_string]"


usage_code="Usage: $0 rastername \"category string in quotes to preserve newlines\"

This script creates category labels based on the provided string and puts in in
the right place based on your current mapset. However, no checking is done on
the contents or formatting of the provided string.

The way to use this (reasonably) properly is to define your categories as a
shell variable and then call the script using the variable as the argument.
For example:

cat_string=\\
\"0:zero value
1:unity
2:two
3:triad
4:cuatro\"

$0 raster_to_assign_categories_to \"\$cat_string\"
"


if [ $# -ne 2 ]; then
  echo "$usage_code"
  exit 1
fi


#####################

test_string=`g.mlist type=rast pattern="$raster_to_categorize"`

if [ -z "$test_string" ]; then
  echo "raster $raster_to_categorize has failed to exist in this mapset"
  exit 1
fi

gisdbase=`g.gisenv get=GISDBASE`
location=`g.gisenv get=LOCATION_NAME`
mapset=`  g.gisenv get=MAPSET`


n_categories=`echo "$category_string" | wc -l`

echo "# $n_categories categories


0.00 0.00 0.00 0.00
$category_string" > ${gisdbase}/${location}/${mapset}/cats/$raster_to_categorize

