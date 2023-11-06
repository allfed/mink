"""
compare_yields.py

Description:
    Provides utilities to compare model-based crop yield predictions against historical data. 
    Offers functionalities from scatter plot visualizations to statistical measures.


    In detail, this script does the following:

    - Compare model results with historical results country-by-country and plot the results

    - Compare model results with historical results grid-cell-by-grid-cell and plot the results

    - Create statistical reporting of the comparison using a few different statistical measures in terms of how well 
    expected results compare to observed results, and print + save as csv, as well as show on the plots.

    - Creates histograms of the common ranges of yield values for different yields.

    - It also allows for comparison between other model results (specifically at this point AGMIP) and this model's 
    results.

    - Finally, it allows for comparison between SPAM and FAOSTAT yields (purely to show they line up properly, as SPAM 
      is made to agree with FAOSTAT).

Author:
    Morgan Rivers

"""


# Importing necessary libraries

import os
import pandas as pd

from data_loader import DataLoader
from plotter import Plotter
from stats_functions import StatsFunctions


git_root = os.path.join("..", "..", "")

table_data = {}

rf_and_ir = {
    # "RF": {
    #     "rf_or_ir": "_RF",
    #     "snx_ending": "wet_averaged_RF",
    #     "title": "Rainfed",
    # },
    # "IR": {
    #     "rf_or_ir": "_IR",
    #     "snx_ending": "wet_averaged_IR",
    #     "title": "Irrigated",
    # },
    "overall": {
        "rf_or_ir": "",
        "snx_ending": "wet_overall_yield",
        "title": "Overall",
    },
}


def main():
    yield_comparison_config = DataLoader.get_crop_variables("yield_comparison_config.yaml")

    all_yearly_averages = {}
    for crop_key, crop_value in yield_comparison_config["crop_codes"].items():
        for water_key, water_values in rf_and_ir.items():
            all_yearly_averages[crop_value["crop_nice_name"]] = display_results(
                crop_key,
                crop_value,
                water_key,
                water_values,
                yield_comparison_config["settings"],
            )

    # Filter out only the percent reduction yields and export them to a .csv file.
    export_percent_yields_to_csv(all_yearly_averages, 'by_country_by_year_percent_yields.csv')

    # plot_sum = yield_comparison_config["settings"]["plot_sum"]
    
    # if yield_comparison_config["settings"]["comparisons_to_run"][
    #     "plot_yield_over_time"
    # ]:
    #     plot_yield_over_time_map(all_yearly_averages, plot_sum)

    # process_table(table_data)

    print("\nHit Enter to close plots and exit")
    input()


def display_results(crop_key, crop_value, water_key, water_values, settings):
    # Load data

    comparisons_to_run = settings["comparisons_to_run"]
    crop_nice_name = crop_value["crop_nice_name"]
    cat_and_or_cntrl = settings["catastrophe_and_or_control"]
    check_catastrophe_and_or_control(cat_and_or_cntrl)
    snx_description = crop_value["snx_description_control"]
    agmip_code = crop_value["AGMIP_code"]
    snx_description_catastrophe = crop_value["snx_description_catastrophe"]
    snx_ending = water_values["snx_ending"]

    if settings['by_country']:
        all_yearly_averages = pd.DataFrame()

    # for plotting maps independent of catastrophe_or_control value for a given crop
    first_overall_loop = True
    for cat_or_cntrl in cat_and_or_cntrl:
        print("Loading data...\n\n")
        world, yearly_averages = DataLoader.load_data(
            git_root,
            crop_key,
            crop_value,
            water_key,
            water_values,
            settings,
            cat_or_cntrl,
            comparisons_to_run,
        )
         ##
         # Place holder for appending control and catastrophe data 
         ##
        if settings['by_country']:
            if all_yearly_averages.empty:
                all_yearly_averages = yearly_averages.copy()
            else:
                all_yearly_averages = pd.merge(all_yearly_averages, yearly_averages, on='iso_a3', how='outer')
                all_yearly_averages.drop(columns=['Country_y'], inplace=True)
                all_yearly_averages.rename(columns={'Country_x': 'Country'}, inplace=True)


        if water_key == "overall":
            show_comparisons_overall(
                world,
                first_overall_loop,
                crop_nice_name,
                yearly_averages,
                snx_description_catastrophe,
                snx_description,
                cat_and_or_cntrl,
                crop_key,
                crop_value,
                water_key,
                water_values,
                settings,
                cat_or_cntrl,
                snx_ending,
                comparisons_to_run,
            )

            first_overall_loop = False

        else:
            rf_or_ir = water_values["rf_or_ir"]
            title = water_values["title"]
            if comparisons_to_run["plot_model_vs_AGMIP"] and not world.empty and (not agmip_code == "SKIP_ME"):
                print("world3.5")
                print(world)
                print(world.columns)

                world = Plotter.scatter_country_averages(
                    world,
                    f"model{rf_or_ir}",
                    f"AGMIP{rf_or_ir}",
                    f"{cat_or_cntrl} {title}: {crop_nice_name} AGMIP vs Model based on Countries",
                )
    if settings['by_country']:
            yearly_averages = all_yearly_averages
    
    # Calling function toe calculate the percent yield reduction by country.
    # Percent reduction values are appended to yearly_averages and held separate
    # in percent_yields dataframe.

    yearly_averages = percent_yield(
        yearly_averages,
        crop_key    
    )

    return yearly_averages


