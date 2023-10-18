#!/bin/bash


if [ $# -eq 0 ]; then
  echo "Usage: $0 raster save_loc max_value"
  exit
fi

. ../default_paths_etc.sh

raster=$1
save_loc=$2
max_value=$3
min_value=$4
extra_description="${@:5}"

if [[ $save_loc == /* ]]; then
  location="${save_loc}"
elif [[ -z $save_loc ]]; then
  location="$git_root"
else
  location="$git_root/${save_loc}"
fi

# Create a unique temporary directory for this script instance
tmp_dir="$git_root/image_dump_${RANDOM}_`date +%N`"

mkdir -p $tmp_dir
export GRASS_PNGFILE=${tmp_dir}/${raster}.png



check_raster_exists() {
  raster_only=`echo "$raster" | cut -d"@" -f1`
  mapset_only=`echo "$raster" | cut -d"@" -f2`
  mapset_test=`echo "$raster" | grep "@"`

  if [ -z "$mapset_test" ]; then
    mapset_only=`g.gisenv get=MAPSET`
  fi

  raster_exists=`g.mlist rast mapset=$mapset_only pattern="$raster_only"`

  if [ -z "$raster_exists" ]; then
      echo ""
      echo "Raster ${raster_only}@${mapset_only} does not exist."
      echo "Cannot generate raster."
      echo ""
      exit 1
  fi

}

apply_color_value() {

  # Erase the contents of the active display frame with user defined color
  d.erase --quiet

  local overall_max_int="$1"
  local raster="$2"
  # calculate the different values for the colors
  local quartermax=`echo "scale=0; $overall_max_int / 4" | bc`
  local halfmax=`echo "scale=0; $overall_max_int / 2" | bc`
  local threequartersmax=`echo "scale=0; ($overall_max_int * 3 + 2) / 4" | bc`

  # corresponds to -2,  magicValueToUseWithBadMaturities in Mink3p2daily.java
  # this means maturity time was crazy or negative.
  local dark_grey="25:25:25" 

  # corresponds to -1 which means magicValueToUseWithNegativeYields in Mink3p2daily.java
  # probably having to do with -99 yields, meaning "bad weather"
  local lighter_grey="50:50:50" 
  # construct the color rule string
  local classic_color_string="-2 $dark_grey\n-1 $lighter_grey\n0 black\n1 blue\n$quartermax cyan\n$halfmax green\n$threequartersmax yellow\n$overall_max_int red"
  # apply the color rule to the raster
  # remove a useless warning message
  printf -- "$classic_color_string" | r.colors --quiet $raster rules=- 2>&1 | grep -v "Color table of raster map"
  
}




# BEGIN FUNCTIONS TO GENERATE THE IMAGES


generate_raster_image() {
  local raster="$1"
  local min="$2"
  local max="$3"
  
  d.rast --quiet $raster vallist=$min-$max
  cp $GRASS_PNGFILE $tmp_dir/heatmap.png
}

generate_vector_image() {
  d.vect cntry05 type=boundary bgcolor=none --quiet
  cp $GRASS_PNGFILE $tmp_dir/vect.png
}

generate_legend_image() {
  local raster="$1"
  local min="$2"
  local max="$3"
  raster_type=`r.info -t $raster | cut -d= -f2`

  if [ $raster_type = "CELL" ]; then
    n_categories=`r.category $raster | wc -l`
    if [ $n_categories -lt 12 ]; then
      eval d.legend --quiet map=$raster range=$min,$max at=1,50,2,5 2>&1 | grep -v "Color range exceeds"
    else
      eval d.legend --quiet map=$raster range=$min,$max at=1,80,2,10 2>&1 | grep -v "Color range exceeds"
    fi
  else
    eval d.legend --quiet map=$raster range=$min,$max at=1,50,2,5 2>&1 | grep -v "Color range exceeds"
  fi
  
  cp $GRASS_PNGFILE $tmp_dir/legend.png
}

generate_text_image() {

  local raster="$1"
  local min="$2"
  local extra_description="$3"
  if [ -z "$extra_description" ]; then
    # show the raster name as text at the top
    echo -e ".C black\n.S 2.0\n$raster" \
    | d.text at=98,95 align=lr --quiet
    cp $GRASS_PNGFILE $tmp_dir/text.png
  else
    # show the raster name as text at the top
    # add the extra description as additional text to the displayed image, if description is specified
    echo -e ".C black\n.S 2.0\n$raster : $extra_description" \
    | d.text at=98,95 align=lr --quiet
    cp $GRASS_PNGFILE $tmp_dir/text.png
  fi

  # zero is included in the image, include the note
  if [ $min -lt 1 ]; then
    # add a note about zero values at bottom left
    echo -e ".C black\n.S 2.0\nNote: black or grey is exactly zero" \
    | d.text at=40,2 align=lr --quiet
    cp $GRASS_PNGFILE $tmp_dir/note.png
  fi


}

merge_images() {
  local min="$1"
  # Create composite image with text and heatmap
  convert -composite -gravity center $tmp_dir/text.png $tmp_dir/heatmap.png $tmp_dir/resulttmp1.png | grep -v "PNG"

  # zero values are included
  if [ $min -lt 1 ]; then
    # Add the note about zero to the composite image with the heatmap
    convert -composite -gravity south $tmp_dir/resulttmp1.png $tmp_dir/note.png $tmp_dir/resulttmp1_1.png | grep -v "PNG"
    text_image_created=resulttmp1_1
  else
    text_image_created=resulttmp1
  fi



  # Create composite image with text, heatmap, and legend
  convert -composite -gravity center $tmp_dir/$text_image_created.png $tmp_dir/legend.png $tmp_dir/resulttmp2.png | grep -v "PNG"

  # Create composite image with text, heatmap, legend, and regional map outline
  convert -composite -gravity center $tmp_dir/resulttmp2.png $tmp_dir/vect.png $tmp_dir/resulttmp3.png | grep -v "PNG"

  convert $tmp_dir/resulttmp3.png -background white -flatten $location/$raster.png | grep -v "PNG"
}

generate_images() {
    # Determine min and max values
    if [ -z $max_value ]; then
      max=`r.univar -g map=$raster | grep max | awk -F "=" '{print $2}'`
    else  
      max=$max_value
    fi

    if [ -z $min_value ]; then
      min=$(r.univar -g map=$raster | grep min | awk -F "=" '{print $2}')
    else
      min=$min_value
    fi

    # Check if either min or max is empty
    if [ -z "$min" ] || [ -z "$max" ]; then
      echo "Min or max value is empty. Exiting..."
      exit 0
    fi

    # Generate various images
    apply_color_value $max $raster
    generate_raster_image $raster $min $max
    generate_vector_image

    # This will round the value of min to the nearest whole number.
    min=$(printf "%.0f" $min)

    # negative mins don't show on the legend
    if [ $min -lt 0 ]; then
      legend_min=0
    else
      legend_min=$min
    fi


    generate_legend_image $raster $legend_min $max
    generate_text_image $raster $min "$extra_description"
    merge_images $min

}

# END FUNCTIONS GENERATING IMAGES

display_output_and_cleanup() {
  echo ""
  echo location saving raster: 
  echo $location/$raster.png
  echo ""

  if [ -d $tmp_dir ]; then
    rm -rf $tmp_dir
  fi
}

main() {
  check_raster_exists
  generate_images
  display_output_and_cleanup
}

# call all the functions that use the variables initially defined
main
