

package org.chardAllocator;

import java.io.File;

import org.R2Useful.*;
import org.R2Useful.MultiFormatMatrix;

public class chardOne {



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

	inputDataFile         = commandLineOptions[0];
	outputFile            = commandLineOptions[1];

	
	// ok, this thingamajig is an attempt at a simple allocator for the zambia
	// household-survey-meets-national-allocation of crop areas while contemplating
	// risk project.
	//
	// the econometric model is based on survey data representative at the provincial level
	// so, we make predictions over all of our households and then add them up within provinces
	//
	// of course, there are more households in the country than in our dataset, so we can
	// look at the predictions and get (hopefully) representative shares in each crop
	//
	// the point of this step is to try to pixelize those provincial level shares.
	//
	// this first attempt is to try to keep provincial level shares at the model-predicted shares
	// as well as keep pixels at the same size as they are in SPAM.
	//
	// this will be based purely on heuristic guesses....
	
	// read in the overall survey totals to a double array so we can then convert them to shares
	// they are supposed to be in the same order as the data in the pixel file (except the pixel
	// file will have an extra column up front with the total area
	// it will be comma delimited and should be a single line

	String[] regionalSurveyAreasByCropAsStringArray = FunTricks.readTextFileToArray(inputDataFile + ".totals.txt");
	
