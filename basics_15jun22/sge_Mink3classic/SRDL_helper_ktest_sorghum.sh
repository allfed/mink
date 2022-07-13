
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here





ktest_sorghum_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_sorghum__irrigated	ZZZZIR.SNX	allAtPlanting	379	sorghum
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_sorghum__irrigated	ZZZZIR.SNX	allAtPlanting	379	sorghum
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_sorghum__irrigated	ZZZZIR.SNX	allAtPlanting	379	sorghum

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_sorghum__rainfed	ZZZZRF.SNX	allAtPlanting	379	sorghum
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_sorghum__rainfed	ZZZZRF.SNX	allAtPlanting	379	sorghum
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_sorghum__rainfed	ZZZZRF.SNX	allAtPlanting	379	sorghum
"


ktest_sorghum_full=\
"
`echo "$ktest_sorghum_raw" | sed "s/ZZZZ/sgK001/g"`
`echo "$ktest_sorghum_raw" | sed "s/ZZZZ/sgK002/g"`
`echo "$ktest_sorghum_raw" | sed "s/ZZZZ/sgK003/g"`
"


