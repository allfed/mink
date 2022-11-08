# -*- coding: utf-8 -*-
"""
Created on Sun Oct 9 13:31:00 2022

@author: Morgan Rivers
"""
from itertools import groupby
import pandas as pd
import matplotlib.pyplot as plt

# import geopandas as gpd
import numpy as np

# import cartopy.crs as ccrs
import glob


def import_data():
    # read in the data for the crop data for control and nuclear winter
    # loc = glob.glob(".")  # Control
    simulation_results = pd.read_csv(
        glob.glob(r"./Simulation_All_Crop_Scenario_State_Planting_cleaned.csv")[0]
    )
    crop_data = pd.read_csv(glob.glob(r"./point_values_yield_irrigation.csv")[0])

    crop_dictionary = {
        "WHEA": "Wheat",
        "MAIZ": "Maize",
        "SOYB": "Soybean",
        "RAPE": "Canola",
        "POTA": "Potato",
    }

    crop_data.replace({"crop": crop_dictionary}, inplace=True)

    return simulation_results, crop_data


def combine_irrigation_and_rainfed_yields(yields, crop_data, nutrition_dictionary):
    crops = yields.groupby("Crop")
    historical_yields = {}
    historical_yields_rainfed = {}
    historical_nutrition = {}
    historical_nutrition_rainfed = {}
    average_yields_by_crop = {}
    average_yields_by_crop_rainfed = {}
    average_nutrition_by_crop = {}
    average_nutrition_by_crop_rainfed = {}
    for crop_name, crop_yields in crops:

        this_crop_data = crop_data[crop_data["crop"] == crop_name]
        historical_yield = this_crop_data["yield"]
        historical_yields_rounded = round(historical_yield.values[0], 1)
        historical_yields[crop_name] = historical_yields_rounded
        historical_nutrition[crop_name] = (
            historical_yields_rounded * nutrition_dictionary[crop_name]
        )

        historical_yield_rainfed = this_crop_data["rainfed_yield"]
        historical_yield_rainfed_rounded = round(historical_yield_rainfed.values[0], 1)

        historical_yields_rainfed[crop_name] = historical_yield_rainfed_rounded
        historical_nutrition_rainfed[crop_name] = (
            historical_yield_rainfed_rounded * nutrition_dictionary[crop_name]
        )

        treatments = crop_yields.groupby("Treatment")
        all_cropland_sum = 0
        crop_weighted_sum = 0
        even_weighted_sum = 0
        just_rainfed = 0
        for treatment_name, treatment_yield in treatments:
            cropland_this_treatment_series = this_crop_data[
                treatment_name.lower() + "_cropland"
            ]
            assert len(cropland_this_treatment_series.values) == 1
            cropland_this_treatment = float(cropland_this_treatment_series.values[0])
            all_cropland_sum += cropland_this_treatment

            # includes all planting years, cultivars for treatment
            # HWAH === Harvest Weight At Harvest
            mean = treatment_yield["HWAH"].mean()
            crop_weighted_sum += cropland_this_treatment * mean
            even_weighted_sum += 0.5 * mean
            if treatment_name.lower() == "rainfed":
                just_rainfed = mean

        if all_cropland_sum == 0 or np.isnan(all_cropland_sum):
            weighted_average = even_weighted_sum
        else:
            weighted_average = crop_weighted_sum / all_cropland_sum

        # print("weighted_average by rainfed")
        # print(just_rainfed / weighted_average)
        # assert len(weighted_average) == 1
        average_yields_by_crop[crop_name] = round(weighted_average, 1)
        average_nutrition_by_crop[crop_name] = (
            round(weighted_average, 1) * nutrition_dictionary[crop_name]
        )
        average_yields_by_crop_rainfed[crop_name] = round(just_rainfed, 1)
        average_nutrition_by_crop_rainfed[crop_name] = (
            round(just_rainfed, 1) * nutrition_dictionary[crop_name]
        )
    return (
        average_yields_by_crop,
        average_nutrition_by_crop,
        average_yields_by_crop_rainfed,
        average_nutrition_by_crop_rainfed,
        historical_yields,
        historical_nutrition,
        historical_yields_rainfed,
        historical_nutrition_rainfed,
    )


