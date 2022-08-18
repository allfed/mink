#!/bin/bash

. default_paths_etc.sh

cd ~/Code/mink/basics_15jun22/small_java_programs/java8_IFPRIconverter/src/

javac org/DSSATRunner/Mink3p2daily.java

mv org/DSSATRunner/Mink3p2daily.class ../bin/org/DSSATRunner/Mink3p2daily.class 

cp -a /home/dmrivers/Code/mink/basics_15jun22/sge_Mink3daily/../small_java_programs/*  $on_node_runner_dir

cd $on_node_DSSAT_dir

# /usr/bin/java "-mx1400M" -cp "${on_node_runner_dir}java8_IFPRIconverter/bin/" org.DSSATRunner.MinkRunner3p2daily $on_node_runner_init_file