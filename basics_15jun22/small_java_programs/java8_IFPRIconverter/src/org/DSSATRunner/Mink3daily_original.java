package org.DSSATRunner;

import java.io.*;
import java.util.Date;

import org.R2Useful.*;

public class Mink3daily_original {

	private boolean readyToRun = false;


	/////////////////////////////////////
	// create a bunch of magic numbers //
	/////////////////////////////////////
	
	public static final int nDecimalsInOutput = 3;
	
	public static final String delimiter = "\t";
	public static final String magicWeatherStationNameToUse     = "RRRR";
	public static final String magicDSSATSummaryToRead          = "Summary.OUT";
	public static final String magicErrorFile                   = "ERROR.OUT";
	public static final String magicInitializationFile          = "deleteme.v45";
	public static final String tempXFileName                    = "deleteme.SNX";
	public static final String magicInitializationContents = 
		"$BATCH()\n@FILEX                                                                                        TRTNO     RP     SQ     OP     CO\n"
		+	tempXFileName
		+ "                                                                                      1      0      0      0      0\n";

	// search and replace elements in the template X file
	public static final String soilPlaceholder                = "ssssssssss";
	public static final String initializationStartPlaceholder = "iiiiS";
	public static final String plantingDateStartPlaceholder   = "ppppS";
	public static final String plantingDateEndPlaceholder     = "ppppE";
	public static final String harvestingDatePlaceholder   		= "hhhhh";
	public static final String randomSeedPlaceholder          = "rrrrr";
	public static final String nYearsOrRandomSeedsPlaceholder = "nnnnn";
	public static final String weatherPlaceholder             = "wwww";
	public static final String co2ppmPlaceholder              = "co2p";
	public static final String fertilizerPlaceholder          = "___place fertilizers here___";
	public static final String irrigationPlaceholder          = "___place irrigation here___";
	public static final String soilInitializationPlaceholder  = "___place initializations here___";

//	public static final int hardLimitOnReReads = 200;

//	public static final int hardLimitOnHappyReadingErrors = 20;

	// Beware the MAGIC ASSUMPTION!!! assuming soil codes must fit into 10 characters padded with zeros
	private static final int totalLengthForSoilType = 10;

	public static final int nDaysInMonth =  30;
	public static final int nDaysInYear  = 365;

//	private static final int firstWeatherYear = 0; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly
	// details for DSSAT's summary output files...
//	private static final int magicMissingValue = -99;
	private int magicDSSATSummaryLineIndexToRead = 4;
	
	private static final int magicHarvestedWeightAtHarvestOutputIndex = 20;
	private static final int magicStartingDateOutputIndex             = 11;
	private static final int magicPlantingDateOutputIndex             = 12;
	private static final int magicEmergenceDateOutputIndex            = 13;
	private static final int magicAnthesisDateOutputIndex             = 14;
	private static final int magicMaturityDateOutputIndex             = 15;
	private static final int magicHarvestingDateOutputIndex           = 16;
	
	private static final int magicOffsetForExtraNamesOutputIndex      = 17;
	private static final int magicMaximumLengthForExtraNames          = 10;
	
	

	// latest 4.5 beta...
	private static final String[] extraNames     = {
		"DWAP", "CWAM", "HWAM", "HWAH", "BWAH", "PWAM",
		"HWUM", "H#AM", "H#UM", "HIAM", "LAIX", "IR#M",
		"IRCM", "PRCM", "ETCM", "EPCM", "ESCM", "ROCM",
		"DRCM", "SWXM", "NI#M", "NICM", "NFXM", "NUCM",
		"NLCM", "NIAM", "CNAM", "GNAM", "PI#M", "PICM",
		"PUPC", "SPAM", "KI#M", "KICM", "KUPC", "SKAM",
		"RECM", "ONTAM", "ONAM", "OPTAM", "OPAM", "OCTAM",
		"OCAM", "DMPPM", "DMPEM", "DMPTM", "DMPIM", "YPPM",
		"YPEM", "YPTM", "YPIM", "DPNAM", "DPNUM", "YPNAM",
		"YPNUM", "NDCH", "TMAXA", "TMINA", "SRADA", "DAYLA", "CO2A", "PRCP", "ETCP",
		"ESCP", "EPCP"
	};
	
	private static int magicNumberOfOutputsExpected = magicOffsetForExtraNamesOutputIndex + extraNames.length;
		
	// crop categories.... each needs a unique id number...
	
	private static final String maizeString      = "maize";
	private static final String riceString       = "rice";
	private static final String unfloodedRiceString       = "unfloodedrice";
	private static final String wheatString      = "wheat";
	private static final String winterWheatString      = "winterwheat";
	private static final String soybeansString   = "soybeans";
	private static final String groundnutsString = "groundnuts";
	private static final String cottonString     = "cotton";
	private static final String potatoesString   = "potatoes";
	private static final String potatoes2String   = "potatoes2";
	private static final String sorghumString   = "sorghum";
	private static final String cassavaString   = "cassava";
	private static final String allAtPlantingString      = "allAtPlanting";
	private static final String threeSplitWithFloweringString      = "threeSplitWithFlowering";
	private static final String middleHeavyThreeSplitWithFloweringString      = "middleHeavyThreeSplitWithFlowering";

		
	
	/////////////////////////////////////////////////////
	// other variables which are good to have sharable //
	/////////////////////////////////////////////////////

	private int maxRunTime = 5000; // MAGIC at the moment
	private double bumpUpMultiplier = 2; // MAGIC at the moment
	private int testIntervalToUse = 100; // MAGIC at the moment
	private int rerunAttemptsMax = 4; // MAGIC at the moment

	
	
	private String initFileName         = null;

	private String  gisTableBaseName    = null;
	private String  templateXFile       = null;
	private String  yieldOutputBaseName = null;
	private boolean allFlag             = false;
	private String  allExtraToRecordCSV = null;
	private String[] allExtraToRecordNames = null;
	private int[]   allExtraToRecordIndices = null;
	private String  weatherDataSuffix   = null;

	private static final String pathToDSSATDirectory     = "./";
	private File   pathToDSSATDirectoryAsFile = null;
	private String nameOfDSSATExecutable    = null;
	private String baseNameOfDailyWeather  = null;
	private double SWmultiplier             = Double.NaN;
	private int    firstRandomSeed          = 1234; // should not need this because using preexisting weather data
	private int    nFakeYears        = -2;
	private String magicSoilPrefix          = null;
	private int    fakePlantingYear = -1; 
	private int    spinUpTimeDays           = -4;
	private int    nPlantingWindowsPerMonth = -5;
	private int    plantingWindowLengthDays = -6;
	private int    co2ppm    = -5;
	private String cropFertilizerSchemeToUse = null;
//	private int    nHappyPlantRunsForPhenology = -3;
	private int    happyYieldThresholdToDoRealRuns = 0;
	private int    phenologyBufferInDays = 0;
	private int    happyMaturityThresholdToDoRealRuns = 0;
	private String irrigationSchemeToUse = null;
	private double fractionBetweenLowerLimitAndDrainedUpperLimit = Double.NEGATIVE_INFINITY;

	
	private double depthForNitrogen = 3;
	private double residueNitrogenPercent = -4;
	private double incorporationRate = -5;
	private double incorporationDepth = -6;
	private String clayLoamSandStableCarbonRatesFilename = null;

	// out of order for the usual historical reasons
	private int    optionalHarvestInterval = 0;
	private int plantingDateInMonthShiftInDays = 0;
	
	private double[] standardDepths          = null;
	private double[] clayStableFractionAbove = null;
	private double[] loamStableFractionAbove = null;
	private double[] sandStableFractionAbove = null;

	
	
	
//	private String magicWeatherStationNameToUsePath = null;
	private String magicDSSATSummaryToReadPath      = null;
	private File   summaryDSSATOutputAsFileObject   = null;
	private File   errorAsFileObject   = null;
	private String magicInitializationFilePath      = null;
	
//	private int cropToUseInt = -3;
	
	private NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
	private IrrigationScheme irrigationScheme = null;

	
	private String[] dssatExecutionCommand = new String[3];
//	private String[] weatherExecutionCommand = new String[7];

	///////////////////////////////////////////////////////////////
	// special variables for the purpose of grabbing the results //
	///////////////////////////////////////////////////////////////
	// initialize the place to store the yields...

	private DescriptiveStatisticsUtility realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility realTimeToPlantingEntirePixel = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility realNoPlantingEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility realEmergenceEntirePixel = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility realAnthesisEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility realMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility realBadThingsCountsEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility[] extraSummaryAccumulators = null;

	private DescriptiveStatisticsUtility[] happyYearlyYields = null;
	private DescriptiveStatisticsUtility[] realYearlyYields = null;

	// now let's be crazy and let everything be kept if so desired...
	private DescriptiveStatisticsUtility[][] extraYearly = null;
	
//	private boolean badThingsHappened = false;
	

	public Mink3daily_original(String initFileNameToUse) throws IOException, Exception {
	    this.initFileName = initFileNameToUse;
	    readInitFile();
	}


