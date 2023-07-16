#!/bin/bash
region_to_use=$1

# quietly get rid of any masks
r.mask -r --q &> /dev/null

# set the region
g.region $region_to_use

echo ""
echo "Region: "
g.region -g
echo ""
