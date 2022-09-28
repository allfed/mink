#!/bin/bash


# hacking to optimize over an arbitrary list of rasters

# the idea is to search over all the planting months to see which month provides the best yield.



# this is an attempt to brute force a whole boatload of varieties

# let us store the list in a separate file that can be "source-ed" so
# as to make this more portable

#. MAIZE_varieties.sh
#. RICE_varieties.sh

# reset the IFS
IFS="
"


group_name=VVV
#group_name=RRR

#group_name=NNN

#group_name=N02
#group_name=N04
#group_name=N08
#group_name=N16





if [ 0 = 1 ]; then

yield_mapset_with_at=@`g.gisenv get=MAPSET`

var_list=\
"bahia001
bermuda001
brachiaria001
"

a_good_snx=`echo "$var_list" | grep -v "^$" | head -n 1`

map_pre_suf_list="
`g.mlist rast mapset=${yield_mapset_with_at#@} pat=best_yield*${a_good_snx}*_CWAM | sed "s/${a_good_snx}/\t/g ; "`
"

echo "$map_pre_suf_list"



# build a giant list of day offsets
#var_list=0
for (( ddd=0 ; ddd <= 365 ; ddd = ddd + 16 )); do
  var_list="$var_list
$ddd"

done # ddd build daily list

yield_mapset_with_at=@`g.gisenv get=MAPSET`
a_good_snx=`echo "$var_list" | grep -v "^$" | head -n 1`

map_pre_suf_list="
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_base_2000_p0_maize__eitherN000_yield_mean
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_base_2000_p0_maize__eitherN100_yield_mean
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_base_2000_p0_maize__eitherN200_yield_mean
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize__eitherN000_yield_mean
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize__eitherN100_yield_mean
SEARCHWORLDCOLD__mzK023RF_379_d	_searchworld__1_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize__eitherN200_yield_mean
"


var_list=\
"
13
14
15
16
17
18
21
23
24
25
"
map_pre_suf_list="SWCOLD__mzK0	IR_379_dSEARCHED_searchworldA__1_deltaONpikNOV_base_2000_p0_maize__irrigated_yield_mean"

fi # cutout



if [ 0 = 1 ]; then

crop=ba
#crop=bs
#crop=ch
#crop=cn
#crop=mz
#crop=pt
#crop=ri
#crop=sb
#crop=sg
#crop=su
#crop=wh






group_name=VVV_${crop}

# ISIMIPHARP__suA002RF_400_dSEARCHED_isimipharp__1_gfdl_esm4_baseline_1994_2016_p0_anycrop__N100_yield_mean
var_list=`g.mlist rast pat=ISIMIPHARP__${crop}*RF*yield_mean | cut -d_ -f3 | sed "s/RF//g" | grep -v "^$" | sort | uniq`

echo ">>>$var_list<<<"

first_good_var=`echo "$var_list" | head -n 1`

echo "{$first_good_var}"

map_pre_suf_list="
`g.mlist rast pat=ISIMIPHARP__*${first_good_var}*yield_mean | cut -d_ -f4- | sed "s/^/ISIMIPHARP__	RF_/g"`
`g.mlist rast pat=ISIMIPHARP__*${first_good_var}*yield_mean | cut -d_ -f4- | sed "s/^/ISIMIPHARP__	IR_/g"`
"

#COLDHARP__cold1_	RF_`g.mlist rast pat=COLDHARP__${crop}*yield_mean | head -n 1 | cut -d_ -f4-`
#COLDHARP__cold1_	IR_`g.mlist rast pat=COLDHARP__${crop}*yield_mean | head -n 1 | cut -d_ -f4-`

fi # end cutout



if [ 0 = 1 ]; then # cutout D

# revenue guesses for russian crops
group_name=RRR

# ISIMIPHARP__suA002RF_400_dSEARCHED_isimipharp__1_gfdl_esm4_baseline_1994_2016_p0_anycrop__N100_yield_mean
#var_list=`g.mlist rast pat=ISIMIPHARP__${crop}*RF*yield_mean | cut -d_ -f3 | sed "s/RF//g" | grep -v "^$" | sort | uniq`
echo "arbitrarily ignoring potatoes since they seem to overwhelmingly win; but i think there may be quality problems which prevent their actual dominance"
var_list=`g.mlist rast pat=deleteme_revenue_base_2000_*IR | cut -d_ -f5 | sed "s/RF//g" | grep -v "^$" | sort | uniq | grep -v pota`

echo ">>>$var_list<<<"

first_good_var=`echo "$var_list" | head -n 1`

echo "{$first_good_var}"

map_pre_suf_list="
deleteme_revenue_base_2000_	_IR
deleteme_revenue_base_2000_	_RF
"
#`g.mlist rast pat=ISIMIPHARP__*${first_good_var}*yield_mean | cut -d_ -f4- | sed "s/^/ISIMIPHARP__	RF_/g"`

fi # cutout D



yield_mapset_with_at=@`g.gisenv get=MAPSET`

var_list=\
"
mlA001
mlA002
mlA003
"

a_good_snx=`echo "$var_list" | grep -v "^$" | head -n 1`

map_pre_suf_list="
`g.mlist rast mapset=${yield_mapset_with_at#@} pat=best_yield*${a_good_snx}*_yield_mean | sed "s/${a_good_snx}/\t/g ; "`
"


echo "[[[$map_pre_suf_list]]]"



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
echo "no longer requiring a 6 character long code...."
#  var_code=${variety:0:6}
var_code=$variety

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








old_examplegroundnuts_var_list=\
"
PIARA1 JL24 Standard        . PN0007 11.84  0.00  17.4   7.0  17.5 62.00 70.00  1.36  245.  16.0  0.84 0.600  28.0  1.65  15.0  78.0  .270  .510
PIARA2 M335 Standard        . VIRGIN 11.84  0.00  20.0   8.0  20.3 70.00 78.00  1.36  270.  18.0  0.85 0.750  30.0  1.65  22.0  75.0  .270  .510
PIARA3 55-437               . PN0007 11.84  0.00  16.5   5.5  15.5 38.00 44.00  1.28  250   16.0  0.84 0.360  28.1  1.65  14.0  78.0  .270  .510
"
