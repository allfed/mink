#!/bin/bash



# this is an attempt to chunkify DSSAT runs automatically


if [ $# -lt 4 ]; then
  
  echo ""
  echo "Usage: $0 data_file_base_name n_chunks X_to_use [magic_code]"
  echo "NOT EXACTLY CORRECT... go read the source code..."
  echo "The idea is that this will reassemble the chunks of output into a single dataset for importation"
  echo "into GRASS."
  echo ""
  echo "data_file_base_name is the base name (no .txt) of the GIS table to be used."
  echo ""
  echo "n_chunks specifies how many chunks this case was cut up into"
  echo ""
  echo "X_to_use is the X-file that was/should-be used for this case"
  echo ""
  echo "magic_code is an optional string that will be put at the front of the final name so that different runs"
  echo "using the same GIS data and X_files can be told apart"
  echo ""
  
  exit 1
fi


### read in the arguments...

  data_file_base_name=$1
           co2_to_use=$2
             n_chunks=$3
           X_template=$4
           magic_code=$5
#           plantingDateInMonthShiftInDays=$6



  # this is likely have a full path on it, so we need to strip the path
  # in order to refer to it in its new location on the compute node
  data_file_short_name=`basename $data_file_base_name`

### source in the common stuff...

source default_paths_etc.sh

IFS="
"
n_threads_total=$n_chunks

### split up the text files into chunks

##################
### java setup ###
##################

# load up the stuff from land use modeling

java_to_use=$java_on_headnode

    classpath=${original_runner_dir}/java8_IFPRIconverter/bin/
    classname=org.DSSATRunner.ReassembleSplitTextMatrices

memory_string="-mx1400M"


# which directory to dump them into
chunk_file=${output_data_dir}

# the data files
input_file=${chunked_output_data_dir}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}
base_input=`basename $input_file`

echo -e "\n\ncodd=[$chunked_output_data_dir]; xt=[${X_template%%.*X}]\nCF = [$chunk_file] ; IF = [$input_file] ; NT = [$n_threads_total] \n\n\n"

$java_to_use "$memory_string" -cp $classpath $classname ${input_file} $chunk_file $n_threads_total

test_exit_code=$?
echo "exit code was [$test_exit_code]"

# if we were able to reassemble things, then we'll go ahead and move them
if [ $test_exit_code -eq 0 ]; then

  # copy over the provenance files
  zip -jq ${output_data_dir}${magic_code}${X_template%%.*X}_${co2_to_use}_${data_file_short_name%%_STATS}_provenance.zip ${chunked_output_data_dir}${X_template%%.*X}_${co2_to_use}_${data_file_short_name%%_STATS}*provenance.txt

  # copy over the cols file
  cp ${chunked_output_data_dir}${X_template%%.*X}_${co2_to_use}_${data_file_short_name%%_STATS}_0_STATS.cols.txt ${output_data_dir}${magic_code}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}.cols.txt

  # rename the output file...
  mv -v ${output_data_dir}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}.txt ${output_data_dir}${magic_code}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}.txt
  mv -v ${output_data_dir}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}.info.txt ${output_data_dir}${magic_code}${X_template%%.*X}_${co2_to_use}_${data_file_short_name}.info.txt

  echo ""
  echo ""
  echo "SUCCESS in REASSEMBLY: $base_input"
  echo ""
  echo ""
else
  echo ""
  echo ""
  echo "FAILURE TO REASSEMBLE: $base_input"
  echo ""
  echo ""
fi

