# -*- coding: utf-8 -*-
"""
Created on Sun Oct 9 13:31:00 2022

@author: Morgan Rivers
"""
import pandas as pd
import matplotlib.pyplot as plt

# import geopandas as gpd
import numpy as np

# import cartopy.crs as ccrs
import glob

#%%

loc = glob.glob(".")  # Control
with open("Simulation_All_Crop_Scenario_State_Planting_cleaned.csv") as f_input:
    rows = [row for row in f_input]
    date = np.loadtxt(rows, skiprows=3, usecols=0)
    print(rows)
    print(date)
