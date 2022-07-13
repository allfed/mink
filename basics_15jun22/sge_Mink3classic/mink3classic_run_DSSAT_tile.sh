#!/bin/bash


# the idea is to make it deployable on a compute node

#if [ $# -ne 6 ]; then
if [ $# -lt 6 ]; then

  echo ""
  echo "Usage: $0 data_file_base_name X_template crop_nitro_name co2_level crop_irri_name other_settings_source_file [USE_OLD]"
  echo ""
  echo "The idea is that this will package everything up, accomplish the run, and bring the results back."
  echo "All the details are defined internally, but will hopefully be sensible."
  echo ""
  echo "data_file_base_name is the base name (no .txt) of the GIS table to be used. At the moment, it should be in the from_GRASS/ directory."
  echo "X_template is the name of the template-X-file to build on (i.e., the crop/variety/management). Only the filename is needed."
  echo ""
 echo ""
  echo "crop_nitro_name is the crop name associated with a nitrogen fertilizer scheme"
  echo ""
  echo "co2_level is the ppm integer CO2 concentration to use when doing the modeling"
  echo ""
  echo "crop_irri_name is the crop name associated with an irrigation scheme"
  echo ""
  echo "you provided $0 $@"
  
  exit 1

fi

  data_file_base_name=$1
           X_template=`basename $2`
      crop_nitro_name=$3
            co2_level=$4
       crop_irri_name=$5
         days_to_shift_planting=$6
       use_old_string="$7"

  # this is likely have a full path on it, so we need to strip the path
  # in order to refer to it in its new location on the compute node
  data_file_short_name=`basename $data_file_base_name`

  chunk_index=${data_file_short_name##*_}

### source in the common elements...




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
       templateXFile=$magic_X_file
 yieldOutputBaseName=${on_node_output_dir}${X_template%%.*X}_${co2ppm}_${data_file_short_name} # this is new...
 clean_yieldOutputBaseName=${X_template%%.*X}_${co2ppm}_${data_file_short_name} # this is new...

##################
### java setup ###
##################


java_to_use=$java_on_computenode

    classpath=${on_node_runner_dir}java8_IFPRIconverter/bin/
####    classname=org.DSSATRunner.ThorntonMultiYearIrrigation45
####    classname=org.DSSATRunner.ThorntonMultiYearBigInits # this is the correct one
####    classname=org.DSSATRunner.ThorntonMultiYearBigInits_debughappy # this is the debug one

#    classname=org.DSSATRunner.MinkRunner0
#    classname=org.DSSATRunner.MinkRunner0_droughtcompare # this does a nifty histogram thing with irrigation
#    classname=org.DSSATRunner.MinkRunner1 # just run it, but oryza-in-dssat sometimes mysteriously hangs
#    classname=org.DSSATRunner.MinkRunner1a # has a timeout on the system call
#    classname=org.DSSATRunner.MinkRunner2classic # cleaned up version of 1a
#    classname=org.DSSATRunner.MinkRunner3classic # cleaned up version of 1a

#    classname=org.DSSATRunner.MinkRunner3p1classic # testing dynamic detection of output spacing for Summary.OUT
#    classname=org.DSSATRunner.MinkRunner3p2classic # adding HWAM as a fallback for yields...
    classname=org.DSSATRunner.MinkRunner3p3classic # deleting aflatoxin file if it exists

memory_string="-mx1400M"

# change the log name to include the chunk index

log_file=${log_file/___/_${chunk_index}_}

script_to_run_in_job=${staging_directory}/script_to_run_${chunk_index}_r${quasi_random_code}.sh


   if [ -n "$use_old_string" ]; then
     echo -e "\n !!! !!! !!! !!!\n"
     echo -e "------------- using a magical thingee to copy old beta executable into place.... --------"

     # we need to point to the particular wheat directory instead of the usual one...
     original_DSSAT_dir=$wheat_original_DSSAT_dir

     echo -e " !!! !!! !!! !!!\n"
   fi


# write out the job script....
echo "#!/bin/bash
  echo \"running on \$HOSTNAME at \`date\`\"
  echo \"CASE	$X_template $co2_level ${data_file_base_name/*XZC_/}	$days_to_shift_planting\"

  # write out the runner init file
  echo \"$gisTableBaseName\"             > $runner_init_file
  echo \"$templateXFile\"               >> $runner_init_file
  echo \"$yieldOutputBaseName\"         >> $runner_init_file
  echo \"$nameOfDSSATExecutable\"       >> $runner_init_file
  echo \"$nameOfWeatherExecutable\"     >> $runner_init_file
  echo \"$SWmultiplier\"                >> $runner_init_file
  echo \"$firstRandomSeed\"             >> $runner_init_file
  echo \"$nFakeYears\"                  >> $runner_init_file
  echo \"$magicSoilPrefix\"             >> $runner_init_file
  echo \"$spinUpTimeDays\"              >> $runner_init_file
  echo \"$nPlantingWindowsPerMonth\"    >> $runner_init_file
  echo \"$plantingWindowLengthDays\"    >> $runner_init_file
  echo \"$co2ppm\"                      >> $runner_init_file
  echo \"$cropToUse\"                   >> $runner_init_file
  echo \"$nHappyPlantRunsForPhenology\" >> $runner_init_file
  echo \"$happyYieldThresholdToDoRealRuns\"               >> $runner_init_file
  echo \"$phenologyBufferInDays\"       >> $runner_init_file
  echo \"$happyMaturityThresholdToDoRealRuns\"            >> $runner_init_file

  echo \"$crop_irri_name\"              >> $runner_init_file

  echo \"$fractionBetweenLowerLimitAndDrainedUpperLimit\" >> $runner_init_file
#  echo \"$nitrogenPPMforBothNH4NO2\"   >> $runner_init_file
  echo \"$depthForNitrogen\"            >> $runner_init_file
  echo \"$residueNitrogenPercent\"      >> $runner_init_file
  echo \"$incorporationRate\"           >> $runner_init_file
  echo \"$incorporationDepth\"          >> $runner_init_file
  echo \"$clayLoamSandStableCarbonRatesFilename\"         >> $runner_init_file
  echo \"$optionalHarvestInterval\"     >> $runner_init_file

#  echo \"$plantingDateInMonthShiftInDays\"     >> $runner_init_file
  echo \"$days_to_shift_planting\"      >> $runner_init_file

  echo \"$maxRunTime\"                  >> $runner_init_file
  echo \"$bumpUpMultiplier\"            >> $runner_init_file
  echo \"$testIntervalToUse\"           >> $runner_init_file
  echo \"$rerunAttemptsMax\"            >> $runner_init_file

  
  

###################
### do the work ###
###################


#############################
### package everything up ###
#############################



echo \"------ moving/unpacking ; \`date\` ------\"

  # create the appropriate directories
         mkdir -p $on_node_runner_dir
         mkdir -p $on_node_DSSAT_dir
         mkdir -p $on_node_input_data_dir
         mkdir -p $X_dir
         mkdir -p $on_node_output_dir

  # move the stuff out
  # the runner needs all the subdirectories
  cp -a $original_runner_dir/*   ${on_node_runner_dir}
  # the rest do not want the subdirectories
  cp $original_DSSAT_dir/*    ${on_node_DSSAT_dir}
  cp ${data_file_base_name}_*  ${on_node_input_data_dir}
  cp ${original_X_files_dir}/${X_template} ${X_dir}
  cp $runner_init_file         ${on_node_runner_init_file}


echo \"------ running ; \`date\` ------\"


  # run the program

  cd $on_node_DSSAT_dir


  echo \"now in \`pwd\`\" >> $log_file

  echo \"\" >> $log_file
  echo \"\" >> $log_file
  echo \"\" >> $log_file
  echo \"running on \$HOSTNAME\" >> $log_file
  echo \"shell = \$SHELL\" >> $log_file
  echo \"at \`date\`, we are attempting...\" >> $log_file
  echo \"[[[${yieldOutputBaseName/_CZX*_*XZC_/_}]]]\" >> $log_file
  echo \"\" >> $log_file
  echo \"\" >> $log_file
  echo \"\" >> $log_file
  echo \"\" >> $log_file
  echo \"--B--\" >> $log_file
#   echo command is $java_to_use \"$memory_string\" -cp $classpath $classname $on_node_runner_init_file >> $log_file
#   echo using full interpretive version of the runner even with coordination style DSSAT for wheat.... >> $log_file
   $java_to_use \"$memory_string\" -cp $classpath $classname $on_node_runner_init_file 2>&1 >> $log_file
   test_exit_code=\$?
  if [ \$test_exit_code -eq 0 ]; then
    # copy the results back
   
    cp ${yieldOutputBaseName}_STATS.txt      ${chunked_output_data_dir}${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.txt
    cp ${yieldOutputBaseName}_STATS.info.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.info.txt
    cp ${yieldOutputBaseName}_STATS.cols.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_STATS.cols.txt
    cp ${yieldOutputBaseName}_provenance.txt ${chunked_output_data_dir}${clean_yieldOutputBaseName/_CZX*_*XZC_/_}_provenance.txt

    # clean up the compute node mess
    echo \"all is well, so clean up on-node-home\" >> $log_file
    rm -rf $on_node_home

    # and also get rid of the input chunks, since they will be many
    # with my new attempt at thread safety

    rm ${data_file_base_name}_*

    rm $script_to_run_in_job
    rm $runner_init_file
  else
    # give a warning...
    echo \"something bad happened... leaving everything in place on the node...\" >> $log_file
#    echo \"AND TRYING TO SLEEP FOR A WHILE... at \`date\`\" >> $log_file
#    sleep 600
#    echo \"done sleeping and exiting at \`date\`\" >> $log_file
  fi
  echo \"- done at \`date\` -\" >> $log_file
  echo \"--E--\" >> $log_file


        " > $script_to_run_in_job # end of ssh command....


# do the submission to the grid system...

#Q_command="qsub -l walltime=$max_run_time_string,nodes=1:ppn=1 -S /bin/bash -N R${chunk_index}_r${quasi_random_code} -o ${log_file} -joe -wd $staging_directory $script_to_run_in_job"
#Q_command="qsub -l walltime=$max_run_time_string,nodes=1:ppn=1 -S /bin/bash -N R${chunk_index}_r${quasi_random_code} -o ${log_file/log_/Q_} -joe $script_to_run_in_job"

 Q_command="qsub -S /bin/bash -N R${chunk_index}_r${quasi_random_code} -o ${log_file} -j y -wd $staging_directory -q $queue_name $script_to_run_in_job"

echo "SUBMIT: $Q_command"

eval $Q_command




