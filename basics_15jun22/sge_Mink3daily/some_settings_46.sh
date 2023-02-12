# this is intended to be "sourced" in for defaults when running DSSAT

########################
### control settings ###
########################

#      nameOfDSSATExecutable=dscsm044.exe # heat tolerant
#      nameOfDSSATExecutable=dscsm045.exe
#      nameOfDSSATExecutable=dscsm046.exe # oryza2000 integrated

#      nameOfDSSATExecutable=dscsm046.exe

#      nameOfDSSATExecutable=./dscsm_compiled_aflatoxins_fast.exe
#      nameOfDSSATExecutable=./dscsm_compiled_pnmz_fast.exe # the correct one to use for aflatoxins


      nameOfDSSATExecutable=dscsm_compiled_fast.exe # actual_program_4.7.5.11



echo "   >>> using $nameOfDSSATExecutable <<<"


      nameOfWeatherExecutable=weather_generator_standalone

                             # multiplier to convert from shortwave units in the GIS to the units needed by DSSAT [MJ/m^2/day]
                             # the stuff from GLDAS is in W/m^2; so we have to multiply by the number of seconds in a day
                             # and divide by a million
               SWmultiplier=`echo "scale = 10; 3600 * 24 / 1000000" | bc` # old conversion from hydro style SW
#echo "when using the AR6 first light stuff (summer 2020), i forgot to rescale the srad, so here we are, back to where we started...."
#               SWmultiplier=1 # for thornton's properly scaled stuff


  allFlag=true

  allExtraToRecordCSV=""
#  allExtraToRecordCSV="CWAM,SRADA,TMINA,TMAXA,PRCP"

#  allExtraToRecordCSV="HWAM,IRCM,PRCM"
#  allExtraToRecordCSV="HWAM,SRADA,TMINA,TMAXA,PRCP,"
#  allExtraToRecordCSV="IRCM"
#  allExtraToRecordCSV="PRCM,IRCM"
#  allExtraToRecordCSV="AFCONC,PCTINF"
#  allExtraToRecordCSV="PRCP,TMAXA"


# do we want to keep some useful stuff that is usually superflous
    keepHappyYields=true;
    keepRealDaysToEmergence=true;
    keepRealDaysToAnthesis=true;
    keepRealDaysToMaturity=true;



#  weatherDataSuffixWithDot=.csv
  weatherDataSuffixWithDot=.WTH


            firstRandomSeed=1 

#       # for the first catastrophe daily weather (4nov21)
       fakePlantingYear=2 # 1 is the first year, so to allow for spinup, we need to start in #2
             nFakeYears=8 # 99 #100 # 


#     export fakePlantingYear=`echo "$giant_prefix" | cut -d_ -f1 | cut -c3-4`
#fakePlantingYear=56


#       fakePlantingYear=5 # for enso reshuffled
#             nFakeYears=46 # for enso reshuffled ones
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

#            magicSoilPrefix=PA_GEN00 # a 20% increase in available water by dropping the lower bound ALONG WITH 20% thicker layers and 50% more organic carbon



            magicSoilPrefix=HN_GEN00 # new "normal" soil with SLPF set to 0.something
#            magicSoilPrefix=HD_GEN00 # new "drought-tolerance via roots" soil

#            magicSoilPrefix=XC_GEN00 # variant with higher carbon
#            magicSoilPrefix=XD_GEN00 # variant with greater depth
#            magicSoilPrefix=XT_GEN00 # variant with greater texture

#            magicSoilPrefix=NC_GEN00 # variant with lower carbon
#            magicSoilPrefix=ND_GEN00 # variant with lower depth
#            magicSoilPrefix=NT_GEN00 # variant with lower texture

                           #  magicSoilPrefix=HM_GEN00 # myriam's recommendations
                           #  magicSoilPrefix=HC_GEN00 # the original, classic flavor




             spinUpTimeDays=90 # 90 # 90 # 45
   nPlantingWindowsPerMonth=1 # potato response surface = 2
   plantingWindowLengthDays=3 # 30  # 70

 # this lets you move the entire planting schme around by days...
#  plantingDateInMonthShiftInDays=0 # 0


    optionalHarvestInterval=366 # 700 # 366 # 460 # cassava doesn't like to do days-after-planting, so we have to brute-force it...



# for sensitivity analysis: use 100 (i.e., same as number of repetitions); for ncan30/wcan30 use 5
#      nHappyPlantRunsForPhenology=20 # soil search = 40 # bite wheat = 100 # 100 # 9 # 80 # ncan30/wcan30 is 5
      nHappyPlantRunsForPhenology=$nFakeYears # soil search = 40 # bite wheat = 100 # 100 # 9 # 80 # ncan30/wcan30 is 5
  happyYieldThresholdToDoRealRuns=5 # 50 # 350
            phenologyBufferInDays=-5
  happyMaturityThresholdToDoRealRuns=`echo "$optionalHarvestInterval + 1" | bc` # 300 # 200

 fractionBetweenLowerLimitAndDrainedUpperLimit=0.25 # 0.25 is the CAN15 value




        depthForNitrogen=40    # cm depth over which to distribute the initial soil nitrogen
  residueNitrogenPercent=0.6   # percent nitrogen content of initial crop residues left on the field
       incorporationRate=100   # percent incorporation of the initial crop residues
      incorporationDepth=5     # cm depth for incorporation of the initial crop residues


  clayLoamSandStableCarbonRatesFilename=${BASE}StableCarbonTable.txt     # the best guess # file containing the fraction of stable carbon in each layer by texture
#  clayLoamSandStableCarbonRatesFilename=${BASE}/max_StableCarbonTable.txt # higher stable fractions# file containing the fraction of stable carbon in each layer by texture




# here are some settings for the thing that attempts to cut off execution if it hangs...
       maxRunTime=5500 #7000 # 21000 # milliseconds
 bumpUpMultiplier=2.0
testIntervalToUse=100 # milliseconds
 rerunAttemptsMax=2 #4




 guess_for_weather_files_per_second=40 # 35 # 5000000000 # 100 # 41 #55 # 30






