#!/bin/bash

# previous generation was NEW GISbox: build_NEWPIKFROMDAILY_for_DSSAT_30nov15.sh
#  nothing has really changed, i just wanted to clean up the old settings
#  and have something new for our new attempted baseline using the princeton stuff....

# previous generation was NEW GISbox:
#   /home/grass/grass_scripts/gf_crop_modeling_01mar11/build_dataset_for_DSSAT_BigInits_03nov11.sh
#   /home/grass/grass_scripts/gf_crop_modeling_01mar11/build_dataset_for_DSSAT_monthly_neighborhood_01mar11.sh
# previous generation was old GISbox:
#   /home/grass/grass_scripts/crop_modeling/ricky_DSSAT/improved_calendars_15dec09
#   export_MONTHLY_neighborhood_29mar10.sh


# the purpose of this is to create a single table for me
# to use for the crop modeling
# BUT: now we have added some more things to do the soil initial conditions a little more better-er
# BUT: furthermore, i am going to use this to build a pre-generated daily weather thing to test with
#            the to-be-developed daily weather Mink2 AND i'm going to change the way that masking gets
#            done to be a little more robust


# the output_file is the name to be used when writing out the dataset. 
# the name should have the form directory/prefix_ (ending in an underscore)
# this allows you to give a unique name to all the myriad little
# details that cannot be easily incorporated into the filename directly
#
# typically, this name will be paired primarily with a geographic region
# and resolution


#output_file=to_DSSAT/ilnarrowmonthly_ ; region_to_use="n=42:30N s=37:00N w=89:00W e=87:30W res=0:30 -a " # let's try the whole world...
#output_file=to_DSSAT/simplegrid48only ; region_to_use="n=50 s=25 w=-130 e=-60 res=0:15:00 -a " # let's try just the lower 48 of the USA
#output_file=to_DSSAT/searchworldB_ ; region_to_use="n=70 s=-57.75 w=-178.5 e=178.5 res=1.75 -a" # let's try the whole world...
#output_file=to_DSSAT/i15test ; region_to_use="n=70 s=-57 w=-179.25 e=180 res=0:15:00 -a " # let's try the whole world...

#output_file=to_DSSAT/j15test ; region_to_use="n=70 s=-57 w=-179.25 e=180 res=0:15:00 -a " # let's try the whole world under PRINCETON GLOBAL FORCING

#output_file=to_DSSAT/pgffirst ; region_to_use="n=70 s=-57 w=-179.25 e=180 res=1:15:00 -a " # let's try the whole world under PRINCETON GLOBAL FORCING

#output_file=to_DSSAT/pgfsecond ; region_to_use="n=70 s=-57 w=-178.00 e=178 res=1:45:00 -a " # let's try the whole world under PRINCETON GLOBAL FORCING

#output_file=to_DSSAT/eulabeia_ ; region_to_use="n=70N s=57:45S w=178:30W e=178:30E res=1:45 -a" # let's try the whole world...


#output_file=to_DSSAT/coldhatnub_ ; region_to_use="n=24 s=-25 w=-125 e=180 res=1:45 -a" # let's try the whole world...
#output_file=to_DSSAT/coldharp_ ; region_to_use="n=65 s=-55 w=-125 e=180 res=1:15 -a" # let's try the whole world...

#output_file=to_DSSAT/isimipharp_ ; region_to_use="n=65 s=-55 w=-125 e=180 res=1:15 -a" # let's try the whole world...
#output_file=to_DSSAT/isimipharpcoarser_ ; region_to_use="n=66.5 s=-56 w=-126 e=178.5 res=3:30 -a" # let's try the whole world...

#output_file=to_DSSAT/trillium_ ; region_to_use="n=65.25 s=-56.25 w=-126 e=180 res=2.25 -a" # let's try the whole world...
#output_file=to_DSSAT/toothwort_ ; region_to_use="n=65.25 s=-56.25 w=-126 e=180 res=2.25 -a" # let's try the whole world...
output_file=to_DSSAT/ktest_ ; region_to_use="n=70 s=-55 w=-180 e=180 res=0:30 -a" # let's try the whole world...


 climate_mapset=deltaONpgfXXI # which mapset contains all the climate data we are going to use
calendar_mapset=deltaONpgfXXI        # which mapset contains the target planting month rasters

# climate_mapset=princeton_forcings # which mapset contains all the climate data we are going to use
#calendar_mapset=princeton_forcings        # which mapset contains the target planting month rasters

# climate_mapset=isimip_ar6_averages # which mapset contains all the climate data we are going to use
#calendar_mapset=isimip_ar6_averages        # which mapset contains the target planting month rasters


