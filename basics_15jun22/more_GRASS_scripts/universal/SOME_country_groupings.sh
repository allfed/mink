#!/bin/bash


# this is a list of country groupings we have had need of in the past





### some country groupings

# world bank groupings
east_asia_pacific="American Samoa,Cambodia,China,Fiji,Indonesia,Kiribati,North Korea,Laos,Malaysia,Marshall Is.,Micronesia,Mongolia,Myanmar,Palau,Papua New Guinea,Philippines,Samoa,Solomon Is.,Thailand,Timor Leste,Tonga,Vanuatu,Vietnam"

europe_central_asia="Albania,Armenia,Azerbaijan,Belarus,Bosnia & Herzegovina,Bulgaria,Croatia,Georgia,Kazakhstan,Kyrgyzstan,Latvia,Lithuania,Macedonia,Moldova,Poland,Romania,Russia,Serbia & Montenegro,Tajikistan,Turkey,Turkmenistan,Ukraine,Uzbekistan"

#echo "WARNING: Serbia and Montenegro are together at the moment..."

latin_america_caribbean="Argentina,Belize,Bolivia,Brazil,Chile,Colombia,Costa Rica,Cuba,Dominica,Dominican Republic,Ecuador,El Salvador,Grenada,Guatemala,Guyana,Haiti,Honduras,Jamaica,Mexico,Nicaragua,Panama,Paraguay,Peru,St. Kitts & Nevis,St. Lucia,St. Vincent & the Grenadines,Suriname,Uruguay,Venezuela"

middle_east_north_africa="Algeria,Djibouti,Egypt,Iran,Iraq,Jordan,Lebanon,Libya,Morocco,Syria,Tunisia,West Bank,Gaza Strip,Yemen"
middle_east_north_africa_NEW="Algeria,Bahrain,Djibouti,Egypt,Iran,Iraq,Israel,Jordan,Kuwait,Lebanon,Libya,Malta,Morocco,Oman,Qatar,Saudi Arabia,Syria,Tunisia,United Arab Emirates,West Bank,Gaza Strip,Yemen"

# 7777777

south_asia="Afghanistan,Bangladesh,Bhutan,India,Maldives,Nepal,Pakistan,Sri Lanka"

sub_saharan_africa="Angola,Benin,Botswana,Burkina Faso,Burundi,Cameroon,Cape Verde,Central African Republic,Chad,Comoros,Congo' DRC,Congo,Cote d'Ivoire,Eritrea,Ethiopia,Gabon,The Gambia,Ghana,Guinea,Guinea-Bissau,Kenya,Lesotho,Liberia,Madagascar,Malawi,Mali,Mauritania,Mauritius,Mayotte,Mozambique,Namibia,Niger,Nigeria,Rwanda,Sao Tome & Principe,Senegal,Seychelles,Sierra Leone,Somalia,South Africa,Sudan,Swaziland,Tanzania,Togo,Uganda,Zambia,Zimbabwe"

adb_central_asia="Armenia,Azerbaijan,Georgia,Kazakhstan,Kyrgyzstan,Tajikistan,Turkmenistan,Uzbekistan"
adb_east_asia="China,South Korea,Mongolia"
adb_south_asia="Afghanistan,Bangladesh,Bhutan,India,Maldives,Nepal,Pakistan,Sri Lanka"
adb_southeast_asia="Cambodia,Indonesia,Laos,Malaysia,Myanmar,Philippines,Singapore,Thailand,Vietnam"
adb_pacific="Fiji,Kiribati,Marshall Is.,Micronesia,Palau,Papua New Guinea,Samoa,Solomon Is.,Timor Leste,Tonga,Vanuatu"

all_africa="Algeria,Djibouti,Egypt,Libya,Morocco,Tunisia,Angola,Benin,Botswana,Burkina Faso,Burundi,Cameroon,Cape Verde,Central African Republic,Chad,Comoros,Congo' DRC,Congo,Cote d'Ivoire,Equatorial Guinea,Eritrea,Ethiopia,Gabon,The Gambia,Ghana,Guinea,Guinea-Bissau,Kenya,Lesotho,Liberia,Madagascar,Malawi,Mali,Mauritania,Mauritius,Mayotte,Mozambique,Namibia,Niger,Nigeria,Rwanda,Sao Tome & Principe,Senegal,Seychelles,Sierra Leone,Somalia,South Africa,Sudan,Swaziland,Tanzania,Togo,Uganda,Western Sahara,Zambia,Zimbabwe"

caricom="Antigua & Barbuda,The Bahamas,Barbados,Belize,Dominica,Grenada,Guyana,Haiti,Jamaica,Montserrat,St. Kitts & Nevis,St. Lucia,St. Vincent & the Grenadines,Suriname,Trinidad & Tobago"

