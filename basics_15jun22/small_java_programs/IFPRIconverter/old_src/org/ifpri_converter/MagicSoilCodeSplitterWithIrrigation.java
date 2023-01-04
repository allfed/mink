package org.ifpri_converter;
import java.io.BufferedReader;
//import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;
//import java.util.concurrent.*;

public class MagicSoilCodeSplitterWithIrrigation {


	public static void main(String commandLineOptions[]) throws Exception {


		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i] + "\n");
		}
		System.out.println();

		if (commandLineOptions.length == 0) {
			System.out.println("Usage: org.ifpri_converter.MagicSoilCodeSplitterWithIrrigation input_file soil_column zero_columns_csv soil_code_table_file output_directory_with_slash output_base_name delimiter csv_columns_to_keep\n" +
					"\n" +
					"The point of it is to take the output of Jawoo's runs and split them up into separate files for each \n" +
					"soil code. \n" +
					"\n" +
					"We also want to give a list of columns which, when any is non-zero, should go into a separate file from the zeros.\n" +
					"\n" +
					" The column with the soil code number needs to be specified. We also specify the \n" +
					"columns to keep using a CSV list. The columns will be outputted in the order that they appear \n" +
					"in the list. Counting starts at zero.\n" +
					"The soil code table file should have two comma separated columns per line. The first column is the \n" +
					"soil code number while the second is the three letter code name. The codes are assumed to be sequential starting at 1.\n" +
					"\n" +
					"We assume that there is a header line\n" +
					"\n" +
					"WARNING!!! Nothing is idiot proofed!\n" +
					"\n");
			System.exit(1);
		}

		String inputFile         =                   commandLineOptions[0];
		int soilColumn           = (Integer.parseInt(commandLineOptions[1]));
		String zeroColumnsCSV    =                   commandLineOptions[2];
		String soilCodeTableFile =                   commandLineOptions[3];
		String outputDirectory   =                   commandLineOptions[4];
		String outputBaseName    =                   commandLineOptions[5];
		String delimiter         =                   commandLineOptions[6];
		String csvColumnsToKeep  =                   commandLineOptions[7];

		// some magic numbers
		int magicCheckNumber = 100000;
		int magicWrapNumber = 7;
		

		// build a more easily processed list of columns to test for zero
		String[] zeroColumnsCSVSplit = zeroColumnsCSV.split(","); // Beware the MAGIC NUMBER!!! comma for CSV
		int nZeroColumns = zeroColumnsCSVSplit.length;
		int[] columnsToTestForZeros = new int[nZeroColumns];
		
		for (int colIndex = 0; colIndex < nZeroColumns; colIndex++) {
			columnsToTestForZeros[colIndex] = Integer.parseInt(zeroColumnsCSVSplit[colIndex]);
			System.out.println("  actual col " + columnsToTestForZeros[colIndex] + " will be tested for zero-ness");
		}

		// build a more easily processed list of columns
		String[] csvColumnsToKeepSplit = csvColumnsToKeep.split(","); // Beware the MAGIC NUMBER!!! comma for CSV
		int nColumnsToKeep = csvColumnsToKeepSplit.length;
		int[] columnsToKeepList = new int[nColumnsToKeep];
		
		for (int colIndex = 0; colIndex < nColumnsToKeep; colIndex++) {
			columnsToKeepList[colIndex] = Integer.parseInt(csvColumnsToKeepSplit[colIndex]);
			System.out.println("actual col " + columnsToKeepList[colIndex] + " is being mapped to " + colIndex);
		}
		
		
		// read in the soil code list
//		figure out the total number of pixels in input file...
		int nSoilCodes = 72384976;
		RandomAccessFile randFile = new RandomAccessFile(soilCodeTableFile,"r");
		long lastRec=randFile.length();
		randFile.close();
		FileReader soilCodeInStream = new FileReader(soilCodeTableFile);
		LineNumberReader lineRead = new LineNumberReader(soilCodeInStream);
		lineRead.skip(lastRec);
		nSoilCodes = lineRead.getLineNumber();
		lineRead.close();
		soilCodeInStream.close();

		String[] soilCodeList = new String[nSoilCodes]; // dealing with numbers, not indices, unfortunately
		
		soilCodeInStream = new FileReader(soilCodeTableFile);
		BufferedReader soilCodeReader = new BufferedReader(soilCodeInStream);

		String lineContents = "This is the initial value.";
		String[] lineSplit = null;
		String[] colNames = null;

		int soilCode = -1;
		int soilIndexToUse = -4;
		double[] zeroColumnContents = new double[nZeroColumns];
		
		for (int lineIndex = 0; lineIndex < nSoilCodes; lineIndex++) {
			lineContents = soilCodeReader.readLine();
			lineSplit = lineContents.split(","); // Beware the MAGIC NUMBER!!! comma, for CSV
		
			soilCode = Integer.parseInt(lineSplit[0]);
			soilIndexToUse = soilCode - 1;
			soilCodeList[soilIndexToUse] = new String(lineSplit[1]);

			System.out.println("soil number + [" + soilCode +
					"] = soil index [" + soilIndexToUse +
					"] -> soil type " + soilCodeList[soilIndexToUse] );
		}

		// open up the output files
		// the non-zeros
		String outputOutName = null;
