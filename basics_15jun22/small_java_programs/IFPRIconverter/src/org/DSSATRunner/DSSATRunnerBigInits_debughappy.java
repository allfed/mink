package org.DSSATRunner;

import java.io.*;
import java.util.Date;

import org.R2Useful.*;

public class DSSATRunnerBigInits_debughappy {

	private boolean readyToRun = false;


	/////////////////////////////////////
	// create a bunch of magic numbers //
	/////////////////////////////////////
	public static final String delimiter = "\t";
	public static final String magicWeatherStationNameToUse     = "RRRR";
	public static final String magicDSSATSummaryToRead          = "Summary.OUT";
	public static final String magicErrorFile                   = "ERROR.OUT";
//	public static final String magicInitializationFile          = "deleteme.dv4";
	public static final String magicInitializationFile          = "deleteme.v45";
	public static final String tempXFileName                    = "deleteme.SNX";
//	public static final String tempXFileName                    = "deleteme.meX";
	public static final String magicInitializationContents = 
		"$BATCH()\n@FILEX                                                                                        TRTNO     RP     SQ     OP     CO\n"
		+	tempXFileName
		+ "                                                                                      1      0      0      0      0\n";

	// search and replace elements in the template X file
	public static final String soilPlaceholder                = "ssssssssss";
	public static final String initializationStartPlaceholder = "iiiiS";
	public static final String plantingDateStartPlaceholder   = "ppppS";
	public static final String plantingDateEndPlaceholder     = "ppppE";
	public static final String harvestingDatePlaceholder   = "hhhhh";
	public static final String randomSeedPlaceholder          = "rrrrr";
	public static final String nYearsOrRandomSeedsPlaceholder = "nnnnn";
	public static final String weatherPlaceholder             = "wwww";
	public static final String co2ppmPlaceholder              = "co2p";
	public static final String fertilizerPlaceholder          = "___place fertilizers here___";
	public static final String irrigationPlaceholder          = "___place irrigation here___";
	public static final String soilInitializationPlaceholder  = "___place initializations here___";

//	public static final int hardLimitOnReReads = 200;

//	public static final int hardLimitOnHappyReadingErrors = 20;

	public static final int nDaysInMonth =  30;
	public static final int nDaysInYear  = 365;

	// details for DSSAT's summary output files...
//	private static final int magicMissingValue = -99;
	private int magicDSSATSummaryLineIndexToRead = 4;
//	private int magicDSSATSummaryLineLength = 526; // 467; // 350;
	
	private int magicHarvestedWeightAtHarvestStartIndex = 162; //139; // character # 137, index 136
	private int magicHarvestedWeightAtHarvestEndIndex   = 170; //147; // character # 141, index 140

	private int magicStartingDateStartIndex =  93;// 86; // character # 137, index 136
	private int magicStartingDateEndIndex   = 100; // 93; // character # 141, index 140

	private int magicPlantingDateStartIndex = 101;// 86; // character # 137, index 136
	private int magicPlantingDateEndIndex   = 108; // 93; // character # 141, index 140

	private int magicEmergenceDateStartIndex = 108; 
	private int magicEmergenceDateEndIndex   = 116; 

	private int magicAnthesisDateStartIndex = 116; // character # 137, index 136
	private int magicAnthesisDateEndIndex   = 124; // character # 141, index 140

	private int magicMaturityDateStartIndex = 124; // character # 137, index 136
	private int magicMaturityDateEndIndex   = 132; // character # 141, index 140

	private int magicHarvestingDateStartIndex = 132; // character # 137, index 136
	private int magicHarvestingDateEndIndex   = 140; // character # 141, index 140


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
		"YPNUM", "NDCH", "TMAXA", "TMINA", "SRADA", "DAYLA", "CO2A", "PRCP", "ETCP"
	};
	private static final int[] extraStartIndices = new int[] {140,146,154,162,170,178,184,192,198,206,212,218,224,230,236,242,
		248,254,260,266,272,278,284,290,296,302,308,314,320,326,332,338,344,350,356,362,368,374,381,388,395,402,410,418,427,436,445,
		454,463,472,481,490,499,508,517,526,532,538,544,550,556,563,570};
	private static final int[] extraEndIndices   = new int[] {    146,154,162,170,178,184,192,198,206,212,218,224,230,236,242,
		248,254,260,266,272,278,284,290,296,302,308,314,320,326,332,338,344,350,356,362,368,374,381,388,395,402,410,418,427,436,445,
		454,463,472,481,490,499,508,517,526,532,538,544,550,556,563,570,577};
	
	
	
	// oldie moldies for wheat....
//	private int magicDSSATSummaryLineLength = 467; // 350;
	
	private int OLDmagicHarvestedWeightAtHarvestStartIndex = 139; //139; // character # 137, index 136
	private int OLDmagicHarvestedWeightAtHarvestEndIndex   = 147; //147; // character # 141, index 140

	private int OLDmagicStartingDateStartIndex =  78;// 86; // character # 137, index 136
	private int OLDmagicStartingDateEndIndex   = 85; // 93; // character # 141, index 140

	private int OLDmagicPlantingDateStartIndex = 86; // character # 137, index 136
	private int OLDmagicPlantingDateEndIndex   = 93; // character # 141, index 140

