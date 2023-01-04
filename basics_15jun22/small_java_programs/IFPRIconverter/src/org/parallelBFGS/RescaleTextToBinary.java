package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
import java.util.Date;

public class RescaleTextToBinary {



	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length != 3) {
			System.out.println("Usage: classname dataFile rescaleFile outputFile");
		}
		
		String dataFile           = commandLineOptions[0];
		String rescaleFile        = commandLineOptions[1];
		String outputFile         = commandLineOptions[2];
		


		System.out.println("data file: " + dataFile);

		long startTime;
		long endTime;
		double diffTime;

		startTime = System.currentTimeMillis();

		// load the data in
		MultiFormatFloat originalDataMatrix = null;
		try {
			 originalDataMatrix = new MultiFormatFloat(dataFile);
		} catch (Exception e) {
			System.out.println("file not there as MFM [" + dataFile + "], trying for text...");
//			e.printStackTrace();
			originalDataMatrix = MatrixOperations.read2DMFFfromText(dataFile);
		}

		// load the rescaling factors in
		MultiFormatMatrix rescaleMatrix = null;
		try {
			 rescaleMatrix = new MultiFormatMatrix(rescaleFile);
		} catch (Exception e) {
			System.out.println("file not there as MFM [" + rescaleFile + "], trying for text...");
//			e.printStackTrace();
			rescaleMatrix = MatrixOperations.read2DMFMfromText(rescaleFile);
		}

		// Beware the MAGIC NUMBER!!! the centering values are in line index 2 (third line) based on the descriptive statistics convention
		MultiFormatMatrix centeringValues = MatrixOperations.getSubmatrix(rescaleMatrix, 2, 2, 0, -1, 1);

		// Beware the MAGIC NUMBER!!! the centering values are in line index 3 (fourth line) based on the descriptive statistics convention
		MultiFormatMatrix scalingValues = MatrixOperations.getSubmatrix(rescaleMatrix, 3, 3, 0, -1, 1);

		MultiFormatFloat rescaledData = MatrixOperations.recenterAndRescale(originalDataMatrix, centeringValues, scalingValues);
		
		rescaledData.finishRecordingMatrix();
		
		MatrixOperations.writeMFF(rescaledData, outputFile);

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


