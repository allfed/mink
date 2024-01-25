#!/bin/bash

# This script imports yield, production, and crop area for the crop of interest from SPAM (mapspam.info).

set -e 

if [ $# -eq 0 ]; then
  echo "Usage: $0 crop_spam_4_letters"
  exit
fi

crop_caps=$1

echo ""
echo "creating $crop_caps spam data..."
echo ""

# get paths
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder and export_scripts

# need to put the spam2010 dataset in grassdata/world/spam
cd "$spam_data_folder"

# import spam data for this crop (yield, production)
r.in.gdal input=spam2010V2r0_global_Y_${crop_caps}_A.tif output=${crop_caps}_yield --overwrite --quiet 2> /dev/null
r.in.gdal input=spam2010V2r0_global_P_${crop_caps}_A.tif output=${crop_caps}_production_highres --overwrite  --quiet 2> /dev/null

# save the current low resolution region
g.region save=temp_lowres_region --overwrite

highres_raster="${crop_caps}_production_highres"
lowres_region_name="temp_lowres_region"
coarsened_raster_name="${crop_caps}_production"
bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name


cd ${export_scripts}
./save_ascii.sh $historical_results_directory "${crop_caps}_yield"
./save_tif.sh $historical_results_directory "${crop_caps}_yield"
./save_ascii.sh $historical_results_directory "${crop_caps}_cropland"
./save_tif.sh $historical_results_directory "${crop_caps}_cropland"
./save_ascii.sh $historical_results_directory "${crop_caps}_production"
./save_tif.sh $historical_results_directory "${crop_caps}_production"

# All country's historical yields from SPAM are saved in wth_historical folder.
./export_by_country_data.sh ${crop_caps} "${crop_caps}_yield" "${crop_caps}_cropland_highres" skip_me skip_me wth_historical 

cd -

echo ""
echo "All rasters created for $crop_caps and historical rasters exported"
echo ""
