# this is intended to be "sourced" in for defaults when running DSSAT

########################
### control settings ###
########################

#      nameOfDSSATExecutable=dscsm044.exe # heat tolerant
#      nameOfDSSATExecutable=dscsm045.exe
#      nameOfDSSATExecutable=dscsm046.exe # oryza2000 integrated

#      nameOfDSSATExecutable=dscsm046.exe # use this for durianA

#      nameOfDSSATExecutable=dscsm047.exe
#      nameOfDSSATExecutable=./dscsm047_fast_initfix.exe
#      nameOfDSSATExecutable=./dscsm047_fast_initfix_onedir.exe # tried compiling with the "simple" stuff in a single directory i hopes
#                                                               # of eliminating that weird hanging, but no such luck...
#      nameOfDSSATExecutable=./dscsm047_fast_forceharvest.exe # for SimpleB
#      nameOfDSSATExecutable=./dscsm_compiled_aflatoxins_debug.exe

#      nameOfDSSATExecutable=./dscsm_compiled_aflatoxins_fast.exe # USE THIS ONE MOST OF THE TIME!!!

#       nameOfDSSATExecutable=dscsm_compiled_fast.exe # for 4.7.5.11


      nameOfDSSATExecutable=./dscsm_compiled_fast.exe # goes with actual_program_4.7.5.11
#      nameOfDSSATExecutable=./dscsm046.exe            # goes with actual_program_4.6_whpt_08jul16


echo "   >>> using $nameOfDSSATExecutable <<<"

      nameOfWeatherExecutable=weather_generator_standalone

                             # multiplier to convert from shortwave units in the GIS to the units needed by DSSAT [MJ/m^2/day]
                             # the stuff from GLDAS is in W/m^2; so we have to multiply by the number of seconds in a day
                             # and divide by a million
#               SWmultiplier=`echo "scale = 10; 3600 * 24 / 1000000" | bc` # old conversion from hydro style SW
               SWmultiplier=1 # for thornton's properly scaled stuff

            firstRandomSeed=1 # 10217 # 10214
          nFakeYears=30 # toothwort=45 # 20 # <-- alfalfa egypt becuase years 25+ get screwed up; --> egypt 40 # durian 20 # etest=30 # zambia = 320 # potatoes response surface 200 # dtest=40 # RISKYB=64 # 50 # ngs thing: 10 # 320 # CTEST = 40 # BTEST=100 #mdbig new = 567 # 30 # drought thing 500 # MD_ = 150 # soil search = 40 # bite wheat = 100 # searching = 30 ; 80 # sensitivity = 100 # ncan30 = 80 # can15 = 40 # 90 ; ncan15 = 80
                           #  magicSoilPrefix=HC_GEN00 # classic HC27
                           #  magicSoilPrefix=H2_GEN00 # jawoo's slightly modified HC27




#            magicSoilPrefix=RR_ # the WISE1.1 renumbered sequentially

#            magicSoilPrefix=AA_GEN00 # a 20% increase in available water by raising the upper bound; no srgf change
#            magicSoilPrefix=AC_GEN00 # a 20% increase in available water by spreading the upper and lower symmetrically; no srgf change

#            magicSoilPrefix=AD_GEN00 # a 20% DEcrease in available water by dropping the lower bound; no srgf change
#            magicSoilPrefix=AE_GEN00 # a 10% DEcrease in available water by dropping the lower bound; no srgf change
#            magicSoilPrefix=HN_GEN00 # new "normal" soil with SLPF set to 0.something
#            magicSoilPrefix=AF_GEN00 # a 10% increase in available water by dropping the lower bound; no srgf change
#            magicSoilPrefix=AB_GEN00 # a 20% increase in available water by dropping the lower bound; no srgf change
#            magicSoilPrefix=AG_GEN00 # a 30% increase in available water by dropping the lower bound; no srgf change

#            magicSoilPrefix=PA_GEN00 # a 20% increase in available water by dropping the lower bound ALONG WITH 20% thicker layers and 50% more organic carbon; that is, paleolithic conditions in the mediterranean...

            magicSoilPrefix=HN_GEN00 # new "normal" soil with SLPF set to 0.something
