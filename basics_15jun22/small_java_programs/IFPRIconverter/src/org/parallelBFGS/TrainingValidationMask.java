package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.PrintWriter;
import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.PrintWriter;
//import java.util.Date;


public class TrainingValidationMask {


	public static void main(String commandLineOptions[]) throws Exception {

		if (commandLineOptions.length != 6) {
			System.out.println("Usage: classname initFile originalFullGeogFile PathOfWeights formatIndex nThreads outputFile");
		}

		String initFile             = commandLineOptions[0];
		String originalFullGeogFile = commandLineOptions[1];
		String pathOfWeights        = commandLineOptions[2];
		
//		int formatIndex    = Integer.parseInt(commandLineOptions[3]);
		int nThreads       = Integer.parseInt(commandLineOptions[4]);

		String outputFile           = commandLineOptions[5];


		///////////////////
		// magic numbers //
		///////////////////

		// pull the magic numbers from the previous machine
		Object[] initValues = NNHelperMethods.readInitializationFile(initFile);


//		int nExplanatoryVariables = ((Integer)initValues[0]).intValue();
//		int nHidden               = ((Integer)initValues[1]).intValue();
//		int nOptionsMinusOne      = ((Integer)initValues[2]).intValue();
//		long nElementsThreshold   = ((   Long)initValues[3]).longValue();
		double fractionInTraining = (( Double)initValues[4]).doubleValue();
//		double learningRate       = (( Double)initValues[5]).doubleValue();
//		String weightSeedBaseName =   (String)initValues[6];
//		String biasSeedBaseName   =   (String)initValues[7];
//		int nEpochs               = ((Integer)initValues[8]).intValue();
//		int checkNumber           = ((Integer)initValues[9]).intValue();
//		String     weightBaseName =   (String)initValues[10];
//		String       biasBaseName =   (String)initValues[11];
//		String   gradientBaseName =   (String)initValues[12];
//		String    hessianBaseName =   (String)initValues[13];
//		String     reportBaseName =   (String)initValues[14];
//		String          delimiter =   (String)initValues[15];
		long          aRandomSeed = ((   Long)initValues[16]).longValue();

		
		/*
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
		 */





		// load the data in...
		System.out.println("loading original data...");
		MultiFormatFloat allOriginalFullDataMatrix = null;
		try {
			allOriginalFullDataMatrix = new MultiFormatFloat(originalFullGeogFile);
		} catch (FileNotFoundException fnfe) {
			allOriginalFullDataMatrix = MatrixOperations.read2DMFFfromText(originalFullGeogFile);
		}

		
		// replicate the balanced blocking...
		System.out.println("doing balanced blocks...");
		MultiFormatFloat[] blockMatrices = MatrixOperations.balancedBlocks(allOriginalFullDataMatrix, pathOfWeights, 1);

		int nBlocks = blockMatrices.length;

		// figure out how many actually ended up in training and validation
		long nRowsInFirst = -5;
		long nRowsInSecond = -5;
		long nTrainingTotal = 0;
		long nValidationTotal = 0;
		for (int blockIndex = 0; blockIndex < nBlocks; blockIndex++) {
			nRowsInFirst  = (long)(fractionInTraining * blockMatrices[blockIndex].getDimensions()[0]);
			nRowsInSecond = blockMatrices[blockIndex].getDimensions()[0] - nRowsInFirst;
			
			nTrainingTotal   += nRowsInFirst;
			nValidationTotal += nRowsInSecond;
		}

		System.out.println("allocating final arrays...");

		MultiFormatFloat trainingTogether   = new MultiFormatFloat(3, new long[] {nTrainingTotal,allOriginalFullDataMatrix.getDimensions()[1]});
		MultiFormatFloat validationTogether = new MultiFormatFloat(3, new long[] {nValidationTotal,allOriginalFullDataMatrix.getDimensions()[1]});

//		MultiFormatFloat allDataMatrix = new MultiFormatFloat(originalFullDataFile);

		////////////////// initializations ////////////////////
		long nRowsTotal = -5;
		long nXs = -6;
		MultiFormatFloat[] allDataBlocks = null;
		long nRowsHere = -7;
		long[] firstRowForThread = null;
		MultiFormatFloat[][] trainingValidationPairs = null;
//		MultiFormatFloat[][] trainingXT   = null;
//		MultiFormatFloat[][] validationXT = null;
		long[] nTraining = null;
		long[] nValidation = null;
//		long nTrainingTotalHere = 0;   // this actually needs to be zero
//		long nValidationTotalHere = 0; // this actually needs to be zero
		
		long trainingStorageFinal = 0; // this actually needs to be zero
		long validationStorageFinal = 0; // this actually needs to be zero
		////////////////// end initializations ////////////////////

		for (int blockIndex = 0; blockIndex < nBlocks; blockIndex++) {
			System.out.println("-- block " + blockIndex + " of " + nBlocks + "...");

			nRowsTotal = blockMatrices[blockIndex].getDimensions()[0];
			nXs        = blockMatrices[blockIndex].getDimensions()[1] - 1;

			// now, we have to make within-machine blocks because we're gonna do multiple threads
			allDataBlocks = new MultiFormatFloat[nThreads];
			nRowsHere = 0;
			firstRowForThread = new long[nThreads + 1];

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

			// now actually do the copying...
			System.out.println("  -- copying first... --");
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
								blockMatrices[blockIndex].getValue(rowIndex + firstRowForThread[threadIndex],colIndex));
					}
				}
			} // end for thread for copying into machine level blocks

			
			// split into training/validation as well as X and targets

			trainingValidationPairs = new MultiFormatFloat[nThreads][2];