	public void readInitFile() throws IOException, Exception {

		String[] initFileContents = FunTricks.readTextFileToArray(initFileName);

		int storageIndex = 0;
		allFlag                  = Boolean.parseBoolean(initFileContents[storageIndex++]);
		allExtraToRecordCSV      = initFileContents[storageIndex++];
		gisTableBaseName         = initFileContents[storageIndex++];
		templateXFile            = initFileContents[storageIndex++];
		yieldOutputBaseName      = initFileContents[storageIndex++];
		nameOfDSSATExecutable    = initFileContents[storageIndex++];
		baseNameOfDailyWeather   = initFileContents[storageIndex++];
		SWmultiplier             = Double.parseDouble(
				initFileContents[storageIndex++]);
		weatherDataSuffix    = initFileContents[storageIndex++];
//		firstRandomSeed          = Integer.parseInt(
//				initFileContents[storageIndex++]);
		fakePlantingYear         = Integer.parseInt(
				initFileContents[storageIndex++]);
		nFakeYears        = Integer.parseInt(
				initFileContents[storageIndex++]);
		magicSoilPrefix          = initFileContents[storageIndex++];
		spinUpTimeDays           = Integer.parseInt(
				initFileContents[storageIndex++]);
		nPlantingWindowsPerMonth = Integer.parseInt(
				initFileContents[storageIndex++]);
		plantingWindowLengthDays = Integer.parseInt(
				initFileContents[storageIndex++]);
		
		co2ppm                     = Integer.parseInt(
				initFileContents[storageIndex++]);

		cropFertilizerSchemeToUse       = initFileContents[storageIndex++];
		
//		nHappyPlantRunsForPhenology     = Integer.parseInt(
//				initFileContents[storageIndex++]);
		happyYieldThresholdToDoRealRuns = Integer.parseInt(
				initFileContents[storageIndex++]);
		phenologyBufferInDays = Integer.parseInt(
				initFileContents[storageIndex++]);
		happyMaturityThresholdToDoRealRuns = Integer.parseInt(
				initFileContents[storageIndex++]);

		irrigationSchemeToUse       = initFileContents[storageIndex++];

		fractionBetweenLowerLimitAndDrainedUpperLimit = Double.parseDouble(
				initFileContents[storageIndex++]);
//		nitrogenPPMforBothNH4NO2 = Double.parseDouble(
//				initFileContents[storageIndex++]);

		depthForNitrogen       = Double.parseDouble(initFileContents[storageIndex++]);
		residueNitrogenPercent = Double.parseDouble(initFileContents[storageIndex++]);
		incorporationRate      = Double.parseDouble(initFileContents[storageIndex++]);
		incorporationDepth     = Double.parseDouble(initFileContents[storageIndex++]);
		clayLoamSandStableCarbonRatesFilename = initFileContents[storageIndex++];
		
		optionalHarvestInterval = Integer.parseInt(initFileContents[storageIndex++]);
		plantingDateInMonthShiftInDays = Integer.parseInt(initFileContents[storageIndex++]);

		// i want to be able to feed in the time-out controls from the outside, but with defaults...
		// i think they are already defined in the declarations, so we have the defaults, we just need to
		// overwrite them if they are here...
		
//		bumpUpMultiplier = 2; // MAGIC at the moment
//		testIntervalToUse = 100; // MAGIC at the moment
//		rerunAttemptsMax = 4; // MAGIC at the moment

		if (initFileContents.length >= storageIndex) {
			maxRunTime = Integer.parseInt(initFileContents[storageIndex++]); // MAGIC at the moment
		}
		if (initFileContents.length >= storageIndex) {
			bumpUpMultiplier = Double.parseDouble(initFileContents[storageIndex++]); // MAGIC at the moment
		}
		if (initFileContents.length >= storageIndex) {
			testIntervalToUse = Integer.parseInt(initFileContents[storageIndex++]); // MAGIC at the moment
		}
		if (initFileContents.length >= storageIndex) {
			rerunAttemptsMax = Integer.parseInt(initFileContents[storageIndex++]); // MAGIC at the moment
		}
		
		
//		now prepare the dependent magic numbers...
		
		pathToDSSATDirectoryAsFile = new File(pathToDSSATDirectory);

		// this is for "batch style" for "real" experiments
//		dssatExecutionCommand[0] = pathToDSSATDirectory + nameOfDSSATExecutable;
//		dssatExecutionCommand[1] = "b";
//		dssatExecutionCommand[2] = magicInitializationFile;

		// this is for the "seasonal analysis
		dssatExecutionCommand[0] = pathToDSSATDirectory + nameOfDSSATExecutable;
		dssatExecutionCommand[1] = "n";
		dssatExecutionCommand[2] = magicInitializationFile;

		magicDSSATSummaryToReadPath      = pathToDSSATDirectory + magicDSSATSummaryToRead;
		summaryDSSATOutputAsFileObject   = new File(magicDSSATSummaryToReadPath);
		errorAsFileObject                = new File(pathToDSSATDirectory + magicErrorFile);
		magicInitializationFilePath      = pathToDSSATDirectory + magicInitializationFile;

		// figure out the crop number to use so we can use switches instead of if/then...
		// i'm sure there's a better way, but i'm lame, so i'm gonna brute force it...
				
		if (cropFertilizerSchemeToUse.equalsIgnoreCase(maizeString)) {
			nitrogenFertilizerScheme = new FSMaize();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(riceString)) {
			nitrogenFertilizerScheme = new FSRice();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(wheatString)) {
			nitrogenFertilizerScheme = new FSWheat();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(winterWheatString)) {
			nitrogenFertilizerScheme = new FSWinterWheat();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(soybeansString)) {
			nitrogenFertilizerScheme = new FSLegume();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(groundnutsString)) {
			nitrogenFertilizerScheme = new FSLegume();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(potatoesString)) {
			nitrogenFertilizerScheme = new FSPotatoes();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(potatoes2String)) {
			nitrogenFertilizerScheme = new FSPotatoes2();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(allAtPlantingString)) {
			nitrogenFertilizerScheme = new FSAllAtPlanting();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(threeSplitWithFloweringString)) {
			nitrogenFertilizerScheme = new FSThreeSplitWithFlowering();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(middleHeavyThreeSplitWithFloweringString)) {
			nitrogenFertilizerScheme = new FSMiddleHeavyThreeSplitWithFlowering();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(sorghumString)) {
			nitrogenFertilizerScheme = new FSAllAtPlanting();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(cottonString)) {
			nitrogenFertilizerScheme = new FSCotton();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(cassavaString)) {
		    nitrogenFertilizerScheme = new FSAllAtPlanting();
		    System.out.println("CASSAVA fertilizer scheme doubles as meaning ignore emergence and maturity!");
		} else {
		    System.out.println("crop string [" + cropFertilizerSchemeToUse + "]" + " not in our list of supported crops; or at least, not implemented");
		    throw new Exception();
		}

		if (irrigationSchemeToUse.equalsIgnoreCase(maizeString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(riceString)) {
			irrigationScheme = new IrriSRice();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(unfloodedRiceString)) {
			irrigationScheme = new IrriSRiceUnfloodedPaddy();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(wheatString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(soybeansString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(groundnutsString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(potatoesString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(sorghumString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(cottonString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(cassavaString)) {
		    irrigationScheme = new IrriSNone();
		} else {
			System.out.println("irrigation string [" + irrigationSchemeToUse + "]" + " not in our list of supported crops; assuming scheme NONE.");
			irrigationScheme = new IrriSNone();
		}


		// the descriptive statistics utilities for keeping track of the outputs
		happyYearlyYields = new DescriptiveStatisticsUtility[nFakeYears];
		 realYearlyYields = new DescriptiveStatisticsUtility[nFakeYears];
		for (int yearIndex = 0; yearIndex < nFakeYears; yearIndex++) {
			happyYearlyYields[yearIndex] = new DescriptiveStatisticsUtility(false);
			 realYearlyYields[yearIndex] = new DescriptiveStatisticsUtility(false);
		}

		// switching this to consider life as floats rather than as integers...
		extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraNames.length];
		for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
		    extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(true);
		}

		// for the yearly extra outputs, we have to first figure out which ones we care about
		// then set them up...
//		allExtraToRecordCSV;
//		private String[] allExtraToRecordNames = null;
//		private int[]   allExtraToRecordIndices = null;

		// first, let's split it up on the comma so we can get the different names...
		allExtraToRecordNames = allExtraToRecordCSV.split(",");
		
		// now, let's look through the list of official extra names and figure out the appropriate
		// index. brute force, because we just don't care...
		// Beware the MAGIC ASSUMPTION!!! there is no idiot checking for uniqueness. somebody could
		// ask for IRCM five times and we'll give it to them...
		boolean foundMatchHere = false;
		allExtraToRecordIndices = new int[allExtraToRecordNames.length]; // don't forget to initialize like we already forgot to
		for (int extraToRecord = 0; extraToRecord < allExtraToRecordNames.length; extraToRecord++) {
		    // run through the official list to see if there is a match. if not, throw a hissy fit...
		    foundMatchHere = false;
		    for (int officialExtraIndex = 0; officialExtraIndex < extraNames.length; officialExtraIndex++) {
			if (allExtraToRecordNames[extraToRecord].equalsIgnoreCase(extraNames[officialExtraIndex])) {
			    // we have a match, so write the number down and break out
			    allExtraToRecordIndices[extraToRecord] = officialExtraIndex;
			    foundMatchHere = true;
			    break;
			}
		    }
		    // now, check to see if we found something
		    if (!foundMatchHere) {
			System.out.println("An extra quantity was requested for yearly recording but is unknown [" + allExtraToRecordNames[extraToRecord] + "]");
			throw new Exception();
		    }
		} // looking for the indices of the requested extra quantities

		// if we manage to get here without an exception getting thrown, then we have a good set of requests
		// so, let's set up the accumulators...
		extraYearly = new DescriptiveStatisticsUtility[allExtraToRecordIndices.length][nFakeYears];
		for (int extraIndex = 0; extraIndex < allExtraToRecordIndices.length; extraIndex++) {
		for (int yearIndex = 0; yearIndex < nFakeYears; yearIndex++) {
			 extraYearly[extraIndex][yearIndex] = new DescriptiveStatisticsUtility(true);
		}
		}
	

		// set up the soil stable carbon content tables...
		
		String[] carbonTable = FunTricks.readTextFileToArray(clayLoamSandStableCarbonRatesFilename);
		int nLayers = carbonTable.length;
		
		standardDepths          = new double[nLayers];
		clayStableFractionAbove = new double[nLayers];
		loamStableFractionAbove = new double[nLayers];
		sandStableFractionAbove = new double[nLayers];

		String[] splitLine = null;
		for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
			System.out.println("line " + layerIndex + ": [" + carbonTable[layerIndex] + "]");
			splitLine = carbonTable[layerIndex].split("\t");
			standardDepths[         layerIndex] = Double.parseDouble(splitLine[0]);
			clayStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[1]);
			loamStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[2]);
			sandStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[3]);
		}
		
//		double[] standardDepths  =         {   20,   40,   60,  1000000000};
//		double[] clayStableFractionAbove = { 0.80, 0.96, 0.98,  0.98};
//		double[] loamStableFractionAbove = { 0.80, 0.98, 0.98,  0.98};
//		double[] sandStableFractionAbove = { 0.93, 0.98, 0.98,  0.98};


		
		
//		mark that we're good to go...
		readyToRun = true;

	}

	private void writeProvenance() throws IOException, FileNotFoundException, Exception {
		String filename = yieldOutputBaseName  + "_provenance.txt";
		writeProvenance(filename);
	}

	private void writeProvenance(String filename) throws IOException, FileNotFoundException, Exception {


		File provenanceFileObject = new File(filename);
		PrintWriter provenanceOut = new PrintWriter(provenanceFileObject);


		provenanceOut.print("--- Provenance for run starting at " + new Date() + "\n");
		provenanceOut.print("\n");
		provenanceOut.print("init file:\t" + initFileName + "\n");
		provenanceOut.print("\n");

		String initsAsString = FunTricks.readTextFileToString(initFileName);

		provenanceOut.print("--- begin copy of init file [" + initFileName + "] ---" + "\n");
		provenanceOut.print(initsAsString);
		provenanceOut.print("---- end copy of init file [" + initFileName + "] ----" + "\n");


		provenanceOut.print("\n");
		provenanceOut.print("\n");

		provenanceOut.print("--- some settings in human readable format ---" + "\n");

		provenanceOut.print("allFlag:\t\t"      + allFlag + "\n");
		provenanceOut.print("allExtraToRecordCSV:\t"      + allExtraToRecordCSV + "\n");

		provenanceOut.print("gisTableBaseName:\t"      + gisTableBaseName + "\n");
		provenanceOut.print("templateXFile:\t\t"       + templateXFile + "\n");
		provenanceOut.print("yieldOutputBaseName:\t"   + yieldOutputBaseName + "\n");

		provenanceOut.print("\n");

		provenanceOut.print("pathToDSSATDirectory:\t\t"     + pathToDSSATDirectory + "\n");
		provenanceOut.print("nameOfDSSATExecutable:\t\t"    + nameOfDSSATExecutable + "\n");
		provenanceOut.print("baseNameOfDailyWeather:\t\t"   + baseNameOfDailyWeather + "\n");
		provenanceOut.print("SWmultiplier:\t\t\t"           + SWmultiplier + "\n");
		provenanceOut.print("weatherDataSuffix:\t\t"        + weatherDataSuffix + "\n");
		provenanceOut.print("fakePlantingYear:\t\t"         + fakePlantingYear + "\n");
		provenanceOut.print("nFakeYears:\t\t"               + nFakeYears + "\n");
		provenanceOut.print("magicSoilPrefix:\t\t"          + magicSoilPrefix + "\n");
		provenanceOut.print("spinUpTimeDays:\t\t\t"         + spinUpTimeDays + "\n");
		provenanceOut.print("nPlantingWindowsPerMonth:\t"   + nPlantingWindowsPerMonth + "\n");
		provenanceOut.print("plantingWindowLengthDays:\t"   + plantingWindowLengthDays + "\n");
		provenanceOut.print("co2ppm:\t\t\t\t"               + co2ppm + "\n");

		provenanceOut.print("nitrogenCropToUse:\t\t\t"              + cropFertilizerSchemeToUse + "\n");
//		provenanceOut.print("nHappyPlantRunsForPhenology:\t"        + nHappyPlantRunsForPhenology + "\n");
		provenanceOut.print("happyYieldThresholdToDoRealRuns:\t"    + happyYieldThresholdToDoRealRuns + "\n");
		provenanceOut.print("phenologyBufferInDays:\t"              + phenologyBufferInDays + "\n");
		provenanceOut.print("happyMaturityThresholdToDoRealRuns:\t" + happyMaturityThresholdToDoRealRuns + "\n");

		provenanceOut.print("irrigationCropToUse:\t\t\t"            + irrigationSchemeToUse + "\n");
		provenanceOut.print("fractionBetweenLowerLimitAndDrainedUpperLimit:\t" + fractionBetweenLowerLimitAndDrainedUpperLimit + "\n");
//		provenanceOut.print("nitrogenPPMforBothNH4NO2:\t" + nitrogenPPMforBothNH4NO2 + "\n");

//		initFileContents += "nitrogenPPMforBothNH4NO2 for initializing soil nitrogen content" + "\n";

		provenanceOut.print("depthForNitrogen:\t\t\t"     	+ depthForNitrogen + "\n");
		provenanceOut.print("residueNitrogenPercent:\t\t\t" + residueNitrogenPercent + "\n");
		provenanceOut.print("incorporationRate:\t\t\t"     	+ incorporationRate + "\n");
		provenanceOut.print("incorporationDepth:\t\t\t"     + incorporationDepth + "\n");
		provenanceOut.print("clayLoamSandStableCarbonRatesFilename:\t"     + clayLoamSandStableCarbonRatesFilename + "\n");

		provenanceOut.print("optionalHarvestInterval:\t"     + optionalHarvestInterval + "\n");
		provenanceOut.print("plantingDateInMonthShiftInDays:\t"     + plantingDateInMonthShiftInDays + "\n");

		provenanceOut.print("\n");

		provenanceOut.print("maxRunTime:\t"        + maxRunTime + "\n");
		provenanceOut.print("bumpUpMultiplier:\t"  + bumpUpMultiplier + "\n");
		provenanceOut.print("testIntervalToUse:\t" + testIntervalToUse + "\n");
		provenanceOut.print("rerunAttemptsMax:\t"  + rerunAttemptsMax + "\n");
		
		
		provenanceOut.print("--- Placeholder dictionary ---" + "\n");
		provenanceOut.print("soilPlaceholder =\t\t\t"            + soilPlaceholder + "\n");
		provenanceOut.print("initializationStartPlaceholder =\t" + initializationStartPlaceholder + "\n");
		provenanceOut.print("plantingDateStartPlaceholder =\t\t" + plantingDateStartPlaceholder + "\n");
		provenanceOut.print("plantingDateEndPlaceholder =\t\t"   + plantingDateEndPlaceholder + "\n");
		provenanceOut.print("harvestingDatePlaceholder =\t\t\t"  + harvestingDatePlaceholder + "\n");
		provenanceOut.print("randomSeedPlaceholder =\t\t\t"      + randomSeedPlaceholder + "\n");		provenanceOut.print("nYearsOrRandomSeedsPlaceholder =\t\t\t"      + nYearsOrRandomSeedsPlaceholder + "\n");
		provenanceOut.print("weatherPlaceholder =\t\t\t"         + weatherPlaceholder + "\n");
		provenanceOut.print("co2ppm =\t\t\t\t"                   + co2ppmPlaceholder + "\n");
		provenanceOut.print("fertilizerPlaceholder =\t\t\t"      + fertilizerPlaceholder + "\n");
		provenanceOut.print("irrigationPlaceholder =\t\t\t"      + irrigationPlaceholder + "\n");
		provenanceOut.print("soilInitializationPlaceholder =\t\t\t"      + soilInitializationPlaceholder + "\n");
		
		provenanceOut.print("\n");		

		provenanceOut.print("--- Magic number dictionary ---" + "\n");
		provenanceOut.print("magicWeatherStationNameToUse =\t" + magicWeatherStationNameToUse + "\n");
		provenanceOut.print("magicDSSATSummaryToRead =\t"      + magicDSSATSummaryToRead + "\n");
		provenanceOut.print("magicErrorFile =\t\t\t"           + magicErrorFile + "\n");
		provenanceOut.print("magicInitializationFile =\t"      + magicInitializationFile + "\n");
		provenanceOut.print("tempXFileName =\t\t\t"            + tempXFileName + "\n");
		provenanceOut.print("\n");		

		String XAsString = FunTricks.readTextFileToString(templateXFile);

		provenanceOut.print("--- begin copy of template X file [" + templateXFile + "] ---" + "\n");
		provenanceOut.print(XAsString);
		provenanceOut.print("---- end copy of template X file [" + templateXFile + "] ----" + "\n");
		provenanceOut.print("\n");		
		provenanceOut.print("--- begin copy of temporary initialization file [" + magicInitializationFilePath + "] ---" + "\n");
		provenanceOut.print(magicInitializationContents);
		provenanceOut.print("---- end copy of temporary initialization file [" + magicInitializationFilePath + "] ----" + "\n");
		provenanceOut.print("\n");		

		String carbonTableAsString = FunTricks.readTextFileToString(clayLoamSandStableCarbonRatesFilename);
		provenanceOut.print("clayLoamSandStableCarbonRates table format is tab delimited: depth / clay fraction stable / loam fraction stable / sand fraction stable" + "\n");
		provenanceOut.print("--- begin copy of clayLoamSandStableCarbonRates table file [" + clayLoamSandStableCarbonRatesFilename + "] ---" + "\n");
		provenanceOut.print(carbonTableAsString);
		provenanceOut.print("---- end copy of clayLoamSandStableCarbonRates table file [" + clayLoamSandStableCarbonRatesFilename + "] ----" + "\n");
		provenanceOut.print("\n");
		provenanceOut.print(" or interpreted as stable carbon fraction for..." + "\n");
		provenanceOut.print("Depth\tClay\tLoam\tSand" + "\n");
		for (int layerIndex = 0; layerIndex < standardDepths.length; layerIndex++) {
			provenanceOut.print(standardDepths[layerIndex] + "\t" + clayStableFractionAbove[layerIndex] + "\t"
					+ loamStableFractionAbove[layerIndex] + "\t" + sandStableFractionAbove[layerIndex] + "\n");
		}
		
		provenanceOut.print("maxRunTime: "        + maxRunTime + "\n");
		provenanceOut.print("bumpUpMultiplier: "  + bumpUpMultiplier + "\n");
		provenanceOut.print("testIntervalToUse: " + testIntervalToUse + "\n");
		provenanceOut.print("rerunAttemptsMax: "  + rerunAttemptsMax + "\n");

		provenanceOut.flush();
		provenanceOut.close();


	}

	
	
	public static String happyPlantX(String templateXFile) throws Exception {

		// the idea here is that we need to run through the templateXFile and turn off the
		// switches for stress modeling...
		

		String optionToKeepAsYes = "SYMBI";
		
		String magicSubstringToFind = "@N OPTIONS";
		
		// split up the template file into lines
		// Beware the MAGIC NUMBER!!! Assuming that \n is the line separator...
		String[] templateAsArray = templateXFile.split("\n");
		
		int lineWeWant = -1;
		// go looking for the lines with the labels and switches
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			if (templateAsArray[lineIndex].contains(magicSubstringToFind)) {
				lineWeWant = lineIndex;
				break;
			}
		}
		
		// check to see if we found it
		if (lineWeWant < 0) {
//			System.out.println("template X did not have an \"OPTIONS\" line");
			System.out.println("template X did not have a " + magicSubstringToFind + " line");
			throw new Exception();
		}
		
		String optionNamesLine  = templateAsArray[lineWeWant];
		String optionValuesLine = templateAsArray[lineWeWant + 1];
		
		// since i'm not good with strings, we're gonna brute force this
		
		// count how many "fields" we're dealing with
		int nColumns = 0;
		boolean currentCharIsSpace = false;
		boolean wereInWordPriorToThis = false;
		
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				nColumns++;
			}
		}

		// mark down the index of the end of each word...
		int[] endOfWord = new int[nColumns];
		int fakeColIndex = 0;
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
				endOfWord[fakeColIndex] = charIndex;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				fakeColIndex++;
			}
		}
		if (endOfWord[nColumns - 1] == 0) {
			// the end of the line is clean, so we don't get this far...
			endOfWord[nColumns - 1] = optionNamesLine.length();
		}
		
		int startIndex = 0;
		String tempName   = "";
		String tempValue = "";
		String newNamesLine  = "";
		String newValuesLine = "";
		for (int colIndex = 0; colIndex < nColumns; colIndex++) {
			if (colIndex == 0) {
				startIndex = 0;
			} else {
				startIndex = endOfWord[colIndex - 1];
			}

			tempName  = optionNamesLine.substring( startIndex, endOfWord[colIndex]);
			tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

			// the names are pretty static...
			newNamesLine  += tempName;

			// the first two columns are setup, so preserve them as is...
			if (colIndex < 2) {
				newValuesLine += tempValue;
			} else {
				// check if we want to set this to yes or no
//				System.out.println("considering: [" + tempName.trim() + "]");
				if (tempName.trim().equalsIgnoreCase(optionToKeepAsYes)) {
					// replace any semblance of no with yes
					newValuesLine += tempValue.replaceAll("N", "Y").replaceAll("n", "Y");
				} else {
					// replace any semblance of yes with no
					newValuesLine += tempValue.replaceAll("Y", "N").replaceAll("y", "N");
				} // end if we want it to be YES
				
			} // end if it is actually a switch
			
		} // end of columns
		
		// now we need to replace the lines...
		templateAsArray[lineWeWant]     = newNamesLine;
		templateAsArray[lineWeWant + 1] = newValuesLine;

		
		
		
		
		
		
		
		
		
		
		
		
		////////// ok, and we need to set the planting option to "recorded date" for happy plant purposes //////////////
		///// begin recorded date for happy plant /////
		
		magicSubstringToFind = "@N MANAGEMENT";
		
		lineWeWant = -1;
		// go looking for the lines with the labels and switches
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			if (templateAsArray[lineIndex].contains(magicSubstringToFind)) {
				lineWeWant = lineIndex;
				break;
			}
		}
		
		// check to see if we found it
		if (lineWeWant < 0) {
			System.out.println("template X did not have a " + magicSubstringToFind + " line");
			throw new Exception();
		}
		
		String managementNamesLine  = templateAsArray[lineWeWant];
		String managementValuesLine = templateAsArray[lineWeWant + 1];
		
		// since i'm not good with strings, we're gonna brute force this
		
		// count how many "fields" we're dealing with
		nColumns = 0;
		currentCharIsSpace = false;
		wereInWordPriorToThis = false;
		
		for (int charIndex = 0; charIndex < managementNamesLine.length(); charIndex++) {
			if (managementNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				nColumns++;
			}
		}

		// mark down the index of the end of each word...
		endOfWord = new int[nColumns];
		fakeColIndex = 0;
		for (int charIndex = 0; charIndex < managementNamesLine.length(); charIndex++) {
			if (managementNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
				endOfWord[fakeColIndex] = charIndex;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				fakeColIndex++;
			}
		}
		if (endOfWord[nColumns - 1] == 0) {
			// the end of the line is clean, so we don't get this far...
			endOfWord[nColumns - 1] = managementNamesLine.length();
		}
		
		newNamesLine  = "";
		newValuesLine = "";
		for (int colIndex = 0; colIndex < nColumns; colIndex++) {
			if (colIndex == 0) {
				startIndex = 0;
			} else {
				startIndex = endOfWord[colIndex - 1];
			}

//			System.out.println("mNamesLine  = [" + managementNamesLine + "]");
//			System.out.println("mValuesLine = [" + managementValuesLine + "]");
			tempName  = managementNamesLine.substring( startIndex, endOfWord[colIndex]);
			tempValue = managementValuesLine.substring(startIndex, endOfWord[colIndex]);

			// the names are pretty static...
			newNamesLine  += tempName;

			// the first two columns are setup, so preserve them as is...
			// Beware the MAGIC NUMBER!!! below in how we handle things....
			if (colIndex < 2) {
				newValuesLine += tempValue;
			} else {
				// check if we want to set this to yes or no
				// Beware the MAGIC NUMBER!!! which stuff we want to change to what...
				//				System.out.println("considering: [" + tempName.trim() + "]");
				if (tempName.trim().equalsIgnoreCase("PLANT")) {
					// replace any semblance of automatic (A) with recorded date (R)
					newValuesLine += tempValue.replaceAll("A", "R").replaceAll("a", "R");
				} else {
					// no worries, we have no further questions at this time, your honor
					newValuesLine += tempValue;
				} // end if we want it to be changed
				
			} // end if it is actually a switch
			
		} // end of columns
		
		// now we need to replace the lines...
		templateAsArray[lineWeWant]     = newNamesLine;
		templateAsArray[lineWeWant + 1] = newValuesLine;

		
		
		///// end recorded date for happy plant /////		
		
		
		
		
		
		
		
		
		
		
		
		
		
		/////////////////// look for irrigation treatment... /////////////////
		
		// actually, we also need to turn off the irrigation treatment flag in the treatments section...
		// the code is "MI" and we want it to read as " 0" underneath it...
		
		magicSubstringToFind = "@N R O C";
		String optionToMakeZero = "MI";
		
		lineWeWant = -1;
		// go looking for the lines with the labels and switches
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			if (templateAsArray[lineIndex].contains(magicSubstringToFind)) {
				lineWeWant = lineIndex;
				break;
			}
		}
		
		// check to see if we found it
		if (lineWeWant < 0) {
			System.out.println("template X did not have a line with [" + magicSubstringToFind + "]");
			throw new Exception();
		}
		
		optionNamesLine  = templateAsArray[lineWeWant];
		optionValuesLine = templateAsArray[lineWeWant + 1];

