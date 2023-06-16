#!/bin/bash


# so, we have to find some way to match up the wheat varieties with
# geographic regions. in theory, we have mega-environment masks that
# should do the trick, so here we go....


# now we also need to be able to handle different stuff for winter wheat planting months

# reset the IFS to lines
IFS="
"


# let's try to brute force this first and establish a pattern later.


# some placeholders
snx_ph=SNXSNXSNX

spring_winter_ph=SWSWSWSWSW


yield_mapset=`g.gisenv get=MAPSET`


spring_run_list=\
"
`g.mlist rast pat=best_yield_*__whK001**yield_mean | sed "s/whK001/${snx_ph}/g"`
"

winter_run_list=\
"
`g.mlist rast pat=best_yield_*__whK016**yield_mean | sed "s/whK016/${snx_ph}/g"`
"



#me1irri
#me2a
#me2b
#me3
#me4
#me4c
#me5
#me6
#me7
#me8
#me9
#me10
#me11
#me12


# var / mask / spring or winter string
original_snx_me_list=\
"
whC001	fortyfivemin_me1irri@mega_environments	spring
whC002	fortyfivemin_me1irri@mega_environments	spring

whC003	fortyfivemin_me2a@mega_environments	spring
whC004	fortyfivemin_me2b@mega_environments	spring

whC005	fortyfivemin_me3@mega_environments	spring

whC006	fortyfivemin_me4@mega_environments	spring
whC006	fortyfivemin_me9@mega_environments	spring

whC007	fortyfivemin_me4@mega_environments	spring
whC008	fortyfivemin_me4@mega_environments	spring

whC009	fortyfivemin_me5@mega_environments	spring
whC010	fortyfivemin_me5@mega_environments	spring

whC011	fortyfivemin_me6@mega_environments	spring

whC012	fortyfivemin_me7@mega_environments	spring

whC013	fortyfivemin_me8@mega_environments	spring
whC014	fortyfivemin_me8@mega_environments	spring

whC015	fortyfivemin_me10@mega_environments	winter
whC016	fortyfivemin_me10@mega_environments	winter

whC017	fortyfivemin_me11@mega_environments	winter
whC018	fortyfivemin_me11@mega_environments	winter

whC019	fortyfivemin_me12@mega_environments	winter
"




### baseline varieties (new winter as of 09nov12)
if [ 1 = 1 ]; then

output_tag=GEObslMEAN

# still getting bitten by not-full-coverage
# g.region n=85 s=-65 w=-180 e=180 res=0.0833333333333333

#r.patch --o input=`g.mlist rast pat=simplegrownspamres_ME_me[0-9]* mapset=mega_environments | tr "\n" ","` output=deleteme_wheat_me_covers_here
#r.mapcalc simplegrownspamres_ME_meALLELSE = "if(isnull(deleteme_wheat_me_covers_here),1,null())"
  


snx_me_list=\
"
whK002	simplegrownspamres_ME_me1irri@mega_environments	spring
whK001	simplegrownspamres_ME_me1irri@mega_environments	spring

whK002	simplegrownspamres_ME_me2a@mega_environments	spring
whK002	simplegrownspamres_ME_me2b@mega_environments	spring
whK002	simplegrownspamres_ME_meALLELSE@mega_environments	spring

whK002	simplegrownspamres_ME_me3@mega_environments	spring

whK006	simplegrownspamres_ME_me4@mega_environments	spring

whK007	simplegrownspamres_ME_me4@mega_environments	spring

whK009	simplegrownspamres_ME_me5@mega_environments	spring
whK010	simplegrownspamres_ME_me5@mega_environments	spring

whK011	simplegrownspamres_ME_me6_ugly_fix@mega_environments	spring

whK012	simplegrownspamres_ME_me7@mega_environments	spring

whK013	simplegrownspamres_ME_me8_ugly_fix@mega_environments	spring
whK012	simplegrownspamres_ME_me8_ugly_fix@mega_environments	spring

whK015	simplegrownspamres_ME_me9@mega_environments	spring

