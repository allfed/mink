package org.ORYZARunner;

import java.io.*;
import java.util.Date;
//import java.util.Date;
//import org.DSSATRunner.*;

import org.DSSATRunner.DSSATHelperMethods;
import org.DSSATRunner.SoilProfile;
import org.R2Useful.*;

public class ORYZARunnerThree {

	// some magic settings...
	private final static boolean REAL_RUN  = true;
	private final static boolean HAPPY_RUN = false;

	private static final String delimiter = "\t";
	private final static String dosNewLine = "\r\n"; // DOS-style since ORYZA is lame at the moment
	private final static boolean useDOSNewline = true;
	private final static int outputTimeStepDays = 100000; // big means we will only get day 1's results...
	private final static double shortwaveMultiplierForDSSAT = 1.0; // the thornton data is in the proper units; oryza needs to be off by 1000
	private final static int fakePlantingYear = 2001;

	private final static String endingDateDSSATPlaceholder = "eeDDD";
	private final static String randomSeedDSSATPlaceholder = "rrrrr";
	
	private String newLineToUse = null;

	private final static String standaloneCommandTemp = "./fast_standalone_weather"; // magic number!!! name of the standalone weather binary executable...

	
	// some semi-permanent settings
	private final static String rawOutputFile       = "op.dat"; 
	private final static String rerunFile     			= "reruns.dat"; 
	private final static String controlFile   			= "control.dat"; 
//	private final static String detailedResultsFile = "/dev/null";
//	private final static String modelLogFile 				= "/dev/null";
	private final static String detailedResultsFile = "res.dat";
	private final static String modelLogFile 				= "model.log";
	private final static String soilFile 						= "soilfile.dat";
	

	private final static String ORYZAWeatherPrefix = "wdssat";

	
//	private final static String DSSATWeatherOutput = "Weather.OUT";
	private final static String DSSATClimateInput  = "ZZZZ";
	private final static String DSSATWeatherOutput = DSSATClimateInput + ".WTG";
	
//	private final static String fileXTemplateForDSSATWeather     = "fallow__.SQX";
//	private final static String batchFileForDSSATWeather 				 = "for_wthr.dv4";
//	private final static String batchFileForDSSATWeatherContents = "$BATCH(CROP)" + "\n" +
//	  "@FILEX                                                                                        TRTNO     RP     SQ     OP     CO" + "\n" +
//	  fileXTemplateForDSSATWeather + "                                                                                     01      1     01      1      0";
	
	/*
	private final static String fileXTemplateForDSSATWeatherContents =
			"*EXP.DETAILS: workaround to generate weather" + "\n" +
			"" + "\n" +
			"*GENERAL" + "\n" +
			"@PEOPLE" + "\n" +
			" No-one At All" + "\n" +
			"@ADDRESS" + "\n" +
			" somewhere" + "\n" +
			"@SITE" + "\n" +
			" nomatter" + "\n" +
			"@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM........." + "\n" +
			"    450     4    30   -99   -99   -99    12     4   -99   -99" + "\n" +
			"" + "\n" +
			"*TREATMENTS                        -------------FACTOR LEVELS------------" + "\n" +
			"@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM" + "\n" +
			" 1 1 1 0 FA01.....................  1  1  0  0  1  0  0  0  0  0  0  1  1" + "\n" +
			"" + "\n" +
			"" + "\n" +
			"*CULTIVARS" + "\n" +
			"@C CR INGENO CNAME" + "\n" +
			" 1 FA IB0001 " + "\n" +
			"" + "\n" +
			"*FIELDS" + "\n" +
			"@L ID_FIELD WSTA....  FLSA  FLOB  FLDT  FLDD  FLDS  FLST SLTX  SLDP  ID_SOIL    FLNAME" + "\n" +
			" 1 GRID     " + DSSATClimateInput + "       -99   -99   -99   -99   -99   -99  -99   -99  HN_GEN0001 NONE" + "\n" +
			"@L ...........XCRD ...........YCRD .....ELEV .............AREA .SLEN .FLWR .SLAS FLHST FHDUR" + "\n" +
			" 1             -99             -99       -99               -99   -99   -99   -99 FH202     1" + "\n" +
			"" + "\n" +
			"*SOIL ANALYSIS" + "\n" +
			"_SOIL_ANALYSIS_SECTION_" + "\n" +
			"" + "\n" +
			"*INITIAL CONDITIONS" + "\n" +
			"@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME" + "\n" +
			" 1    FA 16091     0     0     1     1     0     0     0     0 00000 00000 NONE" + "\n" +
			"@C  ICBL  SH2O  SNH4  SNO3" + "\n" +
			" 1    1   0.10   0.9   1.0" + "\n" +
			"" + "\n" +
			"*PLANTING DETAILS" + "\n" +
			"@P PDATE EDATE  PPOP  PPOE  PLME  PLDS  PLRS  PLRD  PLDP  PLWT  PAGE  PENV  PLPH  SPRL" + "\n" +
			"01 01001   -99  0009  0009     S     R  0077     0  0005  0150     0    25  25.0   3.0     0" + "\n" +
			"" + "\n" +
			"*HARVEST DETAILS" + "\n" +
			"@H HDATE  HSTG  HCOM HSIZE   HPC  HBPC HNAME" + "\n" +
			"01 " + endingDateDSSATPlaceholder + "   -99   -99   -99     0     0" + "\n" +
			"" + "\n" +
			"*FERTILIZERS (INORGANIC)" + "\n" +
			"@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD FERNAME" + "\n" +
			" 1 00001 FE005 AP004    05  famt   -99   -99   -99   -99   -99 Fertilizer for maize" + "\n" +
			"" + "\n" +
			"*RESIDUES AND ORGANIC FERTILIZER " + "\n" +
			"@R RDATE  RCOD  RAMT  RESN  RESP  RESK  RINP  RDEP  RMET RENAME" + "\n" +
			"01 00001 RE003 00001 01.40   -99   -99   100    05 AP003 Manure for maize (dummy)" + "\n" +
			"" + "\n" +
			"*SIMULATION CONTROLS" + "\n" +
			"@N GENERAL     NYERS NREPS START SDATE RSEED SNAME...................." + "\n" +
			" 1 GE          00001     1     S 01001 " + randomSeedDSSATPlaceholder + " fallow" + "\n" +
			"@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL" + "\n" +
			" 1 OP              Y     N     N     N     N     N     N     N" + "\n" +
			"@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL" + "\n" +
			" 1 ME              S     S     E     R     S     C     R     1     P     S     2" + "\n" +
			"@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS" + "\n" +
			" 1 MA              R     N     N     N     R" + "\n" +
			"@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT  LONG CHOUT OPOUT" + "\n" +
			" 1 OU              N     N     N 00001     Y     N     N     N     N     N     N     N     N" + "\n" +
			"" + "\n" +
			"@  AUTOMATIC MANAGEMENT" + "\n" +
			"@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN" + "\n" +
			" 1 PL          16121 16151    40   105    30    40    10" + "\n" +
			"@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF" + "\n" +
			" 1 IR             30   000   000 GS000 IR001    10     1" + "\n" +
			"@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF" + "\n" +
			" 1 NI             30    50    25 FE001 GS000" + "\n" +
			"@N RESIDUES    RIPCN RTIME RIDEP" + "\n" +
			" 1 RE          00100     1 00020" + "\n" +
			"@N HARVEST     HFRST HLAST HPCNP HPCNR" + "\n" +
			" 1 HA          16001 17365     0     0" + "\n" +
			"" + "\n";
*/
	private final static String[] colNamesReal = {
			"RUNNUM",
			"WRR14",
			"WSO",
			"WAGT",
			"PARCUM",
			"TS",
			"TMAXC",
			"TMINC",
			"TAVERC",
			"RAINCUM",
			"IRCUM",
			"RUNOFCUM",
			"TRWCUM",
			"EVSWCUM",
			"DRAICUM",
			"CAPTOTCUM",
			"DSWC",
			"EMD",
			"DAE"
		};

