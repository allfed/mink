/**
 * ScenariosRunner class is responsible for initializing and running a set of defined scenarios. It
 * interfaces with external scripts running GRASS and DSSAT. This class provides utility to run
 * through multiple planting scenarios and keep track of the progress. It also has the capability to
 * save and load progress from a file.
 */
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class ScenariosRunner {

  public static void runScenarios(
      String script_folder, List<PlantingScenario> scenariosAndPMList, Scenarios scenarios)
      throws InterruptedException, IOException {

    // loop through the array of planting months and scenario numbers, running DSSAT

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

        BashScripts.applyWeatherFileMask(
            script_folder,
            scenarios.mask_for_this_snx[scenario_and_pm.scenarioNumber],
            scenarios.mask_for_this_snx[scenario_and_pm.scenarioNumber] + "_wth_masked");

        BashScripts.initGRASS(
            script_folder,
            scenarios.region_to_use_n[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_s[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_e[scenario_and_pm.scenarioNumber],
            scenarios.region_to_use_w[scenario_and_pm.scenarioNumber],
            scenarios.nsres[scenario_and_pm.scenarioNumber],
            scenarios.ewres[scenario_and_pm.scenarioNumber],
            scenarios.planting_months,
            scenarios.mask_for_this_snx[scenario_and_pm.scenarioNumber] + "_wth_masked",
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
      int currentRun =
          scenarios.planting_months.length * scenario_and_pm.scenarioNumber
              + scenario_and_pm.plantingMonth
              + 1;
      int totalRuns = scenarios.planting_months.length * scenarios.snx_name.length;
      double percentageComplete = ((double) (currentRun - 1) / totalRuns) * 100;

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
              + scenario_and_pm.scenarioNumber
              + " which is "
              + (scenario_and_pm.scenarioNumber + 1)
              + " out of "
              + scenarios.n_scenarios
              + " scenarios)"
              + "\n Starting run number "
              + currentRun
              + " out of "
              + totalRuns
              + " total runs. ("
              + String.format("%.1f", percentageComplete)
              + "%)");

      System.out.println("==================");

      BashScripts.runScenario(
          script_folder,
          scenarios.snx_name[scenario_and_pm.scenarioNumber],
          scenarios.dssat_executable,
          scenarios.dssat_folder,
          scenarios.co2_level[scenario_and_pm.scenarioNumber],
          scenarios.crop_name[scenario_and_pm.scenarioNumber],
          scenarios.weather_prefix[scenario_and_pm.scenarioNumber],
          scenarios.weather_folder[scenario_and_pm.scenarioNumber],
          scenarios
              .output_stats_basenames[scenario_and_pm.scenarioNumber][
              scenario_and_pm.plantingMonth],
          scenarios.fertilizer_scheme[scenario_and_pm.scenarioNumber],
          scenarios.n_chunks,
          scenarios.nsres[scenario_and_pm.scenarioNumber],
          scenarios.ewres[scenario_and_pm.scenarioNumber]);

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
    System.out.println("done!");
  }

  public static void saveProgressToFile(
      String scripts_folder, String run_descriptor, int planting_month_index, int scenario_index)
      throws InterruptedException {
    String outputBaseName = scripts_folder + "/interrupted_run_locations/" + run_descriptor;
    String dataOutName = outputBaseName + ".txt";
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
    String dataOutName = outputBaseName + ".txt";
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
} // end class scenariorunner
