package org.DSSATRunner;

public class IrriSRiceDepth050mm implements IrrigationScheme {

	private boolean	readyToCreateBlock	= false;

	public IrriSRiceDepth050mm() {
		readyToCreateBlock = false;
	}

	public void initialize() {
		initialize(3,1);
	}

	private int	plantingDate	= -6782;
	private int	nYears	= -67862;

	public void initialize(int providedPlantingDate, int nYears) {
		plantingDate = providedPlantingDate;
		this.nYears = nYears;

		readyToCreateBlock = true;
	}
	
	public void specialPurposeMethod(Object[] inputObjectArray) {
	    
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

	private double fixedFloodDepth = 50.0; // mm; this is to try out different depths to see if we can get different water usages and yields.
	private String fixedFloodDepthString = DSSATHelperMethods.padWithZeros(fixedFloodDepth, 5);
	
	public String buildIrrigationBlock() throws Exception {

		if (!readyToCreateBlock) {
			System.out.println(this.toString() + ": not initialized");
			throw new Exception();
		}

		int threeBefore = -1;
		int threeAfter  = -2;
		int sevenAfter  = -3;
		int endOfSeason = -5;

		int yearlyDayOffset = -4;
		
		String irriBlock = "*IRRIGATION AND WATER MANAGEMENT" + "\n";
		irriBlock += "@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME" + "\n";
		irriBlock += " 1   -99   -99   -99   -99   -99   -99     1 UNKNOWN" + "\n";
		irriBlock += "@I IDATE  IROP IRVAL" + "\n";
		
		for (int yearIndex = 0; yearIndex < nYears; yearIndex++) {

			yearlyDayOffset = yearIndex * 365;

			threeBefore = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate, -3 + yearlyDayOffset);
			threeAfter  = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate,  3 + yearlyDayOffset);
			sevenAfter  = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate,  7 + yearlyDayOffset);
			endOfSeason = DSSATHelperMethods.yyyyDDDaddDaysIgnoreLeap(plantingDate,  230 + yearlyDayOffset);

			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR011   5.0" + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR009  20.0" + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR008   2.0" + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeBefore,5) + " IR010   0.0" + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeAfter,5)  + " IR011 " + fixedFloodDepthString  + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(threeAfter,5)  + " IR009 100.0" + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(sevenAfter,5)  + " IR011 " + fixedFloodDepthString  + "\n";
			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(sevenAfter,5)  + " IR009 150.0" + "\n";

			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(endOfSeason,5) + " IR011   0.0" + "\n";
//			irriBlock += " 1 " + DSSATHelperMethods.padWithZeros(endOfSeason,5) + " IR009   0.0" + "\n";

		}

		return irriBlock;

	}

}