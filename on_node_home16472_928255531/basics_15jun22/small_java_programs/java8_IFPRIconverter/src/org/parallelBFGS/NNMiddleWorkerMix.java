package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.PrintWriter;
import java.util.Date;

public class NNMiddleWorkerMix {

	private static void debugOut(String message, int sequenceNumber) {
		System.out.println("#" +  sequenceNumber + ": " + message + " [" + new Date() + "]");
	}


	public static void main(String commandLineOptions[]) throws Exception {
		
		long overallStart = System.currentTimeMillis();
		
		if (commandLineOptions.length != 4) {
			System.out.println("Usage: classname dataFileBase nThreadsThis sequenceNumber nextMachine");
		}
		
		String dataFile    =                  commandLineOptions[0];
		int nThreads       = Integer.parseInt(commandLineOptions[1]);
		int sequenceNumber = Integer.parseInt(commandLineOptions[2]);
		String nextMachine =                  commandLineOptions[3];
//		boolean doDebugStatements = false;
		boolean useOldSchool = false;
		if (commandLineOptions.length == 5) {
			useOldSchool = true;
		}

		///////////////////
		// magic numbers //
		///////////////////

		long magicSleepTime = 50;
		int magicBadConnectionRetries = 5;
		
		// start up all the receivers....
		SimpleReceiver oneTimeParametersListener  = new SimpleReceiver(NNHelperMethods.ONE_TIME_PARAMETERS_PORT,"one time params"+ sequenceNumber);
		SimpleReceiver weightsBiasesListener      = new SimpleReceiver(NNHelperMethods.WEIGHTS_BIASES_PORT,"WB"+ sequenceNumber);
		SimpleReceiver trainingNTotalListener     = new SimpleReceiver(NNHelperMethods.TRAINING_N_PORT,"tN"+ sequenceNumber);
		SimpleReceiver validationNTotalListener   = new SimpleReceiver(NNHelperMethods.VALIDATION_N_PORT,"vN"+ sequenceNumber);
		SimpleReceiver trainingNCorrectListener   = new SimpleReceiver(NNHelperMethods.TRAINING_N_CORRECT_PORT,"tNC"+ sequenceNumber);
		SimpleReceiver trainingErrorListener      = new SimpleReceiver(NNHelperMethods.TRAINING_ERROR_PORT,"tE"+ sequenceNumber);
		SimpleReceiver validationNCorrectListener = new SimpleReceiver(NNHelperMethods.VALIDATION_N_CORRECT_PORT,"vNC"+ sequenceNumber);
		SimpleReceiver validationErrorListener    = new SimpleReceiver(NNHelperMethods.VALIDATION_ERROR_PORT,"vC"+ sequenceNumber);
		SimpleReceiver gradientListener           = new SimpleReceiver(NNHelperMethods.GRADIENT_PORT,"gradient"+ sequenceNumber);
		SimpleReceiver continueLineSearchListener = new SimpleReceiver(NNHelperMethods.CONTINUE_SEARCH_PORT,"LSflag"+ sequenceNumber);
		SimpleReceiver stepParametersListener     = new SimpleReceiver(NNHelperMethods.LINE_SEARCH_WB_PORT,"LS W/B"+ sequenceNumber);
		SimpleReceiver stepErrorListener          = new SimpleReceiver(NNHelperMethods.LINE_SEARCH_ERROR_PORT,"LS e"+ sequenceNumber);
		SimpleReceiver continueEpochListener      = new SimpleReceiver(NNHelperMethods.CONTINUE_EPOCH_PORT,"epoch flag"+ sequenceNumber);
		SimpleReceiver doValidationListener       = new SimpleReceiver(NNHelperMethods.DO_VALIDATION_PORT,"epoch doV"+ sequenceNumber);

		oneTimeParametersListener.setVerbosity(false);
		weightsBiasesListener.setVerbosity(false);
		trainingNTotalListener.setVerbosity(false);
		validationNTotalListener.setVerbosity(false);
		trainingNCorrectListener.setVerbosity(false);
		trainingErrorListener.setVerbosity(false);
		validationNCorrectListener.setVerbosity(false);
		validationErrorListener.setVerbosity(false);
		gradientListener.setVerbosity(false);
		continueLineSearchListener.setVerbosity(false);
		stepParametersListener.setVerbosity(false);
		stepErrorListener.setVerbosity(false);
		continueEpochListener.setVerbosity(false);
		doValidationListener.setVerbosity(false);

		
		new Thread(oneTimeParametersListener).start();
		new Thread(weightsBiasesListener).start();
		new Thread(trainingNTotalListener).start();
		new Thread(validationNTotalListener).start();
		new Thread(trainingNCorrectListener).start();
		new Thread(trainingErrorListener).start();
		new Thread(validationNCorrectListener).start();
		new Thread(validationErrorListener).start();
		new Thread(gradientListener).start();
		new Thread(continueLineSearchListener).start();
		new Thread(stepParametersListener).start();
		new Thread(stepErrorListener).start();
		new Thread(continueEpochListener).start();
		new Thread(doValidationListener).start();

		// let's sleep for a moment to let things get going
		Thread.sleep(magicSleepTime);
		
		System.out.println("#" + sequenceNumber + " listeners set up...");
		
		// pull the magic numbers from the previous machine
		
				
		int               nHidden = ((Integer)oneTimeParametersListener.pullObject()).intValue();
		int      nOptionsMinusOne = ((Integer)oneTimeParametersListener.pullObject()).intValue();
		double fractionInTraining = (( Double)oneTimeParametersListener.pullObject()).doubleValue();
		long          aRandomSeed = ((   Long)oneTimeParametersListener.pullObject()).longValue();

		// try to be clever and clean up by closing the stream
		oneTimeParametersListener.stopListening();

		// pass them along
		SimpleSender oneTimeParametersSender = new SimpleSender(nextMachine, NNHelperMethods.ONE_TIME_PARAMETERS_PORT,
				magicSleepTime, magicBadConnectionRetries);
		oneTimeParametersSender.establishConnection();
		oneTimeParametersSender.sendObject(nHidden);
		oneTimeParametersSender.sendObject(nOptionsMinusOne);
		oneTimeParametersSender.sendObject(fractionInTraining);
		oneTimeParametersSender.sendObject(aRandomSeed);

		// now try to be clever and clean up by closing the stream
		oneTimeParametersSender.closeConnection();

		
		System.out.println("----- begin initialization values for #" + sequenceNumber + " -----");
		System.out.println("data file for #" + sequenceNumber + ": " + dataFile);
		System.out.println("#" + sequenceNumber + " nHidden = [" + nHidden + "]");
		System.out.println("#" + sequenceNumber + " nOptionsMinusOne = [" + nOptionsMinusOne + "]");
		System.out.println("#" + sequenceNumber + " FractionInTraining = [" + fractionInTraining + "]");
		System.out.println("#" + sequenceNumber + " aRandomSeed = [" + aRandomSeed + "]");
		System.out.println("------ end initialization values for #" + sequenceNumber + " ------");
		
		
		System.out.println();

		
		
		// load the data in

		
		MultiFormatFloat allDataMatrix = new MultiFormatFloat(dataFile + sequenceNumber);
		long nRowsTotal = allDataMatrix.getDimensions()[0];
		long nXs = allDataMatrix.getDimensions()[1] - 1;
		
		// now, we have to make blocks because we're gonna do multiple threads
		
		MultiFormatFloat[] allDataBlocks = new MultiFormatFloat[nThreads];
		long nRowsHere = 0;
		long firstRowForThread[] = new long[nThreads + 1];

		// assign which rows will go in which block. if we have more threads than rows,
		// we'll assign negative one so that hopefully the for loops we will be creating will skip over them
		if (nThreads < nRowsTotal) {
			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				firstRowForThread[threadIndex] = threadIndex * nRowsTotal / nThreads;
			}
			firstRowForThread[nThreads] = nRowsTotal; // the extra one so that we can use set the bounds based on thread and thread+1
		} else {
			for (int threadIndex = 0; threadIndex < nRowsTotal; threadIndex++) {
				firstRowForThread[threadIndex] = threadIndex;
			}
			for (int threadIndex = (int)nRowsTotal; threadIndex < nThreads; threadIndex++) {
				firstRowForThread[threadIndex] = -1;
			}
			firstRowForThread[nThreads] = -1; // the extra one so that we can use set the bounds based on thread and thread+1
		}

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			nRowsHere = firstRowForThread[threadIndex+1] - firstRowForThread[threadIndex];
			// check to make sure that we have non-negative length. if we have more threads than rows, eventually, we
			// will revert to the -1's for the first row marker. so we will end up with a (-1 - 5) kind of situation.
			// we want that one to go to zero.
			if (nRowsHere < 0) {
				nRowsHere = 0;
			}
			
