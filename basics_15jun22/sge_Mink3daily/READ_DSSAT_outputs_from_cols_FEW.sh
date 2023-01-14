#!/bin/bash

# this is to read in a set of predictions based on what was exported
# using build_dataset_for_DSSAT_monthly_neighborhood_01mar11.sh
#
# at this point (march 2011), we are still using an old beta version
# of DSSAT from july of 2010. eventually, we will make the switch to
# the latest version.
#
# note that wheat does not work properly (which is to say, at all) in
# the july 2010 version of DSSAT, so we have to use an earlier beta
#
# the outputs between these two versions are different starting in column 5.
# that means that if you are _*ONLY*_ interested in the mean and stdev of
# the ACTUAL yields, you could use this script for wheat. if you are interested
# in anything else at all, you should use the other read_DSSAT45_...sh script

if [ $# -lt 1 ]; then
  echo "Usage: $0 prefix_to_import input_magic_code"
  echo ""
  echo "This will import only those DSSAT outputs that begin with the stated prefix."
  echo "For all files in the data directory, use \"\" (empty string)"
  echo ""
  echo "magic_code is the input file prefix for this set (used for looking for the corresponding geography files.)"
  echo ""
  exit 1
fi




################################################################################
# !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!  #
# in general you will only need to comment out or uncomment the renaming lines #
# that appear inside the big for loop at the bottom.                           #
# !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!   !!!  #
################################################################################


# this script is called with at least two command line arguments and sometimes three.
#
# the first argument is the prefix or other search pattern for the filenames you
# want to import. this value gets put into an 'ls' statement, so you can use *'s, ?'s, etc.
#
# the second argument is the 'magic code' from the reassemble step in the DSSAT runner. this
# value is needed so as to be able to easily find the appropriate set of geographic coordinates
# that go with the DSSAT outputs. since the magic code is added on to the input name, we need to
# strip it back off.
#
# the optional third argument is an offset used in conjunction with the magic code to strip off
# the DSSAT-related labels on the results. the assumption is that the x-file/fileX template has
# 8 characters in its name, 1 character for the underscore, 3 for the co2 concentration, and 1
# more for that underscore. sometimes this isn't the case: the co2 concentration might be more
# than 999ppm, or the template name might not be 8 characters long. you can adjust for those
# possibilities using this optional magic_offset option. the default is 13.




# rename the command line arguments
            prefix=$1
  geog_correct_base=$2






# if you want to match something exactly, put a ^ in front (for start of line)
# and a $ in back (for end of line)
name_patterns_to_keep=\
"
^real_1$
^real_2$
^real_3$
^real_4$
^real_5$
^real_6$
^real_7$
^real_8$
^real_.$
^real_..$
^yield_mean$
^happy_yield_mean$
"
#shift_in_days

others=\
"
AFCONC
anthesis_of_best
best_yield
culnum
CWAM
deep
emergence_mean
g2cul
g3cul
gro
gro_
gro$
index_of_best
IRCM
IRCM$
maturity_mean
maturity_of_best
n_bad_thing
n_contributing
n_contributing_real
NICM
NUCM
p1cul
p2cul
p5cul
PCTINF
pdate
phint
popZ
PRCM$
PRCP
prec_
real_emergence_mean$
real_maturity_mean$
rowS
^CWAH$
^happy_
^happy_yield_mean$
^N
^real_..$
^real_.$
^real_maturity_mean$
sw_
^yield_mean$
^yield_std$
syn$
tmax_
TMAXA
tmin_
TMINA
"
###################################


# the sleeptime allows you to put a small pause in between each case that is read in.
# when reading a lot of info in, it can bog down the hard-drive and generate a lot
# of I/O wait. that makes it difficult to do anything else on the computer at the same
# time. introducing a small pause allows the hard-drive to catch up a little bit and
# will make it so that the computer doesn't totally freeze up. it will take a little
# bit longer to do the importing since you are intentionally waiting around. however,
# a compromise between speed and usability can usually be reached.

sleeptime=0.0 # this is in seconds; a decent place to start is 0.4


# we need to specify where the original input data were stored so
# we can find the right geographic information to reconstruct the maps
#
# we also need to know which directory the results are in
inputs_directory=to_DSSAT/
       yield_dir=chunks_to_GRASS/
#       yield_dir=from_DSSAT_daily/

echo "!!! pulling from directory: $yield_dir !!!"

# make a quasirandom code so we can parallelize
quasi_random_code="${RANDOM}_`date +%N`"






# we now start to build up the name of the geographic files that will
# allow us to reconstruct the results as maps
DSSAT_result_suffix=_STATS.txt # this is the suffix that the DSSAT runner puts at the end
clean_suffix=${DSSAT_result_suffix%%.txt} # we now strip off the .txt portion for later use

# check and see if the optional offset has been specified
#if [ -z "$magic_offset" ]; then
#  echo -e "\n\n Assuming a 3 digit co2 ppm; if otherwise, manually specify the magic offset ... \n\n"
#  magic_offset=13; # default = 9 places + 4 co2 chars (???_)
#fi

# remind ourselves that a small wait is being employed; we are also putting some
# white space above and below so that it doesn't get lost amongst everything
# else going to the screen. the sleep immediately following also helps in that regard.
echo -e "\n\n\n\n\n"
echo "using sleep time of [$sleeptime]"
echo -e "\n\n\n\n\n"
sleep 1

# get a list of the files that we are interested in importing
file_list=`ls ${yield_dir}${prefix}*${DSSAT_result_suffix}`
echo $file_list
# we will now go through the list of files and import them one at a time
for Y_file_full in $file_list
do


  # do the brief pause and do a status message
  echo ""
  echo "       ... sleeping $sleeptime ..."
  sleep $sleeptime
  echo "Yff = [$Y_file_full]"

  # extract out the base name by getting rid of the directories and the DSSAT runner suffix
  base_file_name=`basename $Y_file_full ${DSSAT_result_suffix}`

  # build up the name of the geography file
#  geog_correct_base=${base_file_name#$magic_code} # first strip off the magic code from the DSSAT runner
#  geog_correct_base=${geog_correct_base:$magic_offset} # now do the offset thing to get rid of the other pieces
  
  
  # DMR removing this and hardcoding it
  # geog_correct_base=$input_magic_code${base_file_name#*$input_magic_code} # first strip off the magic code from the DSSAT runner
  # another status check to help figure out when things go wrong
  echo "bfn = [$base_file_name] ; gcb = [$geog_correct_base]"


  # do the actual imporation
  # this is a special, non-standard grass program so you may need to compile it and figure out where the
  # resulting binaries are placed
#    /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.in.new \
#       data_input=${yield_dir}${base_file_name}${clean_suffix} \
#       geog_input=${inputs_directory}${geog_correct_base}_geog \
#     header_input=${inputs_directory}${geog_correct_base}_header \
#           \
#           output=$base_file_name
#
#  if [ "$?" = 1 ]; then
#
#    echo "++++ some major problem with importing, skipping the rest of this entry ++++"
#    continue
#
#  fi

  # the maps are just numbered starting at 0 for the first column. we need to rename them
  # to something human-readable and possibly give them some reasonable color schemes.
  # we reset the region to match up with the rasters so that one of the color definitions
  # will work as quickly as possible
#  g.region rast=${base_file_name}_0


# check how many columns showed up....
#g.mlist rast pat=${base_file_name}_* | wc





  ##### prepare the geography
      # grab the geog data (for later use); lat then longitude
      echo "cut -f3,4 ${inputs_directory}${geog_correct_base}_geog.txt > deleteme_latitude_longitude_${quasi_random_code}.txt"
      cut -f3,4 ${inputs_directory}${geog_correct_base}_geog.txt > deleteme_latitude_longitude_${quasi_random_code}.txt

      # resolution is in line 5
      # cellsize
      # cellsize=`grep "^cellsize " ${inputs_directory}${geog_correct_base}_header.txt | cut -d" " -f2`
      # extract the resolution from the header file


      # find the region extent from the geography
      # it really wants a lot of columns, so i will just look at the original....
      # range_string=`r.in.xyz input=${inputs_directory}${geog_correct_base}_geog.txt output=deleteme_just_looking_${quasi_random_code} x=4 y=3 z=1 fs=tab -s --q --o 2>>deleteme_junk_${quasi_random_code}.txt`
      #  low_latitude=`echo "$range_string" | grep "^y:" | tr -s " " | cut -d" " -f2`
      # high_latitude=`echo "$range_string" | grep "^y:" | tr -s " " | cut -d" " -f3`
      #  low_longitude=`echo "$range_string" | grep "^x:" | tr -s " " | cut -d" " -f2`
      # high_longitude=`echo "$range_string" | grep "^x:" | tr -s " " | cut -d" " -f3`

      # form up the region. we need to go half a pixel-width to each side
      # bc_scale=10
      # echo "high_latitude"
      # echo $high_latitude
      # echo "low_latitude"
      # echo $low_latitude
      # NNN=`echo "scale = $bc_scale ; $high_latitude + ($cellsize / 2)" | bc`
      # SSS=`echo "scale = $bc_scale ; $low_latitude  - ($cellsize / 2)" | bc`
      # EEE=`echo "scale = $bc_scale ; $high_longitude + ($cellsize / 2)" | bc`
      # WWW=`echo "scale = $bc_scale ; $low_longitude  - ($cellsize / 2)" | bc`

      # always set the region
      # echo $NNN
      # echo $SSS
      # echo $EEE
      # echo $WWW
      # echo $cellsize
      # g.region n=$NNN s=$SSS e=$EEE w=$WWW res=$cellsize

  # ok, now, let's read the cols file and match up indices with names...

  column_list=`cat ${yield_dir}${base_file_name}${clean_suffix}.cols.txt | tr "\t" "\n"`
  n_columns=`echo "$column_list" | wc -l`

  # here is the dangerous magic to try to bail out once we have all the ones we want
  n_patterns=`echo "$name_patterns_to_keep" | grep -v "^$" | wc -l`

  # initialize a counter
  n_patterns_found_so_far=0

 echo "n_patterns"
echo $n_patterns



  # rename all of them
  for (( column_index=0 ; column_index < n_columns ; column_index++ )); do

    if [ $n_patterns_found_so_far -eq $n_patterns ]; then
      # we have found as many as we were looking for, so break out
      echo "<found $n_patterns_found_so_far, so breaking (beware of wildcards)>"
      break
    fi
    # echo "n_patterns_found_so_far"
    # echo $n_patterns_found_so_far
    # echo "column_index"
    let "column_number = column_index + 1"

    this_column_name=`echo "$column_list" | sed -n "${column_number}p"`
    # echo "this_column_name"
    # echo $this_column_name
    # check if we want it
    keep_this=no
    for name_pattern in $name_patterns_to_keep; do
      keep_test=`echo "$this_column_name" | grep "$name_pattern"`
      # echo "keep_test"
      # echo $keep_test
      if [ -n "$keep_test" ]; then
        # we seem to have found something, so break out
        keep_this=yes
        break;
      fi
      
    done
    
#    echo "col#$column_number ; name = [$this_column_name] ; KT = [$keep_test]"

   clean_column_name=`echo "$this_column_name" | sed "s/#/n/g"`

    if [ $keep_this = yes ]; then
      echo -n "[$clean_column_name]"
      let "n_patterns_found_so_far++"
#      g.rename rast=${base_file_name}_${column_index},${base_file_name}_${this_column_name} --o
       

#       data_input=${yield_dir}${base_file_name}${clean_suffix} \
#       geog_input=${inputs_directory}${geog_correct_base}_geog \
#     header_input=${inputs_directory}${geog_correct_base}_header \


    # ok, my plan is to extract just the column we care about, along with the latitude and longitude
      # grab the data
      
      cut -f${column_number} ${yield_dir}${base_file_name}${clean_suffix}.txt > deleteme_single_column_of_data_${quasi_random_code}.txt
      paste deleteme_latitude_longitude_${quasi_random_code}.txt deleteme_single_column_of_data_${quasi_random_code}.txt > deleteme_full_thing_for_import_${quasi_random_code}.txt
      # exitl
      r.in.xyz input=deleteme_full_thing_for_import_${quasi_random_code}.txt output=${base_file_name}_${clean_column_name} x=2 y=1 z=3 fs=tab --o --q



      # contemplate renaming here....
      yield_test=`echo "$clean_column_name" | grep yield`
      gro_test=`echo "$clean_column_name" | grep _gro_`
      day_test=`echo "$clean_column_name" | grep shift_in_days`
      echo ""
      echo "yield_test"
      echo $yield_test

      echo ""
      # put the yield and growth stage stresses together since they both use ./zero_to_black.sh
      if [ -n "${yield_test}" ]; then
        echo ""
        ./zero_to_black.sh ${base_file_name}_${clean_column_name} 2>&1 | grep -v olor | grep -v set | grep -v "$yield_test"
      # elif [ -n "${gro_test}" ]; then
      #   echo ""
      #   ./zero_to_black.sh ${base_file_name}_${clean_column_name} 1>/dev/null 2>&1
      # elif [ -n "$day_test" ]; then
      #   echo ""
      #   ./month_day_colors.sh ${base_file_name}_${clean_column_name}
      fi
    else
      echo -n "."
     # g.remove rast=${base_file_name}_${clean_column_name} --q
    fi

  done # col_index; importing


done # file name loop


# rm deleteme_latitude_longitude_${quasi_random_code}.txt deleteme_just_looking_${quasi_random_code} deleteme_junk_${quasi_random_code}.txt deleteme_single_column_of_data_${quasi_random_code}.txt deleteme_full_thing_for_import_${quasi_random_code}.txt
rm deleteme_latitude_longitude_${quasi_random_code}.txt deleteme_single_column_of_data_${quasi_random_code}.txt deleteme_full_thing_for_import_${quasi_random_code}.txt


