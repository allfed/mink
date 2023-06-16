import csv
import os
import shutil

# Define the source and target directories
archive_dir = "~/Code/mink/basics_15jun22/sge_Mink3daily/SNX_files/archive"
dssat_dir = "~/Code/mink/basics_15jun22/sge_Mink3daily/SNX_files/DSSAT_stuff"
target_dir = (
    "~/Code/mink/basics_15jun22/sge_Mink3daily/SNX_files/megaenvironment_matched"
)

# Expand user home directory symbol (~)
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
