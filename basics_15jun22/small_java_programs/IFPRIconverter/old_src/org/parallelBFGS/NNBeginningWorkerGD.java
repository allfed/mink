package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;


public class NNBeginningWorkerGD {

	private static void debugOut(String message, int sequenceNumber) {
		System.out.println("#" +  sequenceNumber + ": " + message + " [" + new Date() + "]");
	}


	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length != 4) {
			System.out.println("Usage: classname initFile dataFileBase nThreadsThis sequenceNumber nextMachine");
		}

		String initFile    =                  commandLineOptions[0];
		String dataFile    =                  commandLineOptions[1];
		int nThreads       = Integer.parseInt(commandLineOptions[2]);
		int sequenceNumber = Integer.parseInt(commandLineOptions[3]);
		String nextMachine =                  commandLineOptions[4];

		boolean doDebugStatements = false;
		if (commandLineOptions.length == 6) {
			doDebugStatements = true;
		}

		///////////////////
		// magic numbers //
		///////////////////
//		double euclideanStepSize = 0.01;
//		double stoppingTolerance = 0.01;
//		double fractionAboveMinGuess = 0.1;

		long magicSleepTime = 50;
		int magicBadConnectionRetries = 5;

		// pull the magic numbers from the previous machine
		Object[] initValues = NNHelperMethods.readInitializationFile(initFile);


		int nExplanatoryVariables = ((Integer)initValues[0]).intValue();
		int nHidden               = ((Integer)initValues[1]).intValue();
		int nOptionsMinusOne      = ((Integer)initValues[2]).intValue();
		long nElementsThreshold   = ((   Long)initValues[3]).longValue();
		double fractionInTraining = (( Double)initValues[4]).doubleValue();
		double learningRate       = (( Double)initValues[5]).doubleValue();
		String weightSeedBaseName =   (String)initValues[6];
		String biasSeedBaseName   =   (String)initValues[7];
		int nEpochs               = ((Integer)initValues[8]).intValue();
		int checkNumber           = ((Integer)initValues[9]).intValue();
		String     weightBaseName =   (String)initValues[10];
		String       biasBaseName =   (String)initValues[11];
		String   gradientBaseName =   (String)initValues[12];
		String    hessianBaseName =   (String)initValues[13];
		String     reportBaseName =   (String)initValues[14];
		String          delimiter =   (String)initValues[15];
		long          aRandomSeed = ((   Long)initValues[16]).longValue();

		System.out.println("data file: " + dataFile);


		System.out.println("----- begin initialization values -----");
		System.out.println("nExplanatoryVariables = [" + nExplanatoryVariables + "]");
		System.out.println("nHidden = [" + nHidden + "]");
		System.out.println("nOptionsMinusOne = [" + nOptionsMinusOne + "]");
		System.out.println("nElementsThreshold = [" + nElementsThreshold + "]");
		System.out.println("FractionInTraining = [" + fractionInTraining + "]");
		System.out.println("LearningRate = [" + learningRate + "]");
		System.out.println("WeightSeedBaseName = [" + weightSeedBaseName + "]");
		System.out.println("BiasSeedBaseName = [" + biasSeedBaseName + "]");
		System.out.println("nEpochs = [" + nEpochs + "]");
		System.out.println("CheckNumber = [" + checkNumber + "]");
		System.out.println("WeightBaseName = [" + weightBaseName + "]");
		System.out.println("BiasBaseName = [" + biasBaseName + "]");
		System.out.println("GradientBaseName = [" + gradientBaseName + "]");
		System.out.println("HessianBaseName = [" + hessianBaseName + "]");
		System.out.println("ReportBaseName = [" + reportBaseName + "]");
		System.out.println("Delimiter = [" + delimiter + "]");
		System.out.println("aRandomSeed = [" + aRandomSeed + "]");
		System.out.println("------ end initialization values ------");

		System.out.println();


		// start up all the listeners....
		SimpleReceiver trainingNTotalListener     = new SimpleReceiver(NNHelperMethods.TRAINING_N_PORT,"tN" + sequenceNumber);
		SimpleReceiver validationNTotalListener   = new SimpleReceiver(NNHelperMethods.VALIDATION_N_PORT,"vN"+ sequenceNumber);
		SimpleReceiver trainingNCorrectListener   = new SimpleReceiver(NNHelperMethods.TRAINING_N_CORRECT_PORT,"tNC"+ sequenceNumber);
		SimpleReceiver trainingErrorListener      = new SimpleReceiver(NNHelperMethods.TRAINING_ERROR_PORT,"tE"+ sequenceNumber);
		SimpleReceiver validationNCorrectListener = new SimpleReceiver(NNHelperMethods.VALIDATION_N_CORRECT_PORT,"vNC"+ sequenceNumber);
		SimpleReceiver validationErrorListener    = new SimpleReceiver(NNHelperMethods.VALIDATION_ERROR_PORT,"vE"+ sequenceNumber);
		SimpleReceiver gradientListener           = new SimpleReceiver(NNHelperMethods.GRADIENT_PORT,"gradient"+ sequenceNumber);
