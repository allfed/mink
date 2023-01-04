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
		boolean useNewRunner = false;
		if (commandLineOptions.length > 1) {
			useNewRunner = true;
		}
//		int nHappyRepetitions = Integer.parseInt(commandLineOptions[1]);
//		int outputTimeStepDays = Integer.parseInt(commandLineOptions[2]);

		bigTimer.tic();

		if (useNewRunner) {
			// this is for the standalone weather generator version...
			System.out.println(" --- using oryza_v3 ---");
			ORYZARunnerThree runnerObjectThree = new ORYZARunnerThree();
			runnerObjectThree.readInitializationFile(initFileName);

			runnerObjectThree.runIt();
		} else {
			// this is for the standalone weather generator version...
			System.out.println(" --- using STANDALONE weather generator ---");
			ORYZARunnerTwo runnerObjectTwo = new ORYZARunnerTwo();
			runnerObjectTwo.readInitializationFile(initFileName);

			runnerObjectTwo.runIt();
		} 
		
		if (false) {
			// this is the very original runner
			ORYZARunnerZero runnerObject = new ORYZARunnerZero();
			runnerObject.readInitializationFile(initFileName);

			runnerObject.runIt();

			
			System.out.println(" --- using DSSAT as weather generator ---");
			ORYZARunnerOne runnerObjectOne = new ORYZARunnerOne();
			runnerObjectOne.readInitializationFile(initFileName);

			runnerObjectOne.runIt();
		}



//		System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");
		
		System.out.println(bigTimer.sinceStartMessage());
		
	} // main

}

