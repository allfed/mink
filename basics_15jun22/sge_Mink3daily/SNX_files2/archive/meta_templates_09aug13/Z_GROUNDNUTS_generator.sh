#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_groundnuts_IR.SNX
RF_meta_template=meta_groundnuts_RF.SNX

out_dir=new_full_groundnuts

# and escape any funny business
# variety / name in file / name of file
variety_list=\
"
GF0301	JL24-baseline (yield boost)	grK006
GF0302	M335-baseline (yield boost)	grK007
GF0303	55-437-baseline (yield boost)	grK008
GF0304	Fleur 11-baseline (yield boost)	grK009
GF0300	FLORUNNER for developED (yield boost)	grK010
"

basic_variety_list=\
"
GF0001	JL24-baseline	grK001
GF0002	M335-baseline	grK002
GF0003	55-437-baseline	grK003
GF0004	Fleur 11-baseline	grK004
GF0000	FLORUNNER for developED	grK005
"

old_variety_list=\
"
IB0003	Old standby for developED	grC005





GF0101	JL24-10%shortcycle	grD006
GF0201	JL24-10%longercycle	grD007
GF0301	JL24-base+yield	grD008
GF0401	JL24-10%short+yield	grD009
GF0501	JL24-10%long+yield	grD010
GF0102	M335-10%shortcycle	grD011
GF0202	M335-10%longercycle	grD012
GF0302	M335-base+yield	grD013
GF0402	M335-10%short+yield	grD014
GF0502	M335-10%long+yield	grD015
GF0103	55-437-10%shortcycle	grD016
GF0203	55-437-10%longercycle	grD017
GF0303	55-437-base+yield	grD018
GF0403	55-437-10%short+yield	grD019
GF0503	55-437-10%long+yield	grD020
GF0104	Fleu11-10%shortcycle	grD021
GF0204	Fleu11-10%longercycle	grD022
GF0304	Fleu11-base+yield	grD023
GF0404	Fleu11-10%short+yield	grD024
GF0504	Fleu11-10%long+yield	grD025

"

oldvariety_list=\
"
GF0000	FLORUNNER for developED	grC026
GF0100	FLO-10%shortcycle	grD027
GF0200	FLO-10%longercycle	grD028
GF0300	FLO-base+yield	grD029
GF0400	FLO-10%short+yield	grD030
GF0500	FLO-10%long+yield	grD031


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






