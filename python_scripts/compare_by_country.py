# Importing necessary libraries
# from adjustText import adjust_text
import re
import pandas as pd
import geopandas as gpd
import matplotlib.pyplot as plt
import rasterio
from rasterio.features import shapes
from shapely.geometry import Point

import os
import seaborn as sns
import numpy as np
from scipy.stats import linregress
from osgeo import ogr
import matplotlib.colors as mcolors
import statsmodels.api as sm
from statsmodels.tools.eval_measures import rmse as statmodel_rmse

# Configuration dictionary for controlling various plots
config = {
    "plot_merged_gdf": False,
    "plot_hist_side_by_side": False,
    "plot_by_country_scatter": True,
    "plot_cell_by_cell_scatter": False,
}

git_root = "../"


table_data = {}


def main():
    crop_codes = {
        "WHEA": {
            "crop_nice_name": "Wheat",
            "AGMIP_code": "whe",
            "SNX_description": "wheat__Aug04_genSNX",
        },
        "MAIZ": {
            "crop_nice_name": "Maize",
            "AGMIP_code": "mai",
            "SNX_description": "maize__Sep26_genSNX",
        },
        "SOYB": {
            "crop_nice_name": "Soybean",
            "AGMIP_code": "soy",
            "SNX_description": "soybeans__Sep27_genSNX",
        },
        "RICE": {
            "crop_nice_name": "Rice",
            "AGMIP_code": "ric",
            "SNX_description": "rice__Sep29_genSNX",
        },
        # hmm I don't have the AGMIP comparison here...
        "RAPE": {
            "crop_nice_name": "rapeseed",
            "AGMIP_code": "SKIP_ME",
            "SNX_description": "rapeseed__Sep28_genSNX",
        },
        "POTA": {
            "crop_nice_name": "potatoes",
            "AGMIP_code": "SKIP_ME",
            "SNX_description": "potatoes__Oct01_genSNX",
        },
    }

    rf_and_ir = {
        "RF": {
            "rf_or_ir": "_RF",
            "snx_ending": "wet_averaged_RF",
            "title": "Rainfed",
        },
        "IR": {
            "rf_or_ir": "_IR",
            "snx_ending": "wet_averaged_IR",
            "title": "Irrigated",
        },
        "overall": {
            "rf_or_ir": "",
            "snx_ending": "wet_overall_yield",
            "title": "Overall",
        },
    }

    for crop_key, crop_value in crop_codes.items():
        for water_key, water_values in rf_and_ir.items():
            display_for_crops_and_irrigation_level(
                crop_key, crop_value, water_key, water_values
            )

    df = pd.DataFrame(table_data)
    print(df)
    df.to_csv("output_table.csv", index=False)

    # Set 'Crop Name' as the index for more meaningful output
    df.set_index("Crop Name", inplace=True)

    # Loop through each column, create a new DataFrame, and print
    for column in df.columns:
        new_df = pd.DataFrame(df[column])
        print("")
        print("")
        print(f"Data for column '{column}':")
        print(new_df)
        print("-" * 40)  # prints a separator line for clarity

    # merged_gdf.to_csv(
    #     "Model_vs_spam_data.csv",
    #     columns=["ISO3_Model", "average_yield_SPAM", "average_yield_Model"],
    # )
    input()


