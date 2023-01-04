package org.DSSATRunner;

//import java.io.*;

import org.R2Useful.*;
import org.DSSATRunner.DSSATHelperMethods;

public class RenumberYearsInWTHToSequentialWithMonthly {

	// we are adding in a new thing here (25jul14) that will compute monthly values to make data for
	// tingju's IMPACT Global Hydrologic Model in the funky format he needs. this necessitates peeling off
	// the long-wave radiation numbers he needs but i do not... i don't really need to peel them off (DSSAT
	// will ignore them), but we're trying to save space here folks...
	
	// the idea here is that i have some reordered/resampled daily weather so the year numbers are all screwed up...
	// thus, i want to keep them in the order presented, but renumber the days/years to be sequential...

	private static String kickOutCleanAverage(DescriptiveStatisticsUtility[] monthlyAccumulator, int monthIndex, String missingValueString, int nDecimalsToReport) {
		String valueToRecord = null;
		// need the long cast in there because february is 28.24...
		if (monthlyAccumulator[monthIndex].getN() < (long)DSSATHelperMethods.averageDaysInMonth[monthIndex]) {
			valueToRecord = missingValueString;
		} else {
			valueToRecord = FunTricks.onlySomeDecimalPlaces(monthlyAccumulator[monthIndex].getMean(),nDecimalsToReport);
		}

		return valueToRecord;
	}
	

