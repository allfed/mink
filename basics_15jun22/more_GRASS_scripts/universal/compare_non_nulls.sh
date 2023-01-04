#!/bin/bash

# the idea is to compare two rasters to see where they overlap

first_map=$1
second_map=$2
result_map=$3

if [ $# -ne 3 ]; then
  echo "Usage: $0 first_map second_map result_map"
  echo ""
  echo "The two maps are compared and places where the footprints overlap are noted"
  exit 1
fi



r.mapcalc $result_map = "if(isnull($first_map), \
     if(isnull($second_map),1,2), \
     if(isnull($second_map),3,4) \
                           )"

#r.colors $result_map -r

category_string=\
"1:both null
2:$second_map valid
3:$first_map valid
4:both valid"

assign_categories_from_string.sh $result_map "$category_string"

echo \
"1 200:200:200
2 red
3 blue
4 150:150:150" | r.colors $result_map color=rules

