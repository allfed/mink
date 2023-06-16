#!/bin/bash

initial_mask=$1
list_of_megaenvironment_masks=$2
final_mask=$3

# echo initial_mask
# echo $initial_mask 
# echo list_of_megaenvironment_masks
# echo $list_of_megaenvironment_masks 
# echo final_mask
# echo $final_mask


# all the places megaenvironment is applicable, unioned_megaenvironment_masks equals 1
#r.patch input=list_of_megaenvironment_masks output=unioned_megaenvironment_masks

# now, we want to multiply the initial mask by unioned_megaenvironment_masks so as to exclude any regions that are not in unioned_megaenvironment_masks
#r.mapcalc "$final_mask = $initial_mask * unioned_megaenvironment_masks"

# Parse list_of_megaenvironment_masks as an array
#IFS=',' read -r -a array <<< "$list_of_megaenvironment_masks"

#echo "array"
#echo $array

r.patch input=$list_of_megaenvironment_masks output=unioned_megaenvironment_masks --quiet --overwrite
# all the places megaenvironment is applicable, unioned_megaenvironment_masks equals 1
#r.patch input=${array[@]} output=unioned_megaenvironment_masks


#don't print any progress bar
export GRASS_VERBOSE=0

# now, we want to multiply the initial mask by unioned_megaenvironment_masks so as to exclude any regions that are not in unioned_megaenvironment_masks
r.mapcalc expression="$final_mask = $initial_mask * unioned_megaenvironment_masks"

# echo "nonnull"
# r.stats -c input=$final_mask

# echo "null"
# r.stats -c -n input=$final_mask