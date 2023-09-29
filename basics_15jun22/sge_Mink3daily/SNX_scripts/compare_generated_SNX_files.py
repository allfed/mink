# compare between folders, to see if lines in snx files are the same, and if the snx files generated match the snx files they were based on

import os

SNX_file_directory = "../SNX_files/"


def read_file(file_path):
    with open(file_path, "r") as f:
        lines = f.readlines()
        # Filter out lines that start with "!" and remove newline characters
        return [
            line.strip() for line in lines if not line.startswith("!") and line.strip()
        ]


def compare_files(file1, file2):
    content1 = read_file(file1)
    content2 = read_file(file2)

    differences = []
    for i, (line1, line2) in enumerate(zip(content1, content2)):
        if line1 != line2:
            differences.append((i, line1, line2))

    return differences


def main():
    dir1 = f"{SNX_file_directory}generated_SNX_files"
    dir2 = f"{SNX_file_directory}SNX_files_I_used_to_make_SNX_generating_pattern"

    # Ensure both directories exist
    if not os.path.exists(dir1) or not os.path.exists(dir2):
        print("One or both directories do not exist!")
        return

    files_in_dir1 = set(os.listdir(dir1))
    files_in_dir2 = set(os.listdir(dir2))

    # Ensure that the two directories have the same filenames
    if files_in_dir1 != files_in_dir2:
        print("Directories do not have the same set of filenames!")
        return

    for filename in files_in_dir1:
        file1_path = os.path.join(dir1, filename)
        file2_path = os.path.join(dir2, filename)

        differences = compare_files(file1_path, file2_path)
        if differences:
            print(f"Differences in {filename}:")
            for i, line1, line2 in differences:
                print(f"Line {i + 1}:")
                print(f"    {dir1}: {line1.strip()}")
                print(f"    {dir2}: {line2.strip()}")
        else:
            print(f"No differences in {filename}")


if __name__ == "__main__":
    main()
