#!/usr/bin/env python3
import os
import shutil

def create_yaml_copies(template_file, new_strings):
    """
    Creates copies of the template file for each string in the list, 
    making the specified replacements.
    
    Args:
        template_file (str): Path to the template YAML file
        new_strings (list): List of strings to use in new file names and replacements
    """
    # Check if template file exists
    if not os.path.exists(template_file):
        print(f"Error: Template file {template_file} not found!")
        return
    
    # Read the template file content
    with open(template_file, 'r') as f:
        template_content = f.read()
    
    # Process each new string
    for new_string in new_strings:
        # Create the new file name
        new_file = f"rice_{new_string}.yaml"
        
        # Make the replacements
        new_content = template_content.replace("snx_names: [riK001]", f"snx_names: [{new_string}]")
        new_content = new_content.replace("Apr11_CheckingRice_riK001", f"Apr11_CheckingRice_{new_string}")
        new_content = new_content.replace("results_production_rice_riK001", f"results_production_rice_{new_string}")
        
        # Write the new file
        with open(new_file, 'w') as f:
            f.write(new_content)
        
        print(f"Created {new_file}")

def main():
    template_file = "rice_riK001.yaml"
    
    # List of strings to use for new files
    new_strings = [
        "riK0t1",
        "riK0t3",
        "riK002",
        "riIR43",
        "riCL00IF",
        "riMMMirriga1o2",
        "riMMMirriga005",
        "riceceresflooded",
        "riCL00"
    ]
    
    create_yaml_copies(template_file, new_strings)
    print(f"Successfully created {len(new_strings)} new YAML files based on {template_file}")

if __name__ == "__main__":
    main()