grep since log*txt | tr "\t" " " | tr -s " " | cut -d" " -f8-12 | sort -n | cat -n | less
