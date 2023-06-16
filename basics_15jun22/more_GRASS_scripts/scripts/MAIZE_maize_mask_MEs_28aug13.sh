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
nonsense_single_pass
"

#output_tag=GEOcombo
#output_tag=GEObsl
output_tag=GEOfixmaize

# some placeholders
snx_ph=SNXSNXSNX
cli_ph=CLICLICLI



yield_mapset=`g.gisenv get=MAPSET`


run_list=\
"
`g.mlist rast pat=best_yield_*__mzK015**yield_mean | sed "s/mzK015/${snx_ph}/g"`
"


# getting hit again by the not-quite-full-coverage
# use the 5 variety to fill in....

# something about the coarse resolution is making us drop edge pixels. grrr.... so, let's just do this coarsely...
# g.region n=85 s=-65 w=-180 e=180 res=0.5 # res=0.0833333333333333
# OLD: #r.patch --o input=`g.mlist rast pat=simplegrownspamres_ME_mme_[0-9]* mapset=mega_environments | tr "\n" ","` output=deleteme_maize_me_covers_here

#r.patch --o input=simplegrownspamres_ME_mme_1@mega_environments,simplegrownspamres_ME_mme_2@mega_environments,simplegrownspamres_ME_mme_3@mega_environments,simplegrownspamres_ME_mme_4@mega_environments,simplegrownspamres_ME_mme_5@mega_environments,simplegrownspamres_ME_mme_6@mega_environments,simplegrownspamres_ME_mme_7@mega_environments,simplegrownspamres_ME_mme_8@mega_environments,simplegrownspamres_ME_mme_9@mega_environments,simplegrownspamres_ME_mme_10@mega_environments,simplegrownspamres_ME_mme_11@mega_environments,simplegrownspamres_ME_mme_12@mega_environments,simplegrownspamres_ME_mme_13@mega_environments,simplegrownspamres_ME_mme_14@mega_environments,simplegrownspamres_ME_mme_15@mega_environments output=deleteme_maize_me_covers_here

#r.mapcalc deleteme_simplegrownspamres_ME_mme_ALLELSE_ungrown = "if(isnull(deleteme_maize_me_covers_here),1,null())"
#r.grow input=deleteme_simplegrownspamres_ME_mme_ALLELSE_ungrown output=simplegrownspamres_ME_mme_ALLELSE radius=2 --o

if [ 0 = 1 ]; then
  g.region n=85 s=-65 w=-180 e=180 res=0.5 # res=0.0833333333333333
  r.mapcalc deleteme_count_thing = "0"

for (( iii=1 ; iii <= 15 ; iii++ )); do

  r.mapcalc deleteme_count_thing = "deleteme_count_thing + (1 - isnull(simplegrownspamres_ME_mme_${iii}@mega_environments))"

done

# and the all-else

  r.mapcalc deleteme_count_thing = "deleteme_count_thing + (1 - isnull(simplegrownspamres_ME_mme_ALLELSE@mega_environments))"
exit

fi # cutout for counting...



old_thru_ftest_snx_me_list=\
"
mzK015	spamres_mme_1@mega_environments

mzK013	spamres_mme_2@mega_environments
mzK017	spamres_mme_2@mega_environments

mzK014	spamres_mme_3@mega_environments
mzK017	spamres_mme_3@mega_environments

mzK016	spamres_mme_4@mega_environments

mzK014	spamres_mme_5@mega_environments
mzK018	spamres_mme_5@mega_environments

mzK014	spamres_mme_6@mega_environments

mzK029	spamres_mme_7@mega_environments

mzK027	spamres_mme_8@mega_environments

mzK021	spamres_mme_9@mega_environments

mzK021	spamres_mme_10@mega_environments

mzK023	spamres_mme_11@mega_environments

mzK024	spamres_mme_12@mega_environments

mzK014	spamres_mme_13@mega_environments

mzK025	spamres_mme_14@mega_environments

mzK026	spamres_mme_15@mega_environments
"




spamres_snx_me_list=\
"
mzK014	simplegrownspamres_ME_mme_ALLELSE@mega_environments
mzK018	simplegrownspamres_ME_mme_ALLELSE@mega_environments

mzK015	simplegrownspamres_ME_mme_1@mega_environments

mzK013	simplegrownspamres_ME_mme_2@mega_environments
mzK017	simplegrownspamres_ME_mme_2@mega_environments

mzK014	simplegrownspamres_ME_mme_3@mega_environments
mzK017	simplegrownspamres_ME_mme_3@mega_environments

mzK016	simplegrownspamres_ME_mme_4@mega_environments

mzK014	simplegrownspamres_ME_mme_5@mega_environments
mzK018	simplegrownspamres_ME_mme_5@mega_environments

mzK014	simplegrownspamres_ME_mme_6@mega_environments

mzK029	simplegrownspamres_ME_mme_7@mega_environments

mzK027	simplegrownspamres_ME_mme_8@mega_environments

