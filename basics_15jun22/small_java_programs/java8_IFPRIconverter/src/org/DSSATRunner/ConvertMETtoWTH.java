package org.DSSATRunner;

// import java.io.*;

import org.R2Useful.*;

public class ConvertMETtoWTH {

  // i'm sure the agmip people have already done this better than i, but since
  // i am a distrusting sort of fellow, i feel like doing it myself for my own purposes...

  private static double pullOutValueBetweenEqualsAndOpenParenthesis(String stringToInterpret) {

    int equalsIndex = stringToInterpret.indexOf("=");
    int parenthesisIndex = stringToInterpret.indexOf("(");

    String stringValue = stringToInterpret.substring(equalsIndex + 1, parenthesisIndex).trim();

    return Double.parseDouble(stringValue);
  }

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    //		String pixelListFile         =                  commandLineOptions[0];

    String sourceMETName = commandLineOptions[0];
    String destinationWTHName = commandLineOptions[1];

    // magic numbers for spacing
    final int magicFirstLineToWorkOn = 11;

    final int nMonths = 12; // this actually needs to be twelve

    // magic numbers for thresholds to keep it real
    final double maximumAcceptableShortwave = 29.8; // MJ/m^2/day
    final double maximumAcceptableRain = 600.0; // mm/day ; quasi-max in Baseline_NT is 493.2 mm

    final double magicMinimumTemperatureDifferenceNeeded = 0.1;

    /// initializations
    int thisMonthIndex;
    int fourDigitYear, dayNumber; // , twoDigitYear;
    String dssatDateCode;

    // the data stuff
    //		double tavStated, ampStated;

    double shortwaveHere, tmaxHere, tminHere, rainHere, evapHere;
    double temperatureSum,
        hottestMonthValue,
        coldestMonthValue,
        thisMonthAverageTemperature,
        overallAverageAnnualTemperature,
        temperatureFullAmplitude;
    ;

    String[] originalAsArray = null;
    String[] dailyOutputAsArray = null;

