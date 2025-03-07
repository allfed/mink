#!/bin/bash


if [ $# -lt 2 ]; then
  echo "Usage: $0 crop_4_letter_all_caps yield_raster"
  exit 1
fi

crop_caps=$1
overall_yield_raster=$2

. ../default_paths_etc.sh # import catastrophe_results_directory, control_results_directory, historical_results_directory
substring="control"
if [[ $overall_yield_raster =~ *"$substring"* ]]; then
    # export overall yield to control results
    ./save_tif.sh $catastrophe_results_directory $overall_yield_raster
    ./save_asc.sh $catastrophe_results_directory $overall_yield_raster
fi
substring="catastrophe"
if [[ $overall_yield_raster =~ *"$substring"* ]]; then
    # export overall yield to catastrophe results
    ./save_tif.sh $catastrophe_results_directory $overall_yield_raster
    ./save_asc.sh $catastrophe_results_directory $overall_yield_raster
fi



# # export historical yield (useful if the region has changed)
# historical_yield="${crop_caps}_yield"
# ./save_tif.sh $historical_results_directory $historical_yield
# ./save_asc.sh $historical_results_directory $historical_yield

# # export historical cropland (useful if the region has changed)
# historical_cropland="${crop_caps}_cropland"
# ./save_tif.sh $historical_results_directory $historical_cropland
# ./save_asc.sh $historical_results_directory $historical_cropland

# # export historical cropland (useful if the region has changed)
# historical_production="${crop_caps}_production"
# ./save_tif.sh $historical_results_directory $historical_cropland
# ./save_asc.sh $historical_results_directory $historical_cropland
