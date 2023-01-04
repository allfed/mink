package org.ifpri_converter;

import java.io.File;
import java.io.PrintWriter;

import org.R2Useful.*;
//import java.io.BufferedReader;
////import java.io.File;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.LineNumberReader;
//import java.io.PrintWriter;
//import java.io.RandomAccessFile;
//import java.util.Date;
//import java.util.concurrent.*;

public class MagicCropLifeReader1 {


	public static void main(String commandLineOptions[]) throws Exception {


//		Date startTime = new Date();

		TimerUtility timerThing = new TimerUtility();

		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		System.out.print("       command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print("       " + i + " " + commandLineOptions[i] + "\n");
		}
		System.out.println();

		if (commandLineOptions.length == 0) {
			System.out.println("go look at the source code... sorry.\n");
			System.exit(1);
		}

		String cellIDNumberFile              = commandLineOptions[0];
		String dataFile                      = commandLineOptions[1];
		String outputBaseName                = commandLineOptions[2];
		int    skipThisMany = Integer.parseInt(commandLineOptions[3]);
		int    firstYear    = Integer.parseInt(commandLineOptions[4]);
		int    lastYear     = Integer.parseInt(commandLineOptions[5]);
		int    nRepetitions = Integer.parseInt(commandLineOptions[6]);

		// my plan is the step through the cellIDNumberFile and then match up with the data file to keep everything in order
		// that was very opaque...
		
		// MAGIC NUMBERS!!!
		final String delimiter = ",";
		
		// find these via... cut filename.txt | cut -f6 | sort | uniq
//		String[] ListCultivarCode  = {"HC0001","HC0002"};
//		String[] ListClimateCode   = {"mira12050","WorldClimbase2000"};
//		String[] ListTechnologyKey = {"B-Y-TI038-N-N"};
		
		// name the columns
		int IndexUnitID        = 0;
		int IndexCELL30M       = 1;
		int IndexSoilID        = 2;
		int IndexSoilProfile   = 3;
		int IndexSoilShare     = 4;
		int IndexCropCode      = 5;
		int IndexCultivarCode  = 6;
		int IndexClimateCode   = 7;
		int IndexRepID         = 8;
		int IndexTechnologyKey = 9;
		int IndexPYEAR         = 10;
		int IndexNICM          = 11;
		int IndexIRCM          = 12;
		int IndexPRCM          = 13;
		int IndexGRDAYS        = 14;
		int IndexCWAM          = 15;
		int IndexHWAH          = 16;
		
		int PYEARmin = firstYear;
		int PYEARmax = lastYear;
		int PYEARn = PYEARmax - PYEARmin + 1;
		int[] ListPYEAR = new int[PYEARn];
		for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
			ListPYEAR[yearIndex] = PYEARmin + yearIndex;
		}
		
		// ok, so we will have a bunch of cases which will end up as prefixes, e.g., cultivar_climate_technology_*
		// then we have a bunch of characteristics to aggregate for those (i.e., all the other columns); e.g., *_HWAH or *_NICM

		// set up the accumulators...
		String[] aggregateQuantityNames = {"NICM", "IRCM", "PRCM", "GRDAYS", "CWAM", "HWAH"};
		int nAggregateQuantities = aggregateQuantityNames.length; // originally 
		
		DescriptiveStatisticsUtility  soilShareCounter = new DescriptiveStatisticsUtility(true);
		DescriptiveStatisticsUtility[]  overallAverages = new DescriptiveStatisticsUtility[nAggregateQuantities];
		DescriptiveStatisticsUtility[][] yearlyAverages = new DescriptiveStatisticsUtility[nAggregateQuantities][PYEARn];
		