def show_comparisons_overall(
    world,
    first_overall_loop,
    crop_nice_name,
    yearly_averages,
    snx_description_catastrophe,
    snx_description,
    cat_and_or_cntrl,
    crop_key,
    crop_value,
    water_key,
    water_values,
    settings,
    cat_or_cntrl,
    snx_ending,
    comparisons_to_run,
):
    if world.empty:
        print("No data to plot by country or by grid cell")
        return

    if comparisons_to_run["plot_SPAM_vs_FAOSTAT"] and first_overall_loop:
        world = Plotter.scatter_country_averages(
            world,
            "FAOSTAT",
            "SPAM",
            f"{crop_nice_name} SPAM vs FAOSTAT yields Based on Countries",
        )

    if comparisons_to_run["plot_SPAM_country_map"]:
        plot_SPAM_country_map(world, first_overall_loop, crop_nice_name)

    if comparisons_to_run["plot_model_country_map"]:
        plot_model_country_map_func(world, cat_or_cntrl, crop_nice_name)

    if comparisons_to_run["plot_hist_side_by_side"]:
        plot_histograms(world, cat_or_cntrl, crop_nice_name)

    observed_col = "model"
    expected_col = "SPAM"
    if comparisons_to_run["plot_by_country_scatter"]:
        print("world3")
        print(world.columns)

        handle_country_scatter_plots(
            world,
            crop_key,
            crop_value,
            water_key,
            water_values,
            settings,
            cat_and_or_cntrl,
            cat_or_cntrl,
            observed_col,
            expected_col,
            first_overall_loop,
            crop_nice_name,
            comparisons_to_run,
        )

    if comparisons_to_run["plot_cell_by_cell_scatter"]:
        handle_cell_by_cell_scatter_plots(
            git_root,
            crop_key,
            cat_or_cntrl,
            snx_description_catastrophe,
            snx_description,
            snx_ending,
            cat_and_or_cntrl,
            first_overall_loop,
            crop_nice_name,
        )

    if comparisons_to_run["plot_by_country_scatter"] or comparisons_to_run["plot_cell_by_cell_scatter"]:
        add_row_to_table(world, crop_value, observed_col, expected_col, cat_or_cntrl)


def plot_SPAM_country_map(world, first_overall_loop, crop_nice_name):
    if first_overall_loop:
        print("Plotting historical raster yield values...\n\n")
        Plotter.plot_gdf_properties(
            world,
            "SPAM",
            ["average_yield", "production"],
            "SPAM",
            crop_nice_name + ": ",
        )


