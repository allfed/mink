package org.DSSATRunner;

import java.io.*;
import java.util.Date;
import org.R2Useful.*;

public class DSSATRunnerIrrigation45OldBetaHeuristic {

  private boolean readyToRun = false;

  /////////////////////////////////////
  // create a bunch of magic numbers //
  /////////////////////////////////////
  public static final String delimiter = "\t";
  public static final String magicWeatherStationNameToUse = "RICK";
  public static final String magicDSSATSummaryToRead = "Summary.OUT";
  public static final String magicErrorFile = "ERROR.OUT";
  //	public static final String magicInitializationFile          = "deleteme.dv4";
  public static final String magicInitializationFile = "deleteme.v45";
  public static final String tempXFileName = "deleteme.SNX";
  //	public static final String tempXFileName                    = "deleteme.meX";
  public static final String magicInitializationContents =
      "$BATCH()\n"
          + "@FILEX                                                                                "
          + "        TRTNO     RP     SQ     OP     CO\n"
          + tempXFileName
          + "                                                                                     "
          + " 1      0      0      0      0\n";

  // search and replace elements in the template X file
  public static final String soilPlaceholder = "ssssssssss";
  public static final String initializationStartPlaceholder = "iiiiS";
  public static final String plantingDateStartPlaceholder = "ppppS";
  public static final String plantingDateEndPlaceholder = "ppppE";
  public static final String randomSeedPlaceholder = "rrrrr";
  public static final String nYearsOrRandomSeedsPlaceholder = "nnnnn";
  public static final String weatherPlaceholder = "wwww";
  public static final String co2ppmPlaceholder = "co2p";
  public static final String fertilizerPlaceholder = "___place fertilizers here___";
  public static final String irrigationPlaceholder = "___place irrigation here___";
  public static final String soilInitializationPlaceholder = "___place initializations here___";

  public static final int hardLimitOnReReads = 200;

  public static final int hardLimitOnHappyReadingErrors = 20;

  public static final int nDaysInMonth = 30;
  public static final int nDaysInYear = 365;

  // details for DSSAT's summary output files...
  private static final int magicMissingValue = -99;
  private int magicDSSATSummaryLineIndexToRead = 4;
  //	private int magicDSSATSummaryLineLength = 526; // 467; // 350;
  private int magicDSSATSummaryLineLength = 467; // 350;

  private int magicHarvestedWeightAtHarvestStartIndex = 139; // 139; // character # 137, index 136
  private int magicHarvestedWeightAtHarvestEndIndex = 147; // 147; // character # 141, index 140

  private int magicPlantingDateStartIndex = 86; // character # 137, index 136
  private int magicPlantingDateEndIndex = 93; // character # 141, index 140

  //	private int magicEmergenceDateStartIndex = 108;
  //	private int magicEmergenceDateEndIndex   = 116;

  private int magicAnthesisDateStartIndex = 93; // character # 137, index 136
  private int magicAnthesisDateEndIndex = 101; // character # 141, index 140

  private int magicMaturityDateStartIndex = 101; // character # 137, index 136
  private int magicMaturityDateEndIndex = 109; // character # 141, index 140

  private int magicHarvestingDateStartIndex = 131; // character # 137, index 136
  private int magicHarvestingDateEndIndex = 139; // character # 141, index 140

  //	private static final String[] extraNames     =
  // {"IR#M","IRCM","PRCM","ROCM","DRCM","NICM","NFXM","NUCM","NLCM","NIAM","CNAM","GNAM"};
  //	private static final int[] extraStartIndices = new int[]
  // {189,195,201,225,231,249,255,261,267,273,279,285};
  //	private static final int[] extraEndIndices   = new int[]
  // {194,200,206,230,236,254,260,266,272,278,284,290};
  private static final String[] extraNames = {
    "DWAP", "CWAM", "HWAM", "HWAH", "BWAH", "PWAM", "HWUM", "H#AM", "H#UM", "HIAM", "LAIX", "IR#M",
    "IRCM", "PRCM", "ETCM", "EPCM", "ESCM", "ROCM", "DRCM", "SWXM", "NI#M", "NICM", "NFXM", "NUCM",
    "NLCM", "NIAM", "CNAM", "GNAM", "PI#M", "PICM", "PUPC", "SPAM", "KI#M", "KICM", "KUPC", "SKAM",
    "RECM", "ONTAM", "ONAM", "OPTAM", "OPAM", "OCTAM", "OCAM", "DMPPM", "DMPEM", "DMPTM", "DMPIM",
    "YPPM", "YPEM", "YPTM", "YPIM",
  };
  private static final int[] extraStartIndices =
      new int[] {
        118, 123, 131, 139, 147, 155, 161, 169, 175, 183, 189, 195, 202, 207, 214, 219, 225, 231,
        237, 244, 249, 255, 261, 267, 273, 279, 285, 291, 297, 303, 309, 315, 321, 327, 333, 339,
        345, 351, 358, 365, 372, 379, 387, 395, 404, 413, 422, 431, 440, 449, 458,
      };
  private static final int[] extraEndIndices =
      new int[] {
        123, 131, 139, 147, 155, 161, 169, 175, 183, 189, 195, 202, 207, 214, 219, 225, 231, 237,
        244, 249, 255, 261, 267, 273, 279, 285, 291, 297, 303, 309, 315, 321, 327, 333, 339, 345,
        351, 358, 365, 372, 379, 387, 395, 404, 413, 422, 431, 440, 449, 458, 467,
      };
  //	private static final int[] extraEndIndices   = new int[]
  // {122,130,138,146,154,160,168,174,182,188,194,201,206,213,218,224,230,236,243,248,254,260,
  //
  //	266,272,278,284,290,296,302,308,314,320,326,332,338,344,350,357,364,371,378,386,394,403,412,421,430,439,448,457,466,};

  // latest 4.5 beta...
  //	private static final String[] extraNames     = {
  //		"DWAP", "CWAM", "HWAM", "HWAH", "BWAH", "PWAM",
  //		"HWUM", "H#AM", "H#UM", "HIAM", "LAIX", "IR#M",
  //		"IRCM", "PRCM", "ETCM", "EPCM", "ESCM", "ROCM",
  //		"DRCM", "SWXM", "NI#M", "NICM", "NFXM", "NUCM",
  //		"NLCM", "NIAM", "CNAM", "GNAM", "PI#M", "PICM",
  //		"PUPC", "SPAM", "KI#M", "KICM", "KUPC", "SKAM",
  //		"RECM", "ONTAM", "ONAM", "OPTAM", "OPAM", "OCTAM",
  //		"OCAM", "DMPPM", "DMPEM", "DMPTM", "DMPIM", "YPPM",
  //		"YPEM", "YPTM", "YPIM", "DPNAM", "DPNUM", "YPNAM",
  //		"YPNUM",
  //	};
  //	private static final int[] extraStartIndices = new int[]
  // {140,146,154,162,170,178,184,192,198,206,212,218,224,230,236,242,
  //
  //	248,254,260,266,272,278,284,290,296,302,308,314,320,326,332,338,344,350,356,362,368,374,381,388,395,402,410,418,427,436,445,
  //		454,463,472,481,490,499,508,517,};
  //	private static final int[] extraEndIndices   = new int[] {
  // 146,154,162,170,178,184,192,198,206,212,218,224,230,236,242,
  //
  //	248,254,260,266,272,278,284,290,296,302,308,314,320,326,332,338,344,350,356,362,368,374,381,388,395,402,410,418,427,436,445,
  //		454,463,472,481,490,499,508,517,526,};
  // crop categories.... each needs a unique id number...

  private static final String maizeString = "maize";
  private static final String riceString = "rice";
  private static final String wheatString = "wheat";
  private static final String soybeansString = "soybeans";
  private static final String groundnutsString = "groundnuts";
  private static final String cottonString = "cotton";
  private static final String potatoesString = "potatoes";
  private static final String allAtPlantingString = "allAtPlanting";

  //	private static final int maizeInt      = 1;
  //	private static final int riceInt       = 2;
  //	private static final int wheatInt      = 3;
  //	private static final int soybeansInt   = 4;
  //	private static final int groundnutsInt = 5;
  //	private static final int cottonInt     = 6;

  /////////////////////////////////////////////////////
  // other variables which are good to have sharable //
  /////////////////////////////////////////////////////

  private String initFileName = null;

  private String gisTableBaseName = null;
  private String templateXFile = null;
  private String yieldOutputBaseName = null;
  private boolean allFlag = false;

