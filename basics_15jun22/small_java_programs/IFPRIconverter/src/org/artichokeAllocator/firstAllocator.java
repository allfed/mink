package org.artichokeAllocator;

import org.R2Useful.*;

public class firstAllocator {


	private static double[] determineTotalAllocations(MultiFormatMatrix pixelFractions, MultiFormatMatrix pixelAreaAvailable)
	throws Exception {
		long nPixels = (int)pixelFractions.getDimensions()[0];
		int nCrops   = (int)pixelFractions.getDimensions()[1];
		
		double[] theseAllocations = new double[nCrops];
		double thisPixelAreaAvailable = -5;
		double totalPixelAreaAvailable = 0.0; // this really should be zero
		
		for (long pixelIndex = 0; pixelIndex < nPixels; pixelIndex++) {
			thisPixelAreaAvailable = pixelAreaAvailable.getValue(pixelIndex,0);
			totalPixelAreaAvailable += thisPixelAreaAvailable;
			for (int cropIndex = 0; cropIndex < nCrops ; cropIndex++) {
				theseAllocations[cropIndex] += thisPixelAreaAvailable * pixelFractions.getValue(pixelIndex,cropIndex);
			}
		}
		System.out.println("total pixel area available = " + totalPixelAreaAvailable);

		return theseAllocations;
	}

	private static double[] determineSurpluses(double[] initialAllocations, double[] footprintsAsDoubles) {
		double maxSurplus = Double.NEGATIVE_INFINITY;
		int maxSurplusIndex = -1;
		double surplusAmount = Double.NaN;
		
		int nCrops = initialAllocations.length;
		
		for (int cropIndex = 0; cropIndex < nCrops ; cropIndex++) {
			surplusAmount = initialAllocations[cropIndex] - footprintsAsDoubles[cropIndex];
			if (surplusAmount > maxSurplus) {
				maxSurplus = surplusAmount;
				maxSurplusIndex = cropIndex;
			}
			System.out.println(cropIndex + ": r = " + initialAllocations[cropIndex] + " t = " + footprintsAsDoubles[cropIndex] + " S = " + surplusAmount);
//			System.out.println(cropNamesArray[cropIndex] + ": r = " + initialAllocations[cropIndex] + "; t = " + footprintsAsDoubles[cropIndex] + "; S = " + surplusAmount);
		}
		System.out.println("the most overallocated crop is " + maxSurplusIndex + " with " + maxSurplus + " extra ha");
//		System.out.println("the most overallocated crop is " + cropNamesArray[maxSurplusIndex] + " with " + maxSurplus + " extra ha");

		double[] returnObject = {maxSurplusIndex, maxSurplus,};
		return returnObject;
	}
	
	
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
		String cropFootprintsFile = null;
		String cropNamesFile      = null;
		String rastersBasename    = null;
		String outputBasename     = null; // again, let's just supply this from the outside world...
		
		if (true) {
			cropFootprintsFile =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\crop_footprints_irrigated_TMM_THA.txt";

			cropNamesFile =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\crop_names_irrigated_TMM_THA.txt";

			rastersBasename =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\raster_full_irrigated_TMM_THA_data";

			outputBasename = "D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\to_GRASS\\ALLOCATEDs_irrigated_TMM_THA"; // again, let's just supply this from the outside world...

		} else {

			cropFootprintsFile =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\crop_footprints_rainfed_TMM_THA.txt";

			cropNamesFile =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\crop_names_rainfed_TMM_THA.txt";

			rastersBasename =
				"D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\from_GRASS\\raster_full_rainfed_TMM_THA_data";

			outputBasename = "D:\\rdrobert\\global_futures\\toy_land_use\\java_allocator\\to_GRASS\\ALLOCATEDs_rainfed_TMM_THA"; // again, let's just supply this from the outside world...

		}
		
		
		double surplusTolerance = 1E-3; // Beware the MAGIC NUMBER!!!
		double surplusToleranceMultiplier = 1E-8; // Beware the MAGIC NUMBER!!!

		long randomSeed = 531264987267132L;
		
