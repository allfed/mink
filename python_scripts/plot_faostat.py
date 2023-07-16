# Importing necessary libraries
import pandas as pd
import geopandas as gpd
import matplotlib.pyplot as plt
import rasterio
from rasterio.features import shapes
import os
import seaborn as sns
import numpy as np
from scipy.stats import linregress

# Configuration dictionary for controlling various plots
config = {
    "plot_hist_side_by_side": True,
    "plot_FAOSTAT_hist": False,
    "plot_SPAM_hist": False,
    "plot_Model_hist": False,
    "plot_scatter": True,
    "plot_gdf": True,
    "plot_merged_gdf": True,
}


# Function to get average variable by country
def get_average_variable_by_country(world, variable, filename, plot_title):
    # Opening and reading tif file
    with rasterio.open(filename) as src:
        tiff_data = src.read(1).astype("float32")
        transform = src.transform
        vector_features = (
            {"properties": {"raster_val": v}, "geometry": s}
            for i, (s, v) in enumerate(shapes(tiff_data, transform=transform))
        )
        # Create GeoDataFrame from vector features
        gdf = gpd.GeoDataFrame.from_features(vector_features)
        gdf.crs = src.crs

        # Set raster values less than 10 to NaN
        gdf.loc[gdf["raster_val"] < 10, "raster_val"] = np.nan

    # Plot GeoDataFrame if set in config
    if config.get("plot_gdf", False) and not plot_title == "no plot":
        gdf_nonzero = gdf[gdf["raster_val"] != 0]  # filter out zero values
        fig, ax = plt.subplots(1, 1)
        world.boundary.plot(ax=ax, color="black")  # plot country outlines
        plot = gdf_nonzero.plot(
            column="raster_val",
            edgecolor="none",
            legend=True,
            ax=ax,
            cmap="viridis",
        )
        plt.title(plot_title)
        plt.show(block=False)

    # Reproject world to same CRS as raster
    world = world.to_crs("EPSG:4326")

    # Intersection of world polygons with raster
    merged_gdf = gpd.overlay(world, gdf, how="intersection")

    # Calculate area of intersection polygons and calculate variable based on raster values and area
    merged_gdf["intersection_area"] = merged_gdf.geometry.area
    merged_gdf[variable] = merged_gdf["raster_val"] * merged_gdf["intersection_area"]

    # Count the number of records for each country
    country_counts = merged_gdf.groupby("name").size()

    # Get countries with less than 4 cells
    countries_less_than_4_cells = country_counts[country_counts < 4].index

    # Mark those countries as NaN
    for country in countries_less_than_4_cells:
        merged_gdf.loc[merged_gdf["name"] == country, variable] = np.nan

    # Group by country name and calculate sum of area and variable
    grouped = merged_gdf.groupby("name").agg(
        {"intersection_area": "sum", variable: "sum"}
    )
    # Calculate average variable
    grouped["average_" + variable] = grouped[variable] / grouped["intersection_area"]
    grouped.reset_index(inplace=True)

    # Merge the calculated averages back onto the original world GeoDataFrame
    final_gdf = pd.merge(world, grouped, left_on="name", right_on="name", how="left")
    final_gdf["average_" + variable].fillna(0, inplace=True)

    # Plot the merged GeoDataFrame if set in config
    if config.get("plot_merged_gdf", False) and not plot_title == "no plot":
        final_gdf_nonzero = final_gdf[
            final_gdf["average_" + variable] != 0
        ]  # filter out zero values
        fig, ax = plt.subplots(1, 1)
        world.boundary.plot(ax=ax, color="black")  # plot country outlines
        plot = final_gdf_nonzero.plot(
            column="average_" + variable,
            edgecolor="none",
            legend=True,
            ax=ax,
            cmap="viridis",
        )
        plt.title(plot_title + " Merged on World Map")
        plt.show(block=False)

    # Return final GeoDataFrame
    return final_gdf


