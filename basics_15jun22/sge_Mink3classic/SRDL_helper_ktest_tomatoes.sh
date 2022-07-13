
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here





ktest_tomatoes_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_tomatoes__irrigated	ZZZZIR.SNX	allAtPlanting	379	tomatoes
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_tomatoes__irrigated	ZZZZIR.SNX	allAtPlanting	379	tomatoes
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_tomatoes__irrigated	ZZZZIR.SNX	allAtPlanting	379	tomatoes

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_tomatoes__rainfed	ZZZZRF.SNX	allAtPlanting	379	tomatoes
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_tomatoes__rainfed	ZZZZRF.SNX	allAtPlanting	379	tomatoes
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_tomatoes__rainfed	ZZZZRF.SNX	allAtPlanting	379	tomatoes
"


ktest_tomatoes_full=\
"
`echo "$ktest_tomatoes_raw" | sed "s/ZZZZ/tmA001/g"`
`echo "$ktest_tomatoes_raw" | sed "s/ZZZZ/tmA002/g"`
`echo "$ktest_tomatoes_raw" | sed "s/ZZZZ/tmA003/g"`
`echo "$ktest_tomatoes_raw" | sed "s/ZZZZ/tmA004/g"`
"


