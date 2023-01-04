package org.ifpri_converter;
import java.io.BufferedReader;
//import java.io.File;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.FileWriter;
//import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
//import java.util.concurrent.*;

public class MagicExecTestor {




	public static void main(String commandLineOptions[]) throws Exception {

//		String dirToList = commandLineOptions[0];
		
		

		File pathToDSSATDirectoryAsFile = new File("/export/home/rdrobert/crop_stuff/DSSAT/for_cluster/");
		
		Process dssatProcess = Runtime.getRuntime().exec("silly.sh >> stupid.txt" , null , pathToDSSATDirectoryAsFile);

		// try to grab the stdout
		InputStream dssatInputStream = dssatProcess.getInputStream();
		InputStream errorInputStream = dssatProcess.getErrorStream();
		InputStreamReader dssatInputStreamReader = new InputStreamReader(dssatInputStream);
		InputStreamReader errorInputStreamReader = new InputStreamReader(errorInputStream);

		BufferedReader dssatBufferedReader = new BufferedReader(dssatInputStreamReader);
		BufferedReader errorBufferedReader = new BufferedReader(errorInputStreamReader);

		String line;
		int linesRead = 0;
		while ((line = dssatBufferedReader.readLine()) 
				!= null) {
			System.out.println("line_out " + linesRead + " = [" + line + "]");
			linesRead++;
		}

		linesRead = 0;
		while ((line = errorBufferedReader.readLine()) 
				!= null) {
			System.out.println("error_out " + linesRead + " = [" + line + "]");
			linesRead++;
		}

		// read directly into a character buffer
//		dssatInputStreamReader.read(magicStdOutRaw,0,magicLengthOfStdOut);
//		dssatOutputLine = new String(magicStdOutRaw,magicHarvestedWeightAtHarvestStartIndex,magicHarvestedWeightAtHarvestLength).trim();

//		dssatOutputLine = dssatBufferedReader.readLine();

//		dssatBufferedReader.close();
		dssatInputStreamReader.close();
		dssatInputStream.close();
	} // main

}


//figure out the total number of pixels in input file...
//int nLinesTotal = 72384976;
//RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
//long lastRec=randFile.length();
//randFile.close();
//FileReader inRead = new FileReader(inputFile);
//LineNumberReader lineRead = new LineNumberReader(inRead);
//lineRead.skip(lastRec);
//nLinesTotal = lineRead.getLineNumber();
//lineRead.close();
//inRead.close();
