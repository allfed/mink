
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here




ktest_winterwheat_raw=\
"
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p0_winterwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p1_winterwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_winterwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat

ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p0_winterwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_p1_winterwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_winter_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_winterwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
"


ktest_winterwheat_full=\
"
`echo "$ktest_winterwheat_raw" | sed "s/ZZZZ/whK016/g"`
`echo "$ktest_winterwheat_raw" | sed "s/ZZZZ/whK076/g"`
"


ktest_springwheat_raw=\
"
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_springwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_springwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_springwheat__irrigated	ZZZZIR.SNX	winterwheat	379	wheat

ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p0_springwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_p1_springwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
ktest__gremlin_spring_wheat_deltaONpgfXXI_pgf_1995_2015_pn1_springwheat__rainfed	ZZZZRF.SNX	winterwheat	379	wheat
"

ktest_springwheat_full=\
"
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK001/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK002/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK006/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK007/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK009/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK010/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK011/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK012/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK013/g"`
`echo "$ktest_springwheat_raw" | sed "s/ZZZZ/whK015/g"`
"

ktest_wheat_full=\
"
$ktest_springwheat_full
$ktest_winterwheat_full
"

