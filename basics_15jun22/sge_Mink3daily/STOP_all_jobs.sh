#!/bin/bash

log_file=deleteme_STOP_log.txt

rm $log_file

#qstat_list=`qstat | tr -s " " | cut -d" " -f2 | tail -n +3 | tac`
#qstat_list=`qstat | tr -s " " | cut -d" " -f1 | tail -n +3 | tac`
qstat_list=`qstat | tr -s " " | sed "s/^ //g" | cut -d" " -f1 | tail -n +3 | tac`


for jj in $qstat_list; do

  qdel $jj >> $log_file

done


exit





# old style
qstat_contents=`qstat | grep $USER`

if [ -z "$qstat_contents" ]; then
  echo "no jobs in qstat for $USER"
  exit
fi

first=`qstat | grep $USER | head -n 1 | tr -s " " | cut -d" " -f2`
last=`qstat | grep $USER | tail -n 1 | tr -s " " | cut -d" " -f2`

#for (( jobno=$first ; jobno <= $last ; jobno++ )); do
for (( jobno=$last  ; jobno >= $first ; jobno-- )); do

  qdel $jobno

done
