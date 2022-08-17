package org.ORYZARunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import org.DSSATRunner.DSSATHelperMethods;
import org.R2Useful.*;

public class ORYZAWeatherFromStandalone {
	///////////////////////////////////////////
	// various magic numbers that are needed //
	///////////////////////////////////////////

//	private static final int firstWeatherYear = 0; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly
	private static final int firstWeatherYear = 0; // for when we generate the weather, always start here; the fake planting year will be adjusted accordingly
	private static final int fourDigitYearOffest = 1800; // Beware the MAGIC NUMBER!!! oryza like four digit years, so we have to add an offset to two digit years...

	private final static String DSSATClimateInput  = "ZZZZ";
	private final static String DSSATWeatherOutput = DSSATClimateInput + ".WTG";
	private final static String ORYZAWeatherPrefix = "wdssat";
//	private static final String delimiter = "\t";
	private final static String dosNewLine = "\r\n"; // DOS-style since ORYZA is lame at the moment
//	private final static boolean useDOSNewline = true;
	// we get to hardcode this because i'm too lazy to split this up into a "class" and a "runner" like i probably should...
	private final static String newLineToUse = dosNewLine;
	// we might as well put the official choice in here...
//	if (this.useDOSNewline) {
//		newLineToUse = this.dosNewLine;
//	} else {
//		newLineToUse = "\n"; // whatever is native...
//	}

	private final static double shortwaveMultiplierForDSSAT = 1.0; // the thornton data is in the proper units; oryza needs to be off by 1000
	private final static double SWmultiplier = 1000; // the thornton data is in the proper units; oryza needs to be off by 1000

	
	
	private final static String standaloneCommandTemp = "./weather_generator_standalone"; // magic number!!! name of the standalone weather binary executable...


	
	
	
	
	
	
	

