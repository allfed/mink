
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here

ktest_chickpeas_starter=\
"
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_chickpeas__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_chickpeas__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_chickpeas__irrigated	ZZZZIR.SNX	winterwheat	379	wheat

ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_chickpeas__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_chickpeas__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_chickpeas__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
"

ktest_chickpeas_full=\
"
`echo "$ktest_chickpeas_starter" | sed "s/ZZZZ/chA001/g"`
`echo "$ktest_chickpeas_starter" | sed "s/ZZZZ/chA002/g"`
`echo "$ktest_chickpeas_starter" | sed "s/ZZZZ/chA003/g"`
"




