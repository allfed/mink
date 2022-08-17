package org.ifpri_converter;
import java.io.BufferedReader;
//import java.io.File;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
//import java.util.concurrent.*;

public class MagicDSSATRunner4 {


	private static Object[] cliFileContentsAndOtherInfo(String dataLine, String geogLine, double SWmultiplier) {
		String contentsOfCLIFile = "";

		String delimiter = "\t";
//		String dataLine = "";
//		String geogLine = "";

		String[] dataLineSplit = dataLine.split(delimiter);
		String[] geogLineSplit = geogLine.split(delimiter);

		// the geographic info
		double latitude  = Double.parseDouble(geogLineSplit[2]);
		double longitude = Double.parseDouble(geogLineSplit[3]);

		// the data stuff
		double soilType      = Double.parseDouble(dataLineSplit[0]);
		double elevation     = Double.parseDouble(dataLineSplit[1]);
		double plantingMonth = Double.parseDouble(dataLineSplit[2]);

		int nMonths = 12; // this actually needs to be twelve
		double[] monthlySW        = new double[nMonths];
		double[] monthlyTmax      = new double[nMonths];
		double[] monthlyTmin      = new double[nMonths];
		double[] monthlyPrec      = new double[nMonths];
		double[] monthlyRainydays = new double[nMonths];

		int nBeforeMonthly = 3; // this needs to reflect how many variables come before the monthly data

		// soilType (1st col) / elevation (2nd col) / planting month (3rd) / SW (4th and on)
		int shifter = nBeforeMonthly + nMonths * 0; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
			monthlySW[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) * SWmultiplier;
			// make sure the value is non-negative
			if (monthlySW[monthIndex] < 0) {
				monthlySW[monthIndex] = 0.0;
			}
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
			monthlyTmax[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
			monthlyTmin[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
		}

		shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
			monthlyPrec[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			if (monthlyPrec[monthIndex] < 0) {
				monthlyPrec[monthIndex] = 0.0;
			}
		}

		shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
			monthlyRainydays[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			if (monthlyRainydays[monthIndex] < 0) {
				monthlyRainydays[monthIndex] = 0.0;
			}
		}

		// do some math to figure out the annualized values
		/*
		WEATHER.CDE:TMXY     Temperature maximum, yearly average, C                               IB
		[rdrobert@ifpri test_area]$ grep TMNY *.CDE
		WEATHER.CDE:TMNY     Temperature minimum, yearly average, C                               IB
		[rdrobert@ifpri test_area]$ grep RAIY *.CDE
		WEATHER.CDE:RAIY     Rainfall, yearly total, mm                                           IB
		 */

		// initialize some values
		double averageTemperature = 0.0;
		double averageSW = 0.0;
		double averageMaxTemperature = 0.0;
		double averageMinTemperature = 0.0;
		double totalPrecipitation = 0.0;
		double maxTemperature = Double.NEGATIVE_INFINITY;
		double minTemperature = Double.POSITIVE_INFINITY;

		// accumulate/test everything
		for (int monthIndex = 0; monthIndex < nMonths ; monthIndex++) {		
			averageTemperature += (monthlyTmax[monthIndex] + monthlyTmin[monthIndex]);
			averageMaxTemperature += monthlyTmax[monthIndex];
			averageMinTemperature += monthlyTmin[monthIndex];
			averageSW += monthlySW[monthIndex];

			totalPrecipitation += monthlyPrec[monthIndex];

			if (monthlyTmax[monthIndex] > maxTemperature) {
				maxTemperature = monthlyTmax[monthIndex];
			}

			if (monthlyTmin[monthIndex] < minTemperature) {
				minTemperature = monthlyTmin[monthIndex];
			}
		} // for months

		// find the averages
		averageTemperature /= (nMonths * 2); // there are two numbers for each month
		averageMaxTemperature /= nMonths;
		averageMinTemperature /= nMonths;
		averageSW /= nMonths;
		double temperatureAmplitude = maxTemperature - minTemperature;


		// figure out an approximate starting day-of-the-year that corresponds to this planting month
		// we are ignoring
		int firstPlantingDay = -5123;
		switch ((int)plantingMonth) {
		case 1:
			firstPlantingDay = 1;
			break;
		case 2:
			firstPlantingDay = 32; // 31 + 1
			break;
		case 3:
			firstPlantingDay = 60; // etc
			break;
		case 4:
			firstPlantingDay = 91;
			break;
		case 5:
			firstPlantingDay = 121;
			break;
		case 6:
			firstPlantingDay = 152;
			break;
		case 7:
			firstPlantingDay = 182;
			break;
		case 8:
			firstPlantingDay = 213;
			break;
		case 9:
			firstPlantingDay = 244;
			break;
		case 10:
			firstPlantingDay = 274;
			break;
		case 11:
			firstPlantingDay = 305;
			break;
		case 12:
			firstPlantingDay = 335;
			break;
		default:
			firstPlantingDay = -1;
		}

		// to format something...			
		NumberFormat formatForNumber = NumberFormat.getInstance();

		((DecimalFormat)formatForNumber).applyPattern("00");
		String soilTypeFormatted = formatForNumber.format(soilType);
		String plantingMonthFormatted = formatForNumber.format(plantingMonth);
		((DecimalFormat)formatForNumber).applyPattern("###");
		String firstPlantingDayFormatted = formatForNumber.format(firstPlantingDay);


		contentsOfCLIFile = "";
		// create all the stuff for the CLI files... this will be exceedingly magical
		contentsOfCLIFile += "*CLIMATE : Soil Number = " + soilTypeFormatted + " ; Elevation = " + elevation + " ; Planting Month = " + plantingMonthFormatted + " ; First Planting Day = " + firstPlantingDayFormatted + "\n";
		contentsOfCLIFile += "@ INSI      LAT     LONG  ELEV   TAV   AMP  SRAY  TMXY  TMNY  RAIY" + "\n";

		// format the latitude
		((DecimalFormat)formatForNumber).applyPattern(" 00.00;-");
		String latitudeFormatted = formatForNumber.format(latitude);

		((DecimalFormat)formatForNumber).applyPattern(" 000.00;-");
		String longitudeFormatted = formatForNumber.format(longitude);

		((DecimalFormat)formatForNumber).applyPattern(" 0000;-");
		String elevationFormatted = formatForNumber.format(elevation);

		((DecimalFormat)formatForNumber).applyPattern(" 00.0;-");
		String averageTempFormatted = formatForNumber.format(averageTemperature);
		String amplitudeFormatted = formatForNumber.format(temperatureAmplitude);
		String averageSWFormatted = formatForNumber.format(averageSW);
		String averageMaxTempFormatted = formatForNumber.format(averageMaxTemperature);
		String averageMinTempFormatted = formatForNumber.format(averageMinTemperature);

		((DecimalFormat)formatForNumber).applyPattern("0000");
		String totalPrecipitationFormatted = formatForNumber.format(totalPrecipitation);


		contentsOfCLIFile += "  RICK   " + latitudeFormatted + "  " + longitudeFormatted + " " + elevationFormatted
		+ " " + averageTempFormatted + " " + amplitudeFormatted + " " + averageSWFormatted + " " + averageMaxTempFormatted + 
		" " + averageMinTempFormatted + "  " + totalPrecipitationFormatted + "\n";

		contentsOfCLIFile += "@START  DURN  ANGA  ANGB REFHT WNDHT" + "\n";
		contentsOfCLIFile += "  2000    15  0.25  0.50  0.00  0.00" + "\n";
		contentsOfCLIFile += "@ GSST  GSDU" + "\n";
		contentsOfCLIFile += "     0     0" + "\n";
		contentsOfCLIFile += "\n";
		contentsOfCLIFile += "*MONTHLY AVERAGES" + "\n";
		contentsOfCLIFile += "@MONTH  SAMN  XAMN  NAMN  RTOT  RNUM" + "\n";

		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {

			((DecimalFormat)formatForNumber).applyPattern("    00");
			String monthFormatted = formatForNumber.format(monthIndex + 1);

			((DecimalFormat)formatForNumber).applyPattern("000.0");
			String swFormatted = formatForNumber.format(monthlySW[monthIndex]);

			((DecimalFormat)formatForNumber).applyPattern(" 00.0;-");
			String tmaxFormatted = formatForNumber.format(monthlyTmax[monthIndex]);
			String tminFormatted = formatForNumber.format(monthlyTmin[monthIndex]);

			((DecimalFormat)formatForNumber).applyPattern("000.0");
			String precFormatted      = formatForNumber.format(monthlyPrec[monthIndex]);
			String rainydaysFormatted = formatForNumber.format(monthlyRainydays[monthIndex]);

			contentsOfCLIFile += monthFormatted + " " + swFormatted + " " + tmaxFormatted + " " + tminFormatted + " " + precFormatted + " " + rainydaysFormatted + "\n";
		}
		/*

@ INSI      LAT     LONG  ELEV   TAV   AMP  SRAY  TMXY  TMNY  RAIY
NASA    24.88  -006.13 00000  99.0  99.0    18  99.0   0.0   503
@START  DURN  ANGA  ANGB REFHT WNDHT
2000    15  0.25  0.50  0.00  0.00
@ GSST  GSDU
   0     0

		 *MONTHLY AVERAGES
@MONTH  SAMN  XAMN  NAMN  RTOT  RNUM
   1 014.9  23.9  07.4 000.0 000.0
   2 017.9  27.7  09.6 001.0 000.0
		 */

		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return new Object[] {contentsOfCLIFile, nonClimateInfo};
	}



	public static void main(String commandLineOptions[]) throws Exception {

		long startTime = System.currentTimeMillis();

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i] + "\n");
		}
		System.out.println();


		if (commandLineOptions.length == 0) {
			System.out.println("Usage: org.ifpri_converter.MagicDSSATRunner GIS_table_base_name template_X_file yield_output_base_name ALL_flag settings_csv sleep_time_for_file\n" +
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
			"\n");
			System.exit(1);
		}

		////////////////////////////////////////
		// read in the command line arguments //
		////////////////////////////////////////
		String  gisTableBaseName     = commandLineOptions[0];
		String  templateXFile        = commandLineOptions[1];
		String  yieldOutputBaseName  = commandLineOptions[2];
		boolean allFlag = Boolean.parseBoolean(commandLineOptions[3]);
		String  settingsCSV          = commandLineOptions[4];
		long    sleepTimeForFile = Long.parseLong(commandLineOptions[5]);

		//////////////////////////////////
		// interpret the fancy controls //
		//////////////////////////////////

		/*		
		"* full path to DSSAT\n" +
		"* shortwave multiplier\n" +
		"* first random seed\n" +
		"* # of seeds to use\n" +
		"* magic soil prefix\n" +
		"* fake planting year (2 digits)\n" +
		"* spin up time (days)\n" +
		"* # of planting windows to try\n" +
		"* planting window length (days)\n" +
		 */
		String[] settingsSplit = settingsCSV.split(","); // CSV means comma, duh.

		String pathToDSSATDirectory  =                    settingsSplit[0];
		File pathToDSSATDirectoryAsFile = new File(pathToDSSATDirectory);
		String nameOfDSSATExecutable =                    settingsSplit[1];
