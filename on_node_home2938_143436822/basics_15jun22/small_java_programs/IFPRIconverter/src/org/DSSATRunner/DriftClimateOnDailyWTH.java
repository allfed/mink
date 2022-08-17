package org.DSSATRunner;

//import java.io.*;

import org.R2Useful.*;
import org.DSSATRunner.DSSATHelperMethods;

public class DriftClimateOnDailyWTH {

	// NEW: we want to apply climate drift according to a comparison between a baseline and future climate and slap
	// that on top of the daily weather.
	//
	// then we will have to adjust the tamp and tav in hopes of keeping it reasonable
	//
	// we with *not* renumber the years nor do monthly aggregation since i want to keep that in a separate step
	// to keep everything consistent....
	// this is being done starting on 15aug14.
	

	public static void main(String commandLineOptions[]) throws Exception {

		////////////////////
		// magical things //
		////////////////////

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

//		String pixelListFile         =                  commandLineOptions[0];
		
		String sourceDirectory       =                  commandLineOptions[0];
		String sourcePrefix          =                  commandLineOptions[1];
		String sourceSuffix          =                  commandLineOptions[2];
		String destinationDirectory  =                  commandLineOptions[3];
		String destinationPrefix     =                  commandLineOptions[4];
		String destinationSuffix     =                  commandLineOptions[5];
		String climateTransitionBaseName =              commandLineOptions[6];
		int    nJanuariesForSpinup = Integer.parseInt(commandLineOptions[7]);
		int    baselineClimateYear   = Integer.parseInt(commandLineOptions[8]);
		int    futureClimateYear     = Integer.parseInt(commandLineOptions[9]);
		int    yearToAssumeAfterSpinup     = Integer.parseInt(commandLineOptions[10]);


		// magic numbers for spacing
		final int magicFirstLineToWorkOn = 5;
		final int magicLengthOfDate = 5;
		final int shortwaveEnd = 11;
		final int tmaxEnd = 17;
		final int tminEnd = 23;
		final int rainEnd = 29;
		final int longwaveStart = 41;

		final int nMonths = 12; // this actually needs to be twelve
		
		final int nTotalChecks = 5000;

		final int nValuesPerMonth = 8; // for the input data
		
		// magic numbers for thresholds to keep it real
		final double maximumAcceptableShortwave = 29.8; // MJ/m^2/day
		final double maximumAcceptableRain = 600.0; // mm/day ; quasi-max in Baseline_NT is 493.2 mm
		final double magicTinyDailyRainEquivalentToZero = 0.0; // mm/month 
		final double magicTinyMonthlyRainEquivalentToZero = 0.0; // mm/month 

		final double magicMinimumTemperatureDifferenceNeeded = 0.1;

		
		
		/// initializations
		int originalDate;

		String thisDatePart;
//		String restOfLine;
		int yearToUse, dayToUse;
		int thisMonthIndex, dayOfTheMonthIndex;


		// the geographic info
		double latitudeToPullFrom, longitudeToPullFrom;
		
		// the data stuff
//		double tavStated, ampStated;

		double shortwaveHere, tmaxHere, tminHere, rainHere, longwaveHere;
		double shortwaveToUse = Double.POSITIVE_INFINITY, tmaxToUse, tminToUse, rainToUse = Double.NEGATIVE_INFINITY;
		double temperatureSwapSpot;

		double shortwaveBase, tmaxBase, tminBase, rainBase;
		double shortwaveFuture, tmaxFuture, tminFuture, rainFuture;

		double magicDayForDelugeThisMonthAsDouble;

		int magicDayIndexForDelugeThisMonth;

		
		double temperatureSum, hottestMonthValue, coldestMonthValue, thisMonthAverageTemperature, overallAverageAnnualTemperature, temperatureFullAmplitude;;



		String thisInputFile = null;
		String thisOutputFile = null;
		String[] originalAsArray = null;
		String[] dailyOutputAsArray = null;

		
		String statusString = null;

		
		
		
		
		
		// set up the monthly accumulators for later use in tamp and tav
		DescriptiveStatisticsUtility[] monthlyTmax      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyTmin      = new DescriptiveStatisticsUtility[nMonths];
		
		for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
			monthlyTmax[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyTmin[monthIndex]      = new DescriptiveStatisticsUtility(true);
		}


/*
*WEATHER DATA :

@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
  RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
90274   9.7   4.0   3.1   1.5  -999  -999
90275  12.5   5.4   1.8   1.9  -999  -999
*/

		// read in the fundamental data
		// Beware the MAGIC NUMBER!!! gonna force these into memory.
		System.out.println("-- remember: the lat/long in the geog file needs to match the weather file names... --");
		System.out.println("-- no idiot checking is being done except general failure... --");
		int formatIndexToForce = 1;
		// the data file will be assumed to be base_srad / base_tmin / base_tmax / base_rain / future_srad / future_tmin / future_tmax / future_rain
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(climateTransitionBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(climateTransitionBaseName + "_geog",formatIndexToForce);

		int nValidPixels = (int) dataMatrix.getDimensions()[0];

		
		//////////////////////////////////////////////////////////////
		// up here, we will need to put in the BEGINNING OF A FOR LOOP
		//////////////////////////////////////////////////////////////

		// ponder how many years the transition should occur over
		int nYearsForTransition = futureClimateYear - baselineClimateYear;
		
		long startTimeMillis = System.currentTimeMillis(); // just for status updates for impatient users

//		int yearIndexAfterStart;
		double linearMixingCoefficient = Double.NaN;
		double weightForBaseline = Double.NaN;
		double weightForFuture = Double.NaN;
		
		double rawFractionalChangeShortwave = -1;
		double rawFractionalChangeRain = -1;

		boolean rainHasFallenOnThisDayOrBefore = false;
		
//		double mixingMultiplier = Double.NaN;

		int nNewYearsSinceBeginningOfWeatherFile = 0;
		for (int pixelIndex = 0; pixelIndex < nValidPixels; pixelIndex++) {
			// build up the input file name....
			// grab the latitude/longitude/idnumber from the list tingju gave me....

			latitudeToPullFrom = geogMatrix.getValue(pixelIndex,2); // Beware the MAGIC NUMBER!!!
			longitudeToPullFrom = geogMatrix.getValue(pixelIndex,3); // Beware the MAGIC NUMBER!!!

			thisInputFile  = sourceDirectory      + sourcePrefix      + "_" + latitudeToPullFrom + "_" + longitudeToPullFrom + sourceSuffix; 
			thisOutputFile = destinationDirectory + destinationPrefix + "_" + latitudeToPullFrom + "_" + longitudeToPullFrom + destinationSuffix; 

			// read in the raw data
			originalAsArray = FunTricks.readTextFileToArray(thisInputFile);
			dailyOutputAsArray = new String[originalAsArray.length];

			// reset the monthly accumulators and monthly output lines...

			for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
				monthlyTmax[monthIndex].reset();
				monthlyTmin[monthIndex].reset();
			}


			// do the first few header lines
			dailyOutputAsArray[0] = originalAsArray[0];
			dailyOutputAsArray[1] = originalAsArray[1];
			dailyOutputAsArray[2] = originalAsArray[2];
			// we will come back and fix up index 3 with the new amp and tav at the end... 
			dailyOutputAsArray[3] = originalAsArray[3];
			dailyOutputAsArray[4] = originalAsArray[4];


			// the plan is to preserve the original year/day numbers for the moment. the only question
			// is whether we are starting on january 1 or not. and we want to skip a few years for potential spinup...

//			yearIndexAfterStart = 0;
			nNewYearsSinceBeginningOfWeatherFile = 0;
			for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {

				// we don't care about the date, so just figure everything else out....
				thisDatePart = originalAsArray[lineIndex].substring(0, magicLengthOfDate);
				originalDate = Integer.parseInt(thisDatePart.trim());
				// the date is YYddd, so if we do integer division by a thousand, we get the year...
				yearToUse = originalDate / 1000;

				// subtract off the year to get the days...
				dayToUse = originalDate - 1000 * yearToUse;
				
				// we need to fix this up for leap years...
				if (yearToUse % 4 != 0) {
					// normal year....
					thisMonthIndex = DSSATHelperMethods.monthIndexFromDayNumber(dayToUse);
				} else {
					// we gotta do this manually to make sure we know what's happening..
					if (dayToUse <= 31) {
						// it is january
						thisMonthIndex = 0;
					} else if (dayToUse <= (31 + 29)) {
						// it is february with a leap day
						thisMonthIndex = 1;
					} else {
						// it is after february, which means to get the right month index, we
						// have to pretend that the leap day isn't there: so subract it off...
						thisMonthIndex = DSSATHelperMethods.monthIndexFromDayNumber(dayToUse - 1);
					}
					
				} // if leap year or not
				

				// check to see if we are on january 1 or not... and bump up the counter accordingly
				if (dayToUse == 1) {
					nNewYearsSinceBeginningOfWeatherFile++;
				}


				// now comes the fun part, deciding what to write down...

				// grab the temperature info that we need regardless; for simplicity, we do it all here even though we might not need it all
				shortwaveHere = Double.parseDouble(originalAsArray[lineIndex].substring(magicLengthOfDate,shortwaveEnd).trim());
				tmaxHere      = Double.parseDouble(originalAsArray[lineIndex].substring(shortwaveEnd,tmaxEnd).trim());
				tminHere      = Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd,tminEnd).trim());
				rainHere      = Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd,rainEnd).trim());
				
				try {
					longwaveHere  = Double.parseDouble(originalAsArray[lineIndex].substring(longwaveStart).trim());
				} catch (NumberFormatException eee) {
					longwaveHere = -888.888;
				}
				
				// decide if we are drifting here or not
