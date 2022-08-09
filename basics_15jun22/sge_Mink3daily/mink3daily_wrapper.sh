#!/bin/bash

IFS="
"


if [ $# -ne 1 ]; then

  echo "Usage: $0 run|reassemble"
  echo ""
  echo "run means run DSSAT"
  echo "reassemble means put the pieces back together"

  exit 1
fi





if [ 0 = 1 ]; then
. GF_groundnuts.sh
. GF_sorghum.sh
. GF_soybeans.sh
. GF_maize.sh
. GF_rice.sh
. GF_wheat.sh
. GF_potatoes.sh

. GF_groundnuts_monthly.sh
. GF_sorghum_monthly.sh

fi # cutout



if [ 0 = 1 ]; then

#magic_code=ZAMBIADAILY_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
old_zambia_starter=\
"
zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_groundnuts__rainfed_nonCLIMATE	DDDD	grK001RF.SNX	groundnuts	369	groundnuts

zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_beans__rainfed_nonCLIMATE	DDDD	bnB002RF.SNX	groundnuts	369	groundnuts
zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_cassava__rainfed_nonCLIMATE	DDDD	csC004RF.SNX	potatoes	369	potatoes

zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_maizelow__rainfed_nonCLIMATE	DDDD	mzK017RF.SNX	potatoes	369	maize
zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_maizehigh__rainfed_nonCLIMATE	DDDD	mzK017RF.SNX	potatoes	369	maize

zambiavariabilityD__11_DELTAonFC_base_2000_p0_potatoes__rainfed_nonCLIMATE	DDDD	ptJ006RF.SNX	potatoes	369	potatoes
zambiavariabilityD__4_DELTAonFC_base_2000_p0_potatoes__rainfed_nonCLIMATE	DDDD	ptJ006RF.SNX	potatoes	369	potatoes
zambiavariabilityD__potato_season_onset_07mar14_DELTAonFC_base_2000_p0_potatoes__rainfed_nonCLIMATE	DDDD	ptJ006RF.SNX	potatoes	369	potatoes


zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_groundnuts__rainfed_nonCLIMATE	DDDD	grK001RF.SNX	groundnuts	369	groundnuts

zambiavariabilityD__dart_irrigated_DELTAonFC_base_2000_p0_millet__rainfed_nonCLIMATE	DDDD	mill__RF.SNX	potatoes	369	potatoes


"




magic_code=ZAMBIACOV_ plantingDateInMonthShiftInDays=0 chunks_per_case=5 sleeptime=0s # chunks_per_case=10
#magic_code=ZAMBIACOV_ plantingDateInMonthShiftInDays=60 chunks_per_case=5 sleeptime=0s # chunks_per_case=10

zambia_starter_10=\
"
zambiavariabilityF__10_DELTAonFC_base_2000_p0_beans__rainfed_nonCLIMATE	DDDD	bnB002RF.SNX	groundnuts	369	groundnuts

zambiavariabilityF__10_DELTAonFC_base_2000_p0_cassava__rainfed_nonCLIMATE	DDDD	csC004RF.SNX	potatoes	369	potatoes

zambiavariabilityF__10_DELTAonFC_base_2000_p0_groundnuts__rainfed_nonCLIMATE	DDDD	grK001RF.SNX	groundnuts	369	groundnuts

zambiavariabilityF__10_DELTAonFC_base_2000_p0_millet__rainfed_nonCLIMATE	DDDD	mill__RF.SNX	potatoes	369	potatoes

zambiavariabilityF__10_DELTAonFC_base_2000_p0_maizelow__rainfed_nonCLIMATE	DDDD	mzK017RF.SNX	potatoes	369	maize
zambiavariabilityF__10_DELTAonFC_base_2000_p0_maizehigh__rainfed_nonCLIMATE	DDDD	mzK017RF.SNX	potatoes	369	maize

zambiavariabilityF__10_DELTAonFC_base_2000_p0_groundnuts__rainfed_nonCLIMATE	DDDD	grK001RF.SNX	groundnuts	369	groundnuts

zambiavariabilityF__10_DELTAonFC_base_2000_p0_sorghum__rainfed_nonCLIMATE	DDDD	sgK001RF.SNX	potatoes	369	potatoes
"

zambia_starter=\
"
$zambia_starter_10
`echo "$zambia_starter_10" | sed "s/F__10/F__11/g"`
`echo "$zambia_starter_10" | sed "s/F__10/F__12/g"`
"


zambia_full=\
"
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_renumbered\/prnclean_Baseline_NT/g"`
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_gfdl_2055_renumbered\/prn_2055_Baseline_NT_gfdl/g"`
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_hadgem_2055_renumbered\/prn_2055_Baseline_NT_hadgem/g"`
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_ipsl_2055_renumbered\/prn_2055_Baseline_NT_ipsl/g"`
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_miroc_2055_renumbered\/prn_2055_Baseline_NT_miroc/g"`
"

readable_data_list=\
"
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_renumbered\/prnclean_Baseline_NT/g"`
`echo "$zambia_starter" | sed "s/DDDD/pBaseline_NT_miroc_2055_renumbered\/prn_2055_Baseline_NT_miroc/g"`
"

readable_data_list=`echo "$readable_data_list" | grep    cassava`

fi # zambia cutout



if [ 0 = 1 ]; then
. WS_maize.sh

magic_code=JUNKSPEEDTEST_ chunks_per_case=60 sleeptime=0s # chunks_per_case=10

sort_by_size_flag=0 # 1 means sort by size ; anything else means keep the original order

start_readable_data_list=`echo "$full_maize" | sed "s/watersecurityzero__/wstwofix__growncoarse_/g"`
readable_data_list=`echo "$start_readable_data_list" | sed "s/Baseline_renumbered\/ENSO_Baseline/pBaseline_NT_renumbered\/prnclean_Baseline_NT/g"`

fi # cutout

if [ 0 = 1 ]; then

magic_code=MALAWIa_ plantingDateInMonthShiftInDays=0 chunks_per_case=2 sleeptime=0s # chunks_per_case=10

. SRDL_helper_malawi_0.sh

readable_data_list=$malawi_maize_many





#magic_code=WORLDNORMAL_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=0s # chunks_per_case=10
magic_code=WORLDAFpr_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10

a_readable_data_list=\
"
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_hundfert__rainfed_nonCLIMATE	Historical/ENSO_Historical	mzK019RF.SNX	maize	379	maize
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_hundfert__rainfed_nonCLIMATE	Historical/ENSO_Historical	riL011RF.SNX	rice	379	rice
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_hundfert__rainfed_nonCLIMATE	Historical/ENSO_Historical	riL004RF.SNX	rice	379	rice
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_hundfert__rainfed_nonCLIMATE	Historical/ENSO_Historical	bnB028RF.SNX	soybeans	379	soybeans
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_hundfert__rainfed_nonCLIMATE	Historical/ENSO_Historical	grK002RF.SNX	soybeans	379	soybeans


worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	mzK019RF.SNX	maize	379	maize
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	riL011RF.SNX	rice	379	rice
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	riL004RF.SNX	rice	379	rice
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	bnB028RF.SNX	soybeans	379	soybeans
worlddrought__firebird_irrigated_deltaONpik_base_2000_p0_zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	grK002RF.SNX	soybeans	379	soybeans
"

readable_data_list=$a_readable_data_list
#readable_data_list=`echo "$a_readable_data_list" | grep bnB | grep zero`
#readable_data_list=`echo "$a_readable_data_list" | grep -v "zerofert__rainfed_nonCLIMATE	Historical/ENSO_Historical	bnB0"`


magic_code=MALAWIb_ plantingDateInMonthShiftInDays=0 chunks_per_case=4 sleeptime=0s # chunks_per_case=10
. SRDL_helper_malawi_1.sh
#readable_data_list=$malawi_all_water
#readable_data_list=`echo "$malawi_all_water" | grep sgMMMirri`
#readable_data_list=$malawi_three_maize_varieties
readable_data_list=$malawi_three_maize_varieties_more_n

magic_code=MALAWIc_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0s # chunks_per_case=10
. SRDL_helper_malawi_2.sh
readable_data_list=$malawi_with_soils_put_in






fi # cutout


if [ 0 = 1 ]; then
magic_code=MALAWId_ plantingDateInMonthShiftInDays=0 chunks_per_case=5 sleeptime=0s # chunks_per_case=10

. SRDL_helper_malawi_3.sh

export  latitude_resolution=0.5  # these need to match the daily weather files
export longitude_resolution=0.5  # these need to match the daily weather files

readable_data_list=$malawi_all_water



fi # end cutout

if [ 0 = 1 ]; then # wsthree cutout

magic_code=WSTHREE_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=0s # chunks_per_case=10

. WS_groundnuts.sh
. WS_maize.sh
. WS_potatoes.sh
. WS_rice.sh
. WS_sorghum.sh
. WS_soybeans.sh
. WS_wheat.sh


full_readable_data_list=\
"
$full_groundnuts
$full_maize
$full_potatoes
$full_rice
$full_sorghum
$full_soybeans
$full_winterwheat
$full_springwheat
"

readable_data_list=\
"
`echo "$full_potatoes" | grep ptH007`
`echo "$full_potatoes" | grep ptH008`
"

#. extract_failed_cases_from_reassemble_log.sh
#echo "failed = [$failed_cases]"
#readable_data_list=$failed_cases

# running scenario_1
#readable_data_list=`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_renumbered\/scenario1/g"`
readable_data_list=\
"

`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_renumbered\/scenario1/g"`
"

all="

$readable_data_list
`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_renumbered\/scenario1/g"`

`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Baseline_NT_new_2_renumbered\/baseline2/g"`
`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Baseline_NT_new_3_renumbered\/baseline3/g"`

`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_2_renumbered\/scenario1v2/g"`
`echo "$readable_data_list" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_3_renumbered\/scenario1v3/g"`
"






potatoes_some_readable_data_list=\
"
$full_potatoes
`echo "$full_potatoes" | sed "s/Baseline_NT_new_renumbered\/baseline/Scenario_1_NT_new_renumbered\/scenario1/g"`
"

fi # end wsthree cutout

if [ 0 = 1 ]; then
magic_code=POTWORLDb_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=90s # chunks_per_case=10

. SRDL_helper_potatoes_A.sh

readable_data_list="
`echo "$potatoes_B" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_gfdl\/raw_Historical_rcp8p5_2055_gfdl/g"`
`echo "$potatoes_B" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_hadgem2\/raw_Historical_rcp8p5_2055_hadgem2/g"`
`echo "$potatoes_B" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_ipsl\/raw_Historical_rcp8p5_2055_ipsl/g"`
`echo "$potatoes_B" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g"`
`echo "$potatoes_B" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g"`
"

readable_data_list=`echo "$readable_data_list" | sed "s/379/541/g" | grep grown`

readable_data_list=`echo "$readable_data_list" | grep ptH004RF | grep hadgem2`

rrrrreadable_data_list="
"
# chunks = 80 for hadgem ; 10 for rest


magic_code=POTWORLDc_ plantingDateInMonthShiftInDays=0 chunks_per_case=60 sleeptime=40s # chunks_per_case=10

. SRDL_helper_potatoes_A.sh

readable_data_list=`echo "$potatoes_C" | grep -v grown`


magic_code=GUATE_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=1s # chunks_per_case=10

. SRDL_helper_potatoes_A.sh

readable_data_list=`echo "$potatoes_guate" | sed "s/RF/IR/g"`

fi

if [ 0 = 1 ]; then

magic_code=POTWORLDd_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=5s # chunks_per_case=10
. SRDL_helper_potatoes_A.sh
all_readable_data_list=\
"
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_gfdl\/raw_Historical_rcp8p5_2055_gfdl/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_hadgem2\/raw_Historical_rcp8p5_2055_hadgem2/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_ipsl\/raw_Historical_rcp8p5_2055_ipsl/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g"`

`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_gfdl\/raw_Historical_rcp8p5_2055_gfdl/g ; s/379/541/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_hadgem2\/raw_Historical_rcp8p5_2055_hadgem2/g ; s/379/541/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_ipsl\/raw_Historical_rcp8p5_2055_ipsl/g ; s/379/541/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g ; s/379/541/g"`
`echo "$potatoes_D" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g ; s/379/541/g"`
"

#`echo "$all_readable_data_list" | grep miroc | sed "s/rcp8p5/rcp4p5/g ; s/541/487/g"`
#`echo "$all_readable_data_list" | grep miroc `
#`echo "$all_readable_data_list" | grep miroc | sed "    s/2055/2085/g ; s/541/936/g"`
readable_data_list=\
"

`echo "$all_readable_data_list" | sed "    s/2055/2085/g ; s/rcp8p5/rcp4p5/g ; s/541/538/g"`
"

readable_data_list=`echo "$readable_data_list" | grep -v "379"`



#readable_data_list=`echo "$readable_data_list" | grep ptH002RF | grep 379 | grep 2085`

#`echo "$potatoes_D"`

#`echo "$potatoes_B" | grep -v grown | sort -n`
#`echo "$potatoes_B" | grep grown`

#readable_data_list=`echo "$readable_data_list" | grep ptH007`



#. extract_failed_cases_from_reassemble_log.sh
#echo "failed = [$failed_cases]"
#readable_data_list=$failed_cases


#readable_data_list=`echo "$readable_data_list" | grep -v "^$" | head -n 1 | sed "s/Scenario_1_NT_new_renumbered\/scenario1/Historical\/ENSO_Historical/g ; s/ptH007IR/mzK019RFoverview/g"`

#readable_data_list=`echo "$readable_data_list" | grep -v "^$" | head -n 1 | sed "s/Scenario_1_NT_new_renumbered\/scenario1/Historical\/ENSO_Historical/g ; s/ptH007IR/mzK019RF/g"`


#magic_code=PHILIPPINESENSO_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
phil_readable_data_list=\
"
phillipinesenso__1_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__2_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__3_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__4_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__5_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__6_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__7_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__8_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__9_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__10_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__11_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice
phillipinesenso__12_deltaONpikNOV_base_2000_p0_rice__irrigated_nonCLIMATE	Historical/ENSO_Historical	riK001IR.SNX	middleHeavyThreeSplitWithFlowering	379	rice

"

#readable_data_list=`echo "$readable_data_list" | sed "s/riK001IR/riK001RF/g"`






. SRDL_helper_shouldwork.sh

magic_code=YYYMEXICO_ plantingDateInMonthShiftInDays=0 chunks_per_case=5 sleeptime=0s # chunks_per_case=10

readable_data_list=\
"
`echo "$full_maize" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_noresm\/NOSRAD_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_maize" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_miroc\/NOSRAD_Historical_rcp8p5_2055_miroc/g"`

`echo "$full_maize" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_maize" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g"`
"





. SRDL_helper_srad_wheat.sh

magic_code=DAILYTEST_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=0s # chunks_per_case=10

readable_data_list=\
"
`echo "$full_springwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_noresm\/NOSRAD_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_springwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_miroc\/NOSRAD_Historical_rcp8p5_2055_miroc/g"`

`echo "$full_springwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_springwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g"`



`echo "$full_winterwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_noresm\/NOSRAD_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_winterwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_NOSRAD_miroc\/NOSRAD_Historical_rcp8p5_2055_miroc/g"`

`echo "$full_winterwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_noresm\/raw_Historical_rcp8p5_2055_noresm/g"`
`echo "$full_winterwheat" | sed "s/Historical\/ENSO_Historical/Historical_rcp8p5_2055_miroc\/raw_Historical_rcp8p5_2055_miroc/g"`
"

fi

if [ 0 = "seasiaenso" ]; then

. SRDL_helper_seasiaenso.sh

#magic_code=ENSOBASICRICE_ plantingDateInMonthShiftInDays=0 chunks_per_case=240 sleeptime=0s # chunks_per_case=10
#magic_code=ENSORICEUNFLOODED_ plantingDateInMonthShiftInDays=0 chunks_per_case=200 sleeptime=0s # chunks_per_case=10

#readable_data_list=$seasiaenso_starter

#riMMMirriga001.SNX: 1 IR             20    40   100   -99 IR001     1     0
#riMMMirriga005.SNX: 1 IR             20    40   100   -99 IR001     5     0
#riMMMirriga1o2.SNX: 1 IR             20    40   100   -99 IR001   0.5     0
#riMMMrrrainfed.SNX: 1 IR             20    40   100   -99 IR001   0.5     0

#magic_code=ENSOfull_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSO100mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSO050mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSO010mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSO001mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSOcustomA_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#magic_code=ENSOcustomC_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10

#magic_code=ENSOcustomHD_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHD_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHE_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHE_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHF_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHF_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHG_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHG_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHH_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHH_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHI_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHI_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHJ_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHJ_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHK_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHK_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHL_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHL_rice" | sed "s/allenso/ensoB/g" | grep __1_`
#magic_code=ENSOcustomHM_ plantingDateInMonthShiftInDays=0 chunks_per_case=80 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_customHM_rice" | sed "s/allenso/ensoB/g" | grep __1_`

#magic_code=ALLENSOunflooded_ plantingDateInMonthShiftInDays=0 chunks_per_case=160 sleeptime=0s # chunks_per_case=10
#magic_code=ALLENSO010mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=160 sleeptime=0s # chunks_per_case=10
#magic_code=ALLENSO050mm_ plantingDateInMonthShiftInDays=0 chunks_per_case=160 sleeptime=0s # chunks_per_case=10




#magic_code=ENSO1noirrigation_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_flooded_rice_no_irrigation"        | sed "s/allenso/ensoB/g"`
#magic_code=ENSO2firstthirddrought_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_flooded_rice_drought_first_third"  | sed "s/allenso/ensoB/g"`
#magic_code=ENSO3middlethirddrought_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_flooded_rice_drought_middle_third" | sed "s/allenso/ensoB/g"`
#magic_code=ENSO4lastthirddrought_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_flooded_rice_drought_last_third"   | sed "s/allenso/ensoB/g"`
#magic_code=ENSO5nodrought_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_flooded_rice_no_drought"           | sed "s/allenso/ensoB/g"`

#magic_code=ENSO0purerainfed_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_pure_rainfed"                 | sed "s/allenso/ensoB/g"`
#magic_code=ENSO6autofurrow_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$seasiaenso_furrow_rice_automatic"        | sed "s/allenso/ensoB/g"`



#magic_code=ENSO0purerainfed_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$coarse_maize"`
#magic_code=ENSO6autofurrow_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$coarse_maize" | grep "IR"`




#magic_code=ENSO0purerainfedDROUGHT_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$coarse_maize"`
#magic_code=ENSO6autofurrowDROUGHT_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
jjjreadable_data_list=\
"
`echo "$seasiaenso_pure_rainfed" | grep "RF"`
`echo "$coarse_maize" | grep "RF"`
"



#magic_code=ENSOSEARCHnormal_ plantingDateInMonthShiftInDays=0 chunks_per_case=8 sleeptime=0s # chunks_per_case=10
#magic_code=ENSOSEARCHdrttol_ plantingDateInMonthShiftInDays=0 chunks_per_case=8 sleeptime=0s # chunks_per_case=10

othersreadable_data_list=\
"
$C_maize
$C_rice
"



#magic_code=ENSO0purerainfed_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$coarse_tomatoes" | grep RF`
#readable_data_list=`echo "$coarse_beans" | grep RF`

magic_code=ENSO6autofurrow_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=`echo "$coarse_tomatoes" | grep "IR"`
readable_data_list=`echo "$coarse_beans" | grep "IR"`








#magic_code=POWER${magic_code}
#readable_data_list=`echo "$readable_data_list" | sed "s/^ensoB/powerensoB/g ; s/Historical\/ENSO_Historical/CLEANED_seasia_power_1994_2016\/concentrated_0.0_seasia_power_raw/g"`

echo "$readable_data_list"



#`echo "$seasiaenso_50mm_rice" | grep "__1_"`
#`echo "$seasiaenso_unflooded_rice" | sed "s/allenso/ensoB/g"`
#`echo "$seasiaenso_unflooded_rice" | grep "__1_"`
#`echo "$seasiaenso_ir_rice" | grep "__1_"`
#`echo "$seasiaenso_50mm_rice" | grep "__1_"`
#`echo "$seasiaenso_100mm_rice" | grep "__1_"`
#$seasiaenso_unflooded_rice



#magic_code=POWERENSO_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=0s # chunks_per_case=10
#magic_code=POWERENSO_ plantingDateInMonthShiftInDays=7 chunks_per_case=40 sleeptime=0s # chunks_per_case=10
#magic_code=POWERENSO_ plantingDateInMonthShiftInDays=15 chunks_per_case=40 sleeptime=0s # chunks_per_case=10
#magic_code=POWERENSO_ plantingDateInMonthShiftInDays=22 chunks_per_case=40 sleeptime=0s # chunks_per_case=10

#magic_code=POWERENSO700_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=0s # chunks_per_case=10

#readable_data_list=$brac_powerenso
#readable_data_list=`echo "$brac_powerenso" | grep _10_`

junk_readable_data_list=\
"
`echo "$powerenso" | grep riK001RF | grep __2_`
`echo "$powerenso" | grep riK001RF | grep __4_`
`echo "$powerenso" | grep riK001RF | grep __5_`
`echo "$powerenso" | grep riK001RF | grep __6_`
`echo "$powerenso" | grep mzK014IR | grep __7_`
"



. SRDL_helper_aflatoxinA.sh

magic_code=AFLADAILY_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
#readable_data_list=$aflatoxin_groundnuts_A_june_july_daily
readable_data_list=$aflatoxin_maize_A_june_july_daily



. SRDL_helper_ar6test.sh

magic_code=ARSIXTEST_ plantingDateInMonthShiftInDays=0 chunks_per_case=20 sleeptime=0s # chunks_per_case=10
readable_data_list=$maize_test_srdl


. SRDL_helper_ar6firstlight_maize.sh
magic_code=AR6FIRSTLIGHT_ plantingDateInMonthShiftInDays=0 chunks_per_case=40 sleeptime=1s # chunks_per_case=10
readable_data_list="
`echo "$full_maize" | sed "s/ukesm1/mri/g"`
$full_maize
"


#. SRDL_helper_ar6illinois.sh
#magic_code=AR6ILLINOIS_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0s # chunks_per_case=10
#readable_data_list="
#`echo "$full_il_maize" | sed "s/GGGGG/mri/g"`
#`echo "$full_il_maize" | sed "s/GGGGG/ukesm1/g"`
#"
#readable_data_list=`echo "$readable_data_list" | grep mzK029sradIR`


. SRDL_helper_illinois_thing.sh
magic_code=ILNARROWDAILY3colddays_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0.1s # chunks_per_case=10
readable_data_list="
$illinois_maize_full
"
#$illinois_maize_future
#$illinois_maize_full




. SRDL_helper_maine.sh
magic_code=MAINEa_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0.3s # chunks_per_case=10
readable_data_list="
`echo "$maine_maize_full" | grep RF`
"


. SRDL_helper_moreilnarrow_maize.sh
magic_code=MOREILNARROW_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0.0s # chunks_per_case=10
readable_data_list="
`echo "$all_the_rest" | grep N250                                `
`echo "$all_the_rest" | grep N250 | sed "s/2001_2100/1901_2000/g"`
"
#`echo "$moreillinois_maize_full" | sed "s/2001_2100/1901_2000/g"`

readable_data_list=`echo "$readable_data_list" | grep IR`

#readable_data_list=`echo "$readable_data_list" | grep mzK013RF`
#readable_data_list=`echo "$readable_data_list" | grep mzK014RF`
#readable_data_list=`echo "$readable_data_list" | grep mzK015RF`
jjjreadable_data_list="
`echo "$readable_data_list" | grep mzK016RF`
`echo "$readable_data_list" | grep mzK017RF`
`echo "$readable_data_list" | grep mzK018RF`
`echo "$readable_data_list" | grep mzK021RF`
`echo "$readable_data_list" | grep mzK024RF`
`echo "$readable_data_list" | grep mzK025RF`
`echo "$readable_data_list" | grep mzK026RF`
`echo "$readable_data_list" | grep mzK027RF`
"
#`echo "$readable_data_list" | grep mzK023RF`



. SRDL_helper_crazy_wheat.sh
magic_code=CRAZYWHEATa_ plantingDateInMonthShiftInDays=0 chunks_per_case=1 sleeptime=0.0s # chunks_per_case=10
readable_data_list="
`echo "$full_crazy_wheat"`
"

fi # end cutout

. SRDL_helper_catA.sh
magic_code=CATa_ plantingDateInMonthShiftInDays=0 chunks_per_case=5 sleeptime=0.0s # chunks_per_case=10
readable_data_list="$catA_maize_full"



for (( plantingDateInMonthShiftInDays=0 ; plantingDateInMonthShiftInDays <= 364 ; plantingDateInMonthShiftInDays++ )); do
#for (( plantingDateInMonthShiftInDays=1 ; plantingDateInMonthShiftInDays <= 1 ; plantingDateInMonthShiftInDays++ )); do

echo "-- pDIMSID=${plantingDateInMonthShiftInDays} --"


#. extract_failed_cases_from_reassemble_log.sh

#echo --------------
#echo "$failed_cases"
#echo --------------

#readable_data_list=$failed_cases


sort_by_size_flag=0 # 1 means sort by size ; anything else means keep the original order

# the usual boring stuff
#export  latitude_resolution=0.5  # these need to match the daily weather files
#export longitude_resolution=0.5  # these need to match the daily weather files

# crazy climate model resolution from the catastrophe investigations
export  latitude_resolution=1.875  # these need to match the daily weather files
export longitude_resolution=2.5  # these need to match the daily weather files


####################################################################




. default_paths_etc.sh



if [ $sort_by_size_flag = 1 ]; then

for case_line in $readable_data_list; do
  iii=`echo "$case_line" | cut -f1`
  geog_file_size=`ls -l ${input_data_dir}${iii}_geog.txt | tr -s " " | cut -d" " -f5`
  sortable_list="$sortable_list
$case_line	$geog_file_size"
done

sorted_list=`echo "$sortable_list" | grep -v "^$" | sort -n -r -k7`


real_readable_data_list=$sorted_list

else

real_readable_data_list=$readable_data_list

fi






magic_reassembly_log=REASSEMBLE_log.TXT 

# first, let's clean up any blank lines in the machine list

# now, source in the default settings....
source default_paths_etc.sh

  screen_dump=${logs_dir}SCREEN_DUMP.TXT

if [ $1 = "run" ]; then
  date > $screen_dump
  echo "rdl = [$real_readable_data_list]" >> $screen_dump
  echo "chunks per case = $chunks_per_case" >> $screen_dump
else
  echo "rdl = [`echo "$real_readable_data_list" | grep -v "^$" | cat -n`]"
  echo "chunks per case = $chunks_per_case"
  echo "output log in $magic_reassembly_log"
  date > $magic_reassembly_log
fi


# get rid of the blank lines so that it can be sequentially numbered...
data_list=`echo "$real_readable_data_list" | grep -v "^$"`

n_to_try=`echo "$data_list" | wc -l`

echo "trying $n_to_try cases"


countercounter=1
success_failure_list=""
for (( case_num=1 ; case_num <= n_to_try ; case_num++ ))
do

  let "case_index = case_num - 1"

# nonCLIMATE	daily (subdir/tag)	SNX	fertilizer	CO2	irrigation

  data_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f1`
 daily_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f2`
     X_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f3`
  crop_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f4`
   co2_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f5`
  irri_to_use=`echo "$data_list" | sed -n "${case_num}p" | cut -f6`


  if [ $1 = "run" ]; then
    if [ $case_num -eq 1 ]; then
      echo "trying $n_to_try cases" >> $screen_dump
    fi
    echo "running tiled parallelizer"
    ./mink3daily_tiled_parallelizer.sh $data_to_use $daily_to_use $X_to_use $crop_to_use $co2_to_use $irri_to_use $chunks_per_case $plantingDateInMonthShiftInDays 1>>$screen_dump 2>&1

  echo "sleeping for $sleeptime (done with $case_num of $n_to_try)"
  sleep $sleeptime

  else
    echo " -- trying to reassemble #$countercounter --"
#    echo "             data=[$data_to_use] ; co2_to_use = [$co2_to_use]"
    success_failure_list="${success_failure_list}
`./mink3daily_reassemble_outputs.sh ${daily_to_use##*/}_d${plantingDateInMonthShiftInDays/-/n}_${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code | tee -a $magic_reassembly_log | grep REASSEMBL`"
#`./mink3daily_reassemble_outputs.sh ${daily_to_use##*/}_${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code $plantingDateInMonthShiftInDays | tee -a $magic_reassembly_log | grep REASSEMBL`"
#`./mink2daily_reassemble_outputs.sh ${data_to_use%%_data}_STATS $co2_to_use $chunks_per_case $X_to_use $magic_code | tee -a $magic_reassembly_log | grep REASSEMBL`"
    let "countercounter++"
  fi


done

echo "$success_failure_list" | grep -v "^$" | sort | cat -n



done # plantingDateInMonthShiftInDays



