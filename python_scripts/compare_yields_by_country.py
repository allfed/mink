import pandas as pd
import matplotlib.pyplot as plt

# Load the data
df1 = pd.read_csv("../wth_historical/country_WHEA_yield.csv", quotechar='"')
df2 = pd.read_csv(
    "../wth_control/country_379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jun28_global_N250_wet_overall_yield.csv",
    quotechar='"',
)

# Merge the two dataframes on the 'Country' column
df = pd.merge(
    df1, df2, on="Country", how="inner", suffixes=("_historical", "_projected")
)

# Replace 'No data' with NaN and convert the 'Average' columns to numeric type
df.replace("No data", pd.NA, inplace=True)
df["Average_historical"] = pd.to_numeric(df["Average_historical"])
df["Average_projected"] = pd.to_numeric(df["Average_projected"])

# Drop rows with missing data in either 'Average' column
df.dropna(subset=["Average_historical", "Average_projected"], inplace=True)

# Create the scatter plot
plt.scatter(df["Average_historical"], df["Average_projected"])

# Add a 45 degree line
min_val = min(df["Average_historical"].min(), df["Average_projected"].min())
max_val = max(df["Average_historical"].max(), df["Average_projected"].max())
plt.plot([min_val, max_val], [min_val, max_val], "k-", color="r")

# Add labels and title
plt.xlabel("Historical Average")
plt.ylabel("Projected Average")
plt.title("Comparison of Historical and Projected Averages")

# Display the plot
plt.show()
