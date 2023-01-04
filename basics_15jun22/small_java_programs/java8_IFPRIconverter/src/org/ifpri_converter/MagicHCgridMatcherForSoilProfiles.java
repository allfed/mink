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

public class MagicHCgridMatcherForSoilProfiles {

	private static double sliceToDoubleFromFront(String stringToSlice) {

		String cleanString = stringToSlice.trim();
		int stringLength = cleanString.length();
		double valueToReturn = Double.NaN;
		boolean haveGoodValue = false;

		String afterSlicing = null;
		for (int sliceIndex = 0; sliceIndex < stringLength ; sliceIndex++) {
			afterSlicing = cleanString.substring(sliceIndex);
//			System.out.println("sI = " + sliceIndex + "; aS = [" + afterSlicing + "]");
			try {
				valueToReturn = Double.parseDouble(afterSlicing);
//				System.out.println("   vTR = " + valueToReturn);
				break;
			} catch (NumberFormatException nfe) {
				// do nothing
//				System.out.println("nfe = " + nfe);
			}
		}


		return valueToReturn;
	}

	private static double sliceToDoubleFromBack(String stringToSlice) {

		String cleanString = stringToSlice.trim();
		int stringLength = cleanString.length();
		double valueToReturn = Double.NaN;
		boolean haveGoodValue = false;

		String afterSlicing = null;
		for (int sliceIndex = 0; sliceIndex < stringLength ; sliceIndex++) {
			afterSlicing = cleanString.substring(0,(cleanString.length() - sliceIndex));
			System.out.println("sI = " + sliceIndex + "; aS = [" + afterSlicing + "]");
			try {
				valueToReturn = Double.parseDouble(afterSlicing);
				System.out.println("   vTR = " + valueToReturn);
				break;
			} catch (NumberFormatException nfe) {
				// do nothing
//				System.out.println("nfe = " + nfe);
			}
		}

		return valueToReturn;
	}