	private final static String[] OLDcolNamesReal = {
		"RUNNUM",
		"ETDCUM",
		"EVSCCUM",
		"TRCCUM",
		"WRR14",
		"WSO",
		"WAGT",
		"PARCUM",
		"TS",
		"TMAXC",
		"TMINC",
		"TAVERC",
		"RAINCUM",
		"IRCUM",
		"RUNOFCUM",
		"TRWCUM",
		"EVSWCUM",
		"DRAICUM",
		"CAPTOTCUM",
		"DSWC",
		"EMD",
		"DAE"
	};

	private final static String[] OLDcolNamesHappy = {
			"RUNNUM",
			"ETDCUM",
			"EVSCCUM",
			"TRCCUM",
			"WRR14",
			"WSO",
			"WAGT",
			"PARCUM",
			"TS",
			"TMAXC",
			"TMINC",
			"TAVERC",
			"EMD",
			"DAE"
	};
	
	private final static String[] colNamesHappy = {
		"RUNNUM",
		"WRR14",
		"WSO",
		"WAGT",
		"PARCUM",
		"TS",
		"TMAXC",
		"TMINC",
		"TAVERC",
		"EMD",
		"DAE"
};
	
	
	
	
	
//	private final static File rawOutputFileAsFile = new File(rawOutputFile);
//	private final static File weatherOutputFileAsFile = new File(DSSATWeatherOutput);

	private static final int nDaysInMonth =  30;


	// this is for the output, actually
	int[] colsToPull = {1,2,3,5,6,7,8,9,10,11,12,13,14,15,16,17,18}; // ,19,20};

	private final static int nOutputVariablesHappy = colNamesHappy.length; // 14; // 13;
	private final static int nOutputVariablesReal  = colNamesReal.length; // 22; // 13;
	private final static int commercialWeightIndex = 2; // 4;
	private final static int happyMaturityIndex = 10; // 13;
	private final static int realMaturityIndex  = 18; // 21;
//	private final static int[] OLDfixedStartsReal  = {0,11,23,35,47,59,72,83,95, 107,119,131,143,155,167,179,191,203,215,227,239,251,263};
//	private final static int[] fixedStartsReal     = {0,13,26,39,52,65,78,91,104,117,130,137,155,169,182,195,208,221,234,247,260,273,286,299};
//	private final static int[] fixedStartsHappy = fixedStartsReal;
	
	// some variables we'd like to use
	private DescriptiveStatisticsUtility happyYieldThisWindowOnly = null;
	private DescriptiveStatisticsUtility[] summaryAccumulatorsHappy = null;
	private DescriptiveStatisticsUtility[] summaryAccumulatorsReal = null;
	
	private DescriptiveStatisticsUtility timeForWeather = null;
	private DescriptiveStatisticsUtility timeForORYZA = null;
	
	private TimerUtility everythingTimer = null;
	private TimerUtility weatherTimer = null;
	private TimerUtility oryzaTimer = null;

	// actual things to be set eventually
	private boolean readyToRun = false;

	private String realControlsFile 	= "Arealcontrols.dat";
	private String varietyFile 				= "Avariety.dat";
	private String initFileName 	= null;

	String oryzaExecutionCommandTemp = null;
	String dssatBinaryNameTemp = null;

	private String[] oryzaExecutionCommand = null;
//	private String[] dssatExecutionCommand = null;
	private int firstRandomSeed = -1;
	private int nHappyRepetitions = -1;
	private int nRealRepetitions   = -2;
	private double happyYieldThresholdToDoRealRuns = -3.1;
	private int    nPlantingWindowsPerMonth = -5;
	private String gisTableBaseName = null;
	private String magicSoilPrefix = null;
	private double SWmultiplier = -8234.51324;
	private String yieldOutputBaseName = null;
	
	private boolean doPuddling = false;
	private boolean useWaterContent = false;
	private double fractionBetweenLowerLimitAndDrainedUpperLimit = -5;
	private double depthForNitrogen = -6;
	private double residueNitrogenPercent = -7;
	private double co2ppm = -7;

	
	SoilProfile testProfileObject = null;
	
	


	/////////////////////
	// the constructor //
	/////////////////////
	
	public ORYZARunnerThree() throws FileNotFoundException {

		// we might as well put the official choice in here...
		if (this.useDOSNewline) {
			newLineToUse = this.dosNewLine;
		} else {
			newLineToUse = "\n"; // whatever is native...
		}

		// write out the batch file for DSSAT so we only have to do it once...
//		FunTricks.writeStringToFile(batchFileForDSSATWeatherContents, batchFileForDSSATWeather);

		// set up some timers/etc for the fun of it.
		timeForWeather = new DescriptiveStatisticsUtility(true);
		timeForORYZA = new DescriptiveStatisticsUtility(true);

		everythingTimer = new TimerUtility();
		weatherTimer = new TimerUtility();
		oryzaTimer = new TimerUtility();

		readyToRun = false;
	}

	//////////////////////////////
	// some getters and setters //
	//////////////////////////////
	
	private void setOryzaExecutionCommand(String commandString) {
		// assume that it is spaced out properly, so we can just split it up that way
		String[] splitInput = commandString.split(" ");
		
		// assign it to the real variable
		String[] finalInput = null;
		if (splitInput.length < 2) {
			// we only have the name of the program, but not the control.dat part.
			// it should really be put in here because it is a magic number....
			finalInput = new String[splitInput.length + 1];
			for (int splitIndex = 0; splitIndex < splitInput.length; splitIndex++) {
				finalInput[splitIndex] = splitInput[splitIndex];
			}
			finalInput[splitInput.length] = controlFile;
		} else {
			// just keep whatever was provided...
			System.out.println("        --- keeping provided ORYZA execution command [" + commandString + "]; magic control file name = [" + controlFile + "]");
			finalInput = splitInput;
		}
//		oryzaExecutionCommand = splitInput;
		oryzaExecutionCommand = finalInput;
	}

	/*
	private void setDssatExecutionCommand(String binaryName) {
		// assume that it is spaced out properly, so we can just split it up that way
		dssatExecutionCommand = new String[3];
		
		// assign it to the real variable
		dssatExecutionCommand[0] = binaryName;
		dssatExecutionCommand[1] = "q";
		dssatExecutionCommand[2] = batchFileForDSSATWeather;
	}
*/
	//////////////////////////
	// some helpful methods //
	//////////////////////////

	public void readInitializationFile(String filename) throws IOException, Exception {

		initFileName = filename;
		String[] initFileContents = FunTricks.readTextFileToArray(filename);

		// read in the raw info
		int storageIndex = 0;
		
		oryzaExecutionCommandTemp = initFileContents[storageIndex++];
		dssatBinaryNameTemp = initFileContents[storageIndex++];

//		fakePlantingYear    = Integer.parseInt(initFileContents[storageIndex++]);

		nHappyRepetitions   = Integer.parseInt(initFileContents[storageIndex++]);
		happyYieldThresholdToDoRealRuns = Double.parseDouble(initFileContents[storageIndex++]);
		
		firstRandomSeed          = Integer.parseInt(
				initFileContents[storageIndex++]);
		nRealRepetitions    = Integer.parseInt(initFileContents[storageIndex++]);
		nPlantingWindowsPerMonth    = Integer.parseInt(initFileContents[storageIndex++]);
		
		gisTableBaseName         = initFileContents[storageIndex++];
		magicSoilPrefix          = initFileContents[storageIndex++];

		realControlsFile 	       = initFileContents[storageIndex++];
		varietyFile 				     = initFileContents[storageIndex++];

		SWmultiplier             = Double.parseDouble(initFileContents[storageIndex++]);
		
		yieldOutputBaseName      = initFileContents[storageIndex++];
		
		
		doPuddling               = Boolean.parseBoolean(initFileContents[storageIndex++]);
		useWaterContent          = Boolean.parseBoolean(initFileContents[storageIndex++]);
		fractionBetweenLowerLimitAndDrainedUpperLimit = Double.parseDouble(initFileContents[storageIndex++]);
		depthForNitrogen         = Double.parseDouble(initFileContents[storageIndex++]);
		residueNitrogenPercent   = Double.parseDouble(initFileContents[storageIndex++]);

		co2ppm                   = Double.parseDouble(initFileContents[storageIndex++]);

		
		
		// we might as well set up the soil profile here...
		System.out.println("   trying to initialize soil object; prefix = [" + magicSoilPrefix + "]");
		testProfileObject = new SoilProfile(magicSoilPrefix.substring(0, 2) + ".SOL");
/*
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

*/		
		
//		now prepare the dependent magic numbers...
		this.setOryzaExecutionCommand(oryzaExecutionCommandTemp);
//		this.setDssatExecutionCommand(dssatBinaryNameTemp);


/*
 		magicWeatherStationNameToUsePath = pathToDSSATDirectory + magicWeatherStationNameToUse + ".CLI";
		magicDSSATSummaryToReadPath      = pathToDSSATDirectory + magicDSSATSummaryToRead;
		summaryDSSATOutputAsFileObject   = new File(magicDSSATSummaryToReadPath);
		errorAsFileObject                = new File(pathToDSSATDirectory + magicErrorFile);
		magicInitializationFilePath      = pathToDSSATDirectory + magicInitializationFile;
*/
		// the first variable is just the run number that we don't care about. alas, we will
		// end up with an extra statistical thing, but we'll just carry it along for simplicity
		// although it will be unused...
		happyYieldThisWindowOnly = new DescriptiveStatisticsUtility(true);
		summaryAccumulatorsHappy = new DescriptiveStatisticsUtility[nOutputVariablesHappy];
		summaryAccumulatorsReal  = new DescriptiveStatisticsUtility[nOutputVariablesReal];
		for (int outputIndex = 0; outputIndex < nOutputVariablesHappy; outputIndex++) {
			summaryAccumulatorsHappy[outputIndex] = new DescriptiveStatisticsUtility(true); // the results are all doubles and stuff
		}
		for (int outputIndex = 0; outputIndex < nOutputVariablesReal; outputIndex++) {
			summaryAccumulatorsReal[outputIndex]  = new DescriptiveStatisticsUtility(true); // the results are all doubles and stuff
		}

//		mark that we're good to go...
		readyToRun = true;
	}
	
