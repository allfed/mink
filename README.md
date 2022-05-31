# MINK

> A fully open-source global gridded crop model using DSSAT.

The top layer (mink) has been in development for over 10 years at IFPRI/CGIAR. There are several useful scripts enabling full control over the crop model process and modular runs, as well as several useful analysis and visualization tools. At ALLFED, we've taken that setup and put it online for anyone to use much more easily than if it were sitting on IFPRI's offline cluster. This crop model was built with the intention of understand the food production system and how it could be improved for the purposes of food security, and the development of tools to make the results more resilient.

## Quick Start

You can access the pre-built Singularity container here ...

TODO: add details of where to get the complied container, if it's made available publically

... otherwise, you can build the container yourself using the documentation below.

## Dependencies

MINK leverages Singularity to create a containerised environment from which to run the model.
Install singularity by following the [Singularity Installation instructions](https://sylabs.io/guides/3.0/user-guide/installation.html).

Dependencies for the build are fully handled in the Singularity container and can be viewed in ~mink.def~. 
For reference, however, MINK depends on the [GRASS v6.5 development branch](https://svn.osgeo.org/grass/grass/branches/develbranch_6/).

## Build

To build the MINK singularity container:

- Clone this repository.
- Build the singularity container by running `build_singularity.sh`. This will build the `mink.sif` singularity container.
- TODO: details of the DSSAT modules if required

## Running

To run MINK use `singularity exec -B ~/.Xauthority mink.sif grass` and hit ENTER.

TODO: add more details and a toy dataset?


## Troubleshooting

### ~/.Xauthority issues

If this file doesn't exist, the dedicated location may be stored in the `$XAUTHORITY` variable and `-B $XAUTHORITY` must be used instead ([reference](https://pawseysc.github.io/singularity-containers/42-x11-gnuplot/index.html)).


## Development

### Debugging singularty build

Running as a sandox:

- Create sandox directory: `mkdir mink_sandbox`
- Build into that directory: `sudo singularity build --sandbox mink_sandbox mink.def`
- Run the sandbox as a writable image:  `sudo singularity shell --writable --bind $PWD:/mnt/data mink_sandbox`

This will allow you to interactively try each command to see where the problem arises without having to redo all the commands each container build. However, you need run these "one-by-one" commands in an environment where you have root priviledges

## Reference

- [Singularity user guide](https://sylabs.io/guides/3.5/user-guide/index.html)
- [GRASS GIS 6 on Fedora](https://grasswiki.osgeo.org/wiki/Compile_and_Install#GRASS_GIS_6_on_Fedora) (Used as reference for centos 7 singularity container.)
- [GRASS v6.5.svn Programmer's Manual](https://grass.osgeo.org/programming6/index.html) 

## Contributing

TODO: add details as/if required
