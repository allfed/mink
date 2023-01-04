package org.parallelBFGS;

import org.R2Useful.*;


public class ZambiaBuildRandomXs {

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

		
		String filenameOfCDFs = commandLineOptions[0];
		String delimiter = commandLineOptions[1];
		long randomSeed = Long.parseLong(commandLineOptions[2]);
		String filenameOfRealData = commandLineOptions[3];
		
		
		// set up the random generator
		DrawFromCDF drawObject = new DrawFromCDF(filenameOfCDFs,delimiter);
		drawObject.setRandomSeed(randomSeed);
		
		
		// we will start with an assumption that we have only a single
		// X that needs to be randomly generated...
		
		int nRepetitions = 1000;
		for (int repetitionIndex = 0; repetitionIndex < nRepetitions ; repetitionIndex++){
			System.out.println(drawObject.provideSingleDraw());
		}
		
		
		
		
		


			
			
			
	} // main

}

