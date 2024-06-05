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

# now, we want to multiply the initial mask (the weather file) by unioned_megaenvironment_masks so as to exclude any regions that are not in unioned_megaenvironment_masks
# also ensure the value remains 1 or null always
r.mapcalc "${final_mask} = if(isnull($initial_mask * unioned_megaenvironment_masks), null(),1)"


# Below, we make the cultivar's megaenvironmnet high resolution


# save the current low resolution (crop model resolution) region
g.region save=temp_lowres_region --overwrite

# just in case we error out, don't want to have a wierd resolution after
cleanup() {
    g.region region=temp_lowres_region
    echo "reset to original region"
}

# on exit or error, cleanup back to original low res region
trap cleanup EXIT ERR

# Save the north, south, east, west boundaries and resolutions for very lowres (the crop model grid resolution)
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')

g.region rast="${initial_mask}_highres"

# set the north, south, east, west boundaries back to the original low res boundaries
g.region n=$n s=$s w=$w e=$e

r.mapcalc "${final_mask}_highres = $final_mask * ${initial_mask}_highres"

echo "final_mask"
echo "${final_mask}"
echo "final_mask}_highres"
echo "${final_mask}_highres"
r.info "${final_mask}_highres"

# return to crop model resolution
g.region region=temp_lowres_region

