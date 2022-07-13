# this is intended to be "sourced" in for defaults when running DSSAT

###########################
### paths and filenames ###
###########################

  quasi_random_code="${RANDOM}_`date +%N`"





  original_runner_dir=~rdrobert/small_java_programs/

#   java_on_headnode=/usr/java/latest/bin/java
#java_on_computenode=/usr/java/latest/bin/java
#   java_on_headnode=~rdrobert/jres/jre1.8.0_40/bin/java
   java_on_headnode=~rdrobert/jres/jre1.8.0_66/bin/java
java_on_computenode=$java_on_headnode


#            on_node_home=/state/partition1/DSSAT_full_${quasi_random_code}/
#            on_node_home=/state/partition1/RAM_1024/RRR/DSSAT_full_${quasi_random_code}/
            on_node_home=/dev/shm/RRR/JJJ/DSSAT_real46_${quasi_random_code}/
#            on_node_home=/state/partition1/RRR/JJJ/DSSAT_real46_${quasi_random_code}/

  BASE=~rdrobert/sge_Mink3classic/




#####################################





  staging_directory=${BASE}/staging_area/

  runner_archive_path=${staging_directory}runner_archive_${quasi_random_code}.zip
   DSSAT_archive_path=${staging_directory}DSSAT_program_${quasi_random_code}.zip
 X_files_archive_path=${staging_directory}X_files_${quasi_random_code}.zip
 compressed_data_path=${staging_directory}compressed_data_${quasi_random_code}.zip

#      original_DSSAT_dir=${BASE}/actual_program_4.4/ # heat tolerant
#      original_DSSAT_dir=${BASE}/actual_program_4.5/
#      original_DSSAT_dir=${BASE}/actual_program_4.6/ # oryza2000 integrated
#      original_DSSAT_dir=${BASE}/actual_program_real4.6/

#echo "!!!! WARNING: switching to very new DSSAT !!!!"
#      original_DSSAT_dir=${BASE}/actual_program_4.6_07jan16/

#echo "!!!! WARNING: switching to very new DSSAT and UNOFFICIAL potato revisions!!!!"
#      original_DSSAT_dir=${BASE}/actual_program_4.6_potatoes_27jan16


# this is one of the good ones to use... i think it is what went into H15TEST
#echo "!!!! WARNING: switching to very new DSSAT and UNOFFICIAL potato revisions!!!!"
#      original_DSSAT_dir=${BASE}/actual_program_4.6_potatoes_07mar16/
#      original_DSSAT_dir=${BASE}/actual_program_4.6_whpt_08jul16/


#echo "!!!! WARNING: switching to very new DSSAT and UNOFFICIAL potato revisions!!!!"
#echo "!!!! WARNING: and IXCHEL's new wheat definitions  !!!!"
##      original_DSSAT_dir=${BASE}/actual_program_4.6_ixchels_stuff/
#echo "!!!! WARNING: but n-wheat has changed....  !!!!"
# this one below is what i used for durian, i think....
#      original_DSSAT_dir=${BASE}/actual_program_4.6_cross_30mar17/ # USE THIS FOR durianA


#      original_DSSAT_dir=${BASE}/actual_program_4.7_first_simpleB/

#      original_DSSAT_dir=${BASE}/actual_program_4.7_aflatoxins/
# i may have run some stuff that i thought was updated aflatoxins, but it was really with the old one...

# this has the best maize and groundnuts with aflatoxins. everything else should be the same
#      original_DSSAT_dir=${BASE}/actual_program_4.7_aflatoxins_15jun18/


       original_DSSAT_dir=${BASE}/actual_program_4.7.5.11/ # use this for most crops
#      original_DSSAT_dir=${BASE}/actual_program_4.6_whpt_08jul16/ # use this for potatoes with dscsm046.exe
#      original_DSSAT_dir=${BASE}/actual_program_4.7_first_simpleB/


############################
###### note to self... #####
############################
# FTEST used the interim actual_program_real4.6
# except for potatoes which used the first major rubi raymundo
# fix of the potato model. FTEST = original actual_program_real4.6
# FTEST1 = rerun of wheat with actual_program_real4.6
# FTEST2 = rerun of potatoes with actual_program_4.6_potatoes_27jan16
# FTESTBNRI = ciat beans and ciat rice

      # normal temperature/original style
#      wheat_original_DSSAT_dir=${BASE}/actual_program_4.5_wheat/
      # heat tolerant attempt
#      wheat_original_DSSAT_dir=${BASE}/actual_program_4.5_wheat_heattolerant/

#      wheat_original_DSSAT_dir=${BASE}/actual_program_real4.6/
      wheat_original_DSSAT_dir=$original_DSSAT_dir



    original_X_files_dir=${BASE}SNX_files/
#echo "!!! original X files directory changed for the moment !!!"
#    original_X_files_dir=${BASE}SNX_files/meta_templates/full_wheat_search/

          input_data_dir=${BASE}from_GRASS/
  chunked_input_data_dir=${BASE}chunks_from_GRASS/
         output_data_dir=${BASE}to_GRASS/
 chunked_output_data_dir=${BASE}chunks_to_GRASS/
                logs_dir=${BASE}logs/

        runner_init_file=${staging_directory}runner_init_${quasi_random_code}.txt
  
#            on_node_home=/dev/shm/RRR/DSSAT_r2.45_${quasi_random_code}/
#            on_node_home=/state/partition1/RAM_1024/DSSAT_r2.45_${quasi_random_code}/
 
       on_node_DSSAT_dir=${on_node_home}DSSAT/
      on_node_runner_dir=${on_node_home}small_java_programs/

  on_node_input_data_dir=${on_node_home}from_GRASS/

      on_node_output_dir=${on_node_home}to_GRASS/

                   X_dir=${on_node_DSSAT_dir}X_files/

    magic_X_file=${X_dir}${X_template}
    on_node_runner_init_file=${X_dir}runner_init_${quasi_random_code}.txt

        log_file=${logs_dir}log_${current_thread_index}_${node_to_use}_${quasi_random_code}.txt


##########################





#            queue_name=all.q
            queue_name=low_priority.q

#           queue_name=preempt.q

#           queue_name=two_on_all_A.q
#           queue_name=secondhalf.q