		boolean verboseSorting = false;
		/////////////////////////////////
		
		
		// so....
		// the deal goes something like this
		// the "raster" table should have a bunch of columns corresponding to each of the crops
		// we are trying to allocate. there should be three blocks of columns:
		// block one has the values which are an initial guess as to the fraction of the pixel
		//           that is to be dedicated to that crop. this sets the priority WITHIN a pixel among the crops
		// block two has the exponentiated fake revenues. this could really be any monotonic
		//           transformation of the fake revenues. basically, this is a priority setting
		//           BETWEEN the pixels for a particular crop
		// block three is a single column telling us how much area is available within that pixel
		//
		// then we are going to do something similar to the following:
		/*
		THIS roughly needs to be done separately for irrigated and rainfed... so a big double loop or obvious choice for parallelization	
		step one:	go through an initialize each pixel, giving preference to the highest revenue crop (max or softmax)
		step two:	determine  the deficit/surplus for each crop
		step three:	select the crop with the most eggregious surplus
		step four:	within that crop, sort by revenue
		step five:	zero out the lowest pixels successively, until the surplus is eliminated
		step five A	re-allocate the zeroed out amounts to the other crops (e.g., softmax on revenues, but dropping this crop)
		step six:	go back to step two, but rule out the crop(s) for which the correction has already been accomplished
		*/

		System.out.println("--- load tabular data ---");
		// first, let us read in and process the list like files
		String[] cropNamesArray      = FunTricks.readTextFileToArray(cropNamesFile);
		String[] footprintsAsStrings = FunTricks.readTextFileToArray(cropFootprintsFile);
		
		
		// let's determine how many crops there are and test out whether we read stuff correctly...
		int nCrops = footprintsAsStrings.length;
		
		// put the footprints in a double array
		double[] rawFootprintsAsDoubles = new double[nCrops];
		double[] footprintsAsDoubles = new double[nCrops];
		
		for (int cropIndex = 0; cropIndex < nCrops ; cropIndex++) {
			rawFootprintsAsDoubles[cropIndex] = Double.parseDouble(footprintsAsStrings[cropIndex]);
		}
		
		
		
		System.out.println("--- load pixel data ---");
		MultiFormatMatrix allPixelData = MatrixOperations.read2DMFMfromText(rastersBasename);
		
		// now we can split up the pixel tables into something a little more sensible
		long nPixels = allPixelData.getDimensions()[0];
		long nColsRaster = allPixelData.getDimensions()[1];
		
		// test to make sure that our raster table matches up with what we expect
		// Beware the MAGIC NUMBER!!! there should be two columns for each crop and then the final one with the pixel area available
		if (2 * nCrops + 1 != nColsRaster) {
			System.out.println("We have a problem: 2*nCrops + 1 (" + (2 * nCrops + 1) + ") != (" + nColsRaster + ") nColsRaster");
		}

		System.out.println("--- split out areas ---");

		// Beware the MAGIC NUMBER!!! we want all rows (-1 means "end"), and the final column which should be index 2*nCrops
		MultiFormatMatrix pixelAreaAvailable = MatrixOperations.getSubmatrix(allPixelData, 0, -1, (2 * nCrops), (2 * nCrops), 1);

		System.out.println("--- split out fractions ---");

		// Beware the MAGIC NUMBER!!! the first block should have the initial guesses
		// for the fractions of the ag area within a pixel for that crop
		MultiFormatMatrix pixelFractions = MatrixOperations.getSubmatrix(allPixelData, 0, -1, 0, (nCrops - 1), 1);

		// make a copy of the pixel fractions that we can use as the ones to replace... just because i like to be careful and inefficient...
		MultiFormatMatrix finalPixelFractions = MatrixOperations.newCopy(pixelFractions);

		System.out.println("--- split out revenues ---");