#            magicSoilPrefix=HD_GEN00 # new "drought-tolerance via roots" soil; some srgf changes; 5% increase in available water by dropping the lower bound

#            magicSoilPrefix=XC_GEN00 # variant with higher carbon
#            magicSoilPrefix=XD_GEN00 # variant with greater depth
#            magicSoilPrefix=XT_GEN00 # variant with greater texture

#            magicSoilPrefix=NC_GEN00 # variant with lower carbon
#            magicSoilPrefix=ND_GEN00 # variant with lower depth
#            magicSoilPrefix=NT_GEN00 # variant with lower texture

                           #  magicSoilPrefix=HM_GEN00 # myriam's recommendations
                           #  magicSoilPrefix=HC_GEN00 # the original, classic flavor



#           fakePlantingYear=50
             spinUpTimeDays=90 # a good value is: 90 # 3/15/30/90/150/366 # 90 # 90 # 45
   nPlantingWindowsPerMonth=1 # potato response surface = 2
#   plantingWindowLengthDays=135  # 70
   plantingWindowLengthDays=3 # 30  # 70

 # this lsets you move the entire planting schme around by days...
#  plantingDateInMonthShiftInDays=0 # 0


#    optionalHarvestInterval=279 # dunno, grass? 731 #trying 210 for grasses # 365 # 366 # 360  # 366 # 460 # cassava doesn't like to do days-after-planting, so we have to brute-force it...
    optionalHarvestInterval=`echo "8 * 30" | bc` # normal boring crops like maize




# for sensitivity analysis: use 100 (i.e., same as number of repetitions); for ncan30/wcan30 use 5
#      nHappyPlantRunsForPhenology=20 # soil search = 40 # bite wheat = 100 # 100 # 9 # 80 # ncan30/wcan30 is 5
      nHappyPlantRunsForPhenology=$nFakeYears # soil search = 40 # bite wheat = 100 # 100 # 9 # 80 # ncan30/wcan30 is 5
  happyYieldThresholdToDoRealRuns=-1 # -1 # keep them all # 1 # 50 # 350
            phenologyBufferInDays=-5
  happyMaturityThresholdToDoRealRuns=`echo "$optionalHarvestInterval + 1" | bc` # 300 # 200

# fractionBetweenLowerLimitAndDrainedUpperLimit=0.01 # 0.25 is the CAN15 value
 fractionBetweenLowerLimitAndDrainedUpperLimit=0.25 # 0.25 is the CAN15 value
# THIS IS THE RIGHT ONE!!!! fractionBetweenLowerLimitAndDrainedUpperLimit=0.25 # 0.25 is the CAN15 value
# fractionBetweenLowerLimitAndDrainedUpperLimit=0.75 # 0.25 is the CAN15 value
# fractionBetweenLowerLimitAndDrainedUpperLimit=0.99 # 0.25 is the CAN15 value




        depthForNitrogen=40    # cm depth over which to distribute the initial soil nitrogen
  residueNitrogenPercent=0.6   # percent nitrogen content of initial crop residues left on the field
       incorporationRate=100   # percent incorporation of the initial crop residues
      incorporationDepth=5     # cm depth for incorporation of the initial crop residues


#  clayLoamSandStableCarbonRatesFilename=${BASE}/min_StableCarbonTable.txt # lower stable fractions # file containing the fraction of stable carbon in each layer by texture
  clayLoamSandStableCarbonRatesFilename=${BASE}/StableCarbonTable.txt     # the best guess # file containing the fraction of stable carbon in each layer by texture
#  clayLoamSandStableCarbonRatesFilename=${BASE}/max_StableCarbonTable.txt # higher stable fractions# file containing the fraction of stable carbon in each layer by texture



# here are some settings for the thing that attempts to cut off execution if it hangs...
       maxRunTime=400 # 1000 # 2000 # 31000 #7000 # 21000 # milliseconds
 bumpUpMultiplier=2
testIntervalToUse=100 # milliseconds
 rerunAttemptsMax=2 # 3 #4


