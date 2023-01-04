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
*****************************************************************************/

int main(int argc, char *argv[])
{
    void *raster, *ptr;
  /*
    char  *null_row;
    */
    RASTER_MAP_TYPE out_type, map_type;
    char *outfile;
    char *mapset;
    char null_str[80];
    char cell_buf[300];
    int fd;
    int row,col;
    int nrows, ncols, dp;
    int do_stdout;
    FILE *fp;
    double cellsize;
    struct GModule *module;
    struct
    {
	struct Option *map ;
	struct Option *output ;
	struct Option *dp ;
	struct Option *null ;
    } parm;
    struct
    {
        struct Flag *with_coordinates;
        struct Flag *comma_delimited;
        struct Flag *number_lines;
//        struct Flag *singleline;
    } flag;

// stuff added by ricky to see if i can do it right...
    char delimiter_char = '\t';
    struct Option *opt1;
    int sprintf_return;
    char geogfile [500];
    char datafile [500];
    char headfile [500];
    char datainfofile [500];
    char geoginfofile [500];
    char **names;
    char **file_ptr;
    int nfiles;
    double row_to_convert, col_to_convert;
    double easting_here, northing_here;

//    int MAX_NUM_OF_MAPS = 450;
    int MAX_NUM_OF_MAPS = 1500;
    char *all_mapsets[MAX_NUM_OF_MAPS];
    char *namearray[MAX_NUM_OF_MAPS];

    int raster_fd[MAX_NUM_OF_MAPS];
    RASTER_MAP_TYPE out_raster_types[MAX_NUM_OF_MAPS];

    void  *raster_list[MAX_NUM_OF_MAPS];
    void *pointer_list[MAX_NUM_OF_MAPS];
    int MAX_WIDTH_OF_VALUES=16;
    char line_of_output [MAX_NUM_OF_MAPS * MAX_WIDTH_OF_VALUES];
    char line_of_output_copy [MAX_NUM_OF_MAPS * MAX_WIDTH_OF_VALUES];
    int line_of_output_width;
    int line_is_good = 0;

    int n_good_lines = 0;

    FILE *fp_data;
    FILE *fp_geog;
    FILE *fp_data_info, *fp_geog_info;

//    end of stuff added by ricky


    G_gisinit(argv[0]);

    module = G_define_module();
    module->keywords = _("raster");
    module->description =
		_("Converts a raster map layer into a cute data table\ndropping all the missing values. The header info is\nstored in basename_header.txt, the data in basename_data.txt,\nand the row/col/lat/long information in basename_geog.txt\n\nThe header file will be the same as the ARC ASCII\nformat. The data are strung out like -1 format from r.out.arc,\njust with nothing written down for missing values. The geography/location\nfile has a row-to-row correspondence with the data file.\nIts columns are (in order) row, column, latitude, longitude\n(more precisely, northing and easting of the center of the grid cell).");

    /* Define the different options */
    /* new options for input list */
    opt1 = G_define_standard_option(G_OPT_R_INPUTS);
    opt1->description = _("Names of raster maps to be patched together");

    /* old single map option
    parm.map = G_define_option() ;
    parm.map->key        = "input";
    parm.map->type       = TYPE_STRING;
    parm.map->required   = YES;
    parm.map->gisprompt  = "old,cell,raster" ;
    parm.map->description= _("Name of an existing raster map layer");
    */

    parm.output = G_define_option() ;
    parm.output->key        = "output";
    parm.output->type       = TYPE_STRING;
    parm.output->required   = YES;
    parm.output->gisprompt  = "new_file,file,output";
    parm.output->description= _("Base name of where to write output");

    parm.dp = G_define_option() ;
    parm.dp->key        = "dp";
    parm.dp->type       = TYPE_INTEGER;
    parm.dp->required   = NO;
    parm.dp->answer     = "8";
    parm.dp->description= _("Number of decimal places");

    flag.with_coordinates = G_define_flag();
    flag.with_coordinates->key = 'l';
    flag.with_coordinates->description = _("report north/east along with row/col");

    flag.comma_delimited = G_define_flag();
    flag.comma_delimited->key = 'c';
    flag.comma_delimited->description = _("use commas for delimiter instead of tab (not easily imported yet)");

    flag.number_lines = G_define_flag();
    flag.number_lines->key = 'n';
    flag.number_lines->description = _("make the first column a counter for number of valid pixels so far");


    if (G_parser(argc, argv))
       	exit (EXIT_FAILURE);


    sscanf(parm.dp->answer, "%d", &dp);
    if(dp>20 || dp < 0)
       G_fatal_error("dp has to be from 0 to 20");

    outfile =  parm.output->answer;

    if (flag.comma_delimited->answer)
    {
      delimiter_char = ',';
    }
    else
    {
      delimiter_char = '\t';
    }
	/* define the filenames */
    sprintf_return = sprintf(geogfile, "%s_geog.txt",outfile);
    sprintf_return = sprintf(datafile, "%s_data.txt",outfile);
    sprintf_return = sprintf(headfile, "%s_header.txt",outfile);

    sprintf(null_str,"-9999");

    /* new way */
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
      all_mapsets[file_index] = G_find_cell (names[file_index], "");

      if (all_mapsets[file_index] == NULL)
	G_fatal_error (_("Raster map <%s> not found Z"), names[file_index]);

      raster_fd[file_index] = G_open_cell_old (names[file_index], all_mapsets[file_index]);

      if (raster_fd[file_index] < 0)
	G_fatal_error (_("Unable to open raster map <%s> Z"), names[file_index]);


      map_type = G_get_raster_map_type(raster_fd[file_index]);
      out_raster_types[file_index] = map_type;
      raster_list[file_index] =  G_allocate_raster_buf(out_raster_types[file_index]);

//      pointer_list[file_index] = raster_list[file_index];
    }


    nrows = G_window_rows();
    ncols = G_window_cols();

    /* open arc file for writing */
    /* now opening several files */
       if(NULL == (fp = fopen(headfile, "w")))
       {
          G_fatal_error(_("Unable to open file <%s>"), headfile );
       }

	struct Cell_head region;
	char buf[128];

	G_get_window (&region);
	fprintf (fp, "ncols %d\n", region.cols);
	fprintf (fp, "nrows %d\n", region.rows);

	if(G_projection() != 3)  /* Is Projection != LL (3) */
	{
	  G_format_easting (region.west, buf, region.proj);
	  fprintf (fp, "xllcorner %s\n", buf);
	  G_format_northing (region.south, buf, region.proj);
	  fprintf (fp, "yllcorner %s\n", buf);
	}
	else /* yes, lat/long */
	{
	  fprintf (fp, "xllcorner %.13f\n", region.west);
	  fprintf (fp, "yllcorner %.13f\n", region.south);
	}

	cellsize= fabs(region.east - region.west)/region.cols;
	// adding precision for the cellsize
	fprintf(fp, "cellsize %.13f\n", cellsize);
        fprintf(fp, "NODATA_value %s\n", null_str);

    // now close the header file
    fclose(fp);
    
	/* open up the data and geography files */
       if(NULL == (fp_data = fopen(datafile, "w")))
       {
          G_fatal_error(_("Unable to open file <%s>"), headfile );
       }
       if(NULL == (fp_geog = fopen(geogfile, "w")))
       {
          G_fatal_error(_("Unable to open file <%s>"), headfile );
       }


    for (row = 0; row < nrows; row++)
    {

      for (file_index = 0; file_index < nfiles; file_index++)
      {
	if (G_get_raster_row(raster_fd[file_index], raster_list[file_index], row, out_raster_types[file_index]) < 0)
             exit(EXIT_FAILURE);
        pointer_list[file_index] = raster_list[file_index];
      } // end get row for each raster
	G_percent(row, nrows, 2);
      for (col = 0; col < ncols; col++)
      {
        line_of_output_width = sprintf(line_of_output,"");
        line_is_good = 1; // assume good until we find it is bad
        for (file_index = 0; file_index < nfiles; file_index++)
        {
//          pointer_list[file_index] = raster_list[file_index];


          if( (line_is_good == 1) && !G_is_null_value(pointer_list[file_index], out_raster_types[file_index]) )
          {
            if(out_raster_types[file_index] == CELL_TYPE)
            {
//	      line_of_output_width = sprintf (line_of_output,"%s%d\t",line_of_output, *((CELL *) pointer_list[file_index]));
	      line_of_output_width = sprintf (line_of_output,"%s%d%c",line_of_output, *((CELL *) pointer_list[file_index]),delimiter_char);
            }
            else if(out_raster_types[file_index] == FCELL_TYPE)
	    {
	      sprintf(cell_buf, "%.*f", dp, *((FCELL *) pointer_list[file_index]));
	      G_trim_decimal (cell_buf);
	      line_of_output_width = sprintf (line_of_output,"%s%s%c", line_of_output,cell_buf,delimiter_char);
//	      line_of_output_width = sprintf (line_of_output,"%s%s\t", line_of_output,cell_buf);
	    }
            else if(out_raster_types[file_index] == DCELL_TYPE)
	    {
	      sprintf(cell_buf, "%.*f", dp, *((DCELL *) pointer_list[file_index]));
	      G_trim_decimal (cell_buf);
	      line_of_output_width = sprintf (line_of_output,"%s%s%c", line_of_output,cell_buf,delimiter_char);
//	      line_of_output_width = sprintf (line_of_output,"%s%s\t", line_of_output,cell_buf);
	    }
          }
          else
          {
            line_is_good = 0; // there is a missing value in this map
//            continue; // we have to bump up the pointers, so we have to continue
          }


          pointer_list[file_index] = G_incr_void_ptr(pointer_list[file_index],
                      G_raster_size(out_raster_types[file_index]));
        } // end loop over files

        // i always want single line, so do this; i also want the row and column...
        if ( line_is_good == 1 )
        {
 
          // prepend with the line number if necessary
//printf("LOO bef = [%s]\n",line_of_output);
          if (flag.number_lines->answer)
          {
            //line_of_output_width = sprintf(line_of_output, "A%dB%cC%sDE", n_good_lines+1, delimiter_char, line_of_output);
            sprintf(line_of_output_copy, "%s",line_of_output);
            line_of_output_width = sprintf(line_of_output, "%d%c%s", n_good_lines+1, delimiter_char, line_of_output_copy);
          }
//printf("LOO aft = [%s]\n",line_of_output);

          // replace the final character with a newline
          line_of_output[line_of_output_width - 1] = '\n';

	  fprintf (fp_data,"%s",line_of_output); // the goods...

          // write down the row/col etc...
          row_to_convert = row + 0.5;
          col_to_convert = col + 0.5;

          if (flag.with_coordinates->answer)
          {
          northing_here = G_row_to_northing(row_to_convert , &region);
          easting_here  = G_col_to_easting( col_to_convert , &region);

	  fprintf (fp_geog,"%d%c%d%c%.9f%c%.9f\n",row,delimiter_char,col,delimiter_char,northing_here,delimiter_char,easting_here);
//	  fprintf (fp_geog,"%d\t%d\t%.9f\t%.9f\n",row,col,northing_here,easting_here);
          }
          else
          {
	  fprintf (fp_geog,"%d%c%d\n",row,delimiter_char,col);
//	  fprintf (fp_geog,"%d\t%d\n",row,col);
          }

          n_good_lines++;
        } // end if line_is_good == 1

      } // end of for column loop

    } // end of row loop

    /* make sure it got to 100% */
    G_percent(1, 1, 2);

    /* close up the rasters */
    for (file_index = 0; file_index < nfiles; file_index++)
    {
      G_close_cell(raster_fd[file_index]);
    }

    fclose(fp_data);
    fclose(fp_geog);


    /* make some .info.txt files for easy import */

    sprintf_return = sprintf(geoginfofile, "%s_geog.info.txt",outfile);
    sprintf_return = sprintf(datainfofile, "%s_data.info.txt",outfile);
    if(NULL == (fp_geog_info = fopen(geoginfofile, "w")))
    {
      G_fatal_error(_("Unable to open file <%s>"), geoginfofile );
    }
    if(NULL == (fp_data_info = fopen(datainfofile, "w")))
    {
      G_fatal_error(_("Unable to open file <%s>"), datainfofile );
    }

    fprintf(fp_data_info,"%d\t = Number of Rows\n",n_good_lines);
          if (flag.number_lines->answer)
          {
    fprintf(fp_data_info,"%d\t = Number of Columns\n",nfiles + 1);
          }
          else
          {
    fprintf(fp_data_info,"%d\t = Number of Columns\n",nfiles);
          }
    fprintf(fp_data_info,"%d\t = Total Number of Elements\n",nfiles * n_good_lines);
    // Beware the MAGIC NUMBER!!! always assuming on-disk...
    fprintf(fp_data_info,"3\t = The MultiFormatMatrix format the matrix was stored in\n"); 