		// Beware the MAGIC NUMBER!!! the second block should have something reflecting the revenues or other
		// prioritization score for comparing between pixels
		MultiFormatMatrix pixelRevenueLike = MatrixOperations.getSubmatrix(allPixelData, 0, -1, nCrops, (2 * nCrops - 1), 1);
		
//		MatrixOperations.write2DMFMtoText(pixelRevenueLike, outputBasename + "REV", "\t");

		
		
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
			totalPixelAreaAvailable += pixelAreaAvailable.getValue(pixelIndex,0);
		}

		double totalTabularAreaNeeded = 0.0; // this really should be zero
		for (int cropIndex = 0; cropIndex < nCrops ; cropIndex++) {
			totalTabularAreaNeeded += rawFootprintsAsDoubles[cropIndex];
		}

		double multiplierForTabular = totalPixelAreaAvailable / totalTabularAreaNeeded;

		System.out.println("total pixel area available = " + totalPixelAreaAvailable +
				" total tabular area needed = " + totalTabularAreaNeeded + " ratio = " + multiplierForTabular);

		for (int cropIndex = 0; cropIndex < nCrops ; cropIndex++) {
			footprintsAsDoubles[cropIndex] = multiplierForTabular * rawFootprintsAsDoubles[cropIndex];
			System.out.println(cropNamesArray[cropIndex] + " = " + rawFootprintsAsDoubles[cropIndex] +
					" / " + footprintsAsDoubles[cropIndex] + " old/new ha footprint");
		}

		surplusTolerance = surplusToleranceMultiplier * totalPixelAreaAvailable;
		
		///////////////////////
		// some declarations //
		///////////////////////
		double[] currentAllocations;
		
		double[]   surplusInfo = null;
		int    maxSurplusIndex = -3276;
		double      maxSurplus = -7862.4;

		int allocationCounter = 0;
		
		SortComb11MFM revenueSortingObject = null;
		int[] sortedIndices = null;

		double highestRevenueHere = -387;
		double lowestRevenueHere  = -726;
		double differenceRevenueHere = -2367l;

		MultiFormatMatrix randomColumnMFM = null;
		
		double highestFractionHere = -267l;
		double lowestFractionHere  = -554329;
		double differenceFractionHere = -9871652;

		
		double remainingSurplus = -7; // initialize the amount remaining to be peeled off

		double thisTotalOldFractionsNonMax = -7;
		double[] originalFractions = new double[nCrops];
		double[] newFractions      = new double[nCrops];
		double bountifulCropAreaToDrop = -3;
		double probabilityComplementMultiplier = -4;
		double amountToKeep = -4;
		double fractionToKeep = -5;
		double pixelTotalFractionCheck = -6;
		
		int pixelIndexInRealTables = -2;
		boolean bailNow = false;

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
		maxSurplusIndex = (int)surplusInfo[0];
		     maxSurplus =      surplusInfo[1];

		
		System.out.println("our most overallocated crop name is " + cropNamesArray[maxSurplusIndex]);

		allocationCounter = 0;
		while (maxSurplus > surplusTolerance) {
			System.out.println("               ... top of while loop #" + allocationCounter + " maxSurplusIndex = " + maxSurplusIndex + " maxSurplus = " + maxSurplus + " > " + surplusTolerance + " surplusTolerance ...");
			littleTimer.tic();
			// now, we want to sort on the revenue for that crop...
			revenueSortingObject = new SortComb11MFM(pixelRevenueLike,maxSurplusIndex, verboseSorting);

			revenueSortingObject.doSorting();

			// pull out the row indices in order from lowest to highest
			sortedIndices = revenueSortingObject.getSortedIndexList();

			// check to see if the highest revenue and the lowest revenue are the same.
			// if so, we will need a tie breaker which will be to sort on the current
			// fractions allocated. after that, we will need to just go random...
			highestRevenueHere    = revenueSortingObject.getSortedColumnOnly().getValue(nPixels - 1,0);
			lowestRevenueHere     = revenueSortingObject.getSortedColumnOnly().getValue(0,0);
			differenceRevenueHere = highestRevenueHere - lowestRevenueHere;

			System.out.println("low rev = " + lowestRevenueHere + "; high rev = " + highestRevenueHere + "; diff = " + differenceRevenueHere);
			
			
			// the old tie-breaker was to first go to the fractions and then to random.
			// but then we get big blocks.
			// let us now try going straight to random....
			if (highestRevenueHere == lowestRevenueHere) {
				System.out.println("  === we need to activate the final tie-breaker... ===");
					randomColumnMFM = MatrixOperations.generateUniformRandom(nPixels, 1, randomSeed, 1); // Beware the MAGIC NUMBER!!! only one column needed
//					MultiFormatFloat randomColumnMFF = MatrixOperations.MFMtoMFF(randomColumnMFM);
					revenueSortingObject = new SortComb11MFM(randomColumnMFM,0,verboseSorting); // Beware the MAGIC NUMBER!!! only one column
					revenueSortingObject.doSorting();

					// pull out the row indices in order from lowest to highest
//					sortedIndices = revenueSortingObject.getSortedIndexList();

					highestRevenueHere    = revenueSortingObject.getSortedColumnOnly().getValue(nPixels - 1,0);
					lowestRevenueHere     = revenueSortingObject.getSortedColumnOnly().getValue(0,0);
					differenceRevenueHere = highestRevenueHere - lowestRevenueHere;
					System.out.println("low rand = " + lowestRevenueHere + "; high rand = " + highestRevenueHere + "; diff = " + differenceRevenueHere);

					// change the random seed so we get something else the next time we need to do this...
					randomSeed++;
			} // checking if all the revenues are the same

			
			/*
			 // old tie breaker....
			if (highestRevenueHere == lowestRevenueHere) {
				System.out.println("  --- we need to activate the tie-breaker... ---");
				revenueSortingObject = new SortComb11MFM(finalPixelFractions,maxSurplusIndex, verboseSorting);
				revenueSortingObject.doSorting();

				// and check yet again concerning the fractions...
				highestFractionHere = revenueSortingObject.getSortedColumnOnly().getValue(nPixels - 1,0);
				lowestFractionHere  = revenueSortingObject.getSortedColumnOnly().getValue(0,0);
				differenceFractionHere = highestFractionHere - lowestFractionHere;

				System.out.println("low frac = " + lowestFractionHere + "; high frac = " + highestFractionHere + "; diff = " + differenceFractionHere);

				// now we must check again to see if we need the final tie-breaker: random...
				if (highestFractionHere == lowestFractionHere) {
					System.out.println("  === we need to activate the final tie-breaker... ===");
					randomColumnMFM = MatrixOperations.generateUniformRandom(nPixels, 1, randomSeed, 1); // Beware the MAGIC NUMBER!!! only one column needed
//					MultiFormatFloat randomColumnMFF = MatrixOperations.MFMtoMFF(randomColumnMFM);
					revenueSortingObject = new SortComb11MFM(randomColumnMFM,0,verboseSorting); // Beware the MAGIC NUMBER!!! only one column
					revenueSortingObject.doSorting();
				} // checking if all the fractions are the same
			} // checking if all the revenues are the same
			 
			*/
			
			// what we want to do is start with the lowest revenue (or fraction [or random]) pixels and peel off any area there
			// and re-allocate it to the other crops

			// pull out the row indices in order from lowest to highest
			sortedIndices = revenueSortingObject.getSortedIndexList();


			// let's do this as a for loop with a break rather than as a while loop
			remainingSurplus = maxSurplus; // initialize the amount remaining to be peeled off
			bailNow = false;
			for (int inIndexArrayIndex = 0; inIndexArrayIndex < nPixels; inIndexArrayIndex++) {

				pixelIndexInRealTables = sortedIndices[inIndexArrayIndex];
				// write down what the original fractions were
				// and reset the new fractions to zero
				for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
					originalFractions[cropIndex] = finalPixelFractions.getValue(pixelIndexInRealTables,cropIndex);
					newFractions[cropIndex] = 0.0;
				}

				bountifulCropAreaToDrop = originalFractions[maxSurplusIndex] * pixelAreaAvailable.getValue(pixelIndexInRealTables,0);

				// first, let us consider whether the pixel has any area at all
				// if not, let's not even bother and just move on to the next
				// pixel
				if (bountifulCropAreaToDrop == 0.0) {
					continue;
				}

				// check to see if we can drop all of it...
				if (bountifulCropAreaToDrop < remainingSurplus) {
					// drop all of this pixel
					
					// apparently the 1.0 minus the one i want to drop is not working properly because of rounding errors...
					// probabilityComplementMultiplier = 1 / (1.0 - originalFractions[maxSurplusIndex]);

					// let us manually try it...
					thisTotalOldFractionsNonMax = 0.0;
					for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
						if (cropIndex == maxSurplusIndex) {
							continue;
						}
						thisTotalOldFractionsNonMax += originalFractions[cropIndex];
					}
					probabilityComplementMultiplier = 1.0 / thisTotalOldFractionsNonMax;
					
					// compare the total to the complement
//					System.out.println(" total = " + thisTotalOldFractionsNonMax + " ?=? + " + (1.0 - originalFractions[maxSurplusIndex]) + " complement");
					
					// update to make new fractions				// there are several ways we could do this.
					// the easiest is to scale everything up...

					// make sure this matches with the way we're doing it for the partial case...
					for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
						if (cropIndex == maxSurplusIndex) {
							continue;
						}
						newFractions[cropIndex] = originalFractions[cropIndex] * probabilityComplementMultiplier; 
					}

					remainingSurplus -= bountifulCropAreaToDrop;
					
				} else {
					// drop only the portion we need...
					amountToKeep = bountifulCropAreaToDrop - remainingSurplus;
					fractionToKeep = amountToKeep / pixelAreaAvailable.getValue(pixelIndexInRealTables,0);
					// update to make new fractions
					// there are several ways we could do this.
					// the easiest is to scale everything up...

					// make sure this matches with the way we're doing it for the whole case...
					thisTotalOldFractionsNonMax = 0.0;
					for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
						if (cropIndex == maxSurplusIndex) {
							continue;
						}
						thisTotalOldFractionsNonMax += originalFractions[cropIndex];
					}
					
					// we are spreading it around the same way, just we have less to spread around.
					// in the previous case, we had the full unity worth of fraction to divy up; this time
					// we have slightly less...
