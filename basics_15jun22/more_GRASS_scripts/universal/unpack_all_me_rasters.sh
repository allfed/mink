#!/bin/bash

cd ../../../grassdata/world/megaenvironments_packed/

echo $PWD

echo ""
echo "unpacking megaenvironments..."
echo ""

for pack in *.pack; do
    raster_name="${pack%.*}"  # remove the .pack extension

    if g.mlist -r type=rast pattern="$raster_name" --quiet; then
        g.remove -f "$raster_name"  # use with caution!
    fi

    r.unpack input="$pack" output="$raster_name"
done

echo ""
echo "megaenvironments unpacked"
echo ""
