#!/bin/bash

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
# BUT: now this should work nicely with data we get from someone else...


# the output_file is the name to be used when writing out the dataset. 
# the name should have the form directory/prefix_ (ending in an underscore)
# this allows you to give a unique name to all the myriad little
# details that cannot be easily incorporated into the filename directly
#
# typically, this name will be paired primarily with a geographic region
# and resolution
#
# the weather_mask provides a way of telling where we actually have weather data for and not. it should be missing values
#   where we don't and have a valid value where we do

#output_file=to_DSSAT/ensoA_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...

#output_file=to_DSSAT/potatoesusingdaily_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/potatoesdailymonthly_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=1:30:00 " # let's try the whole world...
#output_file=to_DSSAT/ptsearch_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/potatoeszzz_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/watersecurityzero_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/watersecuritytestall_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/wsindia_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=40.5 s=2 w=63.5 e=102 res=0.5 " # let's try just india to figure out the irrigation and wheat problem...

#output_file=to_DSSAT/wstwo_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=90 s=-90 w=-180 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/wstwofix_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00 " # let's try the whole world...
#output_file=to_DSSAT/namz130_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=60 s=25 w=-127 e=-60 res=1:30:00 -a" # let's try the whole world...
#output_file=to_DSSAT/namz30_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=60 s=25 w=-127 e=-60 res=0:30:00 -a" # let's try the whole world...
#output_file=to_DSSAT/asiari30_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=50 s=-10 w=65 e=155 res=0:30:00 -a" # let's try the whole world...
#output_file=to_DSSAT/indiawh30_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=40 s=5 w=60 e=100 res=0:30:00 -a" # let's try the whole world...
#output_file=to_DSSAT/zambiavariabilityD_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-8.0 s=-18.5 w=21.5 e=34.0 res=0.5 " # maize with kindie's revisions, but now looking only at zambia for variability questions...
#output_file=to_DSSAT/zambiavariabilityE_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-8.0 s=-18.5 w=21.5 e=34.0 res=0.5 " # maize with kindie's revisions, but now looking only at zambia for variability questions...
#output_file=to_DSSAT/zambiavariabilityF_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-8.0 s=-18.5 w=21.5 e=34.0 res=0.5 " # maize with kindie's revisions, but now looking only at zambia for variability questions...

#output_file=to_DSSAT/malawiA ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-9 s=-17.5 w=32.5 e=36.5 res=0.5" # coarse malawi, but whatcha gonna do?
#output_file=to_DSSAT/worlddrought_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=62 s=-62 w=-130 e=180 res=1:30:00 -a" # coarse malawi, but whatcha gonna do?
#output_file=to_DSSAT/malawiB ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-9 s=-17.5 w=32.5 e=36.5 res=0.5" # coarse malawi, but whatcha gonna do?
#output_file=to_DSSAT/malawiD ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=-9 s=-17.5 w=32.5 e=36.5 res=0:05" # coarse malawi, but whatcha gonna do?
#output_file=to_DSSAT/potworldA ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00" # world
#output_file=to_DSSAT/wsthree_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00 " # let's try the whole world...

#output_file=to_DSSAT/potworldB ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00" # world
#output_file=to_DSSAT/potworldC ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00" # world
#output_file=to_DSSAT/guate ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=18 s=13 w=-95 e=-87 res=0:30:00" # world
#output_file=to_DSSAT/potworldD ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:15:00" # world

#output_file=to_DSSAT/phillipinesenso_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=20 s=-5 w=115 e=128 res=0:05:00" # world
#output_file=to_DSSAT/allenso_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:05:00" # world
#output_file=to_DSSAT/powerenso_ ; weather_mask=weather_mask_seasia_power_raw@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:05:00" # world
#output_file=to_DSSAT/powerensoA_ ; weather_mask=weather_mask_seasia_power_raw@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:05:00" # world

#output_file=to_DSSAT/dailytest ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=75 s=-65 w=-175 e=180 res=0:30:00" # world

#output_file=to_DSSAT/ensoB_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:30:00" # world
#output_file=to_DSSAT/powerensoB_ ; weather_mask=weather_mask_seasia_power_raw@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:30:00" # world

#output_file=to_DSSAT/ensoC_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=30 s=5 w=90 e=130 res=0:30:00" # world

#output_file=to_DSSAT/afladaily_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=23.6666666666667 s=9.25 w=-5.66666666666667 e=16.1666666666667 res=0.0833333333333334" # niger and burkina faso




#output_file=to_DSSAT/ar6firstlight_ ; weather_mask=mask_for_isimip_ar6@ricky_junk_0 ; region_to_use="n=70 s=-57 w=-179.25 e=180 res=0:30:00 -a"


#output_file=to_DSSAT/ar6illinois_ ; weather_mask=mask_for_isimip_ar6@ricky_junk_0 ; region_to_use="n=41.5 s=37.5 w=-91.5 e=-86.5 res=0:30:00 -a"


#output_file=to_DSSAT/ilnarrowdaily_ ; weather_mask=ENSO_Baseline_MASK@ricky_junk_0 ; region_to_use="n=42:30N s=37:00N w=89:00W e=87:30W res=0:30:00 -a"

