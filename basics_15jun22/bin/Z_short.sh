max_to_see=10

#n_good=`tail -n 1 *.txt | grep prog | wc -l`
#n_pds=`tail -n 1 *.txt | grep pd | wc -l`
#let "n_good = n_good + n_pds"
active=`tail -n 1 log*.txt | grep -v "==" | grep -v "^$" | grep -v -e "--E--" | grep "prog:" | wc -l`
n_good=`tail -n 1 log*.txt | grep -v "==" | grep -v "^$" | grep -v -e "--E--" | wc -l`
let "half_of_good = n_good/2 + 1"

if [ $half_of_good -gt $max_to_see ]; then
  half_of_good=$max_to_see
fi

echo "running = $n_good ; active = $active ; showing top/bottom $half_of_good `date`"

#key_to_sort=4 order_flag=""
#key_to_sort=14 order_flag="-r"
key_to_sort=16 order_flag="-r"

if [ $n_good -gt $max_to_see ]; then
  tail -n 1 *.txt | grep prog: | sed "s/\/ /\/_/g" | sort -n -k${key_to_sort} $order_flag | head -n $half_of_good
  tail -n 1 *.txt | grep prog: | sed "s/\/ /\/_/g" | sort -n -k${key_to_sort} $order_flag | tail -n $half_of_good
else
  tail -n 1 *.txt | grep prog: | sed "s/\/ /\/_/g" | sort -n -k${key_to_sort} $order_flag
fi



