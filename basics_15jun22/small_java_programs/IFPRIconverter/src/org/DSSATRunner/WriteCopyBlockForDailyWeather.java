package org.DSSATRunner;

//import java.io.*;

import org.R2Useful.*;

public class WriteCopyBlockForDailyWeather {

    // the idea here is to take a table of climate averages and then generate daily weather from them
    // so that we have a whole bunch of DSSAT-style daily weather files laying around in some systematic
    // fasion...

    public static void main(String commandLineOptions[]) throws Exception {

        ////////////////////
        // magical things //
        ////////////////////

        ////////////////////////////////////////////
        // handle the command line arguments...
        ////////////////////////////////////////////
        System.out.println("REALLY REALLY AMAZING!!!!!!!!!!!!");
        System.out.println(commandLineOptions);

        String sourcePath       =                commandLineOptions[0];
        String destinationPath  =                commandLineOptions[1];
        String gisDataTablePath =                commandLineOptions[2];

//      final long defaultIDColIndex = 6;
//      long   idColIndex       = -1;

//      if (commandLineOptions.length == 4) {
//          idColIndex = Long.parseLong(commandLineOptions[3]);
//      } else {
//          idColIndex = defaultIDColIndex;
//      }

        
        // read in the fundamental data
        // Beware the MAGIC NUMBER!!! gonna force these onto disk. for tiny stuff, it should run fast enough
        // that it doesn't matter. for big stuff, we'll want disk...
        int formatIndexToForce = 1;
//      MultiFormatMatrix dataMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisDataTablePath + "_data",formatIndexToForce);
        MultiFormatMatrix geogMatrix = MatrixOperations.read2DMFMfromTextForceFormat(gisDataTablePath + "_geog",formatIndexToForce);

        int nLinesInDataFile = (int)geogMatrix.getDimensions()[0];
//      double idNumber = -1; // this will be the pixel-specific id number...
        String fullSourceName = null;

        double latitude, longitude;
        
        for (int lineIndex = 0; lineIndex < nLinesInDataFile; lineIndex++) {

            // Beware the MAGIC NUMBER!!!
//          idNumber = geogMatrix.getValue(lineIndex,idColIndex); // 67 is the column index for
             latitude = geogMatrix.getValue(lineIndex,2); // Beware the MAGIC NUMBER!!!
            longitude = geogMatrix.getValue(lineIndex,3); // Beware the MAGIC NUMBER!!!
            
//          finalWeatherFileLocation = outputDirectory + "/" + outputFilename + "_" + latitude + "n_" + longitude + "e" + ".WTH";

//          fullSourceName = sourcePath + "_" + idNumber + ".WTH";
//          fullSourceName = sourcePath + "_" + latitude + "n_" + longitude + "e" + ".WTH";
            fullSourceName = sourcePath + "_" + latitude + "_" + longitude + ".WTH";

            
            System.out.println("cp " + fullSourceName + " " + destinationPath);

        } // for lineIndex


        //////////////
        // all done //
        //////////////


    } // main



}