#output_file=to_DSSAT/mainedaily_ ; weather_mask=mask_for_isimip_ar6@ricky_junk_0 ; region_to_use="n=50:00N s=45:00N w=72:30W e=65:00W res=0:30:00 -a"

#output_file=to_DSSAT/moreilnarrowdaily_ ; weather_mask=mask_for_isimip_ar6@ricky_junk_0 ; region_to_use="n=42:30N s=37:00N w=89:00W e=87:30W res=0:30:00 -a"



output_file=to_DSSAT/catdailyB_ ; weather_mask=first_weather_mask@ricky_DSSAT_cat_0 ; region_to_use="n=90 s=-90 w=-180 e=180 nsres=2.5 ewres=2.5" # now forcing a square resolution region_to_use="n=90 s=-90 w=-180 e=180 nsres=1.875 ewres=2.5"

#output_file=to_DSSAT/crazywheatA_ ; weather_mask=mask_for_isimip_ar6@ricky_junk_0 ; region_to_use="n=52N s=30S e=95W w=115W res=2.5 -a"


#### we should only need the calendar_mapset now with pre-existing daily weather...

# climate_mapset=deltaPIKnov_from_daily # which mapset contains all the climate data we are going to use
calendar_mapset=deltaPIKnov_from_daily_c # which mapset contains the target planting month rasters

# climate_mapset=PIK_climate # which mapset contains all the climate data we are going to use
#calendar_mapset=PIK_climate # which mapset contains the target planting month rasters

# climate_mapset=thornton_climate_12apr10 # which mapset contains all the climate data we are going to use
#calendar_mapset=thornton_crop_calendars2 # which mapset contains the target planting month rasters

# climate_mapset=DELTAonFC_14aug13 # which mapset contains all the climate data we are going to use
#calendar_mapset=DELTAonFC_calendars # which mapset contains the target planting month rasters
#calendar_mapset=deltaPIK_from_daily_cal # which mapset contains the target planting month rasters

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


#### elevation not needed at the moment because it should be in the daily weather files and
#### even then isn't necessary....
#elevation=base_2000_elev@$climate_mapset # which raster contains the elevation for each location
#elevation=base_2000_elev@thornton_climate_12apr10 # which raster contains the elevation for each location
#elevation=deleteme_fake_zero_elevation@ricky_junk_0 # which raster contains the elevation for each location

# the gcm_list is the list of GCMs that we wish to build datasets for. the options
#    are base_2000 and then {cnr,csi,ech,mir}_{a1,a2,b1}_{2030,2050,2080}
#    put each one you want on a different line


gcm_list=\
"
noGCMcalendar
"
#deltaONpikNOV_base_2000

DELTA_gcm_list=\
"
DELTAonFC_base_2000
DELTAonFC_ipsl_cm5a_lr_future_rcp8p5_2041_2070
DELTAonFC_gfdl_esm2m_future_rcp8p5_2041_2070
DELTAonFC_miroc_esm_chem_future_rcp8p5_2041_2070
DELTAonFC_ipsl_cm5a_lr_future_rcp8p5_2041_2070
DELTAonFC_gfdl_esm2m_future_rcp8p5_2041_2070
DELTAonFC_hadgem2_es_future_rcp8p5_2041_2070
DELTAonFC_miroc_esm_chem_future_rcp8p5_2041_2070
"

pik="
hadgem2_es_future_rcp8p5_2041_2070
ipsl_cm5a_lr_historical_rcp8p5_1981_2010
gfdl_esm2m_historical_rcp8p5_1981_2010
miroc_esm_chem_historical_rcp8p5_1981_2010
"
#hadgem2_es_historical_rcp8p5_1981_2010
#hadgem2_es_future_rcp8p5_2041_2070
#hadgem2_es_future_rcp8p5_2041_2070
#hadgem2_es_historical_rcp8p5_1981_2010

old_gcm_list="
base_2000
csi_a1_2050
mir_a1_2050
csi_b1_2050
mir_b1_2050
"

f="
ipsl_cm5a_lr_future_rcp8p5_2041_2070
ipsl_cm5a_lr_historical_rcp8p5_1981_2010
FUTURECLIM_hadgem2_es_future_rcp8p5_2041_2070
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
#minimum_physical_area=200.0 # 0.05 # value in the masking raster to be considered relevant
                           # this is ha in a SPAM pixel needed to be considered relevant
#growing_radius=15 # 15 # 0.0 # number of pixels
#growing_radius=6 # 0.0 # number of pixels
#growing_radius=5 # 0.0 # number of pixels
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
### i'm not going to do ID_numbers anymore since the weather files should have the lat/lon in the name
### and the whole point of this script is to *always* skip the weather generation step....
# ID_number map: this needs to have a unique value for each pixel; put in the word "CREATE_PLEASE" to have
#    it automatically generated
#
# [skip_climate]: if there is something here, the climate table will not be created. the idea is that then i can
#    make one giant set of daily weather for the region with a single line and then reuse those for multiple crop
#    cases...

