#!/bin/bash
#SBATCH --account=short4hr
#SBATCH --partition=short-serial-4hr
#SBATCH --job-name=myModeltest
#SBATCH -o %j.out
#SBATCH -e %j.err
#SBATCH --time=00:05

echo "were starting"
date
echo "current directory"
pwd
echo "werre done"
