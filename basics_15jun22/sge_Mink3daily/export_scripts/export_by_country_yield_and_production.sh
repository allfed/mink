#!/bin/bash

###

# Summary of script operation:

#     Saving Current Region: The current region is saved to temp_lowres_region.

#     Removing Any Masks: Existing raster masks are removed with r.mask -r.

#     Reading Crop Parameters: The script takes two input parameters, the crop name and yield raster name, and reads a number of variables from another script.

#     Setting Up Region: The north, south, east, west boundaries and resolutions (nsres and ewres) are saved, and a high-resolution cropland raster is loaded and set to this region.

#     Checking Resolution: The script checks if the high-resolution cropland raster has higher resolution than the saved values, and aborts if not.

#     Resampling Low-Res Data: Low-resolution yield data is resampled to match the high-resolution raster.

#     Calculating Production: A new raster, production_highres, is computed by multiplying the high-resolution yield raster with the high-resolution cropland raster.

#     Iterating Through Countries: The script iterates through countries, using a vector layer, and does the following for each country:
#         Extracts country polygon and ISO code.
#         Creates a mask for the country.
#         Reads and sums non-null cells to total production.
#         Reads and sums high-resolution to total cropland area.
#         Writes country name, ISO code, total production, and total area to a CSV file.
#
#     Cleaning Up: The script removes any raster masks and restores the original region.

###

set -e # quit on any error

if [ $# -lt 2 ]; then
  echo "Usage: $0 crop_caps_4_letters raster_to_export save_folder"
  exit 1
fi

# example region that should work as low res enough (if using spam data)
# g.region n=65 s=-65 w=-170 e=170 nsres=1.88405797101449 ewres=1.25

g.region save=temp_lowres_region --overwrite


# example usage:

# ./export_by_country_yield_and_production.sh WHEA WHEA_yield wth_historical# historical wheat yield
# ./export_by_country_yield_and_production.sh WHEA 379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul12_specific_wet_overall_yield wth_control# model wheat yield

r.mask -r

crop_caps=$1
yield_raster=$2
save_folder=$3 # location past git_root to save to

. ../default_paths_etc.sh # import git_root and spam_data_folder variables

# Save the north, south, east, west boundaries and resolutions
read n s w e nsres ewres <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 } $1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# load high resolution cropland
r.in.gdal input="${spam_data_folder}spam2010V2r0_global_H_${crop_caps}_A.tif" output="${crop_caps}_cropland_highres" --overwrite

# set region (including resolution and boundaries) to the high resolution cropland
g.region rast="${crop_caps}_cropland_highres"

# set the north, south, east, west boundaries back to the original
g.region n=$n s=$s w=$w e=$e

# read the new nsres and ewres after setting the high-res region
read new_nsres new_ewres <<< $(g.region -g | awk -F'=' '$1=="nsres" { print $2 } $1=="ewres" { print $2 }')

# check if the high res cropland resolution is in fact higher for both nsres and ewres
if (( $(echo "$new_nsres < $nsres" | bc -l) )) && (( $(echo "$new_ewres < $ewres" | bc -l) )); then
  echo "High res cropland resolution is higher for both nsres and ewres. Continuing."
else
  echo "ERROR: High res cropland resolution is NOT higher for both nsres and ewres! Aborting."
  exit 1
fi

# set the north south and east west boundaries back to the original
g.region n=$n
g.region s=$s
g.region w=$w
g.region e=$e

echo ""
echo "resample low res yield data to match high resolution raster"
r.resample input=$yield_raster output="${yield_raster}_highres" --overwrite

echo ""
echo "calculate the high resolution yield by multiplying area with yield per hectare"
r.mapcalc "production_highres = ${yield_raster}_highres * ${crop_caps}_cropland_highres"

echo ""
echo "removing zero values from area and yield rasters"
r.null setnull=0 map=production_highres
r.null setnull=0 map="${crop_caps}_cropland_highres"

echo ""
echo " connect to the vector so can use sql on each country "
db.connect driver=dbf database='$GISDBASE/$LOCATION_NAME/$MAPSET/dbf/'
db.connect -p
v.db.connect driver=dbf database='/mnt/data/grassdata/world/DSSAT_essentials_12may11/dbf/' map=cntry05 table=cntry05 -o

# useful command:
# v.info -c cntry05 --quiet


# Set the Internal Field Separator to newline
IFS=$'\n'

countries=$(v.db.select map=cntry05 column=CNTRY_NAME -c)

csv_exported_name="by_country_${yield_raster}.csv"

# Write the csv header
echo "Country,iso_a3,Production (kg wet),Area (hectares)" > $csv_exported_name

# add row for each country to csv file
for country in $countries; do

  # Handle special characters in country names
  country=$(echo "$country" | sed "s/'/''/g")

  # # Extract the country polygon and iso3 country code
  country_cat=$(v.db.select map=cntry05 where="CNTRY_NAME = '$country'" column=cat -c)
  country_iso=$(v.db.select map=cntry05 where="CNTRY_NAME = '$country'" column="ISO_3DIGIT" -c)

  if [ ! "$country_iso" ] ; then
    echo ""
    echo "skipping $country due to missing iso3"
    continue
  fi

  # don't recalculate if country exists. That's fine.
  eval `g.findfile element=vector file="country_vector_${country_iso}"`
  if [ ! "$file" ] ; then
    echo "as it is the first time the country outline vector for this country has been imported, we are creating them"
    v.extract input=cntry05 output="country_vector_${country_iso}" list=$country_cat --o --q
    v.to.rast input="country_vector_${country_iso}" output="country_mask_${country_iso}" use=val value=1  --o --q
  fi

  # Create a mask only including this country 
  r.mask input="country_mask_${country_iso}" --o

  read non_null_cells_country_production <<< $(r.univar -g production_highres | awk -F'=' '/^n=/ { print $2 } /^sum=/ { print $2 }')
  non_null_cells=$(echo $non_null_cells_country_production | cut -d ' ' -f 1)
  country_production=$(echo $non_null_cells_country_production | cut -d ' ' -f 2)

  echo ""
  echo "$country"

  # Check if country_production is zero or empty
  if [ -z "$country_production" ] || [ "$country_production" == "0" ]; then
    echo "country production is zero or empty, skipping..."
    echo ""
    continue
  fi

  # get the high resolution cropland area sum over the whole country
  country_area=$(r.univar -g "${crop_caps}_cropland_highres" | grep 'sum' | awk -F'=' '{print $2}')

  # Check if country_area is zero or empty
  if [ -z "$country_area" ] || [ "$country_area" == "0" ]; then
    echo "country area is zero or empty, skipping..."
    echo ""
    continue
  fi
  echo "country_iso"
  echo $country_iso
  echo "country_area"
  echo $country_area
  echo "country_production"
  echo $country_production
  echo ""

  echo "\"$country\",$country_iso,$country_production,$country_area" >> $csv_exported_name
done

mv $csv_exported_name "$git_root/$save_folder"

# remove any mask
r.mask -r

# return to the original region
g.region region=temp_lowres_region
