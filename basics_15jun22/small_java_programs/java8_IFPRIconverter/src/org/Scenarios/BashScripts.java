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

// The averageRasters method averages the yield rasters for a set of scenarios, and the
// calculateProduction method calculates production for rainfed and irrigated scenarios using the
// averaged yields and the crop area raster.

// basically we always use the same callProcess thing after building up the bash script
// command

package org.Scenarios;

import java.io.*;
import java.util.*;

public class BashScripts {
  // This is a shared flag to signal whether 'q' has been pressed.
  private static volatile boolean shouldExit = false;

  public BashScripts() {
    // Start listening for 'q' key press when an instance is created
    // startKeyListener();
  }

  // TODO this was an attempt at exiting cleanly if q is pressed, but doesn't seem to work...
  private void startKeyListener() {
    new Thread(
            () -> {
              try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                char c;
                while ((c = (char) reader.read()) != 'q') {
                  // Do nothing, just waiting for 'q'
                }
                shouldExit = true; // Set the flag
              } catch (IOException e) {
                e.printStackTrace();
              }
            })
        .start();
  }
  // YIELD CALCULATION SCRIPTS

  // initialize spam for the appropriate region (croplands and historical yields)
  public static void initSPAM(String run_script_folder, String crop_code)
      throws InterruptedException, IOException {
    // create the main control list for each month
    ProcessBuilder pb = new ProcessBuilder("bash", "./process_crop_spam_datasets.sh", crop_code);

    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end initSPAM

  // initialize spam for the appropriate region (croplands and historical yields)
  public static void initSPAMallCrops(String run_script_folder)
      throws InterruptedException, IOException {
    // create the main control list for each month
    ProcessBuilder pb = new ProcessBuilder("bash", "./process_allcrops_spam_datasets.sh");
    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end initSPAM

  // make the mask for winter wheat countries where take max of wheat yield rather than average
  public static void makeCountryMask(
      String run_script_folder,
      String countries_csv,
      String crop_area_raster,
      String winter_wheat_mask_raster_name)
      throws InterruptedException, IOException {
    // create the main control list for each month
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./make_country_mask.sh",
            countries_csv,
            crop_area_raster,
            winter_wheat_mask_raster_name);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end makeCountryMask

  // tell grass which geographic region to create and modify rasters for
  public static void setGRASSRegion(String run_script_folder, Config config)
      throws InterruptedException, IOException {
    // create the main control list for each month
    // create the region to use
    String region_to_use_n = String.valueOf(config.physical_parameters.region_to_use_n);
    String region_to_use_s = String.valueOf(config.physical_parameters.region_to_use_s);
    String region_to_use_e = String.valueOf(config.physical_parameters.region_to_use_e);
    String region_to_use_w = String.valueOf(config.physical_parameters.region_to_use_w);
    String nsres = String.valueOf(config.physical_parameters.nsres);
    String ewres = String.valueOf(config.physical_parameters.ewres);
    String region_to_use =
        "n="
            + region_to_use_n
            + " s="
            + region_to_use_s
            + " e="
            + region_to_use_e
            + " w="
            + region_to_use_w
            + " nsres="
            + nsres
            + " ewres="
            + ewres;

    ProcessBuilder pb = new ProcessBuilder("bash", "./setGRASSRegion.sh", region_to_use);

    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end makeCountryMask

  // create the region
  public static void setOrCreateMapset(String run_script_folder, Config config)
      throws InterruptedException, IOException {
    // create the main control list for each month
    // create the region to use
    String run_descriptor = String.valueOf(config.model_configuration.run_descriptor);

    ProcessBuilder pb = new ProcessBuilder("bash", "./setOrCreateMapset.sh", run_descriptor);

    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end makeCountryMask

  public static void makeMegaEnvironmentMasks(String run_script_folder)
      throws InterruptedException, IOException {
    // import from .pack files all the megaenvironments as rasters
    ProcessBuilder pb = new ProcessBuilder("bash", "./unpack_all_me_rasters.sh");

    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end makeMegaEnvironmentMasks

  public static void makeNitrogenRasters(String run_script_folder, String crop_name)
      throws InterruptedException, IOException {
    // import from .pack files all the megaenvironments as rasters
    ProcessBuilder pb = new ProcessBuilder("bash", "./unpack_all_nitrogen_rasters.sh", crop_name);

    String prerun_script_folder = run_script_folder + "prerun_scripts/";
    callProcess(pb, prerun_script_folder);
  } // end makeNitrogenRasters

  // initialize GRASS to the proper region and other initialization tasks
  public static void initGRASS(
      String run_script_folder,
      String region_to_use_n,
      String region_to_use_s,
      String region_to_use_e,
      String region_to_use_w,
      String nsres,
      String ewres,
      String[] months,
      String crop_area_raster_for_this_snx,
      String crop_name,
      String[] output_stats_basenames_this_scenario,
      String minimum_physical_area,
      String nitrogen)
      throws InterruptedException, IOException {

    // create the main control list for each month
    String main_control_list = "";
    for (int pm = 0; pm < months.length; pm++) {
      main_control_list =
          main_control_list
              + crop_area_raster_for_this_snx
              + "\t"
              + crop_name
              + "\t"
              + output_stats_basenames_this_scenario[pm]
              + "\t"
              + nitrogen
              + "\t"
              + months[pm]
              + "\n";
      // System.out.println("crop_area_raster_for_this_snx");
      // System.out.println(crop_area_raster_for_this_snx);
    }

    // create the region to use
    String region_to_use =
        "n="
            + region_to_use_n
            + " s="
            + region_to_use_s
            + " e="
            + region_to_use_e
            + " w="
            + region_to_use_w
            + " nsres="
            + nsres
            + " ewres="
            + ewres;
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh",
            region_to_use,
            main_control_list,
            crop_area_raster_for_this_snx,
            minimum_physical_area);

    callProcess(pb, run_script_folder);
  } // end initGRASS

  // // THIS VERSION WORKS ENTIRELY WITHOUT THE WRAPPER AND CHUNKING AND IS GOOD IF YOU DON'T WANT
  // TO RUN IN PARALLEL
  // // actually run the full set of commands to calculate yields in every point on the
  // // raster. This is the main function which runs DSSAT
  // public static void runScenario(
  //     String run_script_folder,
  //     String snx_name,
  //     String co2_level,
  //     String crop_name,
  //     String weather_prefix,
  //     String weather_folder,
  //     String yield_result_name,
  //     String fertilizer_scheme)
  //     throws InterruptedException, IOException {

  //   // System.out.println("");
  //   ProcessBuilder pb =
  //       new ProcessBuilder(
  //           "bash",
  //           "./mink3daily_run_DSSAT_tile.sh",
  //           "/mnt/data/basics_15jun22/sge_Mink3daily/to_DSSAT/" + yield_result_name,
  //           weather_folder + "/" + weather_prefix,
  //           snx_name + ".SNX",
  //           fertilizer_scheme,
  //           co2_level,
  //           crop_name,
  //           "1",
  //           "0");
  //   // System.out.println("");
  //   // System.out.println("");
  //   // System.out.println(
  //   //     "bash"
  //   //         + " ./mink3daily_run_DSSAT_tile.sh"
  //   //         + " /mnt/data/basics_15jun22/sge_Mink3daily/to_DSSAT/ "
  //   //         + yield_result_name
  //   //         + " "
  //   //         + weather_folder
  //   //         + "/"
  //   //         + weather_prefix
  //   //         + " "
  //   //         + snx_name
  //   //         + ".SNX"
  //   //         + " "
  //   //         + fertilizer_scheme
  //   //         + " "
  //   //         + co2_level
  //   //         + " "
  //   //         + crop_name
  //   //         + " 1"
  //   //         + " 0");
  //   callProcess(pb, run_script_folder);
  // } // end runScenario

  // actually run the full set of commands to calculate yields in every point on the
  // raster. This is the main function which runs DSSAT
  // THIS IS CURRENTLY RUNNING PARALLEL OPERATION
  public static void runScenario(
      String script_folder,
      String snx_name,
      String dssat_executable,
      String dssat_folder,
      String co2_level,
      String crop_name,
      String weather_prefix,
      String weather_folder,
      String output_stats_basename,
      String fertilizer_scheme,
      String chunks_per_case,
      String lat_res,
      String lon_res)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("");
    // System.out.println(
    //     "bash"
    //         + "./mink3daily_wrapper.sh"
    //         + " "
    //         + "run"
    //         + " "
    //         + output_stats_basename
    //         + " "
    //         + dssat_executable
    //         + " "
    //         + dssat_folder
    //         + " "
    //         + weather_folder
    //         + "/"
    //         + weather_prefix
    //         + " "
    //         + snx_name
    //         + ".SNX"
    //         + " "
    //         + fertilizer_scheme
    //         + " "
    //         + co2_level
    //         + " "
    //         + crop_name
    //         + " "
    //         + chunks_per_case
    //         + " "
    //         + lat_res
    //         + " "
    //         + lon_res);

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./mink3daily_wrapper.sh",
            "run",
            output_stats_basename,
            dssat_executable,
            dssat_folder,
            weather_folder + "/" + weather_prefix,
            snx_name + ".SNX",
            fertilizer_scheme,
            co2_level,
            crop_name,
            chunks_per_case,
            "RUN_DESCRIPTOR_NOT_USED_FOR_RUN_DSSAT", // run descriptor is not used, trying to make
            // it obvious if someone tries
            lat_res,
            lon_res);
    callProcess(pb, script_folder);
  } // end runScenario

  // // process the output from yields into a single text file in the to_DSSAT directory
  // public static void processResults(
  //     String run_script_folder, String output_stats_filename, String yield_result_name)
  //     throws InterruptedException, IOException {

  //   // System.out.println("");
  //   // System.out.println("");
  //   // System.out.println("processResults creates raster with name:");
  //   // System.out.println(output_stats_filename);
  //   // System.out.println("");
  //   // System.out.println("");

  //   // create rasters from the yield per unit area
  //   ProcessBuilder pb =
  //       new ProcessBuilder(
  //           "bash",
  //           "./READ_DSSAT_outputs_from_cols_FEW.sh",
  //           output_stats_filename,
  //           yield_result_name);

  //   // System.out.println("about to call the processresults!!");
  //   callProcess(pb, run_script_folder);
  // } // end processResults

  // process the output from yields into a single text file in the to_DSSAT directory
  public static void assembleResults(
      String script_folder,
      String snx_name,
      String dssat_executable,
      String dssat_folder,
      String co2_level,
      String crop_name,
      String weather_prefix,
      String weather_folder,
      String output_stats_basename,
      String fertilizer_scheme,
      String chunks_per_case,
      String run_descriptor,
      String lat_res,
      String lon_res)
      throws InterruptedException, IOException {

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./mink3daily_wrapper.sh",
            "assemble",
            output_stats_basename + "_STATS.txt",
            dssat_executable,
            dssat_folder,
            weather_folder + "/" + weather_prefix,
            snx_name + ".SNX",
            fertilizer_scheme,
            co2_level,
            crop_name,
            chunks_per_case,
            run_descriptor,
            lat_res,
            lon_res);

    callProcess(pb, script_folder);
  } // end processResults

  // process the output from yields into a single text file in the to_DSSAT directory
  public static void generateRasterFromColumns(
      String run_script_folder, List<String> table_to_build_raster, String rasterName)
      throws InterruptedException, IOException {

    // Define the base directory and file name
    String baseDir = "/tmp/";
    String fileName = "raster_data_" + rasterName + ".txt";
    String filePath = baseDir + fileName;

    // Save the table_to_build_raster to a text file in /tmp directory
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      for (String line : table_to_build_raster) {
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
      // Handle exceptions or errors if needed
    }

    // Call the bash script with the file path
    ProcessBuilder pb =
        new ProcessBuilder("bash", "./generateRasterFromColumns.sh", filePath, rasterName);

    callProcess(pb, run_script_folder);
  }
  // CALCULATING PRODUCTION

  // average the crop yield with a script
  public static void averageRasters(
      String run_script_folder,
      String raster_names_to_average,
      String scenario_tag,
      String minimum_value_to_average,
      String method)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("averageRasters creates raster with name:");
    // System.out.println(scenario_tag);
    // System.out.println("");
    // System.out.println("averageRasters uses rasters with names:");
    // System.out.println(raster_names_to_average);
    // System.out.println("");
    // System.out.println("");

    // TODO: make average_rasters also save the key for which rasters were averaged
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./average_rasters.sh",
            raster_names_to_average,
            minimum_value_to_average,
            method,
            scenario_tag);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end averageRasters

  // average the crop yield with a script
  public static void useKeyRasterToMapToValueRaster(
      String script_folder,
      String key_raster,
      String list_of_value_rasters,
      String output_combined_raster)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("useKeyRasterToMapToValueRaster creates raster with name:");
    // System.out.println(scenario_tag);
    // System.out.println("");
    // System.out.println("useKeyRasterToMapToValueRaster uses rasters with names:");
    // System.out.println(raster_names_to_average);
    // System.out.println("");
    // System.out.println("");

    // TODO: make average_rasters also save the key for which rasters were averaged
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./use_key_raster_to_map_to_value_raster.sh",
            key_raster, // input
            list_of_value_rasters, // input
            output_combined_raster // output
            );

    String grass_script_folder = script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end useKeyRasterToMapToValueRaster

  // average the crop yield with a script
  public static void useKeyRasterAndMaskForGettingMaxOrAverage(
      String script_folder,
      String to_combine_int_rasters,
      String mask,
      String method,
      String findmax_rasters,
      String output_raster)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("useKeyRasterAndMaskForGettingMaxOrAverage creates raster with name:");
    // System.out.println(scenario_tag);
    // System.out.println("");
    // System.out.println("useKeyRasterAndMaskForGettingMaxOrAverage uses rasters with names:");
    // System.out.println(raster_names_to_average);
    // System.out.println("");
    // System.out.println("");

    // TODO: make average_rasters also save the key for which rasters were averaged
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./use_key_raster_and_mask_for_getting_max_or_average.sh",
            to_combine_int_rasters, // input rasters
            mask, // input raster
            method, // string input to specify how to average when averaging
            findmax_rasters, // input rasters
            output_raster // output raster
            );

    String grass_script_folder = script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end useKeyRasterAndMaskForGettingMaxOrAverage

  // average the crop yield with a script
  public static void getBestOrAverageBasedOnCountryMasks(
      String run_script_folder,
      String raster_names_to_average,
      String mask_name_for_max,
      String raster_result_name,
      String results_folder)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("getBestOrAverageBasedOnCountryMasks creates raster with name:");
    // System.out.println(scenario_tag);
    // System.out.println("");
    // System.out.println("getBestOrAverageBasedOnCountryMasks uses rasters with names:");
    // System.out.println(raster_names_to_average);
    // System.out.println("");
    // System.out.println("");

    // TODO: make the key outputed from this script
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./average_raster_or_find_max_using_mask.sh",
            raster_names_to_average, // input
            mask_name_for_max, // input
            raster_result_name // output
            );
    // key_for_which_raster_gave_max_or_average);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end getBestOrAverageBasedOnCountryMasks

  // export raster as ascii file
  public static void saveAscii(
      String run_script_folder, String to_save_raster, String results_folder)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("copy results to "+results_folder);
    // System.out.println(overall_yield_raster+".asc");
    // System.out.println(crop_caps_name+"_yield.asc");
    // System.out.println("");
    // System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder("bash", "./save_ascii.sh", results_folder, to_save_raster);

    String export_script_folder = run_script_folder + "export_scripts/";
    callProcess(pb, export_script_folder);
  } // end saveAscii

  // copy ascii for model result and historical and move to results folder
  public static void exportToCountries(
      String run_script_folder,
      String crop_caps_name,
      String overall_yield_raster,
      String highres_cropland_area,
      String planting_month_raster,
      String days_to_maturity_raster,
      String results_folder)
      throws InterruptedException, IOException {
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("copy results to "+results_folder);
    // System.out.println(overall_yield_raster+".asc");
    // System.out.println(crop_caps_name+"_yield.asc");
    // System.out.println("");
    // System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./export_by_country_data.sh",
            crop_caps_name,
            overall_yield_raster,
            highres_cropland_area,
            planting_month_raster,
            days_to_maturity_raster,
            results_folder);

    String export_script_folder = run_script_folder + "export_scripts/";
    callProcess(pb, export_script_folder);
  } // end exportToCountries

  // use averaged yields to calculate production for rainfed and irrigated
  public static void calculateProduction(
      String run_script_folder,
      String scenario_tag_for_averaging,
      String crop_area_raster,
      String scenario_tag_for_production)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("");
    // System.out.println("calculateProduction creates raster with name:");
    // System.out.println(scenario_tag_for_production);
    // System.out.println("");
    // System.out.println("");
    // System.out.println("scenario_tag_for_averaging");
    // System.out.println(scenario_tag_for_averaging);
    // System.out.println("crop_area_raster");
    // System.out.println(crop_area_raster);
    // System.out.println("scenario_tag_for_production");
    // System.out.println(scenario_tag_for_production);

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

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateProduction

  // the raster is multiplied by the coefficient (a constant float)
  public static void setNegativeValuesToZero(String run_script_folder, String raster_name_to_cap)
      throws InterruptedException, IOException {

    ProcessBuilder pb =
        new ProcessBuilder("bash", "./set_negative_values_to_zero.sh", raster_name_to_cap);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateProduction

  // the raster is multiplied by the coefficient (a constant float)
  public static void multiplyRasterByCoefficient(
      String run_script_folder,
      String original_raster_name,
      String coefficient,
      String scaled_raster_name)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("");
    // System.out.println("multiplyRasterByCoefficient creates raster with name:");
    // System.out.println(scaled_raster_name);
    // System.out.println("");
    // System.out.println("");
    // System.out.println("original_raster_name");
    // System.out.println(original_raster_name);
    // System.out.println("coefficient");
    // System.out.println(coefficient);
    // System.out.println("scaled_raster_name");
    // System.out.println(scaled_raster_name);

    // calculate wet weight at harvest by multiplying by appropriate coefficient
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash", "./scale_raster.sh", original_raster_name, coefficient, scaled_raster_name);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateProduction

  public static void setIntersectionWithMegaenvironment(
      String run_script_folder, String initial_mask, String megaEnvMasks, String mask_for_this_snx)
      throws IOException, InterruptedException {
    // Takes a subset of the initial mask which contains nonnull megaenvironments,
    // and returns the resulting mask.

    System.out.println("");
    System.out.println("");
    System.out.println("getIntersectionWithMegaenvironmentMasks creates raster with name:");
    System.out.println(mask_for_this_snx);
    System.out.println("");
    System.out.println("");
    System.out.println("initial_mask");
    System.out.println(initial_mask);
    System.out.println("list_of_megaenvironment_masks");
    System.out.println(megaEnvMasks);
    System.out.println("final_mask");
    System.out.println(mask_for_this_snx);

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./combine_megaenvironment_masks.sh",
            initial_mask,
            megaEnvMasks,
            mask_for_this_snx);
    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  }

  public static void applyWeatherFileMask(
      String run_script_folder,
      String original_mask,
      String raster_masked_by_available_weather_files)
      throws IOException, InterruptedException {
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "apply_weather_file_mask.sh",
            original_mask,
            raster_masked_by_available_weather_files);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  }

  public static void makeWeatherFileMask(
      String run_script_folder,
      String weather_folder,
      double n,
      double s,
      double e,
      double w,
      double nsres,
      double ewres)
      throws IOException, InterruptedException {
    // Takes a subset of the initial mask which contains nonnull megaenvironments,
    // and returns the resulting mask.

    // System.out.println("");
    // System.out.println("");
    // System.out.println("getIntersectionWithMegaenvironmentMasks creates raster with name:");
    // System.out.println(mask_for_this_snx);
    // System.out.println("");
    // System.out.println("");
    // System.out.println("initial_mask");
    // System.out.println(initial_mask);
    // System.out.println("list_of_megaenvironment_masks");
    // System.out.println(megaEnvMasks);
    // System.out.println("final_mask");
    // System.out.println(mask_for_this_snx);
    // System.out.println(
    //     "python3"
    //         + " "
    //         + "make_weather_file_mask.py"
    //         + " "
    //         + weather_folder
    //         + " "
    //         + String.valueOf(n)
    //         + " "
    //         + String.valueOf(s)
    //         + " "
    //         + String.valueOf(e)
    //         + " "
    //         + String.valueOf(w)
    //         + " "
    //         + String.valueOf(nsres)
    //         + " "
    //         + String.valueOf(ewres));

    ProcessBuilder pb =
        new ProcessBuilder(
            "python3",
            "make_weather_file_mask.py",
            "/mnt/data/" + weather_folder,
            String.valueOf(n),
            String.valueOf(s),
            String.valueOf(e),
            String.valueOf(w),
            String.valueOf(nsres),
            String.valueOf(ewres));
    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  }

  // use averaged yields to calculate production for rainfed and irrigated
  public static void calculateOverallYield(
      String run_script_folder,
      String scenario_tag_for_production,
      String crop_area_raster,
      String scenario_tag_for_overall_yields)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("");
    // System.out.println("calculateOverallYields creates raster with name:");
    // System.out.println(scenario_tag_for_overall_yields);
    // System.out.println("");
    // System.out.println("");

    // calculate production for rainfed and irrigated
    // scenario_tag_for_production is the raster where the product of yield and area
    // is saved
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./divide_rasters.sh",
            scenario_tag_for_production,
            crop_area_raster,
            scenario_tag_for_overall_yields);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateOverallYields

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void sumRasters(
      String run_script_folder, String rasters_to_add, String sum_raster, String results_folder)
      throws InterruptedException, IOException {
    // System.out.println("");
    // System.out.println("");
    // System.out.println("sumRasters creates raster with name:");
    // System.out.println(sum_raster);
    // System.out.println("");
    // System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./sum_production.sh",
            rasters_to_add,
            sum_raster,
            "../../../" + results_folder);

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateOverallYields

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void compositeRaster(
      String run_script_folder,
      String combined_yield_results,
      String best_yield_raster,
      String best_month_raster,
      String results_folder)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("combined_yield_results");
    // System.out.println(combined_yield_results);
    // System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./max_yields.sh",
            combined_yield_results,
            best_yield_raster,
            best_month_raster);
    // System.out.println("");
    // System.out.println("");
    // System.out.println("compositeRaster creates raster with name:");
    // System.out.println("best_yield_raster");
    // System.out.println(best_yield_raster);
    // System.out.println("best_month_raster");
    // System.out.println(best_month_raster);
    // System.out.println("");
    // System.out.println("");

    String grass_script_folder = run_script_folder + "../more_GRASS_scripts/universal/";
    callProcess(pb, grass_script_folder);
  } // end calculateProduction

  // UTILITIES

  public static void createPNG(
      String run_script_folder, String[] raster_list, String results_folder)
      throws InterruptedException, IOException {

    List<String> commands = new ArrayList<>();
    commands.add("bash");
    commands.add("./render_all_rasters_same_scale.sh");
    commands.add(results_folder);
    commands.addAll(Arrays.asList(raster_list));

    ProcessBuilder pb = new ProcessBuilder(commands);

    String export_script_folder = run_script_folder + "export_scripts/";
    callProcess(pb, export_script_folder);
  }

  public static void callProcess(ProcessBuilder pb, String run_script_folder)
      throws InterruptedException, IOException {
    pb.inheritIO();
    // pb.redirectErrorStream(true);
    pb.directory(new File(run_script_folder));
    Process process = pb.start(); // Added semicolon here

    // Create a new thread to handle the error stream
    new Thread(
            () -> {
              try (BufferedReader stdError =
                  new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String str = null;
                while ((str = stdError.readLine()) != null) {
                  System.err.println(str); // print to standard error
                }
              } catch (IOException e) {
                e.printStackTrace();
              }
            })
        .start();

    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

    String str = null;
    while ((str = stdInput.readLine()) != null) {
      // displaying the output on the console
      System.out.println(str);
    }

    if (shouldExit) {
      System.out.println(
          "q was pressed! Exiting after finishing the current process. run with continueDSSAT flag"
              + " to continue where you left off.");
      System.exit(0);
    }

    process.waitFor();
    if (process.exitValue() == 1) {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      StackTraceElement caller = stackTrace[2];
      StackTraceElement callerOfCaller = stackTrace[3];
      List<String> commands = pb.command(); // Added semicolon here
      System.out.println("Error occurred in bash script: " + commands.get(1));
      System.out.println(
          "Error occurred in bash command initiated from: "
              + caller.getFileName()
              + ": line "
              + caller.getLineNumber()); // Fixed concatenation here
      System.out.println(
          "The calling method was invoked from: "
              + callerOfCaller.getFileName()
              + ": line "
              + callerOfCaller.getLineNumber()); // Fixed concatenation here
      System.out.println("ERROR: process exited with value 1 (Error)");
      System.exit(1);
    }

    assert process.exitValue() != 1;
  } // end callProcess
} // end BashScripts class
