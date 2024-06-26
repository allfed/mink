
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
    double mult_fact;
    double x;
    struct GModule *module;
    struct History history;
    struct
    {
//	struct Option *input, *output, *type, *title, *mult;
      struct Option *data_input, *header_input, *geog_input, *output, *type, *title;
    } parm;

// various things added by ricky

    int chars_to_read=9999;

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

    int MAX_NUMBER_OF_MAPS = 1500;
//    int MAX_NUMBER_OF_MAPS = 450;

    int cf[MAX_NUMBER_OF_MAPS];
    CELL *cell[MAX_NUMBER_OF_MAPS];
    FCELL *fcell[MAX_NUMBER_OF_MAPS];
    DCELL *dcell[MAX_NUMBER_OF_MAPS];

    char output_names[MAX_NUMBER_OF_MAPS][500];

      int n_found = 0;
      int storage_char = 0;

    int row_currently_restoring, col_currently_restoring;

    int read_value_success = -1;

    

// end various things added by ricky


    G_gisinit(argv[0]);

    module = G_define_module();
    module->keywords = _("raster");
    module->description =
      _("Does the opposite of r.out.new .");

    parm.data_input = G_define_option();
    parm.data_input->key = "data_input";
    parm.data_input->type = TYPE_STRING;
    parm.data_input->required = YES;
    parm.data_input->description =
      _("name of file containing the data (tab delimited) (no.txt)");
//    parm.data_input->gisprompt = "old_file,file,input";

    parm.geog_input = G_define_option();
    parm.geog_input->key = "geog_input";
    parm.geog_input->type = TYPE_STRING;
    parm.geog_input->required = YES;
    parm.geog_input->description =
      _("name of file containing the row/col/north/east information (no .txt)");
//    parm.location_input->gisprompt = "old_file,file,input";

    parm.header_input = G_define_option();
    parm.header_input->key = "header_input";
    parm.header_input->type = TYPE_STRING;
    parm.header_input->required = YES;
    parm.header_input->description =
      _("name of file containing the ARC ASCII style header information (no .txt)");

    parm.output = G_define_standard_option(G_OPT_R_OUTPUT);

    parm.type = G_define_option();
    parm.type->key = "type";
    parm.type->type = TYPE_STRING;
    parm.type->required = NO;
    parm.type->options = "CELL,FCELL,DCELL";
    parm.type->answer = "FCELL";
    parm.type->description = _("Storage type for resultant raster map");

    parm.title = G_define_option();
    parm.title->key = "title";
    parm.title->key_desc = "\"phrase\"";
    parm.title->type = TYPE_STRING;
    parm.title->required = NO;
    parm.title->description = _("Title for resultant raster map");


    if (G_parser(argc, argv))
      exit(EXIT_FAILURE);
//    char *data_input_char, *location_input_char
    data_input_char = parm.data_input->answer;
    geog_input_char = parm.geog_input->answer;
    header_input_char = parm.header_input->answer;

    output = parm.output->answer;
    if (title = parm.title->answer)
      G_strip(title);

//    sscanf(parm.mult->answer, "%lf", &mult_fact);
    if (strcmp("CELL", parm.type->answer) == 0)
      rtype = CELL_TYPE;
    else if (strcmp("DCELL", parm.type->answer) == 0)
      rtype = DCELL_TYPE;
    else
      rtype = FCELL_TYPE;


    /* deal with the header file */

    sprintf(file_to_open,"%s.txt",header_input_char);
    fd_header = fopen(file_to_open, "r");

    if (fd_header == NULL)
      G_fatal_error(_("Unable to open input file <%s>"),file_to_open);

    if (!gethead(fd_header, &cellhd, &missingval))
      G_fatal_error(_("Can't get cell header"));

