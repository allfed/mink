#!/bin/bash

# multiply raster by number
# arg1: input raster to modify
r.mapcalc "$1_temp = if($1<0,0,$1)"
g.rename rast=$1_temp,$1