	private void resetAccumulators(boolean realFlag) {
		if (realFlag == this.REAL_RUN) {
			for (int outputIndex = 0; outputIndex < nOutputVariablesReal; outputIndex++) {
				summaryAccumulatorsReal[outputIndex].reset();
			}
		} else {
			for (int outputIndex = 0; outputIndex < nOutputVariablesHappy; outputIndex++) {
				summaryAccumulatorsHappy[outputIndex].reset();
			}
		}
	}


	private void prepareHappyRunUnique(int plantingDay) throws FileNotFoundException {
		// prepare the control file
		// Beware the MAGIC NUMBER!!! almost everything. some of these should eventually get split out so
		// as to be able to be controlled and/or not be so magic.

		int nHappyRepetitionsToUse = 1;
		
//		String controlFileContents = "*CONTROLFILE =" + newLineToUse;
		String controlFileContents = "*CONTROLFILE =CONTROL.DAT" + newLineToUse;
		controlFileContents += "STRUN = 1" + newLineToUse;
		controlFileContents += "ENDRUN = " + nHappyRepetitionsToUse + newLineToUse;
		controlFileContents += "FILEON = '" + detailedResultsFile + "'    ! Output file" + newLineToUse;
		controlFileContents += "FILEOL = '" + modelLogFile + "'      ! Log file" + newLineToUse;
		controlFileContents += "FILEIR = '" + rerunFile + "'" + newLineToUse;
		controlFileContents += "FILEIT = '" + realControlsFile + "'" + newLineToUse;
		controlFileContents += "FILEI1 = '" + varietyFile + "'" + newLineToUse;
		controlFileContents += "FILEI2 = '" + soilFile + "'" + newLineToUse;
		controlFileContents += "*----------------------------------------------------------------------*" + newLineToUse;
		controlFileContents += "* Set output/print options                                             *" + newLineToUse;
		controlFileContents += "*----------------------------------------------------------------------*" + newLineToUse;
		controlFileContents += "PRDEL  = " + outputTimeStepDays + ".    ! Output time step (day)" + newLineToUse;
		controlFileContents += "IPFORM = 5     ! Code for output table format:" + newLineToUse;
		controlFileContents += "COPINF = 'N'   ! Switch variable whether to copy the input files" + newLineToUse;
		controlFileContents += "! to the output file ('N' = do not copy," + newLineToUse;
		controlFileContents += "! 'Y' = copy)" + newLineToUse;
		controlFileContents += "DELTMP = 'N'   ! Switch variable what should be done with the" + newLineToUse;
		controlFileContents += "! temporary output file ('N' = do not delete," + newLineToUse;
		controlFileContents += "! 'Y' = delete)" + newLineToUse;
		controlFileContents += "IFLAG  = 1100  ! Indicates where weather error and warnings" + newLineToUse;
		controlFileContents += "! go (1101 means errors and warnings to log" + newLineToUse;
		controlFileContents += "! file, errors to screen, see FSE manual)" + newLineToUse;

		FunTricks.writeStringToFile(controlFileContents, controlFile);

		// now prepare the rerun file
		String happyRerunContents = "* --- HAPPY plant runs ---" + newLineToUse;
		for (int fakeYearIndex = 0; fakeYearIndex < nHappyRepetitionsToUse; fakeYearIndex++) {
			happyRerunContents += "*Rerun set #" + fakeYearIndex 										+ newLineToUse;
			happyRerunContents += "PRODENV = 'POTENTIAL'" 													+ newLineToUse;
			happyRerunContents += "NITROENV = 'POTENTIAL'        ! Potential production" + newLineToUse;
			happyRerunContents += "IYEAR  = " + (fakePlantingYear + fakeYearIndex) 	+ newLineToUse;
			happyRerunContents += "STTIME = " + plantingDay + "." 									+ newLineToUse;
			happyRerunContents += "EMD    = " + plantingDay 												+ newLineToUse;
			happyRerunContents += "EMYR   = " + (fakePlantingYear + fakeYearIndex) 	+ newLineToUse;
			happyRerunContents += "CO2    = " + co2ppm + " ! Ambient CO2 concentration (ppm)" + newLineToUse;
		}

		FunTricks.writeStringToFile(happyRerunContents, rerunFile);
	}


