#!/bin/bash
# Builds a singularity container from the mink docker image

singularity build mink.sif docker-daemon:mink:latest
