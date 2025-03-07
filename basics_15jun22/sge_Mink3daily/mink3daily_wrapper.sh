#!/bin/bash
set -e

IFS="
"


# $1 is whether to run or reassemble.
          data_to_use=$2
nameOfDSSATExecutable=$3
         dssat_folder=$4
         daily_to_use=$5
             X_to_use=$6
          crop_to_use=$7
           co2_to_use=$8
          irri_to_use=$9
      chunks_per_case=${10}
           magic_code=${11}
              lat_res=${12}
              lon_res=${13}
  fakePlantingYear=${14}
        nFakeYears=${15}



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
    # echo "./mink3daily_tiled_parallelizer.sh $data_to_use $daily_to_use $X_to_use $crop_to_use $co2_to_use $irri_to_use $chunks_per_case $plantingDateInMonthShiftInDays $lat_res $lon_res $fakePlantingYear $nFakeYears"
    ./mink3daily_tiled_parallelizer.sh $data_to_use $nameOfDSSATExecutable $dssat_folder $daily_to_use $X_to_use $crop_to_use $co2_to_use $irri_to_use $chunks_per_case $plantingDateInMonthShiftInDays $lat_res $lon_res $fakePlantingYear $nFakeYears #1>>$screen_dump 2>&1
  else

    # DMR I heavily modified naming here for simplicity...

    # ./mink3daily_reassemble_outputs.sh ${daily_to_use##*/}_d${plantingDateInMonthShiftInDays/-/n}_${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code # This was the old way...

    # useful to uncomment below to see what's being passed in
    # echo "./mink3daily_reassemble_outputs.sh (basename "{data_to_use}" .txt) co2_to_use chunks_per_case X_to_use magic_code"
    # echo "./mink3daily_reassemble_outputs.sh $(basename "${data_to_use}" .txt) $co2_to_use $chunks_per_case $X_to_use $magic_code"

    ./mink3daily_reassemble_outputs.sh $(basename "${data_to_use}" .txt) $co2_to_use $chunks_per_case $X_to_use $magic_code # the new naming convention is much simpler and a bit different in terms of required input

  fi