	public static void main(String commandLineOptions[]) throws Exception {


		Date startTime = new Date();


		////////////////////////////////////////////
		// handle the command line arguments...
		////////////////////////////////////////////

		System.out.print("command line arguments: \n");
		for (int i = 0; i < commandLineOptions.length; i++) {
			System.out.print(i + " " + commandLineOptions[i] + "\n");
		}
		System.out.println();

		if (commandLineOptions.length == 0) {
			System.out.println("Usage: org.ifpri_converter.MagicHCgridMatcherForSoilProfiles attributes_file geography_base_name output_file header_line_flag\n" +
					"\n" +
					"The point of it is to take the HarvestChoice standardized grid ID numbers and associate them with\n" +
					"some sort of attribute so we can import them into GRASS easily. This is accomplished through Ricky's\n" +
					"r.in.new/r.out.new table and geography modules for GRASS (they really need to be renamed).\n" +
					"\n" +
					"The logic is that we will take a sorted list of id numbers which have other attributes associated\n" +
					"with them in a tab delimited format and match these up against a set of tables created by doing\n" +
					"g.region rast=proper_id_grid ; r.out.new input=proper_id_grid output=place_to_put_them\n" +
					"We will then pull out only the bits of geographic information needed for the pixel ids in the table of" +
					"attributes and make a new set of text files that can be imported via r.in.new." +
					"\n" +
					"WARNING!!! Nothing is idiot proofed!\n" +
					"\n" +
					"If the attributes file has a header line, the header_line_flag should be non-empty. All columns will be transfered to the\n" +
					"new raster table. If a column is non-numeric, characters will be sliced off the beginning until only\n" +
			"a number is found. If that fails, the slicing will proceed from the end. If that fails, -9999 will be asssigned.");
			System.exit(1);
		}

		String attributesFile = commandLineOptions[0];
		String geographyBaseName = commandLineOptions[1];
		String outputBaseName = commandLineOptions[2];
		boolean headerLineExists = false;
		if (commandLineOptions.length == 4) {
			if (Boolean.parseBoolean(commandLineOptions[3]) == true) {
				headerLineExists = true;
				System.out.println("There's a header line!!!");
			}
		}

		// MAGIC NUMBERS!!!!
		String magicDelimiter = "\t";
		double badValueCode = -9999.0;

		// figure out the total number of pixels in attributes file...
		int nLinesTotal = -1234;
		RandomAccessFile randFile = new RandomAccessFile(attributesFile,"r");
		long lastRec=randFile.length();
		randFile.close();
		FileReader inRead = new FileReader(attributesFile);
		LineNumberReader lineRead = new LineNumberReader(inRead);
		lineRead.skip(lastRec);
		nLinesTotal = lineRead.getLineNumber();
		lineRead.close();
		inRead.close();

		int nAttributeLines = -1;
		if (headerLineExists) {
			nAttributeLines = nLinesTotal - 1;
		} else {
			nAttributeLines = nLinesTotal;
		}

		// open up attribute file
		FileReader attributeInStream = new FileReader(attributesFile);
		BufferedReader atIn = new BufferedReader(attributeInStream);

		// open up geography files
		String geographyName = geographyBaseName + "_geog.txt";
		System.out.println("geography name = " + geographyName);

		FileReader geogInStream = new FileReader(geographyName);
		BufferedReader geogIn = new BufferedReader(geogInStream);

		String dataName = geographyBaseName + "_data.txt";
		System.out.println("data name = " + dataName);
		FileReader dataInStream = new FileReader(dataName);
		BufferedReader dataIn = new BufferedReader(dataInStream);

		// open out output files
		String geographyOutName = outputBaseName + "_geog.txt";
		System.out.println("geog out name = " + geographyOutName);
		FileOutputStream geogOutStream = new FileOutputStream(geographyOutName);
		PrintWriter geogOut = new PrintWriter(geogOutStream);

		String dataOutName = outputBaseName + "_data.txt";
		System.out.println("data out name = " + dataOutName);
		FileOutputStream dataOutStream = new FileOutputStream(dataOutName);
		PrintWriter dataOut = new PrintWriter(dataOutStream);


		// set up a for loop concerning the attribute file
		double pixelToFind = Double.NEGATIVE_INFINITY;
		double pixelInGeography = Double.NEGATIVE_INFINITY;


		String atLineContents = null;
		String[] atSplit = null;
		String dataLineContents = null;
		String geogLineContents = null;
		String interColumnSeparator = null;

		double valueToWrite = Double.NaN;

		int nDataLinesRead = 0;
		int nAtLinesRead = 0;
		
		int nLinesDisplayed = 0;
		int wrapValue = 30;
		int displayValue = 500;

		// skip over the first line if there's a header...
		String[] columnNames = null;
		if (headerLineExists) {
			atLineContents = atIn.readLine();
			columnNames = atLineContents.split(magicDelimiter);
		}

		for (long attributeLineIndex = 0; attributeLineIndex < nAttributeLines ; attributeLineIndex++) {
			// pull out a line of attributes
			atLineContents = atIn.readLine();
			if (atLineContents.isEmpty()) {
				System.out.print("{" + (nAtLinesRead + 1) + "has ^M}");
			} else {

				nAtLinesRead++;

				// split into string array on delimiter
				atSplit = atLineContents.split(magicDelimiter);

				// pull out the pixel id number
				pixelToFind = sliceToDoubleFromFront(atSplit[0]);

				if (nAtLinesRead % displayValue != 0) {
//					System.out.print(nAtLinesRead + ".");
				} else {
					if (nLinesDisplayed % wrapValue != 0) {
						System.out.print(nAtLinesRead + ".");
					} else {
						System.out.println(nAtLinesRead);
					}
					nLinesDisplayed++;
				}
//				System.out.flush();

//				System.out.println("aLI = " + attributeLineIndex + "; pTF = " + pixelToFind);
				// step through the geography data file until we find this id number
				while (pixelToFind != pixelInGeography) {
					dataLineContents = dataIn.readLine();
					geogLineContents = geogIn.readLine();

					nDataLinesRead++;

//					System.out.println("  " + nDataLinesRead + ": dLC = [" + dataLineContents + "]; gLC = [" + geogLineContents + "]");
					/*
    		if (nDataLinesRead % wrapValue != 0) {
      		System.out.print(nDataLinesRead + ".");
    		} else {
      		System.out.println(nDataLinesRead);
    		}
					 */    			

					pixelInGeography = sliceToDoubleFromFront(dataLineContents);
				}

				// add the geographic information to the output geographic file
				geogOut.println(geogLineContents);

				// go through the attributes, adding them to the output data file
				for (int atColIndex = 0; atColIndex < atSplit.length; atColIndex++) {
					valueToWrite = sliceToDoubleFromFront(atSplit[atColIndex]);
					if (Double.isNaN(valueToWrite)) {
						valueToWrite = sliceToDoubleFromBack(atSplit[atColIndex]);
					}
					if (Double.isNaN(valueToWrite)) {
						valueToWrite = badValueCode;
					}

					if (atColIndex != 0) {
						interColumnSeparator = magicDelimiter;
					} else {
						interColumnSeparator = "";
					}

					dataOut.print(interColumnSeparator + valueToWrite);
				}
				dataOut.println(); // tack on the "hard return"

			}

		}

		geogOut.flush();
		dataOut.flush();

		dataOut.close();
		dataOutStream.close();
		geogOut.close();
		geogOutStream.close();

		dataIn.close();
		dataInStream.close();
		geogIn.close();
		geogInStream.close();

		int nColsGeog = (geogLineContents.split(magicDelimiter)).length;
		int nColsData = (atLineContents.split(magicDelimiter)).length;
		
		String geographyOutInfoName = outputBaseName + "_geog.info.txt";
		File InfoFileToWrite = new File(geographyOutInfoName);
		FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
		PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);

		outInfoWriterObject.print(nAtLinesRead + "\t = Number of Rows\n");
		outInfoWriterObject.print(nColsGeog + "\t = Number of Columns\n");
		outInfoWriterObject.print((nAtLinesRead*nColsGeog) + "\t = Total Number of Elements\n");
		outInfoWriterObject.print(1 + "\t = The MultiFormatMatrix format the matrix was stored in\n");
		outInfoWriterObject.print(magicDelimiter + "\t = The string used to delimit elements in the Rows");

		outInfoWriterObject.flush();
		outInfoWriterObject.close();

		String dataOutInfoName = outputBaseName + "_data.info.txt";

		InfoFileToWrite = new File(dataOutInfoName);
		outInfoStream = new FileWriter(InfoFileToWrite);
		outInfoWriterObject = new PrintWriter(outInfoStream);

		outInfoWriterObject.print(nAtLinesRead + "\t = Number of Rows\n");
		outInfoWriterObject.print(nColsData + "\t = Number of Columns\n");
		outInfoWriterObject.print((nAtLinesRead*nColsData) + "\t = Total Number of Elements\n");
		outInfoWriterObject.print(1 + "\t = The MultiFormatMatrix format the matrix was stored in\n");
		outInfoWriterObject.print(magicDelimiter + "\t = The string used to delimit elements in the Rows");

		outInfoWriterObject.flush();
		outInfoWriterObject.close();

	
    

	} // main

}