whK016	simplegrownspamres_ME_me10@mega_environments	winter

whK076	simplegrownspamres_ME_me11_ugly_fix@mega_environments	winter

whK016	simplegrownspamres_ME_me12@mega_environments	winter
"

# adapting for the simple model...
#snx_me_list=`echo "$snx_me_list" | sed "s/whK/whOsimple/g"`


# i have tried to grow out the MEs to get full
# coverage (01feb16); these are the old ones
old_snx_me_list=\
"
whK001	spamres_me1irri@mega_environments	spring
whK002	spamres_me1irri@mega_environments	spring

whK002	spamres_me2a@mega_environments	spring
whK002	spamres_me2b@mega_environments	spring

whK002	spamres_me3@mega_environments	spring

whK006	spamres_me4@mega_environments	spring

whK007	spamres_me4@mega_environments	spring

whK009	spamres_me5@mega_environments	spring
whK010	spamres_me5@mega_environments	spring

whK011	spamres_me6_ugly_fix@mega_environments	spring

whK012	spamres_me7@mega_environments	spring

whK013	spamres_me8_ugly_fix@mega_environments	spring
whK012	spamres_me8_ugly_fix@mega_environments	spring

whK015	spamres_me9@mega_environments	spring

whK016	spamres_me10@mega_environments	winter

whK076	spamres_me11_ugly_fix@mega_environments	winter

whK016	spamres_me12@mega_environments	winter
"

fi






######################################################



echo "--- MEAN STUFF ---"


a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`
 a_valid_SW=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f3`



# we need to be able to handle spring and winter separately
# and the old way won't cut it anymore

# first, eliminate the empty lines...
spring_clean=`echo "$spring_run_list" | grep -v "^$"`
winter_clean=`echo "$winter_run_list" | grep -v "^$"`

n_cases=`echo "$spring_clean" | wc -l`

# check to make sure spring and winter lists are the same length
# not quite foolproof, but hopefully close enough most of the time
n_cases_winter=`echo "$winter_clean" | wc -l`
if [ -z "$spring_clean" ]; then
  n_cases_spring=0
fi
if [ -z "$winter_clean" ]; then
  n_cases_winter=0
fi

if [ $n_cases_winter -ne $n_cases ]; then
  echo "n_cases_winter [$n_cases_winter] != [$n_cases] n_cases (spring)"
  exit
fi

average_list=""

for (( line_num=1 ; line_num <= $n_cases ; line_num++ )); do
  spring_line=`echo "$spring_clean" | sed -n "${line_num}p"`
  winter_line=`echo "$winter_clean" | sed -n "${line_num}p"`


  run_line=$spring_line
  echo "-- $run_line --"

      needs_snx=${run_line}
    junk_name_2=${needs_snx/$snx_ph/$output_tag}

    yield_rast=`echo "$junk_name_2" | sed "s/$spring_winter_ph//g"` # ${junk_name_2/$spring_winter_ph/}
    count_rast=count_for_${yield_rast}

    # set the extent based off of the projected yield
    a_valid_raster_name=`echo "$needs_snx" | sed "s/$snx_ph/$a_valid_SNX/g ; s/$spring_winter_ph/$a_valid_SW/g"`@$yield_mapset

