#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_cassava_IR.SNX
RF_meta_template=meta_cassava_RF.SNX

out_dir=full_cassava

# and escape any funny business
# variety / name in file / name of file
variety_list=\
"
UC0002	MCol-22	csC001
UC0006	MCol-1684	csC002
UC0007	MCol-77	csC003
UC0008	MCol-26	csC004
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






