package org.ifpri_converter;

import java.util.Date;
import org.R2Useful.*;
// import java.util.concurrent.*;

public class MagicHCSeqReader {

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
          "Usage: org.ifpri_converter.MagicHCSeqReader input_filename output_filename\n ");
      System.exit(1);
    }

    String inputFilename = commandLineOptions[0];
    String outputFilename = commandLineOptions[1];

    // MAGIC NUMBERS!!!!

    double cellSize = 0.5; // hard-coded...
    String delimiter = "\t";

    double nCols = 360 / cellSize;
    //    double nRows = 180 / cellSize;

    String[] rawData = FunTricks.readTextFileToArray(inputFilename);

    String[] splitLine = null;

    int pixelCode = -1;
    double row, col;

    double value = Double.NaN;

    double latitude, longitude;

    boolean appendPlease = false;

    for (int lineIndex = 0; lineIndex < rawData.length; lineIndex++) {
      splitLine = rawData[lineIndex].split(delimiter);
      pixelCode = Integer.parseInt(splitLine[0]);
      value = Double.parseDouble(splitLine[1]);

      row = Math.floor(pixelCode / nCols);
      col = pixelCode - row * nCols;

      latitude = 90 - cellSize / 2 - cellSize * row;
      longitude = -180 + cellSize / 2 + cellSize * col;

      if (lineIndex == 0) {
        appendPlease = false;
      } else {
        appendPlease = true;
      }

      FunTricks.appendLineToTextFile(
          latitude + "\t" + longitude + "\t" + value, outputFilename, appendPlease);
    }

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
