#!/bin/bash

# this is to automate adding the "simple" model to the x-file templates
# so we can try out the analogous varieties...


ceres_x_files=`ls whK0????.SNX`

for ceres_file_name in $ceres_x_files; do

  simple_file_name=`echo "$ceres_file_name" | sed "s/whK/whOsimple/g"`

  echo "$ceres_file_name -> $simple_file_name"

  sed "s/ 1 GE          nnnnn     1     S iiiiS rrrrr template simulation name/ 1 GE          nnnnn     1     S iiiiS rrrrr template simulation name  SIMPL/g ; s/@N GENERAL     NYERS NREPS START SDATE RSEED SNAME..................../@N GENERAL     NYERS NREPS START SDATE RSEED SNAME.................... SMODEL/g" $ceres_file_name > $simple_file_name


done




