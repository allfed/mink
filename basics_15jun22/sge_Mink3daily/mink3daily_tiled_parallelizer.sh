#!/bin/bash

set -e

# this is an attempt to chunkify DSSAT runs automatically


if [ $# -lt 9 ]; then
  
  echo ""
  echo "Usage: $0 data_file_base_name daily_to_use X_template crop_nitro_name co2_level crop_irri_name chunks_per_case"
  echo ""
  echo "The idea is that this will package everything up, accomplish the run, and bring the results back."
  echo "All the details are defined internally, but will hopefully be sensible."
  echo ""
  echo "data_file_base_name is the base name (no .txt) of the GIS table to be used. At the moment, it should be in the from_GRASS/ directory."
  echo "X_template is the name of the template-X-file to build on (i.e., the crop/variety/management). Only the filename is needed."
  echo ""
  echo "crop_nitro_name is the crop name associated with a nitrogen fertilizer scheme"
  echo ""
  echo "co2_level is the ppm integer CO2 concentration to use when doing the modeling"
  echo ""
  echo "crop_irri_name is the crop name associated with an irrigation scheme"
  echo ""
  echo "chunks_per_case is how many chunks to break everything up into..."
  echo ""
  echo ""
  echo "you provided $0 $@"

  
  exit 1
fi

### read in the arguments...

   data_file_base_name=$1
 nameOfDSSATExecutable=$2
          dssat_folder=$3
          daily_to_use=$4
            X_template=`basename $5`
       crop_nitro_name=$6
             co2_level=$7
        crop_irri_name=$8
       chunks_per_case=$9
days_to_shift_planting=${10}
   latitude_resolution=${11}
  longitude_resolution=${12}

  # this is likely have a full path on it, so we need to strip the path
  # in order to refer to it in its new location on the compute node
  # (DMR) added in extra info here, and removed the same further down the chain, so there are no inadequately specified data files.
  data_file_short_name=`basename $data_file_base_name`


### source in the common stuff...

source default_paths_etc.sh

### examine the machine list
IFS="
"


### split up the text files into chunks

##################
### java setup ###
##################

# load up the stuff from land use modeling

java_to_use=$java_on_headnode

    classpath=${original_runner_dir}java8_IFPRIconverter/bin/
    classname=org.DSSATRunner.SplitTextMatrices

memory_string="-mx1400M"


# which directory to dump them into
chunk_file=${chunked_input_data_dir}

# the data files
input_file=${input_data_dir}${data_file_short_name}_data

# echo "$java_to_use \"$memory_string\" -cp $classpath $classname ${input_file} $chunk_file $chunks_per_case"
nice $java_to_use "$memory_string" -cp $classpath $classname ${input_file} $chunk_file $chunks_per_case

if [ $? -ne 0 ]; then
  echo ""
  echo ""
  echo "FAILURE of chunkification (data); skipping"
  echo ""
  echo ""
  exit
fi

# the geog files
input_file=${input_data_dir}${data_file_short_name}_geog

nice $java_to_use "$memory_string" -cp $classpath $classname ${input_file} $chunk_file $chunks_per_case

if [ $? -ne 0 ]; then
  echo ""
  echo ""
  echo "FAILURE of chunkification (geog); skipping"
  echo ""
  echo ""
  exit
fi


# ack! we need to make this thread-safe: if we are using the same input files
# they get the same chunk names and we can have read/write collisions especially
# in the first few cases...
#
# we need to move these after they are generated and then be sure to tell
# the follow on script where to find them...

QRC_here=$quasi_random_code


### run each chunk on a separate machine/thread
#  for (( this_machine_thread_index=0 ; this_machine_thread_index < $machine_threads ; this_machine_thread_index++ ))

scripts_list=""  # This variable will hold all the script paths
for (( chunk_index=0 ; chunk_index < $chunks_per_case ; chunk_index++ ))
do
  # construct a list of bash scripts to launch as a newline separated string
  quasi_random_code="on_node_home${RANDOM}_`date +%N`"
  script_to_run_in_job=${staging_directory}script_to_run_${chunk_index}_r${quasi_random_code}.sh


  # Append the script path to the scripts_list variable, combined with chunk_index
  scripts_list+="${chunk_index}:${script_to_run_in_job}"$'\n'
done

IFS=$'\n'  # Change the Internal Field Separator to newline for the loop
counter=0
ran_suspected_wheat=false
echo ""
echo "creating script_to_run_in_job scripts..."
echo ""
for entry in $scripts_list
do
  # current_chunk_index=$(echo $entry | cut -d':' -f1)
  # script_path=$(echo $entry | cut -d':' -f2-)
  current_chunk_index="${entry%%:*}" # Everything before the first ':'
  script_path="${entry#*:}" # Everything after the first ':'

  # echo "-- $current_chunk_index / $chunks_per_case `date` --"

  old_chunk_here=${chunk_file}${data_file_short_name}_${current_chunk_index}
  new_chunk_here=${chunk_file}CZX${QRC_here}XZC_${data_file_short_name}_${current_chunk_index} # (DMR) I removed the planting month day thing. I never use it, and the new functionality with planting months should make it obsolete

  mv ${old_chunk_here}_data.txt      ${new_chunk_here}_data.txt
  mv ${old_chunk_here}_data.info.txt ${new_chunk_here}_data.info.txt
  mv ${old_chunk_here}_geog.txt      ${new_chunk_here}_geog.txt
  mv ${old_chunk_here}_geog.info.txt ${new_chunk_here}_geog.info.txt

  # do a very simple wheat checking thing
  # wheat_test=$(echo "$crop_nitro_name" | grep wheat || true)
  # wheat_test=`echo "$crop_nitro_name" | grep wheat`

  if [[ "$crop_nitro_name" =~ "wheat" ]]; then

    ran_suspected_wheat=true
    ./mink3daily_run_DSSAT_tile.sh  $script_path $new_chunk_here $daily_to_use $nameOfDSSATExecutable $dssat_folder $X_template $crop_nitro_name $co2_level $crop_irri_name $days_to_shift_planting $counter $latitude_resolution $longitude_resolution USE_CIMMYT_BETA
  else
    # create all bash scripts that will be run in parallel
    ./mink3daily_run_DSSAT_tile.sh $script_path $new_chunk_here $daily_to_use $nameOfDSSATExecutable $dssat_folder $X_template $crop_nitro_name $co2_level $crop_irri_name $days_to_shift_planting $counter $latitude_resolution $longitude_resolution
  fi
  counter=$((counter + 1))
done
echo ""
echo "done creating script_to_run_in_job scripts"
echo ""

if [ "$ran_suspected_wheat" = true ]; then
    echo "   !!!! ran coordination style for suspected wheat at least once... !!!!"
fi

# run all the bash scripts we constructed in parallel


script_path="${entry#*:}" # Everything after the first ':'

# add xargs parallel launching line of code here to run them all in parallel at once

# I've used cut -d':' -f2- to extract only the script path from the scripts_list because xargs will execute each of those paths.

# The -P $chunks_per_case will attempt to run all chunks (one script per chunk) in parallel simultaneously. This can be lowered by changing chunks_per_case to the number of cpus in the system in question, in which case it will run chunks a few at a time, until it completes.
# If any script fails, xargs will stop, and the shell script will receive an error code.
echo ""
echo "running the script_to_run_in_job scripts (which run DSSAT in parallel, so you see progress for all of them asynchronously)"
echo ""
 
# NOTE: why do we make a temp file? because i wasn't sure how else to capture the exit status of xargs.
# If you care to remove this extra step while capturing stdout feel free

# 1. Print the scripts_list.
scripts_output=$(printf "%s\n" $scripts_list)

# 2. Cut based on ':' and fetch from the second field onward.
cut_output=$(echo "$scripts_output" | cut -d':' -f2-)

# 3. Execute the resulting scripts using xargs in parallel.
# Save the content of the variable to a temporary file
temp_file=$(mktemp)
echo "$cut_output" > "$temp_file"

# temporarily don't exit on non-zero return values
# counter-intuitively, to keep running even if one of the DSSAT fails, you would remove 
# the set +e and set -e lines below.
set +e

# Use xargs to read arguments from the temporary file and execute the command in parallel threaded chunks
xargs -a "$temp_file" -I {} -P $chunks_per_case bash {}
# Check the status of the xargs command.
if [ $? -ne 0 ]; then
    source some_settings_46.sh
    echo "An error occurred in the DSSAT tile run. Exiting."
    echo "To run the DSSAT that failed, navigate to one of the on_node_home directories"
    echo "and execute"
    echo "./run_dssat.sh $nameOfDSSATExecutable"
    exit 1
fi
# back to exiting on non-zero return values
set -e

# Clean up by removing the temporary file
rm "$temp_file"

echo ""
echo "done running the script_to_run_in_job scripts"
echo ""

echo ""
echo "completed all chunks"
echo ""