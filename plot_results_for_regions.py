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
    # print("simulation_results")
    # print(simulation_results)
    # print(simulation_results.iloc[1])
    # print(simulation_results.columns)
    crop_data = pd.read_csv(glob.glob(r"./point_values_yield_irrigation.csv")[0])

    crop_dictionary = {
        "WHEA": "Wheat",
        "MAIZ": "Maize",
        "SOYB": "Soybean",
        "RAPE": "Canola",
        "POTA": "Potato",
    }

    crop_data.replace({"crop": crop_dictionary}, inplace=True)

    # print("crop_data")
    # print(crop_data)

    # print("irrigated_cropland")
    # print(crop_data["irrigated_cropland"])
    # print("rainfed_cropland")
    # print(crop_data["rainfed_cropland"])
    # print("cropland")
    # print(crop_data["cropland"])

    irr_crop_ratio = crop_data["irrigated_cropland"] / (
        crop_data["irrigated_cropland"] + crop_data["rainfed_cropland"]
    )
    rf_crop_ratio = crop_data["rainfed_cropland"] / (
        crop_data["irrigated_cropland"] + crop_data["rainfed_cropland"]
    )

    # print("rainfed_yield")
    # print(crop_data["rainfed_yield"])
    # print(crop_data["rainfed_yield"])
    # print(crop_data)
    # print("irrigated_cropland ratio")
    # print(irr_crop_ratio)
    # print("rf_cropland ratio")
    # print(rf_crop_ratio)

    calculated_yields = (
        irr_crop_ratio * crop_data["irrigated_yield"]
        + rf_crop_ratio * crop_data["rainfed_yield"]
    )
    # print("expected average crop yields")
    # print(
    # irr_crop_ratio * crop_data["irrigated_yield"]
    # + rf_crop_ratio * crop_data["rainfed_yield"]
    # )
    # determine the average yield for each crop in each state in the control
    # print("yield")
    # print(crop_data["yield"])

    # plot above shows two ways of calculating yield are extremely well correlated
    # plt.figure()
    # plt.scatter(calculated_yields, crop_data["yield"])
    # plt.show()
    return simulation_results, crop_data


def combine_irrigation_and_rainfed_yields(yields, crop_data):
    crops = yields.groupby("Crop")
    historical_yields = {}
    dictionary_result = {}
    for crop_name, crop_yields in crops:

        this_crop_data = crop_data[crop_data["crop"] == crop_name]
        historical_yield = this_crop_data["yield"]
        historical_yields[crop_name] = round(historical_yield.values[0], 1)

        # print("expected_yield")
        # print(expected_yield)
        treatments = crop_yields.groupby("Treatment")
        all_cropland_sum = 0
        crop_weighted_sum = 0
        even_weighted_sum = 0
        for treatment_name, treatment_yield in treatments:
            cropland_this_treatment_series = this_crop_data[
                treatment_name.lower() + "_cropland"
            ]
            # print("cropland_this_treatment")
            # print(cropland_this_treatment_series)
            assert len(cropland_this_treatment_series.values) == 1
            cropland_this_treatment = float(cropland_this_treatment_series.values[0])
            all_cropland_sum += cropland_this_treatment

            # includes all planting years, cultivars for treatment
            # HWAH === Harvest Weight At Harvest
            mean = treatment_yield["HWAH"].mean()
            # print("mean")
            # print(mean)
            # if np.isnan(mean):
            # print(":(")
            # quit()
            crop_weighted_sum += cropland_this_treatment * mean
            even_weighted_sum += 0.5 * mean

        if all_cropland_sum == 0 or np.isnan(all_cropland_sum):
            # print("AAAH!")
            # print("AAAH!")
            # print("AAAH!")
            # print("AAAH!")
            weighted_average = even_weighted_sum
        else:
            weighted_average = crop_weighted_sum / all_cropland_sum
        # print("weighted_average")
        # print(weighted_average)
        # assert len(weighted_average) == 1
        dictionary_result[crop_name] = round(weighted_average, 1)
    return dictionary_result, historical_yields


def find_max_value_and_index_all_crops(list_of_yields, list_of_planting_dates):
    # convert into dictionary of list
    # with list as values
    # print("list_of_yields")
    # print("list_of_yields[0]")
    # print(list_of_yields[0])
    yields = {key: [i[key] for i in list_of_yields] for key in list_of_yields[0]}
    # print("yields")
    # print(yields)
    best_yields = {}
    planting_dates = {}
    for crop_name, list_yields_this_crop in yields.items():

        planting_dates[crop_name] = list_of_planting_dates[
            list_yields_this_crop.index(max(list_yields_this_crop))
        ]
        best_yields[crop_name] = max(list_yields_this_crop)

    # print("best_yields")
    # print("planting_dates")
    # print(best_yields)
    # print(planting_dates)
    return planting_dates, best_yields


