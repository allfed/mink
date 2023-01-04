#!/bin/bash


####
# this script tries to make pretty post-script maps

#output_directory=~grass/grass_scripts/maps/new/ # Beware the MAGIC NUMBER!!! this should match with new_map_function.sh

usage_message()
{
  echo ""
  echo "Usage: $0 -r raster_name -C country_names_csv -c comment -f file_name -s sample_dpi [-l] [-t n_cols_in_legend] [-e] [-L] [-v vector_csv] [-Z]"
  echo ""
  echo "raster_name is the raster to be processed"
  echo "country_names_csv is a csv list of the names of the countries you wish to display"
  echo "comment is extra comments to be displayed in the title"
  echo "file_name is where to write the file out (no suffix)"
  echo "sample_dpi is the resolution to resample the map for a PNG version"
  echo "-l means output as landscape"
  echo "-t means assume categorical values (default = continuous)"
  echo "-e means try for EPS rather than PS"
  echo "-L means list all the country names and exit"
  echo "-v means put these vector boundaries on as well"
  echo "-Z means perform a zero-to-black operation within the country"
  echo ""
}


if [ $# -lt 1 ]; then
  usage_message
  exit 1
fi

# initialize the value_type to continuous and the index to 1
OPTIND=1
value_type=cont
landscape_flag=0
vector_names_csv=cntry05 # default...
list_and_exit_flag=0
zero_to_black_flag=0
while getopts r:C:c:v:f:s:lt:eLZ provided_value
do
  case "$provided_value" in
    r) raster_name="$OPTARG";;
    C) country_names_csv="$OPTARG" ; echo ".{$country_names_csv}.";;
    c) raster_comment="$OPTARG";;
    f) file_name="$OPTARG";;
    s) sample_dpi="$OPTARG";;
    l) landscape_flag=1;;
    t) value_type="cat"; n_cols="$OPTARG" ;;
    e) eps_flag="-e";;
    L) list_and_exit_flag="1";;
    v) extra_vectors_to_use=",$OPTARG" ;;
    Z) zero_to_black_flag="1";;
    ?) usage_message;;
  esac
done


country_vector=cntry05
country_name_column=CNTRY_NAME

if [ $list_and_exit_flag -eq 1 ]; then
  eval "v.db.select $country_vector col=$country_name_column -c" | sort
  exit
fi

test_string=\
"${raster_name}
${country_names_csv}
${raster_coment}
${file_name}
${sample_dpi}"

test_lines=`echo "$test_string" | wc -l`

if [ -z "$raster_comment" ]; then
  echo "resetting comment to a single dot"
  raster_comment="."
fi

echo "
   . r) $raster_name
   . C) $country_names_csv
   . c) $raster_comment
   . f) $file_name
   . s) $sample_dpi
   . l) $landscape_flag
   . t) $value_type $n_cols
   . e) $eps_flag
"

if [ $test_lines -ne 5 ]; then
  usage_message
  exit 1
fi


g.remove rast=deleteme_mask


### pull out the desired country/ies
delimiter=,
n_countries=`echo "$country_names_csv" | awk -F "$delimiter" '{ print NF }'`

# i have had some trouble with large where statements since it appears the
# the allocated buffer is only 1024 characters...
# one possible way around this is to create a list of the category numbers and hope that this
# is smaller. supposing 3 digits plus a comma, we can fit 1024/4 = 256 thingees in
cat_list=""
for (( country_num=1 ; country_num <= n_countries ; country_num++ ))
do
  country_name=`echo "$country_names_csv" | cut -d, -f${country_num}`

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
#echo ",,${chunk_value},,"
    where_string="$country_name_column LIKE '%$chunk_value%'"
    for (( chunk=2 ; chunk <= $country_name_n_chunks ; chunk++ ))
    do
      chunk_value=`echo "$country_name" | cut -d"$delimiter" -f$chunk `
#echo ",,${chunk_value},,"
      where_string="${where_string} AND $country_name_column LIKE '%$chunk_value%'"
    done
    
    new_piece="($where_string)" 
  else
    new_piece="($country_name_column = '$country_name')"
  fi

#v.extract input=$country_vector output=deleteme_vectors_we_want where="$where_statement" --o --q
  cat=`v.db.select $country_vector where="$new_piece" col=cat -c`

  cat_list="$cat_list,$cat"

  echo "country = [$country_name] ; cat = [$cat]"
done
cat_list=${cat_list:1}
echo "cat list length = ${#cat_list} (max = 1023)"

### old style



### pull out the desired pieces
echo "... extracting vectors ..."
#v.extract input=$country_vector output=deleteme_vectors_we_want where="$where_statement" --o --q
v.extract input=$country_vector output=deleteme_vectors_we_want list=$cat_list --o --q

