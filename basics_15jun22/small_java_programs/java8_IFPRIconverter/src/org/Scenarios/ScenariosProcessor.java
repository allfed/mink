/*
This ScenariosProcessor class contains methods for processing the results of running crop modeling scenarios.

Key responsibilities:
- Converting yields to wet weight
- Averaging yields across years
- Finding best yields for each pixel
- Generating output rasters
- Creating PNG visualizations

Main methods:
- processScenarios: Handles looping through scenarios and processing results
- convertToWetWeightIfNeeded: Converts yields to wet weight if needed
- averageAcrossYears: Averages yields across simulation years
- findBestYieldsAndBestMaturity: Finds best yield and planting month
- createEachYearPNG: Creates PNG visualizations for each year

Dependencies:
- Relies on output data generated from running scenarios
- Calls BashScripts methods for GRASS GIS processing
- Uses utility methods from Scenarios class

*/

package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class ScenariosProcessor {

  static HashMap<String, Float> coefficients_to_get_wetweight_from_dryweight;

  public static void processScenarios(
      String script_folder,
      List<PlantingScenario> scenariosAndPMList,
      String run_parameters_csv_folder,
      Scenarios scenarios)
      throws InterruptedException, IOException {

    String moisture_csv_location = run_parameters_csv_folder + "moisture_contents.csv";

    coefficients_to_get_wetweight_from_dryweight =
        createWetWeightConversionMap(moisture_csv_location);

    // NOTE: IF YOU'VE PROCESSED ALREADY, THEN YOU CAN COMMENT THIS LOOP OUT
    // (if you've changed something near the end of the processing code it will save you
    // time)

    for (PlantingScenario scenario_and_pm : scenariosAndPMList) {
      // loop for each scenario, then each planting month

      String[][] raster_names_all_years_dry_allmonths =
          scenarios.raster_names_all_years_dry[scenario_and_pm.scenarioNumber];

      String[] raster_name_all_years_this_month_dry =
          raster_names_all_years_dry_allmonths[scenario_and_pm.plantingMonth];

      // String[][] raster_names_all_years_maturity_allmonths =
      //     scenarios.raster_names_all_years_maturity[scenario_and_pm.scenarioNumber];

      // String[] raster_name_all_years_this_month_maturity =
      //     raster_names_all_years_maturity_allmonths[scenario_and_pm.plantingMonth];

      String[][] raster_names_all_years_wet_or_dry_allmonths =
          scenarios.raster_names_all_years_wet_or_dry[scenario_and_pm.scenarioNumber];

      String[] raster_name_all_years_this_month_wet_or_dry =
          raster_names_all_years_wet_or_dry_allmonths[scenario_and_pm.plantingMonth];

      BashScripts.assembleResults(
          script_folder,
          scenarios.snx_name[scenario_and_pm.scenarioNumber],
          scenarios.dssat_executable,
          scenarios.dssat_folder,
          scenarios.co2_level[scenario_and_pm.scenarioNumber],
          scenarios.crop_name[scenario_and_pm.scenarioNumber],
          scenarios.weather_prefix[scenario_and_pm.scenarioNumber],
          scenarios.weather_folder[scenario_and_pm.scenarioNumber],
          scenarios
              .output_stats_basenames[scenario_and_pm.scenarioNumber][
              scenario_and_pm.plantingMonth],
          // scenarios
          // .yield_names[scenario_and_pm.scenarioNumber][scenario_and_pm.plantingMonth],
          scenarios.fertilizer_scheme[scenario_and_pm.scenarioNumber],
          scenarios.n_chunks,
          scenarios.run_descriptor[scenario_and_pm.scenarioNumber],
          scenarios.nsres[scenario_and_pm.scenarioNumber],
          scenarios.ewres[scenario_and_pm.scenarioNumber]);

      String[] non_yield_parameters_to_save = new String[] {"n_bad_things", "real_maturity_mean"};

      String[] outputs_to_save =
          generateColumnsToMakeRastersFromDataFiles(
              scenarios.years, scenarios.real_or_happy, non_yield_parameters_to_save);

      readDSSAToutputs(
          script_folder,
          scenarios
              .output_stats_basenames[scenario_and_pm.scenarioNumber][
              scenario_and_pm.plantingMonth],
          scenarios.years.length,
          outputs_to_save);

      for (int year_index = 0; year_index < scenarios.years.length; year_index++) {
        convertToWetWeightIfNeeded(
            scenarios.calculate_as_wet_weight,
            script_folder,
            scenarios.crop_name[scenario_and_pm.scenarioNumber], // input variable
            raster_name_all_years_this_month_dry[year_index], // input raster
            raster_name_all_years_this_month_wet_or_dry[year_index], // output raster
            coefficients_to_get_wetweight_from_dryweight);
        setNegativeValuesToZero(
            script_folder, raster_name_all_years_this_month_wet_or_dry[year_index]);
      }

      // if we are averaging yields, average the yields across the years being simulated
      // such that each month has an average yield over the years stored in
      // raster_names_average_year
      if (scenarios.average_yields) {
        averageAcrossYears(
            script_folder,
            scenarios.years,
            scenarios.minimum_yield, // minimum allowed yield to average
            raster_name_all_years_this_month_wet_or_dry,
            scenarios
                .raster_names_average_year[scenario_and_pm.scenarioNumber][
                scenario_and_pm.plantingMonth],
            scenarios.results_folder[scenario_and_pm.scenarioNumber]);
      } // END average_yields
    } // end looping through each planting month and each scenario

    // // NOTE: END OF THE PART YOU COULD COMMENT OUT TO SPEED UP RE-PROCESSING

    // just loop through the scenarios, because we have stored all the lists of planting month
    // rasters as a function of scenario number in the 2d array rasters
    for (int i = 0; i < scenarios.n_scenarios; i++) {

      // 1. [This step is optional and does not play a part in overall yield calculations, but can
      // be helpful for later analysis] First, if calculate_each_year_best_month is true, we loop
      // through the yield for all years, and find the yield from the best planting month for each
      // grid cell, saving this to
      // best_planting_month_yield_name_all_years. This occurs for each year, not just the average
      // of years,
      // producing many more rasters than the next usage of findBestYields.averageAcrossYears

      // if we choose to find the best yields, find best yields for each scenario (choosing best
      // planting month)
      // ToDoubleFunction: add scenarios.raster_names_best_month_all_years[i] as the best planting
      // month for all years

      if (scenarios.calculate_each_year_best_month) {
        calculateBestYieldAllYears(
            script_folder,
            scenarios.years,
            scenarios.planting_months,
            scenarios.raster_names_all_years_wet_or_dry[i],
            scenarios.best_planting_month_yield_name_all_years[i],
            scenarios.best_planting_month_name_all_years[i],
            // scenarios.best_month_maturity_result_name_all_years[i], // (can maybe be a future
            // feature)
            scenarios.results_folder[i]);
      }

      // 2. If find_best_yields is true, raster_names_average_year has been calculated as the
      // yield of the best planting month ( determined after averaging all years)

      if (scenarios.find_best_yields) {
        findBestYieldsAndBestMaturity(
            script_folder,
            scenarios.planting_months,
            scenarios.raster_names_average_year[i],
            scenarios.best_planting_month_yield_name[i],
            scenarios.best_planting_month_name[i],
            scenarios.output_maturity_mean[i],
            scenarios.best_planting_month_maturity_name[i],
            scenarios.results_folder[i]);
      }

      // create a png for the raster for each year if create_each_year_png is true

      if (scenarios.create_each_year_png) {
        createEachYearPNG(
            scenarios.planting_months,
            scenarios.years,
            script_folder,
            scenarios.results_folder[i],
            scenarios.raster_names_all_years_wet_or_dry[i]);
      }
    } // end scenario number loop
  } // END processScenarios

  public static HashMap<String, Float> createWetWeightConversionMap(String csv_location)
      throws InterruptedException, IOException {
    String[] initFileContents = FunTricks.readTextFileToArray(csv_location);

    // split the string to get the column titles
    String[] column_titles = initFileContents[0].split(",");

    // make sure the column titles for the CSV match what java expects
    try {
      assert column_titles[0].equals("crop");
      assert column_titles[1].equals("moisture_content_percent_mass");
      assert column_titles[2].equals("source");
    } catch (AssertionError e) {
      System.out.println(
          "Error: The first line of the csv file must contain the column titles in the code block"
              + " (specified above this error message)"
              + " (moisture)");

      System.exit(0); // logging or any action
    }
    System.out.println("");
    System.out.println("");

    HashMap<String, Float> coefficients = new HashMap<String, Float>();

    // loop through the scenario input csv file and create string arrays with the
    // appropriate value for each scenario index
    for (int i = 0; i < initFileContents.length - 1; i++) {

      // if a blank line occurs in the file, do not continue loading scenarios
      if (initFileContents[i] == null) {
        break;
      }

      String line = initFileContents[i + 1];
      String[] line_arguments = line.split(",");

      // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY
      String crop = line_arguments[0];
      String moisture_percent_string = line_arguments[1];
      float moisture_percent = Float.parseFloat(moisture_percent_string);

      // In this case, we need the wet weight
      // Here's an example calculation:

      // Given:

      //   moisture_fraction = moisture_percent / 100
      //   moisture_fraction * wet_weight = water_weight
      //   water_weight + dry_weight = wet_weight
      //   coefficient_to_get_wetweight_from_dryweight * dry_weight = wet_weight

      // Solving for wet_weight in terms of dry_weight and moisture_percent,

      //   moisture_fraction * wet_weight + dry_weight = wet_weight
      //   moisture_fraction + dry_weight / wet_weight = 1
      //   dry_weight / wet_weight = 1 - moisture_fraction
      //   wet_weight = dry_weight / ( 1 - moisture_fraction )
      //   wet_weight = dry_weight / ( 1 - moisture_percent / 100 )

      // we therefore have:

      // coefficient_to_get_wetweight_from_dryweight
      //    =  wet_weight / dry_weight
      //    =  1 / ( 1 - moisture_percent / 100 )

      float coefficient = 1 / (1 - moisture_percent / 100);

      coefficients.put(crop, coefficient);
    }

    return coefficients;
  }

  public static Float getConversionToWetCoefficient(
      String cropName, HashMap<String, Float> coefficients_to_get_wetweight_from_dryweight)
      throws InterruptedException, IOException {

    Float coefficient = coefficients_to_get_wetweight_from_dryweight.get(cropName);
    if (coefficient == null) {
      throw new IllegalArgumentException(
          "ERROR: No wet weight conversion coefficient found for crop name: "
              + cropName
              + ". \n Available options to get conversion:"
              + coefficients_to_get_wetweight_from_dryweight.keySet().toString());
    }

    return coefficient;
  }

  public static String[] generateColumnsToMakeRastersFromDataFiles(
      String[] years, String real_or_happy, String[] non_yield_parameters_to_save) {
    String[] outputs_to_save = new String[years.length + non_yield_parameters_to_save.length];

    for (int i = 0; i < years.length; i++) {
      outputs_to_save[i] = real_or_happy + "_" + years[i];
    }

    // add the non-year data to make the outputs that aren't just the yield
    for (int i = 0; i < non_yield_parameters_to_save.length; i++) {
      outputs_to_save[i + years.length] = non_yield_parameters_to_save[i];
    }

    return outputs_to_save;
  }

  public static void readDSSAToutputs(
      String script_folder, String fileToProcess, int nyears, String[] column_titles)
      throws InterruptedException, IOException, IllegalArgumentException {

    // Read the required files

    // we take from the to_DSSAT because the geography is not recreated again... it should always be
    // the same after running DSSAT, and thus isn't created in the to_GRASS or chunks_to_GRASS
    String[] geogFile =
        FunTricks.readTextFileToArray(script_folder + "to_DSSAT/" + fileToProcess + "_geog.txt");
    // System.out.println("");
    // System.out.println("reading cols");
    String[] statsColsFile =
        FunTricks.readTextFileToArray(
            script_folder + "to_GRASS/" + fileToProcess + "_STATS.cols.txt");
    // System.out.println("done reading cols");
    // System.out.println("");
    // System.out.println("filename");
    // System.out.println(script_folder+"to_GRASS/" + fileToProcess + "_STATS.txt");
    String[] statsFile =
        FunTricks.readTextFileToArray(script_folder + "to_GRASS/" + fileToProcess + "_STATS.txt");
    String[][] stats_data_col_then_row = extractDataAs2DArray(statsFile);

    // System.out.println(script_folder+"to_GRASS/" + fileToProcess + "_STATS.cols.txt");
    // System.out.println(String.join("\n", statsColsFile));

    // System.out.println("statsFile");
    // System.out.println(script_folder+"to_GRASS/" + fileToProcess + "_STATS.txt");
    // System.out.println(String.join("\n", statsFile));

    // Extract latitude and longitude
    List<String> latLonData = new ArrayList<>();
    for (String line : geogFile) {
      String[] parts = line.split("\t");
      latLonData.add(parts[2] + "\t" + parts[3]);
    }

    // Extract the column list and transform it
    StringBuilder columnListBuilder = new StringBuilder();
    for (String s : statsColsFile) {
      columnListBuilder.append(s).append("\t");
    }
    String columnList = columnListBuilder.toString().replace("\t", "\n");
    String[] columnsArray = columnList.split("\n");

    // System.out.println("columnsArray: " + Arrays.toString(columnsArray));
    // System.out.println("column_titles: " + Arrays.toString(column_titles));
    // System.out.println("column_titles: " + Arrays.toString(latLonData));

    // Concatenate the latitude and longitude data columns as a single string (if needed)
    String stringWithLatLonDataColumns = String.join("\n", latLonData);
    Set<Integer> incomplete_rows = new HashSet<>();
    for (String colname : column_titles) {

      int columnNumber = -1;
      for (int j = 0; j < columnsArray.length; j++) {
        if (columnsArray[j].equals(colname)) {
          columnNumber = j + 1; // +1 to get the 1-based column number
          break;
        }
      }

      // Handle case where the column name was not found
      if (columnNumber == -1) {
        throw new IllegalArgumentException(
            "ERROR: Column name " + colname + " not found. Consider fixing columns");
      }
      String[] singleColumnOfData;
      try {
        singleColumnOfData = stats_data_col_then_row[columnNumber - 1];
      } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("ERROR: The specified column number is out of bounds.");
        System.out.println(
            "       What happened is the Mink3p2daily java code failed to actually generate");
        System.out.println(
            "       the correct number of columns when running. This may be due to a failure of");
        System.out.println(
            "       DSSAT to run (DSSAT crashed due to arithmatic error or memory leak, etc) ");
        System.out.println(
            "       or may have been a badly specified resolution.... perhaps a wrong DSSAT"
                + " version... ");
        System.out.println(
            "       Make sure there were no issues running DSSAT during the DSSAT run you're"
                + " processing and that the expected number of pixels considered");
        System.out.println("       matches the pixels you are trying to process.");
        throw e; // Re-throwing the same exception to terminate the program
      }

      if (latLonData.size() != singleColumnOfData.length) {
        throw new IllegalArgumentException(
            "ERROR: _geog.txt with pixel locations and _STATS.txt with DSSAT results seem to"
                + " disagree in the number of pixels considered. This is likely due to a failure to"
                + " run certain pixels. You need to rerun the gridded DSSAT run, this time ensuring"
                + " all pixels for this crop have been evaluated.\n"
                + "_geog.txt number pixels: "
                + latLonData.size()
                + "\n"
                + "_STATS.txt number pixels: "
                + singleColumnOfData.length
                + "\n");
      }
      // Combine latitude, longitude, and the required data
      List<String> table_to_build_raster = new ArrayList<>();
      for (int i = 0; i < latLonData.size(); i++) {
        table_to_build_raster.add(latLonData.get(i) + "\t" + singleColumnOfData[i]);
      }

      // the below generates rasters:
      //
      // scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber][scenario_and_pm.plantingMonth]_real_or_happy
      //
      // scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber][scenario_and_pm.plantingMonth]_real_or_happy_yearnum (for each year number)
      //
      // scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber][scenario_and_pm.plantingMonth]_real_maturity_mean (for days to maturity)
      //
      // scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber][scenario_and_pm.plantingMonth]_n_bad_things (for error codes which can be found in page 146 of Robertson 2017, "Mink: Details of a global gridded crop modeling system")
      //
      // where output_stats_basenames is roughly:
      //
      // snx_name_co2_level[i]_weather_prefix[i]_month_noGCMcalendar_p0_crop_name[i]__run_descriptor[i]
      BashScripts.generateRasterFromColumns(
          script_folder, table_to_build_raster, fileToProcess + "_" + colname);
      // BashScripts.generateRasterFromColumns(
      //     script_folder, String.join("\n", table_to_build_raster), fileToProcess + "_" +
      // colname);

      // do some error reporting!

      if (colname.equals("n_bad_things")) {
        int maturity_errors = 0;
        int flowering_errors = 0;
        int emergence_errors = 0;
        int other_errors = 0;
        for (int i = 0; i < latLonData.size(); i++) {
          String formatted_bad_things =
              ("000000000000" + singleColumnOfData[i]).substring(singleColumnOfData[i].length());
          int[] numbers = new int[4];

          for (int j = 0; j < 4; j++) {
            String substring = formatted_bad_things.substring(j * 3, (j * 3) + 3);
            numbers[j] = Integer.parseInt(substring);
            if (j == 0) {
              // billions
              maturity_errors += numbers[j];
            } else if (j == 1) {
              // millions
              flowering_errors += numbers[j];
            } else if (j == 2) {
              // thousands
              emergence_errors += numbers[j];
            } else if (i == 3) {
              other_errors += numbers[j];
              // ones
              // honestly the documentation was so bad for this, I have no idea what "extras"
              // means >:[
            }
          }
        }
        if (maturity_errors > 0
            || flowering_errors > 0
            || emergence_errors > 0
            || other_errors > 0) {
          double percentageErrorMaturity =
              ((double) (maturity_errors) / (latLonData.size() * (nyears + 1))) * 100;
          double percentageErrorFlowering =
              ((double) (flowering_errors) / (latLonData.size() * (nyears + 1))) * 100;
          double percentageErrorEmergence =
              ((double) (emergence_errors) / (latLonData.size() * (nyears + 1))) * 100;
          double percentageErrorOther =
              ((double) (other_errors) / (latLonData.size() * (nyears + 1))) * 100;

          System.out.println(fileToProcess + ": ");
          System.out.println(
              "  Bad things (out of "
                  + latLonData.size() * (nyears + 1)
                  + " total cells real for each (year+1) ran real (each year and pixel can"
                  + " add one error, and the warmup year counts too)):\n"
                  + "      Maturity: "
                  + maturity_errors
                  + " ("
                  + String.format("%.1f", percentageErrorMaturity)
                  + "%)\n      Flowering: "
                  + flowering_errors
                  + " ("
                  + String.format("%.1f", percentageErrorFlowering)
                  + "%)\n      Emergence: "
                  + emergence_errors
                  + " ("
                  + String.format("%.1f", percentageErrorEmergence)
                  + "%)\n      Other (\"extras\"): "
                  + other_errors);
        }
      }
    }
  }

  public static String[][] extractDataAs2DArray(String[] lines) {
    if (lines.length == 0) {
      throw new IllegalArgumentException("File is empty");
    }

    int rowCount = lines.length;
    int expectedColumnCount = lines[0].split("\t").length;
    String[][] data = new String[expectedColumnCount][rowCount];

    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      String[] parts = lines[rowIndex].split("\t");

      // Ensure all lines have the same number of columns
      if (parts.length != expectedColumnCount) {
        throw new IllegalArgumentException("Inconsistent number of columns detected in the file.");
      }

      for (int colIndex = 0; colIndex < expectedColumnCount; colIndex++) {
        data[colIndex][rowIndex] = parts[colIndex];
        // data[colIndex][rowIndex] = parts[colIndex].isEmpty() ? "null" : parts[colIndex];
      }
    }

    return data;
  }

  public static void createEachYearPNG(
      String[] planting_months,
      String[] years,
      String script_folder,
      String results_folder,
      String[][] raster_names_all_years_wet_or_dry)
      throws InterruptedException, IOException {

    // This function creates png's at the very end, where all png are shown on the same scale and
    // colormap for easy comparison

    List<String> rasterList = new ArrayList<>();
    for (int planting_month_index = 0;
        planting_month_index < planting_months.length;
        planting_month_index++) {

      String[] raster_name_all_years_this_month_wet_or_dry =
          raster_names_all_years_wet_or_dry[planting_month_index];

      for (int year_index = 0; year_index < years.length; year_index++) {
        rasterList.add(raster_name_all_years_this_month_wet_or_dry[year_index]);
      }
    }
    // Convert the list to an array and pass it to the method
    String[] rasterArray = rasterList.toArray(new String[0]);
    BashScripts.createPNG(script_folder, rasterArray, results_folder);
  }

  public static void calculateBestYieldAllYears(
      String script_folder,
      String[] years, // input variable
      String[] planting_months, // input variable
      String[][] raster_names_all_years_wet_or_dry, // the input rasters
      String[] best_planting_month_yield_name_all_years, // the output rasters
      String[] best_planting_month_name_all_years, // the output rasters
      // String[] best_month_maturity_result_name_all_years, // (maybe implement in the future)
      String results_folder // the output rasters
      ) throws InterruptedException, IOException {
    // calculate the average of the raster yield over time for a given scenario
    for (int year_index = 0; year_index < years.length; year_index++) {
      String[] raster_names_all_months_this_year = new String[planting_months.length];

      String best_planting_month_yield_name = best_planting_month_yield_name_all_years[year_index];
      String best_planting_month_name_this_year = best_planting_month_name_all_years[year_index];
      // String best_month_maturity_result_name_this_year =
      // best_month_maturity_result_name_all_years[year_index];

      for (int planting_month_index = 0;
          planting_month_index < planting_months.length;
          planting_month_index++) {
        raster_names_all_months_this_year[planting_month_index] =
            raster_names_all_years_wet_or_dry[planting_month_index][year_index];
      }

      findBestYields(
          script_folder,
          planting_months,
          raster_names_all_months_this_year, // input rasters
          best_planting_month_yield_name, // the output raster
          best_planting_month_name_this_year, // the output raster for best month
          // best_month_maturity_result_name_this_year, // the output raster for best days to
          // maturity (skipping)
          results_folder);
    }
  }

  public static void setNegativeValuesToZero(
      String script_folder,
      String raster_name_this_year_this_month_wet_or_dry // the input raster to be modified
      ) throws InterruptedException, IOException {
    // sets any negative values to zero. Negative values occur when there are no planting dates
    BashScripts.setNegativeValuesToZero(
        script_folder, raster_name_this_year_this_month_wet_or_dry // input raster to be modified
        );
  }

  public static void convertToWetWeightIfNeeded(
      boolean calculate_as_wet_weight,
      String script_folder,
      String crop_name,
      String raster_name_all_years_this_month_dry, // the input raster
      String raster_name_all_years_this_month_wet_or_dry, // the output raster
      HashMap<String, Float> coefficients_to_get_wetweight_from_dryweight)
      throws InterruptedException, IOException {
    // convert to wet weight by either setting raster_name_all_years_this_month_wet_or_dry to
    // raster_name_all_years_this_month_dry (if we want the dryweight specified by setting
    // calculate_as_wet_weight to false). If calculate_as_wet_weight is true, we import the moisture
    // content csv, then determine the wet weight and scale the raster by the appropriate value to
    // estimate the wet weight for the current crop. DSSAT defaults to calculating the dryweight.

    // if calculating wet weight, assign the coefficient to the imported value,
    // otherwise, set the coefficient to 1
    String coefficient;
    if (calculate_as_wet_weight) {
      coefficient =
          String.valueOf(
              getConversionToWetCoefficient(
                  crop_name, coefficients_to_get_wetweight_from_dryweight));
    } else {
      coefficient = "1.0";
    }

    BashScripts.multiplyRasterByCoefficient(
        script_folder,
        raster_name_all_years_this_month_dry, // input raster
        coefficient, // input value
        raster_name_all_years_this_month_wet_or_dry // output raster
        );
  }

  public static void averageAcrossYears(
      String script_folder,
      String[] years,
      String minimum_value_to_average,
      String[] raster_name_all_years_this_month_wet_or_dry, // the input rasters
      String raster_name_this_month, // the output raster
      String results_folder)
      throws InterruptedException, IOException {

    // average the yields across the years being simulated ("5" => 2005,"6" => 2006, etc)  from
    // raster_name_all_years_this_month_wet_or_dry
    // and save to raster_name_this_month

    String raster_names_to_average = "";

    String year;
    // loop over the years to average crop yields
    for (int year_index = 0; year_index < years.length; year_index++) {

      String raster_this_year = raster_name_all_years_this_month_wet_or_dry[year_index];

      year = years[year_index];

      // add a comma after the raster names
      if (!raster_names_to_average.equals("")) {
        raster_names_to_average = raster_names_to_average + ",";
      }

      raster_names_to_average = raster_names_to_average + raster_this_year;
    }

    // average all the cultivars for the crop to estimate the crop's overall yield
    BashScripts.averageRasters(
        script_folder,
        raster_names_to_average,
        raster_name_this_month,
        minimum_value_to_average,
        "average");
  } // end averageYieldsAcrossYears

  public static void findBestYieldsAndBestMaturity(
      String script_folder,
      String[] planting_months,
      String[] raster_names_to_combine, // the input rasters
      String best_planting_month_yield_name, // the output yield raster
      String best_planting_month_name, // the output best planting month raster
      String[] output_maturity_mean, // the input averaged days to maturity
      String best_planting_month_maturity_name, // the output best planting month raster's days to
      // maturity
      String results_folder)
      throws InterruptedException, IOException {

    // raster name at first planting month
    String combined_yields_results = raster_names_to_combine[0];
    String combined_maturity_means = output_maturity_mean[0];
    for (int planting_month_index = 1;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // create a suitable list of rasters to select a maximum from
      combined_yields_results =
          combined_yields_results + "," + raster_names_to_combine[planting_month_index];
      // create a list of days to maturity from which to select from to create a raster with days to
      // maturity where planting months are chosen
      combined_maturity_means =
          combined_maturity_means + "," + output_maturity_mean[planting_month_index];
    }
    BashScripts.compositeRaster(
        script_folder,
        combined_yields_results, // input rasters to find max among
        best_planting_month_yield_name, // output max yield raster name
        best_planting_month_name, // output a grass gis raster of this name for which month max was
        // chosen from
        results_folder);

    BashScripts.useKeyRasterToMapToValueRaster(
        script_folder,
        best_planting_month_name, // input key raster to choose which maturity raster to select
        combined_maturity_means, // input raster to get the values to choose from
        best_planting_month_maturity_name); // output raster with days to maturity of best planting
    // month in each cell png
    BashScripts.createPNG(script_folder, new String[] {best_planting_month_name}, results_folder);
    // maturity days png
    BashScripts.createPNG(
        script_folder, new String[] {best_planting_month_maturity_name}, results_folder);
  }

  // Determine the best yield and best planting month for each grid cell, given a raster for each
  // planting month raster_names_to_combine, and assign this to a single "BestYield" and a single
  // "BestMonth" raster. BestMonth stores the index of the month with the best yield, while
  // BestYield stores a raster of the actual yield per hectare when planting in the BestMonth.)
  public static void findBestYields(
      String script_folder,
      String[] planting_months,
      String[] raster_names_to_combine, // the input rasters
      String best_planting_month_yield_name, // the output yield raster
      String best_planting_month_name, // the output best planting month raster
      String results_folder)
      throws InterruptedException, IOException {
    // combines rasters by choosing the best of each pixel in a list of rasters and
    // creating the "best_planting_month_yield_name" raster as an output

    // raster name at first planting month
    String combined_yields_results = raster_names_to_combine[0];

    for (int planting_month_index = 1;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // create a suitable list of rasters to compare
      combined_yields_results =
          combined_yields_results + "," + raster_names_to_combine[planting_month_index];
    }

    BashScripts.compositeRaster(
        script_folder,
        combined_yields_results, // input rasters to find max among
        best_planting_month_yield_name, // output max yield raster name
        best_planting_month_name, // output a grass gis raster of this name for which month max was
        // chosen from
        results_folder);

    // BashScripts.createPNG(script_folder, best_planting_month_yield_name, results_folder);

  } // findBestYields
} // end ScenariosProcessor class
