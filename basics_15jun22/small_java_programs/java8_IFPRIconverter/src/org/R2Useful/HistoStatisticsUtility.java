package org.R2Useful;

public class HistoStatisticsUtility {

  //	private boolean useFloating = false;

  private int nNumbers;

  private int nFriendCols;

  private double doubleSortingTotal;
  private double doubleSortingSquaredTotal;
  private double doubleSortingMin;
  private double doubleSortingMax;

  private double[] doubleFriendTotal;
  private double[] doubleFriendSquaredTotal;
  private double[] doubleFriendMin;
  private double[] doubleFriendMax;

  private int initialSizeOfStorageArray = 1;
  private double[][] doubleArrayRaw = null; // new double[initialSizeOfStorageArray][2];
  private double[][] doubleArrayRawCopy = null;

  public HistoStatisticsUtility(int nFriendCols) {
    this.nFriendCols = nFriendCols;
    this.reset();
  }

  public void reset() {
    nNumbers = 0;
    initialSizeOfStorageArray = 1000;

    doubleSortingTotal = 0;
    doubleSortingSquaredTotal = 0;
    doubleSortingMin = Double.POSITIVE_INFINITY;
    doubleSortingMax = Double.NEGATIVE_INFINITY;

    doubleFriendTotal = new double[nFriendCols];
    doubleFriendSquaredTotal = new double[nFriendCols];
    doubleFriendMin = new double[nFriendCols];
    doubleFriendMax = new double[nFriendCols]; // Double.NEGATIVE_INFINITY;

    for (int friendColIndex = 0; friendColIndex < nFriendCols; friendColIndex++) {
      doubleFriendMin[friendColIndex] = Double.POSITIVE_INFINITY;
      doubleFriendMax[friendColIndex] = Double.NEGATIVE_INFINITY;
    }

    doubleArrayRaw = new double[initialSizeOfStorageArray][1 + nFriendCols];
  }

  public void useDoublePair(double sortingValue, double[] friendValues) throws Exception {
    doubleSortingTotal += sortingValue;
    doubleSortingSquaredTotal += sortingValue * sortingValue;
    if (sortingValue > doubleSortingMax) {
      doubleSortingMax = sortingValue;
    }
    if (sortingValue < doubleSortingMin) {
      doubleSortingMin = sortingValue;
    }

    for (int friendColIndex = 0; friendColIndex < nFriendCols; friendColIndex++) {

      doubleFriendTotal[friendColIndex] += friendValues[friendColIndex];
      doubleFriendSquaredTotal[friendColIndex] +=
          friendValues[friendColIndex] * friendValues[friendColIndex];
      if (friendValues[friendColIndex] > doubleFriendMax[friendColIndex]) {
        doubleFriendMax[friendColIndex] = friendValues[friendColIndex];
      }
      if (friendValues[friendColIndex] < doubleFriendMin[friendColIndex]) {
        doubleFriendMin[friendColIndex] = friendValues[friendColIndex];
      }
    } // for friendColIndex

    // the nNumbers value tells us the next open spot...
    doubleArrayRaw[nNumbers][0] = sortingValue;
    for (int friendColIndex = 0; friendColIndex < nFriendCols; friendColIndex++) {
      doubleArrayRaw[nNumbers][1 + friendColIndex] = friendValues[friendColIndex];
    }
    nNumbers++;

    // now check if we need to expand the array
    if (nNumbers == doubleArrayRaw.length) {
      doubleArrayRawCopy = doubleArrayRaw.clone();
      doubleArrayRaw = new double[doubleArrayRawCopy.length * 2][doubleArrayRawCopy[0].length];

      // note that nNumbers has already been bumped up, so we can use simple less than
      for (int rawIndex = 0; rawIndex < nNumbers; rawIndex++) {
        for (int colIndex = 0; colIndex < doubleArrayRawCopy[0].length; colIndex++) {
          doubleArrayRaw[rawIndex][colIndex] = doubleArrayRawCopy[rawIndex][colIndex];
          //					doubleArrayRaw[rawIndex][1] = doubleArrayRawCopy[rawIndex][1];
        } // for colIndex
      } // for rawIndex
    } // if (need to extend)
  }

  public double[][] sortDatasetWithCumulativeFraction() throws Exception {

    SortComb11double sorterThing = new SortComb11double(this.getDataset(), 0, false);

    sorterThing.doSorting();

    double[][] sortedDataset = sorterThing.getFullySortedMatrix();
    double[][] sortedWithCumulativeFraction =
        new double[nNumbers][nFriendCols + 2]; // sorted + friends + cumulative fraction

    for (int rowIndex = 0; rowIndex < nNumbers; rowIndex++) {
      for (int colIndex = 0; colIndex < nFriendCols + 1; colIndex++) {
        sortedWithCumulativeFraction[rowIndex][colIndex] = sortedDataset[rowIndex][colIndex];
      }
      sortedWithCumulativeFraction[rowIndex][nFriendCols + 1] = (rowIndex + 1.0) / nNumbers;
    }

    return sortedWithCumulativeFraction;
  }

