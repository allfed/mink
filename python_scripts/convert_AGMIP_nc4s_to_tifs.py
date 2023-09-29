import os
import netCDF4 as nc
import numpy as np
import rasterio
from rasterio.transform import from_origin

print("\nconverting comparison nc4 files to tif\n")


def main():
    # Set paths and constants
    grass_location = "grassdata/world/AGMIP"
    git_root = "../"
    relative_folder_path = os.path.join(git_root, grass_location)
    nc4_files = [f for f in os.listdir(relative_folder_path) if f.endswith(".nc4")]

    # Process each nc4 file
    filenames = [convert_nc4_to_tif(file, relative_folder_path) for file in nc4_files]

    # Perform operations on processed files
    for file in filenames:
        print(f"./import_tif.sh {grass_location} {file}")

    for file in filenames:
        tif_basename = file.replace(".tif", "")
        cleaned_name = tif_basename.replace("_halfdegree_", "_lowres_cleaned_").replace(
            "_plant-day_", "_planting_months_"
        )

        if "plant-day" in tif_basename:
            convert_planting_days_to_months(tif_basename)

    for file in filenames:
        tif_basename = file.replace(".tif", "")
        cleaned_name = tif_basename.replace("_halfdegree_", "_lowres_cleaned_").replace(
            "_plant-day_", "_planting_months_"
        )
        print(f"./save_ascii.sh grassdata/world/AGMIP {cleaned_name}")

    print("cd ../basics_15jun22/sge_Mink3daily/export_scripts")

    # crop_codes = {"WHEA": "whe", "MAIZ": "mai", "SOYB":"soy", "RICE":"ric"}
    crop_codes = {"WHEA": "whe", "MAIZ": "mai", "SOYB": "soy"}
    rf_and_ir = ["RF", "IR"]
    for crop_code in crop_codes.keys():
        for rf_or_ir in rf_and_ir:
            print_script_for_crop(
                crop_codes[crop_code],
                crop_code,
                rf_or_ir,
            )

    print("\nyou should run the above in a grass session")
    print(
        "\ndone converting comparison nc4 files to tif, importing as rasters, and exporting as asc\n"
    )


def print_script_for_crop(crop_prefix, crop_code, rf_or_ir):
    """
    This function collects the relevant rasters for the detected crop type and
    then passes them to the script.
    """
    yield_raster = f"AGMIP_princeton_{rf_or_ir}_yield_{crop_prefix}_lowres_cleaned_2005"
    planting_months_raster = (
        f"AGMIP_princeton_{rf_or_ir}_planting_months_{crop_prefix}_lowres_cleaned_2005"
    )
    maty_day_raster = (
        f"AGMIP_princeton_{rf_or_ir}_maty-day_{crop_prefix}_lowres_cleaned_2005"
    )

    # if (
    #     os.path.exists(yield_raster)
    #     and os.path.exists(planting_months_raster)
    #     and os.path.exists(maty_day_raster)
    # ):
    print(
        f"./export_by_country_data.sh {crop_code} {yield_raster} {planting_months_raster} {maty_day_raster} grassdata/world/AGMIP"
    )


def convert_nc4_to_tif(file, folder_path):
    # Open the NetCDF file
    ds = nc.Dataset(os.path.join(folder_path, file))

    # Extract data key
    data_key = next(
        key for key in ds.variables.keys() if key not in ["lon", "lat", "time"]
    )

    # Extract and preprocess data
    data = ds[data_key][58]  # Data from year 2005

    if "yield" in data_key:
        data_easy_units = data * 1000  # Convert to kg/ha
    else:
        data_easy_units = data  # Retain same units for 'plant-day' and 'maty-day'

    # Replace fill values with NaNs for GeoTIFF
    data_with_nans = np.ma.filled(data_easy_units, np.nan)

    # Prepare metadata for rasterio
    transform = from_origin(-180, 90, 0.5, 0.5)
    rf_or_ir = "IR" if "_firr_" in file else "RF"

    tif_name = f"AGMIP_princeton_{rf_or_ir}_{data_key}_lowres_cleaned_2005.tif"
    output_tif_name = os.path.join(folder_path, tif_name)

    # Write data to a GeoTIFF file
    with rasterio.open(
        output_tif_name,
        "w",
        driver="GTiff",
        height=data_with_nans.shape[0],
        width=data_with_nans.shape[1],
        count=1,
        dtype=str(data_with_nans.dtype),
        crs="+proj=latlong",
        transform=transform,
    ) as dst:
        dst.write(data_with_nans, 1)

    ds.close()
    return tif_name


def convert_planting_days_to_months(tif_name):
    new_raster = tif_name.replace("plant-day", "planting_months")
    print(
        f'echo "0.0:365.0:0:12" | r.recode --overwrite input={tif_name} output={new_raster} rules=-'
    )


if __name__ == "__main__":
    main()
