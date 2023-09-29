# if you wish to just copy over the SNX files associated with megaenvironments, this is a useful script

import csv
import os
import shutil

# Define the source and target directories
archive_dir = "../SNX_files/archive"
dssat_dir = "../SNX_files/DSSAT_stuff"

target_dir = "../SNX_files/megaenvironment_matched"

# Ensure the folder exists, if not, create it
if not os.path.exists(target_dir):
    os.makedirs(target_dir)

# Expand user home directory symbol (~) if exists
archive_dir = os.path.expanduser(archive_dir)
dssat_dir = os.path.expanduser(dssat_dir)
target_dir = os.path.expanduser(target_dir)

# Open the CSV and read the first column
with open("../parameters/default_cultivar_mappings.csv", "r") as f:
    reader = csv.reader(f)
    next(reader)  # Skip the header row
    cultivars = [row[0] for row in reader]  # Get first column

# Copy files
for cultivar in cultivars:
    for folder in [archive_dir, dssat_dir]:
        for filename in os.listdir(folder):
            if filename.startswith(cultivar):
                shutil.copy(os.path.join(folder, filename), target_dir)