//		FileOutputStream[] outputStreamsNonZero = new FileOutputStream[nSoilCodes];
		PrintWriter[]      outWritersNonZero    = new PrintWriter[nSoilCodes];
		long[] nLinesWrittenNonZero = new long[nSoilCodes];

		// the zeros
//		FileOutputStream[] outputStreamsZero = new FileOutputStream[nSoilCodes];
		PrintWriter[]      outWritersZero    = new PrintWriter[nSoilCodes];
		long[] nLinesWrittenZero = new long[nSoilCodes];

		for (int soilIndex = 0; soilIndex < nSoilCodes; soilIndex++) {
			outputOutName = outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_nonzeros.txt";
//			outputStreamsNonZero[soilIndex] = new FileOutputStream(outputOutName);
			outWritersNonZero[soilIndex]    = new PrintWriter(outputOutName);

			outputOutName = outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_zeros.txt";
//			outputStreamsZero[soilIndex] = new FileOutputStream(outputOutName);
			outWritersZero[soilIndex]    = new PrintWriter(outputOutName);
		}

		// open up input file
		FileReader inputInStream = new FileReader(inputFile);
		BufferedReader inReader = new BufferedReader(inputInStream);


		String lineToWrite = null;
		int colToPull = -2;
		long lineNumber = 0;
		boolean hasAllZeros = false;
		// read in the header line...
		lineContents = inReader.readLine();
		colNames = lineContents.split(delimiter);
		lineNumber++;

		// seed it with the first line of real data; we'll do the reading of the line at the end....
		lineContents = inReader.readLine();
		System.out.println("starting...");
		while (lineContents != null) {
			lineNumber++;
			hasAllZeros = true;
			if (lineContents.length() == 0) {
				System.out.println("#" + lineNumber + " was zero length..., skipping");
				lineContents = inReader.readLine();
				continue;
			}
			lineSplit = lineContents.split(delimiter);

			// find the soil contents
			soilCode = Integer.parseInt(lineSplit[soilColumn]);
			soilIndexToUse = soilCode - 1;
			
			for (int zeroIndex = 0; zeroIndex < nZeroColumns; zeroIndex++) {
				zeroColumnContents[zeroIndex] = Double.parseDouble(lineSplit[ columnsToTestForZeros[zeroIndex] ]);
				if (zeroColumnContents[zeroIndex] != 0.0) {
					hasAllZeros = false;
				}
			}
			
			// pull out the columns we want...
			// do the last column separately so as to do the \n
			lineToWrite = "";
			for (int colIndex = 0; colIndex < nColumnsToKeep - 1; colIndex++) {
//				columnsToKeepList[colIndex];
				colToPull = columnsToKeepList[colIndex];
				lineToWrite = lineToWrite + lineSplit[colToPull] + delimiter;
			}
			if (!hasAllZeros) {
				// add in the zeros columns
				for (int zeroIndex = 0; zeroIndex < nZeroColumns; zeroIndex++) {
					lineToWrite = lineToWrite + zeroColumnContents[zeroIndex] + delimiter;
				}
			}
				
			// last column we want
			colToPull = columnsToKeepList[nColumnsToKeep - 1];
			lineToWrite = lineToWrite + lineSplit[colToPull];
			
			if (hasAllZeros) {
				outWritersZero[soilIndexToUse].println(lineToWrite);
				nLinesWrittenZero[soilIndexToUse]++;
			} else {
				outWritersNonZero[soilIndexToUse].println(lineToWrite);
				nLinesWrittenNonZero[soilIndexToUse]++;
			}
			
			if (lineNumber % (magicCheckNumber) == 0) {
				if (lineNumber % (magicCheckNumber * magicWrapNumber) != 0) {
					System.out.print(lineNumber + ".");
				} else {
					System.out.println(lineNumber);

				}
			}

			// read in next line so it can be tested for existence
			lineContents = inReader.readLine();

		}  


		// clean up the mess...
		// write the info file
		File infoFileToWrite = null;
		FileWriter outInfoStream = null;
		PrintWriter outInfoWriterObject = null;


		for (int soilIndex = 0; soilIndex < nSoilCodes; soilIndex++) {
			//////////////
			// zeros... //
			//////////////
			System.out.println("zeros: index " + soilIndex + "/" + soilCodeList[soilIndex] + " -> " + nLinesWrittenZero[soilIndex] + " lines written");
			outWritersZero[soilIndex].flush();
			outWritersZero[soilIndex].close();
//			outputStreamsZero[soilIndex].close();

			// write out the header file
			infoFileToWrite = new File(outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_zeros.info.txt");
			outInfoStream = new FileWriter(infoFileToWrite);
			outInfoWriterObject = new PrintWriter(outInfoStream);

			outInfoWriterObject.print(nLinesWrittenZero[soilIndex] + "\t = Number of Rows\n");
			outInfoWriterObject.print(nColumnsToKeep + "\t = Number of Columns\n");
			outInfoWriterObject.print((nLinesWrittenZero[soilIndex]*nColumnsToKeep) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(1 + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");

			outInfoWriterObject.flush();
			outInfoWriterObject.close();
			
			// write out the column names
			infoFileToWrite = new File(outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_zeros.cols.txt");
			outInfoStream = new FileWriter(infoFileToWrite);
			outInfoWriterObject = new PrintWriter(outInfoStream);

			for (int colIndex = 0; colIndex < nColumnsToKeep - 1; colIndex++) {
				outInfoWriterObject.print(colNames[columnsToKeepList[colIndex]] + delimiter);
			}
			outInfoWriterObject.println(colNames[columnsToKeepList[nColumnsToKeep - 1]]);

			outInfoWriterObject.flush();
			outInfoWriterObject.close();

			//////////////
			// non-zeros... //
			//////////////
			System.out.println("non-zeros: index " + soilIndex + "/" + soilCodeList[soilIndex] + " -> " + nLinesWrittenNonZero[soilIndex] + " lines written");
			outWritersNonZero[soilIndex].flush();
			outWritersNonZero[soilIndex].close();
//			outputStreamsNonZero[soilIndex].close();

			// write out the header file
			infoFileToWrite = new File(outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_nonzeros.info.txt");
			outInfoStream = new FileWriter(infoFileToWrite);
			outInfoWriterObject = new PrintWriter(outInfoStream);

			outInfoWriterObject.print(nLinesWrittenNonZero[soilIndex] + "\t = Number of Rows\n");
			outInfoWriterObject.print((nColumnsToKeep + nZeroColumns) + "\t = Number of Columns\n");
			outInfoWriterObject.print((nLinesWrittenNonZero[soilIndex]*(nColumnsToKeep + nZeroColumns)) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(1 + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");

			outInfoWriterObject.flush();
			outInfoWriterObject.close();
			
			// write out the column names
			infoFileToWrite = new File(outputDirectory + soilCodeList[soilIndex] + "_" + outputBaseName + "_nonzeros.cols.txt");
			outInfoStream = new FileWriter(infoFileToWrite);
			outInfoWriterObject = new PrintWriter(outInfoStream);

			for (int colIndex = 0; colIndex < nColumnsToKeep - 1; colIndex++) {
				outInfoWriterObject.print(colNames[columnsToKeepList[colIndex]] + delimiter);
			}
			for (int zeroIndex = 0; zeroIndex < nZeroColumns; zeroIndex++) {
				outInfoWriterObject.print(colNames[ columnsToTestForZeros[zeroIndex] ] + delimiter);
			}

			outInfoWriterObject.println(colNames[columnsToKeepList[nColumnsToKeep - 1]]);

			outInfoWriterObject.flush();
			outInfoWriterObject.close();
}

		
    
		System.out.println("-- all done at " + new Date());

	} // main

}


// figure out the total number of pixels in input file...
//int nLinesTotal = 72384976;
//RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
//long lastRec=randFile.length();
//	randFile.close();
//FileReader inRead = new FileReader(inputFile);
//LineNumberReader lineRead = new LineNumberReader(inRead);
//lineRead.skip(lastRec);
//nLinesTotal = lineRead.getLineNumber();
//lineRead.close();
//inRead.close();