//		String tempXFileName         =                    settingsSplit[2];
		double SWmultiplier          = Double.parseDouble(settingsSplit[2]);
		int firstRandomSeed          =   Integer.parseInt(settingsSplit[3]);
		int nRandomSeedsToUse        =   Integer.parseInt(settingsSplit[4]);
		String magicSoilPrefix       =                    settingsSplit[5];
		int fakePlantingYear         =   Integer.parseInt(settingsSplit[6]);
		int spinUpTimeDays           =   Integer.parseInt(settingsSplit[7]);
		int nPlantingWindowsPerMonth =   Integer.parseInt(settingsSplit[8]);
		int plantingWindowLengthDays =   Integer.parseInt(settingsSplit[9]);


		/////////////////////////////////////
		// create a bunch of magic numbers //
		/////////////////////////////////////
		String delimiter = "\t";
		String magicWeatherStationNameToUse     = "RICK";
		String magicWeatherStationNameToUsePath = pathToDSSATDirectory + magicWeatherStationNameToUse + ".CLI";
		String magicDSSATSummaryToRead          = "Summary.OUT";
		String magicDSSATSummaryToReadPath      = pathToDSSATDirectory + magicDSSATSummaryToRead;
		File summaryDSSATOutputAsFileObject = new File(magicDSSATSummaryToReadPath);
		String magicInitializationFile          = "deleteme.dv4";
		String magicInitializationFilePath      = pathToDSSATDirectory + magicInitializationFile;
		String tempXFileName                    = "deleteme.meX";
		
		String magicInitializationContents = "*BATCH\n@FILEX       TRTNO\n" + tempXFileName + "     1\n";