    // set up the monthly accumulators for later use in tamp and tav
    DescriptiveStatisticsUtility[] monthlyTmax = new DescriptiveStatisticsUtility[nMonths];
    DescriptiveStatisticsUtility[] monthlyTmin = new DescriptiveStatisticsUtility[nMonths];

    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
      monthlyTmax[monthIndex] = new DescriptiveStatisticsUtility(true);
      monthlyTmin[monthIndex] = new DescriptiveStatisticsUtility(true);
    }

    /*
    *WEATHER DATA :

    @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
      RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
    @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
    90274   9.7   4.0   3.1   1.5  -999  -999
    90275  12.5   5.4   1.8   1.9  -999  -999
    		 */

    // build up the input file name....
    // grab the latitude/longitude/idnumber from the list tingju gave me....
    // read in the raw data
    originalAsArray = FunTricks.readTextFileToArray(sourceMETName);

    int magicOffsetBetweenMETandWTH =
        -11 + 5; // now different because i'm dropping blank lines in the .met; old = -15 + 5;

    dailyOutputAsArray =
        new String
            [originalAsArray.length
                + magicOffsetBetweenMETandWTH]; // Beware the MAGIC NUMBER!!! the difference in
    // length between the headers...

    // reset the monthly accumulators and monthly output lines...

    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
      monthlyTmax[monthIndex].reset();
      monthlyTmin[monthIndex].reset();
    }

    // do the first few header lines
    /*
    !Title  =       Breda,  Syria

    [weather.met.weather]
    site    =       Breda
    elevatio=            300(m)
    latitude=          35.93(dd)
    longitud=          37.17(dd)


            !       TAV     and     AMP     insertedby      tav_amp on      ########at         11:18for     period  from      Jan-01to      365/1950(ddd/yyyy)
            tav     =          17.43(oC)    !       annual  average ambient temperature
            amp     =          22.57(oC)    !       annual  amplitudin      mean    monthly temperature

            Site    year    day     mint    maxt    rain    radn    evap
            ()      ()      ()      (oC)    (oC)    (mm)    (MJ/m2) (mm)
            Breda       1901       1     9.3     9.5    10.9    4.93    0.89
            Breda       1901       2     5.6    11.9       0    8.49    1.56
            Breda       1901       3     2.8    12.7       0    6.26     1.1
    */

    // Beware the MAGIC EVERYTHING!!!
    //		double elevation = Double.parseDouble((originalAsArray[4].substring(9, 24)));
    //		double latitude  = Double.parseDouble((originalAsArray[5].substring(9, 24)));
    //		double longitude = Double.parseDouble((originalAsArray[6].substring(9, 24)));
    //		double tavStated = Double.parseDouble((originalAsArray[10].substring(17, 32)));
    //		double ampStated = Double.parseDouble((originalAsArray[11].substring(17, 32)));

    // let's try again without magic widths...
    // but, we have the problem of inconsistent line spacing. so, i will assume that
    // all blank lines have been eliminated...
    double elevation = pullOutValueBetweenEqualsAndOpenParenthesis(originalAsArray[3]);
    double latitude = pullOutValueBetweenEqualsAndOpenParenthesis(originalAsArray[4]);
    double longitude = pullOutValueBetweenEqualsAndOpenParenthesis(originalAsArray[5]);
    double tavStated = pullOutValueBetweenEqualsAndOpenParenthesis(originalAsArray[7]);
    double ampStated = pullOutValueBetweenEqualsAndOpenParenthesis(originalAsArray[8]);

    /*
    *WEATHER DATA :

    @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
      RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
    @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
    		 */
    // interpret....
    dailyOutputAsArray[0] = "*WEATHER DATA :";
    dailyOutputAsArray[1] = "";
    dailyOutputAsArray[2] = "@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT";
    // we will come back and fix up index 3 with the new amp and tav at the end...
    dailyOutputAsArray[3] =
        "  ZZZZ"
            + " "
            + FunTricks.fitInNCharacters(latitude, 8)
            + " "
            + FunTricks.fitInNCharacters(longitude, 8)
            + " "
            + FunTricks.fitInNCharacters(elevation, 5)
            + " "
            + FunTricks.fitInNCharacters(tavStated, 5)
            + " "
            + FunTricks.fitInNCharacters(ampStated, 5)
            + " "
            + " -999  -999";
    dailyOutputAsArray[4] = "@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D  EVAP";

    // the plan is to preserve the original year/day numbers for the moment. the only question
    // is whether we are starting on january 1 or not. and we want to skip a few years for potential
    // spinup...

    String[] splitLine = null;
    for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {

      /*
          Site    year    day     mint    maxt    rain    radn    evap
          ()      ()      ()      (oC)    (oC)    (mm)    (MJ/m2) (mm)
          Breda       1901       1     9.3     9.5    10.9    4.93    0.89
      */

      splitLine = FunTricks.parseRepeatedDelimiterToArray(originalAsArray[lineIndex], " ");

      //			System.out.println("{" + originalAsArray[lineIndex] + "}");
      //			for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
      //				System.out.println("[" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
      //			}

      fourDigitYear = Integer.MIN_VALUE;
      try {
        fourDigitYear = Integer.parseInt(splitLine[1]);
      } catch (NumberFormatException nfe) {
        System.out.println("lineIndex = [" + lineIndex + "] {" + originalAsArray[lineIndex] + "}");
        for (int splitIndex = 0; splitIndex < splitLine.length; splitIndex++) {
          System.out.println("[" + splitIndex + "] = [" + splitLine[splitIndex] + "]");
        }
      }
      dayNumber = Integer.parseInt(splitLine[2]);
      //			twoDigitYear = fourDigitYear - 100 * (fourDigitYear / 100) + dayNumber;
      dssatDateCode =
          Integer.toString(fourDigitYear).substring(2, 4)
              + FunTricks.padStringWithLeadingZeros(Integer.toString(dayNumber), 3);

      thisMonthIndex = DSSATHelperMethods.monthIndexFromDayNumber(dayNumber);

      // now comes the fun part, deciding what to write down...

      // grab the temperature info that we need regardless; for simplicity, we do it all here even
      // though we might not need it all
      tminHere = Double.parseDouble(splitLine[3]);
      tmaxHere = Double.parseDouble(splitLine[4]);
      rainHere = Double.parseDouble(splitLine[5]);
      shortwaveHere = Double.parseDouble(splitLine[6]);
      evapHere = Double.parseDouble(splitLine[7]);

      // and make sure it's not too ridiculous
      // in theory, this should somehow take into account total possible insolation for this
      // latitude, but i am
      // way too lazy for that right now...
      if (shortwaveHere > maximumAcceptableShortwave) {
        shortwaveHere = maximumAcceptableShortwave;
      }

      //////////////
      // rainfall //
      //////////////

      // and make sure it's not too ridiculous
      // in theory, this should somehow take into account total possible insolation for this
      // latitude, but i am
      // way to lazy for that right now...
      if (rainHere > maximumAcceptableRain) {
        rainHere = maximumAcceptableRain;
      } else if (rainHere < 0) {
        // added in, but after my big runs. negative rainfall should only happen in really bad zero
        // rainfall places
        // to begin with. and hopefully DSSAT does the censoring anyway.
        rainHere = 0.0d;
      }

      //////////////////
      // temperatures //
      //////////////////

      // but, we need to make sure the high and low are different because of whiney crop models
      if (Math.abs(tmaxHere - tminHere) < magicMinimumTemperatureDifferenceNeeded) {
        tmaxHere = tminHere + magicMinimumTemperatureDifferenceNeeded;
      }

      // ok, and actually write it out...

      // write stuff out...
      // ok, here i need to have all the goodies added up and shifted/etc. then we write it out
      dailyOutputAsArray[lineIndex + magicOffsetBetweenMETandWTH] =
          dssatDateCode
              + " "
              + FunTricks.fitInNCharacters(shortwaveHere, 5)
              + " "
              + FunTricks.fitInNCharacters(tmaxHere, 5)
              + " "
              + FunTricks.fitInNCharacters(tminHere, 5)
              + " "
              + FunTricks.fitInNCharacters(rainHere, 5)
              + " "
              + " -999  -999"
              + " "
              + FunTricks.fitInNCharacters(evapHere, 5);
      ;

      /*
      			 *WEATHER DATA :

      					@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
      					  RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
      //				0         1         2         3         4         5
      //				0123456789012345678901234567890123456789012345678901234
      					@DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
      					90274   9.7   4.0   3.1   1.5  -999  -999
      					90275  12.5   5.4   1.8   1.9  -999  -999
      			 */

      // accumulate the monthly stuff en route to tamp and tav...
      monthlyTmax[thisMonthIndex].useDoubleValue(tmaxHere);
      monthlyTmin[thisMonthIndex].useDoubleValue(tminHere);
    } // for lineIndex

    // fix up header line index 3
    //		0         1         2         3         4         5
    //		0123456789012345678901234567890123456789012345678901234
    //		@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
    //		RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999

    // determine the average temperature and the extrema to get the amplitude
    temperatureSum = 0.0;
    hottestMonthValue = Double.NEGATIVE_INFINITY;
    coldestMonthValue = Double.POSITIVE_INFINITY;

    for (int monthIndex = 0; monthIndex < nMonths; monthIndex++) {
      // figure out what we have going on this month, on average...
      thisMonthAverageTemperature =
          (monthlyTmax[monthIndex].getMean() + monthlyTmin[monthIndex].getMean()) / 2.0;

      // check the extrema
      if (thisMonthAverageTemperature > hottestMonthValue) {
        hottestMonthValue = thisMonthAverageTemperature;
      }
      if (thisMonthAverageTemperature < coldestMonthValue) {
        coldestMonthValue = thisMonthAverageTemperature;
      }

      // accumulate the sum
      temperatureSum += thisMonthAverageTemperature;
    }

    overallAverageAnnualTemperature = temperatureSum / nMonths;

    temperatureFullAmplitude = hottestMonthValue - coldestMonthValue;

    System.out.println("tavStated                       = " + tavStated);
    System.out.println("overallAverageAnnualTemperature = " + overallAverageAnnualTemperature);
    System.out.println("ampStated                = " + ampStated);
    System.out.println("temperatureFullAmplitude = " + temperatureFullAmplitude);

    // write out the daily file
    FunTricks.writeStringArrayToFile(dailyOutputAsArray, destinationWTHName);

    ///////////////////////////////////////
    // end of for loop over files goes here
    ///////////////////////////////////////

    // write out the monthly files

    //////////////
    // all done //
    //////////////

  } // main
}
