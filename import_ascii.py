import rasterio
import numpy as np
import matplotlib.pyplot as plt

# Open the ASCII file using rasterio
with rasterio.open("wth_catastrophe/.asc") as src:
    # Read the raster values into a numpy array
    arr = src.read(1)
    # Plot the heatmap using matplotlib
    plt.imshow(arr, cmap="hot")
    plt.show()
