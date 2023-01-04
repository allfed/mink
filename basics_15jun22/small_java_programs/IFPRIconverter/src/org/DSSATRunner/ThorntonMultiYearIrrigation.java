package org.DSSATRunner;

import org.R2Useful.*;

public class ThorntonMultiYearIrrigation {

	
	public static void main(String commandLineOptions[]) throws Exception {

		TimerUtility bigTimer = new TimerUtility();
		
		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		
		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i]);
		}
		
		System.out.println();
		
		String initFileName = commandLineOptions[0];

		DSSATRunnerIrrigation runnerObject = new DSSATRunnerIrrigation();

	  bigTimer.tic();
	  
	  runnerObject.setInitFile(initFileName);
	  
	  runnerObject.readInitFile(initFileName);
	  
	  runnerObject.doSimulationsMultipleYearsAllClimateSupplied();
		
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

