package org.ifpri_converter;

import java.io.BufferedReader;
// import java.io.File;
import java.io.File;
// import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
// import java.io.LineNumberReader;
import java.io.PrintWriter;
// import java.io.RandomAccessFile;
import java.util.Date;
// import java.util.concurrent.*;

// import ncsa.d2k.modules.projects.dtcheng.matrix.MultiFormatMatrix;

public class DynamicsTextFileSplitter {

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
      System.out.println(
          "Usage: org.ifpri_converter.DynamicsTextFileSplitter input_basename output_basename"
              + " skip_header_line\n"
              + " \n"
              + "The assumption is that the final column is some sort of unique positive integer"
              + " identifier.\n"
              + "The rows should be sorted such that the identifiers appear together in blocks.\n"
              + "Each block will be placed in its own text file with the identifier stripped\n"
              + "and placed in the name.\n"
              + "\n"
              + "skip_header_line should be any non-empty string if the first line of the data file"
              + " contains column names");
      System.exit(1);
    }

    String inputBasename = commandLineOptions[0];
    String outputBasename = commandLineOptions[1];

    boolean skipHeaderLine = false;

    if (commandLineOptions.length > 2) {
      skipHeaderLine = true;
    }

    ////////////////////////////////////////////
    // do the work
    ////////////////////////////////////////////

    // read in the header file...
    String fileName = inputBasename + ".txt";
    String infoFileName = inputBasename + ".info.txt";

    // create the "File"
    File infoFileObject = new File(infoFileName);
    // create a character input stream (FileReader inherits from
    // InputStreamReader
    FileReader infoStream = new FileReader(infoFileObject);
    // filter the input stream, buffers characters for efficiency
    BufferedReader infoReader = new BufferedReader(infoStream);
    // read the first line
    String lineContents = infoReader.readLine();
    int indexOfEnd = 0;

    indexOfEnd = lineContents.indexOf("=") - 2;
    long nRows = Long.parseLong(lineContents.substring(0, indexOfEnd));

    // read the second line
    lineContents = infoReader.readLine();
    indexOfEnd = lineContents.indexOf("=") - 2;
    long nCols = Long.parseLong(lineContents.substring(0, indexOfEnd));

    // read the third line (who cares?)
    lineContents = infoReader.readLine();
    // read the fourth line
    lineContents = infoReader.readLine();
    indexOfEnd = lineContents.indexOf("=") - 2;
    int formatIndex = Integer.parseInt(lineContents.substring(0, indexOfEnd));

    // read the fifth line
    lineContents = infoReader.readLine();
    String delimiterString = lineContents.substring(0, indexOfEnd);

    // close up shop on that part...
    infoReader.close();
    infoStream.close();

    // create the "File"
    File inputDataFileObject = new File(fileName);
    // create a character input stream (FileReader inherits from
    // InputStreamReader
    FileReader inputDataStream = new FileReader(inputDataFileObject);
    // filter the input stream, buffers characters for efficiency
    BufferedReader inputDataReader = new BufferedReader(inputDataStream);

    // actually try to read in the goodies and crank it out and write it
    // down...

    ///////////////
    // here are the writer inits
    ///////////////
    File infoFileToWrite = null;
    FileWriter outInfoStream = null;
    PrintWriter outInfoWriterObject = null;

    File dataFileToWrite = null;
    FileWriter outDataStream = null;
    PrintWriter outDataWriterObject = null;

    int finalDelimiterIndex = -1;

    int finalColIndex = (int) (nCols - 1);
    //		String[] splitLineContents = null;

    int previousCode = -993924251;
    int currentCode = previousCode - 1; // to ensure they start out with different values

    // if necessary, skip the labels header row...
    if (skipHeaderLine) {
      lineContents = inputDataReader.readLine();
    }

    int rowsInThisBlock = 0; // this actually needs to be zero
    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
      // read it in...
      lineContents = inputDataReader.readLine();

      finalDelimiterIndex = lineContents.lastIndexOf(delimiterString);
      currentCode = (int) Double.parseDouble(lineContents.substring(finalDelimiterIndex + 1));

      //			System.out.println("LC = [" + lineContents + "], code = " + currentCode);

      if ((currentCode != previousCode) || (rowIndex == 0)) {
        // we need a new block, but only if we are not on the first row....
        if (rowIndex != 0) {
          //					System.out.println("-- in inner part of main if --" + rowIndex);

          // close out the text file
          outDataWriterObject.flush();
          outDataWriterObject.close();

          // write the info file
          infoFileToWrite = new File(outputBasename + "_" + previousCode + ".info.txt");
          outInfoStream = new FileWriter(infoFileToWrite);
          outInfoWriterObject = new PrintWriter(outInfoStream);

          outInfoWriterObject.print(rowsInThisBlock + "\t = Number of Rows\n");
          outInfoWriterObject.print(finalColIndex + "\t = Number of Columns\n");
          outInfoWriterObject.print(
              (rowsInThisBlock * finalColIndex) + "\t = Total Number of Elements\n");
          outInfoWriterObject.print(
              formatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
          outInfoWriterObject.print(
              delimiterString + "\t = The string used to delimit elements in the Rows");

          outInfoWriterObject.flush();
          outInfoWriterObject.close();
        }

        // reset the row counter
        rowsInThisBlock = 0;

        // open the new file
        dataFileToWrite = new File(outputBasename + "_" + currentCode + ".txt");
        outDataStream = new FileWriter(dataFileToWrite);
        outDataWriterObject = new PrintWriter(outDataStream);

        //				System.out.println("-- in outer part of main if --" + rowIndex);
      }

      // write out the current line without the last value
      outDataWriterObject.println(lineContents.substring(0, finalDelimiterIndex));

      // reset the previous code
      previousCode = currentCode;

      // bump up the counter
      rowsInThisBlock++;
    }

    // close out the last block...
    // close out the text file
    outDataWriterObject.flush();
    outDataWriterObject.close();

    // write the info file
    infoFileToWrite = new File(outputBasename + "_" + previousCode + ".info.txt");
    outInfoStream = new FileWriter(infoFileToWrite);
    outInfoWriterObject = new PrintWriter(outInfoStream);

    outInfoWriterObject.print(rowsInThisBlock + "\t = Number of Rows\n");
    outInfoWriterObject.print(finalColIndex + "\t = Number of Columns\n");
    outInfoWriterObject.print(
        (rowsInThisBlock * finalColIndex) + "\t = Total Number of Elements\n");
    outInfoWriterObject.print(
        formatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
    outInfoWriterObject.print(
        delimiterString + "\t = The string used to delimit elements in the Rows");

    outInfoWriterObject.flush();
    outInfoWriterObject.close();

    // close the input file
    inputDataReader.close();
    inputDataStream.close();

    ////////////////////////////////////////////
    // report time it took
    ////////////////////////////////////////////

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
