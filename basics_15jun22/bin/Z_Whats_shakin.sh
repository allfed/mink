#grep __ `ls -t *.txt` | cut -d":" -f2 | grep -v "^\[" | cut -d"/" -f6 | cut -dC -f1,3 | cat -n | less
#grep __ `ls -t *.txt` | cut -d":" -f2 | grep -v "^\[" | cut -d"/" -f6 |                 cat -n | less
#grep __ `ls -t *.txt` | cut -d":" -f2 | grep -v "^\[" | cut -d"/" -f8 | cut -d_ -f1,2,5- | cat -n | less


#grep __ `ls -t *.txt` | cut -d":" -f2 | grep -e "\[\[\[" | cut -d"/" -f7 | cut -d"]" -f1 | cat -n | less

#grep __ `ls -t *.txt` | cut -d":" -f2 | grep -e "\[\[\[" | cut -d"/" -f8 | cut -d"]" -f1 | cat -n | less

if [ -n "$1" ]; then
  grep __ `ls -t *.txt` | cut -d":" -f2 | grep -e "\[\[\[" | cut -d"/" -f6 | cut -d"]" -f1 | cat -n | less
else
  grep __ `ls -t *.txt` | cut -d":" -f2 | grep -e "\[\[\[" | cut -d"/" -f8 | cut -d"]" -f1 | cat -n | less
fi