def find_max_value_and_index_all_crops(
    list_of_yields, list_of_planting_dates, nutrition_dictionary
):
    # convert into dictionary of list
    # with list as values
    yields = {key: [i[key] for i in list_of_yields] for key in list_of_yields[0]}
    best_yields = {}
    planting_dates = {}
    for crop_name, list_yields_this_crop in yields.items():

        planting_dates[crop_name] = list_of_planting_dates[
            list_yields_this_crop.index(max(list_yields_this_crop))
        ]
        best_yields[crop_name] = max(list_yields_this_crop)

    best_nutrition = {}

    for crop_name, best_yield in best_yields.items():
        best_nutrition[crop_name] = best_yield * nutrition_dictionary[crop_name]

    return planting_dates, best_yields, best_nutrition


# get the best planting dates and the average yields for irrigated and non-irrigated
def get_best_planting_date_yields_and_nutrition(group, crop_data):

    nutrition_dictionary = {
        "Wheat": 3340,
        "Maize": 3560,
        "Soybean": 3350,
        "Canola": 4940,
        "Potato": 670,
    }

    list_of_planting_dates = []
    list_of_yields = []
    list_of_yields_rainfed = []
    historical_yields = {}
    historical_yields_rainfed = {}
    for planting_date, planting_group in group.groupby("Planting"):
        (
            average_yields_by_crop,
            average_nutrition_by_crop,
            average_yields_by_crop_rainfed,
            average_nutrition_by_crop_rainfed,
            historical_yields,
            historical_nutrition,
            historical_yields_rainfed,
            historical_nutrition_rainfed,
        ) = combine_irrigation_and_rainfed_yields(
            planting_group, crop_data, nutrition_dictionary
        )

        list_of_yields.append(average_yields_by_crop)
        list_of_yields_rainfed.append(average_yields_by_crop_rainfed)
        list_of_planting_dates.append(planting_date)

    planting_dates, best_yields, best_nutrition = find_max_value_and_index_all_crops(
        list_of_yields, list_of_planting_dates, nutrition_dictionary
    )

    (
        planting_dates_rainfed,
        best_yields_rainfed,
        best_nutrition_rainfed,
    ) = find_max_value_and_index_all_crops(
        list_of_yields_rainfed, list_of_planting_dates, nutrition_dictionary
    )

    # return best_planting_date, yields
    return (
        planting_dates,
        best_yields,
        best_nutrition,
        best_yields_rainfed,
        best_nutrition_rainfed,
        historical_yields,
        historical_nutrition,
        historical_yields_rainfed,
        historical_nutrition_rainfed,
    )


def plot_yields(historical_yields, control_yields, state_name):

    # plt.show()
    # plt.plot(control_yields[key])
    keys = []
    h_yields = []
    co_yields = []
    ca_yields = []
    for key, value in historical_yields.items():
        keys.append(key)
        h_yields.append(historical_yields[key])
        co_yields.append(control_yields[key])
        # ca_yields.append(catastrophe_yields[key])
    plt.xlabel("historical")
    plt.ylabel("control")
    # plt.scatter(h_yields, np.divide(co_yields, h_yields))
    plt.scatter(h_yields, co_yields)
    PLOT_LOGLOG = False
    if PLOT_LOGLOG:
        plt.yscale("log")
        plt.xscale("log")
    for xyz in zip(h_yields, co_yields, keys):
        plt.annotate(xyz[2], xy=xyz[0:2], textcoords="data")

    #     # control_yields[key]


def plot_reduction(control_yields, catastrophe_yields, state_name):

    # plt.show()
    # plt.plot(catastrophe_yields[key])
    keys = []
    co_yields = []
    ca_yields = []
    for key, value in control_yields.items():
        keys.append(key)
        co_yields.append(control_yields[key])
        ca_yields.append(catastrophe_yields[key])
        # ca_yields.append(catastrophe_yields[key])
    ca_ratios = np.divide(ca_yields, co_yields)
    plt.xlabel("control yield")
    plt.ylabel("catastrophe fraction yields")
    plt.scatter(co_yields, ca_ratios)
    PLOT_LOGLOG = False
    if PLOT_LOGLOG:
        plt.xscale("log")
        plt.yscale("log")
    nonzero_yields = {}
    for xyz in zip(co_yields, ca_ratios, keys):
        co_yield = xyz[0]
        ca_yield = xyz[1]
        key = xyz[2]
        plt.annotate(key, xy=xyz[0:2], textcoords="data")
        PRINT_CONTROL_RESULTS = False
        if PRINT_CONTROL_RESULTS:
            print(state_name)
            print(key)
            print(str(control_yields[key]) + "kg / ha")
            print("")
        PRINT_CATASTROPHE_RESULTS = True
        if PRINT_CATASTROPHE_RESULTS:
            if ca_yield > 0:
                print(state_name)
                print(key)
                print(str(catastrophe_yields[key]) + "kg / ha")
                print("")


