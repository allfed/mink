package org.DSSATRunner;

import java.io.*;
import java.util.Date;

import org.R2Useful.*;

public class DSSAT45Runner {

	private boolean readyToRun = false;


	/////////////////////////////////////
	// create a bunch of magic numbers //
	/////////////////////////////////////
	public static final String delimiter = "\t";
	public static final String magicWeatherStationNameToUse     = "RICK";
	public static final String magicDSSATSummaryToRead          = "Summary.OUT";
	public static final String magicErrorFile                   = "ERROR.OUT";
	public static final String magicInitializationFile          = "deleteme.dv4";
	public static final String tempXFileName                    = "deleteme.meX";
	
	// old for dssat 4.0
//	public static final String magicInitializationContents = "*BATCH\n@FILEX       TRTNO\n" + tempXFileName + "     1\n";

	public static final String magicInitializationContents = "*BATCH\n@FILEX       TRTNO\n" + tempXFileName + "     1\n";

	
	// search and replace elements in the template X file
	public static final String soilPlaceholder                = "ssssssssss";
	public static final String initializationStartPlaceholder = "iiiiS";
	public static final String plantingDateStartPlaceholder   = "ppppS";
	public static final String plantingDateEndPlaceholder     = "ppppE";
	public static final String randomSeedPlaceholder          = "rrrrr";
	public static final String nYearsOrRandomSeedsPlaceholder = "nnnnn";
	public static final String weatherPlaceholder             = "wwww";
	public static final String co2ppmPlaceholder              = "co2p";
	public static final String fertilizerPlaceholder          = "___place fertilizers here___";

	public static final int hardLimitOnReReads = 200;

	public static final int hardLimitOnHappyReadingErrors = 20;

	public static final int nDaysInMonth =  30;
	public static final int nDaysInYear  = 365;

	// details for DSSAT's summary output files...
	private int magicDSSATSummaryLineIndexToRead = 4;
	private int magicDSSATSummaryLineLength = 350;
	
	private int magicHarvestedWeightAtHarvestStartIndex = 136; // character # 137, index 136
	private int magicHarvestedWeightAtHarvestEndIndex   = 141; // character # 141, index 140

	private int magicPlantingDateStartIndex = 86; // character # 137, index 136
	private int magicPlantingDateEndIndex   = 93; // character # 141, index 140

	private int magicAnthesisDateStartIndex = 94; // character # 137, index 136
	private int magicAnthesisDateEndIndex   = 101; // character # 141, index 140

	private int magicMaturityDateStartIndex = 102; // character # 137, index 136
	private int magicMaturityDateEndIndex   = 109; // character # 141, index 140

	private static final String[] extraNames     = {"IR#M","IRCM","PRCM","ROCM","DRCM","NICM","NFXM","NUCM","NLCM","NIAM","CNAM","GNAM"};
	private static final int[] extraStartIndices = new int[] {189,195,201,225,231,249,255,261,267,273,279,285};
	private static final int[] extraEndIndices   = new int[] {194,200,206,230,236,254,260,266,272,278,284,290};
	
	// crop categories.... each needs a unique id number...
	
	private static final String maizeString      = "maize";
	private static final String riceString       = "rice";
	private static final String wheatString      = "wheat";
	private static final String soybeansString   = "soybeans";
	private static final String groundnutsString = "groundnuts";
	private static final String cottonString     = "cotton";

//	private static final int maizeInt      = 1;
//	private static final int riceInt       = 2;
//	private static final int wheatInt      = 3;
//	private static final int soybeansInt   = 4;
//	private static final int groundnutsInt = 5;
//	private static final int cottonInt     = 6;
	
	
	/////////////////////////////////////////////////////
	// other variables which are good to have sharable //
	/////////////////////////////////////////////////////
	
	private String initFileName         = null;

	private String  gisTableBaseName    = null;
	private String  templateXFile       = null;
	private String  yieldOutputBaseName = null;
	private boolean allFlag             = false;

	private String pathToDSSATDirectory     = null;
	private File   pathToDSSATDirectoryAsFile = null;
	private String nameOfDSSATExecutable    = null;
	private double SWmultiplier             = Double.NaN;
	private int    firstRandomSeed          = -1;
	private int    nRandomSeedsToUse        = -2;
	private String magicSoilPrefix          = null;
	private int    fakePlantingYear         = -3;
	private int    spinUpTimeDays           = -4;
	private int    nPlantingWindowsPerMonth = -5;
	private int    plantingWindowLengthDays = -6;
	private long   sleeptimeWaitForFileMillis = 21;
	private int    maxParsingTries            = 5;
	private int    co2ppm    = -5;
	private String cropFertilizerSchemeToUse = null;
	private int    nHappyPlantRunsForPhenology = -3;
	private int    happyYieldThresholdToDoRealRuns = 0;
	private int    phenologyBufferInDays = 0;
	private int    happyMaturityThresholdToDoRealRuns = 0;
	
	
	private String magicWeatherStationNameToUsePath = null;
	private String magicDSSATSummaryToReadPath      = null;
	private File   summaryDSSATOutputAsFileObject   = null;
	private File   errorAsFileObject   = null;
	private String magicInitializationFilePath      = null;
	
//	private int cropToUseInt = -3;
	
	private NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;

	private String[] dssatExecutionCommand = new String[3];

	///////////////////////////////////////////////////////////////
	// special variables for the purpose of grabbing the results //
	///////////////////////////////////////////////////////////////
	// initialize the place to store the yields...

	private DescriptiveStatisticsUtility realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility realMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
	private DescriptiveStatisticsUtility happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

	private DescriptiveStatisticsUtility[] extraSummaryAccumulators = null;

//	private long totalYield = -5;
//	private long totalYieldSquared = -6;
//	private int maxYield = Integer.MIN_VALUE;
//	private int minYield = Integer.MAX_VALUE;

//	private int[][] thisPixelYields = null;
//	private int[][] thisPixelMaturities = null;
	
	private int nTimesSuccessfullyReadFirstTime = 0;
	private int nTimesSuccessfullyReadFirstTimeHappy = 0;
	
	private long initialSleepTimeToUse = -1;
	private long initialSleepTimeToUseHappy = 0;

	private DescriptiveStatisticsUtility readingTimesReal = null;
	private DescriptiveStatisticsUtility readingTimesHappy = null;
	
	private boolean badThingsHappened = false;
//	private int badThingsHappenedIndex = -1;
//	private int badPlantingWindow = 9;
	
	

	public DSSAT45Runner() {
		
		readyToRun = false;
	}


	public static void generateDemoInitFile(String filename) throws FileNotFoundException {

		String initFileContents = "";

		initFileContents += "gisTableBaseName" + "\n";
		initFileContents += "templateXFile" + "\n";
		initFileContents += "yieldOutputBaseName" + "\n";
		initFileContents += "allFlag" + "\n";
		initFileContents += "pathToDSSATDirectory" + "\n";
		initFileContents += "nameOfDSSATExecutable" + "\n";
		initFileContents += "SWmultiplier" + "\n";
		initFileContents += "firstRandomSeed" + "\n";
		initFileContents += "nRandomSeedsToUse" + "\n";
		initFileContents += "magicSoilPrefix" + "\n";
		initFileContents += "fakePlantingYear" + "\n";
		initFileContents += "spinUpTimeDays" + "\n";
		initFileContents += "nPlantingWindowsPerMonth" + "\n";
		initFileContents += "plantingWindowLengthDays" + "\n";
		initFileContents += "sleeptimeWaitForFileMillis" + "\n";
//		initFileContents += "sleeptimeExtraMillis" + "\n";
		initFileContents += "maxParsingTries" + "\n";
		
		initFileContents += "co2ppm" + "\n";
		initFileContents += "cropToUse" + "\n";
		initFileContents += "nHappyPlantRunsForPhenology" + "\n";
		initFileContents += "happyYieldThresholdToDoRealRuns" + "\n";
		initFileContents += "phenologyBufferInDays (positive means days AFTER happy plant phenology)" + "\n";
		initFileContents += "happyMaturityThresholdToDoRealRuns (days between planting and maturity; anything above this will be skipped)" + "\n";
		FunTricks.writeStringToFile(initFileContents, filename);

	}

	public static String usageMessage() {

		String usageMessage = 
			"Usage: class GIS_table_base_name template_X_file yield_output_base_name ALL_flag settings_csv  sleep_time_for_file\n" +
			"\n" +
			"This program will try to do one-stop, full-service running of DSSAT based on a table of climate/etc inputs.\n" +
			"Everything is brute force and magical.\n" +
			"\n" +
			"The delimiter in the GIS_table_base_name files should be tab.\n" +
			"\n" +
			"The columns of the data table should be: soil type / elevation (m) / planting month / monthly SW / monthly tmax (C) / monthly tmin (C) / prec (mm) / rainy days (#)\n" +
			"\n" +
			"The geography file should have four columns: row / col / latitude / longitude\n" +
			"\n" +
			"The template_X_file should have the following string in it for search-and-replace purposes:\n" +
			"Of course, these should be verified with the source code...\n" +
			"ssssssssss\tfor replacement by the soil type code (ID_SOIL)\n" +
			"iiiiS\tstart date for the initialization (ICDAT, SDATE)\n" +
			"ppppS\tstart date for the planting window (PDATE, PFRST)\n" +
			"ppppE\tend date for the planting window (PLAST)\n" +
			"rrrrr\trandom seed for weather generator (RSEED)\n" +
			"wwww\tweather file name (WSTA)\n" +
			"\n" +
			"The ALL_flag should be true/false indicating whether to report all simulated yields. Regardless," +
			"the summary for each pixel will be reported.\n" +
			"\n" +
			"The settings_csv should contain all the fancy controls in a particular order as specified below:\n" +
			"* full path to DSSAT directory with trailing slash\n" +
			"* DSSAT executable name\n" +
			"* shortwave multiplier\n" +
			"    SW_multiplier is the value to multiply the short-wave radiation numbers by in order to get the proper units. the final\n" +
			"    units should be MJ/m^2/day. For example, if the raw data are in W/m^2, the multiplier is 3600 * 24 / 10^6 = 0.0864 .\n" +
			"\n" +
			"* first random seed\n" +
			"* # of seeds to use\n" +
			"* magic soil prefix\n" +
			"* fake planting year (2 digits)\n" +
			"* spin up time (days)\n" +
			"* # of planting windows to try (evenly spaced based on a 30-day month; leap years ignored)\n" +
			"* planting window length (days)\n" +
			"\n" +
			"\n" +
			"\n" +
			"The yield_output_base_name indicates where to write out the output.\n" +
			"The following are intended:\n" +
			"yield_output_base_name + _provenance.txt\tlist of all the arguments (both usable and human readable), copy of template X_file used\n" + 
			"yield_output_base_name + _ALL.txt (& .info.txt)\tall the goodies for each pixel\n" +
			"yield_output_base_name + _ALL.codes.txt\t\thuman readable names for each elements (specifically, day for the start of planting window and the random seed used)\n" +
			"yield_output_base_name + _STATS.txt (& .info.txt)\tmin/max/mean/std for each pixel over all realizations and planting dates within the month\n" +
			"\n" +
			"This will create an initialization file called something like deleteme.dv4 along with a re-used climate file like RICK.CLI and.\n" +
			"an X-file called deleteme.meX\n" +
			"\n" +
			"The sleep_time_for_file determines how long to wait for the file before trying to read it. The units are MILLISECONDS..." +
			"\n" +
			"WARNING!!! Nothing is idiot proofed!\n" +
			"\n";

		return usageMessage;
	}

	public String getInitFile() {
		return this.initFileName;
	}

	public void setInitFile(String filename) {
		this.initFileName = filename;
	}

	public String getTemplateXFile() {
		return templateXFile;
	}