//				if (nNewYearsSinceBeginningOfWeatherFile > nJanuariesForSpinup) {
					// we have enough of the spin-up and/or fragmentary years, so let's start drifting
					
					// figure out what fraction of the way through the drift we are in.
					// note: the first "drifting" year is really going to be one last baseline, since i plan
					// to rig the multiplier to come out as zero.
					//
					// ok, that is easy for temperatures:
					// basically, we will do current_value = alpha * baseline + (1 - alpha) * future
					// but, we need the shift so we can re-center it on the daily weather, so...
					// current_shift =       alpha * baseline + (1 - alpha) * future - baseline
					// current_shift = (alpha - 1) * baseline + (1 - alpha) * future
					//
					// then for the first drifting year, alpha should be zero and for the last one, it should be one,
					// and beyond that, it should be bigger than one...
					//
					// BUT!!! rainfall and sunshine are absolute quantities (ok, temperature is too, but we're ignoring that)
					// so we need to do some sort of scaling and checking for absurdity
					
					// note the minus one to get us to zero for the first drifting year
//					yearIndexAfterStart = nNewYearsSinceBeginningOfWeatherFile - nJanuariesForSpinup - 1;
					
					// or perhaps i just had the mixing coefficient wrong...
//					linearMixingCoefficient = ((double)yearIndexAfterStart) / nYearsForTransition;
					// ??? offsetFromBaselineForLastUnmodifiedYear
				
				

				// this was the good one
