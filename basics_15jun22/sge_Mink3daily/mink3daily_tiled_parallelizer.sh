#!/bin/bash



# this is an attempt to chunkify DSSAT runs automatically


if [ $# -ne 8 ]; then
  
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
         daily_to_use=$2
           X_template=`basename $3`
      crop_nitro_name=$4
            co2_level=$5
       crop_irri_name=$6
      chunks_per_case=$7
      days_to_shift_planting=$8


  # this is likely have a full path on it, so we need to strip the path
  # in order to refer to it in its new location on the compute node
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

#echo "$java_to_use \"$memory_string\" -cp $classpath $classname ${input_file} $chunk_file $chunks_per_case"
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

counter=0

  for (( chunk_index=0 ; chunk_index < $chunks_per_case ; chunk_index++ ))
  do

    echo "-- $chunk_index / $chunks_per_case `date` --"



    old_chunk_here=${chunk_file}${data_file_short_name}_${chunk_index}
    new_chunk_here=${chunk_file}CZX${QRC_here}XZC_d${days_to_shift_planting/-/n}_${data_file_short_name}_${chunk_index}

    mv ${old_chunk_here}_data.txt      ${new_chunk_here}_data.txt
    mv ${old_chunk_here}_data.info.txt ${new_chunk_here}_data.info.txt
    mv ${old_chunk_here}_geog.txt      ${new_chunk_here}_geog.txt
    mv ${old_chunk_here}_geog.info.txt ${new_chunk_here}_geog.info.txt


    # do a very simple wheat checking thing
    wheat_test=`echo "$crop_nitro_name" | grep wheat`
    if [ -z "$wheat_test" ]; then

      mink3daily_run_DSSAT_tile.sh $new_chunk_here $daily_to_use $X_template $crop_nitro_name $co2_level $crop_irri_name $days_to_shift_planting $counter

    else
      echo "   !!!! running coordination style for suspected wheat... !!!!"
      mink3daily_run_DSSAT_tile.sh $new_chunk_here $daily_to_use $X_template $crop_nitro_name $co2_level $crop_irri_name $days_to_shift_planting $counter USE_CIMMYT_BETA
    fi

    let "counter++"

  done



