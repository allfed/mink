package org.ifpri_converter;
import java.io.BufferedReader;
//import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
//import java.util.concurrent.*;

public class MagicDSSATClimateProfileMaker {


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
			System.out.println("Usage: org.ifpri_converter.MagicDSSATClimateProfileMaker input_base_name output_base_name SW_multiplier delimiter\n" +
					"\n" +
					"This program will take a magically produced text table of climate/elevation/soil and create DSSAT style *.CLI files.\n" +
					"Everything is brute force and magical. This will create a CLI file for each line in the table. They will be systematically\n" +
					"named, but not directly useful in DSSAT. First, you will have to rename/copy/move it to a four letter name.\n" +
					"\n" +
					"SW_multiplier is the value to multiply the short-wave radiation numbers by in order to get the proper units. the final\n" +
					"units should be MJ/m^2/day" +
					"\n" +
					"The default delimiter is tab.\n" +
					"\n" +
					"The geography file should have four columns: row / col / latitude / longitude" +
					"\n" +
					"WARNING!!! Nothing is idiot proofed!\n" +
					"\n");
			System.exit(1);
		}

		String inputBaseName  = 									 commandLineOptions[0];
		String outputBaseName = 									 commandLineOptions[1];
		double SWmultiplier   = Double.parseDouble(commandLineOptions[2]);
		
		String delimiter = "\t";
		if (commandLineOptions.length > 3) {
			delimiter      = commandLineOptions[3];
		}

		System.out.println("delimiter = [" + delimiter + "]");
