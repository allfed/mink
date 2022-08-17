package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;

import org.R2Useful.*;

import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class NNTwoInOneAttempt {



	public static void main(String commandLineOptions[]) throws Exception {
		
		if (commandLineOptions.length != 2) {
			System.out.println("Usage: classname initFile dataFile");
		}
		
		String initializationFile = commandLineOptions[0];
		String dataFile = commandLineOptions[1];
		
		///////////////////
		// magic numbers //
		///////////////////
		boolean gradientDescent = false;
		boolean lineSearch = true;
		
		double euclideanStepSize = 1.0;
		double stoppingTolerance = 0.00001;
		double fractionAboveMinGuess = 0.1;
		
		int nThreads = 2;
		
		////////////////////////////
		
		
		
		
		if (gradientDescent == lineSearch) {
			gradientDescent = true;
			lineSearch = false;
		}
		
		
		Object[] initValues = NNHelperMethods.readInitializationFile(initializationFile);

//		for (int dIndex = 0; dIndex < initValues.length; dIndex++) {
//			System.out.println("initValues[" + dIndex + "] = [" + initValues[dIndex] + "]");
//		}

		
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
		double[] trainingError      = new double[nThreads];
		double[] trainingNCorrect   = new double[nThreads];
		double[][] validationError  = new double[nThreads][2];
		Object[][] gradientBundles  = new Object[nThreads][3];
		

		MultiFormatMatrix[] weightsAndBiases    = new MultiFormatMatrix[] {weightSeed, biasSeed};
		MultiFormatMatrix[] oldWeightsAndBiases = new MultiFormatMatrix[] {weightSeed, biasSeed};
		
		MultiFormatMatrix oldGradient = null;
		MultiFormatMatrix newGradient = null;
		
		// seed the bigG with an identity matrix; nParameters = nWeights + nBiases - nXs
		// (there are all those placeholders for the explanatory variables in the biases definition)
		MultiFormatMatrix bigGSeed = MatrixOperations.identityMFM(
				(weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - nXs), 1);

		MultiFormatMatrix[] bfgsDirectionAndBigG = new MultiFormatMatrix[] {null,bigGSeed};

		
		
		
		
		System.out.println("starting training at " + new Date());

		// seed the process by doing a linesearch up front...
		
		// initialize some stuff
		double trainingErrorTotal      = 0.0;
		double validationErrorTotal    = 0.0;
		double   trainingNCorrectTotal   = 0;
		double   validationNCorrectTotal = 0;
		oldGradient = new MultiFormatMatrix(1, new long[] {(weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - nXs), 1});
		newGradient = new MultiFormatMatrix(1, new long[] {(weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - nXs), 1});
		for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
			// do the training bit
			gradientBundles[threadIndex] = NNHelperMethods.findSingleHiddenErrorGradient(trainingXT[threadIndex][0], trainingXT[threadIndex][1],
					weightsAndBiases[0], weightsAndBiases[1], nHidden, nOptionsMinusOne);

			// accumulate the gradient
			newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)gradientBundles[threadIndex][2]); // seed the old gradient


			validationError[threadIndex] = NNHelperMethods.findErrorSingleHidden(validationXT[threadIndex][0], validationXT[threadIndex][1],
					weightsAndBiases[0], weightsAndBiases[1], nHidden, nOptionsMinusOne);

			// record progress
			trainingError[threadIndex]    = ((Double)gradientBundles[threadIndex][0]).doubleValue();
			trainingNCorrect[threadIndex] = ((Double)gradientBundles[threadIndex][1]).doubleValue();
			
			trainingErrorTotal   += trainingError[threadIndex];
			validationErrorTotal += validationError[threadIndex][0];
			
			trainingNCorrectTotal   += trainingNCorrect[threadIndex];
			validationNCorrectTotal += validationError[threadIndex][1];

		}

		// we now have everything to do the summary report...
		// Beware the MAGIC NUMBER!!! epochIndex = 0
		summaryOut.println(0 + "\t" + trainingErrorTotal + "\t" + validationErrorTotal + "\t" +
				Math.exp(-trainingErrorTotal/nTrainingTotal) + "\t" + Math.exp(-validationErrorTotal/nValidationTotal) + "\t" +
				trainingNCorrectTotal/nTrainingTotal + "\t" + validationNCorrectTotal/nValidationTotal + "\t" +
				nTrainingTotal + "\t" +	nValidationTotal + "\t" + nHidden + "\t" + bfgsDirectionAndBigG[1].getDimensions()[0]);
		summaryOut.flush();

		
		
		oldGradient = MatrixOperations.newCopy(newGradient);
		
		
		
		
		
		///////////////////////////////////////////
		// begin UGLY line search implementation //
		///////////////////////////////////////////
		