//		int nWaits = -1;
		long sleeptimeWaitForFileMillis = sleepTimeForFile;
		long sleeptimeExtraMillis       = 1;
		long endNanos = -1;
		long startNanos = System.nanoTime();
		
		int maxParsingTries = 5;

		long totalDSSATTimeNanos = 0; // actually needs to be zero
//		long previousDSSATTimeNanos = 0;

		int magicDSSATSummaryLineIndexToRead = 4;
		int magicHarvestedWeightAtHarvestStartIndex = 137; // character # 137, index 136
		int magicHarvestedWeightAtHarvestEndIndex   = 141; // character # 141, index 140

		int nDaysInMonth =  30;
		int nDaysInYear  = 365;

		// search and replace elements in the template X file
		String soilPlaceholder                = "ssssssssss";
		String initializationStartPlaceholder = "iiiiS";
		String plantingDateStartPlaceholder   = "ppppS";
		String plantingDateEndPlaceholder     = "ppppE";
		String randomSeedPlaceholder          = "rrrrr";
		String nYearsOrRandomSeedsPlaceholder = "nnnnn";
		String weatherPlaceholder             = "wwww";

		String[] dssatExecutionCommand = new String[3];
		dssatExecutionCommand[0] = pathToDSSATDirectory + nameOfDSSATExecutable;
		dssatExecutionCommand[1] = "b";
		dssatExecutionCommand[2] = magicInitializationFile;






		////////////////////////////////////////// start new plan //////////////////////////////////////////////		

		///////////////////////////////////////
		// read in the whole template X file //
		///////////////////////////////////////
		System.out.println("-- starting to read in template X file --");
		BufferedReader templateXFileReader = new BufferedReader(new FileReader(templateXFile));
		String templateLine = templateXFileReader.readLine();
		String templateXFileContents = "";
		while (templateLine != null) {
			templateXFileContents += templateLine + "\n";
			templateLine = templateXFileReader.readLine();
		}
		templateXFileReader.close();
		System.out.println("== done reading in template X file ==");



		///////////////////////////////////
		// write out the provenance file //
		///////////////////////////////////
		System.out.println("-- starting to write provenance file --");
		File provenanceFileObject = new File(yieldOutputBaseName  + "_provenance.txt");
