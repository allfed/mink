#!/bin/bash

raster="$1"
file_name="$2"
blurb="$3"

number_4=$4
number_5=$5
number_6=$6

if [ $# -lt 2 ]; then
  echo "Usage: simple_dump.sh raster_name file_name [\"vector [with options]\""
  echo "this is a simplified version of simple_dump.sh raster_name file_name \"descriptive blurb\" [legend_options=\"options\"] [\"vector [with options]\"] [legend_width=%]"
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

export          GRASS_HEIGHT=90 # 60 works well for world-wide (climate1km)
export           GRASS_WIDTH=240 # 160 works well for world-wide (climate1km)
#export           GRASS_WIDTH=800
#export          GRASS_HEIGHT=600
export GRASS_PNG_COMPRESSION=9
export       GRASS_TRUECOLOR=TRUE

output_directory=""

output_path="${output_directory}${file_name}_thumb.png"
echo "    --> quick dumping to: [$output_path]"

export GRASS_PNGFILE=$output_path


d.mon start=PNG --q

d.erase

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

### the maps
  # raster
  d.rast $raster

  # vectors
#  d.vect cntry05 type=boundary color=gray
  if [ -n "$vector" ]; then
    d.vect $vector
  fi

### finish out

d.mon stop=PNG --q

if [ $original_monitor != "monitor" ]; then
  d.mon select=$original_monitor
fi
