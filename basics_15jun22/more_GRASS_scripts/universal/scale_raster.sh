#!/bin/bash

# multiply raster by number
# arg1: input raster 1
# arg2: value to scale by
# arg3: output production
echo "running command"
echo "r.mapcalc"
echo "$3 = $1 * $2"
r.mapcalc "$3 = $1 * $2"
# cd $3

# if you want to save the result as ascii, you could uncomment this line below
# r.out.ascii input=$3 output=- > $3.asc
