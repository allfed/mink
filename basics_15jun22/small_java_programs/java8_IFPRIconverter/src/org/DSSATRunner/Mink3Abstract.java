package org.DSSATRunner;

import java.io.File;

import org.R2Useful.DescriptiveStatisticsUtility;

public interface Mink3Abstract {

	boolean readyToRun = false;


	/////////////////////////////////////
	// create a bunch of magic numbers //
	/////////////////////////////////////
	
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
	static final int totalLengthForSoilType = 10;

	static final int nDaysInMonth =  30;
	static final int nDaysInYear  = 365;

	static final int firstWeatherYear = 0; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly
	// details for DSSAT's summary output files...
//	private static final int magicMissingValue = -99;
	int magicDSSATSummaryLineIndexToRead = 4;
	
	static final int magicHarvestedWeightAtHarvestOutputIndex = 20;
	static final int magicStartingDateOutputIndex             = 11;
	static final int magicPlantingDateOutputIndex             = 12;
	static final int magicEmergenceDateOutputIndex            = 13;
	static final int magicAnthesisDateOutputIndex             = 14;
	static final int magicMaturityDateOutputIndex             = 15;
	static final int magicHarvestingDateOutputIndex           = 16;
	
	static final int magicOffsetForExtraNamesOutputIndex      = 17;
	

	// latest 4.5 beta...
	int magicHWAHindex = 3;
	
	// adding a couple more at the end that were put into "real" version 4.6
	static final String[] extraNames     = {
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
	
	static int magicNumberOfOutputsExpected = magicOffsetForExtraNamesOutputIndex + extraNames.length;
	
		
	// crop categories.... each needs a unique id number...
	
	static final String maizeString      = "maize";
	static final String riceString       = "rice";
	static final String unfloodedRiceString       = "unfloodedrice";
	static final String wheatString      = "wheat";
	static final String winterWheatString      = "winterwheat";
	static final String soybeansString   = "soybeans";
	static final String groundnutsString = "groundnuts";
	static final String cottonString     = "cotton";
	static final String potatoesString   = "potatoes";
	static final String potatoes2String   = "potatoes2";
	static final String sorghumString   = "sorghum";
	static final String cassavaString   = "cassava";
	
	static final String chickpeas = "chickpeas";
	
	static final String allAtPlantingString      = "allAtPlanting";
	static final String threeSplitWithFloweringString      = "threeSplitWithFlowering";
		
	
	/////////////////////////////////////////////////////
	// other variables which are good to have sharable //
	/////////////////////////////////////////////////////

	int maxRunTime = 5000; // MAGIC at the moment
	double bumpUpMultiplier = 2; // MAGIC at the moment
	int testIntervalToUse = 100; // MAGIC at the moment
	int rerunAttemptsMax = 4; // MAGIC at the moment

	
	
	String initFileName         = null;

	String  gisTableBaseName    = null;
	String  templateXFile       = null;
	String  yieldOutputBaseName = null;
//	private boolean allFlag             = false;

	static final String pathToDSSATDirectory     = "./";
	File   pathToDSSATDirectoryAsFile = null;
	String nameOfDSSATExecutable    = null;
	String nameOfWeatherExecutable  = null;
	double SWmultiplier             = Double.NaN;
	int    firstRandomSeed          = -1;
	int    nFakeYears        = -2;
	String magicSoilPrefix          = null;
	int    fakePlantingYear = -1; 
	int    spinUpTimeDays           = -4;
	int    nPlantingWindowsPerMonth = -5;
	int    plantingWindowLengthDays = -6;
	int    co2ppm    = -5;
	String cropFertilizerSchemeToUse = null;
	int    nHappyPlantRunsForPhenology = -3;
	int    happyYieldThresholdToDoRealRuns = 0;
	int    phenologyBufferInDays = 0;
	int    happyMaturityThresholdToDoRealRuns = 0;
	String irrigationSchemeToUse = null;
	double fractionBetweenLowerLimitAndDrainedUpperLimit = Double.NEGATIVE_INFINITY;

	
	double depthForNitrogen = 3;
	double residueNitrogenPercent = -4;
	double incorporationRate = -5;
	double incorporationDepth = -6;
	String clayLoamSandStableCarbonRatesFilename = null;

	// out of order for the usual historical reasons
	int    optionalHarvestInterval = 0;
	
	double[] standardDepths          = null;
	double[] clayStableFractionAbove = null;
	double[] loamStableFractionAbove = null;
	double[] sandStableFractionAbove = null;

	
	
	
	String magicWeatherStationNameToUsePath = null;
	String magicDSSATSummaryToReadPath      = null;
	File   summaryDSSATOutputAsFileObject   = null;
	File   errorAsFileObject   = null;
	String magicInitializationFilePath      = null;
	
//	private int cropToUseInt = -3;
	
	NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
	IrrigationScheme irrigationScheme = null;

	
	String[] dssatExecutionCommand = new String[3];
	String[] weatherExecutionCommand = new String[7];

	///////////////////////////////////////////////////////////////
	// special variables for the purpose of grabbing the results //
	///////////////////////////////////////////////////////////////
	// initialize the place to store the yields...

	DescriptiveStatisticsUtility realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);

	DescriptiveStatisticsUtility realTimeToPlantingEntirePixel = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility realNoPlantingEntirePixel = new DescriptiveStatisticsUtility(false);

	DescriptiveStatisticsUtility realEmergenceEntirePixel = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility realAnthesisEntirePixel = new DescriptiveStatisticsUtility(false);

	DescriptiveStatisticsUtility realMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

	DescriptiveStatisticsUtility realBadThingsCountsEntirePixel = new DescriptiveStatisticsUtility(false);

	DescriptiveStatisticsUtility[] extraSummaryAccumulators = null;
	
	boolean badThingsHappened = false;

	
	
	

}