def display_for_crops_and_irrigation_level(
    crop_key, crop_value, water_key, water_values
):
    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))

    agmip_code = crop_value["AGMIP_code"]
    snx_description = crop_value["SNX_description"]
    crop_nice_name = crop_value["crop_nice_name"]
    rf_or_ir = water_values["rf_or_ir"]
    snx_ending = water_values["snx_ending"]
    title = water_values["title"]
    if crop_key == "RICE" and rf_or_ir == "_RF":
        return

    if water_key == "overall":
        world = load_faostat_data(
            world, f"{git_root}/grassdata/world/FAOSTAT/FAOSTAT_data_en_7-13-2023.csv"
        )

    filename = f"by_country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_{snx_description}_{snx_ending}.csv"

    world = load_grass_csv(
        world,
        os.path.join(f"{git_root}wth_control", filename),
        f"model{rf_or_ir}",
    )
    if water_key == "overall":
        # SPAM is only for overall
        world = load_grass_csv(
            world,
            os.path.join(
                "/home/dmrivers/Code/mink/wth_historical",
                f"by_country_{crop_key}_yield.csv",
            ),
            "SPAM",
        )

        # this might be useful to plot spam vs old spam....
        # world = load_grass_csv(
        #     world,
        #     os.path.join(
        #         "/home/dmrivers/Code/mink/grassdata/world/spam",
        #         f"by_country_{crop_key}_yield.csv",
        #     ),
        #     "SPAM",
        # )

        # world = scatter_country_averages(
        #     world,
        #     "SPAM",
        #     "SPAM_old",
        #     f"{crop_nice_name} SPAM old vs SPAM based on Countries",
        # )
        # breakpoint

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
    elif not agmip_code == "SKIP_ME":
        # AGMIP is only for RF or IR (also does not include rapeseed or potatoes)
        world = load_grass_csv(
            world,
            os.path.join(
                f"{git_root}grassdata/world/AGMIP",
                f"by_country_AGMIP_princeton{rf_or_ir}_yield_{agmip_code}_lowres_cleaned_2005.csv",
            ),
            f"AGMIP{rf_or_ir}",
        )
        world = remove_row_if_any_column_is_nan(
            world,
            [
                f"model{rf_or_ir}_average_yield",
                # f"AGMIP{rf_or_ir}_average_yield",
                f"model{rf_or_ir}_production",
                # f"AGMIP{rf_or_ir}_production",
                f"model{rf_or_ir}_area",
            ],
        )
    else:
        return

    if config.get("plot_cell_by_cell_scatter", True) and water_key == "overall":
        SPAM_yields_ascii_file = os.path.join(
            f"{git_root}wth_historical", f"{crop_key}_yield.asc"
        )
        modelled_yields_ascii_file = os.path.join(
            f"{git_root}wth_control",
            f"379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_{snx_description}_{snx_ending}.asc",
        )
        SPAM_area_ascii_file = os.path.join(
            f"{git_root}wth_historical", f"{crop_key}_cropland.asc"
        )

        compare_yields_with_model_by_cell(
            "SPAM",
            SPAM_yields_ascii_file,
            modelled_yields_ascii_file,
            SPAM_area_ascii_file,
            "SPAM",
            "SPAM Yields",
            "Mink model yields",
            f"{crop_nice_name} SPAM vs model yields per grid cell",
        )

    if config.get("plot_by_country_scatter", True):
        if water_key == "overall":
            observed_col = "model"
            expected_col = "SPAM"
            world = scatter_country_averages(
                world,
                observed_col,
                expected_col,
                f"{crop_nice_name} {expected_col} vs {observed_col} based on Countries",
            )
            add_row_to_table(world, crop_value, observed_col, expected_col)

            # world = scatter_country_averages(
            #     world, "FAOSTAT", "SPAM", "Wheat SPAM vs FAOSTAT based on Countries"
            # )
        # else:
        #     world = scatter_country_averages(
        #         world,
        #         f"model{rf_or_ir}",
        #         f"AGMIP{rf_or_ir}",
        #         f"{title}: {crop_nice_name} AGMIP vs Model based on Countries",
        #     )

        # world = scatter_country_averages(
        #     world, "model", "AGMIP", "Wheat SPAM vs Model based on Countries"
        # )
        # world = scatter_country_production(
        #     world,
        #     "model",
        #     "SPAM",
        #     "Wheat SPAM vs FAOSTAT Production Based on Countries",
        # )