	public void setTemplateXFile(String templateXFile) {
		this.templateXFile = templateXFile;
	}

	public int getMagicDSSATSummaryLineIndexToRead() {
		return magicDSSATSummaryLineIndexToRead;
	}

	public void setMagicDSSATSummaryLineIndexToRead(
			int magicDSSATSummaryLineIndexToRead) {
		this.magicDSSATSummaryLineIndexToRead = magicDSSATSummaryLineIndexToRead;
	}

	public int getMagicHarvestedWeightAtHarvestStartIndex() {
		return magicHarvestedWeightAtHarvestStartIndex;
	}

	public void setMagicHarvestedWeightAtHarvestStartIndex(
			int magicHarvestedWeightAtHarvestStartIndex) {
		this.magicHarvestedWeightAtHarvestStartIndex = magicHarvestedWeightAtHarvestStartIndex;
	}

	public int getMagicHarvestedWeightAtHarvestEndIndex() {
		return magicHarvestedWeightAtHarvestEndIndex;
	}

	public void setMagicHarvestedWeightAtHarvestEndIndex(
			int magicHarvestedWeightAtHarvestEndIndex) {
		this.magicHarvestedWeightAtHarvestEndIndex = magicHarvestedWeightAtHarvestEndIndex;
	}

	public long getSleeptimeWaitForFileMillis() {
		return sleeptimeWaitForFileMillis;
	}

	public void setSleeptimeWaitForFileMillis(long sleeptimeWaitForFileMillis) {
		this.sleeptimeWaitForFileMillis = sleeptimeWaitForFileMillis;
	}

	public int getMaxParsingTries() {
		return maxParsingTries;
	}

	public void setMaxParsingTries(int maxParsingTries) {
		this.maxParsingTries = maxParsingTries;
	}


	public void readInitFile(String filename) throws IOException, Exception {

		String[] initFileContents = FunTricks.readTextFileToArray(filename);

//		for (int index = 0; index < initFileContents.length; index++) {
//		System.out.println(index + " -> [" + initFileContents[index] + "]");
//		}


		int storageIndex = 0;
		gisTableBaseName         = initFileContents[storageIndex++];
		templateXFile            = initFileContents[storageIndex++];
		yieldOutputBaseName      = initFileContents[storageIndex++];
		allFlag                  = Boolean.parseBoolean(
				initFileContents[storageIndex++]);
		pathToDSSATDirectory     = initFileContents[storageIndex++];
		nameOfDSSATExecutable    = initFileContents[storageIndex++];
		SWmultiplier             = Double.parseDouble(
				initFileContents[storageIndex++]);
		firstRandomSeed          = Integer.parseInt(
				initFileContents[storageIndex++]);
		nRandomSeedsToUse        = Integer.parseInt(
				initFileContents[storageIndex++]);
		magicSoilPrefix          = initFileContents[storageIndex++];
		fakePlantingYear         = Integer.parseInt(
				initFileContents[storageIndex++]);
		spinUpTimeDays           = Integer.parseInt(
				initFileContents[storageIndex++]);
		nPlantingWindowsPerMonth = Integer.parseInt(
				initFileContents[storageIndex++]);
		plantingWindowLengthDays = Integer.parseInt(
				initFileContents[storageIndex++]);

		sleeptimeWaitForFileMillis = Long.parseLong(
				initFileContents[storageIndex++]);
		maxParsingTries            = Integer.parseInt(
				initFileContents[storageIndex++]);
		
		co2ppm                     = Integer.parseInt(
				initFileContents[storageIndex++]);

		cropFertilizerSchemeToUse       = initFileContents[storageIndex++];
		
		nHappyPlantRunsForPhenology     = Integer.parseInt(
				initFileContents[storageIndex++]);
		happyYieldThresholdToDoRealRuns = Integer.parseInt(
				initFileContents[storageIndex++]);
		phenologyBufferInDays = Integer.parseInt(
				initFileContents[storageIndex++]);
		happyMaturityThresholdToDoRealRuns = Integer.parseInt(
				initFileContents[storageIndex++]);
		
//		now prepare the dependent magic numbers...
		pathToDSSATDirectoryAsFile = new File(pathToDSSATDirectory);

		dssatExecutionCommand[0] = pathToDSSATDirectory + nameOfDSSATExecutable;
		dssatExecutionCommand[1] = "b";
		dssatExecutionCommand[2] = magicInitializationFile;

		magicWeatherStationNameToUsePath = pathToDSSATDirectory + magicWeatherStationNameToUse + ".CLI";
		magicDSSATSummaryToReadPath      = pathToDSSATDirectory + magicDSSATSummaryToRead;
		summaryDSSATOutputAsFileObject   = new File(magicDSSATSummaryToReadPath);
		errorAsFileObject                = new File(pathToDSSATDirectory + magicErrorFile);
		magicInitializationFilePath      = pathToDSSATDirectory + magicInitializationFile;

		// figure out the crop number to use so we can use switches instead of if/then...
		// i'm sure there's a better way, but i'm lame, so i'm gonna brute force it...
				
		if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.maizeString)) {
//			cropToUseInt = this.maizeInt;
			
			nitrogenFertilizerScheme = new FSMaize();

		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.riceString)) {
//			cropToUseInt = this.riceInt;

			nitrogenFertilizerScheme = new FSRice();

		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.wheatString)) {
//			cropToUseInt = this.wheatInt;

			nitrogenFertilizerScheme = new FSWheat();
		
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.soybeansString)) {
//			cropToUseInt = this.soybeansInt;

			nitrogenFertilizerScheme = new FSLegume();
		
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.groundnutsString)) {
//			cropToUseInt = this.groundnutsInt;

			nitrogenFertilizerScheme = new FSLegume();
		
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.cottonString)) {
//			cropToUseInt = this.cottonInt;
			System.out.println("Need to define the fertilizer scheme for " + cropFertilizerSchemeToUse);
			throw new Exception();

//			nitrogenFertilizerScheme = new FSMaize();
			
			
		} else {
			System.out.println("crop string [" + cropFertilizerSchemeToUse + "]" + "not in our list of supported crops; or at least, not implemented");
			throw new Exception();
		}


		extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraStartIndices.length];
		for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
			extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(false);
		}

