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
  echo "allYearsFlag will attempt to read yearly yield (both real and happy)"
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
  input_magic_code=$2









# if you want to match something exactly, put a ^ in front (for start of line)
# and a $ in back (for end of line)
name_patterns_to_keep=\
"
^yield_mean$
"
#shift_in_days


rest="
real_maturity_mean$
real_emergence_mean$

real_.$
real_..$
emergence_.$
emergence_..$
maturity_.$
maturity_..$
"

rest="

^yield_mean$
"

some="

shift_in_days
real_emergence_mean$
^yield_mean$
shift_in_days
IRCM
"

rest="

real_.$
real_..$
real_maturity_mean$
real_emergence_mean$
"

real="
real_maturity_mean$
CWAM
n_contributing_real

^yield_mean$
AFCONC

PCTINF

real_.$
real_..$
"




others="
TMAXA
TMINA
PRCP

sw_
tmin_
tmax_
prec_
"

#param_search="
param_name_patterns_to_keep="
best_yield
anthesis_of_best
maturity_of_best
index_of_best

pdate
g2cul
g3cul
p1cul
p5cul
p2cul

"

others="
culnum

popZ
rowS
deep
g2cul
g3cul
p1cul
p5cul
p2cul
g2cul
g3cul

phint
"

aflatoxin_name_patterns_to_keep=\
"
^yield_mean$
PCTINF
AFCONC
"

others="
^real_.$
^real_..$
IRCM$
PRCM$

real_maturity_mean$
PCTINF
AFCONC
n_contributing_real

^N
maturity_mean$


CWAM
maturity_mean$
n_contributing
^happy_yield_mean$
^real_.$
^real_..$
^happy_yield_mean$
maturity_mean$
^CWAH$
^yield_mean$
real_emergence_mean$
real_maturity_mean$
^real_.$
^real_..$
^yield_mean$
n_bad_thing
syn$
gro$
"

others="


^yield_std$
NICM
NUCM

^real_.$
^real_..$
real_emergence_mean$
real_maturity_mean$
gro_
IRCM

emergence_mean
maturity_mean
gro
^real_maturity_mean$
IRCM
IRCM
real_emergence_mean$
real_maturity_mean$
^yield_std$
^happy_
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
       yield_dir=from_DSSAT/
#       yield_dir=from_DSSAT_daily/

echo "!!! pulling from directory: $yield_dir !!!"







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

# we will now go through the list of files and import them one at a time
for Y_file_full in $file_list
do


  # do the brief pause and do a status message
  echo "       ... sleeping $sleeptime ..."
  sleep $sleeptime
  echo "Yff = [$Y_file_full]"

  # extract out the base name by getting rid of the directories and the DSSAT runner suffix
  base_file_name=`basename $Y_file_full ${DSSAT_result_suffix}`

  # build up the name of the geography file
#  geog_correct_base=${base_file_name#$magic_code} # first strip off the magic code from the DSSAT runner
#  geog_correct_base=${geog_correct_base:$magic_offset} # now do the offset thing to get rid of the other pieces
  geog_correct_base=$input_magic_code${base_file_name#*$input_magic_code} # first strip off the magic code from the DSSAT runner

  # another status check to help figure out when things go wrong
  echo "bfn = [$base_file_name] ; gcb = [$geog_correct_base]"


  # do the actual imporation
  # this is a special, non-standard grass program so you may need to compile it and figure out where the
  # resulting binaries are placed
    /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.in.new \
       data_input=${yield_dir}${base_file_name}${clean_suffix} \
       geog_input=${inputs_directory}${geog_correct_base}_geog \
     header_input=${inputs_directory}${geog_correct_base}_header \
           \
           output=$base_file_name

  if [ "$?" = 1 ]; then

    echo "++++ some major problem with importing, skipping the rest of this entry ++++"
    continue

  fi

  # the maps are just numbered starting at 0 for the first column. we need to rename them
  # to something human-readable and possibly give them some reasonable color schemes.
  # we reset the region to match up with the rasters so that one of the color definitions
  # will work as quickly as possible
  g.region rast=${base_file_name}_0


# check how many columns showed up....
g.mlist rast pat=${base_file_name}_* | wc




  # ok, now, let's read the cols file and match up indices with names...

  column_list=`cat ${yield_dir}${base_file_name}${clean_suffix}.cols.txt | tr "\t" "\n"`
  n_columns=`echo "$column_list" | wc -l`

  # rename all of them
  for (( column_index=0 ; column_index < n_columns ; column_index++ )); do

    let "column_number = column_index + 1"

    this_column_name=`echo "$column_list" | sed -n "${column_number}p"`

    # check if we want it
    keep_this=no
    for name_pattern in $name_patterns_to_keep; do
      keep_test=`echo "$this_column_name" | grep "$name_pattern"`

      if [ -n "$keep_test" ]; then
        # we seem to have found something, so break out
        keep_this=yes
        break;
      fi
      
    done
    
#    echo "col#$column_number ; name = [$this_column_name] ; KT = [$keep_test]"

   clean_column_name=`echo "$this_column_name" | sed "s/#/n/g"`

if [ 1 = 1 ]; then
    if [ $keep_this = yes ]; then
#      g.rename rast=${base_file_name}_${column_index},${base_file_name}_${this_column_name} --o
      g.rename rast=${base_file_name}_${column_index},${base_file_name}_${clean_column_name} --o
    else
      echo -n "."
      g.remove rast=${base_file_name}_${column_index} --q
    fi
fi
  done # rename all of them


  # do some obvious color changes
  for rrr in `g.mlist rast pat=${base_file_name}_yield_mean`; do
    zero_to_black.sh $rrr
  done

  for rrr in `g.mlist rast pat=${base_file_name}_gro_*watersyn`; do
    zero_to_black.sh $rrr
  done

  for rrr in `g.mlist rast pat=${base_file_name}_gro_*nitrosyn`; do
    zero_to_black.sh $rrr
  done

  for rrr in `g.mlist rast pat=${base_file_name}_gro_*watergro`; do
    zero_to_black.sh $rrr
  done

  for rrr in `g.mlist rast pat=${base_file_name}_gro_*nitrogro`; do
    zero_to_black.sh $rrr
  done

  for rrr in `g.mlist rast pat=${base_file_name}_shift_in_days`; do
    month_day_colors.sh $rrr
  done





done # file name loop




