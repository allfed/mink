package org.Scenarios;

import java.io.*;
import java.util.*;

public class Config {
  public ModelConfiguration model_configuration;
  public PhysicalParameters physical_parameters;
  public List<Crop> crops;
  public HashMap<String, String> crop_lower_to_caps_dictionary;

  public Config() {
    this.model_configuration = new ModelConfiguration();
    this.physical_parameters = new PhysicalParameters();
    this.crops = new ArrayList<>();

    this.crop_lower_to_caps_dictionary = new HashMap<String, String>();
    this.crop_lower_to_caps_dictionary.put("maize", "MAIZ");
    this.crop_lower_to_caps_dictionary.put("soybeans", "SOYB");
    this.crop_lower_to_caps_dictionary.put("rapeseed", "RAPE");
    this.crop_lower_to_caps_dictionary.put("wheat", "WHEA");
    this.crop_lower_to_caps_dictionary.put("potato", "POTA");
  }

  public String getCropNameCaps(String cropName) {
    String code = crop_lower_to_caps_dictionary.get(cropName);
    if (code == null) {
      throw new IllegalArgumentException(
          "ERROR: No value found for crop name: "
              + cropName
              + ". \n Available options:"
              + crop_lower_to_caps_dictionary.keySet().toString());
    }
    return code;
  }

  @Override
  public String toString() {
    return "Config{"
        + "model_configuration="
        + model_configuration
        + ", physical_parameters="
        + physical_parameters
        + ", crops="
        + crops
        + '}';
  }

  public class ModelConfiguration {
    public String run_descriptor;
    public boolean run_crop_model;
    public boolean process_results;
    public boolean calculate_as_wet_weight;
    public boolean average_yields;
    public boolean calculate_each_year_best_month;
    public boolean find_best_yields;
    public boolean calculate_rf_or_ir_specific_average_yield;
    public boolean calculate_rf_or_ir_specific_production;
    public boolean calculate_rf_plus_ir_production;
    public boolean calculate_average_yield_rf_and_ir;
    public boolean make_rasters_comparing_overall_to_historical;
    public boolean create_each_year_png;
    public boolean create_average_png;
    public boolean create_overall_png;
    public String winter_wheat_countries_csv;

    @Override
    public String toString() {
      return "ModelConfiguration{"
          + "run_descriptor='"
          + run_descriptor
          + '\''
          + ", run_crop_model="
          + run_crop_model
          + ", process_results="
          + process_results
          + ", calculate_as_wet_weight="
          + calculate_as_wet_weight
          + ", average_yields="
          + average_yields
          + ", calculate_each_year_best_month="
          + calculate_each_year_best_month
          + ", find_best_yields="
          + find_best_yields
          + ", calculate_rf_or_ir_specific_average_yield="
          + calculate_rf_or_ir_specific_average_yield
          + ", calculate_rf_or_ir_specific_production="
          + calculate_rf_or_ir_specific_production
          + ", calculate_rf_plus_ir_production="
          + calculate_rf_plus_ir_production
          + ", calculate_average_yield_rf_and_ir="
          + calculate_average_yield_rf_and_ir
          + ", make_rasters_comparing_overall_to_historical="
          + make_rasters_comparing_overall_to_historical
          + ", create_each_year_png="
          + create_each_year_png
          + ", create_average_png="
          + create_average_png
          + ", create_overall_png="
          + create_overall_png
          + ", winter_wheat_countries_csv="
          + winter_wheat_countries_csv
          + '}';
    }
  }

  public class PhysicalParameters {
    public double region_to_use_n;
    public double region_to_use_s;
    public double region_to_use_e;
    public double region_to_use_w;
    public double nsres;
    public double ewres;
    public int co2_level;
    public int nitrogen = -1;
    public String real_or_happy;
    public String all_or_crop_specific;
    public List<String> irrigation_to_try;
    public String weather_prefix;
    public String weather_folder;
    public String results_folder;
    public List<String> planting_months;
    public List<String> years;
    public String minimum_physical_area;

