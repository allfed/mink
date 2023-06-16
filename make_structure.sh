#!/bin/bash

# Define the root directory
root_dir="mink"

# Create the root directory if it doesn't already exist
if [ ! -d "$root_dir" ]; then
  mkdir "$root_dir"
fi

# Define the subdirectories
src_dir="$root_dir/src"
main_dir="$src_dir/main"
java_dir="$main_dir/java"
resources_dir="$main_dir/resources"
dssat_dir="$resources_dir/DSSAT"
test_dir="$src_dir/test"
test_java_dir="$test_dir/java"
test_resources_dir="$test_dir/resources"
build_dir="$root_dir/build"
libs_dir="$root_dir/libs"
gradle_dir="$root_dir/gradle"
bin_dir="$root_dir/bin"
data_external_dir="$root_dir/data_external"

grassdata_dir="$data_external_dir/grassdata"
dssat_data_dir="$data_external_dir/DSSAT"
grass_modules_dir="$root_dir/GRASS_modules"
grass_program_dir="$root_dir/GRASS_program"
reports_dir="$root_dir/reports"
example_report_dir="$reports_dir/example_report.xlsx"
config_dir="$root_dir/config"
simulation_params_file="$config_dir/simulation_params.properties"
simulation_config_file="$config_dir/simulation_config.xml"

# Create the subdirectories if they don't already exist
if [ ! -d "$src_dir" ]; then
  mkdir "$src_dir"
fi
if [ ! -d "$main_dir" ]; then
  mkdir "$main_dir"
fi
if [ ! -d "$java_dir" ]; then
  mkdir "$java_dir"
fi
if [ ! -d "$resources_dir" ]; then
  mkdir "$resources_dir"
fi
if [ ! -d "$dssat_dir" ]; then
  mkdir "$dssat_dir"
fi
if [ ! -d "$test_dir" ]; then
  mkdir "$test_dir"
fi
if [ ! -d "$test_java_dir" ]; then
  mkdir "$test_java_dir"
fi
if [ ! -d "$test_resources_dir" ]; then
  mkdir "$test_resources_dir"
fi
if [ ! -d "$build_dir" ]; then
  mkdir "$build_dir"
fi
if [ ! -d "$libs_dir" ]; then
  mkdir "$libs_dir"
fi
if [ ! -d "$gradle_dir" ]; then
  mkdir "$gradle_dir"
fi
if [ ! -d "$bin_dir" ]; then
  mkdir "$bin_dir"
fi
if [ ! -d "$data_external_dir" ]; then
  mkdir "$data_external_dir"
fi


mkdir -p "$grassdata_dir"
mkdir -p "$dssat_data_dir"
mkdir -p "$grass_modules_dir"
mkdir -p "$grass_program_dir"
mkdir -p "$reports_dir"
mkdir -p "$config_dir"

#Create the example report file if it doesn't already exist

if [ ! -f "$example_report_dir" ]; then
  touch "$example_report_dir"
fi
#Create the simulation configuration files if they don't already exist

if [ ! -f "$simulation_params_file" ]; then
  touch "$simulation_params_file"
fi

if [ ! -f "$simulation_config_file" ]; then
  touch "$simulation_config_file"
fi