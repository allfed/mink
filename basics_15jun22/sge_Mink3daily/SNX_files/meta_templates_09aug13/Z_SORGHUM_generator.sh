#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_sorghum_IR.SNX
RF_meta_template=meta_sorghum_RF.SNX

out_dir=new_full_sorghum

# and escape any funny business
# variety / name in file / name of file
variety_list=\
"
GF0306	CSM-63-baseline	sgK004
GF0303	CSM-388-baseline	sgK005
GF0362	CSV 15-baselin	sgK006
"

baseline_list=\
"
GF0006	CSM-63-baseline	sgK001
GF0003	CSM-388-baseline	sgK002
GF0062	CSV 15-baselin	sgK003
"

####################

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