    @Override
    public String toString() {
      return "PhysicalParameters{"
          + "region_to_use_n="
          + region_to_use_n
          + ", region_to_use_s="
          + region_to_use_s
          + ", region_to_use_e="
          + region_to_use_e
          + ", region_to_use_w="
          + region_to_use_w
          + ", nsres="
          + nsres
          + ", ewres="
          + ewres
          + ", co2_level="
          + co2_level
          + ", nitrogen="
          + (nitrogen == -1 ? "default" : nitrogen)
          + ", real_or_happy='"
          + real_or_happy
          + '\''
          + ", all_or_crop_specific='"
          + all_or_crop_specific
          + '\''
          + ", irrigation_to_try="
          + irrigation_to_try
          + ", weather_prefix='"
          + weather_prefix
          + '\''
          + ", weather_folder='"
          + weather_folder
          + '\''
          + ", results_folder='"
          + results_folder
          + '\''
          + ", planting_months="
          + planting_months
          + ", years="
          + years
          + ", minimum_physical_area="
          + minimum_physical_area
          + '}';
    }
  }

  public static class Crop {
    public String name;
    public List<String> snx_names;
    public String fertilizer_scheme;
    public String nitrogen_rainfed;
    public String nitrogen_irrigated;

    public Crop() {
      snx_names = new ArrayList<String>();
    }

    public static Crop getCropByName(List<Crop> crops, String cropName) {
      for (Crop crop : crops) {
        if (crop.name != null && crop.name.equals(cropName)) {
          return crop; // Returns the crop with the matching name
        }
      }
      return null; // Returns null if no matching crop is found
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder("Crop{");

      result.append("name='").append(name).append('\'');
      result.append(", snx_names=").append(snx_names);
      result.append(", fertilizer_scheme='").append(fertilizer_scheme).append('\'');
      if (nitrogen_rainfed != null) {
        result.append(", nitrogen_rainfed='").append(nitrogen_rainfed).append('\'');
      }

      if (nitrogen_irrigated != null) {
        result.append(", nitrogen_irrigated='").append(nitrogen_irrigated).append('\'');
      }

      result.append('}');

      return result.toString(); // Return the result from the StringBuilder
    }
  }

