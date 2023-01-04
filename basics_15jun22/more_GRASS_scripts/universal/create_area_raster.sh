#!/bin/bash

# this is a cheesy thing to try to figure out the area of a pixel in each
# row for a particular region

if [ $# != 2 ]; then
  echo ""
  echo "Usage: create_area_raster.sh region_to_use units"
  echo ""
  echo "The output raster will be {units}_per_pixel_{region_to_use}"
  echo ""
  echo "acceptable units are ha, km, mi, acres. if another"
  echo "unit is desired, used the form \'name=factor\' where"
  echo "name is the name of the unit"
  echo "factor is the number of that unit per square meter"
  echo "for example, to record the area in square feet, we might use"
  echo "create_area_raster.sh region_foo sqft=0.09290304"
  echo "the created map would be called sqft_per_pixel_region_foo"
  echo ""
  exit 1
fi

region_to_use=$1
units=$2

if   [ "$units" = "ha" ]; then
  multiplier=0.0001
elif [ "$units" = "km" ]; then
  multiplier=0.000001
elif [ "$units" = "mi" ]; then
  multiplier=0.000000386102159
elif [ "$units" = "acres" ]; then
  multiplier=0.000247105381
else
  multiplier=`echo "$units" | cut -d"=" -f2`
  units=`echo "units" | cut -d"=" -f1`
fi

output_raster=${units}_per_pixel_${region_to_use}

# make sure we can do what we need to do
this_mapset=`g.gisenv get=MAPSET`

#test_for_existence=`g.mlist type=region mapset=${this_mapset},PERMANENT pattern="$region_to_use"`
test_for_existence=`g.mlist type=region pattern="$region_to_use"`

if [ -z "$test_for_existence" ]; then
  echo "region is not defined [$region_to_use]"
  echo "please create it and try again..."
  exit 1
fi

test_for_existence=`g.mlist type=rast mapset=$this_mapset pattern="$output_raster"`

if [ -n "$test_for_existence" ]; then
  echo "desired output raster [$test_for_existence] already exists"
  echo "please remove it and try again..."
  exit 1
fi

#region_to_use=ifpri
#output_raster=ha_per_pixel_ifpri
# the raw results are in square meters. you can put in a multiplier
# here to convert them to whatever you want
# some common multipliers are:
# 0.0001   -> hectares
# 0.000001 -> sq km
# 0.000000386102159 -> sq mi
# 0.000247105381 -> acres
#multiplier=0.0001


# name the temp text file
temp_col=deleteme_single_col.txt
temp_header=deleteme_region_header.txt
temp_textA=deleteme_raster_partial.txt
temp_textB=deleteme_raster.txt

g.region $region_to_use

# initialize the output raster
#r.mapcalc $output_raster = "null()"

# pull out bits of the region definition
region_string=`g.region -g`
north=` echo "$region_string" | sed -n "1p" | cut --delimiter="=" --fields=2`
south=` echo "$region_string" | sed -n "2p" | cut --delimiter="=" --fields=2`
west=`  echo "$region_string" | sed -n "3p" | cut --delimiter="=" --fields=2`
east=`  echo "$region_string" | sed -n "4p" | cut --delimiter="=" --fields=2`
ns_res=`echo "$region_string" | grep nsres  | cut --delimiter="=" --fields=2`
ew_res=`echo "$region_string" | grep ewres  | cut --delimiter="=" --fields=2`
rows=`  echo "$region_string" | grep rows   | cut --delimiter="=" --fields=2`
cols=`  echo "$region_string" | grep cols   | cut --delimiter="=" --fields=2`

echo "north = [$north]"
echo "south = [$south]"
echo "east  = [$east]"
echo "west  = [$west]"
echo "rows  = [$rows]"
echo "cols  = [$cols]"

n_cols_calc=`echo "scale=10 ; ($west - $east) / $ew_res" | bc`
echo "n_cols_calc = [$n_cols_calc]"

echo "there are $rows rows and $cols cols in our region"

# clear out the temp files
rm $temp_textA $temp_textB

# run through all the rows
for (( row_num=1 ; row_num <= rows ; row_num++ ))
do
  echo "[r$row_num/$rows `date`]"
  #### reset the region
  g.region $region_to_use

  #### define the skinny region
  # move the northern boundary down by the appropriate number of cells
  new_north=`echo "$north - ($row_num - 1) * $ns_res" | bc`
  # move the southern boundary to 1 row down from the new northern boundary
  new_south=`echo "$new_north - $ns_res" | bc`

  #### deploy the new region
  g.region n=$new_north s=$new_south

  #### make a column raster with all the row numbers annotated
  r.mapcalc deleteme_colnum="col()"

  #### get r.stats to give us a bunch of areas...
  col_num_areas=`r.stats -a deleteme_colnum --q | cut --delimiter=" " --fields=2`

  #### paste the new col onto the existing columns...
  # use the wonders of echo without quotes to transpose the single column into a row
  if [ $row_num -eq 1 ] ; then
    # this is the first one, just put the values in the file
    echo $col_num_areas > $temp_textA
  else
    # this is a subsequent column, append
    echo $col_num_areas >> $temp_textA
  fi

done

#### create the headers and put it all together
echo "north: $north
south: $south
east: $east
west: $west
rows: $rows
cols: $cols
multiplier: $multiplier

" > $temp_header

cat $temp_header $temp_textA > $temp_textB

r.in.ascii input=$temp_textB output=$output_raster --o



temp_col=deleteme_single_col.txt
temp_header=deleteme_region_header.txt
temp_textA=deleteme_raster_partial.txt
temp_textB=deleteme_raster.txt

rm $temp_col $temp_header $temp_textA $temp_textB

g.region $region_to_use