  private String pathToDSSATDirectory = null;
  private File pathToDSSATDirectoryAsFile = null;
  private String nameOfDSSATExecutable = null;
  private double SWmultiplier = Double.NaN;
  private int firstRandomSeed = -1;
  private int nRandomSeedsToUse = -2;
  private String magicSoilPrefix = null;
  private int fakePlantingYear = -3;
  private int spinUpTimeDays = -4;
  private int nPlantingWindowsPerMonth = -5;
  private int plantingWindowLengthDays = -6;
  private long sleeptimeWaitForFileMillis = 21;
  private int maxParsingTries = 5;
  private int co2ppm = -5;
  private String cropFertilizerSchemeToUse = null;
  private int nHappyPlantRunsForPhenology = -3;
  private int happyYieldThresholdToDoRealRuns = 0;
  private int phenologyBufferInDays = 0;
  private int happyMaturityThresholdToDoRealRuns = 0;
  private String irrigationSchemeToUse = null;
  private double fractionBetweenLowerLimitAndDrainedUpperLimit = Double.NEGATIVE_INFINITY;
  private double nitrogenPPMforBothNH4NO2 = Double.POSITIVE_INFINITY;

  private String magicWeatherStationNameToUsePath = null;
  private String magicDSSATSummaryToReadPath = null;
  private File summaryDSSATOutputAsFileObject = null;
  private File errorAsFileObject = null;
  private String magicInitializationFilePath = null;

  //	private int cropToUseInt = -3;

  private NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
  private IrrigationScheme irrigationScheme = null;

  private String[] dssatExecutionCommand = new String[3];

  ///////////////////////////////////////////////////////////////
  // special variables for the purpose of grabbing the results //
  ///////////////////////////////////////////////////////////////
  // initialize the place to store the yields...

  private DescriptiveStatisticsUtility realYieldsEntirePixel =
      new DescriptiveStatisticsUtility(false);
  private DescriptiveStatisticsUtility happyYieldsEntirePixel =
      new DescriptiveStatisticsUtility(false);

  private DescriptiveStatisticsUtility realEmergenceEntirePixel =
      new DescriptiveStatisticsUtility(false);
  private DescriptiveStatisticsUtility realAnthesisEntirePixel =
      new DescriptiveStatisticsUtility(false);

  private DescriptiveStatisticsUtility realMaturityEntirePixel =
      new DescriptiveStatisticsUtility(false);
  private DescriptiveStatisticsUtility happyMaturityEntirePixel =
      new DescriptiveStatisticsUtility(false);

  private DescriptiveStatisticsUtility realBadThingsCountsEntirePixel =
      new DescriptiveStatisticsUtility(false);

  private DescriptiveStatisticsUtility[] extraSummaryAccumulators = null;

  private int nTimesSuccessfullyReadFirstTime = 0;
  private int nTimesSuccessfullyReadFirstTimeHappy = 0;

  private long initialSleepTimeToUse = -1;
  private long initialSleepTimeToUseHappy = 0;

  private DescriptiveStatisticsUtility readingTimesReal = null;
  private DescriptiveStatisticsUtility readingTimesHappy = null;

  private boolean badThingsHappened = false;
  //	private int badThingsHappenedIndex = -1;
  //	private int badPlantingWindow = 9;

  public DSSATRunnerIrrigation45OldBetaHeuristic() {

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
    initFileContents +=
        "phenologyBufferInDays (positive means days AFTER happy plant phenology)" + "\n";
    initFileContents +=
        "happyMaturityThresholdToDoRealRuns (days between planting and maturity; anything above"
            + " this will be skipped)\n";

    initFileContents += "cropToUse for irrigation scheme" + "\n";
    initFileContents +=
        "fractionBetweenLowerLimitAndDrainedUpperLimit for initializing the soil moisture" + "\n";
    initFileContents += "nitrogenPPMforBothNH4NO2 for initializing soil nitrogen content" + "\n";

    FunTricks.writeStringToFile(initFileContents, filename);
  }

