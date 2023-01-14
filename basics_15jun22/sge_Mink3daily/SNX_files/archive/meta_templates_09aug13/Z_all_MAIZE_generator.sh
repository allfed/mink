#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_maize_IR.SNX
RF_meta_template=meta_maize_RF.SNX

out_dir=all_maize

# and escape any funny business
# variety / name in file / name of file
base_variety_list=\
"

GF0001	Baseline DevelopING	mzC001
IB0041	Baseline DevelopED	mzC002
"

some_variety_list=\
"
GF0101	Baseline 10%shorter DevelopING	mzD003
GF0201	Baseline 10%longer DevelopING	mzD004
GF0301	Yield norm cycle DevelopING	mzD005
GF0401	Yield 10%shorter DevelopING	mzD006
GF0501	Yield 10%longer DevelopING	mzD007

"

mz_list=`sed "s/-/_/g ; s/\//./g" mz_list.txt`

IFS="
"

counter=0

echo "mz_list=[$mz_list]"

for mz_line in $mz_list; do

let "counter++"


if [ $counter -le 9 ]; then
  padded_number="00$counter"
elif [ $counter -le 99 ]; then
  padded_number="0$counter"
else
  padded_number="$counter"
fi

var_code=`echo "$mz_line" | cut -f1`
var_name=`echo "$mz_line" | cut -f2`

#echo "   mz_line = [$mz_line]"
#echo "       counter = $counter ; code = [$var_code] ; name = [$var_name] ; padded = [$padded_number]"

  variety_list="$variety_list
$var_code	$var_name	mzE${padded_number}"


done

echo "[$variety_list]"



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
  echo "   --- [$variety_code] [$variety_name] ---"
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $IR_meta_template > $full_file_name_IR
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $RF_meta_template > $full_file_name_RF

done






