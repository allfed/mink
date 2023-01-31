#!/bin/bash
#https://stackoverflow.com/questions/16726779/how-do-i-get-the-total-cpu-usage-of-an-application-from-proc-pid-stat

#also, thanks chatGPT! 
# this was my prompt to get this script:
#" okay, for /proc/PID/stat, I have columns as follows:

#     #14 utime - CPU time spent in user code, measured in clock ticks
#     #15 stime - CPU time spent in kernel code, measured in clock ticks
#     #16 cutime - Waited-for children's CPU time spent in user code (in clock ticks)
#     #17 cstime - Waited-for children's CPU time spent in kernel code (in clock ticks)
#     #22 starttime - Time when the process started, measured in clock ticks
# please create a bash script to assign these different clock ticks to variables using the "cat" command"

#here's what it gave me

# Assign the PID of the process to a variable
PID=986115

# Use the cat command to read the values from the /proc/PID/stat file
CLOCK_TICKS=$(cat /proc/$PID/stat)

# Assign the different clock ticks to variables
UPTIME=$(echo $CLOCK_TICKS | awk '{print $1}')
UTIME=$(echo $CLOCK_TICKS | awk '{print $14}')
STIME=$(echo $CLOCK_TICKS | awk '{print $15}')
CUTIME=$(echo $CLOCK_TICKS | awk '{print $16}')
CSTIME=$(echo $CLOCK_TICKS | awk '{print $17}')
STARTTIME=$(echo $CLOCK_TICKS | awk '{print $22}')

# hertz
HERTZ=$(getconf CLK_TCK)

# Print the values of the variables
echo "UPTIME: $UPTIME"
echo "UTIME: $UTIME"
echo "STIME: $STIME"
echo "CUTIME: $CUTIME"
echo "CSTIME: $CSTIME"
echo "STARTTIME: $STARTTIME"


# I wrote the rest below

# TTIME=$(echo "scale=4 ; $UTIME+$STIME" | bc -l)
# echo "TTIME"
# echo $TTIME
# SECONDS=$(echo "scale=4 ; $STARTTIME / $HERTZ" | bc -l)
echo "HERTZ"
echo $HERTZ
# echo "SECONDS"
# echo $SECONDS
echo ""
echo ""
echo "UTIME"
echo $UTIME
echo "STIME"
echo $STIME
echo "HERTZ"
echo $HERTZ
echo "UPTIME"
echo $UPTIME
echo "STARTTIME"
echo $STARTTIME
echo "Total time seconds:"
echo $(echo "scale=4 ; ( $UTIME + $STIME + $CUTIME ) / $HERTZ " | bc -l | xargs printf "%.4f")
echo "seconds:"
echo $(echo "scale=4 ; ($UPTIME - ($STARTTIME / $HERTZ ) ) " | bc -l | xargs printf "%.4f")
echo "percentage CPU:"
echo $(echo "scale=4 ; 100 * ( ( $UTIME + $STIME +  $CUTIME) / $HERTZ ) / ($UPTIME - ($STARTTIME / $HERTZ ) ) " | bc -l | xargs printf "%.4f")
# echo "CPU USAGE PERCENT:"
# echo $CPU_USAGE