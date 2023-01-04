#!/bin/bash

# this is an attempt to export points to a kml for viewing in google earth

# provide a usage statement
if [ $# = 0 ]; then
  echo "Usage: v.out.kml.points.sh point_vector output_file readable_name point_name_field [color:red/green/blue]"
  exit 1
fi

# pull out the arguments
vector_to_export=$1
output_file=$2
readable_name=$3
point_name_field=$4
color_scheme=$5

# process the color scheme
if [ -z "$color_scheme" ]; then
  color_scheme="red"
fi

echo "vector: $vector_to_export"
echo "output: $output_file"
echo "title:  $readable_name"
echo "names:  $point_name_field"
echo "color:  $color_scheme"

# some universal headers
preamble_A="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<kml xmlns=\"http://earth.google.com/kml/2.2\">
<Document>"

# the part where we put the human readable name
name_B="<name>${readable_name}</name>"

#### this is where you define the color styles and what not
#### the colors are in reverse order: alpha blue green red. alpha = transparency, typically
# some more universal stuff
preamble_C="<open>1</open>
<StyleMap id=\"msn_shaded_dot_red\">
  <Pair>
    <key>normal</key>
    <styleUrl>#sn_shaded_dot_red</styleUrl>
  </Pair>
  <Pair>
    <key>highlight</key>
    <styleUrl>#sh_shaded_dot_red</styleUrl>
  </Pair>
</StyleMap>

<StyleMap id=\"msn_shaded_dot_green\">
  <Pair>
    <key>normal</key>
    <styleUrl>#sn_shaded_dot_green</styleUrl>
  </Pair>
  <Pair>
    <key>highlight</key>
    <styleUrl>#sh_shaded_dot_green</styleUrl>
  </Pair>
</StyleMap>

<StyleMap id=\"msn_shaded_dot_blue\">
  <Pair>
    <key>normal</key>
    <styleUrl>#sn_shaded_dot_blue</styleUrl>
  </Pair>
  <Pair>
    <key>highlight</key>
    <styleUrl>#sh_shaded_dot_blue</styleUrl>
  </Pair>
</StyleMap>

<Style id=\"sn_shaded_dot_red\">
  <IconStyle>
    <color>ff0000ff</color>
    <scale>0.25</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ff0000ff</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

<Style id=\"sh_shaded_dot_red\">
  <IconStyle>
    <color>ff0000ff</color>
    <scale>0.5</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ff0000ff</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

<Style id=\"sn_shaded_dot_green\">
  <IconStyle>
    <color>ff00ff00</color>
    <scale>0.25</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ff00ff00</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

<Style id=\"sh_shaded_dot_green\">
  <IconStyle>
    <color>ff00ff00</color>
    <scale>0.5</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ff00ff00</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

<Style id=\"sn_shaded_dot_blue\">
  <IconStyle>
    <color>ffff0000</color>
    <scale>0.25</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ffff0000</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

<Style id=\"sh_shaded_dot_blue\">
  <IconStyle>
    <color>ffff0000</color>
    <scale>0.5</scale>
    <Icon>
      <href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>
    </Icon>
  </IconStyle>
  <LabelStyle>
    <color>ffff0000</color>
    <scale>0.7</scale>
  </LabelStyle>
</Style>

"

# another thing needing human readability
name_D="<Folder>
<name>${readable_name}</name>
<open>1</open>"

closing_Z="</Folder>
</Document>
</kml>"


### prepare all the point info for processing
echo "          -- extracting the info from vector"
coordinates_temp=/tmp/coordinates.txt
characteristics_temp=/tmp/characteristics.txt
together_temp=/tmp/together.txt

# clear out the temp files
rm $coordinates_temp $characteristics_temp

# get the geographic coordinates
v.out.ascii $vector_to_export output=$coordinates_temp fs="," format=point

# get the characteristics
v.db.select $vector_to_export fs="," -c > $characteristics_temp

# get the column names
char_names=`v.db.select $vector_to_export fs="," where="cat = 1" | head -n 1`

 magic_latitude_name=lat_g
magic_longitude_name=long_g
coord_names="$magic_longitude_name,$magic_latitude_name,cat_g,"

echo "${coord_names}${char_names}" > $together_temp

# put them together (the id #'s will be repeated as a simple check)
paste --delimiters="," $coordinates_temp $characteristics_temp >> $together_temp


##### start writing the file #####
echo "$preamble_A" >  $output_file
echo "$name_B"     >> $output_file
echo "$preamble_C" >> $output_file
echo "$name_D"     >> $output_file
echo ""            >> $output_file
echo ""            >> $output_file
echo ""            >> $output_file

### work through all the points...
n_lines_together=`cat $together_temp | wc -l`
n_points=`echo "$n_lines_together - 1" | bc`



header_names=`head -n 1 $together_temp`

# count up the number of fields
# this is done by counting the number of field names...
n_fields=0
magic_max_number_of_fields=20 # i don't want to display any morei
                              # than this even if they exist

for (( cand_field=1 ; cand_field <= magic_max_number_of_fields ; cand_field++ ))
do
  field_name=`echo "$header_names" | cut --delimiter="," --fields=$cand_field`
  if [ -n "$field_name" ]; then
    n_fields=`echo "$n_fields + 1" | bc`
  else
    break
  fi
done

#echo "headers=[$header_names]"
#echo "n_fields = $n_fields"

echo "          -- there are $n_points points with $n_fields characteristics"

# run through all the points
magic_update_interval=10
magic_update_divisor=`echo "$n_lines_together / $magic_update_interval" | bc`
for (( line_number=2 ; line_number <= n_lines_together; line_number++ ))
do
  test_string=`echo "$line_number % $magic_update_divisor" | bc`
  if [ "$test_string" = "0" ]; then
    echo "               -- starting line #${line_number} of $n_lines_together"
  fi

  # get the line corresponding to a point
  line_contents=`sed -n "${line_number}p" $together_temp`

  # initialize the description list...
  description_string=""

  for (( field_number=1 ; field_number <= n_fields ; field_number++ ))
  do
    field_name=`    echo "$header_names"  | cut --delimiter="," --fields=$field_number`
    field_contents=`echo "$line_contents" | cut --delimiter="," --fields=$field_number`

    if [ "$field_name" = "$point_name_field" ]; then
      name_of_this_point="$field_contents"
#      echo "PNF: $field_name / $field_contents"
    elif [ "$field_name" = "$magic_latitude_name" ]; then
      lat_of_this_point="$field_contents"
#      echo "LAT: $field_name / $field_contents"
    elif [ "$field_name" = "$magic_longitude_name" ]; then
      lon_of_this_point="$field_contents"
#      echo "LON: $field_name / $field_contents"
    else
#description_string=`echo -ne "${description_string}<br>${field_name}: ${field_contents}</br>\n"`
description_string=`echo -ne "${description_string}${field_name}: ${field_contents}\n"`
#      echo "ELS: $field_name / $field_contents"
    fi

  done # end for fields

  # write the stuff down...
  echo "<Placemark>"                                             >> $output_file
  echo "  <name>${name_of_this_point}</name>"                    >> $output_file
  echo "<Snippet/>"                                              >> $output_file
  echo "  <visibility>0</visibility>"                            >> $output_file
  echo "  <description>"                                         >> $output_file
  echo "${description_string}"                                   >> $output_file
  echo "  </description>"                                        >> $output_file
  echo "  <styleUrl>#msn_shaded_dot_${color_scheme}</styleUrl>"  >> $output_file
  echo "  <Point>"                                               >> $output_file
  echo "    <coordinates>${lon_of_this_point},${lat_of_this_point}</coordinates>" \
                                                                 >> $output_file
  echo "  </Point>"                                              >> $output_file
  echo "</Placemark>"                                            >> $output_file
  echo ""                                                        >> $output_file

done # end for line/point




# close out the file
echo ""            >> $output_file
echo ""            >> $output_file
echo ""            >> $output_file
echo "$closing_Z"  >> $output_file


exit 1



