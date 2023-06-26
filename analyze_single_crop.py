import numpy as np
import pandas as pd
import geopandas as gpd
from shapely.geometry import Point
import matplotlib.pyplot as plt
from make_powerpoint import MakePowerpoint
from matplotlib.colors import ListedColormap

# Load the data into a pandas array
model_results_dictionary = {}

historical_yields_dictionary = {}
cat_or_cntrl_options = ["control"]
# cat_or_cntrl_options = ["control", "catastrophe"]
model_results_dictionary[cat_or_cntrl_options[0]] = {}
if len(cat_or_cntrl_options) == 2:
    model_results_dictionary[cat_or_cntrl_options[1]] = {}
# descriptions = ["Feb8AUS150tg", "Feb13RealNitAUS150tg"]
descriptions = ["Jun15AUSmasktest"]
the_title = "Crop Yields Australia"


DSSAT_to_SPAM_crop_dictionary = {
    "wheat": "WHEA",
    "maize": "MAIZ",
    "soybean": "SOYB",
    "rapeseed": "RAPE",
    "potatoes": "POTA",
}

DSSAT_to_SNX_crop_dictionary = {
    "wheat": "wh",
    "maize": "mz",
    "soybean": "sb",
    "rapeseed": "cn",
    "potatoes": "pt",
}

dssat_crops_to_consider = [
    # "wheat",
    "maize",
    # "rapeseed",
    # "soybean"
    # "potatoes",
]
historical_filenames = []
for dssat_crop in dssat_crops_to_consider:
    historical_filenames.append(
        DSSAT_to_SPAM_crop_dictionary[dssat_crop] + "_yield.asc"
    )


def import_ascii(filename):
    # Load the data into a numpy array

    with open(filename) as f:
        lines = f.readlines()

    # Get the header information from the .asc file

    assert lines[0][-2] == "N" or lines[0][-2] == "S"
    assert lines[1][-2] == "N" or lines[1][-2] == "S"
    assert lines[2][-2] == "E" or lines[2][-2] == "W"
    assert lines[3][-2] == "E" or lines[3][-2] == "W"

    multiplier = 1 if lines[0][-2] == "N" else -1
    north = multiplier * float(lines[0].split(":")[1].strip())
    multiplier = 1 if lines[1][-2] == "N" else -1
    south = multiplier * float(lines[1].split(":")[1].strip())
    multiplier = 1 if lines[2][-2] == "E" else -1
    east = multiplier * float(lines[2].split(":")[1].strip())
    multiplier = 1 if lines[3][-2] == "E" else -1
    west = multiplier * float(lines[3].split(":")[1].strip())

    rows = int(lines[4].split(":")[1].strip())
    cols = int(lines[5].split(":")[1].strip())

    # Create a numpy array with the data
    data = []
    for line in lines[6:]:
        data.extend([float(x) if x != "*" else np.nan for x in line.strip().split()])
    data = np.array(data).reshape((rows, cols))

    # Create a DataFrame with the latitude and longitude information
    lats = np.linspace(south, north, rows)
    lons = np.linspace(west, east, cols)
    lat_lon = np.array(np.meshgrid(lats, lons)).T.reshape(-1, 2)
    df = pd.DataFrame(lat_lon, columns=["lat", "lon"])

    # Flatten the data array and add it to the DataFrame
    df["yields"] = data.flatten()

    # Create a Geopandas DataFrame with the Point geometry
    df["geometry"] = df.apply(lambda x: Point(x["lon"], x["lat"]), axis=1)
    gdf = gpd.GeoDataFrame(df, geometry="geometry")

    return gdf


