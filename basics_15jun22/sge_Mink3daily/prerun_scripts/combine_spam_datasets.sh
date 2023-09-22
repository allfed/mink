#!/bin/bash
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

echo "crop_caps"
echo crop_caps

# get paths
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder and export_scripts

cd "$spam_data_folder"

area_categories=(
    "A" # total irrigated+rainfed
    "I" # irrigated
    "R" # rainfed
)

echo "going by area"
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
    echo "importing"
    echo "spam2010V2r0_global_H_${crop_caps}_${area_category}.tif output=${crop_caps}${area_name}_cropland"
    r.in.gdal input=spam2010V2r0_global_H_${crop_caps}_${area_category}.tif output=${crop_caps}${area_name}_cropland --quiet --overwrite

    # if the first crop, then don't add a comma
    if [ -z "$areas" ]; then
        areas="${crop_caps}${area_name}_cropland"
    else
        areas="$areas,${crop_caps}${area_name}_cropland"
    fi

    combined_area="ALL_CROPS${area_name}_cropland"
    r.series --overwrite --quiet input=$areas output=$combined_area method=sum 

    # save the results
    r.out.tiff  --quiet --overwrite input=$combined_area output=- > "${combined_area}.tif"
    r.out.ascii  --quiet --overwrite input=$combined_area output=- > "${combined_area}.asc"

done



r.in.gdal input=spam2010V2r0_global_Y_${crop_caps}_A.tif output=${crop_caps}_yield --overwrite --quiet
r.in.gdal input=spam2010V2r0_global_P_${crop_caps}_A.tif output=${crop_caps}_production --overwrite  --quiet

cd ${export_scripts}
./save_ascii.sh "${crop_caps}_yield" $historical_results_directory
./save_tif.sh "${crop_caps}_yield" $historical_results_directory
./save_ascii.sh "${crop_caps}_cropland" $historical_results_directory
./save_tif.sh "${crop_caps}_cropland" $historical_results_directory
./save_ascii.sh "${crop_caps}_production" $historical_results_directory
./save_tif.sh "${crop_caps}_production" $historical_results_directory
cd -

echo ""
echo "All rasters created for $crop_caps and historical rasters exported"
echo ""
