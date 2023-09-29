# -*- coding: utf-8 -*-
"""
Created on Mon Sep 19 21:43:17 2022

@author: InesJ
"""
import pandas as pd
import matplotlib.pyplot as plt

# import geopandas as gpd
import numpy as np
import cartopy.crs as ccrs
import glob

#%%
"""
MAKE 4 PLOTS: MINIMUM TEMPERATURE, MAXIMUM TEMPERATURE, PRECIPITATION, SOLAR
RADIATION. NORMAL WEATHER.
"""
# file location and corresponding states
loc = sorted(glob.glob("/home/dmrivers/Code/mink/wth_control/*/"))  # Control
states = [
    "Mississippi",
    "California",
    "Maryland",
    "Indiana",
    "Kansas",
    "Washington",
    "North Dakota",
]

real_yearly_avg_tmin = [13.3, 11.4, 9.0, 7.0, 6.4, 5.6, -0.7]
real_yearly_avg_tmax = [
    24.8,
    23.0,
    19.2,
    17.4,
    19.7,
    15.9,
    12.2,
]  # https://www.worlddata.info/america/usa/climate.php
real_yearly_avg_rain = (
    np.array([56.48, 22.97, 42.7, 41.86, 32.43, 38.67, 18.59]) * 25 / 365
)  # 1 inch=25mm. http://www.usa.com/rank/us--average-precipitation--states-rank.htm?hl=&hlst=&wist=&yr=&dis=&sb=DESC&plow=&phigh=&ps=

# dates are given as year+day up to 365, so they look like this:
# 1001,...,1365,...,4001,...,4366,...
days_in_month = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
days_in_month_leap = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
years = [1, 2, 3, 4, 5, 6, 7, 8, 9]
NYEARS = len(years)
month = np.linspace(1, NYEARS * 12, NYEARS * 12)
NMONTHS = len(month)

# # loop through each loctation
# print("loc")
# print(loc)
by_state_control_data = {}
for i in range(len(loc)):
    print("i")
    print(i)
    # loadtxt gets angry because the negative values (-273.1) don't leave space
    # for the delimiter. this statement therefore adds an extra space to please numpy
    with open(loc[i] + "RRRR.WTH") as f_input:
        text = [l.replace("-", " -") for l in f_input]
    print("loc[i]")
    print(loc[i])
    # get values of interest from file
    date = np.loadtxt(text, skiprows=3, usecols=0)
    srad = np.loadtxt(text, skiprows=3, usecols=1)  # solar radiation
    tmax = np.loadtxt(text, skiprows=3, usecols=2)
    tmin = np.loadtxt(text, skiprows=3, usecols=3)
    rain = np.loadtxt(text, skiprows=3, usecols=4)
    # remove -273.1 (absolute zero...) and change to nan
    tmax[tmax == -273.1] = np.nan
    tmin[tmin == -273.1] = np.nan

    # create empty lists to store the averages
    daily_tmean = []
    control_monthly_avg_tmin = []
    control_monthly_avg_tmax = []
    control_monthly_avg_srad = []
    control_monthly_avg_rain = []
    for d in range(len(date)):
        daily_tmean.append((tmin[d] + tmax[d]) / 2)  # avg daily temp
    # get monthly averages
    start = 0
    for j in range(NYEARS * 12):  # NYEARS years of data, 12 months
        idx = j % 12  # index, ie the month of the year
        year = j // 12 + 1

        if year % 4 == 0:  # leap year
            day = days_in_month_leap[idx]
        else:
            day = days_in_month[idx]

        control_monthly_avg_tmin.append(min(daily_tmean[start : day + start]))
        control_monthly_avg_tmax.append(max(daily_tmean[start : day + start]))
        control_monthly_avg_srad.append(np.mean(srad[start : day + start]))
        control_monthly_avg_rain.append(np.mean(rain[start : day + start]))
        start += day

    by_state_control_data[states[i]] = {}
    by_state_control_data[states[i]][
        "control_monthly_avg_tmin"
    ] = control_monthly_avg_tmin

    by_state_control_data[states[i]][
        "control_monthly_avg_tmax"
    ] = control_monthly_avg_tmax

    by_state_control_data[states[i]][
        "control_monthly_avg_srad"
    ] = control_monthly_avg_srad

    by_state_control_data[states[i]][
        "control_monthly_avg_rain"
    ] = control_monthly_avg_rain

# %%
"""
MAKE 4 PLOTS: MINIMUM TEMPERATURE, MAXIMUM TEMPERATURE, PRECIPITATION, SOLAR 
RADIATION. NUCLEAR WINTER WEATHER.
"""
loc = sorted(glob.glob("/home/dmrivers/Code/mink/wth_catastrophe/*/"))  # Catastrophe
states = [
    "Mississippi",
    "California",
    "Maryland",
    "Indiana",
    "Kansas",
    "Washington",
    "North Dakota",
]

