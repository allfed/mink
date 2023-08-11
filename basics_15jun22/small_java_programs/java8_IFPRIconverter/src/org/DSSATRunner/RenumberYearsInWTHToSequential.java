package org.DSSATRunner;

// import java.io.*;

import org.R2Useful.*;

public class RenumberYearsInWTHToSequential {

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

    String sourcePath = commandLineOptions[0];
    String destinationPath = commandLineOptions[1];
    int firstYearToUse = Integer.parseInt(commandLineOptions[2]);

    String[] originalAsArray = FunTricks.readTextFileToArray(sourcePath);
    String[] outputAsArray = new String[originalAsArray.length];

    /*
    *WEATHER DATA :

    @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
      RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
    @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
    90274   9.7   4.0   3.1   1.5  -999  -999
    90275  12.5   5.4   1.8   1.9  -999  -999
    */

    // do the first few header lines
    outputAsArray[0] = originalAsArray[0];
    outputAsArray[1] = originalAsArray[1];
    outputAsArray[2] = originalAsArray[2];
    outputAsArray[3] = originalAsArray[3];
    outputAsArray[4] = originalAsArray[4];

    String originalDatePart, restOfLine;
    int originalDate, newDate, yearToUse, dayToUse, originalYear, originalDayOfYear;

    int magicFirstLineToWorkOn = 5;
    int magicLengthOfDate = 5;

    // i think what we will do is figure out which day of the year is represented by the
    // very first entry. then we will just run sequentially from there, taking into account leap
    // years
    // and hoping for the best...

    // initialize year with value requested
    yearToUse = firstYearToUse;
    // initialize day with whatever shows up in the file first...
    originalDatePart = originalAsArray[magicFirstLineToWorkOn].substring(0, magicLengthOfDate);
    originalDate = Integer.parseInt(originalDatePart.trim());
    // the date is YYddd, so if we do integer division by a thousand, we get the year...
    originalYear = originalDate / 1000;

    // subtract off the year to get the days...
    originalDayOfYear = originalDate - 1000 * originalYear;

    // check for weirdness: we might start at the very end of a leap year in the data written down,
    // but
    // not wish to start our fake year in a leap year....
    if (originalDayOfYear > 365) {
      // we will just force it back to 365 which should always work...
      dayToUse = 365;
    } else {
      dayToUse = originalDayOfYear;
    }
    for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {

      // we don't care about the date, so just figure everything else out....
      restOfLine = originalAsArray[lineIndex].substring(magicLengthOfDate);

      // now comes the fun part, deciding what to write down...

      // are we in a leap year for the yearToUse?
      // Beware the MAGIC ASSUMPTION!!! we think DSSAT will interpret 00 as 2000 which *was* a leap
      // year
      // but 2100 will not be. grrr... two digit dates. so don't worry about century correction...

      // ok, so we need to decide when to turn the fake year over. that will happen when the day to
      // use
      // is bigger than the usual 365.
      if (dayToUse > 365) {
        // normal years need to bump up the year and reset the day while leap years should carry on
        // to 366.
        if (yearToUse % 4 != 0) {
          // normal year
          yearToUse = (yearToUse + 1) % 100;
          dayToUse = 1;
        } else {
          // leap year: reset after 366 instead of 365
          if (dayToUse > 366) {
            yearToUse = (yearToUse + 1) % 100;
            dayToUse = 1;
          } // reset leap years
        } // are we in a leap year?
      } // do we need to reset...

      // ok, now we can put together the fake date code...
      newDate = 1000 * yearToUse + dayToUse;

      outputAsArray[lineIndex] =
          FunTricks.padStringWithLeadingSpaces(Integer.toString(newDate), magicLengthOfDate)
              + restOfLine;

      // bump up the day counter
      dayToUse++;
    } // for lineIndex

    FunTricks.writeStringArrayToFile(outputAsArray, destinationPath);

    //////////////
    // all done //
    //////////////

  } // main
}
