import rasterio
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.colors as colors


class CompareYieldsByGridCell:
    def process_file(filepath, unwanted_starts):
        with open(filepath, "r") as f:
            lines = f.readlines()
        lines = [
            line
            for line in lines
            if not any([line.startswith(u) for u in unwanted_starts])
        ]
        data = []
        for line in lines:
            items = line.split()
            items = [None if item == "*" else float(item) for item in items]
            data.append(items)
        df = pd.DataFrame(data)
        return df

    def comparison_heatmap(df_model, df_historical):
        # Compute difference
        df_difference = df_model - df_historical
        print(df_model.shape)
        print(df_historical.shape)

        # Create two DataFrames of size 10x10
        # df1 = pd.DataFrame(np.random.random((10, 10)) * 100)
        # df2 = pd.DataFrame(np.random.random((10, 10)) * 100)

        # Introduce some NaNs into the DataFrames
        # nan_mat = np.random.random((10, 10)) < 0.8
        # df1 = df1.mask(nan_mat)
        # df2 = df2.mask(nan_mat)

        # # Compute difference
        # df_difference = df1 - df2

        # Compute the 30th and 70th percentile of non-zero values
        lower_bound = np.nanpercentile(df_difference[df_difference != 0], 30)
        upper_bound = np.nanpercentile(df_difference[df_difference != 0], 70)

        # Create boolean masks for values outside the 30-70 percentile range
        mask_lower = df_difference < lower_bound
        mask_upper = df_difference > upper_bound

        # Apply the masks
        df_difference = df_difference.mask(mask_lower | mask_upper)

        # Replace NaNs with 0 for plotting
        # df_difference = df_difference.fillna(0)

        print(lower_bound)
        print(upper_bound)
        # Create a colormap that normalizes values between the 30th and 70th percentile
        cmap = plt.get_cmap("viridis")
        norm = colors.Normalize(vmin=lower_bound, vmax=upper_bound, clip=True)

        # Create heatmap
        plt.imshow(df_difference, cmap=cmap, interpolation="nearest", norm=norm)
        plt.colorbar(label="Difference")
        plt.title("Heatmap of Difference")
        plt.show()

    def comparison_plot(df_model, df_historical):
        # Plot
        plt.figure(figsize=(10, 6))
        plt.scatter(df_historical.values.flatten(), df_model.values.flatten(), s=1)
        plt.xlabel("Plant every month")
        plt.ylabel("Plant every other month")
        plt.title("Historical vs Model")

        # Adding a 45 degree line
        limits = [
            min([plt.xlim()[0], plt.ylim()[0]]),  # min of both axes
            max([plt.xlim()[1], plt.ylim()[1]]),  # max of both axes
        ]
        plt.plot(
            limits, limits, "k-", color="red", alpha=0.75, zorder=0
        )  # Line of equality in red

        plt.grid(True)
        plt.show()

    def main(modelled_raster, historical_raster):
        print("")
        print("historical yield")
        print(historical_raster)
        print("")
        print("modelled_raster")
        print(modelled_raster)
        print("")

        unwanted_starts = ["north", "south", "east", "west", "rows", "cols"]

        df_model = CompareYieldsByGridCell.process_file(
            modelled_raster, unwanted_starts
        )
        # print("df_model")
        # print(df_model)
        # print(df_model.sum())
        df_historical = CompareYieldsByGridCell.process_file(
            historical_raster, unwanted_starts
        )
        print("df_historical")
        print(df_historical)
        print(df_historical.sum())

        # Save as CSVs
        df_model.to_csv("overall.csv", index=False, header=False)
        df_historical.to_csv("historical.csv", index=False, header=False)

        # CompareYieldsByGridCell.comparison_heatmap(df_model, df_historical)
        CompareYieldsByGridCell.comparison_plot(df_model, df_historical)


if __name__ == "__main__":
    historical_raster = "../wth_historical/WHEA_yield.asc"
    # historical_raster = "../wth_control/379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul11__wet_overall_yield.asc"
    modelled_raster = "../wth_control/379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul13_specific_allmonths_wet_overall_yield.asc"

    CompareYieldsByGridCell.main(modelled_raster, historical_raster)