//		thisPixelYields = new int[nPlantingWindowsPerMonth][nRandomSeedsToUse];
//		thisPixelMaturities = new int[nPlantingWindowsPerMonth][nRandomSeedsToUse];
		
		
//		mark that we're good to go...
		readyToRun = true;

	}

	private void writeProvenance() throws IOException, FileNotFoundException, Exception {
		String filename = yieldOutputBaseName  + "_provenance.txt";
		writeProvenance(filename);
	}

	private void writeProvenance(String filename) throws IOException, FileNotFoundException, Exception {


		File provenanceFileObject = new File(filename);
//		File provenanceFileObject = new File(yieldOutputBaseName  + "_provenance.txt");
//		outputStream = new FileWriter(weatherStationFileObject);
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

		provenanceOut.print("gisTableBaseName:\t"      + gisTableBaseName + "\n");
		provenanceOut.print("templateXFile:\t\t"       + templateXFile + "\n");
		provenanceOut.print("yieldOutputBaseName:\t"   + yieldOutputBaseName + "\n");
		provenanceOut.print("allFlag:\t\t"             + allFlag + "\n");

		provenanceOut.print("\n");

		provenanceOut.print("pathToDSSATDirectory:\t\t"     + pathToDSSATDirectory + "\n");
		provenanceOut.print("nameOfDSSATExecutable:\t\t"    + nameOfDSSATExecutable + "\n");
		provenanceOut.print("SWmultiplier:\t\t\t"           + SWmultiplier + "\n");
		provenanceOut.print("firstRandomSeed:\t\t"          + firstRandomSeed + "\n");
		provenanceOut.print("nRandomSeedsToUse:\t\t"        + nRandomSeedsToUse + "\n");
		provenanceOut.print("magicSoilPrefix:\t\t"          + magicSoilPrefix + "\n");
		provenanceOut.print("fakePlantingYear:\t\t"         + fakePlantingYear + "\n");
		provenanceOut.print("spinUpTimeDays:\t\t\t"         + spinUpTimeDays + "\n");
		provenanceOut.print("nPlantingWindowsPerMonth:\t"   + nPlantingWindowsPerMonth + "\n");
		provenanceOut.print("plantingWindowLengthDays:\t"   + plantingWindowLengthDays + "\n");
		provenanceOut.print("sleeptimeWaitForFileMillis:\t" + sleeptimeWaitForFileMillis + "\n");
		provenanceOut.print("maxParsingTries:\t\t"          + maxParsingTries + "\n");
		provenanceOut.print("co2ppm:\t\t\t\t"               + co2ppm + "\n");

		provenanceOut.print("cropToUse:\t\t\t"               + cropFertilizerSchemeToUse + "\n");
		provenanceOut.print("nHappyPlantRunsForPhenology:\t" + nHappyPlantRunsForPhenology + "\n");
		provenanceOut.print("happyYieldThresholdToDoRealRuns:\t" + happyYieldThresholdToDoRealRuns + "\n");
		provenanceOut.print("phenologyBufferInDays:\t"       + phenologyBufferInDays + "\n");
		provenanceOut.print("happyMaturityThresholdToDoRealRuns:\t" + happyMaturityThresholdToDoRealRuns + "\n");


		provenanceOut.print("\n");

		provenanceOut.print("--- Placeholder dictionary ---" + "\n");
		provenanceOut.print("soilPlaceholder =\t\t\t"            + soilPlaceholder + "\n");
		provenanceOut.print("initializationStartPlaceholder =\t" + initializationStartPlaceholder + "\n");
		provenanceOut.print("plantingDateStartPlaceholder =\t\t" + plantingDateStartPlaceholder + "\n");
		provenanceOut.print("plantingDateEndPlaceholder =\t\t"   + plantingDateEndPlaceholder + "\n");
		provenanceOut.print("randomSeedPlaceholder =\t\t\t"      + randomSeedPlaceholder + "\n");
		provenanceOut.print("weatherPlaceholder =\t\t\t"         + weatherPlaceholder + "\n");
		provenanceOut.print("co2ppm =\t\t\t\t"                   + co2ppm + "\n");
		provenanceOut.print("fertilizerPlaceholder =\t\t\t"      + fertilizerPlaceholder + "\n");
		
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
		provenanceOut.flush();
		provenanceOut.close();


	}

	public String noWaterStressX(String templateXFile) throws Exception {


		// the idea here is that we need to run through the templateXFile and turn off the
		// switches for water stress modeling...
		

		String optionToSetAsNo = "WATER";
		
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
			System.out.println("template X did not have an \"OPTIONS\" line");
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
		
		
		System.out.println("nColumns = " + nColumns);
		
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
				if (tempName.trim().equalsIgnoreCase(optionToSetAsNo)) {
					// replace any semblance of yes with no
					newValuesLine += tempValue.replaceAll("Y", "N").replaceAll("y", "N");
				}
				
			} // end if it is actually a switch
			
		} // end of columns
		
		// now we need to replace the lines...
		templateAsArray[lineWeWant]     = newNamesLine;
		templateAsArray[lineWeWant + 1] = newValuesLine;

		// ok, and now we need to run everything together into a single string with newlines again...
		
		String happyXTemplate = "";
		
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			happyXTemplate += templateAsArray[lineIndex] + "\n";
		}
		
		
		
		return happyXTemplate;
	
	}
	
	private String happyPlantX(String templateXFile) throws Exception {

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
			System.out.println("template X did not have an \"OPTIONS\" line");
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
		
		
		System.out.println("nColumns = " + nColumns);
		
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

		// ok, and now we need to run everything together into a single string with newlines again...
		
		String happyXTemplate = "";
		
		for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
			happyXTemplate += templateAsArray[lineIndex] + "\n";
		}
		
		
		
		return happyXTemplate;
	}

	
	private int[] grabNewHappyResults(int nYears) throws InterruptedException, Exception {
		
		// magic numbers
		final int timeCheckNumber = 2;
		final long retrySleepTimeToUse = 2;
		final double multiplierForTimeUpperBound = 1.0 + 0.50;
		
		// declarations
		TimerUtility readingTimer = new TimerUtility();
		int plantingDate = -1;
		int eventDate = -2;
		int daysSincePlantingForEvent = -3;

		int yieldToUse = -3;
		int anthesisToUse = -3;
		int maturityToUse = -3;
		boolean everythingIsValid = true;

		
		// declarations with initializations
		boolean readSuccessfully = false;
		int nTries = 0;
		int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
		String[] candidateSummaryContents = new String[nLinesToRead];
		
		readingTimer.tic();
		while (!readSuccessfully) {

			// determine whether we want to increase the initial wait time:
				// let's bump it up or down...
			if (nTimesSuccessfullyReadFirstTimeHappy == 0 && nTries == 1) {
				initialSleepTimeToUseHappy = Math.min(initialSleepTimeToUseHappy + 1, (long)(readingTimesHappy.getMinAsLong() * multiplierForTimeUpperBound));
			} else if (nTimesSuccessfullyReadFirstTimeHappy > 0 && nTimesSuccessfullyReadFirstTimeHappy % timeCheckNumber == 0 && nTries == 0) {
				initialSleepTimeToUseHappy--;
			}

			// do the sleeping...
			if (nTries == 0) {
				Thread.sleep(initialSleepTimeToUseHappy);
			} else {
				Thread.sleep(retrySleepTimeToUse);
			}

			nTries++;

			// attempt to read
			try {
				candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
			} catch (FileNotFoundException fnfe) {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("HAPPY: file not found... try #" + nTries + " [" + errorAsFileObject + "] exists...");
					throw fnfe;
				}
//				System.out.println("HAPPY: file not found... try #" + nTries + " (no error file)");
				
//				candidateSummaryContents = new String[] {}; // make it an empty array so the checking below will poop out
			} catch (IOException ioe) {
				System.out.println("HAPPY: i/o exception...  try #" + nTries);
//				candidateSummaryContents = new String[] {}; // make it an empty array so the checking below will poop out
				throw ioe;
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				System.out.println("HAPPY: array index exception...  try #" + nTries);
				// carry on, since the file is still in the process of being written...
			}

			// check if the file has the right number of lines. if so, check that the last line is completely there...
//			System.out.println("HAPPY: try #" + nTries + " length = " + candidateSummaryContents.length);
			if (
					candidateSummaryContents[nLinesToRead - 1] != null
					&&
					candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength
				 ) {
				readSuccessfully = true;
				nTimesSuccessfullyReadFirstTimeHappy++;
			} else {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("HAPPY: partial read... try #" + nTries + " [" + errorAsFileObject + "] exists...");
					throw new Exception();
				}
				nTimesSuccessfullyReadFirstTimeHappy = 0;
			}
		}
		// check the time and update the timekeeping thingee
		readingTimesHappy.useDoubleValue(readingTimer.tocMillis());
		
		// parse the output file for the necessary goodies...
		DescriptiveStatisticsUtility happyYields        = new DescriptiveStatisticsUtility(false);
		DescriptiveStatisticsUtility happyAnthesisDates = new DescriptiveStatisticsUtility(false);
		DescriptiveStatisticsUtility happyMaturityDates = new DescriptiveStatisticsUtility(false);

		for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
			if (candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] == null) {
				System.out.println("H: funny business");
				for (int fYI = 0; fYI < nYears; fYI++) {
					System.out.println(fYI + " -> [" + candidateSummaryContents[fYI + magicDSSATSummaryLineIndexToRead] + "]");
				}
				throw new NullPointerException();
			}
			everythingIsValid = true;
			try {
				// yield
				yieldToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
				     					                                          .substring(magicHarvestedWeightAtHarvestStartIndex,
				    					                                          		magicHarvestedWeightAtHarvestEndIndex).trim());
				// planting / anthesis / maturity dates
				plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
				                                                         .substring(magicPlantingDateStartIndex,
				                                                        		 magicPlantingDateEndIndex).trim());
				anthesisToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
						                                                      .substring(magicAnthesisDateStartIndex,
						                                                      		magicAnthesisDateEndIndex).trim());;
				maturityToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
						                                                      .substring(magicMaturityDateStartIndex,
						                                                      		magicMaturityDateEndIndex).trim());

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("HAPPY: had trouble reading one of the following as an integer:");
				System.out.println("yield [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                      .substring(magicHarvestedWeightAtHarvestStartIndex,
                                     		magicHarvestedWeightAtHarvestEndIndex).trim()
						+ "]");
				System.out.println("planting  [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                     .substring(magicPlantingDateStartIndex,
                                    		 magicPlantingDateEndIndex).trim()
						+ "]");
				System.out.println("anthesis  [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                     .substring(magicAnthesisDateStartIndex,
                                     		magicAnthesisDateEndIndex).trim()
            + "]");
				System.out.println("maturity  [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                     .substring(magicMaturityDateStartIndex,
                                     		magicMaturityDateEndIndex).trim()
            + "]");
				everythingIsValid = false;
			}

			if (everythingIsValid) {
			// yield
			happyYields.useLongValue(yieldToUse);
			
			// planting date
			plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
			                                                         .substring(magicPlantingDateStartIndex,
			                                                        		 magicPlantingDateEndIndex).trim());
			
			// anthesis
			eventDate = anthesisToUse;
			daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
			happyAnthesisDates.useLongValue(daysSincePlantingForEvent);

			// pull out the bits we want
			// maturity
			eventDate = maturityToUse;
			daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
			happyMaturityDates.useLongValue(daysSincePlantingForEvent);
			}
		}

		// since everything was successful, clear out the summary file
		if (summaryDSSATOutputAsFileObject.exists()) {
			this.summaryDSSATOutputAsFileObject.delete();
		}
		
		return new int[] {(int)Math.floor(happyYields.getMean()),
				(int)Math.floor(happyAnthesisDates.getMean()),
				(int)Math.floor(happyMaturityDates.getMean())};
	}
	
	private void grabNewManyResults(int nYears) throws InterruptedException, Exception {

		
		// magic numbers
		final int timeCheckNumber = 2;
		final long retrySleepTimeToUse = 2;
		final double multiplierForTimeUpperBound = 1.0 + 0.25;
		
		
		
		// declarations
		TimerUtility readingTimer = new TimerUtility();
		int plantingDate = -1;
//		int eventDate = -2;
		int daysSincePlantingForEvent = -3;
//		int valueExtracted = -4;
		
		int yieldToUse = -4;
//		int anthesisToUse = -3;
		int maturityToUse = -3;
		int[] extractedValues = new int[extraStartIndices.length];
		boolean everythingIsValid = true;
		
		
		// declarations with initializations
		boolean readSuccessfully = false;
		int nTries = 0;
		int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
		String[] candidateSummaryContents = new String[nLinesToRead];

		
		readingTimer.tic();
		while (!readSuccessfully) {

			// determine whether we want to increase the initial wait time:
				// let's bump it up or down...
			if (nTimesSuccessfullyReadFirstTime == 0 && nTries == 1) {
				initialSleepTimeToUse = Math.min(initialSleepTimeToUse + 1, (long)(readingTimesReal.getMinAsLong() * multiplierForTimeUpperBound));
			} else if (nTimesSuccessfullyReadFirstTime > 0 && nTimesSuccessfullyReadFirstTime % timeCheckNumber == 0 && nTries == 0) {
				initialSleepTimeToUse--;
			}

			// do the sleeping...
			if (nTries == 0) {
				Thread.sleep(initialSleepTimeToUse);
			} else {
				Thread.sleep(retrySleepTimeToUse);
			}

			nTries++;

			// attempt to read
			try {
				candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath,nLinesToRead);
			} catch (FileNotFoundException fnfe) {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("REAL: file not found... try #" + nTries + " [" + errorAsFileObject + "] exists...");
					throw fnfe;
				}
				System.out.println("REAL: file not found... try #" + nTries + " (no error file)");
				
//				candidateSummaryContents = new String[] {}; // make it an empty array so the checking below will poop out
			} catch (IOException ioe) {
				System.out.println("REAL: i/o exception...  try #" + nTries);
//				candidateSummaryContents = new String[] {}; // make it an empty array so the checking below will poop out
				throw ioe;
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				System.out.println("REAL: array index exception...  try #" + nTries);
				// carry on, since the file is still in the process of being written...
			}

			// check if the file has the right number of lines. if so, check that the last line is completely there...
//			System.out.println("REAL: try #" + nTries + " length = " + candidateSummaryContents.length);
			if (
					candidateSummaryContents[nLinesToRead - 1] != null
					&&
					candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength
				 ) {
				readSuccessfully = true;
				nTimesSuccessfullyReadFirstTime++;
			} else {
				// check for error file
				if (errorAsFileObject.exists()) {
					System.out.println("REAL: partial read... try #" + nTries + " [" + errorAsFileObject + "] exists...");
					throw new Exception();
				}
				nTimesSuccessfullyReadFirstTime = 0;
			}
		}
		// check the time and update the timekeeping thingee
		readingTimesReal.useDoubleValue(readingTimer.tocMillis());
		
		// parse the output file for the necessary goodies...
		for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {

			
			everythingIsValid = true;
			
			try {
				// yield
				yieldToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
				     					                                          .substring(magicHarvestedWeightAtHarvestStartIndex,
				    					                                          		magicHarvestedWeightAtHarvestEndIndex).trim());
				// planting / anthesis / maturity dates
				plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
				                                                         .substring(magicPlantingDateStartIndex,
				                                                        		 magicPlantingDateEndIndex).trim());
//				anthesisToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
//						                                                      .substring(magicAnthesisDateStartIndex,
//						                                                      		magicAnthesisDateEndIndex).trim());;
				maturityToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
						                                                      .substring(magicMaturityDateStartIndex,
						                                                      		magicMaturityDateEndIndex).trim());
				
				// all the other goodies...
				for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
					extractedValues[extraIndex] = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					                                                      .substring(extraStartIndices[extraIndex],
					                                                      		extraEndIndices[extraIndex]).trim());
				}

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println("REAL: had trouble reading one of the following as an integer:");
				System.out.println("yield [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                      .substring(magicHarvestedWeightAtHarvestStartIndex,
                                     		magicHarvestedWeightAtHarvestEndIndex).trim()
						+ "]");
				System.out.println("planting  [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                     .substring(magicPlantingDateStartIndex,
                                    		 magicPlantingDateEndIndex).trim()
						+ "]");
