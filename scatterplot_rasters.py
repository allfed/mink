import numpy as np
import pandas as pd
import geopandas as gpd
from shapely.geometry import Point
import matplotlib.pyplot as plt

# Load the data into a pandas array


def import_ascii(filename):

    # Load the data into a numpy array

    with open(filename) as f:
        lines = f.readlines()

    # Get the header information from the .asc file
    north = float(lines[0].split(":")[1].strip().split("N")[0])
    south = float(lines[1].split(":")[1].strip().split("N")[0])
    east = float(lines[2].split(":")[1].strip().split("W")[0])
    west = float(lines[3].split(":")[1].strip().split("W")[0])
    rows = int(lines[4].split(":")[1].strip())
    cols = int(lines[5].split(":")[1].strip())

    # Create a numpy array with the data
    data = []
    for line in lines[6:]:
        data.extend([float(x) if x != "*" else np.nan for x in line.strip().split()])
    data = np.array(data).reshape((rows, cols))

    # Create a DataFrame with the latitude and longitude information
    lats = np.linspace(south, north, rows)
    lons = np.linspace(west, east, cols)
    lat_lon = np.array(np.meshgrid(lats, lons)).T.reshape(-1, 2)
    df = pd.DataFrame(lat_lon, columns=["lat", "lon"])

    # Flatten the data array and add it to the DataFrame
    df["yields"] = data.flatten()

    # Create a Geopandas DataFrame with the Point geometry
    df["geometry"] = df.apply(lambda x: Point(x["lon"], x["lat"]), axis=1)
    gdf = gpd.GeoDataFrame(df, geometry="geometry")

    return gdf


def plot_against_each_other(crop, control_gdf, historical_gdf):
    # Filter both datasets to only include non-zero values
    control_gdf_nonzero = control_gdf[historical_gdf["yields"] != 0]
    historical_gdf_nonzero = historical_gdf[historical_gdf["yields"] != 0]

    # Calculate the mean yields for both datasets
    control_mean = control_gdf_nonzero["yields"].mean()
    historical_mean = historical_gdf_nonzero["yields"].mean()

    # Plot the data

    # Plot the data as a scatterplot
    plt.scatter(
        control_gdf_nonzero["yields"],
        historical_gdf_nonzero["yields"],
        c=historical_gdf_nonzero["lat"],
        cmap="viridis",
    )
    plt.xlabel("Crop Model Yield (kg/ha)")
    plt.ylabel("Historical Yield (kg/ha)")
    plt.colorbar(label="Latitude")
    plt.title(crop + " Crop Model Yields vs Historical Yields")
    plt.show()

    # Plot the data as a scatterplot
    # Plot historical values in blue
    plt.scatter(
        historical_gdf_nonzero["lat"],
        historical_gdf_nonzero["yields"],
        c="blue",
        label="Historical Yield",
    )
    # Plot control values in red
    plt.scatter(
        control_gdf_nonzero["lat"],
        control_gdf_nonzero["yields"],
        c="red",
        label="Crop model Yield",
    )
    # Label the x-axis as Latitude
    plt.xlabel("Latitude")
    # Label the y-axis as Yield
    plt.ylabel("Yield (kg/ha)")
    # Add a horizontal line at the mean yields for historical values
    plt.axhline(historical_mean, c="blue", label="Historical Mean")
    # Add a horizontal line at the mean yields for catastrophe values
    plt.axhline(control_mean, c="red", label="Crop Model Mean")
    plt.title(crop + "Crop Model vs Historical Yield Yields")
    # Add a legend to distinguish between historical and catastrophe values
    plt.legend()
    # Show the plot
    plt.show()


control_filenames = [
    "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_rapeseed__RUN1USA150tg_overall_yield.asc",
    "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__RUN1USA150tg_overall_yield.asc",
    "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_potatoes__RUN1USA150tg_overall_yield.asc",
]
historical_filenames = ["RAPE_yield.asc", "WHEA_yield.asc", "POTA_yield.asc"]

for i, control_filename in enumerate(control_filenames):
    control_gdf = import_ascii(control_filename)
    historical_gdf = import_ascii(historical_filenames[i])
    crop = historical_filenames[i][0:4]
    plot_against_each_other(crop, control_gdf, historical_gdf)

# control_gdf = import_ascii(control_filename)
# historical_gdf = import_ascii(historical_filename)
# plot_against_each_other(control_gdf, historical_gdf)
