# MINK

> A fully open-source global gridded crop model using DSSAT.

The top layer (mink) has been in development for over 10 years at IFPRI/CGIAR. There are several useful scripts enabling full control over the crop model process and modular runs, as well as several useful analysis and visualization tools. At ALLFED, we've taken that setup and put it online for anyone to use much more easily than if it were sitting on IFPRI's offline cluster. This crop model was built with the intention of understand the food production system and how it could be improved for the purposes of food security, and the development of tools to make the results more resilient.

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

```
plus 294Mb for the temporary grassdata.zip


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

### Setup 

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
sudo chown root:root world
cd world/
sudo chown root:your_username *
```

#### Singularity build in a sandbox

It is likely you will want to build a sandbox to test out singularity first, and ensure everything is installed properly.

To do so, run the following:

```bash
sudo ./build_singularity.sh
```

You should see the output: "Singularity container built: mink_sandbox" as one of the last few lines of a long outputh.

Next, run the sandbox as a writable image:  

```bash
sudo singularity shell --writable --bind $PWD:/mnt/data mink_sandbox
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

## Feeling Confident (Run Directly)

If you are feeling extremely confident, you can run MINK singularity container without testing slowly with the sandbox:

To run MINK use `singularity exec -B ~/.Xauthority mink.sif grass` and hit ENTER.

TODO: add more details and a toy dataset?

## Troubleshooting

### ~/.Xauthority issues

If this file doesn't exist, the dedicated location may be stored in the `$XAUTHORITY` variable and `-B $XAUTHORITY` must be used instead ([reference](https://pawseysc.github.io/singularity-containers/42-x11-gnuplot/index.html)).


## Development

## Reference

- [Singularity user guide](https://sylabs.io/guides/3.5/user-guide/index.html)
- [GRASS GIS 6 on Fedora](https://grasswiki.osgeo.org/wiki/Compile_and_Install#GRASS_GIS_6_on_Fedora) (Used as reference for centos 7 singularity container.)
- [GRASS v6.5.svn Programmer's Manual](https://grass.osgeo.org/programming6/index.html) 

## Contributing

TODO: add details as/if required
