#!/bin/bash

if [ $# = 0 ]; then
  interval=2m
else
  interval=$1
  command=$2
fi


counter=0
while [ 1 = 1 ]; do
  echo "#$counter ; `date`"
  eval $command
  let "counter++"
  sleep $interval

  # check to make sure sleep exited properly
  if [ $? -ne 0 ]; then
    break
  fi
done
