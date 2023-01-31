#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Usage: $0 raster save_loc max_value"

  exit
fi


raster=$1
#vector=$2
save_loc=$2
max_value=$3
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

  #g.region raster=$raster -p
  
  if [ -z $max_value ]; then
    max=`r.univar -g map=$raster | grep max | awk -F "=" '{print $2}'`
  else  
    max=$max_value
  fi
 
  min=`r.univar -g map=$raster | grep min | awk -F "=" '{print $2}'`

  echo " min and max of all cells"
  echo $min
  echo $max

  d.erase
  d.rast $raster vallist=$min-$max 
echo "colors"

#r.colors map=$raster color=bcyr

echo "done colors"
  cp ../../../grass6out.png ../../../$save_loc/bg.png
  if [ -n "$vector" ]; then
    d.vect $vector type=boundary color=black
  fi
  d.vect cntry05 type=boundary bgcolor=none
  cp ../../../grass6out.png ../../../$save_loc/vect.png

  # first change since feb 4, 2011 (30sep15)
  raster_type=`r.info -t $raster | cut -d= -f2`
  if [ $raster_type = "CELL" ]; then
    n_categories=`r.category $raster | wc -l`

    if [ $n_categories -lt 12 ]; then
      eval d.legend map=$raster range=$min,$max at=1,50,2,5
      #eval d.legend map=$raster at=1,50,2,5
    else
      # make a bigger legend
      eval d.legend map=$raster range=$min,$max at=1,80,2,10
      #eval d.legend map=$raster at=1,80,2,10
    fi
  else
    # not a categorical map
      eval d.legend map=$raster range=$min,$max at=1,50,2,5
      #eval d.legend map=$raster at=1,50,2,5
  fi

cp ../../../grass6out.png ../../../$save_loc/legend.png
echo \
".C black
.S 2.0
$1" \
| d.text at=98,95 align=lr
cp ../../../grass6out.png ../../../$save_loc/text.png

convert -composite -gravity center ../../../$save_loc/text.png ../../../$save_loc/bg.png ../../../$save_loc/resulttmp1.png
convert -composite -gravity center ../../../$save_loc/resulttmp1.png ../../../$save_loc/legend.png ../../../$save_loc/resulttmp2.png
convert -composite -gravity center ../../../$save_loc/resulttmp2.png ../../../$save_loc/vect.png ../../../$save_loc/resulttmp3.png
convert ../../../$save_loc/resulttmp3.png -background white -flatten ../../../$save_loc/$raster.png
rm ../../../$save_loc/legend.png
rm ../../../$save_loc/bg.png
rm ../../../$save_loc/text.png
rm ../../../$save_loc/vect.png
rm ../../../$save_loc/resulttmp1.png
rm ../../../$save_loc/resulttmp2.png
rm ../../../$save_loc/resulttmp3.png

else
  echo "${raster_only}@${mapset_only} has failed to exist"
fi
