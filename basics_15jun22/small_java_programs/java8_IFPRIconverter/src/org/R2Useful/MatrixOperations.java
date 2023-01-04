package org.R2Useful;

//import org.parallelBFGS.MultiFormatMatrix;
//import org.parallelBFGS.MultiFormatFloat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Random;



public class MatrixOperations {
	
	public static double altExp(double val) {
    final long tmp = (long) (1512775 * val + (1072693248 - 60801));
    return Double.longBitsToDouble(tmp << 32);
	}

	public static double altLn(double val) {  
		final double x = (Double.doubleToLongBits(val) >> 32);  
		return (x - 1072632447) / 1512775;  
	}

	public static MultiFormatMatrix newCopy(MultiFormatMatrix X) throws Exception {
		MultiFormatMatrix Xcopy = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		long[] coords = new long[2];
		
		for (coords[0] = 0; coords[0] < nRows; coords[0]++) {
			for (coords[1] = 0; coords[1] < nCols; coords[1]++) {
				Xcopy.setValue(coords, X.getValue(coords));
			}
		}
		
		return Xcopy;
		
	}

	public static MultiFormatFloat newCopy(MultiFormatFloat X) throws Exception {
		MultiFormatFloat Xcopy = new MultiFormatFloat(X.getDataFormat(), X.getDimensions());
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		long[] coords = new long[2];
		
		for (coords[0] = 0; coords[0] < nRows; coords[0]++) {
			for (coords[1] = 0; coords[1] < nCols; coords[1]++) {
				Xcopy.setValue(coords, X.getValue(coords));
			}
		}
		
		Xcopy.finishRecordingMatrix();
		return Xcopy;
		
	}

	public static MultiFormatMatrix newFormat(MultiFormatMatrix X, int formatIndex) throws Exception {
		MultiFormatMatrix Xcopy = new MultiFormatMatrix(formatIndex, X.getDimensions());
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		long[] coords = new long[2];
		
		for (coords[0] = 0; coords[0] < nRows; coords[0]++) {
			for (coords[1] = 0; coords[1] < nCols; coords[1]++) {
				Xcopy.setValue(coords, X.getValue(coords));
			}
		}
		
		return Xcopy;
		
	}

	public static MultiFormatMatrix normalizeColumn(MultiFormatMatrix X) throws Exception {
		if (X.getDimensions()[1] != 1) {
			System.out.println("Error: matrix must have a single column, this has " + X.getDimensions()[1]);
			throw new Exception();
		}
		
		MultiFormatMatrix normalizedColumn = null;
		double gradientLengthSquared = 0.0;
		double inverseGradientLength = 0.0;
		for (long rowIndex = 0; rowIndex < X.getDimensions()[0]; rowIndex++) {
			gradientLengthSquared += X.getValue(rowIndex,0) * X.getValue(rowIndex,0);
		}
		inverseGradientLength = 1.0 / Math.sqrt(gradientLengthSquared);

		normalizedColumn = MatrixOperations.multiplyByConstant(inverseGradientLength,X);

		return normalizedColumn;
	}
	
