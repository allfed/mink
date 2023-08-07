#!/bin/bash


. ../default_paths_etc.sh # imports nitrogen_directory



echo ""
echo "unpacking nitrogen maps..."
echo ""
cd "$nitrogen_directory"

for pack in *.pack; do
    cd "$nitrogen_directory"
    raster_name="${pack%.*}"  # remove the .pack extension
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