n_found=`v.db.select deleteme_vectors_we_want -c col=CNTRY_NAME | wc -l`

echo "$n_countries requested, $n_found extracted"
if [ $n_countries -ne $n_found ]; then
  echo "   !!! NOT ALL REQUESTED COUNTRIES WERE FOUND !!!"
raster_comment="${raster_comment}
WARNING!!!
the number of extracted areas ($n_found) is not the same as the number of countries requested ($n_countries)"
fi

### create a mask
echo "... creating mask ..."
g.region rast=$raster_name                 # match up against raw raster
g.region vect=deleteme_vectors_we_want     # pull the extent from the extracted vectors

v.to.rast input=deleteme_vectors_we_want output=deleteme_mask use=val value=1 --o --q

# expand out by a small fraction
magic_buffer_fraction=0.05
current_region=`g.region -g`
  old_n=`echo "$current_region" | sed -n "1p" | cut -d= -f2`
  old_s=`echo "$current_region" | sed -n "2p" | cut -d= -f2`
  old_w=`echo "$current_region" | sed -n "3p" | cut -d= -f2`
  old_e=`echo "$current_region" | sed -n "4p" | cut -d= -f2`
  old_ns_res=`echo "$current_region" | sed -n "5p" | cut -d= -f2`
  old_ew_res=`echo "$current_region" | sed -n "6p" | cut -d= -f2`
  old_rows=`echo "$current_region" | sed -n "7p" | cut -d= -f2`
  old_cols=`echo "$current_region" | sed -n "8p" | cut -d= -f2`

  # initialize the "use" stuff

      use_w=$old_w
      use_e=$old_e

  # determine if region

#echo "old: $old_n $old_s $old_w $old_e"

### ok, let's say, if there is anything within a certain buffer of 180, try shifting
magic_meridian_buffer_size=10
test_string=`echo "if ( sqrt(($old_w - (-180))^2) < $magic_meridian_buffer_size || sqrt(($old_e - 180)^2) < $magic_meridian_buffer_size ) {1} else {0}" | bc`

magic_shift=153
  if [ $test_string -eq 1 ]; then
    echo "shifting by $magic_shift EW, and zooming"
    #echo "original w=$old_w e=$old_e"
    shift_w=`echo "$old_w + $magic_shift" | bc`
    shift_e=`echo "$old_e + $magic_shift" | bc`
    #echo "shifted  w=$shift_w e=$shift_e"
    g.region w=$shift_w e=$shift_e
    echo "zooming..."
    g.region zoom=deleteme_mask
    #echo "re-retrieving stuff"
current_region=`g.region -g`
  old_n=`echo "$current_region" | sed -n "1p" | cut -d= -f2`
  old_s=`echo "$current_region" | sed -n "2p" | cut -d= -f2`
  zoomed_w=`echo "$current_region" | sed -n "3p" | cut -d= -f2`
  zoomed_e=`echo "$current_region" | sed -n "4p" | cut -d= -f2`
  old_ns_res=`echo "$current_region" | sed -n "5p" | cut -d= -f2`
  old_ew_res=`echo "$current_region" | sed -n "6p" | cut -d= -f2`
  old_rows=`echo "$current_region" | sed -n "7p" | cut -d= -f2`
  old_cols=`echo "$current_region" | sed -n "8p" | cut -d= -f2`
    if [ "$zoomed_w" != "$old_w" ]; then
      #echo "       keeping the zoomed values"
      use_w=$zoomed_w
      use_e=$zoomed_e
    else
      use_w=$old_w
      use_e=$old_e
    fi
    #echo "after zoomed   w=$use_w e=$use_e"
  fi




  new_n=`echo "scale=6 ; $old_n + $old_rows * $old_ns_res * $magic_buffer_fraction" | bc`
  new_s=`echo "scale=6 ; $old_s - $old_rows * $old_ns_res * $magic_buffer_fraction" | bc`
              #  new_w=`echo "scale=6 ; $old_w - $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
              #  new_e=`echo "scale=6 ; $old_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`

  echo "total BEFORE width = `echo "scale=10 ;  sqrt(($use_w - $use_e)^2) " | bc`"
  test_string=`echo "scale=10 ; if ( sqrt(($use_w - $use_e)^2) * (1 + 2*$magic_buffer_fraction) > 360) {1} else {0}" | bc`
  if [ $test_string -eq 1 ]; then
    # scale it down to halfway to full coverage; that means a quarter each way
    new_w=`echo "scale=6 ; remaining_gap = 360 - sqrt(($use_w - $use_e)^2); $use_w - remaining_gap / 4" | bc`
    new_e=`echo "scale=6 ; remaining_gap = 360 - sqrt(($use_w - $use_e)^2); $use_e + remaining_gap / 4" | bc`
