# mink gridded crop model

## Java implementation of run_from_csv.sh
running the code:
run:
```
$run_from_csv.sh
```

This script then runs through a set of scenarios found in scenarios.csv. It follows the following procedure:

It first calls the java Scenarios function "main()" in basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/Scenarios.java. This imports the csv, and calculates the yield in the region specified in scenarios.csv by running the basics_15jun22/sge_Mink3daily/mink3daily_run_DSSAT_tile.sh (which is what runs DSSAT itself for each grid cell specified in the region in the scenarios.csv file). This is run over a period of several years.

Yields are stored in the grass gis rasters (which can be viewed with the `g.mlist -r` command).

Once the yield is calculated, a few year's yields are averaged in the averageYieldsAcrossYears function (years to average can be specified in the Scenarios.java).

The main() function in Scenarios then runs the CalculateProduction class, which is responsible for aggregating crop yields into values. 