def plot_yields_scatter(
    control_yields_by_state, historical_yields_by_state, state_names, title
):

    fig = plt.figure()
    ax = fig.add_subplot(111)

    ax.grid()
    for state_name in state_names:
        # get the yields for each state
        control_yields = control_yields_by_state[state_name]
        historical_yields = historical_yields_by_state[state_name]

        plot_yields(historical_yields, control_yields, state_name)
    plt.legend(state_names)
    plt.axline((10**3, 10**3), (10**4, 10**4), color="black", linestyle="--")
    plt.title(title)
    plt.show()


def plot_nutrition_scatter(
    control_nutrition_by_state, historical_nutrition_by_state, state_names, title
):

    fig = plt.figure()
    ax = fig.add_subplot(111)

    ax.grid()
    for state_name in state_names:
        # get the nutrition for each state
        control_nutrition = control_nutrition_by_state[state_name]
        historical_nutrition = historical_nutrition_by_state[state_name]

        plot_yields(historical_nutrition, control_nutrition, state_name)
    plt.legend(state_names)
    plt.axline((10**3, 10**3), (10**4, 10**4), color="black", linestyle="--")
    plt.title(title)
    plt.show()


def plot_yields_fractional_reduction(
    control_yields_by_state, catastrophe_yields_by_state, state_names, title
):

    fig = plt.figure()
    ax = fig.add_subplot(111)

    ax.grid()
    for state_name in state_names:
        # get the yields for each state
        control_yields = control_yields_by_state[state_name]
        catastrophe_yields = catastrophe_yields_by_state[state_name]

        plot_reduction(control_yields, catastrophe_yields, state_name)

    plt.legend(state_names)
    plt.axhline(y=1, color="black", linestyle="--")
    plt.title(title)
    plt.show()


