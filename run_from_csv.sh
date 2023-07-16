#!/bin/bash
# this runs a java script that does everything!
# the data which defines the scenario(s) is currently located at basic_15jun22/sge_Min3daily/scenarios/scenarios.csv.
# this scenarios csv can be modified to run arbitrary number of simulations.

set -e # exit if a command fails

if [ $# -eq 0 ]; then
  echo "Usage: $0 [scenario_config_file_location] [DSSAT,process,both]"
  exit
fi

export GRASS_VERBOSE=0


time_start=$SECONDS
. basics_15jun22/sge_Mink3daily/default_paths_etc.sh
. basics_15jun22/sge_Mink3daily/some_settings_46.sh
cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/src

javac org/Scenarios/Config.java
javac org/Scenarios/GenerateScenarios.java
javac org/Scenarios/Scenarios.java
javac org/Scenarios/CalculateProduction.java
javac org/DSSATRunner/WriteCopyBlockForDailyWeather.java

scenarios_csv_location="/mnt/data/basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.csv"
config_file_location="/mnt/data/$1"
script_folder=/mnt/data/basics_15jun22/sge_Mink3daily/
run_parameters_csv_folder="/mnt/data/basics_15jun22/sge_Mink3daily/parameters/"
whether_run_DSSAT_or_process=$2

java -ea org.Scenarios.Scenarios $scenarios_csv_location $script_folder $run_parameters_csv_folder $config_file_location $whether_run_DSSAT_or_process
time_end=$SECONDS
echo "duration (seconds)"
echo $(echo "scale=2 ; $time_end - $time_start " | bc -l)
