#!/bin/bash

# multiply raster by number
# arg1: input raster 1
# arg2: value to scale by
# arg3: output production

# Calculate univariate statistics of the raster map $1

# echo "running univar for STATS"
# STATS=$(r.univar -g $1)


# # # Check if STATS is empty
# if [ -z "$STATS" ]; then
#     COUNT=0
# else
#     # Extract the count from the statistics
#     COUNT=$(echo "$STATS" | awk -F= '$1=="n"{print $2}' | tr -d '[:space:]')
# fi

# echo "COUNT"
# echo "$COUNT"

# # Check if the count is 0
# if [ "$COUNT" -eq "0" ]; then
#     # The raster map is entirely null, so just return it
#     g.copy raster=$1,$3
# else

    # The raster map is not entirely null, so do the multiplication
    r.mapcalc "$3 = $1 * $2"
# fi

# if you want to save the result as ascii, you could uncomment this line below
# r.out.ascii input=$3 output=- > $3.asc
