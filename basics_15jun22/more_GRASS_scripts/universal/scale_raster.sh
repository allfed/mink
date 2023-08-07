#!/bin/bash

# multiply raster by number
# arg1: input raster 1
# arg2: value to scale by
# arg3: output production

# The raster map is not entirely null, so do the multiplication
r.mapcalc "$3 = $1 * $2"
