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
from osgeo import ogr
import matplotlib.colors as mcolors
import statsmodels.api as sm

# Configuration dictionary for controlling various plots
config = {
    "plot_hist_side_by_side": True,
    "plot_FAOSTAT_hist": False,
    "plot_SPAM_hist": False,
    "plot_Model_hist": False,
    "plot_scatter": False,
    "plot_merged_gdf": True,
}


# Function to get average variable by country
def plot_gdf_properties(world, prefix, properties, max_col):
    for plot_property in properties:
        fig, ax = plt.subplots(1, 1)

        # Use GeoPandas plot() method to create a choropleth map
        world.boundary.plot(
            ax=ax, linewidth=1, color="black"
        )  # Plot country boundaries for better clarity

        world.plot(
            column=prefix + "_" + plot_property,
            ax=ax,
            legend=True,
            legend_kwds={
                "label": prefix + " " + plot_property,
                "orientation": "horizontal",
            },
            vmax=world[
                max_col + "_" + plot_property
            ].max(),  # Set the maximum color limit
        )

        plt.title(prefix + "_" + plot_property, fontsize=20)
        plt.show(block=False)


def load_faostat_data(world, csv_loc):
    df = pd.read_csv(csv_loc)
    df["Year"] = pd.to_numeric(df["Year"], errors="coerce")
    df["Value"] = pd.to_numeric(df["Value"], errors="coerce") / 10
    df = df[(df["Year"] >= 2004) & (df["Year"] <= 2006)]
    result = df.groupby("Area Code (ISO3)")["Value"].mean().reset_index()
    result.columns = ["ISO3", "FAOSTAT_average_yield"]
    world = world.merge(result, left_on="iso_a3", right_on="ISO3", how="left")
    return world


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
    # print(f"R-squared: {r_value ** 2}")

    plt.text(
        min(merged_data["Average Yield 2000-2005 (kg/ha)"]),
        max(merged_data["average_yield"]),
        # f"R^2 = {r_value**2:.2f}",
        fontsize=12,
    )  # Display R-squared on the plot
    plt.title(title)

    plt.title("FAOSTAT vs " + title + "")
    plt.xlabel("FAOSTAT Average Yield 2000-2005 (kg/ha)")
    plt.ylabel(title + " Average Yield")
    plt.show(block=False)


def import_yield_and_area(result, plot_type, area_tif_file, yield_tif_file, plot_title):
    """Handle .tif raster data, overlay them with world map, and create merged data"""
    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))

    breakpoint()

    # averaged_variable = get_average_yield_by_country(
    #     world, yield_tif_file, area_tif_file, plot_title
    # )

    if config.get("plot_" + plot_type + "_hist", False):
        plot_hist(averaged_variable, plot_type)

    merged_data = pd.merge(
        result, averaged_variable, left_on="ISO3", right_on="iso_a3", how="inner"
    )

    if config.get("plot_scatter", False):
        plot_scatter(merged_data, plot_type)

    return merged_data  # Add this line to return the merged data


def plot_hist_side_by_side(crop, world, column_list):
    """Create bar plots of multiple datasets side by side within the same bin ranges"""

    # Check if the columns exist
    for col in column_list:
        if col not in world.columns:
            raise ValueError(f"Column '{col}' not found in DataFrame")

    # Define colors (you may add more if you have more than two columns)
    colors = sns.color_palette("tab10", len(column_list))

    # Find the min and max across all specified columns
    min_val = min(world[col].min() for col in column_list if world[col].min() != 0)
    max_val = max(world[col].max() for col in column_list if world[col].max() != 0)

    # Create bins with numpy by specifying range and number of bins
    bins = np.linspace(min_val, max_val, 20)

    # Initialize an empty DataFrame to hold the counts for each bin and category
    counts_df = pd.DataFrame()

    # Calculate the counts for each bin and category
    for col in column_list:
        data = world[world[col] != 0][col]
        counts, _ = np.histogram(data, bins=bins)
        counts_df[col] = counts

    # Create the bar plot
    ax = counts_df.plot(kind="bar", color=colors, width=0.8)

    # Set the x-tick labels to the bin ranges
    ax.set_xticklabels(
        [f"{int(bins[i])} to {int(bins[i+1])}" for i in range(len(bins) - 1)],
        rotation=45,
    )

    ax.set_title(crop + " Comparison of " + " vs ".join(column_list))
    ax.set_xlabel("Yield Range (kg/ha)")
    ax.set_ylabel("Frequency")

    plt.legend(title="Category")
    plt.show(block=False)