//	private int magicEmergenceDateStartIndex = 108; 
//	private int magicEmergenceDateEndIndex   = 116; 

	private int OLDmagicAnthesisDateStartIndex = 93; // character # 137, index 136
	private int OLDmagicAnthesisDateEndIndex   = 101; // character # 141, index 140

	private int OLDmagicMaturityDateStartIndex = 101; // character # 137, index 136
	private int OLDmagicMaturityDateEndIndex   = 109; // character # 141, index 140

	private int OLDmagicHarvestingDateStartIndex = 110; // character # 137, index 136
	private int OLDmagicHarvestingDateEndIndex   = 117; // character # 141, index 140

	private static final String[] OLDextraNames     = {"DWAP", "CWAM", "HWAM", "HWAH", "BWAH", "PWAM", "HWUM", "H#AM", "H#UM", "HIAM", "LAIX", "IR#M", "IRCM",
		"PRCM", "ETCM", "EPCM", "ESCM", "ROCM", "DRCM", "SWXM", "NI#M", "NICM", "NFXM", "NUCM", "NLCM", "NIAM", "CNAM", "GNAM", "PI#M", "PICM", "PUPC", "SPAM",
		"KI#M", "KICM", "KUPC", "SKAM", "RECM", "ONTAM", "ONAM", "OPTAM", "OPAM", "OCTAM", "OCAM", "DMPPM", "DMPEM", "DMPTM", "DMPIM", "YPPM", "YPEM", "YPTM", "YPIM",};
	private static final int[] OLDextraStartIndices = new int[] {118,123,131,139,147,155,161,169,175,183,189,195,202,207,214,219,225,231,237,244,249,255,
		261,267,273,279,285,291,297,303,309,315,321,327,333,339,345,351,358,365,372,379,387,395,404,413,422,431,440,449,458,};
	private static final int[] OLDextraEndIndices   = new int[] {    123,131,139,147,155,161,169,175,183,189,195,202,207,214,219,225,231,237,244,249,255,261,
		    267,273,279,285,291,297,303,309,315,321,327,333,339,345,351,358,365,372,379,387,395,404,413,422,431,440,449,458,467,};

	// crop categories.... each needs a unique id number...
	
	private static final String maizeString      = "maize";
	private static final String riceString       = "rice";
	private static final String wheatString      = "wheat";
	private static final String winterWheatString      = "winterwheat";
	private static final String soybeansString   = "soybeans";
	private static final String groundnutsString = "groundnuts";
	private static final String cottonString     = "cotton";
	private static final String potatoesString   = "potatoes";
	private static final String allAtPlantingString      = "allAtPlanting";
		
	
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
	private String irrigationSchemeToUse = null;
	private double fractionBetweenLowerLimitAndDrainedUpperLimit = Double.NEGATIVE_INFINITY;
//	private double nitrogenPPMforBothNH4NO2 = Double.POSITIVE_INFINITY;

	
//initFileContents += "nitrogenPPMforBothNH4NO2 for initializing soil nitrogen content" + "\n";
	private double depthForNitrogen = 3;
	private double residueNitrogenPercent = -4;
	private double incorporationRate = -5;
	private double incorporationDepth = -6;
	private String clayLoamSandStableCarbonRatesFilename = null;

	// out of order for the usual historical reasons
	private int    optionalHarvestInterval = 0;
	
	private double[] standardDepths          = null;
	private double[] clayStableFractionAbove = null;
	private double[] loamStableFractionAbove = null;
	private double[] sandStableFractionAbove = null;

	
	
	
	private String magicWeatherStationNameToUsePath = null;
	private String magicDSSATSummaryToReadPath      = null;
	private File   summaryDSSATOutputAsFileObject   = null;
	private File   errorAsFileObject   = null;
	private String magicInitializationFilePath      = null;
	