//		// use comma as default value
//		if (delimiter.isEmpty()) {
//			delimiter = "\t";
//		}
		
		
		/*
		
		elevation
		soil type
		latitude
		longitude
		
		monthly:
		SW
		tmax
		tmin
		prec
		rainydays
		
		*/

		// declare placeholders for the output stuff
		File outputFileToWrite = null;
		FileWriter outputStream = null;
		PrintWriter outoutout = null;

		
		// open up data file
		FileReader inputStream = new FileReader(inputBaseName + "_data.txt");
		BufferedReader ininin = new BufferedReader(inputStream);

		// open up the geography file
		FileReader geogInputStream = new FileReader(inputBaseName + "_geog.txt");
		BufferedReader inininGeog = new BufferedReader(geogInputStream);

		String lineContents = null;
		String[] lineContentsSplit = null;

		String lineContentsGeog = null;
		String[] lineContentsGeogSplit = null;

		// while loop that steps through the file...
		// initialize with the very first line
		long nLines = 0;
		
		double latitude = Double.NaN;
		double longitude = Double.NaN;
		
		double soilType, elevation, plantingMonth;
		int firstPlantingDay = -1;

		int shifter = -1234;
		int nBeforeMonthly = -5;
		int nMonths = 12;
		double[] monthlySW        = new double[nMonths];
		double[] monthlyTmax      = new double[nMonths];
		double[] monthlyTmin      = new double[nMonths];
		double[] monthlyPrec      = new double[nMonths];
		double[] monthlyRainydays = new double[nMonths];
		
		nBeforeMonthly = 3;
		lineContents     = ininin.readLine();
		lineContentsGeog = inininGeog.readLine();
		while (lineContents != null) {
			nLines++;
//			System.out.println("now on line #" + nLines + "[" + lineContents + "]");
//			System.out.println("now on line #" + nLines + "[" + lineContentsGeog + "]");
			
			lineContentsSplit     = lineContents.split(delimiter);
			lineContentsGeogSplit = lineContentsGeog.split(delimiter);

			// the geographic info
			latitude  = Double.parseDouble(lineContentsGeogSplit[2]);
			longitude = Double.parseDouble(lineContentsGeogSplit[3]);
			
			// the data stuff
			soilType      = Double.parseDouble(lineContentsSplit[0]);
			elevation     = Double.parseDouble(lineContentsSplit[1]);
			plantingMonth = Double.parseDouble(lineContentsSplit[2]);
			
			
			// soilType (1st col) / elevation (2nd col) / planting month (3rd) / SW (4th and on)
			shifter = nBeforeMonthly + nMonths * 0; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
				monthlySW[monthIndex] = Double.parseDouble(lineContentsSplit[shifter + monthIndex]) * SWmultiplier;
				// make sure the value is non-negative
				if (monthlySW[monthIndex] < 0) {
					monthlySW[monthIndex] = 0.0;
				}
			}

			// temperature are in tenths of a degree, so we need to divide the raw value by 10
			shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
				monthlyTmax[monthIndex] = Double.parseDouble(lineContentsSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			}

			// temperature are in tenths of a degree, so we need to divide the raw value by 10
			shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
				monthlyTmin[monthIndex] = Double.parseDouble(lineContentsSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			}

			shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
				monthlyPrec[monthIndex] = Double.parseDouble(lineContentsSplit[shifter + monthIndex]);
				if (monthlyPrec[monthIndex] < 0) {
					monthlyPrec[monthIndex] = 0.0;
				}
			}
			
			shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
				monthlyRainydays[monthIndex] = Double.parseDouble(lineContentsSplit[shifter + monthIndex]);
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
			
			// write out the output file
			outputFileToWrite = new File(outputBaseName + nLines + ".txt");
			outputStream = new FileWriter(outputFileToWrite);
			outoutout = new PrintWriter(outputStream);

	    // figure out an approximate starting day-of-the-year that corresponds to this planting month
	    // we are ignoring
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
	    }

			// to format something...			
	    NumberFormat formatForNumber = NumberFormat.getInstance();

	    ((DecimalFormat)formatForNumber).applyPattern("00");
	    String soilTypeFormatted = formatForNumber.format(soilType);
	    String plantingMonthFormatted = formatForNumber.format(plantingMonth);
	    ((DecimalFormat)formatForNumber).applyPattern("###");
	    String firstPlantingDayFormatted = formatForNumber.format(firstPlantingDay);
	    

			// create all the stuff for the CLI files... this will be exceedingly magical
			outoutout.println("*CLIMATE : Soil Number = " + soilTypeFormatted + " ; Elevation = " + elevation + " ; Planting Month = " + plantingMonthFormatted + " ; First Planting Day = " + firstPlantingDayFormatted);
			outoutout.println("@ INSI      LAT     LONG  ELEV   TAV   AMP  SRAY  TMXY  TMNY  RAIY");


	    
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
			
			
			outoutout.println("  RICK   " + latitudeFormatted + "  " + longitudeFormatted + " " + elevationFormatted
					+ " " + averageTempFormatted + " " + amplitudeFormatted + " " + averageSWFormatted + " " + averageMaxTempFormatted + 
					" " + averageMinTempFormatted + "  " + totalPrecipitationFormatted);
			
			outoutout.println("@START  DURN  ANGA  ANGB REFHT WNDHT");
			outoutout.println("  2000    15  0.25  0.50  0.00  0.00");
			outoutout.println("@ GSST  GSDU");
			outoutout.println("     0     0");
			outoutout.println();
			outoutout.println("*MONTHLY AVERAGES");
			outoutout.println("@MONTH  SAMN  XAMN  NAMN  RTOT  RNUM");
			
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
		    
		    outoutout.println(monthFormatted + " " + swFormatted + " " + tmaxFormatted + " " + tminFormatted + " " + precFormatted + " " + rainydaysFormatted);
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
			
			

			// close out the files
			outoutout.flush();
			outoutout.close();
			outputStream.close();

			// read the next line
			lineContents     = ininin.readLine();
			lineContentsGeog = inininGeog.readLine();

		} // end of while loop
		
		// close out the input file
		ininin.close();
		inputStream.close();
		
		

		
		long endTime = System.currentTimeMillis();
    
		System.out.println("-- all done at " + new Date() + " = " + (float)((endTime - startTime)/1000.0d) + " s " + (float)((endTime - startTime)/(60*1000.0d)) + " m");

	} // main

}


// figure out the total number of pixels in input file...
//int nLinesTotal = 72384976;
//RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
//long lastRec=randFile.length();
//	randFile.close();
//FileReader inRead = new FileReader(inputFile);
//LineNumberReader lineRead = new LineNumberReader(inRead);
//lineRead.skip(lastRec);
//nLinesTotal = lineRead.getLineNumber();
//lineRead.close();
//inRead.close();
