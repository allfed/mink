package org.DSSATRunner;

import org.R2Useful.*;

public class TestorDeleteme {

	
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

		int plantingDate = 99365;

		IrrigationScheme testScheme = new IrriSRice();
		
		testScheme.initialize(plantingDate,3);
		
		System.out.print(testScheme.buildIrrigationBlock());
		
		DSSATRunnerIrrigation runnerObject = new DSSATRunnerIrrigation();

		String initFileName = "D:\\temp\\runner_init.txt";
		
	  runnerObject.setInitFile(initFileName);
	  
	  runnerObject.readInitFile(initFileName);

	  String templateXFile = FunTricks.readTextFileToString("D:\\temp\\indicaII.RIX");
	  
	  System.out.println("--------");
	  System.out.print(runnerObject.happyPlantX(templateXFile));
	  System.out.println("--------");
		
	} // main

}