  private void prepareRealRunUnique(int plantingDay, double fertilizerAmount) throws FileNotFoundException, Exception {
		
		// prepare the control file
		// Beware the MAGIC NUMBER!!! almost everything. some of these should eventually get split out so
		// as to be able to be controlled and/or not be so magic.

		int nRealRepetitionsToUse = 1;
		String controlFileContents = "*CONTROLFILE =CONTROL.DAT" + newLineToUse;
		controlFileContents += "STRUN = 1" + newLineToUse;
		controlFileContents += "ENDRUN = " + nRealRepetitionsToUse + newLineToUse;
		controlFileContents += "FILEON = '" + detailedResultsFile + "'    ! Output file" + newLineToUse;
		controlFileContents += "FILEOL = '" + modelLogFile + "'      ! Log file" + newLineToUse;
		controlFileContents += "FILEIR = '" + rerunFile + "'" + newLineToUse;
		controlFileContents += "FILEIT = '" + realControlsFile + "'" + newLineToUse;
		controlFileContents += "FILEI1 = '" + varietyFile + "'" + newLineToUse;
		controlFileContents += "FILEI2 = '" + soilFile + "'" + newLineToUse;
		controlFileContents += "*----------------------------------------------------------------------*" + newLineToUse;
		controlFileContents += "* Set output/print options                                             *" + newLineToUse;
		controlFileContents += "*----------------------------------------------------------------------*" + newLineToUse;
		controlFileContents += "PRDEL  = " + outputTimeStepDays + ".    ! Output time step (day)" + newLineToUse;
		controlFileContents += "IPFORM = 5     ! Code for output table format:" + newLineToUse;
		controlFileContents += "COPINF = 'N'   ! Switch variable whether to copy the input files" + newLineToUse;
		controlFileContents += "! to the output file ('N' = do not copy," + newLineToUse;
		controlFileContents += "! 'Y' = copy)" + newLineToUse;
		controlFileContents += "DELTMP = 'N'   ! Switch variable what should be done with the" + newLineToUse;
		controlFileContents += "! temporary output file ('N' = do not delete," + newLineToUse;
		controlFileContents += "! 'Y' = delete)" + newLineToUse;
		controlFileContents += "IFLAG  = 1100  ! Indicates where weather error and warnings" + newLineToUse;
		controlFileContents += "! go (1101 means errors and warnings to log" + newLineToUse;
		controlFileContents += "! file, errors to screen, see FSE manual)" + newLineToUse;

		FunTricks.writeStringToFile(controlFileContents, controlFile);

		// now prepare the rerun file
		double magicFirstDayForFertilizer = 28; // i think that the first 21 days are in the nursery
		double magicLastDayForFertilizer = 28 + 14; // i think that the first 21 days are in the nursery
		String realRerunContents = "* --- REAL plant runs ---" + newLineToUse;
		for (int fakeYearIndex = 0; fakeYearIndex < nRealRepetitionsToUse; fakeYearIndex++) {
			realRerunContents += "*Rerun set #" + fakeYearIndex 										+ newLineToUse;
			realRerunContents += "PRODENV = 'WATER BALANCE'"												+ newLineToUse;
			realRerunContents += "IYEAR  = " + (fakePlantingYear + fakeYearIndex) 	+ newLineToUse;
			realRerunContents += "STTIME = " + plantingDay + "." 									+ newLineToUse;
			realRerunContents += "EMD    = " + plantingDay 												+ newLineToUse;
			realRerunContents += "EMYR   = " + (fakePlantingYear + fakeYearIndex) 	+ newLineToUse;
			realRerunContents += "CO2    = " + co2ppm + " ! Ambient CO2 concentration (ppm)" + newLineToUse;



			// need to add in the fertilizer application scheme...
//			* Table of fertilizer rate (kg N/ha) (second column) versus days after sowing
//			* in the seed-bed (!) (first column)
//			FERTIL = 36., 0.,
//			57., 40.
			
			// let's take a stab at it and we can always revise later...
			//
			// on 04 oct 13, tao li thought that he had made some useful adjustments to my setup files.
			// one of these turned out to be bracketing the fertilizer application days with zero application
			// the day before and after...
			if (fertilizerAmount < 60) {
				realRerunContents += "FERTIL = " + 
				(magicFirstDayForFertilizer - 1) + ",0.0,"+ newLineToUse +
				magicFirstDayForFertilizer + "," + fertilizerAmount + "," + newLineToUse +
				(magicFirstDayForFertilizer + 1) + ",0.0" + newLineToUse;
			} else {
//				realRerunContents += "FERTIL = " + magicFirstDayForFertilizer + "," + (0.6 * fertilizerAmount) + "," + newLineToUse;
//				realRerunContents += magicLastDayForFertilizer + "," + (fertilizerAmount - 0.6 * fertilizerAmount) + newLineToUse;

				realRerunContents += "FERTIL = " + 
				(magicFirstDayForFertilizer - 1) + ",0.0," + newLineToUse +
				magicFirstDayForFertilizer + "," + (0.6 * fertilizerAmount) + "," + newLineToUse +
				(magicFirstDayForFertilizer + 1) + ",0.0," + newLineToUse +

				(magicLastDayForFertilizer - 1) + ",0.0," + newLineToUse +
				magicLastDayForFertilizer + "," + (fertilizerAmount - 0.6 * fertilizerAmount) + "," + newLineToUse +
				(magicLastDayForFertilizer + 1) + ",0.0" + newLineToUse;

			}

			
		}

		FunTricks.writeStringToFile(realRerunContents, rerunFile);

	}		

	private void prepareSoil(
			MultiFormatMatrix dataMatrix,
			long dataLineIndex,
			String profileToUse,
			boolean doPuddling,
			boolean useWaterContent,

			double fractionBetweenLowerLimitAndDrainedUpperLimit,
			double depthForNitrogen,
			double residueNitrogenPercent
	) throws FileNotFoundException, Exception {

		// soil initializations
		
		// Beware the MAGIC ASSUMPTION!!! using annual average temperature as the annual average topsoil temperature
		// Beware the MAGIC NUMBER!!! column number for annual average temperature...
		long annualTemperatureColumn = 67;
		double annualAverageTopSoilTemperature = dataMatrix.getValue(dataLineIndex,annualTemperatureColumn);

		// Beware the MAGIC NUMBER!!! the planting month is in column #3/index 2
		long plantingMonthColumn = 2;
		int startingMonth = (int)dataMatrix.getValue(dataLineIndex,plantingMonthColumn);

		// Beware the MAGIC ASSUMPTION!!! i'll use the previous month's average as the starting soil temperature
		// Beware the MAGIC NUMBER!!! column number for january average temperature...
		long januaryMaximumTempColumn = 16;
		long januaryMinimumTempColumn = 28;
		double startTopSoilTemperature = (
				dataMatrix.getValue(dataLineIndex,januaryMaximumTempColumn + startingMonth - 1 - 1) +
				dataMatrix.getValue(dataLineIndex,januaryMinimumTempColumn + startingMonth - 1 - 1)
				) / 2;

		// Beware the MAGIC NUMBER!!! column number for initial mineral soil nitrogen
		long initialNitrogenColumn = 70;
		double totalInitialNitrogenKgPerHa = dataMatrix.getValue(dataLineIndex,initialNitrogenColumn);
		
		// Beware the MAGIC NUMBER!!! column number for initial root weight
		long initialRootWeightColumn = 71;
		double rootWeight= dataMatrix.getValue(dataLineIndex,initialRootWeightColumn);

		String soilProfileString = this.testProfileObject.convertProfileToORYZA(
				profileToUse, doPuddling, useWaterContent,  annualAverageTopSoilTemperature, startTopSoilTemperature,
				fractionBetweenLowerLimitAndDrainedUpperLimit, totalInitialNitrogenKgPerHa, depthForNitrogen, rootWeight, 
				residueNitrogenPercent, this.newLineToUse);

		FunTricks.writeStringToFile(soilProfileString, this.soilFile);

	}


	private void readResultsFileSpecifyLength(boolean realFlag, int nRepetitions) throws IOException, Exception {
//	String[] lineAsArray = null;
	String[] candidateSummaryContents = FunTricks.readTextFileToArray(rawOutputFile);

	// first check if there are as many lines as we are expecting...
	if (candidateSummaryContents.length != (nRepetitions + 1)) {
		System.out.println("The summary file [" + rawOutputFile + "] has the wrong number of lines (REAL).");
		System.out.println("Expecting " + (nRealRepetitions + 1) + ", but found " + candidateSummaryContents.length);
		throw new Exception();
	}

	// if we are doing a happy read, then we need to reset the happy yield only counter
	// because it is supposed to be fresh every time so that we can decide whether to do
	// a real run for that window. the other accumulators should be over the whole month....
	happyYieldThisWindowOnly.reset();
	
	// Beware the MAGIC NUMBER!!! we are starting on the second line since the first one is a header....
	for (int lineIndex = 1; lineIndex < candidateSummaryContents.length; lineIndex++) {

		// Beware the MAGIC NUMBER!!! we are starting on the second output variable since the first one
		// is merely the run number that we don't care about....
		
		// the irregularity of the output format is beginning to stink badly enough that i am going to brute force
		// a parser...
		String[] rawSplitLine = candidateSummaryContents[lineIndex].split(" ");
		
		// now count up the number of non-empties
		int nValid = 0;
		for (int splitIndex = 0; splitIndex < rawSplitLine.length; splitIndex++) {
			if (!rawSplitLine[splitIndex].isEmpty()) {
				nValid++;
			}
		}
		String[] onlyValid = new String[nValid];
		int storageIndex = 0;
		for (int splitIndex = 0; splitIndex < rawSplitLine.length; splitIndex++) {
			if (!rawSplitLine[splitIndex].isEmpty()) {
				onlyValid[storageIndex] = rawSplitLine[splitIndex];
				storageIndex++;
			}
		}
		
		if (realFlag == this.REAL_RUN) {
			for (int outputIndex = 1; outputIndex < nOutputVariablesReal; outputIndex++) {
				summaryAccumulatorsReal[outputIndex].useDoubleValue(Double.parseDouble(
						onlyValid[outputIndex]
						));
//				candidateSummaryContents[lineIndex].substring(fixedStartsReal[outputIndex], fixedStartsReal[outputIndex+1]))
//				summaryAccumulatorsReal[outputIndex].useDoubleValue(Double.parseDouble(lineAsArray[outputIndex]));
			}
		} else {
//			lineAsArray = candidateSummaryContents[lineIndex].trim().split(" +");
			// pull out just the happy yield in a separate accumulator so we can keep the planting windows separate...
//			happyYieldThisWindowOnly.useDoubleValue(Double.parseDouble(lineAsArray[commercialWeightIndex]));
			happyYieldThisWindowOnly.useDoubleValue(Double.parseDouble(
					onlyValid[commercialWeightIndex])); // ? need a minus one to make sure we don't run off the end?
//			candidateSummaryContents[lineIndex].substring(fixedStartsHappy[commercialWeightIndex], fixedStartsHappy[commercialWeightIndex+1] - 1))); // ? need a minus one to make sure we don't run off the end?
			for (int outputIndex = 1; outputIndex < nOutputVariablesHappy; outputIndex++) {
				summaryAccumulatorsHappy[outputIndex].useDoubleValue(Double.parseDouble(
						onlyValid[outputIndex]) // ? need a minus one to make sure we don't run off the end?
						);
//				candidateSummaryContents[lineIndex].substring(fixedStartsHappy[outputIndex], fixedStartsHappy[outputIndex+1] - 1)) // ? need a minus one to make sure we don't run off the end?
//				summaryAccumulatorsHappy[outputIndex].useDoubleValue(Double.parseDouble(lineAsArray[outputIndex]));
			}
		} // if (realFlag)
	} // for lineIndex

}


