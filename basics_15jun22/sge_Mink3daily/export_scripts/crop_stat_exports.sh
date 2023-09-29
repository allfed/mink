#!/bin/bash

# Run the python script to convert AGMIP files first!
# should be:
# python3 convert_AGMIP_nc4s_to_tifs.py
# and run associated scripts to reformat and export

# Declare arrays for crops, their corresponding descriptions, and crop_caps
# declare -a crops=("maize" "rice" "soybeans" "potato" "rapeseed")
# declare -a descriptions=("Sep26_genSNX" "SepX_genSNX" "Sep27_genSNX" "Sep27_genSNX" "Sep27_genSNX")
# declare -a crop_caps=("MAIZ" "RICE" "SOYB" "POTA" "RAPE")
declare -a crops=("maize" "soybeans")
declare -a crops_agmip=("mai" "soy")
declare -a descriptions=("Sep26_genSNX" "Sep27_genSNX")
declare -a crop_caps=("MAIZ" "SOYB")

# Loop through each crop, its corresponding description, and crop_caps
for index in "${!crops[@]}"; do
    crop=${crops[$index]}
    crop_agmip=${crops_agmip[$index]}
    description=${descriptions[$index]}
    current_crop_caps=${crop_caps[$index]}

    # Generate AGMIP
    ./render_all_rasters_same_scale.sh . AGMIP_princeton_RF_yield_${crop_agmip}_lowres_cleaned_2005 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_averaged_RF


    # Generate AGMIP planting months and comparison outdoor growing planting months
    ./render_all_rasters_same_scale.sh . AGMIP_princeton_RF_planting_months_${crop_agmip}_lowres_cleaned_2005     379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_planting_month_RF

    # Render rasters (if not done already..)
    ./render_all_rasters_same_scale.sh . ${current_crop_caps}_yield 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_overall_yield 
    
    # View the AGMIP vs model rasters
    echo ""
    echo "run the command outside singularity to view:"
    echo "firefox AGMIP_princeton_RF_yield_${crop_agmip}_lowres_cleaned_2005.png"
    echo "firefox 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_averaged_RF.png"
    echo ""

    # View the AGMIP vs model rasters
    echo ""
    echo "run the command outside singularity to view:"
    echo "firefox AGMIP_princeton_RF_planting_months_${crop_agmip}_lowres_cleaned_2005.png"
    echo "firefox 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_planting_month_RF.png"
    echo ""

    # View the SPAM vs model rasters
    echo ""
    echo "run the command outside singularity to view:"
    echo "firefox ${current_crop_caps}_yield.png"
    echo "firefox 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop}__${description}_wet_overall_yield.png"
    echo ""
    
    
done
