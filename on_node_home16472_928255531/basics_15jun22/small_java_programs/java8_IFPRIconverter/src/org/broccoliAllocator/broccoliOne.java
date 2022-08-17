

package org.broccoliAllocator;

import java.io.File;

import org.R2Useful.*;

public class broccoliOne {



    public static void main(String commandLineOptions[]) throws Exception {

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


	// these will eventually become command line arguments, just i'm lazy right now...
	String inputDataFile = null;
	String outputFile    = null;
	String productionTargetsFile    = null;

	inputDataFile         = commandLineOptions[0];
	outputFile            = commandLineOptions[1];
	productionTargetsFile = commandLineOptions[2];

	// read in the targets to a double array
	String[] targetsAsStrings = FunTricks.readTextFileToArray(productionTargetsFile);
	double[] targetsByCrop = new double[targetsAsStrings.length];
	for (int cropIndex = 0; cropIndex < targetsAsStrings.length; cropIndex++) {
	    targetsByCrop[cropIndex] = Double.parseDouble(targetsAsStrings[cropIndex]);
	}
	
	
	MultiFormatMatrix rawInputData = MatrixOperations.read2DMFMfromTextForceFormat(inputDataFile, MultiFormatMatrix.dataInVector);
	
//	MultiFormatMatrix rawInputData = MatrixOperations.read2DMFMfromTextForceFormat(inputDataFile, MultiFormatMatrix.dataInVector);
	
	// split everything up into their own columns...
	MultiFormatMatrix areaAvailable = MatrixOperations.getSubmatrix(rawInputData, 0, -1, 0, 0, MultiFormatMatrix.dataInVector);
	
	int nCrops = (int) (rawInputData.getDimensions()[1] - 1); // number of columns (dimension index 1) and ignore the first/area column
	int nPixels = (int) (rawInputData.getDimensions()[0]); // number of rows (dimension index 0)

	// idiot check that our targets and crops match...
	if (nCrops != targetsByCrop.length) {
	    System.out.println("Mismatch between number of targets (" + targetsByCrop.length + ") and number of yields (" + nCrops + ")");
	}
	
	MultiFormatMatrix[] originalCropYields = new MultiFormatMatrix[nCrops];
	
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    originalCropYields[cropIndex] = MatrixOperations.getSubmatrix(rawInputData,0,-1,cropIndex+1,cropIndex+1,MultiFormatMatrix.dataInVector);
	}

	
	// ok, now, let's sort each by yield
	MultiFormatMatrix[] sortedCropYields = new MultiFormatMatrix[nCrops];
	int[][] sortedCropYieldsIndices = new int[nCrops][];
	
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    SortComb11MFM sortingObject = new SortComb11MFM(originalCropYields[cropIndex], 0, false);
	    sortingObject.doSorting();
	    
	    
	    sortedCropYields[cropIndex] = MatrixOperations.newCopy(sortingObject.getSortedColumnOnly());
	    sortedCropYieldsIndices[cropIndex] = sortingObject.getSortedIndexList();
	    
//	    System.out.println("yields for " + cropIndex + " has this many elements: " + sortedCropYields[cropIndex].getTotalNumberOfElements());
//	    System.out.println("indices for " + cropIndex + " has this many elements: " + sortedCropYieldsIndices[cropIndex].length);

	}

	// now, let's figure up the total production for the fun of it to do an idiot check to see if
	// each crop could be produced in isolation.
	MultiFormatMatrix[] possibleProductionByCrop = new MultiFormatMatrix[nCrops];
	MultiFormatMatrix[] totalPossibleProductionByCrop = new MultiFormatMatrix[nCrops];
	boolean allCropsFeasibleInIsolation = true;
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    possibleProductionByCrop[cropIndex] = MatrixOperations.multiplyMatricesElementwise(areaAvailable, originalCropYields[cropIndex]);
	    totalPossibleProductionByCrop[cropIndex] = MatrixOperations.sumToRow(possibleProductionByCrop[cropIndex]);
	    System.out.println("crop index " + cropIndex + ": target = " + targetsByCrop[cropIndex] 
		    + ", max possible = " + totalPossibleProductionByCrop[cropIndex].getValue(0, 0));
	    
	    if (targetsByCrop[cropIndex] > totalPossibleProductionByCrop[cropIndex].getValue(0, 0)) {
		allCropsFeasibleInIsolation = false;
	    }
	}
	
	if (!allCropsFeasibleInIsolation) {
	    System.out.println("At least one crop cannot meet its target even if it takes up the whole region. Bailing out.");
	    throw new Exception();
	}
	
	
	// let's try an initial round-robin style allocation. once we get something, ANYTHING, feasible, then
	// we can work to revise it.
	
	// initialize a place to keep track of which pixel gets which crop
	int[] pixelInUseByThisCrop = new int[nPixels];
	
	// make sure it is initialized to all nonsense
	for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
	    pixelInUseByThisCrop[pixelIndex] = Integer.MIN_VALUE;
	}

	// now, we will cycle through the crops and assign the highest yielding available pixel to each
	// until we hit the target production amount.
	
	double[] productionSoFar = new double[nCrops];
	int[] nextInvertedPixelRank = new int[nCrops];
	// recall that the sorting is from least to greatest, so we need to start at the bottom and work our way up...
	// we can then use the sorting original indices to convert from rank to actual pixel number and then get the yield / production value...
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) { nextInvertedPixelRank[cropIndex] = nPixels - 1; }
	
	int realPixelIndexFromThisRank = -5;
	
	// as usual, construct as an infinite loop and then put the break condition back in once we figure it out...
	String reportLine = null;
	int stepCounter = 0;
	int cropUnderConsideration = 0;
	int candidateCropUnderConsideration = -2;
	boolean allTargetsAreMet = false;
	while (!allTargetsAreMet) {


	    // list out the current state of affairs, just to make sure...
//	    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
//		System.out.println("   nipRank[" + cropIndex + "] = " + nextInvertedPixelRank[cropIndex]);
//	    } // end for checking crops for having enough production yet 

	    
	    // find the real pixel index associated with this rank...
	    realPixelIndexFromThisRank = sortedCropYieldsIndices[cropUnderConsideration][ nextInvertedPixelRank[cropUnderConsideration] ];
	    
	    // check to see if it is already occupied
	    while (pixelInUseByThisCrop[realPixelIndexFromThisRank] >= 0) {
		// the pixel is in use, so let's step back by one... but keep the crop the same...
		System.out.println("     collision: rank=" + nextInvertedPixelRank[cropUnderConsideration] + " pixel=" + realPixelIndexFromThisRank 
			+ " held by=" + pixelInUseByThisCrop[realPixelIndexFromThisRank]);
		nextInvertedPixelRank[cropUnderConsideration]--;
		
		realPixelIndexFromThisRank = sortedCropYieldsIndices[cropUnderConsideration]
			[ nextInvertedPixelRank[cropUnderConsideration] ];
	    }
	    
	    // at this point, we should have an open pixel. so, let's assign it...
	    pixelInUseByThisCrop[realPixelIndexFromThisRank] = cropUnderConsideration;
	    // and record its production in the running total.
	    productionSoFar[cropUnderConsideration] += possibleProductionByCrop[cropUnderConsideration].getValue(new long[] {realPixelIndexFromThisRank, 0});

	    // let's walk this back here so that we don't get all those "collisions"
	    nextInvertedPixelRank[cropUnderConsideration]--;

	    
	    // report progress...
	    reportLine = stepCounter + " crop=" + cropUnderConsideration + " got pixel " + realPixelIndexFromThisRank;
	    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
		reportLine += " " + cropIndex + ") " + (targetsByCrop[cropIndex] - productionSoFar[cropIndex]);
	    }

	    System.out.println(reportLine);
	    
	    // cycle to the next crop
