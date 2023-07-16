import pandas as pd
import matplotlib.pyplot as plt


class CompareYieldsByCountry:
    @staticmethod
    def main(first_yield_raster, second_yield_raster, xlabel, ylabel):
        print("first_yield yield")
        print(first_yield_raster)
        print("second_yield_raster")
        print(second_yield_raster)

        # Load the data
        df1 = pd.read_csv(first_yield_raster, quotechar='"')
        df2 = pd.read_csv(second_yield_raster, quotechar='"')

        # Rename 'Average' columns
        df1.rename(columns={"Average": "Average_first_yield"}, inplace=True)
        df2.rename(columns={"Average": "Average_second_yield"}, inplace=True)

        # Merge the two dataframes on the 'Country' column
        df = pd.merge(df1, df2, on="Country", how="inner")

        # Replace 'No data' with NaN and convert the 'Average' columns to numeric type
        df.replace("No data", pd.NA, inplace=True)
        df["Average_first_yield"] = pd.to_numeric(
            df["Average_first_yield"], errors="coerce"
        )
        df["Average_second_yield"] = pd.to_numeric(
            df["Average_second_yield"], errors="coerce"
        )

        # Drop rows with missing data in either 'Average' column
        df.dropna(subset=["Average_first_yield", "Average_second_yield"], inplace=True)

        # Create the scatter plot
        plt.scatter(df["Average_first_yield"], df["Average_second_yield"])

        # Add a 45 degree line
        min_val = min(df["Average_first_yield"].min(), df["Average_second_yield"].min())
        max_val = max(df["Average_first_yield"].max(), df["Average_second_yield"].max())
        plt.plot([min_val, max_val], [min_val, max_val], "k-", color="r")

        # Add labels and title
        plt.xlabel(xlabel)
        plt.ylabel(ylabel)
        plt.title(f"Comparison of {xlabel} and {ylabel}")

        # Display the plot
        plt.show()


if __name__ == "__main__":
    # model vs historical
    historical_raster = "../wth_historical/country_WHEA_yield.csv"
    modelled_raster = "../wth_control/country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul3_global_Nreal_wet_overall_yield.csv"
    CompareYieldsByCountry.main(
        historical_raster, modelled_raster, "Historical Average", "Projected Average"
    )

    # model vs model_no_stress
    modelled_raster_no_nitrogen_stress = "../wth_control/country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jun28_global_N250_wet_overall_yield.csv"
    modelled_raster = "../wth_control/country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jul3_global_Nreal_wet_overall_yield.csv"
    CompareYieldsByCountry.main(
        modelled_raster_no_nitrogen_stress,
        modelled_raster,
        "Model No Stress Average",
        "Model With Stress Average",
    )
