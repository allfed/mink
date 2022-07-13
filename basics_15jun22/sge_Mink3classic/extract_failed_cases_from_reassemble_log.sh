

# the idea is to source this in so as to be able to grab only the failed cases...

echo "WARNING: finding failed cases, assuming snx-templates do NOT have underscores..."

# get the list of failed cases; then get rid of the first stuff
first_failed_list=`grep FAIL REASSEMBLE_log.TXT | cut -d" " -f4`

echo "n failed is....  `echo "$first_failed_list" | wc -l`"
# ok, now we have to slowly go through and pull out the pieces we want...

failed_cases=""
counter=0
for failed_line in $first_failed_list; do

   snx=`echo "$failed_line" | cut -d"_" -f1`
   co2=`echo "$failed_line" | cut -d"_" -f2`
  rest=`echo "$failed_line" | cut -d"_" -f4- | sed "s/_STATS//g"`

#echo -n "{$snx $co2 $rest}"

  failed_cases="$failed_cases
`echo "$start_readable_data_list" | grep "$rest	${snx}.SNX" | grep "	$co2	"`"

#  echo -n "[found `echo "$failed_cases" | grep -v "^$" | wc -l`] "

  let "counter++"
  echo -n " $counter"

done



