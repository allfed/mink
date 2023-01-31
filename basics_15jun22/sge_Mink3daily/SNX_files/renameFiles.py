import re

# Define an array of the new names
new_names = ["IB0005 ATLANTIC", "IB0002 SEBAGO"]

# Open the file for reading
with open("ptRUSSETBURBANKRF.SNX", "r") as f:
    lines = f.readlines()

# Loop through the lines in the file
for i, line in enumerate(lines):
    # Check if the line starts with " 1 WH "
    if re.match(r"^ 1 PT ", line):
        # Loop through the new names
        for new_name in new_names:
            # Replace "IB1500 MANITOU" with the new name
            print("IB0003 Russet Burbank")
            print(new_name)
            print(new_name.split()[1])
            lines[i] = line.replace("IB0003 Russet Burbank", new_name)
            # Write the modified lines to a new file
            # print(lines)
            with open("wh" + new_name.split()[1] + "RF.SNX", "w") as f:
                f.writelines(lines)
