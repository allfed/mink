package org.DSSATRunner;

public class IrriSNone implements IrrigationScheme {

  private boolean readyToCreateBlock = false;

  public IrriSNone() {
    readyToCreateBlock = false;
  }

  public void initialize() {
    initialize(3, 1);
  }

  private int plantingDate = -6782;

  public void initialize(int providedPlantingDate, int nYears) {
    plantingDate = providedPlantingDate;

    readyToCreateBlock = true;
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
  public void specialPurposeMethod(Object[] inputObjectArray) {}

  public String buildIrrigationBlock() throws Exception {

    if (!readyToCreateBlock) {
      System.out.println(this.toString() + ": not initialized");
      throw new Exception();
    }

    int threeBefore = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, -3);

    String irriBlock = "*IRRIGATION AND WATER MANAGEMENT" + "\n";
    irriBlock += "@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME" + "\n";
    irriBlock += " 1   -99   -99   -99   -99   -99   -99     1 DummyPlaceholder" + "\n";
    irriBlock += "@I IDATE  IROP IRVAL" + "\n";
    irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore, 5) + " IR001   ZZZ" + "\n";

    return irriBlock;
  }
}
