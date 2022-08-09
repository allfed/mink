package org.DSSATRunner;

//import java.io.*;

import java.io.File;

import org.R2Useful.*;

public class ChangeWaterPropertiesOfSoilProfiles {

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

	String originalSoilFile     = commandLineOptions[0];
	String newSoilFile          = commandLineOptions[1];
	Float  totalWaterMultiplier = Float.parseFloat(    commandLineOptions[2]);
	boolean changeUpperFlag      = Boolean.parseBoolean(commandLineOptions[3]);
	boolean changeLowerFlag       = Boolean.parseBoolean(commandLineOptions[4]);

	// idiot checking
	if (!changeUpperFlag && !changeLowerFlag) {
	    System.out.println("at least one of the raise/lower flags should be true.");
	    throw new Exception();
	}
	
	if (totalWaterMultiplier < 0) {
	    System.out.println("water multiplier must be non-negative (i.e., not " + totalWaterMultiplier + ")");
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
	float oldDrainedUpper = Float.NaN;
	float oldLower = Float.NaN;
	float waterRange = Float.POSITIVE_INFINITY;
	float oldMiddlePoint = Float.NaN;
	float newWaterRange = Float.POSITIVE_INFINITY;
	float newDrainedUpper = Float.NaN;
	float newLower = Float.NaN;
	float swapSpot = Float.NEGATIVE_INFINITY;
	
	
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
	    
	    // it did! now, we get to actually do the changing of the water parameters...
	    nLayersHere = collectionOfProfiles.getNLayersInProfile(profileIndex);
	
	    for (int layerIndex = 0; layerIndex < nLayersHere; layerIndex++) {
		oldDrainedUpper = collectionOfProfiles.getDrainedUpperLimit_SDUL()[profileIndex][layerIndex];
		oldLower = collectionOfProfiles.getLowerLimit_SLLL()[profileIndex][layerIndex];

		waterRange = oldDrainedUpper - oldLower;

		// ok, now we need to spread or shrink that water range a little bit...
		newWaterRange = totalWaterMultiplier * waterRange;
		
		// and then, move the limits around according to the flag requests...
		
		if (changeUpperFlag && changeLowerFlag) {
		    // we'll split the difference. so, find the middle of the originals, then go up and down by half of the new range.
		    oldMiddlePoint = (oldDrainedUpper + oldLower) / 2.0F;
		    newDrainedUpper = oldMiddlePoint + newWaterRange / 2.0F;
		    newLower        = oldMiddlePoint - newWaterRange / 2.0F;
		} else if (changeUpperFlag) {
		    // move the upper only
		    newLower = oldLower;
		    newDrainedUpper = newLower + newWaterRange;
		} else if (changeLowerFlag) {
		    // move the lower only
		    newDrainedUpper = oldDrainedUpper;
		    newLower = newDrainedUpper - newWaterRange;
		} else {
		    // we should never get here, but just in case.
		    System.out.println("at least one of the raise/lower flags should be true.");
		    throw new Exception();
		}
		
		// ok, and idiot check to make sure that they are are in the order {0 , new lower , new upper , saturated}.
		if (newLower < 0.0F) {
		    newLower = 0.0F;
		}
		if (newDrainedUpper < 0.0F) {
		    newDrainedUpper = 0.0F;
		}
		if (newLower > collectionOfProfiles.getSaturatedUpperLimit_SSAT()[profileIndex][layerIndex]) {
		    newLower = collectionOfProfiles.getSaturatedUpperLimit_SSAT()[profileIndex][layerIndex];
		}
		if (newDrainedUpper > collectionOfProfiles.getSaturatedUpperLimit_SSAT()[profileIndex][layerIndex]) {
		    newDrainedUpper = collectionOfProfiles.getSaturatedUpperLimit_SSAT()[profileIndex][layerIndex];
		}
		if (newLower > newDrainedUpper) {
		    swapSpot = newLower;
		    newLower = newDrainedUpper;
		    newDrainedUpper = swapSpot;
		}
		
		// now that we should have them, let's write 'em down...
		collectionOfProfiles.setDrainedUpperLimit_SDUL(newDrainedUpper, profileIndex, layerIndex);
		collectionOfProfiles.setLowerLimit_SLLL(              newLower, profileIndex, layerIndex);
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
		+ "! totalWaterMultiplier: " + totalWaterMultiplier + "\n"
		+ "! changeUpperFlag: " + changeUpperFlag + "\n"
		+ "! changeLowerFlag: " + changeLowerFlag + "\n"
		;
	
	
	FunTricks.appendLineToTextFile(provenanceString, newSoilFile, true);
	
	
	//////////////
	// all done //
	//////////////


    } // main



}

