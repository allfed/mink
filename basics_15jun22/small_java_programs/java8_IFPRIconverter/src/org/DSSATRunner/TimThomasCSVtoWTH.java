package org.DSSATRunner;

// import java.io.*;

// import java.text.DecimalFormat;
// import java.text.NumberFormat;

import java.io.File;
import org.R2Useful.*;
// import org.DSSATRunner.DSSATHelperMethods;

public class TimThomasCSVtoWTH {

  // this is hacked from ConcentrateRainfallInWTH

  // the idea is that i want to fix up things that sometimes trip up DSSAT:
  // * TMIN >= TMAX
  // * lots of low rainfall (doesn't actually matter, but i can leave the functionality in there)
  // * SRAD <= 0

  private static int numberOfDaysBeforeMonthNumber(int monthNumber, int yearNumber) {

    if (monthNumber == 1) {
      return 0;
    } else if (monthNumber == 2) {
      return 31;
    } else if (monthNumber == 3) {
      if (yearNumber % 4 == 0) {
        return 60;
      } else {
        return 59;
      }
    } else if (monthNumber == 4) {
      if (yearNumber % 4 == 0) {
        return 91;
      } else {
        return 90;
      }
    } else if (monthNumber == 5) {
      if (yearNumber % 4 == 0) {
        return 121;
      } else {
        return 120;
      }
    } else if (monthNumber == 6) {
      if (yearNumber % 4 == 0) {
        return 152;
      } else {
        return 151;
      }
    } else if (monthNumber == 7) {
      if (yearNumber % 4 == 0) {
        return 182;
      } else {
        return 181;
      }
    } else if (monthNumber == 8) {
      if (yearNumber % 4 == 0) {
        return 213;
      } else {
        return 212;
      }
    } else if (monthNumber == 9) {
      if (yearNumber % 4 == 0) {
        return 244;
      } else {
        return 243;
      }
    } else if (monthNumber == 10) {
      if (yearNumber % 4 == 0) {
        return 274;
      } else {
        return 273;
      }
    } else if (monthNumber == 11) {
      if (yearNumber % 4 == 0) {
        return 305;
      } else {
        return 304;
      }
    } else if (monthNumber == 12) {
      if (yearNumber % 4 == 0) {
        return 335;
      } else {
        return 334;
      }
    } else {
      return -1;
    }
  }

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    String sourceDirectory = commandLineOptions[0];
    String sourceSuffix = commandLineOptions[1];
    String destinationPath = commandLineOptions[2];
    double rainyDayMinimumRainfallThreshold = Double.parseDouble(commandLineOptions[3]);
    double sradMinimumThreshold = Double.parseDouble(commandLineOptions[4]);
    double tDiffToEnforce = Double.parseDouble(commandLineOptions[5]);

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
    System.out.println("--- !!! ending in [" + sourceSuffix + "] ---");
    System.out.println("--- !!! [" + sourceDirectory + "] is useful !!! ---");

    File directoryObject = new File(sourceDirectory);

    File[] inputFiles = directoryObject.listFiles();

    // magic numbers
    final String weatherSuffix = ".WTH";

    // magic numbers for spacing
    final int magicLengthOfDate = 5;

    final int longitudeColumn = 0;
    final int latitudeColumn = 1;
    final int yearColumn = 2;
    final int monthColumn = 3;
    final int dayColumn = 4;
    final int dayOfYearColumn = 5;
    final int rainColumn = 6;
    final int tmaxColumn = 7;
    final int tminColumn = 8;
    final int sradColumn = 9;

    final int magicYearOffsetToGetToTwoDigits = 2000;
    final int nLinesInHeader = 5;

    /// initializations
    String[] originalAsArray = null;
    String[] outputHeaderAsArray = null;
    String[] outputAsArray = null;
    String[] finalCombinedOutputArray = null;
    int originalDate = -1;
    int nGoodDays = -5;

    double tav, amp;
    double thisAverageTmax, thisAverageTmin, thisTave;

    final int nMonths = 12;