//	private int cropToUseInt = -3;
	
	private NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
	private IrrigationScheme irrigationScheme = null;

	
	private String[] dssatExecutionCommand = new String[3];

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
	
	private boolean badThingsHappened = false;
	

	public DSSATRunnerBigInits_debughappy() {
		
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
		initFileContents += "cropToUse for nitrogen fertilizer scheme" + "\n";
		initFileContents += "nHappyPlantRunsForPhenology" + "\n";
		initFileContents += "happyYieldThresholdToDoRealRuns" + "\n";
		initFileContents += "phenologyBufferInDays (positive means days AFTER happy plant phenology)" + "\n";
		initFileContents += "happyMaturityThresholdToDoRealRuns (days between planting and maturity; anything above this will be skipped)" + "\n";
		
		initFileContents += "cropToUse for irrigation scheme" + "\n";
		initFileContents += "fractionBetweenLowerLimitAndDrainedUpperLimit for initializing the soil moisture" + "\n";
//		initFileContents += "nitrogenPPMforBothNH4NO2 for initializing soil nitrogen content" + "\n";
		
		initFileContents += "depthForNitrogen: depth (cm) over which to distribute the initial soil nitrogen" + "\n";
		initFileContents += "residueNitrogenPercent: percent nitrogen content of initial crop residues left on the field" + "\n";
		initFileContents += "incorporationRate: rate of incorporation for the initial crop residues" + "\n";
		initFileContents += "incorporationDepth: depth of incorporation for the initial crop residues" + "\n";
		initFileContents += "clayLoamSandStableCarbonRatesFilename: path/filename for where to find a text file with values for stable carbon by layer for major soil textures" + "\n";
	
/* 
	public String makeInitializationAndSoilAnalysisBlock(
			String profileName,
			double fractionBetweenLowerLimitAndDrainedUpperLimit,
			String startingDateCode,
			double totalInitialNitrogenKgPerHa,
			double depthForNitrogen,
			double rootWeight,
			double surfaceResidueWeight,
			double residueNitrogenPercent,
			double incorporationRate,
			double incorporationDepth,
			double[] standardDepths,
			double[] clayStableFractionAbove,
			double[] loamStableFractionAbove,
			double[] sandStableFractionAbove)
			);
*/
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



	public void readInitFile(String filename) throws IOException, Exception {

		String[] initFileContents = FunTricks.readTextFileToArray(filename);

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
//		initFileContents += "nitrogenPPMforBothNH4NO2 for initializing soil nitrogen content" + "\n";
		
//		initFileContents += "depthForNitrogen: depth (cm) over which to distribute the initial soil nitrogen" + "\n";
//		initFileContents += "residueNitrogenPercent: percent nitrogen content of initial crop residues left on the field" + "\n";
//		initFileContents += "incorporationRate: rate of incorporation for the initial crop residues" + "\n";
//		initFileContents += "incorporationDepth: depth of incorporation for the initial crop residues" + "\n";
//		initFileContents += "clayLoamSandStableCarbonRatesFilename: path/filename for where to find a text file with values for stable carbon by layer for major soil textures" + "\n";

		
		
		
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

		magicWeatherStationNameToUsePath = pathToDSSATDirectory + magicWeatherStationNameToUse + ".CLI";
		magicDSSATSummaryToReadPath      = pathToDSSATDirectory + magicDSSATSummaryToRead;
		summaryDSSATOutputAsFileObject   = new File(magicDSSATSummaryToReadPath);
		errorAsFileObject                = new File(pathToDSSATDirectory + magicErrorFile);
		magicInitializationFilePath      = pathToDSSATDirectory + magicInitializationFile;

		// figure out the crop number to use so we can use switches instead of if/then...
		// i'm sure there's a better way, but i'm lame, so i'm gonna brute force it...
				
		if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.maizeString)) {
			nitrogenFertilizerScheme = new FSMaize();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.riceString)) {
			nitrogenFertilizerScheme = new FSRice();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.wheatString)) {
			nitrogenFertilizerScheme = new FSWheat();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.winterWheatString)) {
			nitrogenFertilizerScheme = new FSWinterWheat();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.soybeansString)) {
			nitrogenFertilizerScheme = new FSLegume();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.groundnutsString)) {
			nitrogenFertilizerScheme = new FSLegume();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.potatoesString)) {
			nitrogenFertilizerScheme = new FSPotatoes();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.allAtPlantingString)) {
			nitrogenFertilizerScheme = new FSAllAtPlanting();
		} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.cottonString)) {
			nitrogenFertilizerScheme = new FSCotton();
//			System.out.println("Need to define the fertilizer scheme for " + cropFertilizerSchemeToUse);
//			throw new Exception();
		} else {
			System.out.println("crop string [" + cropFertilizerSchemeToUse + "]" + " not in our list of supported crops; or at least, not implemented");
			throw new Exception();
		}

		if (irrigationSchemeToUse.equalsIgnoreCase(this.maizeString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.riceString)) {
			irrigationScheme = new IrriSRice();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.wheatString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.soybeansString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.groundnutsString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.potatoesString)) {
			irrigationScheme = new IrriSNone();
		} else if (irrigationSchemeToUse.equalsIgnoreCase(this.cottonString)) {
			irrigationScheme = new IrriSNone();
//			System.out.println("Need to define the irrigation scheme for " + irrigationSchemeToUse);
//			throw new Exception();
		} else {
			System.out.println("irrigation string [" + irrigationSchemeToUse + "]" + " not in our list of supported crops; assuming scheme NONE.");
			irrigationScheme = new IrriSNone();
		}


		// switching this to consider life as floats rather than as integers...
		extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraStartIndices.length];
		for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
			extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(true);
		}

		// set up the soil stable carbon content tables...
		;
		
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

		provenanceOut.print("nitrogenCropToUse:\t\t\t"              + cropFertilizerSchemeToUse + "\n");
		provenanceOut.print("nHappyPlantRunsForPhenology:\t"        + nHappyPlantRunsForPhenology + "\n");
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

		provenanceOut.print("\n");

		provenanceOut.print("--- Placeholder dictionary ---" + "\n");
		provenanceOut.print("soilPlaceholder =\t\t\t"            + soilPlaceholder + "\n");
		provenanceOut.print("initializationStartPlaceholder =\t" + initializationStartPlaceholder + "\n");
		provenanceOut.print("plantingDateStartPlaceholder =\t\t" + plantingDateStartPlaceholder + "\n");
		provenanceOut.print("plantingDateEndPlaceholder =\t\t"   + plantingDateEndPlaceholder + "\n");
		provenanceOut.print("randomSeedPlaceholder =\t\t\t"      + randomSeedPlaceholder + "\n");
		provenanceOut.print("weatherPlaceholder =\t\t\t"         + weatherPlaceholder + "\n");
		provenanceOut.print("co2ppm =\t\t\t\t"                   + co2ppmPlaceholder + "\n");
		provenanceOut.print("fertilizerPlaceholder =\t\t\t"      + fertilizerPlaceholder + "\n");
		provenanceOut.print("irrigationPlaceholder =\t\t\t"      + irrigationPlaceholder + "\n");
		
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

	
	private void grabUnifiedNewManyResults(int nYears, boolean useOldSummaryIndices, long lineIndex) throws InterruptedException, Exception {
			// declarations
			int plantingDate = -1;
			int startingDate = -2;
			int daysSincePlantingForEvent = -3;
			
			int yieldToUse = -4;
			int emergenceToUse = -3;
			int anthesisToUse = -3;
			int maturityToUse = -3;
			
			// choose the appropriate indices...
			int heremagicHarvestedWeightAtHarvestStartIndex,
			heremagicHarvestedWeightAtHarvestEndIndex,
			heremagicPlantingDateStartIndex,
			heremagicPlantingDateEndIndex, 
			heremagicAnthesisDateStartIndex,
			heremagicAnthesisDateEndIndex,
			heremagicMaturityDateStartIndex,
			heremagicMaturityDateEndIndex,
			
			heremagicStartingDateStartIndex,
			heremagicStartingDateEndIndex,
	
			heremagicHarvestingDateStartIndex,
			heremagicHarvestingDateEndIndex
	
			;
			
			int[] hereextraStartIndices,
			hereextraEndIndices
			;
	
			String[] hereextraNames;
	
	
			if (useOldSummaryIndices) {
				heremagicHarvestedWeightAtHarvestStartIndex = this.OLDmagicHarvestedWeightAtHarvestStartIndex;
				heremagicHarvestedWeightAtHarvestEndIndex = OLDmagicHarvestedWeightAtHarvestEndIndex;
				heremagicPlantingDateStartIndex = OLDmagicPlantingDateStartIndex;
				heremagicPlantingDateEndIndex = OLDmagicPlantingDateEndIndex;
				heremagicAnthesisDateStartIndex = OLDmagicAnthesisDateStartIndex; 
				heremagicAnthesisDateEndIndex = OLDmagicAnthesisDateEndIndex;
				heremagicMaturityDateStartIndex = OLDmagicMaturityDateStartIndex; 
				heremagicMaturityDateEndIndex = OLDmagicMaturityDateEndIndex; 
				heremagicStartingDateStartIndex = OLDmagicStartingDateStartIndex;
				heremagicStartingDateEndIndex = OLDmagicStartingDateEndIndex;
				heremagicHarvestingDateStartIndex = OLDmagicHarvestingDateStartIndex;
				heremagicHarvestingDateEndIndex = OLDmagicHarvestingDateEndIndex;
	
				hereextraStartIndices = OLDextraStartIndices; 
				hereextraEndIndices = OLDextraEndIndices;
				hereextraNames = this.OLDextraNames;
			} else {
				heremagicHarvestedWeightAtHarvestStartIndex = this.magicHarvestedWeightAtHarvestStartIndex;
				heremagicHarvestedWeightAtHarvestEndIndex = magicHarvestedWeightAtHarvestEndIndex;
				heremagicPlantingDateStartIndex = magicPlantingDateStartIndex;
				heremagicPlantingDateEndIndex = magicPlantingDateEndIndex;
				heremagicAnthesisDateStartIndex = magicAnthesisDateStartIndex; 
				heremagicAnthesisDateEndIndex = magicAnthesisDateEndIndex;
				heremagicMaturityDateStartIndex = magicMaturityDateStartIndex; 
				heremagicMaturityDateEndIndex = magicMaturityDateEndIndex; 
				heremagicStartingDateStartIndex = magicStartingDateStartIndex;
				heremagicStartingDateEndIndex = magicStartingDateEndIndex;
				heremagicHarvestingDateStartIndex = magicHarvestingDateStartIndex;
				heremagicHarvestingDateEndIndex = magicHarvestingDateEndIndex;
				
				hereextraNames = this.extraNames;
				
				hereextraStartIndices = extraStartIndices; 
				hereextraEndIndices = extraEndIndices;
			}
	
			double[] extractedValues = new double[hereextraStartIndices.length];
			boolean[] everythingIsValid = new boolean[hereextraStartIndices.length];
			boolean skipThisOne = false;
			
			
			// declarations with initializations
			int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
			String[] candidateSummaryContents = new String[nLinesToRead];
			String stringToParse = null;
	
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
				
			} catch (IOException ioe) {
				System.out.println("REAL: i/o exception...  ");
				throw ioe;
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				System.out.println("REAL: array index exception...");
				// carry on, since the file is still in the process of being written...
			}
	
		
			// parse the output file for the necessary goodies...
			for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
	
				skipThisOne = false;
				for (int validIndex = 0; validIndex < hereextraStartIndices.length; validIndex++) {
					everythingIsValid[validIndex] = true; // good unless decided otherwise
				}
				
				try {
					// yield
					yieldToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					     					                                          .substring(heremagicHarvestedWeightAtHarvestStartIndex,
					     					                                          		heremagicHarvestedWeightAtHarvestEndIndex).trim());
					// planting / anthesis / maturity dates
					if (!useOldSummaryIndices) {
						emergenceToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
									                                                      .substring(magicEmergenceDateStartIndex,
									                                                      		magicEmergenceDateEndIndex).trim());;
					} else {
						emergenceToUse = -1;
					}
					startingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					                                                         .substring(heremagicStartingDateStartIndex,
					                                                        		 heremagicStartingDateEndIndex).trim());
					plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					                                                         .substring(heremagicPlantingDateStartIndex,
					                                                        		 heremagicPlantingDateEndIndex).trim());
					anthesisToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
							                                                      .substring(heremagicAnthesisDateStartIndex,
							                                                      		heremagicAnthesisDateEndIndex).trim());;
					maturityToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
							                                                      .substring(heremagicMaturityDateStartIndex,
							                                                      		heremagicMaturityDateEndIndex).trim());
	
					if (maturityToUse < 0) {
						maturityToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
								                                                      .substring(heremagicHarvestingDateStartIndex,
								                                                      		heremagicHarvestingDateEndIndex).trim());
					}
	
	
					// all the other goodies...
					for (int extraIndex = 0; extraIndex < hereextraStartIndices.length; extraIndex++) {
						stringToParse = candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                                   .substring(hereextraStartIndices[extraIndex],
	                                                   		hereextraEndIndices[extraIndex]).trim();
						// check if there are the silly stars in there...
						if (stringToParse.contains("*")) {
							// ok, let's try this again, but we'll just skip over the offending value but keep the others from this line...
							everythingIsValid[extraIndex] = false;
							System.out.println("REAL: got ***'s in #" + extraIndex + " " + hereextraNames[extraIndex] + ": [" + stringToParse + "]");
							realBadThingsCountsEntirePixel.useLongValue(1); // assess that something bad happened...
	
							
							/*
							if (false) {
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
	//							System.out.println("anthesis  [" + 
	//							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	//							.substring(magicAnthesisDateStartIndex,
	//							magicAnthesisDateEndIndex).trim()
	//							+ "]");
								System.out.println("maturity  [" + 
										candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
										                         .substring(magicMaturityDateStartIndex,
										                        		 magicMaturityDateEndIndex).trim()
										                        		 + "]");
	
								// all the other goodies...
								for (int extraExtraIndex = 0; extraExtraIndex < extraStartIndices.length; extraExtraIndex++) {
	
									System.out.println("extras [" + extraExtraIndex + "] = [" +
											candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
											                         .substring(extraStartIndices[extraExtraIndex],
											                        		 extraEndIndices[extraExtraIndex]).trim()
											                        		 + "]");
	
								}
								
								throw new Exception();
							}
								*/
						}
						if (everythingIsValid[extraIndex]) {
							extractedValues[extraIndex] = Double.parseDouble(stringToParse);
						}
	
					}
	
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("REAL: had trouble reading one of the following as an integer:");
					System.out.println("yield [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                      .substring(heremagicHarvestedWeightAtHarvestStartIndex,
	                                      		heremagicHarvestedWeightAtHarvestEndIndex).trim()
							+ "]");
					System.out.println("planting  [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                     .substring(heremagicPlantingDateStartIndex,
	                                    		 heremagicPlantingDateEndIndex).trim()
							+ "]");
	//				System.out.println("anthesis  [" + 
	//						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	//                                     .substring(magicAnthesisDateStartIndex,
	//                                     		magicAnthesisDateEndIndex).trim()
	//            + "]");
					System.out.println("maturity  [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                     .substring(heremagicMaturityDateStartIndex,
	                                    		 heremagicMaturityDateEndIndex).trim()
	            + "]");
					
					// all the other goodies...
					for (int extraIndex = 0; extraIndex < hereextraStartIndices.length; extraIndex++) {
						
						System.out.println("extras [" + extraIndex + "] = [" +
						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
						                                                      .substring(hereextraStartIndices[extraIndex],
						                                                      		hereextraEndIndices[extraIndex]).trim()
						                                                      		+ "]");
					}
	
					skipThisOne = true;
	//				throw new Exception();
					System.out.println("     -> something bad happened with the essentials, skipping this line (index = " + lineIndex + ")... <-");
				} catch (NullPointerException npe) {
					// newly added on 19mar12 to try to deal with sorghum's funny business where sometimes it seems like
					// it gets positive yield with zero units, so the per-unit-size goes undefined....
					System.out.println("   fakeYearIndex = [" + fakeYearIndex + "]; magicDSSATSummaryLineIndexToRead = [" + magicDSSATSummaryLineIndexToRead + "]");
					System.out.println("   a null pointer exception came up when trying to read the REAL summary: guess is: ["
							+ candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] + "]");
					System.out.println("   skipping this line (index = " + lineIndex + ")...");
					skipThisOne = true;
				}
				
				// skip this one if the yield is missing
				if (!skipThisOne) {
	
					// yield
					if (yieldToUse > 0) {
						realYieldsEntirePixel.useLongValue(yieldToUse);
					}
	
					// time to planting. since we may be using automatic planting, let's keep track of the
					// time between the simulation start and the actual planting date when the conditions
					// are met.
					if (plantingDate > 0) {
						daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(startingDate, plantingDate);
						realTimeToPlantingEntirePixel.useLongValue(daysSincePlantingForEvent);
					} else {
						realNoPlantingEntirePixel.useLongValue(1); // assess a problem...
					}
	
					// emergence
					// we have to check whether emergence occurred...
					// Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this
					// for the moment...
					if (emergenceToUse > 0 && plantingDate > 0) {
						daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, emergenceToUse);
						realEmergenceEntirePixel.useLongValue(daysSincePlantingForEvent);
					} else {
	//					System.out.println("grab real results: emergence failed...; not counting in average");
						realBadThingsCountsEntirePixel.useLongValue(1); // assess a problem...
					}
	
					// anthesis
					// we have to check whether flowering occurred...
					// Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this
					// for the moment...
					if (anthesisToUse > 0 && plantingDate > 0) {
						daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, anthesisToUse);
						realAnthesisEntirePixel.useLongValue(daysSincePlantingForEvent);
					} else {
	//					System.out.println("grab real results: flowering failed...; not counting in average");
						realBadThingsCountsEntirePixel.useLongValue(1); // assess a problem...
					}
	
					// maturity
					// if it's still missing..., skip over it and record as something bad happened
					if (maturityToUse > 0 && plantingDate > 0) {
	//					if (maturityToUse != magicMissingValue && plantingDate > 0) {
						daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, maturityToUse);
						if (daysSincePlantingForEvent > 300) {
//							System.out.println("pd = " + plantingDate + " ; mat = " + maturityToUse + " ; diff = " + daysSincePlantingForEvent);
//							System.out.println("   lm = " + daysSincePlantingForEvent);
						}
						if (daysSincePlantingForEvent > 0) {
							realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
						} else {
							System.out.print("grab real results: strange maturity; ");
							System.out.println("pd = " + plantingDate + " ; mat = " + maturityToUse + " ; diff = " + daysSincePlantingForEvent);
						}
					} else {
//						System.out.println("grab real results: maturity failed...; not counting in maturity average");
//						System.out.println("  mf");
						realBadThingsCountsEntirePixel.useLongValue(10000); // assess a problem...
					}
	
	
					// all the extra bits
					for (int extraIndex = 0; extraIndex < hereextraStartIndices.length; extraIndex++) {
						if (everythingIsValid[extraIndex]) {
							extraSummaryAccumulators[extraIndex].useDoubleValue(extractedValues[extraIndex]);
						}
					}
					
					
				} // end if everything is valid, do the recording...
				
			} // end for fakeYears...
			
		}




	private int[] grabUnifiedHappyResults(int nYears, boolean useOldSummaryIndices) throws InterruptedException, Exception {
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
	
			// choose the appropriate indices...
			int heremagicHarvestedWeightAtHarvestStartIndex,
			heremagicHarvestedWeightAtHarvestEndIndex,
			heremagicPlantingDateStartIndex,
			heremagicPlantingDateEndIndex, 
			heremagicAnthesisDateStartIndex,
			heremagicAnthesisDateEndIndex,
			heremagicMaturityDateStartIndex,
			heremagicMaturityDateEndIndex
			;
			
			if (useOldSummaryIndices) {
				heremagicHarvestedWeightAtHarvestStartIndex = this.OLDmagicHarvestedWeightAtHarvestStartIndex;
				heremagicHarvestedWeightAtHarvestEndIndex = OLDmagicHarvestedWeightAtHarvestEndIndex;
				heremagicPlantingDateStartIndex = OLDmagicPlantingDateStartIndex;
				heremagicPlantingDateEndIndex = OLDmagicPlantingDateEndIndex;
				heremagicAnthesisDateStartIndex = OLDmagicAnthesisDateStartIndex; 
				heremagicAnthesisDateEndIndex = OLDmagicAnthesisDateEndIndex;
				heremagicMaturityDateStartIndex = OLDmagicMaturityDateStartIndex; 
				heremagicMaturityDateEndIndex = OLDmagicMaturityDateEndIndex; 
			} else {
				heremagicHarvestedWeightAtHarvestStartIndex = this.magicHarvestedWeightAtHarvestStartIndex;
				heremagicHarvestedWeightAtHarvestEndIndex = magicHarvestedWeightAtHarvestEndIndex;
				heremagicPlantingDateStartIndex = magicPlantingDateStartIndex;
				heremagicPlantingDateEndIndex = magicPlantingDateEndIndex;
				heremagicAnthesisDateStartIndex = magicAnthesisDateStartIndex; 
				heremagicAnthesisDateEndIndex = magicAnthesisDateEndIndex;
				heremagicMaturityDateStartIndex = magicMaturityDateStartIndex; 
				heremagicMaturityDateEndIndex = magicMaturityDateEndIndex; 
			}
		
			
			
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
	//		DescriptiveStatisticsUtility happyEmergenceDates = new DescriptiveStatisticsUtility(false);
			DescriptiveStatisticsUtility happyAnthesisDates = new DescriptiveStatisticsUtility(false);
			DescriptiveStatisticsUtility happyMaturityDates = new DescriptiveStatisticsUtility(false);
	
			for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
				if (candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] == null) {
					System.out.println("Happy: funny business; skip and hope for the best");
					for (int fYI = 0; fYI < nYears; fYI++) {
						System.out.println(fYI + " -> [" + candidateSummaryContents[fYI + magicDSSATSummaryLineIndexToRead] + "]");
					}
//					throw new NullPointerException();
					continue;
				}
				everythingIsValid = true;
				try {
					// yield
					yieldToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					     					                                          .substring(heremagicHarvestedWeightAtHarvestStartIndex,
					     					                                          		heremagicHarvestedWeightAtHarvestEndIndex).trim());
					// planting / anthesis / maturity dates
					plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
					                                                         .substring(heremagicPlantingDateStartIndex,
					                                                        		 heremagicPlantingDateEndIndex).trim());
	//				emergenceToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	//						                                                      .substring(magicEmergenceDateStartIndex,
	//						                                                      		magicEmergenceDateEndIndex).trim());;
					anthesisToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
							                                                      .substring(heremagicAnthesisDateStartIndex,
							                                                      		heremagicAnthesisDateEndIndex).trim());;
					maturityToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
							                                                      .substring(heremagicMaturityDateStartIndex,
							                                                      		heremagicMaturityDateEndIndex).trim());
	
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.out.println("HAPPY: had trouble reading one of the following as an integer:");
					System.out.println("yield [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                      .substring(heremagicHarvestedWeightAtHarvestStartIndex,
	                                      		heremagicHarvestedWeightAtHarvestEndIndex).trim()
							+ "]");
					System.out.println("planting  [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                     .substring(heremagicPlantingDateStartIndex,
	                                    		 heremagicPlantingDateEndIndex).trim()
							+ "]");
					System.out.println("anthesis  [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                     .substring(heremagicAnthesisDateStartIndex,
	                                    		 heremagicAnthesisDateEndIndex).trim()
	            + "]");
					System.out.println("maturity  [" + 
							candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
	                                     .substring(heremagicMaturityDateStartIndex,
	                                    		 heremagicMaturityDateEndIndex).trim()
	            + "]");
					everythingIsValid = false;
				}
	
				if (everythingIsValid) {
				// yield
					if (yieldToUse > 0) {
						happyYields.useLongValue(yieldToUse);
					} else {
						// i think we should force it to zero
						// Beware the MAGIC NUMBER!!! mapping "no results" yields to zero
						happyYields.useLongValue(0L);
					}
					
					System.out.println("\tin happy reader: yieldToUse = " + yieldToUse + "; current happyYields mean: " + happyYields.getMean());
				
				// planting date
				plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
				                                                         .substring(heremagicPlantingDateStartIndex,
				                                                        		 heremagicPlantingDateEndIndex).trim());
				
	//			// emergence
	//			eventDate = emergenceToUse;
	//			daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
	//			happyEmergenceDates.useLongValue(daysSincePlantingForEvent);
	
				
				// anthesis
				eventDate = anthesisToUse;
				daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
				happyAnthesisDates.useLongValue(daysSincePlantingForEvent);
	
				// pull out the bits we want
				// maturity
				eventDate = maturityToUse;
				if (maturityToUse > 0 && plantingDate > 0) {
					daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
					if (daysSincePlantingForEvent > 0) {
						happyMaturityDates.useLongValue(daysSincePlantingForEvent);
					} 
				}
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


	
	
	


	
	
	
	
	
////////
	
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
	

	
	
	public void doSimulationsUniqueSeedsAllClimateSupplied(boolean useOldSummaryIndices) throws Exception {

		//////////////////////////////////////////////////
		// check if everything has been set up properly //
		//////////////////////////////////////////////////
		if (!this.readyToRun) {
			System.out.println("DSSATRunner not ready to run... exiting...");
			return;
		}

		// set up a timer for the fun of it...
		TimerUtility thisTimer = new TimerUtility();
		TimerUtility happyTimer = new TimerUtility();
		TimerUtility realTimer = new TimerUtility();

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


		DescriptiveStatisticsUtility happyTimerStats = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility realTimerStats  = new DescriptiveStatisticsUtility(true);


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
		int harvestingDayForThisWindow = -18;
		int harvestingYearForThisWindow = -17;

		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;
		String harvestDayCode  = null;
		String fertilizerBlock  = null;
		String irrigationBlock  = null;

//		String randomSeedCode = null;
		String nYearsCode = null;
		String nHappyYearsCode = null;
		String co2ppmCode = null;

		String soilTypeString = null;

		int[] phenologyInDays = null;

		String statisticsOutLine = null;

		double totalInitialNitrogenKgPerHa = -5, rootWeight = -6, surfaceResidueWeight = -7;

		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		String XHappyStuffToWrite = null;
		String initializationBlock = null;


		////////////////////////////
		// a couple magic numbers //
		////////////////////////////

		long nitrogenKgPerHaCol      = 70;
		long rootWeightCol           = 71; // old = 72; noticed on 18jul12
		long surfaceResidueWeightCol = 72; // old = 71; noticed on 18jul12


		////////////////////////////////////////
		// set up stuff that we actually know //
		////////////////////////////////////////

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
		// Beware the MAGIC NUMBER!!! gonna force these into memory.
		int formatIndexToForce = 1;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);

		if (geogMatrix.getDimensions()[1] != 4) {
			System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
			throw new Exception();
		}

		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];



		// since this implementation (using the multiple years with a single random seed, rather
		// than multiple random seeds with a single year) has the seeds and years as invariants, do
		// them up front to save on a little search and replace overhead...