//printf("\ndata = [%s]; geog = [%s]; header = [%s]\n",data_input_char,geog_input_char,header_input_char); fflush(stdout);

    nrows = cellhd.rows;
    ncols = cellhd.cols;
    if (G_set_window(&cellhd) < 0)
      G_fatal_error(_("Can't set window"));

    if (nrows != G_window_rows())
      G_fatal_error(_("OOPS: rows changed from %d to %d"), nrows,
        G_window_rows());
    if (ncols != G_window_cols())
      G_fatal_error(_("OOPS: cols changed from %d to %d"), ncols,
        G_window_cols());

    fclose(fd_header);

    /* figure out how many maps to create
       and how many values to restore */
    sprintf(file_to_open,"%s.info.txt",data_input_char);
    fd_data_info = fopen(file_to_open, "r");

    if (fd_data_info == NULL)
      G_fatal_error(_("Unable to open input file <%s>"), file_to_open);

    *junk = '\0'; // clear out the junk holders
    *junk_line= '\0';

    // grab the first line
    fgets(junk_line, chars_to_read, fd_data_info);
    sscanf(junk_line, "%d%s", &n_to_restore, junk);

    // grab the second line
    fgets(junk_line, chars_to_read, fd_data_info);
    sscanf(junk_line, "%d%s", &nmaps, junk);
    if ( nmaps > MAX_NUMBER_OF_MAPS ) {
      G_fatal_error(_("number of maps [%d] > magic MAX number of maps [%d]"), nmaps, MAX_NUMBER_OF_MAPS);
    }

    fgets(junk_line, chars_to_read, fd_data_info); // third line (total elements)
    fgets(junk_line, chars_to_read, fd_data_info); // fourth line (MFM format)
    fgets(junk_line, chars_to_read, fd_data_info); // fifth line (delimiter character)

    fclose(fd_data_info);

    data_delimiter = junk_line[0];

// ^^^^
//        read_value_success = fscanf(fd_data, "%lf%*[,]", &x);
    sprintf(data_format_string, "%%lf%%*[%c]",data_delimiter,data_delimiter);
//printf("DFS = {%s}\n",data_format_string); fflush(stdout);

//printf("last   try: nmaps = [%d]; n_to_restore = [%d]; delimiter = [%c]\n",nmaps,n_to_restore,data_delimiter); fflush(stdout);

  // initialize the buffers...
    for (map_index = 0; map_index < nmaps ; map_index++) {
      switch (rtype) {
      case CELL_TYPE:
        cell[map_index] = G_allocate_c_raster_buf();
        G_set_c_null_value( cell[map_index], ncols); // start out with all nulls
        break;
      case FCELL_TYPE:
        fcell[map_index] = G_allocate_f_raster_buf();
        G_set_f_null_value(fcell[map_index], ncols); // start out with all nulls
        break;
      case DCELL_TYPE:
        dcell[map_index] = G_allocate_d_raster_buf();
        G_set_d_null_value(dcell[map_index], ncols); // start out with all nulls
        break;
      }

      sprintf(output_names[map_index],"%s_%d",output,map_index);
      //printf("output_names[map_index] = [%s]\n",output_names[map_index]);

      cf[map_index] = G_open_raster_new(output_names[map_index], rtype);
      if (cf[map_index] < 0)
        G_fatal_error(_("Unable to create raster map <%s>"), output_names[map_index]);
    } // end for map_index


    /* open up the data and geography files  */
    sprintf(file_to_open,"%s.txt",data_input_char);
    fd_data = fopen(file_to_open, "r");

    if (fd_data == NULL)
      G_fatal_error(_("Unable to open input file <%s>"),file_to_open);

    sprintf(file_to_open,"%s.txt",geog_input_char);
    fd_geog = fopen(file_to_open, "r");

    if (fd_geog == NULL)
      G_fatal_error(_("Unable to open input file <%s>"),file_to_open);

    sprintf(file_to_open,"%s.info.txt",geog_input_char);
    fd_geog_info = fopen(file_to_open, "r");

    if (fd_geog_info == NULL)
      G_fatal_error(_("Unable to open input file <%s>"),file_to_open);

    // pull out the delimiter for the geography file
    // grab the first line
    fgets(junk_line, chars_to_read, fd_geog_info);
    fgets(junk_line, chars_to_read, fd_geog_info); // grab the second line
    fgets(junk_line, chars_to_read, fd_geog_info); // third line (total elements)
    fgets(junk_line, chars_to_read, fd_geog_info); // fourth line (MFM format)
    fgets(junk_line, chars_to_read, fd_geog_info); // fifth line (delimiter character)

    fclose(fd_geog_info);

    geog_delimiter = junk_line[0];

    sprintf(geog_format_string, "%%d%%*[%c]%%d%%*[%c]%%s",geog_delimiter,geog_delimiter);