	private static void convertStandaloneWeatherToORYZA(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable, long lineIndex, String outputDirectory, String gisTableBaseName)
	throws FileNotFoundException, IOException, Exception {
		int firstLineIndexWithData = 5; // with the standalone, the first line with info is #6 and it starts on 1901, i guess, so we don't have that funny issue 
//		OLD...
//		Beware the MAGIC NUMBER!!! it happens that 01001 starts us on year 2000, day 366
		// so, we want to start on 2001/001 which is line #13 or index 12
		String stationNumber = "    1"; // Beware the MAGIC NUMBER!!! using padded 1 as the the station number

		String headerLine = null;
		String oryzaStyle = null;
//		String singleLineToBuild = null;
//		String[] splitLine = null;

		int previousYear = -5;
		int statedYear = -1;
		int yearToWriteOryzaFourDigit = -1;
		int lastThreeOfYear = -4;
		int statedDay = -1;
		double sradRaw = -0.3;
		double sradRescaled = -0.32;
		double tmin = 5;
		double tmax = 2;
		double vaporPressure = -99; // Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double windSpeed = 2.5; // and maybe this doesn't work even though it seemed like it did? so, try 2.5... Beware the MAGIC NUMBER!!! this should stay -99 as defaults...
		double rain = -2432.231;


		String[] weatherFileArray = FunTricks.readTextFileToArray(DSSATWeatherOutput);

//		String outputFilename = ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(1, 3);
		String thisCasesOutputDirectory = null;
		String outputFilename = null;
		String tarFilename = null;

		String[] gzipCommand = new String[5]; 
		Process gzipExecution = null; //Runtime.getRuntime().exec(standaloneWeatherCommand , null , null);

//		make the header line for the oryza style...
//		long, lat, elevation, angstrom A, angstrom B (zeros in our case)
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

//		1         2         3         4		
//		01234567890123456789012345678901234567890		
//		@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
//		1001  16.1  14.7  -8.4   0.0  32.2 314.9

		// we need to make the directory to put the pixel's weather data in...
		
		String cleanGISTableBaseName = gisTableBaseName.substring(gisTableBaseName.lastIndexOf("/"));
//		thisCasesOutputDirectory = outputDirectory + cleanGISTableBaseName + "_" + latitude + "N_" + longitude + "E/";
		thisCasesOutputDirectory = "../weather_for_oryza/" + cleanGISTableBaseName + "_" + latitude + "N_" + longitude + "E/";
		// now clean up minus signs...
		thisCasesOutputDirectory = thisCasesOutputDirectory.replaceAll("-", "n");
		File directoryCreator = new File(thisCasesOutputDirectory);
		directoryCreator.mkdir();
		
		
		for (int wLineIndex = firstLineIndexWithData; wLineIndex < weatherFileArray.length; wLineIndex++) {
//			splitLine = weatherFileArray[wLineIndex].trim().split(" +");

			// the weather generator does not front pad the dates with zeros... so empty means year #0
			if (  weatherFileArray[wLineIndex].substring(0,2).trim().equals("")) {
				statedYear = 0;
			} else {
				statedYear = Integer.parseInt(  weatherFileArray[wLineIndex].substring(0,2).trim());
			}
			statedDay  = Integer.parseInt(  weatherFileArray[wLineIndex].substring(2,5).trim());
			rain       = Double.parseDouble(weatherFileArray[wLineIndex].substring(24,29).trim());
			sradRaw    = Double.parseDouble(weatherFileArray[wLineIndex].substring(6,11).trim());
			sradRescaled = sradRaw * SWmultiplier;
			tmax = Double.parseDouble(weatherFileArray[wLineIndex].substring(13,17).trim());
			tmin = Double.parseDouble(weatherFileArray[wLineIndex].substring(18,23).trim());


			if (statedYear != previousYear) {
//				we are done with a whole year, so write out what we have and start the next one...
//				because i'm lazy, we'll end up with one extra file with a junk name....

//				pick the appropriate filename
//				we need the last three digits of the year number
				lastThreeOfYear = previousYear % 1000;
//				Beware the MAGIC NUMBER!!! we will always assume station #1
//				outputFilename = thisCasesOutputDirectory + cleanGISTableBaseName + "_" + latitude + "N_" + longitude + "E_" + ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(lastThreeOfYear, 3);
				outputFilename = thisCasesOutputDirectory + ORYZAWeatherPrefix + "1." + DSSATHelperMethods.padWithZeros(lastThreeOfYear, 3);
				// now clean up minus signs...
				outputFilename = outputFilename.replaceAll("-", "n");

//				write out what we have
				FunTricks.writeStringToFile(oryzaStyle, outputFilename);
				

				

//				update the previous year holder
				previousYear = statedYear;

//				reinitialize with the headerline
				oryzaStyle = headerLine; // initialize with header
			}
//			Beware the MAGIC NUMBER!!! the widths and spacing and what not are what they are but should only matter for the station, year, and day...
//			oryzaStyle += stationNumber + "," 
//			+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedYear), 4) + "," 
//			+ FunTricks.padStringWithLeadingSpaces(Integer.toString(statedDay), 3) + ","
//			+ FunTricks.fitInNCharacters(sradRescaled,9) + ","
//			+ FunTricks.fitInNCharacters(tmin,6) + ","
//			+ FunTricks.fitInNCharacters(tmax,6) + ","
//			+ FunTricks.fitInNCharacters(vaporPressure,6) + ","
//			+ FunTricks.fitInNCharacters(windSpeed,6) + ","
//			+ FunTricks.fitInNCharacters(rain,6) + newLineToUse;

//			statedYear +
			
			yearToWriteOryzaFourDigit = statedYear + fourDigitYearOffest;
			
			oryzaStyle += stationNumber + "," 
			+ yearToWriteOryzaFourDigit + "," 
			+ statedDay + ","
			+ sradRescaled + ","
			+ tmin + ","
			+ tmax + ","
			+ vaporPressure + ","
			+ windSpeed + ","
			+ rain + newLineToUse;

		}

		// let's make a header file
		String headerFileContents = "";
		
		headerFileContents  = "latitude=" + latitude + "N\n";
		headerFileContents += "longitude=" + longitude + "E\n";
		String[] brokenTableName = cleanGISTableBaseName.split("_");
		int nBeforeClimate = 4; // Beware the MAGIC NUMBER!!! input magic code/<blank>/calendar scheme/calendar generic crop scheme
		int nAfterClimate = 4; // Beware the MAGIC!!! month offset/crop/<blank>/water source
		headerFileContents += "intended_water_source=" + brokenTableName[brokenTableName.length-1] + "\n";
		headerFileContents += "climate_description=";
		for (int tableNameIndex = nBeforeClimate; tableNameIndex < brokenTableName.length - nAfterClimate - 1; tableNameIndex++) {
			headerFileContents += brokenTableName[tableNameIndex] + "_";
		}
		headerFileContents += brokenTableName[brokenTableName.length - nAfterClimate] + "\n";
	
		FunTricks.writeStringToFile(headerFileContents, thisCasesOutputDirectory + "header_info.txt");
		
		
		// we should be done with all the years for a particular spot now, so we need to bundle them up....
		//  tar --remove-files -czf a_tar_ball.tar.gz a b

		// we need to re-determine the output file without the year stuff so we can grab all of them...
		tarFilename = outputDirectory + cleanGISTableBaseName + "_" + latitude + "N_" + longitude + "E.tar.gz";
		// now clean up minus signs...
		tarFilename = tarFilename.replaceAll("-", "n");

		// Beware the MAGIC EVERYTHING!!!
		gzipCommand[0] = "tar";
//		gzipCommand[1] = "";
		gzipCommand[1] = "--remove-files";
		gzipCommand[2] = "-czf";
		gzipCommand[3] = tarFilename;
		gzipCommand[4] = thisCasesOutputDirectory;

		// execute DSSAT
		// i don't know that we really need to delete this...
//		weatherOutputFileAsFile.delete(); // clear out any old results file...
		gzipExecution = Runtime.getRuntime().exec(gzipCommand , null , null);

		// try a simple way to wait for the run to finish...
		gzipExecution.waitFor();

//		directoryCreator.delete();
		
	}


