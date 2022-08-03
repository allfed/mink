package org.ifpri_converter;
//import java.util.Date;

import org.R2Useful.*;

import java.io.*;

import ucar.nc2.*;
import ucar.ma2.*;



//import java.util.concurrent.*;

public class IsimipNetcdfDailyToDSSATDailyMultipleFiles_SPEEDTEST {


    static private boolean isYearNumberALeapYear(int yearNumber) {

	// first, we need to check for leap-year-ness.
	boolean isLeapYear = false;
	// check for the 400 thing
	if (yearNumber % 400 == 0) {
	    isLeapYear = true;
	} else if (yearNumber % 100 == 0) {
	    isLeapYear = false;
	} else if (yearNumber % 4 == 0) {
	    isLeapYear = true;
	} 
	// unneeded else...
	//	else {
	//	    isLeapYear = false;
	//	}
	return isLeapYear;
    }

    static private int monthIndexFromDayNumberGregorian(int dayOfYearNumber, int yearNumber) {

	boolean isLeapYear = isYearNumberALeapYear(yearNumber);

	if (!isLeapYear) {
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
	} else {
	    if (dayOfYearNumber <= 31) {
		return 0; // January = 0 in this case because we will use it for array indices...
	    } else if (dayOfYearNumber <= 60) {
		return 1; // February
	    } else if (dayOfYearNumber <= 91) {
		return 2; // March
	    } else if (dayOfYearNumber <= 121) {
		return 3; // April
	    } else if (dayOfYearNumber <= 152) {
		return 4; // May
	    } else if (dayOfYearNumber <= 182) {
		return 5; // June
	    } else if (dayOfYearNumber <= 213) {
		return 6; // July
	    } else if (dayOfYearNumber <= 244) {
		return 7; // August
	    } else if (dayOfYearNumber <= 274) {
		return 8; // September
	    } else if (dayOfYearNumber <= 305) {
		return 9; // October
	    } else if (dayOfYearNumber <= 335) {
		return 10; // November
	    } else {
		return 11; // December
	    }
	}
    }

    static private int[] dayOfYearNumberFromGiantDayNumber(int dayNumberInSeries, int firstDayNumberInSeries, int dayOfYearForFirstDay, int yearNumberForFirstDay) {

	// basically, the "times" seem to be these huge day numbers with some obscure zero-day. basically, i am
	// going to brute force disentangle them so i can get a gregorian year with a day of the year by starting
	// with what i think the first day written down is and then building up to the day i want...

	// surely, there are more efficient ways of doing it, but at this point i don't think that it will be terribly
	// slow and it is more important to avoid logic errors than to avoid slowness.


	// first some idiot checking
	if (dayNumberInSeries < firstDayNumberInSeries) {
	    System.out.println("\tProblem in dayOfYearNumberFromGiantDayNumber: asking for day before start: day requested (dayNumberInSeries) = " 
		    + dayNumberInSeries + "; firstDayNumberInSeries = " + firstDayNumberInSeries);
	    return new int[] {Integer.MIN_VALUE,Integer.MIN_VALUE};
	}

	// look at the very first year to see if we are in a leap year
	int daysInSimpleYear = 365;

	// i feel like doing repeated brute force to slice off year's of days to get ourselves
	// back to a day number less than 366 or 365...

	// or, maybe we should do it constructively and count up until we hit the day number we want... that will
	// probably make more sense and be easier to debug...

	// how many more days do we need to go?
	int daysBeyondFirstDayWeNeed = dayNumberInSeries - firstDayNumberInSeries; // the +1 to start counting at 1.

	// initialize with the known dayOfYearForFirstDay
	int constructiveDayOfYearNumber = dayOfYearForFirstDay;
	int constructiveYearNumber = yearNumberForFirstDay;

	boolean constructiveDayIsInLeapYear = false;

	// and let's be crazy and take one day at a time... and use an ugly approach with a for loop and a break statement
	for (int extraDayIndex = 0; extraDayIndex < daysBeyondFirstDayWeNeed; extraDayIndex++) {
	    // ok, add the day.
	    constructiveDayOfYearNumber++;

	    // and check if we have run off the end of a year...
	    if (constructiveDayOfYearNumber > daysInSimpleYear) {
		constructiveDayIsInLeapYear = isYearNumberALeapYear(constructiveYearNumber);

		// if we 
		if ((!constructiveDayIsInLeapYear) || (constructiveDayOfYearNumber > daysInSimpleYear + 1)) {
		    // ran off the end of a year: reset
		    constructiveDayOfYearNumber = 1; // back to january
		    constructiveYearNumber++; // and bump up the year
		} else if (constructiveDayOfYearNumber > daysInSimpleYear + 1) {
		    // ran off the end of a leap year

		}
	    } // possibly too long
	}

	return new int[]{constructiveDayOfYearNumber , constructiveYearNumber};
    }

//    static private int censorDaysToReasonableRange(int dayNumber, int yearNumber) {
//	if (dayNumber < 1) {
//	    System.out.println("Warning: requested day is not positive, resetting to one: " + dayNumber + " -> 1");
//	    return 1;
//	} else if(isYearNumberALeapYear(yearNumber)) {
//	    if (dayNumber > 366) {
//		System.out.println("Warning: requested day is beyond end of year [" + yearNumber + 
//			"], resetting to end: " + dayNumber + " -> 366 (leap)");
//		return 366;
//	    } else {
//		return dayNumber;
//	    }
//	} else {
//	    if (dayNumber > 365) {
//		System.out.println("Warning: requested day is beyond end of year [" + yearNumber + 
//			"], resetting to end: " + dayNumber + " -> 365 (normal)");
//		return 365;
//	    } else {
//		return dayNumber;
//	    }
//	}
//
//    }

    static private Array readAChunk(Variable theRaster, String variableToPull, String rangeToReadAsString) throws IOException, InvalidRangeException {

	Array dataOnlySome = null;

	// try to read it
	try {
	    dataOnlySome = 
		    theRaster.read(
			    rangeToReadAsString);
	} catch (IOException ioe) {
	    System.out.println("trying to read " + variableToPull + "   " + ioe);
	    throw ioe;

	} catch (InvalidRangeException e) {
	    System.out.println("invalid Range for " + variableToPull + "  " + e);
	    throw e;
	}

	return dataOnlySome;

    }

