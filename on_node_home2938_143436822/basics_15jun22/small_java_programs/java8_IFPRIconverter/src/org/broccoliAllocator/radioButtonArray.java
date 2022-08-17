package org.broccoliAllocator;

public class radioButtonArray {

    private boolean[][] rowInUseFlags = null;
    private int nRows = -1;
    private int nCols = -2;

    public radioButtonArray(int nRowsToInitialize, int nColsToInitialize) {

	nRows = nRowsToInitialize;
	nCols = nColsToInitialize;
	
	rowInUseFlags  = new boolean[nRows][nCols];

	// mark all spots as false
	for (int rowIndex = 0; rowIndex < nRows; rowIndex++ ) {
	    for (int colIndex = 0; colIndex < nCols; colIndex++ ) {
		rowInUseFlags[rowIndex][colIndex] = false;
	    }
	}
    }
    
    
    public void pushButtonInRow(int rowIndexToPush, int colIndexToPush) {
	
	// go to that row, clear it out, then reassign the appropriate column as true
	
	for (int colIndex = 0; colIndex < nCols; colIndex++) {
	   rowInUseFlags[rowIndexToPush][colIndex] = false; 
	}
	
	rowInUseFlags[rowIndexToPush][colIndexToPush] = true;
    }

    public void clearRow(int rowIndexToClear) {
	// go to that row, clear it out
	for (int colIndex = 0; colIndex < nCols; colIndex++) {
	   rowInUseFlags[rowIndexToClear][colIndex] = false; 
	}
    }
    


}