	private void buildORYZAWeatherFromDSSATSpecifySeed(MultiFormatMatrix dataTable,
			MultiFormatMatrix geogTable, long lineIndex, String randomSeedCode)
	    throws Exception         {
		
		// build the climate file...
		String climateFileContents = DSSATHelperMethods.cliFileContentsAllSupplied(dataTable, geogTable, lineIndex, shortwaveMultiplierForDSSAT);

		// write out the climate file
		FunTricks.writeStringToFile(climateFileContents, DSSATClimateInput + ".CLI");
		
		// build and write the fileX

		// determine the ending date
		// the ending date is just the number of repetitions (plus one in case we run over) as the year and 365 as the day...
		
		// Beware the MAGIC NUMBER!!! We will use a finite number of years since we only need one growing season's worth here...
		int nRepetitionsToUse = 3; // we might overrun the end of the year and then need to go another year for bad cases...

		String startingDate = "01001"; // Beware the MAGIC NUMBER!!!

		String yearCode = DSSATHelperMethods.pad2CharactersZeros(nRepetitionsToUse);
		String dayCode = "365"; // Beware the MAGIC NUMBER!!! we are going to the approximate last day of the year...
			
		String endingDate = yearCode + dayCode;
//		String actualXFile = fileXTemplateForDSSATWeatherContents
//															.replaceAll(this.endingDateDSSATPlaceholder, endingDate)
//															.replaceAll(randomSeedDSSATPlaceholder, randomSeedCode);
//		
//		FunTricks.writeStringToFile(actualXFile, this.fileXTemplateForDSSATWeather);
				
		// execute DSSAT
		// i don't know that we really need to delete this...
//		weatherOutputFileAsFile.delete(); // clear out any old results file...
//		Process weatherExecution = Runtime.getRuntime().exec(dssatExecutionCommand , null , null);
		String[] standaloneWeatherCommand = 
		{
				standaloneCommandTemp,
				startingDate,
				endingDate,
				randomSeedCode,
				"S",
				DSSATClimateInput + ".CLI"
		};

//		System.out.println("weather command is:");
//		for (int index = 0; index < standaloneWeatherCommand.length; index++) {
//			System.out.println("[" + standaloneWeatherCommand[index] + "]");
//		}
				
		
		// execute DSSAT
		// i don't know that we really need to delete this...
//		weatherOutputFileAsFile.delete(); // clear out any old results file...
		Process weatherExecution = Runtime.getRuntime().exec(standaloneWeatherCommand , null , null);

		// try a simple way to wait for the run to finish...
		weatherExecution.waitFor();
		
		// interpret the output
//		this.convertDSSATWeatherToORYZA(dataTable, geogTable, lineIndex);
		this.convertStandaloneWeatherToORYZA(dataTable, geogTable, lineIndex);
		
		// make sure the streams got closed for the process after we managed to read everything...
		weatherExecution.getErrorStream().close();
		weatherExecution.getInputStream().close();
		weatherExecution.getOutputStream().close();
		weatherExecution.destroy(); // just in case it is still going...
		
		// recommend garbage collection
		System.gc();
	}

	
	/*
	private void convertDSSATWeatherToORYZA(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable, long lineIndex)
					throws FileNotFoundException, IOException, Exception {
		int firstLineIndexWithData = 12; // Beware the MAGIC NUMBER!!! it happens that 01001 starts us on year 2000, day 366
		                                 // so, we want to start on 2001/001 which is line #13 or index 12
		String stationNumber = "    1"; // Beware the MAGIC NUMBER!!! using padded 1 as the the station number
		
		String headerLine = null;
		String oryzaStyle = null;
//		String singleLineToBuild = null;
		String[] splitLine = null;
		
		int previousYear = -5;
		int statedYear = -1;
		int lastThreeOfYear = -4;
		int statedDay = -1;
		double sradRaw = -0.3;
		double sradRescaled = -0.32;
		double tmin = 5;
		double tmax = 2;
		double vaporPressure = -99; // Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double windSpeed = -99; // Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double rain = -2432.231;

		
		String[] weatherFileArray = FunTricks.readTextFileToArray(DSSATWeatherOutput);
		
		String outputFilename = ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(1, 3);
		
		// make the header line for the oryza style...
		// long, lat, elevation, angstrom A, angstrom B (zeros in our case)
		double latitude  = geogTable.getValue(lineIndex,2);
		double longitude = geogTable.getValue(lineIndex,3);
		double elevation = dataTable.getValue(lineIndex,1);

		String latString  = FunTricks.fitInNCharacters(latitude, 6);
		String longString = FunTricks.fitInNCharacters(longitude,6);
		String elevString = FunTricks.fitInNCharacters(elevation,6);
		String angString  = FunTricks.fitInNCharacters(0.0,      6); // Beware the MAGIC NUMBER!!! we are using zeros for the angstrom parameters since we are providing radiation amounts
		headerLine = longString + "," + latString + "," + elevString + "," + angString + "," + angString + newLineToUse ;

		previousYear = -10; // initialize with a junk value
		oryzaStyle = ""; // initialize with blank
		
		
		for (int wLineIndex = firstLineIndexWithData; wLineIndex < weatherFileArray.length; wLineIndex++) {
			splitLine = weatherFileArray[wLineIndex].trim().split(" +");
			
			statedYear = Integer.parseInt(  splitLine[0]);
			statedDay  = Integer.parseInt(  splitLine[1]);
			rain = Double.parseDouble(splitLine[3]);
			sradRaw    = Double.parseDouble(splitLine[6]);
			sradRescaled = sradRaw * SWmultiplier;
			tmax = Double.parseDouble(splitLine[9]);
			tmin = Double.parseDouble(splitLine[10]);

			// Beware the MAGIC NUMBER!!! we are gonna hard code this and see if it is faster than splitting/etc
//			statedYear = Integer.parseInt(  weatherFileArray[wLineIndex].substring(1,5).trim() );
//			statedDay  = Integer.parseInt(  weatherFileArray[wLineIndex].substring(6,9).trim() );
//			rain = Double.parseDouble(  weatherFileArray[wLineIndex].substring(16,22).trim() );
//			sradRaw    = Double.parseDouble(  weatherFileArray[wLineIndex].substring(37,43).trim() );
//			sradRescaled = sradRaw * SWmultiplier;
//			tmax = Double.parseDouble(  weatherFileArray[wLineIndex].substring(58,64).trim() );
//			tmin = Double.parseDouble(  weatherFileArray[wLineIndex].substring(65,71).trim() );

			
			if (statedYear != previousYear) {
				// we are done with a whole year, so write out what we have and start the next one...
				// because i'm lazy, we'll end up with one extra file with a junk name....

				// pick the appropriate filename
				// we need the last three digits of the year number
				lastThreeOfYear = previousYear % 1000;
				// Beware the MAGIC NUMBER!!! we will always assume station #1
				outputFilename = ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(lastThreeOfYear, 3);
				
				// write out what we have
				FunTricks.writeStringToFile(oryzaStyle, outputFilename);
				
				// update the previous year holder
				previousYear = statedYear;
				
				// reinitialize with the headerline
				oryzaStyle = headerLine; // initialize with header
			}
			// Beware the MAGIC NUMBER!!! the widths and spacing and what not are what they are but should only matter for the station, year, and day...
//			oryzaStyle += stationNumber + "," 
//								+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedYear), 4) + "," 
//								+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedDay), 3) + ","
//								+ FunTricks.fitInNCharacters(sradRescaled,9) + ","
//								+ FunTricks.fitInNCharacters(tmin,6) + ","
//								+ FunTricks.fitInNCharacters(tmax,6) + ","
//								+ FunTricks.fitInNCharacters(vaporPressure,6) + ","
//								+ FunTricks.fitInNCharacters(windSpeed,6) + ","
//								+ FunTricks.fitInNCharacters(rain,6) + newLineToUse;

			oryzaStyle += stationNumber + "," 
										+ statedYear + "," 
										+ statedDay + ","
										+ sradRescaled + ","
										+ tmin + ","
										+ tmax + ","
										+ vaporPressure + ","
										+ windSpeed + ","
										+ rain + newLineToUse;

		}
	}
*/
	private void convertStandaloneWeatherToORYZA(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable, long lineIndex)
					throws FileNotFoundException, IOException, Exception {
		int firstLineIndexWithData = 5; // with the standalone, the first line with info is #6 and it starts on 1901, i guess, so we don't have that funny issue 
		// OLD...
		// Beware the MAGIC NUMBER!!! it happens that 01001 starts us on year 2000, day 366
		                                 // so, we want to start on 2001/001 which is line #13 or index 12
		String stationNumber = "    1"; // Beware the MAGIC NUMBER!!! using padded 1 as the the station number
		
		String headerLine = null;
		String oryzaStyle = null;
//		String singleLineToBuild = null;
//		String[] splitLine = null;
		
		int previousYear = -5;
		int statedYear = -1;
		int lastThreeOfYear = -4;
		int statedDay = -1;
		double sradRaw = -0.3;
		double sradRescaled = -0.32;
		double tmin = 5;
		double tmax = 2;
		double vaporPressure = -99; // Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double windSpeed = -99; // Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double rain = -2432.231;

		
		String[] weatherFileArray = FunTricks.readTextFileToArray(DSSATWeatherOutput);
		
		String outputFilename = ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(1, 3);
		
		// make the header line for the oryza style...
		// long, lat, elevation, angstrom A, angstrom B (zeros in our case)
		double latitude  = geogTable.getValue(lineIndex,2);
		double longitude = geogTable.getValue(lineIndex,3);
		double elevation = dataTable.getValue(lineIndex,1);

		String latString  = FunTricks.fitInNCharacters(latitude, 6);
		String longString = FunTricks.fitInNCharacters(longitude,6);
		String elevString = FunTricks.fitInNCharacters(elevation,6);
		String angString  = FunTricks.fitInNCharacters(0.0,      6); // Beware the MAGIC NUMBER!!! we are using zeros for the angstrom parameters since we are providing radiation amounts
		headerLine = longString + "," + latString + "," + elevString + "," + angString + "," + angString + newLineToUse ;

		previousYear = -10; // initialize with a junk value
		oryzaStyle = ""; // initialize with blank
		
//              1         2         3         4		
//    01234567890123456789012345678901234567890		
//		@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
//		 1001  16.1  14.7  -8.4   0.0  32.2 314.9

		for (int wLineIndex = firstLineIndexWithData; wLineIndex < weatherFileArray.length; wLineIndex++) {
//			splitLine = weatherFileArray[wLineIndex].trim().split(" +");
			
			statedYear = Integer.parseInt(  weatherFileArray[wLineIndex].substring(0,2).trim());
			statedDay  = Integer.parseInt(  weatherFileArray[wLineIndex].substring(2,5).trim());
			rain       = Double.parseDouble(weatherFileArray[wLineIndex].substring(24,29).trim());
			sradRaw    = Double.parseDouble(weatherFileArray[wLineIndex].substring(6,11).trim());
			sradRescaled = sradRaw * SWmultiplier;
			tmax = Double.parseDouble(weatherFileArray[wLineIndex].substring(13,17).trim());
			tmin = Double.parseDouble(weatherFileArray[wLineIndex].substring(18,23).trim());

			
			if (statedYear != previousYear) {
				// we are done with a whole year, so write out what we have and start the next one...
				// because i'm lazy, we'll end up with one extra file with a junk name....

				// pick the appropriate filename
				// we need the last three digits of the year number
				lastThreeOfYear = previousYear % 1000;
				// Beware the MAGIC NUMBER!!! we will always assume station #1
				outputFilename = ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(lastThreeOfYear, 3);
				
				// write out what we have
				FunTricks.writeStringToFile(oryzaStyle, outputFilename);
				
				// update the previous year holder
				previousYear = statedYear;
				
				// reinitialize with the headerline
				oryzaStyle = headerLine; // initialize with header
			}
			// Beware the MAGIC NUMBER!!! the widths and spacing and what not are what they are but should only matter for the station, year, and day...
//			oryzaStyle += stationNumber + "," 
//								+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedYear), 4) + "," 
//								+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedDay), 3) + ","
//								+ FunTricks.fitInNCharacters(sradRescaled,9) + ","
//								+ FunTricks.fitInNCharacters(tmin,6) + ","
//								+ FunTricks.fitInNCharacters(tmax,6) + ","
//								+ FunTricks.fitInNCharacters(vaporPressure,6) + ","
//								+ FunTricks.fitInNCharacters(windSpeed,6) + ","
//								+ FunTricks.fitInNCharacters(rain,6) + newLineToUse;

			oryzaStyle += stationNumber + "," 
										+ statedYear + "," 
										+ statedDay + ","
										+ sradRescaled + ","
										+ tmin + ","
										+ tmax + ","
										+ vaporPressure + ","
										+ windSpeed + ","
										+ rain + newLineToUse;

		}
	}
	
		
	private void writeProvenance() throws IOException, FileNotFoundException, Exception {
		String filename = yieldOutputBaseName  + "_provenance.txt";
		writeProvenance(filename);
	}

