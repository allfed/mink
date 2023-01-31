#!/bin/bash

# divide two rasters
# arg1: input raster 1
# arg2: input raster 2
# arg3: output production
r.mapcalc "$3 = $1 / $2"
# cd $3

# if you want to save the result as ascii, you could uncomment this line below
r.out.ascii input=$3 output=- > $3.asc