def add_row_to_table(world, crop_value, observed_col, expected_col):
    weights, filtered_world = get_weights_and_filtered_world(
        world, observed_col, expected_col
    )

    (
        r_squared,
        weighted_r2,
        _,
        relative_rmse,
        _,
        rmse,
    ) = get_stats(
        filtered_world,
        observed_col + "_average_yield",
        expected_col + "_average_yield",
        weights,
    )

    if "Crop Name" not in table_data:
        table_data["Crop Name"] = []
        table_data[f"R^2 with {expected_col}"] = []
        table_data[f"Weighted R^2 with {expected_col}"] = []
        table_data[f"RMSE (kg/ha) with {expected_col}"] = []
        table_data[f"RRMSE (%) with {expected_col}"] = []
        table_data[f"Ratio {observed_col} to {expected_col} production"] = []

    table_data["Crop Name"].append(crop_value["crop_nice_name"])
    table_data[f"R^2 with {expected_col}"].append(round(r_squared, 2))
    table_data[f"Weighted R^2 with {expected_col}"].append(round(weighted_r2, 2))
    table_data[f"RMSE (kg/ha) with {expected_col}"].append(round(rmse, 2))
    table_data[f"RRMSE (%) with {expected_col}"].append(
        round(relative_rmse * 100, 2)
    )  # Convert to percentage
    table_data[f"Ratio {observed_col} to {expected_col} production"].append(
        round(
            world[f"{observed_col}_production"].sum()
            / world[f"{expected_col}_production"].sum(),
            2,
        )
    )


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
    # df = df[df["Average Yield 2000-2005 (kg/ha)"] > 500]

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
    # df = df[df["average_yield"] > 500]

    ax.hist(df["average_yield"], bins=20, edgecolor="black")
    ax.set_title("Histogram of " + title + " Average Wheat Yield (2000-2005)")
    ax.set_xlabel("Yield (kg/ha)")
    ax.set_ylabel("Frequency")
    plt.show(block=False)


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


def d_statistic(expected, observed):
    O = list(expected)
    P = list(observed)

    numerator = 0
    denominator = 0

    N = len(P)
    if N != len(O):
        raise ValueError("P and O must be the same length")

    mean_O = sum(O) / N
    for i in range(N):
        P_prime = P[i] - mean_O
        O_prime = O[i] - mean_O

        numerator += (P[i] - O[i]) ** 2
        denominator += (abs(P_prime) + abs(O_prime)) ** 2

    if denominator == 0:
        raise ValueError("Denominator is zero")

    result = 1 - (numerator / denominator)

    return result


def fraction_rmse(expected, observed):
    errors = expected - observed
    normalized_errors = errors / observed

    # RMSE of the normalized errors
    rmse = np.sqrt(np.mean(normalized_errors**2))

    return rmse


def rmse_test(expected, observed):
    errors = expected - observed

    # RMSE of the normalized errors
    rmse = np.sqrt(np.mean(errors**2))

    return rmse


def get_stats(filtered_world, observed_col, expected_col, weights):
    # Weighted least squares regression
    X = sm.add_constant(filtered_world[expected_col])
    y = filtered_world[observed_col]

    model = sm.WLS(y, X, weights=weights).fit()

    # Calculate r-squared
    slope, intercept, r_value, p_value, std_err = linregress(
        filtered_world[expected_col], filtered_world[observed_col]
    )

    # Assumingexpected_colis the expected values
    expected_values = filtered_world[expected_col]
    # Assumingobserved_colis the observed results
    observed_results = filtered_world[observed_col]

    rmse = RMSE(expected_values, expected_values)

    weighted_rmse = weighted_RMSE(expected_values, observed_results, weights)
    d_stat = d_statistic(expected_values, observed_results)
    assert statmodel_rmse(expected_values, expected_values) == 0
    linear_rmse = statmodel_rmse(expected_values, observed_results)
    assert round(rmse_test(expected_values, observed_results), 10) == round(
        linear_rmse, 10
    )
    relative_rmse = linear_rmse / np.mean(expected_values)

    return r_value**2, model.rsquared, rmse, relative_rmse, d_stat, linear_rmse


def get_weights_and_filtered_world(world, observed_col, expected_col):
    filtered_world = world.dropna(
        subset=[expected_col + "_average_yield", observed_col + "_average_yield"]
    )
    weights = filtered_world[expected_col + "_production"] / np.average(
        filtered_world[expected_col + "_production"]
    )
    return weights, filtered_world