	public static void main(String commandLineOptions[]) throws Exception {

		////////////////////
		// magical things //
		////////////////////

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		String pixelListFile         =                  commandLineOptions[0];
		String sourceDirectory       =                  commandLineOptions[1];
		String sourcePrefix          =                  commandLineOptions[2];
		String sourceSuffix          =                  commandLineOptions[3];
		String destinationDirectory  =                  commandLineOptions[4];
		String destinationPrefix     =                  commandLineOptions[5];
		String destinationSuffix     =                  commandLineOptions[6];
		String monthlySuffix         =                  commandLineOptions[7];
		int    firstYearToUse        = Integer.parseInt(commandLineOptions[8]);


		// magic numbers for spacing
		final int magicFirstLineToWorkOn = 5;
		final int magicLengthOfDate = 5;
		final int shortwaveEnd = 11;
		final int tmaxEnd = 17;
		final int tminEnd = 23;
		final int rainEnd = 29;
		final int longwaveStart = 41;

		// magic numbers for thresholds to keep it real
		final double maximumAcceptableShortwave = 29.8; // MJ/m^2/day
		final double maximumAcceptableRain = 600.0; // mm/day ; quasi-max in Baseline_NT is 493.2 mm

		final double magicMinimumTemperatureDifferenceNeeded = 0.1;


		final String missingValueString = "-999.000";
		
		final int nMonths = 12; // this actually needs to be twelve
		
		
		final String delimiter = " ";
		final int nDecimalsToReport = 3;
		
		final int nTotalChecks = 5000;
		
		/// initializations
		int originalDate, originalDayOfYear;

		String originalDatePart; // , restOfLine;
		int newDate, yearToUse, dayToUse, originalYear;
		
		// the geographic info
		double latitudeToPullFrom  = -1;
		double longitudeToPullFrom = -2;
		int pixelIDNumberToPullFrom = -5;
		
		// the data stuff
//		double tavStated, ampStated;

		int monthIndexHere;
		double shortwaveHere, tmaxHere, tminHere, rainHere, longwaveHere;
		double temperatureSwapSpot;

		boolean timeToDumpMonthlyDataForPrecedingYear = false;

		String monthlyHeader = null;
		String delimiterToUse = null;
		String thisInputFile = null;
		String thisOutputFile = null;
		String[] originalAsArray = null;
		String[] dailyOutputAsArray = null;

		String statusString = null;
		
		// monthly
		String monthlyShortwaveFilename = null;
		String monthlyTmaxFilename = null;
		String monthlyTminFilename = null;
		String monthlyPrecFilename = null;
		String monthlyLongwaveFilename = null;

		String giantMonthlyShortwave   = "";
		String giantMonthlyTmax = "";
		String giantMonthlyTmin = "";
		String giantMonthlyPrec = "";
		String giantMonthlyLongwave   = "";

		DescriptiveStatisticsUtility[] monthlyShortwave = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyTmax      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyTmin      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyPrec      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyLongwave  = new DescriptiveStatisticsUtility[nMonths];
		
		for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
			monthlyShortwave[monthIndex] = new DescriptiveStatisticsUtility(true);
			monthlyTmax[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyTmin[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyPrec[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyLongwave[monthIndex]  = new DescriptiveStatisticsUtility(true);
		}


/*
*WEATHER DATA :

@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
  RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
90274   9.7   4.0   3.1   1.5  -999  -999
90275  12.5   5.4   1.8   1.9  -999  -999
*/

		// read in and interpret the pixel list file...
		// read...
		String[] rawPixelListLines = FunTricks.readTextFileToArray(pixelListFile);
		
		// interpret...
		// we are expecting 4 columns: pixelIDNumber, longitude, latitude, land area
		final int expectedIDColumns = 4;
		final int pixelIDNumberColumn    = 0;
		final int pixelLongitudeColumn = 1;
		final int pixelLatitudeColumn  = 2;

		final int magicTAVStartIndex = 30;
		final int magicAMPStartIndex = 36;

		
		int nPixels = rawPixelListLines.length;
		int nValidPixels = nPixels; // this might get adjusted if we have empty lines...
		
		int[]    pixelIDNumber  = new int[nPixels];
		double[] pixelLongitude = new double[nPixels];
		double[] pixelLatitude  = new double[nPixels];
		
		String[] pixelLineArray = null;
		int storageIndex = 0;
		for (int pixelIndex = 0; pixelIndex < rawPixelListLines.length; pixelIndex++) {
			pixelLineArray = FunTricks.parseRepeatedDelimiterToArray(rawPixelListLines[pixelIndex], delimiter);
			
			if (pixelLineArray.length != expectedIDColumns) {
				// we have some blank lines in there somewhere, possibly a trailing hard return...
				continue;
			}
			
			pixelIDNumber[storageIndex]  = Integer.parseInt(  pixelLineArray[pixelIDNumberColumn]);
			pixelLongitude[storageIndex] = Double.parseDouble(pixelLineArray[pixelLongitudeColumn]);
			pixelLatitude[storageIndex]  = Double.parseDouble(pixelLineArray[pixelLatitudeColumn]);
			
			storageIndex++;
		}

		// now we know how many valid pixels there are...
		nValidPixels = storageIndex;
		
		
		
		
		// build up the output file names...
		// rain
		// tmin
		// tmax
		// srad
		// longwave
		
		monthlyShortwaveFilename  = destinationDirectory + "shortwave_" + sourcePrefix + monthlySuffix;
		monthlyTmaxFilename       = destinationDirectory + "tmax_"      + sourcePrefix + monthlySuffix;
		monthlyTminFilename       = destinationDirectory + "tmin_"      + sourcePrefix + monthlySuffix;
		monthlyPrecFilename       = destinationDirectory + "prec_"      + sourcePrefix + monthlySuffix;
		monthlyLongwaveFilename   = destinationDirectory + "longwave_"  + sourcePrefix + monthlySuffix;

		
		
		
		//////////////////////////////////////////////////////////////
		// up here, we will need to put in the BEGINNING OF A FOR LOOP
		//////////////////////////////////////////////////////////////

		long startTimeMillis = System.currentTimeMillis(); // just for status updates for impatient users
		
		String missingFilesList = "";
		int nMissingFiles = 0;
		
		for (int pixelIndex = 0; pixelIndex < nValidPixels; pixelIndex++) {
			// build up the input file name....
			// grab the latitude/longitude/idnumber from the list tingju gave me....

			pixelIDNumberToPullFrom = pixelIDNumber[pixelIndex];
			longitudeToPullFrom     = pixelLongitude[pixelIndex];
			latitudeToPullFrom      = pixelLatitude[pixelIndex];

			thisInputFile  = sourceDirectory      + sourcePrefix      + "_" + latitudeToPullFrom + "_" + longitudeToPullFrom + sourceSuffix; 
			thisOutputFile = destinationDirectory + destinationPrefix + "_" + latitudeToPullFrom + "_" + longitudeToPullFrom + destinationSuffix; 

			// read in the raw data

			// if it's not there, we will note it down, skip over it, and report all the
			// failures at the end...
			try {
				originalAsArray = FunTricks.readTextFileToArray(thisInputFile);
			} catch (java.io.FileNotFoundException fnfe) {
				missingFilesList += "\n" + thisInputFile;
				nMissingFiles++;
				continue;
			}
			dailyOutputAsArray = new String[originalAsArray.length];

			// reset the monthly accumulators and monthly output lines...

			for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
				monthlyShortwave[monthIndex].reset();
				monthlyTmax[monthIndex].reset();
				monthlyTmin[monthIndex].reset();
				monthlyPrec[monthIndex].reset();
//				monthlyRainydays[monthIndex].reset();
				monthlyLongwave[monthIndex].reset();
			}


			// do the first few header lines
			dailyOutputAsArray[0] = originalAsArray[0];
			dailyOutputAsArray[1] = originalAsArray[1];
			dailyOutputAsArray[2] = originalAsArray[2];
			dailyOutputAsArray[3] = originalAsArray[3];
			dailyOutputAsArray[4] = originalAsArray[4];

			

//			@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
//			0         1         2         3         4         5
//			0123456789012345678901234567890123456789012345678901234
//			  RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
//			                             "   0.1"
			// i want to check to see if TAV is zero or negative and then put it to an ever so slightly positive value
			// to try to get the soil temperature to not be 20 degrees C by default...
			// Beware the MAGIC NUMBER!!! lengths and positions of tav...
			if (Double.parseDouble(originalAsArray[3].substring(magicTAVStartIndex,magicAMPStartIndex).trim()) <= 0.0d) {
				// the value is negative, so we need to replace it with a suitable minimally positive value
				dailyOutputAsArray[3] = originalAsArray[3].substring(0,magicTAVStartIndex) + "   0.1" + originalAsArray[3].substring(magicAMPStartIndex);
			}
				// the value is positive,so carry on
			

			// build the monthly header...
			monthlyHeader = pixelIDNumberToPullFrom + delimiter + longitudeToPullFrom + delimiter + latitudeToPullFrom;


			// i think what we will do is figure out which day of the year is represented by the
			// very first entry. then we will just run sequentially from there, taking into account leap years
			// and hoping for the best...

			// initialize year with value requested
			yearToUse = firstYearToUse;
			// initialize day with whatever shows up in the file first...
			originalDatePart = originalAsArray[magicFirstLineToWorkOn].substring(0, magicLengthOfDate);
			originalDate = Integer.parseInt(originalDatePart.trim());
			// the date is YYddd, so if we do integer division by a thousand, we get the year...
			originalYear = originalDate / 1000;

			// subtract off the year to get the days...
			originalDayOfYear = originalDate - 1000 * originalYear;

			// check for weirdness: we might start at the very end of a leap year in the data written down, but
			// not wish to start our fake year in a leap year....
			if (originalDayOfYear > 365) {
				// we will just force it back to 365 which should always work...
				dayToUse = 365;
			} else {
				dayToUse = originalDayOfYear;
			}

			// now start marching through the days...
			timeToDumpMonthlyDataForPrecedingYear = false;
//			giantMonthlyShortwave   += monthlyHeader; // tack on the header for this block of data
//			giantMonthlyTmax += monthlyHeader; // tack on the header for this block of data
//			giantMonthlyTmin += monthlyHeader; // tack on the header for this block of data
//			giantMonthlyPrec += monthlyHeader; // tack on the header for this block of data
//			giantMonthlyLongwave   += monthlyHeader; // tack on the header for this block of data

			FunTricks.appendLineToTextFile(monthlyHeader, monthlyShortwaveFilename, true);
			FunTricks.appendLineToTextFile(monthlyHeader,      monthlyTmaxFilename, true);
			FunTricks.appendLineToTextFile(monthlyHeader,      monthlyTminFilename, true);
			FunTricks.appendLineToTextFile(monthlyHeader,      monthlyPrecFilename, true);
			FunTricks.appendLineToTextFile(monthlyHeader,  monthlyLongwaveFilename, true);


			for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {

				// we don't care about the date, so just figure everything else out....
//				restOfLine = originalAsArray[lineIndex].substring(magicLengthOfDate,longwaveStart);

				// now comes the fun part, deciding what to write down...

				// are we in a leap year for the yearToUse?
				// Beware the MAGIC ASSUMPTION!!! we think DSSAT will interpret 00 as 2000 which *was* a leap year
				// but 2100 will not be. grrr... two digit dates. so don't worry about century correction...

				// ok, so we need to decide when to turn the fake year over. that will happen when the day to use
				// is bigger than the usual 365. 
				if (dayToUse > 365) {
					// normal years need to bump up the year and reset the day while leap years should carry on to 366.
					if (yearToUse % 4 != 0) {
						// normal year
						yearToUse = (yearToUse + 1) % 100;
						dayToUse = 1;
						timeToDumpMonthlyDataForPrecedingYear = true;
					} else {
						// leap year: reset after 366 instead of 365
						if (dayToUse > 366) {
							yearToUse = (yearToUse + 1) % 100;
							dayToUse = 1;
							timeToDumpMonthlyDataForPrecedingYear = true;
						} // reset leap years
					} // are we in a leap year?
				} // do we need to reset...

				if (timeToDumpMonthlyDataForPrecedingYear) {

					giantMonthlyShortwave = "";
					giantMonthlyTmax = "";
					giantMonthlyTmin = "";
					giantMonthlyPrec = "";
					giantMonthlyLongwave = "";

					// write down the info and reset the counters...
					for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
						if (monthIndex == 0) { delimiterToUse = ""; } else { delimiterToUse = delimiter; }
						// write down the monthly stuff....
//						giantMonthlyShortwave   += delimiterToUse + kickOutCleanAverage(monthlyShortwave,   monthIndex, missingValueString, nDecimalsToReport);
//						giantMonthlyTmax += delimiterToUse + kickOutCleanAverage(monthlyTmax, monthIndex, missingValueString, nDecimalsToReport);
//						giantMonthlyTmin += delimiterToUse + kickOutCleanAverage(monthlyTmin, monthIndex, missingValueString, nDecimalsToReport);
//						giantMonthlyPrec += delimiterToUse + kickOutCleanAverage(monthlyPrec, monthIndex, missingValueString, nDecimalsToReport);
//						giantMonthlyLongwave   += delimiterToUse + kickOutCleanAverage(monthlyLongwave,   monthIndex, missingValueString, nDecimalsToReport);

						giantMonthlyShortwave   += delimiterToUse + kickOutCleanAverage(monthlyShortwave,   monthIndex, missingValueString, nDecimalsToReport);
						giantMonthlyTmax += delimiterToUse + kickOutCleanAverage(monthlyTmax, monthIndex, missingValueString, nDecimalsToReport);
						giantMonthlyTmin += delimiterToUse + kickOutCleanAverage(monthlyTmin, monthIndex, missingValueString, nDecimalsToReport);
						giantMonthlyPrec += delimiterToUse + kickOutCleanAverage(monthlyPrec, monthIndex, missingValueString, nDecimalsToReport);
						giantMonthlyLongwave   += delimiterToUse + kickOutCleanAverage(monthlyLongwave,   monthIndex, missingValueString, nDecimalsToReport);

						// do the resetting
						monthlyShortwave[monthIndex].reset();
						monthlyTmax[monthIndex].reset();
						monthlyTmin[monthIndex].reset();
						monthlyPrec[monthIndex].reset();
//						monthlyRainydays[monthIndex].reset();
						monthlyLongwave[monthIndex].reset();
					}
					
				FunTricks.appendLineToTextFile(giantMonthlyShortwave, monthlyShortwaveFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyTmax,     monthlyTmaxFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyTmin,     monthlyTminFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyPrec,     monthlyPrecFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyLongwave, monthlyLongwaveFilename, true);

					// now put a newline at the end of each line
//					giantMonthlyShortwave   += "\n";
//					giantMonthlyTmax += "\n";
//					giantMonthlyTmin += "\n";
//					giantMonthlyPrec += "\n";
//					giantMonthlyLongwave   += "\n";

					// reset the dump flag
					timeToDumpMonthlyDataForPrecedingYear = false;

				} // end if time to dump monthly...


				// ok, now we can put together the fake date code...
				newDate = 1000*yearToUse + dayToUse;




				// accumulate the monthly stuff....

//				System.out.println("file = " + thisInputFile + "; lineIndex = " + lineIndex + " [" + originalAsArray[lineIndex] + "]");
				// grab the info
				try {
				shortwaveHere = Double.parseDouble(originalAsArray[lineIndex].substring(magicLengthOfDate,shortwaveEnd).trim());
				} catch (NumberFormatException eee) {
					System.out.println(thisInputFile + ": lineIndex=" + lineIndex + " shortwave");
					eee.printStackTrace();
					throw new Exception();
				}
				try {
					tmaxHere      = Double.parseDouble(originalAsArray[lineIndex].substring(shortwaveEnd,tmaxEnd).trim());
				} catch (NumberFormatException eee) {
					System.out.println(thisInputFile + ": lineIndex=" + lineIndex + " tmax");
					eee.printStackTrace();
					throw new Exception();
				}
				try {
					tminHere      = Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd,tminEnd).trim());
				} catch (NumberFormatException eee) {
					System.out.println(thisInputFile + ": lineIndex=" + lineIndex + " tmin");
					eee.printStackTrace();
					throw new Exception();
				}
				try {
					rainHere      = Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd,rainEnd).trim());
				} catch (NumberFormatException eee) {
					System.out.println(thisInputFile + ": lineIndex=" + lineIndex + " rain");
					eee.printStackTrace();
					throw new Exception();
				}
				try {
					longwaveHere  = Double.parseDouble(originalAsArray[lineIndex].substring(longwaveStart).trim());
				} catch (NumberFormatException eee) {
					longwaveHere = -888.888;
				}

				
				// moving the daily out put down here because i want to do the obvious checks for reasonablity...
				// shortwave
				if (shortwaveHere < 0.0d) {
					shortwaveHere = 0.0;
				} else if (shortwaveHere > maximumAcceptableShortwave) {
					shortwaveHere = maximumAcceptableShortwave;
				}
				
				// tmax & tmin
				if (Math.abs(tmaxHere - tminHere) < magicMinimumTemperatureDifferenceNeeded) {
					tmaxHere = tminHere + magicMinimumTemperatureDifferenceNeeded;
				}

				// and double check that max is bigger than min. otherwise, swap them in the ugly SIMMETEO manner...
				if (tmaxHere < tminHere) {
					temperatureSwapSpot = tmaxHere;
					tmaxHere = tminHere;
					tminHere = temperatureSwapSpot;
				}


				// rain
				if (rainHere < 0.0d) {
					rainHere = 0.0;
				} else if (rainHere > maximumAcceptableRain) {
					rainHere = maximumAcceptableRain;
				}

				// i need to build this up from scratch...
