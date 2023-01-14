// This file loads the scenarios from a CSV and runs the scenarios
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class CalculateProduction {

  public CalculateProduction(String script_folder, Scenarios scenarios)
      throws InterruptedException, FileNotFoundException, IOException {

    // average yield results for each crop
    averageCrops(script_folder, scenarios);

    // sum the rainfed and irrigated yields and save as ascii
    sumRainfedAndIrrigated(script_folder, scenarios);
  }

  public static void averageCrops(String script_folder, Scenarios scenarios)
      throws InterruptedException, IOException {
    // average the crop yield for a given crop by finding all the scenarios running that crop and
    // running a grass gis script which averages the yield

    System.out.println("");
    System.out.println("MUST BE BEFORE THIS?");
    System.out.println("");
    String[] rainfed_and_irrigated = {"RF", "IR"};
    for (String rainfed_or_irrigated : rainfed_and_irrigated) {

      // run a series of crops of the same species in order to act in aggregate on them
      // we also separate out whether it's rainfed or irrigated and run those separately.
      for (String crop : scenarios.uniqueCrops) {

        // loop through the unique crops

        String raster_names_to_average = "";

        int last_index_of_crop = 0;

        // get all the different cultivars for averaging (we calculate the average of all cultivars
        // in order to better estimate how the crop (species) does on average)
        for (int i = 0; i < scenarios.n_scenarios; i++) {

          // only consider the scenarios which are running the current crop for averaging
          if (!crop.equals(scenarios.crop_name[i])) {
            continue;
          }

          // only average together crops with SNX names ending with either "RF" or "IR"
          if (! rainfed_or_irrigated.equals(scenarios.scenario_tag_for_averaging_rf_or_ir[i].substring(
                  scenarios.scenario_tag_for_averaging_rf_or_ir[i].length() - 2))) {
            continue;
          }

          // add a comma after the raster names
          if (!raster_names_to_average.equals("") ){
            raster_names_to_average = raster_names_to_average + ",";
          }

          raster_names_to_average = raster_names_to_average + scenarios.raster_name[i];

          last_index_of_crop = i;
        }

        // average all the cultivars for the crop to estimate the crop's overall yield
        BashScripts.runAverageCropsCommand(
            script_folder,
            raster_names_to_average,
            scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
            scenarios.results_folder[last_index_of_crop]);

        System.out.println("scenario_tag_for_averaging_rf_or_ir[last_index_of_crop]");
        System.out.println(scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop]);
        BashScripts.createPNG(
            script_folder,
            scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
            scenarios.results_folder[last_index_of_crop]);

        // use averaged yields to calculate production for rainfed and irrigated
        // area raster refers to rainfed or irrigated area of a crop, but is not specific to a
        // cultivar (or variety)
        BashScripts.calculateProduction(
            script_folder,
            scenarios.scenario_tag_for_averaging_rf_or_ir[last_index_of_crop],
            scenarios.crop_area_raster[last_index_of_crop],
            scenarios.scenario_tag_for_production_rf_or_ir[last_index_of_crop]);
      } // end loop over unique crops
    } // end looping over irrigation
  }// end averageCrops



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
      System.out.println("tag");
      System.out.println(tag);
      // loop through the unique crops

      String raster_names_to_sum = "";

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
        if (!raster_names_to_sum.equals("") ){
          raster_names_to_sum = raster_names_to_sum + ",";
        }

        raster_names_to_sum =
            raster_names_to_sum + scenarios.scenario_tag_for_production_rf_or_ir[i];

        last_index_of_crop = i;
      }


      BashScripts.sumRasters(
          script_folder,
          raster_names_to_sum,
          scenarios.scenario_tag_for_production[last_index_of_crop], // to save here
          scenarios.results_folder[last_index_of_crop]);

      BashScripts.createPNG(
          script_folder,
          scenarios.scenario_tag_for_production[last_index_of_crop], // to save here
          scenarios.results_folder[last_index_of_crop]);
    } // end sumRainfedAndIrrigated

  } // class CalculateProduction
}
