import re
import os
import sys
import subprocess


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
            if center_south_grid - nsres / 2
            if (not (center_south_grid - nsres / 10 <= point["lat"] <= center_south_grid + nsres / 10))
                or (not (center_west_grid - ewres / 2 <= point["lon"] <= center_west_grid + ewres / 2)):
                # These should match up...
                print()
                print()
                print()
                print("ERROR: You haven't defined your north, south, east west pixel boundaries in conformance with your WTH files. You need to be sure north, south, east, and west from your scenarios yaml file are the appropriate outer edges of the pixels, not the center of the pixels (as defined by the lat/lon in your WTH files!). Here's a printout of the discrepancy.")
                print(' point["lat"]')
                print(point["lat"])
                print(' point["lon"]')
                print(point["lon"])
                print("center_south_grid")
                print(center_south_grid)
                print("center_west_grid")
                print(center_west_grid)
                sys.exit(1)


            return True
    return False


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

    # Generate the data grid rows
    for row in range(int(nlats) - 1, -1, -1):  # Iterate over rows in reverse
        for col in range(int(nlons)):
            is_defined = check_if_this_grid_value_is_defined(
                points_in_wth_files, row, col, south, west, nsres, ewres
            )
            if is_defined:
                value = 1
            else:
                value = "*"

            ascii_content += f"{value} "
        ascii_content += "\n"
    print("")
    print("Here's the ASCII file used to mask out only existing weather files:")
    print("")
    print(ascii_content)
    print("")
    return ascii_content