//		randomSeedCode  = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
//		nYearsCode      = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
//		nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
		// Beware the MAGIC NUMBER!!! since we will try to use a bunch of single years, we need to
		// set it up as putting "1" in the number of years place... this will go inside a for
		// loop over random seeds to do the desired number of years...
		nYearsCode      = DSSATHelperMethods.padWithZeros(1, 5);
		nHappyYearsCode = DSSATHelperMethods.padWithZeros(1, 5);
		co2ppmCode      = DSSATHelperMethods.padWithZeros(co2ppm, 4);

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
		String[] invariantsReplaced = this.buildUniqueInvariantsXTemplates(templateXFileContents, this.randomSeedPlaceholder, nHappyYearsCode, co2ppmCode, dummyScheme, nYearsCode);

		String randomSeedActualValue = null;
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
		Process theRunningProcess = null;
		InputStream theRunningErrorStream = null;
		InputStream theRunningInputStream = null;
		OutputStream theRunningOutputStream = null;

		System.out.println("-- starting through data --");
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

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
			for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
				extraSummaryAccumulators[extraIndex].reset();
			}


			// figure out the climate file...
			cliStuffToWrite = DSSATHelperMethods.cliFileContentsAllSupplied(dataMatrix, geogMatrix, lineIndex, SWmultiplier);
			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, lineIndex);

			FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);

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

				// pick the harvesting day/etc for this window
				harvestingDayForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing + optionalHarvestInterval ;
				harvestingYearForThisWindow = fakePlantingYear;
				
				// take care of the possibility that we will go before the beginning of this year
				while (harvestingDayForThisWindow > nDaysInYear) {
					harvestingDayForThisWindow -= nDaysInYear;
					harvestingYearForThisWindow++;
				}
				
				
				
				// format everything properly....
				startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
						+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
				endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
						+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
				initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
						+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd
				harvestDayCode         = (DSSATHelperMethods.pad2CharactersZeros(harvestingYearForThisWindow) 
						+ DSSATHelperMethods.pad3CharactersZeros(harvestingDayForThisWindow)); // YYddd

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



				for (int happyPlantRunIndex = 0; happyPlantRunIndex < this.nHappyPlantRunsForPhenology; happyPlantRunIndex++) {

					XHappyStuffToWrite = XHappyInvariantsReplaced.replaceAll(this.soilInitializationPlaceholder, dummyInitializationBlock);

					// do the search and replace thing; the invariants have already been done above...
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(soilPlaceholder               , soilTypeString);
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(harvestingDatePlaceholder     , harvestDayCode);

					// put in the random seed to use

//					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(this.randomSeedPlaceholder,Integer.toString(this.firstRandomSeed + happyPlantRunIndex));
					randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed + happyPlantRunIndex,5);
					if (randomSeedActualValue.length() != 5) {
						System.out.println("fRS = " + firstRandomSeed + " ; hPRI = " + happyPlantRunIndex + " ; val = [" + randomSeedActualValue + "]");
						throw new Exception();
					}