		for (int aggIndex = 0; aggIndex < nAggregateQuantities; aggIndex++) {
			overallAverages[aggIndex] = new DescriptiveStatisticsUtility(false);
			
			for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
				yearlyAverages[aggIndex][yearIndex] = new DescriptiveStatisticsUtility(false);
			}
		}		
		
		// grab the cell id map as a matrix
		MultiFormatMatrix cellID = MatrixOperations.read2DMFMfromTextForceFormat(cellIDNumberFile + "_data",1); // Beware the MAGIC NUMBER!!! in memory...

		// grab the cell id geog as a string array 
		String[] geogStringArrayWholeLines = FunTricks.readTextFileToArray(cellIDNumberFile + "_geog.txt");
		
		
		// open up writers for the output files
		File outDataFileObject = new File(outputBaseName  + "_data.txt");
		PrintWriter outData = new PrintWriter(outDataFileObject);

		File outGeogFileObject = new File(outputBaseName  + "_geog.txt");
		PrintWriter outGeog = new PrintWriter(outGeogFileObject);

		File outHeaderFileObject = new File(outputBaseName  + "_header.txt");
		PrintWriter outHeader = new PrintWriter(outHeaderFileObject);

		File outColsFileObject = new File(outputBaseName  + ".cols.txt");
		PrintWriter outCols = new PrintWriter(outColsFileObject);

		// read in the raw data
		String[] dataStringArrayWholeLines = FunTricks.readTextFileToArray(dataFile);
		int nDataFileLines = dataStringArrayWholeLines.length;
		
		// let's parse everything straight up
		int[] unitID  = new int[nDataFileLines];
		int[] CELL30M = new int[nDataFileLines];
		int[] SoilID  = new int[nDataFileLines];
		int[] RepID   = new int[nDataFileLines];
		int[] PYEAR   = new int[nDataFileLines];
		int[] NICM    = new int[nDataFileLines];
		int[] IRCM    = new int[nDataFileLines];
		int[] PRCM    = new int[nDataFileLines];
		int[] GRDAYS  = new int[nDataFileLines];
		int[] CWAM    = new int[nDataFileLines];
		int[] HWAH    = new int[nDataFileLines];
		double[] SoilShare     = new double[nDataFileLines];
		String[] SoilProfile   = new String[nDataFileLines];
		String[] CropCode      = new String[nDataFileLines];
		String[] CultivarCode  = new String[nDataFileLines];
		String[] ClimateCode   = new String[nDataFileLines];
		String[] TechnologyKey = new String[nDataFileLines];

//		int originalLineIndex = -5;
		String[] thisLineSplit = null;
		System.out.println("       Starting to parse raw data");
		// first we will look at the very first line. then we will do the rest
		// and make sure that they all match for the same case....
		for (int lineIndex = 0; lineIndex < nDataFileLines; lineIndex++) {
			thisLineSplit = dataStringArrayWholeLines[lineIndex].split(delimiter);
			
			unitID[lineIndex]    = Integer.parseInt(thisLineSplit[IndexUnitID]);
			CELL30M[lineIndex]   = Integer.parseInt(thisLineSplit[IndexCELL30M]);
			SoilID[lineIndex]    = Integer.parseInt(thisLineSplit[IndexSoilID]);
			RepID[lineIndex]     = Integer.parseInt(thisLineSplit[IndexRepID]);
			PYEAR[lineIndex]     = Integer.parseInt(thisLineSplit[IndexPYEAR]);
			NICM[lineIndex]      = Integer.parseInt(thisLineSplit[IndexNICM]);
			IRCM[lineIndex]      = Integer.parseInt(thisLineSplit[IndexIRCM]);
			PRCM[lineIndex]      = Integer.parseInt(thisLineSplit[IndexPRCM]);
			GRDAYS[lineIndex]    = Integer.parseInt(thisLineSplit[IndexGRDAYS]);
			CWAM[lineIndex]      = Integer.parseInt(thisLineSplit[IndexCWAM]);
			HWAH[lineIndex]      = Integer.parseInt(thisLineSplit[IndexHWAH]);
			SoilShare[lineIndex] = Double.parseDouble(thisLineSplit[IndexSoilShare]);

			SoilProfile[lineIndex]   = thisLineSplit[IndexSoilProfile];
			CropCode[lineIndex]      = thisLineSplit[IndexCropCode];
			CultivarCode[lineIndex]  = thisLineSplit[IndexCultivarCode];
			ClimateCode[lineIndex]   = thisLineSplit[IndexClimateCode];
			TechnologyKey[lineIndex] = thisLineSplit[IndexTechnologyKey];
			
			// check to make sure they are all the same
			if (lineIndex > 0) {
				if (SoilID[lineIndex] != SoilID[0]) {
					System.out.println("SoilID[" + lineIndex + "] != SoilID[0]; " + SoilID[lineIndex] + " != " + SoilID[0]);
					throw new Exception();
				} else if (!CropCode[lineIndex].equals(CropCode[0])) {
					System.out.println("CropCode[" + lineIndex + "] != CropCode[0]; " + CropCode[lineIndex] + " != " + CropCode[0]);
					throw new Exception();
				} else if (!CultivarCode[lineIndex].equals(CultivarCode[0])) {
					System.out.println("CultivarCode[" + lineIndex + "] != CultivarCode[0]; " + CultivarCode[lineIndex] + " != " + CultivarCode[0]);
					throw new Exception();
				} else if (!ClimateCode[lineIndex].equals(ClimateCode[0])) {
					System.out.println("ClimateCode[" + lineIndex + "] != ClimateCode[0]; " + ClimateCode[lineIndex] + " != " + ClimateCode[0]);
					throw new Exception();
				} else if (!TechnologyKey[lineIndex].equals(TechnologyKey[0])) {
					System.out.println("TechnologyKey[" + lineIndex + "] != TechnologyKey[0]; " + TechnologyKey[lineIndex] + " != " + TechnologyKey[0]);
					throw new Exception();
				}
			}
		}
		System.out.println("       Done parsing raw data");
		
		// now we have to go through and interpret the data file in our magic way...
		long nPixels = cellID.getDimensions()[0];
		long nValidPixels = 0; // this really needs to be zero
		
		int thisPixelID = -5;
		int firstLineNumberInDataFileForThisPixelID = -6;
		int dataLineToPullFrom = -7;
		int nextLineIndex = -909;
		int computedYearIndex = -8;
		boolean haveDataForPixel = false;
		boolean doneWithThisBlockInDataFile = false;
		int withinBlockIndex = 0;
		String outputLine = null;