//				linearMixingCoefficient = (nYearsForTransition - (double)yearIndexAfterStart) / nYearsForTransition;
				
				// the following plan should let us start before and/or end after the defined endpoints....
				// years into the transition / nYearsForTransition
				// (yearToAssumeAfterSpinup + nYearsAfterSpinupRightNow - baselineClimateYear)
				// for some reason i need a "minus one" to get the zero-change year to be the last full year of spinup time...
				linearMixingCoefficient = (nYearsForTransition - ((double)yearToAssumeAfterSpinup + (nNewYearsSinceBeginningOfWeatherFile - nJanuariesForSpinup - 1) - baselineClimateYear)) / nYearsForTransition;
				
					
					
					// these are the multipliers on the baseline and future references. the baseline has the "-1.0" because
					// we want the changes, not the levels, so we have to subtract off the starting point for the change, aka, the baseline
					// or perhaps i just had the mixing coefficient wrong...
//					weightForBaseline = (1.0 - linearMixingCoefficient);
//					  weightForFuture = (linearMixingCoefficient);

					// i think i had them backward initially. think of it like this: at the beginning, we are zero years past the start.
					// hence, the mixing coefficient (as defined above) is 0 / something = 0.0;
					// that should mean zero parts of the future....
					// or perhaps i just had the mixing coefficient wrong...
					weightForBaseline = (linearMixingCoefficient - 1.0);
					  weightForFuture = (1.0 - linearMixingCoefficient);

