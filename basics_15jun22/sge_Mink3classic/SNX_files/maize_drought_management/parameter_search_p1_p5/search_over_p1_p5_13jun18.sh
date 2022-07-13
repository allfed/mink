#!/bin/bash


# the idea is to try to figure out what a good variety would look like for north america. i am starting
# with the one we have always used (which tended to have too low of yields, anyway)

IFS="
"


original_definition="IB0185 JACKSON HYBRI        . IB0001 200.0 0.300 950.0 980.0  7.15 43.00"

cultivar_code_placeholder=AAAAAA
           p1_placeholder=BBB
           p5_placeholder=CCCC

template_definition="AAAAAA SEARCHTEMPLATE       . IB0001   BBB 0.300  CCCC 980.0  7.15 43.00"


p1_list=\
"
 50
100
150
200
250
300
350
400
450
"

p5_list=\
"
 400
 500
 600
 700
 800
 900
1000
1100
1200
"


cultivar_prefix=RR # Beware the MAGIC NUMBER!!! this is two characters long....
counter=0

for p1 in $p1_list; do
for p5 in $p5_list; do

  # build a cultivar code
  # be sure that the number of characters works out to 6 total, including the prefix
  # since i am starting with a 2 character prefix, this leaves 4 characters for the
  # number meaning the most padding i need is 3 characters
  if [ $counter -le 9 ]; then
    padding=000
  elif [ $counter -le 99 ]; then
    padding=00
  elif [ $counter -le 999 ]; then
    padding=0
  elif [ $counter -le 9999 ]; then
    padding=""
#    padding=00
#  elif [ $counter -le 99999 ]; then
#    padding=0
#  elif [ $counter -le 999999 ]; then
#    padding=""
  else
    padding="weirdness"
  fi


  new_cultivar_code=${cultivar_prefix}${padding}${counter}



#cultivar_code_placeholder=AAAAAA
#           p1_placeholder=BBB
#           p5_placeholder=CCCC
#template_definition="AAAAAA SEARCHTEMPLATE       . IB0001   BBB 0.300  CCCC 980.0  7.15 43.00"


  new_definition=`echo "$template_definition" | sed "s/$cultivar_code_placeholder/$new_cultivar_code/g ; s/$p1_placeholder/$p1/g ; s/$p5_placeholder/$p5/g"`

  echo "$new_definition"

  let "counter++"

done # p5
done # p1