# get the best planting dates and the average yields for irrigated and non-irrigated
def get_best_planting_date_yields(group, crop_data):
    list_of_planting_dates = []
    list_of_yields = []
    historical_yields = {}
    for planting_date, planting_group in group.groupby("Planting"):
        (
            average_yields_by_crop,
            historical_yields,
        ) = combine_irrigation_and_rainfed_yields(planting_group, crop_data)

        list_of_yields.append(average_yields_by_crop)
        # print(planting_date)
        list_of_planting_dates.append(planting_date)
    # print("list_of_yields")
    # print(list_of_yields)

    planting_dates, best_yields = find_max_value_and_index_all_crops(
        list_of_yields, list_of_planting_dates
    )

    # print("best_yields")
    # print(best_yields)

    # print("planting_dates")
    # print(planting_dates)

    # print("best_yields")
    # print(best_yields)

    # return best_planting_date, yields
    return planting_dates, best_yields, historical_yields


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
    print(h_yields)
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
    #     # print(key)
    #     # print("control_yields[key] / historical_yields[key]")
    #     # print(control_yields[key] / historical_yields[key])
    #     # print("catastrophe_yields[key] / control_yields[key]")
    #     # print(catastrophe_yields[key] / control_yields[key])
    #     # print("")


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
    # print(co_yields)
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
        PRINT_CONTROL_RESULTS = True
        if PRINT_CONTROL_RESULTS:
            print(state_name)
            print(key)
            print(str(control_yields[key]) + "kg / ha")
            print("")
        if ca_yield > 0:
            print(state_name)
            print(key)
            print(str(catastrophe_yields[key]) + "kg / ha")
            print("")
    return min(
        np.where(co_yields > np.linspace(0, 0, len(co_yields)), co_yields, 10 ^ 8)
    )
    #     # control_yields[key]
    #     # print(key)
    #     # print("control_yields[key] / historical_yields[key]")
    #     # print(control_yields[key] / historical_yields[key])
    #     # print("catastrophe_yields[key] / control_yields[key]")
    #     # print(catastrophe_yields[key] / control_yields[key])
    #     # print("")


def plot_yields_comparing_control_and_historical(
    control_yields_by_state, historical_yields_by_state, state_names
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
    plt.title("All the predicted yields compared to historical yields")
    plt.show()


def plot_yields_comparing_control_and_catastrophe(
    control_yields_by_state, catastrophe_yields_by_state, state_names
):

    fig = plt.figure()
    ax = fig.add_subplot(111)

    ax.grid()
    new_min = 10 ^ 8
    for state_name in state_names:
        # get the yields for each state
        control_yields = control_yields_by_state[state_name]
        catastrophe_yields = catastrophe_yields_by_state[state_name]

        new_min = min(
            plot_reduction(control_yields, catastrophe_yields, state_name), new_min
        )

    plt.legend(state_names)
    plt.axhline(y=1, color="black", linestyle="--")
    plt.title("Predicted reduction compared to control yields")
    plt.show()


def main():
    catastrophe_years = [2006, 2007, 2008, 2009]

    simulation_results, crop_data = import_data()
    # for each state
    by_state = simulation_results.groupby("State")
    state_names = []
    catastrophe_yields_by_state = {}
    control_yields_by_state = {}
    historical_yields_by_state = {}
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
                    historical_yields,
                ) = get_best_planting_date_yields(scenario, crop_data_by_state)
            else:
                # for year in catastrophe_years:

                # print("scenario")
                # print(scenario)
                # print(scenario["WYEAR"])
                scenario_value = scenario[
                    (scenario["WYEAR"] == 2007) | (scenario["WYEAR"] == 2008)
                ]

                # print("scenario_value")
                # print(scenario_value)
                (
                    catastrophe_planting_dates,
                    catastrophe_yields,
                    historical_yields,
                ) = get_best_planting_date_yields(scenario_value, crop_data_by_state)

        # append yields by state
        catastrophe_yields_by_state[state_name] = catastrophe_yields
        control_yields_by_state[state_name] = control_yields
        historical_yields_by_state[state_name] = historical_yields

    # plot yields for control and historical
    plot_yields_comparing_control_and_historical(
        control_yields_by_state, historical_yields_by_state, state_names
    )

    # plot yields for control and catastrophe
    plot_yields_comparing_control_and_catastrophe(
        control_yields_by_state, catastrophe_yields_by_state, state_names
    )
    # print(historical_yields)
    # print(control_yields)
    # print(catastrophe_yields)


# let's average over all the crop yields for each cultivar for control and choose the best planting date

# let's average all the crops for years 6 7 and 8, and then pick the best planting date

if __name__ == "__main__":
    main()
