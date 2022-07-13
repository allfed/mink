
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here



ktest_soybeans_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_soybeans__irrigated	ZZZZIR.SNX	allAtPlanting	379	soybeans
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_soybeans__irrigated	ZZZZIR.SNX	allAtPlanting	379	soybeans
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_soybeans__irrigated	ZZZZIR.SNX	allAtPlanting	379	soybeans

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_soybeans__rainfed	ZZZZRF.SNX	allAtPlanting	379	soybeans
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_soybeans__rainfed	ZZZZRF.SNX	allAtPlanting	379	soybeans
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_soybeans__rainfed	ZZZZRF.SNX	allAtPlanting	379	soybeans

"



ktest_soybeans_full=\
"
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK001/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK002/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK003/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK004/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK005/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK006/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK007/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK008/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK009/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK010/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK011/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK012/g"`
`echo "$ktest_soybeans_raw" | sed "s/ZZZZ/sbK013/g"`
"


