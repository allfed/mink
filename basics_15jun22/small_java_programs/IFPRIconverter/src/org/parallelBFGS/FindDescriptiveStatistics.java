package org.parallelBFGS;

//import org.parallelBFGS.NNHelperMethods;
import org.R2Useful.*;
import java.util.Date;

public class FindDescriptiveStatistics {



	public static void main(String commandLineOptions[]) throws Exception {

		long overallStart = System.currentTimeMillis();

		if (commandLineOptions.length != 2) {
			System.out.println("Usage: classname dataFile outputFile");
		}
		
		String dataFile           = commandLineOptions[0];
		String outputFile         = commandLineOptions[1];
		


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
//			System.out.println("i think the file was not there, trying for text...");
			e.printStackTrace();
			originalDataMatrix = MatrixOperations.read2DMFFfromText(dataFile);
		}

		MultiFormatFloat statisticsMatrix = MatrixOperations.descriptiveStatistics(originalDataMatrix);
		
		MatrixOperations.write2DMFFtoText(statisticsMatrix, outputFile, "\t");

		System.out.println("output file: " + outputFile);

		endTime = System.currentTimeMillis();
		diffTime = (endTime - startTime) / 1000.0;
		System.out.println("done =  " + diffTime );

		long overallEnd = System.currentTimeMillis();
		double diffSeconds = (overallEnd - overallStart) / 1000.0;
		double diffMinutes = diffSeconds / 60;
		double diffHours = diffSeconds / 3600; 

		System.out.println("=======  All done at " + new Date() + "; " + diffSeconds + "s or " + diffMinutes + "m or " + diffHours + "h =======");

	} // main

}


