#!/bin/bash

. ../default_paths_etc.sh # imports megaenvironments_directory

cd "$megaenvironments_directory"


echo ""
echo "unpacking megaenvironments..."
echo ""
for pack in *.pack; do
    raster_name="${pack%.*}"  # remove the .pack extension

    if g.mlist -r type=rast pattern="$raster_name" --quiet; then
        g.remove -f "$raster_name"  # use with caution!
    fi

    # create the raster
    r.unpack input="$pack" output="$raster_name"
done

echo ""
echo "megaenvironments unpacked"
echo ""