//		outputStream = new FileWriter(weatherStationFileObject);
		PrintWriter provenanceOut = new PrintWriter(provenanceFileObject);
		provenanceOut.print("--- Command line arguments in re-usable format ---" + "\n");
		provenanceOut.print("\n");
		for (int argIndex = 0; argIndex < commandLineOptions.length; argIndex++) {
			provenanceOut.print(commandLineOptions[argIndex] + " ");
		}
		provenanceOut.print("\n");
		provenanceOut.print("\n");
		provenanceOut.print("--- Command line arguments in human readable format ---" + "\n");
		provenanceOut.print("\n");
		provenanceOut.print("gisTableBaseName:\t"    + gisTableBaseName + "\n");
		provenanceOut.print("templateXFile:\t\t"       + templateXFile + "\n");
		provenanceOut.print("yieldOutputBaseName:\t" + yieldOutputBaseName + "\n");
		provenanceOut.print("allFlag:\t\t"             + allFlag + "\n");
		provenanceOut.print("settingsCSV:\t\t"         + settingsCSV + "\n");
		provenanceOut.print("\n");
		provenanceOut.print("  --- settings CSV interpretations ---" + "\n");
		provenanceOut.print("  pathToDSSATDirectory:\t\t"     + pathToDSSATDirectory + "\n");
		provenanceOut.print("  nameOfDSSATExecutable:\t"    + nameOfDSSATExecutable + "\n");
//		provenanceOut.print("  tempXFileName:\t"            + tempXFileName + "\n");
		provenanceOut.print("  SWmultiplier:\t\t\t"             + SWmultiplier + "\n");
		provenanceOut.print("  firstRandomSeed:\t\t"          + firstRandomSeed + "\n");
		provenanceOut.print("  nRandomSeedsToUse:\t\t"        + nRandomSeedsToUse + "\n");
		provenanceOut.print("  magicSoilPrefix:\t\t"          + magicSoilPrefix + "\n");
		provenanceOut.print("  fakePlantingYear:\t\t"         + fakePlantingYear + "\n");
		provenanceOut.print("  spinUpTimeDays:\t\t"           + spinUpTimeDays + "\n");
		provenanceOut.print("  nPlantingWindowsPerMonth:\t" + nPlantingWindowsPerMonth + "\n");
		provenanceOut.print("  plantingWindowLengthDays:\t" + plantingWindowLengthDays + "\n");
		provenanceOut.print("\n");
		provenanceOut.print("--- Placeholder dictionary ---" + "\n");
		provenanceOut.print("soilPlaceholder =\t\t\t" + soilPlaceholder + "\n");
		provenanceOut.print("initializationStartPlaceholder =\t" + initializationStartPlaceholder + "\n");
		provenanceOut.print("plantingDateStartPlaceholder =\t\t" + plantingDateStartPlaceholder + "\n");
		provenanceOut.print("plantingDateEndPlaceholder =\t\t" + plantingDateEndPlaceholder + "\n");
		provenanceOut.print("randomSeedPlaceholder =\t\t\t" + randomSeedPlaceholder + "\n");
		provenanceOut.print("nYearsOrRandomSeedsPlaceholder =\t\t" + nYearsOrRandomSeedsPlaceholder + "\n");
		provenanceOut.print("weatherPlaceholder =\t\t\t" + weatherPlaceholder + "\n");
		provenanceOut.print("\n");		
		provenanceOut.print("--- Magic number dictionary ---" + "\n");
		provenanceOut.print("magicWeatherStationNameToUse =\t" + magicWeatherStationNameToUse + "\n");
		provenanceOut.print("magicDSSATSummaryToRead =\t" + magicDSSATSummaryToRead + "\n");
		provenanceOut.print("magicInitializationFile =\t" + magicInitializationFile + "\n");
		provenanceOut.print("tempXFileName =\t\t\t" + tempXFileName + "\n");
		provenanceOut.print("\n");		
		provenanceOut.print("--- begin copy of template X file [" + templateXFile + "] ---" + "\n");
		provenanceOut.print(templateXFileContents);
		provenanceOut.print("---- end copy of template X file [" + templateXFile + "] ----" + "\n");
		provenanceOut.print("\n");		
		provenanceOut.print("--- begin copy of temporary initialization file [" + magicInitializationFilePath + "] ---" + "\n");
		provenanceOut.print(magicInitializationContents);
		provenanceOut.print("---- end copy of temporary initialization file [" + magicInitializationFilePath + "] ----" + "\n");
		provenanceOut.flush();
		provenanceOut.close();

		System.out.println("== done writing provenance file ==");
		/////////////////////////////
		// make the required files //
		/////////////////////////////

		// declare placeholders for the output stuff
		// in the future, we may want to try using a RandomAccessFile in order to not have to open and close all the time...

		File weatherStationFile = new File(magicWeatherStationNameToUsePath);
		PrintWriter weatherStationFileWriter = null;
