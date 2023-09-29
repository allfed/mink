#!/bin/bash
# this runs a java script that does everything!
# the data which defines the scenario(s) is currently located at basic_15jun22/sge_Min3daily/scenarios/scenarios.csv.
# this scenarios csv can be modified to run arbitrary number of simulations.

set -e # exit if a command fails

COMPILE_FLAG=0 # Default to not compile

# Handle the --compile or -c flag using getopts
while getopts ":c" option; do
  case $option in
    c) COMPILE_FLAG=1 ;;
    \?) echo "Usage: $0 [-c] [scenario_config_file_location] [DSSAT,process,both]"
        exit 1 ;;
  esac
done

# Remove the options from the positional parameters
shift $((OPTIND-1))

# Check if the positional arguments are present
if [ $# -lt 2 ]; then
  echo "Usage: $0 [-c] [scenario_config_file_location] [DSSAT,process,both]"
  exit
fi

export GRASS_VERBOSE=0

time_start=$SECONDS
. basics_15jun22/sge_Mink3daily/default_paths_etc.sh
. basics_15jun22/sge_Mink3daily/some_settings_46.sh

cd /mnt/data/basics_15jun22/sge_Mink3daily/

# Only compile if the flag is present
if [ $COMPILE_FLAG -eq 1 ]; then
  cd /mnt/data/basics_15jun22/sge_Mink3daily/
  ./compile_java.sh Scenarios.java CalculateProduction.java BashScripts.java Mink3p2daily.java ScenariosProcessor.java ScenariosRunner.java PlantingScenario.java
  # Other useful ones below
  # ./compile_java.sh Config.java GenerateScenarios.java Scenarios.java CalculateProduction.java WriteCopyBlockForDailyWeather.java Mink3p2daily.java SplitTextMatrices.java
  # ./compile_java.sh Config.java GenerateScenarios.java BashScripts.java Scenarios.java Mink3p2daily.java SplitTextMatrices.java

fi


scenarios_csv_location="/mnt/data/basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.csv"
config_file_location="/mnt/data/$1"
script_folder=/mnt/data/basics_15jun22/sge_Mink3daily/
run_parameters_csv_folder="/mnt/data/basics_15jun22/sge_Mink3daily/parameters/"
whether_run_DSSAT_or_process=$2

cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin
java -ea org.Scenarios.Scenarios $scenarios_csv_location $script_folder $run_parameters_csv_folder $config_file_location $whether_run_DSSAT_or_process
time_end=$SECONDS
echo "duration (seconds)"
echo $(echo "scale=2 ; $time_end - $time_start " | bc -l)
