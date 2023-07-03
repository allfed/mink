

   
    # this is what is needed to set grass up properly
    # the reason we need to do this is that we are running scripts outside of the interactive grass environment, 
    # and thus need to do all the environmental set up ourselves in order to run grass commands in the shell 
    # see https://grasswiki.osgeo.org/wiki/Working_with_GRASS_without_starting_it_explicitly for explanation
    # "bash examples" section, and https://grasswiki.osgeo.org/wiki/GRASS_and_Shell for further explanation
   
   export GISBASE=/usr/local/grass-6.5.svn

   export PATH=$PATH:$GISBASE/bin:$GISBASE/scripts

   export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$GISBASE/lib

   # set PYTHONPATH to include the GRASS Python lib
   if [ ! "$PYTHONPATH" ] ; then
      PYTHONPATH="$GISBASE/etc/python"
   else
      PYTHONPATH="$GISBASE/etc/python:$PYTHONPATH"
   fi
   export PYTHONPATH

   # use process ID (PID) as lock file number:
   export GIS_LOCK=$$

   # settings for graphical output to PNG file (optional)
   export GRASS_PNGFILE=/mnt/data/grass6output.png
   export GRASS_TRUECOLOR=TRUE
   export GRASS_WIDTH=900
   export GRASS_HEIGHT=1200
   
   export GRASS_VERSION="6.5.svn"
   MYGISDBASE=/mnt/data/grassdata
   # MYMAPSET=stuff_for_morgan_25apr23
   MYMAPSET=morgan_DSSAT_cat_0
   MYLOC=world
#set the global grassrc file to individual file name
MYGISRC="$HOME/.grassrc.$GRASS_VERSION.$$"

echo "GISDBASE: $MYGISDBASE" > "$MYGISRC"
echo "LOCATION_NAME: $MYLOC" >> "$MYGISRC"
echo "MAPSET: $MYMAPSET" >> "$MYGISRC"
echo "GRASS_GUI: text" >> "$MYGISRC"
 
# path to GRASS settings file
export GISRC=$MYGISRC
export GRASS_PYTHON=python
export GRASS_MESSAGE_FORMAT=plain
export GRASS_TRUECOLOR=TRUE
export GRASS_TRANSPARENT=TRUE
export GRASS_PNG_AUTO_WRITE=TRUE
export GRASS_GNUPLOT='gnuplot -persist'
export GRASS_WIDTH=640
export GRASS_HEIGHT=480
export GRASS_HTML_BROWSER=firefox
export GRASS_PAGER=cat

#For the temporal modules
export TGISDB_DRIVER=sqlite
export TGISDB_DATABASE=$MYGISDBASE/$MYLOC/PERMANENT/tgis/sqlite.db

# for fun, we can even set the shell prompt to contain a hint on GRASS GIS env being active
#export PS1="[\u@\h \W G-$GRASS_VERSION]$ "

# system vars
export PATH="$GISBASE/bin:$GISBASE/scripts:$PATH"
export LD_LIBRARY_PATH="$GISBASE/lib"
export GRASS_LD_LIBRARY_PATH="$LD_LIBRARY_PATH"
export PYTHONPATH="$GISBASE/etc/python:$PYTHONPATH"
export MANPATH=$MANPATH:$GISBASE/man

export JAVA_HOME=$(dirname $(dirname $(which java)))

# grass doesn't show loading bars on the mapcalc
export GRASS_VERBOSE=0

#export JAVA_HOME="/usr/bin/java"

# test a command
#g.list rast
#v.info zipcodes_wake
