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


public class NNBeginningWorkerWeighted {

	private static void debugOut(String message, int sequenceNumber) {
		System.out.println("#" +  sequenceNumber + ": " + message + " [" + new Date() + "]");
	}


	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length < 5) {
			System.out.println("Usage: classname initFile dataFileBase nThreadsThis sequenceNumber nextMachine [euclideanStepSize,stoppingTolerance,fractionAboveMinGuess]");
		}

		String initFile    =                  commandLineOptions[0];
		String dataFile    =                  commandLineOptions[1];
		int nThreads       = Integer.parseInt(commandLineOptions[2]);
		int sequenceNumber = Integer.parseInt(commandLineOptions[3]);
		String nextMachine =                  commandLineOptions[4];


		
		boolean doDebugStatements = false;
		if (commandLineOptions.length == 7) {
			doDebugStatements = true;
		}

		///////////////////
		// magic numbers //
		///////////////////
		double euclideanStepSize = 0.01;
		double stoppingTolerance = 0.01;
		double fractionAboveMinGuess = 0.1;
		if (commandLineOptions.length >= 6) {
			euclideanStepSize     = Double.parseDouble(commandLineOptions[5].split(",")[0]);
			stoppingTolerance     = Double.parseDouble(commandLineOptions[5].split(",")[1]);
			fractionAboveMinGuess = Double.parseDouble(commandLineOptions[5].split(",")[2]);
		}

		
		
		
		
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
		System.out.println("nExplanatoryVariables (unused here, but hey)= [" + nExplanatoryVariables + "]");
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
		SimpleReceiver stepErrorListener          = new SimpleReceiver(NNHelperMethods.LINE_SEARCH_ERROR_PORT,"LS error"+ sequenceNumber);

		trainingNTotalListener.setVerbosity(false);
		validationNTotalListener.setVerbosity(false);
		trainingNCorrectListener.setVerbosity(false);
		trainingErrorListener.setVerbosity(false);
		validationNCorrectListener.setVerbosity(false);
		validationErrorListener.setVerbosity(false);
		gradientListener.setVerbosity(false);
		stepErrorListener.setVerbosity(false);

		
		new Thread(trainingNTotalListener).start();
		new Thread(validationNTotalListener).start();
		new Thread(trainingNCorrectListener).start();
		new Thread(trainingErrorListener).start();
		new Thread(validationNCorrectListener).start();
		new Thread(validationErrorListener).start();
		new Thread(gradientListener).start();
		new Thread(stepErrorListener).start();

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
//		long nXs = allDataMatrix.getDimensions()[1] - 1;
		long nXs = allDataMatrix.getDimensions()[1] - 2; // we now have a set of "importance weights" that go along with the targets...

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
			// and we find our magic number...
			// Beware the MAGIC NUMBER!!! there are two more columns beyond the number of explanatory variables.
			allDataBlocks[threadIndex] = new MultiFormatFloat(1,new long[] {nRowsHere , nXs + 2});
			for (long rowIndex = 0; rowIndex < nRowsHere; rowIndex++) {
				for (long colIndex = 0; colIndex < nXs + 2; colIndex++) {
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

			System.out.println("nTraining[" + threadIndex + "] = " + nTraining[threadIndex] + " ncols = " + trainingValidationPairs[threadIndex][0].getDimensions()[1] +
			" nValidation = " + nValidation[threadIndex] + "; ncols = " + trainingValidationPairs[threadIndex][1].getDimensions()[0]);

			// figure out the total number of training and validation examples
			nTrainingTotal   += nTraining[threadIndex];
			nValidationTotal += nValidation[threadIndex];

//			trainingXT[threadIndex]   = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][0]);
//			validationXT[threadIndex] = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][1]);
			// now changing to last two columns which will be relative importance of example and the target
			trainingXT[threadIndex]   = MatrixOperations.splitOffLastTwoColumns(trainingValidationPairs[threadIndex][0]);
			validationXT[threadIndex] = MatrixOperations.splitOffLastTwoColumns(trainingValidationPairs[threadIndex][1]);
			
//			System.out.println("...immediately after split: trainingXT[" + threadIndex + "[0] cols = " +
//					trainingXT[threadIndex][0].getDimensions()[1] + 
//					"; trainingXT[" + threadIndex + "[1] cols = " +
//					trainingXT[threadIndex][1].getDimensions()[1]
//					);
			
//			// i want to see what is going on with the validation stuff...
//			MultiFormatFloat descriptiveStatsTrainingX = MatrixOperations.descriptiveStatistics(trainingXT[threadIndex][0]);
//			MultiFormatFloat descriptiveStatsTrainingTargets = MatrixOperations.descriptiveStatistics(trainingXT[threadIndex][1]);
//			MultiFormatFloat descriptiveStatsValidationX = MatrixOperations.descriptiveStatistics(validationXT[threadIndex][0]);
//			MultiFormatFloat descriptiveStatsValidationTargets = MatrixOperations.descriptiveStatistics(validationXT[threadIndex][1]);
//
//			System.out.println("training explanatory");
//			MatrixOperations.print2dMFF(descriptiveStatsTrainingX);
//			System.out.println("training targets");
//			MatrixOperations.print2dMFF(descriptiveStatsTrainingTargets);
//
//			System.out.println("validation explanatory");
//			MatrixOperations.print2dMFF(descriptiveStatsValidationX);
//			System.out.println("validation targets");
//			MatrixOperations.print2dMFF(descriptiveStatsValidationTargets);
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
		GradientRunnerWeighted[] trainingGradientRunnerArray = new GradientRunnerWeighted[nThreads];

		DeadDropObjectArray validationResultsDrop = null;
		ErrorRunnerWeighted[]    validationErrorRunnerArray  = new ErrorRunnerWeighted[   nThreads];


		double[] trainingError      = new double[nThreads];
		double[] trainingNCorrect   = new double[nThreads];
//		double[][] validationError  = new double[nThreads][2];
		Object[] gradientThreadBundles = new Object[nThreads];
		Object[] validationErrorThreadBundles = new Object[nThreads];

		MultiFormatMatrix[] oldWeightsAndBiases = new MultiFormatMatrix[2];
//		MultiFormatMatrix[] reallyOldWeightsAndBiases = new MultiFormatMatrix[2];

		MultiFormatMatrix oldGradient = null;
//		MultiFormatMatrix reallyOldGradient = null;
		MultiFormatMatrix newGradient = null;

		// seed the bigG with an identity matrix; nParameters = nWeights + nBiases - nXs
		// (there are all those placeholders for the explanatory variables in the biases definition)
		long nParameters = weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs;
		int bigGFormatIndex = 1;
		if (nParameters * nParameters > nElementsThreshold) {
			bigGFormatIndex = 3;
		}
		MultiFormatMatrix bigGSeed = MatrixOperations.identityMFM(
				(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), bigGFormatIndex);

		MultiFormatMatrix[] bfgsDirectionAndBigG = new MultiFormatMatrix[] {null,bigGSeed};

		// write down the old ones before moving on...
//		System.out.println("weights object is: " + weightsAndBiases[0]);
//		System.out.println("biases object is:  " + weightsAndBiases[1]);
//		MatrixOperations.print2dMFM(weightsAndBiases[0],"weights");
//		MatrixOperations.print2dMFM(weightsAndBiases[1],"biases");
		oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
		oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);

		
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
//			System.out.println("        aaa   ti = " + threadIndex + "; trainingXT[0] cols = " + trainingXT[threadIndex][0].getDimensions()[1] +
//					"; trainingXT[1] cols = " + trainingXT[threadIndex][1].getDimensions()[1] );
			trainingGradientRunnerArray[threadIndex] = new GradientRunnerWeighted(threadIndex,trainingResultsDrop,
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
//		System.out.println("Beginner Validation 1st");
		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

			validationErrorRunnerArray[threadIndex] = new ErrorRunnerWeighted(threadIndex,validationResultsDrop,
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
//			System.out.println("beginner validation: thread = " + threadIndex + " e = " + ((double[])(validationErrorThreadBundles[threadIndex]))[0] + " n = " + ((double[])(validationErrorThreadBundles[threadIndex]))[1]);
		}

		// we now have everything to do the summary report...
		// so, we need to get the pieces from the previous machine and then send them off to the next one...

		///////////////////////
		// first, let's send the total number of examples. since this only really needs to be done once,
		// we'll try to shut it down before going on to the others

//		long nTrainingTotalSoFar   = 0;
//		long nValidationTotalSoFar = 0;
		// let's do this as "total importance", so as doubles...
		double nTrainingTotalSoFar   = 0;
		double nValidationTotalSoFar = 0;

//		sum over importances... and we have to fix up the other workers, too..allDataBlocks.clone().clone().clone().clone()..allDataBlocks.clone().clone().clone().clone()..allDataBlocks.

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			for (int exampleIndex = 0; exampleIndex < trainingXT[threadIndex][1].getDimensions()[0]; exampleIndex++) {
				nTrainingTotalSoFar   += trainingXT[threadIndex][1].getValue(exampleIndex,0); // Beware the MAGIC NUMBER!!! importance is column 0
			}
			for (int exampleIndex = 0; exampleIndex < validationXT[threadIndex][1].getDimensions()[0]; exampleIndex++) {
				nValidationTotalSoFar   += validationXT[threadIndex][1].getValue(exampleIndex,0); // Beware the MAGIC NUMBER!!! importance is column 0
			}
		}

//		nTrainingTotalSoFar   += nTrainingTotal;
//		nValidationTotalSoFar += nValidationTotal;

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

//		System.out.println("1st sending to: " + nextMachine + "\tte=" + trainingErrorSoFar + "\ttn=" + trainingNCorrectTotalSoFar + "\t" + trainingErrorTotal + "/" + trainingNCorrectTotal);
//		System.out.println("1st sending to: " + nextMachine + "\tve=" + validationErrorSoFar + "\tvn=" + validationNCorrectTotalSoFar + "\t" + validationErrorTotal + "/" + validationNCorrectTotal);

		trainingErrorSender.sendObject(trainingErrorSoFar);
		trainingNCorrectSender.sendObject(trainingNCorrectTotalSoFar);
		validationErrorSender.sendObject(validationErrorSoFar);
		validationNCorrectSender.sendObject(validationNCorrectTotalSoFar);

		gradientSender.sendObject(gradientSoFar);





		//////////////////////////////
		/// begin spot for summary ///
		//////////////////////////////

		// the totals in training and validation only have to be grabbed once.

//		nTrainingTotalSoFar   = ((Long)trainingNTotalListener.pullObject()  ).longValue();
//		nValidationTotalSoFar = ((Long)validationNTotalListener.pullObject()).longValue();
		nTrainingTotalSoFar   = ((Double)trainingNTotalListener.pullObject()  ).doubleValue();
		nValidationTotalSoFar = ((Double)validationNTotalListener.pullObject()).doubleValue();

		trainingNTotalListener.stopListening();
		validationNTotalListener.stopListening();



		trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
		trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

		validationErrorSoFar         = ((Double)validationErrorListener.pullObject()).doubleValue();
		validationNCorrectTotalSoFar = ((Double)validationNCorrectListener.pullObject()).doubleValue();


		
		// grab the total gradient after it has made its way around the ring

		newGradient = (MultiFormatMatrix)gradientListener.pullObject();

		oldGradient = MatrixOperations.newCopy(newGradient);

		
		
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
				bfgsDirectionAndBigG[1].getDimensions()[0]);
		summaryOut.flush();

		// write down all the parameters. we'll keep these together

		// Beware the MAGIC NUMBER!!! epochIndex = 0
		MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + 0, delimiter);
		MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + 0, delimiter);
		MatrixOperations.write2DMFMtoText(oldGradient,gradientBaseName + 0, delimiter);
		// there is no BFGS direction to begin with, we merely use the gradient at the seed.
		MatrixOperations.write2DMFMtoText(oldGradient,gradientBaseName + "_bfgs_" + 0, delimiter);
