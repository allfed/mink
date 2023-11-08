"""
plotter.py

Description:
    Visualization utilities for yield comparisons. Supports various types of plots such as 
    scatter plots, histograms, and yield-over-time plots.

Author:
    [Your Name or Alias]
"""


# from adjustText import adjust_text
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import numpy as np
import pandas as pd
import re

from stats_functions import StatsFunctions


class Plotter:
    # Function to show average variable by country
    @staticmethod
    def plot_gdf_properties(world, prefix, properties, max_col, crop_nice_name):
        for plot_property in properties:
            fig, ax = plt.subplots(1, 1)

            # Use GeoPandas plot() method to create a choropleth map
            world.boundary.plot(ax=ax, linewidth=1, color="black")  # Plot country boundaries for better clarity

            world.plot(
                column=prefix + "_" + plot_property,
                ax=ax,
                legend=True,
                legend_kwds={
                    "label": prefix + " " + plot_property,
                    "orientation": "horizontal",
                },
                vmax=world[max_col + "_" + plot_property].max(),  # Set the maximum color limit
            )

            plt.title(crop_nice_name + prefix + "_" + plot_property, fontsize=20)
            plt.show(block=False)

    @staticmethod
    def plot_FAOSTAT_hist(df):
        """Create a histogram of Average Wheat Yield based on FAOSTAT data"""
        fig, ax = plt.subplots()  # Explicitly create new figure and axes

        # filter dataframe to include only non-zero values
        # df = df[df["Average Yield 2000-2005 (kg/ha)"] > 500]

        ax.hist(df["Average Yield 2000-2005 (kg/ha)"], bins=20, edgecolor="black")  # Use ax to create histogram
        ax.set_title("Histogram of Average Wheat Yield (2000-2005)")
        ax.set_xlabel("Yield (kg/ha)")
        ax.set_ylabel("Frequency")
        plt.show(block=False)

    @staticmethod
    def plot_hist(df, title):
        """Create a histogram of Average Wheat Yield based on custom data source"""
        fig, ax = plt.subplots()  # Explicitly create new figure and axes

        # filter dataframe to include only non-zero values
        # df = df[df["average_yield"] > 500]

        ax.hist(df["average_yield"], bins=20, edgecolor="black")
        ax.set_title("Histogram of " + title + " Average Wheat Yield (2000-2005)")
        ax.set_xlabel("Yield (kg/ha)")
        ax.set_ylabel("Frequency")
        plt.show(block=False)

    @staticmethod
    def plot_hist_side_by_side(cat_or_cntrl, crop, world, column_list):
        """Create bar plots of multiple datasets side by side within the same bin ranges"""
        print("world.columns")
        print(world.columns)
        print("column_list")
        print(column_list)

        # Check if the columns exist
        for col in column_list:
            if col not in world.columns:
                raise ValueError(f"Column '{col}' not found in DataFrame")
        # Define colors (you may add more if you have more than two columns)
        colors = sns.color_palette("tab10", len(column_list))

        # Find the min and max across all specified columns
        min_val = min(world[col].min() for col in column_list if world[col].min() != 0)
        max_val = max(world[col].max() for col in column_list if world[col].max() != 0)

        # Create bins with numpy by specifying range and number of bins
        bins = np.linspace(min_val, max_val, 20)

        # Initialize an empty DataFrame to hold the counts for each bin and category
        counts_df = pd.DataFrame()

        # Calculate the counts for each bin and category
        for col in column_list:
            data = world[world[col] != 0][col]
            counts, _ = np.histogram(data, bins=bins)
            counts_df[col] = counts

        # Create the bar plot
        ax = counts_df.plot(kind="bar", color=colors, width=0.8)

        # Set the x-tick labels to the bin ranges
        ax.set_xticklabels(
            [f"{int(bins[i])} to {int(bins[i+1])}" for i in range(len(bins) - 1)],
            rotation=45,
        )

        ax.set_title(cat_or_cntrl + ": " + crop + " Comparison of " + " vs ".join(column_list))
        ax.set_xlabel("Yield Range (kg/ha)")
        ax.set_ylabel("Frequency")

        plt.legend(title="Category")
        plt.show(block=False)

    @staticmethod
    def scatter_country_averages(world, observed_col, expected_col, title):
        weights = StatsFunctions.get_weights(world, observed_col, expected_col)
        # filter dataframes to include only non-zero values
        # world.to_csv("Model_vs_spam_data.csv")

        plt.figure()

        # Normalize the SPAM_area values for color mapping
        norm = mcolors.Normalize(
            vmin=weights.min(),
            vmax=weights.max(),
        )
        cmap = plt.cm.viridis

        # Scale theexpected_col +  _area values for size mapping
        sizes = weights / weights.max() * 100

        plt.scatter(
            world[expected_col + "_average_yield"],
            world[observed_col + "_average_yield"],
            c=weights,
            cmap=cmap,
            norm=norm,
            s=sizes,
        )

        plt.colorbar(label=expected_col + " ratio of production to average country")

        # Add dotted line where x = y
        plt.plot(
            [
                world[expected_col + "_average_yield"].min(),
                world[observed_col + "_average_yield"].max(),
            ],
            [
                world[expected_col + "_average_yield"].min(),
                world[observed_col + "_average_yield"].max(),
            ],
            linestyle="dotted",
            color="gray",
        )

        texts = []
        for index, row in world.iterrows():
            texts.append(
                plt.text(
                    row[expected_col + "_average_yield"],
                    row[observed_col + "_average_yield"],
                    row["name"],
                    fontsize=9,
                )
            )

        # adjust_text(
        #     texts,
        #     # arrowprops=dict(arrowstyle="->", color="black"),
        #     # only_move="overlap",
        # )

        # for index, row in world.iterrows():
        #     plt.text(
        #         row[expected_col + "_average_yield"],
        #         row[observed_col + "_average_yield"],
        #         row["name"],
        #         fontsize=9,
        #     )
        r_squared, WLS, rmse, fractional_rmse, d_stat, linear_RMSE = StatsFunctions.get_stats(
            world,
            observed_col + "_average_yield",
            expected_col + "_average_yield",
            weights,
        )

        # Define a suitable x and y coordinate for the R-squared label
        x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
        y_pos1 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.9 + 0.4)  # 90% from the bottom
        y_pos2 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.85 + 0.4)  # 90% from the bottom
        y_pos3 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.8 + 0.4)  # 90% from the bottom
        y_pos4 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.75 + 0.4)  # 90% from the bottom
        y_pos5 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.7 + 0.4)  # 90% from the bottom
        y_pos6 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.65 + 0.4)  # 90% from the bottom

        plt.text(x_pos, y_pos1, f"R^2 = {r_squared:.2f}", fontsize=12)  # Display weighted R-squared on the plot
        plt.text(x_pos, y_pos2, f"WLS Weighted R^2 = {WLS:.2f}", fontsize=12)  # Display weighted R-squared on the plot

        # plt.text(
        #     x_pos, y_pos3, f"log RMSE  = {rmse:.2f}", fontsize=12
        # )  # Display weighted R-squared on the plot
        plt.text(
            x_pos, y_pos3, f"Fractional RMSE = {fractional_rmse:.2f}", fontsize=12
        )  # Display weighted R-squared on the plot
        plt.text(x_pos, y_pos4, f"d_stat = {d_stat:.2f}", fontsize=12)  # Display weighted R-squared on the plot
        plt.text(
            x_pos, y_pos5, f"linear RMSE = {linear_RMSE:.2f} kg/ha", fontsize=12
        )  # Display weighted R-squared on the plot
        plt.title(title)
        plt.xlabel(expected_col + " Average Yield (kg/ha)")
        plt.ylabel(observed_col + " Average Yield (kg/ha)")
        plt.tight_layout()
        plt.show(block=False)

        return world

    @staticmethod
    def scatter_country_production(world, observed_col, expected_col, title):
        # world = world.dropna(
        #     subset=[expected_col + "_average_yield", observed_col + "_average_yield"]
        # )

        plt.figure()

        plt.scatter(
            world[expected_col + "_production"],
            world[observed_col + "_production"],
        )

        # Add dotted line where x = y
        plt.plot(
            [
                world[expected_col + "_production"].min(),
                world[expected_col + "_production"].max(),
            ],
            [
                world[expected_col + "_production"].min(),
                world[expected_col + "_production"].max(),
            ],
            linestyle="dotted",
            color="gray",
        )

        for index, row in world.iterrows():
            plt.text(
                row[expected_col + "_production"],
                row[observed_col + "_production"],
                row["iso_a3"],
                fontsize=9,
            )
        plt.title(title)
        plt.xlabel(expected_col + "Annual Production (kg)")
        plt.ylabel(observed_col + "Annual Production (kg)")
        plt.show(block=False)

        return world

    @staticmethod
    def scatter_country_area(world, observed_col, expected_col, title):
        # world = world.dropna(
        #     subset=[expected_col + "_average_yield", observed_col + "_average_yield"]
        # )

        plt.figure()

        plt.scatter(
            world[expected_col + "_area"],
            world[observed_col + "_area"],
        )

        # Add dotted line where x = y
        plt.plot(
            [
                world[expected_col + "_area"].min(),
                world[expected_col + "_area"].max(),
            ],
            [
                world[expected_col + "_area"].min(),
                world[expected_col + "_area"].max(),
            ],
            linestyle="dotted",
            color="gray",
        )

        for index, row in world.iterrows():
            plt.text(
                row[expected_col + "_area"],
                row[observed_col + "_area"],
                row["iso_a3"],
                fontsize=9,
            )
        plt.title(title)
        plt.xlabel(expected_col + "Annual area (ha)")
        plt.ylabel(observed_col + "Annual area (ha)")
        plt.show(block=False)

        return world

    @staticmethod
    def plot_average_of_rasters_over_time(data_dict, plot_sum = False, separate_figures=True):
        # print("data_dict")
        # print(data_dict)
        # quit()
        # Ensure the input is a dictionary
        if not isinstance(data_dict, dict):
            print("\nInput is not a dictionary. Skipping plotting.\n")
            return

        # Markers for the plot. You can expand this list if you have more crops.
        markers = ["o", "s", "^", "v", "D", "*", "p"]

        # Line styles for distinguishing lines in the second plot.
        line_styles = ["-", "--", "-.", ":"]
        # Index either "observed yield sums" OR "ratio between observed and 
        # historial yield sums"
        column_type = "sum" if plot_sum else "ratio"
        
        # Figure for absolute mean values
        if not separate_figures:
            plt.figure()

        all_values = []  # A list to collect all y-values for determining the max

        for idx, (crop, df) in enumerate(data_dict.items()):
            if df.empty:
                print(f"\nDataFrame for {crop} is empty. Skipping plotting for this crop.\n")
                continue

            # Check and plot available columns
            for col in [f"{column_type}_catastrophe", f"{column_type}_control"]:
                if col in df.columns:
                    plt.plot(
                        df["year"],
                        df[col],
                        label=f"{col.split('_')[1].capitalize()} {crop}",
                        marker=markers[
                            idx % len(markers)
                        ],  # Choose marker based on crop index
                    )

            if plot_sum:
                plt.ylabel("Total Observed Yields ([units])")
            else:
                plt.ylabel("Yields Ratio")
            plt.xlabel("Years")
            plt.title(f"Yields Over Time {crop if separate_figures else ''}")
            plt.legend()
            plt.grid(True)

            all_values.extend(df[col].tolist())

            if separate_figures:
                plt.ylim(0, max(all_values))
                plt.show(block=False)

        if not separate_figures:
            plt.ylim(0, max(all_values))
            plt.show(block=False)

        # Figure for ratio of catastrophe to control
        plt.figure()

        all_ratios = []  # A list to collect all y-values for determining the max

        for crop, df in data_dict.items():
            if "ratio_catastrophe" in df.columns and "ratio_control" in df.columns:
                ratio = df["ratio_catastrophe"] / df["ratio_control"].mean()
                all_ratios.extend(ratio.tolist())
                plt.plot(
                    df["year"],
                    ratio,
                    label=f"Catastrophe : mean(Control) Ratio {crop}",
                    linestyle=line_styles[idx % len(line_styles)],  # Line style for the second plot
                )
        plt.ylim(0, max(all_ratios))
        plt.ylabel("Ratio Catastrophe to Control Production")
        plt.xlabel("Years")
        plt.title(
            "Production Catastrophe Divided by mean Production Control, Over Catastrophe Duration\nThere has been no relocation, but the best crop month was selected."
        )
        plt.title("Production Catastrophe Divided by mean Production Control")
        plt.grid(True)
        plt.legend()
        plt.show(block=False)

    # @staticmethod
    def plot_average_of_rasters_over_time(data_dict):
        # Ensure the input is a dictionary
        if not isinstance(data_dict, dict):
            print("\nInput is not a dictionary. Skipping plotting.\n")
            return

        # Figure for absolute mean values
        plt.figure()

        # Iterate over each crop in the dictionary
        for crop, df in data_dict.items():
            if df.empty:
                print(
                    f"\nDataFrame for {crop} is empty. Skipping plotting for this crop.\n"
                )
                continue

            # Check and plot available columns
            for col in ["ratio_catastrophe", "ratio_control"]:
                if col in df.columns:
                    plt.plot(
                        df["year"],
                        df[col],
                        label=f"{col.split('_')[1].capitalize()} {crop}",
                    )

        plt.ylabel("Yields Ratio")
        plt.xlabel("Years")
        plt.title("Yields Over Time")
        plt.legend()
        plt.grid(True)
        plt.show(block=False)

        # Figure for ratio of catastrophe to control
        plt.figure()
        for crop, df in data_dict.items():
            if "ratio_catastrophe" in df.columns and "ratio_control" in df.columns:
                ratio = df["ratio_catastrophe"] / df["ratio_control"].mean()
                plt.plot(
                    df["year"],
                    ratio,
                    label=f"Catastrophe : mean(Control) Ratio {crop}",
                    # color="red",
                )

        plt.ylabel("Catastrophe to Control Yield Ratio")
        plt.xlabel("Years")
        plt.title("Production Ratio Over Time")
        plt.grid(True)
        plt.legend()
        plt.show(block=False)

    # @staticmethod
    # def plot_average_of_rasters_over_time(df):
    #     if df.empty:
    #         print("\nDataFrame is empty. Skipping plotting.\n")
    #         return

    #     # Figure for absolute mean values
    #     plt.figure()

    #     # Check and plot available columns
    #     for col in ["ratio_catastrophe", "ratio_control"]:
    #         if col in df.columns:
    #             # print("here's the ratios..")
    #             # print(df)
    #             # print(col)
    #             # print(df[col])
    #             # quit()
    #             plt.plot(
    #                 df["year"],
    #                 df[col],
    #                 label=col.split("_")[1].capitalize(),
    #             )

    #     plt.ylabel("Yields Ratio")
    #     plt.xlabel("Years")
    #     plt.title("Yields Over Time")
    #     plt.legend()
    #     plt.grid(True)
    #     plt.show(block=False)

    #     # Figure for ratio of catastrophe to control
    #     if "ratio_catastrophe" in df.columns and "ratio_control" in df.columns:
    #         plt.figure()
    #         ratio = df["ratio_catastrophe"] / df["ratio_control"].mean()
    #         plt.plot(
    #             df["year"], ratio, label="Catastrophe : mean(Control) Ratio", color="red"
    #         )
    #         plt.ylabel("Catastrophe to Control Yield Ratio")
    #         plt.xlabel("Years")
    #         plt.title("Production Ratio Over Time")
    #         plt.grid(True)
    #         plt.legend()
    #         plt.show(block=False)

    # example of how to show the mean yields per hectare rahther than production
    # @staticmethod
    # def plot_average_of_rasters_over_time(df):
    #     # Figure for absolute mean values
    #     plt.figure()

    #     # Check and plot available columns
    #     for col in ["mean_catastrophe", "mean_control"]:
    #         if col in df.columns:
    #             plt.plot(df["year"], df[col], label=col.split("_")[1].capitalize())

    #     plt.ylabel("Yields Ratio")
    #     plt.xlabel("Years")
    #     plt.title("Yields Over Time")
    #     plt.legend()
    #     plt.grid(True)
    #     plt.show(block=False)

    #     # Figure for ratio of catastrophe to control
    #     if "mean_catastrophe" in df.columns and "mean_control" in df.columns:
    #         ratio = df["mean_catastrophe"] / df["mean_control"]

    #         plt.figure()
    #         ratio = df["mean_catastrophe"] / df["mean_control"]
    #         plt.plot(df["year"], ratio, label="Catastrophe/Control Ratio", color="red")
    #         plt.ylabel("Catastrophe to Control Yield Ratio")
    #         plt.xlabel("Years")
    #         plt.title("Yields Ratio Over Time")
    #         plt.grid(True)
    #         plt.legend()
    #         plt.show(block=False)

    @staticmethod
    def scatter_points_with_weights(
        dataframe,
        expected_data_column_name,
        observed_data_column_name,
        weights,
        weights_column_name,
        x_axis_label,
        y_axis_label,
        title,
    ):
        weights_values = weights[weights_column_name]
        weights_normalized = weights_values / np.average(weights_values)

        plt.figure()

        # Normalize the values for color mapping
        norm = mcolors.Normalize(
            vmin=weights_values.min(),
            vmax=weights_values.max(),
        )
        cmap = plt.cm.viridis

        # Scale the weights for size mapping
        sizes = weights_values / weights_values.max() * 100

        plt.scatter(
            dataframe[expected_data_column_name],
            dataframe[observed_data_column_name],
            c=weights_values,
            cmap=cmap,
            norm=norm,
            s=sizes,
        )

        plt.colorbar(label="crop area of cell")

        # Add a dotted line where x=y
        x = np.linspace(*plt.xlim())
        plt.plot(x, x, "--", color="k")

        r_squared, WLS, rmse, fractional_rmse, d_stat, linear_RMSE = StatsFunctions.get_stats(
            dataframe,
            observed_data_column_name,
            expected_data_column_name,
            weights_normalized,
        )

        # Define a suitable x and y coordinate for the R-squared label
        x_pos = plt.xlim()[0] + (plt.xlim()[1] - plt.xlim()[0]) * 0.05  # 5% from the left
        y_pos1 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.9 + 0.4)  # 90% from the bottom
        y_pos2 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.85 + 0.4)  # 90% from the bottom
        y_pos3 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.8 + 0.4)  # 90% from the bottom
        y_pos4 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.75 + 0.4)  # 90% from the bottom
        y_pos5 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.7 + 0.4)  # 90% from the bottom
        y_pos6 = plt.ylim()[0] + (plt.ylim()[1] - plt.ylim()[0]) * (0.65 + 0.4)  # 90% from the bottom

        plt.text(x_pos, y_pos1, f"R^2 = {r_squared:.2f}", fontsize=12)  # Display weighted R-squared on the plot
        plt.text(x_pos, y_pos2, f"WLS Weighted R^2 = {WLS:.2f}", fontsize=12)  # Display weighted R-squared on the plot

        # plt.text(
        #     x_pos, y_pos3, f"log RMSE  = {rmse:.2f}", fontsize=12
        # )  # Display weighted R-squared on the plot
        plt.text(
            x_pos, y_pos3, f"Fractional RMSE = {fractional_rmse:.2f}", fontsize=12
        )  # Display weighted R-squared on the plot
        plt.text(x_pos, y_pos4, f"d_stat = {d_stat:.2f}", fontsize=12)  # Display weighted R-squared on the plot
        plt.text(
            x_pos, y_pos5, f"linear RMSE = {linear_RMSE:.2f} kg/ha", fontsize=12
        )  # Display weighted R-squared on the plot
        plt.title(title)
        plt.xlabel(x_axis_label)
        plt.ylabel(y_axis_label)
        plt.title(title)
        plt.tight_layout()
        plt.show(block=False)

    @staticmethod
    def plot_by_country_by_year(dfs_dict, crops):
        
        # First, determine the common countries
        common_countries_df = pd.merge(dfs_dict['df1'], dfs_dict['df2'], on='iso_a3', how='inner')
        
         # Determine top populous countries from df1 for each crop
        top_countries_dict = {}

        # Filter out all rows with values above 1000 
        for crop_key in crops.keys():
            numeric_columns = [col for col in common_countries_df.columns 
                            if common_countries_df[col].dtype in ['int64', 'float64']
                            and col != 'pop_est']
            common_countries_df = common_countries_df[(common_countries_df[numeric_columns] <= 1000).all(axis=1)]

            # Get the top 5 countries based on population estimates
            top_countries_dict[crop_key]  = common_countries_df.nlargest(5, 'pop_est')['iso_a3'].tolist()

        # Now process and transpose data for each crop and DataFrame
        transposed_data_dict = {}
        for crop_key in crops.keys():
            for df_name, df in dfs_dict.items():
                # Use the top countries determined from df1
                top_countries = top_countries_dict[crop_key]
                pattern = re.compile(re.escape(crop_key), re.IGNORECASE)
                
                # Transpose data for top countries and each crop
                transposed_data = pd.DataFrame()
                for country in top_countries:
                    country_data = df[df['iso_a3'] == country]
                    # Extract columns that match the crop key pattern
                    crop_columns = [col for col in df.columns if re.search(pattern, col)]
                    # Ensure the columns are in the correct year order
                    crop_columns_sorted = sorted(crop_columns, key=lambda x: int(re.search(r'\d+$', x).group()))
                    transposed_data[country] = country_data[crop_columns_sorted].values.flatten()
                
                # The index of transposed_data will be the years, extracted from column names
                transposed_data.index = [int(re.search(r'\d+$', col).group()) for col in crop_columns_sorted]
                transposed_data_dict[(crop_key, df_name)] = transposed_data

        # Create a color map for countries, ensuring each country has the same color
        unique_countries = set([country for _, transposed_df in transposed_data_dict.items() for country in transposed_df.columns])
        colors = plt.cm.rainbow(np.linspace(0, 1, len(unique_countries)))
        color_map = dict(zip(unique_countries, colors))

        # Plotting
        for crop_key, crop_dict in crops.items():
            plt.figure(figsize=(10, 6))  # Set the figure size or adjust as needed
            handled_countries = set()  # To keep track of countries already in the legend
            
            # Define markers for each DataFrame to distinguish them in the plot
            markers = {'df1': 'o', 'df2': 'x'}
            
            # Plot data for each DataFrame
            for df_name in dfs_dict.keys():
                transposed_df = transposed_data_dict.get((crop_key, df_name))
                if transposed_df is not None:
                    for country in transposed_df.columns:
                        # Plot with the same color if the country is already plotted
                        plt.plot(
                            transposed_df.index,
                            transposed_df[country],
                            marker=markers[df_name],
                            color=color_map[country],
                            label=f"{country}" if country not in handled_countries else ""
                        )
                        handled_countries.add(country)  # Add country to the set once labeled
                    
            plt.title(crop_dict['crop_nice_name'] + ' Production Reduction Yields - Top 5 Countries')
            plt.xlabel('Year')
            plt.ylabel('Production Reduction Yields (%)')
            plt.legend()
            plt.grid(True)
            plt.show()
