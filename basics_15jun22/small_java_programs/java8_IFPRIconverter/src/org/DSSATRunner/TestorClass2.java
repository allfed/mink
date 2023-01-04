package org.DSSATRunner;

import org.R2Useful.*;

import java.io.*;

public class TestorClass2 {

    
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

    static private double[] monthlyWeatherAveragesDuringSeason(String[] originalAsArray, int actualPlantingDate, int numberOfMonthsToBeSummarized) throws Exception {
	// i want to develop a method that extracts monthly weather from the weather file based on the actual planting date
	
	
	// magic numbers for spacing
	final int magicFirstLineToWorkOn = 5;
	final int magicLengthOfDate = 5;
	final int sradEnd = 11;
	final int tmaxEnd = 17;
	final int tminEnd = 23;
	final int rainEnd = 29;



	final int nMonths = 12; // this actually needs to be twelve

	/// initializations
	int originalDate, yearInFile = -5, originalDayOfYear;

	int monthIndexHere;
	double sradHere, tmaxHere, tminHere, rainHere;



	// monthly
	DescriptiveStatisticsUtility[] monthlySW        = new DescriptiveStatisticsUtility[numberOfMonthsToBeSummarized];
	DescriptiveStatisticsUtility[] monthlyTmax      = new DescriptiveStatisticsUtility[numberOfMonthsToBeSummarized];
	DescriptiveStatisticsUtility[] monthlyTmin      = new DescriptiveStatisticsUtility[numberOfMonthsToBeSummarized];
	DescriptiveStatisticsUtility[] monthlyPrec      = new DescriptiveStatisticsUtility[numberOfMonthsToBeSummarized];
	DescriptiveStatisticsUtility[] monthlyRainydays = new DescriptiveStatisticsUtility[numberOfMonthsToBeSummarized];

	for (int monthIndex = 0; monthIndex < numberOfMonthsToBeSummarized; monthIndex++) {
	    monthlySW[monthIndex]        = new DescriptiveStatisticsUtility(true);
	    monthlyTmax[monthIndex]      = new DescriptiveStatisticsUtility(true);
	    monthlyTmin[monthIndex]      = new DescriptiveStatisticsUtility(true);
	    monthlyPrec[monthIndex]      = new DescriptiveStatisticsUtility(true);
	    monthlyRainydays[monthIndex] = new DescriptiveStatisticsUtility(true);
	}

	
	
	////////////////////
	
	
	
	// read the weather file	
	
	
	
	// we will need to start at the beginning of the month that planting takes place in even if it is the last day of the month
	// and we will need to start in that year....
	int actualPlantingYearNumber = (actualPlantingDate / 1000);
	int actualPlantingDayOfYearNumber = actualPlantingDate - actualPlantingYearNumber * 1000;
	
	int monthIndexTheActualPlantingDateFallsIn = DSSATHelperMethods.monthIndexFromDayNumber(actualPlantingDayOfYearNumber); // the method gives a month index so add one to get the number....
	int monthTheActualPlantingDateFallsIn = 1 + monthIndexTheActualPlantingDateFallsIn; // the method gives a month index so add one to get the number....
	int firstDayOfMonthTheActualPlantingDateFallsIn = DSSATHelperMethods.firstPlantingDateFromMonth(monthTheActualPlantingDateFallsIn);
	int dateCodeForWhereWeWantToStartAsInteger = actualPlantingYearNumber * 1000 + firstDayOfMonthTheActualPlantingDateFallsIn;
	
	// we also want to stop, so we need to figure out the first day we do not want
	int firstMonthWeDoNotWant = monthTheActualPlantingDateFallsIn + numberOfMonthsToBeSummarized;
	int firstYearWeDoNotWant = actualPlantingYearNumber;
	
	// now we need to back out the number of years that is....
	
	while (firstMonthWeDoNotWant > nMonths) {
	    firstMonthWeDoNotWant -= nMonths;
	    firstYearWeDoNotWant += 1;
	}

	int firstDayOfMonthWeDoNotWant = DSSATHelperMethods.firstPlantingDateFromMonth(firstMonthWeDoNotWant);
	int dateCodeForWhereWeWantToStopAsInteger = firstYearWeDoNotWant * 1000 + firstDayOfMonthWeDoNotWant;

	
	
	
	
	// the weather generator pads with spaces instead of zeros. we could parse to integer and compare, but that seems like more work than we want....
	// going manual....
	
	String dateCodeForWhereWeWantToStart = null;
	if (dateCodeForWhereWeWantToStartAsInteger <= 9) {
	    dateCodeForWhereWeWantToStart = "    " + dateCodeForWhereWeWantToStartAsInteger;
	} else if (dateCodeForWhereWeWantToStartAsInteger <= 99) {
	    dateCodeForWhereWeWantToStart = "   " + dateCodeForWhereWeWantToStartAsInteger;
	} else if (dateCodeForWhereWeWantToStartAsInteger <= 999) {
	    dateCodeForWhereWeWantToStart = "  " + dateCodeForWhereWeWantToStartAsInteger;
	} else if (dateCodeForWhereWeWantToStartAsInteger <= 9999) {
	    dateCodeForWhereWeWantToStart = " " + dateCodeForWhereWeWantToStartAsInteger;
	} else {
	    dateCodeForWhereWeWantToStart = "" + dateCodeForWhereWeWantToStartAsInteger;
	}
	
	String dateCodeForWhereWeWantToStop = null;
	if (dateCodeForWhereWeWantToStopAsInteger <= 9) {
	    dateCodeForWhereWeWantToStop = "    " + dateCodeForWhereWeWantToStopAsInteger;
	} else if (dateCodeForWhereWeWantToStopAsInteger <= 99) {
	    dateCodeForWhereWeWantToStop = "   " + dateCodeForWhereWeWantToStopAsInteger;
	} else if (dateCodeForWhereWeWantToStopAsInteger <= 999) {
	    dateCodeForWhereWeWantToStop = "  " + dateCodeForWhereWeWantToStopAsInteger;
	} else if (dateCodeForWhereWeWantToStopAsInteger <= 9999) {
	    dateCodeForWhereWeWantToStop = " " + dateCodeForWhereWeWantToStopAsInteger;
	} else {
	    dateCodeForWhereWeWantToStop = "" + dateCodeForWhereWeWantToStopAsInteger;
	}
	
	
	
//	System.out.println("actualPlantingDate = [" + actualPlantingDate + "]");
//	System.out.println("actualPlantingYearNumber = [" + actualPlantingYearNumber + "]");
//	System.out.println("actualPlantingDayOfYearNumber = [" + actualPlantingDayOfYearNumber + "]");
//	System.out.println("monthTheActualPlantingDateFallsIn = [" + monthTheActualPlantingDateFallsIn + "]");
//	System.out.println("firstDayOfMonthTheActualPlantingDateFallsIn = [" + firstDayOfMonthTheActualPlantingDateFallsIn + "]");
//	System.out.println("dateCodeForWhereWeWantToStartAsInteger = [" + dateCodeForWhereWeWantToStartAsInteger + "]");
//	System.out.println("dateCodeForWhereWeWantToStart = [" + dateCodeForWhereWeWantToStart + "]");
//	System.out.println("dateCodeForWhereWeWantToStopAsInteger = [" + dateCodeForWhereWeWantToStopAsInteger + "]");
//	System.out.println("dateCodeForWhereWeWantToStop = [" + dateCodeForWhereWeWantToStop + "]");
	
	
	// figure out the line index with that starting date in it
	int firstLineOfPlantingMonth = -5;
	int firstLineWeDoNotWant = -5;
	for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {
	    // check if this line begins with the right day...
	    if(originalAsArray[lineIndex].substring(0, magicLengthOfDate).equals(dateCodeForWhereWeWantToStart)) {
		firstLineOfPlantingMonth = lineIndex;
	    }
	    if(originalAsArray[lineIndex].substring(0, magicLengthOfDate).equals(dateCodeForWhereWeWantToStop)) {
		firstLineWeDoNotWant = lineIndex;
		break;
	    }
	}	

//	System.out.println("firstLineOfPlantingMonth = [" + firstLineOfPlantingMonth + "]");
//	System.out.println("first line we want: [" + originalAsArray[firstLineOfPlantingMonth] + "]");
//	System.out.println("firstLineWeDoNotWant = [" + firstLineWeDoNotWant + "]");
//	System.out.println("first line we DO NOT want: [" + originalAsArray[firstLineWeDoNotWant] + "]");

	
	int storageMonthIndex = -5;
	
	
	for (int lineIndex = firstLineOfPlantingMonth; lineIndex < firstLineWeDoNotWant; lineIndex++) {

		// figure out the day we're looking at...
		originalDate = Integer.parseInt(originalAsArray[lineIndex].substring(0, magicLengthOfDate).trim());
		yearInFile      = yyDDDtoyyANDddd(originalDate)[0];
		originalDayOfYear = yyDDDtoyyANDddd(originalDate)[1];
		monthIndexHere = monthIndexFromDayNumber(originalDayOfYear);
		
		
			
		storageMonthIndex = monthIndexHere - monthIndexTheActualPlantingDateFallsIn + nMonths * (yearInFile - actualPlantingYearNumber); // the month was recorded as a number

//		System.out.println("monthIndexHere = " + monthIndexHere);
//		System.out.println("monthIndexTheActualPlantingDateFallsIn = " + monthIndexTheActualPlantingDateFallsIn);
//		System.out.println("yearInFile = " + yearInFile);
//		System.out.println("actualPlantingYearNumber = " + actualPlantingYearNumber);
//		System.out.println(storageMonthIndex);
		
		// ok, start interpreting...
		
		sradHere = Double.parseDouble(originalAsArray[lineIndex].substring(magicLengthOfDate,sradEnd).trim());
		tmaxHere = Double.parseDouble(originalAsArray[lineIndex].substring(sradEnd,tmaxEnd).trim());
		tminHere = Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd,tminEnd).trim());
		rainHere = Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd,rainEnd).trim());

		// now comes the fun part, deciding what to write down...
		monthlySW[storageMonthIndex].useDoubleValue(sradHere);
		monthlyTmax[storageMonthIndex].useDoubleValue(tmaxHere);
		monthlyTmin[storageMonthIndex].useDoubleValue(tminHere);
		monthlyPrec[storageMonthIndex].useDoubleValue(rainHere);


	    } // for lineIndex

	// Beware the MAGIC NUMBER!!! the number of variables being reported on
	int nVariablesReportedOn = 4;
	double[] outDoubleArray = new double[numberOfMonthsToBeSummarized * nVariablesReportedOn];
	
	// we will just go SW/Tmax/Tmin/Prec
	int storageIndex = 0; // start at zero

	// shortwave
	for (int monthIndex = 0; monthIndex < numberOfMonthsToBeSummarized; monthIndex++) {
		  outDoubleArray[storageIndex++] = monthlySW[monthIndex].getMean();
		}
	
	// tmax
	for (int monthIndex = 0; monthIndex < numberOfMonthsToBeSummarized; monthIndex++) {
		  outDoubleArray[storageIndex++] = monthlyTmax[monthIndex].getMean();
		}
		
	// tmin
	for (int monthIndex = 0; monthIndex < numberOfMonthsToBeSummarized; monthIndex++) {
		  outDoubleArray[storageIndex++] = monthlyTmin[monthIndex].getMean();
		}
		
	// prec
	for (int monthIndex = 0; monthIndex < numberOfMonthsToBeSummarized; monthIndex++) {
	  outDoubleArray[storageIndex++] = monthlyPrec[monthIndex].getTotalDouble();
	}
	
	return outDoubleArray;

    }
    
    
    public static void main(String commandLineOptions[]) throws Exception {

	TimerUtility bigTimer = new TimerUtility();

	////////////////////////////////////////////
	// handle the command line arguments...
	////////////////////////////////////////////

	System.out.print("command line arguments: \n");
	bigTimer.tic();
	for (int i = 0; i < commandLineOptions.length; i++) {
	    System.out.print(i + " " + commandLineOptions[i] + " " + bigTimer.tocNanos() + " ns\n");
	    bigTimer.tic();
	}
	System.out.println();


	
	
	
	
	
	
	
	boolean doThis = false;

	
	
	DescriptiveStatisticsUtility realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
	
	realYieldsEntirePixel.useLongValue(5);
	realYieldsEntirePixel.useLongValue(6);
	realYieldsEntirePixel.useLongValue(7);
	
	DescriptiveStatisticsUtility copyOfThing = realYieldsEntirePixel;
	System.out.println("copy: " + copyOfThing.getAllPretty());
	System.out.println("original with changes: " + realYieldsEntirePixel.getAllPretty());

	realYieldsEntirePixel.useLongValue(8);
	realYieldsEntirePixel.useLongValue(9);

	System.out.println("copy: " + copyOfThing.getAllPretty());
	System.out.println("original with changes: " + realYieldsEntirePixel.getAllPretty());
	
	
	
	

	if (doThis) {
	
	    
	    
	    
	int[] dayNumberYearPair = { 1 , 1851};
	
	int yearHere = dayNumberYearPair[0] - (dayNumberYearPair[0]/100)*100;
	
	int dateHere = (dayNumberYearPair[0] - (dayNumberYearPair[0]/100)*100) + 1000 * dayNumberYearPair[1];
	    
	System.out.println("yearHere = " + yearHere);
	    System.out.println("dateHere = " + dateHere);
	    
	    
	String initialCharacters = "lpayp";
	
	int nCharacters = initialCharacters.length();
	
	char[] testCase = new char[nCharacters];
	
	int combonumber = 1;
for (int i1 = 0 ; i1 < nCharacters ; i1++) {
    for (int i2 = 0 ; i2 < nCharacters ; i2++) {
	if (i2 == i1) { continue; }
	for (int i3 = 0 ; i3 < nCharacters ; i3++) {
		if (i3 == i1 || i3 == i2 ) { continue; }
	    for (int i4 = 0 ; i4 < nCharacters ; i4++) {
		if (i4 == i1 || i4 == i2 || i4 == i3 ) { continue; }
		for (int i5 = 0 ; i5 < nCharacters ; i5++) {
			if (i5 == i1 || i5 == i2 || i5 == i3 || i5 == i4 ) { continue; }

			System.out.println(
				initialCharacters.substring(i1, i1 + 1) + 
				initialCharacters.substring(i2, i2 + 1) + 
				initialCharacters.substring(i3, i3 + 1) + 
				initialCharacters.substring(i4, i4 + 1) + 
				initialCharacters.substring(i5, i5 + 1) +
				"   #" + combonumber
				);
		    
			combonumber++;
		    
		}
	    }
	}
    }
}
	
	
	
	}
	
	if (7 == 8) {


	String weatherFileName = "C:\\rdrobert\\breaking_DSSAT\\RRRR.WTH";
	String[] weatherFileContents = org.R2Useful.FunTricks.readTextFileToArray(weatherFileName);
	
	int howManyToSummarize = 2;
	int plantingDate = 3120;
	
	double[] outputStuff = null;
	
	outputStuff = monthlyWeatherAveragesDuringSeason(weatherFileContents, plantingDate, howManyToSummarize);
	
	for (int readIndex = 0; readIndex < outputStuff.length; readIndex++) {
	    System.out.println("[" + readIndex + "] = " + outputStuff[readIndex]);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	IrrigationScheme testIrrigationScheme = new IrriSRiceSomeDrySpellsB();
	
	String codedRiceStartString      = "codedRiceCSV ";
	
	String irrigationSchemeToUse = "codedRiceCSV 4,100,10,20";
	
	    String csvPairsOfDaysAndDepths = irrigationSchemeToUse.replaceFirst(codedRiceStartString, "");

	    System.out.println();
	    System.out.println("custom rice irrigation scheme, found this (hopefully) CSV: [" + csvPairsOfDaysAndDepths + "]");
	    System.out.println();

	    String[] pairsOfDaysAndDepthsArray = csvPairsOfDaysAndDepths.split(",");
	    int nPairs = pairsOfDaysAndDepthsArray.length / 2;
	    
	    int[][] daysColOneDepthColTwoFloodArray = new int[nPairs][2];

	    System.out.println("nPairs = " + nPairs);
	    
	    for (int pairIndex = 0; pairIndex < nPairs; pairIndex++) {
		System.out.println("pairIndex = " + pairIndex);
		
		int dayValue   = Integer.parseInt(pairsOfDaysAndDepthsArray[pairIndex * 2]);
		int depthValue = Integer.parseInt(pairsOfDaysAndDepthsArray[pairIndex * 2 + 1]);

		System.out.println("dayValue = "   + dayValue);
		System.out.println("depthValue = " + depthValue);
		
		daysColOneDepthColTwoFloodArray[pairIndex][0] = dayValue;
		daysColOneDepthColTwoFloodArray[pairIndex][1] = depthValue;
	    }
	    
	    Object[] objectArrayForIrrigationScheme = new Object[] { daysColOneDepthColTwoFloodArray };
	    testIrrigationScheme.specialPurposeMethod(objectArrayForIrrigationScheme);

	    testIrrigationScheme.initialize(51001, 1);
	    System.out.println(testIrrigationScheme.buildIrrigationBlock());
	

	
	
	

	    int nDecimalsHere = 5;
	double theValue = 1000.0;

	String original = FunTricks.onlySomeDecimalPlacesKeepTrailingZeros(theValue, nDecimalsHere);
	String simplified = FunTricks.onlySomeDecimalPlaces(theValue, nDecimalsHere);
	
	
	System.out.println("theValue   =  " + theValue + "; with " + nDecimalsHere);
	System.out.println("original   = [" + original + "]");
	System.out.println("simplified = [" + simplified + "]");
	String fitIn = FunTricks.fitInNCharacters(theValue, nDecimalsHere);
	System.out.println("fit in     = [" + fitIn + "]");
	
	
	NitrogenOnlyFertilizerScheme testScheme = new FSZeroItOut();
	testScheme.initialize();

	System.out.println(testScheme.buildNitrogenOnlyBlock(5, 7, 200));

	System.out.println("before");
	Thread.sleep(5000);
	System.out.println("after");

	
	    int magicDSSATSummaryLineIndexToRead = 4;
//	    String magicDSSATSummaryToReadPath = "C:\\rdrobert\\breaking_DSSAT\\dssat_and_emissions\\Summary_OLD.OUT";
	    String magicDSSATSummaryToReadPath = "C:\\rdrobert\\breaking_DSSAT\\dssat_and_emissions\\Summary_N2O.OUT";

	
	

	// declarations with initializations
	// we only need to read the first few lines, specifically, we want to get to the real header line:
	// *SUMMARY : QUKY3114sq Ki
	//
	// !IDENTIFIERS.......
	// @   RUNNO   TRNO R# O# C#
	// so, we need to read to the fourth line...
	int nLinesToRead = magicDSSATSummaryLineIndexToRead;
	String[] candidateSummaryContents = new String[nLinesToRead];

	    candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
	
	// ok, so we want to look at the header line and go through and find the beginnings of all the key words...
	// the first "word" is really that silly @-sign, so we want to skip that...
	// but, we should be able to do my fancy multi-space-tolerant split
	String[] splitUpNames = FunTricks.readLineToArrayOnArbitrarySpaceOrTab(candidateSummaryContents[magicDSSATSummaryLineIndexToRead - 1].substring(1));

	for (int nameIndex = 0; nameIndex < splitUpNames.length; nameIndex++) {
	    System.out.println("sUN[" + nameIndex + "] = [" + splitUpNames[nameIndex] + "]");
	}
	
	int[] endingIndicesForSplitUpNames = new int[splitUpNames.length];
	
	String testExtraction = null;
	
	int previousStartIndex = 1; // this actually needs to be one; skipping the "@"
	for (int nameIndex = 0; nameIndex < splitUpNames.length; nameIndex++) {
	    endingIndicesForSplitUpNames[nameIndex] =
		    candidateSummaryContents[magicDSSATSummaryLineIndexToRead - 1].indexOf(splitUpNames[nameIndex], previousStartIndex)
		    + splitUpNames[nameIndex].length();

	    int endThis = -5;
	    int endPrevious = -6; 
	    int lengthThis = -7;

	    if (nameIndex == 0) {
		    endThis = endingIndicesForSplitUpNames[nameIndex];
		    endPrevious = 0; 
		    lengthThis = endThis - endPrevious;
	    } else {
		endThis = endingIndicesForSplitUpNames[nameIndex];
		endPrevious = endingIndicesForSplitUpNames[(nameIndex-1)]; 
		lengthThis = endThis - endPrevious;
	    }
	    System.out.println("- " + nameIndex + " - eP=" + endPrevious + "  eT=" + endThis + "  lT=" + lengthThis);

	    testExtraction = candidateSummaryContents[magicDSSATSummaryLineIndexToRead - 1].substring(
		    endPrevious, endThis
		    );
	    System.out.println("sUN[" + nameIndex + "] = [" + splitUpNames[nameIndex] + "] beginning at [" + endPrevious + "]");
	    System.out.println("{" + testExtraction + "}");

	    
	}
	

	
    
	
	
	}	
	
	
	
	

	
	
	if (5 == 6) {

		FSPotatoes3 junk = new FSPotatoes3();
		
		junk.initialize();
		
		double fertilizerAmount = 901;
		
		System.out.println(fertilizerAmount + " -> \n" + junk.buildNitrogenOnlyBlock(5, 7, fertilizerAmount));
		

	int[] zeroLengthTry = new int[0];
	
	System.out.println("should be zero: " + zeroLengthTry.length);

	
	double resolution = 5.0/60.0;
	
	double rawLongitude = -120.1549999999;
	
	double positiveNegativeMultiplier = 0;
	double boxIndex = Double.NaN;
	if (rawLongitude < 0) {
	    boxIndex = Math.ceil(rawLongitude / resolution);
	    positiveNegativeMultiplier = -1;
	} else {
	    boxIndex = Math.floor(rawLongitude / resolution);
	    positiveNegativeMultiplier = 1;
	}
	
	double longitudeToUse = positiveNegativeMultiplier * resolution / 2 + boxIndex * resolution;
	
	System.out.println("resolution =\t" + resolution);
	System.out.println("rawLongitude =\t" + rawLongitude);
	System.out.println("boxIndex =\t" + boxIndex);
	System.out.println("longitudeToUse =\t" + longitudeToUse);
	
	

	NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
	nitrogenFertilizerScheme = new FSMiddleHeavyThreeSplitWithFlowering();
	nitrogenFertilizerScheme.initialize();
	System.out.println(nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
		    120, 140, 21
		    ));

	nitrogenFertilizerScheme = new FSRice();
	nitrogenFertilizerScheme.initialize();
	System.out.println(nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
		    120, 140, 21
		    ));

	
	
	
	



		int startingDate = 2005;
		for (int moreDays = -370-365 ; moreDays <= -360-365 ; moreDays++) {

		
		System.out.println("start = [" + startingDate + "] more days = [" + moreDays + "]; new date = [" +
			DSSATHelperMethods.padWithZeros(DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDate, moreDays),5) + "]"
			);
		}



	    String[] zeroLength = new String[0];
	    String[] nullArray = null;

	    System.out.println("zeroLength.length = [" + zeroLength.length + "]");
	    //		System.out.println("nullArray.length = [" + nullArray.length + "]");

	    String uglyLine = "       20      1  1  1  0 SB CRGRO045 RI                        GRID     RRRR.WTH HN_GEN0010 2024155 2024244 2024286     -99     -99 2024303    56 *******       0       0       0     0  0.0000     0     0.0 0.000   0.0     0     0  1001   253     1   252   126   523     0     1     0     1     0    26    34     1     0   -99   -99   -99   -99   -99   -99   -99   -99     0  31890  31890      0      0  227848  227848***************************    -99.0     0.00     0.00     0.00   -99.00    -99.0    -99.0    -99.0    -99.0    59  12.4   4.5  15.9 12.43  369.0  230.4  140.6";

	    String[] splitLineHere = FunTricks.parseRepeatedDelimiterToArray(uglyLine, " ");

	    for (int splitIndex = 0; splitIndex < splitLineHere.length; splitIndex++) {
		System.out.println("[" + splitIndex + "] = [" + splitLineHere[splitIndex] + "]");
	    }

	    String aLongString = "abc";
	    String regExTry = "[abc]";
	    System.out.println("[" + aLongString + "] tested with [" + regExTry + "] gives us [" + aLongString.matches(regExTry) + "]");


	    double funnyValue = 12345678901234567890.012345; // -9.999999906077472;
	    //		double funnyValue = -9.999999996077472; // -9.999999906077472;
	    //		double funnyValue = -9999.999999996077472; // -9.999999906077472;
	    //		double funnyValue = 99999.999999996077472; // -9.999999906077472;
	    double stepsize = -1E-6;
	    String fiveCharacterVersion = null;

	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");

	    funnyValue = -2.456;
	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");

	    funnyValue = 12.3456;
	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
	    System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");

	    //		while (true) {
	    //
	    //			fiveCharacterVersion = FunTricks.fitInNCharacters(funnyValue, 5);
	    ////			if (fiveCharacterVersion.length() != 5) {
	    //				if (true) {
	    //				System.out.println("[" + funnyValue + "] -> [" + fiveCharacterVersion + "]");
	    //				System.out.println("[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersNew(funnyValue, 5) + "] (new)");
	    //			}
	    //			funnyValue += stepsize;
	    //		}


	}


	if (false) {
	    NitrogenOnlyFertilizerScheme fertScheme = new org.DSSATRunner.FSRice();
	    fertScheme.initialize();
	    System.out.println(fertScheme.buildNitrogenOnlyBlock(100, 1, 4001));

	    double numberthing = Double.POSITIVE_INFINITY;
	    System.out.println("the original number is [" + numberthing + "]");

	    int nDecimals = 3;
	    System.out.println("# of decimals = " + nDecimals + "; value = [" + FunTricks.onlySomeDecimalPlaces(numberthing, nDecimals) + "]");

	    File directoryTry = new File("d:\\funkytest\\");

	    File[] inputFiles = directoryTry.listFiles();

	    for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {
		System.out.println("[" + fileIndex + "] " + inputFiles[fileIndex].getAbsolutePath());
	    }

	    DrawFromCDF drawObject = new DrawFromCDF("D:\\rdrobert\\zambia_household_geographic\\text\\first_toy_elevation_quantiles.txt",":");

	    drawObject.setRandomSeed(59);

	    //		drawObject.dumpcheck();

	    int nRepetitions = 1000;
	    for (int repetitionIndex = 0; repetitionIndex < nRepetitions ; repetitionIndex++){
		System.out.println(drawObject.provideSingleDraw());
	    }




	    String lineWithExtraSpaces = "zero one  two   three    four";
	    String[] splitLine = lineWithExtraSpaces.split(" ");

	    System.out.println("original = [" + lineWithExtraSpaces + "]");
	    for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
		System.out.println("[" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
	    }

	    // now count up the number of non-empties
	    int nValid = 0;
	    for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
		if (!splitLine[splitIndex].isEmpty()) {
		    System.out.println("FULL: [" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
		    nValid++;
		}
	    }
	    String[] onlyValid = new String[nValid];
	    int storageIndex = 0;
	    for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
		if (!splitLine[splitIndex].isEmpty()) {
		    onlyValid[storageIndex] = splitLine[splitIndex];
		    storageIndex++;
		}
	    }

	    for (int splitIndex = 0; splitIndex < nValid; splitIndex++) {
		System.out.println("[" + splitIndex + "] = [" + onlyValid[splitIndex] + "]");
	    }

	}
	if (false) {
	    SystemCallWithTimeout scwt = new SystemCallWithTimeout();

	    String[] commandToUse = {"c:\\windows\\system32\\notepad.exe"};
	    java.io.File workingDirectoryToUse = new java.io.File("C:\\");
	    //		java.io.File workingDirectoryToUse = null;
	    int sleepTimeMillisToUse = 3000;
	    int testInterval = 50;

	    scwt.setup(commandToUse, workingDirectoryToUse, sleepTimeMillisToUse, testInterval);

	    System.out.println("before run: completion status = " + scwt.finishedCleanly());

	    scwt.run();

	    System.out.println("after start: completion status = " + scwt.finishedCleanly());

	    System.out.println("after waitfor?: completion status = " + scwt.finishedCleanly());

	    //		System.out.println(DSSATHelperMethods.yearDayToYYDDD(0,1));		
	}





    } // main

}