def load_data():
    df = pd.read_csv(
        "~/Code/mink/grassdata/world/FAOSTAT/FAOSTAT_data_en_7-13-2023.csv"
    )
    df["Year"] = pd.to_numeric(df["Year"], errors="coerce")
    df["Value"] = pd.to_numeric(df["Value"], errors="coerce") / 10
    df = df[(df["Year"] >= 2000) & (df["Year"] <= 2005)]
    result = df.groupby("Area Code (ISO3)")["Value"].mean().reset_index()
    result.columns = ["ISO3", "Average Yield 2000-2005 (kg/ha)"]
    return result


def plot_FAOSTAT_hist(df):
    """Create a histogram of Average Wheat Yield based on FAOSTAT data"""
    fig, ax = plt.subplots()  # Explicitly create new figure and axes

    # filter dataframe to include only non-zero values
    df = df[df["Average Yield 2000-2005 (kg/ha)"] != 0]

    ax.hist(
        df["Average Yield 2000-2005 (kg/ha)"], bins=20, edgecolor="black"
    )  # Use ax to create histogram
    ax.set_title("Histogram of Average Wheat Yield (2000-2005)")
    ax.set_xlabel("Yield (kg/ha)")
    ax.set_ylabel("Frequency")
    plt.show(block=False)


def plot_hist(df, title):
    """Create a histogram of Average Wheat Yield based on custom data source"""
    fig, ax = plt.subplots()  # Explicitly create new figure and axes

    # filter dataframe to include only non-zero values
    df = df[df["average_yield"] != 0]

    ax.hist(df["average_yield"], bins=20, edgecolor="black")
    ax.set_title("Histogram of " + title + " Average Wheat Yield (2000-2005)")
    ax.set_xlabel("Yield (kg/ha)")
    ax.set_ylabel("Frequency")
    plt.show(block=False)


def plot_scatter(merged_data, title):
    """Plot scatter diagram of FAOSTAT vs custom data source"""
    # filter dataframe to include only non-zero values
    merged_data = merged_data[merged_data["Average Yield 2000-2005 (kg/ha)"] != 0]
    merged_data = merged_data[merged_data["average_yield"] != 0]

    plt.figure()

    plt.scatter(
        merged_data["Average Yield 2000-2005 (kg/ha)"], merged_data["average_yield"]
    )
    plt.plot(
        [0, max(merged_data["Average Yield 2000-2005 (kg/ha)"])],
        [0, max(merged_data["Average Yield 2000-2005 (kg/ha)"])],
        color="r",
    )
    # Calculate r-squared
    slope, intercept, r_value, p_value, std_err = linregress(
        merged_data["Average Yield 2000-2005 (kg/ha)"], merged_data["average_yield"]
    )
    print(f"R-squared: {r_value**2}")

    plt.text(
        min(merged_data["Average Yield 2000-2005 (kg/ha)"]),
        max(merged_data["average_yield"]),
        f"R² = {r_value**2:.2f}",
        fontsize=12,
    )  # Display R-squared on the plot
    plt.title(title)

    plt.title("FAOSTAT vs " + title + "")
    plt.xlabel("FAOSTAT Average Yield 2000-2005 (kg/ha)")
    plt.ylabel(title + " Average Yield")
    plt.show(block=False)


def handle_tif(result, plot_type, tif_directory, tif_file, plot_title):
    """Handle .tif raster data, overlay them with world map, and create merged data"""
    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))
    averaged_variable = get_average_variable_by_country(
        world, "yield", os.path.join(tif_directory, tif_file), plot_title
    )

    if config.get("plot_" + plot_type + "_hist", False):
        plot_hist(averaged_variable, plot_type)

    merged_data = pd.merge(
        result, averaged_variable, left_on="ISO3", right_on="iso_a3", how="inner"
    )

    if config.get("plot_scatter", False):
        plot_scatter(merged_data, plot_type)

    return merged_data  # Add this line to return the merged data


