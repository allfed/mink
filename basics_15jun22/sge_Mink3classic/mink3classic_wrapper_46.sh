#!/bin/bash

if [ $# -ne 1 ]; then

  echo "Usage: $0 run|reassemble"
  echo ""
  echo "run means run DSSAT"
  echo "reassemble means put the pieces back together"

  exit 1
fi

IFS="
"








if [ 0 = 1 ]; then

#magic_code=PALEOb_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # using the "potatoes" fertilizer scheme 1/2 at planting 1/2 on day 31
#magic_code=PALEObstress_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # using the "potatoes" fertilizer scheme 1/2 at planting 1/2 on day 31
#magic_code=PALEOc_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # using the "potatoes" fertilizer scheme 1/2 at planting 1/2 on day 31
#magic_code=PALEOd_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # using the "potatoes" fertilizer scheme 1/2 at planting 1/2 on day 31
magic_code=PALEOd3p1_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # using the "potatoes" fertilizer scheme 1/2 at planting 1/2 on day 31
echo "don't forget to use the paleo soil and to change it back when we're done..."
sleep 1s

# 269 ppm at 6000BP comes from
# http://www.ncdc.noaa.gov/paleo/metadata/noaa-icecore-2419.html
# ftp://ftp.ncdc.noaa.gov/pub/data/paleo/icecore/antarctica/taylor/taylor_co2-holocene.txt
old_start_readable_data_list=\
"
paleoB_1_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_2_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_3_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_4_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_5_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_6_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_7_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_8_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_9_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_10_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_11_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoB_12_cmips5_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat

paleoB_1_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_2_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_3_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_4_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_5_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_6_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_7_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_8_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_9_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_10_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_11_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoB_12_cmips5_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
"

#start_readable_data_list=`echo "$start_readable_data_list" | sed "s/cmips5/deltaONpik_base_2000/g ; s/\t269/\t379/g"`
#start_readable_data_list=`echo "$start_readable_data_list" | sed "s/RF/overviewRF/g" | grep whemmer`

#start_readable_data_list=`echo "$start_readable_data_list" | sed "s/RF/overviewRF/g"`

c_start_readable_data_list=\
"
paleoC_1_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_2_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_3_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_4_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_5_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_6_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_7_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_8_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_9_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_10_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_11_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat
paleoC_12_automh6k_p0_neolithiccrop__rainfed	whemmer0overviewRF.SNX	winterwheat	269	wheat

paleoC_1_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_2_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_3_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_4_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_5_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_6_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_7_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_8_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_9_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_10_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_11_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat
paleoC_12_automh6k_p0_neolithiccrop__rainfed	chlowerstuffoverviewRF.SNX	winterwheat	269	wheat

"

d_starter=\
"
paleoD_1_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_2_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_3_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_4_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_5_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_6_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_7_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_8_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_9_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_10_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_11_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat
paleoD_12_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	whemmer0RF.SNX	winterwheat	269	wheat

paleoD_1_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_2_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_3_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_4_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_5_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_6_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_7_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_8_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_9_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_10_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_11_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat
paleoD_12_bcc_csm1_mh6k_p0_neolithiccrop__rainfed	chlowerstuffRF.SNX	winterwheat	269	wheat

"



#start_readable_data_list=$d_starter
start_readable_data_list=\
"
$d_starter
`echo "$d_starter" | sed "s/bcc_csm1/ccsm4/g"`
`echo "$d_starter" | sed "s/bcc_csm1/cnrm_cm5/g"`
`echo "$d_starter" | sed "s/bcc_csm1/ensemble/g"`
`echo "$d_starter" | sed "s/bcc_csm1/giss_e2_r/g"`
`echo "$d_starter" | sed "s/bcc_csm1/ipsl_cm5a_lr/g"`
`echo "$d_starter" | sed "s/bcc_csm1/miroc_esm/g"`
`echo "$d_starter" | sed "s/bcc_csm1/mri_cgcm3/g"`
"

#`echo "$d_starter" | sed "s/bcc_csm1/mri_cgcm3/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/ensemble/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/giss_e2_r/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/ipsl_cm5a_lr/g"`


#`echo "$d_starter" | sed "s/bcc_csm1/cnrm_cm5/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/ensemble/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/giss_e2_r/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/ipsl_cm5a_lr/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/miroc_esm/g"`
#`echo "$d_starter" | sed "s/bcc_csm1/mri_cgcm3/g"`






echo "$start_readable_data_list"


fi # paleo














if [ 0 = 1 ]; then
# HTEST is a high res/high repetition test... like 100 repetitions, that takes way too long, so we're back
# down to like 5 repetitions to see what happens
# H15TEST is a moderate res/high repetition test. let's go with 50 because that is a lifetime of experience
# magic_code=HTEST_ chunks_per_case=160 plantingDateInMonthShiftInDays=0 # wheat fixes
 #magic_code=H15TEST_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=H15TEST_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=H15TEST_ chunks_per_case=40 plantingDateInMonthShiftInDays=0 # for re-dos...

# I15TEST will use the newer mink3.1 which keeps zero yields in the averages, counts how many occurred
# and tries to be a little smaller in the way numbers are written down to save space
 magic_code=I15TEST_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes

. SRDL_helper_htest_beans.sh
. SRDL_helper_itest_chickpeas.sh
. SRDL_helper_htest_groundnuts.sh
. SRDL_helper_htest_maize.sh
. SRDL_helper_htest_potatoes.sh
. SRDL_helper_htest_rice.sh
. SRDL_helper_htest_sorghum.sh
. SRDL_helper_htest_soybeans.sh
. SRDL_helper_htest_sugarcane.sh
. SRDL_helper_htest_wheat.sh

first_start_readable_data_list=\
"
`echo "$htest_beans_full"       | grep "bnB001"  | grep _p0_`
`echo "$htest_groundnuts_full"  | grep "grK001"  | grep _p0_`
`echo "$htest_maize_full"       | grep "mzK013"  | grep _p0_`
`echo "$htest_potatoes_full"    | grep "ptH001"  | grep _p0_`
`echo "$htest_rice_full"        | grep "riK001"  | grep _p0_`
`echo "$htest_sorghum_full"     | grep "sgK001"  | grep _p0_`
`echo "$htest_soybeans_full"    | grep "sbK001"  | grep _p0_`
`echo "$htest_sugarcane_full"   | grep "scK002canegro"  | grep _3_`
`echo "$htest_winterwheat_full" | grep "whK016"  | grep _p0_`
`echo "$htest_springwheat_full" | grep "whK001"  | grep _p0_`
"

# let's use 366 days for the optional harvest date for sugarcane...

base_start_readable_data_list="
$htest_beans_full
$itest_chickpeas_full
$htest_groundnuts_full
$htest_maize_full
$htest_potatoes_full
$htest_rice_full
$htest_sorghum_full
$htest_soybeans_full
$htest_sugarcane_full
$htest_winterwheat_full
$htest_springwheat_full

`echo "$htest_maize_full"       | sed "s/__/HIGHn__/g"`
`echo "$htest_potatoes_full"    | sed "s/__/HIGHn__/g"`
`echo "$htest_rice_full"        | sed "s/__/HIGHn__/g"`
`echo "$htest_sorghum_full"     | sed "s/__/HIGHn__/g"`
`echo "$htest_springwheat_full" | sed "s/__/HIGHn__/g"`
`echo "$htest_winterwheat_full" | sed "s/__/HIGHn__/g"`
"

h15_highN="
`echo "$htest_maize_full"       | sed "s/h15test/h15testhighN/g"`
`echo "$htest_potatoes_full"    | sed "s/h15test/h15testhighN/g"`
`echo "$htest_rice_full"        | sed "s/h15test/h15testhighN/g"`
`echo "$htest_sorghum_full"     | sed "s/h15test/h15testhighN/g"`
`echo "$htest_springwheat_full" | sed "s/h15test/h15testhighN/g"`
`echo "$htest_winterwheat_full" | sed "s/h15test/h15testhighN/g"`
"





# this one is currently running
old_start_readable_data_list=\
"
`echo "$base_start_readable_data_list" | sed "s/h15test/i15test/g ; s/base_2000/miroc_esm_chem__future_rcp8p5_2041_2070/g" | grep -v "^$" | sed -n "1,700p"` 
"

start_readable_data_list=\
"
`echo "$base_start_readable_data_list" | sed "s/h15test/i15test/g ; s/base_2000/noresm1_m__future_rcp8p5_2041_2070/g" | grep -v "^$" | sed -n "1,700p"` 
"








#. SRDL_helper_ricefertilizer_testor.sh

#magic_code=RICETHREESPLIT_    chunks_per_case=60 plantingDateInMonthShiftInDays=0
#start_readable_data_list=$fertilizer_mhthreesplit

#magic_code=RICEPOTATOES3_     chunks_per_case=60 plantingDateInMonthShiftInDays=0
#start_readable_data_list=$fertilizer_potatoes3

#magic_code=RICEALLATPLANTING_ chunks_per_case=60 plantingDateInMonthShiftInDays=0
#start_readable_data_list=$fertilizer_allAtPlanting

#magic_code=RICEZEROITOUT_ chunks_per_case=60 plantingDateInMonthShiftInDays=0
#start_readable_data_list=$fertilizer_zeroItOut






#magic_code=BEANMONTH_ chunks_per_case=10 plantingDateInMonthShiftInDays=0
# icta = central america = bnB006
# jaturong & perola = india = bnB028 & bnB095
bean_start_readable_data_list=\
"
beanmonth__1_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__2_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__3_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__4_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__5_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__6_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__7_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__8_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__9_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__10_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__11_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanmonth__12_deltaONpikNOV_base_2000_p0_N000__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
"

bean_monthly_start_readable_data_list=\
"
`echo "$bean_start_readable_data_list" | sed "s/bnB006/bnB028/g"`
`echo "$bean_start_readable_data_list" | sed "s/bnB006/bnB095/g"`
"

# this is for colombia and central america
# so....
# calima (bnB094), icta (bnB006)
#magic_code=BEANHIRES_ chunks_per_case=30 plantingDateInMonthShiftInDays=0
beanhires_start_readable_data_list=\
"
beanhires__gremlin_irrigated_deltaONpikNOV_base_2000_p0_beans__rainfed	bnB006RF.SNX	allAtPlanting	379	soybeans
beanhires__gremlin_irrigated_deltaONpikNOV_base_2000_p0_beans__rainfed	bnB094RF.SNX	allAtPlanting	379	soybeans
"








fi



if [ 0 = 1 ]; then

  . SRDL_helper_chickpea_test.sh

  magic_code=CHTESTalt_ chunks_per_case=32 plantingDateInMonthShiftInDays=0 # wheat fixes

#  start_readable_data_list=`echo "$chickpea_test_full" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g"`
#  start_readable_data_list=`echo "$chickpea_test_full" | sed "s/chA00/chautoC00/g" | grep 001`
#  start_readable_data_list=$chickpea_on_wheat_full

  start_readable_data_list=`echo "$chickpea_test_full" | grep 001RF`




#  magic_code=WHEATMULTIMODEL_ chunks_per_case=32 plantingDateInMonthShiftInDays=0 # wheat fixes
  magic_code=RACEWITHHIPERGATOR_ chunks_per_case=5 plantingDateInMonthShiftInDays=0 # wheat fixes
start_readable_data_list=\
"
gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK023RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK023RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK023RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK023RF.SNX	allAtPlanting	379	maize

gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK024RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK024RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK024RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK024RF.SNX	allAtPlanting	379	maize

gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK025RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK025RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK025RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK025RF.SNX	allAtPlanting	379	maize

gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK026RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK026RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK026RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK026RF.SNX	allAtPlanting	379	maize

gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK027RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK027RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK027RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK027RF.SNX	allAtPlanting	379	maize

gatordemo_2_deltaONpikNOV_base_2000_p0_maize060__rainfed	mzK028RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_base_2000_p0_maize120__rainfed	mzK028RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize060__rainfed	mzK028RF.SNX	allAtPlanting	379	maize
gatordemo_2_deltaONpikNOV_hadgem2_es__future_rcp8p5_2041_2070_p0_maize120__rainfed	mzK028RF.SNX	allAtPlanting	379	maize

"


  magic_code=MULTIWHEATb_ chunks_per_case=5 plantingDateInMonthShiftInDays=20 # wheat fixes
source SRDL_helper_multiwheat.sh

start_readable_data_list=\
"
`echo "$multi_full" | sed "s/multiwheatA/multiwheatB/g"`
"
#`echo "$multi_full" | grep hadgem | sed "s/multiwheatA/multiwheatB/g"`






  magic_code=DURIANa_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes
source SRDL_helper_durianA_rice.sh
source SRDL_helper_durianA_wheat.sh
source SRDL_helper_durianA_maize.sh
source SRDL_helper_durianA_sorghum.sh
source SRDL_helper_durianA_soybeans.sh

start_readable_data_list=\
"
$durian_rice_full
$durian_springwheat_full
$durian_winterwheat_full
$durian_maize_full
$durian_sorghum_full
$durian_soybeans_full
"

jjjstart_readable_data_list=\
"
$durian_soybeans_full
"

#$durian_sugarcane_full



  magic_code=SIMPLEaFORCElong_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # wheat fixes
start_readable_data_list=\
"
durianA_gremlin_winter_wheat_deltaONpikNOV_base_2000_p0_winterwheat__rainfed	simpleBdemo2RF.SNX	winterwheat	379	wheat

"
#durianA_gremlin_spring_wheat_deltaONpikNOV_base_2000_p0_springwheat__irrigated	simpleBdemo2RF.SNX	winterwheat	379	wheat





 magic_code=I15TESTSIMPLE_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=I15TESTSIMPLElowwater_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes
. SRDL_helper_htest_wheat.sh

base_start_readable_data_list=\
"
$htest_springwheat_full
$htest_winterwheat_full
"

start_readable_data_list=\
"
`echo "$base_start_readable_data_list" | sed "s/h15test/i15test/g ; s/whK/whOsimple/g ; s/	winterwheat	/	zeroItOut	/g" | grep -v "^$"` 
"

start_readable_data_list=\
"
`echo "$start_readable_data_list" | grep whOsimple076`
`echo "$start_readable_data_list" | grep whOsimple090`
`echo "$start_readable_data_list" | grep whOsimple091`
`echo "$start_readable_data_list" | grep whOsimple011`
"



. SRDL_helper_hybrid_testor.sh

 magic_code=HYBRIDCLIMATE1_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
start_readable_data_list=`echo "$testor_maize_full" | sed "s/climate0/climate1/g"`

start_readable_data_list=\
"
`echo "$testor_maize_full" | sed "s/climate0/climate1/g" | grep mzK017IR | grep _M_`
`echo "$testor_maize_full" | sed "s/climate0/climate1/g" | grep mzK024IR | grep _A_`
`echo "$testor_maize_full" | sed "s/climate0/climate1/g" | grep mzK026IR | grep _J_`
"



. SRDL_helper_simple_wheat.sh

 magic_code=SIMPLEGRIDa_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
#start_readable_data_list=$simple_wheat_SRDL
#start_readable_data_list=`echo "$carrot_test" | sed "s/carrot/crazy2/g"`
#start_readable_data_list=`echo "$carrot_test" | sed "s/RF/IR/g"`
start_readable_data_list=\
"
`echo "$carrot_test" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g            "`
`echo "$carrot_test" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g ; s/RF/IR/g"`
"


. SRDL_helper_aflatoxintest.sh

 magic_code=AFLATESTfirst_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # wheat fixes
#start_readable_data_list=$aflatoxins_groundnuts
start_readable_data_list=`echo "$aflatoxins_groundnuts" | sed "s/grAFLATOXINtestRF.SNX/grAFLATOXINtestothersoilRF.SNX/g"`


. SRDL_helper_droughtman.sh

# magic_code=DROUGHTMANwater01_ chunks_per_case=8 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25_ chunks_per_case=4 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25AFsoil_ chunks_per_case=4 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25heattol_ chunks_per_case=4 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25AFsoilheattol_ chunks_per_case=4 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater75_ chunks_per_case=8 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater99_ chunks_per_case=8 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25heattol04_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25heattol06_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25heattol08_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25heattol10photo05_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25FSLFW01_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
# magic_code=DROUGHTMANwater25FSLFW09_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes

# magic_code=PSEARCHa_ chunks_per_case=2 plantingDateInMonthShiftInDays=0 # wheat fixes

# when searching for the heat parameters necessary to return us to previous levels, i am focusing
# on the highest potential (for speed purposes) which seems to be high density and deep planting.
# the advantage seems quite small, so even if it isn't always the best, it will be pretty close
# most of the time (is my assumption).
#
# also, just march planting....
#aassdfstart_readable_data_list=$base_full_drought


start_readable_data_list=\
"
$param_start_list
"


#`echo "$base_full_drought" | grep _3_delta | grep _8p0_038_6_`
other_months="
`echo "$base_full_drought" | grep _6_delta`
`echo "$base_full_drought" | grep _4_delta`
`echo "$base_full_drought" | grep _5_delta`
"

#start_readable_data_list=`echo "$start_readable_data_list" | grep _038_`
#start_readable_data_list=`echo "$start_readable_data_list" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g"`

#start_readable_data_list=`echo "$base_full_drought" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g"`


jjjstart_readable_data_list=\
"
`echo "$start_readable_data_list" | grep "mzdrought_2p7_038_3_1p0" | grep droughtman_5_ | grep __n150`
`echo "$start_readable_data_list" | grep "mzdrought_8p0_150_3_1p5" | grep droughtman_3_ | grep __n100`
"








 magic_code=JUNKTESTc_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # wheat fixes


start_readable_data_list=\
"
paramsearch_3_deltaONpikNOV_base_2000_p0_allcrops__n000	RR0001.SNX	threeSplitWithFlowering	379	maize
"

fi


if [ 0 = "ethiopiacorn" ]; then
. SRDL_helper_ethiopiacorn.sh
 magic_code=ETHIOPIACORN_ chunks_per_case=10 plantingDateInMonthShiftInDays=0 # wheat fixes

#start_readable_data_list="$ethiopiacorn_full"

#$ethiopiasorghum_full
start_readable_data_list=\
"
$ethiopiasorghum_full
$ethiopiawheat_full
"

start_readable_data_list=\
"
$start_readable_data_list
`echo "$start_readable_data_list" | sed "s/base_2000/gfdl_esm2m__future_rcp8p5_2041_2070/g"`
`echo "$start_readable_data_list" | sed "s/base_2000/hadgem2_es__future_rcp8p5_2041_2070/g"`
`echo "$start_readable_data_list" | sed "s/base_2000/ipsl_cm5a_lr__future_rcp8p5_2041_2070/g"`
`echo "$start_readable_data_list" | sed "s/base_2000/miroc_esm_chem__future_rcp8p5_2041_2070/g"`
`echo "$start_readable_data_list" | sed "s/base_2000/noresm1_m__future_rcp8p5_2041_2070/g"`
"

start_readable_data_list=`echo "$start_readable_data_list" | grep wheat`

fi # ethiopia thing


if [ 0 = "more aflatoxins" ]; then

. SRDL_helper_aflatoxinA.sh

 magic_code=AFLATARGETa_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
#start_readable_data_list=$aflatoxin_groundnuts_A
start_readable_data_list=$aflatoxin_groundnuts_A_june_july

fi # aflatoxin again cutout


if [ 0 = 1 ]; then
  magic_code=DURIANa_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # wheat fixes
source SRDL_helper_durianA_rice.sh
source SRDL_helper_durianA_wheat.sh
source SRDL_helper_durianA_maize.sh
source SRDL_helper_durianA_sorghum.sh
source SRDL_helper_durianA_soybeans.sh

source SRDL_helper_durianA_forages.sh

start_readable_data_list=\
"
`echo "$durian_forage_full" | grep -v base_2000`
"
#`echo "$durian_forage_full" | grep -v base_2000 | grep bermuda001RF | grep durianA_gremlin_irrigated_deltaONpikNOV_ipsl_cm5a_lr__future_rcp8p5_2041_2070_p1_legume__rainfed`

others="
$durian_rice_full
$durian_springwheat_full
$durian_winterwheat_full
$durian_maize_full
$durian_sorghum_full
$durian_soybeans_full
"

fi # another cutout for durian


if [ 0 = 1 ]; then

. SRDL_helper_simple_wheat.sh
#magic_code=SIMPLEGRIDb_ chunks_per_case=20 plantingDateInMonthShiftInDays=0 # wheat fixes
#magic_code=SIMPLEGRID48_ chunks_per_case=40 plantingDateInMonthShiftInDays=0 # wheat fixes
magic_code=SIMPLEGRID48_ chunks_per_case=60 plantingDateInMonthShiftInDays=0 # for speed testing

start_readable_data_list=\
"
`echo "$bunch_of_veggies_48" | grep tomato | grep IR | sed "s/zztomato1IR.SNX/zztomato1highIR.SNX/g" | grep -v "^$" | head -n 2`
"




. SRDL_helper_cmip6.sh
echo "+++++"
#magic_code=CMIP6MEDRESTEST_ chunks_per_case=80 plantingDateInMonthShiftInDays=0 # for speed testing
#magic_code=CMIP6HIRES0_ chunks_per_case=16 plantingDateInMonthShiftInDays=0 # for speed testing
magic_code=CMIP6HIRESSEARCH_ chunks_per_case=16 plantingDateInMonthShiftInDays=0 # for speed testing

#start_readable_data_list=`echo "$cmip6_maize_full" | grep mzK013 | sed "s/mzK013RF/mzK013IR/g"`
#start_readable_data_list=$cmip6_full
#start_readable_data_list=`echo "$cmip6_full" | grep riK`
#start_readable_data_list=`echo "$cmip6_full" | sed "s/IR.SNX/RF.SNX/g" | grep MIROC`
#start_readable_data_list=$cmip6_alfalfa_full

#`echo "$egypt_trajectories" | grep base_2000`
#`echo "$egypt_trajectories" | grep -i ipsl`

egypt_start_readable_data_list="
`echo "$egypt_trajectories_RF" | grep "_12_" | grep whK | grep MIR`
`echo "$egypt_trajectories_RF" | grep "_4_" | grep -v whK | grep MIR`
`echo "$egypt_trajectories_RF" | grep "_5_" | grep -v whK | grep MIR`
"
#`echo "$egypt_trajectories" | grep "_12_" | grep whK`


. SRDL_helper_senegal.sh
magic_code=SENEGALA_ chunks_per_case=4 plantingDateInMonthShiftInDays=0 # for speed testing

start_readable_data_list=\
"
$senegal_maize
$senegal_rice
$senegal_sorghum
$senegal_groundnuts
"



. SRDL_helper_trillium_maize.sh
. SRDL_helper_trillium_wheat.sh
magic_code=TRILLIUM_ chunks_per_case=2 plantingDateInMonthShiftInDays=0 # for speed testing

#start_readable_data_list=$trillium_maize_full
start_readable_data_list=$trillium_wheat_full





. SRDL_helper_toothwort_maize.sh
magic_code=TOOTHWORT_ chunks_per_case=8 plantingDateInMonthShiftInDays=0 # for speed testing


start_readable_data_list=\
"
$toothwort_maize_full
`echo "$toothwort_maize_full" | sed "s/_p0_/_pn1_/g"`
`echo "$toothwort_maize_full" | sed "s/_p0_/_p1_/g"`
"

start_readable_data_list=`echo "$start_readable_data_list" | sed "s/toothwort/toothworthires/g"`


start_readable_data_list="
`echo "$start_readable_data_list" | grep gfdl`
"

fi # simple cutout


if [ "russia_cutout" = "do it" ]; then
. SRDL_helper_russia_wheat.sh
. SRDL_helper_russia_maize.sh
. SRDL_helper_russia_barley.sh
. SRDL_helper_russia_canola.sh

. SRDL_helper_russia_soybeans.sh
. SRDL_helper_russia_sugarbeets.sh
. SRDL_helper_russia_sunflowers.sh

. SRDL_helper_russia_potatoes.sh


magic_code=RUSSIAa_ chunks_per_case=5 plantingDateInMonthShiftInDays=0 # for speed testing
chunks_per_case=80


if [ 0 = 1 ]; then
start_readable_data_list=$russiaA_potatoes_full
else
start_readable_data_list="
$russiaA_barley_full
$russiaA_canola_full
$russiaA_canola_winter_full
$russiaA_maize_full
$russiaA_soybeans_full
$russiaA_sunflowers_full
$russiaA_sugarbeets_full
$russiaA_wheat_full
"

fi # cutout

start_readable_data_list=\
"
`echo "$start_readable_data_list" | grep 8p5  | sed "                    s/379/540/g"`
`echo "$start_readable_data_list" | grep ipsl | sed "s/rcp8p5/rcp4p5/g ; s/379/486/g"`
"


# 613 + 238 = 851
# 841 + 51 = 892
#start_readable_data_list=`echo "$start_readable_data_list" | grep -v "^$" | sort | uniq | tail -n +841`


start_readable_data_list=`echo "$start_readable_data_list" | grep "suA007RF" | grep "russiaA__gremlin_irrigated_deltaONpikNOV_miroc_esm_chem__future_rcp8p5_2041_2070_pn1_sunflowers__rainfed"`

fi # russiaA cutout


. SRDL_helper_ktest_maize.sh
. SRDL_helper_ktest_soybeans.sh
. SRDL_helper_ktest_groundnuts.sh
. SRDL_helper_ktest_rice.sh
. SRDL_helper_ktest_sorghum.sh
. SRDL_helper_ktest_wheat.sh
. SRDL_helper_ktest_potatoes.sh

. SRDL_helper_ktest_tomatoes.sh
. SRDL_helper_ktest_cabbage.sh
. SRDL_helper_ktest_greenbeans.sh
. SRDL_helper_ktest_barley.sh
. SRDL_helper_ktest_chickpeas.sh
. SRDL_helper_ktest_peppers.sh
. SRDL_helper_ktest_sunflowers.sh


magic_code=KTEST_ chunks_per_case=8 plantingDateInMonthShiftInDays=0
chunks_per_case=240 # for the redos...
#chunks_per_case=2 # for barley


base_start_readable_data_list=\
"
$ktest_rice_full
$ktest_wheat_full
$ktest_maize_full
$ktest_groundnuts_full
$ktest_soybeans_full
$ktest_sorghum_full
$ktest_tomatoes_full
$ktest_cabbage_full
$ktest_greenbeans_full
$ktest_barley_full
$ktest_chickpeas_full
$ktest_peppers_full
$ktest_sunflowers_full
"

base_potatoes_start_readable_data_list=$ktest_potatoes_full

gfdl_start_readable_data_list="
`echo "$ktest_rice_full"       | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_wheat_full"      | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_maize_full"      | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_groundnuts_full" | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_soybeans_full"   | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_sorghum_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_tomatoes_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_cabbage_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_greenbeans_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_barley_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_chickpeas_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_peppers_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
`echo "$ktest_sunflowers_full"    | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
"

gfdl_potatoes_start_readable_data_list=\
"
`echo "$ktest_potatoes_full"   | sed "s/pgf_1995_2015/gfdl_esm4_ssp585_future_2040_2060/g"`
"


ipsl_start_readable_data_list="
`echo "$ktest_rice_full"       | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_maize_full"      | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_wheat_full"      | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_groundnuts_full" | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_soybeans_full"   | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_sorghum_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_tomatoes_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_cabbage_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_greenbeans_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_barley_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_chickpeas_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_peppers_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
`echo "$ktest_sunflowers_full"    | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
"
ipsl_potatoes_start_readable_data_list=\
"
`echo "$ktest_potatoes_full"   | sed "s/pgf_1995_2015/ipsl_cm6a_lr_ssp585_future_2040_2060/g"`
"


mpi_start_readable_data_list="
`echo "$ktest_rice_full"       | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_maize_full"      | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_wheat_full"      | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_groundnuts_full" | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_soybeans_full"   | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_sorghum_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_tomatoes_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_cabbage_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_greenbeans_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_barley_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_chickpeas_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_peppers_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
`echo "$ktest_sunflowers_full"    | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
"

mpi_potatoes_start_readable_data_list="
`echo "$ktest_potatoes_full"   | sed "s/pgf_1995_2015/mpi_esm1_2_hr_ssp585_future_2040_2060/g"`
"

mri_start_readable_data_list="
`echo "$ktest_rice_full"       | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_maize_full"      | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_wheat_full"      | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_groundnuts_full" | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_soybeans_full"   | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_sorghum_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_tomatoes_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_cabbage_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_greenbeans_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_barley_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_chickpeas_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_peppers_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
`echo "$ktest_sunflowers_full"    | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
"

mri_potatoes_start_readable_data_list="
`echo "$ktest_potatoes_full"   | sed "s/pgf_1995_2015/mri_esm2_0_ssp585_future_2040_2060/g"`
"


ukesm_start_readable_data_list="
`echo "$ktest_rice_full"       | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_maize_full"      | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_wheat_full"      | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_groundnuts_full" | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_soybeans_full"   | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_sorghum_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_tomatoes_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_cabbage_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_greenbeans_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_barley_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_chickpeas_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_peppers_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
`echo "$ktest_sunflowers_full"    | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
"

ukesm_potatoes_start_readable_data_list="
`echo "$ktest_potatoes_full"   | sed "s/pgf_1995_2015/ukesm1_0_ssp585_future_2040_2060/g"`
"


tomato_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep tomatoes`
`echo "$gfdl_start_readable_data_list" | grep tomatoes`
`echo "$ipsl_start_readable_data_list" | grep tomatoes`
`echo "$mpi_start_readable_data_list" | grep tomatoes`
`echo "$mri_start_readable_data_list" | grep tomatoes`
`echo "$ukesm_start_readable_data_list" | grep tomatoes`
"

cabbage_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep cabbage`
`echo "$gfdl_start_readable_data_list" | grep cabbage`
`echo "$ipsl_start_readable_data_list" | grep cabbage`
`echo "$mpi_start_readable_data_list" | grep cabbage`
`echo "$mri_start_readable_data_list" | grep cabbage`
`echo "$ukesm_start_readable_data_list" | grep cabbage`
"

greenbeans_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep greenbeans`
`echo "$gfdl_start_readable_data_list" | grep greenbeans`
`echo "$ipsl_start_readable_data_list" | grep greenbeans`
`echo "$mpi_start_readable_data_list" | grep greenbeans`
`echo "$mri_start_readable_data_list" | grep greenbeans`
`echo "$ukesm_start_readable_data_list" | grep greenbeans`
"

barley_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep barley`
`echo "$gfdl_start_readable_data_list" | grep barley`
`echo "$ipsl_start_readable_data_list" | grep barley`
`echo "$mpi_start_readable_data_list" | grep barley`
`echo "$mri_start_readable_data_list" | grep barley`
`echo "$ukesm_start_readable_data_list" | grep barley`
"

chickpeas_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep chickpeas`
`echo "$gfdl_start_readable_data_list" | grep chickpeas`
`echo "$ipsl_start_readable_data_list" | grep chickpeas`
`echo "$mpi_start_readable_data_list" | grep chickpeas`
`echo "$mri_start_readable_data_list" | grep chickpeas`
`echo "$ukesm_start_readable_data_list" | grep chickpeas`
"

peppers_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep peppers`
`echo "$gfdl_start_readable_data_list" | grep peppers`
`echo "$ipsl_start_readable_data_list" | grep peppers`
`echo "$mpi_start_readable_data_list" | grep peppers`
`echo "$mri_start_readable_data_list" | grep peppers`
`echo "$ukesm_start_readable_data_list" | grep peppers`
"

sunflowers_start_readable_data_list="
`echo "$base_start_readable_data_list" | grep sunflowers`
`echo "$gfdl_start_readable_data_list" | grep sunflowers`
`echo "$ipsl_start_readable_data_list" | grep sunflowers`
`echo "$mpi_start_readable_data_list" | grep sunflowers`
`echo "$mri_start_readable_data_list" | grep sunflowers`
`echo "$ukesm_start_readable_data_list" | grep sunflowers`
"

#start_readable_data_list=$chickpeas_start_readable_data_list
start_readable_data_list="
$sunflowers_start_readable_data_list
"
#$peppers_start_readable_data_list

start_readable_data_list="
$start_readable_data_list
`echo "$start_readable_data_list" | sed "s/379/541/g" | grep -v 1995`
"





magic_code=NEARSUDAN_ chunks_per_case=80 plantingDateInMonthShiftInDays=0

. SRDL_helper_nearsudan_pearlmillet.sh
start_readable_data_list=$nearsudan_pearlmillet_full


#. extract_failed_cases_from_reassemble_log.sh
#start_readable_data_list=$failed_cases


#$next_start_readable_data_list
#`echo "$base_start_readable_data_list" | sed "s/h15test/i15test/g" | grep -v "^$" | sed -n "306,700p"` 

#. extract_failed_cases_from_reassemble_log.sh
#. find_list_of_timed_out_cases_13sep16.sh



echo "{{{$start_readable_data_list}}}"







####################################################################



. default_paths_etc.sh


# eliminate any repeats that snuck in there somehow...
echo "      we have `echo "$start_readable_data_list" | grep -v "^$" | wc -l` before unique-ing"
start_readable_data_list=`echo "$start_readable_data_list" | grep -v "^$" | sort | uniq`
echo "  and we have `echo "$start_readable_data_list" | grep -v "^$" | wc -l` after"



if [ 0 = 1 ]; then

for case_line in $start_readable_data_list; do
  iii=`echo "$case_line" | cut -f1`
  geog_file_size=`ls -l ${input_data_dir}${iii}_geog.txt | tr -s " " | cut -d" " -f5`
  sortable_list="$sortable_list
$case_line	$geog_file_size"
done

sorted_list=`echo "$sortable_list" | grep -v "^$" | sort -n -r -k6`
echo -e "\n\n!!! using reverse order for sorting !!! \n\n"
sleep 3
#sorted_list=`echo "$sortable_list" | grep -v "^$" | sort -n  -k6`


readable_data_list=$sorted_list



else
  echo "==== SKIPPING sorting ===="
  readable_data_list=$start_readable_data_list
fi

















magic_reassembly_log=REASSEMBLE_log.TXT 

# first, let's clean up any blank lines in the machine list

# now, source in the default settings....
source default_paths_etc.sh

  screen_dump=${logs_dir}/SCREEN_DUMP.TXT

if [ $1 = "run" ]; then
  date > $screen_dump
  echo "rdl = [$readable_data_list]" >> $screen_dump
  echo "chunks per case = $chunks_per_case" >> $screen_dump
else
  echo "rdl = [`echo "$readable_data_list" | grep -v "^$" | cat -n`]"
  echo "chunks per case = $chunks_per_case"
  echo "output log in $magic_reassembly_log"
  date > $magic_reassembly_log
fi


# get rid of the blank lines so that it can be sequentially numbered...
data_list=`echo "$readable_data_list" | grep -v "^$"`

n_to_try=`echo "$data_list" | wc -l`

echo "trying $n_to_try cases"


countercounter=1
success_failure_list=""
for (( case_num=1 ; case_num <= n_to_try ; case_num++ ))
do

  let "case_index = case_num - 1"

  data_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f1`
     X_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f2`
  crop_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f3`
   co2_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f4`
  irri_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f5`


  if [ $1 = "run" ]; then
    if [ $case_num -eq 1 ]; then
      echo "trying $n_to_try cases" >> $screen_dump
    fi

    ./mink3classic_tiled_parallelizer.sh $data_to_use $X_to_use $crop_to_use $co2_to_use "$irri_to_use" $chunks_per_case $plantingDateInMonthShiftInDays 1>>$screen_dump 2>&1

  else
    echo " -- trying to reassemble #$countercounter --"
    success_failure_list="${success_failure_list}
`./mink3classic_reassemble_outputs.sh ${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code $plantingDateInMonthShiftInDays | tee -a $magic_reassembly_log | grep REASSEMBL`"
    let "countercounter++"
  fi
done

echo "$success_failure_list" | grep -v "^$" | sort | cat -n





exit

