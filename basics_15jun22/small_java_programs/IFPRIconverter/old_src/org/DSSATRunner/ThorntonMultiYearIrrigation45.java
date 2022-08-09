package org.DSSATRunner;

import org.R2Useful.*;

public class ThorntonMultiYearIrrigation45 {

	
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
		String useOldBetaVersion = "";
		if (commandLineOptions.length > 1) {
			useOldBetaVersion = commandLineOptions[1];
		}

	  bigTimer.tic();

	  if (useOldBetaVersion.length() > 0) {
	  	DSSATRunnerIrrigation45OldBeta runnerObject = new DSSATRunnerIrrigation45OldBeta();
		  
		  runnerObject.setInitFile(initFileName);
		  
		  runnerObject.readInitFile(initFileName);
		  
		  runnerObject.doSimulationsMultipleYearsAllClimateSupplied();
			
		} else {
			DSSATRunnerIrrigation45 runnerObject = new DSSATRunnerIrrigation45();

		  
		  runnerObject.setInitFile(initFileName);
		  
		  runnerObject.readInitFile(initFileName);
		  
		  runnerObject.doSimulationsMultipleYearsAllClimateSupplied();
	
		}
				
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

