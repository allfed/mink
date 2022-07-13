
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here





ktest_sunflowers_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_sunflowers__irrigated	ZZZZIR.SNX	cotton	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_sunflowers__irrigated	ZZZZIR.SNX	cotton	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_sunflowers__irrigated	ZZZZIR.SNX	cotton	379	maize

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_sunflowers__rainfed	ZZZZRF.SNX	cotton	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_sunflowers__rainfed	ZZZZRF.SNX	cotton	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_sunflowers__rainfed	ZZZZRF.SNX	cotton	379	maize
"


ktest_sunflowers_full=\
"
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA001/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA002/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA003/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA004/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA005/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA006/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA007/g"`
`echo "$ktest_sunflowers_raw" | sed "s/ZZZZ/suA008/g"`
"


