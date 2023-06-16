#!/bin/bash
# this runs a java script that can use a simple config file
# to generate a csv file specifying each individual run of the model
# the csv which defines the scenario(s) generated at basic_15jun22/sge_Min3daily/scenarios/generated_scenarios.csv.

set -e # exit if a command fails

time_start=$SECONDS
. basics_15jun22/sge_Mink3daily/default_paths_etc.sh
. basics_15jun22/sge_Mink3daily/some_settings_46.sh
cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/src
javac org/Scenarios/Config.java
javac org/Scenarios/GenerateScenarios.java
scenarios_csv_location="/mnt/data/basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.csv"
config_file_location="/mnt/data/scenarios/AUS/test_AUS_megaenvironments.yaml"
run_parameters_csv_folder="/mnt/data/basics_15jun22/sge_Mink3daily/parameters/"
script_folder=/mnt/data/basics_15jun22/sge_Mink3daily/

java -ea org.Scenarios.GenerateScenarios $script_folder $config_file_location $scenarios_csv_location $run_parameters_csv_folder
time_end=$SECONDS
echo "duration (seconds)"
echo $(echo "scale=2 ; $time_end - $time_start " | bc -l)

