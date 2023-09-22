package org.DSSATRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.R2Useful.FunTricks;

public class SoilProfile {

  private static int readAheadLimit = 20 * 1024;
  private boolean isFull = false;
  private boolean haveCountedProfiles = false;

  private int nProfiles;
  //  private int nLinesInOriginalFile;

  private String originalFileName;

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

  private float[][] layerThicknessCM;

  public static void dumpExampleProfile() {
    System.out.println("*HC_GEN0027  WISE        L       060 HvCh DATABASE, Sand LF060");
    System.out.println("@SITE        COUNTRY          LAT     LONG SCS Family");
    System.out.println(" -99         Generic      -00.000  -00.000 Sand shal (LF)");
    System.out.println("@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE");
    System.out.println("    BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001");
    System.out.println(
        "@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB"
            + "  SCEC  SADC");
    System.out.println(
        "    10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0"
            + " -99.0 -99.0");
    System.out.println(
        "    30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0"
            + " -99.0 -99.0");
    System.out.println(
        "    60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0"
            + " -99.0 -99.0");
    System.out.println(
        "@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU"
            + "  SLEC  SLCA");
    System.out.println(
        "    10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0"
            + " -99.0 -99.0");
    System.out.println(
        "    30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0"
            + " -99.0 -99.0");
    System.out.println(
        "    60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0"
            + " -99.0 -99.0");
  }

