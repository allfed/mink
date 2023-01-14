#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster [legend_options] [vector]"
  echo ""
  echo "Use an empty string (\"\") for legend_options if you don't care, but still want a special vector"

  exit
fi


raster=$1
#vector=$2
save_loc=$2
legend_options="$3"
vector=$4

raster_only=`echo "$raster" | cut -d"@" -f1`
mapset_only=`echo "$raster" | cut -d"@" -f2`
mapset_test=`echo "$raster" | grep "@"`

g.gisenv get=MAPSET

if [ -z "$mapset_test" ]; then

  mapset_only=`g.gisenv get=MAPSET`
fi

g.mlist rast mapset=$mapset_only pattern="$raster_only"

raster_exists=`g.mlist rast mapset=$mapset_only pattern="$raster_only"`
echo "raster_exists"
echo $raster_exists
# #echo "RO = [$raster_only] MO = [$mapset_only] MT = [$mapset_test] RE = [$raster_exists]"

if [ -n "$raster_exists" ]; then
echo $raster

  d.erase
  d.rast $raster 
  cp ../../../grass6out.png ../../../bg.png
  if [ -n "$vector" ]; then
    d.vect $vector type=boundary color=black
  fi
  d.vect cntry05 type=boundary bgcolor=none
  cp ../../../grass6out.png ../../../vect.png

  # first change since feb 4, 2011 (30sep15)
  raster_type=`r.info -t $raster | cut -d= -f2`
  if [ $raster_type = "CELL" ]; then
    n_categories=`r.category $raster | wc -l`

    if [ $n_categories -lt 12 ]; then
      eval d.legend map=$raster $legend_options at=1,50,2,5
    else
      # make a bigger legend
      eval d.legend map=$raster $legend_options at=1,80,2,10
    fi
  else
    # not a categorical map
      eval d.legend map=$raster $legend_options at=1,50,2,5
  fi

cp ../../../grass6out.png ../../../legend.png
echo \
".C black
.S 2.0
$1" \
| d.text at=98,95 align=lr
cp ../../../grass6out.png ../../../text.png

convert -composite -gravity center ../../../text.png ../../../bg.png ../../../resulttmp1.png
convert -composite -gravity center ../../../resulttmp1.png ../../../legend.png ../../../resulttmp2.png
convert -composite -gravity center ../../../resulttmp2.png ../../../vect.png ../../../resulttmp3.png
convert ../../../resulttmp3.png -background white -flatten ../../../$save_loc/$raster.png
rm ../../../legend.png
rm ../../../bg.png
rm ../../../text.png
rm ../../../vect.png
rm ../../../grass6out.png
rm ../../../resulttmp1.png
rm ../../../resulttmp2.png
rm ../../../resulttmp3.png

else
  echo "${raster_only}@${mapset_only} has failed to exist"
fi
