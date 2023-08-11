package org.DSSATRunner;

import org.R2Useful.*;

public class JustBuildInitializationBlocks {

  public static void main(String commandLineOptions[]) throws Exception {

    TimerUtility bigTimer = new TimerUtility();

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(i + " " + commandLineOptions[i]);
    }

    System.out.println();

    bigTimer.tic();

    String initializationFile = commandLineOptions[0];

    String[] initializationValues = org.R2Useful.FunTricks.readTextFileToArray(initializationFile);

    // interpret the initialization file:

    int readIndex = 0;
    String soilTypeString = initializationValues[readIndex++].split(":")[1].trim();

    String initializationDayCode = initializationValues[readIndex++].split(":")[1].trim();

    int appropriateDayCodeLength = 5;
    if (initializationDayCode.length() > appropriateDayCodeLength) {
      initializationDayCode =
          initializationDayCode.substring(
              initializationDayCode.length() - appropriateDayCodeLength);
    }

    SoilProfile soilProfiles = new SoilProfile(soilTypeString.substring(0, 2) + ".SOL");

    double
        totalInitialNitrogenKgPerHa =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        rootWeight = Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        surfaceResidueWeight =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        fractionBetweenLowerLimitAndDrainedUpperLimit =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        depthForNitrogen =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        residueNitrogenPercent =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        incorporationRate =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim()),
        incorporationDepth =
            Double.parseDouble(initializationValues[readIndex++].split(":")[1].trim());

    String clayLoamSandStableCarbonRatesFilename =
        initializationValues[readIndex++].split(":")[1].trim();

    String[] carbonTable = FunTricks.readTextFileToArray(clayLoamSandStableCarbonRatesFilename);
    int nLayers = carbonTable.length;

    double[] standardDepths = new double[nLayers];
    double[] clayStableFractionAbove = new double[nLayers];
    double[] loamStableFractionAbove = new double[nLayers];
    double[] sandStableFractionAbove = new double[nLayers];

    String[] splitLine = null;
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      System.out.println("line " + layerIndex + ": [" + carbonTable[layerIndex] + "]");
      splitLine = carbonTable[layerIndex].split("\t");
      standardDepths[layerIndex] = Double.parseDouble(splitLine[0]);
      clayStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[1]);
      loamStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[2]);
      sandStableFractionAbove[layerIndex] = Double.parseDouble(splitLine[3]);
    }

    String initializationBlock =
        soilProfiles.makeInitializationAndSoilAnalysisBlock(
            soilTypeString,
            fractionBetweenLowerLimitAndDrainedUpperLimit,
            initializationDayCode,
            totalInitialNitrogenKgPerHa,
            depthForNitrogen,
            rootWeight,
            surfaceResidueWeight,
            residueNitrogenPercent,
            incorporationRate,
            incorporationDepth,
            standardDepths,
            clayStableFractionAbove,
            loamStableFractionAbove,
            sandStableFractionAbove);

    initializationBlock =
        "! "
            + soilTypeString
            + " "
            + fractionBetweenLowerLimitAndDrainedUpperLimit
            + " "
            + initializationDayCode
            + " "
            + totalInitialNitrogenKgPerHa
            + " "
            + depthForNitrogen
            + " "
            + rootWeight
            + " "
            + surfaceResidueWeight
            + " "
            + residueNitrogenPercent
            + " "
            + incorporationRate
            + " "
            + incorporationDepth
            + " "
            + standardDepths
            + " "
            + clayStableFractionAbove
            + " "
            + loamStableFractionAbove
            + " "
            + sandStableFractionAbove
            + "\n\n\n"
            + initializationBlock
            + "\n\n\n";

    System.out.print(initializationBlock);

    System.out.println(bigTimer.tocMillis() + " ms for TESTOR...");

    System.out.println(bigTimer.sinceStartMessage());
  } // main
}
