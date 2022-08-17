package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
import java.util.Date;

public class NNMakePredictions {



	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length != 5) {
			System.out.println("Usage: classname initFile dataFile hasTargetsColumn outputFile nDecimals");
		}
		
		String initializationFile = commandLineOptions[0];
		String dataFile           = commandLineOptions[1];
		boolean hasTargetsColumn  = Boolean.parseBoolean(commandLineOptions[2]);
		String outputFile         = commandLineOptions[3];
		int nDecimals             = Integer.parseInt(commandLineOptions[4]);
		
		System.out.println("[[[ nDecimals = " + nDecimals + " ]]]");
		
		
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

		System.out.println("loading parameters...");
		
		MultiFormatMatrix weightSeed = MatrixOperations.read2DMFMfromText(weightSeedBaseName);
		MultiFormatMatrix biasSeed   = MatrixOperations.read2DMFMfromText(biasSeedBaseName);

		startTime = System.currentTimeMillis();

		System.out.println("doing predictions...");

		NNHelperMethods.predictSingleHiddenToFile(allDataMatrix, hasTargetsColumn,
				weightSeed, biasSeed, nHidden, nOptionsMinusOne, outputFile, delimiter, nDecimals);
		

		endTime = System.currentTimeMillis();
		diffTime = (endTime - startTime) / 1000.0;
		System.out.println("done predicting =  " + diffTime );

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.gc();
		
		System.out.println("=======  All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


