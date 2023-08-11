package org.DSSATRunner;

// import java.io.*;

import java.io.File;
import org.R2Useful.*;

// import org.DSSATRunner.DSSATHelperMethods;

public class CountUpHeatStressDaysFromDailyWeather {

  // the plan here is to try to read a pile of daily weather files
  // for each one, we will compute the "THI" for the day and decide how bad it is based on some
  // arbitrary thresholds
  // then, we will count up how many of each category of badness there are within the years
  // represented by the weather file
  //
  // the results will go out as a text file with the latitude and longitude and then a bunch of
  // counts
  //
  // and hopefully a provenance file with the thresholds and paths and stuff

  public static void main(String commandLineOptions[]) throws Exception {

    ////////////////////
    // magical things //
    ////////////////////

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    String sourceDirectory = commandLineOptions[0];
    String destinationFilename = commandLineOptions[1];
    String thresholdsCSV = commandLineOptions[2];

    //		String sourcePrefix          =                  commandLineOptions[1];
    //		String sourceSuffix          =                  commandLineOptions[2];

    // declarations and initializations
    double latitude = Double.NaN;
    double longitude = Double.NaN;
    int yearInFile = -5;
    int previousYearInFile = -5;
    String particularOutputFilename = null;
    String particularProvenanceFilename = null;

    // magic numbers for spacing
    final int magicFirstLineToWorkOn = 5;
    final int magicLengthOfDate = 5;
    final int sradEnd = 11;
    final int tmaxEnd = 17;
    final int tminEnd = 23;
    //	final int rainEnd = 29;

    final double initForyesterdaysTmin = -999.999;
    final int initForPreviousYearInFile = -987656;

    /// initializations
    String[] originalAsArray = null;
    int originalDate;

    // the data stuff

    double tmaxHere, tminHere;
    double approximateRelativeHumidity = -55.3;
    double tmaxHereInF = -5.5;
    double approximateTHItoday = -5.3;
    boolean foundABin = false;

    double yesterdaysTmin = initForyesterdaysTmin;

    String outputLine = null;

    // this should probably get rearranged once i get done hacking everything.

    // parse the CSV
    // count up how many non-empty values we have
    String[] thresholdSplit = thresholdsCSV.split(",");
    int nThresholds = 0;
    for (int splitIndex = 0; splitIndex < thresholdSplit.length; splitIndex++) {
      if (thresholdSplit[splitIndex] == null) {
        continue;
      } else {
        nThresholds++;
      }
    }
    double[] thiThresholds = new double[nThresholds];
    int storageNumber = 0;
    for (int splitIndex = 0; splitIndex < thresholdSplit.length; splitIndex++) {
      if (thresholdSplit[splitIndex] == null) {
        continue;
      } else {
        thiThresholds[storageNumber] = Double.parseDouble(thresholdSplit[splitIndex]);

        System.out.println(
            "thiThresholds[" + storageNumber + "] = " + thiThresholds[storageNumber]);

        // do some idiot checking
        if (storageNumber > 0 && thiThresholds[storageNumber] < thiThresholds[storageNumber - 1]) {
          System.out.println("ERROR: thesholds are not in increasing order");
          System.out.println(thresholdsCSV);
          System.out.println(
              "(storageNumber > 0 && thiThresholds[storageNumber] < thiThresholds[storageNumber -"
                  + " 1])");
          System.out.println(
              "("
                  + storageNumber
                  + "> 0 && "
                  + thiThresholds[storageNumber]
                  + " < "
                  + thiThresholds[storageNumber - 1]
                  + ")");
          throw new Exception();
        }

        storageNumber++;
      }
    }

    // and we will need a counter array for those thresholds
    // there will be one more counter than the threshold so that we will have:
    // below 0th, below 1st, below ..., below last, above last

    int[] daysBelowThreshold = new int[nThresholds + 1];

    // ok, now we will need to read the files....

    // first, let's check if the destination file already exists and advise that we are overwriting
    // it...
    File outputFileObject = new File(destinationFilename);
    if (outputFileObject.exists()) {
      System.out.println(
          "overwriting intended output [" + outputFileObject.getCanonicalPath() + "]");
    }

    // we want to grab everything inside the directory, which will be assumed to be clean
    System.out.println("--- !!! we assume that everything inside the requested directory !!! ---");
    System.out.println("--- !!! [" + sourceDirectory + "] is useful !!! ---");

    File directoryObject = new File(sourceDirectory);

    File[] inputFiles = directoryObject.listFiles();

    long startTime = System.currentTimeMillis();

    // ok, now we have a problem in that we need to have a separate output file for each year, i
    // think. so then we have to know what years we have
    // to work with and initialize those files....

    // determine the years in the first weather file and assume they all are the same
    System.out.println(
        "Assuming that all weather files in the source directory have the same day/year coverage.");

    // let's just use the very first one...
    originalAsArray = FunTricks.readTextFileToArray(inputFiles[0].getCanonicalPath());

    for (int lineIndex = magicFirstLineToWorkOn; lineIndex < originalAsArray.length; lineIndex++) {
      // figure out the day we're looking at...
      originalDate =
          Integer.parseInt(originalAsArray[lineIndex].substring(0, magicLengthOfDate).trim());
      yearInFile = originalDate / 1000;

      if (yearInFile != previousYearInFile) {
        // create an empty file with this name...
        particularOutputFilename =
            destinationFilename + DSSATHelperMethods.padWithZeros(yearInFile, 2) + ".txt";
        FunTricks.writeStringToFile("", particularOutputFilename);
        System.out.println("initialized " + particularOutputFilename);

        // and how about a provenance file
        particularProvenanceFilename =
            destinationFilename
                + DSSATHelperMethods.padWithZeros(yearInFile, 2)
                + ".provenance.txt";

        FunTricks.appendLineToTextFile(
            "sourceDirectory = [" + sourceDirectory + "]",
            particularProvenanceFilename,
            false); // make a new file
        FunTricks.appendLineToTextFile(
            "destinationFilename = [" + destinationFilename + "]",
            particularProvenanceFilename,
            true); // append from there...
        FunTricks.appendLineToTextFile(
            "thresholdsCSV = [" + thresholdsCSV + "]", particularProvenanceFilename, true);
      }
      previousYearInFile = yearInFile;
    }

    // now, do it for real....
    for (int fileIndex = 0; fileIndex < inputFiles.length; fileIndex++) {

      originalAsArray = FunTricks.readTextFileToArray(inputFiles[fileIndex].getCanonicalPath());

      //		String originalDatePart, restOfLine;
      //		int originalDate, newDate, yearToUse, dayToUse, originalYear, originalDayOfYear;

      /*
       *WEATHER DATA :

      @ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT
        RICK   -55.75   -67.25  -999   5.7   2.2  -999  -999
      @DATE  SRAD  TMAX  TMIN  RAIN   PAR  CO2D
      90274   9.7   4.0   3.1   1.5  -999  -999
      90275  12.5   5.4   1.8   1.9  -999  -999
             01234567890123456789012345678901234567890
             0         1         2         3         4
       */

      // the plan is to read through all the days and look at the high and low temperatures for the
      // day and work out an approximate
      // relative humidity and temperature-heat-index (THI)
      //
      // then count up how many days of stress happen.
      //
      // the worst THI will usually happen around the high of the day, so we need the relative
      // humidity the.
      // that will be approximated by the dew point which will be approximated by the previous day's
      // low.
      //
      // thus, we will lose one day off of the front end of our dataset.

      // first, let us grab the stated geographic and yearly info...
      //		lineThreeInPieces =
      // FunTricks.parseRepeatedDelimiterToArray(originalAsArray[magicGeographyAndYearlyLineIndex],
      // " ");
      //
      //		latitude  = Double.parseDouble(lineThreeInPieces[1]);
      //		longitude = Double.parseDouble(lineThreeInPieces[2]);
      ////		elevation = Double.parseDouble(lineThreeInPieces[3]);
      //		tavStated = Double.parseDouble(lineThreeInPieces[4]);
      //		ampStated = Double.parseDouble(lineThreeInPieces[5]);
      ////		System.out.println("lat = " + latitude + "; long = " + longitude + "; elev = " +
      // elevation + "; tav = " + tavStated + "; amp = " + ampStated);

      // determine latitude and longitude from the file name since we might be dealing with jawoo's
      // POWER wth files that he
      // created using the same header information for all of them....

      // example filename: NOSRAD_Historical_rcp8p5_2055_gfdl_78.75_-110.25.WTH
      String simpleInputFile = inputFiles[fileIndex].getName(); // the name all by itself
      String strippingOffSuffix =
          simpleInputFile.substring(
              0, simpleInputFile.lastIndexOf(".")); // getting rid of .WTH or .csv or whatever
      String[] underscoreSplit =
          strippingOffSuffix.split(
              "_"); // separating the various elements so we can find the coordinates

      // latitude is the next-to-the-last entry
      latitude = Double.parseDouble(underscoreSplit[underscoreSplit.length - 2]);
      // longitude is the last entry
      longitude = Double.parseDouble(underscoreSplit[underscoreSplit.length - 1]);

      // initialize the holding spots
      previousYearInFile = initForPreviousYearInFile;
      daysBelowThreshold = new int[nThresholds + 1];

      yesterdaysTmin = -9999.0;

      for (int lineIndex = magicFirstLineToWorkOn;
          lineIndex < originalAsArray.length;
          lineIndex++) {

        // figure out the day we're looking at...
        originalDate =
            Integer.parseInt(originalAsArray[lineIndex].substring(0, magicLengthOfDate).trim());
        yearInFile = originalDate / 1000;

        // if we have just turned over to a new year, then we need to output the old one...
        if (previousYearInFile != initForPreviousYearInFile && previousYearInFile != yearInFile) {
          // write out the old stuff...
          particularOutputFilename =
              destinationFilename + DSSATHelperMethods.padWithZeros(yearInFile, 2) + ".txt";

          outputLine = latitude + "," + longitude; // + "," + notInAnyBin;
          for (int binIndex = 0; binIndex < daysBelowThreshold.length; binIndex++) {
            outputLine += "," + daysBelowThreshold[binIndex];
          }

          FunTricks.appendLineToTextFile(outputLine, particularOutputFilename, true);

          // now, we need to reset the counters
          daysBelowThreshold = new int[nThresholds + 1];
        }

        //		sradHere =
        // Double.parseDouble(originalAsArray[lineIndex].substring(magicLengthOfDate,sradEnd).trim());
        tmaxHere =
            Double.parseDouble(originalAsArray[lineIndex].substring(sradEnd, tmaxEnd).trim());
        tminHere =
            Double.parseDouble(originalAsArray[lineIndex].substring(tmaxEnd, tminEnd).trim());
        //		rainHere =
        // Double.parseDouble(originalAsArray[lineIndex].substring(tminEnd,rainEnd).trim());

        // check to see if we have good info to work with...
        if (yesterdaysTmin == initForyesterdaysTmin) {
          // skip over this and move on to the next day...
          continue;
        }

        // ok, so we should have a value for yesterday's low and today's high.
        // using the rule of thumb from http://journals.ametsoc.org/doi/abs/10.1175/BAMS-86-2-225
        approximateRelativeHumidity = 100.0 - 5 * (tmaxHere - yesterdaysTmin);

        // now we need to make that into a THI
        // THI = T_actual - (0.55 - 0.55 * RH/100) * (T_actual - 58) [temperatures in Fahrenheit]
        // (The seemingly early reference is: Kelly, C. F., and T. E. Bond. 1971. Bioclimatic
        // factors and their measurements. Page 7 in A Guide to Environmental Research in Animals.
        // Natl. Acad. Sci., Washington, DC.)

        tmaxHereInF = tmaxHere * 9 / 5 + 32;

        approximateTHItoday =
            tmaxHereInF - (0.55 - 0.55 * approximateRelativeHumidity / 100) * (tmaxHereInF - 58);

        //		System.out.println("THI = " + approximateTHItoday);

        // now we have to see how that corresponds to our thresholds...
        foundABin = false;
        for (int binIndex = 0; binIndex < nThresholds; binIndex++) {
          //		    System.out.println("    bI " + binIndex + ": THI = " + approximateTHItoday + " ;
          // thresh = " + thiThresholds[binIndex]);
          if (approximateTHItoday < thiThresholds[binIndex]) {
            // the thresholds are supposed to be in order, so if we are under the threshold being
            // tested
            // it means that we have found the first threshold that is bigger than our value
            // so, let's put it in that bin...
            //			System.out.println("       inside: we found something; old count = " +
            // daysBelowThreshold[binIndex]);

            daysBelowThreshold[binIndex]++;
            foundABin = true;
            break;
          }
        }
        // if we still haven't found a bin, it must go in the last "bigger than" bin
        if (!foundABin) {
          //			System.out.println("       bigger: we found something; old count = " +
          // daysBelowThreshold[nThresholds]);
          daysBelowThreshold[nThresholds]++;
        }

        // since it is about to be "tomorrow", we need to copy the year and temperature into holding
        // spots...
        yesterdaysTmin = tminHere;
        previousYearInFile = yearInFile;
      } // for lineIndex

      if (fileIndex % (inputFiles.length / 400 + 1) == 0) {
        double timeMinutes = ((System.currentTimeMillis() - startTime) / 1000.0 / 60);

        System.out.println(
            (fileIndex + 1)
                + "/"
                + inputFiles.length
                + " = "
                + FunTricks.fitInNCharacters((100 * (fileIndex + 1.0) / inputFiles.length), 5)
                + "\t"
                + ((System.currentTimeMillis() - startTime) / 1000)
                + "s\t"
                + timeMinutes
                + "m\t"
                + (timeMinutes * ((inputFiles.length * 1.0) / (fileIndex + 1)) - timeMinutes)
                + "m remaining");
      }
    } // end giant file loop
  } // main

