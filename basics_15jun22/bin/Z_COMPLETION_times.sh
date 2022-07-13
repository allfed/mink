#file_list=`ls log*txt`

#grep since log*txt | -d" " -f1,8-12 > deleteme_junk.txt

file_list=`grep since log*txt | cut -d: -f1`

grep since $file_list | tr "\t" " " | tr -s " " | cut -d" " -f8-12 > deleteme_times.txt

grep CASE $file_list | cut -d":" -f2- | sed "s/CASE.//g" | cut -f1 > deleteme_case_names.txt

paste deleteme_times.txt deleteme_case_names.txt | sort -n | cat -n | less

rm deleteme_times.txt deleteme_case_names.txt
