#!/bin/bash

# this is to copy all the vectors out of one mapset into another


source_mapset=PERMANENT

type=rast

pattern="LdUseN*"

map_list=`g.mlist type=$type mapset=$source_mapset pattern="$pattern"`

echo "---begin list---"
echo "$map_list"
echo "----end list----"

read -p "proceed? if not, break out... " junk

n_maps=`echo "$map_list" | wc -l`

for (( num=1 ; num <= n_maps ; num++ ))
do
  name=`echo "$map_list" | sed -n "${num}p"`

  echo "      [$name] #${num} of $n_maps"
  g.copy $type=${name}@${source_mapset},${name}

done