  public SoilProfile(String filename) throws FileNotFoundException, IOException, Exception {

    /////////////////////////////////////////////////////////////
    // count up the number of profiles in the file provided... //
    /////////////////////////////////////////////////////////////

    // System.out.println(
    //     "WARNING: " + this.getClass().getCanonicalName() + " assumes a fixed order for the
    // values");
    // System.out.println("Use dumpExampleProfile() to see an example of the order.");

    originalFileName = filename;

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

      //      System.out.println("p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" +
      // lineContents + "]");

      if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
        //        System.out.println("replaced initial line with whitespace due to comment...");
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
          } else if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
            lineContents = "";
            //            System.out.println("WS: replaced line index [" + nLinesCounter + "] with
            // whitespace due to comment...");
          }
          //          System.out.println("  WS: p = " + nProfilesCounter + "; [" + nLinesCounter +
          // "] =
          // [" + lineContents + "]");
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
            //            System.out.println("replaced line index [" + nLinesCounter + "] with
            // whitespace
            // due to comment...");
          }
        }
      }
    }

    if (mostRecentLineWasWhitespace) {
      //      System.out.println("WS   FINAL count is " + nLinesCounter + " lines making up " +
      // nProfilesCounter + " profiles...");
      this.nProfiles = nProfilesCounter;
      //      this.nLinesInOriginalFile = nLinesCounter;

    } else {
      //      System.out.println("REAL FINAL count is " + nLinesCounter + " lines making up " +
      // (nProfilesCounter + 1) + " profiles...");
      this.nProfiles = nProfilesCounter + 1;
      //      this.nLinesInOriginalFile = nLinesCounter;
    }

    // System.out.println(
    //     "File ["
    //         + filename
    //         + "] has "
    //         + nLinesCounter
    //         + " lines making up "
    //         + nProfilesCounter
    //         + " profiles...");

    haveCountedProfiles = true;
    fileReader.close();
  }

  private void initializeArrays() throws Exception {
    if (!haveCountedProfiles) {
      System.out.println("Soil profile file must have been read before initializing arrays.");

      throw new Exception();
    }

    // System.out.println("initializing arrays... nProfiles = " + nProfiles);

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
    masterHorizon_SLMH = new String[nProfiles][];
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

    layerThicknessCM = new float[nProfiles][];
  }

  private void initializeLayerArrays(int profileIndex, int nLayersHere) throws Exception {
    if (!haveCountedProfiles) {
      System.out.println("Soil profile file must have been read before initializing arrays.");

      throw new Exception();
    }

    //    System.out.println("initializing arrays... nProfiles = " + nProfiles);

    // first index is for soil profile, second is for soil layer (if necessary

    layerDepthAtBase_SLB[profileIndex] = new float[nLayersHere];
    masterHorizon_SLMH[profileIndex] = new String[nLayersHere];
    lowerLimit_SLLL[profileIndex] = new float[nLayersHere];
    drainedUpperLimit_SDUL[profileIndex] = new float[nLayersHere];
    saturatedUpperLimit_SSAT[profileIndex] = new float[nLayersHere];
    rootGrowthFactor_SRGF[profileIndex] = new float[nLayersHere];
    saturatedHydraulicConductivity_SSKS[profileIndex] = new float[nLayersHere];
    bulkDensity_SBDM[profileIndex] = new float[nLayersHere];
    organicCarbon_SLOC[profileIndex] = new float[nLayersHere];
    clay_SLCL[profileIndex] = new float[nLayersHere];
    silt_SLSI[profileIndex] = new float[nLayersHere];
    coarseFraction_SLCF[profileIndex] = new float[nLayersHere];
    totalNitrogen_SLNI[profileIndex] = new float[nLayersHere];
    phInWater_SLHW[profileIndex] = new float[nLayersHere];
    phInBuffer_SLHB[profileIndex] = new float[nLayersHere];
    cationExchangeCapacity_SCEC[profileIndex] = new float[nLayersHere];
    SADC[profileIndex] = new float[nLayersHere];

    SLPX[profileIndex] = new float[nLayersHere];
    SLPT[profileIndex] = new float[nLayersHere];
    SLPO[profileIndex] = new float[nLayersHere];
    CACO3[profileIndex] = new float[nLayersHere];
    SLAL[profileIndex] = new float[nLayersHere];
    SLFE[profileIndex] = new float[nLayersHere];
    SLMN[profileIndex] = new float[nLayersHere];
    SLBS[profileIndex] = new float[nLayersHere];
    SLPA[profileIndex] = new float[nLayersHere];
    SLPB[profileIndex] = new float[nLayersHere];
    SLKE[profileIndex] = new float[nLayersHere];
    SLMG[profileIndex] = new float[nLayersHere];
    SLNA[profileIndex] = new float[nLayersHere];
    SLSU[profileIndex] = new float[nLayersHere];
    SLEC[profileIndex] = new float[nLayersHere];
    SLCA[profileIndex] = new float[nLayersHere];

    layerThicknessCM[profileIndex] = new float[nLayersHere];
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
    //    boolean mostRecentLineWasWhitespace = false;
    while (lineContents != null) {

      //      System.out.println("p = " + nProfilesCounter + "; [" + nLinesCounter + "] = [" +
      // lineContents + "]");

      // check for an initial line beginning with "*SOILS" since they often have something like that
      if (lineContents.length() >= 6) {
        if (lineContents.substring(0, 6).equalsIgnoreCase("*SOILS")) {
          System.out.println("skip initial *SOILS line...");
          lineContents = "";
        }
      }
      if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
        //        System.out.println("replaced initial line with whitespace due to comment...");
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
        //        mostRecentLineWasWhitespace = true;

        // and then read through until we don't have any more whitespace
        while (lineContents.trim().length() == 0) {
          nLinesCounter++;
          lineContents = fileReader.readLine();
          if (lineContents == null) {
            // we hit the end of the file, so break out...
            break;
          } else if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
            lineContents = "";
            //            System.out.println("WS: replaced line index [" + nLinesCounter + "] with
            // whitespace due to comment...");
          }
          //          System.out.println("  WS: p = " + nProfilesCounter + "; [" + nLinesCounter +
          // "] =
          // [" + lineContents + "]");
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
          this.site[nProfilesCounter] = lineContents.substring(1, 12);
          this.country[nProfilesCounter] = lineContents.substring(13, 24);
          this.latitude[nProfilesCounter] = Float.parseFloat(lineContents.substring(25, 33).trim());
          this.longitude[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(34, 42).trim());
          this.SCSFamily[nProfilesCounter] = lineContents.substring(43);

          // example
          // @ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE
          // BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001
          lineContents = fileReader.readLine(); // this should be the header line
          // read the values
          lineContents = fileReader.readLine(); // this should be the field level characteristics
          color_SCOM[nProfilesCounter] = lineContents.substring(1, 6).trim();
          albedo_SALB[nProfilesCounter] = Float.parseFloat(lineContents.substring(7, 12).trim());
          evaporationLimit_SLU1[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(13, 18).trim());
          drainageRate_SLDR[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(19, 24).trim());
          runoffCurve_SLRO[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(25, 30).trim());
          mineralizationFactor_SLNF[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(31, 36).trim());
          photosynthesisFactor_SLPF[nProfilesCounter] =
              Float.parseFloat(lineContents.substring(37, 42).trim());
          pHInBufferDetermination_SMHB[nProfilesCounter] = lineContents.substring(43, 48).trim();
          phosphorusDetermination_SMPX[nProfilesCounter] = lineContents.substring(49, 54).trim();
          potassiumDetermination_SMKE[nProfilesCounter] = lineContents.substring(55).trim();

          // ok now we need to read through a couple blocks...
          // we'll need to go through once to count up the number
          // of soil layers and then again to actually do the reading
          fileReader.mark(readAheadLimit);

          // example
          // @  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW
          // SLHB  SCEC  SADC
          // 10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0
          // -99.0 -99.0
          // 30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0
          // -99.0 -99.0
          // 60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0
          // -99.0 -99.0
          // @  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA
          // SLSU  SLEC  SLCA
          // 10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
          // -99.0 -99.0
          // 30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
          // -99.0 -99.0
          // 60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
          // -99.0 -99.0
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
          initializeLayerArrays(nProfilesCounter, nSoilLayersHere);

          // now, go through again and do the ugly reading....
          // the first block
          lineContents = fileReader.readLine(); // this should be the header line

          for (int soilLayerIndex = 0; soilLayerIndex < nSoilLayersHere; soilLayerIndex++) {
            lineContents = fileReader.readLine();
            layerDepthAtBase_SLB[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(1, 6).trim());

            if (soilLayerIndex == 0) {
              layerThicknessCM[nProfilesCounter][soilLayerIndex] =
                  layerDepthAtBase_SLB[nProfilesCounter][soilLayerIndex];
            } else {
              layerThicknessCM[nProfilesCounter][soilLayerIndex] =
                  layerDepthAtBase_SLB[nProfilesCounter][soilLayerIndex]
                      - layerDepthAtBase_SLB[nProfilesCounter][soilLayerIndex - 1];
            }

            masterHorizon_SLMH[nProfilesCounter][soilLayerIndex] =
                lineContents.substring(7, 12).trim();
            lowerLimit_SLLL[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(13, 18).trim());
            drainedUpperLimit_SDUL[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(19, 24).trim());
            saturatedUpperLimit_SSAT[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(25, 30).trim());
            rootGrowthFactor_SRGF[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(31, 36).trim());
            saturatedHydraulicConductivity_SSKS[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(37, 42).trim());
            bulkDensity_SBDM[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(43, 48).trim());
            organicCarbon_SLOC[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(49, 54).trim());
            clay_SLCL[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(55, 60).trim());
            silt_SLSI[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(61, 66).trim());
            coarseFraction_SLCF[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(67, 72).trim());
            totalNitrogen_SLNI[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(73, 78).trim());
            phInWater_SLHW[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(79, 84).trim());
            phInBuffer_SLHB[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(85, 90).trim());
            cationExchangeCapacity_SCEC[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(91, 96).trim());
            SADC[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(97).trim());
          }

          // the second block
          lineContents = fileReader.readLine(); // this should be the header line
          for (int soilLayerIndex = 0; soilLayerIndex < nSoilLayersHere; soilLayerIndex++) {
            lineContents = fileReader.readLine();
            SLPX[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(7, 12).trim());
            SLPT[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(13, 18).trim());
            SLPO[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(19, 24).trim());
            CACO3[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(25, 30).trim());
            SLAL[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(31, 36).trim());
            SLFE[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(37, 42).trim());
            SLMN[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(43, 48).trim());
            SLBS[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(49, 54).trim());
            SLPA[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(55, 60).trim());
            SLPB[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(61, 66).trim());
            SLKE[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(67, 72).trim());
            SLMG[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(73, 78).trim());
            SLNA[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(79, 84).trim());
            SLSU[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(85, 90).trim());
            SLEC[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(91, 96).trim());
            SLCA[nProfilesCounter][soilLayerIndex] =
                Float.parseFloat(lineContents.substring(97).trim());
          }
        }

        nLinesCounter++;
        lineContents = fileReader.readLine();
        if (lineContents != null) {
          if (lineContents.length() > 0 && lineContents.charAt(0) == '!') {
            lineContents = "";
            //            System.out.println("replaced line index [" + nLinesCounter + "] with
            // whitespace
            // due to comment...");
          }
        } // zero out comment lines
      } // end if/else have found first profile...
    } // end while there is still something to read....

    // set the flag if we get to here.
    isFull = true;

    fileReader.close();
  }

  public int findProfileIndex(String profileName) {
    if (!isFull) {
      System.out.println("The soil profiles are empty currently...");
      return -2;
    }

    //    System.out.println("We will try to dump something to the screen...");

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

    //    System.out.println("We will try to dump something to the screen...");

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
    profileString +=
        "*" + profileNames[soilProfileIndex] + " " + profileComments[soilProfileIndex] + "\n";

    // example
    // @SITE        COUNTRY          LAT     LONG SCS Family
    //  -99         Generic      -00.000  -00.000 Clay deep (HF)
    profileString += "@SITE        COUNTRY          LAT     LONG SCS Family" + "\n";
    profileString +=
        " "
            + site[soilProfileIndex]
            + " "
            + country[soilProfileIndex]
            + " "
            + FunTricks.fitInNCharacters(latitude[soilProfileIndex], 8)
            + " "
            + FunTricks.fitInNCharacters(longitude[soilProfileIndex], 8)
            + " "
            + SCSFamily[soilProfileIndex]
            + "\n";

    // example
    // @ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE
    //     BK  0.15  4.00  0.75 65.00  1.00  1.00 SA001 SA001 SA001

    profileString += "@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE" + "\n";
    profileString +=
        " "
            + FunTricks.padStringWithLeadingSpaces(color_SCOM[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(albedo_SALB[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(evaporationLimit_SLU1[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(drainageRate_SLDR[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(runoffCurve_SLRO[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(mineralizationFactor_SLNF[soilProfileIndex], 5)
            + " "
            + FunTricks.fitInNCharacters(photosynthesisFactor_SLPF[soilProfileIndex], 5)
            + " "
            + FunTricks.padStringWithLeadingSpaces(
                pHInBufferDetermination_SMHB[soilProfileIndex], 5)
            + " "
            + FunTricks.padStringWithLeadingSpaces(
                phosphorusDetermination_SMPX[soilProfileIndex], 5)
            + " "
            + FunTricks.padStringWithLeadingSpaces(potassiumDetermination_SMKE[soilProfileIndex], 5)
            + "\n";

    // and now the soil layer stuff...
    // example
    // @  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB
    // SCEC  SADC
    // 10     A 0.060 0.165 0.360  1.00 10.00  1.60  0.40  5.00  3.00 -99.0  0.03  6.50 -99.0 -99.0
    // -99.0
    // 30    AB 0.070 0.170 0.365  0.80  8.80  1.60  0.25  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0
    // -99.0
    // 60    BA 0.090 0.172 0.370  0.60  8.60  1.60  0.20  5.00  3.00 -99.0  0.02  6.50 -99.0 -99.0
    // -99.0
    // @  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU
    // SLEC  SLCA
    // 10 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
    // -99.0
    // 30 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
    // -99.0
    // 60 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0 -99.0
    // -99.0
    profileString +=
        "@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB"
            + "  SCEC  SADC\n";
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      profileString +=
          " "
              + FunTricks.fitInNCharacters(layerDepthAtBase_SLB[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.padStringWithLeadingSpaces(
                  masterHorizon_SLMH[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(lowerLimit_SLLL[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(drainedUpperLimit_SDUL[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(
                  saturatedUpperLimit_SSAT[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(rootGrowthFactor_SRGF[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(
                  saturatedHydraulicConductivity_SSKS[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(bulkDensity_SBDM[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(organicCarbon_SLOC[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(clay_SLCL[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(silt_SLSI[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(coarseFraction_SLCF[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(totalNitrogen_SLNI[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(phInWater_SLHW[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(phInBuffer_SLHB[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(
                  cationExchangeCapacity_SCEC[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SADC[soilProfileIndex][layerIndex], 5)
              + "\n";
    }

    profileString +=
        "@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU"
            + "  SLEC  SLCA\n";
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      profileString +=
          " "
              + FunTricks.fitInNCharacters(layerDepthAtBase_SLB[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLPX[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLPT[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLPO[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(CACO3[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLAL[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLFE[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLMN[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLBS[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLPA[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLPB[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLKE[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLMG[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLNA[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLSU[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLEC[soilProfileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(SLCA[soilProfileIndex][layerIndex], 5)
              + "\n";
    }

    return profileString;
  }

  public static double rainfedOrganicCarbonToOrganicNitrogenConverter(
      double organicCarbonAmount, double depthInCentimeters) {

    // these numbers were magically provided by tao li of IRRI in late april of 2012
    //    RAINFED
    //    0-40cm C:N = 20:1
    //    40-60cm C:N = 16:1
    //    60-cm C:N = 11:1

    double nitrogenGuess = Double.NaN;
    if (depthInCentimeters <= 40) {
      nitrogenGuess = organicCarbonAmount / 20.0;
    } else if (depthInCentimeters <= 60) {
      nitrogenGuess = organicCarbonAmount / 16.0;
    } else {
      nitrogenGuess = organicCarbonAmount / 11.0;
    }
    return nitrogenGuess;
  }

  public static double irrigatedOrganicCarbonToOrganicNitrogenConverter(
      double organicCarbonAmount, double depthInCentimeters) {

    // these numbers were magically provided by tao li of IRRI in late april of 2012
    //    IRRIGATED:
    //      0-20cm C:N = 20:1
    //      20-45cm C:N = 16:1
    //      45-cm C:N = 11:1

    double nitrogenGuess = Double.NaN;
    if (depthInCentimeters <= 20) {
      nitrogenGuess = organicCarbonAmount / 20.0;
    } else if (depthInCentimeters <= 45) {
      nitrogenGuess = organicCarbonAmount / 16.0;
    } else {
      nitrogenGuess = organicCarbonAmount / 11.0;
    }
    return nitrogenGuess;
  }

  public String convertProfileToORYZA(
      String profileName,
      boolean doPuddling,
      boolean useWaterContentParameter,
      double annualAverageTopSoilTemperature,
      double startingMonthTopSoilTemperature,
      double fractionBetweenLowerLimitAndDrainedUpperLimit,
      double totalInitialNitrogenKgPerHa,
      double depthForNitrogen,
      double rootWeight,
      double residueNitrogenPercent,
      String newlineToUse)
      throws Exception {

    //    double[] clayStableFractionAbove,
    //    double[] loamStableFractionAbove,
    //    double[] sandStableFractionAbove
    //    double[] standardDepths
    //    String startingDateCode,
    //    double incorporationRate,
    //    double incorporationDepth,
    //    double surfaceResidueWeight, // putting this in the experiment file rather than in the
    // soil
    // definition
    //    , double organicCarbonToNitrogenConversionFactor // replacing this with a magic conversion
    // table by depth
    if (!isFull) {
      System.out.println("The soil profiles are empty currently...");
      return null;
    }

    //    System.out.println("We will try to dump something to the screen...");

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

    // a string where we can put comments as to funny business that has occurred along the way...
    String warningString = "**********************************************" + newlineToUse;
    warningString +=
        "* This convertor was hacked from a visual basic script originally" + newlineToUse;
    warningString +=
        "* put together by Tao Li to work on WISE data." + newlineToUse + "*" + newlineToUse;

    // process the layers a bit
    // apparently in tao's convertor, we want to puddle only through the first layer as
    // listed in our source data. but, we also want the first few layers in oryza
    // to be no more than 10cm thick. so, if the first layer is too thick, it gets split
    // up into no more than 4 layers of no more than 10cm, unless it is more than 30cm,
    // in which case it just gets split into 4 layers regardless.

    // NOW, let us say this a little bit more clearly based on a slightly better understanding...
    // we want to make sure we can puddle to a depth of at least 20cm (inclusive).
    // AND
    // within that puddling zone, we want the soil layers to be no more than 10cm thick.
    // THUS,
    // we need to figure out how many of the source layers we need to use for the puddling
    // zone. then we need to split any thick layers up....

    // ok, we'll have to make a go of this...
    double magicMinimumDepthNeededForPuddling = 20;
    double magicThicknessWarningThreshold = 10.0;
    int magicMaximumNumberOfLayers = 10;
    float magicDefaultValueToUse = 0.0f; // value to use when someone puts in -99
    double magicDefaultValueInSource =
        -99.0; // value that you find in the source data that means UNKNOWN

    // figure how how many source layers are needed for the puddling zone
    int bottomPuddledLayerFromSource = -1;
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      if (this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex]
          >= magicMinimumDepthNeededForPuddling) {
        bottomPuddledLayerFromSource = layerIndex;
        break;
      }
    }
    warningString +=
        "* we need to puddle through original layer index "
            + bottomPuddledLayerFromSource
            + "."
            + newlineToUse;

    int nPuddledLayersFromSource = bottomPuddledLayerFromSource + 1;
    // now let us decide if we need to split up any of the existing layers...
    int[] nExtraLayersNeededBySourceLayer = new int[nPuddledLayersFromSource];
    int nExtraLayersNeededSoFar = 0;
    double thisThickness = -1;
    for (int layerIndex = 0; layerIndex < nPuddledLayersFromSource; layerIndex++) {
      // this needs to be based on thickness....
      if (layerIndex == 0) {
        thisThickness = this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex];
      } else {
        thisThickness =
            this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex]
                - this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex - 1];
        ;
      }
      nExtraLayersNeededBySourceLayer[layerIndex] =
          Math.max(
              0,
              Math.min(
                  -1 + (int) Math.ceil(thisThickness / magicThicknessWarningThreshold),
                  magicMaximumNumberOfLayers - nSoilLayers - nExtraLayersNeededSoFar));
      nExtraLayersNeededSoFar += nExtraLayersNeededBySourceLayer[layerIndex];
      warningString +=
          "** original layer ["
              + layerIndex
              + "] extra needed = "
              + nExtraLayersNeededBySourceLayer[layerIndex]
              + "; running = "
              + nExtraLayersNeededSoFar
              + newlineToUse;
    }

    // first, let's figure out how many extra layers we need, then we can
    // declare new placeholders and split stuff up
    // then we'll go through and do the computation...

    // we do this by looking at how many 10cm slabs (including partials) there are and dropping 1.
    // we also want to enforce the ORYZA limitation of no more than 10 total layers...
    //    int nExtraLayersNeeded = Math.max(0,
    //        Math.min(
    //        -1 + (int)Math.ceil(this.layerDepthAtBase_SLB[soilProfileIndex][0] /
    // magicThicknessWarningThreshold),
    //        magicMaximumNumberOfLayers - nSoilLayers
    //        )
    //        );
    int nExtraLayersNeeded = nExtraLayersNeededSoFar;

    warningString +=
        "* top layer is being split into " + nExtraLayersNeeded + " extra layers" + newlineToUse;
    warningString +=
        "* originalThickness = "
            + this.layerDepthAtBase_SLB[soilProfileIndex][0]
            + " ; max thickness = "
            + magicThicknessWarningThreshold
            + newlineToUse;
    if (-1
            + (int)
                Math.ceil(
                    this.layerDepthAtBase_SLB[soilProfileIndex][0] / magicThicknessWarningThreshold)
        > magicMaximumNumberOfLayers - nSoilLayers) {
      warningString += "*     === slacker error message ===" + newlineToUse;
      warningString += "* possibly sub-optimal first layer split..." + newlineToUse;
      warningString +=
          "* need: "
              + (-1
                  + (int)
                      Math.ceil(
                          this.layerDepthAtBase_SLB[soilProfileIndex][0]
                              / magicThicknessWarningThreshold))
              + " more layers to try to get a good puddling representation"
              + newlineToUse;
      warningString +=
          "* but only [max:]"
              + magicMaximumNumberOfLayers
              + " - [current:]"
              + nSoilLayers
              + " = "
              + (magicMaximumNumberOfLayers - nSoilLayers)
              + " are available"
              + newlineToUse;
    }

    int nSPLITSoilLayers = nSoilLayers + nExtraLayersNeeded;
    int nPuddledLayersToUse =
        nPuddledLayersFromSource + nExtraLayersNeeded; // + 1; // the extras plus the originals
    float[] SPLITlayerDepthAtBase_SLB = new float[nSPLITSoilLayers];
    float[] SPLITlayerThicknessCM = new float[nSPLITSoilLayers];
    float[] SPLITclay_SLCL = new float[nSPLITSoilLayers];
    float[] SPLITsilt_SLSI = new float[nSPLITSoilLayers];
    float[] SPLITbulkDensity_SBDM = new float[nSPLITSoilLayers];
    float[] SPLITorganicCarbon_SLOC = new float[nSPLITSoilLayers];
    float[] SPLITbaseOrganicCarbon_kg_per_ha = new float[nSPLITSoilLayers];
    float[] SPLITresidueContributionOrganicCarbon_kg_per_ha = new float[nSPLITSoilLayers];
    float[] SPLITtotalOrganicCarbon_kg_per_ha = new float[nSPLITSoilLayers];
    float[] SPLITorganicNitrogen_kg_per_ha = new float[nSPLITSoilLayers];
    float[] SPLITnitrateAmountKgPerHa = new float[nSPLITSoilLayers];
    float[] SPLITpH = new float[nSPLITSoilLayers];

    float[] SPLITlowerLimit_SLLL = new float[nSPLITSoilLayers];
    float[] SPLITdrainedUpperLimit_SDUL = new float[nSPLITSoilLayers];

    int firstSPLITLayerHere = -5;
    int lastSPLITLayerHere = -75;
    int counterInsideSplitLayer = 0;

    // initialize the bottom layers that will be unchanged, just shifted
    for (int layerIndex = nPuddledLayersFromSource; layerIndex < nSoilLayers; layerIndex++) {
      SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex] =
          layerDepthAtBase_SLB[soilProfileIndex][layerIndex];

      SPLITlayerThicknessCM[nExtraLayersNeeded + layerIndex] =
          layerDepthAtBase_SLB[soilProfileIndex][layerIndex]
              - layerDepthAtBase_SLB[soilProfileIndex][layerIndex - 1];

      // Beware the UGLY terniary operator (to shorten up a bunch of assignments...)
      SPLITclay_SLCL[nExtraLayersNeeded + layerIndex] =
          (clay_SLCL[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : clay_SLCL[soilProfileIndex][layerIndex];
      SPLITsilt_SLSI[nExtraLayersNeeded + layerIndex] =
          (silt_SLSI[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : silt_SLSI[soilProfileIndex][layerIndex];
      SPLITbulkDensity_SBDM[nExtraLayersNeeded + layerIndex] =
          (bulkDensity_SBDM[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : bulkDensity_SBDM[soilProfileIndex][layerIndex];
      SPLITpH[nExtraLayersNeeded + layerIndex] =
          (phInWater_SLHW[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : phInWater_SLHW[soilProfileIndex][layerIndex];

      SPLITlowerLimit_SLLL[nExtraLayersNeeded + layerIndex] =
          (lowerLimit_SLLL[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : lowerLimit_SLLL[soilProfileIndex][layerIndex];
      SPLITdrainedUpperLimit_SDUL[nExtraLayersNeeded + layerIndex] =
          (drainedUpperLimit_SDUL[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
              ? magicDefaultValueToUse
              : drainedUpperLimit_SDUL[soilProfileIndex][layerIndex];

      //      the original line from the VB code has:
      //      thickness  * BulkDensity * SOC % * 100,000
      //      for the units, this means:
      //      cm/100     * g/cm^3      * %     * 100,000
      //      or
      //      cm         * g/cm^3      * %     *   1,000
      //      = 1000 * g/cm^2
      //      now 100 cm = 1 m -> 10^4 cm^2 = 1 m^2 -> 1 cm^2 = 10^-4 m^2
      //      so...
      //      = 1000 * g / (10^-4 m^2)
      //      = 10^3 * 10^4 * g / m^2
      //      also, (100 m)^2 = 1 ha -> 10^4 m^2 = 1ha -> 1 m^2 = 10^-4 ha
      //      so...
      //      = 10^3 * 10^4 * g / (10^04 ha)
      //      = 10^3 * g/ha
      //      furthermore 1000 g = 1 kg -> 1 g = 10^-3 kg
      //      so...
      //      = 10^3 * (10^-3 kg) / ha = kg/ha
      //      so at the end we get kg/ha
      //      of course, it can't be that easy....
      //      before the SOC numbers get sent to the hydraulic parameters thing, they get
      // transformed
      // one more time
      //      Int(SOC(K) / (TKL(K) * BD(K) * 10000000# * 0.58) * 1000000#) / 1000000#
      //      of course, one fo the multiply-and-divide's is about truncating decimals, so we will
      // forget about that
      //      but, it seems to be basically undoing the thickness and bulk density part, introduces
      // a
      // factor related to 0.58
      //      and then some big power of ten.
      //
      //      all on one line now, let us do algebra:
      //      SOM(K)
      //      =
      //      (SOC(K) / (TKL(K) * BD(K) * 10,000,000 * 0.58) * 1,000,000) / 1,000,000
      //      =
      //       SOC(K) / (TKL(K) * BD(K) * 10,000,000 * 0.58)
      //      = (TKL(K)  * BD(K) * SOC-as-% * 100,000) / (TKL(K) * BD(K) * 10,000,000 * 0.58)
      //      = (SOC-as-% * 1e5) / (1e7 * 0.58)
      //      = SOC-as-% * 1e-2 / 0.58
      //      = SOC-as-fraction / 0.58
      //
      //      apparently, 0.58 is the typical fraction of elemental carbon in organic matter
      //
      //      so, the original is dividing by 0.58, but i think that is ridiculous: what if we
      //      had a soil layer that was 100% organic matter (say, i dump a bunch of compost on)
      //      then we would have elemental carbon of 100% / 0.58 = 172.4%.
      //
      //      we should be multiplying, so that is what i'm going to do...

      // let's try dropping the 100000

      //      (SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex] -
      // SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex - 1]) *
      //      bulkDensity_SBDM[soilProfileIndex][layerIndex] *

      // Beware the MAGIC NUMBER!!! 0.58 is the fraction of elemental carbon typically found in
      // organic matter
      //      ;organicCarbon_SLOC[soilProfileIndex][layerIndex]
      SPLITorganicCarbon_SLOC[nExtraLayersNeeded + layerIndex] =
          (float)
              ((organicCarbon_SLOC[soilProfileIndex][layerIndex] == magicDefaultValueInSource)
                  ? magicDefaultValueToUse
                  : organicCarbon_SLOC[soilProfileIndex][layerIndex] * 0.58 / 100.0);

      SPLITbaseOrganicCarbon_kg_per_ha[nExtraLayersNeeded + layerIndex] =
          SPLITorganicCarbon_SLOC[nExtraLayersNeeded + layerIndex]
              * (SPLITlayerThicknessCM[nExtraLayersNeeded + layerIndex])
              / 100
              * SPLITbulkDensity_SBDM[nExtraLayersNeeded + layerIndex]
              * 100000;
      //      SPLITbaseOrganicCarbon_kg_per_ha  [nExtraLayersNeeded + layerIndex] =
      // SPLITorganicCarbon_SLOC[nExtraLayersNeeded + layerIndex] *
      //      (SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex] -
      // SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex - 1])/100 *
      //      SPLITbulkDensity_SBDM[nExtraLayersNeeded + layerIndex] *
      //      100000;

      // nitrogen is a bit more tricky because this arises from surface residues and initial soil
      // nitrogen assumptions...
      // now, oryza wants an organic nitrogen number which is lacking in standard DSSAT stuff (i
      // think)
      // so i am going to be silly and base it off of the organic carbon assumption and then add in
      // surface residues...
      SPLITorganicNitrogen_kg_per_ha[nExtraLayersNeeded + layerIndex] =
          (float)
              irrigatedOrganicCarbonToOrganicNitrogenConverter(
                  SPLITbaseOrganicCarbon_kg_per_ha[nExtraLayersNeeded + layerIndex],
                  SPLITlayerDepthAtBase_SLB[nExtraLayersNeeded + layerIndex]);
      //      (float) (organicCarbonToNitrogenConversionFactor *
      // SPLITbaseOrganicCarbon_kg_per_ha[nExtraLayersNeeded + layerIndex]);
      // do the initial soil nitrogen bit
      // add in nitrogen from residues

    }

    // now try to split up the puddled layers if necessary...
    //    for (int layerIndex = 0; layerIndex < nExtraLayersNeeded + 1; layerIndex++) { //
    // nExtraLayersNeeded + 1
    firstSPLITLayerHere = 0; // this actually needs to be zero. it will get bumped up as we go along
    for (int sourceLayerIndex = 0;
        sourceLayerIndex < nPuddledLayersFromSource;
        sourceLayerIndex++) {
      // figure out what the first and last of the SPLIT layers are that correspond to this source
      // layer
      firstSPLITLayerHere = 0; // 1 + nExtraLayersNeededBySourceLayer[sourceLayerIndex]; //
      lastSPLITLayerHere =
          1 + nExtraLayersNeededBySourceLayer[sourceLayerIndex]; // the original and all the extras
      // now, add in all the split layers that came before...
      for (int altSourceLayerIndex = 0;
          altSourceLayerIndex < sourceLayerIndex;
          altSourceLayerIndex++) {
        firstSPLITLayerHere += 1 + nExtraLayersNeededBySourceLayer[altSourceLayerIndex];
        lastSPLITLayerHere += 1 + nExtraLayersNeededBySourceLayer[altSourceLayerIndex];
      }

      warningString +=
          "* * * source ["
              + sourceLayerIndex
              + "] first split = "
              + firstSPLITLayerHere
              + " / last = "
              + lastSPLITLayerHere
              + newlineToUse;
      // note the nonstandard continuation condition (<=)
      counterInsideSplitLayer = 0;
      for (int layerIndex = firstSPLITLayerHere; layerIndex < lastSPLITLayerHere; layerIndex++) {

        warningString +=
            "*** new layer "
                + layerIndex
                + " is being processed from source layer "
                + sourceLayerIndex
                + newlineToUse;

        SPLITlayerThicknessCM[layerIndex] =
            layerThicknessCM[soilProfileIndex][sourceLayerIndex]
                / (nExtraLayersNeededBySourceLayer[sourceLayerIndex] + 1);
        // we have to figure out how far up from the original base this is. this depends on how many
        // pieces we have processed, so we need a seperate counter
        SPLITlayerDepthAtBase_SLB[layerIndex] =
            layerDepthAtBase_SLB[soilProfileIndex][sourceLayerIndex]
                - SPLITlayerThicknessCM[layerIndex]
                    * (nExtraLayersNeededBySourceLayer[sourceLayerIndex] - counterInsideSplitLayer);
        counterInsideSplitLayer++;

        //      SPLITlayerDepthAtBase_SLB[layerIndex] = (layerIndex + 1) *
        // layerDepthAtBase_SLB[soilProfileIndex][sourceLayerIndex] /
        // (nExtraLayersNeededBySourceLayer[sourceLayerIndex] + 1);

        //      SPLITclay_SLCL[layerIndex]            =
        // clay_SLCL[soilProfileIndex][sourceLayerIndex];
        //      SPLITsilt_SLSI[layerIndex]            =
        // silt_SLSI[soilProfileIndex][sourceLayerIndex];
        //      SPLITbulkDensity_SBDM[layerIndex]     =
        // bulkDensity_SBDM[soilProfileIndex][sourceLayerIndex];
        //      SPLITpH[layerIndex]                   =
        // phInWater_SLHW[soilProfileIndex][sourceLayerIndex];
        //
        //      SPLITlowerLimit_SLLL[layerIndex]        =
        // lowerLimit_SLLL[soilProfileIndex][sourceLayerIndex];
        //      SPLITdrainedUpperLimit_SDUL[layerIndex] =
        // drainedUpperLimit_SDUL[soilProfileIndex][sourceLayerIndex];

        // Beware the UGLY terniary operator (to shorten up a bunch of assignments...)
        SPLITclay_SLCL[layerIndex] =
            (clay_SLCL[soilProfileIndex][sourceLayerIndex] == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : clay_SLCL[soilProfileIndex][sourceLayerIndex];
        SPLITsilt_SLSI[layerIndex] =
            (silt_SLSI[soilProfileIndex][sourceLayerIndex] == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : silt_SLSI[soilProfileIndex][sourceLayerIndex];
        SPLITbulkDensity_SBDM[layerIndex] =
            (bulkDensity_SBDM[soilProfileIndex][sourceLayerIndex] == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : bulkDensity_SBDM[soilProfileIndex][sourceLayerIndex];
        SPLITpH[layerIndex] =
            (phInWater_SLHW[soilProfileIndex][sourceLayerIndex] == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : phInWater_SLHW[soilProfileIndex][sourceLayerIndex];

        SPLITlowerLimit_SLLL[layerIndex] =
            (lowerLimit_SLLL[soilProfileIndex][sourceLayerIndex] == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : lowerLimit_SLLL[soilProfileIndex][sourceLayerIndex];
        SPLITdrainedUpperLimit_SDUL[layerIndex] =
            (drainedUpperLimit_SDUL[soilProfileIndex][sourceLayerIndex]
                    == magicDefaultValueInSource)
                ? magicDefaultValueToUse
                : drainedUpperLimit_SDUL[soilProfileIndex][sourceLayerIndex];

        // see big ugly note above: we are converting to kg/ha....
        //      SPLITorganicCarbon_SLOC[layerIndex]   =
        // (float)(organicCarbon_SLOC[soilProfileIndex][sourceLayerIndex] * 0.58 / 100.0);
        SPLITorganicCarbon_SLOC[layerIndex] =
            (float)
                ((organicCarbon_SLOC[soilProfileIndex][sourceLayerIndex]
                        == magicDefaultValueInSource)
                    ? magicDefaultValueToUse
                    : organicCarbon_SLOC[soilProfileIndex][sourceLayerIndex] * 0.58 / 100.0);

        SPLITbaseOrganicCarbon_kg_per_ha[layerIndex] =
            SPLITorganicCarbon_SLOC[layerIndex]
                * (SPLITlayerThicknessCM[layerIndex])
                / 100
                * SPLITbulkDensity_SBDM[layerIndex]
                * 100000;

        //      if (layerIndex == 0) {
        //        SPLITbaseOrganicCarbon_kg_per_ha  [layerIndex] =
        // SPLITorganicCarbon_SLOC[layerIndex] *
        // (SPLITlayerDepthAtBase_SLB[layerIndex] - 0)/100 * SPLITbulkDensity_SBDM[layerIndex] *
        // 100000;
        //      } else {
        //        SPLITbaseOrganicCarbon_kg_per_ha  [layerIndex] =
        // SPLITorganicCarbon_SLOC[layerIndex] *
        //        (SPLITlayerDepthAtBase_SLB[layerIndex] - SPLITlayerDepthAtBase_SLB[layerIndex -
        // 1])/100 *
        //        SPLITbulkDensity_SBDM[layerIndex] *
        //        100000;
        //      }
      } // for layerIndex within here...
    } // for sourceLayerIndex

    // we need to add in the surface residues and root mass....
    // we need to determine which layers are appropriate here....
    // find first layer that is under the part where we want to distribute it
    int firstLayerBelowResidueIncorporationDepth = -1;
    int firstLayerBelowNitroDepth = -1;

    for (int realLayerIndex = 0; realLayerIndex < nSPLITSoilLayers; realLayerIndex++) {
      //      if (SPLITlayerDepthAtBase_SLB[realLayerIndex] >= incorporationDepth &&
      // firstLayerBelowResidueIncorporationDepth < 0) {
      //        firstLayerBelowResidueIncorporationDepth = realLayerIndex;
      //      }
      if (SPLITlayerDepthAtBase_SLB[realLayerIndex] >= depthForNitrogen
          && firstLayerBelowNitroDepth < 0) {
        firstLayerBelowNitroDepth = realLayerIndex;
      }
      if (firstLayerBelowResidueIncorporationDepth >= 0 && firstLayerBelowNitroDepth >= 0) {
        break;
      }
    }

    warningString +=
        "* initial surface residues should go in the experiment file, while roots go here..."
            + newlineToUse;
    // let's put surface residues in the very top layer...
    //    SPLITresidueContributionOrganicCarbon_kg_per_ha[0] = (float)surfaceResidueWeight ;
    //    warningString += "* putting unincorporated surface residues entirely in the top (SPLIT)
    // layer\n";
    // split up the residue appropriately; weighted by thickness, i guess
    // the top layer will get any "unincorporated stuff"
    //    double relevantThickness = -1;
    //    System.out.println("residue layer below = " + firstLayerBelowResidueIncorporationDepth);
    //    relevantThickness = Math.min(incorporationDepth, SPLITlayerThicknessCM[0]);
    //    SPLITresidueContributionOrganicCarbon_kg_per_ha[0] = (float)((1 - incorporationRate) *
    // surfaceResidueWeight +
    //       incorporationRate * surfaceResidueWeight *
    //       relevantThickness / incorporationDepth);
    //
    //    // note the <= condition...
    //    for (int layerIndex = 1; layerIndex <= firstLayerBelowResidueIncorporationDepth;
    // layerIndex++) {
    //
    //      relevantThickness = Math.min(incorporationDepth - SPLITlayerDepthAtBase_SLB[layerIndex -
    // 1], SPLITlayerThicknessCM[0]);
    //
    //      SPLITresidueContributionOrganicCarbon_kg_per_ha[layerIndex] = (float)(
    //           incorporationRate * surfaceResidueWeight *
    //           relevantThickness / incorporationDepth);
    //    }

    // let's put the root mass evenly across only the puddling layers...
    warningString += "* putting root mass evenly across puddled layers" + newlineToUse;
    SPLITresidueContributionOrganicCarbon_kg_per_ha[0] +=
        rootWeight
            * (SPLITlayerThicknessCM[0] / SPLITlayerDepthAtBase_SLB[nPuddledLayersToUse - 1]);
    for (int layerIndex = 1; layerIndex < nPuddledLayersToUse; layerIndex++) {
      SPLITresidueContributionOrganicCarbon_kg_per_ha[layerIndex] +=
          rootWeight
              * (SPLITlayerThicknessCM[layerIndex])
              / SPLITlayerDepthAtBase_SLB[nPuddledLayersToUse - 1];
    }

    // do the final assignment of nitrogen things and whatever else remains...
    for (int layerIndex = 0; layerIndex < nSPLITSoilLayers; layerIndex++) {
      // update the organic carbon numbers with the residue/root contributions
      SPLITtotalOrganicCarbon_kg_per_ha[layerIndex] =
          SPLITbaseOrganicCarbon_kg_per_ha[layerIndex]
              + SPLITresidueContributionOrganicCarbon_kg_per_ha[layerIndex];
      // we are basing organic nitrogen off of the organic carbon numbers. also, we are
      // putting residues in here...
      SPLITorganicNitrogen_kg_per_ha[layerIndex] =
          (float)
              (irrigatedOrganicCarbonToOrganicNitrogenConverter(
                      SPLITbaseOrganicCarbon_kg_per_ha[layerIndex],
                      SPLITlayerDepthAtBase_SLB[layerIndex])
                  + (residueNitrogenPercent / 100)
                      * SPLITresidueContributionOrganicCarbon_kg_per_ha[layerIndex]);

      //            (float) (organicCarbonToNitrogenConversionFactor *
      // SPLITbaseOrganicCarbon_kg_per_ha[layerIndex] +
    }

    // and finally, the joy of the mineralized nitrogen....
    // we have previously decided in global futures to assume the ammonia floats away and is always
    // zero
    // and everything shows up as nitrates...

    // Beware the MAGIC ASSUMPTION!!!
    // we are going to take all of nitrogen as nitrates...

    // find total mass of soil in these layers per ha
    double totalSoilMassGrams = 0.0;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // bulk density is g/cm^3
      // total mass in grams... density * depth * length * width
      if (SPLITlayerDepthAtBase_SLB[topLayerIndex] <= depthForNitrogen) {
        //        System.out.println("A: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // keep all of this layer
        totalSoilMassGrams +=
            SPLITbulkDensity_SBDM[topLayerIndex]
                * SPLITlayerThicknessCM[topLayerIndex]
                * (100 * 100)
                * (100 * 100);
        //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*100) * (100*100);
      } else {
        //        System.out.println("B: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        totalSoilMassGrams +=
            SPLITbulkDensity_SBDM[topLayerIndex]
                * (depthForNitrogen - SPLITlayerDepthAtBase_SLB[topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100);
      }
    }

    double fractionalContributionOfLayer = -325;
    //    double nitrogenMassHereGrams = -5;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // this layer mass / total active layers mass
      if (SPLITlayerDepthAtBase_SLB[topLayerIndex] <= depthForNitrogen) {
        // keep all of this layer
        fractionalContributionOfLayer =
            SPLITbulkDensity_SBDM[topLayerIndex]
                * SPLITlayerThicknessCM[topLayerIndex]
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
      } else {
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        fractionalContributionOfLayer =
            SPLITbulkDensity_SBDM[topLayerIndex]
                * (depthForNitrogen - SPLITlayerDepthAtBase_SLB[topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
      }
      // ppm = total nitrogen mass / total soil mass * 10^6;
      // we want to use the whole layer, even for the deepest one
      //      nitrogenMassHereGrams = fractionalContributionOfLayer * totalInitialNitrogenKgPerHa *
      // 1000;
      SPLITnitrateAmountKgPerHa[topLayerIndex] =
          (float) (fractionalContributionOfLayer * totalInitialNitrogenKgPerHa);
      //  this is a ppm computation; oryza wants kg/ha so far as i can tell...
      //  SPLITnitrateAmount[topLayerIndex] = (float) (
      //      nitrogenMassHereGrams / (SPLITbulkDensity_SBDM[topLayerIndex] *
      //      SPLITlayerThicknessCM[topLayerIndex] * (100*10) * (100*10) ) * 1000000
      //      );
      //      System.out.println(topLayerIndex + " fContr = " + fractionalContributionOfLayer);
    }

    /*
    for (int layerIndex = 0; layerIndex < nSPLITSoilLayers; layerIndex++) {
      System.out.println(layerIndex + " -> depth = " + SPLITlayerDepthAtBase_SLB[layerIndex] +
          " ; thic = " + SPLITlayerThicknessCM[layerIndex] +
          " ; c = " + SPLITclay_SLCL[layerIndex] +
          " ; s = " + SPLITsilt_SLSI[layerIndex] +
          " ;\tBD = " + SPLITbulkDensity_SBDM[layerIndex] +
          " ; SOC% = " + SPLITorganicCarbon_SLOC[layerIndex] +
          " ; baseOC = " + SPLITbaseOrganicCarbon_kg_per_ha[layerIndex] +
          " ; residue = " + SPLITresidueContributionOrganicCarbon_kg_per_ha[layerIndex] +
          " ;\ttot OC = " + SPLITtotalOrganicCarbon_kg_per_ha[layerIndex] +
          " ; guess ON = " + SPLITorganicNitrogen_kg_per_ha[layerIndex] +
          " ; NO4 = " + SPLITnitrateAmountKgPerHa[layerIndex] +
          " ; pH = " + SPLITpH[layerIndex]
      );
    }

    */

    /// take a little break to do some hydraulic parameters ///

    double C1,
        s1,
        s2,
        W1,
        W2,
        F3,
        F2,
        F1,
        F0,
        B1,
        VVV,
        wcstx,
        KSTX,
        Clay2,
        Silt2,
        om2,
        BD2,
        TOP2,
        WCST1,
        VGA1,
        VGN1,
        VGL1,
        KS1;
    //    double T;

    double C2 =
        1.0; // a magic number having to do with compacting (1.0 means normal, bigger means smashed
    // together, lower means fluffy/loose)
    //    double IEC = 0.0; // a magic number that i don't know yet... but it actually needs to be
    // zero at the moment
    // apparently it warns whether soil organic matter is available in the temporary file. but that
    // stuff never gets used, so i am cutting it out...

    double[] BD = new double[nSPLITSoilLayers];
    double[] WCST = new double[nSPLITSoilLayers];
    double[] WCFC = new double[nSPLITSoilLayers];
    double[] WCWP = new double[nSPLITSoilLayers];
    //    double[] WCFC1 = new double[nSPLITSoilLayers];
    double[] A = new double[nSPLITSoilLayers];
    double[] B = new double[nSPLITSoilLayers];
    double[] N = new double[nSPLITSoilLayers];
    double[] WCAD = new double[nSPLITSoilLayers];
    double[] KST = new double[nSPLITSoilLayers];
    double[] Top = new double[nSPLITSoilLayers];

    double[] KSTagain = new double[nSPLITSoilLayers];
    double[] VGA = new double[nSPLITSoilLayers];
    double[] VGL = new double[nSPLITSoilLayers];
    double[] VGN = new double[nSPLITSoilLayers];
    //    double[] WCADagain = new double[nSoilLayers];

    double[] WCSTreal = new double[nSPLITSoilLayers];
    double[] KSTreal = new double[nSPLITSoilLayers];

    warningString += "*     ---> Warning: assuming that compaction is: " + C2 + newlineToUse;

    // initialize "Top"; this is actually a set of flags indicating whether the
    // layer is puddled or not
    // we are working off the idea that arrays are constructed as being filled with zeros to start
    for (int layerIndex = 0; layerIndex < nPuddledLayersToUse; layerIndex++) {
      Top[layerIndex] = 1.0;
    }
    for (int layerIndex = 0; layerIndex < nSPLITSoilLayers; layerIndex++) {

      // let us try to compute the wilting point denoted as F3
      // Beware the MAGIC NUMBER!!! convert to fraction from percentage
      C1 = SPLITclay_SLCL[layerIndex] / 100.0; // Clay(I);
      // C2 = COMPACT(I);
      s1 = (100.0 - (SPLITclay_SLCL[layerIndex] + SPLITsilt_SLSI[layerIndex])) / 100.0; // Sand(I);
      s2 = SPLITorganicCarbon_SLOC[layerIndex]; // SOM(I);

      // ''CALCULATE WILTING POINT F3
      W1 =
          -0.024 * s1
              + 0.487 * C1
              + 0.006 * s2
              + 0.005 * (s1 * s2)
              - 0.013 * (C1 * s2)
              + 0.068 * (s1 * C1)
              + 0.031;
      W2 = W1 + (0.14 * W1 - 0.02);
      F3 = W2;

      // and now, the field capacity as F2
      // ''CALCULATE FIELD CAPACITY F2
      W1 =
          -0.251 * s1
              + 0.195 * C1
              + 0.011 * s2
              + 0.006 * (s1 * s2)
              - 0.027 * (C1 * s2)
              + 0.452 * (s1 * C1)
              + 0.299;
      W2 = W1 + (1.283 * (W1 * W1) - 0.374 * (W1) - 0.015);
      F2 = W2;

      // water content between saturated and field capacity
      // 'CALCULATE WATER CONTENT BETWEEN SATURATED AND FIELD CAPACITY F1
      W1 =
          0.278 * s1
              + 0.034 * C1
              + 0.022 * s2
              - 0.018 * (s1 * s2)
              - 0.027 * (C1 * s2)
              - 0.584 * (s1 * C1)
              + 0.078;
      W2 = W1 + (0.636 * W1 - 0.107);
      F1 = W2;

      // air entry pressure as T
      // 'CALCULATE AIR ENTRY PRESSURE T
      //      W1 = -21.67 * s1 - 27.93 * C1 - 81.97 * F1 + 71.12 * (s1 * F1) + 8.29 * (C1 * F1) +
      // 14.05
      // * (s1 * C1) + 27.16;
      //      W2 = W1 + (0.02 * (W1 * W1) - 0.113 * W1 - 0.7);
      //      T = W2;

      // saturated water content as F0
      // 'CALCULATE SATURATED WATER CONTENT F0
      F0 = F2 + F1 - 0.097 * s1 + 0.043;

      // bulk density as B1
      // 'CALCULATE BULK DENSITY B1
      B1 = (1.0 - F0) * 2.65;

      // make adjustements for compact stuff
      // 'ADJUSTED BY COMPACT FACTOR
      // If C2 <> 1# Then
      if (C2 != 1.0) {
        BD[layerIndex] = B1 * C2;
        WCST[layerIndex] = 1.0 - BD[layerIndex] / 2.65;
        WCFC[layerIndex] = WCST[layerIndex] - (F0 - F2) / C2;
        // WCST(I) = F2 + XXX - 0.097 * s1 + 0.043;
        WCWP[layerIndex] = WCFC[layerIndex] - (F2 - F3) / C2; //   ' - 0.2 * (WCST(I) - F1)
      } else {
        BD[layerIndex] = B1;
        WCST[layerIndex] = F0;
        WCFC[layerIndex] = F2;
        WCWP[layerIndex] = F3;
      }

      // 'Readjust the WCFC, WCST, WCWP if WCFC or WCWP is 0 or negative
      if (WCFC[layerIndex] <= 0.02 || WCWP[layerIndex] <= 0.01) {
        WCFC[layerIndex] = WCST[layerIndex] * 0.65;
        WCWP[layerIndex] = Math.max(WCFC[layerIndex] - 0.15, WCFC[layerIndex] * 0.5);
      }

      // something having to do with saturated and field capacities
      // '!! THE DIFFERENT BETWEEN SATURATED AND FIELD CAPACITY
      //      WCFC1[layerIndex] = WCST[layerIndex] - WCFC[layerIndex];

      B[layerIndex] =
          (Math.log(1500) - Math.log(33))
              / (Math.log(WCFC[layerIndex]) - Math.log(WCWP[layerIndex]));
      A[layerIndex] = Math.exp(Math.log(33) + B[layerIndex] * Math.log(WCFC[layerIndex]));

      // some more stuff whose utility i do not understand
      // '!CALCULATE MOISTURE-CONDUCTIVITY PARAMETER N, KST
      N[layerIndex] = 1.0 / B[layerIndex];

      //      WCAD[layerIndex] = (1000000.0 / A[layerIndex]) ^ (-1 / B[layerIndex])
      WCAD[layerIndex] =
          Math.pow((1000000.0 / A[layerIndex]), -N[layerIndex]); // (-1 / B[layerIndex]);

      // it appears that we are checking for proper ordering or something here...
      if (WCST[layerIndex] == WCFC[layerIndex]) {
        WCFC[layerIndex] = WCWP[layerIndex] + (WCST[layerIndex] - WCWP[layerIndex]) * 0.85;
      } else if (WCST[layerIndex] < WCFC[layerIndex]) {
        VVV = WCFC[layerIndex];
        WCFC[layerIndex] = WCST[layerIndex];
        WCST[layerIndex] = VVV;
      }

      if (WCFC[layerIndex] == WCWP[layerIndex]) {
        WCWP[layerIndex] = WCFC[layerIndex] - (WCFC[layerIndex] - WCAD[layerIndex]) * 0.5;
      } else if (WCFC[layerIndex] < WCWP[layerIndex]) {
        VVV = WCWP[layerIndex];
        WCWP[layerIndex] = WCFC[layerIndex];
        WCFC[layerIndex] = VVV;
      }

      // and on to the next few mysterious statements
      KST[layerIndex] =
          1930.0 * Math.pow((WCST[layerIndex] - WCFC[layerIndex]), (3.0 - N[layerIndex]));

      // '!calculate ?water content parameters [original says: van Genuchten parameters]
      wcstx = WCST[layerIndex];
      KSTX = KST[layerIndex];

      // '!calculate van Genuchten parameters
      Clay2 = C1;
      Silt2 = 1.0 - C1 - s1;
      om2 = s2;
      BD2 = BD[layerIndex];
      TOP2 = Top[layerIndex];

      // Beware the MAGIC NUMBER!!! the replacement values for negatives...
      // fix some small number issues
      if (om2 <= 0.0) {
        om2 = 0.0001;
      }
      if (Clay2 <= 0.0) {
        Clay2 = 0.02;
      }
      if (Silt2 <= 0.0) {
        Silt2 = 0.02;
      }

      // undo a lot of what we've already done. :)
      Clay2 = Clay2 * 100.0;
      Silt2 = Silt2 * 100.0;
      om2 = Math.min(10.0, om2 * 100.0);

      WCST1 =
          0.7919
              + 0.001691 * Clay2
              - 0.29619 * BD2
              - 0.000001491 * Silt2 * Silt2
              + 0.0000821 * om2 * om2
              + 0.02427 / Clay2
              + 0.01113 / Silt2
              + 0.01472 * Math.log(Silt2)
              - 0.0000733 * om2 * Clay2
              - 0.000619 * BD2 * Clay2
              - 0.001183 * BD2 * om2
              - 0.0001664 * TOP2 * Silt2;

      VGA1 =
          -14.96
              + 0.03135 * Clay2
              + 0.0351 * Silt2
              + 0.646 * om2
              + 15.26 * BD2
              - 0.192 * TOP2
              - 4.671 * BD2 * BD2
              - 0.000781 * Clay2 * Clay2
              - 0.00687 * om2 * om2
              + 0.0449 / om2
              + 0.0663 * Math.log(Silt2)
              + 0.1482 * Math.log(om2)
              - 0.04546 * BD2 * Silt2
              - 0.4852 * BD2 * om2
              + 0.00673 * TOP2 * Clay2;

      VGN1 =
          -25.23
              - 0.02195 * Clay2
              + 0.0074 * Silt2
              - 0.194 * om2
              + 45.5 * BD2
              - 7.24 * BD2 * BD2
              + 0.0003658 * Clay2 * Clay2
              + 0.002885 * om2 * om2
              - 12.81 / BD2
              - 0.1524 / Silt2
              - 0.01958 / om2
              - 0.2876 * Math.log(Silt2)
              - 0.0709 * Math.log(om2)
              - 44.6 * Math.log(BD2)
              - 0.02264 * BD2 * Clay2
              + 0.0896 * BD2 * om2
              + 0.00718 * TOP2 * Clay2;

      VGL1 =
          0.0202
              + 0.0006193 * Clay2 * Clay2
              - 0.001136 * om2 * om2
              - 0.2316 * Math.log(om2)
              - 0.03544 * BD2 * Clay2
              + 0.00283 * BD2 * Silt2
              + 0.0488 * BD2 * om2;

      KS1 =
          7.755
              + 0.0352 * Silt2
              + 0.93 * TOP2
              - 0.967 * BD2 * BD2
              - 0.000484 * Clay2 * Clay2
              - 0.000322 * Silt2 * Silt2
              + 0.001 / Silt2
              - 0.0748 / om2
              - 0.0643 * Math.log(Silt2)
              - 0.01398 * BD2 * Clay2
              - 0.1673 * BD2 * om2
              + 0.02986 * TOP2 * Clay2
              - 0.03305 * TOP2 * Silt2;

      KSTagain[layerIndex] = Math.exp(KS1);
      VGA[layerIndex] = Math.exp(VGA1);
      VGL[layerIndex] = 10.0 * (Math.exp(VGL1) - 1.0) / (Math.exp(VGL1) + 1.0);
      VGN[layerIndex] = Math.exp(VGN1) + 1.0;
      //      WCADagain[layerIndex] = Math.pow((1000000.0 / A[layerIndex]) , (-N[layerIndex]));
      //      WCAD     [layerIndex] = Math.pow((1000000.0 / A[layerIndex]) , -N[layerIndex]); // (-1
      // /
      // B[layerIndex]);

      if (useWaterContentParameter) {
        // If WaterM = 2 Then    'use water content parameter
        WCSTreal[layerIndex] = wcstx;
        KSTreal[layerIndex] = KSTX;
      } else {
        // presumable WaterM = 1 'use van whoever
        WCSTreal[layerIndex] = WCST1;
        KSTreal[layerIndex] = KSTagain[layerIndex];
      }
    } // end for layerIndex

    /// END: take a little break to do some hydraulic parameters ///

    // get a text version of the original profile so we can
    // put it in as a comment...
    String profileString = this.dumpSingleProfile(profileName);
    String[] profileStringAsArray = profileString.split("\n");
    String commentedProfileString = "";
    for (int arrayIndex = 0; arrayIndex < profileStringAsArray.length; arrayIndex++) {
      commentedProfileString += "* " + profileStringAsArray[arrayIndex] + newlineToUse;
    }

    String oryzaString = "";

    oryzaString +=
        "* this is an oryza-style soil profile meant to mimic the following DSSAT soil profile:"
            + newlineToUse;
    oryzaString += "* the original filename was: " + this.originalFileName + newlineToUse;
    oryzaString += "*" + newlineToUse;
    oryzaString += commentedProfileString;
    oryzaString += "*" + newlineToUse;

    // the plan here will be to just step through the file and do the work in order as needed...

    oryzaString +=
        "* Give code name of soil data file to match the water balance PADDY:" + newlineToUse;
    oryzaString += "SCODE = 'PADDY'" + newlineToUse;
    oryzaString +=
        "*---------------------------------------------------------------*" + newlineToUse;
    oryzaString += "* 1. Various soil and management parameters" + newlineToUse;
    oryzaString +=
        "*---------------------------------------------------------------*" + newlineToUse;
    oryzaString += "WL0MX = 100.0    ! Bund height (mm)" + newlineToUse;
    oryzaString +=
        "NL = "
            + nSPLITSoilLayers
            + "     ! Number of soil layers (maximum is 10) (-)"
            + newlineToUse;
    // ok, i think what we need is the thickness of each layer in meters....
    oryzaString += "TKL = ";
    // note that we are doing the first layer by hand... this is to avoid messing with
    // getting the right number of delimiters...
    // Beware the MAGIC NUMBER!!! 100.0 cm per meter
    oryzaString += (SPLITlayerDepthAtBase_SLB[0]) / 100.0;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString +=
          ","
              + (SPLITlayerDepthAtBase_SLB[layerIndex] - SPLITlayerDepthAtBase_SLB[layerIndex - 1])
                  / 100.0;
    }
    oryzaString += "   ! ? layer thickness in meters" + newlineToUse;

    // maximum rooting depth is the bottom of the bottom layer
    oryzaString +=
        "ZRTMS = "
            + SPLITlayerDepthAtBase_SLB[nSPLITSoilLayers - 1] / 100.0
            + "  ! Maximum rooting depth in the soil (m)"
            + newlineToUse;

    oryzaString +=
        "*---------------------------------------------------------------*" + newlineToUse;
    oryzaString += "* 2. Puddling switch: 1=PUDDLED or 0=NON PUDDLED" + newlineToUse;
    oryzaString +=
        "*---------------------------------------------------------------*" + newlineToUse;
    if (doPuddling) {
      oryzaString += "*SWITPD = 0 ! Non puddled" + newlineToUse;
      oryzaString += "SWITPD = 1  ! Puddled" + newlineToUse;
    } else {
      oryzaString += "SWITPD = 0 ! Non puddled" + newlineToUse;
      oryzaString += "*SWITPD = 1  ! Puddled" + newlineToUse;
    }

    // all the stuff for puddling which might be unnecessary if it weren't chosen that way
    // but, we'll put it in anyway....
    oryzaString += "* If PUDDLED, supply parameters for puddled soil" + newlineToUse;
    oryzaString +=
        "NLPUD = "
            + nPuddledLayersToUse
            + " ! Number of puddled soil layers, including the plow sole (-)"
            + newlineToUse;
    oryzaString += "! (NLPUD cannot exceed the total number of soil layers NL)" + newlineToUse;

    oryzaString += "WCSTRP = " + WCSTreal[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + WCSTreal[layerIndex];
    }
    oryzaString += newlineToUse;

    oryzaString +=
        "* Soil water tension of puddled soil layer at which cracks reach" + newlineToUse;
    oryzaString += "* break through the plow sole (pF):" + newlineToUse;
    oryzaString += "PFCR = 6." + newlineToUse;

    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 3. Groundwater switch: 0=DEEP (i.e., not in profile), 1=DATA"
            + newlineToUse
            + "* (supplied), 2=CALCULATE"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "*SWITGW = 0 ! Deep groundwater"
            + newlineToUse
            + "*SWITGW = 2 ! Calculate groundwater"
            + newlineToUse
            + "SWITGW = 1 ! Groundwater data"
            + newlineToUse
            + "* If DATA, supply table of groundwater table depth (cm; Y-value)"
            + newlineToUse
            + "* as function of calendar day (d; X value):"
            + newlineToUse
            + "ZWTB =   1.,500.,"
            + newlineToUse
            + "366.,500."
            + newlineToUse
            + "* If CALCULATE, supply the following parameters:"
            + newlineToUse
            + "ZWTBI = 100. ! Initial groundwater table depth (cm)"
            + newlineToUse
            + "MINGW = 100. ! Minimum groundwater table depth (cm)"
            + newlineToUse
            + "MAXGW = 100. ! Maximum groundwater table depth (cm)"
            + newlineToUse
            + "ZWA   = 1.0  ! Receding rate of groundwater with no recharge (cm d-1)"
            + newlineToUse
            + "ZWB   = 0.5  ! Sensitivity factor of groundwater recharge (-)"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 4. Percolation switch"
            + newlineToUse
            + "* Value for SWITVP can not be 1 (CALCULATE) for nonpuddled soil"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "SWITVP = -1 ! Fixed percolation rate"
            + newlineToUse
            + "*SWITVP = 0 ! Percolation as function of the groundwater depth"
            + newlineToUse
            + "*SWITVP = 1 ! Calculate percolation"
            + newlineToUse
            + "*SWITVP = 2 ! Fixed percolation rate as function of time"
            + newlineToUse;

    oryzaString += "* If SWITVP = -1, supply fixed percolation rate (mm d-1):" + newlineToUse;
    oryzaString += "FIXPERC = " + KST[nSPLITSoilLayers - 1] + newlineToUse;

    oryzaString +=
        "* If SWITVP = 0, supply table of percolation rate (mm d-1; Y-value)"
            + newlineToUse
            + "* as function of water table depth (cm; X value):"
            + newlineToUse
            + "*PERTB =   0., 3.,"
            + newlineToUse
            + "*         200., 3."
            + newlineToUse
            + "* If SWITVP = 2, give percolation rate (mm/d) as function of calendar day"
            + newlineToUse
            + "PTABLE ="
            + newlineToUse
            + "1.,  1.0,   !First number is calendar day, second is percolation rate)"
            + newlineToUse
            + "50., 1.0,"
            + newlineToUse
            + "100., 20.0,"
            + newlineToUse
            + "366., 20.0"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 5. Conductivity switch: 0=NO DATA, 1=VAN GENUCHTEN or 2=POWER"
            + newlineToUse
            + "*  OR 3= SPAW  function used"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse;

    if (useWaterContentParameter) {
      // If WaterM = 2 Then    'use water content parameter
      oryzaString += "SWITKH = 0 ! No data" + newlineToUse;
    } else {
      // presumably WaterM = 1 'use van whoever
      oryzaString += "SWITKH  = 1 ! van Genuchten" + newlineToUse;
    }
    warningString +=
        "* for section 5 (conductivity switch), this translator is not using option SWITKH = 2 !"
            + " Power"
            + newlineToUse;

    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 6. Water retention switch: 0=DATA; 1=VAN GENUCHTEN. When DATA, data"
            + newlineToUse
            + "* have to be supplied for saturation, field capacity,"
            + newlineToUse
            + "* wilting point and at air dryness"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse;

    if (useWaterContentParameter) {
      // If WaterM = 2 Then    'use water content parameter
      oryzaString += "SWITPF = 0 ! Data" + newlineToUse;
    } else {
      // presumably WaterM = 1 'use van whoever
      oryzaString += "SWITPF  = 1 ! van Genuchten" + newlineToUse;
    }

    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "*7a.Soil physical properties, these parameters will be used when model"
            + newlineToUse
            + "*runs under actual water or nitrogen condition, or even both. Otherwise"
            + newlineToUse
            + "*these parameter will not be used."
            + newlineToUse;

    // doing the first one manually to save effort...
    // CLAY
    oryzaString += "CLAYX = ";
    oryzaString += (SPLITclay_SLCL[0] / 100.0);
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + (SPLITclay_SLCL[layerIndex] / 100.0);
    }
    oryzaString += newlineToUse;

    // SAND
    oryzaString += "SANDX = ";
    oryzaString += (100.0 - SPLITclay_SLCL[0] - SPLITsilt_SLSI[0]) / 100.0;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + (100.0 - SPLITclay_SLCL[0] - SPLITsilt_SLSI[0]) / 100.0;
    }
    oryzaString += newlineToUse;

    // BULK DENSITY
    oryzaString += "BD = ";
    oryzaString += SPLITbulkDensity_SBDM[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + SPLITbulkDensity_SBDM[0];
    }
    oryzaString += newlineToUse;

    // CARBON, NITROGEN, pH, etc.
    oryzaString += "*Soil organic carbon and nitrogen content in kg C or N/ha" + newlineToUse;

    // carbon itself
    oryzaString += "SOC = ";
    oryzaString += SPLITtotalOrganicCarbon_kg_per_ha[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + SPLITtotalOrganicCarbon_kg_per_ha[layerIndex];
    }
    oryzaString += newlineToUse;

    // organic nitrogen
    oryzaString += "SON = ";
    oryzaString += SPLITorganicNitrogen_kg_per_ha[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + SPLITorganicNitrogen_kg_per_ha[layerIndex];
    }
    oryzaString += newlineToUse;

    // mineral ammonia
    // Beware the MAGIC ASSUMPTION!!! ammonia is always zero....
    oryzaString += "SNH4X = ";
    oryzaString += 0.0;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + 0.0;
    }
    oryzaString += newlineToUse;

    // mineral nitrates
    oryzaString += "SNO3X = ";
    oryzaString += SPLITnitrateAmountKgPerHa[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + SPLITnitrateAmountKgPerHa[layerIndex];
    }
    oryzaString += newlineToUse;

    // pH
    oryzaString += "SpH = ";
    oryzaString += SPLITpH[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + SPLITpH[layerIndex];
    }
    oryzaString += newlineToUse;

    // HYDRAULIC PROPERTIES....
    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 7b. Soil hydrological properties. Required type of data input"
            + newlineToUse
            + "* according to setting of conductivity and water retention switch"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "* Saturated hydraulic conductivity, for each soil layer"
            + newlineToUse
            + "* (cm d-1) (always required!):"
            + newlineToUse;

    // KST
    oryzaString += "KST = ";
    oryzaString += KSTreal[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + KSTreal[layerIndex];
    }
    oryzaString += newlineToUse;

    oryzaString +=
        "* Saturated volumetric water content, for each soil layer"
            + newlineToUse
            + "* (m3 m-3)(always required!):"
            + newlineToUse;

    // WCST
    oryzaString += "WCST = ";
    oryzaString += WCSTreal[0];
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + WCSTreal[layerIndex];
    }
    oryzaString += newlineToUse;

    if (!useWaterContentParameter) {
      // we doing van person
      oryzaString +=
          "* Van Genuchten parameters, for each soil layer"
              + newlineToUse
              + "* (needed if SWITKH = 1 and/or SWITPF = 1):"
              + newlineToUse;

      // VGA
      oryzaString += "VGA = ";
      oryzaString += VGA[0];
      for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += "," + VGA[layerIndex];
      }
      oryzaString += newlineToUse;

      // VGL
      oryzaString += "VGL = ";
      oryzaString += VGL[0];
      for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += "," + VGL[layerIndex];
      }
      oryzaString += newlineToUse;

      // VGN
      oryzaString += "VGN = ";
      oryzaString += VGN[0];
      for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += "," + VGN[layerIndex];
      }
      oryzaString += newlineToUse;

      // VGR: looks suspicious
      float magicOneHundredth = 0.01F;
      oryzaString += "* VGR is a magic number..." + newlineToUse;
      //      oryzaString += "VGR = " + (nSPLITSoilLayers * magicOneHundredth) + "   ! suspicious: "
      // +
      // nSPLITSoilLayers + " * " + magicOneHundredth + newlineToUse;
      oryzaString += "VGR = ";
      for (int layerIndex = 0; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += magicOneHundredth + " ";
      }
      oryzaString += newlineToUse;

    } else {
      // WCFC
      oryzaString += "WCFC = ";
      oryzaString += WCFC[0];
      for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += "," + WCFC[layerIndex];
      }
      oryzaString += newlineToUse;

      // WCWP
      oryzaString += "WCWP = ";
      oryzaString += WCWP[0];
      for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
        oryzaString += "," + WCWP[layerIndex];
      }
      oryzaString += newlineToUse;

      // WCAD: looks suspicious
      float magicOneHundredth = 0.01F;
      oryzaString +=
          "WCAD = "
              + (nSPLITSoilLayers * magicOneHundredth)
              + "   ! suspicious: "
              + nSPLITSoilLayers
              + " * "
              + magicOneHundredth
              + newlineToUse;
    }

    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 8. Initialization conditions, and re-initialization"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "WL0I = 10.   ! Initial ponded water depth at start of simulation (mm)"
            + newlineToUse
            + "* Initial volumetric water content at the start of simulation,"
            + newlineToUse
            + "* for each soil layer (m3 m-3):  USE ALWAYS FIELD CAPACITY, OR 0.5 TIMES WCST"
            + newlineToUse
            + "*   *** *** altering to match global futures style *** ***"
            + newlineToUse;

    oryzaString += "WCLI = ";
    double bottomWaterShare = SPLITlowerLimit_SLLL[0];
    double topWaterShare = SPLITdrainedUpperLimit_SDUL[0];

    double initialWaterShare =
        topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
            + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);

    oryzaString += initialWaterShare;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      bottomWaterShare = SPLITlowerLimit_SLLL[layerIndex];
      topWaterShare = SPLITdrainedUpperLimit_SDUL[layerIndex];

      initialWaterShare =
          topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
              + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);
      oryzaString += "," + initialWaterShare;
    }
    oryzaString += newlineToUse;

    // and we'll put in tao's way just in case we are interested...
    // WCST with comments
    oryzaString += "*WCLI = ";
    oryzaString += WCST[0] * 0.95;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      if (layerIndex == nSPLITSoilLayers - 1) {
        oryzaString += "," + WCST[layerIndex] * 0.98;
      } else {
        oryzaString += "," + WCST[layerIndex] * 0.95;
      }
    }
    oryzaString += newlineToUse;

    // an unknown option of some sort...
    if (doPuddling) {
      oryzaString +=
          "RIWCLI = 'YES' ! make initial soil water not very important and force water availability"
              + " at (trans)planting"
              + newlineToUse;
    } else {
      oryzaString += "RIWCLI = 'NO' ! make initial soil water matter more" + newlineToUse;
    }

    // soil thermal conditions, oh great...
    oryzaString +=
        "*---------------------------------------------------------------*"
            + newlineToUse
            + "* 9. Initialization of soil thermal conditions"
            + newlineToUse
            + "*---------------------------------------------------------------*"
            + newlineToUse
            + "SATAV = "
            + annualAverageTopSoilTemperature
            + "       !Soil annual avaerage temperature of the first layers"
            + newlineToUse;

    // a brute force thing for starting soil temperatures: start at 25 and decrease by one per
    // layer...
    oryzaString +=
        "* super simple initial temperatures... go look at the source code." + newlineToUse;
    oryzaString += "SOILT = ";

    float magicTopTemperature = (float) startingMonthTopSoilTemperature;
    oryzaString += magicTopTemperature;
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      oryzaString += "," + (magicTopTemperature - layerIndex);
    }
    oryzaString += newlineToUse;

    // and some other unknown thing:
    oryzaString += "WCLINT = ";

    oryzaString += "1,1,1";
    for (int layerIndex = 1; layerIndex < nSPLITSoilLayers; layerIndex++) {
      for (int sillyCounter = 0; sillyCounter < 3; sillyCounter++) {
        oryzaString += "," + (layerIndex + 1);
      }
    }
    oryzaString += newlineToUse;
    oryzaString += "*" + newlineToUse;

    // finish out with whatever notes we have accumulated along the way....
    oryzaString += warningString;

    return oryzaString;
  }

  public String convertProfileToAPSIM(
      String officialCropName,
      String profileName,
      double fractionBetweenLowerLimitAndDrainedUpperLimit,
      double totalInitialNitrogenKgPerHa,
      double depthForNitrogen,
      double rootWeight,
      double surfaceResidueWeight,
      double residueNitrogenPercent,
      double[] standardDepths,
      double[] clayStableFractionAbove,
      double[] loamStableFractionAbove,
      double[] sandStableFractionAbove,
      String newlineToUse)
      throws Exception {
    //    double incorporationRate,
    //    double incorporationDepth,

    // some magic numbers....
    double magicShallowKLdepthCM = 15.0;
    double magicShallowKLvalue = 0.1;
    double magicDeepKLdepthCM = 200.0;
    double magicDeepKLvalue = 0.0;
    double KLvalueToUse = -1;

    double magicKLslope =
        (magicDeepKLvalue - magicShallowKLvalue) / (magicDeepKLdepthCM - magicShallowKLdepthCM);

    String soilBlock = "";

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

    // put in the opener
    soilBlock += "<soil name=\"" + profileName + "\">" + newlineToUse;

    ///////////////////
    // water portion //
    ///////////////////
    //    <Layer>
    //    <Thickness units="mm">150</Thickness>
    //    <KS units="mm/day" />
    //    <BD units="g/cc">1.02</BD>
    //    <AirDry units="mm/mm">0.15</AirDry>
    //    <LL15 units="mm/mm">0.29</LL15>
    //    <DUL units="mm/mm">0.54</DUL>
    //    <SAT units="mm/mm">0.59</SAT>
    //    </Layer>
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock +=
          "<BD units=\"g/cc\">"
              + bulkDensity_SBDM[soilProfileIndex][layerIndex]
              + "</BD>"
              + newlineToUse;
      soilBlock +=
          "<LL15 units=\"mm/mm\">"
              + lowerLimit_SLLL[soilProfileIndex][layerIndex]
              + "</LL15>"
              + newlineToUse;
      soilBlock +=
          "<DUL units=\"mm/mm\">"
              + drainedUpperLimit_SDUL[soilProfileIndex][layerIndex]
              + "</DUL>"
              + newlineToUse;
      soilBlock +=
          "<SAT units=\"mm/mm\">"
              + saturatedUpperLimit_SSAT[soilProfileIndex][layerIndex]
              + "</SAT>"
              + newlineToUse;

      // 0.95 comes from an approximation from Dean Holzworth LL - (0.05 * LL)
      soilBlock +=
          "<AirDry units=\"mm/mm\">"
              + (0.95 * lowerLimit_SLLL[soilProfileIndex][layerIndex])
              + "</AirDry>"
              + newlineToUse;

      // this is needed, but may be left empty
      soilBlock += "<KS units=\"mm/day\" />" + newlineToUse;
      soilBlock += "</Layer>" + newlineToUse;
    }

    // now the crop-specific thing which is actually going to just be defaulted, but
    // we have to name names regardless.
    soilBlock += "<SoilCrop name=\"" + officialCropName + "\">" + newlineToUse;
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock +=
          "<LL units=\"mm/mm\">"
              + lowerLimit_SLLL[soilProfileIndex][layerIndex]
              + "</LL>"
              + newlineToUse;

      // ok, this has something to do with water extraction. we are going to do something silly to
      // try
      // to be roughly like some of the values previously seen in example files.
      //
      // in particular, i'm going to assume that it is 0.1 to a depth of 0.15 meters
      // then it will go linearly to 0.0 at 2.0 meters.
      //
      // our two points then are (15.0cm, 0.1) and (200.0cm, 0.0)
      // m = -0.1 / 185;

      if (this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex] <= magicShallowKLdepthCM) {
        KLvalueToUse = magicShallowKLvalue;
      } else {
        KLvalueToUse =
            magicKLslope
                    * (this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex] - magicDeepKLdepthCM)
                + magicDeepKLvalue;
      }
      soilBlock += "<KL units=\"/day\">" + KLvalueToUse + "</KL>" + newlineToUse;

      // we will just go with a default here and assume full root penetrability
      soilBlock += "<XF units=\"0-1\">1</XF>" + newlineToUse;
      soilBlock += "</Layer>" + newlineToUse;
    }
    soilBlock += "</SoilCrop>" + newlineToUse;

    // water stuff...
    soilBlock += "<SoilWat>" + newlineToUse;

    soilBlock += "<SummerCona>3.5</SummerCona>" + newlineToUse;
    soilBlock += "<SummerU>6</SummerU>" + newlineToUse;
    soilBlock += "<SummerDate>1-Nov</SummerDate>" + newlineToUse;
    soilBlock += "<WinterCona>3.5</WinterCona>" + newlineToUse;
    soilBlock += "<WinterU>6</WinterU>" + newlineToUse;
    soilBlock += "<WinterDate>1-Apr</WinterDate>" + newlineToUse;

    soilBlock += "<DiffusConst>40</DiffusConst>" + newlineToUse;
    soilBlock += "<DiffusSlope>16</DiffusSlope>" + newlineToUse;

    soilBlock += "<Salb>" + this.albedo_SALB[soilProfileIndex] + "</Salb>" + newlineToUse; // 0.13

    soilBlock +=
        "<Cn2Bare>" + this.runoffCurve_SLRO[soilProfileIndex] + "</Cn2Bare>" + newlineToUse;

    // keep these defaults
    soilBlock += "<CnRed>20</CnRed>" + newlineToUse;
    soilBlock += "<CnCov>0.8</CnCov>" + newlineToUse;

    soilBlock += "<Slope>" + newlineToUse;
    soilBlock += "</Slope>" + newlineToUse;
    soilBlock += "<DischargeWidth>" + newlineToUse;
    soilBlock += "</DischargeWidth>" + newlineToUse;
    soilBlock += "<CatchmentArea>" + newlineToUse;
    soilBlock += "</CatchmentArea>" + newlineToUse;
    soilBlock += "<MaxPond>" + newlineToUse;
    soilBlock += "</MaxPond>" + newlineToUse;

    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock += "<SWCON units=\"0-1\">" + 0.3 + "</SWCON>" + newlineToUse;
      soilBlock += "<MWCON units=\"0-1\" />" + newlineToUse;
      soilBlock += "<KLAT units=\"mm/d\" />" + newlineToUse;

      soilBlock += "</Layer>" + newlineToUse;
    }
    soilBlock += "</SoilWat>" + newlineToUse;

    // soil organic matter
    //    double fractionBetweenLowerLimitAndDrainedUpperLimit,
    //    double totalInitialNitrogenKgPerHa,
    //    double depthForNitrogen,
    //    double rootWeight,
    //    double residueNitrogenPercent,

    soilBlock += "<SoilOrganicMatter>" + newlineToUse;
    double rootCarbonToNitrogenRatio = 40.0; // Beware the MAGIC NUMBER!!!
    soilBlock += "<RootCn>" + rootCarbonToNitrogenRatio + "</RootCn>" + newlineToUse;
    soilBlock += "<RootWt>" + rootWeight + "</RootWt>" + newlineToUse;
    double soilCarbonToNitrogenRatio = 12.5; // Beware the MAGIC NUMBER!!!
    soilBlock += "<SoilCn>" + soilCarbonToNitrogenRatio + "</SoilCn>" + newlineToUse;

    // something to do with erosion; leave as is.
    // Beware the MAGIC NUMBER!!!
    soilBlock += "<EnrACoeff>7.4</EnrACoeff>" + newlineToUse;
    soilBlock += "<EnrBCoeff>0.2</EnrBCoeff>" + newlineToUse;

    double[] stableFractionToUse = null;

    int nStandardDepths = standardDepths.length;

    // first, we have to figure out what the soil type is...
    // Beware the MAGIC ASSUMPTION!!!
    // also we're basing this on the top layer...
    double clayThreshold =
        40; // anything above this percentage will use the clay carbon assumptions
    double sandThreshold =
        30; // anything above this percentage will use the sand carbon assumptions
    if (this.clay_SLCL[soilProfileIndex][0] > clayThreshold) {
      //      System.out.println("   clay: " + this.clay_SLCL[profileIndex][0]);
      stableFractionToUse = clayStableFractionAbove;
    } else if (100 - this.clay_SLCL[soilProfileIndex][0] - this.silt_SLSI[soilProfileIndex][0]
        > sandThreshold) {
      //      System.out.println("   sand: " + (100 - this.clay_SLCL[profileIndex][0] -
      // this.silt_SLSI[profileIndex][0]));
      stableFractionToUse = sandStableFractionAbove;
    } else {
      stableFractionToUse = loamStableFractionAbove;
    }

    double[] stableCarbonPercentage = new double[nSoilLayers];
    int firstStandardLayerBelowTarget = -1;
    double weightForBottom = -2;
    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      //      soilDepth =
      // FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex], 5);

      // find first standard depth that is below what we need
      firstStandardLayerBelowTarget = -1;
      for (int standardIndex = 0; standardIndex < nStandardDepths; standardIndex++) {
        if (standardDepths[standardIndex]
            >= this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex]) {
          firstStandardLayerBelowTarget = standardIndex;
          break;
        }
      }

      // determine weighting between the layer just below and the layer just above

      if (firstStandardLayerBelowTarget == 0) {
        // this whole thing gets treated like the top...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[soilProfileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // top)");
      } else if (firstStandardLayerBelowTarget == nStandardDepths) {
        // this whole thing gets treated like the bottom...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[soilProfileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // bottom)");
      } else {
        weightForBottom =
            (standardDepths[firstStandardLayerBelowTarget]
                    - this.layerDepthAtBase_SLB[soilProfileIndex][layerIndex])
                / (standardDepths[firstStandardLayerBelowTarget]
                    - standardDepths[firstStandardLayerBelowTarget - 1]);

        stableCarbonPercentage[layerIndex] =
            organicCarbon_SLOC[soilProfileIndex][layerIndex]
                * (weightForBottom * stableFractionToUse[firstStandardLayerBelowTarget]
                    + (1.0 - weightForBottom)
                        * stableFractionToUse[firstStandardLayerBelowTarget - 1]);
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // middle)");
        //        System.out.println("wFB = " + weightForBottom);
        //        System.out.println("1 - wFB = " + (1.0 - weightForBottom));
        //        System.out.println("upper frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget]);
        //        System.out.println("lower frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget
        // - 1]);

      }
    }

    double veryUnstableCarbonFraction = -8;

    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock +=
          "<OC units=\"Total %\">"
              + organicCarbon_SLOC[soilProfileIndex][layerIndex]
              + "</OC>"
              + newlineToUse;

      // Beware the MAGIC NUMBER!!! 5% of the unstable carbon is allocated to "really unstable"
      veryUnstableCarbonFraction =
          0.05
              * (organicCarbon_SLOC[soilProfileIndex][layerIndex]
                  - stableCarbonPercentage[layerIndex])
              / 100;
      soilBlock += "<FBiom units=\"0-1\">" + veryUnstableCarbonFraction + "</FBiom>" + newlineToUse;
      soilBlock +=
          "<FInert units=\"0-1\">"
              + stableCarbonPercentage[layerIndex] / 100
              + "</FInert>"
              + newlineToUse;
      soilBlock += "</Layer>" + newlineToUse;
    }
    soilBlock += "</SoilOrganicMatter>" + newlineToUse;

    soilBlock += "<Analysis>" + newlineToUse;

    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock += "<Rocks units=\"%\" />" + newlineToUse;
      soilBlock += "<Texture />" + newlineToUse;
      soilBlock += "<MunsellColour />" + newlineToUse;
      soilBlock += "<EC units=\"1:5 dS/m\" />" + newlineToUse;
      soilBlock +=
          "<PH units=\"1:5 water\">"
              + this.phInWater_SLHW[soilProfileIndex][layerIndex]
              + "</PH>"
              + newlineToUse;
      soilBlock += "<CL units=\"mg/kg\" />" + newlineToUse;
      soilBlock += "<Boron units=\"Hot water mg/kg\" />" + newlineToUse;
      soilBlock += "<CEC units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<Ca units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<Mg units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<Na units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<K units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<ESP units=\"%\" />" + newlineToUse;
      soilBlock += "<Mn units=\"mg/kg\" />" + newlineToUse;
      soilBlock += "<Al units=\"cmol+/kg\" />" + newlineToUse;
      soilBlock += "<ParticleSizeSand units=\"%\" />" + newlineToUse;
      soilBlock += "<ParticleSizeSilt units=\"%\" />" + newlineToUse;
      soilBlock += "<ParticleSizeClay units=\"%\" />" + newlineToUse;
      soilBlock += "</Layer>" + newlineToUse;
    }

    soilBlock += "</Analysis>" + newlineToUse;

    soilBlock += "<Sample name=\"Initial nitrogen\">" + newlineToUse;
    soilBlock += "<Date type=\"date\" description=\"Sample date:\" />" + newlineToUse;

    double bottomWaterShare = -5;
    double topWaterShare = -6;

    double initialWaterShare = -7;

    // Beware the MAGIC ASSUMPTION!!!
    // we are going to take all of nitrogen as nitrates...
    double ammoniumAmount = 0.0;
    double nitrateAmount[] = new double[nSoilLayers];

    // we need to determine which layers are appropriate here....
    // find first layer that is under the part where we want to distribute it
    int firstLayerBelowNitroDepth = -1;
    for (int realLayerIndex = 0; realLayerIndex < nSoilLayers; realLayerIndex++) {
      if (layerDepthAtBase_SLB[soilProfileIndex][realLayerIndex] >= depthForNitrogen) {
        firstLayerBelowNitroDepth = realLayerIndex;
        break;
      }
    }

    // find total mass of soil in these layers per ha
    double totalSoilMassGrams = 0.0;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // bulk density is g/cm^3
      // total mass in grams... density * depth * length * width
      if (this.layerDepthAtBase_SLB[soilProfileIndex][topLayerIndex] <= depthForNitrogen) {
        //        System.out.println("A: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // keep all of this layer
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[soilProfileIndex][topLayerIndex]
                * this.layerThicknessCM[soilProfileIndex][topLayerIndex]
                * (100 * 100)
                * (100 * 100);
        //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*100) * (100*100);
      } else {
        //        System.out.println("B: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[soilProfileIndex][topLayerIndex]
                * (depthForNitrogen
                    - this.layerDepthAtBase_SLB[soilProfileIndex][topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100);
      }
    }

    // decide how much nitrogen should go in each layer
    // based on the fraction of contribution to the mass of soil...
    double nitrogenMassHereGrams = -105;
    double fractionalContributionOfLayer = -325;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // this layer mass / total active layers mass
      if (this.layerDepthAtBase_SLB[soilProfileIndex][topLayerIndex] <= depthForNitrogen) {
        // keep all of this layer
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[soilProfileIndex][topLayerIndex]
                * this.layerThicknessCM[soilProfileIndex][topLayerIndex]
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
        //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*10) * (100*10) /
        // totalSoilMassGrams;
      } else {
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[soilProfileIndex][topLayerIndex]
                * (depthForNitrogen
                    - this.layerDepthAtBase_SLB[soilProfileIndex][topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
      }
      // ppm = total nitrogen mass / total soil mass * 10^6;
      // we want to use the whole layer, even for the deepest one
      nitrogenMassHereGrams = fractionalContributionOfLayer * totalInitialNitrogenKgPerHa * 1000;
      nitrateAmount[topLayerIndex] =
          nitrogenMassHereGrams
              / (this.bulkDensity_SBDM[soilProfileIndex][topLayerIndex]
                  * this.layerThicknessCM[soilProfileIndex][topLayerIndex]
                  * (100 * 100)
                  * (100 * 100))
              * 1000000;
      //      this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*10) * (100*10) ) *
      // 1000000;

      //      System.out.println("DSSAT: " + topLayerIndex + " fContr = " +
      // fractionalContributionOfLayer);

    }

    for (int layerIndex = 0; layerIndex < nSoilLayers; layerIndex++) {
      //      soilDepth =
      // FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);
      bottomWaterShare = lowerLimit_SLLL[soilProfileIndex][layerIndex];
      topWaterShare = drainedUpperLimit_SDUL[soilProfileIndex][layerIndex];

      initialWaterShare =
          topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
              + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);

      soilBlock += "<Layer>" + newlineToUse;
      soilBlock +=
          "<Thickness units=\"mm\">"
              + layerThicknessCM[soilProfileIndex][layerIndex] * 10
              + "</Thickness>"
              + newlineToUse;
      soilBlock += "<NO3 units=\"ppm\">" + nitrateAmount[layerIndex] + "</NO3>" + newlineToUse;
      soilBlock += "<NH4 units=\"ppm\">" + ammoniumAmount + "</NH4>" + newlineToUse;
      soilBlock += "<SW units=\"mm/mm\">" + initialWaterShare + "</SW>" + newlineToUse;
      soilBlock += "</Layer>" + newlineToUse;
    }
    soilBlock += "</Sample>" + newlineToUse;

    // put in the closer
    soilBlock += "</soil>" + newlineToUse;

    soilBlock += "<surfaceom name=\"SurfaceOrganicMatter\">" + newlineToUse;
    soilBlock +=
        "<PoolName type=\"text\" description=\"Organic Matter pool name\">"
            + officialCropName
            + "</PoolName>"
            + newlineToUse;
    soilBlock +=
        "<type type=\"list\" listvalues=\"bambatsi,barley,base_type,broccoli,"
            + "camaldulensis,canola,centro,chickpea,chikenmanure_base,cm,cmA,cmB,constants,"
            + "cotton,cowpea,danthonia,fababean,fieldpea,fym,gbean,globulus,goatmanure,"
            + "grandis,grass,horsegram,inert,lablab,lentil,lucerne,lupin,maize,manB,manure,"
            + "medic,millet,mucuna,nativepasture,navybean,oats,orobanche,peanut,pigeonpea,"
            + "potato,rice,sorghum,soybean,stylo,sugar,sunflower,sweetcorn,sweetsorghum,"
            + "tillage,tithonia,vetch,weed,wheat\" description=\"Organic Matter type\">"
            + officialCropName
            + "</type>"
            + newlineToUse;
    soilBlock +=
        "<mass type=\"text\" description=\"Initial surface residue (kg/ha)\">"
            + surfaceResidueWeight
            + "</mass>"
            + newlineToUse;
    // Beware the MAGIC ASSUMPTION!!! we're just going to keep some of these ratios and such
    soilBlock +=
        "<cnr type=\"text\" description=\"C:N ratio of initial residue\">80</cnr>" + newlineToUse;
    soilBlock +=
        "<standing_fraction type=\"text\" description=\"Fraction of residue"
            + " standing\">0</standing_fraction>"
            + newlineToUse;
    soilBlock += "</surfaceom>" + newlineToUse;

    return soilBlock;
  }

  public String dumpAllProfilesAsString() throws Exception {
    String allString = "";

    for (int profileIndex = 0; profileIndex < nProfiles; profileIndex++) {
      allString += dumpSingleProfile(profileNames[profileIndex]) + "\n";
    }

    return allString;
  }

  public String OLDNOTHICKNESSmakeInitializationAndSoilAnalysisBlock(
      String profileName,
      double fractionBetweenLowerLimitAndDrainedUpperLimit,
      String startingDateCode,
      double totalInitialNitrogenKgPerHa,
      double depthForNitrogen,
      double rootWeight,
      double surfaceResidueWeight,
      double residueNitrogenPercent,
      double incorporationRate,
      double incorporationDepth,
      double[] standardDepths,
      double[] clayStableFractionAbove,
      double[] loamStableFractionAbove,
      double[] sandStableFractionAbove)
      throws Exception {

    /*
        ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME
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
        "*INITIAL CONDITIONS"
            + "\n"
            + "@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME"
            + "\n"
            + " 1   -99 "
            + startingDateCode
            + " "
            + FunTricks.fitInNCharacters(rootWeight, 5)
            + " "
            + "  -99   -99   -99   -99 "
            + FunTricks.fitInNCharacters(surfaceResidueWeight, 5)
            + " "
            + FunTricks.fitInNCharacters(residueNitrogenPercent, 5)
            + " "
            + "-99.0 "
            + FunTricks.fitInNCharacters(incorporationRate, 5)
            + " "
            + FunTricks.fitInNCharacters(incorporationDepth, 5)
            + " to reflect some basic previous crop"
            + "\n"
            + "@C  ICBL  SH2O  SNH4  SNO3"
            + "\n";

    String soilDepth = null;
    double bottomWaterShare = -1;
    double topWaterShare = -2;
    double initialWaterShare = -3;

    int nLayers = this.getNLayersInProfile(profileIndex);

    // Beware the MAGIC ASSUMPTION!!!
    // we are going to take all of nitrogen as nitrates...
    double ammoniumAmount = 0.0;
    double nitrateAmount[] = new double[nLayers];

    // we need to determine which layers are appropriate here....
    // find first layer that is under the part where we want to distribute it
    int firstLayerBelowNitroDepth = -1;
    for (int realLayerIndex = 0; realLayerIndex < nLayers; realLayerIndex++) {
      if (layerDepthAtBase_SLB[profileIndex][realLayerIndex] >= depthForNitrogen) {
        firstLayerBelowNitroDepth = realLayerIndex;
        break;
      }
    }

    // find total mass of soil in these layers per ha
    double totalSoilMassGrams = 0.0;
    //    double totalSoilMassGramsCompletelyAboveDesiredZone = 0.0;
    for (int topLayerIndex = 0; topLayerIndex < firstLayerBelowNitroDepth; topLayerIndex++) {
      // bulk density is g/cm^3
      // total mass in grams... density * depth * length * width
      //      totalSoilMassGrams += this.bulkDensity_SBDM[profileIndex][topLayerIndex] *
      //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*10) * (100*10);
      if (this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] <= depthForNitrogen) {
        //        System.out.println("A: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // keep all of this layer
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * this.layerDepthAtBase_SLB[profileIndex][topLayerIndex]
                * (100 * 100)
                * (100 * 100);
      } else {
        //        System.out.println("B: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * (depthForNitrogen - this.layerDepthAtBase_SLB[profileIndex][topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100);
      }
    }

    // decide how much nitrogen should go in each layer
    // based on the fraction of contribution to the mass of soil...
    double nitrogenMassHereGrams = -105;
    double fractionalContributionOfLayer = -325;
    for (int topLayerIndex = 0; topLayerIndex < firstLayerBelowNitroDepth; topLayerIndex++) {
      // this layer mass / total active layers mass
      if (this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] <= depthForNitrogen) {
        // keep all of this layer
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * this.layerDepthAtBase_SLB[profileIndex][topLayerIndex]
                * (100 * 10)
                * (100 * 10)
                / totalSoilMassGrams;
      } else {
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * (depthForNitrogen - this.layerDepthAtBase_SLB[profileIndex][topLayerIndex - 1])
                * (100 * 10)
                * (100 * 10)
                / totalSoilMassGrams;
      }
      // ppm = total nitrogen mass / total soil mass * 10^6;
      // we want to use the whole layer, even for the deepest one
      nitrogenMassHereGrams = fractionalContributionOfLayer * totalInitialNitrogenKgPerHa * 1000;
      nitrateAmount[topLayerIndex] =
          nitrogenMassHereGrams
              / (this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                  * this.layerDepthAtBase_SLB[profileIndex][topLayerIndex]
                  * (100 * 10)
                  * (100 * 10))
              * 1000000;
    }

    //    System.out.println("-- init nLayers = " + nLayers + " BF = " +
    // layerDepthAtBase_SLB[profileIndex].length + " --");
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      soilDepth =
          FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);
      bottomWaterShare = this.lowerLimit_SLLL[profileIndex][layerIndex];
      topWaterShare = this.drainedUpperLimit_SDUL[profileIndex][layerIndex];

      initialWaterShare =
          topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
              + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);

      outputString +=
          " 1 "
              + soilDepth
              + " "
              + FunTricks.fitInNCharacters(initialWaterShare, 5)
              + " "
              + FunTricks.fitInNCharacters(ammoniumAmount, 5)
              + " "
              + FunTricks.fitInNCharacters(nitrateAmount[layerIndex], 5)
              + "\n";
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
                                          rhizobia effectiveness
                                                water table depth
                                                     residue weight
                                                            residue nitrogen content percent
                                                                 residue phosphorus content percent
                                                                       residue incorporation fraction
                                                                             residue incorporation depth
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

    // and now the soil analysis block....
    // the point here is to set the initial stable carbon...

    /*
        *SOIL ANALYSIS
        @A SADAT  SMHB  SMPX  SMKE  SANAME
         1 16091   -99   -99   -99  -99
        @A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC
           base layer
                 bulk density
                       total organic carbon
                             total nigrogetn
                                   pH in water
                                        pH in buffer
                                               phosphorus extractable
                                                     potassium exchangable
                                                           stable soil carbon
        SA_block_bottom_goes_here

    */

    ////////////////////////////////
    // Beware the MAGIC NUMBER!!! //
    // stable carbon assumptions  //
    ////////////////////////////////

    // add in a couple blank lines to separate before the soil analysis block

    outputString +=
        "\n\n"
            + "*SOIL ANALYSIS"
            + "\n"
            + "@A SADAT  SMHB  SMPX  SMKE  SANAME"
            + "\n"
            + " 1 "
            + startingDateCode
            + "   -99   -99   -99  -99"
            + "\n"
            + "@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC"
            + "\n";

    // double[] standardDepths, double[] clayStableFractionAbove, double[] loamStableFractionAbove,
    // double[] sandStableFractionAbove

    //    double[] standardDepths  =         {   20,   40,   60,  1000000000};
    //    double[] clayStableFractionAbove = { 0.80, 0.96, 0.98,  0.98};
    //    double[] loamStableFractionAbove = { 0.80, 0.98, 0.98,  0.98};
    //    double[] sandStableFractionAbove = { 0.93, 0.98, 0.98,  0.98};

    double[] stableFractionToUse = null;

    int nStandardDepths = standardDepths.length;

    // first, we have to figure out what the soil type is...
    // Beware the MAGIC ASSUMPTION!!!
    // also we're basing this on the top layer...
    double clayThreshold =
        40; // anything above this percentage will use the clay carbon assumptions
    double sandThreshold =
        30; // anything above this percentage will use the sand carbon assumptions
    if (this.clay_SLCL[profileIndex][0] > clayThreshold) {
      //      System.out.println("   clay: " + this.clay_SLCL[profileIndex][0]);
      stableFractionToUse = clayStableFractionAbove;
    } else if (100 - this.clay_SLCL[profileIndex][0] - this.silt_SLSI[profileIndex][0]
        > sandThreshold) {
      //      System.out.println("   sand: " + (100 - this.clay_SLCL[profileIndex][0] -
      // this.silt_SLSI[profileIndex][0]));
      stableFractionToUse = sandStableFractionAbove;
    } else {
      stableFractionToUse = loamStableFractionAbove;
    }

    double[] stableCarbonPercentage = new double[nLayers];
    int firstStandardLayerBelowTarget = -1;
    double weightForBottom = -2;
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      soilDepth =
          FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);

      // find first standard depth that is below what we need
      firstStandardLayerBelowTarget = -1;
      for (int standardIndex = 0; standardIndex < nStandardDepths; standardIndex++) {
        if (standardDepths[standardIndex] >= this.layerDepthAtBase_SLB[profileIndex][layerIndex]) {
          firstStandardLayerBelowTarget = standardIndex;
          break;
        }
      }

      // determine weighting between the layer just below and the layer just above

      if (firstStandardLayerBelowTarget == 0) {
        // this whole thing gets treated like the top...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // top)");
      } else if (firstStandardLayerBelowTarget == nStandardDepths) {
        // this whole thing gets treated like the bottom...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // bottom)");
      } else {
        weightForBottom =
            (standardDepths[firstStandardLayerBelowTarget]
                    - this.layerDepthAtBase_SLB[profileIndex][layerIndex])
                / (standardDepths[firstStandardLayerBelowTarget]
                    - standardDepths[firstStandardLayerBelowTarget - 1]);

        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * (weightForBottom * stableFractionToUse[firstStandardLayerBelowTarget]
                    + (1.0 - weightForBottom)
                        * stableFractionToUse[firstStandardLayerBelowTarget - 1]);
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // middle)");
        //        System.out.println("wFB = " + weightForBottom);
        //        System.out.println("1 - wFB = " + (1.0 - weightForBottom));
        //        System.out.println("upper frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget]);
        //        System.out.println("lower frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget
        // - 1]);

      }

      outputString +=
          " 1 "
              + FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(this.bulkDensity_SBDM[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(this.organicCarbon_SLOC[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(stableCarbonPercentage[layerIndex], 5)
              + "\n";

      // decide what our value is going to be based on those weights
    }

    /*
        *SOIL ANALYSIS
        @A SADAT  SMHB  SMPX  SMKE  SANAME
         1 16091   -99   -99   -99  -99
        @A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC
           base layer
                 bulk density
                       total organic carbon
                             total nigrogetn
                                   pH in water
                                        pH in buffer
                                               phosphorus extractable
                                                     potassium exchangable
                                                           stable soil carbon
        SA_block_bottom_goes_here

    */

    return outputString;
  }

  public String makeInitializationAndSoilAnalysisBlock(
      String profileName,
      double fractionBetweenLowerLimitAndDrainedUpperLimit,
      String startingDateCode,
      double totalInitialNitrogenKgPerHa,
      double depthForNitrogen,
      double rootWeight,
      double surfaceResidueWeight,
      double residueNitrogenPercent,
      double incorporationRate,
      double incorporationDepth,
      double[] standardDepths,
      double[] clayStableFractionAbove,
      double[] loamStableFractionAbove,
      double[] sandStableFractionAbove)
      throws Exception {

    /*
        ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME
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
        "*INITIAL CONDITIONS"
            + "\n"
            + "@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME"
            + "\n"
            + " 1   -99 "
            + startingDateCode
            + " "
            + FunTricks.fitInNCharacters(rootWeight, 5)
            + " "
            + "  -99   -99   -99   -99 "
            + FunTricks.fitInNCharacters(surfaceResidueWeight, 5)
            + " "
            + FunTricks.fitInNCharacters(residueNitrogenPercent, 5)
            + " "
            + "-99.0 "
            + FunTricks.fitInNCharacters(incorporationRate, 5)
            + " "
            + FunTricks.fitInNCharacters(incorporationDepth, 5)
            + " to reflect some basic previous crop"
            + "\n"
            + "@C  ICBL  SH2O  SNH4  SNO3"
            + "\n";

    String soilDepth = null;
    double bottomWaterShare = -1;
    double topWaterShare = -2;
    double initialWaterShare = -3;

    int nLayers = this.getNLayersInProfile(profileIndex);

    // Beware the MAGIC ASSUMPTION!!!
    // we are going to take all of nitrogen as nitrates...
    double ammoniumAmount = 0.0;
    double nitrateAmount[] = new double[nLayers];

    // we need to determine which layers are appropriate here....
    // find first layer that is under the part where we want to distribute it
    int firstLayerBelowNitroDepth = -1;
    for (int realLayerIndex = 0; realLayerIndex < nLayers; realLayerIndex++) {
      if (layerDepthAtBase_SLB[profileIndex][realLayerIndex] >= depthForNitrogen) {
        firstLayerBelowNitroDepth = realLayerIndex;
        break;
      }
    }

    // find total mass of soil in these layers per ha
    double totalSoilMassGrams = 0.0;
    //    double totalSoilMassGramsCompletelyAboveDesiredZone = 0.0;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // bulk density is g/cm^3
      // total mass in grams... density * depth * length * width
      if (this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] <= depthForNitrogen) {
        //        System.out.println("A: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // keep all of this layer
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * this.layerThicknessCM[profileIndex][topLayerIndex]
                * (100 * 100)
                * (100 * 100);
        //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*100) * (100*100);
      } else {
        //        System.out.println("B: lI = " + topLayerIndex + "; lD = " +
        // layerDepthAtBase_SLB[profileIndex][topLayerIndex] + "; dFN = " + depthForNitrogen);
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        totalSoilMassGrams +=
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * (depthForNitrogen - this.layerDepthAtBase_SLB[profileIndex][topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100);
      }
    }

    // decide how much nitrogen should go in each layer
    // based on the fraction of contribution to the mass of soil...
    double nitrogenMassHereGrams = -105;
    double fractionalContributionOfLayer = -325;
    // NOTE: nonstandard condition of <=
    for (int topLayerIndex = 0; topLayerIndex <= firstLayerBelowNitroDepth; topLayerIndex++) {
      // this layer mass / total active layers mass
      if (this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] <= depthForNitrogen) {
        // keep all of this layer
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * this.layerThicknessCM[profileIndex][topLayerIndex]
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
        //        this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*10) * (100*10) /
        // totalSoilMassGrams;
      } else {
        // count only the top part that we need...
        // so we key off of the previous layer's depth...
        fractionalContributionOfLayer =
            this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                * (depthForNitrogen - this.layerDepthAtBase_SLB[profileIndex][topLayerIndex - 1])
                * (100 * 100)
                * (100 * 100)
                / totalSoilMassGrams;
      }
      // ppm = total nitrogen mass / total soil mass * 10^6;
      // we want to use the whole layer, even for the deepest one
      nitrogenMassHereGrams = fractionalContributionOfLayer * totalInitialNitrogenKgPerHa * 1000;
      nitrateAmount[topLayerIndex] =
          nitrogenMassHereGrams
              / (this.bulkDensity_SBDM[profileIndex][topLayerIndex]
                  * this.layerThicknessCM[profileIndex][topLayerIndex]
                  * (100 * 100)
                  * (100 * 100))
              * 1000000;
      //      this.layerDepthAtBase_SLB[profileIndex][topLayerIndex] * (100*10) * (100*10) ) *
      // 1000000;

      //      System.out.println("DSSAT: " + topLayerIndex + " fContr = " +
      // fractionalContributionOfLayer);

    }

    //    System.out.println("-- init nLayers = " + nLayers + " BF = " +
    // layerDepthAtBase_SLB[profileIndex].length + " --");
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      soilDepth =
          FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);
      bottomWaterShare = this.lowerLimit_SLLL[profileIndex][layerIndex];
      topWaterShare = this.drainedUpperLimit_SDUL[profileIndex][layerIndex];

      initialWaterShare =
          topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
              + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);

      outputString +=
          " 1 "
              + soilDepth
              + " "
              + FunTricks.fitInNCharacters(initialWaterShare, 5)
              + " "
              + FunTricks.fitInNCharacters(ammoniumAmount, 5)
              + " "
              + FunTricks.fitInNCharacters(nitrateAmount[layerIndex], 5)
              + "\n";
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
                                          rhizobia effectiveness
                                                water table depth
                                                     residue weight
                                                            residue nitrogen content percent
                                                                 residue phosphorus content percent
                                                                       residue incorporation fraction
                                                                             residue incorporation depth
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

    // and now the soil analysis block....
    // the point here is to set the initial stable carbon...

    /*
        *SOIL ANALYSIS
        @A SADAT  SMHB  SMPX  SMKE  SANAME
         1 16091   -99   -99   -99  -99
        @A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC
           base layer
                 bulk density
                       total organic carbon
                             total nigrogetn
                                   pH in water
                                        pH in buffer
                                               phosphorus extractable
                                                     potassium exchangable
                                                           stable soil carbon
        SA_block_bottom_goes_here

    */

    ////////////////////////////////
    // Beware the MAGIC NUMBER!!! //
    // stable carbon assumptions  //
    ////////////////////////////////

    // add in a couple blank lines to separate before the soil analysis block

    outputString +=
        "\n\n"
            + "*SOIL ANALYSIS"
            + "\n"
            + "@A SADAT  SMHB  SMPX  SMKE  SANAME"
            + "\n"
            + " 1 "
            + startingDateCode
            + "   -99   -99   -99  -99"
            + "\n"
            + "@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC"
            + "\n";

    // double[] standardDepths, double[] clayStableFractionAbove, double[] loamStableFractionAbove,
    // double[] sandStableFractionAbove

    //    double[] standardDepths  =         {   20,   40,   60,  1000000000};
    //    double[] clayStableFractionAbove = { 0.80, 0.96, 0.98,  0.98};
    //    double[] loamStableFractionAbove = { 0.80, 0.98, 0.98,  0.98};
    //    double[] sandStableFractionAbove = { 0.93, 0.98, 0.98,  0.98};

    double[] stableFractionToUse = null;

    int nStandardDepths = standardDepths.length;

    // first, we have to figure out what the soil type is...
    // Beware the MAGIC ASSUMPTION!!!
    // also we're basing this on the top layer...
    double clayThreshold =
        40; // anything above this percentage will use the clay carbon assumptions
    double sandThreshold =
        30; // anything above this percentage will use the sand carbon assumptions
    if (this.clay_SLCL[profileIndex][0] > clayThreshold) {
      //      System.out.println("   clay: " + this.clay_SLCL[profileIndex][0]);
      stableFractionToUse = clayStableFractionAbove;
    } else if (100 - this.clay_SLCL[profileIndex][0] - this.silt_SLSI[profileIndex][0]
        > sandThreshold) {
      //      System.out.println("   sand: " + (100 - this.clay_SLCL[profileIndex][0] -
      // this.silt_SLSI[profileIndex][0]));
      stableFractionToUse = sandStableFractionAbove;
    } else {
      stableFractionToUse = loamStableFractionAbove;
    }

    double[] stableCarbonPercentage = new double[nLayers];
    int firstStandardLayerBelowTarget = -1;
    double weightForBottom = -2;
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      soilDepth =
          FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);

      // find first standard depth that is below what we need
      firstStandardLayerBelowTarget = -1;
      for (int standardIndex = 0; standardIndex < nStandardDepths; standardIndex++) {
        if (standardDepths[standardIndex] >= this.layerDepthAtBase_SLB[profileIndex][layerIndex]) {
          firstStandardLayerBelowTarget = standardIndex;
          break;
        }
      }

      // determine weighting between the layer just below and the layer just above

      if (firstStandardLayerBelowTarget == 0) {
        // this whole thing gets treated like the top...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // top)");
      } else if (firstStandardLayerBelowTarget == nStandardDepths) {
        // this whole thing gets treated like the bottom...
        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * stableFractionToUse[firstStandardLayerBelowTarget];
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // bottom)");
      } else {
        weightForBottom =
            (standardDepths[firstStandardLayerBelowTarget]
                    - this.layerDepthAtBase_SLB[profileIndex][layerIndex])
                / (standardDepths[firstStandardLayerBelowTarget]
                    - standardDepths[firstStandardLayerBelowTarget - 1]);

        stableCarbonPercentage[layerIndex] =
            this.organicCarbon_SLOC[profileIndex][layerIndex]
                * (weightForBottom * stableFractionToUse[firstStandardLayerBelowTarget]
                    + (1.0 - weightForBottom)
                        * stableFractionToUse[firstStandardLayerBelowTarget - 1]);
        //        System.out.println("fSLBT = " + firstStandardLayerBelowTarget + " (supposed to be
        // middle)");
        //        System.out.println("wFB = " + weightForBottom);
        //        System.out.println("1 - wFB = " + (1.0 - weightForBottom));
        //        System.out.println("upper frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget]);
        //        System.out.println("lower frac = " +
        // stableFractionToUse[firstStandardLayerBelowTarget
        // - 1]);

      }

      outputString +=
          " 1 "
              + FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(this.bulkDensity_SBDM[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(this.organicCarbon_SLOC[profileIndex][layerIndex], 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(-99, 5)
              + " "
              + FunTricks.fitInNCharacters(stableCarbonPercentage[layerIndex], 5)
              + "\n";

      // decide what our value is going to be based on those weights
    }

    /*
        *SOIL ANALYSIS
        @A SADAT  SMHB  SMPX  SMKE  SANAME
         1 16091   -99   -99   -99  -99
        @A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC
           base layer
                 bulk density
                       total organic carbon
                             total nigrogetn
                                   pH in water
                                        pH in buffer
                                               phosphorus extractable
                                                     potassium exchangable
                                                           stable soil carbon
        SA_block_bottom_goes_here

    */

    return outputString;
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
        "*INITIAL CONDITIONS"
            + "\n"
            + "@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME"
            + "\n"
            + " 1   -99 "
            + startingDateCode
            + "   -99   -99   -99   -99 -99.0   -99   -99   -99   -99   -99 -99"
            + "\n"
            + "@C  ICBL  SH2O  SNH4  SNO3"
            + "\n";

    String soilDepth = null;
    double bottomWaterShare = -1;
    double topWaterShare = -2;
    double initialWaterShare = -3;

    // Beware the MAGIC ASSUMPTION!!!
    // we are going to partition the desired total nitrogen
    // via a rule of thumb into 90/10 for nitrate/ammonium
    double magicFractionToAmmonium = 0.1;
    double ammoniumAmount = magicFractionToAmmonium * totalNitrogenPPMforBothNH4NO2;
    double nitrateAmount = (1.0 - magicFractionToAmmonium) * totalNitrogenPPMforBothNH4NO2;

    int nLayers = this.getNLayersInProfile(profileIndex);
    //    System.out.println("-- init nLayers = " + nLayers + " BF = " +
    // layerDepthAtBase_SLB[profileIndex].length + " --");
    for (int layerIndex = 0; layerIndex < nLayers; layerIndex++) {
      soilDepth =
          FunTricks.fitInNCharacters(this.layerDepthAtBase_SLB[profileIndex][layerIndex], 5);
      bottomWaterShare = this.lowerLimit_SLLL[profileIndex][layerIndex];
      topWaterShare = this.drainedUpperLimit_SDUL[profileIndex][layerIndex];

      initialWaterShare =
          topWaterShare * (fractionBetweenLowerLimitAndDrainedUpperLimit)
              + bottomWaterShare * (1 - fractionBetweenLowerLimitAndDrainedUpperLimit);

      outputString +=
          " 1 "
              + soilDepth
              + " "
              + FunTricks.fitInNCharacters(initialWaterShare, 5)
              + " "
              + FunTricks.fitInNCharacters(ammoniumAmount, 5)
              + " "
              + FunTricks.fitInNCharacters(nitrateAmount, 5)
              + "\n";
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

  public float[][] getLayerThicknessCM() {
    return layerThicknessCM;
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
