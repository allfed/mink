

if [ $# -lt 2 ]; then
  echo "Usage: $0 crop_caps_4_letters raster_to_export ..."
  exit 1
fi

## REMEMBER TO REMOVE THIS!!

g.region n=65 s=-65 w=-170 e=170 nsres=1.88405797101449 ewres=1.25


r.mask -r


# example usage:
# ./export_by_country_yield_and_production.sh WHEA WHEA_yield
crop_caps=$1
yield_raster=$2

. ../default_paths_etc.sh # import spam folder


#set resolution to that of high res raster


# import high resolution dataset for the crop of interest
# https://grass.osgeo.org/grass82/manuals/r.import.html
# import a high res area dataset, setting the proper extent (current region) but maintaining original resolution

# ASSUMPTION GOING IN IS WE HAVE SOME LOW RESOLUTION YIELD LIKE THE FOLLOWING: 
read n s w e <<< $(g.region -g | awk -F'=' '$1=="n" { print $2 } $1=="s" { print $2 } $1=="w" { print $2 } $1=="e" { print $2 }')

# load in the high res cropland area for the crop (if doesn't exist)

# save the north south and east west resolutions
g.region rast="${crop_caps}_cropland_highres" 
g.region n=$n
g.region s=$s
g.region w=$w
g.region e=$e
g.region -g


db.connect driver=dbf database='$GISDBASE/$LOCATION_NAME/$MAPSET/dbf/'
db.connect -p
v.db.connect driver=dbf database='/mnt/data/grassdata/world/DSSAT_essentials_12may11/dbf/' map=cntry05 table=cntry05 -o

# useful command:
# v.info -c cntry05 --quiet

if v.info -c cntry05 --quiet | grep -q "sum_area"; then
  echo "It seems the necessary columns exist. Proceeding to update them."
else
  echo "No line containing 'sum_area' found. creating necessary columns"
  v.db.addcol map=cntry05 columns="sum_area double precision,sum_prod double precision,prod_count int,area_count int"

  # useful commands:
  # v.db.dropcol map=cntry05 column=prod_count
  # v.db.dropcol map=cntry05 column=area_count
  # v.db.dropcol map=cntry05 column=sum_area
  # v.db.dropcol map=cntry05 column=sum_prod
fi





eval `g.findfile element=raster file="${crop_caps}_cropland_highres"`
if [ ! "$file" ] ; then
  r.in.gdal input="${spam_data_folder}spam2010V2r0_global_H_${crop_caps}_A.tif" output="${crop_caps}_cropland_highres"  --overwrite
  skip_area_calcs=true
  echo "uploading statistics for cropland area per country"

  v.vect.stats points=area_centroids areas=cntry05 method=sum pcolumn=value scolumn=sum_area ccolumn=area_count
else
  echo "SKIPPING CROPLAND IMPORT AS HAS ALREADY BEEN DONE! ENSURE THAT'S WHAT YOU WANT!"
  echo "run the following to make this calculation actually happen:"
  echo "g.remove ${crop_caps}_cropland_highres"

  skip_area_calcs=false
fi


r.mapcalc "production_highres = $yield_raster * ${crop_caps}_cropland_highres"

echo ""
echo "calculating production highres point vector"
r.to.vect input=production_highres output=production_centroids feature=point --o


echo ""
echo "uploading statistics for production per country"
v.vect.stats points=production_centroids areas=cntry05 method=sum pcolumn=value scolumn=sum_prod ccolumn=prod_count

#  below might be useful
# country_iso=$(echo "$country" | sed "s/'//g" | sed "s/\ //g"| sed "s/&//g"| sed "s/\.//g" | sed "s/-//g"| sed "s/,//g")
