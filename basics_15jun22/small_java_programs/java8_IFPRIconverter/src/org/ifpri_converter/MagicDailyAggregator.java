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

public class MagicDailyAggregator {

  private static double sliceToDoubleFromFront(String stringToSlice) {

    String cleanString = stringToSlice.trim();
    int stringLength = cleanString.length();
    double valueToReturn = Double.NaN;
    boolean haveGoodValue = false;

    String afterSlicing = null;
    for (int sliceIndex = 0; sliceIndex < stringLength; sliceIndex++) {
      afterSlicing = cleanString.substring(sliceIndex);
      //			System.out.println("sI = " + sliceIndex + "; aS = [" + afterSlicing + "]");
      try {
        valueToReturn = Double.parseDouble(afterSlicing);
        //				System.out.println("   vTR = " + valueToReturn);
        break;
      } catch (NumberFormatException nfe) {
        // do nothing
        //				System.out.println("nfe = " + nfe);
      }
    }

    return valueToReturn;
  }

  private static double sliceToDoubleFromBack(String stringToSlice) {

    String cleanString = stringToSlice.trim();
    int stringLength = cleanString.length();
    double valueToReturn = Double.NaN;
    boolean haveGoodValue = false;

    String afterSlicing = null;
    for (int sliceIndex = 0; sliceIndex < stringLength; sliceIndex++) {
      afterSlicing = cleanString.substring(0, (cleanString.length() - sliceIndex));
      System.out.println("sI = " + sliceIndex + "; aS = [" + afterSlicing + "]");
      try {
        valueToReturn = Double.parseDouble(afterSlicing);
        System.out.println("   vTR = " + valueToReturn);
        break;
      } catch (NumberFormatException nfe) {
        // do nothing
        //				System.out.println("nfe = " + nfe);
      }
    }

    return valueToReturn;
  }

  private static int monthToNumberOfDays(int monthNumber, int yearNumber) {

    switch (monthNumber) {
      case 1:
        return 31;
      case 2:
        if (yearNumber % 4 == 0) {
          // 2000 *was* a leap year...
          return 29;
        }
        return 28;
      case 3:
        return 31;
      case 4:
        return 30;
      case 5:
        return 31;
      case 6:
        return 30;
      case 7:
        return 31;
      case 8:
        return 31;
      case 9:
        return 30;
      case 10:
        return 31;
      case 11:
        return 30;
      default:
        return 31;
    }
  }

