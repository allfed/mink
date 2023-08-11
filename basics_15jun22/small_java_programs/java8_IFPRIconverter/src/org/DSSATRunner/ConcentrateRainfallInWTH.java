package org.DSSATRunner;

// import java.io.*;

// import java.text.DecimalFormat;
// import java.text.NumberFormat;

import java.io.File;
import org.R2Useful.*;
// import org.DSSATRunner.DSSATHelperMethods;

public class ConcentrateRainfallInWTH {

  // the idea here is that i have some reordered/resampled daily weather so the year numbers are all
  // screwed up...
  // thus, i want to keep them in the order presented, but renumber the days/years to be
  // sequential...

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    String sourceDirectory = commandLineOptions[0];
    String destinationPath = commandLineOptions[1];
    double rainyDayMinimumRainfallThreshold = Double.parseDouble(commandLineOptions[2]);

    // first, let's check if the destination file already exists and advise that we are overwriting
    // it...
    File outputFileObject = new File(destinationPath);
    if (!outputFileObject.exists()) {
      System.out.println(
          "intended output [" + outputFileObject.getCanonicalPath() + "] has failed to exist.");
      throw new Exception();
    }

    // we want to grab everything inside the directory, which will be assumed to be clean
    System.out.println("--- !!! we assume that everything inside the requested directory !!! ---");
    System.out.println("--- !!! [" + sourceDirectory + "] is useful !!! ---");

    File directoryObject = new File(sourceDirectory);

    File[] inputFiles = directoryObject.listFiles();

    // magic numbers for spacing
    final int magicFirstLineToWorkOn = 5;
    final int magicLengthOfDate = 5;
    final int sradEnd = 11;
    final int tmaxEnd = 17;
    final int tminEnd = 23;
    final int rainEnd = 29;

    /// initializations
    String[] originalAsArray = null;
    String[] outputAsArray = null;
    int originalDate;

    // the data stuff
    double rainfallSoFar, rainfallToUse;

    double sradHere, tmaxHere, tminHere, rainHere;

    String destinationFile = "";

    long startTime = System.currentTimeMillis();

    for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {

      destinationFile =
          destinationPath
              + File.separator
              + "concentrated_"
              + rainyDayMinimumRainfallThreshold
              + "_"
              + inputFiles[fileIndex].getName();

      originalAsArray = FunTricks.readTextFileToArray(inputFiles[fileIndex].getCanonicalPath());

      outputAsArray = new String[originalAsArray.length];

      //		String originalDatePart, restOfLine;
      //		int originalDate, newDate, yearToUse, dayToUse, originalYear, originalDayOfYear;

      /*
      *WEATHER DATA :

      @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
        RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
      @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
      90274   9.7   4.0   3.1   1.5  -999  -999
      90275  12.5   5.4   1.8   1.9  -999  -999
      01234567890123456789012345678901234567890
      0         1         2         3         4
      */

      // the plan is to read through all the days and look at the rainfall. if the rainfall is below
      // threshold, we
      // keep accumulating it up until it is enough to be worthwhile and then dump it all out at
      // that point.

      // first, let us grab the stated geographic and yearly info...
      //		lineThreeInPieces =
      // FunTricks.parseRepeatedDelimiterToArray(originalAsArray[magicGeographyAndYearlyLineIndex],
      // " ");
      //
      //		latitude  = Double.parseDouble(lineThreeInPieces[1]);
      //		longitude = Double.parseDouble(lineThreeInPieces[2]);
      ////		elevation = Double.parseDouble(lineThreeInPieces[3]);
      //		tavStated = Double.parseDouble(lineThreeInPieces[4]);
      //		ampStated = Double.parseDouble(lineThreeInPieces[5]);
      ////		System.out.println("lat = " + latitude + "; long = " + longitude + "; elev = " +
      // elevation + "; tav = " + tavStated + "; amp = " + ampStated);

      // do the first few header lines
      outputAsArray[0] = originalAsArray[0];
      outputAsArray[1] = originalAsArray[1];
      outputAsArray[2] = originalAsArray[2];
      outputAsArray[3] = originalAsArray[3];
      outputAsArray[4] = originalAsArray[4];

      // initialize the rainfall checker thing
      rainfallSoFar = 0.0;
      for (int lineIndex = magicFirstLineToWorkOn;
          lineIndex < originalAsArray.length;
          lineIndex++) {

        // figure out the day we're looking at...
        originalDate =
            Integer.parseInt(originalAsArray[lineIndex].substring(0, magicLengthOfDate).trim());

        sradHere =
            Double.parseDouble(
                originalAsArray[lineIndex].substring(magicLengthOfDate, sradEnd).trim());
        tmaxHere =
            Double.parseDouble(originalAsArray[lineIndex].substring(sradEnd, tmaxEnd).trim());
        tminHere =
            Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd, tminEnd).trim());
        rainHere =
            Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd, rainEnd).trim());

        rainfallSoFar += rainHere;

        if (rainfallSoFar >= rainyDayMinimumRainfallThreshold) {
          // open the windows of heaven
          rainfallToUse = rainfallSoFar;

          // reset the cistern
          rainfallSoFar = 0;
        } else {
          // store it up for another day
          rainfallToUse = 0;
        } // if (rainfallSoFar >= rainyDayMinimumRainfallThreshold)

        // ok, now we get to write it out....
        //	    @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
        //	    90274   9.7   4.0   3.1   1.5  -999  -999
        //	    90275  12.5   5.4   1.8   1.9  -999  -999
        //	    01234567890123456789012345678901234567890
        //	    0         1         2         3         4

        outputAsArray[lineIndex] =
            FunTricks.padStringWithLeadingSpaces(Integer.toString(originalDate), magicLengthOfDate)
                + " "
                + FunTricks.fitInNCharacters(sradHere, 5)
                + " "
                + FunTricks.fitInNCharacters(tmaxHere, 5)
                + " "
                + FunTricks.fitInNCharacters(tminHere, 5)
                + " "
                + FunTricks.fitInNCharacters(rainfallToUse, 5)
                + " "
                + " -999  -999";
      } // for lineIndex

      FunTricks.writeStringArrayToFile(outputAsArray, destinationFile);

      if (fileIndex % (inputFiles.length / 400 + 1) == 0) {
        double timeMinutes = ((System.currentTimeMillis() - startTime) / 1000.0 / 60);

        System.out.println(
            (fileIndex + 1)
                + "/"
                + inputFiles.length
                + " = "
                + FunTricks.fitInNCharacters((100 * (fileIndex + 1.0) / inputFiles.length), 5)
                + "\t"
                + ((System.currentTimeMillis() - startTime) / 1000)
                + "s\t"
                + timeMinutes
                + "m\t"
                + (timeMinutes * ((inputFiles.length * 1.0) / (fileIndex + 1)) - timeMinutes)
                + "m remaining");
      }
    } // end giant file loop

    //////////////
    // all done //
    //////////////

  } // main
}
