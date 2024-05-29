# this is intended to be "sourced" in for defaults when running DSSAT

########################
### control settings ###
########################



      # nameOfWeatherExecutable=weather_generator_standalone

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
             nFakeYears=10 # 99 #100 # 

            magicSoilPrefix=HN_GEN00 # new "normal" soil with SLPF set to 0.something


             spinUpTimeDays=90 
   nPlantingWindowsPerMonth=1 
   plantingWindowLengthDays=28

    optionalHarvestInterval=366 # 700 # 366 # 460 # cassava doesn't like to do days-after-planting, so we have to brute-force it...

      nHappyPlantRunsForPhenology=$nFakeYears
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

 guess_for_weather_files_per_second=40


# shuts up mapcalc
 export GRASS_VERBOSE=0
