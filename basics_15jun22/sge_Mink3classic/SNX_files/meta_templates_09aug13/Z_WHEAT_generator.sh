#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_wheat_IR.SNX
RF_meta_template=meta_wheat_RF.SNX

#IR_meta_template=meta_wheat_IR_low_stress.SNX
#RF_meta_template=meta_wheat_RF_low_stress.SNX

#IR_meta_template=meta_wheat_IR_fixedplanting.SNX
#RF_meta_template=meta_wheat_RF_fixedplanting.SNX

#out_dir=full_wheat_fixedplanting
#out_dir=full_wheat_search

out_dir=new_full_wheat

# and escape any funny business
# variety / name in file / name of file
old_variety_list=\
"
IB0010	Seri82BA ME1	whC001
IB0011	PBW343BA ME1	whC002
IB0012	KubsaBA ME2A	whC003
IB0013	TajanBA ME2B	whC004
IB0014	AlondraBA ME3	whC005
IB0015	BacanoraBA ME4A,9	whC006
IB0016	DonErnestoBA ME4B	whC007
IB0017	HI617BA ME4C	whC008
IB0018	KanchanBA ME5A	whC009
IB0019	DebieraBA ME 5B	whC010
IB0020	SaratovBA ME6	whC011
IB0021	PehlivanBA ME7	whC012
IB0022	HalconsnaBA ME8A	whC013
IB0023	KatyaBA ME8B	whC014
IB0024	ChinaBBA ME10A	whC015
IB0025	BezostayaBA ME10B	whC016
IB0026	BrigadierBA ME11A	whC017
IB0027	KoreaBA ME11B	whC018
IB0028	Gerek79BA ME12	whC019
IB0029	KauzBA Sika	whC020
IB0030	AttilaBA Sika	whC021
"
base_variety_list=\
"
IB0100	Seri82BA ME1	whE001
IB0200	PBW343BA ME1	whE002
IB0300	KubsaBA ME2A	whE003
IB0400	TajanBA ME2B	whE004
IB0500	AlondraBA ME3	whE005
IB0600	BacanoraBA ME4A	whE006
IB0700	DonErnestoBA ME4B	whE007
IB0800	HI617BA ME4C	whE008
IB0900	KanchanBA ME5A	whE009
IB1000	DebieraBA ME 5B	whE010
IB1100	SaratovBA ME6	whE011
IB1200	PehlivanBA ME7	whE012
IB1300	HalconsnaBA ME8A	whE013
IB1400	KatyaBA ME8B	whE014
IB1500	Bacanora modified ME9	whE015
IB1600	ChinaBBA ME10A	whE016
IB1700	BezostayaBA ME10B	whE017
IB1800	BrigadierBA ME11A	whE018
IB1900	KoreaBA ME11B	whE019
IB2000	Gerek79BA ME12	whE020
"

variety_list=\
"
IB0100	Seri82BA ME1	whK001
IB0200	PBW343BA ME1	whK002
IB0600	BacanoraBA ME4A	whK006
IB0700	DonErnestoBA ME4B	whK007
IB0900	KanchanBA ME5A	whK009
IB1000	DebieraBA ME 5B	whK010
IB1100	SaratovBA ME6	whK011
IB1200	PehlivanBA ME7	whK012
IB1300	HalconsnaBA ME8A	whK013
IB1500	Bacanora modified ME9	whK015
IB1600	ChinaBBA ME10A	whK016
RRWW00	alt WW low	whK076
"

low_variety_list=\
"
IB0100	Seri82BA ME1	whN081
IB0200	PBW343BA ME1	whN082
IB0600	BacanoraBA ME4A	whN086
IB0700	DonErnestoBA ME4B	whN087
IB0900	KanchanBA ME5A	whN089
IB1000	DebieraBA ME 5B	whN090
IB1100	SaratovBA ME6	whN091
IB1200	PehlivanBA ME7	whN092
IB1300	HalconsnaBA ME8A	whN093
IB1500	Bacanora modified ME9	whN095
IB1600	ChinaBBA ME10A	whN096
RRWW00	alt WW low	whN097
"

