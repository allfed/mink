#!/bin/bash

raster=$1
vector=$2

d.erase
if [ -n $raster ]; then
  d.rast $raster
  if [ -n "$vector" ]; then
    d.vect $vector type=boundary
  fi
  d.vect cntry05 type=boundary color=gray
#  d.legend $raster
fi

echo \
".C black
.S 3.0
$1" \
| d.text at=98,95 align=lr

