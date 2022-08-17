package org.DSSATRunner;

import java.io.*;
import java.util.Date;

import org.R2Useful.*;

public class Mink3p2classic {

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
    // originally, we "deleteme.SNX" here, but this failed when trying to use CROPSIM wheat; now, we will try "./deleteme.SNX"
    // and hopefully, that will be sufficiently robust for a while
    //    public static final String tempXFileName                    = "deleteme.SNX";
    public static final String tempXFileName                    = "./deleteme.SNX";
    // and down here, we have to get rid of two spaces (now occupied by the ./) so that the fixed-spacing is still satisfied.
    // maybe this should be done automatically, but honestly, we're lazy here....
    //    public static final String magicInitializationContents = 
    //	    "$BATCH()\n@FILEX                                                                                        TRTNO     RP     SQ     OP     CO\n"
    //		    +	tempXFileName
    //		    + "                                                                                      1      0      0      0      0\n";
    public static final String magicInitializationContents = 
	    "$BATCH()\n@FILEX                                                                                        TRTNO     RP     SQ     OP     CO\n"
		    +	tempXFileName
		    + "                                                                                    1      0      0      0      0\n";

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

    // Beware the MAGIC ASSUMPTION!!! assuming soil codes must fit into 10 characters padded with zeros
    private static final int totalLengthForSoilType = 10;

    public static final int nDaysInMonth =  30;
    public static final int nDaysInYear  = 365;

    private static final int firstWeatherYear = 0; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly
    private static final int extraWeatherYears = 35; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly

    // details for DSSAT's summary output files...
    private int magicDSSATSummaryLineIndexToRead = 4;

    private static final String magicFirstSummaryColumnNameToKeepForExtras = "DWAP";
    
    private static final String yieldColumnName 	= "HWAH";
    private static final String yieldFallbackColumnName = "HWAM";
    private static final String startingDateColumnName 	= "SDAT";
    private static final String plantingDateColumnName 	= "PDAT";
    private static final String emergenceColumnName 	= "EDAT";
    private static final String anthesisColumnName 	= "ADAT";
    private static final String maturityColumnName 	= "MDAT";
    private static final String harvestColumnName 	= "HDAT";

    // latest 4.6 beta...
    private String[] extraNames = null;


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
    private static final String potatoes3String   = "potatoes3";
    private static final String sorghumString   = "sorghum";
    private static final String cassavaString   = "cassava";
    private static final String sugarcaneString   = "sugarcane";
    private static final String allAtPlantingString      = "allAtPlanting";
    private static final String threeSplitWithFloweringString      = "threeSplitWithFlowering";
    private static final String middleHeavyThreeSplitWithFloweringString      = "middleHeavyThreeSplitWithFlowering";
    private static final String zeroItOutString      = "zeroItOut";



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
    //	private boolean allFlag             = false;

    private static final String pathToDSSATDirectory     = "./";
    private File   pathToDSSATDirectoryAsFile = null;
    private String nameOfDSSATExecutable    = null;
    private String nameOfWeatherExecutable  = null;
    private double SWmultiplier             = Double.NaN;
    private int    firstRandomSeed          = -1;
    private int    nFakeYears        = -2;
    private String magicSoilPrefix          = null;
    private int    fakePlantingYear = -1; 
    private int    spinUpTimeDays           = -4;
    private int    nPlantingWindowsPerMonth = -5;
    private int    plantingWindowLengthDays = -6;
    private int    co2ppm    = -5;
    private String cropFertilizerSchemeToUse = null;
    private int    nHappyPlantRunsForPhenology = -3;
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
    private int optionalHarvestInterval = 0;
    private int plantingDateInMonthShiftInDays = 0;

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
    private String[] weatherExecutionCommand = new String[7];

    ///////////////////////////////////////////////////////////////
    // special variables for the purpose of grabbing the results //
    ///////////////////////////////////////////////////////////////

    // sometimes the Summary.OUT fails to exist at all for unknown reasons
    // as in, DSSAT seems to only partially finish and java comes back (or doesn't allow
    // enough time) and then there is no Summary.OUT. but when we ssh in and run things
    // manually, DSSAT runs quickly and happily.
    // so, we want to be able to skip over those so we can see where it happens on a map.
    // the plan is to write those down as an obvious value for the yields that will stand out.
    private int completelyMissingYieldValue = -3; // to use when the Summary.OUT file fails to exist
    private int completelyMissingAnthesisValue = -2; // to use when the Summary.OUT file fails to exist
    private int completelyMissingMaturityValue = -1; // to use when the Summary.OUT file fails to exist

    // initialize the place to store the yields...

    private DescriptiveStatisticsUtility realYieldsEntirePixel  = new DescriptiveStatisticsUtility(false);
    private DescriptiveStatisticsUtility realYieldsExactlyZeroEntirePixel  = new DescriptiveStatisticsUtility(false);
    private DescriptiveStatisticsUtility happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);

    private DescriptiveStatisticsUtility realTimeToPlantingEntirePixel = new DescriptiveStatisticsUtility(false);
    private DescriptiveStatisticsUtility realNoPlantingEntirePixel = new DescriptiveStatisticsUtility(false);

    private DescriptiveStatisticsUtility realEmergenceEntirePixel = new DescriptiveStatisticsUtility(false);
    private DescriptiveStatisticsUtility realAnthesisEntirePixel = new DescriptiveStatisticsUtility(false);

    private DescriptiveStatisticsUtility realMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
    private DescriptiveStatisticsUtility happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);

    private DescriptiveStatisticsUtility realBadThingsCountsEntirePixel = new DescriptiveStatisticsUtility(false);

    private DescriptiveStatisticsUtility[] extraSummaryAccumulators = null;


    
    // ok, now i am trying to extend in order to read from the OVERVIEW.OUT files...
    
    ////////////// OVERVIEW.OUT stress indices stuff ///////////////
    