//						System.out.println("year=" + yearToUse + "; nNYSBOWF=" + nNewYearsSinceBeginningOfWeatherFile + 
//								"; fakeYear=" + (yearToAssumeAfterSpinup + (nNewYearsSinceBeginningOfWeatherFile - nJanuariesForSpinup - 1)) + 
//								"; lMC=" + linearMixingCoefficient + "; wF=" + weightForFuture);

					  
					  
					// Beware the MAGIC NUMBER!!! all the column numbers are magical at the moment...

					shortwaveBase = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 0});
					tmaxBase      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 1});
					tminBase      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 2});
					rainBase      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 3});
					
					shortwaveFuture = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 4});
					tmaxFuture      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 5});
					tminFuture      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 6});
					rainFuture      = dataMatrix.getValue(new long[] {pixelIndex,nValuesPerMonth * thisMonthIndex + 7});

					
					// rain and sun have a problem because we want to use multipliers and then apply them to the daily
					// values which are coming, duh, from a separate source. the problems include these:
					// A: baseline == 0 && future > 0 ; daily = zero on all days (i.e., we'll never pull it up off of zero); APPROACH: if nothing by day 28, put a constant boost on that day
					// B: baseline == 0 && future > 0 ; daily = nonzero on some days (i.e., how much should we pull it up?); APPROACH: nonzero days get constant boost
					// C: multiplier is fine ; daily = zero on all days (i.e., we'll never pull it up off of zero); APPROACH: if nothing by day 28, put a constant boost on that day
					// D: multiplier is huge ; daily >> 0 (i.e., we'll get massively inflated numbers); APPROACH: set max acceptable value
					
					/////////////////////////////////////////
					// shortwave solar radiation: sunshine //
					/////////////////////////////////////////
					
					// srad should be better behaved and we should be able to scale it. we shouldn't get much of significance going
					// from zero to something with sunshine and even if we do, it will be in super-marginal places anyway.
					// so let's do straight fractional changes on srad with a cap to keep it sorta reasonable.
					
					if (shortwaveBase <= 0.0) {
						// in this case, we have a problem, how far do we move up? a multiplier won't work due to division by zero
						// i'm afraid to move up to the full future amount, because it might be whacky compared to its neighbors
						// in any case, these will be unfavorable conditions anyway
						if (shortwaveFuture <= 0.0) {
							shortwaveToUse = shortwaveHere;
						} else {
							// but the baseline is, by construction, zero or negative, so we don't need to list it...
//						shortwaveToUse = shortwaveHere + weightForBaseline * shortwaveBase + weightForFuture * shortwaveFuture;
							shortwaveToUse = shortwaveHere + weightForFuture * shortwaveFuture;
						}
					} else {
						rawFractionalChangeShortwave = shortwaveFuture / shortwaveBase;
						shortwaveToUse = shortwaveHere * (1 + weightForFuture * (rawFractionalChangeShortwave - 1));
					} // end of shortwave cases....

					// and make sure it's not too ridiculous
					// in theory, this should somehow take into account total possible insolation for this latitude, but i am
					// way to lazy for that right now...
					if (shortwaveToUse > maximumAcceptableShortwave) {
						shortwaveToUse = maximumAcceptableShortwave;
					} else if (shortwaveToUse < 0) {
						// added in, but after my big runs. negative sunshine should only happen in really bad zero sunshine places
						// to begin with. and hopefully DSSAT does the censoring anyway.
						shortwaveToUse = 0.0d;
					}


					
					
					//////////////
					// rainfall //
					//////////////
					
					// ok, so what we want is basically to keep track of the cumulative rainfall and srad through the month
					// if we get to day #28 and haven't gotten anything, then we dump a bunch of rainfall on it.

					// figure out the day we're looking at...
					// we need month NUMBER, not index, you dork!
					dayOfTheMonthIndex = dayToUse - DSSATHelperMethods.firstPlantingDateFromMonth(thisMonthIndex + 1);

					// in leap years, 
					
					
					
					// get the starting value for when we want to dump a bunch of rain if we are in a zero->something situation
					magicDayForDelugeThisMonthAsDouble = DSSATHelperMethods.averageDaysInMonth[thisMonthIndex];

					// let's try to aim for the last day of the month...
					// example: january gives me 31.0; then the floor is 31; then i subtract 1 to get to index=30
					magicDayIndexForDelugeThisMonth = (int)Math.floor(magicDayForDelugeThisMonthAsDouble) - 1;
					// check for leap year; beware the indices versus numbers (starting at 0 vs 1)....
					if (yearToUse % 4 == 0) {
						if (thisMonthIndex > 1) {
							// we need to subtract off a day for march (index = 2) and onwardbecause of february 29
//							dayOfTheMonthIndex = dayToUse - DSSATHelperMethods.firstPlantingDateFromMonth(thisMonthIndex);
							dayOfTheMonthIndex-- ;
						} else if (thisMonthIndex == 1) {
							// we are in february, so the number of days in the month should be one more than usual
							magicDayIndexForDelugeThisMonth++;
						}
					}

					// initialize the flag if we have seen any rainfall...
					if (dayOfTheMonthIndex == 0) {
						rainHasFallenOnThisDayOrBefore = false;
//						System.out.println(latitudeToPullFrom + "_" + longitudeToPullFrom + " INNER "+ nNewYearsSinceBeginningOfWeatherFile + "; year = " + yearToUse + " starting month index " + thisMonthIndex + "; mDIFdeluge = " + magicDayIndexForDelugeThisMonth);
					}
					
					// now update the flag as appropriate
					if (rainHere > magicTinyDailyRainEquivalentToZero) {
						rainHasFallenOnThisDayOrBefore = true;
					}

					
					
