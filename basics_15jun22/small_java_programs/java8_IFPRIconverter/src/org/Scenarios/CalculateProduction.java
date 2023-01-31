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
    
    List<List<String>> cultivar_groups_rasters = getCultivarGroups(scenarios,scenarios.best_planting_raster_name);
    List<List<String>> cultivar_groups_croplands = getCultivarGroups(scenarios,scenarios.crop_area_raster);
    List<List<String>> scenario_tags_for_averaging = getCultivarGroups(scenarios,scenarios.scenario_tag_for_averaging_rf_or_ir);
    List<List<String>> scenario_tags_for_production = getCultivarGroups(scenarios,scenarios.scenario_tag_for_production_rf_or_ir);
    List<List<String>> results_folder_groups = getCultivarGroups(scenarios,scenarios.results_folder);

    for (int i = 0; i < cultivar_groups_rasters.size(); i++) {
      List<String> rasters = cultivar_groups_rasters.get(i);

      // skip if no rasters in the cultivar group
      if (rasters.size() == 0) {
        continue;
      }

      // get the crop area and assert they're all the same
      List<String> croplands = cultivar_groups_croplands.get(i);
      assert verifyAllEqualUsingALoop(croplands);

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

      //combine the rasters in the group into a single comma separated string
      String combined_string=getCombinedString(rasters);

      averageAndCalculateProduction(script_folder, combined_string, averaging_tag, production_tag, results_folder, croplands.get(0));
    }
    // average the yield results for the current crop and irrigation grouping

    // sum the rainfed and irrigated yields and save as ascii
    sumRainfedAndIrrigated(script_folder, scenarios);
  } // end CalculateProduction function

  // https://www.baeldung.com/java-list-all-equal
  public boolean verifyAllEqualUsingALoop(List<String> list) {
      for (String s : list) {
          if (!s.equals(list.get(0)))
              return false;
      }
      return true;
  }

  // put all the rasters in a list of lists. The inner list contains groups of rasters with all the cultivars for a crop, separately for rainfed and irrigated
  public static List<List<String>> getCultivarGroups(Scenarios scenarios,String[] property_to_collect) throws InterruptedException, IOException {
      
    String[] rainfed_and_irrigated = {"RF", "IR"};

    // Create a list to hold the lists of scenarios per crop and irrigation type
    List<List<String>> scenariosPerCropAndIrrigation = new ArrayList<>();
    List<String> crops = new ArrayList<String>();
    crops.addAll(scenarios.uniqueCrops);

    // String[] crops = Arrays.asList(scenarios.uniqueCrops.toArray(new String[scenarios.uniqueCrops.size()]));

    // Initialize the list of lists
    for (String rainfed_or_irrigated : rainfed_and_irrigated) {
      for (String crop : scenarios.uniqueCrops) {
        scenariosPerCropAndIrrigation.add(new ArrayList<String>());
      }
    }

    // loop through the unique crops and add the scenarios to the appropriate list
    for (int i = 0; i < scenarios.n_scenarios; i++) {
      String crop = scenarios.crop_name[i];
      String scenario_RF_or_IR = scenarios.scenario_tag_for_averaging_rf_or_ir[i].substring(scenarios.scenario_tag_for_averaging_rf_or_ir[i].length() - 2);

      System.out.println("scenario_RF_or_IR");
      System.out.println(scenario_RF_or_IR);

      assert("IR".equals(scenario_RF_or_IR)||"RF".equals(scenario_RF_or_IR));

      // Get the index of the list that corresponds to the current crop and irrigation type
      // yeah... this is chatGPT's idea to use MATH to make each index unique.
      // sorry.
      //example: crops is [a, b, c] with indices [0, 1, 2]
      //         rf_and_ir is [RF, IR] with indices [0, 1]
      //         to get a unique index, [aRF aIR bRF bIR cRF cIR]
      //                     has index, [0   1   2   3   4   5  ]
      //         we multiply len(rf_and_ir)=2 by crops indices + rf_and_ir
      //         to get unique index
      int index = crops.indexOf(crop) * rainfed_and_irrigated.length + Arrays.asList(rainfed_and_irrigated).indexOf(scenario_RF_or_IR);


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

  public static void averageAndCalculateProduction(String script_folder, String raster_names_to_average, String scenario_tag_for_averaging_rf_or_ir, String scenario_tag_for_production_rf_or_ir, String results_folder, String crop_area_raster) throws InterruptedException, IOException {
    // average all the cultivars for the crop to estimate the crop's overall yield
    BashScripts.runAverageCropsCommand(
        script_folder,
        raster_names_to_average,
        scenario_tag_for_averaging_rf_or_ir,
        results_folder);

    BashScripts.createPNG(
        script_folder,
        scenario_tag_for_averaging_rf_or_ir,
        results_folder);

    // use averaged yields to calculate production for rainfed and irrigated
    // area raster refers to rainfed or irrigated area of a crop, but is not specific to a
    // cultivar (or variety)
    BashScripts.calculateProduction(
        script_folder,
        scenario_tag_for_averaging_rf_or_ir,
        crop_area_raster,
        scenario_tag_for_production_rf_or_ir);
  }

  // public static void averageCrops(String script_folder, Scenarios scenarios)
  //     throws InterruptedException, IOException {
  //   // average the crop yield for a given crop by finding all the scenarios running that crop and
  //   // running a grass gis script which averages the yield

  //   String[] rainfed_and_irrigated = {"RF", "IR"};
  //   for (String rainfed_or_irrigated : rainfed_and_irrigated) {

  //     // run a series of crops of the same species in order to act in aggregate on them
  //     // we also separate out whether it's rainfed or irrigated and run those separately.
  //     for (String crop : scenarios.uniqueCrops) {

  //       // loop through the unique crops

  //       String raster_names_to_average = "";

  //       int last_index_of_crop = 0;
  //       //print raster name
  //       System.out.println("raster_names");
  //       System.out.println(scenarios.raster_names);
  //       // get all the different cultivars for averaging (we calculate the average of all cultivars
  //       // in order to better estimate how the crop (species) does on average)
  //       for (int i = 0; i < scenarios.n_scenarios; i++) {
  //         // System.out.println("index");
  //         // System.out.println(i);
  //         // only consider the scenarios which are running the current crop for averaging
  //         if (!crop.equals(scenarios.crop_name[i])) {
  //           continue;
  //         }
  //         // System.out.println("scenarios.scenario_tag_for_averaging_rf_or_ir[i]");
  //         // System.out.println(scenarios.scenario_tag_for_averaging_rf_or_ir[i]);
  //         // System.out.println("1");

  //         // only average together crops with SNX names ending with either "RF" or "IR"
  //         String scenario_RF_or_IR = scenarios.scenario_tag_for_averaging_rf_or_ir[i].substring(
  //             scenarios.scenario_tag_for_averaging_rf_or_ir[i].length() - 2);

  //         // System.out.println("scenario_RF_or_IR");
  //         // System.out.println(scenario_RF_or_IR);

  //         // System.out.println("rainfed_or_irrigated");
  //         // System.out.println(rainfed_or_irrigated);


  //         if (!rainfed_or_irrigated.equals(scenario_RF_or_IR)) {
  //           continue;
  //         }
  //         // System.out.println("2");

  //         // add a comma after the raster names
  //         if (!raster_names_to_average.equals("")) {
  //           raster_names_to_average = raster_names_to_average + ",";
  //         }
  //         // System.out.println("3");

  //         raster_names_to_average = raster_names_to_average + scenarios.raster_name[i];

  //         last_index_of_crop = i;
  //       }
  //       // System.out.println("raster_names_to_average");
  //       // System.out.println(raster_names_to_average);

  //       // average all the cultivars for the crop to estimate the crop's overall yield
  //       BashScripts.runAverageCropsCommand(
  //           script_folder,
  //           raster_names_to_average,
  //           scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
  //           scenarios.results_folder[last_index_of_crop]);



  //       // System.out.println("scenario_tag_for_averaging_rf_or_ir[last_index_of_crop]");
  //       // System.out.println(scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop]);
  //       BashScripts.createPNG(
  //           script_folder,
  //           scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
  //           scenarios.results_folder[last_index_of_crop]);

  //       // use averaged yields to calculate production for rainfed and irrigated
  //       // area raster refers to rainfed or irrigated area of a crop, but is not specific to a
  //       // cultivar (or variety)
  //       BashScripts.calculateProduction(
  //           script_folder,
  //           scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
  //           scenarios.crop_area_raster[last_index_of_crop],
  //           scenarios.scenario_tag_for_production_rf_or_ir[last_index_of_crop]);

  //     } // end loop over unique crops
  //   } // end looping over irrigation
  // } // end averageCrops

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

        crop_area_to_sum =
            crop_area_to_sum + scenarios.crop_area_raster[i];

        last_index_of_crop = i;
      }

      //sum rainfed and irrigated rasters to the appropriate scenario_tag_for_production
      BashScripts.sumRasters(
          script_folder,
          raster_names_to_sum,
          scenarios.scenario_tag_for_production[last_index_of_crop], // to save here
          scenarios.results_folder[last_index_of_crop]);

      //sum rainfed and irrigated rasters to the appropriate scenario_tag_for_production
      BashScripts.sumRasters(
          script_folder,
          crop_area_to_sum,
          scenarios.crop_name[last_index_of_crop]+"_cropland", // to save here
          scenarios.results_folder[last_index_of_crop]);

      // use production and crop area to calculate average yield rf and ir
      BashScripts.calculateOverallYield(
          script_folder,
          scenarios.scenario_tag_for_production[last_index_of_crop],
          scenarios.crop_name[last_index_of_crop]+"_cropland",
          scenarios.scenario_tag_for_overall_yield[last_index_of_crop]);

      BashScripts.createPNG(
          script_folder,
          scenarios.scenario_tag_for_overall_yield[last_index_of_crop], // to save here
          scenarios.results_folder[last_index_of_crop]);
    } // end loop over scenarios
  } // function sumRainfedAndIrrigated
}  // class CalculateProduction
