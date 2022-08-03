#!/bin/bash
  echo "running on $HOSTNAME at `date`"

  echo "CASE mzJ029w00.SNX Outdoor_crops_control 379 D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"

  # write out the runner init file
  echo "true"                      > /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo ""         >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "/home/users/inesj/mink/from_GRASS/D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "/home/users/inesj/mink/DSSAT/X_files/mzJ029w00.SNX"               >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "/home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"         >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "./dscsm_compiled_fast.exe"       >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "/home/users/inesj/mink/dailyweather/Outdoor_crops_control"      >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo ".0864000000"                >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo ".WTH"           >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "2"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "13"                  >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "HN_GEN00"             >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "90"              >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "1"    >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "3"    >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "379"                      >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "threeSplitWithFlowering"                   >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
#  echo "13" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "1"               >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "-5"       >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "367"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt

  echo "maize"              >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt

  echo "0.25" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
#  echo ""   >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "40"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "0.6"      >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "100"           >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "5"          >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "/home/users/inesj/mink/basics_15jun22/sge_Mink3daily//StableCarbonTable.txt"         >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "366"     >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "some_settings_46.sh"     >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt

  echo ""         >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo ""        >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt

  echo "5500"                  >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "2.0"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "100"           >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "2"            >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt

  echo "false"             >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "true"     >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "true"      >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  echo "true"      >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  
  
echo reached 3
###################
### do the work ###
###################


#############################
### package everything up ###
#############################



echo "------ moving/unpacking ; `date` ------"

  # create the appropriate directories
         mkdir -p /home/users/inesj/mink/basics_15jun22/small_java_programs/
         mkdir -p /home/users/inesj/mink/DSSAT/
         mkdir -p /home/users/inesj/mink/from_GRASS/
         mkdir -p /home/users/inesj/mink/dailyweather/
         mkdir -p /home/users/inesj/mink/DSSAT/X_files/
         mkdir -p /home/users/inesj/mink/to_GRASS/

  # move the stuff out
  # the runner needs all the subdirectories
  cp -a ~rdrobert/small_java_programs//*   /home/users/inesj/mink/basics_15jun22/small_java_programs/
  # the rest do not want the subdirectories
  cp /home/users/inesj/mink/basics_15jun22/sge_Mink3daily//actual_program_4.7.5.11/*    /home/users/inesj/mink/DSSAT/
  cp D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_*  /home/users/inesj/mink/from_GRASS/
  cp /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/SNX_files//mzJ029w00.SNX /home/users/inesj/mink/DSSAT/X_files/
  cp /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt         /home/users/inesj/mink/DSSAT/X_files/runner_init_10384_278123114.txt

  # copy the daily weather


# check if things copied ok.
#if [ $? -ne 0 ]; then
# i think what i really care about is if any things got copied at all..
if [ `ls -U /home/users/inesj/mink/dailyweather//*.WTH | wc -l` -eq 0 ]; then
    echo " !!! something bad happened on $HOSTNAME and we could not copy nicely, clearing out /home/users/inesj/mink/"
    rm -rf /home/users/inesj/mink/
    echo "- done BAILING at `date` -" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
    exit
fi

echo "------ running          ; `date` ------"


  # run the program

  cd /home/users/inesj/mink/DSSAT/


  echo "now in `pwd`" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt

  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "running on $HOSTNAME" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "shell = $SHELL" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "at `date`, we are attempting..." >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "[[[/home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE]]]" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "--B--" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
#   echo command is /usr/bin/java "-mx1400M" -cp /home/users/inesj/mink/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin/ org.DSSATRunner.MinkRunner3p2daily /home/users/inesj/mink/DSSAT/X_files/runner_init_10384_278123114.txt >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
#   echo using full interpretive version of the runner even with coordination style DSSAT for wheat.... >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
   /usr/bin/java "-mx1400M" -cp /home/users/inesj/mink/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin/ org.DSSATRunner.MinkRunner3p2daily /home/users/inesj/mink/DSSAT/X_files/runner_init_10384_278123114.txt 2>&1 >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
   test_exit_code=$?
  if [ $test_exit_code -eq 0 ]; then
    # copy the results back
   
    cp /home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.txt      /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor_crops_control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.txt
    cp /home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.info.txt /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor_crops_control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.info.txt
    cp /home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.cols.txt /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor_crops_control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.cols.txt
    cp /home/users/inesj/mink/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_provenance.txt /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor_crops_control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_provenance.txt

    # clean up the compute node mess
    echo "all is well, so clean up on-node-home" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
    rm -rf /home/users/inesj/mink/

    # and also get rid of the input chunks, since they will be many
    # with my new attempt at thread safety

    rm D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_*

    rm /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area//script_to_run_nonCLIMATE_r10384_278123114.sh
    rm /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_10384_278123114.txt
  else
    # give a warning...
    echo "something bad happened... leaving everything in place on the node..." >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
#    echo "AND TRYING TO SLEEP FOR A WHILE... at `date`" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
#    sleep 600
#    echo "done sleeping and exiting at `date`" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  fi
  echo "- done at `date` -" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt
  echo "--E--" >> /home/users/inesj/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_10384_278123114.txt


        