mzK021	simplegrownspamres_ME_mme_9@mega_environments

mzK021	simplegrownspamres_ME_mme_10@mega_environments

mzK023	simplegrownspamres_ME_mme_11@mega_environments

mzK024	simplegrownspamres_ME_mme_12@mega_environments

mzK014	simplegrownspamres_ME_mme_13@mega_environments

mzK025	simplegrownspamres_ME_mme_14@mega_environments

mzK026	simplegrownspamres_ME_mme_15@mega_environments
"


snx_me_list=\
"
mzK014	halfdegree_simplegrownspamres_ME_mme_ALLELSE@mega_environments
mzK018	halfdegree_simplegrownspamres_ME_mme_ALLELSE@mega_environments

mzK015	halfdegree_simplegrownspamres_ME_mme_1@mega_environments

mzK013	halfdegree_simplegrownspamres_ME_mme_2@mega_environments
mzK017	halfdegree_simplegrownspamres_ME_mme_2@mega_environments

mzK014	halfdegree_simplegrownspamres_ME_mme_3@mega_environments
mzK017	halfdegree_simplegrownspamres_ME_mme_3@mega_environments

mzK016	halfdegree_simplegrownspamres_ME_mme_4@mega_environments

mzK014	halfdegree_simplegrownspamres_ME_mme_5@mega_environments
mzK018	halfdegree_simplegrownspamres_ME_mme_5@mega_environments

mzK014	halfdegree_simplegrownspamres_ME_mme_6@mega_environments

mzK029	halfdegree_simplegrownspamres_ME_mme_7@mega_environments

mzK027	halfdegree_simplegrownspamres_ME_mme_8@mega_environments

mzK021	halfdegree_simplegrownspamres_ME_mme_9@mega_environments

mzK021	halfdegree_simplegrownspamres_ME_mme_10@mega_environments

mzK023	halfdegree_simplegrownspamres_ME_mme_11@mega_environments

mzK024	halfdegree_simplegrownspamres_ME_mme_12@mega_environments

mzK014	halfdegree_simplegrownspamres_ME_mme_13@mega_environments

mzK025	halfdegree_simplegrownspamres_ME_mme_14@mega_environments

mzK026	halfdegree_simplegrownspamres_ME_mme_15@mega_environments
"



if [ 0 = 1 ]; then
#  g.region n=85 s=-65 w=-180 e=180 res=0.5 # res=0.0833333333333333

#g.region n=70 s=-55 w=-180 e=180 res=0.5
#g.region n=85 s=-65 w=-180 e=180 res=0.5
#g.region n=70 s=-55 w=-180 e=180 res=0:05
#g.region n=83:40N s=59:30S w=-180 e=180 res=0:01

  g.region rast=KTEST__mzK026RF_379_d0_ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_maize__rainfed_yield_mean

  r.mapcalc deleteme_count_thing = "0"

for snx_me_line in $snx_me_list; do

       this_snx=`echo "$snx_me_line" | cut -f1`
      this_mask=`echo "$snx_me_line" | cut -f2`

  r.mapcalc deleteme_count_thing = "deleteme_count_thing + (1 - isnull($this_mask))"

#  quick_display.sh deleteme_count_thing
#  echo "-- $this_mask --"
#  r.report deleteme_count_thing units=c,p

done


exit

fi # cutout for counting...






CLEAR_COUNTS=0 # clear out the counts maps after they are used to save some space

#a_valid_SNX=mzF013
a_valid_SNX=mzK014
#a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`


######################################################


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

    r.mapcalc $yield_rast = "0.0" 2>&1 | grep -v "%"
    r.mapcalc $count_rast = "0" 2>&1 | grep -v "%"

    for snx_me_line in $snx_me_list; do
      echo "-- $snx_me_line --"

       this_snx=`echo "$snx_me_line" | cut -f1`
      this_mask=`echo "$snx_me_line" | cut -f2`

      this_yield=${needs_snx/$snx_ph/$this_snx}@$yield_mapset

      r.mapcalc $count_rast = "$count_rast + (1 - isnull($this_mask))" 2>&1 | grep -v "%"
      r.mapcalc $yield_rast = "$yield_rast + (1 - isnull($this_mask)) * $this_yield" 2>&1 | grep -v "%"

#      r.univar $yield_rast -g --q | grep "^n="
      #quick_display.sh $count_rast
#      quick_display.sh $yield_rast
#      read -p "hit return to step through... " junk

    done # snx_me_line

    # now we need to do the division
    r.mapcalc $yield_rast = "$yield_rast / $count_rast" 2>&1 | grep -v "%"

      r.univar $yield_rast -g --q | grep "^n="
      #quick_display.sh $count_rast
#      quick_display.sh $yield_rast
#      read -p "FINAL DONE; hit return to move on... " junk

    # check to see if we should clear the counts to save some space
    if [ $CLEAR_COUNTS = 1 ]; then
      g.remove rast=$count_rast
    fi

  done # climate


done # run_line