//		RandomAccessFile weatherStationFileWriter = new RandomAccessFile(weatherStationFile,"rwd"); // trying the extra "d" for the fun of it

		File XFileObject = new File(pathToDSSATDirectory + tempXFileName);
		PrintWriter XFileWriter = null;
//	RandomAccessFile XFileWriter = new RandomAccessFile(XFileObject,"rwd"); // trying the extra "d" for the fun of it

//		File XFileObject = new File(pathToDSSATDirectory + tempXFileName);

		File initializationFileObject = new File(magicInitializationFilePath);
		PrintWriter initializationOut = new PrintWriter(initializationFileObject);
		initializationOut.print(magicInitializationContents);
		initializationOut.flush();
		initializationOut.close();
		

		// open up a writer for the statistics output file
		File statisticsFileObject = new File(yieldOutputBaseName  + "_STATS.txt");
		PrintWriter statisticsOut = new PrintWriter(statisticsFileObject);
		File statisticsInfoFileObject = new File(yieldOutputBaseName  + "_STATS.info.txt");
		PrintWriter statisticsInfoOut = new PrintWriter(statisticsInfoFileObject);

		// declare a writer for the ALL file and open up if necessary
		File allFileObject = new File(yieldOutputBaseName  + "_ALL.txt");
		PrintWriter allOut = null;
		File allInfoFileObject = new File(yieldOutputBaseName  + "_ALL.info.txt");
		PrintWriter allInfoOut = null;
		File allCodesFileObject = new File(yieldOutputBaseName  + "_ALL.codes.txt");
		PrintWriter allCodesOut = null;
		if (allFlag) {
			allOut     = new PrintWriter(allFileObject);
			allInfoOut = new PrintWriter(allInfoFileObject);
			allCodesOut = new PrintWriter(allCodesFileObject);
		}

		// declare a reader for the DSSAT Summary file
		FileReader DSSATSummaryStream = null;
		BufferedReader DSSATSummaryReader = null;

		// do a bunch of initializations

		Object[] pixelInfoPair = null; // {contentsOfCLIFile, nonClimateInfo}
		int[] nonClimateInfo = null; // {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
		int soilType = -1, firstPlantingDay = -4;
		int startingDayToPlantForThisWindow = -1;
		int endingDayToPlantForThisWindow = -99;
		int fakePlantingYearEnd = -101;
		int initializationDayForThisWindow = -4;
		int fakeInitializationYear = -5;
		int randomSeedToUse = -13;
//		long newCliLength = -1;
//		long newXLength = -1;

		int[][] thisPixelYields = null;
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

		NumberFormat formatForNumber = NumberFormat.getInstance();

		String sDTP = null;
		String eDTP = null;
		String iD   = null;

		String sFPY = null;
		String eFPY = null;
		String iY   = null;

		String startingDayToPlantCode = null;
		String endingDayToPlantCode   = null;
		String initializationDayCode  = null;

		String randomSeedCode = null;
		String nYearsOrRandomSeedsCode = null;

		boolean readSuccessfully = false;
		int nParsingTries = 0;

		
		
		// compute some one-time values
		String soilTypeString = null;

		int plantingWindowSpacing = nDaysInMonth / nPlantingWindowsPerMonth;
		if (plantingWindowSpacing < 1) {
			plantingWindowSpacing = 1;
		}

		
		// read in the entire data file
		
		// read the info file to figure out how many lines we're dealing with
		FileReader inputInfoStream = new FileReader(gisTableBaseName + "_data.info.txt");
		BufferedReader inininInfo = new BufferedReader(inputInfoStream);
		
		// the first line should have the rows in it
		String rowsLine = inininInfo.readLine();
		int nLinesInDataFile = Integer.parseInt(rowsLine.split("\t")[0]); // Beware the MAGIC NUMBER!!! the delimiter between the values and the description is a tab
		
		inininInfo.close();
		inputInfoStream.close();
		
		// initialize giant arrays for the data and geog stuff
		String[] dataLinesArray = new String[nLinesInDataFile];
		String[] geogLinesArray = new String[nLinesInDataFile];
		
		
		
		
		// open up data file
		FileReader inputStream = new FileReader(gisTableBaseName + "_data.txt");
		BufferedReader inininData = new BufferedReader(inputStream);

		// open up the geography file
		FileReader geogInputStream = new FileReader(gisTableBaseName + "_geog.txt");
		BufferedReader inininGeog = new BufferedReader(geogInputStream);


		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {
			dataLinesArray[lineIndex] = inininData.readLine();
			geogLinesArray[lineIndex] = inininGeog.readLine();
		}
		
		inininGeog.close();
		inininData.close();
		geogInputStream.close();
		inputStream.close();
		
		
		// while loop that steps through the file...
		// initialize with the very first line
