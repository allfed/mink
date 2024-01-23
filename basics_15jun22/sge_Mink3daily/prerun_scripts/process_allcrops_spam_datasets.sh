#!/bin/bash

# This script imports the area of all crops, sums them, and saves both ALL_FOOD_CROPS and ALL_CROPS for
# all, irrigated only, and rainfed only area rasters from SPAM (mapspam.info).

set -e # exit on error

# sum a series of rasters and save the result
echo ""
echo "Creating combined spam cropland rasters..."
echo ""

# get paths
. ../default_paths_etc.sh # imports historical_results_directory and spam_data_folder and export_scripts

# need to put the spam2010 dataset in grassdata/world/spam
cd "$spam_data_folder"

area_categories=(
    "A" # total irrigated+rainfed
    "I" # irrigated
    "R" # rainfed
)

food_crops=(
    "WHEA" # wheat
    "RICE" # rice
    "MAIZ" # maize
    "BARL" # barley
    "PMIL" # pearl millet
    "SMIL" # small millet
    "SORG" # sorghum
    "OCER" # other cereals
    "POTA" # potato
    "SWPO" # sweet potato
    "YAMS" # yams
    "CASS" # cassava
    "ORTS" # other roots
    "BEAN" # bean
    "CHIC" # chickpea
    "COWP" # cowpea
    "PIGE" # pigeonpea
    "LENT" # lentil
    "OPUL" # other pulses
    "SOYB" # soybean
    "GROU" # groundnut
    "CNUT" # coconut
    "BANA" # banana
    "PLNT" # plantain
    "TROF" # tropical fruit
    "TEMF" # temperate fruit
    "VEGE" # vegetables
)
nonfood_crops=(
    "OILP" # oilpalm 
    "SUNF" # sunflower
    "RAPE" # rapeseed
    "SESA" # sesameseed
    "OOIL" # other oil crops
    "SUGC" # sugarcane
    "SUGB" # sugarbeet
    "COTT" # cotton  
    "OFIB" # other fibre crops
    "ACOF" # arabica coffee
    "RCOF" # robusta coffee
    "COCO" # cocoa   
    "TEAS" # tea 
    "TOBA" # tobacco 
    "REST" # rest of crops
)

# remove any masks
r.mask -r

example_highres_raster="${food_crops[0]}_cropland_highres"

# import a raster at high resolution of !!PHYSICAL AREA!!, used for resampling with sum later on
r.in.gdal input=spam2010V2r0_global_A_${food_crops[0]}_A.tif output=$example_highres_raster --quiet --overwrite 2> /dev/null

# save the current low resolution region
g.region save=temp_lowres_region --overwrite

sum_and_save_rasters() {
    local all_crops=$1
    local save_raster_name=$2

    # aggregate combined area of several crops by summing them
    r.series --overwrite --quiet input=$all_crops output=$save_raster_name method=sum 

    # save the summed crops as a tiff file
    r.out.gdal input=$save_raster_name output=$save_raster_name.tif format=GTiff type=Float64 --quiet --overwrite 2> /dev/null

    if [ ! -f $save_raster_name.tif ]; then
        echo "ERROR: r.out.gdal was unable to create the tif file from the raster."
        echo "get rid of the 2> /dev/null in code above to see what it says about the error."
        echo "process_allcrops_spam_datasets.sh script located at basics_15jun22/sge_Mink3daily/prerun_scripts/"
        exit 1
    fi
    # save the summed crops as an asc file
    r.out.ascii  --quiet --overwrite input=$save_raster_name output=- > "${save_raster_name}.asc"
    if [ ! -f $save_raster_name.tif ]; then
        echo "ERROR: r.out.gdal was unable to create the asc file from the raster."
        echo "get rid of the --quiet in code above to see what it says about the error."
        echo "process_allcrops_spam_datasets.sh script located at basics_15jun22/sge_Mink3daily/prerun_scripts/"
        exit 1
    fi
}

