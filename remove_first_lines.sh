#!/bin/bash
for file in control_mink/*;

do
    echo $file
    tail -n +2 "$file" > "$file.tmp" && mv "$file.tmp" "$file";
done