#!/bin/bash

if [ $# = 0 ]; then

  echo "Usage: $0 two_letter_crop_code [part of model name]"
  echo ""
  echo "This will do some comparisons against the older versions of DSSAT to see how different they are."
  echo "You can optionally put in a model name when a crop has multiple models"
  exit
fi


# rename the input
two_letter_crop=$1
   model_extras=$2

#old_dssat_dir=~rdrobert/sge_Mink2daily/actual_program_4.5/
old_dssat_dir=~rdrobert/sge_Mink3classic/testor_program_4.5/

if [ $two_letter_crop = WH ]; then

  old_wheat_dssat_dir=~rdrobert/sge_Mink2daily/actual_program_4.5_wheat/

fi


echo ""
echo ""
echo ""
echo ""
#ls -l ${two_letter_crop}*${model_extras}*.CUL ${old_dssat_dir}${two_letter_crop}*${model_extras}*.CUL
echo "-- $two_letter_crop : CUL --"
diff ${two_letter_crop}*${model_extras}*.CUL ${old_dssat_dir}${two_letter_crop}*${model_extras}*.CUL

echo ""
echo ""
echo ""
echo ""
echo "-- $two_letter_crop : ECO --"
diff ${two_letter_crop}*${model_extras}*.ECO ${old_dssat_dir}${two_letter_crop}*${model_extras}*.ECO

echo ""
echo ""
echo ""
echo ""
echo "-- $two_letter_crop : SPE --"
diff ${two_letter_crop}*${model_extras}*.SPE ${old_dssat_dir}${two_letter_crop}*${model_extras}*.SPE




