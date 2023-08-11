package org.ifpri_converter;

import java.io.BufferedReader;
// import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Date;
import org.R2Useful.*;
// import java.util.concurrent.*;

public class MagicConverterGetCountryRasters28Oct14 {

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

    String delimiter = "\t";

    // MAGIC NUMBERS!!!!

    double cellSize = 1.0 / 12.0; // 0.0833334; // hard-coded... now trying perfectly even
    int pixelField = 0; // index; it is actually #3
    int valueField = 1; // index; it is actually #8

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
    int[] valueList = new int[nLinesTotal];

    StringToUniqueCode uniqueCodeGizmo = new StringToUniqueCode(300);

    // furthermore, let's just put everything on a common grid which is
    // hopefully the correct one. that way we don't have to compute lat/lon
    // and everything will get the same rounding errors upon importation...
    int nRows = 180 * 12; // 180 degrees * 60 minutes / 5 minutes-per-cell
    int nCols = 360 * 12; // 360 degrees * 60 minutes / 5 minutes-per-cell

    double[][] valuesGeographic = new double[nRows][nCols];
    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
      for (int colIndex = 0; colIndex < nCols; colIndex++) {
        valuesGeographic[rowIndex][colIndex] = missingValue;
      }
    }

    int rawRow, rawCol;

    for (int lineNumber = 0; lineNumber < nLinesTotal; lineNumber++) {

      // read the line in
      lineContents = ininin.readLine();

      // split it up using the provided delimiter
      splitLine = lineContents.split(delimiter);

      // pull out the bits we need
      pixelCode = Integer.parseInt(splitLine[pixelField].trim());
      //      valueList[lineNumber] = Double.parseDouble(splitLine[valueField].trim());
      valueList[lineNumber] = uniqueCodeGizmo.getCode(splitLine[valueField].trim());

      rawRow = pixelCode / 10000; // hoping for integer truncation...
      rawCol =
          pixelCode
              - (10000
                  * rawRow); // i think we have a N/S flip going on for this particular
                             // implementation...
      // and we need to get to indices from numbers
      row = nRows - rawRow - 1; // hoping for integer truncation...
      col = rawCol - 1;

      valuesGeographic[row][col] = valueList[lineNumber];
    } // end first pass through the data...

    // close the input file
    ininin.close();
    inStream.close();

    // now try to output it as an arc ascii file...
    String outputFile = filename.substring(0, filename.lastIndexOf(".")) + "_countries.asc";
    String outputDictionaryFile =
        filename.substring(0, filename.lastIndexOf(".")) + "_countries_dictionary.asc";

    FileOutputStream outArcStream = new FileOutputStream(outputFile);
    PrintWriter outArcWriterObject = new PrintWriter(outArcStream);

    // write the headers
    outArcWriterObject.print("ncols         " + nCols + "\n");
    outArcWriterObject.print("nrows         " + nRows + "\n");
    //    outArcWriterObject.print("xllcorner     " + (minLongitude - cellSize/2.0) + "\n");
    //    outArcWriterObject.print("yllcorner     " + (minLatitude  - cellSize/2.0) + "\n");
    // somehow, it appears that we are half a pixel off... let's try that and see what happens
    outArcWriterObject.print("xllcorner     " + (-180) + "\n");
    outArcWriterObject.print("yllcorner     " + (-90) + "\n");
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

    String[] dictionaryWithIndices = new String[uniqueCodeGizmo.getNStored()];
    for (int dictIndex = 0; dictIndex < uniqueCodeGizmo.getNStored(); dictIndex++) {
      dictionaryWithIndices[dictIndex] =
          dictIndex + "\t" + uniqueCodeGizmo.getDictionaryList()[dictIndex];
    }

    FunTricks.writeStringArrayToFile(dictionaryWithIndices, outputDictionaryFile);

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