	public static MultiFormatMatrix transposeMatrix(
				MultiFormatMatrix X
				) throws Exception {
				
		    long nRows = X.getDimensions()[0];
		    long nCols = X.getDimensions()[1];
	
		    long[] coordOriginal = new long[2];
		    long[] coordTrans = new long[2];
		    
	//	    double[][] TransposedMatrix = new double[NumCols][NumRows];
		    MultiFormatMatrix transposedMatrix = new MultiFormatMatrix(X.getDataFormat(), new long[] {nCols, nRows});
		    for (long colIndex = 0; colIndex < nCols; colIndex++) {
		      coordOriginal[1] = colIndex;
		      coordTrans[0] = colIndex;
		      for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
		        coordOriginal[0] = rowIndex;
		        coordTrans[1] = rowIndex;
		        transposedMatrix.setValue(coordTrans, X.getValue(coordOriginal));
		      }
		    }
		    return transposedMatrix;
			}

	public static MultiFormatMatrix multiplyByConstant(
			double constantMultiplier, MultiFormatMatrix X
			)	throws Exception {
		
		MultiFormatMatrix cX = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
		
		long[] getSetCoords = new long[2];
		
		for (getSetCoords[0] = 0; getSetCoords[0] < X.getDimensions()[0]; getSetCoords[0]++) {
			for (getSetCoords[1] = 0; getSetCoords[1] < X.getDimensions()[1]; getSetCoords[1]++) {
				cX.setValue(getSetCoords,constantMultiplier * X.getValue(getSetCoords));
			}
		}
		
		return cX;
	}

	public static MultiFormatMatrix divideByConstant(
		double constantDivisor, MultiFormatMatrix X
		)	throws Exception {
	
	MultiFormatMatrix xOverC = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
	
	long[] getSetCoords = new long[2];
	
	for (getSetCoords[0] = 0; getSetCoords[0] < X.getDimensions()[0]; getSetCoords[0]++) {
		for (getSetCoords[1] = 0; getSetCoords[1] < X.getDimensions()[1]; getSetCoords[1]++) {
			xOverC.setValue(getSetCoords,X.getValue(getSetCoords) / constantDivisor);
		}
	}
	
	return xOverC;
}

	
	public static MultiFormatMatrix addMatrices(
			MultiFormatMatrix X, MultiFormatMatrix Y
			)	throws Exception {
		
		MultiFormatMatrix XpY = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
		
		long[] getSetCoords = new long[2];
		
		for (getSetCoords[0] = 0; getSetCoords[0] < X.getDimensions()[0]; getSetCoords[0]++) {
			for (getSetCoords[1] = 0; getSetCoords[1] < X.getDimensions()[1]; getSetCoords[1]++) {
				XpY.setValue(getSetCoords, X.getValue(getSetCoords) + Y.getValue(getSetCoords));
			}
		}
		
		return XpY;
	}
	
	public static MultiFormatMatrix subtractMatrices(
			MultiFormatMatrix X, MultiFormatMatrix Y
			)	throws Exception {
		
		MultiFormatMatrix XmY = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
		
		long[] getSetCoords = new long[2];
		
		for (getSetCoords[0] = 0; getSetCoords[0] < X.getDimensions()[0]; getSetCoords[0]++) {
			for (getSetCoords[1] = 0; getSetCoords[1] < X.getDimensions()[1]; getSetCoords[1]++) {
				XmY.setValue(getSetCoords, X.getValue(getSetCoords) - Y.getValue(getSetCoords));
			}
		}
		
		return XmY;
	}

	public static MultiFormatMatrix multiplyMatricesElementwise(
			MultiFormatMatrix X, MultiFormatMatrix Y
			)	throws Exception {
		
		MultiFormatMatrix XpY = new MultiFormatMatrix(X.getDataFormat(), X.getDimensions());
		
		long[] getSetCoords = new long[2];
		
		for (getSetCoords[0] = 0; getSetCoords[0] < X.getDimensions()[0]; getSetCoords[0]++) {
			for (getSetCoords[1] = 0; getSetCoords[1] < X.getDimensions()[1]; getSetCoords[1]++) {
				XpY.setValue(getSetCoords, X.getValue(getSetCoords) * Y.getValue(getSetCoords));
			}
		}
		
		return XpY;
	}

	
	public static MultiFormatMatrix multiplyMatrices(
			MultiFormatMatrix X, MultiFormatMatrix Y
			)	throws Exception {
		
		long rowSizeX = X.getDimensions()[0];
		long colSizeX = X.getDimensions()[1];
		long colSizeY = Y.getDimensions()[1];
		
		MultiFormatMatrix XtY = new MultiFormatMatrix(X.getDataFormat(), new long[] {rowSizeX, colSizeY});
				
		long[] xCoord = new long[2];
		long[] storageCoord = new long[2];
		long[] yCoord = new long[2];
		double productSum = Double.NaN;

    for (long OutputRow = 0; OutputRow < rowSizeX; OutputRow++) {
      xCoord[0] = OutputRow;
      storageCoord[0] = OutputRow;
      for (long OutputCol = 0; OutputCol < colSizeY; OutputCol++) {
        yCoord[1] = OutputCol;
        storageCoord[1] = OutputCol;
        productSum = 0.0;
        for (long ConformableIndex = 0; ConformableIndex < colSizeX; ConformableIndex++) {
          xCoord[1] = ConformableIndex;
          yCoord[0] = ConformableIndex;
          productSum += X.getValue(OutputRow, ConformableIndex) * Y.getValue(ConformableIndex, OutputCol);
        }
        XtY.setValue(storageCoord, productSum);
//        ResultMatrix.setValue(OutputRow, OutputCol, ProductSum);
      }
    }
		
		
		
		return XtY;
	}

	public static long[] compareMatrices(MultiFormatMatrix X, MultiFormatMatrix Y, double threshold) throws Exception {
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		long[] firstDifferingElement = null;
		
		if (Y.getDimensions()[0] != nRows) {
			System.out.println("compareMatrices: nRows not consistent (" + nRows + ", " + Y.getDimensions()[0]);
			firstDifferingElement = new long[] {-1,-1};
			return firstDifferingElement;
		}

		if (Y.getDimensions()[1] != nCols) {
			System.out.println("compareMatrices: nCols not consistent (" + nCols + ", " + Y.getDimensions()[1]);
			firstDifferingElement = new long[] {-2,-2};
			return firstDifferingElement;
		}
		
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			for (long colIndex = 0; colIndex < nCols; colIndex++) {
				if (Math.abs(X.getValue(rowIndex,colIndex) - Y.getValue(rowIndex,colIndex)) > threshold) {
					firstDifferingElement = new long[] {rowIndex,colIndex};
					break;
				}
			}
		}

		
		return firstDifferingElement;
		
	}
	public static long[] compareMatrices(MultiFormatMatrix X, MultiFormatMatrix Y) throws Exception {
		
		return compareMatrices(X,Y,0.0);
	}

	public static MultiFormatMatrix sumToCol(MultiFormatMatrix X) throws Exception {
		MultiFormatMatrix colCollapse = new MultiFormatMatrix(X.getDataFormat(), new long[] {X.getDimensions()[0], 1});
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			for (long colIndex = 0; colIndex < nCols; colIndex++) {
				colCollapse.setValue(rowIndex,0, 
						colCollapse.getValue(rowIndex,0) + X.getValue(rowIndex,colIndex));
			}
		}
		
		return colCollapse;
	}

	public static double sumToScalar(MultiFormatMatrix X) throws Exception {
	    double overallTotal = 0.0;
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			for (long colIndex = 0; colIndex < nCols; colIndex++) {
			    overallTotal += X.getValue(rowIndex,colIndex);
			}
		}
		
		return overallTotal;
	}

	
	public static MultiFormatMatrix sumToRow(MultiFormatMatrix X) throws Exception {
		MultiFormatMatrix rowCollapse = new MultiFormatMatrix(X.getDataFormat(), new long[] {1 , X.getDimensions()[1]});
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			for (long colIndex = 0; colIndex < nCols; colIndex++) {
				rowCollapse.setValue(0,colIndex, 
						rowCollapse.getValue(0,colIndex) + X.getValue(rowIndex,colIndex));
			}
		}
		
		return rowCollapse;
	}
	
	public static MultiFormatMatrix identityMFM(long nOnDiagonal, int formatIndex) throws Exception {
		MultiFormatMatrix eye = new MultiFormatMatrix(formatIndex, new long[] {nOnDiagonal, nOnDiagonal});
		
		for (long[] diagCoords = new long[] {0,0} ; diagCoords[0] < nOnDiagonal ; diagCoords[0]++, diagCoords[1]++) {
			eye.setValue(diagCoords, 1.0);
		}
		
		return eye;
	}

	public static MultiFormatMatrix getSubmatrix(
			MultiFormatMatrix X,
			long RowStartIndex, long RowEndIndex,
			long ColStartIndex, long ColEndIndex,
			int formatIndex
	) throws Exception {

		//		pull out the last available indices
		long LastRowIndexX = X.getDimensions()[0] - 1;
		long LastColIndexX = X.getDimensions()[1] - 1;
		//		preprocessing on the "-1"s...
		if (RowEndIndex == -1) {
			RowEndIndex = LastRowIndexX;
		}
		if (ColEndIndex == -1) {
			ColEndIndex = LastColIndexX;
		}

		//		idiot proofing
		if ((RowStartIndex > RowEndIndex)) {
			System.out.println("(RowStartIndex [" + RowStartIndex + "] > RowEndIndex [" + RowEndIndex + "])");
			throw new Exception();
		}
		if (ColStartIndex > ColEndIndex) {
			System.out.println("(ColStartIndex > ColEndIndex)");
			throw new Exception();
		}
		if (RowEndIndex > LastRowIndexX) {
			System.out.println("(RowEndIndex > LastRowIndexX)");
			throw new Exception();
		}
		if (ColEndIndex > LastColIndexX) {
			System.out.println("(ColEndIndex > LastColIndexX)");
			throw new Exception();
		}
		if (RowStartIndex < 0 || RowEndIndex < 0 ||
				ColStartIndex < 0 || ColEndIndex < 0) {
			System.out.println("Bad indices: less than -1 ...");
			throw new Exception();
		}

		//		prepare and calculate the size of the outgoing matrix
		long NumRows = RowEndIndex - RowStartIndex + 1;
		long NumCols = ColEndIndex - ColStartIndex + 1;

		MultiFormatMatrix SubMatrix = new MultiFormatMatrix(formatIndex, new long[] {NumRows, NumCols});
		for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				SubMatrix.setValue(RowIndex, ColIndex, X.getValue(RowIndex + RowStartIndex, ColIndex + ColStartIndex));
			}
		}

		return SubMatrix;
	}

	public static MultiFormatFloat getSubmatrix(
			MultiFormatFloat X,
			long RowStartIndex, long RowEndIndex,
			long ColStartIndex, long ColEndIndex,
			int formatIndex
	) throws Exception {

		//		pull out the last available indices
		long LastRowIndexX = X.getDimensions()[0] - 1;
		long LastColIndexX = X.getDimensions()[1] - 1;
		//		preprocessing on the "-1"s...
		if (RowEndIndex == -1) {
			RowEndIndex = LastRowIndexX;
		}
		if (ColEndIndex == -1) {
			ColEndIndex = LastColIndexX;
		}

		//		idiot proofing
		if ((RowStartIndex > RowEndIndex)) {
			System.out.println("(RowStartIndex [" + RowStartIndex + "] > RowEndIndex [" + RowEndIndex + "])");
			throw new Exception();
		}
		if (ColStartIndex > ColEndIndex) {
			System.out.println("(ColStartIndex > ColEndIndex)");
			throw new Exception();
		}
		if (RowEndIndex > LastRowIndexX) {
			System.out.println("(RowEndIndex > LastRowIndexX)");
			throw new Exception();
		}
		if (ColEndIndex > LastColIndexX) {
			System.out.println("(ColEndIndex > LastColIndexX)");
			throw new Exception();
		}
		if (RowStartIndex < 0 || RowEndIndex < 0 ||
				ColStartIndex < 0 || ColEndIndex < 0) {
			System.out.println("Bad indices: less than -1 ...");
			throw new Exception();
		}

		//		prepare and calculate the size of the outgoing matrix
		long NumRows = RowEndIndex - RowStartIndex + 1;
		long NumCols = ColEndIndex - ColStartIndex + 1;

		MultiFormatFloat SubMatrix = new MultiFormatFloat(formatIndex, new long[] {NumRows, NumCols});
		for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				SubMatrix.setValue(RowIndex, ColIndex, X.getValue(RowIndex + RowStartIndex, ColIndex + ColStartIndex));
			}
		}

		return SubMatrix;
	}

	public static MultiFormatMatrix generateUniformRandom(long nRows, long nCols, long randomSeed, int formatIndex) throws Exception {
		MultiFormatMatrix randomMatrix = new MultiFormatMatrix(formatIndex, new long[] {nRows, nCols});
		
	// prepare the random number generator
    Random randomNumberGenerator = new Random(randomSeed);


    for (long i = 0; i < nRows; i++) {
      for (long j = 0; j < nCols; j++) {
        randomMatrix.setValue(i, j, randomNumberGenerator.nextDouble());
      }
    }
		randomMatrix.finishRecordingMatrix();
		return randomMatrix;
	}

	public static MultiFormatMatrix generateConstant(long nRows, long nCols, double constantValue, int formatIndex) throws Exception {
		MultiFormatMatrix constantMatrix = new MultiFormatMatrix(formatIndex, new long[] {nRows, nCols});
		
		// skip the assignment if it is zero...

		if (constantValue != 0.0) {
			for (long i = 0; i < nRows; i++) {
				for (long j = 0; j < nCols; j++) {
					constantMatrix.setValue(i, j, constantValue);
				}
			}
		}
		constantMatrix.finishRecordingMatrix();
		return constantMatrix;
	}

	
	public static MultiFormatMatrix MFFtoMFM(MultiFormatFloat X) throws Exception {
		
    int dataFormat = X.getDataFormat();

    MultiFormatMatrix resultMatrix = new MultiFormatMatrix(dataFormat, X.getDimensions());

    int     nDimsX        = X.getNumDimensions();
    long[]  theXDims      = X.getDimensions();
    long[]  currentIndex  = new long[X.getNumDimensions()];
    boolean carryOn       = true;

    while(carryOn) {
      
      resultMatrix.setValue(currentIndex,X.getValue(currentIndex));
      
      // determine the last dimension that can be bumped up...
      // all the "-1"s are because we are dealing in indices...
      for (int dimIndex = nDimsX - 1; dimIndex >= 0; dimIndex--) {
        if (currentIndex[dimIndex] < theXDims[dimIndex] - 1) {
          currentIndex[dimIndex]++;
          for (int prevDimIndex = nDimsX - 1; prevDimIndex > dimIndex; prevDimIndex--) {
            currentIndex[prevDimIndex] = 0;
          }
          carryOn = true;
          break;
        }
          carryOn = false;
      } // end for
    } // end while

    resultMatrix.finishRecordingMatrix();
		return resultMatrix;
	}

	public static MultiFormatFloat MFMtoMFF(MultiFormatMatrix X) throws Exception {
		
    int dataFormat = X.getDataFormat();

    MultiFormatFloat resultMatrix = new MultiFormatFloat(dataFormat, X.getDimensions());

    int     nDimsX        = X.getNumDimensions();
    long[]  theXDims      = X.getDimensions();
    long[]  currentIndex  = new long[X.getNumDimensions()];
    boolean carryOn       = true;

    while(carryOn) {
      
      resultMatrix.setValue(currentIndex,(float)X.getValue(currentIndex));
      
      // determine the last dimension that can be bumped up...
      // all the "-1"s are because we are dealing in indices...
      for (int dimIndex = nDimsX - 1; dimIndex >= 0; dimIndex--) {
        if (currentIndex[dimIndex] < theXDims[dimIndex] - 1) {
          currentIndex[dimIndex]++;
          for (int prevDimIndex = nDimsX - 1; prevDimIndex > dimIndex; prevDimIndex--) {
            currentIndex[prevDimIndex] = 0;
          }
          carryOn = true;
          break;
        }
          carryOn = false;
      } // end for
    } // end while

    resultMatrix.finishRecordingMatrix();
		return resultMatrix;
	}

	public static MultiFormatFloat[] randomizeRows2dMffSlow(MultiFormatFloat originalMFF, long randomSeed, double fractionInFirst, int formatCode) throws Exception {
		
		long nRows = originalMFF.getDimensions()[0];
		long nCols = originalMFF.getDimensions()[1];
		
		long nRowsInFirst = (long) (fractionInFirst * nRows);
		long nRowsInSecond = nRows - nRowsInFirst;
		
    Random randomNumberGenerator = new Random(randomSeed);
    
    boolean[] rowUsedFlags = new boolean[(int)nRows];
    
    double theRandomNumber = -1;
//    int rowToPull = -2;
    
    MultiFormatFloat[] matrixPair = new MultiFormatFloat[2];
    
    matrixPair[0] = new MultiFormatFloat(formatCode, new long[] {nRowsInFirst,  originalMFF.getDimensions()[1]});
    matrixPair[1] = new MultiFormatFloat(formatCode, new long[] {nRowsInSecond, originalMFF.getDimensions()[1]});
    
    // pull out as many random numbers as there are rows
    // look up which row corresponds to that number
    // try to pull that row. if it is already taken, go to the next one. if we hit the end, wrap to the beginning
    
    long[] getTopIndices = new long[2];
    long[] setTopIndices = new long[2];
    
    for (int pullIndex = 0; pullIndex < nRowsInFirst; pullIndex++) {
      theRandomNumber = randomNumberGenerator.nextDouble();
//    	rowToPull = (int) (theRandomNumber * nRows);
    	getTopIndices[0] = (int) (theRandomNumber * nRows);
//    	System.out.println("initial gTI[0] = " + getTopIndices[0]);
    	
    	// find next good row to use
    	while (rowUsedFlags[(int)getTopIndices[0]]) {
    		getTopIndices[0]++;
    		if (getTopIndices[0] >= nRows) {
    			getTopIndices[0] = 0;
    		}
    	}
//    	System.out.println("final gTI[0] = " + getTopIndices[0]);
    	// copy them over
//    	System.out.println("-- pullIndex = " + pullIndex + " ; rowToPull = " + getTopIndices[0]);
    	setTopIndices[0] = pullIndex;

    	for (setTopIndices[1] = 0; setTopIndices[1] < nCols ; setTopIndices[1]++) {
    		getTopIndices[1] = setTopIndices[1];
    		matrixPair[0].setValue(setTopIndices, 
    				originalMFF.getValue(getTopIndices));
    	}
    	rowUsedFlags[(int)getTopIndices[0]] = true;
    }

    long[] getBottomIndices = new long[2];
    long[] setBottomIndices = new long[2];

    // reset the row to pull to just step through the remaining ones
//    rowToPull = 0;
    getBottomIndices[0] = 0;
    for (int pullIndex = 0; pullIndex < nRowsInSecond; pullIndex++) {
    	
//    	System.out.println("initial gBI[0] = " + getBottomIndices[0]);
    	// find next good row to use
    	while (rowUsedFlags[(int)getBottomIndices[0]]) {
    		getBottomIndices[0]++;
//    		if (getBottomIndices[0] >= nRows) {
//    			getBottomIndices[0] = 0;
//    		}
    	}
//    	System.out.println("final gBI[0] = " + getBottomIndices[0]);

    	// copy them over
//    	System.out.println("-- pullIndex = " + pullIndex + " ; rowToPull = " + rowToPull);
    	setBottomIndices[0] = pullIndex;
    	for (setBottomIndices[1] = 0; setBottomIndices[1] < nCols ; setBottomIndices[1]++) {
    		getBottomIndices[1] = setBottomIndices[1];
    		matrixPair[1].setValue(setBottomIndices, 
    				originalMFF.getValue(getBottomIndices));
    	}
//    	rowUsedFlags[rowToPull] = true;
    	getBottomIndices[0]++; // bump it up rather than set the flag to check...
    }
    

		matrixPair[0].finishRecordingMatrix();
		matrixPair[1].finishRecordingMatrix();
		
		return matrixPair;
	}

	public static MultiFormatFloat[] randomizeRows2dMff(MultiFormatFloat originalMFF, long randomSeed, double fractionInFirst) throws Exception {
	
		return randomizeRows2dMff(originalMFF, randomSeed,fractionInFirst,originalMFF.getDataFormat());
	}

	public static MultiFormatFloat[] randomizeRows2dMff(MultiFormatFloat originalMFF, long randomSeed, double fractionInFirst, int formatCode) throws Exception {
		
		long nRows = originalMFF.getDimensions()[0];
		long nCols = originalMFF.getDimensions()[1];
		
		long nRowsInFirst = (long) (fractionInFirst * nRows);
		long nRowsInSecond = nRows - nRowsInFirst;
		
    Random randomNumberGenerator = new Random(randomSeed);
    
    boolean[] rowUsedFlags = new boolean[(int)nRows];
    
    double theRandomNumber = -1;
    int rowToPull = -2;
    
    MultiFormatFloat[] matrixPair = new MultiFormatFloat[2];
    
    matrixPair[0] = new MultiFormatFloat(formatCode, new long[] {nRowsInFirst,  originalMFF.getDimensions()[1]});
    matrixPair[1] = new MultiFormatFloat(formatCode, new long[] {nRowsInSecond, originalMFF.getDimensions()[1]});
    
    // pull out as many random numbers as there are rows
    // look up which row corresponds to that number
    // try to pull that row. if it is already taken, go to the next one. if we hit the end, wrap to the beginning
    
//    long[] getTopIndices = new long[2];
//    long[] setTopIndices = new long[2];
//    long[] getBottomIndices = new long[2];
//    long[] setBottomIndices = new long[2];
    
    for (int pullIndex = 0; pullIndex < nRowsInFirst; pullIndex++) {
      theRandomNumber = randomNumberGenerator.nextDouble();
    	rowToPull = (int) (theRandomNumber * nRows);
    	// find next good row to use
    	while (rowUsedFlags[rowToPull]) {
    		rowToPull++;
    		if (rowToPull >= nRows) {
    			rowToPull = 0;
    		}
    	}
    	
    	rowUsedFlags[rowToPull] = true;
    }

    long topStorageRow    = 0; // this actually needs to be zero
    long bottomStorageRow = 0; // this actually needs to be zero

    int nTrue = 0;
    for (int originalRowIndex = 0; originalRowIndex < nRows ; originalRowIndex++) {
    	if (rowUsedFlags[originalRowIndex]) {
    		nTrue++;
    	}
    }

		System.out.println("nFirst = " + nRowsInFirst + " nSecond = " + nRowsInSecond);

    
    for (long originalRowIndex = 0; originalRowIndex < nRows ; originalRowIndex++) {
    	if (rowUsedFlags[(int)originalRowIndex]) {
    		// put in top
//      	setTopIndices[0] = topStorageRow;
//      	getTopIndices[0] = originalRowIndex;
      	
      	for (long colIndex = 0; colIndex < nCols ; colIndex++) {
//      		getTopIndices[1] = colIndex;
//      		setTopIndices[1] = colIndex;
      		
//      		matrixPair[0].setValue(setTopIndices, 
//      				originalMFF.getValue(getTopIndices)
//      				);
      		matrixPair[0].setValue(topStorageRow,colIndex, 
      				originalMFF.getValue(originalRowIndex,colIndex)
      				);
      	}
      	
//      	System.out.println("[T:" + originalRowIndex + "/" + nRows + " ; " + topStorageRow + "/" + nRowsInFirst + "]");
      	topStorageRow++;
    		
    	} else {
    		// put in bottom
    		// put in top
//      	setBottomIndices[0] = bottomStorageRow;
//      	getBottomIndices[0] = originalRowIndex;
      	
      	for (long colIndex = 0; colIndex < nCols ; colIndex++) {
//      		getBottomIndices[1] = colIndex;
//      		setBottomIndices[1] = colIndex;

      		matrixPair[1].setValue(bottomStorageRow,colIndex,
      				originalMFF.getValue(originalRowIndex,colIndex)
      				);

//      		try {
//        		matrixPair[1].setValue(bottomStorageRow,colIndex,
//        				originalMFF.getValue(originalRowIndex,colIndex)
//        				);
//      		} catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
//      			System.out.println("bottom; original row = " + originalRowIndex + "; col = " + colIndex + "; storageIndex = " 
//      					+ bottomStorageRow + "; nBottomRows = " + nRowsInSecond);
//      			System.out.println("actual nRows = " + matrixPair[1].getDimensions()[0] + "; actual nCols = " + matrixPair[1].getDimensions()[1]);
//      			System.out.println("nTrue = " + nTrue);
//      			throw new Exception();
//      		}
      	}
      	
//      	System.out.println("[B:" + originalRowIndex + "/" + nRows + " ; " + bottomStorageRow + "/" + nRowsInSecond + "]");
      	bottomStorageRow++;
    	}
    }
    
		matrixPair[0].finishRecordingMatrix();
		matrixPair[1].finishRecordingMatrix();
		
		return matrixPair;
	}
	

	public static MultiFormatFloat[] splitOffLastColumn(MultiFormatFloat X) throws Exception {
	
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		long lastCol = nCols - 1;
	
		int FormatIndexLast = X.getDataFormat(); // initialize
		int FormatIndexBody = X.getDataFormat(); // initialize
	
		MultiFormatFloat Body = new MultiFormatFloat(FormatIndexBody,
				new long[] { nRows, nCols - 1 });
		MultiFormatFloat LastColumn = new MultiFormatFloat(FormatIndexLast,
				new long[] { nRows, 1 });
	
	  long[] bodyCoord = new long[2];
	  long[] lastGet = new long[2];
	  long[] lastSet = new long[2];
	                 
	  lastGet[1] = lastCol;
	  lastSet[1] = 0;
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
	    bodyCoord[0] = rowIndex;
	    lastGet[0] = rowIndex;
	    lastSet[0] = rowIndex;
			for (long colIndex = 0; colIndex < nCols - 1; colIndex++) {
	      bodyCoord[1] = colIndex;
	      Body.setValue(bodyCoord, X
						.getValue(bodyCoord));
			}
			LastColumn.setValue(lastSet, X.getValue(lastGet));
		}
	
		return new MultiFormatFloat[] {Body, LastColumn};
	
	}

	public static MultiFormatFloat[] splitOffLastTwoColumns(MultiFormatFloat X) throws Exception {
		
		long nRows = X.getDimensions()[0];
		long nCols = X.getDimensions()[1];
		long lastCol = nCols - 1;
	
		int FormatIndexLast = X.getDataFormat(); // initialize
		int FormatIndexBody = X.getDataFormat(); // initialize
	
		MultiFormatFloat Body = new MultiFormatFloat(FormatIndexBody,
				new long[] { nRows, nCols - 2 });
		MultiFormatFloat LastTwoColumns = new MultiFormatFloat(FormatIndexLast,
				new long[] { nRows, 2 });
		
//		System.out.println("     splitting last two (nCols = " + nCols + ", nRows = " + nRows + "): Body cols = " + Body.getDimensions()[1] + "; LastTwoColumns cols = " + LastTwoColumns.getDimensions()[1]);
	
	  long[] bodyCoord = new long[2];
	  long[] lastGetA = new long[2];
	  long[] lastSetA = new long[2];
	  long[] lastGetB = new long[2];
	  long[] lastSetB = new long[2];
	                 
	  lastGetA[1] = lastCol - 1;
	  lastSetA[1] = 0;
	  lastGetB[1] = lastCol;
	  lastSetB[1] = 1;
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
	    bodyCoord[0] = rowIndex;
	    lastGetA[0] = rowIndex;
	    lastSetA[0] = rowIndex;
	    lastGetB[0] = rowIndex;
	    lastSetB[0] = rowIndex;
			for (long colIndex = 0; colIndex < nCols - 2; colIndex++) {
	      bodyCoord[1] = colIndex;
	      Body.setValue(bodyCoord, X
						.getValue(bodyCoord));
			}
			LastTwoColumns.setValue(lastSetA, X.getValue(lastGetA));
			LastTwoColumns.setValue(lastSetB, X.getValue(lastGetB));
		}
	
		return new MultiFormatFloat[] {Body, LastTwoColumns};
	
	}

	
	public static Object[] readInfoFile(String basefilename) throws Exception {

		String InfoFileName = basefilename + ".info.txt";

		// create the "File"
		File InfoFileObject = new File(InfoFileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader InfoStream = new FileReader(InfoFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader InfoReader = new BufferedReader(InfoStream);
		// read the first line
		String LineContents = InfoReader.readLine();
		// Beware the MAGIC NUMBER!!! i am only going to read, at most, a pile
		// of characters on each line
//		int MaxNumCharacters = 50;
		int indexOfEnd = 0;

		indexOfEnd = LineContents.indexOf("=") - 2;
		long nRows = Long.parseLong(LineContents.substring(0, indexOfEnd));

		// read the second line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		long nCols = Long.parseLong(LineContents
				.substring(0, indexOfEnd));

		// read the third line (who cares?)
		LineContents = InfoReader.readLine();
		// read the fourth line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		int FormatIndex = Integer.parseInt(LineContents.substring(0,
				indexOfEnd));

