// can be considered the preprocessing rasters and scenario definitions, needed before actually
// running through all the scenarios with dssat (hopefully eventually will have everything debugged
// and then can put initspam here and ensure all works the same...)
// take a list of specifications about a scenario, and generate a csv specifying the actual csv
// which contains information necessary for each individual run of the grid of locations for a given
// cultivar and level of irrigation
// also imports necessary rasters for the current regions needed for processing
// specifications are listed in a config .yaml file
package org.Scenarios;

import java.io.*;
import java.util.*;
import org.R2Useful.*;

public class GenerateScenarios {

  public static void main(String[] args)
      throws InterruptedException, FileNotFoundException, IOException {
    System.out.println("");
    System.out.println("running GenerateScenarios.java ");
    System.out.println("");

    System.out.println("run_script_folder: ");
    System.out.println(args[0]);
    System.out.println("");

    System.out.println("generated scenario scv location: ");
    System.out.println(args[1]);
    System.out.println("");

    System.out.println("scenario generation location: ");
    System.out.println(args[2]);
    System.out.println("");

    System.out.println("run_parameters_csv_folder: ");
    System.out.println(args[3]);
    System.out.println("");

    String run_script_folder = args[0];
    String config_file = args[1];
    String simulation_csv_location = args[2];
    String run_parameters_csv_folder = args[3];
    String default_cultivar_mappings_location =
        run_parameters_csv_folder + "default_cultivar_mappings.csv";

    Config config = Config.importConfigCSV(config_file);

    // System.out.println("config parameters");
    // System.out.println(config.physical_parameters);
    // System.out.println(config.model_configuration);
    BashScripts.setGRASSRegion(run_script_folder, config);

    HashMap<String, String> crop_name_to_short_code_dictionary = getCropCodeMap();

    // Make the results folder if doesnt exist
    File resultsFolder = new File(config.physical_parameters.results_folder);
    if (!resultsFolder.exists()) {
      if (resultsFolder.mkdirs()) {
        System.out.println(
            "Results folder "
                + config.physical_parameters.results_folder
                + " created successfully.");
      } else {
        System.err.println(
            "Error: Failed to create results folder "
                + config.physical_parameters.results_folder
                + ".");
        System.exit(1);
      }
    } else {
      System.out.println(
          "Results folder " + config.physical_parameters.results_folder + " already exists.");
    }

    // Make all the rasters needed for crop models
    for (Config.Crop crop : config.crops) {
      // make by-crop crop area rasters and present-day yield rasters
      String crop_code_caps = config.getCropNameCaps(crop.name);
      BashScripts.initSPAM(run_script_folder, crop_code_caps);
      // make rasters for nitrogen
      BashScripts.makeNitrogenRasters(run_script_folder, crop.name);
    }

    // we only need to make the mask for these countries when we are estimating baseline crop
    // conditions
    // to reflect current planting patterns
    if (!config.physical_parameters.crop_area_type.equals("no_crops")) {
      // make masks for winter wheat dominant countries
      BashScripts.makeCountryMask(
          run_script_folder,
          config.model_configuration.winter_wheat_countries_csv,
          "ALL_CROPS_cropland",
          "winter_wheat_countries_mask");
    }
    // make masks for megaenvironments (regions where certain megaenvironments are)
    BashScripts.makeMegaEnvironmentMasks(run_script_folder);
    BashScripts.makeWeatherFileMask(
        run_script_folder,
        config.physical_parameters.weather_folder,
        config.physical_parameters.region_to_use_n,
        config.physical_parameters.region_to_use_s,
        config.physical_parameters.region_to_use_e,
        config.physical_parameters.region_to_use_w,
        config.physical_parameters.nsres,
        config.physical_parameters.ewres);

    BashScripts.runPythonWeatherFilesChecker(
        run_script_folder, config.physical_parameters.weather_folder);

    generateScenariosCSV(
        run_script_folder,
        config,
        simulation_csv_location,
        default_cultivar_mappings_location,
        crop_name_to_short_code_dictionary);
    System.out.println("");
    System.out.println("done running GenerateScenarios.java ");
    System.out.println("");
  }

  public static HashMap<String, String> getCropCodeMap() {

    HashMap<String, String> crop_name_to_short_code_dictionary = new HashMap<String, String>();
    crop_name_to_short_code_dictionary.put("maize", "mz");
    crop_name_to_short_code_dictionary.put("soybean", "sb");
    crop_name_to_short_code_dictionary.put("rapeseed", "cn");
    crop_name_to_short_code_dictionary.put("wheat", "wh");
    crop_name_to_short_code_dictionary.put("potatoes", "pt");
    crop_name_to_short_code_dictionary.put("rice", "ri");
    return crop_name_to_short_code_dictionary;
  }

