# this is intended to be "sourced" in for defaults when running DSSAT

###########################
### paths and filenames ###
###########################

  quasi_random_code="${RANDOM}_`date +%N`"





  original_runner_dir="$PWD/../small_java_programs/"

#   java_on_headnode=~rdrobert/jres/jre1.8.0_40/bin/java
   java_on_headnode=/usr/bin/java
java_on_computenode=$java_on_headnode

#original values by Ricky:
#            on_node_home=/state/partition1/DSSAT_full_${quasi_random_code}/
#            on_node_home=/state/partition1/RAM_1024/RRR/DSSAT_full_${quasi_random_code}/
#            on_node_home=/dev/shm/RRR/JJJ/DSSAT_dailyreal46_${quasi_random_code}/
#            on_node_home=/dev/shm/RRR/DDD/DSSAT_dailyreal46_${quasi_random_code}/
#            on_node_home=/state/partition1/RRR/DDD/DSSAT_dailyreal46_${quasi_random_code}/

#changes by IJ:
#           on_node_home=/mink/on_node_home_folder/DSSAT_dailyreal46_${quasi_random_code}/

# this should be the mink/ directory at the root of the git repo (DMR)
on_node_home="$PWD/../../on_node_home/" 

#uncomment as needed based on user (IJ)
#  BASE=/home/users/morganr/mink/basics_15jun22/sge_Mink3daily/

# BASE is now the folder where default_paths is located (DMR)
BASE="$PWD" 



#####################################




# doesn't exist (IJ)
# update: now does exist (DMR). Added a hidden file that keeps the folder around.
  staging_directory=${BASE}/staging_area/
#the staging area directory seems empty... (IJ)
  runner_archive_path=${staging_directory}runner_archive_${quasi_random_code}.zip
   DSSAT_archive_path=${staging_directory}DSSAT_program_${quasi_random_code}.zip
 X_files_archive_path=${staging_directory}X_files_${quasi_random_code}.zip
 compressed_data_path=${staging_directory}compressed_data_${quasi_random_code}.zip

#      original_DSSAT_dir=${BASE}/actual_program_4.4/ # heat tolerant
#      original_DSSAT_dir=${BASE}/actual_program_4.5/
#      original_DSSAT_dir=${BASE}/actual_program_4.6/ # oryza2000 integrated

#      original_DSSAT_dir=${BASE}/actual_program_real4.6/

#echo "!!!! WARNING: switching to very new DSSAT and UNOFFICIAL potato revisions!!!!"
#      original_DSSAT_dir=${BASE}/actual_program_4.6_potatoes_27jan16
#      original_DSSAT_dir=${BASE}/actual_program_4.6_potatoes_07mar16

#      original_DSSAT_dir=${BASE}/actual_program_4.6_cross_30mar17

#echo "!!!! WARNING: switching to very new AFLATOXIN version!!!!"
#      original_DSSAT_dir=${BASE}/actual_program_4.7_aflatoxins_15jun18
#      original_DSSAT_dir=${BASE}/actual_program_4.7_aflatoxins_07sep18



# let's try the new DSSAT 4.7.5.11-develop as instigated by diego
# this does not seem to exist or else i just over-wrote it on 12oct21
#      original_DSSAT_dir=${BASE}/actual_program_4.7.5.11

# latest and greatest as of 12oct21 (checked correct by IJ)
# (WARNING: actual program folder removed by DMR to save space on the repo, may need to be reinstated at some point)
      original_DSSAT_dir=${BASE}/actual_program_4.7.5.11



      # normal temperature/original style
#      wheat_original_DSSAT_dir=${BASE}/actual_program_4.5_wheat/
      # heat tolerant attempt
#      wheat_original_DSSAT_dir=${BASE}/actual_program_4.5_wheat_heattolerant/

#      wheat_original_DSSAT_dir=${BASE}/actual_program_real4.6/
      wheat_original_DSSAT_dir=$original_DSSAT_dir



    original_X_files_dir=${BASE}SNX_files/
#echo "!!! original X files directory changed for the moment !!!"
#    original_X_files_dir=${BASE}SNX_files/meta_templates/full_wheat_search/

#doesn't exist on Github- except for from_GRASS and logs (IJ). Others are still there on Morgan's JASMIN account
          input_data_dir=${BASE}from_GRASS/
  chunked_input_data_dir=${BASE}chunks_from_GRASS/
         output_data_dir=${BASE}to_GRASS/
 chunked_output_data_dir=${BASE}chunks_to_GRASS/
                logs_dir=${BASE}logs/

             weather_dir=${BASE}/dailyweather/
   prestaged_weather_dir=/state/partition1/prestaged_DSSAT_daily_weather/

        runner_init_file=${staging_directory}runner_init_${quasi_random_code}.txt
  
#            on_node_home=/dev/shm/RRR/DSSAT_r2.45_${quasi_random_code}/
#            on_node_home=/state/partition1/RAM_1024/DSSAT_r2.45_${quasi_random_code}/

# confused on the role of these directories and whether they exist (IJ)
       on_node_DSSAT_dir=${on_node_home}DSSAT/
      on_node_runner_dir=${on_node_home}basics_15jun22/small_java_programs/

     on_node_weather_dir=${on_node_home}dailyweather/
  on_node_input_data_dir=${on_node_home}from_GRASS/

      on_node_output_dir=${on_node_home}to_GRASS/

                   X_dir=${on_node_DSSAT_dir}X_files/

    magic_X_file=${X_dir}${X_template}
    on_node_runner_init_file=${X_dir}runner_init_${quasi_random_code}.txt

        log_file=${logs_dir}log_${current_thread_index}_${node_to_use}_${quasi_random_code}.txt


##########################





           queue_name=all.q
#            queue_name=low_priority.q

#           queue_name=preempt.q

#           queue_name=two_on_all_A.q
#           queue_name=secondhalf.q


