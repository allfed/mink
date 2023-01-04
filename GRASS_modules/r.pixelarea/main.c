
/****************************************************************************
 *
 * MODULE:       r.in.arc
 * AUTHOR(S):    Unknown German author,
 *                updated by Bill Brown to floating point support (original contributors)
 *               Markus Neteler <neteler itc.it>, Huidae Cho <grass4u gmail.com>,
 *               Roberto Flor <flor itc.it>, Jachym Cepicky <jachym les-ejk.cz>,
 *               Jan-Oliver Wagner <jan intevation.de>
 * PURPOSE:      Import an ESRI ARC/INFO ascii raster file
 * COPYRIGHT:    (C) 1999-2006 by the GRASS Development Team
 *
 *               This program is free software under the GNU General Public
 *               License (>=v2). Read the file COPYING that comes with GRASS
 *               for details.
 *
 *****************************************************************************/
#include <stdlib.h>
#include <string.h>
#include <grass/config.h>
#ifdef HAVE_UNISTD_H
#include <unistd.h>
#endif
#include <grass/gis.h>
#include <grass/glocale.h>
#include "local_proto.h"

//#include "global.h"
#include <grass/gisdefs.h>

FILE *Tmp_fd = NULL;
char *Tmp_file = NULL;

int main(int argc, char *argv[])
{
    char *input;
    char *output;
    char *title;
    FILE *fd;
    int cf_Z;
    struct Cell_head cellhd;
    CELL *cell_Z;
    FCELL *fcell_Z;
    DCELL *dcell_Z;
    int row, col;
    int nrows, ncols;
    static int missingval;
    int rtype;
    int area_prep;
    double mult_fact;
    double x;
    double zfactor;

//    double G_area_of_cell_at_row();

    struct GModule *module;
    struct History history;
    struct
    {
//	struct Option *input, *output, *type, *title, *mult;
      struct Option *data_input, *header_input, *geog_input, *output, *type, *title, *zfactor;
    } parm;

// various things added by ricky

    int chars_to_read=499;

    FILE *fd_header, *fd_geog, *fd_geog_info, *fd_data_info, *fd_data;
    char *data_input_char, *geog_input_char, *header_input_char;
    char file_to_open[chars_to_read + 1];

    int data_row,data_col;
    int row_to_restore, col_to_restore;

    int MAGIC_ROW_INDEX = 0; // in the geographic info file
    int MAGIC_COL_INDEX = 1; // in the geographic info file

    char data_delimiter = '\t';
    char geog_delimiter = '\t';
    int nmaps = -1;
    int n_to_restore = -1;
    char junk_line[chars_to_read + 1];
    char junk[chars_to_read + 1];

    int data_is_white_delimited = 0;
    int data_is_comma_delimited = 0;

    int geog_is_white_delimited = 0;
    int geog_is_comma_delimited = 0;

    char partial_value[chars_to_read + 1];
    char current_char = '\0';

    char data_format_string[25];
    char geog_format_string[25];

    int map_index;

//    int MAX_NUMBER_OF_MAPS = 250;

    int cf; // [MAX_NUMBER_OF_MAPS];
//    CELL *cell[MAX_NUMBER_OF_MAPS];
    FCELL *fcell; //[MAX_NUMBER_OF_MAPS];
//    DCELL *dcell[MAX_NUMBER_OF_MAPS];

//    char output_names[MAX_NUMBER_OF_MAPS][500];

      int n_found = 0;
      int storage_char = 0;

    int row_currently_restoring, col_currently_restoring;

    int read_value_success = -1;

    

// end various things added by ricky


    G_gisinit(argv[0]);

    module = G_define_module();
    module->keywords = _("raster");
    module->description =
      _("Creates a raster showing the area of each pixel.");

    parm.zfactor = G_define_option();
    parm.zfactor->key = "zfactor";
    parm.zfactor->description =
        _("Multiplicative factor to convert from meters to desired units: default = 0.0001 for ha");
    parm.zfactor->type = TYPE_DOUBLE;
    parm.zfactor->required = NO;
    parm.zfactor->answer = "0.0001";

    parm.output = G_define_standard_option(G_OPT_R_OUTPUT);

    if (G_parser(argc, argv))
      exit(EXIT_FAILURE);

    if (sscanf(parm.zfactor->answer, "%lf", &zfactor) != 1 || zfactor <= 0.0) {
        G_fatal_error(_("%s=%s - must be a positive number"),
                      parm.zfactor->key, parm.zfactor->answer);
    }

    output = parm.output->answer;

    rtype = FCELL_TYPE;


    /* deal with the header file */

    // pull from current region
    nrows = G_window_rows();
    ncols = G_window_cols();

// change NOT the window...
//    if (G_set_window(&cellhd) < 0)
//      G_fatal_error(_("Can't set window"));

//    if (nrows != G_window_rows())
//      G_fatal_error(_("OOPS: rows changed from %d to %d"), nrows,
//        G_window_rows());
//    if (ncols != G_window_cols())
//      G_fatal_error(_("OOPS: cols changed from %d to %d"), ncols,
//        G_window_cols());

  // initialize the buffers...
      fcell = G_allocate_f_raster_buf();
      G_set_f_null_value(fcell, ncols); // start out with all nulls

      cf = G_open_raster_new(output, rtype);
      if (cf < 0)
        G_fatal_error(_("Unable to create raster map <%s>"), output);


    /* open up the data and geography files  */
    // initialize...

// printf("got to A\n"); fflush(stdout);

  // try to initialize the area stuff...
  area_prep = G_begin_cell_area_calculations();

    for (data_row = 0; data_row < nrows; data_row++) {
      G_percent(data_row, nrows, 3);

//printf("got to B %d\n", data_row); fflush(stdout);
          x = G_area_of_cell_at_row(data_row) * zfactor;
//printf("got to C %d; area = %g\n", data_row,x); fflush(stdout);
 

      for (col_to_restore = 0; col_to_restore < ncols; col_to_restore++) {
//printf("   got to D %d/%d:%g\n",data_row,col_to_restore,x); fflush(stdout);
          fcell[col_to_restore] = (FCELL) x;

      }

      G_put_f_raster_row(cf, fcell);

    } // end of data_rows

//printf("got to F\n"); fflush(stdout);
    // store the last bit and close out
    G_put_f_raster_row(cf, fcell);

    // make sure we've filled out the region...

      /* G_message(_("CREATING SUPPORT FILES FOR %s"), output); */
      G_close_cell(cf);

// chopping out the title thing
//      if (title)
//        G_put_cell_title(output, title);

      G_short_history(output, "raster", &history);
      G_command_history(&history);
      G_write_history(output, &history);

    exit(EXIT_SUCCESS);
}

