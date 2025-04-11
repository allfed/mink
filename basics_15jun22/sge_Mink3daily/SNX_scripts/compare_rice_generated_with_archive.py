#!/usr/bin/env python3
import os
import difflib

# Adjust these paths so they are correct relative to SNX_scripts/
DIR_GOOD = "../SNX_files/archive/good_rice_SNX_nocomments"
DIR_GEN  = "../SNX_files/generated_SNX_files"

def strip_suffixes(filename):
    """
    Strip off .SNX, and also remove trailing 'IR' or 'RF' if present.
    e.g. 'riceceresfloodedIR.SNX' -> 'riceceresflooded'
         'riK001IR.SNX' -> 'riK001'
         'riCL00IF.SNX' -> 'riCL00IF' (unchanged except .SNX removed)
    """
    if filename.endswith(".SNX"):
        filename = filename[:-4]  # remove the .SNX
    if filename.endswith("IR") or filename.endswith("RF"):
        filename = filename[:-2]
    return filename

def read_clean_lines(filepath):
    """
    Read a file, returning a list of lines with:
      - No blank/whitespace-only lines
      - No lines starting with '!'
      - No lines containing specified strings to ignore
    """
    ignore_patterns = [
        "*EXP.DETAILS:",
        "Ricky Robertson",
        "usually unneccesary",
        "to fill in marked"
    ]
    
    lines = []
    with open(filepath, "r") as f:
        for line in f:
            stripped = line.strip()
            
            # Skip empty or comment lines
            if not stripped:
                continue
            if stripped.startswith('!'):
                continue
                
            # Skip lines containing any of the ignore patterns
            if any(pattern in line for pattern in ignore_patterns):
                continue
                
            # Keep everything else, but strip trailing newline
            lines.append(line.rstrip('\n'))
    
    return lines

def main():
    # 1. Gather SNX files in each directory
    files_good = [f for f in os.listdir(DIR_GOOD) if f.endswith(".SNX")]
    files_gen  = [f for f in os.listdir(DIR_GEN)  if f.endswith(".SNX")]
    
    # 2. Map "base" name -> actual filename for each directory
    map_good = {}
    for fname in files_good:
        base = strip_suffixes(fname)
        map_good[base] = fname
    
    map_gen = {}
    for fname in files_gen:
        base = strip_suffixes(fname)
        map_gen[base] = fname
    
    # 3. Find common base names
    common_bases = set(map_good.keys()).intersection(set(map_gen.keys()))
    if not common_bases:
        print("No matching files found!")
        return
    
    # 4. For each matching base, read lines, filter them, and show the diff
    for base in sorted(common_bases):
        path_good = os.path.join(DIR_GOOD, map_good[base])
        path_gen  = os.path.join(DIR_GEN,  map_gen[base])
        
        lines_good = read_clean_lines(path_good)
        lines_gen  = read_clean_lines(path_gen)
        
        # Generate the diff with context
        diff = difflib.unified_diff(
            lines_good,
            lines_gen,
            fromfile=path_good,
            tofile=path_gen,
            lineterm='',
            n=1  # Get one line of context
        )
        
        # Process the diff output
        diff_lines = list(diff)
        filtered_diff = []
        
        i = 0
        in_change_block = False
        
        while i < len(diff_lines):
            line = diff_lines[i]
            
            # Keep header lines
            if line.startswith('---') or line.startswith('+++') or line.startswith('@@'):
                filtered_diff.append(line)
                in_change_block = False
                i += 1
                continue
            
            # If it's a change line (+ or -)
            if line.startswith('+') or line.startswith('-'):
                # If we're starting a new change block
                if not in_change_block:
                    # Look backward for the most recent context line
                    j = i - 1
                    while j >= 0 and not diff_lines[j].startswith(' '):
                        j -= 1
                    
                    # If found, add it
                    if j >= 0 and diff_lines[j].startswith(' '):
                        filtered_diff.append(diff_lines[j])
                    
                    in_change_block = True
                
                # Add the change line
                filtered_diff.append(line)
            else:
                # It's a context line, we're no longer in a change block
                in_change_block = False
            
            i += 1
        
        # Print results
        if filtered_diff:
            print("\n=========================================")
            print(f"DIFF for {map_good[base]} vs {map_gen[base]}")
            print("=========================================")
            for line in filtered_diff:
                print(line)

if __name__ == "__main__":
    main()