simple_initial="initial_soil_nitrogen_mass@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
wheat_simple_initial="2*initial_soil_nitrogen_mass@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
old_wheat_revised_initial="initial_soil_nitrogen_mass_07may13@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
wheat_revised_initial="initial_soil_nitrogen_mass_23may13@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"
maize_initial="initial_soil_nitrogen_mass_13mar14@DSSAT_essentials_12may11	initial_root_mass@DSSAT_essentials_12may11	initial_surface_residue_mass@DSSAT_essentials_12may11"



if [ 0 = 1 ]; then # cutout

test_main_control_list=\
"
RICE.R_physical_area@spam2005_12aug13	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	CREATE_PLEASE
RICE.I_physical_area@spam2005_12aug13	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	CREATE_PLEASE
"

monthly_main_control_list=\
"
deleteme_all	potatoes	rainfed_low	30	1	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	2	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	3	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	4	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	5	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	6	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	7	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	8	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	9	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	10	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	11	$wheat_revised_initial
deleteme_all	potatoes	rainfed_low	30	12	$wheat_revised_initial

deleteme_all	potatoes	irrigated_low	30	1	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	2	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	3	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	4	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	5	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	6	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	7	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	8	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	9	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	10	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	11	$wheat_revised_initial
deleteme_all	potatoes	irrigated_low	30	12	$wheat_revised_initial

deleteme_all	potatoes	rainfed_high	125	1	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	2	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	3	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	4	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	5	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	6	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	7	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	8	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	9	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	10	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	11	$wheat_revised_initial
deleteme_all	potatoes	rainfed_high	125	12	$wheat_revised_initial

deleteme_all	potatoes	irrigated_high	125	1	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	2	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	3	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	4	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	5	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	6	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	7	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	8	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	9	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	10	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	11	$wheat_revised_initial
deleteme_all	potatoes	irrigated_high	125	12	$wheat_revised_initial
"

potatoes_main_control_list=\
"
deleteme_all	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	potato_season_onset_full_24jun14_grown	$wheat_revised_initial
"

all_main_control_list="
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial

deleteme_all	groundnuts	rainfed	0	growncoarse_dart_irrigated	$wheat_revised_initial
deleteme_all	groundnuts	irrigated	0	growncoarse_dart_irrigated	$wheat_revised_initial

deleteme_all	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial
deleteme_all	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial

deleteme_all	soybeans	rainfed	0	growncoarse_dart_irrigated	$wheat_revised_initial
deleteme_all	soybeans	irrigated	0	growncoarse_dart_irrigated	$wheat_revised_initial

deleteme_all	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

deleteme_all	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$maize_initial

deleteme_all	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_spring_wheat	$wheat_revised_initial
deleteme_all	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_spring_wheat	$wheat_revised_initial

deleteme_all	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_winter_wheat	$wheat_revised_initial
deleteme_all	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_winter_wheat	$wheat_revised_initial
"

wheat_main_control_list=\
"
deleteme_all	wheat75	rainfed	75	growncoarse_dart_spring_wheat	$wheat_revised_initial
deleteme_all	wheat75	irrigated	75	growncoarse_dart_spring_wheat	$wheat_revised_initial

deleteme_all	wheat150	rainfed	150	growncoarse_dart_spring_wheat	$wheat_revised_initial
deleteme_all	wheat150	irrigated	150	growncoarse_dart_spring_wheat	$wheat_revised_initial
"

namz=\
"
deleteme_all	rice200	irrigated	200	growncoarse_dart_irrigated	$maize_initial

deleteme_all	maizelow	rainfed	40	growncoarse_dart_irrigated	$maize_initial
deleteme_all	maizelow	irrigated	40	growncoarse_dart_irrigated	$maize_initial

deleteme_all	maizehigh	rainfed	200	growncoarse_dart_irrigated	$maize_initial
deleteme_all	maizehigh	irrigated	200	growncoarse_dart_irrigated	$maize_initial

deleteme_all	groundnuts	rainfed	0	growncoarse_dart_irrigated	$maize_initial
deleteme_all	groundnuts	irrigated	0	growncoarse_dart_irrigated	$maize_initial

"

good_all_main_control_list="
RICE.R_physical_area@spam2005_30may14	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial
RICE.I_physical_area@spam2005_30may14	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial

GROU.R_physical_area@spam2005_30may14	groundnuts	rainfed	0	growncoarse_dart_irrigated	$wheat_revised_initial
GROU.I_physical_area@spam2005_30may14	groundnuts	irrigated	0	growncoarse_dart_irrigated	$wheat_revised_initial

SORG.R_physical_area@spam2005_30may14	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial
SORG.I_physical_area@spam2005_30may14	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$wheat_revised_initial

SOYB.R_physical_area@spam2005_30may14	soybeans	rainfed	0	growncoarse_dart_irrigated	$wheat_revised_initial
SOYB.I_physical_area@spam2005_30may14	soybeans	irrigated	0	growncoarse_dart_irrigated	$wheat_revised_initial

POTA.R_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
POTA.I_physical_area@spam2005_30may14	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

MAIZ.R_physical_area@spam2005_30may14	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$maize_initial
MAIZ.I_physical_area@spam2005_30may14	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_irrigated	$maize_initial