//		org.R2Useful.StringToUniqueCode soilCodeUtility = new org.R2Useful.StringToUniqueCode();
		// figure out how many soils we're talking about
//		soilCodeUtility = new org.R2Useful.StringToUniqueCode();
		for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
			thisPixelID = (int)cellID.getValue(pixelIndex,0);
			
			// now we have to see if this is in the real data...
			haveDataForPixel = false;
			for (int dataIndex = 0; dataIndex < nDataFileLines; dataIndex++) {
				if (CELL30M[dataIndex] == thisPixelID) {
					haveDataForPixel = true;
					firstLineNumberInDataFileForThisPixelID = dataIndex;
					break;
				}
			}
	
			// if we don't have any data for this one, just skip over it.
			if (!haveDataForPixel) {
				continue;
			}
			
//			System.out.println("[[[ we have data starting on line index " + firstLineNumberInDataFileForThisPixelID + " for pixel " + thisPixelID);
			
			//////// supposing we have data, we need to process the goodies ///////
			// reset the counters
			soilShareCounter.reset();
			for (int aggIndex = 0; aggIndex < nAggregateQuantities; aggIndex++) {
				overallAverages[aggIndex].reset();
				
				for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
					yearlyAverages[aggIndex][yearIndex].reset();
				} // yearIndex
			} // for aggIndex to reset counters

			// look through all the repetitions years and aggregate everything up
			// we will assume that everything is in the usual order: each repetition
			// is continguous and in order by years...
			
			// but now in version #1, we are going to brute force our way through
			// and interpret it instead of assuming that everything is hunky-dory
			doneWithThisBlockInDataFile = false;
			withinBlockIndex = 0;
			while (!doneWithThisBlockInDataFile) {
				dataLineToPullFrom = firstLineNumberInDataFileForThisPixelID + withinBlockIndex;
//				System.out.println(" {{{ " + withinBlockIndex + " -> " + dataLineToPullFrom + " }}}");
				
				computedYearIndex = PYEAR[dataLineToPullFrom] - firstYear;
				// now, sometimes we get a weird early planting problem, so we'll fix it, but note it in the screen dump
				if (computedYearIndex < 0) {
					System.out.print(dataLineToPullFrom + " <- line with possible early planting. resetting computedYearIndex from " +
							computedYearIndex + " to ");
					computedYearIndex = 0;
					System.out.println(computedYearIndex);
				} else if (computedYearIndex >= PYEARn) {
					System.out.print(dataLineToPullFrom + " <- line with possible LATE thing. resetting computedYearIndex from " +
							computedYearIndex + " to ");
					computedYearIndex = PYEARn - 1;
					System.out.println(computedYearIndex);
				}

				
				// based on which year and repetition this is, we shall proceed
				soilShareCounter.useDoubleValue(SoilShare[dataLineToPullFrom]);
				
				overallAverages[0].useLongValue(NICM  [dataLineToPullFrom]);
				overallAverages[1].useLongValue(IRCM  [dataLineToPullFrom]);
				overallAverages[2].useLongValue(PRCM  [dataLineToPullFrom]);
				overallAverages[3].useLongValue(GRDAYS[dataLineToPullFrom]);
				overallAverages[4].useLongValue(CWAM  [dataLineToPullFrom]);
				overallAverages[5].useLongValue(HWAH  [dataLineToPullFrom]);

				yearlyAverages[0][computedYearIndex].useLongValue(NICM  [dataLineToPullFrom]);
				yearlyAverages[1][computedYearIndex].useLongValue(IRCM  [dataLineToPullFrom]);
				yearlyAverages[2][computedYearIndex].useLongValue(PRCM  [dataLineToPullFrom]);
				yearlyAverages[3][computedYearIndex].useLongValue(GRDAYS[dataLineToPullFrom]);
				yearlyAverages[4][computedYearIndex].useLongValue(CWAM  [dataLineToPullFrom]);
				yearlyAverages[5][computedYearIndex].useLongValue(HWAH  [dataLineToPullFrom]);
			
				withinBlockIndex++;
				
				// check to see if the next one is worth doing
				nextLineIndex = dataLineToPullFrom + 1;
//				System.out.println(" preBAIL! nLI = " + nextLineIndex + "/" + nDataFileLines + 
//						"; next ID = " + CELL30M[nextLineIndex] + "/" + thisPixelID);
				if (nextLineIndex >= nDataFileLines || CELL30M[nextLineIndex] != thisPixelID) {
//					System.out.println(" BAIL! nLI = " + nextLineIndex + "/" + nDataFileLines + 
//							"; next ID = " + CELL30M[nextLineIndex] + "/" + thisPixelID);
					doneWithThisBlockInDataFile = true;
				}
			}
			
