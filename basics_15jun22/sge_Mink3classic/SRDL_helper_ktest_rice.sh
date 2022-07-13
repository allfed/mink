
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here


ktest_rice_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_rice__irrigated	ZZZZIR.SNX	rice	379	rice
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_rice__irrigated	ZZZZIR.SNX	rice	379	rice
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_rice__irrigated	ZZZZIR.SNX	rice	379	rice

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_rice__rainfed	ZZZZRF.SNX	rice	379	rice
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_rice__rainfed	ZZZZRF.SNX	rice	379	rice
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_rice__rainfed	ZZZZRF.SNX	rice	379	rice
"

ktest_rice_full=\
"
`echo "$ktest_rice_raw" | sed "s/ZZZZ/riK001/g"`
`echo "$ktest_rice_raw" | sed "s/ZZZZ/riK002/g"`
"