# virtual stuff....
virtual_variety_list=\
"
IB0101	SerB + S	whF021
IB0102	SerB + L	whF022
IB0103	SerB + Y	whF023
IB0104	SerB + Y	whF024
IB0105	SerB + Y	whF025

IB0201	PbwB + S	whF026
IB0202	PbwB + L	whF027
IB0203	PbwB + Y	whF028
IB0204	PbwB + Y	whF029
IB0205	PbwB + Y	whF030

IB0601	BacB + S	whF031
IB0602	BacB + L	whF032
IB0603	BacB + Y	whF033
IB0604	BacB + Y	whF034
IB0605	BacB + Y	whF035

IB0701	DonB + S	whF036
IB0702	DonB + L	whF037
IB0703	DonB + Y	whF038
IB0704	DonB + Y	whF039
IB0705	DonB + Y	whF040

IB0901	KanB + S	whF041
IB0902	KanB + L	whF042
IB0903	KanB + Y	whF043
IB0904	KanB + Y	whF044
IB0905	KanB + Y	whF045

IB1001	DebB + S	whF046
IB1002	DebB + L	whF047
IB1003	DebB + Y	whF048
IB1004	DebB + Y	whF049
IB1005	DebB + Y	whF050

IB1101	SarB + S	whF051
IB1102	SarB + L	whF052
IB1103	SarB + Y	whF053
IB1104	SarB + Y	whF054
IB1105	SarB + Y	whF055

IB1201	PehB + S	whF056
IB1202	PehB + L	whF057
IB1203	PehB + Y	whF058
IB1204	PehB + Y	whF059
IB1205	PehB + Y	whF060

IB1301	HalB + S	whF061
IB1302	HalB + L	whF062
IB1303	HalB + Y	whF063
IB1304	HalB + Y	whF064
IB1305	HalB + Y	whF065

IB1501	BacB + S	whF066
IB1502	BacB + L	whF067
IB1503	BacB + Y	whF068
IB1504	BacB + Y	whF069
IB1505	BacB + Y	whF070

IB1601	BeiB + S	whF071
IB1602	BeiB + L	whF072
IB1603	BeiB + Y	whF073
IB1604	BeiB + Y	whF074
IB1605	BeiB + Y	whF075

"

winter_wheat_test_variety_list=\
"
RRWW00	alt WW low	whT076
RRWW01	alt WW low	whT077
"



pad_with_zeros() {

         value=$1
  total_digits=$2

  length_of_value=${#value}

  let "n_pads = total_digits - length_of_value"

  out_string=""

  for (( padding=1 ; padding <= n_pads ; padding++ ))
  do
    out_string=${out_string}0
  done

  echo "$out_string$value"
}


#variety_list=""
#for (( num=1 ; num <= 2187 ; num++ )); do
#for (( num=2201 ; num <= 2254 ; num++ )); do
#for (( num=2301 ; num <= 2540 ; num++ )); do
#for (( num=2601 ; num <= 2816 ; num++ )); do
#for (( num=0 ; num <= 57 ; num++ )); do
#  variety_list="$variety_list
#A`pad_with_zeros $num 5`	A`pad_with_zeros $num 5`	A`pad_with_zeros $num 5`"
#
##Z`pad_with_zeros $num 5`	Z`pad_with_zeros $num 5`	Z`pad_with_zeros $num 5`"
#done



####################

# reset the inter-field-seperator
IFS="
"

# make the output directory if it isn't there already
mkdir -p $out_dir

# deal with each case
for variety_line in $variety_list; do

  # pull out the pieces
  variety_code=`echo "$variety_line" | cut -f1`
  variety_name=`echo "$variety_line" | cut -f2`
     file_name=`echo "$variety_line" | cut -f3`

  echo "-- $variety_code --"

  # determine the output names for the filled in files
  full_file_name_IR=${out_dir}/${file_name}IR.SNX
  full_file_name_RF=${out_dir}/${file_name}RF.SNX

  # do the search and replace
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $IR_meta_template > $full_file_name_IR
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $RF_meta_template > $full_file_name_RF

done






