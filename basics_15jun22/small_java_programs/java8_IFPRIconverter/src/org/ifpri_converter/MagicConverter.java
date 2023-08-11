package org.ifpri_converter;

import java.io.BufferedReader;
// import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;
// import java.util.concurrent.*;

public class MagicConverter {

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
      System.out.println("Usage: org.ifpri_converter.MagicConverter filename delimiter\n ");
      System.exit(1);
    }

    String filename = commandLineOptions[0];
    String delimiter = commandLineOptions[1];

    // MAGIC NUMBERS!!!!

    double cellSize = 0.0833334; // hard-coded...
    int pixelField = 2; // index; it is actually #3
    int valueField = 7; // index; it is actually #8

    double missingValue = -9999;

    // figure out the total number of points...
    int nLinesTotal = -1234;
    RandomAccessFile randFile = new RandomAccessFile(filename, "r");
    long lastRec = randFile.length();
    randFile.close();
    FileReader inRead = new FileReader(filename);
    LineNumberReader lineRead = new LineNumberReader(inRead);
    lineRead.skip(lastRec);
    nLinesTotal = lineRead.getLineNumber();
    lineRead.close();
    inRead.close();

    // now let's just open it up in a buffered reader and step through it...
    FileReader inStream = new FileReader(filename);
    BufferedReader ininin = new BufferedReader(inStream);

    // initialize the variables
    String lineContents = null;
    String[] splitLine = null;
    int pixelCode = -1;
    int col = -50;
    int row = -51;
    double[] latitude = new double[nLinesTotal];
    double[] longitude = new double[nLinesTotal];
    double[] valueList = new double[nLinesTotal];

    // initialize these to be lower than any valid values
    double minLatitude = Double.POSITIVE_INFINITY;
    double maxLatitude = Double.NEGATIVE_INFINITY;
    double minLongitude = Double.POSITIVE_INFINITY;
    double maxLongitude = Double.NEGATIVE_INFINITY;

    for (int lineNumber = 0; lineNumber < nLinesTotal; lineNumber++) {

      // read the line in
      lineContents = ininin.readLine();

      // split it up using the provided delimiter
      splitLine = lineContents.split(delimiter);

      // pull out the bits we need
      pixelCode = Integer.parseInt(splitLine[pixelField].trim());
      valueList[lineNumber] = Double.parseDouble(splitLine[valueField].trim());

      // interpret the pixelCode
      // we were told that...
      // The Alloc_key IDs are developed by Latitude and longitude.
      // First, the world is devided into 360 (longitude) * 180 (latitude) cells.
      // Each cell is about 5 arc minutes (0.0833334 degree).
      // The latitude value range is from -90 to +90 and longitude is from -180 to +180.
      // The Alloc_key has eight digits.
      // The first four digits represent row number and the last four digits represent column
      // number.
      // The equation to get row and column number are shown as below:
      //
      //  col = ((gridx + 180)/(0.0833334) ) + 1
      //  row = ((90 - gridy)/(0.0833334) ) + 1
      //
      ////////////////////
      //
      // this means:
      // gridx = (col - 1) * 0.0833334 - 180
      // gridy = 90 - (row - 1) * 0.0833334

      row = pixelCode / 10000; // hoping for integer truncation...
      col = pixelCode - (10000 * row);

      longitude[lineNumber] = (col - 1) * cellSize - 180.0;
      latitude[lineNumber] = 90.0 - (row - 1) * cellSize;

      // update the min and max latitudes and longitudes...
      if (latitude[lineNumber] < minLatitude) {
        minLatitude = latitude[lineNumber];
      }
      if (latitude[lineNumber] > maxLatitude) {
        maxLatitude = latitude[lineNumber];
      }

      if (longitude[lineNumber] < minLongitude) {
        minLongitude = longitude[lineNumber];
      }
      if (longitude[lineNumber] > maxLongitude) {
        maxLongitude = longitude[lineNumber];
      }

      //      System.out.println("t: " + minLatitude + "/" + latitude[lineNumber] + "/" +
      // maxLatitude +
      //          " g: " + minLongitude + "/" + longitude[lineNumber] + "/" + maxLongitude);
    } // end first pass through the data...

    // close the input file
    ininin.close();
    inStream.close();

    // decide on an array and initialize...
    double nRowsDouble = ((maxLatitude - minLatitude) / cellSize) + 1;
    double nColsDouble = ((maxLongitude - minLongitude) / cellSize) + 1;

    int nRows = (int) Math.round(nRowsDouble);
    int nCols = (int) Math.round(nColsDouble);

    System.out.println(
        "nRows = " + nRowsDouble + "/" + nRows + " ; nCols = " + nColsDouble + "/" + nCols);

    double[][] valuesGeographic = new double[nRows][nCols];
    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
      for (int colIndex = 0; colIndex < nCols; colIndex++) {
        valuesGeographic[rowIndex][colIndex] = missingValue;
      }
    }

    // run through it again, putting the values into a raster-like array
    for (int lineNumber = 0; lineNumber < nLinesTotal; lineNumber++) {
      row = (int) Math.round((latitude[lineNumber] - minLatitude) / cellSize);
      col = (int) Math.round((longitude[lineNumber] - minLongitude) / cellSize);

      //      System.out.println("lat = " + latitude[lineNumber] + "; lon = " +
      // longitude[lineNumber] + "; row = " + row + "; col = " + col + "; value to store = " +
      // valueList[lineNumber]);

      valuesGeographic[row][col] = valueList[lineNumber];
    }

    // now try to output it as an arc ascii file...
    String outputFile = filename.substring(0, filename.lastIndexOf(".")) + "_arc.asc";

    FileOutputStream outArcStream = new FileOutputStream(outputFile);
    PrintWriter outArcWriterObject = new PrintWriter(outArcStream);

    // write the headers
    outArcWriterObject.print("ncols         " + nCols + "\n");
    outArcWriterObject.print("nrows         " + nRows + "\n");
    outArcWriterObject.print("xllcorner     " + (minLongitude - cellSize / 2.0) + "\n");
    outArcWriterObject.print("yllcorner     " + (minLatitude - cellSize / 2.0) + "\n");
    outArcWriterObject.print("cellsize      " + cellSize + "\n");
    outArcWriterObject.print("NODATA_value  " + (int) missingValue + "\n");

    for (int rowIndex = nRows - 1; rowIndex >= 0; rowIndex--) {
      for (int colIndex = 0; colIndex < nCols; colIndex++) {
        outArcWriterObject.println(valuesGeographic[rowIndex][colIndex]);
      }
    }

    outArcWriterObject.flush();
    outArcWriterObject.close();
    outArcStream.close();

    Date endTime = new Date();

    double duration = (endTime.getTime() - startTime.getTime()) / 1000.0;

    System.out.println(
        "Finished execution! duration = "
            + duration
            + " seconds or "
            + (duration / 60)
            + " minutes or "
            + (duration / 3600)
            + " hours");
  } // main
}
