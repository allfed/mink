#!/bin/bash
if [ $# -eq 0 ]; then
  echo "Usage: $0 raster save_loc max_value"
  exit
fi

. ../default_paths_etc.sh

raster=$1
save_loc=$2
max_value=$3
min_value=$4
extra_description="${@:5}"


if [[ $save_loc == /* ]]; then
  location="${save_loc}"
elif [[ -z $save_loc ]]; then
  location="$git_root"
else
  location="$git_root/${save_loc}"
fi

# Create a unique temporary directory for this script instance
tmp_dir="$git_root/image_dump_${$}"
mkdir -p $tmp_dir

export GRASS_PNGFILE=${tmp_dir}/${raster}.png

raster_only=`echo "$raster" | cut -d"@" -f1`
mapset_only=`echo "$raster" | cut -d"@" -f2`
mapset_test=`echo "$raster" | grep "@"`

g.gisenv get=MAPSET

if [ -z "$mapset_test" ]; then

  mapset_only=`g.gisenv get=MAPSET`
fi

g.mlist rast mapset=$mapset_only pattern="$raster_only" 

raster_exists=`g.mlist rast mapset=$mapset_only pattern="$raster_only"`
# echo "raster_exists"
# echo $raster_exists
# #echo "RO = [$raster_only] MO = [$mapset_only] MT = [$mapset_test] RE = [$raster_exists]"

# echo $raster
if [ -n "$raster_exists" ]; then
# echo $raster

  #g.region raster=$raster -p
  
  if [ -z $max_value ]; then
    max=`r.univar -g map=$raster | grep max | awk -F "=" '{print $2}'`
  else  
    max=$max_value
  fi
 
# NOTE: might want to go back to the old way which actually prints things out
  if [ -z $min_value ]; then
    min=$(r.univar -g map=$raster | grep min | awk -F "=" '{print $2}')
  else
    min=$min_value
  fi
  # min=`r.univar -g map=$raster | grep min | awk -F "=" '{print $2}'`


  # Check if either min or max is empty
  if [ -z "$min" ] || [ -z "$max" ]; then
    echo "Min or max value is empty. Exiting..."
    exit 0
  fi

  d.erase --quiet
  echo "min-max"
  echo $min-$max

  # echo "colors"

  r.colors map=$raster color=rainbow

  # echo "done colors"


  d.rast $raster vallist=$min-$max #2>&1 | grep -v "PNG"

  # When using cp, use $tmp_dir instead of ../../../
  cp $GRASS_PNGFILE $tmp_dir/bg.png

  if [ -n "$vector" ]; then
    d.vect $vector type=boundary color=black --quiet
  fi
  d.vect cntry05 type=boundary bgcolor=none --quiet #| grep -v "PNG"
  cp $GRASS_PNGFILE $tmp_dir/vect.png

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
      eval d.legend map=$raster range=$min,$max at=1,50,2,5 --quiet
      #eval d.legend map=$raster at=1,50,2,5
  fi

cp $GRASS_PNGFILE $tmp_dir/legend.png
if [ -z "$extra_description" ]; then
echo \
".C black
.S 2.0
$raster" \
| d.text at=98,95 align=lr --quiet
cp $GRASS_PNGFILE $tmp_dir/text.png
else
echo \
".C black
.S 2.0
$raster : $extra_description" \
| d.text at=98,95 align=lr --quiet
cp $GRASS_PNGFILE $tmp_dir/text.png
fi

convert -composite -gravity center $tmp_dir/text.png $tmp_dir/bg.png $tmp_dir/resulttmp1.png | grep -v "PNG"
convert -composite -gravity center $tmp_dir/resulttmp1.png $tmp_dir/legend.png $tmp_dir/resulttmp2.png | grep -v "PNG"
convert -composite -gravity center $tmp_dir/resulttmp2.png $tmp_dir/vect.png $tmp_dir/resulttmp3.png | grep -v "PNG"
echo ""
echo location saving raster: 
echo $location/$raster.png
echo ""

convert $tmp_dir/resulttmp3.png -background white -flatten $location/$raster.png | grep -v "PNG"

if [ -d $tmp_dir ]; then
  rm -rf $tmp_dir
fi

else
  echo ""
  echo "${raster_only}@${mapset_only} has failed to exist"
  echo "cannot generate raster"
  echo ""
fi