  private static int dayToMonth(int dayNumber, int yearNumber) {

    int jan = 31;
    int feb = jan + 28;
    if (yearNumber % 4 == 0) {
      // 2000 *was* a leap year...
      feb = jan + 29;
    }
    int mar = feb + 31;
    int apr = mar + 30;
    int may = apr + 31;
    int june = may + 30;
    int july = june + 31;
    int aug = july + 31;
    int sept = aug + 30;
    int oct = sept + 31;
    int nov = oct + 30;

    if (dayNumber <= jan) {
      return 1;
    } else if (dayNumber <= feb) {
      return 2;
    } else if (dayNumber <= mar) {
      return 3;
    } else if (dayNumber <= apr) {
      return 4;
    } else if (dayNumber <= may) {
      return 5;
    } else if (dayNumber <= june) {
      return 6;
    } else if (dayNumber <= july) {
      return 7;
    } else if (dayNumber <= aug) {
      return 8;
    } else if (dayNumber <= sept) {
      return 9;
    } else if (dayNumber <= oct) {
      return 10;
    } else if (dayNumber <= nov) {
      return 11;
    } else {
      return 12;
    }
  }

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(i + " " + commandLineOptions[i] + "\n");
    }
    System.out.println();

    if (commandLineOptions.length == 0) {
      System.out.println(
          "Usage: org.ifpri_converter.MagicDailyAggregator input_file output_file\n"
              + "\n"
              + "The point of it is to take some data from Jawoo's downloading of the NASA-POWER"
              + " daily stuff and try to aggregate it up to monthly. But we have to check for"
              + " double counting and all sorts of good things....\n"
              + "WARNING!!! Nothing is idiot proofed!\n"
              + "\n"
              + "If the attributes file has a header line, the header_line_flag should be"
              + " non-empty. All columns will be transfered to the\n"
              + "new raster table. If a column is non-numeric, characters will be sliced off the"
              + " beginning until only\n"
              + "a number is found. If that fails, the slicing will proceed from the end. If that"
              + " fails, -9999 will be asssigned.");
      System.exit(1);
    }

    String inputFile = commandLineOptions[0];
    String outputFile = commandLineOptions[1];

    // MAGIC NUMBERS!!!!
    String magicDelimiter = ",";

    System.out.println("-- finding length of input file at " + new Date());

    // figure out the total number of pixels in input file...
    int nLinesTotal = 72384976;
    RandomAccessFile randFile = new RandomAccessFile(inputFile, "r");
    long lastRec = randFile.length();
    randFile.close();
    FileReader inRead = new FileReader(inputFile);
    LineNumberReader lineRead = new LineNumberReader(inRead);
    lineRead.skip(lastRec);
    nLinesTotal = lineRead.getLineNumber();
    lineRead.close();
    inRead.close();

    // open up input file
    FileReader inputInStream = new FileReader(inputFile);
    BufferedReader inReader = new BufferedReader(inputInStream);

    // ok, some magic stuff
    int magicFirstYear = 1997;
    int magicLastYear = 2007;
    int magicNYears = magicLastYear - magicFirstYear + 1;

    int magicDaysInYear = 366;

    int nCellCodes = 64800; // 90 * 180

    float magicBadValue = -9999.0F;

    String lineContents = null;
    String[] lineSplit = null;

    int cellID = -1;
    int yearActual = -2;
    int yearIndexToUse = -3;
    int dayActual = -4;
    int dayIndexToUse = -5;

    int minCellCodeFound = Integer.MAX_VALUE;
    int maxCellCodeFound = Integer.MIN_VALUE;

    float solarRadiation = Float.NaN;
    float rainFall = Float.NaN;

    System.out.println("-- beginning initializations at " + new Date());

    float[][][] dailySolarAll = new float[nCellCodes][magicNYears][magicDaysInYear];
    //		MultiFormatFloat dailySolarAll   = new MultiFormatFloat(3,new long[]
    // {nCellCodes,magicNYears,magicDaysInYear},Float.NaN);

    boolean[][][] dailyIsRainyAll = new boolean[nCellCodes][magicNYears][magicDaysInYear];

    int[][] totalDaysThatAreValid = new int[nCellCodes][12];
    int[][] totalRainyDaysThatAreValid = new int[nCellCodes][12];

    float[][] totalSolarRadiationThatIsValid = new float[nCellCodes][12];
    //		MultiFormatFloat totalSolarRadiationThatIsValid   = new MultiFormatFloat(3,new long[]
    // {nCellCodes,12});

    System.out.println("-- filling in NaNs at " + new Date());

    // initialize the solar stuff with nan's...
    // that way, it will serve a dual purpose of storing the values as well
    // as marking which days have been processed and which not...
    for (int cellIndex = 0; cellIndex < nCellCodes; cellIndex++) {
      for (int yearIndex = 0; yearIndex < magicNYears; yearIndex++) {
        for (int dayIndex = 0; dayIndex < magicDaysInYear; dayIndex++) {
          dailySolarAll[cellIndex][yearIndex][dayIndex] = Float.NaN;
        }
      }
    }

    System.out.println("-- filling in NaNs at " + new Date());

    //		lineContents = " ";
    //		int lineIndex = -1;
    for (long lineIndex = 0; lineIndex < nLinesTotal; lineIndex++) {
      //		while (lineContents != null) {
      //			lineIndex ++;
      if (lineIndex % 1000000 == 0) {
        System.out.println(
            "reading line "
                + (lineIndex + 1)
                + " of "
                + nLinesTotal
                + " "
                + (((float) lineIndex + 1) / nLinesTotal));
      }

      lineContents = inReader.readLine();
      lineSplit = lineContents.split(magicDelimiter);

      //		CELL1D WEYR WEDAY SRAD TMAX TMIN RAIN
      cellID = Integer.parseInt(lineSplit[0]);

      yearActual = Integer.parseInt(lineSplit[1]);
      yearIndexToUse = yearActual - magicFirstYear;

      dayActual = Integer.parseInt(lineSplit[2]);
      dayIndexToUse = dayActual - 1;

      solarRadiation = Float.parseFloat(lineSplit[3]);
      rainFall = Float.parseFloat(lineSplit[6]);

      if (cellID < minCellCodeFound) {
        minCellCodeFound = cellID;
      }
      if (cellID > maxCellCodeFound) {
        maxCellCodeFound = cellID;
      }

      if (Float.isNaN(dailySolarAll[cellID][yearIndexToUse][dayIndexToUse])) {
        //			if ( Float.isNaN( dailySolarAll.getValue(cellID,yearIndexToUse,dayIndexToUse) ) ) {
        // we have not seen this day before, so write the info down; otherwise, do nothing
        //				System.out.println("new: " + minCellCodeFound + "/" + maxCellCodeFound + " | " +
        // cellID + "," + yearIndexToUse + "," + dayIndexToUse + "," + solarRadiation + "," +
        // rainFall);

        dailySolarAll[cellID][yearIndexToUse][dayIndexToUse] = solarRadiation;
        //				dailySolarAll.setValue(cellID,yearIndexToUse,dayIndexToUse,solarRadiation);

        if (rainFall > 0.0F) {
          dailyIsRainyAll[cellID][yearIndexToUse][dayIndexToUse] = true;
        }
      } // end if isNaN
      else {
        //				System.out.println("old: " + minCellCodeFound + "/" + maxCellCodeFound + " | " +
        // cellID + "," + yearIndexToUse + "," + dayIndexToUse + "," + solarRadiation + "," +
        // rainFall);
      }
    } // end for lineNumber
    inReader.close();
    inputInStream.close();

    // now, we should have all the information we need in a reasonable data structure.
    // the only remaining task is to aggregate into months...

    float[] thisYearsSolar = null;
    int[] thisYearsNRainy = null;
    int[] thisYearsCounts = null;

    int yearToConsider = -5;
    int monthToConsider = -6;
    int daysInThisMonth = -7;

    System.out.println("-- starting aggregation at " + new Date());

    String debugOutName = outputFile + "_debug.txt";
    System.out.println("debug out name = " + debugOutName);
    FileOutputStream debugOutStream = new FileOutputStream(debugOutName);
    PrintWriter debugOut = new PrintWriter(debugOutStream);

    //		for (int cellIndex = 0; cellIndex < nCellCodes; cellIndex++) {
    System.out.println("min cell found = " + minCellCodeFound + "; max = " + maxCellCodeFound);

    for (int cellIndex = minCellCodeFound; cellIndex < maxCellCodeFound + 1; cellIndex++) {
      for (int yearIndex = 0; yearIndex < magicNYears; yearIndex++) {
        yearToConsider = yearIndex + magicFirstYear;

        thisYearsSolar = new float[13];
        thisYearsCounts = new int[13];
        thisYearsNRainy = new int[13];

        for (int dayIndex = 0; dayIndex < magicDaysInYear; dayIndex++) {

          if (!Float.isNaN(dailySolarAll[cellIndex][yearIndex][dayIndex])) {
            //					if ( !Float.isNaN( dailySolarAll.getValue(cellIndex,yearIndex,dayIndex) ) ) {
            // this combo has been accounted for, carry on...
            monthToConsider = dayToMonth(dayIndex + 1, yearToConsider);
            thisYearsSolar[monthToConsider] += dailySolarAll[cellIndex][yearIndex][dayIndex];
            //						thisYearsSolar[monthToConsider] +=
            // dailySolarAll.getValue(cellIndex,yearIndex,dayIndex);
            thisYearsCounts[monthToConsider]++;
            if (dailyIsRainyAll[cellIndex][yearIndex][dayIndex]) {
              thisYearsNRainy[monthToConsider]++;
            }
            debugOut.println(
                cellIndex
                    + magicDelimiter
                    + yearIndex
                    + magicDelimiter
                    + dayIndex
                    + magicDelimiter
                    + monthToConsider
                    + magicDelimiter
                    + thisYearsSolar[monthToConsider]
                    + magicDelimiter
                    + thisYearsCounts[monthToConsider]
                    + magicDelimiter
                    + thisYearsNRainy[monthToConsider]);
          }
        } // end dayIndex

        // decide if the months are valid for this year
        for (int monthNumber = 1; monthNumber <= 12; monthNumber++) {
          daysInThisMonth = monthToNumberOfDays(monthNumber, yearIndex + 1);
          if (thisYearsCounts[monthNumber] == daysInThisMonth) {
            // use these values
            totalDaysThatAreValid[cellIndex][monthNumber - 1]++;
            totalRainyDaysThatAreValid[cellIndex][monthNumber - 1] += thisYearsNRainy[monthNumber];
            totalSolarRadiationThatIsValid[cellIndex][monthNumber - 1] +=
                thisYearsSolar[monthNumber];
            //						totalSolarRadiationThatIsValid.setValue(cellIndex,monthNumber-1,
            //								totalSolarRadiationThatIsValid.getValue(cellIndex,monthNumber-1) +
            // thisYearsSolar[monthNumber]);
          }
        } // end monthIndex
      } // end yearIndex
    } // end cellIndex

    debugOut.close();
    debugOutStream.close();

    // now that we have all the info, write it out....
    // open out output files

    String dataOutName = outputFile + "_data.txt";
    System.out.println("data out name = " + dataOutName);
    FileOutputStream dataOutStream = new FileOutputStream(dataOutName);
    PrintWriter dataOut = new PrintWriter(dataOutStream);

    boolean haveWrittenTheCellCode = false;
    String lineToWrite = null;
    float rainyDaysToWrite = -5F;
    float solarToWrite = -6.0F;

    System.out.println("-- starting output at " + new Date());

    int nLinesWritten = 0;
    //		for (int cellIndex = 0; cellIndex < nCellCodes; cellIndex++) {
    for (int cellIndex = minCellCodeFound; cellIndex < maxCellCodeFound + 1; cellIndex++) {
      haveWrittenTheCellCode = false;
      for (int monthIndex = 0; monthIndex < 12; monthIndex++) {

        /*
        				if (totalDaysThatAreValid[cellIndex][monthIndex] > 0) {
        					rainyDaysToWrite = magicBadValue;
        					solarToWrite = magicBadValue;
        				} else {
        					rainyDaysToWrite =  ((float)totalRainyDaysThatAreValid[cellIndex][monthIndex]) / totalDaysThatAreValid[cellIndex][monthIndex] ;
        					solarToWrite     =      totalSolarRadiationThatIsValid[cellIndex][monthIndex]  / totalDaysThatAreValid[cellIndex][monthIndex] ;
        //					solarToWrite     =      totalSolarRadiationThatIsValid.getValue(cellIndex,monthIndex)  / totalDaysThatAreValid[cellIndex][monthIndex] ;
        				}
        				*/
        // this month is valid for this pixel...
        if (!haveWrittenTheCellCode) {
          // only print the cell index the first time
          lineToWrite = Integer.toString(cellIndex);
          haveWrittenTheCellCode = true;
        }
        //				lineToWrite = lineToWrite
        //				+ magicDelimiter
        //				+ Float.toString( rainyDaysToWrite )
        //				+ magicDelimiter
        //				+ Float.toString( solarToWrite );

        lineToWrite =
            lineToWrite
                + magicDelimiter
                + Float.toString(totalRainyDaysThatAreValid[cellIndex][monthIndex])
                + magicDelimiter
                + Float.toString(totalSolarRadiationThatIsValid[cellIndex][monthIndex])
                + magicDelimiter
                + Float.toString(totalDaysThatAreValid[cellIndex][monthIndex]);
      } // end monthIndex
      //			lineToWrite = lineToWrite + "\n";
      dataOut.println(lineToWrite);
      nLinesWritten++;
    } // end cellIndex

    dataOut.flush();
    dataOut.close();
    dataOutStream.close();

    System.out.println("-- all done at " + new Date());
  } // main
}
