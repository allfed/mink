// This file loads the scenarios from a CSV and runs the scenarios
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class CalculateProduction {

  public CalculateProduction(String script_folder, Scenarios scenarios)
      throws InterruptedException, FileNotFoundException, IOException {

    BashScripts.setGRASSRegion(script_folder, scenarios.config);

    calculateRFandIRProductionGivenHarvestYields(
        script_folder,
        scenarios,
        scenarios.best_planting_month_yield_name,
        scenarios.best_planting_month_name,
        scenarios.best_planting_month_maturity_name,
        scenarios.combined_yield_name_rf_or_ir,
        scenarios.combined_production_name_rf_or_ir,
        scenarios.combined_planting_month_name_rf_or_ir,
        scenarios.combined_days_to_maturity_name_rf_or_ir,
        scenarios.combined_yield_name,
        scenarios.combined_production_name,
        true);

    String[] average_best_combined_production_name = new String[scenarios.n_scenarios];
    for (int i = 0; i < scenarios.n_scenarios; i++) {
      average_best_combined_production_name[i] = scenarios.combined_production_name[i] + "_avgbest";
    }

    loopOverRainfedAndIrrigated(
        script_folder,
        scenarios,
        scenarios.combined_production_name_rf_or_ir,
        average_best_combined_production_name,
        scenarios.combined_yield_name,
        scenarios.combined_yield_name_rf_or_ir,
        scenarios.combined_planting_month_name_rf_or_ir,
        scenarios.combined_days_to_maturity_name_rf_or_ir,
        true);

    if (scenarios.calculate_each_year_best_month) {
      for (int year_index = 0; year_index < scenarios.years.length; year_index++) {
        // create the appropriate arrays for the year in question (one for each scenario)
        String[] best_planting_month_yield_name_this_year = new String[scenarios.n_scenarios];
        String[] best_planting_month_name_this_year = new String[scenarios.n_scenarios];
        String[] combined_yield_name_rf_or_ir_this_year = new String[scenarios.n_scenarios];
        String[] combined_production_name_rf_or_ir_this_year = new String[scenarios.n_scenarios];
        String[] combined_planting_month_name_rf_or_ir_this_year =
            new String[scenarios.n_scenarios];
        String[] combined_days_to_maturity_name_rf_or_ir_this_year =
            new String[scenarios.n_scenarios];
        String[] combined_yield_name_this_year = new String[scenarios.n_scenarios];
        String[] combined_production_name_this_year = new String[scenarios.n_scenarios];

        // construct the raster names to be once per scenario, for the year in this loop (the year
        // at year_index)
        for (int i = 0; i < scenarios.n_scenarios; i++) {
          best_planting_month_yield_name_this_year[i] =
              scenarios.best_planting_month_yield_name_all_years[i][year_index];
          best_planting_month_name_this_year[i] =
              scenarios.best_planting_month_name_all_years[i][year_index];
          combined_yield_name_rf_or_ir_this_year[i] =
              scenarios.combined_yield_name_rf_or_ir[i] + "_y" + scenarios.years[year_index];
          combined_production_name_rf_or_ir_this_year[i] =
              scenarios.combined_production_name_rf_or_ir[i] + "_y" + scenarios.years[year_index];
          combined_planting_month_name_rf_or_ir_this_year[i] =
              scenarios.combined_planting_month_name_rf_or_ir[i]
                  + "_y"
                  + scenarios.years[year_index];
          combined_days_to_maturity_name_rf_or_ir_this_year[i] =
              scenarios.combined_days_to_maturity_name_rf_or_ir[i]
                  + "_y"
                  + scenarios.years[year_index];
          combined_production_name_this_year[i] =
              scenarios.combined_production_name[i] + "_y" + scenarios.years[year_index];
          combined_yield_name_this_year[i] =
              scenarios.combined_yield_name[i] + "_y" + scenarios.years[year_index];
        }

        calculateRFandIRProductionGivenHarvestYields(
            script_folder,
            scenarios,
            best_planting_month_yield_name_this_year,
            best_planting_month_name_this_year,
            new String[] {""}, // no days to maturity data on a yearly basis is available.
            combined_yield_name_rf_or_ir_this_year,
            combined_production_name_rf_or_ir_this_year,
            combined_planting_month_name_rf_or_ir_this_year,
            new String[] {""}, // no days to maturity data on a yearly basis is available.
            combined_yield_name_this_year,
            combined_production_name_this_year,
            false); // calculate_maturity set to false

        loopOverRainfedAndIrrigated(
            script_folder,
            scenarios,
            combined_production_name_rf_or_ir_this_year,
            combined_production_name_this_year,
            combined_yield_name_this_year,
            combined_yield_name_rf_or_ir_this_year,
            combined_planting_month_name_rf_or_ir_this_year,
            new String[] {""}, // no days to maturity data on a yearly basis is available.
            false); // export_to_countries set to false... refers to exporting irrigation and
        // rainfed separately
      }
    }
    // Extract and save yield for the single best planting month for all years
    extractYieldForBestPlantingMonthAllYears(
        script_folder,
        scenarios,
        scenarios
            .best_planting_month_name, // array of raster names for best planting month per scenario
        scenarios.raster_names_all_years_wet_or_dry, // [scenario][planting_month][year]
        scenarios.years,
        scenarios.scenario_tag,
        scenarios.results_folder,
        "_using_avgbest_month");
    if (scenarios.calculate_each_year_best_month) {
      extractYieldForBestPlantingMonthAllYearsUsingYearSpecificBest(
          script_folder,
          scenarios,
          scenarios.best_planting_month_name_all_years, // Use YEAR-SPECIFIC best planting months
          scenarios.raster_names_all_years_wet_or_dry,
          scenarios.years,
          scenarios.scenario_tag,
          scenarios.results_folder);
    }
  } // end CalculateProduction function

  private static <T> T getVerifiedFirst(List<T> list) {
    assert verifyAllEqualUsingALoop(list);
    return list.get(0);
  }

  // can either calculate production for a single year, or for different multiple years
  // it either uses the average harvest or the harvest for a single year
  public static void calculateRFandIRProductionGivenHarvestYields(
      String script_folder,
      Scenarios scenarios,
      String[] best_planting_month_yield_name,
      String[] best_planting_month_name,
      String[] best_planting_month_maturity_name,
      String[] combined_yield_name_rf_or_ir,
      String[] combined_production_name_rf_or_ir,
      String[] combined_planting_month_name_rf_or_ir,
      String[] combined_days_to_maturity_name_rf_or_ir,
      String[] combined_yield_name,
      String[] combined_production_name,
      boolean calculate_maturity)
      throws InterruptedException, IOException {

    // average yield results for each crop and irrigation grouping (regardless of cultivar)

    // the yield
    List<List<String>> cultivar_groups_rasters =
        getCultivarGroups(scenarios, best_planting_month_yield_name);

    // the planting months
    List<List<String>> cultivar_groups_planting_months =
        getCultivarGroups(scenarios, best_planting_month_name);

    List<List<String>> cultivar_groups_days_to_maturity =
        calculate_maturity
            ? getCultivarGroups(scenarios, best_planting_month_maturity_name)
            : new ArrayList<>();

    List<List<String>> combined_yields_rf_or_ir =
        getCultivarGroups(scenarios, combined_yield_name_rf_or_ir);
    List<List<String>> combined_production_rf_or_ir =
        getCultivarGroups(scenarios, combined_production_name_rf_or_ir);
    List<List<String>> combined_yields = getCultivarGroups(scenarios, combined_yield_name);
    List<List<String>> combined_production = getCultivarGroups(scenarios, combined_production_name);
    List<List<String>> scenario_tags_for_planting_month =
        getCultivarGroups(scenarios, combined_planting_month_name_rf_or_ir);
    List<List<String>> scenario_tags_for_days_to_maturity =
        calculate_maturity
            ? getCultivarGroups(scenarios, combined_days_to_maturity_name_rf_or_ir)
            : new ArrayList<>();

    List<List<String>> results_folder_groups =
        getCultivarGroups(scenarios, scenarios.results_folder);
    List<List<String>> crop_groups = getCultivarGroups(scenarios, scenarios.crop_name);
    List<List<String>> irrigation_groups = getCultivarGroups(scenarios, scenarios.rf_or_ir);

    for (int i = 0; i < cultivar_groups_rasters.size(); i++) {
      // assert (cultivar_groups_rasters.size() == 2)
      //     : "ERROR: only one crop processed at a time, and you need both rainfed and irrigated.";
      // these are different within the same cultivar group.
      List<String> rasters = cultivar_groups_rasters.get(i);
      List<String> planting_months = cultivar_groups_planting_months.get(i);
      List<String> days_to_maturity =
          calculate_maturity ? cultivar_groups_days_to_maturity.get(i) : new ArrayList<>();

      // concatenate the rasters in the group into a comma separated string
      String raster_names_to_combine = getCommaSeparatedString(rasters);
      String planting_months_to_combine = getCommaSeparatedString(planting_months);
      String days_to_maturity_to_combine =
          calculate_maturity ? getCommaSeparatedString(days_to_maturity) : null;

      // skip if no rasters in the cultivar group
      if (rasters.size() == 0) {
        continue;
      }

      // These are going to be combined for every element of the cultivar group. So just get the
      // first one.
      // But also make sure they're all the same within the cultivar group.
      String irrigation = getVerifiedFirst(irrigation_groups.get(i));
      String crop = getVerifiedFirst(crop_groups.get(i));
      String cg_combined_yield_name_rf_or_ir = getVerifiedFirst(combined_yields_rf_or_ir.get(i));
      String cg_combined_yield_name = getVerifiedFirst(combined_yields.get(i));
      String cg_combined_production_name_rf_or_ir =
          getVerifiedFirst(combined_production_rf_or_ir.get(i));
      String cg_combined_production_name = getVerifiedFirst(combined_production.get(i));
      String cg_combined_planting_month_name_rf_or_ir =
          getVerifiedFirst(scenario_tags_for_planting_month.get(i));
      String cg_combined_days_to_maturity_name_rf_or_ir =
          calculate_maturity ? getVerifiedFirst(scenario_tags_for_days_to_maturity.get(i)) : null;
      String results_folder = getVerifiedFirst(results_folder_groups.get(i));

      String crop_area_raster = getCropAreaRaster(scenarios, crop, irrigation);

      // average or find max of the yield results for the current crop and irrigation grouping
      // (so that would average all cultivars for the crop)
      combineAndCalculateProductionRForIR(
          crop,
          crop_area_raster,
          scenarios.create_average_png,
          scenarios.calculate_rf_or_ir_specific_average_yield,
          scenarios.calculate_rf_or_ir_specific_production,
          scenarios.config.physical_parameters.crop_area_type,
          script_folder,
          raster_names_to_combine,
          planting_months_to_combine,
          days_to_maturity_to_combine,
          cg_combined_yield_name_rf_or_ir,
          cg_combined_production_name_rf_or_ir,
          cg_combined_planting_month_name_rf_or_ir,
          cg_combined_days_to_maturity_name_rf_or_ir,
          calculate_maturity,
          results_folder);
    }
  }

  // public static void createAveragePNG(
  //     String script_folder,
  //     Scenarios scenarios,
  //     List<List<String>> cultivar_groups_rasters,
  //     List<List<String>> combined_yields_rf_or_ir)
  //     throws InterruptedException, IOException {

  //   String historical_yield_raster =
  //       scenarios.config.getCropNameCaps(scenarios.crop_name[0]) + "_yield";
  //   List<String> rasters_to_render = new ArrayList<>();
  //   rasters_to_render.add(historical_yield_raster);

  //   for (int i = 0; i < cultivar_groups_rasters.size(); i++) {
  //     List<String> rasters = cultivar_groups_rasters.get(i);

  //     // skip if no rasters in the cultivar group
  //     if (rasters.size() == 0) {
  //       continue;
  //     }

  //     List<String> averaging_tags = combined_yields_rf_or_ir.get(i);

  //     String averaging_tag = averaging_tags.get(0);
  //     rasters_to_render.add(averaging_tag);
  //   }

  //   BashScripts.createPNG(
  //       script_folder,
  //       rasters_to_render.toArray(new String[0]), // convert to array here
  //       scenarios.results_folder[0]);
  // } // end createAveragePNG function

  // https://www.baeldung.com/java-list-all-equal
  // later modified by chatgpt to make it generic
  private static <T> boolean verifyAllEqualUsingALoop(List<T> list) {
    if (list.isEmpty()) {
      return true;
    }
    T firstItem = list.get(0);
    for (T item : list) {
      if (!item.equals(firstItem)) {
        return false;
      }
    }
    return true;
  }

  // put all the rasters in a list of lists. The inner list contains groups of rasters with all the
  // cultivars for a crop, separately for rainfed and irrigated
  public static List<List<String>> getCultivarGroups(
      Scenarios scenarios, String[] property_to_collect) throws InterruptedException, IOException {

    String[] rainfed_and_irrigated = {"RF", "IR"};

    // Create a list to hold the lists of scenarios per crop and irrigation type
    List<List<String>> scenariosPerCropAndIrrigation = new ArrayList<>();
    List<String> crops = new ArrayList<String>();
    crops.addAll(scenarios.uniqueCrops);

    // Initialize the list of lists
    for (String rainfed_or_irrigated : rainfed_and_irrigated) {
      for (String crop : scenarios.uniqueCrops) {
        scenariosPerCropAndIrrigation.add(new ArrayList<String>());
      }
    }

    // loop through the unique crops and add the scenarios to the appropriate list
    for (int i = 0; i < scenarios.n_scenarios; i++) {
      String crop = scenarios.crop_name[i];
      String scenario_RF_or_IR =
          scenarios.combined_yield_name_rf_or_ir[i].substring(
              scenarios.combined_yield_name_rf_or_ir[i].length() - 2);

      assert ("IR".equals(scenario_RF_or_IR) || "RF".equals(scenario_RF_or_IR));

      // Get the index of the list that corresponds to the current crop and irrigation type
      // yeah... this is chatGPT's idea to use MATH to make each index unique.
      // sorry.
      // example: crops is [a, b, c] with indices [0, 1, 2]
      //         rf_and_ir is [RF, IR] with indices [0, 1]
      //         to get a unique index, [aRF aIR bRF bIR cRF cIR]
      //                     has index, [0   1   2   3   4   5  ]
      //         we multiply len(rf_and_ir)=2 by crops indices + rf_and_ir
      //         to get unique index
      int index =
          crops.indexOf(crop) * rainfed_and_irrigated.length
              + Arrays.asList(rainfed_and_irrigated).indexOf(scenario_RF_or_IR);

      // Add the current scenario to the appropriate list
      scenariosPerCropAndIrrigation.get(index).add(property_to_collect[i]);
    }

    return scenariosPerCropAndIrrigation;
  }

  public static String getCommaSeparatedString(List<String> cultivar_group)
      throws InterruptedException, IOException {
    // average the yield results for the current crop and irrigation grouping
    // by combining the rasters in the group into a single comma separated string
    String raster_names_to_average = "";
    for (String raster_name : cultivar_group) {

      // add a comma after the raster names
      if (!raster_names_to_average.equals("")) {
        raster_names_to_average = raster_names_to_average + ",";
      }

      raster_names_to_average = raster_names_to_average + raster_name;
    }

    return raster_names_to_average;
  }

  public static void combineAndCalculateProductionRForIR(
      String crop,
      String crop_area_raster,
      boolean create_average_png,
      boolean calculate_rf_or_ir_specific_average_yield,
      boolean calculate_rf_or_ir_specific_production,
      String crop_area_type,
      String script_folder,
      String raster_names_to_combine,
      String planting_months_to_combine,
      String days_to_maturity_to_combine,
      String combined_yield_name_rf_or_ir,
      String combined_production_name_rf_or_ir,
      String combined_planting_month_name_rf_or_ir,
      String combined_days_to_maturity_name_rf_or_ir,
      boolean calculate_maturity,
      String results_folder)
      throws InterruptedException, IOException {

    // average or find max of all the cultivars for the crop to estimate the crop's overall yield
    // this is specific to whether IR or RF
    // For soybean we get the best cultivar's (maturity group's) yield
    // For wheat we sometimes get the best cultivar's yield, and sometimes get the average, based on
    // the country
    // (max is for winter wheat)
    if (calculate_rf_or_ir_specific_average_yield) {
      if (crop.equals("soybeans") || crop_area_type.equals("no_crops")) {

        // System.out.println("average rasters USING max value");
        // System.out.println();
        // System.out.println("raster_names_to_combine");
        // System.out.println(raster_names_to_combine);
        // System.out.println("combined_yield_name_rf_or_ir");
        // System.out.println(combined_yield_name_rf_or_ir);

        // System.out.println("combined_planting_month_name_rf_or_ir");
        // System.out.println(combined_planting_month_name_rf_or_ir);
        // System.out.println("(Not used for pm) planting_months_to_combine");
        // System.out.println(planting_months_to_combine);
        // System.out.println();

        // gets the max of all available yields in each cell (all regions)
        BashScripts.compositeRaster(
            script_folder,
            raster_names_to_combine, // input rasters to average
            combined_yield_name_rf_or_ir, // output best raster yield value
            "deleteme_best_cultivar", // output a grass gis raster of the name from
            // which the maximum yielding cultivar was chosen
            results_folder);

        BashScripts.useKeyRasterToMapToValueRaster(
            script_folder,
            "deleteme_best_cultivar", // input key raster for which cultivar to get
            // value from
            planting_months_to_combine, // input rasters to get the values from
            combined_planting_month_name_rf_or_ir); // output raster with planting months

        if (calculate_maturity) {
          // use the best_maturity_group_soybean_key to choose the planting month of interest
          // planting_months_to_combine
          BashScripts.useKeyRasterToMapToValueRaster(
              script_folder,
              "deleteme_best_cultivar", // input key raster for which cultivar to get
              // value from
              days_to_maturity_to_combine, // input rasters to get the values from
              combined_days_to_maturity_name_rf_or_ir); // output raster with days to maturity of
          // best cultivar in each cell
        }

      } else if (crop.equals("wheat")) {
        // gets the max of  all available yields in each cell for regions in the
        // winter_wheat_dominant map, and otherwise just gets the average

        // the idea here is that for the winter wheat dominant countries, we need winter wheat
        // to dominate. so we are arbitrarily enforcing a list of western european countries to
        // use the highest yielding variety rather than the overall average of relevant varieties.
        BashScripts.getBestOrAverageBasedOnCountryMasks(
            script_folder,
            raster_names_to_combine, // input
            "winter_wheat_countries_mask", // input
            combined_yield_name_rf_or_ir, // output
            results_folder);

        BashScripts.useKeyRasterAndMaskForGettingMaxOrAverage(
            script_folder,
            planting_months_to_combine, // input
            "winter_wheat_countries_mask", // input
            "mode", // method of averaging
            raster_names_to_combine, // input
            combined_planting_month_name_rf_or_ir // output
            );

        if (calculate_maturity) {
          BashScripts.useKeyRasterAndMaskForGettingMaxOrAverage(
              script_folder,
              days_to_maturity_to_combine, // input
              "winter_wheat_countries_mask", // input
              "average", // method of averaging
              raster_names_to_combine, // input
              combined_days_to_maturity_name_rf_or_ir // output
              );
        }

      } else {
        // gets the average of all available yields in each cell
        BashScripts.averageRasters(
            script_folder,
            raster_names_to_combine, // input list to average
            combined_yield_name_rf_or_ir, // output averaged list
            "0", // minimum yields
            "average" // method of average
            );

        // gets the average of planting months in each cell
        BashScripts.averageRasters(
            script_folder,
            planting_months_to_combine, // input list to average
            combined_planting_month_name_rf_or_ir, // output averaged list
            "0", // minimum planting month to consider
            "mode" // method of average
            );

        // gets the average of days to maturity in each cell
        if (calculate_maturity) {
          BashScripts.averageRasters(
              script_folder,
              days_to_maturity_to_combine, // input raster
              combined_days_to_maturity_name_rf_or_ir, // output raster
              "0", // minimum days to maturity to consider
              "median" // method of average
              );
        }
      }
    }

    // use averaged yields to calculate production for either rainfed or irrigated yield.
    // area raster refers to rainfed or irrigated area of a crop, but is not specific to a
    // cultivar (or variety)

    if (calculate_rf_or_ir_specific_production) {
      BashScripts.calculateProduction(
          script_folder,
          combined_yield_name_rf_or_ir,
          crop_area_raster,
          combined_production_name_rf_or_ir);
    }
  }

  public static String getCropAreaRaster(Scenarios scenarios, String crop, String irrigation) {

    String prefix = "";

    if (scenarios.crop_area_type.equals("all")) {
      prefix = "ALL_CROPS";
    } else if (scenarios.crop_area_type.equals("food_crops")) {
      prefix = "ALL_FOOD_CROPS";
    } else if (scenarios.crop_area_type.equals("no_crops")) {
      prefix = "LAND_AREA_NO_CROPS";
    } else if (scenarios.crop_area_type.equals("specific")) {
      prefix = scenarios.config.getCropNameCaps(crop);
    } else {
      System.out.println(
          "Error: make sure crop_area_type is all, food_crops, no_crops, or specific");
      System.exit(1);
    }
    String crop_area_raster =
        prefix + "_" + (irrigation.equals("RF") ? "rainfed_cropland" : "irrigated_cropland");

    return crop_area_raster;
  }

  /**
   * Sums the production values for rainfed and irrigated conditions and performs additional
   * calculations based on the scenarios.
   */
  public static void loopOverRainfedAndIrrigated(
      String script_folder,
      Scenarios scenarios,
      String[] combined_production_name_rf_or_ir,
      String[] combined_production_name,
      String[] combined_yield_name,
      String[] combined_yield_name_rf_or_ir,
      String[] combined_planting_month_name_rf_or_ir,
      String[] combined_days_to_maturity_name_rf_or_ir,
      boolean export_to_countries)
      throws InterruptedException, IOException {
    // sums the averaged crop production rasters, if there's an RF tag and associated IR tag. Saves
    // the results as an .asc file.

    // rainfed and irrigated are identified from snx_name last 2 letters (either "RF" or "IR"). this
    // will be used to specify for the first 2 letters appended to the
    // scenarios.combined_production_name

    // run a series of crops of the same species in order to act in aggregate on them
    // we are aggregating over rainfed and irrigated production (if rainfed and irrigated exist for
    // the same crop)

    // so in summary, we're generating the raster names to sum and the crop names to sum,  then
    // actually summing both

    // Initialize an empty map to remember completed rf_or_ir tags
    ArrayList<String> completed_tags = new ArrayList<>();
    // set the unique scenario tags to the uniqueCrops variable

    Set<String> unique_combined_production_name = new TreeSet<String>();
    unique_combined_production_name.addAll(Arrays.asList(combined_production_name));

    assert unique_combined_production_name.size() == 1
        : "ERROR: only one crop (e.g. wheat, maize) processing allowed at a time";

    for (String tag : unique_combined_production_name) {
      // loop through the unique crops

      String raster_names_to_sum = "";
      String crop_area_to_sum = "";

      int last_index_of_crop = 0;
      int number_of_loops = 0;
      for (int i = 0; i < scenarios.n_scenarios; i++) {

        // only consider the scenarios which are running the current crop for averaging
        // we only need to use the results from a given scenario tag once
        if (!tag.equals(combined_production_name[i])) {
          continue;
        }

        // If the map does not already contain an entry for this scenario type, continue
        // this allows us to find the data for a scenario tag
        if (completed_tags.contains(combined_production_name_rf_or_ir[i])) {
          continue;
        }
        completed_tags.add(combined_production_name_rf_or_ir[i]);
        number_of_loops++;
        // add a comma after the raster names
        if (!raster_names_to_sum.equals("")) {
          raster_names_to_sum = raster_names_to_sum + ",";
        }

        // add a comma after the raster names
        if (!crop_area_to_sum.equals("")) {
          crop_area_to_sum = crop_area_to_sum + ",";
        }
        raster_names_to_sum = raster_names_to_sum + combined_production_name_rf_or_ir[i];

        String crop_area_raster =
            getCropAreaRaster(scenarios, scenarios.crop_name[i], scenarios.rf_or_ir[i]);

        crop_area_to_sum = crop_area_to_sum + crop_area_raster;

        // Add before calling BashScripts.saveAscii in loopOverRainfedAndIrrigated:
        System.out.println(
            "DEBUG: Saving production calculation for "
                + combined_production_name[last_index_of_crop]);
        System.out.println(
            "DEBUG: Using planting month from "
                + combined_planting_month_name_rf_or_ir[last_index_of_crop]);

        BashScripts.saveAscii(
            script_folder,
            scenarios.combined_planting_month_name_rf_or_ir[i],
            scenarios.results_folder[last_index_of_crop]);

        BashScripts.saveAscii(
            script_folder,
            scenarios.combined_days_to_maturity_name_rf_or_ir[i],
            scenarios.results_folder[last_index_of_crop]);

        BashScripts.saveAscii(
            script_folder,
            scenarios.combined_yield_name_rf_or_ir[i],
            scenarios.results_folder[last_index_of_crop]);

        if (export_to_countries && scenarios.make_rasters_comparing_overall_to_historical) {

          // either rainfed or irrigated yield exported by country
          String crop_caps = scenarios.config.getCropNameCaps(scenarios.crop_name[i]);

          // String crop_area_raster =
          //     "LAND_AREA_NO_CROPS"
          //         + "_"
          //         + (scenarios.rf_or_ir[i].equals("RF")
          //             ? "rainfed_cropland"
          //             : "irrigated_cropland");

          // String highres_cropland = "";
          String highres_cropland = scenarios.mask_for_this_snx[i] + "_highres";
          System.out.println("Using as by-country high res cropland raster:");
          System.out.println(highres_cropland);
          // if scenarios.config.physical_parameters.crop_area_type.equals("no_crops") {

          BashScripts.exportToCountries(
              script_folder,
              crop_caps,
              scenarios.combined_yield_name_rf_or_ir[i],
              highres_cropland,
              scenarios.combined_planting_month_name_rf_or_ir[i],
              scenarios.combined_days_to_maturity_name_rf_or_ir[i],
              scenarios.results_folder[i]);
        }
        last_index_of_crop = i;
        // System.exit(1);
      }

      // the above is just a (unfortunately messy) way to add irrigated and rainfed for a given crop

      // assert number_of_loops == 2;

      if (scenarios.calculate_rf_plus_ir_production) {
        // sum rainfed and irrigated yield rasters to the appropriate combined_production_name
        // note that the last_index_of_crop is the last index where the tag matched and we had a new
        // IR or RF specific production.
        // but combined_production_name is not specific to ir or rf
        BashScripts.sumRasters(
            script_folder,
            raster_names_to_sum, // input rasters
            combined_production_name[last_index_of_crop], // output raster
            scenarios.results_folder[last_index_of_crop]);

        BashScripts.saveAscii(
            script_folder,
            combined_production_name[last_index_of_crop],
            scenarios.results_folder[last_index_of_crop]);
      }

      if (scenarios.calculate_average_yield_rf_and_ir) {
        processAverageYield(
            script_folder,
            scenarios,
            combined_production_name, // output rasters
            combined_yield_name, // output rasters
            last_index_of_crop,
            crop_area_to_sum // input rasters
            );
      }
      BashScripts.saveAscii(
          script_folder,
          combined_yield_name[last_index_of_crop],
          scenarios.results_folder[last_index_of_crop]);

      if (scenarios.make_rasters_comparing_overall_to_historical) {
        // overall yield results export by country
        String crop_caps =
            scenarios.config.getCropNameCaps(scenarios.crop_name[last_index_of_crop]);

        String highres_cropland = scenarios.mask_for_this_snx[last_index_of_crop] + "_highres";
        System.out.println("Using as by-country high res cropland raster:");
        System.out.println(highres_cropland);

        BashScripts.exportToCountries(
            script_folder,
            crop_caps,
            combined_yield_name[last_index_of_crop],
            highres_cropland,
            "skip_me", // not available summed over rainfed and irrigated
            "skip_me", // not available summed over rainfed and irrigated
            scenarios.results_folder[last_index_of_crop]);
      }
    } // end loop over scenarios
  } // function sumRainfedAndIrrigated

  /**
   * Appends a string to a StringBuilder, prepending with a comma if the StringBuilder isn't empty.
   */
  private static void appendWithComma(StringBuilder sb, String text) {
    if (sb.length() > 0) sb.append(",");
    sb.append(text);
  } // end appendWithComma function

  /**
   * Processes the average yield, summing the rainfed and irrigated area rasters and calculating the
   * overall yield.
   */
  private static void processAverageYield(
      String script_folder,
      Scenarios scenarios,
      String[] combined_production_name,
      String[] combined_yield_name,
      int last_index_of_crop,
      String crop_area_to_sum)
      throws InterruptedException, IOException {

    BashScripts.sumRasters(
        script_folder,
        crop_area_to_sum,
        scenarios.crop_name[last_index_of_crop] + "_cropland",
        scenarios.results_folder[last_index_of_crop]);

    BashScripts.calculateOverallYield(
        script_folder,
        combined_production_name[last_index_of_crop],
        scenarios.crop_name[last_index_of_crop] + "_cropland",
        combined_yield_name[last_index_of_crop]);

    // If enabled, generate a PNG visualization of the overall yield
    if (scenarios.create_overall_png) {
      BashScripts.createPNG(
          script_folder,
          new String[] {
            scenarios.config.getCropNameCaps(scenarios.crop_name[last_index_of_crop]) + "_yield",
            combined_yield_name[last_index_of_crop]
          },
          scenarios.results_folder[last_index_of_crop]);
    }
  } // end processAverageYield function

  public static void extractYieldForBestPlantingMonthAllYears(
      String script_folder,
      Scenarios scenarios,
      String[]
          best_planting_month_rasters, // array of raster names for best planting month per scenario
      String[][][] raster_names_all_years_wet_or_dry, // [scenario][planting_month][year]
      String[] years,
      String[] scenario_tags,
      String[] results_folder,
      String output_suffix)
      throws InterruptedException, IOException {
    for (int i = 0; i < scenarios.n_scenarios; i++) {
      String best_planting_month_raster = best_planting_month_rasters[i];
      for (int year_index = 0; year_index < scenarios.years.length; year_index++) {
        // Get the yield rasters for all planting months for this scenario and year
        String[] yield_rasters_for_year = new String[scenarios.planting_months.length];
        for (int planting_month_index = 0;
            planting_month_index < scenarios.planting_months.length;
            planting_month_index++) {
          yield_rasters_for_year[planting_month_index] =
              raster_names_all_years_wet_or_dry[i][planting_month_index][year_index];
        }
        // Combine the yield rasters for all planting months into a comma-separated string
        String yield_rasters_for_year_str = String.join(",", yield_rasters_for_year);
        // Define output raster name
        String output_raster_name =
            scenario_tags[i] + "_stablemonth_y" + years[year_index] + output_suffix;
        // System.out.println("RESULTS FOLDER");
        // System.out.println("years[year_index]");
        // System.out.println(year_index);
        // System.out.println(years[year_index]);
        // System.out.println("script_folder");
        // System.out.println(script_folder);
        // System.out.println("best_planting_month_raster");
        // System.out.println(best_planting_month_raster);
        // System.out.println("yield_rasters_for_year_str");
        // System.out.println(yield_rasters_for_year_str);
        // System.out.println("output_raster_name");
        // System.out.println(output_raster_name);
        // System.out.println("results_folder");
        // System.out.println(results_folder[i]);
        // Use the best planting month raster to select the yield raster for each grid cell
        BashScripts.useKeyRasterToMapToValueRaster(
            script_folder,
            best_planting_month_raster, // key raster (best planting month indices)
            yield_rasters_for_year_str, // value rasters (yield rasters for all months in this year)
            output_raster_name);
        // Save as ASCII
        BashScripts.saveAscii(script_folder, output_raster_name, results_folder[i]);
      }
    }
  }

  public static void extractYieldForBestPlantingMonthAllYearsUsingYearSpecificBest(
      String script_folder,
      Scenarios scenarios,
      String[][] year_specific_best_planting_month_rasters, // [scenario][year]
      String[][][] raster_names_all_years_wet_or_dry, // [scenario][planting_month][year]
      String[] years,
      String[] scenario_tags,
      String[] results_folder)
      throws InterruptedException, IOException {

    for (int i = 0; i < scenarios.n_scenarios; i++) {
      for (int year_index = 0; year_index < scenarios.years.length; year_index++) {
        // Get the year-specific best planting month raster for this scenario and year
        String best_planting_month_raster_for_year =
            year_specific_best_planting_month_rasters[i][year_index];

        // Get the yield rasters for all planting months for this scenario and year
        String[] yield_rasters_for_year = new String[scenarios.planting_months.length];
        for (int planting_month_index = 0;
            planting_month_index < scenarios.planting_months.length;
            planting_month_index++) {
          yield_rasters_for_year[planting_month_index] =
              raster_names_all_years_wet_or_dry[i][planting_month_index][year_index];
        }

        // Combine the yield rasters for all planting months into a comma-separated string
        String yield_rasters_for_year_str = String.join(",", yield_rasters_for_year);

        // Define output raster name with a suffix to distinguish from the other method
        String output_raster_name = scenario_tags[i] + "_yearspecific_y" + years[year_index];

        System.out.println("Creating year-specific optimal yield raster: " + output_raster_name);
        System.out.println(
            "Using year-specific best month raster: " + best_planting_month_raster_for_year);

        // Use the best planting month raster to select the yield raster for each grid cell
        BashScripts.useKeyRasterToMapToValueRaster(
            script_folder,
            best_planting_month_raster_for_year, // Year-specific best month
            yield_rasters_for_year_str, // Value rasters (yield rasters for all months)
            output_raster_name);

        // Save as ASCII
        BashScripts.saveAscii(script_folder, output_raster_name, results_folder[i]);
      }
    }
  }
} // class CalculateProduction