    DescriptiveStatisticsUtility annualTmax = new DescriptiveStatisticsUtility(true);
    DescriptiveStatisticsUtility annualTmin = new DescriptiveStatisticsUtility(true);
    DescriptiveStatisticsUtility annualTave = new DescriptiveStatisticsUtility(true);
    DescriptiveStatisticsUtility[] monthlyTmax = new DescriptiveStatisticsUtility[nMonths];
    DescriptiveStatisticsUtility[] monthlyTmin = new DescriptiveStatisticsUtility[nMonths];

    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
      monthlyTmax[monthIndex] = new DescriptiveStatisticsUtility(true);
      monthlyTmin[monthIndex] = new DescriptiveStatisticsUtility(true);
    }

    // the data stuff
    double rainfallSoFar, rainfallToUse;

    double longitude = Double.NEGATIVE_INFINITY, latitude = Double.NEGATIVE_INFINITY;
    double previousLongitude = Double.NaN, previousLatitude = Double.NaN;
    int year, month, day, dayOfYear;
    double sradHere, tmaxHere, tminHere, rainHere, sradToUse, tmaxToUse = -9998.5, tminToUse;

    int dateToUse;

    String destinationFile = "";

    long startTime = System.currentTimeMillis();

    for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {

      // check if this is a CSV
      String filenameToCheckForCSV = inputFiles[fileIndex].getName();
      if (!filenameToCheckForCSV.endsWith(sourceSuffix)) {
        // we shall just skip this file....
        continue;
      }

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

      // initialize some stuff
      rainfallSoFar = 0.0;
      nGoodDays = 0;
      previousLongitude = Double.NaN;
      previousLatitude = Double.NaN;

      String[] splitLine = null;
      System.out.println("           =====> starting " + inputFiles[fileIndex].getCanonicalPath());

      for (int lineIndex = 0; lineIndex < originalAsArray.length; lineIndex++) {

        splitLine = originalAsArray[lineIndex].split(",");

        // look at the very first thing (longitude) and if it is not parsable as a double then we
        // bail out
        // figure out the day we're looking at...
        try {
          longitude = Double.parseDouble(splitLine[longitudeColumn]);
        } catch (Exception eee) {
          // we have hit something we can't read. let us skip over it because it should be a
          // header/footer line
          System.out.println(inputFiles[fileIndex].getName());
          System.out.println("bad line [" + lineIndex + "] = [" + originalAsArray[lineIndex] + "]");

          continue;
        }

        //	    originalDate = Integer.parseInt(originalAsArray[lineIndex].substring(0,
        // magicLengthOfDate).trim());
        latitude = Double.parseDouble(splitLine[latitudeColumn]);

        // check and see if we are on a new location or not
        if (Double.isNaN(previousLatitude)) {
          previousLatitude = latitude;
          previousLongitude = longitude;

          System.out.println(
              "initializing what is probably the very first line: pLat = "
                  + previousLatitude
                  + "; pLon = "
                  + previousLongitude
                  + "; lI = "
                  + lineIndex);
        }

        //		System.out.println("lI= " + lineIndex + "\tpLat=" + previousLatitude + "\tlat=" +
        // latitude + "\tpLon=" + previousLongitude + "\tlon=" + longitude);
        if (previousLongitude != longitude || previousLatitude != latitude) {
          // write down what we have seen, before we do anything...

          outputHeaderAsArray = new String[nLinesInHeader];

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

          tav = annualTave.getMean();

          // figure out the highest monthly average
          // figure out the lowest monthly average
          double highestMonthlyAverage = Double.MIN_VALUE;
          double lowestMonthlyAverage = Double.MAX_VALUE;
          for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
            // extract the values
            thisAverageTmax = monthlyTmax[monthIndex].getMean();
            thisAverageTmin = monthlyTmin[monthIndex].getMean();

            // compute the average for this month
            thisTave = (thisAverageTmax + thisAverageTmin) / 2.0;

            // update the candidate max/min monthly values
            // i only care about the value, not the index at this point
            if (thisTave > highestMonthlyAverage) {
              highestMonthlyAverage = thisTave;
            }
            if (thisTave < lowestMonthlyAverage) {
              lowestMonthlyAverage = thisTave;
            }

            //		    System.out.println("this = " + thisTave + "; high = " + highestMonthlyAverage +
            // "; low = " + lowestMonthlyAverage);
          }

          // i think i am doing this right
          // the tamp or amp is very poorly defined. when i chased down the references, the code
          // was more inspired by than following the book's exposition
          // anyways, the important thing is for the amplitude to be the full range of the periodic
          // function rather than a true amplitude. for example, in the source code, it gets divided
          // by two
          // before multiplying the cosine. but only in some files.... :(
          //		STEMP.for:      TA = TAV + TAMP * COS(ALX) / 2.0
          //		STEMP.for:        ST(L) = TAV + (TAMP / 2.0 * COS(ALX + ZD) + DT) * EXP(ZD)
          //		STEMP.for:      SRFTEMP = TAV + (TAMP / 2. * COS(ALX) + DT)
          //		STEMP.for:! TAMP     Amplitude of temperature function used to calculate soil
          //		STEMP.for:! TAV      Average annual soil temperature, used with TAMP to calculate

          amp = highestMonthlyAverage - lowestMonthlyAverage;

          //		System.out.println("tav = [" + tav + "]" + " [" + FunTricks.fitInNCharacters(tav, 5) +
          // "]");
          //		System.out.println("amp = [" + amp + "]" + " [" + FunTricks.fitInNCharacters(amp, 5) +
          // "]");

          outputHeaderAsArray[0] = "*WEATHER DATA :";
          outputHeaderAsArray[1] = "";
          outputHeaderAsArray[2] = "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT";
          outputHeaderAsArray[3] =
              "  XXXX   "
                  + FunTricks.fitInNCharacters(previousLatitude, 6)
                  + "   "
                  + FunTricks.fitInNCharacters(previousLongitude, 6)
                  + "  -999 "
                  + FunTricks.fitInNCharacters(tav, 5)
                  + " "
                  + FunTricks.fitInNCharacters(amp, 5)
                  + "  -999  -999";
          outputHeaderAsArray[4] = "@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D";

          //		System.out.println(" oHAA[3] = {" + outputHeaderAsArray[3] + "}");
          // create the new array for writing out to the file
          finalCombinedOutputArray = new String[nLinesInHeader + nGoodDays];

          // fill it up

          // the header
          for (int rowIndex = 0; rowIndex < nLinesInHeader; rowIndex++) {
            finalCombinedOutputArray[rowIndex] = outputHeaderAsArray[rowIndex];
          }

          // everything else
          for (int rowIndex = 0; rowIndex < nGoodDays; rowIndex++) {
            finalCombinedOutputArray[rowIndex + nLinesInHeader] = outputAsArray[rowIndex];
          }

          // get rid of the suffix and build the latitude and longitude into the filename
          destinationFile =
              destinationPath
                  + File.separator
                  + inputFiles[fileIndex].getName().replaceFirst(sourceSuffix, "")
                  + "_"
                  + previousLatitude
                  + "_"
                  + previousLongitude
                  + weatherSuffix;

          FunTricks.writeStringArrayToFile(finalCombinedOutputArray, destinationFile);

          System.out.println(
              "-----> finished writing " + destinationFile + "; nGoodDays = " + nGoodDays);

          // reset some stuff before we move on to the next location
          previousLatitude = latitude;
          previousLongitude = longitude;
          rainfallSoFar = 0.0;
          nGoodDays = 0;
        }

        year = Integer.parseInt(splitLine[yearColumn]);
        month = Integer.parseInt(splitLine[monthColumn]);
        day = Integer.parseInt(splitLine[dayColumn]);
        //	    dayOfYear = Integer.parseInt(splitLine[dayOfYearColumn]);
        // day of year got screwed up, so we need to do it manually based on month and day.....

        rainHere = Double.parseDouble(splitLine[rainColumn]);
        tmaxHere = Double.parseDouble(splitLine[tmaxColumn]);
        tminHere = Double.parseDouble(splitLine[tminColumn]);
        sradHere = Double.parseDouble(splitLine[sradColumn]);

        // build up the appropriate day of year, accounting for leap years....
        dayOfYear = numberOfDaysBeforeMonthNumber(month, year) + day;

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

        // the rainfall should be taken care of, so now to the boring ones...
        //		double sradHere, tmaxHere, tminHere, rainHere, sradToUse, tmaxToUse, tminToUse;
        if (sradHere < sradMinimumThreshold) {
          sradToUse = sradMinimumThreshold;
        } else {
          sradToUse = sradHere;
        }

        if (Math.abs(tmaxHere - tminHere) < tDiffToEnforce) {
          // checking that the spread is wide enough;
          // if we need to, keep the high and drop the low
          tminToUse = tmaxHere - tDiffToEnforce;
        } else if (tmaxHere < tminHere) {
          // check if they are in the wrong order
          // if so, swap them
          tminToUse = tmaxHere;
          tmaxToUse = tminHere;
        } else {
          // all should be well, so just keep them as is
          tminToUse = tminHere;
          tmaxToUse = tmaxHere;
        }

        dateToUse = (year - magicYearOffsetToGetToTwoDigits) * 1000 + dayOfYear;

        // accumulate some info about temperatures to come up with the yearly temperature and
        // amplitude
        annualTmax.useDoubleValue(tmaxHere);
        annualTmin.useDoubleValue(tminHere);
        annualTave.useDoubleValue((tmaxHere + tminHere) / 2);

        // this uses indices, so we need to subtract one off the month number
        monthlyTmax[month - 1].useDoubleValue(tmaxHere);
        monthlyTmin[month - 1].useDoubleValue(tminHere);

        // use the "nGoodDays" as the counter....
        outputAsArray[nGoodDays++] =
            FunTricks.padStringWithLeadingSpaces(Integer.toString(dateToUse), magicLengthOfDate)
                + " "
                + FunTricks.fitInNCharacters(sradToUse, 5)
                + " "
                + FunTricks.fitInNCharacters(tmaxToUse, 5)
                + " "
                + FunTricks.fitInNCharacters(tminToUse, 5)
                + " "
                + FunTricks.fitInNCharacters(rainfallToUse, 5)
                + " "
                + " -999  -999";
      } // for lineIndex

      // now, we have to write out the last one...
      // and, it will keep the actual lat/lon rather than working off of the "previous"

      //////////////////////////////////////////////////////////////////////////////
      // but: check to see if we have any lines at all because it might be empty....
      //////////////////////////////////////////////////////////////////////////////

      if (nGoodDays > 0) {
        // write down what we have seen, before we do anything...

        outputHeaderAsArray = new String[nLinesInHeader];

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

        tav = annualTave.getMean();

        // figure out the highest monthly average
        // figure out the lowest monthly average
        double highestMonthlyAverage = Double.MIN_VALUE;
        double lowestMonthlyAverage = Double.MAX_VALUE;
        for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
          // extract the values
          thisAverageTmax = monthlyTmax[monthIndex].getMean();
          thisAverageTmin = monthlyTmin[monthIndex].getMean();

          // compute the average for this month
          thisTave = (thisAverageTmax + thisAverageTmin) / 2.0;

          // update the candidate max/min monthly values
          // i only care about the value, not the index at this point
          if (thisTave > highestMonthlyAverage) {
            highestMonthlyAverage = thisTave;
          }
          if (thisTave < lowestMonthlyAverage) {
            lowestMonthlyAverage = thisTave;
          }

          //	System.out.println("this = " + thisTave + "; high = " + highestMonthlyAverage + "; low
          // = " + lowestMonthlyAverage);
        }

        // i think i am doing this right
        // the tamp or amp is very poorly defined. when i chased down the references, the code
        // was more inspired by than following the book's exposition
        // anyways, the important thing is for the amplitude to be the full range of the periodic
        // function rather than a true amplitude. for example, in the source code, it gets divided
        // by two
        // before multiplying the cosine. but only in some files.... :(
        //		STEMP.for:      TA = TAV + TAMP * COS(ALX) / 2.0
        //		STEMP.for:        ST(L) = TAV + (TAMP / 2.0 * COS(ALX + ZD) + DT) * EXP(ZD)
        //		STEMP.for:      SRFTEMP = TAV + (TAMP / 2. * COS(ALX) + DT)
        //		STEMP.for:! TAMP     Amplitude of temperature function used to calculate soil
        //		STEMP.for:! TAV      Average annual soil temperature, used with TAMP to calculate

        amp = highestMonthlyAverage - lowestMonthlyAverage;

        //    System.out.println("tav = [" + tav + "]" + " [" + FunTricks.fitInNCharacters(tav, 5) +
        // "]");
        //    System.out.println("amp = [" + amp + "]" + " [" + FunTricks.fitInNCharacters(amp, 5) +
        // "]");

        outputHeaderAsArray[0] = "*WEATHER DATA :";
        outputHeaderAsArray[1] = "";
        outputHeaderAsArray[2] = "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT";
        outputHeaderAsArray[3] =
            "  XXXX   "
                + FunTricks.fitInNCharacters(latitude, 6)
                + "   "
                + FunTricks.fitInNCharacters(longitude, 6)
                + "  -999 "
                + FunTricks.fitInNCharacters(tav, 5)
                + " "
                + FunTricks.fitInNCharacters(amp, 5)
                + "  -999  -999";
        outputHeaderAsArray[4] = "@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D";

        //    System.out.println(" oHAA[3] = {" + outputHeaderAsArray[3] + "}");
        // create the new array for writing out to the file
        finalCombinedOutputArray = new String[nLinesInHeader + nGoodDays];

        // fill it up

        // the header
        for (int rowIndex = 0; rowIndex < nLinesInHeader; rowIndex++) {
          finalCombinedOutputArray[rowIndex] = outputHeaderAsArray[rowIndex];
        }

        // everything else
        for (int rowIndex = 0; rowIndex < nGoodDays; rowIndex++) {
          finalCombinedOutputArray[rowIndex + nLinesInHeader] = outputAsArray[rowIndex];
        }

        // get rid of the suffix and build the latitude and longitude into the filename
        destinationFile =
            destinationPath
                + File.separator
                + inputFiles[fileIndex].getName().replaceFirst(sourceSuffix, "")
                + "_"
                + latitude
                + "_"
                + longitude
                + weatherSuffix;

        FunTricks.writeStringArrayToFile(finalCombinedOutputArray, destinationFile);
      }

      System.out.println(
          "-----> finished writing " + destinationFile + "; nGoodDays = " + nGoodDays);

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

      /////////////
      // reset ! //
      /////////////

      // annual
      annualTmax.reset();
      annualTmin.reset();
      annualTave.reset();

      // monthly
      for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
        monthlyTmax[monthIndex].reset();
        monthlyTmin[monthIndex].reset();
      }
    } // end giant file loop

    //////////////
    // all done //
    //////////////

  } // main
}
