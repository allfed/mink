
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here



ktest_maize_raw=\
"
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_maize__irrigated	ZZZZIR.SNX	threeSplitWithFlowering	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_maize__irrigated	ZZZZIR.SNX	threeSplitWithFlowering	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_maize__irrigated	ZZZZIR.SNX	threeSplitWithFlowering	379	maize

ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p0_maize__rainfed	ZZZZRF.SNX	threeSplitWithFlowering	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_p1_maize__rainfed	ZZZZRF.SNX	threeSplitWithFlowering	379	maize
ktest__gremlin_irrigated_deltaONpgfXXI_pgf_1995_2015_pn1_maize__rainfed	ZZZZRF.SNX	threeSplitWithFlowering	379	maize
"

ktest_maize_full=\
"
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK013/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK014/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK015/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK016/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK017/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK018/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK021/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK023/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK024/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK025/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK026/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK027/g"`
`echo "$ktest_maize_raw" | sed "s/ZZZZ/mzK029/g"`
"



