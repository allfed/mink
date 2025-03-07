#!/usr/bin/env python3
import os
import glob
import re
import sys

# Get all files ending with productiontest.yaml
files = glob.glob("*productiontest.yaml")

physical_params_replacement = """physical_parameters:
  region_to_use_n: 69.375
  region_to_use_s: -69.375
  region_to_use_e: 176.25
  region_to_use_w: -170
  nsres: 1.875
  ewres: 1.25
  co2_level: 419
  # minimum area cropland in a pixel to consider, in hectares. Any smaller pixel is ignored.
  minimum_physical_area: 0
  # minimum yield to average in kg/ha
  minimum_yield: -3"""

validation_errors = []

for file in files:
    # Create new filename by replacing productiontest with production
    new_filename = file.replace("productiontest.yaml", "production.yaml")
    
    # Read the content of the original file
    with open(file, "r") as f:
        content = f.read()
    
    # Validation checks
    # Check for planting_months: [1, 6]
    planting_months_count = content.count("planting_months: [1, 6]")
    if planting_months_count != 1:
        validation_errors.append(f"Error in {file}: 'planting_months: [1, 6]' found {planting_months_count} times (expected 1)")
    
    # Check physical parameters section
    physical_params_pattern = r"physical_parameters:.*?minimum_yield: -3"
    physical_params_count = len(re.findall(physical_params_pattern, content, flags=re.DOTALL))
    if physical_params_count != 1:
        validation_errors.append(f"Error in {file}: Physical parameters section found {physical_params_count} times (expected 1)")
    
    # Check for run_descriptor
    run_descriptor_count = content.count("run_descriptor: Mar05_ProductionTestOffset1")
    if run_descriptor_count != 1:
        validation_errors.append(f"Error in {file}: 'run_descriptor: Mar05_ProductionTestOffset1' found {run_descriptor_count} times (expected 1)")
    
    # Find crop name correctly - extract from the crops section
    crop_match = re.search(r"crops:\s*\n-\s*name:\s*(\w+)", content)
    if not crop_match:
        validation_errors.append(f"Error in {file}: Could not find crop name in the file")
        continue
        
    actual_crop_name = crop_match.group(1)
    
    # Check for results_folder with a more flexible pattern
    results_folder_pattern = r"results_folder: results_production_\w+_newyears_offset1"
    results_folders = re.findall(results_folder_pattern, content)
    if len(results_folders) != 1:
        validation_errors.append(f"Error in {file}: Results folder pattern found {len(results_folders)} times (expected 1)")
        continue
    
    # Found a results folder entry, just need to continue
    
    # Skip this file if there were validation errors
    if any(error.startswith(f"Error in {file}:") for error in validation_errors):
        continue
    
    # Replace planting months
    content = content.replace("planting_months: [1, 6]", 
                             "planting_months: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]")
    
    # Replace physical parameters section
    content = re.sub(physical_params_pattern, 
                     physical_params_replacement, 
                     content, flags=re.DOTALL)
    
    # Remove lines with "snx_names: ["
    content = "\n".join(line for line in content.split("\n") 
                       if "snx_names: [" not in line)
    
    # Replace run_descriptor
    content = content.replace("run_descriptor: Mar05_ProductionTestOffset1", 
                             "run_descriptor: Mar07_ProductionRun")
    
    # Replace results_folder with a generic pattern
    content = re.sub(r"results_folder: results_production_\w+_newyears_offset1", 
                     f"results_folder: results_production_{actual_crop_name}", 
                     content)
    
    # Write the modified content to the new file
    with open(new_filename, "w") as f:
        f.write(content)
    
    print(f"Created {new_filename}")

if validation_errors:
    print("\nValidation errors found:")
    for error in validation_errors:
        print(f"  - {error}")
    print("\nNo files were created due to validation errors.")
    sys.exit(1)
else:
    print("All files processed successfully!")