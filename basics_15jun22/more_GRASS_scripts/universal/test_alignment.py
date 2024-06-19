"""
Script runs through all weather files in a folder to check if they seem good
You can run with ACTUALLY_ALTER set to True to try to correct the files (Although, this will only work in a subset of inorrect alignments)
"""

import os
import sys

NUM_CHARS_IGNORED_AT_BEGINNING = 2
ACTUALLY_ALTER = False


def find_indices_of_last_char_before_whitespace(line):
    indices = []
    for i in range(1, len(line)):
        if line[i].isspace() and not line[i - 1].isspace():
            indices.append(i - 1)
    return indices


def check_alignment(file_path):
    with open(file_path, "r") as file:
        lines = file.readlines()
        if len(lines) < 2:
            print(f"ERROR: File {file_path} does not have enough lines to check.")
            sys.exit(1)

        # Determine header and positions
        header_line = lines[0]
        data_line = lines[1]
        header_positions = find_indices_of_last_char_before_whitespace(header_line[2:])
        data_positions = find_indices_of_last_char_before_whitespace(data_line[2:])

        if header_positions != data_positions:
            # Print header and data positions for debugging
            print(f"File: {file_path}")
            print(f"header positions: {header_positions}")
            print(f"data positions: {data_positions}")
            print()
            return False
        return True


def first_difference_index(arr1, arr2):
    min_length = min(len(arr1), len(arr2))
    for i in range(min_length):
        if arr1[i] != arr2[i]:
            return i
    if len(arr1) != len(arr2):
        return min_length
    return -1  # Return -1 if arrays are identical in the common length


def remove_extra_space(file_path):
    with open(file_path, "r") as file:
        lines = file.readlines()

    # Determine header and positions
    header_line = lines[0]
    data_line = lines[1]
    header_positions = find_indices_of_last_char_before_whitespace(
        header_line[NUM_CHARS_IGNORED_AT_BEGINNING:]
    )
    data_positions = find_indices_of_last_char_before_whitespace(
        data_line[NUM_CHARS_IGNORED_AT_BEGINNING:]
    )

    first_index = first_difference_index(header_positions, data_positions)
    if first_index == -1:
        print("ERROR: Lines are identical in relevant parts.")
        return

    end_of_column_before = first_index - 1
    actual_whitespace_to_delete = (
        data_positions[end_of_column_before] + NUM_CHARS_IGNORED_AT_BEGINNING + 1
    )

    # Print full second line
    print("Full second line:")
    print(data_line)

    # Print character to be removed
    char_to_remove = data_line[actual_whitespace_to_delete]
    print(f"Character to be removed: '{char_to_remove}'")

    # Print 5 surrounding characters (5 before and 5 after)
    start_index = max(0, actual_whitespace_to_delete - 5)
    end_index = min(len(data_line), actual_whitespace_to_delete + 6)
    surrounding_chars = data_line[start_index:end_index]
    print(f"Surrounding characters: '{surrounding_chars}'")

    new_row = (
        data_line[:actual_whitespace_to_delete]
        + data_line[actual_whitespace_to_delete + 1 :]
    )
    print("new_row")
    print(new_row)
    new_data_positions = find_indices_of_last_char_before_whitespace(
        new_row[NUM_CHARS_IGNORED_AT_BEGINNING:]
    )
    if new_data_positions != header_positions:
        print("ERROR!!!")
        print("new_data_positions")
        print(new_data_positions)
        print("header_positions")
        print(header_positions)
        print("actual_whitespace_to_delete")
        print(actual_whitespace_to_delete)
        print("data_line[:actual_whitespace_to_delete]")
        print(data_line[:actual_whitespace_to_delete])
        print("data_line[actual_whitespace_to_delete + 1 :]")
        print(data_line[actual_whitespace_to_delete + 1 :])
        sys.exit(1)

    if ACTUALLY_ALTER:
        lines[1] = new_row
        with open(file_path, "w") as file:
            file.writelines(lines)
            print("ALTERED!")


def main():
    if len(sys.argv) < 2:
        print("ERROR: Please provide the directory path as the first argument.")
        sys.exit(1)

    target_dir = sys.argv[1]
    if not os.path.isdir(target_dir):
        print(f"ERROR: The provided path '{target_dir}' is not a directory.")
        sys.exit(1)

    at_least_one_is_wrong = False
    for file_name in os.listdir(target_dir):
        if file_name.endswith(".WTH"):
            file_path = os.path.join(target_dir, file_name)
            if not check_alignment(file_path):
                remove_extra_space(file_path)
                at_least_one_is_wrong = True

    if at_least_one_is_wrong:
        # try again now that we've corrected things
        if ACTUALLY_ALTER:
            at_least_one_is_wrong = False
            for file_name in os.listdir(target_dir):
                if file_name.endswith(".WTH"):
                    file_path = os.path.join(target_dir, file_name)
                    if not check_alignment(file_path):
                        remove_extra_space(file_path)
                        at_least_one_is_wrong = True
        if not at_least_one_is_wrong:
            print(
                "FINAL RESULT: The weather files look okay, at least in terms of alignment of first two lines :)"
            )
        else:
            # Get the absolute path of the current script
            current_script_path = os.path.abspath(__file__)

            # Print the path
            print(
                "FINAL RESULT: ERROR! \n\nAt least one file is incorrectly formatted.\n",
                f"If the file is roughly correctly formatted, then temporarily set this script (located at {current_script_path}) with ACTUALLY_ALTER set to True, then try again (be sure to set it back to False once you run once with it set to true)?\n Although ideally you need to change the way you're generating weather files at the source to not have this issue.",
            )
            print(
                "Here's an example of what a the alignments we're testing for look like:"
            )
            print(
                "incorrect:\n",
                "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT\n"
                "  MRAR   64.688   98.750     0  -17.3  33.1 -99.9 -99.9",
            )

            print(
                "correct:\n",
                "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT\n",
                "  MRAR    0.938    0.625  60.0  22.1  11.0 -99.9 -99.9",
            )
            sys.exit(1)

    else:
        print(
            "FINAL RESULT: The weather files look okay, at least in terms of alignment of first two lines :)"
        )


if __name__ == "__main__":
    main()
