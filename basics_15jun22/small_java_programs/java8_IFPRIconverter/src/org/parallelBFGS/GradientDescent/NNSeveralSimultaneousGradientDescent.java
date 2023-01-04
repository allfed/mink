package org.parallelBFGS.GradientDescent;

import org.parallelBFGS.*;

import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.R2Useful.*;

public class NNSeveralSimultaneousGradientDescent {



	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length != 2) {
			System.out.println("Usage: classname initFile dataFile");
		}

		String initializationFile = commandLineOptions[0];
		String dataFile = commandLineOptions[1];

		///////////////////
		// magic numbers //
		///////////////////


		int nThreads = 2;

		////////////////////////////		

		Object[] initValues = NNHelperMethods.readInitializationFile(initializationFile);



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


		// load the data in
		MultiFormatFloat allDataMatrix = new MultiFormatFloat(dataFile);
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
			System.out.println("-- creating data block for thread #" + threadIndex);
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

		System.out.println("doing T/V split at " + new Date());
		MultiFormatFloat[][] trainingValidationPairs = new MultiFormatFloat[nThreads][2];

		MultiFormatFloat[][] trainingXT   = new MultiFormatFloat[nThreads][2];
		MultiFormatFloat[][] validationXT = new MultiFormatFloat[nThreads][2];

		long[] nTraining = new long[nThreads];
		long[] nValidation = new long[nThreads];

		long nTrainingTotal = 0;   // this actually needs to be zero
		long nValidationTotal = 0; // this actually needs to be zero

		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			System.out.println("-- T/V X/T splits for thread #" + threadIndex);

			trainingValidationPairs[threadIndex] = MatrixOperations.randomizeRows2dMff(allDataBlocks[threadIndex], aRandomSeed, fractionInTraining,1);

			nTraining[threadIndex]   = trainingValidationPairs[threadIndex][0].getDimensions()[0];
			nValidation[threadIndex] = trainingValidationPairs[threadIndex][1].getDimensions()[0];

			// figure out the total number of training and validation examples
			nTrainingTotal   += nTraining[threadIndex];
			nValidationTotal += nValidation[threadIndex];

			trainingXT[threadIndex]   = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][0]);
			validationXT[threadIndex] = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][1]);
		}







		System.out.println("loading parameters...");

		MultiFormatMatrix weightSeed = MatrixOperations.read2DMFMfromText(weightSeedBaseName);
		MultiFormatMatrix biasSeed   = MatrixOperations.read2DMFMfromText(biasSeedBaseName);





		// set up the output for the summary

		File fileToWrite = new File(reportBaseName);
		FileOutputStream summaryStream = new FileOutputStream(fileToWrite, true);
		PrintWriter summaryOut = new PrintWriter(summaryStream);





		// set up the training loop
		DeadDropObjectArray trainingResultsDrop   = null;
		GradientRunner[] trainingGradientRunnerArray = new GradientRunner[nThreads];

		DeadDropObjectArray validationResultsDrop = null;
		ErrorRunner[]    validationErrorRunnerArray  = new ErrorRunner[   nThreads];


		double[] trainingError      = new double[nThreads];
		double[] trainingNCorrect   = new double[nThreads];
		Object[] gradientThreadBundles = new Object[nThreads];
		Object[] validationErrorThreadBundles = new Object[nThreads];


		MultiFormatMatrix[] weightsAndBiases    = new MultiFormatMatrix[] {weightSeed, biasSeed};
		MultiFormatMatrix newGradient = null;

		// seed the bigG with an identity matrix; nParameters = nWeights + nBiases - nXs
		// (there are all those placeholders for the explanatory variables in the biases definition)
		int nParameters = (int)(weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - nXs);


		System.out.println("starting training at " + new Date());

		// initialize some stuff
		double trainingErrorTotal      = 0.0;
		double validationErrorTotal    = 0.0;
		double   trainingNCorrectTotal   = 0;
		double   validationNCorrectTotal = 0;

		// initialize the drop points and runner threads
		trainingResultsDrop   = new DeadDropObjectArray(nThreads);
		validationResultsDrop = new DeadDropObjectArray(nThreads);

		for (int epochIndex = 0; epochIndex < nEpochs; epochIndex++) {
			if (epochIndex % checkNumber == 0) {
				System.out.println("-- epoch #" + epochIndex + " --");
			}

			trainingErrorTotal      = 0.0;
			validationErrorTotal    = 0.0;
			trainingNCorrectTotal   = 0;
			validationNCorrectTotal = 0;

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				trainingGradientRunnerArray[threadIndex] = new GradientRunner(threadIndex,trainingResultsDrop,
						trainingXT[threadIndex][0], trainingXT[threadIndex][1],
						weightsAndBiases[0], weightsAndBiases[1],
						nHidden, nOptionsMinusOne);

				new Thread(trainingGradientRunnerArray[threadIndex]).start();
			}

			// accumulate the gradient and training summaries
			gradientThreadBundles = trainingResultsDrop.take();

			newGradient = new MultiFormatMatrix(1, new long[] {nParameters, 1});
			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
				// accumulate the gradient
				newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)((Object[])(gradientThreadBundles[threadIndex]))[2]); // seed the old gradient
				trainingError[threadIndex]    = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[0]).doubleValue();
				trainingNCorrect[threadIndex] = ((Double)((Object[])(gradientThreadBundles[threadIndex]))[1]).doubleValue();

				trainingErrorTotal   += trainingError[threadIndex];
				trainingNCorrectTotal   += trainingNCorrect[threadIndex];

			}

			// update parameters


			if (epochIndex % checkNumber == 0) {

				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

					validationErrorRunnerArray[threadIndex] = new ErrorRunner(threadIndex,validationResultsDrop,
							validationXT[threadIndex][0], validationXT[threadIndex][1],
							weightsAndBiases[0], weightsAndBiases[1],
							nHidden, nOptionsMinusOne);

					new Thread(validationErrorRunnerArray[threadIndex]).start();
				}

				// accumulate the gradient and training summaries
				validationErrorThreadBundles = validationResultsDrop.take();

				validationErrorTotal = 0;
				validationNCorrectTotal = 0;

				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					// record progress
					validationErrorTotal    += ((double[])(validationErrorThreadBundles[threadIndex]))[0];
					validationNCorrectTotal += ((double[])(validationErrorThreadBundles[threadIndex]))[1];

				}

				summaryOut.println(epochIndex + "\t" + trainingErrorTotal + "\t" + validationErrorTotal + "\t" +
						Math.exp(-trainingErrorTotal/nTrainingTotal) + "\t" + Math.exp(-validationErrorTotal/nValidationTotal) + "\t" +
						trainingNCorrectTotal/nTrainingTotal + "\t" + validationNCorrectTotal/nValidationTotal + "\t" +
						nTrainingTotal + "\t" +	nValidationTotal + "\t" + nHidden + "\t" + nParameters);
				summaryOut.flush();


				MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(newGradient,gradientBaseName + epochIndex, delimiter);
			}

			// update weights and biases

			weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],
					MatrixOperations.multiplyByConstant(learningRate,newGradient));

			System.gc();
		} // end for epoch

		summaryOut.flush();
		summaryOut.close();

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


