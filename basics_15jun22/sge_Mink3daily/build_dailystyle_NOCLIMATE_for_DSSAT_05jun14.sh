#!/bin/bash
region_to_use=$1
main_control_list=$2
crop_area_raster=$3
minimum_physical_area=$4

set -e 
# this delay of 0.1s might be helpful, but is probably unnecessary
sleep 0.1

#
# This script is responsible for generating the following files in the output file (to_DSSAT) using r.out.new:
# [month]_[more details]_[run_name]header.txt
# [month]_[more details]_[run_name].cols.txt
# [month]_[more details]_[run_name]_geog.txt
# [month]_[more details]_[run_name]_geog.info.txt
# [month]_[more details]_[run_name]_data.txt
# [month]_[more details]_[run_name]_data.info.txt
# [month]_[more details]_[run_name].provenance.txt
#
# these txt files are data tables in txt format, easy for java to read and use for given latitudes and longitudes
#
# this is needed for the java programs to be able to know which regions have which properties for runtime
# these details are then imported into DSSAT
# 
# it gets these details by importing them from GRASS GIS maps, and the details passed in from the 

# echo ""
# echo "running build_dailystyle"
# echo ""

output_file=to_DSSAT/
r.mapcalc "crop_area_raster_zero_to_null = $crop_area_raster"
r.null setnull=0 map="crop_area_raster_zero_to_null"  # remove any zero values (set them to null)

weather_mask=crop_area_raster_zero_to_null
#### we should only need the calendar_mapset now with pre-existing daily weather...

calendar_mapset=deltaPIKnov_from_daily_c # which mapset contains the target planting month rasters

soils_raster=soil_profile_number_int@DSSAT_essentials_12may11 # which raster contains the soil numbers

#### elevation not needed at the moment because it should be in the daily weather files and
#### even then isn't necessary....
#elevation=base_2000_elev@$climate_mapset # which raster contains the elevation for each location


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
# minimum_physical_area=.05 # 0.05 # value in the masking raster to be considered relevant

growing_radius=0 # 0.0 # number of pixels


# make a simple raster to get everything...
# echo "    ++ making an ALL raster ++"
g.region $region_to_use

# echo ""
# echo "Region: "
# g.region -g
# echo ""

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
# echo "outputs being placed in = [$output_dir]" ; # display something to the screen
mkdir -p $output_dir   # create the directory, if necessary

other_initial=`echo -e "initial_soil_nitrogen_mass@DSSAT_essentials_12may11\tinitial_root_mass@DSSAT_essentials_12may11\tinitial_surface_residue_mass@DSSAT_essentials_12may11"`
wheat_revised_initial=`echo -e "initial_soil_nitrogen_mass_23may13@DSSAT_essentials_12may11\tinitial_root_mass@DSSAT_essentials_12may11\tinitial_surface_residue_mass@DSSAT_essentials_12may11"`
# now pick one of the crop cases
for spam_line in $main_control_list
do
  # pull out the different pieces of the line and store them separately for easy reference
  spam_raster_to_use_for_mask=`echo "$spam_line" | cut -f1`
                    crop_name=`echo "$spam_line" | cut -f2`
        output_stats_basename=`echo "$spam_line" | cut -f3`
                      N_level=`echo "$spam_line" | cut -f4`
              calendar_prefix=`echo "$spam_line" | cut -f5`

  if [ "$crop_name" = "wheat" ]; then
    simple_initial="$wheat_revised_initial"
  else
    simple_initial="$other_initial"
  fi


                initial_N=`echo "$simple_initial" | cut -f1`
      initial_root_weight=`echo "$simple_initial" | cut -f2`
   initial_surface_weight=`echo "$simple_initial" | cut -f3`
             #    id_raster=`echo "$spam_line" | cut -f9`
             # skip_climate=`echo "$spam_line" | cut -f10`


  # define the appropriate mask
  r.mapcalc "deleteme_raster_representing_region=1" 
  # echo spam_raster_to_use_for_mask
  # echo $spam_raster_to_use_for_mask
  # run the masking bash script to only generate data for regions where this crop variety grows
  ./prerun_scripts/mask_unwanted_pixels.sh $spam_raster_to_use_for_mask deleteme_raster_representing_region $minimum_physical_area $growing_radius

  r.mapcalc deleteme_N_to_use = "$N_level" 2>&1 

  # make some fake rasters for initial conditions
  r.mapcalc deleteme_initial_N              = "$initial_N" 2>&1 #| grep -v "%"
  r.mapcalc deleteme_initial_root_weight    = "$initial_root_weight" 2>&1 #| grep -v "%"
  r.mapcalc deleteme_initial_surface_weight = "$initial_surface_weight" 2>&1 #| grep -v "%"

  # let us allow for specific months in the master control list by saying anything
  # that is less than two characters long gets interpreted literally
  # this will more-or-less be an undocumented feature for the moment (17nov11)

  if [ ${#calendar_prefix} -le 2 ]; then
    # just hope that this is either a number or a pre-defined raster...
    original_planting_month_raster=$calendar_prefix
  else
    # proceed as normal

    # define the planting calendar
    original_planting_month_raster=${calendar_prefix}_noGCMcalendar@${calendar_mapset}
  fi

  # define the name for this particular offset
  planting_month_raster=${calendar_prefix}_noGCMcalendar_p0
  
  # compute the appropriate planting month by shifting the target month and then wrapping the months that
  # go outside of 1 to 12
  # if original_planting_month_raster is a constant, this just fills the whole masked area as a raster containing a number corresponding to the month represented by the constant
  r.mapcalc "${original_planting_month_raster}_0" = "eval(cand_month = $original_planting_month_raster, \
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
  nonclimate_list="$soils_raster,${original_planting_month_raster}_0,deleteme_N_to_use,$initial_N,$initial_root_weight,$initial_surface_weight,$weather_mask"

  # do the exporting
  # define the name of the output file
  start_output_name=${output_file}${output_stats_basename}
  real_output_file=${start_output_name}

  # make a header file with all the names of the maps in order so we can figure out what they are later
  echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt

  # exporting $planting_month_raster $crop_name $description 


  # Beware the MAGIC PATH!!! this is a non-standard grass program. so you need to know the actual path
  # this is what actually makes the giant text tables
  # test part is just saying we don't mind if this throws errors (always seems to even if correct operation)
  /usr/local/grass-6.5.svn/bin/r.out.new \
     input=${nonclimate_list} \
     output=${real_output_file} \
     -l || test $? = 1 

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
  echo "" >> ${real_output_file}.provenance.txt
  echo "main_control_list=" >> ${real_output_file}.provenance.txt
  echo "$main_control_list" >> ${real_output_file}.provenance.txt

  # echo "more info at ${real_output_file}.provenance.txt"

done # spam_line


#this says to remove any mask! so that's because we don't expect to see any more grass gis being used until post-processing, and this allows for consistency, rather than just being left with the mask that happened to be for the last cultivar (mask_unwanted_pixels was the last script to set the mask)
g.remove MASK #2>&1 | grep -v "" # silenced


# echo ""
# echo "done running build_dailystyle"
# echo ""