//		SimpleReceiver stepErrorListener          = new SimpleReceiver(NNHelperMethods.LINE_SEARCH_ERROR_PORT,"LS error"+ sequenceNumber);

		trainingNTotalListener.setVerbosity(false);
		validationNTotalListener.setVerbosity(false);
		trainingNCorrectListener.setVerbosity(false);
		trainingErrorListener.setVerbosity(false);
		validationNCorrectListener.setVerbosity(false);
		validationErrorListener.setVerbosity(false);
		gradientListener.setVerbosity(false);
//		stepErrorListener.setVerbosity(false);

		
		new Thread(trainingNTotalListener).start();
		new Thread(validationNTotalListener).start();
		new Thread(trainingNCorrectListener).start();
		new Thread(trainingErrorListener).start();
		new Thread(validationNCorrectListener).start();
		new Thread(validationErrorListener).start();
		new Thread(gradientListener).start();
//		new Thread(stepErrorListener).start();

		// let's sleep for a moment to let things get going
		Thread.sleep(magicSleepTime);



		// send them along to the next machine
		SimpleSender oneTimeParametersSender = new SimpleSender(nextMachine, NNHelperMethods.ONE_TIME_PARAMETERS_PORT,
				magicSleepTime, magicBadConnectionRetries);
		oneTimeParametersSender.establishConnection();
		oneTimeParametersSender.sendObject(nHidden);
		oneTimeParametersSender.sendObject(nOptionsMinusOne);
		oneTimeParametersSender.sendObject(fractionInTraining);
		oneTimeParametersSender.sendObject(aRandomSeed);

		// now try to be clever and clean up by closing the stream
		oneTimeParametersSender.closeConnection();

		// set up the output for the summary

		File fileToWrite = new File(reportBaseName);
		FileOutputStream summaryStream = new FileOutputStream(fileToWrite, true);
		PrintWriter summaryOut = new PrintWriter(summaryStream);


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
			System.out.println("#" + sequenceNumber + " -> creating data block for thread #" + threadIndex);
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

		if (doDebugStatements) {
			debugOut(" doing T/V split ",sequenceNumber);
		}
		MultiFormatFloat[][] trainingValidationPairs = new MultiFormatFloat[nThreads][2];

		MultiFormatFloat[][] trainingXT   = new MultiFormatFloat[nThreads][2];
		MultiFormatFloat[][] validationXT = new MultiFormatFloat[nThreads][2];

		long[] nTraining = new long[nThreads];
		long[] nValidation = new long[nThreads];

		long nTrainingTotal = 0;   // this actually needs to be zero
		long nValidationTotal = 0; // this actually needs to be zero

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			System.out.println("#" + sequenceNumber + " -> T/V X/T splits for thread #" + threadIndex);

			trainingValidationPairs[threadIndex] = MatrixOperations.randomizeRows2dMff(allDataBlocks[threadIndex], aRandomSeed, fractionInTraining,1);

			nTraining[threadIndex]   = trainingValidationPairs[threadIndex][0].getDimensions()[0];
			nValidation[threadIndex] = trainingValidationPairs[threadIndex][1].getDimensions()[0];

			// figure out the total number of training and validation examples
			nTrainingTotal   += nTraining[threadIndex];
			nValidationTotal += nValidation[threadIndex];

			trainingXT[threadIndex]   = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][0]);
			validationXT[threadIndex] = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][1]);
		}





		// load up the parameter seeds...

		MultiFormatMatrix[] weightsAndBiases    = new MultiFormatMatrix[2];
		weightsAndBiases[0] = MatrixOperations.read2DMFMfromText(weightSeedBaseName);
		weightsAndBiases[1] = MatrixOperations.read2DMFMfromText(biasSeedBaseName);



		// pass them along to the next machine
		SimpleSender weightsBiasesSender = new SimpleSender(nextMachine, NNHelperMethods.WEIGHTS_BIASES_PORT,
				magicSleepTime, magicBadConnectionRetries);
		weightsBiasesSender.establishConnection();

		weightsBiasesSender.sendObject(weightsAndBiases);





		// set up the training loop
		DeadDropObjectArray trainingResultsDrop   = null;
		GradientRunner[] trainingGradientRunnerArray = new GradientRunner[nThreads];

		DeadDropObjectArray validationResultsDrop = null;
		ErrorRunner[]    validationErrorRunnerArray  = new ErrorRunner[   nThreads];


		double[] trainingError      = new double[nThreads];
		double[] trainingNCorrect   = new double[nThreads];
