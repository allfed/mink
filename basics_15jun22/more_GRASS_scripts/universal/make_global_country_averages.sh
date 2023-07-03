#!/bin/bash

raster_to_average_for_each_country=$1



db.connect driver=dbf database='$GISDBASE/$LOCATION_NAME/$MAPSET/dbf/'
db.connect -p
v.db.connect driver=dbf database='/mnt/data/grassdata/world/DSSAT_essentials_12may11/dbf/' map=cntry05 table=cntry05 -o

g.region rast=$raster_to_average_for_each_country



# Set the Internal Field Separator to newline
IFS=$'\n'

countries=$(v.db.select map=cntry05 column=CNTRY_NAME -c)

# Write the header
echo "Country,Average" > "country_${raster_to_average_for_each_country}.csv"

for country in $countries; do
  # Handle special characters in country names
  country=$(echo "$country" | sed "s/'/''/g")
  
  # Extract the country
  country_cat=$(v.db.select map=cntry05 where="CNTRY_NAME = '$country'" column=cat -c)
  v.extract input=cntry05 output=country_vector list=$country_cat --o --q
  
  # Create a mask
  g.region vect=country_vector
  v.to.rast input=country_vector output=country_mask use=val value=1 --o --q
  r.mask input=country_mask --o
  
  # Calculate the average
  stats_output=$(r.stats -n input=$raster_to_average_for_each_country)
  if [ -z "$stats_output" ]; then
    average="No data"
  else
    average=$(echo "$stats_output" | awk '{sum+=$1; count+=1} END {if (count > 0) print sum/count; else print "No data"}')
  fi

  # Write to the CSV file
  echo "\"$country\",$average" >> "country_${raster_to_average_for_each_country}.csv"

  # Remove the mask for the next iteration
  r.mask -r
done

mv "country_${raster_to_average_for_each_country}.csv" ../../../


# Reset IFS to its original value
unset IFS
