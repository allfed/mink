package org.ifpri_converter;
import java.util.Date;
import org.R2Useful.*;
import java.io.*;
import ucar.nc2.*;
import ucar.ma2.*;


//import java.util.concurrent.*;

public class NetcdfFirstTry {
  
  
  public static void main(String commandLineOptions[]) throws Exception {


    Date startTime = new Date();
    
    
    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////
    
    System.out.print("command line arguments: ");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(" " + commandLineOptions[i]);
    }
    System.out.println();
    
    // initialize some variables
    String outputString = "";
    String statusString = null;
    
    // stuff for the status checks
    int nTotalChecks = 100; // how many updates we want to see...
    long startTimeMillis = System.currentTimeMillis();
    
    // hacking from http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/tutorial/NetcdfFile.html
    
    // specify the file to deal with, we'll move this to command line eventually
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_cassava_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_groundnut_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_millet_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_potato_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_rice_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_maize_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_sorghum_apprate_fill_NPK_0.5.nc4";
//    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_soybean_apprate_fill_NPK_0.5.nc4";
    String inputFileName  = "D:/rdrobert/global_futures/fertilizer_application_rate_maps/agmip_wheat_apprate_fill_NPK_0.5.nc4";
    
    
    
    
    
    
    
    
    
    String outputFileName = inputFileName + ".txt";
    
    File outputFileObject = new File(outputFileName);
    if (outputFileObject.exists()) {
    	outputFileObject.delete();
    }
    
    float anUpperBoundAboveWhichValuesWillBeIgnored = 1E19F;
    
    NetcdfFile ncfile = null;

    // open up the file
    ncfile = NetcdfFile.open(inputFileName);

    // grab some rudimentary information
    System.out.println(ncfile.getDetailInfo());
    
    // let's see if we can get the latitude array...
    String latitudeName = "latitude";
    Variable theLatitude = ncfile.findVariable(latitudeName);
    
    // try to get some dimensions for the variable
    Dimension latitudeDimension = theLatitude.getDimension(0);
    int latitudeLength = latitudeDimension.getLength();

    // let's see if we can get the latitude array...
    String longitudeName = "longitude";
    Variable theLongitude = ncfile.findVariable(longitudeName);
    
    // try to get some dimensions for the variable
    Dimension longitudeDimension = theLongitude.getDimension(0);
    int longitudeLength = longitudeDimension.getLength();
    
    // let's see if we can get the latitude array...
    String nitrogenName = "Napprate";
    Variable theRaster = ncfile.findVariable(nitrogenName);

    // try to get some dimensions for the variable
//    System.out.println("lat = " + theLatitude.getDataType() + " N = " + theNitrogen.getDataType());
//    int longitudeLength = longitudeDimension.getLength();

    
    
    System.out.println("lat length = " + latitudeLength + "; lon length = " + longitudeLength);
        
    // try to read all of it
    Array  latitudeData =  theLatitude.read();
    Array longitudeData = theLongitude.read();
    Array  rasterData =  theRaster.read();
    
    // ok, and now, i am too lazy to idiot proof this, so i will be just assuming that the
    // fertilizer values are matched up nicely with the appropriate array length
    
    float latitudeHere  = Float.NaN;
    float longitudeHere = Float.NaN;
    float rasterValueHere  = Float.NaN;

    Index2D readIndex = new Index2D(new int[] {latitudeLength , longitudeLength});

    int pixelIndex = 0; // a counter for the status update
    int nValidPixels = latitudeLength * longitudeLength; // the total number of cases to consider
    
    for (int latitudeIndexToPull = 0; latitudeIndexToPull < latitudeLength; latitudeIndexToPull++) {
    	for (int longitudeIndexToPull = 0; longitudeIndexToPull < longitudeLength; longitudeIndexToPull++) {
    		
    		// figure out the real latitude/longitude values
        latitudeHere  =  latitudeData.getFloat( latitudeIndexToPull);
        longitudeHere = longitudeData.getFloat(longitudeIndexToPull);
        
        // figure out the value stored there
        rasterValueHere  = rasterData.getFloat(readIndex.set(latitudeIndexToPull,longitudeIndexToPull));

        if (rasterValueHere < anUpperBoundAboveWhichValuesWillBeIgnored) {
        	// append it to our fabulous output file
        	outputString = latitudeHere + "," + longitudeHere + "," + rasterValueHere; 
        	FunTricks.appendLineToTextFile(outputString, outputFileName, true);
        }

    		statusString = FunTricks.statusCheck(pixelIndex, nValidPixels, nTotalChecks, startTimeMillis);
    		
    		if (statusString != null) {
    			System.out.println(statusString);
    		}

    		// bump up the counter
    		pixelIndex++;
    	}
    }
    
    // actually write the reformatted data down...
//    FunTricks.writeStringToFile(outputString, outputFileName);
    
    

    Date endTime = new Date();
    
    double duration = (endTime.getTime() - startTime.getTime()) / 1000.0;
    
    System.out.println("Finished execution! duration = " + duration + " seconds or " + (duration/60) + " minutes or " + (duration/3600) + " hours" );
    
    
  } // main
  
}



