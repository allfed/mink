The purpose of the scripts in this folder is to generate the SNX files and investigate the snx files that are generated or that exist in general.

the order of operations:
1. make sure you have a reasonable template set up in shared_SNX_template. Otherwise, you'll need to modify the python and the data_to_generate_SNX_files.csv to create SNX files with the appropriate data for your use case.
2. run generate_SNX_files.py to make all the files in the generated_SNX_files directory. This is where mink should be looking for your SNX files (would be good to check this if you're changing things... it's pulled in in the "original_X_files_dir" in default_paths_etc.sh)
3. You can take a look at whether the gnerated snx files match a reference set of SNX files by getting the difference in relevant lines with the compare_generated_SNX_files.py script.

Gnerally most use cases are like, "oh, I want to change from manual to automatic planting for all my files, I'll just alter the data_to_generate_SNX_files.csv and run generate_SNX_files.py to get my new set of files for my simulation".

Other useful things:

 - get_all_contents.py can be used to print out the differences between a set of SNX files (can be useful if you have a bunch of data to add in and consistently formatted SNX files, or just to see if the generation of a bunch of SNX files seems to have worked). 
 - you will almost certainly never need this, but if you wish to just copy over the SNX files associated with megaenvironments, copy_snx_files_megaenvironment.py is a useful script
