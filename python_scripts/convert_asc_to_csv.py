import pandas as pd
import matplotlib.pyplot as plt


def process_file(filepath, unwanted_starts):
    with open(filepath, "r") as f:
        lines = f.readlines()
    lines = [
        line for line in lines if not any([line.startswith(u) for u in unwanted_starts])
    ]
    data = []
    for line in lines:
        items = line.split()
        items = [None if item == "*" else float(item) for item in items]
        data.append(items)
    df = pd.DataFrame(data)
    return df


# Define the names of the unwanted rows
unwanted_starts = ["north", "south", "east", "west", "rows", "cols"]

# Filepaths
filepath_model = "wth_control/379_Outdoor_crops_control_BestYield_noGCMcalendar_p0_wheat__Jun28_global_N250_wet_overall_yield.asc"
filepath_historical = "/home/dmrivers/Code/mink/basics_15jun22/more_GRASS_scripts/universal/WHEA_yield.asc"

# Process both files
df_model = process_file(filepath_model, unwanted_starts)
df_historical = process_file(filepath_historical, unwanted_starts)

# Save as CSVs
df_model.to_csv("overall.csv", index=False, header=False)
df_historical.to_csv("historical.csv", index=False, header=False)
# Plot
plt.figure(figsize=(10, 6))
plt.scatter(df_historical.values.flatten(), df_model.values.flatten(), s=1)
plt.xlabel("Historical")
plt.ylabel("Model")
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