    public static void main(String commandLineOptions[]) throws Exception {





	////////////////////////////////////////////
	// handle the command line arguments...
	////////////////////////////////////////////

	System.out.print("command line arguments: ");
	for (int i = 0; i < commandLineOptions.length; i++) {
	    System.out.println("[" + i + "] = [" + commandLineOptions[i] + "]");
	}
	System.out.println();


	System.out.println("WARNING: the threshold is applied after the value is scaled (not to the raw value)");

	// hacking from http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/NetcdfFile.html

	// specify the file to deal with, we'll move this to command line eventually
	//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_wheat_apprate_fill_NPK_0.5.nc4";
	//    String outputFileName = inputFileName + ".txt";
	//    int nTotalChecks = 100; // how many updates we want to see...


	String initFileName = commandLineOptions[0];

	// ok, so the new plan is to use in init file to take all the goodies
	// basically, we'll have some of these formerly command line options as the first few lines
	// and then we'll put in the collection of which netcdf files to read and which years from each and stuff
	// so that we can aggregate for time intervals going across files

	System.out.println(initFileName);
	String[] initFileContents = FunTricks.readTextFileToArray(initFileName);


	int inputIndex = 0; // to let me do the ++ thing... 

	// here are the settings common to this whole deal...
	String outputDirectory =                     initFileContents[inputIndex++]; // magic name of the variable we are trying to get
	String outputPrefix =                        initFileContents[inputIndex++]; // magic name of the variable we are trying to get
	String inputFront =                          initFileContents[inputIndex++];
	String inputBack  =                          initFileContents[inputIndex++];
	String inputSuffix  =                        initFileContents[inputIndex++];

	String scenarioTagRequested  =               initFileContents[inputIndex++];

	String rainfallInfix  =                          initFileContents[inputIndex++];
	String sunshineInfix  =                      initFileContents[inputIndex++];
	String highTemperatureInfix  =               initFileContents[inputIndex++];
	String lowTemperatureInfix  =                initFileContents[inputIndex++];

	String startFinishPairsForValidFilesCSV =       initFileContents[inputIndex++];

	int maxNumberOfDaysToLoadAtOnce = Integer.parseInt(initFileContents[inputIndex++]);
	
	int firstDay  =                Integer.parseInt(initFileContents[inputIndex++]); // how many latitudes/rows we expect to see
	int firstYear =                Integer.parseInt(initFileContents[inputIndex++]); // how many latitudes/rows we expect to see
	int lastDay   =                Integer.parseInt(initFileContents[inputIndex++]); // how many latitudes/rows we expect to see
	int lastYear  =                Integer.parseInt(initFileContents[inputIndex++]); // how many latitudes/rows we expect to see


	int expectedNRows =                Integer.parseInt(initFileContents[inputIndex++]); // how many latitudes/rows we expect to see
	int expectedNCols =                Integer.parseInt(initFileContents[inputIndex++]); // how many longitudes/cols we expect to see
	int nTotalChecks =                 Integer.parseInt(initFileContents[inputIndex++]); // how many updates we want to see...
	int lastHistoricalYear =           Integer.parseInt(initFileContents[inputIndex++]); // how many updates we want to see...



	// interpret the valid years pairs
	String[] yearsPairsAsSingles = startFinishPairsForValidFilesCSV.split(",");
	if (Math.floorMod(yearsPairsAsSingles.length, 2) != 0) {
	    System.out.println("yearsPairsAsSingles has an odd number of entries [" + yearsPairsAsSingles.length + "]");
	}

	// Bewware the MAGIC NUMBER!!!
	// the first 2 is because we want the number of pairs based on the number of elements, so half as many
	// the second 2 is because we want a start year column and a finish year column
	int[][] validYearPairs = new int[yearsPairsAsSingles.length / 2][2];

	System.out.println("startFinishPairsForValidFilesCSV = [" + startFinishPairsForValidFilesCSV + "]");

	for ( int pairIndex = 0 ; pairIndex < yearsPairsAsSingles.length/2 ; pairIndex++) {
	    // the starting year singles index will be the number of elements that came before
	    System.out.println("pairIndex = " + pairIndex);
	    validYearPairs[pairIndex][0] = Integer.parseInt(yearsPairsAsSingles[pairIndex * 2]);
	    // and the ending year will be one more...
	    validYearPairs[pairIndex][1] = Integer.parseInt(yearsPairsAsSingles[pairIndex * 2 + 1]);

	    // idiot checking... within the pair, they should be in a good order
	    if (validYearPairs[pairIndex][0] > validYearPairs[pairIndex][1]) {
		System.out.println("PROBLEMS: the beginning is after the end in a year pair: " + pairIndex + " has start = " + validYearPairs[pairIndex][0] + " and end = " + validYearPairs[pairIndex][1]);
	    }

	    // idiot checking... between pairs, they should have sequential years....
	    if (pairIndex > 0) {
		if (validYearPairs[pairIndex - 1][1] + 1 != validYearPairs[pairIndex][0]) {
		    System.out.println("PROBLEMS: pairs do not have sequential year numbers. pairIndex " + (pairIndex - 1) + " ends on " + validYearPairs[pairIndex - 1][1] + " while pairIndex " + pairIndex + " starts with " + validYearPairs[pairIndex][0]);
		} // if not sequential
	    } // if pairIndex > 0
	} // pairIndex



	// magic numbers
	final float kelvinToCelsiusAdditiveShifter = -273.15F; // add this to kelvin to get celsius
	final float rainfallToMMperDayMultiplier = 24 * 3600; // multiply the rainfall by this to get from kg/m^2/s to mm/day
	// based on http://esg.pik-potsdam.de/esgf-web-fe/metadataview/isimip-ft.input.hadgem2-es.historical.day.prAdjust.v20130913|esg.pik-potsdam.de.html,
	// "pr" appears to be kg/m^2/s or mm/s so we will need to multiply by 3600 * 24
	
	final String historicalScenarioTag = "historical";
	final String timeName = "time";
	final String latitudeName = "lat";
	final String longitudeName = "lon";
	final int zeroInt = 0;
	final int oneInt = 1;
	final float giantValueIndicatingMissing = 1.0E20F; // this is apprehended from looking at the data (as opposed to the documentation)

	final float tDiffToEnforce  = 0.1F; // DSSAT will yell when tmax <= tmin, so i just force them to be different...
//	final String spacePARspaceCO2DasEMPTY = "  -999  -999"; // with missing value kinds of things
	final String spacePARspaceCO2DasEMPTY = ""; // or just leave it blank and dssat does not seem to complain
	
	
//	final String labelLineForWeather = "@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D";
	final int magicWidthForTextOutput = 5;
	final int nLinesInHeader = 5;
	final int nMonths = 12;



	// initializations
	

	
	String scenarioTagToUse = null;
	int yearToTryToLoad = -1;

	int nYearPairs = -7;

//	String infixToUse          = null;
	//	String variableToPull      = null;
	String sunshineNetCDFforFindingValidLocations = null;
	String sunshineNetCDFToUse = null;
	String rainfallNetCDFToUse = null;
	String highTemperatureNetCDFToUse = null;
	String lowTemperatureNetCDFToUse = null;

	//	String inputFileName = null;

	String outputFileName = null;
	String notesFileName = null;


	// stuff for the status checks
	TimerUtility funnyTimer = new TimerUtility();
	TimerUtility tightTimer = new TimerUtility();

	Variable theTime = null;
	Dimension timeDimension = null;
	int timeLength = -1;

	Variable theLatitude = null;
	Dimension latitudeDimension = null;
	int latitudeLength = -1;

	Variable theLongitude = null;
	Dimension longitudeDimension = null;
	int longitudeLength = -1;


	int firstDayInThisSeries = -234;

	Variable theSunshineRasterForFindingValidLocations = null;
	Variable theSunshineRaster = null;
	Variable theRainfallRaster = null;
	Variable theHighTemperatureKelvinRaster = null;
	Variable theLowTemperatureKelvinRaster = null;



	Array      timeData = null;
//	Array  latitudeData = null;
//	Array longitudeData = null;
	//	Index3D readIndex = null;
//	ArrayFloat rasterData = null;

//	Array latitudeOnlySome = null;
//	Array longitudeOnlySome = null;
//	Array dayOnlySome = null;

//	Array dataOnlySome = null;

	Array sunshineOnlySome = null;
//	Array sunshineAlternate = null;
	Array rainfallOnlySome = null;
	Array highTemperatureKelvinOnlySome = null;
	Array lowTemperatureKelvinOnlySome = null;

	float thisLatitude = Float.POSITIVE_INFINITY;
	float thisLongitude = Float.POSITIVE_INFINITY;
//	float thisTime = Float.POSITIVE_INFINITY;
	// i want to start with time period zero and then longitude #0 and then all the latitudes

	// initialize...
	String rangeToReadAsString = null;
	float sunshineValueHereForFindingValidLocations    = Float.POSITIVE_INFINITY;

	float sunshineHere = Float.NEGATIVE_INFINITY;
	float rainfallHere = Float.NEGATIVE_INFINITY;
	float highTemperatureHereCelsius = Float.NEGATIVE_INFINITY;
	float lowTemperatureHereCelsius = Float.NEGATIVE_INFINITY;
	float tradingSpot = Float.POSITIVE_INFINITY;

	int dateHere = Integer.MIN_VALUE;
	int timeHere = Integer.MIN_VALUE;
	int monthIndexHere = Integer.MIN_VALUE;
//	int dayOfYearHere = Integer.MIN_VALUE;
//	int yearHere = Integer.MIN_VALUE;

	int[] dayNumberYearPair = null;

//	DescriptiveStatisticsUtility sunshineStatUtility = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility rainfallStatUtility = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility highTemperatureStatUtility = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility lowTemperatureStatUtility  = new DescriptiveStatisticsUtility(true);

//	DescriptiveStatisticsUtility annualTave = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility annualTmax = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility annualTmin = new DescriptiveStatisticsUtility(true);
//	DescriptiveStatisticsUtility[] monthlyTmax = new DescriptiveStatisticsUtility[nMonths];
//	DescriptiveStatisticsUtility[] monthlyTmin = new DescriptiveStatisticsUtility[nMonths];

	DescriptiveStatisticsUtility[][] annualTave = null;
	DescriptiveStatisticsUtility[][] annualTmax = null;
	DescriptiveStatisticsUtility[][] annualTmin = null;
	DescriptiveStatisticsUtility[][][] monthlyTmax = null;
	DescriptiveStatisticsUtility[][][] monthlyTmin = null;

	

	NetcdfFile sunshineNCFILEobjectForFindingValidLocations = null;
	NetcdfFile sunshineNCFILEobject = null;
	NetcdfFile rainfallNCFILEobject = null;
	NetcdfFile highTemperatureNCFILEobject = null;
	NetcdfFile lowTemperatureNCFILEobject = null;


//	String dailyWeatherForWTHasString = null;
//	String headerForWTH = null;

	String[] outputHeaderAsArray = null;

	String columnsAndNotes = null;

	double highestMonthlyAverage = Double.MIN_VALUE;
	double lowestMonthlyAverage = Double.MAX_VALUE;

	double tav, amp, thisAverageTmax, thisAverageTmin, thisTave;

	String thisDaysWeather = null, fiveDigitDate = null, fiveSpacedSunshine = null, fiveSpacedHighTemperature = null, fiveSpacedLowTemperature = null, fiveSpacedRainfall = null; 

	Index3D readIndex = null;

	int nRoundsOfLoading = -3;

	int nGoodPixels = 0; // initialize a counter
	boolean[][] pixelIsValid = null;
	String[][] outputFileNameArray = null;

	String[] allTheExistingWeather  = null;


	int latitudeIndexToStartWith = 0; // for shortcutting during the debugging process....



	//	Date endTime = null;
	//	double duration = -5;





	// insert the "historical" versus the appropriate ssp/rcp thing
//	yearToTryToLoad = firstYear;
	yearToTryToLoad = validYearPairs[zeroInt][zeroInt];
	    System.out.println("         yearToTryToLoad = " + yearToTryToLoad);
	if (yearToTryToLoad <= lastHistoricalYear) {
	    scenarioTagToUse = historicalScenarioTag;
	} else {
	    scenarioTagToUse = scenarioTagRequested;
	}
	System.out.println("scenarioTagToUse = [" + scenarioTagToUse + "]");




	// define an output file
	notesFileName = outputDirectory + java.io.File.separator + outputPrefix + ".notes.txt";




	// figure out how many file groups we are working with.... we will need this later
	nYearPairs = validYearPairs.length;



	// build up an example netcdf file to load and see what happens...
	// this is what we will use to figure out where the valid pixels are....

	sunshineNetCDFforFindingValidLocations        = inputFront + scenarioTagToUse + "_" + sunshineInfix        + inputBack + validYearPairs[zeroInt][zeroInt] + "_" + validYearPairs[zeroInt][oneInt] + inputSuffix;
	sunshineNCFILEobjectForFindingValidLocations  = NetcdfFile.open(sunshineNetCDFforFindingValidLocations);

	// grab some rudimentary information, work off of sunshine as our archetype
	System.out.println("----- info that we are assuming is valid for all files -----");
	System.out.println(sunshineNCFILEobjectForFindingValidLocations.getDetailInfo());

	// let's see if we can get the time array, dimensions, etc
	theTime = sunshineNCFILEobjectForFindingValidLocations.findVariable(timeName);
	timeDimension = theTime.getDimension(0);
	timeLength = timeDimension.getLength();

	// and the same for the latitude array...
	theLatitude = sunshineNCFILEobjectForFindingValidLocations.findVariable(latitudeName);
	latitudeDimension = theLatitude.getDimension(0);
	latitudeLength = latitudeDimension.getLength();

	// and the latitude array...
	theLongitude = sunshineNCFILEobjectForFindingValidLocations.findVariable(longitudeName);
	longitudeDimension = theLongitude.getDimension(0);
	longitudeLength = longitudeDimension.getLength();

	//	System.out.println("time length = " + timeLength + "; lat length = " + latitudeLength + "; lon length = " + longitudeLength + " [" + inputFileName + "]");
	System.out.println("time length = " + timeLength + "; lat length = " + latitudeLength + "; lon length = " + longitudeLength);

	// instead of reading the whole thing, we want to look at only one location at a time inside a giant pair of for loops..a..
	// let's see if we can get the values array based on the command line option...
	// in this case, it should be the sunshine, but it does not really matter
	// do one more step to get the values directly available eventually
	theSunshineRasterForFindingValidLocations = sunshineNCFILEobjectForFindingValidLocations.findVariable(sunshineInfix);

	// some quick idiot checking
	if (latitudeLength != expectedNRows || longitudeLength != expectedNCols) {
	    System.out.println("grid resolution problem:");
	    System.out.println("expected rows x cols of " + expectedNRows + " x " + expectedNCols);
	    System.out.println("found    rows x cols of " + latitudeLength + " x " + longitudeLength);

	    throw new Exception();
	}


	// later, we will want to know which day number is the very first one...
	// get the time by itself, so we can get the day numbers or whatever....
	timeData =      theTime.read();
	firstDayInThisSeries = timeData.getInt(zeroInt); // actually needs to be zero

	System.out.println("first day in series = " + firstDayInThisSeries);

	// let's just look at the very first day.
	rangeToReadAsString = "0,0:" + (latitudeLength - 1) + ":1,0:" + (longitudeLength - 1) + ":1";

	// read that tiny, singleton chunk
	sunshineOnlySome = readAChunk(theSunshineRasterForFindingValidLocations, sunshineInfix, rangeToReadAsString);
	
	readIndex = new Index3D(new int[] {timeLength, latitudeLength , longitudeLength}); // i'm not sure if those lengths are necessary or not

	nGoodPixels         = 0; // initialize a counter
	pixelIsValid        = new boolean[latitudeLength][longitudeLength];
	System.out.println("pixelIsValid initialization = " + pixelIsValid[0][0]);
	outputFileNameArray = new  String[latitudeLength][longitudeLength];

	annualTave = new DescriptiveStatisticsUtility[latitudeLength][longitudeLength];
	annualTmax = new DescriptiveStatisticsUtility[latitudeLength][longitudeLength];
	annualTmin = new DescriptiveStatisticsUtility[latitudeLength][longitudeLength];

	monthlyTmax = new DescriptiveStatisticsUtility[latitudeLength][longitudeLength][nMonths];
	monthlyTmin = new DescriptiveStatisticsUtility[latitudeLength][longitudeLength][nMonths];

	
	
	tightTimer.tic();
	for (int latitudeIndex = latitudeIndexToStartWith; latitudeIndex < latitudeLength ; latitudeIndex++) {
	    for (int longitudeIndex = 0; longitudeIndex < longitudeLength ; longitudeIndex++) {

		// there should only be a single element here
		sunshineValueHereForFindingValidLocations  = sunshineOnlySome.getFloat(readIndex.set(zeroInt,latitudeIndex,longitudeIndex));


		if (sunshineValueHereForFindingValidLocations < giantValueIndicatingMissing) {
//		    System.out.println("      starting a good location at " + funnyTimer.TOCSeconds() + "s" + funnyTimer.sinceStartMessage());
		    // we have a valid pixel, so let's process it....
		    // count them up
		    nGoodPixels++;
		    
		    // record a flag
		    pixelIsValid[latitudeIndex][longitudeIndex] = true;

		    // initialize the counters....
		    annualTave[latitudeIndex][longitudeIndex] = new DescriptiveStatisticsUtility(true);
		    annualTmax[latitudeIndex][longitudeIndex] = new DescriptiveStatisticsUtility(true);
		    annualTmin[latitudeIndex][longitudeIndex] = new DescriptiveStatisticsUtility(true);

		    for (int monthIndex = 0; monthIndex < nMonths ; monthIndex++) {
			// extract the values
			monthlyTmax[latitudeIndex][longitudeIndex][monthIndex] = new DescriptiveStatisticsUtility(true);
			monthlyTmin[latitudeIndex][longitudeIndex][monthIndex] = new DescriptiveStatisticsUtility(true);
		    } // monthIndex


		    		    // figure out what latitude and longitude we are talking about
		    rangeToReadAsString = Integer.toString(latitudeIndex);
		    thisLatitude = readAChunk(theLatitude,   latitudeName, rangeToReadAsString).getFloat(zeroInt);

		    rangeToReadAsString = Integer.toString(longitudeIndex);
		    thisLongitude = readAChunk(theLongitude, longitudeName, rangeToReadAsString).getFloat(zeroInt);


		    // decide on the output name
		    outputFileNameArray[latitudeIndex][longitudeIndex] = outputDirectory + java.io.File.separator + outputPrefix + "_" + thisLatitude + "_" + thisLongitude + ".WTH";
		    
		    // clear out that file
//		    File clearOutFileObject = new File(outputFileNameArray[latitudeIndex][longitudeIndex]);
		    new File(outputFileNameArray[latitudeIndex][longitudeIndex]).delete();

		} // if found something good
	    } // longitudeIndex
	} // latitudeIndex


	
	
	System.out.println("after lat/long search at " + tightTimer.TOCSeconds() + "s" + funnyTimer.sinceStartMessage());
	System.out.println("nGoodPixels = " + nGoodPixels);

	/////////////////////////////

	for (int yearPairIndex = 0; yearPairIndex < nYearPairs; yearPairIndex++) {
	    System.out.println("                   yearPairIndex=" + yearPairIndex);
	    // insert the "historical" versus the appropriate ssp/rcp thing
	    yearToTryToLoad = validYearPairs[yearPairIndex][zeroInt];
	    System.out.println("         yearToTryToLoad = " + yearToTryToLoad);
	    if (yearToTryToLoad <= lastHistoricalYear) {
		scenarioTagToUse = historicalScenarioTag;
	    } else {
		scenarioTagToUse = scenarioTagRequested;
	    }

	    sunshineNetCDFToUse        = inputFront + scenarioTagToUse + "_" + sunshineInfix        + inputBack + validYearPairs[yearPairIndex][zeroInt] + "_" + validYearPairs[yearPairIndex][oneInt] + inputSuffix;
	    rainfallNetCDFToUse        = inputFront + scenarioTagToUse + "_" + rainfallInfix        + inputBack + validYearPairs[yearPairIndex][zeroInt] + "_" + validYearPairs[yearPairIndex][oneInt] + inputSuffix;
	    highTemperatureNetCDFToUse = inputFront + scenarioTagToUse + "_" + highTemperatureInfix + inputBack + validYearPairs[yearPairIndex][zeroInt] + "_" + validYearPairs[yearPairIndex][oneInt] + inputSuffix;
	    lowTemperatureNetCDFToUse  = inputFront + scenarioTagToUse + "_" + lowTemperatureInfix  + inputBack + validYearPairs[yearPairIndex][zeroInt] + "_" + validYearPairs[yearPairIndex][oneInt] + inputSuffix;


	    // open up the files
	    sunshineNCFILEobject        = NetcdfFile.open(sunshineNetCDFToUse);
	    rainfallNCFILEobject        = NetcdfFile.open(rainfallNetCDFToUse);
	    highTemperatureNCFILEobject = NetcdfFile.open(highTemperatureNetCDFToUse);
	    lowTemperatureNCFILEobject  = NetcdfFile.open(lowTemperatureNetCDFToUse);


	    // do one more step to get the values directly available eventually
	    theSunshineRaster = sunshineNCFILEobject.findVariable(sunshineInfix);
	    theRainfallRaster = rainfallNCFILEobject.findVariable(rainfallInfix);
	    theHighTemperatureKelvinRaster = highTemperatureNCFILEobject.findVariable(highTemperatureInfix);
	    theLowTemperatureKelvinRaster = lowTemperatureNCFILEobject.findVariable(lowTemperatureInfix);

	    // let's see if we can get the time array, dimensions, etc
	    theTime = sunshineNCFILEobject.findVariable(timeName);
	    timeDimension = theTime.getDimension(0);
	    timeLength = timeDimension.getLength();


	    // actually read the new set of times....
	    timeData = theTime.read();
	    
	    System.out.println("timeLength=" + timeLength + " timeData.getShape()[0]=" + timeData.getShape()[0]);


	    // the actual contents consist of the header (with some summary info that has to be built up)
	    // and the daily weather which comes after. so, i need to initialize those two strings as empty


	    // rebuild the range every time because different files can cover differently lengthed time periods
	    // list out all the time steps
	    // the ranges, i think, are start:finish:stepsize
	    // i was from the start to finish, stepsize = 1 for the time, and then only for this location

	    // i want to try to read and entire map for a single timeslice

	    
	    // we want to load only a few at a time so as to not overwhelm the memory....
	    // let's figure out how many to load in each round; that is, our max number until we don't need any more...
	    
	    nRoundsOfLoading = timeLength / maxNumberOfDaysToLoadAtOnce;
	    // see if we hit it perfectly, or if we need that extra round (most likely, yes)
	    if (nRoundsOfLoading * maxNumberOfDaysToLoadAtOnce < timeLength) {
		nRoundsOfLoading++;
	    }
	    
	    // now, write down how many we need to load each time
	    int[] nDaysToLoadByRound = new int[nRoundsOfLoading];
	    for (int roundIndex = 0; roundIndex < nRoundsOfLoading; roundIndex++) {
		if (roundIndex < nRoundsOfLoading - 1) {
		    // we are in the first few, so use the maximum number
		    nDaysToLoadByRound[roundIndex] = maxNumberOfDaysToLoadAtOnce;
		} else {
		    // we are in the last one, so subtract to see how many more we need
		    nDaysToLoadByRound[roundIndex] = timeLength - roundIndex * maxNumberOfDaysToLoadAtOnce;
		} // if, deciding on how many to load

		System.out.println("    round " + roundIndex + " loads " + nDaysToLoadByRound[roundIndex] + " ; starting at " + tightTimer.sinceStartMessage(Double.toString(tightTimer.tocSeconds())));

		//		    rangeToReadAsString = "0:999:1,0:" + (latitudeLength - 1) + ":1,0:" + (longitudeLength - 1) + ":1";
		rangeToReadAsString = (roundIndex * maxNumberOfDaysToLoadAtOnce) + ":" + (roundIndex * maxNumberOfDaysToLoadAtOnce + nDaysToLoadByRound[roundIndex] - 1) + ":1,0:" + (latitudeLength - 1) + ":1,0:" + (longitudeLength - 1) + ":1";


		// do the reading
		tightTimer.tic();

		//		sunshineOnlySome = theSunshineRaster.read(new int[] {0,0,0}, new int[] {timeLength,1,1});
		sunshineOnlySome = readAChunk(theSunshineRaster, sunshineInfix, rangeToReadAsString);
		System.out.println("sunshine took: " + tightTimer.TOCSeconds() + "s and is size " + sunshineOnlySome.getShape()[0]);

		rainfallOnlySome = readAChunk(theRainfallRaster, rainfallInfix, rangeToReadAsString);
		System.out.println("rainfall took: " + tightTimer.TOCSeconds() + "s and is size " + rainfallOnlySome.getShape()[0]);

		highTemperatureKelvinOnlySome = readAChunk(theHighTemperatureKelvinRaster, highTemperatureInfix, rangeToReadAsString);
		System.out.println("highTemp took: " + tightTimer.TOCSeconds() + "s and is size " + highTemperatureKelvinOnlySome.getShape()[0]);

		lowTemperatureKelvinOnlySome  = readAChunk(theLowTemperatureKelvinRaster,  lowTemperatureInfix, rangeToReadAsString);
		System.out.println("lowwTemp took: " + tightTimer.TOCSeconds() + "s and is size " + lowTemperatureKelvinOnlySome.getShape()[0]);


		// i think we need to redo the time thing so that we do not always end up with day #1
//		    // let's see if we can get the time array, dimensions, etc
//		    theTime = sunshineNCFILEobject.findVariable(timeName);
//		    timeDimension = theTime.getDimension(0);
//		    timeLength = timeDimension.getLength();
//
//		    // actually read the new set of times....
//		    timeData = theTime.read();
//		    
//		    System.out.println("timeLength=" + timeLength + " timeData.getShape()[0]=" + timeData.getShape()[0]);

		// i think this is where we would go through all the stuff and add to each file....

		System.out.println("   .... in here, we would go through each location ...");

		
		// actually read the data; we should have a set of maps, so we want to pick a particular location and
		// accumulate up all the weather days in the "round" that we are working on at the moment
		for (int latitudeIndex = latitudeIndexToStartWith; latitudeIndex < latitudeLength ; latitudeIndex++) {
		    for (int longitudeIndex = 0; longitudeIndex < longitudeLength ; longitudeIndex++) {

			// bail out if there is nothing to do here....
			if (!pixelIsValid[latitudeIndex][longitudeIndex]) {
//			    System.out.println("                                skipping    lat " + latitudeIndex + "   lon " + longitudeIndex);
			    continue;
			} 
//			else {
//			    System.out.println("                                DOING IT!!! lat " + latitudeIndex + "   lon " + longitudeIndex);
//			}
			
			// this is the loop over the days. the bounds are determined by how many days are in this round. 
			for (int timeIndex = 0; timeIndex < nDaysToLoadByRound[roundIndex]; timeIndex++) {


			    // we have to add in the number of days dealt with in the previous rounds
			    timeHere = timeData.getInt( (roundIndex * maxNumberOfDaysToLoadAtOnce) + timeIndex);

			    // i am assuming it starts on january 1 of the year in the file name....
			    dayNumberYearPair = dayOfYearNumberFromGiantDayNumber(timeHere, firstDayInThisSeries, oneInt, validYearPairs[yearPairIndex][zeroInt]);

			    // the DSSAT friendly date code as an int
			    // now, we have a four digit year (for the kinds of years we are looking at), so we need to keep only the last two digits of the year....
			    // i am using integer division to do the trimming
			    dateHere = (dayNumberYearPair[1] - (dayNumberYearPair[1]/100)*100) * 1000 + dayNumberYearPair[0];

			    // determine what month this day corresponds to....
			    monthIndexHere = monthIndexFromDayNumberGregorian(dayNumberYearPair[0], dayNumberYearPair[1]);

			    
			    sunshineOnlySome.getFloat(readIndex.set(zeroInt,latitudeIndex,longitudeIndex));

			    try {
			    sunshineHere = sunshineOnlySome.getFloat(readIndex.set(timeIndex,latitudeIndex,longitudeIndex));
			    } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
				System.out.println("something bad is about to happen...");
				System.out.println("timeIndex=" + timeIndex + "  latitudeIndex=" + latitudeIndex + "  longitudeInde=x" + longitudeIndex);
				
				System.out.println("sunshineOnlySome[0] length = " + sunshineOnlySome.getShape()[0] + "  [1] -> " + sunshineOnlySome.getShape()[1] + "  [2] -> " + sunshineOnlySome.getShape()[2]);
				throw aioobe;
			    }
			    rainfallHere = rainfallOnlySome.getFloat(readIndex.set(timeIndex,latitudeIndex,longitudeIndex)) * rainfallToMMperDayMultiplier;
			    highTemperatureHereCelsius = highTemperatureKelvinOnlySome.getFloat(readIndex.set(timeIndex,latitudeIndex,longitudeIndex)) + kelvinToCelsiusAdditiveShifter;
			    lowTemperatureHereCelsius = lowTemperatureKelvinOnlySome.getFloat(readIndex.set(timeIndex,latitudeIndex,longitudeIndex)) + kelvinToCelsiusAdditiveShifter;

			    
//			    sunshineHere = sunshineOnlySome.getFloat(timeIndex);
//			    rainfallHere = rainfallOnlySome.getFloat(timeIndex) * rainfallToMMperDayMultiplier;
//			    highTemperatureHereCelsius = highTemperatureKelvinOnlySome.getFloat(timeIndex) + kelvinToCelsiusAdditiveShifter;
//			    lowTemperatureHereCelsius = lowTemperatureKelvinOnlySome.getFloat(timeIndex) + kelvinToCelsiusAdditiveShifter;

			    
			    
			    
			    if (highTemperatureHereCelsius <= lowTemperatureHereCelsius) {
				// we have a transposition or equality of temperatures.
//				System.out.println("temperatures are whacky: high = " + highTemperatureHereCelsius + " <= " + lowTemperatureHereCelsius + " = low ; lat/lon indices = " + latitudeIndex + "/" + longitudeIndex);

				// now decide what to do
				// if they are the same, we will lower the low, otherwise we will swap them and lower the low if necessary...
				if (highTemperatureHereCelsius == lowTemperatureHereCelsius) {
				    lowTemperatureHereCelsius -= tDiffToEnforce;
				} else {
				    // we must have a pure swap, so swap them...
				    tradingSpot = lowTemperatureHereCelsius;
				    if (Math.abs(highTemperatureHereCelsius - lowTemperatureHereCelsius) < tDiffToEnforce) {
					// they are too close together
					highTemperatureHereCelsius = tradingSpot;
					lowTemperatureHereCelsius = highTemperatureHereCelsius - tDiffToEnforce;
				    } else {
					// they are far enough apart, so just swap
					lowTemperatureHereCelsius = highTemperatureHereCelsius;
					highTemperatureHereCelsius = tradingSpot;
				    } // if too close
				} // if same
			    } // if need to swap


			    // accumulate some averages for the annual overview stuff
			    // accumulate some info about temperatures to come up with the yearly temperature and amplitude
			    annualTmax[latitudeIndex][longitudeIndex].useDoubleValue(highTemperatureHereCelsius);
			    annualTmin[latitudeIndex][longitudeIndex].useDoubleValue(lowTemperatureHereCelsius);
			    annualTave[latitudeIndex][longitudeIndex].useDoubleValue((highTemperatureHereCelsius + lowTemperatureHereCelsius)/2);

			    // this uses indices, so we need to subtract one off the month number
			    monthlyTmax[latitudeIndex][longitudeIndex][monthIndexHere].useDoubleValue(highTemperatureHereCelsius);
			    monthlyTmin[latitudeIndex][longitudeIndex][monthIndexHere].useDoubleValue(lowTemperatureHereCelsius);

			    // convert the important things to text
			    fiveDigitDate = FunTricks.padStringWithLeadingSpaces(Integer.toString(dateHere), magicWidthForTextOutput);

			    fiveSpacedSunshine        = FunTricks.fitInNCharacters(sunshineHere, magicWidthForTextOutput);
			    fiveSpacedHighTemperature = FunTricks.fitInNCharacters(highTemperatureHereCelsius, magicWidthForTextOutput);
			    fiveSpacedLowTemperature  = FunTricks.fitInNCharacters(lowTemperatureHereCelsius, magicWidthForTextOutput);
			    fiveSpacedRainfall        = FunTricks.fitInNCharacters(rainfallHere, magicWidthForTextOutput);

			    thisDaysWeather = fiveDigitDate + " " + fiveSpacedSunshine + " " + fiveSpacedHighTemperature + " " + fiveSpacedLowTemperature + " " + fiveSpacedRainfall + spacePARspaceCO2DasEMPTY; 

			    FunTricks.appendLineToTextFile(thisDaysWeather, outputFileNameArray[latitudeIndex][longitudeIndex], true);

//			    System.out.println(thisDaysWeather);
//			    System.out.println("sunshineHere = " + sunshineHere + " ; high = " + highTemperatureHereCelsius + " ; low = " + lowTemperatureHereCelsius + " ; rain = " + rainfallHere);
//			    System.out.println("--- breaking out for debugging ---");
//			    break;

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

			    // ok, we should be done for the moment, so move on to the next location
			    // after all the pixels are done, we have to go back and put the header up front and re-write the file...


			} // timeIndex
		    } // longitudeIndex
		} // latitudeIndex
	    } // roundIndex
	    // be sure to clean up....
	    sunshineNCFILEobject.close();


	    sunshineNCFILEobject.close();
	    rainfallNCFILEobject.close();
	    highTemperatureNCFILEobject.close();
	    lowTemperatureNCFILEobject.close();
	} // pairIndex

