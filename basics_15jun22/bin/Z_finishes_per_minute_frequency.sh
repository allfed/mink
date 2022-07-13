#!/bin/bash

ls -lt log* | tr -s " " | cut -d" " -f8 | uniq -c | sort | tr -s " " | cut -d" " -f2 | uniq -c | sort -k2 -n | less