//		// read the fifth line
		LineContents = InfoReader.readLine();
		String delimiterString = LineContents.substring(0, indexOfEnd);

		// close up shop on that part...
		InfoReader.close();
		InfoStream.close();

		Object[] returnArray = new Object[] {nRows, nCols, FormatIndex, delimiterString};
		
		return returnArray;
	}
	
	public static MultiFormatFloat read2DMFFfromText(String basefilename) throws Exception {
		

		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		// create the "File"
		File InfoFileObject = new File(InfoFileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader InfoStream = new FileReader(InfoFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader InfoReader = new BufferedReader(InfoStream);
		// read the first line
		String LineContents = InfoReader.readLine();
		// Beware the MAGIC NUMBER!!! i am only going to read, at most, a pile
		// of characters on each line
//		int MaxNumCharacters = 50;
		int indexOfEnd = 0;

		indexOfEnd = LineContents.indexOf("=") - 2;
		long nRows = Long.parseLong(LineContents.substring(0, indexOfEnd));

		// read the second line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		long nCols = Long.parseLong(LineContents
				.substring(0, indexOfEnd));

		// read the third line (who cares?)
		LineContents = InfoReader.readLine();
		// read the fourth line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		int FormatIndex = Integer.parseInt(LineContents.substring(0,
				indexOfEnd));

		// read the fifth line
		LineContents = InfoReader.readLine();
		String delimiterString = LineContents.substring(0, indexOfEnd);

		// close up shop on that part...
		InfoReader.close();
		InfoStream.close();

		MultiFormatFloat X = new MultiFormatFloat(FormatIndex, new long[] {
				nRows, nCols });

		// create the "File"
		File DataFileObject = new File(FileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader DataStream = new FileReader(DataFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader DataReader = new BufferedReader(DataStream);

		// actually try to read in the goodies and crank it out and write it
		// down...

//		boolean stayOnThisLineFlag = true;
		int previousEndIndex = 0;
		int currentEndIndex = 0;
		long currentColumn = 0;
		float valueToStore = 0.0F;
		String tempString = new String();

		for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
			// read it in...
			LineContents = DataReader.readLine();
			// reset everything going in...
			currentColumn = 0;
			previousEndIndex = -1;
			currentEndIndex = 0;

			while (currentColumn < nCols) {
				try {
					currentEndIndex = LineContents.indexOf(delimiterString,
							previousEndIndex + 1);
				} catch (java.lang.NullPointerException npe) {
					System.out.println("Possibly ran past the end of file (this row = index #" + rowIndex + ")...");
					System.out.println("LineContents = [" + LineContents +"]");
					npe.printStackTrace();
				}
				if ((currentEndIndex != previousEndIndex)
						&& (currentEndIndex != -1)) {
					// we are in the interior of the line...
					tempString = LineContents.substring(previousEndIndex + 1,
							currentEndIndex);
				} else {
					tempString = LineContents.substring(previousEndIndex + 1);
				}
//				System.out.println("tempString = [" + tempString + "]");
				try {
        valueToStore = Float.parseFloat(tempString);
				} catch (java.lang.NumberFormatException eee) {
					System.out.println("row: " + rowIndex + "; col: " + currentColumn + "; string = [" + tempString + "]");
					eee.printStackTrace();
				}
//				valueToStore = (float)Double.parseDouble(tempString);

				X.setValue(rowIndex, currentColumn, valueToStore);
				previousEndIndex = currentEndIndex;
				currentColumn++;

			}

		}

		DataReader.close();
		DataStream.close();

		
		return X;	
		
	}
	
	public static void write2DMFFtoText(MultiFormatFloat X, String basefilename, String delimiter) throws Exception {
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();

			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);

			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();

			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);

			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print(X.getValue(RowIndex,ColIndex) + delimiter);
				}
				else {
					outWriterObject.print(X.getValue(RowIndex,ColIndex) + "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();

	}

	public static void write2DMFFtoTextAsInt(MultiFormatFloat X, String basefilename, String delimiter) throws Exception {
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();

			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);

			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();

			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);

			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print((int)X.getValue(RowIndex,ColIndex) + delimiter);
				}
				else {
					outWriterObject.print((int)X.getValue(RowIndex,ColIndex) + "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();

	}

	
	public static MultiFormatMatrix read2DMFMfromTextForceFormat(String basefilename, int formatIndex) throws Exception {
		

		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		// create the "File"
		File InfoFileObject = new File(InfoFileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader InfoStream = new FileReader(InfoFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader InfoReader = new BufferedReader(InfoStream);
		// read the first line
		String LineContents = InfoReader.readLine();
		// Beware the MAGIC NUMBER!!! i am only going to read, at most, a pile
		// of characters on each line
//		int MaxNumCharacters = 50;
		int indexOfEnd = 0;

		indexOfEnd = LineContents.indexOf("=") - 2;
		long nRows = Long.parseLong(LineContents.substring(0, indexOfEnd));

		// read the second line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		long nCols = Long.parseLong(LineContents
				.substring(0, indexOfEnd));

		// read the third line (who cares?)
		LineContents = InfoReader.readLine();
		// read the fourth line
		LineContents = InfoReader.readLine();
		// forcing a desired format...
//		int FormatIndex = Integer.parseInt(LineContents.substring(0,
//				indexOfEnd));

		// read the fifth line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		// String delimiterString = LineContents.substring(0, indexOfEnd);
		String delimiterString = "\t";
		// System.out.println("delimiterString");
		// System.out.println(delimiterString);
		
		// close up shop on that part...
		InfoReader.close();
		InfoStream.close();

		MultiFormatMatrix X = new MultiFormatMatrix(formatIndex, new long[] {
				nRows, nCols });

		// create the "File"
		File DataFileObject = new File(FileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader DataStream = new FileReader(DataFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader DataReader = new BufferedReader(DataStream);

		// actually try to read in the goodies and crank it out and write it
		// down...

//		boolean stayOnThisLineFlag = true;
		int previousEndIndex = 0;
		int currentEndIndex = 0;
		long currentColumn = 0;
		double valueToStore = 0.0;
		String tempString = new String();


		long[] setCoords = new long[2];
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			setCoords[0] = rowIndex;
			// read it in...
			LineContents = DataReader.readLine();
			// System.out.println("LineContents = " + LineContents);
			// reset everything going in...
			currentColumn = 0;
			previousEndIndex = -1;
			currentEndIndex = 0;

			while (currentColumn < nCols) {
				// System.out.println("row"+rowIndex + " column " + currentColumn);
				setCoords[1] = currentColumn;
				currentEndIndex = LineContents.indexOf(delimiterString,
						previousEndIndex + 1);
				if ((currentEndIndex != previousEndIndex)
						&& (currentEndIndex != -1)) {
					// we are in the interior of the line...
					// System.out.println("previousEndIndex");
					// System.out.println(previousEndIndex);
					tempString = LineContents.substring(previousEndIndex + 1,
							currentEndIndex);
//					valueToStore = Double.parseDouble(LineContents.substring(previousEndIndex + 1,
//							currentEndIndex));
				} else {
//					valueToStore = Double.parseDouble(LineContents.substring(previousEndIndex + 1));
					// System.out.println("previousEndIndex");
					// System.out.println(previousEndIndex);
				tempString = LineContents.substring(previousEndIndex + 1);
				}
				
				// System.out.println("tempString"+tempString);
				
				valueToStore = Double.parseDouble(tempString);

//				X.setValue(rowIndex, currentColumn, valueToStore);
				X.setValue(setCoords, valueToStore);

				previousEndIndex = currentEndIndex;
				currentColumn++;

			}

		}

		DataReader.close();
		DataStream.close();

		
		return X;
		
	}
	
	public static MultiFormatMatrix read2DMFMfromText(String basefilename) throws Exception {
		

		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		// create the "File"
		File InfoFileObject = new File(InfoFileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader InfoStream = new FileReader(InfoFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader InfoReader = new BufferedReader(InfoStream);
		// read the first line
		String LineContents = InfoReader.readLine();
		// Beware the MAGIC NUMBER!!! i am only going to read, at most, a pile
		// of characters on each line
//		int MaxNumCharacters = 50;
		int indexOfEnd = 0;

		indexOfEnd = LineContents.indexOf("=") - 2;
		long nRows = Long.parseLong(LineContents.substring(0, indexOfEnd));

		// read the second line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		long nCols = Long.parseLong(LineContents
				.substring(0, indexOfEnd));

		// read the third line (who cares?)
		LineContents = InfoReader.readLine();
		// read the fourth line
		LineContents = InfoReader.readLine();
		// forcing a desired format...
		indexOfEnd = LineContents.indexOf("=") - 2;
		int FormatIndex = Integer.parseInt(LineContents.substring(0,
				indexOfEnd));

		// read the fifth line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		String delimiterString = LineContents.substring(0, indexOfEnd);

		
		// close up shop on that part...
		InfoReader.close();
		InfoStream.close();

		MultiFormatMatrix X = new MultiFormatMatrix(FormatIndex, new long[] {
				nRows, nCols });

		// create the "File"
		File DataFileObject = new File(FileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader DataStream = new FileReader(DataFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader DataReader = new BufferedReader(DataStream);

		// actually try to read in the goodies and crank it out and write it
		// down...

//		boolean stayOnThisLineFlag = true;
		int previousEndIndex = 0;
		int currentEndIndex = 0;
		long currentColumn = 0;
		double valueToStore = 0.0;
		String tempString = new String();


		long[] setCoords = new long[2];
		for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
			setCoords[0] = rowIndex;
			// read it in...
			LineContents = DataReader.readLine();
			// reset everything going in...
			currentColumn = 0;
			previousEndIndex = -1;
			currentEndIndex = 0;

			while (currentColumn < nCols) {
				setCoords[1] = currentColumn;
				currentEndIndex = LineContents.indexOf(delimiterString,
						previousEndIndex + 1);
				if ((currentEndIndex != previousEndIndex)
						&& (currentEndIndex != -1)) {
					// we are in the interior of the line...
					tempString = LineContents.substring(previousEndIndex + 1,
							currentEndIndex);
//					valueToStore = Double.parseDouble(LineContents.substring(previousEndIndex + 1,
//							currentEndIndex));
				} else {
//					valueToStore = Double.parseDouble(LineContents.substring(previousEndIndex + 1));
				tempString = LineContents.substring(previousEndIndex + 1);
				}
				valueToStore = Double.parseDouble(tempString);

//				X.setValue(rowIndex, currentColumn, valueToStore);
				X.setValue(setCoords, valueToStore);

				previousEndIndex = currentEndIndex;
				currentColumn++;

			}

		}

		DataReader.close();
		DataStream.close();

		
		return X;
		
	}

	public static MultiFormatMatrix read2DMFMfromTextOld(String basefilename) throws Exception {
		

		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		// create the "File"
		File InfoFileObject = new File(InfoFileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader InfoStream = new FileReader(InfoFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader InfoReader = new BufferedReader(InfoStream);
		// read the first line
		String LineContents = InfoReader.readLine();
		// Beware the MAGIC NUMBER!!! i am only going to read, at most, a pile
		// of characters on each line
//		int MaxNumCharacters = 50;
		int indexOfEnd = 0;

		indexOfEnd = LineContents.indexOf("=") - 2;
		long nRows = Long.parseLong(LineContents.substring(0, indexOfEnd));

		// read the second line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		long nCols = Long.parseLong(LineContents
				.substring(0, indexOfEnd));

		// read the third line (who cares?)
		LineContents = InfoReader.readLine();
		// read the fourth line
		LineContents = InfoReader.readLine();
		indexOfEnd = LineContents.indexOf("=") - 2;
		int FormatIndex = Integer.parseInt(LineContents.substring(0,
				indexOfEnd));

		// read the fifth line
		LineContents = InfoReader.readLine();
		String delimiterString = LineContents.substring(0, indexOfEnd);

		// close up shop on that part...
		InfoReader.close();
		InfoStream.close();

		MultiFormatMatrix X = new MultiFormatMatrix(FormatIndex, new long[] {
				nRows, nCols });

		// create the "File"
		File DataFileObject = new File(FileName);
		// create a character input stream (FileReader inherits from
		// InputStreamReader
		FileReader DataStream = new FileReader(DataFileObject);
		// filter the input stream, buffers characters for efficiency
		BufferedReader DataReader = new BufferedReader(DataStream);

		// actually try to read in the goodies and crank it out and write it
		// down...

//		boolean stayOnThisLineFlag = true;
		int previousEndIndex = 0;
		int currentEndIndex = 0;
		long currentColumn = 0;
		double valueToStore = 0.0;
		String tempString = new String();

		for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
			// read it in...
			LineContents = DataReader.readLine();
			// reset everything going in...
			currentColumn = 0;
			previousEndIndex = -1;
			currentEndIndex = 0;

			while (currentColumn < nCols) {
				try {
					currentEndIndex = LineContents.indexOf(delimiterString,
							previousEndIndex + 1);
				} catch (java.lang.NullPointerException npe) {
					System.out.println("Possibly ran past the end of file (this row = index #" + rowIndex + ")...");
					System.out.println("LineContents = [" + LineContents +"]");
					npe.printStackTrace();
				}
				if ((currentEndIndex != previousEndIndex)
						&& (currentEndIndex != -1)) {
					// we are in the interior of the line...
					tempString = LineContents.substring(previousEndIndex + 1,
							currentEndIndex);
				} else {
					tempString = LineContents.substring(previousEndIndex + 1);
				}
				try {
        valueToStore = Double.parseDouble(tempString);
				} catch (java.lang.NumberFormatException eee) {
					System.out.println("row: " + rowIndex + "; col: " + currentColumn + "; string = [" + tempString + "]");
					eee.printStackTrace();
				}

				X.setValue(rowIndex, currentColumn, valueToStore);
				previousEndIndex = currentEndIndex;
				currentColumn++;

			}

		}

		DataReader.close();
		DataStream.close();

		
		return X;
		
	}
	
	public static void write2DMFMtoTextAsInt(MultiFormatMatrix X, String basefilename, String delimiter) throws Exception {
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";
	
		
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();
	
			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);
	
			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();
	
			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);
	
			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print((int)X.getValue(RowIndex,ColIndex) + delimiter);
				}
				else {
					outWriterObject.print((int)X.getValue(RowIndex,ColIndex) + "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();
	
	}

	public static void write2DMFMtoTextDecimals(MultiFormatMatrix X, String basefilename,
			String delimiter, int nDecimals) throws Exception {
		
		System.out.println("write2DMFMtoTextDecimals trying for " + nDecimals + " decimal places");
		
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";
	
		int magicMaxNDecimals = 20;
		
		int nDecimalsToUse = -1;
		if (nDecimals > magicMaxNDecimals || nDecimals < 0) {
			nDecimalsToUse = magicMaxNDecimals;
		} else {
			nDecimalsToUse = nDecimals;
		}
		
		System.out.println("write2DMFMtoTextDecimals trying for (checked) " + nDecimalsToUse + " decimal places");
				
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();
	
			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);
	
			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();
	
			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);
	
			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print( FunTricks.onlySomeDecimalPlaces(X.getValue(RowIndex,ColIndex),nDecimalsToUse)
							+ delimiter);
				}
				else {
					outWriterObject.print( FunTricks.onlySomeDecimalPlaces(X.getValue(RowIndex,ColIndex),nDecimalsToUse)
							+ "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();
	
	}

	public static void write2DMFMtoTextDecimalsAlt(MultiFormatMatrix X, String basefilename,
			String delimiter, int nDecimals) throws Exception {
		
		System.out.println("write2DMFMtoTextDecimals trying for " + nDecimals + " decimal places");
		
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		int magicMaxNDecimals = 20;
		
		int nDecimalsToUse = -1;
		if (nDecimals > magicMaxNDecimals || nDecimals < 0) {
			nDecimalsToUse = magicMaxNDecimals;
		} else {
			nDecimalsToUse = nDecimals;
		}
		
		double trimValue = Math.pow(10, nDecimalsToUse);
		
		System.out.println("write2DMFMtoTextDecimals trying for (checked) " + nDecimalsToUse + " decimal places");
				
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();
	
			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);
	
			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();
	
			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);
	
			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print( ((long)(X.getValue(RowIndex,ColIndex) * trimValue)) / trimValue
							+ delimiter);
				}
				else {
					outWriterObject.print( ((long)(X.getValue(RowIndex,ColIndex) * trimValue)) / trimValue
							+ "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();
	
	}

	public static void write2DMFMtoText(MultiFormatMatrix X, String basefilename, String delimiter) throws Exception {
		String FileName = basefilename + ".txt";
		String InfoFileName = basefilename + ".info.txt";

		
		long NumRows = X.getDimensions()[0];
		long NumCols = X.getDimensions()[1];
		int FormatIndex = X.getDataFormat();

			File InfoFileToWrite = new File(InfoFileName);
			FileWriter outInfoStream = new FileWriter(InfoFileToWrite);
			PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);

			outInfoWriterObject.print(NumRows + "\t = Number of Rows\n");
			outInfoWriterObject.print(NumCols + "\t = Number of Columns\n");
			outInfoWriterObject.print((NumRows*NumCols) + "\t = Total Number of Elements\n");
			outInfoWriterObject.print(FormatIndex + "\t = The MultiFormatMatrix format the matrix was stored in\n");
			outInfoWriterObject.print(delimiter + "\t = The string used to delimit elements in the Rows");
			
			// finish cleaning up the mess...
			outInfoWriterObject.flush();
			outInfoWriterObject.close();

			// the actual goods
			// Create a File
			File FileToWrite = new File(FileName);
			// Create an Output Stream
			FileOutputStream outStream = new FileOutputStream(FileToWrite);
			// Filter bytes to ASCII
			PrintWriter outWriterObject = new PrintWriter(outStream);

			// Here we actually write to file
			for (long RowIndex = 0; RowIndex < NumRows; RowIndex++) {
			for (long ColIndex = 0; ColIndex < NumCols; ColIndex++) {
				if (ColIndex < NumCols - 1) {
					// not the last one in the row...
					outWriterObject.print(X.getValue(RowIndex,ColIndex) + delimiter);
				}
				else {
					outWriterObject.print(X.getValue(RowIndex,ColIndex) + "\n");
				}
			}
			}
			
			// clean up the mess...
			outWriterObject.flush();
			outWriterObject.close();

	}

	public static void print2dMFF(MultiFormatFloat mff) throws Exception {
		for (long rowIndex = 0; rowIndex < mff.getDimensions()[0]; rowIndex++) {
			for (long colIndex = 0; colIndex < mff.getDimensions()[1]; colIndex++) {
				System.out.println("[" + rowIndex + "][" + colIndex + "] = " + mff.getValue(rowIndex,colIndex));
			}
		}
	}


	public static void print2dMFM(MultiFormatMatrix mfm) throws Exception {
		for (long rowIndex = 0; rowIndex < mfm.getDimensions()[0]; rowIndex++) {
			for (long colIndex = 0; colIndex < mfm.getDimensions()[1]; colIndex++) {
				System.out.println("[" + rowIndex + "][" + colIndex + "] = " + mfm.getValue(rowIndex,colIndex));
			}
		}
	}

	public static void print2dMFM(MultiFormatMatrix mfm, String label) throws Exception {
		for (long rowIndex = 0; rowIndex < mfm.getDimensions()[0]; rowIndex++) {
			for (long colIndex = 0; colIndex < mfm.getDimensions()[1]; colIndex++) {
				System.out.println(label + " [" + rowIndex + "][" + colIndex + "] = " + mfm.getValue(rowIndex,colIndex));
			}
		}
	}

	public static MultiFormatFloat[] balancedBlocks(MultiFormatFloat originalMFF, String PathOfWeights, int formatIndex) throws Exception {
		
    String magicDelimiter = " ";

    
    // read in the weight/block file
    File inputFile = new File(PathOfWeights);
    if (!inputFile.exists()) {
      System.err.println("The specified file [" + inputFile + "] has failed to exist!");
      throw new Exception();
    }

    // figure out the total number of maps...
    int nLines = -1234;
    RandomAccessFile randFile = new RandomAccessFile(inputFile,"r");
    long lastRec=randFile.length();
    randFile.close();
    FileReader fileRead = new FileReader(inputFile);
    LineNumberReader lineRead = new LineNumberReader(fileRead);
    lineRead.skip(lastRec);
    nLines = lineRead.getLineNumber();
    fileRead.close();
    lineRead.close();

    BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));

    int[]     rawListBlocks   = new int[   nLines];
    double[]  rawListWeights  = new double[nLines];
    
    String lineContents;
    String[] lineFragments;
    for (int lineIndex = 0; lineIndex < nLines; lineIndex++) {
      lineContents = inputReader.readLine();
      lineFragments = lineContents.split(magicDelimiter);
      // there should be only two fragments
      rawListBlocks[lineIndex]  = Integer.parseInt(lineFragments[0]);
      rawListWeights[lineIndex] = Double.parseDouble(lineFragments[1]);
    }
    
    inputReader.close();
    
    // determine the total number of chunks and the total weight
    int    totBlocks = 0;
    double totWeight = 0.0;
    for (int lineIndex = 0; lineIndex < nLines; lineIndex++) {
      totBlocks += rawListBlocks[lineIndex];
      totWeight += rawListBlocks[lineIndex] * rawListWeights[lineIndex];
    }
    
    System.out.println("totBlocks = " + totBlocks + "; totWeight = " + totWeight);

    long totRows = originalMFF.getDimensions()[0];
    long nCols = originalMFF.getDimensions()[1];
    
    long[] firstRowAfterBlock = new long[totBlocks];
    long[] nRowsInBlock   = new long[totBlocks];
    
    int realBlockIndex = 0;
    for (int lineIndex = 0; lineIndex < nLines; lineIndex++) {
      // within each line of the definitions, we have to repeat a few times...
      for (int withinTypeIndex = 0; withinTypeIndex < rawListBlocks[lineIndex]; withinTypeIndex++) {
        if (realBlockIndex == 0) {
          // this is the first one to be done, we need to seed the recursion...
          firstRowAfterBlock[realBlockIndex] = (long)(totRows * (rawListWeights[lineIndex] / totWeight));
          nRowsInBlock[  realBlockIndex] = firstRowAfterBlock[realBlockIndex];
          realBlockIndex++;
        } else if (realBlockIndex == totBlocks - 1) {
          // this is the last one, we will just make it the final row
          firstRowAfterBlock[realBlockIndex] = totRows;
          nRowsInBlock[  realBlockIndex] = firstRowAfterBlock[realBlockIndex] - firstRowAfterBlock[realBlockIndex - 1];
          realBlockIndex++;
        } else {
          // anything in the middle. we just take the previous block boundary and add the weighted fraction of the total number of rows...
          firstRowAfterBlock[realBlockIndex] = firstRowAfterBlock[realBlockIndex - 1] + (long)(totRows * (rawListWeights[lineIndex] / totWeight));
          nRowsInBlock[  realBlockIndex] = firstRowAfterBlock[realBlockIndex] - firstRowAfterBlock[realBlockIndex - 1];
          realBlockIndex++;
        }
      }
    } // end for(rawBlockIndex)
    
    long nRowsThis = -1;
    long rowOffset = -5;
        
    long[] getCoords = new long[2];
    long[] setCoords = new long[2];

    MultiFormatFloat[] blockMatrices = new MultiFormatFloat[totBlocks];
    
    for (int aBlockIndex = 0; aBlockIndex < totBlocks; aBlockIndex++) {
//      System.out.println("for block #" + aBlockIndex + " the last row is " + lastRowInBlock[aBlockIndex] + "; nRows = " + nRowsInBlock[aBlockIndex]);
//      totalRowCounter += nRowsInBlock[aBlockIndex];
      
      nRowsThis = nRowsInBlock[aBlockIndex];
      if (aBlockIndex == 0) {
        rowOffset = 0;
      } else {
        rowOffset = firstRowAfterBlock[aBlockIndex - 1];
      }
      blockMatrices[aBlockIndex] = new MultiFormatFloat(formatIndex, new long[]{nRowsInBlock[aBlockIndex],nCols});

      // a for loop moving the rows from the original matrix to the little ones...
      for (long outRowIndex = 0; outRowIndex < nRowsThis; outRowIndex++) {
        setCoords[0] = outRowIndex;
        getCoords[0] = outRowIndex + rowOffset;
        for (long colIndex = 0; colIndex < nCols; colIndex++) {
          setCoords[1] = colIndex;
          getCoords[1] = colIndex;
          blockMatrices[aBlockIndex].setValue(setCoords,
              originalMFF.getValue(getCoords)
              );
        }
        
      }
      blockMatrices[aBlockIndex].finishRecordingMatrix();
    }
  
    return blockMatrices;
	}

	
	
	public static MultiFormatFloat descriptiveStatistics(MultiFormatFloat X) throws Exception {

    
    if (X.getNumDimensions() != 2) {
      System.err.println("X.getNumDimensions() {" + X.getNumDimensions() + "} != 2");
      throw new Exception();
    }
    
    long nRows = X.getDimensions()[0];
    long nCols = X.getDimensions()[1];
    
    double[] columnSumSquares  = new double[(int)nCols];
    double[] columnSumDiffSquares = new double[(int)nCols];
    double[] columnSums        = new double[(int)nCols];
    double[] meansDouble       = new double[(int)nCols];
    double   tempDouble         = Double.NaN;

    float[] tempMin = new float[(int)nCols];
    float[] tempMax = new float[(int)nCols];
    
    // initialize the min and max
    for (int colIndex = 0; colIndex < nCols; colIndex++) {
      tempMin[colIndex] = Float.POSITIVE_INFINITY;
      tempMax[colIndex] = Float.NEGATIVE_INFINITY;
    }

    long[] getCoords = new long[2];
    
    for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
      getCoords[0] = rowIndex;
      for (long colIndex = 0; colIndex < nCols; colIndex++) {
        getCoords[1] = colIndex;
//        tempDouble = X.getValue(rowIndex,colIndex);
        tempDouble = X.getValue(getCoords);
        columnSums[(int)colIndex] += tempDouble;
        columnSumSquares[(int)colIndex] += tempDouble*tempDouble;
        
        if (tempDouble > tempMax[(int)colIndex]) {
          tempMax[(int)colIndex] = (float)tempDouble;
        }
        if (tempDouble < tempMin[(int)colIndex]) {
          tempMin[(int)colIndex] = (float)tempDouble;
        }
      }
    }

    
    
    // Beware the MAGIC NUMBER!!! the format is assumed to be in memory...
    MultiFormatFloat descriptiveStats = new MultiFormatFloat(1,new long[] {4,nCols});

    float meanTemp;
    float stdTemp;

    // the mins and maxes and means
    for (long colIndex = 0; colIndex < nCols; colIndex++) {
      // min
      descriptiveStats.setValue(0,colIndex,tempMin[(int)colIndex]);
      // max
      descriptiveStats.setValue(1,colIndex,tempMax[(int)colIndex]);
      // mean
      meanTemp = (float)(columnSums[(int)colIndex] / nRows);
      descriptiveStats.setValue(2,colIndex,meanTemp);
      meansDouble[(int)colIndex] = columnSums[(int)colIndex] / nRows;
    }

    // the stds... (this way results in fewer rounding errors...)
    for (long rowIndex = 0; rowIndex < nRows; rowIndex++) {
      getCoords[0] = rowIndex;
      for (long colIndex = 0; colIndex < nCols; colIndex++) {
        getCoords[1] = colIndex;
//        tempDouble = X.getValue(rowIndex,colIndex);
        tempDouble = X.getValue(getCoords);
        columnSumDiffSquares[(int)colIndex] += (tempDouble - meansDouble[(int)colIndex])*(tempDouble - meansDouble[(int)colIndex]);
      }
    }

    
    for (long colIndex = 0; colIndex < nCols; colIndex++) {
//      meanTemp = (float)(columnSums[(int)colIndex] / nRows);
      stdTemp = (float)(Math.sqrt(columnSumDiffSquares[(int)colIndex] / nRows));
//      ScalingValues.setValue(0,colIndex, stdTemp);
      descriptiveStats.setValue(3,colIndex,stdTemp);
    }    
    
    return descriptiveStats;
  
	}
	
	public static void writeMFF(MultiFormatFloat matrix, String FileName)
			throws Exception {

		MultiFormatFloat newMatrix = new MultiFormatFloat(3,
				matrix.getDimensions(), FileName);

		long[] currentIndex = new long[newMatrix.getNumDimensions()];
		boolean carryOn = true;
		float tempFloat = -3.0F;
		int nDims = newMatrix.getNumDimensions();
		long[] dimList = newMatrix.getDimensions();

		while (carryOn) {

			tempFloat = matrix.getValue(currentIndex);
			newMatrix.setValue(currentIndex, tempFloat);

			// determine the last dimension that can be bumped up...
			// all the "-1"s are because we are dealing in indices...
			for (int dimIndex = nDims - 1; dimIndex >= 0; dimIndex--) {
				if (currentIndex[dimIndex] < dimList[dimIndex] - 1) {
					currentIndex[dimIndex]++;
					for (int prevDimIndex = nDims - 1; prevDimIndex > dimIndex; prevDimIndex--) {
						currentIndex[prevDimIndex] = 0;
					}
					carryOn = true;
					break;
				}
				carryOn = false;
			} // end for
		} // end while

		// newMatrix.finalize();
		newMatrix.finishRecordingMatrix();

	}

	public static void writeMFM(MultiFormatMatrix matrix, String FileName)
	throws Exception {

		MultiFormatMatrix newMatrix = new MultiFormatMatrix(3,
				matrix.getDimensions(), FileName);

		long[] currentIndex = new long[newMatrix.getNumDimensions()];
		boolean carryOn = true;
		double tempDouble = -3.0F;
		int nDims = newMatrix.getNumDimensions();
		long[] dimList = newMatrix.getDimensions();

		while (carryOn) {

			tempDouble = matrix.getValue(currentIndex);
			newMatrix.setValue(currentIndex, tempDouble);

			// determine the last dimension that can be bumped up...
			// all the "-1"s are because we are dealing in indices...
			for (int dimIndex = nDims - 1; dimIndex >= 0; dimIndex--) {
				if (currentIndex[dimIndex] < dimList[dimIndex] - 1) {
					currentIndex[dimIndex]++;
					for (int prevDimIndex = nDims - 1; prevDimIndex > dimIndex; prevDimIndex--) {
						currentIndex[prevDimIndex] = 0;
					}
					carryOn = true;
					break;
				}
				carryOn = false;
			} // end for
		} // end while

//		newMatrix.finalize();
		newMatrix.finishRecordingMatrix();

}

	public static MultiFormatFloat recenterAndRescale(
			MultiFormatFloat  X,
			MultiFormatMatrix  CenteringValuesIn,
			MultiFormatMatrix  ScalingValuesIn
			) throws Exception {


//		MultiFormatFloat  X                         = (MultiFormatFloat)this.pullInput(0);
//		MultiFormatFloat  CenteringValuesIn         = (MultiFormatFloat)this.pullInput(1);
//		MultiFormatFloat  ScalingValuesIn           = (MultiFormatFloat)this.pullInput(2);
//    String            OutputPath                = (          String)this.pullInput(3);
//		long              nElementsThreshold = (           (Long)this.pullInput(4)).longValue();

		long nRowsX = X.getDimensions()[0];
		long nColsX = X.getDimensions()[1];

		// regardless, we rescale the data
		int formatIndex = 1; // just put it in memory

		double currentElement = -15.0;

    MultiFormatFloat standardizedMatrix = new MultiFormatFloat(formatIndex, new long[] { nRowsX, nColsX });

    long[] getSetCoords = new long[2];
    long[] getScaleCoords = new long[2];
    
    getScaleCoords[0] = 0;
		for (long rowIndex = 0; rowIndex < nRowsX; rowIndex++) {
      getSetCoords[0] = rowIndex;
			for (long colIndex = 0; colIndex < nColsX; colIndex++) {
        getSetCoords[1]   = colIndex;
        getScaleCoords[1] = colIndex;
        
//				currentElement = X.getValue(rowIndex, colIndex);
        currentElement = X.getValue(getSetCoords);

        // we center and rescale...
//				standardizedMatrix.setValue(rowIndex, colIndex,
            standardizedMatrix.setValue(getSetCoords,
            		(float)(
				        (currentElement - CenteringValuesIn.getValue(getScaleCoords))
                / ScalingValuesIn.getValue(getScaleCoords)
//                (currentElement - CenteringValuesIn.getValue(0,colIndex))
//				        / ScalingValuesIn.getValue(0, colIndex)
				    )
				);
			}
		}

    standardizedMatrix.finishRecordingMatrix();
    
		// then we kick it out
	
		return standardizedMatrix;
	}

	
}
