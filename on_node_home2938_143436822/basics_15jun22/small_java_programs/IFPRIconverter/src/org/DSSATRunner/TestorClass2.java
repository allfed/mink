package org.DSSATRunner;

import org.R2Useful.*;
import java.io.*;

public class TestorClass2 {

	public static void main(String commandLineOptions[]) throws Exception {

		TimerUtility bigTimer = new TimerUtility();
		
		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////
		
		System.out.print("command line arguments: \n");
		bigTimer.tic();
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i] + " " + bigTimer.tocNanos() + " ns\n");
			bigTimer.tic();
		}
		System.out.println();


		String[] zeroLength = new String[0];
		String[] nullArray = null;
		
		System.out.println("zeroLength.length = [" + zeroLength.length + "]");
//		System.out.println("nullArray.length = [" + nullArray.length + "]");
		
		String uglyLine = "       20      1  1  1  0 SB CRGRO045 RI                        GRID     RRRR.WTH HN_GEN0010 2024155 2024244 2024286     -99     -99 2024303    56 *******       0       0       0     0  0.0000     0     0.0 0.000   0.0     0     0  1001   253     1   252   126   523     0     1     0     1     0    26    34     1     0   -99   -99   -99   -99   -99   -99   -99   -99     0  31890  31890      0      0  227848  227848***************************    -99.0     0.00     0.00     0.00   -99.00    -99.0    -99.0    -99.0    -99.0    59  12.4   4.5  15.9 12.43  369.0  230.4  140.6";
		
		String[] splitLineHere = FunTricks.parseRepeatedDelimiterToArray(uglyLine, " ");
		
		for (int splitIndex = 0; splitIndex < splitLineHere.length; splitIndex++) {
			System.out.println("[" + splitIndex + "] = [" + splitLineHere[splitIndex] + "]");
		}
		
		String aLongString = "abc";
		String regExTry = "[abc]";
		System.out.println("[" + aLongString + "] tested with [" + regExTry + "] gives us [" + aLongString.matches(regExTry) + "]");
		
		if (5 == 6) {
		
		double funnyValue = 12345678901234567890.012345; // -9.999999906077472;
//		double funnyValue = -9.999999996077472; // -9.999999906077472;
//		double funnyValue = -9999.999999996077472; // -9.999999906077472;
//		double funnyValue = 99999.999999996077472; // -9.999999906077472;
		double stepsize = -1E-6;
		String fiveCharacterVersion = null;

		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");

		funnyValue = -2.456;
		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");
		
		funnyValue = 12.3456;
		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersOLD(   funnyValue, 5) + "]");
		System.out.println("-->[" + funnyValue + "] -> [" + FunTricks.fitInNCharacters(funnyValue, 5) + "] (new)");
		
//		while (true) {
//
//			fiveCharacterVersion = FunTricks.fitInNCharacters(funnyValue, 5);
////			if (fiveCharacterVersion.length() != 5) {
//				if (true) {
//				System.out.println("[" + funnyValue + "] -> [" + fiveCharacterVersion + "]");
//				System.out.println("[" + funnyValue + "] -> [" + FunTricks.fitInNCharactersNew(funnyValue, 5) + "] (new)");
//			}
//			funnyValue += stepsize;
//		}
		
		
		}
		
		
		if (false) {
			NitrogenOnlyFertilizerScheme fertScheme = new org.DSSATRunner.FSRice();
			fertScheme.initialize();
			System.out.println(fertScheme.buildNitrogenOnlyBlock(100, 1, 4001));

			double numberthing = Double.POSITIVE_INFINITY;
			System.out.println("the original number is [" + numberthing + "]");
			
			int nDecimals = 3;
			System.out.println("# of decimals = " + nDecimals + "; value = [" + FunTricks.onlySomeDecimalPlaces(numberthing, nDecimals) + "]");

			File directoryTry = new File("d:\\funkytest\\");
			
			File[] inputFiles = directoryTry.listFiles();
			
			for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {
				System.out.println("[" + fileIndex + "] " + inputFiles[fileIndex].getAbsolutePath());
			}

		DrawFromCDF drawObject = new DrawFromCDF("D:\\rdrobert\\zambia_household_geographic\\text\\first_toy_elevation_quantiles.txt",":");
		
		drawObject.setRandomSeed(59);
		
//		drawObject.dumpcheck();
		
		int nRepetitions = 1000;
		for (int repetitionIndex = 0; repetitionIndex < nRepetitions ; repetitionIndex++){
			System.out.println(drawObject.provideSingleDraw());
		}
		
		
		
		
		String lineWithExtraSpaces = "zero one  two   three    four";
		String[] splitLine = lineWithExtraSpaces.split(" ");
		
		System.out.println("original = [" + lineWithExtraSpaces + "]");
		for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
			System.out.println("[" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
		}
		
		// now count up the number of non-empties
		int nValid = 0;
		for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
			if (!splitLine[splitIndex].isEmpty()) {
				System.out.println("FULL: [" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
				nValid++;
			}
		}
		String[] onlyValid = new String[nValid];
		int storageIndex = 0;
		for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
			if (!splitLine[splitIndex].isEmpty()) {
				onlyValid[storageIndex] = splitLine[splitIndex];
				storageIndex++;
			}
		}

		for (int splitIndex = 0; splitIndex < nValid; splitIndex++) {
			System.out.println("[" + splitIndex + "] = [" + onlyValid[splitIndex] + "]");
		}

	}
		if (false) {
		SystemCallWithTimeout scwt = new SystemCallWithTimeout();
		
		String[] commandToUse = {"c:\\windows\\system32\\notepad.exe"};
		java.io.File workingDirectoryToUse = new java.io.File("C:\\");
//		java.io.File workingDirectoryToUse = null;
		int sleepTimeMillisToUse = 3000;
		int testInterval = 50;
		
		scwt.setup(commandToUse, workingDirectoryToUse, sleepTimeMillisToUse, testInterval);
		
		System.out.println("before run: completion status = " + scwt.finishedCleanly());

		scwt.run();
		
		System.out.println("after start: completion status = " + scwt.finishedCleanly());

		System.out.println("after waitfor?: completion status = " + scwt.finishedCleanly());

//		System.out.println(DSSATHelperMethods.yearDayToYYDDD(0,1));		
		}
		


			
			
			
	} // main

}