//					System.out.println("-------happy before " + happyPlantRunIndex + "-------");
//					System.out.println(XHappyStuffToWrite);
//					System.out.println("-------------------");

					
					XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(this.randomSeedPlaceholder,
							randomSeedActualValue);

					FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);

//					System.out.println("-------happy after " + happyPlantRunIndex + "-------");
//					System.out.println(XHappyStuffToWrite);
//					System.out.println("-------------------");
					

					// run DSSAT with the happy plant
					happyTimer.tic();

					theRunningProcess = Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

					// wait for it to finish up
					theRunningProcess.waitFor();
					happyTimerStats.useDoubleValue(happyTimer.tocMillis());

					//////////////////////////////////////////
					// extract the results & store to array //
					//////////////////////////////////////////

					// Beware the MAGIC NUMBER!!! with unique seeds, just look at a single year...
					// we are running a single year...
					// mean yield / anthesis / maturity in days after planting...
//					if (useOldSummaryIndices) {
//						phenologyInDays = this.grabOLDHappyResults(1); // nHappyPlantRunsForPhenology);
//					} else {
//						phenologyInDays = this.grabNewHappyResults(1); // nHappyPlantRunsForPhenology);
//					}
					
					phenologyInDays = this.grabUnifiedHappyResults(1, useOldSummaryIndices);
					// keep track of the happy plant yields for the fun of it...
					happyYieldsEntirePixel.useLongValue(phenologyInDays[0]);
					happyMaturityEntirePixel.useLongValue(phenologyInDays[2]);
					
					System.out.println("\t\toutside: this yield = " + phenologyInDays[0] + "; running average = " + happyYieldsEntirePixel.getMean() + " ; hPRI = " + happyPlantRunIndex + " ; val = [" + randomSeedActualValue + "]");
					
				}



				// proceed, if the happy yield exceeds some theshold and the effective growing season isn't too long....
				if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {

					for (int normalPlantRunIndex = 0; normalPlantRunIndex < this.nRandomSeedsToUse; normalPlantRunIndex++) {

						/////////////////////
						// make the X file //
						/////////////////////

						// create the fertilizer block...
						fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
								phenologyInDays[1] + phenologyBufferInDays, phenologyInDays[2] + phenologyBufferInDays, nitrogenLevel
						);

//						irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), nRandomSeedsToUse);
						// Beware the MAGIC NUMBER!!! we only care about a single season here...
						irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), 1);
						irrigationBlock = irrigationScheme.buildIrrigationBlock();


						// do the search and replace thing; the invariants have already been done above...
						XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
						// put in the random seed to use
