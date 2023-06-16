#!/bin/bash


# the idea is to search over al lthe planting months to see which month provides the best yield.



# this is an attempt to brute force a whole boatload of varieties


# reset the IFS
IFS="
"

# var list and snx file list need to be in the same order, i guess....

original_group_name=NNN

soybeans_RF_var_list=\
"
sbK001RF
sbK002RF
sbK003RF
sbK004RF
sbK005RF
sbK006RF
sbK007RF
sbK008RF
sbK009RF
sbK010RF
sbK011RF
sbK012RF
sbK013RF
"

soybeans_IR_var_list=`echo "$soybeans_RF_var_list" | sed "s/RF/IR/g"`



### i am going to put an ugly wrapper loop around this so that i can just run it once and ###
### it will do both rainfed and irrigated so i don't have to remember to swap it....###

yield_mapset_with_at=@`g.gisenv get=MAPSET`

before_SNX_search_stuff=best_yield_
#after_SNX_search_stuff="I_mpi"
#after_SNX_search_stuff="I_mri"
#after_SNX_search_stuff="541*uke"



#after_SNX_search_stuff="ssp126*"
after_SNX_search_stuff=""

#human_readable_crop_name=soybeans
human_readable_crop_name=""
keep_RFIR="no" # yes or no; but anything other than yes is no....
for (( run_number=1 ; run_number <= 2 ; run_number++ )); do
#for (( run_number=2 ; run_number <= 2 ; run_number++ )); do

  if [ $run_number -eq 1 ]; then
    ### #1 = RF ###
    var_list=$soybeans_RF_var_list RFIR=${human_readable_crop_name}__rainfed
    a_good_snx=`echo "$var_list" | grep -v "^$" | head -n 1`
echo "[$a_good_snx]"
    echo g.mlist rast mapset=${yield_mapset_with_at#@} pat=${before_SNX_search_stuff}*${a_good_snx}*${after_SNX_search_stuff}*${RFIR}*yield_mean | sed "s/${a_good_snx}/\t/g ; "
    map_pre_suf_list=`g.mlist rast mapset=${yield_mapset_with_at#@} pat=${before_SNX_search_stuff}*${a_good_snx}*${after_SNX_search_stuff}*${RFIR}*yield_mean | sed "s/${a_good_snx}/\t/g ; "`

  if [ "$keep_RFIR" = "yes" ]; then
   group_name=${original_group_name}RF
  else
   group_name=${original_group_name}
  fi

  elif [ $run_number -eq 2 ]; then
    ### #2 = RF ###
    var_list=$soybeans_IR_var_list RFIR=${human_readable_crop_name}__irrigated
#    var_list=$soybeans_IR_var_list RFIR=${human_readable_crop_name}__rainfed
    a_good_snx=`echo "$var_list" | grep -v "^$" | head -n 1`
    map_pre_suf_list=`g.mlist rast mapset=${yield_mapset_with_at#@} pat=${before_SNX_search_stuff}*${a_good_snx}*${after_SNX_search_stuff}*${RFIR}*yield_mean | sed "s/${a_good_snx}/\t/g ; "`

  if [ "$keep_RFIR" = "yes" ]; then
   group_name=${original_group_name}IR
  else
   group_name=${original_group_name}
  fi

  else
    ### UNEXPECTED ###
   echo "something incredibly stupid happened...."
   exit
  fi # run_number testing to get RF and IR


#var_list=$soybeans_IR_var_list RFIR=soybeans__irrigated

#var_list=$soybeans_RF_var_list RFIR=legume__rainfed
#var_list=$soybeans_IR_var_list RFIR=legume__irrigated



echo "{{{$map_pre_suf_list}}}"






###############################################################

easy_snx_file_list=$var_list

snx_file_list=`echo "$easy_snx_file_list" | grep -v "^$"`




#let "n_chars = 12 - ${#prefix} - ${#suffix} - 4" # magic - 4 because i'm dropping the .MZX here

#echo "nchars = $n_chars"



pad_with_zeros() {

         value=$1
  total_digits=$2

  length_of_value=${#value}

  let "n_pads = total_digits - length_of_value"

  out_string=""

  for (( padding=1 ; padding <= n_pads ; padding++ ))
  do
    out_string=${out_string}0
  done

  echo "$out_string$value"
}


small_value=0.0001

for map_line in $map_pre_suf_list
do

  echo " [[[$map_line]]]"

  map_prefix=`echo "$map_line" | cut -f1`
  map_suffix=`echo "$map_line" | cut -f2`

#  padded_value=`pad_with_zeros 1 $n_chars`

  first_snx=`echo "$snx_file_list" | head -n 1`
#    echo "1st snx = [$first_snx]"

g.region rast=${map_prefix}${first_snx}${map_suffix}$yield_mapset_with_at
#    echo "after region..."

  best_variety=BEST_variety_${group_name}_${map_prefix}${map_suffix}
    best_yield=BEST_yield_${group_name}_${map_prefix}${map_suffix}

  r.mapcalc $best_variety = "0" 2>&1 | grep -v "%"
  r.mapcalc $best_yield   = "$small_value" 2>&1 | grep -v "%"

  category_string="0:no positive yield"
  var_number=0
for variety in $var_list
do
  var_code=${variety:0:6}

  let "var_number = var_number + 1"

#  padded_value=`pad_with_zeros $var_number $n_chars`

#  out_file=${prefix}${padded_value}${suffix}

  echo "-- #$var_number $var_code --"

  this_snx_file=`echo "$snx_file_list" | sed -n "${var_number}p"`

  this_yield=${map_prefix}${this_snx_file}${map_suffix}$yield_mapset_with_at
#  echo "           $this_yield"

  r.mapcalc $best_yield = "eval(x = $this_yield, \
                  if ( x > $best_yield, x, $best_yield) )" 2>&1 | grep -v "%"

  r.mapcalc $best_variety = "if($best_yield == $this_yield, $var_number, $best_variety)" 2>&1 | grep -v "%"

category_string="$category_string
$var_number:$var_code"

done

# reset the small value back to zero...
r.mapcalc $best_yield = "if($best_yield == $small_value, 0, $best_yield)"

zero_to_black.sh $best_yield
zero_to_black.sh $best_variety

category_string=`echo "$category_string" | grep -v "^$"`
assign_categories_from_string.sh $best_variety "$category_string"

done # map line





done # run_number; giant loop to brute force RF and IR so i don't have to run the script twice and forget to edit it properly