  /**
   * Validates that the difference between N-S bounds and E-W bounds are integer multiples of nsres
   * and ewres respectively within a tolerance.
   *
   * @param regionToUseN northern boundary
   * @param regionToUseS southern boundary
   * @param regionToUseE eastern boundary
   * @param regionToUseW western boundary
   * @param nsres north-south resolution
   * @param ewres east-west resolution
   * @throws IllegalArgumentException if the bounds are not valid multiples of the resolutions
   *     within the tolerance
   */
  public static void validateBounds(
      double regionToUseN,
      double regionToUseS,
      double regionToUseE,
      double regionToUseW,
      double nsres,
      double ewres)
      throws IllegalArgumentException {
    double nsDiff = regionToUseN - regionToUseS;
    double ewDiff = regionToUseE - regionToUseW;
    double tolerance = 0.001;

    if (Math.abs(nsDiff % nsres) > tolerance) {
      throw new IllegalArgumentException(
          "The difference between north and south bounds must be an integer multiple of nsres"
              + " within 3 decimal places. Given N: "
              + regionToUseN
              + ", S: "
              + regionToUseS
              + ", nsres: "
              + nsres
              + ".");
    }

    if (Math.abs(ewDiff % ewres) > tolerance) {
      throw new IllegalArgumentException(
          "The difference between east and west bounds must be an integer multiple of ewres within"
              + " 3 decimal places. Given E: "
              + regionToUseE
              + ", W: "
              + regionToUseW
              + ", ewres: "
              + ewres
              + ".");
    }
  }

  public static void generateScenariosCSV(
      String run_script_folder,
      Config config,
      String simulation_csv_location,
      String default_cultivar_mappings_location,
      HashMap<String, String> crop_name_to_short_code_dictionary)
      throws InterruptedException, IOException {
    // create FileWriter instance with file at simulation_csv_location
    FileWriter csvWriter = new FileWriter(simulation_csv_location);
    System.out.println("");
    System.out.println("simulation_csv_location");
    System.out.println(simulation_csv_location);
    // write header to the CSV file
    csvWriter.append(
        "snx_name,co2_level,crop_name,weather_prefix,weather_folder,results_folder,run_descriptor,nitrogen,region_to_use_n,region_to_use_s,region_to_use_e,region_to_use_w,nsres,ewres,mask_for_this_snx,fertilizer_scheme\n");

    // Validate the bounds
    validateBounds(
        config.physical_parameters.region_to_use_n,
        config.physical_parameters.region_to_use_s,
        config.physical_parameters.region_to_use_e,
        config.physical_parameters.region_to_use_w,
        config.physical_parameters.nsres,
        config.physical_parameters.ewres);

    // iterate over all crops
    for (Config.Crop crop : config.crops) {
      if (crop.snx_names.isEmpty()) {
        System.out.println("");
        System.out.println("");
        System.out.println("WARNING: SNX Name Not Specified for " + crop.name + ".");
        System.out.println("         Using megaenvironments to calculate yield");
        System.out.println("");
        String crop_code = crop_name_to_short_code_dictionary.get(crop.name);
        write_csv_default_snx(
            csvWriter,
            config,
            crop,
            crop_code,
            default_cultivar_mappings_location,
            run_script_folder);
      } else {
        write_csv_snx_names_specified(csvWriter, config, crop);
      }
    }

    // after all lines are written, close the writer
    csvWriter.flush();
    csvWriter.close();
  }

