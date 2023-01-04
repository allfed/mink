package org.ORYZARunner;

import org.R2Useful.*;

public class ORYZAOuterRunner {

	
	public static void main(String commandLineOptions[]) throws Exception {

		TimerUtility bigTimer = new TimerUtility();
		
		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		
		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.println(i + " [" + commandLineOptions[i] + "]");
		}
		
		System.out.println();
		
		String initFileName = commandLineOptions[0];
//		int nHappyRepetitions = Integer.parseInt(commandLineOptions[1]);
//		int outputTimeStepDays = Integer.parseInt(commandLineOptions[2]);

		ORYZARunnerZero runnerObject = new ORYZARunnerZero();

		bigTimer.tic();

		runnerObject.readInitializationFile(initFileName);

		runnerObject.runIt();

		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

