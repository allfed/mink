#!/bin/bash


####
# this script tries to make pretty post-script maps


usage_message()
{
  echo ""
  echo "Usage: $0 -r raster_name -f file_name -s sample_dpi [-t n_cols_in_legend] [-e]"
  echo ""
  echo "raster_name is the raster to be processed"
  echo "file_name is where to write the file out (no suffix)"
  echo "sample_dpi is the resolution to resample the map for a PNG version"
  echo "-t means assume categorical values (default = continuous)"
  echo "-e means try for EPS rather than PS"
  echo ""
}


if [ $# -lt 2 ]; then
  usage_message
  exit 1
fi

# initialize the value_type to continuous and the index to 1
OPTIND=1
value_type=cont
while getopts r:f:s:t:enx: provided_value
do
  case "$provided_value" in
    r) raster_name="$OPTARG";;
    f) file_name="$OPTARG";;
    s) sample_dpi="$OPTARG";;
    t) value_type="cat"; n_cols="$OPTARG" ;;
    e) eps_flag="-e";;
    n) no_name_flag="1";;
    x) fontsize="$OPTARG";; #echo "x:$OPTARG";;
    ?) usage_message;;
  esac
done

echo "
    r) $raster_name
    f) $file_name
    s) $sample_dpi
    t) $value_type $n_cols
    e) $eps_flag
    n) $no_name_flag
    x) $fontsize
"


####################

#output_directory=~grass/grass_scripts/maps/new/
output_directory=""

font_to_use=Times-Roman
bold_font_to_use=Times-Bold
#font_to_use=Helvetica
#font_to_use=Helvetica-Bold




instruction_file=${output_directory}deleteme_instructions.txt
    comment_file=deleteme_comments.txt


if [ $value_type = "cont" ]; then
  colortable_width_line="  width 0.15"
  colortable_cols_line=""
elif [ $value_type = "cat" ]; then
  colortable_width_line="";
  colortable_cols_line="cols $n_cols"
else
  colortable_width_line="";
  colortable_cols_line=""
fi


colortable_where="  where 0.10 0.25"


### let us put the units in a comment
#units=`r.info $raster_name -u`

#echo "${units#units=}" > $comment_file

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

maploc 0.15 2.5 8.2 10.7

border n

colortable y
  raster $raster_name
$colortable_where
$colortable_width_line
$colortable_cols_line
  height 1.50
  nodata n
  fontsize $fontsize
  font $font_to_use
  end
" > $instruction_file

junk="
comments $comment_file
  fontsize $fontsize
  font $font_to_use
  end
"




if [ -n "$eps_flag" ]; then
  output_name=${output_directory}${file_name}.eps
else
  output_name=${output_directory}${file_name}.ps
fi
# actually make the maps
echo ps.map $eps_flag $rotate_flag input=$instruction_file output=${output_name}
eval ps.map $eps_flag $rotate_flag input=$instruction_file output=${output_name}

echo "converting to PNG..."

# convert the post-script into a rotated and trimmed png
echo convert -density $sample_dpi ${output_name} $rotate_string -trim ${output_directory}${file_name}.png 
eval convert -density $sample_dpi ${output_name} $rotate_string -trim ${output_directory}${file_name}.png 







