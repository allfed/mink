#!/bin/bash

# This script uses a for loop to iterate over all the files in the current directory that end with _control.csv. For each file, it creates a new file with the same name but with "control" replaced with "catastrophe", using sed to do the string replacement. The cp command is used to copy the file, and the sed command with the -i option is used to edit the new file in-place and replace all occurrences of "control" with "catastrophe".


for file in *_control\.csv; do
    echo $file

    # uncomment to make the "catastrophe" copies from control
    # new_file="$(echo "$file" | sed 's/control/catastrophe/')"
    # cp "$file" "$new_file"
    # sed -i 's/control/catastrophe/g' "$new_file"

    # uncomment to make the "greenhouse" copies from control
    new_file="$(echo "$file" | sed 's/control/catastrophe_greenhouse/')"
    cp "$file" "$new_file"
    sed -i 's/Outdoor_crops_control/Greenhouse_catastrophe/g' "$new_file"
    sed -i 's/_control/_catastrophe_greenhouse/g' "$new_file"
    sed -i 's/control_mink/catastrophe_mink_greenhouse/g' "$new_file"
done