	sunshineNCFILEobjectForFindingValidLocations.close();

	
	// when we are all done, then we need to extract out the overall averages and remake each file with the good stuff at the top....

	System.out.println("      +++++  rejiggering to add headers +++++ at " + tightTimer.TOCSeconds() + "s and " + tightTimer.sinceStartMessage());
	
	for (int latitudeIndex = latitudeIndexToStartWith; latitudeIndex < latitudeLength ; latitudeIndex++) {
	    for (int longitudeIndex = 0; longitudeIndex < longitudeLength ; longitudeIndex++) {

		// check the flag for whether to bother...
		if (!pixelIsValid[latitudeIndex][longitudeIndex]) {
		    continue;
		}
		// figure out what should go in this header
		    // figure out the temperature amplitudes/etc and make the header...

		    outputHeaderAsArray = new String[nLinesInHeader];

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

		    tav = annualTave[latitudeIndex][longitudeIndex].getMean();

		    // but, bad things happen if the tav is below freezing: DSSAT will reset it to 20C or something
		    // i think the idea is to avoid having to deal with the latent heat of fusion/etc.
		    // but 20 >> 1, so i think it is best to reset it to something low...

		    if (tav < tDiffToEnforce) {
			tav = tDiffToEnforce;
		    }

		    // figure out the highest monthly average
		    // figure out the lowest monthly average
		    highestMonthlyAverage = Double.MIN_VALUE;
		    lowestMonthlyAverage = Double.MAX_VALUE;
		    for (int monthIndex = 0; monthIndex < nMonths ; monthIndex++) {
			// extract the values
			thisAverageTmax = monthlyTmax[latitudeIndex][longitudeIndex][monthIndex].getMean();
			thisAverageTmin = monthlyTmin[latitudeIndex][longitudeIndex][monthIndex].getMean();

			// compute the average for this month
			thisTave = (thisAverageTmax + thisAverageTmin) / 2.0;

			// update the candidate max/min monthly values
			// i only care about the value, not the index at this point
			if (thisTave > highestMonthlyAverage) { highestMonthlyAverage = thisTave; }
			if (thisTave < lowestMonthlyAverage)  { lowestMonthlyAverage  = thisTave; }

			//		    System.out.println("this = " + thisTave + "; high = " + highestMonthlyAverage + "; low = " + lowestMonthlyAverage);
		    }

		    // i think i am doing this right
		    // the tamp or amp is very poorly defined. when i chased down the references, the code
		    // was more inspired by than following the book's exposition
		    // anyways, the important thing is for the amplitude to be the full range of the periodic
		    // function rather than a true amplitude. for example, in the source code, it gets divided by two
		    // before multiplying the cosine. but only in some files.... :(
		    //		STEMP.for:      TA = TAV + TAMP * COS(ALX) / 2.0
		    //		STEMP.for:        ST(L) = TAV + (TAMP / 2.0 * COS(ALX + ZD) + DT) * EXP(ZD)
		    //		STEMP.for:      SRFTEMP = TAV + (TAMP / 2. * COS(ALX) + DT)
		    //		STEMP.for:! TAMP     Amplitude of temperature function used to calculate soil 
		    //		STEMP.for:! TAV      Average annual soil temperature, used with TAMP to calculate 

		    amp = highestMonthlyAverage - lowestMonthlyAverage;

		    //		System.out.println("tav = [" + tav + "]" + " [" + FunTricks.fitInNCharacters(tav, 5) + "]");
		    //		System.out.println("amp = [" + amp + "]" + " [" + FunTricks.fitInNCharacters(amp, 5) + "]");

		    outputHeaderAsArray[0] = "*WEATHER DATA :";
		    outputHeaderAsArray[1] = "";
		    outputHeaderAsArray[2] = "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT";
		    outputHeaderAsArray[3] = "  XXXX   " + FunTricks.fitInNCharacters(thisLatitude, 6) + "   " +
			    FunTricks.fitInNCharacters(thisLongitude, 6) + "  -999 " +
			    FunTricks.fitInNCharacters(tav, 5) + " " +
			    FunTricks.fitInNCharacters(amp, 5) + "  -999  -999";
		    outputHeaderAsArray[4] = "@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D";

		
		
		// grab the existing contents of the text file
		allTheExistingWeather = FunTricks.readTextFileToArray(outputFileNameArray[latitudeIndex][longitudeIndex]);
		
		// over-write the old contents with the header info
		FunTricks.writeStringArrayToFile(outputHeaderAsArray, outputFileNameArray[latitudeIndex][longitudeIndex]);
		
		
		// append the original contents with all the days of weather....
		for (int lineIndex = 0; lineIndex < allTheExistingWeather.length; lineIndex++) {
			FunTricks.appendLineToTextFile(allTheExistingWeather[lineIndex], outputFileNameArray[latitudeIndex][longitudeIndex], true);
		}
		
		
		
	    } // longitudeIndex
	} // latitudeIndex
	
	
	

