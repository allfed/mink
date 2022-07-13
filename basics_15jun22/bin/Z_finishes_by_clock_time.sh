#!/bin/bash

ls -lt log* | tr -s " " | cut -d" " -f8 | uniq -c | less
