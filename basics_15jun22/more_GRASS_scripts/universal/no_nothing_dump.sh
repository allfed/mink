#!/bin/bash



if [ $# -lt 2 ]; then
  echo "Usage: nice_dump.sh raster_name file_name [legend_width=%; must be third] [legend_options=string ; must be fourth]"

  exit 1
fi

# pull out the arguments
raster="$1"
file_name="$2"

third="$3"
fourth="$4"

legend_width=""
legend_options=""

#echo "LW = [$legend_width]"
#echo "LO = [$legend_options]"

echo "3 = ${third} ; ${third##legend_width=} (should be legend width)"
echo "4 = ${fourth} ; ${fourth##legend_options=} (should be legend options)"

if [ -n ${third} ]; then
  # we have a legend width
  legend_width=${third##legend_width=}
else
  legend_width=20
fi

#echo "LW = [$legend_width]"
#echo "LO = [$legend_options]"

if [ -n ${fourth} ]; then
  # we have a legend width
  legend_options=${fourth##legend_options=}
else
  legend_options=""
fi

#echo "LW = [$legend_width]"
#echo "LO = [$legend_options]"

# write down the monitor that is currently active so we can
# re-select it after we're done...

original_monitor=`d.mon -p | cut --delimiter=":" --fields=2 | cut --delimiter=" " --fields=2`


test_string=`d.mon -L | grep "PNG   " | grep "not running"`
if [ -z "$test_string" ]; then
  d.mon stop=PNG
fi

#export          GRASS_HEIGHT=600
#export           GRASS_WIDTH=800
export          GRASS_HEIGHT=800
export           GRASS_WIDTH=1500
#export           GRASS_WIDTH=1024
#export           GRASS_WIDTH=`echo "1024 * 43200 / 18000" | bc`
#export           GRASS_WIDTH=800
#export          GRASS_HEIGHT=600
export GRASS_PNG_COMPRESSION=9
export       GRASS_TRUECOLOR=TRUE

output_directory=~grass/grass_scripts/maps/quick/

output_path="${output_directory}${file_name}.png"
echo "    --> quick dumping to: [$output_path]"

export GRASS_PNGFILE=$output_path



d.mon start=PNG --q

d.erase


#d.frame -c blurb_area  at=90,100,0,100
d.frame -c legend_area at=0,100,0,$legend_width
d.frame -c map_area    at=0,100,$legend_width,100
#d.frame -c legend_area at=0,90,0,20
#d.frame -c map_area    at=0,90,20,100

### the maps
d.frame -s map_area
  # raster
  d.rast $raster

  # vectors
  d.vect cntry05 type=boundary color=black width=1
#  if [ -n "$vector" ]; then
#    eval d.vect $vector
#  fi


### the legend
d.frame -s legend_area
  if [ -n "$legend_options" ]; then
    eval d.legend $raster $legend_options
  else
    eval d.legend $raster $legend_options at=10,90,5,35
  fi


### finish out

d.mon stop=PNG --q

if [ $original_monitor != "monitor" ]; then
  d.mon select=$original_monitor
fi