//				System.out.println("anthesis  [" + 
//						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
//                                     .substring(magicAnthesisDateStartIndex,
//                                     		magicAnthesisDateEndIndex).trim()
//            + "]");
				System.out.println("maturity  [" + 
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                                     .substring(magicMaturityDateStartIndex,
                                     		magicMaturityDateEndIndex).trim()
            + "]");
				
				// all the other goodies...
				for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
					
					System.out.println("extras [" + extraIndex + "] = [" +
					candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					                                                      .substring(extraStartIndices[extraIndex],
					                                                      		extraEndIndices[extraIndex]).trim()
					                                                      		+ "]");
				}

				everythingIsValid = false;
	
			}
			
			
			if (everythingIsValid) {

				// yield
				realYieldsEntirePixel.useLongValue(yieldToUse);
				
				// maturity
				daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, maturityToUse);
				realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);

				// all the extra bits
				for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
					extraSummaryAccumulators[extraIndex].useLongValue(extractedValues[extraIndex]);
				}
				
				
			}
			
		}

		// since this appears to have worked, clear out the summary file
		if (summaryDSSATOutputAsFileObject.exists()) {
			this.summaryDSSATOutputAsFileObject.delete();
		}
	
	}
	
	


	public void doSimulationsMultipleYears() throws Exception {

		//////////////////////////////////////////////////
		// check if everything has been set up properly //
		//////////////////////////////////////////////////
		if (!this.readyToRun) {
			System.out.println("DSSATRunner not ready to run... exiting...");
			return;
		}

		// set up a timer for the fun of it...
		TimerUtility thisTimer = new TimerUtility();
		TimerUtility dssatTimer = new TimerUtility();
		
		realYieldsEntirePixel    = new DescriptiveStatisticsUtility(false);
		happyYieldsEntirePixel   = new DescriptiveStatisticsUtility(false);
		realMaturityEntirePixel  = new DescriptiveStatisticsUtility(false);
		happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

		readingTimesReal  = new DescriptiveStatisticsUtility(true);
		readingTimesHappy = new DescriptiveStatisticsUtility(true);
		
		long totalDSSATNanos = 0;
		long totalReadNanos = 0;
		long totalWriteNanos = 0;

		///////////////////////////////////
		// write out the provenance file //
		///////////////////////////////////
		System.out.println("-- starting to write provenance file --");
		this.writeProvenance();
		System.out.println("== done writing provenance file ==");

		///////////////////////////////////////
		// read in the whole template X file //
		///////////////////////////////////////
		String templateXFileContents = FunTricks.readTextFileToString(templateXFile);

		System.out.println("== done reading in template X file ==");

		///////////////////////////
		// variable declarations //
		///////////////////////////

		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		double nitrogenLevel = Double.NaN;
		int soilType = -1, firstPlantingDay = -4;
		int startingDayToPlantForThisWindow = -1;
		int endingDayToPlantForThisWindow = -99;
		int fakePlantingYearEnd = -101;
		int initializationDayForThisWindow = -4;
		int fakeInitializationYear = -5;

//		double meanYield = Double.NaN;
//		double stdYield = Double.NaN; 
		
		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;
		String fertilizerBlock  = null;

		String randomSeedCode = null;
		String nYearsCode = null;
		String nHappyYearsCode = null;
		String co2ppmCode = null;

//		double badStat = Double.NaN;

		String soilTypeString = null;

		int[] phenologyInDays = null;

		String statisticsOutLine = null;
		
		
		////////////////////////////
		// a couple magic numbers //
		////////////////////////////
		
//		double magicOkBadStat = -1.0;
//		double magicBadYieldAll = -2.0;
		

		
		////////////////////////////////////////
		// set up stuff that we actually know //
		////////////////////////////////////////
		initialSleepTimeToUse = this.sleeptimeWaitForFileMillis;
		initialSleepTimeToUseHappy = this.sleeptimeWaitForFileMillis;
		
		String fullTempXFileName = pathToDSSATDirectory + tempXFileName;
		File fullTempXFile = new File(fullTempXFileName);

		File weatherStationFile = new File(magicWeatherStationNameToUsePath);
		
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
		// Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast enough
		// that it doesn't matter. for big stuff, we'll want disk...
		int formatIndexToForce = 1;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);

		if (geogMatrix.getDimensions()[1] != 4) {
			System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
			throw new Exception();
		}
		
		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];


		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		String XHappyStuffToWrite = null;
		
		// since this implementation (using the multiple years with a single random seed, rather
		// than multiple random seeds with a single year) has the seeds and years as invariants, do
		// them up front to save on a little search and replace overhead...
		randomSeedCode  = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
		nYearsCode      = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
		nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
		co2ppmCode      = DSSATHelperMethods.padWithZeros(co2ppm, 4);

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
		
		
		
		String XInvariantsReplaced = templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);

		// start the timer for just DSSAT proper
		thisTimer.tic();
		dssatTimer.tic();
		
		// clear out the summary and error files if they exist...
		if (errorAsFileObject.exists()) {
			errorAsFileObject.delete();
		}
		if (summaryDSSATOutputAsFileObject.exists()) {
			summaryDSSATOutputAsFileObject.delete();
		}
		
		nTimesSuccessfullyReadFirstTime = 0;
		
		System.out.println("-- starting through data --");
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

			// initialize the descriptive statistics utility and storage array
			   realYieldsEntirePixel.reset();
			  happyYieldsEntirePixel.reset();
 			 realMaturityEntirePixel.reset();
			happyMaturityEntirePixel.reset();
			
			for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
				extraSummaryAccumulators[extraIndex].reset();
			}


			// figure out the climate file...
			cliStuffToWrite = DSSATHelperMethods.cliFileContentsMFM(dataMatrix, geogMatrix, lineIndex, SWmultiplier);
			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, lineIndex);

			dssatTimer.tic();
			FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
			totalWriteNanos += dssatTimer.TOCNanos();
			
			soilType         = nonClimateInfo[0];
			// elevation        = nonClimateInfo[1]; // don't need this here...
			// plantingMonth    = nonClimateInfo[2]; // don't need this here...
			firstPlantingDay = nonClimateInfo[3];
			nitrogenLevel = nonClimateInfo[4];

			// brute force padding
			// Beware the MAGIC ASSUMPTION!!! assuming two digit soil codes
			if (soilType < 10 && soilType > 0) {
				soilTypeString = magicSoilPrefix + 0 + soilType;
			} else if (soilType < 100 && soilType > 0) {
				soilTypeString = magicSoilPrefix  + soilType;
			} else {
				System.out.println("soil type number did not meet our criteria: > 0 and < 100: " + soilType);
				throw new Exception();
			}

			// loop over the planting windows and random seeds
			for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
				// pick the starting day/etc for this window
				startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
				initializationDayForThisWindow = startingDayToPlantForThisWindow - spinUpTimeDays;
				fakeInitializationYear = fakePlantingYear;

				// take care of the possibility that we will go before the beginning of this year
				while (initializationDayForThisWindow < 1) {
					initializationDayForThisWindow += nDaysInYear;
					fakeInitializationYear--;
				}

				endingDayToPlantForThisWindow = startingDayToPlantForThisWindow + plantingWindowLengthDays;
				fakePlantingYearEnd = fakePlantingYear;

				// take care of the possibility that we will go beyond the end of this year
				while (endingDayToPlantForThisWindow > nDaysInYear) {
					endingDayToPlantForThisWindow -= nDaysInYear;
					fakePlantingYearEnd++;
				}

				// format everything properly....
				startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
						+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
				endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
						+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
				initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
						+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd

				//////////////////////////////////////
				// let's run a happy plant first... //
				//////////////////////////////////////
				
				// X file
				// do the search and replace thing; the invariants have already been done above...
				XHappyStuffToWrite = XHappyInvariantsReplaced.replaceAll(soilPlaceholder               , soilTypeString);
				XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
				XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
				XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
				
				FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);

				// run DSSAT with the happy plant

				Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

				//////////////////////////////////////////
				// extract the results & store to array //
				//////////////////////////////////////////

				// mean yield / anthesis / maturity in days after planting...
//				phenologyInDays = this.grabHappyResults(nHappyPlantRunsForPhenology);
				phenologyInDays = this.grabNewHappyResults(nHappyPlantRunsForPhenology);


				// keep track of the happy plant yields for the fun of it...
 				  happyYieldsEntirePixel.useLongValue(phenologyInDays[0]);
				happyMaturityEntirePixel.useLongValue(phenologyInDays[2]);
				
				
				// proceed, if the happy yield exceeds some theshold and the effective growing season isn't too long....
				if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {


					/////////////////////
					// make the X file //
					/////////////////////

					// create the fertilizer block...
					fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
							phenologyInDays[1] + phenologyBufferInDays, phenologyInDays[2] + phenologyBufferInDays, nitrogenLevel
					);

					// do the search and replace thing; the invariants have already been done above...
					XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
					XStuffToWrite =       XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
					XStuffToWrite =       XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);

					// overwrite the old file with the new contents

					dssatTimer.tic();
					FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
					totalWriteNanos += dssatTimer.TOCNanos();

					// recommend garbage collection
					System.gc();

					///////////////
					// run DSSAT //
					///////////////

					Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
					totalDSSATNanos += dssatTimer.TOCNanos();

					//////////////////////////////////////////
					// extract the results & store to array //
					//////////////////////////////////////////

//					this.grabRealResults(lineIndex, plantingWindowIndex);
//					this.grabNewRealResults(this.nRandomSeedsToUse);
					this.grabNewManyResults(this.nRandomSeedsToUse);

					totalReadNanos += dssatTimer.TOCNanos();

					// bail out if we have already encountered bad things for this pixel...
					if (badThingsHappened) {
						break;
					}

				} // end if happy yield threshold is met
				else {
					// we need to set these things to zero artificially...
					for (int yearIndex = 0; yearIndex < nRandomSeedsToUse; yearIndex++) {
//						thisPixelYields[plantingWindowIndex][yearIndex] = 0;
						realYieldsEntirePixel.useLongValue(0L);
						
//						thisPixelMaturities[plantingWindowIndex][yearIndex] = -1;
						realMaturityEntirePixel.useLongValue(-500L);
						
						for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
							extraSummaryAccumulators[extraIndex].useLongValue(-500L);
						}

					}
				} // end of else concerning happy yield threshold being met...
				
			} // end plantingWindowIndex


			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////


			// do the summary out stuff
		
			statisticsOutLine = "";
			
			statisticsOutLine = 
					realYieldsEntirePixel.getMinAsLong() + delimiter 
					+ realYieldsEntirePixel.getMaxAsLong() + delimiter 
					+ realYieldsEntirePixel.getMean() + delimiter
					+ realYieldsEntirePixel.getStd() + delimiter
					+ realMaturityEntirePixel.getMean() + delimiter
					+ realMaturityEntirePixel.getStd() + delimiter
					+ happyYieldsEntirePixel.getMean() + delimiter
					+ happyMaturityEntirePixel.getMean();
			
			for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
				statisticsOutLine += delimiter + extraSummaryAccumulators[extraIndex].getMean();
			}
			
			statisticsOutLine += "\n";
			
			statisticsOut.print(statisticsOutLine);