//				dailyOutputAsArray[lineIndex] = FunTricks.padStringWithLeadingSpaces(Integer.toString(newDate), magicLengthOfDate) + restOfLine;
			dailyOutputAsArray[lineIndex] = 
				FunTricks.padStringWithLeadingSpaces(Integer.toString(newDate), magicLengthOfDate) + " " + 
				FunTricks.fitInNCharacters(shortwaveHere, 5) + " " +
				FunTricks.fitInNCharacters(tmaxHere, 5) + " " +
				FunTricks.fitInNCharacters(tminHere, 5) + " " +
				FunTricks.fitInNCharacters(rainHere, 5) + " " +
				" -999  -999"; // + " " +
//				FunTricks.fitInNCharacters(longwaveHere, 5);
//				;


				
				// figure out the day we're looking at...
				monthIndexHere = DSSATHelperMethods.monthIndexFromDayNumber(dayToUse);
				
				// now comes the fun part, deciding what to write down...
				monthlyShortwave[monthIndexHere].useDoubleValue(shortwaveHere);
				monthlyTmax[monthIndexHere].useDoubleValue(tmaxHere);
				monthlyTmin[monthIndexHere].useDoubleValue(tminHere);
				monthlyPrec[monthIndexHere].useDoubleValue(rainHere);
				monthlyLongwave[monthIndexHere].useDoubleValue(longwaveHere);



				// bump up the day counter
				dayToUse++;

				


			} // for lineIndex

			// check to see if we have gone through a partial year. if so, then we need to write out the monthly stuff...
			// write down the info and reset the counters...
			timeToDumpMonthlyDataForPrecedingYear = false;
			for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
				// using shortwave as the representative
				if (monthlyShortwave[monthIndex].getN() > 0) {
					timeToDumpMonthlyDataForPrecedingYear = true;
					break;
				}
			}
			
			if (timeToDumpMonthlyDataForPrecedingYear) {
				giantMonthlyShortwave = "";
				giantMonthlyTmax = "";
				giantMonthlyTmin = "";
				giantMonthlyPrec = "";
				giantMonthlyLongwave = "";

				for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
					if (monthIndex == 0) { delimiterToUse = ""; } else { delimiterToUse = delimiter; }
					// write down the monthly stuff....
					giantMonthlyShortwave   += delimiterToUse + kickOutCleanAverage(monthlyShortwave,   monthIndex, missingValueString, nDecimalsToReport);
					giantMonthlyTmax += delimiterToUse + kickOutCleanAverage(monthlyTmax, monthIndex, missingValueString, nDecimalsToReport);
					giantMonthlyTmin += delimiterToUse + kickOutCleanAverage(monthlyTmin, monthIndex, missingValueString, nDecimalsToReport);
					giantMonthlyPrec += delimiterToUse + kickOutCleanAverage(monthlyPrec, monthIndex, missingValueString, nDecimalsToReport);
					giantMonthlyLongwave   += delimiterToUse + kickOutCleanAverage(monthlyLongwave,   monthIndex, missingValueString, nDecimalsToReport);

					// do the resetting
					monthlyShortwave[monthIndex].reset();
					monthlyTmax[monthIndex].reset();
					monthlyTmin[monthIndex].reset();
					monthlyPrec[monthIndex].reset();
					monthlyLongwave[monthIndex].reset();
				}

				FunTricks.appendLineToTextFile(giantMonthlyShortwave, monthlyShortwaveFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyTmax,     monthlyTmaxFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyTmin,     monthlyTminFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyPrec,     monthlyPrecFilename, true);
				FunTricks.appendLineToTextFile(giantMonthlyLongwave, monthlyLongwaveFilename, true);

			} // end if time to dump monthly...


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

		
		
		
		
		

		System.out.println(nMissingFiles + " pixels were missing:");
		System.out.println(missingFilesList);

		
//		FunTricks.writeStringToFile(giantMonthlyShortwave, monthlyShortwaveFilename);
//		FunTricks.writeStringToFile(giantMonthlyTmax,     monthlyTmaxFilename);
//		FunTricks.writeStringToFile(giantMonthlyTmin,     monthlyTminFilename);
//		FunTricks.writeStringToFile(giantMonthlyPrec,     monthlyPrecFilename);
//		FunTricks.writeStringToFile(giantMonthlyLongwave, monthlyLongwaveFilename);

		// write out the monthly files

		//////////////
		// all done //
		//////////////


	} // main



}