			// copy the goodies over...
			allDataBlocks[threadIndex] = new MultiFormatFloat(1,new long[] {nRowsHere , nXs + 1});
			for (long rowIndex = 0; rowIndex < nRowsHere; rowIndex++) {
				for (long colIndex = 0; colIndex < nXs + 1; colIndex++) {
					allDataBlocks[threadIndex].setValue(rowIndex,colIndex,
							allDataMatrix.getValue(rowIndex + firstRowForThread[threadIndex],colIndex));
				}
			}
		}
	
		
		// split into training/validation as well as X and targets
		
		MultiFormatFloat[][] trainingValidationPairs = new MultiFormatFloat[nThreads][2];

		MultiFormatFloat[][] trainingXT   = new MultiFormatFloat[nThreads][2];
		MultiFormatFloat[][] validationXT = new MultiFormatFloat[nThreads][2];

		long[] nTraining = new long[nThreads];
		long[] nValidation = new long[nThreads];
		
		long nTrainingTotal = 0;   // this actually needs to be zero
		long nValidationTotal = 0; // this actually needs to be zero
		
		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

			trainingValidationPairs[threadIndex] = MatrixOperations.randomizeRows2dMff(allDataBlocks[threadIndex], aRandomSeed, fractionInTraining,1);
			
			nTraining[threadIndex]   = trainingValidationPairs[threadIndex][0].getDimensions()[0];
			nValidation[threadIndex] = trainingValidationPairs[threadIndex][1].getDimensions()[0];

			// figure out the total number of training and validation examples
			nTrainingTotal   += nTraining[threadIndex];
			nValidationTotal += nValidation[threadIndex];
			
			trainingXT[threadIndex]   = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][0]);
			validationXT[threadIndex] = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][1]);
		}
		
		
		
		
				
		// set up the training loop
		DeadDropObjectArray trainingResultsDrop   = null;
		GradientRunner[] trainingGradientRunnerArray = new GradientRunner[nThreads];

		DeadDropObjectArray validationResultsDrop = null;
		ErrorRunner[]    validationErrorRunnerArray  = new ErrorRunner[   nThreads];
		
		
		double[] trainingError      = new double[nThreads];
		double[] trainingNCorrect   = new double[nThreads];
		Object[] gradientThreadBundles = new Object[nThreads];
		Object[] validationErrorThreadBundles = new Object[nThreads];

		
		// get the parameters from the previous machine...
		MultiFormatMatrix[] weightsAndBiases    = (MultiFormatMatrix[])weightsBiasesListener.pullObject();
		// pass them along to the next machine
		SimpleSender weightsBiasesSender = new SimpleSender(nextMachine, NNHelperMethods.WEIGHTS_BIASES_PORT,
				magicSleepTime, magicBadConnectionRetries);
		weightsBiasesSender.establishConnection();

		weightsBiasesSender.sendObject(weightsAndBiases);


		MultiFormatMatrix newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});;
		
		// seed the process by doing a linesearch up front...
		
		// initialize some stuff
		double trainingErrorTotal      = 0.0;
		double validationErrorTotal    = 0.0;
		double   trainingNCorrectTotal   = 0;
		double   validationNCorrectTotal = 0;
		
		// initialize the drop points and runner threads
		trainingResultsDrop   = new DeadDropObjectArray(nThreads);
		validationResultsDrop = new DeadDropObjectArray(nThreads);

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			trainingGradientRunnerArray[threadIndex] = new GradientRunner(threadIndex,trainingResultsDrop,
					trainingXT[threadIndex][0], trainingXT[threadIndex][1],
					weightsAndBiases[0], weightsAndBiases[1],
					nHidden, nOptionsMinusOne);
			
			if (useOldSchool) {
				trainingGradientRunnerArray[threadIndex].setUseOldSchool(true);
			}

			new Thread(trainingGradientRunnerArray[threadIndex]).start();
		}

		// accumulate the gradient and training summaries
		gradientThreadBundles = trainingResultsDrop.take();

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			// accumulate the gradient

			newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]); // seed the old gradient
			trainingError[threadIndex]    = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[0]).doubleValue();
			trainingNCorrect[threadIndex] = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[1]).doubleValue();

			trainingErrorTotal   += trainingError[threadIndex];
			trainingNCorrectTotal   += trainingNCorrect[threadIndex];

		}

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

			validationErrorRunnerArray[threadIndex] = new ErrorRunner(threadIndex,validationResultsDrop,
					validationXT[threadIndex][0], validationXT[threadIndex][1],
					weightsAndBiases[0], weightsAndBiases[1],
					nHidden, nOptionsMinusOne);
			
			new Thread(validationErrorRunnerArray[threadIndex]).start();
		}
		
		// accumulate the gradient and training summaries
		validationErrorThreadBundles = validationResultsDrop.take();


		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			// record progress
			validationErrorTotal    += ((double[])(validationErrorThreadBundles[threadIndex]))[0];
			validationNCorrectTotal += ((double[])(validationErrorThreadBundles[threadIndex]))[1];
		}

		// we now have everything to do the summary report...
		// so, we need to get the pieces from the previous machine and then send them off to the next one...

		///////////////////////
		// first, let's grab the total number of examples. since this only really needs to be done once,
		// we'll try to shut it down before going on to the others

		long nTrainingTotalSoFar   = ((Long)trainingNTotalListener.pullObject()  ).longValue();
		long nValidationTotalSoFar = ((Long)validationNTotalListener.pullObject()).longValue();

		nTrainingTotalSoFar   += nTrainingTotal;
		nValidationTotalSoFar += nValidationTotal;

		SimpleSender trainingNTotalSender = new SimpleSender(nextMachine, NNHelperMethods.TRAINING_N_PORT,
				magicSleepTime, magicBadConnectionRetries);
		trainingNTotalSender.establishConnection();

		SimpleSender validationNTotalSender = new SimpleSender(nextMachine, NNHelperMethods.VALIDATION_N_PORT,
				magicSleepTime, magicBadConnectionRetries);
		validationNTotalSender.establishConnection();

		trainingNTotalSender.sendObject(  nTrainingTotalSoFar);
		validationNTotalSender.sendObject(nValidationTotalSoFar);

		trainingNTotalListener.stopListening();
		validationNTotalListener.stopListening();
		
		trainingNTotalSender.closeConnection();
		validationNTotalSender.closeConnection();


		///////////////////////
		// second, let's do the errors and gradients...
		

		
		double trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
		double trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

		double validationErrorSoFar         = ((Double)validationErrorListener.pullObject()).doubleValue();
		double validationNCorrectTotalSoFar = ((Double)validationNCorrectListener.pullObject()).doubleValue();
		
		MultiFormatMatrix gradientSoFar = (MultiFormatMatrix)gradientListener.pullObject();
		
		trainingErrorSoFar         += trainingErrorTotal;
		trainingNCorrectTotalSoFar += trainingNCorrectTotal;
		validationErrorSoFar         += validationErrorTotal;
		validationNCorrectTotalSoFar += validationNCorrectTotal;
		
		gradientSoFar = MatrixOperations.addMatrices(gradientSoFar, newGradient);
		
		// pass them along to the next machine
		SimpleSender trainingErrorSender = new SimpleSender(nextMachine, NNHelperMethods.TRAINING_ERROR_PORT,
				magicSleepTime, magicBadConnectionRetries);
		trainingErrorSender.establishConnection();
		SimpleSender trainingNCorrectSender = new SimpleSender(nextMachine, NNHelperMethods.TRAINING_N_CORRECT_PORT,
				magicSleepTime, magicBadConnectionRetries);
		trainingNCorrectSender.establishConnection();
		SimpleSender validationErrorSender = new SimpleSender(nextMachine, NNHelperMethods.VALIDATION_ERROR_PORT,
				magicSleepTime, magicBadConnectionRetries);
		validationErrorSender.establishConnection();
		SimpleSender validationNCorrectSender = new SimpleSender(nextMachine, NNHelperMethods.VALIDATION_N_CORRECT_PORT,
				magicSleepTime, magicBadConnectionRetries);
		validationNCorrectSender.establishConnection();
		
		SimpleSender gradientSender = new SimpleSender(nextMachine, NNHelperMethods.GRADIENT_PORT,
				magicSleepTime, magicBadConnectionRetries);
		gradientSender.establishConnection();
		
		
		trainingErrorSender.sendObject(trainingErrorSoFar);
		trainingNCorrectSender.sendObject(trainingNCorrectTotalSoFar);
		validationErrorSender.sendObject(validationErrorSoFar);
		validationNCorrectSender.sendObject(validationNCorrectTotalSoFar);

		gradientSender.sendObject(gradientSoFar);


		///////////////////////////////////////////
		// begin UGLY line search implementation //
		///////////////////////////////////////////
				
		// section until we are within some tolerance

		DeadDropObjectArray lineSearchResultsDrop = new DeadDropObjectArray(nThreads);
		ErrorRunner[]  lineSearchErrorRunnerArray = new ErrorRunner[nThreads];
		Object[]     lineSearchErrorThreadBundles = new Object[nThreads];
		

		// get the line search stuff set up...

		SimpleSender stepErrorSender = new SimpleSender(nextMachine, NNHelperMethods.LINE_SEARCH_ERROR_PORT,
				magicSleepTime, magicBadConnectionRetries);
		stepErrorSender.establishConnection();
		SimpleSender continueLineSearchSender = new SimpleSender(nextMachine, NNHelperMethods.CONTINUE_SEARCH_PORT,
				magicSleepTime, magicBadConnectionRetries);
		continueLineSearchSender.establishConnection();
		SimpleSender stepParametersSender = new SimpleSender(nextMachine, NNHelperMethods.LINE_SEARCH_WB_PORT,
				magicSleepTime, magicBadConnectionRetries);
		stepParametersSender.establishConnection();
		
		// actually get them and pass them on
		boolean weDoNotWantAnotherRoundFromWorkers = false; //((Boolean)continueLineSearchListener.pullObject()).booleanValue();
		
		MultiFormatMatrix[] stepWeightsAndBiasesToUse = null; // (MultiFormatMatrix[])stepParametersListener.pullObject();

		double totalErrorThisStep = Double.NaN;
		double totalErrorThisStepSoFar = Double.NaN;

		int nLineSearchStepsTotal = 0;
		

		while (true) {
			nLineSearchStepsTotal++;
			weDoNotWantAnotherRoundFromWorkers = ((Boolean)continueLineSearchListener.pullObject()).booleanValue();
			continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromWorkers);

			
			if (weDoNotWantAnotherRoundFromWorkers) {
				break;
			}
			stepWeightsAndBiasesToUse = (MultiFormatMatrix[])stepParametersListener.pullObject();
			stepParametersSender.sendObject(stepWeightsAndBiasesToUse);
			
			// determine the errors associated with this step
			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					lineSearchErrorRunnerArray[threadIndex] = new ErrorRunner(threadIndex,lineSearchResultsDrop,
							trainingXT[threadIndex][0], trainingXT[threadIndex][1],
							stepWeightsAndBiasesToUse[0], stepWeightsAndBiasesToUse[1],
							nHidden, nOptionsMinusOne);
					new Thread(lineSearchErrorRunnerArray[threadIndex]).start();
			} // end of threads finding their errors...

			// try to grab them
			lineSearchErrorThreadBundles = lineSearchResultsDrop.take();

			// accumulate them into a unified step summary
			totalErrorThisStep = 0.0;
			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				totalErrorThisStep += ((double[])(lineSearchErrorThreadBundles[threadIndex]))[0];
			}
			
			// get the previous machine's total and tack ours onto it...
			totalErrorThisStepSoFar = ((Double)stepErrorListener.pullObject()).doubleValue();
			
			totalErrorThisStepSoFar += totalErrorThisStep;
			
			stepErrorSender.sendObject(totalErrorThisStepSoFar);
			
		} // end of while tolerance not met

		
		///////////////////////////////////////////
		//  end UGLY line search implementation  //
		///////////////////////////////////////////
		
		
		
		
		
		
		//////////////////
		//////////////////
		//////////////////
		// Begin Epochs //
		//////////////////
		//////////////////
		//////////////////

		System.out.println("#" + sequenceNumber + " ----- out of initial LS, on to the EPOCHS!!! -------");

		SimpleSender continueEpochSender = new SimpleSender(nextMachine, NNHelperMethods.CONTINUE_EPOCH_PORT,
				magicSleepTime, magicBadConnectionRetries);
		continueEpochSender.establishConnection();

		SimpleSender doValidationSender = new SimpleSender(nextMachine, NNHelperMethods.DO_VALIDATION_PORT,
				magicSleepTime, magicBadConnectionRetries);
		doValidationSender.establishConnection();

		// grab the first "continue" and "check" flags and pass them on....
		boolean continueEpoch = ((Boolean)continueEpochListener.pullObject()).booleanValue();
		boolean doValidation = ((Boolean)doValidationListener.pullObject()).booleanValue();

		continueEpochSender.sendObject(continueEpoch);
		doValidationSender.sendObject(doValidation);

		// actually start doing the work.
		
		while (continueEpoch) {


			// grab the latest set of weights and biases...
			weightsAndBiases    = (MultiFormatMatrix[])weightsBiasesListener.pullObject();
			weightsBiasesSender.sendObject(weightsAndBiases);

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				trainingGradientRunnerArray[threadIndex] = new GradientRunner(threadIndex,trainingResultsDrop,
						trainingXT[threadIndex][0], trainingXT[threadIndex][1],
						weightsAndBiases[0], weightsAndBiases[1],
						nHidden, nOptionsMinusOne);
				
				if (useOldSchool) {
					trainingGradientRunnerArray[threadIndex].setUseOldSchool(true);
				}

				new Thread(trainingGradientRunnerArray[threadIndex]).start();
			}

			// accumulate the gradient and training summaries
			gradientThreadBundles = trainingResultsDrop.take();

