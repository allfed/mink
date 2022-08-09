package org.ifpri_converter;
import java.io.BufferedReader;
//import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
//import java.io.LineNumberReader;
import java.io.PrintWriter;
//import java.io.RandomAccessFile;
import java.util.Date;

import org.R2Useful.*;
//import java.util.concurrent.*;

public class MagicGeogCreatorThornton {
  
  
	private static String[] splitUpLine (String inLine, int[] columnWidth) {
		
		// let us start with a bunch of magic numbers
		int nColumns = columnWidth.length;
		
		String[] outArray = new String[nColumns];
		
		int currentIndex = 0;
		int endIndex = -1;
		for (int colIndex = 0; colIndex < nColumns; colIndex++) {
			endIndex = currentIndex + columnWidth[colIndex] + 1;
			
			if (endIndex < inLine.length() - 1) {
				outArray[colIndex] = inLine.substring(currentIndex,currentIndex + columnWidth[colIndex] + 1).trim();
			} else {
				outArray[colIndex] = inLine.substring(currentIndex).trim();
			}

			currentIndex += columnWidth[colIndex];
		}
		
		return outArray;
	}
	
  public static void main(String commandLineOptions[]) throws Exception {
    
    
    Date startTime = new Date();
    
    
    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////
    
    System.out.print("command line arguments: ");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(" " + commandLineOptions[i]);
    }
    System.out.println();
    
    if (commandLineOptions.length == 0) {
      System.out.println("Usage: org.ifpri_converter.MagicConverterThornton filename output_base_filename\n ");
      System.exit(1);
    }
    
    String filename         = commandLineOptions[0];
    String outputBasename   = commandLineOptions[1];
    
    
    
    // MAGIC NUMBERS!!!!
    
    double cellSize = 0.08333333333333333333333; // hard-coded...
    
    double missingValue = -99999;
    int nRawColumns = 72;
 
		int[] columnWidth = new int[nRawColumns];
		columnWidth[0] = 9;
		columnWidth[1] = 9;
		columnWidth[2] = 9;
		columnWidth[3] = 6;		
		columnWidth[4] = 7;		
		columnWidth[5] = 7;		
		columnWidth[6] = 6;		
		columnWidth[7] = 6;		
		columnWidth[8] = 6;		
		columnWidth[9] = 6;		
		columnWidth[10] = 6;		
		columnWidth[11] = 6;		

		for (int colIndex = 12; colIndex < nRawColumns; colIndex++) {
			columnWidth[colIndex] = 6;
		}

    
    int nGeographyColumns = 2;
    
    // figure out the total number of points...
    int nLinesTotal = FunTricks.nLinesInTextFile(filename);
    
    String delimiter = "\t";
    
    
    // now let's just open it up in a buffered reader and step through it...
    FileReader inStream = new FileReader(filename);
    BufferedReader ininin = new BufferedReader(inStream);
    
    // initialize the variables
    // raw columns are: n,lat,long,elev,col,row,tt,amp,sry,txy,tny,rt,srad,tx,tn,rain,rdays (72 total)
    
    String   lineContents = null;
    double thisLatitude  = -200;
    double thisLongitude = -300;
    
    // initialize these to be lower than any valid values
    double minLatitude  = Double.POSITIVE_INFINITY;
    double maxLatitude  = Double.NEGATIVE_INFINITY;
    double minLongitude = Double.POSITIVE_INFINITY;
    double maxLongitude = Double.NEGATIVE_INFINITY;
    
    for (int lineNumber = 0 ; lineNumber < nLinesTotal ; lineNumber++) {
      
      // read the line in
      lineContents = ininin.readLine();
      
      thisLatitude  = Double.parseDouble(lineContents.substring(9, 18));
      thisLongitude = Double.parseDouble(lineContents.substring(18, 27));
      
      // update the min and max latitudes and longitudes...
      if (thisLatitude < minLatitude) {
        minLatitude = thisLatitude;
      }
      if (thisLatitude > maxLatitude) {
        maxLatitude = thisLatitude;
      }
      
      if (thisLongitude < minLongitude) {
        minLongitude = thisLongitude;
      }
      if (thisLongitude > maxLongitude) {
        maxLongitude = thisLongitude;
      }
      
    } // end first pass through the data...
    