//printf("GFS = {%s}\n",geog_format_string); fflush(stdout);

/////////////////////////////// curlys match through here... that is, the only open one is for the main...
//
    // initialize...
    row_currently_restoring = 0;
    col_currently_restoring = 0;

/////////////// this curly does not have a correct match...
    for (data_row = 0; data_row < n_to_restore; data_row++) {
      G_percent(data_row, n_to_restore, 3);
      /* pull out the geographic row/col to restore, skip over the lat/long */

      *junk = '\0'; // clear out the junk holders
      *junk_line= '\0';

      // pull out the geographic row and column
      // grab the first line

      fgets(junk_line, chars_to_read, fd_geog);

        sscanf(junk_line, geog_format_string, &row_to_restore, &col_to_restore, junk);
//printf("\nat A\n"); fflush(stdout);
/*
printf("A: white_flag = %d ; row = [%d], col = [%d], junk = [%s], delimiter = [%c]\n",geog_is_white_delimited,row_to_restore, col_to_restore, junk, geog_delimiter);

        sscanf(junk_line, "%d%*[,]%d%*[,]%s", &row_to_restore, &col_to_restore, junk);
printf("B: white_flag = %d ; row = [%d], col = [%d], junk = [%s], delimiter = [%c]\n",geog_is_white_delimited,row_to_restore, col_to_restore, junk, geog_delimiter);
*/

      /* check if we're all good or not */
//printf("A: row = [%d], col = [%d], rcr = [%d]\n",row_to_restore, col_to_restore, row_currently_restoring);
      while ( row_to_restore > row_currently_restoring )
      {
        // we need to null out the rest of this row, store it, and move on...
//printf("[    need new row: (in while)..... rtr = %d >  rcr = %d ; nrows = %d ...    ]\n",row_to_restore,row_currently_restoring,nrows); fflush(stdout);
        for (map_index = 0; map_index < nmaps ; map_index++) {
          switch (rtype) {
            case CELL_TYPE:
                G_put_c_raster_row(cf[map_index], cell[map_index]);
                G_set_c_null_value( cell[map_index], ncols); // start out with all nulls
            break;
            case FCELL_TYPE:
//printf("\nat fcell\n"); fflush(stdout);
//printf("\nat %d / %d / %d\n",row_currently_restoring,map_index,nmaps); fflush(stdout);
                G_put_f_raster_row(cf[map_index], fcell[map_index]);
                G_set_f_null_value(fcell[map_index], ncols); // start out with all nulls
            break;
            case DCELL_TYPE:
                G_put_d_raster_row(cf[map_index], dcell[map_index]);
                G_set_d_null_value(dcell[map_index], ncols); // start out with all nulls
            break;
          } // end of switch 
        } // end for map_index for clearing out...
          row_currently_restoring++;
      } // end while ( row_to_restore > row_currently_restoring )
//printf("\nat B\n"); fflush(stdout);

//printf("[    read actual values      ..... rtr = %d == rcr = %d ; nrows = %d ...    ]\n",row_to_restore,row_currently_restoring,nrows); fflush(stdout);
      for (map_index = 0; map_index < nmaps ; map_index++) {

        /* pull out the data a bit at a time */
        read_value_success = fscanf(fd_data, data_format_string, &x);

        if (read_value_success != 1) {
          G_unopen_cell(cf[map_index]);
          G_fatal_error(_("Data conversion failed at data row %d, map %d"),
            data_row + 1, map_index + 1);
        }

//printf("rtr=[%d]; ctr=[%d]; map=[%d]; num=[%g]\n",row_to_restore, col_to_restore,map_index,x); fflush(stdout);
//printf("value = [%f], row = [%d], col = [%d], map = [%d]\n", x, row_to_restore, col_to_restore,map_index); fflush(stdout);

        switch (rtype) {
        case CELL_TYPE:
          cell[map_index][col_to_restore] = (CELL) x;
          break;
        case FCELL_TYPE:
          fcell[map_index][col_to_restore] = (FCELL) x;
          break;
        case DCELL_TYPE:
          dcell[map_index][col_to_restore] = (DCELL) x;
          break;
        } // end of switch 1
      } // end of map_index
//printf("\nat C\n"); fflush(stdout);
//printf("[    done putting values in buff.. rtr = %d == rcr = %d ; nrows = %d ...    ]\n",row_to_restore,row_currently_restoring,nrows); fflush(stdout);
    } // end of data_rows