//		double[][] validationError  = new double[nThreads][2];
		Object[] gradientThreadBundles = new Object[nThreads];
		Object[] validationErrorThreadBundles = new Object[nThreads];

//		MultiFormatMatrix[] oldWeightsAndBiases = new MultiFormatMatrix[2];

//		MultiFormatMatrix oldGradient = null;
		MultiFormatMatrix newGradient = null;
		MultiFormatMatrix parameterStep = null;

		// seed the bigG with an identity matrix; nParameters = nWeights + nBiases - nXs
		// (there are all those placeholders for the explanatory variables in the biases definition)
//		MultiFormatMatrix bigGSeed = MatrixOperations.identityMFM(
//				(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1);

//		MultiFormatMatrix[] bfgsDirectionAndBigG = new MultiFormatMatrix[] {null,bigGSeed};

		// write down the old ones before moving on...
//		System.out.println("weights object is: " + weightsAndBiases[0]);
//		System.out.println("biases object is:  " + weightsAndBiases[1]);
//		MatrixOperations.print2dMFM(weightsAndBiases[0],"weights");
//		MatrixOperations.print2dMFM(weightsAndBiases[1],"biases");
//		oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
//		oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);

		
		if (doDebugStatements) {
			debugOut("starting training",sequenceNumber);
		}


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

			new Thread(trainingGradientRunnerArray[threadIndex]).start();
		}

		// accumulate the gradient and training summaries
		gradientThreadBundles = trainingResultsDrop.take();

		newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			// accumulate the gradient
			if (doDebugStatements) {
				debugOut("1st time gradient accumulator size r=" + newGradient.getDimensions()[0] +
						" c=" + newGradient.getDimensions()[1],sequenceNumber);
				debugOut("1st time bit to accumulate r=" +
						((MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]).getDimensions()[0] +
						" c=" +
						((MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]).getDimensions()[1],sequenceNumber);
			}

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
		// first, let's send the total number of examples. since this only really needs to be done once,
		// we'll try to shut it down before going on to the others

		long nTrainingTotalSoFar   = 0;
		long nValidationTotalSoFar = 0;

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

		trainingNTotalSender.closeConnection();
		validationNTotalSender.closeConnection();

		///////////////////////
		// second, let's do the errors and gradients...




		double trainingErrorSoFar         = 0.0;
		double trainingNCorrectTotalSoFar = 0.0;

		double validationErrorSoFar         = 0.0;
		double validationNCorrectTotalSoFar = 0.0;


		trainingErrorSoFar         += trainingErrorTotal;
		trainingNCorrectTotalSoFar += trainingNCorrectTotal;
		validationErrorSoFar         += validationErrorTotal;
		validationNCorrectTotalSoFar += validationNCorrectTotal;

		MultiFormatMatrix gradientSoFar = newGradient;
