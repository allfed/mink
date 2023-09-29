import re
import numpy as np
import os

import pandas as pd
import geopandas as gpd
from shapely.geometry import Point

import matplotlib.pyplot as plt
import matplotlib.colors as mcolors

import statsmodels.api as sm
from statsmodels.tools.eval_measures import rmse as statmodel_rmse
from scipy.stats import linregress

# from make_powerpoint import MakePowerpoint
# from matplotlib.colors import ListedColormap

git_root = "../"

# Configuration dictionary for controlling various plots
config = {
    "plot_AGMIP_vs_model": True,
    "plot_SPAM_vs_model": True,
    "export_per_cell_data": True,
}


def main():
    SPAM_area_ascii_file = os.path.join(
        f"{git_root}wth_historical", "WHEA_rainfed_cropland.asc"
    )

    SPAM_yields_ascii_file = os.path.join(f"{git_root}wth_historical", "WHEA_yield.asc")

    AGMIP_yields_ascii_file = os.path.join(
        f"{git_root}grassdata/world/AGMIP",
        "AGMIP_princeton_RF_yield_whe_lowres_cleaned_2005.asc",
    )

    AGMIP_planting_day_ascii_file = os.path.join(
        f"{git_root}grassdata/world/AGMIP",
        "AGMIP_princeton_RF_plant-day_whe_lowres_cleaned_2005.asc",
    )

    # modelled_yields_ascii_file = os.path.join(
    #     f"{git_root}wth_control",
    #     "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_genSNX_wet_averaged_RF.asc",
    # )
    modelled_yields_ascii_file = os.path.join(
        f"{git_root}wth_control",
        "379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Aug04_genSNX_wet_overall_yield.asc",
    )

    if config.get("plot_SPAM_vs_model", True):
        compare_yields_with_model(
            "SPAM",
            SPAM_yields_ascii_file,
            modelled_yields_ascii_file,
            SPAM_area_ascii_file,
            "SPAM",
            "SPAM Yields",
            "Mink model yields",
            "Comparison between SPAM and model yields per grid cell",
        )

    if config.get("plot_AGMIP_vs_model", True):
        compare_yields_with_model(
            "AGMIP",
            AGMIP_yields_ascii_file,
            modelled_yields_ascii_file,
            SPAM_area_ascii_file,
            "AGMIP",
            "AGMIP Yields",
            "Mink model yields",
            "Comparison between AGMIP and model yields per grid cell",
        )

    input()

    if config.get("export_per_cell_data", True):
        SPAM_yields_data = import_ascii(SPAM_yields_ascii_file, "yields")
        modelled_yields_data = import_ascii(modelled_yields_ascii_file, "yields")
        AGMIP_yields_data = import_ascii(AGMIP_yields_ascii_file, "yields")

        SPAM_yields_data.to_csv("SPAM_yields_data.csv")
        modelled_yields_data.to_csv("modelled_yields_data.csv")
        AGMIP_yields_data.to_csv("AGMIP_yields_data.csv")

        # export AGMIP and model maturity dates and planting dates.


def compare_yields_with_model(
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
            suffixes=(f"_ {suffix}", "_model"),
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


def extract_coordinate(line):
    # Remove all non-numeric, non-dot, non-minus characters
    cleaned_line = re.sub(r"[^0-9.-]", "", line.split(":")[1])
    multiplier = 1 if "N" in line or "E" in line else -1
    return multiplier * float(cleaned_line)


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

    # Plot the data


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

    # Normalize the SPAM_area values for color mapping
    norm = mcolors.Normalize(
        vmin=weights_values.min(),
        vmax=weights_values.max(),
    )
    cmap = plt.cm.viridis

    # Scale theexpected_col +  _area values for size mapping
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

    r_squared, WLS, rmse, weighted_rmse, d_stat, linear_RMSE = get_stats(
        dataframe[expected_data_column_name],
        dataframe[observed_data_column_name],
        weights_normalized,
    )

    # Define a suitable x and y coordinate for the R-squared label
    x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
    y_pos1 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.9
    )  # 90% from the bottom
    y_pos2 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.85
    )  # 85% from the bottom
    y_pos3 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.8
    )  # 80% from the bottom
    y_pos4 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.75
    )  # 75% from the bottom
    y_pos5 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.7
    )  # 70% from the bottom
    y_pos6 = (
        plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * 0.65
    )  # 65% from the bottom

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
    plt.text(
        x_pos, y_pos5, f"d_stat = {d_stat:.2f}", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.text(
        x_pos, y_pos6, f"linear RMSE = {linear_RMSE:.2f} kg/ha", fontsize=12
    )  # Display weighted R-squared on the plot
    plt.xlabel(x_axis_label)
    plt.ylabel(y_axis_label)
    plt.title(title)
    # plt.savefig(title,
    #     dpi=200,
    #     figsize=(5, 5),
    # )
    plt.show(block=False)


def get_stats(expected_data, observed_data, weights):
    # Weighted least squares regression
    X = sm.add_constant(expected_data)
    y = observed_data

    model = sm.WLS(y, X, weights=weights).fit()
    print(f"Weighted R-squared: {model.rsquared}")

    # Calculate r-squared
    slope, intercept, r_value, p_value, std_err = linregress(
        expected_data, observed_data
    )
    print(f"R-squared: {r_value**2}")

    rmse = RMSE(expected_data, observed_data)
    print(f"RMSE data: {rmse}")

    weighted_rmse = weighted_RMSE(expected_data, observed_data, weights)
    print(f"weighted RMSE data: {weighted_rmse}")

    d_stat = d_statistic(expected_data, observed_data)
    linear_rmse = statmodel_rmse(expected_data, observed_data)

    return r_value**2, model.rsquared, rmse, weighted_rmse, d_stat, linear_rmse


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


if __name__ == "__main__":
    main()
