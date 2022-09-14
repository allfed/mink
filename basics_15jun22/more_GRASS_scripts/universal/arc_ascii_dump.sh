#!/bin/bash

if [ $# -lt 3 ]; then

  echo "Usage: $0 output_directory mapset [pattern] [this]"
  echo ""
  echo "if mapset is \"\", it will use the current mapset"
  echo ""
  echo "optional \"this\" means keep the current region"
  echo "the default is to match the resolution and zoom"
  echo "to smallest smallest necessary region."

  exit
fi

output_directory=$1
  mkdir -p $output_directory # create the directory if it doesn't already exist


# define the mapset we care about
mapset_to_pull_from=$2

if [ -z "$mapset_to_pull_from" ]; then
  mapset_to_pull_from=`g.gisenv get=MAPSET`
fi

# define the pattern to search upon
pattern_to_search_for=$3

if [ -z "$pattern_to_search_for" ]; then
  pattern_to_search_for="*"
fi

# get a list of all the rasters beginning with "wintermix_best"
# Beware the MAGIC NUMBER!!! the search pattern....
raster_list=`g.mlist rast mapset=$mapset_to_pull_from pattern=$pattern_to_search_for`

this_flag=$4


#### shouldn't have to change anything below here ####


# go through the list and convert them to ascii files
for the_raster in $raster_list; do

  # dump something to the screen for impatient users
  echo "-- preparing to dump: $the_raster from mapset $mapset_to_pull_from --"

  # set the region

  if [ "$this_flag" = "this" ]; then
    echo "   keeping current region..."
  else
    g.region rast=${the_raster}@${mapset_to_pull_from}

    # shrink the region down to the minimum necessary
    g.region zoom=${the_raster}@${mapset_to_pull_from}  
  fi

  # finally, the real work! exporting to text....

  # decide on a cute output name
  output_name=${output_directory}/${the_raster}.asc

  # do the export, overwriting if necessary
  r.out.arc input=${the_raster}@${mapset_to_pull_from} output=$output_name --o

  # make a tiny preview
  PNG_monitor.sh ${output_name%.asc} 600,300
  quick_display.sh ${the_raster}@${mapset_to_pull_from}
  d.mon stop=PNG

done # the_raster loop

# leave a note for ourselves about what happened
# Beware the MAGIC NUMBER!!! notes file name
notes_file_name=${output_directory}/NOTES.txt

echo "---- a set of exports finishing at `date` ----" >> $notes_file_name
echo "run from directory: `pwd`" >> $notes_file_name
echo "using script: $0" >> $notes_file_name
echo "" >> $notes_file_name
echo "output_directory:      $output_directory" >> $notes_file_name
echo "mapset_to_pull_from:   $mapset_to_pull_from" >> $notes_file_name
echo "pattern_to_search_for: [$pattern_to_search_for]" >> $notes_file_name
echo "\"this\" flag:	[$this_flag]" >> $notes_file_name
echo "" >> $notes_file_name
echo "GRASS mapset/etc..." >> $notes_file_name
echo "`g.gisenv`" >> $notes_file_name
echo "" >> $notes_file_name
echo "last region used:" >> $notes_file_name
echo "`g.region -g`" >> $notes_file_name
echo "" >> $notes_file_name
echo "--- rasters exported from mapset [$mapset_to_pull_from] were:" >> $notes_file_name
echo "$raster_list" >> $notes_file_name
echo "" >> $notes_file_name
echo "" >> $notes_file_name

