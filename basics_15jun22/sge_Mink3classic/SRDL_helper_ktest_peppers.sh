
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here





ktest_peppers_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_peppers__irrigated	ZZZZIR.SNX	allAtPlanting	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_peppers__irrigated	ZZZZIR.SNX	allAtPlanting	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_peppers__irrigated	ZZZZIR.SNX	allAtPlanting	379	maize

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_peppers__rainfed	ZZZZRF.SNX	allAtPlanting	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_peppers__rainfed	ZZZZRF.SNX	allAtPlanting	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_peppers__rainfed	ZZZZRF.SNX	allAtPlanting	379	maize

"


ktest_peppers_full=\
"
`echo "$ktest_peppers_raw" | sed "s/ZZZZ/prA001/g"`
`echo "$ktest_peppers_raw" | sed "s/ZZZZ/prA002/g"`
"