other_carribean="Anguilla,Bermuda,British Virgin Is.,Cayman Is.,Turks & Caicos Is."

old_caricom="Antigua & Barbuda,The Bahamas,Barbados,Belize,Dominica,Grenada,Guyana,Haiti,Jamaica,Montserrat,St. Kitts & Nevis,St. Lucia,St. Vincent & the Grenadines,Suriname,Trinidad & Tobago,Anguilla,Bermuda,British Virgin Is.,Cayman Is.,Turks & Caicos Is."


commonwealth_countries=\
"Antigua & Barbuda,Australia,The Bahamas,Bangladesh,Barbados,Belize,Botswana,Brunei,Cameroon,Canada,Cyprus,Dominica,Fiji,The Gambia,Ghana,Grenada,Guyana,India,Jamaica,Kenya,Kiribati,Lesotho,Malawi,Malaysia,Maldives,Malta,Mauritius,Mozambique,Namibia,Nauru,New Zealand,Nigeria,Pakistan,Papua New Guinea,St. Kitts & Nevis,St. Lucia,St. Vincent & the Grenadines,Samoa,Seychelles,Sierra Leone,Singapore,Solomon Is.,South Africa,Sri Lanka,Swaziland,Tonga,Trinidad & Tobago,Tuvalu,Uganda,United Kingdom,Tanzania,Vanuatu,Zambia"


######### IIASA regions for project foresight
iiasa_north_america="Canada,United States"
iiasa_caribbean="Mexico,Belize,Cuba,Costa Rica,Dominican Republic,El Salvador,Guatemala,Haiti,Honduras,Nicaragua,Panama"
iiasa_south_america="Argentina,Brazil,Chile,Colombia,Venezuela,Suriname,Guyana,Ecuador,Peru,Bolivia,Paraguay,Uruguay"

iiasa_eastern_europe="Bulgaria,Czech Republic,Hungary,Moldova,Slovakia,Romania,Poland,Ukraine"
iiasa_northern_europe="Belarus,Estonia,Latvia,Lithuania,Ireland,United Kingdom,Denmark,Finland,Norway,Sweden"
iiasa_southern_europe="Albania,Bosnia & Herzegovina,Macedonia,Croatia,Greece,Slovenia,Serbia & Montenegro,Spain,Portugal,Italy"
iiasa_western_europe="Austria,Switzerland,Belgium,Netherlands,Luxembourg,France,Germany"

iiasa_eastern_africa="Burundi,Djibouti,Eritrea,Ethiopia,Kenya,Madagascar,Malawi,Mozambique,Rwanda,Somalia,Tanzania,Uganda,Zambia,Zimbabwe"
iiasa_central_africa="Angola,Cameroon,Central African Republic,Chad,Congo,Congo' DRC,Equatorial Guinea,Gabon"
iiasa_northern_africa="Algeria,Egypt,Libya,Morocco,Sudan,Tunisia"
iiasa_southern_africa="Botswana,Lesotho,Namibia,South Africa,Swaziland"
iiasa_western_africa="Benin,Burkina Faso,The Gambia,Ghana,Guinea,Guinea-Bissau,Cote d'Ivoire,Liberia,Mali,Mauritania,Niger,Nigeria,Senegal,Sierra Leone,Togo"

iiasa_middle_east="Cyprus,Kuwait,Oman,Qatar,Saudi Arabia,United Arab Emirates,Yemen,Iraq,Israel,Jordan,Lebanon,Syria,Turkey"
iiasa_southeast_asia="Indonesia,Malaysia,Myanmar,Philippines,Singapore,Cambodia,Laos,Thailand,Vietnam,Papua New Guinea"
iiasa_south_asia="Afghanistan,Bangladesh,Bhutan,India,Iran,Nepal,Pakistan,Sri Lanka"
iiasa_east_asia="China,Mongolia,North Korea,South Korea"
iiasa_central_asia="Armenia,Azerbaijan,Georgia,Kazakhstan,Kyrgyzstan,Russia,Tajikistan,Turkmenistan,Uzbekistan"

iiasa_oceania="Australia,Japan,New Zealand"
iiasa_rest_of_world="Bahrain,Cape Verde,Comoros,Fiji,Grenada,Iceland,Maldives,Malta,Mauritius,Micronesia,Palau,Puerto Rico,Samoa,Sao Tome & Principe,Solomon Is.,Tonga,Vanuatu,Jamaica,Brunei,West Bank,Gaza Strip,Trinidad & Tobago,St. Lucia,St. Vincent & the Grenadines,Barbados,The Bahamas"