  public static void write_csv_default_snx(
      FileWriter csvWriter,
      Config config,
      Config.Crop crop,
      String crop_code,
      String default_cultivar_mappings_location,
      String run_script_folder)
      throws InterruptedException, IOException {
    // Here import the default snx names and get intersection with another dataset
    HashMap<String, String> cultivar_mask_map =
        createCultivarMaskMap(default_cultivar_mappings_location);

    // all the snx_names
    String[] default_snx_names_prefix = getUniqueSNXnames(default_cultivar_mappings_location);

    String prefix = "";

    if (config.physical_parameters.crop_area_type.equals("all")) {
      prefix = "ALL_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("food_crops")) {
      prefix = "ALL_FOOD_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("no_crops")) {
      prefix = "LAND_AREA_NO_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("specific")) {
      prefix = config.getCropNameCaps(crop.name);
    } else {
      System.out.println(
          "Error: make sure crop_area_type is all, food_crops, no_crops, or specific");
      System.exit(1);
    }

    for (String snx_name_prefix : default_snx_names_prefix) {

      String short_code = snx_name_prefix.substring(0, 2);
      // make sure we only process crops that are relevant
      if (!crop_code.equals(short_code)) {
        continue;
      }

      for (String irrigation : config.physical_parameters.irrigation_to_try) {

        String megaEnvMasks = cultivar_mask_map.get(snx_name_prefix);
        String mask_for_this_snx = "mask_for_" + snx_name_prefix + irrigation;

        String nitrogen_raster_or_constant =
            getNitrogenRasterOrConstant(config, crop.name, irrigation);

        // determine crop area raster based on irrigation type
        String crop_area_raster =
            prefix + "_" + (irrigation.equals("RF") ? "rainfed_cropland" : "irrigated_cropland");

        // loop over the default_snx_names and get the intersection
        BashScripts.setIntersectionWithMegaenvironment(
            run_script_folder, crop_area_raster, megaEnvMasks, mask_for_this_snx);
        String snx_name = snx_name_prefix + irrigation;

        // write a new line to the CSV file
        csvWriter.append(
            String.join(
                ",",
                snx_name,
                String.valueOf(config.physical_parameters.co2_level),
                crop.name,
                config.physical_parameters.weather_prefix,
                config.physical_parameters.weather_folder,
                config.physical_parameters.results_folder,
                config.model_configuration.run_descriptor,
                nitrogen_raster_or_constant,
                String.valueOf(config.physical_parameters.region_to_use_n),
                String.valueOf(config.physical_parameters.region_to_use_s),
                String.valueOf(config.physical_parameters.region_to_use_e),
                String.valueOf(config.physical_parameters.region_to_use_w),
                String.valueOf(config.physical_parameters.nsres),
                String.valueOf(config.physical_parameters.ewres),
                mask_for_this_snx,
                crop.fertilizer_scheme));
        csvWriter.append("\n");
      }
    }
  }

  public static void write_csv_snx_names_specified(
      FileWriter csvWriter, Config config, Config.Crop crop) throws IOException {
    // Code to write to CSV with specific snx names
    // This would be similar to your existing code for handling snx names
    // Only difference being you need to pass csvWriter as parameter and not create new

    String prefix = "";
    if (config.physical_parameters.crop_area_type.equals("all")) {
      prefix = "ALL_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("food_crops")) {
      prefix = "ALL_FOOD_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("no_crops")) {
      prefix = "LAND_AREA_NO_CROPS";
    } else if (config.physical_parameters.crop_area_type.equals("specific")) {
      prefix = config.getCropNameCaps(crop.name);
    } else {
      System.out.println(
          "Error: make sure crop_area_type is all, food_crops, no_crops, or specific");
      System.exit(1);
    }

    if (config.physical_parameters.nitrogen == -1) {
      System.out.println("Using default nitrogen raster");
    }
    for (String snx_name_prefix : crop.snx_names) {
      // iterate over all irrigation types
      for (String irrigation : config.physical_parameters.irrigation_to_try) {
        String nitrogen_raster_or_constant =
            getNitrogenRasterOrConstant(config, crop.name, irrigation);

        // create a unique scenario name
        String snx_name = snx_name_prefix + irrigation;
        // determine crop area raster based on irrigation type
        String crop_area_raster =
            prefix + "_" + (irrigation.equals("RF") ? "rainfed_cropland" : "irrigated_cropland");

        // write a new line to the CSV file
        csvWriter.append(
            String.join(
                ",",
                snx_name,
                String.valueOf(config.physical_parameters.co2_level),
                crop.name,
                config.physical_parameters.weather_prefix,
                config.physical_parameters.weather_folder,
                config.physical_parameters.results_folder,
                config.model_configuration.run_descriptor,
                nitrogen_raster_or_constant,
                String.valueOf(config.physical_parameters.region_to_use_n),
                String.valueOf(config.physical_parameters.region_to_use_s),
                String.valueOf(config.physical_parameters.region_to_use_e),
                String.valueOf(config.physical_parameters.region_to_use_w),
                String.valueOf(config.physical_parameters.nsres),
                String.valueOf(config.physical_parameters.ewres),
                crop_area_raster,
                crop.fertilizer_scheme));
        csvWriter.append("\n");
      }
    }
  }

  public static String getNitrogenRasterOrConstant(
      Config config, String crop_name, String irrigation) throws IOException {
    Integer nitrogen_from_user = config.physical_parameters.nitrogen;
    String nitrogen_raster_string = "";
    if (nitrogen_from_user == -1) {
      Config.Crop crop = Config.Crop.getCropByName(config.crops, crop_name);
      // use nitrogen rasters specified in parameters
      if (irrigation.equals("RF")) {
        nitrogen_raster_string = crop.nitrogen_rainfed;

      } else {
        nitrogen_raster_string = crop.nitrogen_irrigated;
      }

    } else {
      // use constant nitrogen
      nitrogen_raster_string = String.valueOf(nitrogen_from_user);
    }
    return nitrogen_raster_string;
  }

