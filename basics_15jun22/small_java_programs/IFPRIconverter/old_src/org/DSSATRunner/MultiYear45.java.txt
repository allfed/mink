package org.DSSATRunner;

import org.R2Useful.*;

public class MultiYear45 {

	
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
		
		String initFileName = commandLineOptions[0];

		DSSATRunnerIrrigation45 runnerObject = new DSSATRunnerIrrigation45();
		bigTimer.tic();
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR to do nothing...");

	  bigTimer.tic();
	  
	  runnerObject.setInitFile(initFileName);
	  
	  runnerObject.readInitFile(initFileName);
	  
		runnerObject.doSimulationsMultipleYears();
		
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

