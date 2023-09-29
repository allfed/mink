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

    this.planting_months = config.physical_parameters.planting_months.toArray(new String[0]);
    this.years = config.physical_parameters.years.toArray(new String[0]);

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

    String[] initFileContents = importScenariosCSV(simulation_csv_location);
    // System.out.println("import scenarios");
    int n_scenarios = initFileContents.length - 1;
    System.out.println("n_scenarios");
    System.out.println(n_scenarios);

    // System.out.println("new scenario");
    Scenarios scenarios = new Scenarios(initFileContents, config_file);

    BashScripts.setGRASSRegion(script_folder, scenarios.config);

    List<PlantingScenario> scenariosAndPMList = new ArrayList<>();

    if (DSSAT_process_or_both.equals("DSSAT")) {

      scenariosAndPMList = getScenarioAndPMList(script_folder, scenarios, 0, 0);

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

      ScenariosRunner.runScenarios(script_folder, scenariosAndPMList, scenarios);
    } else if (DSSAT_process_or_both.equals("process")) {
      scenariosAndPMList = getScenarioAndPMList(script_folder, scenarios, 0, 0);

      // process result of runs, using config file settings for what to calculate
      scenarios.run_crop_model = false;
      scenarios.process_results = true;

      ScenariosProcessor.processScenarios(
          script_folder, scenariosAndPMList, run_parameters_csv_folder, scenarios);

    } else if (DSSAT_process_or_both.equals("continueDSSAT")) {
      // continue a previous run
      int[] loadedValues =
          ScenariosRunner.loadProgressFromFile(script_folder, scenarios.run_descriptor[0]);

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

      ScenariosRunner.runScenarios(script_folder, scenariosAndPMList, scenarios);

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
        scenariosAndPMList.add(new PlantingScenario(planting_month_index, i));
      } // END planting_months creation loop
    } // END scenario creation loop

    // return a list of all the planting month indices and scenario indices to run or process
    return scenariosAndPMList;
  }
} // class Scenarios
