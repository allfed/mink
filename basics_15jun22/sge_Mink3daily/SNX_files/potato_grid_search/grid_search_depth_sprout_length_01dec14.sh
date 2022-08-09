#!/bin/bash

# this should build some SNX-file templates for a gridsearch across some interesting planting parameters for potatoes.
# basically, we have no idea what is going on, so when in doubt: gridsearch!

IFS="
"


original_template=russet_burbank_template_RF.SNX
#original_template=russet_burbank_template_IR.SNX

output_prefix=rbgrid_
 RF_IR_suffix=_RF.SNX

        depth_key=depth
sprout_length_key=sprou


# don't forget to make them five characters...
depth_list=\
"
00004
00006
00008
00010
00012
"

# don't forget to make them five characters...
sprout_length_list=\
"
00002
00005
00008
00011
"


################################

counter=0

for depth in $depth_list; do
for sprout_length in $sprout_length_list; do

  sed "s/${depth_key}/${depth}/g ; s/${sprout_length_key}/${sprout_length}/g" $original_template > ${output_prefix}d${depth}_s${sprout_length}${RF_IR_suffix}

done
done




