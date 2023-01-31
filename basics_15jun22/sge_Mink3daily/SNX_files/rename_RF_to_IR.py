import os
import re

# Define the target lines to be replaced
old_line1 = "@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\n"
old_line2 = " 1 MA              R     N     D     N     M\n"
new_line1 = "@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\n"
new_line2 = " 1 MA              R     A     D     N     M\n"

# Loop through the files in the current directory
for file_name in os.listdir():
    # Check if the file name matches the pattern "wh*RF.SNX"
    if re.match(r"^pt.*RF\.SNX$", file_name):
        # Open the file for reading
        with open(file_name, "r") as f:
            lines = f.readlines()

        # Replace the target lines
        lines = [line.replace(old_line1, new_line1) for line in lines]
        lines = [line.replace(old_line2, new_line2) for line in lines]

        # Get the new file name
        new_file_name = file_name.replace("RF.SNX", "IR.SNX")

        # Write the modified lines to a new file
        with open(new_file_name, "w") as f:
            f.writelines(lines)
