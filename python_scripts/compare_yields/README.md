
# `compare_yields` - Yield Comparison Toolkit

This toolkit provides a set of utilities to compare model-based crop yield predictions against historical data. It incorporates various functionalities from scatter plot visualizations to statistical measures.

## Running a comparison
You should likely be outside of the singularity module and have a python environment including geopandas. I recommend installing environment.yml in the directory below (`cd ..`) to create a conda environment.

Once you have your conda environment set up, and you want to visualize a completed run of the mink crop model, you should follow the steps below:

1. Edit `yield_comparison_config.yaml`
    - `comparisons_to_run` contains true/false values as to which comparisons which python will show
    - `years` contains the years of the simulation you would like to see 
    - `catastrophe_and_or_control` contains one or both of `catastrophe` and `control` simulations to visualize.
    - `crop_codes` Which crops to analyze, and importantly the `snx_description_control` and `snx_description_catastrophe` which must be set to the `[crop_name]__[run_descriptor]` found in the yaml used for `scenarios` when creating the scenario.
2.  now, run `python3 compare_yields.py` from the current directory. You will see various plots generated as you have specified in step 1. You may also have a newly generated comparison statistics csv for easier further data processing. 

## Features:

- **Country-by-Country Comparison:** Compare model results with historical results on a per-country basis and visualize the comparisons.
  
- **Grid-Cell-by-Grid-Cell Comparison:** Analyze model results against historical data for each grid cell and visualize these results.

- **Statistical Reporting:** Generate statistical reports on the comparison with measures to gauge the alignment between expected and observed results. Reports can be printed, saved as CSV, or visualized through plots.

- **Histogram Generation:** Create histograms for common yield value ranges across different models or data sources.

- **AGMIP Comparison:** Compare results from this model with AGMIP (Agricultural Model Intercomparison and Improvement Project) results.

- **SPAM vs. FAOSTAT Comparison:** A feature to compare SPAM yields with FAOSTAT yields. Useful to ensure SPAM data aligns correctly with FAOSTAT, as SPAM is calibrated to match FAOSTAT.

## File Descriptions:

- `compare_yields.py`: The primary script containing the main logic and function definitions to perform yield comparisons.

- `data_loader.py`: Contains functions related to data loading operations like reading from ASCII files, loading FAOSTAT data, importing by-country CSVs, and more.

- `plotter.py`: Encompasses functions related to plotting. It helps in visualizing country averages, histogram comparisons, and yield-over-time plots among others.

- `stats_functions.py`: Dedicated to statistical operations. Provides functions to compute various statistical measures on the yield data.

- `yield_comparison_config.yaml`: Configuration file that contains settings or parameters required for yield comparisons.

- `output_table.csv`: A potential output file storing comparison results in a tabulated format.


## Diagram

Take a look at the function_calls_within_files.svg diagram to see which files call one another (created in pyan3). Unfortunately, it does not include function calls between different files.

