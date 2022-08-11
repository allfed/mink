
# originally, i did 369 for some stupid reason even though i had moved to 379 for ftest...
# so, i am changing it here



# (DMR) removed the sample for now
# D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE  catastrophe_mink/Outdoor-crops-sample   ZZZZRF.SNX  threeSplitWithFlowering 379 maize
# D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE  control_mink/Outdoor-crops-control  ZZZZRF.SNX  threeSplitWithFlowering 379 maize
catA_maize_raw=\
"
D__1_noGCMcalendar_p0_maize__eitherN250_nonCLIMATE  control_mink/Outdoor-crops-control  ZZZZRF.SNX  threeSplitWithFlowering 379 maize
"




catA_maize_full=\
"
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK023/g"`
"

all_the_rest="
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK013/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK014/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK015/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK016/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK017/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK018/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK021/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK024/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK023/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK025/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK026/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK027/g"`
`echo "$catA_maize_raw" | sed "s/ZZZZ/mzK029/g"`

"



