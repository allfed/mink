#!/bin/bash

if [ "$1" = "-v" ]; then
  extra="-v"
fi

. default_paths_etc.sh

staging_dir=$staging_directory #~/sge_BigInits/staging_area/
   logs_dir=$logs_dir # ~/sge_BigInits/logs/

echo "-- clearing staging_area [$staging_dir] --"

cd $staging_dir

rm $extra scrip*
rm $extra *.txt
rm $extra *.zip

echo "-- clearing logs [$logs_dir] --"

cd $logs_dir

rm $extra log*.txt

echo "-- clearing chunked inputs [$chunked_input_data_dir] --"

cd $chunked_input_data_dir
rm $extra C*C*.info.txt
rm $extra C*C*.txt