//			statisticsOut.flush();

			// now we're ready to deal with the next pixel...
			// Beware the MAGIC NUMBER!!! checking every one percent...
			if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {
				System.out.println("prog: " + lineIndex + "/" + nLinesInDataFile + " = " +
						(float)(100 * (lineIndex + 1.0)/nLinesInDataFile) + "% sleep now = " +
						this.initialSleepTimeToUse + "/" + this.initialSleepTimeToUseHappy + " ; " +
						(float)readingTimesReal.getMean() + "/" + (float)readingTimesHappy.getMean()
				);
			}
		} // end for for lineIndex // old end of while loop

		double totalDSSATTimeMillis = thisTimer.tocMillis();

		// close out the output files
		statisticsOut.flush();
		statisticsOut.close();

		/////////////////////////////////////////
		// when all done, write out info files //
		/////////////////////////////////////////
		// Beware the MAGIC NUMBER!!!
		String columnList = "yield_min" + delimiter + "yield_max" + delimiter + "yield_mean" + delimiter
		+ "yield_std" + delimiter + "real_maturity_mean" + delimiter + "real_maturity_std" + delimiter
		+ "happy_yield_mean" + delimiter + "happy_maturity_mean";
		for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
			columnList += delimiter + this.extraNames[extraIndex];
		}
		columnList += "\n";
		
		
		FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

		long nRows = nLinesInDataFile;

		// Beware the MAGIC NUMBER!!!
		int nCols = 8 + extraStartIndices.length; // min / max/ mean / std / bad / happy mean / happy std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);



		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
		thisTimer.sinceStartMessage("running DSSAT");
		double overallTimeMillis = thisTimer.sinceStartMillis();

		System.out.println("total DSSAT = " + (float)(totalDSSATNanos/1000000000.0));
		System.out.println("total read  = " + (float)(totalReadNanos /1000000000.0));
		System.out.println("total write = " + (float)(totalWriteNanos/1000000000.0));
		
		System.out.println();
		System.out.println("reading: " + readingTimesReal.getAllPretty());
		System.out.println();
		
		
		System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
				+ totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms");
		System.out.println("overall per run average = " +
				overallTimeMillis/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"	);


	} // main

	public void doSimulationMultipleYearsSortaWorksBackup() throws Exception {/*


		//////////////////////////////////////////////////
		// check if everything has been set up properly //
		//////////////////////////////////////////////////
		if (!this.readyToRun) {
			System.out.println("DSSATRunner not ready to run... exiting...");
			return;
		}

		// set up a timer for the fun of it...
		TimerUtility thisTimer = new TimerUtility();
		TimerUtility dssatTimer = new TimerUtility();
		
		realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
		happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);
		realMaturityEntirePixel =  new DescriptiveStatisticsUtility(false);
		happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

		countStuff      = new DescriptiveStatisticsUtility(true);
		readingTimesHappy = new DescriptiveStatisticsUtility(true);
		
		long totalDSSATNanos = 0;
		long totalReadNanos = 0;
		long totalWriteNanos = 0;

		///////////////////////////////////
		// write out the provenance file //
		///////////////////////////////////
		System.out.println("-- starting to write provenance file --");
		this.writeProvenance();
		System.out.println("== done writing provenance file ==");

		///////////////////////////////////////
		// read in the whole template X file //
		///////////////////////////////////////
		String templateXFileContents = FunTricks.readTextFileToString(templateXFile);

		System.out.println("== done reading in template X file ==");

		///////////////////////////
		// variable declarations //
		///////////////////////////

		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		double nitrogenLevel = Double.NaN;
		int soilType = -1, firstPlantingDay = -4;
		int startingDayToPlantForThisWindow = -1;
		int endingDayToPlantForThisWindow = -99;
		int fakePlantingYearEnd = -101;
		int initializationDayForThisWindow = -4;
		int fakeInitializationYear = -5;
		int randomSeedToUse = -13;

		double meanYield = Double.NaN;
		double stdYield = Double.NaN; 

		String codeName = null;
		String allOutputLine = null;
		String allCodesLine = null;

		String sDTP = null;

		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;
		String fertilizerBlock  = null;

		String randomSeedCode = null;
		String nYearsCode = null;
		String nHappyYearsCode = null;
		String co2ppmCode = null;

		double badStat = Double.NaN;

		String soilTypeString = null;

		////////////////////////////
		// a couple magic numbers //
		////////////////////////////
		
		double magicOkBadStat = -1.0;
		double magicBadYieldAll = -2.0;
		
		int[] phenologyInDays = null;

		
		////////////////////////////////////////
		// set up stuff that we actually know //
		////////////////////////////////////////
		initialSleepTimeToUse = this.sleeptimeWaitForFileMillis;
		initialSleepTimeToUseHappy = this.sleeptimeWaitForFileMillis;
		
		String fullTempXFileName = pathToDSSATDirectory + tempXFileName;
		File fullTempXFile = new File(fullTempXFileName);

		File weatherStationFile = new File(magicWeatherStationNameToUsePath);
		
		// open up a writer for the statistics output file
		File statisticsFileObject = new File(yieldOutputBaseName  + "_STATS.txt");
		PrintWriter statisticsOut = new PrintWriter(statisticsFileObject);

		// declare a writer for the ALL file and open up if necessary
		File allFileObject = new File(yieldOutputBaseName  + "_ALL.txt");
		PrintWriter allOut = null;
		File allCodesFileObject = new File(yieldOutputBaseName  + "_ALL.codes.txt");
		PrintWriter allCodesOut = null;
		if (allFlag) {
			allOut     = new PrintWriter(allFileObject);
			allCodesOut = new PrintWriter(allCodesFileObject);
		}



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
		// Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast enough
		// that it doesn't matter. for big stuff, we'll want disk...
		int formatIndexToForce = 1;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);

		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];


		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		String XHappyStuffToWrite = null;
		
		// since this implementation (using the multiple years with a single random seed, rather
		// than multiple random seeds with a single year) has the seeds and years as invariants, do
		// them up front to save on a little search and replace overhead...
		randomSeedCode = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
		nYearsCode = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
		nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
		co2ppmCode = DSSATHelperMethods.padWithZeros(co2ppm, 4);

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
		
		
		
		String XInvariantsReplaced = templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);

		// start the timer for just DSSAT proper
		thisTimer.tic();
		dssatTimer.tic();
		
		// clear out the summary and error files if they exist...
		if (errorAsFileObject.exists()) {
			errorAsFileObject.delete();
		}
		if (summaryDSSATOutputAsFileObject.exists()) {
			summaryDSSATOutputAsFileObject.delete();
		}
		
		nTimesSuccessfullyReadFirstTime = 0;
		
		System.out.println("-- starting through data --");
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

			// initialize the descriptive statistics utility and storage array
			realYieldsEntirePixel.reset();
			happyYieldsEntirePixel.reset();
			realMaturityEntirePixel.reset();
			happyMaturityEntirePixel.reset();
			

			// we're not gonna initialize thisPixelsYields; we'll just re-use it repeatedly
			// it should have already been initialized in this.readInitFile(filename); 
			
//			thisPixelYields = new int[nPlantingWindowsPerMonth][nRandomSeedsToUse];

//			for (int winIndex = 0; winIndex < nPlantingWindowsPerMonth; winIndex++) {
//				for (int yearIndex = 0; yearIndex < nRandomSeedsToUse; yearIndex++) {
//					// Beware the MAGIC NUMBER!!! initializing with an obviously bad value...
//					thisPixelYields[winIndex][yearIndex] = Integer.MIN_VALUE;
//				}
//			}

//			cliStuffToWrite = DSSATHelperMethods.cliFileContentsMFM(dataMatrix, geogMatrix, lineIndex, SWmultiplier);
//			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayMFM(dataMatrix,lineIndex);

			cliStuffToWrite = DSSATHelperMethods.cliFileContentsMFM(dataMatrix, geogMatrix, lineIndex, SWmultiplier);
			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, lineIndex);

			dssatTimer.tic();
			FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
			totalWriteNanos += dssatTimer.TOCNanos();
			
			soilType         = nonClimateInfo[0];
			// elevation        = nonClimateInfo[1]; // don't need this here...
			// plantingMonth    = nonClimateInfo[2]; // don't need this here...
			firstPlantingDay = nonClimateInfo[3];
			nitrogenLevel = nonClimateInfo[4];

			// brute force padding
			// Beware the MAGIC ASSUMPTION!!! assuming two digit soil codes
			if (soilType < 10 && soilType > 0) {
				soilTypeString = magicSoilPrefix + 0 + soilType;
			} else if (soilType < 100 && soilType > 0) {
				soilTypeString = magicSoilPrefix  + soilType;
			} else {
				System.out.println("soil type number did not meet our criteria: > 0 and < 100: " + soilType);
				throw new Exception();
			}

			// loop over the planting windows and random seeds
			for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
				// pick the starting day/etc for this window
				startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
				initializationDayForThisWindow = startingDayToPlantForThisWindow - spinUpTimeDays;
				fakeInitializationYear = fakePlantingYear;

				// take care of the possibility that we will go before the beginning of this year
				while (initializationDayForThisWindow < 1) {
					initializationDayForThisWindow += nDaysInYear;
					fakeInitializationYear--;
				}

				endingDayToPlantForThisWindow = startingDayToPlantForThisWindow + plantingWindowLengthDays;
				fakePlantingYearEnd = fakePlantingYear;

				// take care of the possibility that we will go beyond the end of this year
				while (endingDayToPlantForThisWindow > nDaysInYear) {
					endingDayToPlantForThisWindow -= nDaysInYear;
					fakePlantingYearEnd++;
				}

				// format everything properly....
				startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
						+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
				endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
						+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
				initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
						+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd

				//////////////////////////////////////
				// let's run a happy plant first... //
				//////////////////////////////////////
				
				// X file
				// do the search and replace thing; the invariants have already been done above...
				XHappyStuffToWrite =         XHappyInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
				XHappyStuffToWrite =         XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
				XHappyStuffToWrite =         XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
				XHappyStuffToWrite =         XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
				
				FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);

				// run DSSAT with the happy plant


				Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

				//////////////////////////////////////////
				// extract the results & store to array //
				//////////////////////////////////////////

				// mean yield / anthesis / maturity in days after planting...
//				phenologyInDays = this.grabHappyResults(nHappyPlantRunsForPhenology);
				phenologyInDays = this.grabNewHappyResults(nHappyPlantRunsForPhenology);


				// keep track of the happy plant yields for the fun of it...
				happyYieldsEntirePixel.useLongValue(phenologyInDays[0]);
				happyMaturityEntirePixel.useLongValue(phenologyInDays[2]);
				
				
				// proceed, if the happy yield exceeds some theshold and the effective growing season isn't too long....
				if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {


					/////////////////////
					// make the X file //
					/////////////////////

					// create the fertilizer block...
					fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
							phenologyInDays[1] + phenologyBufferInDays, phenologyInDays[2] + phenologyBufferInDays, nitrogenLevel
					);

					// do the search and replace thing; the invariants have already been done above...
					XStuffToWrite =         XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
					XStuffToWrite =         XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
					XStuffToWrite = XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);

					// overwrite the old file with the new contents

					dssatTimer.tic();
					FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
					totalWriteNanos += dssatTimer.TOCNanos();

					// recommend garbage collection
					System.gc();

					///////////////
					// run DSSAT //
					///////////////

					Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
					totalDSSATNanos += dssatTimer.TOCNanos();

					//////////////////////////////////////////
					// extract the results & store to array //
					//////////////////////////////////////////

					this.grabRealResults(lineIndex, plantingWindowIndex);

					totalReadNanos += dssatTimer.TOCNanos();

					// bail out if we have already encountered bad things for this pixel...
					if (badThingsHappened) {
						break;
					}

				} // end if happy yield threshold is met
				else {
					// we need to set these things to zero artificially...
					for (int yearIndex = 0; yearIndex < nRandomSeedsToUse; yearIndex++) {
						thisPixelYields[plantingWindowIndex][yearIndex] = 0;
						realYieldsEntirePixel.useLongValue(0L);
						
						thisPixelMaturities[plantingWindowIndex][yearIndex] = -1;
						realMaturityEntirePixel.useLongValue(-1L);
					}
				} // end of else concerning happy yield threshold being met...
				
			} // end plantingWindowIndex


			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////


			// do the summary out stuff
			if (badThingsHappened) {
				// set the values for the STATS...
				// Beware the MAGIC NUMBER!!! assuming no more than 10 planting windows per month...
				badStat = badThingsHappenedIndex + badPlantingWindow/10.0;
				meanYield = magicBadYieldAll;
				stdYield  = magicBadYieldAll; 
			} else {
				// set the values for the STATS...
				badStat = magicOkBadStat;
//				meanYield = totalYield / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse);
//				stdYield  = Math.sqrt(totalYieldSquared / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse) - meanYield * meanYield );
				meanYield = realYieldsEntirePixel.getMean();
				stdYield  = realYieldsEntirePixel.getStd();
//				System.out.println(realYieldsEntirePixel.getAllPretty());
			}
			
			statisticsOut.print(
					realYieldsEntirePixel.getMinAsLong() + delimiter 
					+ realYieldsEntirePixel.getMaxAsLong() + delimiter 
					+ meanYield + delimiter
					+ stdYield + delimiter
					+ badStat + delimiter
					+ realMaturityEntirePixel.getMean() + delimiter
					+ realMaturityEntirePixel.getStd() + delimiter
					+ happyYieldsEntirePixel.getMean() + delimiter
					+ happyYieldsEntirePixel.getStd() + delimiter
					+ happyMaturityEntirePixel.getMean() + delimiter
					+ happyMaturityEntirePixel.getStd() + delimiter
					+ "\n"
					);
//			statisticsOut.flush();


			if (allFlag) {
				allOutputLine = "";
				allCodesLine = "";
				// put "bad things happened" back in here...
				if (!badThingsHappened) {
					for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth; plantingWindowIndex++) {
						startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
						sDTP = DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow);

						for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse ; randomSeedIndex++) {
//							randomSeedToUse = firstRandomSeed + randomSeedIndex;
							randomSeedToUse = randomSeedIndex;
							randomSeedCode = DSSATHelperMethods.padWithZeros(randomSeedToUse, 5);

							codeName = "pday" + sDTP + "_yr" + randomSeedCode;
							allCodesLine += codeName + "\t";

							allOutputLine += thisPixelYields[plantingWindowIndex][randomSeedIndex] + delimiter;

						} // randomSeedIndex
					} // plantingWindowIndex
				} else {
					// ok, i'm being stupid here. presumeably, we should have it write out as much as we actually know.
					// but i'm going to be silly and have it just write down bad values for everything since finding
					// and solving the problem will need more than merely knowing the yields that actually worked....
					for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth; plantingWindowIndex++) {
						startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
						sDTP = DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow);

						for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse ; randomSeedIndex++) {
//							randomSeedToUse = firstRandomSeed + randomSeedIndex;
							randomSeedToUse = randomSeedIndex;
							randomSeedCode = DSSATHelperMethods.padWithZeros(randomSeedToUse, 5);

							codeName = "pday" + sDTP + "_yr" + randomSeedCode;
							allCodesLine += codeName + "\t";

							allOutputLine += Integer.MIN_VALUE + delimiter;

						} // randomSeedIndex
					}
				}
				allOut.print(     allOutputLine.substring(0,allOutputLine.length() - 1) + "\n");
				allCodesOut.print( allCodesLine.substring(0,allCodesLine.length()  - 1) + "\n");
			} // if all


			// now we're ready to deal with the next pixel...
			// Beware the MAGIC NUMBER!!! checking every one percent...
			if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {
				System.out.println("prog: " + lineIndex + "/" + nLinesInDataFile + " = " +
						(float)(100 * (lineIndex + 1.0)/nLinesInDataFile) + "% sleep now = " +
						this.initialSleepTimeToUse + "/" + this.initialSleepTimeToUseHappy + " ; " +
						(float)countStuff.getMean() + "/" + (float)readingTimesHappy.getMean()
				);
			}
		} // end for for lineIndex // old end of while loop

		double totalDSSATTimeMillis = thisTimer.tocMillis();

		// close out the output files
		if (allFlag) {
			allOut.flush();
			allOut.close();

			allCodesOut.flush();
			allCodesOut.close();
		}
		statisticsOut.flush();
		statisticsOut.close();

		/////////////////////////////////////////
		// when all done, write out info files //
		/////////////////////////////////////////

		long nRows = nLinesInDataFile;
		long nCols = (nPlantingWindowsPerMonth * nRandomSeedsToUse);
		if (allFlag) {
			FunTricks.writeInfoFile(yieldOutputBaseName  + "_ALL", nRows, nCols, delimiter);
		}

		// Beware the MAGIC NUMBER!!!
		nCols = 11; // min / max/ mean / std / bad / happy mean / happy std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);


		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
		thisTimer.sinceStartMessage("running DSSAT");
		double overallTimeMillis = thisTimer.sinceStartMillis();

		System.out.println("total DSSAT = " + (float)(totalDSSATNanos/1000000000.0));
		System.out.println("total read  = " + (float)(totalReadNanos /1000000000.0));
		System.out.println("total write = " + (float)(totalWriteNanos/1000000000.0));
		
		System.out.println();
		System.out.println("reading: " + countStuff.getAllPretty());
		System.out.println();
		
		
		System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
				+ totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms");
		System.out.println("overall per run average = " +
				overallTimeMillis/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"	);


		
	*/}
	
	public void doSimulationsMultipleYearsOriginal() throws Exception {/*

		//////////////////////////////////////////////////
		// check if everything has been set up properly //
		//////////////////////////////////////////////////
		if (!this.readyToRun) {
			System.out.println("DSSATRunner not ready to run... exiting...");
			return;
		}

		// set up a timer for the fun of it...
		TimerUtility thisTimer = new TimerUtility();
		TimerUtility dssatTimer = new TimerUtility();
		TimerUtility extraTimer = new TimerUtility();
		
		DescriptiveStatisticsUtility countStuff = new DescriptiveStatisticsUtility(true);
		
		long totalDSSATNanos = 0;
		long totalReadNanos = 0;
		long totalWriteNanos = 0;
		double singleReadTime = 0;
		///////////////////////////////////
		// write out the provenance file //
		///////////////////////////////////
		System.out.println("-- starting to write provenance file --");
		this.writeProvenance();
		System.out.println("== done writing provenance file ==");

		///////////////////////////////////////
		// read in the whole template X file //
		///////////////////////////////////////
		String templateXFileContents = FunTricks.readTextFileToString(templateXFile);

		System.out.println("== done reading in template X file ==");

		///////////////////////////
		// variable declarations //
		///////////////////////////

		FileReader DSSATSummaryStream = null;
		BufferedReader DSSATSummaryReader = null;

		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		int soilType = -1, firstPlantingDay = -4;
		int startingDayToPlantForThisWindow = -1;
		int endingDayToPlantForThisWindow = -99;
		int fakePlantingYearEnd = -101;
		int initializationDayForThisWindow = -4;
		int fakeInitializationYear = -5;
		int randomSeedToUse = -13;

//		int[][] thisPixelYields = null;
		long totalYield = -5;
		long totalYieldSquared = -6;
		int maxYield = -1;
		int minYield = 100000001;
		double meanYield = Double.NaN;
		double stdYield = Double.NaN; 

		String goodSummaryLine = null;

		String codeName = null;
		String allOutputLine = null;
		String allCodesLine = null;

		String sDTP = null;

		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;

		String randomSeedCode = null;
		String nYearsCode = null;
		String co2ppmCode = null;

		boolean readSuccessfully = false;
		boolean badThingsHappened = false;
		double badStat = Double.NaN;
		int badThingsHappenedIndex = 0;
//		int badRandomSeed = -5;
		int badPlantingWindow = -6;
		int nParsingTries = 0;
		int nParsingTotalThisRun = 0;

		String soilTypeString = null;

		////////////////////////////
		// a couple magic numbers //
		////////////////////////////
		
		double magicOkBadStat = -100.0;
		double magicBadYieldAll = -200.0;

		
		////////////////////////////////////////
		// set up stuff that we actually know //
		////////////////////////////////////////
		long initialSleepTimeToUse = this.sleeptimeWaitForFileMillis;
		long retrySleepTimeToUse = 2; // Beware the MAGIC NUMBER!!!
//		long previousSleepTime = sleepTimeToUse;
		int nTimesSuccessfullyReadFirstTime = 0;
		
		int timeCheckNumber = 2;
		
		String fullTempXFileName = pathToDSSATDirectory + tempXFileName;
		File fullTempXFile = new File(fullTempXFileName);

		File weatherStationFile = new File(magicWeatherStationNameToUsePath);
		
		// open up a writer for the statistics output file
		File statisticsFileObject = new File(yieldOutputBaseName  + "_STATS.txt");
		PrintWriter statisticsOut = new PrintWriter(statisticsFileObject);

		// declare a writer for the ALL file and open up if necessary
		File allFileObject = new File(yieldOutputBaseName  + "_ALL.txt");
		PrintWriter allOut = null;
		File allCodesFileObject = new File(yieldOutputBaseName  + "_ALL.codes.txt");
		PrintWriter allCodesOut = null;
		if (allFlag) {
			allOut     = new PrintWriter(allFileObject);
			allCodesOut = new PrintWriter(allCodesFileObject);
		}



		///////////////////////////////
		// start doing the real work //
		///////////////////////////////


		int plantingWindowSpacing = nDaysInMonth / nPlantingWindowsPerMonth;
		if (plantingWindowSpacing < 1) {
			plantingWindowSpacing = 1;
		}

		// write out the DSSAT initialization file...
		FunTricks.writeStringToFile(magicInitializationContents, magicInitializationFilePath);

		// create giant arrays of the data and geog stuff
		String[] dataLinesArray = FunTricks.readTextFileToArray(gisTableBaseName + "_data.txt");
		String[] geogLinesArray = FunTricks.readTextFileToArray(gisTableBaseName + "_geog.txt");

		int nLinesInDataFile = dataLinesArray.length;


		// while loop that steps through the file...
		// initialize with the very first line
		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		
		// since this implementation (using the multiple years with a single random seed, rather
		// than multiple random seeds with a single year) has the seeds and years as invariants, do
		// them up front to save on a little search and replace overhead...
		randomSeedCode = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
		nYearsCode = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
		co2ppmCode = DSSATHelperMethods.padWithZeros(co2ppm, 4);

		String XInvariantsReplaced = templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
		XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);

		// start the timer for just DSSAT proper
		thisTimer.tic();
		dssatTimer.tic();
		
		// clear out the summary and error files if they exist...
		if (errorAsFileObject.exists()) {
			errorAsFileObject.delete();
		}
		if (summaryDSSATOutputAsFileObject.exists()) {
			summaryDSSATOutputAsFileObject.delete();
		}
		
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

			// initialize the storage array / etc
			thisPixelYields = new int[nPlantingWindowsPerMonth][nRandomSeedsToUse];
			totalYield = 0L;
			totalYieldSquared = 0L;
			maxYield = Integer.MIN_VALUE;
			minYield = Integer.MAX_VALUE;

			cliStuffToWrite = DSSATHelperMethods.cliFileContents(dataLinesArray[lineIndex], geogLinesArray[lineIndex], SWmultiplier);
			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDay(dataLinesArray[lineIndex]);

			dssatTimer.tic();
			FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
			totalWriteNanos += dssatTimer.TOCNanos();
			
			soilType         = nonClimateInfo[0];
			// elevation        = nonClimateInfo[1]; // don't need this here...
			// plantingMonth    = nonClimateInfo[2]; // don't need this here...
			firstPlantingDay = nonClimateInfo[3];

			// brute force padding
			// Beware the MAGIC ASSUMPTION!!! assuming two digit soil codes
			if (soilType < 10 && soilType > 0) {
				soilTypeString = magicSoilPrefix + 0 + soilType;
			} else if (soilType < 100 && soilType > 0) {
				soilTypeString = magicSoilPrefix  + soilType;
			} else {
				System.out.println("soil type number did not meet our criteria: > 0 and < 100: " + soilType);
				throw new Exception();
			}

			// loop over the planting windows and random seeds
			for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
				// pick the starting day/etc for this window
				startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
				initializationDayForThisWindow = startingDayToPlantForThisWindow - spinUpTimeDays;
				fakeInitializationYear = fakePlantingYear;

				// take care of the possibility that we will go before the beginning of this year
				while (initializationDayForThisWindow < 1) {
					initializationDayForThisWindow += nDaysInYear;
					fakeInitializationYear--;
				}

				endingDayToPlantForThisWindow = startingDayToPlantForThisWindow + plantingWindowLengthDays;
				fakePlantingYearEnd = fakePlantingYear;

				// take care of the possibility that we will go beyond the end of this year
				while (endingDayToPlantForThisWindow > nDaysInYear) {
					endingDayToPlantForThisWindow -= nDaysInYear;
					fakePlantingYearEnd++;
				}

				// format everything properly....
				startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
						+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
				endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
						+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
				initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
						+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd


				/////////////////////
				// make the X file //
				/////////////////////

				// do the search and replace thing; the invariants have already been done above...
				XStuffToWrite =         XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
				XStuffToWrite =         XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
				XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
				XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);

				// do a "happy plant" version of the X-file
				// run as happy plant
				
				
				
				/////////////////////////////////////////////////////////////////////
				// this is where we would append irrigation and nitrogen schedules //
				/////////////////////////////////////////////////////////////////////				
				
				// overwrite the old file with the new contents

				dssatTimer.tic();
				FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
				totalWriteNanos += dssatTimer.TOCNanos();
				
				///////////////
				// run DSSAT //
				///////////////

				Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

				//////////////////////////////////////////
				// extract the results & store to array //
				//////////////////////////////////////////
				totalDSSATNanos += dssatTimer.TOCNanos();
				
				readSuccessfully = false;
				nParsingTries = 0;
				nParsingTotalThisRun = 0;
				badThingsHappened = false;
				badThingsHappenedIndex = -1;
				badPlantingWindow = 9;
				extraTimer.tic();
				while (!readSuccessfully) {
					
					// determine whether we want to increase the initial wait time:
					if (nTimesSuccessfullyReadFirstTime == 0 && nParsingTries == 1) {
						initialSleepTimeToUse = Math.min(initialSleepTimeToUse + 1, countStuff.getMinAsLong());
						initialSleepTimeToUse++;
						System.out.println(lineIndex + ", window=" + plantingWindowIndex + "; new sleep time = " + initialSleepTimeToUse);
					}
					
					// determine whether we want to decrease the initial wait time:
					if (nTimesSuccessfullyReadFirstTime > 0 && nTimesSuccessfullyReadFirstTime % timeCheckNumber == 0 && nParsingTries == 0) {
						initialSleepTimeToUse--;
						System.out.println(lineIndex + ", window=" + plantingWindowIndex + "; new sleep time = " + initialSleepTimeToUse);
					}
										
					if (nParsingTries == 0) {
						Thread.sleep(initialSleepTimeToUse);
					} else {
						Thread.sleep(retrySleepTimeToUse);
					}

					nParsingTries++;

					try {
						nParsingTotalThisRun++;
						if (nParsingTotalThisRun > this.hardLimitOnReReads) {							
							
							System.out.println("breaking due to excessive read attempts; seed = " + badThingsHappenedIndex + "; window = " + plantingWindowIndex);
							badThingsHappenedIndex++; // bump up the badThingsHappenedIndex
							badThingsHappened = true;
							badPlantingWindow = plantingWindowIndex;
							break;
						}

						DSSATSummaryStream = new FileReader(magicDSSATSummaryToReadPath);
						DSSATSummaryReader = new BufferedReader(DSSATSummaryStream);

						// read through the stuff we don't care about
						for (int junkLineIndex = 0; junkLineIndex < magicDSSATSummaryLineIndexToRead ; junkLineIndex++) {
							DSSATSummaryReader.readLine();
						}

						///////////////////////////////
						// begin reading output file //
						///////////////////////////////
						
						for (int fakeYearIndex = 0; fakeYearIndex < nRandomSeedsToUse; fakeYearIndex++) {
							badThingsHappenedIndex = fakeYearIndex;
							// grab the line with actual information
							goodSummaryLine = DSSATSummaryReader.readLine();
							// pull out the harvest yield at harvest
							thisPixelYields[plantingWindowIndex][fakeYearIndex] =
								Integer.parseInt(goodSummaryLine.substring(magicHarvestedWeightAtHarvestStartIndex,
										magicHarvestedWeightAtHarvestEndIndex).trim());
						}

						// now accumulate various statistics
						// we will do this in a separate loop to make sure everything has been read in before doing
						// the accumulation. otherwise, we get non-deterministic computing due to partial file reads...
						for (int fakeYearIndex = 0; fakeYearIndex < nRandomSeedsToUse; fakeYearIndex++) {
							totalYield        += thisPixelYields[plantingWindowIndex][fakeYearIndex];
							totalYieldSquared += thisPixelYields[plantingWindowIndex][fakeYearIndex]
							                                                          * thisPixelYields[plantingWindowIndex][fakeYearIndex];
							if (thisPixelYields[plantingWindowIndex][fakeYearIndex] > maxYield) {
								maxYield = thisPixelYields[plantingWindowIndex][fakeYearIndex];
							}
							if (thisPixelYields[plantingWindowIndex][fakeYearIndex] < minYield) {
								minYield = thisPixelYields[plantingWindowIndex][fakeYearIndex];
							}
						}
						
						///////////////////////////////
						// end reading output file //
						///////////////////////////////

						// we have successfully read it, so flip the flag
						// delete it so that we don't accidentally find it next time...
						readSuccessfully = true;
						nTimesSuccessfullyReadFirstTime++;
						
						DSSATSummaryReader.close();
						DSSATSummaryStream.close();

						summaryDSSATOutputAsFileObject.delete();

					} catch (NumberFormatException nfe) {
						// do nothing so that it will return and try again
						System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [number format exception]; line index " + lineIndex + ", window = " + plantingWindowIndex);
						DSSATSummaryReader.close();
						nTimesSuccessfullyReadFirstTime = 0;
					} catch (NullPointerException npe) {
						// do nothing so that it will return and try again							
						DSSATSummaryReader.close();
						nTimesSuccessfullyReadFirstTime = 0;
						
						// check for error file.
						if (errorAsFileObject.exists()) {
							// something bad happened
							badThingsHappened = true;

							badThingsHappenedIndex++; // bump up the badThingsHappenedIndex
							badPlantingWindow = plantingWindowIndex;

							errorAsFileObject.delete();
							break;
						}
						// otherwise, we just wait for the rest of the file to be written out...
					} catch (java.io.FileNotFoundException fnfe) {
						System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [file not found exception] + (line " + lineIndex + ", window = " + plantingWindowIndex + ")");

						// wait another moment and then check for the error file...
						Thread.sleep(initialSleepTimeToUse);
						// check for error file
						if (errorAsFileObject.exists()) {
							badThingsHappened = true;

							badThingsHappenedIndex++; // bump up the badThingsHappenedIndex
							badPlantingWindow = plantingWindowIndex;

							errorAsFileObject.delete();
							break;
						}
						
						if (nParsingTries > maxParsingTries) {
							System.out.println("re-running DSSAT after " + nParsingTries + "; line index " + lineIndex + ", window = " + plantingWindowIndex);
//							System.out.println(dssatExecutionCommand[0] + " " + dssatExecutionCommand[1] + " " + dssatExecutionCommand[2]);
							Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
							nParsingTries = 0;
						}
						nTimesSuccessfullyReadFirstTime = 0;
					} catch (StringIndexOutOfBoundsException sioobe) {
						System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [string index out of bounds exception]; line index " + lineIndex + ", window = " + plantingWindowIndex);
						nTimesSuccessfullyReadFirstTime = 0;
					}
				} // end while(!readSuccessfully)
				totalReadNanos += dssatTimer.TOCNanos();
				singleReadTime = extraTimer.tocMillis();
				countStuff.useDoubleValue(singleReadTime);
				System.out.println("\t\tline " + lineIndex + ", window " + plantingWindowIndex + " time = " + singleReadTime + " min = " + countStuff.getMinAsDouble());
			} // end plantingWindowIndex


			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////

			meanYield = totalYield / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse);
			stdYield  = Math.sqrt(totalYieldSquared / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse) - meanYield * meanYield ); 

			// do the summary out stuff
			if (badThingsHappened) {
				badStat = badThingsHappenedIndex + badPlantingWindow/10.0;
			} else {
				badStat = magicOkBadStat;
			}

			if (allFlag) {
				allOutputLine = "";
				allCodesLine = "";
				for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth; plantingWindowIndex++) {
					startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
					sDTP = DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow);

					for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse ; randomSeedIndex++) {
//						randomSeedToUse = firstRandomSeed + randomSeedIndex;
						randomSeedToUse = randomSeedIndex;
						randomSeedCode = DSSATHelperMethods.padWithZeros(randomSeedToUse, 5);

						codeName = "pday" + sDTP + "_yr" + randomSeedCode;
						allCodesLine += codeName + "\t";
						
						// badThingsHappenedIndex is initialized at -1...
						if ( (!badThingsHappened) || ((badThingsHappened) && (randomSeedIndex < badThingsHappenedIndex))) {
							allOutputLine += thisPixelYields[plantingWindowIndex][randomSeedIndex] + delimiter;
						} else {
							allOutputLine += magicBadYieldAll + delimiter;
						}

					} // randomSeedIndex
				} // plantingWindowIndex
				allOut.print(     allOutputLine.substring(0,allOutputLine.length() - 1) + "\n");
				allCodesOut.print( allCodesLine.substring(0,allCodesLine.length()  - 1) + "\n");
			} // if all

			if (badThingsHappened) {
				statisticsOut.print(-1 + delimiter + -1 + delimiter + -1 + delimiter + -1 + delimiter + badStat + "\n");
			} else {
				statisticsOut.print(minYield + delimiter + maxYield + delimiter + meanYield + delimiter + stdYield + delimiter + badStat + "\n");
			}
			statisticsOut.flush();

			// now we're ready to deal with the next pixel...

		} // end for for lineIndex // old end of while loop

		double totalDSSATTimeMillis = thisTimer.tocMillis();

		// close out the output files
		if (allFlag) {
			allOut.flush();
			allOut.close();

			allCodesOut.flush();
			allCodesOut.close();
		}
		statisticsOut.flush();
		statisticsOut.close();

		/////////////////////////////////////////
		// when all done, write out info files //
		/////////////////////////////////////////

		long nRows = nLinesInDataFile;
		long nCols = (nPlantingWindowsPerMonth * nRandomSeedsToUse);
		if (allFlag) {
			FunTricks.writeInfoFile(yieldOutputBaseName  + "_ALL", nRows, nCols, delimiter);
		}

		// Beware the MAGIC NUMBER!!!
		nCols = 5; // min / max/ mean / std / bad
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);


		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
		thisTimer.sinceStartMessage("running DSSAT");
		double overallTimeMillis = thisTimer.sinceStartMillis();

		System.out.println("total DSSAT = " + (float)(totalDSSATNanos/1000000000.0));
		System.out.println("total read  = " + (float)(totalReadNanos /1000000000.0));
		System.out.println("total write = " + (float)(totalWriteNanos/1000000000.0));
		
		System.out.println();
		System.out.println("reading: " + countStuff.getAllPretty());
		System.out.println();
		
		
		System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
				+ totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms");
		System.out.println("overall per run average = " +
				overallTimeMillis/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"	);


	*/} // main

	

	public void doSimulationsMultipleYearsAllClimateSupplied() throws Exception {

	//////////////////////////////////////////////////
	// check if everything has been set up properly //
	//////////////////////////////////////////////////
	if (!this.readyToRun) {
		System.out.println("DSSATRunner not ready to run... exiting...");
		return;
	}

	// set up a timer for the fun of it...
	TimerUtility thisTimer = new TimerUtility();
	TimerUtility dssatTimer = new TimerUtility();
	
	realYieldsEntirePixel    = new DescriptiveStatisticsUtility(false);
	happyYieldsEntirePixel   = new DescriptiveStatisticsUtility(false);
	realMaturityEntirePixel  = new DescriptiveStatisticsUtility(false);
	happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

	readingTimesReal  = new DescriptiveStatisticsUtility(true);
	readingTimesHappy = new DescriptiveStatisticsUtility(true);
	
	long totalDSSATNanos = 0;
	long totalReadNanos = 0;
	long totalWriteNanos = 0;

	///////////////////////////////////
	// write out the provenance file //
	///////////////////////////////////
	System.out.println("-- starting to write provenance file --");
	this.writeProvenance();
	System.out.println("== done writing provenance file ==");

	///////////////////////////////////////
	// read in the whole template X file //
	///////////////////////////////////////
	String templateXFileContents = FunTricks.readTextFileToString(templateXFile);

	System.out.println("== done reading in template X file ==");

	///////////////////////////
	// variable declarations //
	///////////////////////////

	int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
	double nitrogenLevel = Double.NaN;
	int soilType = -1, firstPlantingDay = -4;
	int startingDayToPlantForThisWindow = -1;
	int endingDayToPlantForThisWindow = -99;
	int fakePlantingYearEnd = -101;
	int initializationDayForThisWindow = -4;
	int fakeInitializationYear = -5;

//	double meanYield = Double.NaN;
//	double stdYield = Double.NaN; 
	
	String startingDayToPlantCode = null;
	String endingDayToPlantCode   = null;
	String initializationDayCode  = null;
	String fertilizerBlock  = null;

	String randomSeedCode = null;
	String nYearsCode = null;
	String nHappyYearsCode = null;
	String co2ppmCode = null;

//	double badStat = Double.NaN;

	String soilTypeString = null;

	int[] phenologyInDays = null;

	String statisticsOutLine = null;
	
	
	////////////////////////////
	// a couple magic numbers //
	////////////////////////////
	
//	double magicOkBadStat = -1.0;
//	double magicBadYieldAll = -2.0;
	

	
	////////////////////////////////////////
	// set up stuff that we actually know //
	////////////////////////////////////////
	initialSleepTimeToUse = this.sleeptimeWaitForFileMillis;
	initialSleepTimeToUseHappy = this.sleeptimeWaitForFileMillis;
	
	String fullTempXFileName = pathToDSSATDirectory + tempXFileName;
	File fullTempXFile = new File(fullTempXFileName);

	File weatherStationFile = new File(magicWeatherStationNameToUsePath);
	
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
	// Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast enough
	// that it doesn't matter. for big stuff, we'll want disk...
	int formatIndexToForce = 1;
	MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
	MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);

	if (geogMatrix.getDimensions()[1] != 4) {
		System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
		throw new Exception();
	}
	
	int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];


	String cliStuffToWrite = null;
	String XStuffToWrite = null;
	String XHappyStuffToWrite = null;
	
	// since this implementation (using the multiple years with a single random seed, rather
	// than multiple random seeds with a single year) has the seeds and years as invariants, do
	// them up front to save on a little search and replace overhead...
	randomSeedCode  = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
	nYearsCode      = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
	nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
	co2ppmCode      = DSSATHelperMethods.padWithZeros(co2ppm, 4);

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
	
	
	
	String XInvariantsReplaced = templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
	XInvariantsReplaced = XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
	XInvariantsReplaced = XInvariantsReplaced.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
	XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder             , co2ppmCode);

	// start the timer for just DSSAT proper
	thisTimer.tic();
	dssatTimer.tic();
	
	// clear out the summary and error files if they exist...
	if (errorAsFileObject.exists()) {
		errorAsFileObject.delete();
	}
	if (summaryDSSATOutputAsFileObject.exists()) {
		summaryDSSATOutputAsFileObject.delete();
	}
	
	nTimesSuccessfullyReadFirstTime = 0;
	
	System.out.println("-- starting through data --");
	for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

		// initialize the descriptive statistics utility and storage array
		   realYieldsEntirePixel.reset();
		  happyYieldsEntirePixel.reset();
			 realMaturityEntirePixel.reset();
		happyMaturityEntirePixel.reset();
		
		for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
			extraSummaryAccumulators[extraIndex].reset();
		}


		// figure out the climate file...
