#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <stdio.h>
#include <grass/gis.h>
#include <grass/glocale.h>

/* added by ricky */


/*
****************************************************************************
*
* MODULE:       r.out.arc
* AUTHOR(S):    Original author: Michael Shapiro (r.out.ascii)
*               modified to r.out.arc by Markus Neteler, Univ. of Hannover
*               neteler geog.uni-hannover.de (11/99)
* PURPOSE:      r.out.arc: writes ARC/INFO ASCII GRID file
* COPYRIGHT:    (C) 2000 by the GRASS Development Team
*
*               This program is free software under the GNU General Public
*   	    	License (>=v2). Read the file COPYING that comes with GRASS
*   	    	for details.
*
* now getting hacked by Ricky Robertson for the purpose of building ASCII
* data tables which do not contain any null values and from which maps
* can be reconstructed
*
* now further hacked to make a unifiying raster that is 1 where all the
* input rasters are present and null if any are missing
*
* basically, this is an intersection rather than union (like r.patch)
*****************************************************************************/

int main(int argc, char *argv[])
{
    void *raster, *ptr;
  /*
    char  *null_row;
    */
    RASTER_MAP_TYPE out_type, map_type;
    char *mapset;
    char cell_buf[300];
    int fd;
    int row,col;
    int nrows, ncols, rtype;
//    int do_stdout;
    FILE *fp;
    double cellsize;
    struct GModule *module;
    struct History history;

	struct Option *output ;
//    struct
//    {
//	//struct Option *map ;
//	struct Option *output ;
//	//struct Option *dp ;
//	//struct Option *null ;
//    } parm;


    // struct
    // {
        // struct Flag *with_coordinates;
        // struct Flag *comma_delimited;
        // struct Flag *number_lines;
//        struct Flag *singleline;
    // } flag;

// stuff added by ricky to see if i can do it right...
    struct Option *opt1;
    char **names;
    char *output_map_name;
    char **file_ptr;
    int nfiles;
    int create_new_raster_response;
    int MAX_NUM_OF_MAPS = 450;
    char *all_mapsets[MAX_NUM_OF_MAPS];
    char *namearray[MAX_NUM_OF_MAPS];

    int raster_fd[MAX_NUM_OF_MAPS];
    RASTER_MAP_TYPE out_raster_types[MAX_NUM_OF_MAPS];

    void  *raster_list[MAX_NUM_OF_MAPS];
    void *pointer_list[MAX_NUM_OF_MAPS];
    int MAX_WIDTH_OF_VALUES=16;
    char line_of_output_copy [MAX_NUM_OF_MAPS * MAX_WIDTH_OF_VALUES];
    int line_of_output_width;
    int line_is_good = 0;


    FILE *fp_data_info, *fp_geog_info;


    // attempt to create the output map
    CELL *cell;

//    end of stuff added by ricky


    G_gisinit(argv[0]);

    module = G_define_module();
    module->keywords = _("raster");
    module->description =
		_("Creates a raster of valuess and nulls showing where all input raster are valid; the value is the number of input rasters that overlap there.");

    /* Define the different options */
    /* new options for input list */
    opt1 = G_define_standard_option(G_OPT_R_INPUTS);
    opt1->description = _("Names of raster maps to be patched together");

//    parm.output = G_define_standard_option(G_OPT_R_OUTPUT);

    output = G_define_standard_option(G_OPT_R_OUTPUT);
    output->description = _("Name of output raster");


//    parm.output->key        = "output";
//    parm.output->type       = TYPE_STRING;
//    parm.output->required   = YES;
//    parm.output->gisprompt  = "new_file,file,output";
//    parm.output->description= _("Base name of where to write output");


    // parm.dp = G_define_option() ;
    // parm.dp->key        = "dp";
    // parm.dp->type       = TYPE_INTEGER;
    // parm.dp->required   = NO;
    // parm.dp->answer     = "8";
    // parm.dp->description= _("Number of decimal places");

    // flag.with_coordinates = G_define_flag();
    // flag.with_coordinates->key = 'l';
    // flag.with_coordinates->description = _("report north/east along with row/col");

    // flag.comma_delimited = G_define_flag();
    // flag.comma_delimited->key = 'c';
    // flag.comma_delimited->description = _("use commas for delimiter instead of tab (not easily imported yet)");

    // flag.number_lines = G_define_flag();
    // flag.number_lines->key = 'n';
    // flag.number_lines->description = _("make the first column a counter for number of valid pixels so far");


    if (G_parser(argc, argv))
       	exit (EXIT_FAILURE);




    /* new way */

    output_map_name = output->answer;
// printf("got here -> _%s_\n",output_map_name); fflush(stdout);

    // count up how many arguments were provided
    names = opt1->answers;
//    for (file_ptr = names, nfiles = 0; *file_ptr != NULL; file_ptr++, nfiles++);
    for (file_ptr = names, nfiles = 0; *file_ptr != NULL; file_ptr++) 
    {
      nfiles++;
    } // ;
    if (nfiles > MAX_NUM_OF_MAPS)
	G_fatal_error (_("Too many maps: %d > %d (maximum)"), nfiles, MAX_NUM_OF_MAPS);

    /* now we will try to open up all the rasters */

    /* try to open all the maps up */
    int file_index;
    for (file_index = 0; file_index < nfiles; file_index++)
    {
// printf("got here -> _%s_\n",names[file_index]); fflush(stdout);
      all_mapsets[file_index] = G_find_cell (names[file_index], "");

      if (all_mapsets[file_index] == NULL)
	G_fatal_error (_("Raster map <%s> not found Z"), names[file_index]);

      raster_fd[file_index] = G_open_cell_old (names[file_index], all_mapsets[file_index]);

      if (raster_fd[file_index] < 0)
	G_fatal_error (_("Unable to open raster map <%s> Z"), names[file_index]);


      map_type = G_get_raster_map_type(raster_fd[file_index]);
      out_raster_types[file_index] = map_type;
      raster_list[file_index] =  G_allocate_raster_buf(out_raster_types[file_index]);

    }


    nrows = G_window_rows();
    ncols = G_window_cols();

    // we want this to be a simple integer map since it will just be ones and nulls
    rtype = CELL_TYPE;


// printf("got here -> A\n"); fflush(stdout);

// printf("got here -> _(%s)_\n",output_map_name); fflush(stdout);

    // try to initializae the buffer for the new map
    cell = G_allocate_c_raster_buf();


    G_set_c_null_value( cell, ncols); // start out with all nulls

// printf("got here -> +++++\n"); fflush(stdout);

  create_new_raster_response = 5;

// printf("got here -> .|.%i.|.\n",create_new_raster_response); fflush(stdout);
// printf("got here -> ...%i;%s...\n",create_new_raster_response,output_map_name); fflush(stdout);

// problem here
  create_new_raster_response = G_open_raster_new(output_map_name, rtype);

// printf("got here -> =====\n"); fflush(stdout);

    if (create_new_raster_response < 0)
      G_fatal_error(_("Unable to create raster map <%s>"), output_map_name);


// printf("got here -> B\n"); fflush(stdout);

    /* now opening several files */

    for (row = 0; row < nrows; row++)
    {

      // i think i was forgetting to initialize...
      G_set_c_null_value( cell, ncols); // start out with all nulls

      for (file_index = 0; file_index < nfiles; file_index++)
      {
	if (G_get_raster_row(raster_fd[file_index], raster_list[file_index], row, out_raster_types[file_index]) < 0)
             exit(EXIT_FAILURE);
        pointer_list[file_index] = raster_list[file_index];
      } // end get row for each raster

	G_percent(row, nrows, 2);

      for (col = 0; col < ncols; col++)
      {
        line_is_good = 1; // assume good until we find it is bad
// printf("               row=%i col=%i\n",row,col); fflush(stdout);
        for (file_index = 0; file_index < nfiles; file_index++)
        {

// printf("   before if   row=%i col=%i file=[%s] value=%i\n",row,col,names[file_index],*((CELL *) pointer_list[file_index])); fflush(stdout);

          if( ( (line_is_good == 1) && !G_is_null_value(pointer_list[file_index], out_raster_types[file_index]) ) )
          {
            // don't do anything...
//            line_is_good = 1;
// printf("GOOD so far... row=%i col=%i file=[%s] value=%i; lig=%i\n",row,col,names[file_index],*((CELL *) pointer_list[file_index]),line_is_good); fflush(stdout);
          } else
          {
            line_is_good = 0; // there is a missing value in this map
// printf("BAD  so far... row=%i col=%i file=[%s] value=%i; lig=%i\n",row,col,names[file_index],*((CELL *) pointer_list[file_index]),line_is_good); fflush(stdout);
          }

          pointer_list[file_index] = G_incr_void_ptr(pointer_list[file_index],
                      G_raster_size(out_raster_types[file_index]));
        } // end loop over files

        // i always want single line, so do this; i also want the row and column...
        if ( line_is_good == 1 )
        {

          // WRITE DOWN A ONE IN THIS SPOT IN THE OUTPUT RASTER
 
          cell[col] = (CELL) nfiles;

        } // end if line_is_good == 1

      } // end of for column loop

      // we are done with the row, so we need to store this row
      G_put_c_raster_row(create_new_raster_response, cell);

    } // end of row loop

//printf("got here -> C\n"); fflush(stdout);

    // now we need to close up the map itself
      G_close_cell(create_new_raster_response);

      G_short_history(output_map_name, "raster", &history);
      G_command_history(&history);
      G_write_history(output_map_name, &history);


    /* make sure it got to 100% */
    G_percent(1, 1, 2);

    /* close up the rasters */
    for (file_index = 0; file_index < nfiles; file_index++)
    {
      G_close_cell(raster_fd[file_index]);
    }





    exit(EXIT_SUCCESS);
}