//					System.out.println("yearsAS=" + nNewYearsSinceBeginningOfWeatherFile + "; real=" + yearToUse + "; day=" + dayToUse + "; monthI " +
//							thisMonthIndex +
//							"; swB=" + shortwaveBase + "; swF=" + shortwaveFuture + "; rB=" + rainBase + "; rF=" + rainFuture +
//							"; wFuture=" + FunTricks.fitInFiveCharacters(weightForFuture));

					
					
					
					// now we decide what to do. if it has already rained, we can just scale away similar to sunshine.
					// otherwise, we have to decide if we want to dump some on that day or not.
					
					if (rainBase > magicTinyMonthlyRainEquivalentToZero) {
						// we have a reasonable monthly rainfall change going on
						if (rainHasFallenOnThisDayOrBefore) {
							// we are all good and can scale with relative impunity
							// but, we only want to do that if the base reference case
							// has positive rainfall. otherwise, we want to do the "dump it all on one day" trick
							rawFractionalChangeRain = rainFuture / rainBase;
							rainToUse = rainHere * (1 + weightForFuture * (rawFractionalChangeRain - 1));
						}	else {
							// no daily rainfall yet, so we are working toward or are at the deluge on last day scenario
							// PUT DELUGE HERE
							if (dayOfTheMonthIndex < magicDayIndexForDelugeThisMonth) {
								// we haven't gotten to the deluge day yet, so just write down zero or its close equivalent being claimed for the day...
								rainToUse = rainHere;
							} else {
								// let it snow, let it snow, let it snow. or rain. whatever.
//								System.out.println("DELUGE!!! (rainBase > small; meaning normal situation)");
//								System.out.println(latitudeToPullFrom + "_" + longitudeToPullFrom + " INNER "+ nNewYearsSinceBeginningOfWeatherFile + "; year = " + yearToUse + " starting month index " + thisMonthIndex + "; mDIFdeluge = " + magicDayIndexForDelugeThisMonth);
								rainToUse = weightForFuture * (rainFuture - rainBase) + rainHere;
							}
						}
					} else { // rainBase <= magicTinyMonthlyRainEquivalentToZero
						if (rainFuture <= magicTinyMonthlyRainEquivalentToZero) {
							// there is no real rainfall in the future
							// thus, there is no real change, just keep the old one from the daily weather
							rainToUse = rainHere;
						} else {
							// there is basically nothing in the baseline but there is something in the future
							// so, we want to do the "dump it all on the last day of the month" when there is no rain
							
							// that is, deluge on the last day, no matter what. and just copy the other days over as is.
							if (dayOfTheMonthIndex == magicDayIndexForDelugeThisMonth) {
								// it's the last day. whether the daily has rain or not, let's give it a little boost
								// let it snow, let it snow, let it snow. or rain. whatever.
//								System.out.println("DELUGE!!! (rainBase < small; meaning zero to something ugly case)");
//								System.out.println(latitudeToPullFrom + "_" + longitudeToPullFrom + " INNER "+ nNewYearsSinceBeginningOfWeatherFile + "; year = " + yearToUse + " starting month index " + thisMonthIndex
//									+ "; swB=" + shortwaveBase + "; swF=" + shortwaveFuture + "; rB=" + rainBase + "; rF=" + rainFuture
//									);
								rainToUse = weightForFuture * (rainFuture - rainBase) + rainHere;
							} else {
								// just copy it over...
								rainToUse = rainHere;
							} // it's deluge day!!!! or not.
						} // (rainFuture <= magicTinyMonthlyRainEquivalentToZero)
					} // (rainBase > magicTinyMonthlyRainEquivalentToZero)
					

					// and make sure it's not too ridiculous
					// in theory, this should somehow take into account total possible insolation for this latitude, but i am
					// way to lazy for that right now...
					if (rainToUse > maximumAcceptableRain) {
						rainToUse = maximumAcceptableRain;
					} else if (rainToUse < 0) {
						// added in, but after my big runs. negative rainfall should only happen in really bad zero rainfall places
						// to begin with. and hopefully DSSAT does the censoring anyway.
						rainToUse = 0.0d;
					}

						
					
					//////////////////
					// temperatures //
					//////////////////
					
					// and how about the easy ones: temperature....
					tmaxToUse = tmaxHere + weightForBaseline * tmaxBase + weightForFuture * tmaxFuture;
					tminToUse = tminHere + weightForBaseline * tminBase + weightForFuture * tminFuture;
					
					// but, we need to make sure the high and low are different because of whiney crop models
					if (Math.abs(tmaxToUse - tminToUse) < magicMinimumTemperatureDifferenceNeeded) {
						tmaxToUse = tminToUse + magicMinimumTemperatureDifferenceNeeded;
					}
					
					// and double check that max is bigger than min. otherwise, swap them in the ugly SIMMETEO manner...

					if (tmaxToUse < tminToUse) {
						temperatureSwapSpot = tmaxToUse;
						tmaxToUse = tminToUse;
						tminToUse = temperatureSwapSpot;
					}
					

					
					// ok, and actually write it out...

					// write stuff out...
					// ok, here i need to have all the goodies added up and shifted/etc. then we write it out
					dailyOutputAsArray[lineIndex] = 
						thisDatePart + " " + 
						FunTricks.fitInNCharacters(shortwaveToUse, 5) + " " +
						FunTricks.fitInNCharacters(tmaxToUse, 5) + " " +
						FunTricks.fitInNCharacters(tminToUse, 5) + " " +
						FunTricks.fitInNCharacters(rainToUse, 5) + " " +
						" -999  -999" + " " +
						FunTricks.fitInNCharacters(longwaveHere, 5);
						;

					/*
					*WEATHER DATA :

					@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
					  RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
//				0         1         2         3         4         5 
//				0123456789012345678901234567890123456789012345678901234
					@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
					90274   9.7   4.0   3.1   1.5  -999  -999
					90275  12.5   5.4   1.8   1.9  -999  -999
					*/
				