//	    cropUnderConsideration = (cropUnderConsideration + 1) % nCrops;
	    
	    // check if that crop actually needs any more allocation
	    // Beware the MAGIC NUMBER!!! starting the offset at 1 since we don't want to stay on the same crop
	    // unless all the others are satisfied. similarly, we will add one to the stop condition so that
	    // if all the others are satisfied, we end up cycling back around to staying put.
	    for (int cropOffsetIndex = 1; cropOffsetIndex < nCrops + 1; cropOffsetIndex++) {
		candidateCropUnderConsideration = (cropUnderConsideration + cropOffsetIndex) % nCrops;
//		System.out.println("  offset=" + cropOffsetIndex + "; before=" + candidateBeforeFolding + " cand=" + candidateCropUnderConsideration);
		
		if (productionSoFar[candidateCropUnderConsideration] < targetsByCrop[candidateCropUnderConsideration]) {
		    // the candidate is still lacking, so let's go with that one...
		    cropUnderConsideration = candidateCropUnderConsideration;
		    break;
		}
	    } // end for checking crops for having enough production yet 

	    
	    // ok, if we get all the way to here, either we updated the crop under consideration (possibly to the original value)
	    // or we didn't because we have enough production. the "have enough production" case should be caught in the next little
	    // for loop...
	    
	    // check to see if we should break
	    allTargetsAreMet = true;
	    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
		if (productionSoFar[cropIndex] < targetsByCrop[cropIndex]) {
		    allTargetsAreMet = false;
		    break;
		}
	    } // end for checking crops for having enough production yet 
	    
	    // bump up the curiosity counter.
	    stepCounter++;
	    
	} // end while targets remain unsatisfied
	

	// ok, so obviously that will work perfectly the first time without any debugging, so we might as well try to export the results, right?
	FunTricks.writeInfoFile(outputFile, nPixels, 1, "\t");
	// clear out the output file...
	File outputFileObject = new File(outputFile + ".txt");
	if (outputFileObject.exists()) { 
	    System.out.println("Warning: deleting file: " + outputFileObject); 
	    outputFileObject.delete();
	}
	
	for (int pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
	    FunTricks.appendLineToTextFile((Integer.toString(pixelInUseByThisCrop[pixelIndex])), outputFile + ".txt", true);
	}
	
	
	if (1 == 2) {
	    // a quick look through. and it works. yipee! now on to the harder parts...
	    for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
		for (int rowIndex = 0; rowIndex < sortedCropYieldsIndices[cropIndex].length; rowIndex++) {
		    System.out.println("y = [" + cropIndex + "][" + rowIndex + "] = "
			    + sortedCropYields[cropIndex]
				    .getValue(new long[] {rowIndex , 0}) + 
				    "; i = [" + cropIndex + "][" + rowIndex + "] = " + sortedCropYieldsIndices[cropIndex][rowIndex]);
		}
	    }
	}

	
	
	
	
	
	// we're all done
	
	System.out.println(bigTimer.sinceStartMessage());


    } // main

}

