package org.DSSATRunner;

public interface NitrogenOnlyFertilizerScheme {

//	private int nApplications;
//	private int[] timeForApplication = null; // jawoo's column 0
//	private int[] rateApplied = null; // jawoo's column 1

	
	public abstract void initialize();
	
	public abstract void initialize(
			String initialFertilizerType,
			String initialFertilizerApplicationMethod,
			int initialFertilizerDepth,
			
			String subsequentFertilizerType,
			String subsequentFertilizerApplicationMethod,
			int subsequentFertilizerDepth
			);

	public abstract String buildNitrogenOnlyBlock(int daysAfterPlantingAnthesis,
			int daysAfterPlantingMaturity, double nitrogenFertilizerAmount) throws Exception;
	
	

}