# this was the old one....
    g.region rast=$a_valid_raster_name

    r.mapcalc $yield_rast = "0.0" 2>&1 | grep -v "%"
    r.mapcalc $count_rast = "0" 2>&1 | grep -v "%"
    r.mapcalc deleteme_something_valid_here = "null()" 2>&1 | grep -v "%"

    for snx_me_line in $snx_me_list; do
      echo "-- $snx_me_line --"

  # let's debug by pumping out the map as it progresses
       this_snx=`echo "$snx_me_line" | cut -f1`
      this_mask=`echo "$snx_me_line" | cut -f2`
        this_sw=`echo "$snx_me_line" | cut -f3`

      if [ "$this_sw" = "winter" ]; then
        # we are in a winter wheat situation
          needs_snx=${winter_line}
      else
        # we are in a spring wheat situation
          needs_snx=${spring_line}
      fi

      this_yield=`echo "$needs_snx" | sed "s/$snx_ph/$this_snx/g ; s/$spring_winter_ph/$this_sw/g"`@$yield_mapset

      r.mapcalc $count_rast = "$count_rast + (1 - isnull($this_mask))" 2>&1 | grep -v "%"
      r.mapcalc $yield_rast = "$yield_rast + (1 - isnull($this_mask)) * if(isnull($this_yield),0,$this_yield)" 2>&1 | grep -v "%"
      r.mapcalc deleteme_something_valid_here = "if(isnull(deleteme_something_valid_here),if(isnull($this_yield),null(),1),deleteme_something_valid_here)" 2>&1 | grep -v "%"

      r.mapcalc deleteme_this_contribution = "(1 - isnull($this_mask)) * if(isnull($this_yield),0,$this_yield)" 2>&1 | grep -v "%"

#      echo "$this_snx	$this_sw	$this_mask	$this_yield"
#      quick_display.sh deleteme_this_contribution

#      sleep 1.5s

    done # snx_me_line

    # now we need to do the division
    g.copy rast=$yield_rast,deleteme_before_division --o
    r.mapcalc $yield_rast = "$yield_rast / $count_rast * deleteme_something_valid_here" 2>&1 | grep -v "%"
    r.mapcalc $count_rast = "$count_rast * deleteme_something_valid_here" 2>&1 | grep -v "%"


  average_list="$average_list
$yield_rast"


done # run_line


####### end of mean stuff #########

####### now for the max stuff #####


#me1irri
#me2a
#me2b
#me3
#me4
#me4c
#me5
#me6
#me7
#me8
#me9
#me10
#me11
#me12


echo "--- MAX STUFF ---"

output_tag=${output_tag/MEAN/MAX}

