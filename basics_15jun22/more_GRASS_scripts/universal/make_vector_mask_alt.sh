#!/bin/bash


if [ $# -lt 3 ]; then
  echo "Usage: $0 vector column values_to_pull_csv align_raster output_raster_for_mask [invert] [verbose]"
  echo "use \"World\" for all units; to get verbose without inversion, send \"\" for invert"
  exit 1
fi

             vector=$1
             column=$2
 values_to_pull_csv=$3
       align_raster=$4
      output_raster=$5
             invert=$6
              debug=$7

if [ -n "$invert" ]; then
  invert_flag="-r"
else
  invert_flag=""
fi




### now doing some special one-off's for countries...

magic_large_number=1000000

country_vector=$vector
country_name_column=$column
max_name_length=15

echo " -- mask for: [$values_to_pull_csv] --"

if [ "$values_to_pull_csv" = "World" ]; then
  whole_world_flag=1
  cat_list="000-$magic_large_number"
  n_countries=1
  n_countries_found=1

else
  whole_world_flag=0


#****************************#
### pull out the desired country/ies
delimiter=,
n_countries=`echo "$values_to_pull_csv" | awk -F "$delimiter" '{ print NF }'`

# i have had some trouble with large where statements since it appears the
# the allocated buffer is only 1024 characters...
# one possible way around this is to create a list of the category numbers and hope that this
# is smaller. supposing 3 digits plus a comma, we can fit 1024/4 = 256 thingees in
cat_list=""
for (( country_num=1 ; country_num <= n_countries ; country_num++ ))
do
  country_name=`echo "$values_to_pull_csv" | cut -d, -f${country_num}`

  if [ $country_name_column != "cat" ]; then
  name_has_apostrophe=`echo "$country_name" | grep "'"`

  # here is a workaround for places with apostrophe's, like Cote d'Ivoire
  # counting up the number of nodes in the topology. if that is zero, we
  # will select using the "LIKE" sql command and tell people to replace the
  # apostrophe's with percent signs as wildcards...

  if [ -n "$name_has_apostrophe" ]; then
    country_name_n_chunks=`echo "$country_name" | awk -F "'" '{ print NF }'`

    delimiter="'"

    # initialize with the first chunk
    chunk=1
    chunk_value=`echo "$country_name" | cut -d"$delimiter" -f$chunk `
    where_string="$country_name_column LIKE '%$chunk_value%'"
    for (( chunk=2 ; chunk <= $country_name_n_chunks ; chunk++ ))
    do
      chunk_value=`echo "$country_name" | cut -d"$delimiter" -f$chunk `
      where_string="${where_string} AND $country_name_column LIKE '%$chunk_value%'"
    done

    new_piece="($where_string)"
  else
    new_piece="($country_name_column = '$country_name')"
  fi

  cat=`v.db.select $country_vector where="$new_piece" col=cat -c | tr "\n" "," | sed "s/,$//g"` # translate newlines into commas, clear out final commas


  else
    cat=$country_name
  fi # end if col name = cat

  cat_list="$cat_list,$cat"

  if [ -n "$debug" ]; then
    echo "country = [$country_name] ; cat = [$cat]"
  fi
done

fi # end if whole world


cat_list=${cat_list:1}

### pull out the desired pieces
v.extract input=$country_vector output=deleteme_vectors_we_want list=$cat_list --o --q $invert_flag

#n_found=`v.db.select deleteme_vectors_we_want -c col=CNTRY_NAME | wc -l`
n_found=`v.db.select deleteme_vectors_we_want -c col=$country_name_column | wc -l`

if [ $n_countries -ne $n_found ]; then
  echo "$n_countries requested, $n_found extracted   !!! NOT ALL REQUESTED COUNTRIES WERE FOUND !!!"
else
  echo "$n_countries requested, $n_found extracted"
fi

### create a mask
# old way that makes non-registered masks due to boundary issues when g.region-ing to the vector...
#g.region align=$align_raster
#g.region vect=deleteme_vectors_we_want     # pull the extent from the extracted vectors
#
#v.to.rast input=deleteme_vectors_we_want output=$output_raster use=val value=1 --o --q


# to try to avoid registration problems, we're gonna have to suck it down and start
# with the same region every time, and then zoom down...

#g.region rast=$align_raster
g.region vect=deleteme_vectors_we_want     # pull the extent from the extracted vectors
align_region_to_raster.sh $align_raster

v.to.rast input=deleteme_vectors_we_want output=deleteme_large_mask use=val value=1 --o --q

#g.region zoom=deleteme_large_mask

r.mapcalc $output_raster = "deleteme_large_mask"