//		System.out.println("name line = [" + optionNamesLine + "]");
//		System.out.println("valu line = [" + optionValuesLine + "]");
		
		// since i'm not good with strings, we're gonna brute force this
		
		// count how many "fields" we're dealing with
		nColumns = 0;
		currentCharIsSpace = false;
		wereInWordPriorToThis = false;
		
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				nColumns++;
			}
		}

		// mark down the index of the end of each word...
		endOfWord = new int[nColumns];
		fakeColIndex = 0;
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
				endOfWord[fakeColIndex] = charIndex;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				fakeColIndex++;
			}
		}
		if (endOfWord[nColumns - 1] == 0) {
			// the end of the line is clean, so we don't get this far...
			endOfWord[nColumns - 1] = optionNamesLine.length();
		}
		
		
//		System.out.println("nColumns = " + nColumns);
		
		startIndex = 0;
		tempName   = "";
		tempValue = "";
		newNamesLine  = "";
		newValuesLine = "";
		for (int colIndex = 0; colIndex < nColumns; colIndex++) {
			if (colIndex == 0) {
				startIndex = 0;
			} else {
				startIndex = endOfWord[colIndex - 1];
			}

			tempName  = optionNamesLine.substring( startIndex, endOfWord[colIndex]);
			tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

			// the names are pretty static...
			newNamesLine  += tempName;

			// the first two columns are setup, so preserve them as is...
			if (colIndex < 2) {
				newValuesLine += tempValue;
			} else {
				// check if we want to set this to yes or no
//				System.out.println("considering: [" + tempName.trim() + "]");
				if (tempName.trim().equalsIgnoreCase(optionToMakeZero)) {
					// replace any semblance of no with yes
					newValuesLine += tempValue
					.replaceAll("1", "0")
					.replaceAll("2", "0")
					.replaceAll("3", "0")
					.replaceAll("4", "0")
					.replaceAll("5", "0")
					.replaceAll("6", "0")
					.replaceAll("7", "0")
					.replaceAll("8", "0")
					.replaceAll("9", "0");
				} else {
					newValuesLine += tempValue;
				} // end if we should change it...
				
			} // end if it is actually a switch
			
		} // end of columns
		
		// now we need to replace the lines...
		templateAsArray[lineWeWant]     = newNamesLine;
		templateAsArray[lineWeWant + 1] = newValuesLine;
		
		/////////////////// end look for irrigation treatment... /////////////////

		
		
		

		
		
		
		
		
		
		
		
		// now, we also need to make sure the planting occurs on a particular date
		// for happy plant because it gets ornery under automatic planting.
		// presumably because there is not any water accounting going on
		
		
		//////// begin look for planting treatment to set to zero /////////
		
		// actually, we also need to turn off the irrigation treatment flag in the treatments section...
		// the code is "MI" and we want it to read as " 0" underneath it...
		
		magicSubstringToFind = "@N R O C";
		String optionToMakeOne = "MP";
		
		lineWeWant = -1;
		// go looking for the lines with the labels and switches
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			if (templateAsArray[lineIndex].contains(magicSubstringToFind)) {
				lineWeWant = lineIndex;
				break;
			}
		}
		
		// check to see if we found it
		if (lineWeWant < 0) {
			System.out.println("template X did not have a line with [" + magicSubstringToFind + "]");
			throw new Exception();
		}
		
		optionNamesLine  = templateAsArray[lineWeWant];
		optionValuesLine = templateAsArray[lineWeWant + 1];

