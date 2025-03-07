# mink gridded crop model

Ricky Robertson has been working on a crop model based on DSSAT for over a decade at IFPRI. Although Ricky's mink [is documented](https://sylabs.io/guides/3.0/user-guide/installation.html), the code is not open source. Furthermore, the coding practices were not suitable for continued work.

This repository, developed at [ALLFED](allfed.info), is an attempt to make his hard work open to the public, accessible, better validated, and easier to run, as well as more reliable, faster, and more flexible in application. Rather than focusing on the effects of co2 induced climate change, this code was developed to assess the effects of ASRS (Abrupt Sunlight Reduction Scenario) using climate models that were developed at the University of Colorado Boulder from the Toon group. More specifically, this codebase has been used to assess the effects of 150 tg soot scenarios on global crop production, at the relatively low resolution of ~1.5 by ~2 degrees, and to assess the crop yield potential for relocated crops, more suitable for the new colder climate. It has also been used to assess the expansion of cropland area to increase production where it is still possible to grow crops. 

This additionally codebase features portability speed readability and accuracy.
 - it has been developed to run in parallel on multiple core machines
 - it has been validated against SPAM and FAOSTAT crop yields in climate conditions of circa 2005
 - it has been majorly refactored and minimized from its original code footprint
 - you can run it on any linux machine, and it will work without error, using the singularity virtual machine

This crop model is designed to work with daily weather data produced from the CLM5 climate model, but historical climate conditions can also be assessed, as long as they are in the WTH format (see example_weather folder for an example of such a format).

Please note that the underlying crop model is DSSAT version 4.7.5.11 is being used, which is somewhat out of date (DSSAT is now at the version v4.8.2.10 as of June 2024). Also note that this is not the only (or even necessarily the easiest) way to run crop models with DSSAT. Before embarking on a crop modelling project with this codebase, it would be wise to investigate the range of crop models that are available to you, notably including pDSSAT from the AGMIP project run by the University of Potdam.


## Quick Start

NOTE: when running single grid point regions, GRASS struggles a little bit.
The way to do it is a larger region (with at least one standard cell for crop growth to be safe) using the -s flag in generate_scenarios_csv.sh, and then run the generate_scenarios_csv.sh with the single cell region.

In general, it's safest to run ./generate_scenarios_csv.sh with the entire world once, to be safe, and never run the generate_scenarios_csv.sh with the -s flag again. But you can also do what was just described above.

