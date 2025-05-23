package org.DSSATRunner;

public class FSRice implements NitrogenOnlyFertilizerScheme {

  private boolean readyToCreateBlock = false;

  private String initialFertilizerType = null;
  private String initialFertilizerApplicationMethod = null;
  private int initialFertilizerDepth = -2;

  private String subsequentFertilizerType = null;
  private String subsequentFertilizerApplicationMethod = null;
  private int subsequentFertilizerDepth = -3;

  public FSRice() {
    readyToCreateBlock = false;
  }

  public void initialize() {
    initialize(null, null, -1, null, null, -1);
  }

  public void initialize(
      String initialFertilizerType,
      String initialFertilizerApplicationMethod,
      int initialFertilizerDepth,
      String subsequentFertilizerType,
      String subsequentFertilizerApplicationMethod,
      int subsequentFertilizerDepth) {

    // nulls and negative values mean use default....
    // Beware the MAGIC NUMBERS!!!
    if (initialFertilizerType == null) {
      this.initialFertilizerType = "FE005"; // Urea
    } else {
      this.initialFertilizerType = initialFertilizerType;
    }

    if (initialFertilizerApplicationMethod == null) {
      this.initialFertilizerApplicationMethod = "AP001"; // Broadcase, not incorporated
    } else {
      this.initialFertilizerApplicationMethod = initialFertilizerApplicationMethod;
    }

    if (initialFertilizerDepth < 0) {
      this.initialFertilizerDepth = 1; // Depth = 1 cm
    } else {
      this.initialFertilizerDepth = initialFertilizerDepth;
    }

    if (subsequentFertilizerType == null) {
      this.subsequentFertilizerType = "FE005"; // Urea
    } else {
      this.subsequentFertilizerType = subsequentFertilizerType;
    }

    if (subsequentFertilizerApplicationMethod == null) {
      this.subsequentFertilizerApplicationMethod = "AP002"; // Broadcast, incorporated
    } else {
      this.subsequentFertilizerApplicationMethod = subsequentFertilizerApplicationMethod;
    }

    if (subsequentFertilizerDepth < 0) {
      this.subsequentFertilizerDepth = 5; // depth = 5 cm
    } else {
      this.subsequentFertilizerDepth = subsequentFertilizerDepth;
    }

    readyToCreateBlock = true;
  }

  public String buildNitrogenOnlyBlock(
      int daysAfterPlantingAnthesis, int daysAfterPlantingMaturity, double nitrogenFertilizerAmount)
      throws Exception {

    if (!readyToCreateBlock) {
      System.out.println(this.toString() + ": not initialized");
      throw new Exception();
    }

    ///////////////////////////
    // parameter definitions //
    ///////////////////////////
    int maxNumberOfApplications = 3;
    int dapFertilizerRegimeBegins = 10;
    int dapFertilizerRegimeFinishes = Math.max(daysAfterPlantingAnthesis - 10, 10);

    /////////////////////
    // initializations //
    /////////////////////
    int[] daysAfterPlantingForApplication = new int[maxNumberOfApplications]; // jawoo's column 0
    int[] rateAppliedKgPerHa = new int[maxNumberOfApplications]; // jawoo's column 1

    int afterInitialRate = -1;
    int afterInitialInterval = -2;
    double nApplicationsAfterInitial = -3;
    //  	int totalAppliedSoFar = -4;
    String outString = "";

    ////////////////////////////////////////
    // the particular rules for this case //
    ////////////////////////////////////////

    if (nitrogenFertilizerAmount < 0) {
      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 0;
    } else if (nitrogenFertilizerAmount < 20) {
      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = (int) nitrogenFertilizerAmount;
    } else if (nitrogenFertilizerAmount < 100) {

      nApplicationsAfterInitial = 1;
      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = (int) (nitrogenFertilizerAmount * 0.4);

      // apply rest halfway through dumping period
      daysAfterPlantingForApplication[1] = dapFertilizerRegimeFinishes;
      rateAppliedKgPerHa[1] = (int) nitrogenFertilizerAmount - rateAppliedKgPerHa[0];

    } else {
      nApplicationsAfterInitial = 2;

      // same as the 200, just spread across 4 applications....
      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 60;

      afterInitialRate =
          (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / nApplicationsAfterInitial);
      afterInitialInterval =
          (int)
              Math.max(
                  (dapFertilizerRegimeFinishes - dapFertilizerRegimeBegins)
                      / nApplicationsAfterInitial,
                  0.0);

      // all but the last one, so we can make the last one match up nicely
      daysAfterPlantingForApplication[1] =
          daysAfterPlantingForApplication[0] + afterInitialInterval;
      rateAppliedKgPerHa[1] = afterInitialRate;

      daysAfterPlantingForApplication[2] = dapFertilizerRegimeFinishes;
      rateAppliedKgPerHa[2] =
          (int) (nitrogenFertilizerAmount - rateAppliedKgPerHa[0] - rateAppliedKgPerHa[1]);
    }

    ////////////////////////////////////////////////////////////////
    // build up the string representing the fertilizer rule block //
    ////////////////////////////////////////////////////////////////

    // the header lines
    outString = "*FERTILIZERS (INORGANIC)\n";
    outString += "@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD\n";

    // now, brute force the remaining applications
    // do the initial one first, then use a loop for the rest...
    outString +=
        " 1 "
            + DSSATHelperMethods.padWithZeros(daysAfterPlantingForApplication[0], 5)
            + " "
            + this.initialFertilizerType
            + " "
            + this.initialFertilizerApplicationMethod
            + " "
            + DSSATHelperMethods.padWithZeros(this.initialFertilizerDepth, 5)
            + " "
            + DSSATHelperMethods.padWithZeros(rateAppliedKgPerHa[0], 5);

    // ok, now the non nitrogen stuff which we are putting in a missing value for.
    outString += "   -99   -99   -99   -99   -99\n";

    for (int applicationIndex = 1; applicationIndex < maxNumberOfApplications; applicationIndex++) {
      // the nitrogen stuff...
      outString +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(
                  daysAfterPlantingForApplication[applicationIndex], 5)
              + " "
              + this.subsequentFertilizerType
              + " "
              + this.subsequentFertilizerApplicationMethod
              + " "
              + DSSATHelperMethods.padWithZeros(this.subsequentFertilizerDepth, 5)
              + " "
              + DSSATHelperMethods.padWithZeros(rateAppliedKgPerHa[applicationIndex], 5);

      // ok, now the non nitrogen stuff which we are putting in a missing value for.
      outString += "   -99   -99   -99   -99   -99\n";
    }

    // tack on a final newline just to make it look nice
    outString += "\n";

    return outString;
  }
}
