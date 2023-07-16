"""
This script takes a directory path as an argument and uses the GitPython module to interact with the Git repository
where the script is executed. The script scans the Git history of the given directory, starting from the earliest
commit, and tracks the addition of any immediate subdirectories within that directory over time.

The script outputs the commit hash and name of each new subdirectory added to the specified directory, as the changes
appear in chronological order.

This script is useful for tracking the evolution of a directory structure over time within a Git repository, 
specifically by monitoring the addition of new subdirectories. This can be especially helpful in complex projects 
where directories are added and removed over the course of development.

Example usage: 
python this_script.py directory/target_directory

More pertinent:
python this_script.py basics_15jun22/sge_Mink3daily

Please replace 'this_script.py' with the actual name of this script, and 'target_directory' with the directory you 
want to track in your Git repository.
"""
# Import necessary modules
import git
import os


# Define a function to get differences between two commits
def get_commit_diff(commit1, commit2):
    # Generate a diff between the two commits
    diff_index = commit1.diff(commit2)
    # Return a set of all paths where a file was added ('A' stands for 'Added')
    return {diff.a_path for diff in diff_index.iter_change_type("A")}


# Define a function to get a subdirectory from a path
def get_subdirectory(path, directory):
    # Check if the path starts with the directory
    if path.startswith(directory):
        # If so, strip the directory from the path
        sub_path = path[len(directory) :]
        # If there's a slash in the remaining path, return everything before it (the subdirectory name)
        if "/" in sub_path:
            return sub_path[: sub_path.index("/")]
        # If there's no slash, return the remaining path
        else:
            return sub_path
    # If the path doesn't start with the directory, return None
    return None


def main(directory):
    # Ensure the directory ends with a slash
    directory = directory.strip("/") + "/"
    # Open the current Git repository
    repo = git.Repo(".", search_parent_directories=True)

    # Get a list of all commits that affected the directory and reverse the list
    commits = list(repo.iter_commits(paths=directory))
    commits.reverse()

    # Initialize an empty set to store seen subdirectories
    seen = set()
    # Loop over pairs of commits
    for i in range(len(commits) - 1):
        commit1 = commits[i]
        commit2 = commits[i + 1]
        # Get the set of files that were added between these two commits
        added_files = get_commit_diff(commit1, commit2)

        # Loop over the added files
        for file in added_files:
            # Get the subdirectory of each file
            subdirectory = get_subdirectory(file, directory)
            # If the subdirectory is not None and has not been seen before, print it and add it to the seen set
            if subdirectory and subdirectory not in seen:
                seen.add(subdirectory)
                print(f"In commit {commit1.hexsha}, {subdirectory} was added")


if __name__ == "__main__":
    import sys

    # If there are not exactly two arguments (the script name and the directory), print a usage message and exit
    if len(sys.argv) != 2:
        print(f"Usage: python {sys.argv[0]} directory")
        sys.exit(1)

    # Get the directory from the command line arguments and call the main function
    directory = sys.argv[1]
    main(directory)
