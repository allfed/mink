package org.DSSATRunner;

import java.io.*;
import org.R2Useful.*;

public class LotsOfDailyWeather {

  // the idea here is to take a table of climate averages and then generate daily weather from them
  // so that we have a whole bunch of DSSAT-style daily weather files laying around in some
  // systematic
  // fasion...

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    TimerUtility thisTimer = new TimerUtility();
    double timeSoFar = -5;

    System.out.print("command line arguments: \n");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.println(i + " " + commandLineOptions[i]);
    }

    System.out.println();

    String gisTableBaseName = commandLineOptions[0];
    String outputDirectory = commandLineOptions[1];
    String outputFilename = commandLineOptions[2];
    String nameOfWeatherExecutable = commandLineOptions[3];
    int firstWeatherYear = Integer.parseInt(commandLineOptions[4]);
    int nFakeYears = Integer.parseInt(commandLineOptions[5]);
    int randomSeed = Integer.parseInt(commandLineOptions[6]);

    // check to see if we really have the output directory or not...
    File outputDirectoryObject = new File(outputDirectory);
    if (!outputDirectoryObject.isDirectory()) {
      outputDirectoryObject.mkdirs();
    }

    Process theRunningProcess = null;

    //		File weatherStationFile = null;
    String[] weatherExecutionCommand = new String[7];
    final String magicWeatherStationNameToUse = "RSTU";

    String fullCLIName = magicWeatherStationNameToUse + ".CLI";

    // ./fast_new_arbfile.exe 1 99001 1 S RRRR.CLI tryme.wth
    weatherExecutionCommand[0] = nameOfWeatherExecutable;
    weatherExecutionCommand[1] = null; // this is the start date code
    weatherExecutionCommand[2] = null; // this is the end date code
    weatherExecutionCommand[3] = null; // this is the random seed to use
    // Beware the MAGIC NUMBER!!! always using simmeteo
    weatherExecutionCommand[4] = "S"; // this is which generator to use, S = SIMMETEO
    weatherExecutionCommand[5] =
        fullCLIName; // magicWeatherStationNameToUse; // this is the CLI file
    // Beware the MAGIC NUMBER!!! using .WTH because that's what works for me. it might need .WTG
    // under other circumstances
    weatherExecutionCommand[6] = null; // this is the CLI file

    // read in the fundamental data
    // Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast
    // enough
    // that it doesn't matter. for big stuff, we'll want disk...
    int formatIndexToForce = 1;
    MultiFormatMatrix dataMatrix =
        MatrixOperations.read2DMFMfromTextForceFormat(
            gisTableBaseName + "_data", formatIndexToForce);
    MultiFormatMatrix geogMatrix =
        MatrixOperations.read2DMFMfromTextForceFormat(
            gisTableBaseName + "_geog", formatIndexToForce);

    if (geogMatrix.getDimensions()[1] != 4) {
      System.out.println("Geography files need 4 columns, not " + geogMatrix.getDimensions()[1]);
      throw new Exception();
    }

    int nLinesInDataFile = (int) dataMatrix.getDimensions()[0];

    // and a simple provenance file...
    String provenanceString =
        "The command line options were: \n"
            + "gisTableBaseName = "
            + commandLineOptions[0]
            + "\n"
            + "outputDirectory  = "
            + commandLineOptions[1]
            + "\n"
            + "outputFilename   = "
            + commandLineOptions[2]
            + "\n"
            + "nameOfWeatherExecutable = "
            + commandLineOptions[3]
            + "\n"
            + "firstWeatherYear = "
            + commandLineOptions[4]
            + "\n"
            + "nFakeYears       = "
            + commandLineOptions[5]
            + "\n"
            + "randomSeed       = "
            + commandLineOptions[6];

    String provenanceName = outputDirectory + "/" + outputFilename + "_" + "provenance.txt";

    FunTricks.writeStringToFile(provenanceString, provenanceName);

    String cliStuffToWrite = null;

    String finalWeatherFileLocation = null;
    File temporaryWeatherFile = null;

    File finalWeatherFile = null;

    //		boolean floppy = false;

    final String temporaryWeatherFileLocation = "JUNK.WTH";
    //		final long magicIDColIndex = 67;
    // figure out the climate file...

    //		double idNumber = -1; // this will be the pixel-specific id number...

    double latitude, longitude;

    thisTimer.tic();

    for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

      ////////////////////////
      // write out CLI file //
      ////////////////////////

      // Beware the MAGIC NUMBER!!! assuming the SWmultiplier is unity
      cliStuffToWrite =
          DSSATHelperMethods.cliFileContentsAllSuppliedONLYCLIMATEINFO(
              dataMatrix, geogMatrix, lineIndex, 1.0);

      // Beware the MAGIC NUMBER!!!
      //			idNumber = dataMatrix.getValue(lineIndex,magicIDColIndex); // 67 is the column index for

      //			weatherStationFile = new File(fullCLIName);
      //			FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
      FunTricks.writeStringToFile(cliStuffToWrite, fullCLIName);

      // run the weather generator

      //			weatherExecutionCommand[0] = pathToDSSATDirectory + nameOfWeatherExecutable;
      //			weatherExecutionCommand[1] = Integer.toString(fakePlantingYear); // this is the start
      // date code
      // ok, this needs to be slightly more tricky: we want to allow for bazillions of years of fake
      // weather.
      // also, we don't need padding for this, so we will just move the year over and add the day
      // Beware the MAGIC NUMBER!!! starting weather on the first day of the year...
      weatherExecutionCommand[1] =
          Integer.toString(
              firstWeatherYear * 1000 + 1); // this is the start date code; should be year #00
      // this is the end date code; adding a little just to be safe; originally two extra years, now
      // trying more (16jan14)...
      weatherExecutionCommand[2] = Integer.toString((firstWeatherYear + nFakeYears + 1) * 1000 + 1);
      weatherExecutionCommand[3] = Integer.toString(randomSeed); // this is the random seed to use
      // Beware the MAGIC NUMBER!!! always using simmeteo
      //			weatherExecutionCommand[4] = "S"; // this is which generator to use, S = SIMMETEO
      //			weatherExecutionCommand[5] = magicWeatherStationNameToUsePath; // this is the CLI file
      // Beware the MAGIC NUMBER!!! using .WTH because that's what works for me. it might need .WTG
      // under other circumstances
      weatherExecutionCommand[6] = temporaryWeatherFileLocation;

      //			System.out.println("weatherExecutionCommand[1] = [" + weatherExecutionCommand[1] + "]");
      //			System.out.println("weatherExecutionCommand[2] = [" + weatherExecutionCommand[2] + "]");
      //			System.out.println("weatherExecutionCommand[3] = [" + weatherExecutionCommand[3] + "]");
      //			System.out.println("weatherExecutionCommand[4] = [" + weatherExecutionCommand[4] + "]");
      //			System.out.println("weatherExecutionCommand[5] = [" + weatherExecutionCommand[5] + "]");
      //			System.out.println("weatherExecutionCommand[6] = [" + weatherExecutionCommand[6] + "]");

      // this should be pretty stable, so we'll do it the simple way....
      theRunningProcess = Runtime.getRuntime().exec(weatherExecutionCommand, null, null);
      //			theRunningProcess = Runtime.getRuntime().exec(weatherExecutionCommand , null , new
      // File("./"));

      // wait for it to finish up
      theRunningProcess.waitFor();

      // recommend garbage collection
      System.gc();

      // since the generator is stupid and won't take long filenames, we have to generate then
      // move...
      temporaryWeatherFile = new File(temporaryWeatherFileLocation);

      if (!temporaryWeatherFile.exists()) {
        System.out.println(
            "the file at ["
                + temporaryWeatherFileLocation
                + "]; ("
                + temporaryWeatherFile
                + ") FAILED...");
        break;
      }

      latitude = geogMatrix.getValue(lineIndex, 2); // Beware the MAGIC NUMBER!!!
      longitude = geogMatrix.getValue(lineIndex, 3); // Beware the MAGIC NUMBER!!!

      finalWeatherFileLocation =
          outputDirectory + "/" + outputFilename + "_" + latitude + "n_" + longitude + "e" + ".WTH";
      //			finalWeatherFileLocation = outputDirectory + "/" + outputFilename + "_" + idNumber +
      // ".WTH";
      finalWeatherFile = new File(finalWeatherFileLocation);

      temporaryWeatherFile.renameTo(finalWeatherFile);

      if (lineIndex % (nLinesInDataFile / 400 + 1) == 0) {
        timeSoFar = thisTimer.tocMinutes();
        System.out.println(
            lineIndex
                + "/"
                + nLinesInDataFile
                + " "
                + FunTricks.fitInNCharacters((100 * (lineIndex + 1.0) / nLinesInDataFile), 5)
                + "% "
                + timeSoFar
                + "m; "
                + (timeSoFar * nLinesInDataFile / (lineIndex + 1))
                + "m remaining");
      }
    } // for lineIndex

    //////////////
    // all done //
    //////////////

  } // main
}
