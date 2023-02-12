import numpy as np
import pandas as pd
import geopandas as gpd
from shapely.geometry import Point
import matplotlib.pyplot as plt
from make_powerpoint import MakePowerpoint

# Load the data into a pandas array

cat_or_cntrl_options = ["control", "catastrophe"]

the_title = "Crop Yields Australia"


def import_ascii(filename):

    # Load the data into a numpy array

    with open(filename) as f:
        lines = f.readlines()

    # Get the header information from the .asc file
    north = float(lines[0].split(":")[1].strip().split("N")[0])
    south = float(lines[1].split(":")[1].strip().split("N")[0])
    east = float(lines[2].split(":")[1].strip().split("W")[0])
    west = float(lines[3].split(":")[1].strip().split("W")[0])
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
    crop, cat_or_cntrl, control_gdf, historical_gdf, results_folder
):
    # Filter both datasets to only include non-zero values
    control_gdf_nonzero = control_gdf[historical_gdf["yields"] != 0]
    historical_gdf_nonzero = historical_gdf[historical_gdf["yields"] != 0]

    # Calculate the mean yields for both datasets
    control_mean = control_gdf_nonzero["yields"].mean()
    historical_mean = historical_gdf_nonzero["yields"].mean()

    # Plot the data

    # Plot the data as a scatterplot
    plt.scatter(
        control_gdf_nonzero["yields"],
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
    plt.title(crop + " Crop Model Yields vs Historical Yields " + cat_or_cntrl)
    plt.savefig(
        results_folder
        + cat_or_cntrl
        + "_"
        + crop
        + "_Crop_Model_Yield_vs_Historical_Yield.png",
        dpi=200,
        figsize=(5, 5),
    )
    plt.show()

    # Plot the data as a scatterplot
    # Plot historical values in blue
    plt.scatter(
        historical_gdf_nonzero["lat"],
        historical_gdf_nonzero["yields"],
        c="blue",
        label="Historical Yield",
    )
    # Plot control values in red
    plt.scatter(
        control_gdf_nonzero["lat"],
        control_gdf_nonzero["yields"],
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
    plt.axhline(control_mean, c="red", label="Crop Model Mean")
    plt.title(crop + "Crop Model vs Historical Yield " + cat_or_cntrl)
    # Add a legend to distinguish between historical and catastrophe values
    plt.legend()
    # Show the plot
    plt.savefig(
        results_folder + cat_or_cntrl + crop + "Yields_vs_Latitude.png",
        dpi=200,
        figsize=(5, 5),
    )
    plt.show()


historical_filenames = ["WHEA_yield.asc", "MAIZ_yield.asc", "SOYB_yield.asc"]
# historical_filenames = ["RAPE_yield.asc", "WHEA_yield.asc", "MAIZ_yield.asc"]
# historical_filenames = ["POTA_yield.asc"]

ppt = MakePowerpoint()
ppt.create_title_slide(the_title)

ppt.insert_description_slide(
    "Crop Run Details",
    "This involved a run with no nitrogen stress and all crops planted\n in all available cropland in Australia. The DSSAT crop 4.7.5 crop model was used. Yields are\n estimated circa approximately 2010. Cultivars for soybean are accurate, but wheat and maize are using US averages.\n Average yield is shown in the figures below in units kg/ha.\n Existing irrigated area is used and incorporated into the estimate.\n Comparison historical yields are for SPAM2010.",
)
for cat_or_cntrl in cat_or_cntrl_options:

    raster_filenames = [
        "379_Outdoor_crops_"
        + cat_or_cntrl
        + "_BestYield_noGCMcalendar_p0_wheat__Feb8AUS150tg_wet_overall_yield.asc",
        "379_Outdoor_crops_"
        + cat_or_cntrl
        + "_BestYield_noGCMcalendar_p0_maize__Feb8AUS150tg_wet_overall_yield.asc",
        "379_Outdoor_crops_"
        + cat_or_cntrl
        + "_BestYield_noGCMcalendar_p0_soybean__Feb8AUS150tg_wet_overall_yield.asc",
        # "379_Greenhouse_catastrophe_BestYield_noGCMcalendar_p0_potatoes__Feb8AUS150tg_wet_overall_yield.asc",
    ]
    for i, control_filename in enumerate(raster_filenames):
        results_folder = "wth_" + cat_or_cntrl + "/"

        control_gdf = import_ascii(results_folder + control_filename)
        historical_gdf = import_ascii(results_folder + historical_filenames[i])
        crop = historical_filenames[i][0:4]
        plot_against_each_other(
            crop, cat_or_cntrl, control_gdf, historical_gdf, results_folder
        )

        # ppt.insert_slide(
        #     crop + " historical",
        #     "",
        #     results_folder
        #     + cat_or_cntrl
        #     + "_"
        #     + crop
        #     + "_Crop_Model_Yield_vs_Historical_Yield.png",
        # )
        ppt.insert_slide(
            crop + " " + cat_or_cntrl,
            "",
            results_folder + cat_or_cntrl + crop + "Yields_vs_Latitude.png",
        )

ppt.save_ppt("baseline.pptx")


# control_gdf = import_ascii(control_filename)
# historical_gdf = import_ascii(historical_filename)
# plot_against_each_other(control_gdf, historical_gdf)