def plot_hist_side_by_side(df1, df2, title1, title2, column1, column2):
    """Create histograms of two datasets side by side"""
    fig, ax = plt.subplots()  # Explicitly create new figure and axes

    # filter dataframes to include only non-zero values
    df1 = df1[df1[column1] != 0]
    df2 = df2[df2[column2] != 0]

    # Create an array with the colors you want to use
    colors = ["#1f77b4", "#ff7f0e"]

    # Set your custom color palette
    customPalette = sns.set_palette(sns.color_palette(colors))

    # Create bins with numpy by specifying range and number of bins
    bins = np.linspace(
        min(df1[column1].min(), df2[column2].min()),
        max(df1[column1].max(), df2[column2].max()),
        20,
    )

    # Plot the histogram
    sns.histplot(df1, x=column1, color=colors[0], bins=bins, ax=ax, label=title1)
    sns.histplot(df2, x=column2, color=colors[1], bins=bins, ax=ax, label=title2)

    ax.set_title("Histogram of " + title1 + " vs " + title2)
    ax.set_xlabel("Yield (kg/ha)")
    ax.set_ylabel("Frequency")

    plt.legend()
    plt.show(block=False)


def plot_SPAM_vs_Model(SPAM_data, Model_data, title):
    # filter dataframes to include only non-zero values
    SPAM_data = SPAM_data[SPAM_data["average_yield"] != 0]
    Model_data = Model_data[Model_data["average_yield"] != 0]

    print("SPAM_data.columns")
    print("Model_data.columns")
    print(SPAM_data.columns)
    print(Model_data.columns)
    plt.figure()
    merged_data = pd.merge(
        SPAM_data,
        Model_data,
        left_on="ISO3",
        right_on="iso_a3",
        how="inner",
        suffixes=("_SPAM", "_Model"),
    )

    plt.scatter(merged_data["average_yield_SPAM"], merged_data["average_yield_Model"])
    plt.plot(
        [
            0,
            max(
                merged_data["average_yield_SPAM"].max(),
                merged_data["average_yield_Model"].max(),
            ),
        ],
        [
            0,
            max(
                merged_data["average_yield_SPAM"].max(),
                merged_data["average_yield_Model"].max(),
            ),
        ],
        # block=False,
        color="r",
    )

    # Calculate r-squared
    slope, intercept, r_value, p_value, std_err = linregress(
        merged_data["average_yield_SPAM"], merged_data["average_yield_Model"]
    )
    print(f"R-squared: {r_value**2}")

    plt.text(
        min(merged_data["average_yield_SPAM"]),
        max(merged_data["average_yield_Model"]),
        f"R² = {r_value**2:.2f}",
        fontsize=12,
    )  # Display R-squared on the plot
    plt.title(title)
    plt.xlabel("SPAM Average Yield (kg/ha)")
    plt.ylabel("Model Average Yield (kg/ha)")
    plt.show(block=False)


def main():
    result = load_data()

    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))

    averaged_variable_SPAM = get_average_variable_by_country(
        world,
        "yield",
        os.path.join("/home/dmrivers/Code/mink/wth_historical", "WHEA_yield.tif"),
        "SPAM yields kg/ha",
    )

    averaged_variable_Model = get_average_variable_by_country(
        world,
        "yield",
        os.path.join(
            "/home/dmrivers/Code/mink/wth_control",
            "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul13_specific_allmonths_wet_overall_yield.tif",
        ),
        "Model yields kg/ha",
    )

    if config.get("plot_FAOSTAT_hist", False):
        plot_FAOSTAT_hist(result)

    if config.get("plot_hist_side_by_side", False):
        plot_hist_side_by_side(
            averaged_variable_SPAM,
            averaged_variable_Model,
            "SPAM",
            "Model",
            "average_yield",
            "average_yield",
        )
        plot_hist_side_by_side(
            result,
            averaged_variable_SPAM,
            "FAOSTAT",
            "SPAM",
            "Average Yield 2000-2005 (kg/ha)",
            "average_yield",
        )
        plot_hist_side_by_side(
            result,
            averaged_variable_SPAM,
            "FAOSTAT",
            "Model",
            "Average Yield 2000-2005 (kg/ha)",
            "average_yield",
        )

    SPAM_data = handle_tif(
        result,
        "SPAM",
        "/home/dmrivers/Code/mink/wth_historical",
        "WHEA_yield.tif",
        "no plot",
    )
    Model_data = handle_tif(
        result,
        "Model",
        "/home/dmrivers/Code/mink/wth_control",
        "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul13_specific_allmonths_wet_overall_yield.tif",
        "no plot",
    )

    plot_SPAM_vs_Model(SPAM_data, Model_data, "SPAM vs Model based on Countries")

    input()


if __name__ == "__main__":
    main()
