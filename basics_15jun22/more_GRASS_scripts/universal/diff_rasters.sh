#!/bin/bash

function plot_diffs() {
    raster_saved_png=$1
    description=$2
    gold_standard=$3
    raster2=$4
    color_args=$5
    min_max_std=$6
    max_ratio=2
    min_ratio=-2
    r.mapcalc "diff = ($raster2 - $gold_standard) / $gold_standard"
    r.mapcalc "${raster_saved_png} = if(diff > $max_ratio, $max_ratio, if(diff < $min_ratio, $min_ratio, diff))"
        # r.mapcalc "diffcapped = if(diff>2,2,diff)"
    # r.mapcalc "diffcappedlower = if(diffcapped<-2,-2,diffcapped)"

    ./difference_colors3.sh $raster_saved_png $color_args $min_max_std

    echo "Expected ${raster2} to be close to gold standard ${gold_standard}"
    echo "Positive values: fraction more than gold standard as ratio of gold standard."
    echo "Negative values: fraction less than gold standard as ratio of gold standard."

    # save to home dir (where same maximum is set for both)
    ./quick_display.sh $raster_saved_png . $max_ratio $min_ratio "${description}"

}

gold_standard=$1
raster2=$2

plot_diffs plant_more_just_ratios "Results shown as fraction of historical data. Orange/pink: model was too high. Blue/cyan: model was too low." $gold_standard $raster2 classic_white

plot_diffs plant_more_bad_good "Results shown as fraction of historical data. Green: model correct within 50%. Red: model outside 50-150% range." $gold_standard $raster2 bad_good -5,5,0.5
