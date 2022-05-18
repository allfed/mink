This is a fully open source global gridded crop model using DSSAT. The top layer (mink) has been in development for over 10 years at IFPRI/CGIAR. There are several useful scripts enabling full control over the crop model process and modular runs, as well as several useful analysis and visualization tools. At ALLFED, we've taken that setup and put it online for anyone to use much more easily than if it were sitting on IFPRI's offline cluster. This crop model was built with the intention of understand the food production system and how it could be improved for the purposes of food security, and the development of tools to make the results more resilient.

# Setup summary
The basic steps to run your own crop model with your climate data of interest are:
    Get access to a cluster that can run singularity and batch processing, preferably SLURM 
        If you don't have access, you could use an AWS server, although launching all the nodes can get expensive quickly.
    Install singularity
    Clone this repo
    Gather together the climate data, examples of the required format can be found in the []!!! document.

# Setup in more detail:

## Download DSSAT modules
     [document this]
## Download GRASS modules
     [document this]

## Install and build Singularity

Singularity allows for everyone to be running the same version of linux, prevents versioning issues and means you can use "sudo" even on your shared HPC.

requirements (for grass):
gdal3.0.4
proj7.2.0

The singularity install instructions are here:
https://sylabs.io/guides/3.0/user-guide/installation.html

Build singularity image (pulls the default Docker centos7 repository):

$ sudo singularity build centos7_img.sif centos7_def.def

Note: above command has several important installation steps required for crop modelling. You may need to alter the commands in the %post and %run sections of the definition if they are not working for you. If they aren't working, delete every line after "yum -y" and run 

$ sudo singularity shell centos7_img.sif

This will allow you to interactively try each command to see where the problem arises without having to redo all the commands each container build. However, you need run these "one-by-one" commands in an environment where you have root priviledges

# Running the crop model

Run the script [? not sure yet which one to run].

Download and view the results back on your local machine.
    [more details go here...]