#!/bin/bash
###

# Summary of script operation:

#     Saving Current Region: 
#         - The current region is saved to a temporary region named 'temp_lowres_region'.

#     Removing Existing Masks:
#         - Removes any existing raster masks using 'r.mask -r'.

#     Setting Script Parameters: 
#         - The script requires three input parameters: the crop name, yield raster name, and save folder location.

#     Reading Region Information: 
#         - Saves the north, south, east, west boundaries and resolutions of the current region.

#     Loading High-Resolution Cropland Data:
#         - A high-resolution cropland raster is imported and named as "{crop name}_cropland_highres".

#     Setting and Checking Region Resolution: 
#         - Adjusts the region resolution to match the high-resolution cropland data.
#         - Checks to ensure that the high-resolution cropland raster has a higher resolution than the original region. If not, the script aborts.

#     Resampling Rasters:
#         - Resamples the low-resolution yield data, planting months, and days to maturity to match the high-resolution raster.

#     Production Calculation:
#         - Computes a new raster "production_highres" by multiplying the high-resolution yield raster with the high-resolution cropland raster.
#         - Removes zero values from the production and cropland rasters.

#     Country-wise Raster Generation:
#         - Converts a vector layer of countries to a raster layer with categorical values representing country codes.
#         - Generates statistics including country names for each raster category, most common planting month per country, mean days to maturity, sum of production, and sum of cropland area per country.

#     Merging Data:
#         - Uses a Python script to merge all the country-wise CSV data.

#     File Movement:
#         - Moves the merged CSV file to the specified save folder.

#     Cleanup: 
#         - Restores the original region.

###
set -e # quit on any error

