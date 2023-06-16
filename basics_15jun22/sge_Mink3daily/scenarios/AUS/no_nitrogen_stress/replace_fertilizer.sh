#!/bin/bash

# This script uses a for loop to iterate over all the files in the current directory .csv.  the sed command with the -i option is used to edit the new file in-place and replace all occurrences of "250" with "43" for nitrogen.


for file in *\.csv; do
    echo $file
    sed -i 's/,Feb13RealNitAUS150tg,/,Feb13N250AUS150tg,/g' "$file"
done