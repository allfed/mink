#!/bin/bash

# this script takes a list of rasters and pulls out their numeric categories and
# assigns them the same numbers as names so that when you do a post-script map,
# they will show up in the legend.

ttt1=deleteme_t1.txt
ttt2=deleteme_t2.txt

if [ $# -ne 1 ]; then
  echo "Usage: assign_numeric_categories.sh rastername

This script creates labels for each unique numeric value."
  exit 1
fi

map_name="$1"

#####################

gisdbase=`g.gisenv get=GISDBASE`
location=`g.gisenv get=LOCATION_NAME`
mapset=`  g.gisenv get=MAPSET`

#n_maps=`echo "$list_of_maps" | wc -l`



#for (( map_num=1 ; map_num <= n_maps ; map_num++ ))
#do

#  map_name=`echo "$list_of_maps" | sed -n "${map_num}p"`

  category_list=`r.describe -1 -n -d $map_name`
#  category_list=`r.category $map_name`
#  category_list=`r.cats $map_name`

#  echo "$category_list" | cut --fields=1 > $ttt1
#  echo "$category_list" | cut --fields=1 > $ttt2
  echo "$category_list" > $ttt1
  echo "$category_list" > $ttt2

  category_string=`paste --delimiters=":" $ttt1 $ttt2`

  n_categories=`echo "$category_string" | wc -l`
  #echo "C_S = [$category_string]"

echo "# $n_categories categories


0.00 0.00 0.00 0.00
$category_string" > ${gisdbase}/${location}/${mapset}/cats/$map_name

#done

exit 0

list_of_maps=\
"import_farmer_market_beanM
import_farmer_market_peasDryM
import_farmer_market_pulsesM
import_trader_port_beanM
import_trader_port_peasDryM
import_trader_port_pulsesM
export_competition_port_coffeeGX
export_competition_port_cottonCX
export_competition_port_gnutsinSX
export_competition_port_gnutsnoSX
export_competition_port_maizeX
export_competition_port_meatX
export_competition_port_milletX
export_competition_port_riceMillX
export_competition_port_ricePadX
export_competition_port_riceX
export_competition_port_sugarCRX
export_competition_port_wheatX"