WHEA.R_physical_area@spam2005_30may14	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_spring_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_30may14	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_spring_wheat	$wheat_revised_initial

WHEA.R_physical_area@spam2005_30may14	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	growncoarse_dart_winter_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_30may14	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	growncoarse_dart_winter_wheat	$wheat_revised_initial
"


somethingmain_control_list=\
"
RICE.R_physical_area@spam2005_12aug13	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial
RICE.I_physical_area@spam2005_12aug13	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial

GROU.R_physical_area@spam2005_12aug13	groundnuts	rainfed	0	dart_irrigated	$wheat_revised_initial
GROU.I_physical_area@spam2005_12aug13	groundnuts	irrigated	0	dart_irrigated	$wheat_revised_initial

SORG.R_physical_area@spam2005_12aug13	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial
SORG.I_physical_area@spam2005_12aug13	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial

SOYB.R_physical_area@spam2005_12aug13	soybeans	rainfed	0	dart_irrigated	$wheat_revised_initial
SOYB.I_physical_area@spam2005_12aug13	soybeans	irrigated	0	dart_irrigated	$wheat_revised_initial

POTA.R_physical_area@spam2005_12aug13	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	potato_season_onset_full_24jun14_grown	$wheat_revised_initial
POTA.I_physical_area@spam2005_12aug13	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	potato_season_onset_full_24jun14_grown	$wheat_revised_initial

MAIZ.R_physical_area@spam2005_12aug13	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$maize_initial
MAIZ.I_physical_area@spam2005_12aug13	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$maize_initial

WHEA.R_physical_area@spam2005_12aug13	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	dart_spring_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_12aug13	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	dart_spring_wheat	$wheat_revised_initial

WHEA.R_physical_area@spam2005_12aug13	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	dart_winter_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_12aug13	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	dart_winter_wheat	$wheat_revised_initial


"

#POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	potato_season_onset_07mar14_grown	$wheat_revised_initial
#POTA_physical_area@spam2005_30may14	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	potato_season_onset_07mar14_grown	$wheat_revised_initial
#POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	dart_potatoes	$wheat_revised_initial
#POTA_physical_area@spam2005_30may14	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	dart_potatoes	$wheat_revised_initial
#deleteme_all	maizeinitsn100	either	100	dart_irrigated	$maize_initial

#POTA.T_physical_area@spam2005_12aug13	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	dart_potatoes	$wheat_revised_initial	CREATE_PLEASE

all_main_control_list="


RICE.R_physical_area@spam2005_12aug13	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
RICE.I_physical_area@spam2005_12aug13	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

GROU.R_physical_area@spam2005_12aug13	groundnuts	rainfed	0	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
GROU.I_physical_area@spam2005_12aug13	groundnuts	irrigated	0	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

SORG.R_physical_area@spam2005_12aug13	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
SORG.I_physical_area@spam2005_12aug13	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

SOYB.R_physical_area@spam2005_12aug13	soybeans	rainfed	0	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
SOYB.I_physical_area@spam2005_12aug13	soybeans	irrigated	0	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

POTA.R_physical_area@spam2005_12aug13	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	dart_potatoes	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
POTA.I_physical_area@spam2005_12aug13	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	dart_potatoes	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

MAIZ.R_physical_area@spam2005_12aug13	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
MAIZ.I_physical_area@spam2005_12aug13	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	dart_irrigated	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

WHEA.R_physical_area@spam2005_12aug13	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	dart_spring_wheat	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
WHEA.I_physical_area@spam2005_12aug13	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	dart_spring_wheat	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

WHEA.R_physical_area@spam2005_12aug13	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	dart_winter_wheat	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE
WHEA.I_physical_area@spam2005_12aug13	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	dart_winter_wheat	$wheat_revised_initial	BMP_master_id_number_15minute@ricky_DSSAT_lac_cge	SKIP_CLIMATE

"

old_zambia=\
"
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	potatoes	rainfed	20	4	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	potatoes	rainfed	20	11	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	potatoes	rainfed	20	potato_season_onset_07mar14	$maize_initial
"

better_zambia="
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	beans	rainfed	0	dart_irrigated	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	cassava	rainfed	20	dart_irrigated	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	groundnuts	rainfed	0	dart_irrigated	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	millet	rainfed	20	dart_irrigated	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizehigh	rainfed	100	dart_irrigated	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizelow	rainfed	20	dart_irrigated	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	sorghum	rainfed	20	dart_irrigated	$maize_initial
"

month_zambia=\
"
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	beans	rainfed	0	10	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	cassava	rainfed	20	10	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	groundnuts	rainfed	0	10	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	millet	rainfed	20	10	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizehigh	rainfed	100	10	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizelow	rainfed	20	10	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	sorghum	rainfed	20	10	$maize_initial



deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	beans	rainfed	0	11	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	cassava	rainfed	20	11	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	groundnuts	rainfed	0	11	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	millet	rainfed	20	11	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizehigh	rainfed	100	11	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizelow	rainfed	20	11	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	sorghum	rainfed	20	11	$maize_initial



deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	beans	rainfed	0	12	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	cassava	rainfed	20	12	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	groundnuts	rainfed	0	12	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	millet	rainfed	20	12	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizehigh	rainfed	100	12	$maize_initial
deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	maizelow	rainfed	20	12	$maize_initial

deleteme_ZAMBIA_mask@ricky_DSSAT_zambia	sorghum	rainfed	20	12	$maize_initial
"

month_malawi=\
"
deleteme_all	maize	000	000	1	$maize_initial
deleteme_all	maize	000	000	2	$maize_initial
deleteme_all	maize	000	000	3	$maize_initial
deleteme_all	maize	000	000	4	$maize_initial
deleteme_all	maize	000	000	5	$maize_initial
deleteme_all	maize	000	000	6	$maize_initial
deleteme_all	maize	000	000	7	$maize_initial
deleteme_all	maize	000	000	8	$maize_initial
deleteme_all	maize	000	000	9	$maize_initial
deleteme_all	maize	000	000	10	$maize_initial
deleteme_all	maize	000	000	11	$maize_initial
deleteme_all	maize	000	000	12	$maize_initial
"

month_malawi_full=\
"
$month_malawi
`echo "$month_malawi" | sed "s/\t000/\t150/g"`
"

malawi_main_control_list=$month_malawi_full

# trying to see if "drought soils" make a difference
drought_main_control_list=\
"
deleteme_all	zerofert	rainfed	0	firebird_irrigated	$maize_initial
deleteme_all	hundfert	rainfed	100	firebird_irrigated	$maize_initial
"




# a new malawi attempt for the hopefully real set of stuff they need....

malawi_main_control_list=\
"
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n000	unused	000	4	$maize_initial
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n000	unused	000	11	$maize_initial

deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n050	unused	050	4	$maize_initial
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n050	unused	050	11	$maize_initial

deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n100	unused	100	4	$maize_initial
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n100	unused	100	11	$maize_initial

deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n150	unused	150	4	$maize_initial
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n150	unused	150	11	$maize_initial

deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n200	unused	200	4	$maize_initial
deleteme_malawi_mask_grown@ricky_DSSAT_malawi_2	n200	unused	200	11	$maize_initial
"




all_potatoes_main_control_list=\
"
deleteme_all	potatoes	rainfed	N_for_potatoes_RF_16dec14@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	irrigated	N_for_potatoes_RF_16dec14@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

deleteme_all	potatoes	N000	0	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	N000	0	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

deleteme_all	potatoes	N250	250	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_all	potatoes	N250	250	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
"


ftest_main_control_list=\
"
MAIZ_physical_area@spam2005_30may14	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial
MAIZ_physical_area@spam2005_30may14	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial

RICE_physical_area@spam2005_30may14	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial
RICE_physical_area@spam2005_30may14	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial

GROU_physical_area@spam2005_30may14	groundnuts	rainfed	0	gremlin_irrigated	$wheat_revised_initial
GROU_physical_area@spam2005_30may14	groundnuts	irrigated	0	gremlin_irrigated	$wheat_revised_initial

SORG_physical_area@spam2005_30may14	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial
SORG_physical_area@spam2005_30may14	sorghum	irrigated	N_for_sorghum_IR_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$wheat_revised_initial

SOYB_physical_area@spam2005_30may14	soybeans	rainfed	0	gremlin_irrigated	$wheat_revised_initial
SOYB_physical_area@spam2005_30may14	soybeans	irrigated	0	gremlin_irrigated	$wheat_revised_initial

POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	irrigated	N_for_potatoes_IR_12aug13@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

WHEA_physical_area@spam2005_30may14	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
WHEA_physical_area@spam2005_30may14	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial

WHEA_physical_area@spam2005_30may14	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial
WHEA_physical_area@spam2005_30may14	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial

"

some_potatoes_main_control_list=\
"
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	irrigated	N_for_potatoes_IR_08mar16@DSSAT_essentials_12may11	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial

POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	1	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	2	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	3	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	4	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	5	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	6	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	7	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	8	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	9	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	10	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	11	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	rainfed	N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11	12	$wheat_revised_initial
"

#main_control_list=$some_potatoes_main_control_list
othermain_control_list=\
"
`echo "$some_potatoes_main_control_list" | sed "s/N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11/300/g ; s/\trainfed\t/\tN300\t/g" | grep -v "_IR_08"`
`echo "$some_potatoes_main_control_list" | sed "s/N_for_potatoes_RF_08mar16@DSSAT_essentials_12may11/000/g ; s/\trainfed\t/\tN000\t/g" | grep -v "_IR_08"`
"


starter_list=\
"
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	1	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	2	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	3	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	4	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	5	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	6	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	7	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	8	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	9	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	10	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	11	$wheat_revised_initial
POTA_physical_area@spam2005_30may14	potatoes	Nzzz	zzz	12	$wheat_revised_initial
"

a_main_control_list=\
"
`echo "$starter_list" | sed "s/zzz/000/g"`
`echo "$starter_list" | sed "s/zzz/100/g"`
`echo "$starter_list" | sed "s/zzz/200/g"`
"