//		cliStuffToWrite = DSSATHelperMethods.cliFileContentsMFM(dataMatrix, geogMatrix, lineIndex, SWmultiplier);

		cliStuffToWrite = DSSATHelperMethods.cliFileContentsAllSupplied(dataMatrix, geogMatrix, lineIndex, SWmultiplier);
		nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, lineIndex);

		dssatTimer.tic();
		FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
		totalWriteNanos += dssatTimer.TOCNanos();
		
		soilType         = nonClimateInfo[0];
		// elevation        = nonClimateInfo[1]; // don't need this here...
		// plantingMonth    = nonClimateInfo[2]; // don't need this here...
		firstPlantingDay = nonClimateInfo[3];
		nitrogenLevel = nonClimateInfo[4];

		// brute force padding
		// Beware the MAGIC ASSUMPTION!!! assuming two digit soil codes
		if (soilType < 10 && soilType > 0) {
			soilTypeString = magicSoilPrefix + 0 + soilType;
		} else if (soilType < 100 && soilType > 0) {
			soilTypeString = magicSoilPrefix  + soilType;
		} else {
			System.out.println("soil type number did not meet our criteria: > 0 and < 100: " + soilType);
			throw new Exception();
		}

		// loop over the planting windows and random seeds
		for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
			// pick the starting day/etc for this window
			startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
			initializationDayForThisWindow = startingDayToPlantForThisWindow - spinUpTimeDays;
			fakeInitializationYear = fakePlantingYear;

			// take care of the possibility that we will go before the beginning of this year
			while (initializationDayForThisWindow < 1) {
				initializationDayForThisWindow += nDaysInYear;
				fakeInitializationYear--;
			}

			endingDayToPlantForThisWindow = startingDayToPlantForThisWindow + plantingWindowLengthDays;
			fakePlantingYearEnd = fakePlantingYear;

			// take care of the possibility that we will go beyond the end of this year
			while (endingDayToPlantForThisWindow > nDaysInYear) {
				endingDayToPlantForThisWindow -= nDaysInYear;
				fakePlantingYearEnd++;
			}

			// format everything properly....
			startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
					+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
			endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
					+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
			initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
					+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd

			//////////////////////////////////////
			// let's run a happy plant first... //
			//////////////////////////////////////
			
			// X file
			// do the search and replace thing; the invariants have already been done above...
			XHappyStuffToWrite = XHappyInvariantsReplaced.replaceAll(soilPlaceholder               , soilTypeString);
			XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
			XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
			XHappyStuffToWrite =       XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
			
			FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);

			// run DSSAT with the happy plant

			Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

			//////////////////////////////////////////
			// extract the results & store to array //
			//////////////////////////////////////////

			// mean yield / anthesis / maturity in days after planting...
