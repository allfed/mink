package org.DSSATRunner;

public class FSWinterWheat implements NitrogenOnlyFertilizerScheme {

  private boolean readyToCreateBlock = false;

  private String initialFertilizerType = null;
  private String initialFertilizerApplicationMethod = null;
  private int initialFertilizerDepth = -2;

  private String subsequentFertilizerType = null;
  private String subsequentFertilizerApplicationMethod = null;
  private int subsequentFertilizerDepth = -3;

  public FSWinterWheat() {
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
    ////    int dapFertilizerRegimeBegins   = Math.max(daysAfterPlantingMaturity-10, 10);
    ////    int dapFertilizerRegimeFinishes = Math.max(daysAfterPlantingAnthesis-10, 10);

    //		int dapFertilizerRegimeFinishes = Math.max(daysAfterPlantingMaturity-10, 10);
    //    int dapFertilizerRegimeBegins   = Math.max(daysAfterPlantingAnthesis-10, 10);

    /////////////////////
    // initializations //
    /////////////////////
    int[] daysAfterPlantingForApplication = new int[maxNumberOfApplications]; // jawoo's column 0
    int[] rateAppliedKgPerHa = new int[maxNumberOfApplications]; // jawoo's column 1

    //  	int afterInitialRate = -1;
    //  	int afterInitialInterval = -2;
    //  	double nApplicationsAfterInitial = -3;
    //  	int totalAppliedSoFar = -4;
    String outString = "";

    ////////////////////////////////////////
    // the particular rules for this case //
    ////////////////////////////////////////

    // the theory is that all the fertilizer should be put on *before* flowering
    // i'd also like to make this work with spring wheat, even if it isn't perfectly accurate
    // that means that we need to base the application time on relative/fractional intervals rather
    // than absolute delays
    //
    // now, the simple case i'm working off of is plant in september, and put additional fertilizer
    // on in february and early april and possibly early may
    // also, flowering (in this case) is anticipated around may with harvest in june/july
    //
    // thinking in terms of days. total maturity length is around 270 or 290 days; anthesis at about
    // 220 or 240 days.
    // february would occur at about 150 days and april at about 210 days
    //
    // so, then we could figure out the fraction of the way between planting and anthesis as say
    double fractionToSecondApplication = 150.0 / 230;
    double fractionToThirdApplication = 210.0 / 230;

    if (daysAfterPlantingAnthesis < 3) {
      System.out.println(
          "FSWinterWheat: unlikely flowering: "
              + daysAfterPlantingAnthesis
              + "; resetting to 3 for lack of better option");
      daysAfterPlantingAnthesis = 3;
      //  		throw new Exception();
    }
    int daysSecondApplication =
        (int) Math.round(fractionToSecondApplication * daysAfterPlantingAnthesis);
    int daysThirdApplication =
        (int) Math.round(fractionToThirdApplication * daysAfterPlantingAnthesis);

    if (nitrogenFertilizerAmount <= 30) {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = (int) nitrogenFertilizerAmount;
      maxNumberOfApplications = 1;
    } else if (nitrogenFertilizerAmount <= 100) {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 30;

      daysAfterPlantingForApplication[1] = daysSecondApplication;
      rateAppliedKgPerHa[1] = (int) (nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / 2;

      daysAfterPlantingForApplication[2] = daysThirdApplication;
      rateAppliedKgPerHa[2] =
          (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0] - rateAppliedKgPerHa[1]));
      maxNumberOfApplications = 3;

    } else {

      daysAfterPlantingForApplication[0] = 1;
      rateAppliedKgPerHa[0] = 80;

      daysAfterPlantingForApplication[1] = daysSecondApplication;
      rateAppliedKgPerHa[1] = (int) (nitrogenFertilizerAmount - rateAppliedKgPerHa[0]) / 2;

      daysAfterPlantingForApplication[2] = daysThirdApplication;
      rateAppliedKgPerHa[2] =
          (int) ((nitrogenFertilizerAmount - rateAppliedKgPerHa[0] - rateAppliedKgPerHa[1]));
      maxNumberOfApplications = 3;
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
      //			System.out.println("aI = " + applicationIndex + "; mNOA = " + maxNumberOfApplications);
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
