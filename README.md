# mink gridded crop model

## Java implementation of run_from_csv.sh
running the code:
run:
```
$run_from_csv.sh
```

This script then runs through a set of scenarios found in scenarios.csv. It follows the following procedure:

It first calls the java Scenarios function "main()" in basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/Scenarios.java. This first imports the csv into java string arrays. It then initializes using "basics_15jun22/sge_Mink3daily/build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh". This is responsible for loading several initial parameters for the crop model run and setting the region. Next it calculates the yield in the region specified in scenarios.csv by running the basics_15jun22/sge_Mink3daily/mink3daily_run_DSSAT_tile.sh (which is what runs DSSAT itself for each grid cell specified in the region in the scenarios.csv file). This is run over a period of several years.

The variables determined from the "real" yield per hectare DSSAT runs are loaded from the "basics_15jun22/sge_Mink3daily/chunks_to_grass/\*STATS.txt" files and some columns from these files are saved as GRASS GIS rasters using the script "basics_15jun22/sge_Mink3daily/READ_DSSAT_outputs_from_cols_FEW.sh". All GRASS GIS rasters can be viewed with the `g.mlist -r` command.

Raster yields generated from READ_DSSAT_outputs_from_cols_FEW.sh (ending in "_real_[number]") are specific to the year the crop model was run, the planting date month, whether irrigation was applied, and the specific crop cultivar, in the following format:

snx_name: [crop 2 letter code]_[cultivar]_[RF (rainfed) or (IR) irrigated]

[snx_name]_[co2_level]_[weather_prefix]\_D\_[planting_month]_noGCMcalendar_p0_[crop_name]_\_\_real_[year_number]


First, a few year's yields are averaged in the averageYieldsAcrossYears function (which years to average can be specified in the Scenarios.java) and saved as GRASS GIS rasters. These end in "\_real" as the year_number has been averaged.

Next, the best planting month for each cell is found, creating a new GRASS GIS raster  with the "planting_month" yield replaced with "BestYield".

We have a raster with the averaged yields per hectare from the best planting months. The main() function in Scenarios then runs the CalculateProduction class, which is responsible for averaging crop yields for cultivars and aggregating crop yields into total production.

Averaging all the crop cultivars produces a raster ending with "averaged_[RF or IR]".

Getting production is accomplished using the SPAM 2010 imported maps, which were previously saved as rasters ending in "\_rainfed_cropland" and "\_irrigated_cropland". The "averaged_[RF or IR]_[wet or dry]" yield rasters are multiplied by the irrigated or rainfed area to get a raster for rainfed production and a raster for irrigated production. This raster ends with "\_production_[RF or IR]".

Next, rainfed and irrigated production are summed in order to get a total yield for each cultivar. This raster ends in "\_production"

Finally, the production is averaged and the result os saved as "\_overall"