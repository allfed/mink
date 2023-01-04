#!/bin/bash


# the idea is to search over al lthe planting months to see which month provides the best yield.

if [ $# -ne 3 ]; then
  echo "Usage: $0 calendar_A calendar_B output_raster"
  echo ""
  echo "The computation is effectively calendar_A - calendar_B."
  echo ""

  exit
fi

   calendar_A=$1
   calendar_B=$2
output_raster=$3


change_month_colors=\
"-5 magenta
-4 red
-3 orange
-2 yellow
-1 128:100:100
0 black
1 100:100:128
2 green
3 cyan
4 128:128:255
5 blue
6 purple
7 brown
8 0:0:90
9 50:0:0"


category_string=\
"
-5:$calendar_A is 5 months before $calendar_B
-4:-4
-3:-3
-2:-2
-1:1 month earlier
0:no change
1:1 month later
2:2
3:3
4:4
5:$calendar_A is 5 months after $calendar_B
6:complete inversion of growing season
7:one or both months is > 12
8:$calendar_A is missing
9:$calendar_B is missing
"

  # do a month comparison against one of them
  r.mapcalc $output_raster = " \
      eval(ccc = $calendar_A - $calendar_B, \
    if(isnull($calendar_A), \
      if(isnull($calendar_B), null(), 8), \
      if(isnull($calendar_B), 9, \
       if($calendar_A <= 12 && $calendar_B <= 12, \
       if(ccc > 6, ccc - 12, \
         if(ccc <= -6, ccc + 12, ccc) \
       ) \
         , 7 ) \
        ) \
       ) \
          )"

  echo "$change_month_colors" | r.colors $output_raster color=rules

#  assign_numeric_categories.sh $output_raster
  assign_categories_from_string.sh $output_raster "$category_string"









