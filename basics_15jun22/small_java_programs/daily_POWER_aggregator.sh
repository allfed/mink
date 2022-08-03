#!/bin/bash

memory_string="-mx3800M"

 input_file=combined_POWER_stuff.txt
output_file=cells_to_match.txt

#java_path=/state/partition1/ManuallyInstalledJava/jre1.6.0_02/bin/java
java_path=/usr/PROJECTS/java_jre/jre1.6.0_07/bin/java

$java_path "$memory_string" -cp /home/grass/small_java_programs/IFPRIconverter/bin org.ifpri_converter.MagicDailyAggregator $input_file $output_file

echo "finished at `date`"



