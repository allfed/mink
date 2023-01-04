
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here



maine_maize_raw=\
"

mainedaily__1_noGCMcalendar_p0_maize__eitherN125_nonCLIMATE	ukesm1_ssp585_2001_2100/ukesm1_ssp585_2001_2100	ZZZZRF.SNX	threeSplitWithFlowering	379	maize
mainedaily__1_noGCMcalendar_p0_maize__eitherN125_nonCLIMATE	ukesm1_ssp585_2001_2100/ukesm1_ssp585_2001_2100	ZZZZIR.SNX	threeSplitWithFlowering	379	maize

"


maine_maize_full=\
"
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK023/g"`
"

all_the_rest="
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK013/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK014/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK015/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK016/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK017/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK018/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK021/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK024/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK023/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK025/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK026/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK027/g"`
`echo "$maine_maize_raw" | sed "s/ZZZZ/mzK029/g"`

"



