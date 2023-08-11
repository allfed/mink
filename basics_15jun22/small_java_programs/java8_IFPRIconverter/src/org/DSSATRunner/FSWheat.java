package org.DSSATRunner;

public class FSWheat implements NitrogenOnlyFertilizerScheme {

  private boolean readyToCreateBlock = false;

  private String initialFertilizerType = null;
  private String initialFertilizerApplicationMethod = null;
  private int initialFertilizerDepth = -2;

  private String subsequentFertilizerType = null;
  private String subsequentFertilizerApplicationMethod = null;
  private int subsequentFertilizerDepth = -3;

  public FSWheat() {
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
    int maxNumberOfApplications = 5;
    // screwed up previously; noticed on 17jul12
    //    int dapFertilizerRegimeBegins   = Math.max(daysAfterPlantingMaturity-10, 10);
    //    int dapFertilizerRegimeFinishes = Math.max(daysAfterPlantingAnthesis-10, 10);
    int dapFertilizerRegimeFinishes = Math.max(daysAfterPlantingMaturity - 10, 10);
    int dapFertilizerRegimeBegins = Math.max(daysAfterPlantingAnthesis - 10, 10);

    /////////////////////
    // initializations //
    /////////////////////
    int[] daysAfterPlantingForApplication = new int[maxNumberOfApplications]; // jawoo's column 0
    int[] rateAppliedKgPerHa = new int[maxNumberOfApplications]; // jawoo's column 1

    int afterInitialRate = -1;
    //  	int afterInitialInterval = -2;
    //  	double nApplicationsAfterInitial = -3;
    //  	int totalAppliedSoFar = -4;
    String outString = "";

    ////////////////////////////////////////
    // the particular rules for this case //
    ////////////////////////////////////////

    if (nitrogenFertilizerAmount < 30) {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = (int) nitrogenFertilizerAmount;

    } else if (nitrogenFertilizerAmount < 100) {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 10;

      afterInitialRate = (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / 3.0);

      daysAfterPlantingForApplication[1] = dapFertilizerRegimeBegins;
      rateAppliedKgPerHa[1] = afterInitialRate;

      daysAfterPlantingForApplication[2] = dapFertilizerRegimeFinishes;
      rateAppliedKgPerHa[2] =
          (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0] - rateAppliedKgPerHa[1]));

    } else if (nitrogenFertilizerAmount < 200) {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 30;

      afterInitialRate = (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / 3.0);

      daysAfterPlantingForApplication[1] = dapFertilizerRegimeBegins;
      rateAppliedKgPerHa[1] = afterInitialRate;

      daysAfterPlantingForApplication[2] = dapFertilizerRegimeFinishes;
      rateAppliedKgPerHa[2] =
          (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0] - rateAppliedKgPerHa[1]));

    } else {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = (int) (nitrogenFertilizerAmount * 0.25);

      afterInitialRate = (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / 4.0);

      daysAfterPlantingForApplication[1] = dapFertilizerRegimeBegins;
      rateAppliedKgPerHa[1] = afterInitialRate;

      daysAfterPlantingForApplication[2] =
          (int) ((dapFertilizerRegimeBegins + dapFertilizerRegimeFinishes) / 2.0);
      rateAppliedKgPerHa[2] = afterInitialRate;

      daysAfterPlantingForApplication[3] = dapFertilizerRegimeFinishes;
      rateAppliedKgPerHa[3] =
          (int)
              ((nitrogenFertilizerAmount
                  - rateAppliedKgPerHa[0]
                  - rateAppliedKgPerHa[1]
                  - rateAppliedKgPerHa[2]));
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
