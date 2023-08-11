package org.DSSATRunner;

import java.io.IOException;
import org.R2Useful.*;

public class ParseOverview {

  private static final String magicOverviewRunBreak =
      "**************************************************************************************************************";
  private static final String magicStressMainLabel = "*ENVIRONMENTAL AND STRESS FACTORS";
  private static final String magicGiantBar =
      "--------------------------------------------------------------------------------------------------------------";
  private static final String magicSynthGrowth = "synth Growth";
  private static final int magicLengthOfGrowthStageNameAndTimeSpan = 29; // 30
  private static final String magicSpace = " ";
  private static final String magicExtraPrefixForGrowthStages = "gro";
  private static final int magicMinimumLineLengthToConsider = 3;
  private static final int magicLengthOfStressIndices = 5;
  private static final int magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth = 2;

  private static final String[] wheatCleanedGrowthStages = {
    magicExtraPrefixForGrowthStages + "_" + "Germinate____Term_Spklt",
    magicExtraPrefixForGrowthStages + "_" + "Term_Spklt___End_Veg",
    magicExtraPrefixForGrowthStages + "_" + "End_Veg______End_Ear_Gr",
    magicExtraPrefixForGrowthStages + "_" + "End_Ear_Gr___Beg_Gr_Fil",
    magicExtraPrefixForGrowthStages + "_" + "Beg_Gr_Fil___End_Gr_Fil",
    magicExtraPrefixForGrowthStages + "_" + "Germinate____End_Gr_Fil",
  };

  private static final String[] maizeCleanedGrowthStages = {
    magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile",
    magicExtraPrefixForGrowthStages + "_" + "Emergence_End_Juvenile",
    magicExtraPrefixForGrowthStages + "_" + "End_Juvenil_Floral_Init",
    magicExtraPrefixForGrowthStages + "_" + "Floral_Init_End_Lf_Grow",
    magicExtraPrefixForGrowthStages + "_" + "End_Lf_Grth_Beg_Grn_Fil",
    magicExtraPrefixForGrowthStages + "_" + "Grain_Filling_Phase",
    magicExtraPrefixForGrowthStages + "_" + "Planting_to_Harvest",
  };

  private static int countRuns(String overviewFileName, String runSeparator) throws IOException {

    // read in the file
    String[] overviewAsArray = FunTricks.readTextFileToArray(overviewFileName);

    // look through it and count up how many run separators there are. thankfully, they put the bar
    // across the bottom, even on the last on.
    int nSeparatorsFound = 0;

    for (int lineIndex = 0; lineIndex < overviewAsArray.length; lineIndex++) {
      if (overviewAsArray[lineIndex].contains(runSeparator)) {
        nSeparatorsFound++;
      }
    }

    return nSeparatorsFound;
  }

