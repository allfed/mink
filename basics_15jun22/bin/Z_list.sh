#list=`for (( ccc=0 ; ccc <= 19 ; ccc++ )); do ls log_*_compute-0-${ccc}_*.txt; done`
#list=`ls -rS log*.txt`
list=`ls -rt log*.txt`

tail -n 1 $list | grep -v log | grep -v "^$" | grep -v -e "--E--" |  cat -n | less
