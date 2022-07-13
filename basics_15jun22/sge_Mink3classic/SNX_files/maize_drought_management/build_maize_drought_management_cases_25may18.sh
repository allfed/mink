#!/bin/bash


IFS="
"


density_list=\
"
2.7
5.3
8.0
"


row_spacing_list=\
"
38
75
150
"


planting_depth_list=\
"
3
6
"

rain_multiplier_list=\
"
0.5
1.0
1.5
"


template_snx=mzdrought_template.SNX

        density_ph=DPH
    row_spacing_ph=RPH
 planting_depth_ph=PPH
rain_multiplier_ph=MPH



#####

echo ""
echo "remember to check the spacings so the x-file still works"
echo ""


for density         in $density_list; do
  density_clean=`echo "$density" | sed "s/\./p/g"`
for row_spacing     in $row_spacing_list; do
  # the row spacing name should have 3 characters to make sorting easier
  row_spacing_clean_first=`echo "$row_spacing" | sed "s/\./p/g"`
  if [ ${#row_spacing_clean_first} -eq 2 ]; then
    row_spacing_clean=0${row_spacing_clean_first}
  else
    row_spacing_clean=${row_spacing_clean_first}
  fi 
for planting_depth  in $planting_depth_list; do
  planting_depth_clean=`echo "$planting_depth" | sed "s/\./p/g"`
for rain_multiplier in $rain_multiplier_list; do
  rain_multiplier_clean=`echo "$rain_multiplier" | sed "s/\./p/g"`


  output_name=mzdrought_${density_clean}_${row_spacing_clean}_${planting_depth_clean}_${rain_multiplier_clean}.SNX

  # using the clean row spacing to keep everything the same width
  cat $template_snx | sed "s/$density_ph/$density/g ; s/$row_spacing_ph/$row_spacing_clean/g ; s/$planting_depth_ph/$planting_depth/g ; s/$rain_multiplier_ph/$rain_multiplier/g" > $output_name




done # run_multiplier
done # planting_depth
done # row_spacing
done # density














