#!/bin/bash

color_string=\
"-1 225:225:225
0 225:225:225
1 50:50:50
2 130:130:130
3 blue
4 cyan
5 green
6 yellow
7 orange
8 red
9 magenta
10 purple
11 brown
12 black
13 200:200:200"

month_names=\
"-1:None
0:None
1:January
2:February
3:March
4:April
5:May
6:June
7:July
8:August
9:September
10:October
11:November
12:December
13:All"

echo "$color_string" | r.colors $1 color=rules

assign_categories_from_string.sh $1 "$month_names"



