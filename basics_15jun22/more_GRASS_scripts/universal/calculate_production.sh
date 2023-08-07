#!/bin/bash

# multiply two rasters
# arg1: input raster 1
# arg2: input raster 2
# arg3: output production
r.mapcalc "$3 = $1 * $2" #2>&1 | grep -v "0..5..10" | grep -v "Reading raster map"
