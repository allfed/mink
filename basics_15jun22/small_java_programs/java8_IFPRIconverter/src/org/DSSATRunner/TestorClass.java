package org.DSSATRunner;

import org.R2Useful.*;

public class TestorClass {

	private static MultiFormatMatrix manualIncrementer(MultiFormatMatrix x) throws Exception {
		x.setValue(0,0, x.getValue(0,0) + 1.0);
		return x;
	}
	
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

//		System.out.println("5%3 = " + (5%3));
		
		HistoStatisticsUtility histoStat = new HistoStatisticsUtility(2);
		
		histoStat.useDoublePair(1, new double[]{2,7});
		histoStat.useDoublePair(4, new double[]{4,7});
		histoStat.useDoublePair(2, new double[]{6,8});
		histoStat.useDoublePair(4, new double[]{8,8});
		histoStat.useDoublePair(1, new double[]{10,500});
		histoStat.useDoublePair(17, new double[]{2,500});
		histoStat.useDoublePair(21, new double[]{4,500});
		histoStat.useDoublePair(2, new double[]{4,2});
		histoStat.useDoublePair(4, new double[]{18,-5});
		histoStat.useDoublePair(1, new double[]{120,-9});
		System.out.println(histoStat.printDataset());
		
		System.out.println(histoStat.getAllPretty());
		
//		histoStat.sortDatasetWithCumulativeFraction();
		
		
		
		
		double[][] sortedData = histoStat.sortDatasetWithCumulativeFraction();
		String asString = "";

		for (int rowIndex = 0; rowIndex < sortedData.length; rowIndex++) {
			asString += rowIndex + "\t"; 
			for (int colIndex = 0; colIndex < sortedData[0].length; colIndex++) {
				asString += sortedData[rowIndex][colIndex] + ","; 
			}
			asString += "\n";
		}
		
		System.out.println(asString);
		
		
		
		double[][] funnyThing = histoStat.getGoofyMediansWithinQuantileThing(4);
		
		asString = "";
		
//		for (int rawIndex = 0; rawIndex < funnyThing.length; rawIndex++) {
//			asString += rawIndex + "\t" + funnyThing[rawIndex][0] + "," + funnyThing[rawIndex][1] + "\n";
//		}
		for (int rowIndex = 0; rowIndex < funnyThing.length; rowIndex++) {
			asString += rowIndex + "\t"; 
			for (int colIndex = 0; colIndex < funnyThing[0].length; colIndex++) {
				asString += funnyThing[rowIndex][colIndex] + ","; 
			}
			asString += "\n";
		}
	
		System.out.println(asString);
		
		
		
		
		
		
		
		
		
		
		
		
		

		if (false) {

			NitrogenOnlyFertilizerScheme nitrogenFertilizerScheme = null;
			nitrogenFertilizerScheme = new FSWinterWheat();

			int anthesisDays = 80;
			int maturityDays = 100;
			double nitrogenLevel = 50;

			nitrogenFertilizerScheme.initialize();
			String fertilizerBlock = nitrogenFertilizerScheme.buildNitrogenOnlyBlock(
					anthesisDays, maturityDays, nitrogenLevel
			);

			System.out.println("-----------");
			System.out.print(fertilizerBlock);
			System.out.println("-----------");
		}







			boolean doPuddling = true;
			boolean useWaterContent = false;
			double organicCarbonToNitrogenConversionFactor = 0.1;
			double annualAverageTopSoilTemperature = 18;
			double startingMonthTopSoilTemperature = 24;

			double totalInitialNitrogenKgPerHa = 10;
			double depthForNitrogen = 45.00;
			double rootWeight = 5;
			double surfaceResidueWeight = 1000;
			double residueNitrogenPercent = 1;
			double incorporationRate = 100;
			double incorporationDepth = 15;

