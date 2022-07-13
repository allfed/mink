#!/bin/bash

if [ -z "$1" ]; then
  interval=10s
else
  interval=$1
fi

keep_alive_ugly.sh $interval "
uptime;
echo everyone all and running;
qstat -u \"*\" | tail -n +3 | wc -l;
qstat -u \"*\" | grep ' r ' | wc -l;
echo me all running;
qstat | tail -n +3 | wc -l;
qstat | grep ' r ' | wc -l;
qstat | grep ' T ';
qstat | grep ' r ' | head -n4;
qstat | grep ' r ' | tail -n4;
qstat | grep 'qw ' | head -n2;
qstat |              tail -n2"

