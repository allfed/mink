

# the idea is to source this in so as to be able to grab only the failed cases...

echo "WARNING: finding failed cases, assuming snx-templates do NOT have underscores..."

# get the list of failed cases; then get rid of the first stuff
first_failed_list=`grep FAIL REASSEMBLE_log.TXT | cut -d" " -f4`

echo "n failed is....  `echo "$first_failed_list" | wc -l`"
# ok, now we have to slowly go through and pull out the pieces we want...

echo "$readable_data_list" | grep -v "^$" | head

echo "\\\\\\\\\\\\\\"
echo "$first_failed_list"
echo "\\\\\\\\\\\\\\"


failed_cases=""
counter=0
for failed_line in $first_failed_list; do

# FAILURE TO REASSEMBLE: whK016IR_369_baseline_d0_wsthree__gremlin_winter_wheat_deltaONpikNOV_base_2000_p0_winterwheat__irrigated_nonCLIMAT_STATS

  backend_gone=${failed_line%%_nonCLIMATE_STATS}
   snx=`echo "$backend_gone" | cut -d"_" -f1`
   co2=`echo "$backend_gone" | cut -d"_" -f2`

   front_stripped=${backend_gone#${snx}_${co2}_}
   daily=${front_stripped%%_d${plantingDateInMonthShiftInDays}_*}

  rest=${front_stripped##*_d${plantingDateInMonthShiftInDays}_}

echo "{$snx $co2 $daily $rest}"


  failed_cases="$failed_cases
`echo "$readable_data_list" | grep "$rest" | grep "$daily" | grep "	${snx}.SNX" | grep "	$co2	"`"

#  echo -n "[found `echo "$failed_cases" | grep -v "^$" | wc -l`] "

  let "counter++"
  echo -n " $counter"

done



echo "[[[$failed_cases]]]"
