package org.DSSATRunner;

// import java.io.*;

import org.R2Useful.*;

public class TransformWeatherFileArbitrary {

  // i want to take existing weather files and try out some arbtrary changes to see if there
  // are variables that don't matter....

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    String sourcePath = commandLineOptions[0];
    String destinationPath = commandLineOptions[1];

    String[] fileContents = FunTricks.readTextFileToArray(sourcePath);

    int nLinesInDataFile = fileContents.length;

    String[] alteredContents = new String[fileContents.length];

    // the first few lines that are headers
    alteredContents[0] = fileContents[0];
    alteredContents[1] = fileContents[1];
    alteredContents[2] = fileContents[2];

    // this is the one with the latitude and longitude and elevation/etc
    // let's try setting all of these to zero to see what happens
    // remember: fixed spacing
    //
    //		alteredContents[3] = "  RICK   00.000    0.000  00.0  00.0  00.0  0.00  0.00";
    // ok, that made a difference, so which of them matter? let's leave lat/long/elev
    // and trash the others...

    // parse the header and then put it back together...
    // 0         1         2         3         4         5
    // 012345678901234567890123456789012345678901234567890123
    // @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
    //   RICK   -0.750  101.250  329.  25.8   9.5  1.50  2.00
    String insi = fileContents[3].substring(0, 6);
    double lat = Double.parseDouble(fileContents[3].substring(7, 15));
    double lon = Double.parseDouble(fileContents[3].substring(16, 24));
    double elev = Double.parseDouble(fileContents[3].substring(25, 30));
    double tav = Double.parseDouble(fileContents[3].substring(31, 36));
    double amp = Double.parseDouble(fileContents[3].substring(37, 42));
    double refht = Double.parseDouble(fileContents[3].substring(43, 48));
    double wndht = Double.parseDouble(fileContents[3].substring(49));

    // build up the new header...
    alteredContents[3] =
        insi
            + " "
            + FunTricks.fitInNCharacters(lat, 8)
            + " "
            + FunTricks.fitInNCharacters(lon, 8)
            + " "
            + FunTricks.fitInNCharacters(elev, 5)
            + " "
            +
            //		FunTricks.fitInNCharacters(0, 5) + " " +
            //		FunTricks.fitInNCharacters(0, 5) + " " +
            //		FunTricks.fitInNCharacters(0, 5) + " " +
            //		FunTricks.fitInNCharacters(0, 5);
            FunTricks.fitInNCharacters(tav, 5)
            + " "
            + FunTricks.fitInNCharacters(amp, 5)
            + " "
            + FunTricks.fitInNCharacters(0, 5)
            + " "
            + FunTricks.fitInNCharacters(0, 5);
    //		FunTricks.fitInNCharacters(refht, 5) + " " +
    //		FunTricks.fitInNCharacters(wndht, 5);

    // copy the header line for the daily...
    alteredContents[4] = fileContents[4];

    int date = -1;
    double srad, tmax, tmin, rain, par, co2d;

    // leave the rest the same for the moment
    for (int lineIndex = 5; lineIndex < nLinesInDataFile; lineIndex++) {
      if (false) {
        alteredContents[lineIndex] = fileContents[lineIndex];
      }

      /*
      // 0         1         2         3         4         5
      // 012345678901234567890123456789012345678901234567890123
         @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
         51001  20.6  19.0  10.8   0.0  41.1 314.9
         51002  24.6  15.6   6.3   0.0  49.3 314.9
         51003  27.9  11.0   3.4   0.0  55.8 314.9
      */

      date = Integer.parseInt(fileContents[lineIndex].substring(0, 5));
      srad = Double.parseDouble(fileContents[lineIndex].substring(6, 11));
      tmax = Double.parseDouble(fileContents[lineIndex].substring(12, 17));
      tmin = Double.parseDouble(fileContents[lineIndex].substring(18, 23));
      rain = Double.parseDouble(fileContents[lineIndex].substring(24, 29));
      par = Double.parseDouble(fileContents[lineIndex].substring(30, 35));
      co2d = Double.parseDouble(fileContents[lineIndex].substring(36));

      alteredContents[lineIndex] =
          FunTricks.fitInNCharacters(date, 5)
              + " "
              + FunTricks.fitInNCharacters(srad, 5)
              + " "
              + FunTricks.fitInNCharacters(tmax, 5)
              + " "
              + FunTricks.fitInNCharacters(tmin, 5)
              + " "
              + FunTricks.fitInNCharacters(rain, 5)
              + " "
              + FunTricks.fitInNCharacters(0, 5)
              + " "
              + FunTricks.fitInNCharacters(1, 5);

      //				FunTricks.fitInNCharacters(par, 5) + " " +
      //				FunTricks.fitInNCharacters(co2d, 5);

    } // for lineIndex

    // write out the new junky file
    FunTricks.writeStringArrayToFile(alteredContents, destinationPath);

    //////////////
    // all done //
    //////////////

  } // main
}
