
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here

ktest_barley_starter=\
"
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_springbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_springbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_springbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat

ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p0_winterbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p1_winterbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_winterbarley__irrigated	ZZZZIR.SNX	winterwheat	379	wheat



ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_springbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_springbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_springbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat

ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p0_winterbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p1_winterbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_winterbarley__rainfed	ZZZZRF.SNX	winterwheat	379	wheat



"

ktest_barley_full=\
"
`echo "$ktest_barley_starter" | sed "s/ZZZZ/baA001/g"`
`echo "$ktest_barley_starter" | sed "s/ZZZZ/baA002/g"`
`echo "$ktest_barley_starter" | sed "s/ZZZZ/baA003/g"`
"




