#!/bin/bash

set -e

# where the mask is null, average a series of rasters. Where the mask is not null, find max of the raster used to look for max locations, and then take this max.

# an example is planting months: if we choose the max yield, we need to get the planting month where we chose the maximum yield. But if we choose the average yield, then we need to get the average of the planting months for relevant rasters (and the average planting month needs to remain an integer, so we need to round). Planting months would be "key" rasters, and yields are "value" rasters

# NOTE: to_combine_int_rasters and findmax_rasters are the same number of rasters!

to_combine_int_rasters=$1         # raster(s) to average or select from where the "findmax_rasters" is max, typically either some non-yield integer valued statistic outputted from DSSAT e.g. days to maturity, or planting months
mask=$2                           # mask, null if average, defined where want to max
method=$3                         # method to average with
findmax_rasters=$4                # rasters used to look for max locations (typically yields)
output_raster=$5                  # output raster name

# Create a mask with the specified raster
# r.mask raster=$mask --overwrite

./average_rasters.sh $to_combine_int_rasters "0" "" 

# Calculate average of the rasters, but round the result to the nearest integer 
r.series --overwrite input=$to_combine_int_rasters output="deleteme_avg_combined" method=average --quiet

# Calculate raster key of where best value raster is (this is also where the best key raster is)
r.series --overwrite input=$findmax_rasters output="deleteme_max_key_for_combining_rasters" method=max_raster --quiet

r.mapcalc "deleteme_avg_combined_integer = round(deleteme_avg_combined)"

# The IFS (Internal Field Separator) variable tells the read command to use the comma , as the delimiter. -ra tells the read command to read the input into an array.
IFS=',' read -ra to_combine_int_rasters_array <<< "$to_combine_int_rasters"

max_combined_raster_mapcalc=""
# Loop through the value rasters and construct the expression (which gets the value of to_combine_int_rasters given the key map deleteme_max_key_for_combining_rasters)
length_of_array=${#to_combine_int_rasters_array[@]}
for index in "${!to_combine_int_rasters_array[@]}"; do
    # Check if it's the last element
    if [[ $index -eq $((length_of_array - 1)) ]]; then
        max_combined_raster_mapcalc+=", ${to_combine_int_rasters_array[index]}"
    else
        if [[ $index -eq 0 ]]; then
            max_combined_raster_mapcalc+="if(deleteme_max_key_for_combining_rasters == $index, ${to_combine_int_rasters_array[index]}"
        else
            max_combined_raster_mapcalc+=", if(deleteme_max_key_for_combining_rasters == $index, ${to_combine_int_rasters_array[index]}"
        fi
    fi
done

# Close all the if-statements
for index in "${!to_combine_int_rasters_array[@]}"; do
    if [[ $index -eq $((length_of_array - 1)) ]]; then
        continue;
    fi
    max_combined_raster_mapcalc+=")"
done


# echo "$output_raster = if(isnull($mask), deleteme_avg_combined_integer, $max_combined_raster_mapcalc)"
# exit 1
# Create the output raster based on the mask condition, and the created mapcalc expression

# echo "$output_raster = if(isnull($mask), deleteme_avg_combined_integer, $max_combined_raster_mapcalc)"
# the mapcalc command should look something like this:
# 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_updatedN_wet_planting_month_RF = 
# if(isnull(winter_wheat_countries_mask),
#     deleteme_avg_combined_integer,
#     if(key == 0,
#         K013RF, 
#         if(key == 1,
#             K016RF, 
#             if(key == 2,
#                 K015RF, 
#                 if(key == 3,
#                     K010RF, 
#                     if(key == 4,
#                         K076RF, 
#                         if(key == 5,
#                             K012RF, 
#                             if(key == 6,
#                                 K011RF, 
#                                 if(key == 7,
#                                     K007RF, 
#                                     if(key == 8,
#                                         K006RF, 
#                                         if(key == 9,
#                                             K009RF, 
#                                             if(key == 10,
#                                                 K002RF,
#                                                 BestMonth_noGCMcalendar_p0_whK001RF__Aug04_updatedN_wet_RF
#                                             )
#                                         )
#                                     )
#                                 )
#                             )
#                         )
#                     )
#                 )
#             )
#         )
#     )
# )


r.mapcalc "$output_raster = if(isnull($mask), deleteme_avg_combined_integer, $max_combined_raster_mapcalc)"

# remove temporary rasters
g.remove -f rast=deleteme_avg_combined,deleteme_avg_combined_integer,deleteme_max_key_for_combining_rasters

echo "Processing completed and output raster generated: $output_raster"
