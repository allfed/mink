#!/bin/bash
for file in *
do
    echo $file
    cat $file | grep "CULTIVAR" -A 4
done