#calendar_mapset=deltaPIKnov_from_daily_c # which mapset contains the target planting month rasters

# climate_mapset=deltaPIKnov_from_daily # which mapset contains all the climate data we are going to use
#calendar_mapset=deltaPIKnov_from_daily_c # which mapset contains the target planting month rasters

# climate_mapset=deltaPIK_from_daily # which mapset contains all the climate data we are going to use
#calendar_mapset=deltaPIK_from_daily_cal # which mapset contains the target planting month rasters

# climate_mapset=PIK_from_daily # which mapset contains all the climate data we are going to use
#calendar_mapset=PIK_from_daily_calendars # which mapset contains the target planting month rasters

# climate_mapset=PIK_climate # which mapset contains all the climate data we are going to use
#calendar_mapset=PIK_climate # which mapset contains the target planting month rasters

# climate_mapset=thornton_climate_12apr10 # which mapset contains all the climate data we are going to use
#calendar_mapset=thornton_crop_calendars2 # which mapset contains the target planting month rasters

# climate_mapset=DELTAonFC_14aug13 # which mapset contains all the climate data we are going to use
#calendar_mapset=DELTAonFC_calendars # which mapset contains the target planting month rasters
###calendar_mapset=ricky_DSSAT_pakistan # brute force pakistan indus basin calendars... which mapset contains the target planting month rasters

# climate_mapset=pray_climate_27jun14 # which mapset contains all the climate data we are going to use
#calendar_mapset=pray_climate_27jun14 # which mapset contains the target planting month rasters

soils_raster=soil_profile_number_int@DSSAT_essentials_12may11 # which raster contains the soil numbers
#soils_raster=soil_profile_number_HC3@DSSAT_essentials_12may11 # which raster contains the soil numbers

#soils_raster=deleteme_all_99999@clear_me_out_when_done # which raster contains the soil numbers


#### a thing for searching over all the generic soils...
##for (( soil_number=1 ; soil_number <= 27 ; soil_number++ )); do
#
##output_file=to_DSSAT/cornbeltsoil${soil_number}_ ; region_to_use="n=49.375 s=36.9583333333333 w=-97.2083333333333 e=-80.5416666666667 res=0.0833333333333333"
##output_file=to_DSSAT/cornbeltsoil${soil_number}_ ; region_to_use="n=49.5 s=36.75 w=-97.25 e=-80.5 res=0.25"
##
##echo -e "\n\n\n ------------ soil #$soil_number ---------------- \n\n\n\n"
##
##soils_raster=deleteme_soils
##g.region -d
##r.mapcalc deleteme_soils = "${soil_number}"

#elevation=base_2000_elev@$climate_mapset # which raster contains the elevation for each location
#elevation=base_2000_elev@thornton_climate_12apr10 # which raster contains the elevation for each location
elevation=basic_elevation@princeton_forcings # slight inconsistency (it does not matter), i filled in phil thornton's srtm based elevation with zeros
                                             # since the princeton gives us tempeartures over the ocean
                                             # i want the limiting step to be the soil data, i guess
                                             # g.region res=0:05
                                             # r.mapcalc basic_elevation = "if(isnull(base_2000_elev@thornton_climate_12apr10,0,base_2000_elev@thornton_climate_12apr10)"



# the gcm_list is the list of GCMs that we wish to build datasets for. the options
#    are base_2000 and then {cnr,csi,ech,mir}_{a1,a2,b1}_{2030,2050,2080}
#    put each one you want on a different line



# all we have to start with is the baseline climate (and a couple flavors thereof
gcm_list=\
"
deltaONpgfXXI_pgf_1995_2015

deltaONpgfXXI_gfdl_esm4_ssp585_future_2040_2060
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2040_2060
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2040_2060
deltaONpgfXXI_mri_esm2_0_ssp585_future_2040_2060
deltaONpgfXXI_ukesm1_0_ssp585_future_2040_2060
"

others="
deltaONpgfXXI_pgf_1995_2015

deltaONpgfXXI_gfdl_esm4_ssp585_future_2040_2060
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2040_2060
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2040_2060
deltaONpgfXXI_mri_esm2_0_ssp585_future_2040_2060
deltaONpgfXXI_ukesm1_0_ssp585_future_2040_2060
"