//			System.out.println("nValidPixels (index) = " + nValidPixels + " bailing prior to " + nextLineIndex);
						
			// now we need to write down this part of the output
			// to make life simple, we will copy over the bits of the geography file
			
			// first, let us note that we have found a valid pixel
			nValidPixels++;
			
			// copy the geography line
			outGeog.println(geogStringArrayWholeLines[pixelIndex]);

			// check to make sure the soil share is consistent within the pixel
			if (soilShareCounter.getMinAsDouble() != soilShareCounter.getMaxAsDouble()) {
				System.out.println("soilShareCounter.getMinAsDouble() != soilShareCounter.getMaxAsDouble()" + 
						"; line #" + dataLineToPullFrom + " " +
						soilShareCounter.getMinAsDouble() + " != " + soilShareCounter.getMaxAsDouble()		
				);
				throw new Exception();
			}
			// build and write out the data line
			outputLine = "";

			outputLine +=             soilShareCounter.getN();
			
			outputLine += delimiter + soilShareCounter.getMean();

			outputLine += delimiter + overallAverages[0].getMean();
			outputLine += delimiter + overallAverages[1].getMean();
			outputLine += delimiter + overallAverages[2].getMean();
			outputLine += delimiter + overallAverages[3].getMean();
			outputLine += delimiter + overallAverages[4].getMean();
			outputLine += delimiter + overallAverages[5].getMean();


			for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
				outputLine += delimiter + yearlyAverages[0][yearIndex].getMean();
				outputLine += delimiter + yearlyAverages[1][yearIndex].getMean();
				outputLine += delimiter + yearlyAverages[2][yearIndex].getMean();
				outputLine += delimiter + yearlyAverages[3][yearIndex].getMean();
				outputLine += delimiter + yearlyAverages[4][yearIndex].getMean();
				outputLine += delimiter + yearlyAverages[5][yearIndex].getMean();
			} // yearIndex