a_valid_SNX=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f1`
 a_valid_SW=`echo "$snx_me_list" | grep -v "^$" | head -n 1 | cut -f3`


######################################################

# we need to be able to handle spring and winter separately
# and the old way won't cut it anymore

# first, eliminate the empty lines...
spring_clean=`echo "$spring_run_list" | grep -v "^$"`
winter_clean=`echo "$winter_run_list" | grep -v "^$"`

n_cases=`echo "$spring_clean" | wc -l`

max_list=""
#for run_line in $run_list; do
for (( line_num=1 ; line_num <= $n_cases ; line_num++ )); do
  spring_line=`echo "$spring_clean" | sed -n "${line_num}p"`
  winter_line=`echo "$winter_clean" | sed -n "${line_num}p"`


  run_line=$spring_line
  echo "-- $run_line --"

      needs_snx=${run_line}
    junk_name_2=${needs_snx/$snx_ph/$output_tag}

    yield_rast=`echo "$junk_name_2" | sed "s/$spring_winter_ph//g"` # ${junk_name_2/$spring_winter_ph/}
    winning_rast=winningvar_for_${yield_rast}

    # set the extent based off of the projected yield
    a_valid_raster_name=`echo "$needs_snx" | sed "s/$snx_ph/$a_valid_SNX/g ; s/$spring_winter_ph/$a_valid_SW/g"`@$yield_mapset

    g.region rast=$a_valid_raster_name

#    # set the resolution off of the ME mask (possibly resampled)
#    g.region rast=${needs_snx/$snx_ph/$a_valid_SNX}@$yield_mapset

    r.mapcalc $yield_rast = "0.0" 2>&1 | grep -v "%"
    r.mapcalc $winning_rast = "0" 2>&1 | grep -v "%"
    r.mapcalc deleteme_something_valid_here = "null()" 2>&1 | grep -v "%"


    counter=1 # reset variety/ME counter number
    category_string=""
    for snx_me_line in $snx_me_list; do
      echo "-- $snx_me_line ; $counter --"

  # let's debug by pumping out the map as it progresses
       this_snx=`echo "$snx_me_line" | cut -f1`
      this_mask=`echo "$snx_me_line" | cut -f2`
        this_sw=`echo "$snx_me_line" | cut -f3`

      if [ "$this_sw" = "winter" ]; then
        # we are in a winter wheat situation
          needs_snx=${winter_line}
      else
        # we are in a spring wheat situation
          needs_snx=${spring_line}
      fi

      this_yield=`echo "$needs_snx" | sed "s/$snx_ph/$this_snx/g ; s/$spring_winter_ph/$this_sw/g"`@$yield_mapset

echo "!!!! THROWING OUT THE MASK for a while; MAX is going to be MAX over everything, not just varieties deemed appropriate for this ME (winter problems....) !!!!"

      if [ "nope" = "do it the original way" ]; then
        r.mapcalc $yield_rast   = "max($yield_rast, (1 - isnull($this_mask)) * if(isnull($this_yield),0,$this_yield))" 2>&1 | grep -v "%"
  #      r.mapcalc deleteme_something_valid_here = "if(isnull(deleteme_something_valid_here),if(isnull($this_yield),null(),1),deleteme_something_valid_here)" 2>&1 | grep -v "%"
        r.mapcalc deleteme_something_valid_here = "if(isnull(deleteme_something_valid_here),if(isnull($this_yield * $this_mask),null(),1),deleteme_something_valid_here)" 2>&1 | grep -v "%"

        r.mapcalc $winning_rast = "if($yield_rast == (1 - isnull($this_mask)) * if(isnull($this_yield),0,$this_yield), $counter, $winning_rast)" 2>&1 | grep -v "%"  
      else
        r.mapcalc $yield_rast   = "max($yield_rast,                            if(isnull($this_yield),0,$this_yield))" 2>&1 | grep -v "%"
        r.mapcalc deleteme_something_valid_here = "if(isnull(deleteme_something_valid_here),if(isnull($this_yield             ),null(),1),deleteme_something_valid_here)" 2>&1 | grep -v "%"
        r.mapcalc $winning_rast = "if($yield_rast ==                            if(isnull($this_yield),0,$this_yield), $counter, $winning_rast)" 2>&1 | grep -v "%"  
      fi

      # build up a legend name for this
      category_string="${category_string}${counter}:$this_snx ${this_mask%%@*}
"

      let "counter++"
    done # snx_me_line

    # now we need to do the division
    g.copy rast=$yield_rast,deleteme_max_before_validation --o
    r.mapcalc $yield_rast   = "$yield_rast   * deleteme_something_valid_here" 2>&1 | grep -v "%"
    r.mapcalc $winning_rast = "$winning_rast * deleteme_something_valid_here" 2>&1 | grep -v "%"

    assign_categories_from_string.sh $winning_rast "$category_string"

  max_list="$max_list
$yield_rast"


done # run_line


### end of MAX stuff ###


echo "--- put them TOGETHER ---"





# the idea here is that for the winter wheat dominant countries, we need winter wheat
# to dominate. so we are arbitrarily enforcing a list of western european countries to
# use the highest yielding variety rather than the overall average of relevant varieties.


# let's make a csv list of goodies
winter_wheat_dominant_countries="Luxembourg,Belgium,Denmark,France,Germany,Ireland,Netherlands,United Kingdom"




output_prefix=wintermix_

###############

# clean up the lists

clean_average_list=`echo "$average_list" | grep -v "^$"`
    clean_max_list=`echo "$max_list"     | grep -v "^$"`

n_averages=`echo "$clean_average_list" | wc -l`

# define the region

first_average=`echo "$clean_average_list" | head -n 1`

g.region rast=$first_average

# make the winter mask
make_country_mask.sh "$winter_wheat_dominant_countries" $first_average deleteme_winter_wheat_dominant

# and reset the region again
g.region rast=$first_average

for (( line_number=1 ; line_number <= n_averages ; line_number++ )); do

  average_rast=`echo "$clean_average_list" | sed -n "${line_number}p"`
      max_rast=`echo "$clean_max_list"     | sed -n "${line_number}p"`

  r.mapcalc ${output_prefix}${average_rast} = "if(isnull(deleteme_winter_wheat_dominant),$average_rast,$max_rast)"

done







