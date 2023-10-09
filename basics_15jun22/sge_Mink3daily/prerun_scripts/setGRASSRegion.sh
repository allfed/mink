#!/bin/bash
region_to_use=$1

# example usage: whole world:
# g.region n=65 s=-65 e=178 w=-170 nsres=1.875 ewres=1.25

# quietly get rid of any masks and previous region
r.mask -r --q &> /dev/null
g.region -d

# set the region
g.region $region_to_use

echo ""
echo "Region: "
g.region -g
echo ""