  private static Object[] countGrowthStages(String[] overviewFileArray) {

    // look through it and try to find the block of growth stage stresses (the second one that is
    // roughly standardized)
    // and count up how many growth stages we are working with...

    // across the bottom, even on the last on.
    int growthStagesFound = 0;
    int magicHugeMaxNumberOfStages = 100; // i'm going to be lame and just initialize
    // something too big and barely fill it up
    String[] growthStageNames = new String[magicHugeMaxNumberOfStages];
    int labelIsInThisIndex = 0;

    int finalSpaceIndex = -5;
    String paddedGrowthStageName = null;

    int waterSynthStartIndex = -1;
    int nitrogenSynthStartIndex = -2;

    boolean foundFirstGiantBar = false; // this actually needs to be false

    for (int lineIndex = 0; lineIndex < overviewFileArray.length; lineIndex++) {
      if (overviewFileArray[lineIndex].contains(magicStressMainLabel)) {
        // we found what we were looking for, so write it down and bail out...
        labelIsInThisIndex = lineIndex;
        break;
      }
    }

    // ok, step down a few lines and look for the "synth" and "growth" subheadings
    // and figure out what character index ranges they fall in (first will be the
    // water pair, then the nitrogen pair)

    // then, we need to look for a giant bar and count all the non-empty lines between
    // there and the next giant bar. also, keep track of the line indices so we can go
    // back and pull out the growth stage names...

    // i'm going to do this a little bit inefficiently, because i want it to be easier to debug...

    for (int lineIndex = labelIsInThisIndex + 1;
        lineIndex < overviewFileArray.length;
        lineIndex++) {
      if (overviewFileArray[lineIndex].contains(magicSynthGrowth)) {
        // now, find the first occurrence and the second one...
        // Beware the MAGIC NUMBER!!! we are assuming that the first pair are for water
        // and the second pair are for nitrogen
        waterSynthStartIndex = overviewFileArray[lineIndex].indexOf(magicSynthGrowth);
        nitrogenSynthStartIndex =
            overviewFileArray[lineIndex].indexOf(magicSynthGrowth, waterSynthStartIndex + 1);

        // great, but we should continue on in search of the giant bars and stuff...
        // so that will be a separate if/then

        // skip along so we don't waste too much time...
        continue;
      }

      if (overviewFileArray[lineIndex].contains(magicGiantBar)) {
        // mark if we have found the first giant bar or not...
        if (!foundFirstGiantBar) {
          // we just found the first one: mark it and skip along...
          foundFirstGiantBar = true;
          continue;
        } else {
          // we're done here, so bail out...
          break;
        }
      } // we found a giant bar....

      // ok, if we get to here, hopefully, we are between the bars...
      // we are looking for non-empty lines...
      if (foundFirstGiantBar
          && overviewFileArray[lineIndex].length() > magicMinimumLineLengthToConsider) {
        // hmmm... look for the final space in the first piece

        // Beware the MAGIC ASSUMPTION!!! i am going to assume that they all give
        // us " |-----Development Phase------|" which is like 30 characters long.
        // but the last character in the real info is always a space (time span in days
        // then space before the temperatures start). so, we want to look at the initial
        // 30 - 1 = 29 characters
        //		int magicLengthOfGrowthStageNameAndTimeSpan = 30;

        // Beware the MAGIC NUMBER!!! looking for spaces and we know the first
        // character of the line is almost always a space...
        System.out.println("candidate string = [" + overviewFileArray[lineIndex] + "]");
        finalSpaceIndex =
            overviewFileArray[lineIndex]
                .substring(0, magicLengthOfGrowthStageNameAndTimeSpan)
                .lastIndexOf(magicSpace);

        // grab everything before that
        // trim it up
        // convert spaces and hyphens to underscores
        // Beware the MAGIC NUMBER!!! the prefix for the growth stage names so that we can find them
        // easily in lists
        // of variable names... (i.e., for importing the maps into GRASS)
        paddedGrowthStageName =
            magicExtraPrefixForGrowthStages
                + growthStagesFound
                + "_"
                + overviewFileArray[lineIndex]
                    .substring(0, finalSpaceIndex)
                    .trim()
                    .replaceAll("-", "_")
                    .replaceAll(" ", "_")
                    .replaceAll("\\.", "_");

        // store it
        growthStageNames[growthStagesFound] = paddedGrowthStageName;

        // bump up the counter
        growthStagesFound++;
      }
    } // for lineIndex

    // make a new copy of just the growthStages we found
    String[] onlyGrowthStageNames = new String[growthStagesFound];
    for (int stageIndex = 0; stageIndex < growthStagesFound; stageIndex++) {
      onlyGrowthStageNames[stageIndex] = growthStageNames[stageIndex];
    }

    return new Object[] {onlyGrowthStageNames, waterSynthStartIndex, nitrogenSynthStartIndex};
  }