    // close the input file
    ininin.close();
    inStream.close();
    
    // decide on an array and initialize...
    double nRowsDouble = ((maxLatitude  - minLatitude)  / cellSize) + 1;
    double nColsDouble = ((maxLongitude - minLongitude) / cellSize) + 1;
    
    int nRows = (int)Math.round(nRowsDouble);
    int nCols = (int)Math.round(nColsDouble);
    
    System.out.println("nRows = " + nRowsDouble + "/" + nRows + " ; nCols = " + nColsDouble + "/" + nCols);
    
    // create geographic header file
    FileOutputStream outArcStream = new FileOutputStream(outputBasename + "_header.txt");
    PrintWriter outArcWriterObject = new PrintWriter(outArcStream);
    
    // write the headers
    outArcWriterObject.print("ncols         " + nCols + "\n");
    outArcWriterObject.print("nrows         " + nRows + "\n");
    outArcWriterObject.print("xllcorner     " + (minLongitude - cellSize/2.0 + 0*cellSize) + "\n");
    outArcWriterObject.print("yllcorner     " + (minLatitude  - cellSize/2.0 + 1*cellSize) + "\n");
    outArcWriterObject.print("cellsize      " + cellSize + "\n");
    outArcWriterObject.print("NODATA_value  " + (int)missingValue + "\n");

    outArcWriterObject.flush();
    outArcWriterObject.close();


    // re-open the file...
    inStream = new FileReader(filename);
    ininin = new BufferedReader(inStream);

    
    // ok, not only do we have to read everything, we have to do a partial re-ordering.
    // this will be ugly.
    // let's start by putting everything in disk-bound MFFs

    MultiFormatFloat dataMatrix = new MultiFormatFloat(1,new long[] {nLinesTotal,nRawColumns});
    MultiFormatFloat geogMatrix = new MultiFormatFloat(1,new long[] {nLinesTotal,nGeographyColumns});

    System.out.println("-- done initializing --");
    
    String[] splitLine = null;
    int geogRow = -1;
    int geogCol = -2;
    for (int lineNumber = 0 ; lineNumber < nLinesTotal ; lineNumber++) { // nLinesTotal
      
      // read the line in
      lineContents = ininin.readLine();
    
      // read in the goodies and put them in an array
//      splitLine = FunTricks.readLineToArrayOnArbitrarySpaceOrTab(lineContents);
      splitLine = splitUpLine(lineContents,columnWidth);
      
      // convert it to a delimited line...
      for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
      	try {
      	dataMatrix.setValue(lineNumber,splitIndex, Float.parseFloat(splitLine[splitIndex]));
      	} catch (NumberFormatException nfe) {
      		System.out.println("line index = " + lineNumber + "; splitIndex = " + splitIndex + "; contents = [" + splitLine[splitIndex] + "]");
      		System.out.println("whole line = [" + lineContents + "]");
      		for (int altSplit = 0; altSplit < splitLine.length; altSplit++) {
      			System.out.println(altSplit + " -> [" + splitLine[altSplit] + "]");
      		}
      		throw nfe;
      	}
      }
      
      // devise the geography row/col line
      thisLatitude  = Double.parseDouble(lineContents.substring(9, 18));
      thisLongitude = Double.parseDouble(lineContents.substring(18, 27));

      geogCol = (int)Math.round((thisLongitude - minLongitude) / cellSize);
      geogRow = nRows - (int)Math.round((thisLatitude  - minLatitude ) / cellSize);

