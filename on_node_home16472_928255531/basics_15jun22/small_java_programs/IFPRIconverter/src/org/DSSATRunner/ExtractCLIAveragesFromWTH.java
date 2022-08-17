package org.DSSATRunner;

//import java.io.*;

//import java.text.DecimalFormat;
//import java.text.NumberFormat;

import java.io.File;

import org.R2Useful.*;


public class ExtractCLIAveragesFromWTH {

	// the idea here is that i have some reordered/resampled daily weather so the year numbers are all screwed up...
	// thus, i want to keep them in the order presented, but renumber the days/years to be sequential...

	static private int[] yyDDDtoyyANDddd(int yyDDD) {
    // the date is YYddd, so if we do integer division by a thousand, we get the year...
    int originalYear = yyDDD / 1000;

    // subtract off the year to get the days...
    int originalDayOfYear = yyDDD - 1000 * originalYear;
    
    return new int[] {originalYear , originalDayOfYear};
	}
	
	 static private int monthIndexFromDayNumber(int dayOfYearNumber) {
		 
		 if (dayOfYearNumber <= 31) {
			 return 0; // January = 0 in this case because we will use it for array indices...
		 } else if (dayOfYearNumber <= 59) {
			 return 1; // February
		 } else if (dayOfYearNumber <= 90) {
			 return 2; // March
		 } else if (dayOfYearNumber <= 120) {
			 return 3; // April
		 } else if (dayOfYearNumber <= 151) {
			 return 4; // May
		 } else if (dayOfYearNumber <= 181) {
			 return 5; // June
		 } else if (dayOfYearNumber <= 212) {
			 return 6; // July
		 } else if (dayOfYearNumber <= 243) {
			 return 7; // August
		 } else if (dayOfYearNumber <= 273) {
			 return 8; // September
		 } else if (dayOfYearNumber <= 304) {
			 return 9; // October
		 } else if (dayOfYearNumber <= 334) {
			 return 10; // November
		 } else {
			 return 11; // December
		 }
	 }

	
	public static void main(String commandLineOptions[]) throws Exception {

		////////////////////
		// magical things //
		////////////////////

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		String sourceDirectory  =                  commandLineOptions[0];
		String destinationPath  =                  commandLineOptions[1];
		double rainyDayMinimumRainfallThreshold = Double.parseDouble(commandLineOptions[2]);
		
		// first, let's check if the destination file already exists and advise that we are overwriting it...
		File outputFileObject = new File(destinationPath);
		if (outputFileObject.exists()) {
			System.out.println("intended output file [" + outputFileObject.getCanonicalPath() + "] already exists and (too late!) is going to be overwritten.");
			outputFileObject.delete();
		}
		
		
		System.out.println("--- !!! we assume that we have complete years and months... !!! ---");

		// we want to grab everything inside the directory, which will be assumed to be clean
		System.out.println("--- !!! we assume that everything inside the requested directory !!! ---");
		System.out.println("--- !!! [" + sourceDirectory + "] is useful !!! ---");
		
		File directoryObject = new File(sourceDirectory);
		
		File[] inputFiles = directoryObject.listFiles();

		
		
		
		// magic numbers for spacing
		final int magicGeographyAndYearlyLineIndex = 3;
		final int magicFirstLineToWorkOn = 5;
		final int magicLengthOfDate = 5;
		final int sradEnd = 11;
		final int tmaxEnd = 17;
		final int tminEnd = 23;
		final int rainEnd = 29;

		
		
		final int nMonths = 12; // this actually needs to be twelve
		final double magicDaysInYear = 365.24;
		
		final double[] daysInMonth = { 31, 28.24, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		
		final String delimiter = " ";
		final int nDecimalsToReport = 3;
		
		final int nTotalChecks = 5000;
		
		/// initializations
		String[] originalAsArray = null;
		String[] lineThreeInPieces = null;
		int originalDate, originalDayOfYear;
		
		// the geographic info
		double latitude  = -1;
		double longitude = -2;

		// the data stuff
		double tavStated = -1, ampStated;

		int monthIndexHere;
		double sradHere, tmaxHere, tminHere, rainHere;

		String giantOutputLine = "";

		String statusString = null;

		
		// monthly
		DescriptiveStatisticsUtility[] monthlySW        = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyTmax      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyTmin      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyPrec      = new DescriptiveStatisticsUtility[nMonths];
		DescriptiveStatisticsUtility[] monthlyRainydays = new DescriptiveStatisticsUtility[nMonths];

		// annual
		DescriptiveStatisticsUtility annualSW        = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility annualTmax      = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility annualTmin      = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility annualTave      = new DescriptiveStatisticsUtility(true);
//		DescriptiveStatisticsUtility annualTamp      = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility annualPrecTotal = new DescriptiveStatisticsUtility(true);
		
		for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
			monthlySW[monthIndex]        = new DescriptiveStatisticsUtility(true);
			monthlyTmax[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyTmin[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyPrec[monthIndex]      = new DescriptiveStatisticsUtility(true);
			monthlyRainydays[monthIndex] = new DescriptiveStatisticsUtility(true);
		}

		
		
		
		



		long startTimeMillis = System.currentTimeMillis(); // just for status updates for impatient users

		for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {
		
		
		try {
			originalAsArray = FunTricks.readTextFileToArray(inputFiles[fileIndex].getCanonicalPath());
		} catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
			System.out.println(" problem reading file: " + inputFiles[fileIndex].getCanonicalPath());
			throw aioobe;
		}
		
		
		// annual
		annualSW.reset();
		annualTmax.reset();
		annualTmin.reset();
		annualTave.reset();
		annualPrecTotal.reset();
		
		for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
			monthlySW[monthIndex].reset();
			monthlyTmax[monthIndex].reset();
			monthlyTmin[monthIndex].reset();
			monthlyPrec[monthIndex].reset();
			monthlyRainydays[monthIndex].reset();
		}

		
		

/*
example CLI

*CLIMATE : Soil Number = 20 ; Elevation = 24.0 ; Planting Month = 09 ; First Planting Day = 244 ; total N assumption = 200.0
@ INSI      LAT     LONG  ELEV   TAV   AMP  SRAY  TMXY  TMNY  RAIY
  RICK    52.50   006.75  0024  08.9  09.9  09.0  12.7  05.0  0768
@START  DURN  ANGA  ANGB REFHT WNDHT
  2000    15  0.25  0.50  0.00  0.00
@ GSST  GSDU
     0     0

*MONTHLY AVERAGES
@MONTH  SAMN  XAMN  NAMN  RTOT  RNUM
    01 001.6  04.1 -01.3 065.0 013.1
    02 002.3  04.8 -01.2 045.0 010.2
    03 005.2  08.1  01.2 054.0 012.1
    04 008.5  12.1  03.5 051.0 010.8
    05 013.8  16.7  07.1 059.0 011.2
    06 017.6  19.8  09.9 072.0 011.3
    07 018.1  21.0  11.5 077.0 011.8
    08 017.5  21.1  11.3 072.0 011.3
    09 012.4  18.3  09.1 064.0 011.0
    10 007.0  13.7  06.3 063.0 012.2
    11 002.6  08.1  02.7 071.0 012.8
    12 001.4  04.8  00.2 075.0 013.4
*/
//		String originalDatePart, restOfLine;
//		int originalDate, newDate, yearToUse, dayToUse, originalYear, originalDayOfYear;

    /*
	    *WEATHER DATA :

	    @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
	      RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
	    @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
	    90274   9.7   4.0   3.1   1.5  -999  -999
	    90275  12.5   5.4   1.8   1.9  -999  -999
     01234567890123456789012345678901234567890
     0         1         2         3         4
	    */



		
		// the plan is to read through all the days and accumulate them into statistics objects, then dump out
		// the results at the end. we will just hope that a) we have complete years and b) we have enough years
		// so c) there are minimal problems with edge effects...
		

		
		
		// first, let us grab the stated geographic and yearly info...
		lineThreeInPieces = FunTricks.parseRepeatedDelimiterToArray(originalAsArray[magicGeographyAndYearlyLineIndex], " ");

		try {
		latitude  = Double.parseDouble(lineThreeInPieces[1]);
		longitude = Double.parseDouble(lineThreeInPieces[2]);
//		elevation = Double.parseDouble(lineThreeInPieces[3]);
		tavStated = Double.parseDouble(lineThreeInPieces[4]);
		ampStated = Double.parseDouble(lineThreeInPieces[5]);
//		System.out.println("lat = " + latitude + "; long = " + longitude + "; elev = " + elevation + "; tav = " + tavStated + "; amp = " + ampStated);
		} catch (java.lang.NumberFormatException nfe) {
//			System.out.println("file: " + inputFiles[fileIndex].getCanonicalPath());
			System.out.println("lat = " + lineThreeInPieces[1] + "; long = " + lineThreeInPieces[2] + "; tav = " + lineThreeInPieces[4] + "; amp = " + lineThreeInPieces[5] + " file: " + inputFiles[fileIndex].getCanonicalPath());
			try {
				// there is/was some typo leading to a spacing problem and stuff. i'm just going to try to
				// brute force it by sliceing off the last couple characters and hoping for the best
/*
				[rdrobert@ifpri2 dailyweather]$ head /home/rdrobert/sge_Mink2daily/dailyweather/Baseline_NT_hadgem_renumbered/rn_Baseline_NT_hadgem_68.75_102.75.WTH
				*WEATHER DATA :

				@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
				  RICK    68.75   102.75  -999 -8.75 39.88.6  -999  -999
				@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D  LRAD
				 1274 2.574 -3.73 -7.66 1.365  -999  -999
				 1275 2.673 -6.03 -9.86 1.995  -999  -999
				 1276 2.178 -7.03 -14.4 0.840  -999  -999
				 1277 2.079 -10.6 -17.9 0.315  -999  -999
				 1278 2.376 -10.8 -17.8 0.105  -999  -999
				[rdrobert@ifpri2 dailyweather]$ cd raw_from_patrick/Baseline_NT
				[rdrobert@ifpri2 Baseline_NT]$ head ENSO_Baseline_NT_68.75_102.75.csv
				*WEATHER DATA :

				@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
				  RICK    68.75   102.75  -999   -12.0  41.6  -999  -999
				@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D  LRAD
				90274   2.6  -4.3  -8.3   1.3  -999  -999  -2.6
				90275   2.7  -6.6 -10.5   1.9  -999  -999  -3.3
				90276   2.2  -7.6 -15.0   0.8  -999  -999  -3.9
				90277   2.1 -11.2 -18.5   0.3  -999  -999  -4.2
				90278   2.4 -11.4 -18.4   0.1  -999  -999  -5.0
*/
				ampStated = Double.parseDouble(lineThreeInPieces[5].substring(0, lineThreeInPieces[5].length() - 2));
//				System.out.println("simple fix attempt gives us: " + ampStated);
			} catch (java.lang.NumberFormatException nfeInner) {
				System.out.println("simple fix failed, throwing up...");
				throw nfeInner;
			}
//			throw nfe;
		}
		
		
		
		
    for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {

    	// figure out the day we're looking at...
	    originalDate = Integer.parseInt(originalAsArray[lineIndex].substring(0, magicLengthOfDate).trim());
	    originalDayOfYear = yyDDDtoyyANDddd(originalDate)[1];
	    monthIndexHere = monthIndexFromDayNumber(originalDayOfYear);

	    sradHere = Double.parseDouble(originalAsArray[lineIndex].substring(magicLengthOfDate,sradEnd).trim());
	    tmaxHere = Double.parseDouble(originalAsArray[lineIndex].substring(sradEnd,tmaxEnd).trim());
	    tminHere = Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd,tminEnd).trim());
	    rainHere = Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd,rainEnd).trim());

	    // now comes the fun part, deciding what to write down...
			monthlySW[monthIndexHere].useDoubleValue(sradHere);
			monthlyTmax[monthIndexHere].useDoubleValue(tmaxHere);
			monthlyTmin[monthIndexHere].useDoubleValue(tminHere);
			monthlyPrec[monthIndexHere].useDoubleValue(rainHere);
			if (rainHere >= rainyDayMinimumRainfallThreshold) {
				monthlyRainydays[monthIndexHere].useDoubleValue(1.0);
			}

			// annual
			annualSW.useDoubleValue(sradHere);
			annualTmax.useDoubleValue(tmaxHere);
			annualTmin.useDoubleValue(tminHere);
			annualTave.useDoubleValue((tmaxHere + tminHere)/2);
			annualPrecTotal.useDoubleValue(rainHere);
	    
		} // for lineIndex
		
		



  	giantOutputLine = 
  		latitude + delimiter + 
  		longitude + delimiter +
  		FunTricks.onlySomeDecimalPlaces(annualSW.getMean(),nDecimalsToReport) + delimiter +
  		FunTricks.onlySomeDecimalPlaces(annualTmax.getMean(),nDecimalsToReport) + delimiter +
  		FunTricks.onlySomeDecimalPlaces(annualTmin.getMean(),nDecimalsToReport) + delimiter +
  		FunTricks.onlySomeDecimalPlaces(annualTave.getMean(),nDecimalsToReport) + delimiter +
  		FunTricks.onlySomeDecimalPlaces((annualTmax.getMean() - annualTave.getMean()),nDecimalsToReport)  + delimiter +
  		FunTricks.onlySomeDecimalPlaces((annualPrecTotal.getMean() * magicDaysInYear),nDecimalsToReport)  + delimiter;
  	;

    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
    	giantOutputLine += FunTricks.onlySomeDecimalPlaces(monthlySW[monthIndex].getMean(),nDecimalsToReport) + delimiter;
    }
    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
    	giantOutputLine += FunTricks.onlySomeDecimalPlaces(monthlyTmax[monthIndex].getMean(),nDecimalsToReport) + delimiter;
    }
    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
    	giantOutputLine += FunTricks.onlySomeDecimalPlaces(monthlyTmin[monthIndex].getMean(),nDecimalsToReport) + delimiter;
    }
    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
    	giantOutputLine += FunTricks.onlySomeDecimalPlaces(daysInMonth[monthIndex] * monthlyPrec[monthIndex].getMean(),nDecimalsToReport) + delimiter;
    }
    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
    	// we need the fraction of days that it rained times  the number of days in the month. i will get days in the month by
    	// days in the year times the fractional representation of this month
    	giantOutputLine += 
    		FunTricks.onlySomeDecimalPlaces((daysInMonth[monthIndex] * monthlyRainydays[monthIndex].getN()) / monthlyPrec[monthIndex].getN(),nDecimalsToReport) + delimiter;
    }
		         

    giantOutputLine += tavStated + delimiter + ampStated;

    FunTricks.appendLineToTextFile(giantOutputLine, destinationPath, true);
		         
		statusString = FunTricks.statusCheck(fileIndex, inputFiles.length, nTotalChecks, startTimeMillis);

		if (statusString != null) {
			System.out.println(statusString);
		}

		} // end giant file loop
		                  
		

		//////////////
		// all done //
		//////////////


	} // main



}

