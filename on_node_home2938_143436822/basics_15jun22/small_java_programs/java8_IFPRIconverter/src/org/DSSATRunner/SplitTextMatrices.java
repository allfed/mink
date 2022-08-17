package org.DSSATRunner;

import org.R2Useful.*;

public class SplitTextMatrices {

	
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
		
		if (commandLineOptions.length != 3) {
			System.out.println("Usage: SplitTextMatrices input_matrix_basename output_prefix nChunks");
			System.out.println();
			System.out.println("This splits up a matrix into even chunks.");
		}
		
		String originalBaseName      = commandLineOptions[0];
		String outputPrefix        = commandLineOptions[1];
		int nChunks = Integer.parseInt(commandLineOptions[2]);
		
		FunTricks.splitMatrixTextFileByLinesInfixBeforeUnderscore(originalBaseName, outputPrefix, nChunks);
		
		
		
	} // main

}