if [ $# -lt 5 ]; then
  echo "Usage: $0 <crop_caps_4_letters> <raster_to_export> <planting_month> <days_to_maturity> <save_folder>"
  echo " If you only have yield or are missing one of planting months or days to maturity, name the raster \"skip_me\" and it will be skipped!:"
  echo " the script requires spam, which is what the 4 letter crop code is from."
  echo " Example usages:"
  echo " $0 WHEA WHEA_yield <planting_month_value> <days_to_maturity_value> wth_historical"
  echo " $0 WHEA 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_genSNX_wet_overall_yield 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_genSNX_wet_planting_month_RF 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_genSNX_wet_maturity_RF wth_control"
  echo " $0 WHEA ./export_by_country_data.sh WHEA AGMIP_princeton_RF_yield_whe_lowres_cleaned_2005 AGMIP_princeton_RF_planting_months_whe_lowres_cleaned_2005 AGMIP_princeton_RF_maty-day_whe_lowres_cleaned_2005 grassdata/world/AGMIP"
  exit 1
fi

# example region that should work as low res enough (if using spam data)
# g.region n=65 s=-65 w=-170 e=170 nsres=1.88405797101449 ewres=1.25

g.region save=temp_lowres_region --overwrite
# just in case we error out, don't want to have a wierd resolution after
cleanup() {
    g.region region=temp_lowres_region
    echo "reset to original region"
}

trap cleanup EXIT ERR



r.mask -r

crop_caps=$1
yield_raster=$2
planting_month=$3
days_to_maturity=$4
save_folder=$5 # location past git_root to save to


. ../default_paths_etc.sh # import git_root and spam_data_folder variables

# Save the north, south, east, west boundaries and resolutions
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# load high resolution cropland
r.in.gdal input="${spam_data_folder}spam2010V2r0_global_H_${crop_caps}_A.tif" output="${crop_caps}_cropland_highres" --overwrite

# set region (including resolution and boundaries) to the high resolution cropland
g.region rast="${crop_caps}_cropland_highres"

# set the north, south, east, west boundaries back to the original
g.region n=$n s=$s w=$w e=$e

# read the new nsres and ewres after setting the high-res region
read new_nsres new_ewres <<< $(g.region -g | awk -F'=' '$1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# check if the high res cropland resolution is in fact higher for both nsres and ewres
if (( $(echo "$new_nsres < $nsres" | bc -l) )) && (( $(echo "$new_ewres < $ewres" | bc -l) )); then
  echo "High res cropland resolution is higher for both nsres and ewres. Continuing."
else
  echo "ERROR: High res cropland resolution is NOT higher for both nsres and ewres! Aborting."
  exit 1
fi

# set the north south and east west boundaries back to the original
g.region n=$n
g.region s=$s
g.region w=$w
g.region e=$e

echo ""
echo "resample low res yield data to match high resolution raster"
r.resample input=$yield_raster output="${yield_raster}_highres" --overwrite


if [ "$planting_month" != "skip_me" ]; then
  echo ""
  echo "resample planting months based on most common chosen planting month"
  r.resample input=$planting_month output="${planting_month}_highres" --overwrite
fi

if [ "$days_to_maturity" != "skip_me" ]; then
  echo ""
  echo "resample days to maturity"
  r.resample input=$days_to_maturity output="${days_to_maturity}_highres" --overwrite
fi

echo ""
echo "calculate the high resolution production by multiplying area with yield per hectare"
r.mapcalc "production_highres = ${yield_raster}_highres * ${crop_caps}_cropland_highres"

echo ""
echo "removing zero values from area, production rasters"
r.null setnull=0 map=production_highres
r.null setnull=0 map="${crop_caps}_cropland_highres"

echo ""
echo "create labels"
# use the vector database cntry05 to output a raster with categorical values, where the value is  country category as an integer, and the raster also stores country category labels with the iso3 country code from ISO_3DIGIT
v.to.rast --overwrite input=cntry05 output=country_cats use=cat labelcolumn=ISO_3DIGIT

# useful command:
# v.info -c cntry05 --quiet

echo ""
echo "export labels"
# export the country names for each category, which will be used for later unification with the country production, crop area, and days to maturity
r.stats -A -l input=country_cats output=country_names_for_each_category.csv -N

if [ "$planting_month" != "skip_me" ]; then
  echo ""
  echo "create planting months"
  # Create a raster where the statistical mode (most common number in the category, in this case most common planting month in the country) in the cover map ( ${planting_month}_highres), is exported to the category labels in the deleteme_pm_in_labels raster.
  r.statistics --overwrite base=country_cats cover=${planting_month}_highres method=mode output=deleteme_pm_in_labels

  echo ""
  echo "export planting months"
  # export the labels in the deleteme_pm_in_labels raster, along with the category value. 
  r.stats -A -l input=deleteme_pm_in_labels output=country_pm_stats.csv -N
fi

if [ "$days_to_maturity" != "skip_me" ]; then

  echo ""
  echo "export days to maturity"
  # use days to maturity calculated previously and output a raster with all statistics (including the mean) for each country
  r.univar -t map=${days_to_maturity}_highres zones=country_cats fs=comma output=country_days_to_maturity_mean.csv
fi

echo ""
echo "export production"
# use production and output a raster with all statistics (including the sum) for each country
r.univar -t map=production_highres zones=country_cats fs=comma output=country_production_sum.csv

echo ""
echo "export crop area"
# use crop area and output a raster with all statistics (including the sum) for each country
r.univar -t map=${crop_caps}_cropland_highres zones=country_cats fs=comma output=cropland_sum.csv

# return to the original region
g.region region=temp_lowres_region

if [ "$planting_month" != "skip_me" ]; then
  g.remove rast=deleteme_pm_in_labels
fi

echo ""
echo "merge exports and move to save folder $git_root/$save_folder"
python3 merge_all_those_pesky_by_country_csvs.py "by_country_${yield_raster}.csv"

rm -f country_pm_stats.csv
rm -f country_days_to_maturity_mean.csv
rm country_names_for_each_category.csv
rm country_production_sum.csv
rm cropland_sum.csv


mv "by_country_${yield_raster}.csv" "$git_root/$save_folder"

echo ""
echo "Successfully exported to a csv."
echo "Location: $save_folder"
echo "Filename: by_country_${yield_raster}.csv"
echo ""
