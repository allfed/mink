#!/bin/bash

n_files_to_consider=80
          n_to_show=20

if [ -n "$1" ]; then
  n_to_show=$1
fi

if [ -n "$2" ]; then
  n_files_to_consider=$2
fi

top_few_files=`ls -t log*.txt | head -n $n_files_to_consider`

output_list=""
for file_name in $top_few_files; do

  computer_name=`head -n 1 $file_name | cut -d" " -f3`
   minutes_left=`tail -n 1 $file_name | grep "^prog:" | cut -f4`
  not_done_but_no_status=`tail -n 1 $file_name | grep -v "^prog:" | grep -v -e "^--E--"`

  output_list="$output_list
$computer_name	${minutes_left}${not_done_but_no_status}"

done

echo "$output_list" | grep -v "	$" | sort -n -k2 | grep -v "^$" | head -n $n_to_show






if [ 0 = 1 ]; then
max_to_see=10

active_list=`tail -n 1 *.txt | grep -v "==" | grep -v "^$" | grep -v -e "--E--" | grep "prog:" | cut -d: -f1`

echo "$active_list"







n_good=`tail -n 1 *.txt | grep -v "==" | grep -v "^$" | grep -v -e "--E--" | wc -l`
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


fi # cutout
