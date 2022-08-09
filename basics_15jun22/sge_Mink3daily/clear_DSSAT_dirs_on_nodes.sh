#!/bin/bash



. default_paths_etc.sh

#magic_directory=/state/partition1/DSSAT_uc #.45
magic_directory=${on_node_home%_*_*}
echo "md = [$magic_directory]"

on_all_nodes.sh "rm -rf ${magic_directory}* ; eval echo \"[all DONE at...\`date\`]\" "

exit












if [ -z "$1" ]; then
  n_nodes=20
else
  n_nodes=$1
fi


# the format here is "ip_address<TAB>n_cpus"
#compute_node_file=$1
#list_of_compute_nodes=`cat $compute_node_file`

for (( index=0 ; index < n_nodes ; index++ )); do

list_of_compute_nodes="$list_of_compute_nodes
compute-0-$index	4"

done



##############
IFS="
"

# count up how many processors we have
for node_line in $list_of_compute_nodes
do
  ipaddress=`echo "$node_line" | cut -f1`
  echo "     trying: $ipaddress"
  ssh $ipaddress "rm -rf /${magic_directory}*; eval echo "[$ipaddress all DONE at... \`date\`]""  &
done








exit

# old version

# this is an attempt to automate the deployment of a bunch of single-processor
# continuous neural net solvers.

if [ $# -ne 1 ]; then
  echo "Usage: $0 compute_node_file"
  echo ""
  exit 1
fi

#magic_directory=/state/partition1/DSSAT_belly_flop

. default_paths_etc.sh

magic_directory=/state/partition1/DSSAT_uc #.45
magic_directory=${on_node_home%_*_*}
echo "md = [$magic_directory]"

# the format here is "ip_address<TAB>n_cpus"
compute_node_file=$1
list_of_compute_nodes=`cat $compute_node_file`


##############
IFS="
"

# count up how many processors we have
for node_line in $list_of_compute_nodes
do
  ipaddress=`echo "$node_line" | cut -f1`
  echo "     trying: $ipaddress"
  ssh $ipaddress "rm -rf /${magic_directory}*; eval echo "[$ipaddress all DONE at... \`date\`]""  &
done