# Import the original cropland at high resolution, then get the cropland to it's sum at low resolution
import_and_resample_cropland() {
    local crop_name=$1
    local area_category=$2
    local area_name=$3
    echo ""
    echo "importing $crop_name$area_name ..."

    coarsened_raster_name="${crop_name}${area_name}_cropland"
    lowres_region_name="temp_lowres_region"

    #import !!PHYSICAL AREA!! for each crop, all, rainfed, or irrigated"
    highres_tif_name_to_import="spam2010V2r0_global_A_${crop_name}_${area_category}.tif"

    highres_raster="${coarsened_raster_name}_highres"

    # Import the geotiff.
    r.in.gdal input=$highres_tif_name_to_import output=$highres_raster --quiet --overwrite 2> /dev/null

    # echo "bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name"
    bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name
}

# this is for reading in some MAPSPAM data acquired from https://www.mapspam.info/
# and placed in /grassdata/world/spam


# START COMMENT OUT BLOCK REGION IF YOU WANT TO NOT RERUN SUMMING ALL CROPS

for area_category in "${area_categories[@]}"; do
    if [ $area_category == "A" ]; then
        area_name=""
    elif [ $area_category == "I" ]; then
        area_name="_irrigated"
    elif [ $area_category == "R" ]; then
        area_name="_rainfed"
    fi

    echo "" 
    echo "From all (A), irrigated only (I) and rainfed only (R), importing $area_category" 
    echo "" 

    # reset the list of crops names for all, rainfed, or irrigated category
    all_crops=""
    # first import the food crops and sum them
    for food_crop in "${food_crops[@]}"; do
        raster_basename="${food_crop}${area_name}_cropland"
        import_and_resample_cropland $food_crop $area_category $area_name
        
        # Construct a list of all crops. If the first crop, then don't add a comma.
        if [ -z "$all_crops" ]; then
            all_crops="$raster_basename"
        else
            all_crops="$all_crops,$raster_basename"
        fi

    done
    sum_and_save_rasters $all_crops "ALL_FOOD_CROPS${area_name}_cropland"

    # second, import and save the remainder of the crops
    for nonfood_crop in "${nonfood_crops[@]}"; do
        raster_basename="${nonfood_crop}${area_name}_cropland"

        import_and_resample_cropland $nonfood_crop $area_category $area_name
        
        # Construct a list of all crops. If the first crop, then don't add a comma.
        if [ -z "$all_crops" ]; then
            all_crops="$raster_basename"
        else
            all_crops="$all_crops,$raster_basename"
        fi
    done
    sum_and_save_rasters $all_crops "ALL_CROPS${area_name}_cropland"

done

echo ""
echo "All raster areas summed for all crops"
echo ""

# END COMMENT OUT BLOCK REGION IF YOU WANT TO NOT RERUN SUMMING ALL CROPS

# import total land area (ignoring crops) to the proper units and resolution

# import land area in units km^2
r.in.gdal input="${earthdata_data_folder}landArea15min.tif" output="landArea15min_kmsquared" --quiet --overwrite 2> /dev/null
r.null null=0 map="landArea15min_kmsquared"  # remove any null values (set them to zero)

highres_raster="landArea15min_kmsquared"
lowres_region_name="temp_lowres_region"
coarsened_raster_name="landArea_kmsquared"
bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster $lowres_region_name $coarsened_raster_name

# 1 square km is 100 hectares
r.mapcalc "landArea = landArea_kmsquared * 100"

# total land area per pixel, with no crops. Subtracting two low res rasters.
r.mapcalc "LAND_AREA_NO_CROPS_cropland = landArea - ALL_CROPS_cropland"

# sometimes multiple crops are planted in same area. So area can be greater than land area. Set these cases to zero.
r.mapcalc "LAND_AREA_NO_CROPS_cropland = if(LAND_AREA_NO_CROPS_cropland < 0, 0, LAND_AREA_NO_CROPS_cropland)"

# set zero irrigated cropland when we look outside of normal cropland
r.mapcalc "LAND_AREA_NO_CROPS_irrigated_cropland = 0 * LAND_AREA_NO_CROPS"

# all cropland outside of current cropland is assumed to be rainfed 
r.mapcalc "LAND_AREA_NO_CROPS_rainfed_cropland = LAND_AREA_NO_CROPS_cropland"


echo ""
echo "no-crop land area loaded"
echo ""