potatoes_main_control_list=\
"
deleteme_adjustedSPAM_POTA.I_physical_area@ricky_DSSAT_pt_develop3	potatoes	irrigated	N_for_potatoes_IR_03may16@ricky_DSSAT_pt_develop3	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
deleteme_adjustedSPAM_POTA.R_physical_area@ricky_DSSAT_pt_develop3	potatoes	rainfed	N_for_potatoes_RF_03may16@ricky_DSSAT_pt_develop3	growncoarse_potato_season_onset_full_24jun14_grown	$wheat_revised_initial
"


old_main_control_list=\
"
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	2	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	3	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	4	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	5	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	6	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	7	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	8	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	9	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	10	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	11	$wheat_revised_initial
deleteme_all	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	12	$wheat_revised_initial

deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	2	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	3	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	4	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	5	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	6	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	7	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	8	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	9	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	10	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	11	$wheat_revised_initial
deleteme_all	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	12	$wheat_revised_initial

deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	1	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	2	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	3	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	4	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	5	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	6	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	7	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	8	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	9	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	10	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	11	$maize_initial
deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	12	$maize_initial

deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	1	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	2	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	3	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	4	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	5	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	6	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	7	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	8	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	9	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	10	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	11	$maize_initial
deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	12	$maize_initial

deleteme_all	beans	rainfed	0	1	$maize_initial
deleteme_all	beans	rainfed	0	2	$maize_initial
deleteme_all	beans	rainfed	0	3	$maize_initial
deleteme_all	beans	rainfed	0	4	$maize_initial
deleteme_all	beans	rainfed	0	5	$maize_initial
deleteme_all	beans	rainfed	0	6	$maize_initial
deleteme_all	beans	rainfed	0	7	$maize_initial
deleteme_all	beans	rainfed	0	8	$maize_initial
deleteme_all	beans	rainfed	0	9	$maize_initial
deleteme_all	beans	rainfed	0	10	$maize_initial
deleteme_all	beans	rainfed	0	11	$maize_initial
deleteme_all	beans	rainfed	0	12	$maize_initial

deleteme_all	beans	irrigated	0	1	$maize_initial
deleteme_all	beans	irrigated	0	2	$maize_initial
deleteme_all	beans	irrigated	0	3	$maize_initial
deleteme_all	beans	irrigated	0	4	$maize_initial
deleteme_all	beans	irrigated	0	5	$maize_initial
deleteme_all	beans	irrigated	0	6	$maize_initial
deleteme_all	beans	irrigated	0	7	$maize_initial
deleteme_all	beans	irrigated	0	8	$maize_initial
deleteme_all	beans	irrigated	0	9	$maize_initial
deleteme_all	beans	irrigated	0	10	$maize_initial
deleteme_all	beans	irrigated	0	11	$maize_initial
deleteme_all	beans	irrigated	0	12	$maize_initial
"



wheat_main_control_list=\
"
WHEA.R_physical_area@spam2005_v3r1	springwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_v3r1	springwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial

WHEA.R_physical_area@spam2005_v3r1	winterwheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial
WHEA.I_physical_area@spam2005_v3r1	winterwheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_winter_wheat	$wheat_revised_initial

"

# make a mask to avoid doing so much in china
eval g.region $region_to_use
r.mapcalc deleteme_all = "1" 2>&1 | grep -v "%"
make_country_mask.sh Cambodia,Laos,Myanmar,Philippines,Vietnam deleteme_all deleteme_tight_mask
r.grow input=deleteme_tight_mask output=deleteme_SEASIA_mask radius=2 --o --q

rice_main_control_list=\
"
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	2	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	3	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	4	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	5	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	6	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	7	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	8	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	9	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	10	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	11	$wheat_revised_initial
deleteme_SEASIA_mask	rice	rainfed	N_for_rice_RF_12aug13@DSSAT_essentials_12may11	12	$wheat_revised_initial

deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	1	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	2	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	3	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	4	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	5	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	6	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	7	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	8	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	9	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	10	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	11	$wheat_revised_initial
deleteme_SEASIA_mask	rice	irrigated	N_for_rice_IR_12aug13@DSSAT_essentials_12may11	12	$wheat_revised_initial
"

maize_main_control_list=\
"

deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	1	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	2	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	3	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	4	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	5	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	6	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	7	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	8	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	9	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	10	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	11	$maize_initial
deleteme_SEASIA_mask	maize	rainfed	N_for_maize_RF_12aug13@DSSAT_essentials_12may11	12	$maize_initial

deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	1	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	2	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	3	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	4	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	5	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	6	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	7	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	8	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	9	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	10	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	11	$maize_initial
deleteme_SEASIA_mask	maize	irrigated	N_for_maize_IR_12aug13@DSSAT_essentials_12may11	12	$maize_initial
"

starter_thing=\
"
deleteme_SEASIA_mask	maize	nNNN	NNN	MONTH	$maize_initial
deleteme_SEASIA_mask	rice	nNNN	NNN	MONTH	$wheat_revised_initial
"

nitrogen_list=\
"
0
50
100
150
"

nitrogen_month_search=""
for nitrogen_amount in $nitrogen_list; do
for (( month=1 ; month <= 12 ; month++ )); do

  nitrogen_month_search="$nitrogen_month_search