#    new_e=`echo "scale=6 ; $use_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
  else
    # apply the magic buffer zone
    new_w=`echo "scale=6 ; $use_w - $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
    new_e=`echo "scale=6 ; $use_e + $old_rows * $old_ew_res * $magic_buffer_fraction" | bc`
  fi

#echo "new: $new_n $new_s $new_w $new_e"
  new_n=`echo "if ($new_n > 90) {90} else {$new_n}" | bc`
  new_s=`echo "if ($new_s < -90) {-90} else {$new_s}" | bc`

#  echo "scale=10 ; if ( sqrt(($new_w - $new_e)^2) > 360 ) {1} else {0}"
  echo "total width = `echo "scale=10 ;  sqrt(($new_w - $new_e)^2) " | bc`"
  test_string=`echo "scale=10 ; if ( sqrt(($new_w - $new_e)^2) > 360) {1} else {0}" | bc`
  if [ $test_string -eq 1 ]; then
    echo "resetting to old bounds ($old_w/$old_e)"
    # pull just inside the bounds so that vectors are rendered properly
    new_w=$old_w
    new_e=$old_e
#    new_w=`echo "if ($new_w < -180) {180} else {$new_w}" | bc`
#    new_e=`echo "if ($new_e > 180) {180} else {$new_e}" | bc`
  fi
echo "final: $new_n $new_s $new_w $new_e"


  g.region n=$new_n s=$new_s w=$new_w e=$new_e
  g.region align=$raster_name

echo "... masking original values ..."
r.mapcalc deleteme_masked_raster = "$raster_name * deleteme_mask"
r.support deleteme_masked_raster raster=$raster_name
r.colors  deleteme_masked_raster rast=$raster_name


# check for how many missing values there are. if there are no good values, then we will
# skip creating the map and only do the statistics.
  univariate_stats=`r.univar deleteme_masked_raster`
  n_total_pixels=`echo "$univariate_stats" | sed -n "1p" | cut -d" " -f6`
    n_bad_pixels=`echo "$univariate_stats" | sed -n "2p" | cut -d" " -f4`
  echo "n_total = $n_total_pixels ; n_bad = $n_bad_pixels"
if [ $n_total_pixels -eq $n_bad_pixels ]; then
  # this means all of the pixels are bad pixels, so we don't want to make the map
  make_map_flag=0
else
  # this means there are some good pixels, so make the map anyway
  make_map_flag=1
fi


echo "... doing statistics [${output_directory}${file_name}.stats.txt] ..."
  echo "$raster_comment" > ${output_directory}${file_name}.stats.txt
  echo "" >> ${output_directory}${file_name}.stats.txt
  echo "original raster: $raster_name" >> ${output_directory}${file_name}.stats.txt
  echo "values reflect only the masked area for countries:" >> ${output_directory}${file_name}.stats.txt
  echo "$country_names_csv" >> ${output_directory}${file_name}.stats.txt
  echo "" >> ${output_directory}${file_name}.stats.txt

if [ "$value_type" = "cat" ]; then
  # we have categorical data, get the "report"
  r.report deleteme_masked_raster units=c,p,k -n >> ${output_directory}${file_name}.stats.txt
else
  # we have continuous data, get the univariate statistics
#  r.univar deleteme_masked_raster >> ${output_directory}${file_name}.stats.txt
  echo "$univariate_stats" >> ${output_directory}${file_name}.stats.txt
fi


if [ $make_map_flag -eq 1 ]; then

echo "... rendering maps ..."
# set up the flags for plotting
if [ $landscape_flag -eq 1 ]; then
  landscape_flag_to_use="-l"
fi

if [ "$value_type" = "cat" ]; then
  value_options_to_use="-t $n_cols"
else
  value_options_to_use=""
fi

if [ $zero_to_black_flag = 1 ]; then
  zero_to_black.sh deleteme_masked_raster
fi

~grass/grass_scripts/universal/new_map_function.sh -n -r deleteme_masked_raster -c "$raster_comment" -f $file_name -s $sample_dpi $landscape_flag_to_use $value_options_to_use -v $vector_names_csv$extra_vectors_to_use

fi # end if make_map_flag

if [ $n_countries -ne $n_found ]; then
  echo ""
  echo "again:   !!! NOT ALL REQUESTED COUNTRIES WERE FOUND !!! ($n_found of $n_countries requested)"
  echo ""
fi

echo "... all done ..."

#  echo "Usage: $0 -r raster_name -c comment [-v vector_names_csv] -f file_name [-s sample_dpi] [-l] [-t n_cols_in_legend] [-e]"