def plot_yield_over_time_map(yearly_averages, plot_sum):
    print("Plotting average of rasters over time...\n\n")
    Plotter.plot_average_of_rasters_over_time(yearly_averages, plot_sum = plot_sum)


def plot_model_country_map_func(world, cat_or_cntrl, crop_nice_name):
    print("Plotting model yield values by country...\n\n")
    Plotter.plot_gdf_properties(
        world,
        f"model",
        ["average_yield", "production"],
        "SPAM",
        cat_or_cntrl + " " + crop_nice_name + ": ",
    )


def plot_histograms(world, cat_or_cntrl, crop_nice_name):
    print("Plotting histogram...\n\n")
    Plotter.plot_hist_side_by_side(
        cat_or_cntrl,
        crop_nice_name,
        world,
        ["SPAM_average_yield", f"model_average_yield", "FAOSTAT_average_yield"],
    )


def handle_country_scatter_plots(
    world,
    crop_key,
    crop_value,
    water_key,
    water_values,
    settings,
    cat_and_or_cntrl,
    cat_or_cntrl,
    observed_col,
    expected_col,
    first_overall_loop,
    crop_nice_name,
    comparisons_to_run,
):
    # Plot scatter plots
    print(f"Plotting country average yields {expected_col} vs {observed_col} ...\n\n")
    world = Plotter.scatter_country_averages(
        world,
        observed_col,
        expected_col,
        f"{crop_nice_name} {expected_col} vs {observed_col} based on Countries",
    )

    if first_overall_loop and "catastrophe" in cat_and_or_cntrl and "control" in cat_and_or_cntrl:
        # plot catastrophe and control against each other
        print("git_root")
        print(git_root)
        print("crop_key")
        print(crop_key)
        print("crop_value")
        print(crop_value)
        print("water_key")
        print(water_key)
        print("water_values")
        print(water_values)
        print("settings")
        print(settings)
        # load both so they can be plotted against each other
        if cat_or_cntrl == "catastrophe":
            world_control, _ = DataLoader.load_data(
                git_root,
                crop_key,
                crop_value,
                water_key,
                water_values,
                settings,
                "control",
                comparisons_to_run,
            )
            world_catastrophe = world
        else:
            world_catastrophe, _ = DataLoader.load_data(
                git_root,
                crop_key,
                crop_value,
                water_key,
                water_values,
                settings,
                "catastrophe",
                comparisons_to_run,
            )
            world_control = world

        print("world_control")
        print(world_control)
        print(world_control.columns)
        print("world_catastrophe")
        print(world_catastrophe)
        print(world_catastrophe.columns)
        if not world_catastrophe.empty and not world_control.empty:
            world["model_catastrophe_average_yield"] = world_catastrophe["model_average_yield"]
            world["model_catastrophe_production"] = world_catastrophe["model_production"]
            world["model_control_average_yield"] = world_control["model_average_yield"]
            world["model_control_production"] = world_control["model_production"]

            print(f"Plotting country average yields catastrophe vs control ...\n\n")
            world = Plotter.scatter_country_averages(
                world,
                "model_catastrophe",
                "model_control",
                f"{crop_nice_name} {expected_col} vs {observed_col} based on Countries",
            )


