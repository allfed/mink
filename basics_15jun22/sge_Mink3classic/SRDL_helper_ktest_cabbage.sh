
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here





ktest_cabbage_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_cabbage__irrigated	ZZZZIR.SNX	potatoes	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_cabbage__irrigated	ZZZZIR.SNX	potatoes	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_cabbage__irrigated	ZZZZIR.SNX	potatoes	379	maize

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_cabbage__rainfed	ZZZZRF.SNX	potatoes	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_cabbage__rainfed	ZZZZRF.SNX	potatoes	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_cabbage__rainfed	ZZZZRF.SNX	potatoes	379	maize
"


ktest_cabbage_full=\
"
`echo "$ktest_cabbage_raw" | sed "s/ZZZZ/cbA001/g"`
`echo "$ktest_cabbage_raw" | sed "s/ZZZZ/cbA002/g"`
"