	// Beware the MAGIC NUMBER!!! comma delimited... and there should be a single line in that file...
	String totalsDelimiter = ",";
	String[] totalsByCropString = regionalSurveyAreasByCropAsStringArray[0].split(totalsDelimiter);
	int nCrops = totalsByCropString.length;
	double[] totalSurveyAreasByCrop = new double[nCrops];
	// compute the total survey area, since we already have a for loop anyway
	double totalSurveyArea = 0.0;
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    totalSurveyAreasByCrop[cropIndex] = Double.parseDouble(totalsByCropString[cropIndex]);
	    totalSurveyArea += totalSurveyAreasByCrop[cropIndex];
	}
	
	// we will make them into shares later so that we can keep all the reading together and all
	// the processing separate.
	
	
	MultiFormatMatrix rawInputData = MatrixOperations.read2DMFMfromTextForceFormat(inputDataFile + "_data", MultiFormatMatrix.dataInVector);

	// idiot check to make sure we have the correct number of columns...
	// number of columns (dimension index 1) and ignore the first/area column
	if (nCrops != (int) (rawInputData.getDimensions()[1] - 1)) {
	    System.out.println("totals file has a different number of columns than the pixel data file (after dropping the areas column)");
	    System.out.println(nCrops + " (nCrops != (int) (rawInputData.getDimensions()[1] - 1)) " + (int) (rawInputData.getDimensions()[1] - 1));
	    throw new Exception();
	}
	    
	int nPixels = (int) (rawInputData.getDimensions()[0]); // number of rows (dimension index 0)

	// split everything up into their own columns...
	// the available area for each pixel is in the first column
	MultiFormatMatrix areaAvailable = MatrixOperations.getSubmatrix(rawInputData, 0, -1, 0, 0, MultiFormatMatrix.dataInVector);
	
	// the original crop area guesses are in the second through the end columns, hence the offset of one
	// Beware the MAGIC NUMBER!!! the offset of one to get past the area available column
	MultiFormatMatrix originalCropAreas = MatrixOperations.getSubmatrix(rawInputData,0,-1,1,nCrops,MultiFormatMatrix.dataInVector);
	
	
	
	////////////////////////////////
	// alrighty, let's get started!
	////////////////////////////////

	
	// compute the shares
	double[] surveySharesByCrop = new double[nCrops];
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    surveySharesByCrop[cropIndex] = totalSurveyAreasByCrop[cropIndex] / totalSurveyArea;
	    System.out.println("survey share [" + cropIndex + "] = " + surveySharesByCrop[cropIndex]);
	}
	
	
	// how much area is available in the entire region according to the pixel-level information?
	double totalAreaAvailableFromPixels = MatrixOperations.sumToScalar(areaAvailable);

	double totalAreaInOriginalGuesses = MatrixOperations.sumToScalar(originalCropAreas);
	
	
	// so, the first no-brainer is to force the pixel-level to match the summary for *this* machine's precision and rounding tendencies...
	// let's be silly and do the operation in two steps in hopes of minimizing rounding errors on small values...
	MultiFormatMatrix simpleScaledOriginalCropAreas = MatrixOperations.divideByConstant(totalAreaInOriginalGuesses,
		MatrixOperations.multiplyByConstant(totalAreaAvailableFromPixels, originalCropAreas)
		);
	
	double totalAreaInSimpleRescaling = MatrixOperations.sumToScalar(simpleScaledOriginalCropAreas);
	
	
	// ok, now, perhaps we should figure out which pixels are the most over/under allocated in the super simple starting point
	MultiFormatMatrix totalAreaRequestedBySimpleScaledOriginalCropAreas = MatrixOperations.sumToCol(simpleScaledOriginalCropAreas);
	
	MultiFormatMatrix overAllocationInSimpleScaledOriginalCropAreas = 
		MatrixOperations.subtractMatrices(totalAreaRequestedBySimpleScaledOriginalCropAreas,  areaAvailable);

	double checkSumTotalAreaRequestedBySimpleScaledOriginalCropAreas = MatrixOperations.sumToScalar(totalAreaRequestedBySimpleScaledOriginalCropAreas);
	double checkSumOverAllocationInSimpleScaledOriginalCropAreas = MatrixOperations.sumToScalar(overAllocationInSimpleScaledOriginalCropAreas);

	
	System.out.println("totalAreaAvailableFromPixels = " + totalAreaAvailableFromPixels);
	System.out.println("totalAreaInOriginalGuesses   = " + totalAreaInOriginalGuesses);

	System.out.println("totalAreaInSimpleRescaling   = " + totalAreaInSimpleRescaling);
	System.out.println("checkSumTotalAreaRequestedBySimpleScaledOriginalCropAreas   = " + checkSumTotalAreaRequestedBySimpleScaledOriginalCropAreas);
	System.out.println("checkSumOverAllocationInSimpleScaledOriginalCropAreas   = " + checkSumOverAllocationInSimpleScaledOriginalCropAreas);
	
	
	// ok, let's try to lay out a simple adjustment scheme.
	
	// sort in order by overallocated-ness
	
	// start with the most overallocated pixel; scale it down to fit
	//   figure out how much area was lost by each crop in the process
	// now, presumably, the most attractive pixels are the over-allocated ones. so, we want
	// to preserve that as much as possible. hence, we should squeeze these fragments into
	// the most attractive under-allocated pixels. that would mean those which are just barely
	// under-allocated. so, we'll find the first (tiny) negative overallocations and start squeezing
	// the fragments in there... thus, hopefully, the fattest stuff will stay high and the junky stuff
	// will match up with the junky stuff.
	//   of course, this spreading of the single-pixel excess has to be done across crops. i guess we
	//   will be simplistic and just go left to right and just take care of them in order instead of trying
	//   to keep track of scaling and what not....
	// possibly, in a second step, one the pixel-level feasibility is met, we can try to improve on the
	// mix. but first things first.

	
	
	
	
	// let's sort by over-allocationn
	MultiFormatMatrix sortedOverAllocation = null;
	int[] sortedOverAllocationIndices = null;

	SortComb11MFM sortingObject = new SortComb11MFM(overAllocationInSimpleScaledOriginalCropAreas, 0, false);
	sortingObject.doSorting();


	sortedOverAllocation = MatrixOperations.newCopy(sortingObject.getSortedColumnOnly());
	sortedOverAllocationIndices = sortingObject.getSortedIndexList();

	// check on what we've discovered...
	System.out.println("sorted index 0 came from original index " + sortedOverAllocationIndices[0] + " and has a value of " + sortedOverAllocation.getValue(0,0));
	System.out.println("sorted index " + (nPixels - 1) + " came from original index " + sortedOverAllocationIndices[nPixels - 1] + " and has a value of " + sortedOverAllocation.getValue(nPixels - 1,0));

	// and just for kicks, let's see if our aggregated pixel shares are starting out matched up with the tabular survey shares
	MultiFormatMatrix rawAggregatedPixelShares = MatrixOperations.divideByConstant(
		totalAreaInOriginalGuesses,MatrixOperations.sumToRow(originalCropAreas)
		);
	MultiFormatMatrix startingAggregatedPixelShares = MatrixOperations.divideByConstant(
		totalAreaInSimpleRescaling,MatrixOperations.sumToRow(simpleScaledOriginalCropAreas)
		);
	
	// alrighty. so, the overallocated ones have positive values and are thus at the end of the list.
	for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
	    System.out.println("crop [" + cropIndex + "] survey share = " 
		    + surveySharesByCrop[cropIndex] + "; agg. pixel share = " + startingAggregatedPixelShares.getValue(0,cropIndex)
		    + "; original agg. pixel share = " + rawAggregatedPixelShares.getValue(0,cropIndex)
		    );
	}

	
	
	
	
	/*
	
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
	
	*/
	
	
	
	
	
	// we're all done
	
	System.out.println(bigTimer.sinceStartMessage());


    } // main

}