def scatter_country_averages(world, observed_col, expected_col, title):
    weights, filtered_world = get_weights_and_filtered_world(
        world, observed_col, expected_col
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

    texts = []
    for index, row in filtered_world.iterrows():
        texts.append(
            plt.text(
                row[expected_col + "_average_yield"],
                row[observed_col + "_average_yield"],
                row["name"],
                fontsize=9,
            )
        )

    # adjust_text(
    #     texts,
    #     # arrowprops=dict(arrowstyle="->", color="black"),
    #     # only_move="overlap",
    # )

    # for index, row in filtered_world.iterrows():
    #     plt.text(
    #         row[expected_col + "_average_yield"],
    #         row[observed_col + "_average_yield"],
    #         row["name"],
    #         fontsize=9,
    #     )
    r_squared, WLS, rmse, fractional_rmse, d_stat, linear_RMSE = get_stats(
        filtered_world,
        observed_col + "_average_yield",
        expected_col + "_average_yield",
        weights,
    )

    # Define a suitable x and y coordinate for the R-squared label
    x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
    y_pos1 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.9 + 0.4
    )  # 90% from the bottom
    y_pos2 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.85 + 0.4
    )  # 90% from the bottom
    y_pos3 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.8 + 0.4
    )  # 90% from the bottom
    y_pos4 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.75 + 0.4
    )  # 90% from the bottom
    y_pos5 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.7 + 0.4
    )  # 90% from the bottom
    y_pos6 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.65 + 0.4
    )  # 90% from the bottom

    plt.text(
        x_pos, y_pos1, f"R^2 = {r_squared:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos2, f"WLS Weighted R^2 = {WLS:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot

    # plt.text(
    #     x_pos, y_pos3, f"log RMSE  = {rmse:.2f}", fontsize=12
    # )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos3, f"Fractional RMSE = {fractional_rmse:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos4, f"d_stat = {d_stat:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos5, f"linear RMSE = {linear_RMSE:.2f} kg/ha", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.title(title)
    plt.xlabel(expected_col + " Average Yield (kg/ha)")
    plt.ylabel(observed_col + " Average Yield (kg/ha)")
    plt.tight_layout()
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
    assert "Production_Sum" in production_data.columns
    assert "Crop_Area_Sum" in production_data.columns
    assert "Planting_Month_Mode" in production_data.columns
    assert "Days_to_Maturity_Mean" in production_data.columns
    production_data.rename(
        columns={"Production_Sum": data_category + "_production"}, inplace=True
    )
    production_data.rename(
        columns={"Crop_Area_Sum": data_category + "_area"}, inplace=True
    )
    production_data.rename(
        columns={"Planting_Month_Mode": data_category + "_planting_month"}, inplace=True
    )
    production_data.rename(
        columns={"Days_to_Maturity_Mean": data_category + "_maturity"}, inplace=True
    )
    production_data.rename(columns={"ISO_3DIGIT": "iso_a3"}, inplace=True)

    world = world.merge(
        production_data, left_on="iso_a3", right_on="iso_a3", how="left"
    )

    world[data_category + "_average_yield"] = (
        world[data_category + "_production"] / world[data_category + "_area"]
    )
    return world


def remove_row_if_any_column_is_nan(world, columns):
    # Mask for rows where any of the specified columns are missing
    mask = world[columns].isna()
    rows_to_update = mask.any(axis=1)

    # Set the values in the specified columns to NaN for those rows
    world.loc[rows_to_update, columns] = np.nan

    return world


# FUNCTIONS FOR by-CELL BELOW


def compare_yields_with_model_by_cell(
    source_name,
    yields_ascii_file,
    modelled_yields_ascii_file,
    area_ascii_file,
    suffix,
    x_label,
    y_label,
    title,
):
    source_data = import_ascii(yields_ascii_file, "yields")
    model_data = import_ascii(modelled_yields_ascii_file, "yields")
    crop_area_data = import_ascii(area_ascii_file, "area")

    # remove any nan rows for any relevant dataset
    combined_intersection = (
        source_data.merge(
            model_data,
            on=["lat", "lon"],
            how="inner",
            suffixes=(f"_{suffix}", "_model"),
        )
        .merge(crop_area_data, on=["lat", "lon"], how="inner")
        .dropna(subset=[f"yields_{suffix}", "yields_model", "area"])
    )

    combined_intersection = combined_intersection[combined_intersection["area"] > 10]

    scatter_points_with_weights(
        dataframe=combined_intersection,
        expected_data_column_name=f"yields_{suffix}",
        observed_data_column_name="yields_model",
        weights=combined_intersection,
        weights_column_name="area",
        x_axis_label=x_label,
        y_axis_label=y_label,
        title=title,
    )


def scatter_points_with_weights(
    dataframe,
    expected_data_column_name,
    observed_data_column_name,
    weights,
    weights_column_name,
    x_axis_label,
    y_axis_label,
    title,
):
    weights_values = weights[weights_column_name]
    weights_normalized = weights_values / np.average(weights_values)

    plt.figure()

    # Normalize the values for color mapping
    norm = mcolors.Normalize(
        vmin=weights_values.min(),
        vmax=weights_values.max(),
    )
    cmap = plt.cm.viridis

    # Scale the weights for size mapping
    sizes = weights_values / weights_values.max() * 100

    plt.scatter(
        dataframe[expected_data_column_name],
        dataframe[observed_data_column_name],
        c=weights_values,
        cmap=cmap,
        norm=norm,
        s=sizes,
    )

    plt.colorbar(label="crop area of cell")

    # Add a dotted line where x=y
    x = np.linspace(*plt.xlim())
    plt.plot(x, x, "--", color="k")

    r_squared, WLS, rmse, fractional_rmse, d_stat, linear_RMSE = get_stats(
        dataframe,
        observed_data_column_name,
        expected_data_column_name,
        weights_normalized,
    )

    # Define a suitable x and y coordinate for the R-squared label
    x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
    y_pos1 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.9 + 0.4
    )  # 90% from the bottom
    y_pos2 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.85 + 0.4
    )  # 90% from the bottom
    y_pos3 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.8 + 0.4
    )  # 90% from the bottom
    y_pos4 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.75 + 0.4
    )  # 90% from the bottom
    y_pos5 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.7 + 0.4
    )  # 90% from the bottom
    y_pos6 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (
        0.65 + 0.4
    )  # 90% from the bottom

    plt.text(
        x_pos, y_pos1, f"R^2 = {r_squared:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos2, f"WLS Weighted R^2 = {WLS:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot

    # plt.text(
    #     x_pos, y_pos3, f"log RMSE  = {rmse:.2f}", fontsize=12
    # )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos3, f"Fractional RMSE = {fractional_rmse:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos4, f"d_stat = {d_stat:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos5, f"linear RMSE = {linear_RMSE:.2f} kg/ha", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.title(title)
    plt.xlabel(x_axis_label)
    plt.ylabel(y_axis_label)
    plt.title(title)
    plt.tight_layout()
    plt.show(block=False)


def import_ascii(filename, save_column):
    # Load the data into a numpy array

    with open(filename) as f:
        lines = f.readlines()
    # Get the header information from the .asc file

    assert lines[0][-2] == "N" or lines[0][-2] == "S"
    assert lines[1][-2] == "N" or lines[1][-2] == "S"
    assert lines[2][-2] == "E" or lines[2][-2] == "W"
    assert lines[3][-2] == "E" or lines[3][-2] == "W"

    north = extract_coordinate(lines[0])
    south = extract_coordinate(lines[1])
    east = extract_coordinate(lines[2])
    west = extract_coordinate(lines[3])

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
    df[save_column] = data.flatten()

    # Create a Geopandas DataFrame with the Point geometry
    df["geometry"] = df.apply(lambda x: Point(x["lon"], x["lat"]), axis=1)
    gdf = gpd.GeoDataFrame(df, geometry="geometry")

    return gdf


def extract_coordinate(line):
    # Remove all non-numeric, non-dot, non-minus characters
    cleaned_line = re.sub(r"[^0-9.-]", "", line.split(":")[1])
    multiplier = 1 if "N" in line or "E" in line else -1
    return multiplier * float(cleaned_line)


if __name__ == "__main__":
    main()
