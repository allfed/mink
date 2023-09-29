# mink gridded crop model


## Quick Start

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
## Running a gridded crop model run
There are a couple steps involved in trying out the software for the first time.
You need to take a look at a few places in the repository to alter the physical parameters for crop modelling:

```
SNX_files: this folder holds the configuration for the actual SNX files that can be used by the program.
In particular, to make things easier, you can alter data_to_generate_SNX_files.csv. Each row is a new SNX file, and the details around planting these crops can be found there.
The cultivar-specific values will be replaced in the template found in shared_SNX_template.txt. Take a look and modify that file as needed.
SNX files are generated in the generated_SNX_files directory.
StableCarbonTable can also be modified.
scenarios/ contains many config yaml files which are used to specify a given run. Be sure to take a look and customize as you wish. Physical crop-specific information is set in the physical_parameters settings. It's also probably improtant to modify the n_chunks to match the number of physical cores on your machine, for efficient DSSAT runs.
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

### Additional Processing

- A few year's yields are averaged in the `averageYieldsAcrossYears` function.
- The best planting month for each cell is found, creating a new GRASS GIS raster with the "planting_month" yield replaced with "BestYield".
- A raster with the averaged yields per hectare from the best planting months is created.
- Averaging all the crop cultivars produces a raster ending with "averaged_[RF or IR]".
- Production is calculated using the SPAM 2010 imported maps, resulting in rasters ending with "_production_[RF or IR]", "_production", and "_overall".

## Development

## Reference

- [Singularity user guide](https://sylabs.io/guides/3.5/user-guide/index.html)
- [GRASS GIS 6 on Fedora](https://grasswiki.osgeo.org/wiki/Compile_and_Install#GRASS_GIS_6_on_Fedora) (Used as reference for CentOS 7 singularity container.)
- [GRASS v6.5.svn Programmer's Manual](https://grass.osgeo.org/programming6/index.html)
