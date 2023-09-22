package org.DSSATRunner;

public class IrriSRiceSomeDrySpellsA implements IrrigationScheme {

  private boolean readyToCreateBlock = false;

  public IrriSRiceSomeDrySpellsA() {
    readyToCreateBlock = false;
  }

  public void initialize() {
    initialize(3, 1);
  }

  private int plantingDate = -6782;
  private int nYears = -67862;

  // magic numbers
  int oneAfterShift = 1;
  int threeBeforeShift = -3;
  int threeAfterShift = 3;
  int sevenAfterShift = 7;
  int endOfSeasonShift = 230;

  private int[][] daysColOneDepthColTwoFloodArray = null;

  public void initialize(int providedPlantingDate, int nYears) {
    plantingDate = providedPlantingDate;
    this.nYears = nYears;

    if (daysColOneDepthColTwoFloodArray == null) {
      readyToCreateBlock = false;
    } else {
      readyToCreateBlock = true;
    }
  }

  /*

   *PLANTING DETAILS
  @P PDATE EDATE  PPOP  PPOE  PLME  PLDS  PLRS  PLRD  PLDP  PLWT  PAGE  PENV  PLPH  SPRL                        PLNAME
   1 85035   -99  75.0  25.0     T     H    20     0   2.0     0    23  25.0   3.0   0.0                        UNKNOWN

   *IRRIGATION AND WATER MANAGEMENT
  @I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME
   1   -99   -99   -99   -99   -99   -99     1 UNKNOWN
  @I IDATE  IROP IRVAL
   1 85032 IR011   5.0 -> 3 days before planting
   1 85032 IR009  20.0
   1 85032 IR008   2.0
   1 85032 IR010   0.0
   1 85038 IR011  30.0 -> 3 days after planting
   1 85038 IR009 100.0
   1 85042 IR011  50.0 -> 7 days after planting
   1 85042 IR009 150.0

   */

  public void specialPurposeMethod(Object[] inputObjectArray) throws Exception {
    // assume that this is a single element object array and take the first one...
    //	    int[][] daysColOneDepthColTwoFloodDefinitions = (int[][])inputObjectArray[0];

    daysColOneDepthColTwoFloodArray = (int[][]) inputObjectArray[0];

    // idiot checking?
    // we want to make sure that
    // * the reported days are in order from least to greatest
    // * the days are after the "three after" and before the end of the season
    // * the depths are non-negative

    int previousDay = -1;
    int day = -2;
    int depth = -3;

    for (int eventIndex = 1; eventIndex < daysColOneDepthColTwoFloodArray.length; eventIndex++) {
      day = daysColOneDepthColTwoFloodArray[eventIndex][0];
      depth = daysColOneDepthColTwoFloodArray[eventIndex][1];

      if (day <= previousDay) {
        System.out.println(
            "flexible rice IR scheme day out of order: event index = "
                + eventIndex
                + "; day = "
                + day
                + "; previousDay = "
                + previousDay);
        throw new Exception();
      }

      if (day < oneAfterShift) {
        System.out.println(
            "flexible rice IR scheme day too early: event index = "
                + eventIndex
                + "; day = "
                + day
                + "; must be on or after  "
                + oneAfterShift);
        throw new Exception();
      }

      if (day >= endOfSeasonShift) {
        System.out.println(
            "flexible rice IR scheme day too late: event index = "
                + eventIndex
                + "; day = "
                + day
                + "; must be before  "
                + endOfSeasonShift);
        throw new Exception();
      }

      if (depth < 0) {
        System.out.println(
            "flexible rice IR scheme depth too low: event index = "
                + eventIndex
                + "; depth = "
                + depth
                + "; must be non-negative");
        throw new Exception();
      }
    }

    readyToCreateBlock = true;
  }

  //	private double fixedFloodDepth = 1.0; // mm; this is to try out different depths to see if we
  // can get different water usages and yields.
  //	private String fixedFloodDepthString = DSSATHelperMethods.padWithZeros(fixedFloodDepth, 5);