	private void writeProvenance(String filename) throws IOException, FileNotFoundException, Exception {


		File provenanceFileObject = new File(filename);
		PrintWriter provenanceOut = new PrintWriter(provenanceFileObject);


		provenanceOut.print("--- Provenance for ORYZA run starting at " + new Date() + "\n");
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

		provenanceOut.print("oryzaExecutionCommandTemp:\t" + oryzaExecutionCommandTemp + "\n");
		provenanceOut.print("dssatBinaryNameTemp:\t" + dssatBinaryNameTemp + "\n");
		provenanceOut.print("nHappyRepetitions:\t" + nHappyRepetitions + "\n");
		provenanceOut.print("happyYieldThresholdToDoRealRuns:\t" + happyYieldThresholdToDoRealRuns + "\n");
		provenanceOut.print("firstRandomSeed:\t" + firstRandomSeed + "\n");
		provenanceOut.print("nRealRepetitions:\t" + nRealRepetitions + "\n");
		provenanceOut.print("nPlantingWindowsPerMonth:\t" + nPlantingWindowsPerMonth + "\n");
		provenanceOut.print("gisTableBaseName:\t" + gisTableBaseName + "\n");
		provenanceOut.print("magicSoilPrefix:\t" + magicSoilPrefix + "\n");
		provenanceOut.print("realControlsFile:\t" + realControlsFile + "\n");
		provenanceOut.print("varietyFile:\t" + varietyFile + "\n");
		provenanceOut.print("SWmultiplier:\t" + SWmultiplier + "\n");
		provenanceOut.print("yieldOutputBaseName:\t" + yieldOutputBaseName + "\n");
		provenanceOut.print("doPuddling:\t" + doPuddling + "\n");
		provenanceOut.print("useWaterContent:\t" + useWaterContent + "\n");
		provenanceOut.print("fractionBetweenLowerLimitAndDrainedUpperLimit:\t" + fractionBetweenLowerLimitAndDrainedUpperLimit + "\n");
		provenanceOut.print("depthForNitrogen:\t" + depthForNitrogen + "\n");
		provenanceOut.print("residueNitrogenPercent:\t" + residueNitrogenPercent + "\n");
		provenanceOut.print("co2ppm:\t" + co2ppm + "\n");
		

		provenanceOut.print("\n");

		provenanceOut.print("--- Placeholder dictionary ---" + "\n");
		provenanceOut.print("endingDateDSSATPlaceholder =\t\t\t"            + endingDateDSSATPlaceholder + "\n");
		provenanceOut.print("randomSeedDSSATPlaceholder =\t" + randomSeedDSSATPlaceholder + "\n");
		
		provenanceOut.print("\n");		

		provenanceOut.print("--- Magic number dictionary ---" + "\n");
		provenanceOut.print("outputTimeStepDays =\t" + outputTimeStepDays + "\n");
		provenanceOut.print("shortwaveMultiplierForDSSAT =\t"      + shortwaveMultiplierForDSSAT + "\n");
		provenanceOut.print("fakePlantingYear =\t\t\t"           + fakePlantingYear + "\n");
		provenanceOut.print("rerunFile =\t"      + rerunFile + "\n");
		provenanceOut.print("detailedResultsFile =\t\t\t"            + detailedResultsFile + "\n");
		provenanceOut.print("modelLogFile =\t\t\t"            + modelLogFile + "\n");
		provenanceOut.print("soilFile =\t\t\t"            + soilFile + "\n");
		provenanceOut.print("ORYZAWeatherPrefix =\t\t\t"            + ORYZAWeatherPrefix + "\n");
		provenanceOut.print("DSSATWeatherOutput =\t\t\t"            + DSSATWeatherOutput + "\n");
		provenanceOut.print("DSSATClimateInput =\t\t\t"            + DSSATClimateInput + "\n");
//		provenanceOut.print("fileXTemplateForDSSATWeather =\t\t\t"            + fileXTemplateForDSSATWeather + "\n");
//		provenanceOut.print("batchFileForDSSATWeather =\t\t\t"            + batchFileForDSSATWeather + "\n");
		provenanceOut.print("nDaysInMonth =\t\t\t"            + nDaysInMonth + "\n");
		provenanceOut.print("commercialWeightIndex =\t\t\t"            + commercialWeightIndex + "\n");
		provenanceOut.print("happyMaturityIndex =\t\t\t"            + happyMaturityIndex + "\n");
		provenanceOut.print("realMaturityIndex =\t\t\t"            + realMaturityIndex + "\n");
		provenanceOut.print("\n");
		
		provenanceOut.print("--- begin copy of template X file for weather ---" + "\n");
//		provenanceOut.print(fileXTemplateForDSSATWeatherContents + "\n");
		provenanceOut.print("\n");
		
//		provenanceOut.print("--- begin copy of batch file for DSSAT weather for weather ---" + "\n");
//		provenanceOut.print(batchFileForDSSATWeatherContents + "\n");
//		provenanceOut.print("\n");

		provenanceOut.print("--- columns actually recorded ---" + "\n");
		for (int colIndex = 0; colIndex < colsToPull.length; colIndex++) {
			provenanceOut.print(colsToPull[colIndex] + "\n");
		}

		
		provenanceOut.print("--- column names for Real cases ---" + "\n");
		provenanceOut.print("[col_index] [starting character index] [name]" + "\n");
		for (int colIndex = 0; colIndex < nOutputVariablesReal; colIndex++) {
//			provenanceOut.print("[" + colIndex + "] " + fixedStartsReal[colIndex] + " " + colNamesReal[colIndex] + "\n");
			provenanceOut.print("[" + colIndex + "] " + "--" + " " + colNamesReal[colIndex] + "\n");
		}

		provenanceOut.print("\n");
		
		provenanceOut.print("--- column names for Happy cases ---" + "\n");
		provenanceOut.print("[col_index] [starting character index] [name]" + "\n");
		for (int colIndex = 0; colIndex < nOutputVariablesHappy; colIndex++) {
//			provenanceOut.print("[" + colIndex + "] " + fixedStartsHappy[colIndex] + " " + colNamesHappy[colIndex] + "\n");
			provenanceOut.print("[" + colIndex + "] " + "--" + " " + colNamesHappy[colIndex] + "\n");
		}
		
		provenanceOut.flush();
		provenanceOut.close();


	}

	
	