You can access the pre-built Singularity container here ...
TODO: add details of where to get the complied container, if it's made available publically
... otherwise, you can build the container yourself using the documentation below.
## Requirements
Disk space once fully installed:
```bash
mink/
    1.4Gb grassdata/
    205Mb GRASS_program
    15Mb  DSSAT
    1.1Gb mink_sandbox
        64Mb mink_sandbox/usr/local/grass-6.5.svn
TOTAL: 2.6Gb required
HOWEVER, there is probably a lot larger files with your weather data (for example, 13 years at about 2 degrees resolution on a daily basis adds to about 9Gb unzipped).
```
plus 294Mb for the temporary grassdata.zip, and another 140Mb for the singularity installation.
## Dependencies
### Singularity
MINK leverages Singularity to create a containerised environment from which to run the model.
Install singularity by following the [Singularity Installation instructions](https://sylabs.io/guides/3.0/user-guide/installation.html).
Note for Debian/ubuntu:  Using the following section in the installation user guide:
"Install the Debian/Ubuntu package using apt"
Make sure to follow the instructions carefully.
To test that singularity is properly installed, run the command
```bash
singularity --version
```
You should get "2.6.1-dist" or later version.
### Subversion
Dependencies for the build are fully handled in the Singularity container and can be viewed in ~mink.def~. 
For reference, however, MINK depends on the [GRASS v6.5 development branch](https://svn.osgeo.org/grass/grass/branches/develbranch_6/).
To test that subversion is installed, run
```bash
svn --version
```
You should get "svn, version 1.9.3 (r1718519)" or similar.
## Setup 
First, you'll want to clone this git repository. Then there are a few steps to set things up.
### grassdata
There are some custom grass data files to be included in your repository.
These can be downloaded from here:
https://drive.google.com/drive/folders/1uWCqUG5vt9ETtpb0sWbIXzcTtBFqCmUv
You will need to unzip this folder and place it in the root of the mink/ git directory. The folder should be called "grassdata"
### Grass permissions table
There are a few permission modifications needed to run grass.
To make these modifications, you first need to know your username.
First, type the command
```bash
whoami
```
That will tell you your username.
Now, from the mink/ root directory, enter the commands (replacing the username with yours).
```bash
cd grassdata/
sudo chown -R your_username:your_username world/
```

(This is just in case they are not owned by the user)

#### Singularity build in a sandbox
It is likely you will want to build a sandbox to test out singularity first, and ensure everything is installed properly.
To do so, run the following:
```bash
sudo ./build_singularity.sh
```
You should see the output: "Singularity container built: mink_sandbox" as one of the last few lines of a long output.
Next, run the sandbox as a writable image:  
```bash
singularity shell --writable --bind $PWD:/mnt/data mink_sandbox
```
This will allow you to interactively try each command to see where the problem arises without having to redo all the commands each container build. 
You should now be in a shell. To test out whether everything worked, run the following within the shell:
```bash
cd /mnt/data
./build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh
```


You should see the following:
```bash
    ++ making an ALL raster ++
 CHECK THE SPAM RASTERS: should be the 30may14 ones... 
outputs being placed in = [to_DSSAT]
-- creating mask for deleteme_all Wed Jul  6 15:03:50 PDT 2022 --
Removing raster <MASK>
Raster map <MASK> not found
<MASK> nothing removed
    ++ masking ++
      __ masking pin-prick __
0..4..8..11..15..18..22..25..29..33..36..40..43..47..50..54..58..61..65..68..72..75..79..83..86..90..93..97..100
    ++ N/inits ++
0..4..8..11..15..18..22..25..29..33..36..40..43..47..50..54..58..61..65..68..72..75..79..83..86..90..93..97..100
real output
to_DSSAT/catdailyB__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE
 -- exporting 1_noGCMcalendar_p0 maize eitherN250 Wed Jul  6 15:03:51 PDT 2022 --
0..4..8..11..15..18..22..25..29..33..36..40..43..47..50..54..58..61..65..68..72..75..79..83..86..90..93..97..100
Removing raster <MASK>
```
## Simple Tutorial: Running the example single pixel

Note: ensure you have set up singularity and imported relevant SPAM data for this tutorial.

In order to run a single point of the model, you can run the following commands after entering the singularity virtual machine.

Let's now run a dummy program that should finish in under a minute of compute: a single pixel of wheat wh076rf. In this case, we're running a single rainfed pixel. The planting was attempted in all 12 months, but we're having the program select the best planting month by choosing the highest yielding month in that pixel to plant. We're also running on all cropland that is not currently planted (expanded cropped area), imagining a successful scale-up of crop area. We're running in the 150tg nuclear winter climate due to a full scale nuclear war starting May of year 5 (as seen in the WTH file `example_weather/150Tg_biasCorrected_-23.4375_133.125.WTH`), meaning a very severe climate catastrophe has just occurred. We have selected to test a single area of approximately 200 square kilometers in the australian outback.

Please note: The first year in the results (listed as processed year 3) actually represents year 5 due to the subtraction of two years of acclimitization in the crop model. 


#### step 0: compile java
You need all the java programs compiled to actually run mink. To do so, navigate to `basics_15jun22/sge_Mink3daily/scenarios`:
```bash
cd basics_15jun22/sge_Mink3daily/scenarios
```
Next, run the java compile:
```bash
./compile_java.sh
```

May take a few minutes. Finally, navigate back to root dir:
```bash
cd -
```

#### step 1: converting SPAM


It's wise at the outset to generate a spam map of the whole world, so you don't run into problems later on trying to access SPAM where it was not generated. You need to do this once when you start out, and then again every time your resolution changes.

(You don't actually need any of the weather files listed in this particular yaml. It's just for importing Wheat for SPAM and because it has a global latitude/longitude extent at the proper resolution.)

```bash
./generate_scenarios_csv.sh --spam scenarios/world/wheat_relocated.yaml
```

That will take a few minutes.

You could have also added the `--compile` flag to compile relevant java files in the `generate_scenarios_csv.sh` script, but I left that out because we just ran compile for all files in the previous step.

#### step 2: generate scenarios before running DSSAT

Next, let's generate the scenarios csv for the single pixel we want by rerunning the command without the `--spam` flag using the yaml I wrote for this purpose:
```bash
./generate_scenarios_csv.sh scenarios/example_singlepoint/wheat_baseline.yaml

```

It's helpful for your understanding to take a look at that `scenarios/example_singlepoint/wheat_baseline.yaml` file as well, to see which data sources are being used.

Again, you could have also added the `--compile` flag to compile relevant java files in the `generate_scenarios_csv.sh`script, but I left that out because we just ran compile for all files in the previous step.

#### step 3: run DSSAT

Finally, let's compute the crop yields.

```bash
./run_from_csv.sh scenarios/example_singlepoint/wheat_baseline.yaml DSSAT 
```
And again, you could have also added the `--compile` flag to compile relevant java files in the `run_from_csv.sh` script, but I left that out because we just ran compile for all files in the compile step above.

#### step 4: process the DSSAT run

Now, we actually want to take a look at the resulting yields!
```bash
./run_from_csv.sh scenarios/example_singlepoint/wheat_baseline.yaml process
```

A bunch of interesting stuff should now reside in `example_results_control`. If that didn't work, try running
```bash
mkdir example_results_control
```
... and run from csv again.

#### step 6: revel in our success (by looking at resulting yield heat maps and csv outputs)

Congratulations, you made it quite a long way from cloning the repo!

Now let's take a look at `example_results` to see what our hard work to get this crop model working has earned us:
```bash
cd example_results
ls -ltr
```

Please note: The first year in the results (listed as processed year 3) actually represents year 5 due to the subtraction of two years of acclimitization in the crop model. 

We just showed a bunch of interesting data about what resides in this folder, like when it was made, and sorted in reversed order of when created. Notably, you can see there are a bunch of png's which show images of what the yields are. You can see the yields by country as well! This required a lot of high resolution raster work, etc.

Don't forget to go back to the root repo when complete:
```bash
cd -
```

#### more useful things you can do
The majority of the work done at ALLFED was to create more workable and useful processing and rendering/export tools. You can navigate to 
```bash
cd basics_15jun22/sge_Mink3daily/export_scripts
```

and run various export scripts, notably including an export to ascii (`save_ascii.sh`), heatmap generator (`quick_display.sh`) and export to geotiff format (`save_tif.sh`) 

## Running a gridded crop model run
There are a couple steps involved in trying out the software for a full gridded run.
You need to take a look at a few places in the repository to alter the physical parameters for crop modelling:

```
SNX_files: this folder holds the configuration for the actual SNX files that can be used by the program.
In particular, to make things easier, you can alter data_to_generate_SNX_files.csv. Each row is a new SNX file, and the details around planting these crops can be found there.
The cultivar-specific values will be replaced in the template found in shared_SNX_template.txt. Take a look and modify that file as needed.
SNX files are generated in the generated_SNX_files directory.
StableCarbonTable can also be modified.
scenarios/ contains many config yaml files which are used to specify a given run. Be sure to take a look and customize as you wish. Physical crop-specific information is set in the physical_parameters settings. It's also probably improtant to modify the n_chunks to match the number of physical cores on your machine, for efficient DSSAT runs.

**Important Note**: There is a known bug that the simulation does not work if running more parallel processes than the minimum number of grid cells of any cultivar in a given crop model run in the region.
These generate the actual distinct DSSAT runs which can be found in the generated_scenarios.csv.
This file should not be modified directly, but demonstrates exactly which SNX files are being run under which settings. this provides the scenario_number for the java runs. If multiple planting months are attempted, the planting months (and years) are run several times for each scenario number.

grassdata contains a great deal of information relevant to the runs.
the "spam" folder contains crop-specific masks for where each crop is grown. It also indicates the number of plantings per year of each crop implicitly through "harvested area" ("_H") as opposed to "physical area" ("_A"). See the Readme_v2r0_Global.txt.

nitrogen_maps contains information about the amount of nitrogen applied for each crop

In addition, the PERMANENT mapset contains a pre-loaded vector with a map of each country circa ~2005.

All other data should be loaded into grass from tif or ascii rasters.

FAOSTAT data can be helpful for year-by-year analysis of the dataset.  

Also, make sure to place the daily weather in the control_mink/ and catastrophe_mink/ folders (or whatever folder name you choose instead). The wth files must already be in the format needed for DSSAT.

Parameters files:
default_cultivar_mappings.csv contains which cultivars map to which megaenvironments. Pixels within a megaenvironment will average the yields of cultivars contained there.
Not all crops use these cultivar mappings.

moisture_contents multiplies the DSSAT supplied dry yields to their wet weight equivalents, useful for SPAM.

```

## Feeling Confident (Run Directly)
If you are feeling extremely confident, you can run MINK singularity container without testing slowly with the sandbox:
To run MINK use `singularity exec -B ~/.Xauthority mink.sif grass` and hit ENTER.
TODO: add more details and a toy dataset?
## Troubleshooting
### ~/.Xauthority issues
If this file doesn't exist, the dedicated location may be stored in the `$XAUTHORITY` variable and `-B $XAUTHORITY` must be used instead ([reference](https://pawseysc.github.io/singularity-containers/42-x11-gnuplot/index.html)).
Certainly! Here's the corrected version of the additional README content, with appropriate formatting for the underscores and other minor adjustments.

```markdown
## Feeling Confident (Run Directly)

If you are feeling extremely confident, you can run MINK singularity container without testing slowly with the sandbox:
To run MINK, use the command:

```bash
singularity exec -B ~/.Xauthority mink.sif grass
```

and hit ENTER.

TODO: add more details and a toy dataset?

## Troubleshooting

### ~/.Xauthority issues

If this file doesn't exist, the dedicated location may be stored in the `$XAUTHORITY` variable and `-B $XAUTHORITY` must be used instead ([reference](https://pawseysc.github.io/singularity-containers/42-x11-gnuplot/index.html)).

## Java implementation of run_from_csv.sh

This script then runs through a set of scenarios found in `basics_15jun22/sge_Mink3daily/scenarios`. It follows the following procedure:

- It first calls the Java Scenarios function "main()" in `basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/Scenarios.java`. 
- Initializes using `basics_15jun22/sge_Mink3daily/build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh`. 
- Calculates the yield in the region specified in `scenarios.csv` by running `basics_15jun22/sge_Mink3daily/mink3daily_run_DSSAT_tile.sh` (which is what runs DSSAT itself for each grid cell specified in the region in the `scenarios.csv` file). This is run over a period of several years.

The variables determined from the "real" yield per hectare DSSAT runs are loaded from the `basics_15jun22/sge_Mink3daily/chunks_to_grass/*STATS.txt` files. Some columns from these files are saved as GRASS GIS rasters using the script `basics_15jun22/sge_Mink3daily/READ_DSSAT_outputs_from_cols_FEW.sh`. All GRASS GIS rasters can be viewed with the `g.mlist -r` command.

Raster yields generated from `READ_DSSAT_outputs_from_cols_FEW.sh` (ending in `_real_[number]`) are specific to the year the crop model was run, the planting date month, whether irrigation was applied, and the specific crop cultivar, in the following format:

`snx_name: [crop 2 letter code]_[cultivar]_[RF (rainfed) or (IR) irrigated]_[snx_name]_[co2_level]_[weather_prefix]_D_[planting_month]_noGCMcalendar_p0_[crop_name]__real_[year_number]`

Note on naming:
each java raster variable usually has a "_name_" in it. The variable is a name of a raster (a string). But the raster itself is what contains the data referred to in the name of the variable! Rasters are stored as files on the hard drive. So when you alter a raster in one part of the program, it will be changed elsewhere.

### Additional Processing

- A few year's yields are averaged in the `averageYieldsAcrossYears` function.
- The best planting month for each cell is found, creating a new GRASS GIS raster with the "planting_month" yield replaced with "BestYield".
- A raster with the averaged yields per hectare from the best planting months is created.
- Averaging all the crop cultivars produces a raster ending with "averaged_[RF or IR]".
- Production is calculated using the SPAM 2010 imported maps, resulting in rasters ending with "_production_[RF or IR]", "_production", and "_overall".

## Taking a look at simulation results
Generally you will want to look at heatmaps of yields, selected planting months, and days to maturity where applicable.

### Generated scenarios
The generated scenarios (from generate_scenarios_csv.sh) are located at scenarios_csv_location="/mnt/data/basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.csv".

A scenario is defined as one row of  generated_scenarios.csv.
There is one scenario for each crop (although crops are typically run one at a time), cultivar (specified by a specific SNX name) and irrigation type (rainfed or irrigated).
### by country results
These data are already exported as a csv aggregated by country.

To export by country, you will have to ensure the proper flags have been set in your configuration yaml file:

```
make_rasters_comparing_overall_to_historical: true
calculate_as_wet_weight: true
average_yields: true
calculate_each_year_best_month: true
find_best_yields: true
calculate_rf_or_ir_specific_average_yield: true
calculate_rf_or_ir_specific_production: true
calculate_rf_plus_ir_production: true
calculate_average_yield_rf_and_ir: true
make_rasters_comparing_overall_to_historical: true
```

and that you have run:

```
./run_from_csv.sh [config_file_name] process 
```

At that point, you can use some convenient scripts to visualize the by-country csv's in python.

Please note the following caveat: the "overall" yield, as opposed to the yield for each year, is determined using an assumption that farmers choose the best month averaged over all years for the crop yield. 

It's assumed in the model currently that farmers don't know in advance what the best month will be. They have to use the heuristic of what month has worked best in the past. So for example, years 1-7, what is the yield if we always plant in January? Or February? Do that for all 12 months and pick the best month, for each cell. Then the most realistic yield will be if the farmer plants in this highest yielding month (on average for all the years), each year. Each cell will have its own planting month that is used to get the overall mean yield.

You can either choose to use this more pessimistic yield assumption, or average the yields (or production) for each country over the years to get the yield based on a more optimistic assumption that farmers are spot on, and choose the best month each year, every year for planting.


## Adding a Crop
As long as a crop is supported in DSSAT, then it should be straightforward to add it.
You will need to find an SNX file corresponding to the cultivars you would like to run in the archive, or import them yourself. You can also place your own SNX files in the generated_SNX_files folder, but it's recommended to modify the template, to make it clear how the SNX file you are using differs from all the other SNX files.
You can see what SNX files exist in basics_15jun22/sge_Mink3daily/SNX_files/data_to_generate_SNX_files.csv and the shared_SNX_template.txt in that folder. Examples of what this generates are in generated_SNX_files.

Once you've either generated or placed your SNX file in generated_SNX_files, you can either just run the cultivar for the whole region, or create maps that run for a certain region. You would need to create a GRASS gis raster that covered the region and export it as a .pack file to grassdata/world/megaenvironments_packed. You can also just specify the maps according to the megaenvironments -- you can always take a look at the rasters, they should be loaded in whenever scenarios are generated. (hint: to display a raster, use the basics_15jun22/sge_Mink3daily/export_scripts/quick_display.sh script. You can also render multiple rasters with the same scale in a heat map with ./render_all_rasters_same_scale.sh in the same folder). 

To specify which cultivars are run for which megaenvironment maps dfined in the previous step, go to default_cultivar_mappings.csv. Also add in the wet weight moisture content in moisture_contents.csv in the same folder if it's not there.

To run for the whole region, you need to create the scenario file. The scenario file should be placed in scenarios/ folder. 

Be sure to add in a map of elemental nitrogen application in the /home/dmrivers/Code/mink/grassdata/world/nitrogen_maps/ folder. Once you've loaded it into grass, use the r.pack `input=$raster output=$raster.pack` command where $raster is a bash variable assigned the name of the nitrogen map. Several crops already have these maps defined. However, they are not all up to date -- I have used the N_for_rice_IR_12aug13.pack to match published work.

An example with just SNX files (one is specified, but you can specify more):
crops:
- name: rapeseed
  fertilizer_scheme: winterwheat
  snx_names: [cnINVIGOR5440]
  irrigation_to_try: [RF, IR]
  nitrogen_irrigated: N_for_wheat_IR_12aug13
  nitrogen_rainfed: N_for_wheat_RF_12aug13

The crop name:
    basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/DSSATRunner


If you don't list SNX names, then the megaenvironments will be used. If you list them, it is assumed there is no mapping available in default_cultivar_mappings.csv.

crops:
- name: maize
  fertilizer_scheme: threeSplitWithFlowering
  nitrogen_irrigated: N_for_maize_IR_12aug13
  nitrogen_rainfed: N_for_maize_RF_12aug13


Finally be sure to modify other relevant variables -- they are described in existing config files, but most of them can probably stay the same, except the n_chunks variable may need modification based on the hardware you're using to run the simulation.

Once this is all set up, and you have your config file, you should run the commands:

./generate_scenarios_csv.sh --compile --spam [config_file_name] 
./run_from_csv.sh [config_file_name] DSSAT 

that will take some time, then run

./run_from_csv.sh --compile [config_file_name] process 

where the config file is the location of the yaml file you modified in earlier steps.

Future runs should remove the --spam flags. It only needs to be run once. Future runs, if using the same java code, should remove the --compile flag.

The --compile (or -c) flag right after the ./generate_scenarios_csv.sh or ./run_from_csv.sh commands to compile certain relevant java programs. Going in the scripts will allow you to modify what these compiled java programs are (to be sure your saved changes are captured in the program).

The --spam (or -s) flag after the ./generate_scenarios_csv.sh loads all the spam area tiffs as rasters in the program for the current mapset. It should only be run once for a given mapset (usually you only set up the mapset once ever).

You may also need to add your crop at the beginning of the basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/Scenarios/Config.java file in java to match its name in SPAM. And in basics_15jun22/small_java_programs/java8_IFPRIconverter/src/org/Scenarios/GenerateScenarios.java getCropCodeMap() function.

SPAM is necessary as it loads in our crops. You can go to grassdata/world/spam/  to see what the crop 4 letter code would be.

### A Note about years

The specific list of years listed in the scenarios file should be selected with the following in mind:
 - The number of years run by DSSAT is dynamically calculated based on the years array in your scenario configuration. It is calculated as `nFakeYears = last year - first year + 1`. For example, if you specify years [3, 4, 5, 6, 7, 8, 9], DSSAT will run 7 years (9-3+1).
 - The starting planting year is set to the first year in your years array (e.g., 3 in the example above).
 - The years in the config correspond directly to the years of the weather file.
 - DSSAT requires a 90-day spinup/acclimatization period before planting. Because of this requirement, you should never set the first year as year 1 in your scenario configuration, as this would not allow for the full spinup period. Instead, year 2 of the weather files or later should be used as the starting year.


## Development

## Reference

- [Singularity user guide](https://sylabs.io/guides/3.5/user-guide/index.html)
- [GRASS GIS 6 on Fedora](https://grasswiki.osgeo.org/wiki/Compile_and_Install#GRASS_GIS_6_on_Fedora) (Used as reference for CentOS 7 singularity container.)
- [GRASS v6.5.svn Programmer's Manual](https://grass.osgeo.org/programming6/index.html)
