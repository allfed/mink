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
  public String all_or_crop_specific;
  public String minimum_physical_area;
  public String[] crop_name;
  public Config config;

  private String pathToCSV = null;
  public String[] years;
  public String[] planting_months;
  public boolean run_crop_model;
  public boolean process_results;
  public boolean calculate_as_wet_weight;
  public boolean average_yields;
  public boolean calculate_each_year_best_month;
  public boolean calculate_rf_or_ir_specific_average_yield;
  public boolean calculate_rf_or_ir_specific_production;
  public boolean calculate_rf_plus_ir_production;
  public boolean calculate_average_yield_rf_and_ir;
  public boolean make_rasters_comparing_overall_to_historical;
  // public boolean make_plots_overall_vs_historical;
  // public boolean generate_pptx_report;
  public boolean find_best_yields;
  public boolean create_each_year_png;
  public boolean create_average_png;
  public boolean create_overall_png;
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
  public String[][][] raster_names_all_years_dry; // cultivar, month, year
  public String[][][] raster_names_all_years_wet_or_dry; // cultivar, month, year
  public String[][] raster_names_best_all_years; // cultivar, year
  public String[][] raster_names_average_year; // cultivar, month
  public String[][] output_stats_filenames; // cultivar, month
  public String[] mask_for_this_snx;
  public String[] yield_result_name;
  public String[] month_result_name;
  public String[] nitrogen;
  public String[][] yield_result_names; // cultivar, month
  public String[] fertilizer_scheme;
  public String[] rf_or_ir;

  static HashMap<String, Float> coefficients_to_get_wetweight_from_dryweight;

  // The main method is responsible for creating an instance of the Scenarios class by reading in a
  // CSV file and then running the scenarios and averaging the results.
  public static void main(String[] args)
      throws InterruptedException, FileNotFoundException, IOException {

    // System.out.println("Using simulation csv: ");
    // System.out.println(args[0]);
    // System.out.println("");

    // System.out.println("script folder: ");
    // System.out.println(args[1]);
    // System.out.println("");

    String simulation_csv_location = args[0];
    String script_folder = args[1];
    String run_parameters_csv_folder = args[2];
    String config_file = args[3];
    String DSSAT_process_or_both = args[4];

    String moisture_csv_location = run_parameters_csv_folder + "moisture_contents.csv";

    String[] initFileContents = importScenariosCSV(simulation_csv_location);
    // System.out.println("import scenarios");
    int n_scenarios = initFileContents.length - 1;
    System.out.println("n_scenarios");
    System.out.println(n_scenarios);

    // System.out.println("new scenario");
    Scenarios scenarios = new Scenarios(initFileContents, config_file);

    BashScripts.setGRASSRegion(script_folder, scenarios.config);

    coefficients_to_get_wetweight_from_dryweight =
        createWetWeightConversionMap(moisture_csv_location);

    if (DSSAT_process_or_both.equals("DSSAT")) {
      // run a full set of scenarios, without processing anything
      scenarios.run_crop_model = true;
      scenarios.process_results = false;

      // Overwrite inputs from the config file (as we haven't processed yet, we can't do these
      // things)
      scenarios.average_yields = false;
      scenarios.find_best_yields = false;
      scenarios.calculate_each_year_best_month = false;
      scenarios.find_best_yields = false;
      scenarios.calculate_rf_or_ir_specific_average_yield = false;
      scenarios.calculate_rf_or_ir_specific_production = false;
      scenarios.calculate_rf_plus_ir_production = false;
      scenarios.calculate_average_yield_rf_and_ir = false;
      scenarios.make_rasters_comparing_overall_to_historical = false;
      scenarios.create_each_year_png = false;
      scenarios.create_average_png = false;
      scenarios.create_overall_png = false;

      processScenarios(script_folder, scenarios);
    } else if (DSSAT_process_or_both.equals("process")) {
      // process result of runs, using config file settings for what to calculate
      scenarios.run_crop_model = false;
      scenarios.process_results = true;

      processScenarios(script_folder, scenarios);

    } else if (DSSAT_process_or_both.equals("both")) {

      scenarios.run_crop_model = true;
      scenarios.process_results = true;

      processScenarios(script_folder, scenarios);

    } else if (DSSAT_process_or_both.equals("continueDSSAT")) {
      // continue a previous run
      int[] loadedValues = loadProgressFromFile(script_folder, scenarios.run_descriptor[0]);

      int start_planting_month_index = loadedValues[0];
      int scenario_index = loadedValues[1];

      System.out.println("Loaded start_planting_month_index: " + start_planting_month_index);
      System.out.println("Loaded scenario_index: " + scenario_index);

      // run a full set of scenarios, without processing anything
      scenarios.run_crop_model = true;
      scenarios.process_results = false;

      // Overwrite inputs from the config file (as we haven't processed yet, we can't do these
      // things)
      scenarios.average_yields = false;
      scenarios.find_best_yields = false;
      scenarios.calculate_each_year_best_month = false;
      scenarios.find_best_yields = false;
      scenarios.calculate_rf_or_ir_specific_average_yield = false;
      scenarios.calculate_rf_or_ir_specific_production = false;
      scenarios.calculate_rf_plus_ir_production = false;
      scenarios.calculate_average_yield_rf_and_ir = false;
      scenarios.make_rasters_comparing_overall_to_historical = false;
      scenarios.create_each_year_png = false;
      scenarios.create_average_png = false;
      scenarios.create_overall_png = false;

      processScenarios(
          script_folder,
          scenarios,
          scenario_index,
          start_planting_month_index);

    } else {
      System.out.println(
          "ERROR:DSSAT_process_or_both must be one of 'DSSAT', 'process', or 'both'");
      System.exit(1);
    }

    // run a full set of scenarios, but process everything this time and don't run the model

    // calculate aggregate production for crops
    // this involves calculating overall production, then calculating average yields
    CalculateProduction calculator = new CalculateProduction(script_folder, scenarios);

    // PlotCreator plotter = new PlotCreator();
    // plotter.plot();
    // generateReport();
  }

  // // make_plots_overall_vs_historical
  // public static void makeOverallVsHistoricalScatterplots() {
  //   AsciiImporter ascii = new AsciiImporter();
  //   PlotCreator plotter = new PlotCreator();

  // }

  // // // generate_pptx_report

  // // // generate a report with the results of the scenarios
  // // public static void generatePptxReport() {

  // }

  /// initialize the scenario variables
  public Scenarios(String[] initFileContents, String config_file)
      throws InterruptedException, IOException {

    // System.out.println("config_file");
    // System.out.println(config_file);

    // System.out.println("n_scenarios");
    // System.out.println(n_scenarios);
    Config config = Config.importConfigCSV(config_file);
    this.config = config;

    int n_scenarios = initFileContents.length - 1;
    this.n_scenarios = n_scenarios;
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
    this.mask_for_this_snx = new String[n_scenarios];
    this.fertilizer_scheme = new String[n_scenarios];

    this.run_crop_model = new Boolean(config.model_configuration.run_crop_model);
    this.process_results = new Boolean(config.model_configuration.process_results);

    this.calculate_as_wet_weight = new Boolean(config.model_configuration.calculate_as_wet_weight);
    this.average_yields = new Boolean(config.model_configuration.average_yields);
    this.calculate_each_year_best_month =
        new Boolean(config.model_configuration.calculate_each_year_best_month);
    this.find_best_yields = new Boolean(config.model_configuration.find_best_yields);
    this.calculate_rf_or_ir_specific_average_yield =
        new Boolean(config.model_configuration.calculate_rf_or_ir_specific_average_yield);
    this.calculate_rf_or_ir_specific_production =
        new Boolean(config.model_configuration.calculate_rf_or_ir_specific_production);
    this.calculate_rf_plus_ir_production =
        new Boolean(config.model_configuration.calculate_rf_plus_ir_production);
    this.calculate_average_yield_rf_and_ir =
        new Boolean(config.model_configuration.calculate_average_yield_rf_and_ir);
    this.make_rasters_comparing_overall_to_historical =
        new Boolean(config.model_configuration.make_rasters_comparing_overall_to_historical);
    this.create_each_year_png = new Boolean(config.model_configuration.create_each_year_png);
    this.create_average_png = new Boolean(config.model_configuration.create_average_png);
    this.create_overall_png = new Boolean(config.model_configuration.create_overall_png);

    this.all_or_crop_specific = config.physical_parameters.all_or_crop_specific;

    this.minimum_physical_area = config.physical_parameters.minimum_physical_area;

    String real_or_happy = config.physical_parameters.real_or_happy;

    //   // consider may, april, march, june
    //   // this.planting_months = new String[] {"1", "3", "5", "7", "9", "11"};
    // this.planting_months = config.physical_parameters.planting_months;
    this.planting_months = config.physical_parameters.planting_months.toArray(new String[0]);
    this.years = config.physical_parameters.years.toArray(new String[0]);
    //   // NOTE: only year 8 and 9 (5 and 6 here, as there's a 3 year offset) are
    //   // considered later on in the code if this is a nuclear winter.
    // this.years = config.physical_parameters.years;

    //   // NOTE: happy means unstressed by water or nitrogen
    //   //       real means full stress is estimated
    //   // this.run_crop_model = true;
    //   // this.process_results = false;
    //   // this.calculate_as_wet_weight = true;
    //   // this.average_yields = false;
    //   // this.calculate_each_year_best_month = true;
    //   // this.find_best_yields = false;
    //   // this.calculate_rf_or_ir_specific_average_yield = false;
    //   // this.calculate_rf_or_ir_specific_production = false;
    //   // this.calculate_rf_plus_ir_production = false;
    //   // this.calculate_average_yield_rf_and_ir = false;
    //   // this.make_rasters_comparing_overall_to_historical = false;
    //   // this.create_each_year_png = false;
    //   // this.create_average_png = false;
    //   // this.create_overall_png = false;

    // set to "wet" if calculating yields wet weight, "dry" if calculating dry weight
    String wet_or_dry;
    if (config.model_configuration.calculate_as_wet_weight) {
      wet_or_dry = "wet";
    } else {
      wet_or_dry = "dry";
    }

    // composites
    this.scenario_tag = new String[n_scenarios];
    this.rf_or_ir = new String[n_scenarios];
    this.scenario_tag_for_averaging_rf_or_ir = new String[n_scenarios];
    this.scenario_tag_for_production_rf_or_ir = new String[n_scenarios];
    this.scenario_tag_for_production = new String[n_scenarios];
    this.scenario_tag_for_overall_yield = new String[n_scenarios];
    this.scenario_tag_with_snx = new String[n_scenarios];
    this.best_planting_raster_name = new String[n_scenarios]; // best planting raster name
    this.raster_names_best_all_years = new String[n_scenarios][years.length];
    this.raster_names_average_year = new String[n_scenarios][planting_months.length];
    this.raster_names_all_years_dry = new String[n_scenarios][planting_months.length][years.length];
    this.raster_names_all_years_wet_or_dry =
        new String[n_scenarios][planting_months.length][years.length];
    this.output_stats_filenames = new String[n_scenarios][planting_months.length];
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
      this.mask_for_this_snx[i] = line_arguments[14];
      this.fertilizer_scheme[i] = line_arguments[15];

      // some useful composite strings

      // last 2 characters of snx name is rf_or_ir (indicates whether rainfed or irrigated)
      String rf_or_ir = snx_name[i].substring(snx_name[i].length() - 2);
      assert rf_or_ir.equals("RF") || rf_or_ir.equals("IR");
      this.rf_or_ir[i] = rf_or_ir;

      // CAN'T BE CHANGED:
      // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the yield result after running the model and finding the best
      // pixel in all month's rasters
      // "Best" replaces the month number
      // used in "processResults" in BashScripts, used as second argument to
      // READ_DSSAT_outputs_from_cols_FEW
      // this is then used in conjunction with output_stats_filenames[planting_month]
      // to save as a raster from the READ_DSSAT_outputs_from_cols_FEW script
      this.yield_result_name[i] =
          "BestYield_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the months with the best pixel in all month's rasters
      this.month_result_name[i] =
          "BestMonth_noGCMcalendar_p0_"
              + this.snx_name[i]
              + "__"
              + this.run_descriptor[i]
              + "_"
              + wet_or_dry
              + "_"
              + rf_or_ir;

      // SPECIFIC TO THE CROP NAME
      this.scenario_tag[i] =
          this.co2_level[i]
              + "_"
              + this.weather_prefix[i]
              + "_"
              + this.yield_result_name[i]
              + "_"
              + wet_or_dry;

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
      this.best_planting_raster_name[i] = this.scenario_tag_with_snx[i] + "_" + real_or_happy;

      for (int planting_month_index = 0;
          planting_month_index < this.planting_months.length;
          planting_month_index++) {
        // loop through the planting_months and create a raster name for each month

        String month = this.planting_months[planting_month_index];

        // CAN'T BE CHANGED:
        // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
        // SPECIFIC TO THE CROP NAME AND MONTH
        this.yield_result_names[i][planting_month_index] =
            month + "_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

        // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH
        this.raster_names_average_year[i][planting_month_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_result_names[i][planting_month_index]
                + "_"
                + wet_or_dry
                + "_"
                + real_or_happy;

        // CAN'T BE CHANGED:
        // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
        this.output_stats_filenames[i][planting_month_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_result_names[i][planting_month_index]
                + "_STATS.txt";

        for (int year_index = 0; year_index < this.years.length; year_index++) {

          // CAN'T BE CHANGED:
          // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
          // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH AND YEAR
          this.raster_names_all_years_dry[i][planting_month_index][year_index] =
              this.snx_name[i]
                  + "_"
                  + this.co2_level[i]
                  + "_"
                  + this.weather_prefix[i]
                  + "_"
                  + this.yield_result_names[i][planting_month_index]
                  + "_"
                  + real_or_happy
                  + "_"
                  + this.years[year_index];

          // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH AND YEAR AND
          // WHETHER WET WEIGHT OR DRY WEIGHT
          this.raster_names_all_years_wet_or_dry[i][planting_month_index][year_index] =
              this.snx_name[i]
                  + "_"
                  + this.co2_level[i]
                  + "_"
                  + this.weather_prefix[i]
                  + "_"
                  + this.yield_result_names[i][planting_month_index]
                  + "_"
                  + real_or_happy
                  + "_"
                  + wet_or_dry
                  + "_"
                  + this.years[year_index];
        }
      }
      for (int year_index = 0; year_index < this.years.length; year_index++) {
        // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH
        this.raster_names_best_all_years[i][year_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_result_name[i]
                + "_"
                + wet_or_dry
                + "_"
                + real_or_happy
                + "_"
                + this.years[year_index];
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
      assert column_titles[14].equals("mask_for_this_snx");
      assert column_titles[15].equals("fertilizer_scheme");
    } catch (AssertionError e) {
      System.out.println(
          "Error: The first line of the csv file must contain the column titles in the code block"
              + " (specified above this error message)"
              + " (scenarios csv)");
      System.exit(0); // logging or any action
    }
    System.out.println("");
    System.out.println("");

    return initFileContents;
  }

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

    HashMap<String, Float> coefficients =
        new HashMap<String, Float>();

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

  public static Float getConversionToWetCoefficient(String cropName) throws InterruptedException, IOException {
    Float coefficient = coefficients_to_get_wetweight_from_dryweight.get(cropName);
    if (coefficient == null) {
      throw new IllegalArgumentException("ERROR: No wet weight conversion coefficient found for crop name: " + cropName + ". \n Available options to get conversion:"+coefficients_to_get_wetweight_from_dryweight.keySet().toString());
    }

    return coefficient;
  }

  // Overload the method below, with the default starting integers of first planting month, first
  // scenario
  public static void processScenarios(
      String script_folder,
      Scenarios scenarios) throws InterruptedException, IOException {
    processScenarios(script_folder, scenarios, 0, 0);
  }

  public static void processScenarios(
      String script_folder,
      Scenarios scenarios,
      int start_scenario_index,
      int start_planting_month_index) throws InterruptedException, IOException {
    // System.out.println(scenarios.month_result_name[1][0]);
    String crop_name = "";
    // loop through and run each scenario
    // System.out.println("scenarios.n_scenarios");
    // System.out.println(scenarios.n_scenarios);

    for (int i = start_scenario_index; i < scenarios.n_scenarios; i++) {

      crop_name = scenarios.crop_name[i];
      processScenario(
          script_folder,
          i,
          start_planting_month_index,
          scenarios.create_each_year_png,
          scenarios.run_crop_model,
          scenarios.process_results,
          scenarios.calculate_as_wet_weight,
          scenarios.average_yields,
          scenarios.calculate_each_year_best_month,
          scenarios.find_best_yields,
          scenarios.calculate_rf_or_ir_specific_average_yield,
          scenarios.calculate_rf_or_ir_specific_production,
          scenarios.calculate_rf_plus_ir_production,
          scenarios.calculate_average_yield_rf_and_ir,
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
          scenarios.yield_result_names[i],
          scenarios.best_planting_raster_name[i],
          scenarios.raster_names_average_year[i],
          scenarios.raster_names_best_all_years[i],
          scenarios.raster_names_all_years_dry[i],
          scenarios.raster_names_all_years_wet_or_dry[i],
          scenarios.output_stats_filenames[i],
          scenarios.mask_for_this_snx[i],
          scenarios.nitrogen[i],
          scenarios.minimum_physical_area,
          scenarios.fertilizer_scheme[i]);
      System.out.println("====================");
      System.out.println(
          "completed " + (i + 1) + " out of " + scenarios.n_scenarios + " scenarios");
      System.out.println("====================");
    }
  }

  public static void saveProgressToFile(
      String scripts_folder, String run_descriptor, int planting_month_index, int scenario_index)
      throws InterruptedException {
    String outputBaseName = scripts_folder + "/interrupted_run_locations/" + run_descriptor;
    String dataOutName = outputBaseName + "_pmonth_scenariolast6.txt";
    String magicDelimiter = ","; // You can choose any delimiter you like

    try (FileOutputStream dataOutStream = new FileOutputStream(dataOutName);
        PrintWriter dataOut = new PrintWriter(dataOutStream)) {
      System.out.println("Current working directory: " + System.getProperty("user.dir"));
      dataOut.print(planting_month_index + magicDelimiter + scenario_index);

    } catch (IOException e) {
      System.out.println("");
      System.out.println(
          "error! couldn't save progress to the planting month and scenario file for some reason."
              + " Check if the appropriate folder exists. Attempting to save to:");
      System.out.println(dataOutName);
      System.out.println("");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static int[] loadProgressFromFile(String scripts_folder, String run_descriptor) {
    String outputBaseName = scripts_folder + "interrupted_run_locations/" + run_descriptor;
    String dataOutName = outputBaseName + "_pmonth_scenariolast6.txt";
    String magicDelimiter = ","; // The same delimiter used in the save function
    int[] result = new int[2];

    try (FileInputStream dataInStream = new FileInputStream(dataOutName);
        Scanner scanner = new Scanner(dataInStream)) {
      String line = scanner.nextLine();
      String[] parts = line.split(magicDelimiter);
      result[0] = Integer.parseInt(parts[0]); // planting_month_index
      result[1] = Integer.parseInt(parts[1]); // scenario_index
    } catch (IOException e) {
      System.out.println("");
      System.out.println(
          "error! couldn't load progress from the planting month and scenario file for some reason."
              + " Check if the appropriate folder exists. Attempting to load from:");
      System.out.println(dataOutName);
      System.out.println("");
      e.printStackTrace();
      System.exit(1);
    }
    return result;
  }

  public static void processScenario(
      String script_folder,
      int scenario_index,
      int start_planting_month_index,
      boolean create_each_year_png,
      boolean run_crop_model,
      boolean process_results,
      boolean calculate_as_wet_weight,
      boolean average_yields,
      boolean calculate_each_year_best_month,
      boolean find_best_yields,
      boolean calculate_rf_or_ir_specific_average_yield,
      boolean calculate_rf_or_ir_specific_production,
      boolean calculate_rf_plus_ir_production,
      boolean calculate_average_yield_rf_and_ir,
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
      String[] yield_result_names,
      String best_planting_raster_name,
      String[] raster_names_average_year,
      String[] raster_names_best_all_years,
      String[][] raster_names_all_years_dry,
      String[][] raster_names_all_years_wet_or_dry,
      String[] output_stats_filenames,
      String mask_for_this_snx,
      String nitrogen,
      String minimum_physical_area,
      String fertilizer_scheme)
      throws InterruptedException, IOException {

    BashScripts.initGRASS(
        script_folder,
        region_to_use_n,
        region_to_use_s,
        region_to_use_e,
        region_to_use_w,
        nsres,
        ewres,
        planting_months,
        mask_for_this_snx,
        crop_name,
        run_descriptor,
        minimum_physical_area,
        nitrogen);

    for (int planting_month_index = start_planting_month_index;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // loop through the planting_months, and average the yield for all years at that planting month, saving the result to the raster name defined in the raster_names_average_year variable

      String yield_result_name_this_month = yield_result_names[planting_month_index];

      String raster_name_this_month = raster_names_average_year[planting_month_index];

      if (run_crop_model) {

        System.out.println("");
        System.out.println("====RUN DETAILS===");
        System.out.println(
            "run: "
                + run_descriptor
                + "\ncrop name: "
                + crop_name
                + "\nplanting month: "
                + planting_months[planting_month_index]
                + "\nsnx_name: "
                + snx_name + "(scenario index: " + scenario_index + ")");
        System.out.println("==================");
        System.out.println("");

        saveProgressToFile(script_folder, run_descriptor, planting_month_index, scenario_index);

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
        System.out.println(
            "completed "
                + (planting_month_index + 1)
                + " out of "
                + (planting_months.length)
                + " planting months");
        System.out.println("---------------");
        System.out.println("");
        System.out.println("");
      }

      String[] raster_name_all_years_this_month_dry =
          raster_names_all_years_dry[planting_month_index];
      String[] raster_name_all_years_this_month_wet_or_dry =
          raster_names_all_years_wet_or_dry[planting_month_index];

      // process the results if processresults flag is set
      if (process_results) {
        BashScripts.processResults(
            script_folder,
            output_stats_filenames[planting_month_index],
            yield_result_name_this_month);

        for (int year_index = 0; year_index < years.length; year_index++) {
          convertToWetWeightIfNeeded(
              calculate_as_wet_weight,
              script_folder,
              crop_name,
              raster_name_all_years_this_month_dry[year_index],
              raster_name_all_years_this_month_wet_or_dry[year_index]);
        }
      }

      // if we are averaging yields, average the yields across the years being simulated
      if (average_yields) {
        averageYieldsAcrossYears(
            script_folder,
            years,
            raster_name_all_years_this_month_wet_or_dry,
            raster_name_this_month,
            results_folder);
      }
    }

    // 1. [This step is optional and does not play a part in overall yield calculations, but can be helpful for later analysis] First, if calculate_each_year_best_month is true, we loop through the yield for all years, and find the best month for each grid cell, saving this to raster_names_best_all_years. This occurs for each year, not just the average of years, producing many more rasters than the next usage of findBestYields.

    // if we choose to find the best yields, find best yields for each scenario
    if (calculate_each_year_best_month) {
      calculateBestYieldAllYears(
          script_folder,
          years,
          planting_months,
          raster_names_all_years_wet_or_dry,
          raster_names_best_all_years,
          results_folder);
    }

    // 2. If find_best_yields is true, raster_names_average_year has been calculated as the average of all years at a given planting month. When we findBestYields,

    if (find_best_yields) {
      findBestYields(
          script_folder,
          planting_months,
          raster_names_average_year,
          best_planting_raster_name,
          month_result_name,
          results_folder);
    }

    // create a png for the raster for each year if create_each_year_png is true

    if (create_each_year_png) {
      createEachYearPNG(
          planting_months, years, script_folder, results_folder, raster_names_all_years_wet_or_dry);
    }
  } // processScenario

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
      String script_folder, // the output rasters
      String[] years,
      String[] planting_months,
      String[][] raster_names_all_years_wet_or_dry, // the input rasters
      String[] raster_names_best_all_years, // the output rasters
      String results_folder // the output rasters
      ) throws InterruptedException, IOException {
    // calculate the average of the raster yield over time for a given scenario
    for (int year_index = 0; year_index < years.length; year_index++) {
      String[] raster_names_all_months_this_year = new String[planting_months.length];

      String raster_name_best_this_year = raster_names_best_all_years[year_index];

      for (int planting_month_index = 0;
          planting_month_index < planting_months.length;
          planting_month_index++) {
        raster_names_all_months_this_year[planting_month_index] =
            raster_names_all_years_wet_or_dry[planting_month_index][year_index];
      }
      // System.out.println("raster_name_best_this_year");
      // System.out.println(raster_name_best_this_year);
      findBestYields(
          script_folder,
          planting_months,
          raster_names_all_months_this_year, // input rasters
          raster_name_best_this_year, // the output raster
          "planting_month_raster_result_unused",
          results_folder);
    }
  }

  public static void convertToWetWeightIfNeeded(
      boolean calculate_as_wet_weight,
      String script_folder,
      String crop_name,
      String raster_name_all_years_this_month_dry, // the input raster
      String raster_name_all_years_this_month_wet_or_dry) // the output raster
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
      coefficient = String.valueOf(getConversionToWetCoefficient(crop_name));
    } else {
      coefficient = "1.0";
    }
    BashScripts.multiplyRasterByCoefficient(
        script_folder,
        raster_name_all_years_this_month_dry,
        coefficient,
        raster_name_all_years_this_month_wet_or_dry);
  }

  public static void averageYieldsAcrossYears(
      String script_folder,
      String[] years,
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

      // if weather_folder contains "catastrophe" string, skip years other than 6 and 7 for
      // averaging
      // The nuclear event starts mid way through year 5. Years 2-3 from the scenario are the
      // coldest. So the worst year would be 5+2 and 5+3 (years 7 and 8). The first two years are
      // used for calibrating soil temperature though, so the year numbers from the DSSAT runs are
      // actually offset by 2. Therefore, years 5 and 6 are the worst years.
      // System.out.println("Averaging years!");
      year = years[year_index];
      if (raster_this_year.contains("catastrophe")) {
        if (!(year.equals("5") || year.equals("6"))) {
          continue;
        }
      }

      // add a comma after the raster names
      if (!raster_names_to_average.equals("")) {
        raster_names_to_average = raster_names_to_average + ",";
      }

      raster_names_to_average = raster_names_to_average + raster_this_year;
    }

    // average all the cultivars for the crop to estimate the crop's overall yield
    BashScripts.runAverageCropsCommand(
        script_folder, raster_names_to_average, raster_name_this_month, results_folder);
  } // end averageYieldsAcrossYears

  // Determine the best yield and best planting month for each grid cell, given a raster for each
  // planting month raster_names_to_combine, and assign this to a single "BestYield" and a single
  // "BestMonth" raster. BestMonth stores the index of the month with the best yield, while
  // BestYield stores a raster of the actual yield per hectare when planting in the BestMonth.)
  public static void findBestYields(
      String script_folder,
      String[] planting_months,
      String[] raster_names_to_combine, // the input rasters
      String best_planting_raster_name, // the output yield raster
      String month_result_name, // the output best planting month raster
      String results_folder)
      throws InterruptedException, IOException {
    // combines rasters by choosing the best of each pixel in a list of rasters and
    // creating the "best_planting_raster_name" raster as an output

    // raster name at first planting month
    String combined_yields_results = raster_names_to_combine[0];

    for (int planting_month_index = 1;
        planting_month_index < planting_months.length;
        planting_month_index++) {
      // create a suitable list of rasters to compare
      combined_yields_results =
          combined_yields_results + "," + raster_names_to_combine[planting_month_index];
    }

    String best_yield_raster = best_planting_raster_name;
    String best_month_raster = month_result_name;
    // System.out.println("running composite raster");
    BashScripts.compositeRaster(
        script_folder,
        combined_yields_results,
        best_yield_raster,
        best_month_raster,
        results_folder);

    // BashScripts.createPNG(script_folder, best_month_raster, results_folder);

  } // findBestYields
} // class Scenarios
