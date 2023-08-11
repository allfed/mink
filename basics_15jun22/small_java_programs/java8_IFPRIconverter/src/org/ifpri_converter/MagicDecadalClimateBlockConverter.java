package org.ifpri_converter;

import java.util.Date;
import org.R2Useful.*;
// import java.util.concurrent.*;

public class MagicDecadalClimateBlockConverter {

  // this is to take some decadal monthly climate averages that tingju uses and
  // convert them into grass maps so that i can use them

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
          "Usage: org.ifpri_converter.MagicDecadalClimateBlockConverter input_filename"
              + " output_filename n_years_in-block delimiter\n"
              + " ");
      System.exit(1);
    }

    String inputFilename = commandLineOptions[0];
    String outputFilename = commandLineOptions[1];
    int nYearsInBlock = Integer.parseInt(commandLineOptions[2]);
    String delimiter = commandLineOptions[3];

    // Beware the MAGIC NUMBER!!! there is a header line before the data lines
    int blockLength = nYearsInBlock + 1;

    // MAGIC NUMBERS!!!!

    String[] rawData = FunTricks.readTextFileToArray(inputFilename);

    String[] splitLine = null;

    // let's do this in two pieces:
    // first: read into giant arrays
    // second: write it back out in the format we want...

    // Beware the MAGIC NUMBER!!! there is a header line before the data lines
    int nBlocksGuess = rawData.length / blockLength;

    // check for oddities

    if (rawData.length % blockLength != 0) {
      System.out.println(
          "Warning! Unexpected file length: "
              + rawData.length
              + " % "
              + blockLength
              + " = "
              + (rawData.length % blockLength)
              + "; i.e., != 0");
    }

    int[] cellID = new int[nBlocksGuess];
    double[] latitude = new double[nBlocksGuess];
    double[] longitude = new double[nBlocksGuess];

    // cell / year / month
    // Beware the MAGIC NUMBER!!! number of months in the year
    double[][][] climateValues = new double[nBlocksGuess][nYearsInBlock][12];

    int headerLineIndex = -1;
    int firstDataLineIndex = -2;
    int yearlyLineIndex = -3;
    String[] headerSplit = null;

    System.out.println("-- reading data into memory --");
    for (int blockIndex = 0; blockIndex < nBlocksGuess; blockIndex++) {
      // figure out the starting points
      headerLineIndex = blockIndex * blockLength;
      firstDataLineIndex = headerLineIndex + 1;

      // grab the header line
      headerSplit = rawData[headerLineIndex].split(delimiter);

      // process the header line
      cellID[blockIndex] = Integer.parseInt(headerSplit[0]);
      longitude[blockIndex] = Double.parseDouble(headerSplit[1]);
      latitude[blockIndex] = Double.parseDouble(headerSplit[2]);

      // get the real data
      for (int yearIndex = 0; yearIndex < nYearsInBlock; yearIndex++) {
        // figure out what we're working on
        yearlyLineIndex = firstDataLineIndex + yearIndex;

        // grab the data
        splitLine = rawData[yearlyLineIndex].split(delimiter);

        // Beware the MAGIC NUMBER!!! 12 months in a year
        for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
          climateValues[blockIndex][yearIndex][monthIndex] =
              Double.parseDouble(splitLine[monthIndex]);
        } // monthIndex
      } // yearIndex
    } // blockIndex

    // now, we try to write it back out in a sensible way

    boolean appendPlease = false;
    String outputFileToUse = null;
    String lineToRecord = null;

    // we will go year by year and put all the months in a single file for that year
    for (int yearIndex = 0; yearIndex < nYearsInBlock; yearIndex++) {
      // build the file name
      outputFileToUse = outputFilename + "_" + yearIndex + ".txt";

      System.out.println("  -- writing " + outputFileToUse + " --");

      for (int blockIndex = 0; blockIndex < nBlocksGuess; blockIndex++) {

        if (blockIndex == 0) {
          appendPlease = false;
        } else {
          appendPlease = true;
        }

        // start filling in the info we want to record: header info
        lineToRecord = longitude[blockIndex] + " " + latitude[blockIndex];

        // now the real data
        for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
          lineToRecord += " " + climateValues[blockIndex][yearIndex][monthIndex];
        } // monthIndex

        FunTricks.appendLineToTextFile(lineToRecord, outputFileToUse, appendPlease);
      } // blockIndex
    } // yearIndex

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
