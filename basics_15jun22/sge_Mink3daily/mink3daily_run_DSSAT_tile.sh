#!/bin/bash

# echo ""
# echo "running mink3daily_run_DSSAT_tile.sh"
# echo ""

set -e

# the idea is to make it deployable on a compute node

#if [ $# -ne 6 ]; then
if [ $# -lt 8 ]; then

  echo ""
  echo "Usage: $0 data_file_base_name daily_to_use X_template crop_nitro_name co2_level crop_irri_name other_settings_source_file n_before_me [USE_OLD]"
  echo ""
  echo "The idea is that this will package everything up, accomplish the run, and bring the results back."
  echo "All the details are defined internally, but will hopefully be sensible."
  echo ""
  echo "data_file_base_name is the base name (no .txt) of the GIS table to be used. At the moment, it should be in the to_DSSAT/ directory. #DMR: changed from_GRASS to to_DSSAT"
  echo "X_template is the name of the template-X-file to build on (i.e., the crop/variety/management). Only the filename is needed."
  echo ""
 echo ""
  echo "crop_nitro_name is the crop name associated with a nitrogen fertilizer scheme"
  echo ""
  echo "co2_level is the ppm integer CO2 concentration to use when doing the modeling"
  echo ""
  echo "crop_irri_name is the crop name associated with an irrigation scheme"
  echo ""
  echo "received: $0 $@"
  echo ""
  
  exit 1

fi

  script_to_run_in_job=$1
  data_file_base_name=$2
         daily_to_use=$3
nameOfDSSATExecutable=$4
         dssat_folder=$5
           X_template=`basename $6`
      crop_nitro_name=$7
            co2_level=$8
       crop_irri_name=${9}
    plantingDateInMonthShiftInDays=${10}
          n_before_me=${11}
  latitude_resolution=${12}
 longitude_resolution=${13}

  # this is likely have a full path on it, so we need to strip the path
  # in order to refer to it in its new location on the compute node
  data_file_short_name=`basename $data_file_base_name`
  chunk_index=${data_file_short_name##*_}

### source in the common elements...


# ADDED this so that we have a clean run each time and can repeat this script (DMR)
# what it does is take a look at the previous on_node_home directory it created last time to run DSSAT and delete it
# this leaves behind the latest one created.
rm -rf $on_node_home

########################
### control settings ###
########################

  # source in the defaults...
  source default_paths_etc.sh
  source some_settings_46.sh

  # over-ride some obvious ones...
      cropToUse=$crop_nitro_name
         co2ppm=$co2_level

    gisTableBaseName=${on_node_input_data_dir}${data_file_short_name} # this should get pulled from the argument
 baseNameOfDailyWeather=${on_node_weather_dir}`basename $daily_to_use`
# baseNameOfDailyWeather=${on_node_weather_dir}

       templateXFile=$magic_X_file
 yieldOutputBaseName=${on_node_output_dir}${data_file_short_name} # this is new...
# clean_yieldOutputBaseName=${X_template%%.*X}_${co2ppm}_${data_file_short_name} # this is new...
 clean_yieldOutputBaseName=${data_file_short_name} # this is new...

##################
### java setup ###
##################


java_to_use=$java_on_computenode

    classpath=${on_node_runner_dir}java8_IFPRIconverter/bin/
    headnode_classpath=${original_runner_dir}java8_IFPRIconverter/bin/
####    classname=org.DSSATRunner.ThorntonMultiYearIrrigation45
####    classname=org.DSSATRunner.ThorntonMultiYearBigInits # this is the correct one
####    classname=org.DSSATRunner.ThorntonMultiYearBigInits_debughappy # this is the debug one

#    classname=org.DSSATRunner.MinkRunner0
#    classname=org.DSSATRunner.MinkRunner0_droughtcompare # this does a nifty histogram thing with irrigation
#    classname=org.DSSATRunner.MinkRunner1 # just run it, but oryza-in-dssat sometimes mysteriously hangs
#    classname=org.DSSATRunner.MinkRunner1a # has a timeout on the system call
#    classname=org.DSSATRunner.MinkRunner2classic # cleaned up version of 1a
#    classname=org.DSSATRunner.MinkRunner2daily # cleaned up version of 1a
#    classname=org.DSSATRunner.MinkRunner3daily # cleaned up version of 1a

#    classname=org.DSSATRunner.MinkRunner3p1daily # cleaned up version of 1a

    # trying to fix some problems....
    classname=org.DSSATRunner.MinkRunner3p2daily # cleaned up version of 1a

   daily_weather_copier_classname=org.DSSATRunner.WriteCopyBlockForDailyWeather


memory_string="-mx1400M"

# change the log name to include the chunk index

log_file=${log_file/___/_${chunk_index}_}

# script_to_run_in_job=${staging_directory}script_to_run_${chunk_index}_r${quasi_random_code}.sh


# build the block which will copy all those silly weather files. this will be ugly...
# echo $daily_weather_copier_classname

# echo java_to_use -cp headnode_classpath daily_weather_copier_classname {prestaged_weather_dir}daily_to_use on_node_weather_dir data_file_base_name weatherDataSuffixWithDot latitude_resolution longitude_resolution

# echo "$java_to_use $headnode_classpath $daily_weather_copier_classname ${prestaged_weather_dir}$daily_to_use $on_node_weather_dir $data_file_base_name $weatherDataSuffixWithDot $latitude_resolution $longitude_resolution"
copy_block=`$java_to_use -cp $headnode_classpath $daily_weather_copier_classname ${prestaged_weather_dir}$daily_to_use $on_node_weather_dir $data_file_base_name $weatherDataSuffixWithDot $latitude_resolution $longitude_resolution | uniq`
# echo "copy_block"
# echo $copy_block
# number_of_pixels=`echo "$copy_block" | wc -l`
# echo $number_of_pixels

#  we_need_to_delay=`echo "if($n_before_me > -2 && $n_before_me <= $number_of_initial_cases_to_stagger) {1} else {0}" | bc`
# we_need_to_delay=0

# In the regular expression '^cp', the ^ symbol means "start of the line", and cp is the exact character sequence you are looking for. Therefore, grep '^cp' matches any line that starts with "cp". The wc -l command then counts the number of these lines.
number_of_pixels=`echo "$copy_block" | grep '^cp' | wc -l`

    # echo "n_before_me = $n_before_me; maxstagger = $number_of_initial_cases_to_stagger ; n_pixels = $number_of_pixels / guess files per sec $guess_for_weather_files_per_second; need_to_delay = $we_need_to_delay"

  # echo "optionalHarvestInterval"
  # echo $optionalHarvestInterval
  # echo "clayLoamSandStableCarbonRatesFilename"
  # echo $clayLoamSandStableCarbonRatesFilename
  # echo "optionalHarvestInterval"
  # echo $optionalHarvestInterval
  # echo "plantingDateInMonthShiftInDays"
  # echo $plantingDateInMonthShiftInDays
  # echo "latitude_resolution"
  # echo $latitude_resolution
  # echo "longitude_resolution"
  # echo $longitude_resolution
# echo $script_to_run_in_job
# write out the job script....
echo "#!/bin/bash
  set -e # exit on error
  
  # write out the runner init file
  echo \"allFlag=$allFlag\"                      > $runner_init_file
  echo \"allExtraToRecordCSV=$allExtraToRecordCSV\"         >> $runner_init_file
  echo \"gisTableBaseName=$gisTableBaseName\"            >> $runner_init_file
  echo \"templateXFile=$templateXFile\"               >> $runner_init_file
  echo \"yieldOutputBaseName=$yieldOutputBaseName\"         >> $runner_init_file
  echo \"on_node_DSSAT_dir=$on_node_DSSAT_dir\"           >> $runner_init_file # ADDED THIS (DMR)
  echo \"nameOfDSSATExecutable=$nameOfDSSATExecutable\"       >> $runner_init_file
  echo \"baseNameOfDailyWeather=$baseNameOfDailyWeather\"      >> $runner_init_file
  echo \"SWmultiplier=$SWmultiplier\"                >> $runner_init_file
  echo \"weatherDataSuffixWithDot=$weatherDataSuffixWithDot\"           >> $runner_init_file
  echo \"fakePlantingYear=$fakePlantingYear\"            >> $runner_init_file
  echo \"nFakeYears=$nFakeYears\"                  >> $runner_init_file
  echo \"magicSoilPrefix=$magicSoilPrefix\"             >> $runner_init_file
  echo \"spinUpTimeDays=$spinUpTimeDays\"              >> $runner_init_file
  echo \"nPlantingWindowsPerMonth=$nPlantingWindowsPerMonth\"    >> $runner_init_file
  echo \"plantingWindowLengthDays=$plantingWindowLengthDays\"    >> $runner_init_file
  echo \"co2ppm=$co2ppm\"                      >> $runner_init_file
  echo \"cropToUse=$cropToUse\"                   >> $runner_init_file
  echo \"phenologyBufferInDays=$phenologyBufferInDays\"       >> $runner_init_file
  echo \"happyMaturityThresholdToDoRealRuns=$happyMaturityThresholdToDoRealRuns\"            >> $runner_init_file
  echo \"crop_irri_name=$crop_irri_name\"              >> $runner_init_file
  echo \"fractionBetweenLowerLimitAndDrainedUpperLimit=$fractionBetweenLowerLimitAndDrainedUpperLimit\" >> $runner_init_file
  echo \"depthForNitrogen=$depthForNitrogen\"            >> $runner_init_file
  echo \"residueNitrogenPercent=$residueNitrogenPercent\"      >> $runner_init_file
  echo \"incorporationRate=$incorporationRate\"           >> $runner_init_file
  echo \"incorporationDepth=$incorporationDepth\"          >> $runner_init_file
  echo \"clayLoamSandStableCarbonRatesFilename=$clayLoamSandStableCarbonRatesFilename\"         >> $runner_init_file
  echo \"optionalHarvestInterval=$optionalHarvestInterval\"     >> $runner_init_file
  echo \"plantingDateInMonthShiftInDays=$plantingDateInMonthShiftInDays\"     >> $runner_init_file
  echo \"latitude_resolution=$latitude_resolution\"         >> $runner_init_file
  echo \"longitude_resolution=$longitude_resolution\"        >> $runner_init_file
  echo \"maxRunTime=$maxRunTime\"                  >> $runner_init_file
  echo \"bumpUpMultiplier=$bumpUpMultiplier\"            >> $runner_init_file
  echo \"testIntervalToUse=$testIntervalToUse\"           >> $runner_init_file
  echo \"rerunAttemptsMax=$rerunAttemptsMax\"            >> $runner_init_file
  echo \"keepHappyYields=$keepHappyYields\"             >> $runner_init_file
  echo \"keepRealDaysToEmergence=$keepRealDaysToEmergence\"     >> $runner_init_file
  echo \"keepRealDaysToAnthesis=$keepRealDaysToAnthesis\"      >> $runner_init_file
  echo \"keepRealDaysToMaturity=$keepRealDaysToMaturity\"      >> $runner_init_file
    
  
###################
### do the work ###
###################


#############################
### package everything up ###
#############################


# echo \"------ moving/unpacking ; \`date\` ------\"
time_start=\$(date +%s%3N)


  # create the appropriate directories
         mkdir -p $on_node_runner_dir
         mkdir -p $on_node_DSSAT_dir
         mkdir -p $on_node_input_data_dir
         mkdir -p $on_node_weather_dir
         mkdir -p $X_dir
         mkdir -p $on_node_output_dir

 # Added this recompile condition each time (DMR)

  # move the stuff out
  # the runner needs all the subdirectories
  set +e  # Disable exit on error
  cp -a $original_runner_dir*   ${on_node_runner_dir} 2>/dev/null
  # the rest do not want the subdirectories
  cp ${BASE}$dssat_folder/*    ${on_node_DSSAT_dir} 2>/dev/null
  cp ${data_file_base_name}_*  ${on_node_input_data_dir} 2>/dev/null
  cp ${original_X_files_dir}${X_template} ${X_dir} 2>/dev/null
  cp $runner_init_file         ${on_node_runner_init_file} 2>/dev/null
  set -e  # Re-enable exit on error
  # copy the daily weather
$copy_block
# echo \"number_of_pixels\"
# echo \"$number_of_pixels\"

# i think what i really care about is if any things got copied at all..
# however, if there are actually no valid cells, we don't want to error out
if [ $number_of_pixels -ne 0 ] && [ \`ls -U $on_node_weather_dir*${weatherDataSuffixWithDot} | wc -l\` -eq 0 ]; then
    echo \" !!! something bad happened on \$HOSTNAME and we could not copy nicely, clearing out $on_node_home\"
    rm -rf $on_node_home
    echo \"- done BAILING at \`date\` -\" >> $log_file
    exit
fi


  # run the program
  cd $on_node_DSSAT_dir


  # echo \"now in \`pwd\`\" >> $log_file

  # echo \"\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"running on \$HOSTNAME\" >> $log_file
  # echo \"shell = \$SHELL\" >> $log_file
  # echo \"at \`date\`, we are attempting...\" >> $log_file
  # echo \"[[[${yieldOutputBaseName/CZX*_*XZC_/}]]]\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"\" >> $log_file
  # echo \"--B--\" >> $log_file
  # time_before_run=\$(date +%s%3N)
   $java_to_use \"$memory_string\" -cp $classpath $classname $on_node_runner_init_file 
  # time_after_run=\$(date +%s%3N)


# this used to be part of the above run (DMR)
#    test_exit_code=\$?
#   if [ \$test_exit_code -eq 0 ]; then
#     # copy the results back

  cp ${yieldOutputBaseName}_STATS.cols.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/CZX*_*XZC_/}_STATS.cols.txt
   if [ $number_of_pixels -ne 0 ]; then

    cp ${yieldOutputBaseName}_STATS.txt      ${chunked_output_data_dir}${clean_yieldOutputBaseName/CZX*_*XZC_/}_STATS.txt
    cp ${yieldOutputBaseName}_STATS.info.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/CZX*_*XZC_/}_STATS.info.txt
    cp ${yieldOutputBaseName}_provenance.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/CZX*_*XZC_/}_provenance.txt
    fi

    # clean up the compute node mess
    # echo \"all is well, so clean up on-node-home\" >> $log_file
    rm -rf $on_node_home

    rm ${data_file_base_name}_*

    rm $script_to_run_in_job
    rm $runner_init_file
#   else
#     # give a warning...
#     echo \"something bad happened... leaving everything in place on the node...\" >> $log_file
# #    echo \"AND TRYING TO SLEEP FOR A WHILE... at \`date\`\" >> $log_file
# #    sleep 600
# #    echo \"done sleeping and exiting at \`date\`\" >> $log_file
#   fi
  # echo \"- done at \`date\` -\" >> $log_file
  # echo \"--E--\" >> $log_file

  # time_end=\$(date +%s%3N)

  # time_elapsed=\$(echo \"scale=4; (\$time_end - \$time_start)/1000\" | bc -l)
  # # time_non_run=\$(echo \"scale=4; (\$time_end - \$time_start - \$time_after_run + \$time_before_run)/1000\" | bc -l)
  # # time_run=\$(echo \"scale=4; (\$time_after_run - \$time_before_run)/1000\" | bc -l)

  # echo \"time_elapsed for script_to_run_in_job  = \$time_elapsed seconds\"
  # echo \"time_non_run  = \$time_non_run\"
  # echo \"time_run  = \$time_run\" 

        " > $script_to_run_in_job # end of ssh command....
chmod +x "${script_to_run_in_job}"

# echo ""
# echo "done running mink3daily_run_DSSAT_tile.sh"
# echo ""