      geogMatrix.setValue(lineNumber,0, geogRow);
      geogMatrix.setValue(lineNumber,1, geogCol);

    }

    geogMatrix.finishRecordingMatrix();
    dataMatrix.finishRecordingMatrix();

    ininin.close();
    
    System.out.println("-- done reading data into a matrix --");
    // now comes the tricky part. we need to put everything in the opposite order by row number, but keep the
    // column numbers the same...
    
    MultiFormatFloat dataMatrixFinal = new MultiFormatFloat(1,new long[] {nLinesTotal,nRawColumns});
    MultiFormatFloat geogMatrixFinal = new MultiFormatFloat(1,new long[] {nLinesTotal,nGeographyColumns});
    
    
    // start at the bottom of the geog matrix
    // look for the next new row number
    // copy the block to the top of the final matrices...
    
    
    int nRowsToCopy = -1;
    int destinationRow = -5;
    int sourceRow = -6;
    
    // initialize...
    int topOfDestination = 0; // this actually needs to be zero
    int bottomOfBlockToCopy = nLinesTotal - 1; // this actually needs to have this value

    float currentGeogRowBeingConsidered = geogMatrix.getValue(nLinesTotal - 1,0);
    float oldGeogRowBeingConsidered = currentGeogRowBeingConsidered;
    
    float tempValue;
    
    for (int readRowIndex = nLinesTotal - 1; readRowIndex >= 0; readRowIndex--) {
    	
    	currentGeogRowBeingConsidered = geogMatrix.getValue(readRowIndex,0);
//    	System.out.println(readRowIndex + ": current considered = " + currentGeogRowBeingConsidered + " ; old = " + oldGeogRowBeingConsidered);
    	
    	if (currentGeogRowBeingConsidered != oldGeogRowBeingConsidered || readRowIndex == 0) {
    		// we have reached a new block (or the end).

    		// let us be evil and modify the index to simulate running past the end of the array
    		if (readRowIndex == 0) {
    			readRowIndex = -1;
    		}

    		// copy over the old block

    		nRowsToCopy = bottomOfBlockToCopy - readRowIndex;
    		
//    		System.out.println("  -- initiate copying --");
//    		System.out.println("nRowsToCopy = " + nRowsToCopy);
//    		System.out.println("source base = " + (readRowIndex + 1));
//    		System.out.println("dest base   = " + topOfDestination);
    		
    		for (int copyRowIndex = 0; copyRowIndex < nRowsToCopy; copyRowIndex++) {
    			
    			sourceRow      = readRowIndex + 1 + copyRowIndex;
    			destinationRow = topOfDestination + copyRowIndex;

    			// geography
    			for (int colIndex = 0; colIndex < nGeographyColumns; colIndex++) {
    				tempValue = geogMatrix.getValue(sourceRow, colIndex);
    				
    				geogMatrixFinal.setValue(destinationRow, colIndex,
    						tempValue
    				);
    			}
    			
    			// data
    			for (int colIndex = 0; colIndex < nRawColumns; colIndex++) {
    				tempValue = dataMatrix.getValue(sourceRow, colIndex);
    				
    				dataMatrixFinal.setValue(destinationRow, colIndex,
    						tempValue
    				);
    			}
    		}
    		
    		// reset everything
    		topOfDestination = topOfDestination + nRowsToCopy;
    		oldGeogRowBeingConsidered = currentGeogRowBeingConsidered;
    		bottomOfBlockToCopy = readRowIndex;
    	}
//    	else {
//  			System.out.println("    not");
//  		}
    	
    }
    
    // manually do the last block...
    
    
    // finish up and write out...

    geogMatrixFinal.finishRecordingMatrix();
    dataMatrixFinal.finishRecordingMatrix();
    

//    MatrixOperations.write2DMFFtoText(dataMatrix, outputBasename + "_data_raw", delimiter);
//    MatrixOperations.write2DMFFtoText(geogMatrix, outputBasename + "_geog_raw", delimiter);

    MatrixOperations.write2DMFFtoText(dataMatrixFinal, outputBasename + "_data", delimiter);
    MatrixOperations.write2DMFFtoTextAsInt(geogMatrixFinal, outputBasename + "_geog", delimiter);

    
    
    Date endTime = new Date();
    
    double duration = (endTime.getTime() - startTime.getTime()) / 1000.0;
    
    System.out.println("Finished execution! duration = " + duration + " seconds or " + (duration/60) + " minutes or " + (duration/3600) + " hours" );
    
    
  } // main
  
}