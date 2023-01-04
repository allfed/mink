#!/bin/bash

color_string=\
"-1000 225:225:225
-1 225:225:225
0 50:50:50
30 50:50:50
31 130:130:130
59 130:130:130
60 blue
90 blue
91 cyan
120 cyan
121 green
151 green
152 yellow
181 yellow
182 orange
212 orange
213 red
243 red
244 magenta
273 magenta
274 purple
304 purple
305 brown
335 brown
336 black
366 black
1000 200:200:200"

month_names=\
"-1:None
0:January
31:February
60:March
91:April
121:May
152:June
182:July
213:August
244:September
274:October
305:November
336:December"

echo "$color_string" | r.colors $1 color=rules

#assign_categories_from_string.sh $1 "$month_names"



