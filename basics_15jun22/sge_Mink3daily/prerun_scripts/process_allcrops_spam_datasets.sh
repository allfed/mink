#!/bin/bash

# This script imports the area of all crops, sums them, and saves both ALL_FOOD_CROPS and ALL_CROPS for
# all, irrigated only, and rainfed only area rasters from SPAM (mapspam.info).
# The region is always at the crop model resolution in this script (with the exception of r.null call at the end). Called functions operate sometimes at different resolutions.
# However, simply importing a script does not resample the rasters. So they are imported at high resolution even though the region this script operates in is low resolution (of the crop model).

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

# save the current low resolution region
g.region save=temp_lowres_region --overwrite

# this function sums several rasters specified as a comma separated list with GRASS GIS and exports these rasters as both a tif and asc file. The raster of cropland will be used in the crop model to estimate production by multiplying by area. The high resolution cropland is used to split up the low resolution crop model results into by-country results. Low resolution crops are typically "{SPAM 4 letter code, such as WHEA}_cropland" and high resolution are "{SPAM 4 letter code, such as WHEA}_cropland_highres"
sum_and_save_rasters() {
    local all_crops=$1
    local save_raster_name=$2

    # set the region 

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
    highres_raster_name="${coarsened_raster_name}_highres"

    lowres_region_name="temp_lowres_region"

    #import !!PHYSICAL AREA!! for each crop, all, rainfed, or irrigated"
    highres_tif_name_to_import="spam2010V2r0_global_A_${crop_name}_${area_category}.tif"


    # Import the geotiff.
    r.in.gdal input=$highres_tif_name_to_import output=$highres_raster_name --quiet --overwrite 2> /dev/null


    # create all the individual crops with low resolution, for example, this creates WHEA_cropland
    # echo "bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster_name $lowres_region_name $coarsened_raster_name"
    bash ${universal_scripts}resample_with_sum_highres_to_lowres.sh $highres_raster_name $lowres_region_name $coarsened_raster_name
}

# this is for reading in some MAPSPAM data acquired from https://www.mapspam.info/
# and placed in /grassdata/world/spam


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
    all_crops_highres=""
    # first import the food crops and sum them
    for food_crop in "${food_crops[@]}"; do
        raster_basename="${food_crop}${area_name}_cropland"
        raster_basename_highres="${food_crop}${area_name}_cropland_highres"

        # import the crop at highres, resample to low res. Imports both high res and low res rasters.
        import_and_resample_cropland $food_crop $area_category $area_name
        
        # Construct a list of all cropland. If the first crop, then don't add a comma.
        if [ -z "$all_crops" ]; then
            all_crops="$raster_basename"
        else
            all_crops="$all_crops,$raster_basename"
        fi

       # Construct a list of all cropland at high resolution. If the first crop, then don't add a comma.
        if [ -z "$all_crops_highres" ]; then
            all_crops_highres="$raster_basename_highres"
        else
            all_crops_highres="$all_crops_highres,$raster_basename_highres"
        fi

    done
    # low resolution summation 
    sum_and_save_rasters $all_crops "ALL_FOOD_CROPS${area_name}_cropland"

    # high resolution summation 
    bash ${universal_scripts}sum_rasters_highres.sh $all_crops_highres $lowres_region_name "ALL_FOOD_CROPS${area_name}_cropland_highres"

    # second, import and save the remainder of the crops (nonfood crops like alphalfa, cotton, tobacco, etc, but not grassland or pasture)
    for nonfood_crop in "${nonfood_crops[@]}"; do
        raster_basename="${nonfood_crop}${area_name}_cropland"
        raster_basename_highres="${nonfood_crop}${area_name}_cropland_highres"

        # import the crop at highres, resample to low res. Imports both high res and low res rasters.
        import_and_resample_cropland $nonfood_crop $area_category $area_name
        
        # Construct a list of all croplands. If the first crop, then don't add a comma.
        if [ -z "$all_crops" ]; then
            all_crops="$raster_basename"
        else
            all_crops="$all_crops,$raster_basename"
        fi

        # Construct a list of all cropland at high resolution. If the first crop, then don't add a comma.
         if [ -z "$all_crops_highres" ]; then
             all_crops_highres="$raster_basename_highres"
         else
             all_crops_highres="$all_crops_highres,$raster_basename_highres"
         fi
    done
    # low resolution summation 
    sum_and_save_rasters $all_crops "ALL_CROPS${area_name}_cropland"

    # high resolution summation 
    bash ${universal_scripts}sum_rasters_highres.sh $all_crops_highres $lowres_region_name "ALL_CROPS${area_name}_cropland_highres"

    # set the region to high res, fill nulls with zero, then return back to low res
    echo "setting null values to zero for ALL_CROPS${area_name}_cropland_highres..."
    g.region save=temp_lowres_region --overwrite
    g.region rast="ALL_CROPS${area_name}_cropland_highres"
    # important to ensure subtraction occurs for near-zero values
    r.null null=0 map="ALL_CROPS${area_name}_cropland_highres"
    g.region region=temp_lowres_region

done

echo ""
echo "All raster areas summed for all crops"
echo ""
