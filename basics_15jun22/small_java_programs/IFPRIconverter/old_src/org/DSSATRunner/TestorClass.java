package org.DSSATRunner;

import org.R2Useful.*;

public class TestorClass {

	
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

		double valueA = 1.12;
		double valueB = 13896.00;
		
		System.out.println("A = " + valueA + " -> " + FunTricks.fitInNCharacters(valueA, 10));
		System.out.println("B = " + valueB + " -> " + FunTricks.fitInNCharacters(valueB, 10));
		
		if (false) {
		
		int firstPlantingDay = -1;
		int plantingWindowIndex = 0;
		int plantingWindowSpacing = 15;
		
		
		
		int spinUpTimeDays = 90;
		int fakePlantingYear = 50;
		int nDaysInYear = 365;
		int plantingWindowLengthDays = 20;
		
		int startingDayToPlantForThisWindow = firstPlantingDay + plantingWindowIndex * plantingWindowSpacing;
		
		System.out.println("startingDayToPlantForThisWindow = " + startingDayToPlantForThisWindow);
		int initializationDayForThisWindow = startingDayToPlantForThisWindow - spinUpTimeDays;
		int fakeInitializationYear = fakePlantingYear;

		// take care of the possibility that we will go before the beginning of this year
		while (initializationDayForThisWindow < 1) {
			System.out.println("top iDFTW = " + initializationDayForThisWindow + "; fIY = " + fakeInitializationYear);
			initializationDayForThisWindow += nDaysInYear;
			fakeInitializationYear--;
			System.out.println("bot iDFTW = " + initializationDayForThisWindow + "; fIY = " + fakeInitializationYear);
		}

		int endingDayToPlantForThisWindow = startingDayToPlantForThisWindow + plantingWindowLengthDays;
		int fakePlantingYearEnd = fakePlantingYear;

		// take care of the possibility that we will go beyond the end of this year
		while (endingDayToPlantForThisWindow > nDaysInYear) {
			System.out.println("top eDTPFTW = " + endingDayToPlantForThisWindow + "; fPYE = " + fakePlantingYearEnd);
			endingDayToPlantForThisWindow -= nDaysInYear;
			fakePlantingYearEnd++;
			System.out.println("bot eDTPFTW = " + endingDayToPlantForThisWindow + "; fPYE = " + fakePlantingYearEnd);
		}
		
		
		// format everything properly....
			String startingDayToPlantCode = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYear)
					+	DSSATHelperMethods.pad3CharactersZeros(startingDayToPlantForThisWindow)); // YYddd
			String endingDayToPlantCode   = (DSSATHelperMethods.pad2CharactersZeros(fakePlantingYearEnd) 
					+ DSSATHelperMethods.pad3CharactersZeros(endingDayToPlantForThisWindow)); // YYddd
			String initializationDayCode  = (DSSATHelperMethods.pad2CharactersZeros(fakeInitializationYear)
					+ DSSATHelperMethods.pad3CharactersZeros(initializationDayForThisWindow));   // YYddd
		
		
		System.out.println("startingDayToPlantCode " + startingDayToPlantCode + " ; endingDayToPlantCode " + endingDayToPlantCode +
				" ; initializationDayCode " + initializationDayCode);
		
		
		
		
	}		
		
		if (false) {
		String soilFileName = commandLineOptions[0];

		SoilProfile testProfileObject = new SoilProfile("D:/rdrobert/one_off_dumping/ZZ.SOL");

		String dumpage = testProfileObject.dumpAllProfilesAsString();
			System.out.println(dumpage);

			String profileToUse = "HC_GEN0013";
			int profileIndexToUse = testProfileObject.findProfileIndex(profileToUse);
			
			System.out.println(testProfileObject.dumpSingleProfile(profileToUse));
			
			String initializationString = 
				testProfileObject.makeInitializationBlockFractionBetweenBounds(
						profileToUse,
						0.0,
						"iiiiS",
						0.1);
			
			System.out.println(initializationString);
			
		}
			
			
			
		if (false) {
			
		String initFileName = commandLineOptions[0];

		DSSATRunner runnerObject = new DSSATRunner();
		bigTimer.tic();
//		runnerObject.generateDemoInitFile("D:\\temp\\stupid_init_demo.txt");
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR to do nothing...");

	  bigTimer.tic();
	  
//	  String initFileName = "D:\\temp\\stupid_init_demo.txt";
	  runnerObject.setInitFile(initFileName);
	  
	  runnerObject.readInitFile(initFileName);
	  
		runnerObject.doSimulationsUniqueRandomSeeds();
		
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		}
	} // main

}

