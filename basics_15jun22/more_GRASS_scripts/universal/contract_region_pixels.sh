#!/bin/bash


####
# this script tries to make pretty post-script maps

#output_directory=~grass/grass_scripts/maps/new/ # Beware the MAGIC NUMBER!!! this should match with new_map_function.sh

usage_message()
{
  echo ""
  echo "Usage: $0 n_pixels_to_shrink" 
  echo ""
  echo "This shrinks the nsew bounds by the indicated number of pixels."
  echo ""
}


if [ $# -lt 1 ]; then
  usage_message
  exit 1
fi

 n_pixels=$1




# expand out by a small fraction
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

  new_n=`echo "$old_n - $n_pixels * $old_ns_res" | bc`
  new_s=`echo "$old_s + $n_pixels * $old_ns_res" | bc`
  new_w=`echo "$old_w + $n_pixels * $old_ns_res" | bc`
  new_e=`echo "$old_e - $n_pixels * $old_ns_res" | bc`

  g.region n=$new_n s=$new_s w=$new_w e=$new_e -a

