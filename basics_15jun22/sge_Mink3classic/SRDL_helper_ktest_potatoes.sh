
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here



ktest_potatoes_raw=\
"
ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_p0_potatoes__irrigated	ZZZZIR.SNX	potatoes2	379	potatoes
ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_p1_potatoes__irrigated	ZZZZIR.SNX	potatoes2	379	potatoes
ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_pn1_potatoes__irrigated	ZZZZIR.SNX	potatoes2	379	potatoes

ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_p0_potatoes__rainfed	ZZZZRF.SNX	potatoes2	379	potatoes
ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_p1_potatoes__rainfed	ZZZZRF.SNX	potatoes2	379	potatoes
ktest__growncoarse_potato_season_onset_full_24jun14_grown_deltaONpgfXXI_pgf_1995_2015_pn1_potatoes__rainfed	ZZZZRF.SNX	potatoes2	379	potatoes
"

# i should have been using the "ptH" set which has revised sprout lengths and depths
# along with revised harvest intervals...

ktest_potatoes_full=\
"
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH001/g"`
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH002/g"`
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH003/g"`
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH004/g"`
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH007/g"`
`echo "$ktest_potatoes_raw" | sed "s/ZZZZ/ptH008/g"`
"


