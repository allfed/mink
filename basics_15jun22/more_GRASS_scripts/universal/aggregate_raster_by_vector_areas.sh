#!/bin/bash

if [ $# -eq 0 ]; then

  echo "Usage: $0 raster vector output_file"

  exit
fi


raster=$1
vector=$2
output_file=$3


cat_list=`v.db.select $vector col=cat -c`

if [ -z "$cat_list" ]; then
  echo "the script is lame and assumes there is a \"cat\" column; this is not the case"
  exit
fi

full_details_list=`v.db.select $vector -c`


g.region rast=$raster

n_cats=`echo "$cat_list" | wc -l`
cat_num=1

for cat in $cat_list
do
  echo "-- $cat ; #$cat_num of $n_cats --"
  # pull out the ugly details
  ugly_details=`echo "$full_details_list" | sed -n "${cat_num}p"`

  # make the mask
  make_vector_mask_alt.sh $vector cat $cat $raster deleteme_MASK

  g.copy deleteme_MASK,MASK
  total_here=`r.sum $raster | cut -d" " -f3`
  g.remove MASK

  echo -e "$cat\t$total_here\t$ugly_details" >> $output_file

  let "cat_num++"

done



