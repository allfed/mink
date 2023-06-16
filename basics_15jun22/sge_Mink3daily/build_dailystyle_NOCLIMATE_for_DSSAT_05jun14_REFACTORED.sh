#!/bin/bash

# This script is designed to perform map calculations for GRASS GIS. 
# It redirects stderr to stdout, and filters out any lines with a % symbol to remove progress messages. 
# Input parameters define the regions and masks to be used. 
# It sets up constants and initial variables, processes the main control list for map calculations and exports the non-climate data.

# Function to apply map calculation
# The 2>&1 part is redirecting stderr to stdout. This means that both the standard output (stdout) and standard error (stderr) of the command will be piped to grep.
# The grep -v % part is filtering out any line that contains a %. This is often used to filter out progress messages when running GRASS GIS commands, as they usually include a % symbol. So, this script will print all output of the r.mapcalc command that does not include a %.
mapCalc() {
  r.mapcalc "$1" 2>&1 | grep -v "%"
}

# Input parameters
region_to_use=$1
main_control_list=$2
weather_mask=$3


# Set up constants and output directory
output_file="to_DSSAT/"

# Set up other constants
calendar_mapset="deltaPIKnov_from_daily_c"
soils_raster="soil_profile_number_int@DSSAT_essentials_12may11"
minimum_physical_area=.05
growing_radius=0
month_shifter_list="0"
initial_N="initial_soil_nitrogen_mass@DSSAT_essentials_12may11"
initial_root_weight="initial_root_mass@DSSAT_essentials_12may11"
initial_surface_weight="initial_surface_residue_mass@DSSAT_essentials_12may11"


# Set up constants and output directory
output_file="to_DSSAT/"

# Set up other constants
calendar_mapset="deltaPIKnov_from_daily_c"
soils_raster="soil_profile_number_int@DSSAT_essentials_12may11"
minimum_physical_area=.05
growing_radius=0
month_shifter_list="0"
initial_N="initial_soil_nitrogen_mass@DSSAT_essentials_12may11"
initial_root_weight="initial_root_mass@DSSAT_essentials_12may11"
initial_surface_weight="initial_surface_residue_mass@DSSAT_essentials_12may11"


# Define the list of non-climate factors
# Used for r.out.new calculation of text tables
nonclimate_list="$soils_raster,deleteme_planting_month,deleteme_N_to_use,deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$weather_mask"


# Set the DSSAT region
g.region $region_to_use

# deleteme_all is some big number that creates a dummy raster that calculates crop growing everywhere if large
mapCalc "deleteme_all = 100000" 

# Set nitrogen related maps
# these rasters are used for r.out.new calculation of text tables
mapCalc "deleteme_initial_N = $initial_N"
mapCalc "deleteme_initial_root_weight = $initial_root_weight"
mapCalc "deleteme_initial_surface_weight = $initial_surface_weight"


# Process each line in the main control list
for list_line in $main_control_list; do

  # Parse the list line to get all required parameters
  raster_for_mask=`echo "$list_line" | cut -f1`
        crop_name=`echo "$list_line" | cut -f2`
      description=`echo "$list_line" | cut -f3`
          N_level=`echo "$list_line" | cut -f4`
  calendar_prefix=`echo "$list_line" | cut -f5`

  # Remove any existing MASK
  g.remove MASK

  # first the mask raster
  g.region rast=$raster_for_mask
  nsres_from_raster=`g.region -g | grep nsres | cut -d= -f2`

  # second the desired region
  eval g.region $region_to_use
  nsres_from_region=`g.region -g | grep nsres | cut -d= -f2`

  raster_res_is_coarser_than_region_res=`echo "if($nsres_from_raster >= $nsres_from_region) {1} else {0}" | bc`

  # we should already be in the target region...
  if [ $raster_res_is_coarser_than_region_res = 1 ]; then
    # do it the old way using pin pricks
    echo "      __ masking pin-prick __"
    mapCalc "deleteme_initial_spam_ungrown = if($raster_for_mask >= $minimum_physical_area, 1, null())"
  else
    # do a statistical coarsening to make sure we catch everything
    echo "      __ masking coarsening __"
    r.resamp.stats input=$raster_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
    mapCalc "deleteme_initial_spam_ungrown = if(deleteme_coarse_mask_ungrown >= $minimum_physical_area, 1, null())"
  fi

  # also, choose the pixels around those deemed relevant; note that this stays in the original region
  #   so desired pixels on the edges will not have a buffer to the outside (this is usually not a big deal)
  r.grow input=deleteme_initial_spam_ungrown output=deleteme_crop_mask radius=$growing_radius --o
  # finally, make the final mask a clean 1's and nulls version of the previous step
  mapCalc "MASK = if(isnull(deleteme_crop_mask),null(),1)"


  # Set up other initial values
  mapCalc "deleteme_N_to_use = $N_level"

  # Process each month shifter
  # Iterate over the month shifters
  for month_shifter in $month_shifter_list
  do

    # Allow for specific months in the master control list by saying anything
    # that is less than two characters long gets interpreted literally.
    # This will more-or-less be an undocumented feature for the moment (17nov11)
    if [ ${#calendar_prefix} -le 2 ]; then
      # Assume this is either a number or a pre-defined raster
      original_planting_month_raster=$calendar_prefix
    else
      # Define the planting calendar with normal naming conventions
      original_planting_month_raster=${calendar_prefix}_noGCMcalendar@${calendar_mapset}
    fi

    # Convert the negative sign to an 'n' for use in the names
    text_month_shifter=${month_shifter/-/n}

    # Define the name for this particular offset
    planting_month_raster=${calendar_prefix}_${gcm}_p${text_month_shifter}
    # Compute the appropriate planting month by shifting the target month and then wrapping the months that
    # go outside of 1 to 12
    r.mapcalc deleteme_planting_month = "eval(cand_month = $original_planting_month_raster + ($month_shifter), \
                                              too_low_fixed  = if( cand_month    <=  0, 12 + cand_month,    cand_month    ), \
                                              too_high_fixed = if( too_low_fixed >= 13, too_low_fixed - 12, too_low_fixed ), \
                                              too_high_fixed \
                                             )"

    # Define the output file name
    real_output_file=${output_file}${planting_month_raster}_${crop_name}__${description}

    # Make a header file with all the names of the maps in order so we can figure out what they are later
    echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt

    # Display a status report on the screen
    echo " -- exporting $planting_month_raster $crop_name $description `date` --"

    # Export the non-climate data
    # This is what actually makes the giant text tables
    /usr/local/grass-6.5.svn/bin/r.out.new \
      input=${nonclimate_list} \
      output=${real_output_file} \
      -l


    # Record the settings for provenance
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
    echo "calendar_mapset=$calendar_mapset" >> ${real_output_file}.provenance.txt
    echo "soils_raster=$soils_raster" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "minimum_physical_area=$minimum_physical_area" >> ${real_output_file}.provenance.txt
    echo "growing_radius=$growing_radius" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "month_shifter_list=" >> ${real_output_file}.provenance.txt
    echo "$month_shifter_list" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "main_control_list=" >> ${real_output_file}.provenance.txt
    echo "$main_control_list" >> ${real_output_file}.provenance.txt


  done # end of month_shifter loop

done # end of list_line loop

# Remove any existing MASK
g.remove MASK
