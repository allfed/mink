#!/bin/bash

#
# Quick summary of what this does: runs r.in.xyz to create GRASS rasters using 
# [file_description]STATS.txt in the chunks_to_GRASS folder
# the data to import are passed in from command line (probably by a java call)
# 
# echo ""
# echo "running generateRasterFromColumns script"
# echo ""

data_file=$1
raster_name_to_save=$2

# Use the file for input instead of echoing the argument
r.in.xyz input=$data_file output=$raster_name_to_save x=2 y=1 z=3 fs=tab --o --q

# data_with_columns=$1
# raster_name_to_save=$2
# echo -e "$data_with_columns" | r.in.xyz input=- output=$raster_name_to_save x=2 y=1 z=3 fs=tab --o --q

# r.in.xyz input=$data_with_columns output=$raster_name_to_save x=2 y=1 z=3 fs=tab --o --q

if [ $? -eq 1 ]; then
    echo ""
    echo "ERROR: An error occurred while importing DSSAT results r.in.xyz. This means DSSAT did not successfully generate results for this planting month, cultivar, and scenario description, in one or more pixels (most likely, all of them)."
    echo "    You will need to either change the range of planting months processed, adjust cultivars associated with the DSSAT run, or re-run DSSAT to generate the necessary files."
    echo "    The issue is most likely having to do with specifically, $file_to_process"
    echo "    To duplicate this error, you would have to run the following script from ${PWD}: "
    echo "    $ $0 [relevant tab separated data columns] $raster_name_to_save"
    echo ""
    exit 1
fi

# echo ""
# echo "done running generateRasterFromColumns script"
# echo ""
