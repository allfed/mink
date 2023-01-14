#!/bin/bash
# this runs a java script that does everything!
# the data which defines the scenario(s) is currently located at basic_15jun22/sge_Min3daily/scenarios/scenarios.csv.
# this scenarios csv can be modified to run arbitrary number of simulations.

cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/src;
javac org/Scenarios/Scenarios.java;
java -ea org.Scenarios.Scenarios /mnt/data/basics_15jun22/sge_Mink3daily/scenarios/scenarios.csv /mnt/data/basics_15jun22/sge_Mink3daily/
