#!/bin/bash


# the idea is to search over all the months in the year to see which month provides the best yield.


# argument $1 is the line_list, with the following pattern:
# "raster_month_1 raster_month_2
#  raster_month_2 raster_month_3
#  raster_month_3 raster_month_4
#  raster_month_4 raster_month_5
#  raster_month_5 raster_month_6
#  raster_month_6 raster_month_7
#  raster_month_7 raster_month_8
#  raster_month_8 raster_month_9
#  raster_month_9 raster_month_10
#  raster_month_10 raster_month_11
#  raster_month_11 raster_month_12"


# argument $2 is the raster name to save the result


# this assigns raster

# or, whichever months should be compared.


# this is the list of cases to consider
# split it with a tab and remove the variable suffix...
# e.g., ALLUSA__mzUSA_RF_369_allusasearch__M    _maize__rainfed
#####
# !!! changing to leave the suffix on !!!


line_list=$1


month_list=$2

best_yield=$3

best_month=$4


#################################################
# should not have to change anything below here #
#################################################

# pick one of the cases to deal with
for line in $line_list; do
  echo "line"
  echo $line
#   # pull out what is meant by the different pieces
  before_month=`echo "$line" | cut -d "," -f1`
   after_month=`echo "$line" | cut -d "," -f2`

  # initialize the goods
  best_month=best_month_${before_month}${after_month}
  best_yield=best_yield_${before_month}${after_month}

  # r.mapcalc $best_month = "13" 2>&1 | grep -v "%"
  # r.mapcalc $best_yield = "-1" 2>&1 | grep -v "%"

  echo "best_month"
  echo $best_month
  echo "best_yield"
  echo $best_yield
  this_months_yield=$best_yield
  # run through the possibilities for the months
  for month in ${month_list//,/ } # replaces commas in month_list with space
  do
    echo "-- $before_month $after_month ; MONTH = $month --"
  
    # update the base yield if we meet the criteria
    r.mapcalc $best_yield = " \
                  if($this_months_yield > $best_yield, $this_months_yield, $best_yield) \
                            " --quiet #2>&1 | grep -v "%"
  
    # update the best month
    r.mapcalc $best_month = "if($best_yield == ${this_months_yield}, $month, $best_month)" --quiet #@2>&1 | grep -v "%"

  done # month

# set some colors and labels
# zero_to_black.sh $best_yield 2>&1 | grep -v "%"
# month_colors.sh $best_month

done # suffix

# # just assign the resulting raster name to the best month using grass gis
# r.mapcalc $2 = $best_yield
# r.mapcalc $3 = $best_month