//						XStuffToWrite = XStuffToWrite.replaceAll(this.randomSeedPlaceholder,Integer.toString(firstRandomSeed + normalPlantRunIndex));

						randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed + normalPlantRunIndex,5);
						if (randomSeedActualValue.length() != 5) {
							System.out.println("fRS = " + firstRandomSeed + " ; nPRI = " + normalPlantRunIndex + " ; val = [" + randomSeedActualValue + "]");
							throw new Exception();
						}

						XStuffToWrite = XStuffToWrite.replaceAll(this.randomSeedPlaceholder,randomSeedActualValue);
						XStuffToWrite =       XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
						XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
						XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
						XStuffToWrite =       XStuffToWrite.replaceAll(harvestingDatePlaceholder     , harvestDayCode);
						XStuffToWrite =       XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);
						XStuffToWrite =       XStuffToWrite.replaceAll(irrigationPlaceholder, irrigationBlock);

						// pull the values from the data matrix
						// Beware the MAGIC NUMBER!!! the columns in the data file corresponding to the various initial conditions
						totalInitialNitrogenKgPerHa = dataMatrix.getValue(lineIndex,nitrogenKgPerHaCol);
						rootWeight = dataMatrix.getValue(lineIndex,rootWeightCol);
						surfaceResidueWeight = dataMatrix.getValue(lineIndex,surfaceResidueWeightCol);

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
// this should have already been done above...
//						XStuffToWrite = XStuffToWrite.replaceAll(this.randomSeedPlaceholder,Integer.toString(this.firstRandomSeed + normalPlantRunIndex));

						// overwrite the old file with the new contents

						FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);

