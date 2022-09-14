#!/bin/bash

# the idea is to compare two rasters to see where they overlap

first_map=$1
second_map=$2
result_map=$3

if [ $# -ne 3 ]; then
  echo "Usage: $0 first_map second_map result_map"
  echo ""
  echo "The two maps are compared and a cartesian cross of the categories is performed"
  exit 1
fi

 first_set_of_values=`r.category $first_map  | cut -f1`
second_set_of_values=`r.category $second_map | cut -f1`


CS="r.mapcalc \"$result_map ="
closing=""

counter=0

for first_val in $first_set_of_values; do
for second_val in $second_set_of_values; do

  let "counter = counter + 1"

CS="${CS}
if($first_map == $first_val && $second_map == $second_val, $counter,"

closing="${closing} )"

cat_string="${cat_string}
$counter:$first_val,$second_val"

done # second_val
done # first_val

full_command="${CS}null()${closing}\""
#echo $full_command
eval $full_command 2>&1 | grep -v "%"


assign_categories_from_string.sh $result_map "$cat_string"



#r.mapcalc $result_map = "if(isnull($first_map), \
#     if(isnull($second_map),1,2), \
#     if(isnull($second_map),3,4) \
#                           )"
#
##r.colors $result_map -r
#
#category_string=\
#"1:both null
#2:$second_map valid
#3:$first_map valid
#4:both valid"
#
#assign_categories_from_string.sh $result_map "$category_string"
#
#echo \
#"1 200:200:200
#2 red
#3 blue
#4 150:150:150" | r.colors $result_map color=rules