  public String buildIrrigationBlock() throws Exception {

    if (!readyToCreateBlock) {
      System.out.println(this.toString() + ": not initialized");
      System.out.println("daysColOneDepthColTwoFloodArray = " + daysColOneDepthColTwoFloodArray);
      throw new Exception();
    }

    int dayAfter = -6;
    //		int threeBefore = -1;
    //		int threeAfter  = -2;
    //		int sevenAfter  = -3;
    int endOfSeason = -5;

    int yearlyDayOffset = -4;

    int day = -3, eventDay;
    double depth = -4.0;
    String floodDepthString = null;

    String irriBlock = "*IRRIGATION AND WATER MANAGEMENT" + "\n";
    irriBlock += "@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME" + "\n";
    irriBlock += " 1   -99   -99   -99   -99   -99   -99     1 UNKNOWN" + "\n";
    irriBlock += "@I IDATE  IROP IRVAL" + "\n";

    double percolationRate =
        0.5; // IR008 mm/day; originally, i used 2mm, but i think it should be lower, so stealing
    // 0.5 from a standard X file
    double plowpanPuddlingDepth = 15; // IR010; dunno if this is actually read or not
    double initialBundHeight = 20.0; // IR009; mm
    double initialFloodDepth = 20.0; // IR003; mm
    double finalBundHeight = 150.0; // IR009; mm
    for (int yearIndex = 0; yearIndex < nYears; yearIndex++) {

      yearlyDayOffset = yearIndex * 365;

      dayAfter =
          DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(
              plantingDate, oneAfterShift + yearlyDayOffset);
      //			threeBefore = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, threeBeforeShift
      // + yearlyDayOffset);
      //			threeAfter  = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, threeAfterShift +
      // yearlyDayOffset);
      //			sevenAfter  = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, sevenAfterShift +
      // yearlyDayOffset);
      endOfSeason =
          DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(
              plantingDate, endOfSeasonShift + yearlyDayOffset);

      // the "unflooded" setup; similar, but not the same as what i am using for real
      irriBlock +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(plantingDate, 5)
              + " IR009 "
              + DSSATHelperMethods.padWithZeros(initialBundHeight, 5)
              + "\n";
      irriBlock +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(plantingDate, 5)
              + " IR003 "
              + DSSATHelperMethods.padWithZeros(initialFloodDepth, 5)
              + "\n";
      irriBlock +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(plantingDate, 5)
              + " IR010 "
              + DSSATHelperMethods.padWithZeros(plowpanPuddlingDepth, 5)
              + "\n";
      irriBlock +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(plantingDate, 5)
              + " IR008 "
              + DSSATHelperMethods.padWithZeros(percolationRate, 5)
              + "\n";
      irriBlock +=
          " 1 "
              + DSSATHelperMethods.padWithZeros(dayAfter, 5)
              + " IR009 "
              + DSSATHelperMethods.padWithZeros(finalBundHeight, 5)
              + "\n";

      // the logic here is that we have enough water to soak the paddy for a few days around
      // planting
      // forget that for the moment: i want this custom one to be able to reduce to unflooded
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR011   5.0" +
      // "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR009  20.0" +
      // "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR008   2.0" +
      // "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR010   0.0" +
      // "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeAfter,5)  + " IR009 100.0" +
      // "\n";

      for (int eventIndex = 0; eventIndex < daysColOneDepthColTwoFloodArray.length; eventIndex++) {
        day = daysColOneDepthColTwoFloodArray[eventIndex][0];
        depth = daysColOneDepthColTwoFloodArray[eventIndex][1];

        eventDay = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, day + yearlyDayOffset);

        floodDepthString = DSSATHelperMethods.padWithZeros(depth, 5);

        irriBlock +=
            " 1 "
                + DSSATHelperMethods.padWithZeros(eventDay, 5)
                + " IR011 "
                + floodDepthString
                + "\n";

        //			    System.out.println("[" + eventIndex + "][0] = " +
        // daysColOneDepthColTwoFloodArray[eventIndex][0]);
        //			    System.out.println("[" + eventIndex + "][1] = " +
        // daysColOneDepthColTwoFloodArray[eventIndex][1]);
      }

      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(sevenAfter,5)  + " IR011 " +
      // floodDepthString  + "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(sevenAfter,5)  + " IR009 150.0" +
      // "\n";

      irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(endOfSeason, 5) + " IR011   0.0" + "\n";
      //			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(endOfSeason,5) + " IR009   0.0" +
      // "\n";

    }

    return irriBlock;
  }
}