	public void runIt() throws IOException, Exception {

		if (!readyToRun) {
			System.out.println("readyToRun = [" + readyToRun + "], so we are bailing.");
			return;
		}

		//////////////////////////////
		// write out the provenance //
		//////////////////////////////
		
		this.writeProvenance();
				
		//////////////////
		// declarations //
		//////////////////
		double happyYieldMeanThisWindowOnly = Double.NaN;
		String statisticsOutLine = null;
		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		double timeSinceStart = -4.3;
		double projectedTime = -4.5;
		double timeRemaining = -4.6;

		String soilTypeString = null;
		
		double fertilizerAmount = -10; 
		int soilType         = -1;
		int firstPlantingDay = -2;
		int magicPlantingDay = -99;
		int fertilizerColumn = -5;
		
		String randomSeedActualValue = null;
		
		Process happyExecution = null;
		Process realExecution = null;

		// start the overall timer...
		everythingTimer.tic();
		
		// open up a writer for the statistics output file
		File statisticsFileObject = new File(yieldOutputBaseName  + "_STATS.txt");
		PrintWriter statisticsOut = new PrintWriter(statisticsFileObject);


		// prepare for multiple planting dates in each month
		int plantingWindowSpacing = nDaysInMonth / nPlantingWindowsPerMonth;
		if (plantingWindowSpacing < 1) {
			plantingWindowSpacing = 1;
		}


		// read in the fundamental data
		// Beware the MAGIC NUMBER!!! gonna force these into memory
		int formatIndexToForce = MultiFormatMatrix.dataInVector;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);

		if (geogMatrix.getDimensions()[1] != 4) {
			System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
			throw new Exception();
		}

		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];

		// **************************************** //
		// put beginning of data line for loop here //
		// **************************************** //
		System.out.println("-- starting through data --");
		for (int dataLineIndex = 0; dataLineIndex < nLinesInDataFile; dataLineIndex++) {

			// System.out.println("\t\t-- data line index " + dataLineIndex + " --");
			// we need to do the runs for each planting window in the planting month
			// reset the accumulators for the month
			this.resetAccumulators(this.HAPPY_RUN); // we want the happy ones reset
			this.resetAccumulators(this.REAL_RUN);  // we want the real ones reset





			nonClimateInfo  = DSSATHelperMethods.soilElevMonthDayNitrogenMFM(dataMatrix, dataLineIndex);

			soilType         = nonClimateInfo[0];
			// elevation        = nonClimateInfo[1]; // don't need this here...
			// plantingMonth    = nonClimateInfo[2]; // don't need this here...
			firstPlantingDay = nonClimateInfo[3];
//			int nitrogenLevel = nonClimateInfo[4];


			// we need to put together the soil file
			// put together the soil bundle
			
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
			
			this.prepareSoil(
					dataMatrix,
	    		dataLineIndex,
	    		soilTypeString,
	    		doPuddling,
	    		useWaterContent,
	    		fractionBetweenLowerLimitAndDrainedUpperLimit,
	    		depthForNitrogen,
	    		residueNitrogenPercent
	    		);
			
			// i'm going to change this to work like the latest version for DSSAT where we cycle through random seeds rather than
			// go for a bunch of years off of the same seed.
			
			
			// now run through the planting windows....
			
			for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth; plantingWindowIndex++) {

				magicPlantingDay = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;

				// create the appropriate HAPPY rerun file
				prepareHappyRunUnique(magicPlantingDay);

				// if this is the first time through, build a little bit of weather
				// we always need the little bit of weather for the happy plant regardless of the window...
				for (int happyPlantSeedIndex = 0; happyPlantSeedIndex < this.nHappyRepetitions; happyPlantSeedIndex++) {
					randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed + happyPlantSeedIndex,5);
					if (randomSeedActualValue.length() != 5) {
						System.out.println("fRS = " + firstRandomSeed + " ; hPRI = " + happyPlantSeedIndex + " ; val = [" + randomSeedActualValue + "]");
						throw new Exception();
					}

					weatherTimer.tic();
					this.buildORYZAWeatherFromDSSATSpecifySeed(dataMatrix, geogMatrix, dataLineIndex, randomSeedActualValue);
					this.timeForWeather.useDoubleValue(weatherTimer.tocSeconds());

					oryzaTimer.tic();
					happyExecution = Runtime.getRuntime().exec(oryzaExecutionCommand , null , null);

					// try a simple way to wait for the run to finish...
					happyExecution.waitFor();
					timeForORYZA.useDoubleValue(oryzaTimer.tocSeconds());

					// read the results....
					// Beware the MAGIC NUMBER!!! We are only doing single repetitions...
					readResultsFileSpecifyLength(this.HAPPY_RUN,1); // we want the Happy reader...

					// make sure the streams got closed for the process after we managed to read everything...
					happyExecution.getErrorStream().close();
					happyExecution.getInputStream().close();
					happyExecution.getOutputStream().close();
					happyExecution.destroy(); // just in case it is still going...
					System.gc();
					
				} // for happyPlantSeedIndex
				
				///////////////////////
				// end of happy runs //
				///////////////////////					



				// determine if the results are sufficiently favorable to run the real runs...
				happyYieldMeanThisWindowOnly = happyYieldThisWindowOnly.getMean(); // summaryAccumulatorsHappy[this.commercialWeightIndex].getMean();

