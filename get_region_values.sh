# lat    long    s
# 36.562    -119.375    california
# 38.438    -86.875    indiana
# 38.438    -96.875    kansas
# 38.438    -76.875    maryland
# 30.938    -89.375    mississippi
# 45.938    -96.875    north dakota
# 45.938    -121.875    washington

# 36.562+1.875/2 = 37.5
# 36.562-1.875/2 = 35.625
# -119.375-1.25/2 = 120
# -119.375+1.25/2 = 118.75

# IMPORT SCRIPTS FOR MAPSPAM DATA
# # MAIZE
# r.in.gdal input=spam2010V2r0_global_H_MAIZ_A.tif output=MAIZ_cropland
# r.in.gdal input=spam2010V2r0_global_Y_MAIZ_A.tif output=MAIZ_yield
# r.in.gdal input=spam2010V2r0_global_H_MAIZ_I.tif output=MAIZ_irrigated_cropland
# r.in.gdal input=spam2010V2r0_global_Y_MAIZ_I.tif output=MAIZ_irrigated_yield
# r.in.gdal input=spam2010V2r0_global_H_MAIZ_R.tif output=MAIZ_rainfed_cropland
# r.in.gdal input=spam2010V2r0_global_Y_MAIZ_R.tif output=MAIZ_rainfed_yield
# # POTATOES
# r.in.gdal input=spam2010V2r0_global_H_POTA_A.tif output=POTA_cropland
# r.in.gdal input=spam2010V2r0_global_Y_POTA_A.tif output=POTA_yield
# r.in.gdal input=spam2010V2r0_global_H_POTA_I.tif output=POTA_irrigated_cropland
# r.in.gdal input=spam2010V2r0_global_Y_POTA_I.tif output=POTA_irrigated_yield
# r.in.gdal input=spam2010V2r0_global_H_POTA_R.tif output=POTA_rainfed_cropland
# r.in.gdal input=spam2010V2r0_global_Y_POTA_R.tif output=POTA_rainfed_yield
# # RAPESEED
# r.in.gdal input=spam2010V2r0_global_H_RAPE_A.tif output=RAPE_cropland
# r.in.gdal input=spam2010V2r0_global_Y_RAPE_A.tif output=RAPE_yield
# r.in.gdal input=spam2010V2r0_global_H_RAPE_I.tif output=RAPE_irrigated_cropland
# r.in.gdal input=spam2010V2r0_global_Y_RAPE_I.tif output=RAPE_irrigated_yield
# r.in.gdal input=spam2010V2r0_global_H_RAPE_R.tif output=RAPE_rainfed_cropland
# r.in.gdal input=spam2010V2r0_global_Y_RAPE_R.tif output=RAPE_rainfed_yield
# # SOYBEAN
# r.in.gdal input=spam2010V2r0_global_H_SOYB_A.tif output=SOYB_cropland
# r.in.gdal input=spam2010V2r0_global_Y_SOYB_A.tif output=SOYB_yield
# r.in.gdal input=spam2010V2r0_global_H_SOYB_I.tif output=SOYB_irrigated_cropland
# r.in.gdal input=spam2010V2r0_global_Y_SOYB_I.tif output=SOYB_irrigated_yield
# r.in.gdal input=spam2010V2r0_global_H_SOYB_R.tif output=SOYB_rainfed_cropland
# r.in.gdal input=spam2010V2r0_global_Y_SOYB_R.tif output=SOYB_rainfed_yield
# # WHEAT
# r.in.gdal input=spam2010V2r0_global_H_WHEA_A.tif output=WHEA_cropland
# r.in.gdal input=spam2010V2r0_global_Y_WHEA_A.tif output=WHEA_yield
# r.in.gdal input=spam2010V2r0_global_H_WHEA_I.tif output=WHEA_irrigated_cropland
# r.in.gdal input=spam2010V2r0_global_Y_WHEA_I.tif output=WHEA_irrigated_yield
# r.in.gdal input=spam2010V2r0_global_H_WHEA_R.tif output=WHEA_rainfed_cropland
# r.in.gdal input=spam2010V2r0_global_Y_WHEA_R.tif output=WHEA_rainfed_yield

lat_lon_list=\
"
36.562\t-119.375
38.438\t-86.875
38.438\t-96.875
38.438\t-76.875
30.938\t-89.375
45.938\t-96.875
45.938\t-121.875
"

raster_names=\
"
_cropland
_yield
_irrigated_cropland
_irrigated_yield
_rainfed_cropland
_rainfed_yield
"
crops=\
"
MAIZ
POTA
WHEA
RAPE
SOYB
"
rm -f point_values_yield_irrigation.csv
touch point_values_yield_irrigation.csv
echo "crop,lat,lon,cropland,yield,irrigated_cropland,irrigated_yield,rainfed_cropland,rainfed_yield" >> point_values_yield_irrigation.csv

radius=5 # 1 would give sinle cell, 3 would give an extra neighbor, 5 gives 2 extra neighbors

for lat_lon in $lat_lon_list; do
    lat=`echo -e "$lat_lon" | cut -f1`
    lon=`echo -e "$lat_lon" | cut -f2`
    latn=`echo "scale=5;($lat)+1.875*$radius/2"|bc`
    lats=`echo "scale=5;($lat)-1.875*$radius/2"|bc`
    lonn=`echo "scale=5;($lon)+1.25*$radius/2"|bc`
    lons=`echo "scale=5;($lon)-1.25*$radius/2"|bc`

    region_to_use="n=$latn s=$lats w=$lons e=$lonn nsres=1.875 ewres=1.25"

    # echo $region_to_use
    echo "latlon: $lat_lon"
    g.region $region_to_use
    for crop in $crops; do
        echo ""
        echo "crop: $crop"
        printf "$crop,$lat,$lon," >> point_values_yield_irrigation.csv
        for rname in $raster_names; do
            echo "rname: $crop$rname"
            output=`r.univar -g "$crop$rname"`
	    echo $output
            val=`echo "$output" | grep "mean=" | cut -d "=" -f2`
            if echo "$output" | grep -q -wi "max=0"; then
                printf "0" >> point_values_yield_irrigation.csv
                echo "zero"
                # grep $output 
            else
                if echo $val; then
                    echo "value: $val"
                    printf "%.1f" $val >> point_values_yield_irrigation.csv 
                else
                    echo "N/A"
                    printf "" >> point_values_yield_irrigation.csv
                fi
            fi 
            printf "," >> point_values_yield_irrigation.csv
        done
        printf "\n" >> point_values_yield_irrigation.csv
    done
done

region_to_use="n=49 s=26 w=-124 e=-66 nsres=1.875 ewres=1.25"
g.region $region_to_use
