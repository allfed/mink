#/bin/bash


# this is to create a single "combination" rice map which puts values for indica
# rice in places where indica predominates and uses japonica values where we
# think japonica is most important



# since this combination will need to be done for many cases, we list out the
# cases in the two lists below. one list is for the japonica data and the other
# is for the indica data. each map in the japonica list will be matched up with the
# same line number from the indica list. so, you want to keep everything in the same
# order.
#
# it is fine to put in blank lines to help you organize things. the blank lines will
# not count as part of the line-by-line matching. for example you might have two blocks
# of cases: rainfed and irrigated. you could accidently put 3 blank lines between the blocks
# in the japonica list and only 1 blank line in the indica list and everything would
# still work properly.
#
# a simple way to generate these lists is to use commands like
# g.mlist rast pat=best_yield*japi*
# g.mlist rast pat=best_yield*indi*
#
# here, the assumption is that the japonica x-file templates had "japi" in them
# while the indica templates had "indi"


japonica_list=\
"
`g.mlist rast pat=best_yield*riK002**yield_mean`
"

indica_list=\
"
`g.mlist rast pat=best_yield*riK001**yield_mean`
"








# i was once doing a rice search over all the varieties and these are the two that we use normally,
# so i wanted to compare the fancy versus just the two we usually play with
# japonica
#`g.mlist rast pat=best_yield*H15TEST*ri_990003_*yield_mean`
# indica
#`g.mlist rast pat=best_yield*H15TEST*ri_IB0015_*yield_mean`




# this is the raster that has 1's for pixels that are most important for japonica
# and 0 if indica is most important
#rice_assignment_rast=JAPONICA_predominates@ricky_DSSAT_improved
#rice_assignment_rast=JAPONICA_predominates@DSSAT_essentials_12may11
rice_assignment_rast=JAPONICA_predominates_full_coverage@DSSAT_essentials_12may11


# as usual, as of 28apr22, i am getting bitten by not-quite-full-coverage
# so, i am filling in everything thing that is not yet filled with indica....
# doing this in DSSAT_essentials_12may11
#
# g.region rast=JAPONICA_predominates@DSSAT_essentials_12may11
# g.region n=85 s=-90 w=-180 e=180 res=0.0833333333333333

# r.mapcalc JAPONICA_predominates_full_coverage = "if(isnull(JAPONICA_predominates@DSSAT_essentials_12may11),0,JAPONICA_predominates@DSSAT_essentials_12may11)"




# the x-file name in the map name needs to get changed in order to build
# up the name for this combo rice. so, we need to strip out the
# old x-file names. this is accomplished by brute force...
#
# my convention in naming the x-file/fileX templates has been to use the last
# two characters to denote the type of water source:
# RF = rainfed
# IR = irrigated (automatic keeping a soil layer moist)
# II = flooded rice style
# the appropriate values need to be put below
magic_irrigated_pair=IR # the final two characters for irrigated X-file names
#magic_irrigated_pair=II # the final two characters for irrigated X-file names
  magic_rainfed_pair=RF # the final two characters for rainfed   X-file names

# this is a placeholder for use in striping out the old x-file name and
# replacing it with our combination code. basically it needs to be something
# that will never show up in the map name for any other reason
random_replacer=,,,abcgasfrtu,,,

# this is the name that will go in the place of the x-file name once we have
# put the indicat and japonica together. it would be wise to keep this the
# same length as we usually use for the x-file names: 8 characters
#combo_X_replacement=COMBrice
#combo_X_replacement=GEOcmb
combo_X_replacement=GEObslrice







#####################################################
# you should not have to change anything below here #
#####################################################


# clean up the lists by dropping all the blank lines
clean_japonica_list=`echo "$japonica_list" | grep -v "^$"`
  clean_indica_list=`echo "$indica_list"   | grep -v "^$"`


# do some quick idiot proofing to make sure we have the same number
# of cases in each list...
#
# first, we count them up
n_japonica=`echo "$clean_japonica_list" | wc -l`
  n_indica=`echo "$clean_indica_list"   | wc -l`

# a little status check to the screen
  echo "check nJ = [$n_japonica] ; nI = [$n_indica]"

# ask if they have the same number or not. if not, let's bail out
if [ $n_japonica -ne $n_indica ]; then
  echo "unequally lengthed lists... nJ = $n_japonica ; nI = $n_indica"
  exit
fi


# now we will do the real work. we have to go through each line
# and pull them out and match them up
for (( line_number=1 ; line_number <= n_indica ; line_number++ ))
do

  # grab the appropriate line from each list
  japonica_map=`echo "$clean_japonica_list" | sed -n "${line_number}p"`
    indica_map=`echo "$clean_indica_list"   | sed -n "${line_number}p"`

  # search and replace to get rid of the X-file names
  combo_map=${indica_map/??????$magic_irrigated_pair/$random_replacer$magic_irrigated_pair}
  combo_map=${combo_map/??????$magic_rainfed_pair/$random_replacer$magic_rainfed_pair}
  combo_map=${combo_map/$random_replacer/$combo_X_replacement}

  # status check so we know what's going on
  echo "-- $combo_map --"

  # Beware the MAGIC ASSUMPTION!!! the indica map has the appropriate
  # extent and resolution. we are hoping this is true and are too lazy
  # to check if the maps really match up properly or not
  g.region rast=$indica_map

  # and the real guts of this: look at the reference map and if it is japonica
  # use japonica at that location, otherwise use indica.
  r.mapcalc $combo_map = "if($rice_assignment_rast == 1, $japonica_map, $indica_map)" 2>&1 | grep -v "%"

done # line_number