//		weightsAndBiases = NNHelperMethods.categoricalLineSearch(trainingXT[threadToUse][0], trainingXT[threadToUse][1],
//				nHidden, nOptionsMinusOne, weightsAndBiases[0], weightsAndBiases[1], (MultiFormatMatrix)gradientBundles[threadToUse][2],
//				euclideanStepSize, stoppingTolerance, fractionAboveMinGuess);
		


		// initializations
		
		double errorRange = Double.NaN;
		double errorMax = -1;
		double errorMin = -2;
		
		double a = -1, b = -2, c = -3, A =-4, B = -5;
		double x_1, x_2, x_3, y_1, y_2, y_3;
		double minGuess = Double.NaN;
		double lowerEdgeHeight = -7;
		double newEdgeTargetHeight = -8;
		double newXLower = -8;
		double newXUpper = -8;
		double finalMultiplier = Double.NaN;

		double[] stepParameters = null;
		double[][] stepErrors     = null; // let's go [thread][step]
		double[] stepErrorsTotal = null;

		Object[] stepWeightsBiasesPairsParameters = null;

		MultiFormatMatrix parameterStep = null; 
//		MultiFormatMatrix[] finalPair = new MultiFormatMatrix[2];

		// section until we are within some tolerance

		// normalize the direction of the gradient...
		MultiFormatMatrix normalizedDirection = MatrixOperations.normalizeColumn(newGradient);

		boolean withinTolerance = false;
		boolean breakDueToNaNs = false;
		
		double startingMultiplierBoundary = 0.0; // this actually needs to be zero
		double stepSizeToUse = euclideanStepSize;
		int nSearches = 0;
		while (!withinTolerance) {
			nSearches++;
			
			// ask for all the multipliers and resulting parameters
			stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(startingMultiplierBoundary, stepSizeToUse,
					weightsAndBiases[0], weightsAndBiases[1],
					normalizedDirection);
			
			// unpack the goodies
			stepParameters = (double[]) stepWeightsBiasesPairsParameters[1];
			stepErrors = new double[nThreads][stepParameters.length];
			stepErrorsTotal = new double[stepParameters.length];

			// do everything else...
			stepErrorsTotal = new double[stepParameters.length];

			for (int stepIndex = 0; stepIndex < stepParameters.length; stepIndex++) {
				// determine the errors associated with each step
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					System.out.println("a thread = " + threadIndex + " step = " + stepIndex);
					stepErrors[threadIndex][stepIndex] = NNHelperMethods.findErrorSingleHidden(
							trainingXT[threadIndex][0], trainingXT[threadIndex][1],
							( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[0][stepIndex],
							( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[1][stepIndex],
							nHidden, nOptionsMinusOne
							)[0];
					if (Double.isNaN(stepErrors[threadIndex][stepIndex])) {
						breakDueToNaNs = true;
					}
				} // end of threads finding their errors...
				
				// we have to put this outside for the moment.
				if (breakDueToNaNs) {
					break;
				}
				
				// accumulate them to a single stepError thing to work with
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					stepErrorsTotal[stepIndex] += stepErrors[threadIndex][stepIndex];
				}
				

//				System.out.println("[" + stepIndex + "] " + stepParameters[stepIndex] + " -> " + stepErrors[stepIndex]);

				// check if we are going uphill. but, make sure we have stuff to compare to...
				if (stepIndex > 1) {
					if (stepErrorsTotal[stepIndex] > stepErrorsTotal[stepIndex - 1]) {
						// reset step size & bounds; breakout; and restart

						// this will be before the minimum because either we went down and came back up
						// or, we are only on the third point, so we should start at the beginning again, and just look more finely
						errorMax = Math.max(stepErrorsTotal[stepIndex], Math.max(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
						errorMin = Math.min(stepErrorsTotal[stepIndex], Math.min(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
						errorRange = errorMax - errorMin;

						// pure sectioning
						// startingMultiplierBoundary = stepParameters[stepIndex - 2];
						// stepSizeToUse = Math.abs((stepParameters[stepIndex] - stepParameters[stepIndex - 2]) / (magicNumberOfSteps - 1));
						
						// try a quadratic approximation approach
						// first, we define a parabola going through the final three points; this is ugly, so you might want to
						// derive it yourself in case you're wondering. it's not terribly hard...
						// y = ax^2 + bx + c
						x_1 = stepParameters[stepIndex - 2];
						x_2 = stepParameters[stepIndex - 1];
						x_3 = stepParameters[stepIndex];
						y_1 = stepErrorsTotal[stepIndex - 2];
						y_2 = stepErrorsTotal[stepIndex - 1];
						y_3 = stepErrorsTotal[stepIndex];
						
						A = (x_2 - x_1)/(x_2*x_2 - x_1*x_1) ;
						B = (y_2 - y_1)/(x_2*x_2 - x_1*x_1);
						
						b = (y_3 - y_1 - B*(x_3*x_3 - x_1*x_1)) / ((x_3 - x_1) - A*(x_3*x_3 - x_1*x_1));
						a = -b * A + B ;
						c = y_1 - a*x_1*x_1 - b*x_1;
						
						
						// now, we can figure where the bottom of the parabola is... -b/2a
						// but, instead of going straight to the bottom, let's go a little bit up from
						// the bottom on either side and use those as new edges for our search
						// minGuess the height at the bottom of the parabola, i.e., y(-b/2a)
						minGuess = - b*b / (4*a) + c;
//						System.out.println("a=" + a + " b=" + b + " c=" + c + " ; minGuess=" + minGuess + " at " + (-b/(2*a)));
						
						// now, let us define "a little bit up" as a certain fraction of the distance
						// between the bottom and the lower of our two edges... now, we know that
						// points 1 and 3 are the edges, so we look for the lower of those two
						lowerEdgeHeight = Math.min(y_1,y_3);
						
						newEdgeTargetHeight = minGuess + fractionAboveMinGuess * (lowerEdgeHeight - minGuess);

						// now we determine what x's result in that height.
						// the ole quadratic formula...
						newXUpper = (-b - Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
						newXLower = (-b + Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);

						startingMultiplierBoundary = (newXLower);
						stepSizeToUse = Math.abs((newXUpper - newXLower) / (stepParameters.length - 1));
						
//						System.out.println("lEH=" + lowerEdgeHeight + " nETH=" + newEdgeTargetHeight + " nXL=" + newXLower + " nxU=" + newXUpper);

						finalMultiplier = -b / (2*a);

						System.out.println("--> restarting: errorRange = " + errorRange + " ; guess = " + finalMultiplier + " ; new size = " + stepSizeToUse);
						if (errorRange < stoppingTolerance) {
							withinTolerance = true;
							finalMultiplier = -b / (2*a);
							System.out.println("    == hit stopping tolerance, moving on after " + nSearches + " searches ==");
						}
						break;
					} // end of if going uphill
					if (stepIndex == stepParameters.length - 1) {
						startingMultiplierBoundary = stepParameters[0];
						stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
						System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
					}
				} // end of if stepIndex > 2
			} // end of for stepIndex

			
			if (breakDueToNaNs) {
				System.out.println("cont LS encountered a NaN on search " + nSearches + "; returning original parameters");
				break;
			}
			
			
		} // end of while tolerance not met

		
		if (breakDueToNaNs) {
			// something bad happened, use the old ones
			
			// since we are not passing these out, just keep the old ones...
//			finalPair[0] = weightsAndBiases[0];
//			finalPair[1] = weightsAndBiases[1];
		} else {
			// should be fine, use the new ones
			parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);
			weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);
		}

		
		
		
		
		
		
		///////////////////////////////////////////
		//  end UGLY line search implementation  //
		///////////////////////////////////////////
		
		
		
		
		
		
		
		
		
		// write down all the parameters. we'll keep these together
		
		// Beware the MAGIC NUMBER!!! epochIndex = 0
		MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + 0, delimiter);
		MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + 0, delimiter);
		MatrixOperations.write2DMFMtoText(oldGradient,gradientBaseName + 0, delimiter);
		// there is no BFGS direction to begin with, we merely use the gradient at the seed.
		MatrixOperations.write2DMFMtoText(oldGradient,gradientBaseName + "_bfgs_" + 0, delimiter);

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//			int threadToUse = 0;
			for (int epochIndex = 1; epochIndex < nEpochs; epochIndex++) {
				System.out.println("-- epoch #" + epochIndex + " --");
				trainingErrorTotal      = 0.0;
				validationErrorTotal    = 0.0;
				trainingNCorrectTotal   = 0;
				validationNCorrectTotal = 0;
				newGradient = new MultiFormatMatrix(1, new long[] {(weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - nXs), 1});
				for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
					System.out.println("\t training " + threadIndex);
					// do the training bit
					gradientBundles[threadIndex] = NNHelperMethods.findSingleHiddenErrorGradient(trainingXT[threadIndex][0], trainingXT[threadIndex][1],
							weightsAndBiases[0], weightsAndBiases[1], nHidden, nOptionsMinusOne);

					// accumulate the gradient
					newGradient = MatrixOperations.addMatrices(newGradient,(MultiFormatMatrix)gradientBundles[threadIndex][2]); // seed the old gradient
				}

				// determine new weights


				bfgsDirectionAndBigG = NNHelperMethods.findBFGSDirection(
						oldWeightsAndBiases[0], oldWeightsAndBiases[1],
						oldGradient,	bfgsDirectionAndBigG[1],
						weightsAndBiases[0], weightsAndBiases[1],
						newGradient);

				oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
				oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);

				oldGradient = MatrixOperations.newCopy(newGradient);
				
				// line search...
