#!/bin/bash

#
# (DMR) Reformats in-place, and compiles java classes, then moves them to the bin directory.
#

. default_paths_etc.sh # import original_runner_dir, java_dir,

set -e # exit if a command fails

# Base directory for your Java source files
BASE_SRC_DIR="${java_dir}src"

# Base directory for the compiled classes
BASE_BIN_DIR="${java_dir}bin"

# Compile java files and move .class files to bin directory
for dir in $(find ${BASE_SRC_DIR} -type d); do
    PACKAGE_PATH=${dir#${BASE_SRC_DIR}/} # Remove base src directory path
    for java_file in $(find ${dir} -maxdepth 1 -name "*.java"); do
        if [ "$#" -gt 0 ]; then
            this_file_is_being_compiled_and_args_passed=false
            for arg in "$@"; do
                # the logic below captures subclasses of a class in java, if we pass in the parent class, this means all the subclasses will be compiled...
                if [ "$(basename ${java_file})" == "${arg}" ]; then
                    this_file_is_being_compiled_and_args_passed=true
                    break
                fi
            done
        else
            this_file_is_being_compiled_and_args_passed=true
        fi

        if [ "$this_file_is_being_compiled_and_args_passed" == "false" ]; then
            continue # continue is only possible if arguments were passed. if arguments passed, continue only if the current filename matches one of the args 
        fi

        # Uncomment below if you wish to print the reformat command
        # echo ""
        # echo "java -jar ${original_runner_dir}google-java-format-1.15.0-all-deps.jar --replace ${java_file}"

        echo ""
        echo "Reformatting ${java_file}"
        java -jar "${original_runner_dir}google-java-format-1.15.0-all-deps.jar" --replace "${java_file}"


        echo ""
        echo "Compiling  ${java_file}, and moving the compiled .class file to the proper bin/ directory..."

        # Compile the Java file with -Xlint flag
        javac -sourcepath ${BASE_SRC_DIR} ${java_file}        

        # Move the compiled .class file to the corresponding package in the bin directory
        CLASS_FILE="${java_file%.java}" # Replace .java extension with .class
        BIN_DIR=${BASE_BIN_DIR}/${PACKAGE_PATH} # Corresponding bin directory
        mkdir -p ${BIN_DIR} # Create the bin directory if it does not exist
        mv ${CLASS_FILE}*.class ${BIN_DIR}
        echo ""
    done
done

echo "Cleaning up any remaining .class files in ${BASE_SRC_DIR}..."
find ${BASE_SRC_DIR} -type f -name "*.class" -exec rm -f {} \;
echo "Cleanup complete."

cd "${BASE}"

