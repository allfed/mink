
Take a look at the function_calls_within_files.svg diagram to see which files call one another (created in pyan3). Unfortunately, it does not include function calls between different files.

# `compare_yields` - Yield Comparison Toolkit

This toolkit provides a set of utilities to compare model-based crop yield predictions against historical data. It incorporates various functionalities from scatter plot visualizations to statistical measures.

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