  //   public Scenarios(String[] initFileContents) throws InterruptedException, IOException {

  //       // SNX name was not specified.
  //       // So, we are going to use megaenvironment maps to identify the SNX files to use,
  //       // and limit the run to just the region where the crop yields are determined.
  //       // To do so, we loop through the snx names for the current crop type
  //       //
  //       // First we check if the megaenvironment map is
  //
  // setPlantedAreasToThisCultivar(run_script_folder,crop_area_raster,snx_name,cultivar_mask_map,mask_for_this_snx);
  //     }

  // HashMap<String, String> cultivar_mask_map =
  // createCultivarMaskMap(default_cultivar_mappings_location);

  // public static void setPlantedAreasToThisCultivar(
  //   String run_script_folder, // the output raster,mask_for_this_snxs
  //   String crop_area_raster,
  //   String snx_name,
  //   HashMap<String,String> cultivar_mask_map,
  //   String mask_for_this_snx)
  //   throws InterruptedException, IOException {
  //   // get the required megaenvironments and create the appropriate raster with the crop areas
  // only needed for this snx file

  //   String megaEnvMasks = cultivar_mask_map.get(snx_name);

  //
  // BashScripts.getIntersectionWithMegaenvironmentMasks(run_script_folder,crop_area_raster,megaEnvMasks,mask_for_this_snx);
  // }

  // String megaEnvMasks = cultivar_mask_map.get(snx_name);

  // mask_for_this_snx = "mask_for_"+snx_name;

  // BashScripts.getIntersectionWithMegaenvironmentMasks(run_script_folder,crop_area_raster,megaEnvMasks,mask_for_this_snx);

  public static HashMap<String, String> createCultivarMaskMap(String csv_location)
      throws InterruptedException, IOException {
    String[] initFileContents = FunTricks.readTextFileToArray(csv_location);

    // split the string to get the column titles
    String[] column_titles = initFileContents[0].split(",");

    // make sure the column titles for the CSV match what java expects
    try {
      assert column_titles[0].equals("cultivar");
      assert column_titles[1].equals("megaenvironment");
    } catch (AssertionError e) {
      System.out.println(
          "Error: The first line of the csv file must contain the column titles in the code block"
              + " (specified above this error message)"
              + " megaenv");
      System.exit(0); // logging or any action
    }
    System.out.println("");
    System.out.println("");

    HashMap<String, String> cultivar_mask_map = new HashMap<String, String>();

    for (int i = 1; i < initFileContents.length; i++) { // start from 1, to skip the header
      String[] cultivarAndMask = initFileContents[i].split(",");
      String cultivar = cultivarAndMask[0];
      String mask = cultivarAndMask[1];

      // If the map contains the cultivar, append the new mask
      if (cultivar_mask_map.containsKey(cultivar)) {
        String existingMask = cultivar_mask_map.get(cultivar);
        cultivar_mask_map.put(cultivar, existingMask + "," + mask);
      } else {
        // Else add new entry
        cultivar_mask_map.put(cultivar, mask);
      }
    }

    return cultivar_mask_map;
  }

  public static String[] getUniqueSNXnames(String csv_location)
      throws InterruptedException, IOException {
    String[] initFileContents = FunTricks.readTextFileToArray(csv_location);

    // split the string to get the column titles
    String[] column_titles = initFileContents[0].split(",");

    // make sure the column titles for the CSV match what java expects
    try {
      assert column_titles[0].equals("cultivar");
      assert column_titles[1].equals("megaenvironment");
    } catch (AssertionError e) {
      System.out.println(
          "Error: The first line of the csv file must contain the column titles in the code block"
              + " (specified above this error message)"
              + " cultivar map");
      System.exit(0); // logging or any action
    }

    // Use a Set to automatically avoid duplicates
    Set<String> uniqueSNXNames = new HashSet<String>();

    for (int i = 1; i < initFileContents.length; i++) { // start from 1, to skip the header
      String[] cultivarAndMask = initFileContents[i].split(",");
      String cultivar = cultivarAndMask[0];

      // Add the cultivar to the Set
      uniqueSNXNames.add(cultivar);
    }

    // Convert the Set to an Array and return it
    return uniqueSNXNames.toArray(new String[0]);
  }
}
