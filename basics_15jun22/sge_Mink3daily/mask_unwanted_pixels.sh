#!/bin/bash
spam_raster_to_use_for_mask=$1
region_to_use=$2
minimum_physical_area=$3
growing_radius=$4



# example usage:

# r.mapcalc "deleteme_raster_representing_region=1"  2>&1 | grep -v %
# ./mask_unwanted_pixels.sh WHEA_yield deleteme_raster_representing_region 0.05 0


# clear out the GRASS magic mask name
g.remove MASK 2>&1 | grep -v "" # silenced


# find all the pixels that meet the criterion for being relevant
# old way, just looking at pin-prick style
#  g.region rast=$spam_raster_to_use_for_mask # set the region to match up with the desired raster
# r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())"

# we need to figure out which is coarser, the desired region or the masking source raster


# echo "    ++ masking ++"
# echo "spam_raster_to_use_for_mask"
# echo $spam_raster_to_use_for_mask


# first the mask raster
g.region rast=$spam_raster_to_use_for_mask
nsres_from_raster=`g.region -g | grep nsres | cut -d= -f2`
echo "nsres_from_raster"
echo $nsres_from_raster
# echo "setting desired region"
# echo $region_to_use
# second the desired region
eval g.region rast=$region_to_use
nsres_from_region=`g.region -g | grep nsres | cut -d= -f2`

# echo "setting desired region"
raster_res_is_coarser_than_region_res=`echo "if($nsres_from_raster >= $nsres_from_region) {1} else {0}" | bc`

# we should already be in the target region...
if [ $raster_res_is_coarser_than_region_res = 1 ]; then
  # do it the old way using pin pricks
  # echo "      __ masking pin-prick __"
  r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
else
  # do a statistical coarsening to make sure we catch everything
  # echo "      __ masking coarsening __"
  r.resamp.stats input=$spam_raster_to_use_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
  r.mapcalc deleteme_initial_spam_ungrown = "if(deleteme_coarse_mask_ungrown >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
fi


eval g.region rast=$region_to_use


# also, choose the pixels around those deemed relevant; note that this stays in the original region
#   so desired pixels on the edges will not have a buffer to the outside (this is usually not a big deal)
r.grow input=deleteme_initial_spam_ungrown output=deleteme_crop_mask radius=$growing_radius --o
# finally, make the final mask a clean 1's and nulls version of the previous step
r.mapcalc MASK = "if(isnull(deleteme_crop_mask),null(),1)" 2>&1 | grep -v %