def plot_against_each_other(
    crop, cat_or_cntrl, model_gdf, historical_gdf, results_folder
):
    # Filter both datasets to only include non-zero values
    model_gdf_nonzero = model_gdf[historical_gdf["yields"] != 0]
    historical_gdf_nonzero = historical_gdf[historical_gdf["yields"] != 0]

    # Calculate the mean yields for both datasets
    model_mean = model_gdf_nonzero["yields"].mean()
    historical_mean = historical_gdf_nonzero["yields"].mean()

    # Plot the data

    # uncomment below

    # Plot the data as a scatterplot
    plt.scatter(
        model_gdf_nonzero["yields"],
        historical_gdf_nonzero["yields"],
        c=historical_gdf_nonzero["lat"],
        cmap="viridis",
    )
    # Add a dotted line where x=y
    x = np.linspace(*plt.xlim())
    plt.plot(x, x, "--", color="k")

    plt.xlabel("Crop Model Yield (kg/ha)")
    plt.ylabel("Historical Yield (kg/ha)")
    plt.colorbar(label="Latitude")
    plt.title(
        crop
        + " Crop Model Yields vs Historical Yields "
        + cat_or_cntrl
        + " "
        + description
    )
    plt.savefig(
        results_folder
        + cat_or_cntrl
        + "_"
        + crop
        + "_Crop_Model_Yield_vs_Historical_Yield.png",
        dpi=200,
        figsize=(5, 5),
    )
    # plt.show()

    # Plot the data as a scatterplot
    # Plot historical values in blue
    plt.scatter(
        historical_gdf_nonzero["lat"],
        historical_gdf_nonzero["yields"],
        c="blue",
        label="Historical Yield",
    )
    # Plot model values in red
    plt.scatter(
        model_gdf_nonzero["lat"],
        model_gdf_nonzero["yields"],
        c="red",
        label="Crop model Yield",
    )
    # Label the x-axis as Latitude
    plt.xlabel("Latitude")
    # Label the y-axis as Yield
    plt.ylabel("Yield (kg/ha)")
    # Add a horizontal line at the mean yields for historical values
    plt.axhline(historical_mean, c="blue", label="Historical Mean")
    # Add a horizontal line at the mean yields for catastrophe values
    plt.axhline(model_mean, c="red", label="Crop Model Mean")
    plt.title(
        crop + "Crop Model vs Historical Yield " + cat_or_cntrl + " " + description
    )
    # Add a legend to distinguish between historical and catastrophe values
    plt.legend()
    # Show the plot
    plt.savefig(
        results_folder + cat_or_cntrl + crop + description + "Yields_vs_Latitude.png",
        dpi=200,
        figsize=(5, 5),
    )
    # plt.show()

    return model_mean, historical_mean


ppt = MakePowerpoint()
ppt.create_title_slide(the_title)

ppt.insert_description_slide(
    "Crop Run Details",
    """This involved a run with no nitrogen stress (250kg/ha elemental nitrogen) 
and all crops planted in all available cropland in Australia. 
All cropland was run for each crop, but only comparisons with existing 
Yields are shown in these slides.
The DSSAT crop 4.7.5 crop model was used. Yields are
estimated circa approximately 2010. 
Cultivars for soybean are accurate, but wheat and maize are using US averages.
Average yield is shown in the figures below in units kg/ha.
Existing irrigated area is used and incorporated into the estimate.""",
)
crop_names = []
table_printout = ""
for description in descriptions:
    for cat_or_cntrl in cat_or_cntrl_options:
        model_results_dictionary[cat_or_cntrl][description] = {}
        historical_yields_dictionary[cat_or_cntrl] = {}
        raster_filenames = []
        for dssat_crop in dssat_crops_to_consider:
            # if dssat_crop == "wheat" and cat_or_cntrl == "control":
            #     raster_description = "Mar03RealNitAUS150tg"
            # else:
            raster_description = description
            raster_filenames += [
                "379_Outdoor_crops_"
                + cat_or_cntrl
                + "_BestYield_noGCMcalendar_p0_"
                + dssat_crop
                + "__"
                + raster_description
                + "_wet_overall_yield.asc"
            ]

        for i, control_filename in enumerate(raster_filenames):
            results_folder = "wth_" + cat_or_cntrl + "/"

            year_gdf = import_ascii(results_folder + control_filename)
            historical_gdf = import_ascii("wth_historical/" + historical_filenames[i])
            crop = historical_filenames[i][0:4]
            crop_names.append(crop)

            model_mean, historical_mean = plot_against_each_other(
                crop, cat_or_cntrl, year_gdf, historical_gdf, results_folder
            )
            model_results_dictionary[cat_or_cntrl][description][crop] = model_mean
            historical_yields_dictionary[cat_or_cntrl][crop] = historical_mean

            ppt.insert_slide(
                crop + " " + cat_or_cntrl,
                "Mar 03 results\n"
                + "Realistic nitrogen stress in model\n"
                + "mean model yields kg/ha:"
                + str(model_mean)
                + "\nmean historical kg/ha:"
                + str(historical_mean),
                results_folder
                + cat_or_cntrl
                + crop
                + description
                + "Yields_vs_Latitude.png",
            )