iiasa_developed="Austria,Switzerland,Australia,Belgium,Luxembourg,Ireland,United Kingdom,Canada,Cyprus,France,Germany,Kuwait,Oman,Qatar,Saudi Arabia,United Arab Emirates,Yemen,Spain,Portugal,Israel,Italy,Japan,Netherlands,New Zealand,Denmark,Finland,Norway,Sweden,Singapore,South Korea,United States"
iiasa_developing="Albania,Bosnia & Herzegovina,Macedonia,Croatia,Greece,Slovenia,Serbia & Montenegro,Afghanistan,Algeria,Angola,Argentina,Belarus,Estonia,Latvia,Lithuania,Bangladesh,Benin,Bhutan,Botswana,Brazil,Burkina Faso,Burundi,Cameroon,Belize,Cuba,Costa Rica,Dominican Republic,El Salvador,Guatemala,Haiti,Honduras,Nicaragua,Panama,Armenia,Azerbaijan,Georgia,Central African Republic,Bulgaria,Czech Republic,Hungary,Moldova,Slovakia,Romania,Bolivia,Paraguay,Chad,Chile,China,Colombia,Congo,Congo' DRC,Djibouti,Ecuador,Egypt,Equatorial Guinea,Eritrea,Ethiopia,Gabon,The Gambia,Ghana,Guinea,Guinea-Bissau,India,Indonesia,Iran,Iraq,Cote d'Ivoire,Jordan,Kazakhstan,Kenya,Kyrgyzstan,Lebanon,Lesotho,Liberia,Libya,Madagascar,Malawi,Malaysia,Mali,Mauritania,Mexico,Mongolia,Morocco,Mozambique,Myanmar,Namibia,Nepal,Niger,Nigeria,North Korea,Guyana,Suriname,Venezuela,Pakistan,Papua New Guinea,Peru,Philippines,Poland,$iiasa_rest_of_world,Russia,Rwanda,Senegal,Sierra Leone,Somalia,South Africa,Cambodia,Laos,Sri Lanka,Sudan,Swaziland,Syria,Tajikistan,Tanzania,Thailand,Togo,Tunisia,Turkey,Turkmenistan,Uganda,Ukraine,Uruguay,Uzbekistan,Vietnam,Zambia,Zimbabwe"
iiasa_world="$iiasa_developed,$iiasa_developing"
iiasa_middle_developing="Albania,Bosnia & Herzegovina,Macedonia,Croatia,Greece,Slovenia,Serbia & Montenegro,Algeria,Angola,Argentina,Belarus,Estonia,Latvia,Lithuania,Bhutan,Botswana,Brazil,Cameroon,Belize,Cuba,Costa Rica,Dominican Republic,El Salvador,Guatemala,Haiti,Honduras,Nicaragua,Panama,Armenia,Azerbaijan,Georgia,Bulgaria,Czech Republic,Hungary,Moldova,Slovakia,Romania,Bolivia,Paraguay,Chile,China,Colombia,Congo,Djibouti,Ecuador,Egypt,The Gambia,India,Indonesia,Iran,Iraq,Cote d'Ivoire,Jordan,Kazakhstan,Lebanon,Lesotho,Libya,Malaysia,Mexico,Mongolia,Morocco,Namibia,Nigeria,Guyana,Suriname,Venezuela,Pakistan,Papua New Guinea,Peru,Philippines,Poland,$iiasa_rest_of_world,Russia,South Africa,Sri Lanka,Sudan,Swaziland,Syria,Thailand,Tunisia,Turkey,Turkmenistan,Ukraine,Uruguay"

iiasa_low_developing="Afghanistan,Bangladesh,Benin,Burkina Faso,Burundi,Central African Republic,Chad,Congo' DRC,Equatorial Guinea,Eritrea,Ethiopia,Gabon,Ghana,Guinea,Guinea-Bissau,Kenya,Kyrgyzstan,Liberia,Madagascar,Malawi,Mali,Mauritania,Mozambique,Myanmar,Nepal,Niger,North Korea,Rwanda,Senegal,Sierra Leone,Somalia,Cambodia,Laos,Tajikistan,Tanzania,Togo,Uganda,Uzbekistan,Vietnam,Zambia,Zimbabwe"






all_adb_asia_from_tolu="Afghanistan,American Samoa,Azerbaijan,Australia,Bangladesh,Armenia,Bhutan,Solomon Is.,Brunei,Myanmar,Cambodia,Sri Lanka,China,Cook Is.,Fiji,French Polynesia,Georgia,Kiribati,Guam,India,Indonesia,Iran,Japan,Kazakhstan,North Korea,South Korea,Kyrgyzstan,Laos,Malaysia,Maldives,Mongolia,Nauru,Nepal,New Caledonia,Vanuatu,New Zealand,Niue,Northern Mariana Is.,Micronesia,Marshall Is.,Palau,Pakistan,Papua New Guinea,Philippines,Russia,Singapore,Vietnam,Tajikistan,Thailand,Tonga,Turkey,Turkmenistan,Tuvalu,Uzbekistan,Samoa,Timor Leste" # missing are Macau/Macao, Hong Kong,




