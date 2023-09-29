#!/bin/bash

# average a series of rasters and save the result

# arg0: rasters
# arg1:minimum raster value
# arg2:method of averaging
# arg3:location to save
rasters_to_average=$1
minimum=$2
method=$3
average=$4

# echo ""
# echo "running the average..."


# r.series: 
#    *NOTES on null values*
#    No-data (NULL) handling
#    https://grass.osgeo.org/grass82/manuals/r.series.html
#    Without -n flag, the complete list of inputs for each cell (including NULLs) is passed to the aggregate function. Individual aggregates can handle data as they choose. Mostly, they just compute the aggregate over the non-NULL values, producing a NULL result only if all inputs are NULL. 

if [ "$method" == "mode" ]; then
    # two step process if we're getting the mode (most common) of a categorical raster: 
    #    have to convert from the DCELL to CELL
    #    (converting raster from double values to integer values)
    r.series --overwrite input=$rasters_to_average output=deleteme_$average method=$method range=$minimum,999999 --quiet 
    r.mapcalc "$average = round(deleteme_$average)"
    g.remove rast=deleteme_$average
else
    r.series --overwrite input=$rasters_to_average output=$average method=$method range=$minimum,999999 --quiet 
fi
