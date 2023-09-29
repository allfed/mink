#!/bin/bash

set -e

. ../default_paths_etc.sh # imports nitrogen_directory

crop_name=$1
if [[ "$crop_name" == "rapeseed" ]]; then
    crop_name="wheat" # we assume rapeseed gets the wheat nitrogen maps
    echo "NOTE: rapeseed is assumed to get the wheat nitrogen maps"
fi

echo ""
echo "unpacking nitrogen maps..."
echo ""
cd "$nitrogen_directory"

for pack in *.pack; do
    cd "$nitrogen_directory"
    raster_name="${pack%.*}"  # remove the .pack extension

    # Check if the first argument is passed and it's not in the raster_name
    if [[ -n "$crop_name" && "$raster_name" != *"$crop_name"* ]]; then
        continue
    fi

    if g.mlist -r type=rast pattern="$raster_name" --quiet; then
        g.remove -f "$raster_name" --quiet # use with caution!
    fi

    r.unpack input="$pack" output="$raster_name"

    cd $prerun_scripts
    
    ./copy_over_f_format_for_nitrogen_if_missing.sh $raster_name
done

echo ""
echo "nitrogen maps unpacked"
echo ""
