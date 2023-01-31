// This file loads the scenarios from a CSV and runs the scenarios
// Running the scenarios calculates the yields in all the cells
// this class runs another class which also calculates the total
// production by averaging values in the cells

// The Scenarios class is responsible for running a set of scenarios specified in a CSV file. The
// scenarios are run by calling the processScenarios method, which takes as input the path to a
// script folder and an instance of the Scenarios class. The averageCrops method is also called,
// which averages the yield results for each crop.

// The Scenarios class has several member variables that store information about each scenario, such
// as the scenario name, the CO2 level, the crop name, the weather prefix, and the weather folder.
// It also has several composite variables, such as the scenario_tag, scenario_tag_for_averaging,
// scenario_tag_for_production, and scenario_tag_with_snx, which store derived information about
// each scenario.

// "scenarios" are specific to irrigated (IR) or rainfed (RF), but not to the specific month that is
// chosen for the planting year. Each scenario has several planting months tried, and the best one
// is always chosen
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class Scenarios {

  public int n_scenarios;
  public String[] crop_name;

  private String pathToCSV = null;
  public String[] years;
  public String[] planting_months;
  public String[] snx_name;
  public String[] co2_level;
  public String[] weather_prefix;
  public String[] weather_folder;
  public String[] results_folder;
  public String[] run_descriptor;
  public String[] region_to_use_n;
  public String[] region_to_use_s;
  public String[] region_to_use_e;
  public String[] region_to_use_w;
  public String[] nsres;
  public String[] ewres;
  Set<String> uniqueCrops;
  Set<String> unique_scenario_tags_for_production;
  public String[] scenario_tag;
  public String[] scenario_tag_for_averaging_rf_or_ir;
  public String[] scenario_tag_for_production_rf_or_ir;
  public String[] scenario_tag_for_production;
  public String[] scenario_tag_for_overall_yield;
  public String[] scenario_tag_with_snx;
  public String[] best_planting_raster_name;
  public String[][][] raster_names_all_years; // cultivar, month, year
  public String[][] raster_names; // cultivar, month
  public String[] crop_area_raster;
  public String[] yield_result_name;
  public String[] month_result_name;
  public String[] nitrogen;
  public String[][] yield_result_names; // cultivar, month
  public String[] fertilizer_scheme;

  // The main method is responsible for creating an instance of the Scenarios class by reading in a
  // CSV file and then running the scenarios and averaging the results.
  public static void main(String[] args)
      throws InterruptedException, FileNotFoundException, IOException {

    System.out.println("Using simulation csv: ");
    System.out.println(args[0]);
    System.out.println("");

    System.out.println("script folder: ");
    System.out.println(args[1]);
    System.out.println("");
    String simulation_csv_location = args[0];
    String script_folder = args[1];

    System.out.println("import scenarios");
    String[] initFileContents = importScenariosCSV(simulation_csv_location);

    System.out.println("new scenario");
    Scenarios scenarios = new Scenarios(initFileContents);

    // run a full set of scenarios
    processScenarios(script_folder, scenarios);

    // calculate aggregate production for crops
    CalculateProduction calculator = new CalculateProduction(script_folder, scenarios);
  }

  /// initialize the scenario variables
  public Scenarios(String[] initFileContents) throws InterruptedException, IOException {

    n_scenarios = initFileContents.length - 1;

    this.snx_name = new String[n_scenarios];
    this.co2_level = new String[n_scenarios];
    this.crop_name = new String[n_scenarios];
    this.weather_prefix = new String[n_scenarios];
    this.weather_folder = new String[n_scenarios];
    this.results_folder = new String[n_scenarios];
    this.run_descriptor = new String[n_scenarios];
    this.nitrogen = new String[n_scenarios];
    this.region_to_use_n = new String[n_scenarios];
    this.region_to_use_s = new String[n_scenarios];
    this.region_to_use_e = new String[n_scenarios];
    this.region_to_use_w = new String[n_scenarios];
    this.nsres = new String[n_scenarios];
    this.ewres = new String[n_scenarios];
    this.crop_area_raster = new String[n_scenarios];
    this.fertilizer_scheme = new String[n_scenarios];

    // consider may, april, march, june
    this.planting_months = new String[] {"1", "3", "5" ,"7", "9", "11"};

    //NOTE: only year 8 and 9 are considered later on in the code
    //      if this is a nuclear winter
    this.years = new String[] {"1","2","3","4","5","6","7"};

    // composites
    this.scenario_tag = new String[n_scenarios];
    this.scenario_tag_for_averaging_rf_or_ir = new String[n_scenarios];
    this.scenario_tag_for_production_rf_or_ir = new String[n_scenarios];
    this.scenario_tag_for_production = new String[n_scenarios];
    this.scenario_tag_for_overall_yield = new String[n_scenarios];
    this.scenario_tag_with_snx = new String[n_scenarios];
    this.best_planting_raster_name = new String[n_scenarios]; // best planting raster name
    this.raster_names = new String[n_scenarios][planting_months.length];
    this.raster_names_all_years = new String[n_scenarios][planting_months.length][years.length];
    this.month_result_name = new String[n_scenarios];
    this.yield_result_name = new String[n_scenarios];
    this.yield_result_names = new String[n_scenarios][planting_months.length];

    // loop through the scenario input csv file and create string arrays with the
    // appropriate value for each scenario index
    for (int i = 0; i < initFileContents.length - 1; i++) {

      // if a blank line occurs in the file, do not continue loading scenarios
      if (initFileContents[i] == null) {
        break;
      }

      String scenario = initFileContents[i + 1];
      String[] line_arguments = scenario.split(",");

      // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY
      this.snx_name[i] = line_arguments[0];

      this.co2_level[i] = line_arguments[1];

      // SPECIFIC TO THE CROP NAME
      this.crop_name[i] = line_arguments[2];

      // SPECIFIC TO WHETHER CONTROL/CATASTROPHE/GREENHOUSE
      // (ALWAYS THE SAME FOR ALL ROWS OF CSV)
      this.weather_prefix[i] = line_arguments[3];
      this.weather_folder[i] = line_arguments[4];
      this.results_folder[i] = line_arguments[5];
      this.run_descriptor[i] = line_arguments[6];
      this.nitrogen[i] = line_arguments[7];
      this.region_to_use_n[i] = line_arguments[8];
      this.region_to_use_s[i] = line_arguments[9];
      this.region_to_use_e[i] = line_arguments[10];
      this.region_to_use_w[i] = line_arguments[11];
      this.nsres[i] = line_arguments[12];
      this.ewres[i] = line_arguments[13];

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      this.crop_area_raster[i] = line_arguments[14];
      this.fertilizer_scheme[i] = line_arguments[15];

      // some useful composite strings

      // last 2 characters of snx name is rf_or_ir (indicates whether rainfed or irrigated)
      String rf_or_ir = snx_name[i].substring(snx_name[i].length() - 2);
      assert rf_or_ir.equals("RF") || rf_or_ir.equals("IR");

      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the yield result after running the model and finding the best
      // pixel in all month's rasters
      // "Best" replaces the month number
      this.yield_result_name[i] =
          "BestYield_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the months with the best pixel in all month's rasters
      this.month_result_name[i] =
          "BestMonth_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

      // SPECIFIC TO THE CROP NAME
      this.scenario_tag[i] =
          this.co2_level[i] + "_" + this.weather_prefix[i] + "_" + this.yield_result_name[i];

      // if tag_month_month_identifier is empty, then this is the best_combined months
      // SPECIFIC TO THE CROP NAME
      // the raster name that yield times the crop area is stored for a scenario
      this.scenario_tag_for_production[i] = this.scenario_tag[i] + "_production";

      // if tag_month_month_identifier is empty, then this is the best_combined months
      // SPECIFIC TO THE CROP NAME
      // the raster name of the average irrigated and rainfed yield weighted by crop area
      this.scenario_tag_for_overall_yield[i] = this.scenario_tag[i] + "_overall_yield";

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated yield is stored
      this.scenario_tag_for_averaging_rf_or_ir[i] = this.scenario_tag[i] + "_averaged_" + rf_or_ir;

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated production is stored
      this.scenario_tag_for_production_rf_or_ir[i] =
          this.scenario_tag[i] + "_production_" + rf_or_ir;

      // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY
      this.scenario_tag_with_snx[i] = this.snx_name[i] + "_" + this.scenario_tag[i];

      // best_planting_raster_name is the name of the raster that is shown as a png image
      this.best_planting_raster_name[i] = this.scenario_tag_with_snx[i] + "_real";
      // this.best_planting_raster_name[i] = this.scenario_tag_with_snx[i] + "_real_7@morgan_DSSAT_cat_0";

      for (int planting_month_index = 0;
          planting_month_index < this.planting_months.length;
          planting_month_index++) {
        // loop through the planting_months and create a raster name for each month

        String month = this.planting_months[planting_month_index];

        // SPECIFIC TO THE CROP NAME AND MONTH
        this.yield_result_names[i][planting_month_index] =
            ""
                + month
                + "_noGCMcalendar_p0_"
                + this.crop_name[i]
                + "__"
                + this.run_descriptor[i];

        // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH
        // this.raster_names[i][planting_month_index] = this.snx_name[i] + "_" + this.co2_level[i] +
        // "_" + this.weather_prefix[i] +"_"+
        // this.yield_result_name[i]+"_real_7@morgan_DSSAT_cat_0";
        this.raster_names[i][planting_month_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_result_names[i][planting_month_index]
                + "_real";

        for (int year_index = 0; year_index < this.years.length; year_index++) {

          // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH AND YEAR
          this.raster_names_all_years[i][planting_month_index][year_index] =
              this.snx_name[i]
                  + "_"
                  + this.co2_level[i]
                  + "_"
                  + this.weather_prefix[i]
                  + "_"
                  + this.yield_result_names[i][planting_month_index]
                  + "_real_"
                  + this.years[year_index];
        }
      }
    }

    // set the unique crop names to the uniqueCrops variable
    this.uniqueCrops = new TreeSet<String>();
    this.uniqueCrops.addAll(Arrays.asList(this.crop_name));

    // set the unique scenario tags to the uniqueCrops variable
    this.unique_scenario_tags_for_production = new TreeSet<String>();
    this.unique_scenario_tags_for_production.addAll(
        Arrays.asList(this.scenario_tag_for_production));

    // assert this.uniqueCrops.size() == this.unique_scenario_tags_for_production.size();

  }

  public static String[] importScenariosCSV(String csv_location)
      throws InterruptedException, IOException {
    String[] initFileContents = FunTricks.readTextFileToArray(csv_location);

    // split the string to get the column titles
    String[] column_titles = initFileContents[0].split(",");

    // make sure the column titles for the CSV match what java expects
    try {
      assert column_titles[0].equals("snx_name");
      assert column_titles[1].equals("co2_level");
      assert column_titles[2].equals("crop_name");
      assert column_titles[3].equals("weather_prefix");
      assert column_titles[4].equals("weather_folder");
      assert column_titles[5].equals("results_folder");
      assert column_titles[6].equals("run_descriptor");
      assert column_titles[7].equals("nitrogen");
      assert column_titles[8].equals("region_to_use_n");
      assert column_titles[9].equals("region_to_use_s");
      assert column_titles[10].equals("region_to_use_e");
      assert column_titles[11].equals("region_to_use_w");
      assert column_titles[12].equals("nsres");
      assert column_titles[13].equals("ewres");
      assert column_titles[14].equals("crop_area_raster");
      assert column_titles[15].equals("fertilizer_scheme");
    } catch (AssertionError e) {
      System.out.println(
          "Error: The first line of the csv file must contain the column titles in the code block"
              + " (specified above this error message)");
      System.exit(0); // logging or any action
    }
    System.out.println("");
    System.out.println("");

    return initFileContents;
  }

  public static void processScenarios(String script_folder, Scenarios scenarios)
      throws InterruptedException, IOException {

    // loop through and run each scenario
    for (int i = 0; i < scenarios.n_scenarios; i++) {
      processScenario(
          script_folder,
          i,
          scenarios.planting_months,
          scenarios.years,
          scenarios.snx_name[i],
          scenarios.co2_level[i],
          scenarios.crop_name[i],
          scenarios.weather_prefix[i],
          scenarios.weather_folder[i],
          scenarios.results_folder[i],
          scenarios.run_descriptor[i],
          scenarios.region_to_use_n[i],
          scenarios.region_to_use_s[i],
          scenarios.region_to_use_e[i],
          scenarios.region_to_use_w[i],
          scenarios.nsres[i],
          scenarios.ewres[i],
          scenarios.month_result_name[i],
          scenarios.yield_result_name[i],
          scenarios.yield_result_names[i],
          scenarios.best_planting_raster_name[i],
          scenarios.raster_names[i],
          scenarios.raster_names_all_years[i],
          scenarios.crop_area_raster[i],
          scenarios.nitrogen[i],
          scenarios.fertilizer_scheme[i]
        );
      System.out.println("====================");
      System.out.println("====================");
      System.out.println("=====PROGRESS=======");
      System.out.println("====================");
      System.out.println("====================");
      System.out.println("completed "+(i+1)+" out of "+scenarios.n_scenarios+" scenarios");
      System.out.println("====================");
    }
  }

  public static void processScenario(
      String script_folder,
      int scenario_index,
      String[] planting_months,
      String[] years,
      String snx_name,
      String co2_level,
      String crop_name,
      String weather_prefix,
      String weather_folder,
      String results_folder,
      String run_descriptor,
      String region_to_use_n,
      String region_to_use_s,
      String region_to_use_e,
      String region_to_use_w,
      String nsres,
      String ewres,
      String month_result_name,
      String yield_result_name,
      String[] yield_result_names,
      String best_planting_raster_name,
      String[] raster_names,
      String[][] raster_names_all_years,
      String crop_area_raster,
      String nitrogen,
      String fertilizer_scheme)
      throws InterruptedException, IOException {

    // run through all the required scripts for determining and saving yields

    if (scenario_index == 0) {
      // we only need to initialize the GRASS GIS region to use in the first round (all
      // other regions in the CSV rows from "scenarios.csv" should be identical)
      BashScripts.initGRASS(
          script_folder,
          region_to_use_n,
          region_to_use_s,
          region_to_use_e,
          region_to_use_w,
          nsres,
          ewres,
          planting_months,
          crop_area_raster,
          crop_name,
          run_descriptor,
          nitrogen);
    }

    for (int planting_month_index = 0;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // loop through the planting_months, assign the rasters to the yield result

      String yield_result_name_this_month = yield_result_names[planting_month_index];

      String raster_name_this_month = raster_names[planting_month_index];

      BashScripts.runScenario(
          script_folder,
          snx_name,
          co2_level,
          crop_name,
          weather_prefix,
          weather_folder,
          yield_result_name_this_month,
          fertilizer_scheme);

      System.out.println("");
      System.out.println("");
      System.out.println("---------------");
      System.out.println("completed "+(planting_month_index+1)+" out of "+(planting_months.length)+" planting months");
      System.out.println("---------------");
      System.out.println("");
      System.out.println("");

      String[] raster_name_all_years_this_month = raster_names_all_years[planting_month_index];

      BashScripts.processResults(script_folder, snx_name, yield_result_name_this_month);

      // // create a png for the raster for each year
      // for(int year_index = 0; year_index < years.length; year_index++) {
      //   BashScripts.createPNG(script_folder, raster_name_all_years_this_month[year_index],results_folder);
      // }

      averageYieldsAcrossYears(
          script_folder,
          years,
          raster_name_all_years_this_month,
          raster_name_this_month,
          results_folder);

      BashScripts.createPNG(script_folder, raster_name_this_month, results_folder);
    }

    // find best yields for each scenario
    findBestYields(
        script_folder,
        planting_months,
        raster_names,
        best_planting_raster_name,
        month_result_name,
        results_folder);
  } // processScenario

  public static void averageYieldsAcrossYears(
      String script_folder,
      String[] years,
      String[] raster_name_all_years_this_month,
      String raster_name_this_month,
      String results_folder)
      throws InterruptedException, IOException {

    // average the yields across the years being simulated ("5" => 2005,"6" => 2006, etc)  from
    // raster_name_all_years_this_month
    // and save to raster_name_this_month

    String raster_names_to_average = "";

    String year;
    // loop over the years to average crop yields
    for (int year_index = 0; year_index < years.length; year_index++) {

      String raster_this_year = raster_name_all_years_this_month[year_index];

      // if weather_folder contains "catastrophe" string, skip years other than 6 and 7 for averaging
      // The nuclear event starts mid way through year 5. Years 2-3 from the scenario are the coldest. So the worst year would be 5+2 and 5+3 (years 7 and 8). The first two years are used for calibrating soil temperature though, so the year numbers from the DSSAT runs are actually offset by 2. Therefore, years 5 and 6 are the worst years.
      year = years[year_index];
      if (raster_this_year.contains("catastrophe")) {
        if(!(year.equals("5") || year.equals("6"))) {
          continue;
        }
      }

      // add a comma after the raster names
      if (!raster_names_to_average.equals("")) {
        raster_names_to_average = raster_names_to_average + ",";
      }

      raster_names_to_average =
          raster_names_to_average + raster_this_year;
    }

    // in scenarios
    System.out.println("in scenario ");



    // average all the cultivars for the crop to estimate the crop's overall yield
    BashScripts.runAverageCropsCommand(
        script_folder, raster_names_to_average, raster_name_this_month, results_folder);
  } // end averageYieldsAcrossYears

  // (assigns the best scenario cell of all planting planting_months to the rasters with "Best" in
  // it)
  public static void findBestYields(
      String script_folder,
      String[] planting_months,
      String[] raster_names,
      String best_planting_raster_name,
      String month_result_name,
      String results_folder)
      throws InterruptedException, IOException {
    String rasters_to_get_composite = "";

    String combined_yields_results = raster_names[0];

    for (int planting_month_index = 1;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // create a suitable list of rasters to compare
      combined_yields_results = combined_yields_results + "," + raster_names[planting_month_index];
    }

    String best_yield_raster = best_planting_raster_name;
    String best_month_raster = month_result_name;

    BashScripts.compositeRaster(
        script_folder,
        combined_yields_results,
        best_yield_raster,
        best_month_raster,
        results_folder);

    BashScripts.createPNG(script_folder, best_month_raster, results_folder);
    
  } // findBestYields
} // class Scenarios
