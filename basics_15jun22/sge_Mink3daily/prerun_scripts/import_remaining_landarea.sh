#!/bin/bash

# This script imports the remaining land area not including SPAM crops.
# IMPORTANT NOTE: must run the import of all spam data first!! (process_allcrops_spam_datasets.sh)
set -e # exit on error

# sum a series of rasters and save the result
echo ""
echo "Importing and resampling land area data and land area minus cropland..."
echo ""

# get paths
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder and export_scripts

# need to put the spam2010 dataset in grassdata/world/spam
cd "$spam_data_folder"

# save the current low resolution (crop model resolution) region
g.region save=temp_lowres_region --overwrite

# just in case we error out, don't want to have a wierd resolution after
cleanup() {
    g.region region=temp_lowres_region
    echo "reset to original region"
}

# on exit or error, cleanup back to original low res region
trap cleanup EXIT ERR


# # remove any masks
# r.mask -r
set +e # don't exit on error

allcrops_spam_processed_was_run_flag=$(g.mlist -r | grep ALL_CROPS_cropland_highres)
# Construct a list of all cropland. If the first crop, then don't add a comma.
if [ -z "$allcrops_spam_processed_was_run_flag" ]; then
    echo "ERROR: must run the process_allcrops_spam_datasets.sh script first before importing land area minus cropland."
    exit 1
fi
set -e # exit on error
# remaining code imports total land area (minus cropland) to the proper units and resolution

# import land area in units km^2 at 2.5 min resolution from EARTHDATA/SEDAC raster 
# source:
# https://sedac.ciesin.columbia.edu/data/set/gpw-v4-land-water-area-rev11/data-download
r.in.gdal input="${earthdata_data_folder}gpw_v4_land_water_area_rev11_landareakm_2pt5_min.tif" output="landArea2p5min_kmsquared" --quiet --overwrite 2> /dev/null
r.null null=0 map="landArea2p5min_kmsquared"  # remove any null values (set them to zero)

echo "2"
# Save the north, south, east, west boundaries and resolutions for very lowres (the crop model grid resolution)
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')



# set region (including resolution and boundaries) to the higher resolution  (5min), but not highest, cropland raster
g.region rast=ALL_CROPS_cropland_highres

# set the north, south, east, west boundaries back to the original low res boundaries
g.region n=$n s=$s w=$w e=$e

# save the cropland area as the lower resolution region
g.region save=temp_lowres_cropland_region --overwrite

# return to crop model resolution
g.region region=temp_lowres_region


echo "calculating 5 minute land area minus crops..."

# move from 2.5 min to 5 min land area raster and save as landArea_kmsquared_highres
highres_raster="landArea2p5min_kmsquared"
lowres_region_name="temp_lowres_cropland_region"
coarsened_raster_name="landArea_kmsquared_highres" # this is still "high resolution" relative to the crop model at 5 minute
bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name

# need to be at 5 minutes to do these conversions to avoid resampling.
g.region region=temp_lowres_cropland_region

# 1 square km is 100 hectares. Convert to proper units.
r.mapcalc "landArea_highres = landArea_kmsquared_highres * 100"

# total land area per pixel, with no crops. Subtracting two low res rasters.
r.mapcalc "LAND_AREA_NO_CROPS_cropland_highres = landArea_highres - ALL_CROPS_cropland_highres"

# sometimes multiple crops are planted in same area. So area can be greater than land area. Set these cases to zero.
r.mapcalc "LAND_AREA_NO_CROPS_cropland_highres = if(LAND_AREA_NO_CROPS_cropland_highres < 0, 0, LAND_AREA_NO_CROPS_cropland_highres)"

# set zero irrigated cropland when we look outside of normal cropland
r.mapcalc "LAND_AREA_NO_CROPS_irrigated_cropland_highres = 0 * LAND_AREA_NO_CROPS_cropland_highres"

# all cropland outside of current cropland is assumed to be rainfed 
r.mapcalc "LAND_AREA_NO_CROPS_rainfed_cropland_highres = LAND_AREA_NO_CROPS_cropland_highres"

g.region region=temp_lowres_region

echo "calculating crop model resolution land area minus crops..."

# move from 5 min to crop model resolution and save as landArea_kmsquared
highres_raster="LAND_AREA_NO_CROPS_irrigated_cropland_highres"
lowres_region_name="temp_lowres_region"
coarsened_raster_name="LAND_AREA_NO_CROPS_irrigated_cropland"
bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name

# move from 5 min to crop model resolution and save as landArea_kmsquared
highres_raster="LAND_AREA_NO_CROPS_rainfed_cropland_highres"
lowres_region_name="temp_lowres_region"
coarsened_raster_name="LAND_AREA_NO_CROPS_rainfed_cropland"
bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name

echo ""
echo "land area data minus cropland area loaded"
echo ""

