"""
This script loads a bunch of csv's if they exist and merges the relevant columns for:
    country code
    cropping area (ha)  using the sum
    production (kg) using the sum
    planting month (index, from 0 to 11) using the mode
    days to maturity using the mean
into a single csv file.
If data are missing, then the column is skipped.
"""

import csv
import sys
import os


def load_data(filename, key_column, fieldnames=None, delimiter=","):
    """
    Load data from a CSV into a dictionary based on a specific key column.
    If the file doesn't exist or is empty, return None.
    """
    if not os.path.exists(filename) or os.path.getsize(filename) == 0:
        return None

    data = {}
    with open(filename, "r") as f:
        reader = csv.DictReader(f, fieldnames=fieldnames, delimiter=delimiter)
        for row in reader:
            if fieldnames and row[key_column] == fieldnames[0]:
                continue  # Skip the header row if fieldnames are provided
            key = row[key_column]
            data[key] = row
    return data


def main(input_filename):
    """input_filename
    Load data from multiple CSVs, unify the data, and save to a new CSV.
    """
    # Load data
    country_cat = load_data(
        "country_names_for_each_category.csv",
        "zone",
        ["zone", "ISO_3DIGIT"],
        delimiter=" ",
    )
    pm_stats = load_data(
        "country_pm_stats.csv",
        "zone",
        ["zone", "mode"],
        delimiter=" ",
    )
    days_to_maturity = load_data("country_days_to_maturity_mean.csv", "zone")
    production_sum = load_data("country_production_sum.csv", "zone")
    crop_area_sum = load_data("cropland_sum.csv", "zone")

    # Create a unified dictionary
    unified_data = {}

    # Start with the keys from the first dictionary, then get the intersection with the keys from the others
    common_categories = set(country_cat.keys()) if country_cat else set()
    # print("pm_stats.keys()")
    # print(pm_stats.keys())
    # print("days_to_maturity.keys()")
    # print(days_to_maturity.keys())
    # print("production_sum.keys()")
    # print(production_sum.keys())
    # print("crop_area_sum.keys()")
    # print(crop_area_sum.keys())
    if pm_stats:
        common_categories &= set(pm_stats.keys())
    if days_to_maturity:
        common_categories &= set(days_to_maturity.keys())
    if production_sum:
        common_categories &= set(production_sum.keys())
    if crop_area_sum:
        common_categories &= set(crop_area_sum.keys())

    # If there are no common categories across the files, terminate the operation
    if not common_categories:
        print("Error: No common categories found across the CSV files.")
        return

    # Build the unified dictionary using only the common categories
    for category in common_categories:
        unified_data[category] = {
            "ISO_3DIGIT": country_cat[category]["ISO_3DIGIT"] if country_cat else "",
            "Planting_Month_Mode": pm_stats[category]["mode"] if pm_stats else "",
            "Days_to_Maturity_Mean": days_to_maturity[category]["mean"]
            if days_to_maturity
            else "",
            "Production_Sum": production_sum[category]["sum"] if production_sum else "",
            "Crop_Area_Sum": crop_area_sum[category]["sum"] if crop_area_sum else "",
        }

    # Write the unified data to a new CSV
    with open(input_filename, "w", newline="") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=[
                "ISO_3DIGIT",
                "Planting_Month_Mode",
                "Days_to_Maturity_Mean",
                "Production_Sum",
                "Crop_Area_Sum",
            ],
        )
        writer.writeheader()
        for data in unified_data.values():
            writer.writerow(data)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(
            "Usage: python3 merge_all_those_pesky_by_country_csvs.py <input_filename>"
        )
        sys.exit(1)

    input_filename = sys.argv[1]
    main(input_filename)
