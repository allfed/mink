package org.DSSATRunner;

//import java.io.*;

import java.io.File;

import org.R2Useful.*;

public class ChangeThicknessesAndOrganicContentOfSoilProfiles {

    // the idea here is to read in a soil profiles file and make a new set of
    // profiles that has been modified. we are starting with the idea of changing the
    // water properties. later, we may wish to change the organic carbon content and
    // that kind of thing

    public static void main(String commandLineOptions[]) throws Exception {

	////////////////////
	// magical things //
	////////////////////
	
	int maxNameLength = 10; // the maximum length for the profile name. it might actually be 12, but i don't know...

	////////////////////////////////////////////
	// handle the command line arguments...
	////////////////////////////////////////////

	String originalSoilFile        = commandLineOptions[0];
	String newSoilFile             = commandLineOptions[1];
	Float  thicknessMultiplier     = Float.parseFloat(    commandLineOptions[2]);
	Float  organicCarbonMultiplier = Float.parseFloat(    commandLineOptions[3]);

	
	if (thicknessMultiplier < 0) {
	    System.out.println("thickness multiplier must be non-negative (i.e., not " + thicknessMultiplier + ")");
	    throw new Exception();
	}

	if (organicCarbonMultiplier < 0) {
	    System.out.println("organic carbon multiplier must be non-negative (i.e., not " + organicCarbonMultiplier + ")");
	    throw new Exception();
	}

	// all right: let's try to build the original soil profile object
	
	SoilProfileModifyExisting collectionOfProfiles = new SoilProfileModifyExisting(originalSoilFile);

	// and we will need to rename the soil profiles. traditionally, the first two letters of
	// the soil profile name match the first two letters of the soil file name (which should be ??.SOL)
	// ok, and i will brute force some path stripping...
	File newSoilFileObject = new File(newSoilFile);
	String firstTwoLettersOffileNameWithoutDirectories = newSoilFileObject.getName().substring(0, 2);
	
//	String newFirstTwoLetters = newSoilFile.lastIndexOf(originalSoilFile);
	
	// grab the old profile name, build the new name, check whether it already exists...
	String[] oldProfileNames = collectionOfProfiles.getProfileNames();

		// now, we want to just go through each profile and modify the water holding parameters
	String oldName = null;
	String newName = null;
	int possiblyDuplicateProfileIndex = -5;
	int nLayersHere = -4;
	float oldOrganicCarbon = Float.NaN;
	float newOrganicCarbon = Float.NaN;

	float oldDepthAtBase = Float.POSITIVE_INFINITY;
	float newDepthAtBase = Float.NaN;
	
	int layerIndexToUse = Integer.MAX_VALUE;
	
	
	for (int profileIndex = 0; profileIndex < collectionOfProfiles.getNProfiles(); profileIndex++) {
	   
	    // now, we need to march through the layers
	    oldName = oldProfileNames[profileIndex];
	    
	    // let's change the first two letters to match the new file name...
	    newName = firstTwoLettersOffileNameWithoutDirectories + oldName.substring(2);
	    
	    // now, we should idiot check to make sure this name isn't already taken somewhere else.
	    possiblyDuplicateProfileIndex = collectionOfProfiles.findProfileIndex(newName);
	    if (possiblyDuplicateProfileIndex >= 0) {
		// let's fall back on pre-pending. if that doesn't work, then we're out of luck...
		newName = firstTwoLettersOffileNameWithoutDirectories + oldName;
		if (newName.length() > maxNameLength) {
		    System.out.println("Cannot adequately rename profile, fallback of pre-pending failed (too long).");
		    System.out.println("old name: " + oldName);
		    System.out.println("new name: " + newName);
		    System.out.println("first try conflicts with existing profile index " + possiblyDuplicateProfileIndex + "...");
		    System.out.println("with: " + oldProfileNames[possiblyDuplicateProfileIndex]);
		    
		    throw new Exception();
		} // end if (second check)
		
		// if we get here, it means there was no exception...
		// look for the (revise) new name, again.
		possiblyDuplicateProfileIndex = collectionOfProfiles.findProfileIndex(newName);
		if (possiblyDuplicateProfileIndex >= 0) {
		    System.out.println("Cannot adequately rename profile, fallback of pre-pending failed (both names already taken).");
		    System.out.println("old name: " + oldName);
		    System.out.println("new name: " + newName);
		    System.out.println("final try conflicts with existing profile index " + possiblyDuplicateProfileIndex + "...");
		    System.out.println("with: " + oldProfileNames[possiblyDuplicateProfileIndex]);

		    throw new Exception();
		} // end if (third check)
	    } // end if (first check)

	    
	    // alrighty, let's change the name and then try to see if it worked...
	    
	    collectionOfProfiles.setProfileNames(newName, profileIndex);
	    
	    // it did! now, we get to actually do the changing of the parameters...
	    nLayersHere = collectionOfProfiles.getNLayersInProfile(profileIndex);
	
	    // let's quickly do the easy one: organic carbon...
	    for (int layerIndex = 0; layerIndex < nLayersHere; layerIndex++) {

		// the organic carbon should be easy: just multiply...
		oldOrganicCarbon = collectionOfProfiles.getOrganicCarbon_SLOC()[profileIndex][layerIndex];
		
		newOrganicCarbon = oldOrganicCarbon * organicCarbonMultiplier;

		collectionOfProfiles.setOrganicCarbon_SLOC(newOrganicCarbon, profileIndex, layerIndex);
	    } // end for layerIndex


	    
	    // now the trickier one: changing layer thicknesses...
	    // if we are thinning them, then we want to start at the top,
	    // but, thickening, i think we want to start at the bottom...
		for (int layerIndex = 0; layerIndex < nLayersHere; layerIndex++) {
		    if (thicknessMultiplier > 1.0F) {
			// we are thickening, so start at the bottom
			layerIndexToUse = (nLayersHere - 1) - layerIndex;
		    } else {
			// we are thinning, so start at the top
			layerIndexToUse = layerIndex;
		    }
		    
		    // ok, this should be less tricky than i originally thought. since this is a truly linear stretching,
		    // i just need to change all the depths by multiplying them directly rather than trying to relate them
		    // to the above and below and such.
		    // grab the old thickness and stretch to make the new thickness.

		    oldDepthAtBase = collectionOfProfiles.getLayerDepthAtBase_SLB()[profileIndex][layerIndexToUse];

		    newDepthAtBase = oldDepthAtBase * thicknessMultiplier;

		    // let's try to assign it and see what happens...
		    collectionOfProfiles.setLayerDepthAtBase_SLB(newDepthAtBase, profileIndex, layerIndexToUse);
		    
		} // end for layerIndex

	    
	} // end for profileIndex, i.e., the soil type under consideration....
	
	FunTricks.writeStringToFile(collectionOfProfiles.dumpAllProfilesAsString(), newSoilFileObject);

	// and, some provenance is always good...
	String provenanceString = "\n"
		+ "! based on soil file: " + originalSoilFile + "\n"
		+ "! using org.DSSATRunner.ChangeWaterPropertiesOfSoilProfiles \n"
		+ "! at " + new java.util.Date().toString() + "\n"
		+ "! settings were:" + "\n"
		+ "! originalSoilFile: " + originalSoilFile + "\n"
		+ "! newSoilFile: " + newSoilFile + "\n"
		+ "! thicknessMultiplier: " + thicknessMultiplier + "\n"
		+ "! organicCarbonMultiplier: " + organicCarbonMultiplier + "\n"
		;
	
	
	FunTricks.appendLineToTextFile(provenanceString, newSoilFile, true);
	
	
	//////////////
	// all done //
	//////////////


    } // main



}

