#!/bin/bash


# so, we have to find some way to match up the wheat varieties with
# geographic regions. in theory, we have mega-environment masks that
# should do the trick, so here we go....


# reset the IFS to lines
IFS="
"

# read in some pre-defined lists
#. variety_lists_04jan12.sh




# let's try to brute force this first and establish a pattern later.

climate_list=\
"single_run_nonsense"


# some placeholders
snx_ph=SNXSNXSNX
cli_ph=CLICLICLI


yield_mapset=`g.gisenv get=MAPSET`

#yield_mapset=ricky_DSSAT_risky



# cases to deal with
# bsl: baseline
# srt: short
# lng: long
# bay: yield boost
# say: short + yield
# lay: long + yield


if [ 1 = 1 ]; then

#output_tag=GEObsl
output_tag=GEObslgroundnuts

snx_me_list=\
"
grK001	MASK_for_groundnuts_asia_like_NEW
grK002	MASK_for_groundnuts_asia_like_NEW

grK003	MASK_for_groundnuts_central_africa_like_NEW
grK004	MASK_for_groundnuts_central_africa_like_NEW

grK005	MASK_for_groundnuts_everything_else_NEW
"
#grC005	MASK_for_groundnuts_everything_else_NEW

fi

if [ 0 = 1 ]; then

output_tag=GEOsrt

snx_me_list=\
"
grD006	MASK_for_groundnuts_asia_like_NEW

grD021	MASK_for_groundnuts_central_africa_like_NEW

grD027	MASK_for_groundnuts_everything_else_NEW
"

fi

if [ 0 = 1 ]; then

output_tag=GEOlng

snx_me_list=\
"
grD007	MASK_for_groundnuts_asia_like_NEW

grD022	MASK_for_groundnuts_central_africa_like_NEW

grD028	MASK_for_groundnuts_everything_else_NEW
"

fi

if [ 0 = 1 ]; then


output_tag=GEObay
snx_me_list=\
"
grK006	MASK_for_groundnuts_asia_like_NEW
grK007	MASK_for_groundnuts_asia_like_NEW

grK008	MASK_for_groundnuts_central_africa_like_NEW
grK009	MASK_for_groundnuts_central_africa_like_NEW

grK010	MASK_for_groundnuts_everything_else_NEW
"

fi

if [ 0 = 1 ]; then

output_tag=GEOsay

snx_me_list=\
"
grD009	MASK_for_groundnuts_asia_like_NEW

grD024	MASK_for_groundnuts_central_africa_like_NEW

grD030	MASK_for_groundnuts_everything_else_NEW
"

fi

if [ 0 = 1 ]; then

output_tag=GEOlay

snx_me_list=\
"
grD010	MASK_for_groundnuts_asia_like_NEW

grD025	MASK_for_groundnuts_central_africa_like_NEW

grD031	MASK_for_groundnuts_everything_else_NEW
"

fi

if [ 0 = 1 ]; then

output_tag=GEOAFLAbsl

snx_me_list=\
"
grKAFLA001	MASK_for_groundnuts_asia_like_NEW
grKAFLA002	MASK_for_groundnuts_asia_like_NEW

grKAFLA003	MASK_for_groundnuts_central_africa_like_NEW
grKAFLA004	MASK_for_groundnuts_central_africa_like_NEW

grKAFLA005	MASK_for_groundnuts_everything_else_NEW
"
#grC005	MASK_for_groundnuts_everything_else_NEW

fi



run_list=\
"
`g.mlist rast pat=best_yield*grK003** | sed "s/grK003/${snx_ph}/g"`
"


echo "RL=[$run_list]"
echo "+++++++++++++++++++++++++++++++++"



#a_valid_SNX=grC001
a_valid_SNX=grK003
#a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1  | cut -f1`

# build up the country masks

if [ 1 = 1 ]; then


  grow_radius=2

  . SOME_country_groupings.sh

  # let us do this. the africa like countries will be everything that is NOT asia
  # while the asia like countries will be everything that is NOT africa
  # thus, we will be using an average for the USAs, chinas, etc
  #
  # i am now going to add a not-africa/not-asia zone which will use our old standby variety

#  africa_like_list_inverse="$iiasa_middle_east,$iiasa_southeast_asia,$iiasa_south_asia,$iiasa_east_asia,$iiasa_centra_asia"

