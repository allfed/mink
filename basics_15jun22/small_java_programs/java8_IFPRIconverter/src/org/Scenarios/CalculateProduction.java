// This file loads the scenarios from a CSV and runs the scenarios
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class CalculateProduction {

  public CalculateProduction(String script_folder, Scenarios scenarios)
      throws InterruptedException, FileNotFoundException, IOException {

    // average yield results for each crop and irrigation grouping (regardless of cultivar)

    // group together the rasters of the cultivars to average

    List<List<String>> cultivar_groups_rasters =
        getCultivarGroups(scenarios, scenarios.best_planting_raster_name);
    List<List<String>> cultivar_groups_croplands =
        getCultivarGroups(scenarios, scenarios.mask_for_this_snx);
    List<List<String>> scenario_tags_for_averaging =
        getCultivarGroups(scenarios, scenarios.scenario_tag_for_averaging_rf_or_ir);
    List<List<String>> scenario_tags_for_production =
        getCultivarGroups(scenarios, scenarios.scenario_tag_for_production_rf_or_ir);
    List<List<String>> results_folder_groups =
        getCultivarGroups(scenarios, scenarios.results_folder);
    List<List<String>> crop_groups =
        getCultivarGroups(scenarios, scenarios.crop_name);


    for (int i = 0; i < cultivar_groups_rasters.size(); i++) {
      List<String> rasters = cultivar_groups_rasters.get(i);

      // skip if no rasters in the cultivar group
      if (rasters.size() == 0) {
        continue;
      }


      // get the crop area and assert they're all the same
      // NOTE: different cultivars now can have different croplands
      List<String> croplands = cultivar_groups_croplands.get(i);
      // assert verifyAllEqualUsingALoop(croplands);
      String cropland = croplands.get(0);

      // get the results folders and assert they're all the same
      List<String> crops = crop_groups.get(i);
      assert verifyAllEqualUsingALoop(crops);
      String crop = crops.get(0);

      // get the average yield tag and assert they're all the same
      List<String> averaging_tags = scenario_tags_for_averaging.get(i);
      assert verifyAllEqualUsingALoop(averaging_tags);
      String averaging_tag = averaging_tags.get(0);

      // get the production tag and assert they're all the same
      List<String> production_tags = scenario_tags_for_production.get(i);
      assert verifyAllEqualUsingALoop(production_tags);
      String production_tag = production_tags.get(0);

      // get the results folders and assert they're all the same
      List<String> results_folders = results_folder_groups.get(i);
      assert verifyAllEqualUsingALoop(results_folders);
      String results_folder = results_folders.get(0);

      // combine the rasters in the group into a single comma separated string
      String combined_string = getCombinedString(rasters);

      averageAndCalculateProduction(
          crop,
          scenarios.create_average_png,
          scenarios.calculate_rf_or_ir_specific_average_yield,
          scenarios.calculate_rf_or_ir_specific_production,
          script_folder,
          combined_string,
          averaging_tag,
          production_tag,
          results_folder,
          cropland);
    }
    // average the yield results for the current crop and irrigation grouping

    // sum the rainfed and irrigated yields and save as ascii
    sumRainfedAndIrrigated(script_folder, scenarios);
  } // end CalculateProduction function

  // https://www.baeldung.com/java-list-all-equal
  public boolean verifyAllEqualUsingALoop(List<String> list) {
    for (String s : list) {
      if (!s.equals(list.get(0))) return false;
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

    // String[] crops = Arrays.asList(scenarios.uniqueCrops.toArray(new
    // String[scenarios.uniqueCrops.size()]));

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
          scenarios.scenario_tag_for_averaging_rf_or_ir[i].substring(
              scenarios.scenario_tag_for_averaging_rf_or_ir[i].length() - 2);

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

  public static String getCombinedString(List<String> cultivar_group)
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

  public static void averageAndCalculateProduction(
      String crop,
      boolean create_average_png,
      boolean calculate_rf_or_ir_specific_average_yield,
      boolean calculate_rf_or_ir_specific_production,
      String script_folder,
      String raster_names_to_average,
      String scenario_tag_for_averaging_rf_or_ir,
      String scenario_tag_for_production_rf_or_ir,
      String results_folder,
      String mask_for_this_snx)
      throws InterruptedException, IOException {

    // average all the cultivars for the crop to estimate the crop's overall yield
    // this is specific to whether IR or RF
    // UNLESS this is soybean.. in which case get the best cultivar's (maturity group's) yield
    if(calculate_rf_or_ir_specific_average_yield){
      // System.out.println("crop");
      // System.out.println(crop);
      if(crop.equals("soybean")){
        BashScripts.compositeRaster(
            script_folder,
            raster_names_to_average,
            scenario_tag_for_averaging_rf_or_ir,
            "deleteme_best_maturity_group_soybean",
            results_folder);
      } else {
        BashScripts.runAverageCropsCommand(
            script_folder,
            raster_names_to_average,
            scenario_tag_for_averaging_rf_or_ir,
            results_folder);
      }
    }

    // create a png of the averaged yield raster if the average_png is true
    if (create_average_png) {
      BashScripts.createPNG(
          script_folder,
          scenario_tag_for_averaging_rf_or_ir,
          results_folder);
    }

    // use averaged yields to calculate production for either rainfed or irrigated yield.
    // area raster refers to rainfed or irrigated area of a crop, but is not specific to a
    // cultivar (or variety)
    if(calculate_rf_or_ir_specific_production){

      BashScripts.calculateProduction(
          script_folder,
          scenario_tag_for_averaging_rf_or_ir,
          mask_for_this_snx,
          scenario_tag_for_production_rf_or_ir);
    }
  }

  public static void sumRainfedAndIrrigated(String script_folder, Scenarios scenarios)
      throws InterruptedException, IOException {
    // sums the averaged crop production rasters, if there's an RF tag and associated IR tag. Saves
    // the results as an .asc file.

    // rainfed and irrigated are identified from snx_name last 2 letters (either "RF" or "IR"). this
    // will be used to specify for the first 2 letters appended to the
    // scenarios.scenario_tag_for_production

    // run a series of crops of the same species in order to act in aggregate on them
    // we are aggregating over rainfed and irrigated production (if rainfed and irrigated exist for
    // the same crop)

    // Initialize an empty map to remember completed rf_or_ir tags
    ArrayList<String> completed_tags = new ArrayList<>();

    for (String tag : scenarios.unique_scenario_tags_for_production) {
      // loop through the unique crops

      String raster_names_to_sum = "";
      String crop_area_to_sum = "";

      int last_index_of_crop = 0;

      for (int i = 0; i < scenarios.n_scenarios; i++) {

        // only consider the scenarios which are running the current crop for averaging
        // we only need to use the results from a given scenario tag once
        if (!tag.equals(scenarios.scenario_tag_for_production[i])) {
          continue;
        }

        // If the map does not already contain an entry for this scenario type, continue
        // this allows us to find the data for a scenario tag
        if (completed_tags.contains(scenarios.scenario_tag_for_production_rf_or_ir[i])) {
          continue;
        }
        completed_tags.add(scenarios.scenario_tag_for_production_rf_or_ir[i]);

        // add a comma after the raster names
        if (!raster_names_to_sum.equals("")) {
          raster_names_to_sum = raster_names_to_sum + ",";
        }

        // add a comma after the raster names
        if (!crop_area_to_sum.equals("")) {
          crop_area_to_sum = crop_area_to_sum + ",";
        }

        raster_names_to_sum =
            raster_names_to_sum + scenarios.scenario_tag_for_production_rf_or_ir[i];

        crop_area_to_sum = crop_area_to_sum + scenarios.mask_for_this_snx[i];

        last_index_of_crop = i;
      }

      if(scenarios.calculate_rf_plus_ir_production){
      // sum rainfed and irrigated yield rasters to the appropriate scenario_tag_for_production
        BashScripts.sumRasters(
            script_folder,
            raster_names_to_sum,
            scenarios.scenario_tag_for_production[last_index_of_crop], // to save here
            scenarios.results_folder[last_index_of_crop]);
      }

      if(scenarios.calculate_average_yield_rf_and_ir) {

        // sum rainfed and irrigated area rasters to the appropriate cropland
        BashScripts.sumRasters(
            script_folder,
            crop_area_to_sum,
            scenarios.crop_name[last_index_of_crop] + "_cropland", // to save here
            scenarios.results_folder[last_index_of_crop]);

        // use production and crop area to calculate average yield rf and ir
        // (just divide them as average yield is total production / total area)
        BashScripts.calculateOverallYield(
            script_folder,
            scenarios.scenario_tag_for_production[last_index_of_crop],
            scenarios.crop_name[last_index_of_crop] + "_cropland",
            scenarios.scenario_tag_for_overall_yield[last_index_of_crop]);

      }


      // process the overall yield rasters to .asc files
      if (scenarios.make_rasters_comparing_overall_to_historical) {
        String crop_caps = scenarios.crop_lower_to_caps_dictionary.get(scenarios.crop_name[last_index_of_crop]);

        // System.out.println("crop_caps");
        // System.out.println(crop_caps);

        BashScripts.CreateHistoricalVsModelYieldsASCIIs(
          script_folder,
          crop_caps,
          scenarios.scenario_tag_for_overall_yield[last_index_of_crop], 
          scenarios.results_folder[last_index_of_crop]);  
      }

      // create a png of the overall yield if create_overall_png is true
      if (scenarios.create_overall_png) {
        BashScripts.createPNG(
          script_folder,
          scenarios.scenario_tag_for_overall_yield[last_index_of_crop], // to save here
          scenarios.results_folder[last_index_of_crop]);  
      }

    } // end loop over scenarios
  } // function sumRainfedAndIrrigated
} // class CalculateProduction