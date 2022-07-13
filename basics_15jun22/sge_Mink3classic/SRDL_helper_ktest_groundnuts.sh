
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here


ktest_groundnuts_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_groundnuts__irrigated	ZZZZIR.SNX	allAtPlanting	379	groundnuts
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_groundnuts__irrigated	ZZZZIR.SNX	allAtPlanting	379	groundnuts
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_groundnuts__irrigated	ZZZZIR.SNX	allAtPlanting	379	groundnuts

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_groundnuts__rainfed	ZZZZRF.SNX	allAtPlanting	379	groundnuts
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_groundnuts__rainfed	ZZZZRF.SNX	allAtPlanting	379	groundnuts
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_groundnuts__rainfed	ZZZZRF.SNX	allAtPlanting	379	groundnuts
"

ktest_groundnuts_full=\
"
`echo "$ktest_groundnuts_raw" | sed "s/ZZZZ/grK001/g"`
`echo "$ktest_groundnuts_raw" | sed "s/ZZZZ/grK002/g"`
`echo "$ktest_groundnuts_raw" | sed "s/ZZZZ/grK003/g"`
`echo "$ktest_groundnuts_raw" | sed "s/ZZZZ/grK004/g"`
`echo "$ktest_groundnuts_raw" | sed "s/ZZZZ/grK005/g"`
"