#    asia_like_list_inverse="$all_africa"

#  africa_obverse="$all_africa"
  africa_obverse="$iiasa_central_africa,$iiasa_northern_africa,$iiasa_western_africa"
    asia_obverse="$iiasa_middle_east,$iiasa_southeast_asia,$iiasa_south_asia,$iiasa_central_asia,$iiasa_eastern_africa,$iiasa_southern_africa" # dropping east asia


    # set the extent based off of the projected yield
    climate=`echo "$climate_list" | grep -v "^$" | head -n 1`
       needs_snx=`echo "$run_list" | grep -v "^$" | head -n 1 | sed "s/$cli_ph/$climate/g"`
    align_raster=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

  # build the raw mask for west and central africa_like
  make_country_mask.sh "$africa_obverse" $align_raster deleteme_central_africa_like_NEW_raw

  # grow it out a little to catch everything
  r.grow input=deleteme_central_africa_like_NEW_raw output=MASK_for_groundnuts_central_africa_like_NEW radius=$grow_radius --o


  # now the asia like and east and southern africa
  make_country_mask.sh "$asia_obverse"   $align_raster deleteme_asia_like_NEW_raw

  # grow it out a little to catch everything
  r.grow input=deleteme_asia_like_NEW_raw output=MASK_for_groundnuts_asia_like_NEW radius=$grow_radius --o

  # ok, for everything else, we actually need to free ourselves from the shackles of the vectors....
  if [ "do it the old way" = "nope" ]; then
    # everything else
    make_country_mask.sh "$africa_obverse,$asia_obverse"   $align_raster deleteme_everything_else_NEW_raw INVERT

    # grow it out a little to catch everything
    r.grow input=deleteme_everything_else_NEW_raw output=MASK_for_groundnuts_everything_else_NEW radius=$grow_radius --o
  else
    # always set the region!!!!
    g.region rast=$align_raster

    # first, find the pieces, then make it 1/null
    r.patch input=MASK_for_groundnuts_asia_like_NEW,MASK_for_groundnuts_central_africa_like_NEW output=deleteme_everything_else_NEW_raw --o --q
    r.mapcalc MASK_for_groundnuts_everything_else_NEW_tight = "if(isnull(deleteme_everything_else_NEW_raw),1,null())"
    r.grow input=MASK_for_groundnuts_everything_else_NEW_tight output=MASK_for_groundnuts_everything_else_NEW radius=$grow_radius --o
  fi
  

fi # end cutout





##################


for run_line in $run_list; do
  echo "-- $run_line --"

  for climate in $climate_list; do
    echo "---- $climate ----"

      needs_snx=${run_line/$cli_ph/$climate}
    junk_name_2=${needs_snx/$snx_ph/$output_tag}

    yield_rast=${junk_name_2}
    count_rast=count_for_${yield_rast}

    # Beware the MAGIC NUMBER!!! setting the region based off of the overall NCAN15 region
#    g.region rast=WHEA_physical_area@spam_14feb12; g.region zoom=WHEA_physical_area@spam_05nov09; g.region res=0:15 -a

       this_snx=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`
      this_yield=${needs_snx/$snx_ph/$this_snx}@$yield_mapset

     g.region rast=$this_yield

    # set the extent based off of the projected yield
#    g.region rast=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset
    # set the resolution off of the ME mask (possibly resampled)
#    g.region rast=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

    r.mapcalc $yield_rast = "0.0" 2>&1 | grep -v "%"
    r.mapcalc $count_rast = "0" 2>&1 | grep -v "%"


    for snx_me_line in $snx_me_list; do
      echo "-- $snx_me_line --"

       this_snx=`echo "$snx_me_line" | cut -f1`
      this_mask=`echo "$snx_me_line" | cut -f2`

      this_yield=${needs_snx/$snx_ph/$this_snx}@$yield_mapset

      r.mapcalc $count_rast = "$count_rast + (1 - isnull($this_mask))" 2>&1 | grep -v "%"
      r.mapcalc $yield_rast = "$yield_rast + (1 - isnull($this_mask)) * $this_yield" 2>&1 | grep -v "%"
    done # snx_me_line

    # now we need to do the division
    r.mapcalc $yield_rast = "$yield_rast / $count_rast" 2>&1 | grep -v "%"

  done # climate


done # run_line