//				} else {
//					// we don't have enough of the basics yet, so we should just recopy for the moment...
//					dailyOutputAsArray[lineIndex] = originalAsArray[lineIndex];
//					
//					// but, we still need the tmin/tmax to use for amp & tav later
//					tmaxToUse = tmaxHere;
//					tminToUse = tminHere;
//				}
				
				// accumulate the monthly stuff en route to tamp and tav...
				monthlyTmax[thisMonthIndex].useDoubleValue(tmaxToUse);
				monthlyTmin[thisMonthIndex].useDoubleValue(tminToUse);

			} // for lineIndex


			// fix up header line index 3
//			0         1         2         3         4         5 
//			0123456789012345678901234567890123456789012345678901234
//			@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
//		    RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
			
			// determine the average temperature and the extrema to get the amplitude
			temperatureSum = 0.0;
			hottestMonthValue = Double.NEGATIVE_INFINITY;
			coldestMonthValue = Double.POSITIVE_INFINITY;
			
			for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
				// figure out what we have going on this month, on average...
				thisMonthAverageTemperature = (monthlyTmax[monthIndex].getMean() + monthlyTmin[monthIndex].getMean()) / 2.0;

				// check the extrema
				if (thisMonthAverageTemperature > hottestMonthValue) { hottestMonthValue = thisMonthAverageTemperature;	}
				if (thisMonthAverageTemperature < coldestMonthValue) { coldestMonthValue = thisMonthAverageTemperature; }

				// accumulate the sum
				temperatureSum += thisMonthAverageTemperature;
			}
			
			overallAverageAnnualTemperature = temperatureSum / nMonths;

			temperatureFullAmplitude = hottestMonthValue - coldestMonthValue;

			String upUntilTAV, afterAMP;
			int upUntilTAVLength, firstAfterAMP;
			upUntilTAVLength = 31;
			firstAfterAMP = 42;
			upUntilTAV = originalAsArray[3].substring(0, upUntilTAVLength);
			afterAMP = originalAsArray[3].substring(firstAfterAMP);
			
			dailyOutputAsArray[3] = upUntilTAV + 
				FunTricks.fitInNCharacters(overallAverageAnnualTemperature, 5) + " " +
				FunTricks.fitInNCharacters(temperatureFullAmplitude, 5) + afterAMP;


			// write out the daily file
			FunTricks.writeStringArrayToFile(dailyOutputAsArray, thisOutputFile);

			statusString = FunTricks.statusCheck(pixelIndex, nValidPixels, nTotalChecks, startTimeMillis);
			
			if (statusString != null) {
				System.out.println(statusString);
			}

		} // end for pixelIndex
		///////////////////////////////////////
		// end of for loop over files goes here
		///////////////////////////////////////

		// write out the monthly files

		//////////////
		// all done //
		//////////////


	} // main



}

