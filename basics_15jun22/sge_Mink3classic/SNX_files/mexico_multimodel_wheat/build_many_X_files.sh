#!/bin/bash



IFS="
"


meta_file_list=\
"
whMETAceresRF.SNX
whMETAcropsimRF.SNX
whMETAnwheatRF.SNX
"


cultivar_list=\
"
IB0007 ME1-SeriM82
IB0008 ME1-PBW343
IB0009 ME2A-Kubsa
IB0010 ME2B-Tajan
IB0011 ME3-Alondra
IB0012 ME4A-Bacanora
IB0013 ME4B-DonErnesto
IB0014 ME4C-HI617
IB0015 ME5A-Kanchan
IB0016 ME5B-Debeira
IB0017 ME6-Saratovskay
IB0018 ME7-Pehlivan
IB0019 ME8A-HalconSNA
IB0020 ME8B-Katia
IB0022 ME10-Bezostaya
IB0023 ME11-Brigadier
IB0024 ME12-Gerek79
"

# run through each cultivar and fill in the blanks
# to get a fully functional x-file template

# define an infix to replace the META in the
# file name

infix=MXhardcoded

# initializat a counter for systematic naming
counter=1

for cultivar_line in $cultivar_list; do

  # extract the pieces
  cultivar_code=`echo "$cultivar_line" | cut -d" " -f1`  
  cultivar_name=`echo "$cultivar_line" | cut -d" " -f2`  

  for meta_file in $meta_file_list; do

    new_file_name=`echo "$meta_file" | sed "s/META/${infix}${counter}/g"`

    sed "s/VVVVVV/$cultivar_code/g ; s/NNNNNNNNNN/$cultivar_name/g" $meta_file > $new_file_name

  done # meta_file

  let "counter = counter + 1"

done # cultivar_line



