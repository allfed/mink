package org.DSSATRunner;

public class FSPotatoes3 implements NitrogenOnlyFertilizerScheme {

  private boolean readyToCreateBlock = false;

  private String initialFertilizerType = null;
  private String initialFertilizerApplicationMethod = null;
  private int initialFertilizerDepth = -2;

  private String subsequentFertilizerType = null;
  private String subsequentFertilizerApplicationMethod = null;
  private int subsequentFertilizerDepth = -3;

  public FSPotatoes3() {
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
      this.initialFertilizerApplicationMethod = "AP004"; // Banded beneath surface
    } else {
      this.initialFertilizerApplicationMethod = initialFertilizerApplicationMethod;
    }

    if (initialFertilizerDepth < 0) {
      this.initialFertilizerDepth = 5; // Depth = 5 cm
    } else {
      this.initialFertilizerDepth = initialFertilizerDepth;
    }

    if (subsequentFertilizerType == null) {
      this.subsequentFertilizerType = "FE005"; // Urea
    } else {
      this.subsequentFertilizerType = subsequentFertilizerType;
    }

    if (subsequentFertilizerApplicationMethod == null) {
      this.subsequentFertilizerApplicationMethod = "AP004"; // Banded beneath surface
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

    // the idea is that we will be splitting it into chunks of 100 kg/ha, separated by 20 day blocks
    // with the exception of the first two which will be at 15 and 40 days (then 60/80/100/etc).
    // we always want at least two applications.
    int maxNumberOfApplications = Math.max(2, (int) Math.ceil(nitrogenFertilizerAmount / 100));

    /////////////////////
    // initializations //
    /////////////////////
    int[] daysAfterPlantingForApplication = new int[maxNumberOfApplications]; // jawoo's column 0
    int[] rateAppliedKgPerHa = new int[maxNumberOfApplications]; // jawoo's column 1

    String outString = "";

    ////////////////////////////////////////
    // the particular rules for this case //
    ////////////////////////////////////////

    int maxFertilizerInOneApplication = 100;
    int fertilizerLeftToApply = (int) nitrogenFertilizerAmount;
    int storageIndex = -5;
    int daysBetweenApplications = 20;
    // check whether we have enough to go beyond two applications....
    if (nitrogenFertilizerAmount <= 2 * maxFertilizerInOneApplication) {
      daysAfterPlantingForApplication[0] = 15;
      rateAppliedKgPerHa[0] = (int) (nitrogenFertilizerAmount / 2);

      daysAfterPlantingForApplication[1] = 40;
      rateAppliedKgPerHa[1] = (int) nitrogenFertilizerAmount - rateAppliedKgPerHa[0];
    } else {
      // Beware the MAGIC NUMBER!!! we are doing 100 a piece until we run out
      daysAfterPlantingForApplication[0] = 15;
      rateAppliedKgPerHa[0] = maxFertilizerInOneApplication;
      fertilizerLeftToApply -= rateAppliedKgPerHa[0];

      daysAfterPlantingForApplication[1] = 40;
      rateAppliedKgPerHa[1] = maxFertilizerInOneApplication;
      fertilizerLeftToApply -= rateAppliedKgPerHa[1];

      // now, we need to count down to get rid of the rest...
      storageIndex = 2;
      while (fertilizerLeftToApply > 0) {
        daysAfterPlantingForApplication[storageIndex] =
            daysAfterPlantingForApplication[storageIndex - 1] + daysBetweenApplications;
        rateAppliedKgPerHa[storageIndex] =
            Math.min(maxFertilizerInOneApplication, fertilizerLeftToApply);
        fertilizerLeftToApply -= rateAppliedKgPerHa[storageIndex];

        // increment the storage index
        storageIndex++;
      }
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