all_gcm_list=\
"
deltaONpgfXXI_pgf_1995_2015
deltaONpgfXXI_gfdl_esm4_ssp585_future_2020_2040
deltaONpgfXXI_gfdl_esm4_ssp585_future_2040_2060
deltaONpgfXXI_gfdl_esm4_ssp585_future_2060_2080
deltaONpgfXXI_gfdl_esm4_ssp585_future_2080_2100
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2020_2040
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2040_2060
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2060_2080
deltaONpgfXXI_ipsl_cm6a_lr_ssp585_future_2080_2100
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2020_2040
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2040_2060
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2060_2080
deltaONpgfXXI_mpi_esm1_2_hr_ssp585_future_2080_2100
deltaONpgfXXI_mri_esm2_0_ssp585_future_2020_2040
deltaONpgfXXI_mri_esm2_0_ssp585_future_2040_2060
deltaONpgfXXI_mri_esm2_0_ssp585_future_2060_2080
deltaONpgfXXI_mri_esm2_0_ssp585_future_2080_2100
deltaONpgfXXI_ukesm1_0_ssp585_future_2020_2040
deltaONpgfXXI_ukesm1_0_ssp585_future_2040_2060
deltaONpgfXXI_ukesm1_0_ssp585_future_2060_2080
deltaONpgfXXI_ukesm1_0_ssp585_future_2080_2100
"











# masking settings
# you can do some masking of the area you want to extract using these
# settings. in the next control list, you will specify a raster map.
# we have used the SPAM product (http://mapspam.info/) to helpfully identify
# locations likely to be important for a particular crop. then, we can choose
# a minimum value that we want that raster to have. the SPAM physical area
# maps have missing values, zeros, and values from 0.1 ha/pixel and up.
# thus, choosing something like 0.05 will get all the pixels with positive values
# (the criterion is raster >= threshold). or, if you want fewer locations, you
# might choose 50 for 50 ha/pixel.
#
# you can also include a "buffer" around those initial chosen location. this
# is specified as a number of pixels
#
# i have now changed the way the masking goes, so the radius is in terms of
# the resolution of the TARGET REGION (not the original masking raster)
minimum_physical_area=0.0001 # 0.05 # value in the masking raster to be considered relevant
                           # this is ha in a SPAM pixel needed to be considered relevant
#growing_radius=15 # 15 # 0.0 # number of pixels
#growing_radius=6 # 0.0 # number of pixels
#growing_radius=2 # 0.0 # number of pixels
growing_radius=0 # 0.0 # number of pixels


# make a simple raster to get everything...
echo "    ++ making an ALL raster ++"
g.region $region_to_use
r.mapcalc deleteme_all = "100000" 2>&1 | grep -v % # some big number



# this is the main control where we list the crop cases we wish to model
# the columns are tab delimited...
#
# masking raster: historically, we have used the SPAM physical areas to
#    restrict the pixels modeled to those which are likely to be intereseting
#    this is the raster that will be used as the inputs on which the masking
#    and buffering will be done. if you don't want any masking, put in a raster
#    which has values at all locations (e.g., r.mapcalc everywhere_mask = "1" and
#    then put everywhere_mask in this spot.)
#
# crop name: this gives you a chance to put a human readable crop name in so you can
#    remember what you were trying to do

# water source: rainfed or irrigated. this gives you a chance to name the way you intend
#    to do the water since often there are different input choices (e.g., fertilizer).
#
# nitrogen fertilizer raster: this is a raster that specifies the total fertilizer application
#    for each location. this can be a real raster, or a value (e.g., "300").
#
# planting month calendar prefix: this is the first part of the name of the map showing the
#    target planting month that we want to use. this is combined with the chosen GCM names
#    and should be found in the calendar_mapset. the available choices at the time of writing
#    are: unified_irrigated, unified_rainfed, unified_spring_wheat, and (possibly) unified_winter_wheat
#
# initial nitrogen in soil: this is a map or value to indicate the amount of nitrogen that is originally
#    in the soil before we start the simulation. the units are kg/ha elemental nitrogen
#
# root weight from prior activities: this is a map or value to indicate the amount of root mass in
#    the soil prior to this season. the units are kg/ha
#
# surface residue weight from prior activities: this is a map or value to indicate the amount of
#    crop residues left on the field prior to this season. the units are kg/ha
#

simple_initial="initial_soil_nitrogen_mass@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
wheat_simple_initial="2*initial_soil_nitrogen_mass@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
old_wheat_revised_initial="initial_soil_nitrogen_mass_07may13@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
wheat_revised_initial="initial_soil_nitrogen_mass_23may13@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"

maize_initial="initial_soil_nitrogen_mass_13mar14@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"

paleo_initial="40	1000	1000"



a_bunch_of_good_stuff=\
"
BEAN.A_physical_area@spam2005_v3	beans	rainfed	0	1	$wheat_revised_initial
BEAN.A_physical_area@spam2005_v3	beans	irrigated	0	1	$wheat_revised_initial

CHIC.A_physical_area@spam2005_v3	chickpeas	rainfed	0	1	$wheat_revised_initial
CHIC.A_physical_area@spam2005_v3	chickpeas	irrigated	0	1	$wheat_revised_initial