//		System.out.println("name line = [" + optionNamesLine + "]");
//		System.out.println("valu line = [" + optionValuesLine + "]");
		
		// since i'm not good with strings, we're gonna brute force this
		
		// count how many "fields" we're dealing with
		nColumns = 0;
		currentCharIsSpace = false;
		wereInWordPriorToThis = false;
		
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				nColumns++;
			}
		}

		// mark down the index of the end of each word...
		endOfWord = new int[nColumns];
		fakeColIndex = 0;
		for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
			if (optionNamesLine.substring(charIndex,charIndex+1).equals(" ")) {
				currentCharIsSpace = true;
			} else {
				currentCharIsSpace = false;
			}
			
			if (wereInWordPriorToThis && currentCharIsSpace) {
				// we have reached the end of the word
				wereInWordPriorToThis = false;
				endOfWord[fakeColIndex] = charIndex;
			} else if (wereInWordPriorToThis && !currentCharIsSpace) {
				// still in that word, do nothing
			} else if (!wereInWordPriorToThis && currentCharIsSpace) {
				// still between words, do nothing
			} else {
				// we have hit a new word
				wereInWordPriorToThis = true;
				fakeColIndex++;
			}
		}
		if (endOfWord[nColumns - 1] == 0) {
			// the end of the line is clean, so we don't get this far...
			endOfWord[nColumns - 1] = optionNamesLine.length();
		}
		
		
//		System.out.println("nColumns = " + nColumns);
		
		startIndex = 0;
		tempName   = "";
		tempValue = "";
		newNamesLine  = "";
		newValuesLine = "";
		for (int colIndex = 0; colIndex < nColumns; colIndex++) {
			if (colIndex == 0) {
				startIndex = 0;
			} else {
				startIndex = endOfWord[colIndex - 1];
			}

			tempName  = optionNamesLine.substring( startIndex, endOfWord[colIndex]);
			tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

			// the names are pretty static...
			newNamesLine  += tempName;

			// the first two columns are setup, so preserve them as is...
			if (colIndex < 2) {
				newValuesLine += tempValue;
			} else {
				// check if we want to set this to yes or no
//				System.out.println("considering: [" + tempName.trim() + "]");
				if (tempName.trim().equalsIgnoreCase(optionToMakeOne)) {
					// replace any semblance of no with yes
					newValuesLine += tempValue
					.replaceAll("0", "1")
					.replaceAll("2", "1")
					.replaceAll("3", "1")
					.replaceAll("4", "1")
					.replaceAll("5", "1")
					.replaceAll("6", "1")
					.replaceAll("7", "1")
					.replaceAll("8", "1")
					.replaceAll("9", "1");
				} else {
					newValuesLine += tempValue;
				} // end if we should change it...
				
			} // end if it is actually a switch
			
		} // end of columns
		
		// now we need to replace the lines...
		templateAsArray[lineWeWant]     = newNamesLine;
		templateAsArray[lineWeWant + 1] = newValuesLine;

		//////// end look for planting treatment to set to zero /////////
		
		
		
		
		
		
		
		
		
		
		
		
		
		// ok, and now we need to run everything together into a single string with newlines again...
		
		String happyXTemplate = "";
		
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			happyXTemplate += templateAsArray[lineIndex] + "\n";
		}
		
		
		
		return happyXTemplate;
	}
	
	
	private String[] buildUniqueInvariantsXTemplates(
			String templateXFileContents,
			String randomSeedCode,
			String nHappyYearsCode,
			String co2ppmCode,
			IrrigationScheme dummyScheme,
			String nYearsCode
			
			) throws Exception {
		
		
		String XHappyInvariantsReplaced = happyPlantX(templateXFileContents);
		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(randomSeedPlaceholder, randomSeedCode);
		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nHappyYearsCode);
		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);
		// and put in a dummy set of fertilizer stuff
		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(fertilizerPlaceholder,
				"*FERTILIZERS (INORGANIC)\n" +
				"@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD\n" +
				" 1     1 FE005 AP001     1 00000   -99   -99   -99   -99   -99\n"
		);
		// and a dummy set of irrigation stuff

		XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(irrigationPlaceholder, dummyScheme.buildIrrigationBlock());
		
//		irrigationScheme
		
		
		String XInvariantsReplaced = templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);


		return new String[] {XHappyInvariantsReplaced, XInvariantsReplaced};
	}
	
		private int[] grabUnifiedHappyResults(int nYears) throws InterruptedException, Exception {
			// if we wait properly using .waitFor(), we shouldn't have to check if things exist...
			
			// declarations
			int plantingDate = -1;
			int eventDate = -2;
			int daysSincePlantingForEvent = -3;
	
			int yieldToUse = -3;
	//		int emergenceToUse = -3;
			int anthesisToUse = -3;
			int maturityToUse = -3;
			boolean everythingIsValid = true;

			String[] splitLine = null;
			
			
			// declarations with initializations
			int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
			String[] candidateSummaryContents = new String[nLinesToRead];
			
			try {
				candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
			} catch (FileNotFoundException fnfe) {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("HAPPY: file not found... [" + errorAsFileObject + "] exists...");
					throw fnfe;
				}
				System.out.println("HAPPY: file not found...  (no error file)");
			}
			
			// parse the output file for the necessary goodies...
			DescriptiveStatisticsUtility happyYields        = new DescriptiveStatisticsUtility(false);
			DescriptiveStatisticsUtility happyAnthesisDates = new DescriptiveStatisticsUtility(false);
			DescriptiveStatisticsUtility happyMaturityDates = new DescriptiveStatisticsUtility(false);
	
			for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
				if (candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] == null) {
					System.out.println("Happy: funny business; skip and hope for the best");
					System.out.println(fakeYearIndex + " -> [" + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] + "]");

					continue;
				}
				everythingIsValid = true;
				try {
					// split the line up into "fields"
					// Beware the MAGIC NUMBER!!! fixed format output from DSSAT with spaces everywhere
					// so we're using "space" as the delimiter
					splitLine = FunTricks.parseRepeatedDelimiterToArray(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]," ");
					
					// yield
					yieldToUse = Integer.parseInt(splitLine[magicHarvestedWeightAtHarvestOutputIndex]);
					
					// planting / anthesis / maturity dates
					plantingDate = Integer.parseInt(splitLine[magicPlantingDateOutputIndex]);
					anthesisToUse = Integer.parseInt(splitLine[magicAnthesisDateOutputIndex]);
					maturityToUse = Integer.parseInt(splitLine[magicMaturityDateOutputIndex]);
					
					// now need to check whether maturity exists, if not, just go with harvest date if that exists...
					if (maturityToUse < 0) {
						maturityToUse = Integer.parseInt(splitLine[magicHarvestingDateOutputIndex]);
					}

					// something funny could happen and we end up with a -99 yield; i haven't seen it because i haven't looked for it,
					// but i have seen it with the "real" yields. so, let's just censor the yields here...
					// guess: bad weather data, say tmin > tmax
					if (yieldToUse < 0) {
						System.out.println("    HAPPY: negative yield: [" + yieldToUse + "], censoring to zero; fakeYearIndex=" + fakeYearIndex);
						yieldToUse = 0;
					}
					happyYearlyYields[fakeYearIndex].useLongValue(yieldToUse);
	
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("HAPPY: had trouble reading one of the following as an integer:");
					System.out.println("yield [" + splitLine[magicHarvestedWeightAtHarvestOutputIndex] + "]");
					System.out.println("planting  [" + splitLine[magicPlantingDateOutputIndex] + "]");
					System.out.println("anthesis  [" + splitLine[magicAnthesisDateOutputIndex] + "]");
					System.out.println("maturity  [" + splitLine[magicMaturityDateOutputIndex] + "]");
					everythingIsValid = false;
				}
	
				if (everythingIsValid) {
				// yield
					if (yieldToUse > 0) {
						happyYields.useLongValue(yieldToUse);
						happyYieldsEntirePixel.useLongValue(yieldToUse);
					} else {
						// i think we should force it to zero
						// Beware the MAGIC NUMBER!!! mapping "no results" yields to zero
						happyYields.useLongValue(0L);
						happyYieldsEntirePixel.useLongValue(0L);
					}
					
				
				// anthesis
				eventDate = anthesisToUse;
				daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
				// check to make sure we got something real. for example, if flowering failed, we will get -99 for anthesis
				// so when we do the differencing, we will get a -1 or -2 code from the differencer. that means we want
				// to skip over it and not use it for the accumulator
				if (daysSincePlantingForEvent > 0) {
					happyAnthesisDates.useLongValue(daysSincePlantingForEvent);
				}
	
				// pull out the bits we want
				// maturity
				eventDate = maturityToUse;
				if (maturityToUse > 0 && plantingDate > 0) {
					daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
					// checking for reasonableness...
					if (daysSincePlantingForEvent > 0) {
						happyMaturityDates.useLongValue(daysSincePlantingForEvent);
						happyMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
					}
					
				}
				} // everything is valid
			} // for fake year
			
			return new int[] {(int)Math.floor(happyYields.getMean()),
					(int)Math.floor(happyAnthesisDates.getMean()),
					(int)Math.floor(happyMaturityDates.getMean())};
	
			
		}

	
		private void grabUnifiedNewManyResults(int nYears, long lineIndex, int plantingWindowIndex) throws InterruptedException, Exception {
			// declarations
			int plantingDate, startingDate, daysSincePlantingForEmergence, daysSincePlantingForPlanting, daysSincePlantingForAnthesis,
			daysSincePlantingForMaturity = Integer.MIN_VALUE,	yieldToUse, emergenceToUse, anthesisToUse, maturityToUse;
			
			double[] extractedValues = new double[extraNames.length];
			boolean[] everythingIsValid = new boolean[extraNames.length];
			String[] candidateSummaryContents = null, splitLine = null; // new String[nLinesToRead];
			String stringToParse = null;


			// declarations with initializations
			int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;

			
			// attempt to read
			try {
				candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath,nLinesToRead);
			} catch (FileNotFoundException fnfe) {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("REAL: file not found...  [" + errorAsFileObject + "] exists...");
					throw fnfe;
				}
				System.out.println("REAL: file not found...  (no error file)");
				
			} catch (IOException ioe) { System.out.println("REAL: i/o exception...  ");	throw ioe;
			} 