cat_or_cntrl = "control"
colors = ListedColormap(["red", "blue"])

# create scatterplot
plt.close()

# uncomment below

# plot the scatterplot
fig, ax = plt.subplots()
hist_yields_list = []
model_yields_list = []
values = []
classes = []
print("crop_names")
print(crop_names)
for crop in crop_names:
    x = historical_yields_dictionary[cat_or_cntrl][crop]

    values.append(0)
    classes.append("No Nitrogen Stress")
    hist_yields_list.append(x)
    y = model_results_dictionary[cat_or_cntrl][descriptions[0]][crop]
    model_yields_list.append(y)
    ax.text(x + 0.1, y + 0.1, crop)

    if len(descriptions) > 1:
        values.append(1)
        classes.append("Realistic Nitrogen Stress")
        hist_yields_list.append(x)
        y = model_results_dictionary[cat_or_cntrl][descriptions[1]][crop]
        model_yields_list.append(y)
        ax.text(x + 0.1, y + 0.1, crop)

scatter = plt.scatter(hist_yields_list, model_yields_list, c=values, cmap=colors)
plt.legend(handles=scatter.legend_elements()[0], labels=classes)

# ax.legend()
# Add a dotted line where x=y
x = np.linspace(*plt.xlim())
plt.plot(x, x, "--", color="k")

# add axis labels and legend
plt.ylabel("Crop Model Yield (kg/ha)")
plt.xlabel("Historical Yield (kg/ha)")
plt.title("Australia Crop Model Mean Yields vs Historical Yields " + cat_or_cntrl)

plt.savefig(
    "reports/" + cat_or_cntrl + "Crop_Model_Yield_vs_Historical_Yield.png",
    dpi=200,
    figsize=(5, 5),
)
plt.show()
# plt.close()


# Below: plot control vs catastrophe

if "catastrophe" in cat_or_cntrl_options:
    colors = ListedColormap(["red", "blue"])

    # create scatterplot

    # plot the scatterplot
    fig, ax = plt.subplots()
    hist_yields_list = []
    model_yields_list = []
    values = []
    classes = []
    print("crop_names")
    print(crop_names)
    for crop in crop_names:
        values.append(0)
        classes.append("Realistic Nitrogen Stress")
        x = model_results_dictionary["control"][descriptions[0]][crop]
        hist_yields_list.append(x)
        y = model_results_dictionary["catastrophe"][descriptions[0]][crop]
        model_yields_list.append(y)
        ax.text(x + 0.1, y + 0.1, crop)

        if len(descriptions) > 1:
            values.append(1)
            classes.append("No Nitrogen Stress")
            x = model_results_dictionary["control"][descriptions[1]][crop]
            hist_yields_list.append(x)
            y = model_results_dictionary["catastrophe"][descriptions[1]][crop]
            model_yields_list.append(y)
            ax.text(x + 0.1, y + 0.1, crop)

    scatter = plt.scatter(hist_yields_list, model_yields_list, c=values, cmap=colors)
    plt.legend(handles=scatter.legend_elements()[0], labels=classes)
    # plt.legend(handles=scatter.legend_elements()[0], labels=classes)

    # ax.legend()
    # Add a dotted line where x=y
    x = np.linspace(*plt.xlim())
    plt.plot(x, x, "--", color="k")

    # add axis labels and legend
    plt.ylabel("Catastrophe Model Yield (kg/ha)")
    plt.xlabel("Control Yield (kg/ha)")
    plt.title(" Australia Model Catastrophe Mean Yields vs Model Control mean Yields ")

    plt.savefig(
        "reports/Crop_Model_Yield_vs_Control_Yield.png",
        dpi=200,
        figsize=(5, 5),
    )
    plt.show()
