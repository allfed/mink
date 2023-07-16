# Summary: check if nitrogen rasters are properly set up to run. Sometimes there are issues with unpacking them. Copy over if missing

# This script accepts two arguments: the root directory of a git repository (git_root) and a level identifier (N_level). It then checks for the existence of a specific file (f_format) within a certain directory structure under the given git repository. The directory structure includes directories corresponding to mapsets in a GRASS GIS database.

# If either of the arguments is not provided, the script outputs an error message and exits with a status code of 1.

# It iterates through all mapsets, attempting to build a filepath from the provided git_root, a static path, the mapset name, the provided N_level, and a static file name f_format.

# If the script finds the f_format file, it ends the search, keeping a flag variable found_file as true. If it does not find the f_format file in any of the mapsets, the script outputs an error message, suggesting a command to copy the f_format file from a predefined directory to the first mapset. In this case, the script exits with a status code of 1.

# exit if there's an error
set -e

git_root=$1
N_level=$2

if [ $# -ne 2 ]; then
    echo "Usage: $0 git_root N_level"
    exit 1
fi

# Execute the g.mapsets -p command, replace spaces with newlines, and read each line as a separate mapset
mapsets=$(g.mapsets -p | tr ' ' '\n')

# Loop through the mapsets
for mapset in $mapsets
do
    if [ "$mapset" == "PERMANENT" ]; then
        continue
    fi
    
    # Construct the file path
    f_format_file="$git_root/grassdata/world/$mapset/cell_misc/$N_level/f_format"
    
    # Check if the f_format_file exists, and don't overwrite if it does
    if [ -f "$f_format_file" ]; then
        continue
    fi
    
    echo ""
    echo "Creating missing f_format file: $f_format_file"
    echo ""

    echo "type: float" > "$f_format_file"
    echo "byte_order: xdr" >> "$f_format_file"
    echo "lzw_compression_bits: -1" >> "$f_format_file"
done