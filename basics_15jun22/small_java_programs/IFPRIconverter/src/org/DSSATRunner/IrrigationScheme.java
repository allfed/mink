package org.DSSATRunner;

public interface IrrigationScheme {


	
	public abstract void initialize();
	
	public abstract void initialize(
			int plantingDate,
			int nYears
			);

	public abstract String buildIrrigationBlock() throws Exception;
	
	

}