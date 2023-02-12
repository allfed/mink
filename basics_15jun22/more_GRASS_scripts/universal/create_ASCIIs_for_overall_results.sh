#!/bin/bash
crops_caps_list=(
    # "POTA"
    "MAIZ"
    "RAPE"
    "WHEA"
    "SOYB"
)
crops_long_list=(
    # "potatoes"
    "maize"
    "rapeseed"
    "wheat"
    "soybean"
)


for i in "${!crops_caps_list[@]}"; do
    crop_caps="${crops_caps_list[i]}"
    crop_long="${crops_long_list[i]}"

    historical="${crop_caps}_yield"
    control="379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_${crop_long}__Feb8AUS150tg_wet_overall_yield"
    catastrophe="379_Outdoor_crops_catastrophe_BestYield_noGCMcalendar_p0_${crop_long}__Feb8AUS150tg_wet_overall_yield"
    # greenhouse="379_Greenhouse_catastrophe_BestYield_noGCMcalendar_p0_${crop_long}__TESTUSA150tg_wet_overall_yield"

    ./save_ascii.sh $historical 
    ./save_ascii.sh $control 
    ./save_ascii.sh $catastrophe 
    # ./save_ascii.sh $greenhouse 

    echo "cp ${historical}.asc ../../../"
    echo "cp ${control}.asc ../../../"
    echo "cp ${catastrophe}.asc ../../../"
    # echo "cp ${greenhouse}.asc ../../../"

    cp "${historical}.asc" ../../../
    cp "${control}.asc" ../../../
    cp "${catastrophe}.asc" ../../../
    # cp "${greenhouse}.asc" ../../../
done
