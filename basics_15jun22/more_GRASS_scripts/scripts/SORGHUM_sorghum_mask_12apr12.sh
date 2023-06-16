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
"
singe_pass_nonsense
"


# some placeholders
snx_ph=SNXSNXSNX
cli_ph=CLICLICLI


yield_mapset=`g.gisenv get=MAPSET`


run_list=\
"
`g.mlist rast pat=best_yield*sgK001**yield_mean  mapset=$yield_mapset | sed "s/sgK001/${snx_ph}/g"`
"










#output_tag=GEObsl
output_tag=GEObslsorghum

snx_me_list=\
"
sgK001	MASK_for_sorghum_africa_like
sgK002	MASK_for_sorghum_africa_like

sgK003	MASK_for_sorghum_asia_like

sgK003	MASK_for_sorghum_everything_else



"

if [ 0 = 1 ]; then

output_tag=GEObay

snx_me_list=\
"
sgK004	MASK_for_sorghum_africa_like
sgK005	MASK_for_sorghum_africa_like

sgK006	MASK_for_sorghum_asia_like
"

fi # cutout

# build up the country masks
#a_valid_SNX=sgK001
a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`

if [ 1 = 1 ]; then


  grow_radius=2

  . SOME_country_groupings.sh

  # let us do this. the africa like countries will be everything that is NOT asia
  # while the asia like countries will be everything that is NOT africa
  # thus, we will be using an average for the USAs, chinas, etc

  africa_like_list_inverse="$iiasa_middle_east,$iiasa_southeast_asia,$iiasa_south_asia,$iiasa_east_asia,$iiasa_central_asia" # error discovered 08jun15: centrA_asia instead of central_asia

    asia_like_list_inverse="$all_africa"


    # set the extent based off of the projected yield
    climate=`echo "$climate_list" | grep -v "^$" | head -n 1`
       needs_snx=`echo "$run_list" | grep -v "^$" | head -n 1 | sed "s/$cli_ph/$climate/g"`
    align_raster=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

  # build the raw mask for africa_like
  make_country_mask.sh "$africa_like_list_inverse" $align_raster deleteme_africa_like_raw invert

  # grow it out a little to catch everything
  r.grow input=deleteme_africa_like_raw output=MASK_for_sorghum_africa_like radius=$grow_radius --o

  # now the asia like
  make_country_mask.sh "$asia_like_list_inverse" $align_raster deleteme_asia_like_raw invert

  # grow it out a little to catch everything
  r.grow input=deleteme_asia_like_raw output=MASK_for_sorghum_asia_like radius=$grow_radius --o


  # we need an everything else to catch those weird edges or islands
  # always set the region!!!!
    g.region rast=$align_raster

    # first, find the pieces, then make it 1/null
    r.patch input=MASK_for_sorghum_asia_like,MASK_for_sorghum_africa_like output=deleteme_everything_else_NEW_raw --o --q
    r.mapcalc MASK_for_sorghum_everything_else_NEW_tight = "if(isnull(deleteme_everything_else_NEW_raw),1,null())"
    r.grow input=MASK_for_sorghum_everything_else_NEW_tight output=MASK_for_sorghum_everything_else radius=$grow_radius --o




fi # end cutout

for run_line in $run_list; do
  echo "-- $run_line --"

  for climate in $climate_list; do
    echo "---- $climate ----"

      needs_snx=${run_line/$cli_ph/$climate}
    junk_name_2=${needs_snx/$snx_ph/$output_tag}

    yield_rast=${junk_name_2}
    count_rast=count_for_${yield_rast}

    # set the extent based off of the projected yield
    g.region rast=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

    # set the resolution off of the ME mask (possibly resampled)
    g.region rast=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

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


