package org.parallelBFGS.GradientDescent;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
import org.parallelBFGS.*;

import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class NNSingleGradientDescent {



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

		long startTime;
		long endTime;
		double diffTime;

		// load the data in
		MultiFormatFloat allDataMatrix = null;
		try {
			 allDataMatrix = new MultiFormatFloat(dataFile);
		} catch (Exception e) {
			System.out.println("i think the file was not there, trying for text...");
			e.printStackTrace();
			allDataMatrix = MatrixOperations.read2DMFFfromText(dataFile);
		}

		// split into training/validation as well as X and targets
		
		System.out.println("doing T/V split at " + new Date());
		startTime = System.currentTimeMillis();

		MultiFormatFloat[] trainingValidationPair = MatrixOperations.randomizeRows2dMff(allDataMatrix, aRandomSeed, fractionInTraining,1);
		long nTraining   = trainingValidationPair[0].getDimensions()[0];
		long nValidation = trainingValidationPair[1].getDimensions()[0];

		endTime = System.currentTimeMillis();
		diffTime = (endTime - startTime) / 1000.0;
		System.out.println("alt T/V =  " + diffTime );

		
		System.out.println("doing X/target splits at " + new Date());
		MultiFormatFloat[] trainingPair   = MatrixOperations.splitOffLastColumn(trainingValidationPair[0]);
		MultiFormatFloat[] validationPair = MatrixOperations.splitOffLastColumn(trainingValidationPair[1]);

		System.out.println("loading parameters...");
		
		MultiFormatMatrix weightSeed = MatrixOperations.read2DMFMfromText(weightSeedBaseName);
		MultiFormatMatrix biasSeed   = MatrixOperations.read2DMFMfromText(biasSeedBaseName);
		
		int nParameters = (int) (weightSeed.getDimensions()[0] + biasSeed.getDimensions()[0] - trainingValidationPair[0].getDimensions()[1]);

		// set up the output for the summary

		File fileToWrite = new File(reportBaseName);
		FileOutputStream summaryStream = new FileOutputStream(fileToWrite, true);
		PrintWriter summaryOut = new PrintWriter(summaryStream);

		// set up the training loop
		double trainingError = -1;
		double trainingNCorrect = -2;
		double[] validationError = null;
		Object[] gradientBundle = null;
		

		MultiFormatMatrix[] weightsAndBiases    = new MultiFormatMatrix[] {weightSeed, biasSeed};
		MultiFormatMatrix[] oldWeightsAndBiases = new MultiFormatMatrix[] {weightSeed, biasSeed};
				
		System.out.println("starting training at " + new Date());
		
		for (int epochIndex = 0; epochIndex < nEpochs; epochIndex++) {

			// determine new weights

			startTime = System.currentTimeMillis();
			
			gradientBundle = NNHelperMethods.findSingleHiddenErrorGradient(trainingPair[0], trainingPair[1],
					weightsAndBiases[0], weightsAndBiases[1], nHidden, nOptionsMinusOne);
			
			endTime = System.currentTimeMillis();
			diffTime = (endTime - startTime) / 1000.0;
			System.out.println("T " + epochIndex + " =\t" + diffTime + "s");
			startTime = System.currentTimeMillis();
			
			oldWeightsAndBiases[0] = MatrixOperations.newCopy(weightsAndBiases[0]);
			oldWeightsAndBiases[1] = MatrixOperations.newCopy(weightsAndBiases[1]);

			weightsAndBiases = NNHelperMethods.newWeightsBiases(weightsAndBiases[0], weightsAndBiases[1],
					MatrixOperations.multiplyByConstant(learningRate, (MultiFormatMatrix)gradientBundle[2]));
						
			endTime = System.currentTimeMillis();
			diffTime = (endTime - startTime) / 1000.0;
			System.out.println("LS " + epochIndex + " =\t" + diffTime + "s");

			
			if (epochIndex % checkNumber == 0) {
				startTime = System.currentTimeMillis();
				validationError = NNHelperMethods.findErrorSingleHidden(validationPair[0], validationPair[1], weightsAndBiases[0], weightsAndBiases[1],
						nHidden, nOptionsMinusOne);
				endTime = System.currentTimeMillis();
				diffTime = (endTime - startTime) / 1000.0;
				System.out.println("\tV " + epochIndex + " =\t" + diffTime + "s");

				// record progress
				trainingError = ((Double)gradientBundle[0]).doubleValue();
				trainingNCorrect = ((Double)gradientBundle[1]).doubleValue();
				
				summaryOut.println(epochIndex + "\t" + ((Double)gradientBundle[0]).doubleValue() + "\t" + validationError[0] + "\t" +
						Math.exp(-trainingError/nTraining) + "\t" + Math.exp(-validationError[0]/nValidation) + "\t" +
						trainingNCorrect/nTraining + "\t" + validationError[1]/nValidation + "\t" + nTraining + "\t" +
						nValidation +"\t" + nHidden + "\t" + nParameters);
				summaryOut.flush();
				
				MatrixOperations.write2DMFMtoText(weightsAndBiases[0], weightBaseName     + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText(weightsAndBiases[1], biasBaseName       + epochIndex, delimiter);
				MatrixOperations.write2DMFMtoText((MultiFormatMatrix)gradientBundle[2],gradientBaseName + epochIndex, delimiter);
				
			}
			
			System.gc();
		}
		
		summaryOut.flush();
		summaryOut.close();

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


