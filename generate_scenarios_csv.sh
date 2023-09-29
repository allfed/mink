set -e # exit if a command fails

COMPILE_FLAG=0 # Default to not compile

# Handle the --compile or -c flag using getopts
while getopts ":c" option; do
  case $option in
    c) COMPILE_FLAG=1 ;;
    \?) echo "Usage: $0 [-c] [scenario_config_file_location]"
        echo "This script compiles java and sets up generated generated_scenarios in basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.txt"
        echo "the -c or --compile compiles java. If it's not present, previously compiled java is used."
        exit 1 ;;
  esac
done

# Remove the options from the positional parameters
shift $((OPTIND-1))

# Check if the positional argument is present
if [ $# -eq 0 ]; then
  echo "Usage: $0 [-c] [scenario_config_file_location]"
  exit 1
fi

time_start=$SECONDS
. basics_15jun22/sge_Mink3daily/default_paths_etc.sh
. basics_15jun22/sge_Mink3daily/some_settings_46.sh

cd /mnt/data/basics_15jun22/sge_Mink3daily/

# Only compile if the flag is present
if [ $COMPILE_FLAG -eq 1 ]; then
  ./compile_java.sh Config.java GenerateScenarios.java BashScripts.java
fi

cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/src
scenarios_csv_location="/mnt/data/basics_15jun22/sge_Mink3daily/scenarios/generated_scenarios.csv"
config_file_location="/mnt/data/$1"
run_parameters_csv_folder="/mnt/data/basics_15jun22/sge_Mink3daily/parameters/"
run_script_folder=/mnt/data/basics_15jun22/sge_Mink3daily/

cd /mnt/data/basics_15jun22/small_java_programs/java8_IFPRIconverter/bin

java -ea org.Scenarios.GenerateScenarios $run_script_folder $config_file_location $scenarios_csv_location $run_parameters_csv_folder
time_end=$SECONDS
echo "duration (seconds)"
echo $(echo "scale=2 ; $time_end - $time_start " | bc -l)

