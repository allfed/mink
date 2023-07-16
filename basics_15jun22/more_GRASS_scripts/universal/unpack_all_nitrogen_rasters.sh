#!/bin/bash

cd ../../../grassdata/world/nitrogen_maps/

script_folder=$1

echo ""
echo "unpacking nitrogen maps..."
echo ""

for pack in *.pack; do
    raster_name="${pack%.*}"  # remove the .pack extension

    if g.mlist -r type=rast pattern="$raster_name" --quiet; then
        g.remove -f "$raster_name" --quiet # use with caution!
    fi

    r.unpack input="$pack" output="$raster_name"

    git_root="$script_folder../.."
    sh $git_root/basics_15jun22/more_GRASS_scripts/universal/copy_over_f_format_for_nitrogen_if_missing.sh $git_root $raster_name

done

echo ""
echo "nitrogen maps unpacked"
echo ""
