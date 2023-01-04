#!/bin/bash

raster="$1"
file_name="$2"
blurb="$3"

number_4=$4
number_5=$5
number_6=$6

if [ $# -lt 3 ]; then
  echo "Usage: nice_dump.sh raster_name file_name \"descriptive blurb\" [legend_options=\"options\"] [\"vector [with options]\"] [legend_width=%]"
  echo "a common legend option for a continuous legend is: at=10,90,5,25"

  exit 1
fi

# test whether we got a "-s"

#before_equals_4=`echo "$number_4" | cut --delimiter="=" --fields=1`
#before_equals_5=`echo "$number_5" | cut --delimiter="=" --fields=1`
#before_equals_6=`echo "$number_6" | cut --delimiter="=" --fields=1`
before_equals_4=${number_4%%=*} # remove anything after the equals inclusive
before_equals_5=${number_5%%=*}
before_equals_6=${number_6%%=*}

if   [ "$before_equals_4" = "legend_options" ]; then
  legend_options=${number_4#*=} # strip out everything before the equals inclusive
#  legend_options=`echo "$number_4" | cut --delimiter="=" --fields=2`
  if [ "$before_equals_5" = "legend_width" ]; then
    # 5 is a width thing
    legend_width=${number_5#*=}
    # so 6 is either empty or a vector
    vector="$number_6"
  elif [ "$before_equals_6" = "legend_width" ]; then
    # 6 is a width thing
    legend_width=${number_6#*=}
    # so 5 is a vector
    vector="$number_5"
  fi
elif [ "$before_equals_4" = "legend_width" ]; then
  legend_width=${number_4#*=}
  if [ "$before_equals_5" = "legend_options" ]; then
    # 5 is an option thing
    legend_options=${number_5#*=}
    # so 6 is either empty or a vector
    vector="$number_6"
  elif [ "$before_equals_6" = "legend_options" ]; then
    # 6 is a width thing
    legend_options=${number_6#*=}
    # so 5 is a vector
    vector="$number_5"
  fi
elif [ -n "$number_4" ]; then
#  legend_width=`echo "$number_4" | cut --delimiter="=" --fields=2`
  vector="$number_4"
  if [ "$before_equals_5" = "legend_width" ]; then
    # 5 is a width thing
    legend_width=${number_5#*=}
    # so 6 is legend or empty
    legend_options=${number_6#*=}
  elif [ "$before_equals_6" = "legend_width" ]; then
    # 6 is a width thing
    legend_width=${number_6#*=}
    # so 5 is legend or empty
    legend_options=${number_5#*=}
  fi

else
  vector="$number_4"
fi

#echo "#4 = [$number_4] ; BE4 = [$before_equals_4]"
#echo "#5 = [$number_5] ; BE5 = [$before_equals_5]"
#echo "#6 = [$number_6] ; BE6 = [$before_equals_6]"
#
#echo "legend_options = [$legend_options]"
#echo "legend_width   = [$legend_width]"
#echo "vector         = [$vector]"
#
#
#exit 1

# write down the monitor that is currently active so we can
# re-select it after we're done...

original_monitor=`d.mon -p | cut --delimiter=":" --fields=2 | cut --delimiter=" " --fields=2`


test_string=`d.mon -L | grep "PNG   " | grep "not running"`
if [ -z "$test_string" ]; then
  d.mon stop=PNG
fi

#export          GRASS_HEIGHT=600
#export           GRASS_WIDTH=800
export          GRASS_HEIGHT=768
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


# figure out a reasonable grid to overlay
# we're going to ask for n_lines north to south
n_grid_lines=6

north_edge=`g.region -g | sed -n "1p" | cut --delimiter="=" --fields=2`
south_edge=`g.region -g | sed -n "2p" | cut --delimiter="=" --fields=2`

grid_size_raw=`echo "scale=5 ; ($north_edge - $south_edge) / $n_grid_lines" | bc`
# round to the nearest minute
grid_size_minutes=`echo "($grid_size_raw * 60)/1" | bc`
# convert back to degrees
grid_size_degrees=`echo "scale=12; $grid_size_minutes / 60" | bc`


d.mon start=PNG --q

d.erase

if [ -z "$legend_width" ]; then
  legend_width=20
fi

d.frame -c blurb_area  at=90,100,0,100
d.frame -c legend_area at=0,90,0,$legend_width
d.frame -c map_area    at=0,90,$legend_width,100
#d.frame -c legend_area at=0,90,0,20
#d.frame -c map_area    at=0,90,20,100

font_list=\
"cyrilc
gothgbt
gothgrt
gothitt
greekc
greekcs
greekp
greeks
italicc
italiccs
italict
romanc
romancs
romand
romans
romant
scriptc
scripts"

font_num=14 ; # CHOOSE THE FONT HERE !!!

font_name=`echo "$font_list" | sed -n "${font_num}p"`
#echo "font = [$font_name] #$font_num"

d.font font=$font_name

### figure out the units for this raster
these_units=`r.info -u $raster | cut --delimiter="=" --fields=2`

if [ -n "$these_units" ]; then
  unit_blurb=" ($these_units)"
else
  read -p "Enter units for this map: " entered_units
  if [ -n "$entered_units" ]; then
    unit_blurb=" ($entered_units)"
    echo "setting units for [$raster] to [$entered_units]"
    r.support $raster units="$entered_units"
  else
    unit_blurb=""
  fi
fi

### the maps
d.frame -s map_area
  # raster
  d.rast $raster

  # vectors
  d.vect cntry05 type=boundary color=gray
  if [ -n "$vector" ]; then
    eval d.vect $vector
  fi

  # grid
  d.grid size=$grid_size_degrees color=32:32:32 textcolor=black

### the legend
d.frame -s legend_area
  d.legend $raster $legend_options #at=10,90,5,25

### the blurbs
d.frame -s blurb_area

# figure out what mapset and location we're in
location=`g.gisenv get=LOCATION_NAME`
test_string=`echo "$raster" | grep "@"`
if [ -z "$test_string" ]; then
  mapset="@`g.gisenv get=MAPSET`"
else
  mapset=""
fi

# figure out the units for this raster
#these_units=`r.info -u $raster | cut --delimiter="=" --fields=2`
#
#if [ -n "$these_units" ]; then
#  unit_blurb=" ($these_units)"
#else
#  read -p "Enter units for this map: " entered_units
#  if [ -n "$entered_units" ]; then
#    unit_blurb=" ($entered_units)"
#  else
#    unit_blurb=""
#  fi
#fi

echo \
".C black
.S 30.0
$blurb
${raster}${mapset} [${location}]${unit_blurb}" \
| d.text at=50,70 align=cc 


### finish out

d.mon stop=PNG --q

if [ $original_monitor != "monitor" ]; then
  d.mon select=$original_monitor
fi