//			phenologyInDays = this.grabHappyResults(nHappyPlantRunsForPhenology);
			phenologyInDays = this.grabNewHappyResults(nHappyPlantRunsForPhenology);


			// keep track of the happy plant yields for the fun of it...
				  happyYieldsEntirePixel.useLongValue(phenologyInDays[0]);
			happyMaturityEntirePixel.useLongValue(phenologyInDays[2]);
			
			
			// proceed, if the happy yield exceeds some theshold and the effective growing season isn't too long....
			if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {


				/////////////////////
				// make the X file //
				/////////////////////

				// create the fertilizer block...
				fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
						phenologyInDays[1] + phenologyBufferInDays, phenologyInDays[2] + phenologyBufferInDays, nitrogenLevel
				);

				// do the search and replace thing; the invariants have already been done above...
				XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
				XStuffToWrite =       XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
				XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
				XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
				XStuffToWrite =       XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);

				// overwrite the old file with the new contents

				dssatTimer.tic();
				FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
				totalWriteNanos += dssatTimer.TOCNanos();

				// recommend garbage collection
				System.gc();

				///////////////
				// run DSSAT //
				///////////////

				Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
				totalDSSATNanos += dssatTimer.TOCNanos();

				//////////////////////////////////////////
				// extract the results & store to array //
				//////////////////////////////////////////