//		gradientSoFar = MatrixOperations.addMatrices(gradientSoFar, newGradient);

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





		//////////////////////////////
		/// begin spot for summary ///
		//////////////////////////////

		// the totals in training and validation only have to be grabbed once.

		nTrainingTotalSoFar   = ((Long)trainingNTotalListener.pullObject()  ).longValue();
		nValidationTotalSoFar = ((Long)validationNTotalListener.pullObject()).longValue();

		trainingNTotalListener.stopListening();
		validationNTotalListener.stopListening();



		trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
		trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

		validationErrorSoFar         = ((Double)validationErrorListener.pullObject()).doubleValue();
		validationNCorrectTotalSoFar = ((Double)validationNCorrectListener.pullObject()).doubleValue();


		// we now have everything to do the summary report...
		// Beware the MAGIC NUMBER!!! epochIndex = 0
		summaryOut.println(0 + "\t" +
				trainingErrorSoFar + "\t" +
				validationErrorSoFar + "\t" +
				Math.exp(-trainingErrorSoFar/nTrainingTotalSoFar) + "\t" +
				Math.exp(-validationErrorSoFar/nValidationTotalSoFar) + "\t" +
				trainingNCorrectTotalSoFar/nTrainingTotalSoFar + "\t" +
				validationNCorrectTotalSoFar/nValidationTotalSoFar + "\t" +
				nTrainingTotalSoFar + "\t" +
				nValidationTotalSoFar + "\t" +
				nHidden + "\t" +
				newGradient.getDimensions()[0]);
		summaryOut.flush();


		//////////////////////////////
		///  end spot for summary  ///
		//////////////////////////////


		// grab the total gradient after it has made its way around the ring

		newGradient = (MultiFormatMatrix)gradientListener.pullObject();

		
//		oldGradient = MatrixOperations.newCopy(newGradient);




		///////////////////////////////////////////
		// begin UGLY line search implementation //
		///////////////////////////////////////////
			parameterStep = MatrixOperations.multiplyByConstant(learningRate,newGradient);

			weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);



		///////////////////////////////////////////
		//  end UGLY line search implementation  //
		///////////////////////////////////////////


		// write down all the parameters. we'll keep these together

		// Beware the MAGIC NUMBER!!! epochIndex = 0
		MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + 0, delimiter);
		MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + 0, delimiter);
		MatrixOperations.write2DMFMtoText(newGradient,gradientBaseName + 0, delimiter);
		// there is no BFGS direction to begin with, we merely use the gradient at the seed.
