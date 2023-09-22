package org.R2Useful;

// import ncsa.d2k.modules.projects.dtcheng.*;
// import org.R2Useful.*;
import java.util.Date;
// import ncsa.d2k.modules.projects.dtcheng.primitive.*;

public class SortComb11double {

  public String getModuleInfo() {
    return "MffSortComb11. This module sorts a 2-d double matrix by the values in the specified"
        + " column. That is, the rows are preserved together and are put in increasing order"
        + " by the values in the specified column.<p>This will also sort a 1-d long array if"
        + " supplied (otherwise pass in a null). This is so that the matrix could be"
        + " restored to another form (e.g., a map). A long array is necessary due to the"
        + " shortcomings of floats in storing large integers. A second long array will be"
        + " created (and similarly sorted) to contain the original row number within the"
        + " matrix.";
  }

  // bad programming, there should really be a lock on these or something, but i'm going
  // to try to use them responsibly here.
  // DO NOT blythely copy this code....
  //	private long[] indicesA = null;
  //	private long[] indicesB = null;
  private double contentsA = Double.NaN;
  private double contentsB = Double.NaN;
  int rowListValueA = -1;
  int rowListValueB = -1;

  private double[][] originalMatrix = null;
  private int columnToSortBy = -1;

  private int[] originalRowList = null;
  private double[] sortedMatrix = null;

  private boolean readyToRun = false;
  private boolean sortingHasBeenDone = false;

  private boolean verboseFlag = true;

  public SortComb11double(double[][] OriginalMatrix, int ColumnToSortBy) {
    originalMatrix = OriginalMatrix;
    columnToSortBy = ColumnToSortBy;

    //		indicesA = new long[2]; // assuming 2-d matrix
    //		indicesB = new long[2]; // assuming 2-d matrix

    readyToRun = true;
    sortingHasBeenDone = false;
    verboseFlag = true;
  }

  public SortComb11double(double[][] OriginalMatrix, int ColumnToSortBy, boolean verboseFlag) {
    originalMatrix = OriginalMatrix;
    columnToSortBy = ColumnToSortBy;

    //		indicesA = new long[2]; // assuming 2-d matrix
    //		indicesB = new long[2]; // assuming 2-d matrix

    readyToRun = true;
    sortingHasBeenDone = false;
    this.verboseFlag = verboseFlag;
  }

  private void swapRows(double[] sortMatrix, int[] originalRowList, int rowA, int rowB) {
    contentsA = sortMatrix[rowA]; // [colIndex]; //.getValue(indicesA);
    contentsB = sortMatrix[rowB]; // [colIndex]; //.getValue(indicesB);

    // put contents B into row A ; A into B
    sortMatrix[rowB] = contentsA; // .setValue(indicesB,contentsA[colIndex]);
    sortMatrix[rowA] = contentsB; // .setValue(indicesA,contentsB[colIndex]);

    // swap the longList rows
    rowListValueA = originalRowList[rowA];
    rowListValueB = originalRowList[rowB];

    originalRowList[rowA] = rowListValueB;
    originalRowList[rowB] = rowListValueA;
  }

  public void doSorting() throws Exception {

    if (!readyToRun) {
      System.out.println("matrix and column have not been specified");
      throw new Exception();
    }

    if (sortingHasBeenDone) {
      System.out.println("matrix has already been sorted");
      throw new Exception();
    }

    //    float            InitialGap     = ((          Float)this.pullInput(1)).floatValue();

    // get some info
    int nRows = originalMatrix.length;

    sortedMatrix =
        new double[nRows]; // new MultiFormatMatrix(originalMatrix.getDataFormat(), theDims);
    originalRowList = new int[nRows];

    // if there is nothing to sort, just bail leaving some zero size matrices laying around....
    if (nRows == 0) {
      sortingHasBeenDone = true;
      return;
    }

    int nCols = originalMatrix[0].length;

    if (columnToSortBy >= nCols) {
      System.out.println(
          "sorting column does not exist: ColumnToSortBy ("
              + columnToSortBy
              + ") >= nCols ("
              + nCols
              + ")");
      throw new Exception();
    }

    // make copy of the original matrix to play with
    // also, add an extra column that has the row index in it...

    if (verboseFlag) {
      System.out.println("Starting initialization at " + new Date());
    }

    //		int nColsSorted = nCols; // changing because i'm going to keep the list of original indices
    // separate

    // let us just pull out the single column we're interested in.
    // we can do the reconstruction in a separate method
    //		long[] theDims = {nRows, 1};
    //		int nDims = theDims.length;

    //		long[]  getIndices      = new long[nDims];
    //		long[]  setIndices      = new long[nDims];
    double tempDouble = -3.0F;

    //		getIndices[1] = columnToSortBy; // always use the column we're interested in
    //		setIndices[1] = 0;

    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
      //			getIndices[0] = rowIndex;
      //			setIndices[0] = rowIndex;
      // initialize the long array
      originalRowList[rowIndex] = rowIndex;

      // initialize the MFF
      tempDouble = originalMatrix[rowIndex][columnToSortBy]; // .getValue(getIndices);
      sortedMatrix[rowIndex] = tempDouble; // .setValue(setIndices,tempDouble);
    }
    //		sortedMatrix.finishRecordingMatrix();

    System.gc();

