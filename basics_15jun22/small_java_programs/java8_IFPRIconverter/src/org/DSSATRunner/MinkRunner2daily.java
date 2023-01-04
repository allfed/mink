package org.DSSATRunner;

import org.R2Useful.*;

public class MinkRunner2daily {

	
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

	  bigTimer.tic();

	  Mink2daily runnerObject = new Mink2daily();

	  runnerObject.setInitFile(initFileName);

	  runnerObject.readInitFile();

	  runnerObject.doSimulationsOnExistingWeather();
				
		System.out.println(bigTimer.tocMillis() + " ms for RUNNER...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