//						System.out.println("-------real " + normalPlantRunIndex + " --------");
//						System.out.println(XStuffToWrite);
//						System.out.println("-------------------");

						// recommend garbage collection
						System.gc();

						///////////////
						// run DSSAT //
						///////////////

						realTimer.tic();

						theRunningProcess = Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);

						// wait for it to finish up
						theRunningProcess.waitFor();
						realTimerStats.useDoubleValue(realTimer.tocMillis());

						//////////////////////////////////////////
						// extract the results & store to array //
						//////////////////////////////////////////

//						if(useOldSummaryIndices) {
//							this.grabOLDManyResults(this.nRandomSeedsToUse);
//						} else {
//							// if we are doing unique seeds, then we just grab a single year...
//							// Beware the MAGIC NUMBER!!! only look at a single year
//							this.grabNewManyResults(1);
//						}
						this.grabUnifiedNewManyResults(1, useOldSummaryIndices, lineIndex);


						// make sure the streams got closed for the process after we managed to read everything...
						theRunningErrorStream = theRunningProcess.getErrorStream();
						theRunningErrorStream.close();
						theRunningInputStream = theRunningProcess.getInputStream();
						theRunningInputStream.close();
						theRunningOutputStream = theRunningProcess.getOutputStream();
						theRunningOutputStream.close();
						theRunningProcess.destroy();

						// bail out if we have already encountered bad things for this pixel...
						if (badThingsHappened) {
							break;
						}

					} // end for loop over random seeds....

				} // end if happy yield threshold is met
				else {
					// actually, if we keep the zeros, it screws up our average yields and stds (duh)
					// so, we're just going to skip it. 
				} // end of else concerning happy yield threshold being met...

			} // end plantingWindowIndex


			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////


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
					+ happyYieldsEntirePixel.getMean() + delimiter
					+ happyMaturityEntirePixel.getMean();

				if (useOldSummaryIndices) {
					for (int extraIndex = 0; extraIndex < OLDextraStartIndices.length; extraIndex++) {
						statisticsOutLine += delimiter + 0;
					}
				} else {
					for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
						statisticsOutLine += delimiter + 0;
					}
				}
				
