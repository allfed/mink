#!/bin/bash
. default_paths_etc.sh
region_to_use=$1
main_control_list=$2
crop_area_raster=$3

echo "region_to_use"
echo $region_to_use
echo "weather_mask"
echo $weather_mask
# previous generation was NEW GISbox:
#   /home/grass/grass_scripts/gf_crop_modeling_01mar11/build_dataset_for_DSSAT_BigInits_03nov11.sh
#   /home/grass/grass_scripts/gf_crop_modeling_01mar11/build_dataset_for_DSSAT_monthly_neighborhood_01mar11.sh
# previous generation was old GISbox:
#   /home/grass/grass_scripts/crop_modeling/ricky_DSSAT/improved_calendars_15dec09
#   export_MONTHLY_neighborhood_29mar10.sh


# the purpose of this is to create a single table for me
# to use for the crop modeling
# BUT: now we have added some more things to do the soil initial conditions a little more better-er
# BUT: furthermore, i am going to use this to build a pre-generated daily weather thing to test with
#            the to-be-developed daily weather Mink2 AND i'm going to change the way that masking gets
#            done to be a little more robust
# BUT: now this should work nicely with data we get from someone else...


# the output_file is the name to be used when writing out the dataset. 
# the name should have the form directory/prefix_ (ending in an underscore)
# this allows you to give a unique name to all the myriad little
# details that cannot be easily incorporated into the filename directly
#
# typically, this name will be paired primarily with a geographic region
# and resolution
#
# the weather_mask provides a way of telling where we actually have weather data for and not. it should be missing values
#   where we don't and have a valid value where we do
# 1.875
# 1.25
echo "output_file_dir"
echo "${output_file_dir}"
output_file=${output_file_dir}
 weather_mask="${crop_area_raster}@morgan_DSSAT_cat_0" ; #region_to_use="n=90 s=-90 w=-180 e=180 nsres=1.875 ewres=2.5" #eegion_to_use="n=49 s=26 w=-124 e=-66 nsres=1.875 ewres=1.25" # now forcing a square resolution region_to_use="n=90 s=-90 w=-180 e=180 nsres=1.875 ewres=2.5" # NOTE: region_to_use SHOULD BE SET BEFORE RUNNING THIS SCRIPT
# output_file=to_DSSAT/D_ ; weather_mask=MAIZE_cropland@morgan_DSSAT_cat_0 ; #region_to_use="n=90 s=-90 w=-180 e=180 nsres=1.875 ewres=2.5" #eegion_to_use="n=49 s=26 w=-124 e=-66 nsres=1.875 ewres=1.25" # now forcing a square resolution region_to_use="n=90 s=-90 w=-180 e=180 nsres=1.875 ewres=2.5" # NOTE: region_to_use SHOULD BE SET BEFORE RUNNING THIS SCRIPT
echo "weather_mask"
echo $weather_mask
#### we should only need the calendar_mapset now with pre-existing daily weather...

calendar_mapset=deltaPIKnov_from_daily_c # which mapset contains the target planting month rasters

soils_raster=soil_profile_number_int@DSSAT_essentials_12may11 # which raster contains the soil numbers

#### elevation not needed at the moment because it should be in the daily weather files and
#### even then isn't necessary....
#elevation=base_2000_elev@$climate_mapset # which raster contains the elevation for each location


# the gcm_list is the list of GCMs that we wish to build datasets for. the options
#    are base_2000 and then {cnr,csi,ech,mir}_{a1,a2,b1}_{2030,2050,2080}
#    put each one you want on a different line

gcm_list=\
"
noGCMcalendar
"

# masking settings
# you can do some masking of the area you want to extract using these
# settings. in the next control list, you will specify a raster map.
# we have used the SPAM product (http://mapspam.info/) to helpfully identify
# locations likely to be important for a particular crop. then, we can choose
# a minimum value that we want that raster to have. the SPAM physical area
# maps have missing values, zeros, and values from 0.1 ha/pixel and up.
# thus, choosing something like 0.05 will get all the pixels with positive values
# (the criterion is raster >= threshold). or, if you want fewer locations, you
# might choose 50 for 50 ha/pixel.
#
# you can also include a "buffer" around those initial chosen location. this
# is specified as a number of pixels
#
# i have now changed the way the masking goes, so the radius is in terms of
# the resolution of the TARGET REGION (not the original masking raster)