def RMSE(expected, observed):
    log_ratios = np.log(observed / expected)
    squared_diffs = log_ratios**2
    mean_of_differences = np.mean(squared_diffs)
    result = np.sqrt(mean_of_differences)
    return result / np.log(2)


def weighted_RMSE(expected, observed, weights):
    log_ratios = np.log(observed / expected)
    weighted_squared_diffs = log_ratios**2
    avg_squared_diffs = np.average(weighted_squared_diffs, weights=weights)
    result = np.sqrt(avg_squared_diffs)
    return result / np.log(2)


def get_stats(filtered_world, observed_col, expected_col, weights):
    # Weighted least squares regression
    X = sm.add_constant(filtered_world[expected_col])
    y = filtered_world[observed_col]

    model = sm.WLS(y, X, weights=weights).fit()
    print(f"Weighted R-squared: {model.rsquared}")

    # Calculate r-squared
    slope, intercept, r_value, p_value, std_err = linregress(
        filtered_world[expected_col], filtered_world[observed_col]
    )
    print(f"R-squared: {r_value**2}")

    # Assumingexpected_colis the expected values
    expected_values = filtered_world[expected_col]
    # Assumingobserved_colis the observed results
    observed_results = filtered_world[observed_col]

    rmse = RMSE(expected_values, expected_values)
    print(f"RMSE same: {rmse}")
    rmse = RMSE(expected_values, expected_values * 2)
    print(f"RMSE twice: {rmse}")
    rmse = RMSE(expected_values, expected_values / 2)
    print(f"RMSE half: {rmse}")
    rmse = RMSE(expected_values, observed_results)
    print(f"RMSE data: {rmse}")

    weighted_rmse = weighted_RMSE(expected_values, expected_values, weights)
    print(f"weighted RMSE same: {weighted_rmse}")
    weighted_rmse = weighted_RMSE(expected_values, expected_values * 2, weights)
    print(f"weighted RMSE twice: {weighted_rmse}")
    weighted_rmse = weighted_RMSE(expected_values, expected_values / 2, weights)
    print(f"weighted RMSE half: {weighted_rmse}")
    weighted_rmse = weighted_RMSE(expected_values, observed_results, weights)
    print(f"weighted RMSE data: {weighted_rmse}")

    return r_value**2, model.rsquared, rmse, weighted_rmse