//		long nLines = 0;
//		String dataLine = inininData.readLine();
//		String geogLine = inininGeog.readLine();
//		String dataLine = null;
//		String geogLine = null;
		String cliStuffToWrite = null;
		String XStuffToWrite = null;
		for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {
//		while (dataLine != null) {
//			nLines++;
//			System.out.println("now on line #" + nLines + "[" + lineContents + "]");
//			System.out.println("now on line #" + nLines + "[" + lineContentsGeog + "]");

			// initialize the storage array / etc
			thisPixelYields = new int[nPlantingWindowsPerMonth][nRandomSeedsToUse];
			totalYield = 0L;
			totalYieldSquared = 0L;
			maxYield = 0;
			minYield = Integer.MAX_VALUE;



//			pixelInfoPair = cliFileContentsAndOtherInfo(dataLine, geogLine, SWmultiplier);
			pixelInfoPair = cliFileContentsAndOtherInfo(dataLinesArray[lineIndex], geogLinesArray[lineIndex], SWmultiplier);

			cliStuffToWrite   = (String)pixelInfoPair[0];
			nonClimateInfo = (int[] )pixelInfoPair[1];

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


//			newCliLength = cliStuffToWrite.length();
//
//			weatherStationFileWriter.seek(0L);
//			weatherStationFileWriter.setLength(newCliLength);
//			weatherStationFileWriter.writeBytes(cliStuffToWrite);
			
			weatherStationFileWriter = new PrintWriter(weatherStationFile);
			weatherStationFileWriter.print(cliStuffToWrite);
			weatherStationFileWriter.flush();
			weatherStationFileWriter.close();

			
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
				((DecimalFormat)formatForNumber).applyPattern("000");
				sDTP = formatForNumber.format(startingDayToPlantForThisWindow);
				eDTP = formatForNumber.format(endingDayToPlantForThisWindow);
				iD   = formatForNumber.format(initializationDayForThisWindow);

				((DecimalFormat)formatForNumber).applyPattern("00");
				sFPY = formatForNumber.format(fakePlantingYear);
				eFPY = formatForNumber.format(fakePlantingYearEnd);
				iY   = formatForNumber.format(fakeInitializationYear);

				startingDayToPlantCode = sFPY + sDTP; // YYddd
				endingDayToPlantCode   = eFPY + eDTP; // YYddd
				initializationDayCode  = iY   + iD;   // YYddd


//				for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse ; randomSeedIndex++) {
					startNanos = System.nanoTime();

					// pick the random seed and format it
					randomSeedToUse = firstRandomSeed; // we are gonna try to use DSSATs internal thing... + randomSeedIndex;

					((DecimalFormat)formatForNumber).applyPattern("00000");
					randomSeedCode = formatForNumber.format(randomSeedToUse);

					((DecimalFormat)formatForNumber).applyPattern("00000");
					nYearsOrRandomSeedsCode = formatForNumber.format(nRandomSeedsToUse);

					/////////////////////
					// make the X file //
					/////////////////////

					// do the search and replace thing
					XStuffToWrite = templateXFileContents.replaceAll(weatherPlaceholder            , magicWeatherStationNameToUse);
					XStuffToWrite =         XStuffToWrite.replaceAll(soilPlaceholder               , soilTypeString);
					XStuffToWrite =         XStuffToWrite.replaceAll(initializationStartPlaceholder, initializationDayCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateStartPlaceholder  , startingDayToPlantCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(plantingDateEndPlaceholder    , endingDayToPlantCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(randomSeedPlaceholder         , randomSeedCode);
					XStuffToWrite =         XStuffToWrite.replaceAll(nYearsOrRandomSeedsPlaceholder, nYearsOrRandomSeedsCode);
					

					// overwrite the old file with the new contents
					
					XFileWriter = new PrintWriter(XFileObject);
					XFileWriter.print(XStuffToWrite);
					XFileWriter.flush();
					XFileWriter.close();
					
					///////////////
					// run DSSAT //
					///////////////


//					System.out.println("-- Starting DSSAT run for line #" + lineIndex + "/" + nLinesInDataFile 
//							+ " " + plantingWindowIndex + "/" + nPlantingWindowsPerMonth + " ---");
					Thread.sleep(sleeptimeExtraMillis);
					System.gc();
					
//					System.out.println("Runtime.getRuntime().exec([" + dssatExecutionCommand[0] + "][" + dssatExecutionCommand[1] + "][" + dssatExecutionCommand[2] + "] , null , " + pathToDSSATDirectoryAsFile + ")");
					Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
					
					//////////////////////////////////////////
					// extract the results & store to array //
					//////////////////////////////////////////
	
					readSuccessfully = false;
					nParsingTries = 0;
					
					while (!readSuccessfully) {
						Thread.sleep(sleeptimeWaitForFileMillis);

						nParsingTries++;
						try {
							DSSATSummaryStream = new FileReader(magicDSSATSummaryToReadPath);
							DSSATSummaryReader = new BufferedReader(DSSATSummaryStream);

							// read through the stuff we don't care about
							for (int junkLineIndex = 0; junkLineIndex < magicDSSATSummaryLineIndexToRead ; junkLineIndex++) {
//								System.out.println(junkLineIndex + " [" + DSSATSummaryReader.readLine() + "]");
								DSSATSummaryReader.readLine();
							}

							///////////////////////////////
							// begin reading output file //
							///////////////////////////////
							
							for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse; randomSeedIndex++) {
								
								// grab the line with actual information
								goodSummaryLine = DSSATSummaryReader.readLine();
								// pull out the harvest yield at harvest
//								System.out.println("G [" + goodSummaryLine + "]");
//								System.out.println("pWI = " + plantingWindowIndex + "; rSI = " + randomSeedIndex + "; mHSI = " + magicHarvestedWeightAtHarvestStartIndex + "; mHEI = " + magicHarvestedWeightAtHarvestEndIndex);
//								System.out.println("substring = [" + goodSummaryLine.substring(magicHarvestedWeightAtHarvestStartIndex, magicHarvestedWeightAtHarvestEndIndex) + "]");
								thisPixelYields[plantingWindowIndex][randomSeedIndex] =
									Integer.parseInt(goodSummaryLine.substring(magicHarvestedWeightAtHarvestStartIndex,
											magicHarvestedWeightAtHarvestEndIndex).trim());
							

							}

							// now accumulate various statistics
							// we will do this in a separate loop to make sure everything has been read in before doing
							// the accumulation. otherwise, we get non-deterministic computing due to partial file reads...
							for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse; randomSeedIndex++) {
								totalYield        += thisPixelYields[plantingWindowIndex][randomSeedIndex];
								totalYieldSquared += thisPixelYields[plantingWindowIndex][randomSeedIndex]
								                                                          * thisPixelYields[plantingWindowIndex][randomSeedIndex];
								if (thisPixelYields[plantingWindowIndex][randomSeedIndex] > maxYield) {
									maxYield = thisPixelYields[plantingWindowIndex][randomSeedIndex];
								}
								if (thisPixelYields[plantingWindowIndex][randomSeedIndex] < minYield) {
									minYield = thisPixelYields[plantingWindowIndex][randomSeedIndex];
								}
							}
							
							///////////////////////////////
							// end reading output file //
							///////////////////////////////

							
							// we have successfully read it, so flip the flag
							// delete it so that we don't accidentally find it next time...
							readSuccessfully = true;
							DSSATSummaryReader.close();
							DSSATSummaryStream.close();
//							System.out.println("!!!! leaving the summary file there; it should probably be deleted.... !!!");
							summaryDSSATOutputAsFileObject.delete();
//							System.gc();

							
						} catch (NumberFormatException nfe) {
							// do nothing so that it will return and try again
							System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [number format exception ]; line index " + lineIndex + ", window = " + plantingWindowIndex);
							DSSATSummaryReader.close();
//							nfe.printStackTrace();
						} catch (NullPointerException npe) {
							// do nothing so that it will return and try again							
							System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [null pointer exception  ]; line index " + lineIndex + ", window = " + plantingWindowIndex);
							DSSATSummaryReader.close();
//							npe.printStackTrace();
						} catch (java.io.FileNotFoundException fnfe) {
//							System.out.println("Failed to read summary properly on try #" + nParsingTries + ", trying again [file not found exception] + (line " + nLines + ", window = " + plantingWindowIndex + ", seed = " + randomSeedIndex);
							if (nParsingTries > maxParsingTries) {
								System.out.println("re-running DSSAT after " + nParsingTries + "; line index " + lineIndex + ", window = " + plantingWindowIndex);
								DSSATSummaryReader.close();

								Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
								nParsingTries = 0;
							} else {
//								System.out.print("[" + nParsingTries + "]");
								Thread.sleep(sleeptimeWaitForFileMillis);
							}
						} catch (StringIndexOutOfBoundsException sioobe) {
							System.out.println("String problem... re-running");
							sioobe.printStackTrace();
							DSSATSummaryReader.close();

							Thread.sleep(sleeptimeWaitForFileMillis);
							Runtime.getRuntime().exec(dssatExecutionCommand , null , pathToDSSATDirectoryAsFile);
						}
					} // end while(!readSuccessfully)
					

					endNanos = System.nanoTime();

					totalDSSATTimeNanos += (endNanos - startNanos);


//				} // end randomSeedIndex
			} // end plantingWindowIndex


			//////////////////////////////////////////////////////////////////////////
			// when finished with a pixel, then write out a line to the output file //
			//////////////////////////////////////////////////////////////////////////

			meanYield = totalYield / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse);
			stdYield  = Math.sqrt(totalYieldSquared / ((double)nPlantingWindowsPerMonth * nRandomSeedsToUse) - meanYield * meanYield ); 

			
			
			if (allFlag) {
				allOutputLine = "";
				allCodesLine = "";
				for (int plantingWindowIndex = 0; plantingWindowIndex < nPlantingWindowsPerMonth; plantingWindowIndex++) {
					startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
					((DecimalFormat)formatForNumber).applyPattern("000");
					sDTP = formatForNumber.format(startingDayToPlantForThisWindow);

					for (int randomSeedIndex = 0; randomSeedIndex < nRandomSeedsToUse ; randomSeedIndex++) {
						randomSeedToUse = firstRandomSeed + randomSeedIndex;
						((DecimalFormat)formatForNumber).applyPattern("00000");
						randomSeedCode = formatForNumber.format(randomSeedToUse);

						codeName = "pd" + sDTP + "_rs" + randomSeedCode;
						allCodesLine += codeName + "\t";			    
						allOutputLine += thisPixelYields[plantingWindowIndex][randomSeedIndex] + delimiter;

					} // randomSeedIndex
				} // plantingWindowIndex
				allOut.print(allOutputLine.substring(0,allOutputLine.length() - 1) + "\n");
				allCodesOut.print(allCodesLine.substring(0,allCodesLine.length() - 1) + "\n");
			} // if all

			// do the summary out stuff
			statisticsOut.print(minYield + delimiter + maxYield + delimiter + meanYield + delimiter + stdYield + "\n");
			statisticsOut.flush();

			// for the fun of it, reset the totalYield...
