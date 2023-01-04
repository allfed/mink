#!/bin/bash


####
# this script tries to make pretty post-script maps


usage_message()
{
  echo ""
  echo "Usage: $0 -r raster_name [-v vector_names_csv] -f file_name -s sample_dpi [-l] [-e]"
  echo ""
  echo "raster_name is the raster to be processed"
  echo "vector_names_csv is a list of boundary vectors (first on list will be on top)"
  echo "file_name is where to write the file out (no suffix)"
  echo "sample_dpi is the resolution to resample the map for a PNG version"
  echo "-l means output as landscape"
  echo "-e means try for EPS rather than PS"
  echo ""
}


if [ $# -lt 3 ]; then
  usage_message
  exit 1
fi

# initialize the value_type to continuous and the index to 1
OPTIND=1
value_type=cont
landscape_flag=0
vector_names_csv="" # default...
no_name_flag=0
while getopts r:c:v:f:s:lt:en provided_value
do
  case "$provided_value" in
    r) raster_name="$OPTARG";;
    c) raster_comment="$OPTARG";;
    v) vector_names_csv="$OPTARG";;
    f) file_name="$OPTARG";;
    s) sample_dpi="$OPTARG";;
    l) landscape_flag=1;;
    t) value_type="cat"; n_cols="$OPTARG" ;;
    e) eps_flag="-e";;
    n) no_name_flag="1";;
    ?) usage_message;;
  esac
done

echo "
    r) $raster_name
    c) $raster_comment
    v) $vector_names_csv
    f) $file_name
    s) $sample_dpi
    l) $landscape_flag
    t) $value_type $n_cols
    e) $eps_flag
    n) $no_name_flag
"


####################

#output_directory=~grass/grass_scripts/maps/new/
output_directory=""

font_to_use=Times-Roman
bold_font_to_use=Times-Bold
#font_to_use=Helvetica
#font_to_use=Helvetica-Bold

default_color_list=\
"0:0:0
128:128:128
128:0:0
128:128:0
0:128:0
0:128:128
0:0:128
128:0:128"


rotate_string=""
if [ $landscape_flag -eq 1 ]; then
  rotate_flag="-r"
  rotate_string="-rotate 90"
fi


instruction_file=${output_directory}deleteme_instructions.txt


#echo "   map size is determined by horizontal dimension..."

### build up vector boundaries commands
v_commands=""

delimiter=,
n_vectors=`echo "$vector_names_csv" | awk -F "$delimiter" '{ print NF }'`

for (( vector_num=1 ; vector_num <= n_vectors ; vector_num++ ))
do
v_name=`echo "$vector_names_csv" | cut -d, -f${vector_num}`
v_color=`echo "$default_color_list" | sed -n "${vector_num}p"`

v_commands=\
"${v_commands}
vlines $v_name
  type boundary
  color 254:254:254
  width 0.001
  end

"

done



if [ -n "$rotate_flag" ]; then
### instructions for landscape
echo "
paper
  height 11.0
  width 11.0
  right 0
  left 0
  bottom 0
  top 0
  end

$v_commands

raster $raster_name

maploc 0.15 0.15 10.70 10.85

border y
  color 250:250:250
  width 0.1
  end


" > $instruction_file

else
### instructions for portrait

echo "
paper
  height 11.0
  width 8.5 
  right 0
  left 0
  bottom 0
  top 0
  end

$v_commands

raster $raster_name

maploc 0.15 0.15 8.2 10.7

border y
  color 250:250:250
  width 0.1
  end


" > $instruction_file

fi # end of landscape/portrait

if [ -n "$eps_flag" ]; then
  output_name=${output_directory}${file_name}.eps
else
  output_name=${output_directory}${file_name}.ps
fi
# actually make the maps
#echo ps.map $eps_flag $rotate_flag input=$instruction_file output=${output_name}
eval ps.map $eps_flag $rotate_flag input=$instruction_file output=${output_name}

echo "converting to PNG..."

# convert the post-script into a rotated and trimmed png
#echo convert -density $sample_dpi ${output_name} $rotate_string -trim ${output_directory}${file_name}.png 
#eval convert -density $sample_dpi ${output_name} $rotate_string -trim ${output_directory}${file_name}.png 
eval convert -density $sample_dpi ${output_name}  -trim ${output_directory}${file_name}.png 