  public double[][] getGoofyMediansWithinQuantileThing(int nQuantiles) throws Exception {

    // the idea here is to divide the "sort" domain into several quantiles.
    // then, within each quantile, find the median of both the sorted values and
    // the friend values and write those down...

    if (nNumbers == 0) {
      return new double[nQuantiles][1 + nFriendCols];
    }

    // idiot proofing: make sure we have a reasonable number of quantiles
    if (nQuantiles < 2) {
      nQuantiles = 1;
    } else if (nQuantiles > nNumbers) {
      nQuantiles = getN();
    }

    // step one: do the sorting
    double[][] sortedVersion = sortDatasetWithCumulativeFraction();

    // go through the quantiles and do the median thing
    int nInThisQuantile = -5;
    int quantileStartIndex = -1;
    int quantileMedianIndexLo = -3;
    int quantileMedianIndexHi = -3;
    int quantileEndIndex = -2;
    double[][] friendValuesToSort = null;
    double[][] friendValuesAfterSorting = null;
    double sortedMedianValue = -7;
    double friendMedianValue = -5.0;

    SortComb11double sorterThing = null;

    // the output matrix; quantile number / median sorted value / median friend value among those
    double[][] quantileSummaries = new double[nQuantiles][1 + nFriendCols];

    for (int quantileIndex = 0; quantileIndex < nQuantiles; quantileIndex++) {

      // finding the "sorting value" median
      quantileStartIndex = (quantileIndex * nNumbers) / nQuantiles;
      quantileEndIndex = ((quantileIndex + 1) * nNumbers) / nQuantiles - 1;
      if ((quantileEndIndex - quantileStartIndex) % 2 == 1) {
        // we have an even number of numbers here, so we have to take the average of two of them
        quantileMedianIndexLo = (quantileEndIndex + quantileStartIndex) / 2;
        quantileMedianIndexHi = quantileMedianIndexLo + 1;
      } else {
        // we have an odd number of numbers here, so we have to take them to be the same
        quantileMedianIndexLo = (quantileEndIndex + quantileStartIndex) / 2;
        quantileMedianIndexHi = quantileMedianIndexLo;
      }

      //			System.out.println("sI = " + quantileStartIndex + "; eI = " + quantileEndIndex + "; qmIl
      // = " + quantileMedianIndexLo + "; qmIH = " + quantileMedianIndexHi);

      sortedMedianValue =
          (sortedVersion[quantileMedianIndexLo][0] + sortedVersion[quantileMedianIndexHi][0])
              / 2; // Beware the MAGIC NUMBER!!! the sorting values are in column 0
      quantileSummaries[quantileIndex][0] = sortedMedianValue;

      // we now have to do another sorting exercise to get the friends median value...
      nInThisQuantile = quantileEndIndex - quantileStartIndex + 1;
      for (int friendCol = 0; friendCol < nFriendCols; friendCol++) {
        friendValuesToSort = new double[nInThisQuantile][1];
        for (int rowIndex = 0; rowIndex < nInThisQuantile; rowIndex++) {
          friendValuesToSort[rowIndex][0] =
              sortedVersion[quantileStartIndex + rowIndex][
                  1 + friendCol]; // Beware the MAGIC NUMBER!!! the friends are in column 2
        }
        sorterThing = new SortComb11double(friendValuesToSort, 0, false);

        sorterThing.doSorting();

        friendValuesAfterSorting = sorterThing.getFullySortedMatrix();
        friendMedianValue =
            (friendValuesAfterSorting[quantileMedianIndexLo - quantileStartIndex][0]
                    + friendValuesAfterSorting[quantileMedianIndexHi - quantileStartIndex][0])
                / 2; // Beware the MAGIC NUMBER!!! there is only a single column here

        quantileSummaries[quantileIndex][1 + friendCol] = friendMedianValue;
      } // for friendCol
    } // for quantileIndex

    return quantileSummaries;
  }

  public double[] getMinsAsDouble() {
    double[] allMins = new double[1 + nFriendCols];
    allMins[0] = doubleSortingMin;
    for (int friendIndex = 0; friendIndex < nFriendCols; friendIndex++) {
      allMins[1 + friendIndex] = doubleFriendMin[friendIndex];
    }
    //			return new double[] {doubleSortingMin , doubleFriendMin};
    return allMins;
  }