/*
			catch (ArrayIndexOutOfBoundsException aioobe) {
				System.out.println("REAL: array index exception...");
				// carry on, since the file is still in the process of being written...
			}
*/
			// parse the output file for the necessary goodies...
			for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {

				for (int validIndex = 0; validIndex < extraNames.length; validIndex++) {
					everythingIsValid[validIndex] = true; // good unless decided otherwise
				}
				
				try {
					// Beware the MAGIC NUMBER!!! using space as the possibly repeated delimiter
					splitLine = FunTricks.parseRepeatedDelimiterToArray(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]," ");
					// yield
					yieldToUse = Integer.parseInt(splitLine[magicHarvestedWeightAtHarvestOutputIndex]);

					// planting / anthesis / maturity dates
					emergenceToUse = Integer.parseInt(splitLine[magicEmergenceDateOutputIndex]);
					startingDate   = Integer.parseInt(splitLine[magicStartingDateOutputIndex]);
					plantingDate   = Integer.parseInt(splitLine[magicPlantingDateOutputIndex]);
					anthesisToUse  = Integer.parseInt(splitLine[magicAnthesisDateOutputIndex]);
					maturityToUse  = Integer.parseInt(splitLine[magicMaturityDateOutputIndex]);

					if (maturityToUse < 0) {
						maturityToUse = Integer.parseInt(splitLine[magicHarvestingDateOutputIndex]);
					}

					// all the other goodies...
					// hopefully this spacey thing will fix the stars problem
					if (splitLine.length != magicNumberOfOutputsExpected) {
						System.out.println("REAL: got " + splitLine.length + " rather than expected " + magicNumberOfOutputsExpected + 
								" so short interpretation; (data line " + lineIndex + "; fakeYear " + fakeYearIndex + " planting window index " + plantingWindowIndex + "):");
					}
					
					// time to planting. since we may be using automatic planting, let's keep track of the
					// time between the simulation start and the actual planting date when the conditions are met.
					if (plantingDate > 0) {
						daysSincePlantingForPlanting = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(startingDate, plantingDate);
						realTimeToPlantingEntirePixel.useLongValue(daysSincePlantingForPlanting);
					} else {
						realNoPlantingEntirePixel.useLongValue(1); // assess a problem...
					}

					// emergence
					// we have to check whether emergence occurred...
					// Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this
					// for the moment...
					if (emergenceToUse > 0 && plantingDate > 0) {
						daysSincePlantingForEmergence = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, emergenceToUse);
						realEmergenceEntirePixel.useLongValue(daysSincePlantingForEmergence);
					} else { // System.out.println("grab real results: emergence failed...; not counting in average");
						realBadThingsCountsEntirePixel.useLongValue(1000); // assess a problem...
					}

					// anthesis
					// we have to check whether flowering occurred...
					// Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this for the moment...
					if (anthesisToUse > 0 && plantingDate > 0) {
						daysSincePlantingForAnthesis = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, anthesisToUse);
						realAnthesisEntirePixel.useLongValue(daysSincePlantingForAnthesis);
					} else { // System.out.println("grab real results: flowering failed...; not counting in average");
						realBadThingsCountsEntirePixel.useLongValue(1000000); // assess a problem...
					}

					// maturity
					// if it's still missing..., skip over it and record as something bad happened
					if (maturityToUse > 0 && plantingDate > 0) {
						daysSincePlantingForMaturity = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, maturityToUse);
						if (daysSincePlantingForMaturity > 0) {
							realMaturityEntirePixel.useLongValue(daysSincePlantingForMaturity);
						} else {
							// we still have some sort of maturity failure... so we should assess a problem
							System.out.println("grab real: pd = " + plantingDate + " ; mat = " + maturityToUse + " ; diff = " + daysSincePlantingForMaturity);
							realBadThingsCountsEntirePixel.useLongValue(1000000000); // assess a problem... a myriad possibilities but i'm too lazy...
						}
					} else { // System.out.println("grab real results: maturity failed...; not counting in maturity average");
						realBadThingsCountsEntirePixel.useLongValue(1000000000); // assess a problem...
					}

					// yields proper
					
					// now, since we are keeping everything (this is the true yearly results, after all), we need
					// to keep even the -99's as crop failures. perhaps, we should encode them as a very very small yield rather
					// than zero, just so we can see them, but so they don't move the averages too far? but then that gives us the
					// problem of negative yields. so we'll just censor at zero.
					if (yieldToUse < 0) {
						yieldToUse = 0;
					}
					
					// also, i want to see if the maturity length is like way crazy. if so, again: write down zero
					if (daysSincePlantingForMaturity <= 0 || daysSincePlantingForMaturity > happyMaturityThresholdToDoRealRuns) {
						yieldToUse = 0;
					}
					
					// now, write it down...
					// the yield gets used once for the overall average and once for the yearly
					realYieldsEntirePixel.useLongValue(yieldToUse);
					realYearlyYields[fakeYearIndex].useLongValue(yieldToUse);

					// the extras...
					// first, read them...
					for (int extraIndex = 0; extraIndex < (splitLine.length - magicOffsetForExtraNamesOutputIndex); extraIndex++) {
						stringToParse = splitLine[extraIndex + magicOffsetForExtraNamesOutputIndex];

						// check if there are the silly stars in there...
						if (stringToParse.contains("*")) {
							// ok, let's try this again, but we'll just skip over the offending value but keep the others from this line...
							everythingIsValid[extraIndex] = false;
							System.out.println("REAL: got ***'s in #" + extraIndex + " " + extraNames[extraIndex] + ": [" + stringToParse + 
									"] (data line " + lineIndex + "; fakeYear " + fakeYearIndex + " planting window index " + plantingWindowIndex + "):");
							realBadThingsCountsEntirePixel.useLongValue(1); // assess that something bad happened...
						}

						if (everythingIsValid[extraIndex]) {
							try {
								extractedValues[extraIndex] = Double.parseDouble(stringToParse);
							} catch (NumberFormatException nfe) {
								// check to see if we have two negative signs or two decimals since those are the
								// obvious problems...
								if (stringToParse.indexOf("-") != stringToParse.lastIndexOf("-") ||
										stringToParse.indexOf(".") != stringToParse.lastIndexOf(".") ||
										stringToParse.length() >= magicMaximumLengthForExtraNames) {
									System.out.println("  two numbers likely smooshed together: [" + stringToParse + "] for column " + extraNames[extraIndex]);

									// break out of the loop so we don't mismatch values
									break;
								} // if an obvious case

								throw nfe;
							} // catch
						} // if everything is valid

					} // for extraIndex

					// second write them down
					for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
						if (everythingIsValid[extraIndex]) {
						    // the overall average
						    extraSummaryAccumulators[extraIndex].useDoubleValue(extractedValues[extraIndex]);
						    
						    // check to see if we want this for the yearly version
						    for (int wantThisIndex = 0; wantThisIndex < allExtraToRecordIndices.length; wantThisIndex++ )
							if (extraIndex == allExtraToRecordIndices[wantThisIndex]) {
							    // yes, we are being stupid and not checking for uniqueness
							    // notice all the convoluted indexing.
							    // the value is coming from "extraIndex"
							    // the value is being stored in "wantThisIndex"
							    // and this is done only if the real index represented
							    //     by wantThisIndex (i.e., allExtraToRecordIndices[wantThisIndex]
							    //     matches with the extraIndex we are looking at at the moment.
							    extraYearly[wantThisIndex][fakeYearIndex].useDoubleValue(extractedValues[extraIndex]);
							}
						    } else {
							// well, if it isn't valid, i want to mark that in the yearly results....
							// this might be able to be done more cleanly, but i don't care at the moment (11jun15)
							
							// Beware the MAGIC NUMBER!!! missing value
							double magicMissingValue = -3;
							// check to see if we want this for the yearly version
							for (int wantThisIndex = 0; wantThisIndex < allExtraToRecordIndices.length; wantThisIndex++ )
							    if (extraIndex == allExtraToRecordIndices[wantThisIndex]) {
								// yes, we are being stupid and not checking for uniqueness
								// notice all the convoluted indexing.
								// the value is coming from "extraIndex"
								// the value is being stored in "wantThisIndex"
								// and this is done only if the real index represented
								//     by wantThisIndex (i.e., allExtraToRecordIndices[wantThisIndex]
								//     matches with the extraIndex we are looking at at the moment.
								extraYearly[wantThisIndex][fakeYearIndex].useDoubleValue(magicMissingValue);
							    }
						    }
					}

				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("REAL: had trouble reading one of the following (data line " + lineIndex + "; fakeYear " + fakeYearIndex + " planting window index " + plantingWindowIndex + "):");
					System.out.println("yield [" + splitLine[magicHarvestedWeightAtHarvestOutputIndex] + "]");
					System.out.println("planting  [" + splitLine[magicPlantingDateOutputIndex] + "]");
					System.out.println("anthesis  [" + splitLine[magicAnthesisDateOutputIndex] + "]");
					System.out.println("maturity  [" + splitLine[magicMaturityDateOutputIndex] + "]");
					
					// all the other goodies...
					// sometimes things get screwed up with a plant (sorghum, i'm looking at you!) not dying off and hence
					// runs off the end of the available weather. this can result in some bizarro spacing issues with -99.00-99.00
					// which then means that when we split on space, we don't get the right number of items. sooo...
					// this display needs to run for the length of the splitLine array, not the extraNames length
					
					if (splitLine.length - magicOffsetForExtraNamesOutputIndex != extraNames.length) {
						System.out.println("lack of whitespace problem: expecting " + extraNames.length + " items, but got " + (splitLine.length - magicOffsetForExtraNamesOutputIndex));
					}
					for (int extraIndex = 0; extraIndex < splitLine.length - magicOffsetForExtraNamesOutputIndex; extraIndex++) {
						
						System.out.println("extras [" + extraIndex + "] " + extraNames[extraIndex] + " = [" +
								splitLine[extraIndex + magicOffsetForExtraNamesOutputIndex]	+ "]");
					}
					System.out.println("     -> something bad happened with the essentials, skipping this line (index = " + lineIndex + ")... <-");
				} catch (NullPointerException npe) {
					// newly added on 19mar12 to try to deal with sorghum's funny business where sometimes it seems like
					// it gets positive yield with zero units, so the per-unit-size goes undefined....
					System.out.println("   fakeYearIndex = [" + fakeYearIndex + "]; magicDSSATSummaryLineIndexToRead = [" + magicDSSATSummaryLineIndexToRead + "]");
					System.out.println("   a null pointer exception came up when trying to read the REAL summary: guess is: ["
							+ candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] + "]");
					System.out.println("   skipping this line (index = " + lineIndex + ")...");
				}
				
				
			} // end for fakeYears...
			
		}

		
		

	