	private static void buildORYZAWeatherFromDSSATSpecifySeed(MultiFormatMatrix dataTable,
			MultiFormatMatrix geogTable, long lineIndex, String randomSeedCode, int nRepetitionsToUse, String outputDirectory, String gisTableBaseName)
	throws Exception         {

		// build the climate file...
		String climateFileContents = DSSATHelperMethods.cliFileContentsAllSupplied(dataTable, geogTable, lineIndex, shortwaveMultiplierForDSSAT);

		// write out the climate file
		FunTricks.writeStringToFile(climateFileContents, DSSATClimateInput + ".CLI");

		// build and write the fileX

		// determine the ending date
		// the ending date is just the number of repetitions (plus one in case we run over) as the year and 365 as the day...

		// Beware the MAGIC NUMBER!!! We will use a finite number of years since we only need one growing season's worth here...
//		int nRepetitionsToUse = 3; // we might overrun the end of the year and then need to go another year for bad cases...

		String startingDate = Integer.toString(firstWeatherYear * 1000 + 1); // Beware the MAGIC NUMBER!!!
		String endingDate = Integer.toString((nRepetitionsToUse + 3) * 1000 + 1);

//		String yearCode = DSSATHelperMethods.pad2CharactersZeros(nRepetitionsToUse);
//		String dayCode = "365"; // Beware the MAGIC NUMBER!!! we are going to the approximate last day of the year...
//		String endingDate = yearCode + dayCode;
//		String actualXFile = fileXTemplateForDSSATWeatherContents
//		.replaceAll(this.endingDateDSSATPlaceholder, endingDate)
//		.replaceAll(randomSeedDSSATPlaceholder, randomSeedCode);

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
				DSSATClimateInput + ".CLI",
				DSSATClimateInput + ".WTG"
		};


		// execute DSSAT
		// i don't know that we really need to delete this...
//		weatherOutputFileAsFile.delete(); // clear out any old results file...
//		for (int pIndex = 0; pIndex < standaloneWeatherCommand.length; pIndex++) {
//			System.out.println("command[" + pIndex + "] = " + standaloneWeatherCommand[pIndex]);
//		}
		Process weatherExecution = Runtime.getRuntime().exec(standaloneWeatherCommand , null , null);

		// try a simple way to wait for the run to finish...
		weatherExecution.waitFor();

		// interpret the output
//		this.convertDSSATWeatherToORYZA(dataTable, geogTable, lineIndex);
		convertStandaloneWeatherToORYZA(dataTable, geogTable, lineIndex, outputDirectory, gisTableBaseName);

		// make sure the streams got closed for the process after we managed to read everything...
		weatherExecution.getErrorStream().close();
		weatherExecution.getInputStream().close();
		weatherExecution.getOutputStream().close();
		weatherExecution.destroy(); // just in case it is still going...

		// recommend garbage collection
		System.gc();
	}

	public static void main(String commandLineOptions[]) throws Exception {

		TimerUtility bigTimer = new TimerUtility();

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////


		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.println(i + " [" + commandLineOptions[i] + "]");
		}

		System.out.println();

		String gisTableBaseName = commandLineOptions[0];
		String outputDirectory = commandLineOptions[1];
		int firstRandomSeed = Integer.parseInt(commandLineOptions[2]);
		int nYearsToRun =  Integer.parseInt(commandLineOptions[3]);
		


		
		
		bigTimer.tic();

		// read in the fundamental data
		// Beware the MAGIC NUMBER!!! gonna force these into memory
		int formatIndexToForce = MultiFormatMatrix.dataInVector;
		MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_data",formatIndexToForce);
		MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisTableBaseName + "_geog",formatIndexToForce);


		int nLinesInDataFile = (int)dataMatrix.getDimensions()[0];

		String randomSeedActualValue = DSSATHelperMethods.padWithZeros(firstRandomSeed,5);

		double timeSinceStart, projectedTime, timeRemaining;
		
		for (int dataLineIndex = 0; dataLineIndex < nLinesInDataFile; dataLineIndex++) {
			
			buildORYZAWeatherFromDSSATSpecifySeed(dataMatrix, geogMatrix, dataLineIndex, randomSeedActualValue, nYearsToRun, outputDirectory, gisTableBaseName);

			if (dataLineIndex % (nLinesInDataFile / 400 + 1) == 0) {

				timeSinceStart = bigTimer.sinceStartSeconds();
//				averageTimePerEvent = timeForWeather.getMean() + timeForORYZA.getMean();
				projectedTime = timeSinceStart / (dataLineIndex + 1) * nLinesInDataFile;
				timeRemaining = projectedTime - timeSinceStart;

				System.out.println("prog: " + (dataLineIndex + 1) + "/" + nLinesInDataFile + " = " +
						FunTricks.fitInNCharacters((100 * (dataLineIndex + 1.0)/nLinesInDataFile),5) +
						" N/P/R = " + FunTricks.fitInNCharacters(timeSinceStart,6) +
						"/" + FunTricks.fitInNCharacters(projectedTime,6) +
						"/" + FunTricks.fitInNCharacters(timeRemaining,6) +
						"\t" + FunTricks.fitInNCharacters(timeRemaining / 60, 6)
				);
			}

			
		}



		System.out.println(bigTimer.sinceStartMessage());

	} // main

}