//				System.out.println("==== HAPPY plant results ====");
//				System.out.println("commercial weight (this window only = " + happyYieldMeanThisWindowOnly + ")");
//				System.out.println(summaryAccumulatorsHappy[this.commercialWeightIndex].getAllPretty());

				if (happyYieldMeanThisWindowOnly >= happyYieldThresholdToDoRealRuns) {
					
					// Beware the MAGIC NUMBER!!! fertilizer column
					fertilizerColumn = 3;
					fertilizerAmount = dataMatrix.getValue(dataLineIndex,fertilizerColumn);

					prepareRealRunUnique(magicPlantingDay, fertilizerAmount);

					for (int realSeedIndex = 0; realSeedIndex < this.nRealRepetitions; realSeedIndex++) {
						randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed + realSeedIndex,5);
						if (randomSeedActualValue.length() != 5) {
							System.out.println("fRS = " + firstRandomSeed + " ; hPRI = " + realSeedIndex + " ; val = [" + randomSeedActualValue + "]");
							throw new Exception();
						}


						weatherTimer.tic();
						buildORYZAWeatherFromDSSATSpecifySeed(dataMatrix, geogMatrix, dataLineIndex, randomSeedActualValue);
						timeForWeather.useDoubleValue(weatherTimer.tocSeconds());

						// do the real runs

						oryzaTimer.tic();
						realExecution = Runtime.getRuntime().exec(oryzaExecutionCommand , null , null);

						// try a simple way to wait for the run to finish...
						realExecution.waitFor();
						timeForORYZA.useDoubleValue(oryzaTimer.tocSeconds());

						// read the results....
//						this.readResultsFile(this.REAL_RUN); // we want the Real reader...
						// Beware the MAGIC NUMBER!!! We are only doing single repetitions...
						readResultsFileSpecifyLength(this.REAL_RUN,1); // we want the Happy reader...

						// make sure the streams got closed for the process after we managed to read everything...
						realExecution.getErrorStream().close();
						realExecution.getInputStream().close();
						realExecution.getOutputStream().close();
						realExecution.destroy(); // just in case it is still going...
						System.gc();

					} // for random seed
					
				} else {
					// skip the real runs and write down zeros...
					for (int fakeRun = 0; fakeRun < nRealRepetitions; fakeRun++) {
						for (int outputIndex = 1; outputIndex < nOutputVariablesReal; outputIndex++) {
							summaryAccumulatorsReal[outputIndex].useDoubleValue(0.0);
						} // for outputIndex
					} // for fakeRun
				} // if it is not worth running for real...


//				System.out.println(summaryAccumulatorsReal[this.commercialWeightIndex].getAllPretty());
			} // end of planting window for-loop

			
			
			// grab the happy plant yields and maturities (mean/std)
			// grab the real run yields (min/max/mean/std) and maturities (mean/std)
				statisticsOutLine =
					summaryAccumulatorsHappy[this.commercialWeightIndex].getMean() + delimiter +
					summaryAccumulatorsHappy[this.commercialWeightIndex].getStd() + delimiter +
					summaryAccumulatorsHappy[happyMaturityIndex].getMean() + delimiter +
					summaryAccumulatorsHappy[happyMaturityIndex].getStd() + delimiter +

					summaryAccumulatorsReal[this.commercialWeightIndex].getMinAsDouble() + delimiter +
					summaryAccumulatorsReal[this.commercialWeightIndex].getMaxAsDouble() + delimiter +
					summaryAccumulatorsReal[this.commercialWeightIndex].getMean() + delimiter +
					summaryAccumulatorsReal[this.commercialWeightIndex].getStd() + delimiter +
					summaryAccumulatorsReal[realMaturityIndex].getMean() + delimiter +
					summaryAccumulatorsReal[realMaturityIndex].getStd();

				// now grab all the other real run results
				for (int extraIndex = 0; extraIndex < colsToPull.length; extraIndex++) {
					statisticsOutLine += delimiter + summaryAccumulatorsReal[colsToPull[extraIndex]].getMean();
				}
			
			statisticsOutLine += "\n";
			statisticsOut.print(statisticsOutLine);

			// now we're ready to deal with the next pixel...
			// Beware the MAGIC NUMBER!!! checking every one percent...
			if (dataLineIndex % (nLinesInDataFile / 400 + 1) == 0) {
				
				timeSinceStart = everythingTimer.sinceStartSeconds();
//				averageTimePerEvent = timeForWeather.getMean() + timeForORYZA.getMean();
				projectedTime = timeSinceStart / (dataLineIndex + 1) * nLinesInDataFile;
				timeRemaining = projectedTime - timeSinceStart;
				
				System.out.println("prog: " + (dataLineIndex + 1) + "/" + nLinesInDataFile + " = " +
						FunTricks.fitInNCharacters(100 * (dataLineIndex + 1.0)/nLinesInDataFile,5) +
						"\tave W: " + FunTricks.fitInNCharacters(timeForWeather.getMean(),5) + 
						" ave O: " + FunTricks.fitInNCharacters(timeForORYZA.getMean(),5) +
						" N/P/R = " + FunTricks.fitInNCharacters(timeSinceStart,6) +
						"/" + FunTricks.fitInNCharacters(projectedTime,6) +
						"/" + FunTricks.fitInNCharacters(timeRemaining,6) +
						"\t" + FunTricks.fitInNCharacters(timeRemaining / 60, 6)
				);
			}
			
			
			// ********************************** //
			// put end of data line for loop here //
			// ********************************** //
		} // end of data line loop
		
		// close out the output files
		statisticsOut.flush();
		statisticsOut.close();

		/////////////////////////////////////////
		// when all done, write out info files //
		/////////////////////////////////////////
		// Beware the MAGIC NUMBER!!!
		String columnList = "happy_mean" + delimiter + "happy_std" + delimiter
		+ "happy_maturity_mean" + delimiter + "happy_maturity_std" + delimiter
		+ "yield_min" + delimiter + "yield_max" + delimiter 
		+ "yield_mean" + delimiter + "yield_std" + delimiter 
		+ "real_maturity_mean" + delimiter + "real_maturity_std";

		for (int extraIndex = 0; extraIndex < colsToPull.length; extraIndex++) {
			columnList += delimiter + colNamesReal[colsToPull[extraIndex]];
		}
		columnList += "\n";
		
		
		FunTricks.writeStringToFile(columnList,yieldOutputBaseName  + "_STATS.cols.txt");

		long nRows = nLinesInDataFile;

		// Beware the MAGIC NUMBER!!!
		int nCols = 10 + colsToPull.length; // min / max/ mean / std / bad / happy mean / happy std / real anthesis mean / real anthesis std / real maturity mean / real maturity std / happy maturity mean / happy maturity std
		FunTricks.writeInfoFile(yieldOutputBaseName  + "_STATS", nRows, nCols, delimiter);

		
		System.out.println("total DSSAT/weather time = " + timeForWeather.getTotalDouble());
		System.out.println("total ORYZA         time = " + timeForORYZA.getTotalDouble());
	}
	
	
	
	
	
	

	


}