//				statisticsOutLine += delimiter + 0 + delimiter + (-1);
				statisticsOutLine += delimiter + (-this.spinUpTimeDays) + delimiter + (-1);



			} else {

				statisticsOutLine = 
					realBadThingsCountsEntirePixel.getN() + delimiter
					+ realYieldsEntirePixel.getMinAsLong() + delimiter 
					+ realYieldsEntirePixel.getMaxAsLong() + delimiter 
					+ realYieldsEntirePixel.getMean() + delimiter
					+ realYieldsEntirePixel.getStd() + delimiter
					+ realEmergenceEntirePixel.getMean() + delimiter
					+ realEmergenceEntirePixel.getStd() + delimiter
					+ realAnthesisEntirePixel.getMean() + delimiter
					+ realAnthesisEntirePixel.getStd() + delimiter
					+ realMaturityEntirePixel.getMean() + delimiter
					+ realMaturityEntirePixel.getStd() + delimiter
					+ happyYieldsEntirePixel.getMean() + delimiter
					+ happyMaturityEntirePixel.getMean();

				if (useOldSummaryIndices) {
					for (int extraIndex = 0; extraIndex < OLDextraStartIndices.length; extraIndex++) {
						statisticsOutLine += delimiter + extraSummaryAccumulators[extraIndex].getMean();
					}
				} else {
					for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
						statisticsOutLine += delimiter + extraSummaryAccumulators[extraIndex].getMean();
					}
				}

//				statisticsOutLine += delimiter + realTimeToPlantingEntirePixel.getMean()
				statisticsOutLine += delimiter + (realTimeToPlantingEntirePixel.getMean() - this.spinUpTimeDays)
				+ delimiter + realNoPlantingEntirePixel.getN();
			}

			statisticsOutLine += "\n";
			statisticsOut.print(statisticsOutLine);
//			statisticsOut.flush();

			// now we're ready to deal with the next pixel...
			// Beware the MAGIC NUMBER!!! checking every one percent...

			if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {

				timeSinceStart = thisTimer.sinceStartSeconds();
//				averageTimePerEvent = timeForWeather.getMean() + timeForORYZA.getMean();
				projectedTime = timeSinceStart / (lineIndex + 1) * nLinesInDataFile;
				timeRemaining = projectedTime - timeSinceStart;

				System.out.println("prog: " + (lineIndex + 1) + "/" + nLinesInDataFile + " = " +
						FunTricks.fitInNCharacters((100 * (lineIndex + 1.0)/nLinesInDataFile),5) +
						"\tave R: " + FunTricks.fitInNCharacters(realTimerStats.getMean(),6) + 
						" ave H: " + FunTricks.fitInNCharacters(happyTimerStats.getMean(),6) +
						" N/P/R = " + FunTricks.fitInNCharacters(timeSinceStart,6) +
						"/" + FunTricks.fitInNCharacters(projectedTime,6) +
						"/" + FunTricks.fitInNCharacters(timeRemaining,6) +
						"\t" + FunTricks.fitInNCharacters(timeRemaining / 60, 6)
				);
			}

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
		
		if (useOldSummaryIndices) {
			for (int extraIndex = 0; extraIndex < OLDextraStartIndices.length; extraIndex++) {
				columnList += delimiter + this.OLDextraNames[extraIndex];
			}
		} else {
			for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
				columnList += delimiter + this.extraNames[extraIndex];
			}
		}

		columnList += delimiter + "time_to_planting" + delimiter + "n_no_planting";
		columnList += "\n";


		FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

		long nRows = nLinesInDataFile;

		// Beware the MAGIC NUMBER!!!
		int nCols = -5;
		if (useOldSummaryIndices) {
			nCols = 13 + OLDextraStartIndices.length + 2; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		} else {
			nCols = 13 + extraStartIndices.length + 2; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		}
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);



		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
		thisTimer.sinceStartMessage("running DSSAT");
		double overallTimeMillis = thisTimer.sinceStartMillis();
		double totalDSSATTimeMillis = thisTimer.tocMillis();



		System.out.println(" total time in DSSAT loop = " + totalDSSATTimeMillis / 1000 / 60 + "min ; per run average = " 
				+ totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms");
		System.out.println("overall per run average = " +
				overallTimeMillis/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"	);


	} // main
	
	
////////
		
	



	public void placeholder() {
		return;
	}
	
	
}

