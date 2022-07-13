#key_to_sort=4 order_flag=""
key_to_sort=16 order_flag="-r"

#tail -n 1 *.txt | grep prog | sort -n -k4 | cat -n | less
tail -n 1 *.txt | grep prog | sed "s/\/ /\/_/g" |  sort -n -k${key_to_sort} -r | cat -n | less