#################
#American Samoa,Cook Is.,Fiji,French Polynesia,Guam,Kiribati,Marshall Is.,Micronesia,Nauru,New Caledonia,Niue,Northern Mariana Is.,Palau,Papua New Guinea,Pitcairn Is.,Samoa,Solomon Is.,Tokelau,Tonga,Tuvalu,United States Minor Outlying Islands,Vanuatu,Wallis ' Futuna	xoc
#Benin,Burkina Faso,Cape Verde,The Gambia,Guinea,Guinea-Bissau,Liberia,Mali,Mauritania,Niger,Sierra Leone,Togo	xwf

# attempt based on https://www.gtap.agecon.purdue.edu/databases/regions.asp?Version=8.211
# taiwan has to be done separately
# a box for taiwan has been defined as:
#taiwan_box=\
#"
#1       Taiwan  taiwan_box@ricky_DSSAT_chad     cat
#"
gtap_country_regions_codes=\
"
Benin,Burkina Faso,The Gambia,Guinea,Guinea-Bissau,Liberia,Mali,Mauritania,Niger,Sierra Leone,Togo	xwf
Fiji,Kiribati,New Caledonia,Papua New Guinea,Vanuatu	xoc
Australia	aus
New Zealand	nzl
China	chn
Hong Kong	hkg
Japan	jpn
South Korea	kor
Mongolia	mng
Taiwan	twn
North Korea	xea
Cambodia	khm
Indonesia	idn
Laos	lao
Malaysia	mys
Philippines	phl
Singapore	sgp
Thailand	tha
Vietnam	vnm
Brunei,Myanmar,Timor Leste	xse
Bangladesh	bgd
India	ind
Nepal	npl
Pakistan	pak
Sri Lanka	lka
Afghanistan,Bhutan,Maldives	xsa
Canada	can
United States	usa
Mexico	mex
Bermuda	xna
Argentina	arg
Bolivia	bol
Brazil	bra
Chile	chl
Colombia	col
Ecuador	ecu
Paraguay	pry
Peru	per
Uruguay	ury
Venezuela	ven
French Guiana,Guyana,Suriname	xsm
Costa Rica	cri
Guatemala	gtm
Honduras	hnd
Nicaragua	nic
Panama	pan
El Salvador	slv
Belize	xca
Antigua & Barbuda,Barbados,Cuba,Dominica,Dominican Republic,Grenada,Haiti,Jamaica,Montserrat,St. Kitts & Nevis,St. Lucia,St. Vincent & the Grenadines,Trinidad & Tobago,Virgin Is.	xcb
Austria	aut
Belgium	bel
Cyprus	cyp
Czech Republic	cze
Denmark	dnk
Estonia	est
Finland	fin
France,Guadeloupe,Martinique	fra
Germany	deu
Greece	grc
Hungary	hun
Ireland	irl
Italy	ita
Latvia	lva
Lithuania	ltu
Luxembourg	lux
Malta	mlt
Netherlands	nld
Poland	pol
Portugal	prt
Slovakia	svk
Slovenia	svn
Spain	esp
Sweden	swe
United Kingdom	gbr
Switzerland	che
Norway	nor
Iceland,Liechtenstein	xef
Albania	alb
Bulgaria	bgr
Belarus	blr
Croatia	hrv
Romania	rou
Russia	rus
Ukraine	ukr
Moldova	xee
Bosnia ' Herzegovina,Macedonia,Montenegro,Serbia	xer
Kazakhstan	kaz
Kyrgyzstan	kgz
Tajikistan,Turkmenistan,Uzbekistan	xsu
Armenia	arm
Azerbaijan	aze
Georgia	geo
Bahrain	bhr
Iran	irn
Israel	isr
Kuwait	kwt
Oman	omn
Qatar	qat
Saudi Arabia	sau
Turkey	tur
United Arab Emirates	are
Iraq,Jordan,Lebanon,Syria,Yemen	xws
Egypt	egy
Morocco	mar
Tunisia	tun
Algeria,Libya	xnf
Cameroon	cmr
Cote d'Ivoire	civ
Ghana	gha
Nigeria	nga
Senegal	sen






Central African Republic,Chad,Congo,Equatorial Guinea,Gabon,Sao Tome & Principe	xcf
Angola,Congo' DRC	xac
Ethiopia	eth
Kenya	ken
Madagascar	mdg
Malawi	mwi
Mauritius	mus
Mozambique	moz
Tanzania	tza
Uganda	uga
Zambia	zmb
Zimbabwe	zwe
Burundi,Djibouti,Eritrea,Rwanda,Seychelles,Somalia,Sudan	xec
Botswana	bwa
Namibia	nam
South Africa	zaf
Lesotho,Swaziland	xsc
Rest of the World	xtw

"

