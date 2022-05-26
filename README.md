# ALLFED MINK

> A fully open-source global gridded crop model using DSSAT

This is a fully open source global gridded crop model using DSSAT. The top layer (mink) has been in development for over 10 years at IFPRI/CGIAR. There are several useful scripts enabling full control over the crop model process and modular runs, as well as several useful analysis and visualization tools. At ALLFED, we've taken that setup and put it online for anyone to use much more easily than if it were sitting on IFPRI's offline cluster. This crop model was built with the intention of understand the food production system and how it could be improved for the purposes of food security, and the development of tools to make the results more resilient.

## Install

- Clone this repository
- Build the mink container with `docker build -t mink .`.
- Optionally, create the singularity container, `mink.sif`, by running `build_singularity.sh`

### GRASS docker build install

For reference.

- Run `git submodule update` to clone the `grass` repository into the `GRASS_program` folder.
  - Note: Currently need change line 107 in `GRASS_program/grass/Dockerfile` to `--without-pdal \` to avoid dependency issue; see https://github.com/OSGeo/grass/issues/2400
- Build the GRASS and mink docker containers by running `install.sh`.

## Running

### Running with Docker

TODO

### Running with Singularity

TODO


## Development

### Upstream development of GRASS

The following isn't relevant if using the grass dockerhub image to build mink, it only matters if building from the grass repo Dockerfile.

Updates to the release verison of GRASS will result in it being installed in a different directory.
After building the GRASS docker container, the location of the install directory can be found by running `docker run -t grassgis ls /usr/local` and locating `grassXX` where `XX` is the version suffix, eg. `grass83`.
This directory needs to be updated in the `Dockerfile` by setting `ARG GRASS_PREFIX=/usr/local/grassXX`.
This will be passed as an environement variable to the modules files at install time.

### Debugging docker build

Start a root session in the docker container wth `docker run -it --rm --volume $(pwd):/data --env --user=$(id -u):$(id -g) HOME=/data/ mink /bin/bash`

### Debugging singularty build

Running as a sandox:

- Create sandox directory: `mkdir mink_sandbox`
- Build into that directory: `sudo singularity build --sandbox mink_sandbox mink.def`
- Run the sandbox as a writable image:  `sudo singularity shell --writable --bind $PWD:/mnt/data mink_sandbox`


## Reference

- [Singularity user guide](https://sylabs.io/guides/3.5/user-guide/index.html)
- [GRASS GIS 6 on Fedora](https://grasswiki.osgeo.org/wiki/Compile_and_Install#GRASS_GIS_6_on_Fedora) (Used as reference for centos 7 singularity container.
===================

## Setup summary
NOTE: see "grass git submodule setup", you probably don't have the grass code pulled in the repo yet.

The basic steps to run your own crop model with your climate data of interest are:

- Get access to a cluster that can run singularity and batch processing, preferably SLURM 
- If you don't have access, you could use an AWS server, although launching all the nodes can get expensive quickly.
- Install singularity
- Clone this repo
- Gather together the climate data, examples of the required format can be found in the []!!! document.

# Setup in more detail:
## Download DSSAT modules
     [document this]
## Download GRASS modules
### F git submodule setup
### git submodule setup
The GRASS_Program/grass repo and GRASS_dependencies/GDAL repos are git "submodules".

to get GRASS source code submodules to your local, you need to run:

$ git submodule init

to get the actual module, run:

$ git submodule update

now if you run:

$ git config -l

you should see the grass and GDAL programs there (somewhere in there, something like:
"submodule.GRASS_program/grass.url=https://github.com/OSGeo/grass.git
submodule.GRASS_program/grass.active=true
submodule.GRASS_dependencies/GDAL.url=https://github.com/OSGeo/GDAL.git
submodule.GRASS_dependencies/GDAL.active=true").

requirements (for grass):
    gdal3.0.4
    proj7.2.0
    [document this]

## Install and build Singularity

Singularity allows for everyone to be running the same version of linux, prevents versioning issues and means you can use "sudo" even on your shared HPC.

Useful resource: https://sdsc.edu/education_and_training/tutorials1/singularity_old.html

The singularity install instructions are here:
https://sylabs.io/guides/3.0/user-guide/installation.html

Build singularity image (pulls the default Docker centos7 repository):

#export SINGULARITY_BINDPATH="/data/$USER,/fdb,/lscratch"
#--bind $PWD:/mnt
#https://hpc.nih.gov/apps/singularity.html
$ sudo singularity build --sandbox centos7_img.sif centos7_def.def

Note: above command has several important installation steps required for crop modelling. You may need to alter the commands in the %post and %run sections of the definition if they are not working for you. If they aren't working, delete every line after "yum -y" and run 

$ sudo singularity shell --writable --bind $PWD:/mnt centos7_img.sif

if that worked and you're in the singularity directory, run

$ cd /mnt


This will allow you to interactively try each command to see where the problem arises without having to redo all the commands each container build. However, you need run these "one-by-one" commands in an environment where you have root priviledges

### Further singularity editing

By default, singularity doesn't have access to the files in the directory and needs to be mounted. To play around with this, navigat to the root of the git folder and try the command:

$ singularity shell --bind $PWD:/mnt centos7_img.sif

# Running the crop model

Run the script [? not sure yet which one to run].

Download and view the results back on your local machine.
    [more details go here...]

