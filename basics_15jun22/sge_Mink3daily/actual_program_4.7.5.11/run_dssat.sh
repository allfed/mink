#!/bin/bash
# THIS SCRIPT -C THING WAS THE ONLY WAY TO STOP SENDING OUTPUT WITHOUT DSSAT CRASHING
# But later, it stopped working... so I just removed the InheritIO from Mink3p2daily.java and that prevented it from printing out
# script -c "./dscsm_compiled_fast.exe n deleteme.v45" > /dev/null

# if want it to print out, uncomment below and comment above
 ./dscsm_compiled_fast.exe n deleteme.v45

# (DMR) this copies the dssat out to a sub-folder so we can look at the individual 
# cells being run. Can be deleted.
# new_name_postfix=`head RRRR.WTH -n 2 | tail -n 1 | head -c 25 | tail -c 19|tr ' ' '_'`
# cp -r . ../../DSSAT_"$new_name_postfix"
# rm ../../DSSAT_"$new_name_postfix"/dscsm_compiled_fast.exe
