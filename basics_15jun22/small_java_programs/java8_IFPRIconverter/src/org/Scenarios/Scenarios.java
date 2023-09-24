// This file loads the scenarios from a CSV and runs the scenarios
// Running the scenarios calculates the yields in all the cells
// this class runs another class which also calculates the total
// production by averaging values in the cells

// The Scenarios class is responsible for running a set of scenarios specified in a CSV file. The
// scenarios are run by calling the runScenarios method, which takes as input the path to a
// script folder and an instance of the Scenarios class. The averageCrops method is also called,
// which averages the yield results for each crop.

// The Scenarios class has several member variables that store information about each scenario, such
// as the scenario name, the CO2 level, the crop name, the weather prefix, and the weather folder.
// It also has several composite variables, such as the scenario_tag, scenario_tag_for_averaging,
// combined_production_name, and scenario_tag_with_snx, which store derived information about
// each scenario.

// "scenarios" are specific to irrigated (IR) or rainfed (RF), but not to the specific month that is
// chosen for the planting year. Each scenario has several planting months tried, and the best one
// is always chosen
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class Scenarios {
  public static class PlantingScenario {
    int plantingMonth;
    int scenarioNumber;

    public PlantingScenario(int plantingMonth, int scenarioNumber) {
      this.plantingMonth = plantingMonth;
      this.scenarioNumber = scenarioNumber;
    }
  }

  public int n_scenarios;
  public String all_or_crop_specific;
  public String minimum_physical_area;
  public String minimum_yield;
  public String n_chunks;
  public String[] crop_name;
  public Config config;

  private String pathToCSV = null;
  public String[] years;
  public String[] planting_months;
  public String real_or_happy;
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
  public boolean find_best_yields;
  public boolean just_save_needed_rasters;
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
  // Set<String> unique_scenario_tags_for_production;
  public String[] scenario_tag;
  public String[] combined_yield_name_rf_or_ir;
  public String[] combined_production_name_rf_or_ir;
  public String[] combined_planting_month_name_rf_or_ir;
  public String[] combined_days_to_maturity_name_rf_or_ir;
  public String[] combined_production_name;
  public String[] combined_yield_name;
  public String[] scenario_tag_with_snx;
  public String[] best_planting_month_yield_name;
  public String[][][] raster_names_all_years_dry; // cultivar, month, year
  public String[][][] raster_names_all_years_wet_or_dry; // cultivar, month, year
  public String[][] best_planting_month_yield_name_all_years; // cultivar, year
  // public String[][] best_month_maturity_result_name_all_years; // cultivar, year
  public String[][] best_planting_month_name_all_years; // cultivar, year
  public String[][] raster_names_average_year; // cultivar, month
  public String[][] output_stats_basenames; // cultivar, month
  public String[][] output_maturity_mean; // cultivar, month
  public String[] mask_for_this_snx;
  public String[] yield_name;
  public String[] best_planting_month_name;
  public String[] best_planting_month_maturity_name;
  public String[] nitrogen;
  public String[][] yield_names; // cultivar, month
  public String[] fertilizer_scheme;
  public String[] rf_or_ir;

  static HashMap<String, Float> coefficients_to_get_wetweight_from_dryweight;

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
    this.minimum_yield = config.physical_parameters.minimum_yield;
    this.n_chunks = config.model_configuration.n_chunks;

    this.real_or_happy = config.physical_parameters.real_or_happy;

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
    this.combined_yield_name_rf_or_ir = new String[n_scenarios];
    this.combined_production_name_rf_or_ir = new String[n_scenarios];
    this.combined_days_to_maturity_name_rf_or_ir = new String[n_scenarios];
    this.combined_planting_month_name_rf_or_ir = new String[n_scenarios];
    this.combined_production_name = new String[n_scenarios];
    this.combined_yield_name = new String[n_scenarios];
    this.scenario_tag_with_snx = new String[n_scenarios];
    this.best_planting_month_yield_name = new String[n_scenarios]; // best planting raster name
    this.best_planting_month_yield_name_all_years = new String[n_scenarios][years.length];
    // this.best_month_maturity_name_all_years = new String[n_scenarios][years.length];
    this.best_planting_month_name_all_years = new String[n_scenarios][years.length];
    this.raster_names_average_year = new String[n_scenarios][planting_months.length];
    this.raster_names_all_years_dry = new String[n_scenarios][planting_months.length][years.length];
    this.raster_names_all_years_wet_or_dry =
        new String[n_scenarios][planting_months.length][years.length];
    this.output_stats_basenames = new String[n_scenarios][planting_months.length];
    this.output_maturity_mean = new String[n_scenarios][planting_months.length];
    this.best_planting_month_name = new String[n_scenarios];
    this.best_planting_month_maturity_name = new String[n_scenarios];
    this.yield_name = new String[n_scenarios];
    this.yield_names = new String[n_scenarios][planting_months.length];

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
      // this is then used in conjunction with output_stats_basenames[planting_month]
      // to save as a raster from the READ_DSSAT_outputs_from_cols_FEW script
      this.yield_name[i] =
          "BestYield_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the months with the best pixel in all month's rasters
      this.best_planting_month_name[i] =
          "BestMonth_noGCMcalendar_p0_"
              + this.snx_name[i]
              + "__"
              + this.run_descriptor[i]
              + "_"
              + wet_or_dry
              + "_"
              + rf_or_ir;

      // SPECIFIC TO THE CROP NAME
      // the name of the raster for: the months with the best pixel in all month's rasters
      this.best_planting_month_maturity_name[i] =
          "BestMonthMaturity_noGCMcalendar_p0_"
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
              + this.yield_name[i]
              + "_"
              + wet_or_dry;

      // if tag_month_month_identifier is empty, then this is the best_combined months
      // SPECIFIC TO THE CROP NAME
      // the raster name that yield times the crop area is stored for a scenario
      this.combined_production_name[i] = this.scenario_tag[i] + "_production";

      // if tag_month_month_identifier is empty, then this is the best_combined months
      // SPECIFIC TO THE CROP NAME
      // the raster name of the average irrigated and rainfed yield weighted by crop area
      this.combined_yield_name[i] = this.scenario_tag[i] + "_overall_yield";

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated yield is stored
      this.combined_yield_name_rf_or_ir[i] = this.scenario_tag[i] + "_averaged_" + rf_or_ir;

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated production is stored
      this.combined_production_name_rf_or_ir[i] = this.scenario_tag[i] + "_production_" + rf_or_ir;

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated planting month is stored
      this.combined_planting_month_name_rf_or_ir[i] =
          this.scenario_tag[i] + "_planting_month_" + rf_or_ir;

      // SPECIFIC TO THE CROP NAME AND WATERING STRATEGY
      // the raster name where the average of the rainfed or irrigated days to maturity is stored
      this.combined_days_to_maturity_name_rf_or_ir[i] =
          this.scenario_tag[i] + "_maturity_" + rf_or_ir;

      // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY
      this.scenario_tag_with_snx[i] = this.snx_name[i] + "_" + this.scenario_tag[i];

      // best_planting_month_yield_name is the name of the raster that is shown as a png image
      this.best_planting_month_yield_name[i] =
          this.scenario_tag_with_snx[i] + "_" + this.real_or_happy;

      for (int planting_month_index = 0;
          planting_month_index < this.planting_months.length;
          planting_month_index++) {
        // loop through the planting_months and create a raster name for each month

        String month = this.planting_months[planting_month_index];

        // CAN'T BE CHANGED:
        // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
        // SPECIFIC TO THE CROP NAME AND MONTH
        this.yield_names[i][planting_month_index] =
            month + "_noGCMcalendar_p0_" + this.crop_name[i] + "__" + this.run_descriptor[i];

        // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH
        this.raster_names_average_year[i][planting_month_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_names[i][planting_month_index]
                + "_"
                + wet_or_dry
                + "_"
                + this.real_or_happy;

        // CAN'T BE CHANGED:
        // (PREDETERMINED BY DSSAT AND READ_DSSAT_outputs_from_cols_FEW SCRIPT)
        this.output_stats_basenames[i][planting_month_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_names[i][planting_month_index];

        this.output_maturity_mean[i][planting_month_index] =
            this.output_stats_basenames[i][planting_month_index] + "_real_maturity_mean";

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
                  + this.yield_names[i][planting_month_index]
                  + "_"
                  + this.real_or_happy
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
                  + this.yield_names[i][planting_month_index]
                  + "_"
                  + this.real_or_happy
                  + "_"
                  + wet_or_dry
                  + "_"
                  + this.years[year_index];
        }
      }
      for (int year_index = 0; year_index < this.years.length; year_index++) {
        // SPECIFIC TO THE CULTIVAR AND WATERING STRATEGY AND MONTH
        this.best_planting_month_yield_name_all_years[i][year_index] =
            this.snx_name[i]
                + "_"
                + this.co2_level[i]
                + "_"
                + this.weather_prefix[i]
                + "_"
                + this.yield_name[i]
                + "_"
                + wet_or_dry
                + "_"
                + this.real_or_happy
                + "_y"
                + this.years[year_index];
        // the name of the raster for: the months with the best pixel in all month's rasters
        this.best_planting_month_name_all_years[i][year_index] =
            "BestMonth_noGCMcalendar_p0_"
                + this.snx_name[i]
                + "__"
                + this.run_descriptor[i]
                + "_"
                + wet_or_dry
                + "_"
                + rf_or_ir
                + "_y"
                + this.years[year_index];
      }

      // days to maturity defined for all years (maybe a future feature if desired)
      // this.best_month_maturity_result_name_all_years[i][year_index] =
      //     "BestMonthMaturity_noGCMcalendar_p0_"
      //         + this.snx_name[i]
      //         + "__"
      //         + this.run_descriptor[i]
      //         + "_"
      //         + wet_or_dry
      //         + "_"
      //         + rf_or_ir
      //         + "_y"
      //         + this.years[year_index];

    }

    // set the unique crop names to the uniqueCrops variable
    this.uniqueCrops = new TreeSet<String>();
    this.uniqueCrops.addAll(Arrays.asList(this.crop_name));

    // set the unique scenario tags to the uniqueCrops variable
    // this.unique_scenario_tags_for_production = new TreeSet<String>();
    // this.unique_scenario_tags_for_production.addAll(Arrays.asList(this.combined_production_name));

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

    List<PlantingScenario> scenariosAndPMList = new ArrayList<>();

    if (DSSAT_process_or_both.equals("DSSAT")) {

      scenariosAndPMList = getScenarioAndPMList(script_folder, scenarios, 0, 0);
      // System.out.println("scenariosAndPMList.size()");
      // System.out.println(scenariosAndPMList.size());

      // run a full set of scenarios, without processing anything
      scenarios.run_crop_model = true;
      scenarios.process_results = false;

      // Overwrite inputs from the config file (as we haven't processed yet, we can't do these
      // things)
      scenarios.average_yields = false;
      scenarios.find_best_yields = false;
      scenarios.calculate_each_year_best_month = false;
      scenarios.calculate_rf_or_ir_specific_average_yield = false;
      scenarios.calculate_rf_or_ir_specific_production = false;
      scenarios.calculate_rf_plus_ir_production = false;
      scenarios.calculate_average_yield_rf_and_ir = false;
      scenarios.make_rasters_comparing_overall_to_historical = false;
      scenarios.create_each_year_png = false;
      scenarios.create_average_png = false;
      scenarios.create_overall_png = false;

      runScenarios(script_folder, scenariosAndPMList, scenarios);
    } else if (DSSAT_process_or_both.equals("process")) {
      scenariosAndPMList = getScenarioAndPMList(script_folder, scenarios, 0, 0);
      System.out.println("scenariosAndPMList.size()");
      System.out.println(scenariosAndPMList.size());

      // process result of runs, using config file settings for what to calculate
      scenarios.run_crop_model = false;
      scenarios.process_results = true;

      processScenarios(script_folder, scenariosAndPMList, scenarios);

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
      scenarios.calculate_rf_or_ir_specific_average_yield = false;
      scenarios.calculate_rf_or_ir_specific_production = false;
      scenarios.calculate_rf_plus_ir_production = false;
      scenarios.calculate_average_yield_rf_and_ir = false;
      scenarios.make_rasters_comparing_overall_to_historical = false;
      scenarios.create_each_year_png = false;
      scenarios.create_average_png = false;
      scenarios.create_overall_png = false;

      scenariosAndPMList =
          getScenarioAndPMList(
              script_folder, scenarios, scenario_index, start_planting_month_index);
      System.out.println("scenariosAndPMList.size()");
      System.out.println(scenariosAndPMList.size());

      runScenarios(script_folder, scenariosAndPMList, scenarios);

      // } else if (DSSAT_process_or_both.equals("remainingDSSAT")) {
      //   runRemainingDSSAT();

    } else {
      System.out.println(
          "ERROR:DSSAT_process_or_both must be one of 'DSSAT', 'process', or 'continueDSSAT'");
      System.exit(1);
    }

    // run a full set of scenarios, but process everything this time and don't run the model

    // calculate aggregate production for crops
    // this involves calculating overall production, then calculating average yields
    CalculateProduction calculator = new CalculateProduction(script_folder, scenarios);
  }

  // // a function to run DSSAT for the remaining rasters
  // public static void runRemainingDSSAT()
  //     throws InterruptedException, IOException {
  //   String all_rasters_expected = "";
  //   String all_rasters_search_string = "";

  //   Set<String> unique_yield_result_names = new TreeSet<String>();

  //   raster_names_to_average = raster_names_to_average + raster_this_year;

  //   // we loop through the scenarios to build up a list of search strings, and a loop through
  // planting months for each scenario to get a list of rasters we expect to find from the search

  //   for (int i = 0; i < scenarios.n_scenarios; i++) {
  //     // add a newline after the raster names
  //     if (!all_rasters_expected.equals("")) {
  //       all_rasters_expected = all_rasters_expected + "\n";
  //     }

  //     // add a | character to add a logical "or" to the regex search expression
  //     if (!all_rasters_search_string.equals("")) {
  //       all_rasters_search_string = all_rasters_search_string + "|";
  //     }

  //     // add an additional search term for this crop and this run descriptor, should match all
  // planting months.
  //     all_rasters_search_string = all_rasters_search_string + "_noGCMcalendar_p0_" +
  // this.crop_name[i] + "__" + this.run_descriptor[i];

  //     for (int planting_month_index = start_planting_month_index_this_scenario;
  //         planting_month_index < planting_months.length;
  //         planting_month_index++) {
  //       // loop through the planting_months, and get the full raster result to run
  //       String yield_result_name_this_month = yield_names[planting_month_index];
  //       // append the month raster to the list of rasters we want to calculate with DSSAT
  //       all_rasters_expected = all_rasters_expected + yield_result_name_this_month;

  //     }

  //   }

  //   unique_yield_result_names.addAll(
  //       Arrays.asList(this.combined_production_name)
  //   );

  //   // get all the yield rasters, for each each combination of unique crop name and run
  // descriptor.
  //   // get unique crop names
  //   // get unique run descriptors
  //   // grep for the union of all combinations of these yield rasters of crop name and run
  // descriptor
  //   // save the grep results to a file
  //   // load the grepped file
  //   // check if any of the expected files are missing!

  //   BashScripts.createFileContainingAllRasters(script_folder,);
  //   String[] existing_rasters = FunTricks.readTextFileToArray(csv_location);

  //     // FunTricks.writeStringToFile(all_rasters_to_run, REMAININGDSSAT);
  // }

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

  public static Float getConversionToWetCoefficient(String cropName)
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

  public static List<PlantingScenario> getScenarioAndPMList(
      String script_folder,
      Scenarios scenarios,
      int start_scenario_index,
      int start_planting_month_index)
      throws InterruptedException, IOException {
    List<PlantingScenario> scenariosAndPMList = new ArrayList<>();

    String crop_name = "";
    int start_planting_month_index_this_scenario;
    for (int i = start_scenario_index; i < scenarios.n_scenarios; i++) {
      if (i == start_scenario_index) {
        start_planting_month_index_this_scenario = start_planting_month_index;
      } else {
        start_planting_month_index_this_scenario = 0;
      }
      crop_name = scenarios.crop_name[i];

      for (int planting_month_index = start_planting_month_index_this_scenario;
          planting_month_index < scenarios.planting_months.length;
          planting_month_index++) {

        // create an array of planting months and scenario numbers to run
        scenariosAndPMList.add(new Scenarios.PlantingScenario(planting_month_index, i));
      } // END planting_months creation loop
    } // END scenario creation loop

    // return a list of all the planting month indices and scenario indices to run or process
    return scenariosAndPMList;
  }

  public static void runScenarios(
      String script_folder, List<PlantingScenario> scenariosAndPMList, Scenarios scenarios)
      throws InterruptedException, IOException {

    // loop through the array of planting months and scenario numbers, so that in the inner loop but
    // for DSSAT, just run X at a time, and for process, have a second loop that goes through each
    // one individually
    // int X = 1;
    // ExecutorService executorService = Executors.newFixedThreadPool(X);

    // now that we made all the initialized data files, we can loop through and run DSSAT using the
    // generated data.
    Set<Integer> initializedScenarios = new HashSet<>();

    for (PlantingScenario scenario_and_pm : scenariosAndPMList) {
      if (!initializedScenarios.contains(scenario_and_pm.scenarioNumber)) {
        // Perform initialization for this scenario here

        // this needs to initialize for every scenario! Because it depends on the masking, which
        // depends
        // on the irrigation (I'm pretty sure) as well as the cultivar

        System.out.println("");
        System.out.println(
            "==== Initializing geographic and other input data and rasters needed for grass ===");
        // System.out.println(
        //     "     (cannot happen in parallel because it sets GRASS gis shell variables)");
        System.out.println("");
        BashScripts.initGRASS(
            script_folder,
            scenarios.region_to_use_n[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_s[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_e[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_w[scenario_and_pm.scenarioNumber],
            scenarios.nsres[scenario_and_pm.scenarioNumber],
            scenarios.ewres[scenario_and_pm.scenarioNumber],
            scenarios.planting_months,
            scenarios.mask_for_this_snx[scenario_and_pm.scenarioNumber],
            scenarios.crop_name[scenario_and_pm.scenarioNumber],
            scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber],
            scenarios.minimum_physical_area,
            scenarios.nitrogen[scenario_and_pm.scenarioNumber]);
        System.out.println("");
        System.out.println("==== Done initializing geographic and GRASS data ===");
        System.out.println("");
        // Add any other initialization code for the scenario here
        initializedScenarios.add(scenario_and_pm.scenarioNumber);
      }

      System.out.println("");
      System.out.println("==== RUNNING GRIDDED DSSAT ===");
      System.out.println(
          "run: "
              + scenarios.run_descriptor[scenario_and_pm.scenarioNumber]
              + "\ncrop name: "
              + scenarios.crop_name[scenario_and_pm.scenarioNumber]
              + "\nplanting month: "
              + scenarios.planting_months[scenario_and_pm.plantingMonth]
              + "\nsnx_name: "
              + scenarios.snx_name[scenario_and_pm.scenarioNumber]
              + " (scenario index "
              + (scenario_and_pm.scenarioNumber + 1)
              + " out of "
              + scenariosAndPMList.size()
              + " scenarios -- which is RF and IR for all cultivars)");
      System.out.println("==================");

      BashScripts.runScenario(
          script_folder,
          scenarios.snx_name[scenario_and_pm.scenarioNumber],
          scenarios.co2_level[scenario_and_pm.scenarioNumber],
          scenarios.crop_name[scenario_and_pm.scenarioNumber],
          scenarios.weather_prefix[scenario_and_pm.scenarioNumber],
          scenarios.weather_folder[scenario_and_pm.scenarioNumber],
          scenarios
              .output_stats_basenames[scenario_and_pm.scenarioNumber][
              scenario_and_pm.plantingMonth],
          scenarios.fertilizer_scheme[scenario_and_pm.scenarioNumber],
          scenarios
              .n_chunks); // TODO: put the number of threads used (num chunks) in the config file!

      System.out.println("");
      System.out.println("");
      System.out.println("---------------");
      System.out.println(
          "completed "
              + (scenario_and_pm.plantingMonth + 1)
              + " out of "
              + (scenarios.planting_months.length)
              + " planting months");
      System.out.println("---------------");
      System.out.println("");
      System.out.println("");

      saveProgressToFile(
          script_folder,
          scenarios.run_descriptor[scenario_and_pm.scenarioNumber],
          scenario_and_pm.plantingMonth,
          scenario_and_pm.scenarioNumber);
    }

    // executorService.shutdown();
    // executorService.shutdown();
    // try {
    //     if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
    //         executorService.shutdownNow();
    //         System.out.println("ERROR: Process Ran out of time!");
    //         System.exit(1);
    //     }
    // } catch (InterruptedException e) {
    //     executorService.shutdownNow();
    // }
  }

  public static void processScenarios(
      String script_folder, List<PlantingScenario> scenariosAndPMList, Scenarios scenarios)
      throws InterruptedException, IOException {

    for (PlantingScenario scenario_and_pm : scenariosAndPMList) {
      // loop for each scenario, then each planting month

      BashScripts.initGRASS(
          script_folder,
          scenarios.region_to_use_n[scenario_and_pm.scenarioNumber],
          scenarios.region_to_use_s[scenario_and_pm.scenarioNumber],
          scenarios.region_to_use_e[scenario_and_pm.scenarioNumber],
          scenarios.region_to_use_w[scenario_and_pm.scenarioNumber],
          scenarios.nsres[scenario_and_pm.scenarioNumber],
          scenarios.ewres[scenario_and_pm.scenarioNumber],
          scenarios.planting_months,
          scenarios.mask_for_this_snx[scenario_and_pm.scenarioNumber],
          scenarios.crop_name[scenario_and_pm.scenarioNumber],
          scenarios.output_stats_basenames[scenario_and_pm.scenarioNumber],
          scenarios.minimum_physical_area,
          scenarios.nitrogen[scenario_and_pm.scenarioNumber]);

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
          scenarios
              .run_descriptor[
              scenario_and_pm
                  .scenarioNumber]); // TODO: put the number of threads used (num chunks) in the
      // config file!

      String[] non_yield_parameters_to_save = new String[] {"n_bad_things", "real_maturity_mean"};

      String[] outputs_to_save =
          generateColumnsToMakeRastersFromDataFiles(
              scenarios.years, scenarios.real_or_happy, non_yield_parameters_to_save);
      // new String[] {
      //   // "n_bad_things",
      //   "happy_yield_mean",
      //   "yield_mean",
      //   "real_maturity_mean",
      //   // "n_contributing_real",
      //   // "n_real_exactly_zero",
      //   // "n_no_planting",
      //   // "real_0",
      //   // "real_1",
      //   // "real_2",
      //   // "real_3",
      //   // "real_4",
      //   // "real_5",
      //   // "real_6",
      //   // "real_7"
      // };

      readDSSAToutputs(
          script_folder,
          scenarios
              .output_stats_basenames[scenario_and_pm.scenarioNumber][
              scenario_and_pm.plantingMonth],
          outputs_to_save);

      for (int year_index = 0; year_index < scenarios.years.length; year_index++) {
        convertToWetWeightIfNeeded(
            scenarios.calculate_as_wet_weight,
            script_folder,
            scenarios.crop_name[scenario_and_pm.scenarioNumber], // input variable
            raster_name_all_years_this_month_dry[year_index], // input raster
            raster_name_all_years_this_month_wet_or_dry[year_index] // output raster
            );
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

  public static void saveProgressToFile(
      String scripts_folder, String run_descriptor, int planting_month_index, int scenario_index)
      throws InterruptedException {
    String outputBaseName = scripts_folder + "/interrupted_run_locations/" + run_descriptor;
    String dataOutName = outputBaseName + "_pmonth_scenariolast6.txt";
    String magicDelimiter = ","; // You can choose any delimiter you like

    try (FileOutputStream dataOutStream = new FileOutputStream(dataOutName);
        PrintWriter dataOut = new PrintWriter(dataOutStream)) {
      // System.out.println("Current working directory: " + System.getProperty("user.dir"));
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

  // public static String formatString(String value) {
  //   if (value.length() > 12 || !value.matches("\\d*")) {
  //     // Ensure that the string is within the length and contains only digits
  //     throw new IllegalArgumentException(
  //         "String length exceeds 12 characters or contains non-digit characters.");
  //   }

  //   // %012s means:
  //   // % - start of the format specifier
  //   // 0 - pad with zeros
  //   // 12 - at least 12 characters wide
  //   // s - string
  //   return String.format("%012s", value);
  // }

  public static void readDSSAToutputs(
      String script_folder, String fileToProcess, String[] column_titles)
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
          script_folder, String.join("\n", table_to_build_raster), fileToProcess + "_" + colname);

      // do some error reporting!

      if (colname.equals("n_bad_things")) {
        int maturity_errors = 0;
        int flowering_errors = 0;
        int emergence_errors = 0;
        int other_errors = 0;
        for (int i = 0; i < latLonData.size(); i++) {
          // System.out.println(singleColumnOfData[i]);
          String formatted_bad_things =
              ("000000000000" + singleColumnOfData[i]).substring(singleColumnOfData[i].length());
          // String formatted_bad_things = String.format("%012s", singleColumnOfData[i]);
          // String formatted_bad_things = formatString(singleColumnOfData[i]);
          // System.out.println("formatted_bad_thing");
          // System.out.println(formatted_bad_things);

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
        if (maturity_errors > 0 || flowering_errors > 0 || emergence_errors > 0) {
          System.out.println(fileToProcess + ": ");
          System.out.println(
              "  Bad things (out of "
                  + latLonData.size()
                  + " total cells real for each year ran (each year and pixel can add one"
                  + " error)):\n"
                  + "      Maturity: "
                  + maturity_errors
                  + "\n      Flowering: "
                  + flowering_errors
                  + "\n      Emergence: "
                  + emergence_errors
                  + "\n      Other (\"extras\"): "
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
        "average",
        results_folder);
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
    // script_folder,
    // scenarios.planting_months,
    // scenarios.raster_names_average_year[i],
    // scenarios.best_planting_month_yield_name[i],
    // scenarios.best_planting_month_name[i],
    // scenarios.output_maturity_mean[i],
    // scenarios.best_planting_month_maturity_name[i],
    // scenarios.results_folder[i]);
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

    // System.out.println("running composite raster");
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
    // month in each cell

    // BashScripts.createPNG(script_folder, best_planting_month_yield_name, results_folder);
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

    System.out.println(
        "running composite raster to get best yields on the results (should differ with"
            + " cultivar):");
    System.out.println(combined_yields_results);
    System.out.println("output yield name:");
    System.out.println(best_planting_month_yield_name);
    BashScripts.compositeRaster(
        script_folder,
        combined_yields_results, // input rasters to find max among
        best_planting_month_yield_name, // output max yield raster name
        best_planting_month_name, // output a grass gis raster of this name for which month max was
        // chosen from
        results_folder);

    // BashScripts.createPNG(script_folder, best_planting_month_yield_name, results_folder);

  } // findBestYields
} // class Scenarios
