import re
import os
import sys
import subprocess


def check_if_this_grid_value_is_defined(
    points_in_wth_files, row, col, south, west, nsres, ewres, tolerance=0.0001
):
    # Calculate the center coordinates of the grid cell
    center_south_grid = south + (row + 0.5) * nsres
    center_west_grid = west + (col + 0.5) * ewres

    # Loop through points in weather files
    for point in points_in_wth_files:
        # Check if the point's latitude and longitude match the center of the grid cell with tolerance
        if (
            center_south_grid - nsres / 2 - tolerance
            <= point["lat"]
            <= center_south_grid + nsres / 2 + tolerance
        ) and (
            center_west_grid - ewres / 2 - tolerance
            <= point["lon"]
            <= center_west_grid + ewres / 2 + tolerance
        ):
            return True, point["lat"], point["lon"]
    return False, None, None


def check_if_this_grid_value_is_defined(
    points_in_wth_files, row, col, south, west, nsres, ewres
):
    # Calculate the center coordinates of the grid cell
    center_south_grid = south + (row + 0.5) * nsres
    center_west_grid = west + (col + 0.5) * ewres

    # Loop through points in weather files
    for point in points_in_wth_files:
        # Check if the point's latitude and longitude match the center of the grid cell
        if (
            center_south_grid - nsres / 2
            <= point["lat"]
            <= center_south_grid + nsres / 2
        ) and (
            center_west_grid - ewres / 2 <= point["lon"] <= center_west_grid + ewres / 2
        ):
            if (
                not (
                    center_south_grid - nsres / 100
                    <= point["lat"]
                    <= center_south_grid + nsres / 100
                )
            ) or (
                not (
                    center_west_grid - ewres / 100
                    <= point["lon"]
                    <= center_west_grid + ewres / 100
                )
            ):
                # These should match up...
                print()
                print()
                print()
                print(
                    "ERROR: You haven't defined your north, south, east west pixel boundaries in conformance with your WTH files. You need to be sure north, south, east, and west from your scenarios yaml file are the appropriate outer edges of the pixels, not the center of the pixels (as defined by the lat/lon in your WTH files!). Here's a printout of one such discrepancy."
                )
                print(' point["lat"]')
                print(point["lat"])
                print(' point["lon"]')
                print(point["lon"])
                print("center_south_grid")
                print(center_south_grid)
                print("center_west_grid")
                print(center_west_grid)
                sys.exit(1)

            return True, point["lat"], point["lon"]
    return False, None, None


# Sample function to calculate latitude and longitude (of WTH file) based on row and column
def calculate_lat_lon(row, col, south, west, nsres, ewres):
    lat = south + row * nsres
    lon = west + col * ewres
    return lat, lon


def get_the_ascii(
    points_in_wth_files, north, south, east, west, nsres, ewres, nlats, nlons
):
    # Generate the ASCII file content
    ascii_content = "north: " + str(north) + "\n"
    ascii_content += "south: " + str(south) + "\n"
    ascii_content += "east: " + str(east) + "\n"
    ascii_content += "west: " + str(west) + "\n"
    ascii_content += "rows: " + str(int(nlats)) + "\n"
    ascii_content += "cols: " + str(int(nlons)) + "\n"
    updated_points_in_wth_files = []
    # Generate the data grid rows
    for row in range(int(nlats) - 1, -1, -1):  # Iterate over rows in reverse
        for col in range(int(nlons)):
            is_defined, lat, lon = check_if_this_grid_value_is_defined(
                points_in_wth_files, row, col, south, west, nsres, ewres
            )
            if is_defined:
                value = 1
                updated_points_in_wth_files.append({"lat": lat, "lon": lon})
            else:
                value = "*"
            ascii_content += f"{value} "
        ascii_content += "\n"
    print("")
    print("Here's the ASCII file used to mask out only existing weather files:")
    print("")
    print(ascii_content)
    print("")
    return ascii_content, updated_points_in_wth_files


