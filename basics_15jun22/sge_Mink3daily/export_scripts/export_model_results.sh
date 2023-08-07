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
    ./save_tif.sh $overall_yield_raster $catastrophe_results_directory
    ./save_asc.sh $overall_yield_raster $catastrophe_results_directory
fi

substring="catastrophe"
if [[ $overall_yield_raster =~ *"$substring"* ]]; then
    # export overall yield to catastrophe results
    ./save_tif.sh $overall_yield_raster $catastrophe_results_directory
    ./save_asc.sh $overall_yield_raster $catastrophe_results_directory
fi



# # export historical yield (useful if the region has changed)
# historical_yield="${crop_caps}_yield"
# ./save_tif.sh $historical_yield $historical_results_directory
# ./save_asc.sh $historical_yield $historical_results_directory

# # export historical cropland (useful if the region has changed)
# historical_cropland="${crop_caps}_cropland"
# ./save_tif.sh $historical_cropland $historical_results_directory
# ./save_asc.sh $historical_cropland $historical_results_directory

# # export historical cropland (useful if the region has changed)
# historical_production="${crop_caps}_production"
# ./save_tif.sh $historical_cropland $historical_results_directory
# ./save_asc.sh $historical_cropland $historical_results_directory