    // now try to do the sorting on the copy....
    if (verboseFlag) {
      System.out.println("Starting sort at " + new Date());
    }

    int gap = nRows;
    int nSwaps = -1; // initialize to a non-zero value
    int internalRowIndex = -2;
    long[] sortIndicesA = new long[2]; // assuming 2-d matrix
    long[] sortIndicesB = new long[2]; // assuming 2-d matrix
    //		long[] sortIndicesA = new long[(int)nColsSorted];
    //		long[] sortIndicesB = new long[(int)nColsSorted];
    //		contentsA = new double;
    //		contentsB = new double 5;

    double magicShrinkFactor = 1.3F;

    int rowA, rowB = 0;
    double valueRowA, valueRowB = 0.0F;

    int approxPassesToOne = (int) Math.round(Math.log(nRows) / Math.log(magicShrinkFactor));
    //		int maxPasses = 20;
    if (verboseFlag) {
      System.out.println(
          "It will require roughly " + approxPassesToOne + " passes to get to a gap of unity.");
    }

    // initialize some goodies
    boolean keepGoing = true;
    int nPasses = 0;
    //		System.out.println("sI length = " + sortIndicesA.length);
    //		sortIndicesA[1] = ColumnToSortBy;
    //		sortIndicesB[1] = ColumnToSortBy;
    sortIndicesA[1] = 0; // always using a single column at this point
    sortIndicesB[1] = 0; // always using a single column at this point
    while (keepGoing) {
      // update the gap value for the next comb
      if (gap > 1) {
        gap = (int) (gap / magicShrinkFactor);

        // check for special values of the gap and adjust accordingly
        if (gap == 10 || gap == 9) {
          gap = 11;
        }
      }

      //			System.out.println("near top of gap loop: gap = " + gap + "; nSwaps = " + nSwaps);

      internalRowIndex = 0;
      nSwaps = 0;
      // do a single comb over the data
      while (internalRowIndex + gap < nRows) {
        //				System.out.println(" == dealing with row " + internalRowIndex);

        rowA = internalRowIndex;
        rowB = internalRowIndex + gap;

        sortIndicesA[0] = rowA;
        sortIndicesB[0] = rowB;

        valueRowA = sortedMatrix[rowA]; // sortedMatrix.getValue(sortIndicesA);
        valueRowB = sortedMatrix[rowB]; // sortedMatrix.getValue(sortIndicesB);

        //				System.out.println(" rowA [" + rowA + "] = " + valueRowA + "; rowB [" + rowB + "] = "
        // + valueRowB);

        if (valueRowA > valueRowB) {
          swapRows(sortedMatrix, originalRowList, rowA, rowB);
          nSwaps++;
          //					System.out.println("  -- now swapping rows " + internalRowIndex + " and " +
          // (internalRowIndex + gap) + "; nSwaps now = " + nSwaps);
        }
        internalRowIndex++;

        //				System.out.println(" == bottom: internalRowIndex = " + internalRowIndex + "; + gap = "
        // + (internalRowIndex + gap) + "; nRows = " + nRows);
      } // end comb while loop

      if (gap > 1) {
        keepGoing = true;
      } else {
        if (nSwaps == 0) {
          keepGoing = false;
        } else {
          keepGoing = true;
        }
      }

      nPasses++;
      //			System.out.println("at bottom [" + nPasses + "]: gap = " + gap + "; nSwaps = " + nSwaps +
      // "; keepGoing = " + keepGoing);
      //			if (nPasses == maxPasses) {
      //			keepGoing = false;
      //			}
      if (verboseFlag) {
        System.out.println(
            "Finishing pass ["
                + nPasses
                + "/"
                + approxPassesToOne
                + "] (gap = "
                + gap
                + "; nSwaps = "
                + nSwaps
                + ") at "
                + new Date());
      }
      System.gc();
    } // end gap while loop

    if (verboseFlag) {
      System.out.println(
          "Finished with sort ("
              + nPasses
              + " passes and "
              + nSwaps
              + " swaps required) "
              + new Date());
    }

    sortingHasBeenDone = true;
  }

  public int[] getSortedIndexList() {
    if (this.sortingHasBeenDone) {
      return this.originalRowList;
    }
    return null;
  }

  public double[] getSortedColumnOnly() {
    if (this.sortingHasBeenDone) {
      return this.sortedMatrix;
    }
    return null;
  }

  public static double[][] reconstructFullSortedMatrix(
      double[][] originalMatrix, int[] originalRowIndices) {
    int nRows = originalMatrix.length;
    int nCols = originalMatrix[0].length;

    double[][] sortedMatrix =
        new double[nRows]
            [nCols]; // MultiFormatMatrix(originalMatrix.getDataFormat(),originalMatrix.getDimensions());

    int originalRowIndex = -1;
    for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
      originalRowIndex = originalRowIndices[rowIndex];
      for (int colIndex = 0; colIndex < nCols; colIndex++) {
        sortedMatrix[rowIndex][colIndex] = originalMatrix[originalRowIndex][colIndex];
        //				sortedMatrix.setValue(rowIndex,colIndex,
        //						originalMatrix.getValue(originalRowIndex,colIndex)
        //				);
      }
    }
    return sortedMatrix;
  }

  public double[][] getFullySortedMatrix() {

    return this.reconstructFullSortedMatrix(this.originalMatrix, this.originalRowList);
  }
}
