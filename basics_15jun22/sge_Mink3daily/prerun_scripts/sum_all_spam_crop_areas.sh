#!/bin/bash

# sum a series of rasters and save the result
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder

# need to put the spam2010 dataset in grassdata/world/spam

cd $spam_data_folder

area_categories=(
    "A" # total irrigated+rainfed
    "I" # irrigated
    "R" # rainfed
)

crops=(
    "ACOF"
    "PMIL"
    "LENT"
    "SUGC"
    "BANA"
    "POTA"
    "MAIZ"
    "SUNF"
    "BARL"
    "RAPE"
    "OCER"
    "SWPO"
    "BEAN"
    "RCOF"
    "OFIB"
    "TEAS"
    "CASS"
    "REST"
    "OILP"
    "TEMF"
    "CHIC"
    "RICE"
    "OOIL"
    "TOBA"
    "CNUT"
    "SESA"
    "OPUL"
    "TROF"
    "COCO"
    "SMIL"
    "ORTS"
    "VEGE"
    "COTT"
    "SORG"
    "PIGE"
    "WHEA"
    "COWP"
    "SOYB"
    "PLNT" 
    "YAMS" 
    "GROU" 
    "SUGB" 
)

for area_category in "${area_categories[@]}"; do
    echo "area_category"
    echo "$area_category"

    if [ $area_category == "A" ]; then
        area_name=""
    elif [ $area_category == "I" ]; then
        area_name="_irrigated"
    elif [ $area_category == "R" ]; then
        area_name="_rainfed"
    fi
    areas=""
    echo "area_name"
    echo "$area_name"
    for crop in "${crops[@]}"; do
        # echo "crop"
        # echo $crop

        # this is for reading in some MAPSPAM data acquired from https://www.mapspam.info/
        # and placed in /grassdata/world/spam
        # (imports the geotiffs)
        r.in.gdal input=spam2010V2r0_global_H_${crop}_${area_category}.tif output=${crop}${area_name}_cropland 2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"


        r.out.ascii input=${crop}${area_name}_cropland output=- > "${crop}${area_name}_cropland.asc"

        # if the first crop, then don't add a comma
        if [ -z "$areas" ]; then
            areas="${crop}${area_name}_cropland"
        else
            areas="$areas,${crop}${area_name}_cropland"
        fi

    done


    # statements
    combined_area="ALL_CROPS${area_name}_cropland"
    r.series --overwrite input=$areas output=$combined_area method=sum 2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"


    # save the result as tiff
    r.out.tiff input=$combined_area output=- > "${combined_area}.tif"
    r.out.ascii input=$combined_area output=- > "${combined_area}.asc"

done