//printf("[    finished with data        ... rtr = %d == rcr = %d ; nrows = %d ...    ]\n",row_to_restore,row_currently_restoring,nrows); fflush(stdout);
    // store the last bit and close out
    for (map_index = 0; map_index < nmaps ; map_index++) {
      switch (rtype) {
      case CELL_TYPE:
          G_put_c_raster_row(cf[map_index], cell[map_index]);
          break;
      case FCELL_TYPE:
          G_put_f_raster_row(cf[map_index], fcell[map_index]);
          break;
      case DCELL_TYPE:
          G_put_d_raster_row(cf[map_index], dcell[map_index]);
          break;
      } // end of switch 2
    } // end for map_index (close out)
//printf("[    closed out last row... rcr = %d ; nrows = %d ...    ]\n",row_currently_restoring,nrows); fflush(stdout);
      row_currently_restoring++;

    // make sure we've filled out the region...
//printf("[ AFTER     out last row... rcr = %d ; nrows = %d ...    ]\n",row_currently_restoring,nrows); fflush(stdout);
    while ( row_currently_restoring <= nrows )
    {
//printf("[    trying to finishing out row %d of %d...    ]\n",row_currently_restoring,nrows); fflush(stdout);
// swapping the order of the put/set. i think i had it wrong originally due to copy/paste and no-lookee
// it should be set to null THEN put away...
      // we need to null out the rest of this row, store it, and move on...
      for (map_index = 0; map_index < nmaps ; map_index++) {
      switch (rtype) {
        case CELL_TYPE:
          G_set_c_null_value( cell[map_index], ncols); // start out with all nulls
          G_put_c_raster_row(cf[map_index], cell[map_index]);
          break;
        case FCELL_TYPE:
          G_set_f_null_value(fcell[map_index], ncols); // start out with all nulls
          G_put_f_raster_row(cf[map_index], fcell[map_index]);
          break;
        case DCELL_TYPE:
          G_set_d_null_value(dcell[map_index], ncols); // start out with all nulls
          G_put_d_raster_row(cf[map_index], dcell[map_index]);
          break;
        } // end of switch 
      } // end for map_index for clearing out...
      row_currently_restoring++;
    } // end if row_to_restore > row_currently_restoring

    for (map_index = 0; map_index < nmaps ; map_index++) {
      /* G_message(_("CREATING SUPPORT FILES FOR %s"), output); */
      G_close_cell(cf[map_index]);

      if (title)
        G_put_cell_title(output_names[map_index], title);

      G_short_history(output_names[map_index], "raster", &history);
      G_command_history(&history);
      G_write_history(output_names[map_index], &history);
    } // end for map_index (close out)

    exit(EXIT_SUCCESS);
}

/*
    if (data_delimiter == '\t' || data_delimiter == ' ') {
      data_is_white_delimited = 1;
    } else if (data_delimiter == ',') {
      data_is_comma_delimited = 1;
    } else {
      G_fatal_error(_("data delimiter must be whitespace or comma; actual = [%c]"),data_delimiter);
    }
*/
/*
      if (data_is_white_delimited == 1) {
        read_value_success = fscanf(fd_data, "%lf", &x);
      } else {
//        sscanf(junk_line, "%d%*[,]%d%*[,]%s", &row_to_restore, &col_to_restore, junk);
        read_value_success = fscanf(fd_data, "%lf%*[,]", &x);
      }
*/
/*
    if (geog_delimiter == '\t' || geog_delimiter == ' ') {
      geog_is_white_delimited = 1;
    } else if (geog_delimiter == ',') {
      geog_is_comma_delimited = 1;
    } else {
      G_fatal_error(_("geog delimiter must be whitespace or comma; actual = [%c]"),geog_delimiter);
    }
*/
//        if (fscanf(fd_data, "%lf", &x) != 1) {}