//				weightsAndBiases = NNHelperMethods.categoricalLineSearch(trainingXT[threadToUse][0], trainingXT[threadToUse][1],
//						nHidden, nOptionsMinusOne, weightsAndBiases[0], weightsAndBiases[1], bfgsDirectionAndBigG[0],
//						euclideanStepSize, stoppingTolerance, fractionAboveMinGuess);


				
				///////////////////////////////////////////
				// begin UGLY line search implementation //
				///////////////////////////////////////////
				
//				weightsAndBiases = NNHelperMethods.categoricalLineSearch(trainingXT[threadToUse][0], trainingXT[threadToUse][1],
//						nHidden, nOptionsMinusOne, weightsAndBiases[0], weightsAndBiases[1], (MultiFormatMatrix)gradientBundles[threadToUse][2],
//						euclideanStepSize, stoppingTolerance, fractionAboveMinGuess);
				



				// normalize the direction of the gradient...
				normalizedDirection = MatrixOperations.normalizeColumn(newGradient);

				// initializations
				startingMultiplierBoundary = 0.0; // this actually needs to be zero
				stepSizeToUse = euclideanStepSize;
				withinTolerance = false;
				breakDueToNaNs = false;
				nSearches = 0;
				while (!withinTolerance) {
					nSearches++;
					
					// ask for all the multipliers and resulting parameters
					stepWeightsBiasesPairsParameters = NNHelperMethods.makeSteps(startingMultiplierBoundary, stepSizeToUse,
							weightsAndBiases[0], weightsAndBiases[1],
							normalizedDirection);
					
					// unpack the goodies
					stepParameters = (double[]) stepWeightsBiasesPairsParameters[1];
					stepErrors = new double[nThreads][stepParameters.length];
					stepErrorsTotal = new double[stepParameters.length];

					// do everything else...
					stepErrorsTotal = new double[stepParameters.length];

					for (int stepIndex = 0; stepIndex < stepParameters.length; stepIndex++) {
						// determine the errors associated with each step
						for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
							System.out.println("b thread = " + threadIndex + " step = " + stepIndex);
							stepErrors[threadIndex][stepIndex] = NNHelperMethods.findErrorSingleHidden(
									trainingXT[threadIndex][0], trainingXT[threadIndex][1],
									( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[0][stepIndex],
									( (MultiFormatMatrix[][])stepWeightsBiasesPairsParameters[0] )[1][stepIndex],
									nHidden, nOptionsMinusOne
									)[0];
							if (Double.isNaN(stepErrors[threadIndex][stepIndex])) {
								breakDueToNaNs = true;
							}
						} // end of threads finding their errors...
						
						// we have to put this outside for the moment.
						if (breakDueToNaNs) {
							break;
						}
						
						// accumulate them to a single stepError thing to work with
						for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
							stepErrorsTotal[stepIndex] += stepErrors[threadIndex][stepIndex];
						}
						

//						System.out.println("[" + stepIndex + "] " + stepParameters[stepIndex] + " -> " + stepErrors[stepIndex]);

						// check if we are going uphill. but, make sure we have stuff to compare to...
						if (stepIndex > 1) {
							if (stepErrorsTotal[stepIndex] > stepErrorsTotal[stepIndex - 1]) {
								// reset step size & bounds; breakout; and restart

								// this will be before the minimum because either we went down and came back up
								// or, we are only on the third point, so we should start at the beginning again, and just look more finely
								errorMax = Math.max(stepErrorsTotal[stepIndex], Math.max(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
								errorMin = Math.min(stepErrorsTotal[stepIndex], Math.min(stepErrorsTotal[stepIndex - 1],stepErrorsTotal[stepIndex - 2]));
								errorRange = errorMax - errorMin;

								// pure sectioning
								// startingMultiplierBoundary = stepParameters[stepIndex - 2];
								// stepSizeToUse = Math.abs((stepParameters[stepIndex] - stepParameters[stepIndex - 2]) / (magicNumberOfSteps - 1));
								
								// try a quadratic approximation approach
								// first, we define a parabola going through the final three points; this is ugly, so you might want to
								// derive it yourself in case you're wondering. it's not terribly hard...
								// y = ax^2 + bx + c
								x_1 = stepParameters[stepIndex - 2];
								x_2 = stepParameters[stepIndex - 1];
								x_3 = stepParameters[stepIndex];
								y_1 = stepErrorsTotal[stepIndex - 2];
								y_2 = stepErrorsTotal[stepIndex - 1];
								y_3 = stepErrorsTotal[stepIndex];
								
								A = (x_2 - x_1)/(x_2*x_2 - x_1*x_1) ;
								B = (y_2 - y_1)/(x_2*x_2 - x_1*x_1);
								
								b = (y_3 - y_1 - B*(x_3*x_3 - x_1*x_1)) / ((x_3 - x_1) - A*(x_3*x_3 - x_1*x_1));
								a = -b * A + B ;
								c = y_1 - a*x_1*x_1 - b*x_1;
								
								
								// now, we can figure where the bottom of the parabola is... -b/2a
								// but, instead of going straight to the bottom, let's go a little bit up from
								// the bottom on either side and use those as new edges for our search
								// minGuess the height at the bottom of the parabola, i.e., y(-b/2a)
								minGuess = - b*b / (4*a) + c;
//								System.out.println("a=" + a + " b=" + b + " c=" + c + " ; minGuess=" + minGuess + " at " + (-b/(2*a)));
								
								// now, let us define "a little bit up" as a certain fraction of the distance
								// between the bottom and the lower of our two edges... now, we know that
								// points 1 and 3 are the edges, so we look for the lower of those two
								lowerEdgeHeight = Math.min(y_1,y_3);
								
								newEdgeTargetHeight = minGuess + fractionAboveMinGuess * (lowerEdgeHeight - minGuess);

								// now we determine what x's result in that height.
								// the ole quadratic formula...
								newXUpper = (-b - Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);
								newXLower = (-b + Math.sqrt(b*b - 4*a*(c - newEdgeTargetHeight))) / (2*a);

								startingMultiplierBoundary = (newXLower);
								stepSizeToUse = Math.abs((newXUpper - newXLower) / (stepParameters.length - 1));
								
//								System.out.println("lEH=" + lowerEdgeHeight + " nETH=" + newEdgeTargetHeight + " nXL=" + newXLower + " nxU=" + newXUpper);

								finalMultiplier = -b / (2*a);

								System.out.println("--> restarting: errorRange = " + errorRange + " ; guess = " + finalMultiplier + " ; new size = " + stepSizeToUse);
								if (errorRange < stoppingTolerance) {
									withinTolerance = true;
									finalMultiplier = -b / (2*a);
									System.out.println("    == hit stopping tolerance, moving on after " + nSearches + " searches ==");
								}
								break;
							} // end of if going uphill
							if (stepIndex == stepParameters.length - 1) {
								startingMultiplierBoundary = stepParameters[0];
								stepSizeToUse = stepSizeToUse * 2.0; // Beware the MAGIC NUMBER!!! doubling our way back up...
								System.out.println("--> no min yet, restarting: new start = " + startingMultiplierBoundary + " ; new size = " + stepSizeToUse);
							}
						} // end of if stepIndex > 2
					} // end of for stepIndex

					
					if (breakDueToNaNs) {
						System.out.println("cont LS encountered a NaN on search " + nSearches + "; returning original parameters");
						break;
					}
					
					
				} // end of while tolerance not met

				
				if (breakDueToNaNs) {
					// something bad happened, use the old ones
					// since we aren't passing them around, just keep the old ones
//					finalPair[0] = weightsAndBiases[0];
//					finalPair[1] = weightsAndBiases[1];
				} else {
					// should be fine, use the new ones
					parameterStep = MatrixOperations.multiplyByConstant(finalMultiplier,normalizedDirection);
					weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],parameterStep);
				}

			
				
				
				
				
				
				///////////////////////////////////////////
				//  end UGLY line search implementation  //
				///////////////////////////////////////////
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				


				if (epochIndex % checkNumber == 0) {
					for (int threadIndex = 0; threadIndex < nThreads; threadIndex++) {
						System.out.println("\t validation/recording " + threadIndex);
						validationError[threadIndex] = NNHelperMethods.findErrorSingleHidden(validationXT[threadIndex][0], validationXT[threadIndex][1],
								weightsAndBiases[0], weightsAndBiases[1], nHidden, nOptionsMinusOne);

						// record progress
						trainingError[threadIndex]    = ((Double)gradientBundles[threadIndex][0]).doubleValue();
						trainingNCorrect[threadIndex] = ((Double)gradientBundles[threadIndex][1]).doubleValue();
						
						trainingErrorTotal   += trainingError[threadIndex];
						validationErrorTotal += validationError[threadIndex][0];
						
						trainingNCorrectTotal   += trainingNCorrect[threadIndex];
						validationNCorrectTotal += validationError[threadIndex][1];
					}
					

//					summaryOut.println(epochIndex + "\t" + trainingError[threadToUse] + "\t" + validationError[threadToUse][0] + "\t" +
//							Math.exp(-trainingError[threadToUse]/nTrainingTotal) + "\t" + Math.exp(-validationError[threadToUse][0]/nValidationTotal) + "\t" +
//							trainingNCorrect[threadToUse]/nTrainingTotal + "\t" + validationError[threadToUse][1]/nValidationTotal + "\t" +
//							nTrainingTotal + "\t" +	nValidationTotal +"\t" + nHidden + "\t" + bfgsDirectionAndBigG[1].getDimensions()[0]);
					summaryOut.println(epochIndex + "\t" + trainingErrorTotal + "\t" + validationErrorTotal + "\t" +
							Math.exp(-trainingErrorTotal/nTrainingTotal) + "\t" + Math.exp(-validationErrorTotal/nValidationTotal) + "\t" +
							trainingNCorrectTotal/nTrainingTotal + "\t" + validationNCorrectTotal/nValidationTotal + "\t" +
							nTrainingTotal + "\t" +	nValidationTotal + "\t" + nHidden + "\t" + bfgsDirectionAndBigG[1].getDimensions()[0]);
					summaryOut.flush();

					
					MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + epochIndex, delimiter);
					MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + epochIndex, delimiter);
					MatrixOperations.write2DMFMtoText(newGradient,gradientBaseName + epochIndex, delimiter);
					MatrixOperations.write2DMFMtoText(bfgsDirectionAndBigG[0],gradientBaseName + "_bfgs_" + epochIndex, delimiter);
				}

				System.gc();
			} // end for epoch
		
		summaryOut.flush();
		summaryOut.close();
		
		System.out.println("=======  All done at " + new Date() + " =======");

	} // main

}


