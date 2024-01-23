#!/bin/bash

# Move raster from high to low resolution by summing
# this function requires us to already be at the low resolution.

set -e # exit on error

if [ $# -ne 3 ]; then
  echo "Function is to convert from low resolution to high resolution raster, summing the cells to get"
  echo "from high to low resolution."
  echo "NOTE: this is not using r.stats.resamp as it was reported to have bugs on online forums for summation."
  echo "Usage: $0 <highres_raster> <lowres_region_name> <coarsened_raster_name>"
  echo " Example usages:"
  echo " $0 spam2010V2r0_global_H_WHEA_A.tif temp_lowres_region WHEA_cropland"
  exit 1
fi

highres_raster=$1
lowres_region_name=$2
coarsened_raster=$3


# just in case we error out, don't want to have a wierd resolution after
cleanup() {
    g.region region=$lowres_region_name
    echo "reset to original region"
}

# on exit or error, cleanup back to original low res region
trap cleanup EXIT ERR



# Function to convert hh:mm:ss (or hh:mm or hh) format to decimal degrees
convert_to_decimal() {
    local coord_str=$1
    local h=0
    local m=0
    local s=0

    # Count the number of colons in the string
    local colon_count=$(grep -o ":" <<< "$coord_str" | wc -l)

    if [[ $colon_count -eq 0 ]]; then
        # Only hours
        h=$coord_str
    elif [[ $colon_count -eq 1 ]]; then
        # Hours and minutes
        IFS=: read -r h m <<< "$coord_str"
    elif [[ $colon_count -eq 2 ]]; then
        # Hours, minutes, and seconds
        IFS=: read -r h m s <<< "$coord_str"
    else
        # Incorrect format, return error
        echo "Error: Incorrect coord format"
        exit 1
    fi

    # Convert to decimal degrees
    echo "scale=12; $h + $m/60 + $s/3600" | bc
}

g.region region=$lowres_region_name

# Save the north, south, east, west boundaries and resolutions for lowres
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# set region (including resolution and boundaries) to the high resolution raster
g.region rast=$highres_raster

# set the north, south, east, west boundaries back to the original low res boundaries
g.region n=$n s=$s w=$w e=$e

# get the high resolution raster pixel sizes
read nsres_highres ewres_highres <<< $(r.info -gs $highres_raster | awk -F'=' '$1=="nsres" { print $2 } $1=="ewres" { print $2 }')


# Get the high resolution raster pixel sizes
read nsres_highres ewres_highres <<< $(r.info -gs $highres_raster | awk -F'=' '$1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# Converting to decimal degrees
nsdecimal_degrees=$(convert_to_decimal "$nsres_highres")
ewdecimal_degrees=$(convert_to_decimal "$ewres_highres")



if (( $(echo "$nsdecimal_degrees < $nsres" | bc -l) )) && (( $(echo "$ewdecimal_degrees < $ewres" | bc -l) )); then
  echo "High res pixel size is smaller for both nsres and ewres. Continuing."
else
  echo "north-south high res: nsdecimal_degrees: $nsdecimal_degrees, hours:minutes:seconds: $nsres_highres"
  echo "east-west   high res:   ewdecimal_degrees: $ewdecimal_degrees, hours:minutes:seconds: $ewres_highres"
  echo "north-south  low res: nsdecimal_degrees: $nsres"
  echo "east-west    low res:   ewdecimal_degrees: $nsres"
  echo "ERROR: High res pixel size is NOT smaller for both nsres and ewres! Aborting."
  echo "Most likely, you called $0 to resample to coarse resolution when you didn't need to."
  exit 1
fi

# calculate average number of (smaller) high resolution cells in a coarse (bigger, low resolution) pixel 
n_cells=$(echo "scale=12;($nsres/$nsdecimal_degrees) * ($ewres/$ewdecimal_degrees) " | bc)


# Explanation of method to get summation of cropland (or production) to coarser resolution:
# (the numbers represent hectares (or kg harvested) per cell in the raster maps)
#  _____ _____
# |     |     |
# |  1  |  4  |
# |_____|_____|
# |     |     |
# |  2  |  0  |
# |_____|_____|
# 
# mean: (A + B + C + D) / 4
# sum: mean * 4
# 
# coarse map:
#  _____ _____
# |           |
# |           |
# |     7     |
# |           |
# |           |
# |_____ _____|
# 
# 
# In the case of mean of cells with nulls, this doesn't work!
#  _____ 
# |     |
# |  1  |    
# |_____|_____
# |     |     |
# |  2  |  0  |
# |_____|_____|
# 
# mean: (A + C + D) / 3
# but then have to count the number of cells.
# 
# So set nulls to zero:
# 
#  _____ _____
# |     |     |
# |  1  |  0  |
# |_____|_____|
# |     |     |
# |  2  |  0  |
# |_____|_____|
# 
# Now it works. n_cells always equals 4 (or whatever resolution_scaling_x*resolution_scaling_y is).
# 
# mean: (A + B + C + D) / 4
# sum: mean * n_cells
# 
# This works even if course to small are not perfectly aligned on their edges. 
# The average is still by the number of cells.
# 
# Also, the reason we can't use r.resamp.stats with "sum" is online discussion forums report this doesn't work.

# First copy the map to another name for zeroing.
# Then set nulls in the map to zero (for future taking of the mean, and then multiplying by resolution ratio).
r.mapcalc "${coarsened_raster}_highres_zeroed = ${highres_raster}"
r.null null=0 map="${coarsened_raster}_highres_zeroed"

# We've imported at high res. Now move to low res and resample with sum.

# move back to low res
g.region region=$lowres_region_name

# actually resample to high resolution by taking mean of small pixels
# r.resample input="${coarsened_raster}_highres_zeroed" output="${coarsened_raster}_mean" --overwrite
r.resamp.stats input="${coarsened_raster}_highres_zeroed" output="${coarsened_raster}_mean" method=average --overwrite

# multiply the mean of the small pixels by the number of cells, so low resolution pixels contain the sum of high resolution pixels
r.mapcalc "${coarsened_raster} = ${coarsened_raster}_mean * $n_cells"

echo "successfully coarsened raster"