GROU.A_physical_area@spam2005_v3	groundnuts	rainfed	0	1	$wheat_revised_initial
GROU.A_physical_area@spam2005_v3	groundnuts	irrigated	0	1	$wheat_revised_initial

MAIZ.A_physical_area@spam2005_v3	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	1	$maize_initial
MAIZ.A_physical_area@spam2005_v3	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	1	$maize_initial

POTA.A_physical_area@spam2005_v3	potatoes	irrigated	N_for_potatoes_IR_03may16@ricky_DSSAT_pt_develop3	1	$wheat_revised_initial
POTA.A_physical_area@spam2005_v3	potatoes	rainfed	N_for_potatoes_RF_03may16@ricky_DSSAT_pt_develop3	1	$wheat_revised_initial

RICE.A_physical_area@spam2005_v3	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
RICE.A_physical_area@spam2005_v3	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

SORG.A_physical_area@spam2005_v3	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
SORG.A_physical_area@spam2005_v3	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

SOYB.A_physical_area@spam2005_v3	soybeans	rainfed	0	1	$wheat_revised_initial
SOYB.A_physical_area@spam2005_v3	soybeans	irrigated	0	1	$wheat_revised_initial

WHEA.A_physical_area@spam2005_v3	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
WHEA.A_physical_area@spam2005_v3	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

WHEA.A_physical_area@spam2005_v3	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
WHEA.A_physical_area@spam2005_v3	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

SUGC.A_physical_area@spam2005_v3	sugarcane	rainfed	0	1	$wheat_revised_initial
SUGC.A_physical_area@spam2005_v3	sugarcane	irrigated	0	1	$wheat_revised_initial

VEGE.A_physical_area@spam2005_v3	tomatoes	rainfed	100	1	$wheat_revised_initial
VEGE.A_physical_area@spam2005_v3	tomatoes	irrigated	150	1	$wheat_revised_initial

VEGE.A_physical_area@spam2005_v3	cabbage	rainfed	175	1	$wheat_revised_initial
VEGE.A_physical_area@spam2005_v3	cabbage	irrigated	200	1	$wheat_revised_initial

RAPE.A_physical_area@spam2005_v3	canola	rainfed	175	1	$wheat_revised_initial
RAPE.A_physical_area@spam2005_v3	canola	irrigated	200	1	$wheat_revised_initial

CASS.A_physical_area@spam2005_v3	cassava	rainfed	10	1	$wheat_revised_initial
CASS.A_physical_area@spam2005_v3	cassava	irrigated	10	1	$wheat_revised_initial


COTT.A_physical_area@spam2005_v3	cotton	rainfed	200	1	$wheat_revised_initial
COTT.A_physical_area@spam2005_v3	cotton	irrigated	200	1	$wheat_revised_initial

BARL.A_physical_area@spam2005_v3	barley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
BARL.A_physical_area@spam2005_v3	barley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

VEGE.A_physical_area@spam2005_v3	greenbeans	rainfed	50	1	$wheat_revised_initial
VEGE.A_physical_area@spam2005_v3	greenbeans	irrigated	50	1	$wheat_revised_initial

BARL.A_physical_area@spam2005_v3	barley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
BARL.A_physical_area@spam2005_v3	barley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

PMIL.A_physical_area@spam2005_v3	pearlmillet	rainfed	20	1	$wheat_revised_initial
PMIL.A_physical_area@spam2005_v3	pearlmillet	irrigated	20	1	$wheat_revised_initial

VEGE.A_physical_area@spam2005_v3	peppers	rainfed	100	1	$wheat_revised_initial
VEGE.A_physical_area@spam2005_v3	peppers	irrigated	100	1	$wheat_revised_initial

SUGB.A_physical_area@spam2005_v3	sugarbeets	rainfed	300	1	$wheat_revised_initial
SUGB.A_physical_area@spam2005_v3	sugarbeets	irrigated	300	1	$wheat_revised_initial

SUNF.A_physical_area@spam2005_v3	sunflowers	rainfed	180	1	$wheat_revised_initial
SUNF.A_physical_area@spam2005_v3	sunflowers	irrigated	180	1	$wheat_revised_initial
"


some_good_junk=\
"
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	1	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	1	$maize_initial

deleteme_all	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

deleteme_all	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

deleteme_all	potatoes	irrigated	N_for_potatoes_IR_03may16@ricky_DSSAT_pt_develop3	1	$wheat_revised_initial
deleteme_all	potatoes	rainfed	N_for_potatoes_RF_03may16@ricky_DSSAT_pt_develop3	1	$wheat_revised_initial

deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

deleteme_all	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial

deleteme_all	soybeans	rainfed	0	1	$wheat_revised_initial
deleteme_all	soybeans	irrigated	0	1	$wheat_revised_initial

deleteme_all	groundnuts	rainfed	0	1	$wheat_revised_initial
deleteme_all	groundnuts	irrigated	0	1	$wheat_revised_initial

"


# old, with only partial coverage
#deleteme_all	potatoes	irrigated	N_for_potatoes_IR_03may16@ricky_DSSAT_pt_develop3	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
#deleteme_all	potatoes	rainfed	N_for_potatoes_RF_03may16@ricky_DSSAT_pt_develop3	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

core_crops_everywhere_three_month_window_main_control_list=\
"
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial

deleteme_all	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial

deleteme_all	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial
deleteme_all	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial

deleteme_all	potatoes	irrigated	N_for_potatoes_IR_19apr22@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	rainfed	N_for_potatoes_RF_19apr22@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial

deleteme_all	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial
deleteme_all	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial

deleteme_all	soybeans	rainfed	0	gremlin_irrigated	$wheat_revised_initial
deleteme_all	soybeans	irrigated	0	gremlin_irrigated	$wheat_revised_initial

deleteme_all	groundnuts	rainfed	0	gremlin_irrigated	$wheat_revised_initial
deleteme_all	groundnuts	irrigated	0	gremlin_irrigated	$wheat_revised_initial

deleteme_all	tomatoes	rainfed	100	gremlin_irrigated	$wheat_revised_initial
deleteme_all	tomatoes	irrigated	150	gremlin_irrigated	$wheat_revised_initial

deleteme_all	cabbage	rainfed	175	gremlin_irrigated	$wheat_revised_initial
deleteme_all	cabbage	irrigated	200	gremlin_irrigated	$wheat_revised_initial

deleteme_all	greenbeans	rainfed	50	gremlin_irrigated	$wheat_revised_initial
deleteme_all	greenbeans	irrigated	50	gremlin_irrigated	$wheat_revised_initial

deleteme_all	springbarley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	springbarley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial

deleteme_all	winterbarley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial
deleteme_all	winterbarley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial

deleteme_all	chickpeas	rainfed	0	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	chickpeas	irrigated	0	gremlin_spring_wheat	$wheat_revised_initial

deleteme_all	peppers	rainfed	100	gremlin_irrigated	$wheat_revised_initial
deleteme_all	peppers	irrigated	100	gremlin_irrigated	$wheat_revised_initial

deleteme_all	sunflowers	rainfed	100	gremlin_irrigated	$wheat_revised_initial
deleteme_all	sunflowers	irrigated	100	gremlin_irrigated	$wheat_revised_initial
"

# sugarcane needs to be limited to a few particular timeframes, so we need to do the search thing....
sugarcane_main_control_list=\
"
deleteme_all	sugarcane	rainfed	0	1	$wheat_revised_initial
deleteme_all	sugarcane	irrigated	0	1	$wheat_revised_initial
"

# cassava: based on very little information, it appears that the irrigated map would work better than the spring_wheat map
# https://www.agrifarming.in/cassava-cultivation-information-guide recommends may/june for india (i know, tiny homogeneous place)
# https://www.fao.org/3/x5032e/x5032E01.htm says you want to plant at the beginning of the rainy season (or have rain year-round)

even_more_useful_junk=\
"
deleteme_all	springbarley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	springbarley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial

deleteme_all	winterbarley	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial
deleteme_all	winterbarley	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial

deleteme_all	beans	rainfed	0	gremlin_irrigated	$wheat_revised_initial
deleteme_all	beans	irrigated	0	gremlin_irrigated	$wheat_revised_initial

deleteme_all	chickpeas	rainfed	0	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	chickpeas	irrigated	0	gremlin_spring_wheat	$wheat_revised_initial

deleteme_all	cassava	rainfed	10	gremlin_irrigated	$wheat_revised_initial
deleteme_all	cassava	irrigated	10	gremlin_irrigated	$wheat_revised_initial

deleteme_all	tomatoes	rainfed	100	1	$wheat_revised_initial
deleteme_all	tomatoes	irrigated	150	1	$wheat_revised_initial

deleteme_all	cabbage	rainfed	175	1	$wheat_revised_initial
deleteme_all	cabbage	irrigated	200	1	$wheat_revised_initial

deleteme_all	canola	rainfed	175	1	$wheat_revised_initial
deleteme_all	canola	irrigated	200	1	$wheat_revised_initial

deleteme_all	cotton	rainfed	200	1	$wheat_revised_initial
deleteme_all	cotton	irrigated	200	1	$wheat_revised_initial