  public static String usageMessage() {

    String usageMessage =
        "Usage: class GIS_table_base_name template_X_file yield_output_base_name ALL_flag"
            + " settings_csv  sleep_time_for_file\n"
            + "\n"
            + "This program will try to do one-stop, full-service running of DSSAT based on a table"
            + " of climate/etc inputs.\n"
            + "Everything is brute force and magical.\n"
            + "\n"
            + "The delimiter in the GIS_table_base_name files should be tab.\n"
            + "\n"
            + "The columns of the data table should be: soil type / elevation (m) / planting month"
            + " / monthly SW / monthly tmax (C) / monthly tmin (C) / prec (mm) / rainy days (#)\n"
            + "\n"
            + "The geography file should have four columns: row / col / latitude / longitude\n"
            + "\n"
            + "The template_X_file should have the following string in it for search-and-replace"
            + " purposes:\n"
            + "Of course, these should be verified with the source code...\n"
            + "ssssssssss\tfor replacement by the soil type code (ID_SOIL)\n"
            + "iiiiS\tstart date for the initialization (ICDAT, SDATE)\n"
            + "ppppS\tstart date for the planting window (PDATE, PFRST)\n"
            + "ppppE\tend date for the planting window (PLAST)\n"
            + "rrrrr\trandom seed for weather generator (RSEED)\n"
            + "wwww\tweather file name (WSTA)\n"
            + "\n"
            + "The ALL_flag should be true/false indicating whether to report all simulated yields."
            + " Regardless,the summary for each pixel will be reported.\n"
            + "\n"
            + "The settings_csv should contain all the fancy controls in a particular order as"
            + " specified below:\n"
            + "* full path to DSSAT directory with trailing slash\n"
            + "* DSSAT executable name\n"
            + "* shortwave multiplier\n"
            + "    SW_multiplier is the value to multiply the short-wave radiation numbers by in"
            + " order to get the proper units. the final\n"
            + "    units should be MJ/m^2/day. For example, if the raw data are in W/m^2, the"
            + " multiplier is 3600 * 24 / 10^6 = 0.0864 .\n"
            + "\n"
            + "* first random seed\n"
            + "* # of seeds to use\n"
            + "* magic soil prefix\n"
            + "* fake planting year (2 digits)\n"
            + "* spin up time (days)\n"
            + "* # of planting windows to try (evenly spaced based on a 30-day month; leap years"
            + " ignored)\n"
            + "* planting window length (days)\n"
            + "\n"
            + "\n"
            + "\n"
            + "The yield_output_base_name indicates where to write out the output.\n"
            + "The following are intended:\n"
            + "yield_output_base_name + _provenance.txt\tlist of all the arguments (both usable and"
            + " human readable), copy of template X_file used\n"
            + "yield_output_base_name + _ALL.txt (& .info.txt)\tall the goodies for each pixel\n"
            + "yield_output_base_name + _ALL.codes.txt\t\thuman readable names for each elements"
            + " (specifically, day for the start of planting window and the random seed used)\n"
            + "yield_output_base_name + _STATS.txt (& .info.txt)\tmin/max/mean/std for each pixel"
            + " over all realizations and planting dates within the month\n"
            + "\n"
            + "This will create an initialization file called something like deleteme.dv4 along"
            + " with a re-used climate file like RICK.CLI and.\n"
            + "an X-file called deleteme.meX\n"
            + "\n"
            + "The sleep_time_for_file determines how long to wait for the file before trying to"
            + " read it. The units are MILLISECONDS...\n"
            + "WARNING!!! Nothing is idiot proofed!\n"
            + "\n";

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

    //		for (int index = 0; index < initFileContents.length; index++) {
    //		System.out.println(index + " -> [" + initFileContents[index] + "]");
    //		}

    int storageIndex = 0;
    gisTableBaseName = initFileContents[storageIndex++];
    templateXFile = initFileContents[storageIndex++];
    yieldOutputBaseName = initFileContents[storageIndex++];
    allFlag = Boolean.parseBoolean(initFileContents[storageIndex++]);
    pathToDSSATDirectory = initFileContents[storageIndex++];
    nameOfDSSATExecutable = initFileContents[storageIndex++];
    SWmultiplier = Double.parseDouble(initFileContents[storageIndex++]);
    firstRandomSeed = Integer.parseInt(initFileContents[storageIndex++]);
    nRandomSeedsToUse = Integer.parseInt(initFileContents[storageIndex++]);
    magicSoilPrefix = initFileContents[storageIndex++];
    fakePlantingYear = Integer.parseInt(initFileContents[storageIndex++]);
    spinUpTimeDays = Integer.parseInt(initFileContents[storageIndex++]);
    nPlantingWindowsPerMonth = Integer.parseInt(initFileContents[storageIndex++]);
    plantingWindowLengthDays = Integer.parseInt(initFileContents[storageIndex++]);

    sleeptimeWaitForFileMillis = Long.parseLong(initFileContents[storageIndex++]);
    maxParsingTries = Integer.parseInt(initFileContents[storageIndex++]);

    co2ppm = Integer.parseInt(initFileContents[storageIndex++]);

    cropFertilizerSchemeToUse = initFileContents[storageIndex++];

    nHappyPlantRunsForPhenology = Integer.parseInt(initFileContents[storageIndex++]);
    happyYieldThresholdToDoRealRuns = Integer.parseInt(initFileContents[storageIndex++]);
    phenologyBufferInDays = Integer.parseInt(initFileContents[storageIndex++]);
    happyMaturityThresholdToDoRealRuns = Integer.parseInt(initFileContents[storageIndex++]);

    irrigationSchemeToUse = initFileContents[storageIndex++];

    fractionBetweenLowerLimitAndDrainedUpperLimit =
        Double.parseDouble(initFileContents[storageIndex++]);
    nitrogenPPMforBothNH4NO2 = Double.parseDouble(initFileContents[storageIndex++]);

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
    magicDSSATSummaryToReadPath = pathToDSSATDirectory + magicDSSATSummaryToRead;
    summaryDSSATOutputAsFileObject = new File(magicDSSATSummaryToReadPath);
    errorAsFileObject = new File(pathToDSSATDirectory + magicErrorFile);
    magicInitializationFilePath = pathToDSSATDirectory + magicInitializationFile;

    // figure out the crop number to use so we can use switches instead of if/then...
    // i'm sure there's a better way, but i'm lame, so i'm gonna brute force it...

    if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.maizeString)) {
      nitrogenFertilizerScheme = new FSMaize();
    } else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.riceString)) {
      nitrogenFertilizerScheme = new FSRice();
    } else if (cropFertilizerSchemeToUse.equalsIgnoreCase(this.wheatString)) {
      nitrogenFertilizerScheme = new FSWheat();
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
      //			System.out.println("Need to define the fertilizer scheme for " +
      // cropFertilizerSchemeToUse);
      //			throw new Exception();
    } else {
      System.out.println(
          "crop string ["
              + cropFertilizerSchemeToUse
              + "]"
              + " not in our list of supported crops; or at least, not implemented");
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
      System.out.println(
          "irrigation string ["
              + irrigationSchemeToUse
              + "]"
              + " not in our list of supported crops; assuming scheme NONE.");
      irrigationScheme = new IrriSNone();
    }

    // switching this to consider life as floats rather than as integers...
    extraSummaryAccumulators = new DescriptiveStatisticsUtility[extraStartIndices.length];
    for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
      extraSummaryAccumulators[extraIndex] = new DescriptiveStatisticsUtility(true);
    }

    //		mark that we're good to go...
    readyToRun = true;
  }

  private void writeProvenance() throws IOException, FileNotFoundException, Exception {
    String filename = yieldOutputBaseName + "_provenance.txt";
    writeProvenance(filename);
  }

  private void writeProvenance(String filename)
      throws IOException, FileNotFoundException, Exception {

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

    provenanceOut.print("gisTableBaseName:\t" + gisTableBaseName + "\n");
    provenanceOut.print("templateXFile:\t\t" + templateXFile + "\n");
    provenanceOut.print("yieldOutputBaseName:\t" + yieldOutputBaseName + "\n");
    provenanceOut.print("allFlag:\t\t" + allFlag + "\n");

    provenanceOut.print("\n");

    provenanceOut.print("pathToDSSATDirectory:\t\t" + pathToDSSATDirectory + "\n");
    provenanceOut.print("nameOfDSSATExecutable:\t\t" + nameOfDSSATExecutable + "\n");
    provenanceOut.print("SWmultiplier:\t\t\t" + SWmultiplier + "\n");
    provenanceOut.print("firstRandomSeed:\t\t" + firstRandomSeed + "\n");
    provenanceOut.print("nRandomSeedsToUse:\t\t" + nRandomSeedsToUse + "\n");
    provenanceOut.print("magicSoilPrefix:\t\t" + magicSoilPrefix + "\n");
    provenanceOut.print("fakePlantingYear:\t\t" + fakePlantingYear + "\n");
    provenanceOut.print("spinUpTimeDays:\t\t\t" + spinUpTimeDays + "\n");
    provenanceOut.print("nPlantingWindowsPerMonth:\t" + nPlantingWindowsPerMonth + "\n");
    provenanceOut.print("plantingWindowLengthDays:\t" + plantingWindowLengthDays + "\n");
    provenanceOut.print("sleeptimeWaitForFileMillis:\t" + sleeptimeWaitForFileMillis + "\n");
    provenanceOut.print("maxParsingTries:\t\t" + maxParsingTries + "\n");
    provenanceOut.print("co2ppm:\t\t\t\t" + co2ppm + "\n");

    provenanceOut.print("nitrogenCropToUse:\t\t\t" + cropFertilizerSchemeToUse + "\n");
    provenanceOut.print("nHappyPlantRunsForPhenology:\t" + nHappyPlantRunsForPhenology + "\n");
    provenanceOut.print(
        "happyYieldThresholdToDoRealRuns:\t" + happyYieldThresholdToDoRealRuns + "\n");
    provenanceOut.print("phenologyBufferInDays:\t" + phenologyBufferInDays + "\n");
    provenanceOut.print(
        "happyMaturityThresholdToDoRealRuns:\t" + happyMaturityThresholdToDoRealRuns + "\n");

    provenanceOut.print("irrigationCropToUse:\t\t\t" + irrigationSchemeToUse + "\n");
    provenanceOut.print(
        "fractionBetweenLowerLimitAndDrainedUpperLimit:\t"
            + fractionBetweenLowerLimitAndDrainedUpperLimit
            + "\n");
    provenanceOut.print("nitrogenPPMforBothNH4NO2:\t" + nitrogenPPMforBothNH4NO2 + "\n");

    provenanceOut.print("\n");

    provenanceOut.print("--- Placeholder dictionary ---" + "\n");
    provenanceOut.print("soilPlaceholder =\t\t\t" + soilPlaceholder + "\n");
    provenanceOut.print(
        "initializationStartPlaceholder =\t" + initializationStartPlaceholder + "\n");
    provenanceOut.print("plantingDateStartPlaceholder =\t\t" + plantingDateStartPlaceholder + "\n");
    provenanceOut.print("plantingDateEndPlaceholder =\t\t" + plantingDateEndPlaceholder + "\n");
    provenanceOut.print("randomSeedPlaceholder =\t\t\t" + randomSeedPlaceholder + "\n");
    provenanceOut.print("weatherPlaceholder =\t\t\t" + weatherPlaceholder + "\n");
    provenanceOut.print("co2ppm =\t\t\t\t" + co2ppm + "\n");
    provenanceOut.print("fertilizerPlaceholder =\t\t\t" + fertilizerPlaceholder + "\n");
    provenanceOut.print("irrigationPlaceholder =\t\t\t" + irrigationPlaceholder + "\n");

    provenanceOut.print("\n");

    provenanceOut.print("--- Magic number dictionary ---" + "\n");
    provenanceOut.print("magicWeatherStationNameToUse =\t" + magicWeatherStationNameToUse + "\n");
    provenanceOut.print("magicDSSATSummaryToRead =\t" + magicDSSATSummaryToRead + "\n");
    provenanceOut.print("magicErrorFile =\t\t\t" + magicErrorFile + "\n");
    provenanceOut.print("magicInitializationFile =\t" + magicInitializationFile + "\n");
    provenanceOut.print("tempXFileName =\t\t\t" + tempXFileName + "\n");
    provenanceOut.print("\n");

    String XAsString = FunTricks.readTextFileToString(templateXFile);

    provenanceOut.print("--- begin copy of template X file [" + templateXFile + "] ---" + "\n");
    provenanceOut.print(XAsString);
    provenanceOut.print("---- end copy of template X file [" + templateXFile + "] ----" + "\n");
    provenanceOut.print("\n");
    provenanceOut.print(
        "--- begin copy of temporary initialization file ["
            + magicInitializationFilePath
            + "] ---"
            + "\n");
    provenanceOut.print(magicInitializationContents);
    provenanceOut.print(
        "---- end copy of temporary initialization file ["
            + magicInitializationFilePath
            + "] ----"
            + "\n");
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

    String optionNamesLine = templateAsArray[lineWeWant];
    String optionValuesLine = templateAsArray[lineWeWant + 1];

    // since i'm not good with strings, we're gonna brute force this

    // count how many "fields" we're dealing with
    int nColumns = 0;
    boolean currentCharIsSpace = false;
    boolean wereInWordPriorToThis = false;

    for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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
    String tempName = "";
    String tempValue = "";
    String newNamesLine = "";
    String newValuesLine = "";
    for (int colIndex = 0; colIndex < nColumns; colIndex++) {
      if (colIndex == 0) {
        startIndex = 0;
      } else {
        startIndex = endOfWord[colIndex - 1];
      }

      tempName = optionNamesLine.substring(startIndex, endOfWord[colIndex]);
      tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

      // the names are pretty static...
      newNamesLine += tempName;

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
    templateAsArray[lineWeWant] = newNamesLine;
    templateAsArray[lineWeWant + 1] = newValuesLine;

    // ok, and now we need to run everything together into a single string with newlines again...

    String happyXTemplate = "";

    for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
      happyXTemplate += templateAsArray[lineIndex] + "\n";
    }

    return happyXTemplate;
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
      System.out.println("template X did not have an \"OPTIONS\" line");
      throw new Exception();
    }

    String optionNamesLine = templateAsArray[lineWeWant];
    String optionValuesLine = templateAsArray[lineWeWant + 1];

    // since i'm not good with strings, we're gonna brute force this

    // count how many "fields" we're dealing with
    int nColumns = 0;
    boolean currentCharIsSpace = false;
    boolean wereInWordPriorToThis = false;

    for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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

    int startIndex = 0;
    String tempName = "";
    String tempValue = "";
    String newNamesLine = "";
    String newValuesLine = "";
    for (int colIndex = 0; colIndex < nColumns; colIndex++) {
      if (colIndex == 0) {
        startIndex = 0;
      } else {
        startIndex = endOfWord[colIndex - 1];
      }

      tempName = optionNamesLine.substring(startIndex, endOfWord[colIndex]);
      tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

      // the names are pretty static...
      newNamesLine += tempName;

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
    templateAsArray[lineWeWant] = newNamesLine;
    templateAsArray[lineWeWant + 1] = newValuesLine;

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

    optionNamesLine = templateAsArray[lineWeWant];
    optionValuesLine = templateAsArray[lineWeWant + 1];

    //		System.out.println("name line = [" + optionNamesLine + "]");
    //		System.out.println("valu line = [" + optionValuesLine + "]");

    // since i'm not good with strings, we're gonna brute force this

    // count how many "fields" we're dealing with
    nColumns = 0;
    currentCharIsSpace = false;
    wereInWordPriorToThis = false;

    for (int charIndex = 0; charIndex < optionNamesLine.length(); charIndex++) {
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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
      if (optionNamesLine.substring(charIndex, charIndex + 1).equals(" ")) {
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
    tempName = "";
    tempValue = "";
    newNamesLine = "";
    newValuesLine = "";
    for (int colIndex = 0; colIndex < nColumns; colIndex++) {
      if (colIndex == 0) {
        startIndex = 0;
      } else {
        startIndex = endOfWord[colIndex - 1];
      }

      tempName = optionNamesLine.substring(startIndex, endOfWord[colIndex]);
      tempValue = optionValuesLine.substring(startIndex, endOfWord[colIndex]);

      // the names are pretty static...
      newNamesLine += tempName;

      // the first two columns are setup, so preserve them as is...
      if (colIndex < 2) {
        newValuesLine += tempValue;
      } else {
        // check if we want to set this to yes or no
        //				System.out.println("considering: [" + tempName.trim() + "]");
        if (tempName.trim().equalsIgnoreCase(optionToMakeZero)) {
          // replace any semblance of no with yes
          newValuesLine +=
              tempValue
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
    templateAsArray[lineWeWant] = newNamesLine;
    templateAsArray[lineWeWant + 1] = newValuesLine;

    /////////////////// end look for irrigation treatment... /////////////////

    // ok, and now we need to run everything together into a single string with newlines again...

    String happyXTemplate = "";

    for (int lineIndex = 0; lineIndex < templateAsArray.length; lineIndex++) {
      happyXTemplate += templateAsArray[lineIndex] + "\n";
    }

    return happyXTemplate;
  }

  private int[] grabNewHappyResults(int nYears) throws InterruptedException, Exception {
    //		System.out.println(" *** top of grab happy ***");
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
    //		int emergenceToUse = -3;
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

      //			System.out.println("     %% reading attempt #" + nTries + " %%");
      // determine whether we want to increase the initial wait time:
      // let's bump it up or down...
      if (nTimesSuccessfullyReadFirstTimeHappy == 0 && nTries == 1) {
        initialSleepTimeToUseHappy =
            Math.min(
                initialSleepTimeToUseHappy + 1,
                (long) (readingTimesHappy.getMinAsLong() * multiplierForTimeUpperBound));
      } else if (nTimesSuccessfullyReadFirstTimeHappy > 0
          && nTimesSuccessfullyReadFirstTimeHappy % timeCheckNumber == 0
          && nTries == 0) {
        initialSleepTimeToUseHappy--;
      }

      // make sure the sleep time is positive...
      // Beware the MAGIC NUMBER!!!
      if (initialSleepTimeToUseHappy < 0) {
        initialSleepTimeToUseHappy = 0;
      }

      // do the sleeping...
      if (nTries == 0) {
        Thread.sleep(initialSleepTimeToUseHappy);
      } else {
        Thread.sleep(retrySleepTimeToUse);
      }

      nTries++;
      //			System.out.println("           about to try to read [" + magicDSSATSummaryToReadPath + "]
      // ... #" + nTries);

      // attempt to read
      try {
        //				System.out.println("           in try above read");
        candidateSummaryContents =
            FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
        //				System.out.println("           in try below read; length = " +
        // candidateSummaryContents.length);
      } catch (FileNotFoundException fnfe) {
        // check for error file
        if (errorAsFileObject.exists()) {
          System.out.println(
              "HAPPY: file not found... try #" + nTries + " [" + errorAsFileObject + "] exists...");
          throw fnfe;
        }
        System.out.println("HAPPY: file not found... try #" + nTries + " (no error file)");

        //				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
        // below will poop out
      } catch (IOException ioe) {
        System.out.println("HAPPY: i/o exception...  try #" + nTries);
        //				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
        // below will poop out
        throw ioe;
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        System.out.println("HAPPY: array index exception...  try #" + nTries);
        // carry on, since the file is still in the process of being written...
      }

      // check if the file has the right number of lines. if so, check that the last line is
      // completely there...
      //			System.out.println("HAPPY: try #" + nTries + " length = " +
      // candidateSummaryContents.length);
      //			System.out.println("    --- out of try/catch for reading; last length = " +
      // candidateSummaryContents[candidateSummaryContents.length - 1].length() + " ---");
      if (candidateSummaryContents[nLinesToRead - 1] != null
          && candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength) {
        readSuccessfully = true;
        nTimesSuccessfullyReadFirstTimeHappy++;
      } else {
        // check for error file
        if (errorAsFileObject.exists()) {
          System.out.println(
              "HAPPY: partial read... try #" + nTries + " [" + errorAsFileObject + "] exists...");
          throw new Exception();
        }
        nTimesSuccessfullyReadFirstTimeHappy = 0;
      }
    }
    // check the time and update the timekeeping thingee
    readingTimesHappy.useDoubleValue(readingTimer.tocMillis());

    // parse the output file for the necessary goodies...
    DescriptiveStatisticsUtility happyYields = new DescriptiveStatisticsUtility(false);
    //		DescriptiveStatisticsUtility happyEmergenceDates = new DescriptiveStatisticsUtility(false);
    DescriptiveStatisticsUtility happyAnthesisDates = new DescriptiveStatisticsUtility(false);
    DescriptiveStatisticsUtility happyMaturityDates = new DescriptiveStatisticsUtility(false);

    for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
      if (candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead] == null) {
        System.out.println("H: funny business");
        for (int fYI = 0; fYI < nYears; fYI++) {
          System.out.println(
              fYI
                  + " -> ["
                  + candidateSummaryContents[fYI + magicDSSATSummaryLineIndexToRead]
                  + "]");
        }
        throw new NullPointerException();
      }
      everythingIsValid = true;
      try {
        // yield
        yieldToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(
                        magicHarvestedWeightAtHarvestStartIndex,
                        magicHarvestedWeightAtHarvestEndIndex)
                    .trim());
        // planting / anthesis / maturity dates
        plantingDate =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicPlantingDateStartIndex, magicPlantingDateEndIndex)
                    .trim());
        //				emergenceToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
        // magicDSSATSummaryLineIndexToRead]
        //
        // .substring(magicEmergenceDateStartIndex,
        //
        //	magicEmergenceDateEndIndex).trim());;
        anthesisToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicAnthesisDateStartIndex, magicAnthesisDateEndIndex)
                    .trim());
        ;
        maturityToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicMaturityDateStartIndex, magicMaturityDateEndIndex)
                    .trim());

      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
        System.out.println("HAPPY: had trouble reading one of the following as an integer:");
        System.out.println(
            "yield ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(
                        magicHarvestedWeightAtHarvestStartIndex,
                        magicHarvestedWeightAtHarvestEndIndex)
                    .trim()
                + "]");
        System.out.println(
            "planting  ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicPlantingDateStartIndex, magicPlantingDateEndIndex)
                    .trim()
                + "]");
        System.out.println(
            "anthesis  ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicAnthesisDateStartIndex, magicAnthesisDateEndIndex)
                    .trim()
                + "]");
        System.out.println(
            "maturity  ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicMaturityDateStartIndex, magicMaturityDateEndIndex)
                    .trim()
                + "]");
        everythingIsValid = false;
      }

      if (everythingIsValid) {
        // yield
        happyYields.useLongValue(yieldToUse);

        // planting date
        plantingDate =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicPlantingDateStartIndex, magicPlantingDateEndIndex)
                    .trim());

        //			// emergence
        //			eventDate = emergenceToUse;
        //			daysSincePlantingForEvent =
        // DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
        //			happyEmergenceDates.useLongValue(daysSincePlantingForEvent);

        // anthesis
        eventDate = anthesisToUse;
        daysSincePlantingForEvent =
            DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
        happyAnthesisDates.useLongValue(daysSincePlantingForEvent);

        // pull out the bits we want
        // maturity
        eventDate = maturityToUse;
        daysSincePlantingForEvent =
            DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, eventDate);
        happyMaturityDates.useLongValue(daysSincePlantingForEvent);
      }
    }

    // since everything was successful, clear out the summary file
    if (summaryDSSATOutputAsFileObject.exists()) {
      this.summaryDSSATOutputAsFileObject.delete();
    }

    return new int[] {
      (int) Math.floor(happyYields.getMean()),
      (int) Math.floor(happyAnthesisDates.getMean()),
      (int) Math.floor(happyMaturityDates.getMean())
    };
  }

  private void grabNewManyResults(int nYears) throws InterruptedException, Exception {

    // magic numbers
    final int timeCheckNumber = 2;
    final long retrySleepTimeToUse = 2;
    final double multiplierForTimeUpperBound = 1.0 + 0.25;

    // declarations
    TimerUtility readingTimer = new TimerUtility();
    int plantingDate = -1;
    int daysSincePlantingForEvent = -3;

    int yieldToUse = -4;
    int emergenceToUse = -3;
    int anthesisToUse = -3;
    int maturityToUse = -3;
    double[] extractedValues = new double[extraStartIndices.length];
    boolean[] everythingIsValid = new boolean[extraStartIndices.length];

    // declarations with initializations
    boolean readSuccessfully = false;
    int nTries = 0;
    int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
    String[] candidateSummaryContents = new String[nLinesToRead];
    String stringToParse = null;

    readingTimer.tic();
    while (!readSuccessfully) {

      // determine whether we want to increase the initial wait time:
      // let's bump it up or down...
      if (nTimesSuccessfullyReadFirstTime == 0 && nTries == 1) {
        initialSleepTimeToUse =
            Math.min(
                initialSleepTimeToUse + 1,
                (long) (readingTimesReal.getMinAsLong() * multiplierForTimeUpperBound));
      } else if (nTimesSuccessfullyReadFirstTime > 0
          && nTimesSuccessfullyReadFirstTime % timeCheckNumber == 0
          && nTries == 0) {
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
        candidateSummaryContents =
            FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath, nLinesToRead);
      } catch (FileNotFoundException fnfe) {
        // check for error file
        if (errorAsFileObject.exists()) {
          System.out.println(
              "REAL: file not found... try #" + nTries + " [" + errorAsFileObject + "] exists...");
          throw fnfe;
        }
        System.out.println("REAL: file not found... try #" + nTries + " (no error file)");

        //				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
        // below will poop out
      } catch (IOException ioe) {
        System.out.println("REAL: i/o exception...  try #" + nTries);
        //				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
        // below will poop out
        throw ioe;
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        System.out.println("REAL: array index exception...  try #" + nTries);
        // carry on, since the file is still in the process of being written...
      }

      // check if the file has the right number of lines. if so, check that the last line is
      // completely there...
      //			System.out.println("REAL: try #" + nTries + " length = " +
      // candidateSummaryContents.length);
      if (candidateSummaryContents[nLinesToRead - 1] != null
          && candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength) {
        readSuccessfully = true;
        nTimesSuccessfullyReadFirstTime++;
      } else {
        // check for error file
        if (errorAsFileObject.exists()) {
          System.out.println(
              "REAL: partial read... try #" + nTries + " [" + errorAsFileObject + "] exists...");
          throw new Exception();
        }
        nTimesSuccessfullyReadFirstTime = 0;
      }
    }
    // check the time and update the timekeeping thingee
    readingTimesReal.useDoubleValue(readingTimer.tocMillis());

    // parse the output file for the necessary goodies...
    for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {

      for (int validIndex = 0; validIndex < extraStartIndices.length; validIndex++) {
        everythingIsValid[validIndex] = true; // good unless decided otherwise
      }

      try {
        // yield
        yieldToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(
                        magicHarvestedWeightAtHarvestStartIndex,
                        magicHarvestedWeightAtHarvestEndIndex)
                    .trim());
        // planting / anthesis / maturity dates
        plantingDate =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicPlantingDateStartIndex, magicPlantingDateEndIndex)
                    .trim());
        //				emergenceToUse = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
        // magicDSSATSummaryLineIndexToRead]
        //
        // .substring(magicEmergenceDateStartIndex,
        //
        //	magicEmergenceDateEndIndex).trim());;
        anthesisToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicAnthesisDateStartIndex, magicAnthesisDateEndIndex)
                    .trim());
        ;
        maturityToUse =
            Integer.parseInt(
                candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicMaturityDateStartIndex, magicMaturityDateEndIndex)
                    .trim());

        if (maturityToUse == magicMissingValue) {
          maturityToUse =
              Integer.parseInt(
                  candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                      .substring(magicHarvestingDateStartIndex, magicHarvestingDateEndIndex)
                      .trim());
        }

        // all the other goodies...
        for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
          stringToParse =
              candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                  .substring(extraStartIndices[extraIndex], extraEndIndices[extraIndex])
                  .trim();
          // check if there are the silly stars in there...
          if (stringToParse.contains("*")) {
            // ok, let's try this again, but we'll just skip over the offending value but keep the
            // others from this line...
            everythingIsValid[extraIndex] = false;
            System.out.println(
                "REAL: got ***'s in #"
                    + extraIndex
                    + this.extraNames[extraIndex]
                    + ": ["
                    + stringToParse
                    + "]");
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
        System.out.println(
            "yield ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(
                        magicHarvestedWeightAtHarvestStartIndex,
                        magicHarvestedWeightAtHarvestEndIndex)
                    .trim()
                + "]");
        System.out.println(
            "planting  ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicPlantingDateStartIndex, magicPlantingDateEndIndex)
                    .trim()
                + "]");
        //				System.out.println("anthesis  [" +
        //						candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
        //                                     .substring(magicAnthesisDateStartIndex,
        //                                     		magicAnthesisDateEndIndex).trim()
        //            + "]");
        System.out.println(
            "maturity  ["
                + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                    .substring(magicMaturityDateStartIndex, magicMaturityDateEndIndex)
                    .trim()
                + "]");

        // all the other goodies...
        for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {

          System.out.println(
              "extras ["
                  + extraIndex
                  + "] = ["
                  + candidateSummaryContents[fakeYearIndex + magicDSSATSummaryLineIndexToRead]
                      .substring(extraStartIndices[extraIndex], extraEndIndices[extraIndex])
                      .trim()
                  + "]");
        }

        //				everythingIsValid = false;
        throw new Exception();
      }

      //			if (everythingIsValid) {

      // yield
      realYieldsEntirePixel.useLongValue(yieldToUse);

      // emergence
      // we have to check whether emergence occurred...
      // Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this
      // for the moment...
      if (anthesisToUse > 0) {
        daysSincePlantingForEvent =
            DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, emergenceToUse);
        realEmergenceEntirePixel.useLongValue(daysSincePlantingForEvent);
      } else {
        //					System.out.println("grab real results: emergence failed...; not counting in
        // average");
        realBadThingsCountsEntirePixel.useLongValue(1); // assess a problem...
      }

      // anthesis
      // we have to check whether flowering occurred...
      // Beware the MAGIC NUMBER!!! assuming that "0" means no flowering... we will skip over this
      // for the moment...
      if (anthesisToUse > 0) {
        daysSincePlantingForEvent =
            DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, anthesisToUse);
        realAnthesisEntirePixel.useLongValue(daysSincePlantingForEvent);
      } else {
        //					System.out.println("grab real results: flowering failed...; not counting in
        // average");
        realBadThingsCountsEntirePixel.useLongValue(1); // assess a problem...
      }

      // maturity
      // if it's still missing..., skip over it and record as something bad happened
      if (maturityToUse != magicMissingValue) {
        daysSincePlantingForEvent =
            DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate, maturityToUse);
        if (daysSincePlantingForEvent > 300) {
          System.out.println(
              "pd = "
                  + plantingDate
                  + " ; mat = "
                  + maturityToUse
                  + " ; diff = "
                  + daysSincePlantingForEvent);
        }
        realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
      } else {
        System.out.println(
            "grab real results: maturity failed...; not counting in maturity average");
        realBadThingsCountsEntirePixel.useLongValue(10000); // assess a problem...
      }

      // all the extra bits
      for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
        if (everythingIsValid[extraIndex]) {
          extraSummaryAccumulators[extraIndex].useDoubleValue(extractedValues[extraIndex]);
        }
      }

      //			} // end if everything is valid, do the recording...

    } // end for fakeYears...

    // since this appears to have worked, clear out the summary file
    if (summaryDSSATOutputAsFileObject.exists()) {
      this.summaryDSSATOutputAsFileObject.delete();
    }
  }

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

    realYieldsEntirePixel = new DescriptiveStatisticsUtility(false);
    happyYieldsEntirePixel = new DescriptiveStatisticsUtility(false);
    realAnthesisEntirePixel = new DescriptiveStatisticsUtility(false);
    realMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
    happyMaturityEntirePixel = new DescriptiveStatisticsUtility(false);
    realBadThingsCountsEntirePixel = new DescriptiveStatisticsUtility(false);

    readingTimesReal = new DescriptiveStatisticsUtility(true);
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

    int[] nonClimateInfo =
        null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
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
    String endingDayToPlantCode = null;
    String initializationDayCode = null;
    String fertilizerBlock = null;
    String irrigationBlock = null;

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
    File statisticsFileObject = new File(yieldOutputBaseName + "_STATS.txt");
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
    // Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast
    // enough
    // that it doesn't matter. for big stuff, we'll want disk...
    int formatIndexToForce = 1;
    MultiFormatMatrix dataMatrix =
        MatrixOperations.read2DMFMfromTextForceFormat(
            gisTableBaseName + "_data", formatIndexToForce);
    MultiFormatMatrix geogMatrix =
        MatrixOperations.read2DMFMfromTextForceFormat(
            gisTableBaseName + "_geog", formatIndexToForce);

    if (geogMatrix.getDimensions()[1] != 4) {
      System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
      throw new Exception();
    }

    int nLinesInDataFile = (int) dataMatrix.getDimensions()[0];

    String cliStuffToWrite = null;
    String XStuffToWrite = null;
    String XHappyStuffToWrite = null;
    String initializationBlock = null;

    // since this implementation (using the multiple years with a single random seed, rather
    // than multiple random seeds with a single year) has the seeds and years as invariants, do
    // them up front to save on a little search and replace overhead...
    randomSeedCode = DSSATHelperMethods.padWithZeros(firstRandomSeed, 5);
    nYearsCode = DSSATHelperMethods.padWithZeros(nRandomSeedsToUse, 5);
    nHappyYearsCode = DSSATHelperMethods.padWithZeros(nHappyPlantRunsForPhenology, 5);
    co2ppmCode = DSSATHelperMethods.padWithZeros(co2ppm, 4);

    String XHappyInvariantsReplaced = happyPlantX(templateXFileContents);
    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(randomSeedPlaceholder, randomSeedCode);
    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nHappyYearsCode);
    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(weatherPlaceholder, magicWeatherStationNameToUse);
    XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(co2ppmPlaceholder, co2ppmCode);
    // and put in a dummy set of fertilizer stuff
    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(
            fertilizerPlaceholder,
            "*FERTILIZERS (INORGANIC)\n"
                + "@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD\n"
                + " 1     1 FE005 AP001     1 00000   -99   -99   -99   -99   -99\n");
    // and a dummy set of irrigation stuff

    IrrigationScheme dummyScheme = new IrriSNone();
    dummyScheme.initialize();
    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(
            irrigationPlaceholder, dummyScheme.buildIrrigationBlock());

    //	irrigationScheme

    String XInvariantsReplaced =
        templateXFileContents.replaceAll(randomSeedPlaceholder, randomSeedCode);
    XInvariantsReplaced =
        XInvariantsReplaced.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsCode);
    XInvariantsReplaced =
        XInvariantsReplaced.replaceAll(weatherPlaceholder, magicWeatherStationNameToUse);
    XInvariantsReplaced = XInvariantsReplaced.replaceAll(co2ppmPlaceholder, co2ppmCode);

    ////////////////////////////////
    // get the soil profile ready //
    ////////////////////////////////

    // Beware the MAGIC ASSUMPTION!!! the soil file is the first two characters of the soil name
    // with a .SOL
    SoilProfile soilProfiles = new SoilProfile(magicSoilPrefix.substring(0, 2) + ".SOL");

    // make a dummy initialization for the happy plant...
    String dummyInitializationBlock =
        soilProfiles.makeInitializationBlockFractionBetweenBounds(
            magicSoilPrefix + "01",
            fractionBetweenLowerLimitAndDrainedUpperLimit,
            initializationStartPlaceholder,
            nitrogenPPMforBothNH4NO2);

    XHappyInvariantsReplaced =
        XHappyInvariantsReplaced.replaceAll(
            this.soilInitializationPlaceholder, dummyInitializationBlock);

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
      realEmergenceEntirePixel.reset();
      realAnthesisEntirePixel.reset();
      realMaturityEntirePixel.reset();
      happyMaturityEntirePixel.reset();
      realBadThingsCountsEntirePixel.reset();

      for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
        extraSummaryAccumulators[extraIndex].reset();
      }

      // figure out the climate file...
      //		cliStuffToWrite = DSSATHelperMethods.cliFileContentsMFM(dataMatrix, geogMatrix, lineIndex,
      // SWmultiplier);

      cliStuffToWrite =
          DSSATHelperMethods.cliFileContentsAllSupplied(
              dataMatrix, geogMatrix, lineIndex, SWmultiplier);
      nonClimateInfo = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, lineIndex);

      dssatTimer.tic();
      FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
      totalWriteNanos += dssatTimer.TOCNanos();

      soilType = nonClimateInfo[0];
      // elevation        = nonClimateInfo[1]; // don't need this here...
      // plantingMonth    = nonClimateInfo[2]; // don't need this here...
      firstPlantingDay = nonClimateInfo[3];
      nitrogenLevel = nonClimateInfo[4];

      // brute force padding
      // Beware the MAGIC ASSUMPTION!!! assuming two digit soil codes
      if (soilType < 10 && soilType > 0) {
        soilTypeString = magicSoilPrefix + 0 + soilType;
      } else if (soilType < 100 && soilType > 0) {
        soilTypeString = magicSoilPrefix + soilType;
      } else {
        System.out.println(
            "soil type number did not meet our criteria: > 0 and < 100: " + soilType);
        throw new Exception();
      }

      // loop over the planting windows and random seeds
      for (int plantingWindowIndex = 0;
          plantingWindowIndex < nPlantingWindowsPerMonth;
          plantingWindowIndex++) {
        // pick the starting day/etc for this window
        startingDayToPlantForThisWindow =
            firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
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
        startingDayToPlantCode =
            (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
                + DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
        endingDayToPlantCode =
            (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd)
                + DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
        initializationDayCode =
            (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
                + DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow)); // YYddd

        //////////////////////////////////////
        // let's run a happy plant first... //
        //////////////////////////////////////

        // X file
        // do the search and replace thing; the invariants have already been done above...
        XHappyStuffToWrite = XHappyInvariantsReplaced.replaceAll(soilPlaceholder, soilTypeString);
        XHappyStuffToWrite =
            XHappyStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
        XHappyStuffToWrite =
            XHappyStuffToWrite.replaceAll(plantingDateStartPlaceholder, startingDayToPlantCode);
        XHappyStuffToWrite =
            XHappyStuffToWrite.replaceAll(plantingDateEndPlaceholder, endingDayToPlantCode);

        FunTricks.writeStringToFile(XHappyStuffToWrite, fullTempXFile);

        // run DSSAT with the happy plant

        Runtime.getRuntime().exec(dssatExecutionCommand, null, pathToDSSATDirectoryAsFile);

        //////////////////////////////////////////
        // extract the results & store to array //
        //////////////////////////////////////////

        // mean yield / anthesis / maturity in days after planting...
        //			phenologyInDays = this.grabHappyResults(nHappyPlantRunsForPhenology);
        //			System.out.println("       -> before grab happy " + lineIndex + " <-");
        phenologyInDays = this.grabNewHappyResults(nHappyPlantRunsForPhenology);
        //			System.out.println("       -> after grab happy " + lineIndex + " <-");

        // keep track of the happy plant yields for the fun of it...
        happyYieldsEntirePixel.useLongValue(phenologyInDays[0]);
        happyMaturityEntirePixel.useLongValue(phenologyInDays[2]);

        // proceed, if the happy yield exceeds some theshold and the effective growing season isn't
        // too long....
        if (phenologyInDays[0] >= this.happyYieldThresholdToDoRealRuns
            && phenologyInDays[2] <= this.happyMaturityThresholdToDoRealRuns) {

          /////////////////////
          // make the X file //
          /////////////////////

          // create the fertilizer block...
          fertilizerBlock =
              nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
                  phenologyInDays[1] + phenologyBufferInDays,
                  phenologyInDays[2] + phenologyBufferInDays,
                  nitrogenLevel);

          irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), nRandomSeedsToUse);
          irrigationBlock = irrigationScheme.buildIrrigationBlock();

          // do the search and replace thing; the invariants have already been done above...
          XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder, soilTypeString);
          XStuffToWrite =
              XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
          XStuffToWrite =
              XStuffToWrite.replaceAll(plantingDateStartPlaceholder, startingDayToPlantCode);
          XStuffToWrite =
              XStuffToWrite.replaceAll(plantingDateEndPlaceholder, endingDayToPlantCode);
          XStuffToWrite = XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);
          XStuffToWrite = XStuffToWrite.replaceAll(irrigationPlaceholder, irrigationBlock);

          // make the real initialization block
          initializationBlock =
              soilProfiles.makeInitializationBlockFractionBetweenBounds(
                  soilTypeString,
                  fractionBetweenLowerLimitAndDrainedUpperLimit,
                  initializationDayCode,
                  nitrogenPPMforBothNH4NO2);

          XStuffToWrite =
              XStuffToWrite.replaceAll(soilInitializationPlaceholder, initializationBlock);

          // overwrite the old file with the new contents

          dssatTimer.tic();
          FunTricks.writeStringToFile(XStuffToWrite, fullTempXFile);
          totalWriteNanos += dssatTimer.TOCNanos();

          // recommend garbage collection
          System.gc();

          ///////////////
          // run DSSAT //
          ///////////////

          Runtime.getRuntime().exec(dssatExecutionCommand, null, pathToDSSATDirectoryAsFile);
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
          // actually, if we keep the zeros, it screws up our average yields and stds (duh)
          // so, we're just going to skip it.

          // we need to set these things to zero artificially...
          //				for (int yearIndex = 0; yearIndex < nRandomSeedsToUse; yearIndex++) {
          //					thisPixelYields[plantingWindowIndex][yearIndex] = 0;
          //					realYieldsEntirePixel.useLongValue(0L);

          //					thisPixelMaturities[plantingWindowIndex][yearIndex] = -1;
          // let's skip everything else in its entirity if there were failures

          //					realMaturityEntirePixel.useLongValue(-500L);
          //
          //					for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
          //						extraSummaryAccumulators[extraIndex].useLongValue(-500L);
          //					}

          //				}
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
            0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + 0
                + delimiter
                + happyYieldsEntirePixel.getMean()
                + delimiter
                + happyMaturityEntirePixel.getMean();

        for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
          statisticsOutLine += delimiter + 0;
        }

      } else {

        statisticsOutLine =
            realBadThingsCountsEntirePixel.getN()
                + delimiter
                + realYieldsEntirePixel.getMinAsLong()
                + delimiter
                + realYieldsEntirePixel.getMaxAsLong()
                + delimiter
                + realYieldsEntirePixel.getMean()
                + delimiter
                + realYieldsEntirePixel.getStd()
                + delimiter
                + realEmergenceEntirePixel.getMean()
                + delimiter
                + realEmergenceEntirePixel.getStd()
                + delimiter
                + realAnthesisEntirePixel.getMean()
                + delimiter
                + realAnthesisEntirePixel.getStd()
                + delimiter
                + realMaturityEntirePixel.getMean()
                + delimiter
                + realMaturityEntirePixel.getStd()
                + delimiter
                + happyYieldsEntirePixel.getMean()
                + delimiter
                + happyMaturityEntirePixel.getMean();

        for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
          statisticsOutLine += delimiter + extraSummaryAccumulators[extraIndex].getMean();
        }
      }
      statisticsOutLine += "\n";
      statisticsOut.print(statisticsOutLine);
      //		statisticsOut.flush();

      // now we're ready to deal with the next pixel...
      // Beware the MAGIC NUMBER!!! checking every one percent...
      if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {
        System.out.println(
            "prog: "
                + lineIndex
                + "/"
                + nLinesInDataFile
                + " = "
                + (float) (100 * (lineIndex + 1.0) / nLinesInDataFile)
                + "% sleep now = "
                + this.initialSleepTimeToUse
                + "/"
                + this.initialSleepTimeToUseHappy
                + " ; "
                + (float) readingTimesReal.getMean()
                + "/"
                + (float) readingTimesHappy.getMean());
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
    String columnList =
        "n_bad_things"
            + delimiter
            + "yield_min"
            + delimiter
            + "yield_max"
            + delimiter
            + "yield_mean"
            + delimiter
            + "yield_std"
            + delimiter
            + "real_emergence_mean"
            + delimiter
            + "real_emergence_std"
            + delimiter
            + "real_anthesis_mean"
            + delimiter
            + "real_anthesis_std"
            + delimiter
            + "real_maturity_mean"
            + delimiter
            + "real_maturity_std"
            + delimiter
            + "happy_yield_mean"
            + delimiter
            + "happy_maturity_mean";
    for (int extraIndex = 0; extraIndex < extraStartIndices.length; extraIndex++) {
      columnList += delimiter + this.extraNames[extraIndex];
    }
    columnList += "\n";

    FunTricks.writeStringToFile(columnList, yieldOutputBaseName + "_STATS.cols.txt");

    long nRows = nLinesInDataFile;

    // Beware the MAGIC NUMBER!!!
    int nCols =
        13
            + extraStartIndices
                .length; // min / max/ mean / std / bad / happy mean / happy std / real anthesis
                         // mean / real anthesis std / real maturity mean / real maturity std /
                         // happy maturity mean / happy maturity std
    FunTricks.writeInfoFile(yieldOutputBaseName + "_STATS", nRows, nCols, delimiter);

    /////////////////////////////////////////// end new plan
    // ///////////////////////////////////////////////
    thisTimer.sinceStartMessage("running DSSAT");
    double overallTimeMillis = thisTimer.sinceStartMillis();

    System.out.println("total DSSAT = " + (float) (totalDSSATNanos / 1000000000.0));
    System.out.println("total read  = " + (float) (totalReadNanos / 1000000000.0));
    System.out.println("total write = " + (float) (totalWriteNanos / 1000000000.0));

    System.out.println();
    System.out.println("reading: " + readingTimesReal.getAllPretty());
    System.out.println();

    System.out.println(
        " total time in DSSAT loop = "
            + totalDSSATTimeMillis / 1000 / 60
            + "min ; per run average = "
            + totalDSSATTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth
            + "ms");
    System.out.println(
        "overall per run average = "
            + overallTimeMillis / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth
            + "ms");
  } // main

  public static void stupidHolder() {
    //	private void grabNewRealResults(int nYears) throws InterruptedException, Exception {
    //
    //
    //		// magic numbers
    //		final int timeCheckNumber = 2;
    //		final long retrySleepTimeToUse = 2;
    //		final double multiplierForTimeUpperBound = 1.0 + 0.25;
    //
    //
    //
    //		// declarations
    //		TimerUtility readingTimer = new TimerUtility();
    //		int plantingDate = -1;
    //		int eventDate = -2;
    //		int daysSincePlantingForEvent = -3;
    //
    //
    //		// declarations with initializations
    //		boolean readSuccessfully = false;
    //		int nTries = 0;
    //		int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
    //		String[] candidateSummaryContents = new String[nLinesToRead];
    //
    //
    //		readingTimer.tic();
    //		while (!readSuccessfully) {
    //
    //			// determine whether we want to increase the initial wait time:
    //				// let's bump it up or down...
    //			if (nTimesSuccessfullyReadFirstTime == 0 && nTries == 1) {
    //				initialSleepTimeToUse = Math.min(initialSleepTimeToUse + 1,
    // (long)(readingTimesReal.getMinAsLong() * multiplierForTimeUpperBound));
    //			} else if (nTimesSuccessfullyReadFirstTime > 0 && nTimesSuccessfullyReadFirstTime %
    // timeCheckNumber == 0 && nTries == 0) {
    //				initialSleepTimeToUse--;
    //			}
    //
    //			// do the sleeping...
    //			if (nTries == 0) {
    //				Thread.sleep(initialSleepTimeToUse);
    //			} else {
    //				Thread.sleep(retrySleepTimeToUse);
    //			}
    //
    //			nTries++;
    //
    //			// attempt to read
    //			try {
    //				candidateSummaryContents =
    // FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath,nLinesToRead);
    //			} catch (FileNotFoundException fnfe) {
    //				// check for error file
    //				if (errorAsFileObject.exists()) {
    //					System.out.println("REAL: file not found... try #" + nTries + " [" + errorAsFileObject +
    // "] exists...");
    //					throw fnfe;
    //				}
    //				System.out.println("REAL: file not found... try #" + nTries + " (no error file)");
    //
    ////				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
    // below will poop out
    //			} catch (IOException ioe) {
    //				System.out.println("REAL: i/o exception...  try #" + nTries);
    ////				candidateSummaryContents = new String[] {}; // make it an empty array so the checking
    // below will poop out
    //				throw ioe;
    //			} catch (ArrayIndexOutOfBoundsException aioobe) {
    //				System.out.println("REAL: array index exception...  try #" + nTries);
    //				// carry on, since the file is still in the process of being written...
    //			}
    //
    //			// check if the file has the right number of lines. if so, check that the last line is
    // completely there...
    ////			System.out.println("REAL: try #" + nTries + " length = " +
    // candidateSummaryContents.length);
    //			if (
    //					candidateSummaryContents[nLinesToRead - 1] != null
    //					&&
    //					candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength
    //				 ) {
    //				readSuccessfully = true;
    //				nTimesSuccessfullyReadFirstTime++;
    //			} else {
    //				// check for error file
    //				if (errorAsFileObject.exists()) {
    //					System.out.println("REAL: partial read... try #" + nTries + " [" + errorAsFileObject + "]
    // exists...");
    //					throw new Exception();
    //				}
    //				nTimesSuccessfullyReadFirstTime = 0;
    //			}
    //		}
    //		// check the time and update the timekeeping thingee
    //		readingTimesReal.useDoubleValue(readingTimer.tocMillis());
    //
    //		// parse the output file for the necessary goodies...
    //		for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
    //			// yield
    //			realYieldsEntirePixel.useLongValue(
    //					Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicHarvestedWeightAtHarvestStartIndex,
    //
    //	magicHarvestedWeightAtHarvestEndIndex).trim()) );
    //
    //			// planting date
    //			plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicPlantingDateStartIndex,
    //
    // magicPlantingDateEndIndex).trim());
    //
    //			// pull out the bits we want
    //			// maturity
    //			eventDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicMaturityDateStartIndex,
    //			                                                      		magicMaturityDateEndIndex).trim());
    //
    //			daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate,
    // eventDate);
    //			realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
    //
    //		}
    //
    //		// since this appears to have worked, clear out the summary file
    //		if (summaryDSSATOutputAsFileObject.exists()) {
    //			this.summaryDSSATOutputAsFileObject.delete();
    //		}
    //
    //	}

    /*
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
    			realAnthesisEntirePixel  = new DescriptiveStatisticsUtility(false);
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
    			String irrigationBlock  = null;

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

    			IrrigationScheme dummyScheme = new IrriSNone();
    			dummyScheme.initialize();
    			XHappyInvariantsReplaced = XHappyInvariantsReplaced.replaceAll(irrigationPlaceholder, dummyScheme.buildIrrigationBlock());



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
    	 			 realAnthesisEntirePixel.reset();
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

    						irrigationScheme.initialize(Integer.parseInt(startingDayToPlantCode), nRandomSeedsToUse);
    						irrigationBlock = irrigationScheme.buildIrrigationBlock();



    						// do the search and replace thing; the invariants have already been done above...
    						XStuffToWrite = XInvariantsReplaced.replaceAll(soilPlaceholder , soilTypeString);
    						XStuffToWrite =       XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
    						XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
    						XStuffToWrite =       XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
    						XStuffToWrite =       XStuffToWrite.replaceAll(fertilizerPlaceholder, fertilizerBlock);
    						XStuffToWrite =       XStuffToWrite.replaceAll(irrigationPlaceholder, irrigationBlock);

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
    							realYieldsEntirePixel.useLongValue(0L);

    							realAnthesisEntirePixel.useLongValue(-500L);
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
    						+ realAnthesisEntirePixel.getMean() + delimiter
    						+ realAnthesisEntirePixel.getStd() + delimiter
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
    */

    // private void grabNewRealResults(int nYears) throws InterruptedException, Exception {
    //
    //
    //			// magic numbers
    //			final int timeCheckNumber = 2;
    //			final long retrySleepTimeToUse = 2;
    //			final double multiplierForTimeUpperBound = 1.0 + 0.25;
    //
    //
    //
    //			// declarations
    //			TimerUtility readingTimer = new TimerUtility();
    //			int plantingDate = -1;
    //			int eventDate = -2;
    //			int daysSincePlantingForEvent = -3;
    //
    //
    //			// declarations with initializations
    //			boolean readSuccessfully = false;
    //			int nTries = 0;
    //			int nLinesToRead = nYears + magicDSSATSummaryLineIndexToRead;
    //			String[] candidateSummaryContents = new String[nLinesToRead];
    //
    //
    //			readingTimer.tic();
    //			while (!readSuccessfully) {
    //
    //				// determine whether we want to increase the initial wait time:
    //					// let's bump it up or down...
    //				if (nTimesSuccessfullyReadFirstTime == 0 && nTries == 1) {
    //					initialSleepTimeToUse = Math.min(initialSleepTimeToUse + 1,
    // (long)(readingTimesReal.getMinAsLong() * multiplierForTimeUpperBound));
    //				} else if (nTimesSuccessfullyReadFirstTime > 0 && nTimesSuccessfullyReadFirstTime %
    // timeCheckNumber == 0 && nTries == 0) {
    //					initialSleepTimeToUse--;
    //				}
    //
    //				// do the sleeping...
    //				if (nTries == 0) {
    //					Thread.sleep(initialSleepTimeToUse);
    //				} else {
    //					Thread.sleep(retrySleepTimeToUse);
    //				}
    //
    //				nTries++;
    //
    //				// attempt to read
    //				try {
    //					candidateSummaryContents =
    // FunTricks.readSomeLinesOfTextFileToArray(magicDSSATSummaryToReadPath,nLinesToRead);
    //				} catch (FileNotFoundException fnfe) {
    //					// check for error file
    //					if (errorAsFileObject.exists()) {
    //						System.out.println("REAL: file not found... try #" + nTries + " [" + errorAsFileObject +
    // "] exists...");
    //						throw fnfe;
    //					}
    //					System.out.println("REAL: file not found... try #" + nTries + " (no error file)");
    //
    ////					candidateSummaryContents = new String[] {}; // make it an empty array so the checking
    // below will poop out
    //				} catch (IOException ioe) {
    //					System.out.println("REAL: i/o exception...  try #" + nTries);
    ////					candidateSummaryContents = new String[] {}; // make it an empty array so the checking
    // below will poop out
    //					throw ioe;
    //				} catch (ArrayIndexOutOfBoundsException aioobe) {
    //					System.out.println("REAL: array index exception...  try #" + nTries);
    //					// carry on, since the file is still in the process of being written...
    //				}
    //
    //				// check if the file has the right number of lines. if so, check that the last line is
    // completely there...
    ////				System.out.println("REAL: try #" + nTries + " length = " +
    // candidateSummaryContents.length);
    //				if (
    //						candidateSummaryContents[nLinesToRead - 1] != null
    //						&&
    //						candidateSummaryContents[nLinesToRead - 1].length() == magicDSSATSummaryLineLength
    //					 ) {
    //					readSuccessfully = true;
    //					nTimesSuccessfullyReadFirstTime++;
    //				} else {
    //					// check for error file
    //					if (errorAsFileObject.exists()) {
    //						System.out.println("REAL: partial read... try #" + nTries + " [" + errorAsFileObject +
    // "] exists...");
    //						throw new Exception();
    //					}
    //					nTimesSuccessfullyReadFirstTime = 0;
    //				}
    //			}
    //			// check the time and update the timekeeping thingee
    //			readingTimesReal.useDoubleValue(readingTimer.tocMillis());
    //
    //			// parse the output file for the necessary goodies...
    //			for (int fakeYearIndex = 0; fakeYearIndex < nYears; fakeYearIndex++) {
    //				// yield
    //				realYieldsEntirePixel.useLongValue(
    //						Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicHarvestedWeightAtHarvestStartIndex,
    //
    //	magicHarvestedWeightAtHarvestEndIndex).trim()) );
    //
    //				// planting date
    //				plantingDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicPlantingDateStartIndex,
    //
    // magicPlantingDateEndIndex).trim());
    //
    //				// pull out the bits we want
    //				// maturity
    //				eventDate = Integer.parseInt(candidateSummaryContents[fakeYearIndex +
    // magicDSSATSummaryLineIndexToRead]
    //
    // .substring(magicMaturityDateStartIndex,
    //
    //	magicMaturityDateEndIndex).trim());
    //
    //				daysSincePlantingForEvent = DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(plantingDate,
    // eventDate);
    //				realMaturityEntirePixel.useLongValue(daysSincePlantingForEvent);
    //
    //			}
    //
    //			// since this appears to have worked, clear out the summary file
    //			if (summaryDSSATOutputAsFileObject.exists()) {
    //				this.summaryDSSATOutputAsFileObject.delete();
    //			}
    //
    //		}

  }
}
