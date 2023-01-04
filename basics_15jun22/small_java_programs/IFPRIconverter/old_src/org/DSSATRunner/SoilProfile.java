package org.DSSATRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.R2Useful.FunTricks;

public class SoilProfile {

	private static int readAheadLimit = 20*1024;
	private boolean isFull = false;
	private boolean haveCountedProfiles = false;

	private int nProfiles;
//	private int nLinesInOriginalFile;

	// first index is for soil profile, second is for soil layer (if necessary
	private String[] profileNames;
	private String[] profileComments;

	private String[] site;
	private String[] country;
	private float[] latitude;
	private float[] longitude;
	private String[] SCSFamily;

	private String[] color_SCOM;
	private float[] albedo_SALB;
	private float[] evaporationLimit_SLU1;
	private float[] drainageRate_SLDR;
	private float[] runoffCurve_SLRO;
	private float[] mineralizationFactor_SLNF;
	private float[] photosynthesisFactor_SLPF;
	private String[] pHInBufferDetermination_SMHB;
	private String[] phosphorusDetermination_SMPX;
	private String[] potassiumDetermination_SMKE;

	private float[][] layerDepthAtBase_SLB;
	private String[][] masterHorizon_SLMH; 
	private float[][] lowerLimit_SLLL;
	private float[][] drainedUpperLimit_SDUL;
	private float[][] saturatedUpperLimit_SSAT;
	private float[][] rootGrowthFactor_SRGF;
	private float[][] saturatedHydraulicConductivity_SSKS;
	private float[][] bulkDensity_SBDM;
	private float[][] organicCarbon_SLOC;
	private float[][] clay_SLCL;
	private float[][] silt_SLSI;
	private float[][] coarseFraction_SLCF;
	private float[][] totalNitrogen_SLNI;
	private float[][] phInWater_SLHW;
	private float[][] phInBuffer_SLHB;
	private float[][] cationExchangeCapacity_SCEC;
	private float[][] SADC;

	private float[][] SLPX;
	private float[][] SLPT;
	private float[][] SLPO;
	private float[][] CACO3;
	private float[][] SLAL;
	private float[][] SLFE;
	private float[][] SLMN;
	private float[][] SLBS;
	private float[][] SLPA;
	private float[][] SLPB;
	private float[][] SLKE;
	private float[][] SLMG;
	private float[][] SLNA;
	private float[][] SLSU;
	private float[][] SLEC;
	private float[][] SLCA;


	public static void dumpExampleProfile() {
		System.out.println("*HC_GEN0027  WISE        L       060 HvCh DATABASE, Sand LF060");
		System.out.println("@SITE        COUNTRY          LAT     LONG SCS Family");
		System.out.println(" -99         Generic      -00.000  -00.000 Sand shal (LF)");
		System.out.println("@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE");
		System.out.println("    BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001");
		System.out.println("@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC");
		System.out.println("    10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0 -99.0 -99.0");
		System.out.println("    30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0");
		System.out.println("    60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0");
		System.out.println("@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA");
		System.out.println("    10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0");
		System.out.println("    30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0");
		System.out.println("    60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0");
	}

	public SoilProfile(String filename) throws FileNotFoundException, IOException, Exception {

		/////////////////////////////////////////////////////////////
		// count up the number of profiles in the file provided... //
		/////////////////////////////////////////////////////////////

		System.out.println("WARNING: " + this.getClass().getCanonicalName() + " assumes a fixed order for the values");
		System.out.println("Use dumpExampleProfile() to see an example of the order.");

		this.countProfiles(filename);

		this.initializeArrays();

		this.readProfiles(filename);
	}

