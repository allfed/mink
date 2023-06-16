#!/bin/bash

# Function to extract a field from a string
getField() {
  echo "$1" | cut -f$2
}

# Function to mapcalc
mapCalc() {
  r.mapcalc "$1" 2>&1 | grep -v "%"
}

region_to_use=$1
main_control_list=$2
crop_area_raster=$3

output_file="to_DSSAT/"
calendar_mapset="deltaPIKnov_from_daily_c"
soils_raster="soil_profile_number_int@DSSAT_essentials_12may11"
gcm_list="noGCMcalendar"
minimum_physical_area=.05
growing_radius=0

simple_initial=`echo -e "initial_soil_nitrogen_mass@DSSAT_essentials_12may11\tinitial_root_mass@DSSAT_essentials_12may11\tinitial_surface_residue_mass@DSSAT_essentials_12may11"`

month_shifter_list="0"

output_dir=${output_file%/*}
mkdir -p $output_dir

for gcm in $gcm_list; do
  for spam_line in $main_control_list
  do
    spam_raster_to_use_for_mask=$(getField "$spam_line" 1)
    crop_name=$(getField "$spam_line" 2)
    water_source=$(getField "$spam_line" 3)
    N_level=$(getField "$spam_line" 4)
    calendar_prefix=$(getField "$spam_line" 5)
    initial_N=$(getField "$simple_initial" 1)
    initial_root_weight=$(getField "$simple_initial" 2)
    initial_surface_weight=$(getField "$simple_initial" 3)

    g.remove MASK

    mapCalc "deleteme_initial_spam_ungrown = if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())"
    r.resamp.stats input=$spam_raster_to_use_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
    mapCalc "deleteme_initial_spam_ungrown = if(deleteme_coarse_mask_ungrown >= $minimum_physical_area, 1, null())"
    mapCalc "MASK = if(isnull(deleteme_crop_mask),null(),1)"
    mapCalc "deleteme_N_to_use = $N_level"

    if [ -z "$variable" ]; then
      mapCalc "deleteme_initial_N = N_for_${crop_name}_${water_source}"
      mapCalc "deleteme_initial_N = $initial_N"
    fi

    mapCalc "deleteme_initial_root_weight = $initial_root_weight"
    mapCalc "deleteme_initial_surface_weight = $initial_surface_weight"

    for month_shifter in $month_shifter_list
    do

      # here is where we go through the shifts against the target planting month
      for month_shifter in $month_shifter_list
      do

          original_planting_month_raster=$calendar_prefix
          original_planting_month_raster=${calendar_prefix}_${gcm}@${calendar_mapset}
        fi

        text_month_shifter=${month_shifter/-/n} # convert the negative sign to an n for use in the names...
        planting_month_raster=${calendar_prefix}_${gcm}_p${text_month_shifter}
        r.mapcalc deleteme_planting_month = "eval(cand_month = $original_planting_month_raster + ($month_shifter), \
                                                  too_low_fixed  = if( cand_month    <=  0, 12 + cand_month,    cand_month    ), \
                                                  too_high_fixed = if( too_low_fixed >= 13, too_low_fixed - 12, too_low_fixed ), \
                                                  too_high_fixed \
                                                 )"



          nonclimate_list="$soils_raster,deleteme_planting_month,deleteme_N_to_use,deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$weather_mask"
          start_output_name=${output_file}${planting_month_raster}_${crop_name}__${water_source}

          real_output_file=${start_output_name}
          echo "real output"
          echo $real_output_file
          echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt
          echo " -- exporting $planting_month_raster $crop_name $water_source `date` --"
           /usr/local/grass-6.5.svn/bin/r.out.new \
             input=${nonclimate_list} \
             output=${real_output_file} \
             -l


      done # month_shifter
    done # month_shifter
  done # spam_line

  g.remove MASK

done # gcm

exit