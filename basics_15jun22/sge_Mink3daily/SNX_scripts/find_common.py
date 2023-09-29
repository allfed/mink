import os


def get_file_content(file_path):
    with open(file_path, "r") as f:
        return f.read()


def find_common_part(directory):
    file_paths = [
        os.path.join(directory, file_name)
        for file_name in os.listdir(directory)
        if os.path.isfile(os.path.join(directory, file_name))
        and file_name.endswith(".SNX")
    ]
    if not file_paths:
        print("No .SNX files found in the directory!")
        return

    # Load content of the first file
    common_content = get_file_content(file_paths[0])

    # Compare the common content with the content of each file
    for file_path in file_paths[1:]:
        content = get_file_content(file_path)
        common_content = find_common_sequence(common_content, content)

    if common_content:
        print("Common part among all files:\n", common_content)
    else:
        print("No common part found among the files!")


def find_common_sequence(s1, s2):
    length1, length2 = len(s1), len(s2)
    dp = [[0] * (length2 + 1) for _ in range(length1 + 1)]
    max_len = 0
    end_index = 0

    for i in range(1, length1 + 1):
        for j in range(1, length2 + 1):
            if s1[i - 1] == s2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
                if dp[i][j] > max_len:
                    max_len = dp[i][j]
                    end_index = i
            else:
                dp[i][j] = 0

    if max_len == 0:
        return ""

    return s1[end_index - max_len : end_index]


if __name__ == "__main__":
    directory = input("Enter the directory path: ")
    find_common_part(directory)
