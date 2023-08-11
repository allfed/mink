package org.ifpri_converter;

import java.io.BufferedReader;
// import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
// import java.util.concurrent.*;

public class MagicHCgridMatcherForMappingRawSimulations {

  public static void main(String commandLineOptions[]) throws Exception {

    //		Date startTime = new Date();

    ////////////////////////////////////////////
    // handle the command line arguments...
    ////////////////////////////////////////////

    System.out.print("command line arguments: \n");
    for (int i = 0; i < commandLineOptions.length; i++) {
      System.out.print(i + " " + commandLineOptions[i] + "\n");
    }
    System.out.println();

    if (commandLineOptions.length == 0) {
      System.out.println(
          "Usage: org.ifpri_converter.MagicHCgridMatcherForMappingRawSimulations cell_info_file"
              + " data_file output_file\n"
              + "column_designations_csv values_to_find_csv\n"
              + "\n"
              + "The point of it is to take the HarvestChoice standardized grid ID numbers and"
              + " associate them with\n"
              + "some sort of attribute so we can import them into GRASS easily. This is"
              + " accomplished through Ricky's\n"
              + "r.in.new/r.out.new table and geography modules for GRASS (they really need to be"
              + " renamed).\n"
              + "\n"
              + "The cell_info_file file should be the tab delimited list of pixels of the form:"
              + " cell_id<tab>soil_number<tab>planting month\n"
              + "with one pixel on each line.\n"
              + "\n"
              + "The data_file should be tab delimited and sorted by cell id number (whichever"
              + " column that is). The\n"
              + "necessary columns are:\n"
              + "* cell id\n"
              + "* climate code\n"
              + "* planting month\n"
              + "* irrigated/rainfed code\n"
              + "* yield\n"
              + "* nitrogen application level\n"
              + "* soil number\n"
              + "* CO2 concentration\n"
              + "\n"
              + "The column_designations_csv should be a comma separated list specifying which"
              + " column INDEX corresponds\n"
              + "to each of the values in the above list (and in that order).\n"
              + "\n"
              + "The values_to_find_csv should be a comma separated list specifying the specific"
              + " values desired for the\n"
              + "following attributes in this order:\n"
              + "* climate code\n"
              + "* irrigated/rainfed code\n"
              + "* nitrogen application level\n"
              + "* CO2 concentration\n"
              + "\n"
              + "The output columns will be in the order:\n"
              + "* cellNumberInMap\n"
              + "* soilNumberInMap\n"
              + "* plantingMonthInMap\n"
              + "* ClimateCode\n"
              + "* WaterSource\n"
              + "* Yield\n"
              + "* Nitrogen\n"
              + "* Co2\n"
              + "* currentDataLineNumber\n");
      System.exit(1);
    }

    String cellIDSoilNumberFile = commandLineOptions[0];
    String dataFile = commandLineOptions[1];
    String outputBaseName = commandLineOptions[2];
    String columnDesignationsCSV = commandLineOptions[3];
    String valuesToFindCSV = commandLineOptions[4];

    // MAGIC NUMBERS!!!!
    String magicDelimiter = "\t";
    int badValueCode = -9999;

    // parse the CSV inputs
    String[] columnDesignationsSplit = columnDesignationsCSV.split(",");
    String[] valuesToFindSplit = valuesToFindCSV.split(",");

    int cellIDColumn = Integer.parseInt(columnDesignationsSplit[0]);
    int climateCodeColumn = Integer.parseInt(columnDesignationsSplit[1]);
    int plantingMonthColumn = Integer.parseInt(columnDesignationsSplit[2]);
    int waterSourceColumn = Integer.parseInt(columnDesignationsSplit[3]);
    int yieldColumn = Integer.parseInt(columnDesignationsSplit[4]);
    int nitrogenColumn = Integer.parseInt(columnDesignationsSplit[5]);
    int soilNumberColumn = Integer.parseInt(columnDesignationsSplit[6]);
    int co2Column = Integer.parseInt(columnDesignationsSplit[7]);

    int climateCodeToFind = Integer.parseInt(valuesToFindSplit[0]);
    int waterSourceToFind = Integer.parseInt(valuesToFindSplit[1]);
    int nitrogenToFind = Integer.parseInt(valuesToFindSplit[2]);
    int co2ToFind = Integer.parseInt(valuesToFindSplit[3]);

    System.out.println("climateCodeToFind = " + climateCodeToFind);
    System.out.println("waterSourceToFind = " + waterSourceToFind);
    System.out.println("nitrogenToFind    = " + nitrogenToFind);
    System.out.println("co2ToFind         = " + co2ToFind);

    // open up attribute file
    FileReader cellAndSoilStream = new FileReader(cellIDSoilNumberFile);
    BufferedReader cellAndSoilIn = new BufferedReader(cellAndSoilStream);

    FileReader dataFileStream = new FileReader(dataFile);
    BufferedReader dataIn = new BufferedReader(dataFileStream);

    // open out output files
    String dataOutName = outputBaseName + "_data.txt";
    System.out.println("data out name = " + dataOutName);
    FileOutputStream dataOutStream = new FileOutputStream(dataOutName);
    PrintWriter dataOut = new PrintWriter(dataOutStream);

    // set up a for loop concerning the attribute file

    String cellIDSoilNumberLineContents = null;
    String[] cellSplit = null;
    String dataLineContents = null;
    String[] dataSplit = null;

    int cellNumberInMap = -900;
    int soilNumberInMap = -10123;
    int plantingMonthInMap = -1044;

    int dataCellNumber = -1555;
    int dataClimateCode = -2666;
    int dataPlantingMonth = -3777;
    int dataWaterSource = -4888;
    int dataYield = -5999;
    int dataNitrogen = -8111;
    int dataSoilNumber = -9222;
    int dataCo2 = -16333;

    boolean cellNotInDataTable = false;
    boolean everythingMatches = false;
    boolean noMatchingValuesFound = false;

    // read the first line of each file to initialize the loop
    cellIDSoilNumberLineContents = cellAndSoilIn.readLine();
    dataLineContents = dataIn.readLine();
    dataSplit = dataLineContents.split(magicDelimiter);
    dataCellNumber = Integer.parseInt(dataSplit[0]); // Beware the MAGIC NUMBER!!!

    int nCellLinesProcessed = 0;
    int currentDataLineNumber = 1;
    while (cellIDSoilNumberLineContents != null) {

      cellSplit = cellIDSoilNumberLineContents.split(magicDelimiter);

      // figure out which cell code we're looking for
      cellNumberInMap = Integer.parseInt(cellSplit[0]); // Beware the MAGIC NUMBER!!!

      // initialize a flag
      cellNotInDataTable = false;

      // when we get here, would should have already read a line where the
      // dataCellNumber is >= cellNumberInMap
      //
      // now we need to check if is is == or not

      //			System.out.println(".dCN = " + dataCellNumber + "; cNIM = " + cellNumberInMap);

      if (dataCellNumber > cellNumberInMap) {
        // we have overshot. that is, there is no data for the cell the map wants
        // to match to. we will record a "missing data" value.

        // create a bunch of missing values for the data values
        dataClimateCode = badValueCode;
        dataWaterSource = badValueCode;
        dataYield = badValueCode;
        dataNitrogen = badValueCode;
        dataCo2 = badValueCode;

        soilNumberInMap = badValueCode;
        plantingMonthInMap = badValueCode;

        // actually do the output
        dataOut.print(cellNumberInMap + magicDelimiter);
        dataOut.print(soilNumberInMap + magicDelimiter);
        dataOut.print(plantingMonthInMap + magicDelimiter);
        dataOut.print(dataClimateCode + magicDelimiter);
        dataOut.print(dataWaterSource + magicDelimiter);
        dataOut.print(dataYield + magicDelimiter);
        dataOut.print(dataNitrogen + magicDelimiter);
        dataOut.print(dataCo2 + magicDelimiter);
        dataOut.print(currentDataLineNumber + "\n");

        cellNotInDataTable = true;
        //				continue;
      }

      // this relies on already having read some line in the data file...
      //			System.out.println("..dCN = " + dataCellNumber + "; cNIM = " + cellNumberInMap);
      while (dataCellNumber < cellNumberInMap) {
        // step through the data file until we get a high enough cell number; if necessary
        //				System.out.println("...dCN = " + dataCellNumber + "; cNIM = " + cellNumberInMap);
        currentDataLineNumber++;
        dataLineContents = dataIn.readLine();
        if (dataLineContents != null) {
          dataSplit = dataLineContents.split(magicDelimiter);
          dataCellNumber = Integer.parseInt(dataSplit[cellIDColumn]);
        } else {
          dataCellNumber = Integer.MAX_VALUE;
          System.out.println(
              "    == hit end of data file, setting dataCellNumber to Integer.MAX_VALUE ("
                  + dataCellNumber
                  + ")");
        }
      }

      // now, we need to repeat that "greater than" thing...
      if ((dataCellNumber > cellNumberInMap) && !cellNotInDataTable) {
        // we have overshot. that is, there is no data for the cell the map wants
        // to match to. we will record a "missing data" value.
        //				System.out.println("....dCN = " + dataCellNumber + "; cNIM = " + cellNumberInMap +
        // "(overshot second search)");

        // create a bunch of missing values for the data values
        dataClimateCode = badValueCode - 2;
        dataWaterSource = badValueCode - 2;
        dataYield = badValueCode - 2;
        dataNitrogen = badValueCode - 2;
        dataCo2 = badValueCode - 2;

        soilNumberInMap = badValueCode - 2;
        plantingMonthInMap = badValueCode - 2;

        // actually do the output
        dataOut.print(cellNumberInMap + magicDelimiter);
        dataOut.print(soilNumberInMap + magicDelimiter);
        dataOut.print(plantingMonthInMap + magicDelimiter);
        dataOut.print(dataClimateCode + magicDelimiter);
        dataOut.print(dataWaterSource + magicDelimiter);
        dataOut.print(dataYield + magicDelimiter);
        dataOut.print(dataNitrogen + magicDelimiter);
        dataOut.print(dataCo2 + magicDelimiter);
        dataOut.print(currentDataLineNumber + "\n");

        cellNotInDataTable = true;
        //				continue;
      }

      if (!cellNotInDataTable) {
        //				if (dataCellNumber != cellNumberInMap) {
        //					System.out.println("   we have a problem at dCN = " + dataCellNumber + " ; cNIM = " +
        // cellNumberInMap);
        //				}

        // supposing we get here, it means that we are on a line whose cell number
        // is exactly equal to the one we are looking for
        //
        // now, we must look within that until we find the combination of values we're looking for

        // since we will actually need them, let's parse the text values from the map to match
        soilNumberInMap = Integer.parseInt(cellSplit[1]); // Beware the MAGIC NUMBER!!!
        plantingMonthInMap = Integer.parseInt(cellSplit[2]); // Beware the MAGIC NUMBER!!!

        // and now, let's get the info from the data file
        dataClimateCode = Integer.parseInt(dataSplit[climateCodeColumn]);
        dataPlantingMonth = Integer.parseInt(dataSplit[plantingMonthColumn]);
        dataWaterSource = Integer.parseInt(dataSplit[waterSourceColumn]);
        dataYield = (int) Double.parseDouble(dataSplit[yieldColumn]);
        dataNitrogen = Integer.parseInt(dataSplit[nitrogenColumn]);
        dataSoilNumber = Integer.parseInt(dataSplit[soilNumberColumn]);
        dataCo2 = Integer.parseInt(dataSplit[co2Column]);

        // finally, search through until we find a matching row...
        everythingMatches =
            (climateCodeToFind == dataClimateCode
                && plantingMonthInMap == dataPlantingMonth
                && waterSourceToFind == dataWaterSource
                && nitrogenToFind == dataNitrogen
                && soilNumberInMap == dataSoilNumber
                && co2ToFind == dataCo2);

        noMatchingValuesFound = false;
        while (!everythingMatches) {
          // since it doesn't match, we need to read another line...
          currentDataLineNumber++;
          dataLineContents = dataIn.readLine();
          dataSplit = dataLineContents.split(magicDelimiter);

          dataCellNumber = Integer.parseInt(dataSplit[0]); // Beware the MAGIC NUMBER!!!
          if (dataCellNumber > cellNumberInMap) {
            // output a nonsense code and break out
            dataClimateCode = badValueCode - 1;
            dataWaterSource = badValueCode - 1;
            dataYield = badValueCode - 1;
            dataNitrogen = badValueCode - 1;
            dataCo2 = badValueCode - 1;

            dataOut.print(cellNumberInMap + magicDelimiter);
            dataOut.print(soilNumberInMap + magicDelimiter);
            dataOut.print(plantingMonthInMap + magicDelimiter);
            dataOut.print(dataClimateCode + magicDelimiter);
            dataOut.print(dataWaterSource + magicDelimiter);
            dataOut.print(dataYield + magicDelimiter);
            dataOut.print(dataNitrogen + magicDelimiter);
            dataOut.print(dataCo2 + magicDelimiter);
            dataOut.print(currentDataLineNumber + "\n");

            noMatchingValuesFound = true;
            break;
          }
          dataClimateCode = Integer.parseInt(dataSplit[climateCodeColumn]);
          dataPlantingMonth = Integer.parseInt(dataSplit[plantingMonthColumn]);
          dataWaterSource = Integer.parseInt(dataSplit[waterSourceColumn]);
          dataYield = (int) Double.parseDouble(dataSplit[yieldColumn]);
          dataNitrogen = Integer.parseInt(dataSplit[nitrogenColumn]);
          dataSoilNumber = Integer.parseInt(dataSplit[soilNumberColumn]);
          dataCo2 = Integer.parseInt(dataSplit[co2Column]);

          // test this line
          everythingMatches =
              (climateCodeToFind == dataClimateCode
                  && plantingMonthInMap == dataPlantingMonth
                  && waterSourceToFind == dataWaterSource
                  && nitrogenToFind == dataNitrogen
                  && soilNumberInMap == dataSoilNumber
                  && co2ToFind == dataCo2);
          // if dataCellNumber > cellNumberInMap; i.e., we have overshot when looking within this
          // pixel code

        } // done looking for a match

        if (!noMatchingValuesFound) {
          dataOut.print(cellNumberInMap + magicDelimiter);
          dataOut.print(soilNumberInMap + magicDelimiter);
          dataOut.print(plantingMonthInMap + magicDelimiter);
          dataOut.print(dataClimateCode + magicDelimiter);
          dataOut.print(dataWaterSource + magicDelimiter);
          dataOut.print(dataYield + magicDelimiter);
          dataOut.print(dataNitrogen + magicDelimiter);
          dataOut.print(dataCo2 + magicDelimiter);
          dataOut.print(currentDataLineNumber + "\n");
        }
      } // end of if we found the cell...

      //			int climateCodeToFind   = Integer.parseInt(valuesToFindSplit[0]);
      //			int waterSourceToFind   = Integer.parseInt(valuesToFindSplit[1]);
      //			int nitrogenToFind      = Integer.parseInt(valuesToFindSplit[2]);
      //			int co2ToFind           = Integer.parseInt(valuesToFindSplit[3]);

      // read the next cell to look for...
      nCellLinesProcessed++;
      cellIDSoilNumberLineContents = cellAndSoilIn.readLine();
    } // end of while looking through the map to match to....

    // a simple status display
    //		if (nAtLinesRead % displayValue != 0) {
    //		} else {
    //		if (nLinesDisplayed % wrapValue != 0) {
    //		System.out.print(nAtLinesRead + ".");
    //		} else {
    //		System.out.println(nAtLinesRead);
    //		}
    //		nLinesDisplayed++;
    //		}

    dataOut.flush();

    dataOut.close();
    dataOutStream.close();

    cellAndSoilIn.close();
    cellAndSoilStream.close();

    dataIn.close();
    dataFileStream.close();

    // write out the header file
    File infoFileToWrite = new File(outputBaseName + "_data.info.txt");
    FileWriter outInfoStream = new FileWriter(infoFileToWrite);
    PrintWriter outInfoWriterObject = new PrintWriter(outInfoStream);

    int magicNumberOfOutputColumns = 9;
    outInfoWriterObject.print(nCellLinesProcessed + "\t = Number of Rows\n");
    outInfoWriterObject.print(magicNumberOfOutputColumns + "\t = Number of Columns\n");
    outInfoWriterObject.print(
        (nCellLinesProcessed * magicNumberOfOutputColumns) + "\t = Total Number of Elements\n");
    outInfoWriterObject.print(1 + "\t = The MultiFormatMatrix format the matrix was stored in\n");
    outInfoWriterObject.print(
        magicDelimiter + "\t = The string used to delimit elements in the Rows");

    outInfoWriterObject.flush();
    outInfoWriterObject.close();

    System.out.println("-- all done " + nCellLinesProcessed + " ; " + currentDataLineNumber + "--");
  } // main
}
