#MODULE_TOPDIR = /PROJECTS/GRASS_program/grass-6.4.svn_src_snapshot_2011_02_12

MODULE_TOPDIR =  /mnt/GRASS_program/grass

#MODULE_TOPDIR = ../..

PGM = r.in.new

LIBES = $(GISLIB)
DEPENDENCIES = $(GISDEP)

include $(MODULE_TOPDIR)/include/Make/Module.make

ifneq ($(USE_LARGEFILES),)
    EXTRA_CFLAGS = -D_FILE_OFFSET_BITS=64
endif

default: cmd
