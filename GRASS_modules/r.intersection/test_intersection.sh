#!/bin/bash

g.region n=90 s=-90 w=-180 e=180 res=30


# let's make a northern hemisphere

r.mapcalc deleteme_NH = "if(y() > 0, 1, null())"
r.mapcalc deleteme_EH = "if(x() > 45, 1, null())"
r.mapcalc deleteme_stripe = "if(row()%2 == 0, 1, null())"

echo "-- the intersection attempt --"
# let's just try the intersector and see what it does
/PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.intersection \
    input=deleteme_EH,deleteme_NH,deleteme_stripe \
   output=deleteme_zz_intersection \
    --o

#    input=deleteme_NH,deleteme_EH \