//			outputLine += delimiter + overallAverages[0].getN();
//			outputLine += delimiter + overallAverages[1].getN();
//			outputLine += delimiter + overallAverages[2].getN();
//			outputLine += delimiter + overallAverages[3].getN();
//			outputLine += delimiter + overallAverages[4].getN();
//			outputLine += delimiter + overallAverages[5].getN();
//
//			for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
//				outputLine += delimiter + yearlyAverages[0][yearIndex].getN();
//				outputLine += delimiter + yearlyAverages[1][yearIndex].getN();
//				outputLine += delimiter + yearlyAverages[2][yearIndex].getN();
//				outputLine += delimiter + yearlyAverages[3][yearIndex].getN();
//				outputLine += delimiter + yearlyAverages[4][yearIndex].getN();
//				outputLine += delimiter + yearlyAverages[5][yearIndex].getN();
//			} // yearIndex
			
			outData.println(outputLine);

			
		} // pixelIndex
		
		// copy the header file (ok, read it and then re-write it)
		outHeader.print(FunTricks.readTextFileToString(cellIDNumberFile + "_header.txt"));

		// build and write out the columns
		String columnsLine = "";

		columnsLine += "ngood";

		columnsLine += delimiter + "soil_share";

		columnsLine += delimiter + aggregateQuantityNames[0] + "_mean";
		columnsLine += delimiter + aggregateQuantityNames[1] + "_mean";
		columnsLine += delimiter + aggregateQuantityNames[2] + "_mean";
		columnsLine += delimiter + aggregateQuantityNames[3] + "_mean";
		columnsLine += delimiter + aggregateQuantityNames[4] + "_mean";
		columnsLine += delimiter + aggregateQuantityNames[5] + "_mean";

		for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
			columnsLine += delimiter + aggregateQuantityNames[0] + "_" + yearIndex + "_mean";
			columnsLine += delimiter + aggregateQuantityNames[1] + "_" + yearIndex + "_mean";
			columnsLine += delimiter + aggregateQuantityNames[2] + "_" + yearIndex + "_mean";
			columnsLine += delimiter + aggregateQuantityNames[3] + "_" + yearIndex + "_mean";
			columnsLine += delimiter + aggregateQuantityNames[4] + "_" + yearIndex + "_mean";
			columnsLine += delimiter + aggregateQuantityNames[5] + "_" + yearIndex + "_mean";
		} // yearIndex

//		columnsLine += delimiter + aggregateQuantityNames[0] + "_n";
//		columnsLine += delimiter + aggregateQuantityNames[1] + "_n";
//		columnsLine += delimiter + aggregateQuantityNames[2] + "_n";
//		columnsLine += delimiter + aggregateQuantityNames[3] + "_n";
//		columnsLine += delimiter + aggregateQuantityNames[4] + "_n";
//		columnsLine += delimiter + aggregateQuantityNames[5] + "_n";
//
//		for (int yearIndex = 0; yearIndex < PYEARn; yearIndex++) {
//			columnsLine += delimiter + aggregateQuantityNames[0] + "_" + yearIndex + "_n";
//			columnsLine += delimiter + aggregateQuantityNames[1] + "_" + yearIndex + "_n";
//			columnsLine += delimiter + aggregateQuantityNames[2] + "_" + yearIndex + "_n";
//			columnsLine += delimiter + aggregateQuantityNames[3] + "_" + yearIndex + "_n";
//			columnsLine += delimiter + aggregateQuantityNames[4] + "_" + yearIndex + "_n";
//			columnsLine += delimiter + aggregateQuantityNames[5] + "_" + yearIndex + "_n";
//		} // yearIndex
		
		outCols.println(columnsLine);

		
		
		// flush and close up shop
		outGeog.flush();
		outGeog.close();
		outData.flush();
		outData.close();
		outHeader.flush();
		outHeader.close();
		outCols.flush();
		outCols.close();
		
		// make the info files for data and geog
		// Beware the MAGIC NUMBER!!! number of columns
//		FunTricks.writeInfoFile(outputBaseName + "_data",
//				nValidPixels, 
//				6 * (1 + PYEARn) * 2 + 1 + 1, 
//				delimiter);

		FunTricks.writeInfoFile(outputBaseName + "_data",
				nValidPixels, 
				6 * (1 + PYEARn) * 1 + 1 + 1, 
				delimiter);

		FunTricks.writeInfoFile(outputBaseName + "_geog",
				nValidPixels, 
				geogStringArrayWholeLines[0].split(delimiter).length,
				delimiter);
		
		
		
		
		System.out.println("       -- " + timerThing.sinceStartMessage("all done!") + "--");

	} // main

}