# this is ha in a SPAM pixel needed to be considered relevant
minimum_physical_area=100 # 0.05 # value in the masking raster to be considered relevant

growing_radius=0 # 0.0 # number of pixels


# make a simple raster to get everything...
echo "    ++ making an ALL raster ++"
g.region $region_to_use
r.mapcalc deleteme_all = "100000" 2>&1 | grep -v % # some big number that creates a dummy raster that calculates crop growing everywhere if large...


# this is the main control where we list the crop cases we wish to model
# the columns are tab delimited...
#
# masking raster: historically, we have used the SPAM physical areas to
#    restrict the pixels modeled to those which are likely to be intereseting
#    this is the raster that will be used as the inputs on which the masking
#    and buffering will be done. if you don't want any masking, put in a raster
#    which has values at all locations (e.g., r.mapcalc everywhere_mask = "1" and
#    then put everywhere_mask in this spot.)
#
# crop name: this gives you a chance to put a human readable crop name in so you can
#    remember what you were trying to do

# water source: rainfed or irrigated. this gives you a chance to name the way you intend
#    to do the water since often there are different input choices (e.g., fertilizer).
#
# nitrogen fertilizer raster: this is a raster that specifies the total fertilizer application
#    for each location. this can be a real raster, or a value (e.g., "300").
#
# planting month calendar prefix: this is the first part of the name of the map showing the
#    target planting month that we want to use. this is combined with the chosen GCM names
#    and should be found in the calendar_mapset. the available choices at the time of writing
#    are: unified_irrigated, unified_rainfed, unified_spring_wheat, and (possibly) unified_winter_wheat
#
# initial nitrogen in soil: this is a map or value to indicate the amount of nitrogen that is originally
#    in the soil before we start the simulation. the units are kg/ha elemental nitrogen
#
# root weight from prior activities: this is a map or value to indicate the amount of root mass in
#    the soil prior to this season. the units are kg/ha
#
# surface residue weight from prior activities: this is a map or value to indicate the amount of
#    crop residues left on the field prior to this season. the units are kg/ha
#
### i'm not going to do ID_numbers anymore since the weather files should have the lat/lon in the name
### and the whole point of this script is to *always* skip the weather generation step....
# ID_number map: this needs to have a unique value for each pixel; put in the word "CREATE_PLEASE" to have
#    it automatically generated
#
# [skip_climate]: if there is something here, the climate table will not be created. the idea is that then i can
#    make one giant set of daily weather for the region with a single line and then reuse those for multiple crop
#    cases...

simple_initial=`echo -e "initial_soil_nitrogen_mass@DSSAT_essentials_12may11\tinitial_root_mass@DSSAT_essentials_12may11\tinitial_surface_residue_mass@DSSAT_essentials_12may11"`
wheat_simple_initial="2*initial_soil_nitrogen_mass@DSSAT_essentials_12may11 initial_root_mass@DSSAT_essentials_12may11  initial_surface_residue_mass@DSSAT_essentials_12may11"
old_wheat_revised_initial="initial_soil_nitrogen_mass_07may13@DSSAT_essentials_12may11  initial_root_mass@DSSAT_essentials_12may11  initial_surface_residue_mass@DSSAT_essentials_12may11"
wheat_revised_initial="initial_soil_nitrogen_mass_23may13@DSSAT_essentials_12may11  initial_root_mass@DSSAT_essentials_12may11  initial_surface_residue_mass@DSSAT_essentials_12may11"
maize_initial=`echo -e "initial_soil_nitrogen_mass_13mar14@DSSAT_essentials_12may11\tinitial_root_mass@DSSAT_essentials_12may11\tinitial_surface_residue_mass@DSSAT_essentials_12may11"`


# this is a list of offsets from the target month that you would like to try.
# integers only please...
#  0 means the target month
# +1 means the month after the target month
# -1 means the month before the target month
month_shifter_list=\
"
0
"
# EXAMPLE
# month_shifter_list=\
# "
# 0
# 1
# -1
# "


echo -e "\n\n\n CHECK THE SPAM RASTERS: should be the 30may14 ones... \n\n\n"
sleep 1



