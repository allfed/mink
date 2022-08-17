package org.DSSATRunner;

import java.io.*;

public class FunTricks {


	public static int nLinesInTextFile(String inputFile) throws FileNotFoundException, IOException{
		//figure out the total number of pixels in input file...
		int nLinesTotal = -1;
		
		RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
		
		long lastRec=randFile.length();
		
		randFile.close();
		
		FileReader inRead = new FileReader(inputFile);
		LineNumberReader lineRead = new LineNumberReader(inRead);
		
		lineRead.skip(lastRec);
		nLinesTotal = lineRead.getLineNumber();
		
		lineRead.close();
		inRead.close();
		
		return nLinesTotal;
	}


	public static void writeInfoFile (String basename, long nRows, long nCols, int formatIndex, String delimiter) 
	throws FileNotFoundException {
		
		File infoFileObject = new File(basename  + ".info.txt");
		PrintWriter infoOutSteam = new PrintWriter(infoFileObject);

		infoOutSteam.print(nRows + "\t = Number of Rows\n");
		infoOutSteam.print(nCols  + "\t = Number of Columns\n");
		infoOutSteam.print((nRows * nCols) + "\t = Total Number of Elements\n");
		infoOutSteam.print(formatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n"); // Beware the MAGIC NUMBER!!!
		infoOutSteam.print(delimiter + "\t = The string used to delimit elements in the Rows\n");

		infoOutSteam.flush();
		infoOutSteam.close();
		
	}
	
	public static void writeInfoFile (String basename, long nRows, long nCols, String delimiter) 
	throws FileNotFoundException {
		int formatIndex = 3; // brute force it to "in memory"
		writeInfoFile(basename, nRows, nCols, formatIndex, delimiter);
	}

	
	public static void writeStringToFile (String contents, String filename) 
	throws FileNotFoundException {
		
//		File fileObject = new File(filename);
//		PrintWriter outSteam = new PrintWriter(fileObject);
		PrintWriter outSteam = new PrintWriter(filename);

		outSteam.print(contents);

		outSteam.flush();
		outSteam.close();
		
	}
	

	public static void writeStringToFile (String contents, File filename) 
	throws FileNotFoundException {
		
		PrintWriter outSteam = new PrintWriter(filename);

		outSteam.print(contents);

		outSteam.flush();
		outSteam.close();
		
	}
	
	
	
	public static String readTextFileToString (String basename) throws FileNotFoundException, IOException {
		
		BufferedReader fileReader = new BufferedReader(new FileReader(basename));
		
		String fileContents = "";
		
		String fileLine = fileReader.readLine();
			while (fileLine != null) {
			fileContents += fileLine + "\n";
			

			fileLine = fileReader.readLine();
		}
		fileReader.close();

		return fileContents;
		
	}

	
	public static String[] readTextFileToArray (String filename) throws FileNotFoundException, IOException {
		
		int nLines = FunTricks.nLinesInTextFile(filename);
		
		BufferedReader fileReader = new BufferedReader(new FileReader(filename));
		
		String[] fileContents = new String[nLines];
		
		int lineIndex = 0;
		String fileLine = fileReader.readLine();
			while (fileLine != null) {
			fileContents[lineIndex] = fileLine;
			
			lineIndex++;

			fileLine = fileReader.readLine();
		}
		fileReader.close();

		return fileContents;
		
	}
	
	
}

