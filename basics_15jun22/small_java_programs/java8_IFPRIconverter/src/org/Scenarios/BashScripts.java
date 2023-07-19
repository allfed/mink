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
import java.util.*;

public class BashScripts {

  // YIELD CALCULATION SCRIPTS

  // initialize spam for the appropriate region (croplands and historical yields)
  public static void initSPAM(
      String script_folder)
      throws InterruptedException, IOException {
    // create the main control list for each month
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./combine_spam_datasets.sh",
            script_folder
        );

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end initSPAM

  // initialize spam for the appropriate region (croplands and historical yields)
  public static void makeCountryMask(
      String script_folder, String countries_csv, String crop_area_raster, String winter_wheat_mask_raster_name)
      throws InterruptedException, IOException {
    // create the main control list for each month
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./make_country_mask.sh",
            countries_csv,
            crop_area_raster,
            winter_wheat_mask_raster_name);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end makeCountryMask

  // initialize spam for the appropriate region (croplands and historical yields)
  public static void setGRASSRegion(
      String script_folder, Config config)
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

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./setGRASSRegion.sh",
            region_to_use);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end makeCountryMask

  public static void makeMegaEnvironmentMasks(
    String script_folder
  ) throws InterruptedException, IOException {
    // import from .pack files all the megaenvironments as rasters
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./unpack_all_me_rasters.sh");

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");

  } // end makeMegaEnvironmentMasks

  public static void makeNitrogenRasters(
    String script_folder
  ) throws InterruptedException, IOException {
    // import from .pack files all the megaenvironments as rasters
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./unpack_all_nitrogen_rasters.sh",
            script_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");

  } // end makeNitrogenRasters

  // initialize GRASS to the proper region and other initialization tasks
  public static void initGRASS(
      String script_folder,
      String region_to_use_n,
      String region_to_use_s,
      String region_to_use_e,
      String region_to_use_w,
      String nsres,
      String ewres,
      String[] months,
      String crop_area_raster,
      String crop_name,
      String run_descriptor,
      String minimum_physical_area,
      String nitrogen)
      throws InterruptedException, IOException {

    // create the main control list for each month
    String main_control_list = "";
    for (int i = 0; i < months.length; i++) {
      main_control_list =
          main_control_list
              + crop_area_raster
              + "\t"
              + crop_name
              + "\t"
              + run_descriptor
              + "\t"
              + nitrogen
              + "\t"
              + months[i]
              + "\n";
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
            "./build_dailystyle_NOCLIMATE_for_DSSAT_05jun14.sh",
            region_to_use,
            main_control_list,
            crop_area_raster,
            minimum_physical_area);

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
      String yield_result_name,
      String fertilizer_scheme)
      throws InterruptedException, IOException {

    // System.out.println("");
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./mink3daily_run_DSSAT_tile.sh",
            "/mnt/data/basics_15jun22/sge_Mink3daily/to_DSSAT/" + yield_result_name,
            weather_folder + "/" + weather_prefix,
            snx_name + ".SNX",
            fertilizer_scheme,
            co2_level,
            crop_name,
            "1",
            "0");
    // System.out.println("");
    // System.out.println("");
    // System.out.println(
    //     "bash"
    //         + " ./mink3daily_run_DSSAT_tile.sh"
    //         + " /mnt/data/basics_15jun22/sge_Mink3daily/to_DSSAT/ "
    //         + yield_result_name
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
    //         + " 1"
    //         + " 0");
    callProcess(pb, script_folder);
  } // end runScenario

  // process the output from yields into a single text file in the to_DSSAT directory
  public static void processResults(
      String script_folder, String output_stats_filename, String yield_result_name)
      throws InterruptedException, IOException {

    // System.out.println("");
    // System.out.println("");
    // System.out.println("processResults creates raster with name:");
    // System.out.println(output_stats_filename);
    // System.out.println("");
    // System.out.println("");

    // create rasters from the yield per unit area
    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./READ_DSSAT_outputs_from_cols_FEW.sh",
            output_stats_filename,
            yield_result_name);

    // System.out.println("about to call the processresults!!");
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
    // System.out.println("");

    // System.out.println("");
    // System.out.println("");
    // System.out.println("runAverageCropsCommand creates raster with name:");
    // System.out.println(scenario_tag);
    // System.out.println("");
    // System.out.println("runAverageCropsCommand uses rasters with names:");
    // System.out.println(raster_names_to_average);
    // System.out.println("");
    // System.out.println("");

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./average_rasters.sh",
            raster_names_to_average,
            scenario_tag,
            "../../../" + results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end runAverageCropsCommand

  // average the crop yield with a script
  public static void getBestOrAverageBasedOnCountryMasks(
      String script_folder,
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

    ProcessBuilder pb =
        new ProcessBuilder(
            "bash",
            "./average_raster_or_find_max_using_mask.sh",
            raster_names_to_average,
            mask_name_for_max,
            raster_result_name,
            "../../../" + results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end runAverageCropsCommand

  // copy ascii for model result and historical and move to results folder
  public static void CreateHistoricalVsModelYieldsASCIIs(
      String script_folder,
      String crop_caps_name,
      String overall_yield_raster,
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
            "./create_historical_vs_model_yields_ASCIIs.sh",
            crop_caps_name,
            overall_yield_raster,
            results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end runAverageCropsCommand

  // use averaged yields to calculate production for rainfed and irrigated
  public static void calculateProduction(
      String script_folder,
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

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

  // the raster is multiplied by the coefficient (a constant float)
  public static void multiplyRasterByCoefficient(
      String script_folder,
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
            "bash",
            "./scale_raster.sh",
            original_raster_name,
            coefficient,
            scaled_raster_name);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

    public static void setIntersectionWithMegaenvironmentMasks(String script_folder, String initial_mask, String megaEnvMasks,String mask_for_this_snx) throws IOException, InterruptedException {
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

        ProcessBuilder pb =
            new ProcessBuilder(
                "bash",
                "./combine_megaenvironment_masks.sh",
                initial_mask,
                megaEnvMasks,
                mask_for_this_snx);
        // System.out.println(script_folder + "../more_GRASS_scripts/universal/");
        callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
    }


  // use averaged yields to calculate production for rainfed and irrigated
  public static void calculateOverallYield(
      String script_folder,
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

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateOverallYields

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void sumRasters(
      String script_folder, String rasters_to_add, String sum_raster, String results_folder)
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
            "./sum_production_and_save.sh",
            rasters_to_add,
            sum_raster,
            "../../../" + results_folder);

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateOverallYields

  // use averaged yields to calculate production for rainfed and irrigated
  // sum raster is the resulting raster
  public static void compositeRaster(
      String script_folder,
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
            best_month_raster,
            "../../../" + results_folder);
    // System.out.println("");
    // System.out.println("");
    // System.out.println("compositeRaster creates raster with name:");
    // System.out.println("best_yield_raster");
    // System.out.println(best_yield_raster);
    // System.out.println("best_month_raster");
    // System.out.println(best_month_raster);
    // System.out.println("");
    // System.out.println("");

    callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  } // end calculateProduction

  // UTILITIES

  public static void createPNG(String script_folder, String[] raster_list, String results_folder)
        throws InterruptedException, IOException {

      List<String> commands = new ArrayList<>();
      commands.add("bash");
      commands.add("./render_all_rasters_same_scale.sh");
      commands.add(results_folder);
      commands.addAll(Arrays.asList(raster_list));
    
      ProcessBuilder pb = new ProcessBuilder(commands);

      callProcess(pb, script_folder + "../more_GRASS_scripts/universal/");
  }
    
  public static void callProcess(ProcessBuilder pb, String script_folder)
        throws InterruptedException, IOException {
      pb.inheritIO();
      pb.redirectErrorStream(true);
      pb.directory(new File(script_folder));
      Process process = pb.start(); // Added semicolon here
      // Create a new thread to handle the error stream
      new Thread(() -> {
          try (BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
              String str = null;
              while ((str = stdError.readLine()) != null) {
                  System.err.println(str);  // print to standard error
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }).start(); // Added semicolon here
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String str = null;
      while ((str = stdInput.readLine()) != null) {
          // displaying the output on the console
          System.out.println(str);
      }

      process.waitFor();
      if (process.exitValue() == 1) {
          StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
          StackTraceElement caller = stackTrace[2];  
          StackTraceElement callerOfCaller = stackTrace[3];  
          List<String> commands = pb.command(); // Added semicolon here
          System.out.println("Error occurred in bash script: " + commands.get(1));
          System.out.println("Error occurred in bash command initiated from: " + caller.getFileName() + ": line " + caller.getLineNumber()); // Fixed concatenation here
          System.out.println("The calling method was invoked from: " + callerOfCaller.getFileName() + ": line " + callerOfCaller.getLineNumber()); // Fixed concatenation here
          System.out.println("ERROR: process exited with value 1 (Error)");
          System.exit(1);
      }

      assert process.exitValue() != 1;
  } // end callProcess

  } // end BashScripts class