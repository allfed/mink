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

################################################
### make sure you match up the controls...   ###
###                                          ###
### output_tag -> run_list -> snx_me_list    ###
################################################



#output_tag=GEOcombo
output_tag=GEObsl
#output_tag=GEOheat

# some placeholders
snx_ph=SNXSNXSNX
#cli_ph=CLICLICLI


yield_mapset=`g.gisenv get=MAPSET`

run_list=\
"
`g.mlist rast pat=best_yield*ptH001**yield_mean | sed "s/ptH001/${snx_ph}/g"`
"

old="
`g.mlist rast pat=best_yield*ptH001*1995*yield_mean | sed "s/ptH001/${snx_ph}/g"`
`g.mlist rast pat=best_yield*ptH001*gfdl*yield_mean | sed "s/ptH001/${snx_ph}/g"`
`g.mlist rast pat=best_yield*ptH001*ipsl*yield_mean | sed "s/ptH001/${snx_ph}/g"`
"



echo "---- updating to grown, full-coverage megaenvironments ----"

older_normal_snx_me_list=\
"
ptJ001	potato_ME_32@mega_environments
ptJ002	potato_ME_31@mega_environments
ptJ003	potato_ME_22@mega_environments
ptJ004	potato_ME_21@mega_environments
ptJ005	potato_ME_12@mega_environments
ptJ006	potato_ME_11@mega_environments
"

old_normal_snx_me_list=\
"
ptH001	potato_ME_32@mega_environments
ptH002	potato_ME_31@mega_environments
ptH003	potato_ME_22@mega_environments
ptH004	potato_ME_21@mega_environments
ptH005	potato_ME_12@mega_environments
ptH006	potato_ME_11@mega_environments
"

normal_snx_me_list=\
"
ptH001	potato_ME_32@mega_environments
ptH002	potato_ME_31@mega_environments
ptH003	potato_ME_22@mega_environments
ptH004	potato_ME_21@mega_environments
ptH007	potato_ME_12@mega_environments
ptH008	potato_ME_11@mega_environments
"

heat_snx_me_list=\
"
ptH011	potato_ME_32@mega_environments
ptH012	potato_ME_31@mega_environments
ptH013	potato_ME_22@mega_environments
ptH014	potato_ME_21@mega_environments
ptH015	potato_ME_12@mega_environments
ptH016	potato_ME_11@mega_environments
"

normal_full_coverage_snx_me_list=\
"
ptH001	potato_ME_fullcoverage_32@mega_environments
ptH002	potato_ME_fullcoverage_31@mega_environments
ptH003	potato_ME_fullcoverage_22@mega_environments
ptH004	potato_ME_fullcoverage_21@mega_environments
ptH007	potato_ME_fullcoverage_12@mega_environments
ptH008	potato_ME_fullcoverage_11@mega_environments
"


snx_me_list=$normal_full_coverage_snx_me_list

#snx_me_list=$old_normal_snx_me_list


a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`


######################################################


for run_line in $run_list; do
  echo "== $run_line =="

#      needs_snx=${run_line/$cli_ph/$climate}
      needs_snx=${run_line}
#    junk_name_2=${needs_snx/$snx_ph/$output_tag}
    junk_name_2=${run_line/$snx_ph/$output_tag}



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
    done # snx_me_line

    # now we need to do the division
    r.mapcalc $yield_rast = "$yield_rast / $count_rast" 2>&1 | grep -v "%"


done # run_line