`echo "$starter_thing" | sed "s/NNN/$nitrogen_amount/g ; s/MONTH/$month/g"`"

done # month
done # nitrogen_amount


#main_control_list=$nitrogen_month_search

# pulling some info from http://vegetableipmasia.org/uploads/files/20140923/files/Overview-on-tomato-production-and-tomato-varieties-in-Vietnam.pdf
# additional fertilizer is 130-200 kg urea; the base rate is 70 kg urea; also, there is a bunch of compost, apparently.
#
# so, how about we use the top end of that range and add a little more to make up for the compost and say 300 kg urea
#
# urea is like 46% N, so 300*0.46 = 138 kg, round it to 140kg N. i will probably just pick a random fertilizer scheme to allocate it...
tomatoes_main_control_list=\
"
deleteme_SEASIA_mask	tomatoes	rainfed	140	1	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	2	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	3	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	4	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	5	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	6	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	7	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	8	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	9	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	10	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	11	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	rainfed	140	12	$wheat_revised_initial

deleteme_SEASIA_mask	tomatoes	irrigated	140	1	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	2	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	3	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	4	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	5	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	6	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	7	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	8	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	9	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	10	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	11	$wheat_revised_initial
deleteme_SEASIA_mask	tomatoes	irrigated	140	12	$wheat_revised_initial
"


# and now i need to redo beans quickly....
beans_main_control_list=\
"
deleteme_SEASIA_mask	beans	rainfed	0	1	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	2	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	3	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	4	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	5	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	6	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	7	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	8	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	9	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	10	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	11	$wheat_revised_initial
deleteme_SEASIA_mask	beans	rainfed	0	12	$wheat_revised_initial

deleteme_SEASIA_mask	beans	irrigated	0	1	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	2	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	3	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	4	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	5	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	6	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	7	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	8	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	9	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	10	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	11	$wheat_revised_initial
deleteme_SEASIA_mask	beans	irrigated	0	12	$wheat_revised_initial
"




main_control_list=$beans_main_control_list



fi # end cutout?




if [ "AFLAtoxins" = "aflatoxins" ]; then

make_country_mask.sh "Niger,Burkina Faso,Nepal,Guatemala,Honduras" total.A_physical_area@spam2005_v3r1 deleteme_afla_target_countries
#make_country_mask.sh "Niger,Burkina Faso" total.A_physical_area@spam2005_v3r1 deleteme_afla_target_countries

r.mapcalc deleteme_afla_target_countries_near_groundnuts = "deleteme_afla_target_countries * if(GROU.R_physical_area@spam2005_v3r1 > 0, 1, null())"
r.mapcalc deleteme_afla_target_countries_near_maize = "deleteme_afla_target_countries * if(MAIZ.A_physical_area@spam2005_v3r1 > 0, 1, null())"

main_control_list=\
"
deleteme_afla_target_countries_near_maize	maize	N000	0	7	$maize_initial
deleteme_afla_target_countries_near_maize	maize	N000	0	6	$maize_initial

deleteme_afla_target_countries_near_maize	maize	N100	100	7	$maize_initial
deleteme_afla_target_countries_near_maize	maize	N100	100	6	$maize_initial

deleteme_afla_target_countries_near_maize	maize	N000	0	gremlin_irrigated	$maize_initial
deleteme_afla_target_countries_near_maize	maize	N000	0	gremlin_irrigated	$maize_initial

deleteme_afla_target_countries_near_maize	maize	N100	100	gremlin_irrigated	$maize_initial
deleteme_afla_target_countries_near_maize	maize	N100	100	gremlin_irrigated	$maize_initial
"

others="
deleteme_afla_target_countries_near_groundnuts	groundnuts	either	0	7	$wheat_revised_initial
deleteme_afla_target_countries_near_groundnuts	groundnuts	either	0	6	$wheat_revised_initial
"



sorghum_wheat_main_control_list="
deleteme_all	sorghum	rainfed	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial
deleteme_all	sorghum	irrigated	N_for_sorghum_RF_12aug13@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial

deleteme_all	wheat	rainfed	N_for_wheat_RF_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
deleteme_all	wheat	irrigated	N_for_wheat_IR_12aug13@DSSAT_essentials_12may11	gremlin_spring_wheat	$wheat_revised_initial
"
#deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial
#deleteme_all	maize	irrigated	N_for_maize_IR_13mar14@DSSAT_essentials_12may11	gremlin_irrigated	$maize_initial

fi

old_main_control_list="
deleteme_all	basic	N130	130	1	$maize_initial
deleteme_all	basic	N130	130	2	$maize_initial
deleteme_all	basic	N130	130	3	$maize_initial
deleteme_all	basic	N130	130	4	$maize_initial
deleteme_all	basic	N130	130	5	$maize_initial
deleteme_all	basic	N130	130	6	$maize_initial
deleteme_all	basic	N130	130	7	$maize_initial
deleteme_all	basic	N130	130	8	$maize_initial
deleteme_all	basic	N130	130	9	$maize_initial
deleteme_all	basic	N130	130	10	$maize_initial
deleteme_all	basic	N130	130	11	$maize_initial
deleteme_all	basic	N130	130	12	$maize_initial
"