def scatter_country_averages(world, observed_col, expected_col, title):
    filtered_world = world.dropna(
        subset=[expected_col + "_average_yield", observed_col + "_average_yield"]
    )
    weights = filtered_world[expected_col + "_production"] / np.average(
        filtered_world[expected_col + "_production"]
    )

    # filter dataframes to include only non-zero values
    # filtered_world.to_csv("Model_vs_spam_data.csv")

    plt.figure()

    # Normalize the SPAM_area values for color mapping
    norm = mcolors.Normalize(
        vmin=weights.min(),
        vmax=weights.max(),
    )
    cmap = plt.cm.viridis

    # Scale theexpected_col +  _area values for size mapping
    sizes = weights / weights.max() * 100

    plt.scatter(
        filtered_world[expected_col + "_average_yield"],
        filtered_world[observed_col + "_average_yield"],
        c=weights,
        cmap=cmap,
        norm=norm,
        s=sizes,
    )

    plt.colorbar(label=expected_col + " ratio of production to average country")

    # Add dotted line where x = y
    plt.plot(
        [
            filtered_world[expected_col + "_average_yield"].min(),
            filtered_world[observed_col + "_average_yield"].max(),
        ],
        [
            filtered_world[expected_col + "_average_yield"].min(),
            filtered_world[observed_col + "_average_yield"].max(),
        ],
        linestyle="dotted",
        color="gray",
    )

    for index, row in filtered_world.iterrows():
        plt.text(
            row[expected_col + "_average_yield"],
            row[observed_col + "_average_yield"],
            row["iso_a3"],
            fontsize=9,
        )

    r_squared, WLS, rmse, weighted_rmse = get_stats(
        filtered_world,
        observed_col + "_average_yield",
        expected_col + "_average_yield",
        weights,
    )

    # result = custom_metric(np.zeros(len(expected_values)), expected_values, weights)
    # print(f"Result: {result}")

    # Define a suitable x and y coordinate for the R-squared label
    x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
    y_pos1 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.9
    )  # 90% from the bottom
    y_pos2 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.85
    )  # 90% from the bottom
    y_pos3 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.8
    )  # 90% from the bottom
    y_pos4 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.75
    )  # 90% from the bottom

    plt.text(
        x_pos, y_pos1, f"R^2 = {r_squared:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos2, f"WLS Weighted R^2 = {WLS:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot

    plt.text(
        x_pos, y_pos3, f"log RMSE  = {rmse:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos4, f"Weighted log RMSE = {weighted_rmse:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.title(title)
    plt.xlabel(expected_col + " Average Yield (kg/ha)")
    plt.ylabel(observed_col + " Average Yield (kg/ha)")
    plt.show(block=False)

    return filtered_world


def scatter_country_production(world, observed_col, expected_col, title):
    filtered_world = world.dropna(
        subset=[expected_col + "_average_yield", observed_col + "_average_yield"]
    )

    plt.figure()

    plt.scatter(
        filtered_world[expected_col + "_production"],
        filtered_world[observed_col + "_production"],
    )

    # Add dotted line where x = y
    plt.plot(
        [
            filtered_world[expected_col + "_production"].min(),
            filtered_world[expected_col + "_production"].max(),
        ],
        [
            filtered_world[expected_col + "_production"].min(),
            filtered_world[expected_col + "_production"].max(),
        ],
        linestyle="dotted",
        color="gray",
    )

    for index, row in filtered_world.iterrows():
        plt.text(
            row[expected_col + "_production"],
            row[observed_col + "_production"],
            row["iso_a3"],
            fontsize=9,
        )
    plt.title(title)
    plt.xlabel(expected_col + "Annual Production (kg)")
    plt.ylabel(observed_col + "Annual Production (kg)")
    plt.show(block=False)

    return filtered_world


def load_grass_csv(world, production_area_by_country, data_category):
    production_data = pd.read_csv(production_area_by_country)
    production_data.rename(
        columns={"Production (kg wet)": data_category + "_production"}, inplace=True
    )
    production_data.rename(
        columns={"Area (hectares)": data_category + "_area"}, inplace=True
    )

    world = world.merge(
        production_data, left_on="iso_a3", right_on="iso_a3", how="left"
    )

    world[data_category + "_average_yield"] = (
        world[data_category + "_production"] / world[data_category + "_area"]
    )
    print(data_category + "_average_yield")
    print(world[data_category + "_average_yield"])
    return world


def remove_row_if_any_column_is_nan(world, columns):
    # Mask for rows where any of the specified columns are missing or zero
    mask = world[columns].isna() | (world[columns] == 0)
    rows_to_update = mask.any(axis=1)

    # Set the values in the specified columns to NaN for those rows
    world.loc[rows_to_update, columns] = np.nan

    return world


def main():
    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))
    world = load_faostat_data(
        world, "~/Code/mink/grassdata/world/FAOSTAT/FAOSTAT_data_en_7-13-2023.csv"
    )

    world = load_grass_csv(
        world,
        os.path.join(
            "/home/dmrivers/Code/mink/wth_historical", "by_country_WHEA_yield.csv"
        ),
        "SPAM",
    )
    world = load_grass_csv(
        world,
        os.path.join(
            "/home/dmrivers/Code/mink/wth_control",
            "by_country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug03_revisedinit_wet_overall_yield.csv",
        ),
        "model",
    )

    world = remove_row_if_any_column_is_nan(
        world,
        [
            "SPAM_average_yield",
            "model_average_yield",
            "FAOSTAT_average_yield",
            "SPAM_production",
            "model_production",
            "SPAM_area",
            "model_area",
        ],
    )
    if config.get("plot_merged_gdf"):
        plot_gdf_properties(world, "SPAM", ["average_yield", "production"], "SPAM")
        plot_gdf_properties(world, "model", ["average_yield", "production"], "SPAM")
        # plot_gdf_properties(world, "FAOSTAT", ["average_yield"])

    if config.get("plot_hist_side_by_side", True):
        plot_hist_side_by_side(
            "Wheat",
            world,
            ["SPAM_average_yield", "model_average_yield", "FAOSTAT_average_yield"],
        )

    world = scatter_country_averages(
        world, "model", "SPAM", "Wheat SPAM vs Model based on Countries"
    )
    world = scatter_country_averages(
        world, "FAOSTAT", "SPAM", "Wheat SPAM vs FAOSTAT based on Countries"
    )
    world = scatter_country_production(
        world, "model", "SPAM", "Wheat SPAM vs FAOSTAT Production Based on Countries"
    )

    print("SPAM_production sum")
    print(str(world["SPAM_production"].sum() / 1e9) + " million tonnes wet")

    print("model_production sum")
    print(str(world["model_production"].sum() / 1e9) + " million tonnes wet")

    # merged_gdf.to_csv(
    #     "Model_vs_spam_data.csv",
    #     columns=["ISO3_Model", "average_yield_SPAM", "average_yield_Model"],
    # )
    input()


if __name__ == "__main__":
    main()