			double[] standardDepths  =         {   20,   40,   60,  1000000000};
			double[] clayStableFractionAbove = { 0.80, 0.96, 0.98,  0.98};
			double[] loamStableFractionAbove = { 0.80, 0.98, 0.98,  0.98};
			double[] sandStableFractionAbove = { 0.93, 0.98, 0.98,  0.98};

		
		if (false) {
		String funnyString = " 1001  16.1  14.7  -8.4   0.0  32.2 314.9";
		String[] asArray = funnyString.trim().split(" ");
		System.out.println("funnyString = [" + funnyString + "]");
		for (int index = 0; index < asArray.length; index++) {
			System.out.println("[" + index + "]" + asArray[index]);
		}
		
		// terniary operator test
		int original = 1;
		int newvalue = -5;
		System.out.println((original < 0)?5:6);
		
//			String soilFileName = commandLineOptions[0];

//			SoilProfile testProfileObject = new SoilProfile("D:/rdrobert/one_off_dumping/ZZ.SOL");

//			String profileToUse = "HC_GEN0022";
//			String profileToUse = "HC_GEN0013";

		SoilProfile testProfileObject = new SoilProfile("D:/rdrobert/global_futures/first_baseline_varieties/now_with_big_inits/from_ulrich_possibly_bad_CI.SOL");
			String profileToUse = "CI_ACZR048";
//			int profileIndexToUse = testProfileObject.findProfileIndex(profileToUse);

//			String dumpage = testProfileObject.dumpSingleProfile(profileToUse);
//			System.out.println(dumpage);

			System.out.println(testProfileObject.dumpSingleProfile(profileToUse));

			System.out.println("--------  now oryza ---------------");
			

			double waterFraction = 0.25;
//			float[] thicknesses = testProfileObject.getLayerThicknessCM()[testProfileObject.findProfileIndex(profileToUse)];
//			
//			for (int index = 0; index < thicknesses.length; index++) {
//				System.out.println("[" + index + "] = " + thicknesses[index]);
//			}
			
			
			String anewInitializationString = 
				testProfileObject.makeInitializationAndSoilAnalysisBlock(
						profileToUse,
						waterFraction,
						"iiiiS",
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


			System.out.println(anewInitializationString);
			System.out.println("-------- old above / apsim below ----------");

//			surfaceResidueWeight,
//		organicCarbonToNitrogenConversionFactor,
//			incorporationRate, incorporationDepth, 
//			 clayStableFractionAbove, loamStableFractionAbove, sandStableFractionAbove, standardDepths
//			System.out.println(testProfileObject.convertProfileToORYZA(
//					profileToUse, doPuddling, useWaterContent,  annualAverageTopSoilTemperature, startingMonthTopSoilTemperature,
//					waterFraction, totalInitialNitrogenKgPerHa, depthForNitrogen, rootWeight, 
//					residueNitrogenPercent, "\n")
//					);

//			incorporationRate,
//			incorporationDepth,

			System.out.println(testProfileObject.convertProfileToAPSIM(
					"wheat",
					profileToUse,
					waterFraction,
					totalInitialNitrogenKgPerHa,
					depthForNitrogen,
					rootWeight,
					surfaceResidueWeight,
					residueNitrogenPercent,
					standardDepths,
					clayStableFractionAbove,
					loamStableFractionAbove,
					sandStableFractionAbove,
					"\n"
					)
					);

		}
			
			
			if (false) {
			
				SoilProfile testProfileObject = new SoilProfile("D:/rdrobert/global_futures/first_baseline_varieties/now_with_big_inits/from_ulrich_possibly_bad_CI.SOL");
			System.out.println("--------OLD---------------");

			String profileToUse = "";
			String initializationString = 
				testProfileObject.makeInitializationBlockFractionBetweenBounds(
						profileToUse,
						0.2,
						"iiiiS",
						0.1);

//			System.out.println(initializationString);

			
			System.out.println("--------NEW---------------");
/*
			double totalInitialNitrogenKgPerHa = 10;
			double depthForNitrogen = 40;
			double rootWeight = 500;
			double surfaceResidueWeight = 250;
			double residueNitrogenPercent = 1;
			double incorporationRate = 50;
			double incorporationDepth = 5;

			double[] standardDepths  =         {   20,   40,   60,  1000000000};
			double[] clayStableFractionAbove = { 0.80, 0.96, 0.98,  0.98};
			double[] loamStableFractionAbove = { 0.80, 0.98, 0.98,  0.98};
			double[] sandStableFractionAbove = { 0.93, 0.98, 0.98,  0.98};

*/
			
			String newInitializationString = 
				testProfileObject.makeInitializationAndSoilAnalysisBlock(
						profileToUse,
						0.2,
						"iiiiS",
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


			System.out.println(newInitializationString);
			System.out.println("--------DUN---------------");



		}

		
		if (false) {

			MultiFormatMatrix j = MatrixOperations.identityMFM(1, 1);

			MultiFormatMatrix k = MatrixOperations.generateUniformRandom(1, 1, 1, 1);

			System.out.println("before: j = " + j + " ; k = " + k);

			MatrixOperations.print2dMFM(j, "before: j");
			MatrixOperations.print2dMFM(k, "before: k");


			k = manualIncrementer(j);

			System.out.println("j = " + j + " ; k = " + k);

			MatrixOperations.print2dMFM(j, "j");
			MatrixOperations.print2dMFM(k, "k");

		}
		
		if (false) {
		// generate a matrix to sort
		MultiFormatFloat aRandomMatrix = MatrixOperations.MFMtoMFF(MatrixOperations.generateUniformRandom(8, 2, 999, 1));
		
		// write out a copy
		MatrixOperations.write2DMFFtoText(aRandomMatrix, "d:\\rdrobert\\one_off_dumping\\ZZZ_random", "\t");
		
		// now sort by the first column and write it out
		SortComb11MFF sortingObject = new SortComb11MFF(aRandomMatrix,0);
		
		sortingObject.doSorting();
		
		MultiFormatFloat sortedColumn = sortingObject.getSortedColumnOnly();
		
		int[] sortedIndices = sortingObject.getSortedIndexList();
		
		MultiFormatFloat allSorted = sortingObject.getFullySortedMatrix();

		System.out.println("--- just the sorted column ---");
		MatrixOperations.print2dMFF(sortedColumn);

		System.out.println("--- original row indices ---");
		for (int rowIndex = 0; rowIndex < sortedIndices.length; rowIndex++) {
			System.out.println("[" + rowIndex + "] = " + sortedIndices[rowIndex]);
		}

		// write out a copy
		MatrixOperations.write2DMFFtoText(allSorted, "d:\\rdrobert\\one_off_dumping\\ZZZ_sorted", "\t");
		
		
		}		
		
		
		
		
		
		
		
		
		if (false) {
			
		double valueA = 1.12;
		double valueB = 13896.00;
		
		System.out.println("A = " + valueA + " -> " + FunTricks.fitInNCharacters(valueA, 10));
		System.out.println("B = " + valueB + " -> " + FunTricks.fitInNCharacters(valueB, 10));
		
		}
		
		if (false) {
		String AsoilFileName = commandLineOptions[0];

		String profileToUse = "";
		SoilProfile testProfileObject = new SoilProfile("D:/rdrobert/one_off_dumping/ZZ.SOL");

		String dumpage = testProfileObject.dumpAllProfilesAsString();
			System.out.println(dumpage);

			String AprofileToUse = "HC_GEN0013";
			int AprofileIndexToUse = testProfileObject.findProfileIndex(profileToUse);
			
			System.out.println(testProfileObject.dumpSingleProfile(profileToUse));
			
			String initializationString = 
				testProfileObject.makeInitializationBlockFractionBetweenBounds(
						profileToUse,
						0.0,
						"iiiiS",
						0.1);
			
			System.out.println(initializationString);
			
		}
			
			
			
	} // main

}

