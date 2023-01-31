#!/bin/bash
# this runs a java script that does everything!
# the data which defines the scenario(s) is currently located at basic_15jun22/sge_Min3daily/scenarios/scenarios.csv.
# this scenarios csv can be modified to run arbitrary number of simulations.
time_start=$SECONDS
. basics_15jun22/sge_Mink3daily/default_paths_etc.sh
. basics_15jun22/sge_Mink3daily/some_settings_46.sh
cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/src
javac org/Scenarios/Scenarios.java
java -ea org.Scenarios.Scenarios /mnt/data/basics_15jun22/sge_Mink3daily/scenarios/$1 /mnt/data/basics_15jun22/sge_Mink3daily/
time_end=$SECONDS
echo "duration (seconds)"
echo $(echo "scale=2 ; $time_end - $time_start " | bc -l)