//		MatrixOperations.write2DMFMtoText(oldGradient,gradientBaseName + "_bfgs_" + 0, delimiter);
//		MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[1],gradientBaseName + "_bfgsG_" + 0, delimiter);





		//////////////////
		//////////////////
		//////////////////
		// Begin Epochs //
		//////////////////
		//////////////////
		//////////////////


		if (doDebugStatements) {
			debugOut("sleeping before moving on to epochs",sequenceNumber);
			Thread.sleep(1500);
		}
		SimpleSender continueEpochSender = new SimpleSender(nextMachine, NNHelperMethods.CONTINUE_EPOCH_PORT,
				magicSleepTime, magicBadConnectionRetries);
		continueEpochSender.establishConnection();

		SimpleSender doValidationSender = new SimpleSender(nextMachine, NNHelperMethods.DO_VALIDATION_PORT,
				magicSleepTime, magicBadConnectionRetries);
		doValidationSender.establishConnection();







		for (int epochIndex = 1; epochIndex < nEpochs; epochIndex++) {
			if (epochIndex == 1 || epochIndex % checkNumber == 0) {
				System.out.println("-- epoch #" + epochIndex + " " + new Date() + " --");
			}
			trainingErrorTotal      = 0.0;
			validationErrorTotal    = 0.0;
			trainingNCorrectTotal   = 0;
			validationNCorrectTotal = 0;
			newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});

			// tell all the others that we are still doing something...
			continueEpochSender.sendObject(true);

			// tell all the others whether to bother with the validation stuff
			if (epochIndex % checkNumber == 0) {
				doValidationSender.sendObject(true);
			} else {
				doValidationSender.sendObject(false);
			}

			// send out the latest set of weights and biases...
			weightsBiasesSender.sendObject(weightsAndBiases);

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				trainingGradientRunnerArray[threadIndex] = new GradientRunner(threadIndex,trainingResultsDrop,
						trainingXT[threadIndex][0], trainingXT[threadIndex][1],
						weightsAndBiases[0], weightsAndBiases[1],
						nHidden, nOptionsMinusOne);

				new Thread(trainingGradientRunnerArray[threadIndex]).start();
			}

			// accumulate the gradient and training summaries
			gradientThreadBundles = trainingResultsDrop.take();

			if (doDebugStatements) {
				debugOut("message",sequenceNumber);
			}

			newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				// accumulate the gradient
				if (doDebugStatements) {
					debugOut("gradient accumulator size r=" + newGradient.getDimensions()[0] +
							" c=" + newGradient.getDimensions()[1],sequenceNumber);
					debugOut("bit to accumulate r=" +
							((MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]).getDimensions()[0] +
							" c=" +
							((MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]).getDimensions()[1],sequenceNumber);
				}

				newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]); // seed the old gradient
				trainingError[threadIndex]    = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[0]).doubleValue();
				trainingNCorrect[threadIndex] = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[1]).doubleValue();

				trainingErrorTotal   += trainingError[threadIndex];
				trainingNCorrectTotal   += trainingNCorrect[threadIndex];

			}

			if (doDebugStatements) {
				debugOut("accumulated gradient",sequenceNumber);
			}

			if (epochIndex % checkNumber == 0) {

				if (doDebugStatements) {
					debugOut("inside validation block (" + epochIndex + "/" + checkNumber + ")",sequenceNumber);
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

				validationErrorSender.sendObject(validationErrorTotal);
				validationNCorrectSender.sendObject(validationNCorrectTotal);

				// wait for them to come back around...
				validationErrorSoFar         = ((Double)validationErrorListener.pullObject()).doubleValue();
				validationNCorrectTotalSoFar      = ((Double)validationNCorrectListener.pullObject()).doubleValue();

				if (doDebugStatements) {
					debugOut("validation stuff done accumulating",sequenceNumber);
				}

			} // end if (doValidation)

			// we now have everything to do the summary report...
			// so, we need to get the pieces from the previous machine and then send them off to the next one...
			// let's do the errors and gradients...


			gradientSoFar = MatrixOperations.addMatrices(gradientSoFar, newGradient);

			// pass them along to the next machine
			trainingErrorSender.sendObject(trainingErrorTotal);
			trainingNCorrectSender.sendObject(trainingNCorrectTotal);

			gradientSender.sendObject(gradientSoFar);

			// wait for everything to come back around...
			trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
			trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

			newGradient = (MultiFormatMatrix)gradientListener.pullObject();



//			bfgsDirectionAndBigG = NNHelperMethods.findBFGSDirection(
//					oldWeightsAndBiases[0], oldWeightsAndBiases[1],
//					oldGradient,	bfgsDirectionAndBigG[1],
//					weightsAndBiases[0], weightsAndBiases[1],
//					newGradient);
//
//			oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
//			oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);
//
//			oldGradient = MatrixOperations.newCopy(newGradient);



			///////////////////////////////////////////
			// begin UGLY line search implementation //
			///////////////////////////////////////////
				parameterStep = MatrixOperations.multiplyByConstant(learningRate,newGradient);
				weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);

			///////////////////////////////////////////
			//  end UGLY line search implementation  //
			///////////////////////////////////////////


			// do the status reporting...
			if (epochIndex % checkNumber == 0) {


				summaryOut.println(epochIndex + "\t" +
						trainingErrorSoFar + "\t" +
						validationErrorSoFar + "\t" +
						Math.exp(-trainingErrorSoFar/nTrainingTotalSoFar) + "\t" +
						Math.exp(-validationErrorSoFar/nValidationTotalSoFar) + "\t" +
						trainingNCorrectTotalSoFar/nTrainingTotalSoFar + "\t" +
						validationNCorrectTotalSoFar/nValidationTotalSoFar + "\t" +
						nTrainingTotalSoFar + "\t" +
						nValidationTotalSoFar + "\t" +
						nHidden + "\t" +
						newGradient.getDimensions()[0]);
				summaryOut.flush();

				summaryOut.close();
				summaryStream = new FileOutputStream(fileToWrite, true);
				summaryOut = new PrintWriter(summaryStream);


				MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(newGradient,gradientBaseName + epochIndex, delimiter);
//				MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[0],gradientBaseName + "_bfgs_" + epochIndex, delimiter);
//				MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[1],gradientBaseName + "_bfgsG_" + epochIndex, delimiter);
			}





			System.gc();
		} // end for epoch

		// tell all the others that we're done...
		continueEpochSender.sendObject(false);
		doValidationSender.sendObject(false);
		

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  #" + sequenceNumber + " All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