////////
	


	
	
	public void doSimulationsOnExistingWeather() throws Exception {

		//////////////////////////////////////////////////
		// check if everything has been set up properly //
		//////////////////////////////////////////////////
		if (!this.readyToRun) {
			System.out.println("DSSATRunner not ready to run... exiting...");
			return;
		}

		// set up a timer for the fun of it...
		TimerUtility thisTimer = new TimerUtility();

		TimerUtility weatherTimer = new TimerUtility();

		
		TimerUtility happyTimer = new TimerUtility();
		TimerUtility realTimer = new TimerUtility();

		TimerUtility writingTimer = new TimerUtility();
		TimerUtility readingTimer = new TimerUtility();

		TimerUtility logTimer = new TimerUtility();
		TimerUtility elseTimer = new TimerUtility();

		double timeSinceStart = -4.3;
		double projectedTime = -4.5;
		double timeRemaining = -4.6;



		realYieldsEntirePixel    = new DescriptiveStatisticsUtility(false);
		happyYieldsEntirePixel   = new DescriptiveStatisticsUtility(false);
		realAnthesisEntirePixel  = new DescriptiveStatisticsUtility(false);
		realMaturityEntirePixel  = new DescriptiveStatisticsUtility(false);
		happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
		realBadThingsCountsEntirePixel = new DescriptiveStatisticsUtility(false);
		realTimeToPlantingEntirePixel  = new DescriptiveStatisticsUtility(false);
		realNoPlantingEntirePixel      = new DescriptiveStatisticsUtility(false);


		DescriptiveStatisticsUtility weatherTimerStats = new DescriptiveStatisticsUtility(true);

		DescriptiveStatisticsUtility happyTimerStats = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility realTimerStats  = new DescriptiveStatisticsUtility(true);

		DescriptiveStatisticsUtility readingTimerStats  = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility writingTimerStats  = new DescriptiveStatisticsUtility(true);

		DescriptiveStatisticsUtility logTimerStats  = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility elseTimerStats  = new DescriptiveStatisticsUtility(true);

		///////////////////////////////////
		// write out the provenance file //
		///////////////////////////////////
		this.writeProvenance();

		///////////////////////////////////////
		// read in the whole template X file //
		///////////////////////////////////////
		String templateXFileContents = FunTricks.readTextFileToString(templateXFile);

		///////////////////////////
		// variable declarations //
		///////////////////////////

//		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		double nitrogenLevel = Double.NaN;
		int soilType = -1, firstPlantingDay = -4;
		int startingDayToPlantForThisWindow = -1;
		int endingDayToPlantForThisWindow = -99;
		int startingDateAsInt = -59; 
		
		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;
		String harvestDayCode  = null;
		String fertilizerBlock  = null;
		String irrigationBlock  = null;

		String randomSeedCode = null;
		String nYearsCode = null;
		String nHappyYearsCode = null;
		String co2ppmCode = null;

		String soilTypeString = null;

		int[] phenologyInDays = null;

		String statisticsOutLine = null;

		double totalInitialNitrogenKgPerHa = -5, rootWeight = -6, surfaceResidueWeight = -7,  latitude, longitude;
//		double idNumberForWeather = -9;
		
//		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		String XHappyStuffToWrite = null;
		String initializationBlock = null;

		File originalWeatherFile = null;
		File finalWeatherFile = null;
		String originalWeatherFileLocation = null;
		String finalWeatherFileLocation = null;


		////////////////////////////
		// a couple magic numbers //
		////////////////////////////

		long soilColIndex = 0;
		long firstPlantingDayIndex = 1;
		long nitrogenLevelIndex = 2;
		
		long nitrogenKgPerHaCol      = 3; // this is the daily style; old = 70;
		long rootWeightCol           = 4; // 71; // old = 72; noticed on 18jul12
		long surfaceResidueWeightCol = 5; // 72; // old = 71; noticed on 18jul12

		// i am always running real, so we need something to use for fertilizer schemes if happy looks silly
		final int defaultFlowering = 4;
		final int defaultMaturity = 8;

		
//		long idNumberForWeatherCol = 6; // new for the daily thing....

		////////////////////////////////////////
		// set up stuff that we actually know //
		////////////////////////////////////////

		String fullTempXFileName = pathToDSSATDirectory + tempXFileName;
		File fullTempXFile = new File(fullTempXFileName);

//		File weatherStationFile   = new File(magicWeatherStationNameToUsePath);

		// open up a writer for the statistics output file
		File statisticsFileObject = new File(yieldOutputBaseName  + "_STATS.txt");
		PrintWriter statisticsOut = new PrintWriter(statisticsFileObject);



		///////////////////////////////
		// start doing the real work //
		///////////////////////////////


		int plantingWindowSpacing = nDaysInMonth / nPlantingWindowsPerMonth;
		if (plantingWindowSpacing < 1) {
			plantingWindowSpacing = 1;
		}

		// write out the DSSAT initialization file...
		FunTricks.writeStringToFile(magicInitializationContents, magicInitializationFilePath);

		// initialize the fertilizer scheme
		// this could be done elsewhere, but just in case, i'm doing it here.
		// we're starting out with the defaults. but in the future this might
		// get extended....
		nitrogenFertilizerScheme.initialize();

		// read in the fundamental data
		// Beware the MAGIC NUMBER!!! gonna force these into memory.
		int formatIndexToForce = 1;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);


		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];



		// since this implementation (using the multiple years with a single random seed, rather
		// than multiple random seeds with a single year) has the seeds and years as invariants, do
		// them up front to save on a little search and replace overhead...
		randomSeedCode  = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
		nYearsCode      = DSSATHelperMethods.padWithZeros(nFakeYears, 5);
		// now forcing happy and real to be done every time
//		nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
		nHappyYearsCode = DSSATHelperMethods.padWithZeros(nFakeYears, 5);
		co2ppmCode      = DSSATHelperMethods.padWithZeros(co2ppm, 4);

		
		// AND GOING BACK TO MULTIPLE YEARS....
		// Beware the MAGIC NUMBER!!! since we will try to use a bunch of single years, we need to
		// set it up as putting "1" in the number of years place... this will go inside a for
		// loop over random seeds to do the desired number of years...
//		nYearsCode      = DSSATHelperMethods.padWithZeros(1, 5);
//		nHappyYearsCode = DSSATHelperMethods.padWithZeros(1, 5);

		

		IrrigationScheme dummyScheme = new IrriSNone();
		dummyScheme.initialize();

		////////////////////////////////
		// get the soil profile ready //
		////////////////////////////////

		// Beware the MAGIC ASSUMPTION!!! the soil file is the first two characters of the soil name with a .SOL
		SoilProfile soilProfiles = new SoilProfile(magicSoilPrefix.substring(0, 2) + ".SOL"); 

		// make a dummy initialization for the happy plant...
//		String dummyInitializationBlock = soilProfiles.makeInitializationBlockFractionBetweenBounds(
//		magicSoilPrefix + "01",
//		fractionBetweenLowerLimitAndDrainedUpperLimit,
//		initializationStartPlaceholder,
//		nitrogenPPMforBothNH4NO2);
		String dummyInitializationBlock = null;

		// we need to replace all the easy stuff once so we don't have to do it repeatedly...
		// we will maintain the random seed placeholder for later use...
		String[] invariantsReplaced = this.buildUniqueInvariantsXTemplates(templateXFileContents, randomSeedPlaceholder, nHappyYearsCode, co2ppmCode, dummyScheme, nYearsCode);

		String XHappyInvariantsReplaced = invariantsReplaced[0];
		String XInvariantsReplaced      = invariantsReplaced[1];


		// start the timer for just DSSAT proper
		thisTimer.tic();

		// clear out the summary and error files if they exist...
		if (errorAsFileObject.exists()) {
			errorAsFileObject.delete();
		}
		if (summaryDSSATOutputAsFileObject.exists()) {
			summaryDSSATOutputAsFileObject.delete();
		}

		// adding a process thing to try to force streams closed to avoid
		// "Too many files open" errors
//		Process theRunningProcess = null;
//		InputStream theRunningErrorStream = null;
//		InputStream theRunningInputStream = null;
//		OutputStream theRunningOutputStream = null;

		System.out.println("-- starting through data --");
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

			elseTimer.tic();

			// initialize the descriptive statistics utility and storage array
			realYieldsEntirePixel.reset();
			happyYieldsEntirePixel.reset();
			realEmergenceEntirePixel.reset();
			realAnthesisEntirePixel.reset();
			realMaturityEntirePixel.reset();
			happyMaturityEntirePixel.reset();
			realBadThingsCountsEntirePixel.reset();
			realTimeToPlantingEntirePixel.reset();
			realNoPlantingEntirePixel.reset();

			// resetting is fine to do the extra couple that might be missing from the OLD stuff
			for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
				extraSummaryAccumulators[extraIndex].reset();
			}

			for (int yearIndex = 0; yearIndex < nFakeYears; yearIndex++) {
				happyYearlyYields[yearIndex].reset();
				 realYearlyYields[yearIndex].reset();
			}
			
			for (int extraIndex = 0; extraIndex < allExtraToRecordIndices.length; extraIndex++) {
			for (int yearIndex = 0; yearIndex < nFakeYears; yearIndex++) {
			    extraYearly[extraIndex][yearIndex].reset();;
			}
			}


			

			soilType         = (int)dataMatrix.getValue(lineIndex,soilColIndex);
			// elevation        = nonClimateInfo[1]; // don't need this here...
			// plantingMonth    = nonClimateInfo[2]; // don't need this here...
			firstPlantingDay = DSSATHelperMethods.firstPlantingDateFromMonth((int)dataMatrix.getValue(lineIndex,firstPlantingDayIndex)); // nonClimateInfo[3];
			nitrogenLevel = dataMatrix.getValue(lineIndex,nitrogenLevelIndex); // nonClimateInfo[4];

//			idNumberForWeather = dataMatrix.getValue(lineIndex,idNumberForWeatherCol);
			 latitude = geogMatrix.getValue(lineIndex,2); // Beware the MAGIC NUMBER!!!
			longitude = geogMatrix.getValue(lineIndex,3); // Beware the MAGIC NUMBER!!!

			// brute force padding
			soilTypeString = magicSoilPrefix + FunTricks.padStringWithLeadingZeros(Integer.toString(soilType), totalLengthForSoilType - magicSoilPrefix.length());


			// let us now rename the weather data file that will be used for this pixel

			// determine filename of real .WTH file
			// determine the dummy spot to put it in
			// do the moving...
			// since the generator is stupid and won't take long filenames, we have to generate then move...

			elseTimerStats.useDoubleValue(elseTimer.tocMillis());
			weatherTimer.tic();

			// Beware the MAGIC NUMBER!!!
