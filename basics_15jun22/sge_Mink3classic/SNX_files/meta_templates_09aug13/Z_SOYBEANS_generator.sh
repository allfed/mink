#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_soybeans_IR.SNX
RF_meta_template=meta_soybeans_RF.SNX

out_dir=new_full_soybeans

# and escape any funny business
# variety / name in file / name of file
variety_list=\
"
990011	M GROUP 000	sbK011
990012	M GROUP  00	sbK012
990013	M GROUP   0	sbK013
990001	M GROUP   1	sbK001
990002	M GROUP   2	sbK002
990003	M GROUP   3	sbK003
990004	M GROUP   4	sbK004
990005	M GROUP   5	sbK005
990006	M GROUP   6	sbK006
990007	M GROUP   7	sbK007
990008	M GROUP   8	sbK008
990009	M GROUP   9	sbK009
990010	M GROUP  10	sbK010
"
#GFB002	CALIMA	bnB002

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