//		MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[1],gradientBaseName + "_bfgsG_" + 0, delimiter);


		//////////////////////////////
		///  end spot for summary  ///
		//////////////////////////////






		///////////////////////////////////////////
		// begin UGLY line search implementation //
		///////////////////////////////////////////

		double errorRange = Double.NaN;
		double finalMultiplier = Double.NaN;

//		double a = -1, b = -2, c = -3, A =-4, B = -5;
//		if (doDebugStatements) {
//		debugOut("a=" + a + " b=" + b + " c=" + c + " ; minGuess=" + minGuess + " at " + (-b/(2*a)),sequenceNumber);
//		debugOut("lEH=" + lowerEdgeHeight + " nETH=" + newEdgeTargetHeight + " nXL=" + newXLower + " nxU=" + newXUpper,sequenceNumber);
//		debugOut("--> restarting: errorRange = " + errorRange + " ; guess = " + finalMultiplier + " ; new size = " + stepSizeToUse,sequenceNumber);
//		}

		double[] stepParameters = null;

		double[] stepErrorsTotal = null;

		Object[] stepWeightsBiasesPairsParameters = null;

		MultiFormatMatrix parameterStep = null; 

		// normalize the direction of the gradient...
		MultiFormatMatrix normalizedDirection = MatrixOperations.normalizeColumn(newGradient);




		// section until we are within some tolerance

		DeadDropObjectArray lineSearchResultsDrop = new DeadDropObjectArray(nThreads);
		ErrorRunnerWeighted[]  lineSearchErrorRunnerArray = new ErrorRunnerWeighted[nThreads];
		Object[]     lineSearchErrorThreadBundles = new Object[nThreads];


		// get the line search stuff set up...



		SimpleSender continueLineSearchSender = new SimpleSender(nextMachine, NNHelperMethods.CONTINUE_SEARCH_PORT,
				magicSleepTime, magicBadConnectionRetries);
		continueLineSearchSender.establishConnection();

		SimpleSender stepWeightsBiasesSender = new SimpleSender(nextMachine, NNHelperMethods.LINE_SEARCH_WB_PORT,
				magicSleepTime, magicBadConnectionRetries);
		stepWeightsBiasesSender.establishConnection();

		SimpleSender stepErrorSender = new SimpleSender(nextMachine, NNHelperMethods.LINE_SEARCH_ERROR_PORT,
				magicSleepTime, magicBadConnectionRetries);
		stepErrorSender.establishConnection();


		// actually get them and pass them on
		boolean withinTolerance = false;

		boolean weDoNotWantAnotherRoundFromTheWorkers = false;


		MultiFormatMatrix[] stepWeightsAndBiasesToUse = new MultiFormatMatrix[2]; // weightsAndBiases;

		double totalErrorThisStep = Double.NaN;
		double totalErrorThisStepSoFar = Double.NaN;


		// initialize the listeners even though we won't need them exactly right here, we'll need them soon enough

		boolean breakDueToNaNs = false;

		double startingMultiplierBoundary = 0.0; // this actually needs to be zero
		double stepSizeToUse = euclideanStepSize;

		while (!withinTolerance) {
			weDoNotWantAnotherRoundFromTheWorkers = false;

			// ask for all the multipliers and resulting parameters
			stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(startingMultiplierBoundary, stepSizeToUse,
					weightsAndBiases[0], weightsAndBiases[1],
					normalizedDirection);

			// unpack the goodies
			stepParameters = (double[]) stepWeightsBiasesPairsParameters[1];

			// do everything else...
			stepErrorsTotal = new double[stepParameters.length];

			for (int stepIndex = 0; stepIndex < stepParameters.length; stepIndex++) {
				totalErrorThisStepSoFar = 0.0; // initialize this up...

				stepWeightsAndBiasesToUse[0] = ((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[0][stepIndex];
				stepWeightsAndBiasesToUse[1] = ((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[1][stepIndex];

				continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromTheWorkers);
				stepWeightsBiasesSender.sendObject(stepWeightsAndBiasesToUse);

//				System.out.println("\t\tsP[" + stepIndex + "] = " + stepParameters[stepIndex]);
				// determine the errors associated with this step
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					lineSearchErrorRunnerArray[threadIndex] = new ErrorRunnerWeighted(threadIndex,lineSearchResultsDrop,
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

				// ship out this beginning value
				totalErrorThisStepSoFar += totalErrorThisStep;
				stepErrorSender.sendObject(totalErrorThisStepSoFar);

				// wait for it to go around the ring and pick up the result...
				stepErrorsTotal[stepIndex] = ((Double)stepErrorListener.pullObject()).doubleValue();

				if (Double.isNaN(stepErrorsTotal[stepIndex])) {
					breakDueToNaNs = true;
					break;
				}

				// check if we are going uphill. but, make sure we have stuff to compare to...
				if (stepIndex > 1) {


					if (stepErrorsTotal[stepIndex] > stepErrorsTotal[stepIndex - 1]) {
						// reset step size & bounds; breakout; and restart
						double[] quadResults = NNHelperMethods.quadraticSearchHelper(stepParameters, stepErrorsTotal, stepIndex, fractionAboveMinGuess);

						startingMultiplierBoundary = quadResults[0];
						stepSizeToUse = quadResults[1];
						errorRange = quadResults[2];
						finalMultiplier = quadResults[3];

//						System.out.println("quad sectioning: sMB = " + startingMultiplierBoundary + "; eR = " +
//								errorRange + "; sSTU = " + stepSizeToUse + "; fM = " + finalMultiplier);

						if (errorRange < stoppingTolerance) {
							withinTolerance = true;
//							System.out.println("    == hit stopping tolerance, moving on ==");
						}
						break;
					} // end of if going uphill

					if (stepIndex == stepParameters.length - 1) {
//						for (int aStepIndex = 0; aStepIndex < stepParameters.length; aStepIndex++) {
//							System.out.println(aStepIndex + " -> " + stepParameters[aStepIndex] + "\t" + stepErrorsTotal[aStepIndex]);
//						}

						startingMultiplierBoundary = stepParameters[0];
						stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
//						System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
					}


				} // end of if stepIndex > 2

			} // end of for (step = 1:n)

			if (breakDueToNaNs) {
				System.out.println("cont LS encountered a NaN on search; returning original parameters");
				// shouldn't have to send the true flag, we'll do that outside of the while loop...
				break;
			} // end of breakDueToNaNs


		} // end of WHILE tolerance not met

		// tell the world that we're done...
		weDoNotWantAnotherRoundFromTheWorkers = true;

		continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromTheWorkers);



		if (breakDueToNaNs) {
			// something bad happened, use the old ones
			// since we are not passing these out, just keep the old ones unchanged ...
			// finalPair[0] = weightsAndBiases[0];
			// finalPair[1] = weightsAndBiases[1];

			if (doDebugStatements) {
				debugOut("LS beginner, breaking due to NaNs... sending the flag",sequenceNumber);
			}
			// need to tell the world we're done...
		} else {
			// should be fine, use the new ones
			parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);

			weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);

			if (doDebugStatements) {
				debugOut("LS beginner, breaking due to fabulosity... sleeping",sequenceNumber);
				Thread.sleep(1600);
			}

		}


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


		if (doDebugStatements) {
			debugOut("sleeping before moving on to epochs",sequenceNumber);
			Thread.sleep(1500);
		}
		SimpleSender continueEpochSender = new SimpleSender(nextMachine, NNHelperMethods.CONTINUE_EPOCH_PORT,
				magicSleepTime, magicBadConnectionRetries);
		continueEpochSender.establishConnection();

//		SimpleSender doValidationSender = new SimpleSender(nextMachine, NNHelperMethods.DO_VALIDATION_PORT,
//				magicSleepTime, magicBadConnectionRetries);
//		doValidationSender.establishConnection();



		// reset this so i can use it to bail later...
		breakDueToNaNs = false;




		for (int epochIndex = 1; epochIndex < nEpochs; epochIndex++) {
			if (epochIndex == 1 || epochIndex % checkNumber == 0) {
				System.out.println("-- epoch #" + epochIndex + " " + new Date() + " --");
			}
			
			if (breakDueToNaNs) {
				System.out.println("========= trying to bail early due to NaNs... ===========");
				break;
			}
			trainingErrorTotal      = 0.0;
			validationErrorTotal    = 0.0;
			trainingNCorrectTotal   = 0;
			validationNCorrectTotal = 0;
			newGradient = new MultiFormatMatrix(1, new long[] {(weightsAndBiases[0].getDimensions()[0] + weightsAndBiases[1].getDimensions()[0] - nXs), 1});

			// tell all the others that we are still doing something...
			continueEpochSender.sendObject(true);

			// tell all the others whether to bother with the validation stuff
//			if (epochIndex % checkNumber == 0) {
//				doValidationSender.sendObject(true);
//			} else {
//				doValidationSender.sendObject(false);
//			}

			// send out the latest set of weights and biases...
			weightsBiasesSender.sendObject(weightsAndBiases);

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				trainingGradientRunnerArray[threadIndex] = new GradientRunnerWeighted(threadIndex,trainingResultsDrop,
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

//			if (epochIndex % checkNumber == 0) {
				if (true) {

				if (doDebugStatements) {
					debugOut("inside validation block (" + epochIndex + "/" + checkNumber + ")",sequenceNumber);
				}

//				System.out.println("Beginner Validation inner");

				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

					validationErrorRunnerArray[threadIndex] = new ErrorRunnerWeighted(threadIndex,validationResultsDrop,
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

//				System.out.println("sending to: " + nextMachine + "\tve=" + validationErrorSoFar + "\tvn=" + validationNCorrectTotalSoFar + "\t" + validationErrorTotal + "/" + validationNCorrectTotal);

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

			// this may be wrong; we should be seeding with only one "newGradient..."
//			gradientSoFar = MatrixOperations.addMatrices(gradientSoFar, newGradient);
			gradientSoFar = newGradient;

			// pass them along to the next machine
//			System.out.println("sending to: " + nextMachine + "\tte=" + trainingErrorSoFar + "\ttn=" + trainingNCorrectTotalSoFar + "\t" + trainingErrorTotal + "/" + trainingNCorrectTotal);

			trainingErrorSender.sendObject(trainingErrorTotal);
			trainingNCorrectSender.sendObject(trainingNCorrectTotal);

			gradientSender.sendObject(gradientSoFar);

			// wait for everything to come back around...
			trainingErrorSoFar         = ((Double)trainingErrorListener.pullObject()).doubleValue();
			trainingNCorrectTotalSoFar = ((Double)trainingNCorrectListener.pullObject()).doubleValue();

			newGradient = (MultiFormatMatrix)gradientListener.pullObject();



			bfgsDirectionAndBigG = NNHelperMethods.findBFGSDirection(
					oldWeightsAndBiases[0], oldWeightsAndBiases[1],
					oldGradient,	bfgsDirectionAndBigG[1],
					weightsAndBiases[0], weightsAndBiases[1],
					newGradient);

//			reallyOldWeightsAndBiases[0] = MatrixOperations.newCopy(oldWeightsAndBiases[0]);
//			reallyOldWeightsAndBiases[1] = MatrixOperations.newCopy(oldWeightsAndBiases[1]);

			oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
			oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);

//			reallyOldGradient = MatrixOperations.newCopy(oldGradient);
			oldGradient = MatrixOperations.newCopy(newGradient);

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
						bfgsDirectionAndBigG[1].getDimensions()[0]);
				summaryOut.flush();

				summaryOut.close();
				summaryStream = new FileOutputStream(fileToWrite, true);
				summaryOut = new PrintWriter(summaryStream);


				MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(newGradient,gradientBaseName + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[0],gradientBaseName + "_bfgs_" + epochIndex, delimiter);
//				MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[1],gradientBaseName + "_bfgsG_" + epochIndex, delimiter);
			}



			///////////////////////////////////////////
			// begin UGLY line search implementation //
			///////////////////////////////////////////

			// normalize the direction of the gradient...
			normalizedDirection = MatrixOperations.normalizeColumn(bfgsDirectionAndBigG[0]);

			// actually get them and pass them on
			withinTolerance = false;
			weDoNotWantAnotherRoundFromTheWorkers = false;
			breakDueToNaNs = false;
			startingMultiplierBoundary = 0.0; // this actually needs to be zero
			stepSizeToUse = euclideanStepSize;

			while (!withinTolerance) {
				weDoNotWantAnotherRoundFromTheWorkers = false;

				// ask for all the multipliers and resulting parameters
				stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(startingMultiplierBoundary, stepSizeToUse,
						weightsAndBiases[0], weightsAndBiases[1],
						normalizedDirection);

				// unpack the goodies
				stepParameters = (double[]) stepWeightsBiasesPairsParameters[1];

				// do everything else...
				stepErrorsTotal = new double[stepParameters.length];

				for (int stepIndex = 0; stepIndex < stepParameters.length; stepIndex++) {
					totalErrorThisStepSoFar = 0.0; // initialize this up...

					stepWeightsAndBiasesToUse[0] = ((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[0][stepIndex];
					stepWeightsAndBiasesToUse[1] = ((MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[1][stepIndex];

					continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromTheWorkers);
					stepWeightsBiasesSender.sendObject(stepWeightsAndBiasesToUse);

//					System.out.println("\t\tsP[" + stepIndex + "] = " + stepParameters[stepIndex]);
					// determine the errors associated with this step
					for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
						lineSearchErrorRunnerArray[threadIndex] = new ErrorRunnerWeighted(threadIndex,lineSearchResultsDrop,
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

					// ship out this beginning value
					totalErrorThisStepSoFar += totalErrorThisStep;
					stepErrorSender.sendObject(totalErrorThisStepSoFar);

					// wait for it to go around the ring and pick up the result...
					stepErrorsTotal[stepIndex] = ((Double)stepErrorListener.pullObject()).doubleValue();

					if (Double.isNaN(stepErrorsTotal[stepIndex])) {
						breakDueToNaNs = true;
						break;
					}

					// check if we are going uphill. but, make sure we have stuff to compare to...
					if (stepIndex > 1) {


						if (stepErrorsTotal[stepIndex] > stepErrorsTotal[stepIndex - 1]) {
							// reset step size & bounds; breakout; and restart
							double[] quadResults = NNHelperMethods.quadraticSearchHelper(stepParameters, stepErrorsTotal, stepIndex, fractionAboveMinGuess);

							startingMultiplierBoundary = quadResults[0];
							stepSizeToUse = quadResults[1];
							errorRange = quadResults[2];
							finalMultiplier = quadResults[3];

//							System.out.println("quad sectioning: sMB = " + startingMultiplierBoundary + "; eR = " +
//									errorRange + "; sSTU = " + stepSizeToUse + "; fM = " + finalMultiplier);

							if (errorRange < stoppingTolerance) {
								withinTolerance = true;
//								System.out.println("    == hit stopping tolerance, moving on ==");
							}
							break;
						} // end of if going uphill

						if (stepIndex == stepParameters.length - 1) {
//							for (int aStepIndex = 0; aStepIndex < stepParameters.length; aStepIndex++) {
//								System.out.println(aStepIndex + " -> " + stepParameters[aStepIndex] + "\t" + stepErrorsTotal[aStepIndex]);
//							}

							startingMultiplierBoundary = stepParameters[0];
							stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
//							System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
						}


					} // end of if stepIndex > 2

				} // end of for (step = 1:n)

				if (breakDueToNaNs) {
					System.out.println("cont LS encountered a NaN on search; returning original parameters");
					// shouldn't have to send the true flag, we'll do that outside of the while loop...
					break;
				} // end of breakDueToNaNs


			} // end of WHILE tolerance not met

			// tell the world that we're done...
			weDoNotWantAnotherRoundFromTheWorkers = true;

			continueLineSearchSender.sendObject(weDoNotWantAnotherRoundFromTheWorkers);



			if (breakDueToNaNs) {
				// something bad happened, use the old ones
				// since we are not passing these out, just keep the old ones unchanged ...
				// finalPair[0] = weightsAndBiases[0];
				// finalPair[1] = weightsAndBiases[1];

				if (doDebugStatements) {
					debugOut("LS beginner, breaking due to NaNs... sending the flag",sequenceNumber);
				}
				// need to tell the world we're done...
			} else {
				// should be fine, use the new ones
				
				// but, i'm sick of positive finalMultipliers. so i'm gonna check for positives.
				// if it's positive, i'm going to reset the bigG to an identity...
				if (finalMultiplier > 0.0) {
					System.out.println("\t\t!!! finalMultiplier is positive (" + finalMultiplier + ") !!!");
//					System.out.println("\t\t!!! resetting big G to identity and keeping the same parameters... !!!");
//
//					bfgsDirectionAndBigG = NNHelperMethods.findBFGSDirection(
//							oldWeightsAndBiases[0], oldWeightsAndBiases[1],
//							oldGradient,	bfgsDirectionAndBigG[1],
//							weightsAndBiases[0], weightsAndBiases[1],
//							newGradient);
//
//					oldWeightsAndBiases[0] = MatrixOperations.newCopy(reallyOldWeightsAndBiases[0]);
//					oldWeightsAndBiases[1] = MatrixOperations.newCopy(reallyOldWeightsAndBiases[1]);
//					oldGradient = MatrixOperations.newCopy(reallyOldGradient);
//					
//					finalMultiplier = 0.0; // ok, we've wasted this line search, but oh well
//					
//					bfgsDirectionAndBigG[1] = MatrixOperations.newCopy(bigGSeed);
					
					
				}
				parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);
				weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);

				if (doDebugStatements) {
					debugOut("LS beginner, breaking due to fabulosity... sleeping",sequenceNumber);
					Thread.sleep(1600);
				}

			}



			///////////////////////////////////////////
			//  end UGLY line search implementation  //
			///////////////////////////////////////////







			System.gc();
		} // end for epoch

		// tell all the others that we're done...
		continueEpochSender.sendObject(false);
//		doValidationSender.sendObject(false);
		

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  #" + sequenceNumber + " All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