//			originalWeatherFileLocation = baseNameOfDailyWeather + "_" + idNumberForWeather + ".WTH"; // using ".WTH"
//			originalWeatherFileLocation = baseNameOfDailyWeather + "_"  + latitude + "n_" + longitude + "e" + ".WTH";
//			originalWeatherFileLocation = baseNameOfDailyWeather + "_"  + latitude + "_" + longitude + ".WTH";
			originalWeatherFileLocation = baseNameOfDailyWeather + "_"  + latitude + "_" + longitude + weatherDataSuffix;
			
			originalWeatherFile = new File(originalWeatherFileLocation);
			
			if (!originalWeatherFile.exists()) {
				System.out.println("\tthe weather file at [" + originalWeatherFileLocation + "]; lineIndex=" + lineIndex + " FAILED...");
//				throw new Exception();
				// put some silly values in for the happy plant so we know what happened...
				// Beware the MAGIC NUMBER!!! the warning that something funny is happening... that is, the weather file went missing
				this.realYieldsEntirePixel.useLongValue(-2L);
				this.realMaturityEntirePixel.useLongValue(-2L);
				this.happyYieldsEntirePixel.useLongValue(-2L);
				this.happyMaturityEntirePixel.useLongValue(-2L);
			} else { 
				
			
			finalWeatherFileLocation = magicWeatherStationNameToUse+ ".WTH";
			finalWeatherFile = new File(finalWeatherFileLocation);

			originalWeatherFile.renameTo(finalWeatherFile);

			weatherTimerStats.useDoubleValue(weatherTimer.tocMillis());
			
			
			
			
			
			// loop over the planting windows and random seeds
			for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
				elseTimer.tic();
				
//				// pick the starting day/etc for this window
//				startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
//				startingDayToPlantCode = DSSATHelperMethods.yearDayToYYDDD(fakePlantingYear, startingDayToPlantForThisWindow);
//
////				System.out.println("fPD = [" + firstPlantingDay + "], pWI = [" + plantingWindowIndex + "], pWS = [" + plantingWindowSpacing + "] --> sDTPFTW = [" +
////						startingDayToPlantForThisWindow + "], fake planting year = [" + fakePlantingYear + "], coded version = [" + startingDayToPlantCode + "]");
//				startingDateAsInt = Integer.parseInt(startingDayToPlantCode);
//				
//				// pick the ending day/etc for this window
//				endingDayToPlantForThisWindow = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, plantingWindowLengthDays);
//				endingDayToPlantCode = DSSATHelperMethods.padWithZeros(endingDayToPlantForThisWindow,5);
//
//				initializationDayCode = DSSATHelperMethods.padWithZeros(
//						DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, -spinUpTimeDays),5
//						);
//				
//				harvestDayCode = DSSATHelperMethods.padWithZeros(
//						DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, optionalHarvestInterval),5
//						);

				// pick the starting day/etc for this window
				startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
				startingDateAsInt = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(fakePlantingYear*1000 + startingDayToPlantForThisWindow, plantingDateInMonthShiftInDays);
				startingDayToPlantCode = DSSATHelperMethods.padWithZeros(startingDateAsInt,5); 


				// pick the ending day/etc for this window
				endingDayToPlantForThisWindow = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, plantingWindowLengthDays);
				endingDayToPlantCode = DSSATHelperMethods.padWithZeros(endingDayToPlantForThisWindow,5);

				initializationDayCode = DSSATHelperMethods.padWithZeros(
					DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, -spinUpTimeDays),5
					);

				harvestDayCode = DSSATHelperMethods.padWithZeros(
					DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(startingDateAsInt, optionalHarvestInterval),5
					);

				
				
				//////////////////////////////////////
				// let's run a happy plant first... //
				//////////////////////////////////////

				// X file
				dummyInitializationBlock = soilProfiles.makeInitializationAndSoilAnalysisBlock(
						soilTypeString,
						fractionBetweenLowerLimitAndDrainedUpperLimit,
						initializationDayCode,
						totalInitialNitrogenKgPerHa,
						depthForNitrogen,
						rootWeight,
						surfaceResidueWeight,
						residueNitrogenPercent,
						incorporationRate,
						incorporationDepth,
						standardDepths,
						clayStableFractionAbove,
						loamStableFractionAbove,
						sandStableFractionAbove
				);
				initializationBlock = "! " + soilTypeString + " " +
				fractionBetweenLowerLimitAndDrainedUpperLimit + " " +
				initializationDayCode + " " +
				totalInitialNitrogenKgPerHa + " " +
				depthForNitrogen + " " +
				rootWeight + " " +
				surfaceResidueWeight + " " +
				residueNitrogenPercent + " " +
				incorporationRate + " " +
				incorporationDepth + " " +
				standardDepths + " " +
				clayStableFractionAbove + " " +
				loamStableFractionAbove + " " +
				sandStableFractionAbove
				+ "\n\n\n" +
				initializationBlock;


				elseTimerStats.useDoubleValue(elseTimer.tocMillis());


				elseTimer.tic();

				// now we do this inside the loop....
				XHappyStuffToWrite = XHappyInvariantsReplaced.replaceAll(soilInitializationPlaceholder, dummyInitializationBlock);

				// do the search and replace thing; the invariants have already been done above...
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(soilPlaceholder               , soilTypeString);
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(harvestingDatePlaceholder     , harvestDayCode);
//				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(nYearsOrRandomSeedsPlaceholder, DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology,5));
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(nYearsOrRandomSeedsPlaceholder, DSSATHelperMethods.padWithZeros(nFakeYears,5));
//				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(this.randomSeedPlaceholder,Integer.toString(this.firstRandomSeed + happyPlantRunIndex));
//				randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed,5);
				XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(randomSeedPlaceholder,randomSeedCode);

				elseTimerStats.useDoubleValue(elseTimer.tocMillis());


				writingTimer.tic();
				FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);
				writingTimerStats.useDoubleValue(writingTimer.tocMillis());


				// run DSSAT with the happy plant
				happyTimer.tic();


				// ok, i've had trouble with unstable ORYZA-in-DSSAT not finishing when run under here, but it does
				// just fine manually. go figure. so first, i'm going to run it with a timer. if i get that working,
				// then i can start having it retry a few times before giving up...
				
				SystemCallWithTimeout happyRunnerThing = new SystemCallWithTimeout();

				for (int rerunIndex = 0; rerunIndex < rerunAttemptsMax ; rerunIndex++) {
					happyRunnerThing.setup(dssatExecutionCommand, pathToDSSATDirectoryAsFile, (int)Math.ceil(maxRunTime * Math.pow(bumpUpMultiplier, rerunIndex)), testIntervalToUse);
					happyRunnerThing.run();

					if (happyRunnerThing.finishedCleanly() != SystemCallWithTimeout.SYSTEM_CALL_RAN_FINE) {
						// check how many lines ended up in Summary.OUT
						if (new File(magicDSSATSummaryToRead).exists()) {
							System.out.println("     +++ happy timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
									+ " lines in Summary.OUT = " + FunTricks.nLinesInTextFile(magicDSSATSummaryToRead) + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
							// check one more time to see if the length goal has been met because sometimes it is by the time we get to here...
							if (FunTricks.nLinesInTextFile(magicDSSATSummaryToRead) == (nFakeYears + magicDSSATSummaryLineIndexToRead)) {
								System.out.println("  apparently, the goal was met by the time we got here, moving on...");
								break;
							}
						} else {
							System.out.println("     +++ happy timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
									+ " lines in Summary.OUT = " + 0 + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
						}
					} else {
						// it seems all is well
						break;
					}
				}
				
//				theRunningProcess = Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
//				// wait for it to finish up
//				theRunningProcess.waitFor();
				happyTimerStats.useDoubleValue(happyTimer.tocMillis());

				// make sure the streams got closed for the process after we managed to read everything...
//				theRunningErrorStream = theRunningProcess.getErrorStream();
//				theRunningErrorStream.close();
//				theRunningInputStream = theRunningProcess.getInputStream();
//				theRunningInputStream.close();
//				theRunningOutputStream = theRunningProcess.getOutputStream();
//				theRunningOutputStream.close();
//				theRunningProcess.destroy();

				elseTimer.tic();
				// recommend garbage collection
				System.gc();
				elseTimerStats.useDoubleValue(elseTimer.tocMillis());

				//////////////////////////////////////////
				// extract the results & store to array //
				//////////////////////////////////////////

				// Beware the MAGIC NUMBER!!! with unique seeds, just look at a single year...
				// we are running a single year...
				// mean yield / anthesis / maturity in days after planting...
//				if (useOldSummaryIndices) {
//				phenologyInDays = this.grabOLDHappyResults(1); // nHappyPlantRunsForPhenology);
//				} else {
//				phenologyInDays = this.grabNewHappyResults(1); // nHappyPlantRunsForPhenology);
//				}

				readingTimer.tic();
				phenologyInDays = this.grabUnifiedHappyResults(nFakeYears);
//				phenologyInDays = this.grabUnifiedHappyResults(nHappyPlantRunsForPhenology);
				readingTimerStats.useDoubleValue(readingTimer.tocMillis());

				// i no longer care about saving time by dropping bad happy results. we're just going to plow through regardless
				//  but then the problem becomes what to do if we don't get a maturity length or something...

				// default the flowering to 4 days if the happy flowering looks strange
				if (phenologyInDays[1] < defaultFlowering) {
					phenologyInDays[1] = defaultFlowering;
				}
				if (phenologyInDays[2] < defaultMaturity) {
					phenologyInDays[2] = defaultMaturity;
				}
				
				// proceed, if the happy yield exceeds some theshold and the effective growing season isn't too long....
//				if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {

//					for (int normalPlantRunIndex = 0; normalPlantRunIndex < this.nFakeYears; normalPlantRunIndex++) {

					elseTimer.tic();
					/////////////////////
					// make the X file //
					/////////////////////

					// create the fertilizer block...
					fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
							phenologyInDays[1] + phenologyBufferInDays, phenologyInDays[2] + phenologyBufferInDays, nitrogenLevel
					);

//					irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), nRandomSeedsToUse);
					// Beware the MAGIC NUMBER!!! we only care about a single season here...
					irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), 1);
					irrigationBlock = irrigationScheme.buildIrrigationBlock();


					// do the search and replace thing; the invariants have already been done above...
					XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
					// put in the random seed to use
//					XStuffToWrite = XStuffToWrite.replaceAll(this.randomSeedPlaceholder,Integer.toString(firstRandomSeed + normalPlantRunIndex));

