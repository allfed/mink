#!/bin/bash

memory_string="-mx3800M"

input_data_to_match=cells_to_match_tabbed.txt
table_of_all_HC_codes=~grass/grass_scripts/crop_modeling/one_degree_table
output_file=power_stuff_out_totals
header_line=false

#java_path=/state/partition1/ManuallyInstalledJava/jre1.6.0_02/bin/java
java_path=/usr/PROJECTS/java_jre/jre1.6.0_07/bin/java

$java_path "$memory_string" -cp /home/grass/small_java_programs/IFPRIconverter/bin org.ifpri_converter.MagicHCgridMatcherForSoilProfiles $input_data_to_match $table_of_all_HC_codes $output_file $header_line

echo "finished at `date`"



