#!/bin/bash

if [ $# = 0 ]; then
  echo "Usage: reclassify_to_sequential.sh input_raster output_raster terse_flag"
  exit 0
fi 

echo "name of raster = [$1]"
echo "output raster = [$2]"

in_rast=$1
out_rast=$2

#temp_list_path="/tmp/category_list.txt"

#cat_list=`r.cats $1`

# check to see if it is a "cell" map or not
map_type=`r.info -t $in_rast | cut -d= -f3`

if [ "$map_type" = "CELL" ]; then
  # proceed as normal

cat_list=`r.describe -d1n map=$1`

n_cats=`echo "$cat_list" | wc -l`

cat_list_single_line=`echo $cat_list`

if_string=""
tail_cap=""

#gis_dbase=`g.gisenv get=GISDBASE`
#location=`g.gisenv get=LOCATION_NAME`
#mapset=`g.gisenv get=MAPSET`

#original_cat_file=${gis_dbase}/${location}/${mapset}/cats/${in_rast}
#original_cats=`cat $original_cat_file`
original_cats=`r.category $1`


for (( cat = 1 ; cat <= n_cats ; cat++ ))
do
  cat_index=`echo "$cat - 1" | bc`

  old_number=`echo $cat_list_single_line | cut --delimiter=" " -f$cat`

  if_string="${if_string} if( ${in_rast} == $old_number , $cat_index , "
  tail_cap="$tail_cap )"

#  original_label=`echo "$original_cats" | grep "${old_number}:" | cut -d: -f2`
  original_label=`echo "$original_cats" | grep "^${old_number}	" | cut -f2`

#new_category_string="${new_category_string}${cat}:${old_number}=${original_label}
  if [ -n "$3" ]; then
new_category_string="${new_category_string}${cat_index}:${original_label}
"
  else
new_category_string="${new_category_string}${cat_index}:${old_number}=${original_label}
"
  fi
  
done

  final_string="r.mapcalc $out_rast = \" $if_string null() $tail_cap \" "

echo "command = [$final_string]"
echo "categories = [$new_category_string]"
eval $final_string

assign_categories_from_string.sh $out_rast "$new_category_string"





else

  # make a temporary integer version to work off of...
  echo "map type is $map_type; converting to int before reclassifying"


  # always set the region...
  g.region rast=$in_rast

  r.mapcalc deleteme_as_int = "int($in_rast)"

cat_list=`r.describe -d1n map=deleteme_as_int`

n_cats=`echo "$cat_list" | wc -l`

cat_list_single_line=`echo $cat_list`

if_string=""
tail_cap=""

#gis_dbase=`g.gisenv get=GISDBASE`
#location=`g.gisenv get=LOCATION_NAME`
#mapset=`g.gisenv get=MAPSET`

#original_cat_file=${gis_dbase}/${location}/${mapset}/cats/${in_rast}
#original_cats=`cat $original_cat_file`
original_cats=`r.category deleteme_as_int` # there are probably no worthwhile integer labels from a floating point (at least the ones i have made)


for (( cat = 1 ; cat <= n_cats ; cat++ ))
do


  cat_index=`echo "$cat - 1" | bc`

  old_number=`echo $cat_list_single_line | cut --delimiter=" " -f$cat`

  if_string="${if_string} if( ${in_rast} == $old_number , $cat_index , "
  tail_cap="$tail_cap )"

#  original_label=`echo "$original_cats" | grep "${old_number}:" | cut -d: -f2`
  original_label=`echo "$original_cats" | grep "^${old_number}	" | cut -f2`

#new_category_string="${new_category_string}${cat}:${old_number}=${original_label}
  if [ -n "$3" ]; then
new_category_string="${new_category_string}${cat_index}:${original_label}
"
  else
new_category_string="${new_category_string}${cat_index}:${old_number}=${original_label}
"
  fi
  
done

  final_string="r.mapcalc $out_rast = \" $if_string null() $tail_cap \" "

echo "command = [$final_string]"
echo "categories = [$new_category_string]"
eval $final_string


assign_categories_from_string.sh $out_rast "$new_category_string"


fi # is it a floating point or not


















#echo "the number of things is [$#]"

#echo "they are:"

#for (( arg_num = 1 ; arg_num <= $# ; arg_num++ ))
#do
#  command_string="echo \"\$arg_num = [\${$arg_num}]\""
#  echo "command_string = [$command_string]"
#  eval $command_string
#  echo "$arg_num = [${$arg_num}}]"
#done
