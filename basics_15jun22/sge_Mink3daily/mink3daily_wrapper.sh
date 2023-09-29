#!/bin/bash
set -e

IFS="
"


# $1 is whether to run or reassemble.
    data_to_use=$2
   daily_to_use=$3
       X_to_use=$4
    crop_to_use=$5
     co2_to_use=$6
    irri_to_use=$7
chunks_per_case=$8
     lat_res=${10}
     lon_res=${11}

plantingDateInMonthShiftInDays=0
sleeptime=0.0s

#. extract_failed_cases_from_reassemble_log.sh

#echo --------------
#echo "$failed_cases"
#echo --------------

#readable_data_list=$failed_cases


. default_paths_etc.sh

magic_reassembly_log=REASSEMBLE_log.TXT 

# first, let's clean up any blank lines in the machine list

# now, source in the default settings....
source default_paths_etc.sh

  screen_dump=${logs_dir}SCREEN_DUMP.TXT

# if [ $1 = "run" ]; then
  # date > $screen_dump
  # echo "rdl = [$real_readable_data_list]" >> $screen_dump
  # echo "chunks per case = $chunks_per_case" >> $screen_dump
# else
  # echo "rdl = [`echo "$real_readable_data_list" | grep -v "^$" | cat -n`]"
  # echo "chunks per case = $chunks_per_case"
  # echo "output log in $magic_reassembly_log"
  # date > $magic_reassembly_log
# fi


# get rid of the blank lines so that it can be sequentially numbered...

# nonCLIMATE  daily (subdir/tag)  SNX fertilizer  CO2 irrigation

  if [ $1 = "run" ]; then
    # echo "./mink3daily_tiled_parallelizer.sh $data_to_use $daily_to_use $X_to_use $crop_to_use $co2_to_use $irri_to_use $chunks_per_case $plantingDateInMonthShiftInDays $lat_res $lon_res"
    ./mink3daily_tiled_parallelizer.sh $data_to_use $daily_to_use $X_to_use $crop_to_use $co2_to_use $irri_to_use $chunks_per_case $plantingDateInMonthShiftInDays $lat_res $lon_res #1>>$screen_dump 2>&1
  else

    # DMR I heavily modified naming here for simplicity...

    # ./mink3daily_reassemble_outputs.sh ${daily_to_use##*/}_d${plantingDateInMonthShiftInDays/-/n}_${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code # This was the old way...
    ./mink3daily_reassemble_outputs.sh $(basename "${data_to_use}" .txt) $co2_to_use $chunks_per_case $X_to_use $magic_code # the new naming convention is much simpler and a bit different in terms of required input

  fi

