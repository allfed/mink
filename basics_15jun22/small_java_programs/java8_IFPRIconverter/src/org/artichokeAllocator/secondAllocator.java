package org.artichokeAllocator;

import org.R2Useful.*;

public class secondAllocator {

  private static double[] determineTotalAllocations(
      MultiFormatMatrix pixelFractions, MultiFormatMatrix pixelAreaAvailable) throws Exception {
    long nPixels = (int) pixelFractions.getDimensions()[0];
    int nCrops = (int) pixelFractions.getDimensions()[1];

    double[] theseAllocations = new double[nCrops];
    double thisPixelAreaAvailable = -5;
    double totalPixelAreaAvailable = 0.0; // this really should be zero

    for (long pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
      thisPixelAreaAvailable = pixelAreaAvailable.getValue(pixelIndex, 0);
      totalPixelAreaAvailable += thisPixelAreaAvailable;
      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        theseAllocations[cropIndex] +=
            thisPixelAreaAvailable * pixelFractions.getValue(pixelIndex, cropIndex);
      }
    }
    System.out.println("total pixel area available = " + totalPixelAreaAvailable);

    return theseAllocations;
  }

  private static double[] determineSurpluses(
      double[] initialAllocations, double[] footprintsAsDoubles) {
    double maxSurplus = Double.NEGATIVE_INFINITY;
    int maxSurplusIndex = -1;
    double surplusAmount = Double.NaN;

    int nCrops = initialAllocations.length;

    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      surplusAmount = initialAllocations[cropIndex] - footprintsAsDoubles[cropIndex];
      if (surplusAmount > maxSurplus) {
        maxSurplus = surplusAmount;
        maxSurplusIndex = cropIndex;
      }
      System.out.println(
          cropIndex
              + ": r = "
              + initialAllocations[cropIndex]
              + " t = "
              + footprintsAsDoubles[cropIndex]
              + " S = "
              + surplusAmount);
      //			System.out.println(cropNamesArray[cropIndex] + ": r = " + initialAllocations[cropIndex] +
      // "; t = " + footprintsAsDoubles[cropIndex] + "; S = " + surplusAmount);
    }
    System.out.println(
        "the most overallocated crop is " + maxSurplusIndex + " with " + maxSurplus + " extra ha");
    //		System.out.println("the most overallocated crop is " + cropNamesArray[maxSurplusIndex] + "
    // with " + maxSurplus + " extra ha");

    double[] returnObject = {
      maxSurplusIndex, maxSurplus,
    };
    return returnObject;
  }

  private static boolean fractionCheckIfProblems(
      double pixelTotalFractionCheck,
      int inIndexArrayIndex,
      int pixelIndexInRealTables,
      double thisTotalOldFractionsNonMax,
      double probabilityNumerator,
      double probabilityDenominator,
      double[] originalFractions,
      double[] newFractions,
      int nCrops,
      int maxSurplusIndex) {

    // some idiot checking for the moment... this is only checking on pixels we mess with
    //	if (Math.abs(pixelTotalFractionCheck - 1.0) > 0.00000001) {
    if (Math.abs(pixelTotalFractionCheck - 1.0) > 0.00000001
        || Double.isNaN(pixelTotalFractionCheck)) {

      System.out.println(
          "total pixel fraction is "
              + pixelTotalFractionCheck
              + " for sorted/real "
              + inIndexArrayIndex
              + "/"
              + pixelIndexInRealTables);
      System.out.println("thisTotalOldFractionsNonMax = " + thisTotalOldFractionsNonMax);
      //			System.out.println("probabilityComplementMultiplier = " +
      // probabilityComplementMultiplier);
      System.out.println("probabilityN/D = " + probabilityNumerator + "/" + probabilityDenominator);
      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        if (cropIndex == maxSurplusIndex) {
          System.out.println(
              "["
                  + cropIndex
                  + "] old = "
                  + originalFractions[cropIndex]
                  + " new = "
                  + newFractions[cropIndex]
                  + " <--- one to shrink");
        } else {
          System.out.println(
              "["
                  + cropIndex
                  + "] old = "
                  + originalFractions[cropIndex]
                  + " new = "
                  + newFractions[cropIndex]);
        }
      }
      return true;
    }

    return false;
  }

  private static MultiFormatMatrix rationalizeFractions(
      MultiFormatMatrix pixelFractions, MultiFormatMatrix pixelAreaAvailable, boolean verboseFlag)
      throws Exception {

    double fractionTotal = 0.0;
    double highestFraction = -1;
    int highestFractionIndex = -2;
    double lowestFraction = -3;
    int lowestFractionIndex = -4;
    double thisFraction = -5;
    double fractionLacking = -6;

    int nPixels = (int) pixelFractions.getDimensions()[0];
    int nCrops = (int) pixelFractions.getDimensions()[1];

    MultiFormatMatrix newPixelFractions =
        MatrixOperations.generateConstant(nPixels, nCrops, 0.0, 1);

    // we want to make everything add up to unity.
    // but we also want to mark which fractions will get ignored
    // because the area available is zero. we will mark this by
    // making everything negative for those cases...
    int nNoArea = 0; // this actually needs to be zero
    int nBoosted = 0; // this actually needs to be zero
    int nRobbed = 0; // this actually needs to be zero
    for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {

      if (pixelAreaAvailable.getValue(pixelIndex, 0) <= 0.0) {
        // this pixel will not be relevant
        for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
          //					newPixelFractions.setValue(pixelIndex,cropIndex, -1.0 *
          // pixelFractions.getValue(pixelIndex,cropIndex));
          newPixelFractions.setValue(pixelIndex, cropIndex, -1.0);
        }

        nNoArea++;

        continue;
      }

      fractionTotal = 0.0;
      highestFraction = Double.NEGATIVE_INFINITY;
      highestFractionIndex = -1;
      lowestFraction = Double.POSITIVE_INFINITY;
      lowestFractionIndex = nCrops + 1;
      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        thisFraction = pixelFractions.getValue(pixelIndex, cropIndex);
        fractionTotal += thisFraction;
        if (thisFraction > highestFraction) {
          highestFraction = thisFraction;
          highestFractionIndex = cropIndex;
        }
        if (thisFraction < lowestFraction) {
          lowestFraction = thisFraction;
          lowestFractionIndex = cropIndex;
        }
        // we need to actually copy the fractions over, which i have failed to do previously...
        // perhaps that was the source of some problems...
        newPixelFractions.setValue(pixelIndex, cropIndex, thisFraction);
      }
      fractionLacking = 1.0 - fractionTotal;

      // Beware the MAGIC ASSUMPTION!!!
      // very arbitrarily, if we have too much, let's take it away from
      // the highest fraction and if we have too little, we will add it to
      // the lowest fraction
      if (fractionLacking > 0) {
        // we need a little more fraction allocation
        // put it on the poorest crop
        newPixelFractions.setValue(
            pixelIndex, lowestFractionIndex, lowestFraction + fractionLacking);
        nBoosted++;
        if (verboseFlag) {
          System.out.println(
              "p "
                  + pixelIndex
                  + " fl = "
                  + fractionLacking
                  + " lfi = "
                  + lowestFractionIndex
                  + " lf = "
                  + lowestFraction
                  + " nv = "
                  + (lowestFraction + fractionLacking));
        }
      } else if (fractionLacking < 0) {
        // we need to take a little bit away
        // rob from the rich
        newPixelFractions.setValue(
            pixelIndex, highestFractionIndex, highestFraction + fractionLacking);
        nRobbed++;
        if (verboseFlag) {
          System.out.println(
              "p "
                  + pixelIndex
                  + " fl = "
                  + fractionLacking
                  + " hfi = "
                  + highestFractionIndex
                  + " hf = "
                  + highestFraction
                  + " nv = "
                  + (highestFraction + fractionLacking));
        }
      }
    }

    System.out.println(
        "nBoosted = "
            + nBoosted
            + " nRobbed = "
            + nRobbed
            + " nNoArea = "
            + nNoArea
            + " % changed: "
            + (nBoosted * 1.0 + nRobbed) * 100.0 / nPixels);

    return newPixelFractions;
  }

  private static double[] agnosticRedistribution(
      double newFractionForMaxSurplusCrop,
      int pixelIndexInRealTables,
      int maxSurplusIndex,
      MultiFormatMatrix pixelRevenueLike)
      throws Exception {

    int nCrops = (int) pixelRevenueLike.getDimensions()[1];

    double[] newFractions = new double[nCrops];
    double totalRelevantRevenues = 0.0;

    double amountToDistribute = 1.0 - newFractionForMaxSurplusCrop;
    // do the agnostic redistribution; but based loosely on the revenue-like values...
    // make sure this matches with the way we're doing it for the partial case...
    totalRelevantRevenues = 0.0;
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      if (cropIndex == maxSurplusIndex) {
        continue;
      }
      totalRelevantRevenues += pixelRevenueLike.getValue(pixelIndexInRealTables, cropIndex);
    }

    // let us see if a non-informative prior will move us along differently than using the
    // revenues...
    //		if (true) {
    if (totalRelevantRevenues == 0.0) {
      // go pure agnostic
      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        if (cropIndex == maxSurplusIndex) {
          newFractions[cropIndex] = newFractionForMaxSurplusCrop;
        } else {
          newFractions[cropIndex] =
              amountToDistribute
                  * 1.0
                  / (nCrops - 1.0); // we don't want the crop we just cleaned out...
        }
      }
    } else {

      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        if (cropIndex == maxSurplusIndex) {
          newFractions[cropIndex] = newFractionForMaxSurplusCrop;
        } else {
          newFractions[cropIndex] =
              amountToDistribute
                  * pixelRevenueLike.getValue(pixelIndexInRealTables, cropIndex)
                  / totalRelevantRevenues;
        }
      }
    }

    return newFractions;
  }

  private static double[] useScalingReduction(
      MultiFormatMatrix pixelFractions,
      MultiFormatMatrix pixelRevenueLike,
      MultiFormatMatrix pixelAreaAvailable,
      int pixelIndex,
      int maxSurplusIndex,
      double multiplierNeededToReduceToTarget)
      throws Exception {

    // , double multiplierNumerator, double multiplierDenominator
    int nCrops = (int) pixelFractions.getDimensions()[1];

    double[] originalFractions = new double[nCrops];
    double[] newFractions = new double[nCrops];
    double bountifulCropAreaToDrop = -5;
    double newFractionForMaxSurplusCrop = -6;
    //		double altNewFractionForMaxSurplusCrop = -6;
    double thisTotalOldFractionsNonMax = -7;
    double probabilityNumerator = -8;
    double probabilityDenominator = -9;

    int nPositiveOriginalFractions = -998172;

    // write down what the original fractions were and reset the new fractions to zero
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      originalFractions[cropIndex] = pixelFractions.getValue(pixelIndex, cropIndex);
      //			newFractions[cropIndex] = 0.0;
    }

    bountifulCropAreaToDrop =
        originalFractions[maxSurplusIndex] * pixelAreaAvailable.getValue(pixelIndex, 0);

    // if there is no relevant area to drop here, we'll just return the original fractions
    if (bountifulCropAreaToDrop == 0.0) {
      return originalFractions;
    }

    // now we need to reduce the area dedicated to our surplus crop and then re-allocate it to
    // everything else....
    newFractionForMaxSurplusCrop =
        multiplierNeededToReduceToTarget * originalFractions[maxSurplusIndex];
    //		altNewFractionForMaxSurplusCrop = multiplierNumerator * originalFractions[maxSurplusIndex] /
    // multiplierDenominator;

    //		if (newFractionForMaxSurplusCrop - altNewFractionForMaxSurplusCrop != 0) {
    //			System.out.println("nF = " + newFractionForMaxSurplusCrop + " != " +
    // altNewFractionForMaxSurplusCrop + " aNF; area diff = " +
    //					(pixelAreaAvailable.getValue(pixelIndex,0) * (newFractionForMaxSurplusCrop -
    // altNewFractionForMaxSurplusCrop))
    //			);
    //
    //		}

    // let us manually try it...
    thisTotalOldFractionsNonMax = 0.0;
    nPositiveOriginalFractions = 0;
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      if (cropIndex == maxSurplusIndex) {
        continue;
      }
      thisTotalOldFractionsNonMax += originalFractions[cropIndex];
      if (originalFractions[cropIndex] > 0.0) {
        nPositiveOriginalFractions++;
      }
    }

    probabilityNumerator = (1.0 - newFractionForMaxSurplusCrop);
    probabilityDenominator = thisTotalOldFractionsNonMax;

    // it could be that everything gets piled up, so we need to be clever about how to reduce in
    // that case...
    // this is another agnostic situation...
    if (probabilityDenominator == 0.0) {

      // do the agnostic redistribution; but based loosely on the revenue-like values...
      newFractions =
          agnosticRedistribution(
              newFractionForMaxSurplusCrop, pixelIndex, maxSurplusIndex, pixelRevenueLike);

    } else {
      // if there is only one other crop with representation, we can easily drop
      // into an oscillating thing where two crops trade who is most over-allocated
      // (most likely in a random or non-sensical prior and the switching goes between the
      // two smallest crops)
      //
      // if there aren't enough crops to do a sensible redistribution, let's go agnostic...
      if (nPositiveOriginalFractions > 1) {
        for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
          if (cropIndex == maxSurplusIndex) {
            newFractions[cropIndex] = newFractionForMaxSurplusCrop;
          } else {
            newFractions[cropIndex] =
                probabilityNumerator * originalFractions[cropIndex] / probabilityDenominator;
          }
        }
      } else {
        newFractions =
            agnosticRedistribution(
                newFractionForMaxSurplusCrop, pixelIndex, maxSurplusIndex, pixelRevenueLike);
      }
    } // probability denominator is just fine...

    return newFractions;
  }

  private static Object[] useSortingReduction(
      int[] sortedIndices,
      int inIndexArrayIndex,
      MultiFormatMatrix pixelFractions,
      MultiFormatMatrix pixelRevenueLike,
      MultiFormatMatrix pixelAreaAvailable,
      int maxSurplusIndex,
      double remainingSurplus)
      throws Exception {

    int nCrops = (int) pixelFractions.getDimensions()[1];

    double[] originalFractions = new double[nCrops];
    double[] newFractions = new double[nCrops];

    double bountifulCropAreaToDrop = -5;
    double thisTotalOldFractionsNonMax = -6;
    double probabilityNumerator = -7;
    double probabilityDenominator = -8;
    double amountToKeep = -9;
    double fractionToKeep = -10;
    double finalSurplusRemaining = -11;

    boolean bailNow = false;

    int pixelIndexInRealTables = sortedIndices[inIndexArrayIndex];
    // write down what the original fractions were
    // and reset the new fractions to zero
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      originalFractions[cropIndex] = pixelFractions.getValue(pixelIndexInRealTables, cropIndex);
      //			newFractions[cropIndex] = 0.0;
    }

    bountifulCropAreaToDrop =
        originalFractions[maxSurplusIndex] * pixelAreaAvailable.getValue(pixelIndexInRealTables, 0);

    // if there is no relevant area to drop here, we'll just return the original fractions
    if (bountifulCropAreaToDrop <= 0.0) {
      return new Object[] {bailNow, remainingSurplus, originalFractions};
    }

    // check to see if we can drop all of it...
    if (bountifulCropAreaToDrop < remainingSurplus) {
      // drop all of this pixel
      fractionToKeep = 0.0;
      amountToKeep = 0.0;

      bailNow = false;
    } else {
      // drop only the portion we need...
      amountToKeep = bountifulCropAreaToDrop - remainingSurplus;
      fractionToKeep = amountToKeep / pixelAreaAvailable.getValue(pixelIndexInRealTables, 0);

      // the surplus should be now zero, so let's break out...
      bailNow = true;
    }

    // apparently the 1.0 minus the one i want to drop is not working properly because of rounding
    // errors...
    // probabilityComplementMultiplier = 1 / (1.0 - originalFractions[maxSurplusIndex]);

    // let us manually try it...
    thisTotalOldFractionsNonMax = 0.0;
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      if (cropIndex == maxSurplusIndex) {
        continue;
      }
      thisTotalOldFractionsNonMax += originalFractions[cropIndex];
    }
    probabilityNumerator = 1.0 - fractionToKeep;
    probabilityDenominator = thisTotalOldFractionsNonMax;

    // now, we have failed to foresee that it could be that a pixel needs to
    // be dropped but is entirely dominated by a single crop. in that case,
    // let us agnostically re-distribute the fraction across all other crops...

    if (thisTotalOldFractionsNonMax == 0) {
      // do the agnostic redistribution; but based loosely on the revenue-like values...
      // make sure this matches with the way we're doing it for the partial case...

      // do the agnostic redistribution; but based loosely on the revenue-like values...
      // and we are dropping the entire allocation for this pixel, so the new amount is zero...
      newFractions =
          agnosticRedistribution(
              fractionToKeep, pixelIndexInRealTables, maxSurplusIndex, pixelRevenueLike);
    } else {
      // do the normal redistribution
      // update to make new fractions	there are several ways we could do this. the easiest is to
      // scale everything up...
      // make sure this matches with the way we're doing it for the partial case...
      for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
        if (cropIndex == maxSurplusIndex) {
          newFractions[cropIndex] = fractionToKeep; // this should be zero
        } else {
          newFractions[cropIndex] =
              probabilityNumerator * originalFractions[cropIndex] / probabilityDenominator;
        }
      }
    }

    //		remainingSurplus -= bountifulCropAreaToDrop;
    finalSurplusRemaining = remainingSurplus - (bountifulCropAreaToDrop - amountToKeep);

    return new Object[] {bailNow, finalSurplusRemaining, newFractions};
  }

  public static void main(String commandLineOptions[]) throws Exception {

    //		Runtime.getRuntime().addShutdownHook(new Thread() {
    //		public void run() {
    //		System.out.println("Exited!");
    //		}
    //		});

    TimerUtility bigTimer = new TimerUtility();
    TimerUtility littleTimer = new TimerUtility();

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    bigTimer.tic();
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(i + " " + commandLineOptions[i] + " " + bigTimer.tocNanos() + " ns\n");
      bigTimer.tic();
    }
    System.out.println();

    if (commandLineOptions.length < 5) {
      System.out.println("command line options are:");
      System.out.println("cropFootprintsFile  = commandLineOptions[0]");
      System.out.println("cropNamesFile       = commandLineOptions[1]");
      System.out.println("rastersBasename     = commandLineOptions[2]");
      System.out.println("outputBasename      = commandLineOptions[3]");
      System.out.println("maximumIterations   = commandLineOptions[4]");
      System.out.println("[verbose if exists] = commandLineOptions[5]");

      return;
    }

    // these will eventually become command line arguments, just i'm lazy right now...
    String cropFootprintsFile = null;
    String cropNamesFile = null;
    String rastersBasename = null;
    String outputBasename = null; // again, let's just supply this from the outside world...
    int maximumIterations = -1;
    boolean verboseFlag = false;

    cropFootprintsFile = commandLineOptions[0];
    cropNamesFile = commandLineOptions[1];
    rastersBasename = commandLineOptions[2];
    outputBasename = commandLineOptions[3];
    maximumIterations = Integer.parseInt(commandLineOptions[4]);

    if (commandLineOptions.length == 6) {
      verboseFlag = true;
    }

    String solvePathFilename = outputBasename + "_solvepath.txt";
    //		java.io.File silly = new java.io.File(solvePathFilename);
    // clear it out if it exists...
    (new java.io.File(solvePathFilename)).delete();

    double surplusTolerance = 1E-3; // Beware the MAGIC NUMBER!!!
    double surplusToleranceMultiplier = 1E-7; // Beware the MAGIC NUMBER!!!

    int defaultFormatIndex = 1;
    boolean verboseSorting = verboseFlag; // false;
    /////////////////////////////////

    // so....
    // the deal goes something like this
    // the "raster" table should have a bunch of columns corresponding to each of the crops
    // we are trying to allocate. there should be three blocks of columns:
    // block one has the values which are an initial guess as to the fraction of the pixel
    //           that is to be dedicated to that crop. this sets the priority WITHIN a pixel among
    // the crops
    // block two has the exponentiated fake revenues. this could really be any monotonic
    //           transformation of the fake revenues. basically, this is a priority setting
    //           BETWEEN the pixels for a particular crop
    // block three is a single column telling us how much area is available within that pixel
    //
    // then we are going to do something similar to the following:

    //		THIS roughly needs to be done separately for irrigated and rainfed... so a big double loop
    // or obvious choice for parallelization
    //		step one:	go through an initialize each pixel, giving preference to the highest revenue crop
    // (max or softmax)
    //		step two:	determine  the deficit/surplus for each crop
    //		step three:	select the crop with the most eggregious surplus
    //		step four:	within that crop, sort by revenue
    //		step five:	zero out the lowest pixels successively, until the surplus is eliminated
    //		step five A	re-allocate the zeroed out amounts to the other crops (e.g., softmax on
    // revenues, but dropping this crop)
    //		step six:	go back to step two, but rule out the crop(s) for which the correction has already
    // been accomplished

    System.out.println("--- load tabular data ---");
    // first, let us read in and process the list like files
    String[] cropNamesArray = FunTricks.readTextFileToArray(cropNamesFile);
    String[] footprintsAsStrings = FunTricks.readTextFileToArray(cropFootprintsFile);

    // let's determine how many crops there are and test out whether we read stuff correctly...
    int nCrops = footprintsAsStrings.length;

    // put the footprints in a double array
    double[] rawFootprintsAsDoubles = new double[nCrops];
    double[] footprintsAsDoubles = new double[nCrops];

    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      rawFootprintsAsDoubles[cropIndex] = Double.parseDouble(footprintsAsStrings[cropIndex]);
    }

    System.out.println("--- load pixel data ---");
    MultiFormatMatrix allPixelData = MatrixOperations.read2DMFMfromText(rastersBasename);

    // now we can split up the pixel tables into something a little more sensible
    long nPixels = allPixelData.getDimensions()[0];
    long nColsRaster = allPixelData.getDimensions()[1];

    // test to make sure that our raster table matches up with what we expect
    // Beware the MAGIC NUMBER!!! there should be two columns for each crop and then the final one
    // with the pixel area available
    if (2 * nCrops + 1 != nColsRaster) {
      System.out.println(
          "We have a problem: 2*nCrops + 1 ("
              + (2 * nCrops + 1)
              + ") != ("
              + nColsRaster
              + ") nColsRaster");
    }

    System.out.println("--- split out areas ---");

    // Beware the MAGIC NUMBER!!! we want all rows (-1 means "end"), and the final column which
    // should be index 2*nCrops
    MultiFormatMatrix pixelAreaAvailable =
        MatrixOperations.getSubmatrix(
            allPixelData, 0, -1, (2 * nCrops), (2 * nCrops), defaultFormatIndex);

    System.out.println("--- split out fractions ---");

    // Beware the MAGIC NUMBER!!! the first block should have the initial guesses
    // for the fractions of the ag area within a pixel for that crop
    MultiFormatMatrix pixelFractions =
        MatrixOperations.getSubmatrix(allPixelData, 0, -1, 0, (nCrops - 1), defaultFormatIndex);

    // make a copy of the pixel fractions that we can use as the ones to replace... just because i
    // like to be careful and inefficient...
    //		MultiFormatMatrix finalPixelFractions = MatrixOperations.newCopy(pixelFractions);

    System.out.println("--- split out revenues ---");

    // Beware the MAGIC NUMBER!!! the second block should have something reflecting the revenues or
    // other
    // prioritization score for comparing between pixels
    MultiFormatMatrix pixelRevenueLike =
        MatrixOperations.getSubmatrix(
            allPixelData, 0, -1, nCrops, (2 * nCrops - 1), defaultFormatIndex);

    ///////////////////////////////////////////////////
    // we need to adjust our target footprints       //
    // so that they will match with the available    //
    // pixel area...                                 //
    //                                               //
    // we will try to spread the deficit/surplus     //
    // around proportionally to the original targets //
    ///////////////////////////////////////////////////

    double totalPixelAreaAvailable = 0.0; // this really should be zero
    for (long pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
      totalPixelAreaAvailable += pixelAreaAvailable.getValue(pixelIndex, 0);
    }

    double totalTabularAreaNeeded = 0.0; // this really should be zero
    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      totalTabularAreaNeeded += rawFootprintsAsDoubles[cropIndex];
    }

    double multiplierForTabular = totalPixelAreaAvailable / totalTabularAreaNeeded;

    System.out.println(
        "total pixel area available = "
            + totalPixelAreaAvailable
            + " total tabular area needed = "
            + totalTabularAreaNeeded
            + " ratio = "
            + multiplierForTabular);

    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
      footprintsAsDoubles[cropIndex] = multiplierForTabular * rawFootprintsAsDoubles[cropIndex];
      System.out.println(
          cropNamesArray[cropIndex]
              + " = "
              + rawFootprintsAsDoubles[cropIndex]
              + " / "
              + footprintsAsDoubles[cropIndex]
              + " old/new ha footprint");
    }

    // decide on a breaking criterion for the big while loop: how close to zero do
    // we need the raster areas to match the tabular areas?
    surplusTolerance = surplusToleranceMultiplier * totalPixelAreaAvailable;

    ///////////////////////
    // some declarations //
    ///////////////////////
    double[] currentAllocations;

    double[] surplusInfo = null;
    int maxSurplusIndex = -3276;
    double maxSurplus = -7862.4;

    int allocationCounter = 0;

    SortComb11MFM prioritySortingObject = null;
    int[] sortedIndices = null;

    MultiFormatMatrix previousAllocationThisCrop = null;

    double multiplierNeededToReduceToTarget = 5;

    double remainingSurplus = -7; // initialize the amount remaining to be peeled off
    double thisTotalOldFractionsNonMax = -7;
    double[] originalFractions = new double[nCrops];
    double[] newFractions = new double[nCrops];
    double probabilityNumerator = -4;
    double probabilityDenominator = -4;
    double pixelTotalFractionCheck = -6;

    int pixelIndexInRealTables = -2;
    boolean bailNow = false;

    double firstRevenue = Double.NEGATIVE_INFINITY;

    boolean diversityOfRevenues = false;

    Object[] sortingResultBundle = null;
    // we also want to make sure that the fraction actually add up to unity...
    // so let's just do that up front so we start out with good stuff...
    pixelFractions = rationalizeFractions(pixelFractions, pixelAreaAvailable, verboseFlag);

    System.out.println("  === just finished a quick add-up check on the fractions ===");

    /////////////////////////////////////////////////
    // try to set up the big while loop over crops //
    /////////////////////////////////////////////////

    // do some initializations...
    // that is, let's assess the situation before starting so
    // that we can do the assessment at the end of the loop normally...

    System.out.println("--- tally up initial allocations ---");
    // let us begin by determining the initial areas allocated
    // this could be based on either the original pixelFractions or the finalPixelFractions
    // since they should be the same at this point.
    //
    // i may eventually decide to make them all one... but until i'm sure everything is working
    // i want to keep both around for comparison purposes...
    currentAllocations = determineTotalAllocations(pixelFractions, pixelAreaAvailable);

    // now we can assess. we also want to identify which crop has the greatest surplus
    surplusInfo = determineSurpluses(currentAllocations, footprintsAsDoubles);
    maxSurplusIndex = (int) surplusInfo[0];
    maxSurplus = surplusInfo[1];

    System.out.println("our most overallocated crop name is " + cropNamesArray[maxSurplusIndex]);

    // we're gonna switch things up a bit from the first attempt
    // let us sort on ag area allocated (i.e., fraction * agArea)
    // the idea here is that, possibly, pure revenue is not what matters.
    // the important thing is that at the particular location, this crop
    // is the most profitable. of course, we will not be able to attain that
    // because a) we don't purely observe that in the real world, b) our estimates
    // of the revenues/profits are imperfect and are revenues, not profits, and c) we
    // have to match up against the numbers we have which are themselves imperfect
    //
    // secondly, sorting on previous allocations only really matters if we believe
    // the revenues: that is, the revenues are based on some yield and price estimates
    // rather than being a default/blanket value for the unknown crops.
    //
    // so, we will first check if the maximally surplus crop has all the revenues identical.
    // yes: then we will determine the total area it has, the total area it needs and do
    //      a multiplier. we will then scale all the pixels accordingly. this will hopefully
    //      maintain the ignorance and get away from the problem of artificial concentration
    //      that we had in the previous algorithm.
    //
    // no:  then we will sort by "previous area allocated" and peel off the lowest allocated
    //      pixels.

    allocationCounter = 0;
    while (maxSurplus > surplusTolerance && allocationCounter < maximumIterations) {
      System.out.println(
          "               ... top of while loop #"
              + allocationCounter
              + " maxSurplusIndex = "
              + maxSurplusIndex
              + " maxSurplus = "
              + maxSurplus
              + " > "
              + surplusTolerance
              + " surplusTolerance ...");
      littleTimer.tic();

      // check for diversity of revenues; we will determine the min and max and if they
      // are the same we will assume that this means a default value was employed and we
      // don't really know anything.
      diversityOfRevenues = false;
      firstRevenue = pixelRevenueLike.getValue(0, maxSurplusIndex);
      for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
        if (pixelRevenueLike.getValue(pixelIndex, maxSurplusIndex) != firstRevenue) {
          diversityOfRevenues = true;
          break;
        }
      }

      // decide what to do based on possible default values having been used for revenues
      if (!diversityOfRevenues) {
        System.out.println("     low info, doing an even reduction");
        // do a standard scaling reduction to get everything back in line
        multiplierNeededToReduceToTarget =
            footprintsAsDoubles[maxSurplusIndex] / currentAllocations[maxSurplusIndex];
        System.out.println(
            "   -> multiplier = "
                + multiplierNeededToReduceToTarget
                + " n = "
                + footprintsAsDoubles[maxSurplusIndex]
                + " d = "
                + currentAllocations[maxSurplusIndex]
                + " mS = "
                + maxSurplus);

        // now, let's go through all the pixels...
        for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {

          // check for any area at all; if there isn't, just skip along
          if (pixelAreaAvailable.getValue(pixelIndex, 0) <= 0.0) {
            continue;
          }

          //					newFractions = useScalingReduction(finalPixelFractions,
          newFractions =
              useScalingReduction(
                  pixelFractions,
                  pixelRevenueLike,
                  pixelAreaAvailable,
                  pixelIndex,
                  maxSurplusIndex,
                  multiplierNeededToReduceToTarget);
          //					,
          //							footprintsAsDoubles[maxSurplusIndex], currentAllocations[maxSurplusIndex]
          //					);

          // copy the new fractions over to the final fraction array
          pixelTotalFractionCheck = 0.0;
          for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
            if (newFractions[cropIndex] < 0 || newFractions[cropIndex] > 1) {
              System.out.println(
                  "EVEN REDUCTION bad fraction: "
                      + newFractions[cropIndex]
                      + " realPixelIndex = "
                      + pixelIndex
                      + " cropIndex = "
                      + cropIndex);
            }
            //						originalFractions[cropIndex] =
            // finalPixelFractions.getValue(pixelIndex,cropIndex);
            originalFractions[cropIndex] = pixelFractions.getValue(pixelIndex, cropIndex);
            //						finalPixelFractions.setValue(pixelIndex, cropIndex, newFractions[cropIndex]);
            pixelFractions.setValue(pixelIndex, cropIndex, newFractions[cropIndex]);
            pixelTotalFractionCheck += newFractions[cropIndex];
          }

          // some idiot checking for the moment... this is only checking on pixels we mess with
          // note that we are going straight through, so the "in array" index and the "real" index
          // are the same...
          fractionCheckIfProblems(
              pixelTotalFractionCheck,
              pixelIndex,
              pixelIndex,
              thisTotalOldFractionsNonMax,
              probabilityNumerator,
              probabilityDenominator,
              originalFractions,
              newFractions,
              nCrops,
              maxSurplusIndex);

          //					finalPixelFractions.finishRecordingMatrix();
          pixelFractions.finishRecordingMatrix();
        } // end of for pixelIndex; even reductions loop

        // end of if we do not have actual information...
      } else {
        // we think we have actual information
        // sort on crop area allocated and zero out the least allocated pixels for this
        // crop so the overall allocation is maintained in the seemingly most important
        // areas identified previously.
        System.out.println("     HIGH info, doing a sorting-based reduction");

        //				MatrixOperations.getSubmatrix(finalPixelFractions, 0, -1, maxSurplusIndex,
        // maxSurplusIndex, defaultFormatIndex)
        previousAllocationThisCrop =
            MatrixOperations.multiplyMatricesElementwise(
                pixelAreaAvailable,
                MatrixOperations.getSubmatrix(
                    pixelFractions, 0, -1, maxSurplusIndex, maxSurplusIndex, defaultFormatIndex));

        // now, we want to sort on the previous allocation (which has but a single column) for that
        // crop...
        prioritySortingObject = new SortComb11MFM(previousAllocationThisCrop, 0, verboseSorting);

        prioritySortingObject.doSorting();

        // pull out the row indices in order from lowest to highest
        sortedIndices = prioritySortingObject.getSortedIndexList();

        // let's do this as a for loop with a break rather than as a while loop
        remainingSurplus = maxSurplus; // initialize the amount remaining to be peeled off
        bailNow = false;
        for (int inIndexArrayIndex = 0; inIndexArrayIndex < nPixels; inIndexArrayIndex++) {

          pixelIndexInRealTables = sortedIndices[inIndexArrayIndex];

          // first check to see if we have any area to bother allocating
          if (pixelAreaAvailable.getValue(pixelIndexInRealTables, 0) <= 0.0) {
            continue; // skip along
          }

          //					 finalPixelFractions,  pixelRevenueLike,  pixelAreaAvailable,
          sortingResultBundle =
              useSortingReduction(
                  sortedIndices,
                  inIndexArrayIndex,
                  pixelFractions,
                  pixelRevenueLike,
                  pixelAreaAvailable,
                  maxSurplusIndex,
                  remainingSurplus);

          bailNow = ((Boolean) sortingResultBundle[0]).booleanValue();
          remainingSurplus = ((Double) sortingResultBundle[1]).doubleValue();
          newFractions = (double[]) sortingResultBundle[2];

          // copy the new fractions over to the final fraction array
          pixelTotalFractionCheck = 0.0;

          for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
            if (newFractions[cropIndex] < 0 || newFractions[cropIndex] > 1) {
              System.out.println(
                  "SORTING bad fraction: "
                      + newFractions[cropIndex]
                      + " for sortedIndex = "
                      + inIndexArrayIndex
                      + " realPixelIndex = "
                      + pixelIndexInRealTables
                      + " cropIndex = "
                      + cropIndex);
            }
            //						originalFractions[cropIndex] =
            // finalPixelFractions.getValue(pixelIndexInRealTables,cropIndex);
            originalFractions[cropIndex] =
                pixelFractions.getValue(pixelIndexInRealTables, cropIndex);
            //						finalPixelFractions.setValue(pixelIndexInRealTables, cropIndex,
            // newFractions[cropIndex]);
            pixelFractions.setValue(pixelIndexInRealTables, cropIndex, newFractions[cropIndex]);
            pixelTotalFractionCheck += newFractions[cropIndex];
          }

          // some idiot checking for the moment... this is only checking on pixels we mess with
          boolean thereWereProblems =
              fractionCheckIfProblems(
                  pixelTotalFractionCheck,
                  inIndexArrayIndex,
                  pixelIndexInRealTables,
                  thisTotalOldFractionsNonMax,
                  probabilityNumerator,
                  probabilityDenominator,
                  originalFractions,
                  newFractions,
                  nCrops,
                  maxSurplusIndex);

          if (thereWereProblems) {
            System.out.println(
                "Area Available above was = "
                    + pixelAreaAvailable.getValue(pixelIndexInRealTables, 0));
          }

          //					finalPixelFractions.finishRecordingMatrix();
          pixelFractions.finishRecordingMatrix();

          // check if we should break out...
          if (bailNow) {
            break;
          }
        } // end of for loop looking in the index array
      } // end of else: we have actual information

      // make a little record of how we got here....
      FunTricks.appendLineToTextFile(
          allocationCounter + "\t" + maxSurplus + "\t" + maxSurplusIndex, solvePathFilename, true);

      // recheck the surpluses...

      //			currentAllocations = determineTotalAllocations(finalPixelFractions, pixelAreaAvailable);
      currentAllocations = determineTotalAllocations(pixelFractions, pixelAreaAvailable);
      surplusInfo = determineSurpluses(currentAllocations, footprintsAsDoubles);

      maxSurplusIndex = (int) surplusInfo[0];
      maxSurplus = surplusInfo[1];

      System.out.println(
          "our most overallocated crop name is "
              + cropNamesArray[maxSurplusIndex]
              + " mSI = "
              + maxSurplusIndex
              + " mS = "
              + maxSurplus
              + " ");

      //			MatrixOperations.write2DMFMtoText(pixelFractions, outputBasename + allocationCounter,
      // "\t");

      allocationCounter++;

      // a debugging thing if necessary...
      //			if (allocationCounter > 1510) {
      //				MatrixOperations.write2DMFMtoText(pixelFractions, outputBasename + (allocationCounter %
      // 3), "\t");
      //			}

      System.out.println("this cycle finished after " + littleTimer.TOCSeconds() + " seconds");
      System.out.println();
      System.out.println();
    } // end of giant while loop over crops to equalize...

    if (allocationCounter >= maximumIterations) {
      System.out.println("   --> exited due to excessive iterations <--");
    }

    // we should be all done here, so write it out...
    //		MatrixOperations.write2DMFMtoText(finalPixelFractions, outputBasename, "\t");
    MatrixOperations.write2DMFMtoText(pixelFractions, outputBasename, "\t");

    System.out.println(bigTimer.sinceStartMessage());
  } // main
}