//					randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed,5);

					XStuffToWrite = XStuffToWrite.replaceAll(randomSeedPlaceholder,randomSeedCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(harvestingDatePlaceholder     , harvestDayCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);
					XStuffToWrite =       XStuffToWrite.replaceAll(irrigationPlaceholder, irrigationBlock);
					XStuffToWrite =       XStuffToWrite.replaceAll(nYearsOrRandomSeedsPlaceholder, DSSATHelperMethods.padWithZeros(nFakeYears,5));

					// pull the values from the data matrix
					// Beware the MAGIC NUMBER!!! the columns in the data file corresponding to the various initial conditions
					totalInitialNitrogenKgPerHa = dataMatrix.getValue(lineIndex,nitrogenKgPerHaCol);
					rootWeight                  = dataMatrix.getValue(lineIndex,rootWeightCol);
					surfaceResidueWeight        = dataMatrix.getValue(lineIndex,surfaceResidueWeightCol);

					initializationBlock = soilProfiles.makeInitializationAndSoilAnalysisBlock(
							soilTypeString,
							fractionBetweenLowerLimitAndDrainedUpperLimit,
							initializationDayCode,
							totalInitialNitrogenKgPerHa,
							depthForNitrogen,
							rootWeight,
							surfaceResidueWeight,
							residueNitrogenPercent,
							incorporationRate,
							incorporationDepth,
							standardDepths,
							clayStableFractionAbove,
							loamStableFractionAbove,
							sandStableFractionAbove
					);
					initializationBlock = "! " + soilTypeString + " " +
					fractionBetweenLowerLimitAndDrainedUpperLimit + " " +
					initializationDayCode + " " +
					totalInitialNitrogenKgPerHa + " " +
					depthForNitrogen + " " +
					rootWeight + " " +
					surfaceResidueWeight + " " +
					residueNitrogenPercent + " " +
					incorporationRate + " " +
					incorporationDepth + " " +
					standardDepths + " " +
					clayStableFractionAbove + " " +
					loamStableFractionAbove + " " +
					sandStableFractionAbove
					+ "\n\n\n" +
					initializationBlock;

					XStuffToWrite = XStuffToWrite.replaceAll(soilInitializationPlaceholder, initializationBlock);

					elseTimerStats.useDoubleValue(elseTimer.tocMillis());

					// overwrite the old file with the new contents

					writingTimer.tic();
					FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
					writingTimerStats.useDoubleValue(writingTimer.tocMillis());

					elseTimer.tic();
					// recommend garbage collection
					System.gc();
					elseTimerStats.useDoubleValue(elseTimer.tocMillis());

					///////////////
					// run DSSAT //
					///////////////

					realTimer.tic();

					
					SystemCallWithTimeout realRunnerThing = new SystemCallWithTimeout();

					for (int rerunIndex = 0; rerunIndex < rerunAttemptsMax ; rerunIndex++) {
						realRunnerThing.setup(dssatExecutionCommand, pathToDSSATDirectoryAsFile, (int)Math.ceil(maxRunTime * Math.pow(bumpUpMultiplier, rerunIndex)), testIntervalToUse);
						realRunnerThing.run();

						if (realRunnerThing.finishedCleanly() != SystemCallWithTimeout.SYSTEM_CALL_RAN_FINE) {
							// check how many lines ended up in Summary.OUT
//							System.out.println("     +++ real timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") [" + realRunnerThing.finishedCleanly() + "] timedoutcode=" + happyRunnerThing.SYSTEM_CALL_TIMED_OUT
//							System.out.println("     +++ real timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
//									+ " lines in Summary.OUT = " + FunTricks.nLinesInTextFile(magicDSSATSummaryToRead) + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
							if (new File(magicDSSATSummaryToRead).exists()) {
								System.out.println("     +++ happy timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
										+ " lines in Summary.OUT = " + FunTricks.nLinesInTextFile(magicDSSATSummaryToRead) + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
								// check one more time to see if the length goal has been met because sometimes it is by the time we get to here...
								if (FunTricks.nLinesInTextFile(magicDSSATSummaryToRead) == (nFakeYears + magicDSSATSummaryLineIndexToRead)) {
									System.out.println("  apparently, the goal was met by the time we got here, moving on...");
									break;
								}

							} else {
								System.out.println("     +++ happy timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
										+ " lines in Summary.OUT = " + 0 + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
							}

						} else {
							// it seems all is well
							break;
						}
					}

					
					realTimerStats.useDoubleValue(realTimer.tocMillis());

					//////////////////////////////////////////
					// extract the results & store to array //
					//////////////////////////////////////////

					readingTimer.tic();
					grabUnifiedNewManyResults(nFakeYears, lineIndex, plantingWindowIndex);
					readingTimerStats.useDoubleValue(readingTimer.tocMillis());


					elseTimer.tic();
					// make sure the streams got closed for the process after we managed to read everything...
//					theRunningErrorStream = theRunningProcess.getErrorStream();
//					theRunningErrorStream.close();
//					theRunningInputStream = theRunningProcess.getInputStream();
//					theRunningInputStream.close();
//					theRunningOutputStream = theRunningProcess.getOutputStream();
//					theRunningOutputStream.close();
//					theRunningProcess.destroy();

					// bail out if we have already encountered bad things for this pixel...
//					if (badThingsHappened) {
//						break;
//					}

					elseTimerStats.useDoubleValue(elseTimer.tocMillis());


			} // end plantingWindowIndex

		} // weather file exists

			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////


			elseTimer.tic();
			// do the summary out stuff

			statisticsOutLine = "";

			// first, let us check whether anything got recorded...
			if (realYieldsEntirePixel.getN() == 0) {

				statisticsOutLine = 
					(-1) + delimiter 
					+ 0 + delimiter 
					+ 0 + delimiter 
					+ 0 + delimiter 
					+ 0 + delimiter 
					+ 0 + delimiter
					+ 0 + delimiter
					+ 0 + delimiter
					+ 0 + delimiter
					+ 0 + delimiter
					+ 0 + delimiter
					+ FunTricks.onlySomeDecimalPlaces(happyYieldsEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(happyMaturityEntirePixel.getMean(),nDecimalsInOutput);

				for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
					statisticsOutLine += delimiter + 0;
				}
				
//				statisticsOutLine += delimiter + 0 + delimiter + (-1);
				statisticsOutLine += delimiter + (-this.spinUpTimeDays) + delimiter + (-1) +
				delimiter + realYieldsEntirePixel.getN() + delimiter + happyYieldsEntirePixel.getN();

				if (allFlag) {
	
					for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
						statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(realYearlyYields[fakeYearIndex].getMean(),nDecimalsInOutput);
					}
				
					for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
						statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(happyYearlyYields[fakeYearIndex].getMean(),nDecimalsInOutput);
					}
	
		/*			
					// the "real" years
					for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
						statisticsOutLine += delimiter + 0;
					}
					// the "fake" years
					for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
						statisticsOutLine += delimiter + 0;
					}
	*/
				}


			} else {

				statisticsOutLine = 
					realBadThingsCountsEntirePixel.getTotalLong() + delimiter
					+ realYieldsEntirePixel.getMinAsLong() + delimiter 
					+ realYieldsEntirePixel.getMaxAsLong() + delimiter 
					+ FunTricks.onlySomeDecimalPlaces(realYieldsEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realYieldsEntirePixel.getStd(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realEmergenceEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realEmergenceEntirePixel.getStd(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realAnthesisEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realAnthesisEntirePixel.getStd(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realMaturityEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(realMaturityEntirePixel.getStd(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(happyYieldsEntirePixel.getMean(),nDecimalsInOutput) + delimiter
					+ FunTricks.onlySomeDecimalPlaces(happyMaturityEntirePixel.getMean(),nDecimalsInOutput);

				for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
					statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(extraSummaryAccumulators[extraIndex].getMean(),nDecimalsInOutput);
				}
				
//				statisticsOutLine += delimiter + realTimeToPlantingEntirePixel.getMean()
				statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces((realTimeToPlantingEntirePixel.getMean() - this.spinUpTimeDays),nDecimalsInOutput)
				+ delimiter + realNoPlantingEntirePixel.getN() +
				delimiter + realYieldsEntirePixel.getN() + delimiter + happyYieldsEntirePixel.getN();;

				if (allFlag) {
				    for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
					statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(realYearlyYields[fakeYearIndex].getMean(),nDecimalsInOutput);
				    }

				    for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
					statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(happyYearlyYields[fakeYearIndex].getMean(),nDecimalsInOutput);
				    }

				    // the yearly extras...
				    for (int wantThisIndex = 0; wantThisIndex < allExtraToRecordIndices.length; wantThisIndex++) {
					for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
					    statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(extraYearly[wantThisIndex][fakeYearIndex].getMean(),nDecimalsInOutput);
					}
				    }

				} // end if(allFlag)

			}

			statisticsOutLine += "\n";

			elseTimerStats.useDoubleValue(elseTimer.tocMillis());

			writingTimer.tic();
			statisticsOut.print(statisticsOutLine);
			writingTimerStats.useDoubleValue(writingTimer.tocMillis());

//			statisticsOut.flush();

			// now we're ready to deal with the next pixel...
			// Beware the MAGIC NUMBER!!! checking every one percent...

			logTimer.tic();
			if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {

				timeSinceStart = thisTimer.sinceStartSeconds();
//				averageTimePerEvent = timeForWeather.getMean() + timeForORYZA.getMean();
				projectedTime = timeSinceStart / (lineIndex + 1) * nLinesInDataFile;
				timeRemaining = projectedTime - timeSinceStart;

				System.out.println("prog: " + (lineIndex + 1) + "/" + nLinesInDataFile + " = " +
						FunTricks.fitInNCharacters((100 * (lineIndex + 1.0)/nLinesInDataFile),5) +
						"\tr/w/l/e/W: " + FunTricks.fitInNCharacters(readingTimerStats.getMean(), 6) +
											"/" + FunTricks.fitInNCharacters(writingTimerStats.getMean(), 6) +
											"/" + FunTricks.fitInNCharacters(logTimerStats.getMean(), 6) + 
											"/" + FunTricks.fitInNCharacters(elseTimerStats.getTotalDouble() / (lineIndex + 1), 6) + 
											"/" + FunTricks.fitInNCharacters(weatherTimerStats.getMean(), 6) + 
						"\tave R: " + FunTricks.fitInNCharacters(realTimerStats.getMean(),6) + 
						" ave H: " + FunTricks.fitInNCharacters(happyTimerStats.getMean(),6) +
						" N/P/R = " + FunTricks.fitInNCharacters(timeSinceStart,6) +
						"/" + FunTricks.fitInNCharacters(projectedTime,6) +
						"/" + FunTricks.fitInNCharacters(timeRemaining,6) +
						"\t" + FunTricks.fitInNCharacters(timeRemaining / 60, 6)
				);
			}
			logTimerStats.useDoubleValue(logTimer.tocMillis());


		} // end for for lineIndex // old end of while loop


		// close out the output files
		statisticsOut.flush();
		statisticsOut.close();

		/////////////////////////////////////////
		// when all done, write out info files //
		/////////////////////////////////////////
		// Beware the MAGIC NUMBER!!!
		String columnList = "n_bad_things" + delimiter 
		+ "yield_min" + delimiter + "yield_max" + delimiter 
		+ "yield_mean" + delimiter + "yield_std" + delimiter 
		+ "real_emergence_mean" + delimiter + "real_emergence_std" + delimiter 
		+ "real_anthesis_mean" + delimiter + "real_anthesis_std" + delimiter
		+ "real_maturity_mean" + delimiter + "real_maturity_std" + delimiter
		+ "happy_yield_mean" + delimiter + "happy_maturity_mean";
		
		for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
			columnList += delimiter + extraNames[extraIndex];
		}

		columnList += delimiter + "time_to_planting" + delimiter + "n_no_planting" + delimiter + "n_contributing_real" + delimiter + "n_contributing_happy";

		if (allFlag) {
		    
		    // the yields
		    for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
			columnList += delimiter + "real_" + fakeYearIndex;
		    }

		    for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
			columnList += delimiter + "happy_" + fakeYearIndex;
		    }
		    
		    // the yearly extras...
		    for (int wantThisIndex = 0; wantThisIndex < allExtraToRecordIndices.length; wantThisIndex++) {
		    for (int fakeYearIndex = 0; fakeYearIndex < this.nFakeYears; fakeYearIndex++) {
			columnList += delimiter + allExtraToRecordNames[wantThisIndex] + "_" + fakeYearIndex;
		    }
		    }
		    
		}

		
		columnList += "\n";


		FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

		long nRows = nLinesInDataFile;

		// Beware the MAGIC NUMBER!!!
		int nCols = -5;
		nCols = 13 + extraNames.length + 2 + 2; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		
		if (allFlag) {
		    // the yields
		    nCols += 2 * nFakeYears;
		    // the yearly extras...
		    nCols += allExtraToRecordIndices.length * nFakeYears;
		}
		
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);



		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
		thisTimer.sinceStartMessage("running DSSAT");
		double overallTimeMillis = thisTimer.sinceStartMillis();
		double totalDSSATTimeMillis = thisTimer.tocMillis();



		System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
				+ totalDSSATTimeMillis / nLinesInDataFile / nFakeYears / nPlantingWindowsPerMonth + "ms");
		System.out.println("overall per run average = " +
				overallTimeMillis/ nLinesInDataFile / nFakeYears / nPlantingWindowsPerMonth + "ms"	);


	} // main
	
	
////////
		
	



	public void placeholder() {
		return;
	}
}