//			newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});;
			newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});
			trainingErrorTotal      = 0.0;
			validationErrorTotal    = 0.0;
			trainingNCorrectTotal   = 0;
			validationNCorrectTotal = 0;
			
			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				// accumulate the gradient
				newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]); // seed the old gradient
				trainingError[threadIndex]    = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[0]).doubleValue();
				trainingNCorrect[threadIndex] = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[1]).doubleValue();

				trainingErrorTotal   += trainingError[threadIndex];
				trainingNCorrectTotal   += trainingNCorrect[threadIndex];

			}

			if (doValidation) {
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

					validationErrorRunnerArray[threadIndex] = new ErrorRunner(threadIndex,validationResultsDrop,
							validationXT[threadIndex][0], validationXT[threadIndex][1],
							weightsAndBiases[0], weightsAndBiases[1],
							nHidden, nOptionsMinusOne);

					new Thread(validationErrorRunnerArray[threadIndex]).start();
				}

				// accumulate the gradient and training summaries
				validationErrorThreadBundles = validationResultsDrop.take();


				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					// record progress
					validationErrorTotal    += ((double[])(validationErrorThreadBundles[threadIndex]))[0];
					validationNCorrectTotal += ((double[])(validationErrorThreadBundles[threadIndex]))[1];
				}

				validationErrorSoFar         = ((Double)validationErrorListener.pullObject()).doubleValue();
				validationNCorrectTotalSoFar = ((Double)validationNCorrectListener.pullObject()).doubleValue();

				validationErrorSoFar         += validationErrorTotal;
				validationNCorrectTotalSoFar += validationNCorrectTotal;

				validationErrorSender.sendObject(validationErrorSoFar);
				validationNCorrectSender.sendObject(validationNCorrectTotalSoFar);

			} // end if (doValidation)
			
			// we now have everything to do the summary report...
			// so, we need to get the pieces from the previous machine and then send them off to the next one...
			// let's do the errors and gradients...
						
			trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
			trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

			
			gradientSoFar = (MultiFormatMatrix)gradientListener.pullObject();
			
			trainingErrorSoFar         += trainingErrorTotal;
			trainingNCorrectTotalSoFar += trainingNCorrectTotal;
			
			gradientSoFar = MatrixOperations.addMatrices(gradientSoFar, newGradient);
			
			// pass them along to the next machine
			trainingErrorSender.sendObject(trainingErrorSoFar);
			trainingNCorrectSender.sendObject(trainingNCorrectTotalSoFar);

			gradientSender.sendObject(gradientSoFar);
			
			///////////////////////////////////////////
			// begin UGLY line search implementation //
			///////////////////////////////////////////
			
			// section until we are within some tolerance

			// actually get them and pass them on
			weDoNotWantAnotherRoundFromWorkers = false; //((Boolean)continueLineSearchListener.pullObject()).booleanValue();
			
			while (true) {
				nLineSearchStepsTotal++;
				weDoNotWantAnotherRoundFromWorkers = ((Boolean)continueLineSearchListener.pullObject()).booleanValue();
				continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromWorkers);
				
				if (weDoNotWantAnotherRoundFromWorkers) {
					break;
				}
				stepWeightsAndBiasesToUse = (MultiFormatMatrix[])stepParametersListener.pullObject();
				stepParametersSender.sendObject(stepWeightsAndBiasesToUse);

				// determine the errors associated with this step
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
						lineSearchErrorRunnerArray[threadIndex] = new ErrorRunner(threadIndex,lineSearchResultsDrop,
								trainingXT[threadIndex][0], trainingXT[threadIndex][1],
								stepWeightsAndBiasesToUse[0], stepWeightsAndBiasesToUse[1],
								nHidden, nOptionsMinusOne);
						new Thread(lineSearchErrorRunnerArray[threadIndex]).start();
				} // end of threads finding their errors...

				// try to grab them
				lineSearchErrorThreadBundles = lineSearchResultsDrop.take();

				// accumulate them into a unified step summary
				totalErrorThisStep = 0.0;
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					totalErrorThisStep += ((double[])(lineSearchErrorThreadBundles[threadIndex]))[0];
				}
				
				// get the previous machine's total and tack ours onto it...
				totalErrorThisStepSoFar = ((Double)stepErrorListener.pullObject()).doubleValue();
				
				totalErrorThisStepSoFar += totalErrorThisStep;
				
				stepErrorSender.sendObject(totalErrorThisStepSoFar);
				
			} // end of while tolerance not met

			///////////////////////////////////////////
			//  end UGLY line search implementation  //
			///////////////////////////////////////////

			// get the next continuation flags....
			continueEpoch = ((Boolean)continueEpochListener.pullObject()).booleanValue();
			doValidation = ((Boolean)doValidationListener.pullObject()).booleanValue();

			continueEpochSender.sendObject(continueEpoch);
			doValidationSender.sendObject(doValidation);


			System.gc();
		} // end for epoch
	
		
		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  #" + sequenceNumber + " All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