def get_points_in_wth_files(directory):
    # Initialize lists to store latitude and longitude values
    points_in_wth_files = []

    # Define a regex pattern to extract lat and lon from filenames
    pattern = re.compile(r".*_([-+]?[0-9]*\.?[0-9]+)_([-+]?[0-9]*\.?[0-9]+)\.WTH")

    # Iterate through files in the directory
    for filename in os.listdir(directory):
        match = pattern.match(filename)
        if match:
            lat = float(match.group(1))
            lon = float(match.group(2))
            points_in_wth_files.append({"lat": lat, "lon": lon})

    # points_in_wth_files = [
    #     {"lat": -12.1875, "lon": 131.875},
    #     {"lat": -12.1875, "lon": 133.125},
    #     {"lat": -14.0625, "lon": 130.625},
    #     {"lat": -14.0625, "lon": 131.875},
    #     {"lat": -14.0625, "lon": 133.125},
    #     {"lat": -14.0625, "lon": 134.375},
    #     {"lat": -14.0625, "lon": 135.625},
    #     {"lat": -15.9375, "lon": 125.625},
    #     {"lat": -15.9375, "lon": 126.875},
    #     {"lat": -15.9375, "lon": 128.125},
    #     {"lat": -15.9375, "lon": 129.375},
    #     {"lat": -15.9375, "lon": 130.625},
    #     {"lat": -15.9375, "lon": 131.875},
    #     {"lat": -15.9375, "lon": 133.125},
    #     {"lat": -15.9375, "lon": 134.375},
    #     {"lat": -15.9375, "lon": 135.625},
    #     {"lat": -15.9375, "lon": 143.125},
    #     {"lat": -15.9375, "lon": 144.375},
    #     {"lat": -15.9375, "lon": 145.625},
    #     {"lat": -17.8125, "lon": 123.125},
    #     {"lat": -17.8125, "lon": 124.375},
    #     {"lat": -17.8125, "lon": 125.625},
    #     {"lat": -17.8125, "lon": 126.875},
    #     {"lat": -17.8125, "lon": 128.125},
    #     {"lat": -17.8125, "lon": 129.375},
    #     {"lat": -17.8125, "lon": 130.625},
    #     {"lat": -17.8125, "lon": 131.875},
    #     {"lat": -17.8125, "lon": 133.125},
    #     {"lat": -17.8125, "lon": 134.375},
    #     {"lat": -17.8125, "lon": 135.625},
    #     {"lat": -17.8125, "lon": 136.875},
    #     {"lat": -17.8125, "lon": 138.125},
    #     {"lat": -17.8125, "lon": 139.375},
    #     {"lat": -17.8125, "lon": 141.875},
    #     {"lat": -17.8125, "lon": 143.125},
    #     {"lat": -17.8125, "lon": 144.375},
    #     {"lat": -17.8125, "lon": 145.625},
    #     {"lat": -19.6875, "lon": 123.125},
    #     {"lat": -19.6875, "lon": 124.375},
    #     {"lat": -19.6875, "lon": 125.625},
    #     {"lat": -19.6875, "lon": 126.875},
    #     {"lat": -19.6875, "lon": 128.125},
    #     {"lat": -19.6875, "lon": 129.375},
    #     {"lat": -19.6875, "lon": 130.625},
    #     {"lat": -19.6875, "lon": 131.875},
    #     {"lat": -19.6875, "lon": 133.125},
    #     {"lat": -19.6875, "lon": 134.375},
    #     {"lat": -19.6875, "lon": 135.625},
    #     {"lat": -19.6875, "lon": 136.875},
    #     {"lat": -19.6875, "lon": 138.125},
    #     {"lat": -19.6875, "lon": 139.375},
    #     {"lat": -19.6875, "lon": 140.625},
    #     {"lat": -19.6875, "lon": 141.875},
    #     {"lat": -19.6875, "lon": 143.125},
    #     {"lat": -19.6875, "lon": 144.375},
    #     {"lat": -19.6875, "lon": 145.625},
    #     {"lat": -19.6875, "lon": 146.875},
    #     {"lat": -21.5625, "lon": 119.375},
    #     {"lat": -21.5625, "lon": 120.625},
    #     {"lat": -21.5625, "lon": 121.875},
    #     {"lat": -21.5625, "lon": 123.125},
    #     {"lat": -21.5625, "lon": 124.375},
    #     {"lat": -21.5625, "lon": 125.625},
    #     {"lat": -21.5625, "lon": 126.875},
    #     {"lat": -21.5625, "lon": 128.125},
    #     {"lat": -21.5625, "lon": 129.375},
    #     {"lat": -21.5625, "lon": 130.625},
    #     {"lat": -21.5625, "lon": 131.875},
    #     {"lat": -21.5625, "lon": 133.125},
    #     {"lat": -21.5625, "lon": 134.375},
    #     {"lat": -21.5625, "lon": 135.625},
    #     {"lat": -21.5625, "lon": 136.875},
    #     {"lat": -21.5625, "lon": 138.125},
    #     {"lat": -21.5625, "lon": 139.375},
    #     {"lat": -21.5625, "lon": 140.625},
    #     {"lat": -21.5625, "lon": 141.875},
    #     {"lat": -21.5625, "lon": 143.125},
    #     {"lat": -21.5625, "lon": 144.375},
    #     {"lat": -21.5625, "lon": 145.625},
    #     {"lat": -21.5625, "lon": 146.875},
    #     {"lat": -21.5625, "lon": 148.125},
    #     {"lat": -23.4375, "lon": 114.375},
    #     {"lat": -23.4375, "lon": 115.625},
    #     {"lat": -23.4375, "lon": 116.875},
    #     {"lat": -23.4375, "lon": 118.125},
    #     {"lat": -23.4375, "lon": 119.375},
    #     {"lat": -23.4375, "lon": 120.625},
    #     {"lat": -23.4375, "lon": 121.875},
    #     {"lat": -23.4375, "lon": 123.125},
    #     {"lat": -23.4375, "lon": 124.375},
    #     {"lat": -23.4375, "lon": 125.625},
    #     {"lat": -23.4375, "lon": 126.875},
    #     {"lat": -23.4375, "lon": 128.125},
    #     {"lat": -23.4375, "lon": 130.625},
    #     {"lat": -23.4375, "lon": 131.875},
    #     {"lat": -23.4375, "lon": 133.125},
    #     {"lat": -23.4375, "lon": 134.375},
    #     {"lat": -23.4375, "lon": 135.625},
    #     {"lat": -23.4375, "lon": 136.875},
    #     {"lat": -23.4375, "lon": 138.125},
    #     {"lat": -23.4375, "lon": 139.375},
    #     {"lat": -23.4375, "lon": 140.625},
    #     {"lat": -23.4375, "lon": 141.875},
    #     {"lat": -23.4375, "lon": 143.125},
    #     {"lat": -23.4375, "lon": 144.375},
    #     {"lat": -23.4375, "lon": 145.625},
    #     {"lat": -23.4375, "lon": 146.875},
    #     {"lat": -23.4375, "lon": 148.125},
    #     {"lat": -23.4375, "lon": 149.375},
    #     {"lat": -23.4375, "lon": 150.625},
    #     {"lat": -25.3125, "lon": 114.375},
    #     {"lat": -25.3125, "lon": 115.625},
    #     {"lat": -25.3125, "lon": 116.875},
    #     {"lat": -25.3125, "lon": 118.125},
    #     {"lat": -25.3125, "lon": 119.375},
    #     {"lat": -25.3125, "lon": 120.625},
    #     {"lat": -25.3125, "lon": 121.875},
    #     {"lat": -25.3125, "lon": 123.125},
    #     {"lat": -25.3125, "lon": 124.375},
    #     {"lat": -25.3125, "lon": 125.625},
    #     {"lat": -25.3125, "lon": 126.875},
    #     {"lat": -25.3125, "lon": 128.125},
    #     {"lat": -25.3125, "lon": 129.375},
    #     {"lat": -25.3125, "lon": 130.625},
    #     {"lat": -25.3125, "lon": 131.875},
    #     {"lat": -25.3125, "lon": 133.125},
    #     {"lat": -25.3125, "lon": 134.375},
    #     {"lat": -25.3125, "lon": 135.625},
    #     {"lat": -25.3125, "lon": 136.875},
    #     {"lat": -25.3125, "lon": 138.125},
    #     {"lat": -25.3125, "lon": 139.375},
    #     {"lat": -25.3125, "lon": 140.625},
    #     {"lat": -25.3125, "lon": 141.875},
    #     {"lat": -25.3125, "lon": 143.125},
    #     {"lat": -25.3125, "lon": 144.375},
    #     {"lat": -25.3125, "lon": 145.625},
    #     {"lat": -25.3125, "lon": 146.875},
    #     {"lat": -25.3125, "lon": 148.125},
    #     {"lat": -25.3125, "lon": 149.375},
    #     {"lat": -25.3125, "lon": 150.625},
    #     {"lat": -25.3125, "lon": 151.875},
    #     {"lat": -27.1875, "lon": 114.375},
    #     {"lat": -27.1875, "lon": 115.625},
    #     {"lat": -27.1875, "lon": 116.875},
    #     {"lat": -27.1875, "lon": 118.125},
    #     {"lat": -27.1875, "lon": 119.375},
    #     {"lat": -27.1875, "lon": 120.625},
    #     {"lat": -27.1875, "lon": 121.875},
    #     {"lat": -27.1875, "lon": 123.125},
    #     {"lat": -27.1875, "lon": 124.375},
    #     {"lat": -27.1875, "lon": 125.625},
    #     {"lat": -27.1875, "lon": 126.875},
    #     {"lat": -27.1875, "lon": 128.125},
    #     {"lat": -27.1875, "lon": 129.375},
    #     {"lat": -27.1875, "lon": 130.625},
    #     {"lat": -27.1875, "lon": 131.875},
    #     {"lat": -27.1875, "lon": 133.125},
    #     {"lat": -27.1875, "lon": 134.375},
    #     {"lat": -27.1875, "lon": 135.625},
    #     {"lat": -27.1875, "lon": 136.875},
    #     {"lat": -27.1875, "lon": 138.125},
    #     {"lat": -27.1875, "lon": 139.375},
    #     {"lat": -27.1875, "lon": 140.625},
    #     {"lat": -27.1875, "lon": 141.875},
    #     {"lat": -27.1875, "lon": 143.125},
    #     {"lat": -27.1875, "lon": 144.375},
    #     {"lat": -27.1875, "lon": 145.625},
    #     {"lat": -27.1875, "lon": 146.875},
    #     {"lat": -27.1875, "lon": 148.125},
    #     {"lat": -27.1875, "lon": 149.375},
    #     {"lat": -27.1875, "lon": 150.625},
    #     {"lat": -27.1875, "lon": 151.875},
    #     {"lat": -29.0625, "lon": 115.625},
    #     {"lat": -29.0625, "lon": 116.875},
    #     {"lat": -29.0625, "lon": 118.125},
    #     {"lat": -29.0625, "lon": 119.375},
    #     {"lat": -29.0625, "lon": 120.625},
    #     {"lat": -29.0625, "lon": 121.875},
    #     {"lat": -29.0625, "lon": 123.125},
    #     {"lat": -29.0625, "lon": 124.375},
    #     {"lat": -29.0625, "lon": 125.625},
    #     {"lat": -29.0625, "lon": 126.875},
    #     {"lat": -29.0625, "lon": 128.125},
    #     {"lat": -29.0625, "lon": 129.375},
    #     {"lat": -29.0625, "lon": 130.625},
    #     {"lat": -29.0625, "lon": 131.875},
    #     {"lat": -29.0625, "lon": 133.125},
    #     {"lat": -29.0625, "lon": 134.375},
    #     {"lat": -29.0625, "lon": 135.625},
    #     {"lat": -29.0625, "lon": 136.875},
    #     {"lat": -29.0625, "lon": 138.125},
    #     {"lat": -29.0625, "lon": 139.375},
    #     {"lat": -29.0625, "lon": 140.625},
    #     {"lat": -29.0625, "lon": 141.875},
    #     {"lat": -29.0625, "lon": 143.125},
    #     {"lat": -29.0625, "lon": 144.375},
    #     {"lat": -29.0625, "lon": 145.625},
    #     {"lat": -29.0625, "lon": 146.875},
    #     {"lat": -29.0625, "lon": 148.125},
    #     {"lat": -29.0625, "lon": 149.375},
    #     {"lat": -29.0625, "lon": 150.625},
    #     {"lat": -29.0625, "lon": 151.875},
    #     {"lat": -30.9375, "lon": 115.625},
    #     {"lat": -30.9375, "lon": 116.875},
    #     {"lat": -30.9375, "lon": 118.125},
    #     {"lat": -30.9375, "lon": 119.375},
    #     {"lat": -30.9375, "lon": 120.625},
    #     {"lat": -30.9375, "lon": 121.875},
    #     {"lat": -30.9375, "lon": 123.125},
    #     {"lat": -30.9375, "lon": 124.375},
    #     {"lat": -30.9375, "lon": 125.625},
    #     {"lat": -30.9375, "lon": 126.875},
    #     {"lat": -30.9375, "lon": 128.125},
    #     {"lat": -30.9375, "lon": 129.375},
    #     {"lat": -30.9375, "lon": 130.625},
    #     {"lat": -30.9375, "lon": 131.875},
    #     {"lat": -30.9375, "lon": 133.125},
    #     {"lat": -30.9375, "lon": 134.375},
    #     {"lat": -30.9375, "lon": 135.625},
    #     {"lat": -30.9375, "lon": 136.875},
    #     {"lat": -30.9375, "lon": 138.125},
    #     {"lat": -30.9375, "lon": 139.375},
    #     {"lat": -30.9375, "lon": 140.625},
    #     {"lat": -30.9375, "lon": 141.875},
    #     {"lat": -30.9375, "lon": 143.125},
    #     {"lat": -30.9375, "lon": 144.375},
    #     {"lat": -30.9375, "lon": 145.625},
    #     {"lat": -30.9375, "lon": 146.875},
    #     {"lat": -30.9375, "lon": 148.125},
    #     {"lat": -30.9375, "lon": 149.375},
    #     {"lat": -30.9375, "lon": 150.625},
    #     {"lat": -30.9375, "lon": 151.875},
    #     {"lat": -32.8125, "lon": 116.875},
    #     {"lat": -32.8125, "lon": 118.125},
    #     {"lat": -32.8125, "lon": 119.375},
    #     {"lat": -32.8125, "lon": 120.625},
    #     {"lat": -32.8125, "lon": 121.875},
    #     {"lat": -32.8125, "lon": 123.125},
    #     {"lat": -32.8125, "lon": 124.375},
    #     {"lat": -32.8125, "lon": 125.625},
    #     {"lat": -32.8125, "lon": 126.875},
    #     {"lat": -32.8125, "lon": 128.125},
    #     {"lat": -32.8125, "lon": 133.125},
    #     {"lat": -32.8125, "lon": 134.375},
    #     {"lat": -32.8125, "lon": 135.625},
    #     {"lat": -32.8125, "lon": 136.875},
    #     {"lat": -32.8125, "lon": 138.125},
    #     {"lat": -32.8125, "lon": 139.375},
    #     {"lat": -32.8125, "lon": 140.625},
    #     {"lat": -32.8125, "lon": 141.875},
    #     {"lat": -32.8125, "lon": 143.125},
    #     {"lat": -32.8125, "lon": 144.375},
    #     {"lat": -32.8125, "lon": 145.625},
    #     {"lat": -32.8125, "lon": 146.875},
    #     {"lat": -32.8125, "lon": 148.125},
    #     {"lat": -32.8125, "lon": 149.375},
    #     {"lat": -32.8125, "lon": 150.625},
    #     {"lat": -32.8125, "lon": 151.875},
    #     {"lat": -34.6875, "lon": 115.625},
    #     {"lat": -34.6875, "lon": 116.875},
    #     {"lat": -34.6875, "lon": 118.125},
    #     {"lat": -34.6875, "lon": 119.375},
    #     {"lat": -34.6875, "lon": 120.625},
    #     {"lat": -34.6875, "lon": 121.875},
    #     {"lat": -34.6875, "lon": 123.125},
    #     {"lat": -34.6875, "lon": 124.375},
    #     {"lat": -34.6875, "lon": 135.625},
    #     {"lat": -34.6875, "lon": 136.875},
    #     {"lat": -34.6875, "lon": 139.375},
    #     {"lat": -34.6875, "lon": 140.625},
    #     {"lat": -34.6875, "lon": 141.875},
    #     {"lat": -34.6875, "lon": 143.125},
    #     {"lat": -34.6875, "lon": 144.375},
    #     {"lat": -34.6875, "lon": 145.625},
    #     {"lat": -34.6875, "lon": 146.875},
    #     {"lat": -34.6875, "lon": 148.125},
    #     {"lat": -34.6875, "lon": 149.375},
    #     {"lat": -34.6875, "lon": 150.625},
    #     {"lat": -34.6875, "lon": 151.875},
    #     {"lat": -36.5625, "lon": 138.125},
    #     {"lat": -36.5625, "lon": 140.625},
    #     {"lat": -36.5625, "lon": 141.875},
    #     {"lat": -36.5625, "lon": 143.125},
    #     {"lat": -36.5625, "lon": 144.375},
    #     {"lat": -36.5625, "lon": 145.625},
    #     {"lat": -36.5625, "lon": 146.875},
    #     {"lat": -36.5625, "lon": 148.125},
    #     {"lat": -36.5625, "lon": 149.375},
    #     {"lat": -36.5625, "lon": 150.625},
    #     {"lat": -38.4375, "lon": 140.625},
    #     {"lat": -38.4375, "lon": 141.875},
    #     {"lat": -38.4375, "lon": 143.125},
    #     {"lat": -38.4375, "lon": 144.375},
    #     {"lat": -38.4375, "lon": 145.625},
    #     {"lat": -38.4375, "lon": 146.875},
    #     {"lat": -38.4375, "lon": 148.125},
    #     {"lat": -38.4375, "lon": 149.375},
    #     {"lat": -42.1875, "lon": 145.625},
    #     {"lat": -42.1875, "lon": 146.875},
    #     {"lat": -42.1875, "lon": 148.125},
    # ]

    return points_in_wth_files


def main(directory, north, south, east, west, nsres, ewres):
    nlats = int(round((north - south) / nsres))
    nlons = int(round((east - west) / ewres))
    points_in_wth_files = get_points_in_wth_files(directory)

    ascii_file = get_the_ascii(
        points_in_wth_files, north, south, east, west, nsres, ewres, nlats, nlons
    )
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
