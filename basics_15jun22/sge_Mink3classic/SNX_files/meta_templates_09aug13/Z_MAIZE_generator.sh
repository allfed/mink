#!/bin/bash


variety_search=VVVVVV
name_search=NNNN

IR_meta_template=meta_maize_IR.SNX
RF_meta_template=meta_maize_RF.SNX

out_dir=new_full_maize

# and escape any funny business
# variety / name in file / name of file
base_variety_list=\
"

GF0001	Baseline DevelopING	mzC001
IB0041	Baseline DevelopED	mzC002
"

virtual_variety_list=\
"
GF0101	Baseline 10%shorter DevelopING	mzD003
GF0201	Baseline 10%longer DevelopING	mzD004
GF0301	Yield norm cycle DevelopING	mzD005
GF0401	Yield 10%shorter DevelopING	mzD006
GF0501	Yield 10%longer DevelopING	mzD007

GF0100	Garst 10%shorter	mzD008
GF0200	Garst 10%longer	mzD009
GF0300	GYield norm cycle	mzD010
GF0400	GYield 10%shorter	mzD011
GF0500	GYield 10%longer	mzD012

"

variety_list=\
"
CYMA01	WH403	mzK013
CYMA02	ZM521	mzK014
CYMA03	BH660	mzK015
CYMA04	SC513	mzK016
CYMA05	SC403	mzK017
CYMA06	PIO 30F32	mzK018
IB0041	Garst 8808	mzK019
IB0173	DKB 333B	mzK020
IB0026	A632 x W117	mzK021
IB0155	DEA	mzK022
CT0001	CF1505	mzK023
CT0002	PIO 31R88	mzK024
CT0003	Suwan 3851	mzK025
CT0004	DEKALB XL82	mzK026
IB0035	McCurdy 84aa	mzK027
GH0010	OBATAMPA	mzK028
IB0185	JACKSON HYBRI	mzK029

"

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

  # determine the output names for the filled in files
  full_file_name_IR=${out_dir}/${file_name}IR.SNX
  full_file_name_RF=${out_dir}/${file_name}RF.SNX

  # do the search and replace
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $IR_meta_template > $full_file_name_IR
  sed "s/${variety_search}/${variety_code}/g ; s/${name_search}/${variety_name}/g" $RF_meta_template > $full_file_name_RF

done






