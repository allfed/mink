#!/bin/bash

# this is to try to be more clever about aligning to a raster than
# g.region align="" which sometimes doesn't do what i want...


if [ $# -ne 1 ]; then

  echo "Usage: $0 raster_to_align_to"
  echo ""
  echo "adapts the current region to have the same resolution as and line up with the raster_to_align_to"

  exit 1
fi

raster=$1

ms=25 # magic scale for bash calculator
ss=10 # magic scale for bash calculator

magic_minimum_ew_distance=0.00027777
magic_minimum_resolution=0.000027777 # 1/10 arc-second
magic_near_180_thresh=1

sexa_to_deci_with_hemisphere()
{
  full_string=$1
  # check if it is zero, because it won't have a hemisphere in that case
  if [ $full_string = 0 ]; then
    echo "0"
  else


  # peel off final character for hemisphere
  length=${#full_string}

  let "last_index = length - 1"
  sexa_value=${full_string:0:$last_index}
  hemi_value=${full_string:$last_index}

  d=0 ; m=0 ; s=0

  n_fields=`echo "$sexa_value" | awk -F ":" '{ print NF }'`

  d=`echo "$sexa_value" | cut -d: -f1`
  if [ $n_fields -gt 1 ]; then
    m=`echo "$sexa_value" | cut -d: -f2`
  fi
  if [ $n_fields -gt 2 ]; then
    s=`echo "$sexa_value" | cut -d: -f3`
  fi

  if [ $hemi_value = S -o $hemi_value = W ]; then
    sign=-1
  else
    sign=1
  fi


  deci_value=`echo "scale = $ms ; $sign * ( $d + $m / 60 + $s / 3600)" | bc`

  echo "$deci_value"

  fi

}

sexa_to_deci()
{
  full_string=$1
  # peel off final character for hemisphere
  length=${#full_string}

  sexa_value=${full_string}

  d=0 ; m=0 ; s=0

  n_fields=`echo "$sexa_value" | awk -F ":" '{ print NF }'`

  d=`echo "$sexa_value" | cut -d: -f1`
  if [ $n_fields -gt 1 ]; then
    m=`echo "$sexa_value" | cut -d: -f2`
  fi
  if [ $n_fields -gt 2 ]; then
    s=`echo "$sexa_value" | cut -d: -f3`
  fi

  
  deci_value=`echo "scale = $ms ; ( $d + $m / 60 + $s / 3600)" | bc`


  echo "$deci_value"
}

deci_to_sexa_with_hemisphere()
{
  raw_deci_value=$1
  NS_flag=$2

  deci_value=`echo "if($raw_deci_value < 0) {-1 * $raw_deci_value} else {$raw_deci_value}" | bc`
#  echo "abs'ed = $deci_value"

  d=`echo "$deci_value / 1" | bc`
  m=`echo "($deci_value - $d) * 60 / 1" | bc`
  if [ ${#m} -eq 1 ]; then
    m=0$m
  fi
  
  s_raw=`echo "scale = $ms ; ($deci_value - $d - $m / 60) * 3600" | bc`
  s_short=`echo "scale=$ss ; $s_raw / 1" | bc`
  s_front=`echo "$s_short" | cut -d. -f1`
   s_back=`echo "$s_short" | cut -d. -f2`

  if [ -z "$s_front" ]; then
    s_real_front=00
  elif [ ${#s_front} -eq 1 ]; then
    s_real_front=0${s_front}
  else
    s_real_front=${s_front}
  fi

  s=${s_real_front}.${s_back}

  if [ $raw_deci_value = $deci_value ]; then
    if [ -n "$NS_flag" ]; then
      echo "$d:$m:${s}N"
    else
      echo "$d:$m:${s}E"
    fi
  else
    if [ -n "$NS_flag" ]; then
      echo "$d:$m:${s}S"
    else
      echo "$d:$m:${s}W"
    fi
  fi
  
}

deci_to_sexa()
{
  raw_deci_value=$1

  deci_value=`echo "if($raw_deci_value < 0) {-1 * $raw_deci_value} else {$raw_deci_value}" | bc`
#  echo "abs'ed = $deci_value"

#  echo "dv = [$deci_value]" >&2

  d=`echo "$deci_value / 1" | bc`
  m=`echo "($deci_value - $d) * 60 / 1" | bc`
  if [ ${#m} -eq 1 ]; then
    m=0$m
  fi
  s_raw=`echo "scale = $ms ; ($deci_value - $d - $m / 60) * 3600" | bc`
  #echo "scale = $ms ; ($deci_value - $d - $m / 60) * 3600" >&2
  #echo "sraw = [$s_raw]" >&2

  s_short=`echo "scale=$ss ; $s_raw / 1" | bc`

  s_front=`echo "$s_short" | cut -d. -f1`
   s_back=`echo "$s_short" | cut -d. -f2`

  if [ -z "$s_front" ]; then
    s_real_front=00
  elif [ ${#s_front} -eq 1 ]; then
    s_real_front=0${s_front}
  else
    s_real_front=${s_front}
  fi

  s=${s_real_front}.${s_back}

  echo "$d:$m:${s}"


}











# save off the current region

echo "raster = [$raster]"

# check if it exists...
only_raster=${raster%%@*}
only_mapset=${raster##*@}

if [ $only_raster = $only_mapset ]; then
  # there was no at sign...
  # check in this mapset
  raster_exists=`g.mlist rast pat=$only_raster`
else
  # there was an at sign...
  raster_exists=`g.mlist rast pat=$only_raster mapset=$only_mapset`
fi

if [ -z "$raster_exists" ]; then
  echo "requested raster [$raster] does not exist..."
  exit
fi

#g.region save=deleteme_region_before_alignment --o ; echo "==== original long ====" ; g.region -p

original_region=`g.region -p`

o_n=`echo "$original_region" | sed -n "5p" | cut -c13-`
o_s=`echo "$original_region" | sed -n "6p" | cut -c13-`
o_w=`echo "$original_region" | sed -n "7p" | cut -c13-`
o_e=`echo "$original_region" | sed -n "8p" | cut -c13-`
o_nsres=`echo "$original_region" | sed -n "9p" | cut -c13-`
o_ewres=`echo "$original_region" | sed -n "10p" | cut -c13-`

o_n_d=`sexa_to_deci_with_hemisphere $o_n`
o_s_d=`sexa_to_deci_with_hemisphere $o_s`
o_e_d=`sexa_to_deci_with_hemisphere $o_e`
o_w_d=`sexa_to_deci_with_hemisphere $o_w`

# check to make sure that west is west of east.
#echo "sqrt(($o_e_d - $o_w_d)^2) " | bc
ew_are_ok=`echo "if(sqrt(($o_e_d - $o_w_d)^2) > $magic_minimum_ew_distance) {1} else {0}" | bc`

o_nsres_d=`sexa_to_deci $o_nsres`
o_ewres_d=`sexa_to_deci $o_ewres`

ew_res_is_ok=`echo "if($o_ewres_d > $magic_minimum_resolution) {1} else {0}" | bc`

ew_are_close_to_180=`echo "scale = 10 ; if( \
            sqrt(($o_e_d - 180)^2) < $magic_near_180_thresh  || \
            sqrt(($o_e_d + 180)^2) < $magic_near_180_thresh  || \
            sqrt(($o_w_d - 180)^2) < $magic_near_180_thresh  || \
            sqrt(($o_w_d + 180)^2) < $magic_near_180_thresh \
                               ) {1} else {0}" | bc`

if [ $ew_res_is_ok = 0 ]; then
  g.region -d # reset from default
  echo "resetting ew res [old = $o_ewres] to match ns res [$o_nsres]"
fi

  # assume that ns and ew res should be the same...
  # switch east and west if necessary
#  if [ $ew_are_ok = 0 ]; then
#    echo "switching east and west boundaries..."
#    new_west=$o_e_d
#    o_e_d=$o_w_d
#    o_w_d=$new_west
#  fi

# now figure out what the raster's native region is

g.region rast=$raster # ; echo "====  raster long  ====" ; g.region -p ; echo "======================="

raster_region=`g.region -p`

r_n=`echo "$raster_region" | sed -n "5p" | cut -c13-`
r_s=`echo "$raster_region" | sed -n "6p" | cut -c13-`
r_w=`echo "$raster_region" | sed -n "7p" | cut -c13-`
r_e=`echo "$raster_region" | sed -n "8p" | cut -c13-`
r_nsres=`echo "$raster_region" | sed -n "9p" | cut -c13-`
r_ewres=`echo "$raster_region" | sed -n "10p" | cut -c13-`

r_n_d=`sexa_to_deci_with_hemisphere $r_n`
r_s_d=`sexa_to_deci_with_hemisphere $r_s`
r_e_d=`sexa_to_deci_with_hemisphere $r_e`
r_w_d=`sexa_to_deci_with_hemisphere $r_w`
r_nsres_d=`sexa_to_deci $r_nsres`
r_ewres_d=`sexa_to_deci $r_ewres`

#echo "n: [$r_n] -> [$r_n_d]"
#echo "s: [$r_s] -> [$r_s_d]"
#echo "e: [$r_e] -> [$r_e_d]"
#echo "w: [$r_w] -> [$r_w_d]"
#echo "NSRES: [$r_nsres] -> [$r_nsres_d]"
#deci_to_sexa $r_nsres_d
#echo "EWRES: [$r_ewres] -> [$r_ewres_d]"
#deci_to_sexa $r_ewres_d


# determine the new northern boundary

# figure out how many cells up or down it is from the desired region to teh raster's edge...
#echo "N lat diff"
#echo "($o_n_d - $r_n_d) " | bc
#echo "S lat diff"
#echo "($o_s_d - $r_s_d) " | bc

#echo "----"
n_n_steps=`echo "($o_n_d - $r_n_d) / $r_nsres_d + 1" | bc`
n_s_steps=`echo "($o_s_d - $r_s_d) / $r_nsres_d - 1" | bc`

n_e_steps=`echo "($o_e_d - $r_e_d) / $r_ewres_d + 1" | bc`
n_w_steps=`echo "($o_w_d - $r_w_d) / $r_ewres_d - 1" | bc`

#echo "$r_n_d + $n_n_steps * $r_nsres_d"

#echo "steps: n=$n_n_steps s=$n_s_steps e=$n_e_steps w=$n_w_steps"

new_n=`echo "$r_n_d + $n_n_steps * $r_nsres_d" | bc`
new_s=`echo "$r_s_d + $n_s_steps * $r_nsres_d" | bc`

new_e=`echo "$r_e_d + $n_e_steps * $r_ewres_d" | bc`
new_w=`echo "$r_w_d + $n_w_steps * $r_ewres_d" | bc`

#echo "new boundaries NSEW..."
#echo "$r_n_d + $n_n_steps * $r_nsres_d" 
#echo "$r_s_d + $n_s_steps * $r_nsres_d"

#echo "$r_e_d + $n_e_steps * $r_ewres_d"
#echo "$r_w_d + $n_w_steps * $r_ewres_d"

#echo "N: $new_n `deci_to_sexa $new_n`"
#echo "S: $new_s `deci_to_sexa $new_s`"
#echo "E: $new_e `deci_to_sexa $new_e`"
#echo "W: $new_w `deci_to_sexa $new_w`"
#echo "done, gonna check for beyond the ken..."

# check if they are beyond the poles
new_n=`echo "if($new_n > 90) { 90 } else { if($new_n < -90) {-90} else {$new_n} }" | bc`
new_s=`echo "if($new_s > 90) { 90 } else { if($new_s < -90) {-90} else {$new_s} }" | bc`

new_e=`echo "if($new_e > 180) { 180 } else { if($new_e < -180) {-180} else {$new_e} }" | bc`
new_w=`echo "if($new_w > 180) { 180 } else { if($new_w < -180) {-180} else {$new_w} }" | bc`

# convert to sexa
new_n_s=`deci_to_sexa_with_hemisphere $new_n NFLAG`
new_s_s=`deci_to_sexa_with_hemisphere $new_s NFLAG`

# only move the e/w if they were non-equal originally...

if [ $o_e = $o_w ]; then
  new_e_s=$o_e
  new_w_s=$o_w
else
  new_e_s=`deci_to_sexa_with_hemisphere $new_e `
  new_w_s=`deci_to_sexa_with_hemisphere $new_w `
fi

#echo "n_n_steps = $n_n_steps"
#echo "n_s_steps = $n_s_steps"

#echo "new_n = $new_n / $new_n_s"
#echo "new_s = $new_s / $new_s_s"

#echo "r_nsres = $r_nsres"

#sexa_to_deci_with_hemisphere $value_to_convert
#asdf=`sexa_to_deci_with_hemisphere $value_to_convert`
#deci_to_sexa_with_hemisphere $asdf

#echo "new: n=$new_n_s s=$new_s_s e=$new_e_s w=$new_w_s nsres=$r_nsres ewres=$r_ewres"
g.region n=$new_n_s s=$new_s_s e=$new_e_s w=$new_w_s nsres=$r_nsres ewres=$r_ewres

#g.region -p
#g.region -g