  public double[] getMaxesAsDouble() {
    double[] allMaxes = new double[1 + nFriendCols];
    allMaxes[0] = doubleSortingMax;
    for (int friendIndex = 0; friendIndex < nFriendCols; friendIndex++) {
      allMaxes[1 + friendIndex] = doubleFriendMax[friendIndex];
    }
    //			return new double[] {doubleSortingMin , doubleFriendMin};
    return allMaxes;
  }

  public double[] getMeans() {
    double[] allMeans = new double[1 + nFriendCols];
    allMeans[0] = doubleSortingTotal / nNumbers;
    for (int friendIndex = 0; friendIndex < nFriendCols; friendIndex++) {
      allMeans[1 + friendIndex] = doubleFriendTotal[friendIndex] / nNumbers;
    }
    //			return new double[] {doubleSortingMin , doubleFriendMin};
    return allMeans;

    //		return new double[] {doubleSortingTotal / nNumbers , doubleFriendTotal / nNumbers};
  }

  public double[] getStds() {
    double[] means = getMeans();

    double[] allStds = new double[1 + nFriendCols];
    allStds[0] = Math.sqrt(doubleSortingSquaredTotal / nNumbers - means[0] * means[0]);
    for (int friendIndex = 0; friendIndex < nFriendCols; friendIndex++) {
      allStds[1 + friendIndex] =
          Math.sqrt(
              doubleFriendSquaredTotal[friendIndex] / nNumbers
                  - means[1 + friendIndex] * means[1 + friendIndex]);
    }
    //				return new double[] {doubleSortingMin , doubleFriendMin};
    return allStds;

    //			return new double[] {
    //					Math.sqrt( doubleSortingSquaredTotal / nNumbers - means[0] * means[0]  ) ,
    //					Math.sqrt(  doubleFriendSquaredTotal / nNumbers - means[1] * means[1]  )
    //			};
  }

  public int getN() {
    return nNumbers;
  }

  public double[][] getDataset() {
    // chop it down to just what we have put in...
    doubleArrayRawCopy = new double[nNumbers][1 + nFriendCols];

    // note that nNumbers has already been bumped up, so we can use simple less than
    for (int rawIndex = 0; rawIndex < nNumbers; rawIndex++) {
      for (int colIndex = 0; colIndex < 1 + nFriendCols; colIndex++) {
        doubleArrayRawCopy[rawIndex][colIndex] = doubleArrayRaw[rawIndex][colIndex];
      }
    }

    return doubleArrayRawCopy;
  }

  public String printDataset() {

    double[][] minimalDataset = getDataset();

    String asString = "";

    for (int rawIndex = 0; rawIndex < nNumbers; rawIndex++) {
      asString += rawIndex + "\t";
      for (int colIndex = 0; colIndex < nFriendCols; colIndex++) {
        asString += minimalDataset[rawIndex][colIndex] + ",";
      }
      asString += minimalDataset[rawIndex][nFriendCols] + "\n";
    }

    return asString;
  }

  public String getAllPretty() {

    String asString = "";
    double[] theseStatsToPrint = null;

    asString += "mins:\t";
    theseStatsToPrint = this.getMinsAsDouble();
    for (int colIndex = 0; colIndex < nFriendCols; colIndex++) {
      asString += theseStatsToPrint[colIndex] + ",";
    }
    asString += theseStatsToPrint[nFriendCols] + "\n";

    asString += "maxes:\t";
    theseStatsToPrint = this.getMaxesAsDouble();
    for (int colIndex = 0; colIndex < nFriendCols; colIndex++) {
      asString += theseStatsToPrint[colIndex] + ",";
    }
    asString += theseStatsToPrint[nFriendCols] + "\n";

    asString += "means:\t";
    theseStatsToPrint = this.getMeans();
    for (int colIndex = 0; colIndex < nFriendCols; colIndex++) {
      asString += theseStatsToPrint[colIndex] + ",";
    }
    asString += theseStatsToPrint[nFriendCols] + "\n";

    asString += "stds:\t";
    theseStatsToPrint = this.getStds();
    for (int colIndex = 0; colIndex < nFriendCols; colIndex++) {
      asString += theseStatsToPrint[colIndex] + ",";
    }
    asString += theseStatsToPrint[nFriendCols] + "\n";

    asString += "n: " + this.getN();
    return asString;

    //		return ("min = " + getMinsAsDouble()[0] + "\n" + getMinsAsDouble()[1] + " max = " +
    // getMaxesAsDouble()[0] + "," + getMaxesAsDouble()[1] +
    //				" mean = " + getMeans()[0] + "," + getMeans()[1] + " std = " + getStds()[0] + "," +
    // getStds()[1] + " n = " + nNumbers);
  }
}
