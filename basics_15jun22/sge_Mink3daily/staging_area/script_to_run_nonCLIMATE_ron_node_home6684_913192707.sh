#!/bin/bash
  echo "Looks like the script is printing out"
  echo "running on $HOSTNAME at `date`"

  echo "CASE mzJ029w00.SNX Outdoor-crops-control 379 /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/from_GRASS/D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"

  # write out the runner init file
  echo "true"                      > /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo ""         >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "/home/dmrivers/Code/mink/on_node_home6684_913192707/from_GRASS/D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "/home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/mzJ029w00.SNX"               >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "/home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE"         >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "./dscsm_compiled_fast.exe"       >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "/home/dmrivers/Code/mink/on_node_home6684_913192707/dailyweather/Outdoor-crops-control"      >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo ".0864000000"                >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo ".WTH"           >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "2"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "13"                  >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "HN_GEN00"             >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "90"              >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "1"    >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "3"    >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "379"                      >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "threeSplitWithFlowering"                   >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
#  echo "13" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "1"               >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "-5"       >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "367"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt

  echo "maize"              >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt

  echo "0.25" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
#  echo ""   >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "40"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "0.6"      >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "100"           >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "5"          >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "/home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/StableCarbonTable.txt"         >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "366"     >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "0"     >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt

  echo "1.875"         >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "1.25"        >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt

  echo "5500"                  >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "2.0"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "100"           >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "2"            >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt

  echo "false"             >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "true"     >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "true"      >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  echo "true"      >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  
  
echo "reached 3"
###################
### do the work ###
###################


#############################
### package everything up ###
#############################



echo "------ moving/unpacking ; `date` ------"

  # create the appropriate directories
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/basics_15jun22/small_java_programs/
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/from_GRASS/
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/dailyweather/
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/
         mkdir -p /home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/

  # move the stuff out
  # the runner needs all the subdirectories
  cp -a /home/dmrivers/Code/mink/basics_15jun22/small_java_programs/*   /home/dmrivers/Code/mink/on_node_home6684_913192707/basics_15jun22/small_java_programs/
  # the rest do not want the subdirectories
  cp /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/actual_program_4.7.5.11/*    /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/
  cp /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/from_GRASS/D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_*  /home/dmrivers/Code/mink/on_node_home6684_913192707/from_GRASS/
  cp /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/SNX_files/mzJ029w00.SNX /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/
  cp /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt         /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/runner_init_on_node_home6684_913192707.txt

  # copy the daily weather
echo ""
echo "about to copy"

echo ""
echo "finished copying"
echo ""

# check if things copied ok.
#if [ $? -ne 0 ]; then
# i think what i really care about is if any things got copied at all..
if [ `ls -U /home/dmrivers/Code/mink/on_node_home6684_913192707/dailyweather/*.WTH | wc -l` -eq 0 ]; then
    echo " !!! something bad happened on $HOSTNAME and we could not copy nicely, clearing out /home/dmrivers/Code/mink/on_node_home6684_913192707/"
    rm -rf /home/dmrivers/Code/mink/on_node_home6684_913192707/
    echo "- done BAILING at `date` -" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
    exit
fi

echo "------ running          ; `date` ------"


  # run the program

  cd /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/


  echo "now in `pwd`" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt

  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "running on $HOSTNAME" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "shell = $SHELL" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "at `date`, we are attempting..." >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "[[[/home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE]]]" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "--B--" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
#   echo command is /usr/bin/java "-mx1400M" -cp /home/dmrivers/Code/mink/on_node_home6684_913192707/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin/ org.DSSATRunner.MinkRunner3p2daily /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/runner_init_on_node_home6684_913192707.txt >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
#   echo using full interpretive version of the runner even with coordination style DSSAT for wheat.... >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
   /usr/bin/java "-mx1400M" -cp /home/dmrivers/Code/mink/on_node_home6684_913192707/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin/ org.DSSATRunner.MinkRunner3p2daily /home/dmrivers/Code/mink/on_node_home6684_913192707/DSSAT/X_files/runner_init_on_node_home6684_913192707.txt 2>&1 >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
   test_exit_code=$?
  if [ $test_exit_code -eq 0 ]; then
    # copy the results back
   
    cp /home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.txt      /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor-crops-control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.txt
    cp /home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.info.txt /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor-crops-control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.info.txt
    cp /home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.cols.txt /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor-crops-control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_STATS.cols.txt
    cp /home/dmrivers/Code/mink/on_node_home6684_913192707/to_GRASS/mzJ029w00_379_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_provenance.txt /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/chunks_to_GRASS/mzJ029w00_379_Outdoor-crops-control_D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_provenance.txt

    # clean up the compute node mess
    echo "all is well, so clean up on-node-home" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
    rm -rf /home/dmrivers/Code/mink/on_node_home6684_913192707/

    # and also get rid of the input chunks, since they will be many
    # with my new attempt at thread safety

    rm /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/from_GRASS/D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE_*

    rm /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/script_to_run_nonCLIMATE_ron_node_home6684_913192707.sh
    rm /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/staging_area/runner_init_on_node_home6684_913192707.txt
  else
    # give a warning...
    echo "something bad happened... leaving everything in place on the node..." >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
#    echo "AND TRYING TO SLEEP FOR A WHILE... at `date`" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
#    sleep 600
#    echo "done sleeping and exiting at `date`" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  fi
  echo "- done at `date` -" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt
  echo "--E--" >> /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/logs/log_nonCLIMATE_on_node_home6684_913192707.txt


        