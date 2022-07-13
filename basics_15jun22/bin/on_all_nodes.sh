#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Usage: $0 command"
  echo ""
  echo "This is supposed to be cluster-fork clone. The command is run"
  echo "across all the compute nodes. The list is derived from the"
  echo "ROCKS accounting."

  exit;
fi

#command_to_run="$1"
command_to_run="$@"

#machine_list=`rocks list host | grep Compute | cut -d: -f1`
machine_list=`rocks list host | grep Compute | cut -d: -f1 | tac`

#echo "$machine_list" | cat -n

for machine in $machine_list
do

  echo "-- $machine at `date` --"
  ssh $machine "$command_to_run"

done



