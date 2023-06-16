#/bin/bash

# the idea is that for something like soybeans where we don't have region/variety correspondences defined,
# that we try them all and then optimize over the varieties. however, when moving to similar-but-not-the-same
# climate theme and variations (especially daily based) we need to keep the varieties the same since people
# won't know what the weather for the next year is...
#
# so, my plan is to take all the baselines together (say, in the *_yield_mean map), use that to pick the best
# variety for each location. then we assume that everyone will follow that mean approach blindly even if the
# climate changes underneath them (e.g., to more ENSOs or "real" climate change).
#
# this script takes the "optimized" maps as the regional definitions and then assigns the others as needed...

# now, one question is how to deal with the "no positive yield" cases. i think what i will do is to
# eliminate those and then grow out the positively yielding varieties to fill things in...


IFS="
"

snx_ph=,,,ZZZXXXZZZjilks,,,
combo_infix=GEOnnn


a_good_snx=sbK001

#BEST_variety_NNN_best_yield_KTEST___379_d0_ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_soybeans__irrigated_yield_mean
#BEST_variety_NNN_best_yield_KTEST___379_d0_ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_soybeans__rainfed_yield_mean

#best_variety_raster_list=`g.mlist rast pat=BEST_variety_NNN_*KTEST*1995*`
best_variety_raster_list=`g.mlist rast pat=BEST_variety_NNN_*MTEST*1995*`

for best_variety_raster in $best_variety_raster_list; do

  # cut up the best variety raster name to recover the pieces of the original yield raster names
  # Beware the MAGIC ASSUMPTION/NUMBER!!! i am assuming that the best variety thing has the form
  # BEST_variety_{code with no underscores}_{everything else}
  # so, i want underscore delimited and to keep everything from #4 on....
  no_best_variety=`echo "$best_variety_raster" | cut -d_ -f4-`

  # then, there should be three underscores in a row where the SNX file name used to be, so we should be
  # able to split there....
  front_of_constituent_yields=${no_best_variety%%___*}
   back_of_constituent_yields=${no_best_variety##*___}


  # figure out if this is rainfed or irrigated
  no_yield_mean=${best_variety_raster%_yield_mean}
  claimed_water_source=${no_yield_mean##*__}

  if   [ "$claimed_water_source" = "rainfed" ]; then
    ### rainfed ###
#    best_variety_raster=BEST_variety_NNN_PGFTHIRD___379_dSEARCHED_pgfsecond_1_pgf_1994_2016_p0_soybeans__rainfed_yield_mean
    #case_list=`g.mlist rast pat=${front_of_constituent_yields}__*${a_good_snx}RF*I_m?i*yield_mean | sed "s/${a_good_snx}/$snx_ph/g"`
    case_list=`g.mlist rast pat=${front_of_constituent_yields}__*${a_good_snx}RF**yield_mean | sed "s/${a_good_snx}/$snx_ph/g"`

  elif [ "$claimed_water_source" = "irrigated" ]; then
    ### irrigated ###
#    best_variety_raster=BEST_variety_NNN_PGFTHIRD___379_dSEARCHED_pgfsecond_1_pgf_1994_2016_p0_soybeans__irrigated_yield_mean
#    case_list=`g.mlist rast pat=*${a_good_snx}IR*yield_mean | sed "s/${a_good_snx}/$snx_ph/g"`
    #case_list=`g.mlist rast pat=${front_of_constituent_yields}__*${a_good_snx}IR*I_m?i*yield_mean | sed "s/${a_good_snx}/$snx_ph/g"`
    case_list=`g.mlist rast pat=${front_of_constituent_yields}__*${a_good_snx}IR***yield_mean | sed "s/${a_good_snx}/$snx_ph/g"`

  else
    ### unexpected ###
    echo "sometihng incredibly stupid happened: claimed_water_source=[$claimed_water_source]"
    exit
  fi


echo "[$case_list]"



#####################################################
# you should not have to change anything below here #
#####################################################


# pick our region to make sense...
g.region rast=$best_variety_raster


# first, make a copy and get rid of the zero yield pixels. we will grow out a ways to try to pick those back up...
g.copy $best_variety_raster,deleteme_reduced_best_varieties --o

r.null deleteme_reduced_best_varieties setnull=0 --o

grow_radius=50
r.grow deleteme_reduced_best_varieties output=deleteme_reduced_best_varieties_grown radius=$grow_radius --o


# figure out the varieties present...
# this should get us: value<tab>SNX_string_no_IR_RF
raw_category_list=`r.category deleteme_reduced_best_varieties`


for particular_case in $case_list; do

  echo "== $particular_case =="

  # define the new raster
  final_yield=${particular_case/$snx_ph/$combo_infix}
  r.mapcalc $final_yield = "null()" 2>&1 | grep -v "%"

  # go through the varieties and put them in
  for variety_line in $raw_category_list; do
    variety_category=`echo "$variety_line" | cut -f1`
      variety_string=`echo "$variety_line" | cut -f2`

    echo "   -- $variety_category / $variety_string --"

    this_yield=${particular_case/$snx_ph/$variety_string}

    r.mapcalc $final_yield = "if(deleteme_reduced_best_varieties_grown == $variety_category, $this_yield, $final_yield)" 2>&1 | grep -v "%"

  done # variety line

  zero_to_black.sh $final_yield

  # write down how this happened...
  r.support $final_yield history="created by $0"
  r.support $final_yield history="based on $best_variety_raster"





done # particular case



done # best_variety_raster



