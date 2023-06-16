#!/bin/bash

initial_mask=$1
list_of_megaenvironment_masks=$2
final_mask=$3

# given a weather file (with locations we have weather) and a series of megaenvironment rasters, we calculate all the regions where we should be running our crop model (which includes all the non-null megaenvironment and weather regions and excludes anywhere the weather is null, or one of the megaenvironments is null)

# note that this is typically done ONLY for a single cultivar (which can have multiple megaenvironments)
# and then the weather mask is used to narrow this down to regions of interest
# this allows us to constrain the crop growth to just the regions where the crop is grown

# echo "" > log.txt

# all the places megaenvironment is applicable, this series equals 1. if always null, stays null
r.series input=$list_of_megaenvironment_masks method="maximum" output=unioned_megaenvironment_masks --quiet --overwrite

# r.patch input=$list_of_megaenvironment_masks output=unioned_megaenvironment_masks --quiet --overwrite

# loop through each megaenvironment mask and print ASCII representation
# for megaenv_mask in ${list_of_megaenvironment_masks//,/ }; do
#     echo "" >> log.txt
#     echo "the raster for megaenvironment mask $megaenv_mask" >> log.txt
#     r.out.ascii input=$megaenv_mask output=- > $megaenv_mask.asc
#     cat "$megaenv_mask.asc" >> log.txt
# done

# echo "" >> log.txt
# echo "the raster with all the megaenvironments" >> log.txt
# r.out.ascii input=unioned_megaenvironment_masks output=- > unioned_megaenvironment_masks.asc
# cat "unioned_megaenvironment_masks.asc" >> log.txt

#don't print any progress bar
export GRASS_VERBOSE=0

# # now, we want to multiply the initial mask (the weather file) by unioned_megaenvironment_masks so as to exclude any regions that are not in unioned_megaenvironment_masks
r.mapcalc "$final_mask = $initial_mask * unioned_megaenvironment_masks"

# echo "" >> log.txt
# echo "the raster for the weather" >> log.txt
# r.out.ascii input=$initial_mask output=- > $initial_mask.asc
# cat "$initial_mask.asc" >> log.txt


# echo "" >> log.txt
# echo "the final combined mask which has extra nans" >> log.txt
# r.out.ascii input=$final_mask output=- > $final_mask.asc
# cat "$final_mask.asc" >> log.txt
