#!/usr/bin/env bash
echo ""
echo ""
echo ""
echo ""
echo "BEGGINNINING TEST!!!!"
echo ""
echo ""
echo ""
set -euo pipefail
#!/usr/bin/env bash
##############################################################################
# Manual checks for year-2 rasters                                            #
#  • overall(IRRF) between IR and RF                                          #
#  • overall order-independent (IRRF vs RFIR)                                 #
#  • yearspecific ≥ avgbest   (IR and RF)                                     #
#  • overall == yearspecific  (IR, RF, IRRF, RFIR)                            #
##############################################################################

set -euo pipefail

#------------------------ EDIT THESE 3 LINES --------------------------------
STEM_BASE="400_weather_150TgBC_mink_BestYield_noGCMcalendar_p0_rice__Apr11_CheckingRice_bug2_riCL00IF"
SUFFIX="wet"            # part that precedes irrigation tag in IR/RF rasters
YEAR="y2"
#----------------------------------------------------------------------------

# raster-name helpers
R_overall () { echo "${STEM_BASE}_${1}_${SUFFIX}_overall_yield_${YEAR}"; }     # TAG in middle
R_yearspec() { echo "${STEM_BASE}_${1}_${SUFFIX}${1}_yearspecific_${YEAR}"; }        # TAG after SUFFIX
R_avgbest () { echo "${STEM_BASE}_${1}_${SUFFIX}${1}_stablemonth_${YEAR}_using_avgbest_month"; }

mean_of ()  { r.univar -g "$1" | awk -F= '$1=="mean"{print $2}'; }

echo -e "\n==========  checks for $YEAR  ==========\n"
echo "in numbers below, irrf should be: ir > irrf = rfir > rf numerically"
############################ 1. overall in-between ###########################
mIR=$(   mean_of "$(R_overall IR)"   )
mRF=$(   mean_of "$(R_overall RF)"   )
mIRRF=$( mean_of "$(R_overall IRRF)" )
mRFIR=$( mean_of "$(R_overall RFIR)" )

echo ""
echo ""
echo "---- overall_yield means ----"
printf "  IR   : %8.2f\n  RF   : %8.2f\n  IRRF : %8.2f\n  RFIR : %8.2f\n" "$mIR" "$mRF" "$mIRRF"  "$mRFIR"
echo "Expect IRRF between IR and RF.\n"

###################### 2. overall order-independence #########################
echo ""
echo ""
echo "---- overall_yield: IRRF – RFIR (should be zero) ----"
r1=$(R_overall IRRF); r2=$(R_overall RFIR)
r.mapcalc "neg_$r2 = - $r2"
r.series --overwrite input="$r1,neg_$r2" output=diff_order_yield method=sum
r.univar -g diff_order_yield
g.remove -f rast=neg_"$r2",diff_order_yield >/dev/null
echo

################# 3. yearspecific ≥ avgbest  (IR & RF only) ##################
for tag in IR RF; do
  ys=$(R_yearspec "$tag")
  ab=$(R_avgbest "$tag")
  echo ""
  echo ""
  echo "---- $tag : yearspecific - avgbest (min >= 0) ----"
  # r.mapcalc "diff_yb = $ys - $ab"
  echo ab
  echo $ab
  echo 1
  r.mapcalc "neg_$ab = - $ab"
  echo 2
  r.series --overwrite input="$ys,neg_$ab" output=diff_yb method=sum

  r.univar -g diff_yb
  echo
done

########## 4. overall == yearspecific  (all four scenario tags) ##############
for tag in IR RF; do
  ov=$(R_overall "$tag")
  ys=$(R_yearspec "$tag")
  echo ""
  echo ""
  echo "---- $tag : overall - yearspecific (should be zero) ----"
  # r.mapcalc "diff_oy = $ov - $ys"
  r.mapcalc "neg_$ys = - $ys"
  r.series --overwrite input="$ov,neg_$ys" output=diff_oy method=sum

  r.univar -g diff_oy
  echo
done

echo "================  END – inspect the stats above  ================"
