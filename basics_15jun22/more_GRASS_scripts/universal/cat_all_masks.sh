#!/bin/bash
# An array of all the mask names
declare -a masks=( "whK013" "whK016" "whK015" "whK010" "whK076" "whK012" "whK011" "whK007" "whK006" "whK009" "whK002" "whK001")
# declare -a masks=("mzK013" "mzK013IR" "mzK013RF" "mzK014" "mzK014IR" "mzK014RF" "mzK015"
#                   "mzK015IR" "mzK015RF" "mzK016" "mzK016IR" "mzK016RF" "mzK017"
#                   "mzK017IR" "mzK017RF" "mzK018" "mzK018IR" "mzK018RF" "mzK021"
#                   "mzK021IR" "mzK021RF" "mzK023" "mzK023IR" "mzK023RF" "mzK024"
#                   "mzK024IR" "mzK024RF" "mzK025" "mzK025IR" "mzK025RF" "mzK026"
#                   "mzK026IR" "mzK026RF" "mzK027" "mzK027IR" "mzK027RF" "mzK029"
#                   "mzK029IR" "mzK029RF")

# Loop over the array
for mask in "${masks[@]}"; do
    echo "Processing $mask..."
    # Run the command with the mask as an argument
    ./save_ascii.sh "mask_for_${mask}IR"
    # Use cat to display the output
    cat "mask_for_$mask.asc"
done

echo "All done."