def handle_cell_by_cell_scatter_plots(
    git_root,
    crop_key,
    cat_or_cntrl,
    snx_description_catastrophe,
    snx_description,
    snx_ending,
    cat_and_or_cntrl,
    first_overall_loop,
    crop_nice_name,
):
    if cat_or_cntrl == "catastrophe":
        description_tag = snx_description_catastrophe
    else:
        description_tag = snx_description

    print("description_tag")
    print(description_tag)
    print("crop_key")
    print(crop_key)
    if description_tag == "SKIP_ME":
        return

    SPAM_yields_ascii_file = os.path.join(f"{git_root}wth_historical", f"{crop_key}_yield.asc")

    modelled_yields_ascii_file = os.path.join(
        f"{git_root}wth_{cat_or_cntrl}",
        f"379_Outdoor_crops_{cat_or_cntrl}_BestYield_noGCMcalendar_p0_{description_tag}_{snx_ending}.asc",
    )
    SPAM_area_ascii_file = os.path.join(f"{git_root}wth_historical", f"{crop_key}_cropland.asc")

    print("Plotting grid cell yields...\n\n")
    compare_yields_with_model_by_cell(
        SPAM_yields_ascii_file,
        modelled_yields_ascii_file,
        SPAM_area_ascii_file,
        "SPAM",
        "SPAM Yields",
        "Mink model yields",
        f"{cat_or_cntrl} {crop_nice_name} SPAM vs model yields per grid cell",
    )

    if first_overall_loop and "catastrophe" in cat_and_or_cntrl and "control" in cat_and_or_cntrl:
        # plot catastrophe and control against each other

        if snx_description == "SKIP_ME" or snx_description_catastrophe == "SKIP_ME":
            return

        # load both so they can be plotted against each other
        control_yields_ascii_file = os.path.join(
            f"{git_root}wth_control",
            f"379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_{snx_description}_{snx_ending}.asc",
        )

        catastrophe_yields_ascii_file = os.path.join(
            f"{git_root}wth_catastrophe",
            "379_Outdoor_crops_catastrophe_BestYield_noGCMcalendar_p0_"
            + f"{snx_description_catastrophe}_{snx_ending}.asc",
        )

        SPAM_area_ascii_file = os.path.join(f"{git_root}wth_historical", f"{crop_key}_cropland.asc")

        print("Plotting grid cell yields...\n\n")
        compare_yields_with_model_by_cell(
            control_yields_ascii_file,
            catastrophe_yields_ascii_file,
            SPAM_area_ascii_file,
            "Control",
            "Mink yields control",
            "Mink yields catastrophe",
            f"{crop_nice_name} control vs catastrophe yields per grid cell",
        )


def compare_yields_with_model_by_cell(
    yields_ascii_file,
    modelled_yields_ascii_file,
    area_ascii_file,
    baseline_tag,
    x_label,
    y_label,
    title,
):
    source_data = DataLoader.import_ascii(yields_ascii_file, "yields")
    model_data = DataLoader.import_ascii(modelled_yields_ascii_file, "yields")
    crop_area_data = DataLoader.import_ascii(area_ascii_file, "area")
    # remove any nan rows for any relevant dataset
    combined_intersection = (
        source_data.merge(
            model_data,
            on=["lat", "lon"],
            how="inner",
            suffixes=(f"_{baseline_tag}", "_model"),
        )
        .merge(crop_area_data, on=["lat", "lon"], how="inner")
        .dropna(subset=[f"yields_{baseline_tag}", "yields_model", "area"])
    )

    combined_intersection = combined_intersection[combined_intersection["area"] > 10]

    Plotter.scatter_points_with_weights(
        dataframe=combined_intersection,
        expected_data_column_name=f"yields_{baseline_tag}",
        observed_data_column_name="yields_model",
        weights=combined_intersection,
        weights_column_name="area",
        x_axis_label=x_label,
        y_axis_label=y_label,
        title=title,
    )


def check_catastrophe_and_or_control(cat_and_or_cntrl):
    valid_combinations = [
        ["catastrophe"],
        ["control"],
        ["catastrophe", "control"],
        ["control", "catastrophe"],
    ]
    assert cat_and_or_cntrl in valid_combinations, (
        "ERROR: catastrophe_and_or_control must be one of the following: "
        "['catastrophe'], ['control'], ['catastrophe', 'control'], ['control', 'catastrophe']"
    )