//			trainingXT   = new MultiFormatFloat[nThreads][2];
//			validationXT = new MultiFormatFloat[nThreads][2];

			nTraining = new long[nThreads];
			nValidation = new long[nThreads];

			System.out.println("  -- copying second... --");

			for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {

				trainingValidationPairs[threadIndex] = MatrixOperations.randomizeRows2dMff(allDataBlocks[threadIndex], aRandomSeed, fractionInTraining,1);

				nTraining[threadIndex]   = trainingValidationPairs[threadIndex][0].getDimensions()[0];
				nValidation[threadIndex] = trainingValidationPairs[threadIndex][1].getDimensions()[0];

				// copy the training back into the big deal
				for (int rowIndex = 0; rowIndex < nTraining[threadIndex]; rowIndex++) {
					for (int colIndex = 0; colIndex < trainingValidationPairs[threadIndex][0].getDimensions()[1]; colIndex++) {
						trainingTogether.setValue(trainingStorageFinal,colIndex,
								trainingValidationPairs[threadIndex][0].getValue(rowIndex,colIndex));
					}
					trainingStorageFinal++;
				}

				// copy the validation back into the big deal
				for (int rowIndex = 0; rowIndex < nValidation[threadIndex]; rowIndex++) {
					for (int colIndex = 0; colIndex < trainingValidationPairs[threadIndex][1].getDimensions()[1]; colIndex++) {
						validationTogether.setValue(validationStorageFinal,colIndex,
								trainingValidationPairs[threadIndex][1].getValue(rowIndex,colIndex));
					}
					validationStorageFinal++;
				}

//				trainingStorageFinal = 0; // this actually needs to be zero
//				validationStorageFinal = 0; // this actually needs to be zero

//				// figure out the total number of training and validation examples
//				nTrainingTotalHere   += nTraining[threadIndex];
//				nValidationTotalHere += nValidation[threadIndex];
//
//				trainingXT[threadIndex]   = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][0]);
//				validationXT[threadIndex] = MatrixOperations.splitOffLastColumn(trainingValidationPairs[threadIndex][1]);
			}

			
			
		} ////////// end of new block loop /////////////////
		
		

		// write out the goods. row and col need to be integers, the res
		System.out.println("  -- writing output... --");

		Object[] geogInfo = MatrixOperations.readInfoFile(originalFullGeogFile);
		
		String theDelimiter = (String)geogInfo[3];
		
		MatrixOperations.write2DMFFtoTextAsInt(trainingTogether,   outputFile + "_T", theDelimiter);
		MatrixOperations.write2DMFFtoTextAsInt(validationTogether, outputFile + "_V", theDelimiter);



		
		




	} // main

}