//    static final private String magicOverviewRunBreak = "**************************************************************************************************************";
    static final private String magicStressMainLabel = "*ENVIRONMENTAL AND STRESS FACTORS";
    static final private String magicGiantBar = "--------------------------------------------------------------------------------------------------------------";
    static final private String magicSynthGrowth = "synth Growth";
    static final private int magicLengthOfGrowthStageNameAndTimeSpan = 29; // 30
    static final private String magicSpace = " ";
    static final private String magicExtraPrefixForGrowthStages = "gro";
    static final private int magicMinimumLineLengthToConsider = 3;
    static final private int magicLengthOfStressIndices = 5;
    static final private int magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth = 2;

    public static final String magicOverviewToRead          = "OVERVIEW.OUT";
    public static final String magicAltOverviewToRead          = "Overview.OUT";

    
    // GRRRRrrrr.... of course, DSSAT only reports on growth stages that occur, not all the
    // possible ones. so we'll likely have to do this crop by crop....
    static final private String[] maizeCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile" ,
	magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile",
	magicExtraPrefixForGrowthStages + "_" + "End_Juvenil_Floral_Init",
	magicExtraPrefixForGrowthStages + "_" + "Floral_Init_End_Lf_Grow",
	magicExtraPrefixForGrowthStages + "_" + "End_Lf_Grth_Beg_Grn_Fil",
	magicExtraPrefixForGrowthStages + "_" + "Grain_Filling_Phase",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };
    
    static final private String[] beansCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence__First_Flower",
	magicExtraPrefixForGrowthStages + "_" + "First_Flower_First_Seed",
	magicExtraPrefixForGrowthStages + "_" + "First_Seed___Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Emergence____Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",

    };
    
    static final private String[] groundnutsCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence__First_Flower",
	magicExtraPrefixForGrowthStages + "_" + "First_Flower_First_Seed",
	magicExtraPrefixForGrowthStages + "_" + "First_Seed___Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Emergence____Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };
    
    static final private String[] potatoesCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence_Begin_Tuber",
	magicExtraPrefixForGrowthStages + "_" + "Begin_Tuber_Maturity",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };
    
    static final private String[] riceCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile",
	magicExtraPrefixForGrowthStages + "_" + "End_Juvenil_Panicl_Init",
	magicExtraPrefixForGrowthStages + "_" + "Panicl_Init_End_Lf_Grow",
	magicExtraPrefixForGrowthStages + "_" + "End_Lf_Grth_Beg_Grn_Fil",
	magicExtraPrefixForGrowthStages + "_" + "Grain_Filling_Phase",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",

    };
    
    static final private String[] sorghumCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile",
	magicExtraPrefixForGrowthStages + "_" + "End_Juvenil_Panicle_Ini",
	magicExtraPrefixForGrowthStages + "_" + "Panicle_Ini_End_Lf_Grow",
	magicExtraPrefixForGrowthStages + "_" + "End_Lf_Grth_Beg_Grn_Fil",
	magicExtraPrefixForGrowthStages + "_" + "Grain_Filling_Phase",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };
    
    static final private String[] soybeansCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence__First_Flower",
	magicExtraPrefixForGrowthStages + "_" + "First_Flower_First_Seed",
	magicExtraPrefixForGrowthStages + "_" + "First_Seed___Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Emergence____Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };
    
    static final private String[] wheatCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Germinate____Term_Spklt",
	magicExtraPrefixForGrowthStages + "_" + "Term_Spklt___End_Veg",
	magicExtraPrefixForGrowthStages + "_" + "End_Veg______End_Ear_Gr",
	magicExtraPrefixForGrowthStages + "_" + "End_Ear_Gr___Beg_Gr_Fil",
	magicExtraPrefixForGrowthStages + "_" + "Beg_Gr_Fil___End_Gr_Fil",
	magicExtraPrefixForGrowthStages + "_" + "Germinate____End_Gr_Fil",

    };

    static final private String[] chickpeasCleanedGrowthStages = {
	magicExtraPrefixForGrowthStages + "_" + "Emergence__First_Flower",
	magicExtraPrefixForGrowthStages + "_" + "First_Flower_First_Seed",
	magicExtraPrefixForGrowthStages + "_" + "First_Seed___Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Emergence____Phys__Mat_",
	magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
    };

    private String[] findGrowthStageNames(String xFileContents) throws Exception {
	
	// get as array then step through to find the cultivar/crop definition
	String[] xFileAsArray = xFileContents.split("\n");
	
	// look for the magic "*CULTIVARS" block
	final String magicCultivarsHeader = "*CULTIVARS";
	int cultivarHeaderLineIndex = -1;
	for (int lineIndex = 0; lineIndex < xFileAsArray.length; lineIndex++) {
	    if (xFileAsArray[lineIndex].equalsIgnoreCase(magicCultivarsHeader)) {
		// we found the top of the array, so record and break;
		cultivarHeaderLineIndex = lineIndex;
		break;
	    }
	}
	
	// starting there, look for the first cultivar condition definition
	final String magicFirstCultivarTreatmentBeginning = " 1 ";
	String cropTwoLetterCode = "";
	for (int lineIndex = cultivarHeaderLineIndex; lineIndex < xFileAsArray.length; lineIndex++) {
	    if (xFileAsArray[lineIndex].startsWith(magicFirstCultivarTreatmentBeginning)) {
		// we think we have what we want, so get the two letter code
		cropTwoLetterCode = xFileAsArray[lineIndex].substring(3,5); // Beware the MAGIC NUMBER!!! where the two letter code appears: immediately after the treatment/cultivar number
		break;
	    }
	}
	

	// now, we have to match up the two-letter codes with the growth stages
	// Beware the MAGIC ASSUMPTION!!! right now we are considering only the primary crop model
	// we are not looking at secondary ones. so, oryza-in-dssat won't work...

	switch (cropTwoLetterCode) {
	case "MZ": return maizeCleanedGrowthStages;
	case "BN": return beansCleanedGrowthStages;
	case "PN": return groundnutsCleanedGrowthStages;
	case "PT": return potatoesCleanedGrowthStages;
	case "RI": return riceCleanedGrowthStages;
	case "SG": return sorghumCleanedGrowthStages;
	case "SB": return soybeansCleanedGrowthStages;
	case "WH": return wheatCleanedGrowthStages;
	case "CH": return chickpeasCleanedGrowthStages;
	default: 
	    System.out.println("unknown crop code: [" + cropTwoLetterCode + "]"); throw new Exception();
	}
	
    }
    
    //////////// end OVERVIEW.OUT stress indices stuff ///////////////
    
    
    


    public Mink3p2classic(String initFileNameToUse) throws IOException, Exception {
	this.initFileName = initFileNameToUse;
	readInitFile();
    }




    private void readInitFile() throws IOException, Exception {

	String[] initFileContents = FunTricks.readTextFileToArray(initFileName);

	int storageIndex = 0;
	gisTableBaseName         = initFileContents[storageIndex++];
	templateXFile            = initFileContents[storageIndex++];
	yieldOutputBaseName      = initFileContents[storageIndex++];
	nameOfDSSATExecutable    = initFileContents[storageIndex++];
	nameOfWeatherExecutable  = initFileContents[storageIndex++];
	SWmultiplier             = Double.parseDouble(
		initFileContents[storageIndex++]);
	firstRandomSeed          = Integer.parseInt(
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

	// ok, one thing is that we want to make sure that when we pre-generate the weather that we do so far enough in advance that
	// the spin-up time is provided for. so, we are going to start at year #0 for the weather, but then the fake planting year
	// needs to be far enough out.... we will add a small number of years just to make sure nothing stupid happens.


	fakePlantingYear = spinUpTimeDays / 365 + 2;


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

	// ./fast_new_arbfile.exe 1 99001 1 S RRRR.CLI tryme.wth
	weatherExecutionCommand[0] = pathToDSSATDirectory + nameOfWeatherExecutable;
	weatherExecutionCommand[1] = null; // this is the start date code
	weatherExecutionCommand[2] = null; // this is the end date code
	weatherExecutionCommand[3] = null; // this is the random seed to use
	// Beware the MAGIC NUMBER!!! always using simmeteo
	weatherExecutionCommand[4] = "S"; // this is which generator to use, S = SIMMETEO
	weatherExecutionCommand[5] = magicWeatherStationNameToUsePath; // this is the CLI file
	// Beware the MAGIC NUMBER!!! using .WTH because that's what works for me. it might need .WTG under other circumstances
	weatherExecutionCommand[6] = pathToDSSATDirectory + magicWeatherStationNameToUse + ".WTH"; // this is the CLI file


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
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(potatoes3String)) {
	    nitrogenFertilizerScheme = new FSPotatoes3();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(allAtPlantingString)) {
	    nitrogenFertilizerScheme = new FSAllAtPlanting();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(sorghumString)) {
	    nitrogenFertilizerScheme = new FSAllAtPlanting();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(cottonString)) {
	    nitrogenFertilizerScheme = new FSCotton();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(threeSplitWithFloweringString)) {
	    nitrogenFertilizerScheme = new FSThreeSplitWithFlowering();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(middleHeavyThreeSplitWithFloweringString)) {
		nitrogenFertilizerScheme = new FSMiddleHeavyThreeSplitWithFlowering();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(zeroItOutString)) {
	    nitrogenFertilizerScheme = new FSZeroItOut();
	} else if (cropFertilizerSchemeToUse.equalsIgnoreCase(cassavaString)) {
	    nitrogenFertilizerScheme = new FSAllAtPlanting();
	    System.out.println("CASSAVA fertilizer scheme doubles as meaning ignore emergence and maturity!");
	    //			System.out.println("Need to define the fertilizer scheme for " + cropFertilizerSchemeToUse);
	    //			throw new Exception();
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
	    //			System.out.println("Need to define the irrigation scheme for " + irrigationSchemeToUse);
	    //			throw new Exception();
	} else {
	    System.out.println("irrigation string [" + irrigationSchemeToUse + "]" + " not in our list of supported crops; assuming scheme NONE.");
	    irrigationScheme = new IrriSNone();
	}


	// moving this inside the real loop during only the first line...
//	// switching this to consider life as floats rather than as integers...
//	extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraNames.length];
//	for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
//	    extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(true);
//	}


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

	provenanceOut.print("\n");

	provenanceOut.print("pathToDSSATDirectory:\t\t"     + pathToDSSATDirectory + "\n");
	provenanceOut.print("nameOfDSSATExecutable:\t\t"    + nameOfDSSATExecutable + "\n");
	provenanceOut.print("SWmultiplier:\t\t\t"           + SWmultiplier + "\n");
	provenanceOut.print("firstRandomSeed:\t\t"          + firstRandomSeed + "\n");
	provenanceOut.print("nRandomSeedsToUse:\t\t"        + nFakeYears + "\n");
	provenanceOut.print("magicSoilPrefix:\t\t"          + magicSoilPrefix + "\n");
	provenanceOut.print("fakePlantingYear:\t\t"         + fakePlantingYear + "\n");
	provenanceOut.print("spinUpTimeDays:\t\t\t"         + spinUpTimeDays + "\n");
	provenanceOut.print("nPlantingWindowsPerMonth:\t"   + nPlantingWindowsPerMonth + "\n");
	provenanceOut.print("plantingWindowLengthDays:\t"   + plantingWindowLengthDays + "\n");
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
	provenanceOut.print("plantingDateInMonthShiftInDays:\t"    + plantingDateInMonthShiftInDays + "\n");

	provenanceOut.print("maxRunTime:\t"    + maxRunTime + "\n");
	provenanceOut.print("bumpUpMultiplier:\t"    + bumpUpMultiplier + "\n");
	provenanceOut.print("testIntervalToUse:\t"    + testIntervalToUse + "\n");
	provenanceOut.print("rerunAttemptsMax:\t"    + rerunAttemptsMax + "\n");


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
		    // and just in case we try F for forced planting (29may15)
		    newValuesLine += tempValue.replaceAll("A", "R").replaceAll("a", "R").replaceAll("F", "R").replaceAll("f", "R");
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

    
    private Object[] findSummarySpacing() throws Exception {
	// declarations with initializations
	// we only need to read the first few lines, specifically, we want to get to the real header line:
	// *SUMMARY : QUKY3114sq Ki
	//
	// !IDENTIFIERS.......
	// @   RUNNO   TRNO R# O# C#
	// so, we need to read to the fourth line...
	int nLinesToRead = magicDSSATSummaryLineIndexToRead;
	String[] candidateSummaryContents = new String[nLinesToRead];

	try {
	    candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
	} catch (FileNotFoundException fnfe) {
	    // check for error file
	    if (errorAsFileObject.exists()) {
		System.out.println("count spacing: file not found... [" + errorAsFileObject + "] exists...");
		throw fnfe;
	    }
	    System.out.println("count spacing: file not found...  (no error file)");
	}
	
	// ok, so we want to look at the header line and go through and find the beginnings of all the key words...
	// the first "word" is really that silly @-sign, so we want to skip that...
	// but, we should be able to do my fancy multi-space-tolerant split
	String[] splitUpNames = null;

	int lineToGetNamesFrom = magicDSSATSummaryLineIndexToRead - 1;
	try {
	    splitUpNames = FunTricks.readLineToArrayOnArbitrarySpaceOrTab(candidateSummaryContents[lineToGetNamesFrom]);
	} catch (ArrayIndexOutOfBoundsException aioobe) {
	    System.out.println("lineToGetNamesFrom = " + lineToGetNamesFrom +
		    "; candidateSummaryContents.length = " + candidateSummaryContents.length);
	    for (int dumpIndex = 0; dumpIndex < candidateSummaryContents.length; dumpIndex++) {
		System.out.println("candidateSummaryContents[" + dumpIndex + "] = [" + candidateSummaryContents[dumpIndex] + "]");
	    }
	}

	int[] endingIndicesForSplitUpNames = new int[splitUpNames.length];
	int previousEndIndex = 1; // this actually needs to be one; skipping the "@"
	for (int nameIndex = 0; nameIndex < splitUpNames.length; nameIndex++) {
	    endingIndicesForSplitUpNames[nameIndex] =
		    candidateSummaryContents[magicDSSATSummaryLineIndexToRead - 1].indexOf(splitUpNames[nameIndex], previousEndIndex)
		    + splitUpNames[nameIndex].length();
	}	
	return new Object[] {splitUpNames , endingIndicesForSplitUpNames};
    }
    
    
    private static double pullValueUsingName(String summaryLine, String columnName, String[] splitUpNames, int[] endingIndicesForSplitUpNames) {

	// figure out which column index we are looking for
	int nameIndex = findIndexForColumnName(columnName,splitUpNames);

	// figure out what the ending index within the string should be for the one we want
	int endThis = endingIndicesForSplitUpNames[nameIndex];
	
	// default the end of the previous value with zero to take care of the very first column
	int endPrevious = 0; // this actually 

	// if we want anything besides the first one, we need to look up what the ending index of the previous one is
	if (nameIndex > 0) {
	    endPrevious = endingIndicesForSplitUpNames[(nameIndex-1)]; 
	}

	String extractedStringValue = summaryLine.substring(
		endPrevious, endThis
		);
	
	// ok, now the problem is that we need to check for all sorts of problems...
	
	// look for stars
	// look for double whitespace.

	
	return Double.parseDouble(extractedStringValue.trim());
    }

    private static String pullStringValueUsingName(String summaryLine, String columnName, String[] splitUpNames, int[] endingIndicesForSplitUpNames) {

	// figure out which column index we are looking for
	int nameIndex = findIndexForColumnName(columnName,splitUpNames);

	// figure out what the ending index within the string should be for the one we want
	int endThis = endingIndicesForSplitUpNames[nameIndex];
	
	// default the end of the previous value with zero to take care of the very first column
	int endPrevious = 0; // this actually 

	// if we want anything besides the first one, we need to look up what the ending index of the previous one is
	if (nameIndex > 0) {
	    endPrevious = endingIndicesForSplitUpNames[(nameIndex-1)]; 
	}

	String extractedStringValue = summaryLine.substring(
		endPrevious, endThis
		);
	
	// ok, now the problem is that we need to check for all sorts of problems...
	
	// look for stars
	// look for double whitespace.

	
	return extractedStringValue.trim();
    }

    private static int findIndexForColumnName(String columnName, String[] splitUpNames) {
	int columnNumber = -1;
	for (int searchColumnIndex = 0 ; searchColumnIndex < splitUpNames.length ; searchColumnIndex++) {
	    if (splitUpNames[searchColumnIndex].equals(columnName)) {
		columnNumber = searchColumnIndex;
		break;
	    }
	}
	return columnNumber;
    }

    private double[] grabHappyResultsByName(int nYears) throws InterruptedException, Exception {
	// if we wait properly using .waitFor(), we shouldn't have to check if things exist...

	// declarations
	int plantingDate = -1, eventDate = -2, daysSincePlantingForEvent = -3;
	int yieldToUse = -3;
	int anthesisToUse = -3, maturityToUse = -3;
	boolean everythingIsValid = true;
	
	// declarations with initializations
	int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
	String[] candidateSummaryContents = new String[nLinesToRead];

	
	try {
	    candidateSummaryContents = FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
	} catch (FileNotFoundException fnfe) {
	    // check for error file
	    if (errorAsFileObject.exists()) {
		System.out.println("HAPPYbyname: file not found... [" + errorAsFileObject + "] exists...");
		throw fnfe;
	    }
	    System.out.println("HAPPYbyname: file not found...  (no error file)");
	    System.out.println("HAPPYbyname: inscrutable hangup, recording happy yield as -3");
		return new double[] {completelyMissingYieldValue,
			completelyMissingAnthesisValue,
			completelyMissingMaturityValue};

	}

	// parse the output file for the necessary goodies...
	DescriptiveStatisticsUtility happyYields        = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility happyAnthesisDates = new DescriptiveStatisticsUtility(false);
	DescriptiveStatisticsUtility happyMaturityDates = new DescriptiveStatisticsUtility(false);

	Object[] spacingFindings = findSummarySpacing();
	String[] splitUpNamesHere = (String[])spacingFindings[0];
	int[] endingIndicesForSplitUpNamesHere = (int[])spacingFindings[1];
	
	
	for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
	    if (candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] == null) {
		System.out.println("Happy: funny business; skip and hope for the best");
		System.out.println(fakeYearIndex + " -> [" + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] + "]");

		continue;
	    }
	    everythingIsValid = true;
	    try {
		// here is where we try the dynamic way based on whatever was in the header line...

		// yield
		yieldToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			yieldColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		
		// check if it is negative (-99 is usually the missing value); if so, fall back to the other yield
		// reported. this is because the new "dssat simpleB" is currently reporting only HWAM, but not HWAH.
		if (yieldToUse < 0) {
			yieldToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
				yieldFallbackColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		}

		// planting / anthesis / maturity dates
		plantingDate = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			plantingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		anthesisToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			anthesisColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		maturityToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			maturityColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));


		// now need to check whether maturity exists, if not, just go with harvest date if that exists...
		if (maturityToUse < 0) {
		    maturityToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			    harvestColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		}


	    } catch (NumberFormatException nfe) {
		nfe.printStackTrace();
		System.out.println("HAPPY: had trouble reading one of the following as an integer:");
		System.out.println("yield [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			yieldColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("planting  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			plantingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("anthesis  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			anthesisColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("maturity  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			maturityColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
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

	// 22sep17, i decided to make this a double thing instead of rounding. the rounding is done later if necessary
	return new double[] {Math.floor(happyYields.getMean()),
		Math.floor(happyAnthesisDates.getMean()),
		Math.floor(happyMaturityDates.getMean())};


    }

    
    private String[] dynamicallyBuildExtraNamesAndFirstIndexToKeepForExtras(String firstExtraName) throws Exception {

	Object[] spacingFindings = findSummarySpacing();
	String[] splitUpNamesHere = (String[])spacingFindings[0];

	// now we need to figure out which one is the first one we want to keep
	int firstExtraIndex = findIndexForColumnName(firstExtraName, splitUpNamesHere);
	
	String[] justExtraNames = new String[splitUpNamesHere.length - firstExtraIndex];
	
	// now, copy the extras in...
	for (int storageIndex = 0; storageIndex < justExtraNames.length; storageIndex++) {
	    justExtraNames[storageIndex] = splitUpNamesHere[storageIndex + firstExtraIndex];
	}
	
//	int indexOfFirstToKeep = findIndexForColumnName(firstExtraName, splitUpNamesHere);
	
	return justExtraNames;
    }
    
    
    private void grabRealResultsByName(int nYears, long lineIndex, int plantingWindowIndex, String[] splitUpNamesHere, int[] endingIndicesForSplitUpNamesHere)
	    throws InterruptedException, Exception {

	
	// declarations
	int plantingDate = -1, startingDate = -2, daysSincePlantingForEvent = -3;
	int yieldToUse = -4, emergenceToUse = -3, anthesisToUse = -3, maturityToUse = -3;

	// choose the appropriate indices...
	double[] extractedValues = new double[extraNames.length];
	boolean[] everythingIsValid = new boolean[extraNames.length];

	boolean skipThisOne = false, skipExtras = false;

	// declarations with initializations
	int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
	String[] candidateSummaryContents = new String[nLinesToRead];

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


	// get the names of the columns from the


	// parse the output file for the necessary goodies...
	for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {

	    skipThisOne = false;
	    for (int validIndex = 0; validIndex < extraNames.length; validIndex++) {
		everythingIsValid[validIndex] = true; // good unless decided otherwise
	    }

	    try {
		// here is where we try the dynamic way based on whatever was in the header line...

		// yield
		yieldToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			yieldColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));

		// check if it is negative (-99 is usually the missing value); if so, fall back to the other yield
		// reported. this is because the new "dssat simpleB" is currently reporting only HWAM, but not HWAH.
		if (yieldToUse < 0) {
			yieldToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
				yieldFallbackColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		}

		// planting / anthesis / maturity dates
		startingDate = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			startingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		plantingDate = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			plantingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		emergenceToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			emergenceColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		anthesisToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			anthesisColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		maturityToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			maturityColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));


		// now need to check whether maturity exists, if not, just go with harvest date if that exists...
		if (maturityToUse < 0) {
		    maturityToUse = (int)Math.round(pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			    harvestColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere));
		}


		// all the other goodies...
		// hopefully this spacey thing will fix the stars problem; but it just creates new ones when values are
		// improperly formatted and run together...

		// so, we used to check for how many values we have and then skip if it doesn't match up...
		// but, i don't know if we will keep doing that or not...

		// i am trying to simplify the handling of problems. it may not be quite as robust, but i am hoping....
		// basically, the "fundamentals" have one set of try/catch-es and the "extras" have another...
		for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {

		    try {
			extractedValues[extraIndex] = pullValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
				extraNames[extraIndex], splitUpNamesHere, endingIndicesForSplitUpNamesHere);
		    } catch (NumberFormatException nfeExtras) {
			nfeExtras.printStackTrace();
			// ok, let's try this again, but we'll just skip over the offending value but keep the others from this line...
			everythingIsValid[extraIndex] = false;
			System.out.println("REAL: had trouble reading #" + extraIndex + " " + extraNames[extraIndex] + ": [" + 
				pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
					extraNames[extraIndex], splitUpNamesHere, endingIndicesForSplitUpNamesHere)
				+ 
				"] (data line " + lineIndex + "; fakeYear " + fakeYearIndex + " planting window index " + plantingWindowIndex + "):");
			realBadThingsCountsEntirePixel.useLongValue(1); // assess that something bad happened...

			skipExtras = true; // changing to "true" on 02jun15
		    }

		    // should have been taken care of above...