deleteme_all	greenbeans	rainfed	50	1	$wheat_revised_initial
deleteme_all	greenbeans	irrigated	50	1	$wheat_revised_initial

deleteme_all	pearlmillet	rainfed	20	1	$wheat_revised_initial
deleteme_all	pearlmillet	irrigated	20	1	$wheat_revised_initial

deleteme_all	peppers	rainfed	100	1	$wheat_revised_initial
deleteme_all	peppers	irrigated	100	1	$wheat_revised_initial

deleteme_all	sugarbeets	rainfed	300	1	$wheat_revised_initial
deleteme_all	sugarbeets	irrigated	300	1	$wheat_revised_initial

deleteme_all	sunflowers	rainfed	180	1	$wheat_revised_initial
deleteme_all	sunflowers	irrigated	180	1	$wheat_revised_initial


"


#main_control_list=$zero_offset_main_control_list
#main_control_list=$three_month_window_main_control_list

#main_control_list=$everywhere_zero_offset_main_control_list
#main_control_list=$everywhere_three_month_window_main_control_list

#main_control_list=`echo "$everywhere_three_month_window_main_control_list" | grep -v maize | grep -v springwheat | grep -v winterwheat`
#main_control_list=$core_crops_everywhere_three_month_window_main_control_list
main_control_list="
`echo "$core_crops_everywhere_three_month_window_main_control_list" | grep peppers`
`echo "$core_crops_everywhere_three_month_window_main_control_list" | grep sunflowers`
"







# this is a list of offsets from the target month that you would like to try.
# integers only please...
#  0 means the target month
# +1 means the month after the target month
# -1 means the month before the target month
single_month_shifter_list=\
"
0
"

month_shifter_list="
0
1
-1
"





#####################################################
# you should not have to change anything below here #
#####################################################

# reset the internal field separator; basically, items
# in a list are separated by newlines rather than
# generic whitespace
IFS="
"

echo -e "\n\n\n  CHECK on EVERYTHING!!! : changing to censored tav and alternate amp \n\n\n"
sleep 2