  public static Config importConfigCSV(String config_file)
      throws InterruptedException, IOException {

    Config config = new Config();
    Scanner scanner = new Scanner(new File(config_file));

    String all_properties =
        "region_to_use_n region_to_use_s region_to_use_e region_to_use_w nsres ewres co2_level"
            + " run_descriptor minimum_physical_area winter_wheat_countries_csv irrigation_to_try"
            + " weather_prefix weather_folder results_folder planting_months years real_or_happy"
            + " all_or_crop_specific run_crop_model process_results calculate_as_wet_weight"
            + " average_yields calculate_each_year_best_month find_best_yields"
            + " calculate_rf_or_ir_specific_average_yield calculate_rf_or_ir_specific_production"
            + " calculate_rf_plus_ir_production calculate_average_yield_rf_and_ir"
            + " make_rasters_comparing_overall_to_historical create_each_year_png"
            + " create_average_png create_overall_png nitrogen_irrigated nitrogen_rainfed nitrogen"
            + " ";

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim(); // Trim whitespace from ends
      if (line.startsWith("region_to_use_n: ")) {
        config.physical_parameters.region_to_use_n = Double.parseDouble(line.split(": ")[1]);
        all_properties = all_properties.replace("region_to_use_n ", "");
      } else if (line.startsWith("region_to_use_s: ")) {
        all_properties = all_properties.replace("region_to_use_s ", "");
        config.physical_parameters.region_to_use_s = Double.parseDouble(line.split(": ")[1]);
      } else if (line.startsWith("region_to_use_e: ")) {
        all_properties = all_properties.replace("region_to_use_e ", "");
        config.physical_parameters.region_to_use_e = Double.parseDouble(line.split(": ")[1]);
      } else if (line.startsWith("region_to_use_w: ")) {
        all_properties = all_properties.replace("region_to_use_w ", "");
        config.physical_parameters.region_to_use_w = Double.parseDouble(line.split(": ")[1]);
      } else if (line.startsWith("nsres: ")) {
        all_properties = all_properties.replace("nsres ", "");
        config.physical_parameters.nsres = Double.parseDouble(line.split(": ")[1]);
      } else if (line.startsWith("ewres: ")) {
        all_properties = all_properties.replace("ewres ", "");
        config.physical_parameters.ewres = Double.parseDouble(line.split(": ")[1]);
      } else if (line.startsWith("co2_level: ")) {
        all_properties = all_properties.replace("co2_level ", "");
        config.physical_parameters.co2_level = Integer.parseInt(line.split(": ")[1]);
      } else if (line.startsWith("nitrogen: ")) {
        config.physical_parameters.nitrogen = Integer.parseInt(line.split(": ")[1]);
      } else if (line.startsWith("run_descriptor: ")) {
        all_properties = all_properties.replace("run_descriptor ", "");
        config.model_configuration.run_descriptor = line.split(": ")[1].trim();
      } else if (line.startsWith("minimum_physical_area: ")) {
        all_properties = all_properties.replace("minimum_physical_area ", "");
        config.physical_parameters.minimum_physical_area = line.split(": ")[1].trim();
      } else if (line.startsWith("winter_wheat_countries_csv: ")) {
        all_properties = all_properties.replace("winter_wheat_countries_csv ", "");
        config.model_configuration.winter_wheat_countries_csv = line.split(": ")[1].trim();
      } else if (line.startsWith("irrigation_to_try: ")) {
        all_properties = all_properties.replace("irrigation_to_try ", "");
        String[] array = line.split(": ")[1].replace("[", "").replace("]", "").split(", ");
        config.physical_parameters.irrigation_to_try = Arrays.asList(array);
      } else if (line.startsWith("- name: ")) {
        all_properties = all_properties.replace("- ", "");
        Config.Crop crop = new Config.Crop();
        crop.name = line.split(": ")[1].trim();
        config.crops.add(crop);
        // System.out.println("Warning: can only run one crop at a time for a new configuration!");
        // assert config.crops.length() <= 1;
      } else if (line.startsWith("fertilizer_scheme: ")) {
        all_properties = all_properties.replace("fertilizer_scheme ", "");
        config.crops.get(config.crops.size() - 1).fertilizer_scheme = line.split(": ")[1].trim();
      } else if (line.startsWith("nitrogen_irrigated: ")) {
        all_properties = all_properties.replace("nitrogen_irrigated ", "");
        config.crops.get(config.crops.size() - 1).nitrogen_irrigated = line.split(": ")[1].trim();
      } else if (line.startsWith("nitrogen_rainfed: ")) {
        all_properties = all_properties.replace("nitrogen_rainfed ", "");
        config.crops.get(config.crops.size() - 1).nitrogen_rainfed = line.split(": ")[1].trim();
      } else if (line.startsWith("snx_names: ")) {
        all_properties = all_properties.replace("snx_names ", "");
        Config.Crop lastCrop = config.crops.get(config.crops.size() - 1);
        String[] array = line.split(": ")[1].replace("[", "").replace("]", "").split(", ");
        lastCrop.snx_names = new ArrayList<>(Arrays.asList(array));
      } else if (line.startsWith("weather_prefix: ")) {
        all_properties = all_properties.replace("weather_prefix ", "");
        config.physical_parameters.weather_prefix = line.split(": ")[1].trim();
      } else if (line.startsWith("weather_folder: ")) {
        all_properties = all_properties.replace("weather_folder ", "");
        config.physical_parameters.weather_folder = line.split(": ")[1].trim();
      } else if (line.startsWith("results_folder: ")) {
        all_properties = all_properties.replace("results_folder ", "");
        config.physical_parameters.results_folder = line.split(": ")[1].trim();
      } else if (line.startsWith("planting_months: ")) {
        all_properties = all_properties.replace("planting_months ", "");
        String[] array = line.split(": ")[1].replace("[", "").replace("]", "").split(", ");
        config.physical_parameters.planting_months = Arrays.asList(array);

      } else if (line.startsWith("years: ")) {
        all_properties = all_properties.replace("years ", "");
        String[] array = line.split(": ")[1].replace("[", "").replace("]", "").split(", ");
        config.physical_parameters.years = Arrays.asList(array);
      } else if (line.startsWith("real_or_happy: ")) {
        all_properties = all_properties.replace("real_or_happy ", "");
        config.physical_parameters.real_or_happy = line.split(": ")[1].trim();
      } else if (line.startsWith("all_or_crop_specific: ")) {
        all_properties = all_properties.replace("all_or_crop_specific ", "");
        config.physical_parameters.all_or_crop_specific = line.split(": ")[1].trim();
      } else if (line.startsWith("run_crop_model: ")) {
        all_properties = all_properties.replace("run_crop_model ", "");
        config.model_configuration.run_crop_model = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("process_results: ")) {
        all_properties = all_properties.replace("process_results ", "");
        config.model_configuration.process_results = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_as_wet_weight: ")) {
        all_properties = all_properties.replace("calculate_as_wet_weight ", "");
        config.model_configuration.calculate_as_wet_weight =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("average_yields: ")) {
        all_properties = all_properties.replace("average_yields ", "");
        config.model_configuration.average_yields = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_each_year_best_month: ")) {
        all_properties = all_properties.replace("calculate_each_year_best_month ", "");
        config.model_configuration.calculate_each_year_best_month =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("find_best_yields: ")) {
        all_properties = all_properties.replace("find_best_yields ", "");
        config.model_configuration.find_best_yields = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_rf_or_ir_specific_average_yield: ")) {
        all_properties = all_properties.replace("calculate_rf_or_ir_specific_average_yield ", "");
        config.model_configuration.calculate_rf_or_ir_specific_average_yield =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_rf_or_ir_specific_production: ")) {
        all_properties = all_properties.replace("calculate_rf_or_ir_specific_production ", "");
        config.model_configuration.calculate_rf_or_ir_specific_production =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_rf_plus_ir_production: ")) {
        all_properties = all_properties.replace("calculate_rf_plus_ir_production ", "");
        config.model_configuration.calculate_rf_plus_ir_production =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("calculate_average_yield_rf_and_ir: ")) {
        all_properties = all_properties.replace("calculate_average_yield_rf_and_ir ", "");
        config.model_configuration.calculate_average_yield_rf_and_ir =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("make_rasters_comparing_overall_to_historical: ")) {
        all_properties =
            all_properties.replace("make_rasters_comparing_overall_to_historical ", "");
        config.model_configuration.make_rasters_comparing_overall_to_historical =
            Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("create_each_year_png: ")) {
        all_properties = all_properties.replace("create_each_year_png ", "");
        config.model_configuration.create_each_year_png = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("create_average_png: ")) {
        all_properties = all_properties.replace("create_average_png ", "");
        config.model_configuration.create_average_png = Boolean.parseBoolean(line.split(": ")[1]);
      } else if (line.startsWith("create_overall_png: ")) {
        all_properties = all_properties.replace("create_overall_png ", "");
        config.model_configuration.create_overall_png = Boolean.parseBoolean(line.split(": ")[1]);
      }
    }
    scanner.close();
    // System.out.println("all_properties");
    // System.out.println(all_properties);
    assert all_properties.equals("nitrogen ")
            || all_properties.equals("nitrogen_irrigated nitrogen_rainfed ")
        : "remaining unspecified properties in config yaml file can either have only nitrogen field"
            + " specified, or at least one crop with nitrogen_irrigated and nitrogen_rainfed"
            + " specified\n"
            + " (if you specify the nitrogen, this is integer kg/ha in all cropland. If you"
            + " specify raster maps for each crop, you need irrigated and rainfed raster nitrogen"
            + " maps in kg/ha, which should be loaded in the initialization, which can be found"
            + " in the prerun_scripts/upack_all_nitrogen_rasters.sh). Unset properties: \n"
            + all_properties;
    return config;
  }
}