//		    if (everythingIsValid[extraIndex]) {
//			extractedValues[extraIndex] = Double.parseDouble(stringToParse);
//		    }

		} // for extraIndex

	    } catch (NumberFormatException nfeEssentials) {
		nfeEssentials.printStackTrace();
		System.out.println("REAL: had trouble reading one of the following (data line " + lineIndex + "; fakeYear " + fakeYearIndex + " planting window index " + plantingWindowIndex + "):");
		System.out.println("yield [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			yieldColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("starting  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			startingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("planting  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			plantingDateColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("emergence  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			emergenceColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("anthesis  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			anthesisColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		System.out.println("maturity  [" + pullStringValueUsingName(candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead],
			maturityColumnName, splitUpNamesHere, endingIndicesForSplitUpNamesHere) + "]");
		
		
		skipThisOne = true;
		//				throw new Exception();
		System.out.println("     -> something bad happened with the essentials, skipping this line (index = " + lineIndex + ")... <-");
	    } catch (NullPointerException npe) {
		npe.printStackTrace();
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
		// consider changing this to yieldToUse >= 0 [03nov16; not done yet]
		if (yieldToUse >= 0) {
		    realYieldsEntirePixel.useLongValue(yieldToUse);
		    
		    // for the fun of it, let's start keeping track of how many total crop failures there are...
		    if (yieldToUse == 0) {
			realYieldsExactlyZeroEntirePixel.useLongValue(1);
		    }
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
		    realBadThingsCountsEntirePixel.useLongValue(1000); // assess a problem...
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
		    realBadThingsCountsEntirePixel.useLongValue(1000000); // assess a problem...
		}

		// maturity
		// if it's still missing..., skip over it and record as something bad happened
		if (maturityToUse > 0 && plantingDate > 0) {
		    //					if (maturityToUse != magicMissingValue && plantingDate > 0) {
		    daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, maturityToUse);
//		    if (daysSincePlantingForEvent > 300) {
			//						System.out.println("pd = " + plantingDate + " ; mat = " + maturityToUse + " ; diff = " + daysSincePlantingForEvent);
			//						System.out.println("   lm = " + daysSincePlantingForEvent);
//		    }
		    if (daysSincePlantingForEvent > 0) {
			realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
		    } else {
			System.out.print("grab real results: strange maturity; ");
			System.out.println("pd = " + plantingDate + " ; mat = " + maturityToUse + " ; diff = " + daysSincePlantingForEvent);
			realBadThingsCountsEntirePixel.useLongValue(1000000000); // assess a problem...
		    }
		} else {
		    //					System.out.println("grab real results: maturity failed...; not counting in maturity average");
		    //					System.out.println("  mf");
		    realBadThingsCountsEntirePixel.useLongValue(1000000000); // assess a problem...
		}


		// all the extra bits
		if (!skipExtras) {
		    for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
			if (everythingIsValid[extraIndex]) {
			    extraSummaryAccumulators[extraIndex].useDoubleValue(extractedValues[extraIndex]);
			}
		    }
		}


	    } // end if everything is valid, do the recording...

	} // end for fakeYears...

    }
    
    
    
    
    
    
    
    

   

    static private Integer[] stressStartingIndices(String[] overviewFileArray) {

	// look through it and try to find the block of growth stage stresses (the second one that is roughly standardized)
	// and count up how many growth stages we are working with...
	
	int labelIsInThisIndex = 0;
	
	int waterSynthStartIndex = -1;
	int nitrogenSynthStartIndex = -2;
	
	boolean foundFirstGiantBar = false; // this actually needs to be false
	
	for (int lineIndex = 0; lineIndex < overviewFileArray.length; lineIndex++) {
	    if (overviewFileArray[lineIndex].contains(magicStressMainLabel)) {
		// we found what we were looking for, so write it down and bail out...
		labelIsInThisIndex = lineIndex;
		break;
	    }
	}
	
	// ok, step down a few lines and look for the "synth" and "growth" subheadings
	// and figure out what character index ranges they fall in (first will be the
	// water pair, then the nitrogen pair)
	
	// then, we need to look for a giant bar and count all the non-empty lines between
	// there and the next giant bar. also, keep track of the line indices so we can go
	// back and pull out the growth stage names...
	
	// i'm going to do this a little bit inefficiently, because i want it to be easier to debug...
	
	for (int lineIndex = labelIsInThisIndex + 1; lineIndex < overviewFileArray.length; lineIndex++) {
	    if (overviewFileArray[lineIndex].contains(magicSynthGrowth)) {
		// now, find the first occurrence and the second one...
		// Beware the MAGIC NUMBER!!! we are assuming that the first pair are for water
		// and the second pair are for nitrogen
		waterSynthStartIndex = overviewFileArray[lineIndex].indexOf(magicSynthGrowth);
		nitrogenSynthStartIndex = overviewFileArray[lineIndex].indexOf(magicSynthGrowth,waterSynthStartIndex + 1);
		
		// great, but we should continue on in search of the giant bars and stuff...
		// so that will be a separate if/then
		
		// skip along so we don't waste too much time...
		continue;
	    }
	    
	    if (overviewFileArray[lineIndex].contains(magicGiantBar)) {
		// mark if we have found the first giant bar or not...
		if (!foundFirstGiantBar) {
		    // we just found the first one: mark it and skip along...
		    foundFirstGiantBar = true;
		    continue;
		} else {
		    // we're done here, so bail out...
		    break;
		}
	    } // we found a giant bar....
	    
	    
	} // for lineIndex
	
	
	return new Integer[] {waterSynthStartIndex , nitrogenSynthStartIndex } ;
    }

    
    
    static private DescriptiveStatisticsUtility[][] extractStressIndicesWpsWgNpsNg(
	    String[] overviewFileArray, String[] growthStageNames, int waterSynthStartIndex, int nitrogenSynthStartIndex,
	    DescriptiveStatisticsUtility[][] stressAccumulators
	    ) throws Exception {
	

	
	final int nStressIndices = 4; // a magic number: we have photosynthesis and growth crossed with water and nitrogen
	int nGrowthStages = growthStageNames.length;

	// check if we have non-null stat utilities. create them if necessary...
	if (stressAccumulators == null) {
	    // define our accumulators; there will be four in each row, water/photosynthesis, water/growth, nitrogen/photosynthesis, nitrogen/growth 
	    stressAccumulators = new DescriptiveStatisticsUtility[nGrowthStages][4];

	    // initialize the accumulators
	    for (int growthStageIndex = 0; growthStageIndex < nGrowthStages; growthStageIndex++) {
		stressAccumulators[growthStageIndex][0] = new DescriptiveStatisticsUtility(true); // water/photosynthesis
		stressAccumulators[growthStageIndex][1] = new DescriptiveStatisticsUtility(true); // water/growth
		stressAccumulators[growthStageIndex][2] = new DescriptiveStatisticsUtility(true); // nitrogen/photosynthesis
		stressAccumulators[growthStageIndex][3] = new DescriptiveStatisticsUtility(true); // nitrogen/growth
	    } // end for growthStageIndex
	} else {
	    // check to make sure they are the right dimensions... we'll leave it at that...
	    if (stressAccumulators.length != growthStageNames.length) {
		System.out.println("mismatched stress accumulators and growth stage names: ["
			+ stressAccumulators.length + "] stressAccumulators.length != growthStageNames.length [" + growthStageNames.length + "]");
		throw new Exception();
	    }
	    for (int growthStageIndex = 0; growthStageIndex < nGrowthStages; growthStageIndex++) {
		if (stressAccumulators[growthStageIndex].length != nStressIndices) {
		    System.out.println("incorrect number of stress index accumulators: [" + stressAccumulators[growthStageIndex].length
			    + "] stressAccumulators[growthStageIndex].length != nStressIndices [" + nStressIndices +"]");
		} // if wrong number of accumulators
	    } // for growthStageIndex
	} // if/else idiot checking



	// look through it and try to find the block of growth stage stresses (the second one that is roughly standardized)
	// and count up how many growth stages we are working with...

	// across the bottom, even on the last on.
	double waterPhotosynthesisStress;
	double waterGrowthStress;
	double nitrogenPhotosynthesisStress;
	double nitrogenGrowthStress;

	int finalSpaceIndex = -5;
	int growthStageIndexThatMatches = -6;
	String paddedGrowthStageNameFromOverviewDotOut = null;
	
	int labelIsInThisIndex = 0;

	boolean foundFirstGiantBar = false; // this actually needs to be false
	boolean keepLookingForRuns = true; // this actually needs to be true
	
	int startLineIndex = 0; // this actually needs to be zero
	int growthStageWithinBlockLineIndex = 0;
	
	while (keepLookingForRuns && startLineIndex < overviewFileArray.length) {

	    
//	    System.out.println("top of while: [" + startLineIndex + "] = [" + overviewFileArray[startLineIndex] + "]");
	    
	    // start at the previous ending point and keep going...
	    for (int lineIndex = startLineIndex; lineIndex < overviewFileArray.length; lineIndex++) {
		if (overviewFileArray[lineIndex].contains(magicStressMainLabel)) {
		    // we found what we were looking for, so write it down and bail out...
		    labelIsInThisIndex = lineIndex;
		    break;
		} else if (lineIndex == overviewFileArray.length - 1) {
		    // we need to check if we have run off the end again...
		    keepLookingForRuns = false;
		}
	    }

	    if (!keepLookingForRuns) {
		// get the biggest thing we can so that hopefully
		startLineIndex = Integer.MAX_VALUE;
		break;
	    }

	    // then, we need to look for a giant bar and count all the non-empty lines between
	    // there and the next giant bar. also, keep track of the line indices so we can go
	    // back and pull out the growth stage names...

	    // i'm going to do this a little bit inefficiently, because i want it to be easier to debug...

	    // at this point, we should have found the stress main label
	    for (int lineIndex = labelIsInThisIndex + 1; lineIndex < overviewFileArray.length; lineIndex++) {
		if (overviewFileArray[lineIndex].startsWith(magicGiantBar)) {
		    // mark if we have found the first giant bar or not...
		    if (!foundFirstGiantBar) {
			// we just found the first one: mark it and skip along...
			foundFirstGiantBar = true;
			growthStageWithinBlockLineIndex = 0;
			continue;
		    } else {
			// we're done with this "RUN", so reset the startLineIndex and bail back out to the main while loop
			startLineIndex = lineIndex + 1; // tell it to start after this giant bar...
			foundFirstGiantBar = false;
//			System.out.println("     ... hit second giant bar (breaking) ...");
			break;
		    }
		} // we found a giant bar....

		// ok, if we get to here, hopefully, we are between the bars...
		// we are looking for non-empty lines...
		if (foundFirstGiantBar && overviewFileArray[lineIndex].length() > magicMinimumLineLengthToConsider
//			&& !overviewFileArray[lineIndex].startsWith(magicGiantBar)
			) {
		    // hmmm... look for the final space in the first piece
//System.out.println("overviewFileArray[" + lineIndex + "] = [" + overviewFileArray[lineIndex] + "]");
		    // Beware the MAGIC ASSUMPTION!!! i am going to assume that they all give
		    // us " |-----Development Phase------|" which is like 30 characters long.
		    // but the last character in the real info is always a space (time span in days
		    // then space before the temperatures start). so, we want to look at the initial
		    // 30 - 1 = 29 characters
		    //		int magicLengthOfGrowthStageNameAndTimeSpan = 30;

		    // Beware the MAGIC NUMBER!!! looking for spaces and we know the first
		    // character of the line is almost always a space...
//		    System.out.println("candidate string = [" + overviewFileArray[lineIndex] + "]");
		    finalSpaceIndex = overviewFileArray[lineIndex].substring(0, magicLengthOfGrowthStageNameAndTimeSpan).lastIndexOf(magicSpace);
		    // grab everything before that
		    // trim it up
		    // convert spaces and hyphens to underscores
		    // Beware the MAGIC NUMBER!!! the prefix for the growth stage names so that we can find them easily in lists
		    // of variable names... (i.e., for importing the maps into GRASS)
		    paddedGrowthStageNameFromOverviewDotOut = magicExtraPrefixForGrowthStages + "_" 
			    + overviewFileArray[lineIndex].substring(0, finalSpaceIndex).trim().replaceAll("-", "_").replaceAll(" ","_").replaceAll("\\.","_");

		    // and now, we must compare to the list we are working with.
		    growthStageIndexThatMatches = -1;
		    for (int listGrowthStageIndex = 0; listGrowthStageIndex < growthStageNames.length; listGrowthStageIndex++) {
			if (growthStageNames[listGrowthStageIndex].equals(paddedGrowthStageNameFromOverviewDotOut)) {
			    // we found what we're looking for
			    growthStageIndexThatMatches = listGrowthStageIndex;
			    break;
			}
		    }
		    
		    if (growthStageIndexThatMatches < 0) {
			// we likely had a problem, so let's look at what we have...
			System.out.println("   we probably didn't find growth stage [" + paddedGrowthStageNameFromOverviewDotOut + "]");
		    }
		    
		    // grab the four values
		    waterPhotosynthesisStress = -5;
		    
		    
		    waterPhotosynthesisStress = Double.parseDouble(
			    overviewFileArray[lineIndex].substring(waterSynthStartIndex,waterSynthStartIndex + magicLengthOfStressIndices)
			    );
		    waterGrowthStress = Double.parseDouble(
			    overviewFileArray[lineIndex].substring(
				    waterSynthStartIndex + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth + magicLengthOfStressIndices,
				    waterSynthStartIndex + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth + 2*magicLengthOfStressIndices)
			    );
		    nitrogenPhotosynthesisStress = Double.parseDouble(
			    overviewFileArray[lineIndex].substring(nitrogenSynthStartIndex,nitrogenSynthStartIndex + magicLengthOfStressIndices)
			    );
		    nitrogenGrowthStress = Double.parseDouble(
			    overviewFileArray[lineIndex].substring(
				    nitrogenSynthStartIndex + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth + magicLengthOfStressIndices,
				    nitrogenSynthStartIndex + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth + 2*magicLengthOfStressIndices)
			    );
		    
		    stressAccumulators[growthStageIndexThatMatches][0].useDoubleValue(waterPhotosynthesisStress);    // water/photosynthesis
		    stressAccumulators[growthStageIndexThatMatches][1].useDoubleValue(waterGrowthStress);            // water/growth
		    stressAccumulators[growthStageIndexThatMatches][2].useDoubleValue(nitrogenPhotosynthesisStress); // nitrogen/photosynthesis
		    stressAccumulators[growthStageIndexThatMatches][3].useDoubleValue(nitrogenGrowthStress);         // nitrogen/growth


//		    System.out.println("-- internal line index " + growthStageWithinBlockLineIndex + " ; real line index " + lineIndex + " --");
//		    System.out.println("Wps = " + waterPhotosynthesisStress);
//		    System.out.println("Wg  = " + waterGrowthStress);
//		    System.out.println("Nps = " + nitrogenPhotosynthesisStress);
//		    System.out.println("Ng  = " + nitrogenGrowthStress);
		    
		    growthStageWithinBlockLineIndex++;
		    
		} // if found giant bar and valid line...

		// now, we should do some sort of checking on whether we have found all the lines we anticipate and thus should
		// be looking for the next RUN's block...
		
		if (growthStageWithinBlockLineIndex >= nGrowthStages
//			|| overviewFileArray[lineIndex].startsWith(magicOverviewRunBreak)
//			|| overviewFileArray[lineIndex].startsWith(magicGiantBar)
			) {
		    
		    // we think we found everything, so reset a bunch of things...
//		    System.out.println("      ____resetting because we think we found all the growth stages_____");
		    foundFirstGiantBar = false;
		    startLineIndex = lineIndex + 1;
		    growthStageWithinBlockLineIndex = 0;
		    
//		    System.out.println("      ====about to break: startLineIndex=" + startLineIndex + " ====");
		    
		    break;
		}

	    } // for lineIndex

//	    nGroupsFound++;
//	    System.out.println("      #### at bottom; startLineIndex=" + startLineIndex + " ; length = " + overviewFileArray.length + " ; nFound = " + nGroupsFound + " ####");


	} // end keepLookingForRuns
	
	return stressAccumulators;
    
    }

    
    
    
    
    ////////





    public void doSimulationsAndPregenerateWeather() throws Exception {

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

	double timeSinceStart = -4.3, projectedTime = -4.5, timeRemaining = -4.6;



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

	int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
	double nitrogenLevel = Double.NaN;
	int soilType = -1, firstPlantingDay = -4, startingDayToPlantForThisWindow = -1, endingDayToPlantForThisWindow = -99;
	int startingDateAsInt = -59; 

	String startingDayToPlantCode = null, endingDayToPlantCode   = null, initializationDayCode  = null;
	String harvestDayCode  = null, fertilizerBlock  = null, irrigationBlock  = null;
	String randomSeedCode = null, nYearsCode = null, nHappyYearsCode = null, co2ppmCode = null;
	String soilTypeString = null;

	double[] phenologyInDays = null;

	String statisticsOutLine = null;

	double totalInitialNitrogenKgPerHa = -5, rootWeight = -6, surfaceResidueWeight = -7;

	String cliStuffToWrite = null, XStuffToWrite = null, XHappyStuffToWrite = null, initializationBlock = null;

	boolean happyIsGoodEnoughToProceedWithReal = false;

	boolean weHaveFoundASummaryFile = false;
	boolean weHaveLinesToWriteAtTheVeryBeginningBecauseOfFailedInitialRuns= false;
	int nLinesToRecordAtTopDueToFailedInitialRunsAndNoSummaryHeaderKnowledge = 0;
	boolean weShouldBotherParsingOverviewDotOut = false;
	String overviewToTry = null;
	File overviewTestFile = null;
//	Object[] growthStageInfoPacket = null;
	String[] growthStageNames        = null;
	String[] overviewAsArray = null;
	Integer[] startingIndices = null;			    

	Object[] spacingFindings = null;
	String[] splitUpNamesHere = null;
	int[] endingIndicesForSplitUpNamesHere = null;

	
	
	int waterSynthStartIndexToUse    = -89;
	int nitrogenSynthStartIndexToUse = -95;
	DescriptiveStatisticsUtility[][] stressAccumulators = null;

//	    // where we will be storing things
//	    String[] overviewAsArray = null;
//	    Object[] growthStageInfoPacket = null;
//
//	    String[] growthStageNames        = null;
//	    int waterSynthStartIndexToUse    = -5;
//	    int nitrogenSynthStartIndexToUse = -6;
//
//	    // ok, now let's try to accumulate...	
//	    DescriptiveStatisticsUtility[][] growthStageStressAccumulators = null;

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

	File weatherStationFile   = new File(magicWeatherStationNameToUsePath);

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
	    statisticsOut.flush();
	    statisticsOut.close();
	    throw new Exception();
	}

	int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];



	// since this implementation (using the multiple years with a single random seed, rather
	// than multiple random seeds with a single year) has the seeds and years as invariants, do
	// them up front to save on a little search and replace overhead...
	randomSeedCode  = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
	nYearsCode      = DSSATHelperMethods.padWithZeros(nFakeYears, 5);
	nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
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

	// declare the execution process for generating fake weather
	Process theRunningProcess = null;

	SystemCallWithTimeout happyRunnerThing = new SystemCallWithTimeout();
	SystemCallWithTimeout realRunnerThing = new SystemCallWithTimeout();

	int nLinesFoundInSummary = -1;

	System.out.println("-- starting through data --");
	for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

	    elseTimer.tic();

	    // initialize the descriptive statistics utility and storage array
	    realYieldsEntirePixel.reset();
	    realYieldsExactlyZeroEntirePixel.reset();
	    happyYieldsEntirePixel.reset();
	    realEmergenceEntirePixel.reset();
	    realAnthesisEntirePixel.reset();
	    realMaturityEntirePixel.reset();
	    happyMaturityEntirePixel.reset();
	    realBadThingsCountsEntirePixel.reset();
	    realTimeToPlantingEntirePixel.reset();
	    realNoPlantingEntirePixel.reset();

	    
	    // ok, so the very first time through, these are not yet defined, so they do not need to be reset. after that, they are fair game.
	    // resetting is fine to do the extra couple that might be missing from the OLD stuff
	    if (weHaveFoundASummaryFile) {
		for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
		    extraSummaryAccumulators[extraIndex].reset();
		}
	    } // lineIndex > 0


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
	    soilTypeString = magicSoilPrefix + FunTricks.padStringWithLeadingZeros(Integer.toString(soilType), totalLengthForSoilType - magicSoilPrefix.length());


	    // let us now pregenerate the weather data that will be used for everything
	    // specifically:
	    // * happy plant runs
	    // * normal plant runs
	    // * AND all planting windows; that is, suppose we ask for 2 planting dates per month, the early planters
	    //   and the late planters will be subject to the same weather, just the early planters will have had their
	    //   crops in the ground for 2 weeks before the late planters do...

	    // run the weather generator

	    //			weatherExecutionCommand[0] = pathToDSSATDirectory + nameOfWeatherExecutable;
	    //			weatherExecutionCommand[1] = Integer.toString(fakePlantingYear); // this is the start date code
	    // ok, this needs to be slightly more tricky: we want to allow for bazillions of years of fake weather.
	    // also, we don't need padding for this, so we will just move the year over and add the day
	    // Beware the MAGIC NUMBER!!! starting weather on the first day of the year...
	    weatherExecutionCommand[1] = Integer.toString(firstWeatherYear * 1000 + 1); // this is the start date code; should be year #00
	    // this is the end date code; adding a little just to be safe; originally two extra years, now trying more (16jan14)...
	    weatherExecutionCommand[2] = Integer.toString((fakePlantingYear + nFakeYears + extraWeatherYears) * 1000 + 1); 
	    weatherExecutionCommand[3] = Integer.toString(firstRandomSeed); // this is the random seed to use
	    // Beware the MAGIC NUMBER!!! always using simmeteo
	    //			weatherExecutionCommand[4] = "S"; // this is which generator to use, S = SIMMETEO
	    //			weatherExecutionCommand[5] = magicWeatherStationNameToUsePath; // this is the CLI file
	    // Beware the MAGIC NUMBER!!! using .WTH because that's what works for me. it might need .WTG under other circumstances
	    //			weatherExecutionCommand[6] = pathToDSSATDirectory + magicWeatherStationNameToUse + ".WTH"; // this is the CLI file

	    //			System.out.println("weatherExecutionCommand[1] = [" + weatherExecutionCommand[1] + "]");
	    //			System.out.println("weatherExecutionCommand[2] = [" + weatherExecutionCommand[2] + "]");
	    //			System.out.println("weatherExecutionCommand[3] = [" + weatherExecutionCommand[3] + "]");
	    //			System.out.println("weatherExecutionCommand[4] = [" + weatherExecutionCommand[4] + "]");
	    //			System.out.println("weatherExecutionCommand[5] = [" + weatherExecutionCommand[5] + "]");
	    //			System.out.println("weatherExecutionCommand[6] = [" + weatherExecutionCommand[6] + "]");

	    elseTimerStats.useDoubleValue(elseTimer.tocMillis());
	    weatherTimer.tic();

	    // this should be pretty stable, so we'll do it the simple way....
	    theRunningProcess = Runtime.getRuntime().exec(weatherExecutionCommand , null , pathToDSSATDirectoryAsFile);

	    // wait for it to finish up
	    theRunningProcess.waitFor();
	    weatherTimerStats.useDoubleValue(weatherTimer.tocMillis());





	    // loop over the planting windows and random seeds
	    for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth ; plantingWindowIndex++) {
		elseTimer.tic();

		// pick the starting day/etc for this window
		startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;

		startingDateAsInt = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(fakePlantingYear*1000 + startingDayToPlantForThisWindow, plantingDateInMonthShiftInDays);

		startingDayToPlantCode = DSSATHelperMethods.padWithZeros(startingDateAsInt,5); 

		//				System.out.println("fPD = [" + firstPlantingDay + "], pWI = [" + plantingWindowIndex + "], pWS = [" + plantingWindowSpacing + "] --> sDTPFTW = [" +
		//						startingDayToPlantForThisWindow + "], fake planting year = [" + fakePlantingYear + "], coded version = [" + startingDayToPlantCode + "]");
//		startingDateAsInt = Integer.parseInt(startingDayToPlantCode);

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
		XHappyStuffToWrite = XHappyStuffToWrite.replaceAll(nYearsOrRandomSeedsPlaceholder, DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology,5));
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

		happyRunnerThing = new SystemCallWithTimeout();

		for (int rerunIndex = 0; rerunIndex < rerunAttemptsMax ; rerunIndex++) {
		    happyRunnerThing.setup(dssatExecutionCommand, pathToDSSATDirectoryAsFile, (int)Math.ceil(maxRunTime * Math.pow(bumpUpMultiplier, rerunIndex)), testIntervalToUse);
		    happyRunnerThing.run();

		    if (happyRunnerThing.finishedCleanly() != SystemCallWithTimeout.SYSTEM_CALL_RAN_FINE) {
			// check if the summary exists at all...
			if ( ! (new File(magicDSSATSummaryToRead).exists()) ) {
			    nLinesFoundInSummary = 0;
			} else {
			    nLinesFoundInSummary = FunTricks.nLinesInTextFile(magicDSSATSummaryToRead);
			}
			System.out.println("     +++ happy timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
				+ " lines in Summary.OUT = " + nLinesFoundInSummary + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );
		    } else {
			// it seems all is well
			break;
		    }
		}

		happyTimerStats.useDoubleValue(happyTimer.tocMillis());

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
		phenologyInDays = this.grabHappyResultsByName(nHappyPlantRunsForPhenology);

		// OVERVIEW.OUT setup AND NOW the extra names, too...
		// on the very first line and very first planting window, we'll need to be clever and set everything up. after that, we'll just re-use...
