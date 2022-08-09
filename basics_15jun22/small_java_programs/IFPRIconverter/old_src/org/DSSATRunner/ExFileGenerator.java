package org.DSSATRunner;

import org.R2Useful.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Vector;
import java.sql.Statement;
import java.sql.ResultSet;

public class ExFileGenerator {

	DecimalFormat dfYY = new DecimalFormat("00");
	
    public boolean exFileGenerator(
    		int rid,
    		int nodeID,
    		int threadID,
    		java.sql.Statement stmt, 
    		ExeRunner er,
    		TextReader tr,
    		TextWriter tw,
    		String modelName,
    		int outputFrequency,
    		boolean weatherOutput,
    		boolean waterOutput,
    		boolean annOutput,
    		boolean adbOutput,
    		boolean growthOutput,
    		boolean etOutput,
    		boolean wdiOutput,
    		String spatialResolution,
    	   	int leadingDays,
    	   	String harvestWhen,
    	   	Scenario scn,
    	   	boolean automaticPlanting,
    	   	int plantingDatePotential,
    	   	int anthesisDatePotential,
    	   	int maturityDatePotential
    	   	)
    throws FileNotFoundException, IOException
    {
        
    	// Run this?
    	boolean runThis = true;
    	
        // Field info
    	int cid                      	= scn.getCellID();
    	int yyyy						= scn.getYYYY();
    	int nmyr						= scn.getNMYR();
    	int fiClimateScenario			= scn.getClimateScenario();
    	String fiClimateRandomSeed		= scn.getClimateRandomSeed();
    	String fiSoilProfile  			= scn.getSoilProfile();
    	String fiPlantingFirst 			= scn.getPlantingFirst();
    	String fiPlantingLast  			= scn.getPlantingLast();
    	int fiPlantingDensity			= scn.getPlantingDensity();
    	String fiPlantingMethod			= scn.getPlantingMethod();
    	int fiPlantingSpacing			= scn.getPlantingSpacing();
    	String fiCropCode				= scn.getCropCode();
    	String fiCultivarCode			= scn.getCultivarCode();
    	int leafDamagePercentage		= scn.getLeafDamage();
    	int fiNitrogenFertilizerRate	= scn.getNitrogenFertilizerRate();
    	int ithl						= scn.getIrrigationThreshold();
    	int imax						= scn.getIrrigationMaximum();
    	int ival						= scn.getIrrigationValue();
    	String emCO2					= scn.getEmCO2Code();
    	String pathToWorkspace          = "./thread_"+threadID+"/";
    	
        // crX template file
    	String xFileTemplateName = Simplr.xFileName[0]+Simplr.xFileName[1]+Simplr.xFileName[2];
        String xNext = tr.textReader(pathToWorkspace+xFileTemplateName);

        // dv4 template file
    	String bFileTemplateName = Simplr.bFileName[0]+Simplr.bFileName[1]+Simplr.bFileName[2];
        String bNext = tr.textReader(pathToWorkspace+bFileTemplateName);

        // RunID, CellID
    	xNext = xNext.replaceFirst("_RunID_", String.valueOf(rid));
    	xNext = xNext.replaceFirst("_CellID_", String.valueOf(cid));
        
		// Years in two-digit
		String strYY = "";                   		
		if (yyyy<2000) strYY = dfYY.format(yyyy-1900);
		else strYY = dfYY.format(yyyy-2000);

		// Next year..
		String strYZ = "";
		int yyyz = yyyy + 1;
		if (yyyz<2000) strYZ = dfYY.format(yyyz-1900);
		else strYZ = dfYY.format(yyyz-2000);
		
		// Planting of crop
		int pFirstInt = Integer.parseInt(fiPlantingFirst.substring(2,5));
		
        // Leaf damage on 60 days after planting
        if (leafDamagePercentage>0)
        {
        	xNext = xNext.replaceFirst("_DI01", "    Y");
        	DecimalFormat dfLDP = new DecimalFormat("000.0");
        	String tNext = tr.textReader(pathToWorkspace+Simplr.tFileName[0]+Simplr.tFileName[1]+Simplr.tFileName[2]);
            String leafDamageDate_pd01 = checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 60);
            String leafDamageDate_pd02 = checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 61);
            tNext = tNext.replaceAll("_PD01", leafDamageDate_pd01);
            tNext = tNext.replaceAll("_PD02", leafDamageDate_pd02);
            tNext = tNext.replaceAll("_PL01", dfLDP.format(leafDamagePercentage));
            try
            {
            	tw.textWriter(pathToWorkspace, Simplr.tFileName[0]+"."+fiCropCode+"T", tNext);
            }
            catch (Exception ex) { ex.printStackTrace(); }
        }
        else
        {
        	xNext = xNext.replaceAll("_DI01", "    N");
        }
		
		// Initial condition date (1 month before planting)
		String strInitialConditionYY = strYY;
		int initialConditionInt = pFirstInt - leadingDays;
		if (initialConditionInt==0) initialConditionInt = 1;
		if (initialConditionInt<0)
		{
			initialConditionInt = initialConditionInt + 365;
			strInitialConditionYY = dfYY.format(Integer.parseInt(strYY)-1);
		}
		if (Integer.parseInt(strInitialConditionYY)<0)
		{
			strInitialConditionYY = String.valueOf(Integer.parseInt(strInitialConditionYY)+100);
		}
		DecimalFormat dfDDD = new DecimalFormat("000");         
		String strInitialConditionDate = strInitialConditionYY+dfDDD.format(initialConditionInt);
		
        // Year
    	xNext = xNext.replaceAll("_Y", strYY);
    	xNext = xNext.replaceAll("_Z", strYZ);

        // Initial Condition Date, Simulation Start Date
    	xNext = xNext.replaceAll("_IC01", strInitialConditionDate);
        
        // Climate or Weather
        if (String.valueOf(fiClimateScenario).equals("0"))
        {
        	xNext = xNext.replaceAll("_WT01", "    M");
        	xNext = xNext.replaceAll("_WTHCODE", "WEATHERS");
        }
        else
        {
        	xNext = xNext.replaceAll("_WT01", "    S");
        	xNext = xNext.replaceAll("_WTHCODE", "CLIM    ");
        }
        DecimalFormat dfRDSD = new DecimalFormat("00000");
        if (Integer.parseInt(fiClimateRandomSeed)==0) fiClimateRandomSeed = "81384";
        xNext = xNext.replaceAll("_RS01", dfRDSD.format(Integer.parseInt(fiClimateRandomSeed)));
        
        // Output frequency
       	xNext = xNext.replaceAll("_FO01", dfRDSD.format(outputFrequency));
        
       	// ANN output?
       	if (annOutput)
        {
        	xNext = xNext.replaceAll("_GO01", "    Y");
        	xNext = xNext.replaceAll("_WO01", "    Y");
        }
       	
       	// ADB output?
       	if (adbOutput)
        {
        	xNext = xNext.replaceAll("_GO01", "    N");
        	xNext = xNext.replaceAll("_WO01", "    N");
        }

       	// WDI output?
       	if (wdiOutput)
        {
        	xNext = xNext.replaceAll("_GO01", "    Y");
        	xNext = xNext.replaceAll("_WO01", "    Y");
        }
       	
        // Weather or growth output?
        if (weatherOutput || growthOutput)
        {
        	xNext = xNext.replaceAll("_GO01", "    Y");
        }
        else
        {
        	xNext = xNext.replaceAll("_GO01", "    N");
        }
        
        // ET output?
        if (etOutput || waterOutput)
        {
        	xNext = xNext.replaceAll("_WO01", "    Y");
        }
        else
        {
        	xNext = xNext.replaceAll("_WO01", "    N");
        }
        
        // Cultivar code
        xNext = xNext.replaceAll("_C", fiCropCode);
        bNext = bNext.replaceAll("_.__", threadID+"."+fiCropCode);
        xNext = xNext.replaceAll("_ING01", fiCultivarCode);
        
        // Soil profile
        xNext = xNext.replaceAll("_SOILCODE_", fiSoilProfile);
        
        // Soil properties
        Vector<Object> soilProperties = getSoilProperties(stmt, fiSoilProfile);
        
        // Soil initial condition
        DecimalFormat dfICBL = new DecimalFormat("0000");
        DecimalFormat dfINIT = new DecimalFormat("00.00");
        int nmslb = soilProperties.size(); 
        for (int d=0; d<nmslb; d++)
        {
        	double[] s = (double[])soilProperties.elementAt(d);
        	double sw = 0.0; 
        	if (Simplr.initialAvailableWater>0) sw = s[1] + ( (s[2]-s[1])*Simplr.initialAvailableWater );
        	xNext = xNext.replaceFirst("L"+(d+1), " "+(d+1));
        	xNext = xNext.replaceFirst("_BL"+(d+1), dfICBL.format(s[0]));
        	xNext = xNext.replaceFirst("_H2O"+(d+1), dfINIT.format(sw));
        	xNext = xNext.replaceFirst("_NH4"+(d+1), dfINIT.format(Simplr.initialAvailableNH4));
        	xNext = xNext.replaceFirst("_NO3"+(d+1), dfINIT.format(Simplr.initialAvailableNO3));
        }
        
        // Soil initial condition: Cleaning for unused layers
        if (nmslb<9)
        {
        	for (int d=nmslb+1; d<=9; d++) 
        	{
        		xNext = xNext.replaceFirst("L"+d, "  ");
        		xNext = xNext.replaceFirst("_BL"+d, "    ");
        		xNext = xNext.replaceFirst("_H2O"+d, "     ");
        		xNext = xNext.replaceFirst("_NH4"+d, "     ");
        		xNext = xNext.replaceFirst("_NO3"+d, "     ");
        	}
        }
        
        // N fertilizer
        DecimalFormat dfFAMN = new DecimalFormat("00000");
        
        // When was the flowering date?
        if (String.valueOf(fiNitrogenFertilizerRate).equals("-99"))
        {
        	xNext = xNext.replaceAll("_NI01", "    N");
        	xNext = xNext.replaceFirst("MF1", "  0");
        }
        else
        {
        	xNext = xNext.replaceAll("_NI01", "    Y");
        	xNext = xNext.replaceFirst("MF1", "  1");
            double   nitrogenFertilizerAmount = (double)fiNitrogenFertilizerRate;

            // Flowering date?
            int dapAnthesis = 100;
            if (anthesisDatePotential>0)
            {
                dapAnthesis = Integer.parseInt(String.valueOf(anthesisDatePotential).substring(4)) 
							- Integer.parseInt(String.valueOf(plantingDatePotential).substring(4)); 
                if (dapAnthesis<0) dapAnthesis = dapAnthesis + 365;
            }
            
            // Maturity date?
            int dapMaturity = 100;
            if (maturityDatePotential>0)
            {
            	dapMaturity = Integer.parseInt(String.valueOf(maturityDatePotential).substring(4)) 
							- Integer.parseInt(String.valueOf(plantingDatePotential).substring(4)); 
                if (dapMaturity<0) dapMaturity = dapMaturity + 365;
            }
    
            // Regime
            int[][] nfrt = getFertilizerRegime(fiCropCode, nitrogenFertilizerAmount, dapAnthesis, dapMaturity);
            
            // First application right after planting
            xNext = xNext.replaceAll("_FE01", "FE005");		// Urea
            xNext = xNext.replaceAll("_FA01", "AP001");		// Broadcast, not incorporated
            xNext = xNext.replaceAll("_FD01", "    1");		// Depth, 1 cm
            
            // Subsequent applications banded on surface
            xNext = xNext.replaceAll("_FE02", "FE005");		// Urea
            xNext = xNext.replaceAll("_FA02", "AP002");		// Broadcast, incorporated        
            xNext = xNext.replaceAll("_FD02", "    5");		// Depth, 5 cm

            // Applications
            xNext = xNext.replaceAll("_FT01", dfFAMN.format(nfrt[0][0]));
            xNext = xNext.replaceAll("_FR01", dfFAMN.format(nfrt[0][1]));
            xNext = xNext.replaceAll("_FT02", dfFAMN.format(nfrt[1][0]));
            xNext = xNext.replaceAll("_FR02", dfFAMN.format(nfrt[1][1]));
            xNext = xNext.replaceAll("_FT03", dfFAMN.format(nfrt[2][0]));
            xNext = xNext.replaceAll("_FR03", dfFAMN.format(nfrt[2][1]));
            xNext = xNext.replaceAll("_FT04", dfFAMN.format(nfrt[3][0]));
            xNext = xNext.replaceAll("_FR04", dfFAMN.format(nfrt[3][1]));
            xNext = xNext.replaceAll("_FT05", dfFAMN.format(nfrt[4][0]));
            xNext = xNext.replaceAll("_FR05", dfFAMN.format(nfrt[4][1]));
            
        }
        
        // Irrigation
        DecimalFormat dfITHRL = new DecimalFormat("000");
        if (ithl==-99) // Unlimited water application
        {
        	xNext = xNext.replaceFirst("MI1", "  0");
        	xNext = xNext.replaceAll("_WA01", "    N");
        	xNext = xNext.replaceAll("_IR01", "    N");
        	xNext = xNext.replaceAll("_IT01", "  100");
        	xNext = xNext.replaceAll("_IM01", "  100");
        }
        else if (ithl==-10) // No irrigation
        {
        	xNext = xNext.replaceFirst("MI1", "  0");
        	xNext = xNext.replaceAll("_WA01", "    Y");
        	xNext = xNext.replaceAll("_IR01", "    N");
        	xNext = xNext.replaceAll("_IT01", "    0");
        	xNext = xNext.replaceAll("_IM01", "    0");
        }
        else if (ival>0) // Supplementary irrigation to make the fertilizer better mobilized in soils
        {
        	double irrigationRate = (double)ival;
        	double irrigationAmount = Double.valueOf(irrigationRate);
            double irrigationAmountSplit = irrigationAmount / (double)2.0;
        	xNext = xNext.replaceFirst("MI1", "  1");
        	xNext = xNext.replaceAll("_WA01", "    Y");
        	xNext = xNext.replaceAll("_IR01", "    D");
        	xNext = xNext.replaceAll("_IT01", "   50");
        	xNext = xNext.replaceAll("_IM01", "  100");
            xNext = xNext.replaceAll("_IV01", dfFAMN.format(irrigationAmountSplit));
        }
        else // Irrigation controlling with threshold level
        {
        	xNext = xNext.replaceFirst("MI1", "  0");
        	xNext = xNext.replaceAll("_WA01", "    Y");
        	xNext = xNext.replaceAll("_IR01", "    A");
        	xNext = xNext.replaceAll("_IT01", "  "+dfITHRL.format(ithl));
        	xNext = xNext.replaceAll("_IM01", "  "+dfITHRL.format(imax));
        }
    	// Irrigation method
    	if (String.valueOf(scn.cropCode).equals("RI"))
    		xNext = xNext.replaceAll("_IRGMETHOD_", "GS000 IR001"); // Furrow
    	else
    		xNext = xNext.replaceAll("_IRGMETHOD_", "GS000 IR001"); // Furrow
        
        // Planting date
        if (automaticPlanting)
        {
        	if (leafDamagePercentage>0)
        		xNext = xNext.replaceAll("_PM01", "    R");
        	else
        		xNext = xNext.replaceAll("_PM01", "    A");
        }
        else
        {
       		xNext = xNext.replaceAll("_PM01", "    R");        		
        }
    	xNext = xNext.replaceAll("_PD01", fiPlantingFirst);
    	xNext = xNext.replaceAll("_PF01", fiPlantingFirst);
    	xNext = xNext.replaceAll("_PL01", fiPlantingLast);
        
        // Planting density
        DecimalFormat dfPP = new DecimalFormat("0000");
        xNext = xNext.replaceAll("_PP1", dfPP.format(fiPlantingDensity));

        // Planting method
        xNext = xNext.replaceAll("_PM1", "   "+fiPlantingMethod);
        if (String.valueOf(fiPlantingMethod).equals("T"))
        	xNext = xNext.replaceFirst("_AGE", "  25");
        else
        	xNext = xNext.replaceFirst("_AGE", "   0");

        // Planting spacing
        DecimalFormat dfPS = new DecimalFormat("0000");
        xNext = xNext.replaceAll("_PS1", dfPS.format(fiPlantingSpacing));
        
        // Environmental modifications
        // _EA01 _EX01 _EN01 _ER01 _EC01
        xNext = xNext.replaceAll("ME1", "  1");
        xNext = xNext.replaceAll("_EC01", emCO2);
    	for (int m=0; m<12; m++)
        {
    		String mm = dfYY.format(m+1);
    		xNext = xNext.replaceAll("_EA"+mm, "A0000");
           	xNext = xNext.replaceAll("_EX"+mm, "A0000");
           	xNext = xNext.replaceAll("_EN"+mm, "A0000");
           	xNext = xNext.replaceAll("_ER"+mm, "A0000");
        }        	
        
        // Harvest when?
    	DecimalFormat dfNYR = new DecimalFormat("00000");
        if (String.valueOf(harvestWhen).substring(0,1).equals("Y"))
        {
        	int nyr = Integer.parseInt(String.valueOf(harvestWhen).substring(1));
        	int yyHarvest = Integer.parseInt(strYY)+nyr;
        	if (yyHarvest>100) yyHarvest = yyHarvest - 100;
        	xNext = xNext.replaceAll("_HA01", "    R");
        	xNext = xNext.replaceAll("_HD01", checkDateAcrossDec31(yyHarvest, pFirstInt, 365));
        	xNext = xNext.replaceAll("_NY01", dfNYR.format(nmyr));
        	xNext = xNext.replaceAll("_HF01", strYY+"001");
        	xNext = xNext.replaceAll("_HL01", strYY+"365");
        }
        else if (String.valueOf(harvestWhen).equals("EOY"))
        {
        	xNext = xNext.replaceAll("_HA01", "    R");
        	xNext = xNext.replaceAll("_HD01", checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 365));
        	xNext = xNext.replaceAll("_NY01", dfNYR.format(nmyr));
        	xNext = xNext.replaceAll("_HF01", strYY+"001");
        	xNext = xNext.replaceAll("_HL01", strYY+"365");
        }
        else if (String.valueOf(harvestWhen).equals("A"))
        {
        	xNext = xNext.replaceAll("_HA01", "    A");
        	xNext = xNext.replaceAll("_HD01", checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 365));
        	xNext = xNext.replaceAll("_NY01", dfNYR.format(nmyr));
        	xNext = xNext.replaceAll("_HF01", strYY+"001");
        	xNext = xNext.replaceAll("_HL01", checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 365));
        }
        else if (String.valueOf(harvestWhen).equals("D"))
        {
        	xNext = xNext.replaceAll("_HA01", "    D");
        	xNext = xNext.replaceAll("_HD01", "  365");
        	xNext = xNext.replaceAll("_NY01", dfNYR.format(nmyr));
        	xNext = xNext.replaceAll("_HF01", strYY+"001");
        	xNext = xNext.replaceAll("_HL01", checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 365));
        }
        else
        {
        	xNext = xNext.replaceAll("_HA01", "    M");
        	xNext = xNext.replaceAll("_HD01", checkDateAcrossDec31(Integer.parseInt(strYY), pFirstInt, 365));
        	xNext = xNext.replaceAll("_NY01", dfNYR.format(nmyr));
        	xNext = xNext.replaceAll("_HF01", strYY+"001");
        	xNext = xNext.replaceAll("_HL01", strYY+"365");
        }
        
        // Writing input file
        try
        {
        	tw.textWriter(pathToWorkspace, Simplr.xFileName[0]+"."+fiCropCode+"X", xNext);
        }
        catch (Exception ex) { }
        
        // Return if it's okay to run this
        return runThis;
        
    }


    
	// -------------------------------------------------------------------------	
	// Soil properties
	// -------------------------------------------------------------------------	
	Vector<Object> getSoilProperties(Statement stmt, String soilProfile)
	{
		Vector<Object> soilProperties = new Vector<Object>();
		try
		{
			String query = "SELECT SLB, SLLL, SDUL, SLNI FROM lut_soilproperties WHERE SoilProfile='"+soilProfile+"' ORDER BY SLB ASC";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
			{
				double[] s = { rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4) };
				soilProperties.add(s);
			}
		}
		catch (Exception e){ e.printStackTrace(); }
		return soilProperties;
	}	

	
	
	// -------------------------------------------------------------------------	
	// Fertilizer application regime 
	// -------------------------------------------------------------------------	
	int[][] getFertilizerRegime(String cropCode, double nitrogenFertilizerAmount, int dapAnthesis, int dapMaturity)
	{

		// Maximum five times
		// [][0]: dap
		// [][1]: amount, in kg[N]/ha
		int[][] nfrt = new int[5][2];

		// Crop-wise
        if (String.valueOf(cropCode).equals("MZ"))
        {

        	// Regime
            int dapFertilizerRegimeBegins   = 10;
            int dapFertilizerRegimeFinishes = Math.max(dapAnthesis-10, 10);
                       
            // Apply once
            if (nitrogenFertilizerAmount<20)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)nitrogenFertilizerAmount;
            }
            else if (nitrogenFertilizerAmount<100)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = 20;
            	
            	// 2nd
            	nfrt[1][0] = (dapFertilizerRegimeBegins+dapFertilizerRegimeFinishes)/2;	
            	nfrt[1][1] = (int)nitrogenFertilizerAmount - nfrt[0][1];
            }
            else if (nitrogenFertilizerAmount<200)
            {
            	// 1st
            	nfrt[0][0] = 1;
            	nfrt[0][1] = 30;

            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/3.0);

            	// Subsequent date scheduling
            	int ndate = (int)Math.max((dapFertilizerRegimeFinishes - dapFertilizerRegimeBegins)/3.0,0.0);
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeBegins;
            	nfrt[1][1] = nrate;
            	
            	// 3rd
            	nfrt[2][0] = nfrt[1][0] + ndate;
            	nfrt[2][1] = nrate;

            	// 4rd
            	nfrt[3][0] = nfrt[2][0] + ndate;
            	nfrt[3][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1] - nfrt[2][1]);            
            }
            else
            {
            	// 1st
            	nfrt[0][0] = 1;
            	nfrt[0][1] = 50;
            	
            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/4.0);

            	// Subsequent date scheduling
            	int ndate = (int)Math.max((dapFertilizerRegimeFinishes - dapFertilizerRegimeBegins)/4.0,0.0);
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeBegins;
            	nfrt[1][1] = nrate;
            	
            	// 3rd
            	nfrt[2][0] = nfrt[1][0] + ndate;
            	nfrt[2][1] = nrate;

            	// 4th
            	nfrt[3][0] = nfrt[2][0] + ndate;
            	nfrt[3][1] = nrate;
            	
            	// 5th
            	nfrt[4][0] = nfrt[3][0] + ndate;
            	nfrt[4][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1] - nfrt[2][1] - nfrt[3][1]);            
            }
        	
        	
        }
        else if (String.valueOf(cropCode).equals("RI"))
        {

        	// Regime
            int dapFertilizerRegimeBegins   = 10;
            int dapFertilizerRegimeFinishes = Math.max(dapAnthesis-10, 10);
                       
            // Apply once
            if (nitrogenFertilizerAmount<20)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)nitrogenFertilizerAmount;
            }
            else if (nitrogenFertilizerAmount<100)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)(nitrogenFertilizerAmount*(double)0.4);
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeFinishes;	
            	nfrt[1][1] = (int)nitrogenFertilizerAmount - nfrt[0][1];
            }
            else
            {
            	// 1st
            	nfrt[0][0] = 1;
            	nfrt[0][1] = 60;

            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/2.0);

            	// Subsequent date scheduling
            	int ndate = (int)Math.max((dapFertilizerRegimeFinishes - dapFertilizerRegimeBegins)/2.0,0.0);
            	
            	// 2nd
            	nfrt[1][0] = nfrt[1][0] + ndate;
            	nfrt[1][1] = nrate;

            	// 3rd
            	nfrt[2][0] = dapFertilizerRegimeFinishes;
            	nfrt[2][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1]);            
            }
            
        }
        else if (String.valueOf(cropCode).equals("WH"))
        {

        	// Regime
            int dapFertilizerRegimeBegins   = Math.max(dapAnthesis-10, 10);
            int dapFertilizerRegimeFinishes = Math.max(dapMaturity-10, 10);
                       
            // Apply once
            if (nitrogenFertilizerAmount<30)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)nitrogenFertilizerAmount;
            }
            else if (nitrogenFertilizerAmount<100)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = 10;

            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/3.0);           	
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeBegins;	
            	nfrt[1][1] = nrate;

            	// 3rd
            	nfrt[2][0] = dapFertilizerRegimeFinishes;	
            	nfrt[2][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1]);           	            
            }
            else if (nitrogenFertilizerAmount<200)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = 30;

            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/3.0);           	
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeBegins;	
            	nfrt[1][1] = nrate;

            	// 3rd
            	nfrt[2][0] = dapFertilizerRegimeFinishes;	
            	nfrt[2][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1]);           	            
            }
            else
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)(nitrogenFertilizerAmount * 0.25);

            	// Subsequent rate
            	int nrate = (int)((nitrogenFertilizerAmount - nfrt[0][1])/4.0);           	
            	
            	// 2nd
            	nfrt[1][0] = dapFertilizerRegimeBegins;	
            	nfrt[1][1] = nrate;

            	// 3rd
            	nfrt[2][0] = (int)((double)(dapFertilizerRegimeBegins+dapFertilizerRegimeFinishes)/2.0);	
            	nfrt[2][1] = nrate;           	            

            	// 4th
            	nfrt[3][0] = dapFertilizerRegimeFinishes;	
            	nfrt[3][1] = (int)(nitrogenFertilizerAmount - nfrt[0][1] - nfrt[1][1] - nfrt[2][1]);           	            
            }
            
        }
        else if (String.valueOf(cropCode).equals("PN") || String.valueOf(cropCode).equals("SB"))
        {

            // Apply once
            if (nitrogenFertilizerAmount<=20)
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = (int)nitrogenFertilizerAmount;
            }
            else
            {
            	// 1st
            	nfrt[0][0] = 1;		
            	nfrt[0][1] = 20;
            	
            	// 2nd
            	nfrt[1][0] = 45;	
            	nfrt[1][1] = (int)nitrogenFertilizerAmount - nfrt[0][1];
            }          
        	
        }
        
        return nfrt;
	}	

    
    
	// -------------------------------------------------------------------------	
	// Remove un-runnable cell ID 
	// -------------------------------------------------------------------------	
	void removeCellID(java.sql.Statement stmt, String inputTableName, String climateCode, int[] runIDs)
	{
		try
		{
			String sqlDelete = "DELETE FROM "+inputTableName+" WHERE CellID="+runIDs[1];
			//System.out.println("> Error at CellID: "+runIDs[1]+" (no climate profile found for the climate code "+climateCode+")");
			stmt.execute(sqlDelete);
		}
		catch (Exception e){ e.printStackTrace(); }
	}	

	
	
    // ---------------------------------------------------------------------        
    // DATE CALCULATION ACROSS DECEMBER 31
    // ---------------------------------------------------------------------
    String checkDateAcrossDec31(int yy, int ddd, int delta_d)
    {
    	DecimalFormat dfYY = new DecimalFormat("00");            
    	DecimalFormat dfDDD = new DecimalFormat("000");
    	int yy_update = yy;
    	int ddd_update = ddd + delta_d;
    	if (ddd_update>365)
    	{
    		ddd_update = ddd_update - 365;
    		yy_update = yy_update + 1;
    		if (yy_update==100) yy_update = 1;
    	}
    	else if (ddd_update==0)
    	{
    		ddd_update = 1;
    	}
    	else if (ddd_update<0)
    	{
    		ddd_update = ddd_update + 365;
    		yy_update = yy_update - 1;
    		if (yy_update==100) yy_update = 1;
    		if (yy_update<0) yy_update = yy_update + 100;
    	}
    	String yyddd = dfYY.format(yy_update) + dfDDD.format(ddd_update); 
    	return yyddd;
    }
    String checkYearAcross00(String yy, int delta_y)
    {
    	DecimalFormat dfYY = new DecimalFormat("00");            
    	int yy_update = Integer.parseInt(yy) + delta_y;
    	if (yy_update>=100)
    	{
    		yy_update = yy_update - 100;
    	}
    	return dfYY.format(yy_update);
    }

    
    
    
}
