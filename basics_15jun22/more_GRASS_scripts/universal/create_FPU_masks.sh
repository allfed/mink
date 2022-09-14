#!/bin/bash

# reset the IFS
IFS="
"

if [ $# -ne 1 ]; then
  echo "Usage: $0 raster_to_match_to cat_numbers_csv"

  exit 1
fi

raster_to_match_to=$1
cat_numbers_csv=$2


# set the region to match the raster
g.region rast=$raster_to_match_to

vector=food_production_units_with_codes

# get a list of all the "categories" in the vector
if [ -z "$cat_number" ]; then
  cat_list=`v.db.select -c $vector columns=cat`
else
  cat_list=$cat_numbers_csv
fi

for cat in $cat_list
do
  # pull out a single FPU
  v.extract input=$vector output=deleteme_an_FPU list=$cat --o 2>/dev/null
  echo "-- starting: `v.db.select deleteme_an_FPU -c col=FPU_ID,FPU fs=,` at `date` --"


  # do a one-off exception for ROW
  if [ $cat -eq 182 ]; then
    g.region rast=total_physical_area@spam_05nov09
    g.region zoom=total_physical_area@spam_05nov09
    align_region_to_raster.sh $raster_to_match_to
  else
    # reduce the region
    g.region vect=deleteme_an_FPU
    align_region_to_raster.sh $raster_to_match_to
  fi

  # convert it to a raster mask
  v.to.rast input=deleteme_an_FPU output=deleteme_FPU_mask_$cat use=val value=1 --o 2>/dev/null

done # category