//			totalYield = 0;
//			totalYieldSquared = 0;
//			minYield = Integer.MAX_VALUE;
//			maxYield = Integer.MIN_VALUE;

			
			// read the next line
//			dataLine = inininData.readLine();
//			geogLine = inininGeog.readLine();

			// now we're ready to deal with the next pixel...

		} // end for for lineIndex // old end of while loop

		long endTime = System.currentTimeMillis();

		// close out the weather file placeholder
		XFileWriter.close();
		weatherStationFileWriter.close();

		// close out the input file
//		inininData.close();
//		inputStream.close();

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
			allInfoOut.print(nRows + "\t = Number of Rows\n");
			allInfoOut.print(nCols  + "\t = Number of Columns\n");
			allInfoOut.print((nRows * nCols) + "\t = Total Number of Elements\n");
			allInfoOut.print(3 + "\t = The MultiFormatMatrix format the matrix was stored in\n"); // Beware the MAGIC NUMBER!!!
			allInfoOut.print(delimiter + "\t = The string used to delimit elements in the Rows\n");

			allInfoOut.flush();
			allInfoOut.close();
		}

		nCols = 4; // min / max/ mean / std
		statisticsInfoOut.print(nRows + "\t = Number of Rows\n");
		statisticsInfoOut.print(nCols  + "\t = Number of Columns\n");
		statisticsInfoOut.print((nRows * nCols) + "\t = Total Number of Elements\n");
		statisticsInfoOut.print(3 + "\t = The MultiFormatMatrix format the matrix was stored in\n"); // Beware the MAGIC NUMBER!!!
		statisticsInfoOut.print(delimiter + "\t = The string used to delimit elements in the Rows\n");

		statisticsInfoOut.flush();
		statisticsInfoOut.close();




		System.out.println("-- all done at " + new Date() + " = " + (float)((endTime - startTime)/1000.0d) + " s " + (float)((endTime - startTime)/(60*1000.0d)) + " m");
		System.out.println(" total time in DSSAT = " + totalDSSATTimeNanos / 1000000000.0 / 60 + "min ; per run average = " 
				+ totalDSSATTimeNanos / nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth / 1000000.0 + "ms");
		System.out.println("overall per run average = " +
				((endTime - startTime) * 1.0)/ nLinesInDataFile / nRandomSeedsToUse / nPlantingWindowsPerMonth + "ms"
				);

		/////////////////////////////////////////// end new plan ///////////////////////////////////////////////
	} // main

}


//figure out the total number of pixels in input file...
//int nLinesTotal = 72384976;
//RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
//long lastRec=randFile.length();
//randFile.close();
//FileReader inRead = new FileReader(inputFile);
//LineNumberReader lineRead = new LineNumberReader(inRead);
//lineRead.skip(lastRec);
//nLinesTotal = lineRead.getLineNumber();
//lineRead.close();
//inRead.close();
