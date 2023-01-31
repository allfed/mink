#!/bin/bash

# average a series of rasters and save the result
# arg0: rasters
# arg1:location to save
echo ""
echo "running the average..."
r.series --overwrite input=$1 output=$2 method=average
cd $3

echo ""
echo "running the save..."
# if you want to save the result as ascii, you could uncomment this line below
r.out.ascii input=$2 output=- > $2.asc
