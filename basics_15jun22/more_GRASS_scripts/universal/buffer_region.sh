#!/bin/bash


####
# this script tries to make pretty post-script maps

#output_directory=~grass/grass_scripts/maps/new/ # Beware the MAGIC NUMBER!!! this should match with new_map_function.sh

usage_message()
{
  echo ""
  echo "Usage: $0 percent_buffer reference_raster"
  echo ""
  echo "this takes the current region and grows it by the supplied decimal fraction"
  echo "for example, 0.1 would move each edge out 10% of the original size"
  echo "of the region"
  echo ""
  echo "the reference_raster is a raster to which we will zoom and then apply the buffer. this"
  echo "is helpful when the raster goes over 180 (e.g., Russia or the USA)."
}


if [ $# -lt 2 ]; then
  usage_message
  exit 1
fi











# expand out by a small fraction
magic_buffer_fraction=$1
reference_raster=$2

current_region=`g.region -g`
  old_n=`echo "$current_region" | sed -n "1p" | cut -d= -f2`
  old_s=`echo "$current_region" | sed -n "2p" | cut -d= -f2`
  old_w=`echo "$current_region" | sed -n "3p" | cut -d= -f2`
  old_e=`echo "$current_region" | sed -n "4p" | cut -d= -f2`
  old_ns_res=`echo "$current_region" | sed -n "5p" | cut -d= -f2`
  old_ew_res=`echo "$current_region" | sed -n "6p" | cut -d= -f2`
  old_rows=`echo "$current_region" | sed -n "7p" | cut -d= -f2`
  old_cols=`echo "$current_region" | sed -n "8p" | cut -d= -f2`

  # initialize the "use" stuff

      use_w=$old_w
      use_e=$old_e

  # determine if region

#echo "old: $old_n $old_s $old_w $old_e"

### ok, let's say, if there is anything within a certain buffer of 180, try shifting
magic_meridian_buffer_size=10
#test_string=`echo "if ( sqrt(($old_w - (-180))^2) < $magic_meridian_buffer_size || sqrt(($old_e - 180)^2) < $magic_meridian_buffer_size ) {1} else {0}" | bc`
test_string=`echo "if ( sqrt(($old_w - (-180))^2) < $magic_meridian_buffer_size || sqrt(($old_e - 180)^2) < $magic_meridian_buffer_size || sqrt(($old_w + (-180))^2) < $magic_meridian_buffer_size || sqrt(($old_e + 180)^2) < $magic_meridian_buffer_size ) {1} else {0}" | bc`

echo "meridian near 180 test = [$test_string]"

magic_shift=153
  if [ $test_string -eq 1 ]; then
    echo "shifting by $magic_shift EW, and zooming"
    #echo "original w=$old_w e=$old_e"
    shift_w=`echo "$old_w + $magic_shift" | bc`
    shift_e=`echo "$old_e + $magic_shift" | bc`
    #echo "shifted  w=$shift_w e=$shift_e"
    g.region w=$shift_w e=$shift_e -a
    echo "zooming..."
#    g.region zoom=deleteme_mask
    g.region zoom=$reference_raster
    #echo "re-retrieving stuff"
current_region=`g.region -g`
  old_n=`echo "$current_region" | sed -n "1p" | cut -d= -f2`
  old_s=`echo "$current_region" | sed -n "2p" | cut -d= -f2`
  zoomed_w=`echo "$current_region" | sed -n "3p" | cut -d= -f2`
  zoomed_e=`echo "$current_region" | sed -n "4p" | cut -d= -f2`
  old_ns_res=`echo "$current_region" | sed -n "5p" | cut -d= -f2`
  old_ew_res=`echo "$current_region" | sed -n "6p" | cut -d= -f2`
  old_rows=`echo "$current_region" | sed -n "7p" | cut -d= -f2`
  old_cols=`echo "$current_region" | sed -n "8p" | cut -d= -f2`
    if [ "$zoomed_w" != "$old_w" ]; then
      #echo "       keeping the zoomed values"
      use_w=$zoomed_w
      use_e=$zoomed_e
    else
      use_w=$old_w
      use_e=$old_e
    fi
    #echo "after zoomed   w=$use_w e=$use_e"
  fi




  new_n=`echo "scale=6 ; $old_n + $old_rows * $old_ns_res * $magic_buffer_fraction" | bc`
  new_s=`echo "scale=6 ; $old_s - $old_rows * $old_ns_res * $magic_buffer_fraction" | bc`
              #  new_w=`echo "scale=6 ; $old_w - $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
              #  new_e=`echo "scale=6 ; $old_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`

  echo "total BEFORE width = `echo "scale=10 ;  sqrt(($use_w - $use_e)^2) " | bc`"
  test_string=`echo "scale=10 ; if ( sqrt(($use_w - $use_e)^2) * (1 + 2*$magic_buffer_fraction) > 360) {1} else {0}" | bc`
  if [ $test_string -eq 1 ]; then
    # scale it down to halfway to full coverage; that means a quarter each way
    new_w=`echo "scale=6 ; remaining_gap = 360 - sqrt(($use_w - $use_e)^2); $use_w - remaining_gap / 4" | bc`
    new_e=`echo "scale=6 ; remaining_gap = 360 - sqrt(($use_w - $use_e)^2); $use_e + remaining_gap / 4" | bc`
#    new_e=`echo "scale=6 ; $use_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
  else
    # apply the magic buffer zone
    new_w=`echo "scale=6 ; $use_w - $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
    new_e=`echo "scale=6 ; $use_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
  fi

#echo "new: $new_n $new_s $new_w $new_e"
  new_n=`echo "if ($new_n > 90) {90} else {$new_n}" | bc`
  new_s=`echo "if ($new_s < -90) {-90} else {$new_s}" | bc`

#  echo "scale=10 ; if ( sqrt(($new_w - $new_e)^2) > 360 ) {1} else {0}"
  echo "total width = `echo "scale=10 ;  sqrt(($new_w - $new_e)^2) " | bc`"
  test_string=`echo "scale=10 ; if ( sqrt(($new_w - $new_e)^2) > 360) {1} else {0}" | bc`
  if [ $test_string -eq 1 ]; then
    echo "resetting to old bounds ($old_w/$old_e)"
    # pull just inside the bounds so that vectors are rendered properly
    new_w=$old_w
    new_e=$old_e
#    new_w=`echo "if ($new_w < -180) {180} else {$new_w}" | bc`
#    new_e=`echo "if ($new_e > 180) {180} else {$new_e}" | bc`
  fi
echo "final: $new_n $new_s $new_w $new_e"


  g.region n=$new_n s=$new_s w=$new_w e=$new_e -a
#  g.region align=$raster_name

