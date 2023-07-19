#!/bin/bash

. default_paths_etc.sh

set -e # exit if a command fails

# Base directory for your Java source files
BASE_SRC_DIR="${java_dir}src"

# Base directory for the compiled classes
BASE_BIN_DIR="${java_dir}bin"

# Compile java files and move .class files to bin directory
for dir in $(find ${BASE_SRC_DIR} -type d); do
    PACKAGE_PATH=${dir#${BASE_SRC_DIR}/} # Remove base src directory path
    for java_file in $(find ${dir} -maxdepth 1 -name "*.java"); do
        echo "Compiling  ${java_file} and moving the .class file to the proper bin/ directory..."

        # Compile the Java file with -Xlint flag
        javac -sourcepath ${BASE_SRC_DIR} ${java_file}

        # Move the compiled .class file to the corresponding package in the bin directory
        CLASS_FILE=${java_file%.java}.class # Replace .java extension with .class
        BIN_DIR=${BASE_BIN_DIR}/${PACKAGE_PATH} # Corresponding bin directory
        mkdir -p ${BIN_DIR} # Create the bin directory if it does not exist
        mv ${CLASS_FILE} ${BIN_DIR}
        echo ""
    done
done

echo "Cleaning up any remaining .class files in ${BASE_SRC_DIR}..."
find ${BASE_SRC_DIR} -type f -name "*.class" -exec rm -f {} \;
echo "Cleanup complete."

cd "${BASE}"

