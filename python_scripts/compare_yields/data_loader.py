"""
data_loader.py

Description:
    Contains functions dedicated to loading and preprocessing data for yield comparisons. 
    Supports operations like reading ASCII files, loading FAOSTAT data, and importing CSV data.

Author:
    Morgan Rivers
"""
import sys
import traceback
import os
import re
import yaml
from shapely.geometry import Point
import pandas as pd
import numpy as np
import geopandas as gpd


class DataLoader:
    @staticmethod
    def load_data(
        git_root,
        crop_key,
        crop_value,
        water_key,
        water_values,
        settings,
        cat_or_cntrl,
        comparisons_to_run,
    ):
        (
            agmip_code,
            faostat_code,
            snx_description,
            snx_description_catastrophe,
            rf_or_ir,
            snx_ending,
            skip_crop_load_for_agmip,
        ) = DataLoader.get_codes(crop_value, water_values)
        yearly_averages = None
        print("agmip_code")
        print(agmip_code)
        print("faostat_code")
        print(faostat_code)
        print("snx_description")
        print(snx_description)
        print("snx_description_catastrophe")
        print(snx_description_catastrophe)
        print("rf_or_ir")
        print(rf_or_ir)
        print("snx_ending")
        print(snx_ending)
        print("skip_crop_load_for_agmip")
        print(skip_crop_load_for_agmip)

        # reset the world to contain country boundaries and names and iso3 codes, but no other columns
        world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))

        # conditionally load by-country model data based on comparisons
        if cat_or_cntrl == "catastrophe":
            description_tag = snx_description_catastrophe
        else:
            description_tag = snx_description

        if description_tag == "SKIP_ME":
            return pd.DataFrame({}), yearly_averages

        # load average of grid cells over time if relevant comparisons are enabled
        if water_key == "overall" and (comparisons_to_run["plot_yield_over_time"]):
            cat_and_or_cntrl = settings["catastrophe_and_or_control"]

            # load world data by-country, by-year, then calcualte yearly by-country averages
            if settings["by_country"]:

                #Initialize an empty dataframe
                yearly_averages = pd.DataFrame()
                yearly_averages['iso_a3'] = world['iso_a3']
                yearly_averages['Country'] = world['name']
                yearly_averages['pop_est'] = world['pop_est']

                for year in settings['years']:

                    country_csv_filename = (
                        f"by_country_379_Outdoor_crops_{cat_or_cntrl}_BestYield_noGCMcalendar_p0"
                        + f"_{description_tag}_{snx_ending}_y{year}.csv"
                    )
                    # reset the world to contain country boundaries and names and iso3 codes, but no other columns
                    world = gpd.read_file(gpd.datasets.get_path("naturalearth_lowres"))

                    world = DataLoader.load_by_country_csv(
                    world,
                    git_root,
                    f"wth_{cat_or_cntrl}",
                    country_csv_filename,
                    f"model{rf_or_ir}",
                    )
                    
                    # Load historical data and merge with world dataframe containting catastrophe or control data
                    world = DataLoader.load_by_country_csv(
                    world,
                    git_root,
                    "wth_historical",
                    f"by_country_{crop_key}_yield.csv",
                    "SPAM",
                    )

                    # area is the sname no matter what year. no sense appending the area n_year times.
                    if not any('area' in col for col in yearly_averages.columns):
                        column_keys = ['production', 'yield', 'area']
                    else:
                        column_keys = ['production', 'yield']
                    
                    selected_column_keys = [col for col in world.columns if any(keyword in col for keyword in column_keys)]

                    # Append data for each year to "yearly_averages"
                    for key in selected_column_keys:
                        new_column_name = f"{crop_key}_{cat_or_cntrl}_{key}_y{year}"
                        yearly_averages[new_column_name] = world[key]

                yearly_averages = yearly_averages.dropna(
                    subset=[new_column_name]
                )
                # Only necessary to reset index after the loop.
                yearly_averages.reset_index(drop=True, inplace=True)
            
            # this operates on a by-grid-cell basis, not by-country
            else :

                yearly_averages = DataLoader.get_average_of_rasters_over_time(
                    settings["years"],
                    git_root,
                    crop_key,
                    cat_and_or_cntrl,
                    snx_description,
                    snx_description_catastrophe,
                )

        # conditionally load by-country model data based on comparisons
        if cat_or_cntrl == "catastrophe":
            description_tag = snx_description_catastrophe
        else:
            description_tag = snx_description

        if description_tag == "SKIP_ME":
            return pd.DataFrame({}), yearly_averages

        country_csv_filename = (
            f"by_country_379_Outdoor_crops_{cat_or_cntrl}_BestYield_noGCMcalendar_p0"
            + f"_{description_tag}_{snx_ending}.csv"
        )

        # below are all the data loaded on a by-country basis

        rows_to_filter_nans_from = []
        if (
            comparisons_to_run["plot_by_country_scatter"]
            or comparisons_to_run["plot_model_country_map"]
            or comparisons_to_run["plot_model_vs_AGMIP"]
            or comparisons_to_run["plot_cell_by_cell_scatter"]
            or comparisons_to_run["plot_SPAM_country_map"]
            or comparisons_to_run["plot_hist_side_by_side"]
        ):
            world = DataLoader.load_by_country_csv(
                world,
                git_root,
                f"wth_{cat_or_cntrl}",
                country_csv_filename,
                f"model{rf_or_ir}",
            )
            rows_to_filter_nans_from.append(f"model{rf_or_ir}_average_yield")
            rows_to_filter_nans_from.append(f"model{rf_or_ir}_production")
            rows_to_filter_nans_from.append(f"model{rf_or_ir}_area")

        # conditionally load relevant datasets for overall water key
        if water_key == "overall":
            if comparisons_to_run["plot_SPAM_vs_FAOSTAT"] or comparisons_to_run["plot_hist_side_by_side"]:
                world = DataLoader.load_faostat_data(world, git_root, faostat_code, settings["faostat_data_location"])
                rows_to_filter_nans_from.append("FAOSTAT_average_yield")

            if (
                comparisons_to_run["plot_SPAM_vs_FAOSTAT"]
                or comparisons_to_run["plot_SPAM_country_map"]
                or comparisons_to_run["plot_hist_side_by_side"]
                or comparisons_to_run["plot_model_country_map"]
                or comparisons_to_run["plot_cell_by_cell_scatter"]
                or comparisons_to_run["plot_by_country_scatter"]  # need the area in this case
            ):
                world = DataLoader.load_by_country_csv(
                    world,
                    git_root,
                    "wth_historical",
                    f"by_country_{crop_key}_yield.csv",
                    "SPAM",
                )
                rows_to_filter_nans_from.append("SPAM_average_yield")
                rows_to_filter_nans_from.append("SPAM_production")
                rows_to_filter_nans_from.append("SPAM_area")

        elif water_key in ["RF", "IR"]:
            if (not skip_crop_load_for_agmip) and comparisons_to_run["plot_model_vs_AGMIP"]:
                world = DataLoader.load_by_country_csv(
                    world,
                    git_root,
                    "grassdata/world/AGMIP",
                    f"by_country_AGMIP_princeton{rf_or_ir}_yield_{agmip_code}_lowres_cleaned_2005.csv",
                    f"AGMIP{rf_or_ir}",
                )
                rows_to_filter_nans_from.append(f"AGMIP{rf_or_ir}_average_yield")
                rows_to_filter_nans_from.append(f"AGMIP{rf_or_ir}_production")

        # print("rows_to_filter_nans_from")
        # print(rows_to_filter_nans_from)
        world = DataLoader.remove_rows_with_nan(
            world,
            rows_to_filter_nans_from,
        )
        print("world2")
        print(world)
        print(world.columns)
        return (world, yearly_averages)

    @staticmethod
    def get_codes(crop_value, water_values):
        agmip_code = crop_value["AGMIP_code"]
        faostat_code = crop_value["FAOSTAT_code"]
        snx_description = crop_value["snx_description_control"]
        snx_description_catastrophe = crop_value["snx_description_catastrophe"]
        rf_or_ir = water_values["rf_or_ir"]
        snx_ending = water_values["snx_ending"]
        # Skip crops that are not modelled by AGMIP for AGMIP visualization
        skip_crop_load_for_agmip = agmip_code == "SKIP_ME"

        return (
            agmip_code,
            faostat_code,
            snx_description,
            snx_description_catastrophe,
            rf_or_ir,
            snx_ending,
            skip_crop_load_for_agmip,
        )

    @staticmethod
    def get_average_of_rasters_over_time(
        years,
        git_root,
        crop_key,
        cat_or_cntrl_list,
        snx_description,
        snx_description_catastrophe,
    ):
        SPAM_production_ascii_file = os.path.join(f"{git_root}wth_historical", f"{crop_key}_production.asc")
        try:
            historical_data = DataLoader.import_ascii(SPAM_production_ascii_file, "production")
        except FileNotFoundError:
            print(f"Error: File {SPAM_production_ascii_file} not found!")
            print(
                "Ensure the './generate_scenarios_csv.sh scenarios/your_config_file.yaml' command was",
                "run in the GRASS enabled singularity terminal.",
            )
            traceback.print_exc()  # This prints the detailed error message with line numbers.
            sys.exit(1)

        data_list = []

        for cat_or_cntrl in cat_or_cntrl_list:
            if cat_or_cntrl == "catastrophe":
                each_year_description_tag = snx_description_catastrophe
            else:
                each_year_description_tag = snx_description

            if each_year_description_tag == "SKIP_ME":
                continue

            each_year_base_filename = (
                f"379_Outdoor_crops_"
                + cat_or_cntrl
                + "_BestYield_noGCMcalendar_p0_"
                + each_year_description_tag
                + "_wet_production"
            )

            for year in years:
                ascii_loc = os.path.join(
                    f"{git_root}wth_{cat_or_cntrl}",
                    each_year_base_filename + "_y" + str(year) + ".asc",
                )
                try:
                    model_results_by_year = DataLoader.import_ascii(ascii_loc, "production")
                except FileNotFoundError:
                    print(f"Error: File {ascii_loc} not found!")
                    print(
                        "Please set 'make_rasters_comparing_overall_to_historical' to true in the config yaml",
                        "in the scenarios/ folder",
                    )
                    print(
                        "Ensure the './run_from_csv.sh scenarios/your_config_file.yaml process' command ",
                        "was run in the GRASS enabled singularity terminal with that setting and finished succesfully.",
                    )
                    traceback.print_exc()  # This prints the detailed error message with line numbers.
                    sys.exit(1)

                # Merge the dataframes on the basis of lat and lon
                # combined_data = pd.merge(
                #     model_results_by_year,
                #     historical_data[["lat", "lon", "production"]],
                #     on=["lat", "lon"],
                #     how="left",
                #     suffixes=("", "_historical"),
                # )

                # Drop rows where either of the yields columns is NaN
                # combined_data = DataLoader.remove_rows_with_nan(
                #     combined_data, ["production", "production_historical"]
                # )

                model_sum = model_results_by_year["production"].sum()
                historical_sum = historical_data["production"].sum()

                sum_ratio = model_sum / historical_sum / 1000
                # Check if year is already in the data_list
                year_entry = next((item for item in data_list if item["year"] == year), None)

                if year_entry:
                    # If year entry already exists, update the mean for the current cat_or_cntrl
                    year_entry[f"ratio_{cat_or_cntrl}"] = sum_ratio
                else:
                    # If year entry does not exist, append a new one
                    data_list.append({"year": year, f"ratio_{cat_or_cntrl}": sum_ratio, f"sum_{cat_or_cntrl}": model_sum})

        # Convert the list to a dataframe
        yearly_averages = pd.DataFrame(data_list)
        return yearly_averages

        # this is the same function commented out, but with yields per hectare, not overall production
        # def get_average_of_rasters_over_time(
        #     years,
        #     git_root,
        #     crop_key,
        #     cat_or_cntrl_list,
        #     snx_description,
        #     snx_description_catastrophe,
        # ):
        #     SPAM_yields_ascii_file = os.path.join(
        #         f"{git_root}wth_historical", f"{crop_key}_production.asc"
        #     )
        #     historical_data = DataLoader.import_ascii(SPAM_yields_ascii_file, "yields")

        #     data_list = []

        #     for cat_or_cntrl in cat_or_cntrl_list:
        #         if cat_or_cntrl == "catastrophe":
        #             each_year_description_tag = snx_description_catastrophe
        #         else:
        #             each_year_description_tag = snx_description

        #         each_year_base_filename = (
        #             f"379_Outdoor_crops_"
        #             + cat_or_cntrl
        #             + "_BestYield_noGCMcalendar_p0_"
        #             + each_year_description_tag
        #             + "_wet_overall_yield"
        #         )

        #         for year in years:
        #             ascii_loc = os.path.join(
        #                 f"{git_root}wth_{cat_or_cntrl}",
        #                 each_year_base_filename + "_y" + str(year) + ".asc",
        #             )
        #             model_results_by_year = DataLoader.import_ascii(ascii_loc, "yields")

        #             # Merge the dataframes on the basis of lat and lon
        #             combined_data = pd.merge(
        #                 model_results_by_year,
        #                 historical_data[["lat", "lon", "yields"]],
        #                 on=["lat", "lon"],
        #                 how="left",
        #                 suffixes=("", "_historical"),
        #             )

        #             # Drop rows where either of the yields columns is NaN
        #             combined_data = DataLoader.remove_rows_with_nan(
        #                 combined_data, ["yields", "yields_historical"]
        #             )

        #             model_mean = combined_data["yields"].mean()
        #             historical_mean = combined_data["yields_historical"].mean()

        #             mean_value = model_mean / historical_mean

        #             # Check if year is already in the data_list
        #             year_entry = next(
        #                 (item for item in data_list if item["year"] == year), None
        #             )

        #             if year_entry:
        #                 # If year entry already exists, update the mean for the current cat_or_cntrl
        #                 year_entry[f"mean_{cat_or_cntrl}"] = mean_value
        #             else:
        #                 # If year entry does not exist, append a new one
        #                 data_list.append({"year": year, f"mean_{cat_or_cntrl}": mean_value})

        #     # Convert the list to a dataframe
        #     yearly_averages = pd.DataFrame(data_list)
        #     return yearly_averages

    @staticmethod
    def get_crop_variables(yaml_file):
        with open(yaml_file, "r") as file:
            data = yaml.load(file, Loader=yaml.FullLoader)
        return data

    @staticmethod
    def import_ascii(filename, save_column):
        # Load the data into a numpy array

        with open(filename) as f:
            lines = f.readlines()
        # Get the header information from the .asc file

        assert lines[0][-2] == "N" or lines[0][-2] == "S"
        assert lines[1][-2] == "N" or lines[1][-2] == "S"
        assert lines[2][-2] == "E" or lines[2][-2] == "W"
        assert lines[3][-2] == "E" or lines[3][-2] == "W"

        north = DataLoader.extract_coordinate(lines[0])
        south = DataLoader.extract_coordinate(lines[1])
        east = DataLoader.extract_coordinate(lines[2])
        west = DataLoader.extract_coordinate(lines[3])

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

    @staticmethod
    def extract_coordinate(line):
        # Remove all non-numeric, non-dot, non-minus characters
        cleaned_line = re.sub(r"[^0-9.-]", "", line.split(":")[1])
        multiplier = 1 if "N" in line or "E" in line else -1
        return multiplier * float(cleaned_line)

    @staticmethod
    def remove_rows_with_nan(world, columns):
        # Mask for rows where any of the specified columns are missing
        mask = world[columns].isna()
        rows_to_drop = mask.any(axis=1)

        # Show rows where dataframe for countries has NaN values
        rows_with_nan = world[rows_to_drop]

        # Drop rows with NaN values in specified columns
        world = world.drop(index=world[rows_to_drop].index)

        return world

    @staticmethod
    def load_faostat_data(world, git_root, faostat_code, FAOSTAT_data_loc):
        csv_loc = f"{git_root}/{FAOSTAT_data_loc}"
        try:
            df = pd.read_csv(csv_loc)
        except FileNotFoundError:
            print(f"Error: File {csv_loc} not found!")
            print(
                "and run './run_from_csv.sh scenarios/your_config_file.yaml process' again in the GRASS enabled ",
                "singularity terminal.",
            )
            traceback.print_exc()  # This prints the detailed error message with line numbers.
            sys.exit(1)
        df["Year"] = pd.to_numeric(df["Year"], errors="coerce")
        df["Value"] = pd.to_numeric(df["Value"], errors="coerce") / 10
        df = df[(df["Year"] >= 2004) & (df["Year"] <= 2006) & (df["Item"] == faostat_code)]
        result = df.groupby("Area Code (ISO3)")["Value"].mean().reset_index()
        result.columns = ["ISO3", "FAOSTAT_average_yield"]
        world = world.merge(result, left_on="iso_a3", right_on="ISO3", how="left")
        print("world")
        print(world)
        print(world.columns)
        return world

    @staticmethod
    def load_by_country_csv(
        world,
        git_root,
        csv_folder,
        country_csv_filename,
        data_category,
    ):
        production_area_by_country_csv = os.path.join(git_root, csv_folder, country_csv_filename)

        try:
            production_data = pd.read_csv(production_area_by_country_csv)
        except FileNotFoundError:
            print(f"Error: File {production_area_by_country_csv} not found!")
            print(
                "Please set 'make_rasters_comparing_overall_to_historical' to true in the config yaml in ",
                "the scenarios/ folder",
            )
            print(
                "and run './run_from_csv.sh scenarios/your_config_file.yaml process' again in the ",
                "GRASS enabled singularity terminal.",
            )
            traceback.print_exc()  # This prints the detailed error message with line numbers.
            sys.exit(1)

        # Since the csv files have different column names this code ensures there are no errors by 
        # labeling correct column name. If the filename has "_y{number}.csv" ending, then assert different
        # column names

        if (re.search(r"_y\d+\.csv$", country_csv_filename) and 
            re.search(r"control", country_csv_filename)):
            assert "Production (kg wet)" in production_data.columns
            assert "Area (hectares)" in production_data.columns

            production_data.rename(
                columns={"Production (kg wet)": data_category + "_production"}, inplace=True
            )
            production_data.rename(
                columns={"Area (hectares)": data_category + "_area"}, inplace=True
            )
        else:

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
            production_data, left_on="iso_a3", right_on="iso_a3", how="outer"
        )
        world[data_category + "_average_yield"] = (
            world[data_category + "_production"] / world[data_category + "_area"]
        )
        world_no_zero_area_or_non_existent_countries = world.dropna(subset=[data_category + "_average_yield"])

        return world_no_zero_area_or_non_existent_countries
