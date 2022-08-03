package org.DSSATRunner;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.PrintWriter;
//import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
//import java.util.Date;

import org.R2Useful.*;

public class DSSATHelperMethods {

	public static final double[] averageDaysInMonth = { 31, 28.24, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	public static double makePositive(double original) {
		if (original >= 0) {
			return original;
		}
		return 0.0;
	}
	
	public static int monthToDayOfYearOnFirstIgnoreLeap(int plantingMonth) {
		// figure out an approximate starting day-of-the-year that corresponds to this planting month
		// we are ignoring
		int firstPlantingDay = -5123;
		switch (plantingMonth) {
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

		return firstPlantingDay;
	}
	
	 static public int monthIndexFromDayNumber(int dayOfYearNumber) {
		 
		 if (dayOfYearNumber <= 31) {
			 return 0; // January = 0 in this case because we will use it for array indices...
		 } else if (dayOfYearNumber <= 59) {
			 return 1; // February
		 } else if (dayOfYearNumber <= 90) {
			 return 2; // March
		 } else if (dayOfYearNumber <= 120) {
			 return 3; // April
		 } else if (dayOfYearNumber <= 151) {
			 return 4; // May
		 } else if (dayOfYearNumber <= 181) {
			 return 5; // June
		 } else if (dayOfYearNumber <= 212) {
			 return 6; // July
		 } else if (dayOfYearNumber <= 243) {
			 return 7; // August
		 } else if (dayOfYearNumber <= 273) {
			 return 8; // September
		 } else if (dayOfYearNumber <= 304) {
			 return 9; // October
		 } else if (dayOfYearNumber <= 334) {
			 return 10; // November
		 } else {
			 return 11; // December
		 }
	 }

	public static Object[] cliFileContentsAndOtherInfo(String dataLine, String geogLine, double SWmultiplier) {
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
		contentsOfCLIFile += "  1901  9999  0.25  0.50  0.00  0.00" + "\n";
//		contentsOfCLIFile += "  2000    15  0.25  0.50  0.00  0.00" + "\n";
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


	public static String cliFileContentsMFM(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable,
				long lineIndex, double SWmultiplier) throws Exception {
	
	
			String contentsOfCLIFile = "";
	
	//		String delimiter = "\t";
	//		String dataLine = "";
	//		String geogLine = "";
	
	//		String[] dataLineSplit = dataLine.split(delimiter);
	//		String[] geogLineSplit = geogLine.split(delimiter);
	
			// the geographic info
	//		double latitude  = Double.parseDouble(geogLineSplit[2]);
	//		double longitude = Double.parseDouble(geogLineSplit[3]);
			double latitude  = geogTable.getValue(lineIndex,2);
			double longitude = geogTable.getValue(lineIndex,3);
	
			// the data stuff
	//		double soilType      = Double.parseDouble(dataLineSplit[0]);
	//		double elevation     = Double.parseDouble(dataLineSplit[1]);
	//		double plantingMonth = Double.parseDouble(dataLineSplit[2]);
			double soilType      = dataTable.getValue(lineIndex,0);
			double elevation     = dataTable.getValue(lineIndex,1);
			double plantingMonth = dataTable.getValue(lineIndex,2);
	//		double nitrogenLevel = dataTable.getValue(lineIndex,3);
	
			int nMonths = 12; // this actually needs to be twelve
			double[] monthlySW        = new double[nMonths];
			double[] monthlyTmax      = new double[nMonths];
			double[] monthlyTmin      = new double[nMonths];
			double[] monthlyPrec      = new double[nMonths];
			double[] monthlyRainydays = new double[nMonths];
	
			int nBeforeMonthly = 4; // this needs to reflect how many variables come before the monthly data
	
			// soilType (1st col) / elevation (2nd col) / planting month (3rd) / SW (4th and on)
			int shifter = nBeforeMonthly + nMonths * 0; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
	//			monthlySW[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) * SWmultiplier;
				monthlySW[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) * SWmultiplier;
				// make sure the value is non-negative
				if (monthlySW[monthIndex] < 0) {
					monthlySW[monthIndex] = 0.0;
				}
			}
	
			// temperature are in tenths of a degree, so we need to divide the raw value by 10
			shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
	//			monthlyTmax[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
				monthlyTmax[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			}
	
			// temperature are in tenths of a degree, so we need to divide the raw value by 10
			shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
	//			monthlyTmin[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
				monthlyTmin[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			}
	
			shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
	//			monthlyPrec[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
				monthlyPrec[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
				if (monthlyPrec[monthIndex] < 0) {
					monthlyPrec[monthIndex] = 0.0;
				}
			}
	
			shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
			for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
	//			monthlyRainydays[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
				monthlyRainydays[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
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
	
	//		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};
	
			return contentsOfCLIFile;
		
		
		}

	
	public static String cliFileContentsAllSuppliedONLYCLIMATEINFO(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable,
			long lineIndex, double SWmultiplier) throws Exception {



		String contentsOfCLIFile = "";

		// some magic numbers;
		double smallRainyDays = 0.1;

		// the geographic info
		double latitude  = geogTable.getValue(lineIndex,2);
		double longitude = geogTable.getValue(lineIndex,3);

		// the data stuff
		double elevation     = dataTable.getValue(lineIndex,0);
		double idNumber = dataTable.getValue(lineIndex,67); 
		
		int nMonths = 12; // this actually needs to be twelve
		double[] monthlySW        = new double[nMonths];
		double[] monthlyTmax      = new double[nMonths];
		double[] monthlyTmin      = new double[nMonths];
		double[] monthlyPrec      = new double[nMonths];
		double[] monthlyRainydays = new double[nMonths];

		int nBeforeMonthly = 1; // this needs to reflect how many variables come before the monthly data

		// elevation (1st col) / SW (2nd and on)
		int shifter = nBeforeMonthly + nMonths * 0; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlySW[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) * SWmultiplier;
			monthlySW[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) * SWmultiplier;
			// make sure the value is non-negative
			if (monthlySW[monthIndex] < 0) {
				monthlySW[monthIndex] = 0.0;
			}
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmax[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmax[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmin[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmin[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
		}

		shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyPrec[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyPrec[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
			if (monthlyPrec[monthIndex] < 0) {
				monthlyPrec[monthIndex] = 0.0;
			}
		}

		shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyRainydays[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyRainydays[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
			if (monthlyRainydays[monthIndex] < 0) {
				monthlyRainydays[monthIndex] = 0.0;
			}
			// do some checking to make sure that rainydays and actual rain are coherent
			if (monthlyPrec[monthIndex] > 0 && monthlyRainydays[monthIndex] <= 0) {
				monthlyRainydays[monthIndex] = smallRainyDays; // Beware the MAGIC NUMBER!!!
			}
			else if (monthlyPrec[monthIndex] <= 0) {
				monthlyRainydays[monthIndex] = 0; // Beware the MAGIC NUMBER!!!
			}
		}

		// additional things supplied by bro. thorton are:
		// txy -> yearly maximum temp 
		// tt  -> yearly ave temp
		// tny -> yearly minimum temp
		// rt  -> total rainfall
		// amp -> amplitude
		// sry -> ave solar
		// elev

		
		// initialize some values
		// Beware the MAGIC NUMBERS!!! the indices for these values...
		int yearlyValueIndex = nBeforeMonthly + nMonths * 5; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		
//		contentsOfCLIFile += "  RICK   " + latitudeFormatted + "  " + longitudeFormatted + " " + elevationFormatted
//		+ " " + averageTempFormatted + " " + amplitudeFormatted + " " + averageSWFormatted + " " + averageMaxTempFormatted + 
//		" " + averageMinTempFormatted + "  " + totalPrecipitationFormatted + "\n";

		double averageSW             = dataTable.getValue(lineIndex,yearlyValueIndex++);
		averageSW = makePositive(averageSW);
		double averageMaxTemperature = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double averageMinTemperature = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double averageTemperature    = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double temperatureAmplitude  = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double totalPrecipitation    = dataTable.getValue(lineIndex,yearlyValueIndex++);
		totalPrecipitation = makePositive(totalPrecipitation);


		// to format something...			
		NumberFormat formatForNumber = NumberFormat.getInstance();

		contentsOfCLIFile = "";
		// create all the stuff for the CLI files... this will be exceedingly magical
		contentsOfCLIFile += "*CLIMATE : pixel id number = " + idNumber + " ; Elevation = " + elevation + "\n";
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

//		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return contentsOfCLIFile;
	}

	

	public static String cliFileContentsAllSupplied(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable,
			long lineIndex, double SWmultiplier) throws Exception {


		String contentsOfCLIFile = "";

		// some magic numbers;
		double smallRainyDays = 0.1;

		// the geographic info
		double latitude  = geogTable.getValue(lineIndex,2);
		double longitude = geogTable.getValue(lineIndex,3);

		// the data stuff
//		double soilType      = Double.parseDouble(dataLineSplit[0]);
//		double elevation     = Double.parseDouble(dataLineSplit[1]);
//		double plantingMonth = Double.parseDouble(dataLineSplit[2]);
		double soilType      = dataTable.getValue(lineIndex,0);
		double elevation     = dataTable.getValue(lineIndex,1);
		double plantingMonth = dataTable.getValue(lineIndex,2);
		double nitrogenLevel = dataTable.getValue(lineIndex,3);

		int nMonths = 12; // this actually needs to be twelve
		double[] monthlySW        = new double[nMonths];
		double[] monthlyTmax      = new double[nMonths];
		double[] monthlyTmin      = new double[nMonths];
		double[] monthlyPrec      = new double[nMonths];
		double[] monthlyRainydays = new double[nMonths];

		int nBeforeMonthly = 4; // this needs to reflect how many variables come before the monthly data

		// soilType (1st col) / elevation (2nd col) / planting month (3rd) / SW (4th and on)
		int shifter = nBeforeMonthly + nMonths * 0; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlySW[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) * SWmultiplier;
			monthlySW[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) * SWmultiplier;
			// make sure the value is non-negative
			if (monthlySW[monthIndex] < 0) {
				monthlySW[monthIndex] = 0.0;
			}
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmax[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmax[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmin[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmin[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
		}

		shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyPrec[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyPrec[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
			if (monthlyPrec[monthIndex] < 0) {
				monthlyPrec[monthIndex] = 0.0;
			}
		}

		shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyRainydays[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyRainydays[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
			if (monthlyRainydays[monthIndex] < 0) {
				monthlyRainydays[monthIndex] = 0.0;
			}
			// do some checking to make sure that rainydays and actual rain are coherent
			if (monthlyPrec[monthIndex] > 0 && monthlyRainydays[monthIndex] <= 0) {
				monthlyRainydays[monthIndex] = smallRainyDays; // Beware the MAGIC NUMBER!!!
			}
			else if (monthlyPrec[monthIndex] <= 0) {
				monthlyRainydays[monthIndex] = 0; // Beware the MAGIC NUMBER!!!
			}
		}

		// additional things supplied by bro. thorton are:
		// txy -> yearly maximum temp 
		// tt  -> yearly ave temp
		// tny -> yearly minimum temp
		// rt  -> total rainfall
		// amp -> amplitude
		// sry -> ave solar
		// elev

		
		// initialize some values
		// Beware the MAGIC NUMBERS!!! the indices for these values...
		int yearlyValueIndex = nBeforeMonthly + nMonths * 5; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		
//		contentsOfCLIFile += "  RICK   " + latitudeFormatted + "  " + longitudeFormatted + " " + elevationFormatted
//		+ " " + averageTempFormatted + " " + amplitudeFormatted + " " + averageSWFormatted + " " + averageMaxTempFormatted + 
//		" " + averageMinTempFormatted + "  " + totalPrecipitationFormatted + "\n";

		double averageSW             = dataTable.getValue(lineIndex,yearlyValueIndex++);
		averageSW = makePositive(averageSW);
		double averageMaxTemperature = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double averageMinTemperature = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double averageTemperature    = dataTable.getValue(lineIndex,yearlyValueIndex++);
		// new for 08sep14, let's censor the average temperature just above zero so that it won't get defaulted to 20.0
		// hopefully, that will be more reasonable for low temperature situations
		double magicNearZeroCensoringValueForAverageTemperature = 0.1;
		if (averageTemperature < magicNearZeroCensoringValueForAverageTemperature) {
			averageTemperature = magicNearZeroCensoringValueForAverageTemperature;
		}
		double temperatureAmplitude  = dataTable.getValue(lineIndex,yearlyValueIndex++);
		double totalPrecipitation    = dataTable.getValue(lineIndex,yearlyValueIndex++);
		totalPrecipitation = makePositive(totalPrecipitation);


		// figure out an approximate starting day-of-the-year that corresponds to this planting month
		// we are ignoring
		int firstPlantingDay = monthToDayOfYearOnFirstIgnoreLeap((int)plantingMonth);

		// to format something...			
		NumberFormat formatForNumber = NumberFormat.getInstance();

		((DecimalFormat)formatForNumber).applyPattern("00");
		String soilTypeFormatted = formatForNumber.format(soilType);
		String plantingMonthFormatted = formatForNumber.format(plantingMonth);
		((DecimalFormat)formatForNumber).applyPattern("###");
		String firstPlantingDayFormatted = formatForNumber.format(firstPlantingDay);


		contentsOfCLIFile = "";
		// create all the stuff for the CLI files... this will be exceedingly magical
		contentsOfCLIFile += "*CLIMATE : Soil Number = " + soilTypeFormatted + " ; Elevation = " + elevation + " ; Planting Month = " + plantingMonthFormatted + " ; First Planting Day = " + firstPlantingDayFormatted + " ; total N assumption = " + nitrogenLevel + "\n";
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

//		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return contentsOfCLIFile;
	
	
	}
	public static String cliFileContentsOldMFM(MultiFormatMatrix dataTable, MultiFormatMatrix geogTable,
			long lineIndex, double SWmultiplier) throws Exception {

		String contentsOfCLIFile = "";

//		String delimiter = "\t";
//		String dataLine = "";
//		String geogLine = "";

//		String[] dataLineSplit = dataLine.split(delimiter);
//		String[] geogLineSplit = geogLine.split(delimiter);

		// the geographic info
//		double latitude  = Double.parseDouble(geogLineSplit[2]);
//		double longitude = Double.parseDouble(geogLineSplit[3]);
		double latitude  = geogTable.getValue(lineIndex,2);
		double longitude = geogTable.getValue(lineIndex,3);

		// the data stuff
//		double soilType      = Double.parseDouble(dataLineSplit[0]);
//		double elevation     = Double.parseDouble(dataLineSplit[1]);
//		double plantingMonth = Double.parseDouble(dataLineSplit[2]);
		double soilType      = dataTable.getValue(lineIndex,0);
		double elevation     = dataTable.getValue(lineIndex,1);
		double plantingMonth = dataTable.getValue(lineIndex,2);

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
//			monthlySW[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) * SWmultiplier;
			monthlySW[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) * SWmultiplier;
			// make sure the value is non-negative
			if (monthlySW[monthIndex] < 0) {
				monthlySW[monthIndex] = 0.0;
			}
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 1; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmax[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmax[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
		}

		// temperature are in tenths of a degree, so we need to divide the raw value by 10
		shifter = nBeforeMonthly + nMonths * 2; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyTmin[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
			monthlyTmin[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex) / 10; // Beware the MAGIC NUMBER!! tenths of a degree
		}

		shifter = nBeforeMonthly + nMonths * 3; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyPrec[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyPrec[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
			if (monthlyPrec[monthIndex] < 0) {
				monthlyPrec[monthIndex] = 0.0;
			}
		}

		shifter = nBeforeMonthly + nMonths * 4; // Beware the MAGIC NUMBER!!! the column index of the first month for this variable
		for (int monthIndex = 0 ; monthIndex < nMonths ; monthIndex++) {
//			monthlyRainydays[monthIndex] = Double.parseDouble(dataLineSplit[shifter + monthIndex]);
			monthlyRainydays[monthIndex] = dataTable.getValue(lineIndex,shifter + monthIndex);
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

//		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return contentsOfCLIFile;
	
	}
	

	public static int firstPlantingDateFromMonth(int plantingMonth) {
		int firstPlantingDay = -5123;
		switch (plantingMonth) {
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
		return firstPlantingDay;
	}
	public static int[] soilElevMonthDayNitrogenMFM(MultiFormatMatrix dataTable, long lineIndex)  throws Exception {
	
	
	//		String delimiter = "\t";
	
	//		String[] dataLineSplit = dataLine.split(delimiter);
	
	
			// the data stuff
	//		double soilType      = Double.parseDouble(dataLineSplit[0]);
	//		double elevation     = Double.parseDouble(dataLineSplit[1]);
	//		double plantingMonth = Double.parseDouble(dataLineSplit[2]);
	
			double soilType      = dataTable.getValue(lineIndex,0);
			double elevation     = dataTable.getValue(lineIndex,1);
			double plantingMonth = dataTable.getValue(lineIndex,2);
			double nitrogenLevel = dataTable.getValue(lineIndex,3);
	
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
	
			int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay, (int)nitrogenLevel};
	
			return nonClimateInfo;
		}


	public static int[] soilElevMonthDayMFM(MultiFormatMatrix dataTable, long lineIndex)  throws Exception {

//		String delimiter = "\t";

//		String[] dataLineSplit = dataLine.split(delimiter);


		// the data stuff
//		double soilType      = Double.parseDouble(dataLineSplit[0]);
//		double elevation     = Double.parseDouble(dataLineSplit[1]);
//		double plantingMonth = Double.parseDouble(dataLineSplit[2]);

		double soilType      = dataTable.getValue(lineIndex,0);
		double elevation     = dataTable.getValue(lineIndex,1);
		double plantingMonth = dataTable.getValue(lineIndex,2);

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

		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return nonClimateInfo;
	}

	
	public static String cliFileContents(String dataLine, String geogLine, double SWmultiplier) {
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

//		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return contentsOfCLIFile;
	}


	public static int[] soilElevMonthDay(String dataLine) {

		String delimiter = "\t";

		String[] dataLineSplit = dataLine.split(delimiter);


		// the data stuff
		double soilType      = Double.parseDouble(dataLineSplit[0]);
		double elevation     = Double.parseDouble(dataLineSplit[1]);
		double plantingMonth = Double.parseDouble(dataLineSplit[2]);

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

		int[] nonClimateInfo = new int[] {(int)soilType, (int)elevation, (int)plantingMonth, firstPlantingDay};

		return nonClimateInfo;
	}

	public static String padWithZeros(double value, int stringLength) {
		NumberFormat formatForNumber = NumberFormat.getInstance();

		String patternFormat = "";
		for (int index = 0; index < stringLength; index++) {
			patternFormat += "0";
		}
		
		((DecimalFormat)formatForNumber).applyPattern(patternFormat);

	  return formatForNumber.format(value);
	}


	public static String pad3CharactersZeros(double d) {
		NumberFormat formatForNumber = NumberFormat.getInstance();

		((DecimalFormat)formatForNumber).applyPattern("000");

	  return formatForNumber.format(d);
	}

	public static String pad2CharactersZeros(double d) {
		NumberFormat formatForNumber = NumberFormat.getInstance();

		((DecimalFormat)formatForNumber).applyPattern("00");

	  return formatForNumber.format(d);
	}
	
	public static int yyyyDDDdifferencerIgnoreLeap(int startDate, int endDate) {
		
		if (startDate < 0 || endDate < 0) {
			return -1;
		} else if (startDate > endDate) {
			return -2;
		}
		int startYear = startDate / 1000;
		int   endYear =   endDate / 1000;
		
		int startDays = startDate - startYear * 1000;
		int   endDays =   endDate - endYear * 1000;
		
		return (endYear - startYear) * 365 + (endDays - startDays);
		
		
	}

	public static int yyyyDDDaddDaysIgnoreLeap(int startDate, int nDaysToAdd) {
		
		int startYear = startDate / 1000;
		int startDays = startDate - startYear * 1000;

		int   endDaysTemp =   startDays + nDaysToAdd;

		int   endDays =   -1;
		int   endYear =   -1;

		
		if (endDaysTemp >= 1 && endDaysTemp <= 365) {
			// no worries, the year is the same.
			endYear = startYear % 100;
			endDays = endDaysTemp;
		} else if (endDaysTemp < 1) {
			// we need to back up a few years
			int yearsToChange = endDaysTemp / 365 - 1;
//			System.out.println("eDT = " + endDaysTemp + "; eDT/365 = " + (endDaysTemp / 365) + "; yTC = " + yearsToChange);
			endYear = (startYear + yearsToChange) % 100;
			endDays = endDaysTemp % 365 + 365;
			
		} else if (endDaysTemp > 365) {
			// we need to back up a few years
			int yearsToChange = endDaysTemp / 365;
//			System.out.println("eDT = " + endDaysTemp + "; eDT/365 = " + (endDaysTemp / 365) + "; yTC = " + yearsToChange);
			endYear = (startYear + yearsToChange) % 100;
			endDays = endDaysTemp % 365;
		}			


		
		
		return endYear * 1000 + endDays;
		
		
	}

	public static String yearDayToYYDDD(int year, int day) {
		
		if (year < 0 || day <= 0) {
			System.out.println("yearDayMaker: negative/zero arguments [year = " + year + ", day = " + day + "]");
			return null;
		}
		
		int twoDigitYear = year % 100;
		
		int threeDigitDay = -1;
		if (day > 366) {
			threeDigitDay = 365;
		} else {
			threeDigitDay = day;
		}

		String yearString = null;
		String dayString = null;
		if (twoDigitYear < 10) {
			yearString = "0" + Integer.toString(twoDigitYear);
		} else {
			yearString = Integer.toString(twoDigitYear);
		}

		if (threeDigitDay < 10) {
			dayString = "00" + Integer.toString(threeDigitDay);
		}	else if (threeDigitDay < 100) {
				dayString = "0" + Integer.toString(threeDigitDay);
		} else {
			dayString = Integer.toString(threeDigitDay);
		}

		return yearString + dayString;
	}
	
	static public int[] yyDDDtoyyANDddd(int yyDDD) {
    // the date is YYddd, so if we do integer division by a thousand, we get the year...
    int originalYear = yyDDD / 1000;

    // subtract off the year to get the days...
    int originalDayOfYear = yyDDD - 1000 * originalYear;
    
    return new int[] {originalYear , originalDayOfYear};
	}

	
}

