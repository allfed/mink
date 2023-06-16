#!/bin/bash

# This script aims to map the maize varieties with the geographical regions using
# mega-environment masks as the relation

# Set IFS to newline for proper list handling
IFS="
"

# Defining a list of climates, brute force initially
climate_list="nonsense_single_pass"

# Predefined output tag
output_tag="GEOfixmaize"

# Define placeholders for substitutions
snx_placeholder="SNXSNXSNX"
cli_placeholder="CLICLICLI"

# Getting the current mapset from GRASS GIS
yield_mapset=$(g.gisenv get=MAPSET)

# Constructing run list by replacing specific patterns from the raster list
run_list=$(g.mlist rast pat="best_yield_*__mzK015**yield_mean" | sed "s/mzK015/${snx_placeholder}/g")


# Defining a list of mega-environments and their corresponding wheat varieties
# Format: SNX    Mega-environment
spamres_snx_me_list=\
"
mzK014  simplegrownspamres_ME_mme_ALLELSE@mega_environments
mzK018  simplegrownspamres_ME_mme_ALLELSE@mega_environments

mzK015  simplegrownspamres_ME_mme_1@mega_environments

mzK013  simplegrownspamres_ME_mme_2@mega_environments
mzK017  simplegrownspamres_ME_mme_2@mega_environments

mzK014  simplegrownspamres_ME_mme_3@mega_environments
mzK017  simplegrownspamres_ME_mme_3@mega_environments

mzK016  simplegrownspamres_ME_mme_4@mega_environments

mzK014  simplegrownspamres_ME_mme_5@mega_environments
mzK018  simplegrownspamres_ME_mme_5@mega_environments

mzK014  simplegrownspamres_ME_mme_6@mega_environments

mzK029  simplegrownspamres_ME_mme_7@mega_environments

mzK027  simplegrownspamres_ME_mme_8@mega_environments

mzK021  simplegrownspamres_ME_mme_9@mega_environments

mzK021  simplegrownspamres_ME_mme_10@mega_environments

mzK023  simplegrownspamres_ME_mme_11@mega_environments

mzK024  simplegrownspamres_ME_mme_12@mega_environments

mzK014  simplegrownspamres_ME_mme_13@mega_environments

mzK025  simplegrownspamres_ME_mme_14@mega_environments

mzK026  simplegrownspamres_ME_mme_15@mega_environments
"

# Defining a valid SNX for the region setting
valid_SNX="mzK014"

# Iterating over each item in the run list
for run_line in $run_list; do
  echo "-- $run_line --"

  # Iterating over each climate in the climate list
  for climate in $climate_list; do
    echo "---- $climate ----"

    # Substituting placeholders with appropriate values
    needs_snx=${run_line/$cli_placeholder/$climate}
    junk_name_2=${needs_snx/$snx_placeholder/$output_tag}

    # Defining raster names
    yield_rast=${junk_name_2}
    count_rast="count_for_${yield_rast}"

    # Setting the region based on the projected yield
    g.region rast=${needs_snx/$snx_placeholder/$valid_SNX}@$yield_mapset

    # Initializing rasters to 0
    r.mapcalc $yield_rast = "0.0" 2>&1 | grep -v "%"
    r.mapcalc $count_rast = "0" 2>&1 | grep -v "%"

    # Iterating over each line in spamres_snx_me_list
    for snx_me_line in $spamres_snx_me_list; do
      echo "-- $snx_me_line --"

      # Extracting SNX and mask values from the line
      this_snx=$(echo "$snx_me_line" | cut -f1)
      this_mask=$(echo "$snx_me_line" | cut -f2)

      # Updating this_yield raster name
      this_yield=${needs_snx/$snx_placeholder/$this_snx}@$yield_mapset

      # Increment count_rast for each non-null area in the mask
      # Calculate the yield for the current SNX and add it to the yield raster
      r.mapcalc $count_rast = "$count_rast + (1 - isnull($this_mask))" 2>&1 | grep -v "%"
      r.mapcalc $yield_rast = "$yield_rast + (1 - isnull($this_mask)) * $this_yield" 2>&1 | grep -v "%"
    done

    # Performing average yield calculation
    r.mapcalc $yield_rast = "$yield_rast / $count_rast" 2>&1 | grep -v "%"

    # Display count of cells with non-NULL values
    r.univar $yield_rast -g --q | grep "^n="

    # Removing the temporary count raster
    g.remove rast=$count_rast
  done
done
