#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

#IR_meta_template=meta_oryza_rice_IR.SNX
#RF_meta_template=meta_oryza_rice_RF.SNX

IR_meta_template=meta_oryza_rice_noN_IR.SNX
RF_meta_template=meta_oryza_rice_noN_RF.SNX

out_dir=new_full_oryzarice

# and escape any funny business
# variety / name in file / name of file
withN_variety_list=\
"
IB0118	indica IR72	riK0t0

GF0001	IR64 normal	riK0t1
GF0002	IR64 drought	riK0t2
"

variety_list=\
"
IB0118	indica IR72	riK0t3

GF0001	IR64 normal	riK0t4
GF0002	IR64 drought	riK0t5
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






