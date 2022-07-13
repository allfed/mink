
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here

ktest_greenbeans_starter=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_greenbeans__irrigated	ZZZZIR.SNX	potatoes	379	wheat
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_greenbeans__irrigated	ZZZZIR.SNX	potatoes	379	wheat
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_greenbeans__irrigated	ZZZZIR.SNX	potatoes	379	wheat

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_greenbeans__rainfed	ZZZZRF.SNX	potatoes	379	wheat
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_greenbeans__rainfed	ZZZZRF.SNX	potatoes	379	wheat
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_greenbeans__rainfed	ZZZZRF.SNX	potatoes	379	wheat

"

ktest_greenbeans_full=\
"
`echo "$ktest_greenbeans_starter" | sed "s/ZZZZ/gbA001/g"`
"




