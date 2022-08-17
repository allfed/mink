#!/bin/bash

memory_string="-mx3800M"

# H P R are the suffixes
file_name="/home/grass/IFPRI_yield/India_23jan08/India_R.txt"
output_base="/home/grass/IFPRI_yield/India_23jan08/output/R"
delimiter=":"
pixel_field=2
#dataColumnsCSV="4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33"

dataColumnsCSV="4"
for (( i=5 ; i<=108 ; i++))
do
  dataColumnsCSV="${dataColumnsCSV},${i}"
done

java_path=/state/partition1/ManuallyInstalledJava/jre1.6.0_02/bin/java

$java_path -cp /home/grass/small_java_programs/IFPRIconverter/bin org.ifpri_converter.MagicConverterSeveral $file_name $output_base $delimiter $pixel_field $dataColumnsCSV

echo "finished at `date`"



