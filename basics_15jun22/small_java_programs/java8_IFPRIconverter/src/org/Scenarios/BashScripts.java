// Utility class for running bash scripts directly from given arguments
// Mostly this is needed because grass GIS doesn't have a java api
// However, there's a python API. So we can do that.
// https://grasswiki.osgeo.org/wiki/GRASS_GIS_APIs

// The BashScripts class is a utility class that allows you to run Bash scripts from Java. It
// contains several methods that build a ProcessBuilder object with the necessary arguments and then
// call the callProcess method to execute the script.

// The initGRASS method initializes a GRASS environment with the given region and resolution
// parameters. The runScenario method runs a DSSAT scenario using the given inputs. The
// processResults method processes the results of a scenario run.

// The runAverageCropsCommand method averages the yield rasters for a set of scenarios, and the
// calculateProduction method calculates production for rainfed and irrigated scenarios using the
// averaged yields and the crop area raster.

// basically we always use the same callProcess thing after building up the bash script
// command

package org.Scenarios;

import java.io.*;

public class BashScripts {

  // YIELD CALCULATION SCRIPTS

  // initialize GRASS to the proper region and other initialization tasks
  public static void initGRASS(
      String script_folder,
      String region_to_use_n,
      String region_to_use_s,
      String region_to_use_e,
      String region_to_use_w,
      String nsres,
      String ewres)
      throws InterruptedException, IOException {

    System.out.println("");
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh",
            region_to_use_n,
            region_to_use_s,
            region_to_use_e,
            region_to_use_w,
            nsres,
            ewres);

    callProcess(pb, script_folder);
  } // end initGRASS

  // actually run the full set of commands to calculate yields in every point on the
  // raster. This is the main function which runs DSSAT
  public static void runScenario(
      String script_folder,
      String snx_name,
      String co2_level,
      String crop_name,
      String weather_prefix,
      String weather_folder,
      String yield_result_name)
      throws InterruptedException, IOException {

    // ProcessBuilder pb = new ProcessBuilder("bash", "./mink3daily_run_DSSAT_tile.sh");
    System.out.println("");
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./mink3daily_run_DSSAT_tile.sh",
            "/mnt/data/basics_15jun22/sge_Mink3daily/to_DSSAT/" + yield_result_name,
            weather_folder + "/" + weather_prefix,
            snx_name + ".SNX",
            "threeSplitWithFlowering",
            co2_level,
            crop_name,
            "1",
            "0");

    callProcess(pb, script_folder);
  } // end runScenario

  // process the output from yields into a single text file in the to_DSSAT directory
  public static void processResults(String script_folder, String snx_name, String yield_result_name)
      throws InterruptedException, IOException {

    System.out.println("");
    System.out.println("");
    System.out.println("processResults creates raster with name:");
    System.out.println(yield_result_name);
    System.out.println("");
    System.out.println("");

    // create rasters from the yield per unit area
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash", "./READ_DSSAT_outputs_from_cols_FEW.sh", snx_name, yield_result_name);
    callProcess(pb, script_folder);
  } // end processResults

  // CALCULATING PRODUCTION

  // average the crop yield with a script
  public static void runAverageCropsCommand(
      String script_folder,
      String raster_names_to_average,
      String scenario_tag,
      String results_folder)
      throws InterruptedException, IOException {
    System.out.println("");

    System.out.println("");
    System.out.println("");
    System.out.println("runAverageCropsCommand creates raster with name:");
    System.out.println(scenario_tag);
    System.out.println("");
    System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./average_rasters.sh",
            raster_names_to_average,
            scenario_tag,
            "../../../" + results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end runAverageCropsCommand

  // use averaged yields to calculate production for rainfed and irrigated
  public static void calculateProduction(
      String script_folder,
      String scenario_tag_for_averaging,
      String crop_area_raster,
      String scenario_tag_for_production)
      throws InterruptedException, IOException {

    System.out.println("");
    System.out.println("");
    System.out.println("calculateProduction creates raster with name:");
    System.out.println(scenario_tag_for_production);
    System.out.println("");
    System.out.println("");
    

    // calculate production for rainfed and irrigated
    // scenario_tag_for_production is the raster where the product of yield and area
    // is saved
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./calculate_production.sh",
            scenario_tag_for_averaging,
            crop_area_raster,
            scenario_tag_for_production);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void sumRasters(
      String script_folder, String rasters_to_add, String sum_raster, String results_folder)
      throws InterruptedException, IOException {
    System.out.println("");
    System.out.println("");
    System.out.println("sumRasters creates raster with name:");
    System.out.println(sum_raster);
    System.out.println("");
    System.out.println("");
    

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./sum_production_and_save.sh",
            rasters_to_add,
            sum_raster,
            "../../../" + results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void compositeRaster(
      String script_folder,
      String combined_yield_results,
      String best_yield_raster,
      String best_month_raster,
      String results_folder)
      throws InterruptedException, IOException {

    System.out.println("");
    System.out.println("combined_yield_results");
    System.out.println(combined_yield_results);

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./max_yields.sh",
            combined_yield_results,
            best_yield_raster,
            best_month_raster,
            "../../../" + results_folder);
    System.out.println("");
    System.out.println("");
    System.out.println("compositeRaster creates raster with name:");
    System.out.println("best_yield_raster");
    System.out.println(best_yield_raster);
    System.out.println("best_month_raster");
    System.out.println(best_month_raster);
    System.out.println("");
    System.out.println("");
    


    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

  // UTILITIES

  // creates a raster (which is a 2d grid) and saves it as a png image
  public static void createPNG(String script_folder, String raster_name, String results_folder)
      throws InterruptedException, IOException {

    System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder("bash", "./quick_display.sh", raster_name, results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end createPNG

  // call a generic process
  public static void callProcess(ProcessBuilder pb, String script_folder)
      throws InterruptedException, IOException {
    pb.redirectErrorStream(true);
    pb.directory(new File(script_folder));
    Process process = pb.start();

    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String str = null;
    while ((str = stdInput.readLine()) != null) {
      // displaying the output on the console
      System.out.println(str);
    }

    process.waitFor();
    if (process.exitValue() == 1) {
      System.out.println("ERROR: process exited with value 1 (Error)");
      System.exit(1);
    }
    ;
    assert process.exitValue() != 1;
  } // end callProcess
} // end BashScripts class