//    fprintf(fp_data_info,"\t\t = The string used to delimit elements in the Rows\n");
    if (flag.comma_delimited->answer) {
      fprintf(fp_data_info,",\t = The string used to delimit elements in the Rows\n");
    } else {
      fprintf(fp_data_info,"\t\t = The string used to delimit elements in the Rows\n");
    }

    fprintf(fp_geog_info,"%d\t = Number of Rows\n",n_good_lines);
          if (flag.with_coordinates->answer)
          {
    fprintf(fp_geog_info,"%d\t = Number of Columns\n",4); // row/col/north/east
    fprintf(fp_geog_info,"%d\t = Total Number of Elements\n",4 * n_good_lines); // row/col/north/east
          }
          else
          {
    fprintf(fp_geog_info,"%d\t = Number of Columns\n",2); // row/col/north/east
    fprintf(fp_geog_info,"%d\t = Total Number of Elements\n",2 * n_good_lines); // row/col/north/east
          }

    // Beware the MAGIC NUMBER!!! always assuming on-disk...
    fprintf(fp_geog_info,"3\t = The MultiFormatMatrix format the matrix was stored in\n"); 
    if (flag.comma_delimited->answer) {
      fprintf(fp_geog_info,",\t = The string used to delimit elements in the Rows\n");
    } else {
      fprintf(fp_geog_info,"\t\t = The string used to delimit elements in the Rows\n");
    }



    fclose(fp_geog_info);
    fclose(fp_data_info);

    exit(EXIT_SUCCESS);
}