def main():
    catastrophe_years = [2006, 2007, 2008, 2009]

    simulation_results, crop_data = import_data()
    # for each state
    by_state = simulation_results.groupby("State")
    state_names = []

    catastrophe_yields_by_state = {}
    catastrophe_greenhouse_yields_by_state = {}
    catastrophe_rainfed_yields_by_state = {}
    control_rainfed_yields_by_state = {}
    control_yields_by_state = {}
    historical_yields_by_state = {}
    historical_yields_rainfed_by_state = {}

    catastrophe_nutrition_by_state = {}
    catastrophe_greenhouse_nutrition_by_state = {}
    catastrophe_rainfed_nutrition_by_state = {}
    control_rainfed_nutrition_by_state = {}
    control_nutrition_by_state = {}
    historical_nutrition_by_state = {}
    historical_nutrition_rainfed_by_state = {}

    for state_name, state in by_state:
        if state_name == "WA" or state_name == "CA":
            continue
        state_names.append(state_name)
        print("")
        print("")
        print(state_name)
        print("")
        for scenario_name, scenario in state.groupby("Scenario"):
            print("scenario_name")
            print(scenario_name)
            crop_data_by_state = crop_data[crop_data["state"] == state_name]
            if scenario_name == "Control":
                (
                    control_planting_dates,
                    control_yields,
                    control_nutrition,
                    control_rainfed_yields,
                    control_rainfed_nutrition,
                    historical_yields,
                    historical_nutrition,
                    historical_yields_rainfed,
                    historical_nutrition_rainfed,
                ) = get_best_planting_date_yields_and_nutrition(
                    scenario, crop_data_by_state
                )
            elif scenario_name == "Catastrophe":
                # for year in catastrophe_years:

                scenario_value = scenario[
                    (scenario["WYEAR"] == 2007) | (scenario["WYEAR"] == 2008)
                ]

                (
                    catastrophe_planting_dates,
                    catastrophe_yields,
                    catastrophe_nutrition,
                    catastrophe_rainfed_yields,
                    catastrophe_rainfed_nutrition,
                    historical_yields,
                    historical_nutrition,
                    historical_yields_rainfed,
                    historical_nutrition_rainfed,
                ) = get_best_planting_date_yields_and_nutrition(
                    scenario_value, crop_data_by_state
                )
            else:
                scenario_value = scenario[
                    (scenario["WYEAR"] == 2007) | (scenario["WYEAR"] == 2008)
                ]

                (
                    planting_dates,
                    catastrophe_greenhouse_yields,
                    catastrophe_greenhouse_nutrition,
                    catastrophe_greenhouse_rainfed_yields,
                    catastrophe_greenhouse_rainfed_nutrition,
                    historical_yields,
                    historical_nutrition,
                    historical_yields_rainfed,
                    historical_nutrition_rainfed,
                ) = get_best_planting_date_yields_and_nutrition(
                    scenario_value, crop_data_by_state
                )

        # append yields by state
        catastrophe_yields_by_state[state_name] = catastrophe_yields
        catastrophe_greenhouse_yields_by_state[
            state_name
        ] = catastrophe_greenhouse_yields
        control_yields_by_state[state_name] = control_yields
        catastrophe_rainfed_yields_by_state[state_name] = catastrophe_rainfed_yields
        control_rainfed_yields_by_state[state_name] = control_rainfed_yields
        historical_yields_by_state[state_name] = historical_yields
        historical_yields_rainfed_by_state[state_name] = historical_yields_rainfed

        catastrophe_nutrition_by_state[state_name] = catastrophe_nutrition
        catastrophe_greenhouse_nutrition_by_state[
            state_name
        ] = catastrophe_greenhouse_nutrition
        control_nutrition_by_state[state_name] = control_nutrition
        catastrophe_rainfed_nutrition_by_state[
            state_name
        ] = catastrophe_rainfed_nutrition
        control_rainfed_nutrition_by_state[state_name] = control_rainfed_nutrition
        historical_nutrition_by_state[state_name] = historical_nutrition
        historical_nutrition_rainfed_by_state[state_name] = historical_nutrition_rainfed

    # plot yields for control and historical
    plot_yields_scatter(
        control_yields_by_state,
        historical_yields_by_state,
        state_names,
        "control vs historical yields",
    )
    plot_nutrition_scatter(
        control_nutrition_by_state,
        historical_nutrition_by_state,
        state_names,
        "control vs historical nutrition",
    )

    # plot yields for control and catastrophe
    plot_yields_fractional_reduction(
        control_yields_by_state,
        catastrophe_yields_by_state,
        state_names,
        "reduction in catastrophe compared to control",
    )

    # plot yields for control and catastrophe
    plot_yields_fractional_reduction(
        control_yields_by_state,
        catastrophe_greenhouse_yields_by_state,
        state_names,
        "reduction in greenhouse compared to control",
    )
    # plot yields for control and catastrophe
    plot_yields_fractional_reduction(
        catastrophe_yields_by_state,
        catastrophe_greenhouse_yields_by_state,
        state_names,
        "catastrophe greenhouse compared to catastrophe",
    )

    # plot yields for control and catastrophe
    plot_yields_fractional_reduction(
        catastrophe_yields_by_state,
        catastrophe_rainfed_yields_by_state,
        state_names,
        "catastrophe: expected reduction in rainfed yields",
    )

    # plot yields for control and catastrophe
    plot_yields_fractional_reduction(
        control_yields_by_state,
        control_rainfed_yields_by_state,
        state_names,
        "control vs control rainfed",
    )

    print("RAINFED")

    # # plot yields for control and historical
    # plot_yields_fractional_reduction(
    #     control_yields_by_state,
    #     control_rainfed_yields_by_state,
    #     state_names,
    #     "Reduction in rainfed in predicted baseline climate yields",
    # )

    # # plot yields for control and catastrophe
    # plot_yields_fractional_reduction(
    #     historical_yields_by_state,
    #     historical_yields_rainfed_by_state,
    #     state_names,
    #     "Reduction in rainfed in historical yields",
    # )

    # # plot yields for control and catastrophe
    # plot_yields_scatter(
    #     control_rainfed_yields_by_state,
    #     historical_yields_rainfed_by_state,
    #     state_names,
    #     "Accuracy of prediction for baseline climate rainfed yields",
    # )


# let's average over all the crop yields for each cultivar for control and choose the best planting date

# let's average all the crops for years 6 7 and 8, and then pick the best planting date

if __name__ == "__main__":
    main()
