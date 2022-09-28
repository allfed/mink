#!/bin/bash


# the idea is to search over all the offset planting months to see which month provides the best yield.



# this is the list of cases that were run for the zero offset (p0).
# you can get this by some variation on g.mlist rast pat=RRR__*p0*yield_mean
# the p0 will be cut out and replaced by the offsets/shifts in the shifter_list
# the resul


line_list="
`g.mlist rast pat=RUSSIAa*_p0_springbarley*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_winterbarley*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_canola*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_wintercanola*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_sugarbeets*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_barley*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_soybeans*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_sunflowers*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_potatoes*yield_mean`

`g.mlist rast pat=RUSSIAa*_p0_maize*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_springwheat*yield_mean`
`g.mlist rast pat=RUSSIAa*_p0_winterwheat*yield_mean`
"

#line_list=`echo "$line_list" | grep "[IR]_[45]"`
#`g.mlist rast pat=KTEST__**_p0_*yield_mean`
other_line_list="
`g.mlist rast pat=FERT*sbK*_p0_*yield_mean | grep -v GEO`
"


#`g.mlist rast pat=KTEST*_p0_maize*yield_mean`
#`g.mlist rast pat=KTEST*_p0_soybeans*yield_mean`
line_list=`g.mlist rast pat=KTEST*ssp370*_p0_*yield_mean`




# this is the list of shifters to be considered
# typically we have done 3-month windows of -1,+1, and 0
# i usually list zero last so that if all the months have
# the same yield (usually only if the yield is exactly zero)
# then they get the target month shifter assigned to them
shifter_list=\
"
-1
1
0
"

# this is the prefix that prefaces the shift amount
# i have been using "p"; this is originally defined
# in the dataset building script.
magic_shifter_prefix=_p # adding an underscore to see if that makes it more robust

#################################################
# should not have to change anything below here #
#################################################

# reset the inter-field-separator
IFS="
"

# run through all the cases
for line in $line_list
do

  # pull out the pieces we need based on the notion that we have the zeroth months in the list
  before_month=${line%%${magic_shifter_prefix}0*}
   after_month=${line##*${magic_shifter_prefix}0}

  
  # set the region to match what we'll be dealing with 
  g.region rast=${before_month}${magic_shifter_prefix}0${after_month}

  # initialize the goods
  best_month=best_month_${before_month}${after_month}
  best_yield=best_yield_${before_month}${after_month}

  r.mapcalc $best_month = "0" 2>&1 | grep -v "%" ; echo -n "("
  r.mapcalc $best_yield = "0" 2>&1 | grep -v "%" ; echo -n ")"

for shifter in $shifter_list
do
  # to avoid minus signs in raster names, we are using "n" to represent "negative"
  text_shifter=${shifter/-/n}

  # build up the name of the yield map for the case we're currently considering
  this_months_yield=${before_month}${magic_shifter_prefix}${text_shifter}${after_month}

  # actually do the comparison to see if this month's value is higher or not
  echo "-- grabbing = $this_months_yield --"

#  echo "             *** before $shifter ***"
#  r.univar $best_yield
  r.mapcalc $best_yield = " if ($this_months_yield > $best_yield, $this_months_yield, $best_yield) " 2>&1 | grep -v "%" ; echo -n "-"
#  echo "             *** after $shifter ***"
#  r.univar $best_yield

  r.mapcalc $best_month = "if($best_yield == ${this_months_yield}, $shifter, $best_month)" 2>&1 | grep -v "%" ; echo -n "="

done # month

echo ""

# set the colors
    zero_to_black.sh $best_yield
difference_colors.sh $best_month


done # suffix