  private static DescriptiveStatisticsUtility[][] originalExtractStressIndicesWpsWgNpsNg(
      String[] overviewFileArray,
      String[] growthStageNames,
      int waterSynthStartIndex,
      int nitrogenSynthStartIndex)
      throws Exception {

    //	int nGroupsFound = 0;

    int nGrowthStages = growthStageNames.length;

    // define our accumulators; there will be four in each row, water/photosynthesis, water/growth,
    // nitrogen/photosynthesis, nitrogen/growth
    DescriptiveStatisticsUtility[][] stressAccumulators =
        new DescriptiveStatisticsUtility[nGrowthStages][4];

    // initialize the accumulators
    for (int growthStageIndex = 0; growthStageIndex < nGrowthStages; growthStageIndex++) {
      stressAccumulators[growthStageIndex][0] =
          new DescriptiveStatisticsUtility(true); // water/photosynthesis
      stressAccumulators[growthStageIndex][1] =
          new DescriptiveStatisticsUtility(true); // water/growth
      stressAccumulators[growthStageIndex][2] =
          new DescriptiveStatisticsUtility(true); // nitrogen/photosynthesis
      stressAccumulators[growthStageIndex][3] =
          new DescriptiveStatisticsUtility(true); // nitrogen/growth
    }

    // ok, repeat a lot of the logic from the growth stage counter, just this time we need to read
    // the values
    // and keep going through all the runs.

    // look through it and try to find the block of growth stage stresses (the second one that is
    // roughly standardized)
    // and count up how many growth stages we are working with...

    // across the bottom, even on the last on.
    double waterPhotosynthesisStress;
    double waterGrowthStress;
    double nitrogenPhotosynthesisStress;
    double nitrogenGrowthStress;

    //	int growthStagesFound = 0;
    //	int magicHugeMaxNumberOfStages = 100; // i'm going to be lame and just initialize
    // something too big and barely fill it up
    //	String[] growthStageNames = new String[magicHugeMaxNumberOfStages];
    int labelIsInThisIndex = 0;

    //	int finalSpaceIndex = -5;
    //	String paddedGrowthStageName = null;

    //	int waterSynthStartIndex = -1;
    //	int nitrogenSynthStartIndex = -2;

    boolean foundFirstGiantBar = false; // this actually needs to be false

    int startLineIndex = 0;
    boolean keepLookingForRuns = true;
    int growthStageWithinBlockLineIndex = 0;

    while (keepLookingForRuns) {

      // start at the previous ending point and keep going...
      for (int lineIndex = startLineIndex; lineIndex < overviewFileArray.length; lineIndex++) {
        if (overviewFileArray[lineIndex].contains(magicStressMainLabel)) {
          // we found what we were looking for, so write it down and bail out...
          labelIsInThisIndex = lineIndex;
          break;
        } else if (lineIndex == overviewFileArray.length - 1) {
          // we need to check if we have run off the end again...
          keepLookingForRuns = false;
        }
      }

      // a cheater way of trying to bail out: short circuit the while loop to get it back to its
      // natural check
      if (!keepLookingForRuns) {
        //		System.out.println(" trying to bail because we read the whole array....");
        continue;
      }
      // then, we need to look for a giant bar and count all the non-empty lines between
      // there and the next giant bar. also, keep track of the line indices so we can go
      // back and pull out the growth stage names...

      // i'm going to do this a little bit inefficiently, because i want it to be easier to debug...

      for (int lineIndex = labelIsInThisIndex + 1;
          lineIndex < overviewFileArray.length;
          lineIndex++) {
        if (overviewFileArray[lineIndex].contains(magicGiantBar)) {
          // mark if we have found the first giant bar or not...
          if (!foundFirstGiantBar) {
            // we just found the first one: mark it and skip along...
            foundFirstGiantBar = true;
            growthStageWithinBlockLineIndex = 0;
            continue;
          } else {
            // we're done with this "RUN", so reset the startLineIndex and bail back out to the main
            // while loop
            startLineIndex = lineIndex + 1; // tell it to start after this giant bar...
            //			System.out.println("     ... hit second giant bar (breaking) ...");
            break;
          }
        } // we found a giant bar....

        // ok, if we get to here, hopefully, we are between the bars...
        // we are looking for non-empty lines...
        if (foundFirstGiantBar
            && overviewFileArray[lineIndex].length() > magicMinimumLineLengthToConsider) {
          // hmmm... look for the final space in the first piece

          // Beware the MAGIC ASSUMPTION!!! i am going to assume that they all give
          // us " |-----Development Phase------|" which is like 30 characters long.
          // but the last character in the real info is always a space (time span in days
          // then space before the temperatures start). so, we want to look at the initial
          // 30 - 1 = 29 characters
          //		int magicLengthOfGrowthStageNameAndTimeSpan = 30;

          // Beware the MAGIC NUMBER!!! looking for spaces and we know the first
          // character of the line is almost always a space...
          System.out.println("candidate string = [" + overviewFileArray[lineIndex] + "]");
          //		    finalSpaceIndex = overviewFileArray[lineIndex].substring(0,
          // magicLengthOfGrowthStageNameAndTimeSpan).lastIndexOf(magicSpace);

          // grab the four values
          waterPhotosynthesisStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      waterSynthStartIndex, waterSynthStartIndex + magicLengthOfStressIndices));
          waterGrowthStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      waterSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + magicLengthOfStressIndices,
                      waterSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + 2 * magicLengthOfStressIndices));
          nitrogenPhotosynthesisStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      nitrogenSynthStartIndex,
                      nitrogenSynthStartIndex + magicLengthOfStressIndices));
          nitrogenGrowthStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      nitrogenSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + magicLengthOfStressIndices,
                      nitrogenSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + 2 * magicLengthOfStressIndices));

          stressAccumulators[growthStageWithinBlockLineIndex][0].useDoubleValue(
              waterPhotosynthesisStress); // water/photosynthesis
          stressAccumulators[growthStageWithinBlockLineIndex][1].useDoubleValue(
              waterGrowthStress); // water/growth
          stressAccumulators[growthStageWithinBlockLineIndex][2].useDoubleValue(
              nitrogenPhotosynthesisStress); // nitrogen/photosynthesis
          stressAccumulators[growthStageWithinBlockLineIndex][3].useDoubleValue(
              nitrogenGrowthStress); // nitrogen/growth

          //		    System.out.println("-- internal line index " + growthStageWithinBlockLineIndex + "
          // ; real line index " + lineIndex + " --");
          //		    System.out.println("Wps = " + waterPhotosynthesisStress);
          //		    System.out.println("Wg  = " + waterGrowthStress);
          //		    System.out.println("Nps = " + nitrogenPhotosynthesisStress);
          //		    System.out.println("Ng  = " + nitrogenGrowthStress);

          growthStageWithinBlockLineIndex++;
        } // if found giant bar and valid line...

        // now, we should do some sort of checking on whether we have found all the lines we
        // anticipate and thus should
        // be looking for the next RUN's block...

        if (growthStageWithinBlockLineIndex >= nGrowthStages) {

          // we think we found everything, so reset a bunch of things...
          //		    System.out.println("      ____resetting because we think we found all the growth
          // stages_____");
          foundFirstGiantBar = false;
          startLineIndex = lineIndex + 1;
          growthStageWithinBlockLineIndex = 0;

          //		    System.out.println("      ====about to break: startLineIndex=" + startLineIndex +
          // " ====");

          break;
        }
      } // for lineIndex

      //	    nGroupsFound++;

      //	    System.out.println("      #### at bottom; startLineIndex=" + startLineIndex + " ;
      // length = " + overviewFileArray.length + " ; nFound = " + nGroupsFound + " ####");

    } // end keepLookingForRuns

    return stressAccumulators;
  }

  private static DescriptiveStatisticsUtility[][] extractStressIndicesWpsWgNpsNg(
      String[] overviewFileArray,
      String[] growthStageNames,
      int waterSynthStartIndex,
      int nitrogenSynthStartIndex,
      DescriptiveStatisticsUtility[][] stressAccumulators)
      throws Exception {

    final int nStressIndices =
        4; // a magic number: we have photosynthesis and growth crossed with water and nitrogen
    int nGrowthStages = growthStageNames.length;

    // check if we have non-null stat utilities. create them if necessary...
    if (stressAccumulators == null) {
      // define our accumulators; there will be four in each row, water/photosynthesis,
      // water/growth, nitrogen/photosynthesis, nitrogen/growth
      stressAccumulators = new DescriptiveStatisticsUtility[nGrowthStages][4];

      // initialize the accumulators
      for (int growthStageIndex = 0; growthStageIndex < nGrowthStages; growthStageIndex++) {
        stressAccumulators[growthStageIndex][0] =
            new DescriptiveStatisticsUtility(true); // water/photosynthesis
        stressAccumulators[growthStageIndex][1] =
            new DescriptiveStatisticsUtility(true); // water/growth
        stressAccumulators[growthStageIndex][2] =
            new DescriptiveStatisticsUtility(true); // nitrogen/photosynthesis
        stressAccumulators[growthStageIndex][3] =
            new DescriptiveStatisticsUtility(true); // nitrogen/growth
      } // end for growthStageIndex
    } else {
      // check to make sure they are the right dimensions... we'll leave it at that...
      if (stressAccumulators.length != growthStageNames.length) {
        System.out.println(
            "mismatched stress accumulators and growth stage names: ["
                + stressAccumulators.length
                + "] stressAccumulators.length != growthStageNames.length ["
                + growthStageNames.length
                + "]");
        throw new Exception();
      }
      for (int growthStageIndex = 0; growthStageIndex < nGrowthStages; growthStageIndex++) {
        if (stressAccumulators[growthStageIndex].length != nStressIndices) {
          System.out.println(
              "incorrect number of stress index accumulators: ["
                  + stressAccumulators[growthStageIndex].length
                  + "] stressAccumulators[growthStageIndex].length != nStressIndices ["
                  + nStressIndices
                  + "]");
        } // if wrong number of accumulators
      } // for growthStageIndex
    } // if/else idiot checking

    // look through it and try to find the block of growth stage stresses (the second one that is
    // roughly standardized)
    // and count up how many growth stages we are working with...

    // across the bottom, even on the last on.
    double waterPhotosynthesisStress;
    double waterGrowthStress;
    double nitrogenPhotosynthesisStress;
    double nitrogenGrowthStress;

    int labelIsInThisIndex = 0;

    boolean foundFirstGiantBar = false; // this actually needs to be false
    boolean keepLookingForRuns = true; // this actually needs to be true

    int startLineIndex = 0; // this actually needs to be zero
    int growthStageWithinBlockLineIndex = 0;

    while (keepLookingForRuns && startLineIndex < overviewFileArray.length) {

      System.out.println(
          "top of while: [" + startLineIndex + "] = [" + overviewFileArray[startLineIndex] + "]");

      // start at the previous ending point and keep going...
      for (int lineIndex = startLineIndex; lineIndex < overviewFileArray.length; lineIndex++) {
        if (overviewFileArray[lineIndex].contains(magicStressMainLabel)) {
          // we found what we were looking for, so write it down and bail out...
          labelIsInThisIndex = lineIndex;
          break;
        } else if (lineIndex == overviewFileArray.length - 1) {
          // we need to check if we have run off the end again...
          keepLookingForRuns = false;
        }
      }

      if (!keepLookingForRuns) {
        // get the biggest thing we can so that hopefully
        startLineIndex = Integer.MAX_VALUE;
        break;
      }

      // then, we need to look for a giant bar and count all the non-empty lines between
      // there and the next giant bar. also, keep track of the line indices so we can go
      // back and pull out the growth stage names...

      // i'm going to do this a little bit inefficiently, because i want it to be easier to debug...

      // at this point, we should have found the stress main label
      for (int lineIndex = labelIsInThisIndex + 1;
          lineIndex < overviewFileArray.length;
          lineIndex++) {
        if (overviewFileArray[lineIndex].startsWith(magicGiantBar)) {
          // mark if we have found the first giant bar or not...
          if (!foundFirstGiantBar) {
            // we just found the first one: mark it and skip along...
            foundFirstGiantBar = true;
            growthStageWithinBlockLineIndex = 0;
            continue;
          } else {
            // we're done with this "RUN", so reset the startLineIndex and bail back out to the main
            // while loop
            startLineIndex = lineIndex + 1; // tell it to start after this giant bar...
            foundFirstGiantBar = false;
            //			System.out.println("     ... hit second giant bar (breaking) ...");
            break;
          }
        } // we found a giant bar....

        // ok, if we get to here, hopefully, we are between the bars...
        // we are looking for non-empty lines...
        if (foundFirstGiantBar
            && overviewFileArray[lineIndex].length() > magicMinimumLineLengthToConsider
        //			&& !overviewFileArray[lineIndex].startsWith(magicGiantBar)
        ) {
          // hmmm... look for the final space in the first piece
          System.out.println(
              "overviewFileArray[" + lineIndex + "] = [" + overviewFileArray[lineIndex] + "]");
          // Beware the MAGIC ASSUMPTION!!! i am going to assume that they all give
          // us " |-----Development Phase------|" which is like 30 characters long.
          // but the last character in the real info is always a space (time span in days
          // then space before the temperatures start). so, we want to look at the initial
          // 30 - 1 = 29 characters
          //		int magicLengthOfGrowthStageNameAndTimeSpan = 30;

          // Beware the MAGIC NUMBER!!! looking for spaces and we know the first
          // character of the line is almost always a space...
          //		    System.out.println("candidate string = [" + overviewFileArray[lineIndex] + "]");
          int finalSpaceIndex =
              overviewFileArray[lineIndex]
                  .substring(0, magicLengthOfGrowthStageNameAndTimeSpan)
                  .lastIndexOf(magicSpace);
          // grab everything before that
          // trim it up
          // convert spaces and hyphens to underscores
          // Beware the MAGIC NUMBER!!! the prefix for the growth stage names so that we can find
          // them easily in lists
          // of variable names... (i.e., for importing the maps into GRASS)
          String paddedGrowthStageNameFromOverviewDotOut =
              magicExtraPrefixForGrowthStages
                  + "_"
                  + overviewFileArray[lineIndex]
                      .substring(0, finalSpaceIndex)
                      .trim()
                      .replaceAll("-", "_")
                      .replaceAll(" ", "_")
                      .replaceAll("\\.", "_");

          // and now, we must compare to the list we are working with.
          int growthStageIndexThatMatches = -1;
          for (int listGrowthStageIndex = 0;
              listGrowthStageIndex < growthStageNames.length;
              listGrowthStageIndex++) {
            if (growthStageNames[listGrowthStageIndex].equals(
                paddedGrowthStageNameFromOverviewDotOut)) {
              // we found what we're looking for
              growthStageIndexThatMatches = listGrowthStageIndex;
              break;
            }
          }

          if (growthStageIndexThatMatches < 0) {
            // we likely had a problem, so let's look at what we have...
            System.out.println(
                "   we probably didn't find growth stage ["
                    + paddedGrowthStageNameFromOverviewDotOut
                    + "]");
          }

          // grab the four values
          waterPhotosynthesisStress = -5;

          waterPhotosynthesisStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      waterSynthStartIndex, waterSynthStartIndex + magicLengthOfStressIndices));
          waterGrowthStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      waterSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + magicLengthOfStressIndices,
                      waterSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + 2 * magicLengthOfStressIndices));
          nitrogenPhotosynthesisStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      nitrogenSynthStartIndex,
                      nitrogenSynthStartIndex + magicLengthOfStressIndices));
          nitrogenGrowthStress =
              Double.parseDouble(
                  overviewFileArray[lineIndex].substring(
                      nitrogenSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + magicLengthOfStressIndices,
                      nitrogenSynthStartIndex
                          + magicLengthOfEmptySpaceBetweenPhotosynthesisAndGrowth
                          + 2 * magicLengthOfStressIndices));

          stressAccumulators[growthStageIndexThatMatches][0].useDoubleValue(
              waterPhotosynthesisStress); // water/photosynthesis
          stressAccumulators[growthStageIndexThatMatches][1].useDoubleValue(
              waterGrowthStress); // water/growth
          stressAccumulators[growthStageIndexThatMatches][2].useDoubleValue(
              nitrogenPhotosynthesisStress); // nitrogen/photosynthesis
          stressAccumulators[growthStageIndexThatMatches][3].useDoubleValue(
              nitrogenGrowthStress); // nitrogen/growth

          //		    System.out.println("-- internal line index " + growthStageWithinBlockLineIndex + "
          // ; real line index " + lineIndex + " --");
          //		    System.out.println("Wps = " + waterPhotosynthesisStress);
          //		    System.out.println("Wg  = " + waterGrowthStress);
          //		    System.out.println("Nps = " + nitrogenPhotosynthesisStress);
          //		    System.out.println("Ng  = " + nitrogenGrowthStress);

          growthStageWithinBlockLineIndex++;
        } // if found giant bar and valid line...

        // now, we should do some sort of checking on whether we have found all the lines we
        // anticipate and thus should
        // be looking for the next RUN's block...

        if (growthStageWithinBlockLineIndex >= nGrowthStages
        //			|| overviewFileArray[lineIndex].startsWith(magicOverviewRunBreak)
        //			|| overviewFileArray[lineIndex].startsWith(magicGiantBar)
        ) {

          // we think we found everything, so reset a bunch of things...
          //		    System.out.println("      ____resetting because we think we found all the growth
          // stages_____");
          foundFirstGiantBar = false;
          startLineIndex = lineIndex + 1;
          growthStageWithinBlockLineIndex = 0;

          //		    System.out.println("      ====about to break: startLineIndex=" + startLineIndex +
          // " ====");

          break;
        }
      } // for lineIndex

      //	    nGroupsFound++;
      //	    System.out.println("      #### at bottom; startLineIndex=" + startLineIndex + " ;
      // length = " + overviewFileArray.length + " ; nFound = " + nGroupsFound + " ####");

    } // end keepLookingForRuns

    return stressAccumulators;
  }

  public static void main(String commandLineOptions[]) throws Exception {

    TimerUtility bigTimer = new TimerUtility();

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    bigTimer.tic();
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(i + " " + commandLineOptions[i] + "; " + bigTimer.tocNanos() + " ns\n");
      bigTimer.tic();
    }
    System.out.println();

    String overviewToTry = commandLineOptions[0];

    //	int nRuns = countRuns(overviewToTry,magicOverviewRunBreak);
    //	System.out.println("file: " + overviewToTry + " contains " + nRuns + " runs, we think...");

    String[] overviewAsArray = FunTricks.readTextFileToArray(overviewToTry);
    Object[] growthStageInfoPacket = countGrowthStages(overviewAsArray);

    String[] growthStageNames = (String[]) growthStageInfoPacket[0];
    int waterSynthStartIndexToUse = (int) growthStageInfoPacket[1];
    int nitrogenSynthStartIndexToUse = (int) growthStageInfoPacket[2];

    // ok, now let's try to accumulate...
    growthStageNames = maizeCleanedGrowthStages;

    DescriptiveStatisticsUtility[][] growthStageStressAccumulators =
        extractStressIndicesWpsWgNpsNg(
            overviewAsArray,
            growthStageNames,
            waterSynthStartIndexToUse,
            nitrogenSynthStartIndexToUse,
            null);

    System.out.println("growth stages...");
    for (int growthStageIndex = 0; growthStageIndex < growthStageNames.length; growthStageIndex++) {
      System.out.println(
          "[" + growthStageIndex + "] = [" + growthStageNames[growthStageIndex] + "]");
    }

    System.out.println("water starts at index " + waterSynthStartIndexToUse);
    System.out.println("nitro starts at index " + nitrogenSynthStartIndexToUse);

    for (int growthStageIndex = 0;
        growthStageIndex < growthStageStressAccumulators.length;
        growthStageIndex++) {
      for (int stressIndex = 0;
          stressIndex < growthStageStressAccumulators[0].length;
          stressIndex++) {
        System.out.println(
            "growth stage index "
                + growthStageIndex
                + " ["
                + growthStageNames[growthStageIndex]
                + "]["
                + stressIndex
                + "] = "
                + growthStageStressAccumulators[growthStageIndex][stressIndex].getMean());
      }
    }
  } // main
}
