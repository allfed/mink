package org.DSSATRunner;

import org.R2Useful.*;

public class TestorHappy {

	
	public static void main(String commandLineOptions[]) throws Exception {

		TimerUtility bigTimer = new TimerUtility();
		
		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		
		System.out.print("command line arguments: \n");
		bigTimer.tic();
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i] + "; " + bigTimer.tocNanos() + " ns\n");
			bigTimer.tic();
		}
		System.out.println();
		
		double minN = 0;
		double maxN = 300;
		int nSteps = 11;
		
		double stepN = (maxN - minN) / (nSteps - 1);
		double nToUse = -1;
		
		String blockToWrite = null;
		
		NitrogenOnlyFertilizerScheme testScheme = new org.DSSATRunner.FSWheat();
		testScheme.initialize();
		
		for (int stepIndex = 0; stepIndex < nSteps; stepIndex++) {
			nToUse = minN + stepIndex * stepN;
			blockToWrite = testScheme.buildNitrogenOnlyBlock(60, 140, nToUse);
			
			System.out.println(" --- N = " + nToUse + " ---");
			System.out.print(blockToWrite);
			System.out.println();
		}

		
//		int startDate = 2000001;
//		int   endDate = 2001071;
//		
//		
//		System.out.println(endDate + " - " + startDate + " = " + DSSATHelperMethods.yyyyDDDdifferencerIgnoreLeap(startDate, endDate));
		
	} // main

}