//				this.grabRealResults(lineIndex, plantingWindowIndex);
//				this.grabNewRealResults(this.nRandomSeedsToUse);
				this.grabNewManyResults(this.nRandomSeedsToUse);

				totalReadNanos += dssatTimer.TOCNanos();

				// bail out if we have already encountered bad things for this pixel...
				if (badThingsHappened) {
					break;
				}

			} // end if happy yield threshold is met
			else {
				// we need to set these things to zero artificially...
				for (int yearIndex = 0; yearIndex < nRandomSeedsToUse; yearIndex++) {
//					thisPixelYields[plantingWindowIndex][yearIndex] = 0;
					realYieldsEntirePixel.useLongValue(0L);
					
//					thisPixelMaturities[plantingWindowIndex][yearIndex] = -1;
					realMaturityEntirePixel.useLongValue(-500L);
					
					for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
						extraSummaryAccumulators[extraIndex].useLongValue(-500L);
					}

				}
			} // end of else concerning happy yield threshold being met...
			
		} // end plantingWindowIndex


		//////////////////////////////////////////////////////////////////////////
		// when finished with a pixel, then write out a line to the output file //
		//////////////////////////////////////////////////////////////////////////


		// do the summary out stuff
	
		statisticsOutLine = "";
		
		statisticsOutLine = 
				realYieldsEntirePixel.getMinAsLong() + delimiter 
				+ realYieldsEntirePixel.getMaxAsLong() + delimiter 
				+ realYieldsEntirePixel.getMean() + delimiter
				+ realYieldsEntirePixel.getStd() + delimiter
				+ realMaturityEntirePixel.getMean() + delimiter
				+ realMaturityEntirePixel.getStd() + delimiter
				+ happyYieldsEntirePixel.getMean() + delimiter
				+ happyMaturityEntirePixel.getMean();
		
		for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
			statisticsOutLine += delimiter + extraSummaryAccumulators[extraIndex].getMean();
		}
		
		statisticsOutLine += "\n";
		
		statisticsOut.print(statisticsOutLine);
//		statisticsOut.flush();

		// now we're ready to deal with the next pixel...
		// Beware the MAGIC NUMBER!!! checking every one percent...
		if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {
			System.out.println("prog: " + lineIndex + "/" + nLinesInDataFile + " = " +
					(float)(100 * (lineIndex + 1.0)/nLinesInDataFile) + "% sleep now = " +
					this.initialSleepTimeToUse + "/" + this.initialSleepTimeToUseHappy + " ; " +
					(float)readingTimesReal.getMean() + "/" + (float)readingTimesHappy.getMean()
			);
		}
	} // end for for lineIndex // old end of while loop

	double totalDSSATTimeMillis = thisTimer.tocMillis();

	// close out the output files
	statisticsOut.flush();
	statisticsOut.close();

	/////////////////////////////////////////
	// when all done, write out info files //
	/////////////////////////////////////////
	// Beware the MAGIC NUMBER!!!
	String columnList = "yield_min" + delimiter + "yield_max" + delimiter + "yield_mean" + delimiter
	+ "yield_std" + delimiter + "real_maturity_mean" + delimiter + "real_maturity_std" + delimiter
	+ "happy_yield_mean" + delimiter + "happy_maturity_mean";
	for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
		columnList += delimiter + this.extraNames[extraIndex];
	}
	columnList += "\n";
	
	
	FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

	long nRows = nLinesInDataFile;

	// Beware the MAGIC NUMBER!!!
	int nCols = 8 + extraStartIndices.length; // min / max/ mean / std / bad / happy mean / happy std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
	FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);



	/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
	thisTimer.sinceStartMessage("running DSSAT");
	double overallTimeMillis = thisTimer.sinceStartMillis();

	System.out.println("total DSSAT = " + (float)(totalDSSATNanos/1000000000.0));
	System.out.println("total read  = " + (float)(totalReadNanos /1000000000.0));
	System.out.println("total write = " + (float)(totalWriteNanos/1000000000.0));
	
	System.out.println();
	System.out.println("reading: " + readingTimesReal.getAllPretty());
	System.out.println();
	
	
	System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
			+ totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms");
	System.out.println("overall per run average = " +
			overallTimeMillis/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"	);


} // main


}