# make sure the output directory exists
output_dir=${output_file%/*}  # extract the directory part out the output file
echo "outputs being placed in = [$output_dir]" ; # display something to the screen
mkdir -p $output_dir   # create the directory, if necessary




# pick one of the gcm cases from the list
for gcm in $gcm_list; do

  # initialize the lists of climate variables that we are going to build up
  prec_list=""
  tmin_list=""
  tmax_list=""

  rainydays_list=""
  SW_list=""

  # go through each month and add in that month's climate
for (( month=1 ; month <= 12 ; month++ )); do

  prec_list="${prec_list},${gcm}_rain_${month}@$climate_mapset"
  tmin_list="${tmin_list},${gcm}_tn_${month}@$climate_mapset"
  tmax_list="${tmax_list},${gcm}_tx_${month}@$climate_mapset"

  rainydays_list="${rainydays_list},${gcm}_rdays_${month}@$climate_mapset"
  SW_list="${SW_list},${gcm}_srad_${month}@$climate_mapset"

done

  # clean off the initial commas
  prec_list="${prec_list:1}"
  tmin_list="${tmin_list:1}"
  tmax_list="${tmax_list:1}"

  rainydays_list="${rainydays_list:1}"
  SW_list="${SW_list:1}"



# put together the list of annual climate information that we need
#echo "!!! back to using old amplitude momentarily !!!"
#annual_list=${gcm}_sry@$climate_mapset,${gcm}_txy@$climate_mapset,${gcm}_tny@$climate_mapset,${gcm}_tt@$climate_mapset,${gcm}_amp_alt@$climate_mapset,${gcm}_rt@$climate_mapset
annual_list=${gcm}_sry@$climate_mapset,${gcm}_txy@$climate_mapset,${gcm}_tny@$climate_mapset,${gcm}_tt_censored@$climate_mapset,${gcm}_amp_alt_fixed@$climate_mapset,${gcm}_rt@$climate_mapset







# now pick one of the crop cases
for spam_line in $main_control_list
do
  # pull out the different pieces of the line and store them separately for easy reference
  spam_raster_to_use_for_mask=`echo "$spam_line" | cut -f1`
                    crop_name=`echo "$spam_line" | cut -f2`
                 water_source=`echo "$spam_line" | cut -f3`
                      N_level=`echo "$spam_line" | cut -f4`
              calendar_prefix=`echo "$spam_line" | cut -f5`
                    initial_N=`echo "$spam_line" | cut -f6`
          initial_root_weight=`echo "$spam_line" | cut -f7`
       initial_surface_weight=`echo "$spam_line" | cut -f8`
                    id_raster=`echo "$spam_line" | cut -f9`
                 skip_climate=`echo "$spam_line" | cut -f10`

  # define the appropriate mask
  echo "-- creating mask for $spam_raster_to_use_for_mask `date` --"
  g.remove MASK # clear out the GRASS magic mask name


  # find all the pixels that meet the criterion for being relevant
  # old way, just looking at pin-prick style
  #  g.region rast=$spam_raster_to_use_for_mask # set the region to match up with the desired raster
  # r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())"

  # we need to figure out which is coarser, the desired region or the masking source raster


  echo "    ++ masking ++"
  # first the mask raster
  g.region rast=$spam_raster_to_use_for_mask
  nsres_from_raster=`g.region -g | grep nsres | cut -d= -f2`

  # second the desired region
  eval g.region $region_to_use
  nsres_from_region=`g.region -g | grep nsres | cut -d= -f2`

  raster_res_is_coarser_than_region_res=`echo "if($nsres_from_raster >= $nsres_from_region) {1} else {0}" | bc`

#  if [ -n "$id_raster" ]; then
#    r.mapcalc deleteme_IDed_spam_raster = "if(isnull($id_raster),null(),$spam_raster_to_use_for_mask)"
#  else
#    r.mapcalc deleteme_IDed_spam_raster = "$spam_raster_to_use_for_mask"
#  fi

  # we should already be in the target region...
  if [ $raster_res_is_coarser_than_region_res = 1 ]; then
    # do it the old way using pin pricks
    echo "      __ masking pin-prick __"
    r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
#    r.mapcalc deleteme_initial_spam_ungrown = "if(deleteme_IDed_spam_raster >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
  else
    # do a statistical coarsening to make sure we catch everything
    echo "      __ masking coarsening __"
    r.resamp.stats input=$spam_raster_to_use_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
#    r.resamp.stats input=deleteme_IDed_spam_raster output=deleteme_coarse_mask_ungrown method=sum --o
    r.mapcalc deleteme_initial_spam_ungrown = "if(deleteme_coarse_mask_ungrown >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
  fi
  



  # also, choose the pixels around those deemed relevant; note that this stays in the original region
  #   so desired pixels on the edges will not have a buffer to the outside (this is usually not a big deal)
  r.grow input=deleteme_initial_spam_ungrown output=deleteme_crop_mask radius=$growing_radius --o
  # finally, make the final mask a clean 1's and nulls version of the previous step
  r.mapcalc MASK = "if(isnull(deleteme_crop_mask),null(),1)" 2>&1 | grep -v %



  echo "    ++ N/inits ++"
#  # define the region to use
#  eval g.region $region_to_use

  # make some fake rasters for nitrogen; this allows us to use either a raster or a supplied value.
  # you could even use some sort of formula like "original_N * 1.25"
  r.mapcalc deleteme_N_to_use = "$N_level" 2>&1 | grep -v %

  # make some fake rasters for initial conditions
  r.mapcalc deleteme_initial_N              = "$initial_N" 2>&1 | grep -v "%"
  r.mapcalc deleteme_initial_root_weight    = "$initial_root_weight" 2>&1 | grep -v "%"
  r.mapcalc deleteme_initial_surface_weight = "$initial_surface_weight" 2>&1 | grep -v "%"

#  # if there is already an ID raster, great, otherwise make one...
#  generated_id_raster=deleteme_id_raster
#  if [ $id_raster = "CREATE_PLEASE" ]; then
#    echo "    ++ automatic id raster ++"
#
#    # figure out the number of rows and columns
#    nrows=`g.region -g | grep rows | cut -d= -f2`
#    ncols=`g.region -g | grep cols | cut -d= -f2`
#
#    r.mapcalc $generated_id_raster = "(row() - 1.0) * $ncols + col()" 2>&1 | grep -v "%"
#    id_raster=$generated_id_raster
#  fi


# here is where we go through the shifts against the target planting month
for month_shifter in $month_shifter_list
do

  # let us allow for specific months in the master control list by saying anything
  # that is less than two characters long gets interpreted literally
  # this will more-or-less be an undocumented feature for the moment (17nov11)

  if [ ${#calendar_prefix} -le 2 ]; then
    # just hope that this is either a number or a pre-defined raster...
    original_planting_month_raster=$calendar_prefix
  else
    # proceed as normal

    # define the planting calendar
    original_planting_month_raster=${calendar_prefix}_${gcm}@${calendar_mapset}
  fi

  text_month_shifter=${month_shifter/-/n} # convert the negative sign to an n for use in the names...

  # define the name for this particular offset
  planting_month_raster=${calendar_prefix}_${gcm}_p${text_month_shifter}

  # compute the appropriate planting month by shifting the target month and then wrapping the months that
  # go outside of 1 to 12
  r.mapcalc deleteme_planting_month = "eval(cand_month = $original_planting_month_raster + ($month_shifter), \
                                            too_low_fixed  = if( cand_month    <=  0, 12 + cand_month,    cand_month    ), \
                                            too_high_fixed = if( too_low_fixed >= 13, too_low_fixed - 12, too_low_fixed ), \
                                            too_high_fixed \
                                           )"



  ####################################
  # new stuff split up for daily ... #
  ####################################

  # we need to figure out the appropriate footprint that has everything.
  # then, we need to split up the pure climate stuff into one table and the
  # non-climate stuff into another. but they need to match up.


    # build up the final list of maps to export
    full_export_list="$soils_raster,$elevation,deleteme_planting_month,deleteme_N_to_use,$SW_list,$tmax_list,$tmin_list,$prec_list,$rainydays_list,$annual_list"
    full_export_list="${full_export_list},deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$id_raster"

#  # time to use the new intersection module to find the common footprint
#  /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.intersection \
#      input=$full_export_list \
#     output=deleteme_intersection \
#      --o
#
#  # make the new, smaller lists...
#    climate_list="$elevation,$SW_list,$tmax_list,$tmin_list,$prec_list,$rainydays_list,$annual_list,$id_raster,deleteme_intersection"
#
#    nonclimate_list="$soils_raster,deleteme_planting_month,deleteme_N_to_use,deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$id_raster,deleteme_intersection"

  # do the exporting
  # define the name of the output file
    start_output_name=${output_file}_${planting_month_raster}_${crop_name}__${water_source}

#    real_output_file=${start_output_name}_nonCLIMATE
#    climate_name=${start_output_name}_CLIMATE

    # make a header file with all the names of the maps in order so we can figure out what they are later
#    echo "$climate_list"    | tr "," "\n" | cat -n > ${climate_name}.cols.txt
#    echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt
  # tack on the id_raster if available...
  if [ -n "$id_raster" ]; then
    full_export_list=$full_export_list,$id_raster
  fi
    echo "$full_export_list" | tr "," "\n" | cat -n > ${start_output_name}.cols.txt

    # dump a status report out to the screen
    echo " -- exporting $planting_month_raster $crop_name $water_source `date` --"

#  # skip the climate if requested... that is, the flag is empty
#  if [ -z "$skip_climate" ]; then
#     # Beware the MAGIC PATH!!! this is a non-standard grass program. so you need to know the actual path
#     # this is what actually makes the giant text tables
#     /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.out.new \
#       input=${climate_list} \
#       output=${climate_name} \
#       -l
#  fi
#
#     /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.out.new \
#       input=${nonclimate_list} \
#       output=${real_output_file} \

     /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12/dist.x86_64-unknown-linux-gnu/bin/r.out.new \
       input=${full_export_list} \
       output=${start_output_name} \
       -l



  # try some provenance from here...
  # the idea here is to record all the important settings so that you can reproduce this dataset
  # later and/or have the information when you have to do a write-up about your results
    echo "--- provenance started at `date` ---" > ${start_output_name}.provenance.txt
    echo "run from mapset: `g.gisenv get=MAPSET`" >> ${start_output_name}.provenance.txt
    echo "this dir: `pwd`" >> ${start_output_name}.provenance.txt
    echo "this script: $0" >> ${start_output_name}.provenance.txt
    echo "export list below" >> ${start_output_name}.provenance.txt
    echo "$full_export_list" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "`echo "$full_export_list" | sed "s/,/\n/g" | cat -n`" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "output_file = $output_file" >> ${start_output_name}.provenance.txt
    echo "region_to_use = $region_to_use" >> ${start_output_name}.provenance.txt
    echo "climate_mapset=$climate_mapset" >> ${start_output_name}.provenance.txt
    echo "calendar_mapset=$calendar_mapset" >> ${start_output_name}.provenance.txt
    echo "soils_raster=$soils_raster" >> ${start_output_name}.provenance.txt
    echo "elevation=$elevation" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "minimum_physical_area=$minimum_physical_area" >> ${start_output_name}.provenance.txt
    echo "growing_radius=$growing_radius" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "month_shifter_list=" >> ${start_output_name}.provenance.txt
    echo "$month_shifter_list" >> ${start_output_name}.provenance.txt
    echo "" >> ${start_output_name}.provenance.txt
    echo "main_control_list=" >> ${start_output_name}.provenance.txt
    echo "$main_control_list" >> ${start_output_name}.provenance.txt


done # month_shifter


done # spam_line

  g.remove MASK

done # gcm





## done # soil number search






############### old things for reference.... ##############
exit