#deleteme_all	maize	rainfed	N_for_maize_RF_13mar14@DSSAT_essentials_12may11	12	$maize_initial

maize_main_control_list="
deleteme_all	maize	eitherN000	0	1	$maize_initial
deleteme_all	maize	eitherN075	75	1	$maize_initial
deleteme_all	maize	eitherN150	150	1	$maize_initial
deleteme_all	maize	eitherN200	200	1	$maize_initial
deleteme_all	maize	eitherN250	250	1	$maize_initial
"

wheat_main_control_list=\
"
deleteme_all	wheat	eitherN150	150	1	$maize_initial
"

main_control_list=\
"
deleteme_all	maize	eitherN250	250	1	$maize_initial
"



# this is a list of offsets from the target month that you would like to try.
# integers only please...
#  0 means the target month
# +1 means the month after the target month
# -1 means the month before the target month
month_shifter_list=\
"
0
"

other_shifters="
0
1
-1
"


echo -e "\n\n\n CHECK THE SPAM RASTERS: should be the 30may14 ones... \n\n\n"
sleep 3



#####################################################
# you should not have to change anything below here #
#####################################################

# reset the internal field separator; basically, items
# in a list are separated by newlines rather than
# generic whitespace
IFS="
"

# make sure the output directory exists
output_dir=${output_file%/*}  # extract the directory part out the output file
echo "outputs being placed in = [$output_dir]" ; # display something to the screen
mkdir -p $output_dir   # create the directory, if necessary




# pick one of the gcm cases from the list
for gcm in $gcm_list; do


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

  # we should already be in the target region...
  if [ $raster_res_is_coarser_than_region_res = 1 ]; then
    # do it the old way using pin pricks
    echo "      __ masking pin-prick __"
    r.mapcalc deleteme_initial_spam_ungrown = "if($spam_raster_to_use_for_mask >= $minimum_physical_area, 1, null())" 2>&1 | grep -v %
  else
    # do a statistical coarsening to make sure we catch everything
    echo "      __ masking coarsening __"
    r.resamp.stats input=$spam_raster_to_use_for_mask output=deleteme_coarse_mask_ungrown method=sum --o
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


  # make the new, smaller lists...
    nonclimate_list="$soils_raster,deleteme_planting_month,deleteme_N_to_use,deleteme_initial_N,deleteme_initial_root_weight,deleteme_initial_surface_weight,$weather_mask"

  # do the exporting
  # define the name of the output file
    start_output_name=${output_file}_${planting_month_raster}_${crop_name}__${water_source}

    real_output_file=${start_output_name}_nonCLIMATE
    echo "real output"
    echo $real_output_file

    # make a header file with all the names of the maps in order so we can figure out what they are later
    echo "$nonclimate_list" | tr "," "\n" | cat -n > ${real_output_file}.cols.txt

    # dump a status report out to the screen
    echo " -- exporting $planting_month_raster $crop_name $water_source `date` --"

  # skip the climate if requested... that is, always, now....
     # Beware the MAGIC PATH!!! this is a non-standard grass program. so you need to know the actual path
     # this is what actually makes the giant text tables
     /usr/local/grass-6.5.svn/bin/r.out.new \
       input=${nonclimate_list} \
       output=${real_output_file} \
       -l


  # try some provenance from here...
  # the idea here is to record all the important settings so that you can reproduce this dataset
  # later and/or have the information when you have to do a write-up about your results
    echo "--- provenance started at `date` ---" > ${real_output_file}.provenance.txt
    echo "this dir: `pwd`" >> ${real_output_file}.provenance.txt
    echo "this script: $0" >> ${real_output_file}.provenance.txt
    echo "nonclimate_list below" >> ${real_output_file}.provenance.txt
    echo "$nonclimate_list" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "`echo "$nonclimate_list" | sed "s/,/\n/g" | cat -n`" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "output_file = $output_file" >> ${real_output_file}.provenance.txt
    echo "region_to_use = $region_to_use" >> ${real_output_file}.provenance.txt
    echo "weather_mask = $weather_mask" >> ${real_output_file}.provenance.txt
#    echo "climate_mapset=$climate_mapset" >> ${real_output_file}.provenance.txt
    echo "calendar_mapset=$calendar_mapset" >> ${real_output_file}.provenance.txt
    echo "soils_raster=$soils_raster" >> ${real_output_file}.provenance.txt
#    echo "elevation=$elevation" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "minimum_physical_area=$minimum_physical_area" >> ${real_output_file}.provenance.txt
    echo "growing_radius=$growing_radius" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "month_shifter_list=" >> ${real_output_file}.provenance.txt
    echo "$month_shifter_list" >> ${real_output_file}.provenance.txt
    echo "" >> ${real_output_file}.provenance.txt
    echo "main_control_list=" >> ${real_output_file}.provenance.txt
    echo "$main_control_list" >> ${real_output_file}.provenance.txt


done # month_shifter


done # spam_line

  g.remove MASK

done # gcm





## done # soil number search






############### old things for reference.... ##############
exit
