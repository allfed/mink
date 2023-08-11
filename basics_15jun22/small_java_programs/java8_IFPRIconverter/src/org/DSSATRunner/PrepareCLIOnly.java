package org.DSSATRunner;

import java.io.*;
// import java.util.Date;
import org.R2Useful.*;

public class PrepareCLIOnly {

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.println(i + " " + commandLineOptions[i]);
    }

    System.out.println();

    String gisTableBaseName = commandLineOptions[0];
    String outputDirectory = commandLineOptions[1];
    String outputFilename = commandLineOptions[2];
    int lineIndexToProcess = -1;
    if (commandLineOptions.length > 3) {
      lineIndexToProcess = Integer.parseInt(commandLineOptions[3]);
    }
    //		String outputBaseName										= commandLineOptions[2];

    File weatherStationFile = null;
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

    String fullCLIName = null;
    //		String fullOtherName = null;

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
            + "\n";
    if (lineIndexToProcess > 0) {
      provenanceString += "lineIndexToProcess = " + commandLineOptions[3] + "\n";
    }

    String provenanceName = outputDirectory + "/" + outputFilename + "_" + "provenance.txt";

    FunTricks.writeStringToFile(provenanceString, provenanceName);

    String cliStuffToWrite = null;

    // figure out the climate file...
    if (lineIndexToProcess >= 0) {
      cliStuffToWrite =
          DSSATHelperMethods.cliFileContentsAllSupplied(
              dataMatrix, geogMatrix, lineIndexToProcess, 1.0);

      fullCLIName =
          outputDirectory + "/" + outputFilename + "_" + (lineIndexToProcess + 1) + ".CLI";

      weatherStationFile = new File(fullCLIName);
      FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);

    } else {

      for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

        ////////////////////////
        // write out CLI file //
        ////////////////////////

        // Beware the MAGIC NUMBER!!! assuming the SWmultiplier is unity
        cliStuffToWrite =
            DSSATHelperMethods.cliFileContentsAllSupplied(dataMatrix, geogMatrix, lineIndex, 1.0);

        fullCLIName = outputDirectory + "/" + outputFilename + "_" + (lineIndex + 1) + ".CLI";

        weatherStationFile = new File(fullCLIName);
        FunTricks.writeStringToFile(cliStuffToWrite, weatherStationFile);
      }
    }

    //////////////
    // all done //
    //////////////

  } // main
}
