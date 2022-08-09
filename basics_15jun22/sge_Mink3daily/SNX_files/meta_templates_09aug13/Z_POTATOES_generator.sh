#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_potatoes_IR.SNX
RF_meta_template=meta_potatoes_RF.SNX

out_dir=full_potatoes

# and escape any funny business
# variety / name in file / name of file
variety_list=\
"
GFB001	Kufri Bahar	ptB001
GFB002	Amarilis (SLFP should be 0.94)	ptB002
GFB003	Victoria\/Asante	ptB003

IB0008	DESIREE recommended	ptB004
IB0004	KATHADIN recommended	ptB005
IB0009	LT-1 recommended	ptB006
IB0002	SEBAGO recommended	ptB007
IB0011	NORCHIP recommended	ptB008
IB0003	RUSSET BURBANK recommended	ptB009
"

####################

echo -e "\n\n\n\n WARNING!!!! potatoes need further processing about harvest dates and planting weights and stuff!!!! \n\n\n\n"
echo -e "pulling info from AssessmentGlobalSimsPotato_10132011.xlsx \n"
echo -e "and/or \n"
echo -e "Zone x Cultivar Table Potatoes Full 15082011.xls \n"
echo -e "and/or \n"
echo -e "coeffcient list_potato.xls \n\n"

# reset the inter-field-seperator
IFS="
"

# make the output directory if it isn't there already
mkdir -p $out_dir

# deal with each case
for variety_line in $variety_list; do

  # pull out the pieces
  variety_code=`echo "$variety_line" | cut -f1`
  variety_name=`echo "$variety_line" | cut -f2`
     file_name=`echo "$variety_line" | cut -f3`

  # determine the output names for the filled in files
  full_file_name_IR=${out_dir}/${file_name}IR.SNX
  full_file_name_RF=${out_dir}/${file_name}RF.SNX

  # do the search and replace
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $IR_meta_template > $full_file_name_IR
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $RF_meta_template > $full_file_name_RF

done