# loop through each location
for i in range(len(loc)):

    # loadtxt gets angry because the negative values (-273.1) don't leave space
    # for the delimiter. this statement therefore adds an extra space to please numpy
    with open(loc[i] + "RRRR.WTH") as f_input:
        text = [l.replace("-", " -") for l in f_input]

    # get values of interest from file
    date = np.loadtxt(text, skiprows=3, usecols=0)
    srad = np.loadtxt(text, skiprows=3, usecols=1)  # solar radiation
    tmax = np.loadtxt(text, skiprows=3, usecols=2)
    tmin = np.loadtxt(text, skiprows=3, usecols=3)
    rain = np.loadtxt(text, skiprows=3, usecols=4)

    # remove -273.1 (absolute zero...) and change to nan
    tmax[tmax == -273.1] = np.nan
    tmin[tmin == -273.1] = np.nan

    # create empty lists to store the averages
    daily_tmean = []
    monthly_avg_tmin = []
    monthly_avg_tmax = []
    monthly_avg_srad = []
    monthly_avg_rain = []
    yearly_avg_tmin = []
    yearly_avg_tmax = []
    yearly_avg_srad = []
    yearly_avg_rain = []

    for d in range(len(date)):
        daily_tmean.append((tmin[d] + tmax[d]) / 2)  # avg daily temp

    # get monthly averages
    start = 0
    for j in range(NYEARS * 12):  # 2 years of data, 12 months
        idx = j % 12  # index, ie the month of the year
        year = j // 12 + 1
        day = days_in_month[idx]

        monthly_avg_tmin.append(min(daily_tmean[start : day + start]))
        monthly_avg_tmax.append(max(daily_tmean[start : day + start]))
        monthly_avg_srad.append(np.mean(srad[start : day + start]))
        monthly_avg_rain.append(np.mean(rain[start : day + start]))
        start += day

        if idx == 11:  # reach end of year
            yearly_avg_tmin.append(np.mean(monthly_avg_tmin[j - 11 : j + 1]))
            yearly_avg_tmax.append(np.mean(monthly_avg_tmax[j - 11 : j + 1]))
            yearly_avg_srad.append(np.mean(monthly_avg_srad[j - 11 : j + 1]))
            yearly_avg_rain.append(np.mean(monthly_avg_rain[j - 11 : j + 1]))

    control_monthly_avg_tmin = by_state_control_data[states[i]][
        "control_monthly_avg_tmin"
    ]
    control_monthly_avg_tmax = by_state_control_data[states[i]][
        "control_monthly_avg_tmax"
    ]
    control_monthly_avg_srad = by_state_control_data[states[i]][
        "control_monthly_avg_srad"
    ]
    control_monthly_avg_rain = by_state_control_data[states[i]][
        "control_monthly_avg_rain"
    ]

    ####
    # PLOTS
    ####

    fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, sharex="col")
    fig.suptitle(states[i])

    # tmin
    ax1.plot(month, monthly_avg_tmin)
    ax1.plot(month, control_monthly_avg_tmin, color="grey")
    # ax1.fill_between(years,np.array(yearly_avg_tmin[1:-1])+np.array(yearly_stdev_tmin[1:-1]),
    #                  np.array(yearly_avg_tmin[1:-1])-np.array(yearly_stdev_tmin[1:-1]),
    #                  color='lightblue')
    ax1.plot(month, np.full(NMONTHS, real_yearly_avg_tmin[i]), color="black")
    ax1.set_title("Average minimum temperature", fontsize=10)
    ax1.set_ylabel("Temperature (ºC)")
    # ax1.legend()

    # tmax
    ax2.plot(month, monthly_avg_tmax, color="red")  # ,label='Simulation'
    ax2.plot(month, control_monthly_avg_tmax, color="grey")
    # ax2.fill_between(years,np.array(yearly_avg_tmax[1:-1])+np.array(yearly_stdev_tmax[1:-1]),
    #                  np.array(yearly_avg_tmax[1:-1])-np.array(yearly_stdev_tmax[1:-1]),
    #                  color='lightcoral')
    ax2.plot(
        month, np.full(NMONTHS, real_yearly_avg_tmax[i]), color="black"
    )  # ,label='Observed'
    ax2.set_title("Average maximum temperature", fontsize=10)
    ax2.set_ylabel("Temperature (ºC)")
    # ax2.legend()

    # srad
    ax3.plot(month, monthly_avg_srad, color="darkorange")
    ax3.plot(month, control_monthly_avg_srad, color="grey")
    # ax3.fill_between(years,np.array(yearly_avg_srad[1:-1])+np.array(yearly_stdev_srad[1:-1]),
    #                  np.array(yearly_avg_srad[1:-1])-np.array(yearly_stdev_srad[1:-1]),
    #                  color='yellow')
    # ax3.plot(years,np.full(13,real_yearly_avg_srad[i]),label='Observed')
    ax3.set_title("Average solar radiation", fontsize=10)
    ax3.set_ylabel(r"Solar radiation ($MJm^{-2}d^{-1}$)")
    ax3.set_xlabel("Month")
    # ax3.legend()

    # rain
    ax4.plot(month, np.array(monthly_avg_rain) * 30, color="darkblue")
    ax4.plot(month, np.array(control_monthly_avg_rain) * 30, color="grey")
    # ax4.fill_between(years,np.array(yearly_avg_rain[1:-1])+np.array(yearly_stdev_rain[1:-1]),
    #                  np.array(yearly_avg_rain[1:-1])-np.array(yearly_stdev_rain[1:-1]),
    #                  color='lightslategrey')
    ax4.plot(month, np.full(NMONTHS, real_yearly_avg_rain[i] * 30), color="black")
    ax4.set_title("Average rain (mm/month)", fontsize=10)
    ax4.set_ylabel("Precipitation")
    # ax4.legend()
    plt.savefig("cata_weather_" + states[i])
    plt.show()
#%%
"""
YIELDS. NORMAL WEATHER.
"""
