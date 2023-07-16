#!/bin/bash
raster1=$1
raster2=$2

max1float=`r.univar -g map=$raster1 | grep max | awk -F "=" '{print $2}'`
max2float=`r.univar -g map=$raster2 | grep max | awk -F "=" '{print $2}'`

min1float=`r.univar -g map=$raster1 | grep min | awk -F "=" '{print $2}'`
min2float=`r.univar -g map=$raster2 | grep min | awk -F "=" '{print $2}'`

max1int=${max1float%.*}
max2int=${max2float%.*}

min1int=${min1float%.*}
min2int=${min2float%.*}

overall_max_int=$(( $max1int > $max2int ? $max1int : $max2int ))
overall_min_int=$(( $min1int < $min2int ? $min1int : $min2int ))

r.mapcalc "${raster1} = if($raster1 > $overall_max_int, $overall_max_int, $raster1)"
r.mapcalc "${raster2} = if($raster2 > $overall_max_int, $overall_max_int, $raster2)"

r.mapcalc "${raster1} = if($raster1 < $overall_min_int, $overall_min_int, $raster1)"
r.mapcalc "${raster2} = if($raster2 < $overall_min_int, $overall_min_int, $raster2)"

stats=`r.univar $raster1 -g`
min=`echo "$stats" | grep min= | cut -d= -f2`
max=`echo "$stats" | grep max= | cut -d= -f2`

halfmax=`echo $overall_max_int / 2 | bc`

classic_color_string=\
"$overall_min_int blue
$halfmax green
$overall_max_int red"

echo "$classic_color_string" | r.colors $raster1 rules=-
echo "$classic_color_string" | r.colors $raster2 rules=-

./quick_display.sh $raster1 . $overall_max_int $overall_min_int
./quick_display.sh $raster2 . $overall_max_int $overall_min_int
