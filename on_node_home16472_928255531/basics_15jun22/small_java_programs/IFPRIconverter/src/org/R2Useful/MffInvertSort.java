package org.R2Useful;

//import org.R2Useful.*;

public class MffInvertSort
{


  public static MultiFormatFloat InvertSortMFF(
  		MultiFormatFloat InputMatrix,
  		int[]                RowList,
  		long      nElementsThreshold
  ) throws Exception {

  	
// "MffInvertSort: This module inverts the sorting of the module MffSortComb11. That is, a 1-d integer array " +
//      		"is created by MffSortComb11 which contains the original row index. This module puts the rows " +
//          "back in the order indicated by such a column.";

//    MultiFormatFloat InputMatrix = (MultiFormatFloat)this.pullInput(0);
//    int[]                RowList = (           int[])this.pullInput(1);
//    long      nElementsThreshold = (           (Long)this.pullInput(2)).longValue();

    long nRows = InputMatrix.getDimensions()[0];
    long nCols = InputMatrix.getDimensions()[1];

//    long nColsOutput = nCols - 1;
    
    // check if we have requested the final column rather than specifying a number...
    if (RowList.length != InputMatrix.getDimensions()[0]) {
      System.out.println("RowList.length {" + RowList.length + "} != " + "{" + InputMatrix.getDimensions()[0] + "} InputMatrix.getDimensions()[0]");
      throw new Exception();
    }
    

    long nElements = nCols * nRows;
    int formatIndex = -1;
    if (nElements < nElementsThreshold) { // small means keep it in core; single dimensional in memory is best
      formatIndex = 1; // Beware the MAGIC NUMBER!!!
    }
    else { // not small means big, so go out of core; serialized blocks on disk are best
      formatIndex = 3; // Beware the MAGIC NUMBER!!!
    }

    MultiFormatFloat reorderedMatrix = new MultiFormatFloat(formatIndex, new long[] {nRows, nCols});

//    long[] indexIndices = {-51,-52};
    long[] getIndices = {-1,-2};
    long[] setIndices = {-1,-2};

//    indexIndices[1] = IndexColumn;

    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
//      indexIndices[0] = rowIndex;
      getIndices[0] = rowIndex;
      
      // we will be putting things back into a different row entirely
//      setIndices[0] = (long)InputMatrix.getValue(indexIndices);
      setIndices[0] = RowList[rowIndex];
      
      for (int colIndex = 0; colIndex < nCols; colIndex++) {
        // let's just skip the index column since we don't need to write that down in the new matrix
          getIndices[1] = colIndex;
          setIndices[1] = colIndex;
          // copy them over
          reorderedMatrix.setValue(setIndices,
              InputMatrix.getValue(getIndices));
      }
    }
    
    
    return reorderedMatrix;
//    this.pushOutput(reorderedMatrix, 0);
  }
}
