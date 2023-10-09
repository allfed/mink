#!/bin/bash

set -e 

if [ $# -eq 0 ]; then
  echo "Usage: $0 crop_spam_4_letters"
  exit
fi

# sum a series of rasters and save the result
 echo ""
 echo "creating combined spam data..."
 echo ""
# need to put the spam2010 dataset in grassdata/world/spam

crop_caps=$1

echo "Crop SPAM 4 letter code: $crop_caps"

# get paths
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder and export_scripts
cd "$spam_data_folder"

area_categories=(
    "A" # total irrigated+rainfed
    "I" # irrigated
    "R" # rainfed
)

for area_category in "${area_categories[@]}"; do
    if [ $area_category == "A" ]; then
        area_name=""
    elif [ $area_category == "I" ]; then
        area_name="_irrigated"
    elif [ $area_category == "R" ]; then
        area_name="_rainfed"
    fi
    areas=""
    # this is for reading in some MAPSPAM data acquired from https://www.mapspam.info/
    # and placed in /grassdata/world/spam
    # (imports the geotiffs)
    # echo "spam2010V2r0_global_H_${crop_caps}_${area_category}.tif output=${crop_caps}${area_name}_cropland"
    r.in.gdal input=spam2010V2r0_global_H_${crop_caps}_${area_category}.tif output=${crop_caps}${area_name}_cropland --quiet --overwrite 2> /dev/null

    # if the first crop, then don't add a comma
    if [ -z "$areas" ]; then
        areas="${crop_caps}${area_name}_cropland"
    else
        areas="$areas,${crop_caps}${area_name}_cropland"
    fi

    combined_area="ALL_CROPS${area_name}_cropland"
    r.series --overwrite --quiet input=$areas output=$combined_area method=sum 

    # save the results
    r.out.gdal input=$combined_area output=$combined_area.tif format=GTiff type=Float64 --quiet --overwrite 2> /dev/null

    if [ ! -f $combined_area.tif ]; then
        echo "ERROR: r.out.gdal was unable to create the tif."
        echo "get rid of the 2> /dev/null in code above to see what it says about the error."
        exit 1
    fi

    r.out.ascii  --quiet --overwrite input=$combined_area output=- > "${combined_area}.asc"

done


r.in.gdal input=spam2010V2r0_global_Y_${crop_caps}_A.tif output=${crop_caps}_yield --overwrite --quiet 2> /dev/null
r.in.gdal input=spam2010V2r0_global_P_${crop_caps}_A.tif output=${crop_caps}_production --overwrite  --quiet 2> /dev/null

cd ${export_scripts}
./save_ascii.sh $historical_results_directory "${crop_caps}_yield"
./save_tif.sh $historical_results_directory "${crop_caps}_yield"
./save_ascii.sh $historical_results_directory "${crop_caps}_cropland"
./save_tif.sh $historical_results_directory "${crop_caps}_cropland"
./save_ascii.sh $historical_results_directory "${crop_caps}_production"
./save_tif.sh $historical_results_directory "${crop_caps}_production"

./export_by_country_data.sh ${crop_caps} "${crop_caps}_yield" skip_me skip_me wth_historical 

cd -



echo ""
echo "All rasters created for $crop_caps and historical rasters exported"
echo ""