	private void countProfiles(String filename) throws FileNotFoundException, IOException {

		BufferedReader fileReader = new BufferedReader(new FileReader(filename));

		/////////////////////////////////////////////////////////////
		// count up the number of profiles in the file provided... //
		/////////////////////////////////////////////////////////////
		int nProfilesCounter = 0;
		int nLinesCounter = 0;
		String lineContents = fileReader.readLine();
		// treat comments like whitespace
		boolean haveFoundFirstProfile = false;
		boolean mostRecentLineWasWhitespace = false;
		while (lineContents != null) {

//			System.out.println("p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" + lineContents + "]");

			if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
//				System.out.println("replaced initial line with whitespace due to comment...");
				lineContents = "";
			}

			// check for empty lines....
			if (lineContents.trim().length() == 0) {
				// assume that this is the break between profiles.
				// bump up the profile counter
				if (haveFoundFirstProfile) {
					nProfilesCounter++;
				}
				mostRecentLineWasWhitespace = true;

				// and then read through until we don't have any more whitespace
				while (lineContents.trim().length() == 0) {
					nLinesCounter++;
					lineContents = fileReader.readLine();
					if (lineContents == null) {
						// we hit the end of the file, so break out...
						break;
					}	else if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
						lineContents = "";
//						System.out.println("WS: replaced line index [" + nLinesCounter + "] with whitespace due to comment...");
					}
//					System.out.println("  WS: p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" + lineContents + "]");
				}
			} else {

				// if it's not a comment line, then we are inside a profile, so just read the next line
				// bump up the line counter
				mostRecentLineWasWhitespace = false;
				haveFoundFirstProfile = true;

				nLinesCounter++;
				lineContents = fileReader.readLine();
				if (lineContents != null) {
					if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
						lineContents = "";
//						System.out.println("replaced line index [" + nLinesCounter + "] with whitespace due to comment...");
					}
				}
			}

		}

		if (mostRecentLineWasWhitespace) {
//			System.out.println("WS   FINAL count is " + nLinesCounter + " lines making up " + nProfilesCounter + " profiles...");
			this.nProfiles = nProfilesCounter;
//			this.nLinesInOriginalFile = nLinesCounter;

		} else {
//			System.out.println("REAL FINAL count is " + nLinesCounter + " lines making up " + (nProfilesCounter + 1) + " profiles...");
			this.nProfiles = nProfilesCounter + 1;
//			this.nLinesInOriginalFile = nLinesCounter;
		}

		System.out.println("File [" + filename + "] has " + nLinesCounter + " lines making up " + nProfilesCounter + " profiles...");

		haveCountedProfiles = true;
	}

	private void initializeArrays() throws Exception {
		if (!haveCountedProfiles) {
			System.out.println("Soil profile file must have been read before initializing arrays.");

			throw new Exception();
		}

		System.out.println("initializing arrays... nProfiles = " + nProfiles);

		// first index is for soil profile, second is for soil layer (if necessary
		profileNames = new String[nProfiles];
		profileComments = new String[nProfiles];

		site = new String[nProfiles];
		country = new String[nProfiles];
		latitude = new float[nProfiles];
		longitude = new float[nProfiles];
		SCSFamily = new String[nProfiles];

		color_SCOM = new String[nProfiles];
		albedo_SALB = new float[nProfiles];
		evaporationLimit_SLU1 = new float[nProfiles];
		drainageRate_SLDR = new float[nProfiles];
		runoffCurve_SLRO = new float[nProfiles];
		mineralizationFactor_SLNF = new float[nProfiles];
		photosynthesisFactor_SLPF = new float[nProfiles];
		pHInBufferDetermination_SMHB = new String[nProfiles];
		phosphorusDetermination_SMPX = new String[nProfiles];
		potassiumDetermination_SMKE = new String[nProfiles];

		layerDepthAtBase_SLB = new float[nProfiles][];
		masterHorizon_SLMH  = new String[nProfiles][];
		lowerLimit_SLLL = new float[nProfiles][];
		drainedUpperLimit_SDUL = new float[nProfiles][];
		saturatedUpperLimit_SSAT = new float[nProfiles][];
		rootGrowthFactor_SRGF = new float[nProfiles][];
		saturatedHydraulicConductivity_SSKS = new float[nProfiles][];
		bulkDensity_SBDM = new float[nProfiles][];
		organicCarbon_SLOC = new float[nProfiles][];
		clay_SLCL = new float[nProfiles][];
		silt_SLSI = new float[nProfiles][];
		coarseFraction_SLCF = new float[nProfiles][];
		totalNitrogen_SLNI = new float[nProfiles][];
		phInWater_SLHW = new float[nProfiles][];
		phInBuffer_SLHB = new float[nProfiles][];
		cationExchangeCapacity_SCEC = new float[nProfiles][];
		SADC = new float[nProfiles][];

		SLPX = new float[nProfiles][];
		SLPT = new float[nProfiles][];
		SLPO = new float[nProfiles][];
		CACO3 = new float[nProfiles][];
		SLAL = new float[nProfiles][];
		SLFE = new float[nProfiles][];
		SLMN = new float[nProfiles][];
		SLBS = new float[nProfiles][];
		SLPA = new float[nProfiles][];
		SLPB = new float[nProfiles][];
		SLKE = new float[nProfiles][];
		SLMG = new float[nProfiles][];
		SLNA = new float[nProfiles][];
		SLSU = new float[nProfiles][];
		SLEC = new float[nProfiles][];
		SLCA = new float[nProfiles][];
	}

	private void initializeLayerArrays(int profileIndex, int nLayersHere) throws Exception {
		if (!haveCountedProfiles) {
			System.out.println("Soil profile file must have been read before initializing arrays.");

			throw new Exception();
		}

//		System.out.println("initializing arrays... nProfiles = " + nProfiles);

		// first index is for soil profile, second is for soil layer (if necessary

		layerDepthAtBase_SLB[profileIndex]                = new float[nLayersHere];
		masterHorizon_SLMH[profileIndex]                 = new String[nLayersHere];
		lowerLimit_SLLL[profileIndex]                     = new float[nLayersHere];
		drainedUpperLimit_SDUL[profileIndex]              = new float[nLayersHere];
		saturatedUpperLimit_SSAT[profileIndex]            = new float[nLayersHere];
		rootGrowthFactor_SRGF[profileIndex]               = new float[nLayersHere];
		saturatedHydraulicConductivity_SSKS[profileIndex] = new float[nLayersHere];
		bulkDensity_SBDM[profileIndex]                    = new float[nLayersHere];
		organicCarbon_SLOC[profileIndex]                  = new float[nLayersHere];
		clay_SLCL[profileIndex]                           = new float[nLayersHere];
		silt_SLSI[profileIndex]                           = new float[nLayersHere];
		coarseFraction_SLCF[profileIndex]                 = new float[nLayersHere];
		totalNitrogen_SLNI[profileIndex]                  = new float[nLayersHere];
		phInWater_SLHW[profileIndex]                      = new float[nLayersHere];
		phInBuffer_SLHB[profileIndex]                     = new float[nLayersHere];
		cationExchangeCapacity_SCEC[profileIndex]         = new float[nLayersHere];
		SADC[profileIndex]                                = new float[nLayersHere];

		SLPX[profileIndex]  = new float[nLayersHere];
		SLPT[profileIndex]  = new float[nLayersHere];
		SLPO[profileIndex]  = new float[nLayersHere];
		CACO3[profileIndex] = new float[nLayersHere];
		SLAL[profileIndex]  = new float[nLayersHere];
		SLFE[profileIndex]  = new float[nLayersHere];
		SLMN[profileIndex]  = new float[nLayersHere];
		SLBS[profileIndex]  = new float[nLayersHere];
		SLPA[profileIndex]  = new float[nLayersHere];
		SLPB[profileIndex]  = new float[nLayersHere];
		SLKE[profileIndex]  = new float[nLayersHere];
		SLMG[profileIndex]  = new float[nLayersHere];
		SLNA[profileIndex]  = new float[nLayersHere];
		SLSU[profileIndex]  = new float[nLayersHere];
		SLEC[profileIndex]  = new float[nLayersHere];
		SLCA[profileIndex]  = new float[nLayersHere];
	}

	private void readProfiles(String filename) throws FileNotFoundException, IOException, Exception {

		BufferedReader fileReader = new BufferedReader(new FileReader(filename));

		// try to keep track of where we are as we parse the profile...
		boolean inProfileBlock = false;

		/////////////////////////////////////////////////////////////
		// count up the number of profiles in the file provided... //
		/////////////////////////////////////////////////////////////
		int nProfilesCounter = 0;
		int nLinesCounter = 0;
		int nSoilLayersHere = 0;
		String lineContents = fileReader.readLine();
		// treat comments like whitespace
		boolean haveFoundFirstProfile = false;
//		boolean mostRecentLineWasWhitespace = false;
		while (lineContents != null) {

//			System.out.println("p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" + lineContents + "]");

			if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
//				System.out.println("replaced initial line with whitespace due to comment...");
				lineContents = "";
			}

			// check for empty lines....
			if (lineContents.trim().length() == 0) {
				// assume that this is the break between profiles.
				// reset the within-profile flags
				inProfileBlock = false;


				// bump up the profile counter
				if (haveFoundFirstProfile) {
					nProfilesCounter++;
				}
//				mostRecentLineWasWhitespace = true;

				// and then read through until we don't have any more whitespace
				while (lineContents.trim().length() == 0) {
					nLinesCounter++;
					lineContents = fileReader.readLine();
					if (lineContents == null) {
						// we hit the end of the file, so break out...
						break;
					}	else if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
						lineContents = "";
//						System.out.println("WS: replaced line index [" + nLinesCounter + "] with whitespace due to comment...");
					}
//					System.out.println("  WS: p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" + lineContents + "]");
				}
			} else {

				// if it's not a comment line, then we are inside a profile, so just read the next line
				// bump up the line counter
				haveFoundFirstProfile = true;



				if (inProfileBlock == false) {
					// we are at the very beginning...
					// example
					// *HC_GEN0027  WISE        L       060 HvCh DATABASE, Sand LF060
					profileNames[nProfilesCounter] = lineContents.substring(1, 11);
					profileComments[nProfilesCounter] = lineContents.substring(12);
					inProfileBlock = true;
					// example
					// @SITE        COUNTRY          LAT     LONG SCS Family
					//  -99         Generic      -00.000  -00.000 Clay deep (HF)
					// read another line
					lineContents = fileReader.readLine(); // this should be the header line
					// read the values
					lineContents = fileReader.readLine(); // this should be the site level attributes
					this.site[nProfilesCounter] = lineContents.substring(1,12);
					this.country[nProfilesCounter] = lineContents.substring(13,24);
					this.latitude[nProfilesCounter] = Float.parseFloat(lineContents.substring(25,33).trim());
					this.longitude[nProfilesCounter] = Float.parseFloat(lineContents.substring(34,42).trim());
					this.SCSFamily[nProfilesCounter] = lineContents.substring(43);

					// example
					// @ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE
					// BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001
					lineContents = fileReader.readLine(); // this should be the header line
					// read the values
					lineContents = fileReader.readLine(); // this should be the field level characteristics
					color_SCOM[nProfilesCounter] = lineContents.substring(1,6).trim();
					albedo_SALB[nProfilesCounter] = Float.parseFloat(lineContents.substring(7,12).trim());
					evaporationLimit_SLU1[nProfilesCounter] = Float.parseFloat(lineContents.substring(13,18).trim());
					drainageRate_SLDR[nProfilesCounter] = Float.parseFloat(lineContents.substring(19,24).trim());
					runoffCurve_SLRO[nProfilesCounter] = Float.parseFloat(lineContents.substring(25,30).trim());
					mineralizationFactor_SLNF[nProfilesCounter] = Float.parseFloat(lineContents.substring(31,36).trim());
					photosynthesisFactor_SLPF[nProfilesCounter] = Float.parseFloat(lineContents.substring(37,42).trim());
					pHInBufferDetermination_SMHB[nProfilesCounter] = lineContents.substring(43,48).trim();
					phosphorusDetermination_SMPX[nProfilesCounter] = lineContents.substring(49,54).trim();
					potassiumDetermination_SMKE[nProfilesCounter] = lineContents.substring(55).trim();

					// ok now we need to read through a couple blocks...
					// we'll need to go through once to count up the number
					// of soil layers and then again to actually do the reading
					fileReader.mark(readAheadLimit);

					// example
					// @  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC
					// 10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0 -99.0 -99.0
					// 30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0
					// 60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0
					// @  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA
					// 10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
					// 30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
					// 60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
					lineContents = fileReader.readLine(); // this should be the header line

					lineContents = fileReader.readLine(); // this should be the first soil layer
					nSoilLayersHere = 0;
					while (lineContents.charAt(0) != '@') {
						nSoilLayersHere++;
						lineContents = fileReader.readLine(); // grab the next line to consider
					}

					// ok, we should now have the correct number stored in nSoilLayers
					// reset the stream and allocate the arrays
					fileReader.reset();
					initializeLayerArrays(nProfilesCounter,nSoilLayersHere);

					// now, go through again and do the ugly reading....
					// the first block
					lineContents = fileReader.readLine(); // this should be the header line

					for (int soilLayerIndex = 0; soilLayerIndex < nSoilLayersHere; soilLayerIndex++) {
						lineContents = fileReader.readLine();
						layerDepthAtBase_SLB[nProfilesCounter][soilLayerIndex]                = Float.parseFloat(lineContents.substring(1, 6).trim());
						masterHorizon_SLMH[nProfilesCounter][soilLayerIndex]                  =                  lineContents.substring(7, 12).trim();
						lowerLimit_SLLL[nProfilesCounter][soilLayerIndex]                     = Float.parseFloat(lineContents.substring(13, 18).trim());
						drainedUpperLimit_SDUL[nProfilesCounter][soilLayerIndex]              = Float.parseFloat(lineContents.substring(19, 24).trim());
						saturatedUpperLimit_SSAT[nProfilesCounter][soilLayerIndex]            = Float.parseFloat(lineContents.substring(25, 30).trim());
						rootGrowthFactor_SRGF[nProfilesCounter][soilLayerIndex]               = Float.parseFloat(lineContents.substring(31, 36).trim());
						saturatedHydraulicConductivity_SSKS[nProfilesCounter][soilLayerIndex] = Float.parseFloat(lineContents.substring(37, 42).trim());
						bulkDensity_SBDM[nProfilesCounter][soilLayerIndex]                    = Float.parseFloat(lineContents.substring(43, 48).trim());
						organicCarbon_SLOC[nProfilesCounter][soilLayerIndex]                  = Float.parseFloat(lineContents.substring(49, 54).trim());
						clay_SLCL[nProfilesCounter][soilLayerIndex]                           = Float.parseFloat(lineContents.substring(55, 60).trim());
						silt_SLSI[nProfilesCounter][soilLayerIndex]                           = Float.parseFloat(lineContents.substring(61, 66).trim());
						coarseFraction_SLCF[nProfilesCounter][soilLayerIndex]                 = Float.parseFloat(lineContents.substring(67, 72).trim());
						totalNitrogen_SLNI[nProfilesCounter][soilLayerIndex]                  = Float.parseFloat(lineContents.substring(73, 78).trim());
						phInWater_SLHW[nProfilesCounter][soilLayerIndex]                      = Float.parseFloat(lineContents.substring(79, 84).trim());
						phInBuffer_SLHB[nProfilesCounter][soilLayerIndex]                     = Float.parseFloat(lineContents.substring(85, 90).trim());
						cationExchangeCapacity_SCEC[nProfilesCounter][soilLayerIndex]         = Float.parseFloat(lineContents.substring(91, 96).trim());
						SADC[nProfilesCounter][soilLayerIndex]                                = Float.parseFloat(lineContents.substring(97).trim());
					}

					// the second block
					lineContents = fileReader.readLine(); // this should be the header line
					for (int soilLayerIndex = 0; soilLayerIndex < nSoilLayersHere; soilLayerIndex++) {
						lineContents = fileReader.readLine();
						SLPX[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(7, 12).trim());
						SLPT[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(13, 18).trim());
						SLPO[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(19, 24).trim());
						CACO3[nProfilesCounter][soilLayerIndex] = Float.parseFloat(lineContents.substring(25, 30).trim());
						SLAL[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(31, 36).trim());
						SLFE[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(37, 42).trim());
						SLMN[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(43, 48).trim());
						SLBS[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(49, 54).trim());
						SLPA[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(55, 60).trim());
						SLPB[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(61, 66).trim());
						SLKE[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(67, 72).trim());
						SLMG[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(73, 78).trim());
						SLNA[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(79, 84).trim());
						SLSU[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(85, 90).trim());
						SLEC[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(91, 96).trim());
						SLCA[nProfilesCounter][soilLayerIndex]  = Float.parseFloat(lineContents.substring(97).trim());

					}

				}

				nLinesCounter++;
				lineContents = fileReader.readLine();
				if (lineContents != null) {
					if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
						lineContents = "";
//						System.out.println("replaced line index [" + nLinesCounter + "] with whitespace due to comment...");
					}
				} // zero out comment lines
			} // end if/else have found first profile...

		} // end while there is still something to read....


		// set the flag if we get to here.
		isFull = true;

	}

	public int findProfileIndex(String profileName) {
		if (!isFull) {
			System.out.println("The soil profiles are empty currently...");
			return -2;
		}

//		System.out.println("We will try to dump something to the screen...");

		// search for first matching soil profile

		int soilProfileIndex = -1;
		for (int searchIndex = 0; searchIndex < nProfiles; searchIndex++) {
			if (profileNames[searchIndex].equals(profileName)) {
				soilProfileIndex = searchIndex;
				break;
			}
		}

		if (soilProfileIndex == -1) {
			System.out.println("Soil profile for [" + profileName + "] not found in this object");
			System.out.println("available names are:");
			for (int searchIndex = 0; searchIndex < nProfiles; searchIndex++) {
				System.out.println("[" + searchIndex + "] = [" + profileNames[searchIndex] + "]");
			}
		}
		
		return soilProfileIndex;
	}

	public String dumpSingleProfile(String profileName) throws Exception {

		if (!isFull) {
			System.out.println("The soil profiles are empty currently...");
			return null;
		}

//		System.out.println("We will try to dump something to the screen...");

		// search for first matching soil profile

		int soilProfileIndex = -1;
		for (int searchIndex = 0; searchIndex < nProfiles; searchIndex++) {
			if (profileNames[searchIndex].equals(profileName)) {
				soilProfileIndex = searchIndex;
				break;
			}
		}

		if (soilProfileIndex == -1) {
			System.out.println("Soil profile for [" + profileName + "] not found in this object");
			return null;
		}

		int nSoilLayers = layerDepthAtBase_SLB[soilProfileIndex].length;

		String profileString = "";
		// try to recreate the same format for the soil profile as DSSAT likes....

		// example
		// *HC_GEN0027  WISE        L       060 HvCh DATABASE, Sand LF060
		profileString += "*" + profileNames[soilProfileIndex] + " " + profileComments[soilProfileIndex] + "\n";

		// example
		// @SITE        COUNTRY          LAT     LONG SCS Family
		//  -99         Generic      -00.000  -00.000 Clay deep (HF)
		profileString += "@SITE        COUNTRY          LAT     LONG SCS Family" + "\n";
		profileString += " " + site[soilProfileIndex] + " " 
		+ country[soilProfileIndex] + " "
		+ FunTricks.fitInNCharacters(latitude[soilProfileIndex], 8) + " "
		+ FunTricks.fitInNCharacters(longitude[soilProfileIndex], 8) + " "
		+ SCSFamily[soilProfileIndex] + "\n";

		// example
		// @ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE
		//     BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001

		profileString += "@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE" + "\n";
		profileString += 
			" " + FunTricks.padStringWithLeadingSpaces(color_SCOM[soilProfileIndex], 5) +
			" " + FunTricks.fitInNCharacters(albedo_SALB[soilProfileIndex],5) +
			" " + FunTricks.fitInNCharacters(evaporationLimit_SLU1[soilProfileIndex],5) +
			" " + FunTricks.fitInNCharacters(drainageRate_SLDR[soilProfileIndex],5) +
			" " + FunTricks.fitInNCharacters(runoffCurve_SLRO[soilProfileIndex],5) +
			" " + FunTricks.fitInNCharacters(mineralizationFactor_SLNF[soilProfileIndex],5) +
			" " + FunTricks.fitInNCharacters(photosynthesisFactor_SLPF[soilProfileIndex],5) +
			" " + FunTricks.padStringWithLeadingSpaces(pHInBufferDetermination_SMHB[soilProfileIndex], 5) +
			" " + FunTricks.padStringWithLeadingSpaces(phosphorusDetermination_SMPX[soilProfileIndex], 5) +
			" " + FunTricks.padStringWithLeadingSpaces(potassiumDetermination_SMKE[soilProfileIndex], 5) + "\n"
			;

		// and now the soil layer stuff...
		// example
		// @  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC
		// 10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0 -99.0 -99.0
		// 30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0
		// 60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0 -99.0
		// @  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA
		// 10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
		// 30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
		// 60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
		profileString += "@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC" + "\n";
		for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
			profileString += 
				" " + FunTricks.fitInNCharacters(layerDepthAtBase_SLB[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.padStringWithLeadingSpaces(masterHorizon_SLMH[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(lowerLimit_SLLL[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(drainedUpperLimit_SDUL[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(saturatedUpperLimit_SSAT[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(rootGrowthFactor_SRGF[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(saturatedHydraulicConductivity_SSKS[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(bulkDensity_SBDM[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(organicCarbon_SLOC[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(clay_SLCL[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(silt_SLSI[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(coarseFraction_SLCF[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(totalNitrogen_SLNI[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(phInWater_SLHW[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(phInBuffer_SLHB[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(cationExchangeCapacity_SCEC[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SADC[soilProfileIndex][layerIndex],5) + "\n"
				;
		}


		profileString += "@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA" + "\n";
		for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
			profileString += 
				" " + FunTricks.fitInNCharacters(layerDepthAtBase_SLB[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLPX[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLPT[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLPO[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(CACO3[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLAL[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLFE[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLMN[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLBS[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLPA[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLPB[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLKE[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLMG[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLNA[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLSU[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLEC[soilProfileIndex][layerIndex],5) +
				" " + FunTricks.fitInNCharacters(SLCA[soilProfileIndex][layerIndex],5) + "\n"
				;
		}


		return profileString;

	}


	public String dumpAllProfilesAsString() throws Exception {
		String allString = "";
		
		for (int profileIndex = 0; profileIndex < nProfiles; profileIndex++) {
			allString += dumpSingleProfile(profileNames[profileIndex]) + "\n";
		}
		
		
		return allString;
	}

	
	public String makeInitializationBlockFractionBetweenBounds(
			String profileName,
			double fractionBetweenLowerLimitAndDrainedUpperLimit,
			String startingDateCode,
			double totalNitrogenPPMforBothNH4NO2)
	throws Exception {

		/*
		*INITIAL CONDITIONS
		@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME
		 1    MZ 84357  1000   -99  1.00  1.00 -99.0     1  1.00  0.00   100    15 -99
		@C  ICBL  SH2O  SNH4  SNO3
		 1    15 0.311   0.1   5.5
		 1    30 0.317   0.1   4.7
		 1    45 0.327   0.1   4.2
		 1    60 0.317   0.1  12.5
		 1    75 0.307   0.1  24.6
		 1    90 0.281   0.1  28.9
		 1   105 0.271   0.1  28.5
		 1   120 0.275   0.1  28.6
*/

		int profileIndex = findProfileIndex(profileName);
		
		String outputString = 
			"*INITIAL CONDITIONS" + "\n" +
			"@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME" + "\n" +
			" 1   -99 " + startingDateCode + 
			              "   -99   -99   -99   -99 -99.0   -99   -99   -99   -99   -99 -99" + "\n" +
			"@C  ICBL  SH2O  SNH4  SNO3" + "\n";

		String soilDepth = null;
		double bottomWaterShare = -1;
		double topWaterShare = -2;
		double initialWaterShare = -3;
		
		// Beware the MAGIC ASSUMPTION!!!
		// we are going to partition the desired total nitrogen
		// via a rule of thumb into 90/10 for nitrate/ammonium
		double magicFractionToAmmonium = 0.1;
		double ammoniumAmount = magicFractionToAmmonium * totalNitrogenPPMforBothNH4NO2;
		double nitrateAmount  = (1.0 - magicFractionToAmmonium) * totalNitrogenPPMforBothNH4NO2;
		
		int nLayers = this.getNLayersInProfile(profileIndex);
//		System.out.println("-- init nLayers = " + nLayers + " BF = " + layerDepthAtBase_SLB[profileIndex].length + " --");
		for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
			soilDepth = FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);
			bottomWaterShare = this.lowerLimit_SLLL[profileIndex][layerIndex];
			topWaterShare    = this.drainedUpperLimit_SDUL[profileIndex][layerIndex];
			
			initialWaterShare =
				topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
				+	bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);
			
			
			outputString +=
				" 1 " + soilDepth + " " 
				+ FunTricks.fitInNCharacters(initialWaterShare, 5) + " "
				+ FunTricks.fitInNCharacters(ammoniumAmount, 5) + " "
				+ FunTricks.fitInNCharacters(nitrateAmount, 5) + "\n";
		}
		
		/*
		*INITIAL CONDITIONS
		@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME
		 crop component number
		     previous crop code
		         initial conditions measurement date
		                root weight from previous crop
		                      nodule weight from previous crop
		                            rhizobia number
		                                  rhizobia effectivness
		                                        ????
		                                             ?????
		                                                    ????
		                                                         ????
		 1    MZ 84357  1000   -99  1.00  1.00 -99.0     1  1.00  0.00   100    15 -99
		@C  ICBL  SH2O  SNH4  SNO3
		    soil base layer depth
		          share of water (cm3 / cm3)
		                ammonium gN/Mg soil
		                      nitrate gN/Mg soil
		 1    15 0.311   0.1   5.5
		 1    30 0.317   0.1   4.7
		 1    45 0.327   0.1   4.2
		 1    60 0.317   0.1  12.5
		 1    75 0.307   0.1  24.6
		 1    90 0.281   0.1  28.9
		 1   105 0.271   0.1  28.5
		 1   120 0.275   0.1  28.6
*/

		
		return outputString;
	}
	
	
	
	
	
	public boolean isFull() {
		return isFull;
	}

	public int getNProfiles() {
		return nProfiles;
	}
	
	public int getNLayersInProfile(int profileIndex) {
		return layerDepthAtBase_SLB[profileIndex].length;
	}

	public int getNLayersInProfile(String profileName) {
		return layerDepthAtBase_SLB[findProfileIndex(profileName)].length;
	}

	public String[] getProfileNames() {
		return profileNames;
	}

	public String[] getProfileComments() {
		return profileComments;
	}

	public String[] getSite() {
		return site;
	}

	public String[] getCountry() {
		return country;
	}

	public float[] getLatitude() {
		return latitude;
	}

	public float[] getLongitude() {
		return longitude;
	}

	public String[] getSCSFamily() {
		return SCSFamily;
	}

	public String[] getColor_SCOM() {
		return color_SCOM;
	}

	public float[] getAlbedo_SALB() {
		return albedo_SALB;
	}

	public float[] getEvaporationLimit_SLU1() {
		return evaporationLimit_SLU1;
	}

	public float[] getDrainageRate_SLDR() {
		return drainageRate_SLDR;
	}

	public float[] getRunoffCurve_SLRO() {
		return runoffCurve_SLRO;
	}

	public float[] getMineralizationFactor_SLNF() {
		return mineralizationFactor_SLNF;
	}

	public float[] getPhotosynthesisFactor_SLPF() {
		return photosynthesisFactor_SLPF;
	}

	public String[] getPHInBufferDetermination_SMHB() {
		return pHInBufferDetermination_SMHB;
	}

	public String[] getPhosphorusDetermination_SMPX() {
		return phosphorusDetermination_SMPX;
	}

	public String[] getPotassiumDetermination_SMKE() {
		return potassiumDetermination_SMKE;
	}

	public float[][] getLayerDepthAtBase_SLB() {
		return layerDepthAtBase_SLB;
	}

	public String[][] getMasterHorizon_SLMH() {
		return masterHorizon_SLMH;
	}

	public float[][] getLowerLimit_SLLL() {
		return lowerLimit_SLLL;
	}

	public float[][] getDrainedUpperLimit_SDUL() {
		return drainedUpperLimit_SDUL;
	}

	public float[][] getSaturatedUpperLimit_SSAT() {
		return saturatedUpperLimit_SSAT;
	}

	public float[][] getRootGrowthFactor_SRGF() {
		return rootGrowthFactor_SRGF;
	}

	public float[][] getSaturatedHydraulicConductivity_SSKS() {
		return saturatedHydraulicConductivity_SSKS;
	}

	public float[][] getBulkDensity_SBDM() {
		return bulkDensity_SBDM;
	}

	public float[][] getOrganicCarbon_SLOC() {
		return organicCarbon_SLOC;
	}

	public float[][] getClay_SLCL() {
		return clay_SLCL;
	}

	public float[][] getSilt_SLSI() {
		return silt_SLSI;
	}

	public float[][] getCoarseFraction_SLCF() {
		return coarseFraction_SLCF;
	}

	public float[][] getTotalNitrogen_SLNI() {
		return totalNitrogen_SLNI;
	}

	public float[][] getPhInWater_SLHW() {
		return phInWater_SLHW;
	}

	public float[][] getPhInBuffer_SLHB() {
		return phInBuffer_SLHB;
	}

	public float[][] getCationExchangeCapacity_SCEC() {
		return cationExchangeCapacity_SCEC;
	}

	public float[][] getSADC() {
		return SADC;
	}

	public float[][] getSLPX() {
		return SLPX;
	}

	public float[][] getSLPT() {
		return SLPT;
	}

	public float[][] getSLPO() {
		return SLPO;
	}

	public float[][] getCACO3() {
		return CACO3;
	}

	public float[][] getSLAL() {
		return SLAL;
	}

	public float[][] getSLFE() {
		return SLFE;
	}

	public float[][] getSLMN() {
		return SLMN;
	}

	public float[][] getSLBS() {
		return SLBS;
	}

	public float[][] getSLPA() {
		return SLPA;
	}

	public float[][] getSLPB() {
		return SLPB;
	}

	public float[][] getSLKE() {
		return SLKE;
	}

	public float[][] getSLMG() {
		return SLMG;
	}

	public float[][] getSLNA() {
		return SLNA;
	}

	public float[][] getSLSU() {
		return SLSU;
	}

	public float[][] getSLEC() {
		return SLEC;
	}

	public float[][] getSLCA() {
		return SLCA;
	}





}