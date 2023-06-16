#!/bin/bash

# This script does the following:

# Check for the correct number of arguments passed into the script
# Set environment variables, including latitude and longitude resolution
# Create directories for specific purposes
# Assign script arguments to variables
# Remove existing directories to ensure a clean run
# Source in default settings from other files
# Set up the Java environment
# Define some Java classpath and classname
# Manipulate file paths and filenames
# Generate blocks for copying weather files
# Create a new script (which they run) that:
#     Creates directories
#     Copies files
#     Runs Java commands
#     Manages the output
#     Cleans up the workspace
# Print out information and progress to the console


# Set latitude and longitude resolution values. They must match the daily weather files.
export  latitude_resolution=1.875  
export longitude_resolution=1.25  

# Check if the correct number of arguments were provided
if [ $# -lt 8 ]; then
  echo ""
  echo "Usage: $0 data_file_base_name daily_to_use X_template crop_nitro_name co2_level crop_irri_name other_settings_source_file n_before_me [USE_OLD]"
  echo "Refer to the script's documentation for more details about the arguments."
  exit 1
fi

# Assign script arguments to variables
data_file_base_name=$1
daily_to_use=$2
X_template=`basename $3`
crop_nitro_name=$4
co2_level=$5
crop_irri_name=$6
plantingDateInMonthShiftInDays=$7
n_before_me=$8
use_old_string="$9"

# Strip the path from the data_file_base_name
data_file_short_name=`basename $data_file_base_name`
chunk_index=${data_file_short_name##*_}

# Remove the run directory to ensure a clean run each time
rm -rf $on_node_home

# Source the default settings
source default_paths_etc.sh
source some_settings_46.sh

# Override the defaults
cropToUse=$crop_nitro_name
co2ppm=$co2_level
gisTableBaseName=${on_node_input_data_dir}${data_file_short_name}
baseNameOfDailyWeather=${on_node_weather_dir}`basename $daily_to_use`
clean_yieldOutputBaseName=${X_template%%.*X}_${co2ppm}_${daily_to_use##*/}_${data_file_short_name}

# Setup Java 
java_to_use=$java_on_computenode
classpath=${on_node_runner_dir}java8_IFPRIconverter/bin/
headnode_classpath=${original_runner_dir}java8_IFPRIconverter/bin/
classname=org.DSSATRunner.MinkRunner3p2daily 
daily_weather_copier_classname=org.DSSATRunner.WriteCopyBlockForDailyWeather
memory_string="-mx1400M"

# Change the log name to include the chunk index
log_file=${log_file/___/_${chunk_index}_}
script_to_run_in_job=${staging_directory}script_to_run_${chunk_index}_r${quasi_random_code}.sh

# If USE_OLD is specified, override the default DSSAT directory
if [ -n "$use_old_string" ]; then
  original_DSSAT_dir=$wheat_original_DSSAT_dir
fi

# Generate the block that will copy all the weather files
copy_block=`$java_to_use -cp $headnode_classpath $daily_weather_copier_classname ${prestaged_weather_dir}$daily_to_use $on_node_weather_dir $data_file_base_name $weatherDataSuffixWithDot $latitude_resolution $longitude_resolution | uniq`
number_of_pixels=`echo "$copy_block" | wc -l`

we_need_to_delay=0

# If we_need_to_delay is true, calculate time to delay
if [ "$we_need_to_delay" = 1 ]; then
  time_to_delay=`echo "scale=1 ; $number_of_pixels / $guess_for_weather_files_per_second" | bc`s
else
  time_to_delay=0.1s
fi

cat << EOF > "$script_to_run_in_job"
  #!/bin/bash
  echo "Looks like the script is printing out"
  echo "running on \$HOSTNAME at \$(date)"

  echo "CASE \$X_template \${daily_to_use##*/} \$co2_level \${data_file_base_name##*/CZX*_*XZC_}"

  # write out the runner init file
  echo "\$allFlag"                      > \$runner_init_file
  echo "\$allExtraToRecordCSV"         >> \$runner_init_file
  echo "\$gisTableBaseName"            >> \$runner_init_file
  echo "\$templateXFile"               >> \$runner_init_file
  echo "\$yieldOutputBaseName"         >> \$runner_init_file
  echo "\$on_node_DSSAT_dir"           >> \$runner_init_file
  echo "\$nameOfDSSATExecutable"       >> \$runner_init_file
  echo "\$baseNameOfDailyWeather"      >> \$runner_init_file
  echo "\$SWmultiplier"                >> \$runner_init_file
  echo "\$weatherDataSuffixWithDot"           >> \$runner_init_file
  echo "\$fakePlantingYear"            >> \$runner_init_file
  echo "\$nFakeYears"                  >> \$runner_init_file
  echo "\$magicSoilPrefix"             >> \$runner_init_file
  echo "\$spinUpTimeDays"              >> \$runner_init_file
  echo "\$nPlantingWindowsPerMonth"    >> \$runner_init_file
  echo "\$plantingWindowLengthDays"    >> \$runner_init_file
  echo "\$co2ppm"                      >> \$runner_init_file
  echo "\$cropToUse"                   >> \$runner_init_file
  echo "\$happyYieldThresholdToDoRealRuns"               >> \$runner_init_file
  echo "\$phenologyBufferInDays"       >> \$runner_init_file
  echo "\$happyMaturityThresholdToDoRealRuns"            >> \$runner_init_file
  echo "\$crop_irri_name"              >> \$runner_init_file
  echo "\$fractionBetweenLowerLimitAndDrainedUpperLimit" >> \$runner_init_file
  echo "\$depthForNitrogen"            >> \$runner_init_file
  echo "\$residueNitrogenPercent"      >> \$runner_init_file
  echo "\$incorporationRate"           >> \$runner_init_file
  echo "\$incorporationDepth"          >> \$runner_init_file
  echo "\$clayLoamSandStableCarbonRatesFilename"         >> \$runner_init_file
  echo "\$optionalHarvestInterval"     >> \$runner_init_file
  echo "\$plantingDateInMonthShiftInDays"     >> \$runner_init_file
  echo "\$latitude_resolution"         >> \$runner_init_file
  echo "\$longitude_resolution"        >> \$runner_init_file
  echo "\$maxRunTime"                  >> \$runner_init_file
  echo "\$bumpUpMultiplier"            >> \$runner_init_file
  echo "\$testIntervalToUse"           >> \$runner_init_file
  echo "\$rerunAttemptsMax"            >> \$runner_init_file
  echo "\$keepHappyYields"             >> \$runner_init_file
  echo "\$keepRealDaysToEmergence"     >> \$runner_init_file
  echo "\$keepRealDaysToAnthesis"      >> \$runner_init_file
  echo "\$keepRealDaysToMaturity"      >> \$runner_init_file

  echo "------ moving/unpacking ; $(date) ------"

  mkdir -p \$on_node_runner_dir
  mkdir -p \$on_node_DSSAT_dir
  mkdir -p \$on_node_input_data_dir
  mkdir -p \$on_node_weather_dir
  mkdir -p \$X_dir
  mkdir -p \$on_node_output_dir

  cd \${original_runner_dir}java8_IFPRIconverter/src/
  javac org/DSSATRunner/Mink3p2daily.java
  mv org/DSSATRunner/Mink3p2daily.class ../bin/org/DSSATRunner/Mink3p2daily.class
  cd -

  cp -a \$original_runner_dir*   \${on_node_runner_dir}
  cp \$original_DSSAT_dir/*    \${on_node_DSSAT_dir}
  cp \${data_file_base_name}_*  \${on_node_input_data_dir}
  cp \${original_X_files_dir}\${X_template} \$X_dir
  cp \$runner_init_file         \${on_node_runner_init_file}

  \$copy_block

  if [ \$(ls -U \$on_node_weather_dir*\${weatherDataSuffixWithDot} | wc -l) -eq 0 ]; then
      echo " !!! something bad happened on \$HOSTNAME and we could not copy nicely, clearing out \$on_node_home"
      rm -rf \$on_node_home
      echo "- done BAILING at \$(date) -" >> \$log_file
      exit
  fi

  echo "------ running          ; \$(date) ------"
  cd \$on_node_DSSAT_dir

  echo "running on \$HOSTNAME" >> \$log_file
  echo "at \$(date), we are attempting..." >> \$log_file
  echo "[[[\${yieldOutputBaseName/_CZX*_*XZC_/_}]]]" >> \$log_file

  \$java_to_use "\$memory_string" -cp \$classpath \$classname \$on_node_runner_init_file 

  cp \${yieldOutputBaseName}_STATS.txt      \${chunked_output_data_dir}\${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.txt
  cp \${yieldOutputBaseName}_STATS.info.txt \${chunked_output_data_dir}\${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.info.txt
  cp \${yieldOutputBaseName}_STATS.cols.txt \${chunked_output_data_dir}\${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.cols.txt
  cp \${yieldOutputBaseName}_provenance.txt \${chunked_output_data_dir}\${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_provenance.txt

  echo "all is well, so clean up on-node-home" >> \$log_file
  rm -rf \$on_node_home

  echo "- done at \$(date) -" >> \$log_file

EOF

cd staging_area

latest_script=$(ls *.sh -rt | tail -n 1)

chmod +x "$latest_script" 

./"$latest_script"