def add_row_to_table(world, crop_value, observed_col, expected_col, control_or_catastrophe):
    weights = StatsFunctions.get_weights(world, observed_col, expected_col)

    (
        r_squared,
        weighted_r2,
        _,
        relative_rmse,
        _,
        rmse,
    ) = StatsFunctions.get_stats(
        world,
        observed_col + "_average_yield",
        expected_col + "_average_yield",
        weights,
    )

    if "Crop Name" not in table_data:
        table_data["Crop Name"] = []
        table_data["Control or Catastrophe"] = []
        table_data[f"R^2 with {expected_col}"] = []
        table_data[f"Weighted R^2 with {expected_col}"] = []
        table_data[f"RMSE (kg/ha) with {expected_col}"] = []
        table_data[f"RRMSE (%) with {expected_col}"] = []
        table_data[f"Ratio {observed_col} to {expected_col} production"] = []
        table_data[f"{observed_col} production"] = []

    table_data["Crop Name"].append(crop_value["crop_nice_name"])
    table_data["Control or Catastrophe"].append(control_or_catastrophe)
    table_data[f"R^2 with {expected_col}"].append(round(r_squared, 2))
    table_data[f"Weighted R^2 with {expected_col}"].append(round(weighted_r2, 2))
    table_data[f"RMSE (kg/ha) with {expected_col}"].append(round(rmse, 2))
    table_data[f"RRMSE (%) with {expected_col}"].append(round(relative_rmse * 100, 2))  # Convert to percentage
    table_data[f"Ratio {observed_col} to {expected_col} production"].append(
        round(
            world[f"{observed_col}_production"].sum() / world[f"{expected_col}_production"].sum(),
            2,
        )
    )
    table_data[f"{observed_col} production"].append(
        round(world[f"{observed_col}_production"].sum(),2)
    )


def process_table(table_data):
    df = pd.DataFrame(table_data)

    if table_data not in [None, {}]:
        # Save the entire DataFrame as CSV
        df.to_csv("output_table.csv", index=False)

        # Set 'Crop Name' as the index for more meaningful output
        df.set_index("Crop Name", inplace=True)

        for condition in ["catastrophe", "control"]:
            sub_df = df[df["Control or Catastrophe"] == condition]

            if not sub_df.empty:
                print("\n\n\nGenerating results table for {}...".format(condition))
                print(sub_df.drop(columns=["Control or Catastrophe"]))
                # Loop through each column (excluding the 'Control or Catastrophe'), create a new DataFrame, and print
                for column in sub_df.columns:
                    if column != "Control or Catastrophe":
                        new_df = pd.DataFrame(sub_df[column])
                        print("\n\nData for column '{}' under {}:".format(column, condition))
                        print(new_df)
                        print("-" * 40)  # prints a separator line for clarity

def percent_yield(
        df, crop_key
):
    # Identify columns containing "catastrophe" and "yield" for the model. 
    # SPAM is historical data, not utilised but present in the dataframe incase its needed 
    catastrophe_cols = [col for col in df.columns if 'catastrophe' in col and 'yield' in col and 'SPAM' not in col]
    control_cols = [col for col in df.columns if 'control' in col and 'yield' in col and 'SPAM' not in col]

    # Assuming catastrophe and control columns are in the same order for each year.
    for cat_col, control_col in zip(catastrophe_cols, control_cols):
        percent_reduction = (df[cat_col] - df[control_col]) / df[control_col] * 100
        df[f'{crop_key}_percent_reduction_{cat_col.split("_")[-1]}'] = percent_reduction

    return df

def export_percent_yields_to_csv(data_dict, filename):

    df_to_export = None
    
    for key, df in data_dict.items():
        # Extract columns containing 'percent_reduction', 'iso_a3', and 'Country' and rename columns
        percent_yields = df.loc[:, ['iso_a3', 'Country'] + [col for col in df.columns if 'percent_reduction' in col]]
        percent_yields.rename(columns=lambda x: x.replace('_percent_reduction', ''), inplace=True)
        
        # Merge DataFrames based on 'iso_a3'
        if df_to_export is None:
            df_to_export = percent_yields
        else:
            df_to_export = pd.merge(df_to_export, percent_yields, on=['iso_a3', 'Country'], how='outer', suffixes=('', '_drop'))

    # Remove duplicate columns and NaN values
    df_to_export = df_to_export[[col for col in df_to_export.columns if not col.endswith('_drop')]]
    df_to_export.dropna(inplace=True)

    # Export combined DataFrame to csv
    df_to_export.to_csv(filename, index=False)

if __name__ == "__main__":
    main()
