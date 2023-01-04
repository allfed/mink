package org.DSSATRunner;

import org.R2Useful.*;

public class MinkRunner0 {

	
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
	  	System.out.println("[[[ planning to use the old beta runner ]]]");
	  	Mink0 runnerObject = new Mink0();
		  
		  runnerObject.setInitFile(initFileName);
		  
		  runnerObject.readInitFile(initFileName);
		  
//		  runnerObject.doSimulationsMultipleYearsAllClimateSupplied(true);
		  runnerObject.doSimulationsUniqueSeedsAllClimateSupplied(true);
			
		} else {
	  	System.out.println("((( planning to use the normal beta runner )))");
	  	Mink0 runnerObject = new Mink0();
		  
		  runnerObject.setInitFile(initFileName);
		  
		  runnerObject.readInitFile(initFileName);
		  
//		  runnerObject.doSimulationsMultipleYearsAllClimateSupplied(false);
		  runnerObject.doSimulationsUniqueSeedsAllClimateSupplied(false);
			
		}
				
		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

