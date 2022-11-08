"""

Determine the amount of food from non-relocated and fully relocated for US farmland.

Non-relocated: only uses existing farmland in areas that can grow wheat potatoes and 
canola.

Relocated: potatoes are ignored as they're not good. Wheat is grown where possible, and 
half canola, half wheat is assumed for mississippi or below (although all canola would 
technically give better overall yields)

Created on Fri Oct 14 17:24:34 2022

Modified on Thu Oct 20 14:10:26 2022

@author: InesJ
@author: MorganR

"""
import numpy as np

calories_per_years_US_fed = 1 / (2100 * 365 * 335e6)


states_w = sorted(
    [
        "Kansas",
        "Oklahoma",
        "Texas",
        "New Mexico",
        "Arizona",
        "California",
        "Arkansas",
        "Louisiana",
        "Mississippi",
        "Alabama",
        "Georgia",
        "Tennessee",
        "Florida",
        "South Carolina",
    ]
)  # below kansas

states_pc = sorted(
    [
        "Oklahoma",
        "Texas",
        "New Mexico",
        "Arizona",
        "Arkansas",
        "Louisiana",
        "Mississippi",
        "Alabama",
        "Georgia",
        "Florida",
        "South Carolina",
    ]
)  # below mississippi


hectares_harvested_w = (
    np.array([110, 0, 145, 80, 110, 7000, 70, 75, 2950, 100, 330, 2000]) * 1000 * 0.4047
)  # in 1000 acres to ha, https://downloads.usda.library.cornell.edu/usda-esmis/files/j098zb09z/0z70b374s/w9506686w/acrg0622.pdf

hectares_harvested_p = (
    np.array([20]) * 1000 * 0.4047
)  # https://downloads.usda.library.cornell.edu/usda-esmis/files/fx719m44h/gb19gf71k/37721m72q/pots0922.pdf

hectares_harvested_c = (
    np.array([10]) * 1000 * 0.4047
)  # https://downloads.usda.library.cornell.edu/usda-esmis/files/j098zb09z/0z70b374s/w9506686w/acrg0622.pdf


yield_w = hectares_harvested_w * 305.5

yield_p = hectares_harvested_p * 1198

yield_c = hectares_harvested_c * 1520.5


calories_per_kg_w = 3340

calories_per_kg_p = 670

calories_per_kg_c = 4940


calories_w = sum(yield_w * calories_per_kg_w)
calories_w_US = calories_w * calories_per_years_US_fed
calories_p = sum(yield_p * calories_per_kg_p)
calories_p_US = calories_p * calories_per_years_US_fed
calories_c = sum(yield_c * calories_per_kg_c)
calories_c_US = calories_c * calories_per_years_US_fed
print(
    "WITHOUT RELOCATION:",
    "\nwheat: ",
    round(calories_w_US, 3) * 100,
    "percent US population fed at minimum caloric needs",
    "\npotatoes: ",
    round(calories_p_US, 3) * 100,
    "percent US population fed at minimum caloric needs",
    "\ncanola: ",
    round(calories_c_US, 3) * 100,
    "percent US population fed at minimum caloric needs",
    "\ntotal:",
    round(
        calories_c_US + calories_w_US + calories_p_US,
        3,
    )
    * 100,
    "percent US population fed at minimum caloric needs",
)

# WITH RELOCATION

# relocation: plant only canola and wheat (most efficient) in all crop land
states_w = sorted(
    ["Kansas", "California", "Tennessee"]
)  # between kansas and mississippi
states_wc = sorted(
    [
        "Oklahoma",
        "Texas",
        "New Mexico",
        "Arizona",
        "Arkansas",
        "Louisiana",
        "Mississippi",
        "Alabama",
        "Georgia",
        "Florida",
        "South Carolina",
    ]
)  # below mississippi
hectares_planted_w_only = np.array([2274, 23914, 5194]) * 1000 * 0.4047
hectares_planted_wc = (
    np.array([2145, 630, 7024, 1062, 3328, 3152, 4240, 795, 9433, 1431, 22485])
    * 1000
    * 0.4047
)  # wc  means "wheat and canola planted crop area in thousands of hectares"
print("hectares_planted_nuclear winter")
print(sum(hectares_planted_wc) + sum(hectares_planted_w_only))

total_wheat_area = sum(hectares_planted_w_only) + 0.5 * sum(
    hectares_planted_wc
)  # half for wheat, half for canola

total_canola_area = 0.5 * sum(hectares_planted_wc)

# yield (kg/ha) years 2/3 after disaster
yield_w = total_wheat_area * 305.5
yield_c = total_canola_area * 1520.5

# get calories
calories_per_kg_w = 3340
calories_per_kg_c = 4940

calories_w = yield_w * calories_per_kg_w
calories_c = yield_c * calories_per_kg_c


print(
    "WITH RELOCATION:",
    "\nwheat: ",
    round(calories_w * calories_per_years_US_fed, 3) * 100,
    "percent US population fed at minimum caloric needs",
    "\ncanola: ",
    round(calories_c * calories_per_years_US_fed, 3) * 100,
    "percent US population fed at minimum caloric needs",
    "\ntotal:",
    round(
        calories_c * calories_per_years_US_fed + calories_w * calories_per_years_US_fed,
        3,
    )
    * 100,
    "percent US population fed at minimum caloric needs",
)