  /*

       http://climatechange.ifpri.info/heat-stress-could-be-a-problem-for-livestock-living-outdoors-under-climate-change/

       http://journals.ametsoc.org/doi/abs/10.1175/BAMS-86-2-225


       It began with an innocent enquiry: do we have relative humidity data, under climate change for possible future situations, that could be used to think about a direct effect on animal productivity?

  Currently, in our economic modeling, the cattle, hogs, chickens, etc., are only indirectly affected by changes in feed/fodder prices when those are affected by climate change. But, we are not capturing any direct effect from hot animals.

  We would like to end up with global maps of some index that can tell us something about the stress that animals would experience under typical conditions at each place on the map.

  The problem, of course, is that the climate data we normally use for crop modeling do not include relative humidity. That is not quite right: some of the raw data do, but not in the data we have cleaned up and arranged for using in the crop models. In order to try to keep things internally consistent, we would like to build up a rough approximation based on what we do have.

  Here is a way to go about it:

  Enter Mark Lawrence. He points out that there is a remarkably effective rule of thumb to relate relative humidity, the dew point, and the temperature we are concerned about.

  RH roughly = 100 - 5(T_actual - T_dewpoint) [temperatures in Celsius]

  Is it perfect? Does it work over all ranges of temperatures? No. Does he outline more complicated relationships that we could have used? Yes. But the simple one will get us going: we can get relative humidity in terms of the temperature we care about and the dewpoint that is occurring at the same time.

  Great, but what is the dewpoint? We don't know or else we wouldn't have to do this exercise. Again, we need a trick to provide a rough approximation: in most circumstances, the dew point is something close to the overnight low temperature. A little clicking around on popular weather websites indicates that, at least where I live and at this time, the dewpoint is remarkably steady and only rises a few degrees above the overnight low (while the actual air temperature rockets up to oppressive levels). Checking a few other places in the world indicates a similar relationship except for the most exceedingly dry places I checked like Phoenix, AZ or San Bernardino, CA.

  Now, we can plug in the overnight low as the dewpoint and hope for the best. This will usually, in fact, be lower than the reality, so the relative humidity should appear to be lower which in turn will mean the heat will seem less oppressive using our proxy dewpoint than it would with the "real" dewpoint.

  Then, we have to choose what temperature we care about. I decided to go with halfway between the high and low for the day to get a situation which should be reasonable for a big chunk of the day. That is, the most oppressive part of the day should be as bad or worse than what we get by using the midpoint temperature. At least, that's the idea.

  In practice, we will pick a month and pull out the monthly average low and high temperatures. So, our numbers are not tied to any specific day or extremes or counts of miserable days or anything like that. They will be more like a reflection of what we might expect a reasonable day to feel like during that month.

  Once we have a source for our temperatures and the approximation for extracting relative humidity, we need something to tell us how badly the animals might suffer. Here, we turn to the "temperature humidity index".

  THI = T_actual - (0.55 - 0.55 * RH/100) * (T_actual - 58) [temperatures in Fahrenheit]

  (The seemingly early reference is: Kelly, C. F., and T. E. Bond. 1971. Bioclimatic factors and their measurements. Page 7 in A Guide to Environmental Research in Animals. Natl. Acad. Sci., Washington, DC.)

  A little algebra reveals that the THI is the same as the actual temperature (in F) when the relative humidity is 100% (and at 58F, there is no effect from humidity). It is decreased (that is, we are rewarded in a way) for dryness. This is kind of the opposite of the usual "feels-like" heat index for people which starts at the actual temperature (and assumes some very low humidity like 30%) and then increases/penalizes for humidity.

  We also learn interesting things about what THI values correspond to different levels of suffering for dairy cows: when the THI hits 68, they start to suffer (especially genetically modern cows under contemporary management); at 72, they experience mild stress; 80 brings about moderate stress; and above 90, severe stress can be expected. Death rates hit a break point at around 70 for the minimum THI and 80 for the maximum THI (over the course of the day, presumably) where you start losing more animals than normal. The particular dataset shown by Vitali, et al. starts with a base of about 443 adjusted deaths per thousand for THIs ranging from 10 all the way up. Once they passed those 70/80 THI thresholds, they started losing an additional 240 or so adjusted deaths per thousand for every one point/degree increase in the THI. So, by the time we get to a (maximum-daily-) THI of, say 90, the death index has gone up to about 2500 or roughly 5 times the baseline rate.

  Great: all the pieces are in place. Now, all we have to do is a few temperature conversions and try to keep everything straight.

  We want global maps, so here are our steps:

      Take maps of the monthly average high and low temperatures.
      Compute the average of the high and low to use as our typical temperature to expect through a large portion of the day.
      Combine the typical temperature map with the low temperature map to get the very approximate relative humidity map.
      Then, run the typical temperature map (converted to F) and the relative humidity map through the THI formula.
      Finally, apply the thresholds to THI map to obtain a more easily interpreted map.

  We can do the whole process for a baseline/historical climate for the month of July (that is, more or less the dog days of summer in the northern hemisphere) and then for Hadley model projections under RCP8.5 for a climate like 2055 and 2085.

  The legend color-codes the various stress regimes. Green will represent no problems expected. Yellow is where we start to see problems and so on. Bluish-purple is the extreme stress where emergency attention would need to be paid to the cows to keep them alive.

  thi_stress_july_baseline_legend

  Starting with a reasonable day for July in the near recent past, you can see that most of Europe, all of Canada, and even large portions of the United States show a no-stress situation. Looking at the United States, the big dairy states are California, Wisconsin, New York, Idaho, and Pennsylvania; all except California are mostly green with only a few bits of yellow. Thus, it seems that, despite all the approximations and shortcuts, the map is pretty reasonable in showing that the geography of dairy production in the United States may be located right on the edges of where the cows can be comfortable (but presumably also allows for good access to feed and fodder).

  Baseline/Historical

  thi_stress_july_baseline

  But, how might the situation change in the future? Applying the same process to July averages from the Hadley model under RCP8.5 for the time period around 2055, we see a much more miserable situation for dairy cows. All the core dairy areas in the United States (except possibly Idaho) have turned orange for mild stress. Much of Europe is either at the starting or mild stress levels. Even Canada begins to have some regions with beginning level stress. India and China also see a deterioration of conditions with "moderate stress" taking over large swaths.

  Hadley/RCP8.5/2055

  thi_stress_july_hadgem2_rcp8p5_2055

  And pushing farther out to around 2085, there could be some serious unpleasantry. Most of the traditional USA dairy regions are in moderate stress. Europe is enveloped with mild stress. And, bits of China and India progress to severe stress.

  Hadley/RCP8.5/2085

  thi_stress_july_hadgem2_rcp8p5_2085

  While the idea presented here is just a preliminary analysis, it shows in a graphical way places on the earth that are going to be adversely impacted enough by climate change that it will create a poor environment for livestock (increasing mortality and decreasing productivity), as well as for people who would work outdoors or spend considerable time outdoors.  Much of the previous work done with climate change focused on just temperature.  Here, we took the analysis one step further, recognizing that people and cattle are stressed by the combined effects of heat and humidity.  With continued research, we will be able to improve the estimates to such an extent that we should be able to count the toll climate change will take on the health and productivity of both man and beast.

  Naturally, others have looked into this idea with slightly different approaches. For further reading, see:

      A city-based look at human misery. http://www.climatecentral.org/news/danger-days-on-rise-in-us-cities-19322
      A general discussion of risk to livestock during heat waves, focused on USA. http://www.c2es.org/blog/huberd/risk-livestock-during-heat-waves
      An economic estimate of climate changes costs for US dairy production. http://www.ers.usda.gov/publications/err-economic-research-report/err175.aspx

  Sources

      http://www.progressivedairy.com/topics/herd-health/how-do-i-determine-how-do-i-calculate-temperature-humidity-index-thi ; http://www.extension.umn.edu/agriculture/dairy/health-and-comfort/easing-milking-time-heat-stress/easing-milking-time-heat-stress-humidity-index.pdf ;
      https://www.heatstress.info/heatstressinfo/TemperatureHumidityIndexCattle/tabid/1232/Default.aspx
      http://www.agweb.com/assets/1/6/revisiting_the_temperature_humidity_index2.pdf
      Bernabucci, U., Lacetera, N., Baumgard, L. H., Rhoads, R. P., Ronchi, B., & Nardone, A. (2010). Metabolic and hormonal acclimation to heat stress in domesticated ruminants. Animal, 4(07), 1167-1183.
      http://dspace.unitus.it/bitstream/2067/1536/1/FINAL%20ANIMAL%20repo_A77rkGLY.pdf
      Lawrence, Mark. 2005. The Relationship between Relative Humidity and the Dewpoint Temperature in Moist Air: A Simple Conversion and Applications. Bulletin of the American Meteorological Society. pp. 225-233. DOI:10.1175/BAMS-86-2-225; http://journals.ametsoc.org/doi/abs/10.1175/BAMS-86-2-225
      Vitali, A. et al. (2009). Seasonal pattern of mortality and relationships between mortality and temperature-humidity index in dairy cows. Journal of Dairy Science , Volume 92 , Issue 8 , 3781 - 3790. http://dx.doi.org/10.3168/jds.2009-2127
      http://www.journalofdairyscience.org/article/S0022-0302(09)70700-3/fulltext?refuid=S0022-0302(13)00746-7&refissn=0022-0302


       */

}