	// some sort of provenance file....
	columnsAndNotes = "";
	// and we want to name the months with numbers, not indices, hence the plus ones...


	//	String variableToPull = initFileContents[inputIndex++]; // magic name of the variable we are trying to get
	//	String rawOutputFileName = initFileContents[inputIndex++];
	//	float additiveShifterForVariable = Float.parseFloat(commandLineOptions[inputIndex++]); // a scale factor so we can rescale on the fly...
	//	float multiplierForVariable = Float.parseFloat(commandLineOptions[inputIndex++]); // a scale factor so we can rescale on the fly...
	//	String valueType      = commandLineOptions[inputIndex++]; // magic type (e.g., average for temperature/sunshine, total and counts for precipitation
	//	String optionalMinimumThresholdString = commandLineOptions[inputIndex++]; // what qualifies as a rainy day
	//	int expectedNRows = Integer.parseInt(commandLineOptions[inputIndex++]); // how many latitudes/rows we expect to see
	//	int expectedNCols = Integer.parseInt(commandLineOptions[inputIndex++]); // how many longitudes/cols we expect to see
	//	int nTotalChecks = Integer.parseInt(commandLineOptions[inputIndex++]); // how many updates we want to see...


	columnsAndNotes += "\n\n";
	columnsAndNotes += "initFile = [" + initFileName +"]\n";
	columnsAndNotes += "\n\n";
	columnsAndNotes += "\n";
	//	columnsAndNotes += "outputFileName = [" + outputFileName + "]\n";


	columnsAndNotes += "this file original = [" + notesFileName + "]\n\n";
	columnsAndNotes += "--- real initialization file contents ---\n";

	for (int lineIndex = 0; lineIndex < initFileContents.length; lineIndex++) {
	    columnsAndNotes += initFileContents[lineIndex] + "\n";
	}

	FunTricks.writeStringToFile(columnsAndNotes, notesFileName);
	
	
	//////////////////// end of timing test //////////////////////

	
	
	
	
	














	//	Date ORIGIANLendTime = new Date();
	//
	//	double ORIGINALduration = (endTime.getTime() - startTime.getTime()) / 1000.0;
	//
	//	System.out.println("Finished execution! duration = " + duration + " seconds or " + (duration/60) + " minutes or " + (duration/3600) + " hours" );







    } // main
}