//		if (lineIndex == 0 && plantingWindowIndex == 0) {
		if (!weHaveFoundASummaryFile) {
		    
		    // figure out what the extra names are and initialize/etc.
		    try {
			spacingFindings = findSummarySpacing();

			splitUpNamesHere = (String[])spacingFindings[0];
			endingIndicesForSplitUpNamesHere = (int[])spacingFindings[1];

			extraNames = dynamicallyBuildExtraNamesAndFirstIndexToKeepForExtras(magicFirstSummaryColumnNameToKeepForExtras);

			// switching this to consider life as floats rather than as integers...
			extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraNames.length];
			for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
			    extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(true);
			}

			System.out.println("   testing for overview (first line; after happy)");
			overviewToTry = magicOverviewToRead;
			overviewTestFile = new File(overviewToTry); // assuming it is in the current working directory, of course

			// check if the first option exists.
			if (!overviewTestFile.exists()) {
			    System.out.println("    didn't find [" + overviewTestFile + "], so trying the next one...");
			    // switch to the other possible known file name
			    overviewToTry = magicAltOverviewToRead;
			    overviewTestFile = new File(overviewToTry); // assuming it is in the current working directory, of course
			    // and try this one....
			    if (overviewTestFile.exists()) {
				System.out.println("    FOUND IT! [" + overviewTestFile + "]");
				weShouldBotherParsingOverviewDotOut = true;
			    } else {
				System.out.println("    didn't find [" + overviewTestFile + "], either");
			    }
			} else {
			    System.out.println("    FOUND IT! [" + overviewTestFile + "]");
			    weShouldBotherParsingOverviewDotOut = true;
			}

			if (weShouldBotherParsingOverviewDotOut) {
			    // initiate brute force by getting crop from x-file and choosing the appropriate
			    // hard-coded list of growth stages
			    growthStageNames = findGrowthStageNames(XHappyStuffToWrite);

			    // figure out the starting indices for the stress indices
			    overviewAsArray = FunTricks.readTextFileToArray(overviewToTry);
			    startingIndices = stressStartingIndices(overviewAsArray);			    

			    waterSynthStartIndexToUse    = startingIndices[0];
			    nitrogenSynthStartIndexToUse = startingIndices[1];


			} // we should bother, so set everything up...

			// now we are ok, so flip the boolean
			weHaveFoundASummaryFile = true;
		    } catch (NullPointerException npe) {
			System.out.println("    failed to find a " + magicDSSATSummaryToRead +" so far at line " 
				+ lineIndex + " window " + plantingWindowIndex);
			// bump up the counter so that once we know what the "extras" are, we can
			// write a bunch of zeros or something....
			weHaveLinesToWriteAtTheVeryBeginningBecauseOfFailedInitialRuns= true;
			// we don't want to multi-count multiple planting windows. so we will just pull this directly
			// off of the lineIndex since we should only see this in the initial block of failures....
			nLinesToRecordAtTopDueToFailedInitialRunsAndNoSummaryHeaderKnowledge = lineIndex + 1;
		    }
		} // if lineIndex == 0, that is, this is the first time through...

		// we don't care about the happy overview, so clear it out so we don't get confused
		if (weShouldBotherParsingOverviewDotOut) {
			overviewTestFile.delete(); // clear it out after reading it? to make sure they don't get confused...
		}

		readingTimerStats.useDoubleValue(readingTimer.tocMillis());


		// proceed, if the happy yield exceeds some threshold and the effective growing season isn't too long....

		happyIsGoodEnoughToProceedWithReal = false;
		if ( this.cropFertilizerSchemeToUse.equalsIgnoreCase(cassavaString) ||
			this.cropFertilizerSchemeToUse.equalsIgnoreCase(sugarcaneString)) {
		    if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns) {
			happyIsGoodEnoughToProceedWithReal = true;
		    }
		} else {
		    if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {
			happyIsGoodEnoughToProceedWithReal = true;
		    }
		}

		if (happyIsGoodEnoughToProceedWithReal) {

		    elseTimer.tic();
		    /////////////////////
		    // make the X file //
		    /////////////////////

		    // create the fertilizer block...
		    fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
			    (int)Math.floor(phenologyInDays[1]) + phenologyBufferInDays, (int)Math.floor(phenologyInDays[2]) + phenologyBufferInDays, nitrogenLevel
			    );

		    //					irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), nRandomSeedsToUse);
		    // Beware the MAGIC NUMBER!!! we only care about a single season here...
		    irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), 1);
		    irrigationBlock = irrigationScheme.buildIrrigationBlock();


		    // do the search and replace thing; the invariants have already been done above...
		    XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
		    // put in the random seed to use

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



		    realRunnerThing = new SystemCallWithTimeout();


		    for (int rerunIndex = 0; rerunIndex < rerunAttemptsMax ; rerunIndex++) {
			realRunnerThing.setup(dssatExecutionCommand, pathToDSSATDirectoryAsFile, (int)Math.ceil(maxRunTime * Math.pow(bumpUpMultiplier, rerunIndex)), testIntervalToUse);
			realRunnerThing.run();

			if (realRunnerThing.finishedCleanly() != SystemCallWithTimeout.SYSTEM_CALL_RAN_FINE) {

			    // check if the summary exists at all...
			    if ( ! (new File(magicDSSATSummaryToRead).exists()) ) {
				nLinesFoundInSummary = 0;
			    } else {
				nLinesFoundInSummary = FunTricks.nLinesInTextFile(magicDSSATSummaryToRead);
			    }
			    // check how many lines ended up in Summary.OUT
			    System.out.println("     +++ real timed out #" + rerunIndex + "(max = " + (rerunAttemptsMax - 1) + ") line=" + lineIndex + " plantingWindow=" + plantingWindowIndex 
				    + " lines in Summary.OUT = " + nLinesFoundInSummary + " goal = " + (nFakeYears + magicDSSATSummaryLineIndexToRead) );

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
		    grabRealResultsByName(nFakeYears, lineIndex, plantingWindowIndex, splitUpNamesHere, endingIndicesForSplitUpNamesHere);

		    // ok, i think i want to add in the reading of the OVERVIEW.OUT file here...
		    if (weShouldBotherParsingOverviewDotOut) {

			// reset things...
			if (plantingWindowIndex == 0) {
			    stressAccumulators = null;
			}
			
			// and try actually reading them...
			// the correct filename should have been ascertained; we should not get here if the file does not exist
			overviewAsArray = FunTricks.readTextFileToArray(overviewToTry);
			
			// ok, now let's try to accumulate...
			// but i need to modify to match up growth stage names rather than just looking for them...
//			System.out.println("about to extract stresses: lineIndex = " + lineIndex + "; planting window = " + plantingWindowIndex);
			stressAccumulators = extractStressIndicesWpsWgNpsNg(
				overviewAsArray, growthStageNames, waterSynthStartIndexToUse, nitrogenSynthStartIndexToUse, stressAccumulators
				);

			overviewTestFile = new File(overviewToTry); // assuming it is in the current working directory, of course
			overviewTestFile.delete(); // clear it out after reading it?
			
		    } // if weShouldBotherParsingOverviewDotOut, but outside the initialization block for lineIndex == 0

		    readingTimerStats.useDoubleValue(readingTimer.tocMillis());


		    elseTimer.tic();

		    elseTimerStats.useDoubleValue(elseTimer.tocMillis());



		} // end if happy yield threshold is met

	    } // end plantingWindowIndex


	    //////////////////////////////////////////////////////////////////////////
	    // when finished with a pixel, then write out a line to the output file //
	    //////////////////////////////////////////////////////////////////////////


	    elseTimer.tic();
	    // do the summary out stuff

	    statisticsOutLine = "";

	    // we need to see whether we have the information to write down...
	    if (weHaveLinesToWriteAtTheVeryBeginningBecauseOfFailedInitialRuns && weHaveFoundASummaryFile) {
		// now, run through and create ugly initial failure output lines to get us back to where we need to be.
		
		System.out.println("---->>>> recovering from initial failures <<<<----");
		System.out.println(" we need to build " + nLinesToRecordAtTopDueToFailedInitialRunsAndNoSummaryHeaderKnowledge + " garbage lines at the top..." );
		
		for (int failureLineIndex = 0 ; failureLineIndex < nLinesToRecordAtTopDueToFailedInitialRunsAndNoSummaryHeaderKnowledge ; failureLineIndex++) {
		    System.out.println(" doing garbage line failureLineIndex = " + failureLineIndex);
		    // the fundamentals
		    statisticsOutLine = 
			    (-2) + delimiter 
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
			    + FunTricks.onlySomeDecimalPlaces(completelyMissingYieldValue,nDecimalsInOutput) + delimiter
			    + FunTricks.onlySomeDecimalPlaces(completelyMissingMaturityValue,nDecimalsInOutput);

		    for (int extraIndex = 0; extraIndex < extraNames.length; extraIndex++) {
			statisticsOutLine += delimiter + 0;
		    }

		    //				statisticsOutLine += delimiter + 0 + delimiter + (-1);
		    // "time_to_planting" + delimiter + "n_no_planting" + delimiter + "n_real_exactly_zero" + delimiter + "n_contributing_real" + delimiter + "n_contributing_happy";
		    statisticsOutLine += delimiter + (-this.spinUpTimeDays) + delimiter + (-1) + delimiter + (0) +
			    delimiter + realYieldsEntirePixel.getN() + delimiter + happyYieldsEntirePixel.getN();

		    // the HWAH_stdev
		    statisticsOutLine += delimiter + 0;

		    // and the stress indices...
		    if (weShouldBotherParsingOverviewDotOut) {
			for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
			    for (int stressIndexIndex = 0; stressIndexIndex < 4; stressIndexIndex++) {
				//					statisticsOutLine += delimiter + stressAccumulators[growthStageIndex][stressIndexIndex].getMean();
				statisticsOutLine += delimiter + (-1);
			    } // for stress index index
			} // for growth stage index
		    } // if we should bother with overview
		
		    // we need to tack this on the end
		    statisticsOutLine += "\n";

		    writingTimer.tic();
		    statisticsOut.print(statisticsOutLine);
		    writingTimerStats.useDoubleValue(writingTimer.tocMillis());

		}

		// and we have been forgetting to actually write it down....
		
		
		// don't foreget to reset!!! since we have hopefully accomplished the task, we shouldn't need to do it again...
		weHaveLinesToWriteAtTheVeryBeginningBecauseOfFailedInitialRuns = false;
	    }

	    // first, let us check whether anything got recorded...
	    // but only do so if we have read a real Summary.OUT
	    if (weHaveFoundASummaryFile) {
		if (realYieldsEntirePixel.getN() == 0) {

		    // the fundamentals
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
		    // "time_to_planting" + delimiter + "n_no_planting" + delimiter + "n_real_exactly_zero" + delimiter + "n_contributing_real" + delimiter + "n_contributing_happy";
		    statisticsOutLine += delimiter + (-this.spinUpTimeDays) + delimiter + (-1) + delimiter + (0) +
			    delimiter + realYieldsEntirePixel.getN() + delimiter + happyYieldsEntirePixel.getN();

		    // the HWAH_stdev
		    statisticsOutLine += delimiter + 0;

		    // and the stress indices...
		    if (weShouldBotherParsingOverviewDotOut) {
			for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
			    for (int stressIndexIndex = 0; stressIndexIndex < 4; stressIndexIndex++) {
				//				statisticsOutLine += delimiter + stressAccumulators[growthStageIndex][stressIndexIndex].getMean();
				statisticsOutLine += delimiter + (-1);
			    } // for stress index index
			} // for growth stage index
		    } // if we should bother with overview


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
		    + delimiter + realNoPlantingEntirePixel.getN() + delimiter + realYieldsExactlyZeroEntirePixel.getN() +
		    delimiter + realYieldsEntirePixel.getN() + delimiter + happyYieldsEntirePixel.getN();

		    statisticsOutLine += delimiter + FunTricks.onlySomeDecimalPlaces(
			    extraSummaryAccumulators[findIndexForColumnName(yieldColumnName, splitUpNamesHere)].getStd(),
			    nDecimalsInOutput);


		    // and the stress indices...
		    if (weShouldBotherParsingOverviewDotOut) {
			for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
			    for (int stressIndexIndex = 0; stressIndexIndex < 4; stressIndexIndex++) {
				statisticsOutLine += delimiter + 
					FunTricks.onlySomeDecimalPlaces(stressAccumulators[growthStageIndex][stressIndexIndex].getMean(),nDecimalsInOutput);
				//			statisticsOutLine += delimiter + 0;
			    } // for stress index index
			} // for growth stage index
		    } // if we should bother with overview
		}

		statisticsOutLine += "\n";

		elseTimerStats.useDoubleValue(elseTimer.tocMillis());

		writingTimer.tic();
		statisticsOut.print(statisticsOutLine);
		writingTimerStats.useDoubleValue(writingTimer.tocMillis());
	    } // should be the end of if(weHaveFoundASummaryFile) because we don't want to write anything until we have found such a thing...
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

	columnList += delimiter + "time_to_planting" + delimiter + "n_no_planting" + delimiter + "n_real_exactly_zero" + delimiter + "n_contributing_real" + delimiter + "n_contributing_happy";
	columnList += delimiter + "HWAH_std";

	// and the stress indices...
	if (weShouldBotherParsingOverviewDotOut) {
	    for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
		    columnList += delimiter + growthStageNames[growthStageIndex] + "_watersyn";
		    columnList += delimiter + growthStageNames[growthStageIndex] + "_watergro";
		    columnList += delimiter + growthStageNames[growthStageIndex] + "_nitrosyn";
		    columnList += delimiter + growthStageNames[growthStageIndex] + "_nitrogro";
	    } // for growth stage index
	} // if we should bother with overview

	
	columnList += "\n";


	FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

	long nRows = nLinesInDataFile;

	// Beware the MAGIC NUMBER!!!
	int nCols = -5;
	//		nCols = 13 + extraNames.length + 2 + 2; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
	// now with the HWAH_std....
//	nCols = 13 + extraNames.length + 2 + 2 + 1; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
	// now with number of exactly zero yields
	nCols = 13 + extraNames.length + 2 + 1 + 2 + 1;
	// min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
	// time / no planting / exactly zero / n real / n happy / HWAH std

	if (weShouldBotherParsingOverviewDotOut) {
	    for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
		nCols += 4; // Beware the MAGIC NUMBER!!! there are four stress indices per growth stage...W
		    
	    } // for growth stage index
	} // if we should bother with overview
	
	
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