#####################################################
# you should not have to change anything below here #
#####################################################

# reset the internal field separator; basically, items
# in a list are separated by newlines rather than
# generic whitespace
IFS="
"

# make sure the output directory exists
output_dir=${output_file%/*}  # extract the directory part out the output file
echo "outputs being placed in = [$output_dir]" ; # display something to the screen
mkdir -p $output_dir   # create the directory, if necessary




# pick one of the gcm cases from the list
for gcm in $gcm_list; do


# now pick one of the crop cases
for spam_line in $main_control_list
do
  # pull out the different pieces of the line and store them separately for easy reference
  spam_raster_to_use_for_mask=`echo "$spam_line" | cut -f1`
                    crop_name=`echo "$spam_line" | cut -f2`
                 water_source=`echo "$spam_line" | cut -f3`
                      N_level=`echo "$spam_line" | cut -f4`
              calendar_prefix=`echo "$spam_line" | cut -f5`
                    initial_N=`echo "$simple_initial" | cut -f1`
          initial_root_weight=`echo "$simple_initial" | cut -f2`
       initial_surface_weight=`echo "$simple_initial" | cut -f3`
                 #    id_raster=`echo "$spam_line" | cut -f9`
                 # skip_climate=`echo "$spam_line" | cut -f10`
  # echo "spam_raster_to_use_for_mask"
  # echo $spam_raster_to_use_for_mask
  # echo "crop_name"
  # echo $crop_name
  # echo "water_source"
  # echo $water_source
  # echo "N_level"
  # echo $N_level
  echo "calendar_prefix"
  echo $calendar_prefix
  # echo "initial_N"
  # echo $initial_N
  # echo "initial_root_weight"
  # echo $initial_root_weight
  # echo "initial_surface_weight"
  # echo $initial_surface_weight

  # define the appropriate mask
  echo ""
  echo "-- creating mask for $spam_raster_to_use_for_mask `date` --"
  g.remove MASK # clear out the GRASS magic mask name


  # find all the pixels that meet the criterion for being relevant
  # old way, just looking at pin-prick style
  #  g.region rast=$spam_raster_to_use_for_mask # set the region to match up with the desired raster
  # r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())"

  # we need to figure out which is coarser, the desired region or the masking source raster


  echo "    ++ masking ++"
  # first the mask raster
  g.region rast=$spam_raster_to_use_for_mask
  nsres_from_raster=`g.region -g | grep nsres | cut -d= -f2`

  # second the desired region
  eval g.region $region_to_use
  nsres_from_region=`g.region -g | grep nsres | cut -d= -f2`

  raster_res_is_coarser_than_region_res=`echo "if($nsres_from_raster >= $nsres_from_region) {1} else {0}" | bc`

  # we should already be in the target region...
  if [ $raster_res_is_coarser_than_region_res = 1 ]; then
    # do it the old way using pin pricks
    echo "      __ masking pin-prick __"
    r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
  else
    # do a statistical coarsening to make sure we catch everything
    echo "      __ masking coarsening __"
    r.resamp.stats input=$spam_raster_to_use_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
    r.mapcalc deleteme_initial_spam_ungrown = "if(deleteme_coarse_mask_ungrown >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
  fi
  



  # also, choose the pixels around those deemed relevant; note that this stays in the original region
  #   so desired pixels on the edges will not have a buffer to the outside (this is usually not a big deal)
  r.grow input=deleteme_initial_spam_ungrown output=deleteme_crop_mask radius=$growing_radius --o
  # finally, make the final mask a clean 1's and nulls version of the previous step
  r.mapcalc MASK = "if(isnull(deleteme_crop_mask),null(),1)" 2>&1 | grep -v %



  echo "    ++ N/inits ++"
#  # define the region to use
#  eval g.region $region_to_use

  # make some fake rasters for nitrogen; this allows us to use either a raster or a supplied value.
  # you could even use some sort of formula like "original_N * 1.25"
  r.mapcalc deleteme_N_to_use = "$N_level" 2>&1 | grep -v %

  # make some fake rasters for initial conditions
  r.mapcalc deleteme_initial_N              = "$initial_N" 2>&1 | grep -v "%"
  r.mapcalc deleteme_initial_root_weight    = "$initial_root_weight" 2>&1 | grep -v "%"
  r.mapcalc deleteme_initial_surface_weight = "$initial_surface_weight" 2>&1 | grep -v "%"


# here is where we go through the shifts against the target planting month
for month_shifter in $month_shifter_list
do

  # let us allow for specific months in the master control list by saying anything
  # that is less than two characters long gets interpreted literally
  # this will more-or-less be an undocumented feature for the moment (17nov11)

  if [ ${#calendar_prefix} -le 2 ]; then
    # just hope that this is either a number or a pre-defined raster...
    original_planting_month_raster=$calendar_prefix
  else
    # proceed as normal

    # define the planting calendar
    original_planting_month_raster=${calendar_prefix}_${gcm}@${calendar_mapset}
  fi

  text_month_shifter=${month_shifter/-/n} # convert the negative sign to an n for use in the names...

  # define the name for this particular offset
  planting_month_raster=${calendar_prefix}_${gcm}_p${text_month_shifter}

  # compute the appropriate planting month by shifting the target month and then wrapping the months that
  # go outside of 1 to 12
  r.mapcalc deleteme_planting_month = "eval(cand_month = $original_planting_month_raster + ($month_shifter), \
                                            too_low_fixed  = if( cand_month    <=  0, 12 + cand_month,    cand_month    ), \
                                            too_high_fixed = if( too_low_fixed >= 13, too_low_fixed - 12, too_low_fixed ), \
                                            too_high_fixed \
                                           )"



  ####################################
  # new stuff split up for daily ... #
  ####################################

  # we need to figure out the appropriate footprint that has everything.
  # then, we need to split up the pure climate stuff into one table and the
  # non-climate stuff into another. but they need to match up.


  # make the new, smaller lists...
    nonclimate_list="$soils_raster,deleteme_planting_month,deleteme_N_to_use,deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$weather_mask"

  # do the exporting
  # define the name of the output file
    start_output_name=${output_file}${planting_month_raster}_${crop_name}__${water_source}

    real_output_file=${start_output_name}
    echo "real output"
    echo $real_output_file

    # make a header file with all the names of the maps in order so we can figure out what they are later
    echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt

    # dump a status report out to the screen
    echo " -- exporting $planting_month_raster $crop_name $water_source `date` --"

  # skip the climate if requested... that is, always, now....
     # Beware the MAGIC PATH!!! this is a non-standard grass program. so you need to know the actual path
     # this is what actually makes the giant text tables
     /usr/local/grass-6.5.svn/bin/r.out.new \
       input=${nonclimate_list} \
       output=${real_output_file} \
       -l


  # try some provenance from here...
  # the idea here is to record all the important settings so that you can reproduce this dataset
  # later and/or have the information when you have to do a write-up about your results
    echo "--- provenance started at `date` ---" > ${real_output_file}.provenance.txt
    echo "this dir: `pwd`" >> ${real_output_file}.provenance.txt
    echo "this script: $0" >> ${real_output_file}.provenance.txt
    echo "nonclimate_list below" >> ${real_output_file}.provenance.txt
    echo "$nonclimate_list" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "`echo "$nonclimate_list" | sed "s/,/\n/g" | cat -n`" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "output_file = $output_file" >> ${real_output_file}.provenance.txt
    echo "region_to_use = $region_to_use" >> ${real_output_file}.provenance.txt
    echo "weather_mask = $weather_mask" >> ${real_output_file}.provenance.txt
#    echo "climate_mapset=$climate_mapset" >> ${real_output_file}.provenance.txt
    echo "calendar_mapset=$calendar_mapset" >> ${real_output_file}.provenance.txt
    echo "soils_raster=$soils_raster" >> ${real_output_file}.provenance.txt
#    echo "elevation=$elevation" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "minimum_physical_area=$minimum_physical_area" >> ${real_output_file}.provenance.txt
    echo "growing_radius=$growing_radius" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "month_shifter_list=" >> ${real_output_file}.provenance.txt
    echo "$month_shifter_list" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "main_control_list=" >> ${real_output_file}.provenance.txt
    echo "$main_control_list" >> ${real_output_file}.provenance.txt

done # month_shifter


done # spam_line

  g.remove MASK

done # gcm

exit
