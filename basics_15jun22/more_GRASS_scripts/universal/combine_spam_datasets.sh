#!/bin/bash
if [ $# -eq 0 ]; then
  echo "Usage: $0 script_folder"

  exit
fi

# sum a series of rasters and save the result
 echo ""
 echo "creating combined spam data..."
 echo ""
# need to put the spam2010 dataset in grassdata/world/spam
echo "PWD"
echo "$PWD"
script_folder=$1
cd "$script_folder../../grassdata/world/spam"

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
    if [ $area_category == "A" ]; then
        area_name=""
    elif [ $area_category == "I" ]; then
        area_name="_irrigated"
    elif [ $area_category == "R" ]; then
        area_name="_rainfed"
    fi
    areas=""
    for crop in "${crops[@]}"; do

        # this is for reading in some MAPSPAM data acquired from https://www.mapspam.info/
        # and placed in /grassdata/world/spam
        # (imports the geotiffs)
        script -c "r.in.gdal input=spam2010V2r0_global_H_${crop}_${area_category}.tif output=${crop}${area_name}_cropland --quiet" > /dev/null

        # if the first crop, then don't add a comma
        if [ -z "$areas" ]; then
            areas="${crop}${area_name}_cropland"
        else
            areas="$areas,${crop}${area_name}_cropland"
        fi

    done

    combined_area="ALL_CROPS${area_name}_cropland"
    r.series --overwrite input=$areas output=$combined_area method=sum --quiet

    # save the results
    r.out.tiff input=$combined_area output=- > "${combined_area}.tif" --quiet
    r.out.ascii input=$combined_area output=- > "${combined_area}.asc" --quiet

done

for crop in "${crops[@]}"; do
    r.in.gdal input=spam2010V2r0_global_Y_${crop}_A.tif output=${crop}_yield --quiet &> /dev/null
    r.out.ascii input=${crop}_yield output=- > "${crop}_yield.asc" --quiet
    mv "${crop}_yield.asc" "$script_folder../../../wth_historical"
done

echo ""
echo "All rasters created for all spam crops"
echo ""
