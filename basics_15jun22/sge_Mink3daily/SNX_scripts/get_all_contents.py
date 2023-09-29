# can be used to print out the differences between a set of SNX files (can be useful if you have a bunch of data to add in and consistently formatted SNX files, or just to see if the generation of a bunch of SNX files seems to have worked)

import os

SNX_file_directory = "../SNX_files/generated_SNX_files"


def get_file_lines(file_path):
    with open(file_path, "r") as f:
        lines = f.readlines()
        # Filter out lines that start with '!' or are empty
        return [line for line in lines if not line.startswith("!") and line.strip()]


def find_common_and_unique_parts(directory):
    file_paths = [
        os.path.join(directory, file_name)
        for file_name in os.listdir(directory)
        if os.path.isfile(os.path.join(directory, file_name))
        and file_name.endswith(".SNX")
    ]

    if not file_paths:
        print("No .SNX files found in the directory!")
        return

    # Load lines of all files into memory
    all_lines = [get_file_lines(fp) for fp in file_paths]

    # Iterate through lines of each file simultaneously
    for lines in zip(*all_lines):
        if all(line == lines[0] for line in lines):
            # All lines are the same
            print(lines[0], end="")
        else:
            for file_path, line in zip(file_paths, lines):
                print(f"FILENAME {os.path.basename(file_path)}:\n{line}", end="")


if __name__ == "__main__":
    find_common_and_unique_parts(SNX_file_directory)