def get_points_in_wth_files(directory):
    # Create a set of filenames in the directory
    filenames = set(
        [filename for filename in os.listdir(directory) if filename.endswith(".WTH")]
    )
    count = 0
    # just get the text after second underscore from the end (these will be the lat lon numbers)
    # Initialize lists to store latitude and longitude values
    points_in_wth_files = []
    for f in filenames:
        count += 1
        lat = f.rsplit("_")[-2]
        lon = f.rsplit("_")[-1][: -len(".WTH")]
        points_in_wth_files.append({"lat": float(lat), "lon": float(lon)})
    print("count")
    print(count)
    return points_in_wth_files


def check_points_in_directory(directory, points_in_wth_files):
    # Create a set of filenames in the directory
    filenames = set(
        [filename for filename in os.listdir(directory) if filename.endswith(".WTH")]
    )

    # just get the text after second underscore from the end (these will be the lat lon numbers)
    just_latlons = set([])
    for f in filenames:
        just_latlon = "_" + "_".join(f.rsplit("_")[-2:])
        just_latlons.add(just_latlon)
    count = 0
    # Iterate through the points and check if corresponding files exist in the directory
    for point in points_in_wth_files:
        lat = point["lat"]
        lon = point["lon"]
        # Format lat and lon to match the filename pattern
        filename = f"_{lat}_{lon}.WTH"
        if not filename in just_latlons:
            print(f"expected filename: {filename}")
            print("ERROR!")
            print(
                "lat {lat} lon {lon} doesn't exist in weather folder but was added..."
            )
            return False
        count += 1
    print("count check")
    print(count)
    return True


def main(directory, north, south, east, west, nsres, ewres):
    nlats = int(round((north - south) / nsres))
    nlons = int(round((east - west) / ewres))
    points_in_wth_files = get_points_in_wth_files(directory)

    ascii_file, updated_points_in_wth_files = get_the_ascii(
        points_in_wth_files, north, south, east, west, nsres, ewres, nlats, nlons
    )
    if not check_points_in_directory(directory, updated_points_in_wth_files):
        print("ERROR!")
        sys.exit(1)
    r_in_ascii_output = subprocess.run(
        ["r.in.ascii", "input=-", "output=WTH_file_mask", "--overwrite", "--quiet"],
        input=ascii_file,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
    )
    # Optionally print or process the outputs
    if len(r_in_ascii_output.stderr) > 0:
        print(
            "ERROR: Output of r.in.ascii from weather files:", r_in_ascii_output.stderr
        )
        sys.exit(1)

    print("Making image mink/WTH_file_mask.png of the WTH files present")
    quick_display_output = subprocess.run(
        [
            "/mnt/data/basics_15jun22/sge_Mink3daily/export_scripts/quick_display.sh",
            "WTH_file_mask",
            ".",
        ],
        cwd="/mnt/data/basics_15jun22/sge_Mink3daily/export_scripts/",
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
    )
    print("")
    print("WTH files quick_diplay.sh output:")
    print(quick_display_output.stdout)
    print(quick_display_output.stderr)
    # r_info_output = subprocess.run(
    #     ["r.info", "WTH_file_mask"],
    #     stdout=subprocess.PIPE,
    #     stderr=subprocess.PIPE,
    #     universal_newlines=True,
    # )
    # print("r_info_output")
    # print(r_info_output)


if __name__ == "__main__":
    if len(sys.argv) != 8:
        print(
            "Usage: python3 make_weather_file_mask.py weather_folder latitudinal_resolution longitudinal_resolution\n"
            "Example:python3 make_weather_file_mask.py /mnt/data/bias_corrected_control/ -10.3125 -42.1875 151.875 113.125 1.875 1.25"
        )
        sys.exit(1)

    wth_folder = sys.argv[1]
    n = sys.argv[2]
    s = sys.argv[3]
    e = sys.argv[4]
    w = sys.argv[5]
    nsres = sys.argv[6]
    ewres = sys.argv[7]

    main(
        wth_folder,
        float(n),
        float(s),
        float(e),
        float(w),
        float(nsres),
        float(ewres),
    )