//					probabilityComplementMultiplier = (1.0 - fractionToKeep) / (1.0 - originalFractions[maxSurplusIndex]);
					probabilityComplementMultiplier = (1.0 - fractionToKeep) / thisTotalOldFractionsNonMax;
				
					newFractions[maxSurplusIndex] = fractionToKeep;
					for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
						if (cropIndex == maxSurplusIndex) {
							continue;
						}
						newFractions[cropIndex] = originalFractions[cropIndex] * probabilityComplementMultiplier; 
					}
					remainingSurplus -= (bountifulCropAreaToDrop - amountToKeep);

					// the surplus should be now zero, so let's break out...
					bailNow = true;
				}

//				System.out.println(" remaining surplus = " + remainingSurplus + " pixel #" + inIndexArrayIndex);

				// copy the new fractions over to the final fraction array
				pixelTotalFractionCheck = 0.0;
				for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
					if (newFractions[cropIndex] < 0 || newFractions[cropIndex] > 1) {
						System.out.println("bad fraction: " + newFractions[cropIndex] + " for sortedIndex = " +
								inIndexArrayIndex + " realPixelIndex = " + pixelIndexInRealTables + " cropIndex = " + cropIndex);
					}
					finalPixelFractions.setValue(pixelIndexInRealTables, cropIndex, (float)newFractions[cropIndex]);
					pixelTotalFractionCheck += newFractions[cropIndex];
				}
				
				if (Math.abs(pixelTotalFractionCheck - 1.0) > 0.00000001) {
					System.out.println("total pixel fraction is " + pixelTotalFractionCheck + " for sorted/real " + inIndexArrayIndex + "/" + pixelIndexInRealTables);
					System.out.println("thisTotalOldFractionsNonMax = " + thisTotalOldFractionsNonMax);
					System.out.println("probabilityComplementMultiplier = " + probabilityComplementMultiplier);
					for (int cropIndex = 0; cropIndex < nCrops; cropIndex++) {
						if (cropIndex == maxSurplusIndex) {
							System.out.println("[" + cropIndex + "] old = " + originalFractions[cropIndex] + " new = " + newFractions[cropIndex] + " <--- one to shrink");
						} else {
							System.out.println("[" + cropIndex + "] old = " + originalFractions[cropIndex] + " new = " + newFractions[cropIndex]);
						}
					}
				}

				// check if we should break out...
				if (bailNow) {
					break;
				}

			} // end of for loop looking in the index array

			finalPixelFractions.finishRecordingMatrix();
			
			// recheck the surpluses...
			
			currentAllocations = determineTotalAllocations(finalPixelFractions, pixelAreaAvailable);
			       surplusInfo = determineSurpluses(currentAllocations, footprintsAsDoubles);
			
			    maxSurplusIndex = (int)surplusInfo[0];
			         maxSurplus =      surplusInfo[1];
			
			System.out.println("our most overallocated crop name is " + cropNamesArray[maxSurplusIndex]);
			
//			MatrixOperations.write2DMFMtoText(finalPixelFractions, outputBasename + allocationCounter, "\t");
			
			allocationCounter++;

			
			System.out.println("this cycle finished after " + littleTimer.TOCSeconds() + " seconds");
			System.out.println();
			System.out.println();

		} // end of giant while loop over crops to equalize...
		
		// we should be all done here, so write it out...
		MatrixOperations.write2DMFMtoText(finalPixelFractions, outputBasename, "\t");
		
		
		
		
		
		
		
		
		System.out.println(bigTimer.sinceStartMessage());
			
	} // main

}

