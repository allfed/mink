#!/bin/bash
# THIS SCRIPT -C THING WAS THE ONLY WAY TO STOP SENDING OUTPUT WITHOUT DSSAT CRASHING
# But later, it stopped working... so I just removed the InheritIO from Mink3p2daily.java and that prevented it from printing out
# script -c "./dscsm_compiled_fast.exe n deleteme.v45" > /dev/null

# if want it to print out, uncomment below and comment above
 # echo "running dscsm047_fast n deleteme.v45" >> /mnt/data/log.txt
 # ./dscsm_compiled_fast.exe n deleteme.v45


 # time_start=$(date +%s%3N)

 ./dscsm047_debug n deleteme.v45 # >> /mnt/data/log.txt 2>&1

 # time_end=$(date +%s%3N)
 # time_elapsed=$(echo "scale=4; ($time_end - $time_start)/1000" | bc -l)

 # echo "DSSAT time_elapsed = $time_elapsed" >> /mnt/data/log.txt #2>&1




# (DMR) this copies the dssat out to a sub-folder so we can look at the individual 
# cells being run. Can be deleted.
# new_name_postfix=`head RRRR.WTH -n 2 | tail -n 1 | head -c 25 | tail -c 19|tr ' ' '_'`
# cp -r . ../../DSSAT_"$new_name_postfix"
# rm ../../DSSAT_"$new_name_postfix"/dscsm_compiled_fast.exe
